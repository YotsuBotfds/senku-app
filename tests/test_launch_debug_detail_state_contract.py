import re
import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "launch_debug_detail_state.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class LaunchDebugDetailStateContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_adb_launch_wrapper_is_bounded_by_common_capture_helper(self):
        self.assertIn("[int]$AdbCommandTimeoutMilliseconds = 60000", self.script)
        self.assertIn('throw "-AdbCommandTimeoutMilliseconds must be a positive integer."', self.script)
        self.assertIn('$androidHarnessCommonPath = Join-Path $PSScriptRoot "android_harness_common.psm1"', self.script)
        self.assertIn("Import-Module $androidHarnessCommonPath -Force", self.script)
        self.assertIn(
            "Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $fullArgs -TimeoutMilliseconds $TimeoutMilliseconds",
            self.script,
        )
        self.assertIn('throw ("adb timed out after {0}ms ({1}): {2}" -f $TimeoutMilliseconds, $joinedArgs, $outputText).TrimEnd()', self.script)
        self.assertIn("$script:LastAdbExitCode = [int]$result.exit_code", self.script)

        invoke_adb = re.search(
            r"function Invoke-AdbChecked \{(?P<body>.*?)\r?\n\}\r?\n\r?\nfunction Test-DetailForeground",
            self.script,
            re.DOTALL,
        )
        self.assertIsNotNone(invoke_adb)
        body = invoke_adb.group("body")
        self.assertNotIn("Start-Process", body)
        self.assertNotIn("-Wait", body)

    def test_no_unbounded_adb_start_process_wait_remains(self):
        self.assertNotRegex(
            self.script,
            re.compile(r"Start-Process\s+-FilePath\s+\$adb(?:(?!\r?\n\r?\n).)*-Wait", re.DOTALL),
        )
        self.assertNotIn("Start-Process -FilePath $adb", self.script)

    def test_debug_helper_paths_still_launch_dump_and_relaunch(self):
        self.assertIn('Invoke-AdbChecked -Arguments $activityArgs | Out-Null', self.script)
        self.assertIn('Invoke-AdbChecked -Arguments @("shell", "am", "force-stop", "com.senku.mobile") | Out-Null', self.script)
        self.assertIn('Invoke-AdbChecked -Arguments @("shell", "dumpsys", "activity", "activities")', self.script)
        self.assertIn('Invoke-AdbChecked -Arguments @("exec-out", "uiautomator", "dump", "/dev/tty")', self.script)
        self.assertIn('Write-Host "Launched MainActivity debug detail intent on emulator \'$Emulator\'."', self.script)

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
                "scripts\\launch_debug_detail_state.ps1",
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
