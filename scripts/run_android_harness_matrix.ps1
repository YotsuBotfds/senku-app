param(
    [Parameter(Mandatory = $true)]
    [string]$RunFile,
    [string]$OutputDir = "artifacts\\bench\\android_harness_matrix",
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "preserve",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string[]]$Emulators = @("emulator-5554", "emulator-5556", "emulator-5558", "emulator-5560"),
    [int]$MaxParallel = 3,
    [switch]$DefaultPromptAsk,
    [switch]$DefaultPromptWaitForCompletion,
    [switch]$DefaultSkipSourceProbe,
    [switch]$StopOnError,
    [int]$PromptWaitSeconds = 5,
    [int]$PromptMaxWaitSeconds = 180,
    [int]$PromptPollSeconds = 5,
    [int]$FollowUpInitialMaxWaitSeconds = 260,
    [int]$FollowUpMaxWaitSeconds = 180,
    [int]$FollowUpPollSeconds = 5
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$promptLoggedScript = Join-Path $PSScriptRoot "run_android_prompt_logged.ps1"
$detailLoggedScript = Join-Path $PSScriptRoot "run_android_detail_followup_logged.ps1"
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
if (-not (Test-Path $promptLoggedScript)) {
    throw "run_android_prompt_logged.ps1 not found at $promptLoggedScript"
}
if (-not (Test-Path $detailLoggedScript)) {
    throw "run_android_detail_followup_logged.ps1 not found at $detailLoggedScript"
}
if (-not (Test-Path -LiteralPath $commonHarnessModule)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}
Import-Module $commonHarnessModule -Force -DisableNameChecking
$Emulators = @(Resolve-AndroidHarnessDeviceList -Devices $Emulators)
if ($Emulators.Count -eq 0) {
    throw "No valid emulators provided."
}

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function Safe-Text {
    param([string]$Text)

    if ($null -eq $Text) {
        return ""
    }

    return [string]$Text
}

function New-Slug {
    param([string]$Text)

    $slug = (Safe-Text -Text $Text).ToLowerInvariant() -replace "[^a-z0-9]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "run"
    }
    return $slug
}

function Sanitize-RunLabel {
    param([string]$Text)

    $label = (Safe-Text -Text $Text) -replace "[^A-Za-z0-9_-]+", "_"
    $label = $label.Trim("_")
    if ([string]::IsNullOrWhiteSpace($label)) {
        return "run"
    }
    return $label
}

