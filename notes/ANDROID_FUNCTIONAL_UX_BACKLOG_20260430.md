# Android Functional UX Backlog - 2026-04-30

Status after evening functional/code-health head `3b2bae3`.

## Fixed This Loop

- `e338a2d` routes the shared Search button through the active shared-input
  target. When Ask owns the field, pressing the visible submit/search affordance
  now submits as Ask instead of guide search.
- Coverage:
  `MainActivityPhoneNavigationTest.searchButtonSubmitUsesAskTargetWhenAskOwnsSharedInput`.
- `070b3de` tightened answer detail field cues: material chips now read as
  `Material 01: ...`, related wet-fire guides prioritize practical workflow
  neighbors, compact answer composer context is suppressed when top chrome
  already carries it, and the composer `+` action has an explicit disabled
  48dp touch target policy.
- `3b2bae3` hardened functional seams: previous-tab back transitions now apply
  resolved route state through `MainRouteEffectController`, review/demo recent
  thread placeholders require review mode, follow-up empty submit has
  interaction proof, and another small search-result display helper moved into
  `SearchResultCardModelMapper`.

## Current Proof

- Full visual/state pack:
  `artifacts/ui_state_pack/final_pre_noon_full_visual_proof/20260430_113129`
- Result: `22 / 22`, `fail_count=0`, `platform_anr_count=0`,
  `matrix_homogeneous=true`.
- Full unit tests passed after the final app-code commit:
  `:app:testDebugUnitTest`.
- Current evening proof:
  `:app:testDebugUnitTest` focused route/review/search suites,
  `:app:assembleDebugAndroidTest`, and physical phone functional preset on
  `RFCX607ZM8L` all passed after `3b2bae3`.

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
   - Status: partially addressed. Review mode already requires debug automation
     authorization, fixture access is policy-gated, and recent-thread
     placeholders now require the resolved review mode.
   - Next safe slice: continue moving direct fixture-shaped display decisions
     behind explicit `ReviewDemoPolicy` guards without changing production UI.
   - Key code: `MainActivity.resolveProductReviewMode()`,
     `buildReviewSearchResults()`, `reviewHomeCategoryCount()`, and
     `appendReviewSearchLatency()`.

2. Route/back state extraction
   - Code-health risk: back behavior is coupled to adapter contents and tab
     side effects. Empty search, Ask-unavailable, and Saved paths should be
     explicit routes rather than inferred from `items`.
   - Status: partially addressed. Route state and effect helpers are live, and
     previous-tab transitions now apply helper-resolved route state directly.
   - Next safe slice: move another small `MainActivity` route side effect into
     `MainRouteEffectController` with tests.
   - Validation: zero-result search back, Ask unavailable back, Saved back,
     and detail return tests.

3. Follow-up composer submit behavior
   - Status: addressed at proof level. AndroidTest coverage now proves Send,
     IME SEND, and empty input behavior on real `DetailActivity`.
   - Key code: `DetailActivity.configureFollowUpInput()` and `runFollowUp()`.

4. Back affordance click behavior
   - Existing tests cover policy predicates and chrome shape; add interaction
     coverage for home chrome back and detail back in task-root and
     non-task-root cases.
   - Status: partially addressed. Detail overflow visibility now derives from
     concrete menu actions, and the shared detail action vocabulary explicitly
     distinguishes chrome-only Back/Overflow from menu-backed Home/Save.
   - Key code: `MainActivity.handleHomeChromeBack()` and
     `DetailActivity.navigateBackFromDetail()`.

5. Saved entry path
   - Status: covered. Saved intent, nav, back, non-empty guide tap-through, and
     guide detail return paths have androidTest coverage. Saved semantics are
     documented in `notes/backendphases.md`.
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
   - Status: partially addressed. Pure `FollowUpComposerState` and
     `FollowUpComposerController` exist; retry presentation now centralizes
     visibility, enabled state, and normalized retry query. Next slice is
     wiring more of `DetailActivity` to these helpers.

8. Tablet rail end-to-end navigation
   - Status: covered. Existing androidTest methods
     `tabletDetailRailLibraryTapReturnsManualHome`,
     `tabletDetailRailSavedTapOpensSavedDestination`, and
     `tabletDetailRailAskTapOpensEmptyAskLane` launch a tablet detail surface,
     tap the rail, and assert the route transition.

9. Search result card model extraction
   - Code-health risk: `SearchResultAdapter` still owns card heuristics and
     mapping as well as RecyclerView binding.
   - Status: in progress. `SearchResultCardModelMapper` is live and now owns
     multiple pure display labels/descriptions; continue trimming only small
     pure helpers from the adapter.

10. Physical device install/smoke
   - ADB only reported the emulator matrix during the pre-noon check:
     `emulator-5554`, `emulator-5556`, `emulator-5558`, and `emulator-5560`.
   - Status: addressed for the current evening head. Physical `phone-functional`
     preset passed on `RFCX607ZM8L`, covering Ask/Search, Saved back, and
     answer provenance open/back.
