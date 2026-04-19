# AGENTS.md

This is the fast landing page for agents working in this repo.

Keep it light and referential. Put changing execution detail in focused notes and link them from here.

## Repository Layout

- Historical benchmark outputs and progress logs live under [`artifacts/bench`](./artifacts/bench)
- Ad hoc benchmark prompt packs and temporary batch files live under [`artifacts/prompts`](./artifacts/prompts)
- One-off handoffs, research notes, and review notes live under [`notes`](./notes)
- The Android app subproject lives under [`android-app`](./android-app)

## Quick Start

The checked-in `venv/` is POSIX-origin. On Windows, create your own venv (for example `py -3 -m venv venv_win`) and activate that instead, or run under WSL.
PowerShell:
```powershell
. .\venv\Scripts\Activate.ps1
python query.py
python ingest.py --stats
```

POSIX:
```bash
source venv/bin/activate
python3 query.py
python3 ingest.py --stats
```

## Core Entry Points

Desktop:
- [`config.py`](./config.py): runtime config, models, prompt, `TOP_K`
- [`query.py`](./query.py): query decomposition, retrieval, prompt build, streaming
- [`bench.py`](./bench.py): benchmark runner
- [`ingest.py`](./ingest.py): guide ingestion into ChromaDB
- [`deterministic_special_case_registry.py`](./deterministic_special_case_registry.py): declarative deterministic rule registry
- [`special_case_builders.py`](./special_case_builders.py): deterministic/control-path response templates
- [`GUIDE_PLAN.md`](./GUIDE_PLAN.md): living guide-direction backlog; keep true KB gaps separate from retrieval-surfacing work
- [`guideupdates.md`](./guideupdates.md): concrete active guide defects only

Shared support:
- [`lmstudio_utils.py`](./lmstudio_utils.py): LM Studio retry and embedding fallback helpers
- [`guide_catalog.py`](./guide_catalog.py): live guide-id/slug catalog used for citation validation
- [`token_estimation.py`](./token_estimation.py): chunk-sizing helper

Mobile:
- [`android-app/README.md`](./android-app/README.md): Android build/install notes
- [`scripts/export_mobile_pack.py`](./scripts/export_mobile_pack.py): mobile pack export entry point
- [`mobile_pack.py`](./mobile_pack.py): mobile pack builder/exporter
- [`scripts/push_mobile_pack_to_android.ps1`](./scripts/push_mobile_pack_to_android.ps1): hot-swap a refreshed mobile pack without rebuilding the APK
- [`scripts/run_android_instrumented_ui_smoke.ps1`](./scripts/run_android_instrumented_ui_smoke.ps1): instrumentation-backed Android smoke lane with screenshots, dumps, logcat, and `summary.json`
- [`scripts/run_android_prompt.ps1`](./scripts/run_android_prompt.ps1): emulator prompt automation harness
- [`scripts/run_android_search_log_only.ps1`](./scripts/run_android_search_log_only.ps1): long Android retrieval checks from logcat without repeated UI-dump polling
- [`scripts/run_android_ui_validation_pack.ps1`](./scripts/run_android_ui_validation_pack.ps1): default multi-case Android UI validation lane
- [`scripts/start_senku_emulator_matrix.ps1`](./scripts/start_senku_emulator_matrix.ps1): launch the fixed four-emulator posture matrix, with read-only as the default mode
- [`scripts/build_android_ui_state_pack_parallel.ps1`](./scripts/build_android_ui_state_pack_parallel.ps1): concurrent four-lane screenshot/dump sweep across the fixed emulator matrix
- [`scripts/start_senku_device_mirrors.ps1`](./scripts/start_senku_device_mirrors.ps1): stable view-only scrcpy launcher for the physical phone/tablet mirrors with always-on-top defaults
- [`scripts/run_guide_prompt_validation.ps1`](./scripts/run_guide_prompt_validation.ps1): run targeted guide-direction prompt packs (`w`, `x`, `y`, `af`, `ag`, `ah`, `ai`) after a clean re-ingest

Async helper lanes:
- OpenCode sidecar workflow: use `$opencode-sidecar` for async review, bounded edits, polling, follow-up input, and viewer-backed task lifecycle
- OpenCode sidecar scripts:
  - [`scripts/enqueue_opencode_sidecar.ps1`](./scripts/enqueue_opencode_sidecar.ps1)
  - [`scripts/get_opencode_sidecar_status.ps1`](./scripts/get_opencode_sidecar_status.ps1)
  - [`scripts/get_opencode_sidecar_result.ps1`](./scripts/get_opencode_sidecar_result.ps1)
  - [`scripts/enqueue_opencode_sidecar_batch.ps1`](./scripts/enqueue_opencode_sidecar_batch.ps1)
