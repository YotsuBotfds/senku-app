import subprocess
import unittest
from pathlib import Path
import re


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_followup_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidFollowupMatrixContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_summary_rolls_up_first_host_adb_platform_tools_version(self):
        self.assertIn(
            "function Get-FirstNonEmptyHostAdbPlatformToolsVersion",
            self.script,
        )
        self.assertIn(
            '$version = Get-ObjectStringProperty -Object $item -PropertyName "host_adb_platform_tools_version"',
            self.script,
        )
        self.assertIn(
            "host_adb_platform_tools_version = Get-FirstNonEmptyHostAdbPlatformToolsVersion -Items $Records",
            self.script,
        )
        self.assertIn(
            "- host_adb_platform_tools_version: $($Summary.host_adb_platform_tools_version)",
            self.script,
        )

        record_field_index = self.script.index(
            "host_adb_platform_tools_version = $hostAdbPlatformToolsVersion"
        )
        summary_rollup_index = self.script.index(
            "host_adb_platform_tools_version = Get-FirstNonEmptyHostAdbPlatformToolsVersion -Items $Records"
        )
        json_write_index = self.script.index("$summary | ConvertTo-Json -Depth 8")
        markdown_write_index = self.script.index(
            "ConvertTo-FollowupMatrixSummaryMarkdown -Summary $summary"
        )

        self.assertLess(record_field_index, json_write_index)
        self.assertLess(summary_rollup_index, json_write_index)
        self.assertLess(summary_rollup_index, markdown_write_index)

    def test_followup_matrix_contract_fields_and_pass_throughs(self):
        self.assertIn("[switch]$WarmStart", self.script)
        self.assertIn("[switch]$RequireStrictFollowUpProof", self.script)
        self.assertIn("[switch]$SkipPackPushIfCurrent", self.script)
        self.assertIn("[switch]$ForcePackPush", self.script)
        self.assertIn("if (-not [string]::IsNullOrWhiteSpace($PushPackDir)) {", self.script)
        self.assertIn('$pushPackSummaryPath = Join-Path $resolvedOutputDir ($runLabel + ".pack_push.json")', self.script)
        self.assertIn("$detailArgs.PushPackSummaryPath = $pushPackSummaryPath", self.script)
        self.assertIn("$detailArgs.SkipPackPushIfCurrent = $true", self.script)
        self.assertIn("$detailArgs.ForcePackPush = $true", self.script)
        self.assertIn("$detailArgs.WarmStart = $true", self.script)
        self.assertIn("$detailArgs.RequireStrictFollowUpProof = $true", self.script)
        self.assertIn('"require_strict_followup_proof"', self.script)
        self.assertIn("warm_start = [bool]$WarmStart", self.script)
        self.assertIn("strict_followup_proof_required = [bool]$runRequiresStrictFollowUpProof", self.script)
        self.assertIn("function New-FollowupMatrixSummary", self.script)
        self.assertIn("function ConvertTo-FollowupMatrixSummaryMarkdown", self.script)
        self.assertIn("function Resolve-MatrixDevicePosture", self.script)
        self.assertIn('return "phone_portrait"', self.script)
        self.assertIn('return "phone_landscape"', self.script)
        self.assertIn('return "tablet_portrait"', self.script)
        self.assertIn('return "tablet_landscape"', self.script)
        self.assertIn('$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"', self.script)
        self.assertIn('$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"', self.script)
        self.assertIn("posture_groups = $postureGroups", self.script)
        self.assertIn("emulator_groups = $emulatorGroups", self.script)
        self.assertIn("posture = $_.posture", self.script)
        self.assertIn("push_pack_summary_path =", self.script)
        self.assertIn("push_pack_cache_hit =", self.script)
        self.assertIn("push_pack_pushed =", self.script)
        self.assertIn("function Get-HostAdbPlatformToolsVersion", self.script)
        self.assertIn("function Get-FirstNonEmptyHostAdbPlatformToolsVersion", self.script)
        self.assertIn("$detailOutput = & $detailRunner @detailArgs 2>&1", self.script)
        self.assertIn("host_adb_platform_tools_version = $hostAdbPlatformToolsVersion", self.script)
        self.assertIn("host_adb_platform_tools_version = Get-FirstNonEmptyHostAdbPlatformToolsVersion -Items $Records", self.script)
        self.assertIn("host_adb_platform_tools_version:", self.script)
        self.assertIn("push_pack_cache_hit_count =", self.script)
        self.assertIn("push_pack_pushed_count =", self.script)
        self.assertIn("pack_cache_hits=", self.script)
        self.assertIn("failed_items = @($failed | ForEach-Object", self.script)
        self.assertIn("## Posture Groups", self.script)
        self.assertIn("warm_start_count =", self.script)
        self.assertIn("Matrix summary JSON written to", self.script)

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
        self.assertRegex("powershell -File run_android_detail_followup.ps1 -Emulator emulator-5554 -InitialQuery x", pattern)
        self.assertRegex("powershell -File run_android_detail_followup.ps1 -Emulator:emulator-5554", pattern)
        self.assertRegex('powershell -File run_android_detail_followup.ps1 -Emulator "emulator-5554"', pattern)
        self.assertNotRegex("powershell -File run_android_detail_followup.ps1 -Emulator emulator-55540 -InitialQuery x", pattern)
        self.assertNotRegex("powershell -File run_android_detail_followup.ps1 -Emulator emulator-5554-extra", pattern)

    def test_followup_matrix_parser_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_followup_matrix.ps1",
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
