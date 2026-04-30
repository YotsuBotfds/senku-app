import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_ui_state_pack_plan import validate_plan


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_ui_state_pack_plan.py"


def make_plan() -> dict:
    return {
        "run_id": "20260427_122409",
        "output_root": "artifacts\\bench\\android_planonly_probe_integrator",
        "preflight_only": True,
        "acceptance_evidence": False,
        "non_acceptance_evidence": True,
        "will_build": False,
        "will_install": False,
        "will_start_role_jobs": False,
        "will_finalize": False,
        "selected_roles": [
            "phone_portrait",
            "phone_landscape",
            "tablet_portrait",
            "tablet_landscape",
        ],
        "devices": [
            {
                "role": "phone_portrait",
                "device": "emulator-5556",
                "orientation": "portrait",
                "avd": "Senku_Large_4",
                "dimensions": "1080x2400",
            },
            {
                "role": "phone_landscape",
                "device": "emulator-5560",
                "orientation": "landscape",
                "avd": "Senku_Large_3",
                "dimensions": "2400x1080",
            },
            {
                "role": "tablet_portrait",
                "device": "emulator-5554",
                "orientation": "portrait",
                "avd": "Senku_Tablet_2",
                "dimensions": "1600x2560",
            },
            {
                "role": "tablet_landscape",
                "device": "emulator-5558",
                "orientation": "landscape",
                "avd": "Senku_Tablet",
                "dimensions": "2560x1600",
            },
        ],
        "skip_build": False,
        "skip_install": True,
        "skip_host_states": False,
        "host_inference_url": "http://10.0.2.2:1235/v1",
        "host_inference_model": "gemma-4-e2b-it-litert",
        "max_parallel_devices": 4,
        "effective_max_parallel_devices": 4,
        "plan_only": True,
        "migration_checklist_intent": {
            "selected_roles": [
                "phone_portrait",
                "phone_landscape",
                "tablet_portrait",
                "tablet_landscape",
            ],
            "host_flags": {
                "skip_host_states": False,
                "will_request_host_states": True,
            },
            "host_model_identity": {
                "host_inference_url": "http://10.0.2.2:1235/v1",
                "host_inference_model": "gemma-4-e2b-it-litert",
            },
            "skip_flags": {
                "skip_build": False,
                "skip_install": True,
                "skip_host_states": False,
            },
            "max_parallel_devices": 4,
            "effective_max_parallel_devices": 4,
            "acceptance_evidence": False,
            "non_acceptance_evidence": True,
            "preflight_only": True,
        },
        "launchers": [
            {
                "role": "phone_portrait",
                "command": '& "scripts\\build_android_ui_state_pack.ps1" -RoleFilter "phone_portrait" -SkipFinalize -SkipBuild -SkipInstall',
            },
            {
                "role": "phone_landscape",
                "command": '& "scripts\\build_android_ui_state_pack.ps1" -RoleFilter "phone_landscape" -SkipFinalize -SkipBuild -SkipInstall',
            },
            {
                "role": "tablet_portrait",
                "command": '& "scripts\\build_android_ui_state_pack.ps1" -RoleFilter "tablet_portrait" -SkipFinalize -SkipBuild -SkipInstall',
            },
            {
                "role": "tablet_landscape",
                "command": '& "scripts\\build_android_ui_state_pack.ps1" -RoleFilter "tablet_landscape" -SkipFinalize -SkipBuild -SkipInstall',
            },
        ],
        "goal_pack": {
            "canonical_directory": "mocks",
            "expected_png_count": 22,
            "expected_png_names": sorted(
                [
                    f"{family}-{posture}.png"
                    for family in ("home", "search", "thread", "guide", "answer")
                    for posture in (
                        "phone-portrait",
                        "phone-landscape",
                        "tablet-portrait",
                        "tablet-landscape",
                    )
                ]
                + ["emergency-phone-portrait.png", "emergency-tablet-portrait.png"]
            ),
            "validator": "scripts/validate_android_mock_goal_pack.py",
            "excludes_debug_intermediate_screenshots": True,
        },
    }


