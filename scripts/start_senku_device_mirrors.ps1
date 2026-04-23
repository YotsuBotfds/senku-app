param(
    [string]$PhoneDevice = "RFCX607ZM8L",
    [string]$TabletDevice = "R92X51AG48D",
    [switch]$PhoneOnly,
    [switch]$TabletOnly,
    [switch]$ControlEnabled,
    [switch]$AlwaysOnTop = $true,
    [int]$PhoneMaxSize = 540,
    [int]$TabletMaxSize = 0
)

$ErrorActionPreference = "Stop"

function Resolve-ScrcpyPath {
    $running = Get-Process scrcpy -ErrorAction SilentlyContinue |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_.Path) } |
        Select-Object -First 1 -ExpandProperty Path
    if (-not [string]::IsNullOrWhiteSpace($running) -and (Test-Path -LiteralPath $running)) {
        return $running
    }

    $wingetRoot = Join-Path $env:LOCALAPPDATA "Microsoft\\WinGet\\Packages"
    if (Test-Path -LiteralPath $wingetRoot) {
        $candidate = Get-ChildItem -LiteralPath $wingetRoot -Directory -Filter "Genymobile.scrcpy*" -ErrorAction SilentlyContinue |
            Sort-Object LastWriteTime -Descending |
            ForEach-Object {
                Get-ChildItem -LiteralPath $_.FullName -Recurse -File -Filter "scrcpy.exe" -ErrorAction SilentlyContinue
            } |
            Sort-Object FullName -Descending |
            Select-Object -First 1 -ExpandProperty FullName
        if (-not [string]::IsNullOrWhiteSpace($candidate) -and (Test-Path -LiteralPath $candidate)) {
            return $candidate
        }
    }

    $command = Get-Command scrcpy.exe -ErrorAction SilentlyContinue
    if ($command -and (Test-Path -LiteralPath $command.Source)) {
        return $command.Source
    }

    throw "Could not locate scrcpy.exe"
}

function Stop-MirrorForDevice {
    param([string]$Device)

    Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
        Where-Object {
            $_.Name -eq "scrcpy.exe" -and
            $_.CommandLine -and
            $_.CommandLine -match [regex]::Escape($Device)
        } |
        ForEach-Object {
            Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
        }
}

function Start-MirrorWindow {
    param(
        [string]$ScrcpyPath,
        [string]$Device,
        [string]$Title,
        [int]$MaxSize
    )

    Stop-MirrorForDevice -Device $Device
    Start-Sleep -Milliseconds 500

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $ScrcpyPath
    $psi.WorkingDirectory = Split-Path -Parent $ScrcpyPath
    $psi.UseShellExecute = $true

    $argumentParts = New-Object System.Collections.Generic.List[string]
    $argumentParts.Add("-s")
    $argumentParts.Add($Device)
    $argumentParts.Add("--window-title")
    $argumentParts.Add('"' + $Title.Replace('"', '\"') + '"')
    if (-not $ControlEnabled) {
        $argumentParts.Add("--no-control")
    }
    if ($AlwaysOnTop) {
        $argumentParts.Add("--always-on-top")
    }
    if ($MaxSize -gt 0) {
        $argumentParts.Add("--max-size")
        $argumentParts.Add($MaxSize.ToString())
    }
    $psi.Arguments = ($argumentParts -join " ")

    $process = [System.Diagnostics.Process]::Start($psi)
    if ($null -eq $process) {
        throw "scrcpy did not start for $Device"
    }

    Start-Sleep -Seconds 4
    $live = Get-Process -Id $process.Id -ErrorAction SilentlyContinue
    if ($null -eq $live) {
        throw "scrcpy exited immediately for $Device"
    }

    return [pscustomobject]@{
        device = $Device
        process_id = $live.Id
        title = $live.MainWindowTitle
        responding = $live.Responding
        path = $live.Path
    }
}

$scrcpyPath = Resolve-ScrcpyPath
$targets = New-Object System.Collections.Generic.List[object]

if (-not $TabletOnly) {
    $targets.Add([pscustomobject]@{
        device = $PhoneDevice
        title = "Senku Phone $PhoneDevice"
        max_size = $PhoneMaxSize
    })
}
if (-not $PhoneOnly) {
    $targets.Add([pscustomobject]@{
        device = $TabletDevice
        title = "Senku Tablet $TabletDevice"
        max_size = $TabletMaxSize
    })
}
if ($targets.Count -eq 0) {
    throw "Nothing to launch. Remove -PhoneOnly/-TabletOnly conflict."
}

$results = foreach ($target in $targets) {
    Start-MirrorWindow -ScrcpyPath $scrcpyPath -Device $target.device -Title $target.title -MaxSize $target.max_size
}

$results | ConvertTo-Json -Depth 4
