# Backend Cleanup Phase Tracker

Last updated after local validation and cleanup through `f7887fc6` on 2026-05-01.

Purpose: prevent future agents from rerunning Ask/query backend cleanup that is already complete. Keep this note short; implementation detail belongs in commits and tests.

## Completed

- Local-only workflow guard is in place so backend validation stays on the
  intended local path.
- Ask/Search route helpers are consolidated, with saved-route proof and
  follow-up failure restore proof covering the key route handoffs.
- Anchor-prior behavior is extracted behind policy, and route-output parity is
  characterized for the affected paths.
- Review fixture policy is guarded so demo/review-only data does not bleed into
  production query paths.
- No-source Ask handling now fails closed instead of fabricating unsupported
  output, including the explicit no-source route and generated-answer
  safeguard tests.
- Install completion preservation is guarded by policy/tests so completion is
  not cleared by later route refreshes.
- Topic-tag parsing is delimiter-robust for saved/current-head route output.
- Physical smoke evidence passed with refreshed saved-route checks, and the
  instrumented UI smoke contract now includes install_contract evidence.
- Answer detail field UX was refined through `b96f325`: material chips now use
  explicit indexed material labels/copy affordance text, the composer add action
  keeps a 48dp touch target around the 32dp visual circle, and redundant
  context hints are suppressed when phone top chrome already carries context.
- Related-guide ordering now prefers immediate workflow relevance for survival
  and fire-starting anchors before truncating the candidate pool, with focused
  repository coverage for relevance ordering and candidate limits.
- Physical material-chip smoke coverage passed for the indexed material label
  and long-press copy affordance.
- Answer-mode source selection now reopens the compact phone cross-reference
  lane even when the explicit tap selects the same source already used as the
  implicit anchor; the physical phone source-graph smoke passed with direct,
  preview, and cleared-state artifacts.
- The source-entry preview was tightened and given composer clearance so the
  Open full guide CTA remains reachable above the docked composer.
- Current-head route refinements now bias glass-from-scratch, cabin
  weatherproofing, and broad house-building prompts away from generic or
  adjacent drift candidates, with focused route-output policy coverage.
- Saved-tab semantics are current-guide scoped: the tab stores whole guides by
  normalized `guide_id`, orders newly saved or re-saved guides first, caps the
  saved list at 12, and loads each saved entry through `loadGuideById`. It does
  not save sections, answer threads, or per-turn thread state. Existing proof:
  `android-app/app/src/test/java/com/senku/mobile/PinnedGuideStoreTest.java`
  covers normalization, dedupe, newest-first movement, and cap behavior;
  `MainActivity.refreshPinnedGuidesAsync()` shows the saved tab resolving the
  stored IDs as full guide records.
- Main route surface ownership now flows through `MainRouteDecisionHelper.RouteState`;
  `MainActivity.applyMainRouteState()` derives browse/result visibility from
  the route surface and renders chrome separately, so `browseChromeActive` is no
  longer a parallel route decision source.
- Search orchestration has a first controller boundary:
  `MainSearchController` owns repository availability checks, session-command
  short-circuiting, deterministic search routing, async search, review-demo
  result shaping, and stale async suppression; `MainActivity` keeps UI rendering
  in host callbacks. Focused proof: `MainSearchControllerTest`.
- Phone tab history stack behavior is covered by `MainPhoneTabHistoryPolicy`,
  including null filtering, duplicate suppression, eight-entry cap, and pop to a
  destination different from the active tab.
- Route/helper guardrails now cover zero-result search, no-source Ask failure,
  saved open/back, previous-tab back, install-completion preservation,
  review-fixture boundary scans, less brittle guide/detail smoke assertions, and
  stricter physical smoke evidence contracts.
- Route-back effects, follow-up answer outcomes, and saved/shared input smoke
  route assertions are covered so route restoration and input handoffs stay
  observable without broad UI rewrites.
- Source provenance/open coordinators and query/search mapper helpers now carry
  the coordinator/helper boundaries for route-facing backend logic.
- Chrome policy and review-boundary cleanup are characterized by focused tests
  so route chrome decisions and review-only data gates remain explicit.
- Route-focused executor extraction keeps route execution out of the activity
  while preserving focused route proof.
- Detail source opens now flow through a coordinator boundary, with saved-open
  policy routing kept explicit.
- Review search fixture gating keeps review-only search data behind policy.
- Recent-thread rendering and thread surface decisions now sit behind focused
  renderer/policy boundaries.
- Tablet evidence rail behavior is captured as policy instead of activity-local
  branching.