- Async Qwen scout helpers:
  - [`scripts/start_qwen27_scout_job.ps1`](./scripts/start_qwen27_scout_job.ps1)
  - [`scripts/get_qwen27_scout_job.ps1`](./scripts/get_qwen27_scout_job.ps1)
- OpenCode-backed local Qwen sidecar:
  - [`scripts/enqueue_qwen27_sidecar.ps1`](./scripts/enqueue_qwen27_sidecar.ps1)

Use async Qwen27 for quick prompt-only scouting. Use sidecar for file-edit or file-review tasks.

## Lane Indexes

- Generic engineering swarm lane: [`notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md`](./notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md)
- Guide lane index: [`notes/GUIDE_INDEX.md`](./notes/GUIDE_INDEX.md)
- Android lane index: [`notes/ANDROID_INDEX.md`](./notes/ANDROID_INDEX.md)
- Swarm / sidecar / model-routing index: [`notes/SWARM_INDEX.md`](./notes/SWARM_INDEX.md)

## Local MCP Helpers

- [`opencode.json`](./opencode.json): enables repo-local MCP servers for OpenCode
- `context7`: use for library/docs grounding when you want current API or setup details without generic web-search drift
- `git`: use for repo history, diff, branch, and git-aware reasoning inside OpenCode
- `sequential-thinking`: use for stepwise planning, decomposition, and "think before acting" prompts
- `puppeteer`: use for browser-based UI testing, screenshots, navigation, and repeatable web checks

Keep these as helper lanes. Prefer the existing OpenCode sidecar, Qwen scout, and Spark workers for the actual swarm workflow.

## Testing

- Read [`TESTING_METHODOLOGY.md`](./TESTING_METHODOLOGY.md) for the living desktop/mobile validation workflow
- Focused local tests: `python3 -m unittest discover -s tests -v`
- Deterministic citation/routing guard: `python3 scripts/validate_special_cases.py`
- Current prompt suite: `test_prompts.txt` with 171 prompts across 24 sections

## Current Baseline

- Desktop retrieval uses hybrid retrieval plus metadata-aware reranking and structured session state in [`query.py`](./query.py)
- Android app is the active parity project: retrieval quality, prompt shaping, and chat/session continuity
- Android validation now defaults to the fixed four-emulator posture matrix:
  - `5556` phone portrait
  - `5560` phone landscape
  - `5554` tablet portrait
  - `5558` tablet landscape
- Daily Android work is emulator-first; physical phone/tablet checks are milestone truth checks and mirror/review lanes
- Mobile now targets both `E2B` and `E4B`: `E2B` is the practical floor, `E4B` is the quality tier to keep evaluating
- Session-aware follow-up behavior is part of normal validation now, not a later extra
- Multi-model cost posture: keep Codex thin and strategic; push cheap scouting to local Qwen, bounded patches to Spark, and longer async work to OpenCode sidecars first
- Preferred routing ladder for this repo: `GLM 5.1 sidecar > Spark > Qwen 27B > Qwen 9B`
- Latest broad visual baseline:
  - [`artifacts/external_review/ui_review_20260417_gallery/index.html`](./artifacts/external_review/ui_review_20260417_gallery/index.html)

## Practical Cautions

- Re-ingest after guide edits before trusting desktop retrieval results
- Distinguish true knowledge gaps from retrieval-language issues before adding guides
- For safety-critical guide edits, grep the repo for shared formulas, thresholds, dose-like statements, and other invariants before validation
- For guide-family cleanup, prefer stronger routing blocks and reciprocal links before merging files
- Prompt-pack validation should use an explicit LiteRT generation host plus a separate embedding-capable endpoint; do not silently fall back to the desktop LM Studio 26B lane
- LiteRT guide-validation smoke now works with `top_k=8`; treat the desktop `top_k=24` default as too large for the current 4096-token LiteRT context window
- For wide sidecar coding waves, prefer spaced submission to avoid `artifacts/opencode/sidecar/index.jsonl` contention
- For local Qwen 27B, prefer the pinned identifier `qwen27_58k` and avoid running 9B + 27B concurrently on constrained VRAM
- Sidecar result lane reliability: `get_opencode_sidecar_result.ps1` now prefers latest non-empty assistant text when completed text is empty, so terminal output and `text` fields stay aligned with status previews
