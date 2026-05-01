param(
    [string]$Emulator = "emulator-5554",
    [Parameter(Mandatory = $true)]
    [string]$InitialQuery,
    [Parameter(Mandatory = $true)]
    [string]$FollowUpQuery,
    [string]$PushPackDir,
    [string]$PushPackSummaryPath = "",
    [switch]$SkipPackPushIfCurrent,
    [switch]$ForcePackPush,
    [switch]$WarmStart,
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "preserve",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string]$OutputDir = "artifacts\\bench\\android_detail_followups",
    [string]$RunLabel,
    [ValidateSet("auto", "in_detail_ui", "auto_intent", "send_button", "ime_send", "ime_done", "hardware_enter")]
    [string]$FollowUpSubmissionMode = "auto",
    [switch]$SkipSourceProbe,
    [switch]$RequireStrictFollowUpProof,
    [int]$InitialMaxWaitSeconds = 260,
    [int]$FollowUpMaxWaitSeconds = 180,
    [int]$AdbPullTimeoutMilliseconds = 30000,
    [int]$PollSeconds = 5
)

$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
if (-not (Test-Path -LiteralPath $commonHarnessModule)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}
Import-Module $commonHarnessModule -Force -DisableNameChecking
if ($AdbPullTimeoutMilliseconds -le 0) {
    throw "-AdbPullTimeoutMilliseconds must be a positive integer."
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
        $uri = [System.Uri]$Url
        if ($uri.Host -ne "10.0.2.2") {
            return $Url
        }
        $port = if ($uri.Port -gt 0) { $uri.Port } else { 80 }
        & $adb -s $Emulator reverse ("tcp:{0}" -f $port) ("tcp:{0}" -f $port) | Out-Null
        $builder = [System.UriBuilder]::new($uri)
        $builder.Host = "127.0.0.1"
        Write-Host ("Physical device {0}: using adb reverse for host inference {1} -> {2}" -f $Emulator, $Url, $builder.Uri.AbsoluteUri)
        return $builder.Uri.AbsoluteUri
    } catch {
        Write-Warning ("Could not prepare physical-device host inference URL '{0}': {1}" -f $Url, $_.Exception.Message)
        return $Url
    }
}

$EffectiveHostInferenceUrl = Resolve-HostInferenceUrlForDevice -Url $HostInferenceUrl

$pushPackScript = Join-Path $PSScriptRoot "push_mobile_pack_to_android.ps1"
if (-not [string]::IsNullOrWhiteSpace($PushPackDir)) {
    if (-not (Test-Path $pushPackScript)) {
        throw "push script not found at $pushPackScript"
    }
    $pushArgs = @(
        "-Device", $Emulator,
        "-PackDir", $PushPackDir,
        "-ForceStop"
    )
    if ($SkipPackPushIfCurrent) {
        $pushArgs += "-SkipIfCurrent"
    }
    if ($ForcePackPush) {
        $pushArgs += "-ForcePush"
    }
    if (-not [string]::IsNullOrWhiteSpace($PushPackSummaryPath)) {
        $pushArgs += @("-SummaryPath", $PushPackSummaryPath)
    }
    & $pushPackScript @pushArgs
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

function New-Slug {
    param([string]$Text)

    $slug = (Safe-Text -Text $Text).ToLowerInvariant() -replace "[^a-z0-9]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "detail_followup"
    }
    return $slug
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

function Capture-Screenshot {
    param([string]$LocalPath)

    if ([string]::IsNullOrWhiteSpace($LocalPath)) {
        return $false
    }

    $parent = Split-Path -Parent $LocalPath
    if ($parent) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }

    $remotePath = "/sdcard/" + [System.IO.Path]::GetFileName($LocalPath)
    & $adb -s $Emulator shell rm -f $remotePath *>&1 | Out-Null
    & $adb -s $Emulator shell screencap -p $remotePath *>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        return $false
    }
    $pulled = (Invoke-AdbPullQuiet -DeviceDump $remotePath -LocalDump $LocalPath) `
        -and (Test-Path -LiteralPath $LocalPath) `
        -and ((Get-Item -LiteralPath $LocalPath).Length -gt 0)
    & $adb -s $Emulator shell rm -f $remotePath *>&1 | Out-Null
    return $pulled
}

