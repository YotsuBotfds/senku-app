[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$OutputDir = "artifacts\bench\senku_tablet_2_large_data_avd_preflight",
    [string]$AvdHome = "",
    [string]$AvdName = "Senku_Tablet_2",
    [string]$DesiredDataPartitionSize = "20G",
    [string]$ExpectedSerial = "emulator-5554",
    [string]$AdbDevicesOutputPath = "",
    [switch]$Apply,
    [string]$ConfirmPrepare = "",
    [switch]$SkipRunningDeviceCheck
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$confirmationToken = "PREPARE_SENKU_TABLET_2_LARGE_DATA_AVD"
$expectedAvdName = "Senku_Tablet_2"
$expectedTarget = "android-36.1"
$expectedAbi = "x86_64"
$expectedWidth = "1600"
$expectedHeight = "2560"
$requiredPath = "config_based_avd_data_partition"
$cliPartitionSizeMaxMb = 2047

function Resolve-RepoPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path $repoRoot $Path))
}

function Convert-ToRepoRelativePath {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }
    $resolvedPath = [System.IO.Path]::GetFullPath($Path)
    $resolvedRoot = [System.IO.Path]::GetFullPath($repoRoot)
    if ($resolvedPath.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $resolvedPath.Substring($resolvedRoot.Length).TrimStart("\", "/") -replace "\\", "/"
    }
    return $resolvedPath
}

function Read-KeyValueFile {
    param([string]$Path)

    $values = [ordered]@{}
    if (-not (Test-Path -LiteralPath $Path)) {
        return $values
    }

    foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
        $trimmed = [string]$line
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.TrimStart().StartsWith("#")) {
            continue
        }
        $index = $trimmed.IndexOf("=")
        if ($index -lt 0) {
            continue
        }
        $key = $trimmed.Substring(0, $index).Trim()
        $value = $trimmed.Substring($index + 1).Trim()
        if (-not [string]::IsNullOrWhiteSpace($key)) {
            $values[$key] = $value
        }
    }
    return $values
}

function Write-KeyValueFile {
    param(
        [string]$Path,
        [hashtable]$Values
    )

    $lines = New-Object System.Collections.Generic.List[string]
    $seen = @{}
    foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
        $text = [string]$line
        $index = $text.IndexOf("=")
        if ($index -ge 0) {
            $key = $text.Substring(0, $index).Trim()
            if ($Values.ContainsKey($key)) {
                $lines.Add(("{0}={1}" -f $key, $Values[$key])) | Out-Null
                $seen[$key] = $true
                continue
            }
        }
        $lines.Add($text) | Out-Null
    }
    foreach ($key in $Values.Keys) {
        if (-not $seen.ContainsKey($key)) {
            $lines.Add(("{0}={1}" -f $key, $Values[$key])) | Out-Null
        }
    }
    $lines | Set-Content -LiteralPath $Path -Encoding UTF8
}

function Resolve-AdbPath {
    $candidates = @()
    if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_SDK_ROOT)) {
        $candidates += (Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe")
    }
    if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_HOME)) {
        $candidates += (Join-Path $env:ANDROID_HOME "platform-tools\adb.exe")
    }
    if (-not [string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        $candidates += (Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe")
    }

    foreach ($candidate in $candidates) {
        if (-not [string]::IsNullOrWhiteSpace([string]$candidate) -and (Test-Path -LiteralPath $candidate)) {
            return $candidate
        }
    }

    try {
        $command = Get-Command adb.exe -ErrorAction Stop
        if ($command -and -not [string]::IsNullOrWhiteSpace([string]$command.Source)) {
            return [string]$command.Source
        }
    } catch {
    }
    return $null
}

function Get-RunningDeviceCheck {
    param(
        [string]$AdbPath,
        [string]$Serial,
        [string]$DevicesOutputPath = ""
    )

    if (-not [string]::IsNullOrWhiteSpace($DevicesOutputPath)) {
        $text = Get-Content -LiteralPath $DevicesOutputPath -Raw -Encoding UTF8
        return [pscustomobject]@{
            status = "captured_output"
            running = [bool]($text -match ("(?m)^{0}\s+device\b" -f [regex]::Escape($Serial)))
            output = $text
        }
    }

    if ([string]::IsNullOrWhiteSpace($AdbPath)) {
        return [pscustomobject]@{
            status = "adb_unavailable"
            running = $null
            output = ""
        }
    }

    $output = @(& $AdbPath devices 2>&1)
    $text = ($output | ForEach-Object { [string]$_ }) -join [Environment]::NewLine
    return [pscustomobject]@{
        status = "checked"
        running = [bool]($text -match ("(?m)^{0}\s+device\b" -f [regex]::Escape($Serial)))
        output = $text
    }
}

function Get-PathInventoryItem {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return $null
    }
    $item = Get-Item -LiteralPath $Path -Force
    return [pscustomobject]@{
        path = $Path
        name = $item.Name
        type = $(if ($item.PSIsContainer) { "directory" } else { "file" })
        bytes = $(if ($item.PSIsContainer) { $null } else { [int64]$item.Length })
    }
}

