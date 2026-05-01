param(
    [string]$Device = "emulator-5554",
    [string]$PackDir = "android-app\\app\\src\\main\\assets\\mobile_pack",
    [string]$PackageName = "com.senku.mobile",
    [string]$ActivityName = ".MainActivity",
    [string]$RemoteTempDir = "/data/local/tmp/senku_mobile_pack_push",
    [switch]$RestartApp,
    [switch]$ForceStop,
    [switch]$ShowInstalledManifest,
    [switch]$SkipIfCurrent,
    [switch]$ForcePush,
    [int]$AdbCommandTimeoutMilliseconds = 30000,
    [int]$AdbPushTimeoutMilliseconds = 300000,
    [string]$SummaryPath = ""
)

$ErrorActionPreference = "Stop"
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$androidHarnessCommonPath = Join-Path $PSScriptRoot "android_harness_common.psm1"
Import-Module $androidHarnessCommonPath -Force

if ($AdbCommandTimeoutMilliseconds -le 0) {
    throw "AdbCommandTimeoutMilliseconds must be greater than zero."
}
if ($AdbPushTimeoutMilliseconds -le 0) {
    throw "AdbPushTimeoutMilliseconds must be greater than zero."
}

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function Get-SafeStateName {
    param([string]$Value)

    $safe = ([string]$Value) -replace '[^A-Za-z0-9_.-]+', '_'
    $safe = $safe.Trim("_")
    if ([string]::IsNullOrWhiteSpace($safe)) {
        return "unknown"
    }
    return $safe
}

function Get-PackFileFingerprint {
    param(
        [string]$Path,
        [string]$Name,
        [long]$ExpectedBytes
    )

    $item = Get-Item -LiteralPath $Path
    return [pscustomobject]@{
        name = $Name
        path = $Path
        length = [int64]$item.Length
        expected_bytes = [int64]$ExpectedBytes
        last_write_utc = $item.LastWriteTimeUtc.ToString("o")
        sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
    }
}

function Test-MobilePackPushStateCurrent {
    param(
        [string]$StatePath,
        [object]$ExpectedState
    )

    if (-not (Test-Path -LiteralPath $StatePath)) {
        return $false
    }

    try {
        $cached = Get-Content -LiteralPath $StatePath -Raw | ConvertFrom-Json
    } catch {
        return $false
    }

    if ([string]$cached.device -ne [string]$ExpectedState.device) { return $false }
    if ([string]$cached.package_name -ne [string]$ExpectedState.package_name) { return $false }
    if ([string]$cached.app_pack_dir -ne [string]$ExpectedState.app_pack_dir) { return $false }
    if ([string]$cached.pack_fingerprint -ne [string]$ExpectedState.pack_fingerprint) { return $false }
    if ($null -eq $cached.files -or $cached.files.Count -ne $ExpectedState.files.Count) { return $false }

    for ($i = 0; $i -lt $ExpectedState.files.Count; $i++) {
        $expected = $ExpectedState.files[$i]
        $actual = $cached.files[$i]
        if ([string]$actual.name -ne [string]$expected.name) { return $false }
        if ([int64]$actual.length -ne [int64]$expected.length) { return $false }
        if ([int64]$actual.expected_bytes -ne [int64]$expected.expected_bytes) { return $false }
        if ([string]$actual.sha256 -ne [string]$expected.sha256) { return $false }
    }

    return $true
}

function Write-MobilePackPushSummary {
    param(
        [object]$Summary,
        [string]$Path
    )

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return
    }

    $summaryParent = Split-Path -Parent $Path
    if ($summaryParent) {
        New-Item -ItemType Directory -Force -Path $summaryParent | Out-Null
    }
    ($Summary | ConvertTo-Json -Depth 6) | Set-Content -LiteralPath $Path -Encoding UTF8
}

