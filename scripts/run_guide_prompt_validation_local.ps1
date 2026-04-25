[CmdletBinding()]
param(
    [ValidateSet('w', 'x', 'xx', 'y', 'af', 'ag', 'ah', 'ai', 'aj', 'ak', 'al', 'am', 'an', 'ao', 'ap', 'aq', 'ar', 'as', 'at', 'au', 'av', 'aw', 'ax', 'ay', 'az', 'ba', 'bb', 'bc', 'bd', 'be', 'bf', 'bg', 'bh', 'bi', 'bj', 'bk', 'bl', 'bm', 'bn', 'bo', 'bp', 'bq', 'br', 'bs', 'bt', 'bu', 'bv', 'bw', 'bx', 'by', 'bz', 'ca', 'cb', 'cd', 'ce', 'cf', 'cg', 'ch', 'ci', 'cj', 'ck', 'cl', 'cm', 'cn', 'co', 'cp', 'cq', 'cr', 'cs', 'ct', 'cu', 'cv', 'cw', 'cx', 'cy', 'cz', 'da', 'db', 'dc', 'dd', 'de', 'df', 'dg', 'dh', 'di', 'dj', 'dk', 'dl', 'dm', 'dn', 'do', 'dp', 'dq', 'dr', 'ds', 'dt', 'du', 'dv', 'dw', 'dx', 'dy', 'dz', 'ea', 'eb', 'ec', 'ed', 'ee', 'ef', 'eg', 'eh', 'ei', 'ej', 'ek', 'el', 'em', 'en', 'eo', 'ep', 'eq', 'er', 'es', 'et', 'eu', 'ev', 'ew', 'ex', 'ey', 'ez', 'fa', 'fb', 'fc', 'fd', 'fe', 'all')]
    [string]$Wave = 'be',

    [string]$VenvPath = ".venvs\senku-validate",

    [string]$GenerationUrl = "http://127.0.0.1:1235/v1",

    [string]$GenerationModel = "gemma-4-e2b-it-litert",

    [string]$EmbedUrl = "http://127.0.0.1:8801/v1",

    [int]$TopK = 8,

    [string]$OutputDir = "artifacts/bench",

    [string[]]$ExtraBenchArgs = @()
)

$ErrorActionPreference = 'Stop'

$pythonModule = Join-Path $PSScriptRoot "senku_windows_python.psm1"
Import-Module $pythonModule -Force

$repoRoot = Get-SenkuRepoRoot
$pythonPath = Resolve-SenkuPythonPath -RepoRoot $repoRoot -VenvPath $VenvPath
$validationScript = Join-Path $PSScriptRoot "run_guide_prompt_validation.ps1"

$depResult = Test-SenkuPythonDeps -PythonPath $pythonPath
if (-not $depResult.ok) {
    Write-Warning "Validation dependencies are not ready for $pythonPath."
    Write-Host $depResult.output
    Write-Host "Run setup first:"
    Write-Host "  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup_windows_validation_env.ps1 -AllowInstall"
    Write-Host ""
    Write-Host "If the sandbox blocks network access, run setup from a normal shell or provide cached Windows wheels."
    throw "Validation dependencies are not ready."
}

& $validationScript `
    -Wave $Wave `
    -PythonPath $pythonPath `
    -GenerationUrl $GenerationUrl `
    -GenerationModel $GenerationModel `
    -EmbedUrl $EmbedUrl `
    -TopK $TopK `
    -OutputDir $OutputDir `
    -ExtraBenchArgs $ExtraBenchArgs
