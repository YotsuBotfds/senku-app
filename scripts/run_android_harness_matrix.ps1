param(
    [Parameter(Mandatory = $true)]
    [string]$RunFile,
    [string]$OutputDir = "artifacts\\bench\\android_harness_matrix",
    [ValidateSet("preserve", "local", "host")]
    [string]$InferenceMode = "preserve",
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [string]$PushPackDir = "",
    [string[]]$Emulators = @("emulator-5554", "emulator-5556", "emulator-5558", "emulator-5560"),
    [int]$MaxParallel = 3,
    [ValidateRange(1, 2147483647)]
    [int]$JobTimeoutSeconds = 3600,
    [switch]$DefaultWarmStart,
    [switch]$SkipPackPushIfCurrent,
    [switch]$ForcePackPush,
    [switch]$DefaultPromptAsk,
    [switch]$DefaultPromptWaitForCompletion,
    [switch]$DefaultSkipSourceProbe,
    [switch]$StopOnError,
    [switch]$PlanOnly,
    [int]$PromptWaitSeconds = 5,
    [int]$PromptMaxWaitSeconds = 180,
    [int]$PromptPollSeconds = 5,
    [int]$FollowUpInitialMaxWaitSeconds = 260,
    [int]$FollowUpMaxWaitSeconds = 180,
    [int]$FollowUpPollSeconds = 5
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$promptLoggedScript = Join-Path $PSScriptRoot "run_android_prompt_logged.ps1"
$detailLoggedScript = Join-Path $PSScriptRoot "run_android_detail_followup_logged.ps1"
$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"
if (-not (Test-Path $promptLoggedScript)) {
    throw "run_android_prompt_logged.ps1 not found at $promptLoggedScript"
}
if (-not (Test-Path $detailLoggedScript)) {
    throw "run_android_detail_followup_logged.ps1 not found at $detailLoggedScript"
}
if (-not (Test-Path -LiteralPath $commonHarnessModule)) {
    throw "android_harness_common.psm1 not found at $commonHarnessModule"
}
Import-Module $commonHarnessModule -Force -DisableNameChecking
$Emulators = @(Resolve-AndroidHarnessDeviceList -Devices $Emulators)
if ($Emulators.Count -eq 0) {
    throw "No valid emulators provided."
}

function Resolve-TargetPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }

    return (Join-Path $repoRoot $Path)
}

function Safe-Text {
    param([string]$Text)

    if ($null -eq $Text) {
        return ""
    }

    return [string]$Text
}

function New-Slug {
    param([string]$Text)

    $slug = (Safe-Text -Text $Text).ToLowerInvariant() -replace "[^a-z0-9]+", "_"
    $slug = $slug.Trim("_")
    if ([string]::IsNullOrWhiteSpace($slug)) {
        return "run"
    }
    return $slug
}

function Sanitize-RunLabel {
    param([string]$Text)

    $label = (Safe-Text -Text $Text) -replace "[^A-Za-z0-9_-]+", "_"
    $label = $label.Trim("_")
    if ([string]::IsNullOrWhiteSpace($label)) {
        return "run"
    }
    return $label
}

function Load-Runs {
    param([string]$Path)

    $resolvedPath = Resolve-TargetPath -Path $Path
    if (-not (Test-Path $resolvedPath)) {
        throw "Run file not found: $resolvedPath"
    }

    $raw = Get-Content -Path $resolvedPath -Raw
    if ([string]::IsNullOrWhiteSpace($raw)) {
        return @()
    }

    $trimmed = $raw.Trim()
    $rawLines = @($raw -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    $looksLikeSingleJsonObject = ($trimmed.StartsWith("{") -and $rawLines.Count -eq 1)
    if ($trimmed.StartsWith("[") -or $looksLikeSingleJsonObject) {
        $parsed = $trimmed | ConvertFrom-Json
        if ($parsed -is [System.Collections.IEnumerable] -and -not ($parsed -is [string])) {
            return @($parsed)
        }
        return @($parsed)
    }

    $runs = @()
    foreach ($line in Get-Content -Path $resolvedPath) {
        $safeLine = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($safeLine) -or $safeLine.StartsWith("#")) {
            continue
        }
        $runs += ($safeLine | ConvertFrom-Json)
    }
    return $runs
}

function Read-RunJson {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return $null
    }

    try {
        return (Get-Content -Path $Path -Raw | ConvertFrom-Json)
    } catch {
        return $null
    }
}

function Get-ObjectStringProperty {
    param(
        $Object,
        [string]$PropertyName
    )

    if (-not $Object) {
        return $null
    }
    if (-not ($Object.PSObject.Properties.Name -contains $PropertyName)) {
        return $null
    }

    $value = $Object.$PropertyName
    if ($null -eq $value -or [string]::IsNullOrWhiteSpace([string]$value)) {
        return $null
    }

    return [string]$value
}

function Get-HostAdbPlatformToolsVersion {
    param(
        $Manifest,
        [string]$OutputText
    )

    $manifestVersion = Get-ObjectStringProperty -Object $Manifest -PropertyName "host_adb_platform_tools_version"
    if (-not [string]::IsNullOrWhiteSpace($manifestVersion)) {
        return $manifestVersion
    }

    foreach ($line in @($OutputText -split "`r?`n")) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or -not $trimmed.Contains("host_adb_platform_tools_version")) {
            continue
        }

        try {
            $parsed = $trimmed | ConvertFrom-Json
            $outputVersion = Get-ObjectStringProperty -Object $parsed -PropertyName "host_adb_platform_tools_version"
            if (-not [string]::IsNullOrWhiteSpace($outputVersion)) {
                return $outputVersion
            }
        } catch {
        }
    }

    return $null
}

function Get-FirstNonEmptyHostAdbPlatformToolsVersion {
    param([object[]]$Items)

    foreach ($item in @($Items)) {
        $version = Get-ObjectStringProperty -Object $item -PropertyName "host_adb_platform_tools_version"
        if (-not [string]::IsNullOrWhiteSpace($version)) {
            return $version
        }
    }

    return $null
}

function Test-HasProperty {
    param(
        $Object,
        [string]$PropertyName
    )

    return ($Object.PSObject.Properties.Name -contains $PropertyName)
}

