param(
    [string[]]$Devices = @("emulator-5556", "emulator-5554"),
    [string[]]$LandscapeDevices = @("emulator-5560", "emulator-5558"),
    [string]$PromptPackFile = "",
    [switch]$InstallApk,
    [string]$ApkPath = "android-app/app/build/outputs/apk/debug/app-debug.apk",
    [ValidateSet("preserve", "local", "host")]
    [string]$E2BInferenceMode = "host",
    [string]$E2BHostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$E2BHostInferenceModel = "gemma-4-e2b-it-litert",
    [ValidateSet("preserve", "local", "host")]
    [string]$E4BInferenceMode = "host",
    [string]$E4BHostInferenceUrl = "http://10.0.2.2:1236/v1",
    [string]$E4BHostInferenceModel = "gemma-4-e4b-it-litert",
    [ValidateSet("portrait", "landscape")]
    [string]$Orientation = "portrait",
    [ValidateSet("portrait", "landscape")]
    [string]$LandscapeOrientation = "landscape",
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
$validationPackScript = Join-Path $PSScriptRoot "run_android_ui_validation_pack.ps1"
if (-not (Test-Path -LiteralPath $validationPackScript)) {
    throw "Missing script: $validationPackScript"
}

$benchRootRelative = "artifacts/bench"
$stamp = Get-Date -Format "yyyyMMdd_HHmmss_fff"
$baselineRelativeRoot = Join-Path $benchRootRelative ("e2b_e4b_diff_" + $stamp)
$baselineRoot = Join-Path $repoRoot $baselineRelativeRoot
$logsDir = Join-Path $baselineRoot "logs"
New-Item -ItemType Directory -Force -Path $logsDir | Out-Null

$resolvedPromptPackFile = ""
if (-not [string]::IsNullOrWhiteSpace($PromptPackFile)) {
    $candidatePromptPack = if ([System.IO.Path]::IsPathRooted($PromptPackFile)) {
        $PromptPackFile
    } else {
        Join-Path $repoRoot $PromptPackFile
    }
    if (-not (Test-Path -LiteralPath $candidatePromptPack)) {
        throw "Prompt pack file not found: $candidatePromptPack"
    }
    $resolvedPromptPackFile = (Resolve-Path -LiteralPath $candidatePromptPack).Path
}

function Convert-ToObjectArray {
    param([object]$InputObject)

    if ($null -eq $InputObject) {
        return @()
    }

    if ($InputObject -is [System.Collections.IEnumerable] -and -not ($InputObject -is [string])) {
        return @($InputObject)
    }

    return @($InputObject)
}

function Normalize-DeviceList {
    param([string[]]$InputDevices)

    $normalized = New-Object System.Collections.Generic.List[string]
    foreach ($entry in @($InputDevices)) {
        foreach ($split in ([string]$entry -split ",")) {
            $trimmed = $split.Trim()
            if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
                $normalized.Add($trimmed)
            }
        }
    }

    return @($normalized)
}

function Get-PropertyValue {
    param(
        [object]$InputObject,
        [string]$PropertyName
    )

    if ($null -eq $InputObject -or [string]::IsNullOrWhiteSpace($PropertyName)) {
        return $null
    }

    $property = $InputObject.PSObject.Properties[$PropertyName]
    if ($null -eq $property) {
        return $null
    }

    return $property.Value
}

function Format-InvariantDouble {
    param([double]$Value)

    return $Value.ToString([System.Globalization.CultureInfo]::InvariantCulture)
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

function Normalize-ComparableText {
    param([object]$Value)

    if ($null -eq $Value) {
        return ""
    }

    $text = [string]$Value
    if ([string]::IsNullOrWhiteSpace($text)) {
        return ""
    }

    return ([regex]::Replace($text.Trim(), "\s+", " "))
}

function Convert-ToRepoRelativePath {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }

    $fullPath = [System.IO.Path]::GetFullPath($Path)
    if ($fullPath.Equals($repoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        return "."
    }

    $repoRootWithSeparator = $repoRoot.TrimEnd("\", "/") + [System.IO.Path]::DirectorySeparatorChar
    if ($fullPath.StartsWith($repoRootWithSeparator, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $fullPath.Substring($repoRootWithSeparator.Length).Replace("\", "/")
    }

    return $fullPath
}

function Get-ResultRowKey {
    param([object]$Row)

    $device = Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "device")
    $caseId = Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "case_id")
    return ($device + "|" + $caseId)
}

function Get-ComparableRowSignature {
    param([object]$Row)

    if ($null -eq $Row) {
        return ""
    }

    $values = @(
        Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "status"),
        Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "detected_mode"),
        Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "instrumentation_status"),
        Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "screen_title"),
        Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "screen_meta"),
        Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "body_preview"),
        Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "run_error"),
        Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "has_detail_body"),
        Normalize-ComparableText (Get-PropertyValue -InputObject $Row -PropertyName "has_followup_input")
    )

    return ($values -join "|")
}

