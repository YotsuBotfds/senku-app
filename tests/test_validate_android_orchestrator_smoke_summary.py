import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_orchestrator_smoke_summary import validate_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_orchestrator_smoke_summary.py"


def make_summary() -> dict:
    return {
        "status": "dry_run_only",
        "dry_run": True,
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "test_class": "com.senku.mobile.ScriptedPromptHarnessContractTest",
        "gradle_task": ":app:connectedDebugAndroidTest",
        "gradle_property": "-Psenku.enableTestOrchestrator=true",
        "class_filter_property": (
            "-Pandroid.testInstrumentationRunnerArguments.class="
            "com.senku.mobile.ScriptedPromptHarnessContractTest"
        ),
        "clear_package_data": True,
        "clear_package_data_posture": "enabled_by_orchestrator_property",
        "required_app_apk": "android-app/app/build/outputs/apk/debug/app-debug.apk",
        "required_test_apk": "android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk",
        "required_inputs": {
            "app_apk": "android-app/app/build/outputs/apk/debug/app-debug.apk",
            "test_apk": "android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk",
            "app_apk_exists": False,
            "test_apk_exists": False,
        },
        "expected_result_roots": [
            "android-app/app/build/outputs/androidTest-results/connected/debug",
            "android-app/app/build/reports/androidTests/connected/debug",
        ],
        "planned_command": (
            ".\\gradlew.bat :app:connectedDebugAndroidTest "
            "'-Psenku.enableTestOrchestrator=true' "
            "'-Pandroid.testInstrumentationRunnerArguments.class="
            "com.senku.mobile.ScriptedPromptHarnessContractTest' --console=plain"
        ),
        "gradle_project_dir": "android-app",
        "gradle_wrapper": "android-app/gradlew.bat",
        "would_call_am_instrument": False,
        "would_start_emulators": False,
        "would_launch_connected_instrumentation": False,
        "comparison_baseline": "fixed_four_emulator_matrix",
        "primary_evidence": "fixed_four_emulator_matrix",
        "stop_line": (
            "STOP: fixed four-emulator evidence remains primary; this Orchestrator "
            "smoke is non-acceptance evidence only."
        ),
        "started_at_utc": "2026-04-27T00:00:00.0000000Z",
        "finished_at_utc": "2026-04-27T00:00:00.0000000Z",
    }


class ValidateAndroidOrchestratorSmokeSummaryTests(unittest.TestCase):
    def write_summary(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_summary_passes(self):
        data, errors = validate_summary(self.write_summary(make_summary()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["status"], "dry_run_only")

    def test_rejects_acceptance_evidence(self):
        summary = make_summary()
        summary["acceptance_evidence"] = True

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.acceptance_evidence to be False, got True", errors)

    def test_rejects_missing_orchestrator_property_in_planned_command(self):
        summary = make_summary()
        summary["planned_command"] = ".\\gradlew.bat :app:connectedDebugAndroidTest --console=plain"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.planned_command to include senku.enableTestOrchestrator", errors)

    def test_rejects_non_fixed_four_baseline(self):
        summary = make_summary()
        summary["comparison_baseline"] = "orchestrator_smoke"
        summary["primary_evidence"] = "orchestrator_smoke"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.comparison_baseline to be 'fixed_four_emulator_matrix', "
            "got 'orchestrator_smoke'",
            errors,
        )
        self.assertIn(
            "expected root.primary_evidence to be 'fixed_four_emulator_matrix', "
            "got 'orchestrator_smoke'",
            errors,
        )

    def test_rejects_clear_package_data_posture_drift(self):
        summary = make_summary()
        summary["clear_package_data"] = False
        summary["clear_package_data_posture"] = "disabled"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.clear_package_data to be True, got False", errors)
        self.assertIn(
            "expected root.clear_package_data_posture to be "
            "'enabled_by_orchestrator_property', got 'disabled'",
            errors,
        )

    def test_rejects_runtime_or_emulator_actions(self):
        summary = make_summary()
        summary["would_call_am_instrument"] = True
        summary["would_start_emulators"] = True

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.would_call_am_instrument to be False, got True", errors)
        self.assertIn("expected root.would_start_emulators to be False, got True", errors)

    def test_rejects_wrong_apk_and_result_paths(self):
        summary = make_summary()
        summary["required_app_apk"] = "app.apk"
        summary["required_inputs"]["test_apk"] = "test.apk"
        summary["expected_result_roots"] = ["results"]

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.required_app_apk to be "
            "'android-app/app/build/outputs/apk/debug/app-debug.apk', got 'app.apk'",
            errors,
        )
        self.assertIn(
            "expected root.required_inputs.test_apk to be "
            "'android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk', "
            "got 'test.apk'",
            errors,
        )
        self.assertIn(
            "expected root.expected_result_roots to be "
            "['android-app/app/build/outputs/androidTest-results/connected/debug', "
            "'android-app/app/build/reports/androidTests/connected/debug'], got ['results']",
            errors,
        )

    def test_cli_reports_ok(self):
        path = self.write_summary(make_summary())
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), "-B", str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("android_orchestrator_smoke_summary: ok", result.stdout)
        self.assertIn("evidence: non_acceptance", result.stdout)

    def test_cli_reports_failure(self):
        summary = make_summary()
        summary["dry_run"] = False
        path = self.write_summary(summary)
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), "-B", str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 1)
        self.assertIn("ERROR: expected root.dry_run to be True, got False", result.stdout)


if __name__ == "__main__":
    unittest.main()
