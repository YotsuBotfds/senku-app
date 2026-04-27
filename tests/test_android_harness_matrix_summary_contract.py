import json
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_harness_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidHarnessMatrixSummaryContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_matrix_summary_contract_is_present(self):
        self.assertIn("function New-MatrixSummary", self.script)
        self.assertIn("function ConvertTo-MatrixSummaryMarkdown", self.script)
        self.assertIn('[string]$PushPackDir = ""', self.script)
        self.assertIn("[switch]$DefaultWarmStart", self.script)
        self.assertIn("[switch]$SkipPackPushIfCurrent", self.script)
        self.assertIn("[switch]$ForcePackPush", self.script)
        self.assertIn('Get-RunBoolOrDefault -Run $Run -PropertyName "warm_start"', self.script)
        self.assertIn('Get-RunStringOrDefault -Run $Run -PropertyName "push_pack_dir"', self.script)
        self.assertIn("$args.WarmStart = $true", self.script)
        self.assertIn("$args.PushPackDir = $runPushPackDir", self.script)
        self.assertIn("$args.SkipPackPushIfCurrent = $true", self.script)
        self.assertIn("$args.ForcePackPush = $true", self.script)
        self.assertIn("warm_start = [bool]$entry.runner.args.WarmStart", self.script)
        self.assertIn("push_pack_dir =", self.script)
        self.assertIn("function Get-HostAdbPlatformToolsVersion", self.script)
        self.assertIn("function Get-FirstNonEmptyHostAdbPlatformToolsVersion", self.script)
        self.assertIn("host_adb_platform_tools_version = $hostAdbPlatformToolsVersion", self.script)
        self.assertIn("host_adb_platform_tools_version = Get-FirstNonEmptyHostAdbPlatformToolsVersion -Items $Results", self.script)
        self.assertIn("host_adb_platform_tools_version:", self.script)
        self.assertIn("warm_start_count =", self.script)
        self.assertIn("function Resolve-MatrixDevicePosture", self.script)
        self.assertIn('"emulator-5556"', self.script)
        self.assertIn('posture = "phone_portrait"', self.script)
        self.assertIn('posture = "phone_landscape"', self.script)
        self.assertIn('posture = "tablet_portrait"', self.script)
        self.assertIn('posture = "tablet_landscape"', self.script)
        self.assertIn("device_role = $entry.runner.device_role", self.script)
        self.assertIn("orientation = $entry.runner.orientation", self.script)
        self.assertIn("posture = $entry.runner.posture", self.script)
        self.assertIn("posture=$($group.posture)", self.script)
        self.assertIn("$statusGroups = @()", self.script)
        self.assertIn("status_groups = $statusGroups", self.script)
        self.assertIn("function Convert-ToDateTimeOrNull", self.script)
        self.assertIn("function Get-RunDurationSeconds", self.script)
        self.assertIn("duration_seconds =", self.script)
        self.assertIn("$summaryJsonPath = Join-Path $resolvedOutputDir \"summary.json\"", self.script)
        self.assertIn("$summaryMarkdownPath = Join-Path $resolvedOutputDir \"summary.md\"", self.script)
        self.assertIn("$emulatorGroups = @()", self.script)
        self.assertIn("$failedItems = @(", self.script)
        self.assertIn("failed_items = $failedItems", self.script)
        self.assertIn("error = $_.error", self.script)
        self.assertIn("kind = \"jsonl\"", self.script)
        self.assertIn("kind = \"csv\"", self.script)
        self.assertIn("kind = \"manifest_json\"", self.script)
        self.assertIn("kind = \"logcat\"", self.script)
        self.assertIn("artifact_paths = $artifactPaths", self.script)
        self.assertIn("summary_jsonl_path = $SummaryJsonlPath", self.script)
        self.assertIn("summary_csv_path = $SummaryCsvPath", self.script)
        self.assertIn("duration_samples:", self.script)
        self.assertIn("duration_seconds_min:", self.script)
        self.assertIn("duration_seconds_max:", self.script)
        self.assertIn("duration_seconds_avg:", self.script)
        self.assertIn("duration_seconds_total:", self.script)
        self.assertIn("[switch]$PlanOnly", self.script)
        self.assertIn("function New-MatrixPlan", self.script)
        self.assertIn('plan_kind = "android_harness_matrix"', self.script)
        self.assertIn("will_touch_emulators = $false", self.script)
        self.assertIn("runner_commands =", self.script)
        self.assertIn("posture_groups = $postureGroups", self.script)
        self.assertIn('validation_commands', self.script)
        self.assertIn('validate_android_migration_summary.py', self.script)
        self.assertIn('PlanOnly validation only; does not start jobs or touch emulators.', self.script)
        self.assertIn('## Validation Commands', self.script)

    def test_plan_only_writes_matrix_plan_without_starting_jobs(self):
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        temp_path = Path(temp_dir.name)
        run_file = temp_path / "runs.jsonl"
        output_dir = temp_path / "matrix_plan"
        run_file.write_text(
            "\n".join(
                [
                    json.dumps(
                        {
                            "mode": "prompt",
                            "query": "How do I purify water?",
                            "run_label": "prompt_case",
                            "warm_start": True,
                            "push_pack_dir": "artifacts\\packs\\candidate",
                            "ask": True,
                            "wait_for_completion": True,
                        }
                    ),
                    json.dumps(
                        {
                            "mode": "detail_followup",
                            "initial_query": "sprained ankle",
                            "follow_up_query": "when should I seek care",
                            "run_label": "followup_case",
                            "emulator": "emulator-5558",
                        }
                    ),
                ]
            ),
            encoding="utf-8",
        )

        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(SCRIPT),
                "-RunFile",
                str(run_file),
                "-OutputDir",
                str(output_dir),
                "-Emulators",
                "emulator-5556",
                "-PlanOnly",
                "-SkipPackPushIfCurrent",
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Plan JSON written to", result.stdout)
        self.assertNotIn("Started [", result.stdout)

        summary_path = output_dir / "summary.json"
        self.assertTrue(summary_path.exists(), result.stdout)
        summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
        self.assertEqual(summary["plan_kind"], "android_harness_matrix")
        self.assertTrue(summary["preflight_only"])
        self.assertTrue(summary["plan_only"])
        self.assertTrue(summary["non_acceptance_evidence"])
        self.assertFalse(summary["acceptance_evidence"])
        self.assertFalse(summary["will_start_jobs"])
        self.assertFalse(summary["will_touch_emulators"])
        self.assertEqual(summary["row_count"], 2)
        self.assertEqual(summary["rows"][0]["emulator"], "emulator-5556")
        self.assertEqual(summary["rows"][0]["posture"], "phone_portrait")
        self.assertTrue(summary["rows"][0]["warm_start"])
        self.assertTrue(summary["rows"][0]["will_push_pack"])
        self.assertTrue(summary["rows"][0]["skip_pack_push_if_current"])
        self.assertEqual(summary["rows"][1]["emulator"], "emulator-5558")
        self.assertEqual(summary["rows"][1]["posture"], "tablet_landscape")
        self.assertIn("-RunLabel", summary["runner_commands"][0])
        self.assertIn("prompt_case", summary["runner_commands"][0])
        self.assertEqual(len(summary["validation_commands"]), 1)
        validation_command = summary["validation_commands"][0]
        self.assertEqual(validation_command["name"], "validate_summary_json")
        self.assertIn("validate_android_migration_summary.py", validation_command["command"])
        self.assertIn(str(summary_path), validation_command["command"])
        self.assertEqual(validation_command["validates"], "summary.json")
        self.assertEqual(Path(validation_command["summary_json_path"]), summary_path)
        self.assertTrue(validation_command["plan_only"])
        self.assertFalse(validation_command["will_start_jobs"])
        self.assertFalse(validation_command["will_touch_emulators"])
        self.assertIn("does not start jobs or touch emulators", validation_command["note"])

        summary_markdown_path = output_dir / "summary.md"
        self.assertTrue(summary_markdown_path.exists(), result.stdout)
        summary_markdown = summary_markdown_path.read_text(encoding="utf-8-sig")
        self.assertIn("## Validation Commands", summary_markdown)
        self.assertIn("validate_summary_json:", summary_markdown)
        self.assertIn("validate_android_migration_summary.py", summary_markdown)
        self.assertIn("validates: summary.json", summary_markdown)
        self.assertIn("will_start_jobs: False", summary_markdown)
        self.assertIn("will_touch_emulators: False", summary_markdown)

        validator = REPO_ROOT / "scripts" / "validate_android_migration_summary.py"
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"
        validation = subprocess.run(
            [str(python_path), str(validator), str(summary_path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(validation.returncode, 0, validation.stderr + validation.stdout)
        self.assertIn("android_migration_summary: ok", validation.stdout)

    def test_matrix_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_harness_matrix.ps1",
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
