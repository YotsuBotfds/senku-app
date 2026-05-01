import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_migration_validator_suite.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidMigrationValidatorSuiteContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_parameters_and_unittest_invocation_are_stable(self):
        self.assertIn(
            '[string]$PythonPath = ".\\.venvs\\senku-validate\\Scripts\\python.exe"',
            self.script,
        )
        self.assertIn("[switch]$WhatIf", self.script)
        self.assertIn('"unittest"', self.script)
        self.assertIn('$arguments = @("-B", "-m", "unittest") + $testModules', self.script)
        self.assertIn("& $python @arguments", self.script)

    def test_suite_lists_migration_evidence_validators_and_exact_emulator_contracts(self):
        expected_modules = {
            "tests.test_validate_android_migration_summary",
            "tests.test_validate_android_asset_pack_parity_summary",
            "tests.test_validate_android_fts_fallback_summary",
            "tests.test_validate_android_managed_device_smoke_summary",
            "tests.test_validate_android_physical_phone_smoke_summary",
            "tests.test_validate_android_litert_readiness_summary",
            "tests.test_validate_android_orchestrator_smoke_summary",
            "tests.test_validate_android_harness_matrix_plan",
            "tests.test_validate_android_ui_state_pack_plan",
            "tests.test_validate_android_tooling_version_manifest",
            "tests.test_validate_android_headless_state_pack_lane_summary",
            "tests.test_validate_android_instrumented_capture_summary",
            "tests.test_validate_android_large_data_litert_tablet_lane_summary",
            "tests.test_validate_senku_tablet_2_large_data_avd_preflight_summary",
            "tests.test_validate_android_migration_preflight_bundle_summary",
            "tests.test_summarize_android_migration_proof",
            "tests.test_android_prompt_logged_contract",
            "tests.test_android_followup_matrix_contract",
            "tests.test_android_logged_wrapper_pack_cache_contract",
            "tests.test_run_android_functional_ux_smoke_matrix_contract",
            "tests.test_run_android_physical_phone_smoke_contract",
            "tests.test_run_android_instrumented_ui_smoke_summary_contract",
            "tests.test_run_android_instrumented_ui_smoke_capture_summary",
            "tests.test_run_android_search_log_only_contract",
            "tests.test_stop_android_harness_runs_contract",
        }
        for module in expected_modules:
            self.assertIn(f'"{module}"', self.script)

    def test_suite_does_not_call_emulator_or_lane_runners_directly(self):
        forbidden = (
            "adb ",
            "emulator ",
            "run_android_harness_matrix.ps1",
            "run_android_prompt.ps1",
            "run_android_detail_followup.ps1",
            "run_android_migration_preflight_bundle.ps1",
            "start_senku_emulator_matrix.ps1",
        )
        for token in forbidden:
            self.assertNotIn(token, self.script)

    def test_whatif_prints_unittest_modules_without_running_them(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(SCRIPT),
                "-WhatIf",
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Android migration validator suite dry run.", result.stdout)
        self.assertIn("-B -m unittest", result.stdout)
        self.assertIn("tests.test_validate_android_migration_summary", result.stdout)
        self.assertIn("tests.test_android_prompt_logged_contract", result.stdout)

    def test_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_migration_validator_suite.ps1",
                "-SkipAnalyzer",
                "-SkipPester",
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)


if __name__ == "__main__":
    unittest.main()
