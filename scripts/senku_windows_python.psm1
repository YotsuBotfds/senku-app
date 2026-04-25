function Get-SenkuRepoRoot {
    [CmdletBinding()]
    param()

    return (Split-Path -Parent $PSScriptRoot)
}

function Initialize-SenkuUvCache {
    [CmdletBinding()]
    param(
        [string]$RepoRoot = (Get-SenkuRepoRoot)
    )

    $cachePath = Join-Path $RepoRoot ".tmp\uv-cache"
    New-Item -ItemType Directory -Force -Path $cachePath | Out-Null
    $env:UV_CACHE_DIR = $cachePath
    return $cachePath
}

function Resolve-SenkuPythonPath {
    [CmdletBinding()]
    param(
        [string]$RepoRoot = (Get-SenkuRepoRoot),

        [string]$VenvPath = ".venvs\senku-validate"
    )

    $checkedInVenv = Join-Path $RepoRoot "venv"
    if (-not [string]::IsNullOrWhiteSpace($env:SENKU_PYTHON_PATH)) {
        $candidate = $env:SENKU_PYTHON_PATH
        if ($candidate.StartsWith($checkedInVenv, [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "SENKU_PYTHON_PATH points at the checked-in POSIX-origin venv, which is not usable on Windows: $candidate"
        }
        if (Test-Path -LiteralPath $candidate) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
        throw "SENKU_PYTHON_PATH is set but does not exist: $candidate"
    }

    $resolvedVenvPath = if ([System.IO.Path]::IsPathRooted($VenvPath)) {
        $VenvPath
    } else {
        Join-Path $RepoRoot $VenvPath
    }
    $candidate = Join-Path $resolvedVenvPath "Scripts\python.exe"
    if (Test-Path -LiteralPath $candidate) {
        return (Resolve-Path -LiteralPath $candidate).Path
    }

    throw "No Windows validation Python found. Run scripts\setup_windows_validation_env.ps1, or set SENKU_PYTHON_PATH to a Windows python.exe with repo dependencies installed."
}

function Test-SenkuPythonDeps {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)]
        [string]$PythonPath,

        [string[]]$Modules = @("chromadb", "requests", "rich", "yaml"),

        [switch]$ThrowOnFailure
    )

    $moduleList = ($Modules | ForEach-Object { "'$_'" }) -join ", "
$script = @"
import importlib
import sys

missing = []
for name in [$moduleList]:
    try:
        importlib.import_module(name)
    except Exception as exc:
        missing.append(name + ': ' + type(exc).__name__ + ': ' + str(exc))

if missing:
    print('missing validation dependencies:')
    for item in missing:
        print('  - ' + item)
    raise SystemExit(1)

print('validation dependency smoke test OK')
"@

    $output = & $PythonPath -c $script 2>&1
    $ok = ($LASTEXITCODE -eq 0)
    $result = [pscustomobject]@{
        ok = $ok
        python_path = $PythonPath
        modules = $Modules
        output = ($output -join [Environment]::NewLine)
    }

    if (-not $ok -and $ThrowOnFailure) {
        throw $result.output
    }

    return $result
}

Export-ModuleMember -Function Get-SenkuRepoRoot, Initialize-SenkuUvCache, Resolve-SenkuPythonPath, Test-SenkuPythonDeps
