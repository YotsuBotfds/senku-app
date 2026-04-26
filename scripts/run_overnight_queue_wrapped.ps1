param(
    [Parameter(Mandatory = $true)][string]$QueueNote,
    [string]$Branch = "main",
    [int]$PerTaskTimeoutMinutes = 30,
    [string]$RunLogDir = "",
    [switch]$SkipBaselineChecks
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

function Resolve-RepoPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return Join-Path $repoRoot $Path
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

function Get-PythonCommand {
    foreach ($candidate in @("python3", "python")) {
        try {
            $null = & $candidate --version 2>&1
            if ($LASTEXITCODE -eq 0) {
                return $candidate
            }
        }
        catch {
        }
    }

    throw "Unable to find python3 or python on PATH."
}

function Invoke-NativeCommand {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [string[]]$Arguments = @()
    )

    $previousPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = "Continue"
        $output = & $FilePath @Arguments 2>&1
    }
    finally {
        $ErrorActionPreference = $previousPreference
    }

    $exitCode = $LASTEXITCODE
    $lines = @()
    foreach ($line in $output) {
        if ($null -ne $line) {
            $lines += $line.ToString()
        }
    }

    return [pscustomobject]@{
        ExitCode = $exitCode
        Lines = $lines
        Text = ($lines -join "`n").Trim()
    }
}

function Write-RunLog {
    param([string]$Line)
    Add-Content -Path $script:RunLogPath -Value $Line -Encoding UTF8
}

function Add-RunLogSection {
    param([string]$Title)
    Write-RunLog ""
    Write-RunLog "## $Title"
    Write-RunLog ""
}

function Add-PreflightStep {
    param(
        [Parameter(Mandatory = $true)][int]$Number,
        [Parameter(Mandatory = $true)][string]$Title,
        [Parameter(Mandatory = $true)][string]$Status,
        [string[]]$Details = @()
    )

    Write-RunLog "### $Number. $Title"
    Write-RunLog "**Status:** $Status"
    foreach ($detail in $Details) {
        Write-RunLog "**Details:** $detail"
    }
    Write-RunLog ""
}

function Add-ExitSection {
    param(
        [Parameter(Mandatory = $true)][string]$Heading,
        [Parameter(Mandatory = $true)][string]$Status,
        [Parameter(Mandatory = $true)][int]$ExitCode,
        [Parameter(Mandatory = $true)][string]$Notes
    )

    Add-RunLogSection -Title $Heading
    Write-RunLog "**Status:** $Status"
    Write-RunLog "**Exit code:** $ExitCode"
    Write-RunLog "**Notes:** $Notes"
}

function Get-QueueTaskIds {
    param([string]$QueueText)

    $matches = [regex]::Matches($QueueText, '(?m)^## Task \d+ - `(?<id>[^`]+)`')
    $ids = @()
    foreach ($match in $matches) {
        $ids += $match.Groups['id'].Value
    }
    return $ids
}

function Get-QueueTaskStatuses {
    param([string[]]$TaskIds)

    $trackerPath = Join-Path $repoRoot "reviewer_backend_tasks.md"
    $trackerText = ""
    if (Test-Path $trackerPath) {
        $trackerText = Get-Content -Raw -Path $trackerPath
    }

    $statuses = @()
    foreach ($taskId in $TaskIds) {
        $trackerDone = $false
        $trackerPattern = '(?m)^\| ' + [regex]::Escape($taskId) + ' \| done \|'
        if ($trackerText -match $trackerPattern) {
            $trackerDone = $true
        }

        $gitLog = Invoke-NativeCommand -FilePath "git" -Arguments @("log", "--format=%s", "--grep", ("^" + $taskId + " "), "-n", "1")
        $codeCommitSeen = ($gitLog.ExitCode -eq 0) -and -not [string]::IsNullOrWhiteSpace($gitLog.Text)

        $source = "pending"
        if ($trackerDone) {
            $source = "tracker"
        }
        elseif ($codeCommitSeen) {
            $source = "code-commit"
        }

        $statuses += [pscustomobject]@{
            TaskId = $taskId
            TrackerDone = $trackerDone
            CodeCommitSeen = $codeCommitSeen
            Complete = ($trackerDone -or $codeCommitSeen)
            Source = $source
        }
    }

    return $statuses
}

