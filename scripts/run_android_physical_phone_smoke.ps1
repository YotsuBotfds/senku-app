param(
    [string]$OutputDir = "artifacts\bench\android_physical_phone_smoke",
    [switch]$DryRun,
    [string]$Serial,
    [string]$ApkPath = "android-app\app\build\outputs\apk\debug\app-debug.apk",
    [string]$AdbPath,
    [string]$FocusPath,
    [string]$ScreenshotPath,
    [string]$DumpPath,
    [string]$LogcatPath
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$packageName = "com.senku.mobile"
$launchActivity = "com.senku.mobile/.MainActivity"

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

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

function Get-DefaultAdbPath {
    if ([string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        throw "LOCALAPPDATA is not set; pass -AdbPath explicitly."
    }

    return (Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe")
}

function Assert-PhysicalSerial {
    param([string]$DeviceSerial)

    if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
        throw "Serial is required unless -DryRun is set. Pass -Serial for the attached physical phone."
    }

    if ($DeviceSerial -match '^emulator-\d+$') {
        throw "Refusing emulator serial '$DeviceSerial'. This runner only targets a physical phone."
    }
}

function Invoke-AdbCapture {
    param(
        [string]$ResolvedAdbPath,
        [string[]]$Arguments
    )

    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $output = & $ResolvedAdbPath @Arguments 2>&1
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $text = ($output | ForEach-Object { "$_" }) -join [Environment]::NewLine
    if ($exitCode -ne 0) {
        throw "adb command failed ($exitCode): adb $($Arguments -join ' ')`n$text"
    }
    return $text
}

function Invoke-AdbChecked {
    param(
        [string]$ResolvedAdbPath,
        [string[]]$Arguments
    )

    Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments $Arguments | Out-Null
}

function Get-AdbDeviceMap {
    param([string]$ResolvedAdbPath)

    $devicesText = Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("devices")
    $devices = @{}
    foreach ($line in ($devicesText -split "`r?`n")) {
        if ($line -match '^(?<serial>\S+)\s+(?<state>device|offline|unauthorized|recovery|sideload|no permissions)') {
            $devices[$Matches["serial"]] = $Matches["state"]
        }
    }
    return $devices
}

function Assert-SerialIsConnectedPhysicalDevice {
    param(
        [string]$ResolvedAdbPath,
        [string]$DeviceSerial
    )

    $devices = Get-AdbDeviceMap -ResolvedAdbPath $ResolvedAdbPath
    if (-not $devices.ContainsKey($DeviceSerial)) {
        throw "Serial '$DeviceSerial' is not present in adb devices output."
    }

    if ($devices[$DeviceSerial] -ne "device") {
        throw "Serial '$DeviceSerial' is connected as '$($devices[$DeviceSerial])', not 'device'."
    }

    $qemu = Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "getprop", "ro.kernel.qemu")
    $bootQemu = Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "getprop", "ro.boot.qemu")
    if (($qemu.Trim() -eq "1") -or ($bootQemu.Trim() -eq "1")) {
        throw "Serial '$DeviceSerial' resolves to an emulator/qemu device. Refusing to run physical-phone smoke."
    }
}

function Write-Utf8Text {
    param(
        [string]$Path,
        [string]$Content
    )

    $parent = Split-Path -Parent $Path
    if (-not [string]::IsNullOrWhiteSpace($parent)) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    Set-Content -Path $Path -Value $Content -Encoding UTF8
}

function Write-JsonFile {
    param(
        [string]$Path,
        $Value
    )

    $Value | ConvertTo-Json -Depth 8 | Set-Content -Path $Path -Encoding UTF8
}

