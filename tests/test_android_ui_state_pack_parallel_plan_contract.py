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

    def test_plan_only_branch_precedes_launch_and_finalize_work(self):
        script = SCRIPT_PATH.read_text(encoding="utf-8-sig")

        plan_only_index = script.index("if ($PlanOnly) {")
        build_index = script.index("if (-not $SkipBuild) {")
        logs_index = script.index("New-Item -ItemType Directory -Force -Path $logsDir")
        launcher_index = script.index("function Start-RoleProcess")
        finalize_index = script.index("-FinalizeOnly -SkipBuild")

        self.assertLess(plan_only_index, build_index)
        self.assertLess(plan_only_index, logs_index)
        self.assertLess(plan_only_index, launcher_index)
        self.assertLess(plan_only_index, finalize_index)

    def test_followup_state_name_matches_seeded_thread_method(self):
        build_script = BUILD_SCRIPT_PATH.read_text(encoding="utf-8-sig")

        self.assertIn(
            '(New-StateDefinition -Method "scriptedSeededFollowUpThreadShowsInlineHistory")',
            build_script,
        )
        self.assertNotIn(
            '-Name "autoFollowUpWithHostInferenceBuildsInlineThreadHistory" '
            '-Method "scriptedSeededFollowUpThreadShowsInlineHistory"',
            build_script,
        )


if __name__ == "__main__":
    unittest.main()
