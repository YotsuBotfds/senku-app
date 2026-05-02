param(
    [string]$OutputDir = "artifacts\bench\android_route_smoke",
    [string[]]$Devices = @("emulator-5554"),
    [switch]$DryRun,
    [switch]$SkipBuild,
    [switch]$SkipInstall,
    [switch]$SkipDeviceLock,
    [string]$AdbPath = "",
    [int]$InstallTimeoutMilliseconds = 180000,
    [int]$InstrumentationTimeoutMilliseconds = 300000
)

$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$androidRoot = Join-Path $repoRoot "android-app"
$gradlew = Join-Path $androidRoot "gradlew.bat"
$adb = if ([string]::IsNullOrWhiteSpace($AdbPath)) {
    Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
} else {
    $AdbPath
}
$lockRoot = Join-Path $repoRoot "artifacts\harness_locks"
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
$instrumentationClass = "com.senku.mobile.PackRepositoryCurrentHeadRouteSmokeAndroidTest"
$instrumentationRunner = "com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner"
$expectedTests = 1

if (-not (Test-Path -LiteralPath $commonHarnessModule)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}
Import-Module $commonHarnessModule -Force -DisableNameChecking
$Devices = @(Resolve-AndroidHarnessDeviceList -Devices $Devices)
if (@($Devices).Count -eq 0) {
    throw "No Android devices resolved. Provide at least one non-empty device via -Devices."
}
if (-not $DryRun) {
    if (-not (Test-Path -LiteralPath $gradlew)) {
        throw "gradlew.bat not found at $gradlew"
    }
    if (-not (Test-Path -LiteralPath $adb)) {
        throw "adb not found at $adb"
    }
}
New-Item -ItemType Directory -Force -Path $lockRoot | Out-Null

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

function Write-JsonFile {
    param(
        [string]$Path,
        $Value
    )

    $Value | ConvertTo-Json -Depth 8 | Set-Content -Path $Path -Encoding UTF8
}

function Write-MarkdownSummary {
    param(
        [string]$Path,
        $Summary,
        [string]$SummaryJsonPath
    )

    $failedCount = @($Summary.failed_devices).Count
    $deviceList = @($Summary.devices) -join ", "
    $failedDeviceList = if ($failedCount -gt 0) { @($Summary.failed_devices) -join ", " } else { "none" }
    $timedOutDeviceList = if (@($Summary.timed_out_devices).Count -gt 0) { @($Summary.timed_out_devices) -join ", " } else { "none" }
    $lines = @(
        "# Android route smoke summary",
        "",
        "- instrumentation_class: $($Summary.instrumentation_class)",
        "- expected_tests: $($Summary.expected_tests)",
        "- devices: $deviceList",
        "- passed_count: $($Summary.passed_count)",
        "- failed_count: $failedCount",
        "- failed_devices: $failedDeviceList",
        "- timed_out_devices: $timedOutDeviceList",
        "- dry_run: $($Summary.dry_run.ToString().ToLowerInvariant())",
        "- build_skipped: $($Summary.build_skipped.ToString().ToLowerInvariant())",
        "- install_skipped: $($Summary.install_skipped.ToString().ToLowerInvariant())",
        "- device_lock_posture: $($Summary.device_lock_posture)",
        "- device_lock_used: $($Summary.device_lock_used.ToString().ToLowerInvariant())",
        "- adb_path: $($Summary.adb_path)",
        "- host_adb_platform_tools_version: $($Summary.host_adb_platform_tools_version)",
        "- summary_json: $SummaryJsonPath"
    )

    $lines | Set-Content -Path $Path -Encoding UTF8
}

function Invoke-RouteSmokeAdb {
    param(
        [string]$Device,
        [string[]]$AdbArgs,
        [switch]$DryRun,
        [int]$TimeoutMilliseconds = 0
    )

    $command = "adb $($AdbArgs -join ' ')"
    if ($DryRun) {
        return [pscustomobject]@{
            exit_code = 0
            timed_out = $false
            stdout = "DRY RUN: $command"
            stderr = ""
            command = $command
        }
    }

    $result = Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $AdbArgs -TimeoutMilliseconds $TimeoutMilliseconds
    return [pscustomobject]@{
        exit_code = [int]$result.exit_code
        timed_out = [bool]$result.timed_out
        stdout = [string]$result.output
        stderr = ""
        command = $command
    }
}

