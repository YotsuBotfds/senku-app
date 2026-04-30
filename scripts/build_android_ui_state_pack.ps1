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
    [switch]$FinalizeOnly,
    [object]$NormalizeFilteredReview = $true
)

$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

function ConvertTo-BoolParameter {
    param(
        [object]$Value,
        [string]$Name
    )

    if ($Value -is [bool]) {
        return [bool]$Value
    }

    $text = ([string]$Value).Trim()
    switch ($text.ToLowerInvariant()) {
        "true" { return $true }
        "1" { return $true }
        "yes" { return $true }
        "false" { return $false }
        "0" { return $false }
        "no" { return $false }
        default { throw ("{0} must be true or false; got '{1}'" -f $Name, $text) }
    }
}

$NormalizeFilteredReview = ConvertTo-BoolParameter -Value $NormalizeFilteredReview -Name "NormalizeFilteredReview"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$androidRoot = Join-Path $repoRoot "android-app"
$gradlew = Join-Path $androidRoot "gradlew.bat"
$smokeScript = Join-Path $PSScriptRoot "run_android_instrumented_ui_smoke.ps1"
$detailFollowupScript = Join-Path $PSScriptRoot "run_android_detail_followup.ps1"
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
$goalPackValidator = Join-Path $PSScriptRoot "validate_android_mock_goal_pack.py"
$goalMockFrameExporter = Join-Path $PSScriptRoot "export_android_goal_mock_frame.ps1"

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
if (-not (Test-Path -LiteralPath $goalPackValidator)) {
    throw "validate_android_mock_goal_pack.py not found at $goalPackValidator"
}
if (-not (Test-Path -LiteralPath $goalMockFrameExporter)) {
    throw "export_android_goal_mock_frame.ps1 not found at $goalMockFrameExporter"
}

Import-Module $commonHarnessModule -Force -DisableNameChecking

$GoalMockFamilies = @("home", "search", "thread", "guide", "answer")
$GoalMockRoles = @("phone_portrait", "phone_landscape", "tablet_portrait", "tablet_landscape")
$GoalMockRoleSlugByRole = @{
    phone_portrait = "phone-portrait"
    phone_landscape = "phone-landscape"
    tablet_portrait = "tablet-portrait"
    tablet_landscape = "tablet-landscape"
}
$GoalMockCaptureSuffixByFamily = @{
    home = "__home_entry.png"
    search = "__search_results.png"
    thread = "__followup_thread.png"
    guide = "__guide_related_paths.png"
    answer = "__generative_detail.png"
    emergency = "__emergency_portrait_answer.png"
}

function Get-ExpectedGoalMockNames {
    $names = New-Object System.Collections.Generic.List[string]
    foreach ($family in $GoalMockFamilies) {
        foreach ($role in $GoalMockRoles) {
            $names.Add(("{0}-{1}.png" -f $family, $GoalMockRoleSlugByRole[$role]))
        }
    }
    $names.Add("emergency-phone-portrait.png")
    $names.Add("emergency-tablet-portrait.png")
    return @($names | Sort-Object)
}

function Get-GoalMockCanonicalName {
    param([string]$Family, [string]$Role)

    if ($Family -eq "emergency") {
        if ($Role -eq "phone_portrait") { return "emergency-phone-portrait.png" }
        if ($Role -eq "tablet_portrait") { return "emergency-tablet-portrait.png" }
        return $null
    }
    if (-not $GoalMockRoleSlugByRole.ContainsKey($Role)) {
        return $null
    }
    return ("{0}-{1}.png" -f $Family, $GoalMockRoleSlugByRole[$Role])
}

function Select-GoalMockScreenshot {
    param([object]$Entry, [string]$Family)

    $suffix = [string]$GoalMockCaptureSuffixByFamily[$Family]
    if ([string]::IsNullOrWhiteSpace($suffix)) {
        return $null
    }
    $candidates = @($Entry.screenshots | Where-Object {
        -not [string]::IsNullOrWhiteSpace([string]$_) -and
        ([System.IO.Path]::GetFileName([string]$_)).EndsWith($suffix, [System.StringComparison]::Ordinal)
    })
    if ($candidates.Count -eq 0) {
        return $null
    }
    return [string]$candidates[0]
}

function Write-GoalMockZip {
    param([string]$MocksDir, [string]$ZipPath)

    if (Test-Path -LiteralPath $ZipPath) {
        Remove-Item -LiteralPath $ZipPath -Force
    }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::CreateFromDirectory($MocksDir, $ZipPath)
    return $ZipPath
}

function Invoke-GoalMockValidator {
    param([string]$Path)

    $validatorOutput = & python -B $goalPackValidator $Path 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw ("Goal mock pack validation failed for {0}: {1}" -f $Path, (($validatorOutput | Out-String).Trim()))
    }
    foreach ($line in @($validatorOutput)) {
        if (-not [string]::IsNullOrWhiteSpace([string]$line)) {
            Write-Host $line
        }
    }
}

