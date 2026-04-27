param(
    [string]$Device = "emulator-5556",
    [Parameter(Mandatory = $true)]
    [string]$OutputDir,
    [string]$ConfirmDevice = "",
    [string]$PackageName = "com.senku.mobile",
    [string]$ModelPath = ""
)

$ErrorActionPreference = "Stop"
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path -LiteralPath $adb)) {
    throw "adb not found at $adb"
}

$outputRoot = [System.IO.Path]::GetFullPath((Join-Path $repoRoot $OutputDir))
New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null
$payloadDir = Join-Path $outputRoot "payloads"
$runDir = Join-Path $outputRoot "runs"
$logDir = Join-Path $outputRoot "logs"
New-Item -ItemType Directory -Force -Path $payloadDir, $runDir, $logDir | Out-Null

$controlPayloadPath = Join-Path $payloadDir "control_ascii_256b.bin"
$binaryPayloadPath = Join-Path $payloadDir "binary_random_64mb.bin"
$artifactSummaryPath = Join-Path $outputRoot "summary.md"
$artifactResultsPath = Join-Path $outputRoot "results.json"
$environmentPath = Join-Path $outputRoot "environment.txt"
$deviceRootDir = "files/transport_probe"
$deviceAbsoluteRoot = "/data/user/0/$PackageName/files/transport_probe"
$remoteTmpRoot = "/data/local/tmp/senku_litert_transport_probe"

function Get-NowUtcString {
    return [DateTime]::UtcNow.ToString("o")
}

function Get-SafeName {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Value
    )

    return (($Value -replace '[^A-Za-z0-9._-]', '_').Trim('_'))
}

function Join-CommandText {
    param(
        [string[]]$Arguments
    )

    return ($Arguments -join " ")
}

function Get-TextValue {
    param(
        $Value
    )

    if ($null -eq $Value) {
        return ""
    }
    return [string]$Value
}