function Get-RunStringOrDefault {
    param(
        $Run,
        [string]$PropertyName,
        [string]$DefaultValue = ""
    )

    if (-not (Test-HasProperty -Object $Run -PropertyName $PropertyName)) {
        return $DefaultValue
    }

    $value = $Run.$PropertyName
    if ($null -eq $value -or [string]::IsNullOrWhiteSpace([string]$value)) {
        return $DefaultValue
    }

    return [string]$value
}

function Get-RunIntOrDefault {
    param(
        $Run,
        [string]$PropertyName,
        [int]$DefaultValue
    )

    if (-not (Test-HasProperty -Object $Run -PropertyName $PropertyName)) {
        return $DefaultValue
    }

    $value = $Run.$PropertyName
    if ($null -eq $value -or [string]::IsNullOrWhiteSpace([string]$value)) {
        return $DefaultValue
    }

    return [int]$value
}

function Get-RunBoolOrDefault {
    param(
        $Run,
        [string]$PropertyName,
        [bool]$DefaultValue
    )

    if (-not (Test-HasProperty -Object $Run -PropertyName $PropertyName)) {
        return $DefaultValue
    }

    $value = $Run.$PropertyName
    if ($null -eq $value) {
        return $DefaultValue
    }

    if ($value -is [bool]) {
        return $value
    }

    $stringValue = [string]$value
    if ([string]::IsNullOrWhiteSpace($stringValue)) {
        return $DefaultValue
    }

    return [System.Convert]::ToBoolean($stringValue)
}

function Get-RunMode {
    param($Run)

    $rawMode = Get-RunStringOrDefault -Run $Run -PropertyName "mode" -DefaultValue "detail_followup"
    $mode = $rawMode.ToLowerInvariant()
    switch ($mode) {
        "followup" { return "detail_followup" }
        "detail_followup" { return "detail_followup" }
        "prompt" { return "prompt" }
        default { throw "Unsupported mode '$rawMode'. Use 'prompt' or 'detail_followup'." }
    }
}

function Require-RunValue {
    param(
        $Run,
        [string]$PropertyName
    )

    $value = Get-RunStringOrDefault -Run $Run -PropertyName $PropertyName
    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Run is missing required property '$PropertyName'"
    }
    return $value
}

function Get-AssignedEmulator {
    param(
        $Run,
        [int]$Index
    )

    $explicit = Get-RunStringOrDefault -Run $Run -PropertyName "emulator"
    if (-not [string]::IsNullOrWhiteSpace($explicit)) {
        return $explicit
    }

    if ($Emulators.Count -gt 0) {
        return $Emulators[$Index % $Emulators.Count]
    }

    return "emulator-5554"
}

function Resolve-MatrixDevicePosture {
    param([string]$Emulator)

    switch ($Emulator) {
        "emulator-5556" {
            return [pscustomobject]@{ role = "phone"; orientation = "portrait"; posture = "phone_portrait" }
        }
        "emulator-5560" {
            return [pscustomobject]@{ role = "phone"; orientation = "landscape"; posture = "phone_landscape" }
        }
        "emulator-5554" {
            return [pscustomobject]@{ role = "tablet"; orientation = "portrait"; posture = "tablet_portrait" }
        }
        "emulator-5558" {
            return [pscustomobject]@{ role = "tablet"; orientation = "landscape"; posture = "tablet_landscape" }
        }
        default {
            return [pscustomobject]@{ role = "unknown"; orientation = "unknown"; posture = "unknown" }
        }
    }
}

function New-DefaultRunLabel {
    param(
        $Run,
        [string]$Mode,
        [string]$Emulator,
        [int]$Index
    )

    $emulatorTag = $Emulator -replace "[^a-zA-Z0-9]+", "_"
    if ($Mode -eq "prompt") {
        $query = Require-RunValue -Run $Run -PropertyName "query"
        return ("row{0:D3}_{1}_{2}_{3}" -f ($Index + 1), "prompt", $emulatorTag, (New-Slug -Text $query))
    }

    $initialQuery = Require-RunValue -Run $Run -PropertyName "initial_query"
    $followUpQuery = Require-RunValue -Run $Run -PropertyName "follow_up_query"
    return ("row{0:D3}_{1}_{2}_{3}" -f ($Index + 1), "followup", $emulatorTag, (New-Slug -Text ($initialQuery + "_" + $followUpQuery)))
}

