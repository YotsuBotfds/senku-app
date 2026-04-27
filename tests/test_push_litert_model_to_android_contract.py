import json
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "push_litert_model_to_android.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class PushLiteRtModelToAndroidContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_contract_includes_conservative_preflight_controls(self):
        self.assertIn("[switch]$DryRun", self.script)
        self.assertIn("[switch]$SkipDataSpaceCheck", self.script)
        self.assertIn('[string]$SummaryPath = ""', self.script)
        self.assertIn('if ($Skip) {', self.script)
        self.assertIn("Skipping /data free-space preflight because -SkipDataSpaceCheck was set.", self.script)
        self.assertIn("Assert-DeviceHasModelStagingSpace -ModelBytes $modelBytes -Skip:$SkipDataSpaceCheck", self.script)
        self.assertIn("LiteRT model push dry run; no emulator/device commands will be run and no bytes will be transferred.", self.script)
        self.assertIn('Write-Host "Model: $resolvedModelPath"', self.script)
        self.assertIn('Write-Host "Model size: $modelBytes bytes (${modelGiB} GiB)"', self.script)
        self.assertIn('Write-Host "Staging requirement: model is staged in $RemoteTempDir before copying into app storage."', self.script)
        self.assertIn('Write-Host "Free-space check posture: real push requires about ${requiredGiB} GiB free on /data unless -SkipDataSpaceCheck is set."', self.script)
        self.assertIn('Write-Host "SkipDataSpaceCheck posture: $([bool]$SkipDataSpaceCheck)"', self.script)
        self.assertIn('Write-Host "Transfer posture: skipped by -DryRun."', self.script)
        self.assertIn("non_acceptance_evidence = $true", self.script)
        self.assertIn("acceptance_evidence = $false", self.script)
        self.assertIn("dry_run = $true", self.script)
        self.assertIn("required_staging_bytes = $requiredBytes", self.script)
        self.assertIn("skip_data_space_check = [bool]$SkipDataSpaceCheck", self.script)
        self.assertIn("transfer_skipped = $true", self.script)
        self.assertIn("fixed_four_emulator_stop_line = $fixedFourEmulatorStopLine", self.script)
        self.assertIn('[long]$ModelBytes,', self.script)
        self.assertIn('param(', self.script)
        self.assertIn('$trimmed -match "^\\S+\\s+\\d+\\s+\\d+\\s+(\\d+)\\s+\\d+%\\s+/data(\\s|$)"', self.script)
        self.assertIn('Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "rm", "-rf", $RemoteTempDir) -AllowFailure', self.script)
        dry_run = self.script.index("if ($DryRun) {")
        temp_setup = self.script.index('$tempDir = Join-Path')
        first_adb_wait = self.script.index('Invoke-AdbChecked -Arguments @("-s", $Device, "wait-for-device")')
        adb_required = self.script.index('throw "adb not found at $adb"')
        self.assertLess(dry_run, temp_setup)
        self.assertLess(dry_run, first_adb_wait)
        self.assertLess(dry_run, adb_required)
        preflight = self.script.index("Assert-DeviceHasModelStagingSpace -ModelBytes $modelBytes -Skip:$SkipDataSpaceCheck")
        finallyStart = self.script.index("} finally {")
        cleanup = self.script.index('Invoke-AdbChecked -Arguments @("-s", $Device, "shell", "rm", "-rf", $RemoteTempDir) -AllowFailure', finallyStart)
        self.assertLess(preflight, cleanup)

    def test_dry_run_summary_output_uses_tiny_model_without_adb(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            model_path = tmp_path / "tiny.task"
            summary_path = tmp_path / "summary.json"
            model_path.write_bytes(b"abc")

            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-DryRun",
                    "-SkipDataSpaceCheck",
                    "-ModelPath",
                    str(model_path),
                    "-SummaryPath",
                    str(summary_path),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertTrue(summary_path.exists(), result.stderr + result.stdout)
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
            self.assertTrue(summary["non_acceptance_evidence"])
            self.assertFalse(summary["acceptance_evidence"])
            self.assertTrue(summary["dry_run"])
            self.assertEqual(summary["model_path"], str(model_path.resolve()))
            self.assertEqual(summary["model_bytes"], 3)
            self.assertEqual(summary["model_gib"], 0)
            self.assertEqual(summary["required_staging_bytes"], 67108870)
            self.assertEqual(summary["required_staging_gib"], 0.06)
            self.assertTrue(summary["skip_data_space_check"])
            self.assertTrue(summary["transfer_skipped"])
            self.assertEqual(
                summary["fixed_four_emulator_stop_line"],
                "fixed four-emulator posture matrix: 5556 phone portrait; 5560 phone landscape; 5554 tablet portrait; 5558 tablet landscape",
            )

    def test_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\push_litert_model_to_android.ps1",
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
