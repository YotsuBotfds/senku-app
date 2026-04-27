param(
    [Parameter(Mandatory = $true)]
    [string]$SuitePath,
    [string]$OutputDir = "artifacts\\bench\\android_followup_matrix",
    [string]$PushPackDir,
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "host",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string[]]$Emulators = @("emulator-5554", "emulator-5556", "emulator-5558", "emulator-5560"),
    [int]$InitialMaxWaitSeconds = 260,
    [int]$FollowUpMaxWaitSeconds = 180,
    [int]$PollSeconds = 5,
    [int]$PauseSeconds = 2,
    [int]$MaxCases = 0,
    [bool]$CaptureLogcat = $true,
    [switch]$WarmStart,
    [switch]$SkipPackPushIfCurrent,
    [switch]$ForcePackPush
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$detailRunner = Join-Path $PSScriptRoot "run_android_detail_followup.ps1"
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $detailRunner)) {
    throw "Missing runner: $detailRunner"
}
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function New-Slug {
    param([string]$Text)

    $slug = [string]$Text
    $slug = $slug.ToLowerInvariant() -replace "[^a-z0-9]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "followup_case"
    }
    return $slug
}

function Sanitize-RunLabel {
    param([string]$Text)

    $slug = ([string]$Text -replace "[^A-Za-z0-9_-]+", "_").Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "followup_case"
    }
    return $slug
}

function Load-SuiteCases {
    param([string]$Path)

    $resolved = Resolve-TargetPath -Path $Path
    if (-not (Test-Path $resolved)) {
        throw "Suite file not found: $resolved"
    }

    $raw = Get-Content -Path $resolved -Raw
    $trimmed = $raw.Trim()
    if ([string]::IsNullOrWhiteSpace($trimmed)) {
        return @()
    }

    if ($trimmed.StartsWith("[")) {
        $items = $trimmed | ConvertFrom-Json
        if ($items -is [System.Array]) {
            return $items
        }
        return @($items)
    }

    $cases = @()
    foreach ($line in (Get-Content -Path $resolved)) {
        $safeLine = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($safeLine) -or $safeLine.StartsWith("#")) {
            continue
        }
        $cases += ($safeLine | ConvertFrom-Json)
    }
    return $cases
}

function Read-RunJson {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return $null
    }

    try {
        return (Get-Content -Path $Path -Raw | ConvertFrom-Json)
    } catch {
        return $null
    }
}

