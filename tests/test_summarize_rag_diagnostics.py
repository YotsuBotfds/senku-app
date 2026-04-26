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
        "app_acceptance_root_cause_counts": {
            "supported": 7,
            "evidence_owner": 2,
            "card_contract": 1,
            "safety_surface": 3,
            "gate_policy": 1,
        },
        "answer_card_counts": {"pass": 3, "partial": 4, "fail": 5},
        "claim_support_counts": {"pass": 10},
        "generated_shadow_card_gap_rows": 2,
        "answer_provenance_counts": {
            "generated_model": 6,
            "reviewed_card_runtime": 7,
            "deterministic_rule": 11,
        },
        "artifact_reviewed_card_runtime_answer_counts": {
            "enabled": 7,
            "disabled": 16,
            "unknown": 1,
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
        self.assertEqual(row["root_supported"], 7)
        self.assertEqual(row["root_evidence_owner"], 2)
        self.assertEqual(row["root_card_contract"], 1)
        self.assertEqual(row["root_safety_surface"], 3)
        self.assertEqual(row["root_gate_policy"], 1)
        self.assertEqual(row["card_pass"], 3)
        self.assertEqual(row["card_partial"], 4)
        self.assertEqual(row["card_fail"], 5)
        self.assertEqual(row["claim_pass"], 10)
        self.assertEqual(row["generated_shadow_card_gap_rows"], 2)
        self.assertEqual(row["generated_model"], 6)
        self.assertEqual(row["reviewed_card_runtime"], 7)
        self.assertEqual(row["reviewed_card_runtime_enabled"], 7)
        self.assertEqual(row["reviewed_card_runtime_disabled"], 16)
        self.assertEqual(row["reviewed_card_runtime_unknown"], 1)
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
            self.assertIn("reviewed_card_runtime_enabled", markdown)
            self.assertIn("reviewed_card_runtime_unknown", markdown)
            self.assertIn("root_evidence_owner", markdown)
            self.assertIn("| dir_a |  | 24 | 24 | 7 | 0.875 |", markdown)
            self.assertIn("| 5 | 2 | 1 | 7 | 2 | 1 | 3 | 1 | 3 |", markdown)
            self.assertIn("| 6 | 7 | 7 | 16 | 1 | 11 |", markdown)

            json_output = io.StringIO()
            with redirect_stdout(json_output):
                self.module.main([str(dir_a), "--json"])
            payload = json.loads(json_output.getvalue())

        self.assertEqual(len(payload), 1)
        self.assertEqual(payload[0]["label"], "dir_a")
        self.assertEqual(payload[0]["bad_buckets"], 7)

    def test_render_markdown_table_sanitizes_row_breaking_cells(self):
        markdown = self.module.render_markdown_table(
            [
                {
                    "label": "wave|one",
                    "generated_at": "2026-04-26\r\n12:00",
                    "total_rows": 1,
                }
            ],
            columns=("label", "generated_at", "total_rows"),
        )

        lines = markdown.splitlines()
        self.assertEqual(len(lines), 3)
        self.assertIn("wave\\|one", lines[2])
        self.assertIn("2026-04-26  12:00", lines[2])

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

    def test_collect_summaries_counts_root_causes_from_rows_when_summary_missing(self):
        payload = base_summary()
        payload.pop("app_acceptance_root_cause_counts")
        payload["rows"] = [
            {"app_acceptance_root_cause": "supported"},
            {"app_acceptance_root_cause": "supported"},
            {"app_acceptance_root_cause": "evidence_owner"},
            {"app_acceptance_root_cause": "card_contract"},
            {"app_acceptance_root_cause": "safety_surface"},
            {"app_acceptance_root_cause": "gate_policy"},
        ]

        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "dir_a"
            dir_a.mkdir()
            write_diagnostics(dir_a / self.module.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([dir_a])

        self.assertEqual(rows[0]["root_supported"], 2)
        self.assertEqual(rows[0]["root_evidence_owner"], 1)
        self.assertEqual(rows[0]["root_card_contract"], 1)
        self.assertEqual(rows[0]["root_safety_surface"], 1)
        self.assertEqual(rows[0]["root_gate_policy"], 1)

    def test_collect_summaries_counts_runtime_unknown_from_rows_when_summary_missing(self):
        payload = base_summary()
        payload.pop("artifact_reviewed_card_runtime_answer_counts")
        payload["rows"] = [
            {"artifact_reviewed_card_runtime_answers": "enabled"},
            {"artifact_reviewed_card_runtime_answers": "disabled"},
            {"artifact_reviewed_card_runtime_answers": "disabled"},
            {"artifact_reviewed_card_runtime_answers": "unknown"},
        ]

        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "dir_a"
            dir_a.mkdir()
            write_diagnostics(dir_a / self.module.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([dir_a])

        self.assertEqual(rows[0]["reviewed_card_runtime_enabled"], 1)
        self.assertEqual(rows[0]["reviewed_card_runtime_disabled"], 2)
        self.assertEqual(rows[0]["reviewed_card_runtime_unknown"], 1)

    def test_collect_summaries_normalizes_row_fallback_status_values(self):
        payload = base_summary()
        payload.pop("app_acceptance_counts")
        payload.pop("answer_card_counts")
        payload["generated_shadow_card_gap_rows"] = None
        payload["rows"] = [
            {
                "app_acceptance_status": " strong_supported ",
                "answer_card_status": " pass ",
                "generated_shadow_card_gap_rows": " yes ",
            },
            {
                "app_acceptance_status": "moderate_supported",
                "answer_card_status": "partial",
                "generated_shadow_card_gap_rows": "no",
            },
        ]

        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "dir_a"
            dir_a.mkdir()
            write_diagnostics(dir_a / self.module.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([dir_a])

        self.assertEqual(rows[0]["strong_supported"], 1)
        self.assertEqual(rows[0]["moderate_supported"], 1)
        self.assertEqual(rows[0]["card_pass"], 1)
        self.assertEqual(rows[0]["card_partial"], 1)
        self.assertEqual(rows[0]["generated_shadow_card_gap_rows"], 1)

    def test_collect_summaries_row_fallback_status_values_are_case_insensitive(self):
        payload = base_summary()
        payload.pop("app_acceptance_counts")
        payload.pop("answer_card_counts")
        payload["generated_shadow_card_gap_rows"] = None
        payload["rows"] = [
            {
                "app_acceptance_status": "Strong_Supported",
                "answer_card_status": "PASS",
                "generated_shadow_card_gap_rows": "YES",
            },
            {
                "app_acceptance_status": "UNCERTAIN_FIT_ACCEPTED",
                "answer_card_status": "Fail",
                "generated_shadow_card_gap_rows": "No",
            },
        ]

        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "dir_a"
            dir_a.mkdir()
            write_diagnostics(dir_a / self.module.DIAGNOSTICS_FILENAME, payload)

            rows = self.module.collect_summaries([dir_a])

        self.assertEqual(rows[0]["strong_supported"], 1)
        self.assertEqual(rows[0]["uncertain_fit_accepted"], 1)
        self.assertEqual(rows[0]["card_pass"], 1)
        self.assertEqual(rows[0]["card_fail"], 1)
        self.assertEqual(rows[0]["generated_shadow_card_gap_rows"], 1)

    def test_parse_rate_handles_multiple_formats(self):
        self.assertAlmostEqual(self.module._parse_rate("21/24 (87.5%)"), 0.875)
        self.assertAlmostEqual(self.module._parse_rate("100%"), 1.0)
        self.assertAlmostEqual(self.module._parse_rate("0.25"), 0.25)
        self.assertIsNone(self.module._parse_rate("unknown"))
        self.assertIsNone(self.module._parse_rate("n/a"))


if __name__ == "__main__":
    unittest.main()
