import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_orchestrator_smoke.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"
MIGRATION_VALIDATOR = REPO_ROOT / "scripts" / "validate_android_migration_summary.py"


class AndroidOrchestratorSmokeContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_wrapper_is_dry_run_only_and_records_orchestrator_contract(self):
        self.assertIn("[switch]$DryRun", self.script)
        self.assertIn("[string]$AppApkPath", self.script)
        self.assertIn("[string]$TestApkPath", self.script)
        self.assertIn("com.senku.mobile.ScriptedPromptHarnessContractTest", self.script)
        self.assertIn("if (-not $DryRun)", self.script)
        self.assertIn("dry-run-only", self.script)
        self.assertIn("must not call am instrument", self.script)
        self.assertIn("start emulators", self.script)
        self.assertIn('"-Psenku.enableTestOrchestrator=true"', self.script)
        self.assertIn('"-Pandroid.testInstrumentationRunnerArguments.class=$TestClass"', self.script)
        self.assertIn('":app:connectedDebugAndroidTest"', self.script)
        self.assertIn("clear_package_data = $clearPackageData", self.script)
        self.assertIn('"enabled_by_orchestrator_property"', self.script)
        self.assertIn("required_app_apk", self.script)
        self.assertIn("required_test_apk", self.script)
        self.assertIn("androidTest-results/connected/debug", self.script)
        self.assertIn("reports/androidTests/connected/debug", self.script)
        self.assertIn("non_acceptance_evidence = $true", self.script)
        self.assertIn("acceptance_evidence = $false", self.script)
        self.assertIn("would_call_am_instrument = $false", self.script)
        self.assertIn("would_start_emulators = $false", self.script)
        self.assertIn("would_launch_connected_instrumentation = $false", self.script)
        self.assertIn("fixed four-emulator evidence remains primary", self.script)
        self.assertIn('$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"', self.script)
        self.assertIn('$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"', self.script)

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
                "scripts\\run_android_orchestrator_smoke.ps1",
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

    def test_dry_run_writes_validator_compatible_summary(self):
        output_dir = Path(tempfile.mkdtemp(prefix="orchestrator_smoke_"))
        try:
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-OutputDir",
                    str(output_dir),
                    "-DryRun",
                    "-AppApkPath",
                    "android-app\\app\\build\\outputs\\apk\\debug\\app-debug.apk",
                    "-TestApkPath",
                    "android-app\\app\\build\\outputs\\apk\\androidTest\\debug\\app-debug-androidTest.apk",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("fixed four-emulator evidence remains primary", result.stdout)
            self.assertNotIn("am instrument", result.stdout)
            summary_path = output_dir / "summary.json"
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["status"], "dry_run_only")
            self.assertTrue(summary["dry_run"])
            self.assertTrue(summary["non_acceptance_evidence"])
            self.assertFalse(summary["acceptance_evidence"])
            self.assertNotIn("evidence_kind", summary)
            self.assertEqual(summary["test_class"], "com.senku.mobile.ScriptedPromptHarnessContractTest")
            self.assertEqual(summary["gradle_task"], ":app:connectedDebugAndroidTest")
            self.assertEqual(summary["gradle_property"], "-Psenku.enableTestOrchestrator=true")
            self.assertEqual(
                summary["class_filter_property"],
                "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.ScriptedPromptHarnessContractTest",
            )
            self.assertTrue(summary["clear_package_data"])
            self.assertEqual(summary["clear_package_data_posture"], "enabled_by_orchestrator_property")
            self.assertEqual(
                summary["required_app_apk"],
                "android-app/app/build/outputs/apk/debug/app-debug.apk",
            )
            self.assertEqual(
                summary["required_test_apk"],
                "android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk",
            )
            self.assertEqual(
                summary["expected_result_roots"],
                [
                    "android-app/app/build/outputs/androidTest-results/connected/debug",
                    "android-app/app/build/reports/androidTests/connected/debug",
                ],
            )
            self.assertEqual(
                summary["planned_command"],
                ".\\gradlew.bat :app:connectedDebugAndroidTest '-Psenku.enableTestOrchestrator=true' "
                "'-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.ScriptedPromptHarnessContractTest' "
                "--console=plain",
            )
            self.assertFalse(summary["would_call_am_instrument"])
            self.assertFalse(summary["would_start_emulators"])
            self.assertFalse(summary["would_launch_connected_instrumentation"])
            self.assertEqual(summary["comparison_baseline"], "fixed_four_emulator_matrix")
            self.assertEqual(summary["primary_evidence"], "fixed_four_emulator_matrix")
            self.assertIn("fixed four-emulator evidence remains primary", summary["stop_line"])
            self.assertTrue((output_dir / "summary.md").exists())

            validator = subprocess.run(
                [
                    str(REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"),
                    "-B",
                    str(MIGRATION_VALIDATOR),
                    str(summary_path),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertEqual(validator.returncode, 0, validator.stderr + validator.stdout)
            self.assertIn("android_migration_summary: ok", validator.stdout)
            self.assertIn("evidence: non_acceptance", validator.stdout)
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_without_dry_run_fails_before_writing_artifacts(self):
        output_dir = Path(tempfile.mkdtemp(prefix="orchestrator_smoke_no_dry_run_"))
        try:
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-OutputDir",
                    str(output_dir),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("dry-run-only", result.stderr + result.stdout)
            self.assertFalse((output_dir / "summary.json").exists())
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
