# Local-only matrix wrapper for the functional UX smoke presets. This script
# intentionally delegates to run_android_instrumented_ui_smoke.ps1 and does not
# require GitHub CI; run with -Help for examples and parameter notes.
param(
    [string]$Device = "emulator-5554",
    [switch]$PhysicalDevice,
    [string]$OutputLabel = "",
    [string]$ArtifactRoot = "artifacts/android_functional_ux_smoke_matrix",
    [switch]$CaptureLogcat,
    [switch]$ClearLogcatBeforeRun,
    [switch]$SkipBuild,
    [switch]$SkipInstall,
    [switch]$SkipDeviceLock,
    [switch]$FailFast,
    [switch]$Help
)

$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

function Show-Usage {
    @"
Functional UX smoke matrix wrapper.

Runs the existing run_android_instrumented_ui_smoke.ps1 presets used by the
release-scout functional UX check:
  - phone-functional
  - phone-functional-follow-up
  - phone-functional-saved
  - phone-functional-back-provenance

Usage:
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_functional_ux_smoke_matrix.ps1 -Device emulator-5554 -CaptureLogcat -ClearLogcatBeforeRun

Physical phone example:
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_functional_ux_smoke_matrix.ps1 -Device RFCX607ZM8L -PhysicalDevice -OutputLabel physical_current_head -CaptureLogcat

Parameters:
  -Device                 ADB serial to pass through to the smoke runner.
  -PhysicalDevice         Refuses emulator-* serials; use for attached phones.
  -OutputLabel            Subdirectory label under -ArtifactRoot. Defaults to a timestamp.
  -ArtifactRoot           Matrix output root. Default: artifacts/android_functional_ux_smoke_matrix.
  -CaptureLogcat          Accepted for compatibility; logcat is always captured because capture summaries require it.
  -ClearLogcatBeforeRun   Passes -ClearLogcatBeforeRun to each preset run.
  -SkipBuild              Passes -SkipBuild to each preset run.
  -SkipInstall            Passes -SkipInstall to each preset run.
  -SkipDeviceLock         Passes -SkipDeviceLock to each preset run.
  -FailFast               Stops after the first failing preset.
  -Help                   Prints this usage text without touching adb or Gradle.

Outputs:
  Each preset writes to <ArtifactRoot>/<OutputLabel>/<preset>/.
  This wrapper writes <ArtifactRoot>/<OutputLabel>/matrix_summary.json.
"@ | Write-Host
}

if ($Help) {
    Show-Usage
    exit 0
}

if ([string]::IsNullOrWhiteSpace($Device)) {
    throw "-Device must not be empty."
}
if ($PhysicalDevice -and $Device -like "emulator-*") {
    throw "-PhysicalDevice was set, but '$Device' looks like an emulator serial."
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$smokeScript = Join-Path $PSScriptRoot "run_android_instrumented_ui_smoke.ps1"
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
$lockRoot = Join-Path $repoRoot "artifacts\harness_locks"
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
if (-not (Test-Path -LiteralPath $smokeScript -PathType Leaf)) {
    throw "Missing smoke script: $smokeScript"
}
if (-not (Test-Path -LiteralPath $adb -PathType Leaf)) {
    throw "adb not found at $adb"
}
if (-not (Test-Path -LiteralPath $commonHarnessModule -PathType Leaf)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}
Import-Module $commonHarnessModule -Force -DisableNameChecking
New-Item -ItemType Directory -Force -Path $lockRoot | Out-Null

function Acquire-MatrixDeviceLock {
    param(
        [string]$DeviceName,
        [int]$TimeoutSeconds = 900
    )

    return Acquire-AndroidHarnessDeviceLock -DeviceName $DeviceName -LockRoot $lockRoot -TimeoutSeconds $TimeoutSeconds -ProgressLabel ("[functional-ux-matrix:{0}]" -f $DeviceName)
}

function Assert-FunctionalUxPhoneDevice {
    Write-Host ("[functional-ux-matrix:{0}] preflighting phone-preset device role" -f $Device)
    & $adb -s $Device wait-for-device
    if ($LASTEXITCODE -ne 0) {
        throw "adb wait-for-device failed for $Device"
    }
    $facts = Resolve-AndroidDeviceFacts -AdbPath $adb -DeviceName $Device -RequestedOrientation "portrait"
    $role = if ($null -ne $facts -and -not [string]::IsNullOrWhiteSpace([string]$facts.resolved_role)) {
        [string]$facts.resolved_role
    } else {
        "unknown"
    }
    if ($role -ne "phone") {
        $smallest = if ($null -ne $facts -and $null -ne $facts.smallest_width_dp) {
            [string]$facts.smallest_width_dp
        } else {
            "unknown"
        }
        throw ("Functional UX smoke matrix runs phone-* presets, but {0} resolved as {1} (smallest_width_dp={2}). Use a phone-class device such as emulator-5556 or the attached physical phone, or run a tablet-specific state-pack lane." -f $Device, $role, $smallest)
    }
}

if ([string]::IsNullOrWhiteSpace($OutputLabel)) {
    $OutputLabel = "functional_ux_{0}" -f (Get-Date -Format "yyyyMMdd_HHmmss")
}

$matrixRoot = if ([System.IO.Path]::IsPathRooted($ArtifactRoot)) {
    Join-Path $ArtifactRoot $OutputLabel
} else {
    Join-Path (Join-Path $repoRoot $ArtifactRoot) $OutputLabel
}
New-Item -ItemType Directory -Force -Path $matrixRoot | Out-Null

$presets = @(
    "phone-functional",
    "phone-functional-follow-up",
    "phone-functional-saved",
    "phone-functional-back-provenance"
)

function Convert-ToRepoRelativePath {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }

    $resolvedPath = [System.IO.Path]::GetFullPath($Path)
    $resolvedRoot = [System.IO.Path]::GetFullPath($repoRoot)
    if ($resolvedPath.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $resolvedPath.Substring($resolvedRoot.Length).TrimStart("\", "/") -replace "\\", "/"
    }
    return $resolvedPath
}

function Get-PowerShellExecutable {
    $currentProcess = Get-Process -Id $PID
    if ($currentProcess.Path -and (Test-Path -LiteralPath $currentProcess.Path -PathType Leaf)) {
        return $currentProcess.Path
    }
    return "powershell"
}

$powerShellExe = Get-PowerShellExecutable
$results = New-Object System.Collections.Generic.List[object]
$anyFailed = $false
$canReuseInstalledApks = $false
$matrixDeviceLock = $null
$matrixDeviceLockUsed = $false
$matrixStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

function Read-JsonFileOrNull {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        return $null
    }
    try {
        return Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
    } catch {
        return $null
    }
}

