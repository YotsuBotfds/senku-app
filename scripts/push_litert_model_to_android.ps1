param(
    [string]$Device = "emulator-5556",
    [string]$ModelPath = "",
    [string]$PackageName = "com.senku.mobile",
    [string]$RemoteTempDir = "/data/local/tmp/senku_litert_model_push",
    [switch]$RestartApp,
    [switch]$ForceStop,
    [switch]$PruneExistingModels
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

function Resolve-DefaultLiteRtModelPath {
    $candidateNames = @(
        "gemma-4-E4B-it.litertlm",
        "gemma-4-E4B-it.task",
        "gemma-4-E2B-it.litertlm",
        "gemma-4-E2B-it.task"
    )
    $userHome = [Environment]::GetFolderPath("UserProfile")
    foreach ($candidateName in $candidateNames) {
        $candidates = @(
            (Join-Path $repoRoot $candidateName),
            (Join-Path $repoRoot ("models\" + $candidateName)),
            (Join-Path $userHome ("Downloads\" + $candidateName))
        )
        foreach ($candidate in $candidates) {
            if (Test-Path -LiteralPath $candidate) {
                return (Resolve-Path -LiteralPath $candidate).Path
            }
        }
    }
    return ""
}

function Get-DeviceDataAvailableBytes {
    $dfOutput = Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "df", "-k", "/data") -AllowFailure
    if ($script:LastAdbExitCode -ne 0) {
        return $null
    }

    foreach ($line in $dfOutput) {
        $trimmed = ([string]$line).Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith("Filesystem")) {
            continue
        }
        $parts = @($trimmed -split "\s+")
        if ($parts.Count -lt 4) {
            continue
        }
        $availableKb = 0L
        if ([long]::TryParse($parts[3], [ref]$availableKb)) {
            return ($availableKb * 1024L)
        }
    }
    return $null
}

function Assert-DeviceHasModelStagingSpace {
    param([long]$ModelBytes)

    $availableBytes = Get-DeviceDataAvailableBytes
    if ($null -eq $availableBytes) {
        Write-Warning "Could not determine free space on $Device /data; continuing with model push."
        return
    }

    $requiredBytes = ($ModelBytes * 2L) + 67108864L
    if ($availableBytes -lt $requiredBytes) {
        $availableGiB = [Math]::Round(($availableBytes / 1GB), 2)
        $requiredGiB = [Math]::Round(($requiredBytes / 1GB), 2)
        throw "Insufficient /data free space on $Device for staged LiteRT push. Available ${availableGiB} GiB; need about ${requiredGiB} GiB because the helper stages the model in /data/local/tmp before copying it into app storage."
    }
}

if ([string]::IsNullOrWhiteSpace($ModelPath)) {
    $ModelPath = Resolve-DefaultLiteRtModelPath
}

if ([string]::IsNullOrWhiteSpace($ModelPath) -or -not (Test-Path -LiteralPath $ModelPath)) {
    throw "LiteRT model not found. Pass -ModelPath or place an E4B/E2B .litertlm/.task in repo root, repo models/, or Downloads."
}

$resolvedModelPath = (Resolve-Path -LiteralPath $ModelPath).Path
$modelBytes = (Get-Item -LiteralPath $resolvedModelPath).Length
$modelFileName = [System.IO.Path]::GetFileName($resolvedModelPath)
$modelExtension = [System.IO.Path]::GetExtension($resolvedModelPath).ToLowerInvariant()
if ($modelExtension -ne ".litertlm" -and $modelExtension -ne ".task") {
    throw "Unsupported model extension: $modelExtension"
}

$appModelsDir = "/data/user/0/$PackageName/files/models"
$appPrefsDir = "/data/user/0/$PackageName/shared_prefs"
$appModelPath = "$appModelsDir/$modelFileName"
$prefsXml = @"
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="model_name">$modelFileName</string>
    <string name="model_path">$appModelPath</string>
</map>
"@

$tempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_litert_model_push_" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
$localPrefsPath = Join-Path $tempDir "senku_model_store.xml"
Set-Content -LiteralPath $localPrefsPath -Value $prefsXml -Encoding UTF8

try {
    Write-Host "Pushing LiteRT model to $Device"
    Write-Host "Model: $resolvedModelPath"
    Write-Host "Target: $appModelPath"

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
    Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "mkdir", "-p", $appModelsDir) | Out-Null
    Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "mkdir", "-p", $appPrefsDir) | Out-Null

    if ($PruneExistingModels) {
        $existingModels = Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "ls", "files/models") -AllowFailure
        if ($script:LastAdbExitCode -eq 0) {
            foreach ($line in $existingModels) {
                $trimmed = ([string]$line).Trim()
                if ([string]::IsNullOrWhiteSpace($trimmed)) {
                    continue
                }
                if ($trimmed.EndsWith(".litertlm") -or $trimmed.EndsWith(".task")) {
                    Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "rm", "-f", ("files/models/" + $trimmed)) | Out-Null
                }
            }
        }
    }

    Assert-DeviceHasModelStagingSpace -ModelBytes $modelBytes

    $remoteModelPath = "$RemoteTempDir/$modelFileName"
    $remotePrefsPath = "$RemoteTempDir/senku_model_store.xml"
    Invoke-AdbChecked -Arguments @("-s", $Device, "push", $resolvedModelPath, $remoteModelPath) | Out-Null
    Invoke-AdbChecked -Arguments @("-s", $Device, "push", $localPrefsPath, $remotePrefsPath) | Out-Null
    Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "cp", $remoteModelPath, $appModelPath) | Out-Null
    Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "cp", $remotePrefsPath, "$appPrefsDir/senku_model_store.xml") | Out-Null

    $verifyOutput = Invoke-AdbChecked -Arguments @(
        "-s", $Device, "shell", "run-as", $PackageName, "wc", "-c",
        "files/models/$modelFileName",
        "shared_prefs/senku_model_store.xml"
    )

    Write-Host ""
    Write-Host "Installed model verification:"
    ($verifyOutput | Out-String).TrimEnd() | Write-Host

    Write-Host ""
    Write-Host "Active model prefs:"
    $prefsOutput = Invoke-AdbChecked -Arguments @(
        "-s", $Device, "shell", "run-as", $PackageName, "cat", "shared_prefs/senku_model_store.xml"
    )
    ($prefsOutput | Out-String).TrimEnd() | Write-Host

    if ($RestartApp) {
        Write-Host ""
        Write-Host "Restarting app..."
        Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "monkey", "-p", $PackageName, "-c", "android.intent.category.LAUNCHER", "1") | Out-Null
    }
} finally {
    if (-not [string]::IsNullOrWhiteSpace($RemoteTempDir)) {
        try {
            Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "rm", "-rf", $RemoteTempDir) -AllowFailure | Out-Null
        } catch {
        }
    }
    if (Test-Path -LiteralPath $localPrefsPath) {
        Remove-Item -LiteralPath $localPrefsPath -Force
    }
    if (Test-Path -LiteralPath $tempDir) {
        Remove-Item -LiteralPath $tempDir -Force -Recurse
    }
}
