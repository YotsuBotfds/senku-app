param(
    [string]$Emulator = "emulator-5554",
    [Parameter(Mandatory = $true)]
    [string]$Query,
    [string]$PushPackDir,
    [switch]$SkipPackPushIfCurrent,
    [switch]$ForcePackPush,
    [switch]$WarmStart,
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "preserve",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [int]$MaxWaitSeconds = 180,
    [int]$PollSeconds = 5,
    [string]$LogcatPath,
    [switch]$ClearLogcatBeforeLaunch,
    [string]$LogcatSpec = "SenkuPackRepo:D SenkuMobile:D *:S"
)

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}

function Quote-AndroidShellArg {
    param([string]$Value)

    if ($null -eq $Value) {
        return "''"
    }

    return "'" + $Value.Replace("'", "'\''") + "'"
}

$pushPackScript = Join-Path $PSScriptRoot "push_mobile_pack_to_android.ps1"
if (-not [string]::IsNullOrWhiteSpace($PushPackDir)) {
    if (-not (Test-Path $pushPackScript)) {
        throw "push script not found at $pushPackScript"
    }
    $pushPackArgs = @(
        "-Device", $Emulator,
        "-PackDir", $PushPackDir,
        "-ForceStop"
    )
    if ($SkipPackPushIfCurrent) {
        $pushPackArgs += "-SkipIfCurrent"
    }
    if ($ForcePackPush) {
        $pushPackArgs += "-ForcePush"
    }
    & $pushPackScript @pushPackArgs
}

function Save-Logcat {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return
    }

    $parent = Split-Path -Parent $Path
    if ($parent) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }

    & $adb -s $Emulator logcat -d $LogcatSpec | Out-File -Encoding utf8 $Path
}

function Stop-ConflictingHarnessRuns {
    param([string]$TargetEmulator)

    $pattern = "*-Emulator $TargetEmulator*"
    $conflicts = Get-CimInstance Win32_Process | Where-Object {
        $_.ProcessId -ne $PID -and
        $_.CommandLine -and
        (($_.CommandLine -like '*run_android_search_log_only.ps1*') -or
            ($_.CommandLine -like '*run_android_prompt_logged.ps1*') -or
            ($_.CommandLine -like '*run_android_prompt.ps1*')) -and
        $_.CommandLine -like $pattern
    }

    if (-not $conflicts) {
        return
    }

    $stopped = @()
    foreach ($conflict in $conflicts) {
        try {
            Stop-Process -Id $conflict.ProcessId -Force -ErrorAction Stop
            $stopped += $conflict.ProcessId
        } catch {
        }
    }

    if ($stopped.Count -gt 0) {
        Write-Warning ("Stopped conflicting search/prompt harness runs on {0}: {1}" -f $TargetEmulator, ($stopped -join ", "))
    }
}

function Get-LogText {
    return (& $adb -s $Emulator logcat -d $LogcatSpec | Out-String)
}

function Get-ProgressLines {
    param([string]$LogText)

    if ([string]::IsNullOrWhiteSpace($LogText)) {
        return @()
    }

    return $LogText -split "`r?`n" | Where-Object {
        $_.Contains($queryNeedle) -and (
            $_ -match "search.start query=" -or
            $_ -match "routeGuideSearch" -or
            $_ -match "routeSearch query=" -or
            $_ -match "search query=" -or
            $_ -match "search.topResults query=" -or
            $_ -match "routeChunkFts.skip"
        )
    }
}

$searchStartNeedle = "search.start query=""$Query"""
$searchCompleteNeedle = "search query=""$Query"""
$topResultsNeedle = "search.topResults query=""$Query"""
$queryNeedle = "query=""$Query"""

if ($ClearLogcatBeforeLaunch) {
    & $adb -s $Emulator logcat -c | Out-Null
}

Stop-ConflictingHarnessRuns -TargetEmulator $Emulator

function Start-SearchLaunch {
    $encodedQuery = [System.Uri]::EscapeDataString($Query)
    $commandParts = @(
        "am", "start",
        "-n", "com.senku.mobile/.MainActivity",
        "--es", "auto_query", (Quote-AndroidShellArg $encodedQuery)
    )
    switch ($InferenceMode) {
        "host" {
            $commandParts += @("--ez", "host_inference_enabled", "true")
            if (-not [string]::IsNullOrWhiteSpace($HostInferenceUrl)) {
                $commandParts += @("--es", "host_inference_url", (Quote-AndroidShellArg $HostInferenceUrl))
            }
            if (-not [string]::IsNullOrWhiteSpace($HostInferenceModel)) {
                $commandParts += @("--es", "host_inference_model", (Quote-AndroidShellArg $HostInferenceModel))
            }
        }
        "local" {
            $commandParts += @("--ez", "host_inference_enabled", "false")
        }
    }

    if (-not $WarmStart) {
        & $adb -s $Emulator shell am force-stop com.senku.mobile | Out-Null
    }
    & $adb -s $Emulator shell ($commandParts -join " ")
}

Write-Host "Launching search on $Emulator"
Start-SearchLaunch

$deadline = (Get-Date).AddSeconds($MaxWaitSeconds)
$searchStarted = $false
$completed = $false
$lastLogText = ""
$lastProgressCount = 0

while ((Get-Date) -lt $deadline) {
    Start-Sleep -Seconds $PollSeconds
    $lastLogText = Get-LogText
    $progressLines = Get-ProgressLines -LogText $lastLogText
    if ($progressLines.Count -gt $lastProgressCount) {
        $progressLines[$lastProgressCount..($progressLines.Count - 1)] | ForEach-Object { Write-Host $_ }
        $lastProgressCount = $progressLines.Count
    }
    if (-not $searchStarted -and $lastLogText.Contains($searchStartNeedle)) {
        $searchStarted = $true
    }
    if ($searchStarted -and ($lastLogText.Contains($topResultsNeedle) -or $lastLogText.Contains($searchCompleteNeedle))) {
        $completed = $true
        break
    }
}

Save-Logcat -Path $LogcatPath

$summary = @()
if ($completed) {
    $summary = Get-ProgressLines -LogText $lastLogText
    if ($summary.Count -gt $lastProgressCount) {
        $summary[$lastProgressCount..($summary.Count - 1)] | ForEach-Object { Write-Host $_ }
    } elseif ($summary.Count -gt 0 -and $lastProgressCount -eq 0) {
        $summary | ForEach-Object { Write-Host $_ }
    }
    exit 0
}

Write-Warning "Search did not finish within $MaxWaitSeconds seconds on $Emulator."
if ($searchStarted) {
    Write-Host "Search started but did not log completion."
} else {
    Write-Host "No search.start line was observed."
}
exit 1
