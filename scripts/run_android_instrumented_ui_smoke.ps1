param(
    [string]$Device = "RFCX607ZM8L",
    [string]$TestClass = "com.senku.mobile.PromptHarnessSmokeTest",
    [switch]$BuildOnly,
    [switch]$SkipBuild,
    [switch]$SkipInstall,
    [string]$ScriptedQuery = "",
    [switch]$ScriptedAsk,
    [string]$ScriptedFollowUpQuery = "",
    [ValidateSet("detail", "results")]
    [string]$ScriptedExpectedSurface = "detail",
    [string]$ScriptedExpectedTitle = "",
    [string]$ScriptedCaptureLabel = "",
    [string]$ScriptedRequiredResId = "",
    [int]$ScriptedTimeoutMs = 0,
    [int]$ScriptedExtraSettleMs = 0,
    [switch]$ScriptedEnableReviewedCardRuntime,
    [string]$ScriptedExpectedAnswerSurfaceLabel = "",
    [Alias("ScriptedForbiddenAnswerSurfaceLabel")]
    [string]$ScriptedForbiddenAnswerSurfaceLabels = "",
    [string]$ScriptedExpectedRuleId = "",
    [string]$ScriptedExpectedSourceGuideId = "",
    [string]$ScriptedExpectedReviewedCardId = "",
    [string]$ScriptedExpectedReviewedCardGuideId = "",
    [string]$ScriptedExpectedReviewedCardReviewStatus = "",
    [string]$ScriptedExpectedReviewedCardSourceGuideIds = "",
    [switch]$ScriptedAssertRecentThreadReviewedCardMetadata,
    [string]$ScriptedExpectedBodyContains = "",
    [switch]$AllowHostFallback,
    [switch]$EnableHostInferenceSmoke,
    [switch]$EnableFollowUpSmoke,
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string]$ArtifactRoot = "artifacts/instrumented_ui_smoke",
    [ValidateSet("", "phone-basic", "phone-host", "phone-full", "phone-functional", "phone-functional-follow-up", "phone-functional-saved", "phone-functional-back-provenance", "tablet-functional-rail", "tablet-functional-header", "tablet-landscape", "large-font", "tablet-large-font")]
    [string]$SmokePreset = "",
    [ValidateSet("basic", "host", "full", "functional", "functional-follow-up", "functional-saved", "functional-back-provenance", "tablet-functional-rail", "tablet-functional-header", "custom")]
    [string]$SmokeProfile = "basic",
    [ValidateSet("portrait", "landscape")]
    [string]$Orientation = "portrait",
    [double]$FontScale = 1.0,
    [switch]$CaptureLogcat,
    [switch]$ClearLogcatBeforeRun,
    [string]$LogcatSpec = "SenkuPackRepo:D SenkuMobile:D AndroidJUnitRunner:D TestRunner:D *:S",
    [string]$SummaryPath = "",
    [string]$CaptureSummaryPath = "",
    [switch]$SkipDeviceLock
)

$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$androidRoot = Join-Path $repoRoot "android-app"
$gradlew = Join-Path $androidRoot "gradlew.bat"
$adb = Join-Path $env:LOCALAPPDATA "Android\\Sdk\\platform-tools\\adb.exe"
$lockRoot = Join-Path $repoRoot "artifacts\\harness_locks"
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"

if (-not (Test-Path -LiteralPath $gradlew)) {
    throw "gradlew.bat not found at $gradlew"
}
if (-not (Test-Path -LiteralPath $adb)) {
    throw "adb not found at $adb"
}
if (-not (Test-Path -LiteralPath $commonHarnessModule)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}
Import-Module $commonHarnessModule -Force -DisableNameChecking
New-Item -ItemType Directory -Force -Path $lockRoot | Out-Null

function Write-RunPhase {
    param([string]$Phase)

    Write-Host ("[instrumented-ui-smoke:{0}] {1}" -f $Device, $Phase)
}

function Acquire-DeviceLock {
    param(
        [string]$DeviceName,
        [int]$TimeoutSeconds = 900
    )

    return Acquire-AndroidHarnessDeviceLock -DeviceName $DeviceName -LockRoot $lockRoot -TimeoutSeconds $TimeoutSeconds -ProgressLabel ("[instrumented-ui-smoke:{0}]" -f $DeviceName)
}

$deviceLock = $null
if (-not $SkipDeviceLock) {
    Write-RunPhase -Phase "acquiring device lock"
    $deviceLock = Acquire-DeviceLock -DeviceName $Device
    Write-RunPhase -Phase "device lock acquired"
} else {
    Write-RunPhase -Phase "device lock skipped"
}

Push-Location $androidRoot
try {
    if (-not $SkipBuild) {
        Write-RunPhase -Phase "building debug APKs"
        & $gradlew :app:assembleDebug :app:assembleDebugAndroidTest
        if ($LASTEXITCODE -ne 0) {
            throw "Gradle build failed"
        }
        Write-RunPhase -Phase "build completed"
    } else {
        Write-RunPhase -Phase "build skipped"
    }
} finally {
    Pop-Location
}

if ($BuildOnly) {
    if ($deviceLock) {
        $deviceLock.Dispose()
        $deviceLock = $null
    }
    Write-Output ([pscustomobject]@{
        built = (-not $SkipBuild)
        build_skipped = [bool]$SkipBuild
        device = $Device
        test_class = $TestClass
        app_apk = (Join-Path $androidRoot "app\\build\\outputs\\apk\\debug\\app-debug.apk")
        test_apk = (Join-Path $androidRoot "app\\build\\outputs\\apk\\androidTest\\debug\\app-debug-androidTest.apk")
    } | ConvertTo-Json -Depth 4)
    return
}

$appApk = Join-Path $androidRoot "app\\build\\outputs\\apk\\debug\\app-debug.apk"
$testApk = Join-Path $androidRoot "app\\build\\outputs\\apk\\androidTest\\debug\\app-debug-androidTest.apk"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss_fff"
$resolvedArtifactRoot = if ([System.IO.Path]::IsPathRooted($ArtifactRoot)) {
    $ArtifactRoot
} else {
    Join-Path $repoRoot $ArtifactRoot
}
$artifactDir = Join-Path $resolvedArtifactRoot (Join-Path $timestamp $Device)
$screenshotDir = Join-Path $artifactDir "screenshots"
$dumpDir = Join-Path $artifactDir "dumps"
$harnessStateDir = Join-Path $repoRoot "artifacts\\harness_state"
$installStatePath = Join-Path $harnessStateDir ("instrumented_ui_smoke_" + $Device + ".json")
$identityStatePath = Join-Path $harnessStateDir ("instrumented_ui_smoke_identity_" + $Device + ".json")
$apkInstallTimeoutMilliseconds = 180000
$script:IdentityCacheHit = $false
New-Item -ItemType Directory -Force -Path $screenshotDir | Out-Null
New-Item -ItemType Directory -Force -Path $dumpDir | Out-Null
New-Item -ItemType Directory -Force -Path $harnessStateDir | Out-Null
$runStartedUtc = (Get-Date).ToUniversalTime()
$runStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

foreach ($apk in @($appApk, $testApk)) {
    if (-not (Test-Path -LiteralPath $apk)) {
        throw "Expected APK missing: $apk"
    }
}

function Resolve-HostInferenceUrlForDevice {
    param([string]$Url)

    if (-not $EnableHostInferenceSmoke -or [string]::IsNullOrWhiteSpace($Url)) {
        return $Url
    }

    if ($Device -like "emulator-*") {
        return $Url
    }

    try {
        $resolved = Resolve-AndroidHostInferenceUrlForDevice -AdbPath $adb -DeviceName $Device -Url $Url
        if ($resolved -ne $Url) {
            Write-Host ("Physical device {0}: using adb reverse for host inference {1} -> {2}" -f $Device, $Url, $resolved)
        }
        return $resolved
    } catch {
        Write-Warning ("Could not prepare physical-device host inference URL '{0}': {1}" -f $Url, $_.Exception.Message)
        return $Url
    }
}

function Get-ApkFingerprint {
    param([string]$Path)

    $item = Get-Item -LiteralPath $Path
    return [pscustomobject]@{
        path = $Path
        length = [int64]$item.Length
        last_write_utc = $item.LastWriteTimeUtc.ToString("o")
        sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
    }
}

function Get-HostAdbPlatformToolsVersion {
    param([string]$AdbPath)

    $result = Invoke-AndroidAdbCommandCapture -AdbPath $AdbPath -Arguments @("version") -TimeoutMilliseconds 10000
    if ($result.exit_code -ne 0 -or [string]::IsNullOrWhiteSpace([string]$result.output)) {
        return $null
    }

    $lines = @(([string]$result.output -split "`r?`n") | ForEach-Object { $_.Trim() } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    $platformToolsLine = @($lines | Where-Object { $_ -match "^Version\s+(.+)$" } | Select-Object -First 1)
    if ($platformToolsLine.Count -gt 0 -and $platformToolsLine[0] -match "^Version\s+(.+)$") {
        return $Matches[1]
    }

    if ($lines.Count -eq 0) {
        return $null
    }

    $line = $lines[0]
    if ($line -match "Android Debug Bridge version\s+([0-9A-Za-z.\-]+)") {
        return $Matches[1]
    }
    return $line
}

$hostAdbPlatformToolsVersion = Get-HostAdbPlatformToolsVersion -AdbPath $adb

function Invoke-AdbChecked {
    param(
        [string[]]$Arguments,
        [string]$FailureMessage
    )

    $output = & $adb @Arguments 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        $details = $output.Trim()
        if ([string]::IsNullOrWhiteSpace($details)) {
            throw $FailureMessage
        }
        throw ("{0}: {1}" -f $FailureMessage, $details)
    }
    return $output
}

function Invoke-AdbCheckedWithTimeout {
    param(
        [string[]]$Arguments,
        [string]$FailureMessage,
        [int]$TimeoutMilliseconds
    )

    $result = Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $Arguments -TimeoutMilliseconds $TimeoutMilliseconds
    $output = if ($null -eq $result.output) { "" } else { [string]$result.output }
    if ($result.timed_out) {
        $commandText = $Arguments -join " "
        throw ("{0}: adb {1} timed out after {2} ms" -f $FailureMessage, $commandText, $TimeoutMilliseconds)
    }
    if ($result.exit_code -ne 0) {
        $details = $output.Trim()
        if ([string]::IsNullOrWhiteSpace($details)) {
            throw $FailureMessage
        }
        throw ("{0}: {1}" -f $FailureMessage, $details)
    }
    return $output
}

