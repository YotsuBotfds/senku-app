import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_uiautomator_24_comparison_summary import validate_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_uiautomator_24_comparison_summary.py"


def make_summary() -> dict:
    return {
        "summary_kind": "android_uiautomator_24_comparison",
        "status": "dry_run_only",
        "dry_run": True,
        "plan_only": True,
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "reference_version": "2.3.0",
        "observed_current_version": "2.3.0",
        "candidate_version": "2.4.0-beta02",
        "candidate_dependency": "androidx.test.uiautomator:uiautomator:2.4.0-beta02",
        "dependency_override_property": "-Psenku.uiautomatorComparisonVersion=2.4.0-beta02",
        "dependency_change_posture": "no_global_bump_dry_run_property_only",
        "gradle_task": ":app:connectedDebugAndroidTest",
        "test_class": "com.senku.mobile.ScriptedPromptHarnessContractTest",
        "class_filter_property": (
            "-Pandroid.testInstrumentationRunnerArguments.class="
            "com.senku.mobile.ScriptedPromptHarnessContractTest"
        ),
        "planned_command": (
            ".\\gradlew.bat :app:connectedDebugAndroidTest "
            "'-Psenku.uiautomatorComparisonVersion=2.4.0-beta02' "
            "'-Pandroid.testInstrumentationRunnerArguments.class="
            "com.senku.mobile.ScriptedPromptHarnessContractTest' --console=plain"
        ),
        "gradle_project_dir": "android-app",
        "gradle_wrapper": "android-app/gradlew.bat",
        "app_build_gradle": "android-app/app/build.gradle",
        "expected_result_roots": [
            "android-app/app/build/outputs/androidTest-results/connected/debug",
            "android-app/app/build/reports/androidTests/connected/debug",
        ],
        "comparison_lane": "uiautomator_24_beta_against_current_uiautomator",
        "comparison_baseline": "fixed_four_emulator_matrix",
        "primary_evidence": "fixed_four_emulator_matrix",
        "would_modify_gradle_files": False,
        "would_resolve_candidate_dependency": False,
        "would_call_am_instrument": False,
        "would_start_emulators": False,
        "would_launch_connected_instrumentation": False,
        "devices_touched": False,
        "emulator_required": False,
        "stop_line": (
            "STOP: fixed four-emulator evidence remains primary; this UIAutomator "
            "2.4 comparison is non-acceptance planning evidence only."
        ),
        "started_at_utc": "2026-04-27T00:00:00.0000000Z",
        "finished_at_utc": "2026-04-27T00:00:00.0000000Z",
    }


class ValidateAndroidUiautomator24ComparisonSummaryTests(unittest.TestCase):
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
        self.assertEqual(data["candidate_version"], "2.4.0-beta02")

    def test_rejects_acceptance_or_runtime_posture(self):
        summary = make_summary()
        summary["dry_run"] = False
        summary["acceptance_evidence"] = True
        summary["would_modify_gradle_files"] = True
        summary["would_launch_connected_instrumentation"] = True

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.dry_run to be True, got False", errors)
        self.assertIn("expected root.acceptance_evidence to be False, got True", errors)
        self.assertIn("expected root.would_modify_gradle_files to be False, got True", errors)
        self.assertIn(
            "expected root.would_launch_connected_instrumentation to be False, got True",
            errors,
        )

    def test_rejects_non_fixed_four_baseline(self):
        summary = make_summary()
        summary["comparison_baseline"] = "uiautomator_24"
        summary["primary_evidence"] = "uiautomator_24"
        summary["stop_line"] = "UIAutomator 2.4 is primary now."

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.comparison_baseline to be 'fixed_four_emulator_matrix', "
            "got 'uiautomator_24'",
            errors,
        )
        self.assertIn(
            "expected root.primary_evidence to be 'fixed_four_emulator_matrix', got 'uiautomator_24'",
            errors,
        )
        self.assertIn(
            "expected root.stop_line to preserve fixed four-emulator baseline posture",
            errors,
        )

    def test_rejects_dependency_and_command_drift(self):
        summary = make_summary()
        summary["candidate_version"] = "2.4.0"
        summary["candidate_dependency"] = "androidx.test.uiautomator:uiautomator:2.4.0"
        summary["dependency_override_property"] = "-Pother=true"
        summary["planned_command"] = ".\\gradlew.bat :app:connectedDebugAndroidTest --console=plain"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.candidate_version to be '2.4.0-beta02', got '2.4.0'", errors)
        self.assertIn(
            "expected root.candidate_dependency to be "
            "'androidx.test.uiautomator:uiautomator:2.4.0-beta02', "
            "got 'androidx.test.uiautomator:uiautomator:2.4.0'",
            errors,
        )
        self.assertIn(
            "expected root.dependency_override_property to be "
            "'-Psenku.uiautomatorComparisonVersion=2.4.0-beta02', got '-Pother=true'",
            errors,
        )
        self.assertIn(
            "expected root.planned_command to include UIAutomator comparison property",
            errors,
        )

    def test_rejects_current_version_drift(self):
        summary = make_summary()
        summary["observed_current_version"] = "2.2.0"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.observed_current_version to match current checked-in "
            "UIAutomator '2.3.0', got '2.2.0'",
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
        self.assertIn("android_uiautomator_24_comparison_summary: ok", result.stdout)
        self.assertIn("candidate_version: 2.4.0-beta02", result.stdout)

    def test_cli_reports_failure(self):
        summary = make_summary()
        summary["would_start_emulators"] = True
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
        self.assertIn(
            "ERROR: expected root.would_start_emulators to be False, got True",
            result.stdout,
        )


if __name__ == "__main__":
    unittest.main()
