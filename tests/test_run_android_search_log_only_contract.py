import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_search_log_only.ps1"


class AndroidSearchLogOnlyContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_exposes_pack_push_cache_flags(self):
        self.assertIn("[switch]$SkipPackPushIfCurrent", self.script)
        self.assertIn("[switch]$ForcePackPush", self.script)
        self.assertIn("[string]$PushPackSummaryPath", self.script)

    def test_forwards_pack_push_cache_flags_to_push_mobile_pack(self):
        self.assertIn('$pushPackArgs = @(', self.script)
        self.assertIn('"-Device", $Emulator,', self.script)
        self.assertIn('"-PackDir", $PushPackDir,', self.script)
        self.assertIn("-ForceStop", self.script)
        self.assertIn("if ($SkipPackPushIfCurrent) {", self.script)
        self.assertIn('"-SkipIfCurrent"', self.script)
        self.assertIn("if ($ForcePackPush) {", self.script)
        self.assertIn('"-ForcePush"', self.script)
        self.assertIn("if (-not [string]::IsNullOrWhiteSpace($PushPackSummaryPath)) {", self.script)
        self.assertIn('"-SummaryPath", $PushPackSummaryPath', self.script)
        self.assertIn("& $pushPackScript @pushPackArgs", self.script)


if __name__ == "__main__":
    unittest.main()
