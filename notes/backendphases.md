# Backend Cleanup Phase Tracker

Last updated after pushed cleanup through `27a8ffdf` on 2026-05-01.

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
  `phone-functional`, `phone-functional-follow-up`, `phone-functional-saved`,
  and `phone-functional-back-provenance` presets and writes a matrix summary;
  the PromptHarness also covers restored Saved destination Back returning Home.
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
- Saved guide semantics are current through `d7287a4a`: saved refresh planning
  now normalizes/dedupes guide IDs, drops blank entries, caps loads at 12, and
  only exposes empty/list children when the Saved section itself is visible.
- Follow-up composer draft safety is current through `686c3f68`: successful
  follow-up completion clears only the completed draft, preserves newer visible
  phone/tablet drafts, and failure handling preserves the visible draft while
  retry remains tied to the submitted query.
- Related-guide relevance cleanup is current through `1321e8f2`:
  `PackRelatedGuidePolicy` owns related-guide candidate limits and
  workflow-proximity ordering, leaving `PackRepository` as a thin delegator for
  that policy.
- Latest local proof after `1321e8f2`: focused Saved/follow-up/related-guide
  JVM tests passed; `git diff --check` passed; full Android
  `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed; fresh physical phone `phone-functional` PromptHarness smoke passed on
  `RFCX607ZM8L` with APK SHA
  `ea86e50c0ff966c447dfb43d34655e11247ef96b50f5837ac77005c1f16dedb1` at
  `artifacts/physical_phone_functional_after_1321e8f2/run_summary.json`.
- Main route ownership is current through `fc21d50e`: `MainRouteCoordinator`
  owns route state, browse/result mode, and phone tab history while
  `MainActivity` keeps view rendering and lifecycle work.
- Detail save language is current through `5bf82801` and `67351deb`: detail
  chrome now uses Save/Saved resource names and copy consistently, and the
  PromptHarness follows the renamed resources.
- Pack search directive parsing is current through `28d88aef`:
  `PackSearchQueryParser` owns anchor-prior directive parsing while
  `PackRepository.search()` delegates to the parser.
- PromptHarness route assertions are current through `a8b7445c`: androidTest
  route checks read `currentMainRouteState()` instead of stale MainActivity
  route fields removed by the coordinator extraction.
- Review/demo search CTA cleanup is current through `fbbcc38b`: product
  shared-input search controls use neutral `home_search_button` resources
  instead of `external_review_*` names, and `ProductSearchCtaResourceLeakageTest`
  guards normal `activity_main` variants.
- Tablet detail source-state extraction is current through `8df1e45b`:
  `DetailTabletStateBuilder` owns pure source-row assembly for tablet state.
- Recent-thread placeholder gating is current through `b9f3f4ba`: synthetic
  review placeholders route through `ReviewDemoPolicy`.
- Query routing test isolation is current through `5373e6b4`: vector-focused
  tests mock lexical fallback and no longer open sqlite during those paths.
- Physical smoke summary ergonomics are current through `2326e46a`: the
  validator success output now prints proof breadcrumbs such as summary
  Markdown, serial, focused package, screenshot, dump, and interaction status.
- Tablet search header review shaping is current through `399eca6b`:
  `ReviewDemoPolicy` owns the review-latency decision instead of direct
  `MainActivity` branching.
- Tablet visual-owner source selection is current through `c4bcbe19`:
  `DetailTabletStateBuilder` owns the source/question overlap scoring seam.
- Pack text matching is current through `18ea1b6e`: `PackTextMatchPolicy`
  owns marker/term normalization used by `PackRepository`.
- Pack text-match edge coverage and a small tablet topic-haystack helper are
  current through `5882301f` and `3d8d12a1`.
- Manual-home recent thread label shaping is current through `c53dc9dc`:
  `ReviewDemoPolicy` owns the live-label fallback vs review-label decision.
- Physical smoke post-check hardening is current through `88ff4815`: standalone
  physical interactions no longer accept generic unchanged Search/Ask/query
  evidence for submit/back, and validator proof includes dump-change metadata.
- Ask-owned physical smoke mode is done: real hardware smoke coverage now proves
  Ask tab ownership and visible-submit behavior separately from the hardened
  simple Saved/search/back path.
- Latest local proof after `88ff4815`: `git diff --check` passed; full Android
  `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed; physical-smoke contract and summary validator tests ran 40 tests OK.
- Backend cleanup is current through `8b0fd85d`: recent threads now sort by
  last activity, strict follow-up proof forwards through wrapper seams,
  route-effect tests live with the controller, route-search SQL handles
  hardening/no-op/SQLiteException paths, and Ask result publication helper
  names are cleaned up.
- Backend cleanup is current through `9add4e86`: Ask result presentation
  decisions now sit behind a controller boundary, desktop water-distribution
  route hints are explicit, and pack retrieval fusion plus detail tablet source
  ownership now live behind focused policies.