function Invoke-AdbPullQuiet {
    param(
        [string]$DeviceDump,
        [string]$LocalDump,
        [int]$TimeoutMilliseconds = $AdbPullTimeoutMilliseconds
    )

    if ($TimeoutMilliseconds -le 0) {
        throw "-TimeoutMilliseconds must be a positive integer."
    }
    $result = Invoke-AndroidAdbCommandCapture `
        -AdbPath $adb `
        -Arguments @("-s", $Emulator, "pull", $DeviceDump, $LocalDump) `
        -TimeoutMilliseconds $TimeoutMilliseconds
    if ($result.timed_out) {
        return $false
    }
    return ($result.exit_code -eq 0)
}

function Copy-DumpSnapshot {
    param(
        [string]$SourceDump,
        [string]$DestinationDump
    )

    if ([string]::IsNullOrWhiteSpace($SourceDump) -or [string]::IsNullOrWhiteSpace($DestinationDump)) {
        return
    }

    if (-not (Test-Path $SourceDump)) {
        return
    }

    $destinationParent = Split-Path -Parent $DestinationDump
    if ($destinationParent) {
        New-Item -ItemType Directory -Force -Path $destinationParent | Out-Null
    }

    Copy-Item -LiteralPath $SourceDump -Destination $DestinationDump -Force
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
    if ($null -eq $xml -or $null -eq $xml.DocumentElement) {
        return $null
    }

    $detailHeaderLabelNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_header_label"]' | Select-Object -First 1
    $detailBodyLabelNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_body_label"]' | Select-Object -First 1
    $detailTitleNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_title"]' | Select-Object -First 1
    $detailSubtitleNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_subtitle"]' | Select-Object -First 1
    $detailBodyNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_body"]' | Select-Object -First 1
    $detailStatusNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_status_text"]' | Select-Object -First 1
    $mainStatusNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/status_text"]' | Select-Object -First 1
    $detailScrollNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_scroll"]' | Select-Object -First 1
    $detailInputNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_followup_input"]' | Select-Object -First 1
    $detailSendNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_followup_send"]' | Select-Object -First 1
    $detailSourcesNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_sources_panel"]' | Select-Object -First 1
    $detailSessionNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_session_panel"]' | Select-Object -First 1
    $detailSessionTextNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_session_text"]' | Select-Object -First 1
    $provenanceOpenNode = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_provenance_open"]' | Select-Object -First 1
    $sourceButtonNodes = Select-Xml -Xml $xml -XPath '//node[@resource-id="com.senku.mobile:id/detail_sources_container"]//node[@class="android.widget.Button"]'
    $sourceButtons = @()
    $sourceButtonDetails = @()
    foreach ($buttonNode in $sourceButtonNodes) {
        $text = [string]$buttonNode.Node.text
        if (-not [string]::IsNullOrWhiteSpace($text)) {
            $sourceButtons += $text
            $sourceButtonDetails += [pscustomobject]@{
                text = $text
                bounds = [string]$buttonNode.Node.bounds
            }
        }
    }

    return [pscustomobject]@{
        detail_header_label = if ($detailHeaderLabelNode) { $detailHeaderLabelNode.Node.text } else { $null }
        detail_body_label = if ($detailBodyLabelNode) { $detailBodyLabelNode.Node.text } else { $null }
        detail_title = if ($detailTitleNode) { $detailTitleNode.Node.text } else { $null }
        detail_subtitle = if ($detailSubtitleNode) { $detailSubtitleNode.Node.text } else { $null }
        detail_body = if ($detailBodyNode) { $detailBodyNode.Node.text } else { $null }
        detail_status = if ($detailStatusNode) { $detailStatusNode.Node.text } else { $null }
        main_status = if ($mainStatusNode) { $mainStatusNode.Node.text } else { $null }
        detail_scroll_bounds = if ($detailScrollNode) { $detailScrollNode.Node.bounds } else { $null }
        followup_bounds = if ($detailInputNode) { $detailInputNode.Node.bounds } else { $null }
        followup_send_bounds = if ($detailSendNode) { $detailSendNode.Node.bounds } else { $null }
        followup_input_text = if ($detailInputNode) { $detailInputNode.Node.text } else { $null }
        followup_available = [bool]($detailInputNode -and $detailSendNode)
        followup_input_enabled = if ($detailInputNode) { [string]$detailInputNode.Node.enabled } else { $null }
        followup_send_enabled = if ($detailSendNode) { [string]$detailSendNode.Node.enabled } else { $null }
        session_text = if ($detailSessionTextNode) { $detailSessionTextNode.Node.text } else { $null }
        sources_visible = ($sourceButtons.Count -gt 0)
        source_buttons = $sourceButtons
        source_button_details = $sourceButtonDetails
        session_visible = [bool]$detailSessionNode
        provenance_open_bounds = if ($provenanceOpenNode) { $provenanceOpenNode.Node.bounds } else { $null }
        provenance_open_text = if ($provenanceOpenNode) { $provenanceOpenNode.Node.text } else { $null }
    }
}

function Test-IsSenkuDetailSummary {
    param($Summary)

    if (-not $Summary) {
        return $false
    }

    if (-not [string]::IsNullOrWhiteSpace($Summary.detail_title)) {
        return $true
    }

    if (-not [string]::IsNullOrWhiteSpace($Summary.detail_body)) {
        return $true
    }

    if (-not [string]::IsNullOrWhiteSpace($Summary.detail_status)) {
        return $true
    }

    return $Summary.followup_available
}

function Test-MainScreenBusy {
    param($Summary)

    if (-not $Summary) {
        return $false
    }

    $statusText = Normalize-UiText -Text $Summary.main_status
    if ([string]::IsNullOrWhiteSpace($statusText)) {
        return $false
    }

    $busyMarkers = @(
        'retrieving offline context',
        'retrieving offline context with chat memory',
        'generating answer locally',
        'generating answer on host gpu',
        'generating answer on host',
        'searching for',
        'importing model',
        'installing offline pack',
        'installing pack',
        'reinstalling pack'
    )
    foreach ($marker in $busyMarkers) {
        if ($statusText.Contains($marker)) {
            return $true
        }
    }

    return $false
}

function Test-IsGuideModeSummary {
    param($Summary)

    if (-not $Summary) {
        return $false
    }

    $headerLabel = Normalize-UiText -Text $Summary.detail_header_label
    $bodyLabel = Normalize-UiText -Text $Summary.detail_body_label
    return ($headerLabel -eq "guide note" -or $bodyLabel -eq "excerpt")
}

function Test-IsAnswerModeSummary {
    param($Summary)

    if (-not $Summary) {
        return $false
    }

    if (Test-IsGuideModeSummary -Summary $Summary) {
        return $false
    }

    $headerLabel = Normalize-UiText -Text $Summary.detail_header_label
    $bodyLabel = Normalize-UiText -Text $Summary.detail_body_label
    $subtitle = Normalize-UiText -Text $Summary.detail_subtitle
    $statusText = Normalize-UiText -Text $Summary.detail_status

    if ($headerLabel -eq "you asked" -or $bodyLabel -eq "senku answered") {
        return $true
    }

    if (-not [string]::IsNullOrWhiteSpace($subtitle) -and (
        $subtitle.StartsWith("host answer") -or
        $subtitle.StartsWith("offline answer") -or
        $subtitle.StartsWith("deterministic offline answer")
    )) {
        return $true
    }

    if (-not [string]::IsNullOrWhiteSpace($statusText) -and (
        $statusText.StartsWith("offline answer ready in") -or
        $statusText.StartsWith("deterministic offline answer ready") -or
        $statusText.Contains("retrieving offline context") -or
        $statusText.Contains("generating answer locally")
    )) {
        return $true
    }

    return $false
}

function Decode-UiText {
    param([string]$Text)

    $safeText = Safe-Text -Text $Text
    if ([string]::IsNullOrWhiteSpace($safeText)) {
        return ""
    }

    if ($safeText -match '%[0-9A-Fa-f]{2}') {
        try {
            return [System.Uri]::UnescapeDataString($safeText)
        } catch {
            return $safeText
        }
    }

    return $safeText
}

function Normalize-UiText {
    param([string]$Text)

    $parts = @((Decode-UiText -Text $Text).ToLowerInvariant().Split([char[]]" `r`n`t", [System.StringSplitOptions]::RemoveEmptyEntries))
    return ([string]::Join(" ", $parts)).Trim()
}

