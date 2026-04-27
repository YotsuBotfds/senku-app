import unittest
import subprocess
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_windows_validation.ps1"


class RunWindowsValidationContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_android_migration_mode_delegates_to_no_emulator_validator_suite(self):
        self.assertIn(
            "[ValidateSet('compile', 'unit', 'ingest', 'guide', 'android-migration')]",
            self.script,
        )
        self.assertIn("'android-migration' {", self.script)
        self.assertIn("run_android_migration_validator_suite.ps1", self.script)
        self.assertIn(
            "& $androidMigrationScript -PythonPath $pythonPath -WhatIf:$WhatIf",
            self.script,
        )
        self.assertIn("android migration validation failed", self.script)

    def test_android_migration_whatif_runs_no_emulator_validator_plan(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(SCRIPT),
                "-Mode",
                "android-migration",
                "-WhatIf",
            ],
            cwd=REPO_ROOT,
            text=True,
            capture_output=True,
            check=False,
        )

        output = result.stdout + result.stderr
        self.assertEqual(result.returncode, 0, output)
        self.assertIn("Android migration validator suite dry run.", output)
        self.assertIn("-B -m unittest", output)
        self.assertIn("tests.test_validate_android_migration_summary", output)
        self.assertIn("tests.test_validate_android_migration_preflight_bundle_summary", output)
        self.assertIn("tests.test_summarize_android_migration_proof", output)
        self.assertNotIn("adb ", output.lower())
        self.assertNotIn("start_senku_emulator_matrix", output)


if __name__ == "__main__":
    unittest.main()
