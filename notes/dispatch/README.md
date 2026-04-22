# `notes/dispatch/`

Editable slice prompts for main-agent dispatch. One file per slice. Each
file is meant to be opened, tweaked, and copy-pasted into a fresh main
agent session. Treat them as living - if a slice evolves mid-session,
edit the file, do not let the drift live only in your head.

## Convention

- Filename: `<slice_id>_<short_name>.md` (e.g. `P1_planner_cleanup.md`).
- Slice IDs map 1:1 to rows in `notes/CP9_ACTIVE_QUEUE.md`.
- Each file has: `Slice`, `Role`, `Preconditions`, `Outcome`, `Boundaries`,
  `Acceptance`, `Delegation hints`, `Report format`.
- Role is always "main agent (`gpt-5.4 xhigh`)" unless we are explicitly
  dispatching a scout or worker directly.
- Delegation hints are **suggestions**, not orders. Main agent owns the
  final routing choice per `notes/SUBAGENT_WORKFLOW.md`.
- For slices that touch external framework/library behavior (Android
  SDK, Compose, Gradle, Python libs), include a one-line hint in
  `Anti-recommendations` (or wherever natural) pointing Codex at the
  relevant MCP — e.g. context7 for Android/Compose/library API docs,
  git MCP for repo-history questions, sequential-thinking for
  genuinely ambiguous decomposition. Codex has context7, git, and
  sequential-thinking registered in `~/.codex/config.toml` but
  doesn't always reach for them unprompted. Especially valuable
  when the symptom involves framework behavior (IME, focus,
  lifecycle, recomposition) — without the hint, Codex tends to
  layer defensive code on top of assumptions rather than checking
  authoritative docs (R-ui2 v1→v2→v3 is the cautionary tale).

## Active slices

CP9 is closed (RC v5 cut landed 2026-04-20). The post-RC retrieval
chain substantively closed 2026-04-20 with four landings:
`2ec77b8`, `0a8b260`, `971961b`, and `585320c`. No slices are
currently in flight after `R-anchor-refactor1`.

Key remaining post-RC tracked items live in `notes/CP9_ACTIVE_QUEUE.md`:
the post-`R-pack-drift1` hygiene/audit lane is `R-hygiene2` then
`R-track1`, followed by Wave C planning and optional ask-telemetry
enrichment; `R-tool2`, `R-anchor-refactor1`, `R-ret1b`, `R-host`,
`R-search`, and `R-telemetry` are closed, and the gallery is republished at
`artifacts/external_review/ui_review_20260421_retrieval_chain_closed/`
(45/45). The carry-over `R-search` wrapper-hang observation remains in
backlog there as well.

## Landed (not yet rotated)

Pending rotation as of `R-anchor-refactor1` landing:
`R-anchor-refactor1_pack_support_breakdown.md`,
`R-tool2_state_pack_logcat_capture.md` (next D-slice will rotate).
Retained live: `A1_retry_5560_landscape.md` (superseded-but-kept),
`P5_scope_note_landscape_phone.md` (cancelled-but-kept),
`probe_rain_shelter_mode_flip.md` (stale probe kept per D5),
`D7_post_r_ret1b_tracker_reconciliation.md` (D7 bootstrap - waiting on
next D-slice), `R-tool2_SCOUT_PROMPT.md` (scout-companion, rotate with
R-tool2 slice file).

## Superseded - do not redispatch

- `A1_retry_5560_landscape.md` - prescribed ESC-dismiss approach was
  wrong (ESC keyevent 111 does not close Gboard on modern AVDs).
  A1b's device-level `UiDevice.pressBack()` variant landed GREEN.
  Rotation by a later cleanup slice.

## Cancelled - not dispatched

- `P5_scope_note_landscape_phone.md` - partial-GREEN fallback not
  needed; A1b closed Stage 0 GREEN on 5560 landscape. File retained
  for record.

Rotate (move to `notes/dispatch/completed/` or delete) when a slice
lands. Do not leave stale slice files around - a new dispatcher should
trust that every file here is current.
