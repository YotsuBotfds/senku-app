param(
    [string]$Emulator = "emulator-5554",
    [Parameter(Mandatory = $true)]
    [string]$InitialQuery,
    [Parameter(Mandatory = $true)]
    [string[]]$FollowUpQueries,
    [string]$OutputDir = "artifacts\\bench\\android_sessions",
    [string]$SessionLabel,
    [switch]$ClearChatFirst,
    [switch]$CaptureSessionPanelAfterTurn,
    [switch]$ContinueOnError,
    [int]$WaitSeconds = 5,
    [int]$MaxWaitSeconds = 180,
    [int]$PollSeconds = 5
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $commonHarnessModule)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}

Import-Module $commonHarnessModule -Force -DisableNameChecking
$hostAdbPlatformToolsVersion = Get-AndroidHostAdbPlatformToolsVersion -AdbPath $adb

if ($FollowUpQueries.Count -lt 1) {
    throw "Provide at least one follow-up query with -FollowUpQueries."
}

if ($PollSeconds -lt 1) {
    throw "PollSeconds must be at least 1."
}

if ($MaxWaitSeconds -lt $PollSeconds) {
    throw "MaxWaitSeconds must be greater than or equal to PollSeconds."
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

    $safeText = if ($null -eq $Text) { "" } else { $Text }
    $slug = $safeText.ToLowerInvariant() -replace "[^a-z0-9]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        $slug = "turn"
    }
    if ($slug.Length -gt 64) {
        $slug = $slug.Substring(0, 64).Trim("_")
    }
    return $slug
}

function Quote-AndroidShellArg {
    param([string]$Value)

    if ($null -eq $Value) {
        return "''"
    }

    return "'" + $Value.Replace("'", "'\''") + "'"
}

function Save-UiDump {
    param(
        [string]$DeviceDump,
        [string]$LocalDump
    )

    $localParent = Split-Path -Parent $LocalDump
    if ($localParent) {
        New-Item -ItemType Directory -Force -Path $localParent | Out-Null
    }

    for ($attempt = 0; $attempt -lt 3; $attempt++) {
        & $adb -s $Emulator shell rm -f $DeviceDump | Out-Null
        & $adb -s $Emulator shell uiautomator dump $DeviceDump | Out-Null
        $existsOutput = & $adb -s $Emulator shell "if [ -f '$DeviceDump' ]; then echo present; else echo missing; fi"
        if ($existsOutput -match "present") {
            & $adb -s $Emulator pull $DeviceDump $LocalDump | Out-Null
            return $true
        }
        Start-Sleep -Milliseconds 750
    }

    return $false
}

function Test-UiCompleted {
    param([string]$DumpFile)

    if (-not (Test-Path $DumpFile)) {
        return $false
    }

    $text = Get-Content $DumpFile -Raw
    if ($text -match 'resource-id="com\.senku\.mobile:id/detail_title"') {
        return $true
    }

    $busyMarkers = @(
        'Retrieving offline context...',
        'Retrieving offline context with chat memory...',
        'Generating answer locally...',
        'Searching for &quot;',
        'Searching for "',
        'Importing model...',
        'Installing offline pack...',
        'Installing pack...',
        'Reinstalling pack...'
    )
    foreach ($marker in $busyMarkers) {
        if ($text.Contains($marker)) {
            return $false
        }
    }

    if ($text -match 'resource-id="com\.senku\.mobile:id/status_text"') {
        return $true
    }

    return $false
}

