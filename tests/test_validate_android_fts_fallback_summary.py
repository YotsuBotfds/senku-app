import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_fts_fallback_summary import validate_fts_fallback_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_fts_fallback_summary.py"


def make_summary() -> dict:
    return {
        "passed_count": 1,
        "failed_devices": ["emulator-5556"],
        "failed_device_results": [
            {
                "device": "emulator-5556",
                "exit_code": 1,
                "command": "adb -s emulator-5556 shell am instrument -w ...",
            }
        ],
        "expected_tests": 3,
        "runtime_evidence": "fts4_fallback",
        "expected_fts_table": "pack_pages_fts4",
        "fts5_runtime_proof": False,
        "not_fts5_runtime_proof": True,
        "dry_run": False,
        "device_lock_posture": "required_per_device",
        "device_lock_used": True,
        "adb_path": r"C:\Users\tateb\AppData\Local\Android\Sdk\platform-tools\adb.exe",
        "host_adb_platform_tools_version": "36.0.0-13206524",
        "device_count": 2,
        "devices": ["emulator-5554", "emulator-5556"],
        "results": [
            {
                "device": "emulator-5554",
                "status": "passed",
                "passed": True,
                "expected_tests": 3,
                "runtime_evidence": "fts4_fallback",
                "expected_fts_table": "pack_pages_fts4",
                "fts5_runtime_proof": False,
                "not_fts5_runtime_proof": True,
                "dry_run": False,
            },
            {
                "device": "emulator-5556",
                "status": "failed",
                "passed": False,
                "expected_tests": 3,
                "runtime_evidence": "fts4_fallback",
                "expected_fts_table": "pack_pages_fts4",
                "fts5_runtime_proof": False,
                "not_fts5_runtime_proof": True,
                "dry_run": False,
            },
        ],
    }


class ValidateAndroidFtsFallbackSummaryTests(unittest.TestCase):
    def write_summary(self, payload: object) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_summary_passes(self):
        data, errors = validate_fts_fallback_summary(self.write_summary(make_summary()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["runtime_evidence"], "fts4_fallback")

    def test_results_are_optional_when_rollup_is_consistent(self):
        summary = make_summary()
        del summary["results"]

        _, errors = validate_fts_fallback_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_rejects_wrong_runtime_proof_fields(self):
        summary = make_summary()
        summary["runtime_evidence"] = "fts5"
        summary["not_fts5_runtime_proof"] = False
        summary["fts5_runtime_proof"] = True

        _, errors = validate_fts_fallback_summary(self.write_summary(summary))

        self.assertIn("expected root.runtime_evidence to be 'fts4_fallback'", errors)
        self.assertIn("expected root.not_fts5_runtime_proof to be true", errors)
        self.assertIn("expected root.fts5_runtime_proof to be false", errors)

    def test_rejects_bad_host_and_lock_shape(self):
        summary = make_summary()
        summary["adb_path"] = ""
        summary["host_adb_platform_tools_version"] = ["36.0.0"]
        summary["device_lock_posture"] = ""
        summary["device_lock_used"] = "yes"

        _, errors = validate_fts_fallback_summary(self.write_summary(summary))

        self.assertIn("expected root.adb_path to be non-empty", errors)
        self.assertIn("expected root.host_adb_platform_tools_version to be str, got list", errors)
        self.assertIn("expected root.device_lock_posture to be non-empty", errors)
        self.assertIn("expected root.device_lock_used to be bool, got str", errors)

    def test_rejects_unknown_lock_posture_and_malformed_platform_tools_version(self):
        summary = make_summary()
        summary["device_lock_posture"] = "maybe_locked"
        summary["host_adb_platform_tools_version"] = "Android Debug Bridge version 1.0.41"

        _, errors = validate_fts_fallback_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.device_lock_posture to be one of "
            "not_acquired_dry_run, required_per_device, skipped_by_flag",
            errors,
        )
        self.assertIn(
            "expected root.host_adb_platform_tools_version to be 'dry_run' "
            "or a platform-tools version like 36.0.0-13206524",
            errors,
        )

    def test_rejects_count_mismatches(self):
        summary = make_summary()
        summary["device_count"] = 3
        summary["passed_count"] = 2

        _, errors = validate_fts_fallback_summary(self.write_summary(summary))

        self.assertIn("expected root.device_count to match len(root.devices)", errors)
        self.assertIn(
            "expected root.passed_count plus len(root.failed_devices) to match len(root.devices)",
            errors,
        )

    def test_rejects_failed_device_result_mismatch(self):
        summary = make_summary()
        summary["failed_devices"] = ["emulator-5554"]

        _, errors = validate_fts_fallback_summary(self.write_summary(summary))

        self.assertIn("expected root.failed_device_results devices to match root.failed_devices", errors)
        self.assertIn("expected failed root.results devices to match root.failed_devices", errors)

    def test_rejects_failed_device_outside_device_list(self):
        summary = make_summary()
        summary["failed_devices"] = ["emulator-9999"]
        summary["failed_device_results"][0]["device"] = "emulator-9999"

        _, errors = validate_fts_fallback_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.failed_devices to be a subset of root.devices, unknown: emulator-9999",
            errors,
        )

    def test_rejects_bad_embedded_result_status_shape(self):
        summary = make_summary()
        summary["results"][0]["status"] = "ok"
        summary["results"][1]["passed"] = True
        summary["results"][1]["fts5_runtime_proof"] = True

        _, errors = validate_fts_fallback_summary(self.write_summary(summary))

        self.assertIn("expected root.results[0].status to be one of failed, passed", errors)
        self.assertIn("expected root.results[1].passed to be false when status is failed", errors)
        self.assertIn("expected root.results[1].fts5_runtime_proof to be false", errors)

    def test_cli_reports_success_without_emulator_work(self):
        path = self.write_summary(make_summary())
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("android_fts_fallback_summary: ok", result.stdout)
        self.assertIn("failed_count: 1", result.stdout)


if __name__ == "__main__":
    unittest.main()
