import json
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "push_litert_model_to_android.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"
SUMMARY_VALIDATOR_SCRIPT = REPO_ROOT / "scripts" / "validate_android_migration_summary.py"
VALIDATION_PYTHON = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"


class PushLiteRtModelToAndroidContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_contract_includes_conservative_preflight_controls(self):
        self.assertIn("[switch]$DryRun", self.script)
        self.assertIn("[switch]$SkipDataSpaceCheck", self.script)
        self.assertIn('[string]$SummaryPath = ""', self.script)
        self.assertIn('[string]$SummaryMarkdownPath = ""', self.script)
        self.assertIn('throw "-SummaryMarkdownPath is only supported with -DryRun."', self.script)
        self.assertIn('if ($Skip) {', self.script)
        self.assertIn("Skipping /data free-space preflight because -SkipDataSpaceCheck was set.", self.script)
        self.assertIn("Assert-DeviceHasModelStagingSpace -ModelBytes $modelBytes -Skip:$SkipDataSpaceCheck", self.script)
        self.assertIn("LiteRT model push dry run; no emulator/device commands will be run and no bytes will be transferred.", self.script)
        self.assertIn('Write-Host "Model: $resolvedModelPath"', self.script)
        self.assertIn('Write-Host "Model size: $modelBytes bytes (${modelGiB} GiB)"', self.script)
        self.assertIn('$modelSha256 = (Get-FileHash -LiteralPath $resolvedModelPath -Algorithm SHA256).Hash.ToLowerInvariant()', self.script)
        self.assertIn('Write-Host "Model sha256: $modelSha256"', self.script)
        self.assertIn('Write-Host "Staging requirement: model is staged in $RemoteTempDir before copying into app storage."', self.script)
        self.assertIn('Write-Host "Free-space check posture: real push requires about ${requiredGiB} GiB free on /data unless -SkipDataSpaceCheck is set."', self.script)
        self.assertIn('Write-Host "SkipDataSpaceCheck posture: $([bool]$SkipDataSpaceCheck)"', self.script)
        self.assertIn('Write-Host "Transfer posture: skipped by -DryRun."', self.script)
        self.assertIn("non_acceptance_evidence = $true", self.script)
        self.assertIn("acceptance_evidence = $false", self.script)
        self.assertIn("dry_run = $true", self.script)
        self.assertIn('status = "dry_run_only"', self.script)
        self.assertIn("stop_line = $nonAcceptanceStopLine", self.script)
        self.assertIn("primary_evidence = $fixedFourEmulatorMatrix", self.script)
        self.assertIn("comparison_baseline = $fixedFourEmulatorMatrix", self.script)
        self.assertIn("model_name = $modelFileName", self.script)
        self.assertIn("required_staging_bytes = $requiredBytes", self.script)
        self.assertIn("model_sha256 = $modelSha256", self.script)
        self.assertIn("app_private_target = $true", self.script)
        self.assertIn("app_private_target_path = $appModelPath", self.script)
        self.assertIn("remote_temp_dir = $RemoteTempDir", self.script)
        self.assertIn("tmp_staging_required = $true", self.script)
        self.assertIn("data_free_space_check_required = (-not [bool]$SkipDataSpaceCheck)", self.script)
        self.assertIn("data_free_space_required_bytes = $requiredBytes", self.script)
        self.assertIn("skip_data_space_check = [bool]$SkipDataSpaceCheck", self.script)
        self.assertIn("transfer_skipped = $true", self.script)
        self.assertIn("fixed_four_emulator_stop_line = $fixedFourEmulatorStopLine", self.script)
        self.assertIn('"# LiteRT Model Push Dry Run"', self.script)
        self.assertIn('"STOP: LiteRT model push dry run is non-acceptance evidence only; fixed four-emulator evidence remains primary."', self.script)
        self.assertIn('"- App-private target: ``$appModelPath``"', self.script)
        self.assertIn('"- Temporary staging: ``$RemoteTempDir``"', self.script)
        self.assertIn('"- /data free-space requirement: $requiredBytes bytes (${requiredGiB} GiB)"', self.script)
        self.assertIn('"- skip_data_space_check: $([bool]$SkipDataSpaceCheck)"', self.script)
        self.assertIn('"- transfer_skipped: true"', self.script)
        self.assertIn('"- Fixed-four stop line: $fixedFourEmulatorStopLine"', self.script)
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
            markdown_path = tmp_path / "summary.md"
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
                    "-SummaryMarkdownPath",
                    str(markdown_path),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertTrue(summary_path.exists(), result.stderr + result.stdout)
            self.assertTrue(markdown_path.exists(), result.stderr + result.stdout)
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["status"], "dry_run_only")
            self.assertTrue(summary["non_acceptance_evidence"])
            self.assertFalse(summary["acceptance_evidence"])
            self.assertTrue(summary["dry_run"])
            self.assertEqual(
                summary["stop_line"],
                "STOP: LiteRT model push dry run is non-acceptance evidence only; fixed four-emulator evidence remains primary.",
            )
            self.assertEqual(summary["primary_evidence"], "fixed_four_emulator_matrix")
            self.assertEqual(summary["comparison_baseline"], "fixed_four_emulator_matrix")
            self.assertEqual(summary["model_path"], str(model_path.resolve()))
            self.assertEqual(summary["model_name"], "tiny.task")
            self.assertEqual(summary["model_bytes"], 3)
            self.assertEqual(
                summary["model_sha256"],
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            )
            self.assertEqual(summary["model_gib"], 0)
            self.assertTrue(summary["app_private_target"])
            self.assertEqual(
                summary["app_private_target_path"],
                "/data/user/0/com.senku.mobile/files/models/tiny.task",
            )
            self.assertEqual(summary["remote_temp_dir"], "/data/local/tmp/senku_litert_model_push")
            self.assertTrue(summary["tmp_staging_required"])
            self.assertEqual(summary["required_staging_bytes"], 67108870)
            self.assertEqual(summary["required_staging_gib"], 0.06)
            self.assertFalse(summary["data_free_space_check_required"])
            self.assertEqual(summary["data_free_space_required_bytes"], 67108870)
            self.assertTrue(summary["skip_data_space_check"])
            self.assertTrue(summary["transfer_skipped"])
            self.assertEqual(
                summary["fixed_four_emulator_stop_line"],
                "fixed four-emulator posture matrix: 5556 phone portrait; 5560 phone landscape; 5554 tablet portrait; 5558 tablet landscape",
            )
            markdown = markdown_path.read_text(encoding="utf-8-sig")
            self.assertIn("# LiteRT Model Push Dry Run", markdown)
            self.assertIn(
                "STOP: LiteRT model push dry run is non-acceptance evidence only; fixed four-emulator evidence remains primary.",
                markdown,
            )
            self.assertIn(f"- Path: `{model_path.resolve()}`", markdown)
            self.assertIn("- Name: `tiny.task`", markdown)
            self.assertIn("- Bytes: 3", markdown)
            self.assertIn(
                "- SHA256: `ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad`",
                markdown,
            )
            self.assertIn(
                "- App-private target: `/data/user/0/com.senku.mobile/files/models/tiny.task`",
                markdown,
            )
            self.assertIn("- Temporary staging: `/data/local/tmp/senku_litert_model_push`", markdown)
            self.assertIn("- /data free-space requirement: 67108870 bytes (0.06 GiB)", markdown)
            self.assertIn("- skip_data_space_check: True", markdown)
            self.assertIn("- transfer_skipped: true", markdown)
            self.assertIn("- acceptance_evidence: false", markdown)
            self.assertIn("- non_acceptance_evidence: true", markdown)
            self.assertIn(
                "- Fixed-four stop line: fixed four-emulator posture matrix: 5556 phone portrait; 5560 phone landscape; 5554 tablet portrait; 5558 tablet landscape",
                markdown,
            )

            validator = subprocess.run(
                [
                    str(VALIDATION_PYTHON),
                    str(SUMMARY_VALIDATOR_SCRIPT),
                    str(summary_path),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(validator.returncode, 0, validator.stderr + validator.stdout)
            self.assertIn("android_migration_summary: ok", validator.stdout)
            self.assertIn("evidence: non_acceptance", validator.stdout)

    def test_summary_markdown_path_requires_dry_run(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            model_path = tmp_path / "tiny.task"
            markdown_path = tmp_path / "summary.md"
            model_path.write_bytes(b"abc")

            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-ModelPath",
                    str(model_path),
                    "-SummaryMarkdownPath",
                    str(markdown_path),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertFalse(markdown_path.exists(), result.stderr + result.stdout)
            self.assertIn("-SummaryMarkdownPath is only supported with -DryRun.", result.stderr + result.stdout)

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
