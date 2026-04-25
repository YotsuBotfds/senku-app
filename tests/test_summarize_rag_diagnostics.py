import importlib.util
import json
import io
import tempfile
import unittest
from pathlib import Path
from contextlib import redirect_stdout


def load_module():
    module_path = (
        Path(__file__).resolve().parents[1] / "scripts" / "summarize_rag_diagnostics.py"
    )
    spec = importlib.util.spec_from_file_location("summarize_rag_diagnostics", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def write_diagnostics(path: Path, payload: dict) -> None:
    path.write_text(json.dumps(payload), encoding="utf-8")


def base_summary() -> dict:
    return {
        "total_rows": 24,
        "expected_guide_rows": 24,
        "by_bucket": {
            "retrieval_miss": 3,
            "ranking_miss": 2,
            "generation_miss": 1,
            "unsupported_or_truncated_answer": 0,
            "safety_contract_miss": 1,
            "artifact_error": 0,
            "deterministic_pass": 17,
        },
        "expected_hit_at_1": "21/24 (87.5%)",
        "expected_hit_at_3": "20/24 (83.3%)",
        "expected_hit_at_k": "24/24 (100.0%)",
        "expected_cited": "22/24 (91.7%)",
        "app_acceptance_counts": {
            "strong_supported": 5,
            "moderate_supported": 2,
            "uncertain_fit_accepted": 1,
        },
        "answer_card_counts": {"pass": 3, "partial": 4, "fail": 5},
        "claim_support_counts": {"pass": 10},
        "generated_shadow_card_gap_rows": 2,
        "answer_provenance_counts": {
            "generated_model": 6,
            "reviewed_card_runtime": 7,
            "deterministic_rule": 11,
        },
    }


class SummarizeRagDiagnosticsTests(unittest.TestCase):
    def setUp(self) -> None:
        self.module = load_module()

    def test_collect_summaries_parses_fields_and_rates(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "wave_a"
            dir_a.mkdir()
            write_diagnostics(dir_a / self.module.DIAGNOSTICS_FILENAME, base_summary())

            rows = self.module.collect_summaries([dir_a])

        row = rows[0]
        self.assertEqual(row["label"], "wave_a")
        self.assertEqual(row["total_rows"], 24)
        self.assertEqual(row["expected_rows"], 24)
        self.assertEqual(row["bad_buckets"], 7)
        self.assertAlmostEqual(row["hit_at_1_rate"], 0.875)
        self.assertAlmostEqual(row["hit_at_3_rate"], 20 / 24)
        self.assertAlmostEqual(row["hit_at_k_rate"], 1.0)
        self.assertAlmostEqual(row["cited_rate"], 22 / 24)
        self.assertEqual(row["strong_supported"], 5)
        self.assertEqual(row["moderate_supported"], 2)
        self.assertEqual(row["uncertain_fit_accepted"], 1)
        self.assertEqual(row["card_pass"], 3)
        self.assertEqual(row["card_partial"], 4)
        self.assertEqual(row["card_fail"], 5)
        self.assertEqual(row["claim_pass"], 10)
        self.assertEqual(row["generated_shadow_card_gap_rows"], 2)
        self.assertEqual(row["generated_model"], 6)
        self.assertEqual(row["reviewed_card_runtime"], 7)
        self.assertEqual(row["deterministic_rule"], 11)
        self.assertAlmostEqual(row["quality_score_10"], 6.55)

    def test_collect_summaries_renders_markdown_and_json(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "dir_a"
            dir_a.mkdir()
            write_diagnostics(dir_a / self.module.DIAGNOSTICS_FILENAME, base_summary())

            output_text = io.StringIO()
            with redirect_stdout(output_text):
                self.module.main([str(dir_a)])
            markdown = output_text.getvalue()

            self.assertIn(
                "| label | generated_at | total_rows | expected_rows | bad_buckets |",
                markdown,
            )
            self.assertIn("| dir_a |  | 24 | 24 | 7 | 0.875 |", markdown)

            json_output = io.StringIO()
            with redirect_stdout(json_output):
                self.module.main([str(dir_a), "--json"])
            payload = json.loads(json_output.getvalue())

        self.assertEqual(len(payload), 1)
        self.assertEqual(payload[0]["label"], "dir_a")
        self.assertEqual(payload[0]["bad_buckets"], 7)

    def test_collect_summaries_rejects_label_mismatch(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "dir_a"
            dir_a.mkdir()
            dir_b = root / "dir_b"
            dir_b.mkdir()
            write_diagnostics(
                dir_a / self.module.DIAGNOSTICS_FILENAME,
                base_summary(),
            )
            write_diagnostics(
                dir_b / self.module.DIAGNOSTICS_FILENAME,
                base_summary(),
            )

            with self.assertRaises(ValueError):
                self.module.collect_summaries([dir_a, dir_b], labels=["only_one"])

    def test_parse_rate_handles_multiple_formats(self):
        self.assertAlmostEqual(self.module._parse_rate("21/24 (87.5%)"), 0.875)
        self.assertAlmostEqual(self.module._parse_rate("100%"), 1.0)
        self.assertAlmostEqual(self.module._parse_rate("0.25"), 0.25)
        self.assertIsNone(self.module._parse_rate("unknown"))
        self.assertIsNone(self.module._parse_rate("n/a"))


if __name__ == "__main__":
    unittest.main()
