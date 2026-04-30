# Backend Cleanup Phase Tracker

Last updated against master `c07fe78`.

Purpose: prevent future agents from rerunning Ask/query backend cleanup that is already complete. Keep this note short; implementation detail belongs in commits and tests.

## Completed

- Ask/Search submit routing extracted into `AskSearchCoordinator`.
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
- Compact answer save, production answer body formatting, generic warning owner
  copy, and emergency composer anchoring have focused regression coverage.
- XML parity helper setup and tablet rail navigation proof are in place for
  follow-up backend cleanup confidence.
- Black-box Ask backend route tests now cover deterministic, reviewed-card,
  generative host, unavailable, and stale-request paths.
- UI stabilization cleanup is complete for model-less reviewed-card preflight
  gating by supported query, first-class main route-state persistence,
  `ReviewDemoFixtureSet` boundary, Pack support/anchor/route-search helpers,
  physical phone smoke summary/latest validation, and detail/home overflow
  policy clarification.

## Remaining Next Slices

- Continue shrinking `MainActivity` only where backend lifecycle, route-decision,
  or category orchestration code remains; avoid visual/layout work.
- Keep `PackRepository` cleanup incremental around pure policy/helper extraction,
  especially retrieval/answer anchoring and catalog support logic, with route
  output parity tests.
- Keep review/demo fixture gates explicit when adding mocks or preview labels so
  production query paths stay fixture-free.
- Prefer backend/code-health slices that remove mixed responsibilities from
  activities/repositories without changing chrome, copy, or layout behavior.
- Re-run focused backend tests after each backend slice, plus UI state packs only
  as regression proof when behavior could affect screenshots.

## Guardrails

- Do not touch app UI files for this tracking-note task.
- Do not rerun completed extraction phases unless a regression points to a specific follow-up.
- Prefer small backend-only commits with focused tests.
