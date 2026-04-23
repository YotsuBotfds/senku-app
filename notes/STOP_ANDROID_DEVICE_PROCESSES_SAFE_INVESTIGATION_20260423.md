# `stop_android_device_processes_safe.ps1` investigation (2026-04-23)

## Scope

This note now records both the original D33 truth-surface findings and the D35
disposition for `scripts/stop_android_device_processes_safe.ps1`.

## D33 findings that remained true at retirement time

The D33 read-only audit remained the basis for the decision in D35:

- The helper defined a fixed global process-name set:
  `adb`, `emulator`, `qemu-system-x86_64`, and `qemu-system-aarch64`.
- It enumerated host processes with `Get-Process` and filtered only by those
  raw process names, with no harness-run id, workspace path, emulator serial,
  or lane ownership boundary.
- It force-stopped every matching process with `Stop-Process -Id $target.Id
  -Force`.
- Its terminal message explicitly stated that it targeted Android
  device/emulator processes only and intentionally did not target `node`.

## D35 disposition

As of D35, `scripts/stop_android_device_processes_safe.ps1` was retired from
the repo and removed rather than tracked or repaired.

## Why retirement was the cleanest outcome

- The helper's scope was host-global raw-process killing rather than
  lane-bounded harness cleanup.
- Current tracked workflow docs did not provide a live operator anchor that
  recommended routine use of this helper.
- The narrower tracked helper `scripts/stop_android_harness_orphans.ps1`
  remains available for the lane-bounded cleanup path that still has an active
  documented workflow.

## Forward rule

If a future global Android cleanup helper is needed, it should return only
through a new explicit contract and tracked operator guidance, not by silently
restoring this retired file.
