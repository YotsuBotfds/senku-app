param(
    [string]$OutputDir = "artifacts\bench\android_physical_phone_smoke",
    [switch]$DryRun,
    [string]$Serial,
    [string]$ApkPath = "android-app\app\build\outputs\apk\debug\app-debug.apk",
    [string]$AdbPath,
    [string]$FocusPath,
    [string]$ScreenshotPath,
    [string]$DumpPath,
    [string]$LogcatPath,
    [string[]]$RequiredText = @(),
    [switch]$Interact,
    [string]$InteractionQuery = "water filter charcoal sand"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$packageName = "com.senku.mobile"
$launchActivity = "com.senku.mobile/.MainActivity"

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function Convert-ToRepoRelativePath {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }

    $resolvedPath = [System.IO.Path]::GetFullPath($Path)
    $resolvedRoot = [System.IO.Path]::GetFullPath($repoRoot)
    if ($resolvedPath.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $resolvedPath.Substring($resolvedRoot.Length).TrimStart("\", "/") -replace "\\", "/"
    }
    return $resolvedPath
}

function Get-DefaultAdbPath {
    if ([string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        throw "LOCALAPPDATA is not set; pass -AdbPath explicitly."
    }

    return (Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe")
}

function Assert-PhysicalSerial {
    param([string]$DeviceSerial)

    if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
        throw "Serial is required unless -DryRun is set. Pass -Serial for the attached physical phone."
    }

    if ($DeviceSerial -match '^emulator-\d+$') {
        throw "Refusing emulator serial '$DeviceSerial'. This runner only targets a physical phone."
    }
}

function Invoke-AdbCapture {
    param(
        [string]$ResolvedAdbPath,
        [string[]]$Arguments
    )

    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $output = & $ResolvedAdbPath @Arguments 2>&1
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $text = ($output | ForEach-Object { "$_" }) -join [Environment]::NewLine
    if ($exitCode -ne 0) {
        throw "adb command failed ($exitCode): adb $($Arguments -join ' ')`n$text"
    }
    return $text
}

function Invoke-AdbChecked {
    param(
        [string]$ResolvedAdbPath,
        [string[]]$Arguments
    )

    Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments $Arguments | Out-Null
}

function Get-AdbDeviceMap {
    param([string]$ResolvedAdbPath)

    $devicesText = Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("devices")
    $devices = @{}
    foreach ($line in ($devicesText -split "`r?`n")) {
        if ($line -match '^(?<serial>\S+)\s+(?<state>device|offline|unauthorized|recovery|sideload|no permissions)') {
            $devices[$Matches["serial"]] = $Matches["state"]
        }
    }
    return $devices
}

function Assert-SerialIsConnectedPhysicalDevice {
    param(
        [string]$ResolvedAdbPath,
        [string]$DeviceSerial
    )

    $devices = Get-AdbDeviceMap -ResolvedAdbPath $ResolvedAdbPath
    if (-not $devices.ContainsKey($DeviceSerial)) {
        throw "Serial '$DeviceSerial' is not present in adb devices output."
    }

    if ($devices[$DeviceSerial] -ne "device") {
        throw "Serial '$DeviceSerial' is connected as '$($devices[$DeviceSerial])', not 'device'."
    }

    $qemu = Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "getprop", "ro.kernel.qemu")
    $bootQemu = Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "getprop", "ro.boot.qemu")
    if (($qemu.Trim() -eq "1") -or ($bootQemu.Trim() -eq "1")) {
        throw "Serial '$DeviceSerial' resolves to an emulator/qemu device. Refusing to run physical-phone smoke."
    }
}

function Write-Utf8Text {
    param(
        [string]$Path,
        [string]$Content
    )

    $parent = Split-Path -Parent $Path
    if (-not [string]::IsNullOrWhiteSpace($parent)) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    Set-Content -Path $Path -Value $Content -Encoding UTF8
}

function Write-JsonFile {
    param(
        [string]$Path,
        $Value
    )

    $Value | ConvertTo-Json -Depth 8 | Set-Content -Path $Path -Encoding UTF8
}

