[CmdletBinding()]
param(
    [string]$RequirementsPath = "requirements.txt",

    [string]$OutputPath = "requirements.lock.txt",

    [string]$PythonVersion = "3.13",

    [string]$PythonPlatform = "x86_64-pc-windows-msvc",

    [string]$UvPath = "",

    [switch]$NoGenerateHashes,

    [switch]$Upgrade,

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

function Resolve-RepoRelativePath {
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

function Get-UvExecutable {
    param(
        [string]$RequestedUv
    )

    if (-not [string]::IsNullOrWhiteSpace($RequestedUv)) {
        return $RequestedUv
    }

    $uv = Get-Command uv -ErrorAction SilentlyContinue
    if ($null -eq $uv) {
        throw "uv is unavailable. Install uv or pass -UvPath with the uv executable path."
    }
    return $uv.Source
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

function New-UvCompileArguments {
    param(
        [Parameter(Mandatory)]
        [string]$RequirementsFile,

        [Parameter(Mandatory)]
        [string]$OutputFile,

        [Parameter(Mandatory)]
        [string]$PythonVersion,

        [Parameter(Mandatory)]
        [string]$PythonPlatform,

        [switch]$NoGenerateHashes,

        [switch]$Upgrade
    )

    $args = @(
        "pip",
        "compile",
        $RequirementsFile,
        "--output-file",
        $OutputFile,
        "--python-version",
        $PythonVersion,
        "--python-platform",
        $PythonPlatform,
        "--custom-compile-command",
        ".\scripts\compile_python_lock.ps1"
    )

    if (-not $NoGenerateHashes) {
        $args += "--generate-hashes"
    }
    if ($Upgrade) {
        $args += "--upgrade"
    }
    return $args
}

$repoRoot = Get-RepoRoot
$requirementsFile = Resolve-RepoRelativePath -RepoRoot $repoRoot -Path $RequirementsPath
$outputFile = Resolve-RepoRelativePath -RepoRoot $repoRoot -Path $OutputPath
$uvExecutable = Get-UvExecutable -RequestedUv $UvPath

if (-not (Test-Path -LiteralPath $requirementsFile)) {
    throw "Requirements file not found: $RequirementsPath"
}

$outputDirectory = Split-Path -Parent $outputFile
$compileArgs = New-UvCompileArguments `
    -RequirementsFile $RequirementsPath `
    -OutputFile $OutputPath `
    -PythonVersion $PythonVersion `
    -PythonPlatform $PythonPlatform `
    -NoGenerateHashes:$NoGenerateHashes `
    -Upgrade:$Upgrade

Push-Location $repoRoot
try {
    Write-Host "Python dependency lock compile command:"
    Write-Host (Format-CommandLine -Executable $uvExecutable -Arguments $compileArgs)

    if ($WhatIf) {
        Write-Host "Python dependency lock dry run."
        return
    }

    if (-not [string]::IsNullOrWhiteSpace($outputDirectory)) {
        New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
    }

    & $uvExecutable @compileArgs
    if ($LASTEXITCODE -ne 0) {
        throw "uv pip compile failed with exit code $LASTEXITCODE."
    }
    Write-Host ("Python dependency lock refreshed: {0}" -f $OutputPath)
} finally {
    Pop-Location
}
