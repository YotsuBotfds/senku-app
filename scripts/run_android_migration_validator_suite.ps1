[CmdletBinding()]
param(
    [string]$PythonPath = ".\.venvs\senku-validate\Scripts\python.exe",

    [switch]$WhatIf
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot

function Resolve-ValidatorSuitePath {
    param(
        [Parameter(Mandatory)]
        [string]$Path
    )

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path $repoRoot $Path))
}

function Quote-ValidatorSuiteToken {
    param(
        [Parameter(Mandatory)]
        [string]$Value
    )

    if ($Value -match '^[A-Za-z0-9_./:\\-]+$') {
        return $Value
    }
    return "'" + ($Value -replace "'", "''") + "'"
}

function Format-ValidatorSuiteCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Executable,

        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    $parts = @("&", (Quote-ValidatorSuiteToken -Value $Executable))
    $parts += $Arguments | ForEach-Object { Quote-ValidatorSuiteToken -Value $_ }
    return ($parts -join " ")
}

$testModules = @(
    "tests.test_validate_android_migration_summary",
    "tests.test_validate_android_asset_pack_parity_summary",
    "tests.test_validate_android_fts_fallback_summary",
    "tests.test_validate_android_managed_device_smoke_summary",
    "tests.test_validate_android_litert_readiness_summary",
    "tests.test_validate_android_orchestrator_smoke_summary",
    "tests.test_validate_android_harness_matrix_plan",
    "tests.test_validate_android_ui_state_pack_plan",
    "tests.test_validate_android_capture_summary",
    "tests.test_validate_android_tooling_version_manifest",
    "tests.test_validate_android_headless_state_pack_lane_summary",
    "tests.test_validate_android_instrumented_capture_summary",
    "tests.test_validate_android_large_data_litert_tablet_lane_summary",
    "tests.test_validate_android_migration_preflight_bundle_summary",
    "tests.test_summarize_android_migration_proof",
    "tests.test_android_prompt_logged_contract",
    "tests.test_android_followup_matrix_contract",
    "tests.test_android_logged_wrapper_pack_cache_contract",
    "tests.test_run_android_search_log_only_contract",
    "tests.test_write_android_capture_summary",
    "tests.test_write_android_tooling_version_manifest",
    "tests.test_stop_android_harness_runs_contract"
)

$python = Resolve-ValidatorSuitePath -Path $PythonPath
if (-not (Test-Path -LiteralPath $python)) {
    throw "PythonPath does not exist: $python"
}

$arguments = @("-B", "-m", "unittest") + $testModules
$displayCommand = Format-ValidatorSuiteCommand -Executable $python -Arguments $arguments

Push-Location $repoRoot
try {
    if ($WhatIf) {
        Write-Host "Android migration validator suite dry run."
        Write-Host $displayCommand
        Write-Host "Modules:"
        foreach ($module in $testModules) {
            Write-Host "- $module"
        }
        return
    }

    Write-Host "Android migration validator suite."
    Write-Host $displayCommand
    & $python @arguments
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        exit $exitCode
    }
} finally {
    Pop-Location
}