function Write-SummaryMarkdown {
    param(
        [string]$Path,
        $Summary
    )

    $lines = @(
        "# Android Physical Phone Smoke",
        "",
        "- status: $($Summary.status)",
        "- dry_run: $($Summary.dry_run)",
        "- physical_device: $($Summary.physical_device)",
        "- launches_emulators: $($Summary.launches_emulators)",
        "- serial: $($Summary.serial)",
        "- adb_path: ``$($Summary.adb_path)``",
        "- apk_path: ``$($Summary.apk_path)``",
        "- package: $($Summary.package)",
        "- launch_activity: $($Summary.launch_activity)",
        "- install_command: ``$($Summary.commands.install)``",
        "- launch_command: ``$($Summary.commands.launch)``",
        "- focus_path: ``$($Summary.evidence.focus_path)``",
        "- screenshot_path: ``$($Summary.evidence.screenshot_path)``",
        "- dump_path: ``$($Summary.evidence.dump_path)``",
        "- logcat_path: ``$($Summary.evidence.logcat_path)``",
        "- summary_json: ``$($Summary.summary_json)``"
    )
    $lines | Set-Content -Path $Path -Encoding UTF8
}

function Test-FocusContainsLaunchActivity {
    param([string]$Text)

    if ([string]::IsNullOrWhiteSpace($Text)) {
        return $false
    }

    return $Text -match "mCurrentFocus=.*com\.senku\.mobile" `
        -or $Text -match "mFocusedApp=.*com\.senku\.mobile" `
        -or $Text -match "topResumedActivity=.*com\.senku\.mobile" `
        -or $Text -match "ResumedActivity:.*com\.senku\.mobile"
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$resolvedAdbPath = if ([string]::IsNullOrWhiteSpace($AdbPath)) { Get-DefaultAdbPath } else { Resolve-TargetPath -Path $AdbPath }
$resolvedApkPath = Resolve-TargetPath -Path $ApkPath
$resolvedFocusPath = if ([string]::IsNullOrWhiteSpace($FocusPath)) { Join-Path $resolvedOutputDir "focus.txt" } else { Resolve-TargetPath -Path $FocusPath }
$resolvedScreenshotPath = if ([string]::IsNullOrWhiteSpace($ScreenshotPath)) { $null } else { Resolve-TargetPath -Path $ScreenshotPath }
$resolvedDumpPath = if ([string]::IsNullOrWhiteSpace($DumpPath)) { $null } else { Resolve-TargetPath -Path $DumpPath }
$resolvedLogcatPath = if ([string]::IsNullOrWhiteSpace($LogcatPath)) { $null } else { Resolve-TargetPath -Path $LogcatPath }

$installCommandText = "adb -s $Serial install --no-streaming -r `"$resolvedApkPath`""
$launchCommandText = "adb -s $Serial shell am start -n $launchActivity"
$startedAt = (Get-Date).ToUniversalTime()
$status = "dry_run_only"
$focusText = $null

if (-not $DryRun) {
    Assert-PhysicalSerial -DeviceSerial $Serial
    if (-not (Test-Path -LiteralPath $resolvedAdbPath)) {
        throw "adb not found at $resolvedAdbPath"
    }
    if (-not (Test-Path -LiteralPath $resolvedApkPath)) {
        throw "APK not found at $resolvedApkPath"
    }

    Assert-SerialIsConnectedPhysicalDevice -ResolvedAdbPath $resolvedAdbPath -DeviceSerial $Serial
    Invoke-AdbChecked -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "install", "--no-streaming", "-r", $resolvedApkPath)
    Invoke-AdbChecked -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "shell", "am", "start", "-n", $launchActivity)
    Start-Sleep -Seconds 2

    $windowFocus = Invoke-AdbCapture -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "shell", "dumpsys", "window", "windows")
    $topActivity = Invoke-AdbCapture -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "shell", "dumpsys", "activity", "activities")
    $focusText = @(
        "## dumpsys window windows",
        $windowFocus,
        "",
        "## dumpsys activity activities",
        $topActivity
    ) -join [Environment]::NewLine
    Write-Utf8Text -Path $resolvedFocusPath -Content $focusText
    $focusContainsLaunchActivity = Test-FocusContainsLaunchActivity -Text $focusText
    if (-not $focusContainsLaunchActivity) {
        throw "Physical phone smoke launched '$launchActivity' but focus evidence did not show '$packageName' resumed. See $resolvedFocusPath"
    }

    if ($null -ne $resolvedScreenshotPath) {
        $screenshotParent = Split-Path -Parent $resolvedScreenshotPath
        if (-not [string]::IsNullOrWhiteSpace($screenshotParent)) {
            New-Item -ItemType Directory -Force -Path $screenshotParent | Out-Null
        }
        & $resolvedAdbPath "-s" $Serial "exec-out" "screencap" "-p" > $resolvedScreenshotPath
        if ($LASTEXITCODE -ne 0) {
            throw "adb screenshot capture failed for serial '$Serial'."
        }
    }

    if ($null -ne $resolvedDumpPath) {
        $dumpText = Invoke-AdbCapture -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "shell", "uiautomator", "dump", "/dev/tty")
        Write-Utf8Text -Path $resolvedDumpPath -Content $dumpText
    }

    if ($null -ne $resolvedLogcatPath) {
        $logcatText = Invoke-AdbCapture -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "logcat", "-d", "-v", "time")
        Write-Utf8Text -Path $resolvedLogcatPath -Content $logcatText
    }

    $status = "completed"
}

