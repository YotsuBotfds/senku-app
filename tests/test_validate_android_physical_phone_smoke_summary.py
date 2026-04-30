import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_physical_phone_smoke_summary import find_latest_summary, validate_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_physical_phone_smoke_summary.py"
RUNNER_PATH = REPO_ROOT / "scripts" / "run_android_physical_phone_smoke.ps1"


def make_summary(*, completed: bool = False) -> dict:
    return {
        "status": "completed" if completed else "dry_run_only",
        "dry_run": not completed,
        "physical_device": True,
        "launches_emulators": False,
        "serial_required_unless_dry_run": True,
        "serial": "R5CT123456A" if completed else None,
        "adb_path": "Android/Sdk/platform-tools/adb.exe",
        "adb_path_source": "override" if completed else "LOCALAPPDATA_ANDROID_SDK",
        "apk_path": "android-app/app/build/outputs/apk/debug/app-debug.apk",
        "package": "com.senku.mobile",
        "launch_activity": "com.senku.mobile/.MainActivity",
        "evidence": {
            "focus_path": "artifacts/bench/android_physical_phone_smoke/focus.txt",
            "screenshot_path": None,
            "dump_path": None,
            "logcat_path": None,
            "focus_contains_launch_activity": True if completed else None,
        },
        "commands": {
            "devices": "adb devices",
            "install": "adb -s R5CT123456A install --no-streaming -r app-debug.apk",
            "launch": "adb -s R5CT123456A shell am start -n com.senku.mobile/.MainActivity",
        },
        "summary_json": "artifacts/bench/android_physical_phone_smoke/summary.json",
        "summary_markdown": "artifacts/bench/android_physical_phone_smoke/summary.md",
        "started_at_utc": "2026-04-30T00:00:00.0000000Z",
        "finished_at_utc": "2026-04-30T00:00:00.0000000Z",
    }


def add_text_checks(summary: dict, *, missing: list[str] | None = None) -> dict:
    missing = [] if missing is None else missing
    summary["text_checks"] = {
        "requested": ["Senku", "Library"],
        "passed": ["Senku"] if missing else ["Senku", "Library"],
        "missing": missing,
    }
    return summary


