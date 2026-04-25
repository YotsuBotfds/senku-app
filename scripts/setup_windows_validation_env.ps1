[CmdletBinding()]
param(
    [string]$VenvPath = ".venvs\senku-validate",

    [string]$PythonVersion = "3.13",

    [string]$RequirementsPath = "requirements.txt",

    [switch]$AllowInstall,

    [switch]$Offline
)

$ErrorActionPreference = 'Stop'

$pythonModule = Join-Path $PSScriptRoot "senku_windows_python.psm1"
Import-Module $pythonModule -Force

$repoRoot = Get-SenkuRepoRoot
$resolvedVenvPath = if ([System.IO.Path]::IsPathRooted($VenvPath)) {
    $VenvPath
} else {
    Join-Path $repoRoot $VenvPath
}
$resolvedRequirementsPath = if ([System.IO.Path]::IsPathRooted($RequirementsPath)) {
    $RequirementsPath
} else {
    Join-Path $repoRoot $RequirementsPath
}
$uvCacheDir = Initialize-SenkuUvCache -RepoRoot $repoRoot
$pythonPath = Join-Path $resolvedVenvPath "Scripts\python.exe"

function Invoke-Step {
    param(
        [Parameter(Mandatory)]
        [string]$Label,

        [Parameter(Mandatory)]
        [scriptblock]$Body
    )

    Write-Host ""
    Write-Host "==> $Label"
    & $Body
}

if (-not (Get-Command uv -ErrorAction SilentlyContinue)) {
    throw "uv is required but was not found on PATH."
}

if (-not (Test-Path -LiteralPath $resolvedRequirementsPath)) {
    throw "Requirements file not found: $resolvedRequirementsPath"
}

Invoke-Step "Create validation venv" {
    if (Test-Path -LiteralPath $pythonPath) {
        Write-Host "Using existing venv: $resolvedVenvPath"
    } else {
        Push-Location $repoRoot
        try {
            uv venv $resolvedVenvPath --python $PythonVersion
        } finally {
            Pop-Location
        }
    }
}

if ($AllowInstall) {
    Invoke-Step "Install validation requirements" {
        $installArgs = @(
            "pip",
            "install",
            "--python",
            $pythonPath,
            "-r",
            $resolvedRequirementsPath
        )

        if ($Offline) {
            $installArgs = @("pip", "install", "--offline", "--python", $pythonPath, "-r", $resolvedRequirementsPath)
        }

        Push-Location $repoRoot
        try {
            & uv @installArgs
            if ($LASTEXITCODE -ne 0) {
                throw "uv pip install failed with exit code $LASTEXITCODE"
            }
        } catch {
            Write-Host ""
            Write-Warning "Dependency install failed. In the current sandbox this usually means PyPI/network access is blocked or the required Windows wheels are not cached."
            Write-Host "Needed packages are listed in: $resolvedRequirementsPath"
            Write-Host "Most validation paths require chromadb, requests, rich, and pyyaml."
            Write-Host "Retry with package/network access, or pre-populate $uvCacheDir with compatible Windows wheels."
            throw
        } finally {
            Pop-Location
        }
    }
} else {
    Write-Host ""
    Write-Host "Skipping dependency install. Re-run with -AllowInstall when network/cache access is available."
}

Invoke-Step "Smoke test imports" {
    $depResult = Test-SenkuPythonDeps -PythonPath $pythonPath
    Write-Host $depResult.output
    if (-not $depResult.ok) {
        Write-Host ""
        Write-Warning "Full ingest/bench validation is blocked until these dependencies are installed for Windows."
        Write-Host "Setup command when install is allowed:"
        Write-Host "  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup_windows_validation_env.ps1 -AllowInstall"
        throw "Validation dependency smoke test failed."
    }
}

Write-Host ""
Write-Host "Validation environment is ready:"
Write-Host "  Python: $pythonPath"
Write-Host "  Use with: powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_windows_validation.ps1 -Mode guide -Wave be"
