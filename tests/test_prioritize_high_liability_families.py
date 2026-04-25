import json
import tempfile
import unittest
from pathlib import Path

from scripts.prioritize_high_liability_families import (
    collect_family_priorities,
    render_markdown,
)


class PrioritizeHighLiabilityFamiliesTests(unittest.TestCase):
    def test_family_priority_starts_from_observed_diagnostics(self):
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
                                "expected_guide_family": "wound_family",
                                "expected_guide_ids": "GD-100|GD-101",
                                "cited_guide_ids": "GD-200",
                                "top_retrieved_guide_ids": "GD-200|GD-100",
                                "suspected_failure_bucket": "ranking_miss",
                                "app_acceptance_status": "needs_evidence_owner",
                                "answer_card_status": "partial",
                                "expected_owner_best_rank": 2,
                                "expected_owner_top3_share": 0.3333,
                                "expected_owner_topk_share": 0.5,
                                "top1_marker_risk": "fail",
                                "top1_has_unresolved_partial": "yes",
                                "decision_path": "rag",
                                "generated": "no",
                                "safety_critical": "true",
                            },
                            {
                                "expected_guide_family": "healthy_family",
                                "expected_guide_ids": "GD-300",
                                "cited_guide_ids": "GD-300",
                                "top_retrieved_guide_ids": "GD-300",
                                "suspected_failure_bucket": "expected_supported",
                                "app_acceptance_status": "strong_supported",
                                "answer_card_status": "pass",
                                "expected_owner_best_rank": 1,
                                "expected_owner_top3_share": 0.3333,
                                "expected_owner_topk_share": 1.0,
                                "decision_path": "rag",
                                "generated": "no",
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
                                "gaps": ["missing_routing_support"],
                                "has_reviewed_answer_card": False,
                            },
                            {
                                "guide_id": "GD-101",
                                "gaps": ["missing_citation_policy"],
                                "has_reviewed_answer_card": False,
                            },
                            {
                                "guide_id": "GD-300",
                                "gaps": [],
                                "has_reviewed_answer_card": True,
                            },
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

            payload = collect_family_priorities(
                [diag_dir],
                metadata_audit_path=metadata_path,
                corpus_marker_scan_path=markers_path,
            )
            markdown = render_markdown(payload)

        self.assertEqual(payload["families"][0]["expected_guide_family"], "wound_family")
        self.assertEqual(payload["families"][0]["candidate_action"], "repair_corpus_partial")
        self.assertEqual(payload["families"][0]["corpus_unresolved_partial_guides"], 1)
        self.assertEqual(payload["families"][0]["metadata_gap_guide_count"], 2)
        self.assertIn("wound_family", markdown)


if __name__ == "__main__":
    unittest.main()
