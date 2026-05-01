function Convert-AndroidProcessArgumentString {
    param([string[]]$Arguments)

    $quoted = foreach ($argument in $Arguments) {
        if ($null -eq $argument) {
            '""'
            continue
        }
        $value = [string]$argument
        if ($value -match '[\s"]') {
            '"' + $value.Replace('"', '\"') + '"'
        } else {
            $value
        }
    }
    return ($quoted -join " ")
}

function Resolve-AndroidHarnessDeviceList {
    param([string[]]$Devices)

    $normalized = New-Object System.Collections.Generic.List[string]
    foreach ($deviceEntry in @($Devices)) {
        foreach ($split in ([string]$deviceEntry -split ",")) {
            $trimmed = $split.Trim()
            if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
                $normalized.Add($trimmed)
            }
        }
    }
    return $normalized.ToArray()
}

function New-AndroidHarnessDeviceLockHandle {
    param(
        [System.IO.FileStream]$Stream,
        [string]$LockPath
    )

    $handle = [pscustomobject]@{
        stream = $Stream
        lock_path = $LockPath
    }

    $disposeBody = {
        if ($this.stream) {
            try {
                $this.stream.Dispose()
            } catch {
            }
            $this.stream = $null
        }
        if (-not [string]::IsNullOrWhiteSpace($this.lock_path) -and (Test-Path -LiteralPath $this.lock_path)) {
            try {
                Remove-Item -LiteralPath $this.lock_path -Force -ErrorAction Stop
            } catch {
            }
        }
    }

    Add-Member -InputObject $handle -MemberType ScriptMethod -Name Dispose -Value $disposeBody -Force
    return $handle
}