function Load-Runs {
    param([string]$Path)

    $resolvedPath = Resolve-TargetPath -Path $Path
    if (-not (Test-Path $resolvedPath)) {
        throw "Run file not found: $resolvedPath"
    }

    $raw = Get-Content -Path $resolvedPath -Raw
    if ([string]::IsNullOrWhiteSpace($raw)) {
        return @()
    }

    $trimmed = $raw.Trim()
    $rawLines = @($raw -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    $looksLikeSingleJsonObject = ($trimmed.StartsWith("{") -and $rawLines.Count -eq 1)
    if ($trimmed.StartsWith("[") -or $looksLikeSingleJsonObject) {
        $parsed = $trimmed | ConvertFrom-Json
        if ($parsed -is [System.Collections.IEnumerable] -and -not ($parsed -is [string])) {
            return @($parsed)
        }
        return @($parsed)
    }

    $runs = @()
    foreach ($line in Get-Content -Path $resolvedPath) {
        $safeLine = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($safeLine) -or $safeLine.StartsWith("#")) {
            continue
        }
        $runs += ($safeLine | ConvertFrom-Json)
    }
    return $runs
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

function Test-HasProperty {
    param(
        $Object,
        [string]$PropertyName
    )

    return ($Object.PSObject.Properties.Name -contains $PropertyName)
}

function Get-RunStringOrDefault {
    param(
        $Run,
        [string]$PropertyName,
        [string]$DefaultValue = ""
    )

    if (-not (Test-HasProperty -Object $Run -PropertyName $PropertyName)) {
        return $DefaultValue
    }

    $value = $Run.$PropertyName
    if ($null -eq $value -or [string]::IsNullOrWhiteSpace([string]$value)) {
        return $DefaultValue
    }

    return [string]$value
}

function Get-RunIntOrDefault {
    param(
        $Run,
        [string]$PropertyName,
        [int]$DefaultValue
    )

    if (-not (Test-HasProperty -Object $Run -PropertyName $PropertyName)) {
        return $DefaultValue
    }

    $value = $Run.$PropertyName
    if ($null -eq $value -or [string]::IsNullOrWhiteSpace([string]$value)) {
        return $DefaultValue
    }

    return [int]$value
}

function Get-RunBoolOrDefault {
    param(
        $Run,
        [string]$PropertyName,
        [bool]$DefaultValue
    )

    if (-not (Test-HasProperty -Object $Run -PropertyName $PropertyName)) {
        return $DefaultValue
    }

    $value = $Run.$PropertyName
    if ($null -eq $value) {
        return $DefaultValue
    }

    if ($value -is [bool]) {
        return $value
    }

    $stringValue = [string]$value
    if ([string]::IsNullOrWhiteSpace($stringValue)) {
        return $DefaultValue
    }

    return [System.Convert]::ToBoolean($stringValue)
}

function Get-RunMode {
    param($Run)

    $rawMode = Get-RunStringOrDefault -Run $Run -PropertyName "mode" -DefaultValue "detail_followup"
    $mode = $rawMode.ToLowerInvariant()
    switch ($mode) {
        "followup" { return "detail_followup" }
        "detail_followup" { return "detail_followup" }
        "prompt" { return "prompt" }
        default { throw "Unsupported mode '$rawMode'. Use 'prompt' or 'detail_followup'." }
    }
}

function Require-RunValue {
    param(
        $Run,
        [string]$PropertyName
    )

    $value = Get-RunStringOrDefault -Run $Run -PropertyName $PropertyName
    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Run is missing required property '$PropertyName'"
    }
    return $value
}

function Get-AssignedEmulator {
    param(
        $Run,
        [int]$Index
    )

    $explicit = Get-RunStringOrDefault -Run $Run -PropertyName "emulator"
    if (-not [string]::IsNullOrWhiteSpace($explicit)) {
        return $explicit
    }

    if ($Emulators.Count -gt 0) {
        return $Emulators[$Index % $Emulators.Count]
    }

    return "emulator-5554"
}

function New-DefaultRunLabel {
    param(
        $Run,
        [string]$Mode,
        [string]$Emulator,
        [int]$Index
    )

    $emulatorTag = $Emulator -replace "[^a-zA-Z0-9]+", "_"
    if ($Mode -eq "prompt") {
        $query = Require-RunValue -Run $Run -PropertyName "query"
        return ("row{0:D3}_{1}_{2}_{3}" -f ($Index + 1), "prompt", $emulatorTag, (New-Slug -Text $query))
    }

    $initialQuery = Require-RunValue -Run $Run -PropertyName "initial_query"
    $followUpQuery = Require-RunValue -Run $Run -PropertyName "follow_up_query"
    return ("row{0:D3}_{1}_{2}_{3}" -f ($Index + 1), "followup", $emulatorTag, (New-Slug -Text ($initialQuery + "_" + $followUpQuery)))
}