class ValidateAndroidUiStatePackPlanTests(unittest.TestCase):
    def write_plan(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "plan.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_fixed_four_plan_passes(self):
        data, errors = validate_plan(self.write_plan(make_plan()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(len(data["selected_roles"]), 4)

    def test_valid_filtered_plan_passes_when_roles_devices_and_launchers_match(self):
        plan = make_plan()
        plan["selected_roles"] = ["phone_portrait", "tablet_landscape"]
        plan["devices"] = [plan["devices"][0], plan["devices"][3]]
        plan["launchers"] = [plan["launchers"][0], plan["launchers"][3]]
        plan["migration_checklist_intent"]["selected_roles"] = ["phone_portrait", "tablet_landscape"]
        plan["max_parallel_devices"] = 0
        plan["effective_max_parallel_devices"] = 1
        plan["migration_checklist_intent"]["max_parallel_devices"] = 0
        plan["migration_checklist_intent"]["effective_max_parallel_devices"] = 1

        _, errors = validate_plan(self.write_plan(plan))

        self.assertEqual(errors, [])

    def test_rejects_acceptance_or_runtime_posture(self):
        plan = make_plan()
        plan["acceptance_evidence"] = True
        plan["will_start_role_jobs"] = True
        plan["migration_checklist_intent"]["acceptance_evidence"] = True

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn("expected root.acceptance_evidence to be false", errors)
        self.assertIn("expected root.will_start_role_jobs to be false", errors)
        self.assertIn("expected migration_checklist_intent.acceptance_evidence to be false", errors)

    def test_rejects_wrong_device_matrix_shape(self):
        plan = make_plan()
        plan["devices"][1]["device"] = "emulator-9999"
        plan["devices"][2]["orientation"] = "landscape"
        plan["devices"].pop()

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn("expected root.devices[1].device to be 'emulator-5560'", errors)
        self.assertIn("expected root.devices[2].orientation to be 'portrait'", errors)
        self.assertIn("expected root.devices roles to exactly match root.selected_roles", errors)

    def test_rejects_skip_host_and_model_identity_mismatches(self):
        plan = make_plan()
        plan["skip_host_states"] = True
        plan["migration_checklist_intent"]["host_flags"]["skip_host_states"] = False
        plan["migration_checklist_intent"]["host_flags"]["will_request_host_states"] = True
        plan["migration_checklist_intent"]["skip_flags"]["skip_install"] = False
        plan["migration_checklist_intent"]["host_model_identity"]["host_inference_model"] = "other-model"

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn("expected migration_checklist_intent.host_flags.skip_host_states to mirror root.skip_host_states", errors)
        self.assertIn(
            "expected migration_checklist_intent.host_flags.will_request_host_states "
            "to be the inverse of root.skip_host_states",
            errors,
        )
        self.assertIn("expected migration_checklist_intent.skip_flags.skip_install to mirror root.skip_install", errors)
        self.assertIn(
            "expected migration_checklist_intent.host_model_identity.host_inference_model "
            "to mirror root.host_inference_model",
            errors,
        )

    def test_rejects_effective_parallel_and_intent_mismatch(self):
        plan = make_plan()
        plan["max_parallel_devices"] = 0
        plan["effective_max_parallel_devices"] = 0
        plan["migration_checklist_intent"]["max_parallel_devices"] = 2

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn("expected root.effective_max_parallel_devices to be 1", errors)
        self.assertIn("expected migration_checklist_intent.max_parallel_devices to mirror root.max_parallel_devices", errors)

    def test_rejects_missing_launcher_for_selected_role(self):
        plan = make_plan()
        plan["launchers"].pop()
        plan["launchers"][0]["command"] = '& "scripts\\build_android_ui_state_pack.ps1" -RoleFilter "other"'

        _, errors = validate_plan(self.write_plan(plan))

        self.assertIn("expected root.launchers[0].command to contain selected role 'phone_portrait'", errors)
        self.assertIn("expected root.launchers[0].command to keep launcher in slice/preflight posture", errors)
        self.assertIn("expected root.launchers to contain exactly one launcher for each selected role", errors)

    def test_cli_reports_failure(self):
        plan = make_plan()
        plan["plan_only"] = False
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
        self.assertIn("ERROR: expected root.plan_only to be true", result.stdout)


if __name__ == "__main__":
    unittest.main()
