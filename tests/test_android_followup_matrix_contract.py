import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_followup_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidFollowupMatrixContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_followup_matrix_contract_fields_and_pass_throughs(self):
        self.assertIn("[switch]$WarmStart", self.script)
        self.assertIn("[switch]$SkipPackPushIfCurrent", self.script)
        self.assertIn("[switch]$ForcePackPush", self.script)
        self.assertIn("if (-not [string]::IsNullOrWhiteSpace($PushPackDir)) {", self.script)
        self.assertIn("$detailArgs.SkipPackPushIfCurrent = $true", self.script)
        self.assertIn("$detailArgs.ForcePackPush = $true", self.script)
        self.assertIn("$detailArgs.WarmStart = $true", self.script)
        self.assertIn("warm_start = [bool]$WarmStart", self.script)

    def test_followup_matrix_parser_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_followup_matrix.ps1",
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