function New-RunnerSpec {
    param(
        $Run,
        [int]$Index,
        [string]$ResolvedOutputDir
    )

    $mode = Get-RunMode -Run $Run
    $emulator = Get-AssignedEmulator -Run $Run -Index $Index
    $posture = Resolve-MatrixDevicePosture -Emulator $emulator
    $requestedLabel = Get-RunStringOrDefault -Run $Run -PropertyName "run_label"
    $warmStart = Get-RunBoolOrDefault -Run $Run -PropertyName "warm_start" -DefaultValue ([bool]$DefaultWarmStart)
    $runPushPackDir = Get-RunStringOrDefault -Run $Run -PropertyName "push_pack_dir" -DefaultValue $PushPackDir
    $runLabel = if ([string]::IsNullOrWhiteSpace($requestedLabel)) {
        New-DefaultRunLabel -Run $Run -Mode $mode -Emulator $emulator -Index $Index
    } else {
        Sanitize-RunLabel -Text $requestedLabel
    }

    if ($mode -eq "prompt") {
        $query = Require-RunValue -Run $Run -PropertyName "query"
        $ask = Get-RunBoolOrDefault -Run $Run -PropertyName "ask" -DefaultValue ([bool]$DefaultPromptAsk)
        $waitForCompletion = Get-RunBoolOrDefault -Run $Run -PropertyName "wait_for_completion" -DefaultValue ([bool]$DefaultPromptWaitForCompletion)
        $expectedDetailTitle = Get-RunStringOrDefault -Run $Run -PropertyName "expected_detail_title"

        $args = [ordered]@{
            Emulator = $emulator
            Query = $query
            InferenceMode = $InferenceMode
            HostInferenceUrl = $HostInferenceUrl
            HostInferenceModel = $HostInferenceModel
            OutputDir = $ResolvedOutputDir
            RunLabel = $runLabel
            WaitSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "wait_seconds" -DefaultValue $PromptWaitSeconds
            MaxWaitSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "max_wait_seconds" -DefaultValue $PromptMaxWaitSeconds
            PollSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "poll_seconds" -DefaultValue $PromptPollSeconds
        }
        if ($ask) {
            $args.Ask = $true
        }
        if ($waitForCompletion) {
            $args.WaitForCompletion = $true
        }
        if ($warmStart) {
            $args.WarmStart = $true
        }
        if (-not [string]::IsNullOrWhiteSpace($runPushPackDir)) {
            $args.PushPackDir = $runPushPackDir
        }
        if ($SkipPackPushIfCurrent) {
            $args.SkipPackPushIfCurrent = $true
        }
        if ($ForcePackPush) {
            $args.ForcePackPush = $true
        }
        if ([string]::IsNullOrWhiteSpace($expectedDetailTitle) -and $ask -and $waitForCompletion) {
            $expectedDetailTitle = $query
        }
        if (-not [string]::IsNullOrWhiteSpace($expectedDetailTitle)) {
            $args.ExpectedDetailTitle = $expectedDetailTitle
        }

        return [pscustomobject]@{
            mode = $mode
            runner_path = $promptLoggedScript
            run_label = $runLabel
            device_role = $posture.role
            orientation = $posture.orientation
            posture = $posture.posture
            manifest_path = Join-Path $ResolvedOutputDir ($runLabel + ".json")
            logcat_path = Join-Path $ResolvedOutputDir ($runLabel + ".logcat.txt")
            args = $args
        }
    }

    $args = [ordered]@{
        Emulator = $emulator
        InitialQuery = Require-RunValue -Run $Run -PropertyName "initial_query"
        FollowUpQuery = Require-RunValue -Run $Run -PropertyName "follow_up_query"
        InferenceMode = $InferenceMode
        HostInferenceUrl = $HostInferenceUrl
        HostInferenceModel = $HostInferenceModel
        OutputDir = $ResolvedOutputDir
        RunLabel = $runLabel
        InitialMaxWaitSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "initial_max_wait_seconds" -DefaultValue $FollowUpInitialMaxWaitSeconds
        FollowUpMaxWaitSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "follow_up_max_wait_seconds" -DefaultValue $FollowUpMaxWaitSeconds
        PollSeconds = Get-RunIntOrDefault -Run $Run -PropertyName "poll_seconds" -DefaultValue $FollowUpPollSeconds
    }
    if (Get-RunBoolOrDefault -Run $Run -PropertyName "skip_source_probe" -DefaultValue ([bool]$DefaultSkipSourceProbe)) {
        $args.SkipSourceProbe = $true
    }
    if ($warmStart) {
        $args.WarmStart = $true
    }
    if (-not [string]::IsNullOrWhiteSpace($runPushPackDir)) {
        $args.PushPackDir = $runPushPackDir
    }
    if ($SkipPackPushIfCurrent) {
        $args.SkipPackPushIfCurrent = $true
    }
    if ($ForcePackPush) {
        $args.ForcePackPush = $true
    }

    return [pscustomobject]@{
        mode = $mode
        runner_path = $detailLoggedScript
        run_label = $runLabel
        device_role = $posture.role
        orientation = $posture.orientation
        posture = $posture.posture
        manifest_path = Join-Path $ResolvedOutputDir ($runLabel + ".json")
        logcat_path = Join-Path $ResolvedOutputDir ($runLabel + ".logcat.txt")
        args = $args
    }
}

function Convert-ArgumentsToCliArray {
    param([hashtable]$Arguments)

    $cliArgs = New-Object System.Collections.Generic.List[string]
    foreach ($key in $Arguments.Keys) {
        $value = $Arguments[$key]
        if ($value -is [bool]) {
            if ($value) {
                $cliArgs.Add("-$key")
            }
            continue
        }

        $cliArgs.Add("-$key")
        $cliArgs.Add([string]$value)
    }

    return @($cliArgs.ToArray())
}

function ConvertTo-QuotedCliToken {
    param([string]$Value)

    if ($null -eq $Value) {
        return '""'
    }

    $escaped = ([string]$Value).Replace('"', '\"')
    return '"' + $escaped + '"'
}

function ConvertTo-RunnerCommandLine {
    param($RunnerSpec)

    $tokens = New-Object System.Collections.Generic.List[string]
    $tokens.Add("&")
    $tokens.Add((ConvertTo-QuotedCliToken -Value $RunnerSpec.runner_path))
    foreach ($arg in (Convert-ArgumentsToCliArray -Arguments $RunnerSpec.args)) {
        if ([string]$arg -like "-*") {
            $tokens.Add([string]$arg)
        } else {
            $tokens.Add((ConvertTo-QuotedCliToken -Value ([string]$arg)))
        }
    }

    return ($tokens.ToArray() -join " ")
}