function Invoke-ApkInstallWithPhysicalNoStreamingFallback {
    param(
        [string]$ApkPath,
        [string]$FailureMessage,
        [string]$ApkLabel = "APK",
        [int]$TimeoutMilliseconds = 180000
    )

    try {
        Write-RunPhase -Phase ("installing {0} with adb install -r (timeout {1} ms)" -f $ApkLabel, $TimeoutMilliseconds)
        [void](Invoke-AdbCheckedWithTimeout -Arguments @("-s", $Device, "install", "-r", $ApkPath) -FailureMessage $FailureMessage -TimeoutMilliseconds $TimeoutMilliseconds)
        return
    } catch {
        $streamingInstallError = $_.Exception.Message
        if ($Device -like "emulator-*") {
            throw
        }
    }

    $script:InstallNoStreamingFallbackAttempted = $true
    Write-Warning ("{0}; retrying with adb install --no-streaming on physical device {2} for {1}" -f $streamingInstallError, $ApkLabel, $Device)
    try {
        Write-RunPhase -Phase ("installing {0} with adb install --no-streaming -r (timeout {1} ms)" -f $ApkLabel, $TimeoutMilliseconds)
        [void](Invoke-AdbCheckedWithTimeout -Arguments @("-s", $Device, "install", "--no-streaming", "-r", $ApkPath) -FailureMessage $FailureMessage -TimeoutMilliseconds $TimeoutMilliseconds)
    } catch {
        throw ("{0}; --no-streaming retry failed: {1}" -f $streamingInstallError, $_.Exception.Message)
    }
}

function Test-PackageInstalled {
    param([string]$PackageName)

    $packagePath = (& $adb -s $Device shell pm path $PackageName 2>$null | Out-String).Trim()
    return -not [string]::IsNullOrWhiteSpace($packagePath)
}

function Test-InstallCacheMatch {
    param(
        [string]$StatePath,
        [object]$AppFingerprint,
        [object]$TestFingerprint
    )

    if (-not (Test-Path -LiteralPath $StatePath)) {
        return $false
    }
    if (-not (Test-PackageInstalled -PackageName "com.senku.mobile")) {
        return $false
    }
    if (-not (Test-PackageInstalled -PackageName "com.senku.mobile.test")) {
        return $false
    }
    try {
        $state = Get-Content -LiteralPath $StatePath -Raw | ConvertFrom-Json
        return ($state.device -eq $Device) -and
            (-not [string]::IsNullOrWhiteSpace([string]$state.app.sha256)) -and
            (-not [string]::IsNullOrWhiteSpace([string]$state.test.sha256)) -and
            ([string]$state.app.sha256 -eq [string]$AppFingerprint.sha256) -and
            ([string]$state.test.sha256 -eq [string]$TestFingerprint.sha256) -and
            ($state.app.length -eq $AppFingerprint.length) -and
            ($state.app.last_write_utc -eq $AppFingerprint.last_write_utc) -and
            ($state.test.length -eq $TestFingerprint.length) -and
            ($state.test.last_write_utc -eq $TestFingerprint.last_write_utc)
    } catch {
        return $false
    }
}

function Convert-ToShellSingleQuotedLiteral {
    param([string]$Value)

    if ($null -eq $Value) {
        return "''"
    }
    return "'" + $Value.Replace("'", "'""'""'") + "'"
}

function Get-InstalledPackageCodePath {
    param([string]$PackageName)

    $packageOutput = (& $adb -s $Device shell pm path $PackageName 2>$null | Out-String)
    $paths = @()
    foreach ($line in ($packageOutput -split "`r?`n")) {
        $trimmed = $line.Trim()
        if ($trimmed -notlike "package:*") {
            continue
        }
        $candidate = $trimmed.Substring(8).Trim()
        if (-not [string]::IsNullOrWhiteSpace($candidate)) {
            $paths += $candidate
        }
    }

    $basePath = @($paths | Where-Object { $_ -like "*/base.apk" } | Select-Object -First 1)
    if ($basePath.Count -gt 0) {
        return [string]$basePath[0]
    }

    $firstPath = @($paths | Select-Object -First 1)
    if ($firstPath.Count -gt 0) {
        return [string]$firstPath[0]
    }

    return $null
}

function Get-RemoteFileListingSignature {
    param(
        [string]$Path,
        [string]$RunAsPackage = ""
    )

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }

    try {
        $statArguments = if ([string]::IsNullOrWhiteSpace($RunAsPackage)) {
            @("-s", $Device, "shell", "stat", "-c", "%n|%s|%Y|%f|%i", $Path)
        } else {
            @("-s", $Device, "shell", "run-as", $RunAsPackage, "stat", "-c", "%n|%s|%Y|%f|%i", $Path)
        }
        $statOutput = Invoke-AdbChecked -Arguments $statArguments -FailureMessage ("Remote stat failed for {0}" -f $Path)
        $statTrimmed = $statOutput.Trim()
        if (-not [string]::IsNullOrWhiteSpace($statTrimmed)) {
            return "stat:$statTrimmed"
        }
    } catch {
    }

    try {
        $arguments = if ([string]::IsNullOrWhiteSpace($RunAsPackage)) {
            @("-s", $Device, "shell", "ls", "-l", $Path)
        } else {
            @("-s", $Device, "shell", "run-as", $RunAsPackage, "ls", "-l", $Path)
        }
        $output = Invoke-AdbChecked -Arguments $arguments -FailureMessage ("Remote listing failed for {0}" -f $Path)
        $trimmed = $output.Trim()
        return $(if ([string]::IsNullOrWhiteSpace($trimmed)) { $null } else { $trimmed })
    } catch {
        return $null
    }
}

function Get-RemoteSha256 {
    param(
        [string]$Path,
        [string]$RunAsPackage = ""
    )

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }

    try {
        $tempRoot = Join-Path $env:TEMP "senku_remote_hash"
        New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
        $tempPath = Join-Path $tempRoot ([System.Guid]::NewGuid().ToString("N") + ".bin")

        $copyResult = if ([string]::IsNullOrWhiteSpace($RunAsPackage)) {
            Invoke-AdbStreamingCopy -Arguments @("-s", $Device, "exec-out", "cat", $Path) -DestinationPath $tempPath -TimeoutSeconds 240
        } else {
            Invoke-AdbStreamingCopy -Arguments @("-s", $Device, "exec-out", "run-as", $RunAsPackage, "cat", $Path) -DestinationPath $tempPath -TimeoutSeconds 240
        }
        if ($copyResult.exit_code -ne 0) {
            return $null
        }

        if (-not (Test-Path -LiteralPath $tempPath)) {
            return $null
        }
        $tempItem = Get-Item -LiteralPath $tempPath -ErrorAction SilentlyContinue
        if ($null -eq $tempItem -or $tempItem.Length -le 0) {
            return $null
        }

        return (Get-FileHash -Algorithm SHA256 -LiteralPath $tempPath).Hash.ToLowerInvariant()
    } catch {
        return $null
    } finally {
        if (-not [string]::IsNullOrWhiteSpace([string]$tempPath)) {
            Remove-Item -LiteralPath $tempPath -Force -ErrorAction SilentlyContinue
        }
    }
}

function Get-StableIdentitySha256 {
    param([string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return $null
    }

    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($Value)
        return -join ($sha256.ComputeHash($bytes) | ForEach-Object { $_.ToString("x2") })
    } finally {
        $sha256.Dispose()
    }
}

function Get-AppModelMetadata {
    $knownNames = @(
        "gemma-4-E4B-it.litertlm",
        "gemma-4-E4B-it.task",
        "gemma-4-E2B-it.litertlm",
        "gemma-4-E2B-it.task"
    )
    $candidateNames = New-Object System.Collections.Generic.List[string]
    $listedNames = New-Object System.Collections.Generic.List[string]

    try {
        $prefsXml = Invoke-AdbChecked -Arguments @(
            "-s", $Device, "shell", "run-as", "com.senku.mobile", "cat", "shared_prefs/senku_model_store.xml"
        ) -FailureMessage "Model preference probe failed"
        if ($prefsXml -match '<string name="model_name">([^<]+)</string>') {
            $preferredName = $matches[1].Trim()
            if (-not [string]::IsNullOrWhiteSpace($preferredName)) {
                $candidateNames.Add($preferredName)
            }
        }
    } catch {
    }

    try {
        $listing = Invoke-AdbChecked -Arguments @(
            "-s", $Device, "shell", "run-as", "com.senku.mobile", "ls", "files/models"
        ) -FailureMessage "Model directory listing failed"
        foreach ($line in ($listing -split "`r?`n")) {
            $trimmed = $line.Trim()
            if ([string]::IsNullOrWhiteSpace($trimmed)) {
                continue
            }
            if ($trimmed.EndsWith(".litertlm") -or $trimmed.EndsWith(".task")) {
                $listedNames.Add($trimmed)
            }
        }
    } catch {
    }

    foreach ($knownName in $knownNames) {
        if ($listedNames -contains $knownName -and -not ($candidateNames -contains $knownName)) {
            $candidateNames.Add($knownName)
        }
    }
    foreach ($listedName in $listedNames) {
        if (-not ($candidateNames -contains $listedName)) {
            $candidateNames.Add($listedName)
        }
    }

    $modelName = $null
    foreach ($candidateName in $candidateNames) {
        if ([string]::IsNullOrWhiteSpace($candidateName)) {
            continue
        }
        $modelName = $candidateName
        break
    }

    if ([string]::IsNullOrWhiteSpace($modelName)) {
        return [pscustomobject]@{
            name = $null
            path = $null
            listing_signature = $null
        }
    }

    $relativePath = "files/models/$modelName"
    return [pscustomobject]@{
        name = $modelName
        path = $relativePath
        listing_signature = Get-RemoteFileListingSignature -Path $relativePath -RunAsPackage "com.senku.mobile"
    }
}