function Get-FileSha256 {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    return (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
}

function Invoke-NativeCapture {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath,
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $stdoutFile = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_transport_" + [guid]::NewGuid().ToString("N") + ".out")
    $stderrFile = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_transport_" + [guid]::NewGuid().ToString("N") + ".err")
    $started = [DateTime]::UtcNow
    try {
        $process = Start-Process -FilePath $FilePath `
            -ArgumentList $Arguments `
            -NoNewWindow `
            -Wait `
            -PassThru `
            -RedirectStandardOutput $stdoutFile `
            -RedirectStandardError $stderrFile
        $stdout = if (Test-Path -LiteralPath $stdoutFile) { Get-TextValue (Get-Content -LiteralPath $stdoutFile -Raw) } else { "" }
        $stderr = if (Test-Path -LiteralPath $stderrFile) { Get-TextValue (Get-Content -LiteralPath $stderrFile -Raw) } else { "" }
    } finally {
        if (Test-Path -LiteralPath $stdoutFile) {
            Remove-Item -LiteralPath $stdoutFile -Force
        }
        if (Test-Path -LiteralPath $stderrFile) {
            Remove-Item -LiteralPath $stderrFile -Force
        }
    }

    return [pscustomobject]@{
        file_path = $FilePath
        arguments = @($Arguments)
        command_text = $FilePath + " " + (Join-CommandText -Arguments $Arguments)
        exit_code = $process.ExitCode
        stdout = $stdout
        stderr = $stderr
        started_utc = $started.ToString("o")
        duration_ms = [int]([DateTime]::UtcNow - $started).TotalMilliseconds
    }
}

function Resolve-LiteRtModelPath {
    param(
        [string]$ExplicitPath
    )

    if (-not [string]::IsNullOrWhiteSpace($ExplicitPath)) {
        if (-not (Test-Path -LiteralPath $ExplicitPath)) {
            throw "ModelPath not found: $ExplicitPath"
        }
        return (Resolve-Path -LiteralPath $ExplicitPath).Path
    }

    $userHome = [Environment]::GetFolderPath("UserProfile")
    $candidateNames = @(
        "gemma-4-E2B-it.litertlm",
        "gemma-4-E2B-it.task",
        "gemma-4-E4B-it.litertlm",
        "gemma-4-E4B-it.task"
    )
    foreach ($candidateName in $candidateNames) {
        $candidatePaths = @(
            (Join-Path $repoRoot $candidateName),
            (Join-Path $repoRoot ("models\" + $candidateName)),
            (Join-Path $userHome ("Downloads\" + $candidateName))
        )
        foreach ($candidatePath in $candidatePaths) {
            if (Test-Path -LiteralPath $candidatePath) {
                return (Resolve-Path -LiteralPath $candidatePath).Path
            }
        }
    }
    return ""
}

function New-ControlPayload {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $seed = "SENKU-LITERT-CONTROL-ASCII-0123456789-"
    $builder = New-Object System.Text.StringBuilder
    while ($builder.Length -lt 256) {
        [void]$builder.Append($seed)
    }
    $text = $builder.ToString().Substring(0, 256)
    $bytes = [System.Text.Encoding]::ASCII.GetBytes($text)
    [System.IO.File]::WriteAllBytes($Path, $bytes)
}

function New-RandomPayload {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [long]$ByteCount
    )

    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $buffer = New-Object byte[] (1024 * 1024)
    $remaining = $ByteCount
    $stream = [System.IO.File]::Open($Path, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write, [System.IO.FileShare]::None)
    try {
        while ($remaining -gt 0) {
            $chunkLength = [Math]::Min($buffer.Length, [int]$remaining)
            $rng.GetBytes($buffer)
            $stream.Write($buffer, 0, $chunkLength)
            $remaining -= $chunkLength
        }
    } finally {
        $stream.Dispose()
        $rng.Dispose()
    }
}

function Invoke-Adb {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    return Invoke-NativeCapture -FilePath $adb -Arguments $Arguments
}

function Get-HostAdbVersionEvidence {
    $versionResult = Invoke-Adb -Arguments @("version")
    $versionText = (($versionResult.stdout + [Environment]::NewLine + $versionResult.stderr).Trim())
    $versionLine = ""
    $platformToolsVersion = ""

    foreach ($line in ($versionText -split "`r?`n")) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed)) {
            continue
        }
        if ([string]::IsNullOrWhiteSpace($versionLine) -and $trimmed -match "^Android Debug Bridge version\b") {
            $versionLine = $trimmed
        }
        if ([string]::IsNullOrWhiteSpace($platformToolsVersion) -and $trimmed -match "^Version\s+(.+)$") {
            $platformToolsVersion = $Matches[1].Trim()
        }
    }

    return [pscustomobject]@{
        adb_path = $adb
        adb_version_exit_code = $versionResult.exit_code
        adb_version_line = $versionLine
        adb_platform_tools_version = $platformToolsVersion
        adb_version_output = $versionText
    }
}

function Assert-DeviceReady {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial
    )

    $waitResult = Invoke-Adb -Arguments @("-s", $Serial, "wait-for-device")
    if ($waitResult.exit_code -ne 0) {
        throw "wait-for-device failed for ${Serial}: $($waitResult.stderr.Trim())"
    }

    $runAsProbe = Invoke-Adb -Arguments @("-s", $Serial, "shell", "run-as", $PackageName, "pwd")
    if ($runAsProbe.exit_code -ne 0) {
        $details = (($runAsProbe.stdout + "`n" + $runAsProbe.stderr).Trim())
        throw "run-as failed for $PackageName on $Serial. $details"
    }
}

function Initialize-RemoteDirs {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial
    )

    $tmpResult = Invoke-Adb -Arguments @("-s", $Serial, "shell", "mkdir", "-p", $remoteTmpRoot)
    if ($tmpResult.exit_code -ne 0) {
        throw "Failed to create tmp probe dir on ${Serial}: $($tmpResult.stderr.Trim())"
    }

    $deviceDirResult = Invoke-Adb -Arguments @("-s", $Serial, "shell", "run-as", $PackageName, "mkdir", "-p", $deviceAbsoluteRoot)
    if ($deviceDirResult.exit_code -ne 0) {
        throw "Failed to create app probe dir on ${Serial}: $($deviceDirResult.stderr.Trim())"
    }
}

