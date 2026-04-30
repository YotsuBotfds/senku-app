param(
    [string]$OutputDir = "artifacts\bench\android_managed_device_smoke",
    [switch]$DryRun,
    [string]$TaskInventoryPath,
    [switch]$ProbeTaskInventory,
    [string]$PhysicalSerial,
    [string]$PhysicalInstalledApkPath,
    [string]$PhysicalFocusPath,
    [string]$PhysicalScreenshotPath,
    [string]$PhysicalDumpPath,
    [string]$PhysicalLogcatPath
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$gradleProjectDir = Join-Path $repoRoot "android-app"
$gradleWrapper = Join-Path $gradleProjectDir "gradlew.bat"
$gradleBuildConfig = Join-Path (Join-Path $gradleProjectDir "app") "build.gradle"
$gradleRootConfig = Join-Path (Join-Path $repoRoot "android-app") "build.gradle"
$managedDeviceProperty = "-Psenku.enableManagedDevices=true"
$taskName = "senkuManagedSmoke"
$stopLine = "STOP: fixed four-emulator evidence remains primary; this Gradle Managed Devices smoke is non-acceptance evidence only."
$fallbackDeviceNames = @("senkuPhoneApi30", "senkuTabletApi30")
$expectedArtifactRoots = @(
    "android-app/app/build/outputs/androidTest-results/managedDevice/senkuPhoneApi30",
    "android-app/app/build/outputs/androidTest-results/managedDevice/senkuTabletApi30",
    "android-app/app/build/reports/androidTests/managedDevice/senkuPhoneApi30",
    "android-app/app/build/reports/androidTests/managedDevice/senkuTabletApi30"
)
$expectedTestTarget = ":app:$taskName"
$comparisonBaseline = "fixed_four_emulator_matrix"
$plannedTaskInventoryCommand = ".\gradlew.bat :app:tasks --all $managedDeviceProperty --console=plain"
$expectedGradleTaskNames = @(
    ":app:senkuPhoneApi30DebugAndroidTest",
    ":app:senkuTabletApi30DebugAndroidTest",
    ":app:senkuManagedSmokeGroupDebugAndroidTest"
)

function Parse-ManagedDeviceScaffold {
    param(
        [string]$BuildGradlePath,
        [string]$RootGradlePath
    )

    $result = [ordered]@{}

    if (Test-Path -LiteralPath $RootGradlePath) {
        $rootGradle = Get-Content -LiteralPath $RootGradlePath -Raw -Encoding UTF8
        if ($rootGradle -match 'id\s+[''"]com\.android\.application[''"]\s+version\s+[''"](?<version>[^''"]+)[''"]') {
            $result["agp_plugin_version"] = $Matches["version"]
        }
    }

    if (-not (Test-Path -LiteralPath $BuildGradlePath)) {
        return $result
    }

    $buildGradle = Get-Content -LiteralPath $BuildGradlePath -Raw -Encoding UTF8
    $managedDevicesIndex = $buildGradle.IndexOf("managedDevices", [StringComparison]::Ordinal)
    if ($managedDevicesIndex -lt 0) {
        return $result
    }

    $braceOpen = $buildGradle.IndexOf("{", $managedDevicesIndex)
    if ($braceOpen -lt 0) {
        return $result
    }

    $depth = 0
    $managedBlockEnd = -1
    for ($i = $braceOpen; $i -lt $buildGradle.Length; $i++) {
        if ($buildGradle[$i] -eq "{") {
            $depth++
        } elseif ($buildGradle[$i] -eq "}") {
            $depth--
            if ($depth -eq 0) {
                $managedBlockEnd = $i
                break
            }
        }
    }

    if ($managedBlockEnd -lt 0) {
        return $result
    }

    $managedBlock = $buildGradle.Substring($braceOpen + 1, $managedBlockEnd - $braceOpen - 1)

    $deviceConfig = @()
    $deviceNames = @()
    $apiLevels = @()
    $imageSources = @()

    $deviceDefinitionPattern = [regex]::Matches(
        $managedBlock,
        "(?ms)^\s*([A-Za-z_][A-Za-z0-9_]*)\s*\(com\.android\.build\.api\.dsl\.ManagedVirtualDevice\)\s*\{(.*?)^\s*\}",
        ([System.Text.RegularExpressions.RegexOptions]::Singleline -bor [System.Text.RegularExpressions.RegexOptions]::Multiline)
    )
    foreach ($match in $deviceDefinitionPattern) {
        $name = $match.Groups[1].Value
        $blockBody = $match.Groups[2].Value
        $apiLevel = $null
        $imageSource = $null
        if ($blockBody -match "apiLevel\s*=\s*(?<api>\d+)") {
            $apiLevel = [int]$Matches["api"]
        }
        if ($blockBody -match 'systemImageSource\s*=\s+[''"](?<source>[^''"]+)[''"]') {
            $imageSource = $Matches["source"]
        }

        $deviceNames += $name
        $apiLevels += $apiLevel
        $imageSources += $imageSource
        $deviceConfig += [ordered]@{
            name = $name
            api_level = $apiLevel
            image_source = $imageSource
        }
    }

    $result["configured_device_names"] = $deviceNames
    $result["configured_device_api_levels"] = $apiLevels
    $result["configured_device_image_sources"] = $imageSources
    $result["configured_devices"] = $deviceConfig

    $groupsBlock = ""
    $groupsMatch = [regex]::Match(
        $managedBlock,
        "(?ms)groups\s*\{\s*(.*?)^\s*\}",
        ([System.Text.RegularExpressions.RegexOptions]::Singleline -bor [System.Text.RegularExpressions.RegexOptions]::Multiline)
    )
    if ($groupsMatch.Success) {
        $groupsBlock = $groupsMatch.Groups[1].Value
    }

    if ($groupsBlock) {
        $smokeGroupMatch = [regex]::Match(
            $groupsBlock,
            "(?m)^\s*([A-Za-z_][A-Za-z0-9_]*)\s*\{"
        )
        if ($smokeGroupMatch.Success) {
            $smokeGroup = $smokeGroupMatch.Groups[1].Value
            $result["smoke_group"] = $smokeGroup
            $groupDeviceMatches = [regex]::Matches($groupsBlock, "targetDevices\.add\(devices\.([A-Za-z_][A-Za-z0-9_]*)\)")
            if ($groupDeviceMatches.Count -gt 0) {
                $result["smoke_group_devices"] = @($groupDeviceMatches | ForEach-Object { $_.Groups[1].Value })
            }
        }
    }

    return $result
}

function Expand-ArtifactRoots {
    param([string[]]$DeviceNames)

    $roots = @()
    foreach ($name in $DeviceNames) {
        $roots += "android-app/app/build/outputs/androidTest-results/managedDevice/$name"
    }
    foreach ($name in $DeviceNames) {
        $roots += "android-app/app/build/reports/androidTests/managedDevice/$name"
    }
    return $roots
}

function Build-ExpectedGradleTaskNames {
    param(
        [string[]]$DeviceNames,
        [string]$SmokeGroup
    )

    $names = @()
    foreach ($name in $DeviceNames) {
        $names += ":app:${name}DebugAndroidTest"
    }
    if (-not [string]::IsNullOrWhiteSpace($SmokeGroup)) {
        $names += ":app:${SmokeGroup}GroupDebugAndroidTest"
    }
    return $names
}

function Build-PlannedResultEvidenceSchema {
    param(
        [string[]]$DeviceNames,
        [string[]]$ArtifactRoots
    )

    return [ordered]@{
        schema_version = 1
        status = "planned_not_collected"
        description = "Future real-run managed-device smoke result shape; dry-run summaries do not collect these values."
        non_acceptance_evidence = $true
        acceptance_evidence = $false
        result_fields = [ordered]@{
            total_test_count = $null
            passed_test_count = $null
            failed_test_count = $null
            skipped_test_count = $null
            failure_count = $null
            failures = @()
            per_device_results = @($DeviceNames | ForEach-Object {
                [ordered]@{
                    device_name = $_
                    total_test_count = $null
                    failed_test_count = $null
                    artifact_paths = @()
                }
            })
        }
        evidence_fields = [ordered]@{
            artifact_roots = $ArtifactRoots
            junit_xml_paths = @()
            html_report_paths = @()
            logcat_paths = @()
            screenshot_paths = @()
            stdout_path = $null
            stderr_path = $null
        }
    }
}

function Convert-OptionalEvidencePath {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }

    return (Convert-ToRepoRelativePath -Path (Resolve-TargetPath -Path $Path))
}

