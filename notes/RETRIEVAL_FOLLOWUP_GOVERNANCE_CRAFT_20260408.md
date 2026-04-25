# Retrieval Follow-Up: Governance / Soap / Leather (2026-04-08)

This note records the targeted follow-up for:

- `how do i run a fair trial with no lawyers or judges`
- `we ran out of soap how do i make more`
- `can i tan leather with brains`

## Goal

Tighten retrieval and answer shape on three non-deterministic prompts without adding new deterministic rules.

## Code Changes Kept

- Added a `fair trial` retrieval/rerank lane that boosts community justice / trial-procedure material and penalizes drift into property law, federation, and broad governance theory.
- Added compact answer contracts for:
  - fair trial
  - soap restock / soap making
  - brain tanning
- Added light soap de-noising to avoid candle drift.
- Added light brain-tanning rerank bias toward direct brain-tanning sections and away from bark tanning / butchering spillover.

## Best Validation Artifact

- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_fair_soap_brain_pair1_w1_20260408.*`

Prompt-level results versus the broad post-citation-cleanup baseline:

- `fair trial with no lawyers or judges`
  - duplicate citations `4 -> 2`
  - completion tokens `727 -> 426`
  - retrieval categories tightened to `society=24`
- `we ran out of soap how do i make more`
  - duplicate citations `1 -> 0`
  - completion tokens `779 -> 497`
- `can i tan leather with brains`
  - duplicate citations `5 -> 4`
  - completion tokens `563 -> 448`

## Current Read

- This pass is worth keeping as-is.
- `fair trial` was the real retrieval problem in the trio and is materially better now.
- `soap` and `brain tanning` mainly benefited from tighter answer contracts rather than heavy retrieval changes.

## Suggested Next Candidates

- `how do i preserve a body for burial in hot weather`
- `how do i make a bow and arrows`
- after that: revisit the updated residual watchlist rather than expanding this lane further