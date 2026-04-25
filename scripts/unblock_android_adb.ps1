param(
    [string]$SdkRoot = "$env:LOCALAPPDATA\Android\Sdk",
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"

function Add-PathSegment {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Segment
    )

    $currentUserPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $parts = @()
    if (-not [string]::IsNullOrWhiteSpace($currentUserPath)) {
        $parts = $currentUserPath -split ';' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    }

    $alreadyPresent = $false
    foreach ($part in $parts) {
        if ([string]::Equals($part.TrimEnd('\'), $Segment.TrimEnd('\'), [System.StringComparison]::OrdinalIgnoreCase)) {
            $alreadyPresent = $true
            break
        }
    }

    if (-not $alreadyPresent) {
        $newUserPath = if ([string]::IsNullOrWhiteSpace($currentUserPath)) {
            $Segment
        } else {
            "$Segment;$currentUserPath"
        }
        [Environment]::SetEnvironmentVariable("Path", $newUserPath, "User")
    }

    $processParts = $env:Path -split ';' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    $processAlreadyPresent = $false
    foreach ($part in $processParts) {
        if ([string]::Equals($part.TrimEnd('\'), $Segment.TrimEnd('\'), [System.StringComparison]::OrdinalIgnoreCase)) {
            $processAlreadyPresent = $true
            break
        }
    }

    if (-not $processAlreadyPresent) {
        $env:Path = "$Segment;$env:Path"
    }
}

$sdkManagerCandidates = @(
    (Join-Path $SdkRoot "cmdline-tools\latest\bin\sdkmanager.bat"),
    (Join-Path $SdkRoot "tools\bin\sdkmanager.bat")
)

$sdkManager = $sdkManagerCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
$platformTools = Join-Path $SdkRoot "platform-tools"
$adb = Join-Path $platformTools "adb.exe"

Write-Host "SDK root: $SdkRoot"

if (-not $SkipInstall) {
    if (-not $sdkManager) {
        throw @"
Could not find sdkmanager.bat.

Open Android Studio > Settings > Languages & Frameworks > Android SDK > SDK Tools,
install Android SDK Command-line Tools and Android SDK Platform-Tools, then rerun this script.
"@
    }

    Write-Host "Installing Android SDK Platform-Tools with: $sdkManager"
    & $sdkManager --install "platform-tools"
    if ($LASTEXITCODE -ne 0) {
        throw "sdkmanager failed with exit code $LASTEXITCODE"
    }
}

if (-not (Test-Path -LiteralPath $adb)) {
    throw "adb.exe still not found at $adb"
}

[Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", $SdkRoot, "User")
$env:ANDROID_SDK_ROOT = $SdkRoot
Add-PathSegment -Segment $platformTools

Write-Host ""
Write-Host "ADB is available at: $adb"
Write-Host ""
& $adb version
Write-Host ""
& $adb devices

Write-Host ""
Write-Host "If devices are listed as unauthorized, unlock the device/emulator and accept the USB debugging prompt."
Write-Host "Open a new terminal after this if another shell still cannot find adb by name."