function Build-PhysicalDeviceSmokeEvidence {
    param(
        [string]$Serial,
        [string]$InstalledApkPath,
        [string]$FocusPath,
        [string]$ScreenshotPath,
        [string]$DumpPath,
        [string]$LogcatPath
    )

    $evidenceFields = [ordered]@{
        installed_apk_path = Convert-OptionalEvidencePath -Path $InstalledApkPath
        focus_path = Convert-OptionalEvidencePath -Path $FocusPath
        screenshot_path = Convert-OptionalEvidencePath -Path $ScreenshotPath
        dump_path = Convert-OptionalEvidencePath -Path $DumpPath
        logcat_path = Convert-OptionalEvidencePath -Path $LogcatPath
    }
    $hasEvidence = -not [string]::IsNullOrWhiteSpace($Serial)
    foreach ($value in $evidenceFields.Values) {
        if ($null -ne $value) {
            $hasEvidence = $true
        }
    }

    return [ordered]@{
        schema_version = 1
        status = if ($hasEvidence) { "evidence_paths_recorded" } else { "not_collected" }
        device_profile = "phone-full"
        physical_device = $true
        physical_serial = if ([string]::IsNullOrWhiteSpace($Serial)) { $null } else { $Serial }
        dry_run = $true
        launches_emulators = $false
        acceptance_evidence = $false
        non_acceptance_evidence = $true
        evidence_fields = $evidenceFields
    }
}

