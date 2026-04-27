import unittest
import subprocess
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "run_android_prompt_batch.ps1"


class AndroidPromptBatchSummaryContractTests(unittest.TestCase):
    def test_batch_summary_contract_is_derived_from_jsonl_rows(self):
        script = SCRIPT_PATH.read_text(encoding="utf-8")

        self.assertIn("function New-BatchSummary", script)
        self.assertIn("function ConvertTo-BatchSummaryMarkdown", script)
        self.assertIn("throw \"StartIndex must be at least 0\"", script)
        self.assertIn("throw \"MaxPrompts must be at least 1\"", script)
        self.assertIn("throw \"Stride must be at least 1\"", script)
        self.assertIn("$records += [pscustomobject]$record", script)
        self.assertIn("$summaryJsonPath = Join-Path $outputPath \"summary.json\"", script)
        self.assertIn("$summaryMarkdownPath = Join-Path $outputPath \"summary.md\"", script)
        self.assertIn("status_groups = $statusGroups", script)
        self.assertIn("dump_completion_state_groups = $dumpStateGroups", script)
        self.assertIn("failed_items = @($failed | ForEach-Object", script)
        self.assertIn("artifact_paths = @(", script)
        self.assertIn("kind = \"manifest_jsonl\"", script)
        self.assertIn("kind = \"ui_dump\"", script)
        self.assertIn("error = $_.error", script)
        self.assertIn("dump_path = $_.dump_path", script)
        self.assertIn('started_at = $startedAt.ToString("o")', script)
        self.assertIn('finished_at = $finishedAt.ToString("o")', script)
        self.assertIn("elapsed_seconds =", script)

    def test_prompt_batch_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"),
                "-Path",
                "scripts\\run_android_prompt_batch.ps1",
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
