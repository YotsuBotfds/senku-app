param(
    [string]$Emulator = "emulator-5554",
    [Parameter(Mandatory = $true)]
    [string]$Query,
    [string]$FollowUpQuery = "",
    [string]$PushPackDir,
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
    [string]$DumpPath,
    [string]$ExpectedDetailTitle,
    [switch]$ClearLogcatBeforeLaunch,
    [string]$LogcatPath,
    [string]$LogcatSpec = "SenkuPackRepo:D SenkuMobile:D *:S",
    [switch]$UseInstrumentationPreflight,
    [switch]$UseInstrumentationExecution,
    [switch]$ForceShellExecution,
    [switch]$SkipDeviceLock
)

if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$lockRoot = Join-Path $repoRoot "artifacts\\harness_locks"
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
if (-not (Test-Path -LiteralPath $commonHarnessModule)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}
Import-Module $commonHarnessModule -Force -DisableNameChecking
New-Item -ItemType Directory -Force -Path $lockRoot | Out-Null

function Acquire-DeviceLock {
    param(
        [string]$DeviceName,
        [int]$TimeoutSeconds = 900
    )

    return Acquire-AndroidHarnessDeviceLock -DeviceName $DeviceName -LockRoot $lockRoot -TimeoutSeconds $TimeoutSeconds
}

$deviceLock = $null
try {

$pushPackScript = Join-Path $PSScriptRoot "push_mobile_pack_to_android.ps1"
$instrumentedSmokeScript = Join-Path $PSScriptRoot "run_android_instrumented_ui_smoke.ps1"
if (-not [string]::IsNullOrWhiteSpace($PushPackDir)) {
    if (-not (Test-Path $pushPackScript)) {
        throw "push script not found at $pushPackScript"
    }
    & $pushPackScript -Device $Emulator -PackDir $PushPackDir -ForceStop
}

$resolvedInstrumentationExecution = $UseInstrumentationExecution -or (-not $ForceShellExecution -and -not $WarmStart)
if (-not $resolvedInstrumentationExecution) {
    Write-Warning "run_android_prompt.ps1 is using the legacy shell fallback path; prefer instrumentation execution unless you explicitly need warm-start shell behavior."
}

if ($UseInstrumentationPreflight) {
    if (-not (Test-Path -LiteralPath $instrumentedSmokeScript)) {
        throw "instrumented smoke script not found at $instrumentedSmokeScript"
    }
    $preflightArgs = @(
        "-ExecutionPolicy", "Bypass",
        "-File", $instrumentedSmokeScript,
        "-Device", $Emulator,
        "-SkipBuild"
    )
    if ($Ask -and $InferenceMode -eq "host" -and -not [string]::IsNullOrWhiteSpace($HostInferenceUrl)) {
        $preflightArgs += @(
            "-SmokeProfile", "host",
            "-EnableHostInferenceSmoke",
            "-HostInferenceUrl", $HostInferenceUrl,
            "-HostInferenceModel", $HostInferenceModel
        )
    } else {
        $preflightArgs += @("-SmokeProfile", "basic")
    }
    Write-Host ("Running instrumentation preflight on {0}..." -f $Emulator)
    & powershell @preflightArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Instrumentation preflight failed"
    }
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
        $directDump = (& $adb -s $Emulator exec-out uiautomator dump /dev/tty 2>$null | Out-String)
        if (-not [string]::IsNullOrWhiteSpace($directDump)) {
            $xmlMatch = [regex]::Match($directDump, '(?s)<\?xml.*</hierarchy>')
            if ($xmlMatch.Success) {
                if (Test-Path $LocalDump) {
                    try {
                        Remove-Item -LiteralPath $LocalDump -Force -ErrorAction Stop
                    } catch {
                    }
                }
                [System.IO.File]::WriteAllText($LocalDump, $xmlMatch.Value, [System.Text.Encoding]::UTF8)
                if ((Test-Path $LocalDump) -and ((Get-Item $LocalDump).Length -gt 0)) {
                    return $true
                }
            }
        }

        & $adb -s $Emulator shell rm -f $DeviceDump *>&1 | Out-Null
        & $adb -s $Emulator shell "uiautomator dump $DeviceDump >/dev/null 2>/dev/null" *>&1 | Out-Null
        $existsOutput = & $adb -s $Emulator shell "if [ -f '$DeviceDump' ]; then echo present; else echo missing; fi" 2>$null
        if ($existsOutput -match "present") {
            if (Test-Path $LocalDump) {
                try {
                    Remove-Item -LiteralPath $LocalDump -Force -ErrorAction Stop
                } catch {
                }
            }
            if ((Invoke-AdbPullQuiet -DeviceDump $DeviceDump -LocalDump $LocalDump) -and (Test-Path $LocalDump) -and ((Get-Item $LocalDump).Length -gt 0)) {
                return $true
            }
        }
        Start-Sleep -Milliseconds 750
    }

    return $false
}

