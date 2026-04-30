# Android Functional UX Backlog - 2026-04-30

Status after pre-noon polish head `567f96d`.

## Fixed This Loop

- `e338a2d` routes the shared Search button through the active shared-input
  target. When Ask owns the field, pressing the visible submit/search affordance
  now submits as Ask instead of guide search.
- Coverage:
  `MainActivityPhoneNavigationTest.searchButtonSubmitUsesAskTargetWhenAskOwnsSharedInput`.

## Current Proof

- Full visual/state pack:
  `artifacts/ui_state_pack/final_pre_noon_full_visual_proof/20260430_113129`
- Result: `22 / 22`, `fail_count=0`, `platform_anr_count=0`,
  `matrix_homogeneous=true`.
- Full unit tests passed after the final app-code commit:
  `:app:testDebugUnitTest`.

## Top Queue - Phone Chrome Normalization

The old exact mock targets are no longer the source of truth. Current polish
should favor shared design language and working navigation over pixel-chasing
the exported mocks.

1. Phone home/search chrome
   - Base phone home/search still used a smaller anonymous chevron and a
     single title label, while detail/thread/guide use the newer topbar
     grammar. Normalize it to the shared back action, divider, mode, title,
     search, and overflow tokens.
   - Key code: `android-app/app/src/main/res/layout/activity_main.xml`,
     `MainActivity.updateHomeChromeTitle()`, and
     `MainActivity.handleHomeChromeBack()`.
   - Validation: XML parity for base phone chrome plus phone portrait state
     pack screenshots for home and search.

2. Phone detail/header action consistency
   - Keep answer/thread/guide/emergency topbar title sizing, underline, leading
     back affordance, and trailing action priority aligned. Prefer one shared
     action vocabulary: Back, Home, Share, Pin/Overflow.
   - Key code: `DetailActivity.updateTopBarActions()`,
     `buildRev03TopBarTitle()`, and `SenkuTopBarHostView`.
   - Validation: phone portrait state pack screenshots across answer,
     follow-up thread, guide, and emergency.

## Remaining Functional Slices

1. Review/demo harness isolation
   - Code-health risk: review/mock mode can shape default product behavior
     (home metadata, recent threads, category counts, search labels/results).
   - Safe slice: extract an explicit review/demo policy and require a debug
     test extra before any review-shaped data replaces real app state.
   - Key code: `MainActivity.resolveProductReviewMode()`,
     `buildReviewSearchResults()`, `reviewHomeCategoryCount()`, and
     `appendReviewSearchLatency()`.

2. Route/back state extraction
   - Code-health risk: back behavior is coupled to adapter contents and tab
     side effects. Empty search, Ask-unavailable, and Saved paths should be
     explicit routes rather than inferred from `items`.
   - Safe slice: introduce a small pure route model first, then let
     `MainActivity` delegate back handling to it.
   - Validation: zero-result search back, Ask unavailable back, Saved back,
     and detail return tests.

3. Follow-up composer submit behavior
   - Add interaction-level coverage that Send, IME action, and empty input all
     route through the expected follow-up path.
   - Key code: `DetailActivity.configureFollowUpInput()` and `runFollowUp()`.

4. Back affordance click behavior
   - Existing tests cover policy predicates and chrome shape; add interaction
     coverage for home chrome back and detail back in task-root and
     non-task-root cases.
   - Key code: `MainActivity.handleHomeChromeBack()` and
     `DetailActivity.navigateBackFromDetail()`.

5. Saved entry path
   - Add intent-level coverage that `EXTRA_OPEN_SAVED` lands on the Saved/Pins
     owner and shows saved-guide semantics.
   - Key code: `MainActivity.maybeHandleOpenSavedIntent()`.

6. Shared chrome action model
   - Code-health risk: Home/Ask/Saved/Back/Pin/Share/Overflow are encoded in
     several phone/tablet renderers. Consolidate action/destination mapping
     while keeping XML/Compose renderers separate.
   - Key code: `MainActivity`, `DetailActivity`, `BottomTabBar.kt`, and
     `TopBar.kt`.

7. Follow-up composer state owner
   - Code-health risk: the visible Compose composer mirrors a nearly hidden
     legacy `EditText`; draft, busy, and retry state are split across
     `DetailActivity` and `DetailAnswerPresenterHost`.
   - Safe slice: introduce a pure `FollowUpComposerState`/controller and let
     the legacy input mirror it temporarily.

8. Tablet rail end-to-end navigation
   - Policy tests cover rail order and callback dispatch. Add a small UI-level
     smoke that rail taps trigger the intended route transitions.

9. Search result card model extraction
   - Code-health risk: `SearchResultAdapter` still owns card heuristics and
     mapping as well as RecyclerView binding.
   - Safe slice: extract `SearchResultCardModelMapper`; keep adapter as
     binder/callback owner.

10. Physical device install/smoke
   - ADB only reported the emulator matrix during the pre-noon check:
     `emulator-5554`, `emulator-5556`, `emulator-5558`, and `emulator-5560`.
   - When the USB phone is visible in `adb devices -l`, install
     `android-app/app/build/outputs/apk/debug/app-debug.apk` and run a manual
     Ask/Search/Saved/back-stack smoke.
