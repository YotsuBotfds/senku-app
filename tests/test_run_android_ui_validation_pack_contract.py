import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_ui_validation_pack.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidUiValidationPackContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_physical_install_retries_with_no_streaming_without_changing_emulators(self):
        self.assertIn("function Install-SenkuApk", self.script)
        self.assertIn('"install", "-r", $Apk', self.script)
        self.assertIn('if ($Device -notlike "emulator-*") {', self.script)
        self.assertIn('"install", "--no-streaming", "-r", $Apk', self.script)
        self.assertIn(
            "retrying with adb install --no-streaming on physical device",
            self.script,
        )
        self.assertLess(
            self.script.index('if ($Device -notlike "emulator-*") {'),
            self.script.index('"install", "--no-streaming", "-r", $Apk'),
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
                "scripts\\run_android_ui_validation_pack.ps1",
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