function Remove-ArtifactIfExists {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return
    }

    if (Test-Path $Path) {
        try {
            Remove-Item -LiteralPath $Path -Force -ErrorAction Stop
        } catch {
        }
    }
}

function Get-UiSurface {
    param([string]$DumpFile)

    if (-not (Test-Path $DumpFile)) {
        return "missing"
    }

    $text = Get-Content $DumpFile -Raw
    if ([string]::IsNullOrWhiteSpace($text)) {
        return "empty"
    }

    if ($text.Contains('resource-id="com.senku.mobile:id/detail_title"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_screen_title"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_body"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_scroll"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_followup_panel"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_anchor_chip"')) {
        return "detail"
    }
    if ($text.Contains('resource-id="com.senku.mobile:id/results_header"')) {
        return "results"
    }
    if ($text.Contains('resource-id="com.senku.mobile:id/status_text"') -or $text.Contains('resource-id="com.senku.mobile:id/detail_status_text"')) {
        return "status"
    }
    if ($text.Contains('package="com.google.android.apps.nexuslauncher"') -or $text.Contains('package="com.android.launcher"')) {
        return "home"
    }

    return "other"
}

function Test-SenkuForeground {
    $focusOutput = & $adb -s $Emulator shell "dumpsys window windows" 2>$null
    if (-not $focusOutput) {
        return $false
    }

    $focusLine = $focusOutput | Select-String -Pattern "mCurrentFocus|mFocusedApp" | Select-Object -First 1
    if (-not $focusLine) {
        return $false
    }

    return $focusLine.ToString().Contains("com.senku.mobile")
}

function Invoke-SenkuLaunch {
    if (-not $WarmStart) {
        & $adb -s $Emulator shell am force-stop com.senku.mobile | Out-Null
        Start-Sleep -Milliseconds 250
    }

    & $adb -s $Emulator shell $activityCommand | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "am start failed for query: $Query"
    }
}

