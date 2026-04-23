param(
    [string]$Emulator = "emulator-5554",
    [Parameter(Mandatory = $true)]
    [string]$PromptFile,
    [string]$OutputDir = "artifacts\\bench\\android_batches",
    [switch]$Ask,
    [switch]$WaitForCompletion,
    [int]$MaxWaitSeconds = 180,
    [int]$PollSeconds = 10,
    [int]$StartIndex = 0,
    [int]$MaxPrompts = 20,
    [int]$Stride = 1,
    [string]$BatchLabel
)

$repoRoot = Split-Path -Parent $PSScriptRoot
$runnerPath = Join-Path $PSScriptRoot "run_android_prompt.ps1"
if (-not (Test-Path $runnerPath)) {
    throw "run_android_prompt.ps1 not found at $runnerPath"
}

$promptPath = $PromptFile
if (-not [System.IO.Path]::IsPathRooted($promptPath)) {
    $promptPath = Join-Path $repoRoot $PromptFile
}
if (-not (Test-Path $promptPath)) {
    throw "Prompt file not found at $promptPath"
}

$outputPath = $OutputDir
if (-not [System.IO.Path]::IsPathRooted($outputPath)) {
    $outputPath = Join-Path $repoRoot $OutputDir
}
New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

$prompts = @()
$lineNumber = -1
foreach ($line in Get-Content -Path $promptPath) {
    $lineNumber += 1
    $trimmed = $line.Trim()
    if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith("#")) {
        continue
    }
    $prompts += [pscustomobject]@{
        Prompt = $trimmed
        SourceLine = $lineNumber + 1
    }
}

if ($prompts.Count -eq 0) {
    throw "No runnable prompts found in $promptPath"
}

if ($StartIndex -lt 0) {
    throw "StartIndex must be at least 0"
}

if ($Stride -lt 1) {
    throw "Stride must be at least 1"
}

$selected = @()
for ($index = $StartIndex; $index -lt $prompts.Count -and $selected.Count -lt $MaxPrompts; $index += $Stride) {
    $selected += $prompts[$index]
}

if ($selected.Count -eq 0) {
    throw "Selection produced no prompts. prompts=$($prompts.Count) start=$StartIndex max=$MaxPrompts stride=$Stride"
}

function New-Slug {
    param([string]$Text)

    $slug = $Text.ToLowerInvariant() -replace "[^a-z0-9]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        $slug = "prompt"
    }
    if ($slug.Length -gt 64) {
        $slug = $slug.Substring(0, 64).Trim("_")
    }
    return $slug
}

function Get-DumpCompletionState {
    param([string]$DumpPath)

    if (-not (Test-Path $DumpPath)) {
        return [pscustomobject]@{
            Completed = $false
            State = "missing_dump"
        }
    }

    $text = Get-Content -Path $DumpPath -Raw
    if ($text -match 'resource-id="com\.senku\.mobile:id/detail_title"') {
        return [pscustomobject]@{
            Completed = $true
            State = "detail_screen"
        }
    }

    $busyMarkers = @(
        'Retrieving offline context...',
        'Retrieving offline context with chat memory...',
        'Generating answer locally...',
        'Searching for &quot;',
        'Searching for "',
        'Importing model...',
        'Installing offline pack...',
        'Installing pack...',
        'Reinstalling pack...'
    )
    foreach ($marker in $busyMarkers) {
        if ($text.Contains($marker)) {
            return [pscustomobject]@{
                Completed = $false
                State = "busy_screen"
            }
        }
    }

    if ($text -match 'resource-id="com\.senku\.mobile:id/status_text"') {
        return [pscustomobject]@{
            Completed = $false
            State = "status_screen"
        }
    }

    return [pscustomobject]@{
        Completed = $false
        State = "unknown_screen"
    }
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$safeEmulator = $Emulator.Replace("-", "_")
$label = if ([string]::IsNullOrWhiteSpace($BatchLabel)) {
    "${safeEmulator}_${timestamp}"
} else {
    ($BatchLabel -replace "[^A-Za-z0-9_-]+", "_")
}
$label = $label.Trim("_")
if ([string]::IsNullOrWhiteSpace($label)) {
    $label = "${safeEmulator}_${timestamp}"
}
$manifestPath = Join-Path $outputPath "${label}.jsonl"

for ($runIndex = 0; $runIndex -lt $selected.Count; $runIndex++) {
    $entry = $selected[$runIndex]
    $slug = New-Slug -Text $entry.Prompt
    $dumpName = "{0:D3}_{1}_{2}.xml" -f ($runIndex + 1), $safeEmulator, $slug
    $dumpPath = Join-Path $outputPath $dumpName

    $runnerArgs = @{
        Emulator = $Emulator
        Query = $entry.Prompt
        DumpPath = $dumpPath
        MaxWaitSeconds = $MaxWaitSeconds
        PollSeconds = $PollSeconds
    }
    if ($Ask) {
        $runnerArgs["Ask"] = $true
    }
    if ($WaitForCompletion) {
        $runnerArgs["WaitForCompletion"] = $true
    }

    $startedAt = Get-Date
    $failed = $null
    try {
        & $runnerPath @runnerArgs
        if (-not $?) {
            $failed = "run_android_prompt.ps1 reported failure"
        } elseif ($LASTEXITCODE -ne 0) {
            $failed = "run_android_prompt.ps1 exit code $LASTEXITCODE"
        }
    } catch {
        $failed = $_.Exception.Message
    }
    $finishedAt = Get-Date
    $dumpState = Get-DumpCompletionState -DumpPath $dumpPath
    if (-not $failed -and $Ask -and $WaitForCompletion -and -not $dumpState.Completed) {
        $failed = "Saved dump did not reach detail screen ($($dumpState.State))"
    }

    $record = [ordered]@{
        batch_label = $label
        emulator = $Emulator
        prompt = $entry.Prompt
        prompt_source_line = $entry.SourceLine
        run_index = $runIndex + 1
        ask = [bool]$Ask
        wait_for_completion = [bool]$WaitForCompletion
        dump_path = $dumpPath
        dump_completion_state = $dumpState.State
        started_at = $startedAt.ToString("o")
        finished_at = $finishedAt.ToString("o")
        elapsed_seconds = [math]::Round(($finishedAt - $startedAt).TotalSeconds, 1)
        error = $failed
    }
    ($record | ConvertTo-Json -Compress) | Add-Content -Path $manifestPath
}

Write-Host "Batch manifest saved to $manifestPath"
