import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"
WINDOWS_VALIDATION_PATH = REPO_ROOT / "scripts" / "run_windows_validation.ps1"
ANDROID_PROMPT_PATH = REPO_ROOT / "scripts" / "run_android_prompt.ps1"
ANDROID_SMOKE_PATH = REPO_ROOT / "scripts" / "run_android_instrumented_ui_smoke.ps1"
ANDROID_UI_STATE_PACK_PATH = REPO_ROOT / "scripts" / "build_android_ui_state_pack.ps1"
ANDROID_UI_STATE_PACK_PARALLEL_PATH = REPO_ROOT / "scripts" / "build_android_ui_state_pack_parallel.ps1"
LITERT_MODEL_PUSH_PATH = REPO_ROOT / "scripts" / "push_litert_model_to_android.ps1"
ANDROID_WRAPPER_SMOKE_PATHS = (
    "scripts\\run_android_instrumented_ui_smoke.ps1",
    "scripts\\run_android_prompt.ps1",
)
ANDROID_HARNESS_SCRIPT_PATHS = (
    "scripts\\run_android_prompt.ps1",
    "scripts\\run_android_prompt_logged.ps1",
    "scripts\\run_android_prompt_batch.ps1",
    "scripts\\run_android_harness_matrix.ps1",
    "scripts\\android_harness_common.psm1",
    "scripts\\run_android_instrumented_ui_smoke.ps1",
    "scripts\\start_senku_emulator_matrix.ps1",
    "scripts\\build_android_ui_state_pack.ps1",
    "scripts\\build_android_ui_state_pack_parallel.ps1",
    "scripts\\start_litert_host_server.ps1",
    "scripts\\push_litert_model_to_android.ps1",
)
WRAPPER_SLICE_PATHS = (
    "scripts\\run_powershell_quality_gate.ps1",
    "scripts\\run_windows_validation.ps1",
    "scripts\\run_validation_slice.ps1",
    "scripts\\run_non_android_regression_gate.ps1",
    "tests\\powershell",
)


def run_gate(*args: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [
            "powershell",
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-File",
            str(SCRIPT_PATH),
            *args,
        ],
        cwd=REPO_ROOT,
        capture_output=True,
        text=True,
        check=False,
    )


def run_windows_validation(*args: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [
            "powershell",
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-File",
            str(WINDOWS_VALIDATION_PATH),
            *args,
        ],
        cwd=REPO_ROOT,
        capture_output=True,
        text=True,
        check=False,
    )