class ValidateAndroidPhysicalPhoneSmokeSummaryTests(unittest.TestCase):
    def write_summary(self, payload: object) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_dry_run_planning_summary_passes(self):
        data, errors = validate_summary(self.write_summary(make_summary()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["status"], "dry_run_only")
        self.assertTrue(data["dry_run"])

    def test_valid_completed_physical_evidence_summary_passes(self):
        data, errors = validate_summary(self.write_summary(make_summary(completed=True)))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["status"], "completed")
        self.assertFalse(data["dry_run"])

    def test_valid_dry_run_with_requested_text_checks_passes(self):
        summary = make_summary()
        summary["text_checks"] = {
            "requested": ["Field manual", "Senku"],
            "passed": [],
            "missing": [],
        }

        data, errors = validate_summary(self.write_summary(summary))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["text_checks"]["requested"], ["Field manual", "Senku"])

    def test_valid_completed_text_checks_pass(self):
        data, errors = validate_summary(
            self.write_summary(add_text_checks(make_summary(completed=True)))
        )

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["text_checks"]["passed"], ["Senku", "Library"])

    def test_completed_text_checks_reject_missing_fragments(self):
        summary = add_text_checks(make_summary(completed=True), missing=["Library"])

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.text_checks.missing to be empty for completed summaries", errors)

    def test_text_checks_validate_shape_and_membership(self):
        summary = make_summary(completed=True)
        summary["text_checks"] = {
            "requested": ["Senku", ""],
            "passed": ["Senku", "Other"],
            "missing": [123],
        }

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.text_checks.requested[1] to be non-empty", errors)
        self.assertIn("expected root.text_checks.passed item to be requested: 'Other'", errors)
        self.assertIn("expected root.text_checks.missing[0] to be str, got int", errors)

    def test_completed_summary_requires_physical_contract_fields(self):
        summary = make_summary(completed=True)
        summary["dry_run"] = True
        summary["physical_device"] = False
        summary["launches_emulators"] = True
        summary["serial_required_unless_dry_run"] = False
        summary["serial"] = "emulator-5554"
        summary["package"] = "com.example.senku"
        summary["launch_activity"] = "com.example.senku/.MainActivity"
        summary["evidence"]["focus_contains_launch_activity"] = False
        summary["summary_json"] = ""
        summary["summary_markdown"] = ""

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.dry_run to be False, got True", errors)
        self.assertIn("expected root.physical_device to be True, got False", errors)
        self.assertIn("expected root.launches_emulators to be False, got True", errors)
        self.assertIn("expected root.serial_required_unless_dry_run to be True, got False", errors)
        self.assertIn("expected root.serial not to be an emulator serial, got 'emulator-5554'", errors)
        self.assertIn("expected root.package to be 'com.senku.mobile', got 'com.example.senku'", errors)
        self.assertIn(
            "expected root.launch_activity to be 'com.senku.mobile/.MainActivity', "
            "got 'com.example.senku/.MainActivity'",
            errors,
        )
        self.assertIn(
            "expected root.evidence.focus_contains_launch_activity to be True, got False",
            errors,
        )
        self.assertIn("expected root.summary_json to be non-empty", errors)
        self.assertIn("expected root.summary_markdown to be non-empty", errors)

    def test_completed_summary_requires_non_empty_serial(self):
        summary = make_summary(completed=True)
        summary["serial"] = " "

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.serial to be non-empty", errors)
        self.assertIn("expected root.serial to be non-empty for completed physical-phone smoke", errors)

    def test_dry_run_rejects_emulator_serial_and_real_focus_evidence(self):
        summary = make_summary()
        summary["serial"] = "emulator-5554"
        summary["evidence"]["focus_contains_launch_activity"] = True

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.serial not to be an emulator serial, got 'emulator-5554'", errors)
        self.assertIn(
            "expected root.evidence.focus_contains_launch_activity not to be true for dry-run summaries",
            errors,
        )

    def test_runner_dry_run_summary_passes_validator_without_phone(self):
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        output_dir = Path(temp_dir.name) / "out"

        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(RUNNER_PATH),
                "-OutputDir",
                str(output_dir),
                "-DryRun",
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        _, errors = validate_summary(output_dir / "summary.json")
        self.assertEqual(errors, [])

    def test_cli_reports_ok_for_completed_summary(self):
        path = self.write_summary(make_summary(completed=True))
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), "-B", str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("android_physical_phone_smoke_summary: ok", result.stdout)
        self.assertIn("status: completed", result.stdout)

    def test_find_latest_summary_uses_newest_timestamped_summary(self):
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        root = Path(temp_dir.name)
        older = root / "20260430_155113" / "summary.json"
        newer = root / "20260430_155156" / "summary.json"
        older.parent.mkdir()
        newer.parent.mkdir()
        older.write_text(json.dumps(make_summary()), encoding="utf-8")
        newer.write_text(json.dumps(make_summary(completed=True)), encoding="utf-8")
        os.utime(older, (1_777_575_000, 1_777_575_000))
        os.utime(newer, (1_777_575_100, 1_777_575_100))

        path, errors = find_latest_summary(root)

        self.assertEqual(errors, [])
        self.assertEqual(path, newer)

    def test_cli_latest_validates_newest_summary_without_phone(self):
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        root = Path(temp_dir.name)
        older = root / "20260430_155113" / "summary.json"
        newer = root / "20260430_155156" / "summary.json"
        older.parent.mkdir()
        newer.parent.mkdir()
        bad_summary = make_summary(completed=True)
        bad_summary["evidence"]["focus_contains_launch_activity"] = False
        older.write_text(json.dumps(bad_summary), encoding="utf-8")
        newer.write_text(json.dumps(make_summary(completed=True)), encoding="utf-8")
        os.utime(older, (1_777_575_000, 1_777_575_000))
        os.utime(newer, (1_777_575_100, 1_777_575_100))
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), "-B", str(SCRIPT_PATH), "--latest", "--summary-root", str(root)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("android_physical_phone_smoke_summary: ok", result.stdout)
        self.assertIn(str(newer), result.stdout)

    def test_cli_reports_failure(self):
        summary = make_summary(completed=True)
        summary["evidence"]["focus_contains_launch_activity"] = False
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
            "ERROR: expected root.evidence.focus_contains_launch_activity to be True, got False",
            result.stdout,
        )


if __name__ == "__main__":
    unittest.main()