function Invoke-GoalMockFrameExporter {
    param(
        [string]$MocksDir,
        [string]$MetadataPath,
        [string[]]$TargetNames = @(),
        [bool]$AllowPartial = $false
    )

    $referenceDir = Join-Path $repoRoot "artifacts\mocks"
    $exportOutput = & powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass -File $goalMockFrameExporter `
        -MocksDir $MocksDir `
        -MetadataPath $MetadataPath `
        -TargetNames $TargetNames `
        -AllowPartial:$AllowPartial `
        -ReferenceDir $referenceDir 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw ("Goal mock deterministic frame export failed for {0}: {1}" -f $MocksDir, (($exportOutput | Out-String).Trim()))
    }
    foreach ($line in @($exportOutput)) {
        if (-not [string]::IsNullOrWhiteSpace([string]$line)) {
            Write-Host $line
        }
    }
}

function Export-NormalizedTabletReviewSet {
    param(
        [string]$MocksDir,
        [string]$OutputDir
    )

    $reviewDir = Join-Path $OutputDir "normalized_tablet_review"
    if (Test-Path -LiteralPath $reviewDir) {
        Remove-Item -LiteralPath $reviewDir -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $reviewDir | Out-Null

    $copied = New-Object System.Collections.Generic.List[object]
    $tabletMocks = @(Get-ChildItem -LiteralPath $MocksDir -Filter "*-tablet-*.png" -File | Sort-Object Name)
    foreach ($mock in $tabletMocks) {
        $destination = Join-Path $reviewDir $mock.Name
        Copy-Item -LiteralPath $mock.FullName -Destination $destination -Force
        $copied.Add([pscustomobject]@{
            name = $mock.Name
            source = $mock.FullName
            destination = $destination
            sha256 = (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant()
        })
    }

    $metadataPath = Join-Path $reviewDir "metadata.json"
    ([pscustomobject]@{
        schema_version = 1
        output_mode = "normalized_tablet_review_mocks"
        generated_at_utc = (Get-Date).ToUniversalTime().ToString("o")
        review_dir = $reviewDir
        source_mocks_dir = $MocksDir
        source_exporter = "scripts/export_android_goal_mock_frame.ps1"
        png_count = [int]$copied.Count
        raw_state_pack_screenshots_unchanged = $true
        source_policy = "copy tablet canonical mocks after deterministic frame export; no raw screenshot rewrite"
        live_os_chrome_policy = "review PNGs inherit deterministic crop/frame normalization from canonical mocks"
        files = @($copied.ToArray())
    } | ConvertTo-Json -Depth 8) | Set-Content -LiteralPath $metadataPath -Encoding UTF8

    return [pscustomobject]@{
        status = "pass"
        review_dir = $reviewDir
        metadata_path = $metadataPath
        png_count = [int]$copied.Count
        png_names = @($copied | ForEach-Object { $_.name })
        raw_state_pack_screenshots_unchanged = $true
        source_exporter = "scripts/export_android_goal_mock_frame.ps1"
    }
}

function Export-NormalizedReviewSet {
    param(
        [string]$MocksDir,
        [string]$OutputDir
    )

    $reviewDir = Join-Path $OutputDir "normalized_review"
    if (Test-Path -LiteralPath $reviewDir) {
        Remove-Item -LiteralPath $reviewDir -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $reviewDir | Out-Null

    $copied = New-Object System.Collections.Generic.List[object]
    $mocks = @(Get-ChildItem -LiteralPath $MocksDir -Filter "*.png" -File | Sort-Object Name)
    foreach ($mock in $mocks) {
        $destination = Join-Path $reviewDir $mock.Name
        Copy-Item -LiteralPath $mock.FullName -Destination $destination -Force
        $copied.Add([pscustomobject]@{
            name = $mock.Name
            source = $mock.FullName
            destination = $destination
            sha256 = (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant()
        })
    }

    $metadataPath = Join-Path $reviewDir "metadata.json"
    ([pscustomobject]@{
        schema_version = 1
        output_mode = "normalized_review_mocks"
        generated_at_utc = (Get-Date).ToUniversalTime().ToString("o")
        review_dir = $reviewDir
        source_mocks_dir = $MocksDir
        source_exporter = "scripts/export_android_goal_mock_frame.ps1"
        png_count = [int]$copied.Count
        raw_state_pack_screenshots_unchanged = $true
        source_policy = "copy all canonical mocks after deterministic frame export; no raw screenshot rewrite"
        live_os_chrome_policy = "review PNGs inherit deterministic crop/frame normalization from canonical mocks"
        files = @($copied.ToArray())
    } | ConvertTo-Json -Depth 8) | Set-Content -LiteralPath $metadataPath -Encoding UTF8

    return [pscustomobject]@{
        status = "pass"
        review_dir = $reviewDir
        metadata_path = $metadataPath
        png_count = [int]$copied.Count
        png_names = @($copied | ForEach-Object { $_.name })
        raw_state_pack_screenshots_unchanged = $true
        source_exporter = "scripts/export_android_goal_mock_frame.ps1"
    }
}

function Export-FilteredNormalizedReviewSet {
    param(
        [string]$MocksDir,
        [string]$OutputDir,
        [string[]]$ExpectedNames,
        [string[]]$ActualNames,
        [string[]]$MissingNames,
        [string[]]$Failures,
        [bool]$Normalize
    )
    $reviewDir = Join-Path $OutputDir "filtered_normalized_review"
    if (Test-Path -LiteralPath $reviewDir) {
        Remove-Item -LiteralPath $reviewDir -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $reviewDir | Out-Null

    $failureAccumulator = New-Object System.Collections.Generic.List[string]
    foreach ($message in @($Failures)) {
        if (-not [string]::IsNullOrWhiteSpace([string]$message)) {
            $failureAccumulator.Add([string]$message)
        }
    }

    $targetMockNames = @($ActualNames | Where-Object { $ExpectedNames -contains $_ } | Sort-Object)
    $sourceDirForReview = $MocksDir
    $sourcePolicy = "copy available canonical-named mocks from an incomplete filtered pack; no raw screenshot rewrite"
    $liveChromePolicy = "review PNGs are copied raw"
    $exportedMetadataPath = Join-Path $OutputDir "filtered_normalized_review_metadata.json"
    $normalizationDir = $null

    if ($Normalize -and $targetMockNames.Count -gt 0) {
        $normalizationDir = Join-Path $OutputDir "filtered_normalized_review_frame_tmp"
        if (Test-Path -LiteralPath $normalizationDir) {
            Remove-Item -LiteralPath $normalizationDir -Recurse -Force
        }
        New-Item -ItemType Directory -Force -Path $normalizationDir | Out-Null

        try {
            foreach ($mockName in @($targetMockNames)) {
                $sourcePath = Join-Path $MocksDir $mockName
                if (Test-Path -LiteralPath $sourcePath) {
                    Copy-Item -LiteralPath $sourcePath -Destination (Join-Path $normalizationDir $mockName) -Force
                }
            }

            Invoke-GoalMockFrameExporter `
                -MocksDir $normalizationDir `
                -MetadataPath $exportedMetadataPath `
                -TargetNames $targetMockNames `
                -AllowPartial $true
            $sourceDirForReview = $normalizationDir
            $sourcePolicy = "deterministically frame-export filtered canonical mocks for OS chrome removal"
            $liveChromePolicy = "review PNGs inherit deterministic crop/frame normalization from canonical mocks"
        } catch {
            $failureAccumulator.Add(("filtered normalized review chrome-crop pass failed; using raw filtered mocks: {0}" -f $_.Exception.Message))
            if (Test-Path -LiteralPath $exportedMetadataPath) {
                Remove-Item -LiteralPath $exportedMetadataPath -Force
            }
        }
    }

    $copied = New-Object System.Collections.Generic.List[object]
    $mocks = @(Get-ChildItem -LiteralPath $sourceDirForReview -Filter "*.png" -File | Sort-Object Name)
    foreach ($mock in $mocks) {
        if ($ExpectedNames -notcontains $mock.Name) {
            continue
        }
        $destination = Join-Path $reviewDir $mock.Name
        Copy-Item -LiteralPath $mock.FullName -Destination $destination -Force
        $copied.Add([pscustomobject]@{
            name = $mock.Name
            source = $mock.FullName
            destination = $destination
            sha256 = (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant()
        })
    }

    $metadataPath = Join-Path $reviewDir "metadata.json"
    ([pscustomobject]@{
        schema_version = 1
        output_mode = "filtered_normalized_review_mocks"
        status = "skipped_canonical_incomplete"
        generated_at_utc = (Get-Date).ToUniversalTime().ToString("o")
        review_dir = $reviewDir
        source_mocks_dir = $MocksDir
        png_count = [int]$copied.Count
        expected_count = [int]$ExpectedNames.Count
        actual_count = [int]$ActualNames.Count
        missing_count = [int]$MissingNames.Count
        expected_names = @($ExpectedNames)
        actual_names = @($ActualNames)
        missing_names = @($MissingNames)
        canonical_pack_status = "fail"
        canonical_review_status = "skipped"
        raw_state_pack_screenshots_unchanged = $true
        source_policy = $sourcePolicy
        source_exporter = "scripts/export_android_goal_mock_frame.ps1"
        live_os_chrome_policy = $liveChromePolicy
        review_policy = "human convenience artifact only; full normalized_review and mock ZIP remain available only for complete 22-PNG canonical packs"
        failures = @($failureAccumulator.ToArray())
        files = @($copied.ToArray())
    } | ConvertTo-Json -Depth 8) | Set-Content -LiteralPath $metadataPath -Encoding UTF8

    if ($null -ne $normalizationDir -and (Test-Path -LiteralPath $normalizationDir)) {
        Remove-Item -LiteralPath $normalizationDir -Recurse -Force
    }
    if (Test-Path -LiteralPath $exportedMetadataPath) {
        Remove-Item -LiteralPath $exportedMetadataPath -Force
    }

    return [pscustomobject]@{
        status = "skipped_canonical_incomplete"
        review_dir = $reviewDir
        metadata_path = $metadataPath
        png_count = [int]$copied.Count
        expected_count = [int]$ExpectedNames.Count
        actual_count = [int]$ActualNames.Count
        missing_count = [int]$MissingNames.Count
        png_names = @($copied | ForEach-Object { $_.name })
        raw_state_pack_screenshots_unchanged = $true
        source_policy = $sourcePolicy
        source_exporter = "scripts/export_android_goal_mock_frame.ps1"
        review_policy = "human convenience artifact only; full-pack parity contract unchanged"
    }
}

function New-SkippedNormalizedTabletReviewSet {
    param(
        [string]$Reason
    )

    return [pscustomobject]@{
        status = "skipped"
        reason = $Reason
        review_dir = $null
        metadata_path = $null
        png_count = 0
        png_names = @()
        raw_state_pack_screenshots_unchanged = $true
        source_exporter = "scripts/export_android_goal_mock_frame.ps1"
    }
}

function New-SkippedNormalizedReviewSet {
    param(
        [string]$Reason
    )

    return [pscustomobject]@{
        status = "skipped"
        reason = $Reason
        review_dir = $null
        metadata_path = $null
        png_count = 0
        png_names = @()
        raw_state_pack_screenshots_unchanged = $true
        source_exporter = "scripts/export_android_goal_mock_frame.ps1"
    }
}

function Export-GoalMockPack {
    param(
        [object[]]$ResultEntries,
        [string]$OutputDir,
        [string]$Timestamp
    )

    $mocksDir = Join-Path $OutputDir "mocks"
    if (Test-Path -LiteralPath $mocksDir) {
        Remove-Item -LiteralPath $mocksDir -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $mocksDir | Out-Null

    $copied = New-Object System.Collections.Generic.List[object]
    $failures = New-Object System.Collections.Generic.List[string]
    foreach ($entry in @($ResultEntries)) {
        $family = [string]$entry.goal_family
        if ([string]::IsNullOrWhiteSpace($family)) {
            continue
        }

        $canonicalName = Get-GoalMockCanonicalName -Family $family -Role ([string]$entry.role)
        if ([string]::IsNullOrWhiteSpace($canonicalName)) {
            continue
        }
        if ([string]$entry.status -ne "pass") {
            $failures.Add(("goal source failed for {0}: {1}/{2}" -f $canonicalName, [string]$entry.role, [string]$entry.state_method))
            continue
        }

        $source = Select-GoalMockScreenshot -Entry $entry -Family $family
        if ([string]::IsNullOrWhiteSpace($source) -or -not (Test-Path -LiteralPath $source)) {
            $failures.Add(("missing goal source screenshot for {0}: {1}/{2}" -f $canonicalName, [string]$entry.role, [string]$entry.state_method))
            continue
        }

        $destination = Join-Path $mocksDir $canonicalName
        Copy-Item -LiteralPath $source -Destination $destination -Force
        $copied.Add([pscustomobject]@{
            name = $canonicalName
            source = $source
            destination = $destination
            role = [string]$entry.role
            family = $family
            state_method = [string]$entry.state_method
        })
    }

    $expectedNames = Get-ExpectedGoalMockNames
    $actualNames = @(Get-ChildItem -LiteralPath $mocksDir -Filter "*.png" -File | ForEach-Object { $_.Name } | Sort-Object)
    $missing = @($expectedNames | Where-Object { $actualNames -notcontains $_ })
    $extra = @($actualNames | Where-Object { $expectedNames -notcontains $_ })
    if ($missing.Count -gt 0) {
        $failures.Add(("missing canonical mock PNG(s): {0}" -f ($missing -join ", ")))
    }
    if ($extra.Count -gt 0) {
        $failures.Add(("unexpected canonical mock PNG(s): {0}" -f ($extra -join ", ")))
    }

    if ($failures.Count -gt 0) {
        $filteredReview = if ($actualNames.Count -gt 0) {
            Export-FilteredNormalizedReviewSet `
                -MocksDir $mocksDir `
                -OutputDir $OutputDir `
                -ExpectedNames $expectedNames `
                -ActualNames $actualNames `
                -MissingNames $missing `
                -Failures $failures `
                -Normalize $NormalizeFilteredReview
        } else {
            (New-SkippedNormalizedReviewSet -Reason "canonical mock pack incomplete and no canonical-named PNGs were available for filtered review")
        }
        return [pscustomobject]@{
            status = "fail"
            mocks_dir = $mocksDir
            zip_path = $null
            normalized_review = (New-SkippedNormalizedReviewSet -Reason "canonical mock pack incomplete; deterministic review generated only for full canonical packs")
            normalized_tablet_review = (New-SkippedNormalizedTabletReviewSet -Reason "canonical mock pack incomplete; deterministic tablet review generated only for full canonical packs")
            filtered_normalized_review = $filteredReview
            expected_count = [int]$expectedNames.Count
            actual_count = [int]$actualNames.Count
            expected_names = @($expectedNames)
            actual_names = @($actualNames)
            copied = @($copied.ToArray())
            failures = @($failures)
        }
    }

    $metadataPath = Join-Path $OutputDir "goal_mock_export_metadata.json"
    Invoke-GoalMockFrameExporter -MocksDir $mocksDir -MetadataPath $metadataPath
    Invoke-GoalMockValidator -Path $mocksDir
    $normalizedReview = Export-NormalizedReviewSet -MocksDir $mocksDir -OutputDir $OutputDir
    $normalizedTabletReview = Export-NormalizedTabletReviewSet -MocksDir $mocksDir -OutputDir $OutputDir
    $zipPath = Join-Path (Split-Path -Parent $OutputDir) ($Timestamp + "_mocks.zip")
    $zipPath = Write-GoalMockZip -MocksDir $mocksDir -ZipPath $zipPath
    Invoke-GoalMockValidator -Path $zipPath

    return [pscustomobject]@{
        status = "pass"
        mocks_dir = $mocksDir
        zip_path = $zipPath
        metadata_path = $metadataPath
        deterministic_frame_export = $true
        normalized_review = $normalizedReview
        normalized_tablet_review = $normalizedTabletReview
        filtered_normalized_review = (New-SkippedNormalizedReviewSet -Reason "not needed for full canonical packs; use normalized_review")
        expected_count = [int]$expectedNames.Count
        actual_count = [int]$actualNames.Count
        expected_names = @($expectedNames)
        actual_names = @($actualNames)
        copied = @($copied.ToArray())
        failures = @()
    }
}

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