function New-RunnerSpec {
    param(
        $Run,
        [int]$Index,
        [string]$ResolvedOutputDir
    )

    $mode = Get-RunMode -Run $Run
    $emulator = Get-AssignedEmulator -Run $Run -Index $Index
    $requestedLabel = Get-RunStringOrDefault -Run $Run -PropertyName "run_label"
    $runLabel = if ([string]::IsNullOrWhiteSpace($requestedLabel)) {
        New-DefaultRunLabel -Run $Run -Mode $mode -Emulator $emulator -Index $Index
    } else {
        Sanitize-RunLabel -Text $requestedLabel
    }

    if ($mode -eq "prompt") {
        $query = Require-RunValue -Run $Run -PropertyName "query"
        $ask = Get-RunBoolOrDefault -Run $Run -PropertyName "ask" -DefaultValue ([bool]$DefaultPromptAsk)
        $waitForCompletion = Get-RunBoolOrDefault -Run $Run -PropertyName "wait_for_completion" -DefaultValue ([bool]$DefaultPromptWaitForCompletion)
        $expectedDetailTitle = Get-RunStringOrDefault -Run $Run -PropertyName "expected_detail_title"

        $args = [ordered]@{
            Emulator = $emulator
            Query = $query
            InferenceMode = $InferenceMode
            HostInferenceUrl = $HostInferenceUrl
            HostInferenceModel = $HostInferenceModel
            OutputDir = $ResolvedOutputDir
            RunLabel = $runLabel
            WaitSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "wait_seconds" -DefaultValue $PromptWaitSeconds
            MaxWaitSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "max_wait_seconds" -DefaultValue $PromptMaxWaitSeconds
            PollSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "poll_seconds" -DefaultValue $PromptPollSeconds
        }
        if ($ask) {
            $args.Ask = $true
        }
        if ($waitForCompletion) {
            $args.WaitForCompletion = $true
        }
        if ([string]::IsNullOrWhiteSpace($expectedDetailTitle) -and $ask -and $waitForCompletion) {
            $expectedDetailTitle = $query
        }
        if (-not [string]::IsNullOrWhiteSpace($expectedDetailTitle)) {
            $args.ExpectedDetailTitle = $expectedDetailTitle
        }

        return [pscustomobject]@{
            mode = $mode
            runner_path = $promptLoggedScript
            run_label = $runLabel
            manifest_path = Join-Path $ResolvedOutputDir ($runLabel + ".json")
            logcat_path = Join-Path $ResolvedOutputDir ($runLabel + ".logcat.txt")
            args = $args
        }
    }

    $args = [ordered]@{
        Emulator = $emulator
        InitialQuery = Require-RunValue -Run $Run -PropertyName "initial_query"
        FollowUpQuery = Require-RunValue -Run $Run -PropertyName "follow_up_query"
        InferenceMode = $InferenceMode
        HostInferenceUrl = $HostInferenceUrl
        HostInferenceModel = $HostInferenceModel
        OutputDir = $ResolvedOutputDir
        RunLabel = $runLabel
        InitialMaxWaitSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "initial_max_wait_seconds" -DefaultValue $FollowUpInitialMaxWaitSeconds
        FollowUpMaxWaitSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "follow_up_max_wait_seconds" -DefaultValue $FollowUpMaxWaitSeconds
        PollSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "poll_seconds" -DefaultValue $FollowUpPollSeconds
    }
    if (Get-RunBoolOrDefault -Run $Run -PropertyName "skip_source_probe" -DefaultValue ([bool]$DefaultSkipSourceProbe)) {
        $args.SkipSourceProbe = $true
    }

    return [pscustomobject]@{
        mode = $mode
        runner_path = $detailLoggedScript
        run_label = $runLabel
        manifest_path = Join-Path $ResolvedOutputDir ($runLabel + ".json")
        logcat_path = Join-Path $ResolvedOutputDir ($runLabel + ".logcat.txt")
        args = $args
    }
}

function Convert-ArgumentsToCliArray {
    param([hashtable]$Arguments)

    $cliArgs = New-Object System.Collections.Generic.List[string]
    foreach ($key in $Arguments.Keys) {
        $value = $Arguments[$key]
        if ($value -is [bool]) {
            if ($value) {
                $cliArgs.Add("-$key")
            }
            continue
        }

        $cliArgs.Add("-$key")
        $cliArgs.Add([string]$value)
    }

    return @($cliArgs.ToArray())
}

function Start-HarnessJob {
    param($RunnerSpec)

    return Start-Job -ScriptBlock {
        param($WorkingDirectory, $ScriptPath, $ScriptCliArgs)
        Set-Location $WorkingDirectory
        $ErrorActionPreference = "Continue"
        try {
            & powershell -ExecutionPolicy Bypass -File $ScriptPath @ScriptCliArgs 2>&1 | ForEach-Object { $_ }
        } catch {
            Write-Output ("SCRIPT_EXCEPTION: " + $_.Exception.Message)
        }
    } -ArgumentList $repoRoot, $RunnerSpec.runner_path, (Convert-ArgumentsToCliArray -Arguments $RunnerSpec.args)
}

