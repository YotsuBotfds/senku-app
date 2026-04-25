# Retrieval Follow-Up: Transport / Metalworking Watchlist (2026-04-08)

This note records the narrow follow-up after the craft/technical suite, focused on the remaining technical prompts:

- `how do i build a raft to cross a lake`
- `how do i cast metal without a foundry`

## Goal

Reduce citation noise and answer sprawl on the transport / casting watchlist. This note started as a pure retrieval/prompt-shaping pass; later the casting half moved to a narrow deterministic cited closure after repeated follow-up testing.

## Code Changes Kept

- Added a narrow raft-lake retrieval bias toward buoyancy / water transport and away from rescue / road-building drift.
- Added a short starter-plan answer contract for raft crossing prompts.
- Added a short starter-plan answer contract for `cast metal without a foundry`.
- Added a small casting retrieval bias toward low-tech casting, crucibles, and non-ferrous metalworking.

## Best Validation Artifacts

- Initial retrieval/prompt-shaping pass:
  - `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_raft_cast_pair1_w1_20260408.*`
- Current kept state:
  - `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_raft_cast_pair1_r2_w1_20260408.*`

Prompt-level results versus the broad post-citation-cleanup baseline:

- `raft to cross a lake`
  - duplicate citations `7 -> 4`
  - completion tokens `468 -> 322`
  - retrieval categories tightened from `survival=12, transportation=8, building=2, power-generation=2` to `survival=11, transportation=11, building=2`
- `cast metal without a foundry`
  - duplicate citations stayed `6`
  - completion tokens `717 -> 505`
  - retrieval tightened to `metalworking=24`

Follow-up closure after the later refinement/safety pass:

- `cast metal without a foundry`
  - moved from `rag` to deterministic cited `cast_without_foundry_starter`
  - generation tokens `505 -> 0`
  - duplicate citations `6 -> 2`

## Current Read

- The raft change is a clear keep.
- The intermediate casting retrieval change was directionally right, but the final kept solution is the narrow deterministic starter rule, not more prompt pressure.

## Suggested Next Watchlist Candidates

The next likely prompt families after this pass are:

- `how do i run a fair trial with no lawyers or judges`
- `we ran out of soap how do i make more`
- `can i tan leather with brains`