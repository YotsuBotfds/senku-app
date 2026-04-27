import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "android_fts5_probe.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidFts5ProbeContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_device_normalization_and_default_order_are_stable(self):
        self.assertIn("function Normalize-DeviceArguments", self.script)
        self.assertIn('[string]$serial -split ","', self.script)
        self.assertIn("function Order-Devices", self.script)
        for serial in ("emulator-5556", "emulator-5560", "emulator-5554", "emulator-5558"):
            self.assertIn(f'"{serial}"', self.script)

    def test_host_pack_probe_checks_fts5_and_fts4_tables(self):
        self.assertIn("function Get-HostPackProbe", self.script)
        self.assertIn('("lexical_chunks_fts", "lexical_chunks_fts4")', self.script)
        self.assertIn('summary["sqlite_version"]', self.script)
        self.assertIn('summary["row_counts"]', self.script)
        self.assertIn('for table_name in ("lexical_chunks_fts", "lexical_chunks_fts4")', self.script)

    def test_device_probe_checks_memory_and_installed_pack_fts_paths(self):
        self.assertIn('CREATE VIRTUAL TABLE fts5_probe USING fts5(body);', self.script)
        self.assertIn('CREATE VIRTUAL TABLE fts4_probe USING fts4(body);', self.script)
        self.assertIn('.schema lexical_chunks_fts"', self.script)
        self.assertIn('.schema lexical_chunks_fts4"', self.script)
        self.assertIn("SELECT count(*) FROM lexical_chunks_fts WHERE lexical_chunks_fts MATCH 'water';", self.script)
        self.assertIn("SELECT count(*) FROM lexical_chunks_fts4 WHERE lexical_chunks_fts4 MATCH 'water';", self.script)

    def test_app_runtime_and_summary_conclusion_are_stable(self):
        self.assertIn("function Get-AppRuntimeProbe", self.script)
        self.assertIn('"logcat", "-d", "SenkuPackRepo:D", "SenkuMobile:D", "*:S"', self.script)
        self.assertIn('$runtimeLogState = "fts5"', self.script)
        self.assertIn('$runtimeLogState = "fts4"', self.script)
        self.assertIn('$runtimeLogState = "unavailable"', self.script)
        self.assertIn("hostPackContainsFts5Schema =", self.script)
        self.assertIn("hostPackContainsFts4Schema =", self.script)
        self.assertIn("anyDeviceReportsFts5 =", self.script)
        self.assertIn("authoritativeForTestedRuntime =", self.script)
        self.assertIn('Save-Utf8Text -Path (Join-Path $outputDirectory "summary.json")', self.script)

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
                "scripts\\android_fts5_probe.ps1",
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
