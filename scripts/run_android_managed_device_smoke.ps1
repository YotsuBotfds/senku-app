param(
    [string]$OutputDir = "artifacts\bench\android_managed_device_smoke",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$gradleProjectDir = Join-Path $repoRoot "android-app"
$gradleWrapper = Join-Path $gradleProjectDir "gradlew.bat"
$managedDeviceProperty = "-Psenku.enableManagedDevices=true"
$taskName = "senkuManagedSmoke"
$stopLine = "STOP: fixed four-emulator evidence remains primary; this Gradle Managed Devices smoke is non-acceptance evidence only."

if (-not $DryRun) {
    throw "This first-slice wrapper is dry-run-only. Re-run with -DryRun; it must not launch Gradle Managed Devices yet."
}

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
        "# Android Gradle Managed Devices Smoke",
        "",
        "- status: $($Summary.status)",
        "- dry_run: $($Summary.dry_run)",
        "- non_acceptance_evidence: $($Summary.non_acceptance_evidence)",
        "- gradle_property: ``$($Summary.gradle_property)``",
        "- task_group: $($Summary.task_group)",
        "- planned_command: ``$($Summary.planned_command)``",
        "- stop_line: $($Summary.stop_line)"
    )
    $lines | Set-Content -Path $Path -Encoding UTF8
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$plannedCommand = ".\gradlew.bat :app:$taskName $managedDeviceProperty --console=plain"
$startedAt = (Get-Date).ToUniversalTime()
$finishedAt = (Get-Date).ToUniversalTime()

$summary = [pscustomobject]@{
    status = "dry_run_only"
    dry_run = $true
    non_acceptance_evidence = $true
    gradle_property = $managedDeviceProperty
    task_group = $taskName
    task_name = ":app:$taskName"
    planned_command = $plannedCommand
    gradle_project_dir = (Convert-ToRepoRelativePath -Path $gradleProjectDir)
    gradle_wrapper = (Convert-ToRepoRelativePath -Path $gradleWrapper)
    would_launch_emulators = $false
    managed_devices_launched = $false
    acceptance_evidence = $false
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
Write-Host "Wrote Android Gradle Managed Devices smoke summary: $summaryJsonPath"
