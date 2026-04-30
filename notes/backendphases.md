# Backend Cleanup Phase Tracker

Last updated against master `db9fedf`.

Purpose: prevent future agents from rerunning Ask/query backend cleanup that is already complete. Keep this note short; implementation detail belongs in commits and tests.

## Completed

- Ask/Search submit routing extracted into `AskSearchCoordinator`.
- Ask runtime orchestration extracted into `AskQueryController`.
- Host inference now has explicit host policy handling.
- Review/demo fixture behavior is explicitly gated.
- `OfflineAnswerEngine.prepare` was split into named stages.
- Retrieval route detection helpers were extracted into `RetrievalRoutePolicy`.
- Black-box Ask backend route tests now cover deterministic, reviewed-card,
  generative host, unavailable, and stale-request paths.

## Remaining Next Slices

- Continue shrinking `MainActivity` only where backend lifecycle code remains; avoid visual/layout work.
- Keep `PackRepository` cleanup incremental: extract pure policy/helpers only, with route output parity tests.
- Audit review/demo fixtures when adding new mocks so production query paths stay fixture-free.
- Re-run focused backend tests after each backend slice, plus UI state packs only as regression proof when behavior could affect screenshots.

## Guardrails

- Do not touch app UI files for this tracking-note task.
- Do not rerun completed extraction phases unless a regression points to a specific follow-up.
- Prefer small backend-only commits with focused tests.
