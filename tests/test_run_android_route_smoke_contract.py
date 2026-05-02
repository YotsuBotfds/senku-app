import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_route_smoke.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidRouteSmokeContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_wrapper_targets_live_safe_route_smoke_only(self):
        self.assertIn("PackRepositoryCurrentHeadRouteSmokeAndroidTest", self.script)
        self.assertNotIn("PackRepositoryCurrentHeadRouteParityAndroidTest", self.script)
        self.assertIn("com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner", self.script)
        self.assertIn('"shell", "am", "instrument", "-w"', self.script)
        self.assertIn("$expectedTests = 1", self.script)
        self.assertIn(":app:assembleDebug :app:assembleDebugAndroidTest", self.script)
        self.assertIn("Acquire-AndroidHarnessDeviceLock", self.script)
        self.assertIn("Resolve-AndroidHarnessDeviceList -Devices $Devices", self.script)
        self.assertIn("[switch]$SkipBuild", self.script)
        self.assertIn("[switch]$SkipInstall", self.script)
        self.assertIn("[switch]$SkipDeviceLock", self.script)
        self.assertIn('"emulator-5554"', self.script)

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
                "scripts\\run_android_route_smoke.ps1",
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

    def test_dry_run_writes_summary_and_per_device_result(self):
        output_dir = Path(tempfile.mkdtemp(prefix="route_smoke_"))
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
            self.assertEqual(summary["failed_device_results"], [])
            self.assertEqual(summary["expected_tests"], 1)
            self.assertEqual(
                summary["instrumentation_class"],
                "com.senku.mobile.PackRepositoryCurrentHeadRouteSmokeAndroidTest",
            )
            self.assertTrue(summary["dry_run"])
            self.assertTrue(summary["build_skipped"])
            self.assertFalse(summary["install_skipped"])
            self.assertEqual(summary["device_lock_posture"], "not_acquired_dry_run")
            self.assertFalse(summary["device_lock_used"])
            self.assertEqual(summary["host_adb_platform_tools_version"], "dry_run")
            self.assertEqual(summary["devices"], ["emulator-5554", "emulator-5556"])

            summary_markdown = (output_dir / "summary.md").read_text(encoding="utf-8-sig")
            self.assertIn(
                "- instrumentation_class: com.senku.mobile.PackRepositoryCurrentHeadRouteSmokeAndroidTest",
                summary_markdown,
            )
            self.assertIn("- expected_tests: 1", summary_markdown)
            self.assertIn("- devices: emulator-5554, emulator-5556", summary_markdown)
            self.assertIn("- passed_count: 2", summary_markdown)
            self.assertIn("- failed_count: 0", summary_markdown)
            self.assertIn("- dry_run: true", summary_markdown)

            first_device = json.loads((output_dir / "emulator-5554.json").read_text(encoding="utf-8-sig"))
            self.assertTrue(first_device["passed"])
            self.assertEqual(
                first_device["instrumentation_class"],
                "com.senku.mobile.PackRepositoryCurrentHeadRouteSmokeAndroidTest",
            )
            self.assertIn("adb -s emulator-5554 shell am instrument -w", first_device["command"])
            self.assertIn("PackRepositoryCurrentHeadRouteSmokeAndroidTest", first_device["command"])
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_dry_run_fails_closed_when_no_devices_resolve(self):
        output_dir = Path(tempfile.mkdtemp(prefix="route_smoke_empty_"))
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
                    " , ",
                    "-DryRun",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("No Android devices resolved", result.stderr + result.stdout)
            self.assertFalse((output_dir / "summary.json").exists())
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
