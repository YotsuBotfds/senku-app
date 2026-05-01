param(
    [string[]]$Emulators = @(),
    [switch]$IncludePromptRunner
)

$ErrorActionPreference = "Stop"
$adb = Join-Path $env:LOCALAPPDATA "Android\\Sdk\\platform-tools\\adb.exe"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$senkuPackageNames = @(
    "com.senku.mobile.test",
    "com.senku.mobile"
)

function Test-ScopedAndroidDeviceId {
    param([string]$DeviceId)

    if ([string]::IsNullOrWhiteSpace($DeviceId)) {
        return $false
    }

    return $DeviceId.Trim() -match '^(emulator-\d+|[A-Za-z0-9][A-Za-z0-9._:-]{2,127})$'
}

function Assert-ScopedHarnessScriptPatterns {
    param([string[]]$ScriptPatterns)

    foreach ($pattern in $ScriptPatterns) {
        if ([string]::IsNullOrWhiteSpace($pattern)) {
            throw "Android harness stop pattern must not be empty."
        }
        if ($pattern -notmatch '^[A-Za-z0-9_.-]+\.ps1$') {
            throw ("Android harness stop pattern must be a literal .ps1 basename: {0}" -f $pattern)
        }
    }
}

function Matches-TargetEmulator {
    param(
        [string]$CommandLine,
        [string[]]$Targets
    )

    if (-not $Targets -or $Targets.Count -eq 0) {
        return $true
    }

    foreach ($target in $Targets) {
        if ([string]::IsNullOrWhiteSpace($target)) {
            continue
        }
        $escapedTarget = [regex]::Escape($target.Trim())
        $targetPattern = "(?<![A-Za-z0-9_-])" + $escapedTarget + "(?![A-Za-z0-9_-])"
        if ($CommandLine -match $targetPattern) {
            return $true
        }
    }

    return $false
}

function Get-TargetDevices {
    param([string[]]$RequestedDevices)

    $normalized = @()
    $sawRequestedDevice = $false
    foreach ($entry in $RequestedDevices) {
        foreach ($split in ($entry -split ",")) {
            $trimmed = $split.Trim()
            if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
                $sawRequestedDevice = $true
            }
            if (Test-ScopedAndroidDeviceId -DeviceId $trimmed) {
                $normalized += $trimmed
            }
        }
    }
    if ($normalized.Count -gt 0 -or $sawRequestedDevice) {
        return $normalized
    }
    if (-not (Test-Path -LiteralPath $adb)) {
        return @()
    }

    $devices = @()
    $adbOutput = & $adb devices 2>$null
    foreach ($line in $adbOutput) {
        if ($line -match '^\s*([^\s]+)\s+device\s*$') {
            $deviceId = $matches[1]
            if (Test-ScopedAndroidDeviceId -DeviceId $deviceId) {
                $devices += $deviceId
            }
        }
    }
    return $devices
}

function Get-SenkuPackagePidFromPsLine {
    param([string]$Line)

    if ([string]::IsNullOrWhiteSpace($Line)) {
        return $null
    }

    $columns = ($Line.Trim() -split "\s+")
    if ($columns.Count -lt 2) {
        return $null
    }

    $targetPid = $columns[0]
    if ($targetPid -notmatch '^\d+$') {
        return $null
    }

    $processFields = @($columns | Select-Object -Skip 1)
    foreach ($field in $processFields) {
        if ($senkuPackageNames -contains $field) {
            return $targetPid
        }
    }

    return $null
}

function Stop-AndroidPackages {
    param([string[]]$Devices)

    $stoppedDevices = @()
    if (-not (Test-Path -LiteralPath $adb)) {
        return $stoppedDevices
    }

    foreach ($device in $Devices) {
        if (-not (Test-ScopedAndroidDeviceId -DeviceId $device)) {
            Write-Warning ("Skipping unscoped Android device id: {0}" -f $device)
            continue
        }
        try {
            & $adb -s $device shell am force-stop com.senku.mobile.test | Out-Null
            & $adb -s $device shell am force-stop com.senku.mobile | Out-Null
            $psOutput = (& $adb -s $device shell ps -A -o PID,NAME,ARGS 2>$null | Out-String)
            foreach ($line in ($psOutput -split "`r?`n")) {
                $targetPid = Get-SenkuPackagePidFromPsLine -Line $line
                if ($null -ne $targetPid) {
                    & $adb -s $device shell kill -9 $targetPid 2>$null | Out-Null
                }
            }
            $stoppedDevices += $device
        } catch {
        }
    }
    return $stoppedDevices
}

