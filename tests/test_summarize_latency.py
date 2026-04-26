import json
import tempfile
import unittest
from pathlib import Path

import summarize_latency


class SummarizeLatencyTests(unittest.TestCase):
    def test_summarize_rows_aggregates_stage_percentiles(self):
        rows = [
            {
                "retrieval": 10.0,
                "rerank": 5.0,
                "prompt_build": 7.0,
                "first_token": 20.0,
                "decode": 100.0,
                "total": 130.0,
            },
            {
                "retrieval": 20.0,
                "rerank": 6.0,
                "prompt_build": 9.0,
                "first_token": 30.0,
                "decode": 150.0,
                "total": 190.0,
            },
            {
                "retrieval": 30.0,
                "rerank": 7.0,
                "prompt_build": 11.0,
                "first_token": 40.0,
                "decode": 200.0,
                "total": 250.0,
            },
        ]

        summary = summarize_latency.summarize_rows(rows)
        retrieval = next(row for row in summary if row["stage"] == "retrieval")
        decode = next(row for row in summary if row["stage"] == "decode")

        self.assertEqual(3, retrieval["count"])
        self.assertEqual(20.0, retrieval["p50_ms"])
        self.assertEqual(29.0, retrieval["p95_ms"])
        self.assertEqual(30.0, retrieval["max_ms"])
        self.assertEqual(200.0, decode["max_ms"])

    def test_iter_latency_rows_reads_nested_jsonl_answer_runs(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "answer_runs.jsonl"
            path.write_text(
                "\n".join(
                    [
                        json.dumps(
                            {
                                "answerRun": {
                                    "latencyBreakdown": {
                                        "retrievalMs": 12,
                                        "rerankMs": 3,
                                        "promptBuildMs": 5,
                                        "firstTokenMs": 22,
                                        "decodeMs": 90,
                                        "totalMs": 110,
                                    }
                                }
                            }
                        ),
                        json.dumps(
                            {
                                "latency_breakdown": {
                                    "retrieval_ms": 18,
                                    "rerank_ms": 4,
                                    "prompt_build_ms": 7,
                                    "first_token_ms": 28,
                                    "generation_ms": 120,
                                    "total_ms": 149,
                                }
                            }
                        ),
                    ]
                ),
                encoding="utf-8",
            )

            rows = list(summarize_latency.iter_latency_rows([path]))

        self.assertEqual(2, len(rows))
        self.assertEqual(12.0, rows[0]["retrieval"])
        self.assertEqual(120.0, rows[1]["decode"])

    def test_iter_latency_rows_skips_malformed_jsonl_rows(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "answer_runs.jsonl"
            path.write_text(
                "\n".join(
                    [
                        json.dumps({"latency_breakdown": {"retrieval_ms": 10, "total_ms": 50}}),
                        '{"latency_breakdown": ',
                        json.dumps({"latency_breakdown": {"retrieval_ms": 20, "total_ms": 80}}),
                    ]
                ),
                encoding="utf-8",
            )

            rows = list(summarize_latency.iter_latency_rows([path]))

        self.assertEqual(2, len(rows))
        self.assertEqual([10.0, 20.0], [row["retrieval"] for row in rows])
        self.assertEqual([50.0, 80.0], [row["total"] for row in rows])

    def test_iter_latency_rows_skips_malformed_json_file(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            bad_path = Path(tmpdir) / "bad.json"
            good_path = Path(tmpdir) / "good.json"
            bad_path.write_text('{"latency_breakdown": ', encoding="utf-8")
            good_path.write_text(
                json.dumps({"latency_breakdown": {"retrieval_ms": 15, "total_ms": 55}}),
                encoding="utf-8",
            )

            rows = list(summarize_latency.iter_latency_rows([bad_path, good_path]))

        self.assertEqual(1, len(rows))
        self.assertEqual(15.0, rows[0]["retrieval"])
        self.assertEqual(55.0, rows[0]["total"])

    def test_iter_latency_rows_ignores_bad_and_non_finite_timing_values(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "answer_runs.jsonl"
            path.write_text(
                json.dumps(
                    {
                        "latency_breakdown": {
                            "retrievalMs": "nan",
                            "retrieval_ms": 17,
                            "rerank_ms": "inf",
                            "prompt_build_ms": object(),
                            "total_ms": 60,
                        }
                    },
                    default=str,
                ),
                encoding="utf-8",
            )

            rows = list(summarize_latency.iter_latency_rows([path]))

        self.assertEqual(1, len(rows))
        self.assertEqual(17.0, rows[0]["retrieval"])
        self.assertEqual(0.0, rows[0]["rerank"])
        self.assertEqual(0.0, rows[0]["prompt_build"])
        self.assertEqual(60.0, rows[0]["total"])

    def test_render_summary_table_uses_expected_headers(self):
        summary = [
            {"stage": "retrieval", "count": 2, "p50_ms": 15.0, "p95_ms": 19.5, "max_ms": 20.0}
        ]

        rendered = summarize_latency.render_summary_table(summary)

        self.assertIn("stage", rendered)
        self.assertIn("p95_ms", rendered)
        self.assertIn("retrieval", rendered)


if __name__ == "__main__":
    unittest.main()
