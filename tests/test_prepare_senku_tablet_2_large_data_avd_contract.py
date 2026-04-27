import json
import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "prepare_senku_tablet_2_large_data_avd.ps1"
VALIDATOR = REPO_ROOT / "scripts" / "validate_senku_tablet_2_large_data_avd_preflight_summary.py"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"
VALIDATION_PYTHON = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"


class PrepareSenkuTablet2LargeDataAvdContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def make_fake_avd_home(self, *, adb_devices_output: str = "List of devices attached\n\n"):
        temp_dir = Path(tempfile.mkdtemp(prefix="senku_tablet2_avd_"))
        self.addCleanup(lambda: shutil.rmtree(temp_dir, ignore_errors=True))
        avd_home = temp_dir / "avd"
        avd_root = avd_home / "Senku_Tablet_2.avd"
        avd_root.mkdir(parents=True)
        (avd_home / "Senku_Tablet_2.ini").write_text(
            f"avd.ini.encoding=UTF-8\npath={avd_root}\npath.rel=avd/Senku_Tablet_2.avd\ntarget=android-36.1\n",
            encoding="utf-8",
        )
        (avd_root / "config.ini").write_text(
            "\n".join(
                [
                    "target=android-36.1",
                    "abi.type=x86_64",
                    "hw.lcd.width=1600",
                    "hw.lcd.height=2560",
                    "disk.dataPartition.size=6G",
                    "",
                ]
            ),
            encoding="utf-8",
        )
        (avd_root / "userdata-qemu.img.qcow2").write_bytes(b"fake-qcow2")
        (avd_root / "snapshots").mkdir()
        (avd_root / "snapshots" / "default_boot").write_text("snapshot", encoding="utf-8")

        sdk_root = temp_dir / "sdk"
        adb_dir = sdk_root / "platform-tools"
        adb_dir.mkdir(parents=True)
        adb = adb_dir / "adb.exe"
        adb.write_text(
            "@echo off\r\n"
            f"echo {adb_devices_output.splitlines()[0] if adb_devices_output.splitlines() else 'List of devices attached'}\r\n"
            + "".join(f"echo {line}\r\n" for line in adb_devices_output.splitlines()[1:]),
            encoding="utf-8",
        )
        env = os.environ.copy()
        env["ANDROID_SDK_ROOT"] = str(sdk_root)
        return temp_dir, avd_home, avd_root, env

    def run_script(self, env, *args):
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
                "scripts\\prepare_senku_tablet_2_large_data_avd.ps1",
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

    def test_script_declares_guarded_non_acceptance_avd_maintenance_lane(self):
        self.assertIn("PREPARE_SENKU_TABLET_2_LARGE_DATA_AVD", self.script)
        self.assertIn("Senku_Tablet_2", self.script)
        self.assertIn("emulator-5554", self.script)
        self.assertIn("config_based_avd_data_partition", self.script)
        self.assertIn("cli_partition_size_max_mb", self.script)
        self.assertIn("non_acceptance_evidence", self.script)
        self.assertIn("acceptance_evidence", self.script)
        self.assertIn("destructive_actions_performed", self.script)
        self.assertIn("quarantine_stale_userdata_and_snapshots", self.script)
        self.assertIn("fixed four-emulator state-pack proof", self.script)

    def test_dry_run_writes_summary_without_mutating_avd(self):
        temp_dir, avd_home, avd_root, env = self.make_fake_avd_home()
        output_dir = temp_dir / "out"

        result = self.run_script(
            env,
            "-AvdHome",
            str(avd_home),
            "-OutputDir",
            str(output_dir),
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Senku Tablet 2 large-data AVD preflight: dry_run_only", result.stdout)
        summary_path = output_dir / "summary.json"
        markdown_path = output_dir / "summary.md"
        self.assertTrue(summary_path.exists())
        self.assertTrue(markdown_path.exists())

        summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
        validator = subprocess.run(
            [str(VALIDATION_PYTHON), str(VALIDATOR), str(summary_path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(summary["status"], "dry_run_only")
        self.assertTrue(summary["dry_run"])
        self.assertFalse(summary["apply"])
        self.assertTrue(summary["non_acceptance_evidence"])
        self.assertFalse(summary["acceptance_evidence"])
        self.assertFalse(summary["ui_acceptance_evidence"])
        self.assertFalse(summary["deploy_evidence"])
        self.assertFalse(summary["runtime_evidence"])
        self.assertEqual(summary["required_path"], "config_based_avd_data_partition")
        self.assertEqual(summary["prepared_lane_flag"], "-UsePreparedAvdDataPartition")
        self.assertEqual(summary["cli_partition_size_max_mb"], 2047)
        self.assertEqual(summary["avd_name"], "Senku_Tablet_2")
        self.assertEqual(summary["expected_serial"], "emulator-5554")
        self.assertEqual(summary["current_data_partition_size"], "6G")
        self.assertEqual(summary["desired_data_partition_size"], "20G")
        self.assertTrue(summary["config_identity"]["verified"])
        self.assertEqual(summary["config_identity"]["target"], "android-36.1")
        self.assertEqual(summary["config_identity"]["abi_type"], "x86_64")
        self.assertFalse(summary["destructive_actions_performed"])
        self.assertFalse(summary["config_updated"])
        planned_items = summary["planned_destructive_actions"]["quarantine_stale_userdata_and_snapshots"]
        self.assertEqual({item["name"] for item in planned_items}, {"userdata-qemu.img.qcow2", "snapshots"})
        self.assertIn("run_android_large_data_litert_tablet_lane.ps1", summary["next_command"])
        self.assertIn("-UsePreparedAvdDataPartition", summary["next_command"])
        self.assertIn("fixed four-emulator state-pack proof", summary["stop_line"])
        self.assertIn("- acceptance_evidence: False", markdown_path.read_text(encoding="utf-8-sig"))
        self.assertIn(
            "disk.dataPartition.size=6G",
            (avd_root / "config.ini").read_text(encoding="utf-8-sig"),
        )
        self.assertTrue((avd_root / "userdata-qemu.img.qcow2").exists())
        self.assertTrue((avd_root / "snapshots").exists())
        self.assertEqual(validator.returncode, 0, validator.stderr + validator.stdout)
        self.assertIn("senku_tablet_2_large_data_avd_preflight_summary: ok", validator.stdout)

    def test_apply_requires_confirmation_before_mutation(self):
        temp_dir, avd_home, avd_root, env = self.make_fake_avd_home()

        result = self.run_script(
            env,
            "-AvdHome",
            str(avd_home),
            "-OutputDir",
            str(temp_dir / "out"),
            "-Apply",
        )

        output = result.stdout + result.stderr
        self.assertNotEqual(result.returncode, 0, output)
        self.assertIn("Apply mode requires -ConfirmPrepare PREPARE_SENKU_TABLET_2_LARGE_DATA_AVD", output)
        self.assertIn("disk.dataPartition.size=6G", (avd_root / "config.ini").read_text(encoding="utf-8-sig"))
        self.assertTrue((avd_root / "userdata-qemu.img.qcow2").exists())

    def test_apply_blocks_when_target_serial_is_running(self):
        temp_dir, avd_home, avd_root, env = self.make_fake_avd_home()
        output_dir = temp_dir / "out"
        devices_output_path = temp_dir / "adb_devices.txt"
        devices_output_path.write_text(
            "List of devices attached\nemulator-5554\tdevice\n",
            encoding="utf-8",
        )

        result = self.run_script(
            env,
            "-AvdHome",
            str(avd_home),
            "-OutputDir",
            str(output_dir),
            "-AdbDevicesOutputPath",
            str(devices_output_path),
            "-Apply",
            "-ConfirmPrepare",
            "PREPARE_SENKU_TABLET_2_LARGE_DATA_AVD",
        )

        self.assertEqual(result.returncode, 2, result.stderr + result.stdout)
        summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
        self.assertEqual(summary["status"], "blocked")
        self.assertEqual(summary["blocked_reason"], "target_serial_running")
        self.assertTrue(summary["running_device_check"]["expected_serial_running"])
        self.assertFalse(summary["config_updated"])
        self.assertFalse(summary["destructive_actions_performed"])
        self.assertIn("disk.dataPartition.size=6G", (avd_root / "config.ini").read_text(encoding="utf-8-sig"))


if __name__ == "__main__":
    unittest.main()
