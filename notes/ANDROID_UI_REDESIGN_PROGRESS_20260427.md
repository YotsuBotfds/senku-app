# Android UI Redesign Progress - 2026-04-27

Visual progress note for the Android field manual redesign wave.

## Current Pushed Baseline

- Latest pushed commit: `3b8baf6` (`HEAD -> master`, `origin/master`).
- Current implementation batch: `4ac1e48` (`advance android W5 mock parity batch`).
- Current review batch: `3b8baf6` (`record android wave5 visual review`).
- Wave5 state-pack artifact: `artifacts/ui_state_pack/20260427_223445`.
- State-pack status: `partial`, 38 pass, 7 fail, 45 total, 0 platform ANRs.
- APK/model matrix: homogeneous per Wave5 triage.

## What Changed In `4ac1e48`

- Advanced Android mock-parity work across detail, home/search, tablet evidence,
  tablet detail/thread, and search result presentation.
- Updated presentation/unit coverage for follow-up landscape composer, home
  chrome, search result adapter, tablet stress reading policy, and tablet
  evidence visibility policy.
- Refreshed the phase tracker after the W5 implementation batch.

## What Changed In `3b8baf6`

- Added Wave5 visual review notes:
  - `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE5_HOME_SEARCH.md`
  - `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE5_ANSWER_THREAD.md`
  - `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE5_GUIDE_EMERGENCY.md`
  - `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE5_STATE_PACK_TRIAGE.md`
- No production app code changed in the review-note commit.

## Wave5 Surface Status

| Surface | Status | Current evidence |
| --- | --- | --- |
| Home | Partial | Screenshots exist for phone/tablet portrait and landscape. Structure is present, but typography, density, header copy, category counts, and recent-thread content diverge from mocks. |
| Search | Partial, with one fail | Phone/tablet search screenshots mostly render but use `fire`/75-result content instead of mock `rain shelter`/4-result content. Phone landscape search is a visual fail because the captured content area is effectively blank despite a harness pass. Tablet linked-guide handoff fails before trusted screenshots. |
| Answer detail | Partial, with phone portrait coverage gap | Generative answer screenshots exist for all roles, but phone portrait deterministic detail fails before trusted summary and anchor chip visibility is missing. Phone landscape answer is a visual fail against the split-pane target. |
| Follow-up thread | Partial, with phone landscape fail | Thread state exists in all four roles, but cards, typography, and spacing remain much larger than target; phone landscape misses the compact split layout. |
| Guide reader / cross-reference | Partial | Tablet guide-adjacent screenshots exist and phone guide-adjacent screenshots exist, but direct phone guide detail states fail before trusted screenshots. Reader chrome, paper body, and cross-reference rail still differ sharply from mocks. |
| Emergency portrait | Fail / not captured | No emergency-specific phone portrait or tablet portrait screenshot was found in the Wave5 state pack. Emergency landscape remains out of scope unless a target mock is added. |
| Tablet linked-guide handoff | Fail / harness readiness | Both tablet postures fail `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` before trusted screenshots with readiness stuck at idle. Phone postures pass this state. |

## Wave5 Harness Failures

All seven failures are instrumentation assertions before trusted screenshot
capture, not setup failures, app crashes, screenshot diffs, rotation mismatch,
or platform ANRs.

- Phone portrait and phone landscape:
  `guideDetailShowsRelatedGuideNavigation` fails because the preview title does
  not identify the selected linked guide.
- Phone portrait and phone landscape:
  `guideDetailUsesCrossReferenceCopyOffRail` fails because the non-rail guide
  row does not describe cross-reference behavior.
- Phone portrait:
  `deterministicAskNavigatesToDetailScreen` fails because the anchor chip is not
  visible.
- Tablet portrait and tablet landscape:
  `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` never reaches linked
  guide handoff readiness even though harness signals are idle.

## W6 Active Slice Ownership

Tracker: `notes/ANDROID_UI_REDESIGN_WAVE6_PLAN_20260427.md`.

- W6-A shell/search/home: compact `rain shelter` search parity, phone landscape
  blank-search blocker, tablet search density/filter controls, home density.