function New-MatrixPlan {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Runs,
        [Parameter(Mandatory = $true)]
        [string]$ResolvedOutputDir
    )

    $rows = @(
        for ($index = 0; $index -lt $Runs.Count; $index++) {
            $runnerSpec = New-RunnerSpec -Run $Runs[$index] -Index $index -ResolvedOutputDir $ResolvedOutputDir
            $runnerCommand = ConvertTo-RunnerCommandLine -RunnerSpec $runnerSpec
            [pscustomobject][ordered]@{
                row_index = $index
                row_number = $index + 1
                mode = $runnerSpec.mode
                run_label = $runnerSpec.run_label
                emulator = $runnerSpec.args.Emulator
                device_role = $runnerSpec.device_role
                orientation = $runnerSpec.orientation
                posture = $runnerSpec.posture
                runner_path = $runnerSpec.runner_path
                runner_command = $runnerCommand
                manifest_path = $runnerSpec.manifest_path
                logcat_path = $runnerSpec.logcat_path
                warm_start = [bool]$runnerSpec.args.WarmStart
                push_pack_dir = $(if ($runnerSpec.args.Contains("PushPackDir")) { [string]$runnerSpec.args.PushPackDir } else { $null })
                skip_pack_push_if_current = [bool]$runnerSpec.args.SkipPackPushIfCurrent
                force_pack_push = [bool]$runnerSpec.args.ForcePackPush
                will_push_pack = $runnerSpec.args.Contains("PushPackDir")
                runner_args = $runnerSpec.args
            }
        }
    )

    $emulatorGroups = @(
        foreach ($group in @($rows | Group-Object -Property emulator | Sort-Object -Property Name)) {
            $items = @($group.Group)
            [ordered]@{
                emulator = [string]$group.Name
                posture = $(if ($items.Count -gt 0) { $items[0].posture } else { "unknown" })
                device_role = $(if ($items.Count -gt 0) { $items[0].device_role } else { "unknown" })
                orientation = $(if ($items.Count -gt 0) { $items[0].orientation } else { "unknown" })
                row_count = $items.Count
                run_labels = @($items | ForEach-Object { $_.run_label })
            }
        }
    )

    $postureGroups = @(
        foreach ($group in @($rows | Group-Object -Property posture | Sort-Object -Property Name)) {
            $items = @($group.Group)
            [ordered]@{
                posture = [string]$group.Name
                row_count = $items.Count
                emulators = @($items | ForEach-Object { $_.emulator } | Sort-Object -Unique)
                run_labels = @($items | ForEach-Object { $_.run_label })
            }
        }
    )

    return [ordered]@{
        plan_kind = "android_harness_matrix"
        preflight_only = $true
        plan_only = $true
        non_acceptance_evidence = $true
        acceptance_evidence = $false
        will_start_jobs = $false
        will_touch_emulators = $false
        will_write_run_manifests = $false
        run_file = (Resolve-TargetPath -Path $RunFile)
        output_dir = $ResolvedOutputDir
        max_parallel = [int]$MaxParallel
        effective_max_parallel = [Math]::Max(1, $MaxParallel)
        job_timeout_seconds = [int]$JobTimeoutSeconds
        inference_mode = $InferenceMode
        host_inference_url = $HostInferenceUrl
        host_inference_model = $HostInferenceModel
        default_warm_start = [bool]$DefaultWarmStart
        push_pack_dir = $PushPackDir
        skip_pack_push_if_current = [bool]$SkipPackPushIfCurrent
        force_pack_push = [bool]$ForcePackPush
        row_count = $rows.Count
        selected_emulators = @($Emulators)
        rows = $rows
        emulator_groups = $emulatorGroups
        posture_groups = $postureGroups
        runner_commands = @($rows | ForEach-Object { $_.runner_command })
        migration_checklist_intent = [ordered]@{
            plan_kind = "android_harness_matrix"
            preflight_only = $true
            plan_only = $true
            non_acceptance_evidence = $true
            acceptance_evidence = $false
            will_start_jobs = $false
            will_touch_emulators = $false
            posture_groups = @($postureGroups | ForEach-Object { $_.posture })
        }
    }
}

function ConvertTo-MatrixPlanMarkdown {
    param([Parameter(Mandatory = $true)][object]$Plan)

    $lines = @(
        "# Android Harness Matrix Plan",
        "",
        "- plan_kind: $($Plan.plan_kind)",
        "- preflight_only: $($Plan.preflight_only)",
        "- non_acceptance_evidence: $($Plan.non_acceptance_evidence)",
        "- acceptance_evidence: $($Plan.acceptance_evidence)",
        "- will_start_jobs: $($Plan.will_start_jobs)",
        "- will_touch_emulators: $($Plan.will_touch_emulators)",
        "- row_count: $($Plan.row_count)",
        "- output_dir: $($Plan.output_dir)",
        "",
        "## Rows"
    )

    foreach ($row in $Plan.rows) {
        $lines += "- row=$($row.row_number) label=$($row.run_label) mode=$($row.mode) emulator=$($row.emulator) posture=$($row.posture) warm_start=$($row.warm_start) will_push_pack=$($row.will_push_pack)"
        $lines += "  command: $($row.runner_command)"
    }

    $lines += @("", "## Posture Groups")
    foreach ($group in $Plan.posture_groups) {
        $lines += "- posture=$($group.posture) rows=$($group.row_count) emulators=$(($group.emulators) -join ', ')"
    }

    if ($null -ne $Plan.validation_commands -and $Plan.validation_commands.Count -gt 0) {
        $lines += @("", "## Validation Commands")
        foreach ($command in $Plan.validation_commands) {
            $lines += "- $($command.name): $($command.command)"
            $lines += "  - validates: $($command.validates)"
            $lines += "  - will_start_jobs: $($command.will_start_jobs)"
            $lines += "  - will_touch_emulators: $($command.will_touch_emulators)"
        }
    }

    return ($lines -join [Environment]::NewLine) + [Environment]::NewLine
}

function Start-HarnessJob {
    param($RunnerSpec)

    return Start-Job -ScriptBlock {
        param($WorkingDirectory, $ScriptPath, $ScriptCliArgs)
        Set-Location $WorkingDirectory
        $ErrorActionPreference = "Continue"
        try {
            & powershell -ExecutionPolicy Bypass -File $ScriptPath @ScriptCliArgs 2>&1 | ForEach-Object { $_ }
        } catch {
            Write-Output ("SCRIPT_EXCEPTION: " + $_.Exception.Message)
        }
    } -ArgumentList $repoRoot, $RunnerSpec.runner_path, (Convert-ArgumentsToCliArray -Arguments $RunnerSpec.args)
}

function Get-ResultTitle {
    param($Manifest)

    if (-not $Manifest) {
        return $null
    }

    if ($Manifest.PSObject.Properties.Name -contains "final_detail_title") {
        return $Manifest.final_detail_title
    }

    if ($Manifest.PSObject.Properties.Name -contains "detail_title") {
        return $Manifest.detail_title
    }

    return $null
}

function Get-ResultSubtitle {
    param($Manifest)

    if (-not $Manifest) {
        return $null
    }

    if ($Manifest.PSObject.Properties.Name -contains "final_detail_subtitle") {
        return $Manifest.final_detail_subtitle
    }

    if ($Manifest.PSObject.Properties.Name -contains "detail_subtitle") {
        return $Manifest.detail_subtitle
    }

    return $null
}

function Convert-ToDateTimeOrNull {
    param($Value)

    if ($null -eq $Value) {
        return $null
    }

    if ($Value -is [datetime]) {
        return $Value
    }

    $text = [string]$Value
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $null
    }

    try {
        return [datetime]::Parse($text)
    } catch {
        return $null
    }
}

