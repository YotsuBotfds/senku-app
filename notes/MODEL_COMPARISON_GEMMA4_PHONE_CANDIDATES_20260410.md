# Gemma 4 Phone Candidates — 2026-04-10

## Purpose

This note captures the local phone-oriented Gemma comparison state after the fresh `e4b` and `e2b` runs on 2026-04-10, so the next agent does not need to reconstruct it from chat history or raw JSON artifacts.

## Current Candidate Ladder

- `gemma-4-e2b-it`
  - current efficiency floor
- `gemma-4-e4b-it@q4_k_s`
  - current practical phone candidate
- `gemma-4-e4b-it@bf16`
  - desktop-only quality/control check, not the likely phone deployment form

## Important Runtime Notes

### 1. `e4b q4_k_s` concurrency

Measured on the 24-prompt compare pack in `demo` mode:

- `w1` wall-clock throughput: `48.16 tok/s`
- `w3` wall-clock throughput: `64.09 tok/s`
- wall-clock throughput increase: about `33.1%`
- wall-time reduction: about `26.6%`

Per-prompt generation speed dropped under concurrency:

- `w1` prompt-level speed: `70.75 tok/s`
- `w3` prompt-level speed: `39.75 tok/s`

Interpretation:

- `w3` is better for batch throughput
- `w1` is better for single-prompt latency

### 2. `e2b` concurrency

Measured on the 24-prompt compare pack in `demo` mode:

- `w1` wall-clock throughput: `43.03 tok/s`
- `w4` wall-clock throughput: `65.11 tok/s`
- wall-clock throughput increase: about `51.3%`
- wall-time reduction: about `30.4%`

Per-prompt generation speed dropped under concurrency:

- `w1` prompt-level speed: `70.25 tok/s`
- `w4` prompt-level speed: `41.07 tok/s`

Interpretation:

- `w4` is clearly useful for batch throughput
- `w1` is still the better latency profile for one-off prompt reads

### 3. Bench architecture caveat

`bench.py` still prepares retrieval/build serially before generation fan-out.

Practical implication:

- generation concurrency improves batch throughput
- prep remains a bottleneck
- embedding concurrency is not meaningfully exercised by the current harness yet

### 4. `e2b w4` runtime caveat

An earlier `w4` attempt produced initial LM Studio `400` failures under concurrency.

After the operator reloaded the runtime with a higher context setting, a fresh `w4` rerun completed cleanly.

Interpretation:

- the first failure looked like a runtime/load/config issue, not a hard model limitation
- keep this in mind before treating any future burst of `400`s as a pure model-quality problem

## Artifacts Produced

### `e4b bf16`

- `artifacts/bench/20260410/bench_gemma4_e4b_bf16_smoke_demo_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e4b_bf16_compare_pack_default_w1_20260410_t768.*`

### `e4b q4_k_s`

- `artifacts/bench/20260410/bench_gemma4_e4b_q4ks_smoke_demo_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e4b_q4ks_compare_pack_default_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e4b_q4ks_compare_pack_demo_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e4b_q4ks_compare_pack_demo_w3_20260410_t768.*`

### `e2b`

- `artifacts/bench/20260410/bench_gemma4_e2b_q4_0_smoke_demo_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e2b_q4_0_compare_pack_default_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e2b_q4_0_compare_pack_demo_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e2b_q4_0_compare_pack_demo_w4_20260410_t1024.*`

## Summary Metrics

### `e4b bf16`

#### Smoke demo (`w1`, `768`)

- prompts: `3`
- successful: `3`
- total generation time: `32.4s`
- avg per prompt: `10.8s`
- duplicate citations: `13`

#### Compare pack default (`w1`, `768`)

- prompts: `24`
- successful: `24`
- total generation time: `237.8s`
- avg per prompt: `9.9s`
- duplicate citations: `51`

### `e4b q4_k_s`

#### Smoke demo (`w1`, `768`)

- prompts: `3`
- successful: `3`
- total generation time: `18.9s`
- avg per prompt: `6.3s`
- duplicate citations: `8`

#### Compare pack default (`w1`, `768`)

- prompts: `24`
- successful: `24`
- total generation time: `158.7s`
- avg per prompt: `6.6s`
- duplicate citations: `61`

#### Compare pack demo (`w1`, `768`)

- prompts: `24`
- successful: `24`
- total generation time: `136.5s`
- avg per prompt: `5.7s`
- duplicate citations: `63`

#### Compare pack demo (`w3`, `768`)

- prompts: `24`
- successful: `24`
- total generation time: `237.2s`
- avg per prompt: `9.9s`
- duplicate citations: `52`

### `e2b`

#### Smoke demo (`w1`, `768`)

- prompts: `3`
- successful: `3`
- total generation time: `13.9s`
- avg per prompt: `4.6s`
- duplicate citations: `10`

#### Compare pack default (`w1`, `768`)

- prompts: `24`
- successful: `24`
- total generation time: `109.3s`
- avg per prompt: `4.6s`
- duplicate citations: `46`

#### Compare pack demo (`w1`, `768`)

- prompts: `24`
- successful: `24`
- total generation time: `104.6s`
- avg per prompt: `4.4s`
- duplicate citations: `47`

#### Compare pack demo (`w4`, `1024`)

- prompts: `24`
- successful: `24`
- total generation time: `185.4s`
- avg per prompt: `7.7s`
- duplicate citations: `50`

## Current Read

### `e4b q4_k_s`

Strengths:

- clearly fast enough for practical use
- strong phone-oriented candidate
- better smoke duplication than BF16
- validated `w3` batch throughput improvement

Weaknesses:

- compare-pack duplicate-citation totals are a bit noisy
- still needs broader validation beyond the compare pack before final selection

### `e2b`

Strengths:

- surprisingly strong so far
- faster than `e4b q4_k_s` on the current local compare-pack lanes
- lower duplicate-citation totals than `e4b q4_k_s` on both current `w1` compare-pack lanes
- `w4` batching looks viable after runtime reload

Weaknesses:

- outputs are visibly more compressed on at least some smoke prompts
- still needs broader `Gate/Coverage/Sentinel` validation before calling it the phone default

## Best Next Steps For The Next Agent

### 1. Run broad validation for the two phone candidates

Run:

- `Gate`
- `Coverage`
- `Sentinel`

For:

- `gemma-4-e2b-it`
- `gemma-4-e4b-it@q4_k_s`

Suggested profile:

- `default`
- `w1`
- local embeddings
- start at `768`
- rerun only clipped prompts at `1024`

### 2. Build an external-review bundle

The operator wants to compare outputs across models and hand them plus the guides to an external reviewer.

Best immediate bundle:

- `e2b`
- `e4b q4_k_s`
- `e4b bf16`
- optionally `26b` anchor later

Use:

- compare pack outputs
- selected watchlist prompts
- a fixed human rubric for decision quality, compactness, constraint handling, safety, and citation cleanliness

### 3. If a single phone-target default had to be chosen today

Provisional read:

- `e4b q4_k_s` remains the safer “practical phone candidate”
- `e2b` is the more interesting efficiency candidate and may be good enough, but it needs broad validation before promotion
