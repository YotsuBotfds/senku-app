import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_followup_suite.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidFollowupSuiteContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_parameters_and_default_emulators_are_stable(self):
        self.assertIn("[Parameter(Mandatory = $true)]", self.script)
        self.assertIn("[string]$SuitePath", self.script)
        self.assertIn('[string]$OutputDir = "artifacts\\\\bench\\\\android_followup_suite"', self.script)
        self.assertIn('[ValidateSet("preserve", "local", "host")]', self.script)
        self.assertIn('[string]$InferenceMode = "host"', self.script)
        for emulator in ("emulator-5554", "emulator-5556", "emulator-5558", "emulator-5560"):
            self.assertIn(f'"{emulator}"', self.script)

    def test_suite_parsing_supports_json_arrays_and_jsonl(self):
        self.assertIn("function Resolve-TargetPath", self.script)
        self.assertIn("function New-Slug", self.script)
        self.assertIn("function Load-SuiteCases", self.script)
        self.assertIn('$trimmed.StartsWith("[")', self.script)
        self.assertIn("$items = $trimmed | ConvertFrom-Json", self.script)
        self.assertIn('$safeLine.StartsWith("#")', self.script)
        self.assertIn("$cases += ($safeLine | ConvertFrom-Json)", self.script)
        self.assertIn('throw "No follow-up cases found in $resolvedSuitePath"', self.script)
        self.assertIn('throw "Each case must provide initial_query and follow_up_query."', self.script)

    def test_runner_invocation_and_manifest_fields_are_stable(self):
        self.assertIn('$detailRunner = Join-Path $PSScriptRoot "run_android_detail_followup.ps1"', self.script)
        self.assertIn("& powershell -ExecutionPolicy Bypass -File $detailRunner", self.script)
        for arg in (
            "-InferenceMode $InferenceMode",
            "-HostInferenceUrl $HostInferenceUrl",
            "-HostInferenceModel $HostInferenceModel",
            "-InitialMaxWaitSeconds $InitialMaxWaitSeconds",
            "-FollowUpMaxWaitSeconds $FollowUpMaxWaitSeconds",
            "-PollSeconds $PollSeconds",
        ):
            self.assertIn(arg, self.script)
        self.assertIn('"followup_suite_" + $timestamp + ".jsonl"', self.script)
        for field in (
            "label = $runLabel",
            "emulator = $emulator",
            "initial_query = $initialQuery",
            "follow_up_query = $followUpQuery",
            "status = $status",
            'started_at = $startedAt.ToString("o")',
            'finished_at = $finishedAt.ToString("o")',
            "duration_seconds =",
            "output_json =",
            "error = $errorMessage",
        ):
            self.assertIn(field, self.script)

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
                "scripts\\run_android_followup_suite.ps1",
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
