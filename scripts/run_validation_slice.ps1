[CmdletBinding()]
param(
    [string[]]$Slice = @(),

    [string[]]$Command = @(),

    [string]$VenvPath = ".venvs\senku-validate",

    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'

$pythonModule = Join-Path $PSScriptRoot "senku_windows_python.psm1"
Import-Module $pythonModule -Force

function New-ValidationCommand {
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
        Display = Format-ValidationCommand -Executable $Executable -Arguments $Arguments
    }
}

function Quote-ValidationArgument {
    param(
        [Parameter(Mandatory)]
        [string]$Value
    )

    if ($Value -match '^[A-Za-z0-9_./:\\-]+$') {
        return $Value
    }

    return "'" + ($Value -replace "'", "''") + "'"
}

function Format-ValidationCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Executable,

        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    $parts = @("&", (Quote-ValidationArgument $Executable))
    $parts += $Arguments | ForEach-Object { Quote-ValidationArgument $_ }
    return ($parts -join ' ')
}

function Get-ValidationSliceCommands {
    param(
        [Parameter(Mandatory)]
        [string]$SliceName,

        [Parameter(Mandatory)]
        [string]$PythonPath
    )

    $normalized = $SliceName.Trim().ToLowerInvariant()
    switch ($normalized) {
        'toolingcontextindexes' {
            $testModules = @(
                'tests.test_agent_context_snapshot',
                'tests.test_index_bench_artifacts',
                'tests.test_index_dispatch_notes',
                'tests.test_index_prompt_packs',
                'tests.test_summarize_run_manifest',
                'tests.test_write_run_manifest',
                'tests.test_summarize_worktree_delta',
                'tests.test_query_rag_diagnostics',
                'tests.test_compact_rag_context',
                'tests.test_guide_edit_impact.GuideEditImpactTests.test_from_git_status_handles_renames_and_untracked_files',
                'tests.test_guide_edit_impact.GuideEditImpactTests.test_json_cli_output_is_parseable'
            )
            return @(
                New-ValidationCommand `
                    -Name 'ToolingContextIndexes.unit' `
                    -Executable $PythonPath `
                    -Arguments (@('-B', '-m', 'unittest') + $testModules + @('-v'))
            )
        }
        'ragevalpostfixes' {
            $queryCommand = New-ValidationCommand `
                -Name 'RAGEvalPostFixes.query_retrieval_miss' `
                -Executable $PythonPath `
                -Arguments @(
                    '-B',
                    'scripts\query_rag_diagnostics.py',
                    'artifacts\bench\rag_eval_partial_router_holdouts_20260425_post_fixes_diag',
                    '--bucket',
                    'retrieval_miss',
                    '--guide-id',
                    'GD-634'
                )
            $compactCommand = New-ValidationCommand `
                -Name 'RAGEvalPostFixes.compact_context' `
                -Executable $PythonPath `
                -Arguments @(
                    '-B',
                    'scripts\compact_rag_context.py',
                    'artifacts\bench\rag_eval_partial_router_holdouts_20260425_post_fixes_diag',
                    '--max-rows',
                    '5'
                )
            return @($queryCommand, $compactCommand)
        }
        default {
            throw "Unknown validation slice '$SliceName'. Known slices: ToolingContextIndexes, RAGEvalPostFixes."
        }
    }
}

function Get-AdhocValidationCommands {
    param(
        [string[]]$CommandList
    )

    $index = 0
    foreach ($item in $CommandList) {
        if ([string]::IsNullOrWhiteSpace($item)) {
            throw "-Command entries must not be empty."
        }
        $index += 1
        [pscustomobject]@{
            Name = "AdHoc.$index"
            CommandText = $item
            Display = $item
        }
    }
}

function Invoke-ValidationCommand {
    param(
        [Parameter(Mandatory)]
        [pscustomobject]$ValidationCommand
    )

    Write-Host ""
    Write-Host ("=== {0} ===" -f $ValidationCommand.Name)
    Write-Host $ValidationCommand.Display

    if ($ValidationCommand.PSObject.Properties.Name -contains 'CommandText') {
        powershell -NoProfile -ExecutionPolicy Bypass -Command $ValidationCommand.CommandText
    } else {
        & $ValidationCommand.Executable @($ValidationCommand.Arguments)
    }
    if ($LASTEXITCODE -ne 0) {
        throw "Validation command '$($ValidationCommand.Name)' failed with exit code $LASTEXITCODE."
    }
}

$repoRoot = Get-SenkuRepoRoot
Initialize-SenkuUvCache -RepoRoot $repoRoot | Out-Null
$pythonPath = Resolve-SenkuPythonPath -RepoRoot $repoRoot -VenvPath $VenvPath

$commandsToRun = @()
foreach ($sliceName in ($Slice | ForEach-Object { $_ -split ',' })) {
    if ([string]::IsNullOrWhiteSpace($sliceName)) {
        throw "-Slice entries must not be empty."
    }
    $commandsToRun += Get-ValidationSliceCommands -SliceName $sliceName -PythonPath $pythonPath
}
$commandsToRun += Get-AdhocValidationCommands -CommandList $Command

if ($commandsToRun.Count -eq 0) {
    throw "No validation commands selected. Use -Slice ToolingContextIndexes, -Slice RAGEvalPostFixes, or -Command."
}

Push-Location $repoRoot
try {
    if ($WhatIf) {
        Write-Host "Validation slice dry run. Commands that would run:"
        foreach ($item in $commandsToRun) {
            Write-Host ("[{0}] {1}" -f $item.Name, $item.Display)
        }
        return
    }

    foreach ($item in $commandsToRun) {
        Invoke-ValidationCommand -ValidationCommand $item
    }
} finally {
    Pop-Location
}
