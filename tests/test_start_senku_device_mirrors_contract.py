import os
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "start_senku_device_mirrors.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class StartSenkuDeviceMirrorsContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_default_devices_and_window_flags_are_stable(self):
        self.assertIn('[string]$PhoneDevice = "RFCX607ZM8L"', self.script)
        self.assertIn('[string]$TabletDevice = "R92X51AG48D"', self.script)
        self.assertIn("[switch]$ControlEnabled", self.script)
        self.assertIn("[switch]$AlwaysOnTop = $true", self.script)
        self.assertIn("[int]$PhoneMaxSize = 540", self.script)
        self.assertIn("[int]$TabletMaxSize = 0", self.script)

        self.assertIn("if (-not $ControlEnabled) {", self.script)
        self.assertIn('$argumentParts.Add("--no-control")', self.script)
        self.assertIn("if ($AlwaysOnTop) {", self.script)
        self.assertIn('$argumentParts.Add("--always-on-top")', self.script)
        self.assertIn('max_size = $PhoneMaxSize', self.script)
        self.assertIn('max_size = $TabletMaxSize', self.script)

    def test_scrcpy_resolution_preserves_running_winget_path_fallbacks(self):
        self.assertIn("function Resolve-ScrcpyPath", self.script)
        self.assertIn("Get-Process scrcpy -ErrorAction SilentlyContinue", self.script)
        self.assertIn("return $running", self.script)
        self.assertIn('Join-Path $env:LOCALAPPDATA "Microsoft\\\\WinGet\\\\Packages"', self.script)
        self.assertIn('Get-ChildItem -LiteralPath $wingetRoot -Directory -Filter "Genymobile.scrcpy*"', self.script)
        self.assertIn('Get-ChildItem -LiteralPath $_.FullName -Recurse -File -Filter "scrcpy.exe"', self.script)
        self.assertIn("return $candidate", self.script)
        self.assertIn("Get-Command scrcpy.exe -ErrorAction SilentlyContinue", self.script)
        self.assertIn("return $command.Source", self.script)
        self.assertIn('throw "Could not locate scrcpy.exe"', self.script)

    def test_cleanup_only_targets_scrcpy_for_requested_device(self):
        self.assertIn("function Stop-MirrorForDevice", self.script)
        self.assertIn("param([string]$Device)", self.script)
        self.assertIn("Get-CimInstance Win32_Process -ErrorAction SilentlyContinue", self.script)
        self.assertIn('$_.Name -eq "scrcpy.exe"', self.script)
        self.assertIn("$_.CommandLine -and", self.script)
        self.assertIn("$_.CommandLine -match [regex]::Escape($Device)", self.script)
        self.assertIn("Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue", self.script)
        self.assertIn("Stop-MirrorForDevice -Device $Device", self.script)
        self.assertNotIn("Stop-Process -Name scrcpy", self.script)

    def test_phone_only_tablet_only_conflict_fails_closed(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            fake_scrcpy = Path(temp_dir) / "scrcpy.exe"
            fake_scrcpy.write_bytes(b"")
            env = {
                **dict(os.environ),
                "PATH": f"{temp_dir};{os.environ.get('PATH', '')}",
            }

            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-PhoneOnly",
                    "-TabletOnly",
                ],
                cwd=REPO_ROOT,
                env=env,
                capture_output=True,
                text=True,
                check=False,
            )

        self.assertNotEqual(result.returncode, 0)
        self.assertIn(
            "Nothing to launch. Remove -PhoneOnly/-TabletOnly conflict.",
            result.stderr + result.stdout,
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
                "scripts\\start_senku_device_mirrors.ps1",
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