function Remove-StaleAndroidHarnessLocks {
    param([string]$LockRoot)

    if (-not (Test-Path -LiteralPath $LockRoot)) {
        return
    }

    Get-ChildItem -LiteralPath $LockRoot -Filter "*.lock" -File -ErrorAction SilentlyContinue | ForEach-Object {
        try {
            $probe = [System.IO.File]::Open($_.FullName, [System.IO.FileMode]::Open, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
            try {
                $probe.Dispose()
            } finally {
                Remove-Item -LiteralPath $_.FullName -Force -ErrorAction Stop
            }
        } catch [System.IO.FileNotFoundException] {
        } catch [System.IO.IOException] {
        } catch {
        }
    }
}

function Acquire-AndroidHarnessDeviceLock {
    param(
        [string]$DeviceName,
        [string]$LockRoot,
        [int]$TimeoutSeconds = 900,
        [string]$ProgressLabel = "",
        [int]$ProgressIntervalSeconds = 15
    )

    New-Item -ItemType Directory -Force -Path $LockRoot | Out-Null
    Remove-StaleAndroidHarnessLocks -LockRoot $LockRoot
    $safeName = ($DeviceName -replace '[^A-Za-z0-9._-]', '_')
    $lockPath = Join-Path $LockRoot ($safeName + ".lock")
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $startedAt = Get-Date
    $nextProgressAt = $startedAt
    while ((Get-Date) -lt $deadline) {
        try {
            $stream = [System.IO.File]::Open($lockPath, [System.IO.FileMode]::OpenOrCreate, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
            $elapsedSeconds = [Math]::Round(((Get-Date) - $startedAt).TotalSeconds, 1)
            if ($elapsedSeconds -ge 1 -and -not [string]::IsNullOrWhiteSpace($ProgressLabel)) {
                Write-Host ("{0} device lock acquired for {1} after {2}s" -f $ProgressLabel, $DeviceName, $elapsedSeconds)
            }
            return New-AndroidHarnessDeviceLockHandle -Stream $stream -LockPath $lockPath
        } catch [System.IO.IOException] {
            $now = Get-Date
            if (-not [string]::IsNullOrWhiteSpace($ProgressLabel) -and $now -ge $nextProgressAt) {
                $elapsedSeconds = [Math]::Round(($now - $startedAt).TotalSeconds, 1)
                $remainingSeconds = [Math]::Max(0, [Math]::Round(($deadline - $now).TotalSeconds, 1))
                Write-Host ("{0} waiting for device lock on {1}; elapsed_seconds={2}; remaining_seconds={3}; lock_path={4}" -f $ProgressLabel, $DeviceName, $elapsedSeconds, $remainingSeconds, $lockPath)
                $nextProgressAt = $now.AddSeconds([Math]::Max(1, $ProgressIntervalSeconds))
            }
            Start-Sleep -Milliseconds 500
        }
    }
    $elapsedSeconds = [Math]::Round(((Get-Date) - $startedAt).TotalSeconds, 1)
    throw "Timed out waiting for device lock on $DeviceName after ${elapsedSeconds}s at $lockPath"
}

function Invoke-AndroidAdbCommandCapture {
    param(
        [string]$AdbPath,
        [string[]]$Arguments,
        [int]$TimeoutMilliseconds = 0
    )

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $AdbPath
    $psi.Arguments = Convert-AndroidProcessArgumentString -Arguments $Arguments
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi

    $timedOut = $false
    $stdoutTask = $null
    $stderrTask = $null
    $stdoutCompleted = $false
    $stderrCompleted = $false

    try {
        [void]$process.Start()
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()

        if ($TimeoutMilliseconds -gt 0) {
            $finished = $process.WaitForExit($TimeoutMilliseconds)
            if (-not $finished) {
                $timedOut = $true
                try {
                    $process.Kill()
                } catch {
                }
            }
        } else {
            $process.WaitForExit()
        }

        try {
            if ($stdoutTask) {
                $stdoutCompleted = $stdoutTask.Wait(5000)
                if (-not $stdoutCompleted) {
                    $timedOut = $true
                }
            }
        } catch {
            $stdoutCompleted = $false
        }
        try {
            if ($stderrTask) {
                $stderrCompleted = $stderrTask.Wait(5000)
                if (-not $stderrCompleted) {
                    $timedOut = $true
                }
            }
        } catch {
            $stderrCompleted = $false
        }

        if ($timedOut) {
            try {
                if (-not $process.HasExited) {
                    [void]$process.WaitForExit(1000)
                }
            } catch {
            }
        }

        $stdout = ""
        if ($stdoutTask -and $stdoutCompleted -and $stdoutTask.IsCompleted) {
            try {
                $stdout = [string]$stdoutTask.GetAwaiter().GetResult()
            } catch {
                $stdout = ""
            }
        }
        $stderr = ""
        if ($stderrTask -and $stderrCompleted -and $stderrTask.IsCompleted) {
            try {
                $stderr = [string]$stderrTask.GetAwaiter().GetResult()
            } catch {
                $stderr = ""
            }
        }
        $combinedOutput = (($stdout, $stderr) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join [Environment]::NewLine

        return [pscustomobject]@{
            exit_code = $(if ($timedOut -or -not $process.HasExited) { -1 } else { $process.ExitCode })
            timed_out = $timedOut
            output = $combinedOutput.TrimEnd()
        }
    } finally {
        if ($process) {
            try {
                $process.Dispose()
            } catch {
            }
        }
    }
}

function Get-AndroidHarnessAdbOutputText {
    param(
        [string]$AdbPath,
        [string[]]$Arguments,
        [int]$TimeoutMilliseconds = 10000
    )

    $result = Invoke-AndroidAdbCommandCapture -AdbPath $AdbPath -Arguments $Arguments -TimeoutMilliseconds $TimeoutMilliseconds
    if ($result.timed_out) {
        return $null
    }

    if ($null -eq $result.output) {
        return ""
    }

    return [string]$result.output
}

function Resolve-AndroidHostAdbPlatformToolsVersionText {
    param([string]$OutputText)

    if ([string]::IsNullOrWhiteSpace($OutputText)) {
        return $null
    }

    $lines = @(([string]$OutputText -split "`r?`n") | ForEach-Object { $_.Trim() } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    $platformToolsLine = @($lines | Where-Object { $_ -match "^Version\s+(.+)$" } | Select-Object -First 1)
    if ($platformToolsLine.Count -gt 0 -and $platformToolsLine[0] -match "^Version\s+(.+)$") {
        return $Matches[1]
    }

    $bridgeVersionLine = @($lines | Where-Object { $_ -match "Android Debug Bridge version\s+([0-9A-Za-z.\-]+)" } | Select-Object -First 1)
    if ($bridgeVersionLine.Count -gt 0 -and $bridgeVersionLine[0] -match "Android Debug Bridge version\s+([0-9A-Za-z.\-]+)") {
        return $Matches[1]
    }

    if ($lines.Count -eq 0) {
        return $null
    }

    return $lines[0]
}

function Get-AndroidHostAdbPlatformToolsVersion {
    param([string]$AdbPath)

    $result = Invoke-AndroidAdbCommandCapture -AdbPath $AdbPath -Arguments @("version") -TimeoutMilliseconds 10000
    if ($result.exit_code -ne 0 -or [string]::IsNullOrWhiteSpace([string]$result.output)) {
        return $null
    }

    return Resolve-AndroidHostAdbPlatformToolsVersionText -OutputText ([string]$result.output)
}

function Get-AndroidPhysicalDisplaySize {
    param(
        [string]$AdbPath,
        [string]$DeviceName
    )

    $wmOutput = Get-AndroidHarnessAdbOutputText -AdbPath $AdbPath -Arguments @("-s", $DeviceName, "shell", "wm", "size")
    if ([string]::IsNullOrWhiteSpace($wmOutput)) {
        return $null
    }

    $match = [regex]::Match($wmOutput, 'Physical size:\s*(\d+)x(\d+)')
    if (-not $match.Success) {
        $match = [regex]::Match($wmOutput, '(\d+)x(\d+)')
    }
    if (-not $match.Success) {
        return $null
    }

    return [pscustomobject]@{
        width = [int]$match.Groups[1].Value
        height = [int]$match.Groups[2].Value
    }
}

function Get-AndroidDisplayDensity {
    param(
        [string]$AdbPath,
        [string]$DeviceName
    )

    $densityOutput = Get-AndroidHarnessAdbOutputText -AdbPath $AdbPath -Arguments @("-s", $DeviceName, "shell", "wm", "density")
    if ([string]::IsNullOrWhiteSpace($densityOutput)) {
        return $null
    }

    $physicalDensity = $null
    $overrideDensity = $null

    $physicalMatch = [regex]::Match($densityOutput, 'Physical density:\s*(\d+)')
    if ($physicalMatch.Success) {
        $physicalDensity = [int]$physicalMatch.Groups[1].Value
    }

    $overrideMatch = [regex]::Match($densityOutput, 'Override density:\s*(\d+)')
    if ($overrideMatch.Success) {
        $overrideDensity = [int]$overrideMatch.Groups[1].Value
    }

    $resolvedDensity = if ($null -ne $overrideDensity) {
        $overrideDensity
    } elseif ($null -ne $physicalDensity) {
        $physicalDensity
    } else {
        $fallbackMatch = [regex]::Match($densityOutput, '(\d+)')
        if ($fallbackMatch.Success) {
            [int]$fallbackMatch.Groups[1].Value
        } else {
            $null
        }
    }

    if ($null -eq $resolvedDensity) {
        return $null
    }

    return [pscustomobject]@{
        density_dpi = $resolvedDensity
        physical_density_dpi = $physicalDensity
        override_density_dpi = $overrideDensity
    }
}

function Get-AndroidCurrentRotation {
    param(
        [string]$AdbPath,
        [string]$DeviceName
    )

    $displayDump = Get-AndroidHarnessAdbOutputText -AdbPath $AdbPath -Arguments @("-s", $DeviceName, "shell", "dumpsys", "display") -TimeoutMilliseconds 15000
    if ([string]::IsNullOrWhiteSpace($displayDump)) {
        return $null
    }

    $overrideMatch = [regex]::Match($displayDump, 'mOverrideDisplayInfo=.*?\brotation (\d)')
    if ($overrideMatch.Success) {
        return [int]$overrideMatch.Groups[1].Value
    }

    $baseMatch = [regex]::Match($displayDump, 'mBaseDisplayInfo=.*?\brotation (\d)')
    if ($baseMatch.Success) {
        return [int]$baseMatch.Groups[1].Value
    }

    return $null
}

function Resolve-AndroidRequestedRotation {
    param(
        [string]$RequestedOrientation,
        [object]$PhysicalSize
    )

    if ($RequestedOrientation -ne "portrait" -and $RequestedOrientation -ne "landscape") {
        return $null
    }

    if ($null -ne $PhysicalSize) {
        $deviceIsLandscapeNative = $PhysicalSize.width -gt $PhysicalSize.height
        if ($RequestedOrientation -eq "landscape") {
            if ($deviceIsLandscapeNative) {
                return 0
            }
            return 1
        }

        if ($deviceIsLandscapeNative) {
            return 1
        }
        return 0
    }

    if ($RequestedOrientation -eq "landscape") {
        return 1
    }
    return 0
}

function Resolve-AndroidRotationOrientation {
    param(
        [Nullable[int]]$Rotation,
        [object]$PhysicalSize
    )

    if ($null -eq $Rotation) {
        return $null
    }

    $nativeOrientation = "portrait"
    if ($null -ne $PhysicalSize -and $PhysicalSize.width -gt $PhysicalSize.height) {
        $nativeOrientation = "landscape"
    }

    switch ([int]$Rotation) {
        0 { return $nativeOrientation }
        1 { return $(if ($nativeOrientation -eq "landscape") { "portrait" } else { "landscape" }) }
        2 { return $nativeOrientation }
        3 { return $(if ($nativeOrientation -eq "landscape") { "portrait" } else { "landscape" }) }
        default { return $null }
    }
}

function Resolve-AndroidDimensionsOrientation {
    param(
        [Nullable[int]]$Width,
        [Nullable[int]]$Height
    )

    if ($null -eq $Width -or $null -eq $Height) {
        return $null
    }

    if ($Width -gt $Height) {
        return "landscape"
    }
    if ($Height -gt $Width) {
        return "portrait"
    }
    return "square"
}

function Resolve-AndroidDeviceRole {
    param([Nullable[double]]$SmallestWidthDp)

    if ($null -eq $SmallestWidthDp) {
        return "unknown"
    }

    if ([double]$SmallestWidthDp -ge 600.0) {
        return "tablet"
    }

    return "phone"
}

function Resolve-AndroidDeviceFacts {
    param(
        [string]$AdbPath,
        [string]$DeviceName,
        [string]$RequestedOrientation
    )

    $physicalSize = Get-AndroidPhysicalDisplaySize -AdbPath $AdbPath -DeviceName $DeviceName
    $densityFacts = Get-AndroidDisplayDensity -AdbPath $AdbPath -DeviceName $DeviceName
    $currentRotation = Get-AndroidCurrentRotation -AdbPath $AdbPath -DeviceName $DeviceName
    $expectedRotation = Resolve-AndroidRequestedRotation -RequestedOrientation $RequestedOrientation -PhysicalSize $physicalSize

    $smallestWidthDp = $null
    if ($null -ne $physicalSize -and $null -ne $densityFacts -and $null -ne $densityFacts.density_dpi -and $densityFacts.density_dpi -gt 0) {
        $smallestSidePx = [Math]::Min([double]$physicalSize.width, [double]$physicalSize.height)
        $smallestWidthDp = [Math]::Round(($smallestSidePx * 160.0) / [double]$densityFacts.density_dpi, 2)
    }

    $nativeOrientation = $null
    if ($null -ne $physicalSize) {
        $nativeOrientation = Resolve-AndroidDimensionsOrientation -Width $physicalSize.width -Height $physicalSize.height
    }

    $currentOrientation = Resolve-AndroidRotationOrientation -Rotation $currentRotation -PhysicalSize $physicalSize
    $rotationMatchesRequested = $null
    $rotationMismatch = $null
    if ($null -ne $expectedRotation -and $null -ne $currentRotation) {
        $rotationMatchesRequested = ([int]$currentRotation -eq [int]$expectedRotation)
        $rotationMismatch = -not $rotationMatchesRequested
    }

    return [pscustomobject]@{
        device = $DeviceName
        physical_size_px = $physicalSize
        density_dpi = $(if ($null -ne $densityFacts) { $densityFacts.density_dpi } else { $null })
        physical_density_dpi = $(if ($null -ne $densityFacts) { $densityFacts.physical_density_dpi } else { $null })
        override_density_dpi = $(if ($null -ne $densityFacts) { $densityFacts.override_density_dpi } else { $null })
        smallest_width_dp = $smallestWidthDp
        resolved_role = Resolve-AndroidDeviceRole -SmallestWidthDp $smallestWidthDp
        native_orientation = $nativeOrientation
        requested_orientation = $(if ([string]::IsNullOrWhiteSpace($RequestedOrientation)) { $null } else { $RequestedOrientation })
        expected_rotation = $expectedRotation
        current_rotation = $currentRotation
        current_orientation = $currentOrientation
        rotation_matches_requested = $rotationMatchesRequested
        rotation_mismatch = $rotationMismatch
    }
}

function Get-AndroidScreenshotDimensions {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return $null
    }

    try {
        Add-Type -AssemblyName System.Drawing -ErrorAction SilentlyContinue | Out-Null
        $image = [System.Drawing.Image]::FromFile((Resolve-Path -LiteralPath $Path))
        try {
            return [pscustomobject]@{
                width = [int]$image.Width
                height = [int]$image.Height
            }
        } finally {
            $image.Dispose()
        }
    } catch {
        return $null
    }
}

function Get-AndroidScreenshotFacts {
    param(
        [string]$Path,
        [string]$RequestedOrientation
    )

    $dimensions = Get-AndroidScreenshotDimensions -Path $Path
    $actualOrientation = $null
    if ($null -ne $dimensions) {
        $actualOrientation = Resolve-AndroidDimensionsOrientation -Width $dimensions.width -Height $dimensions.height
    }

    $orientationMatchesRequested = $null
    $orientationMismatch = $null
    if (-not [string]::IsNullOrWhiteSpace($RequestedOrientation) -and -not [string]::IsNullOrWhiteSpace($actualOrientation)) {
        $orientationMatchesRequested = ($actualOrientation -eq $RequestedOrientation)
        $orientationMismatch = -not $orientationMatchesRequested
    }

    return [pscustomobject]@{
        path = $Path
        dimensions_px = $dimensions
        actual_orientation = $actualOrientation
        requested_orientation = $(if ([string]::IsNullOrWhiteSpace($RequestedOrientation)) { $null } else { $RequestedOrientation })
        orientation_matches_requested = $orientationMatchesRequested
        orientation_mismatch = $orientationMismatch
    }
}

function Resolve-AndroidHostInferenceUrlForDevice {
    param(
        [string]$AdbPath,
        [string]$DeviceName,
        [string]$Url
    )

    if ([string]::IsNullOrWhiteSpace($Url)) {
        return $Url
    }
    if ($DeviceName -like "emulator-*") {
        return $Url
    }

    try {
        $uri = [System.Uri]$Url
        if ($uri.Host -ne "10.0.2.2") {
            return $Url
        }
        $port = if ($uri.Port -gt 0) { $uri.Port } else { 80 }
        & $AdbPath -s $DeviceName reverse ("tcp:{0}" -f $port) ("tcp:{0}" -f $port) | Out-Null
        $builder = [System.UriBuilder]::new($uri)
        $builder.Host = "127.0.0.1"
        return $builder.Uri.AbsoluteUri
    } catch {
        return $Url
    }
}

function Write-AndroidHarnessZipBundle {
    param(
        [string]$SourceDirectory,
        [string]$DestinationZip
    )

    if (-not (Test-Path -LiteralPath $SourceDirectory)) {
        return $null
    }

    $entries = Get-ChildItem -LiteralPath $SourceDirectory -Force -ErrorAction SilentlyContinue
    if ($null -eq $entries -or $entries.Count -eq 0) {
        return $null
    }

    if (Test-Path -LiteralPath $DestinationZip) {
        Remove-Item -LiteralPath $DestinationZip -Force -ErrorAction SilentlyContinue
    }

    Compress-Archive -Path ($entries | ForEach-Object { $_.FullName }) -DestinationPath $DestinationZip -Force
    return $DestinationZip
}

Export-ModuleMember -Function Acquire-AndroidHarnessDeviceLock,Get-AndroidCurrentRotation,Get-AndroidDisplayDensity,Get-AndroidHostAdbPlatformToolsVersion,Get-AndroidPhysicalDisplaySize,Get-AndroidScreenshotDimensions,Get-AndroidScreenshotFacts,Invoke-AndroidAdbCommandCapture,Resolve-AndroidDeviceFacts,Resolve-AndroidDeviceRole,Resolve-AndroidDimensionsOrientation,Resolve-AndroidHarnessDeviceList,Resolve-AndroidHostInferenceUrlForDevice,Resolve-AndroidRequestedRotation,Write-AndroidHarnessZipBundle