- Retry presentation is routed through the composer controller.
- Stale async search suppression has focused proof for dropped outdated results.
- Search score marker rendering now sits behind extracted policy.
- Restored route back transitions have explicit route-coverage proof.
- Reviewed-card metadata behavior is centralized behind policy.
- Tablet Ask rail submit routing has focused proof.
- Physical smoke proof is current for the completed backend cleanup stack.
- Result publication now has a presentation boundary:
  `MainResultPublicationPolicy` separates result-list/highlight/route
  publication from search-query chrome publication through
  `ResultItemsPresentation` and `SearchQueryChromePresentation`, with focused
  proof in `MainResultPublicationPolicyTest`.
- Follow-up generation and prompt-harness seams are extracted:
  `FollowUpGenerationCoordinator` owns generation-start and stall-notice
  decisions, `PromptHarnessReflection` owns androidTest reflection helpers, and
  review-demo abrasives cross-reference shaping is policy-gated.
- Brittle presentation assertions now prefer semantic contracts for suggestion
  descriptions, prompt clipping, linked-guide cue descriptions, session
  context, and answer-card notice copy instead of incidental exact strings.
- Pack answer-context section loading now lives in
  `PackAnswerContextSectionLoader`; `PackRepository` keeps orchestration and
  delegates guide-section loading. Proof includes focused Pack repository
  tests plus `PackRepositoryAnswerContextIntegrationTest`, which exercises
  `buildGuideAnswerContext()` against a tiny on-device SQLite pack.
- Presentation helper boundaries are current through `74ab01c9`:
  prompt-harness artifact writing, detail emergency action text, detail source
  stack policy, linked-guide cue policy, and tablet evidence visibility now sit
  behind focused helpers instead of presenter/test bodies.
- Route and retrieval helper boundaries are current through `71a99de0`:
  `MainCategoryFilterController` owns main category filter behavior,
  `PackWaterDistributionAnchorPolicy` owns water-distribution anchor routing,
  `PackRouteFocusedResultRanker` owns focused route ranking, and
  `PackRouteRefinementPolicy` was split so refinement helpers stay isolated
  from executor/repository orchestration.
- Prompt expectation validation is current through `51ae1f69`: empty
  structured primary expected-guide fields are accepted by the validator
  instead of being treated as malformed prompt metadata.
- Automation intent route application now has a controller boundary:
  `MainAutomationRouteController` applies `MainAutomationIntentPolicy`
  decisions through host callbacks, while `MainActivity` keeps lifecycle and
  rendering ownership. Focused proof covers apply, empty Ask, wait-for-repo,
  Ask run, and Search run paths.
- External-review route-family JVM proof now covers additional broad-house,
  cabin weatherproofing/window, and food-theft/governance variants without
  relying on a slow current-head device search.
- Prompt expectation validation also accepts blank-string retrieval primary
  expected-guide fields so JSON and Markdown blank-primary metadata behave
  consistently.
- Latest reported proof: direct pack guard passed; the FTS fallback matrix
  passed on `emulator-5554`; the non-Android regression gate passes with
  `-AllowRetrievalWarnings` and still reports the 3 known retrieval warnings;
  a fresh installed `phone-functional` PromptHarness smoke passed on
  `emulator-5554` with 3/3 tests.
- Compose presentation policy helpers are current through `69ee33eb`:
  answer-body parsing, search-result card policy, tablet evidence rows, guide
  paper text policy, and thread-rail policy now live outside their larger
  render files while preserving focused UI policy proof.
- Route row filtering is current through `1d94318c`:
  route-focused candidate gates for specialized topic rows, broad water/house
  rows, and direct anchor signal requirements now live in
  `PackRouteRowFilterPolicy`, with repository wrappers kept for compatibility
  and a current-head route matrix characterizing high-signal backend queries.
- Saved-guide route ownership is current through `a32b3a5a`:
  saved/pinned refresh planning, load caps, section visibility, browse-load
  decisions, and pending section focus now live in `MainSavedGuidesController`
  while `MainActivity` keeps Android view mutation.
- Chrome and rail metrics are current through `49e7fd2a`:
  Compose topbar measurements and host action shaping are named behind
  topbar policy helpers, and Compose nav-rail dimensions now live in
  `NavRailMetrics` with legacy aliases preserved for existing callers.
- Latest local proof after `49e7fd2a`: `git diff --check` passed; full Android
  `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed; the preceding fresh installed `phone-functional` PromptHarness smoke
  after `69ee33eb` passed on `emulator-5554` with 3/3 tests.
- Answer-anchor guide scoring is current through `4d798f4d`:
  `PackAnswerAnchorScoringPolicy` owns anchor guide score aggregation while
  `PackRepository` keeps compatibility wrappers for existing callers/tests.
- Main search suggestion routing is current through `e7688fc2`:
  `MainSearchSuggestionController` turns suggestion taps into category-filter,
  search, or ignore routes while `MainActivity` keeps host-side UI actions.
- Visible XML typography auditing is current through `a8d0268d`:
  `TypographyTokenAuditTest` now guards visible Rev03 XML shell layouts against
  new raw monospace/framework text-appearance islands, with documented legacy
  and hidden-placeholder exceptions.
- Functional UX smoke orchestration is current through `f7887fc6`:
  `scripts/run_android_functional_ux_smoke_matrix.ps1` runs the local-only
  `phone-functional`, `phone-functional-saved`, and
  `phone-functional-back-provenance` presets and writes a matrix summary; the
  PromptHarness also covers restored Saved destination Back returning Home.
- Latest local proof after `f7887fc6`: focused backend/search/typography tests
  passed; `git diff --check` passed; full Android
  `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed. The new restored Saved/back androidTest also passed directly on
  `emulator-5554` before integration.