function Invoke-AdbPullQuiet {
    param(
        [string]$DeviceDump,
        [string]$LocalDump
    )

    $token = [guid]::NewGuid().ToString("N")
    $redirectStdout = Join-Path $env:TEMP ("senku_adb_pull_" + $token + "_out.log")
    $redirectStderr = Join-Path $env:TEMP ("senku_adb_pull_" + $token + "_err.log")
    try {
        $process = Start-Process `
            -FilePath $adb `
            -ArgumentList @("-s", $Emulator, "pull", $DeviceDump, $LocalDump) `
            -NoNewWindow `
            -Wait `
            -PassThru `
            -RedirectStandardOutput $redirectStdout `
            -RedirectStandardError $redirectStderr
        return ($process.ExitCode -eq 0)
    } finally {
        foreach ($path in @($redirectStdout, $redirectStderr)) {
            if (-not (Test-Path $path)) {
                continue
            }
            try {
                Remove-Item -LiteralPath $path -Force -ErrorAction Stop
            } catch {
            }
        }
    }
}

function Normalize-UiText {
    param([string]$Text)

    if ($null -eq $Text) {
        return ""
    }

    return ([string]::Join(" ", ([string]$Text).ToLowerInvariant().Split([char[]]" `r`n`t", [System.StringSplitOptions]::RemoveEmptyEntries))).Trim()
}

function Quote-AndroidShellArg {
    param([string]$Value)

    if ($null -eq $Value) {
        return "''"
    }

    return "'" + $Value.Replace("'", "'\''") + "'"
}

function Copy-ArtifactIfRequested {
    param(
        [string]$SourcePath,
        [string]$DestinationPath
    )

    if ([string]::IsNullOrWhiteSpace($DestinationPath) -or [string]::IsNullOrWhiteSpace($SourcePath)) {
        return
    }
    if (-not (Test-Path -LiteralPath $SourcePath)) {
        return
    }
    $parent = Split-Path -Parent $DestinationPath
    if ($parent) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    Copy-Item -LiteralPath $SourcePath -Destination $DestinationPath -Force
}

function Test-AppRunning {
    $appPid = & $adb -s $Emulator shell pidof com.senku.mobile 2>$null
    return -not [string]::IsNullOrWhiteSpace(($appPid | Out-String).Trim())
}

function Test-UiCompleted {
    param(
        [string]$DumpFile,
        [string]$ExpectedTitle
    )

    if (-not (Test-Path $DumpFile)) {
        return $false
    }

    $text = Get-Content $DumpFile -Raw
    if ($null -eq $text) {
        return $false
    }
    $lowerText = $text.ToLowerInvariant()
    $xml = $null
    try {
        $xml = [xml]$text
    } catch {
        $xml = $null
    }

    $busyMarkers = @(
        'retrieving offline context',
        'generating answer locally',
        'generating answer on host',
        'while senku builds the answer',
        'building answer',
        'searching for',
        'importing model',
        'installing offline pack',
        'installing pack'
    )

    $detailStatusText = ""
    $statusText = ""
    $detailBodyText = ""
    $detailTitleText = ""
    if ($null -ne $xml) {
        $detailStatusNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_status_text"]' | Select-Object -First 1
        if ($detailStatusNode) {
            $detailStatusText = Normalize-UiText -Text $detailStatusNode.Node.text
        }

        $statusNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/status_text"]' | Select-Object -First 1
        if ($statusNode) {
            $statusText = Normalize-UiText -Text $statusNode.Node.text
        }

        $detailBodyNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_body"]' | Select-Object -First 1
        if ($detailBodyNode) {
            $detailBodyText = Normalize-UiText -Text $detailBodyNode.Node.text
        }

        $detailTitleNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_title" or @resource-id="com.senku.mobile:id/detail_screen_title"]' | Select-Object -First 1
        if ($detailTitleNode) {
            $detailTitleText = Normalize-UiText -Text $detailTitleNode.Node.text
        }
    }

    $statusAggregate = (($detailStatusText + " " + $statusText).Trim()).ToLowerInvariant()
    if ($statusAggregate.Contains("offline answer failed") -or
        $statusAggregate.Contains("failed to connect") -or
        $statusAggregate.Contains("pack install failed") -or
        $statusAggregate.Contains("import failed") -or
        $statusAggregate.Contains("model load failed")) {
        return $true
    }
    $statusLooksBusy = $false
    foreach ($marker in $busyMarkers) {
        if ((-not [string]::IsNullOrWhiteSpace($statusAggregate) -and $statusAggregate.Contains($marker)) -or
            $lowerText.Contains($marker)) {
            $statusLooksBusy = $true
            break
        }
    }

    if ($lowerText.Contains('offline answer ready') -and $lowerText.Contains('resource-id="com.senku.mobile:id/detail_body"')) {
        return $true
    }

    $detailBodyLooksPreview = $detailBodyText.Contains("building answer")
    $hasSubstantialDetailBody = (-not [string]::IsNullOrWhiteSpace($detailBodyText)) -and
        ($detailBodyText.Length -ge 8) -and
        -not $detailBodyLooksPreview
    $hasDetailSurface = $text.Contains('resource-id="com.senku.mobile:id/detail_title"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_screen_title"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_body"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_scroll"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_anchor_chip"')
    $hasInlineSources = $text.Contains('resource-id="com.senku.mobile:id/detail_inline_sources_scroll"')
    $hasFollowUpInput = $text.Contains('resource-id="com.senku.mobile:id/detail_followup_input"')
    $hasFollowUpSend = $text.Contains('resource-id="com.senku.mobile:id/detail_followup_send"')
    $hasFollowUpRetry = $text.Contains('resource-id="com.senku.mobile:id/detail_followup_retry"')
    $hasCompletionControls = $hasFollowUpInput -or $hasFollowUpSend -or $hasFollowUpRetry
    $hasSourcesPanel = $text.Contains('resource-id="com.senku.mobile:id/detail_sources_panel"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_sources_container"')
    $hasProvenancePanel = $text.Contains('resource-id="com.senku.mobile:id/detail_provenance_panel"')
    $hasThreadContext = $text.Contains('resource-id="com.senku.mobile:id/detail_thread_container"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_question_bubble"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_answer_bubble"')
    $hasAnchorContext = $text.Contains('resource-id="com.senku.mobile:id/detail_anchor_chip"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_screen_meta"')
    $hasEvidenceScaffold = $hasInlineSources -or $hasSourcesPanel -or $hasProvenancePanel
    $hasActionBlocks = $lowerText.Contains('text="do first"') -or
        $lowerText.Contains('text="avoid"') -or
        $lowerText.Contains('text="escalate if"')
    if ($hasSubstantialDetailBody) {
        return $true
    }

    $hasCompletedDetailScaffold = $hasDetailSurface -and
        -not $statusLooksBusy -and
        $hasCompletionControls -and
        (($hasEvidenceScaffold -and ($hasAnchorContext -or $hasThreadContext)) -or
            ($hasInlineSources -and $hasActionBlocks))
    if ($hasCompletedDetailScaffold) {
        return $true
    }

    if (-not $statusLooksBusy) {
        $statusTextIsReady = $statusAggregate.Contains("ready") -or
            $statusAggregate.Contains("completed") -or
            $statusAggregate.Contains("complete") -or
            $statusAggregate.Contains("done")
        if ($statusTextIsReady -and -not [string]::IsNullOrWhiteSpace($statusAggregate)) {
            return $true
        }
    }

    if ($text -match 'resource-id="com\.senku\.mobile:id/status_text"') {
        if (-not [string]::IsNullOrWhiteSpace($ExpectedTitle)) {
            return $hasSubstantialDetailBody
        }
        return (-not $statusLooksBusy)
    }

    if ($text -match 'resource-id="com\.senku\.mobile:id/results_header"') {
        if (-not [string]::IsNullOrWhiteSpace($ExpectedTitle)) {
            return $false
        }
        return (-not $statusLooksBusy)
    }

    return $false
}

function Test-UiHighConfidenceCompleted {
    param(
        [string]$DumpFile,
        [string]$ExpectedTitle
    )

    if (-not (Test-Path $DumpFile)) {
        return $false
    }

    $text = Get-Content $DumpFile -Raw
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $false
    }
    $lowerText = $text.ToLowerInvariant()
    $xml = $null
    try {
        $xml = [xml]$text
    } catch {
        $xml = $null
    }

    $busyMarkers = @(
        'retrieving offline context',
        'generating answer locally',
        'generating answer on host',
        'while senku builds the answer',
        'building answer',
        'searching for',
        'importing model',
        'installing offline pack',
        'installing pack'
    )

    $terminalFailureMarkers = @(
        "offline answer failed",
        "failed to connect",
        "pack install failed",
        "import failed",
        "model load failed"
    )

    $detailBodyText = ""
    $detailStatusText = ""
    $statusText = ""
    if ($null -ne $xml) {
        $detailBodyNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_body"]' | Select-Object -First 1
        if ($detailBodyNode) {
            $detailBodyText = Normalize-UiText -Text $detailBodyNode.Node.text
        }

        $detailStatusNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_status_text"]' | Select-Object -First 1
        if ($detailStatusNode) {
            $detailStatusText = Normalize-UiText -Text $detailStatusNode.Node.text
        }

        $statusNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/status_text"]' | Select-Object -First 1
        if ($statusNode) {
            $statusText = Normalize-UiText -Text $statusNode.Node.text
        }
    }

    $statusAggregate = (($detailStatusText + " " + $statusText).Trim()).ToLowerInvariant()
    foreach ($marker in $terminalFailureMarkers) {
        if (-not [string]::IsNullOrWhiteSpace($statusAggregate) -and $statusAggregate.Contains($marker)) {
            return $true
        }
    }

    $busy = $false
    foreach ($marker in $busyMarkers) {
        if ((-not [string]::IsNullOrWhiteSpace($statusAggregate) -and $statusAggregate.Contains($marker)) -or
            $lowerText.Contains($marker)) {
            $busy = $true
            break
        }
    }

    $hasSubstantialBody = (-not [string]::IsNullOrWhiteSpace($detailBodyText)) -and
        ($detailBodyText.Length -ge 8) -and
        (-not $detailBodyText.Contains("building answer"))
    $hasDetailSurface = $text.Contains('resource-id="com.senku.mobile:id/detail_title"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_screen_title"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_body"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_scroll"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_anchor_chip"')
    $hasStatusSurface = $text.Contains('resource-id="com.senku.mobile:id/detail_status_text"') -or
        $text.Contains('resource-id="com.senku.mobile:id/status_text"')
    $hasInlineSources = $text.Contains('resource-id="com.senku.mobile:id/detail_inline_sources_scroll"')
    $hasFollowUpInput = $text.Contains('resource-id="com.senku.mobile:id/detail_followup_input"')
    $hasFollowUpSend = $text.Contains('resource-id="com.senku.mobile:id/detail_followup_send"')
    $hasFollowUpRetry = $text.Contains('resource-id="com.senku.mobile:id/detail_followup_retry"')
    $hasCompletionControls = $hasFollowUpInput -or $hasFollowUpSend -or $hasFollowUpRetry
    $hasSourcesPanel = $text.Contains('resource-id="com.senku.mobile:id/detail_sources_panel"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_sources_container"')
    $hasProvenancePanel = $text.Contains('resource-id="com.senku.mobile:id/detail_provenance_panel"')
    $hasThreadContext = $text.Contains('resource-id="com.senku.mobile:id/detail_thread_container"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_question_bubble"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_answer_bubble"')
    $hasAnchorContext = $text.Contains('resource-id="com.senku.mobile:id/detail_anchor_chip"') -or
        $text.Contains('resource-id="com.senku.mobile:id/detail_screen_meta"')
    $hasEvidenceScaffold = $hasInlineSources -or $hasSourcesPanel -or $hasProvenancePanel
    $hasActionBlocks = $lowerText.Contains('text="do first"') -or
        $lowerText.Contains('text="avoid"') -or
        $lowerText.Contains('text="escalate if"')
    $statusTextIsReady = -not [string]::IsNullOrWhiteSpace($statusAggregate) -and (
        $statusAggregate.Contains("ready") -or
        $statusAggregate.Contains("completed") -or
        $statusAggregate.Contains("complete") -or
        $statusAggregate.Contains("done")
    )

    if ($hasDetailSurface -and $hasSubstantialBody) {
        return $true
    }

    if ($hasDetailSurface -and -not $busy -and $hasCompletionControls -and (
            ($hasEvidenceScaffold -and ($hasAnchorContext -or $hasThreadContext) -and ($statusTextIsReady -or $hasActionBlocks)) -or
            ($hasInlineSources -and $hasActionBlocks)
        )) {
        return $true
    }

    if (-not $busy -and
        $hasStatusSurface -and
        -not [string]::IsNullOrWhiteSpace($statusAggregate) -and
        ($hasDetailSurface -or [string]::IsNullOrWhiteSpace($ExpectedTitle))) {
        return $true
    }

    if (-not [string]::IsNullOrWhiteSpace($ExpectedTitle)) {
        if ($hasDetailSurface -and -not [string]::IsNullOrWhiteSpace($detailBodyText)) {
            return $true
        }
    }

    if (-not $busy -and
        [string]::IsNullOrWhiteSpace($ExpectedTitle) -and
        $text.Contains('resource-id="com.senku.mobile:id/results_header"')) {
        return $true
    }

    return $false
}