function Get-InstalledPackMetadata {
    $packDir = "files/mobile_pack"
    $manifestRelativePath = "$packDir/senku_manifest.json"
    try {
        $manifestText = Invoke-AdbChecked -Arguments @(
            "-s", $Device, "shell", "run-as", "com.senku.mobile", "cat", $manifestRelativePath
        ) -FailureMessage "Installed pack manifest probe failed"
        if ([string]::IsNullOrWhiteSpace($manifestText)) {
            return [pscustomobject]@{
                status = "missing"
                manifest_path = $manifestRelativePath
            }
        }
        $manifest = $manifestText | ConvertFrom-Json
        $sqliteName = [string]$manifest.files.sqlite.path
        $vectorName = [string]$manifest.files.vectors.path
        $sqlitePath = if ([string]::IsNullOrWhiteSpace($sqliteName)) { $null } else { "$packDir/$sqliteName" }
        $vectorPath = if ([string]::IsNullOrWhiteSpace($vectorName)) { $null } else { "$packDir/$vectorName" }

        return [pscustomobject]@{
            status = "available"
            manifest_path = $manifestRelativePath
            pack_format = $(if ($null -ne $manifest.pack_format) { [string]$manifest.pack_format } else { $null })
            pack_version = $(if ($null -ne $manifest.pack_version) { [int]$manifest.pack_version } else { $null })
            generated_at = $(if ($null -ne $manifest.generated_at) { [string]$manifest.generated_at } else { $null })
            counts = [pscustomobject]@{
                guides = $(if ($null -ne $manifest.counts.guides) { [int]$manifest.counts.guides } else { $null })
                chunks = $(if ($null -ne $manifest.counts.chunks) { [int]$manifest.counts.chunks } else { $null })
                deterministic_rules = $(if ($null -ne $manifest.counts.deterministic_rules) { [int]$manifest.counts.deterministic_rules } else { $null })
                guide_related_links = $(if ($null -ne $manifest.counts.guide_related_links) { [int]$manifest.counts.guide_related_links } else { $null })
                retrieval_metadata_guides = $(if ($null -ne $manifest.counts.retrieval_metadata_guides) { [int]$manifest.counts.retrieval_metadata_guides } else { $null })
                answer_cards = $(if ($null -ne $manifest.counts.answer_cards) { [int]$manifest.counts.answer_cards } else { $null })
                answer_card_clauses = $(if ($null -ne $manifest.counts.answer_card_clauses) { [int]$manifest.counts.answer_card_clauses } else { $null })
                answer_card_sources = $(if ($null -ne $manifest.counts.answer_card_sources) { [int]$manifest.counts.answer_card_sources } else { $null })
            }
            sqlite = [pscustomobject]@{
                path = $sqlitePath
                manifest_bytes = $(if ($null -ne $manifest.files.sqlite.bytes) { [long]$manifest.files.sqlite.bytes } else { $null })
                manifest_sha256 = $(if ($null -ne $manifest.files.sqlite.sha256) { [string]$manifest.files.sqlite.sha256 } else { $null })
                listing_signature = $(if ([string]::IsNullOrWhiteSpace($sqlitePath)) { $null } else { Get-RemoteFileListingSignature -Path $sqlitePath -RunAsPackage "com.senku.mobile" })
            }
            vectors = [pscustomobject]@{
                path = $vectorPath
                manifest_bytes = $(if ($null -ne $manifest.files.vectors.bytes) { [long]$manifest.files.vectors.bytes } else { $null })
                manifest_sha256 = $(if ($null -ne $manifest.files.vectors.sha256) { [string]$manifest.files.vectors.sha256 } else { $null })
                listing_signature = $(if ([string]::IsNullOrWhiteSpace($vectorPath)) { $null } else { Get-RemoteFileListingSignature -Path $vectorPath -RunAsPackage "com.senku.mobile" })
            }
        }
    } catch {
        return [pscustomobject]@{
            status = "unavailable"
            manifest_path = $manifestRelativePath
            error = $_.Exception.Message
        }
    }
}

function Resolve-InstalledBinaryIdentity {
    param([string]$CachePath)

    $packagePath = Get-InstalledPackageCodePath -PackageName "com.senku.mobile"
    $packageSignature = Get-RemoteFileListingSignature -Path $packagePath
    $modelMetadata = Get-AppModelMetadata
    $cachedIdentity = $null
    if (Test-Path -LiteralPath $CachePath) {
        try {
            $cachedIdentity = Get-Content -LiteralPath $CachePath -Raw | ConvertFrom-Json
        } catch {
            $cachedIdentity = $null
        }
    }

    $cacheCanMatchModel = if ([string]::IsNullOrWhiteSpace([string]$modelMetadata.name)) {
        [string]::IsNullOrWhiteSpace([string]$cachedIdentity.model_name)
    } else {
        (-not [string]::IsNullOrWhiteSpace([string]$modelMetadata.listing_signature)) -and
            ([string]$cachedIdentity.model_name -eq [string]$modelMetadata.name) -and
            ([string]$cachedIdentity.model_signature -eq [string]$modelMetadata.listing_signature)
    }

    $script:IdentityCacheHit = $false
    $cacheMatches = ($null -ne $cachedIdentity) -and
        ([string]$cachedIdentity.device -eq $Device) -and
        (-not [string]::IsNullOrWhiteSpace($packagePath)) -and
        (-not [string]::IsNullOrWhiteSpace($packageSignature)) -and
        ([string]$cachedIdentity.package_path -eq $packagePath) -and
        ([string]$cachedIdentity.package_signature -eq $packageSignature) -and
        $cacheCanMatchModel

    if ($cacheMatches -and -not [string]::IsNullOrWhiteSpace([string]$cachedIdentity.apk_sha)) {
        if ([string]::IsNullOrWhiteSpace([string]$modelMetadata.name) -or -not [string]::IsNullOrWhiteSpace([string]$cachedIdentity.model_sha)) {
            $script:IdentityCacheHit = $true
            return [pscustomobject]@{
                device = $Device
                package_path = $packagePath
                package_signature = $packageSignature
                apk_sha = [string]$cachedIdentity.apk_sha
                model_name = $(if ([string]::IsNullOrWhiteSpace([string]$modelMetadata.name)) { $null } else { [string]$modelMetadata.name })
                model_path = $(if ([string]::IsNullOrWhiteSpace([string]$modelMetadata.path)) { $null } else { [string]$modelMetadata.path })
                model_signature = $(if ([string]::IsNullOrWhiteSpace([string]$modelMetadata.listing_signature)) { $null } else { [string]$modelMetadata.listing_signature })
                model_sha = $(if ([string]::IsNullOrWhiteSpace([string]$cachedIdentity.model_sha)) { $null } else { [string]$cachedIdentity.model_sha })
                recorded_at = [string]$cachedIdentity.recorded_at
            }
        }
    }

    $apkSha = if ([string]::IsNullOrWhiteSpace($packagePath)) { $null } else { Get-RemoteSha256 -Path $packagePath }
    if (-not [string]::IsNullOrWhiteSpace($packagePath) -and [string]::IsNullOrWhiteSpace($apkSha)) {
        throw "Unable to hash installed APK at $packagePath on $Device"
    }

    $modelSha = if ([string]::IsNullOrWhiteSpace([string]$modelMetadata.path)) {
        $null
    } else {
        Get-RemoteSha256 -Path ([string]$modelMetadata.path) -RunAsPackage "com.senku.mobile"
    }
    if (-not [string]::IsNullOrWhiteSpace([string]$modelMetadata.path) -and [string]::IsNullOrWhiteSpace($modelSha)) {
        throw "Unable to hash installed model at $([string]$modelMetadata.path) on $Device"
    }

    $resolvedIdentity = [ordered]@{
        device = $Device
        package_path = $packagePath
        package_signature = $packageSignature
        apk_sha = $apkSha
        model_name = $(if ([string]::IsNullOrWhiteSpace([string]$modelMetadata.name)) { $null } else { [string]$modelMetadata.name })
        model_path = $(if ([string]::IsNullOrWhiteSpace([string]$modelMetadata.path)) { $null } else { [string]$modelMetadata.path })
        model_signature = $(if ([string]::IsNullOrWhiteSpace([string]$modelMetadata.listing_signature)) { $null } else { [string]$modelMetadata.listing_signature })
        model_sha = $modelSha
        recorded_at = (Get-Date).ToUniversalTime().ToString("o")
    }
    $identityObject = [pscustomobject]$resolvedIdentity
    try {
        ($identityObject | ConvertTo-Json -Depth 6) | Set-Content -LiteralPath $CachePath -Encoding UTF8
    } catch {
    }
    return $identityObject
}

function Resolve-SummaryModelIdentity {
    param([object]$InstalledIdentity)

    $installedModelName = if ($null -ne $InstalledIdentity) { [string]$InstalledIdentity.model_name } else { "" }
    $installedModelSha = if ($null -ne $InstalledIdentity) { [string]$InstalledIdentity.model_sha } else { "" }
    if (-not [string]::IsNullOrWhiteSpace($installedModelSha)) {
        return [pscustomobject]@{
            source = "installed_model"
            name = $(if ([string]::IsNullOrWhiteSpace($installedModelName)) { $null } else { $installedModelName })
            sha = $installedModelSha
        }
    }

    if ($EnableHostInferenceSmoke -and
        -not [string]::IsNullOrWhiteSpace($EffectiveHostInferenceUrl) -and
        -not [string]::IsNullOrWhiteSpace($HostInferenceModel)) {
        $identityInput = "host-inference|$EffectiveHostInferenceUrl|$HostInferenceModel"
        return [pscustomobject]@{
            source = "host_inference"
            name = $HostInferenceModel
            sha = Get-StableIdentitySha256 -Value $identityInput
        }
    }

    return [pscustomobject]@{
        source = $null
        name = $null
        sha = $null
    }
}

function Get-LocalFileSha256 {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        return $null
    }
    return (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
}

function New-CaptureSummaryArtifact {
    param(
        [string]$Name,
        [string]$Path
    )

    $sha256 = Get-LocalFileSha256 -Path $Path
    if ([string]::IsNullOrWhiteSpace($sha256)) {
        throw "Cannot write canonical capture summary: required $Name artifact is missing at $Path"
    }

    return [pscustomobject]@{
        path = $Path
        sha256 = $sha256
    }
}

function Resolve-CaptureSummaryFirstPath {
    param(
        [string]$Directory,
        [string[]]$FileNames
    )

    foreach ($fileName in $FileNames) {
        if ([string]::IsNullOrWhiteSpace($fileName)) {
            continue
        }
        $candidate = Join-Path $Directory $fileName
        if (Test-Path -LiteralPath $candidate -PathType Leaf) {
            return $candidate
        }
    }
    return $null
}

function Convert-InstalledPackMetadataForCaptureSummary {
    param([object]$InstalledPack)

    if ($null -eq $InstalledPack) {
        return [pscustomobject]@{
            status = "not_provided"
            pack_format = "not_provided"
            pack_version = 0
        }
    }

    $packFormat = if ([string]::IsNullOrWhiteSpace([string]$InstalledPack.pack_format)) { "not_provided" } else { [string]$InstalledPack.pack_format }
    $packVersion = 0
    if ($null -ne $InstalledPack.pack_version) {
        try {
            $packVersion = [int]$InstalledPack.pack_version
        } catch {
            $packVersion = 0
        }
    }

    return [pscustomobject]@{
        status = $(if ([string]::IsNullOrWhiteSpace([string]$InstalledPack.status)) { "not_provided" } else { [string]$InstalledPack.status })
        pack_format = $packFormat
        pack_version = $packVersion
        metadata = $InstalledPack
    }
}

function Resolve-CaptureSummaryWindowSizeClass {
    param([object]$DeviceFacts)

    if ($null -eq $DeviceFacts -or $null -eq $DeviceFacts.smallest_width_dp) {
        return "not_provided"
    }

    $smallestWidthDp = [double]$DeviceFacts.smallest_width_dp
    if ($smallestWidthDp -ge 840.0) {
        return "expanded"
    }
    if ($smallestWidthDp -ge 600.0) {
        return "medium"
    }
    return "compact"
}

function Convert-CaptureSummaryViewportFacts {
    param(
        [object]$DeviceFacts,
        [object]$ArtifactFacts
    )

    $dimensions = $null
    if ($null -ne $ArtifactFacts -and $null -ne $ArtifactFacts.first_screenshot -and $null -ne $ArtifactFacts.first_screenshot.dimensions_px) {
        $dimensions = $ArtifactFacts.first_screenshot.dimensions_px
    } elseif ($null -ne $DeviceFacts -and $null -ne $DeviceFacts.physical_size_px) {
        $dimensions = $DeviceFacts.physical_size_px
    }

    return [pscustomobject]@{
        width = $(if ($null -ne $dimensions -and $null -ne $dimensions.width) { [int]$dimensions.width } else { 0 })
        height = $(if ($null -ne $dimensions -and $null -ne $dimensions.height) { [int]$dimensions.height } else { 0 })
        density = $(if ($null -ne $DeviceFacts -and $null -ne $DeviceFacts.density_dpi) { [double]$DeviceFacts.density_dpi } else { 0 })
        font_scale = [double]$FontScale
        window_size_class = Resolve-CaptureSummaryWindowSizeClass -DeviceFacts $DeviceFacts
        source = "run_android_instrumented_ui_smoke.ps1"
    }
}

