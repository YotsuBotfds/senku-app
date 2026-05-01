import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_detail_followup_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidDetailFollowupMatrixContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_uses_logged_followup_wrapper(self):
        self.assertIn('$loggedScript = Join-Path $PSScriptRoot "run_android_detail_followup_logged.ps1"', self.script)
        self.assertIn('throw "run_android_detail_followup_logged.ps1 not found at $loggedScript"', self.script)

    def test_load_runs_supports_json_arrays_objects_and_jsonl(self):
        self.assertIn("function Load-Runs", self.script)
        self.assertIn('$trimmed.StartsWith("[") -or $trimmed.StartsWith("{")', self.script)
        self.assertIn("$parsed = $trimmed | ConvertFrom-Json", self.script)
        self.assertIn("foreach ($line in Get-Content $resolvedPath)", self.script)
        self.assertIn("$runs += ($trimmedLine | ConvertFrom-Json)", self.script)

    def test_required_run_values_and_argument_defaults_are_stable(self):
        self.assertIn("function Require-RunValue", self.script)
        self.assertIn("Run is missing required property '$PropertyName'", self.script)
        self.assertIn('Emulator = if ([string]::IsNullOrWhiteSpace([string]$Run.emulator)) { "emulator-5554" }', self.script)
        self.assertIn('InitialQuery = Require-RunValue -Run $Run -PropertyName "initial_query"', self.script)
        self.assertIn('FollowUpQuery = Require-RunValue -Run $Run -PropertyName "follow_up_query"', self.script)
        for field in (
            "InferenceMode = $InferenceMode",
            "HostInferenceUrl = $HostInferenceUrl",
            "HostInferenceModel = $HostInferenceModel",
            "InitialMaxWaitSeconds = $InitialMaxWaitSeconds",
            "FollowUpMaxWaitSeconds = $FollowUpMaxWaitSeconds",
            "PollSeconds = $PollSeconds",
        ):
            self.assertIn(field, self.script)
        self.assertIn("$args.RunLabel = [string]$Run.run_label", self.script)
        self.assertIn("$args.SkipSourceProbe = $true", self.script)

    def test_strict_followup_proof_can_be_enabled_globally_or_per_run(self):
        self.assertIn("[switch]$DefaultRequireStrictFollowUpProof", self.script)
        self.assertIn(
            '$DefaultRequireStrictFollowUpProof -or ($Run.PSObject.Properties.Name -contains "require_strict_followup_proof" -and [bool]$Run.require_strict_followup_proof)',
            self.script,
        )
        self.assertIn("$args.RequireStrictFollowUpProof = $true", self.script)

    def test_wait_parameters_are_validated_before_child_prompt_runs(self):
        self.assertIn("function Assert-PositiveWaitParameter", self.script)
        self.assertIn("function Assert-WaitParameters", self.script)
        for field in (
            "InitialMaxWaitSeconds",
            "FollowUpMaxWaitSeconds",
            "JobTimeoutSeconds",
            "PollSeconds",
        ):
            self.assertIn(f'Assert-PositiveWaitParameter -Name "{field}" -Value ${field}', self.script)
        self.assertIn("must be greater than or equal to 1 before forwarding to child prompt runs.", self.script)
        self.assertLess(
            self.script.index("Assert-WaitParameters"),
            self.script.index("$runs = Load-Runs -Path $RunFile"),
        )
        self.assertLess(
            self.script.index("Assert-WaitParameters"),
            self.script.index("$runArgs = New-RunArgs -Run $run"),
        )

    def test_parallel_jobs_invoke_logged_wrapper_from_repo_root(self):
        self.assertIn("return Start-Job -ScriptBlock {", self.script)
        self.assertIn("param($WorkingDirectory, $ScriptPath, $Arguments)", self.script)
        self.assertIn("Set-Location $WorkingDirectory", self.script)
        self.assertIn("& $ScriptPath @Arguments", self.script)
        self.assertIn("} -ArgumentList $repoRoot, $loggedScript, $RunArgs", self.script)
        self.assertIn("$parallelLimit = [Math]::Max(1, $MaxParallel)", self.script)

    def test_parent_job_watchdog_stops_timed_out_jobs_with_diagnostics(self):
        self.assertIn("[int]$JobTimeoutSeconds = 900", self.script)
        self.assertIn("function Stop-TimedOutJobs", self.script)
        self.assertIn("$elapsedSeconds = [int](($now - $entry.startedAt).TotalSeconds)", self.script)
        self.assertIn("if ($elapsedSeconds -lt $JobTimeoutSeconds)", self.script)
        self.assertIn('("Timed out after {0}s; stopping job {1}." -f $elapsedSeconds, $job.Id)', self.script)
        self.assertIn('("timeout_seconds={0}" -f $JobTimeoutSeconds)', self.script)
        self.assertIn('("job_state_before_stop={0}" -f $job.State)', self.script)
        self.assertIn('("emulator={0}" -f $entry.runArgs.Emulator)', self.script)
        self.assertIn('("initial_query={0}" -f $entry.runArgs.InitialQuery)', self.script)
        self.assertIn('("follow_up_query={0}" -f $entry.runArgs.FollowUpQuery)', self.script)
        self.assertIn("Stop-Job -Job $job -ErrorAction SilentlyContinue", self.script)
        self.assertIn('status = "timed_out"', self.script)
        self.assertIn("Write-Warning ($diagnostics -join [Environment]::NewLine)", self.script)
        self.assertIn("startedAt = [DateTimeOffset]::UtcNow", self.script)

    def test_timeout_watchdog_runs_during_slot_wait_and_final_drain(self):
        slot_wait_index = self.script.index("while ($activeJobs.Count -ge $parallelLimit)")
        final_drain_index = self.script.index("while ($activeJobs.Count -gt 0)")
        timeout_call = "Stop-TimedOutJobs -ActiveJobs $activeJobs -Results $results"
        self.assertLess(slot_wait_index, self.script.index(timeout_call, slot_wait_index))
        self.assertLess(
            self.script.index(timeout_call, slot_wait_index),
            self.script.index("Receive-CompletedJobs -ActiveJobs $activeJobs -Results $results", slot_wait_index),
        )
        self.assertLess(final_drain_index, self.script.index(timeout_call, final_drain_index))
        self.assertLess(
            self.script.index(timeout_call, final_drain_index),
            self.script.index("Receive-CompletedJobs -ActiveJobs $activeJobs -Results $results", final_drain_index),
        )

    def test_summary_and_stop_on_error_contract_are_stable(self):
        self.assertIn('Write-Host "Matrix run summary:"', self.script)
        self.assertIn('$failedResults = @($results | Where-Object { $_.status -ne "completed" })', self.script)
        self.assertIn("if ($failedResults.Count -gt 0 -and $StopOnError) {", self.script)
        self.assertIn('throw ("One or more matrix runs failed: "', self.script)

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
                "scripts\\run_android_detail_followup_matrix.ps1",
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
