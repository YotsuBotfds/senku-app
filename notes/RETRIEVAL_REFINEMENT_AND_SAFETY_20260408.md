# Retrieval Refinement And Safety — 2026-04-08

## Scope

Focused cleanup after the earlier craft / transport / governance follow-up notes.

Prompts targeted in this pass:

- `how do i make glass`
- `how do i weld without a welder`
- `how did ancient romans make concrete`
- `how do i signal a rescue plane`
- `how do i make soap from animal fat`
- `we ran out of soap how do i make more`
- `how do i preserve a body for burial in hot weather`

## Goals

- Tighten remaining noisy craft answers without reopening broad suites.
- Correct the soap/lye safety drift.
- Eliminate hot-weather burial advice that suggested using streams as body-cooling sites.
- Stop `Roman concrete` from burning reasoning budget in think-mode.
- Stop `signal a rescue plane` from blowing back up to the completion cap in Gate.

## Code Changes Kept

- Added broader soapmaking detection in [`query.py`](../query.py) so both `ran out of soap` and generic `soap from animal fat` prompts get the same safe answer contract and retrieval support.
- Added soapmaking retrieval hints for ash-lye / fat / curing / rinse-with-water safety.
- Tightened `glass` answer shape and metadata filtering.
- Tightened `weld without a welder` answer shape to a shorter choose / prep / join / cool structure.
- Added deterministic cited control paths for:
  - `roman_concrete_starter`
  - `hot_weather_burial_preservation`
  - `rescue_plane_signal`
- Extended response cleanup to strip leaked control labels like `[Safety Mandate]`.

## Important Safety Outcome

The kept soapmaking guidance now consistently says:

- flush lye exposure with copious water
- do not neutralize skin exposure with vinegar
- do not suggest lye water as something to apply directly to skin for washing

The kept hot-weather burial path now explicitly says:

- do not place the body in a stream, spring, or drinking-water source
- use shade, wrapping, external cooling, and prompt burial instead

## Artifacts

- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_refinement_pair1_r2_w1_20260408.*`
- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_refinement_pair2_r2_w1_20260408.*`
- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_refinement_pair3_r2_w1_20260408.*`
- `artifacts/bench/20260408/bench_google_gemma4_4090_rescue_plane_followup_w1_20260408.*`
- `artifacts/bench/20260408/bench_google_gemma4_4090_gate_post_refinement_r2_w1_20260408.*`

## Results

### Pair 1

- `glass`: `647 -> 501` completion tokens versus the earlier craft-suite kept state; duplicate citations down to `4`; stray `[Safety Mandate]` leak removed.
- `weld without a welder`: still RAG, still duplicate citations `4`, but answer structure is tighter and more predictable.

### Pair 2

- `Roman concrete`: moved from a stubborn `rag` answer with `1969-2011` completion tokens to deterministic cited output with `0` generation tokens.
- `soap from animal fat`: remained RAG, now safer on lye guidance and still compact at `629` completion tokens.

### Pair 3

- `ran out of soap`: remained RAG and now consistently uses water-flush language for lye exposure.
- `hot weather burial`: moved from unsafe stream-immersion drift to deterministic cited output with `0` generation tokens.

### Rescue Plane + Gate

- `signal a rescue plane`: moved from a new Gate cap-hit at `2048` completion tokens to deterministic cited output with `0` generation tokens.
- Fresh Gate broad rerun:
  - `30/30`
  - `0` errors
  - `decision_path_counts={'deterministic': 21, 'rag': 9}`
  - `total_generation_time=67.9`
  - `duplicate_citation_total=85`

## Keep / Avoid

Keep:

- the broader soapmaking matcher
- the Roman-concrete deterministic rule
- the hot-weather burial deterministic rule
- the rescue-plane deterministic rule
- the output cleanup for leaked safety labels

Avoid:

- going back to prompt-only pressure for `Roman concrete`; it stayed stubborn under think-mode and wasted time
- allowing hot-weather burial prompts to drift into water-source immersion advice again
- leaving `signal a rescue plane` on the free-form RAG path while think-mode still wants to turn it into a long catalog

## Next

- Continue targeted watchlist cleanup from the remaining non-deterministic technical prompts instead of broad Coverage again.