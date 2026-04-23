# Overnight Launcher Missing-Loop Investigation - 2026-04-23

## Problem statement

The current checkout still contains overnight launcher surfaces that assume
`scripts/overnight_continue_loop.ps1`, but that loop script is absent here.
That leaves the overnight continuation path documented historically but not
actually runnable in this repo state.

## Evidence

- `scripts/start_overnight_continuation.ps1:7` sets `$scriptPath` to
  `scripts\overnight_continue_loop.ps1` and launches it with `Start-Process`.
- `scripts/run_overnight_queue_wrapped.ps1:413` sets `$loopScript` to
  `scripts/overnight_continue_loop.ps1` and launches the same child path when
  pending tasks remain after preflight.
- `Test-Path scripts/overnight_continue_loop.ps1` returned `False` in this
  checkout on 2026-04-23, while `scripts/start_overnight_continuation.ps1`,
  `scripts/run_overnight_queue_wrapped.ps1`,
  `scripts/start_fastembed_server.ps1`, and
  `scripts/start_litert_host_server.ps1` all returned `True`.
- `scripts/start_fastembed_server.ps1` is an adjacent launcher/helper surface
  only: it resolves `scripts\fastembed_openai_server.py` and does not reference
  the overnight loop path.
- `scripts/start_litert_host_server.ps1` is also an adjacent launcher/helper
  surface only: it resolves `scripts\litert_native_openai_server.py` and does
  not reference the overnight loop path.

## Current-checkout conclusions

- `scripts/start_overnight_continuation.ps1` is present but not runnable in the
  current checkout because its target loop script is missing.
- `scripts/run_overnight_queue_wrapped.ps1` is tracked and historically landed,
  but its pending-task child-launch path depends on the same missing
  `scripts/overnight_continue_loop.ps1` file.
- `scripts/start_fastembed_server.ps1` is adjacent to this branch of launcher
  work but is not implicated in the missing-loop dependency.
- `scripts/start_litert_host_server.ps1` is adjacent to this branch of launcher
  work but is not implicated in the missing-loop dependency.

## Recommended next step

- Run a dedicated restore-or-retire follow-up for the overnight loop path:
  either restore `scripts/overnight_continue_loop.ps1` plus its intended
  current contract, or retire/update the launcher surfaces that still claim it
  exists.
- Keep any Rule-2b / cross-reference tracking work for
  `scripts/start_fastembed_server.ps1` and `scripts/start_litert_host_server.ps1`
  as separate future slices rather than widening this investigation.
