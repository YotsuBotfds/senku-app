[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$OutputRoot = "artifacts/ui_state_pack_headless_lane",
    [ValidateSet("clean-headless", "large-litert-data")]
    [string]$LaunchProfile = "large-litert-data",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [int]$MaxParallelDevices = 4,
    [int]$BootWaitSeconds = 90,
    [switch]$SkipBuild,
    [switch]$SkipInstall,
    [switch]$SkipHostStates,
    [switch]$PlanOnly,
    [switch]$RealRun
)

$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$matrixScript = Join-Path $PSScriptRoot "start_senku_emulator_matrix.ps1"
$packScript = Join-Path $PSScriptRoot "build_android_ui_state_pack_parallel.ps1"
$laneValidatorScript = Join-Path $PSScriptRoot "validate_android_headless_state_pack_lane_summary.py"
foreach ($scriptPath in @($matrixScript, $packScript, $laneValidatorScript)) {
    if (-not (Test-Path -LiteralPath $scriptPath)) {
        throw "Required lane script not found: $scriptPath"
    }
}

$fixedRoles = @("phone_portrait", "phone_landscape", "tablet_portrait", "tablet_landscape")
$fixedMatrix = @(
    [pscustomobject]@{ role = "phone_portrait"; device = "emulator-5556"; avd = "Senku_Large_4"; dimensions = "1080x2400"; orientation = "portrait" },
    [pscustomobject]@{ role = "phone_landscape"; device = "emulator-5560"; avd = "Senku_Large_3"; dimensions = "2400x1080"; orientation = "landscape" },
    [pscustomobject]@{ role = "tablet_portrait"; device = "emulator-5554"; avd = "Senku_Tablet_2"; dimensions = "1600x2560"; orientation = "portrait" },
    [pscustomobject]@{ role = "tablet_landscape"; device = "emulator-5558"; avd = "Senku_Tablet"; dimensions = "2560x1600"; orientation = "landscape" }
)

function New-LaneRunId {
    return Get-Date -Format "yyyyMMdd_HHmmss"
}

function Get-ProfilePartitionSize {
    param([string]$Profile)

    if ($Profile -eq "large-litert-data") {
        return 8192
    }
    return 0
}

function New-AcceptanceCriteria {
    return @(
        "Real run only: -RealRun was supplied and neither -PlanOnly nor -WhatIf was active.",
        "Fixed four-emulator matrix only: phone_portrait, phone_landscape, tablet_portrait, and tablet_landscape all completed.",
        "State-pack summary.json exists under the run output and reports status pass.",
        "Summary total_states is greater than zero, pass_count equals total_states, and fail_count is zero.",
        "Planning, dry-run, launch-profile metadata, and emulator WhatIf artifacts remain non-acceptance evidence."
    )
}

function ConvertTo-QuotedCliToken {
    param([string]$Value)

    if ($Value -match "\s") {
        return "'" + ($Value -replace "'", "''") + "'"
    }
    return $Value
}

function New-ValidationCommands {
    param(
        [string]$ArtifactPath,
        [string]$Validates
    )

    return @(
        [pscustomobject]@{
            name = "validate_headless_state_pack_lane_artifact"
            command = ("& .\.venvs\senku-validate\Scripts\python.exe scripts\validate_android_headless_state_pack_lane_summary.py " + (ConvertTo-QuotedCliToken -Value $ArtifactPath))
            validates = $Validates
            artifact_path = $ArtifactPath
            plan_only = $true
            will_start_jobs = $false
            will_touch_emulators = $false
            note = "Validation only; does not start jobs or touch emulators."
        }
    )
}

