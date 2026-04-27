param(
    [string]$Emulator = "emulator-5554",
    [Parameter(Mandatory = $true)]
    [string]$Query,
    [switch]$WarmStart,
    [switch]$Ask,
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "preserve",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [int]$WaitSeconds = 5,
    [switch]$WaitForCompletion,
    [int]$MaxWaitSeconds = 180,
    [int]$PollSeconds = 5,
    [string]$OutputDir = "artifacts\\bench\\android_prompts",
    [string]$RunLabel,
    [string]$ExpectedDetailTitle,
    [string]$PushPackDir,
    [switch]$SkipPackPushIfCurrent,
    [switch]$ForcePackPush
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
$promptScript = Join-Path $PSScriptRoot "run_android_prompt.ps1"
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}
if (-not (Test-Path $promptScript)) {
    throw "run_android_prompt.ps1 not found at $promptScript"
}

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function Safe-Text {
    param([string]$Text)

    if ($null -eq $Text) {
        return ""
    }

    return [string]$Text
}

function New-Slug {
    param([string]$Text)

    $slug = (Safe-Text -Text $Text).ToLowerInvariant() -replace "[^a-z0-9]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "prompt_run"
    }
    return $slug
}

function Sanitize-RunLabel {
    param([string]$Text)

    $slug = (Safe-Text -Text $Text) -replace "[^A-Za-z0-9_-]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "prompt_run"
    }
    return $slug
}

