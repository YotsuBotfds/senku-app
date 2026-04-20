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

## Active slices

See `notes/CP9_ACTIVE_QUEUE.md` Dispatch order cheat-sheet for
the canonical "what runs when" picture.

In flight:
- `R-pack_poisoning_chunk_rebuild.md` - poisoning guide chunk + metadata fix; doing an ingest rebuild

Parallel-safe with R-pack (dispatch now if you want):
- `D3_pre_rc_followup_cleanup.md` - doc-only single commit; absorbs four small D1/D2 deferred items

Sequential after R-pack lands:
- `RP1_apk_rebuild_and_reprovision.md` - APK rebuild + four-serial re-provision; needs R-pack's commit + pack output

Sequential after RP1 lands:
- `S2-rerun_stage2_revalidation.md` - Wave B re-validation against the fixed matrix; dispatch with "use subagents where stated" for the 4-way fan-out

## Landed (not yet rotated)

Rotation performed in D3 on 2026-04-19: the previously landed slice files were moved to `notes/dispatch/completed/`.

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
