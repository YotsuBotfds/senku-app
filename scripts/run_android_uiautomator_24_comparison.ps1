param(
    [string]$OutputDir = "artifacts\bench\android_uiautomator_24_comparison",
    [switch]$DryRun,
    [string]$CandidateVersion = "2.4.0-beta02",
    [string]$ReferenceVersion = "2.3.0",
    [string]$TestClass = "com.senku.mobile.ScriptedPromptHarnessContractTest"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$gradleProjectDir = Join-Path $repoRoot "android-app"
$gradleWrapper = Join-Path $gradleProjectDir "gradlew.bat"
$appBuildGradle = Join-Path (Join-Path $gradleProjectDir "app") "build.gradle"
$gradleTask = ":app:connectedDebugAndroidTest"
$candidateDependency = "androidx.test.uiautomator:uiautomator:$CandidateVersion"
$dependencyOverrideProperty = "-Psenku.uiautomatorComparisonVersion=$CandidateVersion"
$classFilterProperty = "-Pandroid.testInstrumentationRunnerArguments.class=$TestClass"
$plannedCommand = ".\gradlew.bat $gradleTask '$dependencyOverrideProperty' '$classFilterProperty' --console=plain"
$stopLine = "STOP: fixed four-emulator evidence remains primary; this UIAutomator 2.4 comparison is non-acceptance planning evidence only."

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function Convert-ToRepoRelativePath {
    param([string]$Path)

    $resolvedPath = [System.IO.Path]::GetFullPath($Path)
    $resolvedRoot = [System.IO.Path]::GetFullPath($repoRoot)
    if ($resolvedPath.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $resolvedPath.Substring($resolvedRoot.Length).TrimStart("\", "/") -replace "\\", "/"
    }
    return $resolvedPath
}

function Write-JsonFile {
    param(
        [string]$Path,
        $Value
    )

    $Value | ConvertTo-Json -Depth 8 | Set-Content -Path $Path -Encoding UTF8
}

function Write-SummaryMarkdown {
    param(
        [string]$Path,
        $Summary
    )

    $lines = @(
        "# Android UIAutomator 2.4 Comparison",
        "",
        "- status: $($Summary.status)",
        "- dry_run: $($Summary.dry_run)",
        "- non_acceptance_evidence: $($Summary.non_acceptance_evidence)",
        "- acceptance_evidence: $($Summary.acceptance_evidence)",
        "- reference_version: $($Summary.reference_version)",
        "- candidate_version: $($Summary.candidate_version)",
        "- candidate_dependency: ``$($Summary.candidate_dependency)``",
        "- planned_command: ``$($Summary.planned_command)``",
        "- primary_evidence: $($Summary.primary_evidence)",
        "- dependency_change_posture: $($Summary.dependency_change_posture)",
        "- would_modify_gradle_files: $($Summary.would_modify_gradle_files)",
        "- would_launch_connected_instrumentation: $($Summary.would_launch_connected_instrumentation)",
        "- stop_line: $($Summary.stop_line)"
    )
    $lines | Set-Content -Path $Path -Encoding UTF8
}

function Read-UiautomatorVersion {
    param([string]$BuildGradlePath)

    if (-not (Test-Path -LiteralPath $BuildGradlePath)) {
        return $null
    }
    $text = Get-Content -LiteralPath $BuildGradlePath -Raw -Encoding UTF8
    if ($text -match "androidx\.test\.uiautomator:uiautomator:(?<version>[^'`"]+)") {
        return $Matches["version"]
    }
    return $null
}

if (-not $DryRun) {
    throw "This wrapper is dry-run-only. Re-run with -DryRun; it must not change Gradle dependencies or launch connected instrumentation."
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$observedReferenceVersion = Read-UiautomatorVersion -BuildGradlePath $appBuildGradle
$startedAt = (Get-Date).ToUniversalTime()
$finishedAt = (Get-Date).ToUniversalTime()

$summary = [pscustomobject]@{
    summary_kind = "android_uiautomator_24_comparison"
    status = "dry_run_only"
    dry_run = $true
    plan_only = $true
    non_acceptance_evidence = $true
    acceptance_evidence = $false
    reference_version = $ReferenceVersion
    observed_current_version = $observedReferenceVersion
    candidate_version = $CandidateVersion
    candidate_dependency = $candidateDependency
    dependency_override_property = $dependencyOverrideProperty
    dependency_change_posture = "no_global_bump_dry_run_property_only"
    gradle_task = $gradleTask
    test_class = $TestClass
    class_filter_property = $classFilterProperty
    planned_command = $plannedCommand
    gradle_project_dir = (Convert-ToRepoRelativePath -Path $gradleProjectDir)
    gradle_wrapper = (Convert-ToRepoRelativePath -Path $gradleWrapper)
    app_build_gradle = (Convert-ToRepoRelativePath -Path $appBuildGradle)
    expected_result_roots = @(
        "android-app/app/build/outputs/androidTest-results/connected/debug",
        "android-app/app/build/reports/androidTests/connected/debug"
    )
    comparison_lane = "uiautomator_24_beta_against_current_uiautomator"
    comparison_baseline = "fixed_four_emulator_matrix"
    primary_evidence = "fixed_four_emulator_matrix"
    would_modify_gradle_files = $false
    would_resolve_candidate_dependency = $false
    would_call_am_instrument = $false
    would_start_emulators = $false
    would_launch_connected_instrumentation = $false
    devices_touched = $false
    emulator_required = $false
    stop_line = $stopLine
    started_at_utc = $startedAt.ToString("o")
    finished_at_utc = $finishedAt.ToString("o")
}

$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"
Write-JsonFile -Path $summaryJsonPath -Value $summary
Write-SummaryMarkdown -Path $summaryMarkdownPath -Summary $summary

Write-Host $stopLine
Write-Host "Dry run only. Planned command: $plannedCommand"
Write-Host "Wrote Android UIAutomator 2.4 comparison summary: $summaryJsonPath"
