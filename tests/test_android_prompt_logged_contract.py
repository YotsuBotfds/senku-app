import re
import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_prompt_logged.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidPromptLoggedContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_conflicting_harness_match_bounds_emulator_argument(self):
        self.assertIn("$emulatorPattern = [regex]::Escape($TargetEmulator)", self.script)
        self.assertIn("-Emulator(?:\\s+|:)", self.script)
        self.assertIn("(?=\\s|$)", self.script)
        self.assertIn("$_.CommandLine -match $pattern", self.script)
        self.assertNotIn("*-Emulator $TargetEmulator*", self.script)
        self.assertNotIn("$_.CommandLine -like $pattern", self.script)

        target = "emulator-5554"
        pattern = (
            rf"(?i)(?:^|\s)-Emulator(?:\s+|:)"
            rf"(?:\"{re.escape(target)}\"|'{re.escape(target)}'|{re.escape(target)})(?=\s|$)"
        )
        self.assertRegex("powershell -File run_android_prompt_logged.ps1 -Emulator emulator-5554 -Query x", pattern)
        self.assertRegex("powershell -File run_android_prompt_logged.ps1 -Emulator:emulator-5554", pattern)
        self.assertRegex('powershell -File run_android_prompt_logged.ps1 -Emulator "emulator-5554"', pattern)
        self.assertNotRegex("powershell -File run_android_prompt_logged.ps1 -Emulator emulator-55540 -Query x", pattern)
        self.assertNotRegex("powershell -File run_android_prompt_logged.ps1 -Emulator emulator-5554-extra", pattern)

    def test_prompt_logged_parser_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_prompt_logged.ps1",
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
