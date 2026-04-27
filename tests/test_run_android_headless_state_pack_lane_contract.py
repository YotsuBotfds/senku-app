import json
import re
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_headless_state_pack_lane.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidHeadlessStatePackLaneContractTests(unittest.TestCase):
    def run_lane(self, *args):
        return subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(SCRIPT),
                *args,
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

    def read_plan_from_output(self, output):
        match = re.search(r"Plan:\s*(?P<path>.+headless_lane_plan\.json)", output)
        self.assertIsNotNone(match, output)
        plan_path = Path(match.group("path").strip())
        self.assertTrue(plan_path.is_file(), output)
        return plan_path, json.loads(plan_path.read_text(encoding="utf-8-sig"))

    def test_plan_only_writes_non_acceptance_fixed_matrix_plan_without_emulators(self):
        with tempfile.TemporaryDirectory(
            prefix="headless_lane_plan_",
            dir=REPO_ROOT / "artifacts",
        ) as temp_dir:
            output_root = Path(temp_dir).relative_to(REPO_ROOT)
            result = self.run_lane(
                "-OutputRoot",
                str(output_root),
                "-PlanOnly",
                "-MaxParallelDevices",
                "0",
            )

            output = result.stdout + result.stderr
            self.assertEqual(result.returncode, 0, output)
            plan_path, plan = self.read_plan_from_output(output)

        self.assertTrue(plan["plan_only"])
        self.assertFalse(plan["whatif"])
        self.assertFalse(plan["real_run_requested"])
        self.assertFalse(plan["will_start_emulators"])
        self.assertFalse(plan["will_run_state_pack"])
        self.assertFalse(plan["acceptance_evidence"])
        self.assertTrue(plan["non_acceptance_evidence"])
        self.assertFalse(plan["acceptance_label_allowed"])
        self.assertFalse(plan["planning_artifacts_are_acceptance"])
        self.assertEqual(plan["fixed_matrix_roles"], [
            "phone_portrait",
            "phone_landscape",
            "tablet_portrait",
            "tablet_landscape",
        ])
        self.assertEqual(
            [entry["device"] for entry in plan["fixed_matrix"]],
            ["emulator-5556", "emulator-5560", "emulator-5554", "emulator-5558"],
        )
        self.assertEqual(plan["max_parallel_devices"], 0)
        self.assertEqual(plan["effective_max_parallel_devices"], 1)
        self.assertTrue(plan["headless"])
        self.assertEqual(plan["mode"], "read_only")
        self.assertEqual(plan["launch_profile"], "large-litert-data")
        self.assertEqual(plan["partition_size_mb"], 8192)
        self.assertIn("Planning, dry-run", " ".join(plan["acceptance_criteria"]))
        self.assertIn("Non-acceptance evidence", output)
        self.assertFalse((plan_path.parent / "headless_lane_summary.json").exists())

    def test_whatif_dry_run_is_non_acceptance_even_with_real_run_requested(self):
        with tempfile.TemporaryDirectory(
            prefix="headless_lane_whatif_",
            dir=REPO_ROOT / "artifacts",
        ) as temp_dir:
            output_root = Path(temp_dir).relative_to(REPO_ROOT)
            result = self.run_lane(
                "-OutputRoot",
                str(output_root),
                "-RealRun",
                "-WhatIf",
            )

            output = result.stdout + result.stderr
            self.assertEqual(result.returncode, 0, output)
            plan_path, plan = self.read_plan_from_output(output)

        self.assertFalse(plan["plan_only"])
        self.assertTrue(plan["whatif"])
        self.assertTrue(plan["real_run_requested"])
        self.assertFalse(plan["will_start_emulators"])
        self.assertFalse(plan["will_run_state_pack"])
        self.assertFalse(plan["acceptance_evidence"])
        self.assertTrue(plan["non_acceptance_evidence"])
        self.assertFalse(plan["acceptance_label_allowed"])
        self.assertIn("-LaunchProfile large-litert-data", plan["commands"]["emulator_profile_preflight"])
        self.assertIn("-WhatIf", plan["commands"]["emulator_profile_preflight"])
        self.assertIn("-Headless", plan["commands"]["emulator_real_run"])
        self.assertIn("build_android_ui_state_pack_parallel.ps1", plan["commands"]["state_pack_real_run"])
        self.assertFalse((plan_path.parent / "headless_lane_summary.json").exists())

    def test_without_plan_or_real_run_refuses_before_launching(self):
        with tempfile.TemporaryDirectory(
            prefix="headless_lane_guard_",
            dir=REPO_ROOT / "artifacts",
        ) as temp_dir:
            output_root = Path(temp_dir).relative_to(REPO_ROOT)
            result = self.run_lane(
                "-OutputRoot",
                str(output_root),
                "-BootWaitSeconds",
                "0",
            )

            output = result.stdout + result.stderr
            self.assertNotEqual(result.returncode, 0, output)
            self.assertIn("Refusing to start emulators without -RealRun", output)
            plan_path, plan = self.read_plan_from_output(output)

        self.assertFalse(plan["acceptance_evidence"])
        self.assertTrue(plan["non_acceptance_evidence"])
        self.assertFalse((plan_path.parent / "headless_lane_summary.json").exists())

    def test_real_path_is_gated_and_calls_expected_lane_scripts(self):
        script = SCRIPT.read_text(encoding="utf-8-sig")

        guard_index = script.index('throw "Refusing to start emulators without -RealRun')
        matrix_call_index = script.index("& powershell @matrixRunArgs")
        pack_call_index = script.index("& powershell @packRunArgs")
        acceptance_index = script.index("$acceptanceEvidence = [bool]($RealRun")

        self.assertLess(guard_index, matrix_call_index)
        self.assertLess(matrix_call_index, pack_call_index)
        self.assertLess(acceptance_index, script.index("return [pscustomobject]@{", acceptance_index))
        self.assertIn('"start_senku_emulator_matrix.ps1"', script)
        self.assertIn('"build_android_ui_state_pack_parallel.ps1"', script)
        self.assertIn('"-Roles", "all"', script)
        self.assertIn('"-Headless"', script)
        self.assertIn('"-LaunchProfile", $LaunchProfile', script)
        self.assertIn("acceptance_label_allowed = [bool]$acceptanceEvidence", script)
        self.assertIn("planning_artifacts_are_acceptance = $false", script)
        self.assertIn("if ($null -ne $device.roles)", script)
        self.assertIn("foreach ($role in @($device.roles))", script)

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
                "scripts\\run_android_headless_state_pack_lane.ps1",
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