function New-PlanObject {
    param(
        [string]$RunId,
        [string]$RunDir,
        [bool]$DryRun
    )

    $partitionSizeMb = Get-ProfilePartitionSize -Profile $LaunchProfile
    $matrixArgs = @(
        "-File", $matrixScript,
        "-Roles", "all",
        "-Mode", "read_only",
        "-Headless",
        "-NoBootAnim",
        "-LaunchProfile", $LaunchProfile,
        "-SummaryPath", (Join-Path $RunDir "emulator_launch_profile_summary.json"),
        "-WhatIf"
    )
    if ($partitionSizeMb -gt 0) {
        $matrixArgs += @("-PartitionSizeMb", [string]$partitionSizeMb)
    }

    $realMatrixArgs = @(
        "-File", $matrixScript,
        "-Roles", "all",
        "-Mode", "read_only",
        "-Headless",
        "-NoBootAnim"
    )
    if ($partitionSizeMb -gt 0) {
        $realMatrixArgs += @("-PartitionSizeMb", [string]$partitionSizeMb)
    }

    $packArgs = @(
        "-File", $packScript,
        "-OutputRoot", $OutputRoot,
        "-MaxParallelDevices", [string]$MaxParallelDevices,
        "-HostInferenceUrl", $HostInferenceUrl,
        "-HostInferenceModel", $HostInferenceModel
    )
    if ($SkipBuild) {
        $packArgs += "-SkipBuild"
    }
    if ($SkipInstall) {
        $packArgs += "-SkipInstall"
    }
    if ($SkipHostStates) {
        $packArgs += "-SkipHostStates"
    }

    return [pscustomobject]@{
        run_id = $RunId
        output_root = $OutputRoot
        run_dir = $RunDir
        fixed_matrix_roles = @($fixedRoles)
        fixed_matrix = @($fixedMatrix)
        launch_profile = $LaunchProfile
        headless = $true
        mode = "read_only"
        partition_size_mb = $(if ($partitionSizeMb -gt 0) { $partitionSizeMb } else { $null })
        max_parallel_devices = [int]$MaxParallelDevices
        effective_max_parallel_devices = [Math]::Max(1, $MaxParallelDevices)
        boot_wait_seconds = [int]$BootWaitSeconds
        host_inference_url = $HostInferenceUrl
        host_inference_model = $HostInferenceModel
        skip_build = [bool]$SkipBuild
        skip_install = [bool]$SkipInstall
        skip_host_states = [bool]$SkipHostStates
        plan_only = [bool]$PlanOnly
        whatif = [bool]$DryRun
        real_run_requested = [bool]$RealRun
        will_start_emulators = [bool]($RealRun -and -not $PlanOnly -and -not $DryRun)
        will_run_state_pack = [bool]($RealRun -and -not $PlanOnly -and -not $DryRun)
        acceptance_evidence = $false
        non_acceptance_evidence = $true
        acceptance_label_allowed = $false
        acceptance_criteria = New-AcceptanceCriteria
        planning_artifacts_are_acceptance = $false
        validation_commands = @(New-ValidationCommands -ArtifactPath (Join-Path $RunDir "headless_lane_plan.json") -Validates "headless_lane_plan.json")
        commands = [pscustomobject]@{
            emulator_profile_preflight = "powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass $($matrixArgs -join ' ')"
            emulator_real_run = "powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass $($realMatrixArgs -join ' ')"
            state_pack_real_run = "powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass $($packArgs -join ' ')"
        }
    }
}

function Write-JsonFile {
    param(
        [Parameter(Mandatory)]
        [object]$Value,
        [Parameter(Mandatory)]
        [string]$Path
    )

    $resolvedPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Path)
    $parent = Split-Path -Parent $resolvedPath
    if (-not [string]::IsNullOrWhiteSpace($parent)) {
        New-Item -ItemType Directory -Force -Path $parent -WhatIf:$false | Out-Null
    }
    $Value | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $resolvedPath -Encoding UTF8 -WhatIf:$false
    return $resolvedPath
}

function Test-FixedFourDeviceSummary {
    param([object]$Summary)

    if ($null -eq $Summary -or $null -eq $Summary.devices) {
        return $false
    }
    $summaryRoles = @(
        foreach ($device in @($Summary.devices)) {
            if ($null -eq $device) {
                continue
            }
            if ($null -ne $device.roles) {
                foreach ($role in @($device.roles)) {
                    if (-not [string]::IsNullOrWhiteSpace([string]$role)) {
                        [string]$role
                    }
                }
            } elseif (-not [string]::IsNullOrWhiteSpace([string]$device.role)) {
                [string]$device.role
            }
        }
    ) | Sort-Object -Unique
    $expectedRoles = @($fixedRoles | Sort-Object)
    if ($summaryRoles.Count -ne $expectedRoles.Count) {
        return $false
    }
    for ($index = 0; $index -lt $expectedRoles.Count; $index++) {
        if ($summaryRoles[$index] -ne $expectedRoles[$index]) {
            return $false
        }
    }
    return $true
}