function Get-LatestTaskMarker {
    if (-not (Test-Path $script:RunLogPath)) {
        return $null
    }

    $content = Get-Content -Raw -Path $script:RunLogPath
    $matches = [regex]::Matches($content, "(?m)^## (?<task>[A-Z0-9-]+) - (?<stamp>\d{4}-\d{2}-\d{2}T[^\r\n]+)$")
    if ($matches.Count -eq 0) {
        return $null
    }

    $last = $matches[$matches.Count - 1]
    return [pscustomobject]@{
        TaskId = $last.Groups["task"].Value
        Timestamp = [datetimeoffset]::Parse($last.Groups["stamp"].Value)
    }
}

function Get-StaleProcesses {
    $cutoff = (Get-Date).AddHours(-1)
    $names = @("git", "python", "python3", "pwsh", "powershell")
    $stale = @()

    foreach ($name in $names) {
        foreach ($process in @(Get-Process -Name $name -ErrorAction SilentlyContinue)) {
            try {
                if ($process.StartTime -lt $cutoff) {
                    $stale += [pscustomobject]@{
                        Name = $process.ProcessName
                        Id = $process.Id
                        StartedAt = $process.StartTime
                    }
                }
            }
            catch {
            }
        }
    }

    return $stale
}

if ([string]::IsNullOrWhiteSpace($RunLogDir)) {
    $RunLogDir = Join-Path $repoRoot ("artifacts/overnight_queue_{0}" -f (Get-Date -Format "yyyyMMdd_HHmmss"))
}
else {
    $RunLogDir = Resolve-RepoPath -Path $RunLogDir
}

New-Item -ItemType Directory -Path $RunLogDir -Force | Out-Null
$script:RunLogPath = Join-Path $RunLogDir "run_log.md"

Set-Content -Path $script:RunLogPath -Encoding UTF8 -Value @(
    "# Overnight Queue Wrapper Run Log - $((Get-Date).ToString('o'))",
    "",
    "## Preflight",
    ""
)

$python = Get-PythonCommand
$shellExe = Get-ShellExecutable
$queueNotePath = Resolve-RepoPath -Path $QueueNote
$loopScript = Join-Path $repoRoot "scripts/overnight_continue_loop.ps1"

$step1Status = "pass"
$step1Details = @()
$indexLockPath = Join-Path $repoRoot ".git/index.lock"
if (Test-Path $indexLockPath) {
    $gitProcesses = @(Get-Process -Name "git" -ErrorAction SilentlyContinue)
    if ($gitProcesses.Count -gt 0) {
        $step1Status = "fail"
        $step1Details += "Found .git/index.lock and active git process ids: $((($gitProcesses | Select-Object -ExpandProperty Id) -join ', '))"
        Add-PreflightStep -Number 1 -Title "Stale lock sweep" -Status $step1Status -Details $step1Details
        Add-ExitSection -Heading "Wrapper Exit" -Status "blocked" -ExitCode 4 -Notes "Stale git lock cleanup was blocked by an active git process."
        exit 4
    }

    Remove-Item -LiteralPath $indexLockPath -Force
    $step1Details += "Removed stale lock at $indexLockPath because no git process was running."
}
else {
    $step1Details += "No .git/index.lock present."
}
Add-PreflightStep -Number 1 -Title "Stale lock sweep" -Status $step1Status -Details $step1Details