$finishedAt = (Get-Date).ToUniversalTime()
$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"

$summary = [pscustomobject]@{
    status = $status
    dry_run = [bool]$DryRun
    physical_device = $true
    launches_emulators = $false
    serial_required_unless_dry_run = $true
    serial = if ([string]::IsNullOrWhiteSpace($Serial)) { $null } else { $Serial }
    adb_path = (Convert-ToRepoRelativePath -Path $resolvedAdbPath)
    adb_path_source = if ([string]::IsNullOrWhiteSpace($AdbPath)) { "LOCALAPPDATA_ANDROID_SDK" } else { "override" }
    apk_path = (Convert-ToRepoRelativePath -Path $resolvedApkPath)
    package = $packageName
    launch_activity = $launchActivity
    evidence = [ordered]@{
        focus_path = (Convert-ToRepoRelativePath -Path $resolvedFocusPath)
        screenshot_path = (Convert-ToRepoRelativePath -Path $resolvedScreenshotPath)
        dump_path = (Convert-ToRepoRelativePath -Path $resolvedDumpPath)
        logcat_path = (Convert-ToRepoRelativePath -Path $resolvedLogcatPath)
        focus_contains_launch_activity = if ($null -eq $focusText) { $null } else { Test-FocusContainsLaunchActivity -Text $focusText }
    }
    commands = [ordered]@{
        devices = "adb devices"
        install = $installCommandText
        launch = $launchCommandText
        focus = "adb -s $Serial shell dumpsys window windows; adb -s $Serial shell dumpsys activity activities"
        screenshot = if ($null -eq $resolvedScreenshotPath) { $null } else { "adb -s $Serial exec-out screencap -p > `"$resolvedScreenshotPath`"" }
        dump = if ($null -eq $resolvedDumpPath) { $null } else { "adb -s $Serial shell uiautomator dump /dev/tty" }
        logcat = if ($null -eq $resolvedLogcatPath) { $null } else { "adb -s $Serial logcat -d -v time" }
    }
    summary_json = (Convert-ToRepoRelativePath -Path $summaryJsonPath)
    summary_markdown = (Convert-ToRepoRelativePath -Path $summaryMarkdownPath)
    started_at_utc = $startedAt.ToString("o")
    finished_at_utc = $finishedAt.ToString("o")
}

Write-JsonFile -Path $summaryJsonPath -Value $summary
Write-SummaryMarkdown -Path $summaryMarkdownPath -Summary $summary

Write-Host "Wrote Android physical phone smoke summary: $summaryJsonPath"
if ($DryRun) {
    Write-Host "Dry run only. Planned install command: $installCommandText"
}
