import unittest
import re
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
PROMPT_LOGGED = REPO_ROOT / "scripts" / "run_android_prompt_logged.ps1"
FOLLOWUP_LOGGED = REPO_ROOT / "scripts" / "run_android_detail_followup_logged.ps1"


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


if __name__ == "__main__":
    unittest.main()
