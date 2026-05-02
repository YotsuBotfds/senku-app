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
        self.assertIn("Invoke-AndroidAdbCommandCapture", self.script)
        self.assertNotIn("$output = & $adb @AdbArgs", self.script)
        self.assertIn("[int]$InstallTimeoutMilliseconds = 180000", self.script)
        self.assertIn("[int]$InstrumentationTimeoutMilliseconds = 300000", self.script)
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
            self.assertEqual(summary["timed_out_devices"], [])
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
            self.assertIn("- timed_out_devices: none", summary_markdown)
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

    def test_fake_adb_records_instrumentation_nonzero_failure_summary(self):
        output_dir = Path(tempfile.mkdtemp(prefix="route_smoke_nonzero_"))
        try:
            adb = self._write_fake_adb(output_dir, "instrument_nonzero")
            result = self._run_with_fake_adb(output_dir, adb)

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            device = json.loads((output_dir / "emulator-5554.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["passed_count"], 0)
            self.assertEqual(summary["failed_devices"], ["emulator-5554"])
            self.assertEqual(summary["timed_out_devices"], [])
            self.assertEqual(summary["failed_device_results"][0]["failure_phase"], "instrumentation_exit")
            self.assertFalse(device["passed"])
            self.assertEqual(device["failure_phase"], "instrumentation_exit")
            self.assertEqual(device["exit_code"], 13)
            self.assertFalse(device["timed_out"])
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_fake_adb_records_instrumentation_failures_output(self):
        output_dir = Path(tempfile.mkdtemp(prefix="route_smoke_failures_"))
        try:
            adb = self._write_fake_adb(output_dir, "instrument_failures")
            result = self._run_with_fake_adb(output_dir, adb)

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            device = json.loads((output_dir / "emulator-5554.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(device["failure_phase"], "instrumentation_failure")
            self.assertIn("FAILURES!!!", device["stdout"])
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_fake_adb_records_app_install_failure(self):
        output_dir = Path(tempfile.mkdtemp(prefix="route_smoke_app_install_"))
        try:
            adb = self._write_fake_adb(output_dir, "app_install_fail")
            result = self._run_with_fake_adb(output_dir, adb)

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            device = json.loads((output_dir / "emulator-5554.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(device["failure_phase"], "install_app")
            self.assertIn("app install failed", device["stdout"])
            self.assertIsNone(device["install_test_command"])
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_fake_adb_records_test_install_failure(self):
        output_dir = Path(tempfile.mkdtemp(prefix="route_smoke_test_install_"))
        try:
            adb = self._write_fake_adb(output_dir, "test_install_fail")
            result = self._run_with_fake_adb(output_dir, adb)

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            device = json.loads((output_dir / "emulator-5554.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(device["failure_phase"], "install_test")
            self.assertIn("test install failed", device["stdout"])
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_fake_adb_records_timeout_summary(self):
        output_dir = Path(tempfile.mkdtemp(prefix="route_smoke_timeout_"))
        try:
            adb = self._write_fake_adb(output_dir, "instrument_timeout")
            result = self._run_with_fake_adb(
                output_dir,
                adb,
                extra_args=["-InstrumentationTimeoutMilliseconds", "100"],
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            device = json.loads((output_dir / "emulator-5554.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["timed_out_devices"], ["emulator-5554"])
            self.assertEqual(summary["failed_device_results"][0]["failure_phase"], "instrumentation_timeout")
            self.assertEqual(device["failure_phase"], "instrumentation_timeout")
            self.assertTrue(device["timed_out"])
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

    def _run_with_fake_adb(self, output_dir, adb, extra_args=None):
        args = [
            "powershell",
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-File",
            str(SCRIPT),
            "-OutputDir",
            str(output_dir),
            "-Devices",
            "emulator-5554",
            "-SkipBuild",
            "-SkipDeviceLock",
            "-AdbPath",
            str(adb),
        ]
        if extra_args:
            args.extend(extra_args)
        return subprocess.run(
            args,
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

    def _write_fake_adb(self, output_dir, mode):
        fake_ps1 = output_dir / "fake_adb.ps1"
        fake_ps1.write_text(
            "\n".join(
                [
                    f"$mode = '{mode}'",
                    "if ($args.Count -gt 0 -and $args[0] -eq 'version') {",
                    "  Write-Output 'Android Debug Bridge version 1.0.41'",
                    "  Write-Output 'Version 37.0.0-14910828'",
                    "  exit 0",
                    "}",
                    "if ($args.Count -gt 3 -and $args[2] -eq 'install') {",
                    "  if ($args -contains '-t') {",
                    "    if ($mode -eq 'test_install_fail') { Write-Output 'test install failed'; exit 8 }",
                    "    Write-Output 'Success'",
                    "    exit 0",
                    "  }",
                    "  if ($mode -eq 'app_install_fail') { Write-Output 'app install failed'; exit 7 }",
                    "  Write-Output 'Success'",
                    "  exit 0",
                    "}",
                    "if ($args.Count -gt 5 -and $args[2] -eq 'shell' -and $args[3] -eq 'am' -and $args[4] -eq 'instrument') {",
                    "  if ($mode -eq 'instrument_nonzero') { Write-Output 'instrumentation failed'; exit 13 }",
                    "  if ($mode -eq 'instrument_failures') { Write-Output 'FAILURES!!!'; exit 0 }",
                    "  if ($mode -eq 'instrument_timeout') { Start-Sleep -Milliseconds 600; Write-Output 'late instrumentation output'; exit 0 }",
                    "  Write-Output 'com.senku.mobile.PackRepositoryCurrentHeadRouteSmokeAndroidTest:.'",
                    "  Write-Output 'OK (1 test)'",
                    "  exit 0",
                    "}",
                    "Write-Output ('unrecognized fake adb args: ' + ($args -join ' '))",
                    "exit 0",
                ]
            )
            + "\n",
            encoding="utf-8",
        )
        fake_adb = output_dir / "fake_adb.cmd"
        fake_adb.write_text(
            "\n".join(
                [
                    "@echo off",
                    'powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0fake_adb.ps1" %*',
                    "exit /b %ERRORLEVEL%",
                ]
            )
            + "\n",
            encoding="utf-8",
        )
        return fake_adb


if __name__ == "__main__":
    unittest.main()
