param(
    [string]$OutputDir = "artifacts\bench\android_migration_preflight_bundle",
    [string]$PythonPath = ".\.venvs\senku-validate\Scripts\python.exe",
    [switch]$NoProbeTools
)

$ErrorActionPreference = "Stop"
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$startedAt = (Get-Date).ToUniversalTime()

function Resolve-BundlePath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path $repoRoot $Path))
}

function Convert-ToRepoRelativePath {
    param([string]$Path)

    $resolvedPath = [System.IO.Path]::GetFullPath($Path)
    $resolvedRoot = [System.IO.Path]::GetFullPath($repoRoot)
    if ($resolvedPath.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $resolvedPath.Substring($resolvedRoot.Length).TrimStart("\", "/") -replace "\\", "/"
    }
    return $resolvedPath
}

function ConvertTo-QuotedCliToken {
    param([string]$Value)

    if ($null -eq $Value) {
        return '""'
    }
    return '"' + ([string]$Value).Replace('"', '\"') + '"'
}

function Invoke-BundleCommand {
    param(
        [string]$Name,
        [string[]]$Command,
        [string]$OutputDirectory,
        [string]$SummaryPath = "",
        [string]$MarkdownPath = ""
    )

    New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
    $commandText = ($Command | ForEach-Object { ConvertTo-QuotedCliToken -Value $_ }) -join " "
    $commandStartedAt = (Get-Date).ToUniversalTime()
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $output = & $Command[0] @($Command[1..($Command.Count - 1)]) 2>&1
        $exitCode = $LASTEXITCODE
        if ($null -eq $exitCode) {
            $exitCode = 0
        }
    } catch {
        $output = @($_.ToString())
        $exitCode = 1
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $commandFinishedAt = (Get-Date).ToUniversalTime()
    $stdoutPath = Join-Path $OutputDirectory "stdout.txt"
    ($output | Out-String).TrimEnd() | Set-Content -LiteralPath $stdoutPath -Encoding UTF8

    return [ordered]@{
        name = $Name
        command = $commandText
        exit_code = [int]$exitCode
        status = $(if ($exitCode -eq 0) { "pass" } else { "fail" })
        summary_path = $(if ([string]::IsNullOrWhiteSpace($SummaryPath)) { $null } else { Convert-ToRepoRelativePath -Path $SummaryPath })
        markdown_path = $(if ([string]::IsNullOrWhiteSpace($MarkdownPath)) { $null } else { Convert-ToRepoRelativePath -Path $MarkdownPath })
        stdout_path = (Convert-ToRepoRelativePath -Path $stdoutPath)
        started_at_utc = $commandStartedAt.ToString("o")
        finished_at_utc = $commandFinishedAt.ToString("o")
    }
}

function Write-BundleMarkdown {
    param(
        [string]$Path,
        [object]$Summary
    )

    $lines = @(
        "# Android Migration Preflight Bundle",
        "",
        "- status: $($Summary.status)",
        "- metadata_only: $($Summary.metadata_only)",
        "- preflight_only: $($Summary.preflight_only)",
        "- non_acceptance_evidence: $($Summary.non_acceptance_evidence)",
        "- acceptance_evidence: $($Summary.acceptance_evidence)",
        "- emulator_required: $($Summary.emulator_required)",
        "- output_dir: ``$($Summary.output_dir)``",
        "",
        "## Steps"
    )
    foreach ($step in @($Summary.steps)) {
        $lines += "- $($step.name): $($step.status) (exit $($step.exit_code))"
    }
    $lines += @("", "## Validation Commands")
    foreach ($command in @($Summary.validation_commands)) {
        $lines += "- $($command.name): ``$($command.command)``"
    }
    if ($null -ne $Summary.self_validation) {
        $lines += @("", "## Self Validation")
        $lines += "- $($Summary.self_validation.name): $($Summary.self_validation.status) (exit $($Summary.self_validation.exit_code))"
        $lines += "- command: ``$($Summary.self_validation.command)``"
    }
    $lines += @("", "## Migration Summarizer")
    $lines += "- json: ``$($Summary.migration_summarizer.json_path)``"
    $lines += "- markdown: ``$($Summary.migration_summarizer.markdown_path)``"
    $lines | Set-Content -LiteralPath $Path -Encoding UTF8
}

$resolvedOutputDir = Resolve-BundlePath -Path $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$fixtureDir = Join-Path $resolvedOutputDir "fixtures"
New-Item -ItemType Directory -Force -Path $fixtureDir | Out-Null
$tinyModelPath = Join-Path $fixtureDir "tiny_litert_fixture.task"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($tinyModelPath, "tiny litert placeholder`n", $utf8NoBom)
$taskInventoryPath = Join-Path $fixtureDir "managed_device_tasks.txt"
@(
    "> Task :app:tasks",
    "",
    "Android tasks",
    "-------------",
    "senkuPhoneApi30DebugAndroidTest - Installs and runs instrumentation tests on senkuPhoneApi30.",
    "senkuTabletApi30DebugAndroidTest - Installs and runs instrumentation tests on senkuTabletApi30.",
    "senkuManagedSmokeGroupDebugAndroidTest - Installs and runs instrumentation tests on a group."
) | Set-Content -LiteralPath $taskInventoryPath -Encoding UTF8

$runFilePath = Join-Path $fixtureDir "harness_matrix_runs.jsonl"
@(
    '{"mode":"prompt","query":"How do I purify water?","run_label":"preflight_prompt","ask":true,"wait_for_completion":true}',
    '{"mode":"detail_followup","initial_query":"sprained ankle","follow_up_query":"when should I seek care","run_label":"preflight_followup","emulator":"emulator-5558"}'
) | Set-Content -LiteralPath $runFilePath -Encoding UTF8

$python = Resolve-BundlePath -Path $PythonPath
if (-not (Test-Path -LiteralPath $python)) {
    throw "PythonPath does not exist: $python"
}

$steps = New-Object System.Collections.Generic.List[object]

$toolingDir = Join-Path $resolvedOutputDir "tooling_version_manifest"
$toolingJson = Join-Path $toolingDir "summary.json"
$toolingMd = Join-Path $toolingDir "summary.md"
$toolingCommand = @(
    $python,
    (Join-Path $PSScriptRoot "write_android_tooling_version_manifest.py"),
    "--repo-root",
    $repoRoot,
    "--json-out",
    $toolingJson,
    "--markdown-out",
    $toolingMd
)
if ($NoProbeTools) {
    $toolingCommand += "--no-probe-tools"
}
$steps.Add((Invoke-BundleCommand -Name "tooling_version_manifest" -Command $toolingCommand -OutputDirectory $toolingDir -SummaryPath $toolingJson -MarkdownPath $toolingMd)) | Out-Null

$managedDir = Join-Path $resolvedOutputDir "managed_device_smoke"
$steps.Add((Invoke-BundleCommand -Name "managed_device_smoke_dry_run" -Command @(
    "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $PSScriptRoot "run_android_managed_device_smoke.ps1"),
    "-OutputDir", $managedDir, "-DryRun", "-TaskInventoryPath", $taskInventoryPath
) -OutputDirectory $managedDir -SummaryPath (Join-Path $managedDir "summary.json") -MarkdownPath (Join-Path $managedDir "summary.md"))) | Out-Null

$litertDir = Join-Path $resolvedOutputDir "litert_readiness"
$steps.Add((Invoke-BundleCommand -Name "litert_readiness_dry_run" -Command @(
    "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $PSScriptRoot "run_android_litert_readiness_matrix.ps1"),
    "-OutputDir", $litertDir, "-ModelPath", $tinyModelPath, "-DryRun"
) -OutputDirectory $litertDir -SummaryPath (Join-Path $litertDir "summary.json") -MarkdownPath (Join-Path $litertDir "summary.md"))) | Out-Null

$orchestratorDir = Join-Path $resolvedOutputDir "orchestrator_smoke"
$steps.Add((Invoke-BundleCommand -Name "orchestrator_smoke_dry_run" -Command @(
    "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $PSScriptRoot "run_android_orchestrator_smoke.ps1"),
    "-OutputDir", $orchestratorDir, "-DryRun"
) -OutputDirectory $orchestratorDir -SummaryPath (Join-Path $orchestratorDir "summary.json") -MarkdownPath (Join-Path $orchestratorDir "summary.md"))) | Out-Null

$harnessDir = Join-Path $resolvedOutputDir "harness_matrix_plan"
$steps.Add((Invoke-BundleCommand -Name "harness_matrix_plan_only" -Command @(
    "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $PSScriptRoot "run_android_harness_matrix.ps1"),
    "-RunFile", $runFilePath, "-OutputDir", $harnessDir, "-PlanOnly"
) -OutputDirectory $harnessDir -SummaryPath (Join-Path $harnessDir "summary.json") -MarkdownPath (Join-Path $harnessDir "summary.md"))) | Out-Null

$uiOutputRoot = Convert-ToRepoRelativePath -Path (Join-Path $resolvedOutputDir "ui_state_pack_plan")
$uiStepDir = Join-Path $resolvedOutputDir "ui_state_pack_plan"
$steps.Add((Invoke-BundleCommand -Name "ui_state_pack_plan_only" -Command @(
    "powershell", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $PSScriptRoot "build_android_ui_state_pack_parallel.ps1"),
    "-OutputRoot", $uiOutputRoot, "-PlanOnly"
) -OutputDirectory $uiStepDir)) | Out-Null