function Convert-ObservedGradleTaskName {
    param([string]$TaskName)

    if ($TaskName.StartsWith(":")) {
        return $TaskName
    }
    return ":app:$TaskName"
}

function Parse-GradleTaskInventory {
    param([string]$InventoryText)

    $observedTaskNames = New-Object System.Collections.Generic.List[string]
    $seen = New-Object System.Collections.Generic.HashSet[string]
    $linePattern = [regex]"^\s*(?<task>:?[A-Za-z][A-Za-z0-9_:\-]*)\s+-\s+"

    foreach ($line in ($InventoryText -split "`r?`n")) {
        $match = $linePattern.Match($line)
        if (-not $match.Success) {
            continue
        }

        $taskName = Convert-ObservedGradleTaskName -TaskName $match.Groups["task"].Value
        if ($seen.Add($taskName)) {
            $observedTaskNames.Add($taskName) | Out-Null
        }
    }

    return @($observedTaskNames)
}

function Read-TaskInventory {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }

    $resolvedPath = Resolve-TargetPath -Path $Path
    if (-not (Test-Path -LiteralPath $resolvedPath)) {
        throw "Task inventory path does not exist: $resolvedPath"
    }

    return [pscustomobject]@{
        source = "path"
        source_path = (Convert-ToRepoRelativePath -Path $resolvedPath)
        gradle_invoked = $false
        raw_text = (Get-Content -LiteralPath $resolvedPath -Raw -Encoding UTF8)
    }
}

function Invoke-TaskInventoryProbe {
    param([string]$CommandText)

    $previousLocation = Get-Location
    try {
        Set-Location -LiteralPath $gradleProjectDir
        $output = & $gradleWrapper ":app:tasks" "--all" $managedDeviceProperty "--console=plain" 2>&1
        return [pscustomobject]@{
            source = "probe"
            source_path = $null
            gradle_invoked = $true
            command = $CommandText
            exit_code = $LASTEXITCODE
            raw_text = ($output -join [Environment]::NewLine)
        }
    } finally {
        Set-Location -LiteralPath $previousLocation
    }
}

