import json
import tempfile
import unittest
from pathlib import Path

from scripts.summarize_android_migration_proof import (
    rows_to_markdown,
    summarize_file,
    summarize_summary,
)


class SummarizeAndroidMigrationProofTests(unittest.TestCase):
    def test_state_pack_row_includes_counts_identity_and_acceptance_posture(self) -> None:
        row = summarize_summary(
            {
                "status": "pass",
                "output_dir": "artifacts/state-pack/run",
                "total_states": 45,
                "pass_count": 45,
                "fail_count": 0,
                "skip_count": 0,
                "platform_anr_count": 0,
                "matrix_homogeneous": True,
                "matrix_model_name": "gemma-4-e2b-it-litert",
                "matrix_apk_sha": "apk-sha",
                "devices": [
                    {"device": "emulator-5556", "identity_missing": False},
                    {"device": "emulator-5560", "identity_missing": False},
                ],
                "installed_pack": {
                    "source": "bundled",
                    "cache_hit": False,
                    "pushed": False,
                    "manifest_counts": {
                        "guides": 754,
                        "chunks": 49841,
                        "answer_cards": 271,
                    },
                },
                "host_adb_platform_tools_version": "37.0.0-14910828",
            }
        )

        self.assertEqual(row["status"], "pass")
        self.assertEqual(row["passes"], "45/45")
        self.assertEqual(row["artifact_path"], "artifacts/state-pack/run")
        self.assertEqual(row["platform_anr_count"], 0)
        self.assertIs(row["matrix_homogeneous"], True)
        self.assertEqual(row["matrix_model_name"], "gemma-4-e2b-it-litert")
        self.assertIs(row["identity_missing"], False)
        self.assertEqual(row["apk_sha"], "apk-sha")
        self.assertEqual(
            row["installed_pack_counts"],
            {"guides": 754, "chunks": 49841, "answer_cards": 271},
        )
        self.assertEqual(row["installed_pack_source"], "bundled")
        self.assertIs(row["installed_pack_cache"], False)
        self.assertIs(row["installed_pack_pushed"], False)
        self.assertEqual(row["evidence_posture"], "acceptance")

    def test_fts_fallback_row_uses_runtime_evidence_as_non_acceptance(self) -> None:
        row = summarize_summary(
            {
                "passed_count": 2,
                "failed_devices": [],
                "expected_tests": 3,
                "runtime_evidence": "fts4_fallback",
                "dry_run": False,
                "host_adb_platform_tools_version": "dry_run",
                "device_count": 2,
            }
        )

        self.assertEqual(row["status"], "pass")
        self.assertEqual(row["passes"], "2/2")
        self.assertEqual(row["runtime_evidence"], "fts4_fallback")
        self.assertEqual(row["host_adb_platform_tools_version"], "dry_run")
        self.assertEqual(row["evidence_posture"], "non-acceptance")

    def test_non_acceptance_tooling_row_uses_pack_highlights(self) -> None:
        row = summarize_summary(
            {
                "status": "pass",
                "output": "artifacts/bench/parity.json",
                "non_acceptance_evidence": True,
                "acceptance_evidence": False,
                "evidence_kind": "asset_pack_parity",
                "candidate_highlights": {
                    "manifest_counts": {
                        "guides": 754,
                        "chunks": 49841,
                        "answer_cards": 271,
                        "deterministic_rules": 9,
                    }
                },
            }
        )

        self.assertEqual(row["artifact_path"], "artifacts/bench/parity.json")
        self.assertEqual(row["installed_pack_source"], "asset_pack_parity")
        self.assertEqual(
            row["installed_pack_counts"],
            {
                "guides": 754,
                "chunks": 49841,
                "answer_cards": 271,
                "deterministic_rules": 9,
            },
        )
        self.assertEqual(row["evidence_posture"], "non-acceptance")

    def test_summarize_file_reads_without_mutating_and_markdown_is_compact(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "summary.json"
            payload = {"status": "pass", "pass_count": 1, "total_states": 1}
            path.write_text(json.dumps(payload), encoding="utf-8")
            before = path.read_text(encoding="utf-8")

            row = summarize_file(path)
            markdown = rows_to_markdown([row])

            self.assertEqual(path.read_text(encoding="utf-8"), before)
            self.assertEqual(row["artifact_path"], str(path))
            self.assertIn("| status | passes |", markdown)
            self.assertIn("| pass | 1/1 |", markdown)


if __name__ == "__main__":
    unittest.main()