function Matches-HarnessCommand {
    param(
        [string]$CommandLine,
        [string[]]$ScriptPatterns
    )

    if ([string]::IsNullOrWhiteSpace($CommandLine)) {
        return $false
    }

    Assert-ScopedHarnessScriptPatterns -ScriptPatterns $ScriptPatterns

    foreach ($pattern in $ScriptPatterns) {
        $escapedPattern = [regex]::Escape($pattern)
        $scriptPattern = "(?i)(^|[\s`"'])" + $escapedPattern + "($|[\s`"':])"
        if ($CommandLine -match $scriptPattern) {
            return $true
        }

        $pathPattern = "(?i)[\\/]" + $escapedPattern + "($|[\s`"':])"
        if ($CommandLine -match $pathPattern) {
            return $true
        }
    }

    $normalizedCommand = $CommandLine.Replace("/", "\")
    $normalizedRoot = $repoRoot.Replace("/", "\")
    return $normalizedCommand -like ("*" + $normalizedRoot + "*") `
        -and $normalizedCommand -like "*\ui_state_pack*\*" `
        -and $normalizedCommand -like "*.launcher.ps1*"
}

$patterns = @(
    "build_android_ui_state_pack.ps1",
    "build_android_ui_state_pack_parallel.ps1",
    "run_android_detail_followup.ps1",
    "run_android_detail_followup_logged.ps1",
    "run_android_followup_matrix.ps1",
    "run_android_fts_fallback_matrix.ps1",
    "run_android_harness_matrix.ps1",
    "run_android_instrumented_ui_smoke.ps1",
    "run_android_ui_validation_pack.ps1"
)
if ($IncludePromptRunner) {
    $patterns += "run_android_prompt.ps1"
}

Assert-ScopedHarnessScriptPatterns -ScriptPatterns $patterns

$targets = Get-CimInstance Win32_Process | Where-Object {
    $commandLine = $_.CommandLine
    $_.Name -eq "powershell.exe" `
        -and $_.ProcessId -ne $PID `
        -and $commandLine `
        -and (Matches-TargetEmulator -CommandLine $commandLine -Targets $Emulators) `
        -and (Matches-HarnessCommand -CommandLine $commandLine -ScriptPatterns $patterns)
}

if (-not $targets) {
    $stoppedDevices = Stop-AndroidPackages -Devices (Get-TargetDevices -RequestedDevices $Emulators)
    if ($stoppedDevices.Count -gt 0) {
        Write-Host ("No matching Android harness processes found. Force-stopped Senku packages on: " + ($stoppedDevices -join ", "))
        exit 0
    }
    Write-Host "No matching Android harness processes found."
    exit 0
}

$stopped = @()
foreach ($target in $targets) {
    try {
        Stop-Process -Id $target.ProcessId -Force -ErrorAction Stop
        $stopped += [pscustomobject]@{
            ProcessId = $target.ProcessId
            CommandLine = $target.CommandLine
        }
    } catch {
        Write-Warning ("Failed to stop PID " + $target.ProcessId + ": " + $_.Exception.Message)
    }
}

if (-not $stopped) {
    Write-Warning "No Android harness processes were stopped."
    exit 1
}

$stopped | Format-Table -AutoSize
Write-Host ("Stopped " + $stopped.Count + " Android harness process(es).")
$stoppedDevices = Stop-AndroidPackages -Devices (Get-TargetDevices -RequestedDevices $Emulators)
if ($stoppedDevices.Count -gt 0) {
    Write-Host ("Force-stopped Senku packages on: " + ($stoppedDevices -join ", "))
}
