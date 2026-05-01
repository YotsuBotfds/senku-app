# Android Functional UX Backlog - 2026-04-30

Status after pushed validation/code-health head `51ae1f69`.

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
- `7ac07a3d` added the result-publication presentation boundary: result
  list/highlight/route publication and search-query chrome publication are now
  separate policy-owned presentations.
- `ea9284bc` extracted follow-up and harness helper seams: follow-up stall
  visibility is covered in `FollowUpGenerationCoordinator`, prompt-harness
  reflection moved to `PromptHarnessReflection`, and review-demo
  cross-reference shaping stayed behind policy.
- `cd0812b4` relaxed brittle presentation assertions so copy/layout tests guard
  semantic requirements without pinning incidental exact strings.
- `164d6ca2` moved Pack answer-context section loading into
  `PackAnswerContextSectionLoader`, with focused Pack tests and
  `PackRepositoryAnswerContextIntegrationTest` proving the repository
  `buildGuideAnswerContext()` path against a tiny SQLite fixture.
- `74ab01c9` extracted presentation helper boundaries for prompt-harness
  artifacts, detail emergency actions, detail source stacks, linked-guide cues,
  and tablet evidence visibility.
- `71a99de0` extracted route/retrieval helpers:
  `MainCategoryFilterController`, `PackWaterDistributionAnchorPolicy`,
  `PackRouteFocusedResultRanker`, and the split
  `PackRouteRefinementPolicy` helper responsibilities.
- `51ae1f69` fixed prompt expectation validation so empty structured primary
  expected-guide fields are valid when the prompt metadata intentionally leaves
  the primary guide blank.
- The current automation cleanup wave extracted `MainAutomationRouteController`
  so intent-driven Ask/Search handoffs apply through a tested host boundary
  instead of direct `MainActivity` branching, and added JVM route-family
  coverage for broad-house, cabin weatherproofing/window, and food-theft
  governance variants.
- Prompt expectation validation now also accepts blank-string retrieval primary
  fields, keeping JSON and Markdown blank-primary metadata consistent.

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
- Latest reported pushed validation after `51ae1f69`: direct pack guard
  passed; the FTS fallback matrix passed on `emulator-5554`; the non-Android
  regression gate passes with `-AllowRetrievalWarnings` and still reports the
  3 known retrieval warnings.
- Latest local smoke after the automation route extraction: fresh build/install
  `phone-functional` PromptHarness smoke on `emulator-5554` passed 3/3 tests
  with screenshots, dumps, and logcat captured under
  `artifacts/instrumented_ui_smoke/20260501_063203_907`.
- Latest physical proof after `1321e8f2`: full Android
  `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed, and physical `phone-functional` PromptHarness smoke passed on
  `RFCX607ZM8L` at
  `artifacts/physical_phone_functional_after_1321e8f2/run_summary.json`.
- Latest physical proof after `a8b7445c`: full Android
  `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed, and physical `phone-functional` PromptHarness smoke passed on
  `RFCX607ZM8L` at
  `artifacts/physical_phone_functional_after_a8b7445c/run_summary.json`.

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
   - Current scout finding: product shared-input search controls still reference
     `external_review_home_search_button` by resource name from normal
     `activity_main` layouts and `SharedInputChromePolicy`; rename to a neutral
     production resource or gate it explicitly through review policy.
   - Key code: `MainActivity.resolveProductReviewMode()`,
     `buildReviewSearchResults()`, `reviewHomeCategoryCount()`,
     `appendReviewSearchLatency()`, and `SharedInputChromePolicy`.

2. Route/back state extraction
   - Code-health risk: back behavior is coupled to adapter contents and tab
     side effects. Empty search, Ask-unavailable, and Saved paths should be
     explicit routes rather than inferred from `items`.
   - Status: partially addressed. `MainRouteCoordinator` now owns Main route
     state, browse/result mode, and phone tab history; route state/effect
     helpers and result-publication presentations remain live.
   - Next safe slice: extend the coordinator around remaining route side
     effects only when a concrete branch remains in `MainActivity`; do not
     reopen result publication unless a regression points there.
   - Validation: zero-result search back, Ask unavailable back, Saved back,
     and detail return tests.

3. Follow-up composer submit behavior
   - Status: addressed at proof level. AndroidTest coverage now proves Send,
     IME SEND, and empty input behavior on real `DetailActivity`; controller
     coverage now also preserves newer visible drafts across success/failure
     completion while retry remains tied to the submitted query.
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
     guide detail return paths have androidTest coverage. Saved refresh
     semantics now normalize/dedupe saved guide IDs and hide Saved child views
     outside the visible Saved destination.
   - Key code: `MainActivity.maybeHandleOpenSavedIntent()`.

6. Shared chrome action model
   - Code-health risk: Home/Ask/Saved/Back/Pin/Share/Overflow are encoded in
     several phone/tablet renderers. Consolidate action/destination mapping
     while keeping XML/Compose renderers separate.
   - Status: detail visible save action now uses Save/Saved resource names and
     copy. Internal implementation names such as `detail_pin_button` and
     `PinnedGuideStore` remain to avoid route/store churn.
   - Key code: `MainActivity`, `DetailActivity`, `BottomTabBar.kt`, and
     `TopBar.kt`.

7. Follow-up composer state owner
   - Code-health risk: the visible Compose composer mirrors a nearly hidden
     legacy `EditText`; draft, busy, and retry state are split across
     `DetailActivity` and `DetailAnswerPresenterHost`.
   - Status: partially addressed. Pure `FollowUpComposerState` and
     `FollowUpComposerController` exist; retry presentation now centralizes
     visibility, enabled state, and normalized retry query. Generation-start
     cleanup is coordinated and stall-notice visibility now has focused
     coordinator proof. Next slice is wiring more of `DetailActivity` composer
     state through these helpers.

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
     multiple pure display labels/descriptions; recent tests assert semantic
     display contracts instead of exact incidental copy. Presentation helper
     boundaries were extended in `74ab01c9`; continue trimming only small pure
     helpers from the adapter/presenter surfaces.

10. Physical device install/smoke
   - ADB only reported the emulator matrix during the pre-noon check:
     `emulator-5554`, `emulator-5556`, `emulator-5558`, and `emulator-5560`.
   - Status: addressed for the current evening head. Physical `phone-functional`
     preset passed on `RFCX607ZM8L`, covering Ask/Search, Saved back, and
     answer provenance open/back.
