param(
    [switch]$WhatIf
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$patterns = @(
    "run_android_prompt.ps1",
    "run_android_detail_followup.ps1",
    "run_android_functional_ux_smoke_matrix.ps1",
    "run_android_followup_suite.ps1",
    "run_android_instrumented_ui_smoke.ps1",
    "run_android_session_flow.ps1",
    "run_android_session_batch.ps1",
    "auto_followup_query"
)

function Matches-HarnessPattern {
    param([string]$CommandLine)

    if ([string]::IsNullOrWhiteSpace($CommandLine)) {
        return $false
    }

    foreach ($pattern in $patterns) {
        if ($CommandLine.IndexOf($pattern, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
            return $true
        }
    }

    return $false
}

function Add-Descendants {
    param(
        [int]$ProcessId,
        $Processes,
        [System.Collections.Generic.HashSet[int]]$Ids
    )

    if (-not $Ids.Add($ProcessId)) {
        return
    }

    $children = $Processes | Where-Object { $_.ParentProcessId -eq $ProcessId }
    foreach ($child in $children) {
        Add-Descendants -ProcessId ([int]$child.ProcessId) -Processes $Processes -Ids $Ids
    }
}

$allProcesses = Get-CimInstance Win32_Process
$rootProcesses = $allProcesses | Where-Object {
    Matches-HarnessPattern -CommandLine ([string]$_.CommandLine)
}

if (-not $rootProcesses) {
    Write-Host "No Android harness orphan roots found."
    exit 0
}

$targetIds = [System.Collections.Generic.HashSet[int]]::new()
foreach ($root in $rootProcesses) {
    Add-Descendants -ProcessId ([int]$root.ProcessId) -Processes $allProcesses -Ids $targetIds
}

$targets = $allProcesses |
    Where-Object { $targetIds.Contains([int]$_.ProcessId) } |
    Sort-Object CreationDate

if (-not $targets) {
    Write-Host "No Android harness orphan processes resolved."
    exit 0
}

Write-Host "Android harness processes:"
$targets |
    Select-Object Name, ProcessId, ParentProcessId, CreationDate, CommandLine |
    Format-Table -AutoSize

if ($WhatIf) {
    Write-Host "WhatIf set; no processes were stopped."
    exit 0
}

foreach ($target in ($targets | Sort-Object @{ Expression = "CreationDate"; Descending = $true })) {
    try {
        Stop-Process -Id $target.ProcessId -Force -ErrorAction Stop
    } catch {
        Write-Warning ("Failed to stop PID {0}: {1}" -f $target.ProcessId, $_.Exception.Message)
    }
}

Write-Host "Stopped Android harness orphan processes."
