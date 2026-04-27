[CmdletBinding()]
param(
    [string]$OutputDir = "artifacts\bench\android_large_data_litert_tablet_lane",
    [string]$ModelPath = "",
    [string]$Device = "emulator-5554",
    [string]$PackageName = "com.senku.mobile",
    [int]$PartitionSizeMb = 8192,
    [switch]$RealMode,
    [string]$ConfirmRealMode = "",
    [switch]$StartEmulator,
    [switch]$RestartRunning,
    [switch]$SkipDataSpaceCheck,
    [switch]$PruneExistingModels,
    [switch]$SkipInstall,
    [switch]$SkipBuild,
    [ValidateSet("model_store", "readiness_dry_run")]
    [string]$RuntimeCheck = "model_store"
)

$ErrorActionPreference = "Stop"
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$launchScript = Join-Path $PSScriptRoot "start_senku_emulator_matrix.ps1"
$pushScript = Join-Path $PSScriptRoot "push_litert_model_to_android.ps1"
$readinessScript = Join-Path $PSScriptRoot "run_android_litert_readiness_matrix.ps1"
$instrumentedSmokeScript = Join-Path $PSScriptRoot "run_android_instrumented_ui_smoke.ps1"
$confirmationToken = "RUN_EMULATOR_5554_LARGE_LITERT_DATA"
$fixedFourEmulatorMatrix = "fixed_four_emulator_matrix"
$stopLine = "STOP: large-data LiteRT tablet lane evidence is deploy/runtime evidence only; fixed four-emulator UI acceptance evidence remains primary."
$modelStoreTestClass = "com.senku.mobile.ModelFileStoreImportedModelTest#importedLiteRtModelIsDiscoverableAndNonEmpty"

function Resolve-LanePath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path $repoRoot $Path))
}

function Assert-RequiredScript {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Required helper script not found: $Path"
    }
}

function Invoke-LaneChildPowerShell {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments,
        [switch]$AllowFailure
    )

    $output = @(& powershell @Arguments 2>&1)
    $exitCode = $LASTEXITCODE
    $text = ($output | ForEach-Object { [string]$_ }) -join [Environment]::NewLine
    if (-not $AllowFailure -and $exitCode -ne 0) {
        throw "Child command failed with exit code ${exitCode}: powershell $($Arguments -join ' ')`n$text"
    }
    return [pscustomobject]@{
        exit_code = $exitCode
        output = $text
        command = "powershell " + ($Arguments -join " ")
    }
}

function Convert-ToJsonObject {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) {
        return $null
    }
    return (Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json)
}

