# llama.cpp Migration Spec

Migrate Senku from LM Studio to llama.cpp server for a self-contained portable deployment.

## Why

- Eliminate LM Studio's Electron overhead (~1GB RAM savings)
- Single binary, no GUI dependency
- `--parallel N` for concurrent requests
- Portable bundle: everything in one folder
- Required for phone/embedded deployment path

## Prerequisites

```bash
brew install llama.cpp
```

## GGUF Models

Copy from LM Studio cache or place the GGUF files locally:

```bash
# Embedding (use F16, only 274MB — quantization degrades retrieval quality)
huggingface-cli download nomic-ai/nomic-embed-text-v1.5-GGUF nomic-embed-text-v1.5.f16.gguf --local-dir models/

# Generation (Q4_K_M is the sweet spot)
# Place a GGUF build for google/gemma-4-26b-a4b at this path:
cp /path/to/gemma-4-26b-a4b-Q4_K_M.gguf models/
```

LM Studio stores GGUFs in `~/.cache/lm-studio/models/` — can symlink instead of re-downloading.

## Architecture Change

llama-server loads one model per instance. Need two servers:

```bash
# Embedding server (port 8801)
llama-server \
  --model models/nomic-embed-text-v1.5.f16.gguf \
  --port 8801 \
  --embedding \
  --ctx-size 2048 \
  --batch-size 2048 \
  --n-gpu-layers 99

# Generation server (port 8802)
llama-server \
  --model models/gemma-4-26b-a4b-Q4_K_M.gguf \
  --port 8802 \
  --ctx-size 8192 \
  --n-gpu-layers 99 \
  --parallel 2 \
  --flash-attn
```

Key flags:
- `--embedding` is REQUIRED on the embed instance, must NOT be set on generation
- `--n-gpu-layers 99` offloads all layers to Metal on Apple Silicon
- `--flash-attn` reduces memory and speeds up generation
- `--parallel 2` allows concurrent requests (good for bench.py)

## API Compatibility

Wire-compatible with LM Studio. Same endpoints, same JSON format:
- `POST /v1/embeddings` (requires `--embedding` flag on server)
- `POST /v1/chat/completions` (supports `stream: true` with SSE)
- `GET /v1/models`

No changes needed to HTTP/JSON parsing logic.

## Code Changes Required

### config.py

Split the single URL:

```python
# Before
LM_STUDIO_URL = "http://localhost:1234/v1"

# After
EMBED_URL = "http://localhost:8801/v1"
GEN_URL = "http://localhost:8802/v1"
```

### query.py (4 changes)

1. `embed_query()`: `config.LM_STUDIO_URL` -> `config.EMBED_URL`
2. `embed_batch()`: `config.LM_STUDIO_URL` -> `config.EMBED_URL`
3. `stream_response()`: `config.LM_STUDIO_URL` -> `config.GEN_URL`
4. `main()` connectivity check: check both URLs separately

### ingest.py (2 changes)

1. `embed_batch()`: `config.LM_STUDIO_URL` -> `config.EMBED_URL`
2. Connectivity check: only check `config.EMBED_URL` (ingest doesn't need generation)

### bench.py (3 changes)

1. Default `gen_urls`: use `config.GEN_URL`
2. Connectivity check: use `config.EMBED_URL` for local embedding verification
3. `--urls` flag already works for remote generation servers

### All files

Update error messages from "LM Studio" to "inference server" (or keep generic).

## RAM Estimates

| Component | Size |
|---|---|
| nomic-embed-text-v1.5 (F16) | ~300MB |
| gemma-4-26b-a4b (Q4_K_M) | ~15-16GB |
| KV cache (ctx=8192, parallel=2) | ~200MB |
| llama-server overhead (2 instances) | ~50MB |
| **Total** | **~16GB** |

M5 24GB: comfortable (~8GB free for OS + ChromaDB)
M5 18GB: tight — consider Q3_K_M (~12GB) or reduce ctx-size to 4096

## Launcher Script

Create `start.sh`:

```bash
#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
MODELS="$DIR/models"

echo "Starting Senku servers..."

llama-server \
  --model "$MODELS/nomic-embed-text-v1.5.f16.gguf" \
  --port 8801 --embedding --ctx-size 2048 --batch-size 2048 \
  --n-gpu-layers 99 &
EMBED_PID=$!

llama-server \
  --model "$MODELS/gemma-4-26b-a4b-Q4_K_M.gguf" \
  --port 8802 --ctx-size 8192 --n-gpu-layers 99 \
  --parallel 2 --flash-attn &
GEN_PID=$!

echo "Embedding server: PID $EMBED_PID (port 8801)"
echo "Generation server: PID $GEN_PID (port 8802)"
echo "Waiting for servers to initialize..."
sleep 3

# Verify
curl -s http://localhost:8801/v1/models > /dev/null && echo "Embedding: OK" || echo "Embedding: FAILED"
curl -s http://localhost:8802/v1/models > /dev/null && echo "Generation: OK" || echo "Generation: FAILED"

echo ""
echo "Ready. Run: python3 query.py"
echo "Stop with: kill $EMBED_PID $GEN_PID"
wait
```

## Target Folder Structure

```
senku/
  models/
    nomic-embed-text-v1.5.f16.gguf
    gemma-4-26b-a4b-Q4_K_M.gguf
  db/                  # chromadb
  guides/              # source markdown
  config.py
  query.py
  ingest.py
  bench.py
  start.sh
  requirements.txt
```

## Estimated Effort

~15-20 lines of code changes across 4 files. Plus the launcher script. Should take one focused session.

## Verification

After migration, run the bench suite to confirm no regressions:

```bash
python3 bench.py --output bench_llamacpp.md
```

Compare against latest LM Studio bench report.
