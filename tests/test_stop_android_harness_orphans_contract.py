import re
import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "stop_android_harness_orphans.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class StopAndroidHarnessOrphansContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_parameters_patterns_and_case_insensitive_matching_are_stable(self):
        self.assertIn("[switch]$WhatIf", self.script)
        self.assertIn('$ErrorActionPreference = "Stop"', self.script)
        for pattern in (
            '"run_android_prompt.ps1"',
            '"run_android_detail_followup.ps1"',
            '"run_android_followup_suite.ps1"',
            '"run_android_session_flow.ps1"',
            '"run_android_session_batch.ps1"',
            '"auto_followup_query"',
        ):
            self.assertIn(pattern, self.script)
        self.assertIn("[string]::IsNullOrWhiteSpace($CommandLine)", self.script)
        self.assertIn("return $false", self.script)
        self.assertIn("IndexOf($pattern, [System.StringComparison]::OrdinalIgnoreCase)", self.script)

    def test_descendant_collection_is_recursive_and_deduped(self):
        self.assertIn("[System.Collections.Generic.HashSet[int]]", self.script)
        self.assertIn("if (-not $Ids.Add($ProcessId))", self.script)
        self.assertIn("Where-Object { $_.ParentProcessId -eq $ProcessId }", self.script)
        self.assertIn("Add-Descendants -ProcessId ([int]$child.ProcessId)", self.script)
        self.assertIn("$targetIds = [System.Collections.Generic.HashSet[int]]::new()", self.script)

    def test_no_target_paths_exit_successfully(self):
        self.assertIn('Write-Host "No Android harness orphan roots found."', self.script)
        self.assertIn('Write-Host "No Android harness orphan processes resolved."', self.script)
        self.assertGreaterEqual(self.script.count("exit 0"), 3)

    def test_whatif_reports_and_exits_before_stop(self):
        match = re.search(r"if\s*\(\$WhatIf\)\s*\{(?P<body>.*?)\n\}", self.script, re.DOTALL)
        self.assertIsNotNone(match)
        body = match.group("body")
        self.assertIn('Write-Host "WhatIf set; no processes were stopped."', body)
        self.assertIn("exit 0", body)
        self.assertNotIn("Stop-Process", body)

    def test_stop_order_and_warning_contract_are_stable(self):
        self.assertIn('Sort-Object @{ Expression = "CreationDate"; Descending = $true }', self.script)
        self.assertIn("Stop-Process -Id $target.ProcessId -Force -ErrorAction Stop", self.script)
        self.assertIn(
            'Write-Warning ("Failed to stop PID {0}: {1}" -f $target.ProcessId, $_.Exception.Message)',
            self.script,
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
                "scripts\\stop_android_harness_orphans.ps1",
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
