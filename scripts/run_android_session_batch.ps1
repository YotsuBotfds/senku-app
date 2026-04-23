param(
    [string]$Emulator = "emulator-5554",
    [Parameter(Mandatory = $true)]
    [string]$ScenarioFile,
    [string]$OutputDir = "artifacts\\bench\\android_sessions",
    [string]$BatchLabel,
    [int]$StartIndex = 0,
    [int]$MaxScenarios = 20,
    [switch]$ContinueOnError,
    [int]$WaitSeconds = 5,
    [int]$MaxWaitSeconds = 180,
    [int]$PollSeconds = 5
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$runnerPath = Join-Path $PSScriptRoot "run_android_session_flow.ps1"
if (-not (Test-Path $runnerPath)) {
    throw "run_android_session_flow.ps1 not found at $runnerPath"
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

    $slug = ($Text -replace "[^A-Za-z0-9_-]+", "_").Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        $slug = "session"
    }
    return $slug.ToLowerInvariant()
}

$scenarioPath = Resolve-TargetPath -Path $ScenarioFile
if (-not (Test-Path $scenarioPath)) {
    throw "Scenario file not found at $scenarioPath"
}

$outputPath = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

$scenarios = @()
foreach ($line in Get-Content -Path $scenarioPath) {
    $trimmed = $line.Trim()
    if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith("#")) {
        continue
    }
    $scenarios += ($trimmed | ConvertFrom-Json)
}

if ($StartIndex -lt 0) {
    throw "StartIndex must be at least 0."
}
if ($MaxScenarios -lt 1) {
    throw "MaxScenarios must be at least 1."
}
if ($StartIndex -ge $scenarios.Count) {
    throw "StartIndex $StartIndex is past the end of the scenario list ($($scenarios.Count))."
}

$selected = $scenarios[$StartIndex..([Math]::Min($scenarios.Count - 1, $StartIndex + $MaxScenarios - 1))]

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$safeEmulator = $Emulator.Replace("-", "_")
$label = if ([string]::IsNullOrWhiteSpace($BatchLabel)) {
    "${safeEmulator}_session_batch_${timestamp}"
} else {
    New-Slug -Text $BatchLabel
}
$aggregateManifestPath = Join-Path $outputPath "${label}.jsonl"

for ($index = 0; $index -lt $selected.Count; $index++) {
    $scenario = $selected[$index]
    $scenarioId = if ($scenario.session_id) { [string]$scenario.session_id } else { "scenario_{0:D3}" -f ($StartIndex + $index + 1) }
    $scenarioSlug = New-Slug -Text $scenarioId
    $sessionLabel = "${label}_${scenarioSlug}"
    $followUps = @()
    if ($scenario.follow_up_queries) {
        $followUps = @($scenario.follow_up_queries)
    } elseif ($scenario.follow_up_query) {
        $followUps = @([string]$scenario.follow_up_query)
    }

    if (-not $scenario.initial_query) {
        throw "Scenario $scenarioId is missing initial_query."
    }
    if ($followUps.Count -lt 1) {
        throw "Scenario $scenarioId must include at least one follow_up_queries entry."
    }

    Write-Host ("[{0}/{1}] Running session scenario {2}" -f ($index + 1), $selected.Count, $scenarioId)

    $startedAt = Get-Date
    $scenarioError = $null
    $sessionManifestPath = Join-Path $outputPath "${sessionLabel}.jsonl"

    try {
        & $runnerPath `
            -Emulator $Emulator `
            -InitialQuery ([string]$scenario.initial_query) `
            -FollowUpQueries $followUps `
            -OutputDir $outputPath `
            -SessionLabel $sessionLabel `
            -ClearChatFirst `
            -CaptureSessionPanelAfterTurn `
            -WaitSeconds $WaitSeconds `
            -MaxWaitSeconds $MaxWaitSeconds `
            -PollSeconds $PollSeconds
    } catch {
        $scenarioError = $_.Exception.Message
    }

    $finishedAt = Get-Date
    $turnRecords = @()
    if (Test-Path $sessionManifestPath) {
        foreach ($entry in Get-Content -Path $sessionManifestPath) {
            if (-not [string]::IsNullOrWhiteSpace($entry)) {
                $turnRecords += ($entry | ConvertFrom-Json)
            }
        }
    }
    $lastTurn = if ($turnRecords.Count -gt 0) { $turnRecords[-1] } else { $null }
    $timedOut = $false
    if ($turnRecords.Count -gt 0) {
        $timedOut = ($turnRecords | Where-Object { $_.timed_out }).Count -gt 0
    }

    $record = [ordered]@{
        batch_label = $label
        emulator = $Emulator
        scenario_index = $StartIndex + $index + 1
        session_id = $scenarioId
        focus = if ($scenario.focus) { [string]$scenario.focus } else { $null }
        initial_query = [string]$scenario.initial_query
        follow_up_queries = $followUps
        expected_behavior = if ($scenario.expected_behavior) { [string]$scenario.expected_behavior } else { $null }
        session_manifest_path = $sessionManifestPath
        started_at = $startedAt.ToString("o")
        finished_at = $finishedAt.ToString("o")
        elapsed_seconds = [math]::Round(($finishedAt - $startedAt).TotalSeconds, 1)
        turn_count = $turnRecords.Count
        timed_out = $timedOut
        error = if ($scenarioError) { $scenarioError } elseif ($lastTurn) { $lastTurn.error } else { $null }
        final_detail_title = if ($lastTurn) { $lastTurn.detail_title } else { $null }
        final_detail_subtitle = if ($lastTurn) { $lastTurn.detail_subtitle } else { $null }
        final_detail_body = if ($lastTurn) { $lastTurn.detail_body } else { $null }
        final_session_text = if ($lastTurn) { $lastTurn.session_text } else { $null }
    }
    ($record | ConvertTo-Json -Compress -Depth 5) | Add-Content -Path $aggregateManifestPath

    if (($scenarioError -or ($lastTurn -and $lastTurn.error)) -and -not $ContinueOnError) {
        if ($scenarioError) {
            throw $scenarioError
        }
        throw $lastTurn.error
    }
}

Write-Host "Session batch manifest saved to $aggregateManifestPath"