$uiPlanPath = Get-ChildItem -LiteralPath $uiStepDir -Recurse -Filter "plan.json" -File |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if ($null -ne $uiPlanPath) {
    $steps[$steps.Count - 1]["summary_path"] = Convert-ToRepoRelativePath -Path $uiPlanPath.FullName
    $steps[$steps.Count - 1]["markdown_path"] = $null
}

$validationCommands = @(
    [ordered]@{ name = "validate_tooling_version_manifest"; command = "$($python) scripts\validate_android_tooling_version_manifest.py $toolingJson"; target = (Convert-ToRepoRelativePath -Path $toolingJson) },
    [ordered]@{ name = "validate_managed_device_smoke"; command = "$($python) scripts\validate_android_managed_device_smoke_summary.py $managedDir\summary.json"; target = (Convert-ToRepoRelativePath -Path (Join-Path $managedDir "summary.json")) },
    [ordered]@{ name = "validate_litert_readiness"; command = "$($python) scripts\validate_android_litert_readiness_summary.py $litertDir\summary.json"; target = (Convert-ToRepoRelativePath -Path (Join-Path $litertDir "summary.json")) },
    [ordered]@{ name = "validate_orchestrator_smoke"; command = "$($python) scripts\validate_android_orchestrator_smoke_summary.py $orchestratorDir\summary.json"; target = (Convert-ToRepoRelativePath -Path (Join-Path $orchestratorDir "summary.json")) },
    [ordered]@{ name = "validate_harness_matrix_plan"; command = "$($python) scripts\validate_android_harness_matrix_plan.py $harnessDir\summary.json"; target = (Convert-ToRepoRelativePath -Path (Join-Path $harnessDir "summary.json")) }
)
if ($null -ne $uiPlanPath) {
    $validationCommands += [ordered]@{
        name = "validate_ui_state_pack_plan"
        command = "$($python) scripts\validate_android_ui_state_pack_plan.py $($uiPlanPath.FullName)"
        target = (Convert-ToRepoRelativePath -Path $uiPlanPath.FullName)
    }
}

