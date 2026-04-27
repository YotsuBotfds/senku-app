import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_migration_preflight_bundle_summary import (
    validate_android_migration_preflight_bundle_summary,
)


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_migration_preflight_bundle_summary.py"


def make_step(name: str) -> dict:
    return {
        "name": name,
        "command": f"run {name}",
        "exit_code": 0,
        "status": "pass",
        "summary_path": f"artifacts/{name}/summary.json",
        "markdown_path": f"artifacts/{name}/summary.md",
        "stdout_path": f"artifacts/{name}/stdout.txt",
        "started_at_utc": "2026-04-27T00:00:00.0000000Z",
        "finished_at_utc": "2026-04-27T00:00:01.0000000Z",
    }


def make_validator(name: str) -> dict:
    return {
        "name": name,
        "command": f"python scripts/{name}.py artifacts/{name}/summary.json",
        "target": f"artifacts/{name}/summary.json",
        "exit_code": 0,
        "status": "pass",
        "stdout_path": f"artifacts/{name}/stdout.txt",
    }


def make_summary() -> dict:
    return {
        "bundle_kind": "android_migration_preflight_bundle",
        "schema_version": 1,
        "status": "pass",
        "metadata_only": True,
        "preflight_only": True,
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "emulator_required": False,
        "devices_touched": False,
        "adb_required": False,
        "output_dir": "artifacts/bench/android_migration_preflight_bundle",
        "fixtures": {
            "tiny_model_path": "artifacts/bench/android_migration_preflight_bundle/fixtures/tiny_litert_fixture.task",
            "task_inventory_path": "artifacts/bench/android_migration_preflight_bundle/fixtures/managed_device_tasks.txt",
            "harness_run_file": "artifacts/bench/android_migration_preflight_bundle/fixtures/harness_matrix_runs.jsonl",
        },
        "steps": [
            make_step("tooling_version_manifest"),
            make_step("managed_device_smoke_dry_run"),
            make_step("litert_readiness_dry_run"),
            make_step("senku_tablet_2_large_data_avd_preflight"),
            make_step("orchestrator_smoke_dry_run"),
            make_step("uiautomator_24_comparison_dry_run"),
            make_step("harness_matrix_plan_only"),
            make_step("ui_state_pack_plan_only"),
            make_step("migration_proof_summarizer_json"),
            make_step("migration_proof_summarizer_markdown"),
        ],
        "validation_commands": [
            make_validator("validate_tooling_version_manifest"),
            make_validator("validate_managed_device_smoke"),
            make_validator("validate_litert_readiness"),
            make_validator("validate_senku_tablet_2_large_data_avd_preflight"),
            make_validator("validate_orchestrator_smoke"),
            make_validator("validate_uiautomator_24_comparison"),
            make_validator("validate_harness_matrix_plan"),
            make_validator("validate_ui_state_pack_plan"),
        ],
        "migration_summarizer": {
            "json_path": "artifacts/bench/android_migration_preflight_bundle/migration_rows.json",
            "markdown_path": "artifacts/bench/android_migration_preflight_bundle/migration_rows.md",
            "inputs": [
                "artifacts/bench/android_migration_preflight_bundle/managed_device_smoke/summary.json",
                "artifacts/bench/android_migration_preflight_bundle/litert_readiness/summary.json",
                "artifacts/bench/android_migration_preflight_bundle/orchestrator_smoke/summary.json",
                "artifacts/bench/android_migration_preflight_bundle/harness_matrix_plan/summary.json",
            ],
        },
        "self_validation": {
            "name": "validate_migration_preflight_bundle_summary",
            "command": "python scripts/validate_android_migration_preflight_bundle_summary.py artifacts/bench/android_migration_preflight_bundle/summary.json",
            "target": "artifacts/bench/android_migration_preflight_bundle/summary.json",
            "status": "pass",
            "exit_code": 0,
            "stdout_path": "artifacts/bench/android_migration_preflight_bundle/validation_validate_migration_preflight_bundle_summary/stdout.txt",
        },
        "started_at_utc": "2026-04-27T00:00:00.0000000Z",
        "finished_at_utc": "2026-04-27T00:00:02.0000000Z",
    }


