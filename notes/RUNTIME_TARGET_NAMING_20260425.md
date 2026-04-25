# Runtime Target Naming Decision - 2026-04-25

## Decision

Senku desktop and guide-validation defaults use a split local runtime:

| Purpose | Default endpoint | Default ID | Meaning |
| --- | --- | --- | --- |
| Generation | `http://127.0.0.1:1235/v1` | `gemma-4-e2b-it-litert` | Local LiteRT generation target ID |
| Embeddings | `http://127.0.0.1:1234/v1` | `nomic-ai/text-embedding-nomic-embed-text-v1.5` | Embedding-capable LM Studio target |
| Broad-quality generation | explicit override only | `google/gemma-4-26b-a4b` | Non-default hosted/broad local quality lane |

`gemma-4-e2b-it-litert` is the canonical Senku runtime target ID for the
current LiteRT generation lane. It is not intended to be a public API model
identifier and should not be described as one in user-facing or release docs.

## Current Wiring

- `config.DEFAULT_GEN_URL` is `http://127.0.0.1:1235/v1`.
- `config.LITERT_GEN_MODEL` is `gemma-4-e2b-it-litert`.
- `config.DEFAULT_EMBED_URL` is `http://127.0.0.1:1234/v1`.
- `config.LM_STUDIO_URL` is a legacy embedding alias and should not be used as
  the generation default.
- Manual `query.py` defaults to the LiteRT generation endpoint/model and the
  separate embedding endpoint.
- `scripts/run_guide_prompt_validation.ps1` passes the same generation URL,
  generation model, and embedding URL into `bench.py`.

## Override Rules

Broad-generation or alternate local models are allowed only by explicit
override:

- `SENKU_GEN_MODEL` or `query.py --model`
- `SENKU_GEN_URL` or `query.py --gen-url`
- `SENKU_EMBED_URL` or `query.py --embed-url`

If `query.py` starts with `google/gemma-4-26b-a4b` or another broad model, treat
that as an environment/CLI override first. Check `Get-ChildItem Env:SENKU*` and
the launch command before changing defaults.

## Validation Snapshot

Validated 2026-04-25:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_runtime_profiles tests.test_bench_config tests.test_run_guide_prompt_validation_harness -v
& .\.venvs\senku-validate\Scripts\python.exe -B -c "import config; print(config.GEN_URL, config.GEN_MODEL, config.EMBED_URL, config.LM_STUDIO_URL)"
& .\.venvs\senku-validate\Scripts\python.exe -B query.py --help
```

Results:

- Runtime/default tests passed: `21`.
- Imported config printed:
  `http://127.0.0.1:1235/v1 gemma-4-e2b-it-litert http://127.0.0.1:1234/v1 http://127.0.0.1:1234/v1`
- `query.py --help` shows:
  - model default `gemma-4-e2b-it-litert`
  - generation URL default `http://127.0.0.1:1235/v1`
  - embedding URL default `http://127.0.0.1:1234/v1`

## Follow-Up

If future launchers or Android tooling add model selectors, mirror this split
explicitly: generation target ID, generation endpoint, embedding endpoint, and
local file artifact are separate fields.
