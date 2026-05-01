[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("litert", "fastembed", "live-queue-monitor", "sidecar-viewer")]
    [string]$Service,
    [switch]$StopOnly,
    [switch]$StatusOnly,
    [string]$PythonPath = "",
    [string]$PowerShellPath = "powershell.exe"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$stateDir = Join-Path $repoRoot "artifacts\local_servers"
New-Item -ItemType Directory -Force -Path $stateDir | Out-Null

function Resolve-PythonCommand {
    if (-not [string]::IsNullOrWhiteSpace($PythonPath)) {
        return $PythonPath
    }
    if (-not [string]::IsNullOrWhiteSpace($env:SENKU_PYTHON_PATH)) {
        return $env:SENKU_PYTHON_PATH
    }
    $python = Get-Command python -ErrorAction SilentlyContinue
    if ($python) {
        return $python.Source
    }
    $repoPython = Join-Path $repoRoot ".venvs\senku-validate\Scripts\python.exe"
    if (Test-Path -LiteralPath $repoPython) {
        return $repoPython
    }
    throw "Python executable not found. Pass -PythonPath or set SENKU_PYTHON_PATH."
}

function New-ServiceSpec {
    param([string]$Name)

    $python = Resolve-PythonCommand
    switch ($Name) {
        "litert" {
            $script = Join-Path $repoRoot "scripts\start_litert_host_server.ps1"
            return [ordered]@{
                service = $Name
                port = 1235
                expected_script = (Resolve-Path -LiteralPath (Join-Path $repoRoot "scripts\litert_native_openai_server.py")).Path
                start_file = $PowerShellPath
                start_args = @("-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $script)
            }
        }
        "fastembed" {
            $script = Join-Path $repoRoot "scripts\start_fastembed_server.ps1"
            return [ordered]@{
                service = $Name
                port = 8801
                expected_script = (Resolve-Path -LiteralPath (Join-Path $repoRoot "scripts\fastembed_openai_server.py")).Path
                start_file = $PowerShellPath
                start_args = @("-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $script)
            }
        }
        "live-queue-monitor" {
            $script = (Resolve-Path -LiteralPath (Join-Path $repoRoot "scripts\live_queue_monitor.py")).Path
            return [ordered]@{
                service = $Name
                port = 8765
                expected_script = $script
                start_file = $python
                start_args = @($script, "--host", "127.0.0.1", "--port", "8765")
            }
        }
        "sidecar-viewer" {
            $script = (Resolve-Path -LiteralPath (Join-Path $repoRoot "tools\sidecar-viewer\server.py")).Path
            return [ordered]@{
                service = $Name
                port = 4320
                expected_script = $script
                start_file = $python
                start_args = @($script, "--host", "127.0.0.1", "--port", "4320")
            }
        }
    }
}

function Get-PortOwners {
    param([int]$Port)

    @(Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique |
        Where-Object { $_ -gt 0 })
}

function Get-ProcessCommandLine {
    param([int]$ProcessId)

    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $ProcessId" -ErrorAction SilentlyContinue
    if ($process) {
        return [string]$process.CommandLine
    }
    return ""
}

function Test-ExpectedCommandLine {
    param(
        [string]$CommandLine,
        [string]$ExpectedScript
    )

    if ([string]::IsNullOrWhiteSpace($CommandLine)) {
        return $false
    }
    $normalizedCommand = $CommandLine.Replace("/", "\").ToLowerInvariant()
    $normalizedScript = $ExpectedScript.Replace("/", "\").ToLowerInvariant()
    if ($normalizedCommand.Contains($normalizedScript)) {
        return $true
    }

    $normalizedRoot = $repoRoot.Replace("/", "\").TrimEnd("\").ToLowerInvariant()
    if ($normalizedScript.StartsWith($normalizedRoot + "\")) {
        $normalizedRelativeScript = $normalizedScript.Substring($normalizedRoot.Length + 1)
        return $normalizedCommand.Contains($normalizedRelativeScript)
    }
    return $false
}

function Get-PidFileRecord {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return $null
    }

    try {
        return Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
    }
    catch {
        return $null
    }
}

$spec = New-ServiceSpec -Name $Service
$pidPath = Join-Path $stateDir ($Service + ".pid.json")
$stdoutPath = Join-Path $stateDir ($Service + ".stdout.log")
$stderrPath = Join-Path $stateDir ($Service + ".stderr.log")

$owners = @(Get-PortOwners -Port $spec.port)
$ownerRows = @()
foreach ($ownerPid in $owners) {
    $commandLine = Get-ProcessCommandLine -ProcessId $ownerPid
    $matchesExpected = Test-ExpectedCommandLine -CommandLine $commandLine -ExpectedScript $spec.expected_script
    $ownerRows += [pscustomobject]@{
        pid = $ownerPid
        matches_expected = $matchesExpected
        command_line = $commandLine
    }
}

if ($StatusOnly) {
    $pidFileRecord = Get-PidFileRecord -Path $pidPath
    $pidFilePid = $null
    if ($pidFileRecord -and $pidFileRecord.pid) {
        $pidFilePid = [int]$pidFileRecord.pid
    }
    $listenerPids = @($ownerRows | Select-Object -ExpandProperty pid)
    $detail = "pidfile_pid is the launcher/wrapper process recorded at start; listener_pids own the port and may be a child server process."

    [pscustomobject]@{
        service = $Service
        port = $spec.port
        pidfile_pid = $pidFilePid
        listener_pids = $listenerPids
        status_note = $detail
        owners = $ownerRows
        pidfile = $pidPath
    } | ConvertTo-Json -Depth 5
    return
}

foreach ($owner in $ownerRows) {
    if (-not $owner.matches_expected) {
        throw "Refusing to stop PID $($owner.pid) on port $($spec.port); command line does not match expected script $($spec.expected_script): $($owner.command_line)"
    }
}

foreach ($owner in $ownerRows) {
    Write-Host "Stopping $Service PID $($owner.pid) on port $($spec.port)"
    Stop-Process -Id $owner.pid -Force -ErrorAction Stop
}

if ($StopOnly) {
    if (Test-Path -LiteralPath $pidPath) {
        Remove-Item -LiteralPath $pidPath -Force
    }
    Write-Host "Stopped $Service"
    return
}

$process = Start-Process -FilePath $spec.start_file `
    -ArgumentList $spec.start_args `
    -WorkingDirectory $repoRoot `
    -RedirectStandardOutput $stdoutPath `
    -RedirectStandardError $stderrPath `
    -PassThru `
    -WindowStyle Hidden

[ordered]@{
    service = $Service
    pid = $process.Id
    port = $spec.port
    expected_script = $spec.expected_script
    started_at = (Get-Date).ToString("o")
    stdout = $stdoutPath
    stderr = $stderrPath
} | ConvertTo-Json -Depth 4 | Set-Content -Path $pidPath -Encoding UTF8

Write-Host "Started $Service PID $($process.Id) on port $($spec.port)"
Write-Host "Logs: $stdoutPath ; $stderrPath"
