param(
    [string]$OutputRoot = "artifacts/ui_state_pack",
    [switch]$SkipBuild,
    [switch]$SkipInstall,
    [switch]$SkipHostStates,
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string[]]$RoleFilter = @(),
    [string]$RunId = "",
    [switch]$SkipFinalize,
    [switch]$FinalizeOnly
)

$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$androidRoot = Join-Path $repoRoot "android-app"
$gradlew = Join-Path $androidRoot "gradlew.bat"
$smokeScript = Join-Path $PSScriptRoot "run_android_instrumented_ui_smoke.ps1"
$detailFollowupScript = Join-Path $PSScriptRoot "run_android_detail_followup.ps1"
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"

if (-not (Test-Path -LiteralPath $gradlew)) {
    throw "gradlew.bat not found at $gradlew"
}
if (-not (Test-Path -LiteralPath $smokeScript)) {
    throw "run_android_instrumented_ui_smoke.ps1 not found at $smokeScript"
}
if (-not (Test-Path -LiteralPath $detailFollowupScript)) {
    throw "run_android_detail_followup.ps1 not found at $detailFollowupScript"
}
if (-not (Test-Path -LiteralPath $commonHarnessModule)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}

Import-Module $commonHarnessModule -Force -DisableNameChecking

function Copy-PackArtifactSet {
    param(
        [object]$Summary,
        [string]$PackScreenshotsDir,
        [string]$PackDumpsDir
    )

    $artifactDir = [string]$Summary.artifact_dir
    $copiedScreenshots = @()
    $copiedDumps = @()

    foreach ($name in @($Summary.screenshots)) {
        if ([string]::IsNullOrWhiteSpace([string]$name)) {
            continue
        }
        $source = Join-Path (Join-Path $artifactDir "screenshots") $name
        if (Test-Path -LiteralPath $source) {
            $destination = Join-Path $PackScreenshotsDir $name
            Copy-Item -LiteralPath $source -Destination $destination -Force
            $copiedScreenshots += $destination
        }
    }

    foreach ($name in @($Summary.dumps)) {
        if ([string]::IsNullOrWhiteSpace([string]$name)) {
            continue
        }
        $source = Join-Path (Join-Path $artifactDir "dumps") $name
        if (Test-Path -LiteralPath $source) {
            $destination = Join-Path $PackDumpsDir $name
            Copy-Item -LiteralPath $source -Destination $destination -Force
            $copiedDumps += $destination
        }
    }

    return [pscustomobject]@{
        screenshots = $copiedScreenshots
        dumps = $copiedDumps
    }
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

function Get-PackArtifactTrust {
    param(
        [object]$Summary,
        [string[]]$CopiedScreenshots,
        [string[]]$CopiedDumps
    )

    $expectedScreenshots = Get-ArtifactFileNames -Artifacts $(if ($null -ne $Summary) { $Summary.screenshots } else { @() })
    $expectedDumps = Get-ArtifactFileNames -Artifacts $(if ($null -ne $Summary) { $Summary.dumps } else { @() })
    $copiedScreenshotNames = Get-ArtifactFileNames -Artifacts $CopiedScreenshots
    $copiedDumpNames = Get-ArtifactFileNames -Artifacts $CopiedDumps
    $failures = New-Object System.Collections.Generic.List[string]

    if ($expectedScreenshots.Count -eq 0) {
        $failures.Add("summary declared no screenshot artifacts to copy into the final pack")
    }
    if ($expectedDumps.Count -eq 0) {
        $failures.Add("summary declared no dump artifacts to copy into the final pack")
    }
    if ($copiedScreenshotNames.Count -eq 0) {
        $failures.Add("no copied screenshot artifacts are present in the final pack")
    }
    if ($copiedDumpNames.Count -eq 0) {
        $failures.Add("no copied dump artifacts are present in the final pack")
    }

    $missingExpectedScreenshots = @($expectedScreenshots | Where-Object { $copiedScreenshotNames -notcontains $_ })
    if ($missingExpectedScreenshots.Count -gt 0) {
        $failures.Add(("missing copied screenshot artifacts: {0}" -f ($missingExpectedScreenshots -join ", ")))
    }
    $missingExpectedDumps = @($expectedDumps | Where-Object { $copiedDumpNames -notcontains $_ })
    if ($missingExpectedDumps.Count -gt 0) {
        $failures.Add(("missing copied dump artifacts: {0}" -f ($missingExpectedDumps -join ", ")))
    }

    $rawStatus = if ($null -ne $Summary -and ($Summary.PSObject.Properties.Name -contains "status")) {
        [string]$Summary.status
    } else {
        ""
    }
    $trustedStatus = if ($rawStatus -eq "pass" -and $failures.Count -eq 0) {
        "pass"
    } elseif ([string]::IsNullOrWhiteSpace($rawStatus) -or $rawStatus -eq "pass") {
        "fail"
    } else {
        $rawStatus
    }

    return [pscustomobject]@{
        raw_status = $rawStatus
        status = $trustedStatus
        expectations_met = ($failures.Count -eq 0)
        failures = @($failures)
        expected_screenshots = @($expectedScreenshots)
        expected_dumps = @($expectedDumps)
        copied_screenshots = @($copiedScreenshotNames)
        copied_dumps = @($copiedDumpNames)
    }
}

function Write-TrustedPackSummary {
    param(
        [object]$Summary,
        [string]$SummaryPath,
        [string[]]$CopiedScreenshots,
        [string[]]$CopiedDumps
    )

    $trust = Get-PackArtifactTrust -Summary $Summary -CopiedScreenshots $CopiedScreenshots -CopiedDumps $CopiedDumps
    $summaryData = [ordered]@{}
    if ($null -ne $Summary) {
        foreach ($property in $Summary.PSObject.Properties) {
            $summaryData[$property.Name] = $property.Value
        }
    }

    $summaryData["status"] = $trust.status
    $summaryData["raw_status"] = $trust.raw_status
    $summaryData["failure_reason"] = Join-FailureReasons -ExistingMessage ([string]$summaryData["failure_reason"]) -AdditionalMessages $trust.failures
    $summaryData["screenshots"] = @($trust.copied_screenshots)
    $summaryData["dumps"] = @($trust.copied_dumps)
    $summaryData["pack_artifact_expectations_met"] = [bool]$trust.expectations_met
    $summaryData["pack_artifact_expectation_failures"] = @($trust.failures)
    $summaryData["pack_expected_screenshots"] = @($trust.expected_screenshots)
    $summaryData["pack_expected_dumps"] = @($trust.expected_dumps)
    $summaryData["pack_copied_screenshot_paths"] = @($CopiedScreenshots)
    $summaryData["pack_copied_dump_paths"] = @($CopiedDumps)
    $summaryData["pack_summary_generated_at_utc"] = (Get-Date).ToUniversalTime().ToString("o")

    $summaryObject = [pscustomobject]$summaryData
    ($summaryObject | ConvertTo-Json -Depth 8) | Set-Content -LiteralPath $SummaryPath -Encoding UTF8
    return $summaryObject
}

function Normalize-IdentityValue {
    param([object]$Value)

    $text = [string]$Value
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $null
    }
    return $text.Trim().ToLowerInvariant()
}