try {
if (-not $SkipDeviceLock) {
    Write-Host ("[functional-ux-matrix:{0}] acquiring matrix device lock for {1} preset(s)" -f $Device, $presets.Count)
    $matrixDeviceLock = Acquire-MatrixDeviceLock -DeviceName $Device
    $matrixDeviceLockUsed = $true
    Write-Host ("[functional-ux-matrix:{0}] matrix device lock acquired; child smoke runs will skip nested lock acquisition" -f $Device)
} else {
    Write-Host ("[functional-ux-matrix:{0}] matrix device lock skipped; child smoke runs will also skip device locks" -f $Device)
}

Assert-FunctionalUxPhoneDevice

for ($presetIndex = 0; $presetIndex -lt $presets.Count; $presetIndex++) {
    $preset = $presets[$presetIndex]
    $presetOrdinal = $presetIndex + 1
    $presetRoot = Join-Path $matrixRoot $preset
    $presetRootForRunner = Convert-ToRepoRelativePath -Path $presetRoot
    $runSummaryPath = Join-Path $presetRoot "run_summary.json"
    $captureSummaryPath = Join-Path $presetRoot "capture_summary.json"
    $reuseInstalledApksForPreset = [bool]$canReuseInstalledApks
    $effectiveSkipBuild = [bool]($SkipBuild -or $reuseInstalledApksForPreset)
    $effectiveSkipInstall = [bool]($SkipInstall -or $reuseInstalledApksForPreset)
    New-Item -ItemType Directory -Force -Path $presetRoot | Out-Null

    $arguments = @(
        "-NoProfile",
        "-ExecutionPolicy",
        "Bypass",
        "-File",
        $smokeScript,
        "-Device",
        $Device,
        "-SmokePreset",
        $preset,
        "-ArtifactRoot",
        $presetRootForRunner,
        "-SummaryPath",
        $runSummaryPath,
        "-CaptureSummaryPath",
        $captureSummaryPath,
        "-CaptureLogcat"
    )
    if ($ClearLogcatBeforeRun) {
        $arguments += "-ClearLogcatBeforeRun"
    }
    if ($effectiveSkipBuild) {
        $arguments += "-SkipBuild"
    }
    if ($effectiveSkipInstall) {
        $arguments += "-SkipInstall"
    }
    if ($SkipDeviceLock -or $matrixDeviceLockUsed) {
        $arguments += "-SkipDeviceLock"
    }

    Write-Host ("[functional-ux-matrix:{0}] starting {1} ({2}/{3}); matrix_elapsed_seconds={4}; artifact_root={5}" -f $Device, $preset, $presetOrdinal, $presets.Count, [Math]::Round($matrixStopwatch.Elapsed.TotalSeconds, 1), $presetRootForRunner)
    $startedAt = Get-Date
    & $powerShellExe @arguments
    $exitCode = $LASTEXITCODE
    $endedAt = Get-Date
    $durationSeconds = [Math]::Round(($endedAt - $startedAt).TotalSeconds, 3)
    $passed = ($exitCode -eq 0)
    if (-not $passed) {
        $anyFailed = $true
    }
    $runSummary = Read-JsonFileOrNull -Path $runSummaryPath

    $results.Add([ordered]@{
        preset = $preset
        exit_code = $exitCode
        passed = $passed
        duration_seconds = $durationSeconds
        artifact_root = Convert-ToRepoRelativePath -Path $presetRoot
        run_summary_path = Convert-ToRepoRelativePath -Path $runSummaryPath
        capture_summary_path = Convert-ToRepoRelativePath -Path $captureSummaryPath
        skip_build_requested = [bool]$SkipBuild
        skip_install_requested = [bool]$SkipInstall
        reused_installed_apks = [bool]$reuseInstalledApksForPreset
        effective_skip_build = [bool]$effectiveSkipBuild
        effective_skip_install = [bool]$effectiveSkipInstall
        status = $(if ($null -ne $runSummary) { $runSummary.status } else { $null })
        failure_reason = $(if ($null -ne $runSummary) { $runSummary.failure_reason } else { $null })
        selected_test_methods = $(if ($null -ne $runSummary) { @($runSummary.selected_test_methods) } else { @() })
        screenshot_count = $(if ($null -ne $runSummary) { $runSummary.screenshot_count } else { $null })
        dump_count = $(if ($null -ne $runSummary) { $runSummary.dump_count } else { $null })
        artifact_dir = $(if ($null -ne $runSummary) { Convert-ToRepoRelativePath -Path ([string]$runSummary.artifact_dir) } else { $null })
        artifact_bundle_zip = $(if ($null -ne $runSummary) { Convert-ToRepoRelativePath -Path ([string]$runSummary.artifact_bundle_zip) } else { $null })
        instrumentation_log = $(if ($null -ne $runSummary) { Convert-ToRepoRelativePath -Path ([string]$runSummary.instrumentation_log) } else { $null })
        artifact_expectations_met = $(if ($null -ne $runSummary) { $runSummary.artifact_expectations_met } else { $null })
    }) | Out-Null

    Write-Host ("[functional-ux-matrix:{0}] finished {1} ({2}/{3}) exit_code={4} duration_seconds={5} matrix_elapsed_seconds={6}" -f $Device, $preset, $presetOrdinal, $presets.Count, $exitCode, $durationSeconds, [Math]::Round($matrixStopwatch.Elapsed.TotalSeconds, 1))
    if ($passed) {
        $canReuseInstalledApks = $true
    }
    if ($FailFast -and -not $passed) {
        Write-Host ("[functional-ux-matrix:{0}] FailFast stopping after {1}/{2} preset(s); matrix_summary.json will include completed results" -f $Device, $presetOrdinal, $presets.Count)
        break
    }
}

$matrixStopwatch.Stop()
$matrixSummaryPath = Join-Path $matrixRoot "matrix_summary.json"
$summary = [ordered]@{
    source = "run_android_functional_ux_smoke_matrix.ps1"
    generated_at = (Get-Date).ToString("o")
    device = $Device
    physical_device_mode = [bool]$PhysicalDevice
    output_label = $OutputLabel
    matrix_root = Convert-ToRepoRelativePath -Path $matrixRoot
    capture_logcat = $true
    capture_logcat_requested = [bool]$CaptureLogcat
    clear_logcat_before_run = [bool]$ClearLogcatBeforeRun
    device_lock_used = [bool]$matrixDeviceLockUsed
    device_lock_posture = $(if ($matrixDeviceLockUsed) { "matrix_lock_children_skip_nested" } else { "skipped" })
    fail_fast = [bool]$FailFast
    stopped_early = [bool]($FailFast -and $anyFailed -and $results.Count -lt $presets.Count)
    completed_preset_count = [int]$results.Count
    total_preset_count = [int]$presets.Count
    duration_seconds = [Math]::Round($matrixStopwatch.Elapsed.TotalSeconds, 3)
    presets = $results
    passed = -not $anyFailed
}
$summary | ConvertTo-Json -Depth 8 | Set-Content -Path $matrixSummaryPath -Encoding UTF8
Write-Host ("[functional-ux-matrix:{0}] wrote {1}" -f $Device, (Convert-ToRepoRelativePath -Path $matrixSummaryPath))
} finally {
    if ($matrixDeviceLock) {
        $matrixDeviceLock.Dispose()
    }
}

if ($anyFailed) {
    exit 1
}
exit 0