foreach ($validation in @($validationCommands)) {
    $validationDir = Join-Path $resolvedOutputDir ("validation_" + $validation.name)
    $parts = @($python, (Join-Path $PSScriptRoot (($validation.name -replace "^validate_", "validate_android_") + ".py")))
    switch ($validation.name) {
        "validate_tooling_version_manifest" { $parts = @($python, (Join-Path $PSScriptRoot "validate_android_tooling_version_manifest.py"), $toolingJson) }
        "validate_managed_device_smoke" { $parts = @($python, (Join-Path $PSScriptRoot "validate_android_managed_device_smoke_summary.py"), (Join-Path $managedDir "summary.json")) }
        "validate_litert_readiness" { $parts = @($python, (Join-Path $PSScriptRoot "validate_android_litert_readiness_summary.py"), (Join-Path $litertDir "summary.json")) }
        "validate_orchestrator_smoke" { $parts = @($python, (Join-Path $PSScriptRoot "validate_android_orchestrator_smoke_summary.py"), (Join-Path $orchestratorDir "summary.json")) }
        "validate_harness_matrix_plan" { $parts = @($python, (Join-Path $PSScriptRoot "validate_android_harness_matrix_plan.py"), (Join-Path $harnessDir "summary.json")) }
        "validate_ui_state_pack_plan" { $parts = @($python, (Join-Path $PSScriptRoot "validate_android_ui_state_pack_plan.py"), $uiPlanPath.FullName) }
    }
    $result = Invoke-BundleCommand -Name $validation.name -Command $parts -OutputDirectory $validationDir
    $validation.exit_code = $result.exit_code
    $validation.status = $result.status
    $validation.stdout_path = $result.stdout_path
}