$trackedDiff = Invoke-NativeCommand -FilePath "git" -Arguments @("diff", "--name-only")
$stagedDiff = Invoke-NativeCommand -FilePath "git" -Arguments @("diff", "--cached", "--name-only")
if ($trackedDiff.ExitCode -ne 0 -or $stagedDiff.ExitCode -ne 0) {
    Add-PreflightStep -Number 2 -Title "Clean tracked tree check" -Status "fail" -Details @(
        "git diff exit=$($trackedDiff.ExitCode) output: $($trackedDiff.Text)",
        "git diff --cached exit=$($stagedDiff.ExitCode) output: $($stagedDiff.Text)"
    )
    Add-ExitSection -Heading "Wrapper Exit" -Status "failed" -ExitCode 1 -Notes "Tracked-tree check could not be completed."
    exit 1
}

if (-not [string]::IsNullOrWhiteSpace($trackedDiff.Text) -or -not [string]::IsNullOrWhiteSpace($stagedDiff.Text)) {
    $details = @(
        "git diff --name-only: $($trackedDiff.Text)",
        "git diff --cached --name-only: $($stagedDiff.Text)"
    )
    Add-PreflightStep -Number 2 -Title "Clean tracked tree check" -Status "fail" -Details $details
    Add-ExitSection -Heading "Wrapper Exit" -Status "failed" -ExitCode 1 -Notes "Tracked diffs or staged changes were present."
    exit 1
}

Add-PreflightStep -Number 2 -Title "Clean tracked tree check" -Status "pass" -Details @(
    "git diff --name-only returned empty output.",
    "git diff --cached --name-only returned empty output."
)

$headHash = (Invoke-NativeCommand -FilePath "git" -Arguments @("rev-parse", "HEAD")).Text
$currentBranch = (Invoke-NativeCommand -FilePath "git" -Arguments @("rev-parse", "--abbrev-ref", "HEAD")).Text
$branchOk = $false
$branchDetails = @(
    "HEAD: $headHash",
    "Current branch: $currentBranch",
    "Expected branch: $Branch"
)

if ($currentBranch -eq $Branch) {
    $branchOk = $true
}
elseif ($Branch -eq "main" -and $currentBranch -eq "master") {
    $mainRef = Invoke-NativeCommand -FilePath "git" -Arguments @("show-ref", "--verify", "--quiet", "refs/heads/main")
    if ($mainRef.ExitCode -ne 0) {
        $branchOk = $true
        $branchDetails += "Accepted master as the default-branch alias because refs/heads/main does not exist in this checkout."
    }
}

if (-not $branchOk) {
    Add-PreflightStep -Number 3 -Title "HEAD sanity" -Status "fail" -Details $branchDetails
    Add-ExitSection -Heading "Wrapper Exit" -Status "failed" -ExitCode 1 -Notes "HEAD was not on the expected branch."
    exit 1
}

Add-PreflightStep -Number 3 -Title "HEAD sanity" -Status "pass" -Details $branchDetails

if (-not (Test-Path -LiteralPath $loopScript -PathType Leaf)) {
    Add-PreflightStep -Number 4 -Title "Continuation loop present" -Status "fail" -Details @("Loop script not found: $loopScript")
    Add-ExitSection -Heading "Wrapper Exit" -Status "failed" -ExitCode 1 -Notes "Required overnight continuation loop script was missing."
    exit 1
}

Add-PreflightStep -Number 4 -Title "Continuation loop present" -Status "pass" -Details @("Loop script: $loopScript")