function Normalize-UiInputText {
    param([string]$Text)

    $decoded = (Decode-UiText -Text $Text).ToLowerInvariant()
    $parts = @($decoded -split '[^a-z0-9]+' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    return ([string]::Join(" ", $parts)).Trim()
}

function Quote-AndroidShellArg {
    param([string]$Value)

    if ($null -eq $Value) {
        return "''"
    }

    return "'" + $Value.Replace("'", "'\''") + "'"
}

function Get-BoundsRect {
    param([string]$Bounds)

    if ([string]::IsNullOrWhiteSpace($Bounds)) {
        return $null
    }

    $match = [regex]::Match($Bounds, '\[(\d+),(\d+)\]\[(\d+),(\d+)\]')
    if (-not $match.Success) {
        return $null
    }

    $left = [int]$match.Groups[1].Value
    $top = [int]$match.Groups[2].Value
    $right = [int]$match.Groups[3].Value
    $bottom = [int]$match.Groups[4].Value
    return [pscustomobject]@{
        left = $left
        top = $top
        right = $right
        bottom = $bottom
        x = [int](($left + $right) / 2)
        y = [int](($top + $bottom) / 2)
    }
}

function Get-BoundsCenter {
    param([string]$Bounds)

    $rect = Get-BoundsRect -Bounds $Bounds
    if (-not $rect) {
        return $null
    }

    return [pscustomobject]@{
        x = $rect.x
        y = $rect.y
    }
}

function Test-IsAnswerSummary {
    param($Summary)

    if (-not $Summary) {
        return $false
    }

    if (Test-IsGuideModeSummary -Summary $Summary) {
        return $false
    }

    $headerLabel = Normalize-UiText -Text $Summary.detail_header_label
    $bodyLabel = Normalize-UiText -Text $Summary.detail_body_label
    $subtitle = Normalize-UiText -Text $Summary.detail_subtitle
    if ($headerLabel -eq "you asked" -or $bodyLabel -eq "senku answered") {
        return $true
    }

    $answerSubtitleMarkers = @(
        "host answer |",
        "offline answer |",
        "deterministic offline answer ready",
        "offline answer ready in"
    )
    foreach ($marker in $answerSubtitleMarkers) {
        if (-not [string]::IsNullOrWhiteSpace($subtitle) -and $subtitle.StartsWith($marker)) {
            return $true
        }
    }

    return $false
}

function Tap-Bounds {
    param([string]$Bounds)

    $center = Get-BoundsCenter -Bounds $Bounds
    if (-not $center) {
        return $false
    }

    & $adb -s $Emulator shell input tap $center.x $center.y | Out-Null
    return $true
}

function Swipe-Bounds {
    param(
        [string]$Bounds,
        [double]$StartRatio = 0.82,
        [double]$EndRatio = 0.28,
        [int]$DurationMs = 250
    )

    $rect = Get-BoundsRect -Bounds $Bounds
    if (-not $rect) {
        return $false
    }

    $height = [math]::Max(1, $rect.bottom - $rect.top)
    $x = $rect.x
    $startY = [int]($rect.top + ($height * $StartRatio))
    $endY = [int]($rect.top + ($height * $EndRatio))
    if ($startY -le $endY) {
        $startY = $rect.bottom - 16
        $endY = $rect.top + 16
    }

    & $adb -s $Emulator shell input swipe $x $startY $x $endY $DurationMs | Out-Null
    return $true
}

function ConvertTo-AdbInputText {
    param([string]$Text)

    $trimmed = (Safe-Text $Text).Trim()
    if ($trimmed.Length -eq 0) {
        return ""
    }

    $converted = $trimmed.Replace("%", "\%")
    $converted = $converted.Replace(" ", "%s")
    $converted = $converted.Replace("&", "\&")
    $converted = $converted.Replace("|", "\|")
    $converted = $converted.Replace("<", "\<")
    $converted = $converted.Replace(">", "\>")
    $converted = $converted.Replace("(", "\(")
    $converted = $converted.Replace(")", "\)")
    $converted = $converted.Replace("[", "\[")
    $converted = $converted.Replace("]", "\]")
    $converted = $converted.Replace("{", "\{")
    $converted = $converted.Replace("}", "\}")
    $converted = $converted.Replace("$", "\$")
    $converted = $converted.Replace(";", "\;")
    return $converted
}

function Test-DetailCompletion {
    param(
        $Summary,
        [string]$ExpectedTitle,
        [string]$PreviousBody
    )

    if (-not $Summary -or [string]::IsNullOrWhiteSpace($Summary.detail_body)) {
        return $false
    }

    $statusText = Normalize-UiText -Text $Summary.detail_status
    $busyMarkers = @(
        'retrieving offline context',
        'retrieving offline context with chat memory',
        'generating answer locally',
        'searching for',
        'importing model',
        'installing offline pack',
        'installing pack',
        'reinstalling pack',
        'offline answer failed'
    )
    foreach ($marker in $busyMarkers) {
        if (-not [string]::IsNullOrWhiteSpace($statusText) -and $statusText.Contains($marker)) {
            return $false
        }
    }

    $terminalStatuses = @(
        "offline answer ready in",
        "deterministic offline answer ready"
    )
    if (-not [string]::IsNullOrWhiteSpace($statusText) -and -not ($terminalStatuses | Where-Object { $statusText.StartsWith($_) })) {
        return $false
    }

    # Some completed answers land with the updated title/body visible while the inline
    # composer has been scrolled off-screen in the current dump. Treat completion as an
    # answer-state check, not a viewport/composer-visibility check.
    if (-not (Test-IsAnswerSummary -Summary $Summary)) {
        return $false
    }

    $expected = Normalize-UiText -Text $ExpectedTitle
    if ([string]::IsNullOrWhiteSpace($expected)) {
        return $true
    }

    if ((Normalize-UiText -Text $Summary.detail_title) -eq $expected) {
        return $true
    }

    $sessionText = Normalize-UiText -Text $Summary.session_text
    if (-not [string]::IsNullOrWhiteSpace($sessionText) -and $sessionText.Contains("you: $expected")) {
        return $true
    }

    if (-not (Test-IsAnswerModeSummary -Summary $Summary)) {
        return $false
    }

    $previous = Normalize-UiText -Text $PreviousBody
    $currentBody = Normalize-UiText -Text $Summary.detail_body
    if (-not [string]::IsNullOrWhiteSpace($previous) -and -not [string]::IsNullOrWhiteSpace($currentBody) -and $currentBody -ne $previous) {
        return $true
    }

    return $false
}

function Test-DetailTerminalStateCandidate {
    param($Summary)

    if (-not $Summary -or [string]::IsNullOrWhiteSpace($Summary.detail_body)) {
        return $false
    }

    $statusText = Normalize-UiText -Text $Summary.detail_status
    $busyMarkers = @(
        'retrieving offline context',
        'retrieving offline context with chat memory',
        'generating answer locally',
        'searching for',
        'importing model',
        'installing offline pack',
        'installing pack',
        'reinstalling pack',
        'offline answer failed'
    )
    foreach ($marker in $busyMarkers) {
        if (-not [string]::IsNullOrWhiteSpace($statusText) -and $statusText.Contains($marker)) {
            return $false
        }
    }

    $terminalStatuses = @(
        "offline answer ready in",
        "deterministic offline answer ready"
    )
    if (-not [string]::IsNullOrWhiteSpace($statusText) -and -not ($terminalStatuses | Where-Object { $statusText.StartsWith($_) })) {
        return $false
    }

    return (Test-IsAnswerModeSummary -Summary $Summary)
}

function Test-GuideSourceProbe {
    param(
        $Summary,
        [string]$ExpectedGuideTitle
    )

    if (-not $Summary -or [string]::IsNullOrWhiteSpace($Summary.detail_body)) {
        return $false
    }

    if ($Summary.followup_available) {
        return $false
    }

    $expected = Normalize-UiText -Text $ExpectedGuideTitle
    if ([string]::IsNullOrWhiteSpace($expected)) {
        return $true
    }

    return (Normalize-UiText -Text $Summary.detail_title) -eq $expected
}

function Test-FollowUpInputMatch {
    param(
        $Summary,
        [string]$ExpectedText
    )

    if (-not $Summary) {
        return $false
    }

    $expected = Normalize-UiInputText -Text $ExpectedText
    $actual = Normalize-UiInputText -Text $Summary.followup_input_text
    if ([string]::IsNullOrWhiteSpace($expected) -or [string]::IsNullOrWhiteSpace($actual)) {
        return $false
    }

    return ($actual -eq $expected)
}

function Clear-FollowUpInput {
    param([string]$ExistingText)

    $decoded = Decode-UiText -Text $ExistingText
    if ([string]::IsNullOrWhiteSpace($decoded)) {
        return
    }

    & $adb -s $Emulator shell input keyevent 123 | Out-Null
    Start-Sleep -Milliseconds 150

    $deleteCount = [Math]::Min(160, [Math]::Max($decoded.Length + 6, 12))
    for ($index = 0; $index -lt $deleteCount; $index++) {
        & $adb -s $Emulator shell input keyevent 67 | Out-Null
    }
    Start-Sleep -Milliseconds 250
}

function Wait-ForDetailCompletion {
    param(
        [string]$ExpectedTitle,
        [string]$PreviousBody,
        [int]$MaxWaitSeconds,
        [string]$LocalDump,
        [string]$FrozenDump
    )

    $deviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($LocalDump)
    $elapsed = 0
    $bestSummary = $null
    while ($elapsed -lt $MaxWaitSeconds) {
        if (-not (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump)) {
            Start-Sleep -Seconds $PollSeconds
            $elapsed += $PollSeconds
            continue
        }
        $summary = Get-UiSummary -DumpFile $LocalDump
        if (Test-DetailCompletion -Summary $summary -ExpectedTitle $ExpectedTitle -PreviousBody $PreviousBody) {
            Copy-DumpSnapshot -SourceDump $LocalDump -DestinationDump $FrozenDump
            return $summary
        }
        if (Test-DetailTerminalStateCandidate -Summary $summary) {
            $bestSummary = $summary
            if (-not [string]::IsNullOrWhiteSpace($summary.detail_scroll_bounds)) {
                $revealedSummary = Reveal-ExpectedAnswerState `
                    -ExpectedTitle $ExpectedTitle `
                    -PreviousBody $PreviousBody `
                    -LocalDump $LocalDump `
                    -MaxScrollAttempts 4
                if (Test-DetailCompletion -Summary $revealedSummary -ExpectedTitle $ExpectedTitle -PreviousBody $PreviousBody) {
                    Copy-DumpSnapshot -SourceDump $LocalDump -DestinationDump $FrozenDump
                    return $revealedSummary
                }
                if ($revealedSummary) {
                    $bestSummary = $revealedSummary
                }
            }
        }
        Start-Sleep -Seconds $PollSeconds
        $elapsed += $PollSeconds
    }

    if (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump) {
        $finalSummary = Get-UiSummary -DumpFile $LocalDump
        if (Test-DetailCompletion -Summary $finalSummary -ExpectedTitle $ExpectedTitle -PreviousBody $PreviousBody) {
            Copy-DumpSnapshot -SourceDump $LocalDump -DestinationDump $FrozenDump
            return $finalSummary
        }
    }
    return $null
}

function Test-FollowUpAdvanceSignal {
    param(
        $Summary,
        [string]$ExpectedTitle,
        [string]$PreviousTitle,
        [string]$PreviousBody
    )

    if (-not $Summary) {
        return $false
    }

    $statusText = Normalize-UiText -Text $Summary.detail_status
    $busyMarkers = @(
        'retrieving offline context',
        'retrieving offline context with chat memory',
        'generating answer locally',
        'searching for',
        'importing model'
    )
    foreach ($marker in $busyMarkers) {
        if (-not [string]::IsNullOrWhiteSpace($statusText) -and $statusText.Contains($marker)) {
            return $true
        }
    }

    $expected = Normalize-UiText -Text $ExpectedTitle
    $currentTitle = Normalize-UiText -Text $Summary.detail_title
    $previousTitleNormalized = Normalize-UiText -Text $PreviousTitle
    if (-not [string]::IsNullOrWhiteSpace($expected) -and $currentTitle -eq $expected) {
        return $true
    }
    if ((Test-IsAnswerModeSummary -Summary $Summary) `
        -and -not [string]::IsNullOrWhiteSpace($previousTitleNormalized) `
        -and $currentTitle -ne $previousTitleNormalized) {
        return $true
    }

    $previousBodyNormalized = Normalize-UiText -Text $PreviousBody
    $currentBody = Normalize-UiText -Text $Summary.detail_body
    if ((Test-IsAnswerModeSummary -Summary $Summary) `
        -and -not [string]::IsNullOrWhiteSpace($previousBodyNormalized) `
        -and -not [string]::IsNullOrWhiteSpace($currentBody) `
        -and $currentBody -ne $previousBodyNormalized) {
        return $true
    }

    return $false
}

function Wait-ForFollowUpAdvanceSignal {
    param(
        [string]$ExpectedTitle,
        [string]$PreviousTitle,
        [string]$PreviousBody,
        [string]$LocalDump,
        [int]$MaxWaitSeconds = 12
    )

    $deviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($LocalDump)
    $elapsed = 0
    while ($elapsed -lt $MaxWaitSeconds) {
        if (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump) {
            $summary = Get-UiSummary -DumpFile $LocalDump
            if (Test-FollowUpAdvanceSignal -Summary $summary -ExpectedTitle $ExpectedTitle -PreviousTitle $PreviousTitle -PreviousBody $PreviousBody) {
                return $summary
            }
        }
        Start-Sleep -Seconds 1
        $elapsed += 1
    }

    if (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump) {
        return Get-UiSummary -DumpFile $LocalDump
    }
    return $null
}

function Reveal-SourceLinks {
    param(
        [string]$LocalDump,
        [int]$MaxScrollAttempts = 10
    )

    $deviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($LocalDump)
    $lastSummary = $null

    for ($attempt = 0; $attempt -le $MaxScrollAttempts; $attempt++) {
        if (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump) {
            $lastSummary = Get-UiSummary -DumpFile $LocalDump
            if ($lastSummary -and $lastSummary.sources_visible -and $lastSummary.source_button_details.Count -gt 0) {
                return $lastSummary
            }
        }

        if ($attempt -eq $MaxScrollAttempts) {
            break
        }

        if (-not $lastSummary -or [string]::IsNullOrWhiteSpace($lastSummary.detail_scroll_bounds)) {
            break
        }

        if (-not (Swipe-Bounds -Bounds $lastSummary.detail_scroll_bounds)) {
            break
        }
        Start-Sleep -Milliseconds 850
    }

    return $lastSummary
}

function Get-SourceButtonTitle {
    param([string]$Label)

    $lines = (Decode-UiText -Text $Label) -split "`r?`n"
    if ($lines.Count -lt 1) {
        return ""
    }

    $titleLine = $lines[0].Trim()
    $titleLine = $titleLine -replace '^(Anchor\s*\|\s*)?\d+/\d+\s+', ''
    if ($titleLine -match '^\[[^\]]+\]\s*(.+)$') {
        return $matches[1].Trim()
    }

    return $titleLine
}

function Probe-FirstSourceLink {
    param(
        $SourceSummary,
        [string]$LocalDump
    )

    $probe = [ordered]@{
        attempted = $false
        verified = $false
        source_label = $null
        expected_guide_title = $null
        dump_path = $LocalDump
        observed_title = $null
        observed_subtitle = $null
        observed_body = $null
        followup_available = $null
        error = $null
    }

    if (-not $SourceSummary -or $SourceSummary.source_button_details.Count -lt 1) {
        $probe.error = "No source buttons were visible to probe."
        return [pscustomobject]$probe
    }

    $firstSource = $SourceSummary.source_button_details[0]
    $probe.attempted = $true
    $probe.source_label = $firstSource.text
    $probe.expected_guide_title = Get-SourceButtonTitle -Label $firstSource.text

    $deviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($LocalDump)
    for ($attempt = 0; $attempt -lt 2; $attempt++) {
        if (-not (Tap-Bounds -Bounds $firstSource.bounds)) {
            $probe.error = "Unable to tap the first source button."
            return [pscustomobject]$probe
        }

        Start-Sleep -Milliseconds (1200 + ($attempt * 500))
        if (-not (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump)) {
            $probe.error = "Guide dump capture failed after tapping the first source button."
            return [pscustomobject]$probe
        }

        $guideSummary = Reveal-AnswerHeader -LocalDump $LocalDump
        if (-not $guideSummary) {
            $guideSummary = Get-UiSummary -DumpFile $LocalDump
        }
        if ($guideSummary) {
            $probe.observed_title = $guideSummary.detail_title
            $probe.observed_subtitle = $guideSummary.detail_subtitle
            $probe.observed_body = $guideSummary.detail_body
            $probe.followup_available = $guideSummary.followup_available
            $probe.verified = Test-GuideSourceProbe -Summary $guideSummary -ExpectedGuideTitle $probe.expected_guide_title
            if (-not $probe.verified `
                -and (Test-IsAnswerModeSummary -Summary $guideSummary) `
                -and -not [string]::IsNullOrWhiteSpace($guideSummary.provenance_open_bounds)) {
                if (Tap-Bounds -Bounds $guideSummary.provenance_open_bounds) {
                    Start-Sleep -Milliseconds (1200 + ($attempt * 500))
                    if (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump) {
                        $guideSummary = Reveal-AnswerHeader -LocalDump $LocalDump
                        if (-not $guideSummary) {
                            $guideSummary = Get-UiSummary -DumpFile $LocalDump
                        }
                        if ($guideSummary) {
                            $probe.observed_title = $guideSummary.detail_title
                            $probe.observed_subtitle = $guideSummary.detail_subtitle
                            $probe.observed_body = $guideSummary.detail_body
                            $probe.followup_available = $guideSummary.followup_available
                            $probe.verified = Test-GuideSourceProbe -Summary $guideSummary -ExpectedGuideTitle $probe.expected_guide_title
                        }
                    }
                }
            }
            if ($probe.verified) {
                break
            }
        }
    }

    if (-not $probe.verified -and [string]::IsNullOrWhiteSpace($probe.error)) {
        $probe.error = "Tapped source button did not open the expected guide detail view."
    }

    return [pscustomobject]$probe
}

function Get-StrictFollowUpProofErrors {
    param(
        $SubmissionResult,
        $SourceProbe
    )

    $errors = @()

    if (-not $SubmissionResult) {
        $errors += "followup_submission_missing"
    } else {
        if ($SubmissionResult.used_fallback -eq $true) {
            $errors += ("followup_submission_used_fallback:{0}" -f $SubmissionResult.mode)
        }
        if ($SubmissionResult.advanced_after_submit -ne $true) {
            $errors += ("followup_submission_did_not_advance_after_submit:{0}" -f $SubmissionResult.primary_signal)
        }
    }

    if (-not $SourceProbe) {
        $errors += "source_link_probe_missing"
    } elseif ($SourceProbe.verified -ne $true) {
        if ($SourceProbe.attempted -ne $true) {
            $errors += ("source_link_probe_not_attempted:{0}" -f $SourceProbe.error)
        } else {
            $errors += ("source_link_not_verified:{0}" -f $SourceProbe.error)
        }
    }

    return @($errors)
}

function Start-AskActivity {
    param(
        [string]$Query,
        [string]$AutoFollowUpQuery
    )

    $encodedQuery = [System.Uri]::EscapeDataString($Query)
    $activityArgs = @(
        "-s", $Emulator,
        "shell", "am", "start",
        "-n", "com.senku.mobile/.MainActivity",
        "--es", "auto_query", (Quote-AndroidShellArg $encodedQuery),
        "--ez", "auto_ask", "true"
    )
    if ($InferenceMode -eq "host") {
        $activityArgs += @("--ez", "host_inference_enabled", "true")
        if (-not [string]::IsNullOrWhiteSpace($EffectiveHostInferenceUrl)) {
            $activityArgs += @("--es", "host_inference_url", (Quote-AndroidShellArg $EffectiveHostInferenceUrl))
        }
        if (-not [string]::IsNullOrWhiteSpace($HostInferenceModel)) {
            $activityArgs += @("--es", "host_inference_model", (Quote-AndroidShellArg $HostInferenceModel))
        }
    } elseif ($InferenceMode -eq "local") {
        $activityArgs += @("--ez", "host_inference_enabled", "false")
    }
    if (-not [string]::IsNullOrWhiteSpace($AutoFollowUpQuery)) {
        $activityArgs += @(
            "--es",
            "auto_followup_query",
            (Quote-AndroidShellArg ([System.Uri]::EscapeDataString($AutoFollowUpQuery)))
        )
    }

    if (-not $WarmStart) {
        & $adb -s $Emulator shell am force-stop com.senku.mobile | Out-Null
    }
    & $adb @activityArgs | Out-Null
}

function Submit-InDetailFollowUp {
    param(
        $InitialSummary,
        [string]$InitialQuery,
        [string]$FollowUpQuery,
        [string]$PreviousBody,
        [ValidateSet("auto", "in_detail_ui", "send_button", "ime_send", "ime_done", "hardware_enter")]
        [string]$SubmitMode = "auto"
    )

    if (-not (Tap-Bounds -Bounds $InitialSummary.followup_bounds)) {
        throw "Unable to focus the in-detail follow-up input."
    }

    Start-Sleep -Milliseconds 600
    $focusDump = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_followup_focus_" + $Emulator.Replace("-", "_") + "_" + [guid]::NewGuid().ToString("N") + ".xml")
    $focusDeviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($focusDump)
    $focusedSummary = $InitialSummary
    if (Save-UiDump -DeviceDump $focusDeviceDump -LocalDump $focusDump) {
        $latestFocusedSummary = Get-UiSummary -DumpFile $focusDump
        if ($latestFocusedSummary) {
            $focusedSummary = $latestFocusedSummary
        }
    }

    $expectedNormalized = Normalize-UiText -Text $FollowUpQuery
    $existingNormalized = Normalize-UiText -Text $focusedSummary.followup_input_text
    if ($existingNormalized -ne $expectedNormalized) {
        Clear-FollowUpInput -ExistingText $focusedSummary.followup_input_text

        $encodedFollowUp = ConvertTo-AdbInputText -Text $FollowUpQuery
        if ([string]::IsNullOrWhiteSpace($encodedFollowUp)) {
            throw "The requested follow-up query was empty after sanitization."
        }

        & $adb -s $Emulator shell input text $encodedFollowUp | Out-Null
        Start-Sleep -Milliseconds 500
    }

    $submitDump = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_followup_submit_" + $Emulator.Replace("-", "_") + "_" + [guid]::NewGuid().ToString("N") + ".xml")
    $submitDeviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($submitDump)
    $submitSummary = $null
    if (Save-UiDump -DeviceDump $submitDeviceDump -LocalDump $submitDump) {
        $submitSummary = Get-UiSummary -DumpFile $submitDump
    }

    $composerVisible = $submitSummary `
        -and $submitSummary.followup_available `
        -and -not [string]::IsNullOrWhiteSpace($submitSummary.followup_bounds)
    $sendVisible = $submitSummary `
        -and -not [string]::IsNullOrWhiteSpace($submitSummary.followup_send_bounds)
    $inputMatched = Test-FollowUpInputMatch -Summary $submitSummary -ExpectedText $FollowUpQuery
    if ($composerVisible -and -not $inputMatched) {
        $observedInput = if ($submitSummary) { $submitSummary.followup_input_text } else { $focusedSummary.followup_input_text }
        Clear-FollowUpInput -ExistingText $observedInput
        Start-Sleep -Milliseconds 250

        $encodedFollowUp = ConvertTo-AdbInputText -Text $FollowUpQuery
        if ([string]::IsNullOrWhiteSpace($encodedFollowUp)) {
            throw "The requested follow-up query was empty after sanitization."
        }

        & $adb -s $Emulator shell input text $encodedFollowUp | Out-Null
        Start-Sleep -Milliseconds 500
        if (Save-UiDump -DeviceDump $submitDeviceDump -LocalDump $submitDump) {
            $submitSummary = Get-UiSummary -DumpFile $submitDump
        }
        $composerVisible = $submitSummary `
            -and $submitSummary.followup_available `
            -and -not [string]::IsNullOrWhiteSpace($submitSummary.followup_bounds)
        $sendVisible = $submitSummary `
            -and -not [string]::IsNullOrWhiteSpace($submitSummary.followup_send_bounds)
        $inputMatched = Test-FollowUpInputMatch -Summary $submitSummary -ExpectedText $FollowUpQuery
    }
    if (-not $composerVisible) {
        if ($SubmitMode -ne "auto") {
            throw "Explicit follow-up submit mode '$SubmitMode' failed before submit: composer_missing."
        }
        Start-AskActivity -Query $InitialQuery -AutoFollowUpQuery $FollowUpQuery
        return [pscustomobject]@{
            mode = "auto_followup_fallback"
            input_echo = if ($submitSummary) { $submitSummary.followup_input_text } else { $null }
            used_fallback = $true
            input_match = $inputMatched
            composer_visible = $composerVisible
            send_visible = $sendVisible
            primary_signal = "composer_missing"
        }
    }
    if (-not $inputMatched) {
        if ($SubmitMode -ne "auto") {
            throw "Explicit follow-up submit mode '$SubmitMode' failed before submit: input_mismatch."
        }
        Start-AskActivity -Query $InitialQuery -AutoFollowUpQuery $FollowUpQuery
        return [pscustomobject]@{
            mode = "auto_followup_fallback"
            input_echo = if ($submitSummary) { $submitSummary.followup_input_text } else { $null }
            used_fallback = $true
            input_match = $false
            composer_visible = $composerVisible
            send_visible = $sendVisible
            primary_signal = "input_mismatch"
        }
    }

    if (Save-UiDump -DeviceDump $submitDeviceDump -LocalDump $submitDump) {
        $submitSummary = Get-UiSummary -DumpFile $submitDump
    }

    $sendBounds = if ($submitSummary -and -not [string]::IsNullOrWhiteSpace($submitSummary.followup_send_bounds)) {
        $submitSummary.followup_send_bounds
    } else {
        $InitialSummary.followup_send_bounds
    }

    $effectiveSubmitMode = if ($SubmitMode -eq "auto" -or $SubmitMode -eq "in_detail_ui") { "send_button" } else { $SubmitMode }

    # The inline composer already keeps the send button visible. Avoid KEYCODE_BACK here,
    # because some emulator keyboard setups treat it as navigation and drop us out of detail.
    switch ($effectiveSubmitMode) {
        "send_button" {
            if (-not (Tap-Bounds -Bounds $sendBounds)) {
                if ($SubmitMode -ne "auto") {
                    throw "Explicit follow-up submit mode '$SubmitMode' failed: send_button_missing."
                }
                & $adb -s $Emulator shell input keyevent 66 | Out-Null
                $effectiveSubmitMode = "hardware_enter"
            }
        }
        "ime_send" {
            & $adb -s $Emulator shell input keyevent 66 | Out-Null
        }
        "ime_done" {
            & $adb -s $Emulator shell input keyevent 66 | Out-Null
        }
        "hardware_enter" {
            & $adb -s $Emulator shell input keyevent 66 | Out-Null
        }
    }

    Start-Sleep -Milliseconds 900
    $advanceDump = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_followup_advance_" + $Emulator.Replace("-", "_") + "_" + [guid]::NewGuid().ToString("N") + ".xml")
    $advancedSummary = Wait-ForFollowUpAdvanceSignal `
        -ExpectedTitle $FollowUpQuery `
        -PreviousTitle $InitialSummary.detail_title `
        -PreviousBody $PreviousBody `
        -LocalDump $advanceDump `
        -MaxWaitSeconds 8
    $advanced = Test-FollowUpAdvanceSignal `
        -Summary $advancedSummary `
        -ExpectedTitle $FollowUpQuery `
        -PreviousTitle $InitialSummary.detail_title `
        -PreviousBody $PreviousBody

    if (-not $advanced -and $SubmitMode -eq "auto") {
        & $adb -s $Emulator shell input keyevent 66 | Out-Null
        Start-Sleep -Milliseconds 900
        $advancedSummary = Wait-ForFollowUpAdvanceSignal `
            -ExpectedTitle $FollowUpQuery `
            -PreviousTitle $InitialSummary.detail_title `
            -PreviousBody $PreviousBody `
            -LocalDump $advanceDump `
            -MaxWaitSeconds 8
        $advanced = Test-FollowUpAdvanceSignal `
            -Summary $advancedSummary `
            -ExpectedTitle $FollowUpQuery `
            -PreviousTitle $InitialSummary.detail_title `
            -PreviousBody $PreviousBody
    }

    if (-not $advanced -and $SubmitMode -eq "auto") {
        if (Save-UiDump -DeviceDump $submitDeviceDump -LocalDump $submitDump) {
            $submitSummary = Get-UiSummary -DumpFile $submitDump
        }
        $retrySendBounds = if ($submitSummary -and -not [string]::IsNullOrWhiteSpace($submitSummary.followup_send_bounds)) {
            $submitSummary.followup_send_bounds
        } else {
            $sendBounds
        }
        if (-not [string]::IsNullOrWhiteSpace($retrySendBounds)) {
            Tap-Bounds -Bounds $retrySendBounds | Out-Null
            Start-Sleep -Milliseconds 900
            $advancedSummary = Wait-ForFollowUpAdvanceSignal `
                -ExpectedTitle $FollowUpQuery `
                -PreviousTitle $InitialSummary.detail_title `
                -PreviousBody $PreviousBody `
                -LocalDump $advanceDump `
                -MaxWaitSeconds 8
            $advanced = Test-FollowUpAdvanceSignal `
                -Summary $advancedSummary `
                -ExpectedTitle $FollowUpQuery `
                -PreviousTitle $InitialSummary.detail_title `
                -PreviousBody $PreviousBody
        }
    }

    return [pscustomobject]@{
        mode = if ($SubmitMode -eq "auto") { "in_detail_ui" } else { $SubmitMode }
        input_echo = if ($submitSummary) { $submitSummary.followup_input_text } else { $null }
        used_fallback = $false
        input_match = $inputMatched
        composer_visible = $composerVisible
        send_visible = $sendVisible
        primary_signal = if ($advanced) {
            "{0}_advanced" -f $effectiveSubmitMode
        } else {
            "{0}_without_visible_advance" -f $effectiveSubmitMode
        }
        advanced_after_submit = $advanced
    }
}

function Wait-ForFollowUpControls {
    param(
        [string]$LocalDump,
        [int]$MaxWaitSeconds = 12,
        [int]$PollMilliseconds = 1000,
        [int]$MaxScrollAttempts = 4
    )

    $deviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($LocalDump)
    $attempts = [Math]::Max(1, [int][Math]::Ceiling(($MaxWaitSeconds * 1000.0) / $PollMilliseconds))
    $latestSummary = $null
    for ($attempt = 0; $attempt -lt $attempts; $attempt++) {
        if (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump) {
            $latestSummary = Get-UiSummary -DumpFile $LocalDump
            if ($latestSummary -and -not [string]::IsNullOrWhiteSpace($latestSummary.followup_bounds)) {
                return $latestSummary
            }
            if ($latestSummary -and -not [string]::IsNullOrWhiteSpace($latestSummary.detail_scroll_bounds)) {
                for ($scrollAttempt = 0; $scrollAttempt -lt $MaxScrollAttempts; $scrollAttempt++) {
                    if (-not (Swipe-Bounds -Bounds $latestSummary.detail_scroll_bounds)) {
                        break
                    }
                    Start-Sleep -Milliseconds 850
                    if (-not (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump)) {
                        continue
                    }
                    $latestSummary = Get-UiSummary -DumpFile $LocalDump
                    if ($latestSummary -and -not [string]::IsNullOrWhiteSpace($latestSummary.followup_bounds)) {
                        return $latestSummary
                    }
                }
            }
        }
        if ($attempt -lt ($attempts - 1)) {
            Start-Sleep -Milliseconds $PollMilliseconds
        }
    }
    return $latestSummary
}

function Reveal-AnswerHeader {
    param(
        [string]$LocalDump,
        [string]$ExpectedTitle,
        [string]$PreviousBody,
        [int]$MaxScrollAttempts = 5
    )

    $lastSummary = $null
    if (Test-Path $LocalDump) {
        $lastSummary = Get-UiSummary -DumpFile $LocalDump
        if (Test-DetailCompletion -Summary $lastSummary -ExpectedTitle $ExpectedTitle -PreviousBody $PreviousBody) {
            return $lastSummary
        }
        if ($lastSummary -and [string]::IsNullOrWhiteSpace($ExpectedTitle) -and -not [string]::IsNullOrWhiteSpace($lastSummary.detail_title)) {
            return $lastSummary
        }
    }

    $refreshDump = Join-Path (Split-Path -Parent $LocalDump) (([System.IO.Path]::GetFileNameWithoutExtension($LocalDump)) + "_reveal_tmp.xml")
    $deviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($refreshDump)

    for ($attempt = 0; $attempt -le $MaxScrollAttempts; $attempt++) {
        if (Save-UiDump -DeviceDump $deviceDump -LocalDump $refreshDump) {
            $lastSummary = Get-UiSummary -DumpFile $refreshDump
            if (Test-DetailCompletion -Summary $lastSummary -ExpectedTitle $ExpectedTitle -PreviousBody $PreviousBody) {
                Copy-DumpSnapshot -SourceDump $refreshDump -DestinationDump $LocalDump
                return $lastSummary
            }
            if ($lastSummary -and [string]::IsNullOrWhiteSpace($ExpectedTitle) -and -not [string]::IsNullOrWhiteSpace($lastSummary.detail_title)) {
                Copy-DumpSnapshot -SourceDump $refreshDump -DestinationDump $LocalDump
                return $lastSummary
            }
        }

        if ($attempt -eq $MaxScrollAttempts) {
            break
        }

        if (-not $lastSummary -or [string]::IsNullOrWhiteSpace($lastSummary.detail_scroll_bounds)) {
            break
        }

        if (-not (Swipe-Bounds -Bounds $lastSummary.detail_scroll_bounds -StartRatio 0.28 -EndRatio 0.86)) {
            break
        }
        Start-Sleep -Milliseconds 850
    }

    return $lastSummary
}

function Reveal-ExpectedAnswerState {
    param(
        [string]$ExpectedTitle,
        [string]$PreviousBody,
        [string]$LocalDump,
        [int]$MaxScrollAttempts = 5
    )

    $deviceDump = "/sdcard/" + [System.IO.Path]::GetFileName($LocalDump)
    $lastSummary = $null

    for ($attempt = 0; $attempt -le $MaxScrollAttempts; $attempt++) {
        if (Save-UiDump -DeviceDump $deviceDump -LocalDump $LocalDump) {
            $lastSummary = Get-UiSummary -DumpFile $LocalDump
            if (Test-DetailCompletion -Summary $lastSummary -ExpectedTitle $ExpectedTitle -PreviousBody $PreviousBody) {
                return $lastSummary
            }
        }

        if ($attempt -eq $MaxScrollAttempts) {
            break
        }

        if (-not $lastSummary -or [string]::IsNullOrWhiteSpace($lastSummary.detail_scroll_bounds)) {
            break
        }

        if (-not (Swipe-Bounds -Bounds $lastSummary.detail_scroll_bounds -StartRatio 0.24 -EndRatio 0.90 -DurationMs 300)) {
            break
        }
        Start-Sleep -Milliseconds 850
    }

    return $lastSummary
}

function Invoke-InitialAnswerRun {
    param([string]$LocalDump)

    # The follow-up harness needs the detail activity to remain alive after the
    # initial answer settles so it can reuse the inline composer. The
    # instrumentation prompt lane intentionally tears the activity down when the
    # test completes, so force the shell-backed execution path here.
    powershell -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "run_android_prompt.ps1") `
        -Emulator $Emulator `
        -Query $InitialQuery `
        -Ask `
        -InferenceMode $InferenceMode `
        -HostInferenceUrl $EffectiveHostInferenceUrl `
        -HostInferenceModel $HostInferenceModel `
        -ForceShellExecution `
        -WaitForCompletion `
        -MaxWaitSeconds $InitialMaxWaitSeconds `
        -PollSeconds $PollSeconds `
        -DumpPath $LocalDump | Out-Null
}

$outputPath = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

$slug = if ([string]::IsNullOrWhiteSpace($RunLabel)) {
    "{0}_{1}" -f (New-Slug -Text $InitialQuery), (New-Slug -Text $FollowUpQuery)
} else {
    ($RunLabel -replace "[^A-Za-z0-9_-]+", "_").Trim("_")
}

$initialDump = Join-Path $outputPath ("{0}_initial.xml" -f $slug)
$followupReadyDump = Join-Path $outputPath ("{0}_followup_ready.xml" -f $slug)
$followupDump = Join-Path $outputPath ("{0}_followup.xml" -f $slug)
$followupAnswerDump = Join-Path $outputPath ("{0}_followup_answer.xml" -f $slug)
$followupAnswerScreenshot = Join-Path $outputPath ("{0}_followup_answer.png" -f $slug)
$followupLowerDump = Join-Path $outputPath ("{0}_followup_lower.xml" -f $slug)
$sourceProbeDump = Join-Path $outputPath ("{0}_source_probe.xml" -f $slug)
$manifestPath = Join-Path $outputPath ("{0}.json" -f $slug)

Write-Host "Running initial offline answer on $Emulator"
Invoke-InitialAnswerRun -LocalDump $initialDump
$initialAnswerSummary = Get-UiSummary -DumpFile $initialDump
$initialLooksValid = Test-IsSenkuDetailSummary -Summary $initialAnswerSummary
if (-not $initialLooksValid -and -not (Test-MainScreenBusy -Summary $initialAnswerSummary)) {
    Write-Warning "Initial answer capture did not land on a Senku detail screen; retrying the initial detail acquisition once."
    Invoke-InitialAnswerRun -LocalDump $initialDump
    $initialAnswerSummary = Get-UiSummary -DumpFile $initialDump
}
$initialControlWaitSeconds = 12
if (Test-MainScreenBusy -Summary $initialAnswerSummary) {
    $initialControlWaitSeconds = [Math]::Min([Math]::Max(30, $InitialMaxWaitSeconds), 120)
}
$initialSummary = Wait-ForFollowUpControls -LocalDump $followupReadyDump -MaxWaitSeconds $initialControlWaitSeconds
$expectedFollowUpTitle = $FollowUpQuery.Trim()
$submissionResult = $null
$allowAutoFollowUpFallback = ($FollowUpSubmissionMode -eq "auto")
if ($FollowUpSubmissionMode -eq "auto_intent") {
    Write-Host ("Submitting follow-up via auto intent: {0}" -f $expectedFollowUpTitle)
    Start-AskActivity -Query $InitialQuery -AutoFollowUpQuery $expectedFollowUpTitle
    $submissionResult = [pscustomobject]@{
        mode = "auto_followup_direct"
        input_echo = $null
        used_fallback = $false
        input_match = $null
        composer_visible = $null
        send_visible = $null
        primary_signal = "explicit_auto_intent"
        advanced_after_submit = $true
    }
} elseif (-not $initialSummary -or [string]::IsNullOrWhiteSpace($initialSummary.followup_bounds)) {
    Write-Warning "Initial answer screen did not expose the in-detail follow-up controls; retrying the initial detail acquisition once more."
    Invoke-InitialAnswerRun -LocalDump $initialDump
    $initialAnswerSummary = Get-UiSummary -DumpFile $initialDump
    $initialSummary = Wait-ForFollowUpControls -LocalDump $followupReadyDump
}
if ($FollowUpSubmissionMode -ne "auto_intent" -and (-not $initialSummary -or [string]::IsNullOrWhiteSpace($initialSummary.followup_bounds))) {
    if (-not $allowAutoFollowUpFallback) {
        throw "Explicit follow-up submit mode '$FollowUpSubmissionMode' failed before submit: initial_controls_missing."
    }
    Write-Warning "Initial answer screen did not expose the in-detail follow-up controls; retrying with auto-followup fallback."
    Start-AskActivity -Query $InitialQuery -AutoFollowUpQuery $expectedFollowUpTitle
        $submissionResult = [pscustomobject]@{
            mode = "auto_followup_initial_fallback"
            input_echo = $null
            used_fallback = $true
            input_match = $null
            composer_visible = $false
            send_visible = $false
            primary_signal = "initial_controls_missing"
        }
} elseif ($FollowUpSubmissionMode -ne "auto_intent") {
    Write-Host ("Submitting in-detail follow-up: {0}" -f $expectedFollowUpTitle)
    $submissionResult = Submit-InDetailFollowUp `
        -InitialSummary $initialSummary `
        -InitialQuery $InitialQuery `
        -FollowUpQuery $expectedFollowUpTitle `
        -PreviousBody $(if ($initialAnswerSummary) { $initialAnswerSummary.detail_body } else { $initialSummary.detail_body }) `
        -SubmitMode $(if ($FollowUpSubmissionMode -eq "auto") { "auto" } else { $FollowUpSubmissionMode })
}
if ($submissionResult -and -not $submissionResult.used_fallback -and -not $submissionResult.advanced_after_submit -and $allowAutoFollowUpFallback) {
    Write-Warning "In-detail follow-up did not show an advance signal after repeated send attempts; retrying with auto-followup fallback."
    Start-AskActivity -Query $InitialQuery -AutoFollowUpQuery $expectedFollowUpTitle
    $submissionResult = [pscustomobject]@{
        mode = "auto_followup_post_submit_fallback"
        input_echo = if ($submissionResult) { $submissionResult.input_echo } else { $null }
        used_fallback = $true
        input_match = if ($submissionResult) { $submissionResult.input_match } else { $null }
        composer_visible = if ($submissionResult) { $submissionResult.composer_visible } else { $null }
        send_visible = if ($submissionResult) { $submissionResult.send_visible } else { $null }
        primary_signal = "post_submit_no_advance"
        advanced_after_submit = $false
    }
}
    $followupSummary = Wait-ForDetailCompletion `
        -ExpectedTitle $expectedFollowUpTitle `
        -PreviousBody $(if ($initialAnswerSummary) { $initialAnswerSummary.detail_body } else { $initialSummary.detail_body }) `
        -MaxWaitSeconds ($InitialMaxWaitSeconds + $FollowUpMaxWaitSeconds) `
        -LocalDump $followupDump `
        -FrozenDump $followupAnswerDump
if (-not (Test-DetailCompletion -Summary $followupSummary -ExpectedTitle $expectedFollowUpTitle -PreviousBody $(if ($initialAnswerSummary) { $initialAnswerSummary.detail_body } else { $initialSummary.detail_body }))) {
    if ($submissionResult -and -not $submissionResult.used_fallback -and $allowAutoFollowUpFallback) {
        Write-Warning "In-detail follow-up did not advance cleanly; retrying with auto-followup fallback."
        Start-AskActivity -Query $InitialQuery -AutoFollowUpQuery $expectedFollowUpTitle
        $submissionResult = [pscustomobject]@{
            mode = "auto_followup_post_submit_fallback"
            input_echo = if ($submissionResult) { $submissionResult.input_echo } else { $null }
            used_fallback = $true
            input_match = if ($submissionResult) { $submissionResult.input_match } else { $null }
            composer_visible = if ($submissionResult) { $submissionResult.composer_visible } else { $null }
            send_visible = if ($submissionResult) { $submissionResult.send_visible } else { $null }
            primary_signal = if ($submissionResult -and -not $submissionResult.advanced_after_submit) { "post_submit_no_advance" } else { "post_submit_timeout" }
            advanced_after_submit = if ($submissionResult) { $submissionResult.advanced_after_submit } else { $null }
        }
        $followupSummary = Wait-ForDetailCompletion `
            -ExpectedTitle $expectedFollowUpTitle `
            -PreviousBody $(if ($initialAnswerSummary) { $initialAnswerSummary.detail_body } else { $initialSummary.detail_body }) `
            -MaxWaitSeconds ($InitialMaxWaitSeconds + $FollowUpMaxWaitSeconds) `
            -LocalDump $followupDump `
            -FrozenDump $followupAnswerDump
    }
    if (-not (Test-DetailCompletion -Summary $followupSummary -ExpectedTitle $expectedFollowUpTitle -PreviousBody $(if ($initialAnswerSummary) { $initialAnswerSummary.detail_body } else { $initialSummary.detail_body }))) {
        if (-not $allowAutoFollowUpFallback -and $submissionResult) {
            throw ("Explicit follow-up submit mode '{0}' did not complete; primary signal: {1}." -f $FollowUpSubmissionMode, $submissionResult.primary_signal)
        }
        throw "Timed out waiting for in-detail follow-up completion."
    }
}