function New-RowSnapshot {
    param([object]$Row)

    if ($null -eq $Row) {
        return $null
    }

    $artifactDir = [string](Get-PropertyValue -InputObject $Row -PropertyName "artifact_dir")
    $screenshotPath = [string](Get-PropertyValue -InputObject $Row -PropertyName "screenshot_path")
    $dumpPath = [string](Get-PropertyValue -InputObject $Row -PropertyName "dump_path")
    $logcatPath = [string](Get-PropertyValue -InputObject $Row -PropertyName "logcat_path")

    return [pscustomobject]@{
        device = [string](Get-PropertyValue -InputObject $Row -PropertyName "device")
        case_id = [string](Get-PropertyValue -InputObject $Row -PropertyName "case_id")
        status = [string](Get-PropertyValue -InputObject $Row -PropertyName "status")
        detected_mode = [string](Get-PropertyValue -InputObject $Row -PropertyName "detected_mode")
        instrumentation_status = [string](Get-PropertyValue -InputObject $Row -PropertyName "instrumentation_status")
        elapsed_ms = Get-PropertyValue -InputObject $Row -PropertyName "elapsed_ms"
        screen_title = [string](Get-PropertyValue -InputObject $Row -PropertyName "screen_title")
        screen_meta = [string](Get-PropertyValue -InputObject $Row -PropertyName "screen_meta")
        body_preview = [string](Get-PropertyValue -InputObject $Row -PropertyName "body_preview")
        run_error = [string](Get-PropertyValue -InputObject $Row -PropertyName "run_error")
        artifact_dir = $artifactDir
        artifact_dir_relative = Convert-ToRepoRelativePath -Path $artifactDir
        screenshot_path = $screenshotPath
        screenshot_path_relative = Convert-ToRepoRelativePath -Path $screenshotPath
        dump_path = $dumpPath
        dump_path_relative = Convert-ToRepoRelativePath -Path $dumpPath
        logcat_path = $logcatPath
        logcat_path_relative = Convert-ToRepoRelativePath -Path $logcatPath
    }
}

