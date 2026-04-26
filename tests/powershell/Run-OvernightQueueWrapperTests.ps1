$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location $repoRoot

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
$escapedRepoRoot = $repoRoot.Replace("'", "''")
$innerScript = @"
`$ErrorActionPreference = 'Stop'
Set-Location '$escapedRepoRoot'

function Assert-True {
    param([bool]`$Condition, [string]`$Message)
    if (-not `$Condition) {
        throw `$Message
    }
}

`$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ('overnight_wrapper_test_' + [guid]::NewGuid().ToString('N'))
`$queueNotePath = Join-Path `$tempRoot 'mock_queue.md'
`$runLogDir = Join-Path `$tempRoot 'run'
`$missingRunLogDir = Join-Path `$tempRoot 'missing-run'
`$loopScriptPath = Join-Path '$escapedRepoRoot' 'scripts/overnight_continue_loop.ps1'
`$createdLoopStub = `$false

try {
    New-Item -ItemType Directory -Path `$tempRoot -Force | Out-Null
    @('# Mock Queue', '', '## Task 1 - ``DAY-G-01`` - Already landed') | Set-Content -Path `$queueNotePath -Encoding UTF8
    `$branch = (& git rev-parse --abbrev-ref HEAD 2>`$null).Trim()

    if (-not (Test-Path -LiteralPath `$loopScriptPath -PathType Leaf)) {
        powershell -NoProfile -File (Join-Path '$escapedRepoRoot' 'scripts/run_overnight_queue_wrapped.ps1') -QueueNote `$queueNotePath -Branch `$branch -RunLogDir `$missingRunLogDir
        Assert-True (`$LASTEXITCODE -eq 1) "Expected missing loop exit code 1, got `$LASTEXITCODE."
        `$missingRunLogPath = Join-Path `$missingRunLogDir 'run_log.md'
        Assert-True (Test-Path `$missingRunLogPath) "Expected missing-loop run log at `$missingRunLogPath."
        `$missingRunLog = Get-Content -Raw -Path `$missingRunLogPath
        Assert-True (`$missingRunLog.Contains('### 4. Continuation loop present')) "Expected missing-loop preflight step."
        Assert-True (`$missingRunLog.Contains('Loop script not found')) "Expected missing-loop detail."

        Set-Content -Path `$loopScriptPath -Encoding UTF8 -Value @(
            'param([string]$StatusPath = "", [string]$StatePath = "")',
            'if ($StatusPath) { Set-Content -Path $StatusPath -Encoding UTF8 -Value "# stub" }',
            'if ($StatePath) { Set-Content -Path $StatePath -Encoding UTF8 -Value "{}" }',
            'exit 0'
        )
        `$createdLoopStub = `$true
    }

    powershell -NoProfile -File (Join-Path '$escapedRepoRoot' 'scripts/run_overnight_queue_wrapped.ps1') -QueueNote `$queueNotePath -Branch `$branch -RunLogDir `$runLogDir -SkipBaselineChecks
    Assert-True (`$LASTEXITCODE -eq 0) "Expected wrapper child exit code 0, got `$LASTEXITCODE."
    `$runLogPath = Join-Path `$runLogDir 'run_log.md'
    Assert-True (Test-Path `$runLogPath) "Expected wrapper run log at `$runLogPath."
    `$runLog = Get-Content -Raw -Path `$runLogPath
    foreach (`$required in @('## Preflight', '### 1. Stale lock sweep', '### 4. Continuation loop present', '### 7. Queue note present and well-formed', '## Wrapper Exit', '**Exit code:** 0')) {
        Assert-True (`$runLog.Contains(`$required)) "Expected run log to contain '`$required'."
    }
}
finally {
    if (`$createdLoopStub -and (Test-Path -LiteralPath `$loopScriptPath)) {
        Remove-Item -LiteralPath `$loopScriptPath -Force
    }
    if (Test-Path `$tempRoot) {
        Remove-Item -LiteralPath `$tempRoot -Recurse -Force
    }
}
"@

& $shellExe -NoProfile -Command $innerScript
if ($LASTEXITCODE -ne 0) {
    throw "Run-OvernightQueueWrapperTests.ps1 failed with exit code $LASTEXITCODE."
}

Write-Host "Run-OvernightQueueWrapperTests.ps1 passed"