if ($SkipBaselineChecks) {
    Add-PreflightStep -Number 5 -Title "Baseline tests green" -Status "skipped" -Details @(
        "Skipped by -SkipBaselineChecks."
    )
}
else {
    $unitTests = Invoke-NativeCommand -FilePath $python -Arguments @("-m", "unittest", "discover", "-s", "tests", "-v")
    $validator = Invoke-NativeCommand -FilePath $python -Arguments @("scripts/validate_special_cases.py")
    $unitTestCount = ""
    if ($unitTests.Text -match "Ran (\d+) tests") {
        $unitTestCount = $matches[1]
    }
    $unitTestCountSuffix = if ($unitTestCount) { " count=$unitTestCount" } else { "" }
    $unitTestPassSuffix = if ($unitTestCount) { " ($unitTestCount tests)" } else { "" }

    if ($unitTests.ExitCode -ne 0 -or $validator.ExitCode -ne 0) {
        Add-PreflightStep -Number 5 -Title "Baseline tests green" -Status "fail" -Details @(
            "$python -m unittest discover -s tests -v exit=$($unitTests.ExitCode)$unitTestCountSuffix",
            "$python scripts/validate_special_cases.py exit=$($validator.ExitCode)"
        )
        Add-ExitSection -Heading "Wrapper Exit" -Status "failed" -ExitCode 1 -Notes "Baseline tests were not green."
        exit 1
    }

    Add-PreflightStep -Number 5 -Title "Baseline tests green" -Status "pass" -Details @(
        "$python -m unittest discover -s tests -v passed$unitTestPassSuffix.",
        "$python scripts/validate_special_cases.py passed."
    )
}

$validatorScript = @"
text = open('reviewer_backend_tasks.md', encoding='utf-8').read()
log = text.split('## State Log', 1)[1]
rows = [l for l in log.splitlines() if l.startswith('|') and ('BACK-' in l or 'OPUS-' in l or 'DAY-' in l)]
for r in rows:
    count = len(r.split('|'))
    assert count == 8, f'bad row: {r!r} ({count} cells)'
print(f'{len(rows)} rows validated')
"@
$trackerRows = Invoke-NativeCommand -FilePath $python -Arguments @("-c", $validatorScript)
if ($trackerRows.ExitCode -ne 0) {
    Add-PreflightStep -Number 6 -Title "State Log row validator" -Status "fail" -Details @($trackerRows.Text)
    Add-ExitSection -Heading "Wrapper Exit" -Status "failed" -ExitCode 1 -Notes "State Log row validation failed."
    exit 1
}

Add-PreflightStep -Number 6 -Title "State Log row validator" -Status "pass" -Details @($trackerRows.Text)

if (-not (Test-Path -LiteralPath $queueNotePath -PathType Leaf)) {
    Add-PreflightStep -Number 7 -Title "Queue note present and well-formed" -Status "fail" -Details @("Queue note not found: $queueNotePath")
    Add-ExitSection -Heading "Wrapper Exit" -Status "failed" -ExitCode 1 -Notes "Queue note was missing."
    exit 1
}

$queueText = Get-Content -Raw -Path $queueNotePath
$taskIds = Get-QueueTaskIds -QueueText $queueText
if ($taskIds.Count -eq 0) {
    Add-PreflightStep -Number 7 -Title "Queue note present and well-formed" -Status "fail" -Details @("Queue note does not contain any ## Task sections: $queueNotePath")
    Add-ExitSection -Heading "Wrapper Exit" -Status "failed" -ExitCode 1 -Notes "Queue note was malformed."
    exit 1
}

$taskStatuses = Get-QueueTaskStatuses -TaskIds $taskIds
$taskStatusSummary = @()
foreach ($taskStatus in $taskStatuses) {
    $taskStatusSummary += "$($taskStatus.TaskId): $($taskStatus.Source)"
}

Add-PreflightStep -Number 7 -Title "Queue note present and well-formed" -Status "pass" -Details @(
    "Queue note: $queueNotePath",
    "Task ids: $($taskIds -join ', ')",
    "Queue status: $($taskStatusSummary -join '; ')"
)

$staleProcesses = Get-StaleProcesses
if ($staleProcesses.Count -eq 0) {
    Add-PreflightStep -Number 8 -Title "Stale process sweep (advisory)" -Status "pass" -Details @("No git, python, or PowerShell processes older than one hour were found.")
}
else {
    $details = @()
    foreach ($process in $staleProcesses) {
        $details += "$($process.Name) pid=$($process.Id) started=$($process.StartedAt.ToString('o'))"
    }
    Add-PreflightStep -Number 8 -Title "Stale process sweep (advisory)" -Status "pass" -Details $details
}

