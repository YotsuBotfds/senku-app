import re
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "start_android_detail_followup_lane.ps1"


class StartAndroidDetailFollowupLaneContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_required_queries_and_stable_parameter_defaults(self):
        self.assertRegex(
            self.script,
            r"\[Parameter\(Mandatory\s*=\s*\$true\)\]\s*\r?\n\s*\[string\]\$InitialQuery",
        )
        self.assertRegex(
            self.script,
            r"\[Parameter\(Mandatory\s*=\s*\$true\)\]\s*\r?\n\s*\[string\]\$FollowUpQuery",
        )
        self.assertIn('[ValidateSet("auto", "in_detail_ui", "auto_intent")]', self.script)
        self.assertIn('[string]$FollowUpSubmissionMode = "auto"', self.script)
        self.assertIn('[ValidateSet("preserve", "local", "host")]', self.script)
        self.assertIn('[string]$InferenceMode = "preserve"', self.script)
        self.assertIn(
            '[string]$OutputDir = "artifacts\\\\bench\\\\android_detail_followups"',
            self.script,
        )

    def test_powershell_argument_quoting_escapes_single_quotes(self):
        self.assertIn("function Quote-PowerShellArg", self.script)
        self.assertIn('return "\'\'"', self.script)
        self.assertIn('return "\'" + $Value.Replace("\'", "\'\'") + "\'"', self.script)
        self.assertIn('"-InitialQuery " + (Quote-PowerShellArg -Value $InitialQuery)', self.script)
        self.assertIn('"-FollowUpQuery " + (Quote-PowerShellArg -Value $FollowUpQuery)', self.script)

    def test_run_label_sanitization_and_default_slug_are_stable(self):
        self.assertIn("function New-Slug", self.script)
        self.assertIn("function Sanitize-RunLabel", self.script)
        self.assertIn('[regex]::Replace($safeText.ToLowerInvariant(), "[^a-z0-9]+", "_").Trim("_")', self.script)
        self.assertIn('$safeText -replace "[^A-Za-z0-9_-]+", "_"', self.script)
        self.assertGreaterEqual(self.script.count('return "android_detail_followup"'), 2)
        self.assertIn(
            '$RunLabel = "{0}_{1}" -f (New-Slug -Text $InitialQuery), (New-Slug -Text $FollowUpQuery)',
            self.script,
        )
        self.assertIn("$RunLabel = Sanitize-RunLabel -Text $RunLabel", self.script)

    def test_launcher_writes_logs_and_launcher_metadata(self):
        self.assertIn('$stdoutLog = Join-Path $resolvedOutputDir ($RunLabel + ".stdout.log")', self.script)
        self.assertIn('$stderrLog = Join-Path $resolvedOutputDir ($RunLabel + ".stderr.log")', self.script)
        self.assertIn('$launchMeta = Join-Path $resolvedOutputDir ($RunLabel + ".launcher.json")', self.script)
        self.assertIn('$record | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $launchMeta -Encoding UTF8', self.script)

    def test_start_process_uses_windows_powershell_with_expected_redirection(self):
        start_process_match = re.search(r"\$proc = Start-Process\s+`(?P<body>.*?)\r?\n\r?\n\$record =", self.script, re.DOTALL)

        self.assertIsNotNone(start_process_match)
        start_process = start_process_match.group("body")
        self.assertIn('-FilePath "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe"', start_process)
        self.assertIn('-ArgumentList @("-ExecutionPolicy", "Bypass", "-Command", $command)', start_process)
        self.assertIn("-WorkingDirectory $repoRoot", start_process)
        self.assertIn("-PassThru", start_process)
        self.assertIn("-RedirectStandardOutput $stdoutLog", start_process)
        self.assertIn("-RedirectStandardError $stderrLog", start_process)

    def test_manifest_json_includes_stable_launcher_contract_fields(self):
        record_match = re.search(
            r"\$record = \[pscustomobject\]@\{(?P<body>.*?)\r?\n\}",
            self.script,
            re.DOTALL,
        )

        self.assertIsNotNone(record_match)
        record_body = record_match.group("body")
        expected_fields = [
            "pid = $proc.Id",
            "emulator = $Emulator",
            "initial_query = $InitialQuery",
            "follow_up_query = $FollowUpQuery",
            "followup_submission_mode = $FollowUpSubmissionMode",
            "inference_mode = $InferenceMode",
            "output_dir = $resolvedOutputDir",
            "run_label = $RunLabel",
            "stdout_log = $stdoutLog",
            "stderr_log = $stderrLog",
            "launcher_script = $PSCommandPath",
            "detail_script = $detailScript",
            'started_at = (Get-Date).ToString("o")',
        ]

        for field in expected_fields:
            with self.subTest(field=field):
                self.assertIn(field, record_body)


if __name__ == "__main__":
    unittest.main()
