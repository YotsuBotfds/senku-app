[CmdletBinding()]
param(
    [string]$BaselinePackDir = "android-app\app\src\main\assets\mobile_pack",

    [Parameter(Mandatory = $true)]
    [string]$CandidatePackDir,

    [string]$Output = "artifacts\bench\android_asset_pack_parity_gate.json",

    [string]$VenvPath = ".venvs\senku-validate",

    [switch]$WhatIf
)

$ErrorActionPreference = "Stop"

$pythonModule = Join-Path $PSScriptRoot "senku_windows_python.psm1"
Import-Module $pythonModule -Force

function Resolve-GatePath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        [string]$RepoRoot
    )

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $RepoRoot $Path)
}

function Quote-GateArgument {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Value
    )

    if ($Value -match "^[A-Za-z0-9_./:\\-]+$") {
        return $Value
    }

    return "'" + ($Value -replace "'", "''") + "'"
}

function Format-GateCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Executable,

        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $parts = @("&", (Quote-GateArgument -Value $Executable))
    $parts += $Arguments | ForEach-Object { Quote-GateArgument -Value $_ }
    return ($parts -join " ")
}

function Get-PackHighlights {
    param(
        [Parameter(Mandatory = $true)]
        [object]$PackReport
    )

    $highlights = [ordered]@{}
    if ($null -ne $PackReport.sqlite_status) {
        $highlights.sqlite_status = $PackReport.sqlite_status
    }
    if ($null -ne $PackReport.manifest_counts) {
        $highlights.manifest_counts = $PackReport.manifest_counts
    }
    if ($null -ne $PackReport.sqlite_counts) {
        $highlights.sqlite_counts = $PackReport.sqlite_counts
    }
    if ($null -ne $PackReport.sqlite_file) {
        $sqliteFile = [ordered]@{}
        foreach ($field in @("sha256", "bytes", "lfs_oid_sha256", "lfs_size", "pointer_matches_manifest")) {
            if ($null -ne $PackReport.sqlite_file.$field) {
                $sqliteFile.$field = $PackReport.sqlite_file.$field
            }
        }
        if ($sqliteFile.Count -gt 0) {
            $highlights.sqlite_file = $sqliteFile
        }
    }
    return $highlights
}

$repoRoot = Get-SenkuRepoRoot
Initialize-SenkuUvCache -RepoRoot $repoRoot | Out-Null
$pythonPath = Resolve-SenkuPythonPath -RepoRoot $repoRoot -VenvPath $VenvPath

if ([string]::IsNullOrWhiteSpace($CandidatePackDir)) {
    throw "-CandidatePackDir is required."
}
if ([string]::IsNullOrWhiteSpace($BaselinePackDir)) {
    throw "-BaselinePackDir must not be blank."
}
if ([string]::IsNullOrWhiteSpace($Output)) {
    throw "-Output must not be blank."
}

$baselinePath = Resolve-GatePath -Path $BaselinePackDir -RepoRoot $repoRoot
$candidatePath = Resolve-GatePath -Path $CandidatePackDir -RepoRoot $repoRoot
$outputPath = Resolve-GatePath -Path $Output -RepoRoot $repoRoot
$comparisonBaseline = "fixed_four_emulator_matrix"
$stopLine = "STOP: fixed four-emulator evidence remains primary; this asset-pack parity dry run is non-acceptance evidence only."

if (-not (Test-Path -LiteralPath $baselinePath)) {
    throw "Baseline pack path not found: $baselinePath"
}
if (-not (Test-Path -LiteralPath $candidatePath)) {
    throw "Candidate pack path not found: $candidatePath"
}

$arguments = @(
    "-B",
    "scripts\compare_mobile_pack_counts.py",
    $baselinePath,
    $candidatePath,
    "--fail-on-mismatch",
    "--output",
    $outputPath
)
$displayCommand = Format-GateCommand -Executable $pythonPath -Arguments $arguments

if ($WhatIf) {
    $whatIfSummary = [ordered]@{
        status = "dry_run_only"
        baseline_pack_dir = $baselinePath
        candidate_pack_dir = $candidatePath
        output = $outputPath
        display_command = $displayCommand
        dry_run = $true
        non_acceptance_evidence = $true
        acceptance_evidence = $false
        comparison_baseline = $comparisonBaseline
        primary_evidence = $comparisonBaseline
        stop_line = $stopLine
        would_run = $false
        fail_on_mismatch = $true
    }

    $outputDir = Split-Path -Parent $outputPath
    if (-not [string]::IsNullOrWhiteSpace($outputDir)) {
        New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
    }
    $whatIfSummary | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $outputPath -Encoding UTF8

    Write-Host "Android asset-pack parity gate dry run."
    Write-Host $stopLine
    Write-Host ("Baseline pack: " + $baselinePath)
    Write-Host ("Candidate pack: " + $candidatePath)
    Write-Host ("Output: " + $outputPath)
    Write-Host $displayCommand
    Write-Host ("Android asset-pack parity dry-run summary written to " + $outputPath)
    return
}

$outputDir = Split-Path -Parent $outputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir)) {
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
}

Write-Host "Android asset-pack parity gate."
Write-Host $displayCommand
$rawOutputPath = $outputPath + ".compare_mobile_pack_counts.json"
$rawArguments = @(
    "-B",
    "scripts\compare_mobile_pack_counts.py",
    $baselinePath,
    $candidatePath,
    "--fail-on-mismatch",
    "--output",
    $rawOutputPath
)
& $pythonPath @rawArguments
$exitCode = $LASTEXITCODE
$compareReport = $null
if (Test-Path -LiteralPath $rawOutputPath) {
    $compareReport = Get-Content -LiteralPath $rawOutputPath -Raw -Encoding UTF8 | ConvertFrom-Json
}

$gateReport = [ordered]@{
    status = if ($exitCode -eq 0) { "pass" } else { "fail" }
    baseline_pack_dir = $baselinePath
    candidate_pack_dir = $candidatePath
    output = $outputPath
    fail_on_mismatch = $true
    dry_run = $false
    non_acceptance_evidence = $true
    acceptance_evidence = $false
    comparison_baseline = $comparisonBaseline
    primary_evidence = $comparisonBaseline
    stop_line = $stopLine
}
if ($null -ne $compareReport) {
    $gateReport.count_deltas = $compareReport.count_deltas
    $gateReport.baseline_highlights = Get-PackHighlights -PackReport $compareReport.baseline
    $gateReport.candidate_highlights = Get-PackHighlights -PackReport $compareReport.candidate
}
$gateReport | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $outputPath -Encoding UTF8

if ($exitCode -ne 0) {
    throw "Android asset-pack parity gate failed with exit code $exitCode."
}

Write-Host ("Android asset-pack parity report written to " + $outputPath)
