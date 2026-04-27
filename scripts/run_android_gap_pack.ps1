param(
    [string]$GapPackPath = "artifacts\\prompts\\adhoc\\android_gap_pack_20260412.jsonl",
    [string]$OutputDir = "artifacts\\bench\\android_gap_pack_runs",
    [ValidateSet("single", "followup", "both")]
    [string]$Mode = "both",
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "host",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string[]]$FallbackEmulators = @("emulator-5554", "emulator-5556", "emulator-5558", "emulator-5560"),
    [string[]]$CaseIds = @(),
    [string[]]$Families = @(),
    [string[]]$Priorities = @(),
    [int]$MaxCases = 0,
    [int]$SingleMaxWaitSeconds = 180,
    [int]$InitialMaxWaitSeconds = 260,
    [int]$FollowUpMaxWaitSeconds = 180,
    [int]$PollSeconds = 5,
    [int]$PauseSeconds = 2
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$singleRunner = Join-Path $PSScriptRoot "run_android_prompt.ps1"
$followUpRunner = Join-Path $PSScriptRoot "run_android_detail_followup.ps1"
if (-not (Test-Path $singleRunner)) {
    throw "Missing runner: $singleRunner"
}
if (-not (Test-Path $followUpRunner)) {
    throw "Missing runner: $followUpRunner"
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
        return "case"
    }
    return $slug
}

function Load-GapPackCases {
    param([string]$Path)

    $resolved = Resolve-TargetPath -Path $Path
    if (-not (Test-Path $resolved)) {
        throw "Gap pack not found: $resolved"
    }

    $cases = @()
    foreach ($line in Get-Content -Path $resolved) {
        $safeLine = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($safeLine) -or $safeLine.StartsWith("#")) {
            continue
        }
        $cases += ($safeLine | ConvertFrom-Json)
    }
    return $cases
}

function Normalize-FilterValues {
    param([string[]]$Values)

    $normalized = @()
    foreach ($value in $Values) {
        if (-not [string]::IsNullOrWhiteSpace([string]$value)) {
            $normalized += ([string]$value).Trim().ToLowerInvariant()
        }
    }
    return $normalized
}

function Assert-RunParameterGuards {
    if ($PollSeconds -lt 1) {
        throw "PollSeconds must be at least 1."
    }
    if ($SingleMaxWaitSeconds -lt $PollSeconds) {
        throw "SingleMaxWaitSeconds must be greater than or equal to PollSeconds."
    }
    if ($InitialMaxWaitSeconds -lt $PollSeconds) {
        throw "InitialMaxWaitSeconds must be greater than or equal to PollSeconds."
    }
    if ($FollowUpMaxWaitSeconds -lt $PollSeconds) {
        throw "FollowUpMaxWaitSeconds must be greater than or equal to PollSeconds."
    }
    if ($PauseSeconds -lt 0) {
        throw "PauseSeconds must be at least 0."
    }
    if ($MaxCases -lt 0) {
        throw "MaxCases must be at least 0."
    }
}

function Assert-FallbackEmulatorsAvailable {
    param($Cases)

    $needsFallback = $false
    foreach ($case in $Cases) {
        if ([string]::IsNullOrWhiteSpace([string]$case.suggested_emulator)) {
            $needsFallback = $true
            break
        }
    }
    if (-not $needsFallback) {
        return
    }

    $hasFallback = $false
    foreach ($emulator in $FallbackEmulators) {
        if (-not [string]::IsNullOrWhiteSpace([string]$emulator)) {
            $hasFallback = $true
            break
        }
    }
    if (-not $hasFallback) {
        throw "Provide at least one non-empty fallback emulator."
    }
}

function Select-Emulator {
    param(
        $Case,
        [int]$CaseIndex
    )

    $suggested = [string]$Case.suggested_emulator
    if (-not [string]::IsNullOrWhiteSpace($suggested)) {
        return $suggested
    }
    $usableFallbackEmulators = @()
    foreach ($emulator in $FallbackEmulators) {
        if (-not [string]::IsNullOrWhiteSpace([string]$emulator)) {
            $usableFallbackEmulators += ([string]$emulator).Trim()
        }
    }
    if ($usableFallbackEmulators.Count -eq 0) {
        throw "Provide at least one non-empty fallback emulator."
    }
    return $usableFallbackEmulators[$CaseIndex % $usableFallbackEmulators.Count]
}