function Get-ResultTitle {
    param($Manifest)

    if (-not $Manifest) {
        return $null
    }

    if ($Manifest.PSObject.Properties.Name -contains "final_detail_title") {
        return $Manifest.final_detail_title
    }

    if ($Manifest.PSObject.Properties.Name -contains "detail_title") {
        return $Manifest.detail_title
    }

    return $null
}

function Get-ResultSubtitle {
    param($Manifest)

    if (-not $Manifest) {
        return $null
    }

    if ($Manifest.PSObject.Properties.Name -contains "final_detail_subtitle") {
        return $Manifest.final_detail_subtitle
    }

    if ($Manifest.PSObject.Properties.Name -contains "detail_subtitle") {
        return $Manifest.detail_subtitle
    }

    return $null
}

function Receive-CompletedJobs {
    param(
        [System.Collections.ArrayList]$ActiveJobs,
        [System.Collections.ArrayList]$Results
    )

    for ($index = $ActiveJobs.Count - 1; $index -ge 0; $index--) {
        $entry = $ActiveJobs[$index]
        $job = $entry.job
        if ($job.State -eq "Running" -or $job.State -eq "NotStarted") {
            continue
        }

        $failed = $job.State -ne "Completed"
        $outputText = ""
        $errorText = ""
        try {
            $received = Receive-Job -Job $job -Keep
            if ($received) {
                $outputText = ($received | Out-String).Trim()
            }
        } catch {
            $failed = $true
            $outputText = $_.ToString()
        }

        $reason = $job.ChildJobs | ForEach-Object {
            if ($_.JobStateInfo.Reason) {
                $_.JobStateInfo.Reason.ToString()
            }
        } | Select-Object -First 1
        $jobErrors = $job.ChildJobs | ForEach-Object {
            if ($_.Error.Count -gt 0) {
                ($_.Error | Out-String).Trim()
            }
        } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        if ($jobErrors.Count -gt 0) {
            $errorText = ($jobErrors -join [Environment]::NewLine)
        } elseif (-not [string]::IsNullOrWhiteSpace($reason)) {
            $errorText = $reason
        }

        $manifest = Read-RunJson -Path $entry.runner.manifest_path
        $manifestError = $null
        if ($manifest -and ($manifest.PSObject.Properties.Name -contains "error_message")) {
            $manifestError = [string]$manifest.error_message
        }
        if (-not $manifest) {
            $failed = $true
        } elseif (-not [string]::IsNullOrWhiteSpace($manifestError)) {
            $failed = $true
        }

        $Results.Add([pscustomobject]@{
            mode = $entry.runner.mode
            emulator = $entry.runner.args.Emulator
            run_label = $entry.runner.run_label
            status = if ($failed) { "failed" } else { "completed" }
            job_state = $job.State
            manifest_path = $entry.runner.manifest_path
            logcat_path = $entry.runner.logcat_path
            final_title = Get-ResultTitle -Manifest $manifest
            final_subtitle = Get-ResultSubtitle -Manifest $manifest
            error = if (-not [string]::IsNullOrWhiteSpace($manifestError)) { $manifestError } else { $errorText }
            output = $outputText
        }) | Out-Null

        Remove-Job -Job $job -Force | Out-Null
        $ActiveJobs.RemoveAt($index)
    }
}

