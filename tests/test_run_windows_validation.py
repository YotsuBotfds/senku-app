import unittest
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
        self.assertIn("& $androidMigrationScript -PythonPath $pythonPath", self.script)
        self.assertIn("android migration validation failed", self.script)


if __name__ == "__main__":
    unittest.main()