function Get-SingleDumpSummary {
    param([string]$DumpPath)

    if (-not (Test-Path $DumpPath)) {
        return $null
    }

    try {
        $xml = [xml](Get-Content -Path $DumpPath -Raw)
    } catch {
        return $null
    }
    if ($null -eq $xml -or $null -eq $xml.DocumentElement) {
        return $null
    }

    $titleNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_title"]' | Select-Object -First 1
    $subtitleNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_subtitle"]' | Select-Object -First 1
    $bodyNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_body"]' | Select-Object -First 1
    return [pscustomobject]@{
        detail_title = if ($titleNode) { [string]$titleNode.Node.text } else { $null }
        detail_subtitle = if ($subtitleNode) { [string]$subtitleNode.Node.text } else { $null }
        detail_body = if ($bodyNode) { [string]$bodyNode.Node.text } else { $null }
    }
}

function Get-FollowUpSummary {
    param([string]$JsonPath)

    if (-not (Test-Path $JsonPath)) {
        return $null
    }

    try {
        return (Get-Content -Path $JsonPath -Raw | ConvertFrom-Json)
    } catch {
        try {
            $text = Get-Content -Path $JsonPath -Raw -Encoding UTF8
            if ($text.Length -gt 0 -and [int][char]$text[0] -eq 0xFEFF) {
                $text = $text.Substring(1)
            }
            return ($text | ConvertFrom-Json)
        } catch {
            return $null
        }
    }
}

$caseIdFilter = Normalize-FilterValues -Values $CaseIds
$familyFilter = Normalize-FilterValues -Values $Families
$priorityFilter = Normalize-FilterValues -Values $Priorities

Assert-RunParameterGuards

$cases = @(Load-GapPackCases -Path $GapPackPath)
$selectedCases = @()
foreach ($case in $cases) {
    $caseId = ([string]$case.case_id).Trim().ToLowerInvariant()
    $family = ([string]$case.family).Trim().ToLowerInvariant()
    $priority = ([string]$case.priority).Trim().ToLowerInvariant()
    if ($caseIdFilter.Count -gt 0 -and -not ($caseIdFilter -contains $caseId)) {
        continue
    }
    if ($familyFilter.Count -gt 0 -and -not ($familyFilter -contains $family)) {
        continue
    }
    if ($priorityFilter.Count -gt 0 -and -not ($priorityFilter -contains $priority)) {
        continue
    }
    $selectedCases += $case
}

if ($MaxCases -gt 0) {
    $selectedCases = @($selectedCases | Select-Object -First $MaxCases)
}
if ($selectedCases.Count -eq 0) {
    throw "No gap-pack cases matched the selected filters."
}
Assert-FallbackEmulatorsAvailable -Cases $selectedCases

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
$singleOutputDir = Join-Path $resolvedOutputDir "single"
$followUpOutputDir = Join-Path $resolvedOutputDir "followup"
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null
New-Item -ItemType Directory -Force -Path $singleOutputDir | Out-Null
New-Item -ItemType Directory -Force -Path $followUpOutputDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$manifestPath = Join-Path $resolvedOutputDir ("gap_pack_run_" + $timestamp + ".jsonl")

