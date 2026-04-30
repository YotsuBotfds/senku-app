# Backend Cleanup Phase Tracker

Last updated after the 2026-04-30 Android backend and UI-smoke cleanup waves.

Purpose: prevent future agents from rerunning Ask/query backend cleanup that is already complete. Keep this note short; implementation detail belongs in commits and tests.

## Completed

- Ask/Search submit routing extracted into `AskSearchCoordinator`.
- Shared-input submit routing is integrated: explicit Search submits through the
  active shared-input target, so Ask-owned input stays on the Ask route.
- Ask runtime orchestration extracted into `AskQueryController`.
- Host inference now has explicit host policy handling.
- Main result entry, submit, browse-back, empty-result back, follow-up composer,
  detail-back, and rail callback routing now pass through focused helpers/tests.
- Review/demo fixture behavior is explicitly gated, including search rows, related
  guide labels, tablet evidence/preview rows, proof source labels, and the rain
  shelter demo path.
- `OfflineAnswerEngine.prepare` was split into named stages.
- Retrieval route detection helpers were extracted into `RetrievalRoutePolicy`.
- Route FTS backfill, water distribution support, answer anchor, and home
  category policies have been extracted from heavier activity/repository code.
- Answer-anchor choice construction now uses named policy/builder fields instead
  of long positional wiring.
- Answer-context support ranking now has explicit policy coverage for candidate
  scoring, vector fallbacks, and retrieval-order tie behavior.
- Detail source navigation policy is covered for source buttons, related cards,
  guide detail transitions, and back behavior.
- Specialized route-anchor policy and candidate predicate extraction are complete
  enough to keep route decisions out of large inline repository blocks.
- Compact answer save, production answer body formatting, generic warning owner
  copy, and emergency composer anchoring have focused regression coverage.
- XML parity helper setup and tablet rail navigation proof are in place for
  follow-up backend cleanup confidence.
- Black-box Ask backend route tests now cover deterministic, reviewed-card,
  generative host, unavailable, and stale-request paths.
- Physical smoke interactions have covered shared input, Ask/Search submit,
  detail/source navigation, latest validation, and key phone/tablet route paths.
- Instrumented UI smoke now has a named functional phone preset for shared-input,
  Saved/back, and answer provenance routes.
- Functional smoke coverage now includes supported-query gating, shared-input
  chrome, detail descriptions, prompt pack status, and screenshot-state probes.
- UI stabilization cleanup is complete for model-less reviewed-card preflight
  gating by supported query, first-class main route-state persistence,
  `ReviewDemoFixtureSet` boundary, Pack support/anchor/route-search helpers,
  physical phone smoke summary/latest validation, and detail/home overflow
  policy clarification.

## Remaining Next Slices

- Continue shrinking `MainActivity` only where backend lifecycle or
  route-decision orchestration remains.
- Keep `PackRepository` cleanup incremental around remaining pure policy/helper
  extraction with route-output parity tests.
- Keep review/demo fixture gates explicit when adding mocks or preview labels so
  production query paths stay fixture-free.
- Prefer backend/code-health slices that remove mixed responsibilities from
  activities/repositories without changing chrome, copy, or layout behavior.
- Re-run focused backend tests after each backend slice; use UI state packs only
  when behavior could affect screenshots.

## Guardrails

- Do not touch app UI files for this tracking-note task.
- Do not rerun completed extraction phases unless a regression points to a specific follow-up.
- Prefer small backend-only commits with focused tests.
