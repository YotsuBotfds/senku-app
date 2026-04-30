param(
    [string]$OutputRoot = "artifacts/ui_state_pack",
    [switch]$SkipBuild,
    [switch]$SkipInstall,
    [switch]$SkipHostStates,
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [int]$MaxParallelDevices = 4,
    [string[]]$RoleFilter = @(),
    [switch]$PlanOnly,
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
$buildScript = Join-Path $PSScriptRoot "build_android_ui_state_pack.ps1"
$gradlew = Join-Path (Join-Path $repoRoot "android-app") "gradlew.bat"
if (-not (Test-Path -LiteralPath $buildScript)) {
    throw "build_android_ui_state_pack.ps1 not found at $buildScript"
}
if (-not (Test-Path -LiteralPath $gradlew)) {
    throw "gradlew.bat not found at $gradlew"
}

$runId = Get-Date -Format "yyyyMMdd_HHmmss"
$allRoles = @("phone_portrait", "phone_landscape", "tablet_portrait", "tablet_landscape")
$goalMockFamilies = @("home", "search", "thread", "guide", "answer")
$goalMockPostures = @("phone-portrait", "phone-landscape", "tablet-portrait", "tablet-landscape")
$goalMockExpectedNames = @(
    foreach ($family in $goalMockFamilies) {
        foreach ($posture in $goalMockPostures) {
            "{0}-{1}.png" -f $family, $posture
        }
    }
    "emergency-phone-portrait.png"
    "emergency-tablet-portrait.png"
) | Sort-Object
$roleDeviceMatrix = @(
    [pscustomobject]@{ role = "phone_portrait"; device = "emulator-5556"; orientation = "portrait"; avd = "Senku_Large_4"; dimensions = "1080x2400" },
    [pscustomobject]@{ role = "phone_landscape"; device = "emulator-5560"; orientation = "landscape"; avd = "Senku_Large_3"; dimensions = "2400x1080" },
    [pscustomobject]@{ role = "tablet_portrait"; device = "emulator-5554"; orientation = "portrait"; avd = "Senku_Tablet_2"; dimensions = "1600x2560" },
    [pscustomobject]@{ role = "tablet_landscape"; device = "emulator-5558"; orientation = "landscape"; avd = "Senku_Tablet"; dimensions = "2560x1600" }
)
$roleInfoByRole = @{}
foreach ($entry in $roleDeviceMatrix) {
    $roleInfoByRole[$entry.role] = $entry
}
$roles = @($allRoles)
if ($RoleFilter.Count -gt 0) {
    $requestedRoles = New-Object System.Collections.Generic.List[string]
    foreach ($entry in $RoleFilter) {
        foreach ($part in ([string]$entry -split ",")) {
            $trimmed = $part.Trim()
            if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
                $requestedRoles.Add($trimmed)
            }
        }
    }
    $unknownRoles = @($requestedRoles | Where-Object { $allRoles -notcontains $_ })
    if ($unknownRoles.Count -gt 0) {
        throw ("RoleFilter contains unknown role(s): {0}" -f (($unknownRoles | Sort-Object -Unique) -join ", "))
    }
    $roles = @($allRoles | Where-Object { $requestedRoles -contains $_ })
    if ($roles.Count -eq 0) {
        throw "RoleFilter did not match any device roles."
    }
}
$active = New-Object System.Collections.Generic.List[object]
$sliceFailures = New-Object System.Collections.Generic.List[object]
$logsDir = Join-Path $repoRoot (Join-Path (Join-Path $OutputRoot $runId) "parallel_logs")

