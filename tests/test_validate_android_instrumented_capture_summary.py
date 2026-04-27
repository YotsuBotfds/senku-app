import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_instrumented_capture_summary import (
    validate_instrumented_capture_summary,
)


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_instrumented_capture_summary.py"


def make_instrumented_capture_summary() -> dict:
    return {
        "schema_version": 1,
        "serial": "emulator-5554",
        "role": "phone_portrait",
        "orientation": "portrait",
        "apk_sha256": "a" * 64,
        "platform_tools_version": "36.0.0-13206524",
        "artifacts": {
            "screenshot": {
                "path": "artifacts/bench/android_instrumented_ui_smoke/screenshots/first.png",
                "sha256": "b" * 64,
            },
            "ui_dump": {
                "path": "artifacts/bench/android_instrumented_ui_smoke/dumps/first.xml",
                "sha256": "c" * 64,
            },
            "logcat": {
                "path": "artifacts/bench/android_instrumented_ui_smoke/logcat.txt",
                "sha256": "d" * 64,
            },
        },
        "package_data_posture": {
            "cleared_before_capture": False,
            "restored_after_capture": False,
            "description": "instrumented smoke preserves caller/device package data unless setup steps change it",
        },
        "model_identity": {
            "name": "gemma-4-e2b-it-litert",
            "sha256": "e" * 64,
        },
        "installed_pack_metadata": {
            "status": "available",
            "pack_format": "senku-mobile-pack-v2",
            "pack_version": 2,
            "metadata": {
                "source": "run_android_instrumented_ui_smoke.ps1",
            },
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
    }


class ValidateAndroidInstrumentedCaptureSummaryTests(unittest.TestCase):
    def write_summary(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "capture_summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_capture_summary_path_output_passes_without_emulator(self):
        data, errors = validate_instrumented_capture_summary(
            self.write_summary(make_instrumented_capture_summary())
        )

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["role"], "phone_portrait")

    def test_requires_canonical_root_and_nested_metadata_fields(self):
        summary = make_instrumented_capture_summary()
        del summary["platform_tools_version"]
        del summary["package_data_posture"]["description"]
        del summary["installed_pack_metadata"]["pack_format"]

        _, errors = validate_instrumented_capture_summary(self.write_summary(summary))

        self.assertIn("missing root.platform_tools_version", errors)
        self.assertIn("missing package_data_posture.description", errors)
        self.assertIn("missing installed_pack_metadata.pack_format", errors)

    def test_required_artifact_paths_and_hashes_are_mandatory(self):
        summary = make_instrumented_capture_summary()
        del summary["artifacts"]["screenshot"]["path"]
        del summary["artifacts"]["ui_dump"]["sha256"]
        del summary["artifacts"]["logcat"]

        _, errors = validate_instrumented_capture_summary(self.write_summary(summary))

        self.assertIn("missing artifacts.screenshot.path", errors)
        self.assertIn("missing artifacts.ui_dump.sha256", errors)
        self.assertIn("missing artifacts.logcat", errors)

    def test_rejects_acceptance_evidence_posture(self):
        summary = make_instrumented_capture_summary()
        summary["evidence_posture"]["non_acceptance_evidence"] = False
        summary["evidence_posture"]["acceptance_evidence"] = True

        _, errors = validate_instrumented_capture_summary(self.write_summary(summary))

        self.assertIn("expected evidence_posture.non_acceptance_evidence to be true", errors)
        self.assertIn("expected evidence_posture.acceptance_evidence to be false", errors)

    def test_optional_migration_metadata_is_validated_when_present(self):
        summary = make_instrumented_capture_summary()
        del summary["migration_metadata"]["large_data_lane_change"]
        summary["migration_metadata"]["capture_summary_schema_version"] = 2

        _, errors = validate_instrumented_capture_summary(self.write_summary(summary))

        self.assertIn("missing migration_metadata.large_data_lane_change", errors)
        self.assertIn(
            "expected migration_metadata.capture_summary_schema_version to match root.schema_version",
            errors,
        )

    def test_legacy_capture_summary_path_without_migration_metadata_still_passes(self):
        summary = make_instrumented_capture_summary()
        del summary["migration_metadata"]

        _, errors = validate_instrumented_capture_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_cli_reports_contract_errors_without_emulator(self):
        summary = make_instrumented_capture_summary()
        del summary["artifacts"]["logcat"]["sha256"]
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
        self.assertIn("ERROR: missing artifacts.logcat.sha256", result.stdout)


if __name__ == "__main__":
    unittest.main()