function Write-AndroidInstrumentedCaptureSummary {
    param(
        [string]$Path,
        [string[]]$ScreenshotFiles,
        [string[]]$DumpFiles,
        [string]$LogcatFilePath,
        [object]$InstalledPack,
        [object]$ModelIdentity,
        [object]$DeviceFacts,
        [object]$ArtifactFacts,
        [string]$ResolvedApkSha
    )

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return
    }

    $firstScreenshotPath = Resolve-CaptureSummaryFirstPath -Directory $screenshotDir -FileNames $ScreenshotFiles
    $firstDumpPath = Resolve-CaptureSummaryFirstPath -Directory $dumpDir -FileNames $DumpFiles
    $logcatArtifactPath = if ([string]::IsNullOrWhiteSpace($LogcatFilePath)) { $null } else { $LogcatFilePath }
    $captureRole = if (-not [string]::IsNullOrWhiteSpace($SmokePreset)) { $SmokePreset } else { $SmokeProfile }

    $captureSummary = [pscustomobject]@{
        schema_version = 1
        serial = $Device
        role = $captureRole
        orientation = $Orientation
        apk_sha256 = $(if ([string]::IsNullOrWhiteSpace($ResolvedApkSha)) { "not_provided" } else { $ResolvedApkSha })
        platform_tools_version = $(if ([string]::IsNullOrWhiteSpace($hostAdbPlatformToolsVersion)) { "not_provided" } else { $hostAdbPlatformToolsVersion })
        artifacts = [pscustomobject]@{
            screenshot = New-CaptureSummaryArtifact -Name "screenshot" -Path $firstScreenshotPath
            ui_dump = New-CaptureSummaryArtifact -Name "ui_dump" -Path $firstDumpPath
            logcat = New-CaptureSummaryArtifact -Name "logcat" -Path $logcatArtifactPath
        }
        package_data_posture = [pscustomobject]@{
            cleared_before_capture = $false
            restored_after_capture = $false
            description = "instrumented smoke preserves caller/device package data unless setup steps change it"
        }
        model_identity = [pscustomobject]@{
            name = $(if ($null -eq $ModelIdentity -or [string]::IsNullOrWhiteSpace([string]$ModelIdentity.name)) { "not_provided" } else { [string]$ModelIdentity.name })
            sha256 = $(if ($null -eq $ModelIdentity -or [string]::IsNullOrWhiteSpace([string]$ModelIdentity.sha)) { "not_provided" } else { [string]$ModelIdentity.sha })
        }
        installed_pack_metadata = Convert-InstalledPackMetadataForCaptureSummary -InstalledPack $InstalledPack
        viewport_facts = Convert-CaptureSummaryViewportFacts -DeviceFacts $DeviceFacts -ArtifactFacts $ArtifactFacts
        evidence_posture = [pscustomobject]@{
            non_acceptance_evidence = $true
            acceptance_evidence = $false
        }
    }

    $captureSummaryParent = Split-Path -Parent $Path
    if ($captureSummaryParent) {
        New-Item -ItemType Directory -Force -Path $captureSummaryParent | Out-Null
    }
    ($captureSummary | ConvertTo-Json -Depth 8) | Set-Content -LiteralPath $Path -Encoding UTF8
}

