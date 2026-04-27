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


def make_non_acceptance_tooling_summary() -> dict:
    return {
        "status": "dry_run_only",
        "dry_run": True,
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "comparison_baseline": "fixed_four_emulator_matrix",
        "primary_evidence": "fixed_four_emulator_matrix",
        "stop_line": (
            "STOP: fixed four-emulator evidence remains primary; "
            "this Gradle Managed Devices smoke is non-acceptance evidence only."
        ),
        "task_name": ":app:senkuManagedSmoke",
    }


def make_fts_fallback_wrapper_summary() -> dict:
    return {
        "passed_count": 4,
        "failed_devices": [],
        "runtime_evidence": "fts4_fallback",
        "not_fts5_runtime_proof": True,
        "fts5_runtime_proof": False,
        "devices": [
            "emulator-5554",
            "emulator-5556",
            "emulator-5558",
            "emulator-5560",
        ],
        "host_adb_platform_tools_version": "36.0.0-13206524",
        "dry_run": False,
        "results": [
            {
                "device": "emulator-5554",
                "status": "passed",
                "passed": True,
                "runtime_evidence": "fts4_fallback",
                "dry_run": False,
            }
        ],
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

    def test_valid_non_acceptance_tooling_summary_passes(self):
        data, errors = validate_summary(self.write_summary(make_non_acceptance_tooling_summary()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["status"], "dry_run_only")

    def test_valid_fts_fallback_wrapper_summary_passes(self):
        data, errors = validate_summary(self.write_summary(make_fts_fallback_wrapper_summary()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["runtime_evidence"], "fts4_fallback")

    def test_legacy_fts_fallback_wrapper_summary_without_newer_fields_passes(self):
        summary = make_fts_fallback_wrapper_summary()
        del summary["not_fts5_runtime_proof"]
        del summary["fts5_runtime_proof"]
        del summary["host_adb_platform_tools_version"]

        _, errors = validate_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_fts_fallback_wrapper_summary_rejects_wrong_runtime_proof_fields(self):
        summary = make_fts_fallback_wrapper_summary()
        summary["not_fts5_runtime_proof"] = False
        summary["fts5_runtime_proof"] = True

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.not_fts5_runtime_proof to be true", errors)
        self.assertIn("expected root.fts5_runtime_proof to be false", errors)

    def test_fts_fallback_wrapper_summary_rejects_failed_device_outside_device_list(self):
        summary = make_fts_fallback_wrapper_summary()
        summary["failed_devices"] = ["emulator-9999"]

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.failed_devices to be a subset of root.devices, unknown: emulator-9999",
            errors,
        )

    def test_non_acceptance_tooling_summary_rejects_acceptance_evidence(self):
        summary = make_non_acceptance_tooling_summary()
        summary["acceptance_evidence"] = True

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.acceptance_evidence to be false", errors)

    def test_non_acceptance_tooling_summary_requires_fixed_four_baseline(self):
        summary = make_non_acceptance_tooling_summary()
        summary["comparison_baseline"] = "managed_device_smoke"
        summary["primary_evidence"] = "managed_device_smoke"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected root.comparison_baseline or root.primary_evidence "
            "to be 'fixed_four_emulator_matrix'",
            errors,
        )

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

    def test_valid_installed_pack_rollup_passes(self):
        summary = make_summary()
        summary["installed_pack"] = {
            "metadata_present": True,
            "matrix_homogeneous": True,
            "pack_format": "senku-mobile-pack-v2",
            "pack_version": 2,
            "answer_cards": 271,
            "devices": [
                {
                    "device": "emulator-5556",
                    "state_count": 2,
                    "metadata_count": 2,
                    "available_count": 2,
                    "statuses": ["available"],
                    "metadata_missing": False,
                    "metadata_conflict": False,
                }
            ],
        }

        _, errors = validate_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_malformed_installed_pack_rollup_fails(self):
        summary = make_summary()
        summary["installed_pack"] = {
            "metadata_present": "yes",
            "matrix_homogeneous": True,
            "devices": [
                {
                    "device": "emulator-5556",
                    "state_count": 1,
                    "metadata_count": 2,
                    "metadata_missing": False,
                }
            ],
        }

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn(
            "expected installed_pack.metadata_present to be bool, got str",
            errors,
        )
        self.assertIn(
            "missing installed_pack.devices[0].metadata_conflict",
            errors,
        )
        self.assertIn(
            "expected installed_pack.devices[0].metadata_count to be <= installed_pack.devices[0].state_count",
            errors,
        )

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
