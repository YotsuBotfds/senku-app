import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_headless_state_pack_lane_summary import validate_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_headless_state_pack_lane_summary.py"


FIXED_MATRIX = [
    {
        "role": "phone_portrait",
        "device": "emulator-5556",
        "avd": "Senku_Large_4",
        "dimensions": "1080x2400",
        "orientation": "portrait",
    },
    {
        "role": "phone_landscape",
        "device": "emulator-5560",
        "avd": "Senku_Large_3",
        "dimensions": "2400x1080",
        "orientation": "landscape",
    },
    {
        "role": "tablet_portrait",
        "device": "emulator-5554",
        "avd": "Senku_Tablet_2",
        "dimensions": "1600x2560",
        "orientation": "portrait",
    },
    {
        "role": "tablet_landscape",
        "device": "emulator-5558",
        "avd": "Senku_Tablet",
        "dimensions": "2560x1600",
        "orientation": "landscape",
    },
]

FIXED_ROLES = [entry["role"] for entry in FIXED_MATRIX]


def make_plan(plan_only: bool = True, whatif: bool = False) -> dict:
    return {
        "run_id": "20260427_120000",
        "output_root": "artifacts/ui_state_pack_headless_lane",
        "run_dir": "artifacts/ui_state_pack_headless_lane/20260427_120000",
        "fixed_matrix_roles": FIXED_ROLES,
        "fixed_matrix": FIXED_MATRIX,
        "launch_profile": "large-litert-data",
        "headless": True,
        "mode": "read_only",
        "partition_size_mb": 8192,
        "max_parallel_devices": 4,
        "effective_max_parallel_devices": 4,
        "boot_wait_seconds": 90,
        "host_inference_url": "http://10.0.2.2:1235/v1",
        "host_inference_model": "gemma-4-e2b-it-litert",
        "skip_build": False,
        "skip_install": False,
        "skip_host_states": False,
        "plan_only": plan_only,
        "whatif": whatif,
        "real_run_requested": False,
        "will_start_emulators": False,
        "will_run_state_pack": False,
        "acceptance_evidence": False,
        "non_acceptance_evidence": True,
        "acceptance_label_allowed": False,
        "acceptance_criteria": ["Real run only"],
        "planning_artifacts_are_acceptance": False,
        "commands": {
            "emulator_profile_preflight": "powershell -File scripts/start_senku_emulator_matrix.ps1 -WhatIf",
            "emulator_real_run": "powershell -File scripts/start_senku_emulator_matrix.ps1",
            "state_pack_real_run": "powershell -File scripts/build_android_ui_state_pack_parallel.ps1",
        },
    }


def make_pack_summary() -> dict:
    return {
        "status": "pass",
        "total_states": 45,
        "pass_count": 45,
        "fail_count": 0,
        "devices": [
            {"device": "emulator-5556", "roles": ["phone_portrait"]},
            {"device": "emulator-5560", "roles": ["phone_landscape"]},
            {"device": "emulator-5554", "roles": ["tablet_portrait"]},
            {"device": "emulator-5558", "roles": ["tablet_landscape"]},
        ],
    }


def make_real_summary(pack_path: str) -> dict:
    return {
        "run_id": "20260427_120000",
        "output_root": "artifacts/ui_state_pack_headless_lane",
        "run_dir": "artifacts/ui_state_pack_headless_lane/20260427_120000",
        "state_pack_summary_path": pack_path,
        "status": "pass",
        "real_run": True,
        "fixed_four_matrix_output": True,
        "state_pack_passed": True,
        "acceptance_evidence": True,
        "non_acceptance_evidence": False,
        "acceptance_label_allowed": True,
        "acceptance_criteria": ["Fixed four-emulator matrix only"],
        "pack_status": "pass",
        "pack_total_states": 45,
        "pack_pass_count": 45,
        "pack_fail_count": 0,
    }


