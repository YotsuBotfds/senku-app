import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_large_data_litert_tablet_lane_summary import (
    validate_large_data_litert_tablet_lane_summary,
)


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_large_data_litert_tablet_lane_summary.py"


def make_summary(real_mode: bool = False) -> dict:
    dry_run = not real_mode
    return {
        "status": "pass" if real_mode else "dry_run_only",
        "dry_run": dry_run,
        "real_mode": real_mode,
        "real_mode_guard": {
            "required_switch": "-RealMode",
            "required_confirmation": "RUN_EMULATOR_5554_LARGE_LITERT_DATA",
            "confirmed": real_mode,
        },
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "ui_acceptance_evidence": False,
        "deploy_evidence": real_mode,
        "deploy_evidence_kind": "litert_model_push",
        "runtime_evidence": real_mode,
        "runtime_evidence_kind": "model_store_instrumentation",
        "evidence_boundary": "deploy/runtime only; not UI acceptance",
        "stop_line": (
            "STOP: large-data LiteRT tablet lane evidence is deploy/runtime evidence only; "
            "fixed four-emulator UI acceptance evidence remains primary."
        ),
        "primary_evidence": "fixed_four_emulator_matrix",
        "comparison_baseline": "fixed_four_emulator_matrix",
        "device": "emulator-5554",
        "role": "tablet_portrait",
        "launch_profile": "large-litert-data",
        "partition_size_mb": 8192,
        "package_name": "com.senku.mobile",
        "runtime_check": "model_store",
        "start_emulator_requested": False,
        "selected_roles": ["tablet_portrait"],
        "devices": ["emulator-5554"],
        "generated_utc": "2026-04-27T12:00:00.0000000Z",
        "child_artifacts": {
            "launch_profile_summary": "C:\\tmp\\launch_profile_summary.json",
            "push_summary": "C:\\tmp\\model_push_summary.json" if real_mode else None,
            "push_markdown": "C:\\tmp\\model_push_summary.md" if real_mode else None,
            "readiness_summary": None,
            "instrumentation_summary": (
                "C:\\tmp\\model_store_instrumentation_summary.json" if real_mode else None
            ),
        },
        "child_status": {
            "launch_profile_preflight_exit_code": 0,
            "emulator_start_exit_code": None,
            "model_push_exit_code": 0,
            "litert_readiness_exit_code": 0 if dry_run else None,
            "model_store_instrumentation_exit_code": 0 if real_mode else None,
        },
        "planned_commands": {
            "launch_profile_preflight": (
                "powershell -NoProfile -ExecutionPolicy Bypass -File start "
                "-Roles tablet_portrait -Mode read_only -LaunchProfile large-litert-data "
                "-Headless -PartitionSizeMb 8192 -SummaryPath out -WhatIf"
            ),
            "emulator_start": "",
            "model_push": "powershell push -Device emulator-5554 -PackageName com.senku.mobile"
            + (" -DryRun" if dry_run else " -RestartApp"),
            "litert_readiness": (
                "powershell readiness -DeviceMatrix large_litert_data_emulator_5554 -DryRun"
                if dry_run
                else ""
            ),
            "model_store_instrumentation": (
                "powershell ui -Device emulator-5554 -TestClass ModelFileStoreImportedModelTest"
                if real_mode
                else ""
            ),
        },
        "child_summaries": {
            "launch_profile": {
                "selected_lanes": [
                    {
                        "role": "tablet_portrait",
                        "serial": "emulator-5554",
                        "orientation": "portrait",
                    }
                ],
                "profile_metadata": {
                    "profile": "large-litert-data",
                    "expected_role": "tablet_portrait",
                    "expected_serial": "emulator-5554",
                    "acceptance_evidence": False,
                },
            },
            "model_push": {
                "device": "emulator-5554",
                "package_name": "com.senku.mobile",
                "dry_run": dry_run,
                "acceptance_evidence": False,
                "ui_acceptance_evidence": False,
            },
            "litert_readiness": {
                "package_name": "com.senku.mobile",
                "acceptance_evidence": False,
                "backend": {"name": "litert"},
                "request": {"mode": "single_prompt_smoke"},
            }
            if dry_run
            else None,
            "model_store_instrumentation": {
                "device": "emulator-5554",
                "acceptance_evidence": False,
                "ui_acceptance_evidence": False,
            }
            if real_mode
            else None,
        },
    }


