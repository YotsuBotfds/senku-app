# Backend Cleanup Phase Tracker

Last updated after local validation of the backend cleanup slices through physical smoke proof on 2026-05-01.

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
- Physical smoke proof is current for the completed backend cleanup stack.

## Remaining Next Slices

- Continue shrinking `MainActivity` only where backend lifecycle or route
  orchestration still owns too much.
- Keep repository cleanup incremental around remaining pure policy/helper
  extraction with parity tests.
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
