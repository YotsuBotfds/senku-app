import hashlib
import json
import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_large_data_litert_tablet_lane.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"
SUMMARY_VALIDATOR_SCRIPT = (
    REPO_ROOT / "scripts" / "validate_android_large_data_litert_tablet_lane_summary.py"
)
VALIDATION_PYTHON = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"


class AndroidLargeDataLiteRtTabletLaneContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def run_with_fake_sdk(self, *args):
        sdk_dir = Path(tempfile.mkdtemp(prefix="fake_android_sdk_"))
        self.addCleanup(lambda: shutil.rmtree(sdk_dir, ignore_errors=True))
        emulator_dir = sdk_dir / "emulator"
        adb_dir = sdk_dir / "platform-tools"
        emulator_dir.mkdir(parents=True)
        adb_dir.mkdir(parents=True)
        (emulator_dir / "emulator.exe").write_text("", encoding="utf-8")
        (adb_dir / "adb.exe").write_text("", encoding="utf-8")
        env = os.environ.copy()
        env["ANDROID_SDK_ROOT"] = str(sdk_dir)

        return subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(SCRIPT),
                *args,
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
            env=env,
        )

    def make_fake_avd_home(self):
        temp_dir = Path(tempfile.mkdtemp(prefix="large_litert_avd_home_"))
        self.addCleanup(lambda: shutil.rmtree(temp_dir, ignore_errors=True))
        avd_home = temp_dir / "avd"
        avd_root = avd_home / "Senku_Tablet_2.avd"
        avd_root.mkdir(parents=True)
        (avd_home / "Senku_Tablet_2.ini").write_text(
            f"avd.ini.encoding=UTF-8\npath={avd_root}\ntarget=android-36.1\n",
            encoding="utf-8",
        )
        (avd_root / "config.ini").write_text(
            "\n".join(
                [
                    "target=android-36.1",
                    "abi.type=x86_64",
                    "hw.lcd.width=1600",
                    "hw.lcd.height=2560",
                    "disk.dataPartition.size=20G",
                    "",
                ]
            ),
            encoding="utf-8",
        )
        return avd_home

    def test_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_large_data_litert_tablet_lane.ps1",
                "-SkipAnalyzer",
                "-SkipPester",
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)

    def test_script_is_guarded_to_5554_large_data_real_mode(self):
        self.assertIn('[string]$Device = "emulator-5554"', self.script)
        self.assertIn("[int]$PartitionSizeMb = 8192", self.script)
        self.assertIn("$emulatorCliPartitionSizeMaxMb = 2047", self.script)
        self.assertIn('$blockedReason = "emulator_cli_partition_size_max_2047"', self.script)
        self.assertIn("[switch]$RealMode", self.script)
        self.assertIn("[switch]$UsePreparedAvdDataPartition", self.script)
        self.assertIn('[string]$PreparedAvdPreflightAvdHome = ""', self.script)
        self.assertIn('[string]$ConfirmRealMode = ""', self.script)
        self.assertIn('$confirmationToken = "RUN_EMULATOR_5554_LARGE_LITERT_DATA"', self.script)
        self.assertIn('if ($Device -ne "emulator-5554")', self.script)
        self.assertIn('if ($PartitionSizeMb -lt 8192)', self.script)
        self.assertIn("Real mode requires -ConfirmRealMode", self.script)
        self.assertIn('"large-litert-data"', self.script)
        self.assertIn('"tablet_portrait"', self.script)
        self.assertIn(
            '"com.senku.mobile.ModelFileStoreImportedModelTest#importedLiteRtModelIsDiscoverableAndNonEmpty"',
            self.script,
        )
        self.assertIn("push_litert_model_to_android.ps1", self.script)
        self.assertIn("prepare_senku_tablet_2_large_data_avd.ps1", self.script)
        self.assertIn("run_android_litert_readiness_matrix.ps1", self.script)
        self.assertIn("start_senku_emulator_matrix.ps1", self.script)
        self.assertIn("run_android_instrumented_ui_smoke.ps1", self.script)
        self.assertIn("validate_android_large_data_litert_tablet_lane_summary.py", self.script)
        self.assertIn("validation_commands", self.script)
        self.assertIn("ui_acceptance_evidence = $false", self.script)
        self.assertIn("use_prepared_avd_data_partition", self.script)
        self.assertIn("data_partition_source", self.script)
        self.assertIn("cli_partition_size_argument_used", self.script)
        self.assertIn('if ($RealMode -and $StartEmulator -and $PartitionSizeMb -gt $emulatorCliPartitionSizeMaxMb -and -not $UsePreparedAvdDataPartition)', self.script)
        self.assertIn("deploy/runtime evidence only", self.script)
        self.assertIn('status = "blocked"', self.script)

    def test_real_mode_requires_explicit_confirmation_before_running_helpers(self):
        with tempfile.TemporaryDirectory(prefix="large_litert_guard_") as temp_dir:
            result = self.run_with_fake_sdk(
                "-OutputDir",
                str(Path(temp_dir) / "out"),
                "-RealMode",
            )

        output = result.stdout + result.stderr
        self.assertNotEqual(result.returncode, 0, output)
        self.assertIn("Real mode requires -ConfirmRealMode RUN_EMULATOR_5554_LARGE_LITERT_DATA", output)

    def test_dry_run_writes_child_summaries_and_top_level_non_acceptance_boundary(self):
        payload = b"tiny tablet litert model\n"
        expected_hash = hashlib.sha256(payload).hexdigest()

        with tempfile.TemporaryDirectory(prefix="large_litert_lane_") as temp_dir:
            temp_path = Path(temp_dir)
            model_path = temp_path / "gemma-4-E2B-it.task"
            model_path.write_bytes(payload)
            output_dir = temp_path / "out"

            result = self.run_with_fake_sdk(
                "-OutputDir",
                str(output_dir),
                "-ModelPath",
                str(model_path),
                "-PackageName",
                "com.example.senku",
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("fixed four-emulator UI acceptance evidence remains primary", result.stdout)
            summary_path = output_dir / "summary.json"
            markdown_path = output_dir / "summary.md"
            launch_path = output_dir / "launch_profile_summary.json"
            push_path = output_dir / "model_push_summary.json"
            readiness_path = output_dir / "readiness" / "summary.json"
            self.assertTrue(summary_path.exists())
            self.assertTrue(markdown_path.exists())
            self.assertTrue(launch_path.exists())
            self.assertTrue(push_path.exists())
            self.assertTrue(readiness_path.exists())

            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
            markdown = markdown_path.read_text(encoding="utf-8-sig")
            launch = json.loads(launch_path.read_text(encoding="utf-8-sig"))
            push = json.loads(push_path.read_text(encoding="utf-8-sig"))
            readiness = json.loads(readiness_path.read_text(encoding="utf-8-sig"))

            validator = subprocess.run(
                [str(VALIDATION_PYTHON), str(SUMMARY_VALIDATOR_SCRIPT), str(summary_path)],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

        self.assertEqual(summary["status"], "dry_run_only")
        self.assertTrue(summary["dry_run"])
        self.assertFalse(summary["real_mode"])
        self.assertTrue(summary["non_acceptance_evidence"])
        self.assertFalse(summary["acceptance_evidence"])
        self.assertFalse(summary["ui_acceptance_evidence"])
        self.assertFalse(summary["deploy_evidence"])
        self.assertFalse(summary["runtime_evidence"])
        self.assertEqual(summary["deploy_evidence_kind"], "litert_model_push")
        self.assertEqual(summary["runtime_evidence_kind"], "model_store_instrumentation")
        self.assertEqual(summary["primary_evidence"], "fixed_four_emulator_matrix")
        self.assertEqual(summary["comparison_baseline"], "fixed_four_emulator_matrix")
        self.assertEqual(summary["device"], "emulator-5554")
        self.assertEqual(summary["role"], "tablet_portrait")
        self.assertEqual(summary["launch_profile"], "large-litert-data")
        self.assertEqual(summary["partition_size_mb"], 8192)
        self.assertEqual(summary["data_partition_source"], "emulator_cli_partition_size")
        self.assertFalse(summary["use_prepared_avd_data_partition"])
        self.assertFalse(summary["cli_partition_size_argument_used"])
        self.assertEqual(summary["package_name"], "com.example.senku")
        self.assertFalse(summary["start_emulator_requested"])
        self.assertEqual(summary["devices"], ["emulator-5554"])
        self.assertIn("deploy/runtime evidence only", summary["stop_line"])
        self.assertIn("fixed four-emulator UI acceptance evidence remains primary", summary["stop_line"])
        self.assertEqual(summary["child_status"]["launch_profile_preflight_exit_code"], 0)
        self.assertIsNone(summary["child_status"]["prepared_avd_preflight_exit_code"])
        self.assertEqual(summary["child_status"]["model_push_exit_code"], 0)
        self.assertEqual(summary["child_status"]["litert_readiness_exit_code"], 0)
        self.assertIsNone(summary["child_status"]["model_store_instrumentation_exit_code"])
        self.assertIn("-WhatIf", summary["planned_commands"]["launch_profile_preflight"])
        self.assertIn("-DryRun", summary["planned_commands"]["model_push"])
        self.assertIn("-DryRun", summary["planned_commands"]["litert_readiness"])
        self.assertIn("large-litert-data", summary["planned_commands"]["launch_profile_preflight"])
        self.assertEqual(len(summary["validation_commands"]), 1)
        validation_command = summary["validation_commands"][0]
        self.assertEqual(
            validation_command["name"],
            "validate_large_data_litert_tablet_lane_summary",
        )
        self.assertIn(
            "validate_android_large_data_litert_tablet_lane_summary.py",
            validation_command["command"],
        )
        self.assertIn(str(summary_path), validation_command["command"])
        self.assertEqual(validation_command["validates"], "summary.json")
        self.assertEqual(Path(validation_command["summary_json_path"]), summary_path)
        self.assertTrue(validation_command["plan_only"])
        self.assertFalse(validation_command["will_start_jobs"])
        self.assertFalse(validation_command["will_touch_emulators"])
        self.assertIn("does not start jobs", validation_command["note"])
        self.assertIn("touch emulators", validation_command["note"])
        self.assertIn("create UI acceptance evidence", validation_command["note"])

        self.assertEqual(launch["profile_metadata"]["profile"], "large-litert-data")
        self.assertEqual(launch["profile_metadata"]["cli_partition_size_max_mb"], 2047)
        self.assertFalse(launch["profile_metadata"]["cli_partition_size_supported"])
        self.assertEqual(launch["profile_metadata"]["expected_serial"], "emulator-5554")
        self.assertEqual(launch["selected_lanes"][0]["serial"], "emulator-5554")
        self.assertEqual(launch["selected_lanes"][0]["emulator_args"][-2:], ["-partition-size", "8192"])
        self.assertTrue(push["transfer_skipped"])
        self.assertEqual(push["model_name"], "gemma-4-E2B-it.task")
        self.assertEqual(push["model_sha256"], expected_hash)
        self.assertEqual(push["app_private_target_path"], "/data/user/0/com.example.senku/files/models/gemma-4-E2B-it.task")
        self.assertEqual(readiness["status"], "dry_run_only")
        self.assertEqual(readiness["backend"]["readiness_matrix"], "large_litert_data_emulator_5554")
        self.assertEqual(readiness["model"]["sha256"], expected_hash)

        self.assertIn("# Android Large-Data LiteRT Tablet Lane", markdown)
        self.assertIn("Real mode: false", markdown)
        self.assertIn("- UI acceptance evidence: false", markdown)
        self.assertIn("- Runtime evidence: false", markdown)
        self.assertIn("## Validation Commands", markdown)
        self.assertIn("validate_large_data_litert_tablet_lane_summary:", markdown)
        self.assertIn("validate_android_large_data_litert_tablet_lane_summary.py", markdown)
        self.assertIn("validates: summary.json", markdown)
        self.assertIn("will_touch_emulators: False", markdown)
        self.assertTrue(markdown.rstrip().endswith(summary["stop_line"]))

        self.assertEqual(validator.returncode, 0, validator.stderr + validator.stdout)
        self.assertIn("android_large_data_litert_tablet_lane_summary: ok", validator.stdout)
        self.assertIn("status: dry_run_only", validator.stdout)
        self.assertIn("evidence: deploy/runtime only, non_acceptance", validator.stdout)

    def test_prepared_avd_dry_run_adds_config_preflight_boundary(self):
        payload = b"tiny tablet litert model\n"

        with tempfile.TemporaryDirectory(prefix="large_litert_prepared_") as temp_dir:
            temp_path = Path(temp_dir)
            model_path = temp_path / "gemma-4-E2B-it.task"
            model_path.write_bytes(payload)
            avd_home = self.make_fake_avd_home()
            output_dir = temp_path / "out"

            result = self.run_with_fake_sdk(
                "-OutputDir",
                str(output_dir),
                "-ModelPath",
                str(model_path),
                "-UsePreparedAvdDataPartition",
                "-PreparedAvdPreflightAvdHome",
                str(avd_home),
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            summary_path = output_dir / "summary.json"
            prepared_summary_path = output_dir / "prepared_avd_preflight_summary.json"
            self.assertTrue(summary_path.exists())
            self.assertTrue(prepared_summary_path.exists())
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
            prepared = json.loads(prepared_summary_path.read_text(encoding="utf-8-sig"))
            launch = json.loads((output_dir / "launch_profile_summary.json").read_text(encoding="utf-8-sig"))
            validator = subprocess.run(
                [str(VALIDATION_PYTHON), str(SUMMARY_VALIDATOR_SCRIPT), str(summary_path)],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

        self.assertEqual(summary["status"], "dry_run_only")
        self.assertTrue(summary["dry_run"])
        self.assertTrue(summary["use_prepared_avd_data_partition"])
        self.assertEqual(summary["data_partition_source"], "config_based_avd_data_partition")
        self.assertEqual(summary["required_path"], "config_based_avd_data_partition")
        self.assertFalse(summary["cli_partition_size_argument_used"])
        self.assertEqual(summary["child_status"]["prepared_avd_preflight_exit_code"], 0)
        self.assertEqual(Path(summary["child_artifacts"]["prepared_avd_preflight_summary"]), prepared_summary_path)
        self.assertNotIn("-PartitionSizeMb", summary["planned_commands"]["launch_profile_preflight"])
        self.assertNotIn("-partition-size", launch["selected_lanes"][0]["emulator_args"])
        self.assertEqual(launch["profile_metadata"]["data_partition_source"], "prepared_avd_config_required")
        self.assertFalse(launch["profile_metadata"]["cli_partition_size_argument_supported"])
        self.assertEqual(
            summary["child_summaries"]["prepared_avd_preflight"]["summary_kind"],
            "senku_tablet_2_large_data_avd_preflight",
        )
        self.assertEqual(prepared["status"], "dry_run_only")
        self.assertEqual(prepared["avd_name"], "Senku_Tablet_2")
        self.assertFalse(prepared["acceptance_evidence"])
        self.assertFalse(prepared["deploy_evidence"])
        self.assertEqual(validator.returncode, 0, validator.stderr + validator.stdout)

    def test_real_mode_start_emulator_blocks_impossible_cli_partition_size_with_summary(self):
        payload = b"tiny tablet litert model\n"

        with tempfile.TemporaryDirectory(prefix="large_litert_blocked_") as temp_dir:
            temp_path = Path(temp_dir)
            model_path = temp_path / "gemma-4-E2B-it.task"
            model_path.write_bytes(payload)
            output_dir = temp_path / "out"

            result = self.run_with_fake_sdk(
                "-OutputDir",
                str(output_dir),
                "-ModelPath",
                str(model_path),
                "-RealMode",
                "-ConfirmRealMode",
                "RUN_EMULATOR_5554_LARGE_LITERT_DATA",
                "-StartEmulator",
            )

            summary_path = output_dir / "summary.json"
            markdown_path = output_dir / "summary.md"
            self.assertTrue(summary_path.exists(), result.stderr + result.stdout)
            self.assertTrue(markdown_path.exists(), result.stderr + result.stdout)
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
            markdown = markdown_path.read_text(encoding="utf-8-sig")
            validator = subprocess.run(
                [str(VALIDATION_PYTHON), str(SUMMARY_VALIDATOR_SCRIPT), str(summary_path)],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

        self.assertEqual(result.returncode, 2, result.stderr + result.stdout)
        self.assertIn("emulator CLI -partition-size max is 2047 MB", result.stdout)
        self.assertEqual(summary["status"], "blocked")
        self.assertFalse(summary["dry_run"])
        self.assertTrue(summary["real_mode"])
        self.assertTrue(summary["real_mode_guard"]["confirmed"])
        self.assertEqual(summary["blocked_reason"], "emulator_cli_partition_size_max_2047")
        self.assertEqual(summary["required_path"], "config_based_avd_data_partition")
        self.assertEqual(summary["cli_partition_size_max_mb"], 2047)
        self.assertEqual(summary["data_partition_source"], "emulator_cli_partition_size")
        self.assertFalse(summary["use_prepared_avd_data_partition"])
        self.assertTrue(summary["cli_partition_size_argument_used"])
        self.assertTrue(summary["non_acceptance_evidence"])
        self.assertFalse(summary["acceptance_evidence"])
        self.assertFalse(summary["ui_acceptance_evidence"])
        self.assertFalse(summary["deploy_evidence"])
        self.assertFalse(summary["runtime_evidence"])
        self.assertTrue(summary["start_emulator_requested"])
        self.assertIsNone(summary["child_status"]["model_push_exit_code"])
        self.assertEqual(summary["child_status"]["launch_profile_preflight_exit_code"], 0)
        self.assertIn("-PartitionSizeMb 8192", summary["planned_commands"]["emulator_start"])
        self.assertEqual(summary["planned_commands"]["model_push"], "")
        self.assertIn("Status: blocked", markdown)
        self.assertEqual(validator.returncode, 0, validator.stderr + validator.stdout)
        self.assertIn("status: blocked", validator.stdout)


if __name__ == "__main__":
    unittest.main()
