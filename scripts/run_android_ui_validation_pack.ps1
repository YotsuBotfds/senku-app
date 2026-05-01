param(
    [string[]]$Devices = @("RFCX607ZM8L", "R92X51AG48D"),
    [string]$OutputRoot = "artifacts/ui_validation",
    [string]$PromptPackFile = "",
    [switch]$InstallApk,
    [string]$ApkPath = "android-app/app/build/outputs/apk/debug/app-debug.apk",
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "preserve",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [ValidateSet("portrait", "landscape")]
    [string]$Orientation = "portrait",
    [double]$FontScale = 1.0,
    [switch]$UseInstrumentationPreflight,
    [switch]$UseInstrumentationExecution,
    [switch]$ForceShellExecution,
    [switch]$IncludeFollowUpCase,
    [int]$MaxWaitSeconds = 300,
    [int]$PollSeconds = 4,
    [int]$WaitSeconds = 2
)

$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$runPromptScript = Join-Path $PSScriptRoot "run_android_prompt.ps1"
$instrumentedSmokeScript = Join-Path $PSScriptRoot "run_android_instrumented_ui_smoke.ps1"
if (-not (Test-Path -LiteralPath $runPromptScript)) {
    throw "Missing script: $runPromptScript"
}
if ($UseInstrumentationPreflight -and -not (Test-Path -LiteralPath $instrumentedSmokeScript)) {
    throw "Missing script: $instrumentedSmokeScript"
}

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path -LiteralPath $adb)) {
    throw "adb not found at $adb"
}
$lockRoot = Join-Path $repoRoot "artifacts\\harness_locks"
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
if (-not (Test-Path -LiteralPath $commonHarnessModule)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}
Import-Module $commonHarnessModule -Force -DisableNameChecking
New-Item -ItemType Directory -Force -Path $lockRoot | Out-Null

$resolvedApkPath = Join-Path $repoRoot $ApkPath
if ($InstallApk -and -not (Test-Path -LiteralPath $resolvedApkPath)) {
    throw "APK not found: $resolvedApkPath"
}
if (-not [string]::IsNullOrWhiteSpace($PromptPackFile)) {
    $resolvedPromptPackFile = if ([System.IO.Path]::IsPathRooted($PromptPackFile)) { $PromptPackFile } else { Join-Path $repoRoot $PromptPackFile }
    if (-not (Test-Path -LiteralPath $resolvedPromptPackFile)) {
        throw "Prompt pack file not found: $resolvedPromptPackFile"
    }
}

$normalizedDevices = @(Resolve-AndroidHarnessDeviceList -Devices $Devices)
if ($normalizedDevices.Count -eq 0) {
    throw "No valid devices provided."
}

function Get-ValidationCases {
    param(
        [string]$PackPath,
        [switch]$IncludeFollowUp
    )

    $defaultCases = @(
        @{
            id = "det_fire_rain"
            query = "How do I start a fire in rain?"
            expected_mode_hint = "deterministic"
        },
        @{
            id = "gen_rain_shelter"
            query = "How do I build a simple rain shelter from tarp and cord?"
            expected_mode_hint = "generated"
        }
    )
    if ($IncludeFollowUp) {
        $defaultCases += @{
            id = "followup_rain_shelter"
            query = "How do I build a simple rain shelter from tarp and cord?"
            followup_query = "What should I do next after the ridge line is up?"
            expected_mode_hint = "generated"
        }
    }

    if ([string]::IsNullOrWhiteSpace($PackPath)) {
        return $defaultCases
    }

    $extension = [System.IO.Path]::GetExtension($PackPath).ToLowerInvariant()
    $loadedCases = @()
    switch ($extension) {
        ".json" {
            $raw = Get-Content -LiteralPath $PackPath -Raw | ConvertFrom-Json
            if ($raw -is [System.Collections.IEnumerable] -and -not ($raw -is [string])) {
                $loadedCases = @($raw)
            } else {
                $loadedCases = @($raw)
            }
        }
        ".jsonl" {
            foreach ($line in Get-Content -LiteralPath $PackPath) {
                $trimmed = $line.Trim()
                if ([string]::IsNullOrWhiteSpace($trimmed)) {
                    continue
                }
                $loadedCases += ($trimmed | ConvertFrom-Json)
            }
        }
        default {
            throw "Unsupported prompt-pack extension '$extension'. Use .json or .jsonl."
        }
    }

    $normalized = New-Object System.Collections.Generic.List[object]
    $index = 0
    foreach ($case in $loadedCases) {
        $index += 1
        $query = [string]$case.query
        if ([string]::IsNullOrWhiteSpace($query)) {
            throw "Prompt-pack case #$index is missing 'query'."
        }
        $id = [string]$case.id
        if ([string]::IsNullOrWhiteSpace($id)) {
            $id = "case_{0:d2}" -f $index
        }
        $expectedModeHint = [string]$case.expected_mode_hint
        if ([string]::IsNullOrWhiteSpace($expectedModeHint)) {
            $expectedModeHint = if ([string]::IsNullOrWhiteSpace([string]$case.followup_query)) { "generated" } else { "generated" }
        }
        $normalized.Add([ordered]@{
            id = $id
            query = $query
            followup_query = [string]$case.followup_query
            expected_mode_hint = $expectedModeHint
        })
    }

    return $normalized
}

$cases = Get-ValidationCases -PackPath $resolvedPromptPackFile -IncludeFollowUp:$IncludeFollowUpCase

$resolvedInstrumentationExecution = $UseInstrumentationExecution -or (-not $ForceShellExecution)

function Get-UiFieldText {
    param(
        [string]$XmlText,
        [string]$ResourceId
    )

    if ([string]::IsNullOrWhiteSpace($XmlText)) {
        return ""
    }

    $escaped = [regex]::Escape($ResourceId)
    $nodeMatch = [regex]::Match($XmlText, "(?s)<node\b[^>]*resource-id=[""']$escaped[""'][^>]*>")
    if (-not $nodeMatch.Success) {
        return ""
    }
    $textMatch = [regex]::Match($nodeMatch.Value, "\btext=(?:""([^""]*)""|'([^']*)')")
    if (-not $textMatch.Success) {
        return ""
    }
    $rawValue = if ($textMatch.Groups[1].Success) { $textMatch.Groups[1].Value } else { $textMatch.Groups[2].Value }
    return [System.Net.WebUtility]::HtmlDecode($rawValue)
}

