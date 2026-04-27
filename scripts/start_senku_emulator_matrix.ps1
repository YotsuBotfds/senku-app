[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [ValidateSet("all", "phone_portrait", "phone_landscape", "tablet_portrait", "tablet_landscape")]
    [string[]]$Roles = @("all"),
    [ValidateSet("read_only", "writable")]
    [string]$Mode = "read_only",
    [switch]$RestartRunning,
    [switch]$NoBootAnim,
    [switch]$GpuSwiftshader,
    [switch]$Headless,
    [int]$PartitionSizeMb = 0
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Resolve-SenkuEmulatorPath {
    $runningPath = $null
    try {
        $runningPath = Get-Process -Name emulator -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty Path
    } catch {
    }
    if (-not [string]::IsNullOrWhiteSpace([string]$runningPath)) {
        return [string]$runningPath
    }

    $candidates = @()
    if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_SDK_ROOT)) {
        $candidates += (Join-Path $env:ANDROID_SDK_ROOT "emulator\emulator.exe")
    }
    if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_HOME)) {
        $candidates += (Join-Path $env:ANDROID_HOME "emulator\emulator.exe")
    }
    if (-not [string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        $candidates += (Join-Path $env:LOCALAPPDATA "Android\Sdk\emulator\emulator.exe")
    }

    foreach ($candidate in $candidates) {
        if ([string]::IsNullOrWhiteSpace([string]$candidate)) {
            continue
        }
        try {
            if (Test-Path -LiteralPath $candidate) {
                return $candidate
            }
        } catch {
        }
    }

    throw "Could not resolve emulator.exe. Start one emulator manually first or set ANDROID_SDK_ROOT."
}

function Resolve-SenkuAdbPath {
    param([string]$EmulatorPath)

    try {
        $adbCommand = Get-Command adb.exe -ErrorAction Stop
        if ($adbCommand -and -not [string]::IsNullOrWhiteSpace([string]$adbCommand.Source)) {
            return [string]$adbCommand.Source
        }
    } catch {
    }

    if (-not [string]::IsNullOrWhiteSpace($EmulatorPath)) {
        try {
            $sdkRoot = Split-Path -Parent (Split-Path -Parent $EmulatorPath)
            $derived = Join-Path $sdkRoot "platform-tools\adb.exe"
            if (-not [string]::IsNullOrWhiteSpace([string]$derived)) {
                return $derived
            }
        } catch {
        }
    }

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
        if ([string]::IsNullOrWhiteSpace([string]$candidate)) {
            continue
        }
        try {
            if (Test-Path -LiteralPath $candidate) {
                return $candidate
            }
        } catch {
            if ($candidate -match 'adb\.exe$') {
                return $candidate
            }
        }
    }

    throw "Could not resolve adb.exe."
}

function Get-SenkuEmulatorMatrix {
    return @(
        [pscustomobject]@{
            role = "phone_portrait"
            avd = "Senku_Large_4"
            port = 5556
            serial = "emulator-5556"
            resolution = "1080x2400"
            orientation = "portrait"
        }
        [pscustomobject]@{
            role = "phone_landscape"
            avd = "Senku_Large_3"
            port = 5560
            serial = "emulator-5560"
            resolution = "2400x1080"
            orientation = "landscape"
        }
        [pscustomobject]@{
            role = "tablet_portrait"
            avd = "Senku_Tablet_2"
            port = 5554
            serial = "emulator-5554"
            resolution = "1600x2560"
            orientation = "portrait"
        }
        [pscustomobject]@{
            role = "tablet_landscape"
            avd = "Senku_Tablet"
            port = 5558
            serial = "emulator-5558"
            resolution = "2560x1600"
            orientation = "landscape"
        }
    )
}

function Stop-SenkuEmulatorLane {
    param(
        [string]$AdbPath,
        [string]$Serial
    )

    try {
        & $AdbPath -s $Serial emu kill | Out-Null
    } catch {
    }

    $deadline = (Get-Date).AddSeconds(30)
    while ((Get-Date) -lt $deadline) {
        try {
            $devices = & $AdbPath devices
            if ($devices -notmatch [regex]::Escape($Serial)) {
                return
            }
        } catch {
        }
        Start-Sleep -Milliseconds 500
    }
}

