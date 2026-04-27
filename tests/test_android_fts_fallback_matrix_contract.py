import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_fts_fallback_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidFtsFallbackMatrixContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_wrapper_contract_is_present(self):
        self.assertIn('Resolve-AndroidHarnessDeviceList -Devices $Devices', self.script)
        self.assertIn('"emulator-5554"', self.script)
        self.assertIn('"emulator-5556"', self.script)
        self.assertIn('"emulator-5558"', self.script)
        self.assertIn('"emulator-5560"', self.script)
        self.assertIn("com.senku.mobile.PackRepositoryFtsFallbackAndroidTest", self.script)
        self.assertIn("com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner", self.script)
        self.assertIn('"shell", "am", "instrument", "-w"', self.script)
        self.assertIn('"fts4_fallback"', self.script)
        self.assertIn("$expectedTests = 3", self.script)
        self.assertIn("passed_count =", self.script)
        self.assertIn("failed_devices =", self.script)
        self.assertIn('$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"', self.script)

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
                "scripts\\run_android_fts_fallback_matrix.ps1",
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

    def test_dry_run_writes_per_device_json_and_summary(self):
        output_dir = Path(tempfile.mkdtemp(prefix="fts_fallback_matrix_"))
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
                    "-Devices",
                    "emulator-5554, emulator-5556",
                    "-DryRun",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["passed_count"], 2)
            self.assertEqual(summary["failed_devices"], [])
            self.assertEqual(summary["expected_tests"], 3)
            self.assertEqual(summary["runtime_evidence"], "fts4_fallback")
            self.assertEqual(summary["devices"], ["emulator-5554", "emulator-5556"])

            first_device = json.loads((output_dir / "emulator-5554.json").read_text(encoding="utf-8-sig"))
            self.assertTrue(first_device["passed"])
            self.assertTrue(first_device["dry_run"])
            self.assertIn("adb -s emulator-5554 shell am instrument -w", first_device["command"])
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
