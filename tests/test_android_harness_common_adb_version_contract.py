import subprocess
import tempfile
import textwrap
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
MODULE_PATH = REPO_ROOT / "scripts" / "android_harness_common.psm1"


def run_version_probe(fake_adb_text: str) -> subprocess.CompletedProcess[str]:
    with tempfile.TemporaryDirectory() as temp_dir:
        fake_adb = Path(temp_dir) / "adb.cmd"
        fake_adb.write_text(
            "@echo off\r\n"
            "if not \"%1\"==\"version\" exit /b 64\r\n"
            + "".join(f"echo {line}\r\n" for line in fake_adb_text.splitlines())
            + "exit /b 0\r\n",
            encoding="utf-8",
        )
        probe = textwrap.dedent(
            f"""
            Import-Module {str(MODULE_PATH)!r} -Force -DisableNameChecking
            $version = Get-AndroidHostAdbPlatformToolsVersion -AdbPath {str(fake_adb)!r}
            if ($null -ne $version) {{
                Write-Output $version
            }}
            """
        )
        return subprocess.run(
            ["powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", probe],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )


class AndroidHarnessCommonAdbVersionContractTests(unittest.TestCase):
    def assert_probe_output(self, fake_adb_text: str, expected: str) -> None:
        result = run_version_probe(fake_adb_text)
        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertEqual(result.stdout.strip(), expected)

    def test_get_host_adb_platform_tools_version_prefers_version_line(self):
        self.assert_probe_output(
            "\n".join(
                [
                    "Android Debug Bridge version 1.0.41",
                    "Version 36.0.0-13206524",
                    "Installed as C:\\Android\\platform-tools\\adb.exe",
                ]
            ),
            "36.0.0-13206524",
        )

    def test_get_host_adb_platform_tools_version_falls_back_to_bridge_version(self):
        self.assert_probe_output(
            "\n".join(
                [
                    "Android Debug Bridge version 1.0.41",
                    "Installed as C:\\Android\\platform-tools\\adb.exe",
                ]
            ),
            "1.0.41",
        )

    def test_get_host_adb_platform_tools_version_falls_back_to_first_line(self):
        self.assert_probe_output(
            "\n".join(
                [
                    "custom adb build metadata",
                    "Installed as C:\\Android\\platform-tools\\adb.exe",
                ]
            ),
            "custom adb build metadata",
        )

    def test_get_host_adb_platform_tools_version_uses_common_adb_capture_helper(self):
        module = MODULE_PATH.read_text(encoding="utf-8")
        self.assertIn("function Get-AndroidHostAdbPlatformToolsVersion", module)
        self.assertIn('Invoke-AndroidAdbCommandCapture -AdbPath $AdbPath -Arguments @("version")', module)
        self.assertIn("Get-AndroidHostAdbPlatformToolsVersion", module.rsplit("Export-ModuleMember", 1)[1])


if __name__ == "__main__":
    unittest.main()