Add-PreflightStep -Number 9 -Title "Run log directory prep" -Status "pass" -Details @(
    "Run log directory: $RunLogDir",
    "Run log path: $script:RunLogPath"
)

$pendingTasks = @($taskStatuses | Where-Object { -not $_.Complete })
if ($pendingTasks.Count -eq 0) {
    Add-ExitSection -Heading "Wrapper Exit" -Status "pass" -ExitCode 0 -Notes "Queue note tasks are already satisfied by tracker rows or matching code-commit subjects, so the wrapper skipped loop launch."
    exit 0
}

$firstPendingTask = $pendingTasks[0].TaskId
$seedMarker = Get-LatestTaskMarker
if ($null -eq $seedMarker) {
    Add-RunLogSection -Title "$firstPendingTask - $((Get-Date).ToString('o'))"
    Write-RunLog "**Status:** watchdog seed"
    Write-RunLog "**Notes:** Seeded the timeout clock from the first pending queue task because the wrapped continuation loop does not emit task markers on its own."
    $seedMarker = Get-LatestTaskMarker
}

$statusPath = Join-Path $RunLogDir "overnight_status.md"
$statePath = Join-Path $RunLogDir "OVERNIGHT_CONTINUATION_STATE.json"
$child = Start-Process -FilePath $shellExe -WorkingDirectory $repoRoot -WindowStyle Hidden -ArgumentList @(
    "-NoProfile",
    "-ExecutionPolicy",
    "Bypass",
    "-File",
    $loopScript,
    "-StatusPath",
    $statusPath,
    "-StatePath",
    $statePath
) -PassThru

$loopStartedAt = Get-Date
$currentMarker = $seedMarker
$watchStart = if ($null -ne $currentMarker) { $currentMarker.Timestamp } else { [datetimeoffset]::Now }

while ($true) {
    Start-Sleep -Seconds 60

    $latestMarker = Get-LatestTaskMarker
    if ($null -ne $latestMarker) {
        if ($null -eq $currentMarker -or $latestMarker.TaskId -ne $currentMarker.TaskId -or $latestMarker.Timestamp -ne $currentMarker.Timestamp) {
            $currentMarker = $latestMarker
            $watchStart = $latestMarker.Timestamp
        }
    }

    if ($child.HasExited) {
        $duration = New-TimeSpan -Start $loopStartedAt -End (Get-Date)
        $queueAfterExit = Get-QueueTaskStatuses -TaskIds $taskIds
        $queueComplete = (@($queueAfterExit | Where-Object { -not $_.Complete }).Count -eq 0)

        Add-RunLogSection -Title "Loop Exit"
        Write-RunLog "**Status:** $(if ($child.ExitCode -eq 0 -and $queueComplete) { 'pass' } else { 'fail' })"
        Write-RunLog "**Exit code:** $($child.ExitCode)"
        Write-RunLog "**Duration:** $($duration.ToString('hh\:mm\:ss'))"
        Write-RunLog "**Notes:** Queue complete after exit: $queueComplete"

        if ($child.ExitCode -eq 0 -and $queueComplete) {
            exit 0
        }

        exit 3
    }

    $elapsed = [datetimeoffset]::Now - $watchStart
    if ($elapsed.TotalMinutes -gt $PerTaskTimeoutMinutes) {
        Stop-Process -Id $child.Id -Force
        try {
            $child.WaitForExit()
        }
        catch {
        }

        Add-RunLogSection -Title "Wrapper Stop"
        Write-RunLog "**Status:** timeout"
        Write-RunLog "**Exit code:** 2"
        Write-RunLog "**Notes:** Killed loop process $($child.Id) after $([math]::Round($elapsed.TotalMinutes, 2)) minutes on task $($currentMarker.TaskId)."
        exit 2
    }
}
