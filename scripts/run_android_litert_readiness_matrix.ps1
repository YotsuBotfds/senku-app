[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [Parameter(Mandatory = $true)]
    [string]$OutputDir,
    [string]$ModelPath = "",
    [string]$PackageName = "com.senku.mobile",
    [string]$Backend = "litert",
    [string]$RequestMode = "single_prompt_smoke",
    [string]$DeviceMatrix = "fixed_four_emulator",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$effectiveDryRun = [bool]($DryRun -or $WhatIfPreference)
$fixedFourEmulatorMatrix = "fixed_four_emulator_matrix"
$fixedFourEmulatorStopLine = "fixed four-emulator posture matrix: 5556 phone portrait; 5560 phone landscape; 5554 tablet portrait; 5558 tablet landscape"
$nonAcceptanceStopLine = "STOP: LiteRT readiness dry run is non-acceptance evidence only; fixed four-emulator evidence remains primary."

function Resolve-ReadinessPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path $repoRoot $Path))
}

function Resolve-OptionalModelPath {
    param(
        [string]$Path
    )

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return ""
    }
    return Resolve-ReadinessPath -Path $Path
}

function New-ModelReadinessSummary {
    param(
        [string]$Path
    )

    $resolvedPath = Resolve-OptionalModelPath -Path $Path
    if ([string]::IsNullOrWhiteSpace($resolvedPath) -or -not (Test-Path -LiteralPath $resolvedPath)) {
        return [ordered]@{
            path = $resolvedPath
            exists = $false
            name = ""
            bytes = $null
            sha256 = ""
        }
    }

    $item = Get-Item -LiteralPath $resolvedPath
    return [ordered]@{
        path = $item.FullName
        exists = $true
        name = $item.Name
        bytes = $item.Length
        sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $item.FullName).Hash.ToLowerInvariant()
    }
}

function New-LiteRtReadinessSummary {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ResolvedOutputDir,
        [Parameter(Mandatory = $true)]
        [hashtable]$Model
    )

    $modelName = if ($Model.exists) { $Model.name } else { "<model-file>" }
    $requiredBytes = if ($Model.exists) { ($Model.bytes * 2L) + 67108864L } else { $null }

    return [ordered]@{
        status = "dry_run_only"
        non_acceptance_evidence = $true
        acceptance_evidence = $false
        dry_run = $true
        stop_line = $nonAcceptanceStopLine
        primary_evidence = $fixedFourEmulatorMatrix
        comparison_baseline = $fixedFourEmulatorMatrix
        generated_utc = [DateTime]::UtcNow.ToString("o")
        package_name = $PackageName
        model = $Model
        app_private_target = [ordered]@{
            directory = "/data/user/0/$PackageName/files/models"
            path = "/data/user/0/$PackageName/files/models/$modelName"
        }
        data_free_space_posture = [ordered]@{
            adb_required_in_dry_run = $false
            adb_queried = $false
            check = "real run must verify /data free bytes before staging"
            required_bytes = $requiredBytes
            required_rule = "model_bytes * 2 + 67108864 when model exists"
            skip_allowed_for_acceptance = $false
        }
        backend = [ordered]@{
            name = $Backend
            package = $PackageName
            readiness_matrix = $DeviceMatrix
        }
        request = [ordered]@{
            mode = $RequestMode
            prompt = "LiteRT readiness placeholder prompt."
            expected_artifacts = @("summary.json", "logcat excerpt", "backend request/response timing")
        }
        logcat_extraction_plan = [ordered]@{
            clear_before_run = $true
            capture_after_run = $true
            command = "adb -s <serial> logcat -d -v time Senku:D LiteRT:D AndroidRuntime:E *:S"
            artifact = "logcat_litert_readiness_<serial>.txt"
        }
        fixed_four_emulator_stop_line = $fixedFourEmulatorStopLine
        real_run_status = "not_implemented"
        output_dir = $ResolvedOutputDir
    }
}

$resolvedOutputDir = Resolve-ReadinessPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null
$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"

if (-not $effectiveDryRun) {
    throw "LiteRT readiness real run is not implemented yet. Re-run with -DryRun or -WhatIf to write non-acceptance readiness evidence without adb."
}

$modelSummary = New-ModelReadinessSummary -Path $ModelPath
$summary = New-LiteRtReadinessSummary -ResolvedOutputDir $resolvedOutputDir -Model $modelSummary
$summary | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $summaryJsonPath -Encoding UTF8

Write-Host "LiteRT readiness dry run; no adb, emulator, or device command was required."
Write-Host "Summary JSON written to $summaryJsonPath"
Write-Host $fixedFourEmulatorStopLine