function New-RunSummary {
    param(
        [object]$Plan,
        [string]$PackSummaryPath
    )

    $packSummary = $null
    if (-not [string]::IsNullOrWhiteSpace($PackSummaryPath) -and (Test-Path -LiteralPath $PackSummaryPath)) {
        $packSummary = Get-Content -LiteralPath $PackSummaryPath -Raw | ConvertFrom-Json
    }

    $fixedFour = Test-FixedFourDeviceSummary -Summary $packSummary
    $packPassed = ($null -ne $packSummary) -and
        ([string]$packSummary.status -eq "pass") -and
        ([int]$packSummary.total_states -gt 0) -and
        ([int]$packSummary.pass_count -eq [int]$packSummary.total_states) -and
        ([int]$packSummary.fail_count -eq 0)
    $acceptanceEvidence = [bool]($RealRun -and -not $PlanOnly -and -not [bool]$WhatIfPreference -and $fixedFour -and $packPassed)

    return [pscustomobject]@{
        run_id = $Plan.run_id
        output_root = $Plan.output_root
        run_dir = $Plan.run_dir
        state_pack_summary_path = $PackSummaryPath
        status = $(if ($acceptanceEvidence) { "pass" } else { "non_acceptance" })
        real_run = [bool]($RealRun -and -not $PlanOnly -and -not [bool]$WhatIfPreference)
        fixed_four_matrix_output = [bool]$fixedFour
        state_pack_passed = [bool]$packPassed
        acceptance_evidence = [bool]$acceptanceEvidence
        non_acceptance_evidence = -not [bool]$acceptanceEvidence
        acceptance_label_allowed = [bool]$acceptanceEvidence
        acceptance_criteria = New-AcceptanceCriteria
        validation_commands = @(New-ValidationCommands -ArtifactPath (Join-Path $Plan.run_dir "headless_lane_summary.json") -Validates "headless_lane_summary.json")
        pack_status = $(if ($null -ne $packSummary) { [string]$packSummary.status } else { $null })
        pack_total_states = $(if ($null -ne $packSummary) { [int]$packSummary.total_states } else { 0 })
        pack_pass_count = $(if ($null -ne $packSummary) { [int]$packSummary.pass_count } else { 0 })
        pack_fail_count = $(if ($null -ne $packSummary) { [int]$packSummary.fail_count } else { 0 })
    }
}

$runId = New-LaneRunId
$runDir = Join-Path $repoRoot (Join-Path $OutputRoot $runId)
$isDryRun = [bool]$WhatIfPreference
$plan = New-PlanObject -RunId $runId -RunDir $runDir -DryRun:$isDryRun
$planPath = Write-JsonFile -Value $plan -Path (Join-Path $runDir "headless_lane_plan.json")

Write-Host ("Plan: {0}" -f $planPath)
Write-Host "Acceptance criteria:"
foreach ($criterion in (New-AcceptanceCriteria)) {
    Write-Host ("- {0}" -f $criterion)
}

if ($PlanOnly -or $isDryRun) {
    Write-Host "Non-acceptance evidence: planning/dry-run artifact only."
    exit 0
}

if (-not $RealRun) {
    throw "Refusing to start emulators without -RealRun. Use -PlanOnly or -WhatIf for planning."
}

$partitionSizeMb = Get-ProfilePartitionSize -Profile $LaunchProfile
$matrixRunArgs = @(
    "-NoProfile",
    "-NonInteractive",
    "-ExecutionPolicy", "Bypass",
    "-File", $matrixScript,
    "-Roles", "all",
    "-Mode", "read_only",
    "-Headless",
    "-NoBootAnim"
)
if ($partitionSizeMb -gt 0) {
    $matrixRunArgs += @("-PartitionSizeMb", [string]$partitionSizeMb)
}

$packRunArgs = @(
    "-NoProfile",
    "-NonInteractive",
    "-ExecutionPolicy", "Bypass",
    "-File", $packScript,
    "-OutputRoot", $OutputRoot,
    "-MaxParallelDevices", [string]$MaxParallelDevices,
    "-HostInferenceUrl", $HostInferenceUrl,
    "-HostInferenceModel", $HostInferenceModel
)
if ($SkipBuild) {
    $packRunArgs += "-SkipBuild"
}
if ($SkipInstall) {
    $packRunArgs += "-SkipInstall"
}
if ($SkipHostStates) {
    $packRunArgs += "-SkipHostStates"
}

if ($PSCmdlet.ShouldProcess("fixed four-emulator Android matrix", "Start headless emulators and build state pack")) {
    & powershell @matrixRunArgs
    if ($LASTEXITCODE -ne 0) {
        throw "start_senku_emulator_matrix.ps1 failed with exit code $LASTEXITCODE"
    }

    if ($BootWaitSeconds -gt 0) {
        Write-Host ("Waiting {0} second(s) before state-pack replay." -f $BootWaitSeconds)
        Start-Sleep -Seconds $BootWaitSeconds
    }

    & powershell @packRunArgs
    if ($LASTEXITCODE -ne 0) {
        throw "build_android_ui_state_pack_parallel.ps1 failed with exit code $LASTEXITCODE"
    }
}

$summaryRoot = Join-Path $repoRoot $OutputRoot
$latestSummary = Get-ChildItem -LiteralPath $summaryRoot -Recurse -Filter "summary.json" -File |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if ($null -eq $latestSummary) {
    throw "Real lane did not produce a state-pack summary.json under $summaryRoot"
}

$runSummary = New-RunSummary -Plan $plan -PackSummaryPath $latestSummary.FullName
$summaryPath = Write-JsonFile -Value $runSummary -Path (Join-Path $runDir "headless_lane_summary.json")
Write-Host ("Summary: {0}" -f $summaryPath)
if (-not $runSummary.acceptance_evidence) {
    throw "Real lane output did not satisfy fixed four-emulator acceptance criteria."
}
