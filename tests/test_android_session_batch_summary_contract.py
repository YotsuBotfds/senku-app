import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "run_android_session_batch.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidSessionBatchSummaryContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT_PATH.read_text(encoding="utf-8-sig")

    def test_session_batch_summary_contract_is_derived_from_jsonl_rows(self):
        self.assertIn("function New-SessionBatchSummary", self.script)
        self.assertIn("function ConvertTo-SessionBatchSummaryMarkdown", self.script)
        self.assertIn("$records += [pscustomobject]$record", self.script)
        self.assertIn("$summaryJsonPath = Join-Path $outputPath \"summary.json\"", self.script)
        self.assertIn("$summaryMarkdownPath = Join-Path $outputPath \"summary.md\"", self.script)
        self.assertIn("status_groups = $statusGroups", self.script)
        self.assertIn("timed_out_items = @($timedOut | ForEach-Object", self.script)
        self.assertIn("failed_items = @($failed | ForEach-Object", self.script)
        self.assertIn("artifact_paths = @(", self.script)
        self.assertIn("kind = \"manifest_jsonl\"", self.script)
        self.assertIn("kind = \"session_manifest_jsonl\"", self.script)
        self.assertIn("error = $_.error", self.script)
        self.assertIn("timed_out = $_.timed_out", self.script)
        self.assertIn("session_manifest_path = $_.session_manifest_path", self.script)

    def test_session_batch_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_session_batch.ps1",
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
