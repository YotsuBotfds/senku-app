import os
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "start_senku_emulator_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class StartSenkuEmulatorMatrixContractTests(unittest.TestCase):
    def test_whatif_prints_read_only_headless_large_data_launch_args(self):
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
                    "phone_portrait",
                    "-Mode",
                    "read_only",
                    "-Headless",
                    "-PartitionSizeMb",
                    "4096",
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
        self.assertIn("phone_portrait: emulator-5556 / Senku_Large_4 / 1080x2400", output)
        self.assertIn("-avd Senku_Large_4 -port 5556", output)
        self.assertIn("-read-only -no-snapshot-load -no-snapshot-save", output)
        self.assertIn("-no-window", output)
        self.assertIn("-partition-size 4096", output)
        self.assertIn("What if:", output)
        self.assertIn("Start emulator in read_only mode", output)

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