function New-MatrixSummary {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Results,
        [Parameter(Mandatory = $true)]
        [string]$SummaryJsonlPath,
        [Parameter(Mandatory = $true)]
        [string]$SummaryCsvPath
    )

    $completed = @($Results | Where-Object { $_.status -eq "completed" })
    $failed = @($Results | Where-Object { $_.status -ne "completed" })
    $statusGroups = @()
    foreach ($statusName in @("completed", "failed")) {
        $source = if ($statusName -eq "completed") { $completed } else { $failed }
        $statusGroups += [ordered]@{
            status = $statusName
            count = $source.Count
            run_labels = @($source | ForEach-Object { $_.run_label })
        }
    }

    $emulatorGroups = @()
    foreach ($group in @($Results | Group-Object -Property emulator | Sort-Object -Property Name)) {
        $emulator = [string]$group.Name
        $emulatorResults = @($group.Group)
        $emulatorCompleted = @($emulatorResults | Where-Object { $_.status -eq "completed" })
        $emulatorFailed = @($emulatorResults | Where-Object { $_.status -ne "completed" })
        $emulatorStatusGroups = @()
        foreach ($statusName in @("completed", "failed")) {
            $source = if ($statusName -eq "completed") { $emulatorCompleted } else { $emulatorFailed }
            $emulatorStatusGroups += [ordered]@{
                status = $statusName
                count = $source.Count
                run_labels = @($source | ForEach-Object { $_.run_label })
            }
        }

        $emulatorGroups += [ordered]@{
            emulator = $emulator
            total = $emulatorResults.Count
            completed = $emulatorCompleted.Count
            failed = $emulatorFailed.Count
            status_groups = $emulatorStatusGroups
        }
    }

    $failedItems = @($failed | ForEach-Object {
        $artifactPaths = @()
        if (Test-Path -LiteralPath $_.manifest_path) {
            $artifactPaths += [ordered]@{
                kind = "manifest_json"
                path = $_.manifest_path
            }
        }
        if (Test-Path -LiteralPath $_.logcat_path) {
            $artifactPaths += [ordered]@{
                kind = "logcat"
                path = $_.logcat_path
            }
        }

        [ordered]@{
            run_label = $_.run_label
            mode = $_.mode
            emulator = $_.emulator
            error = $_.error
            artifact_paths = $artifactPaths
        }
    })

    $artifactPaths = @(
        [ordered]@{ kind = "jsonl"; path = $SummaryJsonlPath }
        [ordered]@{ kind = "csv"; path = $SummaryCsvPath }
    )
    foreach ($result in @($Results)) {
        if (Test-Path -LiteralPath $result.manifest_path) {
            $artifactPaths += [ordered]@{
                kind = "manifest_json"
                run_label = $result.run_label
                emulator = $result.emulator
                path = $result.manifest_path
            }
        }
        if (Test-Path -LiteralPath $result.logcat_path) {
            $artifactPaths += [ordered]@{
                kind = "logcat"
                run_label = $result.run_label
                emulator = $result.emulator
                path = $result.logcat_path
            }
        }
    }

    return [ordered]@{
        total = $Results.Count
        completed = $completed.Count
        failed = $failed.Count
        status_groups = $statusGroups
        emulator_groups = $emulatorGroups
        failed_items = $failedItems
        artifact_paths = $artifactPaths
        summary_jsonl_path = $SummaryJsonlPath
        summary_csv_path = $SummaryCsvPath
    }
}

