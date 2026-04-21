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
currently in flight.

Remaining post-RC tracked items live in `notes/CP9_ACTIVE_QUEUE.md`:
`R-ret1b` corpus-vocab revision, `R-anchor2`, `R-host`, and
ask-telemetry enrichment.

## Landed (not yet rotated)

D4 rotated the four post-RC retrieval-chain slice files
(`R-ret1c`, `R-cls2`, `R-anchor1`, `R-gal1`) into
`notes/dispatch/completed/`. The dispatch root should now only hold
live/open items plus the retained superseded or cancelled records.

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
