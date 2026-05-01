# Backend Cleanup Phase Tracker

Last updated after the 2026-05-01 backend cleanup/proof refresh.

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
  output.
- Physical and instrumented smoke evidence was refreshed, including stronger
  presets for shared input, Saved/back, answer provenance, and phone/tablet
  route checks.

## Remaining Next Slices

- Continue shrinking `MainActivity` only where backend lifecycle or route
  orchestration still owns too much.
- Keep repository cleanup incremental around remaining pure policy/helper
  extraction with parity tests.
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