function Get-UiAttributeValues {
    param(
        [string]$XmlText,
        [string]$AttributeName
    )

    if ([string]::IsNullOrWhiteSpace($XmlText) -or [string]::IsNullOrWhiteSpace($AttributeName)) {
        return @()
    }

    $escapedAttribute = [regex]::Escape($AttributeName)
    $matches = [regex]::Matches($XmlText, "\b$escapedAttribute=(?:""([^""]*)""|'([^']*)')")
    $values = New-Object System.Collections.Generic.List[string]
    foreach ($match in $matches) {
        $rawValue = if ($match.Groups[1].Success) { $match.Groups[1].Value } else { $match.Groups[2].Value }
        $decoded = [System.Net.WebUtility]::HtmlDecode([string]$rawValue).Trim()
        if (-not [string]::IsNullOrWhiteSpace($decoded)) {
            $values.Add($decoded)
        }
    }
    return @($values)
}

function Get-ComposeDetailMetaText {
    param([string]$XmlText)

    $contentDescriptions = Get-UiAttributeValues -XmlText $XmlText -AttributeName "content-desc"
    foreach ($description in @($contentDescriptions)) {
        $lower = $description.ToLowerInvariant()
        if ($lower.Contains("sources") -and ($lower.Contains("answered") -or $lower.Contains("evidence") -or $lower.Contains("turn"))) {
            return $description
        }
    }

    $texts = Get-UiAttributeValues -XmlText $XmlText -AttributeName "text"
    foreach ($text in @($texts)) {
        if ($text -match '^\d+\s+SOURCES\b') {
            return $text
        }
    }

    return ""
}

function Get-ComposeDetailTitleText {
    param([string]$XmlText)

    $texts = Get-UiAttributeValues -XmlText $XmlText -AttributeName "text"
    for ($index = 0; $index -lt ($texts.Count - 1); $index++) {
        if ($texts[$index] -match '^GD-\d+$' -and $texts[$index + 1].Length -ge 10) {
            return $texts[$index + 1]
        }
    }

    return ""
}

