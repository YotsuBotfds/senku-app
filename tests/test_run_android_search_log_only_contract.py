import unittest
from pathlib import Path
import re


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

    def test_imports_android_harness_common_and_captures_host_adb_version_after_adb_validation(self):
        adb_validation = 'throw "adb not found at $adb"'
        module_import = 'Import-Module $commonHarnessModule -Force -DisableNameChecking'
        version_capture = "$hostAdbPlatformToolsVersion = Get-AndroidHostAdbPlatformToolsVersion -AdbPath $adb"

        self.assertIn('$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"', self.script)
        self.assertIn('throw "android_harness_common.psm1 not found at $commonHarnessModule"', self.script)
        self.assertIn(module_import, self.script)
        self.assertIn(version_capture, self.script)
        self.assertLess(self.script.index(adb_validation), self.script.index(module_import))
        self.assertLess(self.script.index(module_import), self.script.index(version_capture))

    def test_emits_compact_metadata_json_after_logcat_save_before_exits(self):
        save_logcat = "Save-Logcat -Path $LogcatPath"
        metadata_key = "host_adb_platform_tools_version = $hostAdbPlatformToolsVersion"
        json_emit = "} | ConvertTo-Json -Compress -Depth 4)"
        success_exit = "exit 0"
        failure_exit = "exit 1"

        self.assertIn(metadata_key, self.script)
        self.assertIn(json_emit, self.script)
        self.assertLess(self.script.index(save_logcat), self.script.index(metadata_key))
        self.assertLess(self.script.index(json_emit), self.script.index(success_exit))
        self.assertLess(self.script.index(json_emit), self.script.index(failure_exit))

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

    def test_conflicting_harness_match_bounds_emulator_argument(self):
        self.assertIn("$emulatorPattern = [regex]::Escape($TargetEmulator)", self.script)
        self.assertIn("-Emulator(?:\\s+|:)", self.script)
        self.assertIn("(?=\\s|$)", self.script)
        self.assertIn("$_.CommandLine -match $pattern", self.script)
        self.assertNotIn("*-Emulator $TargetEmulator*", self.script)
        self.assertNotIn("$_.CommandLine -like $pattern", self.script)

        target = "emulator-5554"
        pattern = (
            rf"(?i)(?:^|\s)-Emulator(?:\s+|:)"
            rf"(?:\"{re.escape(target)}\"|'{re.escape(target)}'|{re.escape(target)})(?=\s|$)"
        )
        self.assertRegex("powershell -File run_android_prompt.ps1 -Emulator emulator-5554 -Query x", pattern)
        self.assertRegex("powershell -File run_android_prompt.ps1 -Emulator:emulator-5554", pattern)
        self.assertRegex('powershell -File run_android_prompt.ps1 -Emulator "emulator-5554"', pattern)
        self.assertNotRegex("powershell -File run_android_prompt.ps1 -Emulator emulator-55540 -Query x", pattern)
        self.assertNotRegex("powershell -File run_android_prompt.ps1 -Emulator emulator-5554-extra", pattern)

    def test_conflicting_harness_filter_includes_detail_followup_runners(self):
        self.assertIn("*run_android_search_log_only.ps1*", self.script)
        self.assertIn("*run_android_prompt_logged.ps1*", self.script)
        self.assertIn("*run_android_prompt.ps1*", self.script)
        self.assertIn("*run_android_detail_followup_logged.ps1*", self.script)
        self.assertIn("*run_android_detail_followup.ps1*", self.script)


if __name__ == "__main__":
    unittest.main()
