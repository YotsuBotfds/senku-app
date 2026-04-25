import json
import tempfile
import unittest
from pathlib import Path

from scripts.compare_contextual_shadow_retrieval import (
    ROWS_FILENAME,
    SUMMARY_FILENAME,
    aggregate_comparison_rows,
    compare_retrieval_row,
    extract_expected_guides_from_wave_manifest,
    load_contextual_shadow_jsonl,
    load_expectations,
    write_outputs,
)


class CompareContextualShadowRetrievalTests(unittest.TestCase):
    def test_load_contextual_shadow_jsonl_reads_valid_records(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            shadow_path = Path(tmpdir) / "contextual_shadow.jsonl"
            records = [
                {
                    "chunk_id": "chunk_a",
                    "document": "raw A",
                    "contextual_retrieval_text": "Guide: A\n\nraw A",
                    "metadata": {"guide_id": "GD-111", "slug": "guide-a"},
                },
                {
                    "chunk_id": "chunk_b",
                    "document": "raw B",
                    "contextual_retrieval_text": "Guide: B\n\nraw B",
                },
            ]
            shadow_path.write_text(
                "\n".join(json.dumps(record) for record in records) + "\n\n",
                encoding="utf-8",
            )

            loaded = load_contextual_shadow_jsonl(shadow_path)

        self.assertEqual(len(loaded), 2)
        self.assertEqual(loaded[0]["chunk_id"], "chunk_a")
        self.assertEqual(loaded[0]["document"], "raw A")
        self.assertEqual(loaded[0]["contextual_retrieval_text"], "Guide: A\n\nraw A")
        self.assertEqual(loaded[0]["metadata"], {"guide_id": "GD-111", "slug": "guide-a"})
        self.assertEqual(loaded[1]["metadata"], {})

    def test_extract_expected_guides_from_wave_manifest_uses_wave_key_from_artifact_filename(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            manifest_path = Path(tmpdir) / "expectations.yaml"
            manifest_path.write_text(
                """waves:
  fc:
    topic: abdominal_trauma_emergency
    expected_guides:
      - id: GD-380
      - slug: trauma-hemorrhage-control
""",
                encoding="utf-8",
            )
            expectations = load_expectations(manifest_path)
            artifact_path = Path(tmpdir) / "guide_wave_fc_20260424_093011.json"

            info = extract_expected_guides_from_wave_manifest(
                artifact_path,
                expectations,
                {"slug_to_id": {"trauma-hemorrhage-control": "GD-297"}},
            )

        self.assertEqual(info["wave_key"], "fc")
        self.assertEqual(info["expected_topic"], "abdominal_trauma_emergency")
        self.assertEqual(info["expected_guide_ids"], ["GD-380", "GD-297"])

    def test_hit_1_3_k_delta_aggregation(self):
        rows = [
            compare_retrieval_row(
                artifact_path="guide_wave_fc_20260424_093011.json",
                prompt_index=1,
                question="first",
                expected_guide_ids=["GD-380"],
                baseline_top_guide_ids=["GD-111", "GD-380"],
                shadow_top_guide_ids=["GD-380", "GD-111"],
                expected_topic="abdominal_trauma_emergency",
            ),
            compare_retrieval_row(
                artifact_path="guide_wave_fc_20260424_093011.json",
                prompt_index=2,
                question="second",
                expected_guide_ids=["GD-297"],
                baseline_top_guide_ids=["GD-001"],
                shadow_top_guide_ids=["GD-297", "GD-380", "GD-001"],
                expected_topic="abdominal_trauma_emergency",
            ),
            compare_retrieval_row(
                artifact_path="guide_wave_fc_20260424_093011.json",
                prompt_index=3,
                question="third",
                expected_guide_ids=["GD-297", "GD-380"],
                baseline_top_guide_ids=["GD-297", "GD-380"],
                shadow_top_guide_ids=["GD-380", "GD-297"],
                expected_topic="abdominal_trauma_emergency",
            ),
        ]

        summary = aggregate_comparison_rows(rows)

        self.assertEqual(summary["row_count"], 3)
        self.assertEqual(summary["expected_row_count"], 3)
        self.assertEqual(summary["baseline"]["scored_rows"], 3)
        self.assertEqual(summary["baseline"]["hit_at_1"]["count"], 1)
        self.assertEqual(summary["baseline"]["hit_at_3"]["count"], 2)
        self.assertEqual(summary["baseline"]["hit_at_k"]["count"], 2)
        self.assertEqual(summary["shadow"]["scored_rows"], 3)
        self.assertEqual(summary["shadow"]["hit_at_1"]["count"], 3)
        self.assertEqual(summary["shadow"]["hit_at_3"]["count"], 3)
        self.assertEqual(summary["shadow"]["hit_at_k"]["count"], 3)
        self.assertEqual(summary["deltas"]["hit_at_1"]["improved"], 2)
        self.assertEqual(summary["deltas"]["hit_at_1"]["regressed"], 0)
        self.assertEqual(summary["deltas"]["hit_at_3"]["improved"], 1)
        self.assertEqual(summary["deltas"]["hit_at_k"]["improved"], 1)

    def test_row_jsonl_and_summary_outputs(self):
        rows = [
            compare_retrieval_row(
                artifact_path="guide_wave_fc_20260424_093011.json",
                prompt_index=1,
                question="first",
                expected_guide_ids=["GD-380"],
                baseline_top_guide_ids=["GD-111", "GD-380"],
                shadow_top_guide_ids=["GD-380", "GD-111"],
            ),
            compare_retrieval_row(
                artifact_path="guide_wave_fc_20260424_093011.json",
                prompt_index=2,
                question="second",
                expected_guide_ids=["GD-297"],
                baseline_top_guide_ids=["GD-001"],
                shadow_top_guide_ids=["GD-297", "GD-380", "GD-001"],
            ),
        ]
        summary = aggregate_comparison_rows(rows)

        with tempfile.TemporaryDirectory() as tmpdir:
            outputs = write_outputs(rows, summary, tmpdir)
            rows_path = Path(outputs["rows_jsonl"])
            summary_path = Path(outputs["summary_json"])

            self.assertEqual(rows_path.name, ROWS_FILENAME)
            self.assertEqual(summary_path.name, SUMMARY_FILENAME)
            loaded_rows = [
                json.loads(line)
                for line in rows_path.read_text(encoding="utf-8").splitlines()
            ]
            loaded_summary = json.loads(summary_path.read_text(encoding="utf-8"))

        self.assertEqual(len(loaded_rows), 2)
        self.assertEqual(loaded_rows[0]["shadow_top_guide_ids"], ["GD-380", "GD-111"])
        self.assertEqual(loaded_summary["row_count"], 2)
        self.assertIn("baseline", loaded_summary)
        self.assertIn("shadow", loaded_summary)
        self.assertIn("hit_at_1", loaded_summary["deltas"])


if __name__ == "__main__":
    unittest.main()
