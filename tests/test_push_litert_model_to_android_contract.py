import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "push_litert_model_to_android.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class PushLiteRtModelToAndroidContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_contract_includes_conservative_preflight_controls(self):
        self.assertIn("[switch]$SkipDataSpaceCheck", self.script)
        self.assertIn('if ($Skip) {', self.script)
        self.assertIn("Skipping /data free-space preflight because -SkipDataSpaceCheck was set.", self.script)
        self.assertIn("Assert-DeviceHasModelStagingSpace -ModelBytes $modelBytes -Skip:$SkipDataSpaceCheck", self.script)
        self.assertIn('[long]$ModelBytes,', self.script)
        self.assertIn('param(', self.script)
        self.assertIn('$trimmed -match "^\\S+\\s+\\d+\\s+\\d+\\s+(\\d+)\\s+\\d+%\\s+/data(\\s|$)"', self.script)
        self.assertIn('Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "rm", "-rf", $RemoteTempDir) -AllowFailure', self.script)
        preflight = self.script.index("Assert-DeviceHasModelStagingSpace -ModelBytes $modelBytes -Skip:$SkipDataSpaceCheck")
        finallyStart = self.script.index("} finally {")
        cleanup = self.script.index('Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "rm", "-rf", $RemoteTempDir) -AllowFailure', finallyStart)
        self.assertLess(preflight, cleanup)

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
                "scripts\\push_litert_model_to_android.ps1",
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