function Stop-ConflictingHarnessRuns {
    param([string]$TargetEmulator)

    $pattern = "*-Emulator $TargetEmulator*"
    $conflicts = Get-CimInstance Win32_Process | Where-Object {
        $_.ProcessId -ne $PID -and
        $_.CommandLine -and
        (($_.CommandLine -like '*run_android_detail_followup_logged.ps1*') -or ($_.CommandLine -like '*run_android_detail_followup.ps1*')) -and
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
        Write-Warning ("Stopped conflicting harness runs on {0}: {1}" -f $TargetEmulator, ($stopped -join ", "))
    }
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$manifestPath = Join-Path $resolvedOutputDir ("followup_matrix_" + $timestamp + ".jsonl")
$summaryCsvPath = Join-Path $resolvedOutputDir ("followup_matrix_" + $timestamp + ".csv")

$cases = @(Load-SuiteCases -Path $SuitePath)
if ($MaxCases -gt 0) {
    $cases = @($cases | Select-Object -First $MaxCases)
}
if ($cases.Count -eq 0) {
    throw "No follow-up cases found in $SuitePath"
}
if ($Emulators.Count -eq 0) {
    throw "Provide at least one emulator in -Emulators."
}

$records = New-Object System.Collections.Generic.List[object]

for ($index = 0; $index -lt $cases.Count; $index++) {
    $case = $cases[$index]
    $initialQuery = [string]$case.initial_query
    $followUpQuery = [string]$case.follow_up_query
    if ([string]::IsNullOrWhiteSpace($followUpQuery) -and $null -ne $case.follow_up_queries) {
        $followUpList = @($case.follow_up_queries)
        if ($followUpList.Count -gt 0) {
            $followUpQuery = [string]$followUpList[0]
        }
    }
    if ([string]::IsNullOrWhiteSpace($initialQuery) -or [string]::IsNullOrWhiteSpace($followUpQuery)) {
        throw "Each case must provide initial_query and follow_up_query."
    }

    $emulator = if (-not [string]::IsNullOrWhiteSpace([string]$case.emulator)) {
        [string]$case.emulator
    } elseIf (-not [string]::IsNullOrWhiteSpace([string]$case.suggested_emulator)) {
        [string]$case.suggested_emulator
    } else {
        $Emulators[$index % $Emulators.Count]
    }
    $labelBase = if (-not [string]::IsNullOrWhiteSpace([string]$case.label)) {
        Sanitize-RunLabel -Text ([string]$case.label)
    } else {
        New-Slug -Text ($initialQuery + "_" + $followUpQuery)
    }
    $runLabel = ($labelBase + "_" + $emulator.Replace("-", "_")).Trim("_")
    $jsonPath = Join-Path $resolvedOutputDir ($runLabel + ".json")
    $logcatPath = Join-Path $resolvedOutputDir ($runLabel + ".logcat.txt")

    Write-Host ("[{0}/{1}] {2} on {3}" -f ($index + 1), $cases.Count, $runLabel, $emulator)

    Stop-ConflictingHarnessRuns -TargetEmulator $emulator

    if ($CaptureLogcat) {
        & $adb -s $emulator logcat -c | Out-Null
    }

    $startedAt = Get-Date
    $status = "passed"
    $errorMessage = $null
    $detailArgs = @{
        Emulator = $emulator
        InitialQuery = $initialQuery
        FollowUpQuery = $followUpQuery
        InferenceMode = $InferenceMode
        HostInferenceUrl = $HostInferenceUrl
        HostInferenceModel = $HostInferenceModel
        OutputDir = $resolvedOutputDir
        RunLabel = $runLabel
        InitialMaxWaitSeconds = $InitialMaxWaitSeconds
        FollowUpMaxWaitSeconds = $FollowUpMaxWaitSeconds
        PollSeconds = $PollSeconds
    }
    if (-not [string]::IsNullOrWhiteSpace($PushPackDir)) {
        $detailArgs.PushPackDir = $PushPackDir
        if ($SkipPackPushIfCurrent) {
            $detailArgs.SkipPackPushIfCurrent = $true
        }
        if ($ForcePackPush) {
            $detailArgs.ForcePackPush = $true
        }
    }
    if ($WarmStart) {
        $detailArgs.WarmStart = $true
    }
    try {
        & $detailRunner @detailArgs
    } catch {
        $status = "failed"
        $errorMessage = $_.Exception.Message
        Write-Warning ("Case failed: " + $runLabel + " :: " + $errorMessage)
    }
    $finishedAt = Get-Date

    if ($CaptureLogcat) {
        try {
            & $adb -s $emulator logcat -d | Out-File -FilePath $logcatPath -Encoding utf8
        } catch {
            if (-not $errorMessage) {
                $errorMessage = "logcat capture failed: " + $_.Exception.Message
            }
        }
    }

    $runJson = Read-RunJson -Path $jsonPath
    $record = [ordered]@{
        label = $runLabel
        emulator = $emulator
        initial_query = $initialQuery
        follow_up_query = $followUpQuery
        status = $status
        started_at = $startedAt.ToString("o")
        finished_at = $finishedAt.ToString("o")
        duration_seconds = [math]::Round(($finishedAt - $startedAt).TotalSeconds, 2)
        output_json = $jsonPath
        logcat_path = if ($CaptureLogcat) { $logcatPath } else { $null }
        final_title = if ($runJson) { $runJson.final_detail_title } else { $null }
        final_subtitle = if ($runJson) { $runJson.final_detail_subtitle } else { $null }
        lower_source_button_count = if ($runJson) { $runJson.lower_source_button_count } else { $null }
        followup_submission_mode = if ($runJson) { $runJson.followup_submission_mode } else { $null }
        warm_start = [bool]$WarmStart
        source_link_verified = if ($runJson) { $runJson.source_link_verified } else { $null }
        error = $errorMessage
    }
    $records.Add([pscustomobject]$record) | Out-Null
    ($record | ConvertTo-Json -Compress) | Add-Content -Path $manifestPath

    if ($PauseSeconds -gt 0 -and $index -lt ($cases.Count - 1)) {
        Start-Sleep -Seconds $PauseSeconds
    }
}

$records | Export-Csv -Path $summaryCsvPath -NoTypeInformation -Encoding utf8
Write-Host ("Matrix manifest written to " + $manifestPath)
Write-Host ("Matrix summary written to " + $summaryCsvPath)