function Get-RunDurationSeconds {
    param(
        $Manifest,
        $FallbackStartedAt,
        $FallbackCompletedAt
    )

    $startedAt = Convert-ToDateTimeOrNull -Value $Manifest.started_at
    if (-not $startedAt) {
        $startedAt = Convert-ToDateTimeOrNull -Value $FallbackStartedAt
    }

    $completedAt = Convert-ToDateTimeOrNull -Value $Manifest.completed_at
    if (-not $completedAt) {
        $completedAt = Convert-ToDateTimeOrNull -Value $FallbackCompletedAt
    }

    if ($null -eq $startedAt -or $null -eq $completedAt) {
        return $null
    }
    if ($completedAt -lt $startedAt) {
        return $null
    }

    return [math]::Round(($completedAt - $startedAt).TotalSeconds, 2)
}

function Receive-CompletedJobs {
    param(
        [System.Collections.ArrayList]$ActiveJobs,
        [System.Collections.ArrayList]$Results
    )

    for ($index = $ActiveJobs.Count - 1; $index -ge 0; $index--) {
        $entry = $ActiveJobs[$index]
        $job = $entry.job
        if ($job.State -eq "Running" -or $job.State -eq "NotStarted") {
            continue
        }

        $failed = $job.State -ne "Completed"
        $outputText = ""
        $errorText = ""
        try {
            $received = Receive-Job -Job $job -Keep
            if ($received) {
                $outputText = ($received | Out-String).Trim()
            }
        } catch {
            $failed = $true
            $outputText = $_.ToString()
        }

        $reason = $job.ChildJobs | ForEach-Object {
            if ($_.JobStateInfo.Reason) {
                $_.JobStateInfo.Reason.ToString()
            }
        } | Select-Object -First 1
        $jobErrors = $job.ChildJobs | ForEach-Object {
            if ($_.Error.Count -gt 0) {
                ($_.Error | Out-String).Trim()
            }
        } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        if ($jobErrors.Count -gt 0) {
            $errorText = ($jobErrors -join [Environment]::NewLine)
        } elseif (-not [string]::IsNullOrWhiteSpace($reason)) {
            $errorText = $reason
        }

        $manifest = Read-RunJson -Path $entry.runner.manifest_path
        $resultCompletedAt = Get-Date
        $manifestError = $null
        if ($manifest -and ($manifest.PSObject.Properties.Name -contains "error_message")) {
            $manifestError = [string]$manifest.error_message
        }
        if (-not $manifest) {
            $failed = $true
        } elseif (-not [string]::IsNullOrWhiteSpace($manifestError)) {
            $failed = $true
        }

        $durationSeconds = Get-RunDurationSeconds -Manifest $manifest -FallbackStartedAt $entry.runner.started_at -FallbackCompletedAt $resultCompletedAt
        $hostAdbPlatformToolsVersion = Get-HostAdbPlatformToolsVersion -Manifest $manifest -OutputText $outputText

        $Results.Add([pscustomobject]@{
            mode = $entry.runner.mode
            emulator = $entry.runner.args.Emulator
            run_label = $entry.runner.run_label
            device_role = $entry.runner.device_role
            orientation = $entry.runner.orientation
            posture = $entry.runner.posture
            warm_start = [bool]$entry.runner.args.WarmStart
            push_pack_dir = $(if ($entry.runner.args.Contains("PushPackDir")) { [string]$entry.runner.args.PushPackDir } else { $null })
            push_pack_summary_path = $(if ($manifest -and ($manifest.PSObject.Properties.Name -contains "push_pack_summary_path")) { [string]$manifest.push_pack_summary_path } else { $null })
            push_pack_cache_hit = $(if ($manifest -and ($manifest.PSObject.Properties.Name -contains "push_pack_cache_hit")) { $manifest.push_pack_cache_hit } else { $null })
            push_pack_pushed = $(if ($manifest -and ($manifest.PSObject.Properties.Name -contains "push_pack_pushed")) { $manifest.push_pack_pushed } else { $null })
            push_pack_state_path = $(if ($manifest -and ($manifest.PSObject.Properties.Name -contains "push_pack_state_path")) { [string]$manifest.push_pack_state_path } else { $null })
            host_adb_platform_tools_version = $hostAdbPlatformToolsVersion
            started_at = $entry.runner.started_at
            completed_at = $resultCompletedAt.ToString("o")
            duration_seconds = $durationSeconds
            status = if ($failed) { "failed" } else { "completed" }
            job_state = $job.State
            manifest_path = $entry.runner.manifest_path
            logcat_path = $entry.runner.logcat_path
            final_title = Get-ResultTitle -Manifest $manifest
            final_subtitle = Get-ResultSubtitle -Manifest $manifest
            error = if (-not [string]::IsNullOrWhiteSpace($manifestError)) { $manifestError } else { $errorText }
            output = $outputText
        }) | Out-Null

        Remove-Job -Job $job -Force | Out-Null
        $ActiveJobs.RemoveAt($index)
    }
}