function Get-PackIdentityRollup {
    param([object[]]$ResultEntries)

    $deviceBuckets = [ordered]@{}
    foreach ($entry in @($ResultEntries)) {
        $device = [string]$entry.device
        if ([string]::IsNullOrWhiteSpace($device)) {
            continue
        }
        if (-not $deviceBuckets.Contains($device)) {
            $deviceBuckets[$device] = @{
                roles = New-Object System.Collections.Generic.HashSet[string]
                apk_shas = New-Object System.Collections.Generic.HashSet[string]
                model_names = New-Object System.Collections.Generic.HashSet[string]
                model_shas = New-Object System.Collections.Generic.HashSet[string]
                state_count = 0
                pass_count = 0
                fail_count = 0
            }
        }

        $bucket = $deviceBuckets[$device]
        $null = $bucket.roles.Add([string]$entry.role)
        $bucket.state_count += 1
        if ([string]$entry.status -eq "pass") {
            $bucket.pass_count += 1
        } else {
            $bucket.fail_count += 1
        }

        $summaryPath = [string]$entry.summary_path
        if ([string]::IsNullOrWhiteSpace($summaryPath) -or -not (Test-Path -LiteralPath $summaryPath)) {
            continue
        }

        try {
            $summary = Get-Content -LiteralPath $summaryPath -Raw | ConvertFrom-Json
        } catch {
            continue
        }

        $apkSha = Normalize-IdentityValue -Value $summary.apk_sha
        if (-not [string]::IsNullOrWhiteSpace($apkSha)) {
            $null = $bucket.apk_shas.Add($apkSha)
        }
        $modelName = [string]$summary.model_name
        if (-not [string]::IsNullOrWhiteSpace($modelName)) {
            $null = $bucket.model_names.Add($modelName)
        }
        $modelSha = Normalize-IdentityValue -Value $summary.model_sha
        if (-not [string]::IsNullOrWhiteSpace($modelSha)) {
            $null = $bucket.model_shas.Add($modelSha)
        }
    }

    $deviceSummaries = New-Object System.Collections.Generic.List[object]
    foreach ($device in $deviceBuckets.Keys) {
        $bucket = $deviceBuckets[$device]
        $apkValues = @($bucket.apk_shas | Sort-Object)
        $modelNameValues = @($bucket.model_names | Sort-Object)
        $modelShaValues = @($bucket.model_shas | Sort-Object)
        $identityConflict = ($apkValues.Count -gt 1) -or ($modelShaValues.Count -gt 1)
        $identityMissing = ($apkValues.Count -eq 0) -or ($modelShaValues.Count -eq 0)
        $deviceSummaries.Add([pscustomobject]@{
            device = $device
            roles = @($bucket.roles | Sort-Object)
            state_count = [int]$bucket.state_count
            pass_count = [int]$bucket.pass_count
            fail_count = [int]$bucket.fail_count
            apk_sha = $(if ($apkValues.Count -eq 1) { $apkValues[0] } else { $null })
            model_name = $(if ($modelNameValues.Count -eq 1) { $modelNameValues[0] } else { $null })
            model_sha = $(if ($modelShaValues.Count -eq 1) { $modelShaValues[0] } else { $null })
            identity_conflict = [bool]$identityConflict
            identity_missing = [bool]$identityMissing
        })
    }

    $deviceSummaryArray = if ($deviceSummaries.Count -gt 0) {
        [object[]]$deviceSummaries.ToArray()
    } else {
        @()
    }
    $apkHomogeneous = $deviceSummaryArray.Count -gt 0
    if ($apkHomogeneous) {
        $apkHomogeneous = (@($deviceSummaryArray | Where-Object {
                $_.identity_conflict -or [string]::IsNullOrWhiteSpace([string]$_.apk_sha)
            }).Count -eq 0) -and
            (@($deviceSummaryArray | Select-Object -ExpandProperty apk_sha -Unique).Count -eq 1)
    }

    $modelHomogeneous = $deviceSummaryArray.Count -gt 0
    if ($modelHomogeneous) {
        $modelHomogeneous = (@($deviceSummaryArray | Where-Object {
                $_.identity_conflict -or [string]::IsNullOrWhiteSpace([string]$_.model_sha)
            }).Count -eq 0) -and
            (@($deviceSummaryArray | Select-Object -ExpandProperty model_sha -Unique).Count -eq 1)
    }

    $homogeneous = $apkHomogeneous -and $modelHomogeneous

    $matrixApkSha = $null
    $matrixModelName = $null
    $matrixModelSha = $null
    if ($apkHomogeneous) {
        $matrixApkSha = @($deviceSummaryArray | Select-Object -ExpandProperty apk_sha -Unique)[0]
    }
    if ($modelHomogeneous) {
        $matrixModelSha = @($deviceSummaryArray | Select-Object -ExpandProperty model_sha -Unique)[0]
        $matrixModelNames = @($deviceSummaryArray | Select-Object -ExpandProperty model_name -Unique | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
        if ($matrixModelNames.Count -eq 1) {
            $matrixModelName = $matrixModelNames[0]
        }
    }

    return [pscustomobject]@{
        devices = $deviceSummaryArray
        matrix_homogeneous = [bool]$homogeneous
        matrix_apk_sha = $matrixApkSha
        matrix_model_name = $matrixModelName
        matrix_model_sha = $matrixModelSha
    }
}

function New-StateDefinition {
    param(
        [string]$Name = "",
        [string]$Method,
        [string]$Runner = "instrumented",
        [string]$InitialQuery = "",
        [string]$FollowUpQuery = "",
        [switch]$HostBacked,
        [switch]$FollowUp
    )

    return [pscustomobject]@{
        name = $Name
        method = $Method
        runner = $Runner
        initial_query = $InitialQuery
        followup_query = $FollowUpQuery
        host = [bool]$HostBacked
        followup = [bool]$FollowUp
    }
}

$deviceMatrix = @(
    [pscustomobject]@{
        role = "phone_portrait"
        device = "emulator-5556"
        orientation = "portrait"
        avd = "Senku_Large_4"
        dimensions = "1080x2400"
    },
    [pscustomobject]@{
        role = "phone_landscape"
        device = "emulator-5560"
        orientation = "landscape"
        avd = "Senku_Large_3"
        dimensions = "2400x1080"
    },
    [pscustomobject]@{
        role = "tablet_portrait"
        device = "emulator-5554"
        orientation = "portrait"
        avd = "Senku_Tablet_2"
        dimensions = "1600x2560"
    },
    [pscustomobject]@{
        role = "tablet_landscape"
        device = "emulator-5558"
        orientation = "landscape"
        avd = "Senku_Tablet"
        dimensions = "2560x1600"
    }
)

$normalizedRoleFilter = New-Object System.Collections.Generic.List[string]
foreach ($entry in $RoleFilter) {
    foreach ($split in ($entry -split ",")) {
        $trimmed = $split.Trim()
        if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
            $normalizedRoleFilter.Add($trimmed)
        }
    }
}
if ($normalizedRoleFilter.Count -gt 0) {
    $deviceMatrix = @($deviceMatrix | Where-Object { $normalizedRoleFilter -contains $_.role })
    if ($deviceMatrix.Count -eq 0) {
        throw "RoleFilter did not match any device roles."
    }
}