function New-RoleInvokeLine {
    param([string]$Role)

    return '& "{0}" -OutputRoot "{1}" -RunId "{2}" -RoleFilter "{3}" -SkipFinalize -SkipBuild{4} -HostInferenceUrl "{5}" -HostInferenceModel "{6}"{7} -NormalizeFilteredReview {8}' -f `
        $buildScript,
        $OutputRoot,
        $runId,
        $Role,
        $(if ($SkipInstall) { ' -SkipInstall' } else { '' }),
        $HostInferenceUrl,
        $HostInferenceModel,
        $(if ($SkipHostStates) { ' -SkipHostStates' } else { '' }),
        $(if ($NormalizeFilteredReview) { '$true' } else { '$false' })
}

if ($PlanOnly) {
    $runDir = Join-Path $repoRoot (Join-Path $OutputRoot $runId)
    New-Item -ItemType Directory -Force -Path $runDir | Out-Null
    $planPath = Join-Path $runDir "plan.json"
    $plan = [pscustomobject]@{
        run_id = $runId
        output_root = $OutputRoot
        preflight_only = $true
        acceptance_evidence = $false
        non_acceptance_evidence = $true
        will_build = $false
        will_install = $false
        will_start_role_jobs = $false
        will_finalize = $false
        selected_roles = @($roles)
        devices = @($roles | ForEach-Object { $roleInfoByRole[$_] })
        skip_build = [bool]$SkipBuild
        skip_install = [bool]$SkipInstall
        skip_host_states = [bool]$SkipHostStates
        host_inference_url = $HostInferenceUrl
        host_inference_model = $HostInferenceModel
        max_parallel_devices = [int]$MaxParallelDevices
        effective_max_parallel_devices = [Math]::Max(1, $MaxParallelDevices)
        plan_only = $true
        goal_pack = [pscustomobject]@{
            canonical_directory = "mocks"
            expected_png_count = [int]$goalMockExpectedNames.Count
            expected_png_names = @($goalMockExpectedNames)
            validator = "scripts/validate_android_mock_goal_pack.py"
            excludes_debug_intermediate_screenshots = $true
            deterministic_frame_exporter = "scripts/export_android_goal_mock_frame.ps1"
            deterministic_status_time = "4:21"
            deterministic_status_right = "OFFLINE"
            battery_style = "outline"
            target_dimensions = "match artifacts/mocks"
            metadata = "goal_mock_export_metadata.json"
            normalized_tablet_review_directory = "normalized_tablet_review"
            normalized_tablet_review_metadata = "normalized_tablet_review/metadata.json"
            normalized_tablet_review_source = "canonical framed mocks"
            normalize_filtered_review = [bool]$NormalizeFilteredReview
            raw_state_pack_screenshots_unchanged = $true
        }
        migration_checklist_intent = [pscustomobject]@{
            selected_roles = @($roles)
            host_flags = [pscustomobject]@{
                skip_host_states = [bool]$SkipHostStates
                will_request_host_states = -not [bool]$SkipHostStates
            }
            host_model_identity = [pscustomobject]@{
                host_inference_url = $HostInferenceUrl
                host_inference_model = $HostInferenceModel
            }
            skip_flags = [pscustomobject]@{
                skip_build = [bool]$SkipBuild
                skip_install = [bool]$SkipInstall
                skip_host_states = [bool]$SkipHostStates
            }
            max_parallel_devices = [int]$MaxParallelDevices
            effective_max_parallel_devices = [Math]::Max(1, $MaxParallelDevices)
            acceptance_evidence = $false
            non_acceptance_evidence = $true
            preflight_only = $true
        }
        launchers = @($roles | ForEach-Object {
            [pscustomobject]@{
                role = $_
                command = New-RoleInvokeLine -Role $_
            }
        })
    }
    $plan | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $planPath -Encoding UTF8
    Write-Host ("Plan: {0}" -f $planPath)
    exit 0
}

if (-not $SkipBuild) {
    Push-Location (Join-Path $repoRoot "android-app")
    try {
        & $gradlew :app:assembleDebug :app:assembleDebugAndroidTest
        if ($LASTEXITCODE -ne 0) {
            throw "Gradle build failed"
        }
    } finally {
        Pop-Location
    }
}

New-Item -ItemType Directory -Force -Path $logsDir | Out-Null

function Start-RoleProcess {
    param([string]$Role)

    $logPath = Join-Path $logsDir ($Role + ".out.log")
    $errPath = Join-Path $logsDir ($Role + ".err.log")
    $exitCodePath = Join-Path $logsDir ($Role + ".exitcode.txt")
    $launcherPath = Join-Path $logsDir ($Role + ".launcher.ps1")
    $invokeLine = New-RoleInvokeLine -Role $Role
    $launcherLines = @(
        '$ErrorActionPreference = ''Stop''',
        $invokeLine,
        '$code = $LASTEXITCODE',
        ('Set-Content -LiteralPath "{0}" -Value $code -Encoding ASCII' -f $exitCodePath),
        'exit $code'
    )
    Set-Content -LiteralPath $launcherPath -Value $launcherLines -Encoding UTF8

    $argList = @(
        "-NoProfile",
        "-NonInteractive",
        "-ExecutionPolicy", "Bypass",
        "-File", $launcherPath
    )

    $process = Start-Process -FilePath "powershell" -ArgumentList $argList -RedirectStandardOutput $logPath -RedirectStandardError $errPath -PassThru -WindowStyle Hidden
    return [pscustomobject]@{
        role = $Role
        process = $process
        log_path = $logPath
        err_path = $errPath
        exit_code_path = $exitCodePath
    }
}

function Get-RoleProcessExitCode {
    param([object]$Entry)

    $Entry.process.WaitForExit()
    $Entry.process.Refresh()

    if (Test-Path -LiteralPath $Entry.exit_code_path) {
        $rawExitCode = Get-Content -LiteralPath $Entry.exit_code_path -Raw
        $parsedExitCode = 0
        if ([int]::TryParse($rawExitCode.Trim(), [ref]$parsedExitCode)) {
            return $parsedExitCode
        }
    }

    return [int]$Entry.process.ExitCode
}

function Get-RoleFailureDetails {
    param(
        [object]$Entry,
        [int]$ExitCode
    )

    $tail = ""
    if (Test-Path -LiteralPath $Entry.log_path) {
        $tail = (Get-Content -LiteralPath $Entry.log_path -Tail 80 | Out-String)
    }
    if (Test-Path -LiteralPath $Entry.err_path) {
        $tail += (Get-Content -LiteralPath $Entry.err_path -Tail 80 | Out-String)
    }

    return [pscustomobject]@{
        role = $Entry.role
        exit_code = $exitCode
        details = $tail.Trim()
        log_path = $Entry.log_path
        err_path = $Entry.err_path
    }
}

function Complete-FinishedRoleProcesses {
    param([object[]]$FinishedEntries)

    foreach ($entry in @($FinishedEntries)) {
        $exitCode = Get-RoleProcessExitCode -Entry $entry
        $null = $active.Remove($entry)
        if ($exitCode -ne 0) {
            $sliceFailures.Add((Get-RoleFailureDetails -Entry $entry -ExitCode $exitCode))
        }
    }
}

foreach ($role in $roles) {
    while ($active.Count -ge [Math]::Max(1, $MaxParallelDevices)) {
        $finished = @($active | Where-Object { $_.process.HasExited })
        if ($finished.Count -eq 0) {
            Start-Sleep -Seconds 2
            continue
        }
        Complete-FinishedRoleProcesses -FinishedEntries $finished
    }
    $active.Add((Start-RoleProcess -Role $role))
}

while ($active.Count -gt 0) {
    $finished = @($active | Where-Object { $_.process.HasExited })
    if ($finished.Count -eq 0) {
        Start-Sleep -Seconds 2
        continue
    }
    Complete-FinishedRoleProcesses -FinishedEntries $finished
}

$finalizeArgs = @(
    "-NoProfile",
    "-NonInteractive",
    "-ExecutionPolicy", "Bypass",
    "-File", $buildScript,
    "-OutputRoot", $OutputRoot,
    "-RunId", $runId,
    "-FinalizeOnly",
    "-SkipBuild",
    "-HostInferenceUrl", $HostInferenceUrl,
    "-HostInferenceModel", $HostInferenceModel
)
if ($SkipHostStates) {
    $finalizeArgs += "-SkipHostStates"
}
& powershell @finalizeArgs
$finalizeExitCode = $LASTEXITCODE
$summaryPath = Join-Path $repoRoot (Join-Path (Join-Path $OutputRoot $runId) "summary.json")

if ($sliceFailures.Count -gt 0) {
    foreach ($failure in $sliceFailures) {
        $message = "Role slice failed on {0} with exit code {1}." -f $failure.role, $failure.exit_code
        if (-not [string]::IsNullOrWhiteSpace([string]$failure.details)) {
            $message = "{0}{1}{2}" -f $message, [Environment]::NewLine, $failure.details
        }
        [Console]::Error.WriteLine($message)
        Write-Host ("Logs: {0} | {1}" -f $failure.log_path, $failure.err_path)
    }
}

if ($finalizeExitCode -ne 0) {
    exit $finalizeExitCode
}
if (-not (Test-Path -LiteralPath $summaryPath)) {
    [Console]::Error.WriteLine("Finalization did not produce summary.json at $summaryPath")
    exit 1
}
Write-Host ("Summary: {0}" -f $summaryPath)
if ($sliceFailures.Count -gt 0) {
    exit 1
}

exit 0
