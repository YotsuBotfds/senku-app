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

    def test_exposes_opt_in_strict_followup_proof_switch(self):
        self.assertIn("[switch]$RequireStrictFollowUpProof", self.script)
        self.assertIn("function Get-StrictFollowUpProofErrors", self.script)
        self.assertIn(
            "$strictFollowUpProofErrors = @(Get-StrictFollowUpProofErrors -SubmissionResult $submissionResult -SourceProbe $sourceProbe)",
            self.script,
        )
        self.assertIn("strict_followup_proof_required = [bool]$RequireStrictFollowUpProof", self.script)
        self.assertIn("strict_followup_proof_passed = $strictFollowUpProofPassed", self.script)
        self.assertIn("strict_followup_proof_errors = $strictFollowUpProofErrors", self.script)

    def test_strict_followup_proof_fails_on_fallback_no_advance_or_source_miss(self):
        self.assertIn('followup_submission_used_fallback:{0}', self.script)
        self.assertIn('followup_submission_did_not_advance_after_submit:{0}', self.script)
        self.assertIn('source_link_probe_missing', self.script)
        self.assertIn('source_link_probe_not_attempted:{0}', self.script)
        self.assertIn('source_link_not_verified:{0}', self.script)
        self.assertIn(
            'if ($RequireStrictFollowUpProof -and -not $strictFollowUpProofPassed) {',
            self.script,
        )
        self.assertIn(
            'throw ("Strict follow-up proof failed: {0}" -f ($strictFollowUpProofErrors -join "; "))',
            self.script,
        )
        self.assertLess(
            self.script.index("$record | ConvertTo-Json -Depth 6 | Set-Content"),
            self.script.index('if ($RequireStrictFollowUpProof -and -not $strictFollowUpProofPassed) {'),
        )

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

    def test_adb_pull_uses_bounded_common_harness_helper(self):
        self.assertIn("[int]$AdbPullTimeoutMilliseconds = 30000", self.script)
        self.assertIn('throw "-AdbPullTimeoutMilliseconds must be a positive integer."', self.script)
        self.assertIn('$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"', self.script)
        self.assertIn("Import-Module $commonHarnessModule -Force -DisableNameChecking", self.script)
        self.assertIn("[int]$TimeoutMilliseconds = $AdbPullTimeoutMilliseconds", self.script)
        self.assertIn("Invoke-AndroidAdbCommandCapture `", self.script)
        self.assertIn('-Arguments @("-s", $Emulator, "pull", $DeviceDump, $LocalDump) `', self.script)
        self.assertIn("-TimeoutMilliseconds $TimeoutMilliseconds", self.script)
        self.assertIn("if ($result.timed_out) {", self.script)
        self.assertNotRegex(
            self.script,
            r"Start-Process\s+`(?s:.*?)\s-Wait\s+`(?s:.*?)pull",
        )

    def test_activity_launch_uses_automation_auth_marker(self):
        self.assertIn('"--es", "com.senku.mobile.extra.PRODUCT_REVIEW_AUTOMATION_AUTH", "senku-review-demo-v1"', self.script)
        self.assertLess(
            self.script.index('"--es", "com.senku.mobile.extra.PRODUCT_REVIEW_AUTOMATION_AUTH", "senku-review-demo-v1"'),
            self.script.index('"--es", "auto_query", (Quote-AndroidShellArg $encodedQuery),'),
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
