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
if (-not (Test-Path -LiteralPath $smokeScript -PathType Leaf)) {
    throw "Missing smoke script: $smokeScript"
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

foreach ($preset in $presets) {
    $presetRoot = Join-Path $matrixRoot $preset
    $presetRootForRunner = Convert-ToRepoRelativePath -Path $presetRoot
    $runSummaryPath = Join-Path $presetRoot "run_summary.json"
    $captureSummaryPath = Join-Path $presetRoot "capture_summary.json"
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
    if ($SkipBuild -or $canReuseInstalledApks) {
        $arguments += "-SkipBuild"
    }
    if ($SkipInstall -or $canReuseInstalledApks) {
        $arguments += "-SkipInstall"
    }
    if ($SkipDeviceLock) {
        $arguments += "-SkipDeviceLock"
    }

    Write-Host ("[functional-ux-matrix:{0}] starting {1}" -f $Device, $preset)
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

    Write-Host ("[functional-ux-matrix:{0}] finished {1} exit_code={2} duration_seconds={3}" -f $Device, $preset, $exitCode, $durationSeconds)
    if ($passed) {
        $canReuseInstalledApks = $true
    }
    if ($FailFast -and -not $passed) {
        break
    }
}

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
    presets = $results
    passed = -not $anyFailed
}
$summary | ConvertTo-Json -Depth 8 | Set-Content -Path $matrixSummaryPath -Encoding UTF8
Write-Host ("[functional-ux-matrix:{0}] wrote {1}" -f $Device, (Convert-ToRepoRelativePath -Path $matrixSummaryPath))

if ($anyFailed) {
    exit 1
}
exit 0
