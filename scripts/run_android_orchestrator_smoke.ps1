param(
    [string]$OutputDir = "artifacts\bench\android_orchestrator_smoke",
    [switch]$DryRun,
    [string]$AppApkPath = "android-app\app\build\outputs\apk\debug\app-debug.apk",
    [string]$TestApkPath = "android-app\app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk",
    [string]$TestClass = "com.senku.mobile.ScriptedPromptHarnessContractTest"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$gradleProjectDir = Join-Path $repoRoot "android-app"
$gradleWrapper = Join-Path $gradleProjectDir "gradlew.bat"
$orchestratorProperty = "-Psenku.enableTestOrchestrator=true"
$classFilterProperty = "-Pandroid.testInstrumentationRunnerArguments.class=$TestClass"
$gradleTask = ":app:connectedDebugAndroidTest"
$clearPackageData = $true
$stopLine = "STOP: fixed four-emulator evidence remains primary; this Orchestrator smoke is non-acceptance evidence only."
$expectedResultRoots = @(
    "android-app/app/build/outputs/androidTest-results/connected/debug",
    "android-app/app/build/reports/androidTests/connected/debug"
)

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
        "# Android Orchestrator Smoke",
        "",
        "- status: $($Summary.status)",
        "- dry_run: $($Summary.dry_run)",
        "- non_acceptance_evidence: $($Summary.non_acceptance_evidence)",
        "- acceptance_evidence: $($Summary.acceptance_evidence)",
        "- test_class: ``$($Summary.test_class)``",
        "- required_app_apk: ``$($Summary.required_app_apk)``",
        "- required_test_apk: ``$($Summary.required_test_apk)``",
        "- clear_package_data: $($Summary.clear_package_data)",
        "- planned_command: ``$($Summary.planned_command)``",
        "- expected_result_roots: $($Summary.expected_result_roots -join ', ')",
        "- primary_evidence: $($Summary.primary_evidence)",
        "- comparison_baseline: $($Summary.comparison_baseline)",
        "- stop_line: $($Summary.stop_line)"
    )
    $lines | Set-Content -Path $Path -Encoding UTF8
}

if (-not $DryRun) {
    throw "This wrapper is dry-run-only. Re-run with -DryRun; it must not call am instrument, start emulators, or launch connected instrumentation."
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$resolvedAppApkPath = Resolve-TargetPath -Path $AppApkPath
$resolvedTestApkPath = Resolve-TargetPath -Path $TestApkPath
$appApkExists = Test-Path -LiteralPath $resolvedAppApkPath
$testApkExists = Test-Path -LiteralPath $resolvedTestApkPath
$plannedCommand = ".\gradlew.bat $gradleTask '$orchestratorProperty' '$classFilterProperty' --console=plain"
$startedAt = (Get-Date).ToUniversalTime()
$finishedAt = (Get-Date).ToUniversalTime()

$summary = [pscustomobject]@{
    status = "dry_run_only"
    dry_run = $true
    non_acceptance_evidence = $true
    acceptance_evidence = $false
    test_class = $TestClass
    gradle_task = $gradleTask
    gradle_property = $orchestratorProperty
    class_filter_property = $classFilterProperty
    clear_package_data = $clearPackageData
    clear_package_data_posture = "enabled_by_orchestrator_property"
    required_app_apk = (Convert-ToRepoRelativePath -Path $resolvedAppApkPath)
    required_test_apk = (Convert-ToRepoRelativePath -Path $resolvedTestApkPath)
    required_inputs = [ordered]@{
        app_apk = (Convert-ToRepoRelativePath -Path $resolvedAppApkPath)
        test_apk = (Convert-ToRepoRelativePath -Path $resolvedTestApkPath)
        app_apk_exists = $appApkExists
        test_apk_exists = $testApkExists
    }
    expected_result_roots = $expectedResultRoots
    planned_command = $plannedCommand
    gradle_project_dir = (Convert-ToRepoRelativePath -Path $gradleProjectDir)
    gradle_wrapper = (Convert-ToRepoRelativePath -Path $gradleWrapper)
    would_call_am_instrument = $false
    would_start_emulators = $false
    would_launch_connected_instrumentation = $false
    comparison_baseline = "fixed_four_emulator_matrix"
    primary_evidence = "fixed_four_emulator_matrix"
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
Write-Host "Wrote Android Orchestrator smoke summary: $summaryJsonPath"