function Invoke-ValidationPackRun {
    param(
        [string]$LaneKey,
        [string[]]$RunDevices,
        [string]$RunOrientation,
        [string]$InferenceMode,
        [string]$HostInferenceUrl,
        [string]$HostInferenceModel
    )

    $laneRelativeRoot = Join-Path (Join-Path $baselineRelativeRoot "runs") $LaneKey
    $laneRoot = Join-Path $repoRoot $laneRelativeRoot
    $logPath = Join-Path $logsDir ($LaneKey + ".log")
    New-Item -ItemType Directory -Force -Path $laneRoot | Out-Null

    $argumentList = @(
        "-NoProfile",
        "-NonInteractive",
        "-ExecutionPolicy", "Bypass",
        "-File", $validationPackScript,
        "-Devices", ($RunDevices -join ","),
        "-OutputRoot", $laneRelativeRoot,
        "-InferenceMode", $InferenceMode,
        "-HostInferenceUrl", $HostInferenceUrl,
        "-HostInferenceModel", $HostInferenceModel,
        "-Orientation", $RunOrientation,
        "-FontScale", (Format-InvariantDouble -Value $FontScale),
        "-MaxWaitSeconds", $MaxWaitSeconds.ToString(),
        "-PollSeconds", $PollSeconds.ToString(),
        "-WaitSeconds", $WaitSeconds.ToString()
    )

    if (-not [string]::IsNullOrWhiteSpace($resolvedPromptPackFile)) {
        $argumentList += @("-PromptPackFile", $resolvedPromptPackFile)
    }
    if ($InstallApk) {
        $argumentList += @("-InstallApk", "-ApkPath", $ApkPath)
    }
    if ($UseInstrumentationPreflight) {
        $argumentList += "-UseInstrumentationPreflight"
    }
    if ($UseInstrumentationExecution) {
        $argumentList += "-UseInstrumentationExecution"
    }
    if ($ForceShellExecution) {
        $argumentList += "-ForceShellExecution"
    }
    if ($IncludeFollowUpCase) {
        $argumentList += "-IncludeFollowUpCase"
    }

    Write-Host ""
    Write-Host ("[{0}] Starting validation pack run..." -f $LaneKey.ToUpperInvariant())
    Write-Host ("[{0}] Devices: {1}" -f $LaneKey.ToUpperInvariant(), ($RunDevices -join ", "))
    Write-Host ("[{0}] Orientation: {1}" -f $LaneKey.ToUpperInvariant(), $RunOrientation)
    Write-Host ("[{0}] Inference mode: {1}" -f $LaneKey.ToUpperInvariant(), $InferenceMode)
    Write-Host ("[{0}] Host URL: {1}" -f $LaneKey.ToUpperInvariant(), $HostInferenceUrl)
    Write-Host ("[{0}] Host model: {1}" -f $LaneKey.ToUpperInvariant(), $HostInferenceModel)

    $stdoutTempPath = Join-Path $logsDir ($LaneKey + ".stdout.tmp")
    $stderrTempPath = Join-Path $logsDir ($LaneKey + ".stderr.tmp")
    $process = Start-Process -FilePath "powershell.exe" `
        -ArgumentList (Convert-ToProcessArgumentString -Arguments $argumentList) `
        -WorkingDirectory $repoRoot `
        -NoNewWindow `
        -PassThru `
        -Wait `
        -RedirectStandardOutput $stdoutTempPath `
        -RedirectStandardError $stderrTempPath
    $exitCode = if ($null -eq $process.ExitCode) { 0 } else { [int]$process.ExitCode }

    $logChunks = New-Object System.Collections.Generic.List[string]
    foreach ($tempPath in @($stdoutTempPath, $stderrTempPath)) {
        if (-not (Test-Path -LiteralPath $tempPath)) {
            continue
        }

        $content = Get-Content -LiteralPath $tempPath -Raw
        if (-not [string]::IsNullOrWhiteSpace($content)) {
            $trimmedContent = $content.TrimEnd()
            $logChunks.Add($trimmedContent)
            Write-Host $trimmedContent
        }

        Remove-Item -LiteralPath $tempPath -Force -ErrorAction SilentlyContinue
    }

    $combinedLog = if ($logChunks.Count -gt 0) {
        $logChunks -join [Environment]::NewLine
    } else {
        ""
    }
    $combinedLog | Set-Content -LiteralPath $logPath -Encoding UTF8

    $runDirectory = Get-ChildItem -LiteralPath $laneRoot -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -First 1

    $summaryPath = $null
    $summary = $null
    $results = @()
    if ($null -ne $runDirectory) {
        $candidateSummaryPath = Join-Path $runDirectory.FullName "summary.json"
        if (Test-Path -LiteralPath $candidateSummaryPath) {
            $summaryPath = $candidateSummaryPath
            $summary = Get-Content -LiteralPath $summaryPath -Raw | ConvertFrom-Json

            $resultsJsonPath = [string](Get-PropertyValue -InputObject $summary -PropertyName "results_json")
            if (-not [string]::IsNullOrWhiteSpace($resultsJsonPath) -and (Test-Path -LiteralPath $resultsJsonPath)) {
                $results = Convert-ToObjectArray -InputObject (Get-Content -LiteralPath $resultsJsonPath -Raw | ConvertFrom-Json)
            }
        }
    }

    return [pscustomobject]@{
        lane = $LaneKey
        exit_code = $exitCode
        success = ($exitCode -eq 0)
        output_root = $laneRoot
        output_root_relative = Convert-ToRepoRelativePath -Path $laneRoot
        output_dir = if ($null -ne $runDirectory) { $runDirectory.FullName } else { $null }
        output_dir_relative = if ($null -ne $runDirectory) { Convert-ToRepoRelativePath -Path $runDirectory.FullName } else { $null }
        log_path = $logPath
        log_path_relative = Convert-ToRepoRelativePath -Path $logPath
        summary_json = $summaryPath
        summary_json_relative = Convert-ToRepoRelativePath -Path $summaryPath
        results_json = if ($null -ne $summary) { [string](Get-PropertyValue -InputObject $summary -PropertyName "results_json") } else { $null }
        results_json_relative = if ($null -ne $summary) { Convert-ToRepoRelativePath -Path ([string](Get-PropertyValue -InputObject $summary -PropertyName "results_json")) } else { $null }
        results_csv = if ($null -ne $summary) { [string](Get-PropertyValue -InputObject $summary -PropertyName "results_csv") } else { $null }
        results_csv_relative = if ($null -ne $summary) { Convert-ToRepoRelativePath -Path ([string](Get-PropertyValue -InputObject $summary -PropertyName "results_csv")) } else { $null }
        artifact_bundle_zip = if ($null -ne $summary) { [string](Get-PropertyValue -InputObject $summary -PropertyName "artifact_bundle_zip") } else { $null }
        artifact_bundle_zip_relative = if ($null -ne $summary) { Convert-ToRepoRelativePath -Path ([string](Get-PropertyValue -InputObject $summary -PropertyName "artifact_bundle_zip")) } else { $null }
        summary_status = if ($null -ne $summary) { [string](Get-PropertyValue -InputObject $summary -PropertyName "status") } else { $null }
        pass_count = if ($null -ne $summary) { Get-PropertyValue -InputObject $summary -PropertyName "pass_count" } else { $null }
        fail_count = if ($null -ne $summary) { Get-PropertyValue -InputObject $summary -PropertyName "fail_count" } else { $null }
        duration_ms = if ($null -ne $summary) { Get-PropertyValue -InputObject $summary -PropertyName "duration_ms" } else { $null }
        device_count = if ($null -ne $summary) { Get-PropertyValue -InputObject $summary -PropertyName "device_count" } else { $null }
        case_count = if ($null -ne $summary) { Get-PropertyValue -InputObject $summary -PropertyName "case_count" } else { $null }
        devices = @($RunDevices)
        orientation = $RunOrientation
        inference_mode = $InferenceMode
        host_inference_url = $HostInferenceUrl
        host_inference_model = $HostInferenceModel
        summary = $summary
        results = @($results)
        invocation = [pscustomobject]@{
            script = $validationPackScript
            arguments = @($argumentList)
        }
    }
}

function New-ModelRun {
    param(
        [string]$LaneKey,
        [string]$InferenceMode,
        [string]$HostInferenceUrl,
        [string]$HostInferenceModel,
        [object]$Segments
    )

    $segmentRuns = @(Convert-ToObjectArray -InputObject $Segments | Where-Object { $null -ne $_ })
    $allRows = New-Object System.Collections.Generic.List[object]
    foreach ($segment in $segmentRuns) {
        foreach ($row in (Convert-ToObjectArray -InputObject $segment.results)) {
            $allRows.Add($row)
        }
    }

    $successCount = @($segmentRuns | Where-Object { $_.success }).Count
    $failureCount = @($segmentRuns | Where-Object { -not $_.success }).Count
    $summaryStatuses = @($segmentRuns | ForEach-Object { $_.summary_status } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    $passCount = 0
    $failCount = 0
    foreach ($segment in $segmentRuns) {
        if ($null -ne $segment.pass_count) {
            $passCount += [int]$segment.pass_count
        }
        if ($null -ne $segment.fail_count) {
            $failCount += [int]$segment.fail_count
        }
    }
    $runSuccess = ($segmentRuns.Count -gt 0 -and $failureCount -eq 0)
    $runStatus = if ($segmentRuns.Count -eq 0) {
        "skipped"
    } elseif ($failureCount -eq 0) {
        "pass"
    } elseif ($successCount -gt 0) {
        "partial"
    } else {
        "fail"
    }

    return (New-Object PSObject -Property ([ordered]@{
        lane = $LaneKey
        success = $runSuccess
        status = $runStatus
        inference_mode = $InferenceMode
        host_inference_url = $HostInferenceUrl
        host_inference_model = $HostInferenceModel
        segments = @($segmentRuns)
        segment_count = $segmentRuns.Count
        segment_success_count = $successCount
        segment_failure_count = $failureCount
        summary_statuses = @($summaryStatuses)
        pass_count = $passCount
        fail_count = $failCount
        results = $allRows.ToArray()
    }))
}

function Compare-RunResults {
    param(
        [object]$E2BRun,
        [object]$E4BRun
    )

    $e2bRows = Convert-ToObjectArray -InputObject $E2BRun.results
    $e4bRows = Convert-ToObjectArray -InputObject $E4BRun.results

    $e2bMap = @{}
    foreach ($row in $e2bRows) {
        $e2bMap[(Get-ResultRowKey -Row $row)] = $row
    }

    $e4bMap = @{}
    foreach ($row in $e4bRows) {
        $e4bMap[(Get-ResultRowKey -Row $row)] = $row
    }

    $allKeys = @((@($e2bMap.Keys) + @($e4bMap.Keys)) | Sort-Object -Unique)
    $diffRows = New-Object System.Collections.Generic.List[object]

    $sharedRowCount = 0
    $identicalRowCount = 0
    $e2bOnlyRowCount = 0
    $e4bOnlyRowCount = 0
    $statusMismatchCount = 0
    $detectedModeMismatchCount = 0
    $instrumentationMismatchCount = 0
    $titleMismatchCount = 0
    $metaMismatchCount = 0
    $bodyPreviewMismatchCount = 0
    $runErrorMismatchCount = 0

    foreach ($key in $allKeys) {
        $e2bRow = if ($e2bMap.ContainsKey($key)) { $e2bMap[$key] } else { $null }
        $e4bRow = if ($e4bMap.ContainsKey($key)) { $e4bMap[$key] } else { $null }

        if ($null -eq $e2bRow) {
            $e4bOnlyRowCount += 1
        } elseif ($null -eq $e4bRow) {
            $e2bOnlyRowCount += 1
        } else {
            $sharedRowCount += 1
        }

        $statusMatch = (Normalize-ComparableText (Get-PropertyValue -InputObject $e2bRow -PropertyName "status")) -eq (Normalize-ComparableText (Get-PropertyValue -InputObject $e4bRow -PropertyName "status"))
        $detectedModeMatch = (Normalize-ComparableText (Get-PropertyValue -InputObject $e2bRow -PropertyName "detected_mode")) -eq (Normalize-ComparableText (Get-PropertyValue -InputObject $e4bRow -PropertyName "detected_mode"))
        $instrumentationMatch = (Normalize-ComparableText (Get-PropertyValue -InputObject $e2bRow -PropertyName "instrumentation_status")) -eq (Normalize-ComparableText (Get-PropertyValue -InputObject $e4bRow -PropertyName "instrumentation_status"))
        $titleMatch = (Normalize-ComparableText (Get-PropertyValue -InputObject $e2bRow -PropertyName "screen_title")) -eq (Normalize-ComparableText (Get-PropertyValue -InputObject $e4bRow -PropertyName "screen_title"))
        $metaMatch = (Normalize-ComparableText (Get-PropertyValue -InputObject $e2bRow -PropertyName "screen_meta")) -eq (Normalize-ComparableText (Get-PropertyValue -InputObject $e4bRow -PropertyName "screen_meta"))
        $bodyPreviewMatch = (Normalize-ComparableText (Get-PropertyValue -InputObject $e2bRow -PropertyName "body_preview")) -eq (Normalize-ComparableText (Get-PropertyValue -InputObject $e4bRow -PropertyName "body_preview"))
        $runErrorMatch = (Normalize-ComparableText (Get-PropertyValue -InputObject $e2bRow -PropertyName "run_error")) -eq (Normalize-ComparableText (Get-PropertyValue -InputObject $e4bRow -PropertyName "run_error"))
        $rowSignatureMatch = (Get-ComparableRowSignature -Row $e2bRow) -eq (Get-ComparableRowSignature -Row $e4bRow)

        if ($null -ne $e2bRow -and $null -ne $e4bRow -and $rowSignatureMatch) {
            $identicalRowCount += 1
            continue
        }

        if ($null -ne $e2bRow -and $null -ne $e4bRow) {
            if (-not $statusMatch) {
                $statusMismatchCount += 1
            }
            if (-not $detectedModeMatch) {
                $detectedModeMismatchCount += 1
            }
            if (-not $instrumentationMatch) {
                $instrumentationMismatchCount += 1
            }
            if (-not $titleMatch) {
                $titleMismatchCount += 1
            }
            if (-not $metaMatch) {
                $metaMismatchCount += 1
            }
            if (-not $bodyPreviewMatch) {
                $bodyPreviewMismatchCount += 1
            }
            if (-not $runErrorMatch) {
                $runErrorMismatchCount += 1
            }
        }

        $diffRows.Add([pscustomobject]@{
            key = $key
            device = if ($null -ne $e2bRow) { [string](Get-PropertyValue -InputObject $e2bRow -PropertyName "device") } else { [string](Get-PropertyValue -InputObject $e4bRow -PropertyName "device") }
            case_id = if ($null -ne $e2bRow) { [string](Get-PropertyValue -InputObject $e2bRow -PropertyName "case_id") } else { [string](Get-PropertyValue -InputObject $e4bRow -PropertyName "case_id") }
            row_presence = if ($null -eq $e2bRow) { "e4b_only" } elseif ($null -eq $e4bRow) { "e2b_only" } else { "both" }
            status_match = $statusMatch
            detected_mode_match = $detectedModeMatch
            instrumentation_status_match = $instrumentationMatch
            title_match = $titleMatch
            meta_match = $metaMatch
            body_preview_match = $bodyPreviewMatch
            run_error_match = $runErrorMatch
            e2b = New-RowSnapshot -Row $e2bRow
            e4b = New-RowSnapshot -Row $e4bRow
        })
    }

    return (New-Object PSObject -Property ([ordered]@{
        total_row_keys = $allKeys.Count
        shared_row_count = $sharedRowCount
        identical_row_count = $identicalRowCount
        differing_row_count = $diffRows.Count
        e2b_only_row_count = $e2bOnlyRowCount
        e4b_only_row_count = $e4bOnlyRowCount
        status_mismatch_count = $statusMismatchCount
        detected_mode_mismatch_count = $detectedModeMismatchCount
        instrumentation_status_mismatch_count = $instrumentationMismatchCount
        title_mismatch_count = $titleMismatchCount
        meta_mismatch_count = $metaMismatchCount
        body_preview_mismatch_count = $bodyPreviewMismatchCount
        run_error_mismatch_count = $runErrorMismatchCount
        differing_rows = $diffRows.ToArray()
    }))
}

function Get-DiffNote {
    param([object]$DiffRow)

    if ($null -eq $DiffRow) {
        return ""
    }

    if ([string]$DiffRow.row_presence -eq "e2b_only") {
        return "missing in e4b"
    }
    if ([string]$DiffRow.row_presence -eq "e4b_only") {
        return "missing in e2b"
    }

    $parts = New-Object System.Collections.Generic.List[string]
    if (-not [bool]$DiffRow.status_match) {
        $parts.Add("status")
    }
    if (-not [bool]$DiffRow.detected_mode_match) {
        $parts.Add("mode")
    }
    if (-not [bool]$DiffRow.instrumentation_status_match) {
        $parts.Add("instrumentation")
    }
    if (-not [bool]$DiffRow.title_match) {
        $parts.Add("title")
    }
    if (-not [bool]$DiffRow.meta_match) {
        $parts.Add("meta")
    }
    if (-not [bool]$DiffRow.body_preview_match) {
        $parts.Add("body")
    }
    if (-not [bool]$DiffRow.run_error_match) {
        $parts.Add("error")
    }

    return ($parts -join ", ")
}

function Render-DiffMarkdown {
    param(
        [object]$Manifest,
        [object]$Diff
    )

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("# E2B vs E4B Validation Diff")
    $lines.Add("")
    $lines.Add(('- Generated at UTC: `{0}`' -f $Manifest.generated_at_utc))
    $lines.Add(('- Baseline root: `{0}`' -f $Manifest.baseline_root_relative))
    if (-not [string]::IsNullOrWhiteSpace([string]$Manifest.config.prompt_pack_file_relative)) {
        $lines.Add(('- Prompt pack: `{0}`' -f $Manifest.config.prompt_pack_file_relative))
    } else {
        $lines.Add("- Prompt pack: built-in validation cases")
    }
    $portraitDevicesText = if (@($Manifest.config.portrait_devices).Count -gt 0) {
        (($Manifest.config.portrait_devices | ForEach-Object { $_ }) -join '`, `')
    } else {
        '<none>'
    }
    $landscapeDevicesText = if (@($Manifest.config.landscape_devices).Count -gt 0) {
        (($Manifest.config.landscape_devices | ForEach-Object { $_ }) -join '`, `')
    } else {
        '<none>'
    }
    $lines.Add(('- Portrait devices: `{0}`' -f $portraitDevicesText))
    $lines.Add(('- Landscape devices: `{0}`' -f $landscapeDevicesText))
    $lines.Add("")
    $lines.Add("## Runs")
    $lines.Add("")
    $lines.Add("| lane | segments | status | pass/fail | model | host |")
    $lines.Add("| --- | --- | --- | --- | --- | --- |")

    foreach ($laneName in @("e2b", "e4b")) {
        $lane = $Manifest.runs.$laneName
        $passFail = if ($null -ne $lane.pass_count -or $null -ne $lane.fail_count) {
            ("{0}/{1}" -f $lane.pass_count, $lane.fail_count)
        } else {
            "-"
        }
        $lines.Add((
            '| {0} | {1} | {2} | {3} | `{4}` | `{5}` |' -f
            $laneName,
            $lane.segment_count,
            $lane.status,
            $passFail,
            $lane.host_inference_model,
            $lane.host_inference_url
        ))
    }

    $lines.Add("")
    $lines.Add("## Segment Artifacts")
    $lines.Add("")
    $lines.Add("| segment | orientation | devices | status | output | summary | log |")
    $lines.Add("| --- | --- | --- | --- | --- | --- | --- |")
    foreach ($laneName in @("e2b", "e4b")) {
        foreach ($segment in @($Manifest.runs.$laneName.segments)) {
            $lines.Add((
                '| {0} | {1} | {2} | {3} | `{4}` | `{5}` | `{6}` |' -f
                $segment.lane,
                $segment.orientation,
                (($segment.devices | ForEach-Object { $_ }) -join ', '),
                $(if ([string]::IsNullOrWhiteSpace($segment.summary_status)) { $(if ($segment.success) { 'pass' } else { 'fail' }) } else { $segment.summary_status }),
                $(if ([string]::IsNullOrWhiteSpace($segment.output_dir_relative)) { "-" } else { $segment.output_dir_relative }),
                $(if ([string]::IsNullOrWhiteSpace($segment.summary_json_relative)) { "-" } else { $segment.summary_json_relative }),
                $(if ([string]::IsNullOrWhiteSpace($segment.log_path_relative)) { "-" } else { $segment.log_path_relative })
            ))
        }
    }

    $lines.Add("")
    $lines.Add("## Diff Summary")
    $lines.Add("")
    $lines.Add(('- Shared rows: `{0}`' -f $Diff.shared_row_count))
    $lines.Add(('- Identical rows: `{0}`' -f $Diff.identical_row_count))
    $lines.Add(('- Differing rows: `{0}`' -f $Diff.differing_row_count))
    $lines.Add(('- E2B-only rows: `{0}`' -f $Diff.e2b_only_row_count))
    $lines.Add(('- E4B-only rows: `{0}`' -f $Diff.e4b_only_row_count))
    $lines.Add(('- Status mismatches: `{0}`' -f $Diff.status_mismatch_count))
    $lines.Add(('- Mode mismatches: `{0}`' -f $Diff.detected_mode_mismatch_count))
    $lines.Add(('- Instrumentation mismatches: `{0}`' -f $Diff.instrumentation_status_mismatch_count))
    $lines.Add(('- Body preview mismatches: `{0}`' -f $Diff.body_preview_mismatch_count))

    if ($Diff.differing_row_count -gt 0) {
        $lines.Add("")
        $lines.Add("## Row Deltas")
        $lines.Add("")
        $lines.Add("| device | case_id | delta | e2b artifact | e4b artifact |")
        $lines.Add("| --- | --- | --- | --- | --- |")
        foreach ($row in @($Diff.differing_rows | Sort-Object device, case_id)) {
            $lines.Add((
                '| {0} | {1} | {2} | `{3}` | `{4}` |' -f
                $row.device,
                $row.case_id,
                (Get-DiffNote -DiffRow $row),
                $(if ($null -eq $row.e2b) { "-" } else { $row.e2b.artifact_dir_relative }),
                $(if ($null -eq $row.e4b) { "-" } else { $row.e4b.artifact_dir_relative })
            ))
        }
    }

    return ($lines -join [Environment]::NewLine)
}

$portraitDevices = Normalize-DeviceList -InputDevices $Devices
$landscapeDevices = Normalize-DeviceList -InputDevices $LandscapeDevices
if ($portraitDevices.Count -eq 0 -and $landscapeDevices.Count -eq 0) {
    throw "Provide at least one portrait or landscape device."
}

$runStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$runStartedUtc = (Get-Date).ToUniversalTime()

$e2bSegments = New-Object System.Collections.Generic.List[object]
if ($portraitDevices.Count -gt 0) {
    $e2bSegments.Add((Invoke-ValidationPackRun -LaneKey "e2b_portrait" -RunDevices $portraitDevices -RunOrientation $Orientation -InferenceMode $E2BInferenceMode -HostInferenceUrl $E2BHostInferenceUrl -HostInferenceModel $E2BHostInferenceModel))
}
if ($landscapeDevices.Count -gt 0) {
    $e2bSegments.Add((Invoke-ValidationPackRun -LaneKey "e2b_landscape" -RunDevices $landscapeDevices -RunOrientation $LandscapeOrientation -InferenceMode $E2BInferenceMode -HostInferenceUrl $E2BHostInferenceUrl -HostInferenceModel $E2BHostInferenceModel))
}
$e2bRun = New-ModelRun -LaneKey "e2b" -InferenceMode $E2BInferenceMode -HostInferenceUrl $E2BHostInferenceUrl -HostInferenceModel $E2BHostInferenceModel -Segments ($e2bSegments.ToArray())

$e4bSegments = New-Object System.Collections.Generic.List[object]
if ($portraitDevices.Count -gt 0) {
    $e4bSegments.Add((Invoke-ValidationPackRun -LaneKey "e4b_portrait" -RunDevices $portraitDevices -RunOrientation $Orientation -InferenceMode $E4BInferenceMode -HostInferenceUrl $E4BHostInferenceUrl -HostInferenceModel $E4BHostInferenceModel))
}
if ($landscapeDevices.Count -gt 0) {
    $e4bSegments.Add((Invoke-ValidationPackRun -LaneKey "e4b_landscape" -RunDevices $landscapeDevices -RunOrientation $LandscapeOrientation -InferenceMode $E4BInferenceMode -HostInferenceUrl $E4BHostInferenceUrl -HostInferenceModel $E4BHostInferenceModel))
}
$e4bRun = New-ModelRun -LaneKey "e4b" -InferenceMode $E4BInferenceMode -HostInferenceUrl $E4BHostInferenceUrl -HostInferenceModel $E4BHostInferenceModel -Segments ($e4bSegments.ToArray())

$diff = Compare-RunResults -E2BRun $e2bRun -E4BRun $e4bRun

$runStopwatch.Stop()
$runCompletedUtc = (Get-Date).ToUniversalTime()
$overallStatus = if ($e2bRun.success -and $e4bRun.success) { "pass" } elseif ($e2bRun.success -or $e4bRun.success) { "partial" } else { "fail" }

$manifestConfig = New-Object PSObject -Property ([ordered]@{
    portrait_devices = @($portraitDevices)
    landscape_devices = @($landscapeDevices)
    prompt_pack_file = if ([string]::IsNullOrWhiteSpace($resolvedPromptPackFile)) { $null } else { $resolvedPromptPackFile }
    prompt_pack_file_relative = if ([string]::IsNullOrWhiteSpace($resolvedPromptPackFile)) { $null } else { Convert-ToRepoRelativePath -Path $resolvedPromptPackFile }
    install_apk = [bool]$InstallApk
    apk_path = $ApkPath
    orientation = $Orientation
    landscape_orientation = $LandscapeOrientation
    font_scale = $FontScale
    use_instrumentation_preflight = [bool]$UseInstrumentationPreflight
    use_instrumentation_execution = [bool]$UseInstrumentationExecution
    force_shell_execution = [bool]$ForceShellExecution
    include_followup_case = [bool]$IncludeFollowUpCase
    max_wait_seconds = $MaxWaitSeconds
    poll_seconds = $PollSeconds
    wait_seconds = $WaitSeconds
})
$manifestRuns = New-Object PSObject -Property ([ordered]@{
    e2b = $e2bRun
    e4b = $e4bRun
})
$manifest = New-Object PSObject -Property ([ordered]@{
    generated_at_utc = $runCompletedUtc.ToString("o")
    run_started_utc = $runStartedUtc.ToString("o")
    run_completed_utc = $runCompletedUtc.ToString("o")
    duration_ms = [int]$runStopwatch.ElapsedMilliseconds
    status = $overallStatus
    baseline_root = $baselineRoot
    baseline_root_relative = Convert-ToRepoRelativePath -Path $baselineRoot
    config = $manifestConfig
    runs = $manifestRuns
    diff = $diff
})

$manifestPath = Join-Path $baselineRoot "manifest.json"
$diffJsonPath = Join-Path $baselineRoot "diff_summary.json"
$diffMarkdownPath = Join-Path $baselineRoot "diff_summary.md"

($diff | ConvertTo-Json -Depth 12) | Set-Content -LiteralPath $diffJsonPath -Encoding UTF8
(Render-DiffMarkdown -Manifest $manifest -Diff $diff) | Set-Content -LiteralPath $diffMarkdownPath -Encoding UTF8

$manifest | Add-Member -NotePropertyName manifest_path -NotePropertyValue $manifestPath
$manifest | Add-Member -NotePropertyName manifest_path_relative -NotePropertyValue (Convert-ToRepoRelativePath -Path $manifestPath)
$manifest | Add-Member -NotePropertyName diff_summary_json -NotePropertyValue $diffJsonPath
$manifest | Add-Member -NotePropertyName diff_summary_json_relative -NotePropertyValue (Convert-ToRepoRelativePath -Path $diffJsonPath)
$manifest | Add-Member -NotePropertyName diff_summary_markdown -NotePropertyValue $diffMarkdownPath
$manifest | Add-Member -NotePropertyName diff_summary_markdown_relative -NotePropertyValue (Convert-ToRepoRelativePath -Path $diffMarkdownPath)
($manifest | ConvertTo-Json -Depth 14) | Set-Content -LiteralPath $manifestPath -Encoding UTF8

Write-Host ""
Write-Host "E2B vs E4B validation diff lane complete."
Write-Host "Baseline root: $baselineRoot"
Write-Host "Manifest: $manifestPath"
Write-Host "Diff JSON: $diffJsonPath"
Write-Host "Diff Markdown: $diffMarkdownPath"
Write-Host ("E2B segments: {0}" -f $e2bRun.segment_count)
Write-Host ("E4B segments: {0}" -f $e4bRun.segment_count)
Write-Host ("Differing rows: {0}" -f $diff.differing_row_count)

if (-not $e2bRun.success -or -not $e4bRun.success) {
    exit 1
}

exit 0
