import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_harness_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidHarnessMatrixSummaryContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_matrix_summary_contract_is_present(self):
        self.assertIn("function New-MatrixSummary", self.script)
        self.assertIn("function ConvertTo-MatrixSummaryMarkdown", self.script)
        self.assertIn('[string]$PushPackDir = ""', self.script)
        self.assertIn("[switch]$DefaultWarmStart", self.script)
        self.assertIn("[switch]$SkipPackPushIfCurrent", self.script)
        self.assertIn("[switch]$ForcePackPush", self.script)
        self.assertIn('Get-RunBoolOrDefault -Run $Run -PropertyName "warm_start"', self.script)
        self.assertIn('Get-RunStringOrDefault -Run $Run -PropertyName "push_pack_dir"', self.script)
        self.assertIn("$args.WarmStart = $true", self.script)
        self.assertIn("$args.PushPackDir = $runPushPackDir", self.script)
        self.assertIn("$args.SkipPackPushIfCurrent = $true", self.script)
        self.assertIn("$args.ForcePackPush = $true", self.script)
        self.assertIn("warm_start = [bool]$entry.runner.args.WarmStart", self.script)
        self.assertIn("push_pack_dir =", self.script)
        self.assertIn("warm_start_count =", self.script)
        self.assertIn("$statusGroups = @()", self.script)
        self.assertIn("status_groups = $statusGroups", self.script)
        self.assertIn("$summaryJsonPath = Join-Path $resolvedOutputDir \"summary.json\"", self.script)
        self.assertIn("$summaryMarkdownPath = Join-Path $resolvedOutputDir \"summary.md\"", self.script)
        self.assertIn("$emulatorGroups = @()", self.script)
        self.assertIn("$failedItems = @(", self.script)
        self.assertIn("failed_items = $failedItems", self.script)
        self.assertIn("error = $_.error", self.script)
        self.assertIn("kind = \"jsonl\"", self.script)
        self.assertIn("kind = \"csv\"", self.script)
        self.assertIn("kind = \"manifest_json\"", self.script)
        self.assertIn("kind = \"logcat\"", self.script)
        self.assertIn("artifact_paths = $artifactPaths", self.script)
        self.assertIn("summary_jsonl_path = $SummaryJsonlPath", self.script)
        self.assertIn("summary_csv_path = $SummaryCsvPath", self.script)

    def test_matrix_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_harness_matrix.ps1",
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