function Test-UiTerminalFailure {
    param([string]$DumpFile)

    if (-not (Test-Path $DumpFile)) {
        return $false
    }

    $text = (Get-Content $DumpFile -Raw).ToLowerInvariant()
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $false
    }
    return $text.Contains("offline answer failed")
        -or $text.Contains("failed to connect")
        -or $text.Contains("pack install failed")
        -or $text.Contains("import failed")
        -or $text.Contains("model load failed")
}

function Resolve-HostInferenceUrlForDevice {
    param([string]$Url)

    if ($InferenceMode -ne "host" -or [string]::IsNullOrWhiteSpace($Url)) {
        return $Url
    }
    if ($Emulator -like "emulator-*") {
        return $Url
    }

    try {
        $resolved = Resolve-AndroidHostInferenceUrlForDevice -AdbPath $adb -DeviceName $Emulator -Url $Url
        if ($resolved -ne $Url) {
            Write-Host ("Physical device {0}: using adb reverse for host inference {1} -> {2}" -f $Emulator, $Url, $resolved)
        }
        return $resolved
    } catch {
        Write-Warning ("Could not prepare physical-device host inference URL '{0}': {1}" -f $Url, $_.Exception.Message)
        return $Url
    }
}

function Capture-LogcatSnapshot {
    param([string]$DestinationPath)

    $logcatArgs = @("-s", $Emulator, "logcat", "-d", "-v", "time")
    if (-not [string]::IsNullOrWhiteSpace($LogcatSpec)) {
        $logcatArgs += ($LogcatSpec -split "\s+")
    }
    $result = Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $logcatArgs -TimeoutMilliseconds 30000
    if ($result.timed_out) {
        throw "Logcat capture timed out after 30000 ms"
    }
    $result.output | Set-Content -LiteralPath $DestinationPath -Encoding UTF8
}

