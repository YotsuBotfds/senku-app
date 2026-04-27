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
        baseline_pack_dir = $baselinePath
        candidate_pack_dir = $candidatePath
        output = $outputPath
        display_command = $displayCommand
        would_run = $false
        fail_on_mismatch = $true
    }

    $outputDir = Split-Path -Parent $outputPath
    if (-not [string]::IsNullOrWhiteSpace($outputDir)) {
        New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
    }
    $whatIfSummary | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $outputPath -Encoding UTF8

    Write-Host "Android asset-pack parity gate dry run."
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
& $pythonPath @arguments
$exitCode = $LASTEXITCODE
if ($exitCode -ne 0) {
    throw "Android asset-pack parity gate failed with exit code $exitCode."
}

Write-Host ("Android asset-pack parity report written to " + $outputPath)
