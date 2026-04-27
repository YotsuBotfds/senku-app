import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_launch_profile_summary import validate_launch_profile_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_launch_profile_summary.py"


def make_launch_profile_summary() -> dict:
    return {
        "selected_lanes": [
            {
                "role": "tablet_portrait",
                "avd": "Senku_Tablet_2",
                "port": 5554,
                "serial": "emulator-5554",
                "resolution": "1600x2560",
                "orientation": "portrait",
                "emulator_args": [
                    "-avd",
                    "Senku_Tablet_2",
                    "-port",
                    "5554",
                    "-read-only",
                    "-no-snapshot-load",
                    "-no-snapshot-save",
                    "-no-window",
                    "-partition-size",
                    "8192",
                ],
            }
        ],
        "profile_metadata": {
            "profile": "large-litert-data",
            "preflight_only": True,
            "whatif_metadata_only": True,
            "non_acceptance_evidence": True,
            "acceptance_evidence": False,
            "headless": True,
            "partition_size_mb": 8192,
            "cli_partition_size_max_mb": 2047,
            "cli_partition_size_supported": False,
            "data_sizing": "large data partition for LiteRT model and pack transport preflight",
            "snapshot_cache_posture": "read-only/no snapshot load or save expected",
            "expected_role": "tablet_portrait",
            "expected_serial": "emulator-5554",
        },
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "preflight_only": True,
    }


class ValidateAndroidLaunchProfileSummaryTests(unittest.TestCase):
    def write_summary(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "launch_profile_summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_launch_profile_summary_passes(self):
        data, errors = validate_launch_profile_summary(
            self.write_summary(make_launch_profile_summary())
        )

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["profile_metadata"]["profile"], "large-litert-data")

    def test_acceptance_evidence_is_rejected_at_root_and_profile(self):
        summary = make_launch_profile_summary()
        summary["preflight_only"] = False
        summary["non_acceptance_evidence"] = False
        summary["acceptance_evidence"] = True
        summary["profile_metadata"]["preflight_only"] = False
        summary["profile_metadata"]["non_acceptance_evidence"] = False
        summary["profile_metadata"]["acceptance_evidence"] = True

        _, errors = validate_launch_profile_summary(self.write_summary(summary))

        self.assertIn("expected root.preflight_only to be true", errors)
        self.assertIn("expected root.non_acceptance_evidence to be true", errors)
        self.assertIn("expected root.acceptance_evidence to be false", errors)
        self.assertIn("expected profile_metadata.preflight_only to be true", errors)
        self.assertIn("expected profile_metadata.non_acceptance_evidence to be true", errors)
        self.assertIn("expected profile_metadata.acceptance_evidence to be false", errors)

    def test_profile_metadata_shape_is_required(self):
        summary = make_launch_profile_summary()
        del summary["profile_metadata"]["data_sizing"]
        summary["profile_metadata"]["profile"] = ""
        summary["profile_metadata"]["partition_size_mb"] = True

        _, errors = validate_launch_profile_summary(self.write_summary(summary))

        self.assertIn("missing profile_metadata.data_sizing", errors)
        self.assertIn("expected profile_metadata.profile to be non-empty", errors)
        self.assertIn("expected profile_metadata.partition_size_mb to be int|None, got bool", errors)

    def test_large_litert_profile_requires_cli_partition_limit_metadata(self):
        summary = make_launch_profile_summary()
        del summary["profile_metadata"]["cli_partition_size_max_mb"]
        summary["profile_metadata"]["cli_partition_size_supported"] = True

        _, errors = validate_launch_profile_summary(self.write_summary(summary))

        self.assertIn("expected profile_metadata.cli_partition_size_max_mb to be 2047", errors)
        self.assertIn("expected profile_metadata.cli_partition_size_supported to be false", errors)

    def test_selected_lanes_and_concrete_emulator_args_are_required(self):
        summary = make_launch_profile_summary()
        lane = summary["selected_lanes"][0]
        lane["emulator_args"] = ["-avd", "Wrong_AVD", "-read-only"]
        lane["orientation"] = "sideways"

        _, errors = validate_launch_profile_summary(self.write_summary(summary))

        self.assertIn(
            "expected selected_lanes[0].orientation to be one of landscape, portrait",
            errors,
        )
        self.assertIn(
            "expected selected_lanes[0].emulator_args -avd value to be Senku_Tablet_2, got Wrong_AVD",
            errors,
        )
        self.assertIn("missing selected_lanes[0].emulator_args -port", errors)

    def test_expected_profile_role_and_serial_must_match_selected_lanes(self):
        summary = make_launch_profile_summary()
        summary["selected_lanes"][0]["role"] = "phone_portrait"
        summary["selected_lanes"][0]["serial"] = "emulator-5556"

        _, errors = validate_launch_profile_summary(self.write_summary(summary))

        self.assertIn(
            "expected selected_lanes to include profile_metadata.expected_role tablet_portrait",
            errors,
        )
        self.assertIn(
            "expected selected_lanes to include profile_metadata.expected_serial emulator-5554",
            errors,
        )

    def test_cli_reports_failure(self):
        summary = make_launch_profile_summary()
        del summary["selected_lanes"][0]["emulator_args"]
        path = self.write_summary(summary)
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 1)
        self.assertIn("ERROR: missing selected_lanes[0].emulator_args", result.stdout)


if __name__ == "__main__":
    unittest.main()
