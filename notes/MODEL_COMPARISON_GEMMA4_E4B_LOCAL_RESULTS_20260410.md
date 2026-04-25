# Gemma 4 E4B Local Results — 2026-04-10

## Scope

Local Senku testing on this machine using:

- generation model: `gemma-4-e4b-it`
- embeddings: local LM Studio endpoint
- generation workers used in this pass: `1`

## Artifacts Produced

- `artifacts/bench/20260410/bench_gemma4_e4b_bf16_smoke_demo_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e4b_bf16_compare_pack_default_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e4b_q4ks_smoke_demo_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e4b_q4ks_compare_pack_default_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e4b_q4ks_compare_pack_demo_w1_20260410_t768.*`
- `artifacts/bench/20260410/bench_gemma4_e4b_q4ks_compare_pack_demo_w3_20260410_t768.*`

## Results So Far

### Smoke demo (`768`)

- prompts: `3`
- successful: `3`
- errors: `0`
- total generation time: `32.4s`
- avg per prompt: `10.8s`
- duplicate citations: `13`

### Compare pack default (`768`)

- prompts: `24`
- successful: `24`
- errors: `0`
- total generation time: `237.8s`
- avg per prompt: `9.9s`
- duplicate citations: `51`

## Practical Read

This BF16 Gemma build is immediately usable on the local Senku RAG path at the smaller `768` completion cap.

Compared with the local Qwen 3.5 9B lane from the same date:

- Gemma is much faster on the smoke and compare-pack-default lanes
- Gemma does not require the raised `4096` completion cap that Qwen needed with thinking on
- citation duplication still exists and still needs side-by-side quality reading before any final model decision

## Q4_K_S Results

Model id used:

- `gemma-4-e4b-it@q4_k_s`

### Smoke demo (`768`, `w1`)

- prompts: `3`
- successful: `3`
- errors: `0`
- total generation time: `18.9s`
- avg per prompt: `6.3s`
- duplicate citations: `8`

### Compare pack default (`768`, `w1`)

- prompts: `24`
- successful: `24`
- errors: `0`
- total generation time: `158.7s`
- avg per prompt: `6.6s`
- duplicate citations: `61`

### Compare pack demo (`768`, `w1`)

- prompts: `24`
- successful: `24`
- errors: `0`
- total generation time: `136.5s`
- avg per prompt: `5.7s`
- duplicate citations: `63`

### Compare pack demo (`768`, `w3`)

- prompts: `24`
- successful: `24`
- errors: `0`
- total generation time: `237.2s`
- avg per prompt: `9.9s`
- duplicate citations: `52`

## Q4_K_S Concurrency Read

For `gemma-4-e4b-it@q4_k_s` on this machine, `3` same-host workers improved overall throughput but reduced per-prompt token speed.

From the matched `demo` comparison pack:

- `w1` aggregate generation speed: about `70.75 tok/s`
- `w3` aggregate generation speed: about `39.75 tok/s`
- `w1` wall-clock throughput: about `48.16 tok/s`
- `w3` wall-clock throughput: about `64.09 tok/s`
- wall-clock speedup from `w3`: about `26.6%`

Interpretation:

- if you care about single-prompt latency, `w1` is better
- if you care about batch throughput, `w3` is better

## BF16 vs Q4_K_S Read

At the same `w1` local profile:

- Q4_K_S was faster than BF16 on smoke and compare-pack runs
- Q4_K_S smoke duplication was slightly better than BF16
- Q4_K_S default compare-pack duplication was a bit worse than BF16 (`61` vs `51`)

Practical read:

- Q4_K_S looks strong enough to keep testing
- BF16 is not obviously necessary for this local Senku profile if the goal is lighter deployment
- quality still needs side-by-side reading before declaring Q4_K_S the better local Gemma lane

## Operator Runtime Note

Operator note for future local E4B testing on this 4090:

- `gemma-4-e4b-it` should be able to handle `3` same-host concurrent prompts for faster bench runs

Important:

- this was **not** benchmark-verified in this pass
- current artifacts above were captured with `1` worker only
- treat `3` workers as the next runtime profile to test, not as a measured conclusion yet

Update:

- `3` workers are now benchmark-verified for the Q4_K_S `demo` comparison pack on this machine
- the operator note remains relevant for E4B broadly, but the measured throughput numbers above are currently from the Q4_K_S lane

## Suggested Next Steps

1. Side-by-side read BF16 vs Q4_K_S on the fresh local compare-pack artifacts.
2. If phone deployment is the target and Q4_K_S output still looks clean in manual review, test `e2b` on the smoke lane next.
3. Only take `e2b` to broader lanes if it preserves the same decision quality and citation discipline.
