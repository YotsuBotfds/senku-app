import os
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "start_senku_emulator_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class StartSenkuEmulatorMatrixContractTests(unittest.TestCase):
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
