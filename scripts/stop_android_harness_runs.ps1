param(
    [string[]]$Emulators = @(),
    [switch]$IncludePromptRunner
)

$ErrorActionPreference = "Stop"
$adb = Join-Path $env:LOCALAPPDATA "Android\\Sdk\\platform-tools\\adb.exe"

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
        if ($CommandLine -like ("*" + $target + "*")) {
            return $true
        }
    }

    return $false
}

function Get-TargetDevices {
    param([string[]]$RequestedDevices)

    $normalized = @()
    foreach ($entry in $RequestedDevices) {
        foreach ($split in ($entry -split ",")) {
            $trimmed = $split.Trim()
            if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
                $normalized += $trimmed
            }
        }
    }
    if ($normalized.Count -gt 0) {
        return $normalized
    }
    if (-not (Test-Path -LiteralPath $adb)) {
        return @()
    }

    $devices = @()
    $adbOutput = & $adb devices 2>$null
    foreach ($line in $adbOutput) {
        if ($line -match '^\s*([^\s]+)\s+device\s*$') {
            $devices += $matches[1]
        }
    }
    return $devices
}

function Stop-AndroidPackages {
    param([string[]]$Devices)

    $stoppedDevices = @()
    if (-not (Test-Path -LiteralPath $adb)) {
        return $stoppedDevices
    }

    foreach ($device in $Devices) {
        try {
            & $adb -s $device shell am force-stop com.senku.mobile.test | Out-Null
            & $adb -s $device shell am force-stop com.senku.mobile | Out-Null
            $psOutput = (& $adb -s $device shell ps -A -o PID,NAME,ARGS 2>$null | Out-String)
            foreach ($line in ($psOutput -split "`r?`n")) {
                if ($line -notmatch "com\\.senku\\.mobile(?:\\.test)?") {
                    continue
                }
                $columns = ($line.Trim() -split "\s+")
                if ($columns.Count -lt 1) {
                    continue
                }
                $targetPid = $columns[0]
                if ($targetPid -match '^\d+$') {
                    & $adb -s $device shell kill -9 $targetPid 2>$null | Out-Null
                }
            }
            $stoppedDevices += $device
        } catch {
        }
    }
    return $stoppedDevices
}

$patterns = @(
    "run_android_detail_followup.ps1",
    "run_android_detail_followup_logged.ps1",
    "run_android_followup_matrix.ps1",
    "run_android_instrumented_ui_smoke.ps1",
    "run_android_ui_validation_pack.ps1"
)
if ($IncludePromptRunner) {
    $patterns += "run_android_prompt.ps1"
}

$targets = Get-CimInstance Win32_Process | Where-Object {
    $commandLine = $_.CommandLine
    $_.Name -eq "powershell.exe" `
        -and $_.ProcessId -ne $PID `
        -and $commandLine `
        -and (Matches-TargetEmulator -CommandLine $commandLine -Targets $Emulators) `
        -and ($patterns | Where-Object { $commandLine -like ("*" + $_ + "*") })
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