$EffectiveHostInferenceUrl = Resolve-HostInferenceUrlForDevice -Url $HostInferenceUrl

if ($resolvedInstrumentationExecution) {
    if (-not (Test-Path -LiteralPath $instrumentedSmokeScript)) {
        throw "instrumented smoke script not found at $instrumentedSmokeScript"
    }
    $repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
    $appApk = Join-Path $repoRoot "android-app\\app\\build\\outputs\\apk\\debug\\app-debug.apk"
    $testApk = Join-Path $repoRoot "android-app\\app\\build\\outputs\\apk\\androidTest\\debug\\app-debug-androidTest.apk"
    $canSkipBuild = (Test-Path -LiteralPath $appApk) -and (Test-Path -LiteralPath $testApk)
    $captureLabel = if ($Ask) { "prompt_detail" } else { "prompt_results" }
    $summaryPath = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_instrumented_prompt_" + [guid]::NewGuid().ToString("N") + ".json")
    Remove-ArtifactIfExists -Path $summaryPath
    if ($DumpPath) {
        Remove-ArtifactIfExists -Path $DumpPath
    }
    if ($LogcatPath) {
        Remove-ArtifactIfExists -Path $LogcatPath
    }

    try {
        $instrumentationArgs = @(
            "-ExecutionPolicy", "Bypass",
            "-File", $instrumentedSmokeScript,
            "-Device", $Emulator,
            "-SmokeProfile", "custom",
            "-ScriptedQuery", $Query,
            "-ScriptedExpectedSurface", $(if ($Ask) { "detail" } else { "results" }),
            "-ScriptedCaptureLabel", $captureLabel,
            "-ScriptedTimeoutMs", ([Math]::Max(5000, $MaxWaitSeconds * 1000)).ToString(),
            "-SummaryPath", $summaryPath
        )
        if ($canSkipBuild) {
            $instrumentationArgs += @("-SkipBuild")
        }
        if ($Ask) {
            $instrumentationArgs += @("-ScriptedAsk")
            if (-not [string]::IsNullOrWhiteSpace($ExpectedDetailTitle)) {
                $instrumentationArgs += @("-ScriptedExpectedTitle", $ExpectedDetailTitle)
            }
        }
        if (-not [string]::IsNullOrWhiteSpace($FollowUpQuery)) {
            $instrumentationArgs += @("-ScriptedFollowUpQuery", $FollowUpQuery)
        }
        if ($InferenceMode -eq "host" -and -not [string]::IsNullOrWhiteSpace($EffectiveHostInferenceUrl)) {
            $instrumentationArgs += @(
                "-EnableHostInferenceSmoke",
                "-HostInferenceUrl", $EffectiveHostInferenceUrl,
                "-HostInferenceModel", $HostInferenceModel
            )
        }
        if ($LogcatPath) {
            $instrumentationArgs += @("-CaptureLogcat")
        }
        if ($ClearLogcatBeforeLaunch) {
            $instrumentationArgs += @("-ClearLogcatBeforeRun")
        }

        & powershell @instrumentationArgs
        if ($LASTEXITCODE -ne 0) {
            throw "Instrumentation execution failed"
        }

        if (-not (Test-Path -LiteralPath $summaryPath)) {
            throw "Instrumentation execution did not produce a summary file"
        }

        $summary = Get-Content -LiteralPath $summaryPath -Raw | ConvertFrom-Json
        $artifactDir = [string]$summary.artifact_dir
        $dumpFile = $null
        if ($summary.dumps -and $summary.dumps.Count -gt 0) {
            $dumpFile = Join-Path (Join-Path $artifactDir "dumps") ([string]$summary.dumps[0])
        }
        Copy-ArtifactIfRequested -SourcePath $dumpFile -DestinationPath $DumpPath
        Copy-ArtifactIfRequested -SourcePath ([string]$summary.logcat_path) -DestinationPath $LogcatPath

        if ($DumpPath -and (Test-Path -LiteralPath $DumpPath)) {
            Write-Host "UI dump saved to $DumpPath"
        }
        if ($LogcatPath -and (Test-Path -LiteralPath $LogcatPath)) {
            Write-Host "Logcat saved to $LogcatPath"
        }
        exit 0
    } finally {
        Remove-ArtifactIfExists -Path $summaryPath
    }
}

