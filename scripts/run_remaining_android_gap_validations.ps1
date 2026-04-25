param(
    [ValidateSet("both", "single", "followup")]
    [string]$Mode = "both",

    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "host",

    [switch]$RequireAll
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$gapRunner = Join-Path $PSScriptRoot "run_android_gap_pack.ps1"
if (-not (Test-Path -LiteralPath $gapRunner)) {
    throw "Missing Android gap runner: $gapRunner"
}

function Resolve-AdbPath {
    try {
        $adbCommand = Get-Command adb.exe -ErrorAction Stop
        if ($adbCommand -and -not [string]::IsNullOrWhiteSpace([string]$adbCommand.Source)) {
            return [string]$adbCommand.Source
        }
    } catch {
    }

    $candidates = @()
    if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_SDK_ROOT)) {
        $candidates += (Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe")
    }
    if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_HOME)) {
        $candidates += (Join-Path $env:ANDROID_HOME "platform-tools\adb.exe")
    }
    if (-not [string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        $candidates += (Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe")
    }

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    throw "Could not find adb.exe. Run scripts\unblock_android_adb.ps1 first, then open a fresh terminal."
}

function Get-ReadySerials {
    param([string]$AdbPath)

    $lines = & $AdbPath devices
    if ($LASTEXITCODE -ne 0) {
        throw "adb devices failed with exit code $LASTEXITCODE"
    }

    $serials = @()
    foreach ($line in $lines) {
        if ($line -match "^(emulator-\d+)\s+device\b") {
            $serials += $Matches[1]
        }
    }
    return $serials
}

$adb = Resolve-AdbPath
$readySerials = @(Get-ReadySerials -AdbPath $adb)

Write-Host "ADB: $adb"
Write-Host ("Ready emulators: " + $(if ($readySerials.Count) { $readySerials -join ", " } else { "(none)" }))

$caseGroups = @(
    [pscustomobject]@{
        serial = "emulator-5556"
        cases = @("AGP-20260412-CGS-01", "AGP-20260412-CGS-02")
    },
    [pscustomobject]@{
        serial = "emulator-5560"
        cases = @("AGP-20260412-CRF-04")
    }
)

$missing = @()
foreach ($group in $caseGroups) {
    if ($readySerials -contains $group.serial) {
        Write-Host ""
        Write-Host ("Running {0} on {1}" -f ($group.cases -join ", "), $group.serial)
        Push-Location $repoRoot
        try {
            & powershell -NoProfile -ExecutionPolicy Bypass -File $gapRunner `
                -CaseIds $group.cases `
                -Mode $Mode `
                -InferenceMode $InferenceMode
            if ($LASTEXITCODE -ne 0) {
                throw "run_android_gap_pack.ps1 failed with exit code $LASTEXITCODE"
            }
        } finally {
            Pop-Location
        }
    } else {
        $missing += $group
    }
}

if ($missing.Count -gt 0) {
    Write-Host ""
    foreach ($group in $missing) {
        Write-Warning ("Skipped {0}; required emulator is not ready: {1}" -f ($group.cases -join ", "), $group.serial)
    }
    Write-Host ""
    Write-Host "To start the missing landscape phone lane:"
    Write-Host "  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\start_senku_emulator_matrix.ps1 -Roles phone_landscape -Mode read_only"
    if ($RequireAll) {
        throw "One or more required emulators were missing."
    }
}
