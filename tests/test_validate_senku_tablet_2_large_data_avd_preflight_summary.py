import json
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
VALIDATOR = REPO_ROOT / "scripts" / "validate_senku_tablet_2_large_data_avd_preflight_summary.py"
VALIDATION_PYTHON = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"


class ValidateSenkuTablet2LargeDataAvdPreflightSummaryTests(unittest.TestCase):
    def valid_summary(self):
        return {
            "summary_kind": "senku_tablet_2_large_data_avd_preflight",
            "schema_version": 1,
            "status": "dry_run_only",
            "blocked_reason": None,
            "dry_run": True,
            "apply": False,
            "non_acceptance_evidence": True,
            "acceptance_evidence": False,
            "ui_acceptance_evidence": False,
            "deploy_evidence": False,
            "runtime_evidence": False,
            "evidence_boundary": "AVD maintenance preflight only; not deploy/runtime or UI acceptance evidence",
            "required_path": "config_based_avd_data_partition",
            "cli_partition_size_max_mb": 2047,
            "confirmation_token": "PREPARE_SENKU_TABLET_2_LARGE_DATA_AVD",
            "confirmation_matched": False,
            "avd_name": "Senku_Tablet_2",
            "expected_serial": "emulator-5554",
            "avd_home": "C:/Users/tateb/.android/avd",
            "avd_ini_path": "C:/Users/tateb/.android/avd/Senku_Tablet_2.ini",
            "avd_path": "C:/Users/tateb/.android/avd/Senku_Tablet_2.avd",
            "config_path": "C:/Users/tateb/.android/avd/Senku_Tablet_2.avd/config.ini",
            "desired_data_partition_size": "20G",
            "current_data_partition_size": "6G",
            "config_identity": {
                "target": "android-36.1",
                "abi_type": "x86_64",
                "hw_lcd_width": "1600",
                "hw_lcd_height": "2560",
                "checks": {
                    "avd_name": True,
                    "target": True,
                    "abi_type": True,
                    "width": True,
                    "height": True,
                },
                "verified": True,
            },
            "running_device_check": {
                "adb_path": "C:/Android/platform-tools/adb.exe",
                "status": "checked",
                "expected_serial_running": False,
                "skipped": False,
            },
            "planned_destructive_actions": {
                "update_config_disk_data_partition_size": True,
                "quarantine_stale_userdata_and_snapshots": [
                    {
                        "path": "C:/Users/tateb/.android/avd/Senku_Tablet_2.avd/userdata-qemu.img.qcow2",
                        "name": "userdata-qemu.img.qcow2",
                        "type": "file",
                        "bytes": 7850000000,
                    }
                ],
            },
            "destructive_actions_performed": False,
            "config_updated": False,
            "backup_dir": None,
            "backup_manifest_path": None,
            "quarantine_dir": None,
            "quarantined_items": [],
            "next_command": "powershell -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\run_android_large_data_litert_tablet_lane.ps1 -Device emulator-5554",
            "stop_line": "This prepares AVD data capacity only. It is non-acceptance evidence and does not replace fixed four-emulator state-pack proof.",
            "generated_utc": "2026-04-27T19:30:00Z",
        }

    def run_validator(self, payload):
        with tempfile.TemporaryDirectory(prefix="avd_preflight_validator_") as temp_dir:
            summary_path = Path(temp_dir) / "summary.json"
            summary_path.write_text(json.dumps(payload), encoding="utf-8")
            return subprocess.run(
                [str(VALIDATION_PYTHON), str(VALIDATOR), str(summary_path)],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

    def test_validator_accepts_valid_dry_run_summary(self):
        result = self.run_validator(self.valid_summary())

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("senku_tablet_2_large_data_avd_preflight_summary: ok", result.stdout)
        self.assertIn("status: dry_run_only", result.stdout)
        self.assertIn("non_acceptance", result.stdout)

    def test_validator_rejects_acceptance_claims(self):
        payload = self.valid_summary()
        payload["acceptance_evidence"] = True

        result = self.run_validator(payload)

        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("expected root.acceptance_evidence to be false", result.stdout)

    def test_validator_rejects_wrong_avd_identity(self):
        payload = self.valid_summary()
        payload["avd_name"] = "Other_AVD"

        result = self.run_validator(payload)

        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("expected root.avd_name to be 'Senku_Tablet_2'", result.stdout)


if __name__ == "__main__":
    unittest.main()