$commonStates = @(
    (New-StateDefinition -Method "homeEntryShowsPrimaryBrowseAndAskLanes"),
    (New-StateDefinition -Method "searchQueryShowsResultsWithoutShellPolling"),
    (New-StateDefinition -Method "searchResultsLinkedGuideHandoffOpensLinkedGuideDetail"),
    (New-StateDefinition -Method "homeGuideIntentShowsGuideConnectionContext"),
    (New-StateDefinition -Method "deterministicAskNavigatesToDetailScreen"),
    (New-StateDefinition -Method "generativeAskWithHostInferenceNavigatesToDetailScreen" -HostBacked),
    (New-StateDefinition -Name "autoFollowUpWithHostInferenceBuildsInlineThreadHistory" -Method "scriptedSeededFollowUpThreadShowsInlineHistory"),
    (New-StateDefinition -Method "guideDetailShowsRelatedGuideNavigation"),
    (New-StateDefinition -Method "answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane"),
    (New-StateDefinition -Method "answerModeProvenanceOpenRemainsNeutral")
)

$specialStatesByRole = @{
    phone_portrait = @(
        (New-StateDefinition -Method "guideDetailUsesCrossReferenceCopyOffRail")
    )
    phone_landscape = @(
        (New-StateDefinition -Method "guideDetailUsesCrossReferenceCopyOffRail"),
        (New-StateDefinition -Method "answerModeLandscapePhoneShowsCompactCrossReferenceCue")
    )
    tablet_portrait = @(
        (New-StateDefinition -Method "guideDetailUsesCrossReferenceCopyOffRail")
    )
    tablet_landscape = @(
        (New-StateDefinition -Method "guideDetailDestinationKeepsSourceContextOnTabletLandscape")
    )
}

