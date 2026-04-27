param(
    [string]$OutputDir = "artifacts\bench\android_fts_fallback_matrix",
    [string[]]$Devices = @("emulator-5554", "emulator-5556", "emulator-5558", "emulator-5560"),
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
$instrumentationClass = "com.senku.mobile.PackRepositoryFtsFallbackAndroidTest"
$instrumentationRunner = "com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner"
$expectedTests = 3
$runtimeEvidence = "fts4_fallback"

if (Test-Path -LiteralPath $commonHarnessModule) {
    Import-Module $commonHarnessModule -Force -DisableNameChecking
    $Devices = @(Resolve-AndroidHarnessDeviceList -Devices $Devices)
} else {
    $normalizedDevices = New-Object System.Collections.Generic.List[string]
    foreach ($deviceEntry in @($Devices)) {
        foreach ($split in ([string]$deviceEntry -split ",")) {
            $trimmed = $split.Trim()
            if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
                $normalizedDevices.Add($trimmed)
            }
        }
    }
    $Devices = @($normalizedDevices.ToArray())
}

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function New-DeviceFileName {
    param([string]$Device)

    $slug = ([string]$Device).ToLowerInvariant() -replace "[^a-z0-9._-]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        $slug = "device"
    }
    return "$slug.json"
}

function Invoke-FtsFallbackInstrumentation {
    param(
        [string]$Device,
        [switch]$DryRun
    )

    $adbArgs = @(
        "-s", $Device,
        "shell", "am", "instrument", "-w",
        "-e", "class", $instrumentationClass,
        $instrumentationRunner
    )

    if ($DryRun) {
        return [pscustomobject]@{
            exit_code = 0
            stdout = "DRY RUN: adb $($adbArgs -join ' ')"
            stderr = ""
            command = "adb $($adbArgs -join ' ')"
        }
    }

    $output = & adb @adbArgs 2>&1
    $exitCode = $LASTEXITCODE
    return [pscustomobject]@{
        exit_code = $exitCode
        stdout = ($output -join "`n")
        stderr = ""
        command = "adb $($adbArgs -join ' ')"
    }
}

function Test-InstrumentationPassed {
    param($Result)

    if ([int]$Result.exit_code -ne 0) {
        return $false
    }

    $stdout = [string]$Result.stdout
    if ($stdout -match "FAILURES!!!" -or $stdout -match "Failure in") {
        return $false
    }

    return ($stdout -match "OK \(\d+ tests?\)" -or $stdout -match "DRY RUN:")
}

function Write-JsonFile {
    param(
        [string]$Path,
        $Value
    )

    $Value | ConvertTo-Json -Depth 8 | Set-Content -Path $Path -Encoding UTF8
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Path $resolvedOutputDir -Force | Out-Null

$deviceResults = @()
foreach ($device in @($Devices)) {
    $startedAt = (Get-Date).ToUniversalTime()
    $runResult = Invoke-FtsFallbackInstrumentation -Device $device -DryRun:$DryRun
    $finishedAt = (Get-Date).ToUniversalTime()
    $passed = Test-InstrumentationPassed -Result $runResult

    $deviceRecord = [pscustomobject]@{
        device = $device
        status = $(if ($passed) { "passed" } else { "failed" })
        passed = [bool]$passed
        expected_tests = $expectedTests
        runtime_evidence = $runtimeEvidence
        dry_run = [bool]$DryRun
        started_at_utc = $startedAt.ToString("o")
        finished_at_utc = $finishedAt.ToString("o")
        command = $runResult.command
        exit_code = [int]$runResult.exit_code
        stdout = [string]$runResult.stdout
        stderr = [string]$runResult.stderr
    }

    $deviceJsonPath = Join-Path $resolvedOutputDir (New-DeviceFileName -Device $device)
    Write-JsonFile -Path $deviceJsonPath -Value $deviceRecord
    $deviceResults += $deviceRecord
}

$failedDevices = @($deviceResults | Where-Object { -not $_.passed } | ForEach-Object { $_.device })
$summary = [pscustomobject]@{
    passed_count = @($deviceResults | Where-Object { $_.passed }).Count
    failed_devices = $failedDevices
    expected_tests = $expectedTests
    runtime_evidence = $runtimeEvidence
    dry_run = [bool]$DryRun
    device_count = @($deviceResults).Count
    devices = @($deviceResults | ForEach-Object { $_.device })
    results = $deviceResults
}

$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
Write-JsonFile -Path $summaryJsonPath -Value $summary
Write-Host "Wrote Android FTS fallback matrix summary: $summaryJsonPath"

if ($failedDevices.Count -gt 0) {
    exit 1
}