$encodedQuery = [System.Uri]::EscapeDataString($Query)
$commandParts = @(
    "am", "start",
    "-W",
    "--activity-clear-top",
    "--activity-single-top",
    "-n", "com.senku.mobile/.MainActivity",
    "--es", "auto_query", (Quote-AndroidShellArg $encodedQuery)
)
switch ($InferenceMode) {
    "host" {
        $commandParts += @("--ez", "host_inference_enabled", "true")
        if (-not [string]::IsNullOrWhiteSpace($EffectiveHostInferenceUrl)) {
            $commandParts += @("--es", "host_inference_url", (Quote-AndroidShellArg $EffectiveHostInferenceUrl))
        }
        if (-not [string]::IsNullOrWhiteSpace($HostInferenceModel)) {
            $commandParts += @("--es", "host_inference_model", (Quote-AndroidShellArg $HostInferenceModel))
        }
    }
    "local" {
        $commandParts += @("--ez", "host_inference_enabled", "false")
    }
}
if ($Ask) {
    $commandParts += @("--ez", "auto_ask", "true")
    if (-not [string]::IsNullOrWhiteSpace($FollowUpQuery)) {
        $commandParts += @("--es", "auto_followup_query", (Quote-AndroidShellArg ([System.Uri]::EscapeDataString($FollowUpQuery))))
    }
    if ([string]::IsNullOrWhiteSpace($ExpectedDetailTitle)) {
        $ExpectedDetailTitle = $Query
    }
}
$activityCommand = $commandParts -join " "