function Test-PackPlatformAnrDetected {
    param([object]$PlatformAnr)

    if ($null -eq $PlatformAnr) {
        return $false
    }
    if ($PlatformAnr.PSObject.Properties.Name -notcontains "detected") {
        return $false
    }
    $detected = $PlatformAnr.detected
    if ($detected -is [bool]) {
        return $detected
    }
    return ([string]$detected).Trim().ToLowerInvariant() -eq "true"
}

function Get-PackPlatformAnrCount {
    param([object[]]$ResultEntries)

    $count = 0
    foreach ($entry in @($ResultEntries)) {
        if (Test-PackPlatformAnrDetected -PlatformAnr $entry.platform_anr) {
            $count += 1
            continue
        }

        $summaryPath = [string]$entry.summary_path
        if ([string]::IsNullOrWhiteSpace($summaryPath) -or -not (Test-Path -LiteralPath $summaryPath)) {
            continue
        }

        try {
            $stateSummary = Get-Content -LiteralPath $summaryPath -Raw | ConvertFrom-Json
        } catch {
            continue
        }
        if (Test-PackPlatformAnrDetected -PlatformAnr $stateSummary.platform_anr) {
            $count += 1
        }
    }
    return $count
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

function Get-FirstNonEmptyPackMetadataValue {
    param(
        [object[]]$Entries,
        [string]$PropertyName
    )

    foreach ($entry in @($Entries)) {
        if ($null -eq $entry) {
            continue
        }
        $candidate = $entry.$PropertyName
        if ($null -ne $candidate -and -not [string]::IsNullOrWhiteSpace([string]$candidate)) {
            return [string]$candidate
        }
    }
    return $null
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

    $deviceSummaryArray = @($deviceSummaries.ToArray())
    $deviceCount = @($deviceSummaryArray).Count
    $apkHomogeneous = $deviceCount -gt 0
    if ($apkHomogeneous) {
        $apkHomogeneous = (@($deviceSummaryArray | Where-Object {
                $_.identity_conflict -or [string]::IsNullOrWhiteSpace([string]$_.apk_sha)
            }).Count -eq 0) -and
            (@($deviceSummaryArray | Select-Object -ExpandProperty apk_sha -Unique).Count -eq 1)
    }

    $modelHomogeneous = $deviceCount -gt 0
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
        devices = @($deviceSummaryArray)
        matrix_homogeneous = [bool]$homogeneous
        matrix_apk_sha = $matrixApkSha
        matrix_model_name = $matrixModelName
        matrix_model_sha = $matrixModelSha
    }
}

