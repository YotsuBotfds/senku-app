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
    [ValidateSet("Simple", "Ask")]
    [string]$InteractionMode = "Simple",
    [string]$InteractionQuery = "water filter charcoal sand",
    [int]$AdbCommandTimeoutSeconds = 30,
    [int]$AdbInstallTimeoutSeconds = 120
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

function Assert-PositiveTimeout {
    param(
        [string]$Name,
        [int]$Seconds
    )

    if ($Seconds -le 0) {
        throw "$Name must be greater than zero seconds."
    }
}

function ConvertTo-ProcessArgumentString {
    param([string[]]$Arguments)

    $quoted = @()
    foreach ($argument in $Arguments) {
        $text = [string]$argument
        if ($text.Length -eq 0) {
            $quoted += '""'
            continue
        }
        if ($text -notmatch '[\s"]') {
            $quoted += $text
            continue
        }
        $escaped = $text -replace '(\\*)"', '$1$1\"'
        $escaped = $escaped -replace '(\\+)$', '$1$1'
        $quoted += '"' + $escaped + '"'
    }
    return ($quoted -join " ")
}

function Invoke-BoundedProcess {
    param(
        [string]$FilePath,
        [string[]]$Arguments,
        [int]$TimeoutSeconds,
        [string]$StdoutPath = $null
    )

    Assert-PositiveTimeout -Name "TimeoutSeconds" -Seconds $TimeoutSeconds

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $FilePath
    $psi.Arguments = ConvertTo-ProcessArgumentString -Arguments $Arguments
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi
    $stdoutTask = $null
    $stderrTask = $null
    $stdoutStream = $null
    $timedOut = $false
    $stdoutText = ""
    $stderrText = ""
    try {
        if (-not [string]::IsNullOrWhiteSpace($StdoutPath)) {
            $parent = Split-Path -Parent $StdoutPath
            if (-not [string]::IsNullOrWhiteSpace($parent)) {
                New-Item -ItemType Directory -Force -Path $parent | Out-Null
            }
        }

        [void]$process.Start()
        if ([string]::IsNullOrWhiteSpace($StdoutPath)) {
            $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        } else {
            $stdoutStream = [System.IO.File]::Open($StdoutPath, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write, [System.IO.FileShare]::Read)
            $stdoutTask = $process.StandardOutput.BaseStream.CopyToAsync($stdoutStream)
        }
        $stderrTask = $process.StandardError.ReadToEndAsync()

        $timeoutMilliseconds = [Math]::Max(1, $TimeoutSeconds) * 1000
        if (-not $process.WaitForExit($timeoutMilliseconds)) {
            $timedOut = $true
            try {
                $process.Kill()
            } catch {
            }
        }

        if ($stdoutTask -and -not $stdoutTask.Wait(5000)) {
            $timedOut = $true
        }
        if ($stderrTask -and -not $stderrTask.Wait(5000)) {
            $timedOut = $true
        }

        if ([string]::IsNullOrWhiteSpace($StdoutPath) -and $stdoutTask -and $stdoutTask.IsCompleted) {
            try {
                $stdoutText = [string]$stdoutTask.GetAwaiter().GetResult()
            } catch {
                $stdoutText = ""
            }
        }
        if ($stderrTask -and $stderrTask.IsCompleted) {
            try {
                $stderrText = [string]$stderrTask.GetAwaiter().GetResult()
            } catch {
                $stderrText = ""
            }
        }

        $combinedOutput = (($stdoutText, $stderrText) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join [Environment]::NewLine
        return [pscustomobject]@{
            exit_code = if ($timedOut -or -not $process.HasExited) { -1 } else { $process.ExitCode }
            timed_out = $timedOut
            output = $combinedOutput.TrimEnd()
        }
    } finally {
        if ($stdoutStream) {
            try {
                $stdoutStream.Dispose()
            } catch {
            }
        }
        if ($process) {
            try {
                $process.Dispose()
            } catch {
            }
        }
    }
}

function Invoke-AdbCapture {
    param(
        [string]$ResolvedAdbPath,
        [string[]]$Arguments,
        [int]$TimeoutSeconds = $AdbCommandTimeoutSeconds
    )

    $result = Invoke-BoundedProcess -FilePath $ResolvedAdbPath -Arguments $Arguments -TimeoutSeconds $TimeoutSeconds
    $text = [string]$result.output
    if ($result.timed_out) {
        throw "adb command timed out after $TimeoutSeconds seconds: adb $($Arguments -join ' ')`n$text"
    }
    if ($result.exit_code -ne 0) {
        throw "adb command failed ($($result.exit_code)): adb $($Arguments -join ' ')`n$text"
    }
    return $text
}

function Invoke-AdbChecked {
    param(
        [string]$ResolvedAdbPath,
        [string[]]$Arguments,
        [int]$TimeoutSeconds = $AdbCommandTimeoutSeconds,
        [string]$StdoutPath = $null
    )

    if ([string]::IsNullOrWhiteSpace($StdoutPath)) {
        Invoke-AdbCapture -ResolvedAdbPath $ResolvedAdbPath -Arguments $Arguments -TimeoutSeconds $TimeoutSeconds | Out-Null
        return
    }

    $result = Invoke-BoundedProcess -FilePath $ResolvedAdbPath -Arguments $Arguments -TimeoutSeconds $TimeoutSeconds -StdoutPath $StdoutPath
    $text = [string]$result.output
    if ($result.timed_out) {
        throw "adb command timed out after $TimeoutSeconds seconds: adb $($Arguments -join ' ')`n$text"
    }
    if ($result.exit_code -ne 0) {
        throw "adb command failed ($($result.exit_code)): adb $($Arguments -join ' ')`n$text"
    }
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
        "- adb_command_timeout_seconds: $($Summary.timeouts.adb_command_timeout_seconds)",
        "- adb_install_timeout_seconds: $($Summary.timeouts.adb_install_timeout_seconds)",
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
            "- interaction_mode: $($Summary.interaction.mode)",
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
        [string[]]$ExpectedAnyText,
        [string]$StepName = "",
        [string]$BaselineDumpSha256 = $null,
        [bool]$RequireChangedDump = $false
    )

    $expected = @($ExpectedAnyText | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    $matched = @()
    foreach ($fragment in $expected) {
        if ($DumpText.IndexOf($fragment, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
            $matched += $fragment
        }
    }

    $dumpSha256 = Get-UiDumpHash -DumpText $DumpText
    $hasBaseline = -not [string]::IsNullOrWhiteSpace($BaselineDumpSha256)
    $dumpChanged = if ($hasBaseline) { $dumpSha256 -ne $BaselineDumpSha256 } else { $null }
    $textPassed = (($expected.Count -eq 0) -and (-not $RequireChangedDump)) -or ($matched.Count -gt 0)
    $changePassed = $RequireChangedDump -and ($dumpChanged -eq $true)

    $textFragments = @(Get-UiDumpTextFragments -DumpText $DumpText | Select-Object -First 25)
    $evidence = [ordered]@{
        passed = $textPassed -or $changePassed
        expected_any_text = $expected
        matched_text = $matched
        ui_text_sample = $textFragments
        dump_length = $DumpText.Length
        dump_sha256 = $dumpSha256
        baseline_dump_sha256 = if ($hasBaseline) { $BaselineDumpSha256 } else { $null }
        dump_changed = $dumpChanged
        require_changed_dump = $RequireChangedDump
        captured_at_utc = (Get-Date).ToUniversalTime().ToString("o")
    }
    if (-not [string]::IsNullOrWhiteSpace($StepName)) {
        $evidence.step_name = $StepName
    }
    if ($StepName -eq "tap_saved") {
        $evidence.selected_destination = "saved"
    } elseif ($StepName -eq "tap_ask") {
        $evidence.selected_destination = "ask"
    }
    return $evidence
}

function Get-SavedDestinationEvidenceFragments {
    return @(
        "No saved guides yet",
        "This tab only shows saved guides",
        "Saved guide "
    )
}

function Get-AskOwnedEvidenceFragments {
    return @(
        "Ask Senku",
        "Ask a question",
        "Ask the manual a question",
        "Ask anything",
        "Ask a survival question",
        "Share your situation",
        "What do you need help with"
    )
}

function Get-AskAnswerEvidenceFragments {
    return @(
        "Answer",
        "Answer model unavailable",
        "Import a model",
        "Host GPU",
        "Sources",
        "Details",
        "Related guides",
        "Recommended guides",
        "No matching result"
    )
}

function Get-InteractionStepNames {
    param([string]$Mode)

    if ($Mode -eq "Ask") {
        return @(
            "tap_ask",
            "tap_ask_query_field",
            "enter_query",
            "submit_ask_query",
            "back_to_ask"
        )
    }

    return @(
        "tap_saved",
        "tap_query_field",
        "enter_query",
        "submit_query",
        "back"
    )
}

function Wait-UiPostStepEvidence {
    param(
        [string]$ResolvedAdbPath,
        [string]$DeviceSerial,
        [string[]]$ExpectedAnyText,
        [string]$StepName = "",
        [string]$BaselineDumpSha256 = $null,
        [bool]$RequireChangedDump = $false,
        [int]$TimeoutMs = 3000,
        [int]$PollMs = 500
    )

    $deadline = [DateTime]::UtcNow.AddMilliseconds($TimeoutMs)
    $lastEvidence = $null
    do {
        $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
        $lastEvidence = Get-UiPostStepEvidence -DumpText $dumpText -ExpectedAnyText $ExpectedAnyText -StepName $StepName -BaselineDumpSha256 $BaselineDumpSha256 -RequireChangedDump $RequireChangedDump
        $script:LastUiPostStepEvidence = $lastEvidence
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

function Find-AskControlCenter {
    param([string]$DumpText)

    $center = Find-UiNodeCenter -DumpText $DumpText -Predicate {
        param($attributes)
        $className = [string]$attributes["class"]
        $text = [string]$attributes["text"]
        $description = [string]$attributes["content-desc"]
        return (-not $className.Contains("EditText")) -and (($text -eq "Ask") -or ($description -eq "Ask"))
    }
    if ($null -ne $center) {
        return $center
    }

    return Find-UiNodeCenter -DumpText $DumpText -Predicate {
        param($attributes)
        $className = [string]$attributes["class"]
        $text = [string]$attributes["text"]
        $description = [string]$attributes["content-desc"]
        return (-not $className.Contains("EditText")) -and (($text -match "\bAsk\b") -or ($description -match "\bAsk\b"))
    }
}

function Find-AskSubmitCenter {
    param([string]$DumpText)

    return Find-UiNodeCenter -DumpText $DumpText -Predicate {
        param($attributes)
        $className = [string]$attributes["class"]
        $resourceId = [string]$attributes["resource-id"]
        $text = [string]$attributes["text"]
        $description = [string]$attributes["content-desc"]
        $clickable = [string]$attributes["clickable"]
        $visibleLabel = "$text $description"
        $isRealSubmitControl = $clickable -eq "true" `
            -or $className.Contains("Button") `
            -or ($resourceId -match "(ask_button|search_button|submit|send|share)")
        return (-not $className.Contains("EditText")) -and (
            $isRealSubmitControl -and (
                ($text -in @("Ask", "Submit", "Send", "Share")) `
                -or ($description -in @("Ask", "Submit", "Send", "Share")) `
                -or ($visibleLabel -match "\b(Ask|Submit|Send|Share)\b") `
                -or ($resourceId -match "(ask|submit|send|share)")
            )
        )
    }
}

function Wait-PhysicalSmokePackReadyIfInstalling {
    param(
        [string]$ResolvedAdbPath,
        [string]$DeviceSerial,
        [int]$TimeoutMs = 60000,
        [int]$PollMs = 1000
    )

    $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
    $isInstalling = $dumpText.IndexOf("Installing offline pack", [System.StringComparison]::OrdinalIgnoreCase) -ge 0 `
        -or $dumpText.IndexOf("Preparing manual", [System.StringComparison]::OrdinalIgnoreCase) -ge 0
    $isReady = $dumpText.IndexOf("Ready offline", [System.StringComparison]::OrdinalIgnoreCase) -ge 0 `
        -or $dumpText.IndexOf("Pack ready", [System.StringComparison]::OrdinalIgnoreCase) -ge 0
    if ((-not $isInstalling) -or $isReady) {
        return
    }

    $deadline = [DateTime]::UtcNow.AddMilliseconds($TimeoutMs)
    do {
        Start-Sleep -Milliseconds $PollMs
        $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
        $isReady = $dumpText.IndexOf("Ready offline", [System.StringComparison]::OrdinalIgnoreCase) -ge 0 `
            -or $dumpText.IndexOf("Pack ready", [System.StringComparison]::OrdinalIgnoreCase) -ge 0
        if ($isReady) {
            return
        }
    } while ([DateTime]::UtcNow -lt $deadline)

    throw "Physical phone smoke did not reach a pack-ready UI state before interaction."
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
        [string[]]$ExpectedAnyText = @(),
        [switch]$RequireChangedDump,
        [switch]$RequireTextEvidence
    )

    $postCheck = $null
    $baselineDumpSha256 = $null
    try {
        if ($RequireChangedDump) {
            $baselineDumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
            $baselineDumpSha256 = Get-UiDumpHash -DumpText $baselineDumpText
        }
        & $Action
        if (($ExpectedAnyText.Count -gt 0) -or $RequireChangedDump) {
            $postCheck = Wait-UiPostStepEvidence -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial -ExpectedAnyText $ExpectedAnyText -StepName $Name -BaselineDumpSha256 $baselineDumpSha256 -RequireChangedDump ([bool]$RequireChangedDump)
            $hasTextEvidence = @($postCheck.matched_text).Count -gt 0
            if ((-not $postCheck.passed) -or ($RequireTextEvidence -and (-not $hasTextEvidence))) {
                $message = "Post-step UI check for '$Name' did not find expected text fragment(s) or changed UI dump: $($ExpectedAnyText -join ', ')"
                [void]$Steps.Add((New-InteractionStep -Name $Name -Status "failed" -Message $message -PostCheck $postCheck))
                return $false
            }
        }
        [void]$Steps.Add((New-InteractionStep -Name $Name -Status "success" -PostCheck $postCheck))
        return $true
    } catch {
        if ($null -eq $postCheck -and (($ExpectedAnyText.Count -gt 0) -or $RequireChangedDump) -and $null -ne $script:LastUiPostStepEvidence) {
            $lastEvidenceStepName = [string]$script:LastUiPostStepEvidence.step_name
            if ([string]::IsNullOrWhiteSpace($lastEvidenceStepName) -or $lastEvidenceStepName -eq $Name) {
                $postCheck = $script:LastUiPostStepEvidence
            }
        }
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

    [void](Invoke-InteractionStep -Steps $steps -Name "tap_saved" -ExpectedAnyText (Get-SavedDestinationEvidenceFragments) -Action {
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
    } -ExpectedAnyText @("Answer", "Results", "Sources", "Related guides", "No matching result") -RequireChangedDump)

    [void](Invoke-InteractionStep -Steps $steps -Name "back" -Action {
        Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "keyevent", "BACK")
        Start-Sleep -Milliseconds 500
    } -ExpectedAnyText (Get-SavedDestinationEvidenceFragments) -RequireChangedDump)

    return @($steps)
}

function Invoke-SenkuAskInteraction {
    param(
        [string]$ResolvedAdbPath,
        [string]$DeviceSerial,
        [string]$QueryText
    )

    $steps = [System.Collections.ArrayList]::new()

    [void](Invoke-InteractionStep -Steps $steps -Name "tap_ask" -ExpectedAnyText (Get-AskOwnedEvidenceFragments) -Action {
        $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
        $center = Find-AskControlCenter -DumpText $dumpText
        if ($null -eq $center) {
            throw "Ask control was not found in UIAutomator dump."
        }
        Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "tap", "$($center.x)", "$($center.y)")
        Start-Sleep -Milliseconds 500
    })

    [void](Invoke-InteractionStep -Steps $steps -Name "tap_ask_query_field" -Action {
        $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
        $center = Find-UiNodeCenter -DumpText $dumpText -Predicate {
            param($attributes)
            $className = [string]$attributes["class"]
            $resourceId = [string]$attributes["resource-id"]
            $text = [string]$attributes["text"]
            $description = [string]$attributes["content-desc"]
            return $className.Contains("EditText") `
                -or $resourceId.Contains("query") `
                -or $resourceId.Contains("ask") `
                -or $resourceId.Contains("input") `
                -or $text.Contains("Ask") `
                -or $text.Contains("question") `
                -or $description.Contains("Ask") `
                -or $description.Contains("question")
        }
        if ($null -eq $center) {
            throw "Ask query input control was not found in UIAutomator dump."
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

    [void](Invoke-InteractionStep -Steps $steps -Name "submit_ask_query" -Action {
        $dumpText = Read-UiAutomatorDump -ResolvedAdbPath $ResolvedAdbPath -DeviceSerial $DeviceSerial
        $center = Find-AskSubmitCenter -DumpText $dumpText
        if ($null -ne $center) {
            Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "tap", "$($center.x)", "$($center.y)")
        } else {
            Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "keyevent", "ENTER")
        }
        Start-Sleep -Milliseconds 1000
    } -ExpectedAnyText (Get-AskAnswerEvidenceFragments) -RequireChangedDump -RequireTextEvidence)

    [void](Invoke-InteractionStep -Steps $steps -Name "back_to_ask" -Action {
        Invoke-AdbChecked -ResolvedAdbPath $ResolvedAdbPath -Arguments @("-s", $DeviceSerial, "shell", "input", "keyevent", "BACK")
        Start-Sleep -Milliseconds 500
    } -ExpectedAnyText (Get-AskOwnedEvidenceFragments) -RequireChangedDump)

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

Assert-PositiveTimeout -Name "AdbCommandTimeoutSeconds" -Seconds $AdbCommandTimeoutSeconds
Assert-PositiveTimeout -Name "AdbInstallTimeoutSeconds" -Seconds $AdbInstallTimeoutSeconds

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
$script:LastUiPostStepEvidence = $null
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
    Invoke-AdbChecked -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "install", "--no-streaming", "-r", $resolvedApkPath) -TimeoutSeconds $AdbInstallTimeoutSeconds
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
        Wait-PhysicalSmokePackReadyIfInstalling -ResolvedAdbPath $resolvedAdbPath -DeviceSerial $Serial
        if ($InteractionMode -eq "Ask") {
            $interactionSteps = Invoke-SenkuAskInteraction -ResolvedAdbPath $resolvedAdbPath -DeviceSerial $Serial -QueryText $InteractionQuery
        } else {
            $interactionSteps = Invoke-SenkuSimpleInteraction -ResolvedAdbPath $resolvedAdbPath -DeviceSerial $Serial -QueryText $InteractionQuery
        }
        $failedInteractionSteps = @($interactionSteps | Where-Object { $_.status -ne "success" })
        if ($failedInteractionSteps.Count -gt 0) {
            $interactionFailureMessage = "Physical phone smoke interaction failed step(s): $((@($failedInteractionSteps | ForEach-Object { $_.name })) -join ', ')"
            $failedPostCheckEvidence = @($failedInteractionSteps | Where-Object { $null -ne $_.post_check } | Select-Object -Last 1)
            if ($failedPostCheckEvidence.Count -gt 0) {
                $postCheck = $failedPostCheckEvidence[0].post_check
                $interactionFailureMessage = "{0}; last post_check step={1} expected_any_text=[{2}] matched_text=[{3}] dump_length={4} dump_sha256={5}" -f `
                    $interactionFailureMessage,
                    $failedPostCheckEvidence[0].name,
                    ((@($postCheck.expected_any_text) | ForEach-Object { [string]$_ }) -join ", "),
                    ((@($postCheck.matched_text) | ForEach-Object { [string]$_ }) -join ", "),
                    $postCheck.dump_length,
                    $postCheck.dump_sha256
            }
        }
    }

    if ($null -ne $resolvedScreenshotPath) {
        $screenshotParent = Split-Path -Parent $resolvedScreenshotPath
        if (-not [string]::IsNullOrWhiteSpace($screenshotParent)) {
            New-Item -ItemType Directory -Force -Path $screenshotParent | Out-Null
        }
        Invoke-AdbChecked -ResolvedAdbPath $resolvedAdbPath -Arguments @("-s", $Serial, "exec-out", "screencap", "-p") -StdoutPath $resolvedScreenshotPath
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
    foreach ($stepName in (Get-InteractionStepNames -Mode $InteractionMode)) {
        [void]$interactionSteps.Add((New-InteractionStep -Name $stepName -Status "skipped" -Message "Dry run only."))
    }
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
    timeouts = [ordered]@{
        adb_command_timeout_seconds = $AdbCommandTimeoutSeconds
        adb_install_timeout_seconds = $AdbInstallTimeoutSeconds
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
    $summaryData.commands.interaction = "adb -s $Serial shell input tap/text/keyevent using UIAutomator-selected controls; mode=$InteractionMode"
    $summaryData.interaction = [ordered]@{
        enabled = $true
        mode = $InteractionMode
        query = $InteractionQuery
        steps = @($interactionSteps)
    }
    $failedPostCheckSteps = @($interactionSteps | Where-Object { $_.status -ne "success" -and $null -ne $_.post_check })
    if ($failedPostCheckSteps.Count -gt 0) {
        $summaryData.interaction.last_post_check = $failedPostCheckSteps[-1].post_check
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
