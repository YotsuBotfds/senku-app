import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "restart_local_server.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RestartLocalServerContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_safe_restart_contract_is_present(self):
        self.assertIn('[ValidateSet("litert", "fastembed", "live-queue-monitor", "sidecar-viewer")]', self.script)
        self.assertIn("Get-NetTCPConnection -LocalPort $Port -State Listen", self.script)
        self.assertIn("Get-CimInstance Win32_Process", self.script)
        self.assertIn("Test-ExpectedCommandLine", self.script)
        self.assertIn("$normalizedRoot = $repoRoot.Replace", self.script)
        self.assertIn("$normalizedRelativeScript = $normalizedScript.Substring", self.script)
        self.assertIn("Refusing to stop PID", self.script)
        self.assertIn("Start-Process -FilePath $spec.start_file", self.script)
        self.assertIn("-WindowStyle Hidden", self.script)
        self.assertIn("artifacts\\local_servers", self.script)
        self.assertIn("litert_native_openai_server.py", self.script)
        self.assertIn("fastembed_openai_server.py", self.script)
        self.assertIn("live_queue_monitor.py", self.script)
        self.assertIn("tools\\sidecar-viewer\\server.py", self.script)
        self.assertIn("$Service + \".pid.json\"", self.script)
        self.assertNotIn("Stop-Process -Name", self.script)

    def test_status_only_distinguishes_pidfile_wrapper_from_listener_owner(self):
        self.assertIn("Get-PidFileRecord", self.script)
        self.assertIn("pidfile_pid", self.script)
        self.assertIn("listener_pids", self.script)
        self.assertIn("launcher/wrapper process recorded at start", self.script)
        self.assertIn("listener_pids own the port", self.script)

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
                "scripts\\restart_local_server.ps1",
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
