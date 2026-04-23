param(
    [string]$PanelPath = "",
    [string]$OutputDir = "",
    [int]$TopK = 8,
    [string]$LmStudioUrl = "",
    [string]$ChromaDbDir = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$baselineDir = Join-Path $repoRoot "artifacts\bench\abstain_baseline_20260418"
$defaultRegressionDir = Join-Path $repoRoot ("artifacts\bench\abstain_regression_{0}" -f (Get-Date -Format "yyyyMMdd"))
$helperPath = Join-Path $PSScriptRoot "abstain_regression_panel.py"

if ([string]::IsNullOrWhiteSpace($PanelPath)) {
    $PanelPath = Join-Path $baselineDir "panel.json"
}

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = $defaultRegressionDir
}

$venvPython = Join-Path $repoRoot "venv\Scripts\python.exe"
$pythonExe = if (Test-Path $venvPython) { $venvPython } else { "python" }

$arguments = @(
    $helperPath
    "--panel-path"
    $PanelPath
    "--output-dir"
    $OutputDir
    "--top-k"
    $TopK
)

if (-not [string]::IsNullOrWhiteSpace($LmStudioUrl)) {
    $arguments += @("--lm-studio-url", $LmStudioUrl)
}

if (-not [string]::IsNullOrWhiteSpace($ChromaDbDir)) {
    $arguments += @("--chroma-db-dir", $ChromaDbDir)
}

Write-Host "Running abstain regression panel..." -ForegroundColor Cyan
Write-Host "  Python: $pythonExe"
Write-Host "  Panel:  $PanelPath"
Write-Host "  Output: $OutputDir"
Write-Host "  Top-k:  $TopK"

& $pythonExe @arguments
exit $LASTEXITCODE
