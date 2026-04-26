[CmdletBinding()]
param(
    [string]$Label = "",

    [ValidateSet('Fast', 'Generated', 'All')]
    [string]$Mode = 'Fast',

    [string]$VenvPath = ".venvs\senku-validate",

    [int]$TopK = 8,

    [switch]$IncludeSafetyCritical,

    [switch]$AllowRetrievalWarnings,

    [string]$AllowedDriftManifest = "notes\specs\partial_router_allowed_drift_20260426.json",

    [string]$GeneratedBenchJson = "",

    [switch]$FailOnGeneratedRegression,

    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'

$pythonModule = Join-Path $PSScriptRoot "senku_windows_python.psm1"
Import-Module $pythonModule -Force

function Quote-GateArgument {
    param(
        [Parameter(Mandatory)]
        [string]$Value
    )

    if ($Value -match '^[A-Za-z0-9_./:\\-]+$') {
        return $Value
    }

    return "'" + ($Value -replace "'", "''") + "'"
}

function Format-GateCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Executable,

        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    $parts = @("&", (Quote-GateArgument $Executable))
    $parts += $Arguments | ForEach-Object { Quote-GateArgument $_ }
    return ($parts -join ' ')
}

function New-GateCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Name,

        [Parameter(Mandatory)]
        [string]$Executable,

        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    [pscustomobject]@{
        Name = $Name
        Executable = $Executable
        Arguments = $Arguments
        Display = Format-GateCommand -Executable $Executable -Arguments $Arguments
    }
}