function Remove-RemotePath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial,
        [Parameter(Mandatory = $true)]
        [string]$PathToRemove,
        [switch]$RunAs
    )

    if ($RunAs) {
        return Invoke-Adb -Arguments @("-s", $Serial, "shell", "run-as", $PackageName, "rm", "-f", $PathToRemove)
    }
    return Invoke-Adb -Arguments @("-s", $Serial, "shell", "rm", "-f", $PathToRemove)
}

function Get-RemoteMetadata {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial,
        [Parameter(Mandatory = $true)]
        [string]$AbsolutePath
    )

    $sizeResult = Invoke-Adb -Arguments @(
        "-s", $Serial, "shell", "run-as", $PackageName, "wc", "-c", $AbsolutePath
    )
    $hashResult = Invoke-Adb -Arguments @(
        "-s", $Serial, "shell", "run-as", $PackageName, "sha256sum", $AbsolutePath
    )

    $remoteBytes = $null
    if ($sizeResult.exit_code -eq 0) {
        $sizeText = (($sizeResult.stdout.Trim() -split '\s+') | Select-Object -First 1)
        [long]$parsedBytes = 0
        if ([long]::TryParse($sizeText, [ref]$parsedBytes)) {
            $remoteBytes = $parsedBytes
        } else {
            $remoteBytes = $null
        }
    }

    $remoteSha256 = ""
    if ($hashResult.exit_code -eq 0) {
        $tokens = ($hashResult.stdout.Trim() -split '\s+')
        if ($tokens.Length -gt 0) {
            $remoteSha256 = $tokens[0].ToLowerInvariant()
        }
    }

    return [pscustomobject]@{
        size_result = $sizeResult
        hash_result = $hashResult
        remote_bytes = $remoteBytes
        remote_sha256 = $remoteSha256
    }
}

function Invoke-TmpStagingMethod {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial,
        [Parameter(Mandatory = $true)]
        [pscustomobject]$Payload,
        [Parameter(Mandatory = $true)]
        [string]$RemoteRelativePath,
        [Parameter(Mandatory = $true)]
        [string]$RemoteAbsolutePath,
        [Parameter(Mandatory = $true)]
        [string]$RemoteTmpPath
    )

    $steps = @()
    $steps += Invoke-Adb -Arguments @("-s", $Serial, "push", $Payload.path, $RemoteTmpPath)
    if ($steps[-1].exit_code -eq 0) {
        $steps += Invoke-Adb -Arguments @("-s", $Serial, "shell", "run-as", $PackageName, "cp", $RemoteTmpPath, $RemoteAbsolutePath)
    }

    return $steps
}

function Invoke-CmdScriptMethod {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial,
        [Parameter(Mandatory = $true)]
        [pscustomobject]$Payload,
        [Parameter(Mandatory = $true)]
        [string]$RemoteAbsolutePath,
        [Parameter(Mandatory = $true)]
        [ValidateSet("redirect", "type_pipe")]
        [string]$Mode
    )

    $tempScript = Join-Path ([System.IO.Path]::GetTempPath()) ("senku_transport_" + [guid]::NewGuid().ToString("N") + ".cmd")
    try {
        if ($Mode -eq "redirect") {
            $scriptLine = '"{0}" -s {1} shell run-as {2} sh -c "cat > ''{3}''" < "{4}"' -f $adb, $Serial, $PackageName, $RemoteAbsolutePath, $Payload.path
        } else {
            $scriptLine = 'type "{4}" | "{0}" -s {1} shell run-as {2} sh -c "cat > ''{3}''"' -f $adb, $Serial, $PackageName, $RemoteAbsolutePath, $Payload.path
        }
        $scriptContent = "@echo off`r`n" + $scriptLine + "`r`n"
        [System.IO.File]::WriteAllText($tempScript, $scriptContent, [System.Text.Encoding]::ASCII)
        return @(Invoke-NativeCapture -FilePath "cmd.exe" -Arguments @("/d", "/c", $tempScript))
    } finally {
        if (Test-Path -LiteralPath $tempScript) {
            Remove-Item -LiteralPath $tempScript -Force
        }
    }
}

