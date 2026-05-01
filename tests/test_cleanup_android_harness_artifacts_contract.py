import re
import shutil
import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "cleanup_android_harness_artifacts.ps1"


class CleanupAndroidHarnessArtifactsContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")
        cls.powershell = shutil.which("powershell")

    def test_parameters_and_default_targets_are_present(self):
        self.assertIn("[string[]]$Targets", self.script)
        self.assertIn('"artifacts/instrumented_ui_smoke"', self.script)
        self.assertIn('"artifacts/ui_validation"', self.script)
        self.assertIn('"artifacts/live_debug"', self.script)
        self.assertIn("[int]$KeepDays", self.script)
        self.assertIn("[switch]$WhatIf", self.script)

    def test_targets_resolve_against_repo_root_unless_rooted(self):
        self.assertIn('$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path', self.script)
        self.assertIn("function Resolve-CleanupArtifactPath", self.script)
        self.assertIn("[System.IO.Path]::IsPathRooted($Path)", self.script)
        self.assertIn("Join-Path $repoRoot $Path", self.script)
        self.assertIn("return [System.IO.Path]::GetFullPath($candidate)", self.script)

    def test_allowed_roots_are_resolved_and_enforced_before_deletion(self):
        self.assertIn("$defaultTargetRoots = @(", self.script)
        self.assertIn('"artifacts/instrumented_ui_smoke"', self.script)
        self.assertIn('"artifacts/ui_validation"', self.script)
        self.assertIn('"artifacts/live_debug"', self.script)
        self.assertIn("$allowedCleanupRoots = @($defaultTargetRoots | ForEach-Object { Resolve-CleanupArtifactPath -Path $_ })", self.script)
        self.assertIn("function Test-IsUnderCleanupRoot", self.script)
        self.assertIn(
            "if (-not (Test-IsUnderCleanupRoot -Path $resolvedTarget -Roots $allowedCleanupRoots))",
            self.script,
        )
        self.assertIn(
            'throw "Cleanup target is outside allowed Android harness artifact roots: $resolvedTarget"',
            self.script,
        )

    def test_item_path_is_rechecked_under_selected_target_and_allowed_roots(self):
        item_guard_match = re.search(
            r"\$resolvedItem\s*=\s*Resolve-CleanupArtifactPath -Path \$item\.FullName(?P<body>.*?)"
            r"Remove-Item -LiteralPath \$resolvedItem -Recurse -Force",
            self.script,
            flags=re.DOTALL,
        )
        self.assertIsNotNone(item_guard_match)
        body = item_guard_match.group("body")
        self.assertIn(
            "if (-not (Test-IsUnderCleanupRoot -Path $resolvedItem -Roots @($resolvedTarget)))",
            body,
        )
        self.assertIn(
            "if (-not (Test-IsUnderCleanupRoot -Path $resolvedItem -Roots $allowedCleanupRoots))",
            body,
        )
        self.assertIn(
            'throw "Cleanup item is outside selected Android harness artifact target: $resolvedItem"',
            body,
        )
        self.assertIn(
            'throw "Cleanup item is outside allowed Android harness artifact roots: $resolvedItem"',
            body,
        )

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
            "Remove-Item -LiteralPath $resolvedItem -Recurse -Force",
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

    @unittest.skipIf(shutil.which("powershell") is None, "powershell is required for contract runtime checks")
    def test_whatif_reports_candidate_without_removing_it(self):
        target = REPO_ROOT / "artifacts" / "live_debug" / "cleanup_contract_runtime"
        candidate = target / "old_candidate"
        if target.exists():
            shutil.rmtree(target)
        candidate.mkdir(parents=True)
        try:
            result = subprocess.run(
                [
                    self.powershell,
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-Targets",
                    "artifacts/live_debug/cleanup_contract_runtime",
                    "-KeepDays",
                    "0",
                    "-WhatIf",
                ],
                cwd=REPO_ROOT,
                text=True,
                capture_output=True,
                check=True,
            )
            self.assertIn("[whatif] Would remove", result.stdout)
            self.assertIn(str(candidate), result.stdout)
            self.assertTrue(candidate.exists(), "-WhatIf must not remove cleanup candidates")
        finally:
            if target.exists():
                shutil.rmtree(target)

    @unittest.skipIf(shutil.which("powershell") is None, "powershell is required for contract runtime checks")
    def test_whatif_rejects_path_traversal_target_before_cleanup(self):
        result = subprocess.run(
            [
                self.powershell,
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(SCRIPT),
                "-Targets",
                "artifacts/live_debug/../..",
                "-WhatIf",
            ],
            cwd=REPO_ROOT,
            text=True,
            capture_output=True,
            check=False,
        )
        self.assertNotEqual(result.returncode, 0)
        combined_output = result.stdout + result.stderr
        self.assertIn("Cleanup target is outside allowed Android harness artifact roots", combined_output)


if __name__ == "__main__":
    unittest.main()
