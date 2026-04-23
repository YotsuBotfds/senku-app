# `stop_android_device_processes_safe.ps1` investigation (2026-04-23)

## Scope

This note documents the current-checkout behavior of
`scripts/stop_android_device_processes_safe.ps1` as a read-only truth-surface
slice. No script changes are part of D33.

## Current-checkout behavior

In the current checkout, `scripts/stop_android_device_processes_safe.ps1` is a
global Android device/emulator process-stop helper, not a lane-bounded harness
cleanup tool.

## Evidence

- The script defines a fixed global process-name set:
  `adb`, `emulator`, `qemu-system-x86_64`, and `qemu-system-aarch64`.
- It enumerates host processes with `Get-Process` and filters only by those raw
  process names, with no harness-run id, workspace path, emulator-serial, or
  lane ownership boundary.
- It force-stops every matching process via `Stop-Process -Id $target.Id -Force`.
- Its terminal message explicitly states: Android device/emulator processes only,
  and that it intentionally does not target `node`.

## Conclusion

The current-checkout contract is host-global Android cleanup. This helper can
terminate any matching adb/emulator/qemu process on the host, regardless of
which harness lane, emulator posture, or operator workflow started it.

That makes this script part of the emergency Android device/emulator cleanup
branch, not the narrower harness-helper tranche covered by D31 and not the mixed
host-stop/device-reset branch covered by D32.

## Recommended next step

Keep this helper on its own allow/repair/retire follow-up path for global
device-process stop behavior.

Keep `scripts/cleanup_android_harness_artifacts.ps1` separate as the
recursive-delete contract slice rather than merging the two cleanup surfaces.
