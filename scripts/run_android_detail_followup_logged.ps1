param(
    [string]$Emulator = "emulator-5554",
    [Parameter(Mandatory = $true)]
    [string]$InitialQuery,
    [Parameter(Mandatory = $true)]
    [string]$FollowUpQuery,
    [string]$PushPackDir,
    [switch]$WarmStart,
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "preserve",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string]$OutputDir = "artifacts\\bench\\android_detail_followups",
    [string]$RunLabel,
    [switch]$SkipSourceProbe,
    [switch]$SkipPackPushIfCurrent,
    [switch]$ForcePackPush,
    [int]$InitialMaxWaitSeconds = 260,
    [int]$FollowUpMaxWaitSeconds = 180,
    [int]$PollSeconds = 5
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function New-Slug {
    param([string]$Text)

    if ([string]::IsNullOrWhiteSpace($Text)) {
        return "run"
    }

    $slug = $Text.ToLowerInvariant() -replace "[^a-z0-9]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "run"
    }
    return $slug
}

function Sanitize-RunLabel {
    param([string]$Text)

    $safeText = if ($null -eq $Text) { "" } else { [string]$Text }
    $slug = $safeText -replace "[^A-Za-z0-9_-]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "run"
    }
    return $slug
}

function Stop-ConflictingHarnessRuns {
    param([string]$TargetEmulator)

    $pattern = "*-Emulator $TargetEmulator*"
    $conflicts = Get-CimInstance Win32_Process | Where-Object {
        $_.ProcessId -ne $PID -and
        $_.CommandLine -and
        (($_.CommandLine -like '*run_android_detail_followup_logged.ps1*') -or ($_.CommandLine -like '*run_android_detail_followup.ps1*')) -and
        $_.CommandLine -like $pattern
    }

    if (-not $conflicts) {
        return
    }

    $stopped = @()
    foreach ($conflict in $conflicts) {
        try {
            Stop-Process -Id $conflict.ProcessId -Force -ErrorAction Stop
            $stopped += $conflict.ProcessId
        } catch {
        }
    }

    if ($stopped.Count -gt 0) {
        Write-Warning ("Stopped conflicting harness runs on {0}: {1}" -f $TargetEmulator, ($stopped -join ", "))
    }
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null
$startedAt = (Get-Date).ToString("o")

if ([string]::IsNullOrWhiteSpace($RunLabel)) {
    $RunLabel = "{0}__{1}__{2}" -f (New-Slug $InitialQuery), (New-Slug $FollowUpQuery), ($Emulator -replace "[^a-zA-Z0-9]+", "_")
} else {
    $RunLabel = Sanitize-RunLabel -Text $RunLabel
}

$logcatPath = Join-Path $resolvedOutputDir ($RunLabel + ".logcat.txt")
$manifestPath = Join-Path $resolvedOutputDir ($RunLabel + ".json")
$followupScript = Join-Path $PSScriptRoot "run_android_detail_followup.ps1"

if (-not (Test-Path $followupScript)) {
    throw "run_android_detail_followup.ps1 not found at $followupScript"
}

$scriptArgs = @{
    Emulator = $Emulator
    InitialQuery = $InitialQuery
    FollowUpQuery = $FollowUpQuery
    InferenceMode = $InferenceMode
    HostInferenceUrl = $HostInferenceUrl
    HostInferenceModel = $HostInferenceModel
    OutputDir = $OutputDir
    RunLabel = $RunLabel
    InitialMaxWaitSeconds = $InitialMaxWaitSeconds
    FollowUpMaxWaitSeconds = $FollowUpMaxWaitSeconds
    PollSeconds = $PollSeconds
}
if (-not [string]::IsNullOrWhiteSpace($PushPackDir)) {
    $scriptArgs.PushPackDir = $PushPackDir
}
if ($SkipPackPushIfCurrent) {
    $scriptArgs.SkipPackPushIfCurrent = $true
}
if ($ForcePackPush) {
    $scriptArgs.ForcePackPush = $true
}
if ($WarmStart) {
    $scriptArgs.WarmStart = $true
}
if ($SkipSourceProbe) {
    $scriptArgs.SkipSourceProbe = $true
}

Stop-ConflictingHarnessRuns -TargetEmulator $Emulator

$capturedError = $null
$hasNativeCommandPreference = $null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)
$originalNativeCommandPreference = $false
if ($hasNativeCommandPreference) {
    $originalNativeCommandPreference = $PSNativeCommandUseErrorActionPreference
    $PSNativeCommandUseErrorActionPreference = $false
}
& $adb -s $Emulator logcat -c | Out-Null
try {
    & $followupScript @scriptArgs
} catch {
    $capturedError = $_
} finally {
    if ($hasNativeCommandPreference) {
        $PSNativeCommandUseErrorActionPreference = $originalNativeCommandPreference
    }
    & $adb -s $Emulator logcat -d | Out-File -FilePath $logcatPath -Encoding utf8
    Write-Host "Logcat saved to $logcatPath"
}

if ($null -ne $capturedError -and -not (Test-Path $manifestPath)) {
    $record = [ordered]@{
        emulator = $Emulator
        inference_mode = $InferenceMode
        host_inference_url = $HostInferenceUrl
        host_inference_model = $HostInferenceModel
        initial_query = $InitialQuery
        requested_follow_up = $FollowUpQuery
        warm_start = [bool]$WarmStart
        logcat_path = $logcatPath
        error_message = $capturedError.Exception.Message
        started_at = $startedAt
        completed_at = (Get-Date).ToString("o")
    }
    $record | ConvertTo-Json -Depth 5 | Set-Content -Path $manifestPath -Encoding UTF8
}

if ($null -ne $capturedError) {
    throw $capturedError
}