function Add-PackMetadataSetValue {
    param(
        [object]$Set,
        [object]$Value
    )

    $text = [string]$Value
    if (-not [string]::IsNullOrWhiteSpace($text)) {
        $null = $Set.Add($text)
    }
}

function Get-PackObjectPropertyValue {
    param(
        [object]$InputObject,
        [string[]]$Names
    )

    if ($null -eq $InputObject) {
        return $null
    }

    foreach ($name in @($Names)) {
        if ($InputObject.PSObject.Properties.Name -contains $name) {
            return $InputObject.$name
        }
    }
    return $null
}

function Convert-PackIntOrNull {
    param([object]$Value)

    $parsed = 0
    if ($null -eq $Value) {
        return $null
    }
    if ([int]::TryParse([string]$Value, [ref]$parsed)) {
        return [int]$parsed
    }
    return $null
}

function Get-PackScreenshotDimensions {
    param([object]$Summary)

    if ($null -eq $Summary) {
        return $null
    }

    $dimensionContainer = Get-PackObjectPropertyValue -InputObject $Summary -Names @(
        "primary_screenshot_dimensions",
        "screenshot_dimensions",
        "captured_dimensions",
        "display_dimensions"
    )
    $width = Get-PackObjectPropertyValue -InputObject $dimensionContainer -Names @("width", "screenshot_width", "display_width")
    $height = Get-PackObjectPropertyValue -InputObject $dimensionContainer -Names @("height", "screenshot_height", "display_height")

    if ($null -eq $width) {
        $width = Get-PackObjectPropertyValue -InputObject $Summary -Names @("primary_screenshot_width", "screenshot_width", "display_width")
    }
    if ($null -eq $height) {
        $height = Get-PackObjectPropertyValue -InputObject $Summary -Names @("primary_screenshot_height", "screenshot_height", "display_height")
    }

    if ($null -eq $width -or $null -eq $height) {
        $artifactFacts = Get-PackObjectPropertyValue -InputObject $Summary -Names @("artifact_facts")
        $firstScreenshot = Get-PackObjectPropertyValue -InputObject $artifactFacts -Names @("first_screenshot", "primary_screenshot", "screenshot")
        $nestedDimensions = Get-PackObjectPropertyValue -InputObject $firstScreenshot -Names @("dimensions_px", "dimensions", "captured_dimensions", "display_dimensions")
        if ($null -ne $nestedDimensions) {
            if ($null -eq $width) {
                $width = Get-PackObjectPropertyValue -InputObject $nestedDimensions -Names @("width", "screenshot_width", "display_width")
            }
            if ($null -eq $height) {
                $height = Get-PackObjectPropertyValue -InputObject $nestedDimensions -Names @("height", "screenshot_height", "display_height")
            }
        }
    }

    $parsedWidth = Convert-PackIntOrNull -Value $width
    $parsedHeight = Convert-PackIntOrNull -Value $height
    if ($null -eq $parsedWidth -or $null -eq $parsedHeight) {
        return $null
    }

    return [pscustomobject]@{
        width = [int]$parsedWidth
        height = [int]$parsedHeight
    }
}