function Test-InstalledPackFilesCurrent {
    param(
        [object[]]$PackFiles,
        [string]$ManifestName
    )

    $verifyOutput = Invoke-AdbChecked -Arguments @(
        "-s", $Device, "shell", "run-as", $PackageName, "wc", "-c",
        "files/mobile_pack/$($manifestSummary.sqliteName)",
        "files/mobile_pack/$($manifestSummary.vectorName)",
        "files/mobile_pack/$ManifestName"
    ) -AllowFailure
    if ($script:LastAdbExitCode -ne 0) {
        return $false
    }

    $verifyByName = @{}
    foreach ($line in $verifyOutput) {
        $match = [regex]::Match([string]$line, '^\s*(\d+)\s+(.+?)\s*$')
        if ($match.Success) {
            $verifyByName[$match.Groups[2].Value] = [long]$match.Groups[1].Value
        }
    }

    foreach ($file in $PackFiles) {
        $relativeName = "files/mobile_pack/$($file.name)"
        if (-not $verifyByName.ContainsKey($relativeName)) {
            return $false
        }
        if ($verifyByName[$relativeName] -ne $file.expectedBytes) {
            return $false
        }
    }
    return $true
}

function Quote-AndroidShellArg {
    param([string]$Value)

    if ($null -eq $Value) {
        return "''"
    }

    return "'" + $Value.Replace("'", "'\''") + "'"
}

function Invoke-AdbChecked {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments,
        [int]$TimeoutMilliseconds = $AdbCommandTimeoutMilliseconds,
        [switch]$AllowFailure
    )

    $result = Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $Arguments -TimeoutMilliseconds $TimeoutMilliseconds
    $script:LastAdbExitCode = [int]$result.exit_code
    $output = if ([string]::IsNullOrWhiteSpace([string]$result.output)) {
        @()
    } else {
        @(([string]$result.output) -split "`r?`n")
    }

    if (-not $AllowFailure -and $script:LastAdbExitCode -ne 0) {
        $joined = $Arguments -join " "
        $message = ($output | Out-String).Trim()
        if ($result.timed_out) {
            throw "adb timed out after ${TimeoutMilliseconds}ms ($joined): $message"
        }
        throw "adb failed ($joined): $message"
    }
    return $output
}

function Get-ManifestSummary {
    param([string]$ManifestPath)

    $manifest = Get-Content $ManifestPath -Raw | ConvertFrom-Json
    $sqlitePath = [string]$manifest.files.sqlite.path
    $vectorPath = [string]$manifest.files.vectors.path
    if ([string]::IsNullOrWhiteSpace($sqlitePath) -or [string]::IsNullOrWhiteSpace($vectorPath)) {
        throw "Manifest missing sqlite/vector file paths: $ManifestPath"
    }

    return [pscustomobject]@{
        manifest = $manifest
        sqliteName = [System.IO.Path]::GetFileName($sqlitePath)
        vectorName = [System.IO.Path]::GetFileName($vectorPath)
    }
}

$resolvedPackDir = Resolve-TargetPath -Path $PackDir
if (-not (Test-Path $resolvedPackDir)) {
    throw "Pack directory not found: $resolvedPackDir"
}

$manifestPath = Join-Path $resolvedPackDir "senku_manifest.json"
if (-not (Test-Path $manifestPath)) {
    throw "Manifest not found: $manifestPath"
}

$manifestSummary = Get-ManifestSummary -ManifestPath $manifestPath
$packFiles = @(
    @{
        local = $manifestPath
        name = "senku_manifest.json"
        expectedBytes = [long](Get-Item $manifestPath).Length
    },
    @{
        local = (Join-Path $resolvedPackDir $manifestSummary.sqliteName)
        name = $manifestSummary.sqliteName
        expectedBytes = [long]$manifestSummary.manifest.files.sqlite.bytes
    },
    @{
        local = (Join-Path $resolvedPackDir $manifestSummary.vectorName)
        name = $manifestSummary.vectorName
        expectedBytes = [long]$manifestSummary.manifest.files.vectors.bytes
    }
)