$previousBodyForFollowUp = if ($initialAnswerSummary) { $initialAnswerSummary.detail_body } else { $initialSummary.detail_body }
if (-not (Test-Path $followupAnswerDump) -and (Test-Path $followupDump)) {
    Copy-Item -LiteralPath $followupDump -Destination $followupAnswerDump -Force
}

$followupAnswerSummary = Reveal-AnswerHeader `
    -LocalDump $followupAnswerDump `
    -ExpectedTitle $expectedFollowUpTitle `
    -PreviousBody $previousBodyForFollowUp
if ((-not $followupAnswerSummary) -or (-not (Test-DetailCompletion -Summary $followupAnswerSummary -ExpectedTitle $expectedFollowUpTitle -PreviousBody $previousBodyForFollowUp))) {
    $followupAnswerSummary = $followupSummary
}
if ((-not $followupAnswerSummary) -or (Test-IsGuideModeSummary -Summary $followupAnswerSummary) -or (-not (Test-DetailCompletion -Summary $followupAnswerSummary -ExpectedTitle $expectedFollowUpTitle -PreviousBody $previousBodyForFollowUp))) {
    throw "Captured follow-up screen was not a completed answer state."
}
Capture-Screenshot -LocalPath $followupAnswerScreenshot | Out-Null

$followupLowerSummary = $null
$sourceProbe = $null
try {
    $followupLowerSummary = Reveal-SourceLinks -LocalDump $followupLowerDump
    if ($SkipSourceProbe) {
        $sourceProbe = [pscustomobject]@{
            attempted = $false
            verified = $false
            source_label = $null
            expected_guide_title = $null
            dump_path = $sourceProbeDump
            observed_title = $null
            observed_subtitle = $null
            observed_body = $null
            followup_available = $null
            error = "Source probe skipped by request."
        }
    } else {
        $sourceProbe = Probe-FirstSourceLink -SourceSummary $followupLowerSummary -LocalDump $sourceProbeDump
    }
} catch {
    $sourceProbe = [pscustomobject]@{
        attempted = $false
        verified = $false
        source_label = $null
        expected_guide_title = $null
        dump_path = $sourceProbeDump
        observed_title = $null
        observed_subtitle = $null
        observed_body = $null
        followup_available = $null
        error = $_.Exception.Message
    }
}

$pushPackSummary = Read-JsonFileOrNull -Path $PushPackSummaryPath
$strictFollowUpProofErrors = @(Get-StrictFollowUpProofErrors -SubmissionResult $submissionResult -SourceProbe $sourceProbe)
$strictFollowUpProofPassed = ($strictFollowUpProofErrors.Count -eq 0)

$record = [ordered]@{
    emulator = $Emulator
    inference_mode = $InferenceMode
    host_inference_url = $EffectiveHostInferenceUrl
    host_inference_model = $HostInferenceModel
    initial_query = $InitialQuery
    requested_follow_up = $FollowUpQuery
    initial_dump = $initialDump
    followup_ready_dump = $followupReadyDump
    followup_dump = $followupDump
    followup_answer_dump = $followupAnswerDump
    followup_answer_screenshot = $followupAnswerScreenshot
    followup_lower_dump = $followupLowerDump
    source_probe_dump = $sourceProbeDump
    initial_title = if ($initialAnswerSummary -and -not [string]::IsNullOrWhiteSpace($initialAnswerSummary.detail_title)) { $initialAnswerSummary.detail_title } elseif ($initialSummary -and -not [string]::IsNullOrWhiteSpace($initialSummary.detail_title)) { $initialSummary.detail_title } else { $InitialQuery }
    final_detail_title = if ($followupAnswerSummary -and -not [string]::IsNullOrWhiteSpace($followupAnswerSummary.detail_title)) { $followupAnswerSummary.detail_title } elseif ($followupSummary -and -not [string]::IsNullOrWhiteSpace($followupSummary.detail_title)) { $followupSummary.detail_title } else { $expectedFollowUpTitle }
    final_detail_subtitle = if ($followupAnswerSummary -and -not [string]::IsNullOrWhiteSpace($followupAnswerSummary.detail_subtitle)) { $followupAnswerSummary.detail_subtitle } elseif ($followupSummary) { $followupSummary.detail_subtitle } else { $null }
    final_detail_body = if ($followupAnswerSummary -and -not [string]::IsNullOrWhiteSpace($followupAnswerSummary.detail_body)) { $followupAnswerSummary.detail_body } elseif ($followupSummary) { $followupSummary.detail_body } else { $null }
    final_detail_status = if ($followupAnswerSummary -and -not [string]::IsNullOrWhiteSpace($followupAnswerSummary.detail_status)) { $followupAnswerSummary.detail_status } elseif ($followupSummary) { $followupSummary.detail_status } else { $null }
    final_followup_available = if ($followupAnswerSummary) { $followupAnswerSummary.followup_available } elseif ($followupSummary) { $followupSummary.followup_available } else { $null }
    followup_submission_requested_mode = $FollowUpSubmissionMode
    followup_submission_mode = if ($submissionResult) { $submissionResult.mode } else { $null }
    followup_submission_input_echo = if ($submissionResult) { $submissionResult.input_echo } else { $null }
    followup_submission_input_match = if ($submissionResult) { $submissionResult.input_match } else { $null }
    followup_submission_used_fallback = if ($submissionResult) { $submissionResult.used_fallback } else { $null }
    followup_submission_composer_visible = if ($submissionResult) { $submissionResult.composer_visible } else { $null }
    followup_submission_send_visible = if ($submissionResult) { $submissionResult.send_visible } else { $null }
    followup_submission_primary_signal = if ($submissionResult) { $submissionResult.primary_signal } else { $null }
    followup_submission_advanced_after_submit = if ($submissionResult) { $submissionResult.advanced_after_submit } else { $null }
    push_pack_summary_path = $(if ([string]::IsNullOrWhiteSpace($PushPackSummaryPath)) { $null } else { $PushPackSummaryPath })
    push_pack_cache_hit = $(if ($pushPackSummary) { [bool]$pushPackSummary.cache_hit } else { $null })
    push_pack_pushed = $(if ($pushPackSummary) { [bool]$pushPackSummary.pushed } else { $null })
    push_pack_state_path = $(if ($pushPackSummary) { [string]$pushPackSummary.state_path } else { $null })
    lower_sources_visible = if ($followupLowerSummary) { $followupLowerSummary.sources_visible } else { $null }
    lower_source_buttons = if ($followupLowerSummary) { @($followupLowerSummary.source_buttons) } else { @() }
    lower_source_button_count = if ($followupLowerSummary) { $followupLowerSummary.source_button_details.Count } else { $null }
    lower_session_visible = if ($followupLowerSummary) { $followupLowerSummary.session_visible } else { $null }
    lower_detail_body = if ($followupLowerSummary) { $followupLowerSummary.detail_body } else { $null }
    source_link_probe_attempted = if ($sourceProbe) { $sourceProbe.attempted } else { $null }
    source_link_verified = if ($sourceProbe) { $sourceProbe.verified } else { $null }
    source_link_probe_label = if ($sourceProbe) { $sourceProbe.source_label } else { $null }
    source_link_probe_expected_guide_title = if ($sourceProbe) { $sourceProbe.expected_guide_title } else { $null }
    source_link_probe_observed_title = if ($sourceProbe) { $sourceProbe.observed_title } else { $null }
    source_link_probe_observed_subtitle = if ($sourceProbe) { $sourceProbe.observed_subtitle } else { $null }
    source_link_probe_followup_available = if ($sourceProbe) { $sourceProbe.followup_available } else { $null }
    source_link_probe_error = if ($sourceProbe) { $sourceProbe.error } else { $null }
    strict_followup_proof_required = [bool]$RequireStrictFollowUpProof
    strict_followup_proof_passed = $strictFollowUpProofPassed
    strict_followup_proof_errors = $strictFollowUpProofErrors
    completed_at = (Get-Date).ToString("o")
}

$record | ConvertTo-Json -Depth 6 | Set-Content -Path $manifestPath -Encoding UTF8
Write-Host "Detail follow-up artifacts written to $manifestPath"

if ($RequireStrictFollowUpProof -and -not $strictFollowUpProofPassed) {
    throw ("Strict follow-up proof failed: {0}" -f ($strictFollowUpProofErrors -join "; "))
}

exit 0
