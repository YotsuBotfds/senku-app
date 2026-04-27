import os
import json
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "start_senku_emulator_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class StartSenkuEmulatorMatrixContractTests(unittest.TestCase):
    def run_with_fake_sdk(self, *args):
        with tempfile.TemporaryDirectory(prefix="fake_android_sdk_") as temp_dir:
            sdk_root = Path(temp_dir)
            emulator_dir = sdk_root / "emulator"
            adb_dir = sdk_root / "platform-tools"
            emulator_dir.mkdir(parents=True)
            adb_dir.mkdir(parents=True)
            (emulator_dir / "emulator.exe").write_text("", encoding="utf-8")
            (adb_dir / "adb.exe").write_text("", encoding="utf-8")

            env = os.environ.copy()
            env["ANDROID_SDK_ROOT"] = str(sdk_root)

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

    def parse_profile_metadata(self, output):
        lines = output.splitlines()
        marker_index = lines.index("Launch profile metadata:")
        return json.loads(lines[marker_index + 1])

    def test_whatif_prints_5554_read_only_headless_large_data_launch_args(self):
        with tempfile.TemporaryDirectory(prefix="fake_android_sdk_") as temp_dir:
            sdk_root = Path(temp_dir)
            emulator_dir = sdk_root / "emulator"
            adb_dir = sdk_root / "platform-tools"
            emulator_dir.mkdir(parents=True)
            adb_dir.mkdir(parents=True)
            (emulator_dir / "emulator.exe").write_text("", encoding="utf-8")
            (adb_dir / "adb.exe").write_text("", encoding="utf-8")

            env = os.environ.copy()
            env["ANDROID_SDK_ROOT"] = str(sdk_root)

            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-Roles",
                    "tablet_portrait",
                    "-Mode",
                    "read_only",
                    "-Headless",
                    "-PartitionSizeMb",
                    "8192",
                    "-WhatIf",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
                env=env,
            )

        output = result.stdout + result.stderr
        self.assertEqual(result.returncode, 0, output)
        self.assertIn("tablet_portrait: emulator-5554 / Senku_Tablet_2 / 1600x2560", output)
        self.assertIn("-avd Senku_Tablet_2 -port 5554", output)
        self.assertIn("-read-only -no-snapshot-load -no-snapshot-save", output)
        self.assertIn("-no-window", output)
        self.assertIn("-partition-size 8192", output)
        self.assertIn("What if:", output)
        self.assertIn("emulator-5554 (Senku_Tablet_2)", output)
        self.assertIn("Start emulator in read_only mode", output)

    def test_whatif_large_litert_profile_prints_metadata_without_changing_launch_args(self):
        result = self.run_with_fake_sdk(
            "-Roles",
            "tablet_portrait",
            "-LaunchProfile",
            "large-litert-data",
            "-WhatIf",
        )

        output = result.stdout + result.stderr
        self.assertEqual(result.returncode, 0, output)
        metadata = self.parse_profile_metadata(output)
        self.assertEqual(metadata["profile"], "large-litert-data")
        self.assertTrue(metadata["preflight_only"])
        self.assertTrue(metadata["whatif_metadata_only"])
        self.assertTrue(metadata["non_acceptance_evidence"])
        self.assertFalse(metadata["acceptance_evidence"])
        self.assertTrue(metadata["headless"])
        self.assertEqual(metadata["partition_size_mb"], 8192)
        self.assertEqual(metadata["data_partition_source"], "prepared_avd_config_required")
        self.assertEqual(metadata["cli_partition_size_max_mb"], 2047)
        self.assertFalse(metadata["cli_partition_size_supported"])
        self.assertFalse(metadata["cli_partition_size_argument_supported"])
        self.assertEqual(metadata["expected_role"], "tablet_portrait")
        self.assertEqual(metadata["expected_serial"], "emulator-5554")
        self.assertIn("LiteRT", metadata["data_sizing"])
        self.assertIn("snapshot", metadata["snapshot_cache_posture"])
        self.assertIn("args: -avd Senku_Tablet_2 -port 5554 -read-only -no-snapshot-load -no-snapshot-save", output)
        self.assertNotIn("-partition-size 8192", output)
        self.assertNotIn("-no-window", output)

    def test_whatif_accepts_named_profile_metadata_contracts(self):
        expected = {
            "clean-headless": {
                "headless": True,
                "partition_size_mb": None,
                "expected_serial": "emulator-5556",
            },
            "cached-local": {
                "headless": False,
                "partition_size_mb": None,
                "expected_serial": "emulator-5556",
            },
                "large-litert-data": {
                    "headless": True,
                    "partition_size_mb": 8192,
                    "data_partition_source": "prepared_avd_config_required",
                    "cli_partition_size_max_mb": 2047,
                    "cli_partition_size_supported": False,
                    "cli_partition_size_argument_supported": False,
                    "expected_serial": "emulator-5554",
                },
        }
        for profile, profile_expected in expected.items():
            with self.subTest(profile=profile):
                result = self.run_with_fake_sdk(
                    "-Roles",
                    "all",
                    "-LaunchProfile",
                    profile,
                    "-WhatIf",
                )

                output = result.stdout + result.stderr
                self.assertEqual(result.returncode, 0, output)
                metadata = self.parse_profile_metadata(output)
                self.assertEqual(metadata["profile"], profile)
                self.assertEqual(metadata["headless"], profile_expected["headless"])
                self.assertEqual(metadata["partition_size_mb"], profile_expected["partition_size_mb"])
                if profile == "large-litert-data":
                    self.assertEqual(
                        metadata["data_partition_source"],
                        profile_expected["data_partition_source"],
                    )
                    self.assertEqual(
                        metadata["cli_partition_size_max_mb"],
                        profile_expected["cli_partition_size_max_mb"],
                    )
                    self.assertEqual(
                        metadata["cli_partition_size_supported"],
                        profile_expected["cli_partition_size_supported"],
                    )
                    self.assertEqual(
                        metadata["cli_partition_size_argument_supported"],
                        profile_expected["cli_partition_size_argument_supported"],
                    )
                self.assertEqual(metadata["expected_serial"], profile_expected["expected_serial"])
                self.assertTrue(metadata["non_acceptance_evidence"])
                self.assertFalse(metadata["acceptance_evidence"])

    def test_whatif_launch_profile_writes_machine_readable_summary(self):
        with tempfile.TemporaryDirectory(prefix="senku_profile_summary_") as temp_dir:
            summary_path = Path(temp_dir) / "nested" / "summary.json"
            result = self.run_with_fake_sdk(
                "-Roles",
                "tablet_portrait",
                "-LaunchProfile",
                "large-litert-data",
                "-Headless",
                "-PartitionSizeMb",
                "8192",
                "-SummaryPath",
                str(summary_path),
                "-WhatIf",
            )

            output = result.stdout + result.stderr
            self.assertEqual(result.returncode, 0, output)
            self.assertTrue(summary_path.exists(), output)
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))

        self.assertTrue(summary["non_acceptance_evidence"])
        self.assertFalse(summary["acceptance_evidence"])
        self.assertTrue(summary["preflight_only"])
        self.assertEqual(summary["profile_metadata"]["profile"], "large-litert-data")
        self.assertTrue(summary["profile_metadata"]["preflight_only"])
        self.assertTrue(summary["profile_metadata"]["non_acceptance_evidence"])
        self.assertFalse(summary["profile_metadata"]["acceptance_evidence"])
        self.assertEqual(len(summary["selected_lanes"]), 1)
        lane = summary["selected_lanes"][0]
        self.assertEqual(lane["role"], "tablet_portrait")
        self.assertEqual(lane["serial"], "emulator-5554")
        self.assertEqual(
            lane["emulator_args"],
            [
                "-avd",
                "Senku_Tablet_2",
                "-port",
                "5554",
                "-read-only",
                "-no-snapshot-load",
                "-no-snapshot-save",
                "-no-window",
                "-partition-size",
                "8192",
            ],
        )

    def test_summary_path_requires_launch_profile(self):
        with tempfile.TemporaryDirectory(prefix="senku_profile_summary_") as temp_dir:
            summary_path = Path(temp_dir) / "summary.json"
            result = self.run_with_fake_sdk(
                "-SummaryPath",
                str(summary_path),
                "-WhatIf",
            )

            output = result.stdout + result.stderr
            self.assertNotEqual(result.returncode, 0, output)
            self.assertIn("requires -LaunchProfile", output)
            self.assertFalse(summary_path.exists(), output)

    def test_launch_profile_is_rejected_outside_whatif(self):
        result = self.run_with_fake_sdk(
            "-LaunchProfile",
            "clean-headless",
        )

        output = result.stdout + result.stderr
        self.assertNotEqual(result.returncode, 0, output)
        self.assertIn("must be used with -WhatIf", output)

    def test_real_launch_rejects_partition_size_above_cli_limit_before_starting(self):
        result = self.run_with_fake_sdk(
            "-Roles",
            "tablet_portrait",
            "-PartitionSizeMb",
            "8192",
        )

        output = result.stdout + result.stderr
        self.assertNotEqual(result.returncode, 0, output)
        self.assertIn("exceeds the observed emulator CLI -partition-size maximum of 2047 MB", output)
        self.assertNotIn("Started emulator-5554", output)

    def test_whatif_prints_only_selected_role_serials(self):
        with tempfile.TemporaryDirectory(prefix="fake_android_sdk_") as temp_dir:
            sdk_root = Path(temp_dir)
            emulator_dir = sdk_root / "emulator"
            adb_dir = sdk_root / "platform-tools"
            emulator_dir.mkdir(parents=True)
            adb_dir.mkdir(parents=True)
            (emulator_dir / "emulator.exe").write_text("", encoding="utf-8")
            (adb_dir / "adb.exe").write_text("", encoding="utf-8")

            env = os.environ.copy()
            env["ANDROID_SDK_ROOT"] = str(sdk_root)

            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-Command",
                    f"& '{SCRIPT}' -Roles phone_portrait,tablet_landscape -WhatIf",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
                env=env,
            )

        output = result.stdout + result.stderr
        self.assertEqual(result.returncode, 0, output)
        self.assertIn("emulator-5556", output)
        self.assertIn("emulator-5558", output)
        self.assertNotIn("emulator-5560", output)
        self.assertNotIn("emulator-5554", output)

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
                "scripts\\start_senku_emulator_matrix.ps1",
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


if __name__ == "__main__":
    unittest.main()
