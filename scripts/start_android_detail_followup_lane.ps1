param(
    [string]$Emulator = "emulator-5554",
    [Parameter(Mandatory = $true)]
    [string]$InitialQuery,
    [Parameter(Mandatory = $true)]
    [string]$FollowUpQuery,
    [string]$RunLabel,
    [ValidateSet("auto", "in_detail_ui", "auto_intent")]
    [string]$FollowUpSubmissionMode = "auto",
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "preserve",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string]$OutputDir = "artifacts\\bench\\android_detail_followups",
    [switch]$SkipSourceProbe,
    [int]$InitialMaxWaitSeconds = 260,
    [int]$FollowUpMaxWaitSeconds = 180,
    [int]$PollSeconds = 5
)

$ErrorActionPreference = "Stop"

function Quote-PowerShellArg {
    param([string]$Value)

    if ($null -eq $Value) {
        return "''"
    }

    return "'" + $Value.Replace("'", "''") + "'"
}

function New-Slug {
    param([string]$Text)

    $safeText = if ($null -eq $Text) { "" } else { [string]$Text }
    $slug = [regex]::Replace($safeText.ToLowerInvariant(), "[^a-z0-9]+", "_").Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "android_detail_followup"
    }
    return $slug
}

function Sanitize-RunLabel {
    param([string]$Text)

    $safeText = if ($null -eq $Text) { "" } else { [string]$Text }
    $slug = ($safeText -replace "[^A-Za-z0-9_-]+", "_").Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "android_detail_followup"
    }
    return $slug
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$detailScript = Join-Path $PSScriptRoot "run_android_detail_followup.ps1"
if (-not (Test-Path $detailScript)) {
    throw "detail follow-up script not found at $detailScript"
}

if ([string]::IsNullOrWhiteSpace($RunLabel)) {
    $RunLabel = "{0}_{1}" -f (New-Slug -Text $InitialQuery), (New-Slug -Text $FollowUpQuery)
} else {
    $RunLabel = Sanitize-RunLabel -Text $RunLabel
}

$resolvedOutputDir = if ([System.IO.Path]::IsPathRooted($OutputDir)) {
    $OutputDir
} else {
    Join-Path $repoRoot $OutputDir
}
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$stdoutLog = Join-Path $resolvedOutputDir ($RunLabel + ".stdout.log")
$stderrLog = Join-Path $resolvedOutputDir ($RunLabel + ".stderr.log")
$launchMeta = Join-Path $resolvedOutputDir ($RunLabel + ".launcher.json")
foreach ($path in @($stdoutLog, $stderrLog, $launchMeta)) {
    if (Test-Path $path) {
        Remove-Item -LiteralPath $path -Force
    }
}

$argParts = @(
    "& " + (Quote-PowerShellArg -Value $detailScript),
    "-Emulator " + (Quote-PowerShellArg -Value $Emulator),
    "-InitialQuery " + (Quote-PowerShellArg -Value $InitialQuery),
    "-FollowUpQuery " + (Quote-PowerShellArg -Value $FollowUpQuery),
    "-FollowUpSubmissionMode " + (Quote-PowerShellArg -Value $FollowUpSubmissionMode),
    "-InferenceMode " + (Quote-PowerShellArg -Value $InferenceMode),
    "-HostInferenceUrl " + (Quote-PowerShellArg -Value $HostInferenceUrl),
    "-HostInferenceModel " + (Quote-PowerShellArg -Value $HostInferenceModel),
    "-OutputDir " + (Quote-PowerShellArg -Value $OutputDir),
    "-RunLabel " + (Quote-PowerShellArg -Value $RunLabel),
    "-InitialMaxWaitSeconds " + $InitialMaxWaitSeconds,
    "-FollowUpMaxWaitSeconds " + $FollowUpMaxWaitSeconds,
    "-PollSeconds " + $PollSeconds
)
if ($SkipSourceProbe) {
    $argParts += "-SkipSourceProbe"
}
$command = $argParts -join " "

$proc = Start-Process `
    -FilePath "C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe" `
    -ArgumentList @("-ExecutionPolicy", "Bypass", "-Command", $command) `
    -WorkingDirectory $repoRoot `
    -PassThru `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog

$record = [pscustomobject]@{
    pid = $proc.Id
    emulator = $Emulator
    initial_query = $InitialQuery
    follow_up_query = $FollowUpQuery
    followup_submission_mode = $FollowUpSubmissionMode
    inference_mode = $InferenceMode
    output_dir = $resolvedOutputDir
    run_label = $RunLabel
    stdout_log = $stdoutLog
    stderr_log = $stderrLog
    launcher_script = $PSCommandPath
    detail_script = $detailScript
    started_at = (Get-Date).ToString("o")
}
$record | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $launchMeta -Encoding UTF8
$record | ConvertTo-Json -Depth 5