function Invoke-ProcessStreamMethod {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial,
        [Parameter(Mandatory = $true)]
        [pscustomobject]$Payload,
        [Parameter(Mandatory = $true)]
        [string]$RemoteAbsolutePath
    )

    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = $adb
    $processInfo.Arguments = ('-s {0} shell run-as {1} sh -c "cat > ''{2}''"' -f $Serial, $PackageName, $RemoteAbsolutePath)
    $processInfo.UseShellExecute = $false
    $processInfo.RedirectStandardInput = $true
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $processInfo
    $started = [DateTime]::UtcNow
    $stdout = ""
    $stderr = ""
    $exitCode = -1
    $copyErrorText = ""
    $null = $process.Start()
    $payloadStream = [System.IO.File]::OpenRead($Payload.path)
    try {
        $payloadStream.CopyTo($process.StandardInput.BaseStream)
        $process.StandardInput.BaseStream.Flush()
        $process.StandardInput.Close()
        $stdout = $process.StandardOutput.ReadToEnd()
        $stderr = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        $exitCode = $process.ExitCode
    } catch {
        $copyErrorText = $_.Exception.Message
        try {
            $process.StandardInput.Close()
        } catch {
        }
        try {
            $stdout = Get-TextValue ($process.StandardOutput.ReadToEnd())
        } catch {
            $stdout = ""
        }
        try {
            $stderr = Get-TextValue ($process.StandardError.ReadToEnd())
        } catch {
            $stderr = ""
        }
        try {
            $process.WaitForExit(30000) | Out-Null
        } catch {
        }
        if ($process.HasExited) {
            $exitCode = $process.ExitCode
        } else {
            try {
                $process.Kill()
            } catch {
            }
            $exitCode = -1
        }
    } finally {
        $payloadStream.Dispose()
        $process.Dispose()
    }

    if (-not [string]::IsNullOrWhiteSpace($copyErrorText)) {
        $stderr = ((Get-TextValue $stderr).TrimEnd() + [Environment]::NewLine + "HOST COPY ERROR: $copyErrorText").Trim()
    }

    return @([pscustomobject]@{
        file_path = $adb
        arguments = @($processInfo.Arguments)
        command_text = $adb + " " + $processInfo.Arguments
        exit_code = $exitCode
        stdout = $stdout
        stderr = $stderr
        started_utc = $started.ToString("o")
        duration_ms = [int]([DateTime]::UtcNow - $started).TotalMilliseconds
    })
}

function Get-CombinedOutputText {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Steps
    )

    $lines = New-Object System.Collections.Generic.List[string]
    foreach ($step in $Steps) {
        $lines.Add("COMMAND: $($step.command_text)")
        $lines.Add("EXIT: $($step.exit_code)")
        if (-not [string]::IsNullOrWhiteSpace($step.stdout)) {
            $lines.Add("STDOUT:")
            $lines.Add($step.stdout.TrimEnd())
        }
        if (-not [string]::IsNullOrWhiteSpace($step.stderr)) {
            $lines.Add("STDERR:")
            $lines.Add($step.stderr.TrimEnd())
        }
        $lines.Add("")
    }
    return ($lines -join [Environment]::NewLine).TrimEnd()
}

