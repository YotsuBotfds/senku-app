import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_managed_device_smoke.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidManagedDeviceSmokeContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_wrapper_is_dry_run_only_and_documents_non_acceptance_boundary(self):
        self.assertIn("[switch]$DryRun", self.script)
        self.assertIn("if (-not $DryRun)", self.script)
        self.assertIn("dry-run-only", self.script)
        self.assertIn("must not launch Gradle Managed Devices yet", self.script)
        self.assertIn('"-Psenku.enableManagedDevices=true"', self.script)
        self.assertIn('"senkuManagedSmoke"', self.script)
        self.assertIn("non_acceptance_evidence = $true", self.script)
        self.assertIn("would_launch_emulators = $false", self.script)
        self.assertIn("managed_devices_launched = $false", self.script)
        self.assertIn("acceptance_evidence = $false", self.script)
        self.assertIn("fixed four-emulator evidence remains primary", self.script)
        self.assertIn('$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"', self.script)
        self.assertIn('$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"', self.script)

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
                "scripts\\run_android_managed_device_smoke.ps1",
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

    def test_dry_run_writes_repo_shaped_summary(self):
        output_dir = Path(tempfile.mkdtemp(prefix="managed_device_smoke_"))
        try:
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-OutputDir",
                    str(output_dir),
                    "-DryRun",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("fixed four-emulator evidence remains primary", result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["status"], "dry_run_only")
            self.assertTrue(summary["dry_run"])
            self.assertTrue(summary["non_acceptance_evidence"])
            self.assertEqual(summary["gradle_property"], "-Psenku.enableManagedDevices=true")
            self.assertEqual(summary["task_group"], "senkuManagedSmoke")
            self.assertEqual(summary["task_name"], ":app:senkuManagedSmoke")
            self.assertIn("-Psenku.enableManagedDevices=true", summary["planned_command"])
            self.assertIn(":app:senkuManagedSmoke", summary["planned_command"])
            self.assertFalse(summary["would_launch_emulators"])
            self.assertFalse(summary["managed_devices_launched"])
            self.assertFalse(summary["acceptance_evidence"])
            self.assertEqual(summary["primary_evidence"], "fixed_four_emulator_matrix")
            self.assertIn("fixed four-emulator evidence remains primary", summary["stop_line"])
            self.assertTrue((output_dir / "summary.md").exists())
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_without_dry_run_fails_before_writing_artifacts(self):
        output_dir = Path(tempfile.mkdtemp(prefix="managed_device_smoke_no_dry_run_"))
        try:
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-OutputDir",
                    str(output_dir),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("dry-run-only", result.stderr + result.stdout)
            self.assertFalse((output_dir / "summary.json").exists())
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
