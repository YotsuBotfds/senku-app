[CmdletBinding()]
param(
    [ValidateSet('compile', 'unit', 'ingest', 'guide', 'android-migration')]
    [string]$Mode = 'compile',

    [string]$Wave = 'be',

    [string]$VenvPath = ".venvs\senku-validate",

    [string]$GenerationUrl = "http://127.0.0.1:1235/v1",

    [string]$GenerationModel = "gemma-4-e2b-it-litert",

    [string]$EmbedUrl = "http://127.0.0.1:8801/v1",

    [int]$TopK = 8,

    [string[]]$ExtraBenchArgs = @(),

    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'

$pythonModule = Join-Path $PSScriptRoot "senku_windows_python.psm1"
Import-Module $pythonModule -Force

$repoRoot = Get-SenkuRepoRoot
Initialize-SenkuUvCache -RepoRoot $repoRoot | Out-Null
$pythonPath = Resolve-SenkuPythonPath -RepoRoot $repoRoot -VenvPath $VenvPath

Push-Location $repoRoot
try {
    if ($Mode -eq 'compile') {
        & $pythonPath -m py_compile query.py ingest.py bench.py
        if ($LASTEXITCODE -ne 0) {
            throw "py_compile failed with exit code $LASTEXITCODE"
        }
        return
    }

    if (-not ($Mode -eq 'android-migration' -and $WhatIf)) {
        $depResult = Test-SenkuPythonDeps -PythonPath $pythonPath
        if (-not $depResult.ok) {
            Write-Warning "Validation dependencies are not ready for $pythonPath."
            Write-Host $depResult.output
            Write-Host ""
            Write-Host "Run setup from a shell with package/network access:"
            Write-Host "  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup_windows_validation_env.ps1 -AllowInstall"
            throw "Validation dependencies are not ready."
        }
    }

    switch ($Mode) {
        'unit' {
            & $pythonPath -m unittest discover -s tests -v
            if ($LASTEXITCODE -ne 0) {
                throw "unit tests failed with exit code $LASTEXITCODE"
            }
        }
        'ingest' {
            & $pythonPath ingest.py --stats
            if ($LASTEXITCODE -ne 0) {
                throw "ingest failed with exit code $LASTEXITCODE"
            }
        }
        'guide' {
            $guideScript = Join-Path $PSScriptRoot "run_guide_prompt_validation.ps1"
            & $guideScript `
                -Wave $Wave `
                -PythonPath $pythonPath `
                -GenerationUrl $GenerationUrl `
                -GenerationModel $GenerationModel `
                -EmbedUrl $EmbedUrl `
                -TopK $TopK `
                -ExtraBenchArgs $ExtraBenchArgs
            if ($LASTEXITCODE -ne 0) {
                throw "guide validation failed with exit code $LASTEXITCODE"
            }
        }
        'android-migration' {
            $androidMigrationScript = Join-Path $PSScriptRoot "run_android_migration_validator_suite.ps1"
            & $androidMigrationScript -PythonPath $pythonPath -WhatIf:$WhatIf
            if (-not $WhatIf -and $LASTEXITCODE -ne 0) {
                throw "android migration validation failed with exit code $LASTEXITCODE"
            }
        }
    }
} finally {
    Pop-Location
}
