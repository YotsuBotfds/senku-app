import json
import re
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "build_android_ui_state_pack_parallel.ps1"
BUILD_SCRIPT_PATH = REPO_ROOT / "scripts" / "build_android_ui_state_pack.ps1"


class AndroidUiStatePackParallelPlanContractTests(unittest.TestCase):
    def test_plan_only_with_role_filter_writes_focused_plan_without_launching_jobs(self):
        with tempfile.TemporaryDirectory(
            prefix="ui_state_pack_plan_",
            dir=REPO_ROOT / "artifacts",
        ) as temp_dir:
            output_root = Path(temp_dir).relative_to(REPO_ROOT)
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT_PATH),
                    "-OutputRoot",
                    str(output_root),
                    "-RoleFilter",
                    "phone_portrait,tablet_landscape",
                    "-PlanOnly",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            match = re.search(r"Plan:\s*(?P<path>.+plan\.json)", result.stdout)
            self.assertIsNotNone(match, result.stdout)

            plan_path = Path(match.group("path").strip())
            self.assertTrue(plan_path.is_file(), result.stdout)
            plan = json.loads(plan_path.read_text(encoding="utf-8-sig"))

            self.assertTrue(plan["plan_only"])
            self.assertTrue(plan["preflight_only"])
            self.assertFalse(plan["acceptance_evidence"])
            self.assertTrue(plan["non_acceptance_evidence"])
            self.assertFalse(plan["will_build"])
            self.assertFalse(plan["will_install"])
            self.assertFalse(plan["will_start_role_jobs"])
            self.assertFalse(plan["will_finalize"])
            self.assertEqual(plan["output_root"], str(output_root))
            self.assertEqual(
                plan["selected_roles"],
                ["phone_portrait", "tablet_landscape"],
            )
            self.assertEqual(
                [device["role"] for device in plan["devices"]],
                ["phone_portrait", "tablet_landscape"],
            )
            self.assertEqual(
                [device["device"] for device in plan["devices"]],
                ["emulator-5556", "emulator-5558"],
            )
            self.assertEqual(
                [launcher["role"] for launcher in plan["launchers"]],
                ["phone_portrait", "tablet_landscape"],
            )
            self.assertEqual(plan["max_parallel_devices"], 4)
            self.assertEqual(plan["effective_max_parallel_devices"], 4)
            self.assertEqual(
                plan["goal_pack"]["normalized_tablet_review_directory"],
                "normalized_tablet_review",
            )
            self.assertEqual(
                plan["goal_pack"]["normalized_tablet_review_source"],
                "canonical framed mocks",
            )
            self.assertTrue(plan["goal_pack"]["raw_state_pack_screenshots_unchanged"])
            intent = plan["migration_checklist_intent"]
            self.assertEqual(
                intent["selected_roles"],
                ["phone_portrait", "tablet_landscape"],
            )
            self.assertFalse(intent["acceptance_evidence"])
            self.assertTrue(intent["non_acceptance_evidence"])
            self.assertTrue(intent["preflight_only"])
            self.assertFalse(intent["host_flags"]["skip_host_states"])
            self.assertTrue(intent["host_flags"]["will_request_host_states"])
            self.assertEqual(
                intent["host_model_identity"]["host_inference_url"],
                "http://10.0.2.2:1235/v1",
            )
            self.assertEqual(
                intent["host_model_identity"]["host_inference_model"],
                "gemma-4-e2b-it-litert",
            )
            self.assertFalse(intent["skip_flags"]["skip_build"])
            self.assertFalse(intent["skip_flags"]["skip_install"])
            self.assertFalse(intent["skip_flags"]["skip_host_states"])
            self.assertEqual(intent["max_parallel_devices"], 4)
            self.assertEqual(intent["effective_max_parallel_devices"], 4)
            self.assertTrue(
                all("-SkipFinalize -SkipBuild" in launcher["command"] for launcher in plan["launchers"])
            )

            run_dir = plan_path.parent
            self.assertFalse((run_dir / "parallel_logs").exists())
            launched_artifacts = list((run_dir / "parallel_logs").glob("*.launcher.ps1"))
            launched_artifacts += list((run_dir / "parallel_logs").glob("*.out.log"))
            launched_artifacts += list((run_dir / "parallel_logs").glob("*.err.log"))
            launched_artifacts += list((run_dir / "parallel_logs").glob("*.exitcode.txt"))
            self.assertEqual(launched_artifacts, [])
            self.assertFalse((run_dir / "summary.json").exists())

    def test_plan_only_reports_effective_max_parallel_devices_after_launcher_clamp(self):
        with tempfile.TemporaryDirectory(
            prefix="ui_state_pack_plan_",
            dir=REPO_ROOT / "artifacts",
        ) as temp_dir:
            output_root = Path(temp_dir).relative_to(REPO_ROOT)
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT_PATH),
                    "-OutputRoot",
                    str(output_root),
                    "-RoleFilter",
                    "phone_portrait",
                    "-MaxParallelDevices",
                    "0",
                    "-PlanOnly",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            match = re.search(r"Plan:\s*(?P<path>.+plan\.json)", result.stdout)
            self.assertIsNotNone(match, result.stdout)

            plan_path = Path(match.group("path").strip())
            plan = json.loads(plan_path.read_text(encoding="utf-8-sig"))

            self.assertEqual(plan["max_parallel_devices"], 0)
            self.assertEqual(plan["effective_max_parallel_devices"], 1)
            self.assertEqual(plan["selected_roles"], ["phone_portrait"])
            self.assertEqual(plan["migration_checklist_intent"]["max_parallel_devices"], 0)
            self.assertEqual(plan["migration_checklist_intent"]["effective_max_parallel_devices"], 1)
            self.assertEqual(
                plan["migration_checklist_intent"]["selected_roles"],
                ["phone_portrait"],
            )

    def test_plan_only_branch_precedes_launch_and_finalize_work(self):
        script = SCRIPT_PATH.read_text(encoding="utf-8-sig")

        plan_only_index = script.index("if ($PlanOnly) {")
        build_index = script.index("if (-not $SkipBuild) {")
        logs_index = script.index("New-Item -ItemType Directory -Force -Path $logsDir")
        launcher_index = script.index("function Start-RoleProcess")
        finalize_index = script.index('"-FinalizeOnly"')
        finalize_skip_build_index = script.index('"-SkipBuild"', finalize_index)

        self.assertLess(plan_only_index, build_index)
        self.assertLess(plan_only_index, logs_index)
        self.assertLess(plan_only_index, launcher_index)
        self.assertLess(plan_only_index, finalize_index)
        self.assertLess(plan_only_index, finalize_skip_build_index)

    def test_followup_state_name_matches_seeded_thread_method(self):
        build_script = BUILD_SCRIPT_PATH.read_text(encoding="utf-8-sig")

        self.assertIn(
            '(New-StateDefinition -Method "scriptedSeededFollowUpThreadShowsInlineHistory" -GoalFamily "thread")',
            build_script,
        )

    def test_serial_builder_declares_normalized_tablet_review_without_raw_rewrite(self):
        build_script = BUILD_SCRIPT_PATH.read_text(encoding="utf-8-sig")

        self.assertIn("function Export-NormalizedReviewSet", build_script)
        self.assertIn("function Export-FilteredNormalizedReviewSet", build_script)
        self.assertIn("function Export-NormalizedTabletReviewSet", build_script)
        self.assertIn("function New-SkippedNormalizedReviewSet", build_script)
        self.assertIn("function New-SkippedNormalizedTabletReviewSet", build_script)
        self.assertIn('"normalized_review"', build_script)
        self.assertIn('"normalized_tablet_review"', build_script)
        self.assertIn('"filtered_normalized_review"', build_script)
        self.assertIn("normalized_review = $normalizedReview", build_script)
        self.assertIn("filtered_normalized_review = $filteredReview", build_script)
        self.assertIn("status = \"skipped\"", build_script)
        self.assertIn('status = "skipped_canonical_incomplete"', build_script)
        self.assertIn("canonical mock pack incomplete", build_script)
        self.assertIn("raw_state_pack_screenshots_unchanged = $true", build_script)
        self.assertIn(
            "copy all canonical mocks after deterministic frame export; no raw screenshot rewrite",
            build_script,
        )
        self.assertIn(
            "copy tablet canonical mocks after deterministic frame export; no raw screenshot rewrite",
            build_script,
        )
        self.assertIn(
            "full normalized review PNGs under `normalized_review/`",
            build_script,
        )
        self.assertIn(
            "tablet-only normalized review PNGs under `normalized_tablet_review/`",
            build_script,
        )
        self.assertIn(
            "filtered role packs may report `Status: fail` even when `pass_count == total_states`",
            build_script,
        )
        self.assertIn(
            "filtered role packs with available canonical-named mocks also write `filtered_normalized_review/`",
            build_script,
        )
        self.assertIn(
            "source screenshots by posture under `screenshots/` are raw diagnostic captures",
            build_script,
        )
        self.assertIn(
            "mock parity review uses `mocks/`, `goal_mock_pack.zip_path`, `normalized_review/`, or `normalized_tablet_review/` only",
            build_script,
        )
        self.assertIn(
            "`screenshots/` and `raw/` are diagnostics for state capture/debugging, not deterministic parity artifacts",
            build_script,
        )
        self.assertIn(
            "`filtered_normalized_review/` is for filtered-pack convenience review only",
            build_script,
        )
        self.assertIn("review_contract = [pscustomobject]@", build_script)
        self.assertIn('"goal_mock_pack.zip_path"', build_script)
        self.assertIn('"filtered_normalized_review"', build_script)
        self.assertIn(
            'raw_screenshot_policy = "diagnostics_only; may include live emulator OS chrome"',
            build_script,
        )
        self.assertIn(
            'parity_policy = "use deterministic framed mocks, mock ZIP, or normalized review artifacts for mock parity"',
            build_script,
        )
        self.assertIn(
            "filtered role packs can have pass_count equal total_states while summary status is fail",
            build_script,
        )
        self.assertNotIn(
            '-Name "autoFollowUpWithHostInferenceBuildsInlineThreadHistory" '
            '-Method "scriptedSeededFollowUpThreadShowsInlineHistory"',
            build_script,
        )


if __name__ == "__main__":
    unittest.main()
