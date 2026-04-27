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

function New-SessionBatchSummary {
    param(
        [string]$Label,
        [string]$Emulator,
        [string]$ManifestPath,
        [object[]]$Records
    )

    $completed = @($Records | Where-Object { [string]::IsNullOrWhiteSpace([string]$_.error) })
    $failed = @($Records | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_.error) })
    $timedOut = @($Records | Where-Object { $_.timed_out })
    $statusGroups = @()
    foreach ($statusName in @("completed", "failed")) {
        $source = if ($statusName -eq "completed") { $completed } else { $failed }
        $statusGroups += [ordered]@{
            status = $statusName
            count = @($source).Count
            scenario_indexes = @($source | ForEach-Object { $_.scenario_index })
            session_ids = @($source | ForEach-Object { $_.session_id })
        }
    }

    return [ordered]@{
        batch_label = $Label
        emulator = $Emulator
        manifest_path = $ManifestPath
        total = @($Records).Count
        completed = $completed.Count
        failed = $failed.Count
        timed_out = $timedOut.Count
        status_groups = $statusGroups
        timed_out_items = @($timedOut | ForEach-Object {
            [ordered]@{
                scenario_index = $_.scenario_index
                session_id = $_.session_id
                session_manifest_path = $_.session_manifest_path
            }
        })
        failed_items = @($failed | ForEach-Object {
            [ordered]@{
                scenario_index = $_.scenario_index
                session_id = $_.session_id
                focus = $_.focus
                error = $_.error
                timed_out = $_.timed_out
                turn_count = $_.turn_count
                session_manifest_path = $_.session_manifest_path
            }
        })
        artifact_paths = @(
            [ordered]@{
                kind = "manifest_jsonl"
                path = $ManifestPath
            }
        ) + @($Records | ForEach-Object {
            [ordered]@{
                kind = "session_manifest_jsonl"
                scenario_index = $_.scenario_index
                session_id = $_.session_id
                path = $_.session_manifest_path
            }
        })
    }
}

function ConvertTo-SessionBatchSummaryMarkdown {
    param([object]$Summary)

    $lines = @(
        "# Android Session Batch Summary",
        "",
        "- batch_label: $($Summary.batch_label)",
        "- emulator: $($Summary.emulator)",
        "- total: $($Summary.total)",
        "- completed: $($Summary.completed)",
        "- failed: $($Summary.failed)",
        "- timed_out: $($Summary.timed_out)",
        "- manifest_path: $($Summary.manifest_path)",
        "",
        "## Status Groups"
    )

    foreach ($group in $Summary.status_groups) {
        $sessionIds = if ($group.session_ids.Count -gt 0) { ($group.session_ids -join ", ") } else { "-" }
        $lines += "- $($group.status): $($group.count) ($sessionIds)"
    }

    $lines += @("", "## Timed Out Items")
    if ($Summary.timed_out_items.Count -eq 0) {
        $lines += "- none"
    } else {
        foreach ($item in $Summary.timed_out_items) {
            $lines += "- scenario_index=$($item.scenario_index) session_id=$($item.session_id) session_manifest_path=$($item.session_manifest_path)"
        }
    }

    $lines += @("", "## Failed Items")
    if ($Summary.failed_items.Count -eq 0) {
        $lines += "- none"
    } else {
        foreach ($item in $Summary.failed_items) {
            $lines += "- scenario_index=$($item.scenario_index) session_id=$($item.session_id) focus=$($item.focus) timed_out=$($item.timed_out) turn_count=$($item.turn_count) error=$($item.error) session_manifest_path=$($item.session_manifest_path)"
        }
    }

    $lines += @("", "## Artifact Paths")
    foreach ($artifact in $Summary.artifact_paths) {
        $suffix = ""
        if ($artifact.PSObject.Properties.Name -contains "scenario_index") {
            $suffix = " scenario_index=$($artifact.scenario_index) session_id=$($artifact.session_id)"
        }
        $lines += "- kind=$($artifact.kind)$suffix path=$($artifact.path)"
    }

    return ($lines -join [Environment]::NewLine) + [Environment]::NewLine
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
$records = @()

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
    $records += [pscustomobject]$record
    ($record | ConvertTo-Json -Compress -Depth 5) | Add-Content -Path $aggregateManifestPath

    if (($scenarioError -or ($lastTurn -and $lastTurn.error)) -and -not $ContinueOnError) {
        if ($scenarioError) {
            throw $scenarioError
        }
        throw $lastTurn.error
    }
}

$summary = New-SessionBatchSummary -Label $label -Emulator $Emulator -ManifestPath $aggregateManifestPath -Records $records
$summaryJsonPath = Join-Path $outputPath "summary.json"
$summaryMarkdownPath = Join-Path $outputPath "summary.md"
($summary | ConvertTo-Json -Depth 6) | Set-Content -Path $summaryJsonPath -Encoding UTF8
ConvertTo-SessionBatchSummaryMarkdown -Summary $summary | Set-Content -Path $summaryMarkdownPath -Encoding UTF8

Write-Host "Session batch manifest saved to $aggregateManifestPath"
Write-Host "Session batch summary saved to $summaryJsonPath"
Write-Host "Session batch summary markdown saved to $summaryMarkdownPath"
