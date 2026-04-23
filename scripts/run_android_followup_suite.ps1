param(
    [Parameter(Mandatory = $true)]
    [string]$SuitePath,
    [string]$OutputDir = "artifacts\\bench\\android_followup_suite",
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "host",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string[]]$Emulators = @("emulator-5554", "emulator-5556", "emulator-5558", "emulator-5560"),
    [int]$InitialMaxWaitSeconds = 260,
    [int]$FollowUpMaxWaitSeconds = 180,
    [int]$PollSeconds = 5,
    [int]$PauseSeconds = 2,
    [int]$MaxCases = 0
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$detailRunner = Join-Path $PSScriptRoot "run_android_detail_followup.ps1"
if (-not (Test-Path $detailRunner)) {
    throw "Missing runner: $detailRunner"
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

    $slug = $Text.ToLowerInvariant() -replace "[^a-z0-9]+", "_"
    $slug = $slug.Trim("_")
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

$resolvedSuitePath = Resolve-TargetPath -Path $SuitePath
$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$manifestPath = Join-Path $resolvedOutputDir ("followup_suite_" + $timestamp + ".jsonl")

$cases = @(Load-SuiteCases -Path $resolvedSuitePath)
if ($MaxCases -gt 0) {
    $cases = @($cases | Select-Object -First $MaxCases)
}
if ($cases.Count -eq 0) {
    throw "No follow-up cases found in $resolvedSuitePath"
}
if ($Emulators.Count -eq 0) {
    throw "Provide at least one emulator in -Emulators."
}

for ($index = 0; $index -lt $cases.Count; $index++) {
    $case = $cases[$index]
    $initialQuery = [string]$case.initial_query
    $followUpQuery = [string]$case.follow_up_query
    if ([string]::IsNullOrWhiteSpace($initialQuery) -or [string]::IsNullOrWhiteSpace($followUpQuery)) {
        throw "Each case must provide initial_query and follow_up_query."
    }

    $emulator = if (-not [string]::IsNullOrWhiteSpace([string]$case.emulator)) {
        [string]$case.emulator
    } else {
        $Emulators[$index % $Emulators.Count]
    }
    $labelBase = if (-not [string]::IsNullOrWhiteSpace([string]$case.label)) {
        [string]$case.label
    } else {
        (New-Slug -Text ($initialQuery + "_" + $followUpQuery))
    }
    $runLabel = ($labelBase + "_" + $emulator.Replace("-", "_")).Trim("_")

    Write-Host ("[{0}/{1}] {2} on {3}" -f ($index + 1), $cases.Count, $runLabel, $emulator)

    $startedAt = Get-Date
    $status = "passed"
    $errorMessage = $null
    try {
        & powershell -ExecutionPolicy Bypass -File $detailRunner `
            -Emulator $emulator `
            -InitialQuery $initialQuery `
            -FollowUpQuery $followUpQuery `
            -InferenceMode $InferenceMode `
            -HostInferenceUrl $HostInferenceUrl `
            -HostInferenceModel $HostInferenceModel `
            -OutputDir $resolvedOutputDir `
            -RunLabel $runLabel `
            -InitialMaxWaitSeconds $InitialMaxWaitSeconds `
            -FollowUpMaxWaitSeconds $FollowUpMaxWaitSeconds `
            -PollSeconds $PollSeconds
    } catch {
        $status = "failed"
        $errorMessage = $_.Exception.Message
        Write-Warning ("Case failed: " + $runLabel + " :: " + $errorMessage)
    }
    $finishedAt = Get-Date

    $record = [ordered]@{
        label = $runLabel
        emulator = $emulator
        initial_query = $initialQuery
        follow_up_query = $followUpQuery
        status = $status
        started_at = $startedAt.ToString("o")
        finished_at = $finishedAt.ToString("o")
        duration_seconds = [math]::Round(($finishedAt - $startedAt).TotalSeconds, 2)
        output_json = (Join-Path $resolvedOutputDir ($runLabel + ".json"))
        error = $errorMessage
    }
    ($record | ConvertTo-Json -Compress) | Add-Content -Path $manifestPath

    if ($PauseSeconds -gt 0 -and $index -lt ($cases.Count - 1)) {
        Start-Sleep -Seconds $PauseSeconds
    }
}

Write-Host ("Suite manifest written to " + $manifestPath)
