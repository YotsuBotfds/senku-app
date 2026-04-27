import re
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "cleanup_android_harness_artifacts.ps1"


class CleanupAndroidHarnessArtifactsContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_parameters_and_default_targets_are_present(self):
        self.assertIn("[string[]]$Targets", self.script)
        self.assertIn('"artifacts/instrumented_ui_smoke"', self.script)
        self.assertIn('"artifacts/ui_validation"', self.script)
        self.assertIn('"artifacts/live_debug"', self.script)
        self.assertIn("[int]$KeepDays", self.script)
        self.assertIn("[switch]$WhatIf", self.script)

    def test_targets_resolve_against_repo_root_unless_rooted(self):
        self.assertIn('$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path', self.script)
        self.assertIn("[System.IO.Path]::IsPathRooted($target)", self.script)
        self.assertIn("{ $target }", self.script)
        self.assertIn("{ Join-Path $repoRoot $target }", self.script)

    def test_retention_uses_absolute_keep_days(self):
        self.assertIn("[Math]::Abs($KeepDays)", self.script)
        self.assertRegex(
            self.script,
            re.compile(
                r"\$cutoffUtc\s*=\s*\(Get-Date\)\.ToUniversalTime\(\)"
                r"\.AddDays\(-1\s*\*\s*\[Math\]::Abs\(\$KeepDays\)\)"
            ),
        )

    def test_whatif_branch_reports_without_deleting(self):
        whatif_match = re.search(
            r"if\s*\(\$WhatIf\)\s*\{(?P<body>.*?)\n\s*\}",
            self.script,
            flags=re.DOTALL,
        )
        self.assertIsNotNone(whatif_match)
        whatif_body = whatif_match.group("body")
        self.assertIn('Write-Host "[whatif] Would remove $($item.FullName)"', whatif_body)
        self.assertIn("continue", whatif_body)
        self.assertNotIn("Remove-Item", whatif_body)

    def test_deletion_uses_literal_full_name_recursive_force(self):
        self.assertIn(
            "Remove-Item -LiteralPath $item.FullName -Recurse -Force",
            self.script,
        )

    def test_summary_json_contract_is_present(self):
        self.assertIn("[pscustomobject]@{", self.script)
        for field in (
            "keep_days = $KeepDays",
            'cutoff_utc = $cutoffUtc.ToString("o")',
            "removed_count = $removed.Count",
            "removed = $removed",
        ):
            self.assertIn(field, self.script)
        self.assertIn("ConvertTo-Json -Depth 5", self.script)


if __name__ == "__main__":
    unittest.main()
