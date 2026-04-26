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
                            {
                                "expected_guide_family": "deterministic_card_family",
                                "expected_guide_ids": "GD-400",
                                "cited_guide_ids": "GD-400",
                                "top_retrieved_guide_ids": "GD-400",
                                "suspected_failure_bucket": "deterministic_pass",
                                "app_acceptance_status": "strong_supported",
                                "answer_card_status": "no_generated_answer",
                                "answer_card_ids": "deterministic_card",
                                "expected_owner_best_rank": 1,
                                "expected_owner_top3_share": 1.0,
                                "expected_owner_topk_share": 1.0,
                                "decision_path": "deterministic",
                                "generated": "no",
                            },
                            {
                                "expected_guide_family": "deterministic_without_card_family",
                                "expected_guide_ids": "GD-500",
                                "cited_guide_ids": "GD-500",
                                "top_retrieved_guide_ids": "GD-500",
                                "suspected_failure_bucket": "deterministic_pass",
                                "app_acceptance_status": "strong_supported",
                                "answer_card_status": "no_cards",
                                "expected_owner_best_rank": 1,
                                "expected_owner_top3_share": 1.0,
                                "expected_owner_topk_share": 1.0,
                                "decision_path": "deterministic",
                                "generated": "no",
                            },
                            {
                                "expected_guide_family": "rag_without_card_family",
                                "expected_guide_ids": "GD-600",
                                "cited_guide_ids": "GD-600",
                                "top_retrieved_guide_ids": "GD-600",
                                "suspected_failure_bucket": "expected_supported",
                                "app_acceptance_status": "strong_supported",
                                "answer_card_status": "no_cards",
                                "expected_owner_best_rank": 1,
                                "expected_owner_top3_share": 1.0,
                                "expected_owner_topk_share": 1.0,
                                "decision_path": "rag",
                                "generated": "yes",
                            },
                            {
                                "expected_guide_family": "uncertain_fit_family",
                                "expected_guide_ids": "GD-700",
                                "cited_guide_ids": "GD-701",
                                "top_retrieved_guide_ids": "GD-701|GD-700",
                                "suspected_failure_bucket": "abstain_or_clarify_needed",
                                "app_acceptance_status": "uncertain_fit_accepted",
                                "answer_card_status": "no_generated_answer",
                                "expected_owner_best_rank": 2,
                                "expected_owner_top3_share": 0.5,
                                "expected_owner_topk_share": 0.5,
                                "decision_path": "uncertain_fit",
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
                            {
                                "guide_id": "GD-400",
                                "gaps": [],
                                "has_reviewed_answer_card": True,
                            },
                            {
                                "guide_id": "GD-500",
                                "gaps": [],
                                "has_reviewed_answer_card": False,
                            },
                            {
                                "guide_id": "GD-600",
                                "gaps": [],
                                "has_reviewed_answer_card": False,
                            },
                            {
                                "guide_id": "GD-700",
                                "gaps": [],
                                "has_reviewed_answer_card": False,
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
        deterministic = next(
            family
            for family in payload["families"]
            if family["expected_guide_family"] == "deterministic_card_family"
        )
        self.assertEqual(deterministic["card_missing_rows"], 0)
        self.assertEqual(deterministic["card_not_evaluable_rows"], 1)
        self.assertEqual(deterministic["candidate_action"], "regression_monitor")
        deterministic_without_card = next(
            family
            for family in payload["families"]
            if family["expected_guide_family"] == "deterministic_without_card_family"
        )
        self.assertEqual(deterministic_without_card["card_missing_rows"], 0)
        self.assertEqual(deterministic_without_card["card_not_evaluable_rows"], 1)
        rag_without_card = next(
            family
            for family in payload["families"]
            if family["expected_guide_family"] == "rag_without_card_family"
        )
        self.assertEqual(rag_without_card["card_missing_rows"], 1)
        self.assertEqual(rag_without_card["candidate_action"], "expand_or_fix_answer_cards")
        uncertain_fit = next(
            family
            for family in payload["families"]
            if family["expected_guide_family"] == "uncertain_fit_family"
        )
        self.assertEqual(uncertain_fit["non_expected_owner_cited_rows"], 0)
        self.assertEqual(uncertain_fit["uncertain_fit_non_expected_owner_cited_rows"], 1)
        self.assertNotEqual(uncertain_fit["candidate_action"], "inspect_retrieval_ranking")
        self.assertIn("wound_family", markdown)

    def test_reviewed_answer_card_gaps_are_separate_from_frontmatter_metadata(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            diag_dir = root / "diag"
            diag_dir.mkdir()
            metadata_path = root / "metadata.json"
            (diag_dir / "diagnostics.json").write_text(
                json.dumps(
                    {
                        "rows": [
                            {
                                "expected_guide_family": "reviewed_card_only_family",
                                "expected_guide_ids": "GD-900",
                                "cited_guide_ids": "GD-900",
                                "top_retrieved_guide_ids": "GD-900",
                                "suspected_failure_bucket": "expected_supported",
                                "app_acceptance_status": "strong_supported",
                                "answer_card_status": "pass",
                                "decision_path": "rag",
                                "generated": "yes",
                            },
                            {
                                "expected_guide_family": "mixed_metadata_family",
                                "expected_guide_ids": "GD-901",
                                "cited_guide_ids": "GD-901",
                                "top_retrieved_guide_ids": "GD-901",
                                "suspected_failure_bucket": "expected_supported",
                                "app_acceptance_status": "strong_supported",
                                "answer_card_status": "pass",
                                "decision_path": "rag",
                                "generated": "yes",
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
                                "guide_id": "GD-900",
                                "gaps": ["missing_reviewed_answer_card"],
                                "has_reviewed_answer_card": False,
                            },
                            {
                                "guide_id": "GD-901",
                                "gaps": [
                                    "missing_reviewed_answer_card",
                                    "missing_citation_policy",
                                ],
                                "has_reviewed_answer_card": False,
                            },
                        ]
                    }
                ),
                encoding="utf-8",
            )

            payload = collect_family_priorities(
                [diag_dir],
                metadata_audit_path=metadata_path,
            )
            markdown = render_markdown(payload)

        reviewed_card_only = next(
            family
            for family in payload["families"]
            if family["expected_guide_family"] == "reviewed_card_only_family"
        )
        self.assertEqual(reviewed_card_only["metadata_gap_guide_count"], 0)
        self.assertEqual(reviewed_card_only["reviewed_answer_card_gap_guide_count"], 1)
        self.assertEqual(
            reviewed_card_only["candidate_action"], "consider_reviewed_answer_card"
        )
        mixed_metadata = next(
            family
            for family in payload["families"]
            if family["expected_guide_family"] == "mixed_metadata_family"
        )
        self.assertEqual(mixed_metadata["metadata_gap_guide_count"], 1)
        self.assertEqual(mixed_metadata["reviewed_answer_card_gap_guide_count"], 1)
        self.assertEqual(mixed_metadata["candidate_action"], "add_targeted_metadata")
        self.assertIn("frontmatter/card gaps", markdown)
        self.assertIn("0/1", markdown)
        self.assertIn("1/1", markdown)

    def test_row_status_coercion_handles_report_shape_variants(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            diag_dir = root / "diag"
            diag_dir.mkdir()
            metadata_path = root / "metadata.json"
            (diag_dir / "diagnostics.json").write_text(
                json.dumps(
                    {
                        "rows": [
                            {
                                "expected_guide_family": "coerced_family",
                                "expected_guide_ids": "GD-910",
                                "cited_guide_ids": "GD-911",
                                "top_retrieved_guide_ids": "GD-911|GD-910",
                                "suspected_failure_bucket": " Ranking_Miss ",
                                "app_acceptance_status": " Uncertain_Fit_Accepted ",
                                "answer_card_status": " No_Cards ",
                                "decision_path": " RAG ",
                                "generated": True,
                                "safety_critical": True,
                                "top1_marker_risk": " Fail ",
                                "top1_has_unresolved_partial": True,
                            }
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
                                "guide_id": "GD-910",
                                "gaps": "missing_reviewed_answer_card|missing_citation_policy",
                                "has_reviewed_answer_card": False,
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            payload = collect_family_priorities(
                [diag_dir],
                metadata_audit_path=metadata_path,
            )

        family = payload["families"][0]
        self.assertEqual(family["safety_rows"], 1)
        self.assertEqual(family["generated_rows"], 1)
        self.assertEqual(family["ranking_drift_rows"], 1)
        self.assertEqual(family["card_missing_rows"], 1)
        self.assertEqual(family["top1_marker_fail_rows"], 1)
        self.assertEqual(family["top1_unresolved_partial_rows"], 1)
        self.assertEqual(family["uncertain_fit_non_expected_owner_cited_rows"], 1)
        self.assertEqual(family["metadata_gap_guide_count"], 1)
        self.assertEqual(family["reviewed_answer_card_gap_guide_count"], 1)


if __name__ == "__main__":
    unittest.main()