function Get-PackViewportFactsRollup {
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
                orientations = New-Object System.Collections.Generic.HashSet[string]
                screenshot_sizes = New-Object System.Collections.Generic.HashSet[string]
                physical_sizes = New-Object System.Collections.Generic.HashSet[string]
                densities = New-Object System.Collections.Generic.HashSet[string]
                smallest_widths = New-Object System.Collections.Generic.HashSet[string]
                resolved_roles = New-Object System.Collections.Generic.HashSet[string]
                state_count = 0
                metadata_count = 0
                screenshot_dimension_count = 0
                device_facts_count = 0
                rotation_mismatch_count = 0
            }
        }

        $bucket = $deviceBuckets[$device]
        $null = $bucket.roles.Add([string]$entry.role)
        Add-PackMetadataSetValue -Set $bucket.orientations -Value $entry.orientation
        $bucket.state_count += 1

        $summaryPath = [string]$entry.summary_path
        if ([string]::IsNullOrWhiteSpace($summaryPath) -or -not (Test-Path -LiteralPath $summaryPath)) {
            continue
        }

        try {
            $summary = Get-Content -LiteralPath $summaryPath -Raw | ConvertFrom-Json
        } catch {
            continue
        }

        $bucket.metadata_count += 1
        $screenshotDimensions = Get-PackScreenshotDimensions -Summary $summary
        if ($null -ne $screenshotDimensions) {
            $bucket.screenshot_dimension_count += 1
            $null = $bucket.screenshot_sizes.Add(("{0}x{1}" -f $screenshotDimensions.width, $screenshotDimensions.height))
        }

        $deviceFacts = Get-PackObjectPropertyValue -InputObject $summary -Names @("device_facts")
        if ($null -eq $deviceFacts) {
            continue
        }
        $bucket.device_facts_count += 1

        $physicalSize = Get-PackObjectPropertyValue -InputObject $deviceFacts -Names @("physical_size_px", "physical_size", "display_size_px")
        $physicalWidth = Get-PackObjectPropertyValue -InputObject $physicalSize -Names @("width", "display_width")
        $physicalHeight = Get-PackObjectPropertyValue -InputObject $physicalSize -Names @("height", "display_height")
        $parsedPhysicalWidth = Convert-PackIntOrNull -Value $physicalWidth
        $parsedPhysicalHeight = Convert-PackIntOrNull -Value $physicalHeight
        if ($null -ne $parsedPhysicalWidth -and $null -ne $parsedPhysicalHeight) {
            $null = $bucket.physical_sizes.Add(("{0}x{1}" -f $parsedPhysicalWidth, $parsedPhysicalHeight))
        }

        Add-PackMetadataSetValue -Set $bucket.densities -Value (Get-PackObjectPropertyValue -InputObject $deviceFacts -Names @("density_dpi"))
        Add-PackMetadataSetValue -Set $bucket.smallest_widths -Value (Get-PackObjectPropertyValue -InputObject $deviceFacts -Names @("smallest_width_dp"))
        Add-PackMetadataSetValue -Set $bucket.resolved_roles -Value (Get-PackObjectPropertyValue -InputObject $deviceFacts -Names @("resolved_role"))
        if ((Get-PackObjectPropertyValue -InputObject $deviceFacts -Names @("rotation_mismatch")) -eq $true) {
            $bucket.rotation_mismatch_count += 1
        }
    }

    $deviceSummaries = New-Object System.Collections.Generic.List[object]
    foreach ($device in $deviceBuckets.Keys) {
        $bucket = $deviceBuckets[$device]
        $deviceSummaries.Add([pscustomobject]@{
            device = $device
            roles = @($bucket.roles | Sort-Object)
            orientations = @($bucket.orientations | Sort-Object)
            state_count = [int]$bucket.state_count
            metadata_count = [int]$bucket.metadata_count
            device_facts_count = [int]$bucket.device_facts_count
            screenshot_dimension_count = [int]$bucket.screenshot_dimension_count
            screenshot_sizes = @($bucket.screenshot_sizes | Sort-Object)
            physical_sizes = @($bucket.physical_sizes | Sort-Object)
            density_dpi_values = @($bucket.densities | Sort-Object)
            smallest_width_dp_values = @($bucket.smallest_widths | Sort-Object)
            resolved_roles = @($bucket.resolved_roles | Sort-Object)
            rotation_mismatch_count = [int]$bucket.rotation_mismatch_count
        })
    }

    $deviceSummaryArray = @($deviceSummaries.ToArray())
    return [pscustomobject]@{
        metadata_present = [bool](@($deviceSummaryArray | Where-Object { $_.metadata_count -gt 0 }).Count -gt 0)
        device_count = [int]@($deviceSummaryArray).Count
        states_with_device_facts = [int](@($deviceSummaryArray | Measure-Object -Property device_facts_count -Sum).Sum)
        states_with_screenshot_dimensions = [int](@($deviceSummaryArray | Measure-Object -Property screenshot_dimension_count -Sum).Sum)
        rotation_mismatch_count = [int](@($deviceSummaryArray | Measure-Object -Property rotation_mismatch_count -Sum).Sum)
        devices = @($deviceSummaryArray)
    }
}

