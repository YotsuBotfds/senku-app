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
        },
        {
            "generated": "yes",
            "app_acceptance_status": "moderate_supported",
            "answer_card_status": "partial",
            "claim_support_status": "pass",
            "evidence_nugget_status": "fail",
            "evidence_nugget_total": 1,
            "evidence_nugget_supported": 0,
        },
        {
            "generated": "no",
            "app_acceptance_status": "card_contract_gap",
            "answer_card_status": "fail",
            "claim_support_status": "no_generated_answer",
            "evidence_nugget_status": "no_evaluable_answer",
            "evidence_nugget_total": 1,
            "evidence_nugget_supported": 0,
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
            "| label | total_rows | generated | app_acceptance | answer_cards | "
            "claim_support | evidence_nuggets |",
            markdown,
        )
        self.assertIn("| trend_dir | 3 | 2/3 (66.7%) |", markdown)

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