function Format-LaneValue {
    param([object]$Value)

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

function New-LaneMarkdown {
    param([object]$Summary)

    return @(
        "# Android Large-Data LiteRT Tablet Lane"
        ""
        "Status: $($Summary.status)"
        "Real mode: $(Format-LaneValue -Value $Summary.real_mode)"
        "Dry run: $(Format-LaneValue -Value $Summary.dry_run)"
        ""
        "## Evidence"
        ""
        "- Deploy evidence: $(Format-LaneValue -Value $Summary.deploy_evidence)"
        "- Runtime evidence: $(Format-LaneValue -Value $Summary.runtime_evidence)"
        "- UI acceptance evidence: $(Format-LaneValue -Value $Summary.ui_acceptance_evidence)"
        "- Acceptance evidence: $(Format-LaneValue -Value $Summary.acceptance_evidence)"
        "- Primary evidence: $($Summary.primary_evidence)"
        "- Stop line: $($Summary.stop_line)"
        ""
        "## Lane"
        ""
        "- Device: $($Summary.device)"
        "- Role: $($Summary.role)"
        "- Launch profile: $($Summary.launch_profile)"
        "- Partition size MB: $($Summary.partition_size_mb)"
        "- Package: $($Summary.package_name)"
        "- Runtime check: $($Summary.runtime_check)"
        ""
        "## Child Artifacts"
        ""
        "- Launch profile summary: ``$($Summary.child_artifacts.launch_profile_summary)``"
        "- Push summary: ``$($Summary.child_artifacts.push_summary)``"
        "- Readiness summary: ``$($Summary.child_artifacts.readiness_summary)``"
        "- Instrumentation summary: ``$($Summary.child_artifacts.instrumentation_summary)``"
        ""
        $Summary.stop_line
    ) -join [Environment]::NewLine
}

Assert-RequiredScript -Path $launchScript
Assert-RequiredScript -Path $pushScript
Assert-RequiredScript -Path $readinessScript
Assert-RequiredScript -Path $instrumentedSmokeScript

if ($Device -ne "emulator-5554") {
    throw "This guarded lane is scoped to emulator-5554. Pass Device emulator-5554 or use another Android lane."
}
if ($PartitionSizeMb -lt 8192) {
    throw "The large-data LiteRT tablet lane requires -PartitionSizeMb 8192 or larger."
}
if ($RealMode -and $ConfirmRealMode -ne $confirmationToken) {
    throw "Real mode requires -ConfirmRealMode $confirmationToken."
}

$resolvedOutputDir = Resolve-LanePath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$launchSummaryPath = Join-Path $resolvedOutputDir "launch_profile_summary.json"
$pushSummaryPath = Join-Path $resolvedOutputDir "model_push_summary.json"
$pushMarkdownPath = Join-Path $resolvedOutputDir "model_push_summary.md"
$readinessOutputDir = Join-Path $resolvedOutputDir "readiness"
$instrumentationRoot = Join-Path $resolvedOutputDir "model_store_instrumentation"
$instrumentationSummaryPath = Join-Path $resolvedOutputDir "model_store_instrumentation_summary.json"
$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"

$childResults = [ordered]@{}

$launchPreflightArgs = @(
    "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $launchScript,
    "-Roles", "tablet_portrait",
    "-Mode", "read_only",
    "-LaunchProfile", "large-litert-data",
    "-Headless",
    "-PartitionSizeMb", [string]$PartitionSizeMb,
    "-SummaryPath", $launchSummaryPath,
    "-WhatIf"
)
$childResults.launch_profile_preflight = Invoke-LaneChildPowerShell -Arguments $launchPreflightArgs

if (-not $RealMode) {
    $pushArgs = @(
        "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $pushScript,
        "-Device", $Device,
        "-PackageName", $PackageName,
        "-SummaryPath", $pushSummaryPath,
        "-SummaryMarkdownPath", $pushMarkdownPath,
        "-DryRun"
    )
    if (-not [string]::IsNullOrWhiteSpace($ModelPath)) {
        $pushArgs += @("-ModelPath", $ModelPath)
    }
    if ($SkipDataSpaceCheck) {
        $pushArgs += "-SkipDataSpaceCheck"
    }
    $childResults.model_push = Invoke-LaneChildPowerShell -Arguments $pushArgs

    $readinessArgs = @(
        "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $readinessScript,
        "-OutputDir", $readinessOutputDir,
        "-PackageName", $PackageName,
        "-Backend", "litert",
        "-RequestMode", "single_prompt_smoke",
        "-DeviceMatrix", "large_litert_data_emulator_5554",
        "-DryRun"
    )
    if (-not [string]::IsNullOrWhiteSpace($ModelPath)) {
        $readinessArgs += @("-ModelPath", $ModelPath)
    }
    $childResults.litert_readiness = Invoke-LaneChildPowerShell -Arguments $readinessArgs
} else {
    if ($StartEmulator) {
        $startArgs = @(
            "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $launchScript,
            "-Roles", "tablet_portrait",
            "-Mode", "read_only",
            "-Headless",
            "-PartitionSizeMb", [string]$PartitionSizeMb
        )
        if ($RestartRunning) {
            $startArgs += "-RestartRunning"
        }
        $childResults.emulator_start = Invoke-LaneChildPowerShell -Arguments $startArgs
    } else {
        $childResults.emulator_start = [pscustomobject]@{
            exit_code = $null
            output = "Skipped by default. Pass -StartEmulator with -RealMode to launch emulator-5554."
            command = ""
        }
    }

    $pushArgs = @(
        "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $pushScript,
        "-Device", $Device,
        "-PackageName", $PackageName,
        "-SummaryPath", $pushSummaryPath,
        "-RestartApp"
    )
    if (-not [string]::IsNullOrWhiteSpace($ModelPath)) {
        $pushArgs += @("-ModelPath", $ModelPath)
    }
    if ($SkipDataSpaceCheck) {
        $pushArgs += "-SkipDataSpaceCheck"
    }
    if ($PruneExistingModels) {
        $pushArgs += "-PruneExistingModels"
    }
    $childResults.model_push = Invoke-LaneChildPowerShell -Arguments $pushArgs

    if ($RuntimeCheck -eq "model_store") {
        $instrumentationArgs = @(
            "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $instrumentedSmokeScript,
            "-Device", $Device,
            "-TestClass", $modelStoreTestClass,
            "-SmokeProfile", "custom",
            "-Orientation", "portrait",
            "-ArtifactRoot", $instrumentationRoot,
            "-SummaryPath", $instrumentationSummaryPath,
            "-CaptureLogcat",
            "-ClearLogcatBeforeRun"
        )
        if ($SkipBuild) {
            $instrumentationArgs += "-SkipBuild"
        }
        if ($SkipInstall) {
            $instrumentationArgs += "-SkipInstall"
        }
        $childResults.model_store_instrumentation = Invoke-LaneChildPowerShell -Arguments $instrumentationArgs
    } else {
        $readinessArgs = @(
            "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $readinessScript,
            "-OutputDir", $readinessOutputDir,
            "-PackageName", $PackageName,
            "-Backend", "litert",
            "-RequestMode", "single_prompt_smoke",
            "-DeviceMatrix", "large_litert_data_emulator_5554"
        )
        if (-not [string]::IsNullOrWhiteSpace($ModelPath)) {
            $readinessArgs += @("-ModelPath", $ModelPath)
        }
        $childResults.litert_readiness = Invoke-LaneChildPowerShell -Arguments $readinessArgs
    }
}

$launchSummary = Convert-ToJsonObject -Path $launchSummaryPath
$pushSummary = Convert-ToJsonObject -Path $pushSummaryPath
$readinessSummaryPath = Join-Path $readinessOutputDir "summary.json"
$readinessSummary = Convert-ToJsonObject -Path $readinessSummaryPath
$instrumentationSummary = Convert-ToJsonObject -Path $instrumentationSummaryPath

$deployEvidence = [bool]($RealMode -and $pushSummary -and $childResults.model_push.exit_code -eq 0)
$runtimeEvidence = [bool]($RealMode -and $instrumentationSummary -and $childResults.Contains("model_store_instrumentation") -and $childResults.model_store_instrumentation.exit_code -eq 0)
$status = if ($RealMode) {
    if ($deployEvidence -and ($RuntimeCheck -ne "model_store" -or $runtimeEvidence)) { "pass" } else { "failed" }
} else {
    "dry_run_only"
}

$summary = [ordered]@{
    status = $status
    dry_run = (-not [bool]$RealMode)
    real_mode = [bool]$RealMode
    real_mode_guard = [ordered]@{
        required_switch = "-RealMode"
        required_confirmation = $confirmationToken
        confirmed = [bool]($RealMode -and $ConfirmRealMode -eq $confirmationToken)
    }
    non_acceptance_evidence = $true
    acceptance_evidence = $false
    ui_acceptance_evidence = $false
    deploy_evidence = $deployEvidence
    deploy_evidence_kind = "litert_model_push"
    runtime_evidence = $runtimeEvidence
    runtime_evidence_kind = if ($RuntimeCheck -eq "model_store") { "model_store_instrumentation" } else { "litert_readiness" }
    evidence_boundary = "deploy/runtime only; not UI acceptance"
    stop_line = $stopLine
    primary_evidence = $fixedFourEmulatorMatrix
    comparison_baseline = $fixedFourEmulatorMatrix
    device = $Device
    role = "tablet_portrait"
    launch_profile = "large-litert-data"
    partition_size_mb = $PartitionSizeMb
    package_name = $PackageName
    runtime_check = $RuntimeCheck
    start_emulator_requested = [bool]$StartEmulator
    selected_roles = @("tablet_portrait")
    devices = @($Device)
    generated_utc = [DateTime]::UtcNow.ToString("o")
    child_artifacts = [ordered]@{
        launch_profile_summary = $launchSummaryPath
        push_summary = $(if (Test-Path -LiteralPath $pushSummaryPath) { $pushSummaryPath } else { $null })
        push_markdown = $(if (Test-Path -LiteralPath $pushMarkdownPath) { $pushMarkdownPath } else { $null })
        readiness_summary = $(if (Test-Path -LiteralPath $readinessSummaryPath) { $readinessSummaryPath } else { $null })
        instrumentation_summary = $(if (Test-Path -LiteralPath $instrumentationSummaryPath) { $instrumentationSummaryPath } else { $null })
    }
    child_status = [ordered]@{
        launch_profile_preflight_exit_code = $childResults.launch_profile_preflight.exit_code
        emulator_start_exit_code = $(if ($childResults.Contains("emulator_start")) { $childResults.emulator_start.exit_code } else { $null })
        model_push_exit_code = $(if ($childResults.Contains("model_push")) { $childResults.model_push.exit_code } else { $null })
        litert_readiness_exit_code = $(if ($childResults.Contains("litert_readiness")) { $childResults.litert_readiness.exit_code } else { $null })
        model_store_instrumentation_exit_code = $(if ($childResults.Contains("model_store_instrumentation")) { $childResults.model_store_instrumentation.exit_code } else { $null })
    }
    planned_commands = [ordered]@{
        launch_profile_preflight = $childResults.launch_profile_preflight.command
        emulator_start = $(if ($childResults.Contains("emulator_start")) { $childResults.emulator_start.command } else { "" })
        model_push = $(if ($childResults.Contains("model_push")) { $childResults.model_push.command } else { "" })
        litert_readiness = $(if ($childResults.Contains("litert_readiness")) { $childResults.litert_readiness.command } else { "" })
        model_store_instrumentation = $(if ($childResults.Contains("model_store_instrumentation")) { $childResults.model_store_instrumentation.command } else { "" })
    }
    child_summaries = [ordered]@{
        launch_profile = $launchSummary
        model_push = $pushSummary
        litert_readiness = $readinessSummary
        model_store_instrumentation = $instrumentationSummary
    }
}

$summary | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $summaryJsonPath -Encoding UTF8
New-LaneMarkdown -Summary ([pscustomobject]$summary) | Set-Content -LiteralPath $summaryMarkdownPath -Encoding UTF8

Write-Host "Android large-data LiteRT tablet lane summary written to $summaryJsonPath"
Write-Host "Summary markdown written to $summaryMarkdownPath"
Write-Host $stopLine

if ($status -eq "failed") {
    exit 1
}