$timestamp = if ([string]::IsNullOrWhiteSpace($RunId)) { Get-Date -Format "yyyyMMdd_HHmmss" } else { $RunId }
$outputRootRelative = $OutputRoot
$runRootRelative = Join-Path $outputRootRelative $timestamp
$rawRootRelative = Join-Path $runRootRelative "raw"
$outputDir = Join-Path $repoRoot $runRootRelative
$rawDir = Join-Path $repoRoot $rawRootRelative
$screenshotsDir = Join-Path $outputDir "screenshots"
$dumpsDir = Join-Path $outputDir "dumps"
$summariesDir = Join-Path $outputDir "summaries"
$roleManifestDir = Join-Path $outputDir "role_manifests"
New-Item -ItemType Directory -Force -Path $rawDir, $screenshotsDir, $dumpsDir, $summariesDir, $roleManifestDir | Out-Null

$runStartedUtc = (Get-Date).ToUniversalTime()

if (-not $FinalizeOnly -and -not $SkipBuild) {
    Push-Location $androidRoot
    try {
        & $gradlew :app:assembleDebug :app:assembleDebugAndroidTest
        if ($LASTEXITCODE -ne 0) {
            throw "Gradle build failed"
        }
    } finally {
        Pop-Location
    }
}

$results = New-Object System.Collections.Generic.List[object]
$installedByDevice = @{}

