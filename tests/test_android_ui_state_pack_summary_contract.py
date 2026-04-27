import json
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "build_android_ui_state_pack.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidUiStatePackSummaryContractTests(unittest.TestCase):
    def test_finalize_rolls_up_child_installed_pack_metadata(self):
        with tempfile.TemporaryDirectory(
            prefix="ui_state_pack_summary_",
            dir=REPO_ROOT / "artifacts",
        ) as temp_dir:
            temp_root = Path(temp_dir)
            output_root = temp_root.relative_to(REPO_ROOT)
            run_id = "summary_contract"
            run_dir = temp_root / run_id
            role_manifest_dir = run_dir / "role_manifests"
            child_summary_dir = temp_root / "child_summaries"
            role_manifest_dir.mkdir(parents=True)
            child_summary_dir.mkdir()

            child_summary_path = child_summary_dir / "summary.json"
            child_summary_path.write_text(
                json.dumps(
                    {
                        "status": "pass",
                        "apk_sha": "abc123",
                        "model_name": "fixture-model",
                        "model_sha": "def456",
                        "installed_pack": {
                            "status": "available",
                            "pack_format": "senku-mobile-pack-v2",
                            "pack_version": 2,
                            "generated_at": "2026-04-27T04:21:12.533181+00:00",
                            "counts": {"answer_cards": 271},
                            "sqlite": {"manifest_sha256": "sqlite-sha"},
                            "vectors": {"manifest_sha256": "vectors-sha"},
                        },
                    }
                ),
                encoding="utf-8",
            )
            (role_manifest_dir / "phone_portrait.json").write_text(
                json.dumps(
                    {
                        "role": "phone_portrait",
                        "device": "emulator-5556",
                        "orientation": "portrait",
                        "results": [
                            {
                                "role": "phone_portrait",
                                "device": "emulator-5556",
                                "orientation": "portrait",
                                "state_method": "fixtureState",
                                "test_method": "fixtureState",
                                "status": "pass",
                                "summary_path": str(child_summary_path),
                                "screenshots": [],
                                "dumps": [],
                            }
                        ],
                    }
                ),
                encoding="utf-8",
            )

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
                    "-RunId",
                    run_id,
                    "-FinalizeOnly",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            summary = json.loads((run_dir / "summary.json").read_text(encoding="utf-8-sig"))
            installed_pack = summary["installed_pack"]

            self.assertTrue(installed_pack["metadata_present"])
            self.assertTrue(installed_pack["matrix_homogeneous"])
            self.assertEqual(installed_pack["pack_format"], "senku-mobile-pack-v2")
            self.assertEqual(installed_pack["pack_version"], 2)
            self.assertEqual(installed_pack["generated_at"], "2026-04-27T04:21:12.533181+00:00")
            self.assertEqual(installed_pack["answer_cards"], 271)
            self.assertEqual(installed_pack["sqlite_manifest_sha256"], "sqlite-sha")
            self.assertEqual(installed_pack["vectors_manifest_sha256"], "vectors-sha")
            self.assertEqual(installed_pack["devices"][0]["device"], "emulator-5556")
            self.assertEqual(installed_pack["devices"][0]["available_count"], 1)
            self.assertEqual(installed_pack["devices"][0]["statuses"], ["available"])

    def test_parser_gate_passes_for_state_pack_builder(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\build_android_ui_state_pack.ps1",
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
