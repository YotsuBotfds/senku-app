import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_gap_pack.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidGapPackContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_parameters_expose_gap_pack_modes_filters_and_emulators(self):
        self.assertIn(
            '[string]$GapPackPath = "artifacts\\\\prompts\\\\adhoc\\\\android_gap_pack_20260412.jsonl"',
            self.script,
        )
        self.assertIn('[ValidateSet("single", "followup", "both")]', self.script)
        self.assertIn('[string]$Mode = "both"', self.script)
        self.assertIn('[ValidateSet("preserve", "local", "host")]', self.script)
        self.assertIn('[string]$InferenceMode = "host"', self.script)
        self.assertIn(
            '[string[]]$FallbackEmulators = @("emulator-5554", "emulator-5556", "emulator-5558", "emulator-5560")',
            self.script,
        )
        self.assertIn("[string[]]$CaseIds = @()", self.script)
        self.assertIn("[string[]]$Families = @()", self.script)
        self.assertIn("[string[]]$Priorities = @()", self.script)
        self.assertIn("[int]$MaxCases = 0", self.script)

    def test_gap_pack_loader_skips_blank_comments_and_reads_jsonl(self):
        self.assertIn("function Load-GapPackCases", self.script)
        self.assertIn("$safeLine = $line.Trim()", self.script)
        self.assertIn(
            'if ([string]::IsNullOrWhiteSpace($safeLine) -or $safeLine.StartsWith("#"))',
            self.script,
        )
        self.assertIn("continue", self.script)
        self.assertIn("$cases += ($safeLine | ConvertFrom-Json)", self.script)

    def test_emulator_selection_prefers_suggestion_then_round_robin_fallback(self):
        self.assertIn("function Select-Emulator", self.script)
        self.assertIn("$suggested = [string]$Case.suggested_emulator", self.script)
        self.assertIn("return $suggested", self.script)
        self.assertIn("$usableFallbackEmulators = @()", self.script)
        self.assertIn(
            "if (-not [string]::IsNullOrWhiteSpace([string]$emulator))",
            self.script,
        )
        self.assertIn('throw "Provide at least one non-empty fallback emulator."', self.script)
        self.assertIn(
            "return $usableFallbackEmulators[$CaseIndex % $usableFallbackEmulators.Count]",
            self.script,
        )

    def test_parameter_guards_fail_before_runner_execution(self):
        self.assertIn("function Assert-RunParameterGuards", self.script)
        self.assertIn('throw "PollSeconds must be at least 1."', self.script)
        self.assertIn(
            'throw "SingleMaxWaitSeconds must be greater than or equal to PollSeconds."',
            self.script,
        )
        self.assertIn(
            'throw "InitialMaxWaitSeconds must be greater than or equal to PollSeconds."',
            self.script,
        )
        self.assertIn(
            'throw "FollowUpMaxWaitSeconds must be greater than or equal to PollSeconds."',
            self.script,
        )
        self.assertIn('throw "PauseSeconds must be at least 0."', self.script)
        self.assertIn('throw "MaxCases must be at least 0."', self.script)
        self.assertLess(
            self.script.index("Assert-RunParameterGuards"),
            self.script.index("& powershell -ExecutionPolicy Bypass -File $singleRunner"),
        )
        self.assertLess(
            self.script.index("Assert-RunParameterGuards"),
            self.script.index("& powershell -ExecutionPolicy Bypass -File $followUpRunner"),
        )

    def test_fallback_emulator_guard_runs_after_selection_before_runners(self):
        self.assertIn("function Assert-FallbackEmulatorsAvailable", self.script)
        self.assertIn("$needsFallback = $false", self.script)
        self.assertIn(
            "if ([string]::IsNullOrWhiteSpace([string]$case.suggested_emulator))",
            self.script,
        )
        self.assertIn("$hasFallback = $false", self.script)
        self.assertIn("Assert-FallbackEmulatorsAvailable -Cases $selectedCases", self.script)
        self.assertLess(
            self.script.index("Assert-FallbackEmulatorsAvailable -Cases $selectedCases"),
            self.script.index("& powershell -ExecutionPolicy Bypass -File $singleRunner"),
        )

    def test_runner_invocations_forward_host_inference_and_wait_controls(self):
        self.assertIn('$singleRunner = Join-Path $PSScriptRoot "run_android_prompt.ps1"', self.script)
        self.assertIn(
            '$followUpRunner = Join-Path $PSScriptRoot "run_android_detail_followup.ps1"',
            self.script,
        )
        self.assertIn("& powershell -ExecutionPolicy Bypass -File $singleRunner", self.script)
        self.assertIn("& powershell -ExecutionPolicy Bypass -File $followUpRunner", self.script)
        self.assertIn("-InferenceMode $InferenceMode", self.script)
        self.assertIn("-HostInferenceUrl $HostInferenceUrl", self.script)
        self.assertIn("-HostInferenceModel $HostInferenceModel", self.script)
        self.assertIn("-WaitForCompletion", self.script)
        self.assertIn("-MaxWaitSeconds $SingleMaxWaitSeconds", self.script)
        self.assertIn("-InitialMaxWaitSeconds $InitialMaxWaitSeconds", self.script)
        self.assertIn("-FollowUpMaxWaitSeconds $FollowUpMaxWaitSeconds", self.script)
        self.assertIn("-PollSeconds $PollSeconds", self.script)

    def test_manifest_jsonl_records_include_required_fields(self):
        self.assertIn("$singleRecord = [ordered]@{", self.script)
        self.assertIn('$followUpRecord = [ordered]@{', self.script)
        for field in (
            'mode = "single"',
            'mode = "followup"',
            "case_id = $caseId",
            "family = $family",
            "priority = $priority",
            "emulator = $emulator",
            "initial_query = $initialQuery",
            "error = $singleError",
            "error = $followUpError",
        ):
            self.assertIn(field, self.script)
        self.assertIn("ConvertTo-Json -Compress -Depth 6", self.script)
        self.assertIn("Add-Content -Path $manifestPath", self.script)

    def test_quality_gate_parser_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_gap_pack.ps1",
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
