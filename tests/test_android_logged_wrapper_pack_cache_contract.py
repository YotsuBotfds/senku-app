import unittest
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


if __name__ == "__main__":
    unittest.main()
