import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


def run_gate(*args: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [
            "powershell",
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-File",
            str(SCRIPT_PATH),
            *args,
        ],
        cwd=REPO_ROOT,
        capture_output=True,
        text=True,
        check=False,
    )


class PowerShellQualityGateTests(unittest.TestCase):
    def test_quality_gate_parses_selected_script(self):
        result = run_gate(
            "-Path",
            "scripts\\run_powershell_quality_gate.ps1",
            "-SkipAnalyzer",
            "-SkipPester",
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)

    def test_quality_gate_dry_run_lists_selected_files(self):
        result = run_gate(
            "-Path",
            "scripts\\run_powershell_quality_gate.ps1",
            "-WhatIf",
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("PowerShell quality gate dry run.", result.stdout)
        self.assertIn("scripts\\run_powershell_quality_gate.ps1", result.stdout)

    def test_require_analyzer_fails_cleanly_when_missing(self):
        result = run_gate(
            "-Path",
            "scripts\\run_powershell_quality_gate.ps1",
            "-SkipParser",
            "-SkipPester",
            "-RequireAnalyzer",
        )

        if result.returncode == 0:
            self.skipTest("PSScriptAnalyzer is installed in this environment")
        self.assertIn("PSScriptAnalyzer is not installed", result.stderr + result.stdout)


if __name__ == "__main__":
    unittest.main()