function Get-PackInstalledPackRollup {
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
                statuses = New-Object System.Collections.Generic.HashSet[string]
                pack_formats = New-Object System.Collections.Generic.HashSet[string]
                pack_versions = New-Object System.Collections.Generic.HashSet[string]
                generated_at_values = New-Object System.Collections.Generic.HashSet[string]
                answer_card_counts = New-Object System.Collections.Generic.HashSet[string]
                sqlite_shas = New-Object System.Collections.Generic.HashSet[string]
                vector_shas = New-Object System.Collections.Generic.HashSet[string]
                state_count = 0
                available_count = 0
                metadata_count = 0
            }
        }

        $bucket = $deviceBuckets[$device]
        $null = $bucket.roles.Add([string]$entry.role)
        $bucket.state_count += 1

        $summaryPath = [string]$entry.summary_path
        if ([string]::IsNullOrWhiteSpace($summaryPath) -or -not (Test-Path -LiteralPath $summaryPath)) {
            continue
        }

        try {
            $summary = Get-Content -LiteralPath $summaryPath -Raw | ConvertFrom-Json
        } catch {
            continue
        }

        if ($null -eq $summary.installed_pack) {
            continue
        }
        $bucket.metadata_count += 1

        $installedPack = $summary.installed_pack
        $status = [string]$installedPack.status
        if (-not [string]::IsNullOrWhiteSpace($status)) {
            $null = $bucket.statuses.Add($status)
            if ($status -eq "available") {
                $bucket.available_count += 1
            }
        }
        Add-PackMetadataSetValue -Set $bucket.pack_formats -Value $installedPack.pack_format
        Add-PackMetadataSetValue -Set $bucket.pack_versions -Value $installedPack.pack_version
        Add-PackMetadataSetValue -Set $bucket.generated_at_values -Value $installedPack.generated_at
        Add-PackMetadataSetValue -Set $bucket.answer_card_counts -Value $installedPack.counts.answer_cards
        Add-PackMetadataSetValue -Set $bucket.sqlite_shas -Value $installedPack.sqlite.manifest_sha256
        Add-PackMetadataSetValue -Set $bucket.vector_shas -Value $installedPack.vectors.manifest_sha256
    }

    $deviceSummaries = New-Object System.Collections.Generic.List[object]
    foreach ($device in $deviceBuckets.Keys) {
        $bucket = $deviceBuckets[$device]
        $formats = @($bucket.pack_formats | Sort-Object)
        $versions = @($bucket.pack_versions | Sort-Object)
        $generated = @($bucket.generated_at_values | Sort-Object)
        $answerCards = @($bucket.answer_card_counts | Sort-Object)
        $sqliteShas = @($bucket.sqlite_shas | Sort-Object)
        $vectorShas = @($bucket.vector_shas | Sort-Object)
        $statuses = @($bucket.statuses | Sort-Object)
        $metadataMissing = ([int]$bucket.metadata_count -eq 0)
        $metadataConflict = ($formats.Count -gt 1) -or ($versions.Count -gt 1) -or ($answerCards.Count -gt 1) -or ($sqliteShas.Count -gt 1) -or ($vectorShas.Count -gt 1)

        $deviceSummaries.Add([pscustomobject]@{
            device = $device
            roles = @($bucket.roles | Sort-Object)
            state_count = [int]$bucket.state_count
            metadata_count = [int]$bucket.metadata_count
            available_count = [int]$bucket.available_count
            statuses = @($statuses)
            pack_format = $(if ($formats.Count -eq 1) { $formats[0] } else { $null })
            pack_version = $(if ($versions.Count -eq 1) { [int]$versions[0] } else { $null })
            generated_at = $(if ($generated.Count -eq 1) { $generated[0] } else { $null })
            answer_cards = $(if ($answerCards.Count -eq 1) { [int]$answerCards[0] } else { $null })
            sqlite_manifest_sha256 = $(if ($sqliteShas.Count -eq 1) { $sqliteShas[0] } else { $null })
            vectors_manifest_sha256 = $(if ($vectorShas.Count -eq 1) { $vectorShas[0] } else { $null })
            metadata_conflict = [bool]$metadataConflict
            metadata_missing = [bool]$metadataMissing
        })
    }

    $deviceSummaryArray = @($deviceSummaries.ToArray())
    $devicesWithMetadata = @($deviceSummaryArray | Where-Object { -not $_.metadata_missing })
    $homogeneous = ($devicesWithMetadata.Count -gt 0) -and (@($devicesWithMetadata | Where-Object { $_.metadata_conflict }).Count -eq 0)
    foreach ($propertyName in @("pack_format", "pack_version", "generated_at", "answer_cards", "sqlite_manifest_sha256", "vectors_manifest_sha256")) {
        if ($homogeneous -and @($devicesWithMetadata | Select-Object -ExpandProperty $propertyName -Unique | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) }).Count -gt 1) {
            $homogeneous = $false
        }
    }

    return [pscustomobject]@{
        devices = @($deviceSummaryArray)
        metadata_present = [bool]($devicesWithMetadata.Count -gt 0)
        matrix_homogeneous = [bool]$homogeneous
        pack_format = $(if ($homogeneous) { @($devicesWithMetadata | Select-Object -ExpandProperty pack_format -Unique | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) })[0] } else { $null })
        pack_version = $(if ($homogeneous) { @($devicesWithMetadata | Select-Object -ExpandProperty pack_version -Unique | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) })[0] } else { $null })
        generated_at = $(if ($homogeneous) { @($devicesWithMetadata | Select-Object -ExpandProperty generated_at -Unique | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) })[0] } else { $null })
        answer_cards = $(if ($homogeneous) { @($devicesWithMetadata | Select-Object -ExpandProperty answer_cards -Unique | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) })[0] } else { $null })
        sqlite_manifest_sha256 = $(if ($homogeneous) { @($devicesWithMetadata | Select-Object -ExpandProperty sqlite_manifest_sha256 -Unique | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) })[0] } else { $null })
        vectors_manifest_sha256 = $(if ($homogeneous) { @($devicesWithMetadata | Select-Object -ExpandProperty vectors_manifest_sha256 -Unique | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) })[0] } else { $null })
    }
}