if ($FinalizeOnly) {
    foreach ($manifestFile in Get-ChildItem -LiteralPath $roleManifestDir -Filter "*.json" -File | Sort-Object Name) {
        $roleManifest = Get-Content -LiteralPath $manifestFile.FullName -Raw | ConvertFrom-Json
        foreach ($entry in @($roleManifest.results)) {
            $results.Add($entry)
        }
    }
    if ($results.Count -eq 0) {
        throw "No role manifests found to finalize in $roleManifestDir"
    }
} else {
    foreach ($deviceEntry in $deviceMatrix) {
        $role = $deviceEntry.role
        $device = $deviceEntry.device
        $orientation = $deviceEntry.orientation
        $deviceScreenshotsDir = Join-Path $screenshotsDir $role
        $deviceDumpsDir = Join-Path $dumpsDir $role
        $deviceSummariesDir = Join-Path $summariesDir $role
        New-Item -ItemType Directory -Force -Path $deviceScreenshotsDir, $deviceDumpsDir, $deviceSummariesDir | Out-Null

        $states = @($commonStates)
        if ($specialStatesByRole.ContainsKey($role)) {
            $states += $specialStatesByRole[$role]
        }

        foreach ($state in $states) {
            if ($SkipHostStates -and $state.host) {
                continue
            }

            $stateName = if ([string]::IsNullOrWhiteSpace([string]$state.name)) {
                [string]$state.method
            } else {
                [string]$state.name
            }
            $stateSummaryPath = Join-Path (Join-Path $deviceSummariesDir $stateName) "summary.json"
            $stateSummaryDir = Split-Path -Parent $stateSummaryPath
            New-Item -ItemType Directory -Force -Path $stateSummaryDir | Out-Null

            if ([string]$state.runner -eq "detail_followup") {
                $stateRawDir = Join-Path (Join-Path $rawDir $role) $stateName
                New-Item -ItemType Directory -Force -Path $stateRawDir | Out-Null

                $followupArgs = @(
                    "-ExecutionPolicy", "Bypass",
                    "-File", $detailFollowupScript,
                    "-Emulator", $device,
                    "-InitialQuery", [string]$state.initial_query,
                    "-FollowUpQuery", [string]$state.followup_query,
                    "-OutputDir", $stateRawDir,
                    "-RunLabel", $stateName,
                    "-InferenceMode", $(if ($state.host) { "host" } else { "preserve" }),
                    "-HostInferenceUrl", $HostInferenceUrl,
                    "-HostInferenceModel", $HostInferenceModel,
                    "-InitialMaxWaitSeconds", "180",
                    "-FollowUpMaxWaitSeconds", "120"
                )

                Write-Host ("[{0}] {1}" -f $role, $stateName)
                & powershell -NoProfile -NonInteractive @followupArgs
                if ($LASTEXITCODE -ne 0) {
                    $results.Add([pscustomobject]@{
                        role = $role
                        device = $device
                        orientation = $orientation
                        state_method = $stateName
                        test_method = [string]$state.method
                        status = "fail"
                        summary_path = $stateSummaryPath
                        screenshots = @()
                        dumps = @()
                    })
                    continue
                }

                $followupManifestPath = Join-Path $stateRawDir ($stateName + ".json")
                if (-not (Test-Path -LiteralPath $followupManifestPath)) {
                    throw "Detail follow-up state did not produce a manifest: $followupManifestPath"
                }
                $followupSummary = Get-Content -LiteralPath $followupManifestPath -Raw | ConvertFrom-Json
                $copiedScreenshots = @()
                $copiedDumps = @()

                $followupScreenshotSource = [string]$followupSummary.followup_answer_screenshot
                if (-not [string]::IsNullOrWhiteSpace($followupScreenshotSource) -and (Test-Path -LiteralPath $followupScreenshotSource)) {
                    $followupScreenshotName = $stateName + ".png"
                    Copy-Item -LiteralPath $followupScreenshotSource -Destination (Join-Path $deviceScreenshotsDir $followupScreenshotName) -Force
                    $copiedScreenshots += (Join-Path $deviceScreenshotsDir $followupScreenshotName)
                }

                $followupDumpSource = [string]$followupSummary.followup_answer_dump
                if (-not [string]::IsNullOrWhiteSpace($followupDumpSource) -and (Test-Path -LiteralPath $followupDumpSource)) {
                    $followupDumpName = $stateName + ".xml"
                    Copy-Item -LiteralPath $followupDumpSource -Destination (Join-Path $deviceDumpsDir $followupDumpName) -Force
                    $copiedDumps += (Join-Path $deviceDumpsDir $followupDumpName)
                }

                $followupStateSummary = [pscustomobject]@{
                    status = $(if ($copiedScreenshots.Count -gt 0 -and $copiedDumps.Count -gt 0) { "pass" } else { "fail" })
                    runner = "detail_followup"
                    artifact_dir = $stateRawDir
                    screenshots = @($copiedScreenshots | ForEach-Object { [System.IO.Path]::GetFileName($_) })
                    dumps = @($copiedDumps | ForEach-Object { [System.IO.Path]::GetFileName($_) })
                    manifest_path = $followupManifestPath
                }
                $trustedFollowupSummary = Write-TrustedPackSummary `
                    -Summary $followupStateSummary `
                    -SummaryPath $stateSummaryPath `
                    -CopiedScreenshots $copiedScreenshots `
                    -CopiedDumps $copiedDumps
                $results.Add([pscustomobject]@{
                    role = $role
                    device = $device
                    orientation = $orientation
                    state_method = $stateName
                    test_method = [string]$state.method
                    status = [string]$trustedFollowupSummary.status
                    summary_path = $stateSummaryPath
                    screenshots = $copiedScreenshots
                    dumps = $copiedDumps
                })
                continue
            }

            $args = @(
                "-ExecutionPolicy", "Bypass",
                "-File", $smokeScript,
                "-Device", $device,
                "-SmokeProfile", "custom",
                "-TestClass", ("com.senku.mobile.PromptHarnessSmokeTest#{0}" -f [string]$state.method),
                "-ArtifactRoot", $rawRootRelative,
                "-SummaryPath", $stateSummaryPath,
                "-Orientation", $orientation,
                "-SkipBuild",
                "-CaptureLogcat"
            )

            if ($SkipInstall -or $installedByDevice[$device]) {
                $args += "-SkipInstall"
            }

            if ($state.host) {
                $args += @(
                    "-EnableHostInferenceSmoke",
                    "-HostInferenceUrl", $HostInferenceUrl,
                    "-HostInferenceModel", $HostInferenceModel
                )
            }
            if ($state.followup) {
                $args += "-EnableFollowUpSmoke"
            }

            Write-Host ("[{0}] {1}" -f $role, $stateName)
            & powershell -NoProfile -NonInteractive @args
            if ($LASTEXITCODE -ne 0) {
                $results.Add([pscustomobject]@{
                    role = $role
                    device = $device
                    orientation = $orientation
                    state_method = $stateName
                    test_method = [string]$state.method
                    status = "fail"
                    summary_path = $stateSummaryPath
                    screenshots = @()
                    dumps = @()
                })
                continue
            }

            $installedByDevice[$device] = $true
            $summary = Get-Content -LiteralPath $stateSummaryPath -Raw | ConvertFrom-Json
            $copied = Copy-PackArtifactSet -Summary $summary -PackScreenshotsDir $deviceScreenshotsDir -PackDumpsDir $deviceDumpsDir
            $trustedSummary = Write-TrustedPackSummary `
                -Summary $summary `
                -SummaryPath $stateSummaryPath `
                -CopiedScreenshots $copied.screenshots `
                -CopiedDumps $copied.dumps
            $results.Add([pscustomobject]@{
                role = $role
                device = $device
                orientation = $orientation
                state_method = $stateName
                test_method = [string]$state.method
                status = [string]$trustedSummary.status
                summary_path = $stateSummaryPath
                screenshots = $copied.screenshots
                dumps = $copied.dumps
            })
        }

        $roleManifestPath = Join-Path $roleManifestDir ($role + ".json")
        ([pscustomobject]@{
            role = $role
            device = $device
            orientation = $orientation
            results = @($results | Where-Object { $_.role -eq $role })
        } | ConvertTo-Json -Depth 8) | Set-Content -LiteralPath $roleManifestPath -Encoding UTF8
    }
}