if (-not $DryRun) {
    throw "This first-slice wrapper is dry-run-only. Re-run with -DryRun; it must not launch Gradle Managed Devices yet."
}

$scaffoldSummary = Parse-ManagedDeviceScaffold -BuildGradlePath $gradleBuildConfig -RootGradlePath $gradleRootConfig

$expectedDevices = if ($scaffoldSummary.Contains("configured_device_names") -and $scaffoldSummary["configured_device_names"].Count -gt 0) {
    $scaffoldSummary["configured_device_names"]
} else {
    $fallbackDeviceNames
}

$expectedArtifactRoots = if ($expectedDevices.Count -gt 0) {
    Expand-ArtifactRoots -DeviceNames $expectedDevices
} else {
    @(
        "android-app/app/build/outputs/androidTest-results/managedDevice/$taskName",
        "android-app/app/build/reports/androidTests/managedDevice/$taskName"
    )
}

$smokeGroupFromScaffold = if ($scaffoldSummary.Contains("smoke_group")) {
    [string]$scaffoldSummary["smoke_group"]
} else {
    $taskName
}
$taskName = $smokeGroupFromScaffold
$expectedGradleTaskNames = Build-ExpectedGradleTaskNames -DeviceNames $expectedDevices -SmokeGroup $smokeGroupFromScaffold
$expectedTestTarget = ":app:$taskName"
$plannedTaskInventoryCommand = ".\gradlew.bat :app:tasks --all $managedDeviceProperty --console=plain"

$scaffoldSummary["expected_gradle_task_names"] = $expectedGradleTaskNames
$scaffoldSummary["expected_artifact_roots"] = $expectedArtifactRoots
$plannedResultEvidenceSchema = Build-PlannedResultEvidenceSchema -DeviceNames $expectedDevices -ArtifactRoots $expectedArtifactRoots

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
        "- expected_devices: $($Summary.expected_devices -join ', ')",
        "- expected_artifact_roots: $($Summary.expected_artifact_roots -join ', ')",
        "- expected_test_target: ``$($Summary.expected_test_target)``",
        "- planned_task_inventory_command: ``$($Summary.planned_task_inventory_command)``",
        "- expected_gradle_task_names: $($Summary.expected_gradle_task_names -join ', ')",
        "- observed_gradle_task_names: $($Summary.observed_gradle_task_names -join ', ')",
        "- observed_expected_gradle_task_names: $($Summary.observed_expected_gradle_task_names -join ', ')",
        "- planned_result_evidence_schema: $($Summary.planned_result_evidence_schema.status) v$($Summary.planned_result_evidence_schema.schema_version)",
        "- physical_device_smoke: $($Summary.physical_device_smoke.status) $($Summary.physical_device_smoke.device_profile)",
        "- physical_serial: $($Summary.physical_device_smoke.physical_serial)",
        "- physical_installed_apk_path: $($Summary.physical_device_smoke.evidence_fields.installed_apk_path)",
        "- physical_focus_path: $($Summary.physical_device_smoke.evidence_fields.focus_path)",
        "- physical_screenshot_path: $($Summary.physical_device_smoke.evidence_fields.screenshot_path)",
        "- physical_dump_path: $($Summary.physical_device_smoke.evidence_fields.dump_path)",
        "- physical_logcat_path: $($Summary.physical_device_smoke.evidence_fields.logcat_path)",
        "- task_inventory_source: $($Summary.task_inventory_source)",
        "- task_inventory_probe_ran: $($Summary.task_inventory_probe_ran)",
        "- comparison_baseline: $($Summary.comparison_baseline)",
        "- planned_command: ``$($Summary.planned_command)``",
        "- stop_line: $($Summary.stop_line)"
    )
    $lines | Set-Content -Path $Path -Encoding UTF8
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$plannedCommand = ".\gradlew.bat :app:$taskName $managedDeviceProperty --console=plain"
$startedAt = (Get-Date).ToUniversalTime()