function Stop-TimedOutJobs {
    param(
        [System.Collections.ArrayList]$ActiveJobs,
        [System.Collections.ArrayList]$Results,
        [int]$TimeoutSeconds
    )

    $now = Get-Date
    for ($index = $ActiveJobs.Count - 1; $index -ge 0; $index--) {
        $entry = $ActiveJobs[$index]
        $job = $entry.job
        if ($job.State -ne "Running" -and $job.State -ne "NotStarted") {
            continue
        }

        $startedAt = Convert-ToDateTimeOrNull -Value $entry.runner.started_at
        if ($null -eq $startedAt) {
            continue
        }

        $elapsedSeconds = [math]::Round(($now - $startedAt).TotalSeconds, 2)
        if ($elapsedSeconds -lt $TimeoutSeconds) {
            continue
        }

        $outputText = ""
        $errorText = "Matrix job timed out after $TimeoutSeconds seconds while state was $($job.State)."
        try {
            Stop-Job -Job $job -ErrorAction SilentlyContinue
            Wait-Job -Job $job -Timeout 5 -ErrorAction SilentlyContinue | Out-Null
            $received = Receive-Job -Job $job -Keep -ErrorAction SilentlyContinue
            if ($received) {
                $outputText = ($received | Out-String).Trim()
            }
        } catch {
            $errorText = $errorText + " Stop diagnostics: " + $_.ToString()
        }

        $reason = $job.ChildJobs | ForEach-Object {
            if ($_.JobStateInfo.Reason) {
                $_.JobStateInfo.Reason.ToString()
            }
        } | Select-Object -First 1
        $jobErrors = $job.ChildJobs | ForEach-Object {
            if ($_.Error.Count -gt 0) {
                ($_.Error | Out-String).Trim()
            }
        } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        if ($jobErrors.Count -gt 0) {
            $errorText = $errorText + [Environment]::NewLine + ($jobErrors -join [Environment]::NewLine)
        } elseif (-not [string]::IsNullOrWhiteSpace($reason)) {
            $errorText = $errorText + [Environment]::NewLine + $reason
        }

        $manifest = Read-RunJson -Path $entry.runner.manifest_path
        $timedOutAt = Get-Date
        $durationSeconds = Get-RunDurationSeconds -Manifest $manifest -FallbackStartedAt $entry.runner.started_at -FallbackCompletedAt $timedOutAt
        if ($null -eq $durationSeconds) {
            $durationSeconds = $elapsedSeconds
        }
        $hostAdbPlatformToolsVersion = Get-HostAdbPlatformToolsVersion -Manifest $manifest -OutputText $outputText

        $Results.Add([pscustomobject]@{
            mode = $entry.runner.mode
            emulator = $entry.runner.args.Emulator
            run_label = $entry.runner.run_label
            device_role = $entry.runner.device_role
            orientation = $entry.runner.orientation
            posture = $entry.runner.posture
            warm_start = [bool]$entry.runner.args.WarmStart
            push_pack_dir = $(if ($entry.runner.args.Contains("PushPackDir")) { [string]$entry.runner.args.PushPackDir } else { $null })
            push_pack_summary_path = $(if ($manifest -and ($manifest.PSObject.Properties.Name -contains "push_pack_summary_path")) { [string]$manifest.push_pack_summary_path } else { $null })
            push_pack_cache_hit = $(if ($manifest -and ($manifest.PSObject.Properties.Name -contains "push_pack_cache_hit")) { $manifest.push_pack_cache_hit } else { $null })
            push_pack_pushed = $(if ($manifest -and ($manifest.PSObject.Properties.Name -contains "push_pack_pushed")) { $manifest.push_pack_pushed } else { $null })
            push_pack_state_path = $(if ($manifest -and ($manifest.PSObject.Properties.Name -contains "push_pack_state_path")) { [string]$manifest.push_pack_state_path } else { $null })
            host_adb_platform_tools_version = $hostAdbPlatformToolsVersion
            started_at = $entry.runner.started_at
            completed_at = $timedOutAt.ToString("o")
            duration_seconds = $durationSeconds
            status = "timed_out"
            job_state = $job.State
            job_id = $job.Id
            timeout_seconds = [int]$TimeoutSeconds
            elapsed_seconds = $elapsedSeconds
            timed_out_at = $timedOutAt.ToString("o")
            manifest_path = $entry.runner.manifest_path
            logcat_path = $entry.runner.logcat_path
            final_title = Get-ResultTitle -Manifest $manifest
            final_subtitle = Get-ResultSubtitle -Manifest $manifest
            error = $errorText
            output = $outputText
        }) | Out-Null

        Write-Warning ("Timed out [{0}] {1} on {2} after {3}s; stopped job {4}." -f $entry.runner.mode, $entry.runner.run_label, $entry.runner.args.Emulator, $TimeoutSeconds, $job.Id)
        Remove-Job -Job $job -Force -ErrorAction SilentlyContinue | Out-Null
        $ActiveJobs.RemoveAt($index)
    }
}

