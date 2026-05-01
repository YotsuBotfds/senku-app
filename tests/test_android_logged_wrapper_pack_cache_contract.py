import unittest
import re
import subprocess
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
PROMPT_LOGGED = REPO_ROOT / "scripts" / "run_android_prompt_logged.ps1"
FOLLOWUP_LOGGED = REPO_ROOT / "scripts" / "run_android_detail_followup_logged.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidLoggedWrapperPackCacheContractTests(unittest.TestCase):
    def test_prompt_logged_forwards_pack_cache_controls(self):
        script = PROMPT_LOGGED.read_text(encoding="utf-8-sig")

        self.assertIn("[string]$PushPackDir", script)
        self.assertIn("[switch]$SkipPackPushIfCurrent", script)
        self.assertIn("[switch]$ForcePackPush", script)
        self.assertIn("$scriptArgs.PushPackDir = $PushPackDir", script)
        self.assertIn("$pushPackSummaryPath = Join-Path $resolvedOutputDir", script)
        self.assertIn("$scriptArgs.PushPackSummaryPath = $pushPackSummaryPath", script)
        self.assertIn("$scriptArgs.SkipPackPushIfCurrent = $true", script)
        self.assertIn("$scriptArgs.ForcePackPush = $true", script)
        self.assertIn("push_pack_summary_path =", script)
        self.assertIn("push_pack_cache_hit =", script)
        self.assertIn("push_pack_pushed =", script)

    def test_detail_logged_forwards_pack_cache_controls(self):
        script = FOLLOWUP_LOGGED.read_text(encoding="utf-8-sig")

        self.assertIn("[switch]$SkipPackPushIfCurrent", script)
        self.assertIn("[switch]$ForcePackPush", script)
        self.assertIn("$scriptArgs.PushPackDir = $PushPackDir", script)
        self.assertIn("$pushPackSummaryPath = Join-Path $resolvedOutputDir", script)
        self.assertIn("$scriptArgs.PushPackSummaryPath = $pushPackSummaryPath", script)
        self.assertIn("$scriptArgs.SkipPackPushIfCurrent = $true", script)
        self.assertIn("$scriptArgs.ForcePackPush = $true", script)
        self.assertIn("$pushPackSummary = Read-JsonFileOrNull -Path $pushPackSummaryPath", script)
        self.assertIn("push_pack_summary_path =", script)
        self.assertIn("push_pack_cache_hit =", script)
        self.assertIn("push_pack_pushed =", script)
        self.assertIn("push_pack_state_path =", script)

    def test_detail_logged_forwards_strict_followup_proof_opt_in_only(self):
        script = FOLLOWUP_LOGGED.read_text(encoding="utf-8-sig")

        self.assertIn("[switch]$RequireStrictFollowUpProof", script)
        self.assertIn("if ($RequireStrictFollowUpProof) {", script)
        self.assertIn("$scriptArgs.RequireStrictFollowUpProof = $true", script)
        self.assertIn("strict_followup_proof_required = [bool]$RequireStrictFollowUpProof", script)

    def test_detail_logged_uses_bounded_exact_emulator_conflict_match(self):
        script = FOLLOWUP_LOGGED.read_text(encoding="utf-8-sig")

        self.assertIn("$emulatorPattern = [regex]::Escape($TargetEmulator)", script)
        self.assertIn("-Emulator(?:\\s+|:)", script)
        self.assertIn("$_.CommandLine -match $pattern", script)
        self.assertNotIn("*-Emulator $TargetEmulator*", script)

        target = "emulator-5554"
        escaped = re.escape(target)
        pattern = re.compile(
            rf"(?i)(?:^|\s)-Emulator(?:\s+|:)"
            rf"(?:\"{escaped}\"|'{escaped}'|{escaped})(?=\s|$)"
        )
        self.assertRegex("powershell -File run_android_detail_followup.ps1 -Emulator emulator-5554 -InitialQuery x", pattern)
        self.assertRegex("powershell -File run_android_detail_followup.ps1 -Emulator:emulator-5554", pattern)
        self.assertRegex('powershell -File run_android_detail_followup.ps1 -Emulator "emulator-5554"', pattern)
        self.assertNotRegex("powershell -File run_android_detail_followup.ps1 -Emulator emulator-55540 -InitialQuery x", pattern)
        self.assertNotRegex("powershell -File run_android_detail_followup.ps1 -Emulator emulator-5554-extra", pattern)

    def test_detail_logged_parser_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_detail_followup_logged.ps1",
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