- W6-B answer/thread detail: deterministic phone portrait anchor chip, phone
  landscape answer/thread split layout, answer/thread density.
- W6-C guide/cross-reference: phone related-guide preview title, phone off-rail
  cross-reference row copy, guide reader chrome/paper/rail parity.
- W6-D emergency portrait: produce distinct emergency phone/tablet portrait
  captures and compare against the two emergency mocks.
- W6-E tablet linked-guide handoff readiness: diagnose tablet-only readiness or
  navigation state before changing visual acceptance criteria.
- W6-F visual-progress/tracker: notes-only lane for this file and optional W6
  plan note. No production code.

## Top Unresolved UI Risks

- Phone landscape search is visually blank while the harness reports pass.
- Emergency portrait surfaces have no fresh target captures and cannot be
  accepted.
- Tablet linked-guide handoff fails before screenshot capture in both tablet
  postures.
- Phone direct guide detail states fail before screenshot capture, blocking
  confident guide-reader parity.
- Typography and spacing remain too large across home, search, answer, thread,
  and guide surfaces, pushing expected content below the fold.
- Several screenshots use harness/test content or mismatched query content, so
  visual comparison to the mocks is noisy until state inputs are aligned.

Changed path: `notes/ANDROID_UI_REDESIGN_PROGRESS_20260427.md`.

## Wave7 Closure - 2026-04-27 23:16 Local

- Latest local state pack: `artifacts/ui_state_pack/20260427_233004`.
- State-pack status: `pass`, 47 pass, 0 fail, 47 total, 0 platform ANRs.
- Matrix health: homogeneous APK/model, no rotation mismatches.
- APK SHA: `c5e8b4cc57b59bfef2b7cc481d6ba461681135d0e556b64342a18f685821697a`.
- Model: `gemma-4-e2b-it-litert`.
- Model SHA: `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.

Wave7 cleared the six Wave6/Wave7 pre-capture blockers:

- Tablet portrait and landscape linked-guide handoff now pass with trusted
  screenshots and dumps.
- Phone portrait and landscape related-guide navigation now keep the selected
  linked-guide preview title and pass with trusted screenshots and dumps.
- Tablet portrait emergency now captures trusted emergency screenshots/dumps
  after visible source-context proof from the UIAutomator/accessibility tree.
- Phone landscape generative provenance no longer fails the visible provenance
  wording assertion in the full pack.

Validation run before the full pack:

- `scripts/run_android_local_quality_gate.ps1` passed.
- `:app:assembleDebug :app:assembleDebugAndroidTest` passed.
- Focused smokes passed for tablet handoff, phone guide navigation, and tablet
  emergency portrait.

Remaining work is now visual parity rather than harness readiness: home/search
content and density, answer/thread density, cream guide reader styling, and
emergency content hierarchy against the target mocks.

## Wave8 P2/P3 Proof - 2026-04-27 23:38 Local

- Latest local state pack: `artifacts/ui_state_pack/20260427_233208`.
- State-pack status: `pass`, 47 pass, 0 fail, 47 total, 0 platform ANRs.
- Search capture now uses the `rain shelter` review query instead of `fire`.
- Home manual-shell category counts now match the target mock contract:
  Shelter 84, Water 67, Fire 52, Food 91, Medicine 73, Tools 119.
- Tablet search score markers now use compact check labels.
- Phone-landscape host answer now emits and asserts
  `rain_shelter_gd345_split_answer`, proving a real GD-345 split answer
  surface instead of a source/guide substitute.
- Wave8 review note added:
  `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE8_STATE_PACK_231419.md`.

Validation before this pack:

- `scripts/run_android_local_quality_gate.ps1` passed.
- Focused JVM tests passed for home/search and P3 split-answer proof:
  `SearchResultAdapterTest`, `MainActivityHomeChromeTest`,
  `DetailFollowupLandscapeComposerTest`, and `DetailSurfaceContractTest`.

Remaining visual-parity priorities from the Wave8 reviewer: guide document
surface, thread density, emergency burn-hazard scenario/content, global chrome
density, and search card heaviness.