class ValidateAndroidHeadlessStatePackLaneSummaryTests(unittest.TestCase):
    def write_json(self, payload: dict, name: str) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / name
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def write_fixture_pair(self, lane_payload: dict, pack_payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        root = Path(temp_dir.name)
        pack_path = root / "state_pack_summary.json"
        pack_path.write_text(json.dumps(pack_payload), encoding="utf-8")
        lane_payload["state_pack_summary_path"] = str(pack_path)
        lane_path = root / "headless_lane_summary.json"
        lane_path.write_text(json.dumps(lane_payload), encoding="utf-8")
        return lane_path

    def test_plan_only_summary_is_non_acceptance(self):
        _, errors = validate_summary(self.write_json(make_plan(plan_only=True), "headless_lane_plan.json"))

        self.assertEqual(errors, [])

    def test_whatif_summary_is_non_acceptance(self):
        _, errors = validate_summary(self.write_json(make_plan(plan_only=False, whatif=True), "headless_lane_plan.json"))

        self.assertEqual(errors, [])

    def test_plan_rejects_acceptance_and_runtime_claims(self):
        plan = make_plan()
        plan["acceptance_evidence"] = True
        plan["non_acceptance_evidence"] = False
        plan["will_start_emulators"] = True
        plan["will_run_state_pack"] = True

        _, errors = validate_summary(self.write_json(plan, "headless_lane_plan.json"))

        self.assertIn("expected root.acceptance_evidence to be False, got True", errors)
        self.assertIn("expected root.non_acceptance_evidence to be True, got False", errors)
        self.assertIn("expected root.will_start_emulators to be False, got True", errors)
        self.assertIn("expected root.will_run_state_pack to be False, got True", errors)

    def test_real_run_acceptance_validates_referenced_fixed_four_pack(self):
        pack_path = "placeholder"
        lane_path = self.write_fixture_pair(make_real_summary(pack_path), make_pack_summary())

        _, errors = validate_summary(lane_path)

        self.assertEqual(errors, [])

    def test_real_run_acceptance_tolerates_legacy_scalar_role(self):
        pack = make_pack_summary()
        for device in pack["devices"]:
            device["role"] = device["roles"][0]
            del device["roles"]
        lane_path = self.write_fixture_pair(make_real_summary("placeholder"), pack)

        _, errors = validate_summary(lane_path)

        self.assertEqual(errors, [])

    def test_real_run_rejects_missing_fixed_four_role(self):
        pack = make_pack_summary()
        pack["devices"].pop()
        lane_path = self.write_fixture_pair(make_real_summary("placeholder"), pack)

        _, errors = validate_summary(lane_path)

        self.assertIn("expected state_pack.devices to include four role-bearing device entries", errors)
        self.assertIn("expected state_pack.devices roles to cover fixed four matrix", errors)

    def test_real_run_rejects_pack_count_mismatch(self):
        pack = make_pack_summary()
        pack["pass_count"] = 44
        lane = make_real_summary("placeholder")
        lane["pack_pass_count"] = 44
        lane_path = self.write_fixture_pair(lane, pack)

        _, errors = validate_summary(lane_path)

        self.assertIn("expected root.pack_pass_count to equal root.pack_total_states", errors)
        self.assertIn("expected state_pack.pass_count to equal state_pack.total_states", errors)

    def test_non_acceptance_summary_allows_failed_real_run_without_acceptance_claim(self):
        summary = make_real_summary("")
        summary.update(
            {
                "status": "non_acceptance",
                "state_pack_summary_path": None,
                "real_run": True,
                "fixed_four_matrix_output": False,
                "state_pack_passed": False,
                "acceptance_evidence": False,
                "non_acceptance_evidence": True,
                "acceptance_label_allowed": False,
                "pack_status": None,
                "pack_total_states": 0,
                "pack_pass_count": 0,
                "pack_fail_count": 0,
            }
        )

        _, errors = validate_summary(self.write_json(summary, "headless_lane_summary.json"))

        self.assertEqual(errors, [])

    def test_non_acceptance_summary_rejects_fixed_four_acceptance_claims(self):
        summary = make_real_summary("")
        summary.update(
            {
                "status": "non_acceptance",
                "acceptance_evidence": False,
                "non_acceptance_evidence": True,
                "acceptance_label_allowed": False,
            }
        )

        _, errors = validate_summary(self.write_json(summary, "headless_lane_summary.json"))

        self.assertIn("expected non-acceptance summaries to avoid fixed-four state-pack acceptance claims", errors)

    def test_cli_reports_failure(self):
        plan = make_plan()
        plan["whatif"] = False
        plan["plan_only"] = False
        path = self.write_json(plan, "headless_lane_plan.json")
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 1)
        self.assertIn("ERROR: expected PlanOnly/WhatIf lane plan to have plan_only or whatif true", result.stdout)


if __name__ == "__main__":
    unittest.main()
