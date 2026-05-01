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
        "apk_sha256": "1" * 64 if completed else None,
        "device_identity": {
            "serial": "R5CT123456A",
            "manufacturer": "Google",
            "model": "Pixel 8",
            "device": "shiba",
            "product": "shiba",
            "build_fingerprint": "google/shiba/shiba:15/AP3A/test:user/release-keys",
            "sdk": "35",
        }
        if completed
        else None,
        "package": "com.senku.mobile",
        "launch_activity": "com.senku.mobile/.MainActivity",
        "evidence": {
            "focus_path": "artifacts/bench/android_physical_phone_smoke/focus.txt",
            "screenshot_path": "artifacts/bench/android_physical_phone_smoke/screenshot.png"
            if completed
            else None,
            "dump_path": "artifacts/bench/android_physical_phone_smoke/uiautomator.xml"
            if completed
            else None,
            "logcat_path": None,
            "focus_contains_launch_activity": True if completed else None,
            "focused_package": "com.senku.mobile" if completed else None,
            "orientation": {
                "source": "dumpsys input",
                "raw": "SurfaceOrientation: 0" if completed else None,
                "rotation": 0 if completed else None,
                "orientation": "portrait" if completed else None,
            },
            "artifact_hashes": {
                "focus_sha256": "2" * 64 if completed else None,
                "screenshot_sha256": "3" * 64 if completed else None,
                "dump_sha256": "4" * 64 if completed else None,
                "logcat_sha256": None,
            },
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


def add_interaction(summary: dict, *, statuses: list[str] | None = None) -> dict:
    statuses = statuses or ["success", "success", "success", "success", "success"]
    names = ["tap_saved", "tap_query_field", "enter_query", "submit_query", "back"]
    post_check_steps = {"tap_saved", "submit_query", "back"}
    steps = []
    for name, status in zip(names, statuses):
        step = {"name": name, "status": status}
        if status == "failed":
            step["message"] = "not found"
        if summary["status"] == "completed" and status == "success" and name in post_check_steps:
            step["post_check"] = {
                "passed": True,
                "expected_any_text": ["Saved" if name != "submit_query" else "Search"],
                "matched_text": ["Saved" if name != "submit_query" else "Search"],
                "ui_text_sample": ["Saved", "Search"],
                "dump_length": 128,
                "dump_sha256": "a" * 64,
                "captured_at_utc": "2026-04-30T00:00:01.0000000Z",
            }
        steps.append(step)
    summary["interaction"] = {
        "enabled": True,
        "query": "boil water",
        "steps": steps,
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

    def test_valid_completed_interaction_steps_pass(self):
        data, errors = validate_summary(
            self.write_summary(add_interaction(make_summary(completed=True)))
        )

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["interaction"]["steps"][0]["status"], "success")

    def test_valid_dry_run_interaction_steps_are_skipped(self):
        summary = add_interaction(make_summary(), statuses=["skipped"] * 5)

        data, errors = validate_summary(self.write_summary(summary))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["interaction"]["steps"][0]["status"], "skipped")

    def test_completed_interaction_steps_reject_failure_status(self):
        summary = add_interaction(
            make_summary(completed=True),
            statuses=["success", "failed", "success", "success", "success"],
        )

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.interaction.steps[1].status to be success for completed summaries",
            errors,
        )

    def test_completed_interaction_failed_post_check_payload_keeps_diagnostics(self):
        summary = add_interaction(
            make_summary(completed=True),
            statuses=["success", "success", "success", "failed", "success"],
        )
        summary["interaction"]["steps"][3]["post_check"] = {
            "passed": False,
            "expected_any_text": ["boil water", "Answer", "Results", "Search", "Ask"],
            "matched_text": [],
            "ui_text_sample": ["No matching result"],
            "dump_length": 96,
            "dump_sha256": "b" * 64,
            "captured_at_utc": "2026-04-30T00:00:02.0000000Z",
            "step_name": "submit_query",
        }
        summary["interaction"]["last_post_check"] = summary["interaction"]["steps"][3]["post_check"]

        _, errors = validate_summary(self.write_summary(summary))

        self.assertEqual(
            errors,
            ["expected root.interaction.steps[3].status to be success for completed summaries"],
        )

    def test_completed_interaction_requires_post_step_ui_evidence(self):
        summary = add_interaction(make_summary(completed=True))
        del summary["interaction"]["steps"][0]["post_check"]
        summary["interaction"]["steps"][3]["post_check"]["passed"] = False
        summary["interaction"]["steps"][4]["post_check"]["matched_text"] = []

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.interaction.steps[0].post_check for completed 'tap_saved'",
            errors,
        )
        self.assertIn(
            "expected root.interaction.steps[3].post_check.passed to be True",
            errors,
        )
        self.assertIn(
            "expected root.interaction.steps[4].post_check.matched_text to be non-empty",
            errors,
        )

    def test_post_step_ui_evidence_validates_shape(self):
        summary = add_interaction(make_summary(completed=True))
        post_check = summary["interaction"]["steps"][0]["post_check"]
        post_check["expected_any_text"] = []
        post_check["matched_text"] = ["Other"]
        post_check["dump_length"] = 0
        post_check["dump_sha256"] = "not-a-sha"
        post_check["step_name"] = "unknown"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.interaction.steps[0].post_check.expected_any_text to be non-empty",
            errors,
        )
        self.assertIn(
            "expected root.interaction.steps[0].post_check.matched_text item to be expected: 'Other'",
            errors,
        )
        self.assertIn("expected root.interaction.steps[0].post_check.dump_length to be positive", errors)
        self.assertIn(
            "expected root.interaction.steps[0].post_check.dump_sha256 to be a lowercase sha256 hex digest",
            errors,
        )
        self.assertIn(
            "expected root.interaction.steps[0].post_check.step_name to be a known interaction step, got 'unknown'",
            errors,
        )

    def test_interaction_steps_validate_shape(self):
        summary = add_interaction(make_summary(completed=True))
        summary["interaction"]["steps"] = [
            {"name": "tap_saved", "status": "success"},
            {"name": "unknown", "status": "wat"},
        ]

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.interaction.steps[1].name to be a known interaction step, got 'unknown'",
            errors,
        )
        self.assertIn(
            "expected root.interaction.steps[1].status to be success|failed|skipped, got 'wat'",
            errors,
        )
        self.assertIn(
            "expected root.interaction.steps to include: tap_query_field, enter_query, submit_query, back",
            errors,
        )

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

    def test_completed_summary_requires_screenshot_and_dump_hashes(self):
        summary = make_summary(completed=True)
        summary["evidence"]["screenshot_path"] = None
        summary["evidence"]["dump_path"] = " "
        summary["evidence"]["artifact_hashes"]["screenshot_sha256"] = None
        summary["evidence"]["artifact_hashes"]["dump_sha256"] = "not-a-sha"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.evidence.screenshot_path for completed summaries", errors)
        self.assertIn("expected root.evidence.dump_path for completed summaries", errors)
        self.assertIn(
            "expected root.evidence.artifact_hashes.screenshot_sha256 for completed summaries",
            errors,
        )
        self.assertIn(
            "expected root.evidence.artifact_hashes.dump_sha256 to be a lowercase sha256 hex digest",
            errors,
        )

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
