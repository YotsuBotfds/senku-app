import re
import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_functional_ux_smoke_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidFunctionalUxSmokeMatrixContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_functional_follow_up_preset_is_in_matrix(self):
        self.assertIn("- phone-functional-follow-up", self.script)
        self.assertIn('"phone-functional-follow-up"', self.script)

        match = re.search(r"\$presets = @\((.*?)\)", self.script, re.S)
        self.assertIsNotNone(match)
        presets = re.findall(r'"([^"]+)"', match.group(1))
        self.assertEqual(
            presets,
            [
                "phone-functional",
                "phone-functional-follow-up",
                "phone-functional-saved",
                "phone-functional-back-provenance",
            ],
        )

    def test_runner_invokes_each_preset_with_capture_summary(self):
        self.assertIn('"-SmokePreset"', self.script)
        self.assertIn("$preset,", self.script)
        self.assertIn('"-SummaryPath"', self.script)
        self.assertIn('"-CaptureSummaryPath"', self.script)
        self.assertIn('"-CaptureLogcat"', self.script)
        self.assertIn('$canReuseInstalledApks = $true', self.script)

    def test_matrix_preflights_phone_device_role(self):
        self.assertIn("function Assert-FunctionalUxPhoneDevice", self.script)
        self.assertIn("Resolve-AndroidDeviceFacts -AdbPath $adb -DeviceName $Device", self.script)
        self.assertIn("Functional UX smoke matrix runs phone-* presets", self.script)
        self.assertIn("Assert-FunctionalUxPhoneDevice", self.script)

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
                "scripts\\run_android_functional_ux_smoke_matrix.ps1",
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
