$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location $repoRoot

function Assert-True {
    param(
        [Parameter(Mandatory = $true)][bool]$Condition,
        [Parameter(Mandatory = $true)][string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Get-ShellExecutable {
    foreach ($candidate in @("pwsh", "powershell")) {
        try {
            $null = Get-Command $candidate -ErrorAction Stop
            return $candidate
        }
        catch {
        }
    }

    throw "Unable to find pwsh or powershell."
}

$shellExe = Get-ShellExecutable
$currentBranch = (& git rev-parse --abbrev-ref HEAD 2>&1 | Out-String).Trim()
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("overnight_wrapper_test_" + [guid]::NewGuid().ToString("N"))
$queueNotePath = Join-Path $tempRoot "mock_queue.md"
$runLogDir = Join-Path $tempRoot "run"

try {
    New-Item -ItemType Directory -Path $tempRoot -Force | Out-Null
    @(
        "# Mock Queue",
        "",
        "## Task 1 - `DAY-G-01` - Already landed"
    ) | Set-Content -Path $queueNotePath -Encoding UTF8

    $wrapperScriptPath = (Join-Path $repoRoot "scripts/run_overnight_queue_wrapped.ps1").Replace("'", "''")
    $escapedQueueNotePath = $queueNotePath.Replace("'", "''")
    $escapedBranch = $currentBranch.Replace("'", "''")
    $escapedRunLogDir = $runLogDir.Replace("'", "''")
    $wrapperCommand = "& { & '$wrapperScriptPath' -QueueNote '$escapedQueueNotePath' -Branch '$escapedBranch' -RunLogDir '$escapedRunLogDir'; exit `$LASTEXITCODE }"

    $wrapper = Start-Process -FilePath $shellExe -WorkingDirectory $repoRoot -PassThru -Wait -NoNewWindow -ArgumentList @(
        "-NoProfile",
        "-Command",
        $wrapperCommand
    )

    $runLogPath = Join-Path $runLogDir "run_log.md"
    Assert-True (Test-Path $runLogPath) "Expected wrapper run log at $runLogPath."

    $runLog = Get-Content -Raw -Path $runLogPath
    foreach ($required in @(
        "## Preflight",
        "### 1. Stale lock sweep",
        "### 6. Queue note present and well-formed",
        "## Wrapper Exit",
        "Queue note tasks are already satisfied"
    )) {
        Assert-True ($runLog.Contains($required)) "Expected run log to contain '$required'."
    }
    Assert-True ($runLog.Contains("**Exit code:** 0")) "Expected wrapper run log to record exit code 0."

    Write-Host "Run-OvernightQueueWrapperTests.ps1 passed"
}
finally {
    if (Test-Path $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
