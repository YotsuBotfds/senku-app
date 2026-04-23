param(
    [string]$Emulator = "emulator-5554",
    [string]$DebugDetailTitle = "Synthetic answer",
    [string]$DebugDetailSubtitle = "AI-generated answer",
    [string]$DebugDetailBody = "Synthetic debug answer body.",
    [switch]$ForceStop,
    [string]$DumpPath
)

$ErrorActionPreference = "Stop"

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}

function Invoke-AdbChecked {
    param([Parameter(Mandatory = $true)][string[]]$Arguments)

    $stdoutFile = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_adb_" + [guid]::NewGuid().ToString("N") + ".out")
    $stderrFile = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_adb_" + [guid]::NewGuid().ToString("N") + ".err")
    try {
        $fullArgs = @("-s", $Emulator) + $Arguments
        $process = Start-Process -FilePath $adb `
            -ArgumentList $fullArgs `
            -NoNewWindow `
            -Wait `
            -PassThru `
            -RedirectStandardOutput $stdoutFile `
            -RedirectStandardError $stderrFile
        $stdout = if (Test-Path $stdoutFile) { Get-Content $stdoutFile } else { @() }
        $stderr = if (Test-Path $stderrFile) { Get-Content $stderrFile } else { @() }
        $script:LastAdbOutput = @($stdout + $stderr)
        $script:LastAdbExitCode = $process.ExitCode
    } finally {
        if (Test-Path $stdoutFile) {
            Remove-Item -LiteralPath $stdoutFile -Force
        }
        if (Test-Path $stderrFile) {
            Remove-Item -LiteralPath $stderrFile -Force
        }
    }

    if ($script:LastAdbExitCode -ne 0) {
        $joinedArgs = @($Arguments -join " ")
        $message = ($script:LastAdbOutput | Out-String).Trim()
        throw "adb failed ($joinedArgs): $message"
    }
    return $script:LastAdbOutput
}

function Test-DetailForeground {
    $activityOutput = & $adb -s $Emulator shell "dumpsys activity activities" 2>$null
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($activityOutput)) {
        return $false
    }
    return ($activityOutput -match "topResumedActivity=.*com\.senku\.mobile/\.DetailActivity") -or
        ($activityOutput -match "mResumedActivity:.*com\.senku\.mobile/\.DetailActivity")
}

function Save-UiDump {
    param([Parameter(Mandatory = $true)][string]$LocalDump)

    $xml = (& $adb -s $Emulator exec-out uiautomator dump /dev/tty 2>$null | Out-String)
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