function Test-IsWithinPath {
    param(
        [string]$Child,
        [string]$Parent
    )

    $resolvedChild = [System.IO.Path]::GetFullPath($Child)
    $resolvedParent = [System.IO.Path]::GetFullPath($Parent).TrimEnd("\", "/") + [System.IO.Path]::DirectorySeparatorChar
    return $resolvedChild.StartsWith($resolvedParent, [System.StringComparison]::OrdinalIgnoreCase)
}

if ($AvdName -ne $expectedAvdName) {
    throw "This maintenance lane is scoped to $expectedAvdName; received $AvdName."
}
if ($Apply -and $ConfirmPrepare -ne $confirmationToken) {
    throw "Apply mode requires -ConfirmPrepare $confirmationToken."
}

$resolvedOutputDir = Resolve-RepoPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null
$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"

if ([string]::IsNullOrWhiteSpace($AvdHome)) {
    $AvdHome = Join-Path $env:USERPROFILE ".android\avd"
}
$resolvedAvdHome = [System.IO.Path]::GetFullPath($AvdHome)
$avdIniPath = Join-Path $resolvedAvdHome "$AvdName.ini"
$iniValues = Read-KeyValueFile -Path $avdIniPath
$avdPath = if ($iniValues.Contains("path") -and -not [string]::IsNullOrWhiteSpace([string]$iniValues["path"])) {
    [string]$iniValues["path"]
} else {
    Join-Path $resolvedAvdHome "$AvdName.avd"
}
$resolvedAvdPath = [System.IO.Path]::GetFullPath($avdPath)
$configPath = Join-Path $resolvedAvdPath "config.ini"
$configValues = Read-KeyValueFile -Path $configPath

$identityChecks = [ordered]@{
    avd_name = [bool]($AvdName -eq $expectedAvdName)
    target = [bool]($configValues["target"] -eq $expectedTarget)
    abi_type = [bool]($configValues["abi.type"] -eq $expectedAbi)
    width = [bool]($configValues["hw.lcd.width"] -eq $expectedWidth)
    height = [bool]($configValues["hw.lcd.height"] -eq $expectedHeight)
}
$identityVerified = -not @($identityChecks.Values | Where-Object { $_ -ne $true })

$stateCandidates = @(
    "userdata-qemu.img",
    "userdata-qemu.img.qcow2",
    "userdata.img",
    "cache.img",
    "snapshots",
    "snapshot.lock",
    "multiinstance.lock"
)
$plannedStateItems = @(
    foreach ($name in $stateCandidates) {
        $item = Get-PathInventoryItem -Path (Join-Path $resolvedAvdPath $name)
        if ($null -ne $item) {
            $item
        }
    }
)

$adbPath = $null
if ($Apply -and -not $SkipRunningDeviceCheck -and [string]::IsNullOrWhiteSpace($AdbDevicesOutputPath)) {
    $adbPath = Resolve-AdbPath
}
$runningCheck = if (-not $Apply -or $SkipRunningDeviceCheck) {
    [pscustomobject]@{
        status = "skipped"
        running = $null
        output = ""
    }
} else {
    Get-RunningDeviceCheck -AdbPath $adbPath -Serial $ExpectedSerial -DevicesOutputPath $AdbDevicesOutputPath
}
$runningDeviceBlocked = [bool]($runningCheck.running -eq $true -and -not $SkipRunningDeviceCheck)

$status = "dry_run_only"
$blockedReason = $null
$backupDir = $null
$backupManifestPath = $null
$destructiveActionsPerformed = $false
$configUpdated = $false
$quarantineDir = $null
$quarantinedItems = @()

if (-not $identityVerified) {
    $status = "failed"
    $blockedReason = "avd_identity_mismatch"
} elseif ($Apply -and $runningCheck.status -eq "adb_unavailable" -and -not $SkipRunningDeviceCheck) {
    $status = "blocked"
    $blockedReason = "adb_unavailable_for_running_device_check"
} elseif ($Apply -and $runningDeviceBlocked) {
    $status = "blocked"
    $blockedReason = "target_serial_running"
} elseif ($Apply) {
    $status = "pass"
    $backupDir = Join-Path $resolvedOutputDir "backup"
    $quarantineDir = Join-Path $resolvedOutputDir "quarantine"
    New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
    New-Item -ItemType Directory -Force -Path $quarantineDir | Out-Null

    if (-not (Test-IsWithinPath -Child $resolvedAvdPath -Parent $resolvedAvdHome)) {
        throw "Resolved AVD path is outside the AVD home; refusing to apply: $resolvedAvdPath"
    }
    if (-not (Test-IsWithinPath -Child $quarantineDir -Parent $resolvedOutputDir)) {
        throw "Quarantine path is outside the output directory; refusing to apply: $quarantineDir"
    }

    Copy-Item -LiteralPath $configPath -Destination (Join-Path $backupDir "config.ini.before") -Force
    $backupManifest = [ordered]@{
        avd_name = $AvdName
        avd_ini_path = $avdIniPath
        avd_path = $resolvedAvdPath
        config_path = $configPath
        current_data_partition_size = $configValues["disk.dataPartition.size"]
        desired_data_partition_size = $DesiredDataPartitionSize
        state_items = $plannedStateItems
        generated_utc = [DateTime]::UtcNow.ToString("o")
    }
    $backupManifestPath = Join-Path $backupDir "manifest.json"
    $backupManifest | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $backupManifestPath -Encoding UTF8

    if ($PSCmdlet.ShouldProcess($configPath, "Set disk.dataPartition.size to $DesiredDataPartitionSize")) {
        Write-KeyValueFile -Path $configPath -Values @{ "disk.dataPartition.size" = $DesiredDataPartitionSize }
        $configUpdated = $true
    }
    foreach ($item in @($plannedStateItems)) {
        $source = [string]$item.path
        $destination = Join-Path $quarantineDir $item.name
        if ($PSCmdlet.ShouldProcess($source, "Move stale AVD data/snapshot state to $destination")) {
            Move-Item -LiteralPath $source -Destination $destination -Force
            $quarantinedItems += [pscustomobject]@{
                source = $source
                destination = $destination
                type = $item.type
                bytes = $item.bytes
            }
        }
    }
    $destructiveActionsPerformed = [bool]($quarantinedItems.Count -gt 0)
}

$summary = [ordered]@{
    summary_kind = "senku_tablet_2_large_data_avd_preflight"
    schema_version = 1
    status = $status
    blocked_reason = $blockedReason
    dry_run = (-not [bool]$Apply)
    apply = [bool]$Apply
    non_acceptance_evidence = $true
    acceptance_evidence = $false
    ui_acceptance_evidence = $false
    deploy_evidence = $false
    runtime_evidence = $false
    evidence_boundary = "AVD maintenance preflight only; not deploy/runtime or UI acceptance evidence"
    required_path = $requiredPath
    prepared_lane_flag = "-UsePreparedAvdDataPartition"
    cli_partition_size_max_mb = $cliPartitionSizeMaxMb
    confirmation_token = $confirmationToken
    confirmation_matched = [bool]($ConfirmPrepare -eq $confirmationToken)
    avd_name = $AvdName
    expected_serial = $ExpectedSerial
    avd_home = $resolvedAvdHome
    avd_ini_path = $avdIniPath
    avd_path = $resolvedAvdPath
    config_path = $configPath
    desired_data_partition_size = $DesiredDataPartitionSize
    current_data_partition_size = $configValues["disk.dataPartition.size"]
    config_identity = [ordered]@{
        target = $configValues["target"]
        abi_type = $configValues["abi.type"]
        hw_lcd_width = $configValues["hw.lcd.width"]
        hw_lcd_height = $configValues["hw.lcd.height"]
        checks = $identityChecks
        verified = $identityVerified
    }
    running_device_check = [ordered]@{
        adb_path = $adbPath
        status = $runningCheck.status
        expected_serial_running = $runningCheck.running
        skipped = [bool]$SkipRunningDeviceCheck
    }
    planned_destructive_actions = [ordered]@{
        update_config_disk_data_partition_size = [bool]$identityVerified
        quarantine_stale_userdata_and_snapshots = @($plannedStateItems)
    }
    destructive_actions_performed = $destructiveActionsPerformed
    config_updated = $configUpdated
    backup_dir = (Convert-ToRepoRelativePath -Path $backupDir)
    backup_manifest_path = (Convert-ToRepoRelativePath -Path $backupManifestPath)
    quarantine_dir = (Convert-ToRepoRelativePath -Path $quarantineDir)
    quarantined_items = @($quarantinedItems)
    next_command = "powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_large_data_litert_tablet_lane.ps1 -Device emulator-5554 -PartitionSizeMb 8192 -RealMode -ConfirmRealMode RUN_EMULATOR_5554_LARGE_LITERT_DATA -StartEmulator -UsePreparedAvdDataPartition"
    stop_line = "This prepares AVD data capacity only. It is non-acceptance evidence and does not replace fixed four-emulator state-pack proof."
    generated_utc = [DateTime]::UtcNow.ToString("o")
}

$summary | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $summaryJsonPath -Encoding UTF8
@(
    "# Senku Tablet 2 Large-Data AVD Preflight"
    ""
    "- status: $status"
    "- apply: $([bool]$Apply)"
    "- avd: $AvdName"
    "- current_data_partition_size: $($configValues["disk.dataPartition.size"])"
    "- desired_data_partition_size: $DesiredDataPartitionSize"
    "- required_path: $requiredPath"
    "- acceptance_evidence: False"
    "- summary_json: ``$summaryJsonPath``"
    ""
    $summary.stop_line
) | Set-Content -LiteralPath $summaryMarkdownPath -Encoding UTF8

Write-Host "Senku Tablet 2 large-data AVD preflight: $status"
Write-Host "Summary JSON written to $summaryJsonPath"
Write-Host "Summary Markdown written to $summaryMarkdownPath"

if ($status -eq "failed") {
    exit 1
}
if ($status -eq "blocked") {
    exit 2
}