- Latest local proof after `9add4e86`: full Android
  `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
  passed.
- Post-`9add4e86` harness cleanup is current through `627d3991`: phone
  functional UX smoke presets now guard against tablet-class devices before
  install/instrumentation, and the desktop water-distribution route hint no
  longer catches generic gravity-fed water filter prompts. Physical phone proof
  for `phone-functional-follow-up` passed on `RFCX607ZM8L`.
- Recent-thread preview ordering is current through `cbdd3014`:
  `ChatSessionStore` now makes same-timestamp preview ordering deterministic,
  with focused store proof.
- Review source stack copy is current through `e5edf94d`: foundry guidance now
  uses the corrected quoted text across source-stack and tablet evidence
  presentation tests.
- No-BM25 route FTS ordering is current through `2e8b9292`:
  `PackRouteFtsOrderPolicy` owns route FTS order-label construction while
  `RetrievalRoutePolicy` and route SQL planning delegate to the focused policy.
  Focused proof lives in `PackRouteFtsOrderPolicyTest` and
  `PackRouteSearchSqlPolicyTest`.
- Reviewed-card predicate ownership is current through `8164a7bf`:
  `ReviewedCardPredicatePolicy` owns answer-card selector markers and drift
  guards while `AnswerCardRuntime` keeps answer-plan assembly. Focused proof
  lives in `AnswerCardRuntimeTest` and `ReviewedCardPredicatePolicyTest`.
- Route-search SQL hardening is current through `855fe260`:
  `PackRouteSearchSqlPolicy` locally filters null/blank tokens and categories
  before generating FTS or LIKE plans, preserving valid SQL/args behavior while
  no-oping invalid inputs. Focused proof lives in
  `PackRouteSearchSqlPolicyTest`.
- Functional UX smoke lock ergonomics are current through `5e761339`: the
  matrix wrapper holds one outer device lock across all presets, child smoke
  runs skip nested lock acquisition, and shared lock waits now log elapsed time,
  remaining timeout, and lock path breadcrumbs.
- Physical phone saved proof is current after `31e40916`: the
  `phone-functional-saved` smoke profile passed on `RFCX607ZM8L` with real
  guide save/unsave semantics in addition to Saved back, Saved search, and
  pinned-guide open/back coverage. Release proof is self-contained in
  `artifacts/physical_phone_saved_after_31e40916/run_summary.json`: status
  `pass`, `OK (4 tests)`, APK SHA
  `2fb9ecf7f3691c52c10845cd72cf5da6ef816edb9838b5c0f8d3e53de38e1120`,
  physical phone serial `RFCX607ZM8L`, installed pack identity, screenshots,
  dumps, logcat, and bundle paths.
- PowerShell quality-gate contracts are current through `a3bea24c`: parser and
  dry-run tests now match the refreshed array-style finalize/skip-build
  forwarding and absolute artifact-root resolution for instrumented smoke
  outputs.
- Product fixture-copy cleanup is current through `60527001`: normal
  `activity_main` layouts no longer hard-code review/demo guide-count copy, and
  `ProductSearchCtaResourceLeakageTest` guards product layouts against both
  external-review labels and fixture text.
- Follow-up retry proof is current through `864e2b28`: a visible stalled retry
  presentation still routes through the submit dedupe gate and blocks duplicate
  generation while preserving the resolved query.
- Install completion publication planning is current through `5c44432e`:
  `MainInstallCompletionPolicy` now returns a `CompletionPlan` with the browse
  publication payload only when install completion should publish current
  browse guides; auto-query and result routes preserve current payloads.
- Real-pack external-review route proof is current through `8965c17d`: the
  mobile headless parity fixture now exercises current pack routes for house,
  roof/windows, food theft, gravity-fed water, soap, and glass prompts, checking
  route kind, route order label, top search inclusion, and first context guide.
- Review fixture guide-count sentinel cleanup is current through `7198fecb`:
  `MainActivity` no longer carries the unconditional `754 guides` polish
  sentinel, and `ReviewDemoFixtureBoundaryTest` guards against reintroducing it.
- No-FTS real-pack external-review route proof is current through `0c2f23e0`:
  the headless parity fixture now forces FTS off and verifies the six
  external-review prompts still surface primary expected guides in search and
  context.
- Physical phone follow-up proof is current after `8965c17d`:
  `phone-functional-follow-up` passed on `RFCX607ZM8L` for Ask routing, IME
  follow-up submit, empty follow-up submit, and inline thread history. Artifact
  root: `artifacts/physical_phone_followup_after_8965c17d/`; release proof is
  self-contained in `run_summary.json` with status `pass`, `OK (4 tests)`, the
  same APK SHA
  `2fb9ecf7f3691c52c10845cd72cf5da6ef816edb9838b5c0f8d3e53de38e1120`,
  physical phone serial, installed pack identity, screenshots, dumps, logcat,
  and bundle paths.
- Main review-display gating is current through `4f401c23`: tablet search
  header formatting, adapter search-row visual state, and manual-home recent
  thread label selection now route through `MainReviewDisplayPolicy`, keeping
  review latency/display decisions behind the same policy boundary as phone
  search chrome and preview copy.
- Functional route/follow-up coverage is current through `cce350e0`: focused
  tests cover Ask-results home-chrome Back returning through the same browse
  effect, and busy failed-query retry blocking duplicate generation while
  preserving the retry query.
- Functional smoke matrix diagnostics are current through `96cc63ac`: the
  wrapper now reports preset progress, elapsed time, artifact roots, FailFast
  stop messages, completed/total preset counts, and effective skip/reuse build
  state in the summary. Contract tests also guard matrix preset registration
  against child smoke-runner drift.
- Main shared-input submit ownership is current through `a6032456`: the visible
  Search/Ask submit action now routes through `MainSharedInputSubmitPolicy`,
  with focused chrome/phone-navigation coverage.
- Main home category presentation is current through `4bc840a4`: manual-home
  category shelf height decisions now live in
  `MainHomeCategoryPresentationPolicy`, with phone/tablet focused coverage.
- Detail docked-composer presentation cleanup is current through `efc6e97c`:
  composer hint selection now lives in `DetailDockedComposerPresentationPolicy`
  while `DetailActivity` keeps focus, IME, submit, and generation wiring.
- Pack search finalization and telemetry cleanup is current through `dcd549f1`:
  `PackRepository` funnels repeated combined-hit projection, prerank telemetry,
  rerank timing, and rerank telemetry through `finalizeCombinedHitsForSearch()`
  without changing route merge behavior; focused telemetry coverage now guards
  vector-disabled, centroid-missing, and hybrid finalization shapes. Telemetry
  formatting, elapsed-time conversion, outcome labels, latency breakdown
  construction, and lexical/vector candidate telemetry now live in
  `PackSearchFinalizationPolicy` and `PackSearchTelemetryPolicy`.
- Pack keyword scoring cleanup is current through `5e14994a`: keyword scoring
  and metadata bonus composition now live in `PackKeywordScoringPolicy` without
  touching SQL, route collection, vector/rerank/fusion, query parsing, or answer
  anchors.
- Detail tablet state-builder cleanup is current through `3085fc1e`:
  tablet xref row projection, turn-state projection, and anchor-state
  projection, plus guide-mode label/summary/anchor projection, now live in
  `DetailTabletStateBuilder`, with focused tests for label trimming, relation
  buckets, active-turn selection, evidence anchor projection, guide-mode answer
  and thread summaries, and null/empty input.
- Functional harness proof is current through `27a8ffdf`: AndroidTest coverage
  now includes no-result search system Back returning to Browse/Home with Ask
  ownership cleared. This commit was compile-validated with
  `assembleDebugAndroidTest`; it was not live-run on-device in the cleanup loop.
- Tracker is refreshed through pushed head `27a8ffdf`. Final focused gate after
  that head passed: `HomeCategoryPolicyTest`, `MainActivityHomeChromeTest`,
  `DetailFollowupLandscapeComposerTest`, `EmergencySurfacePolicyTest`,
  `PackRepositoryTest`, `PackTextMatchPolicyTest`,
  `PackRepositoryRouteOutputParityTest`, and `assembleDebugAndroidTest`.

## Remaining Next Slices

- Continue shrinking `MainActivity` only where backend lifecycle or route
  orchestration still owns too much; new route work should extend
  `MainRouteCoordinator` tests rather than restoring route fields to
  `MainActivity`.
- Architecture scout follow-up priority: continue tablet/detail state-builder
  extraction in narrow seams; do not start a broad `PackRepository` search
  pipeline rewrite without route-output parity proof.
- Review/demo leakage follow-up: continue moving remaining fixture-shaped
  display decisions through explicit review-policy gates; the product search CTA
  resource-name leak is closed and guarded.
- Do not rerun the result-publication extraction unless a regression points
  there; future `MainActivity` cleanup should target still-mixed route side
  effects outside `MainResultPublicationPolicy`.
- Keep repository cleanup incremental around remaining pure policy/helper
  extraction with parity tests.
- Architecture scout next priorities: continue pure Java detail tablet-state
  extraction; then consider broader docked-composer model assembly,
  `PackFtsRuntimeDetector` SQLite-bound detection extraction, and smoke harness
  helper splits. Avoid broad route-focused retrieval rewrites and avoid
  `TabletDetailScreen.kt` refactors unless a visual regression demands it.
- Functional proof backlog from scout: physical Ask-owned submit proof on the
  phone, zero-result search visible Back live proof, Ask-unavailable/no-source
  Back, task-root detail visible Back, and Saved tab semantics assertions in
  the Android harness.
- UI normalization is a future explicit Android-only slice (`UI-NORM1 Shared
  Chrome Contract`): align `TopBar`, `BottomTabBar`/`NavRailMetrics`,
  `IdentityStrip`, `SenkuTypography`, XML text appearances/dimens, and Java
  chrome mount/bind points together. Do not mix this with backend cleanup.
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