foreach ($file in $packFiles) {
    if (-not (Test-Path $file.local)) {
        throw "Pack file missing: $($file.local)"
    }
}

$appPackDir = "/data/user/0/$PackageName/files/mobile_pack"
$harnessStateDir = Join-Path $repoRoot "artifacts\harness_state"
$statePath = Join-Path $harnessStateDir ("mobile_pack_push_" + (Get-SafeStateName -Value $Device) + ".json")
$packFingerprints = @(
    foreach ($file in $packFiles) {
        Get-PackFileFingerprint -Path $file.local -Name $file.name -ExpectedBytes $file.expectedBytes
    }
)
$packFingerprint = [string]::Join(
    "|",
    @(
        foreach ($file in $packFingerprints) {
            "{0}:{1}:{2}" -f $file.name, $file.length, $file.sha256
        }
    )
)
$pushState = [pscustomobject]@{
    schema_version = 1
    device = $Device
    package_name = $PackageName
    app_pack_dir = $appPackDir
    pack_dir = $resolvedPackDir
    pack_version = $manifestSummary.manifest.pack_version
    pack_fingerprint = $packFingerprint
    files = $packFingerprints
}
$cacheHit = $false
$stateMatches = Test-MobilePackPushStateCurrent -StatePath $statePath -ExpectedState $pushState