function New-MatrixSummary {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Results,
        [Parameter(Mandatory = $true)]
        [string]$SummaryJsonlPath,
        [Parameter(Mandatory = $true)]
        [string]$SummaryCsvPath
    )

    $completed = @($Results | Where-Object { $_.status -eq "completed" })
    $failed = @($Results | Where-Object { $_.status -ne "completed" })
    $timedOut = @($Results | Where-Object { $_.status -eq "timed_out" })
    $statusGroups = @()
    foreach ($statusName in @("completed", "failed")) {
        $source = if ($statusName -eq "completed") { $completed } else { $failed }
        $statusGroups += [ordered]@{
            status = $statusName
            count = $source.Count
            run_labels = @($source | ForEach-Object { $_.run_label })
        }
    }

    $durationValues = @(
        foreach ($result in @($Results)) {
            if (($result.PSObject.Properties.Name -contains "duration_seconds") -and ($null -ne $result.duration_seconds)) {
                [double]$result.duration_seconds
            }
        }
    )
    $durationStats = [ordered]@{
        count = $durationValues.Count
        min_seconds = $null
        max_seconds = $null
        avg_seconds = $null
        total_seconds = $null
    }
    if ($durationValues.Count -gt 0) {
        $durationStats.min_seconds = [math]::Round(($durationValues | Measure-Object -Minimum).Minimum, 2)
        $durationStats.max_seconds = [math]::Round(($durationValues | Measure-Object -Maximum).Maximum, 2)
        $durationStats.avg_seconds = [math]::Round(($durationValues | Measure-Object -Average).Average, 2)
        $durationStats.total_seconds = [math]::Round(($durationValues | Measure-Object -Sum).Sum, 2)
    }

    $emulatorGroups = @()
    foreach ($group in @($Results | Group-Object -Property emulator | Sort-Object -Property Name)) {
        $emulator = [string]$group.Name
        $emulatorResults = @($group.Group)
        $emulatorCompleted = @($emulatorResults | Where-Object { $_.status -eq "completed" })
        $emulatorFailed = @($emulatorResults | Where-Object { $_.status -ne "completed" })
        $emulatorStatusGroups = @()
        foreach ($statusName in @("completed", "failed")) {
            $source = if ($statusName -eq "completed") { $emulatorCompleted } else { $emulatorFailed }
            $emulatorStatusGroups += [ordered]@{
                status = $statusName
                count = $source.Count
                run_labels = @($source | ForEach-Object { $_.run_label })
            }
        }

        $emulatorGroups += [ordered]@{
            emulator = $emulator
            posture = $(if ($emulatorResults.Count -gt 0) { $emulatorResults[0].posture } else { "unknown" })
            device_role = $(if ($emulatorResults.Count -gt 0) { $emulatorResults[0].device_role } else { "unknown" })
            orientation = $(if ($emulatorResults.Count -gt 0) { $emulatorResults[0].orientation } else { "unknown" })
            total = $emulatorResults.Count
            completed = $emulatorCompleted.Count
            failed = $emulatorFailed.Count
            warm_start_count = @($emulatorResults | Where-Object { $_.warm_start }).Count
            duration_count = @($emulatorResults | Where-Object { $null -ne $_.duration_seconds }).Count
            push_pack_cache_hit_count = @($emulatorResults | Where-Object { $_.push_pack_cache_hit -eq $true }).Count
            push_pack_pushed_count = @($emulatorResults | Where-Object { $_.push_pack_pushed -eq $true }).Count
            status_groups = $emulatorStatusGroups
        }
    }

    $failedItems = @($failed | ForEach-Object {
        $artifactPaths = @()
        if (Test-Path -LiteralPath $_.manifest_path) {
            $artifactPaths += [ordered]@{
                kind = "manifest_json"
                path = $_.manifest_path
                posture = $_.posture
            }
        }
        if (Test-Path -LiteralPath $_.logcat_path) {
            $artifactPaths += [ordered]@{
                kind = "logcat"
                path = $_.logcat_path
                posture = $_.posture
            }
        }

        [ordered]@{
            run_label = $_.run_label
            mode = $_.mode
            emulator = $_.emulator
            posture = $_.posture
            warm_start = $_.warm_start
            push_pack_cache_hit = $_.push_pack_cache_hit
            push_pack_pushed = $_.push_pack_pushed
            timed_out = ($_.status -eq "timed_out")
            timeout_seconds = $(if ($_.PSObject.Properties.Name -contains "timeout_seconds") { $_.timeout_seconds } else { $null })
            elapsed_seconds = $(if ($_.PSObject.Properties.Name -contains "elapsed_seconds") { $_.elapsed_seconds } else { $null })
            job_id = $(if ($_.PSObject.Properties.Name -contains "job_id") { $_.job_id } else { $null })
            error = $_.error
            artifact_paths = $artifactPaths
        }
    })

    $artifactPaths = @(
        [ordered]@{ kind = "jsonl"; path = $SummaryJsonlPath }
        [ordered]@{ kind = "csv"; path = $SummaryCsvPath }
    )
    foreach ($result in @($Results)) {
        if (Test-Path -LiteralPath $result.manifest_path) {
            $artifactPaths += [ordered]@{
                kind = "manifest_json"
                run_label = $result.run_label
                emulator = $result.emulator
                posture = $result.posture
                path = $result.manifest_path
            }
        }
        if (Test-Path -LiteralPath $result.logcat_path) {
            $artifactPaths += [ordered]@{
                kind = "logcat"
                run_label = $result.run_label
                emulator = $result.emulator
                posture = $result.posture
                path = $result.logcat_path
            }
        }
    }

    return [ordered]@{
        total = $Results.Count
        completed = $completed.Count
        failed = $failed.Count
        timed_out = $timedOut.Count
        status_groups = $statusGroups
        emulator_groups = $emulatorGroups
        failed_items = $failedItems
        host_adb_platform_tools_version = Get-FirstNonEmptyHostAdbPlatformToolsVersion -Items $Results
        duration_seconds = $durationStats
        push_pack_cache_hit_count = @($Results | Where-Object { $_.push_pack_cache_hit -eq $true }).Count
        push_pack_pushed_count = @($Results | Where-Object { $_.push_pack_pushed -eq $true }).Count
        artifact_paths = $artifactPaths
        summary_jsonl_path = $SummaryJsonlPath
        summary_csv_path = $SummaryCsvPath
    }
}

function ConvertTo-MatrixSummaryMarkdown {
    param([Parameter(Mandatory = $true)][object]$Summary)

    $lines = @(
        "# Android Harness Matrix Summary",
        "",
        "- total: $($Summary.total)",
        "- completed: $($Summary.completed)",
        "- failed: $($Summary.failed)",
        "- timed_out: $($Summary.timed_out)",
        "- duration_samples: $($Summary.duration_seconds.count)",
        "- push_pack_cache_hit_count: $($Summary.push_pack_cache_hit_count)",
        "- push_pack_pushed_count: $($Summary.push_pack_pushed_count)",
        "- host_adb_platform_tools_version: $($Summary.host_adb_platform_tools_version)",
        "- summary_jsonl_path: $($Summary.summary_jsonl_path)",
        "- summary_csv_path: $($Summary.summary_csv_path)",
        "",
        "## Status Groups"
    )

    foreach ($group in $Summary.status_groups) {
        $runLabels = if ($group.run_labels.Count -gt 0) { ($group.run_labels -join ", ") } else { "-" }
        $lines += "- $($group.status): $($group.count) ($runLabels)"
    }

    $lines += @("", "## Emulator Groups")
    foreach ($group in $Summary.emulator_groups) {
        $lines += "- emulator=$($group.emulator) posture=$($group.posture) total=$($group.total) completed=$($group.completed) failed=$($group.failed) warm_start=$($group.warm_start_count) duration_count=$($group.duration_count) pack_cache_hits=$($group.push_pack_cache_hit_count) pack_pushes=$($group.push_pack_pushed_count)"
        foreach ($statusGroup in $group.status_groups) {
            $runLabels = if ($statusGroup.run_labels.Count -gt 0) { ($statusGroup.run_labels -join ", ") } else { "-" }
            $lines += "  - $($statusGroup.status): $($statusGroup.count) ($runLabels)"
        }
    }

    $lines += @("", "## Failed Items")
    if ($Summary.failed_items.Count -eq 0) {
        $lines += "- none"
    } else {
        foreach ($item in $Summary.failed_items) {
            $lines += "- run_label=$($item.run_label) mode=$($item.mode) emulator=$($item.emulator) posture=$($item.posture) warm_start=$($item.warm_start) pack_cache_hit=$($item.push_pack_cache_hit) pack_pushed=$($item.push_pack_pushed) timed_out=$($item.timed_out) timeout_seconds=$($item.timeout_seconds) elapsed_seconds=$($item.elapsed_seconds) job_id=$($item.job_id) error=$($item.error)"
            foreach ($path in $item.artifact_paths) {
                $lines += "  - $($path.kind): $($path.path)"
            }
        }
    }

    $lines += @("", "## Artifact Paths")
    foreach ($artifact in $Summary.artifact_paths) {
        $suffix = ""
        if ($artifact.PSObject.Properties.Name -contains "run_label") {
            $suffix = " run_label=$($artifact.run_label)"
        }
        if ($artifact.PSObject.Properties.Name -contains "emulator") {
            $suffix = "$suffix emulator=$($artifact.emulator)"
        }
        if ($artifact.PSObject.Properties.Name -contains "posture") {
            $suffix = "$suffix posture=$($artifact.posture)"
        }
        $lines += "- kind=$($artifact.kind)$suffix path=$($artifact.path)"
    }

    if ($Summary.duration_seconds.count -gt 0) {
        $lines += @(
            "",
            "- duration_seconds_min: $($Summary.duration_seconds.min_seconds)",
            "- duration_seconds_max: $($Summary.duration_seconds.max_seconds)",
            "- duration_seconds_avg: $($Summary.duration_seconds.avg_seconds)",
            "- duration_seconds_total: $($Summary.duration_seconds.total_seconds)"
        )
    } else {
        $lines += ""
        $lines += "- duration_seconds: unavailable"
    }

    return ($lines -join [Environment]::NewLine) + [Environment]::NewLine
}

