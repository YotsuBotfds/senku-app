import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_managed_device_smoke_summary import validate_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_managed_device_smoke_summary.py"


def make_summary() -> dict:
    expected_artifact_roots = [
        "android-app/app/build/outputs/androidTest-results/managedDevice/senkuPhoneApi30",
        "android-app/app/build/outputs/androidTest-results/managedDevice/senkuTabletApi30",
        "android-app/app/build/reports/androidTests/managedDevice/senkuPhoneApi30",
        "android-app/app/build/reports/androidTests/managedDevice/senkuTabletApi30",
    ]
    expected_gradle_task_names = [
        ":app:senkuPhoneApi30DebugAndroidTest",
        ":app:senkuTabletApi30DebugAndroidTest",
        ":app:senkuManagedSmokeGroupDebugAndroidTest",
    ]
    return {
        "status": "dry_run_only",
        "dry_run": True,
        "non_acceptance_evidence": True,
        "gradle_property": "-Psenku.enableManagedDevices=true",
        "task_group": "senkuManagedSmoke",
        "task_name": ":app:senkuManagedSmoke",
        "expected_devices": ["senkuPhoneApi30", "senkuTabletApi30"],
        "expected_artifact_roots": expected_artifact_roots,
        "expected_test_target": ":app:senkuManagedSmoke",
        "planned_task_inventory_command": (
            ".\\gradlew.bat :app:tasks --all "
            "-Psenku.enableManagedDevices=true --console=plain"
        ),
        "expected_gradle_task_names": expected_gradle_task_names,
        "observed_gradle_task_names": [],
        "observed_expected_gradle_task_names": [],
        "task_inventory_source": "not_collected",
        "task_inventory_probe_ran": False,
        "task_inventory": None,
        "comparison_baseline": "fixed_four_emulator_matrix",
        "planned_command": (
            ".\\gradlew.bat :app:senkuManagedSmoke "
            "-Psenku.enableManagedDevices=true --console=plain"
        ),
        "gradle_project_dir": "android-app",
        "gradle_wrapper": "android-app/gradlew.bat",
        "would_launch_emulators": False,
        "managed_devices_launched": False,
        "acceptance_evidence": False,
        "primary_evidence": "fixed_four_emulator_matrix",
        "stop_line": (
            "STOP: fixed four-emulator evidence remains primary; this Gradle "
            "Managed Devices smoke is non-acceptance evidence only."
        ),
        "started_at_utc": "2026-04-27T00:00:00.0000000Z",
        "finished_at_utc": "2026-04-27T00:00:00.0000000Z",
        "managed_device_scaffold": {
            "agp_plugin_version": "8.2.1",
            "configured_device_names": ["senkuPhoneApi30", "senkuTabletApi30"],
            "configured_device_api_levels": [30, 30],
            "configured_device_image_sources": ["aosp", "aosp"],
            "configured_devices": [
                {"name": "senkuPhoneApi30", "api_level": 30, "image_source": "aosp"},
                {"name": "senkuTabletApi30", "api_level": 30, "image_source": "aosp"},
            ],
            "smoke_group": "senkuManagedSmoke",
            "smoke_group_devices": ["senkuPhoneApi30", "senkuTabletApi30"],
            "expected_gradle_task_names": expected_gradle_task_names,
            "expected_artifact_roots": expected_artifact_roots,
        },
    }