function Start-SenkuEmulatorLane {
    param(
        [string]$EmulatorPath,
        [object]$Lane,
        [string]$Mode,
        [switch]$NoBootAnim,
        [switch]$GpuSwiftshader,
        [switch]$Headless,
        [int]$PartitionSizeMb = 0
    )

    $arguments = Get-SenkuEmulatorLaunchArguments -Lane $Lane -Mode $Mode -NoBootAnim:$NoBootAnim -GpuSwiftshader:$GpuSwiftshader -Headless:$Headless -PartitionSizeMb $PartitionSizeMb

    return Start-Process -FilePath $EmulatorPath -ArgumentList $arguments -PassThru
}

function Get-SenkuEmulatorLaunchArguments {
    param(
        [object]$Lane,
        [string]$Mode,
        [switch]$NoBootAnim,
        [switch]$GpuSwiftshader,
        [switch]$Headless,
        [int]$PartitionSizeMb = 0
    )

    $arguments = @(
        "-avd", $Lane.avd,
        "-port", [string]$Lane.port
    )

    if ($Mode -eq "read_only") {
        $arguments += @("-read-only", "-no-snapshot-load", "-no-snapshot-save")
    }
    if ($NoBootAnim) {
        $arguments += "-no-boot-anim"
    }
    if ($GpuSwiftshader) {
        $arguments += @("-gpu", "swiftshader_indirect")
    }
    if ($Headless) {
        $arguments += "-no-window"
    }
    if ($PartitionSizeMb -gt 0) {
        $arguments += @("-partition-size", [string]$PartitionSizeMb)
    }

    return $arguments
}

$emulatorPath = Resolve-SenkuEmulatorPath
$adbPath = Resolve-SenkuAdbPath -EmulatorPath $emulatorPath
$matrix = Get-SenkuEmulatorMatrix
$selectedRoles = if ($Roles -contains "all") {
    $matrix.role
} else {
    $Roles
}

$selectedLanes = @(
foreach ($lane in $matrix) {
    if ($selectedRoles -contains $lane.role) {
        $lane
    }
}
)

if (-not $selectedLanes -or $selectedLanes.Count -eq 0) {
    throw "No emulator lanes selected."
}

Write-Host ("Using emulator: {0}" -f $emulatorPath)
Write-Host ("Using adb: {0}" -f $adbPath)
Write-Host ("Launch mode: {0}" -f $Mode)

foreach ($lane in $selectedLanes) {
    Write-Host ("- {0}: {1} / {2} / {3}" -f $lane.role, $lane.serial, $lane.avd, $lane.resolution)
    $launchArguments = Get-SenkuEmulatorLaunchArguments -Lane $lane -Mode $Mode -NoBootAnim:$NoBootAnim -GpuSwiftshader:$GpuSwiftshader -Headless:$Headless -PartitionSizeMb $PartitionSizeMb
    Write-Host ("  args: {0}" -f ($launchArguments -join " "))
}

foreach ($lane in $selectedLanes) {
    if ($RestartRunning -and $PSCmdlet.ShouldProcess($lane.serial, "Restart emulator lane")) {
        Stop-SenkuEmulatorLane -AdbPath $adbPath -Serial $lane.serial
    }

    $target = "{0} ({1})" -f $lane.serial, $lane.avd
    if ($PSCmdlet.ShouldProcess($target, ("Start emulator in {0} mode" -f $Mode))) {
        $process = Start-SenkuEmulatorLane -EmulatorPath $emulatorPath -Lane $lane -Mode $Mode -NoBootAnim:$NoBootAnim -GpuSwiftshader:$GpuSwiftshader -Headless:$Headless -PartitionSizeMb $PartitionSizeMb
        Write-Host ("Started {0} as PID {1}" -f $target, $process.Id)
    }
}

if ($Mode -eq "read_only") {
    Write-Host ""
    Write-Host "Read-only reminder:"
    Write-Host "- APK installs, app data, imported models, and pushed packs persist only for the current emulator session."
    Write-Host "- After a full emulator restart, do not assume -SkipInstall or prior imported-model state is still valid."
}