function Get-FileSha256 {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) {
        return $null
    }

    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Get-FocusedPackage {
    param([string]$Text)

    if ([string]::IsNullOrWhiteSpace($Text)) {
        return $null
    }

    foreach ($pattern in @(
            'mCurrentFocus=.*\s(?<package>[A-Za-z0-9_]+(?:\.[A-Za-z0-9_]+)+)/',
            'mFocusedApp=.*\s(?<package>[A-Za-z0-9_]+(?:\.[A-Za-z0-9_]+)+)/',
            'topResumedActivity=.*(?<package>[A-Za-z0-9_]+(?:\.[A-Za-z0-9_]+)+)/',
            'ResumedActivity:.*(?<package>[A-Za-z0-9_]+(?:\.[A-Za-z0-9_]+)+)/'
        )) {
        $match = [regex]::Match($Text, $pattern)
        if ($match.Success) {
            return $match.Groups["package"].Value
        }
    }

    return $null
}

function Get-OrientationEvidence {
    param([string]$Text)

    if ([string]::IsNullOrWhiteSpace($Text)) {
        return [ordered]@{
            source = "dumpsys input"
            raw = $null
            rotation = $null
            orientation = $null
        }
    }

    $rotation = $null
    if ($Text -match 'SurfaceOrientation:\s*(?<rotation>\d+)') {
        $rotation = [int]$Matches["rotation"]
    } elseif ($Text -match 'orientation=(?<rotation>\d+)') {
        $rotation = [int]$Matches["rotation"]
    }

    $orientation = switch ($rotation) {
        0 { "portrait" }
        1 { "landscape" }
        2 { "reverse_portrait" }
        3 { "reverse_landscape" }
        default { $null }
    }

    return [ordered]@{
        source = "dumpsys input"
        raw = $Text.Trim()
        rotation = $rotation
        orientation = $orientation
    }
}

function Get-PhysicalDeviceIdentity {
    param(
        [string]$ResolvedAdbPath,
        [string]$DeviceSerial
    )

    $props = [ordered]@{}
    foreach ($propName in @(
            "ro.product.manufacturer",
            "ro.product.model",
            "ro.product.device",
            "ro.product.name",
            "ro.build.fingerprint",
            "ro.build.version.sdk"
        )) {
        $props[$propName] = (Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "getprop", $propName)).Trim()
    }

    return [ordered]@{
        serial = $DeviceSerial
        manufacturer = $props["ro.product.manufacturer"]
        model = $props["ro.product.model"]
        device = $props["ro.product.device"]
        product = $props["ro.product.name"]
        build_fingerprint = $props["ro.build.fingerprint"]
        sdk = $props["ro.build.version.sdk"]
    }
}

function Write-SummaryMarkdown {
    param(
        [string]$Path,
        $Summary
    )

    $lines = @(
        "# Android Physical Phone Smoke",
        "",
        "- status: $($Summary.status)",
        "- dry_run: $($Summary.dry_run)",
        "- physical_device: $($Summary.physical_device)",
        "- launches_emulators: $($Summary.launches_emulators)",
        "- serial: $($Summary.serial)",
        "- adb_path: ``$($Summary.adb_path)``",
        "- apk_path: ``$($Summary.apk_path)``",
        "- apk_sha256: $($Summary.apk_sha256)",
        "- package: $($Summary.package)",
        "- launch_activity: $($Summary.launch_activity)",
        "- focused_package: $($Summary.evidence.focused_package)",
        "- orientation: $($Summary.evidence.orientation.orientation)",
        "- install_command: ``$($Summary.commands.install)``",
        "- launch_command: ``$($Summary.commands.launch)``",
        "- focus_path: ``$($Summary.evidence.focus_path)``",
        "- focus_sha256: $($Summary.evidence.artifact_hashes.focus_sha256)",
        "- screenshot_path: ``$($Summary.evidence.screenshot_path)``",
        "- screenshot_sha256: $($Summary.evidence.artifact_hashes.screenshot_sha256)",
        "- dump_path: ``$($Summary.evidence.dump_path)``",
        "- dump_sha256: $($Summary.evidence.artifact_hashes.dump_sha256)",
        "- logcat_path: ``$($Summary.evidence.logcat_path)``",
        "- logcat_sha256: $($Summary.evidence.artifact_hashes.logcat_sha256)",
        "- summary_json: ``$($Summary.summary_json)``"
    )
    if ($null -ne $Summary.interaction) {
        $lines += @(
            "- interaction_enabled: $($Summary.interaction.enabled)",
            "- interaction_query: $($Summary.interaction.query)"
        )
        foreach ($step in $Summary.interaction.steps) {
            $message = if ([string]::IsNullOrWhiteSpace($step.message)) { "" } else { " - $($step.message)" }
            $lines += "- interaction_step: $($step.name)=$($step.status)$message"
            if ($step.status -ne "success" -and $null -ne $step.post_check) {
                $postCheckJson = $step.post_check | ConvertTo-Json -Depth 6 -Compress
                $lines += "- interaction_post_check: $($step.name)=$postCheckJson"
            }
        }
    }
    $lines | Set-Content -Path $Path -Encoding UTF8
}