function Stop-ConflictingHarnessRuns {
    param([string]$TargetEmulator)

    $emulatorPattern = [regex]::Escape($TargetEmulator)
    $pattern = "(?i)(?:^|\s)-Emulator(?:\s+|:)(?:`"$emulatorPattern`"|'$emulatorPattern'|$emulatorPattern)(?=\s|$)"
    $conflicts = Get-CimInstance Win32_Process | Where-Object {
        $_.ProcessId -ne $PID -and
        $_.CommandLine -and
        (($_.CommandLine -like '*run_android_prompt_logged.ps1*') -or ($_.CommandLine -like '*run_android_prompt.ps1*')) -and
        $_.CommandLine -match $pattern
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
        Write-Warning ("Stopped conflicting prompt harness runs on {0}: {1}" -f $TargetEmulator, ($stopped -join ", "))
    }
}

function Get-DumpSummary {
    param([string]$DumpPath)

    $record = [ordered]@{
        detail_detected = $false
        detail_title = $null
        detail_subtitle = $null
        status_text = $null
    }

    if (-not (Test-Path $DumpPath)) {
        return [pscustomobject]$record
    }

    try {
        $xml = [xml](Get-Content -Path $DumpPath -Raw)
    } catch {
        return [pscustomobject]$record
    }

    if ($null -eq $xml -or $null -eq $xml.DocumentElement) {
        return [pscustomobject]$record
    }

    $detailTitleNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_title"]' | Select-Object -First 1
    $detailSubtitleNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_subtitle"]' | Select-Object -First 1
    $statusNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/status_text" or @resource-id="com.senku.mobile:id/detail_status_text"]' | Select-Object -First 1

    $record.detail_detected = [bool]$detailTitleNode
    $record.detail_title = if ($detailTitleNode) { [string]$detailTitleNode.Node.text } else { $null }
    $record.detail_subtitle = if ($detailSubtitleNode) { [string]$detailSubtitleNode.Node.text } else { $null }
    $record.status_text = if ($statusNode) { [string]$statusNode.Node.text } else { $null }
    return [pscustomobject]$record
}

function Read-JsonFileOrNull {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) {
        return $null
    }

    try {
        return (Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json)
    } catch {
        return $null
    }
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
if ((Test-Path $resolvedOutputDir) -and -not (Test-Path $resolvedOutputDir -PathType Container)) {
    throw "OutputDir must be a directory path, but an existing file was found at $resolvedOutputDir"
}
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null
$startedAt = (Get-Date).ToString("o")

if ([string]::IsNullOrWhiteSpace($RunLabel)) {
    $RunLabel = "prompt__{0}__{1}" -f (New-Slug -Text $Query), ($Emulator -replace "[^a-zA-Z0-9]+", "_")
} else {
    $RunLabel = Sanitize-RunLabel -Text $RunLabel
}

$dumpPath = Join-Path $resolvedOutputDir ($RunLabel + ".xml")
$manifestPath = Join-Path $resolvedOutputDir ($RunLabel + ".json")
$logcatPath = Join-Path $resolvedOutputDir ($RunLabel + ".logcat.txt")
$pushPackSummaryPath = Join-Path $resolvedOutputDir ($RunLabel + ".pack_push.json")

$effectiveExpectedTitle = $ExpectedDetailTitle
if ([string]::IsNullOrWhiteSpace($effectiveExpectedTitle) -and $Ask -and $WaitForCompletion) {
    $effectiveExpectedTitle = $Query
}

Stop-ConflictingHarnessRuns -TargetEmulator $Emulator

$scriptArgs = @{
    Emulator = $Emulator
    Query = $Query
    InferenceMode = $InferenceMode
    HostInferenceUrl = $HostInferenceUrl
    HostInferenceModel = $HostInferenceModel
    WaitSeconds = $WaitSeconds
    MaxWaitSeconds = $MaxWaitSeconds
    PollSeconds = $PollSeconds
    DumpPath = $dumpPath
}
if ($WarmStart) {
    $scriptArgs.WarmStart = $true
}
if ($Ask) {
    $scriptArgs.Ask = $true
}
if ($WaitForCompletion) {
    $scriptArgs.WaitForCompletion = $true
}
if (-not [string]::IsNullOrWhiteSpace($effectiveExpectedTitle)) {
    $scriptArgs.ExpectedDetailTitle = $effectiveExpectedTitle
}
if (-not [string]::IsNullOrWhiteSpace($PushPackDir)) {
    $scriptArgs.PushPackDir = $PushPackDir
    $scriptArgs.PushPackSummaryPath = $pushPackSummaryPath
}
if ($SkipPackPushIfCurrent) {
    $scriptArgs.SkipPackPushIfCurrent = $true
}
if ($ForcePackPush) {
    $scriptArgs.ForcePackPush = $true
}

$capturedError = $null
$hasNativeCommandPreference = $null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)
$originalNativeCommandPreference = $false
if ($hasNativeCommandPreference) {
    $originalNativeCommandPreference = $PSNativeCommandUseErrorActionPreference
    $PSNativeCommandUseErrorActionPreference = $false
}
& $adb -s $Emulator logcat -c | Out-Null
try {
    & $promptScript @scriptArgs
} catch {
    $capturedError = $_
} finally {
    if ($hasNativeCommandPreference) {
        $PSNativeCommandUseErrorActionPreference = $originalNativeCommandPreference
    }
    & $adb -s $Emulator logcat -d | Out-File -FilePath $logcatPath -Encoding utf8
}

$dumpSummary = Get-DumpSummary -DumpPath $dumpPath
$detailTitleMatchesExpected = $null
$detailMismatchMessage = $null
if (-not [string]::IsNullOrWhiteSpace($effectiveExpectedTitle) -and $dumpSummary.detail_detected) {
    $detailTitleMatchesExpected = [string]::Equals(
        (Safe-Text -Text $dumpSummary.detail_title).Trim(),
        (Safe-Text -Text $effectiveExpectedTitle).Trim(),
        [System.StringComparison]::OrdinalIgnoreCase
    )
    if (-not $detailTitleMatchesExpected) {
        $detailMismatchMessage = "Detail title mismatch: expected '$effectiveExpectedTitle' but found '$($dumpSummary.detail_title)'"
    }
}
$pushPackSummary = Read-JsonFileOrNull -Path $pushPackSummaryPath
$record = [ordered]@{
    emulator = $Emulator
    inference_mode = $InferenceMode
    host_inference_url = $HostInferenceUrl
    host_inference_model = $HostInferenceModel
    query = $Query
    warm_start = [bool]$WarmStart
    ask = [bool]$Ask
    wait_for_completion = [bool]$WaitForCompletion
    expected_detail_title = $effectiveExpectedTitle
    push_pack_summary_path = $(if (Test-Path -LiteralPath $pushPackSummaryPath) { $pushPackSummaryPath } else { $null })
    push_pack_cache_hit = $(if ($pushPackSummary) { [bool]$pushPackSummary.cache_hit } else { $null })
    push_pack_pushed = $(if ($pushPackSummary) { [bool]$pushPackSummary.pushed } else { $null })
    push_pack_state_path = $(if ($pushPackSummary) { [string]$pushPackSummary.state_path } else { $null })
    dump_path = $dumpPath
    logcat_path = $logcatPath
    detail_detected = $dumpSummary.detail_detected
    detail_title = $dumpSummary.detail_title
    detail_subtitle = $dumpSummary.detail_subtitle
    detail_title_matches_expected = $detailTitleMatchesExpected
    status_text = $dumpSummary.status_text
    error_message = if ($capturedError) {
        $capturedError.Exception.Message
    } elseif ($detailMismatchMessage) {
        $detailMismatchMessage
    } else {
        $null
    }
    started_at = $startedAt
    completed_at = (Get-Date).ToString("o")
}
$record | ConvertTo-Json -Depth 5 | Set-Content -Path $manifestPath -Encoding UTF8
Write-Host "Prompt artifacts written to $manifestPath"

if ($null -ne $capturedError) {
    throw $capturedError
}
if ($detailMismatchMessage) {
    throw $detailMismatchMessage
}
