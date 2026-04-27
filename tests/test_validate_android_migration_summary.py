import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_migration_summary import validate_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_migration_summary.py"


def make_summary() -> dict:
    return {
        "status": "pass",
        "total_states": 2,
        "pass_count": 2,
        "fail_count": 0,
        "platform_anr_count": 0,
        "matrix_homogeneous": True,
        "matrix_apk_sha": "apk-sha",
        "matrix_model_name": "gemma-4-e2b-it-litert",
        "matrix_model_sha": "model-sha",
        "devices": [
            {
                "device": "emulator-5556",
                "roles": ["phone_portrait"],
                "state_count": 2,
                "pass_count": 2,
                "fail_count": 0,
                "apk_sha": "apk-sha",
                "model_name": "gemma-4-e2b-it-litert",
                "model_sha": "model-sha",
                "identity_conflict": False,
                "identity_missing": False,
            }
        ],
        "extra_existing_state_pack_field": {"kept": True},
    }


class ValidateAndroidMigrationSummaryTests(unittest.TestCase):
    def write_summary(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_summary_passes_with_extra_fields(self):
        data, errors = validate_summary(self.write_summary(make_summary()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["status"], "pass")

    def test_missing_top_level_migration_field_fails(self):
        summary = make_summary()
        del summary["matrix_model_sha"]

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("missing root.matrix_model_sha", errors)

    def test_missing_device_evidence_field_fails(self):
        summary = make_summary()
        del summary["devices"][0]["identity_missing"]

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("missing devices[0].identity_missing", errors)

    def test_cli_reports_failure_without_emulator_work(self):
        summary = make_summary()
        del summary["devices"][0]["apk_sha"]
        path = self.write_summary(summary)
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 1)
        self.assertIn("ERROR: missing devices[0].apk_sha", result.stdout)


if __name__ == "__main__":
    unittest.main()