function Get-Verdict {
    param(
        [Parameter(Mandatory = $true)]
        [pscustomobject]$Payload,
        [Parameter(Mandatory = $true)]
        [object[]]$TransportSteps,
        [Parameter(Mandatory = $true)]
        [pscustomobject]$RemoteMetadata
    )

    $combinedOutput = (Get-CombinedOutputText -Steps $TransportSteps) + [Environment]::NewLine +
        "REMOTE SIZE EXIT: $($RemoteMetadata.size_result.exit_code)" + [Environment]::NewLine +
        "REMOTE HASH EXIT: $($RemoteMetadata.hash_result.exit_code)" + [Environment]::NewLine +
        $RemoteMetadata.size_result.stdout + [Environment]::NewLine +
        $RemoteMetadata.size_result.stderr + [Environment]::NewLine +
        $RemoteMetadata.hash_result.stdout + [Environment]::NewLine +
        $RemoteMetadata.hash_result.stderr

    if ($combinedOutput -match "No space left on device") {
        return "unresolved"
    }

    $transportFailed = ($TransportSteps | Where-Object { $_.exit_code -ne 0 } | Select-Object -First 1)
    if ($null -ne $transportFailed) {
        return "unsafe"
    }

    if ($RemoteMetadata.remote_bytes -ne $Payload.expected_bytes) {
        return "unsafe"
    }

    if ([string]::IsNullOrWhiteSpace($RemoteMetadata.remote_sha256)) {
        return "unresolved"
    }

    if ($RemoteMetadata.remote_sha256 -ne $Payload.local_sha256) {
        return "unsafe"
    }

    return "safe"
}

function Get-NotesText {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Verdict,
        [Parameter(Mandatory = $true)]
        [pscustomobject]$RemoteMetadata,
        [Parameter(Mandatory = $true)]
        [object[]]$TransportSteps
    )

    if ($Verdict -eq "safe") {
        return "remote bytes and sha256 matched local payload"
    }

    $allText = ((Get-CombinedOutputText -Steps $TransportSteps) + [Environment]::NewLine +
        $RemoteMetadata.size_result.stdout + [Environment]::NewLine +
        $RemoteMetadata.size_result.stderr + [Environment]::NewLine +
        $RemoteMetadata.hash_result.stdout + [Environment]::NewLine +
        $RemoteMetadata.hash_result.stderr).Trim()

    if ($allText -match "No space left on device") {
        return "storage-confounded failure"
    }

    if ($RemoteMetadata.remote_bytes -ne $null -and $RemoteMetadata.remote_bytes -ne 0) {
        return ("remote bytes/hash mismatch; remote_bytes={0}; remote_sha256={1}" -f $RemoteMetadata.remote_bytes, $RemoteMetadata.remote_sha256)
    }

    $firstError = ($allText -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -First 1)
    if ([string]::IsNullOrWhiteSpace($firstError)) {
        return "no remote file metadata available"
    }
    return $firstError
}