function New-StateDefinition {
    param(
        [string]$Name = "",
        [string]$Method,
        [string]$GoalFamily = "",
        [string]$Runner = "instrumented",
        [string]$InitialQuery = "",
        [string]$FollowUpQuery = "",
        [switch]$HostBacked,
        [switch]$FollowUp
    )

    return [pscustomobject]@{
        name = $Name
        method = $Method
        goal_family = $GoalFamily
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
    (New-StateDefinition -Method "homeEntryShowsPrimaryBrowseAndAskLanes" -GoalFamily "home"),
    (New-StateDefinition -Method "searchQueryShowsResultsWithoutShellPolling" -GoalFamily "search"),
    (New-StateDefinition -Method "scriptedSeededFollowUpThreadShowsInlineHistory" -GoalFamily "thread"),
    (New-StateDefinition -Method "guideDetailShowsRelatedGuideNavigation" -GoalFamily "guide"),
    (New-StateDefinition -Method "generativeAskWithHostInferenceNavigatesToDetailScreen" -GoalFamily "answer" -HostBacked)
)

$specialStatesByRole = @{
    phone_portrait = @(
        (New-StateDefinition -Method "emergencyPortraitAnswerShowsImmediateActionState" -GoalFamily "emergency")
    )
    tablet_portrait = @(
        (New-StateDefinition -Method "emergencyPortraitAnswerShowsImmediateActionState" -GoalFamily "emergency")
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
                        goal_family = [string]$state.goal_family
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
                    failure_reason = $followupSummary.error_message
                    platform_anr = $followupSummary.platform_anr
                    host_inference_url = $(if ($state.host) { [string]$followupSummary.host_inference_url } else { $null })
                    host_inference_model = $(if ($state.host) { [string]$followupSummary.host_inference_model } else { $null })
                    model_identity_source = $(if ($state.host) { "host_inference" } else { $null })
                    model_name = $(if ($state.host) { [string]$followupSummary.host_inference_model } else { $null })
                    model_sha = $(if ($state.host) { Get-StableIdentitySha256 -Value ("host-inference|{0}|{1}" -f [string]$followupSummary.host_inference_url, [string]$followupSummary.host_inference_model) } else { $null })
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
                    goal_family = [string]$state.goal_family
                    status = [string]$trustedFollowupSummary.status
                    summary_path = $stateSummaryPath
                    screenshots = $copiedScreenshots
                    dumps = $copiedDumps
                    host_adb_platform_tools_version = $(if ($null -ne $trustedFollowupSummary.host_adb_platform_tools_version -and -not [string]::IsNullOrWhiteSpace([string]$trustedFollowupSummary.host_adb_platform_tools_version)) { [string]$trustedFollowupSummary.host_adb_platform_tools_version } else { $null })
                    failure_reason = $trustedFollowupSummary.failure_reason
                    platform_anr = $trustedFollowupSummary.platform_anr
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
                    goal_family = [string]$state.goal_family
                    status = "fail"
                    summary_path = $stateSummaryPath
                    screenshots = @()
                    dumps = @()
                    host_adb_platform_tools_version = $null
                    failure_reason = "smoke wrapper failed before trusted summary"
                    platform_anr = $null
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
                goal_family = [string]$state.goal_family
                status = [string]$trustedSummary.status
                summary_path = $stateSummaryPath
                screenshots = $copied.screenshots
                dumps = $copied.dumps
                host_adb_platform_tools_version = $(if ($null -ne $trustedSummary.host_adb_platform_tools_version -and -not [string]::IsNullOrWhiteSpace([string]$trustedSummary.host_adb_platform_tools_version)) { [string]$trustedSummary.host_adb_platform_tools_version } else { $null })
                failure_reason = $trustedSummary.failure_reason
                platform_anr = $trustedSummary.platform_anr
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
$platformAnrCount = Get-PackPlatformAnrCount -ResultEntries $results
$hostAdbPlatformToolsVersion = Get-FirstNonEmptyPackMetadataValue -Entries $results -PropertyName "host_adb_platform_tools_version"
$status = if ($failCount -eq 0) { "pass" } elseif ($successCount -gt 0) { "partial" } else { "fail" }
$identityRollup = Get-PackIdentityRollup -ResultEntries $results
$installedPackRollup = Get-PackInstalledPackRollup -ResultEntries $results
$viewportFactsRollup = Get-PackViewportFactsRollup -ResultEntries $results

if ($SkipFinalize) {
    Write-Host ""
    Write-Host "Android UI state pack role slice complete."
    Write-Host "Output: $outputDir"
    Write-Host "Role manifests: $roleManifestDir"
    Write-Host ("Pass: {0} / {1}" -f $successCount, $results.Count)
    exit 0
}

$goalMockPack = Export-GoalMockPack -ResultEntries $results -OutputDir $outputDir -Timestamp $timestamp
if ($goalMockPack.status -ne "pass") {
    $status = "fail"
}

$manifest = [pscustomobject]@{
    run_started_utc = $runStartedUtc.ToString("o")
    run_completed_utc = $runCompletedUtc.ToString("o")
    output_dir = $outputDir
    status = $status
    host_states_included = (-not $SkipHostStates)
    host_adb_platform_tools_version = $hostAdbPlatformToolsVersion
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
    host_adb_platform_tools_version = $hostAdbPlatformToolsVersion
    total_states = [int]$results.Count
    pass_count = [int]$successCount
    fail_count = [int]$failCount
    platform_anr_count = [int]$platformAnrCount
    matrix_homogeneous = [bool]$identityRollup.matrix_homogeneous
    matrix_apk_sha = $identityRollup.matrix_apk_sha
    matrix_model_name = $identityRollup.matrix_model_name
    matrix_model_sha = $identityRollup.matrix_model_sha
    devices = $identityRollup.devices
    installed_pack = $installedPackRollup
    viewport_facts = $viewportFactsRollup
    goal_mock_pack = $goalMockPack
    review_contract = [pscustomobject]@{
        parity_review_artifacts = @(
            "mocks",
            "goal_mock_pack.zip_path",
            "normalized_review",
            "normalized_tablet_review"
        )
        filtered_review_artifacts = @(
            "filtered_normalized_review"
        )
        raw_screenshot_artifacts = @(
            "screenshots",
            "raw"
        )
        raw_screenshot_policy = "diagnostics_only; may include live emulator OS chrome"
        parity_policy = "use deterministic framed mocks, mock ZIP, or normalized review artifacts for mock parity"
        filtered_pack_policy = "filtered role packs can have pass_count equal total_states while summary status is fail because the 22-PNG canonical mock pack is incomplete; filtered_normalized_review is a human convenience artifact only"
        filtered_review_normalize_chrome = [bool]$NormalizeFilteredReview
        raw_state_pack_screenshots_unchanged = $true
    }
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
    '- home',
    '- search',
    '- thread',
    '- guide',
    '- answer',
    '- emergency phone portrait',
    '- emergency tablet portrait',
    '',
    'Artifacts:',
    '- canonical flat mock PNGs under `mocks/`',
    '- canonical flat mock ZIP at `goal_mock_pack.zip_path` in `summary.json`',
    '- deterministic screenshot-only frame metadata at `goal_mock_pack.metadata_path`',
    '- full normalized review PNGs under `normalized_review/` (`goal_mock_pack.normalized_review`) when a full canonical mock pack is available',
    '- tablet-only normalized review PNGs under `normalized_tablet_review/` (`goal_mock_pack.normalized_tablet_review`) when a full canonical mock pack is available; filtered packs mark this as skipped and raw screenshots remain unchanged',
    '- filtered role packs may report `Status: fail` even when `pass_count == total_states`, because the full 22-PNG canonical mock pack is incomplete',
    '- filtered role packs with available canonical-named mocks also write `filtered_normalized_review/` (`goal_mock_pack.filtered_normalized_review`) as a human review aid; by default it is chrome-normalized via deterministic frame export',
    '- source screenshots by posture under `screenshots/` are raw diagnostic captures and may include live emulator OS chrome; do not use them for mock parity review',
    '- matching XML dumps under `dumps/`',
    '- per-state summaries under `summaries/`',
    '- machine-readable manifest in `manifest.json`',
    '- compact matrix rollup in `summary.json`',
    '',
    'Review contract:',
    '- mock parity review uses `mocks/`, `goal_mock_pack.zip_path`, `normalized_review/`, or `normalized_tablet_review/` only',
    '- `filtered_normalized_review/` is for filtered-pack convenience review only and remains `skipped_canonical_incomplete` until all 22 canonical PNGs are present',
    '- `-NormalizeFilteredReview $true` applies deterministic frame export to `filtered_normalized_review/` to remove emulator status/navigation chrome while keeping raw captures untouched',
    '- `-NormalizeFilteredReview $false` keeps raw filtered review captures when needed for troubleshooting',
    '- `screenshots/` and `raw/` are diagnostics for state capture/debugging, not deterministic parity artifacts'
)
$readme | Set-Content -LiteralPath $readmePath -Encoding UTF8

$bundleZip = $goalMockPack.zip_path

Write-Host ""
Write-Host "Android goal mock pack complete."
Write-Host "Output: $outputDir"
Write-Host "Summary: $summaryPath"
Write-Host "Manifest: $manifestPath"
Write-Host "Mocks: $($goalMockPack.mocks_dir)"
$normalizedReview = $goalMockPack.normalized_review
if ($null -ne $normalizedReview -and [string]$normalizedReview.status -eq "pass") {
    Write-Host "Normalized review: $($normalizedReview.review_dir)"
} elseif ($null -ne $normalizedReview -and -not [string]::IsNullOrWhiteSpace([string]$normalizedReview.reason)) {
    Write-Host "Normalized review: skipped ($($normalizedReview.reason))"
} else {
    Write-Host "Normalized review: skipped"
}
$normalizedTabletReview = $goalMockPack.normalized_tablet_review
if ($null -ne $normalizedTabletReview -and [string]$normalizedTabletReview.status -eq "pass") {
    Write-Host "Normalized tablet review: $($normalizedTabletReview.review_dir)"
} elseif ($null -ne $normalizedTabletReview -and -not [string]::IsNullOrWhiteSpace([string]$normalizedTabletReview.reason)) {
    Write-Host "Normalized tablet review: skipped ($($normalizedTabletReview.reason))"
} else {
    Write-Host "Normalized tablet review: skipped"
}
$filteredNormalizedReview = $goalMockPack.filtered_normalized_review
if ($null -ne $filteredNormalizedReview -and [string]$filteredNormalizedReview.status -eq "skipped_canonical_incomplete") {
    Write-Host "Filtered normalized review: $($filteredNormalizedReview.review_dir) (canonical incomplete; human convenience only)"
} elseif ($null -ne $filteredNormalizedReview -and -not [string]::IsNullOrWhiteSpace([string]$filteredNormalizedReview.reason)) {
    Write-Host "Filtered normalized review: skipped ($($filteredNormalizedReview.reason))"
} else {
    Write-Host "Filtered normalized review: skipped"
}
Write-Host "Goal bundle: $bundleZip"
Write-Host ("Pass: {0} / {1}" -f $successCount, $results.Count)

if ($status -ne "pass") {
    exit 1
}

exit 0
