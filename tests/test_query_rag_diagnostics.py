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
            },
        ]

        bridge_rows = self.module.filter_rows(
            rows,
            buckets=["ranking_miss"],
            guide_ids=["GD-003"],
            top1_bridge=True,
            card_statuses=["pass"],
        )
        unresolved_rows = self.module.filter_rows(
            rows,
            guide_ids=["GD-012"],
            prompt_ids=["P2"],
            top1_unresolved_partial=True,
        )

        self.assertEqual([row["prompt_id"] for row in bridge_rows], ["P1"])
        self.assertEqual([row["prompt_id"] for row in unresolved_rows], ["P2"])

    def test_render_markdown_escapes_pipes(self):
        markdown = self.module.render_markdown(
            [
                {
                    "prompt_id": "P1",
                    "suspected_failure_bucket": "ranking_miss",
                    "expected_guide_ids": "GD-001|GD-002",
                    "short_reason": "expected | cited",
                }
            ]
        )

        self.assertIn("| prompt_id | suspected_failure_bucket |", markdown)
        self.assertIn("GD-001\\|GD-002", markdown)
        self.assertIn("expected \\| cited", markdown)

    def test_main_emits_json(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            write_diagnostics(
                root / self.module.DIAGNOSTICS_FILENAME,
                [
                    {"prompt_id": "P1", "suspected_failure_bucket": "ranking_miss"},
                    {"prompt_id": "P2", "suspected_failure_bucket": "retrieval_miss"},
                ],
            )

            output = io.StringIO()
            with redirect_stdout(output):
                self.module.main([str(root), "--bucket", "retrieval_miss", "--json"])
            payload = json.loads(output.getvalue())

        self.assertEqual(len(payload), 1)
        self.assertEqual(payload[0]["prompt_id"], "P2")


if __name__ == "__main__":
    unittest.main()