if ($ClearLogcatBeforeLaunch) {
    & $adb -s $Emulator logcat -c | Out-Null
}
if (-not $SkipDeviceLock) {
    $deviceLock = Acquire-DeviceLock -DeviceName $Emulator
}
Remove-ArtifactIfExists -Path $DumpPath
if ($LogcatPath) {
    Remove-ArtifactIfExists -Path $LogcatPath
}
Invoke-SenkuLaunch

if ($WaitSeconds -gt 0) {
    Start-Sleep -Seconds $WaitSeconds
}

$completionDump = $null
$completedDumpReusable = $false

if ($WaitForCompletion) {
    $completionDump = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_wait_" + $Emulator.Replace("-", "_") + "_" + [guid]::NewGuid().ToString("N") + ".xml")
    $deviceDump = "/sdcard/senku_wait.xml"
    $elapsed = 0
    $uiMissCount = 0
    $homeSurfaceCount = 0
    $appMissingCount = 0
    $launchRetryCount = 0
    $maxLaunchRetries = 2
    $maxUiMissesBeforeRelaunch = 3
    $minHomeSurfaceRelaunches = 2
    $completionSurfaces = @("detail", "results", "status")
    $lastSurface = "missing"
    $completionSignalCount = 0
    $completed = $false
    while ($elapsed -lt $MaxWaitSeconds) {
        if (Save-UiDump -DeviceDump $deviceDump -LocalDump $completionDump) {
            $uiMissCount = 0
            $lastSurface = Get-UiSurface -DumpFile $completionDump

            $isCompleted = Test-UiCompleted -DumpFile $completionDump -ExpectedTitle $ExpectedDetailTitle
            $isHighConfidenceCompleted = Test-UiHighConfidenceCompleted -DumpFile $completionDump -ExpectedTitle $ExpectedDetailTitle
            if ($isCompleted) {
                if (Test-UiTerminalFailure -DumpFile $completionDump) {
                    $completed = $true
                    break
                }
                if ($isHighConfidenceCompleted) {
                    $completed = $true
                    break
                }
                if ($lastSurface -eq "detail") {
                    $completionSignalCount += 1
                    if ($completionSignalCount -ge 2) {
                        $completed = $true
                        break
                    }
                } else {
                    $completed = $true
                    break
                }
            } else {
                $completionSignalCount = 0
            }

            if ($completionSurfaces -contains $lastSurface) {
                $homeSurfaceCount = 0
            } else {
                if ($lastSurface -eq "home") {
                    $homeSurfaceCount += 1
                    if ($homeSurfaceCount -ge $minHomeSurfaceRelaunches -and $launchRetryCount -lt $maxLaunchRetries) {
                        Write-Warning "Auto-ask launch stayed on launcher surface for '$Emulator'; relaunching."
                        Invoke-SenkuLaunch
                        $launchRetryCount += 1
                        $homeSurfaceCount = 0
                        Start-Sleep -Milliseconds 600
                        continue
                    }
                } else {
                    $homeSurfaceCount = 0
                }
            }
        } else {
            $uiMissCount += 1
            $completionSignalCount = 0
            $homeSurfaceCount = 0

            if ($uiMissCount -ge $maxUiMissesBeforeRelaunch -and $launchRetryCount -lt $maxLaunchRetries) {
                Write-Warning "Unable to capture UI for $Emulator for $uiMissCount polls; relaunching."
                Invoke-SenkuLaunch
                $launchRetryCount += 1
                $uiMissCount = 0
                Start-Sleep -Milliseconds 600
                continue
            }
        }
        if ($elapsed -ge $PollSeconds) {
            if (Test-AppRunning) {
                $appMissingCount = 0
            } else {
                $appMissingCount += 1
                if ($appMissingCount -ge 3) {
                    Write-Warning "com.senku.mobile is no longer running on $Emulator; stopping wait loop."
                    break
                }
            }
        }
        Start-Sleep -Seconds $PollSeconds
        $elapsed += $PollSeconds
    }
    if (-not $completed) {
        throw "Senku did not reach a completed prompt UI before timeout (surface=$lastSurface)."
    }
    $completedDumpReusable = Test-Path $completionDump
}

