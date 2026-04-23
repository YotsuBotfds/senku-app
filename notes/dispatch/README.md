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
  relevant MCP - e.g. context7 for Android/Compose/library API docs,
  git MCP for repo-history questions, sequential-thinking for
  genuinely ambiguous decomposition. Codex has context7, git, and
  sequential-thinking registered in `~/.codex/config.toml` but
  doesn't always reach for them unprompted. Especially valuable
  when the symptom involves framework behavior (IME, focus,
  lifecycle, recomposition) - without the hint, Codex tends to
  layer defensive code on top of assumptions rather than checking
  authoritative docs (R-ui2 v1->v2->v3 is the cautionary tale).

## Active slices

CP9 is closed. RC v5 cut landed 2026-04-20, Wave C is closed through
`W-C-4`, `D11` has landed, and no slices are currently in flight.

Use `notes/CP9_ACTIVE_QUEUE.md` as the live source for next-step
ordering, post-RC backlog truth, and current status. Do not infer the
next slice from this README alone.

## Landed (not yet rotated)

Retained live root records:
`A1_retry_5560_landscape.md` (superseded-but-kept),
`P5_scope_note_landscape_phone.md` (cancelled-but-kept),
`probe_rain_shelter_mode_flip.md` (stale probe kept per D5).

Unrotated prompt drafts still present at the dispatch root:
`D9_tracker_doc_reconciliation_and_historical_labeling.md`,
`D10_wave_c_direction_note_lock.md`,
`D11_repo_root_retention_triage.md`,
`W-C-0_panel_expansion_and_runner_preflight.md`,
`W-C-1a_final_mode_runtime_emission_fix.md`,
`W-C-1_final_mode_telemetry_aggregation_helper.md`,
`W-C-2_desktop_abstain_threshold_tuning.md`,
`W-C-3_android_abstain_vector_mirror.md`,
`W-C-4_uncertain_fit_upper_band_calibration.md`,
`W-C-5a_low_coverage_canary_probe_and_closeout.md`.

Those files reflect landed or historical dispatch work that has not yet
been rotated out of the root. This slice does not move them.

## Dispatch-root trust

`notes/dispatch/` is not a fully normalized live root. It currently
mixes:

- `README.md`
- the retained live records above
- unrotated prompt drafts that still document landed work
- `completed/` for rotated historical slice files

For cleanup, redispatch, or historical reasoning, take a fresh inventory
of the root and cross-check `notes/CP9_ACTIVE_QUEUE.md` instead of
trusting old D8-era assumptions about what has already been rotated.

## Superseded - do not redispatch

- `A1_retry_5560_landscape.md` - prescribed ESC-dismiss approach was
  wrong (ESC keyevent 111 does not close Gboard on modern AVDs).
  A1b's device-level `UiDevice.pressBack()` variant landed GREEN.
  Retained at the dispatch root as a live historical record.

## Cancelled - not dispatched

- `P5_scope_note_landscape_phone.md` - partial-GREEN fallback not
  needed; A1b closed Stage 0 GREEN on 5560 landscape. File retained
  at the dispatch root for record.