function Get-UiSummary {
    param([string]$DumpFile)

    if (-not (Test-Path $DumpFile)) {
        return $null
    }

    try {
        $xml = [xml](Get-Content $DumpFile -Raw)
    } catch {
        return $null
    }

    $detailTitleNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_title"]' | Select-Object -First 1
    $detailSubtitleNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_subtitle"]' | Select-Object -First 1
    $detailBodyNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_body"]' | Select-Object -First 1
    $statusNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/status_text"]' | Select-Object -First 1
    $resultsNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/results_header"]' | Select-Object -First 1
    $sessionNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/session_text"]' | Select-Object -First 1

    return [pscustomobject]@{
        detail_title = if ($detailTitleNode) { $detailTitleNode.Node.text } else { $null }
        detail_subtitle = if ($detailSubtitleNode) { $detailSubtitleNode.Node.text } else { $null }
        detail_body = if ($detailBodyNode) { $detailBodyNode.Node.text } else { $null }
        status_text = if ($statusNode) { $statusNode.Node.text } else { $null }
        results_header = if ($resultsNode) { $resultsNode.Node.text } else { $null }
        session_text = if ($sessionNode) { $sessionNode.Node.text } else { $null }
    }
}

function Invoke-SenkuActivity {
    param(
        [string]$Query,
        [switch]$Ask
    )

    $encodedQuery = [System.Uri]::EscapeDataString($Query)
    $activityArgs = @(
        "-s", $Emulator,
        "shell", "am", "start",
        "--activity-clear-top",
        "--activity-single-top",
        "-n", "com.senku.mobile/.MainActivity",
        "--es", "com.senku.mobile.extra.PRODUCT_REVIEW_AUTOMATION_AUTH", "senku-review-demo-v1",
        "--es", "auto_query", (Quote-AndroidShellArg $encodedQuery)
    )
    if ($Ask) {
        $activityArgs += @("--ez", "auto_ask", "true")
    }

    & $adb @activityArgs | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "am start failed for query: $Query"
    }

    if ($WaitSeconds -gt 0) {
        Start-Sleep -Seconds $WaitSeconds
    }
}

function Wait-ForTurnCompletion {
    param([string]$StageLabel)

    $completionDump = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_session_wait_" + $Emulator.Replace("-", "_") + ".xml")
    $deviceDump = "/sdcard/senku_session_wait.xml"
    $elapsed = 0

    while ($elapsed -lt $MaxWaitSeconds) {
        if ((Save-UiDump -DeviceDump $deviceDump -LocalDump $completionDump) -and (Test-UiCompleted -DumpFile $completionDump)) {
            return [pscustomobject]@{
                Completed = $true
                DumpFile = $completionDump
                ElapsedSeconds = $elapsed
                Summary = Get-UiSummary -DumpFile $completionDump
                Stage = $StageLabel
            }
        }
        Start-Sleep -Seconds $PollSeconds
        $elapsed += $PollSeconds
    }

    Save-UiDump -DeviceDump $deviceDump -LocalDump $completionDump | Out-Null
    return [pscustomobject]@{
        Completed = $false
        DumpFile = $completionDump
        ElapsedSeconds = $elapsed
        Summary = Get-UiSummary -DumpFile $completionDump
        Stage = $StageLabel
    }
}

function Add-ManifestRecord {
    param(
        [string]$ManifestPath,
        [hashtable]$Record
    )

    ($Record | ConvertTo-Json -Compress) | Add-Content -Path $ManifestPath
}

$outputPath = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$safeEmulator = $Emulator.Replace("-", "_")
$label = if ([string]::IsNullOrWhiteSpace($SessionLabel)) {
    "${safeEmulator}_session_${timestamp}"
} else {
    ($SessionLabel -replace "[^A-Za-z0-9_-]+", "_")
}
$label = $label.Trim("_")
if ([string]::IsNullOrWhiteSpace($label)) {
    $label = "${safeEmulator}_session_${timestamp}"
}
$manifestPath = Join-Path $outputPath "${label}.jsonl"

$turns = @(
    [pscustomobject]@{
        Kind = "initial"
        Query = $InitialQuery
    }
)

for ($index = 0; $index -lt $FollowUpQueries.Count; $index++) {
    $turns += [pscustomobject]@{
        Kind = "follow_up"
        Query = $FollowUpQueries[$index]
    }
}

if ($ClearChatFirst) {
    Write-Host "Clearing session memory on $Emulator before the first turn"
    Invoke-SenkuActivity -Query "/reset"
    $resetResult = Wait-ForTurnCompletion -StageLabel "reset"
    if (-not $resetResult.Completed) {
        throw "Timed out waiting for /reset to complete on $Emulator"
    }
}