- Retrieval/session cleanup is current through `7ac8def7`:
  `SessionRetrievalContextPolicy` owns follow-up retrieval query planning,
  structured context terms, source-context term selection, and token matching;
  `SessionMemory` keeps turn storage, prompt rendering, and anchor-prior state.
- Query metadata section scoring is current through `5f1f6069`:
  `QueryMetadataSectionScoringPolicy` owns section-match scoring while
  `QueryMetadataProfile` keeps query classification and compatibility wrappers.
- Route search SQL planning is current through `eb7e5e74`:
  `PackRouteSearchSqlPolicy` builds route FTS/LIKE SQL plans, bind arguments,
  no-BM25 order labels, and effective candidate limits while the executor keeps
  cursor execution and collection.
- Functional UX smoke matrix hardening is current through `db6a69f7`:
  the matrix wrapper now writes separate run/capture summaries, always captures
  logcat for capture summaries, enriches preset status in the matrix summary,
  reuses installed APKs after a passing preset, and handles tablet Compose
  source selection in the source-chip correctness smoke.
- Latest local proof after `db6a69f7`: focused JVM extraction tests passed;
  `git diff --check` passed; full Android
  `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed; the full functional UX matrix passed on `emulator-5554` with all
  three presets green at
  `artifacts/android_functional_ux_smoke_matrix/current_head_0748/matrix_summary.json`.
- Search-card row snippet mapping is current through `2e79e050`:
  compact/regular row snippet viewport decisions now live in
  `SearchResultCardModelMapper.Options`, keeping `SearchResultAdapter`
  focused on binding the mapped model.
- Pack retrieval orchestration is current through `c000d67d`:
  route result limits and preferred/centroid fallback merging now live in
  `PackRetrievalOrchestrationPolicy`, with repository compatibility wrappers
  preserving current callers.
- Guide body section metadata is current through `92ec879d`:
  `GuideBodySanitizer` now emits parsed section and callout metadata so guide
  presentation formatting can count/style parsed structure instead of
  re-scanning rendered lines.
- Detail chrome/source presentation polish is current through `a64d0a2e`:
  guide and emergency headers use the normalized source-aware labels, and
  compact selected-source cards drop the heavy match percent.
- Landscape home/search chrome polish is current through `48a42348`:
  landscape phone Home/Search share the same Back/divider/mode/title/search
  header rhythm, and hidden Home chrome back is fully noninteractive and
  nonsemantic.
- Latest local proof after `48a42348`: focused integrated JVM tests passed;
  `git diff --check` passed; full Android
  `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed; fresh physical phone `phone-basic` PromptHarness smoke passed on
  `RFCX607ZM8L` with APK SHA
  `5510bc32948dc8e677ae7681307f617a1b097ce1b70506db5e9cc908168829f0` at
  `artifacts/instrumented_ui_smoke/20260501_080839_375/RFCX607ZM8L/summary.json`.

## Remaining Next Slices

- Continue shrinking `MainActivity` only where backend lifecycle or route
  orchestration still owns too much.
- Do not rerun the result-publication extraction unless a regression points
  there; future `MainActivity` cleanup should target still-mixed route side
  effects outside `MainResultPublicationPolicy`.
- Keep repository cleanup incremental around remaining pure policy/helper
  extraction with parity tests.
- Do not collapse the new presentation, category-filter, route-ranking,
  water-distribution anchor, or route-refinement helper boundaries back into
  activity/repository/executor bodies.
- Keep automation intent route handling in `MainAutomationRouteController`;
  future automation behavior should extend the controller/policy tests instead
  of adding new intent branches directly to `MainActivity`.
- Keep review/demo fixture gates explicit when adding mocks or preview labels so
  production query paths stay fixture-free.
- Prefer backend/code-health slices that remove mixed responsibilities from
  activities/repositories without changing chrome, copy, or layout behavior.
- Source-entry preview density and composer clearance are improved, but the
  compact phone selected-entry card is still visually heavy and should be
  revisited in a polish slice rather than mixed into route/backend work.
- Re-run focused backend tests after each backend slice; use UI state packs only
  when behavior could affect screenshots.

## Guardrails

- Do not touch app UI files for this tracking-note task.
- Do not rerun completed extraction phases unless a regression points to a specific follow-up.
- Prefer small backend-only commits with focused tests.
