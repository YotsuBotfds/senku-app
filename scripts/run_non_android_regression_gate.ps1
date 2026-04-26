[CmdletBinding()]
param(
    [string]$Label = "",

    [ValidateSet('Fast', 'Generated', 'All')]
    [string]$Mode = 'Fast',

    [string]$VenvPath = ".venvs\senku-validate",

    [int]$TopK = 8,

    [switch]$IncludeSafetyCritical,

    [switch]$AllowRetrievalWarnings,

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
        [bool]$StrictWarnings
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

Push-Location $repoRoot
try {
    if ($WhatIf) {
        Write-Host "Non-Android regression gate dry run. Label: $runLabel"
        foreach ($item in $commandsToRun) {
            Write-Host ("[{0}] {1}" -f $item.Name, $item.Display)
        }
        return
    }

    foreach ($item in $commandsToRun) {
        Invoke-GateCommand -Command $item
    }
} finally {
    Pop-Location
}