$taskInventory = $null
if (-not [string]::IsNullOrWhiteSpace($TaskInventoryPath)) {
    $taskInventory = Read-TaskInventory -Path $TaskInventoryPath
} elseif ($ProbeTaskInventory) {
    $taskInventory = Invoke-TaskInventoryProbe -CommandText $plannedTaskInventoryCommand
}

$observedGradleTaskNames = @()
$observedExpectedGradleTaskNames = @()
$taskInventorySummary = $null
if ($null -ne $taskInventory) {
    $observedGradleTaskNames = Parse-GradleTaskInventory -InventoryText $taskInventory.raw_text
    $observedExpectedGradleTaskNames = @($observedGradleTaskNames | Where-Object { $expectedGradleTaskNames -contains $_ })
    $taskInventorySummary = [ordered]@{
        source = $taskInventory.source
        source_path = $taskInventory.source_path
        gradle_invoked = $taskInventory.gradle_invoked
        command = $plannedTaskInventoryCommand
        observed_gradle_task_names = $observedGradleTaskNames
        observed_expected_gradle_task_names = $observedExpectedGradleTaskNames
    }
    if ($taskInventory.PSObject.Properties.Name -contains "exit_code") {
        $taskInventorySummary["exit_code"] = $taskInventory.exit_code
    }
}

$finishedAt = (Get-Date).ToUniversalTime()
$physicalDeviceSmoke = Build-PhysicalDeviceSmokeEvidence `
    -Serial $PhysicalSerial `
    -InstalledApkPath $PhysicalInstalledApkPath `
    -FocusPath $PhysicalFocusPath `
    -ScreenshotPath $PhysicalScreenshotPath `
    -DumpPath $PhysicalDumpPath `
    -LogcatPath $PhysicalLogcatPath

$summary = [pscustomobject]@{
    status = "dry_run_only"
    dry_run = $true
    non_acceptance_evidence = $true
    gradle_property = $managedDeviceProperty
    task_group = $taskName
    task_name = ":app:$taskName"
    expected_devices = $expectedDevices
    expected_artifact_roots = $expectedArtifactRoots
    expected_test_target = $expectedTestTarget
    planned_task_inventory_command = $plannedTaskInventoryCommand
    expected_gradle_task_names = $expectedGradleTaskNames
    observed_gradle_task_names = $observedGradleTaskNames
    observed_expected_gradle_task_names = $observedExpectedGradleTaskNames
    planned_result_evidence_schema = $plannedResultEvidenceSchema
    physical_device_smoke = $physicalDeviceSmoke
    task_inventory_source = if ($null -eq $taskInventory) { "not_collected" } else { $taskInventory.source }
    task_inventory_probe_ran = ($null -ne $taskInventory -and $taskInventory.gradle_invoked)
    task_inventory = $taskInventorySummary
    comparison_baseline = $comparisonBaseline
    planned_command = $plannedCommand
    gradle_project_dir = (Convert-ToRepoRelativePath -Path $gradleProjectDir)
    gradle_wrapper = (Convert-ToRepoRelativePath -Path $gradleWrapper)
    would_launch_emulators = $false
    managed_devices_launched = $false
    acceptance_evidence = $false
    primary_evidence = $comparisonBaseline
    stop_line = $stopLine
    started_at_utc = $startedAt.ToString("o")
    finished_at_utc = $finishedAt.ToString("o")
    managed_device_scaffold = $scaffoldSummary
}

$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"
Write-JsonFile -Path $summaryJsonPath -Value $summary
Write-SummaryMarkdown -Path $summaryMarkdownPath -Summary $summary

Write-Host $stopLine
Write-Host "Dry run only. Planned command: $plannedCommand"
Write-Host "Wrote Android Gradle Managed Devices smoke summary: $summaryJsonPath"
