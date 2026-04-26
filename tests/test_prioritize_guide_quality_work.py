import json
import tempfile
import unittest
from pathlib import Path

from scripts.prioritize_guide_quality_work import collect_priorities, render_markdown


class PrioritizeGuideQualityWorkTests(unittest.TestCase):
    def test_prioritizer_joins_diagnostics_metadata_and_markers(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            diag_dir = root / "diag"
            diag_dir.mkdir()
            metadata_path = root / "metadata.json"
            markers_path = root / "markers.json"
            (diag_dir / "diagnostics.json").write_text(
                json.dumps(
                    {
                        "rows": [
                            {
                                "artifact_name": "sample.json",
                                "prompt_index": 1,
                                "prompt_text": "Help with a dangerous wound",
                                "suspected_failure_bucket": "ranking_miss",
                                "app_acceptance_status": "needs_evidence_owner",
                                "expected_guide_ids": "GD-100",
                                "cited_guide_ids": "GD-200",
                                "top_retrieved_guide_ids": "GD-200|GD-100",
                            },
                            {
                                "artifact_name": "sample.json",
                                "prompt_index": 2,
                                "prompt_text": "Same wound",
                                "suspected_failure_bucket": "expected_supported",
                                "app_acceptance_status": "strong_supported",
                                "expected_guide_ids": "GD-100",
                                "cited_guide_ids": "GD-100",
                                "top_retrieved_guide_ids": "GD-100|GD-200",
                            },
                        ]
                    }
                ),
                encoding="utf-8",
            )
            metadata_path.write_text(
                json.dumps(
                    {
                        "guides": [
                            {
                                "guide_id": "GD-100",
                                "category": "medical",
                                "liability_level": "high",
                                "high_liability": True,
                                "has_reviewed_answer_card": False,
                                "gaps": [
                                    "missing_reviewed_answer_card",
                                    "missing_routing_support",
                                ],
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )
            markers_path.write_text(
                json.dumps(
                    {
                        "guides": [
                            {
                                "guide_id": "GD-100",
                                "marker_counts": {"unresolved_partial": 1},
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            payload = collect_priorities(
                [diag_dir],
                metadata_audit_path=metadata_path,
                corpus_marker_scan_path=markers_path,
            )
            markdown = render_markdown(payload)

        top = payload["guides"][0]
        self.assertEqual(top["guide_id"], "GD-100")
        self.assertEqual(top["expected_rows"], 2)
        self.assertEqual(top["bad_rows"], 1)
        self.assertIn("missing_reviewed_answer_card", top["metadata_gaps"])
        self.assertIn("unresolved_partial", top["marker_types"])
        self.assertEqual(top["candidate_action"], "repair_corpus_partial")
        self.assertIn("GD-100", markdown)

    def test_prioritizer_accepts_comma_separated_guide_id_fields(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            diagnostics_path = root / "diagnostics.json"
            diagnostics_path.write_text(
                json.dumps(
                    {
                        "rows": [
                            {
                                "artifact_name": "sample.json",
                                "suspected_failure_bucket": "ranking_miss",
                                "expected_guide_ids": "GD-100, GD-101",
                                "cited_guide_ids": "GD-200",
                                "top_retrieved_guide_ids": "GD-200,GD-100",
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            payload = collect_priorities([diagnostics_path])

        guides = {record["guide_id"]: record for record in payload["guides"]}
        self.assertEqual(guides["GD-100"]["expected_rows"], 1)
        self.assertEqual(guides["GD-101"]["expected_rows"], 1)
        self.assertEqual(guides["GD-200"]["top1_rows"], 1)
        self.assertEqual(guides["GD-100"]["topk_rows"], 1)


if __name__ == "__main__":
    unittest.main()
