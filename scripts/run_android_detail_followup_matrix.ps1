param(
    [Parameter(Mandatory = $true)]
    [string]$RunFile,
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "preserve",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string]$OutputDir = "artifacts\\bench\\android_detail_followups",
    [int]$MaxParallel = 3,
    [switch]$DefaultSkipSourceProbe,
    [switch]$DefaultRequireStrictFollowUpProof,
    [switch]$StopOnError,
    [int]$InitialMaxWaitSeconds = 260,
    [int]$FollowUpMaxWaitSeconds = 180,
    [int]$PollSeconds = 5
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$loggedScript = Join-Path $PSScriptRoot "run_android_detail_followup_logged.ps1"
if (-not (Test-Path $loggedScript)) {
    throw "run_android_detail_followup_logged.ps1 not found at $loggedScript"
}

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function Load-Runs {
    param([string]$Path)

    $resolvedPath = Resolve-TargetPath -Path $Path
    if (-not (Test-Path $resolvedPath)) {
        throw "Run file not found: $resolvedPath"
    }

    $raw = Get-Content $resolvedPath -Raw
    if ([string]::IsNullOrWhiteSpace($raw)) {
        return @()
    }

    $trimmed = $raw.Trim()
    if ($trimmed.StartsWith("[") -or $trimmed.StartsWith("{")) {
        $parsed = $trimmed | ConvertFrom-Json
        if ($parsed -is [System.Collections.IEnumerable] -and -not ($parsed -is [string])) {
            return @($parsed)
        }
        return @($parsed)
    }

    $runs = @()
    foreach ($line in Get-Content $resolvedPath) {
        $trimmedLine = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmedLine)) {
            continue
        }
        $runs += ($trimmedLine | ConvertFrom-Json)
    }
    return $runs
}

function Require-RunValue {
    param(
        $Run,
        [string]$PropertyName
    )

    $value = $Run.$PropertyName
    if ($null -eq $value -or [string]::IsNullOrWhiteSpace([string]$value)) {
        throw "Run is missing required property '$PropertyName'"
    }
    return [string]$value
}

function Assert-PositiveWaitParameter {
    param(
        [string]$Name,
        [int]$Value
    )

    if ($Value -lt 1) {
        throw "$Name must be greater than or equal to 1 before forwarding to child prompt runs."
    }
}

function Assert-WaitParameters {
    Assert-PositiveWaitParameter -Name "InitialMaxWaitSeconds" -Value $InitialMaxWaitSeconds
    Assert-PositiveWaitParameter -Name "FollowUpMaxWaitSeconds" -Value $FollowUpMaxWaitSeconds
    Assert-PositiveWaitParameter -Name "PollSeconds" -Value $PollSeconds
}

function New-RunArgs {
    param($Run)

    $args = @{
        Emulator = if ([string]::IsNullOrWhiteSpace([string]$Run.emulator)) { "emulator-5554" } else { [string]$Run.emulator }
        InitialQuery = Require-RunValue -Run $Run -PropertyName "initial_query"
        FollowUpQuery = Require-RunValue -Run $Run -PropertyName "follow_up_query"
        InferenceMode = $InferenceMode
        HostInferenceUrl = $HostInferenceUrl
        HostInferenceModel = $HostInferenceModel
        OutputDir = $OutputDir
        InitialMaxWaitSeconds = $InitialMaxWaitSeconds
        FollowUpMaxWaitSeconds = $FollowUpMaxWaitSeconds
        PollSeconds = $PollSeconds
    }
    if (-not [string]::IsNullOrWhiteSpace([string]$Run.run_label)) {
        $args.RunLabel = [string]$Run.run_label
    }
    if ($DefaultSkipSourceProbe -or ($Run.PSObject.Properties.Name -contains "skip_source_probe" -and [bool]$Run.skip_source_probe)) {
        $args.SkipSourceProbe = $true
    }
    if ($DefaultRequireStrictFollowUpProof -or ($Run.PSObject.Properties.Name -contains "require_strict_followup_proof" -and [bool]$Run.require_strict_followup_proof)) {
        $args.RequireStrictFollowUpProof = $true
    }
    return $args
}

function Start-FollowupJob {
    param(
        $Run,
        [hashtable]$RunArgs
    )

    return Start-Job -ScriptBlock {
        param($WorkingDirectory, $ScriptPath, $Arguments)
        Set-Location $WorkingDirectory
        & $ScriptPath @Arguments
    } -ArgumentList $repoRoot, $loggedScript, $RunArgs
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
        try {
            $received = Receive-Job -Job $job -Keep
            if ($received) {
                $outputText = ($received | Out-String).Trim()
            }
        } catch {
            $failed = $true
            $outputText = $_.ToString()
        }

        $Results.Add([pscustomobject]@{
            emulator = $entry.runArgs.Emulator
            initial_query = $entry.runArgs.InitialQuery
            follow_up_query = $entry.runArgs.FollowUpQuery
            run_label = if ($entry.runArgs.ContainsKey("RunLabel")) { $entry.runArgs.RunLabel } else { "" }
            status = if ($failed) { "failed" } else { "completed" }
            job_state = $job.State
            output = $outputText
        }) | Out-Null

        Remove-Job -Job $job -Force | Out-Null
        $ActiveJobs.RemoveAt($index)
    }
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

Assert-WaitParameters

$runs = Load-Runs -Path $RunFile
if ($runs.Count -eq 0) {
    throw "No runs found in $RunFile"
}

$parallelLimit = [Math]::Max(1, $MaxParallel)
$activeJobs = New-Object System.Collections.ArrayList
$results = New-Object System.Collections.ArrayList

foreach ($run in $runs) {
    while ($activeJobs.Count -ge $parallelLimit) {
        Receive-CompletedJobs -ActiveJobs $activeJobs -Results $results
        Start-Sleep -Milliseconds 500
    }

    $runArgs = New-RunArgs -Run $run
    $job = Start-FollowupJob -Run $run -RunArgs $runArgs
    $activeJobs.Add([pscustomobject]@{
        job = $job
        runArgs = $runArgs
    }) | Out-Null
    Write-Host ("Started {0} on {1}" -f $runArgs.InitialQuery, $runArgs.Emulator)
}

while ($activeJobs.Count -gt 0) {
    Receive-CompletedJobs -ActiveJobs $activeJobs -Results $results
    if ($activeJobs.Count -gt 0) {
        Start-Sleep -Milliseconds 500
    }
}

$failedResults = @($results | Where-Object { $_.status -ne "completed" })
Write-Host ""
Write-Host "Matrix run summary:"
foreach ($result in $results) {
    $label = if ([string]::IsNullOrWhiteSpace($result.run_label)) {
        $result.initial_query
    } else {
        $result.run_label
    }
    Write-Host ("- [{0}] {1} on {2}" -f $result.status, $label, $result.emulator)
}

if ($failedResults.Count -gt 0 -and $StopOnError) {
    throw ("One or more matrix runs failed: " + ($failedResults | ForEach-Object {
        if ([string]::IsNullOrWhiteSpace($_.run_label)) { $_.initial_query } else { $_.run_label }
    } | Sort-Object | Join-String -Separator ", "))
}