function Invoke-ProbeRun {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial,
        [Parameter(Mandatory = $true)]
        [pscustomobject]$Payload,
        [Parameter(Mandatory = $true)]
        [string]$Method
    )

    $remoteFileName = "{0}__{1}__{2}" -f (Get-SafeName -Value $Method), (Get-SafeName -Value $Payload.name), (Get-SafeName -Value $Serial)
    $remoteRelativePath = "$deviceRootDir/$remoteFileName"
    $remoteAbsolutePath = "/data/user/0/$PackageName/$remoteRelativePath"
    $remoteTmpPath = "$remoteTmpRoot/$remoteFileName"

    $cleanupBeforeApp = Remove-RemotePath -Serial $Serial -PathToRemove $remoteAbsolutePath -RunAs
    $cleanupBeforeTmp = Remove-RemotePath -Serial $Serial -PathToRemove $remoteTmpPath

    $transportSteps = switch ($Method) {
        "tmp_staging_push_cp" {
            Invoke-TmpStagingMethod -Serial $Serial -Payload $Payload -RemoteRelativePath $remoteRelativePath -RemoteAbsolutePath $remoteAbsolutePath -RemoteTmpPath $remoteTmpPath
            break
        }
        "cmd_redirect_cat" {
            Invoke-CmdScriptMethod -Serial $Serial -Payload $Payload -RemoteAbsolutePath $remoteAbsolutePath -Mode "redirect"
            break
        }
        "cmd_type_pipe_cat" {
            Invoke-CmdScriptMethod -Serial $Serial -Payload $Payload -RemoteAbsolutePath $remoteAbsolutePath -Mode "type_pipe"
            break
        }
        "process_stdin_copy_cat" {
            Invoke-ProcessStreamMethod -Serial $Serial -Payload $Payload -RemoteAbsolutePath $remoteAbsolutePath
            break
        }
        default {
            throw "Unknown probe method: $Method"
        }
    }

    $remoteMetadata = Get-RemoteMetadata -Serial $Serial -AbsolutePath $remoteAbsolutePath
    $cleanupApp = Remove-RemotePath -Serial $Serial -PathToRemove $remoteAbsolutePath -RunAs
    $cleanupTmp = Remove-RemotePath -Serial $Serial -PathToRemove $remoteTmpPath
    $verdict = Get-Verdict -Payload $Payload -TransportSteps $transportSteps -RemoteMetadata $remoteMetadata
    $noteText = Get-NotesText -Verdict $verdict -RemoteMetadata $remoteMetadata -TransportSteps $transportSteps
    $logFileName = "{0}__{1}__{2}.txt" -f (Get-SafeName -Value $Method), (Get-SafeName -Value $Payload.name), (Get-SafeName -Value $Serial)
    $logPath = Join-Path $logDir $logFileName
    $combinedLog = @(
        "method=$Method"
        "serial=$Serial"
        "payload=$($Payload.name)"
        "expected_bytes=$($Payload.expected_bytes)"
        "local_sha256=$($Payload.local_sha256)"
        ""
        "pre_cleanup_app_exit=$($cleanupBeforeApp.exit_code)"
        "pre_cleanup_tmp_exit=$($cleanupBeforeTmp.exit_code)"
        ""
        (Get-CombinedOutputText -Steps $transportSteps)
        ""
        "REMOTE SIZE COMMAND:"
        $remoteMetadata.size_result.command_text
        "REMOTE SIZE EXIT:"
        "$($remoteMetadata.size_result.exit_code)"
        "REMOTE SIZE STDOUT:"
        (Get-TextValue $remoteMetadata.size_result.stdout).TrimEnd()
        "REMOTE SIZE STDERR:"
        (Get-TextValue $remoteMetadata.size_result.stderr).TrimEnd()
        ""
        "REMOTE HASH COMMAND:"
        $remoteMetadata.hash_result.command_text
        "REMOTE HASH EXIT:"
        "$($remoteMetadata.hash_result.exit_code)"
        "REMOTE HASH STDOUT:"
        (Get-TextValue $remoteMetadata.hash_result.stdout).TrimEnd()
        "REMOTE HASH STDERR:"
        (Get-TextValue $remoteMetadata.hash_result.stderr).TrimEnd()
        ""
        "cleanup_app_exit=$($cleanupApp.exit_code)"
        "cleanup_tmp_exit=$($cleanupTmp.exit_code)"
        "verdict=$verdict"
        "notes=$noteText"
    ) -join [Environment]::NewLine
    Set-Content -LiteralPath $logPath -Value $combinedLog -Encoding UTF8

    $hashMatch = $false
    if (-not [string]::IsNullOrWhiteSpace($remoteMetadata.remote_sha256)) {
        $hashMatch = ($remoteMetadata.remote_sha256 -eq $Payload.local_sha256)
    }

    return [pscustomobject]@{
        method = $Method
        serial = $Serial
        payload = $Payload.name
        payload_class = $Payload.payload_class
        payload_path = $Payload.path
        expected_bytes = $Payload.expected_bytes
        local_sha256 = $Payload.local_sha256
        remote_relative_path = $remoteRelativePath
        remote_tmp_path = $remoteTmpPath
        remote_bytes = $remoteMetadata.remote_bytes
        remote_sha256 = $remoteMetadata.remote_sha256
        hash_match = $hashMatch
        verdict = $verdict
        notes = $noteText
        cleanup_app_exit = $cleanupApp.exit_code
        cleanup_tmp_exit = $cleanupTmp.exit_code
        transport_steps = @($transportSteps)
        log_path = $logPath
        executed_utc = Get-NowUtcString
    }
}

