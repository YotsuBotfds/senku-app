# Retrieval Follow-Up: Craft / Technical Watchlist (2026-04-08)

This note records the targeted retrieval/prompt-policy pass after the aligned broad post-citation-cleanup artifacts.

## Goal

Reduce duplicate citations and weak retrieval drift on the remaining craft / technical watchlist prompts without expanding the deterministic layer.

Primary prompts tested:

- `how do i make glass`
- `how do i make paper and ink`
- `how do i make a bow and arrows`
- `how do i make dye from plants`
- `how do i catch fish without fishing gear`
- `how do i weld without a welder`
- `how did ancient romans make concrete`
- `how do i smoke meat to preserve it`

Control / comparison prompts used during the pass:

- `how do i build a clay oven`
- `how do i build a loom and weave cloth`

## Code Changes Kept

- Added targeted supplemental retrieval lanes and metadata bonuses for:
  - glassmaking from scratch
  - bow and arrow construction
  - no-gear fishing
  - natural dyes
  - welding without a welder
  - Roman concrete
  - smoking/preserving meat
- Added prompt-shaping notes for:
  - glassmaking
  - paper and ink
  - no-gear fishing
  - Roman concrete
- Added no-gear fishing distractor penalties for marine/open-water and aquaculture-maintenance chunks.

## Changes Explicitly Removed

- Extra retrieval shaping for `paper and ink` was removed after it over-concentrated on `Ink & Pigment Chemistry` and raised duplicate citations.
- Extra retrieval shaping for `clay oven` was removed after it produced no net quality gain.

## Best Current Validation Artifacts

- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_crafts_pair1_r2_w1_20260408.*`
  - `glass`: duplicate citations `7 -> 4`, completion tokens `585 -> 533`
  - `paper and ink`: duplicate citations `7 -> 4`, completion tokens `843 -> 650`
- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_crafts_pair2_w1_20260408.*`
  - `bow and arrows`: duplicate citations `7 -> 5`
  - `dye from plants`: duplicate citations `7 -> 4`, completion tokens `639 -> 565`
- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_crafts_pair3_r3_w1_20260408.*`
  - `catch fish without gear`: duplicate citations `7 -> 4`, completion tokens `696 -> 389`
  - `weld without a welder`: completion tokens `938 -> 703` with citation noise roughly flat
- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_crafts_pair4_r2_w1_20260408.*`
  - `Roman concrete`: duplicate citations `5 -> 4`, answer shape cleaner; completion tokens still elevated under think-mode
- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_crafts_pair5_w1_20260408.*`
  - `smoke meat`: duplicate citations `5 -> 1`, completion tokens `708 -> 674`
- `artifacts/bench/20260408/bench_google_gemma4_4090_retrieval_followup_crafts_suite_r2_w1_20260408.*`
  - 10/10 success, 0 errors
  - aggregate duplicate citations `58 -> 42` across the tracked suite
  - aggregate completion tokens `7425 -> 7088`
  - `clay oven`: duplicate citations `5 -> 4`, completion tokens `730 -> 284`
  - `loom`: duplicate citations `5 -> 4`, completion tokens `833 -> 564`

## Current Read

- This pass was worth keeping.
- The strongest wins were `glass`, `paper and ink` (after rollback to prompt-shaping instead of extra retrieval), `bow and arrows`, `dye from plants`, `no-gear fishing`, and `smoke meat`.
- `weld without a welder` improved in retrieval focus and response length, but citation duplication stayed roughly flat.
- `Roman concrete` is retrieval-cleaner, but think-mode token usage is still relatively high.
- `clay oven` and `loom` ended up responding better to answer-shaping than retrieval changes; keep their current prompt-policy notes and avoid reintroducing the removed clay-specific retrieval add-ons.
- The `r2` suite artifact is the cleanest single reference for this lane; use the pair artifacts only when you need prompt-level detail or to understand the intermediate failed attempts.

## Recommended Next Step

Use this note together with `notes/POST_CITATION_CLEANUP_WATCHLIST_20260408.md` for the next watchlist pass, then rerun the affected broad sections only after enough targeted wins accumulate to justify a new baseline.