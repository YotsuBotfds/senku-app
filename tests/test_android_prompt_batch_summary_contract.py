import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "run_android_prompt_batch.ps1"


class AndroidPromptBatchSummaryContractTests(unittest.TestCase):
    def test_batch_summary_contract_is_derived_from_jsonl_rows(self):
        script = SCRIPT_PATH.read_text(encoding="utf-8")

        self.assertIn("function New-BatchSummary", script)
        self.assertIn("function ConvertTo-BatchSummaryMarkdown", script)
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


if __name__ == "__main__":
    unittest.main()