function Write-ArtifactSummary {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Results,
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("# LiteRT Transport Probe Summary")
    $lines.Add("")
    $lines.Add("| method | serial | payload | expected_bytes | remote_bytes | hash_match | verdict | notes |")
    $lines.Add("| --- | --- | --- | ---: | ---: | --- | --- | --- |")
    foreach ($result in $Results) {
        $remoteBytesText = if ($null -eq $result.remote_bytes) { "" } else { [string]$result.remote_bytes }
        $hashText = if ($result.hash_match) { "yes" } elseif ([string]::IsNullOrWhiteSpace($result.remote_sha256)) { "unknown" } else { "no" }
        $notesText = ($result.notes -replace '\|', '/')
        $lines.Add("| $($result.method) | $($result.serial) | $($result.payload) | $($result.expected_bytes) | $remoteBytesText | $hashText | $($result.verdict) | $notesText |")
    }
    Set-Content -LiteralPath $Path -Value ($lines -join [Environment]::NewLine) -Encoding UTF8
}

function Write-EnvironmentLog {
    param(
        [Parameter(Mandatory = $true)]
        [string]$PrimarySerial,
        [string]$SecondarySerial,
        [Parameter(Mandatory = $true)]
        [pscustomobject]$AdbVersionEvidence
    )

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("generated_utc=$(Get-NowUtcString)")
    $lines.Add("powershell_version=$($PSVersionTable.PSVersion)")
    $lines.Add("adb_path=$adb")
    $lines.Add("adb_version_line=$($AdbVersionEvidence.adb_version_line)")
    $lines.Add("adb_platform_tools_version=$($AdbVersionEvidence.adb_platform_tools_version)")
    $lines.Add("adb_version_exit_code=$($AdbVersionEvidence.adb_version_exit_code)")
    $lines.Add("primary_serial=$PrimarySerial")
    if (-not [string]::IsNullOrWhiteSpace($SecondarySerial)) {
        $lines.Add("confirm_serial=$SecondarySerial")
    }
    $lines.Add("")
    $lines.Add("adb_version:")
    $lines.Add($AdbVersionEvidence.adb_version_output)
    $lines.Add("")
    $lines.Add("adb_devices:")
    $lines.Add(((& $adb devices) | Out-String).TrimEnd())
    $lines.Add("")
    foreach ($serial in @($PrimarySerial, $SecondarySerial) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) {
        $lines.Add("df_${serial}:")
        $lines.Add(((& $adb -s $serial shell df -k /data /data/local/tmp) | Out-String).TrimEnd())
        $lines.Add("")
    }
    Set-Content -LiteralPath $environmentPath -Value ($lines -join [Environment]::NewLine) -Encoding UTF8
}

New-ControlPayload -Path $controlPayloadPath
New-RandomPayload -Path $binaryPayloadPath -ByteCount 67108864
$resolvedModelPath = Resolve-LiteRtModelPath -ExplicitPath $ModelPath

$payloads = New-Object System.Collections.Generic.List[object]
$payloads.Add([pscustomobject]@{
    name = "control_ascii_256b"
    payload_class = "control_ascii_256b"
    path = $controlPayloadPath
    expected_bytes = (Get-Item -LiteralPath $controlPayloadPath).Length
    local_sha256 = (Get-FileSha256 -Path $controlPayloadPath)
})
$payloads.Add([pscustomobject]@{
    name = "binary_random_64mb"
    payload_class = "binary_random_64mb"
    path = $binaryPayloadPath
    expected_bytes = (Get-Item -LiteralPath $binaryPayloadPath).Length
    local_sha256 = (Get-FileSha256 -Path $binaryPayloadPath)
})
if (-not [string]::IsNullOrWhiteSpace($resolvedModelPath)) {
    $payloads.Add([pscustomobject]@{
        name = [System.IO.Path]::GetFileName($resolvedModelPath)
        payload_class = "real_model_litert"
        path = $resolvedModelPath
        expected_bytes = (Get-Item -LiteralPath $resolvedModelPath).Length
        local_sha256 = (Get-FileSha256 -Path $resolvedModelPath)
    })
}

