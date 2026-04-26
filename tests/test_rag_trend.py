import importlib.util
import io
import json
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path


def load_module():
    module_path = (
        Path(__file__).resolve().parents[1] / "scripts" / "rag_trend.py"
    )
    spec = importlib.util.spec_from_file_location("rag_trend", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def write_diagnostics(path: Path, payload: dict) -> None:
    path.write_text(json.dumps(payload), encoding="utf-8")


def base_payload(
    *, total_rows: int = 3, include_evidence_summary: bool = False
) -> dict:
    summary = {
        "total_rows": total_rows,
        "generation_workload": {
            "generated": 2,
            "not_generated": 1,
        },
        "app_acceptance_counts": {"strong_supported": 2, "moderate_supported": 1},
        "app_acceptance_root_cause_counts": {"supported": 2, "evidence_owner": 1},
        "safety_surface_counts": {"not_safety_critical": 2, "emergency_first_supported": 1},
        "ui_surface_counts": {"standard": 2, "emergency_first": 1},
        "answer_card_counts": {"pass": 1, "partial": 1, "fail": 1},
        "claim_support_counts": {"pass": 2, "no_generated_answer": 1},
        "app_gate_counts": {},
    }

    if include_evidence_summary:
        summary.update(
            {
                "evidence_nugget_counts": {"pass": 1, "fail": 1},
                "evidence_nugget_totals": {"supported": 2, "total": 4},
            }
        )

    rows = [
        {
            "generated": "yes",
            "app_acceptance_status": "strong_supported",
            "answer_card_status": "pass",
            "claim_support_status": "pass",
            "evidence_nugget_status": "pass",
            "evidence_nugget_total": 2,
            "evidence_nugget_supported": 1,
            "app_acceptance_root_cause": "supported",
            "safety_surface_status": "not_safety_critical",
            "ui_surface_bucket": "standard",
        },
        {
            "generated": "yes",
            "app_acceptance_status": "moderate_supported",
            "answer_card_status": "partial",
            "claim_support_status": "pass",
            "evidence_nugget_status": "fail",
            "evidence_nugget_total": 1,
            "evidence_nugget_supported": 0,
            "app_acceptance_root_cause": "supported",
            "safety_surface_status": "not_safety_critical",
            "ui_surface_bucket": "standard",
        },
        {
            "generated": "no",
            "app_acceptance_status": "card_contract_gap",
            "answer_card_status": "fail",
            "claim_support_status": "no_generated_answer",
            "evidence_nugget_status": "no_evaluable_answer",
            "evidence_nugget_total": 1,
            "evidence_nugget_supported": 0,
            "app_acceptance_root_cause": "evidence_owner",
            "safety_surface_status": "emergency_first_supported",
            "ui_surface_bucket": "emergency_first",
        },
    ]

    return {"summary": summary, "rows": rows}


class RAGTrendTests(unittest.TestCase):
    def setUp(self) -> None:
        self.module = load_module()

    def test_collect_summaries_label_mismatch(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "a"
            dir_a.mkdir()
            dir_b = root / "b"
            dir_b.mkdir()

            write_diagnostics(dir_a / self.module.base.DIAGNOSTICS_FILENAME, base_payload())
            write_diagnostics(dir_b / self.module.base.DIAGNOSTICS_FILENAME, base_payload())

            with self.assertRaises(ValueError):
                self.module.collect_summaries(
                    [dir_a, dir_b],
                    labels=["only_one"],
                )

    def test_render_markdown_contains_trend_columns(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_dir"
            run_dir.mkdir()
            write_diagnostics(
                run_dir / self.module.base.DIAGNOSTICS_FILENAME,
                base_payload(include_evidence_summary=True),
            )

            rows = self.module.collect_summaries([run_dir])
            markdown = self.module.render_markdown_table(rows)

        self.assertIn(
            "| label | total_rows | generated | app_acceptance | "
            "app_acceptance_root_causes | safety_surface | ui_surface | "
            "reviewed_uncertain_fit | answer_cards | claim_support | "
            "evidence_nuggets | top1_marker_risk | top1_bridge | "
            "top1_unresolved_partial | expected_owner_best_rank | "
            "expected_owner_top3_count | expected_owner_topk_count | "
            "expected_owner_top3_share | expected_owner_topk_share |",
            markdown,
        )
        self.assertIn("| trend_dir | 3 | 2/3 (66.7%) |", markdown)
        self.assertIn("supported:2\\|evidence_owner:1", markdown)
        self.assertIn("emergency_first_supported:1\\|not_safety_critical:2", markdown)
        self.assertIn("emergency_first:1\\|standard:2", markdown)

    def test_collect_summaries_falls_back_to_rows_for_surface_facets(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_surface_rows"
            run_dir.mkdir()
            payload = base_payload(total_rows=3)
            payload["summary"].pop("app_acceptance_root_cause_counts")
            payload["summary"].pop("safety_surface_counts")
            payload["summary"].pop("ui_surface_counts")
            write_diagnostics(run_dir / self.module.base.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([run_dir])

        self.assertEqual(rows[0]["app_acceptance_root_causes"], "supported:2|evidence_owner:1")
        self.assertEqual(
            rows[0]["safety_surface"],
            "emergency_first_supported:1|not_safety_critical:2",
        )
        self.assertEqual(rows[0]["ui_surface"], "emergency_first:1|standard:2")

    def test_collect_summaries_includes_top1_marker_overlay_from_summary(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_markers"
            run_dir.mkdir()
            payload = base_payload(total_rows=4)
            payload["summary"].update(
                {
                    "top1_marker_risk_counts": {"fail": 1, "info": 2, "none": 1},
                    "top1_bridge_rows": 1,
                    "top1_unresolved_partial_rows": 1,
                }
            )
            write_diagnostics(run_dir / self.module.base.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([run_dir])

        self.assertEqual(rows[0]["top1_marker_risk"], "fail:1|info:2|none:1")
        self.assertEqual(rows[0]["top1_bridge"], "1/4 (25.0%)")
        self.assertEqual(rows[0]["top1_unresolved_partial"], "1/4 (25.0%)")

    def test_collect_summaries_falls_back_to_rows_for_top1_marker_overlay(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_marker_rows"
            run_dir.mkdir()
            payload = base_payload(total_rows=3)
            payload["summary"].pop("top1_marker_risk_counts", None)
            payload["summary"].pop("top1_bridge_rows", None)
            payload["summary"].pop("top1_unresolved_partial_rows", None)
            payload["rows"] = [
                {
                    "top1_marker_risk": "fail",
                    "top1_is_bridge": "yes",
                    "top1_has_unresolved_partial": "no",
                },
                {
                    "top1_marker_risk": "info",
                    "top1_is_bridge": "no",
                    "top1_has_unresolved_partial": "yes",
                },
                {
                    "top1_marker_risk": "info",
                    "top1_is_bridge": "yes",
                    "top1_has_unresolved_partial": "yes",
                },
            ]
            write_diagnostics(run_dir / self.module.base.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([run_dir])

        self.assertEqual(rows[0]["top1_marker_risk"], "fail:1|info:2")
        self.assertEqual(rows[0]["top1_bridge"], "2/3 (66.7%)")
        self.assertEqual(rows[0]["top1_unresolved_partial"], "2/3 (66.7%)")

    def test_collect_summaries_includes_expected_owner_metrics_from_summary(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_owner"
            run_dir.mkdir()
            payload = base_payload(include_evidence_summary=True)
            payload["summary"].update(
                {
                    "expected_owner_rows": 2,
                    "expected_owner_best_rank": "1.50",
                    "expected_owner_top3_count": "1/2 (50.0%)",
                    "expected_owner_topk_count": "1/2 (50.0%)",
                    "expected_owner_top3_share": "0.2222",
                    "expected_owner_topk_share": "0.5000",
                }
            )
            write_diagnostics(run_dir / self.module.base.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([run_dir])

        self.assertEqual(rows[0]["expected_owner_best_rank"], "1.50")
        self.assertEqual(rows[0]["expected_owner_top3_count"], "1/2 (50.0%)")
        self.assertEqual(rows[0]["expected_owner_top3_share"], "0.2222")

    def test_collect_summaries_includes_expected_owner_metrics_from_rows_when_missing(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_owner_rows"
            run_dir.mkdir()
            payload = base_payload(total_rows=3)
            payload.pop("evidence_nugget_counts", None)
            payload.pop("evidence_nugget_totals", None)
            payload["rows"] = [
                {
                    "generated": "yes",
                    "expected_guide_ids": "GD-111",
                    "expected_owner_best_rank": 1,
                    "expected_owner_top3_count": 1,
                    "expected_owner_topk_count": 1,
                    "expected_owner_top3_share": 0.3333,
                    "expected_owner_topk_share": 1.0,
                    "evidence_nugget_status": "pass",
                    "evidence_nugget_total": 2,
                    "evidence_nugget_supported": 1,
                },
                {
                    "generated": "no",
                    "expected_guide_ids": "GD-222",
                    "expected_owner_best_rank": 2,
                    "expected_owner_top3_count": 0,
                    "expected_owner_topk_count": 0,
                    "expected_owner_top3_share": 0.0,
                    "expected_owner_topk_share": 0.0,
                    "evidence_nugget_status": "no_evaluable_answer",
                    "evidence_nugget_total": 0,
                    "evidence_nugget_supported": 0,
                },
            ]
            write_diagnostics(run_dir / self.module.base.DIAGNOSTICS_FILENAME, payload)
            rows = self.module.collect_summaries([run_dir])

        self.assertEqual(rows[0]["expected_owner_best_rank"], "1.50")
        self.assertEqual(rows[0]["expected_owner_top3_count"], "1/2 (50.0%)")
        self.assertEqual(rows[0]["expected_owner_top3_share"], "0.1667")
        self.assertEqual(rows[0]["expected_owner_topk_share"], "0.5000")

    def test_collect_summaries_recomputes_expected_owner_metrics_for_old_rows(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_owner_old_rows"
            run_dir.mkdir()
            payload = base_payload(total_rows=2)
            payload["rows"] = [
                {
                    "generated": "yes",
                    "expected_guide_ids": "GD-898|GD-301",
                    "top_retrieved_guide_ids": "GD-054|GD-898|GD-301|GD-239",
                    "evidence_nugget_status": "pass",
                    "evidence_nugget_total": 2,
                    "evidence_nugget_supported": 2,
                },
                {
                    "generated": "yes",
                    "expected_guide_ids": "GD-585|GD-589",
                    "top_retrieved_guide_ids": "GD-731|GD-732|GD-403",
                    "evidence_nugget_status": "missing",
                    "evidence_nugget_total": 1,
                    "evidence_nugget_supported": 0,
                },
            ]
            write_diagnostics(run_dir / self.module.base.DIAGNOSTICS_FILENAME, payload)
            rows = self.module.collect_summaries([run_dir])

        self.assertEqual(rows[0]["expected_owner_best_rank"], "2.00")
        self.assertEqual(rows[0]["expected_owner_top3_count"], "1/2 (50.0%)")
        self.assertEqual(rows[0]["expected_owner_topk_count"], "1/2 (50.0%)")
        self.assertEqual(rows[0]["expected_owner_top3_share"], "0.3333")
        self.assertEqual(rows[0]["expected_owner_topk_share"], "0.2857")

    def test_collect_summaries_recomputes_expected_owner_metrics_for_list_ids(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_owner_list_rows"
            run_dir.mkdir()
            payload = base_payload(total_rows=1)
            payload["rows"] = [
                {
                    "generated": "yes",
                    "expected_guide_ids": ["GD-898", "GD-301"],
                    "top_retrieved_guide_ids": [
                        "GD-054",
                        "GD-898",
                        "GD-301",
                        "GD-239",
                    ],
                    "evidence_nugget_status": "pass",
                    "evidence_nugget_total": 2,
                    "evidence_nugget_supported": 2,
                }
            ]
            write_diagnostics(run_dir / self.module.base.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([run_dir])

        self.assertEqual(rows[0]["expected_owner_best_rank"], "2.00")
        self.assertEqual(rows[0]["expected_owner_top3_count"], "1/1 (100.0%)")
        self.assertEqual(rows[0]["expected_owner_topk_count"], "1/1 (100.0%)")
        self.assertEqual(rows[0]["expected_owner_top3_share"], "0.6667")
        self.assertEqual(rows[0]["expected_owner_topk_share"], "0.5000")

    def test_collect_summaries_includes_reviewed_uncertain_fit_from_rows(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_reviewed_uncertain"
            run_dir.mkdir()
            payload = base_payload(total_rows=4)
            payload["summary"]["generation_workload"]["generated"] = 4
            payload["summary"]["generation_workload"]["not_generated"] = 0
            payload["rows"] = [
                {
                    "generated": "yes",
                    "answer_provenance": "reviewed_card_runtime",
                    "answer_mode": "uncertain_fit",
                    "app_gate_status": "",
                    "app_acceptance_status": "uncertain_fit_accepted",
                    "answer_card_status": "partial",
                    "claim_support_status": "pass",
                    "evidence_nugget_status": "pass",
                    "evidence_nugget_total": 1,
                    "evidence_nugget_supported": 1,
                },
                {
                    "generated": "yes",
                    "answer_provenance": "reviewed_card_runtime",
                    "answer_mode": "",
                    "app_gate_status": "uncertain_fit",
                    "app_acceptance_status": "uncertain_fit_accepted",
                    "answer_card_status": "partial",
                    "claim_support_status": "pass",
                    "evidence_nugget_status": "pass",
                    "evidence_nugget_total": 1,
                    "evidence_nugget_supported": 1,
                },
                {
                    "generated": "yes",
                    "answer_provenance": "reviewed_card_runtime",
                    "answer_mode": "",
                    "app_gate_status": "",
                    "app_acceptance_status": "strong_supported",
                    "answer_card_status": "partial",
                    "claim_support_status": "pass",
                    "evidence_nugget_status": "pass",
                    "evidence_nugget_total": 1,
                    "evidence_nugget_supported": 1,
                },
                {
                    "generated": "yes",
                    "answer_provenance": "generated_model",
                    "answer_mode": "confident",
                    "app_gate_status": "",
                    "app_acceptance_status": "moderate_supported",
                    "answer_card_status": "fail",
                    "claim_support_status": "no_generated_answer",
                    "evidence_nugget_status": "no_evaluable_answer",
                    "evidence_nugget_total": 0,
                    "evidence_nugget_supported": 0,
                },
            ]
            write_diagnostics(run_dir / self.module.base.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([run_dir])

        self.assertEqual(rows[0]["reviewed_uncertain_fit"], "2/4 (50.0%)")

    def test_main_json_output(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_json"
            run_dir.mkdir()
            write_diagnostics(
                run_dir / self.module.base.DIAGNOSTICS_FILENAME,
                base_payload(include_evidence_summary=True),
            )

            output = io.StringIO()
            with redirect_stdout(output):
                self.module.main([str(run_dir), "--json"])
            payload = json.loads(output.getvalue())

        self.assertEqual(len(payload), 1)
        self.assertEqual(payload[0]["label"], "trend_json")
        self.assertEqual(payload[0]["generated_rows"], 2)
        self.assertEqual(payload[0]["not_generated_rows"], 1)
        self.assertEqual(payload[0]["evidence_nugget_total"], 4)
        self.assertEqual(payload[0]["evidence_nugget_supported"], 2)

    def test_evidence_nugget_coverage_falls_back_to_row_diagnostics(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            run_dir = root / "trend_fallback"
            run_dir.mkdir()
            payload = base_payload(total_rows=2)
            payload["summary"].pop("evidence_nugget_counts", None)
            payload["summary"].pop("evidence_nugget_totals", None)
            payload["rows"] = [
                {
                    "generated": "yes",
                    "app_acceptance_status": "strong_supported",
                    "answer_card_status": "pass",
                    "claim_support_status": "pass",
                    "evidence_nugget_status": "pass",
                    "evidence_nugget_total": 3,
                    "evidence_nugget_supported": 2,
                },
                {
                    "generated": "no",
                    "app_acceptance_status": "needs_evidence_owner",
                    "answer_card_status": "no_generated_answer",
                    "claim_support_status": "no_generated_answer",
                    "evidence_nugget_status": "no_evaluable_answer",
                    "evidence_nugget_total": 1,
                    "evidence_nugget_supported": 1,
                },
            ]
            write_diagnostics(run_dir / self.module.base.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([run_dir])

        summary = rows[0]
        self.assertEqual(summary["evidence_nugget_total"], 4)
        self.assertEqual(summary["evidence_nugget_supported"], 3)
        self.assertIn("3/4", summary["evidence_nuggets"])

    def test_missing_evidence_nuggets_are_marked_unavailable(self):
        summary = self.module._compact_evidence_summary({}, 0, 0, None)

        self.assertEqual(summary, "unavailable | none")


if __name__ == "__main__":
    unittest.main()