function Convert-ToProcessArgumentString {
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

function Invoke-AdbStreamingCopy {
    param(
        [string[]]$Arguments,
        [string]$DestinationPath,
        [int]$TimeoutSeconds = 30
    )

    $destinationParent = Split-Path -Parent $DestinationPath
    if ($destinationParent) {
        New-Item -ItemType Directory -Force -Path $destinationParent | Out-Null
    }

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $adb
    $psi.Arguments = Convert-ToProcessArgumentString -Arguments $Arguments
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi
    $fileStream = $null
    $stderrTask = $null
    $copyTask = $null

    try {
        $fileStream = [System.IO.File]::Open($DestinationPath, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write, [System.IO.FileShare]::Read)
        [void]$process.Start()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $copyTask = $process.StandardOutput.BaseStream.CopyToAsync($fileStream)

        $finished = $process.WaitForExit([Math]::Max(1000, $TimeoutSeconds * 1000))
        if (-not $finished) {
            try {
                $process.Kill()
            } catch {
            }
            throw "Timed out running adb streaming copy"
        }

        if ($copyTask -and -not $copyTask.Wait(5000)) {
            throw "Timed out finalizing adb stdout copy"
        }

        $stderr = if ($stderrTask) { [string]$stderrTask.GetAwaiter().GetResult() } else { "" }
        $stderr = [string]$stderr
        $stderr = $stderr.Trim()
        $exitCode = if ($null -eq $process.ExitCode) { 0 } else { [int]$process.ExitCode }

        return [pscustomobject]@{
            exit_code = $exitCode
            stderr = $stderr
            destination_path = $DestinationPath
        }
    } finally {
        if ($fileStream) {
            try {
                $fileStream.Dispose()
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

function Invoke-AdbCommandCapture {
    param([string[]]$Arguments)

    return Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $Arguments
}

function Capture-LogcatSnapshot {
    param([string]$DestinationPath)

    $filterArgs = @()
    foreach ($spec in ($LogcatSpec -split '\s+')) {
        $trimmed = ([string]$spec).Trim()
        if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
            $filterArgs += $trimmed
        }
    }

    $argumentList = @("-s", $Device, "logcat", "-d", "-v", "time")
    if ($filterArgs.Count -gt 0) {
        $argumentList += $filterArgs
    }

    $result = Invoke-AdbStreamingCopy -Arguments $argumentList -DestinationPath $DestinationPath -TimeoutSeconds 30
    if ($result.exit_code -ne 0) {
        if ([string]::IsNullOrWhiteSpace($result.stderr)) {
            throw "Logcat capture failed"
        }
        throw "Logcat capture failed: $($result.stderr)"
    }
}

function Get-RunAsArtifactFiles {
    $listResult = Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments @("-s", $Device, "shell", "run-as", "com.senku.mobile", "ls", "-1", "files/test-artifacts") -TimeoutMilliseconds 15000
    if ($listResult.timed_out) {
        throw "Artifact listing timed out after 15000 ms"
    }

    $listOutput = if ($null -eq $listResult.output) { "" } else { [string]$listResult.output }
    $files = @()
    foreach ($line in ($listOutput -split "`r?`n")) {
        if ($null -eq $line) {
            continue
        }
        $trimmed = ([string]$line).Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed)) {
            continue
        }
        if ($trimmed -match '\.(png|xml)$') {
            $files += $trimmed
        }
    }
    return $files
}

function Get-ArtifactFileNames {
    param([object[]]$Artifacts)

    $names = New-Object System.Collections.Generic.List[string]
    foreach ($artifact in @($Artifacts)) {
        $name = [string]$artifact
        if ([string]::IsNullOrWhiteSpace($name)) {
            continue
        }
        $names.Add([System.IO.Path]::GetFileName($name))
    }
    return @($names)
}

function Join-FailureReasons {
    param(
        [string]$ExistingMessage,
        [string[]]$AdditionalMessages
    )

    $parts = New-Object System.Collections.Generic.List[string]
    if (-not [string]::IsNullOrWhiteSpace($ExistingMessage)) {
        $parts.Add($ExistingMessage.Trim())
    }
    foreach ($message in @($AdditionalMessages)) {
        $text = [string]$message
        if (-not [string]::IsNullOrWhiteSpace($text)) {
            $parts.Add($text.Trim())
        }
    }
    if ($parts.Count -eq 0) {
        return $null
    }
    return ($parts -join " | ")
}

function Get-ArtifactExpectationFailures {
    param(
        [string[]]$ListedArtifacts,
        [string[]]$LocalScreenshotFiles,
        [string[]]$LocalDumpFiles,
        [string]$ScreenshotDirectory,
        [string]$DumpDirectory
    )

    $failures = New-Object System.Collections.Generic.List[string]
    $expectedScreenshots = Get-ArtifactFileNames -Artifacts @($ListedArtifacts | Where-Object { ([string]$_).ToLowerInvariant().EndsWith(".png") })
    $expectedDumps = Get-ArtifactFileNames -Artifacts @($ListedArtifacts | Where-Object { ([string]$_).ToLowerInvariant().EndsWith(".xml") })

    if ($LocalScreenshotFiles.Count -eq 0) {
        $failures.Add("Expected at least one copied screenshot artifact under '$ScreenshotDirectory' but found none.")
    }
    if ($LocalDumpFiles.Count -eq 0) {
        $failures.Add("Expected at least one copied dump artifact under '$DumpDirectory' but found none.")
    }

    $missingScreenshotCopies = @($expectedScreenshots | Where-Object { $LocalScreenshotFiles -notcontains $_ })
    if ($missingScreenshotCopies.Count -gt 0) {
        $failures.Add(("Listed screenshot artifacts were not copied locally: {0}" -f ($missingScreenshotCopies -join ", ")))
    }
    $missingDumpCopies = @($expectedDumps | Where-Object { $LocalDumpFiles -notcontains $_ })
    if ($missingDumpCopies.Count -gt 0) {
        $failures.Add(("Listed dump artifacts were not copied locally: {0}" -f ($missingDumpCopies -join ", ")))
    }

    return @($failures)
}

function Get-PlatformAnrEvidence {
    param(
        [string]$DumpDirectory
    )

    if ([string]::IsNullOrWhiteSpace($DumpDirectory) -or -not (Test-Path -LiteralPath $DumpDirectory)) {
        return $null
    }

    foreach ($dump in @(Get-ChildItem -LiteralPath $DumpDirectory -Filter "*.xml" -File -ErrorAction SilentlyContinue | Sort-Object Name)) {
        $text = ""
        try {
            $text = Get-Content -LiteralPath $dump.FullName -Raw -ErrorAction Stop
        } catch {
            continue
        }

        $hasAnrTitle = $text.Contains("System UI isn't responding") -or $text.Contains("System UI isn&apos;t responding")
        $hasAnrControls = $text.Contains('resource-id="android:id/aerr_wait"') -or $text.Contains('resource-id="android:id/aerr_close"')
        if ($hasAnrTitle -or $hasAnrControls) {
            return [pscustomobject]@{
                detected = $true
                dump = $dump.Name
                reason = "Android platform ANR dialog blocked run: System UI isn't responding; dump=$($dump.Name)"
                has_system_ui_title = [bool]$hasAnrTitle
                has_anr_controls = [bool]$hasAnrControls
            }
        }
    }

    return $null
}

function Get-InstrumentationTimeoutMs {
    if (Use-ScriptedPromptRun) {
        if ($ScriptedTimeoutMs -gt 0) {
            return ($ScriptedTimeoutMs + 120000)
        }
        return 300000
    }

    switch ($SmokeProfile) {
        "basic" { return 150000 }
        "host" { return 300000 }
        "functional" { return 300000 }
        "functional-follow-up" { return 300000 }
        "functional-saved" { return 300000 }
        "functional-back-provenance" { return 300000 }
        "tablet-functional-rail" { return 300000 }
        "tablet-functional-header" { return 300000 }
        "full" { return 420000 }
        default { return 300000 }
    }
}

function Stop-InstrumentationPackages {
    & $adb -s $Device shell am force-stop com.senku.mobile.test | Out-Null
    & $adb -s $Device shell am force-stop com.senku.mobile | Out-Null
    $psOutput = (& $adb -s $Device shell ps -A -o PID,NAME,ARGS 2>$null | Out-String)
    foreach ($line in ($psOutput -split "`r?`n")) {
        if ($line -notmatch "com\\.senku\\.mobile(?:\\.test)?") {
            continue
        }
        $columns = ($line.Trim() -split "\s+")
        if ($columns.Count -lt 1) {
            continue
        }
        $targetPid = $columns[0]
        if ($targetPid -match '^\d+$') {
            & $adb -s $Device shell kill -9 $targetPid 2>$null | Out-Null
        }
    }
}

function Copy-RunAsFile {
    param(
        [string]$RemoteRelativePath,
        [string]$LocalPath,
        [int]$TimeoutSeconds = 30
    )

    $result = Invoke-AdbStreamingCopy -Arguments @("-s", $Device, "exec-out", "run-as", "com.senku.mobile", "cat", $RemoteRelativePath) -DestinationPath $LocalPath -TimeoutSeconds $TimeoutSeconds
    if ($result.exit_code -ne 0) {
        if ([string]::IsNullOrWhiteSpace($result.stderr)) {
            throw "Failed to copy artifact $RemoteRelativePath"
        }
        throw "Failed to copy artifact ${RemoteRelativePath}: $($result.stderr)"
    }
}

function Write-ZipBundle {
    param(
        [string]$SourceDirectory,
        [string]$DestinationZip
    )

    return Write-AndroidHarnessZipBundle -SourceDirectory $SourceDirectory -DestinationZip $DestinationZip
}

function Get-ResolvedDeviceFacts {
    param([string]$RequestedOrientation = "")

    return Resolve-AndroidDeviceFacts -AdbPath $adb -DeviceName $Device -RequestedOrientation $RequestedOrientation
}

function Resolve-ExpectedDeviceRoleForSmokePreset {
    param([string]$Preset)

    if ([string]::IsNullOrWhiteSpace($Preset)) {
        return ""
    }
    if ($Preset.StartsWith("phone-", [System.StringComparison]::OrdinalIgnoreCase)) {
        return "phone"
    }
    if ($Preset.StartsWith("tablet-", [System.StringComparison]::OrdinalIgnoreCase)) {
        return "tablet"
    }
    return ""
}

function Assert-SmokePresetDeviceRole {
    param(
        [object]$DeviceFacts,
        [string]$Preset
    )

    $expectedRole = Resolve-ExpectedDeviceRoleForSmokePreset -Preset $Preset
    if ([string]::IsNullOrWhiteSpace($expectedRole)) {
        return
    }
    $actualRole = if ($null -ne $DeviceFacts -and -not [string]::IsNullOrWhiteSpace([string]$DeviceFacts.resolved_role)) {
        [string]$DeviceFacts.resolved_role
    } else {
        "unknown"
    }
    if ($actualRole -eq $expectedRole) {
        return
    }
    $smallestWidth = if ($null -ne $DeviceFacts -and $null -ne $DeviceFacts.smallest_width_dp) {
        [string]$DeviceFacts.smallest_width_dp
    } else {
        "unknown"
    }
    throw ("SmokePreset '{0}' expects a {1} device, but {2} resolved as {3} (smallest_width_dp={4}). Pick a matching emulator/device before running the preset." -f $Preset, $expectedRole, $Device, $actualRole, $smallestWidth)
}

function Resolve-InstrumentationMethodNames {
    param(
        [string]$Profile
    )

    switch ($Profile) {
        "basic" {
            return @(
                "searchQueryShowsResultsWithoutShellPolling",
                "deterministicAskNavigatesToDetailScreen"
            )
        }
        "host" {
            return @(
                "searchQueryShowsResultsWithoutShellPolling",
                "deterministicAskNavigatesToDetailScreen",
                "generativeAskWithHostInferenceNavigatesToDetailScreen"
            )
        }
        "full" {
            return @(
                "searchQueryShowsResultsWithoutShellPolling",
                "deterministicAskNavigatesToDetailScreen",
                "generativeAskWithHostInferenceNavigatesToDetailScreen",
                "autoFollowUpWithHostInferenceBuildsInlineThreadHistory"
            )
        }
        "functional" {
            return @(
                "homeAndAskImeSubmitRouteToSearchResultsAndAnswerDetail",
                "searchButtonFromAskLaneUsesAskSubmitOwnership",
                "savedNavigationBackReturnsManualHomeDestination",
                "answerModeProvenanceOpenBackReturnsAnswerContext"
            )
        }
        "functional-follow-up" {
            return @(
                "homeAndAskImeSubmitRouteToSearchResultsAndAnswerDetail",
                "detailFollowUpImeSendDispatchesLikeSendClick",
                "detailFollowUpEmptySubmitDoesNotDispatchDraft",
                "phoneAnswerDetailFollowUpSubmitReturnsThreadDetailWithInlineHistory"
            )
        }
        "functional-saved" {
            return @(
                "savedNavigationBackReturnsManualHomeDestination",
                "guideDetailSaveButtonSurfacesGuideInSavedDestinationAndUnsaveRemovesIt",
                "savedTabImeSubmitRoutesToSearchResultsNotAnswerDetail",
                "savedTabPinnedGuideButtonOpensGuideDetailAndBackReturnsSaved"
            )
        }
        "functional-back-provenance" {
            return @(
                "answerModeProvenanceOpenBackReturnsAnswerContext",
                "emergencyAnswerVisibleBackButtonReturnsManualHomeDestination",
                "answerSourceChipTapFollowsAdvertisedActionForSeededGuideSources"
            )
        }
        "tablet-functional-rail" {
            return @(
                "tabletDetailRailLibraryTapReturnsManualHome",
                "tabletDetailRailSavedTapOpensSavedDestination",
                "tabletDetailRailAskTapOpensEmptyAskLaneAndSubmitRoutesToAnswerDetail"
            )
        }
        "tablet-functional-header" {
            return @(
                "guideDetailTabletPortraitSuppressesRedundantStateChips"
            )
        }
        default {
            return @()
        }
    }
}

function Resolve-InstrumentationTargetList {
    param(
        [string]$BaseClass,
        [string[]]$MethodNames
    )

    if ($null -eq $MethodNames -or $MethodNames.Count -eq 0) {
        return @($BaseClass)
    }

    $targets = @()
    foreach ($methodName in $MethodNames) {
        if (-not [string]::IsNullOrWhiteSpace($methodName)) {
            $targets += "${BaseClass}#$methodName"
        }
    }
    return $targets
}

function Resolve-InstrumentationClassArgument {
    param(
        [string]$BaseClass,
        [string]$Profile
    )

    $methodNames = @(Resolve-InstrumentationMethodNames -Profile $Profile)
    return (@(Resolve-InstrumentationTargetList -BaseClass $BaseClass -MethodNames $methodNames) -join ",")
}

function Use-ScriptedPromptRun {
    return -not [string]::IsNullOrWhiteSpace($ScriptedQuery)
}

switch ($SmokePreset) {
    "phone-basic" {
        $SmokeProfile = "basic"
        $Orientation = "portrait"
        $FontScale = 1.0
    }
    "phone-host" {
        $SmokeProfile = "host"
        $Orientation = "portrait"
        $FontScale = 1.0
    }
    "phone-full" {
        $SmokeProfile = "full"
        $Orientation = "portrait"
        $FontScale = 1.0
    }
    "phone-functional" {
        $SmokeProfile = "functional"
        $Orientation = "portrait"
        $FontScale = 1.0
    }
    "phone-functional-follow-up" {
        $SmokeProfile = "functional-follow-up"
        $Orientation = "portrait"
        $FontScale = 1.0
    }
    "phone-functional-saved" {
        $SmokeProfile = "functional-saved"
        $Orientation = "portrait"
        $FontScale = 1.0
    }
    "phone-functional-back-provenance" {
        $SmokeProfile = "functional-back-provenance"
        $Orientation = "portrait"
        $FontScale = 1.0
    }
    "tablet-functional-rail" {
        $SmokeProfile = "tablet-functional-rail"
        $Orientation = "landscape"
        $FontScale = 1.0
    }
    "tablet-functional-header" {
        $SmokeProfile = "tablet-functional-header"
        $Orientation = "portrait"
        $FontScale = 1.0
    }
    "tablet-landscape" {
        $SmokeProfile = "host"
        $Orientation = "landscape"
        $FontScale = 1.0
    }
    "large-font" {
        $SmokeProfile = "basic"
        $Orientation = "portrait"
        $FontScale = 1.3
    }
    "tablet-large-font" {
        $SmokeProfile = "host"
        $Orientation = "landscape"
        $FontScale = 1.3
    }
}

switch ($SmokeProfile) {
    "host" {
        $EnableHostInferenceSmoke = $true
    }
    "full" {
        $EnableHostInferenceSmoke = $true
        $EnableFollowUpSmoke = $true
    }
}

if ($EnableFollowUpSmoke -and -not $EnableHostInferenceSmoke) {
    throw "Follow-up smoke requires host inference smoke to be enabled."
}

if (Use-ScriptedPromptRun) {
    $SmokeProfile = "custom"
}

$script:OriginalFontScale = $null
$script:OriginalAccelerometerRotation = $null
$script:OriginalUserRotation = $null

function Get-DeviceFontScale {
    $raw = (& $adb -s $Device shell settings get system font_scale 2>$null | Out-String).Trim()
    $parsed = 1.0
    if ([double]::TryParse($raw, [System.Globalization.NumberStyles]::Float, [System.Globalization.CultureInfo]::InvariantCulture, [ref]$parsed)) {
        return [Math]::Max(0.5, $parsed)
    }
    return 1.0
}

function Invoke-AdbBestEffort {
    param([string[]]$Arguments)

    $result = Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $Arguments -TimeoutMilliseconds 10000
    return $result
}

function Get-AdbShellValue {
    param([string[]]$ShellArguments)

    $result = Invoke-AdbBestEffort -Arguments (@("-s", $Device, "shell") + $ShellArguments)
    if ($result.exit_code -ne 0 -or $result.timed_out) {
        return ""
    }
    return ([string]$result.output).Trim()
}

function Wait-ForDeviceReadiness {
    param([int]$TimeoutSeconds = 90)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastBootCompleted = ""
    $lastBootAnim = ""
    $lastWindowSize = ""

    do {
        $lastBootCompleted = Get-AdbShellValue -ShellArguments @("getprop", "sys.boot_completed")
        $lastBootAnim = Get-AdbShellValue -ShellArguments @("getprop", "init.svc.bootanim")
        $lastWindowSize = Get-AdbShellValue -ShellArguments @("wm", "size")

        $bootReady = ($lastBootCompleted -eq "1")
        $bootAnimReady = ([string]::IsNullOrWhiteSpace($lastBootAnim) -or $lastBootAnim -eq "stopped")
        $windowReady = ($lastWindowSize -match '(Physical|Override) size:\s*\d+x\d+')
        if ($bootReady -and $bootAnimReady -and $windowReady) {
            return
        }

        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    throw ("Device {0} did not become ready after adb wait-for-device within {1}s: sys.boot_completed='{2}', init.svc.bootanim='{3}', wm size='{4}'" -f $Device, $TimeoutSeconds, $lastBootCompleted, $lastBootAnim, $lastWindowSize)
}

function Set-DeviceFontScale([double]$Scale) {
    $safeScale = [Math]::Max(0.5, [Math]::Min(2.0, $Scale))
    $serialized = $safeScale.ToString("0.##", [System.Globalization.CultureInfo]::InvariantCulture)
    [void](Invoke-AdbBestEffort -Arguments @("-s", $Device, "shell", "settings", "put", "system", "font_scale", $serialized))
    Start-Sleep -Milliseconds 700
}

function Set-DeviceSizeOverride([int]$Width, [int]$Height) {
    [void](Invoke-AdbBestEffort -Arguments @("-s", $Device, "shell", "wm", "size", "$Width`x$Height"))
    Start-Sleep -Milliseconds 700
}

function Reset-DeviceSizeOverride() {
    [void](Invoke-AdbBestEffort -Arguments @("-s", $Device, "shell", "wm", "size", "reset"))
    Start-Sleep -Milliseconds 700
}

function Wait-ForDeviceOrientation([string]$TargetOrientation) {
    for ($attempt = 0; $attempt -lt 8; $attempt++) {
        $deviceFacts = Get-ResolvedDeviceFacts -RequestedOrientation $TargetOrientation
        if ($deviceFacts.rotation_matches_requested -eq $true) {
            return $true
        }
        Start-Sleep -Milliseconds 400
    }

    return $false
}

function Set-DeviceOrientation([string]$TargetOrientation) {
    $deviceFacts = Get-ResolvedDeviceFacts -RequestedOrientation $TargetOrientation
    $rotation = if ($null -ne $deviceFacts.expected_rotation) {
        [int]$deviceFacts.expected_rotation
    } else {
        if ($TargetOrientation -eq "landscape") { 1 } else { 0 }
    }
    [void](Invoke-AdbBestEffort -Arguments @("-s", $Device, "shell", "settings", "put", "system", "accelerometer_rotation", "0"))
    [void](Invoke-AdbBestEffort -Arguments @("-s", $Device, "shell", "settings", "put", "system", "user_rotation", "$rotation"))
    Start-Sleep -Milliseconds 900
}

function Get-DeviceSettingValue([string]$Namespace, [string]$Key) {
    return (& $adb -s $Device shell settings get $Namespace $Key 2>$null | Out-String).Trim()
}

function Restore-DeviceSettings {
    if ($null -ne $script:OriginalFontScale) {
        Set-DeviceFontScale -Scale $script:OriginalFontScale
    }
    if ($null -ne $script:OriginalAccelerometerRotation -and -not [string]::IsNullOrWhiteSpace($script:OriginalAccelerometerRotation)) {
        [void](Invoke-AdbBestEffort -Arguments @("-s", $Device, "shell", "settings", "put", "system", "accelerometer_rotation", "$script:OriginalAccelerometerRotation"))
    }
    if ($null -ne $script:OriginalUserRotation -and -not [string]::IsNullOrWhiteSpace($script:OriginalUserRotation)) {
        [void](Invoke-AdbBestEffort -Arguments @("-s", $Device, "shell", "settings", "put", "system", "user_rotation", "$script:OriginalUserRotation"))
    }
}

$EffectiveHostInferenceUrl = Resolve-HostInferenceUrlForDevice -Url $HostInferenceUrl
$appFingerprint = Get-ApkFingerprint -Path $appApk
$testFingerprint = Get-ApkFingerprint -Path $testApk
$InstallCacheMatches = Test-InstallCacheMatch -StatePath $installStatePath -AppFingerprint $appFingerprint -TestFingerprint $testFingerprint
$EffectiveSkipInstall = [bool]$SkipInstall
$script:InstallNoStreamingFallbackAttempted = $false
$EffectiveTestMethods = if (Use-ScriptedPromptRun) {
    @("scriptedPromptFlowCompletes")
} else {
    @(Resolve-InstrumentationMethodNames -Profile $SmokeProfile)
}
$EffectiveTestTargets = @(Resolve-InstrumentationTargetList -BaseClass $TestClass -MethodNames $EffectiveTestMethods)
$EffectiveTestClass = $EffectiveTestTargets -join ","

if ($EnableHostInferenceSmoke -and [string]::IsNullOrWhiteSpace($EffectiveHostInferenceUrl)) {
    throw "Host inference smoke requested but no host inference URL was provided."
}

Write-RunPhase -Phase "waiting for device"
& $adb -s $Device wait-for-device
if ($LASTEXITCODE -ne 0) {
    throw "adb wait-for-device failed for $Device"
}
Write-RunPhase -Phase "device connected; waiting for readiness"
Wait-ForDeviceReadiness
Write-RunPhase -Phase "device ready"
$preflightDeviceFacts = Get-ResolvedDeviceFacts -RequestedOrientation $Orientation
Assert-SmokePresetDeviceRole -DeviceFacts $preflightDeviceFacts -Preset $SmokePreset
if (-not $EffectiveSkipInstall) {
    Write-RunPhase -Phase "installing app APK"
    Invoke-ApkInstallWithPhysicalNoStreamingFallback -ApkPath $appApk -FailureMessage "App APK install failed" -ApkLabel "app APK" -TimeoutMilliseconds $apkInstallTimeoutMilliseconds
    Write-RunPhase -Phase "installing test APK"
    Invoke-ApkInstallWithPhysicalNoStreamingFallback -ApkPath $testApk -FailureMessage "Test APK install failed" -ApkLabel "test APK" -TimeoutMilliseconds $apkInstallTimeoutMilliseconds
    Write-RunPhase -Phase "APK install complete"
} else {
    Write-RunPhase -Phase "install skipped; verifying packages"
    if (-not (Test-PackageInstalled -PackageName "com.senku.mobile")) {
        throw "SkipInstall requested but com.senku.mobile is not installed on $Device."
    }
    if (-not (Test-PackageInstalled -PackageName "com.senku.mobile.test")) {
        throw "SkipInstall requested but com.senku.mobile.test is not installed on $Device."
    }
    Write-RunPhase -Phase "installed packages verified"
}
Write-RunPhase -Phase "clearing app test artifacts"
& $adb -s $Device shell run-as com.senku.mobile rm -rf files/test-artifacts | Out-Null
& $adb -s $Device shell run-as com.senku.mobile mkdir -p files/test-artifacts | Out-Null
Write-RunPhase -Phase "app test artifacts cleared"

$script:OriginalFontScale = Get-DeviceFontScale
$script:OriginalAccelerometerRotation = Get-DeviceSettingValue -Namespace system -Key accelerometer_rotation
$script:OriginalUserRotation = Get-DeviceSettingValue -Namespace system -Key user_rotation
$script:AppliedSizeOverride = $false

$summaryStatus = "unknown"
$failureReason = $null
$artifactFiles = @()
$pendingException = $null
$logcatPath = $null
$instrumentationLog = Join-Path $artifactDir "instrumentation.txt"
$artifactManifestPath = Join-Path $artifactDir "artifact_manifest.json"
$artifactBundleZip = $null
$logcatCaptured = $false
$deviceFacts = $null
$artifactFacts = $null
$orientationSettled = $false
$artifactExpectationFailures = @()
$artifactExpectationsMet = $false
$installedIdentity = $null
$identityProbeError = $null
$platformAnrEvidence = $null

try {
    try {
        Write-RunPhase -Phase "resolving installed binary identity"
        $installedIdentity = Resolve-InstalledBinaryIdentity -CachePath $identityStatePath
        Write-RunPhase -Phase "installed binary identity resolved"
    } catch {
        $identityProbeError = $_.Exception.Message
        Write-Warning ("Could not resolve installed APK/model identity on {0}: {1}" -f $Device, $identityProbeError)
    }

    Write-RunPhase -Phase ("setting font scale {0}" -f $FontScale)
    Set-DeviceFontScale -Scale $FontScale
    Write-RunPhase -Phase ("setting orientation {0}" -f $Orientation)
    Set-DeviceOrientation -TargetOrientation $Orientation
    $deviceFacts = Get-ResolvedDeviceFacts -RequestedOrientation $Orientation
    Assert-SmokePresetDeviceRole -DeviceFacts $deviceFacts -Preset $SmokePreset
    $orientationSettled = ($deviceFacts.rotation_matches_requested -eq $true)
    if (-not $orientationSettled) {
        $orientationSettled = Wait-ForDeviceOrientation -TargetOrientation $Orientation
        if ($orientationSettled) {
            $deviceFacts = Get-ResolvedDeviceFacts -RequestedOrientation $Orientation
        }
    }
    if (-not $orientationSettled) {
        $deviceFacts = Get-ResolvedDeviceFacts -RequestedOrientation $Orientation
        $resolvedOrientation = if ($null -ne $deviceFacts -and -not [string]::IsNullOrWhiteSpace($deviceFacts.current_orientation)) {
            $deviceFacts.current_orientation
        } else {
            "unknown"
        }
        throw "Requested orientation '$Orientation' did not settle on $Device (resolved: $resolvedOrientation). Refusing wm-size fallback because it can fake posture instead of rotating the device."
    }
    try {
        if ($ClearLogcatBeforeRun) {
            Write-RunPhase -Phase "clearing logcat"
            & $adb -s $Device logcat -c | Out-Null
            Write-RunPhase -Phase "logcat cleared"
        }

        $instrumentationTarget = "com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner"
        $args = @(
            "-s", $Device,
            "shell", "am", "instrument", "-w",
            "-e", "class", $EffectiveTestClass
        )
        if ($EnableHostInferenceSmoke -and -not [string]::IsNullOrWhiteSpace($HostInferenceUrl)) {
            $args += @(
                "-e", "hostInferenceEnabled", "true",
                "-e", "hostInferenceUrl", $EffectiveHostInferenceUrl,
                "-e", "hostInferenceModel", $HostInferenceModel
            )
        }
        if ($EnableFollowUpSmoke) {
            $args += @("-e", "followUpSmokeEnabled", "true")
        }
        if (Use-ScriptedPromptRun) {
            $encodedScriptedQuery = [System.Uri]::EscapeDataString($ScriptedQuery)
            $encodedScriptedFollowUpQuery = if (-not [string]::IsNullOrWhiteSpace($ScriptedFollowUpQuery)) {
                [System.Uri]::EscapeDataString($ScriptedFollowUpQuery)
            } else {
                ""
            }
            $encodedScriptedExpectedTitle = if (-not [string]::IsNullOrWhiteSpace($ScriptedExpectedTitle)) {
                [System.Uri]::EscapeDataString($ScriptedExpectedTitle)
            } else {
                ""
            }
            $encodedScriptedCaptureLabel = if (-not [string]::IsNullOrWhiteSpace($ScriptedCaptureLabel)) {
                [System.Uri]::EscapeDataString($ScriptedCaptureLabel)
            } else {
                ""
            }
            $encodedScriptedRequiredResId = if (-not [string]::IsNullOrWhiteSpace($ScriptedRequiredResId)) {
                [System.Uri]::EscapeDataString($ScriptedRequiredResId)
            } else {
                ""
            }
            $encodedScriptedExpectedAnswerSurfaceLabel = if (-not [string]::IsNullOrWhiteSpace($ScriptedExpectedAnswerSurfaceLabel)) {
                [System.Uri]::EscapeDataString($ScriptedExpectedAnswerSurfaceLabel)
            } else {
                ""
            }
            $encodedScriptedForbiddenAnswerSurfaceLabels = if (-not [string]::IsNullOrWhiteSpace($ScriptedForbiddenAnswerSurfaceLabels)) {
                [System.Uri]::EscapeDataString($ScriptedForbiddenAnswerSurfaceLabels)
            } else {
                ""
            }
            $encodedScriptedExpectedRuleId = if (-not [string]::IsNullOrWhiteSpace($ScriptedExpectedRuleId)) {
                [System.Uri]::EscapeDataString($ScriptedExpectedRuleId)
            } else {
                ""
            }
            $encodedScriptedExpectedSourceGuideId = if (-not [string]::IsNullOrWhiteSpace($ScriptedExpectedSourceGuideId)) {
                [System.Uri]::EscapeDataString($ScriptedExpectedSourceGuideId)
            } else {
                ""
            }
            $encodedScriptedExpectedReviewedCardId = if (-not [string]::IsNullOrWhiteSpace($ScriptedExpectedReviewedCardId)) {
                [System.Uri]::EscapeDataString($ScriptedExpectedReviewedCardId)
            } else {
                ""
            }
            $encodedScriptedExpectedReviewedCardGuideId = if (-not [string]::IsNullOrWhiteSpace($ScriptedExpectedReviewedCardGuideId)) {
                [System.Uri]::EscapeDataString($ScriptedExpectedReviewedCardGuideId)
            } else {
                ""
            }
            $encodedScriptedExpectedReviewedCardReviewStatus = if (-not [string]::IsNullOrWhiteSpace($ScriptedExpectedReviewedCardReviewStatus)) {
                [System.Uri]::EscapeDataString($ScriptedExpectedReviewedCardReviewStatus)
            } else {
                ""
            }
            $encodedScriptedExpectedReviewedCardSourceGuideIds = if (-not [string]::IsNullOrWhiteSpace($ScriptedExpectedReviewedCardSourceGuideIds)) {
                [System.Uri]::EscapeDataString($ScriptedExpectedReviewedCardSourceGuideIds)
            } else {
                ""
            }
            $encodedScriptedExpectedBodyContains = if (-not [string]::IsNullOrWhiteSpace($ScriptedExpectedBodyContains)) {
                [System.Uri]::EscapeDataString($ScriptedExpectedBodyContains)
            } else {
                ""
            }
            $args += @(
                "-e", "scriptedQuery", $encodedScriptedQuery,
                "-e", "scriptedAsk", $(if ($ScriptedAsk) { "true" } else { "false" }),
                "-e", "scriptedExpectedSurface", $ScriptedExpectedSurface
            )
            if ($ScriptedEnableReviewedCardRuntime) {
                $args += @("-e", "scriptedEnableReviewedCardRuntime", "true")
            }
            if ($AllowHostFallback) {
                $args += @("-e", "scriptedAllowHostFallback", "true")
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedFollowUpQuery)) {
                $args += @("-e", "scriptedFollowUpQuery", $encodedScriptedFollowUpQuery)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedExpectedTitle)) {
                $args += @("-e", "scriptedExpectedTitle", $encodedScriptedExpectedTitle)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedCaptureLabel)) {
                $args += @("-e", "scriptedCaptureLabel", $encodedScriptedCaptureLabel)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedRequiredResId)) {
                $args += @("-e", "scriptedRequiredResId", $encodedScriptedRequiredResId)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedExpectedAnswerSurfaceLabel)) {
                $args += @("-e", "scriptedExpectedAnswerSurfaceLabel", $encodedScriptedExpectedAnswerSurfaceLabel)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedForbiddenAnswerSurfaceLabels)) {
                $args += @("-e", "scriptedForbiddenAnswerSurfaceLabels", $encodedScriptedForbiddenAnswerSurfaceLabels)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedExpectedRuleId)) {
                $args += @("-e", "scriptedExpectedRuleId", $encodedScriptedExpectedRuleId)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedExpectedSourceGuideId)) {
                $args += @("-e", "scriptedExpectedSourceGuideId", $encodedScriptedExpectedSourceGuideId)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedExpectedReviewedCardId)) {
                $args += @("-e", "scriptedExpectedReviewedCardId", $encodedScriptedExpectedReviewedCardId)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedExpectedReviewedCardGuideId)) {
                $args += @("-e", "scriptedExpectedReviewedCardGuideId", $encodedScriptedExpectedReviewedCardGuideId)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedExpectedReviewedCardReviewStatus)) {
                $args += @("-e", "scriptedExpectedReviewedCardReviewStatus", $encodedScriptedExpectedReviewedCardReviewStatus)
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedExpectedReviewedCardSourceGuideIds)) {
                $args += @("-e", "scriptedExpectedReviewedCardSourceGuideIds", $encodedScriptedExpectedReviewedCardSourceGuideIds)
            }
            if ($ScriptedAssertRecentThreadReviewedCardMetadata) {
                $args += @("-e", "scriptedAssertRecentThreadReviewedCardMetadata", "true")
            }
            if (-not [string]::IsNullOrWhiteSpace($encodedScriptedExpectedBodyContains)) {
                $args += @("-e", "scriptedExpectedBodyContains", $encodedScriptedExpectedBodyContains)
            }
            if ($ScriptedTimeoutMs -gt 0) {
                $args += @("-e", "scriptedTimeoutMs", "$ScriptedTimeoutMs")
            }
            if ($ScriptedExtraSettleMs -gt 0) {
                $args += @("-e", "scriptedExtraSettleMs", "$ScriptedExtraSettleMs")
            }
        }
        $args += @(
            $instrumentationTarget
        )

        Stop-InstrumentationPackages
        $instrumentationTimeoutMs = Get-InstrumentationTimeoutMs
        Write-RunPhase -Phase ("starting instrumentation {0} with timeout {1} ms" -f $EffectiveTestClass, $instrumentationTimeoutMs)
        $instrumentationResult = Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $args -TimeoutMilliseconds $instrumentationTimeoutMs
        if ($instrumentationResult.timed_out) {
            Stop-InstrumentationPackages
            throw "Instrumentation run timed out after $instrumentationTimeoutMs ms"
        }
        Write-RunPhase -Phase "instrumentation completed"
        $instrumentationOutput = if ($null -eq $instrumentationResult.output) { "" } else { [string]$instrumentationResult.output }
        $instrumentationOutput | Set-Content -LiteralPath $instrumentationLog -Encoding UTF8
        if (-not [string]::IsNullOrWhiteSpace($instrumentationOutput)) {
            Write-Output $instrumentationOutput.TrimEnd()
        }
        $instrumentationLower = $instrumentationOutput.ToLowerInvariant()
        $instrumentationLooksPassed = $instrumentationLower.Contains("ok (") -and -not $instrumentationLower.Contains("failures!!!")
        $instrumentationLooksFailed = (($instrumentationResult.exit_code -ne 0) -and -not $instrumentationLooksPassed) -or
            $instrumentationLower.Contains("failures!!!") -or
            $instrumentationLower.Contains("process crashed") -or
            $instrumentationLower.Contains("shortmsg=") -or
            $instrumentationLower.Contains("ok (0 tests)") -or
            [regex]::IsMatch($instrumentationOutput, 'Tests run:\s*\d+,\s*Failures:\s*[1-9]\d*')

        if ($CaptureLogcat) {
            Write-RunPhase -Phase "capturing logcat"
            $logcatPath = Join-Path $artifactDir "logcat.txt"
            Capture-LogcatSnapshot -DestinationPath $logcatPath
            $logcatCaptured = $true
            Write-RunPhase -Phase "logcat captured"
        }

        Stop-InstrumentationPackages
        Write-RunPhase -Phase "listing artifacts"
        $artifactFiles = @(Get-RunAsArtifactFiles)
        Write-RunPhase -Phase ("artifacts listed: {0}" -f $artifactFiles.Count)

        foreach ($artifactFile in $artifactFiles) {
            $extension = ([string][System.IO.Path]::GetExtension($artifactFile)).ToLowerInvariant()
            $targetDir = if ($extension -eq ".xml") { $dumpDir } else { $screenshotDir }
            Write-RunPhase -Phase ("copying artifact {0}" -f $artifactFile)
            Copy-RunAsFile -RemoteRelativePath ("files/test-artifacts/" + $artifactFile) -LocalPath (Join-Path $targetDir $artifactFile)
        }
        Write-RunPhase -Phase "artifact copy complete"
        $platformAnrEvidence = Get-PlatformAnrEvidence -DumpDirectory $dumpDir

        if ($null -ne $platformAnrEvidence -and $platformAnrEvidence.detected -eq $true) {
            throw $platformAnrEvidence.reason
        }

        if ($instrumentationLooksFailed) {
            throw "Instrumentation run failed"
        }

        if ((Use-ScriptedPromptRun) -and $artifactFiles.Count -eq 0) {
            throw "Scripted instrumentation run completed without any captured artifacts"
        }
        $summaryStatus = "pass"
    } catch {
        $pendingException = $_
        $failureReason = $_.Exception.Message
        if ($failureReason -match "without any captured artifacts") {
            $summaryStatus = "no_artifacts"
        } elseif ($failureReason -match "crash" -or $failureReason -match "shortmsg=") {
            $summaryStatus = "crash"
        } else {
            $summaryStatus = "fail"
        }
    } finally {
        if ($summaryStatus -ne "pass" -and @($artifactFiles).Count -eq 0) {
            try {
                Write-RunPhase -Phase "attempting best-effort artifact sync after failure"
                $failureArtifactFiles = @(Get-RunAsArtifactFiles)
                foreach ($artifactFile in $failureArtifactFiles) {
                    $extension = ([string][System.IO.Path]::GetExtension($artifactFile)).ToLowerInvariant()
                    $targetDir = if ($extension -eq ".xml") { $dumpDir } else { $screenshotDir }
                    $targetPath = Join-Path $targetDir $artifactFile
                    if (-not (Test-Path -LiteralPath $targetPath)) {
                        Copy-RunAsFile -RemoteRelativePath ("files/test-artifacts/" + $artifactFile) -LocalPath $targetPath
                    }
                }
                $artifactFiles = $failureArtifactFiles
                Write-RunPhase -Phase ("best-effort artifact sync complete: {0}" -f $artifactFiles.Count)
            } catch {
                $failureReason = Join-FailureReasons `
                    -ExistingMessage $failureReason `
                    -AdditionalMessages @("Best-effort artifact sync failed: $($_.Exception.Message)")
                Write-Warning ("Best-effort artifact sync failed on {0}: {1}" -f $Device, $_.Exception.Message)
            }
        }

        if ($CaptureLogcat -and -not $logcatCaptured) {
            try {
                Write-RunPhase -Phase "capturing logcat after failure"
                $logcatPath = Join-Path $artifactDir "logcat.txt"
                Capture-LogcatSnapshot -DestinationPath $logcatPath
            } catch {
                if ([string]::IsNullOrWhiteSpace($failureReason)) {
                    $failureReason = "Failed to capture logcat after instrumentation failure: $($_.Exception.Message)"
                    if ($summaryStatus -eq "pass") {
                        $summaryStatus = "fail"
                    }
                }
            }
        }

        $localScreenshotFiles = @()
        $localDumpFiles = @()
        if (Test-Path -LiteralPath $screenshotDir) {
            $localScreenshotFiles = @(Get-ChildItem -LiteralPath $screenshotDir -File -ErrorAction SilentlyContinue | Sort-Object Name | ForEach-Object { $_.Name })
        }
        if (Test-Path -LiteralPath $dumpDir) {
            $localDumpFiles = @(Get-ChildItem -LiteralPath $dumpDir -File -ErrorAction SilentlyContinue | Sort-Object Name | ForEach-Object { $_.Name })
        }
        $installedPackMetadata = Get-InstalledPackMetadata
        $deviceFacts = Get-ResolvedDeviceFacts -RequestedOrientation $Orientation
        if ($localScreenshotFiles.Count -gt 0) {
            $firstScreenshotPath = Join-Path $screenshotDir $localScreenshotFiles[0]
            $artifactFacts = [pscustomobject]@{
                first_screenshot = Get-AndroidScreenshotFacts -Path $firstScreenshotPath -RequestedOrientation $Orientation
            }
            if ($null -eq $artifactFacts.first_screenshot.dimensions_px) {
                if ([string]::IsNullOrWhiteSpace($failureReason)) {
                    $failureReason = "Could not read screenshot dimensions for $firstScreenshotPath"
                }
                if ($summaryStatus -eq "pass") {
                    $summaryStatus = "fail"
                }
            } elseif ($artifactFacts.first_screenshot.orientation_mismatch -eq $true) {
                $dimensions = $artifactFacts.first_screenshot.dimensions_px
                if ([string]::IsNullOrWhiteSpace($failureReason)) {
                    $failureReason = "Screenshot orientation mismatch: expected $Orientation but captured $($dimensions.width)x$($dimensions.height)"
                }
                if ($summaryStatus -eq "pass") {
                    $summaryStatus = "fail"
                }
            }
        } else {
            $artifactFacts = [pscustomobject]@{
                first_screenshot = $null
            }
        }

        $artifactExpectationFailures = @(
            Get-ArtifactExpectationFailures `
                -ListedArtifacts $artifactFiles `
                -LocalScreenshotFiles $localScreenshotFiles `
                -LocalDumpFiles $localDumpFiles `
                -ScreenshotDirectory $screenshotDir `
                -DumpDirectory $dumpDir
        )
        $artifactExpectationsMet = ($artifactExpectationFailures.Count -eq 0)
        if (-not $artifactExpectationsMet) {
            if ($summaryStatus -eq "pass" -or [string]::IsNullOrWhiteSpace($failureReason)) {
                $failureReason = Join-FailureReasons -ExistingMessage $failureReason -AdditionalMessages $artifactExpectationFailures
            }
            if ($summaryStatus -eq "pass") {
                $summaryStatus = "fail"
            }
        }

        $artifactManifest = [pscustomobject]@{
            screenshots = $localScreenshotFiles
            dumps = $localDumpFiles
            instrumentation_log = $(if (Test-Path -LiteralPath $instrumentationLog) { [System.IO.Path]::GetFileName($instrumentationLog) } else { $null })
            logcat = $(if ($logcatPath -and (Test-Path -LiteralPath $logcatPath)) { [System.IO.Path]::GetFileName($logcatPath) } else { $null })
            generated_at_utc = (Get-Date).ToUniversalTime().ToString("o")
        }
        ($artifactManifest | ConvertTo-Json -Depth 4) | Set-Content -LiteralPath $artifactManifestPath -Encoding UTF8
        $artifactBundleZip = Write-ZipBundle -SourceDirectory $artifactDir -DestinationZip (Join-Path (Split-Path -Parent $artifactDir) ($Device + "_bundle.zip"))

        if ($summaryStatus -eq "pass") {
            $installState = [pscustomobject]@{
                device = $Device
                app = $appFingerprint
                test = $testFingerprint
                recorded_at = (Get-Date).ToUniversalTime().ToString("o")
            }
            ($installState | ConvertTo-Json -Depth 4) | Set-Content -LiteralPath $installStatePath -Encoding UTF8
        }

        $runStopwatch.Stop()
        $runCompletedUtc = (Get-Date).ToUniversalTime()
        $summaryModelIdentity = Resolve-SummaryModelIdentity -InstalledIdentity $installedIdentity
        $summaryObject = [pscustomobject]@{
        run_started_utc = $runStartedUtc.ToString("o")
        run_completed_utc = $runCompletedUtc.ToString("o")
        elapsed_ms = [int]$runStopwatch.ElapsedMilliseconds
        duration_ms = [int]$runStopwatch.ElapsedMilliseconds
        device = $Device
        test_class = $EffectiveTestClass
        requested_test_class = $TestClass
        selected_test_methods = @($EffectiveTestMethods)
        selected_test_targets = @($EffectiveTestTargets)
        smoke_preset = $(if ([string]::IsNullOrWhiteSpace($SmokePreset)) { $null } else { $SmokePreset })
        smoke_profile = $SmokeProfile
        skip_build = [bool]$SkipBuild
        skip_install = [bool]$EffectiveSkipInstall
        install_cache_matches = [bool]$InstallCacheMatches
        install_no_streaming_fallback_attempted = [bool]$script:InstallNoStreamingFallbackAttempted
        install_contract = [pscustomobject]@{
            apk_install_timeout_ms = [int]$apkInstallTimeoutMilliseconds
            primary_install_mode = "adb install -r"
            physical_no_streaming_fallback_enabled = [bool]($Device -notlike "emulator-*")
            fallback_install_mode = "adb install --no-streaming -r"
            fallback_attempted = [bool]$script:InstallNoStreamingFallbackAttempted
            timeout_failure_is_reported = $true
        }
        scripted_query = $ScriptedQuery
        scripted_ask = [bool]$ScriptedAsk
        scripted_followup_query = $ScriptedFollowUpQuery
        scripted_expected_surface = $(if (Use-ScriptedPromptRun) { $ScriptedExpectedSurface } else { $null })
        scripted_forbidden_answer_surface_labels = $(if (Use-ScriptedPromptRun) { $ScriptedForbiddenAnswerSurfaceLabels } else { $null })
        scripted_allow_host_fallback = [bool]$AllowHostFallback
        scripted_capture_label = $(if (Use-ScriptedPromptRun) { $ScriptedCaptureLabel } else { $null })
        orientation = $Orientation
        font_scale = $FontScale
        host_inference_smoke = ($EnableHostInferenceSmoke -and -not [string]::IsNullOrWhiteSpace($EffectiveHostInferenceUrl))
        followup_smoke = [bool]$EnableFollowUpSmoke
        host_inference_url = $EffectiveHostInferenceUrl
        host_inference_model = $(if ([string]::IsNullOrWhiteSpace($HostInferenceModel)) { $null } else { $HostInferenceModel })
        apk_sha = $(if ($null -ne $installedIdentity -and -not [string]::IsNullOrWhiteSpace([string]$installedIdentity.apk_sha)) { [string]$installedIdentity.apk_sha } else { $null })
        model_identity_source = $summaryModelIdentity.source
        model_name = $summaryModelIdentity.name
        model_sha = $summaryModelIdentity.sha
        host_adb_platform_tools_version = $hostAdbPlatformToolsVersion
        identity_cache_hit = [bool]$script:IdentityCacheHit
        identity_probe_error = $(if ([string]::IsNullOrWhiteSpace($identityProbeError)) { $null } else { $identityProbeError })
        installed_pack = $installedPackMetadata
        platform_anr = $platformAnrEvidence
        artifact_dir = $artifactDir
        device_facts = $deviceFacts
        artifact_facts = [pscustomobject]@{
            orientation_settled = [bool]$orientationSettled
            applied_size_override = [bool]$script:AppliedSizeOverride
            first_screenshot = $(if ($null -ne $artifactFacts) { $artifactFacts.first_screenshot } else { $null })
        }
        artifact_expectations_met = [bool]$artifactExpectationsMet
        artifact_expectation_failures = @($artifactExpectationFailures)
        status = $summaryStatus
        failure_reason = $failureReason
        instrumentation_log = $instrumentationLog
        artifact_manifest_path = $artifactManifestPath
        artifact_bundle_zip = $artifactBundleZip
        screenshot_count = $localScreenshotFiles.Count
        dump_count = $localDumpFiles.Count
        logcat_path = $(if ($CaptureLogcat) { Join-Path $artifactDir "logcat.txt" } else { $null })
        screenshots = $localScreenshotFiles
        dumps = $localDumpFiles
        }
        $summaryJson = $summaryObject | ConvertTo-Json -Depth 4
        $defaultSummaryPath = Join-Path $artifactDir "summary.json"
        $summaryJson | Set-Content -LiteralPath $defaultSummaryPath -Encoding UTF8
        if (-not [string]::IsNullOrWhiteSpace($SummaryPath)) {
            $summaryParent = Split-Path -Parent $SummaryPath
            if ($summaryParent) {
                New-Item -ItemType Directory -Force -Path $summaryParent | Out-Null
            }
            $summaryJson | Set-Content -LiteralPath $SummaryPath -Encoding UTF8
        }
        Write-AndroidInstrumentedCaptureSummary `
            -Path $CaptureSummaryPath `
            -ScreenshotFiles $localScreenshotFiles `
            -DumpFiles $localDumpFiles `
            -LogcatFilePath $(if ($logcatPath -and (Test-Path -LiteralPath $logcatPath -PathType Leaf)) { $logcatPath } else { $null }) `
            -InstalledPack $installedPackMetadata `
            -ModelIdentity $summaryModelIdentity `
            -DeviceFacts $deviceFacts `
            -ArtifactFacts $artifactFacts `
            -ResolvedApkSha $(if ($null -ne $installedIdentity -and -not [string]::IsNullOrWhiteSpace([string]$installedIdentity.apk_sha)) { [string]$installedIdentity.apk_sha } else { $null })
        Write-Output $summaryJson
    }
}
finally {
    try {
        if ($script:AppliedSizeOverride) {
            Reset-DeviceSizeOverride
        }
        Restore-DeviceSettings
    } finally {
        if ($deviceLock) {
            $deviceLock.Dispose()
        }
    }
}

if ($pendingException -ne $null) {
    exit 1
}

exit 0
