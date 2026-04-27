import re
import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "unblock_android_adb.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class UnblockAndroidAdbContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_defaults_and_sdkmanager_discovery_are_stable(self):
        self.assertIn('[string]$SdkRoot = "$env:LOCALAPPDATA\\Android\\Sdk"', self.script)
        self.assertIn("[switch]$SkipInstall", self.script)
        self.assertIn('$ErrorActionPreference = "Stop"', self.script)
        latest = 'cmdline-tools\\latest\\bin\\sdkmanager.bat'
        legacy = 'tools\\bin\\sdkmanager.bat'
        install = '& $sdkManager --install "platform-tools"'
        self.assertIn(latest, self.script)
        self.assertIn(legacy, self.script)
        self.assertLess(self.script.index(latest), self.script.index(install))
        self.assertLess(self.script.index(legacy), self.script.index(install))

    def test_install_branch_and_adb_paths_are_stable(self):
        self.assertRegex(
            self.script,
            re.compile(
                r"if\s*\(-not\s+\$SkipInstall\)\s*\{.*?"
                r"Could not find sdkmanager\.bat\..*?"
                r"&\s+\$sdkManager\s+--install\s+\"platform-tools\"",
                re.DOTALL,
            ),
        )
        self.assertIn('throw "sdkmanager failed with exit code $LASTEXITCODE"', self.script)
        self.assertIn('$platformTools = Join-Path $SdkRoot "platform-tools"', self.script)
        self.assertIn('$adb = Join-Path $platformTools "adb.exe"', self.script)
        self.assertIn('throw "adb.exe still not found at $adb"', self.script)

    def test_environment_and_path_update_contract_is_stable(self):
        self.assertIn("function Add-PathSegment", self.script)
        self.assertIn('[Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", $SdkRoot, "User")', self.script)
        self.assertIn("$env:ANDROID_SDK_ROOT = $SdkRoot", self.script)
        self.assertIn("Add-PathSegment -Segment $platformTools", self.script)
        self.assertIn('[Environment]::SetEnvironmentVariable("Path", $newUserPath, "User")', self.script)
        self.assertIn("[System.StringComparison]::OrdinalIgnoreCase", self.script)
        self.assertIn("TrimEnd('\\')", self.script)

    def test_final_adb_proof_commands_and_guidance_are_stable(self):
        self.assertIn("& $adb version", self.script)
        self.assertIn("& $adb devices", self.script)
        self.assertIn("If devices are listed as unauthorized", self.script)
        self.assertIn("Open a new terminal after this", self.script)

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
                "scripts\\unblock_android_adb.ps1",
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