class PowerShellQualityGateTests(unittest.TestCase):
    def test_analyzer_gate_blocks_errors_but_reports_warning_debt(self):
        script = SCRIPT_PATH.read_text(encoding="utf-8")

        self.assertIn("$blockingFindings = @($findings | Where-Object { $_.Severity -eq 'Error' })", script)
        self.assertIn("error finding(s)", script)
        self.assertIn("non-blocking warning/information finding(s)", script)

    def test_quality_gate_parses_selected_script(self):
        result = run_gate(
            "-Path",
            "scripts\\run_powershell_quality_gate.ps1",
            "-SkipAnalyzer",
            "-SkipPester",
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)

    def test_quality_gate_parses_android_wrapper_smoke_scripts(self):
        result = run_gate(
            "-Path",
            ",".join(ANDROID_WRAPPER_SMOKE_PATHS),
            "-SkipAnalyzer",
            "-SkipPester",
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)
        self.assertIn("2 PowerShell file(s)", result.stdout)

    def test_quality_gate_fails_closed_on_parser_errors(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            invalid_script = Path(temp_dir) / "invalid_wrapper.ps1"
            invalid_script.write_text(
                "param(\n[string]$Name\nWrite-Host $Name\n",
                encoding="utf-8",
            )

            result = run_gate(
                "-Path",
                str(invalid_script),
                "-SkipAnalyzer",
                "-SkipPester",
            )

        self.assertNotEqual(result.returncode, 0)
        output = result.stderr + result.stdout
        self.assertIn("invalid_wrapper.ps1", output)
        self.assertIn("Missing ')' in function parameter list", output)

    def test_android_harness_wrapper_slice_parser_passes_without_emulator(self):
        result = run_gate(
            "-Path",
            ",".join(ANDROID_HARNESS_SCRIPT_PATHS),
            "-SkipAnalyzer",
            "-SkipPester",
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)

    def test_emulator_matrix_exposes_optional_launch_profile_knobs(self):
        script = (REPO_ROOT / "scripts" / "start_senku_emulator_matrix.ps1").read_text(encoding="utf-8")

        self.assertIn("[switch]$Headless", script)
        self.assertIn("[int]$PartitionSizeMb = 0", script)
        self.assertIn('$arguments += "-no-window"', script)
        self.assertIn('$arguments += @("-partition-size", [string]$PartitionSizeMb)', script)
        self.assertIn("function Get-SenkuEmulatorLaunchArguments", script)
        self.assertIn('Write-Host ("  args: {0}" -f ($launchArguments -join " "))', script)
        self.assertIn("-Headless:$Headless", script)
        self.assertIn("-PartitionSizeMb $PartitionSizeMb", script)

    def test_android_ui_state_pack_exposes_slice_contracts(self):
        script = ANDROID_UI_STATE_PACK_PATH.read_text(encoding="utf-8")

        self.assertIn('[string]$OutputRoot = "artifacts/ui_state_pack"', script)
        self.assertIn("[switch]$SkipHostStates", script)
        self.assertIn("[string[]]$RoleFilter = @()", script)
        self.assertIn('[string]$RunId = ""', script)
        self.assertIn('$deviceMatrix = @($deviceMatrix | Where-Object { $normalizedRoleFilter -contains $_.role })', script)
        self.assertIn('throw "RoleFilter did not match any device roles."', script)
        self.assertIn('if ($SkipHostStates -and $state.host) {', script)
        self.assertIn('host_states_included = (-not $SkipHostStates)', script)
        self.assertIn('function Get-StableIdentitySha256', script)
        self.assertIn('host_inference_model = $(if ($state.host) { [string]$followupSummary.host_inference_model } else { $null })', script)
        self.assertIn('model_identity_source = $(if ($state.host) { "host_inference" } else { $null })', script)
        self.assertIn('model_sha = $(if ($state.host) { Get-StableIdentitySha256 -Value ("host-inference|{0}|{1}" -f [string]$followupSummary.host_inference_url, [string]$followupSummary.host_inference_model) } else { $null })', script)
        self.assertIn("platform_anr = $trustedSummary.platform_anr", script)
        self.assertIn("platform_anr = $trustedFollowupSummary.platform_anr", script)
        self.assertIn("$platformAnrCount = @($results | Where-Object", script)
        self.assertIn("platform_anr_count = [int]$platformAnrCount", script)

    def test_android_ui_validation_local_shell_generation_stays_bounded(self):
        script = (REPO_ROOT / "scripts" / "run_android_ui_validation_pack.ps1").read_text(encoding="utf-8")

        self.assertIn('$case.expected_mode_hint -eq "generated" -and $effectiveInferenceMode -ne "local"', script)
        self.assertIn('if ($ForceShellExecution) {', script)
        self.assertIn('$promptArgs += "-ForceShellExecution"', script)
        self.assertIn('"-InferenceMode", $effectiveInferenceMode', script)

    def test_android_ui_state_pack_parallel_forwards_role_slices_and_skip_host_states(self):
        script = ANDROID_UI_STATE_PACK_PARALLEL_PATH.read_text(encoding="utf-8")

        self.assertIn('[switch]$SkipHostStates', script)
        self.assertIn("[switch]$PlanOnly", script)
        self.assertIn('$allRoles = @("phone_portrait", "phone_landscape", "tablet_portrait", "tablet_landscape")', script)
        self.assertIn("$roles = @($allRoles)", script)
        self.assertIn("if ($PlanOnly) {", script)
        self.assertIn('plan_only = $true', script)
        self.assertIn('launchers = @($roles | ForEach-Object', script)
        self.assertIn('Write-Host ("Plan: {0}" -f $planPath)', script)
        self.assertIn('-RunId "{2}" -RoleFilter "{3}" -SkipFinalize -SkipBuild{4}', script)
        self.assertIn("$(if ($SkipHostStates) { ' -SkipHostStates' } else { '' })", script)
        self.assertIn("-FinalizeOnly -SkipBuild", script)

    def test_android_stop_helper_covers_fts_fallback_matrix(self):
        script = (REPO_ROOT / "scripts" / "stop_android_harness_runs.ps1").read_text(encoding="utf-8")

        self.assertIn('"run_android_fts_fallback_matrix.ps1"', script)
        self.assertIn("Stop-AndroidPackages", script)

    def test_android_smoke_remains_single_device_entrypoint_for_state_pack_slices(self):
        script = ANDROID_SMOKE_PATH.read_text(encoding="utf-8")

        self.assertIn('[string]$Device = "RFCX607ZM8L"', script)
        self.assertIn("Acquire-DeviceLock -DeviceName $Device", script)
        self.assertIn('$artifactDir = Join-Path $repoRoot (Join-Path $ArtifactRoot (Join-Path $timestamp $Device))', script)
        self.assertNotIn("RunAllDevices", script)
        self.assertNotIn("adb devices", script)

    def test_android_prompt_accepts_orientation_contract(self):
        script = ANDROID_PROMPT_PATH.read_text(encoding="utf-8")

        self.assertIn('[ValidateSet("", "portrait", "landscape")]', script)
        self.assertIn('[string]$Orientation = ""', script)
        self.assertIn('$preflightArgs += @("-Orientation", $Orientation)', script)
        self.assertIn('$instrumentationArgs += @("-Orientation", $Orientation)', script)
        self.assertIn("function Test-ComposeAnswerCardCompleted", script)
        self.assertIn('$text -eq "senku answered"', script)
        self.assertIn('$text.Contains("next field question")', script)
        self.assertIn("Test-ComposeAnswerCardCompleted -Xml $xml", script)

    def test_android_prompt_forwards_mobile_pack_cache_controls(self):
        script = ANDROID_PROMPT_PATH.read_text(encoding="utf-8")

        self.assertIn("[switch]$SkipPackPushIfCurrent", script)
        self.assertIn("[switch]$ForcePackPush", script)
        self.assertIn('[string]$PushPackSummaryPath = ""', script)
        self.assertIn('$pushPackArgs = @(', script)
        self.assertIn('if ($SkipPackPushIfCurrent)', script)
        self.assertIn('$pushPackArgs += "-SkipIfCurrent"', script)
        self.assertIn('if ($ForcePackPush)', script)
        self.assertIn('$pushPackArgs += "-ForcePush"', script)
        self.assertIn('$pushPackArgs += @("-SummaryPath", $PushPackSummaryPath)', script)
        self.assertIn("& $pushPackScript @pushPackArgs", script)

    def test_android_smoke_classifies_platform_anr_dialogs(self):
        script = ANDROID_SMOKE_PATH.read_text(encoding="utf-8")

        self.assertIn("function Get-PlatformAnrEvidence", script)
        self.assertIn("System UI isn't responding", script)

    def test_android_smoke_records_host_model_identity(self):
        script = ANDROID_SMOKE_PATH.read_text(encoding="utf-8")

        self.assertIn("function Resolve-SummaryModelIdentity", script)
        self.assertIn('$identityInput = "host-inference|$EffectiveHostInferenceUrl|$HostInferenceModel"', script)
        self.assertIn("host_inference_model =", script)
        self.assertIn("model_identity_source = $summaryModelIdentity.source", script)
        self.assertIn("model_sha = $summaryModelIdentity.sha", script)
        self.assertIn('"stat", "-c", "%n|%s|%Y|%f|%i"', script)
        self.assertIn('$script:IdentityCacheHit = $true', script)
        self.assertIn("identity_cache_hit = [bool]$script:IdentityCacheHit", script)
        self.assertIn('resource-id="android:id/aerr_wait"', script)
        self.assertIn('resource-id="android:id/aerr_close"', script)
        self.assertIn("throw $platformAnrEvidence.reason", script)

    def test_android_smoke_install_cache_uses_apk_hashes(self):
        script = ANDROID_SMOKE_PATH.read_text(encoding="utf-8")

        self.assertIn('(Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()', script)
        self.assertIn('(-not [string]::IsNullOrWhiteSpace([string]$state.app.sha256))', script)
        self.assertIn('(-not [string]::IsNullOrWhiteSpace([string]$state.test.sha256))', script)
        self.assertIn('([string]$state.app.sha256 -eq [string]$AppFingerprint.sha256)', script)
        self.assertIn('([string]$state.test.sha256 -eq [string]$TestFingerprint.sha256)', script)
        self.assertIn('$InstallCacheMatches = Test-InstallCacheMatch', script)
        self.assertIn('$EffectiveSkipInstall = [bool]$SkipInstall', script)
        self.assertIn('install_cache_matches = [bool]$InstallCacheMatches', script)
        self.assertIn("app = $appFingerprint", script)
        self.assertIn("test = $testFingerprint", script)
        self.assertIn("Set-Content -LiteralPath $installStatePath", script)
        self.assertNotIn('$EffectiveSkipInstall = $SkipInstall -or', script)
        self.assertIn("platform_anr = $platformAnrEvidence", script)

    def test_litert_model_push_prunes_models_without_shell_wildcards(self):
        script = LITERT_MODEL_PUSH_PATH.read_text(encoding="utf-8")

        self.assertIn("[switch]$PruneExistingModels", script)
        self.assertIn("function Assert-DeviceHasModelStagingSpace", script)
        self.assertIn('$requiredBytes = ($ModelBytes * 2L) + 67108864L', script)
        self.assertIn('Insufficient /data free space', script)
        self.assertIn('Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "ls", "files/models") -AllowFailure', script)
        self.assertIn("$script:LastAdbExitCode -eq 0", script)
        self.assertIn('$trimmed.EndsWith(".litertlm") -or $trimmed.EndsWith(".task")', script)
        self.assertIn('Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "run-as", $PackageName, "rm", "-f", ("files/models/" + $trimmed))', script)
        self.assertIn('Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "rm", "-rf", $RemoteTempDir) -AllowFailure', script)
        self.assertNotIn('sh", "-c"', script)
        self.assertNotIn('files/models/*.litertlm', script)
        self.assertNotIn('files/models/*.task', script)

    def test_android_fts_probe_handles_missing_host_tables(self):
        script = (REPO_ROOT / "scripts" / "android_fts5_probe.ps1").read_text(encoding="utf-8")

        self.assertIn("function Normalize-DeviceArguments", script)
        self.assertIn('[string]$serial -split ","', script)
        self.assertIn("$Devices = Normalize-DeviceArguments -Serials $Devices", script)
        self.assertIn('$hostTables = @()', script)
        self.assertIn('$hostProbe.PSObject.Properties.Name -contains "tables"', script)
        self.assertIn('hostPackContainsFts5Schema = @($hostTables | Where-Object', script)
        self.assertIn('hostPackContainsFts4Schema = @($hostTables | Where-Object', script)

    def test_android_harness_uses_shared_device_normalizer(self):
        common = (REPO_ROOT / "scripts" / "android_harness_common.psm1").read_text(encoding="utf-8")
        matrix = (REPO_ROOT / "scripts" / "run_android_harness_matrix.ps1").read_text(encoding="utf-8")
        validation = (REPO_ROOT / "scripts" / "run_android_ui_validation_pack.ps1").read_text(encoding="utf-8")

        self.assertIn("function Resolve-AndroidHarnessDeviceList", common)
        self.assertIn('[string]$deviceEntry -split ","', common)
        self.assertIn("Resolve-AndroidHarnessDeviceList", matrix)
        self.assertIn("$Emulators = @(Resolve-AndroidHarnessDeviceList -Devices $Emulators)", matrix)
        self.assertIn("Resolve-AndroidHarnessDeviceList", validation)
        self.assertIn("$normalizedDevices = @(Resolve-AndroidHarnessDeviceList -Devices $Devices)", validation)

    def test_android_ui_validation_uses_shared_posture_helpers(self):
        validation = (REPO_ROOT / "scripts" / "run_android_ui_validation_pack.ps1").read_text(encoding="utf-8")

        self.assertIn("Resolve-AndroidDeviceFacts -AdbPath $adb", validation)
        self.assertIn("Get-AndroidScreenshotFacts -Path $screenPath -RequestedOrientation $Orientation", validation)
        self.assertIn("Get-AndroidCurrentRotation -AdbPath $adb", validation)
        self.assertNotIn("function Get-PhysicalDisplaySize", validation)
        self.assertNotIn("function Get-ScreenshotDimensions", validation)
        self.assertNotIn("Get-PhysicalDisplaySize -Device", validation)
        self.assertNotIn("Get-ScreenshotDimensions -Path", validation)

    def test_android_smoke_is_single_device_and_summarizes_direct_proof(self):
        script = ANDROID_SMOKE_PATH.read_text(encoding="utf-8")

        self.assertIn('[string]$Device = "RFCX607ZM8L"', script)
        self.assertNotIn("RunAllDevices", script)
        self.assertNotIn("adb devices", script)
        self.assertIn('$artifactDir = Join-Path $repoRoot (Join-Path $ArtifactRoot (Join-Path $timestamp $Device))', script)
        self.assertIn("$defaultSummaryPath = Join-Path $artifactDir \"summary.json\"", script)
        self.assertIn("device = $Device", script)
        self.assertIn("artifact_dir = $artifactDir", script)
        self.assertIn("artifact_expectations_met = [bool]$artifactExpectationsMet", script)

    def test_android_prompt_surfaces_instrumentation_summary_failure_evidence(self):
        script = ANDROID_PROMPT_PATH.read_text(encoding="utf-8")

        self.assertIn("function Get-InstrumentationSummaryFailureMessage", script)
        self.assertIn("$summary.failure_reason", script)
        self.assertIn("$summary.platform_anr", script)
        self.assertIn("platform_anr.reason={0}", script)
        self.assertIn("platform_anr.dump={0}", script)
        self.assertIn(
            'throw "Instrumentation execution failed ($summaryFailureMessage)"',
            script,
        )

    def test_android_prompt_emits_host_adb_platform_tools_version_metadata(self):
        script = ANDROID_PROMPT_PATH.read_text(encoding="utf-8")

        self.assertIn(
            "$hostAdbPlatformToolsVersion = Get-AndroidHostAdbPlatformToolsVersion -AdbPath $adb",
            script,
        )
        self.assertIn("function Write-RunAndroidPromptMetadata", script)
        self.assertIn("host_adb_platform_tools_version = $HostAdbPlatformToolsVersion", script)
        self.assertIn("ConvertTo-Json -Compress | Write-Output", script)
        self.assertIn(
            "$summaryHostAdbPlatformToolsVersion = [string]$summary.host_adb_platform_tools_version",
            script,
        )
        self.assertIn(
            "$summaryHostAdbPlatformToolsVersion = $hostAdbPlatformToolsVersion",
            script,
        )
        self.assertIn(
            "Write-RunAndroidPromptMetadata -HostAdbPlatformToolsVersion $summaryHostAdbPlatformToolsVersion",
            script,
        )
        self.assertIn(
            "Write-RunAndroidPromptMetadata -HostAdbPlatformToolsVersion $hostAdbPlatformToolsVersion",
            script,
        )

    def test_quality_gate_dry_run_lists_selected_files(self):
        result = run_gate(
            "-Path",
            "scripts\\run_powershell_quality_gate.ps1",
            "-WhatIf",
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("PowerShell quality gate dry run.", result.stdout)
        self.assertIn("scripts\\run_powershell_quality_gate.ps1", result.stdout)

    def test_wrapper_slice_whatif_excludes_android_scripts(self):
        result = run_gate("-Path", ",".join(WRAPPER_SLICE_PATHS), "-WhatIf")

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("PowerShell quality gate dry run.", result.stdout)
        for selected_path in WRAPPER_SLICE_PATHS:
            self.assertIn(selected_path, result.stdout)
        self.assertNotIn("scripts\\android", result.stdout.lower())
        self.assertNotIn("android-app", result.stdout.lower())
        self.assertNotIn("emulator", result.stdout.lower())
        self.assertNotIn("adb", result.stdout.lower())

    def test_wrapper_slice_parser_passes_without_optional_modules(self):
        result = run_gate(
            "-Path",
            ",".join(WRAPPER_SLICE_PATHS),
            "-SkipAnalyzer",
            "-SkipPester",
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)

    def test_require_flags_cannot_be_combined_with_matching_skip_flags(self):
        cases = [
            ("-RequireAnalyzer", "-SkipAnalyzer"),
            ("-RequirePester", "-SkipPester"),
        ]

        for require_flag, skip_flag in cases:
            with self.subTest(require_flag=require_flag, skip_flag=skip_flag):
                result = run_gate(
                    "-Path",
                    "scripts\\run_powershell_quality_gate.ps1",
                    require_flag,
                    skip_flag,
                    "-WhatIf",
                )

                self.assertNotEqual(result.returncode, 0)
                self.assertIn(
                    f"{require_flag} cannot be combined with {skip_flag}",
                    result.stderr + result.stdout,
                )

    def test_require_analyzer_fails_cleanly_when_missing(self):
        result = run_gate(
            "-Path",
            "scripts\\run_powershell_quality_gate.ps1",
            "-SkipParser",
            "-SkipPester",
            "-RequireAnalyzer",
        )

        if result.returncode == 0:
            self.skipTest("PSScriptAnalyzer is installed in this environment")
        self.assertIn("PSScriptAnalyzer is not installed", result.stderr + result.stdout)

    def test_windows_validation_uses_call_operator_for_dynamic_commands(self):
        script = WINDOWS_VALIDATION_PATH.read_text(encoding="utf-8")

        self.assertNotIn("Invoke-Expression", script)
        self.assertIn("& $pythonPath -m py_compile query.py ingest.py bench.py", script)
        self.assertIn("& $pythonPath -m unittest discover -s tests -v", script)
        self.assertIn("& $pythonPath ingest.py --stats", script)
        self.assertIn("& $guideScript", script)
        self.assertIn("-ExtraBenchArgs $ExtraBenchArgs", script)

    def test_windows_validation_android_migration_whatif_lists_validator_modules_only(self):
        result = run_windows_validation("-Mode", "android-migration", "-WhatIf")

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Android migration validator suite dry run.", result.stdout)
        self.assertIn("-B -m unittest", result.stdout)
        self.assertIn("Modules:", result.stdout)
        self.assertIn("tests.test_validate_android_migration_summary", result.stdout)
        self.assertIn("tests.test_validate_android_asset_pack_parity_summary", result.stdout)
        self.assertIn("tests.test_validate_android_fts_fallback_summary", result.stdout)
        self.assertIn("tests.test_validate_android_harness_matrix_plan", result.stdout)
        self.assertIn("tests.test_stop_android_harness_runs_contract", result.stdout)

        output_lower = (result.stderr + result.stdout).lower()
        self.assertNotIn("adb devices", output_lower)
        self.assertNotIn("am instrument", output_lower)
        self.assertNotIn("emulator", output_lower)
        self.assertNotIn("install ", output_lower)
        self.assertNotIn(" shell ", output_lower)


if __name__ == "__main__":
    unittest.main()