class ValidateAndroidLargeDataLiteRtTabletLaneSummaryTests(unittest.TestCase):
    def write_summary(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_dry_run_summary_passes(self):
        data, errors = validate_large_data_litert_tablet_lane_summary(
            self.write_summary(make_summary())
        )

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["status"], "dry_run_only")

    def test_valid_real_mode_summary_passes_with_confirm_token_and_runtime_evidence(self):
        _, errors = validate_large_data_litert_tablet_lane_summary(
            self.write_summary(make_summary(real_mode=True))
        )

        self.assertEqual(errors, [])

    def test_rejects_ui_acceptance_claims(self):
        summary = make_summary()
        summary["acceptance_evidence"] = True
        summary["ui_acceptance_evidence"] = True
        summary["non_acceptance_evidence"] = False
        summary["child_summaries"]["model_push"]["ui_acceptance_evidence"] = True

        _, errors = validate_large_data_litert_tablet_lane_summary(self.write_summary(summary))

        self.assertIn("expected root.non_acceptance_evidence to be True", errors)
        self.assertIn("expected root.acceptance_evidence to be False", errors)
        self.assertIn("expected root.ui_acceptance_evidence to be False", errors)
        self.assertIn("expected child_summaries.model_push.ui_acceptance_evidence to be false", errors)

    def test_rejects_dry_run_that_claims_device_runtime_or_ui_acceptance(self):
        summary = make_summary()
        summary["status"] = "pass"
        summary["deploy_evidence"] = True
        summary["runtime_evidence"] = True
        summary["start_emulator_requested"] = True
        summary["child_status"]["model_store_instrumentation_exit_code"] = 0
        summary["planned_commands"]["model_store_instrumentation"] = "powershell ui -Device emulator-5554"

        _, errors = validate_large_data_litert_tablet_lane_summary(self.write_summary(summary))

        self.assertIn("expected root.status to be 'dry_run_only'", errors)
        self.assertIn("expected root.deploy_evidence to be False", errors)
        self.assertIn("expected root.runtime_evidence to be False", errors)
        self.assertIn("expected root.start_emulator_requested to be False", errors)
        self.assertIn(
            "expected child_status.model_store_instrumentation_exit_code to be null in dry run",
            errors,
        )
        self.assertIn("expected planned_commands.model_store_instrumentation to be empty in dry run", errors)

    def test_rejects_real_mode_without_confirm_token_shape(self):
        summary = make_summary(real_mode=True)
        summary["real_mode_guard"]["required_confirmation"] = "YES"
        summary["real_mode_guard"]["confirmed"] = False

        _, errors = validate_large_data_litert_tablet_lane_summary(self.write_summary(summary))

        self.assertIn(
            "expected real_mode_guard.required_confirmation to be 'RUN_EMULATOR_5554_LARGE_LITERT_DATA'",
            errors,
        )
        self.assertIn("expected real_mode_guard.confirmed to be True", errors)

    def test_rejects_wrong_lane_markers(self):
        summary = make_summary()
        summary["device"] = "emulator-5560"
        summary["role"] = "phone_landscape"
        summary["launch_profile"] = "default"
        summary["partition_size_mb"] = 2048
        summary["selected_roles"] = ["phone_landscape"]
        summary["devices"] = ["emulator-5560"]

        _, errors = validate_large_data_litert_tablet_lane_summary(self.write_summary(summary))

        self.assertIn("expected root.device to be 'emulator-5554'", errors)
        self.assertIn("expected root.role to be 'tablet_portrait'", errors)
        self.assertIn("expected root.launch_profile to be 'large-litert-data'", errors)
        self.assertIn("expected root.selected_roles to include tablet_portrait", errors)
        self.assertIn("expected root.devices to include emulator-5554", errors)

    def test_real_emulator_start_above_cli_max_must_be_blocked(self):
        summary = make_summary(real_mode=True)
        summary["start_emulator_requested"] = True
        summary["status"] = "pass"

        _, errors = validate_large_data_litert_tablet_lane_summary(self.write_summary(summary))

        self.assertIn("expected root.status to be 'blocked'", errors)
        self.assertIn("missing root.blocked_reason", errors)
        self.assertIn("missing root.required_path", errors)
        self.assertIn("missing root.cli_partition_size_max_mb", errors)

    def test_blocked_large_data_summary_passes(self):
        summary = make_summary(real_mode=True)
        summary["status"] = "blocked"
        summary["start_emulator_requested"] = True
        summary["blocked_reason"] = "emulator_cli_partition_size_max_2047"
        summary["required_path"] = "config_based_avd_data_partition"
        summary["cli_partition_size_max_mb"] = 2047
        summary["deploy_evidence"] = False
        summary["runtime_evidence"] = False
        summary["child_artifacts"]["push_summary"] = None
        summary["child_artifacts"]["instrumentation_summary"] = None
        summary["child_status"]["model_push_exit_code"] = None
        summary["child_status"]["model_store_instrumentation_exit_code"] = None
        summary["planned_commands"]["model_push"] = ""
        summary["planned_commands"]["model_store_instrumentation"] = ""
        summary["child_summaries"]["model_push"] = None
        summary["child_summaries"]["model_store_instrumentation"] = None

        _, errors = validate_large_data_litert_tablet_lane_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_rejects_child_pointer_and_role_drift(self):
        summary = make_summary(real_mode=True)
        summary["child_artifacts"]["launch_profile_summary"] = ""
        summary["child_artifacts"]["push_summary"] = None
        summary["child_artifacts"]["instrumentation_summary"] = None
        summary["child_summaries"]["launch_profile"]["selected_lanes"][0]["serial"] = "emulator-5560"
        summary["child_summaries"]["model_push"]["device"] = "emulator-5560"
        summary["child_summaries"]["model_store_instrumentation"]["device"] = "emulator-5560"

        _, errors = validate_large_data_litert_tablet_lane_summary(self.write_summary(summary))

        self.assertIn("expected child_artifacts.launch_profile_summary to be a non-empty path", errors)
        self.assertIn("expected child_artifacts.push_summary when deploy_evidence is true", errors)
        self.assertIn(
            "expected child_artifacts.instrumentation_summary when runtime_evidence is true",
            errors,
        )
        self.assertIn(
            "expected child_summaries.launch_profile.selected_lanes to include tablet_portrait emulator-5554",
            errors,
        )
        self.assertIn("expected child_summaries.model_push.device to be 'emulator-5554'", errors)
        self.assertIn(
            "expected child_summaries.model_store_instrumentation.device to be 'emulator-5554'",
            errors,
        )

    def test_cli_reports_failure_without_emulator(self):
        summary = make_summary()
        summary["planned_commands"]["model_push"] = "powershell push -Device emulator-5560 -DryRun"
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
        self.assertIn(
            "ERROR: expected planned_commands.model_push to include '-Device emulator-5554'",
            result.stdout,
        )


if __name__ == "__main__":
    unittest.main()
