import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_detail_followup.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidDetailFollowupContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_exposes_pack_push_cache_control_params(self):
        self.assertIn("[switch]$SkipPackPushIfCurrent", self.script)
        self.assertIn("[switch]$ForcePackPush", self.script)
        self.assertIn('[string]$PushPackSummaryPath = ""', self.script)

    def test_forwards_pack_push_cache_controls_to_push_script(self):
        self.assertIn(
            '$pushArgs = @(',
            self.script,
        )
        self.assertIn("-SkipIfCurrent", self.script)
        self.assertIn("-ForcePush", self.script)
        self.assertIn("-SummaryPath", self.script)
        self.assertIn('if ($SkipPackPushIfCurrent) {', self.script)
        self.assertIn('if ($ForcePackPush) {', self.script)
        self.assertIn('& $pushPackScript @pushArgs', self.script)
        self.assertIn("push_pack_cache_hit =", self.script)
        self.assertIn("push_pack_pushed =", self.script)

    def test_new_slug_uses_safe_text_before_lowercasing(self):
        self.assertIn(
            '$slug = (Safe-Text -Text $Text).ToLowerInvariant() -replace "[^a-z0-9]+", "_"',
            self.script,
        )
        self.assertNotIn("$slug = $Text.ToLowerInvariant()", self.script)

    def test_followup_submit_modes_are_explicit_and_fail_closed(self):
        self.assertIn(
            '[ValidateSet("auto", "in_detail_ui", "auto_intent", "send_button", "ime_send", "ime_done", "hardware_enter")]',
            self.script,
        )
        self.assertIn('$allowAutoFollowUpFallback = ($FollowUpSubmissionMode -eq "auto")', self.script)
        self.assertIn("if (-not $allowAutoFollowUpFallback) {", self.script)
        self.assertIn(
            'throw "Explicit follow-up submit mode \'$FollowUpSubmissionMode\' failed before submit: initial_controls_missing."',
            self.script,
        )
        self.assertIn(
            'throw "Explicit follow-up submit mode \'$SubmitMode\' failed before submit: composer_missing."',
            self.script,
        )
        self.assertIn(
            'throw "Explicit follow-up submit mode \'$SubmitMode\' failed before submit: input_mismatch."',
            self.script,
        )
        self.assertIn(
            'if ($submissionResult -and -not $submissionResult.used_fallback -and -not $submissionResult.advanced_after_submit -and $allowAutoFollowUpFallback)',
            self.script,
        )
        self.assertIn(
            'if ($submissionResult -and -not $submissionResult.used_fallback -and $allowAutoFollowUpFallback)',
            self.script,
        )

    def test_followup_submit_mode_is_attempted_and_recorded(self):
        self.assertIn(
            '[ValidateSet("auto", "in_detail_ui", "send_button", "ime_send", "ime_done", "hardware_enter")]',
            self.script,
        )
        self.assertIn('-SubmitMode $(if ($FollowUpSubmissionMode -eq "auto") { "auto" } else { $FollowUpSubmissionMode })', self.script)
        self.assertIn('$effectiveSubmitMode = if ($SubmitMode -eq "auto" -or $SubmitMode -eq "in_detail_ui") { "send_button" } else { $SubmitMode }', self.script)
        self.assertIn('"ime_send" {', self.script)
        self.assertIn('"ime_done" {', self.script)
        self.assertIn('"hardware_enter" {', self.script)
        self.assertIn('"{0}_advanced" -f $effectiveSubmitMode', self.script)
        self.assertIn('followup_submission_requested_mode = $FollowUpSubmissionMode', self.script)
        self.assertIn("followup_submission_primary_signal =", self.script)

    def test_quality_gate_parser_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_detail_followup.ps1",
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