$runCompletedUtc = (Get-Date).ToUniversalTime()
$readmePath = Join-Path $outputDir "README.md"
$manifestPath = Join-Path $outputDir "manifest.json"
$summaryPath = Join-Path $outputDir "summary.json"

$successCount = @($results | Where-Object { $_.status -eq "pass" }).Count
$failCount = @($results | Where-Object { $_.status -ne "pass" }).Count
$status = if ($failCount -eq 0) { "pass" } elseif ($successCount -gt 0) { "partial" } else { "fail" }
$identityRollup = Get-PackIdentityRollup -ResultEntries $results

if ($SkipFinalize) {
    Write-Host ""
    Write-Host "Android UI state pack role slice complete."
    Write-Host "Output: $outputDir"
    Write-Host "Role manifests: $roleManifestDir"
    Write-Host ("Pass: {0} / {1}" -f $successCount, $results.Count)
    exit 0
}

$manifest = [pscustomobject]@{
    run_started_utc = $runStartedUtc.ToString("o")
    run_completed_utc = $runCompletedUtc.ToString("o")
    output_dir = $outputDir
    status = $status
    host_states_included = (-not $SkipHostStates)
    device_matrix = $deviceMatrix
    results = $results
}
($manifest | ConvertTo-Json -Depth 8) | Set-Content -LiteralPath $manifestPath -Encoding UTF8