function ConvertTo-MatrixSummaryMarkdown {
    param([Parameter(Mandatory = $true)][object]$Summary)

    $lines = @(
        "# Android Harness Matrix Summary",
        "",
        "- total: $($Summary.total)",
        "- completed: $($Summary.completed)",
        "- failed: $($Summary.failed)",
        "- summary_jsonl_path: $($Summary.summary_jsonl_path)",
        "- summary_csv_path: $($Summary.summary_csv_path)",
        "",
        "## Status Groups"
    )

    foreach ($group in $Summary.status_groups) {
        $runLabels = if ($group.run_labels.Count -gt 0) { ($group.run_labels -join ", ") } else { "-" }
        $lines += "- $($group.status): $($group.count) ($runLabels)"
    }

    $lines += @("", "## Emulator Groups")
    foreach ($group in $Summary.emulator_groups) {
        $lines += "- emulator=$($group.emulator) total=$($group.total) completed=$($group.completed) failed=$($group.failed)"
        foreach ($statusGroup in $group.status_groups) {
            $runLabels = if ($statusGroup.run_labels.Count -gt 0) { ($statusGroup.run_labels -join ", ") } else { "-" }
            $lines += "  - $($statusGroup.status): $($statusGroup.count) ($runLabels)"
        }
    }

    $lines += @("", "## Failed Items")
    if ($Summary.failed_items.Count -eq 0) {
        $lines += "- none"
    } else {
        foreach ($item in $Summary.failed_items) {
            $lines += "- run_label=$($item.run_label) mode=$($item.mode) emulator=$($item.emulator) error=$($item.error)"
            foreach ($path in $item.artifact_paths) {
                $lines += "  - $($path.kind): $($path.path)"
            }
        }
    }

    $lines += @("", "## Artifact Paths")
    foreach ($artifact in $Summary.artifact_paths) {
        $suffix = ""
        if ($artifact.PSObject.Properties.Name -contains "run_label") {
            $suffix = " run_label=$($artifact.run_label)"
        }
        if ($artifact.PSObject.Properties.Name -contains "emulator") {
            $suffix = "$suffix emulator=$($artifact.emulator)"
        }
        $lines += "- kind=$($artifact.kind)$suffix path=$($artifact.path)"
    }

    return ($lines -join [Environment]::NewLine) + [Environment]::NewLine
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$runs = Load-Runs -Path $RunFile
if ($runs.Count -eq 0) {
    throw "No runs found in $RunFile"
}

$parallelLimit = [Math]::Max(1, $MaxParallel)
$activeJobs = New-Object System.Collections.ArrayList
$results = New-Object System.Collections.ArrayList

foreach ($runIndex in 0..($runs.Count - 1)) {
    $run = $runs[$runIndex]
    while ($activeJobs.Count -ge $parallelLimit) {
        Receive-CompletedJobs -ActiveJobs $activeJobs -Results $results
        Start-Sleep -Milliseconds 500
    }

    $runnerSpec = New-RunnerSpec -Run $run -Index $runIndex -ResolvedOutputDir $resolvedOutputDir
    $job = Start-HarnessJob -RunnerSpec $runnerSpec
    $activeJobs.Add([pscustomobject]@{
        job = $job
        runner = $runnerSpec
    }) | Out-Null
    Write-Host ("Started [{0}] {1} on {2}" -f $runnerSpec.mode, $runnerSpec.run_label, $runnerSpec.args.Emulator)
}

while ($activeJobs.Count -gt 0) {
    Receive-CompletedJobs -ActiveJobs $activeJobs -Results $results
    if ($activeJobs.Count -gt 0) {
        Start-Sleep -Milliseconds 500
    }
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$summaryJsonlPath = Join-Path $resolvedOutputDir ("android_harness_matrix_" + $timestamp + ".jsonl")
$summaryCsvPath = Join-Path $resolvedOutputDir ("android_harness_matrix_" + $timestamp + ".csv")
$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"

foreach ($result in $results) {
    ($result | ConvertTo-Json -Compress) | Add-Content -Path $summaryJsonlPath
}
$results | Export-Csv -Path $summaryCsvPath -NoTypeInformation -Encoding utf8

$summary = New-MatrixSummary -Results $results -SummaryJsonlPath $summaryJsonlPath -SummaryCsvPath $summaryCsvPath
($summary | ConvertTo-Json -Depth 10) | Set-Content -Path $summaryJsonPath -Encoding UTF8
ConvertTo-MatrixSummaryMarkdown -Summary $summary | Set-Content -Path $summaryMarkdownPath -Encoding UTF8

$failedResults = @($results | Where-Object { $_.status -ne "completed" })
Write-Host ""
Write-Host "Harness matrix summary:"
foreach ($result in $results) {
    Write-Host ("- [{0}] {1} ({2}) on {3}" -f $result.status, $result.run_label, $result.mode, $result.emulator)
}
Write-Host ("Summary JSONL written to " + $summaryJsonlPath)
Write-Host ("Summary CSV written to " + $summaryCsvPath)
Write-Host ("Summary JSON written to " + $summaryJsonPath)
Write-Host ("Summary Markdown written to " + $summaryMarkdownPath)

if ($failedResults.Count -gt 0 -and $StopOnError) {
    $labels = @($failedResults | ForEach-Object { $_.run_label })
    throw ("One or more matrix runs failed: " + ($labels -join ", "))
}
