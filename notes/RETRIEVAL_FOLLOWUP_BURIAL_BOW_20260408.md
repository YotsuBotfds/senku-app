# Retrieval Follow-Up: Burial / Bow (2026-04-08)

This note records the targeted follow-up for:

- `how do i preserve a body for burial in hot weather`
- `how do i make a bow and arrows`

## Goal

Reduce citation noise and answer sprawl on two still-noisy prompts that already had partial retrieval support.

## Code Changes Kept

- Added hot-weather burial retrieval bias toward mortuary / post-death-care material and away from cold-weather, avalanche, and pandemic drift.
- Added compact answer contracts for:
  - hot-weather body preservation before burial
  - starter self-bow plus starter arrows

## Best Validation Artifact

- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_burial_bow_pair1_w1_20260408.*`

Prompt-level results versus the broad post-citation-cleanup baseline:

- `preserve a body for burial in hot weather`
  - duplicate citations `6 -> 4`
  - completion tokens `508 -> 396`
- `make a bow and arrows`
  - duplicate citations `7 -> 3`
  - completion tokens `890 -> 559`

## Current Read

- This pass is a clear keep.
- `burial in hot weather` now stays on the relevant mortuary / burial-care sections.
- `bow and arrows` improved primarily because the answer contract stopped the model from surveying too many variants.

## Suggested Next Candidates

- Revisit the residual high-noise watchlist after folding these wins into the next summary.
- Likely remaining candidates include `glass`, `weld without a welder`, and `Roman concrete`.
