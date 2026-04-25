# Post-Refinement Watchlist — 2026-04-08

This note replaces the earlier same-day watchlists after:

- the starter-control closures for `weld without a welder`, `knife out of literally nothing`, generic `hypothermia`, generic `paper and ink`, `radio from scrap`, `abandoned factory salvage`, `fire in rain`, `brain tanning`, `glass`, and `what skills should a 10 year old learn first`
- the guarded single-worker Gate rerun at `artifacts/bench/20260408/bench_google_gemma4_4090_gate_post_refinement_r5_w1_20260408.*`

## Current Gate Checkpoint

From `bench_google_gemma4_4090_gate_post_refinement_r5_w1_20260408.json`:

- `30/30` successful
- `0` errors
- `decision_path_counts={'deterministic': 30}`
- `total_generation_time=0`
- `duplicate_citation_total=91`

This is the current clean Gate checkpoint.

## Gate Status

There is no remaining Gate RAG watchlist. All prompts in `Core Regression` and `Quality Floor Tests` now resolve on cited deterministic or control paths before generation.

## Current Practical Next Step

The next real validation task is not more Gate cleanup. It is:

1. fresh Sentinel confirmation on the guarded single-worker profile
2. fresh Coverage confirmation on the guarded single-worker profile
3. then a new watchlist derived from those broad artifacts, not from older Gate-era notes

## Notes

- Do not re-open the newly closed starter prompts unless a new broad artifact shows an actual regression.
- If broad reruns expose remaining issues, prefer fixing prompts that still require normal RAG outside Gate before adding more deterministic rules.
- Use `scripts/report_bench_watchlist.py` on the fresh Sentinel/Coverage JSON artifacts once they exist.
