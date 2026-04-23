# stop_android_harness_runs.ps1 investigation (2026-04-23)

## Scope

Read-only audit of `scripts/stop_android_harness_runs.ps1` in the current
checkout. This note exists to make the script's present behavior explicit
without changing the script itself.

## Current-checkout finding

`scripts/stop_android_harness_runs.ps1` is not purely a host-side harness
cleanup helper. In the current checkout it is a mixed host-stop plus
device-reset surface.

## Evidence

1. Host-side harness runner matching:
   - The script builds a bounded allowlist of runner script names at
     `scripts/stop_android_harness_runs.ps1:93-101`.
   - It then enumerates `Win32_Process` and filters `powershell.exe`
     processes by command line, current-process exclusion, optional emulator
     target match, and the runner allowlist at
     `scripts/stop_android_harness_runs.ps1:104-111`.
   - Matching host processes are force-stopped with `Stop-Process -Force` at
     `scripts/stop_android_harness_runs.ps1:123-133`.

2. adb device resolution:
   - `Get-TargetDevices` normalizes requested devices and, when no explicit
     targets are supplied, falls back to `adb devices` discovery at
     `scripts/stop_android_harness_runs.ps1:31-58`.

3. Package force-stop and PID kill behavior:
   - `Stop-AndroidPackages` force-stops `com.senku.mobile.test` and
     `com.senku.mobile` on each resolved device at
     `scripts/stop_android_harness_runs.ps1:60-72`.
   - The same function then scans `adb shell ps -A -o PID,NAME,ARGS` output
     and issues `kill -9` against matching Senku package PIDs at
     `scripts/stop_android_harness_runs.ps1:72-84`.

4. Device-reset fallback even when no host harness match exists:
   - If no matching host harness process is found, the script still calls
     `Stop-AndroidPackages -Devices (Get-TargetDevices ...)` before exiting at
     `scripts/stop_android_harness_runs.ps1:113-120`.
   - That means attached adb-visible devices can have Senku package state
     force-stopped and package PIDs killed even when the host-side runner
     search returns empty.

## Conclusion

In the current checkout, `scripts/stop_android_harness_runs.ps1` belongs to a
mixed process-control / device-reset branch, not the narrower harness-orphan
helper tranche covered by D31. Treating it as a routine host-only tracking
candidate would be misleading.

## Recommended next step

Run a dedicated future allow/repair/retire slice for this mixed
host-stop/device-reset surface. Keep
`scripts/stop_android_device_processes_safe.ps1` and
`scripts/cleanup_android_harness_artifacts.ps1` as separate follow-up
surfaces rather than collapsing them into this note.
