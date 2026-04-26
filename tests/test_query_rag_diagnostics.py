import importlib.util
import io
import json
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path


def load_module():
    module_path = (
        Path(__file__).resolve().parents[1] / "scripts" / "query_rag_diagnostics.py"
    )
    spec = importlib.util.spec_from_file_location("query_rag_diagnostics", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def write_diagnostics(path: Path, rows: list[dict]) -> None:
    path.write_text(json.dumps({"rows": rows}), encoding="utf-8")


class QueryRagDiagnosticsTests(unittest.TestCase):
    def setUp(self) -> None:
        self.module = load_module()

    def test_load_rows_accepts_file_or_directory(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            diagnostics_path = root / self.module.DIAGNOSTICS_FILENAME
            write_diagnostics(diagnostics_path, [{"prompt_id": "P1"}])

            self.assertEqual(self.module.load_rows(root)[0]["prompt_id"], "P1")
            self.assertEqual(
                self.module.load_rows(diagnostics_path)[0]["prompt_id"], "P1"
            )

    def test_filter_rows_combines_requested_filters(self):
        rows = [
            {
                "prompt_id": "P1",
                "suspected_failure_bucket": "ranking_miss",
                "expected_guide_ids": "GD-001",
                "top_retrieved_guide_ids": "GD-002|GD-003",
                "cited_guide_ids": "GD-004",
                "top1_is_bridge": "yes",
                "top1_has_unresolved_partial": "no",
                "answer_card_status": "pass",
                "app_acceptance_status": "strong_supported",
                "app_acceptance_root_cause": "supported",
                "safety_surface_status": "not_safety_critical",
                "ui_surface_bucket": "standard",
            },
            {
                "prompt_id": "P2",
                "suspected_failure_bucket": "retrieval_miss",
                "expected_guide_ids": "GD-010",
                "top_retrieved_guide_ids": "GD-011",
                "cited_guide_ids": "GD-012",
                "top1_is_bridge": "no",
                "top1_has_unresolved_partial": "yes",
                "answer_card_status": "fail",
                "app_acceptance_status": "needs_evidence_owner",
                "app_acceptance_root_cause": "evidence_owner",
                "safety_surface_status": "emergency_first_supported",
                "ui_surface_bucket": "emergency_first",
            },
        ]

        bridge_rows = self.module.filter_rows(
            rows,
            buckets=["ranking_miss"],
            guide_ids=["GD-003"],
            top1_bridge=True,
            card_statuses=["pass"],
            app_acceptance_statuses=["strong_supported"],
        )
        unresolved_rows = self.module.filter_rows(
            rows,
            guide_ids=["GD-012"],
            prompt_ids=["P2"],
            top1_unresolved_partial=True,
            acceptance_root_causes=["evidence_owner"],
            safety_surface_statuses=["emergency_first_supported"],
            ui_surface_buckets=["emergency_first"],
        )

        self.assertEqual([row["prompt_id"] for row in bridge_rows], ["P1"])
        self.assertEqual([row["prompt_id"] for row in unresolved_rows], ["P2"])

    def test_filter_rows_combines_acceptance_and_safety_allowlists(self):
        rows = [
            {
                "prompt_id": "P1",
                "app_acceptance_status": "strong_supported",
                "safety_surface_status": "not_safety_critical",
            },
            {
                "prompt_id": "P2",
                "app_acceptance_status": "strong_supported",
                "safety_surface_status": "emergency_first_supported",
            },
            {
                "prompt_id": "P3",
                "app_acceptance_status": "strong_supported",
                "safety_surface_status": "emergency_first_missing",
            },
            {
                "prompt_id": "P4",
                "app_acceptance_status": "needs_evidence_owner",
                "safety_surface_status": "emergency_first_supported",
            },
        ]

        matches = self.module.filter_rows(
            rows,
            app_acceptance_statuses=["strong_supported"],
            safety_surface_statuses=[
                "not_safety_critical",
                "emergency_first_supported",
            ],
        )

        self.assertEqual([row["prompt_id"] for row in matches], ["P1", "P2"])

    def test_filter_rows_matches_guide_ids_from_list_fields(self):
        rows = [
            {
                "prompt_id": "P1",
                "expected_guide_ids": ["GD-001", " GD-002 "],
                "top_retrieved_guide_ids": [],
                "cited_guide_ids": [],
            },
            {
                "prompt_id": "P2",
                "expected_guide_ids": [],
                "top_retrieved_guide_ids": ["GD-010"],
                "cited_guide_ids": [" GD-011 "],
            },
        ]

        expected_match = self.module.filter_rows(rows, guide_ids=["GD-002"])
        cited_match = self.module.filter_rows(rows, guide_ids=["GD-011"])

        self.assertEqual([row["prompt_id"] for row in expected_match], ["P1"])
        self.assertEqual([row["prompt_id"] for row in cited_match], ["P2"])

    def test_filter_rows_ignores_null_guide_ids_in_list_fields(self):
        rows = [
            {
                "prompt_id": "P1",
                "expected_guide_ids": [None, " ", "GD-001"],
                "top_retrieved_guide_ids": [],
                "cited_guide_ids": [],
            }
        ]

        null_match = self.module.filter_rows(rows, guide_ids=["none"])
        valid_match = self.module.filter_rows(rows, guide_ids=["GD-001"])

        self.assertEqual(null_match, [])
        self.assertEqual([row["prompt_id"] for row in valid_match], ["P1"])

    def test_filter_rows_tolerates_filter_case_and_whitespace_variants(self):
        rows = [
            {
                "prompt_id": " P1 ",
                "suspected_failure_bucket": " Ranking_Miss ",
                "expected_guide_ids": " gd-001 | GD-002 ",
                "answer_card_status": " Pass ",
                "app_acceptance_status": " Strong_Supported ",
                "app_acceptance_root_cause": " Supported ",
                "safety_surface_status": " Not_Safety_Critical ",
                "ui_surface_bucket": " Standard ",
            },
            {
                "prompt_id": "P2",
                "suspected_failure_bucket": "retrieval_miss",
                "expected_guide_ids": "GD-010",
                "answer_card_status": "fail",
                "app_acceptance_status": "needs_evidence_owner",
                "app_acceptance_root_cause": "evidence_owner",
                "safety_surface_status": "emergency_first_supported",
                "ui_surface_bucket": "emergency_first",
            },
        ]

        matches = self.module.filter_rows(
            rows,
            buckets=[" ranking_miss "],
            guide_ids=["GD-001"],
            prompt_ids=["p1"],
            card_statuses=["pass"],
            app_acceptance_statuses=["strong_supported"],
            acceptance_root_causes=["supported"],
            safety_surface_statuses=["not_safety_critical"],
            ui_surface_buckets=["standard"],
        )

        self.assertEqual([row["prompt_id"] for row in matches], [" P1 "])

    def test_render_markdown_escapes_pipes(self):
        markdown = self.module.render_markdown(
            [
                {
                    "prompt_id": "P1",
                    "suspected_failure_bucket": "ranking_miss",
                    "expected_guide_ids": "GD-001|GD-002",
                    "app_acceptance_root_cause": "evidence_owner",
                    "safety_surface_status": "emergency_first_supported",
                    "ui_surface_bucket": "emergency_first",
                    "short_reason": "expected | cited",
                }
            ]
        )

        self.assertIn("| prompt_id | suspected_failure_bucket |", markdown)
        self.assertIn("app_acceptance_root_cause", markdown)
        self.assertIn("safety_surface_status", markdown)
        self.assertIn("ui_surface_bucket", markdown)
        self.assertIn("GD-001\\|GD-002", markdown)
        self.assertIn("evidence_owner", markdown)
        self.assertIn("emergency_first_supported", markdown)
        self.assertIn("emergency_first", markdown)
        self.assertIn("expected \\| cited", markdown)

    def test_render_markdown_collapses_cell_whitespace(self):
        markdown = self.module.render_markdown(
            [
                {
                    "prompt_id": "P1",
                    "suspected_failure_bucket": "ranking_miss",
                    "short_reason": "first line\r\n\tsecond   line | cited",
                }
            ]
        )

        self.assertIn("first line second line \\| cited", markdown)
        self.assertNotIn("\r", markdown)
        self.assertNotIn("\t", markdown)

    def test_main_emits_json(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            write_diagnostics(
                root / self.module.DIAGNOSTICS_FILENAME,
                [
                    {
                        "prompt_id": "P1",
                        "suspected_failure_bucket": "ranking_miss",
                        "app_acceptance_status": "strong_supported",
                        "app_acceptance_root_cause": "supported",
                        "safety_surface_status": "not_safety_critical",
                        "ui_surface_bucket": "standard",
                    },
                    {
                        "prompt_id": "P2",
                        "suspected_failure_bucket": "retrieval_miss",
                        "app_acceptance_status": "unsafe_or_overconfident",
                        "app_acceptance_root_cause": "gate_policy",
                        "safety_surface_status": "emergency_first_supported",
                        "ui_surface_bucket": "emergency_first",
                    },
                ],
            )

            output = io.StringIO()
            with redirect_stdout(output):
                self.module.main(
                    [
                        str(root),
                        "--bucket",
                        "retrieval_miss",
                        "--app-acceptance-status",
                        "unsafe_or_overconfident",
                        "--acceptance-root-cause",
                        "gate_policy",
                        "--safety-surface-status",
                        "emergency_first_supported",
                        "--ui-surface-bucket",
                        "emergency_first",
                        "--json",
                    ]
                )
            payload = json.loads(output.getvalue())

        self.assertEqual(len(payload), 1)
        self.assertEqual(payload[0]["prompt_id"], "P2")

    def test_main_markdown_filters_diagnostic_triage_facets(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            write_diagnostics(
                root / self.module.DIAGNOSTICS_FILENAME,
                [
                    {
                        "prompt_id": "P1",
                        "suspected_failure_bucket": "ranking_miss",
                        "app_acceptance_status": "strong_supported",
                        "app_acceptance_root_cause": "supported",
                        "safety_surface_status": "not_safety_critical",
                        "ui_surface_bucket": "standard",
                    },
                    {
                        "prompt_id": "P2",
                        "suspected_failure_bucket": "retrieval_miss",
                        "app_acceptance_status": "needs_evidence_owner",
                        "app_acceptance_root_cause": "evidence_owner",
                        "safety_surface_status": "emergency_first_supported",
                        "ui_surface_bucket": "emergency_first",
                    },
                ],
            )

            output = io.StringIO()
            with redirect_stdout(output):
                self.module.main(
                    [
                        str(root),
                        "--acceptance-root-cause",
                        "evidence_owner",
                        "--safety-surface-status",
                        "emergency_first_supported",
                        "--ui-surface-bucket",
                        "emergency_first",
                    ]
                )
            markdown = output.getvalue()

        self.assertIn("| P2 | retrieval_miss |", markdown)
        self.assertIn("needs_evidence_owner", markdown)
        self.assertIn("evidence_owner", markdown)
        self.assertIn("emergency_first_supported", markdown)
        self.assertNotIn("| P1 | ranking_miss |", markdown)


if __name__ == "__main__":
    unittest.main()