$summarizerJson = Join-Path $resolvedOutputDir "migration_rows.json"
$summarizerMd = Join-Path $resolvedOutputDir "migration_rows.md"
$summaryInputs = @(
    (Join-Path $managedDir "summary.json"),
    (Join-Path $litertDir "summary.json"),
    (Join-Path $orchestratorDir "summary.json"),
    (Join-Path $harnessDir "summary.json")
)
$summarizerJsonStep = Invoke-BundleCommand -Name "migration_proof_summarizer_json" -Command @(
    $python, (Join-Path $PSScriptRoot "summarize_android_migration_proof.py"
    ), $summaryInputs[0], $summaryInputs[1], $summaryInputs[2], $summaryInputs[3]
) -OutputDirectory (Join-Path $resolvedOutputDir "migration_summarizer_json")
Copy-Item -LiteralPath (Resolve-BundlePath -Path $summarizerJsonStep.stdout_path) -Destination $summarizerJson -Force
$summarizerMdStep = Invoke-BundleCommand -Name "migration_proof_summarizer_markdown" -Command @(
    $python, (Join-Path $PSScriptRoot "summarize_android_migration_proof.py"
    ), "--format", "markdown", $summaryInputs[0], $summaryInputs[1], $summaryInputs[2], $summaryInputs[3]
) -OutputDirectory (Join-Path $resolvedOutputDir "migration_summarizer_markdown")
Copy-Item -LiteralPath (Resolve-BundlePath -Path $summarizerMdStep.stdout_path) -Destination $summarizerMd -Force
$steps.Add($summarizerJsonStep) | Out-Null
$steps.Add($summarizerMdStep) | Out-Null

$finishedAt = (Get-Date).ToUniversalTime()
$allStatuses = @($steps | ForEach-Object { $_.status }) + @($validationCommands | ForEach-Object { $_.status })
$status = if (@($allStatuses | Where-Object { $_ -ne "pass" }).Count -eq 0) { "pass" } else { "fail" }