function Add-FastGateCommands {
    param(
        [System.Collections.ArrayList]$Commands,

        [Parameter(Mandatory)]
        [string]$PythonPath,

        [Parameter(Mandatory)]
        [string]$PackPath,

        [Parameter(Mandatory)]
        [string]$BenchStem,

        [Parameter(Mandatory)]
        [string]$CommandPrefix,

        [Parameter(Mandatory)]
        [string]$RunLabel,

        [Parameter(Mandatory)]
        [int]$RetrievalTopK,

        [Parameter(Mandatory)]
        [bool]$StrictWarnings,

        [string]$AllowedDriftManifest = ""
    )

    $retrievalJson = "artifacts\bench\${BenchStem}_${RunLabel}_retrieval_only.json"
    $retrievalMd = "artifacts\bench\${BenchStem}_${RunLabel}_retrieval_only.md"

    [void]$Commands.Add((New-GateCommand `
        -Name "$CommandPrefix.prompt_expectations" `
        -Executable $PythonPath `
        -Arguments @('-B', 'scripts\validate_prompt_expectations.py', $PackPath, '--fail-on-errors')))

    [void]$Commands.Add((New-GateCommand `
        -Name "$CommandPrefix.retrieval_eval" `
        -Executable $PythonPath `
        -Arguments @(
            '-B',
            'scripts\evaluate_retrieval_pack.py',
            $PackPath,
            '--top-k',
            "$RetrievalTopK",
            '--output-json',
            $retrievalJson,
            '--output-md',
            $retrievalMd,
            '--progress'
        )))

    $validationArgs = @(
        '-B',
        'scripts\validate_prompt_expectations.py',
        $PackPath,
        '--retrieval-eval',
        $retrievalJson,
        '--retrieval-top-k',
        "$RetrievalTopK",
        '--fail-on-errors'
    )
    if (-not [string]::IsNullOrWhiteSpace($AllowedDriftManifest)) {
        $validationArgs += @('--allowed-drift-manifest', $AllowedDriftManifest)
    }
    if ($StrictWarnings) {
        $validationArgs += '--fail-on-warnings'
    }

    [void]$Commands.Add((New-GateCommand `
        -Name "$CommandPrefix.retrieval_expectations" `
        -Executable $PythonPath `
        -Arguments $validationArgs))
}

function Add-GeneratedGateCommands {
    param(
        [System.Collections.ArrayList]$Commands,

        [Parameter(Mandatory)]
        [string]$PythonPath,

        [Parameter(Mandatory)]
        [string]$RunLabel,

        [string]$BenchJson,

        [Parameter(Mandatory)]
        [bool]$FailOnRegression
    )

    if ([string]::IsNullOrWhiteSpace($BenchJson)) {
        throw "-GeneratedBenchJson is required when -Mode Generated or -Mode All is selected."
    }

    $baselineDiag = "artifacts\bench\rag_eval_partial_router_holdouts_20260425_gd397_expectation_cleanup_diag"
    $candidateDiag = "artifacts\bench\rag_eval_partial_router_holdouts_20260425_${RunLabel}_diag"

    [void]$Commands.Add((New-GateCommand `
        -Name 'Generated.failure_analysis' `
        -Executable $PythonPath `
        -Arguments @(
            '-B',
            'scripts\analyze_rag_bench_failures.py',
            $BenchJson,
            '--output-dir',
            $candidateDiag
        )))

    $gateArgs = @(
        '-B',
        'scripts\rag_regression_gate.py',
        $baselineDiag,
        $candidateDiag,
        '--format',
        'text'
    )
    if ($FailOnRegression) {
        $gateArgs += '--fail-on-regression'
    }

    [void]$Commands.Add((New-GateCommand `
        -Name 'Generated.regression_gate' `
        -Executable $PythonPath `
        -Arguments $gateArgs))
}

function Invoke-GateCommand {
    param(
        [Parameter(Mandatory)]
        [pscustomobject]$Command
    )

    Write-Host ""
    Write-Host ("=== {0} ===" -f $Command.Name)
    Write-Host $Command.Display

    & $Command.Executable @($Command.Arguments)
    if ($LASTEXITCODE -ne 0) {
        throw "Regression gate command '$($Command.Name)' failed with exit code $LASTEXITCODE."
    }
}

function Add-GateSummaryLine {
    param(
        [System.Collections.Generic.List[string]]$Lines,

        [AllowNull()]
        [string]$Value = ""
    )

    [void]$Lines.Add($Value)
}

function Write-GateStepSummary {
    param(
        [Parameter(Mandatory)]
        [string]$Status,

        [Parameter(Mandatory)]
        [string]$RunLabel,

        [Parameter(Mandatory)]
        [string]$SelectedMode,

        [Parameter(Mandatory)]
        [pscustomobject[]]$Commands,

        [Parameter(Mandatory)]
        [datetime]$StartedAt,

        [string]$ErrorMessage = ""
    )

    if ([string]::IsNullOrWhiteSpace($env:GITHUB_STEP_SUMMARY)) {
        return
    }

    $finishedAt = Get-Date
    $elapsed = New-TimeSpan -Start $StartedAt -End $finishedAt
    $lines = [System.Collections.Generic.List[string]]::new()
    Add-GateSummaryLine $lines "## Non-Android Regression Gate"
    Add-GateSummaryLine $lines ""
    Add-GateSummaryLine $lines ("- Status: {0}" -f $Status)
    Add-GateSummaryLine $lines ('- Label: `{0}`' -f $RunLabel)
    Add-GateSummaryLine $lines ('- Mode: `{0}`' -f $SelectedMode)
    Add-GateSummaryLine $lines ("- Commands: {0}" -f $Commands.Count)
    Add-GateSummaryLine $lines ("- Duration: {0:n1}s" -f $elapsed.TotalSeconds)

    if (-not [string]::IsNullOrWhiteSpace($ErrorMessage)) {
        Add-GateSummaryLine $lines ('- Error: `{0}`' -f ($ErrorMessage -replace '\r?\n', ' '))
    }

    Add-GateSummaryLine $lines ""
    Add-GateSummaryLine $lines "### Planned Commands"
    foreach ($item in $Commands) {
        Add-GateSummaryLine $lines ('- `{0}`' -f $item.Name)
    }

    $benchPath = Join-Path $repoRoot "artifacts\bench"
    if (Test-Path -LiteralPath $benchPath) {
        $benchOutputs = Get-ChildItem -LiteralPath $benchPath -Recurse -File -Include *.json, *.md -ErrorAction SilentlyContinue
        Add-GateSummaryLine $lines ""
        Add-GateSummaryLine $lines ('Bench artifact candidates: {0} JSON/Markdown file(s) under `artifacts/bench`.' -f @($benchOutputs).Count)
    }

    Add-Content -LiteralPath $env:GITHUB_STEP_SUMMARY -Value $lines
}

$repoRoot = Get-SenkuRepoRoot
Initialize-SenkuUvCache -RepoRoot $repoRoot | Out-Null
$pythonPath = Resolve-SenkuPythonPath -RepoRoot $repoRoot -VenvPath $VenvPath

$runLabel = $Label
if ([string]::IsNullOrWhiteSpace($runLabel)) {
    $runLabel = "local_" + (Get-Date -Format "yyyyMMdd_HHmmss")
}
if ($runLabel -notmatch '^[A-Za-z0-9_.-]+$') {
    throw "-Label must contain only letters, numbers, underscore, dot, or dash."
}

$commandsToRun = [System.Collections.ArrayList]::new()
$strictWarnings = -not $AllowRetrievalWarnings

if ($Mode -in @('Fast', 'All')) {
    Add-FastGateCommands `
        -Commands $commandsToRun `
        -PythonPath $pythonPath `
        -PackPath 'artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl' `
        -BenchStem 'rag_eval_partial_router_holdouts_20260425' `
        -CommandPrefix 'PartialRouter' `
        -RunLabel $runLabel `
        -RetrievalTopK $TopK `
        -StrictWarnings $strictWarnings `
        -AllowedDriftManifest $AllowedDriftManifest

    Add-FastGateCommands `
        -Commands $commandsToRun `
        -PythonPath $pythonPath `
        -PackPath 'artifacts\prompts\adhoc\rag_eval9_high_liability_compound_holdouts_20260426.jsonl' `
        -BenchStem 'rag_eval9_high_liability_compound_holdouts_20260426' `
        -CommandPrefix 'Eval9Primary' `
        -RunLabel $runLabel `
        -RetrievalTopK $TopK `
        -StrictWarnings $strictWarnings

    if ($IncludeSafetyCritical) {
        Add-FastGateCommands `
            -Commands $commandsToRun `
            -PythonPath $pythonPath `
            -PackPath 'artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl' `
            -BenchStem 'rag_eval_high_liability_compound_holdouts_20260425' `
            -CommandPrefix 'SafetyCritical' `
            -RunLabel $runLabel `
            -RetrievalTopK $TopK `
            -StrictWarnings $false
    }
}

if ($Mode -in @('Generated', 'All')) {
    Add-GeneratedGateCommands `
        -Commands $commandsToRun `
        -PythonPath $pythonPath `
        -RunLabel $runLabel `
        -BenchJson $GeneratedBenchJson `
        -FailOnRegression $FailOnGeneratedRegression
}

$gateStartedAt = Get-Date
$gateStatus = 'success'
$gateError = ''

Push-Location $repoRoot
try {
    if ($WhatIf) {
        $gateStatus = 'what-if'
        Write-Host "Non-Android regression gate dry run. Label: $runLabel"
        foreach ($item in $commandsToRun) {
            Write-Host ("[{0}] {1}" -f $item.Name, $item.Display)
        }
        return
    }

    foreach ($item in $commandsToRun) {
        Invoke-GateCommand -Command $item
    }
} catch {
    $gateStatus = 'failed'
    $gateError = $_.Exception.Message
    throw
} finally {
    Pop-Location
    Write-GateStepSummary `
        -Status $gateStatus `
        -RunLabel $runLabel `
        -SelectedMode $Mode `
        -Commands @($commandsToRun) `
        -StartedAt $gateStartedAt `
        -ErrorMessage $gateError
}
