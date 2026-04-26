import subprocess
import unittest
from pathlib import Path

import yaml


REPO_ROOT = Path(__file__).resolve().parents[1]
DEPENDABOT_PATH = REPO_ROOT / ".github" / "dependabot.yml"
SCAN_SCRIPT_PATH = REPO_ROOT / "scripts" / "run_dependency_security_scan.ps1"
LOCK_SCRIPT_PATH = REPO_ROOT / "scripts" / "compile_python_lock.ps1"


class DependabotConfigTests(unittest.TestCase):
    def test_dependabot_tracks_actions_and_root_python_metadata(self):
        config = yaml.safe_load(DEPENDABOT_PATH.read_text(encoding="utf-8"))
        self.assertEqual(2, config["version"])

        updates = {
            (entry["package-ecosystem"], entry["directory"]): entry
            for entry in config["updates"]
        }
        self.assertIn(("github-actions", "/"), updates)
        self.assertIn(("pip", "/"), updates)

        for entry in updates.values():
            with self.subTest(entry=entry["package-ecosystem"]):
                self.assertEqual("weekly", entry["schedule"]["interval"])
                self.assertEqual("America/Chicago", entry["schedule"]["timezone"])
                self.assertLessEqual(entry["open-pull-requests-limit"], 5)

    def test_dependabot_does_not_reopen_android_gradle_lane(self):
        config = yaml.safe_load(DEPENDABOT_PATH.read_text(encoding="utf-8"))
        ecosystems = {entry["package-ecosystem"] for entry in config["updates"]}
        self.assertNotIn("gradle", ecosystems)


class DependencySecurityScanScriptTests(unittest.TestCase):
    def test_scan_script_prefers_pip_audit_and_keeps_installs_opt_in(self):
        content = SCAN_SCRIPT_PATH.read_text(encoding="utf-8")

        self.assertIn("Get-Command uvx", content)
        self.assertIn('"pip-audit"', content)
        self.assertIn('"pip_audit"', content)
        self.assertIn("$AllowInstall", content)
        self.assertIn("pip install pip-audit", content)
        self.assertIn("pip-audit is unavailable", content)
        self.assertIn("SkipIfUnavailable", content)
        self.assertIn("$previousErrorActionPreference = $ErrorActionPreference", content)
        self.assertIn("*> $null", content)

    def test_scan_script_has_safe_static_defaults(self):
        content = SCAN_SCRIPT_PATH.read_text(encoding="utf-8")

        self.assertIn('$RequirementsPath = "requirements.txt"', content)
        self.assertIn('$OutputJson = "artifacts\\security\\pip_audit.json"', content)
        self.assertIn("[switch]$WhatIf", content)
        self.assertIn("--progress-spinner", content)
        self.assertNotIn("gradle", content.lower())
        self.assertNotIn("emulator", content.lower())

    def test_scan_script_parses_as_powershell(self):
        command = [
            "powershell",
            "-NoProfile",
            "-Command",
            (
                "$tokens=$null; $errors=$null; "
                f"[System.Management.Automation.Language.Parser]::ParseFile('{SCAN_SCRIPT_PATH}', "
                "[ref]$tokens, [ref]$errors) > $null; "
                "if ($errors.Count -gt 0) { $errors | ForEach-Object { Write-Error $_.Message }; exit 1 }"
            ),
        ]
        result = subprocess.run(
            command,
            cwd=REPO_ROOT,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=False,
        )
        self.assertEqual(result.returncode, 0, result.stderr)


class PythonDependencyLockScriptTests(unittest.TestCase):
    def test_lock_script_uses_uv_compile_with_windows_python313_defaults(self):
        content = LOCK_SCRIPT_PATH.read_text(encoding="utf-8")

        self.assertIn("uv", content)
        self.assertIn('"pip"', content)
        self.assertIn('"compile"', content)
        self.assertIn('$RequirementsPath = "requirements.txt"', content)
        self.assertIn('$OutputPath = "requirements.lock.txt"', content)
        self.assertIn('$PythonVersion = "3.13"', content)
        self.assertIn('$PythonPlatform = "x86_64-pc-windows-msvc"', content)
        self.assertIn("--generate-hashes", content)
        self.assertIn("--custom-compile-command", content)
        self.assertIn(".\\scripts\\compile_python_lock.ps1", content)
        self.assertIn("[switch]$Check", content)
        self.assertIn("Get-NormalizedTextFileContent", content)
        self.assertIn("Lock check mode cannot be combined with -Upgrade.", content)
        self.assertIn("[switch]$WhatIf", content)

    def test_lock_script_parses_as_powershell(self):
        command = [
            "powershell",
            "-NoProfile",
            "-Command",
            (
                "$tokens=$null; $errors=$null; "
                f"[System.Management.Automation.Language.Parser]::ParseFile('{LOCK_SCRIPT_PATH}', "
                "[ref]$tokens, [ref]$errors) > $null; "
                "if ($errors.Count -gt 0) { $errors | ForEach-Object { Write-Error $_.Message }; exit 1 }"
            ),
        ]
        result = subprocess.run(
            command,
            cwd=REPO_ROOT,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=False,
        )
        self.assertEqual(result.returncode, 0, result.stderr)


if __name__ == "__main__":
    unittest.main()
