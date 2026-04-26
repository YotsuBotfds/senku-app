param(
    [int]$IntervalMinutes = 5
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$scriptPath = Join-Path $repoRoot "scripts\overnight_continue_loop.ps1"

if (-not (Test-Path -LiteralPath $scriptPath -PathType Leaf)) {
    throw "Overnight continuation loop script not found: $scriptPath"
}

Start-Process powershell -WindowStyle Hidden -WorkingDirectory $repoRoot -ArgumentList @(
    "-NoProfile",
    "-ExecutionPolicy",
    "Bypass",
    "-File",
    $scriptPath,
    "-IntervalMinutes",
    $IntervalMinutes
)
