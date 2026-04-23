param(
    [string]$Device = "emulator-5554",
    [string]$PackDir = "android-app\\app\\src\\main\\assets\\mobile_pack",
    [string]$PackageName = "com.senku.mobile",
    [string]$ActivityName = ".MainActivity",
    [string]$RemoteTempDir = "/data/local/tmp/senku_mobile_pack_push",
    [switch]$RestartApp,
    [switch]$ForceStop,
    [switch]$ShowInstalledManifest
)

$ErrorActionPreference = "Stop"
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = Split-Path -Parent $PSScriptRoot
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
        [switch]$AllowFailure
    )

    $stdoutFile = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_adb_" + [guid]::NewGuid().ToString("N") + ".out")
    $stderrFile = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_adb_" + [guid]::NewGuid().ToString("N") + ".err")
    try {
        $process = Start-Process -FilePath $adb `
            -ArgumentList $Arguments `
            -NoNewWindow `
            -Wait `
            -PassThru `
            -RedirectStandardOutput $stdoutFile `
            -RedirectStandardError $stderrFile
        $stdout = if (Test-Path $stdoutFile) { Get-Content $stdoutFile } else { @() }
        $stderr = if (Test-Path $stderrFile) { Get-Content $stderrFile } else { @() }
        $output = @($stdout + $stderr)
    } finally {
        if (Test-Path $stdoutFile) {
            Remove-Item -LiteralPath $stdoutFile -Force
        }
        if (Test-Path $stderrFile) {
            Remove-Item -LiteralPath $stderrFile -Force
        }
    }

    $script:LastAdbExitCode = $process.ExitCode
    if (-not $AllowFailure -and $script:LastAdbExitCode -ne 0) {
        $joined = $Arguments -join " "
        $message = ($output | Out-String).Trim()
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

Write-Host "Pushing mobile pack to $Device"
Write-Host "Pack dir: $resolvedPackDir"
Write-Host "App pack dir: $appPackDir"
Write-Host ("Pack version: {0} | sqlite: {1} | vector: {2}" -f `
    $manifestSummary.manifest.pack_version,
    $manifestSummary.sqliteName,
    $manifestSummary.vectorName)

Invoke-AdbChecked -Arguments @("-s", $Device, "wait-for-device") | Out-Null
$runAsProbe = Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "pwd") -AllowFailure
if ($script:LastAdbExitCode -ne 0) {
    $message = ($runAsProbe | Out-String).Trim()
    throw "run-as failed for $PackageName on $Device. Install a debuggable build first. Output: $message"
}

if ($ForceStop -or $RestartApp) {
    Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "am", "force-stop", $PackageName) | Out-Null
}

Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "rm", "-rf", $RemoteTempDir) | Out-Null
Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "mkdir", "-p", $RemoteTempDir) | Out-Null
Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "mkdir", "-p", $appPackDir) | Out-Null

foreach ($file in $packFiles) {
    $remoteFile = "$RemoteTempDir/$($file.name)"
    Write-Host "Uploading $($file.name)..."
    Invoke-AdbChecked -Arguments @("-s", $Device, "push", $file.local, $remoteFile) | Out-Null
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