function Ensure-SenkuForeground {
    param(
        [int]$MaxAttempts = 3
    )

    for ($attempt = 0; $attempt -lt $MaxAttempts; $attempt++) {
        if (Test-SenkuForeground) {
            return $true
        }
        $surface = $null
        $probeDump = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_foreground_" + $Emulator.Replace("-", "_") + "_" + [guid]::NewGuid().ToString("N") + ".xml")
        try {
            if (Save-UiDump -DeviceDump "/sdcard/senku_wait.xml" -LocalDump $probeDump) {
                $surface = Get-UiSurface -DumpFile $probeDump
            }
        } finally {
            Remove-ArtifactIfExists -Path $probeDump
        }
        if ($surface -eq "detail" -or $surface -eq "status") {
            return $true
        }
        Invoke-SenkuLaunch
        Start-Sleep -Milliseconds 900
    }

    return $false
}

if ($DumpPath) {
    $dumpParent = Split-Path -Parent $DumpPath
    if ($dumpParent) {
        New-Item -ItemType Directory -Force -Path $dumpParent | Out-Null
    }

    if ($completedDumpReusable) {
        Copy-Item -LiteralPath $completionDump -Destination $DumpPath -Force
        Write-Host "UI dump saved to $DumpPath (reused completed wait-loop capture)"
    } else {
        [void](Ensure-SenkuForeground)
        $deviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($DumpPath)
        if (Save-UiDump -DeviceDump $deviceDump -LocalDump $DumpPath) {
            Write-Host "UI dump saved to $DumpPath"
        } else {
            Write-Warning "UI dump capture failed for $DumpPath"
        }
    }
}

if ($LogcatPath) {
    $logcatParent = Split-Path -Parent $LogcatPath
    if ($logcatParent) {
        New-Item -ItemType Directory -Force -Path $logcatParent | Out-Null
    }
    Capture-LogcatSnapshot -DestinationPath $LogcatPath
    Write-Host "Logcat saved to $LogcatPath"
}

Remove-ArtifactIfExists -Path $completionDump

    exit 0
} finally {
    if ($deviceLock) {
        $deviceLock.Dispose()
    }
}