class ValidateAndroidMigrationPreflightBundleSummaryTests(unittest.TestCase):
    def write_summary(self, payload) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_summary_passes(self):
        data, errors = validate_android_migration_preflight_bundle_summary(
            self.write_summary(make_summary())
        )

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["bundle_kind"], "android_migration_preflight_bundle")

    def test_rejects_acceptance_or_emulator_posture(self):
        summary = make_summary()
        summary["acceptance_evidence"] = True
        summary["emulator_required"] = True
        summary["devices_touched"] = True
        summary["adb_required"] = True

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertIn("expected root.acceptance_evidence to be false", errors)
        self.assertIn("expected root.emulator_required to be false", errors)
        self.assertIn("expected root.devices_touched to be false", errors)
        self.assertIn("expected root.adb_required to be false", errors)

    def test_rejects_non_boolean_posture_flags(self):
        summary = make_summary()
        summary["metadata_only"] = "true"

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertIn("expected root.metadata_only to be bool, got str", errors)

    def test_rejects_missing_child_lane(self):
        summary = make_summary()
        summary["steps"] = [
            step for step in summary["steps"] if step["name"] != "litert_readiness_dry_run"
        ]

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertIn("missing root.steps lane 'litert_readiness_dry_run'", errors)

    def test_rejects_missing_validator_lane(self):
        summary = make_summary()
        summary["validation_commands"] = [
            item
            for item in summary["validation_commands"]
            if item["name"] != "validate_harness_matrix_plan"
        ]

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertIn(
            "missing root.validation_commands validator 'validate_harness_matrix_plan'",
            errors,
        )

    def test_migration_summarizer_is_optional_but_validated_when_emitted(self):
        summary = make_summary()
        del summary["migration_summarizer"]
        summary["steps"] = [
            step
            for step in summary["steps"]
            if step["name"] not in {"migration_proof_summarizer_json", "migration_proof_summarizer_markdown"}
        ]

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_rejects_migration_summarizer_without_proof_steps(self):
        summary = make_summary()
        summary["steps"] = [
            step
            for step in summary["steps"]
            if step["name"] not in {"migration_proof_summarizer_json", "migration_proof_summarizer_markdown"}
        ]

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertIn(
            "expected migration proof summary steps when root.migration_summarizer is emitted",
            errors,
        )

    def test_rejects_malformed_migration_summarizer_inputs(self):
        summary = make_summary()
        summary["migration_summarizer"]["inputs"] = []

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertIn("expected migration_summarizer.inputs to be non-empty", errors)

    def test_rejects_missing_or_failed_self_validation_record(self):
        summary = make_summary()
        del summary["self_validation"]

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertIn("expected root.self_validation to be dict, got NoneType", errors)

        summary = make_summary()
        summary["self_validation"]["status"] = "fail"
        summary["self_validation"]["exit_code"] = 1

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertIn("expected self_validation.status to be 'pass'", errors)
        self.assertIn("expected self_validation.exit_code to be 0", errors)

    def test_allows_pending_self_validation_only_when_explicit(self):
        summary = make_summary()
        summary["self_validation"]["status"] = "not_run"
        summary["self_validation"]["exit_code"] = None

        _, errors = validate_android_migration_preflight_bundle_summary(self.write_summary(summary))

        self.assertIn("expected self_validation.status to be 'pass'", errors)
        self.assertIn("expected self_validation.exit_code to be 0", errors)

        _, errors = validate_android_migration_preflight_bundle_summary(
            self.write_summary(summary),
            allow_pending_self_validation=True,
        )

        self.assertEqual(errors, [])

    def test_cli_reports_failure_without_emulator_work(self):
        summary = make_summary()
        del summary["fixtures"]["tiny_model_path"]
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
        self.assertIn("ERROR: missing fixtures.tiny_model_path", result.stdout)


if __name__ == "__main__":
    unittest.main()