Assert-DeviceReady -Serial $Device
Initialize-RemoteDirs -Serial $Device
if (-not [string]::IsNullOrWhiteSpace($ConfirmDevice)) {
    Assert-DeviceReady -Serial $ConfirmDevice
    Initialize-RemoteDirs -Serial $ConfirmDevice
}

$adbVersionEvidence = Get-HostAdbVersionEvidence
Write-EnvironmentLog -PrimarySerial $Device -SecondarySerial $ConfirmDevice -AdbVersionEvidence $adbVersionEvidence

$results = New-Object System.Collections.Generic.List[object]
$primaryMatrix = @(
    @{ method = "tmp_staging_push_cp"; payloads = @("control_ascii_256b", "binary_random_64mb") },
    @{ method = "cmd_redirect_cat"; payloads = @("control_ascii_256b", "binary_random_64mb") },
    @{ method = "cmd_type_pipe_cat"; payloads = @("control_ascii_256b", "binary_random_64mb") },
    @{ method = "process_stdin_copy_cat"; payloads = @("control_ascii_256b", "binary_random_64mb") }
)

if ($payloads | Where-Object { $_.payload_class -eq "real_model_litert" }) {
    $primaryMatrix += @(
        @{ method = "cmd_redirect_cat"; payloads = @("real_model_litert") }
    )
}

foreach ($entry in $primaryMatrix) {
    foreach ($payloadName in $entry.payloads) {
        $payload = $payloads | Where-Object {
            $_.name -eq $payloadName -or $_.payload_class -eq $payloadName
        } | Select-Object -First 1
        if ($null -eq $payload) {
            continue
        }
        $results.Add((Invoke-ProbeRun -Serial $Device -Payload $payload -Method $entry.method))
    }
}

if (-not [string]::IsNullOrWhiteSpace($ConfirmDevice)) {
    $modelPayload = $payloads | Where-Object { $_.payload_class -eq "real_model_litert" } | Select-Object -First 1
    if ($null -ne $modelPayload) {
        $results.Add((Invoke-ProbeRun -Serial $ConfirmDevice -Payload $modelPayload -Method "cmd_redirect_cat"))
    }
}

$safeCount = ($results | Where-Object { $_.verdict -eq "safe" }).Count
$unsafeCount = ($results | Where-Object { $_.verdict -eq "unsafe" }).Count
$unresolvedCount = ($results | Where-Object { $_.verdict -eq "unresolved" }).Count
$resultsArtifact = [pscustomobject]@{
    summary = [pscustomobject]@{
        generated_utc = Get-NowUtcString
        adb_path = $adbVersionEvidence.adb_path
        adb_version_line = $adbVersionEvidence.adb_version_line
        adb_platform_tools_version = $adbVersionEvidence.adb_platform_tools_version
        adb_version_exit_code = $adbVersionEvidence.adb_version_exit_code
        safe_count = $safeCount
        unsafe_count = $unsafeCount
        unresolved_count = $unresolvedCount
    }
    results = @($results)
}
$resultsArtifact | ConvertTo-Json -Depth 7 | Set-Content -LiteralPath $artifactResultsPath -Encoding UTF8
Write-ArtifactSummary -Results $results -Path $artifactSummaryPath

Write-Host "Probe artifact: $outputRoot"
Write-Host "safe=$safeCount unsafe=$unsafeCount unresolved=$unresolvedCount"