$summary = New-Object psobject
$summary | Add-Member -NotePropertyName bundle_kind -NotePropertyValue "android_migration_preflight_bundle"
$summary | Add-Member -NotePropertyName schema_version -NotePropertyValue 1
$summary | Add-Member -NotePropertyName status -NotePropertyValue $status
$summary | Add-Member -NotePropertyName metadata_only -NotePropertyValue $true
$summary | Add-Member -NotePropertyName preflight_only -NotePropertyValue $true
$summary | Add-Member -NotePropertyName non_acceptance_evidence -NotePropertyValue $true
$summary | Add-Member -NotePropertyName acceptance_evidence -NotePropertyValue $false
$summary | Add-Member -NotePropertyName emulator_required -NotePropertyValue $false
$summary | Add-Member -NotePropertyName devices_touched -NotePropertyValue $false
$summary | Add-Member -NotePropertyName adb_required -NotePropertyValue $false
$summary | Add-Member -NotePropertyName output_dir -NotePropertyValue (Convert-ToRepoRelativePath -Path $resolvedOutputDir)
$summary | Add-Member -NotePropertyName fixtures -NotePropertyValue ([pscustomobject]@{
    tiny_model_path = (Convert-ToRepoRelativePath -Path $tinyModelPath)
    task_inventory_path = (Convert-ToRepoRelativePath -Path $taskInventoryPath)
    harness_run_file = (Convert-ToRepoRelativePath -Path $runFilePath)
})
$stepArray = $steps.ToArray()
$validationCommandArray = @($validationCommands)
$summary | Add-Member -NotePropertyName steps -NotePropertyValue $stepArray
$summary | Add-Member -NotePropertyName validation_commands -NotePropertyValue $validationCommandArray
$summary | Add-Member -NotePropertyName migration_summarizer -NotePropertyValue ([pscustomobject]@{
    json_path = (Convert-ToRepoRelativePath -Path $summarizerJson)
    markdown_path = (Convert-ToRepoRelativePath -Path $summarizerMd)
    inputs = @($summaryInputs | ForEach-Object { Convert-ToRepoRelativePath -Path $_ })
})
$summary | Add-Member -NotePropertyName started_at_utc -NotePropertyValue $startedAt.ToString("o")
$summary | Add-Member -NotePropertyName finished_at_utc -NotePropertyValue $finishedAt.ToString("o")

$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"
$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"
$selfValidationDir = Join-Path $resolvedOutputDir "validation_validate_migration_preflight_bundle_summary"
$selfValidationCommand = @(
    $python,
    (Join-Path $PSScriptRoot "validate_android_migration_preflight_bundle_summary.py"),
    "--allow-pending-self-validation",
    $summaryJsonPath
)
$selfValidationCommandText = ($selfValidationCommand | ForEach-Object { ConvertTo-QuotedCliToken -Value $_ }) -join " "
$summary | Add-Member -NotePropertyName self_validation -NotePropertyValue ([pscustomobject]@{
    name = "validate_migration_preflight_bundle_summary"
    command = $selfValidationCommandText
    target = (Convert-ToRepoRelativePath -Path $summaryJsonPath)
    status = "not_run"
    exit_code = $null
    stdout_path = $null
})
$summary | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $summaryJsonPath -Encoding UTF8
$selfValidationResult = Invoke-BundleCommand -Name "validate_migration_preflight_bundle_summary" -Command $selfValidationCommand -OutputDirectory $selfValidationDir
$summary.self_validation.status = $selfValidationResult.status
$summary.self_validation.exit_code = $selfValidationResult.exit_code
$summary.self_validation.stdout_path = $selfValidationResult.stdout_path
if ($selfValidationResult.status -ne "pass") {
    $summary.status = "fail"
    $status = "fail"
}
$summary | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $summaryJsonPath -Encoding UTF8
Write-BundleMarkdown -Path $summaryMarkdownPath -Summary $summary

Write-Host "Android migration preflight bundle: $status"
Write-Host "Summary JSON written to $summaryJsonPath"
Write-Host "Summary Markdown written to $summaryMarkdownPath"
if ($status -ne "pass") {
    exit 1
}
