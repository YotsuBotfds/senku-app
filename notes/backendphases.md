# Backend Cleanup Phase Tracker

Last updated after commit `b96f325` on 2026-04-30.

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

## Remaining Next Slices

- Continue shrinking `MainActivity` only where backend lifecycle or route
  orchestration still owns too much.
- Keep repository cleanup incremental around remaining pure policy/helper
  extraction with parity tests.
- Keep review/demo fixture gates explicit when adding mocks or preview labels so
  production query paths stay fixture-free.
- Prefer backend/code-health slices that remove mixed responsibilities from
  activities/repositories without changing chrome, copy, or layout behavior.
- Source-selection cross-reference still has an exposed bug: answer-mode source
  selection can lose the source-anchored cross-reference lane and needs a
  focused follow-up before treating that path as closed.
- Re-run focused backend tests after each backend slice; use UI state packs only
  when behavior could affect screenshots.

## Guardrails

- Do not touch app UI files for this tracking-note task.
- Do not rerun completed extraction phases unless a regression points to a specific follow-up.
- Prefer small backend-only commits with focused tests.
