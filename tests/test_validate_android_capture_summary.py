import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_capture_summary import validate_capture_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_capture_summary.py"


def make_capture_summary() -> dict:
    return {
        "schema_version": 1,
        "serial": "emulator-5556",
        "role": "phone_portrait",
        "orientation": "portrait",
        "apk_sha256": "a" * 64,
        "platform_tools_version": "36.0.0-13206524",
        "artifacts": {
            "screenshot": {
                "path": "artifacts/bench/android_capture/phone_portrait/screen.png",
                "sha256": "b" * 64,
            },
            "ui_dump": {
                "path": "artifacts/bench/android_capture/phone_portrait/window.xml",
                "sha256": "c" * 64,
            },
            "logcat": {
                "path": "artifacts/bench/android_capture/phone_portrait/logcat.txt",
                "sha256": "d" * 64,
            },
            "screenrecord": {
                "path": "artifacts/bench/android_capture/phone_portrait/session.mp4",
                "sha256": "e" * 64,
            },
        },
        "package_data_posture": {
            "cleared_before_capture": True,
            "restored_after_capture": False,
            "description": "fresh install with package data cleared before capture",
        },
        "model_identity": {
            "name": "gemma-4-e2b-it-litert",
            "sha256": "f" * 64,
        },
        "installed_pack_metadata": {
            "status": "available",
            "pack_format": "senku-mobile-pack-v2",
            "pack_version": 2,
            "answer_cards": 271,
        },
        "migration_metadata": {
            "status": "capture_only",
            "capture_summary_schema_version": 1,
            "android_layout_change": False,
            "large_data_lane_change": False,
            "reindex_required": False,
        },
        "evidence_posture": {
            "non_acceptance_evidence": True,
            "acceptance_evidence": False,
        },
        "extra_future_capture_field": {"kept": True},
    }


class ValidateAndroidCaptureSummaryTests(unittest.TestCase):
    def write_summary(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "capture_summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_capture_summary_passes_with_optional_screenrecord(self):
        data, errors = validate_capture_summary(self.write_summary(make_capture_summary()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["serial"], "emulator-5556")

    def test_valid_capture_summary_passes_without_optional_screenrecord(self):
        summary = make_capture_summary()
        del summary["artifacts"]["screenrecord"]

        _, errors = validate_capture_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_missing_canonical_capture_fields_fail(self):
        summary = make_capture_summary()
        del summary["apk_sha256"]
        del summary["artifacts"]["screenshot"]["sha256"]
        del summary["model_identity"]["sha256"]

        _, errors = validate_capture_summary(self.write_summary(summary))

        self.assertIn("missing root.apk_sha256", errors)
        self.assertIn("missing artifacts.screenshot.sha256", errors)
        self.assertIn("missing model_identity.sha256", errors)

    def test_acceptance_evidence_is_rejected(self):
        summary = make_capture_summary()
        summary["evidence_posture"]["non_acceptance_evidence"] = False
        summary["evidence_posture"]["acceptance_evidence"] = True

        _, errors = validate_capture_summary(self.write_summary(summary))

        self.assertIn("expected evidence_posture.non_acceptance_evidence to be true", errors)
        self.assertIn("expected evidence_posture.acceptance_evidence to be false", errors)

    def test_optional_migration_metadata_is_validated_when_present(self):
        summary = make_capture_summary()
        del summary["migration_metadata"]["status"]
        summary["migration_metadata"]["capture_summary_schema_version"] = 2
        summary["migration_metadata"]["reindex_required"] = "false"

        _, errors = validate_capture_summary(self.write_summary(summary))

        self.assertIn("missing migration_metadata.status", errors)
        self.assertIn(
            "expected migration_metadata.reindex_required to be bool, got str",
            errors,
        )
        self.assertIn(
            "expected migration_metadata.capture_summary_schema_version to match root.schema_version",
            errors,
        )

    def test_legacy_summary_without_migration_metadata_still_passes(self):
        summary = make_capture_summary()
        del summary["migration_metadata"]

        _, errors = validate_capture_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_cli_reports_missing_field_without_emulator_work(self):
        summary = make_capture_summary()
        del summary["artifacts"]["logcat"]
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
        self.assertIn("ERROR: missing artifacts.logcat", result.stdout)


if __name__ == "__main__":
    unittest.main()
