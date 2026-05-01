import re
import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "probe_litert_model_transport.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class ProbeLiteRtModelTransportContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_records_host_adb_platform_tools_evidence(self):
        self.assertIn("function Get-HostAdbVersionEvidence", self.script)
        self.assertIn('$versionResult = Invoke-Adb -Arguments @("version")', self.script)
        self.assertIn('^Android Debug Bridge version\\b', self.script)
        self.assertIn('^Version\\s+(.+)$', self.script)
        self.assertIn("adb_version_line = $versionLine", self.script)
        self.assertIn("adb_platform_tools_version = $platformToolsVersion", self.script)
        self.assertIn('$lines.Add("adb_version_line=$($AdbVersionEvidence.adb_version_line)")', self.script)
        self.assertIn('$lines.Add("adb_platform_tools_version=$($AdbVersionEvidence.adb_platform_tools_version)")', self.script)
        self.assertIn('$lines.Add("adb_version:")', self.script)
        self.assertIn("$adbVersionEvidence = Get-HostAdbVersionEvidence", self.script)
        self.assertIn(
            "Write-EnvironmentLog -PrimarySerial $Device -SecondarySerial $ConfirmDevice -AdbVersionEvidence $adbVersionEvidence",
            self.script,
        )

    def test_results_json_has_summary_object_with_adb_evidence(self):
        self.assertIn("$resultsArtifact = [pscustomobject]@{", self.script)
        self.assertIn("summary = [pscustomobject]@{", self.script)
        self.assertIn("adb_path = $adbVersionEvidence.adb_path", self.script)
        self.assertIn("adb_version_line = $adbVersionEvidence.adb_version_line", self.script)
        self.assertIn("adb_platform_tools_version = $adbVersionEvidence.adb_platform_tools_version", self.script)
        self.assertIn("adb_version_exit_code = $adbVersionEvidence.adb_version_exit_code", self.script)
        self.assertIn("results = @($results)", self.script)
        self.assertIn("$resultsArtifact | ConvertTo-Json -Depth 7", self.script)
        self.assertNotIn("$results | ConvertTo-Json -Depth 6", self.script)

    def test_transfer_methods_are_not_changed_by_contract(self):
        self.assertIn('Invoke-TmpStagingMethod -Serial $Serial -Payload $Payload', self.script)
        self.assertIn('Invoke-CmdScriptMethod -Serial $Serial -Payload $Payload', self.script)
        self.assertIn('Invoke-ProcessStreamMethod -Serial $Serial -Payload $Payload', self.script)
        self.assertIn('cmd_redirect_cat', self.script)
        self.assertIn('cmd_type_pipe_cat', self.script)
        self.assertIn('process_stdin_copy_cat', self.script)

    def test_adb_capture_paths_are_bounded_and_kill_on_timeout(self):
        self.assertIn("[int]$AdbCommandTimeoutMilliseconds = 60000", self.script)
        self.assertIn("[int]$AdbPushTimeoutMilliseconds = 1800000", self.script)
        self.assertIn("[int]$AdbDeviceWaitTimeoutMilliseconds = 120000", self.script)
        self.assertIn('throw "-AdbCommandTimeoutMilliseconds must be a positive integer."', self.script)
        self.assertIn('throw "-AdbPushTimeoutMilliseconds must be a positive integer."', self.script)
        self.assertIn('throw "-AdbDeviceWaitTimeoutMilliseconds must be a positive integer."', self.script)

        native_capture = re.search(
            r"function Invoke-NativeCapture \{(?P<body>.*?)\r?\n\}\r?\n\r?\nfunction Resolve-LiteRtModelPath",
            self.script,
            re.DOTALL,
        )
        self.assertIsNotNone(native_capture)
        body = native_capture.group("body")
        self.assertNotIn("-Wait `", body)
        self.assertIn("$finished = $process.WaitForExit($TimeoutMilliseconds)", body)
        self.assertIn("$process.Kill()", body)
        self.assertIn("timed_out = (-not $finished)", body)
        self.assertIn("timeout_ms = [int]$TimeoutMilliseconds", body)

    def test_wait_for_device_and_transfer_invocations_pass_timeouts(self):
        self.assertIn(
            'Invoke-Adb -Arguments @("-s", $Serial, "wait-for-device") -TimeoutMilliseconds $AdbDeviceWaitTimeoutMilliseconds',
            self.script,
        )
        self.assertIn("wait-for-device timed out after ${AdbDeviceWaitTimeoutMilliseconds}ms", self.script)
        self.assertIn(
            'Invoke-Adb -Arguments @("-s", $Serial, "push", $Payload.path, $RemoteTmpPath) -TimeoutMilliseconds $AdbPushTimeoutMilliseconds',
            self.script,
        )
        self.assertIn(
            'Invoke-NativeCapture -FilePath "cmd.exe" -Arguments @("/d", "/c", $tempScript) -TimeoutMilliseconds $AdbPushTimeoutMilliseconds',
            self.script,
        )
        self.assertNotIn("& $adb devices", self.script)
        self.assertNotIn("& $adb -s $serial shell df", self.script)

    def test_direct_stream_waits_are_bounded_and_kill_on_timeout(self):
        self.assertIn("[int]$ProcessStreamTimeoutMilliseconds = 1800000", self.script)
        self.assertIn('throw "-ProcessStreamTimeoutMilliseconds must be a positive integer."', self.script)
        stream_method = re.search(
            r"function Invoke-ProcessStreamMethod \{(?P<body>.*?)\r?\n\}\r?\n\r?\nfunction Get-CombinedOutputText",
            self.script,
            re.DOTALL,
        )
        self.assertIsNotNone(stream_method)
        body = stream_method.group("body")
        self.assertNotIn(".WaitForExit()", body)
        self.assertIn("$copyTask.Wait($ProcessStreamTimeoutMilliseconds)", body)
        self.assertIn("$finished = $process.WaitForExit($remainingMilliseconds)", body)
        self.assertIn("[void]$process.WaitForExit([Math]::Min(30000, $ProcessStreamTimeoutMilliseconds))", body)
        self.assertIn("$process.Kill()", body)
        self.assertIn("timed_out = [bool]$timedOut", body)
        self.assertIn("timeout_ms = [int]$ProcessStreamTimeoutMilliseconds", body)

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
                "scripts\\probe_litert_model_transport.ps1",
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