function Test-InstrumentationPassed {
    param($Result)

    if ([int]$Result.exit_code -ne 0) {
        return $false
    }
    if ($Result.PSObject.Properties.Name -contains "timed_out" -and [bool]$Result.timed_out) {
        return $false
    }

    $stdout = [string]$Result.stdout
    if ($stdout -match "FAILURES!!!" -or $stdout -match "Failure in") {
        return $false
    }

    return ($stdout -match "OK \(1 test\)" -or $stdout -match "DRY RUN:")
}

function Acquire-DeviceLock {
    param([string]$Device)

    if ($DryRun -or $SkipDeviceLock) {
        return $null
    }

    return Acquire-AndroidHarnessDeviceLock -DeviceName $Device -LockRoot $lockRoot -TimeoutSeconds 900 -ProgressLabel ("[route-smoke:{0}]" -f $Device)
}

function Get-HostAdbPlatformToolsVersion {
    param([switch]$DryRun)

    if ($DryRun) {
        return "dry_run"
    }

    return Get-AndroidHostAdbPlatformToolsVersion -AdbPath $adb
}

function Invoke-GradleBuild {
    param([switch]$DryRun)

    if ($DryRun -or $SkipBuild) {
        return [pscustomobject]@{
            exit_code = 0
            command = "gradlew :app:assembleDebug :app:assembleDebugAndroidTest"
            stdout = $(if ($DryRun) { "DRY RUN: gradlew :app:assembleDebug :app:assembleDebugAndroidTest" } else { "build skipped" })
            stderr = ""
        }
    }

    Push-Location $androidRoot
    try {
        $output = & $gradlew :app:assembleDebug :app:assembleDebugAndroidTest 2>&1
        return [pscustomobject]@{
            exit_code = [int]$LASTEXITCODE
            command = "gradlew :app:assembleDebug :app:assembleDebugAndroidTest"
            stdout = ($output -join "`n")
            stderr = ""
        }
    } finally {
        Pop-Location
    }
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null
$hostAdbPlatformToolsVersion = Get-HostAdbPlatformToolsVersion -DryRun:$DryRun
$deviceLockUsed = [bool](-not $DryRun -and -not $SkipDeviceLock)
$deviceLockPosture = if ($DryRun) {
    "not_acquired_dry_run"
} elseif ($SkipDeviceLock) {
    "skipped_by_flag"
} else {
    "required_per_device"
}

$buildResult = Invoke-GradleBuild -DryRun:$DryRun
if ([int]$buildResult.exit_code -ne 0) {
    throw "Gradle build failed for route smoke"
}

$appApk = Join-Path $androidRoot "app\build\outputs\apk\debug\app-debug.apk"
$testApk = Join-Path $androidRoot "app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk"
$deviceResults = @()
foreach ($device in @($Devices)) {
    $startedAt = (Get-Date).ToUniversalTime()
    $deviceLock = $null
    $installAppResult = $null
    $installTestResult = $null
    $failurePhase = ""
    try {
        $deviceLock = Acquire-DeviceLock -Device $device
        if ($SkipInstall) {
            $installAppResult = [pscustomobject]@{ exit_code = 0; timed_out = $false; stdout = "install skipped"; stderr = ""; command = "install app skipped" }
            $installTestResult = [pscustomobject]@{ exit_code = 0; timed_out = $false; stdout = "install skipped"; stderr = ""; command = "install test skipped" }
        } else {
            $installAppResult = Invoke-RouteSmokeAdb -Device $device -DryRun:$DryRun -TimeoutMilliseconds $InstallTimeoutMilliseconds -AdbArgs @("-s", $device, "install", "-r", $appApk)
            if ([int]$installAppResult.exit_code -ne 0 -or [bool]$installAppResult.timed_out) {
                $failurePhase = "install_app"
                $runResult = $installAppResult
            } else {
                $installTestResult = Invoke-RouteSmokeAdb -Device $device -DryRun:$DryRun -TimeoutMilliseconds $InstallTimeoutMilliseconds -AdbArgs @("-s", $device, "install", "-r", "-t", $testApk)
                if ([int]$installTestResult.exit_code -ne 0 -or [bool]$installTestResult.timed_out) {
                    $failurePhase = "install_test"
                    $runResult = $installTestResult
                }
            }
        }

        if ([string]::IsNullOrWhiteSpace($failurePhase)) {
            $runResult = Invoke-RouteSmokeAdb -Device $device -DryRun:$DryRun -TimeoutMilliseconds $InstrumentationTimeoutMilliseconds -AdbArgs @(
                "-s", $device,
                "shell", "am", "instrument", "-w",
                "-e", "class", $instrumentationClass,
                $instrumentationRunner
            )
            if ([bool]$runResult.timed_out) {
                $failurePhase = "instrumentation_timeout"
            } elseif ([int]$runResult.exit_code -ne 0) {
                $failurePhase = "instrumentation_exit"
            } elseif ([string]$runResult.stdout -match "FAILURES!!!|Failure in") {
                $failurePhase = "instrumentation_failure"
            }
        }
    } catch {
        $failurePhase = "exception"
        $runResult = [pscustomobject]@{
            exit_code = 1
            timed_out = $false
            stdout = ""
            stderr = [string]$_
            command = "route smoke failed before instrumentation"
        }
    } finally {
        if ($deviceLock) {
            $deviceLock.Dispose()
            $deviceLock = $null
        }
    }
    $finishedAt = (Get-Date).ToUniversalTime()
    $passed = Test-InstrumentationPassed -Result $runResult

    $deviceRecord = [pscustomobject]@{
        device = $device
        status = $(if ($passed) { "passed" } else { "failed" })
        passed = [bool]$passed
        expected_tests = $expectedTests
        instrumentation_class = $instrumentationClass
        dry_run = [bool]$DryRun
        build_skipped = [bool]($DryRun -or $SkipBuild)
        install_skipped = [bool]$SkipInstall
        device_lock_posture = $deviceLockPosture
        device_lock_used = $deviceLockUsed
        adb_path = $adb
        host_adb_platform_tools_version = $hostAdbPlatformToolsVersion
        started_at_utc = $startedAt.ToString("o")
        finished_at_utc = $finishedAt.ToString("o")
        install_app_command = $installAppResult.command
        install_test_command = $installTestResult.command
        command = $runResult.command
        exit_code = [int]$runResult.exit_code
        timed_out = [bool]$runResult.timed_out
        failure_phase = $failurePhase
        install_app_timed_out = [bool]$installAppResult.timed_out
        install_test_timed_out = [bool]$installTestResult.timed_out
        stdout = [string]$runResult.stdout
        stderr = [string]$runResult.stderr
    }

    $deviceJsonPath = Join-Path $resolvedOutputDir (New-DeviceFileName -Device $device)
    Write-JsonFile -Path $deviceJsonPath -Value $deviceRecord
    $deviceResults += $deviceRecord
}

$failedDevices = @($deviceResults | Where-Object { -not $_.passed } | ForEach-Object { $_.device })
$failedDeviceResults = @($deviceResults | Where-Object { -not $_.passed } | ForEach-Object {
    [pscustomobject]@{
        device = $_.device
        exit_code = $_.exit_code
        timed_out = $_.timed_out
        failure_phase = $_.failure_phase
        command = $_.command
    }
})
$timedOutDevices = @($deviceResults | Where-Object { $_.timed_out -or $_.install_app_timed_out -or $_.install_test_timed_out } | ForEach-Object { $_.device })
$summary = [pscustomobject]@{
    passed_count = @($deviceResults | Where-Object { $_.passed }).Count
    failed_devices = $failedDevices
    failed_device_results = $failedDeviceResults
    timed_out_devices = $timedOutDevices
    expected_tests = $expectedTests
    instrumentation_class = $instrumentationClass
    dry_run = [bool]$DryRun
    build_skipped = [bool]($DryRun -or $SkipBuild)
    install_skipped = [bool]$SkipInstall
    device_lock_posture = $deviceLockPosture
    device_lock_used = $deviceLockUsed
    adb_path = $adb
    host_adb_platform_tools_version = $hostAdbPlatformToolsVersion
    device_count = @($deviceResults).Count
    devices = @($deviceResults | ForEach-Object { $_.device })
    build = $buildResult
    results = $deviceResults
}

$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"
Write-JsonFile -Path $summaryJsonPath -Value $summary
Write-MarkdownSummary -Path $summaryMarkdownPath -Summary $summary -SummaryJsonPath $summaryJsonPath
Write-Host "Wrote Android route smoke summary: $summaryJsonPath"
Write-Host "Wrote Android route smoke summary markdown: $summaryMarkdownPath"

if ($failedDevices.Count -gt 0) {
    exit 1
}
