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
    $modelIdentity = [ordered]@{
        source = "dry_run_model_path"
        path = $Model.path
        name = $Model.name
        bytes = $Model.bytes
        sha256 = $Model.sha256
        exists = $Model.exists
    }

    return [ordered]@{
        status = "dry_run_only"
        real_run_status = "not_implemented"
        non_acceptance_evidence = $true
        acceptance_evidence = $false
        dry_run = $true
        stop_line = $nonAcceptanceStopLine
        primary_evidence = $fixedFourEmulatorMatrix
        comparison_baseline = $fixedFourEmulatorMatrix
        runtime_evidence = "none_dry_run_only"
        generated_utc = [DateTime]::UtcNow.ToString("o")
        package_name = $PackageName
        model = $Model
        model_identity = $modelIdentity
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
            real_run_status = "not_implemented"
            adb_required_in_dry_run = $false
        }
        request = [ordered]@{
            mode = $RequestMode
            prompt = "LiteRT readiness placeholder prompt."
            backend = $Backend
            package = $PackageName
            real_run_status = "not_implemented"
            device_required_in_dry_run = $false
            expected_artifacts = @("summary.json", "logcat excerpt", "backend request/response timing")
        }
        logcat_extraction_plan = [ordered]@{
            status = "planned_for_real_run"
            real_run_status = "not_implemented"
            adb_required_in_dry_run = $false
            clear_before_run = $true
            capture_after_run = $true
            command = "adb -s <serial> logcat -d -v time Senku:D LiteRT:D AndroidRuntime:E *:S"
            artifact = "logcat_litert_readiness_<serial>.txt"
            extraction_filters = @("Senku:D", "LiteRT:D", "AndroidRuntime:E", "*:S")
        }
        fixed_four_emulator_stop_line = $fixedFourEmulatorStopLine
        output_dir = $ResolvedOutputDir
    }
}

function Format-ReadinessMarkdownValue {
    param(
        [object]$Value
    )

    if ($null -eq $Value) {
        return "n/a"
    }
    if ($Value -is [bool]) {
        return $Value.ToString().ToLowerInvariant()
    }
    if ([string]::IsNullOrWhiteSpace([string]$Value)) {
        return "n/a"
    }
    return [string]$Value
}

function New-LiteRtReadinessMarkdown {
    param(
        [Parameter(Mandatory = $true)]
        [object]$Summary
    )

    $modelExists = Format-ReadinessMarkdownValue -Value $Summary.model.exists
    $modelName = Format-ReadinessMarkdownValue -Value $Summary.model.name
    $modelPath = Format-ReadinessMarkdownValue -Value $Summary.model.path
    $modelBytes = Format-ReadinessMarkdownValue -Value $Summary.model.bytes
    $modelSha256 = Format-ReadinessMarkdownValue -Value $Summary.model.sha256
    $modelIdentitySource = Format-ReadinessMarkdownValue -Value $Summary.model_identity.source
    $requiredBytes = Format-ReadinessMarkdownValue -Value $Summary.data_free_space_posture.required_bytes

    return @(
        "# LiteRT Readiness Dry Run"
        ""
        "Status: $($Summary.status)"
        "Real run status: $($Summary.real_run_status)"
        ""
        "## Evidence posture"
        ""
        "- Non-acceptance evidence: $(Format-ReadinessMarkdownValue -Value $Summary.non_acceptance_evidence)"
        "- Acceptance evidence: $(Format-ReadinessMarkdownValue -Value $Summary.acceptance_evidence)"
        "- Primary evidence: $($Summary.primary_evidence)"
        "- Comparison baseline: $($Summary.comparison_baseline)"
        "- Stop line: $($Summary.stop_line)"
        ""
        "## Model"
        ""
        "- Exists: $modelExists"
        "- Name: $modelName"
        "- Path: $modelPath"
        "- Bytes: $modelBytes"
        "- SHA-256: $modelSha256"
        "- Identity source: $modelIdentitySource"
        ""
        "## Backend and request"
        ""
        "- Backend: $($Summary.backend.name)"
        "- Package: $($Summary.backend.package)"
        "- Readiness matrix: $($Summary.backend.readiness_matrix)"
        "- Backend real run status: $($Summary.backend.real_run_status)"
        "- Backend ADB required in dry run: $(Format-ReadinessMarkdownValue -Value $Summary.backend.adb_required_in_dry_run)"
        "- Request mode: $($Summary.request.mode)"
        "- Request backend: $($Summary.request.backend)"
        "- Request package: $($Summary.request.package)"
        "- Request real run status: $($Summary.request.real_run_status)"
        "- Request device required in dry run: $(Format-ReadinessMarkdownValue -Value $Summary.request.device_required_in_dry_run)"
        "- Prompt: $($Summary.request.prompt)"
        ""
        "## Data free-space posture"
        ""
        "- ADB required in dry run: $(Format-ReadinessMarkdownValue -Value $Summary.data_free_space_posture.adb_required_in_dry_run)"
        "- ADB queried: $(Format-ReadinessMarkdownValue -Value $Summary.data_free_space_posture.adb_queried)"
        "- Check: $($Summary.data_free_space_posture.check)"
        "- Required bytes: $requiredBytes"
        "- Required rule: $($Summary.data_free_space_posture.required_rule)"
        "- Skip allowed for acceptance: $(Format-ReadinessMarkdownValue -Value $Summary.data_free_space_posture.skip_allowed_for_acceptance)"
        ""
        "## Logcat plan"
        ""
        "- Status: $($Summary.logcat_extraction_plan.status)"
        "- Real run status: $($Summary.logcat_extraction_plan.real_run_status)"
        "- ADB required in dry run: $(Format-ReadinessMarkdownValue -Value $Summary.logcat_extraction_plan.adb_required_in_dry_run)"
        "- Clear before run: $(Format-ReadinessMarkdownValue -Value $Summary.logcat_extraction_plan.clear_before_run)"
        "- Capture after run: $(Format-ReadinessMarkdownValue -Value $Summary.logcat_extraction_plan.capture_after_run)"
        "- Command: ``$($Summary.logcat_extraction_plan.command)``"
        "- Artifact: ``$($Summary.logcat_extraction_plan.artifact)``"
        ""
        $Summary.fixed_four_emulator_stop_line
    ) -join [Environment]::NewLine
}

$resolvedOutputDir = Resolve-ReadinessPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null
$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"

if (-not $effectiveDryRun) {
    throw "LiteRT readiness real run is not implemented yet. Re-run with -DryRun or -WhatIf to write non-acceptance readiness evidence without adb."
}

$modelSummary = New-ModelReadinessSummary -Path $ModelPath
$summary = New-LiteRtReadinessSummary -ResolvedOutputDir $resolvedOutputDir -Model $modelSummary
$summary | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $summaryJsonPath -Encoding UTF8
New-LiteRtReadinessMarkdown -Summary $summary | Set-Content -LiteralPath $summaryMarkdownPath -Encoding UTF8

Write-Host "LiteRT readiness dry run; no adb, emulator, or device command was required."
Write-Host "Summary JSON written to $summaryJsonPath"
Write-Host "Summary markdown written to $summaryMarkdownPath"
Write-Host $fixedFourEmulatorStopLine