function Test-FocusContainsLaunchActivity {
    param([string]$Text)

    if ([string]::IsNullOrWhiteSpace($Text)) {
        return $false
    }

    return $Text -match "mCurrentFocus=.*com\.senku\.mobile" `
        -or $Text -match "mFocusedApp=.*com\.senku\.mobile" `
        -or $Text -match "topResumedActivity=.*com\.senku\.mobile" `
        -or $Text -match "ResumedActivity:.*com\.senku\.mobile"
}

function Read-UiAutomatorDump {
    param(
        [string]$ResolvedAdbPath,
        [string]$DeviceSerial
    )

    try {
        $directDump = Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "uiautomator", "dump", "/dev/tty")
        if ($directDump -match "<hierarchy") {
            return $directDump
        }
    } catch {
        # Some physical devices do not stream /dev/tty dumps reliably; fall back below.
    }

    $deviceDumpPath = "/sdcard/senku_physical_smoke_ui.xml"
    Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "uiautomator", "dump", $deviceDumpPath) | Out-Null
    $dumpText = Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "cat", $deviceDumpPath)
    Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "rm", "-f", $deviceDumpPath)
    return $dumpText
}

function Get-TextCheckSummary {
    param(
        [string[]]$Fragments,
        [string]$DumpText
    )

    $requested = @($Fragments | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    $passed = @()
    $missing = @()

    foreach ($fragment in $requested) {
        if ($DumpText.IndexOf($fragment, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
            $passed += $fragment
        } else {
            $missing += $fragment
        }
    }

    return [ordered]@{
        requested = $requested
        passed = $passed
        missing = $missing
    }
}

function Get-UiDumpHash {
    param([string]$DumpText)

    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($DumpText)
        return ([System.BitConverter]::ToString($sha256.ComputeHash($bytes)) -replace "-", "").ToLowerInvariant()
    } finally {
        $sha256.Dispose()
    }
}

function Get-UiDumpTextFragments {
    param([string]$DumpText)

    $fragments = [System.Collections.ArrayList]::new()
    foreach ($match in [regex]::Matches($DumpText, '<node\b[^>]*>')) {
        $attributes = Get-UiNodeAttributeMap -NodeText $match.Value
        foreach ($key in @("text", "content-desc", "resource-id")) {
            $value = [string]$attributes[$key]
            if ([string]::IsNullOrWhiteSpace($value)) {
                continue
            }
            if (-not $fragments.Contains($value)) {
                [void]$fragments.Add($value)
            }
        }
    }
    return @($fragments)
}

function Get-UiPostStepEvidence {
    param(
        [string]$DumpText,
        [string[]]$ExpectedAnyText
    )

    $expected = @($ExpectedAnyText | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    $matched = @()
    foreach ($fragment in $expected) {
        if ($DumpText.IndexOf($fragment, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
            $matched += $fragment
        }
    }

    $textFragments = @(Get-UiDumpTextFragments -DumpText $DumpText | Select-Object -First 25)
    return [ordered]@{
        passed = ($expected.Count -eq 0) -or ($matched.Count -gt 0)
        expected_any_text = $expected
        matched_text = $matched
        ui_text_sample = $textFragments
        dump_length = $DumpText.Length
        dump_sha256 = Get-UiDumpHash -DumpText $DumpText
        captured_at_utc = (Get-Date).ToUniversalTime().ToString("o")
    }
}

function Wait-UiPostStepEvidence {
    param(
        [string]$ResolvedAdbPath,
        [string]$DeviceSerial,
        [string[]]$ExpectedAnyText,
        [int]$TimeoutMs = 3000,
        [int]$PollMs = 500
    )

    $deadline = [DateTime]::UtcNow.AddMilliseconds($TimeoutMs)
    $lastEvidence = $null
    do {
        $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
        $lastEvidence = Get-UiPostStepEvidence -DumpText $dumpText -ExpectedAnyText $ExpectedAnyText
        if ($lastEvidence.passed) {
            return $lastEvidence
        }
        Start-Sleep -Milliseconds $PollMs
    } while ([DateTime]::UtcNow -lt $deadline)

    return $lastEvidence
}

function Expand-RequiredTextFragments {
    param([string[]]$Fragments)

    $expanded = @()
    foreach ($fragmentGroup in $Fragments) {
        if ([string]::IsNullOrWhiteSpace($fragmentGroup)) {
            continue
        }
        foreach ($fragment in ($fragmentGroup -split ",")) {
            if (-not [string]::IsNullOrWhiteSpace($fragment)) {
                $expanded += $fragment.Trim()
            }
        }
    }
    return $expanded
}

function ConvertTo-AdbInputText {
    param([string]$Text)

    $encoded = $Text -replace "\\", "\\\\" `
        -replace "\s", "%s" `
        -replace "'", "\\'" `
        -replace '"', '\"' `
        -replace "\(", "\\(" `
        -replace "\)", "\\)" `
        -replace "\&", "\\&" `
        -replace "\|", "\\|" `
        -replace ";", "\;" `
        -replace "<", "\<" `
        -replace ">", "\>"
    return $encoded
}

function Get-UiNodeAttributeMap {
    param([string]$NodeText)

    $attributes = @{}
    foreach ($match in [regex]::Matches($NodeText, '\s(?<name>[\w:-]+)="(?<value>[^"]*)"')) {
        $attributes[$match.Groups["name"].Value] = [System.Net.WebUtility]::HtmlDecode($match.Groups["value"].Value)
    }
    return $attributes
}

function Get-BoundsCenter {
    param([string]$Bounds)

    if ([string]::IsNullOrWhiteSpace($Bounds)) {
        return $null
    }
    if ($Bounds -notmatch '^\[(?<left>\d+),(?<top>\d+)\]\[(?<right>\d+),(?<bottom>\d+)\]$') {
        return $null
    }

    return [pscustomobject]@{
        x = [int](([int]$Matches["left"] + [int]$Matches["right"]) / 2)
        y = [int](([int]$Matches["top"] + [int]$Matches["bottom"]) / 2)
    }
}

function Find-UiNodeCenter {
    param(
        [string]$DumpText,
        [scriptblock]$Predicate
    )

    foreach ($match in [regex]::Matches($DumpText, '<node\b[^>]*>')) {
        $attributes = Get-UiNodeAttributeMap -NodeText $match.Value
        if (-not (& $Predicate $attributes)) {
            continue
        }

        $center = Get-BoundsCenter -Bounds $attributes["bounds"]
        if ($null -ne $center) {
            return $center
        }
    }
    return $null
}

function New-InteractionStep {
    param(
        [string]$Name,
        [string]$Status,
        [string]$Message = $null,
        $PostCheck = $null
    )

    $step = [ordered]@{
        name = $Name
        status = $Status
    }
    if (-not [string]::IsNullOrWhiteSpace($Message)) {
        $step.message = $Message
    }
    if ($null -ne $PostCheck) {
        $step.post_check = $PostCheck
    }
    return $step
}

function Invoke-InteractionStep {
    param(
        [System.Collections.ArrayList]$Steps,
        [string]$Name,
        [scriptblock]$Action,
        [string[]]$ExpectedAnyText = @()
    )

    $postCheck = $null
    try {
        & $Action
        if ($ExpectedAnyText.Count -gt 0) {
            $postCheck = Wait-UiPostStepEvidence -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial -ExpectedAnyText $ExpectedAnyText
            if (-not $postCheck.passed) {
                $message = "Post-step UI check for '$Name' did not find expected text fragment(s): $($ExpectedAnyText -join ', ')"
                [void]$Steps.Add((New-InteractionStep -Name $Name -Status "failed" -Message $message -PostCheck $postCheck))
                return $false
            }
        }
        [void]$Steps.Add((New-InteractionStep -Name $Name -Status "success" -PostCheck $postCheck))
        return $true
    } catch {
        [void]$Steps.Add((New-InteractionStep -Name $Name -Status "failed" -Message $_.Exception.Message -PostCheck $postCheck))
        return $false
    }
}

function Invoke-SenkuSimpleInteraction {
    param(
        [string]$ResolvedAdbPath,
        [string]$DeviceSerial,
        [string]$QueryText
    )

    $steps = [System.Collections.ArrayList]::new()

    [void](Invoke-InteractionStep -Steps $steps -Name "tap_saved" -ExpectedAnyText @("Saved") -Action {
        $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
        $center = Find-UiNodeCenter -DumpText $dumpText -Predicate {
            param($attributes)
            return ($attributes["text"] -eq "Saved") -or ($attributes["content-desc"] -eq "Saved")
        }
        if ($null -eq $center) {
            throw "Saved control was not found in UIAutomator dump."
        }
        Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "tap", "$($center.x)", "$($center.y)")
        Start-Sleep -Milliseconds 500
    })

    [void](Invoke-InteractionStep -Steps $steps -Name "tap_query_field" -Action {
        $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
        $center = Find-UiNodeCenter -DumpText $dumpText -Predicate {
            param($attributes)
            $className = [string]$attributes["class"]
            $resourceId = [string]$attributes["resource-id"]
            $text = [string]$attributes["text"]
            $description = [string]$attributes["content-desc"]
            return $className.Contains("EditText") `
                -or $resourceId.Contains("query") `
                -or $resourceId.Contains("search") `
                -or $resourceId.Contains("input") `
                -or $text.Contains("Search") `
                -or $text.Contains("Ask") `
                -or $description.Contains("Search") `
                -or $description.Contains("Ask")
        }
        if ($null -eq $center) {
            throw "Query input control was not found in UIAutomator dump."
        }
        Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "tap", "$($center.x)", "$($center.y)")
        Start-Sleep -Milliseconds 300
    })

    [void](Invoke-InteractionStep -Steps $steps -Name "enter_query" -Action {
        if ([string]::IsNullOrWhiteSpace($QueryText)) {
            throw "InteractionQuery is empty."
        }
        $adbText = ConvertTo-AdbInputText -Text $QueryText
        Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "text", $adbText)
        Start-Sleep -Milliseconds 300
    })

    [void](Invoke-InteractionStep -Steps $steps -Name "submit_query" -Action {
        Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "keyevent", "ENTER")
        Start-Sleep -Milliseconds 1000
    } -ExpectedAnyText @($QueryText, "Answer", "Results", "Search", "Ask"))

    [void](Invoke-InteractionStep -Steps $steps -Name "back" -Action {
        Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "keyevent", "BACK")
        Start-Sleep -Milliseconds 500
    } -ExpectedAnyText @("Saved", "Search", "Ask"))

    return @($steps)
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$resolvedAdbPath = if ([string]::IsNullOrWhiteSpace($AdbPath)) { Get-DefaultAdbPath } else { Resolve-TargetPath -Path $AdbPath }
$resolvedApkPath = Resolve-TargetPath -Path $ApkPath
$resolvedFocusPath = if ([string]::IsNullOrWhiteSpace($FocusPath)) { Join-Path $resolvedOutputDir "focus.txt" } else { Resolve-TargetPath -Path $FocusPath }
$resolvedScreenshotPath = if ([string]::IsNullOrWhiteSpace($ScreenshotPath)) { $null } else { Resolve-TargetPath -Path $ScreenshotPath }
$resolvedDumpPath = if ([string]::IsNullOrWhiteSpace($DumpPath)) { $null } else { Resolve-TargetPath -Path $DumpPath }
$resolvedLogcatPath = if ([string]::IsNullOrWhiteSpace($LogcatPath)) { $null } else { Resolve-TargetPath -Path $LogcatPath }