function Get-ComposeDetailBodyText {
    param([string]$XmlText)

    $texts = Get-UiAttributeValues -XmlText $XmlText -AttributeName "text"
    for ($index = 0; $index -lt $texts.Count; $index++) {
        if ($texts[$index].ToUpperInvariant() -ne "SENKU ANSWERED") {
            continue
        }

        $limit = [Math]::Min($index + 8, $texts.Count - 1)
        for ($candidateIndex = $index + 1; $candidateIndex -le $limit; $candidateIndex++) {
            $candidate = [string]$texts[$candidateIndex]
            if ($candidate.Length -lt 40) {
                continue
            }
            if ($candidate -match '^GD-\d+$') {
                continue
            }
            if ($candidate -match '^\d+\s+SOURCES\b') {
                continue
            }
            if ($candidate.ToUpperInvariant().Contains("EVIDENCE")) {
                continue
            }
            return $candidate
        }
    }

    $fallbackCandidates = @(
        $texts | Where-Object {
            $_.Length -ge 40 `
                -and $_ -notmatch '^GD-\d+$' `
                -and $_ -notmatch '^\d+\s+SOURCES\b' `
                -and $_ -notmatch '^(YOU ASKED|SENKU ANSWERED)$'
        }
    )
    if ($fallbackCandidates.Count -gt 0) {
        return [string]$fallbackCandidates[0]
    }

    return ""
}

function Test-ComposeFollowUpVisible {
    param([string]$XmlText)

    $texts = Get-UiAttributeValues -XmlText $XmlText -AttributeName "text"
    foreach ($text in @($texts)) {
        $lower = $text.ToLowerInvariant()
        if ($lower.Contains("follow-up") -or $lower.Contains("follow up") -or $lower -eq "send") {
            return $true
        }
    }

    return $false
}

function Acquire-DeviceLock {
    param(
        [string]$DeviceName,
        [int]$TimeoutSeconds = 900
    )

    return Acquire-AndroidHarnessDeviceLock -DeviceName $DeviceName -LockRoot $lockRoot -TimeoutSeconds $TimeoutSeconds
}

function Capture-Screenshot {
    param(
        [string]$Device,
        [string]$LocalPath
    )

    $remotePath = "/sdcard/senku_validation_screen.png"
    & $adb -s $Device shell screencap -p $remotePath | Out-Null
    & $adb -s $Device pull $remotePath $LocalPath | Out-Null
    & $adb -s $Device shell rm $remotePath | Out-Null
}

function Copy-ArtifactIntoCase {
    param(
        [string]$SourcePath,
        [string]$DestinationPath
    )

    if ([string]::IsNullOrWhiteSpace($SourcePath) -or [string]::IsNullOrWhiteSpace($DestinationPath)) {
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

function Get-ObjectPropertyValue {
    param(
        [object]$InputObject,
        [string[]]$Names
    )

    if ($null -eq $InputObject) {
        return $null
    }

    foreach ($name in $Names) {
        if ([string]::IsNullOrWhiteSpace($name)) {
            continue
        }
        $property = $InputObject.PSObject.Properties[$name]
        if ($null -ne $property) {
            return $property.Value
        }
    }

    return $null
}

function Resolve-InstrumentationArtifactEntryPath {
    param(
        [object]$Entry,
        [string]$ArtifactDir,
        [string]$ArtifactSubdir
    )

    if ($null -eq $Entry) {
        return $null
    }

    $candidate = $null
    if ($Entry -is [string]) {
        $candidate = [string]$Entry
    } else {
        $candidate = [string](Get-ObjectPropertyValue -InputObject $Entry -Names @(
            "path",
            "source_path",
            "artifact_path",
            "local_path",
            "file_path",
            "relative_path",
            "name",
            "file",
            "filename"
        ))
    }

    if ([string]::IsNullOrWhiteSpace($candidate)) {
        return $null
    }

    if ([System.IO.Path]::IsPathRooted($candidate)) {
        return $candidate
    }

    if ([string]::IsNullOrWhiteSpace($ArtifactDir)) {
        return $candidate
    }

    if ([string]::IsNullOrWhiteSpace($ArtifactSubdir)) {
        return (Join-Path $ArtifactDir $candidate)
    }

    return (Join-Path (Join-Path $ArtifactDir $ArtifactSubdir) $candidate)
}

function Resolve-InstrumentationSummaryArtifactPath {
    param(
        [object]$Summary,
        [string]$ArtifactSubdir,
        [string[]]$PathPropertyNames,
        [string[]]$ListPropertyNames
    )

    if ($null -eq $Summary) {
        return $null
    }

    $artifactDir = [string](Get-ObjectPropertyValue -InputObject $Summary -Names @("artifact_dir", "artifact_root"))

    $explicitEntry = Get-ObjectPropertyValue -InputObject $Summary -Names $PathPropertyNames
    $explicitPath = Resolve-InstrumentationArtifactEntryPath -Entry $explicitEntry -ArtifactDir $artifactDir -ArtifactSubdir $ArtifactSubdir
    if (-not [string]::IsNullOrWhiteSpace($explicitPath)) {
        return $explicitPath
    }

    foreach ($listName in $ListPropertyNames) {
        $entries = Get-ObjectPropertyValue -InputObject $Summary -Names @($listName)
        if ($null -eq $entries) {
            continue
        }

        foreach ($entry in @($entries)) {
            $resolvedPath = Resolve-InstrumentationArtifactEntryPath -Entry $entry -ArtifactDir $artifactDir -ArtifactSubdir $ArtifactSubdir
            if (-not [string]::IsNullOrWhiteSpace($resolvedPath)) {
                return $resolvedPath
            }
        }
    }

    return $null
}

function Get-InstrumentationFirstScreenshotFacts {
    param([object]$Summary)

    if ($null -eq $Summary) {
        return $null
    }

    $artifactFacts = Get-ObjectPropertyValue -InputObject $Summary -Names @("artifact_facts")
    if ($null -eq $artifactFacts) {
        return $null
    }

    return Get-ObjectPropertyValue -InputObject $artifactFacts -Names @(
        "first_screenshot",
        "primary_screenshot",
        "screenshot"
    )
}

function Get-InstrumentationSummaryDimensions {
    param([object]$Summary)

    if ($null -eq $Summary) {
        return $null
    }

    $dimensionContainer = Get-ObjectPropertyValue -InputObject $Summary -Names @(
        "primary_screenshot_dimensions",
        "screenshot_dimensions",
        "captured_dimensions",
        "display_dimensions"
    )

    $width = $null
    $height = $null
    if ($null -ne $dimensionContainer) {
        $width = Get-ObjectPropertyValue -InputObject $dimensionContainer -Names @("width", "screenshot_width", "display_width")
        $height = Get-ObjectPropertyValue -InputObject $dimensionContainer -Names @("height", "screenshot_height", "display_height")
    }

    if ($null -eq $width) {
        $width = Get-ObjectPropertyValue -InputObject $Summary -Names @(
            "primary_screenshot_width",
            "screenshot_width",
            "display_width"
        )
    }
    if ($null -eq $height) {
        $height = Get-ObjectPropertyValue -InputObject $Summary -Names @(
            "primary_screenshot_height",
            "screenshot_height",
            "display_height"
        )
    }

    if ($null -eq $width -or $null -eq $height) {
        $firstScreenshotFacts = Get-InstrumentationFirstScreenshotFacts -Summary $Summary
        $nestedDimensions = Get-ObjectPropertyValue -InputObject $firstScreenshotFacts -Names @(
            "dimensions_px",
            "dimensions",
            "captured_dimensions",
            "display_dimensions"
        )
        if ($null -ne $nestedDimensions) {
            if ($null -eq $width) {
                $width = Get-ObjectPropertyValue -InputObject $nestedDimensions -Names @("width", "screenshot_width", "display_width")
            }
            if ($null -eq $height) {
                $height = Get-ObjectPropertyValue -InputObject $nestedDimensions -Names @("height", "screenshot_height", "display_height")
            }
        }
    }

    $parsedWidth = 0
    $parsedHeight = 0
    if ($null -eq $width -or $null -eq $height) {
        return $null
    }
    if (-not [int]::TryParse([string]$width, [ref]$parsedWidth)) {
        return $null
    }
    if (-not [int]::TryParse([string]$height, [ref]$parsedHeight)) {
        return $null
    }

    return [pscustomobject]@{
        width = $parsedWidth
        height = $parsedHeight
    }
}

function Get-InstrumentationOrientationAssessment {
    param(
        [object]$Summary,
        [ValidateSet("portrait", "landscape")]
        [string]$ExpectedOrientation,
        [string]$ScreenshotPath
    )

    if ($null -eq $Summary -or -not (Test-Path -LiteralPath $ScreenshotPath)) {
        return [pscustomobject]@{
            handled = $false
            passed = $false
            message = $null
        }
    }

    $explicitMatch = Get-ObjectPropertyValue -InputObject $Summary -Names @(
        "orientation_matches_expected",
        "orientation_match",
        "primary_screenshot_matches_expected_orientation",
        "primary_screenshot_orientation_matches",
        "screenshot_matches_expected_orientation",
        "artifacts_match_requested_orientation"
    )
    if (-not ($explicitMatch -is [bool])) {
        $firstScreenshotFacts = Get-InstrumentationFirstScreenshotFacts -Summary $Summary
        $explicitMatch = Get-ObjectPropertyValue -InputObject $firstScreenshotFacts -Names @(
            "orientation_matches_requested",
            "orientation_matches_expected",
            "orientation_match",
            "matches_requested_orientation"
        )
    }
    if ($explicitMatch -is [bool]) {
        if ($explicitMatch) {
            return [pscustomobject]@{
                handled = $true
                passed = $true
                message = $null
            }
        }

        return [pscustomobject]@{
            handled = $true
            passed = $false
            message = "Instrumentation summary reported screenshot orientation mismatch for $ScreenshotPath"
        }
    }

    $actualOrientation = [string](Get-ObjectPropertyValue -InputObject $Summary -Names @(
        "primary_screenshot_orientation",
        "screenshot_orientation",
        "captured_orientation",
        "resolved_orientation"
    ))
    if ([string]::IsNullOrWhiteSpace($actualOrientation)) {
        $firstScreenshotFacts = Get-InstrumentationFirstScreenshotFacts -Summary $Summary
        $actualOrientation = [string](Get-ObjectPropertyValue -InputObject $firstScreenshotFacts -Names @(
            "actual_orientation",
            "primary_screenshot_orientation",
            "screenshot_orientation",
            "captured_orientation",
            "resolved_orientation"
        ))
    }
    if (-not [string]::IsNullOrWhiteSpace($actualOrientation)) {
        $normalizedOrientation = $actualOrientation.Trim().ToLowerInvariant()
        if ($normalizedOrientation -in @("portrait", "landscape")) {
            return [pscustomobject]@{
                handled = $true
                passed = ($normalizedOrientation -eq $ExpectedOrientation)
                message = $(if ($normalizedOrientation -eq $ExpectedOrientation) {
                    $null
                } else {
                    "Instrumentation summary reported screenshot orientation '$normalizedOrientation' but expected '$ExpectedOrientation'"
                })
            }
        }
    }

    $dimensions = Get-InstrumentationSummaryDimensions -Summary $Summary
    if ($null -ne $dimensions) {
        $matchesExpectedOrientation = if ($ExpectedOrientation -eq "landscape") {
            $dimensions.width -gt $dimensions.height
        } else {
            $dimensions.height -gt $dimensions.width
        }

        return [pscustomobject]@{
            handled = $true
            passed = $matchesExpectedOrientation
            message = $(if ($matchesExpectedOrientation) {
                $null
            } else {
                "Instrumentation summary reported screenshot dimensions $($dimensions.width)x$($dimensions.height) but expected $ExpectedOrientation"
            })
        }
    }

    return [pscustomobject]@{
        handled = $false
        passed = $false
        message = $null
    }
}

function Invoke-PowerShellChildScript {
    param(
        [string[]]$Arguments,
        [int]$TimeoutSeconds = 0,
        [string]$LogPath = ""
    )

    $job = $null
    try {
        $job = Start-Job -ScriptBlock {
            param([string[]]$InnerArgs)
            & powershell -NoProfile -NonInteractive @InnerArgs 2>&1
            $code = $LASTEXITCODE
            Write-Output ("__SENKU_EXIT_CODE__={0}" -f $code)
        } -ArgumentList (, $Arguments)

        $completed = if ($TimeoutSeconds -gt 0) {
            Wait-Job -Id $job.Id -Timeout $TimeoutSeconds
        } else {
            Wait-Job -Id $job.Id
        }
        $timedOut = ($null -eq $completed)
        $timeoutEvidence = ""
        if ($timedOut) {
            $timeoutEvidence = "Child PowerShell job timed out after {0} seconds." -f $TimeoutSeconds
            Stop-Job -Id $job.Id -ErrorAction SilentlyContinue | Out-Null
        }

        $rawOutput = if ($job) {
            [string](Receive-Job -Id $job.Id -ErrorAction SilentlyContinue 2>&1 | Out-String)
        } else {
            ""
        }
        $exitCode = if ($timedOut) { -1 } else { 0 }
        $output = $rawOutput
        $exitMarker = [regex]::Match($rawOutput, '__SENKU_EXIT_CODE__=(\d+)')
        if ($exitMarker.Success) {
            $exitCode = [int]$exitMarker.Groups[1].Value
            $output = [regex]::Replace($rawOutput, '(?m)^\s*__SENKU_EXIT_CODE__=\d+\s*$', '').Trim()
        }
        if ($timedOut -and -not [string]::IsNullOrWhiteSpace($timeoutEvidence)) {
            $output = (($output, $timeoutEvidence) | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) }) -join [Environment]::NewLine
        }
        if (-not [string]::IsNullOrWhiteSpace($LogPath)) {
            $parent = Split-Path -Parent $LogPath
            if ($parent) {
                New-Item -ItemType Directory -Force -Path $parent | Out-Null
            }
            $output | Set-Content -LiteralPath $LogPath -Encoding UTF8
        }

        return [pscustomobject]@{
            timed_out = $timedOut
            exit_code = $exitCode
            timeout_seconds = [int]$TimeoutSeconds
            timeout_evidence = $timeoutEvidence
            output = $output
            stdout_path = $null
            stderr_path = $null
        }
    } finally {
        if ($job -and (Get-Job -Id $job.Id -ErrorAction SilentlyContinue)) {
            Remove-Job -Id $job.Id -Force -ErrorAction SilentlyContinue | Out-Null
        }
    }
}

function Set-DeviceOrientation {
    param(
        [string]$Device,
        [ValidateSet("portrait", "landscape")]
        [string]$TargetOrientation
    )

    $deviceFacts = Resolve-DeviceFacts -Device $Device -RequestedOrientation $TargetOrientation
    $physicalSize = $deviceFacts.physical_size_px
    if ($null -ne $physicalSize) {
        $deviceIsLandscapeNative = $physicalSize.width -gt $physicalSize.height
        $rotation = if ($TargetOrientation -eq "landscape") {
            if ($deviceIsLandscapeNative) { 0 } else { 1 }
        } else {
            if ($deviceIsLandscapeNative) { 1 } else { 0 }
        }
    } else {
        $rotation = if ($TargetOrientation -eq "landscape") { 1 } else { 0 }
    }
    & $adb -s $Device shell settings put system accelerometer_rotation 0 | Out-Null
    & $adb -s $Device shell settings put system user_rotation $rotation | Out-Null
    Start-Sleep -Milliseconds 800
}

function Get-DeviceOrientationSettings {
    param([string]$Device)

    $accelerometer = (& $adb -s $Device shell settings get system accelerometer_rotation 2>$null | Out-String).Trim()
    $userRotation = (& $adb -s $Device shell settings get system user_rotation 2>$null | Out-String).Trim()
    return [pscustomobject]@{
        accelerometer_rotation = $(if ([string]::IsNullOrWhiteSpace($accelerometer)) { "1" } else { $accelerometer })
        user_rotation = $(if ([string]::IsNullOrWhiteSpace($userRotation)) { "0" } else { $userRotation })
    }
}

function Restore-DeviceOrientationSettings {
    param(
        [string]$Device,
        [object]$State
    )

    if ($null -eq $State) {
        return
    }

    & $adb -s $Device shell settings put system user_rotation $State.user_rotation | Out-Null
    & $adb -s $Device shell settings put system accelerometer_rotation $State.accelerometer_rotation | Out-Null
    Start-Sleep -Milliseconds 500
}

function Get-DeviceRotation {
    param([string]$Device)

    return Get-AndroidCurrentRotation -AdbPath $adb -DeviceName $Device
}

function Resolve-DeviceFacts {
    param(
        [string]$Device,
        [ValidateSet("portrait", "landscape")]
        [string]$RequestedOrientation
    )

    return Resolve-AndroidDeviceFacts -AdbPath $adb -DeviceName $Device -RequestedOrientation $RequestedOrientation
}

function Wait-ForDeviceOrientation {
    param(
        [string]$Device,
        [ValidateSet("portrait", "landscape")]
        [string]$TargetOrientation
    )

    $deviceFacts = Resolve-DeviceFacts -Device $Device -RequestedOrientation $TargetOrientation
    $expectedRotation = $deviceFacts.expected_rotation
    for ($attempt = 0; $attempt -lt 6; $attempt++) {
        $currentRotation = Get-DeviceRotation -Device $Device
        if ($currentRotation -eq $expectedRotation) {
            return $true
        }
        Start-Sleep -Milliseconds 500
    }

    return $false
}

function Set-DeviceSizeOverride {
    param(
        [string]$Device,
        [int]$Width,
        [int]$Height
    )

    if ($Device -notlike "emulator-*") {
        throw "Refusing wm size override on physical device '$Device'; retry orientation capture without display-size mutation."
    }

    & $adb -s $Device shell wm size "$Width`x$Height" | Out-Null
    Start-Sleep -Milliseconds 700
}

function Get-DeviceFontScale {
    param([string]$Device)

    $raw = (& $adb -s $Device shell settings get system font_scale 2>$null | Out-String).Trim()
    $parsed = 1.0
    if ([double]::TryParse($raw, [System.Globalization.NumberStyles]::Float, [System.Globalization.CultureInfo]::InvariantCulture, [ref]$parsed)) {
        return [Math]::Max(0.5, $parsed)
    }
    return 1.0
}

function Set-DeviceFontScale {
    param(
        [string]$Device,
        [double]$Scale
    )

    $safeScale = [Math]::Max(0.5, [Math]::Min(2.0, $Scale))
    $serialized = $safeScale.ToString("0.##", [System.Globalization.CultureInfo]::InvariantCulture)
    & $adb -s $Device shell settings put system font_scale $serialized | Out-Null
    Start-Sleep -Milliseconds 700
}

function Reset-DeviceSizeOverride {
    param([string]$Device)

    if ($Device -notlike "emulator-*") {
        Write-Warning "Skipping wm size reset on physical device '$Device'."
        return
    }

    & $adb -s $Device shell wm size reset | Out-Null
    Start-Sleep -Milliseconds 700
}

function Test-DeviceReady {
    param([string]$Device)

    $state = (& $adb -s $Device get-state 2>$null | Out-String).Trim().ToLowerInvariant()
    return $state -eq "device"
}

function Invoke-AdbCommandCapture {
    param(
        [string[]]$Arguments
    )

    return Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $Arguments
}

function Install-SenkuApk {
    param(
        [string]$Device,
        [string]$Apk
    )

    $installResult = Invoke-AdbCommandCapture -Arguments @("-s", $Device, "install", "-r", $Apk)
    if ($installResult.exit_code -eq 0) {
        return $true
    }

    $installOutput = $installResult.output
    if ($Device -notlike "emulator-*") {
        Write-Warning ("[{0}] APK install failed; retrying with adb install --no-streaming on physical device." -f $Device)
        $noStreamingResult = Invoke-AdbCommandCapture -Arguments @("-s", $Device, "install", "--no-streaming", "-r", $Apk)
        if ($noStreamingResult.exit_code -eq 0) {
            return $true
        }
        $installOutput = "{0}; --no-streaming retry failed: {1}" -f $installOutput, $noStreamingResult.output
    }

    $storagePressureMarkers = @(
        "not enough space",
        "install_failed_insufficient_storage",
        "insufficient storage"
    )
    foreach ($marker in $storagePressureMarkers) {
        if ($installOutput.ToLowerInvariant().Contains($marker)) {
            Write-Warning "[$Device] APK install hit storage pressure; retrying after uninstall."
            [void](Invoke-AdbCommandCapture -Arguments @("-s", $Device, "uninstall", "com.senku.mobile"))
            Start-Sleep -Seconds 1
            $retryResult = Invoke-AdbCommandCapture -Arguments @("-s", $Device, "install", "-r", $Apk)
            if ($retryResult.exit_code -eq 0) {
                return $true
            }
            throw "APK install failed after uninstall on ${Device}: $($retryResult.output)"
        }
    }

    throw "APK install failed on ${Device}: $installOutput"
}

function Write-ZipBundle {
    param(
        [string]$SourceDirectory,
        [string]$DestinationZip
    )

    return Write-AndroidHarnessZipBundle -SourceDirectory $SourceDirectory -DestinationZip $DestinationZip
}

$stamp = Get-Date -Format "yyyyMMdd_HHmmss_fff"
$outputDir = Join-Path $repoRoot (Join-Path $OutputRoot $stamp)
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
$runStartedUtc = (Get-Date).ToUniversalTime()
$runStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

$results = New-Object System.Collections.Generic.List[object]

if ($resolvedInstrumentationExecution) {
    Write-Host "Preparing instrumentation build artifacts once for validation pack..."
    $buildPrepArgs = @(
        "-ExecutionPolicy", "Bypass",
        "-File", $instrumentedSmokeScript,
        "-BuildOnly"
    )
    $buildPrepResult = Invoke-PowerShellChildScript -Arguments $buildPrepArgs
    if (-not [string]::IsNullOrWhiteSpace($buildPrepResult.output)) {
        Write-Host $buildPrepResult.output
    }
    $preparedAppApk = Join-Path $repoRoot "android-app\\app\\build\\outputs\\apk\\debug\\app-debug.apk"
    $preparedTestApk = Join-Path $repoRoot "android-app\\app\\build\\outputs\\apk\\androidTest\\debug\\app-debug-androidTest.apk"
    if (($buildPrepResult.timed_out) -or
        (-not (Test-Path -LiteralPath $preparedAppApk)) -or
        (-not (Test-Path -LiteralPath $preparedTestApk))) {
        throw "Instrumentation build preparation failed"
    }
}

foreach ($device in $normalizedDevices) {
    $deviceLock = Acquire-DeviceLock -DeviceName $device
    try {
        $deviceDir = Join-Path $outputDir $device
        New-Item -ItemType Directory -Force -Path $deviceDir | Out-Null
        $originalFontScale = Get-DeviceFontScale -Device $device
        $originalOrientationSettings = Get-DeviceOrientationSettings -Device $device
        $fontScaleApplied = $false
        $instrumentationInstallReady = $false

        $deviceInstallError = ""
        if ($InstallApk) {
            Write-Host "Installing APK on $device..."
            try {
                Install-SenkuApk -Device $device -Apk $resolvedApkPath | Out-Null
            } catch {
                $deviceInstallError = $_.Exception.Message
                Write-Warning "[$device] $deviceInstallError"
            }
        }

        if (-not [string]::IsNullOrWhiteSpace($deviceInstallError)) {
            foreach ($case in $cases) {
                $caseId = $case.id
                $caseDir = Join-Path $deviceDir $caseId
                New-Item -ItemType Directory -Force -Path $caseDir | Out-Null

                $dumpPath = Join-Path $caseDir "dump.xml"
                $logPath = Join-Path $caseDir "logcat.txt"
                $screenPath = Join-Path $caseDir "screen.png"
                $results.Add([pscustomobject]@{
                    device = $device
                    case_id = $caseId
                    query = $case.query
                    expected_mode_hint = $case.expected_mode_hint
                    expected_orientation = $Orientation
                    font_scale = $FontScale
                    detected_mode = "unknown"
                    instrumentation_status = $null
                    elapsed_ms = 0
                    status = "install_failure"
                    has_detail_body = $false
                    has_followup_input = $false
                    screen_meta = ""
                    screen_title = ""
                    body_preview = ""
                    run_error = $deviceInstallError
                    artifact_dir = $caseDir
                    dump_path = $dumpPath
                    logcat_path = $logPath
                    screenshot_path = $screenPath
                })
            }
            continue
        }

        if ([Math]::Abs($FontScale - $originalFontScale) -ge 0.01) {
            Set-DeviceFontScale -Device $device -Scale $FontScale
            $fontScaleApplied = $true
        }

        if ($UseInstrumentationPreflight -and -not $resolvedInstrumentationExecution) {
            $instrumentationArtifactRoot = Join-Path $OutputRoot (Join-Path $stamp "instrumentation_preflight")
            $preflightArgs = @(
                "-ExecutionPolicy", "Bypass",
                "-File", $instrumentedSmokeScript,
                "-Device", $device,
                "-ArtifactRoot", $instrumentationArtifactRoot,
                "-SmokeProfile", $(if ($InferenceMode -eq "host") { "host" } else { "basic" }),
                "-SkipBuild",
                "-SkipDeviceLock",
                "-Orientation", $Orientation,
                "-FontScale", "$FontScale",
                "-CaptureLogcat",
                "-ClearLogcatBeforeRun"
            )
            if ($InferenceMode -eq "host") {
                $preflightArgs += @(
                    "-EnableHostInferenceSmoke",
                    "-HostInferenceUrl", $HostInferenceUrl,
                    "-HostInferenceModel", $HostInferenceModel
                )
            }
            Write-Host "[$device] Running instrumentation preflight..."
            $preflightResult = Invoke-PowerShellChildScript -Arguments $preflightArgs
            if (-not [string]::IsNullOrWhiteSpace($preflightResult.output)) {
                Write-Host $preflightResult.output
            }
            if ($preflightResult.exit_code -ne 0) {
                throw "Instrumentation preflight failed on $device"
            }
        }

        foreach ($case in $cases) {
            $caseId = $case.id
            $query = $case.query
            $caseDir = Join-Path $deviceDir $caseId
            New-Item -ItemType Directory -Force -Path $caseDir | Out-Null

            $dumpPath = Join-Path $caseDir "dump.xml"
            $logPath = Join-Path $caseDir "logcat.txt"
            $screenPath = Join-Path $caseDir "screen.png"
            $effectiveMaxWaitSeconds = $MaxWaitSeconds
            $effectiveInferenceMode = $InferenceMode
            $prelaunchSizeOverride = $false
            $instrumentationSummary = $null
            $instrumentationStatus = $null
            if ($device -like "emulator-*" -and [string]$case.expected_mode_hint -eq "generated" -and $effectiveInferenceMode -eq "preserve") {
                $effectiveInferenceMode = "host"
            }
            if ($device -like "emulator-*" -and [string]$case.expected_mode_hint -eq "generated" -and $effectiveInferenceMode -ne "local") {
                $effectiveMaxWaitSeconds = [Math]::Max($effectiveMaxWaitSeconds, 540)
            }

            Set-DeviceOrientation -Device $device -TargetOrientation $Orientation
            if ($Orientation -eq "landscape" -and $device -like "emulator-*") {
                $deviceFacts = Resolve-DeviceFacts -Device $device -RequestedOrientation $Orientation
                $physicalSize = $deviceFacts.physical_size_px
                if ($null -ne $physicalSize -and $physicalSize.width -lt $physicalSize.height) {
                    Set-DeviceSizeOverride -Device $device -Width $physicalSize.height -Height $physicalSize.width
                    $prelaunchSizeOverride = $true
                }
            }
            Write-Host "[$device][$caseId] Running prompt..."
            $sw = [System.Diagnostics.Stopwatch]::StartNew()
            $runError = ""
            $caseRunnerLogPath = Join-Path $caseDir "runner.log"
            try {
                if (-not (Test-DeviceReady -Device $device)) {
                    throw "Device not ready: $device"
                }
                if ($resolvedInstrumentationExecution) {
                    $summaryPath = Join-Path $caseDir "instrumentation_summary.json"
                    $captureLabel = "validation_" + $caseId
                    $instrumentationArgs = @(
                        "-ExecutionPolicy", "Bypass",
                        "-File", $instrumentedSmokeScript,
                        "-Device", $device,
                        "-SmokeProfile", "custom",
                        "-SkipBuild",
                        "-SkipDeviceLock",
                        "-ScriptedQuery", $query,
                        "-ScriptedAsk",
                        "-ScriptedExpectedSurface", "detail",
                        "-ScriptedCaptureLabel", $captureLabel,
                        "-ScriptedTimeoutMs", ([Math]::Max(5000, $effectiveMaxWaitSeconds * 1000)).ToString(),
                        "-Orientation", $Orientation,
                        "-FontScale", "$FontScale",
                        "-CaptureLogcat",
                        "-ClearLogcatBeforeRun",
                        "-SummaryPath", $summaryPath
                    )
                    if (-not [string]::IsNullOrWhiteSpace([string]$case.followup_query)) {
                        $instrumentationArgs += @("-ScriptedFollowUpQuery", [string]$case.followup_query)
                    }
                    if ($instrumentationInstallReady) {
                        $instrumentationArgs += @("-SkipInstall")
                    }
                    if ($effectiveInferenceMode -eq "host") {
                        $instrumentationArgs += @(
                            "-EnableHostInferenceSmoke",
                            "-HostInferenceUrl", $HostInferenceUrl,
                            "-HostInferenceModel", $HostInferenceModel
                        )
                    }
                    $childTimeoutSec = [Math]::Max(150, $effectiveMaxWaitSeconds + 180)
                    $childResult = Invoke-PowerShellChildScript -Arguments $instrumentationArgs -TimeoutSeconds $childTimeoutSec -LogPath $caseRunnerLogPath
                    if (-not [string]::IsNullOrWhiteSpace($childResult.output)) {
                        Write-Host $childResult.output
                    }
                    if ($childResult.timed_out) {
                        throw "Instrumentation execution timed out for $caseId after $childTimeoutSec seconds"
                    }
                    if ($childResult.exit_code -ne 0) {
                        $childTail = [string]$childResult.output
                        if ($childTail.Length -gt 500) {
                            $childTail = $childTail.Substring($childTail.Length - 500)
                        }
                        if ([string]::IsNullOrWhiteSpace($childTail)) {
                            throw "Instrumentation execution failed for $caseId"
                        }
                        throw "Instrumentation execution failed for ${caseId}: $childTail"
                    }
                    if (-not (Test-Path -LiteralPath $summaryPath)) {
                        throw "Instrumentation execution did not produce summary.json for $caseId"
                    }
                    $instrumentationInstallReady = $true
                    $summary = Get-Content -LiteralPath $summaryPath -Raw | ConvertFrom-Json
                    $instrumentationSummary = $summary
                    $instrumentationStatus = [string]$summary.status
                    if ($summary.status -ne "pass" -and [string]::IsNullOrWhiteSpace($runError)) {
                        $runError = if ([string]::IsNullOrWhiteSpace([string]$summary.failure_reason)) {
                            "Instrumentation execution reported status '$([string]$summary.status)' for $caseId"
                        } else {
                            "Instrumentation execution reported status '$([string]$summary.status)' for ${caseId}: $([string]$summary.failure_reason)"
                        }
                    }
                    Copy-ArtifactIntoCase -SourcePath (Resolve-InstrumentationSummaryArtifactPath -Summary $summary -ArtifactSubdir "dumps" -PathPropertyNames @("primary_dump_path", "dump_path") -ListPropertyNames @("dump_paths", "dumps")) -DestinationPath $dumpPath
                    Copy-ArtifactIntoCase -SourcePath (Resolve-InstrumentationSummaryArtifactPath -Summary $summary -ArtifactSubdir "screenshots" -PathPropertyNames @("primary_screenshot_path", "screenshot_path") -ListPropertyNames @("screenshot_paths", "screenshots")) -DestinationPath $screenPath
                    Copy-ArtifactIntoCase -SourcePath (Resolve-InstrumentationSummaryArtifactPath -Summary $summary -ArtifactSubdir "" -PathPropertyNames @("primary_logcat_path", "logcat_path") -ListPropertyNames @("logcat_paths")) -DestinationPath $logPath
                } else {
                    $promptArgs = @(
                        "-ExecutionPolicy", "Bypass",
                        "-File", $runPromptScript,
                        "-Emulator", $device,
                        "-SkipDeviceLock",
                        "-Query", $query,
                        "-Ask",
                        "-WaitForCompletion",
                        "-MaxWaitSeconds", "$effectiveMaxWaitSeconds",
                        "-PollSeconds", "$PollSeconds",
                        "-WaitSeconds", "$WaitSeconds",
                        "-DumpPath", $dumpPath,
                        "-ClearLogcatBeforeLaunch",
                        "-LogcatPath", $logPath,
                        "-InferenceMode", $effectiveInferenceMode
                    )
                    if ($effectiveInferenceMode -eq "host") {
                        $promptArgs += @("-HostInferenceUrl", $HostInferenceUrl, "-HostInferenceModel", $HostInferenceModel)
                    }
                    if ($ForceShellExecution) {
                        $promptArgs += "-ForceShellExecution"
                    }
                    $jobTimeoutSec = [Math]::Max(90, $effectiveMaxWaitSeconds + 90)
                    $childResult = Invoke-PowerShellChildScript -Arguments $promptArgs -TimeoutSeconds $jobTimeoutSec -LogPath $caseRunnerLogPath
                    if (-not [string]::IsNullOrWhiteSpace($childResult.output)) {
                        Write-Host $childResult.output
                    }
                    if ($childResult.timed_out) {
                        throw "Timed out waiting for run_android_prompt.ps1 after $jobTimeoutSec seconds"
                    }
                    if ($childResult.exit_code -ne 0) {
                        $childTail = [string]$childResult.output
                        if ($childTail.Length -gt 500) {
                            $childTail = $childTail.Substring($childTail.Length - 500)
                        }
                        if ([string]::IsNullOrWhiteSpace($childTail)) {
                            throw "run_android_prompt.ps1 failed"
                        }
                        throw "run_android_prompt.ps1 failed: $childTail"
                    }
                }
            } catch {
                $runError = $_.Exception.Message
            } finally {
                $sw.Stop()
            }

            try {
                $usedSizeOverride = $false
                $instrumentationOrientationAssessment = Get-InstrumentationOrientationAssessment -Summary $instrumentationSummary -ExpectedOrientation $Orientation -ScreenshotPath $screenPath
                if ($instrumentationOrientationAssessment.handled) {
                    if (-not $instrumentationOrientationAssessment.passed) {
                        throw $instrumentationOrientationAssessment.message
                    }
                } else {
                    Set-DeviceOrientation -Device $device -TargetOrientation $Orientation
                    [void](Wait-ForDeviceOrientation -Device $device -TargetOrientation $Orientation)
                    Capture-Screenshot -Device $device -LocalPath $screenPath
                    $screenshotFacts = Get-AndroidScreenshotFacts -Path $screenPath -RequestedOrientation $Orientation
                    $dims = $screenshotFacts.dimensions_px
                    if ($null -eq $dims) {
                        throw "Could not read screenshot dimensions for $screenPath"
                    }
                    if ($Orientation -eq "landscape" -and $screenshotFacts.orientation_mismatch) {
                        Set-DeviceOrientation -Device $device -TargetOrientation $Orientation
                        [void](Wait-ForDeviceOrientation -Device $device -TargetOrientation $Orientation)
                        Capture-Screenshot -Device $device -LocalPath $screenPath
                        $screenshotFacts = Get-AndroidScreenshotFacts -Path $screenPath -RequestedOrientation $Orientation
                        $dims = $screenshotFacts.dimensions_px
                        if ($null -ne $dims -and $screenshotFacts.orientation_mismatch) {
                            $deviceFacts = Resolve-DeviceFacts -Device $device -RequestedOrientation $Orientation
                            $physicalSize = $deviceFacts.physical_size_px
                            if ($null -ne $physicalSize -and $physicalSize.width -lt $physicalSize.height) {
                                Set-DeviceSizeOverride -Device $device -Width $physicalSize.height -Height $physicalSize.width
                                $usedSizeOverride = $true
                                Capture-Screenshot -Device $device -LocalPath $screenPath
                                $screenshotFacts = Get-AndroidScreenshotFacts -Path $screenPath -RequestedOrientation $Orientation
                                $dims = $screenshotFacts.dimensions_px
                            }
                        }
                    }
                    if ($Orientation -eq "portrait" -and $screenshotFacts.orientation_mismatch) {
                        Set-DeviceOrientation -Device $device -TargetOrientation $Orientation
                        [void](Wait-ForDeviceOrientation -Device $device -TargetOrientation $Orientation)
                        Capture-Screenshot -Device $device -LocalPath $screenPath
                        $screenshotFacts = Get-AndroidScreenshotFacts -Path $screenPath -RequestedOrientation $Orientation
                        $dims = $screenshotFacts.dimensions_px
                        if ($null -ne $dims -and $screenshotFacts.orientation_mismatch) {
                            $deviceFacts = Resolve-DeviceFacts -Device $device -RequestedOrientation $Orientation
                            $physicalSize = $deviceFacts.physical_size_px
                            if ($null -ne $physicalSize -and $physicalSize.width -gt $physicalSize.height) {
                                Set-DeviceSizeOverride -Device $device -Width $physicalSize.height -Height $physicalSize.width
                                $usedSizeOverride = $true
                                Capture-Screenshot -Device $device -LocalPath $screenPath
                                $screenshotFacts = Get-AndroidScreenshotFacts -Path $screenPath -RequestedOrientation $Orientation
                                $dims = $screenshotFacts.dimensions_px
                            }
                        }
                    }
                    if ($Orientation -eq "landscape" -and -not ($dims.width -gt $dims.height)) {
                        throw "Screenshot orientation mismatch: expected landscape but captured $($dims.width)x$($dims.height)"
                    }
                    if ($Orientation -eq "portrait" -and -not ($dims.height -gt $dims.width)) {
                        throw "Screenshot orientation mismatch: expected portrait but captured $($dims.width)x$($dims.height)"
                    }
                }
            } catch {
                if ([string]::IsNullOrWhiteSpace($runError)) {
                    $runError = "Screenshot capture failed: $($_.Exception.Message)"
                }
            } finally {
                if ($usedSizeOverride -or $prelaunchSizeOverride) {
                    Reset-DeviceSizeOverride -Device $device
                }
            }

            $dumpText = ""
            if (Test-Path -LiteralPath $dumpPath) {
                $dumpText = Get-Content -LiteralPath $dumpPath -Raw
            }

            $meta = Get-UiFieldText -XmlText $dumpText -ResourceId "com.senku.mobile:id/detail_screen_meta"
            if ([string]::IsNullOrWhiteSpace($meta)) {
                $meta = Get-ComposeDetailMetaText -XmlText $dumpText
            }
            $title = Get-UiFieldText -XmlText $dumpText -ResourceId "com.senku.mobile:id/detail_screen_title"
            if ([string]::IsNullOrWhiteSpace($title)) {
                $title = Get-ComposeDetailTitleText -XmlText $dumpText
            }
            $bodyLabel = Get-UiFieldText -XmlText $dumpText -ResourceId "com.senku.mobile:id/detail_body_label"
            $body = Get-UiFieldText -XmlText $dumpText -ResourceId "com.senku.mobile:id/detail_body"
            if ([string]::IsNullOrWhiteSpace($body)) {
                $body = Get-ComposeDetailBodyText -XmlText $dumpText
            }
            if ([string]::IsNullOrWhiteSpace($body) -and -not [string]::IsNullOrWhiteSpace($bodyLabel)) {
                $body = $bodyLabel
            }

            $hasDetailBody = -not [string]::IsNullOrWhiteSpace($body)
            $hasFollowUp = $dumpText.Contains('resource-id="com.senku.mobile:id/detail_followup_input"') -or (Test-ComposeFollowUpVisible -XmlText $dumpText)
            $metaLower = $meta.ToLowerInvariant()
            $bodyLabelLower = $bodyLabel.ToLowerInvariant()
            $modeDetected = if ($metaLower.Contains("deterministic") -or $metaLower.Contains("instant") -or $bodyLabelLower.Contains("deterministic") -or $bodyLabelLower.Contains("instant")) {
                "deterministic"
            } elseif ($metaLower.Contains("ai answer") -or $metaLower.Contains("generated") -or $bodyLabelLower.Contains("ai answer") -or $bodyLabelLower.Contains("generated")) {
                "generated"
            } else {
                "unknown"
            }
            $status = if ([string]::IsNullOrWhiteSpace($runError) -and $hasDetailBody) { "pass" } elseif (-not [string]::IsNullOrWhiteSpace($runError)) { "runtime_failure" } else { "weak" }

            $results.Add([pscustomobject]@{
                device = $device
                case_id = $caseId
                query = $query
                followup_query = [string]$case.followup_query
                expected_mode_hint = $case.expected_mode_hint
                expected_orientation = $Orientation
                font_scale = $FontScale
                detected_mode = $modeDetected
                instrumentation_status = $instrumentationStatus
                elapsed_ms = [int]$sw.ElapsedMilliseconds
                status = $status
                has_detail_body = $hasDetailBody
                has_followup_input = $hasFollowUp
                screen_meta = $meta
                screen_title = $title
                body_preview = if ($body.Length -gt 220) { $body.Substring(0, 220) + "..." } else { $body }
                run_error = $runError
                artifact_dir = $caseDir
                dump_path = $dumpPath
                logcat_path = $logPath
                screenshot_path = $screenPath
            })
            Write-Host ("[{0}][{1}] {2} in {3} ms" -f $device, $caseId, $status.ToUpperInvariant(), [int]$sw.ElapsedMilliseconds)
        }
    } finally {
        try {
            if ($fontScaleApplied) {
                Set-DeviceFontScale -Device $device -Scale $originalFontScale
            }
            Restore-DeviceOrientationSettings -Device $device -State $originalOrientationSettings
        } finally {
            if ($deviceLock) {
                $deviceLock.Dispose()
            }
        }
    }
}

$summaryJson = Join-Path $outputDir "summary.json"
$resultsJson = Join-Path $outputDir "results.json"
$summaryCsv = Join-Path $outputDir "summary.csv"
$results | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $resultsJson -Encoding UTF8
$results | Export-Csv -LiteralPath $summaryCsv -NoTypeInformation -Encoding UTF8

$passCount = @($results | Where-Object { $_.status -eq "pass" }).Count
$installFailureCount = @($results | Where-Object { $_.status -eq "install_failure" }).Count
$totalCount = $results.Count
$failCount = @($results | Where-Object { $_.status -ne "pass" }).Count
$runStopwatch.Stop()
$runCompletedUtc = (Get-Date).ToUniversalTime()
$overallStatus = if ($failCount -eq 0) { "pass" } elseif ($passCount -gt 0) { "partial" } else { "fail" }
$bundleZip = Write-ZipBundle -SourceDirectory $outputDir -DestinationZip (Join-Path (Split-Path -Parent $outputDir) ($stamp + "_bundle.zip"))
$summaryManifest = [pscustomobject]@{
    run_started_utc = $runStartedUtc.ToString("o")
    run_completed_utc = $runCompletedUtc.ToString("o")
    duration_ms = [int]$runStopwatch.ElapsedMilliseconds
    output_dir = $outputDir
    artifact_bundle_zip = $bundleZip
    device_count = $normalizedDevices.Count
    case_count = $totalCount
    pass_count = $passCount
    fail_count = $failCount
    install_failure_count = $installFailureCount
    status = $overallStatus
    inference_mode = $InferenceMode
    orientation = $Orientation
    font_scale = $FontScale
    used_instrumentation_execution = [bool]$resolvedInstrumentationExecution
    used_instrumentation_preflight = [bool]$UseInstrumentationPreflight
    include_followup_case = [bool]$IncludeFollowUpCase
    results_json = $resultsJson
    results_csv = $summaryCsv
}
($summaryManifest | ConvertTo-Json -Depth 6) | Set-Content -LiteralPath $summaryJson -Encoding UTF8

Write-Host ""
Write-Host "UI validation pack complete."
Write-Host "Output: $outputDir"
Write-Host "Pass: $passCount / $totalCount"
if ($installFailureCount -gt 0) {
    Write-Warning "Install failures recorded for $installFailureCount case(s). See summary.csv for per-device details."
}
Write-Host "Summary JSON: $summaryJson"
Write-Host "Results JSON: $resultsJson"
Write-Host "Summary CSV: $summaryCsv"
Write-Host "Artifact bundle: $bundleZip"

if ($overallStatus -ne "pass") {
    exit 1
}

exit 0
