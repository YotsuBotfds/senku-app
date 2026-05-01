import subprocess
import re
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "stop_android_harness_runs.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class StopAndroidHarnessRunsContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_parameters_scope_and_emulator_filtering_are_stable(self):
        self.assertIn("[string[]]$Emulators = @()", self.script)
        self.assertIn("[switch]$IncludePromptRunner", self.script)
        self.assertIn('$ErrorActionPreference = "Stop"', self.script)
        self.assertIn('Join-Path $env:LOCALAPPDATA "Android\\\\Sdk\\\\platform-tools\\\\adb.exe"', self.script)
        self.assertIn("function Matches-TargetEmulator", self.script)
        self.assertIn("if (-not $Targets -or $Targets.Count -eq 0)", self.script)
        self.assertIn("[string]::IsNullOrWhiteSpace($target)", self.script)
        self.assertIn("$escapedTarget = [regex]::Escape($target.Trim())", self.script)
        self.assertIn("(?<![A-Za-z0-9_-])", self.script)
        self.assertIn("(?![A-Za-z0-9_-])", self.script)
        self.assertIn("$CommandLine -match $targetPattern", self.script)
        self.assertNotIn('$CommandLine -like ("*" + $target + "*")', self.script)

    def test_target_emulator_match_bounds_exact_device_references(self):
        target = "emulator-5554"
        escaped = re.escape(target)
        pattern = re.compile(rf"(?<![A-Za-z0-9_-]){escaped}(?![A-Za-z0-9_-])")

        self.assertRegex("powershell -File run_android_prompt.ps1 -Emulator emulator-5554 -Query x", pattern)
        self.assertRegex("powershell -File run_android_prompt.ps1 -Emulator:emulator-5554", pattern)
        self.assertRegex('powershell -File run_android_prompt.ps1 -Emulator "emulator-5554"', pattern)
        self.assertRegex("powershell -File run_android_followup_matrix.ps1 -Emulators emulator-5554,emulator-5556", pattern)
        self.assertNotRegex("powershell -File run_android_prompt.ps1 -Emulator emulator-55540 -Query x", pattern)
        self.assertNotRegex("powershell -File run_android_prompt.ps1 -Emulator emulator-5554-extra", pattern)

    def test_device_resolution_and_package_reset_are_stable(self):
        self.assertIn("function Test-ScopedAndroidDeviceId", self.script)
        self.assertIn("'^(emulator-\\d+|[A-Za-z0-9][A-Za-z0-9._:-]{2,127})$'", self.script)
        self.assertIn("function Get-TargetDevices", self.script)
        self.assertIn("$sawRequestedDevice = $false", self.script)
        self.assertIn("$sawRequestedDevice = $true", self.script)
        self.assertIn("if ($normalized.Count -gt 0 -or $sawRequestedDevice)", self.script)
        self.assertIn('$entry -split ","', self.script)
        self.assertIn("Test-ScopedAndroidDeviceId -DeviceId $trimmed", self.script)
        self.assertIn('$line -match \'^\\s*([^\\s]+)\\s+device\\s*$\'', self.script)
        self.assertIn("Test-ScopedAndroidDeviceId -DeviceId $deviceId", self.script)
        self.assertIn("function Stop-AndroidPackages", self.script)
        self.assertIn("Skipping unscoped Android device id:", self.script)
        self.assertIn("shell am force-stop com.senku.mobile.test", self.script)
        self.assertIn("shell am force-stop com.senku.mobile", self.script)
        self.assertIn("shell ps -A -o PID,NAME,ARGS", self.script)
        self.assertIn("function Get-SenkuPackagePidFromPsLine", self.script)
        self.assertIn('$senkuPackageNames = @(', self.script)
        self.assertIn('"com.senku.mobile.test"', self.script)
        self.assertIn('"com.senku.mobile"', self.script)
        self.assertIn("$senkuPackageNames -contains $field", self.script)
        self.assertIn("$targetPid -notmatch '^\\d+$'", self.script)
        self.assertIn("shell kill -9 $targetPid", self.script)

    def test_harness_command_patterns_and_launcher_fallback_are_stable(self):
        self.assertIn("function Assert-ScopedHarnessScriptPatterns", self.script)
        self.assertIn("'^[A-Za-z0-9_.-]+\\.ps1$'", self.script)
        self.assertIn("Android harness stop pattern must be a literal .ps1 basename", self.script)
        self.assertIn("function Matches-HarnessCommand", self.script)
        self.assertIn("[string]::IsNullOrWhiteSpace($CommandLine)", self.script)
        self.assertIn("Assert-ScopedHarnessScriptPatterns -ScriptPatterns $ScriptPatterns", self.script)
        self.assertIn("$escapedPattern = [regex]::Escape($pattern)", self.script)
        self.assertIn(r'''$scriptPattern = "(?i)(^|[\s`"'])" + $escapedPattern + "($|[\s`"':])"''', self.script)
        self.assertIn(r'''$pathPattern = "(?i)[\\/]" + $escapedPattern + "($|[\s`"':])"''', self.script)
        self.assertNotIn('$ScriptPatterns | Where-Object { $CommandLine -like ("*" + $_ + "*") }', self.script)
        for pattern in (
            '"build_android_ui_state_pack.ps1"',
            '"build_android_ui_state_pack_parallel.ps1"',
            '"run_android_detail_followup.ps1"',
            '"run_android_detail_followup_logged.ps1"',
            '"run_android_followup_matrix.ps1"',
            '"run_android_fts_fallback_matrix.ps1"',
            '"run_android_functional_ux_smoke_matrix.ps1"',
            '"run_android_harness_matrix.ps1"',
            '"run_android_instrumented_ui_smoke.ps1"',
            '"run_android_ui_validation_pack.ps1"',
        ):
            self.assertIn(pattern, self.script)
        self.assertIn("if ($IncludePromptRunner)", self.script)
        self.assertIn('$patterns += "run_android_prompt.ps1"', self.script)
        self.assertIn('$normalizedCommand -like ("*" + $normalizedRoot + "*")', self.script)
        self.assertIn('$normalizedCommand -like "*\\ui_state_pack*\\*"', self.script)
        self.assertIn('$normalizedCommand -like "*.launcher.ps1*"', self.script)

    def test_no_host_process_fallback_still_resets_devices(self):
        self.assertIn("if (-not $targets) {", self.script)
        self.assertIn("Stop-AndroidPackages -Devices (Get-TargetDevices -RequestedDevices $Emulators)", self.script)
        self.assertIn("No matching Android harness processes found. Force-stopped Senku packages on:", self.script)

    def test_host_stop_behavior_and_failure_exit_are_stable(self):
        self.assertIn("Get-CimInstance Win32_Process", self.script)
        self.assertIn('$_.Name -eq "powershell.exe"', self.script)
        self.assertIn("$_.ProcessId -ne $PID", self.script)
        self.assertIn("Stop-Process -Id $target.ProcessId -Force -ErrorAction Stop", self.script)
        self.assertIn('Write-Warning "No Android harness processes were stopped."', self.script)
        self.assertIn("exit 1", self.script)

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
                "scripts\\stop_android_harness_runs.ps1",
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
