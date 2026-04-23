# Current Local Testing State — 2026-04-10

Historical note: This bundle-era local-testing snapshot was relocated from the repo root into `notes/dated/` in D13. The `/Users/tbronson/...` path examples below are preserved as historical context, not the current quick-start for this repo.

## Immediate Goal

Continue the Senku local-model comparison on a laptop-oriented profile.

Primary practical local candidates:

- `qwen/qwen3.5-9b`
- `gemma-4-e4b-it`

Optional larger candidate only if runtime is acceptable:

- `google/gemma-4-26b-a4b`

## Important Repo Change Already Made

`bench.py` now supports:

```bash
--model <model_id>
```

That means comparison runs no longer require swapping `config.py` between lanes.

## Local Constraints From Prior Session

- local-only testing
- laptop inference is slow
- loaded max context was lowered
- previous smoke and bench attempts were canceled before they produced completed comparison artifacts

## Prompt Packs To Use

- `artifacts/prompts/adhoc/model_compare_smoke_pack_20260410.txt`
- `artifacts/prompts/adhoc/model_compare_gemma_qwen_pack_20260409.txt`

## Recommended First Commands

```bash
cd /Users/tbronson/Desktop/senku_local_testing_bundle_20260410
source venv/bin/activate
zsh scripts/verify_local_runtime.sh
```

Then:

```bash
zsh scripts/run_bench_guarded.sh \
  --prompts artifacts/prompts/adhoc/model_compare_smoke_pack_20260410.txt \
  --mode demo \
  --model qwen/qwen3.5-9b \
  --max-completion-tokens 768 \
  --urls http://localhost:1234/v1 \
  --embed-url http://localhost:1234/v1 \
  --output artifacts/bench/20260410/bench_qwen35_9b_q8_smoke_demo_w1_20260410.md
```

Repeat with:

```bash
--model gemma-4-e4b-it
```

If both smoke runs are acceptable, continue with the full pack in `default` mode at the same cap first.

## Why Start With `768` Completion Tokens

The laptop profile is the limiting factor right now.

Starting smaller:

- keeps smoke runs practical
- reduces waiting on scope-sprawl answers
- is enough to compare decision quality, compactness, and support discipline before attempting broader lanes

## Local Model Snapshot

The file `LM_STUDIO_MODELS_20260410.json` contains the visible local model ids at bundle creation time.
