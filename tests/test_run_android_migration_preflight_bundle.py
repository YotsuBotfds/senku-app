import json
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_migration_preflight_bundle.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidMigrationPreflightBundleTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_bundle_declares_no_emulator_preflight_contract(self):
        self.assertIn("[switch]$NoProbeTools", self.script)
        self.assertIn("metadata_only", self.script)
        self.assertIn("preflight_only", self.script)
        self.assertIn("non_acceptance_evidence", self.script)
        self.assertIn("acceptance_evidence", self.script)
        self.assertIn("emulator_required", self.script)
        self.assertIn("devices_touched", self.script)
        self.assertIn("tiny_litert_fixture.task", self.script)
        self.assertIn("managed_device_tasks.txt", self.script)
        self.assertIn("write_android_tooling_version_manifest.py", self.script)
        self.assertIn("run_android_managed_device_smoke.ps1", self.script)
        self.assertIn("run_android_litert_readiness_matrix.ps1", self.script)
        self.assertIn("run_android_orchestrator_smoke.ps1", self.script)
        self.assertIn("run_android_harness_matrix.ps1", self.script)
        self.assertIn("build_android_ui_state_pack_parallel.ps1", self.script)
        self.assertIn("summarize_android_migration_proof.py", self.script)
        self.assertIn("validate_android_tooling_version_manifest.py", self.script)
        self.assertIn("validate_android_managed_device_smoke_summary.py", self.script)
        self.assertIn("validate_android_litert_readiness_summary.py", self.script)
        self.assertIn("validate_android_orchestrator_smoke_summary.py", self.script)
        self.assertIn("validate_android_harness_matrix_plan.py", self.script)
        self.assertIn("validate_android_ui_state_pack_plan.py", self.script)

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
                "scripts\\run_android_migration_preflight_bundle.ps1",
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

    def test_bundle_runs_existing_dry_runs_plans_validators_and_summarizer(self):
        with tempfile.TemporaryDirectory(prefix="android_preflight_bundle_", dir=REPO_ROOT / "artifacts") as temp_dir:
            output_dir = Path(temp_dir) / "bundle"
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-OutputDir",
                    str(output_dir),
                    "-NoProbeTools",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("Android migration preflight bundle: pass", result.stdout)

            summary_path = output_dir / "summary.json"
            summary_md_path = output_dir / "summary.md"
            self.assertTrue(summary_path.exists())
            self.assertTrue(summary_md_path.exists())
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))
            summary_md = summary_md_path.read_text(encoding="utf-8-sig")

            self.assertEqual(summary["bundle_kind"], "android_migration_preflight_bundle")
            self.assertEqual(summary["status"], "pass")
            self.assertTrue(summary["metadata_only"])
            self.assertTrue(summary["preflight_only"])
            self.assertTrue(summary["non_acceptance_evidence"])
            self.assertFalse(summary["acceptance_evidence"])
            self.assertFalse(summary["emulator_required"])
            self.assertFalse(summary["devices_touched"])
            self.assertFalse(summary["adb_required"])

            expected_steps = {
                "tooling_version_manifest",
                "managed_device_smoke_dry_run",
                "litert_readiness_dry_run",
                "orchestrator_smoke_dry_run",
                "harness_matrix_plan_only",
                "ui_state_pack_plan_only",
                "migration_proof_summarizer_json",
                "migration_proof_summarizer_markdown",
            }
            self.assertEqual({step["name"] for step in summary["steps"]}, expected_steps)
            self.assertTrue(all(step["status"] == "pass" for step in summary["steps"]))

            expected_validators = {
                "validate_tooling_version_manifest",
                "validate_managed_device_smoke",
                "validate_litert_readiness",
                "validate_orchestrator_smoke",
                "validate_harness_matrix_plan",
                "validate_ui_state_pack_plan",
            }
            self.assertEqual({item["name"] for item in summary["validation_commands"]}, expected_validators)
            self.assertTrue(all(item["status"] == "pass" for item in summary["validation_commands"]))

            tiny_model = REPO_ROOT / summary["fixtures"]["tiny_model_path"]
            task_inventory = REPO_ROOT / summary["fixtures"]["task_inventory_path"]
            self.assertTrue(tiny_model.exists())
            self.assertEqual(tiny_model.read_bytes(), b"tiny litert placeholder\n")
            self.assertIn("senkuManagedSmokeGroupDebugAndroidTest", task_inventory.read_text(encoding="utf-8-sig"))

            child_summaries = {
                step["name"]: REPO_ROOT / step["summary_path"]
                for step in summary["steps"]
                if step.get("summary_path")
            }
            self.assertTrue(child_summaries["tooling_version_manifest"].exists())
            self.assertTrue(child_summaries["managed_device_smoke_dry_run"].exists())
            self.assertTrue(child_summaries["litert_readiness_dry_run"].exists())
            self.assertTrue(child_summaries["orchestrator_smoke_dry_run"].exists())
            self.assertTrue(child_summaries["harness_matrix_plan_only"].exists())
            self.assertTrue(child_summaries["ui_state_pack_plan_only"].exists())

            tooling = json.loads(child_summaries["tooling_version_manifest"].read_text(encoding="utf-8-sig"))
            managed = json.loads(child_summaries["managed_device_smoke_dry_run"].read_text(encoding="utf-8-sig"))
            litert = json.loads(child_summaries["litert_readiness_dry_run"].read_text(encoding="utf-8-sig"))
            orchestrator = json.loads(child_summaries["orchestrator_smoke_dry_run"].read_text(encoding="utf-8-sig"))
            harness = json.loads(child_summaries["harness_matrix_plan_only"].read_text(encoding="utf-8-sig"))
            ui_plan = json.loads(child_summaries["ui_state_pack_plan_only"].read_text(encoding="utf-8-sig"))

            self.assertTrue(tooling["metadata_only"])
            self.assertFalse(tooling["host_tools"]["probed"])
            self.assertTrue(managed["dry_run"])
            self.assertFalse(managed["would_launch_emulators"])
            self.assertEqual(managed["task_inventory_source"], "path")
            self.assertTrue(litert["dry_run"])
            self.assertTrue(litert["model"]["exists"])
            self.assertFalse(litert["backend"]["adb_required_in_dry_run"])
            self.assertTrue(orchestrator["dry_run"])
            self.assertFalse(orchestrator["would_start_emulators"])
            self.assertTrue(harness["plan_only"])
            self.assertFalse(harness["will_touch_emulators"])
            self.assertTrue(ui_plan["plan_only"])
            self.assertFalse(ui_plan["will_start_role_jobs"])

            rows_json = REPO_ROOT / summary["migration_summarizer"]["json_path"]
            rows_md = REPO_ROOT / summary["migration_summarizer"]["markdown_path"]
            self.assertTrue(rows_json.exists())
            self.assertTrue(rows_md.exists())
            rows = json.loads(rows_json.read_text(encoding="utf-8-sig"))
            self.assertEqual(len(rows), 4)
            self.assertTrue(all(row["evidence_posture"] == "non-acceptance" for row in rows))
            self.assertIn("| status | passes |", rows_md.read_text(encoding="utf-8-sig"))

            self.assertIn("- metadata_only: True", summary_md)
            self.assertIn("- preflight_only: True", summary_md)
            self.assertIn("- acceptance_evidence: False", summary_md)
            self.assertIn("validate_ui_state_pack_plan", summary_md)


if __name__ == "__main__":
    unittest.main()
