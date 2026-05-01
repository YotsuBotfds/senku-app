param(
    [string]$Emulator = "emulator-5554",
    [string]$DebugDetailTitle = "Synthetic answer",
    [string]$DebugDetailSubtitle = "AI-generated answer",
    [string]$DebugDetailBody = "Synthetic debug answer body.",
    [switch]$ForceStop,
    [string]$DumpPath,
    [int]$AdbCommandTimeoutMilliseconds = 60000
)

$ErrorActionPreference = "Stop"

$androidHarnessCommonPath = Join-Path $PSScriptRoot "android_harness_common.psm1"
if (-not (Test-Path -LiteralPath $androidHarnessCommonPath)) {
    throw "android_harness_common.psm1 not found at $androidHarnessCommonPath"
}
Import-Module $androidHarnessCommonPath -Force

if ($AdbCommandTimeoutMilliseconds -le 0) {
    throw "-AdbCommandTimeoutMilliseconds must be a positive integer."
}

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}

function Invoke-AdbChecked {
    param(
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [int]$TimeoutMilliseconds = $AdbCommandTimeoutMilliseconds
    )

    if ($TimeoutMilliseconds -le 0) {
        throw "-TimeoutMilliseconds must be a positive integer."
    }

    $fullArgs = @("-s", $Emulator) + $Arguments
    $result = Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $fullArgs -TimeoutMilliseconds $TimeoutMilliseconds
    $outputText = if ($null -eq $result.output) { "" } else { [string]$result.output }
    $script:LastAdbOutput = @($outputText -split "`r?`n")
    $script:LastAdbExitCode = [int]$result.exit_code
    $joinedArgs = ($Arguments -join " ")

    if ($result.timed_out) {
        throw ("adb timed out after {0}ms ({1}): {2}" -f $TimeoutMilliseconds, $joinedArgs, $outputText).TrimEnd()
    }
    if ($script:LastAdbExitCode -ne 0) {
        $message = ($script:LastAdbOutput | Out-String).Trim()
        throw "adb failed ($joinedArgs): $message"
    }
    return $script:LastAdbOutput
}

function Test-DetailForeground {
    try {
        $activityOutput = Invoke-AdbChecked -Arguments @("shell", "dumpsys", "activity", "activities")
    } catch {
        return $false
    }
    if ([string]::IsNullOrWhiteSpace(($activityOutput | Out-String))) {
        return $false
    }
    return ($activityOutput -match "topResumedActivity=.*com\.senku\.mobile/\.DetailActivity") -or
        ($activityOutput -match "mResumedActivity:.*com\.senku\.mobile/\.DetailActivity")
}

function Save-UiDump {
    param([Parameter(Mandatory = $true)][string]$LocalDump)

    try {
        $xml = (Invoke-AdbChecked -Arguments @("exec-out", "uiautomator", "dump", "/dev/tty") | Out-String)
    } catch {
        return $false
    }
    if ([string]::IsNullOrWhiteSpace($xml)) {
        return $false
    }
    $xmlMatch = [regex]::Match($xml, '(?s)<\?xml.*</hierarchy>')
    if (-not $xmlMatch.Success) {
        return $false
    }
    $parent = Split-Path -Parent $LocalDump
    if ($parent) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    [System.IO.File]::WriteAllText($LocalDump, $xmlMatch.Value, [System.Text.Encoding]::UTF8)
    return (Test-Path $LocalDump)
}

function Test-DetailSurface {
    param([Parameter(Mandatory = $true)][string]$DumpFile)

    if (-not (Test-Path $DumpFile)) {
        return $false
    }
    $text = Get-Content $DumpFile -Raw
    return $text.Contains('resource-id="com.senku.mobile:id/detail_title"')
}

if ($ForceStop) {
    Invoke-AdbChecked -Arguments @("shell", "am", "force-stop", "com.senku.mobile") | Out-Null
}

$activityArgs = @(
    "shell",
    "am",
    "start",
    "-W",
    "-n",
    "com.senku.mobile/.MainActivity",
    "--ez",
    "debug_open_detail",
    "true",
    "--es",
    "debug_detail_title",
    $DebugDetailTitle,
    "--es",
    "debug_detail_subtitle",
    $DebugDetailSubtitle,
    "--es",
    "debug_detail_body",
    $DebugDetailBody
)

Invoke-AdbChecked -Arguments $activityArgs | Out-Null

if (-not [string]::IsNullOrWhiteSpace($DumpPath)) {
    $relaunches = 0
    for ($attempt = 0; $attempt -lt 18; $attempt++) {
        if (Test-DetailForeground) {
            if (Save-UiDump -LocalDump $DumpPath) {
                if (Test-DetailSurface -DumpFile $DumpPath) {
                    break
                }
            }
        } elseif ($attempt -ge 8 -and $relaunches -lt 1) {
            Invoke-AdbChecked -Arguments @("shell", "am", "force-stop", "com.senku.mobile") | Out-Null
            Start-Sleep -Milliseconds 400
            Invoke-AdbChecked -Arguments $activityArgs | Out-Null
            $relaunches += 1
        }
        Start-Sleep -Milliseconds 350
    }
}

Write-Host "Launched MainActivity debug detail intent on emulator '$Emulator'."
