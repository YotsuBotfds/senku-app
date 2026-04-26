[CmdletBinding()]
param(
    [string]$RequirementsPath = "requirements.txt",

    [string]$PythonPath = "",

    [string]$OutputJson = "artifacts\security\pip_audit.json",

    [switch]$AllowInstall,

    [switch]$SkipIfUnavailable,

    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'

function Get-RepoRoot {
    $root = (& git rev-parse --show-toplevel 2>$null)
    if ($LASTEXITCODE -eq 0 -and -not [string]::IsNullOrWhiteSpace($root)) {
        return $root.Trim()
    }
    return (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
}

function Resolve-RepoPath {
    param(
        [Parameter(Mandatory)]
        [string]$RepoRoot,

        [Parameter(Mandatory)]
        [string]$Path
    )

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }
    return (Join-Path $RepoRoot $Path)
}

function Get-PythonExecutable {
    param(
        [Parameter(Mandatory)]
        [string]$RepoRoot,

        [string]$RequestedPython
    )

    if (-not [string]::IsNullOrWhiteSpace($RequestedPython)) {
        return $RequestedPython
    }
    if (-not [string]::IsNullOrWhiteSpace($env:SENKU_PYTHON_PATH)) {
        return $env:SENKU_PYTHON_PATH
    }

    $validationPython = Join-Path $RepoRoot ".venvs\senku-validate\Scripts\python.exe"
    if (Test-Path -LiteralPath $validationPython) {
        return $validationPython
    }
    return "python"
}

function Test-PythonModule {
    param(
        [Parameter(Mandatory)]
        [string]$PythonExecutable,

        [Parameter(Mandatory)]
        [string]$ModuleName
    )

    & $PythonExecutable -c "import $ModuleName" 2>$null
    return ($LASTEXITCODE -eq 0)
}

function New-PipAuditCommand {
    param(
        [Parameter(Mandatory)]
        [string]$RepoRoot,

        [Parameter(Mandatory)]
        [string]$PythonExecutable,

        [Parameter(Mandatory)]
        [string]$RequirementsFile,

        [Parameter(Mandatory)]
        [string]$ReportPath,

        [switch]$AllowInstall,

        [switch]$SkipIfUnavailable
    )

    $auditArgs = @(
        "--requirement",
        $RequirementsFile,
        "--format",
        "json",
        "--output",
        $ReportPath,
        "--progress-spinner",
        "off"
    )

    $uvx = Get-Command uvx -ErrorAction SilentlyContinue
    if ($null -ne $uvx) {
        return [pscustomobject]@{
            Name = "pip-audit via uvx"
            Executable = $uvx.Source
            Arguments = @("--from", "pip-audit", "pip-audit") + $auditArgs
        }
    }

    if (Test-PythonModule -PythonExecutable $PythonExecutable -ModuleName "pip_audit") {
        return [pscustomobject]@{
            Name = "pip-audit module"
            Executable = $PythonExecutable
            Arguments = @("-m", "pip_audit") + $auditArgs
        }
    }

    if ($AllowInstall) {
        Write-Host "pip-audit is not available; installing it with the selected Python."
        & $PythonExecutable -m pip install pip-audit
        if ($LASTEXITCODE -ne 0) {
            throw "Unable to install pip-audit with $PythonExecutable."
        }
        return [pscustomobject]@{
            Name = "pip-audit module"
            Executable = $PythonExecutable
            Arguments = @("-m", "pip_audit") + $auditArgs
        }
    }

    $message = "pip-audit is unavailable. Install uv, install pip-audit in the validation environment, or rerun with -AllowInstall."
    if ($SkipIfUnavailable) {
        Write-Warning $message
        return $null
    }
    throw $message
}

function Format-CommandLine {
    param(
        [Parameter(Mandatory)]
        [string]$Executable,

        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    $quoted = @($Executable) + $Arguments | ForEach-Object {
        if ($_ -match '^[A-Za-z0-9_./:\\-]+$') {
            $_
        } else {
            "'" + ($_ -replace "'", "''") + "'"
        }
    }
    return ($quoted -join ' ')
}

$repoRoot = Get-RepoRoot
$requirementsFile = Resolve-RepoPath -RepoRoot $repoRoot -Path $RequirementsPath
$reportPath = Resolve-RepoPath -RepoRoot $repoRoot -Path $OutputJson
$pythonExecutable = Get-PythonExecutable -RepoRoot $repoRoot -RequestedPython $PythonPath

if (-not (Test-Path -LiteralPath $requirementsFile)) {
    throw "Requirements file not found: $RequirementsPath"
}

$reportDirectory = Split-Path -Parent $reportPath

Push-Location $repoRoot
try {
    $command = New-PipAuditCommand `
        -RepoRoot $repoRoot `
        -PythonExecutable $pythonExecutable `
        -RequirementsFile $requirementsFile `
        -ReportPath $reportPath `
        -AllowInstall:$AllowInstall `
        -SkipIfUnavailable:$SkipIfUnavailable

    if ($null -eq $command) {
        return
    }

    if ($WhatIf) {
        Write-Host "Dependency security scan dry run."
        Write-Host ("Tool: {0}" -f $command.Name)
        Write-Host ("Command: {0}" -f (Format-CommandLine -Executable $command.Executable -Arguments $command.Arguments))
        return
    }

    if (-not [string]::IsNullOrWhiteSpace($reportDirectory)) {
        New-Item -ItemType Directory -Force -Path $reportDirectory | Out-Null
    }

    Write-Host ("Running dependency security scan with {0}." -f $command.Name)
    & $command.Executable @($command.Arguments)
    if ($LASTEXITCODE -ne 0) {
        throw "Dependency security scan failed with exit code $LASTEXITCODE."
    }
    Write-Host ("Dependency security scan passed. Report: {0}" -f $OutputJson)
} finally {
    Pop-Location
}
