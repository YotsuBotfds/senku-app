import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_harness_matrix_plan import validate_plan


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_harness_matrix_plan.py"


def make_plan() -> dict:
    rows = [
        {
            "row_number": 1,
            "run_label": "prompt_case",
            "mode": "prompt",
            "emulator": "emulator-5556",
            "posture": "phone_portrait",
            "device_role": "phone",
            "orientation": "portrait",
            "warm_start": True,
            "push_pack_dir": "artifacts\\packs\\candidate",
            "will_push_pack": True,
            "skip_pack_push_if_current": True,
            "force_pack_push": False,
            "runner_command": "powershell -File scripts\\run_android_prompt_detail.ps1 -RunLabel prompt_case",
        },
        {
            "row_number": 2,
            "run_label": "followup_case",
            "mode": "detail_followup",
            "emulator": "emulator-5558",
            "posture": "tablet_landscape",
            "device_role": "tablet",
            "orientation": "landscape",
            "warm_start": False,
            "push_pack_dir": "",
            "will_push_pack": False,
            "skip_pack_push_if_current": True,
            "force_pack_push": False,
            "runner_command": "powershell -File scripts\\run_android_detail_followup.ps1 -RunLabel followup_case",
        },
    ]
    return {
        "plan_kind": "android_harness_matrix",
        "preflight_only": True,
        "plan_only": True,
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "will_start_jobs": False,
        "will_touch_emulators": False,
        "will_write_run_manifests": False,
        "row_count": 2,
        "selected_emulators": ["emulator-5556", "emulator-5558"],
        "push_pack_dir": "artifacts\\packs\\candidate",
        "skip_pack_push_if_current": True,
        "force_pack_push": False,
        "rows": rows,
        "posture_groups": [
            {
                "posture": "phone_portrait",
                "row_count": 1,
                "emulators": ["emulator-5556"],
                "run_labels": ["prompt_case"],
            },
            {
                "posture": "tablet_landscape",
                "row_count": 1,
                "emulators": ["emulator-5558"],
                "run_labels": ["followup_case"],
            },
        ],
        "runner_commands": [row["runner_command"] for row in rows],
        "validation_commands": [
            {
                "name": "validate_summary_json",
                "command": (
                    "& .\\.venvs\\senku-validate\\Scripts\\python.exe "
                    "scripts\\validate_android_harness_matrix_plan.py summary.json"
                ),
                "validates": "summary.json",
                "summary_json_path": "summary.json",
                "plan_only": True,
                "will_start_jobs": False,
                "will_touch_emulators": False,
            }
        ],
        "migration_checklist_intent": {
            "plan_kind": "android_harness_matrix",
            "preflight_only": True,
            "plan_only": True,
            "non_acceptance_evidence": True,
            "acceptance_evidence": False,
            "will_start_jobs": False,
            "will_touch_emulators": False,
            "posture_groups": ["phone_portrait", "tablet_landscape"],
        },
    }


class ValidateAndroidHarnessMatrixPlanTests(unittest.TestCase):
    def write_plan(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_plan_passes(self):
        data, errors = validate_plan(self.write_plan(make_plan()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["plan_kind"], "android_harness_matrix")

    def test_rejects_non_planonly_or_acceptance_posture(self):
        plan = make_plan()
        plan["plan_kind"] = "android_matrix"
        plan["preflight_only"] = False
        plan["acceptance_evidence"] = True
        plan["will_touch_emulators"] = True
        plan["dry_run"] = True

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn("expected root.plan_kind to be 'android_harness_matrix'", errors)
        self.assertIn("expected root.preflight_only to be true", errors)
        self.assertIn("expected root.acceptance_evidence to be false", errors)
        self.assertIn("expected root.will_touch_emulators to be false", errors)
        self.assertIn("expected root.dry_run to be absent for PlanOnly summaries", errors)

    def test_rejects_row_and_posture_mismatch(self):
        plan = make_plan()
        plan["row_count"] = 3
        plan["runner_commands"][0] = "different command"
        plan["rows"][0]["emulator"] = "emulator-5560"
        plan["posture_groups"][0]["row_count"] = 2
        plan["posture_groups"][0]["emulators"] = ["emulator-5556"]

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn("expected root.row_count to equal len(root.rows)", errors)
        self.assertIn("expected root.runner_commands[0] to mirror rows[0].runner_command", errors)
        self.assertIn("expected posture_groups[0].row_count to match rows with posture 'phone_portrait'", errors)
        self.assertIn("expected posture_groups[0].emulators to match selected rows", errors)

    def test_rejects_bad_validation_command_posture(self):
        plan = make_plan()
        command = plan["validation_commands"][0]
        command["plan_only"] = False
        command["will_start_jobs"] = True
        command["will_touch_emulators"] = True
        command["command"] = "powershell -File scripts\\run_android_harness_matrix.ps1"

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn("expected validation_commands[0].plan_only to be true", errors)
        self.assertIn("expected validation_commands[0].will_start_jobs to be false", errors)
        self.assertIn("expected validation_commands[0].will_touch_emulators to be false", errors)
        self.assertIn("expected validation_commands[0].command to look like a validation command", errors)

    def test_rejects_bad_pack_push_intent(self):
        plan = make_plan()
        plan["rows"][0]["will_push_pack"] = False
        plan["rows"][1]["skip_pack_push_if_current"] = "yes"

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn("expected rows[0].will_push_pack to mirror non-empty rows[0].push_pack_dir", errors)
        self.assertIn("expected rows[1].skip_pack_push_if_current to be bool, got str", errors)

    def test_rejects_migration_checklist_mirror_mismatch(self):
        plan = make_plan()
        plan["migration_checklist_intent"]["will_touch_emulators"] = True
        plan["migration_checklist_intent"]["posture_groups"] = ["phone_portrait"]

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn(
            "expected migration_checklist_intent.will_touch_emulators to mirror root.will_touch_emulators",
            errors,
        )
        self.assertIn("expected migration_checklist_intent.posture_groups to mirror root.posture_groups", errors)

    def test_cli_reports_failure_without_emulator_work(self):
        plan = make_plan()
        plan["will_touch_emulators"] = True
        path = self.write_plan(plan)
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 1)
        self.assertIn("ERROR: expected root.will_touch_emulators to be false", result.stdout)


if __name__ == "__main__":
    unittest.main()