Write-Host "Pushing mobile pack to $Device"
Write-Host "Pack dir: $resolvedPackDir"
Write-Host "App pack dir: $appPackDir"
Write-Host ("Pack version: {0} | sqlite: {1} | vector: {2}" -f `
    $manifestSummary.manifest.pack_version,
    $manifestSummary.sqliteName,
    $manifestSummary.vectorName)
Write-Host ("Mobile pack push cache state: {0}" -f $statePath)

if ($ForcePush -and $SkipIfCurrent) {
    Write-Host "ForcePush requested; ignoring SkipIfCurrent cache state."
}

Invoke-AdbChecked -Arguments @("-s", $Device, "wait-for-device") | Out-Null
$runAsProbe = Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "pwd") -AllowFailure
if ($script:LastAdbExitCode -ne 0) {
    $message = ($runAsProbe | Out-String).Trim()
    throw "run-as failed for $PackageName on $Device. Install a debuggable build first. Output: $message"
}

if (-not $ForcePush -and $SkipIfCurrent -and $stateMatches -and (Test-InstalledPackFilesCurrent -PackFiles $packFiles -ManifestName "senku_manifest.json")) {
    $cacheHit = $true
    Write-Host "Mobile pack push cache hit: true"
    Write-Host "Local pack fingerprint and installed file sizes match prior verified push; skipping adb upload."
    $summaryObject = [pscustomobject]@{
        run_completed_utc = (Get-Date).ToUniversalTime().ToString("o")
        device = $Device
        package_name = $PackageName
        pack_dir = $resolvedPackDir
        app_pack_dir = $appPackDir
        state_path = $statePath
        cache_hit = [bool]$cacheHit
        skip_if_current = [bool]$SkipIfCurrent
        force_push = [bool]$ForcePush
        pushed = $false
        pack_version = $manifestSummary.manifest.pack_version
        pack_fingerprint = $packFingerprint
    }
    Write-MobilePackPushSummary -Summary $summaryObject -Path $SummaryPath
    return
}

Write-Host "Mobile pack push cache hit: false"

if ($ForceStop -or $RestartApp) {
    Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "am", "force-stop", $PackageName) | Out-Null
}

Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "rm", "-rf", $RemoteTempDir) | Out-Null
Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "mkdir", "-p", $RemoteTempDir) | Out-Null
Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "mkdir", "-p", $appPackDir) | Out-Null

foreach ($file in $packFiles) {
    $remoteFile = "$RemoteTempDir/$($file.name)"
    Write-Host "Uploading $($file.name)..."
    Invoke-AdbChecked -Arguments @("-s", $Device, "push", $file.local, $remoteFile) -TimeoutMilliseconds $AdbPushTimeoutMilliseconds | Out-Null
    Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "cp", $remoteFile, "$appPackDir/$($file.name)") | Out-Null
}

$verifyOutput = Invoke-AdbChecked -Arguments @(
    "-s", $Device, "shell", "run-as", $PackageName, "wc", "-c",
    "files/mobile_pack/$($manifestSummary.sqliteName)",
    "files/mobile_pack/$($manifestSummary.vectorName)",
    "files/mobile_pack/senku_manifest.json"
)

$verifyByName = @{}
foreach ($line in $verifyOutput) {
    $match = [regex]::Match([string]$line, '^\s*(\d+)\s+(.+?)\s*$')
    if ($match.Success) {
        $verifyByName[$match.Groups[2].Value] = [long]$match.Groups[1].Value
    }
}

foreach ($file in $packFiles) {
    $relativeName = "files/mobile_pack/$($file.name)"
    if (-not $verifyByName.ContainsKey($relativeName)) {
        throw "Installed file missing from verification output: $relativeName"
    }
    if ($verifyByName[$relativeName] -ne $file.expectedBytes) {
        throw "Installed file size mismatch for $($file.name): expected $($file.expectedBytes), got $($verifyByName[$relativeName])"
    }
}

Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "rm", "-rf", $RemoteTempDir) | Out-Null
New-Item -ItemType Directory -Force -Path $harnessStateDir | Out-Null
$verifiedPushState = [pscustomobject]@{
    schema_version = $pushState.schema_version
    updated_utc = (Get-Date).ToUniversalTime().ToString("o")
    device = $pushState.device
    package_name = $pushState.package_name
    app_pack_dir = $pushState.app_pack_dir
    pack_dir = $pushState.pack_dir
    pack_version = $pushState.pack_version
    pack_fingerprint = $pushState.pack_fingerprint
    files = $pushState.files
}
($verifiedPushState | ConvertTo-Json -Depth 6) | Set-Content -LiteralPath $statePath -Encoding UTF8

if ($RestartApp) {
    Write-Host "Restarting app..."
    Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "am", "start", "-n", "$PackageName/$ActivityName") | Out-Null
}

Write-Host ""
Write-Host "Installed pack verification:"
($verifyOutput | Out-String).TrimEnd() | Write-Host

if ($ShowInstalledManifest) {
    Write-Host ""
    Write-Host "Installed manifest:"
    $manifestOutput = Invoke-AdbChecked -Arguments @(
        "-s", $Device, "shell", "run-as", $PackageName, "cat", "$appPackDir/senku_manifest.json"
    )
    ($manifestOutput | Out-String).TrimEnd() | Write-Host
} else {
    Write-Host ""
    Write-Host "Installed manifest preview:"
    $manifestPreview = Invoke-AdbChecked -Arguments @(
        "-s", $Device, "shell", "run-as", $PackageName, "cat", "files/mobile_pack/senku_manifest.json"
    )
    ($manifestPreview | Select-Object -First 20 | Out-String).TrimEnd() | Write-Host
}

$summaryObject = [pscustomobject]@{
    run_completed_utc = (Get-Date).ToUniversalTime().ToString("o")
    device = $Device
    package_name = $PackageName
    pack_dir = $resolvedPackDir
    app_pack_dir = $appPackDir
    state_path = $statePath
    cache_hit = [bool]$cacheHit
    skip_if_current = [bool]$SkipIfCurrent
    force_push = [bool]$ForcePush
    pushed = $true
    pack_version = $manifestSummary.manifest.pack_version
    pack_fingerprint = $packFingerprint
}
Write-MobilePackPushSummary -Summary $summaryObject -Path $SummaryPath