for ($caseIndex = 0; $caseIndex -lt $selectedCases.Count; $caseIndex++) {
    $case = $selectedCases[$caseIndex]
    $caseId = [string]$case.case_id
    $family = [string]$case.family
    $priority = [string]$case.priority
    $initialQuery = [string]$case.initial_query
    if ([string]::IsNullOrWhiteSpace($initialQuery)) {
        throw "Case $caseId is missing initial_query."
    }
    $emulator = Select-Emulator -Case $case -CaseIndex $caseIndex
    $baseLabel = (New-Slug -Text $caseId) + "_" + $emulator.Replace("-", "_")

    if ($Mode -eq "single" -or $Mode -eq "both") {
        Write-Host ("[single {0}/{1}] {2} on {3}" -f ($caseIndex + 1), $selectedCases.Count, $caseId, $emulator)
        $singleDumpPath = Join-Path $singleOutputDir ($baseLabel + ".xml")
        $singleStartedAt = Get-Date
        $singleError = $null
        try {
            & powershell -ExecutionPolicy Bypass -File $singleRunner `
                -Emulator $emulator `
                -Query $initialQuery `
                -Ask `
                -InferenceMode $InferenceMode `
                -HostInferenceUrl $HostInferenceUrl `
                -HostInferenceModel $HostInferenceModel `
                -WaitForCompletion `
                -MaxWaitSeconds $SingleMaxWaitSeconds `
                -PollSeconds $PollSeconds `
                -DumpPath $singleDumpPath `
                -ExpectedDetailTitle $initialQuery
            if (-not $?) {
                $singleError = "run_android_prompt.ps1 reported failure"
            } elseif ($LASTEXITCODE -ne 0) {
                $singleError = "run_android_prompt.ps1 exit code $LASTEXITCODE"
            }
        } catch {
            $singleError = $_.Exception.Message
            Write-Warning ("Single-turn case failed: " + $caseId + " :: " + $singleError)
        }
        $singleFinishedAt = Get-Date
        $singleSummary = Get-SingleDumpSummary -DumpPath $singleDumpPath
        if (-not $singleError -and -not (Test-Path $singleDumpPath)) {
            $singleError = "Single-turn dump was not created."
        }
        $singleRecord = [ordered]@{
            mode = "single"
            case_id = $caseId
            family = $family
            priority = $priority
            emulator = $emulator
            initial_query = $initialQuery
            dump_path = $singleDumpPath
            started_at = $singleStartedAt.ToString("o")
            finished_at = $singleFinishedAt.ToString("o")
            elapsed_seconds = [math]::Round(($singleFinishedAt - $singleStartedAt).TotalSeconds, 2)
            observed_title = if ($singleSummary) { $singleSummary.detail_title } else { $null }
            observed_subtitle = if ($singleSummary) { $singleSummary.detail_subtitle } else { $null }
            observed_body = if ($singleSummary) { $singleSummary.detail_body } else { $null }
            error = $singleError
        }
        ($singleRecord | ConvertTo-Json -Compress -Depth 6) | Add-Content -Path $manifestPath
        if ($PauseSeconds -gt 0) {
            Start-Sleep -Seconds $PauseSeconds
        }
    }

    if ($Mode -eq "followup" -or $Mode -eq "both") {
        $followUps = @()
        if ($case.follow_up_queries) {
            $followUps = @($case.follow_up_queries)
        }
        for ($followUpIndex = 0; $followUpIndex -lt $followUps.Count; $followUpIndex++) {
            $followUpQuery = [string]$followUps[$followUpIndex]
            if ([string]::IsNullOrWhiteSpace($followUpQuery)) {
                continue
            }
            $runLabel = $baseLabel + "_f" + ($followUpIndex + 1)
            Write-Host ("[followup {0}/{1}] {2} on {3} :: {4}" -f ($caseIndex + 1), $selectedCases.Count, $caseId, $emulator, $followUpQuery)
            $followUpStartedAt = Get-Date
            $followUpError = $null
            try {
                & powershell -ExecutionPolicy Bypass -File $followUpRunner `
                    -Emulator $emulator `
                    -InitialQuery $initialQuery `
                    -FollowUpQuery $followUpQuery `
                    -InferenceMode $InferenceMode `
                    -HostInferenceUrl $HostInferenceUrl `
                    -HostInferenceModel $HostInferenceModel `
                    -OutputDir $followUpOutputDir `
                    -RunLabel $runLabel `
                    -InitialMaxWaitSeconds $InitialMaxWaitSeconds `
                    -FollowUpMaxWaitSeconds $FollowUpMaxWaitSeconds `
                    -PollSeconds $PollSeconds
                if (-not $?) {
                    $followUpError = "run_android_detail_followup.ps1 reported failure"
                } elseif ($LASTEXITCODE -ne 0) {
                    $followUpError = "run_android_detail_followup.ps1 exit code $LASTEXITCODE"
                }
            } catch {
                $followUpError = $_.Exception.Message
                Write-Warning ("Follow-up case failed: " + $caseId + " :: " + $followUpError)
            }
            $followUpFinishedAt = Get-Date
            $followUpJsonPath = Join-Path $followUpOutputDir ($runLabel + ".json")
            $followUpSummary = Get-FollowUpSummary -JsonPath $followUpJsonPath
            if (-not $followUpError -and -not (Test-Path $followUpJsonPath)) {
                $followUpError = "Follow-up JSON artifact was not created."
            }
            $followUpRecord = [ordered]@{
                mode = "followup"
                case_id = $caseId
                family = $family
                priority = $priority
                emulator = $emulator
                follow_up_index = $followUpIndex + 1
                initial_query = $initialQuery
                follow_up_query = $followUpQuery
                output_json = $followUpJsonPath
                started_at = $followUpStartedAt.ToString("o")
                finished_at = $followUpFinishedAt.ToString("o")
                elapsed_seconds = [math]::Round(($followUpFinishedAt - $followUpStartedAt).TotalSeconds, 2)
                observed_initial_title = if ($followUpSummary) { $followUpSummary.initial_title } else { $null }
                observed_final_title = if ($followUpSummary) { $followUpSummary.final_detail_title } else { $null }
                observed_final_subtitle = if ($followUpSummary) { $followUpSummary.final_detail_subtitle } else { $null }
                observed_requested_follow_up = if ($followUpSummary) { $followUpSummary.requested_follow_up } else { $null }
                observed_lower_sources = if ($followUpSummary) { @($followUpSummary.lower_source_buttons) } else { @() }
                error = $followUpError
            }
            ($followUpRecord | ConvertTo-Json -Compress -Depth 6) | Add-Content -Path $manifestPath
            if ($PauseSeconds -gt 0) {
                Start-Sleep -Seconds $PauseSeconds
            }
        }
    }
}

Write-Host ("Gap-pack manifest written to " + $manifestPath)
