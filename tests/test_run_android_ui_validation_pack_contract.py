import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_ui_validation_pack.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidUiValidationPackContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_physical_install_retries_with_no_streaming_without_changing_emulators(self):
        self.assertIn("function Install-SenkuApk", self.script)
        self.assertIn('"install", "-r", $Apk', self.script)
        self.assertIn('if ($Device -notlike "emulator-*") {', self.script)
        self.assertIn('"install", "--no-streaming", "-r", $Apk', self.script)
        self.assertIn(
            "retrying with adb install --no-streaming on physical device",
            self.script,
        )
        self.assertLess(
            self.script.index('if ($Device -notlike "emulator-*") {'),
            self.script.index('"install", "--no-streaming", "-r", $Apk'),
        )

    def test_wm_size_override_is_guarded_to_emulators(self):
        self.assertIn("function Set-DeviceSizeOverride", self.script)
        self.assertIn("function Reset-DeviceSizeOverride", self.script)
        self.assertIn('if ($Device -notlike "emulator-*") {', self.script)
        self.assertIn("Refusing wm size override on physical device", self.script)
        self.assertIn("Skipping wm size reset on physical device", self.script)
        self.assertLess(
            self.script.index("Refusing wm size override on physical device"),
            self.script.index('& $adb -s $Device shell wm size "$Width`x$Height"'),
        )
        self.assertLess(
            self.script.index("Skipping wm size reset on physical device"),
            self.script.index("& $adb -s $Device shell wm size reset"),
        )

    def test_child_script_timeout_contract_fails_closed_and_records_evidence(self):
        start = self.script.index("function Invoke-PowerShellChildScript {")
        end = self.script.index("function Set-DeviceOrientation", start)
        body = self.script[start:end]

        self.assertIn("Start-Job -ScriptBlock", body)
        self.assertIn("Wait-Job -Id $job.Id -Timeout $TimeoutSeconds", body)
        self.assertIn("$timedOut = ($null -eq $completed)", body)
        self.assertIn("Stop-Job -Id $job.Id -ErrorAction SilentlyContinue", body)
        self.assertIn("Receive-Job -Id $job.Id -ErrorAction SilentlyContinue", body)
        self.assertIn("Remove-Job -Id $job.Id -Force -ErrorAction SilentlyContinue", body)
        self.assertLess(body.index("Stop-Job -Id $job.Id"), body.index("Receive-Job -Id $job.Id"))
        self.assertLess(body.index("Receive-Job -Id $job.Id"), body.index("Remove-Job -Id $job.Id"))
        self.assertIn('timed_out = $timedOut', body)
        self.assertIn('exit_code = $exitCode', body)
        self.assertIn('timeout_seconds = [int]$TimeoutSeconds', body)
        self.assertIn('timeout_evidence = $timeoutEvidence', body)
        self.assertIn('Child PowerShell job timed out after {0} seconds.', body)
        self.assertIn('$output = (($output, $timeoutEvidence)', body)
        self.assertLess(body.index('$output = (($output, $timeoutEvidence)'), body.index('$output | Set-Content'))

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
                "scripts\\run_android_ui_validation_pack.ps1",
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
