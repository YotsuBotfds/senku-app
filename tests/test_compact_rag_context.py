import json
import tempfile
import unittest
from pathlib import Path

from scripts.compact_rag_context import (
    compact_rows,
    load_diagnostics,
    render_markdown,
    write_context,
)


class CompactRagContextTests(unittest.TestCase):
    def make_tmpdir(self) -> Path:
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def write_diagnostics(self, root: Path, data: dict) -> Path:
        path = root / "diagnostics.json"
        path.write_text(json.dumps(data), encoding="utf-8")
        return path

    def sample_data(self) -> dict:
        return {
            "generated_at": "2026-04-25T12:00:00",
            "summary": {
                "total_rows": 3,
                "by_bucket": {
                    "artifact_error": 1,
                    "expected_supported": 1,
                    "retrieval_miss": 1,
                },
                "expected_guide_rows": 3,
                "expected_hit_at_1": "1/3 (33.3%)",
                "expected_hit_at_3": "1/3 (33.3%)",
                "expected_hit_at_k": "2/3 (66.7%)",
                "expected_cited": "1/3 (33.3%)",
                "top1_marker_risk_counts": {"none": 2, "warn": 1},
                "top1_bridge_rows": 1,
                "top1_unresolved_partial_rows": 1,
                "app_acceptance_counts": {
                    "moderate_supported": 1,
                    "needs_evidence_owner": 2,
                },
                "evidence_owner_counts": {"expected_owner_cited": 1},
                "answer_card_counts": {"no_cards": 3},
                "evidence_nugget_counts": {"no_cards": 3},
                "claim_support_counts": {"no_cards": 2, "no_generated_answer": 1},
            },
            "rows": [
                {
                    "prompt_index": 1,
                    "prompt_id": "P-001",
                    "prompt_text": "A bad row with a long enough prompt to be capped cleanly.",
                    "suspected_failure_bucket": "artifact_error",
                    "app_acceptance_status": "needs_evidence_owner",
                    "answer_card_status": "no_cards",
                    "claim_support_status": "no_generated_answer",
                    "evidence_nugget_status": "no_cards",
                    "expected_guide_ids": "GD-001",
                    "top_retrieved_guide_ids": "GD-002|GD-003|GD-004",
                    "top1_marker_risk": "warn",
                    "top1_is_bridge": "no",
                    "top1_has_unresolved_partial": "yes",
                    "top1_marker_types": "unresolved_partial|routing_phrase",
                    "short_reason": "artifact has error metadata",
                    "error": "Prompt budget exceeded",
                },
                {
                    "prompt_index": 2,
                    "prompt_id": "P-002",
                    "prompt_text": "Supported row.",
                    "suspected_failure_bucket": "expected_supported",
                    "app_acceptance_status": "moderate_supported",
                    "answer_card_status": "no_cards",
                    "claim_support_status": "no_cards",
                    "evidence_nugget_status": "no_cards",
                    "expected_guide_ids": "GD-010",
                    "top_retrieved_guide_ids": "GD-010",
                    "top1_marker_risk": "none",
                    "top1_is_bridge": "no",
                    "top1_has_unresolved_partial": "no",
                    "short_reason": "expected guide appears supported",
                },
                {
                    "prompt_index": 3,
                    "prompt_id": "P-003",
                    "prompt_text": "Missed row.",
                    "suspected_failure_bucket": "retrieval_miss",
                    "app_acceptance_status": "needs_evidence_owner",
                    "answer_card_status": "no_cards",
                    "claim_support_status": "no_cards",
                    "evidence_nugget_status": "no_cards",
                    "expected_guide_ids": "GD-020",
                    "top_retrieved_guide_ids": "GD-021",
                    "top1_marker_risk": "none",
                    "top1_is_bridge": "yes",
                    "top1_has_unresolved_partial": "no",
                    "top1_marker_types": "bridge_frontmatter",
                    "short_reason": "expected guide not retrieved",
                },
            ],
        }

    def test_renders_summary_counts_and_only_bad_interesting_rows(self):
        data = self.sample_data()

        markdown = render_markdown(data, Path("diag"), max_rows=10, text_limit=24)

        self.assertIn("- Total rows: `3`", markdown)
        self.assertIn("`artifact_error`: 1", markdown)
        self.assertIn("`retrieval_miss`: 1", markdown)
        self.assertIn("- Marker risk counts: `none`: 2, `warn`: 1", markdown)
        self.assertIn("- Top-1 unresolved-partial rows: `1`", markdown)
        self.assertIn("- App acceptance: `moderate_supported`: 1, `needs_evidence_owner`: 2", markdown)
        self.assertIn("| 1 | P-001 | artifact_error |", markdown)
        self.assertIn("| 3 | P-003 | retrieval_miss |", markdown)
        self.assertNotIn("| 2 | P-002 | expected_supported |", markdown)
        self.assertIn("A bad row with a long...", markdown)

    def test_write_context_accepts_directory_and_adds_artifact_links(self):
        root = self.make_tmpdir()
        self.write_diagnostics(root, self.sample_data())
        (root / "diagnostics.csv").write_text("header\n", encoding="utf-8")
        (root / "report.md").write_text("# report\n", encoding="utf-8")
        output = root / "compact.md"

        markdown = write_context(root, output, max_rows=2, text_limit=80)

        self.assertEqual(output.read_text(encoding="utf-8"), markdown)
        self.assertIn("## Artifact Links", markdown)
        self.assertIn("[diagnostics.json]", markdown)
        self.assertIn("[diagnostics.csv]", markdown)
        self.assertIn("[report.md]", markdown)

    def test_real_smoke_counts_match_diagnostics_json(self):
        diag_dir = Path(
            "artifacts/bench/rag_eval_partial_router_holdouts_20260425_post_fixes_diag"
        )
        if not diag_dir.exists():
            self.skipTest(f"missing smoke diagnostics: {diag_dir}")

        data = load_diagnostics(diag_dir)
        markdown = render_markdown(data, diag_dir, max_rows=50, text_limit=80)
        summary = data["summary"]

        self.assertIn(f"- Total rows: `{summary['total_rows']}`", markdown)
        for bucket, count in summary["by_bucket"].items():
            self.assertIn(f"`{bucket}`: {count}", markdown)
        for risk, count in summary["top1_marker_risk_counts"].items():
            self.assertIn(f"`{risk}`: {count}", markdown)
        self.assertEqual(
            len(compact_rows(data["rows"], max_rows=1000, text_limit=80)),
            summary["total_rows"] - summary["by_bucket"]["expected_supported"],
        )


if __name__ == "__main__":
    unittest.main()