class ValidateAndroidManagedDeviceSmokeSummaryTests(unittest.TestCase):
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
        self.assertEqual(data["task_name"], ":app:senkuManagedSmoke")

    def test_rejects_acceptance_or_runtime_posture(self):
        summary = make_summary()
        summary["dry_run"] = False
        summary["non_acceptance_evidence"] = False
        summary["acceptance_evidence"] = True
        summary["would_launch_emulators"] = True
        summary["managed_devices_launched"] = True

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.dry_run to be True, got False", errors)
        self.assertIn("expected root.non_acceptance_evidence to be True, got False", errors)
        self.assertIn("expected root.acceptance_evidence to be False, got True", errors)
        self.assertIn("expected root.would_launch_emulators to be False, got True", errors)
        self.assertIn("expected root.managed_devices_launched to be False, got True", errors)

    def test_rejects_non_fixed_four_baseline(self):
        summary = make_summary()
        summary["comparison_baseline"] = "managed_device_smoke"
        summary["primary_evidence"] = "managed_device_smoke"
        summary["stop_line"] = "Managed Devices are primary now."

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.comparison_baseline to be 'fixed_four_emulator_matrix', "
            "got 'managed_device_smoke'",
            errors,
        )
        self.assertIn(
            "expected root.primary_evidence to be 'fixed_four_emulator_matrix', "
            "got 'managed_device_smoke'",
            errors,
        )
        self.assertIn(
            "expected root.stop_line to preserve fixed four-emulator baseline posture",
            errors,
        )

    def test_rejects_gradle_property_task_and_command_drift(self):
        summary = make_summary()
        summary["gradle_property"] = "-Pother=true"
        summary["task_group"] = "otherGroup"
        summary["task_name"] = ":app:otherGroup"
        summary["planned_task_inventory_command"] = ".\\gradlew.bat :app:tasks --all"
        summary["planned_command"] = ".\\gradlew.bat :app:otherGroup --console=plain"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.gradle_property to be '-Psenku.enableManagedDevices=true', got '-Pother=true'",
            errors,
        )
        self.assertIn("expected root.task_group to be 'senkuManagedSmoke', got 'otherGroup'", errors)
        self.assertIn(
            "expected root.planned_command to include senku.enableManagedDevices",
            errors,
        )
        self.assertIn("expected root.planned_command to target :app:senkuManagedSmoke", errors)

    def test_rejects_device_artifact_and_scaffold_drift(self):
        summary = make_summary()
        summary["expected_devices"] = ["senkuPhoneApi30"]
        summary["expected_artifact_roots"] = ["android-app/app/build/outputs/androidTest-results"]
        summary["managed_device_scaffold"]["configured_device_api_levels"] = [29, 30]
        summary["managed_device_scaffold"]["smoke_group_devices"] = ["senkuPhoneApi30"]

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.expected_devices to be ['senkuPhoneApi30', 'senkuTabletApi30'], "
            "got ['senkuPhoneApi30']",
            errors,
        )
        self.assertIn(
            "expected root.managed_device_scaffold.configured_device_api_levels to be [30, 30], "
            "got [29, 30]",
            errors,
        )
        self.assertIn(
            "expected root.managed_device_scaffold.smoke_group_devices to be "
            "['senkuPhoneApi30', 'senkuTabletApi30'], got ['senkuPhoneApi30']",
            errors,
        )

    def test_accepts_task_inventory_with_observed_expected_tasks(self):
        summary = make_summary()
        observed = summary["expected_gradle_task_names"] + [":app:unrelatedDebugAndroidTest"]
        summary["observed_gradle_task_names"] = observed
        summary["observed_expected_gradle_task_names"] = summary["expected_gradle_task_names"]
        summary["task_inventory_source"] = "path"
        summary["task_inventory"] = {
            "source": "path",
            "source_path": "tasks.txt",
            "gradle_invoked": False,
            "command": summary["planned_task_inventory_command"],
            "observed_gradle_task_names": observed,
            "observed_expected_gradle_task_names": summary["expected_gradle_task_names"],
        }

        _, errors = validate_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_rejects_task_inventory_mismatch(self):
        summary = make_summary()
        summary["observed_gradle_task_names"] = summary["expected_gradle_task_names"]
        summary["observed_expected_gradle_task_names"] = [
            ":app:senkuPhoneApi30DebugAndroidTest",
            ":app:unexpectedManagedTask",
        ]
        summary["task_inventory_source"] = "path"
        summary["task_inventory"] = {
            "source": "probe",
            "gradle_invoked": True,
            "command": ".\\gradlew.bat :app:tasks --all",
            "observed_gradle_task_names": [],
            "observed_expected_gradle_task_names": [],
        }

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.observed_expected_gradle_task_names to contain only expected "
            "managed-device tasks, got unexpected entries [':app:unexpectedManagedTask']",
            errors,
        )
        self.assertIn(
            "expected root.task_inventory.source to match root.task_inventory_source, "
            "got 'probe' and 'path'",
            errors,
        )
        self.assertIn(
            "expected root.task_inventory.command to be "
            "'.\\\\gradlew.bat :app:tasks --all -Psenku.enableManagedDevices=true --console=plain', "
            "got '.\\\\gradlew.bat :app:tasks --all'",
            errors,
        )
        self.assertIn(
            "expected root.task_inventory.observed_gradle_task_names to mirror "
            "root.observed_gradle_task_names",
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
        self.assertIn("android_managed_device_smoke_summary: ok", result.stdout)
        self.assertIn("evidence: non_acceptance", result.stdout)

    def test_cli_reports_failure(self):
        summary = make_summary()
        summary["managed_devices_launched"] = True
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
            "ERROR: expected root.managed_devices_launched to be False, got True",
            result.stdout,
        )


if __name__ == "__main__":
    unittest.main()