$resolvedOutputDir = Resolve-TargetPath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$runs = Load-Runs -Path $RunFile
if ($runs.Count -eq 0) {
    throw "No runs found in $RunFile"
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

if ($PlanOnly) {
    $planJsonPath = Join-Path $resolvedOutputDir ("android_harness_matrix_plan_" + $timestamp + ".json")
    $planMarkdownPath = Join-Path $resolvedOutputDir ("android_harness_matrix_plan_" + $timestamp + ".md")
    $summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
    $summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"
    $plan = New-MatrixPlan -Runs $runs -ResolvedOutputDir $resolvedOutputDir
    $plan["plan_json_path"] = $planJsonPath
    $plan["plan_markdown_path"] = $planMarkdownPath
    $plan["summary_json_path"] = $summaryJsonPath
    $plan["summary_markdown_path"] = $summaryMarkdownPath
    $plan["validation_commands"] = @(
        [ordered]@{
            name = "validate_summary_json"
            command = ("& .\.venvs\senku-validate\Scripts\python.exe scripts\validate_android_migration_summary.py " + (ConvertTo-QuotedCliToken -Value $summaryJsonPath))
            validates = "summary.json"
            summary_json_path = $summaryJsonPath
            plan_only = $true
            will_start_jobs = $false
            will_touch_emulators = $false
            note = "PlanOnly validation only; does not start jobs or touch emulators."
        }
    )
    ($plan | ConvertTo-Json -Depth 12) | Set-Content -LiteralPath $planJsonPath -Encoding UTF8
    ($plan | ConvertTo-Json -Depth 12) | Set-Content -LiteralPath $summaryJsonPath -Encoding UTF8
    ConvertTo-MatrixPlanMarkdown -Plan $plan | Set-Content -LiteralPath $planMarkdownPath -Encoding UTF8
    ConvertTo-MatrixPlanMarkdown -Plan $plan | Set-Content -LiteralPath $summaryMarkdownPath -Encoding UTF8
    Write-Host ("Plan JSON written to " + $planJsonPath)
    Write-Host ("Plan Markdown written to " + $planMarkdownPath)
    Write-Host ("Summary JSON written to " + $summaryJsonPath)
    Write-Host ("Summary Markdown written to " + $summaryMarkdownPath)
    exit 0
}

$parallelLimit = [Math]::Max(1, $MaxParallel)
$activeJobs = New-Object System.Collections.ArrayList
$results = New-Object System.Collections.ArrayList

foreach ($runIndex in 0..($runs.Count - 1)) {
    $run = $runs[$runIndex]
    while ($activeJobs.Count -ge $parallelLimit) {
        Stop-TimedOutJobs -ActiveJobs $activeJobs -Results $results -TimeoutSeconds $JobTimeoutSeconds
        Receive-CompletedJobs -ActiveJobs $activeJobs -Results $results
        Start-Sleep -Milliseconds 500
    }

    $runnerSpec = New-RunnerSpec -Run $run -Index $runIndex -ResolvedOutputDir $resolvedOutputDir
    $runnerSpec.started_at = (Get-Date).ToString("o")
    $job = Start-HarnessJob -RunnerSpec $runnerSpec
    $activeJobs.Add([pscustomobject]@{
        job = $job
        runner = $runnerSpec
    }) | Out-Null
    Write-Host ("Started [{0}] {1} on {2}" -f $runnerSpec.mode, $runnerSpec.run_label, $runnerSpec.args.Emulator)
}

while ($activeJobs.Count -gt 0) {
    Stop-TimedOutJobs -ActiveJobs $activeJobs -Results $results -TimeoutSeconds $JobTimeoutSeconds
    Receive-CompletedJobs -ActiveJobs $activeJobs -Results $results
    if ($activeJobs.Count -gt 0) {
        Start-Sleep -Milliseconds 500
    }
}

$summaryJsonlPath = Join-Path $resolvedOutputDir ("android_harness_matrix_" + $timestamp + ".jsonl")
$summaryCsvPath = Join-Path $resolvedOutputDir ("android_harness_matrix_" + $timestamp + ".csv")
$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"

foreach ($result in $results) {
    ($result | ConvertTo-Json -Compress) | Add-Content -Path $summaryJsonlPath
}
$results | Export-Csv -Path $summaryCsvPath -NoTypeInformation -Encoding utf8

$summary = New-MatrixSummary -Results $results -SummaryJsonlPath $summaryJsonlPath -SummaryCsvPath $summaryCsvPath
($summary | ConvertTo-Json -Depth 10) | Set-Content -Path $summaryJsonPath -Encoding UTF8
ConvertTo-MatrixSummaryMarkdown -Summary $summary | Set-Content -Path $summaryMarkdownPath -Encoding UTF8

$failedResults = @($results | Where-Object { $_.status -ne "completed" })
Write-Host ""
Write-Host "Harness matrix summary:"
foreach ($result in $results) {
    Write-Host ("- [{0}] {1} ({2}) on {3}" -f $result.status, $result.run_label, $result.mode, $result.emulator)
}
Write-Host ("Summary JSONL written to " + $summaryJsonlPath)
Write-Host ("Summary CSV written to " + $summaryCsvPath)
Write-Host ("Summary JSON written to " + $summaryJsonPath)
Write-Host ("Summary Markdown written to " + $summaryMarkdownPath)

if ($failedResults.Count -gt 0 -and $StopOnError) {
    $labels = @($failedResults | ForEach-Object { $_.run_label })
    throw ("One or more matrix runs failed: " + ($labels -join ", "))
}
