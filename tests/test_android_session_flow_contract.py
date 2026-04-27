import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_session_flow.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidSessionFlowContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_requires_followups_and_valid_polling_window(self):
        self.assertIn("[Parameter(Mandatory = $true)]", self.script)
        self.assertIn("[string]$InitialQuery", self.script)
        self.assertIn("[string[]]$FollowUpQueries", self.script)
        self.assertIn('throw "Provide at least one follow-up query with -FollowUpQueries."', self.script)
        self.assertIn('throw "PollSeconds must be at least 1."', self.script)
        self.assertIn(
            'throw "MaxWaitSeconds must be greater than or equal to PollSeconds."',
            self.script,
        )

    def test_activity_launch_uses_encoded_and_quoted_auto_query(self):
        self.assertIn("function Quote-AndroidShellArg", self.script)
        self.assertIn("$encodedQuery = [System.Uri]::EscapeDataString($Query)", self.script)
        self.assertIn('"--es", "auto_query", (Quote-AndroidShellArg $encodedQuery)', self.script)
        self.assertIn('"--ez", "auto_ask", "true"', self.script)
        self.assertIn('"--activity-clear-top"', self.script)
        self.assertIn('"--activity-single-top"', self.script)

    def test_new_slug_coerces_null_text_before_lowercasing(self):
        self.assertIn("function New-Slug", self.script)
        self.assertIn('$safeText = if ($null -eq $Text) { "" } else { $Text }', self.script)
        self.assertIn(
            '$slug = $safeText.ToLowerInvariant() -replace "[^a-z0-9]+", "_"',
            self.script,
        )
        self.assertIn('$slug = "turn"', self.script)

    def test_manifest_records_session_and_turn_artifacts(self):
        self.assertIn("function Add-ManifestRecord", self.script)
        self.assertIn("session_label = $label", self.script)
        self.assertIn("turn_index = $turnIndex + 1", self.script)
        self.assertIn("turn_kind = $turn.Kind", self.script)
        self.assertIn("answer_dump_path = $dumpPath", self.script)
        self.assertIn("session_dump_path = $sessionDumpPath", self.script)
        self.assertIn("timed_out = $timedOut", self.script)
        self.assertIn("error = $turnError", self.script)
        self.assertIn("session_text = if ($sessionSummary)", self.script)

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
                "scripts\\run_android_session_flow.ps1",
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