$installCommandText = "adb -s $Serial install --no-streaming -r `"$resolvedApkPath`""
$launchCommandText = "adb -s $Serial shell am start -n $launchActivity"
$startedAt = (Get-Date).ToUniversalTime()
$status = "dry_run_only"
$focusText = $null
$orientationText = $null
$deviceIdentity = $null
$textCheckFailureMessage = $null
$interactionFailureMessage = $null
$interactionSteps = [System.Collections.ArrayList]::new()
$requestedTextFragments = @(Expand-RequiredTextFragments -Fragments $RequiredText)
$textChecks = if ($requestedTextFragments.Count -gt 0) {
    [ordered]@{
        requested = $requestedTextFragments
        passed = @()
        missing = @()
    }
} else {
    $null
}

if (-not $DryRun) {
    Assert-PhysicalSerial -DeviceSerial $Serial
    if (-not (Test-Path -LiteralPath $resolvedAdbPath)) {
        throw "adb not found at $resolvedAdbPath"
    }
    if (-not (Test-Path -LiteralPath $resolvedApkPath)) {
        throw "APK not found at $resolvedApkPath"
    }
    if ($null -eq $resolvedScreenshotPath) {
        $resolvedScreenshotPath = Join-Path $resolvedOutputDir "screenshot.png"
    }
    if ($null -eq $resolvedDumpPath) {
        $resolvedDumpPath = Join-Path $resolvedOutputDir "uiautomator.xml"
    }

    Assert-SerialIsConnectedPhysicalDevice -ResolvedAdbPath $resolvedAdbPath -DeviceSerial $Serial
    $deviceIdentity = Get-PhysicalDeviceIdentity -ResolvedAdbPath $resolvedAdbPath -DeviceSerial $Serial
    Invoke-AdbChecked -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "install", "--no-streaming", "-r", $resolvedApkPath)
    Invoke-AdbChecked -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "shell", "am", "start", "-n", $launchActivity)
    Start-Sleep -Seconds 2

    $windowFocus = Invoke-AdbCapture -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "shell", "dumpsys", "window", "windows")
    $topActivity = Invoke-AdbCapture -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "shell", "dumpsys", "activity", "activities")
    $orientationText = Invoke-AdbCapture -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "shell", "dumpsys", "input")
    $focusText = @(
        "## dumpsys window windows",
        $windowFocus,
        "",
        "## dumpsys activity activities",
        $topActivity
    ) -join [Environment]::NewLine
    Write-Utf8Text -Path $resolvedFocusPath -Content $focusText
    $focusContainsLaunchActivity = Test-FocusContainsLaunchActivity -Text $focusText
    if (-not $focusContainsLaunchActivity) {
        throw "Physical phone smoke launched '$launchActivity' but focus evidence did not show '$packageName' resumed. See $resolvedFocusPath"
    }

    if ($Interact) {
        $interactionSteps = Invoke-SenkuSimpleInteraction -ResolvedAdbPath $resolvedAdbPath -DeviceSerial $Serial -QueryText $InteractionQuery
        $failedInteractionSteps = @($interactionSteps | Where-Object { $_.status -ne "success" })
        if ($failedInteractionSteps.Count -gt 0) {
            $interactionFailureMessage = "Physical phone smoke interaction failed step(s): $((@($failedInteractionSteps | ForEach-Object { $_.name })) -join ', ')"
        }
    }

    if ($null -ne $resolvedScreenshotPath) {
        $screenshotParent = Split-Path -Parent $resolvedScreenshotPath
        if (-not [string]::IsNullOrWhiteSpace($screenshotParent)) {
            New-Item -ItemType Directory -Force -Path $screenshotParent | Out-Null
        }
        & $resolvedAdbPath "-s" $Serial "exec-out" "screencap" "-p" > $resolvedScreenshotPath
        if ($LASTEXITCODE -ne 0) {
            throw "adb screenshot capture failed for serial '$Serial'."
        }
    }

    if (($null -ne $resolvedDumpPath) -or ($requestedTextFragments.Count -gt 0)) {
        $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $resolvedAdbPath -DeviceSerial $Serial
        if ($null -ne $resolvedDumpPath) {
            Write-Utf8Text -Path $resolvedDumpPath -Content $dumpText
        }
        if ($requestedTextFragments.Count -gt 0) {
            $textChecks = Get-TextCheckSummary -Fragments $requestedTextFragments -DumpText $dumpText
            if ($textChecks.missing.Count -gt 0) {
                $textCheckFailureMessage = "Physical phone smoke UIAutomator dump is missing required text fragment(s): $($textChecks.missing -join ', ')"
            }
        }
    }

    if ($null -ne $resolvedLogcatPath) {
        $logcatText = Invoke-AdbCapture -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "logcat", "-d", "-v", "time")
        Write-Utf8Text -Path $resolvedLogcatPath -Content $logcatText
    }

    $status = "completed"
} elseif ($Interact) {
    [void]$interactionSteps.Add((New-InteractionStep -Name "tap_saved" -Status "skipped" -Message "Dry run only."))
    [void]$interactionSteps.Add((New-InteractionStep -Name "tap_query_field" -Status "skipped" -Message "Dry run only."))
    [void]$interactionSteps.Add((New-InteractionStep -Name "enter_query" -Status "skipped" -Message "Dry run only."))
    [void]$interactionSteps.Add((New-InteractionStep -Name "submit_query" -Status "skipped" -Message "Dry run only."))
    [void]$interactionSteps.Add((New-InteractionStep -Name "back" -Status "skipped" -Message "Dry run only."))
}

$finishedAt = (Get-Date).ToUniversalTime()
$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"

$summaryData = [ordered]@{
    status = $status
    dry_run = [bool]$DryRun
    physical_device = $true
    launches_emulators = $false
    serial_required_unless_dry_run = $true
    serial = if ([string]::IsNullOrWhiteSpace($Serial)) { $null } else { $Serial }
    adb_path = (Convert-ToRepoRelativePath -Path $resolvedAdbPath)
    adb_path_source = if ([string]::IsNullOrWhiteSpace($AdbPath)) { "LOCALAPPDATA_ANDROID_SDK" } else { "override" }
    apk_path = (Convert-ToRepoRelativePath -Path $resolvedApkPath)
    apk_sha256 = Get-FileSha256 -Path $resolvedApkPath
    device_identity = $deviceIdentity
    package = $packageName
    launch_activity = $launchActivity
    evidence = [ordered]@{
        focus_path = (Convert-ToRepoRelativePath -Path $resolvedFocusPath)
        screenshot_path = (Convert-ToRepoRelativePath -Path $resolvedScreenshotPath)
        dump_path = (Convert-ToRepoRelativePath -Path $resolvedDumpPath)
        logcat_path = (Convert-ToRepoRelativePath -Path $resolvedLogcatPath)
        focus_contains_launch_activity = if ($null -eq $focusText) { $null } else { Test-FocusContainsLaunchActivity -Text $focusText }
        focused_package = Get-FocusedPackage -Text $focusText
        orientation = Get-OrientationEvidence -Text $orientationText
        artifact_hashes = [ordered]@{
            focus_sha256 = Get-FileSha256 -Path $resolvedFocusPath
            screenshot_sha256 = Get-FileSha256 -Path $resolvedScreenshotPath
            dump_sha256 = Get-FileSha256 -Path $resolvedDumpPath
            logcat_sha256 = Get-FileSha256 -Path $resolvedLogcatPath
        }
    }
    commands = [ordered]@{
        devices = "adb devices"
        install = $installCommandText
        launch = $launchCommandText
        focus = "adb -s $Serial shell dumpsys window windows; adb -s $Serial shell dumpsys activity activities"
        orientation = "adb -s $Serial shell dumpsys input"
        screenshot = if ($null -eq $resolvedScreenshotPath) { $null } else { "adb -s $Serial exec-out screencap -p > `"$resolvedScreenshotPath`"" }
        dump = if ($null -eq $resolvedDumpPath -and $requestedTextFragments.Count -eq 0) { $null } else { "adb -s $Serial shell uiautomator dump /dev/tty || dump /sdcard/senku_physical_smoke_ui.xml" }
        logcat = if ($null -eq $resolvedLogcatPath) { $null } else { "adb -s $Serial logcat -d -v time" }
    }
    summary_json = (Convert-ToRepoRelativePath -Path $summaryJsonPath)
    summary_markdown = (Convert-ToRepoRelativePath -Path $summaryMarkdownPath)
    started_at_utc = $startedAt.ToString("o")
    finished_at_utc = $finishedAt.ToString("o")
}
if ($null -ne $textChecks) {
    $summaryData.text_checks = $textChecks
}
if ($Interact) {
    $summaryData.commands.interaction = "adb -s $Serial shell input tap/text/keyevent using UIAutomator-selected controls"
    $summaryData.interaction = [ordered]@{
        enabled = $true
        query = $InteractionQuery
        steps = @($interactionSteps)
    }
}
$summary = [pscustomobject]$summaryData

Write-JsonFile -Path $summaryJsonPath -Value $summary
Write-SummaryMarkdown -Path $summaryMarkdownPath -Summary $summary

Write-Host "Wrote Android physical phone smoke summary: $summaryJsonPath"
if ($DryRun) {
    Write-Host "Dry run only. Planned install command: $installCommandText"
}
if ($null -ne $textCheckFailureMessage) {
    throw $textCheckFailureMessage
}
if ($null -ne $interactionFailureMessage) {
    throw $interactionFailureMessage
}
