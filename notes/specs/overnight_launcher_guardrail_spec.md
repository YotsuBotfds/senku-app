# Spec: Overnight Launcher Guardrail Wrapper (Phase 1)

> Historical 2026-04-19 planning/spec note.
>
> Current-checkout status as of 2026-04-23: the scripts referenced here still
> assume `scripts/overnight_continue_loop.ps1`, but that file is absent in this
> checkout. See `notes/OVERNIGHT_LAUNCHER_MISSING_LOOP_INVESTIGATION_20260423.md`
> for the current-state investigation.

Date: 2026-04-19
Task: `DAY-L-01`

## Purpose

The current `scripts/start_overnight_continuation.ps1` +
`scripts/overnight_continue_loop.ps1` pair is thin orchestration with
no guardrails. One stop-condition trips the whole night, there is no
per-task timeout, no stale-process cleanup, no preflight. The
2026-04-19 queue ran cleanly because the preflight-gate fix and the
note-cell hygiene rule were landed just before firing, not because the
launcher defended against failure.

Phase 1 adds the minimum viable set of guardrails: preflight, per-task
timeout, stale-lock sweep. Later phases can add isolation, retry, and
graceful stop — out of scope here.

## Non-Goals (Phase 1)

- Per-task subprocess isolation (each task in its own PowerShell)
- Transient-failure retry logic
- Kill-file / Ctrl+C graceful stop
- Notification hooks (email, push, etc.)
- Resume-from-checkpoint after catastrophic crash beyond what the
  existing `notes/OVERNIGHT_CONTINUATION_STATE.json` already provides

Phase 2+ can revisit any of these once Phase 1 is stable in the wild.

## Scope

New script: `scripts/run_overnight_queue_wrapped.ps1`

This script wraps a call into the existing
`scripts/overnight_continue_loop.ps1`. It does not replace the loop.
It adds a preflight pass before launching the loop and a per-task
watchdog that runs alongside the loop.

Existing scripts are not modified in Phase 1 beyond adding optional
parameters that the wrapper can pass through.

## Wrapper Behavior

### Preflight (runs once before the loop starts)

1. **Stale lock sweep.** If `.git/index.lock` exists and no `git`
   process is running, delete it and log the action. If a `git`
   process is running, stop and log — a real operation is in flight.

2. **Clean tracked tree check.** Run:
   ```
   git diff --name-only
   git diff --cached --name-only
   ```
   Both must be empty. If either returns any path, stop and log.
   Untracked files are not a failure (matches the 2026-04-19 preflight
   fix landed in `df70a43`).

3. **HEAD sanity.** Record current HEAD hash in the run log.
   Confirm HEAD is on the expected branch (main by default; can be
   overridden by `-Branch` parameter).

4. **Baseline tests green.** Run:
   ```
   python3 -m unittest discover -s tests -v
   python3 scripts/validate_special_cases.py
   ```
   Both must pass. If either fails, stop and log.

5. **State Log row validator.** Run the 8-cell check from the overnight
   queue note. Must return all rows validated.

6. **Queue note present and well-formed.** Read the queue note path
   passed via `-QueueNote <path>` parameter. Confirm it exists and
   has at least one `## Task ` section. If missing, stop and log.

7. **Stale process sweep (advisory).** Log any running `git`,
   `python3`, or `pwsh` processes older than 1 hour. Do not kill them
   — just log. Phase 2 can decide policy.

8. **Run log directory prep.** Ensure the run log directory exists
   (default:
   `artifacts/overnight_queue_<YYYYMMDD_HHMMSS>/`). Write a preflight
   section to `run_log.md` summarizing all of the above with
   pass/fail per step.

If any of steps 1-6 fail, the wrapper does not launch the loop.

### Per-Task Watchdog

After preflight passes, the wrapper launches
`scripts/overnight_continue_loop.ps1` as a child process. The wrapper
does NOT replace the loop's per-task logic — it monitors from outside.

Watchdog rules:

- **Per-task timeout.** Default: 30 minutes. Configurable via
  `-PerTaskTimeoutMinutes <int>`. If a single task exceeds the
  timeout (measured from the last task-start marker in the run log),
  the wrapper kills the loop child process and writes a timeout stop
  record to the run log.

- **Task boundaries are detected from run log markers.** Each task
  section in the run log starts with `## <TASK-ID> - <ISO-8601>`.
  The wrapper polls the run log every 60 seconds and resets its
  task-start timestamp whenever a new task section appears.

- **Loop-process health check.** If the child process exits for any
  reason, the wrapper writes an exit record (exit code, duration)
  and stops. Phase 2 can add retry.

### Parameters

```
param(
  [Parameter(Mandatory=$true)][string]$QueueNote,
  [string]$Branch = "main",
  [int]$PerTaskTimeoutMinutes = 30,
  [string]$RunLogDir = ""  # default computed from timestamp
)
```

### Exit Codes

- `0` — preflight passed and loop completed all tasks
- `1` — preflight failed (see run log for which step)
- `2` — per-task timeout killed the loop
- `3` — loop child exited non-zero (see run log for exit code)
- `4` — stale git process blocked lock cleanup

## Run Log Format

The wrapper writes to `run_log.md` in the run log directory. Initial
section is `## Preflight` with sub-sections for each step. The loop
then appends task sections normally. If the wrapper kills the loop,
it appends a `## Wrapper Stop` section at the end.

## Tests

Phase 1 ships with one PowerShell integration test that invokes the
wrapper against a mocked queue note and verifies the preflight output
structure. Test harness pattern:
`tests/powershell/Run-OvernightQueueWrapperTests.ps1`.

Full end-to-end watchdog test (simulating a hanging task) is deferred
to Phase 2 — manual verification is acceptable for the Phase 1
landing.

## Acceptance Criteria

- Wrapper script exists at `scripts/run_overnight_queue_wrapped.ps1`
- Preflight performs all 8 steps and writes structured run log output
- Per-task timeout kills the loop child after configured duration
- Stale `.git/index.lock` is swept when no git process is running
- One integration test covers the preflight happy path
- Running the wrapper with `-QueueNote notes/DAYLIGHT_HYGIENE_QUEUE_2026-04-19.md`
  on a clean tree successfully launches and closes the loop (manual
  verification is fine for this bullet)

## Files Touched

- **New:** `scripts/run_overnight_queue_wrapped.ps1`
- **New:** `tests/powershell/Run-OvernightQueueWrapperTests.ps1`
- **Unchanged:** `scripts/start_overnight_continuation.ps1` and
  `scripts/overnight_continue_loop.ps1` (Phase 1 does not modify the
  existing loop; Phase 2 may add optional hooks)

## Out of Scope Reminders

- Do not modify Wave B code
- Do not modify the existing overnight loop script in Phase 1
- Do not add retry or isolation — those are Phase 2 decisions
- Do not add notification hooks