for ($turnIndex = 0; $turnIndex -lt $turns.Count; $turnIndex++) {
    $turn = $turns[$turnIndex]
    $slug = New-Slug -Text $turn.Query
    $ordinal = "{0:D3}" -f ($turnIndex + 1)
    $dumpPath = Join-Path $outputPath ("{0}_{1}_{2}_{3}.xml" -f $label, $ordinal, $turn.Kind, $slug)
    $sessionDumpPath = if ($CaptureSessionPanelAfterTurn) {
        Join-Path $outputPath ("{0}_{1}_{2}_{3}_session.xml" -f $label, $ordinal, $turn.Kind, $slug)
    } else {
        $null
    }

    Write-Host ("[{0}/{1}] {2}: {3}" -f ($turnIndex + 1), $turns.Count, $turn.Kind, $turn.Query)

    $startedAt = Get-Date
    $turnError = $null
    $answerSummary = $null
    $sessionSummary = $null
    $timedOut = $false

    try {
        Invoke-SenkuActivity -Query $turn.Query -Ask
        $turnResult = Wait-ForTurnCompletion -StageLabel "ask"
        if (-not $turnResult.Completed) {
            $timedOut = $true
            throw "Timed out waiting for turn completion after $($turnResult.ElapsedSeconds)s"
        }

        $answerSummary = $turnResult.Summary
        $deviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($dumpPath)
        if (-not (Save-UiDump -DeviceDump $deviceDump -LocalDump $dumpPath)) {
            throw "UI dump capture failed for $dumpPath"
        }

        if ($CaptureSessionPanelAfterTurn) {
            Invoke-SenkuActivity -Query "/state"
            $sessionResult = Wait-ForTurnCompletion -StageLabel "state"
            if (-not $sessionResult.Completed) {
                $timedOut = $true
                throw "Timed out waiting for /state after turn completion"
            }

            $sessionSummary = $sessionResult.Summary
            $sessionDeviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($sessionDumpPath)
            if (-not (Save-UiDump -DeviceDump $sessionDeviceDump -LocalDump $sessionDumpPath)) {
                throw "Session UI dump capture failed for $sessionDumpPath"
            }
        }
    } catch {
        $turnError = $_.Exception.Message
    }

    $finishedAt = Get-Date
    $record = [ordered]@{
        session_label = $label
        emulator = $Emulator
        host_adb_platform_tools_version = $hostAdbPlatformToolsVersion
        turn_index = $turnIndex + 1
        turn_kind = $turn.Kind
        query = $turn.Query
        answer_dump_path = $dumpPath
        session_dump_path = $sessionDumpPath
        started_at = $startedAt.ToString("o")
        finished_at = $finishedAt.ToString("o")
        elapsed_seconds = [math]::Round(($finishedAt - $startedAt).TotalSeconds, 1)
        timed_out = $timedOut
        error = $turnError
        detail_title = if ($answerSummary) { $answerSummary.detail_title } else { $null }
        detail_subtitle = if ($answerSummary) { $answerSummary.detail_subtitle } else { $null }
        detail_body = if ($answerSummary) { $answerSummary.detail_body } else { $null }
        status_text = if ($answerSummary) { $answerSummary.status_text } else { $null }
        results_header = if ($answerSummary) { $answerSummary.results_header } else { $null }
        session_results_header = if ($sessionSummary) { $sessionSummary.results_header } else { $null }
        session_text = if ($sessionSummary) { $sessionSummary.session_text } else { $null }
    }
    Add-ManifestRecord -ManifestPath $manifestPath -Record $record

    if ($turnError) {
        Write-Warning ("Turn {0} failed: {1}" -f ($turnIndex + 1), $turnError)
        if (-not $ContinueOnError) {
            throw $turnError
        }
    }
}

Write-Host "Session flow manifest saved to $manifestPath"