$summary = [pscustomobject]@{
    run_started_utc = $runStartedUtc.ToString("o")
    run_completed_utc = $runCompletedUtc.ToString("o")
    summary_generated_at_utc = (Get-Date).ToUniversalTime().ToString("o")
    output_dir = $outputDir
    status = $status
    host_states_included = (-not $SkipHostStates)
    total_states = [int]$results.Count
    pass_count = [int]$successCount
    fail_count = [int]$failCount
    matrix_homogeneous = [bool]$identityRollup.matrix_homogeneous
    matrix_apk_sha = $identityRollup.matrix_apk_sha
    matrix_model_name = $identityRollup.matrix_model_name
    matrix_model_sha = $identityRollup.matrix_model_sha
    devices = $identityRollup.devices
    manifest_path = $manifestPath
    readme_path = $readmePath
}
($summary | ConvertTo-Json -Depth 8) | Set-Content -LiteralPath $summaryPath -Encoding UTF8

$readme = @(
    '# Android UI State Pack',
    '',
    ("Status: **{0}**" -f $status),
    '',
    'Fixed emulator matrix:',
    '- `phone_portrait` = `emulator-5556` / `Senku_Large_4` / `1080x2400`',
    '- `phone_landscape` = `emulator-5560` / `Senku_Large_3` / `2400x1080`',
    '- `tablet_portrait` = `emulator-5554` / `Senku_Tablet_2` / `1600x2560`',
    '- `tablet_landscape` = `emulator-5558` / `Senku_Tablet` / `2560x1600`',
    '',
    'Included states:',
    '- home entry',
    '- search results',
    '- browse linked-guide handoff',
    '- home guide-connection detail handoff',
    '- deterministic detail',
    '- host generative detail',
    '- follow-up thread',
    '- guide-detail related-guide state',
    '- off-rail cross-reference state',
    '- answer-source anchored cross-reference state',
    '- neutral provenance state',
    '- phone-landscape compact cross-reference cue',
    '- tablet-landscape source-context state',
    '',
    'Artifacts:',
    '- screenshots by posture under `screenshots/`',
    '- matching XML dumps under `dumps/`',
    '- per-state summaries under `summaries/`',
    '- machine-readable manifest in `manifest.json`',
    '- compact matrix rollup in `summary.json`'
)
$readme | Set-Content -LiteralPath $readmePath -Encoding UTF8

$zipPath = Join-Path (Split-Path -Parent $outputDir) ($timestamp + "_bundle.zip")
$bundleZip = Write-AndroidHarnessZipBundle -SourceDirectory $outputDir -DestinationZip $zipPath

Write-Host ""
Write-Host "Android UI state pack complete."
Write-Host "Output: $outputDir"
Write-Host "Summary: $summaryPath"
Write-Host "Manifest: $manifestPath"
Write-Host "Bundle: $bundleZip"
Write-Host ("Pass: {0} / {1}" -f $successCount, $results.Count)

if ($status -ne "pass") {
    exit 1
}

exit 0
