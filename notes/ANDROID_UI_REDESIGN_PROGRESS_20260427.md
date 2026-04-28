# Android UI Redesign Progress - 2026-04-27

Morning implementation review note for the Android field manual redesign wave.

## Commit Status

- Latest pushed commit: `de0c071c4822d10bb7f68fd89c864b0a71f2b21c`
  (`de0c071`, `HEAD -> master`, `origin/master`).
- Commit title: `advance android field manual redesign`.
- Scope: Android UI-only redesign work across home/search/detail/tablet
  surfaces, bottom tab/composer primitives, emergency surface policy, and
  focused presentation/unit coverage.

## What Changed In `de0c071`

- Refined phone and tablet home/search/detail layouts toward the 2026-04-27
  target mocks documented in `notes/ANDROID_TARGET_MOCKS_20260427.md`.
- Added/updated manual-style drawables for home rows/search/status, search
  result rows, detail shells, source cards, topbar chips/groups, follow-up dock,
  and emergency banner treatment.
- Advanced tablet detail/search composition through `EvidencePane`,
  `TabletDetailScreen`, and `ThreadRail` updates.
- Tightened search result presentation through `SearchResultCard` and adapter
  heuristics.
- Added tests for emergency surfacing, main presentation formatting, search
  result adapter behavior, and search card heuristics.

## Validation Artifacts

Latest passing smoke captures are under these artifact roots:

- Home:
  - `artifacts/redesign_wave_home_phone_portrait_navfix`
  - `artifacts/redesign_wave_home_phone_landscape_navfix`
  - `artifacts/redesign_wave_home_tablet_landscape`
- Search:
  - `artifacts/redesign_wave_search_phone_portrait_navfix`
  - `artifacts/redesign_wave_search_phone_landscape`
  - `artifacts/redesign_wave_search_tablet_landscape`
- Answer/detail:
  - `artifacts/redesign_wave_answer_phone_portrait`
  - `artifacts/redesign_wave_answer_phone_landscape`
  - `artifacts/redesign_wave_answer_tablet_landscape`

Smoke tests with passing latest summaries:

- `PromptHarnessSmokeTest#homeEntryShowsPrimaryBrowseAndAskLanes`
- `PromptHarnessSmokeTest#searchQueryShowsResultsWithoutShellPolling`
- `PromptHarnessSmokeTest#deterministicAskNavigatesToDetailScreen`

Notes:

- `redesign_wave_search_phone_portrait` and
  `redesign_wave_search_tablet_landscape` include earlier failed summaries, but
  both have later passing retry/navfix summaries.
- Captures include phone portrait, phone landscape, and tablet landscape for
  the covered home/search/detail slices. Tablet portrait, guide reader,
  thread, and emergency capture coverage still need follow-up.

## Known UI Gaps From Visual Inspection

- `PaperAnswerCard` still reads too parchment-heavy compared with the flatter,
  cleaner target language.
- Tablet search still lacks the intended filter/preview rail treatment.
- Guide reader and cross-reference polish remain incomplete.
- Thread and emergency captures are still pending, so those surfaces should not
  be called visually closed.

## Recommended Next Slices

- Flatten and rebalance `PaperAnswerCard` so answer/detail reads closer to the
  field-manual target without losing hierarchy.
- Build the tablet search filter/preview rail, then recapture tablet landscape
  and tablet portrait search states.
- Polish guide reader and cross-reference presentation against the target mocks.
- Add pending thread and emergency smoke captures, including emergency portrait
  targets only unless a separate landscape design target is added.
- Run the focused Android unit/presentation tests plus the relevant
  `PromptHarnessSmokeTest` smoke set after each visual slice.

Changed path: `notes/ANDROID_UI_REDESIGN_PROGRESS_20260427.md`.
