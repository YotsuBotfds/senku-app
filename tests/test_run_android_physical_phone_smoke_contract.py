import hashlib
import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_physical_phone_smoke.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"
VALIDATOR_SCRIPT = REPO_ROOT / "scripts" / "validate_android_physical_phone_smoke_summary.py"


class AndroidPhysicalPhoneSmokeContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_wrapper_contract_is_present(self):
        self.assertIn("[switch]$DryRun", self.script)
        self.assertIn("[string]$Serial", self.script)
        self.assertIn("[string]$AdbPath", self.script)
        self.assertIn("[string]$ApkPath", self.script)
        self.assertIn("[string]$FocusPath", self.script)
        self.assertIn("[string]$ScreenshotPath", self.script)
        self.assertIn("[string]$DumpPath", self.script)
        self.assertIn("[string]$LogcatPath", self.script)
        self.assertIn("[string[]]$RequiredText", self.script)
        self.assertIn("[switch]$Interact", self.script)
        self.assertIn("[string]$InteractionQuery", self.script)
        self.assertIn('Join-Path $env:LOCALAPPDATA "Android\\Sdk\\platform-tools\\adb.exe"', self.script)
        self.assertIn("Serial is required unless -DryRun is set", self.script)
        self.assertIn("Refusing emulator serial", self.script)
        self.assertIn("resolves to an emulator/qemu device", self.script)
        self.assertIn('$ErrorActionPreference = "Continue"', self.script)
        self.assertIn("$previousErrorActionPreference", self.script)
        self.assertIn('"devices"', self.script)
        self.assertIn('"install", "--no-streaming", "-r"', self.script)
        self.assertIn('"shell", "am", "start", "-n", $launchActivity', self.script)
        self.assertIn('"com.senku.mobile"', self.script)
        self.assertIn('"com.senku.mobile/.MainActivity"', self.script)
        self.assertIn('"dumpsys", "window", "windows"', self.script)
        self.assertIn('"dumpsys", "activity", "activities"', self.script)
        self.assertIn("function Test-FocusContainsLaunchActivity", self.script)
        self.assertIn("focus evidence did not show", self.script)
        self.assertIn('"exec-out" "screencap" "-p"', self.script)
        self.assertIn('"uiautomator", "dump", "/dev/tty"', self.script)
        self.assertIn("function Read-UiAutomatorDump", self.script)
        self.assertIn("/sdcard/senku_physical_smoke_ui.xml", self.script)
        self.assertIn("function Get-TextCheckSummary", self.script)
        self.assertIn("function Get-PhysicalDeviceIdentity", self.script)
        self.assertIn("function Get-FileSha256", self.script)
        self.assertIn("function Get-FocusedPackage", self.script)
        self.assertIn("function Get-OrientationEvidence", self.script)
        self.assertIn("text_checks", self.script)
        self.assertIn("function Wait-UiPostStepEvidence", self.script)
        self.assertIn("post_check", self.script)
        self.assertIn("dump_sha256", self.script)
        self.assertIn("function Invoke-SenkuSimpleInteraction", self.script)
        self.assertIn("tap_saved", self.script)
        self.assertIn("tap_query_field", self.script)
        self.assertIn("enter_query", self.script)
        self.assertIn("submit_query", self.script)
        self.assertIn('"input", "text"', self.script)
        self.assertIn('"input", "keyevent", "BACK"', self.script)
        self.assertIn('"logcat", "-d", "-v", "time"', self.script)
        self.assertIn("physical_device = $true", self.script)
        self.assertIn("launches_emulators = $false", self.script)
        self.assertIn("serial_required_unless_dry_run = $true", self.script)
        self.assertIn('$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"', self.script)
        self.assertIn('$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"', self.script)

    def test_failed_post_check_is_recorded_without_discarding_payload(self):
        self.assertIn("if (-not $postCheck.passed) {", self.script)
        self.assertIn('$message = "Post-step UI check for', self.script)
        self.assertIn('-PostCheck $postCheck))', self.script)
        self.assertIn('[void]$Steps.Add((New-InteractionStep -Name $Name -Status "failed"', self.script)
        self.assertIn("return $false", self.script)

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
                "scripts\\run_android_physical_phone_smoke.ps1",
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

    def test_dry_run_does_not_require_serial_or_adb_and_writes_summary(self):
        output_dir = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_dry_"))
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
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["status"], "dry_run_only")
            self.assertTrue(summary["dry_run"])
            self.assertTrue(summary["physical_device"])
            self.assertFalse(summary["launches_emulators"])
            self.assertTrue(summary["serial_required_unless_dry_run"])
            self.assertIsNone(summary["serial"])
            self.assertEqual(summary["adb_path_source"], "LOCALAPPDATA_ANDROID_SDK")
            self.assertTrue(summary["adb_path"].endswith("Android\\Sdk\\platform-tools\\adb.exe"))
            self.assertEqual(summary["package"], "com.senku.mobile")
            self.assertEqual(summary["launch_activity"], "com.senku.mobile/.MainActivity")
            self.assertIn("install --no-streaming -r", summary["commands"]["install"])
            self.assertIn("shell am start -n com.senku.mobile/.MainActivity", summary["commands"]["launch"])
            self.assertTrue(summary["evidence"]["focus_path"].endswith("focus.txt"))
            self.assertIsNone(summary["evidence"]["screenshot_path"])
            self.assertIsNone(summary["evidence"]["dump_path"])
            self.assertIsNone(summary["evidence"]["logcat_path"])
            self.assertNotIn("interaction", summary)
            self.assertNotIn("interaction", summary["commands"])
            self.assertTrue((output_dir / "summary.md").exists())
            markdown = (output_dir / "summary.md").read_text(encoding="utf-8-sig")
            self.assertIn("# Android Physical Phone Smoke", markdown)
            self.assertIn("- dry_run: True", markdown)
            self.assertIn("- launches_emulators: False", markdown)
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_dry_run_records_requested_text_checks_without_phone(self):
        output_dir = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_dry_text_"))
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
                    "-RequiredText",
                    "Field manual,Senku",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["text_checks"]["requested"], ["Field manual", "Senku"])
            self.assertEqual(summary["text_checks"]["passed"], [])
            self.assertEqual(summary["text_checks"]["missing"], [])
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_dry_run_interact_records_skipped_steps_and_validates(self):
        output_dir = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_dry_interact_"))
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
                    "-Interact",
                    "-InteractionQuery",
                    "boil water",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            summary_path = output_dir / "summary.json"
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["status"], "dry_run_only")
            self.assertTrue(summary["interaction"]["enabled"])
            self.assertEqual(summary["interaction"]["query"], "boil water")
            self.assertEqual(
                [(step["name"], step["status"]) for step in summary["interaction"]["steps"]],
                [
                    ("tap_saved", "skipped"),
                    ("tap_query_field", "skipped"),
                    ("enter_query", "skipped"),
                    ("submit_query", "skipped"),
                    ("back", "skipped"),
                ],
            )
            for step in summary["interaction"]["steps"]:
                self.assertEqual(step["message"], "Dry run only.")
                self.assertNotIn("post_check", step)

            validation = subprocess.run(
                ["python", str(VALIDATOR_SCRIPT), str(summary_path)],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(validation.returncode, 0, validation.stderr + validation.stdout)
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_without_dry_run_requires_serial_before_writing_summary(self):
        output_dir = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_no_serial_"))
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
            self.assertIn("Serial is required unless -DryRun is set", result.stderr + result.stdout)
            self.assertFalse((output_dir / "summary.json").exists())
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_emulator_serial_is_rejected_before_adb(self):
        output_dir = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_emulator_serial_"))
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
                    "-Serial",
                    "emulator-5554",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("Refusing emulator serial", result.stderr + result.stdout)
            self.assertFalse((output_dir / "summary.json").exists())
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_fake_adb_real_run_captures_expected_evidence_and_commands(self):
        root = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_fake_adb_"))
        try:
            output_dir = root / "out"
            apk = root / "app-debug.apk"
            adb = root / "adb.cmd"
            calls = root / "adb_calls.txt"
            screenshot = root / "screen.png"
            dump = root / "dump.xml"
            logcat = root / "logcat.txt"
            apk.write_text("apk", encoding="utf-8")
            adb.write_text(
                "\r\n".join(
                    [
                        "@echo off",
                        f"echo %*>> \"{calls}\"",
                        "if \"%1\"==\"devices\" (",
                        "  echo List of devices attached",
                        "  echo R5CT123456A\tdevice",
                        "  exit /b 0",
                        ")",
                        "if \"%5\"==\"ro.kernel.qemu\" (echo 0& exit /b 0)",
                        "if \"%5\"==\"ro.boot.qemu\" (echo 0& exit /b 0)",
                        "if \"%4\"==\"getprop\" (echo fake-%5& exit /b 0)",
                        "if \"%3\"==\"install\" (echo Success& exit /b 0)",
                        "if \"%4\"==\"am\" (echo Starting: Intent& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"window\" (echo mCurrentFocus=Window{u0 com.senku.mobile/.MainActivity}& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"activity\" (echo topResumedActivity=com.senku.mobile/.MainActivity& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"input\" (echo SurfaceOrientation: 0& exit /b 0)",
                        "if \"%3\"==\"exec-out\" (echo PNGDATA& exit /b 0)",
                        "if \"%4\"==\"uiautomator\" (echo ^<hierarchy^>^<node text=Senku /^>^<node text=Library /^>^</hierarchy^>& exit /b 0)",
                        "if \"%3\"==\"logcat\" (echo 04-30 Senku log line& exit /b 0)",
                        "echo unexpected args: %*",
                        "exit /b 1",
                    ]
                ),
                encoding="utf-8",
            )

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
                    "-Serial",
                    "R5CT123456A",
                    "-ApkPath",
                    str(apk),
                    "-AdbPath",
                    str(adb),
                    "-ScreenshotPath",
                    str(screenshot),
                    "-DumpPath",
                    str(dump),
                    "-LogcatPath",
                    str(logcat),
                    "-RequiredText",
                    "Senku,Library",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["status"], "completed")
            self.assertFalse(summary["dry_run"])
            self.assertEqual(summary["serial"], "R5CT123456A")
            self.assertEqual(summary["adb_path_source"], "override")
            self.assertEqual(summary["apk_sha256"], hashlib.sha256(b"apk").hexdigest())
            self.assertEqual(summary["device_identity"]["serial"], "R5CT123456A")
            self.assertEqual(summary["device_identity"]["manufacturer"], "fake-ro.product.manufacturer")
            self.assertEqual(summary["device_identity"]["model"], "fake-ro.product.model")
            self.assertFalse(summary["launches_emulators"])
            self.assertTrue(summary["evidence"]["focus_contains_launch_activity"])
            self.assertEqual(summary["evidence"]["focused_package"], "com.senku.mobile")
            self.assertEqual(summary["evidence"]["orientation"]["rotation"], 0)
            self.assertEqual(summary["evidence"]["orientation"]["orientation"], "portrait")
            self.assertTrue((output_dir / "focus.txt").exists())
            self.assertIn("mCurrentFocus", (output_dir / "focus.txt").read_text(encoding="utf-8-sig"))
            self.assertTrue(screenshot.exists())
            self.assertIn("<hierarchy>", dump.read_text(encoding="utf-8-sig"))
            self.assertRegex(summary["evidence"]["artifact_hashes"]["focus_sha256"], r"^[0-9a-f]{64}$")
            self.assertRegex(summary["evidence"]["artifact_hashes"]["screenshot_sha256"], r"^[0-9a-f]{64}$")
            self.assertRegex(summary["evidence"]["artifact_hashes"]["dump_sha256"], r"^[0-9a-f]{64}$")
            self.assertRegex(summary["evidence"]["artifact_hashes"]["logcat_sha256"], r"^[0-9a-f]{64}$")
            self.assertEqual(summary["text_checks"]["requested"], ["Senku", "Library"])
            self.assertEqual(summary["text_checks"]["passed"], ["Senku", "Library"])
            self.assertEqual(summary["text_checks"]["missing"], [])
            self.assertIn("Senku log line", logcat.read_text(encoding="utf-8-sig"))
            call_text = calls.read_text(encoding="utf-8")
            self.assertIn("devices", call_text)
            self.assertIn("-s R5CT123456A install --no-streaming -r", call_text)
            self.assertIn("-s R5CT123456A shell am start -n com.senku.mobile/.MainActivity", call_text)
            self.assertIn("-s R5CT123456A exec-out screencap -p", call_text)
        finally:
            shutil.rmtree(root, ignore_errors=True)

    def test_fake_adb_interaction_success_records_step_statuses(self):
        root = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_interact_"))
        try:
            output_dir = root / "out"
            apk = root / "app-debug.apk"
            adb = root / "adb.cmd"
            calls = root / "adb_calls.txt"
            apk.write_text("apk", encoding="utf-8")
            adb.write_text(
                "\r\n".join(
                    [
                        "@echo off",
                        f"echo %*>> \"{calls}\"",
                        "if \"%1\"==\"devices\" (",
                        "  echo List of devices attached",
                        "  echo R5CT123456A\tdevice",
                        "  exit /b 0",
                        ")",
                        "if \"%5\"==\"ro.kernel.qemu\" (echo 0& exit /b 0)",
                        "if \"%5\"==\"ro.boot.qemu\" (echo 0& exit /b 0)",
                        "if \"%4\"==\"getprop\" (echo fake-%5& exit /b 0)",
                        "if \"%3\"==\"install\" (echo Success& exit /b 0)",
                        "if \"%4\"==\"am\" (echo Starting: Intent& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"window\" (echo mCurrentFocus=Window{u0 com.senku.mobile/.MainActivity}& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"activity\" (echo topResumedActivity=com.senku.mobile/.MainActivity& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"input\" (echo SurfaceOrientation: 0& exit /b 0)",
                        "if \"%3\"==\"exec-out\" (echo PNGDATA& exit /b 0)",
                        "if \"%4\"==\"uiautomator\" (echo ^<hierarchy^>^<node text=\"Saved\" content-desc=\"Saved\" bounds=\"[10,20][110,120]\" /^>^<node class=\"android.widget.EditText\" resource-id=\"com.senku.mobile:id/search\" bounds=\"[30,200][330,260]\" /^>^</hierarchy^>& exit /b 0)",
                        "if \"%4\"==\"input\" if \"%5\"==\"tap\" (echo tapped& exit /b 0)",
                        "if \"%4\"==\"input\" if \"%5\"==\"text\" (echo typed& exit /b 0)",
                        "if \"%4\"==\"input\" if \"%5\"==\"keyevent\" (echo key& exit /b 0)",
                        "exit /b 1",
                    ]
                ),
                encoding="utf-8",
            )

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
                    "-Serial",
                    "R5CT123456A",
                    "-ApkPath",
                    str(apk),
                    "-AdbPath",
                    str(adb),
                    "-Interact",
                    "-InteractionQuery",
                    "boil water",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertTrue(summary["interaction"]["enabled"])
            self.assertEqual(summary["interaction"]["query"], "boil water")
            self.assertEqual(
                [(step["name"], step["status"]) for step in summary["interaction"]["steps"]],
                [
                    ("tap_saved", "success"),
                    ("tap_query_field", "success"),
                    ("enter_query", "success"),
                    ("submit_query", "success"),
                    ("back", "success"),
                ],
            )
            step_by_name = {step["name"]: step for step in summary["interaction"]["steps"]}
            for step_name in ["tap_saved", "submit_query", "back"]:
                self.assertIn("post_check", step_by_name[step_name])
                self.assertTrue(step_by_name[step_name]["post_check"]["passed"])
                self.assertGreater(step_by_name[step_name]["post_check"]["dump_length"], 0)
                self.assertRegex(step_by_name[step_name]["post_check"]["dump_sha256"], r"^[0-9a-f]{64}$")
                self.assertGreaterEqual(len(step_by_name[step_name]["post_check"]["matched_text"]), 1)
                self.assertIn("Saved", step_by_name[step_name]["post_check"]["ui_text_sample"])
            self.assertIn("Search", step_by_name["submit_query"]["post_check"]["matched_text"])
            self.assertNotIn("post_check", step_by_name["tap_query_field"])
            self.assertNotIn("post_check", step_by_name["enter_query"])
            call_text = calls.read_text(encoding="utf-8")
            self.assertIn("-s R5CT123456A shell input tap 60 70", call_text)
            self.assertIn("-s R5CT123456A shell input tap 180 230", call_text)
            self.assertIn("-s R5CT123456A shell input text boil%swater", call_text)
            self.assertIn("-s R5CT123456A shell input keyevent ENTER", call_text)
            self.assertIn("-s R5CT123456A shell input keyevent BACK", call_text)
        finally:
            shutil.rmtree(root, ignore_errors=True)

    def test_fake_adb_interaction_failure_records_step_status_before_failing(self):
        root = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_interact_fail_"))
        try:
            output_dir = root / "out"
            apk = root / "app-debug.apk"
            adb = root / "adb.cmd"
            apk.write_text("apk", encoding="utf-8")
            adb.write_text(
                "\r\n".join(
                    [
                        "@echo off",
                        "if \"%1\"==\"devices\" (",
                        "  echo List of devices attached",
                        "  echo R5CT123456A\tdevice",
                        "  exit /b 0",
                        ")",
                        "if \"%5\"==\"ro.kernel.qemu\" (echo 0& exit /b 0)",
                        "if \"%5\"==\"ro.boot.qemu\" (echo 0& exit /b 0)",
                        "if \"%4\"==\"getprop\" (echo fake-%5& exit /b 0)",
                        "if \"%3\"==\"install\" (echo Success& exit /b 0)",
                        "if \"%4\"==\"am\" (echo Starting: Intent& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"window\" (echo mCurrentFocus=Window{u0 com.senku.mobile/.MainActivity}& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"activity\" (echo topResumedActivity=com.senku.mobile/.MainActivity& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"input\" (echo SurfaceOrientation: 0& exit /b 0)",
                        "if \"%3\"==\"exec-out\" (echo PNGDATA& exit /b 0)",
                        "if \"%4\"==\"uiautomator\" (echo ^<hierarchy^>^<node class=\"android.widget.EditText\" bounds=\"[30,200][330,260]\" /^>^</hierarchy^>& exit /b 0)",
                        "if \"%4\"==\"input\" (exit /b 0)",
                        "exit /b 1",
                    ]
                ),
                encoding="utf-8",
            )

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
                    "-Serial",
                    "R5CT123456A",
                    "-ApkPath",
                    str(apk),
                    "-AdbPath",
                    str(adb),
                    "-Interact",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("interaction failed step", result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["interaction"]["steps"][0]["name"], "tap_saved")
            self.assertEqual(summary["interaction"]["steps"][0]["status"], "failed")
            self.assertIn("Saved control was not found", summary["interaction"]["steps"][0]["message"])
        finally:
            shutil.rmtree(root, ignore_errors=True)

    def test_fake_adb_post_check_failure_records_evidence_payload(self):
        root = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_post_check_fail_"))
        try:
            output_dir = root / "out"
            apk = root / "app-debug.apk"
            adb = root / "adb.cmd"
            entered = root / "entered.txt"
            apk.write_text("apk", encoding="utf-8")
            adb.write_text(
                "\r\n".join(
                    [
                        "@echo off",
                        "if \"%1\"==\"devices\" (",
                        "  echo List of devices attached",
                        "  echo R5CT123456A\tdevice",
                        "  exit /b 0",
                        ")",
                        "if \"%5\"==\"ro.kernel.qemu\" (echo 0& exit /b 0)",
                        "if \"%5\"==\"ro.boot.qemu\" (echo 0& exit /b 0)",
                        "if \"%4\"==\"getprop\" (echo fake-%5& exit /b 0)",
                        "if \"%3\"==\"install\" (echo Success& exit /b 0)",
                        "if \"%4\"==\"am\" (echo Starting: Intent& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"window\" (echo mCurrentFocus=Window{u0 com.senku.mobile/.MainActivity}& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"activity\" (echo topResumedActivity=com.senku.mobile/.MainActivity& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"input\" (echo SurfaceOrientation: 0& exit /b 0)",
                        "if \"%3\"==\"exec-out\" (echo PNGDATA& exit /b 0)",
                        f"if \"%4\"==\"uiautomator\" if exist \"{entered}\" (echo ^<hierarchy^>^<node text=\"No matching result\" /^>^</hierarchy^>& exit /b 0)",
                        "if \"%4\"==\"uiautomator\" (echo ^<hierarchy^>^<node text=\"Saved\" content-desc=\"Saved\" bounds=\"[10,20][110,120]\" /^>^<node class=\"android.widget.EditText\" resource-id=\"com.senku.mobile:id/search\" bounds=\"[30,200][330,260]\" /^>^</hierarchy^>& exit /b 0)",
                        "if \"%4\"==\"input\" if \"%5\"==\"tap\" (echo tapped& exit /b 0)",
                        "if \"%4\"==\"input\" if \"%5\"==\"text\" (echo typed& exit /b 0)",
                        f"if \"%4\"==\"input\" if \"%5\"==\"keyevent\" if \"%6\"==\"ENTER\" (echo entered> \"{entered}\"& exit /b 0)",
                        "if \"%4\"==\"input\" if \"%5\"==\"keyevent\" (echo key& exit /b 0)",
                        "exit /b 1",
                    ]
                ),
                encoding="utf-8",
            )

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
                    "-Serial",
                    "R5CT123456A",
                    "-ApkPath",
                    str(apk),
                    "-AdbPath",
                    str(adb),
                    "-Interact",
                    "-InteractionQuery",
                    "boil water",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("interaction failed step", result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            step_by_name = {step["name"]: step for step in summary["interaction"]["steps"]}
            failed_step = step_by_name["submit_query"]
            self.assertEqual(failed_step["status"], "failed")
            self.assertIn("Post-step UI check", failed_step["message"])
            self.assertIn("post_check", failed_step)
            post_check = failed_step["post_check"]
            self.assertFalse(post_check["passed"])
            self.assertEqual(
                post_check["expected_any_text"],
                ["boil water", "Answer", "Results", "Search", "Ask"],
            )
            self.assertEqual(post_check["matched_text"], [])
            self.assertIn("No matching result", post_check["ui_text_sample"])
            self.assertGreater(post_check["dump_length"], 0)
            self.assertRegex(post_check["dump_sha256"], r"^[0-9a-f]{64}$")
            summary_markdown = (output_dir / "summary.md").read_text(encoding="utf-8-sig")
            self.assertIn("- interaction_post_check: submit_query=", summary_markdown)
            self.assertIn('"passed":false', summary_markdown)
            self.assertIn('"ui_text_sample":["No matching result"]', summary_markdown)
        finally:
            shutil.rmtree(root, ignore_errors=True)

    def test_fake_adb_real_run_falls_back_to_device_dump_file(self):
        root = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_dump_fallback_"))
        try:
            output_dir = root / "out"
            apk = root / "app-debug.apk"
            adb = root / "adb.cmd"
            calls = root / "adb_calls.txt"
            dump = root / "dump.xml"
            device_dump_path = "/sdcard/senku_physical_smoke_ui.xml"
            apk.write_text("apk", encoding="utf-8")
            adb.write_text(
                "\r\n".join(
                    [
                        "@echo off",
                        f"echo %*>> \"{calls}\"",
                        "if \"%1\"==\"devices\" (",
                        "  echo List of devices attached",
                        "  echo R5CT123456A\tdevice",
                        "  exit /b 0",
                        ")",
                        "if \"%5\"==\"ro.kernel.qemu\" (echo 0& exit /b 0)",
                        "if \"%5\"==\"ro.boot.qemu\" (echo 0& exit /b 0)",
                        "if \"%4\"==\"getprop\" (echo fake-%5& exit /b 0)",
                        "if \"%3\"==\"install\" (echo Success& exit /b 0)",
                        "if \"%4\"==\"am\" (echo Starting: Intent& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"window\" (echo mCurrentFocus=Window{u0 com.senku.mobile/.MainActivity}& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"activity\" (echo topResumedActivity=com.senku.mobile/.MainActivity& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"input\" (echo SurfaceOrientation: 0& exit /b 0)",
                        "if \"%3\"==\"exec-out\" (echo PNGDATA& exit /b 0)",
                        "if \"%4\"==\"uiautomator\" if \"%6\"==\"/dev/tty\" (echo direct dump unavailable& exit /b 1)",
                        f"if \"%4\"==\"uiautomator\" if \"%6\"==\"{device_dump_path}\" (echo UI hierarchy dumped to: {device_dump_path}& exit /b 0)",
                        f"if \"%4\"==\"cat\" if \"%5\"==\"{device_dump_path}\" (echo ^<hierarchy^>^<node text=Senku /^>^<node text=Library /^>^</hierarchy^>& exit /b 0)",
                        f"if \"%4\"==\"rm\" if \"%6\"==\"{device_dump_path}\" (exit /b 0)",
                        "exit /b 1",
                    ]
                ),
                encoding="utf-8",
            )

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
                    "-Serial",
                    "R5CT123456A",
                    "-ApkPath",
                    str(apk),
                    "-AdbPath",
                    str(adb),
                    "-DumpPath",
                    str(dump),
                    "-RequiredText",
                    "Senku,Library",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["status"], "completed")
            self.assertEqual(summary["text_checks"]["passed"], ["Senku", "Library"])
            self.assertEqual(summary["text_checks"]["missing"], [])
            self.assertIn("<hierarchy>", dump.read_text(encoding="utf-8-sig"))
            call_text = calls.read_text(encoding="utf-8")
            self.assertIn("-s R5CT123456A shell uiautomator dump /dev/tty", call_text)
            self.assertIn(f"-s R5CT123456A shell uiautomator dump {device_dump_path}", call_text)
            self.assertIn(f"-s R5CT123456A shell cat {device_dump_path}", call_text)
            self.assertIn(f"-s R5CT123456A shell rm -f {device_dump_path}", call_text)
        finally:
            shutil.rmtree(root, ignore_errors=True)

    def test_fake_adb_missing_required_text_fails_after_writing_summary(self):
        root = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_missing_text_"))
        try:
            output_dir = root / "out"
            apk = root / "app-debug.apk"
            adb = root / "adb.cmd"
            apk.write_text("apk", encoding="utf-8")
            adb.write_text(
                "\r\n".join(
                    [
                        "@echo off",
                        "if \"%1\"==\"devices\" (",
                        "  echo List of devices attached",
                        "  echo R5CT123456A\tdevice",
                        "  exit /b 0",
                        ")",
                        "if \"%5\"==\"ro.kernel.qemu\" (echo 0& exit /b 0)",
                        "if \"%5\"==\"ro.boot.qemu\" (echo 0& exit /b 0)",
                        "if \"%4\"==\"getprop\" (echo fake-%5& exit /b 0)",
                        "if \"%3\"==\"install\" (echo Success& exit /b 0)",
                        "if \"%4\"==\"am\" (echo Starting: Intent& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"window\" (echo mCurrentFocus=Window{u0 com.senku.mobile/.MainActivity}& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"activity\" (echo topResumedActivity=com.senku.mobile/.MainActivity& exit /b 0)",
                        "if \"%4\"==\"dumpsys\" if \"%5\"==\"input\" (echo SurfaceOrientation: 0& exit /b 0)",
                        "if \"%3\"==\"exec-out\" (echo PNGDATA& exit /b 0)",
                        "if \"%4\"==\"uiautomator\" (echo ^<hierarchy^>^<node text=Senku /^>^</hierarchy^>& exit /b 0)",
                        "exit /b 1",
                    ]
                ),
                encoding="utf-8",
            )

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
                    "-Serial",
                    "R5CT123456A",
                    "-ApkPath",
                    str(apk),
                    "-AdbPath",
                    str(adb),
                    "-RequiredText",
                    "Senku,Library",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("missing required text fragment", result.stderr + result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["text_checks"]["requested"], ["Senku", "Library"])
            self.assertEqual(summary["text_checks"]["passed"], ["Senku"])
            self.assertEqual(summary["text_checks"]["missing"], ["Library"])
        finally:
            shutil.rmtree(root, ignore_errors=True)

    def test_fake_adb_qemu_device_is_rejected(self):
        root = Path(tempfile.mkdtemp(prefix="physical_phone_smoke_qemu_"))
        try:
            output_dir = root / "out"
            apk = root / "app-debug.apk"
            adb = root / "adb.cmd"
            apk.write_text("apk", encoding="utf-8")
            adb.write_text(
                "\r\n".join(
                    [
                        "@echo off",
                        "if \"%1\"==\"devices\" (",
                        "  echo List of devices attached",
                        "  echo R5CT123456A\tdevice",
                        "  exit /b 0",
                        ")",
                        "if \"%5\"==\"ro.kernel.qemu\" (echo 1& exit /b 0)",
                        "if \"%5\"==\"ro.boot.qemu\" (echo 0& exit /b 0)",
                        "if \"%4\"==\"getprop\" (echo fake-%5& exit /b 0)",
                        "exit /b 1",
                    ]
                ),
                encoding="utf-8",
            )

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
                    "-Serial",
                    "R5CT123456A",
                    "-ApkPath",
                    str(apk),
                    "-AdbPath",
                    str(adb),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("resolves to an emulator/qemu device", result.stderr + result.stdout)
            self.assertFalse((output_dir / "summary.json").exists())
        finally:
            shutil.rmtree(root, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
