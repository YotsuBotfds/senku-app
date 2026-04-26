import io
import json
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path

from scripts import report_bench_watchlist


class ReportBenchWatchlistTests(unittest.TestCase):
    def test_load_rows_skips_malformed_rows_and_normalizes_counts(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            artifact = Path(tmpdir) / "bench.json"
            artifact.write_text(
                json.dumps(
                    {
                        "summary": {"total_prompts": 2},
                        "results": [
                            ["not", "a", "row"],
                            {
                                "question": "Q1",
                                "completion_tokens": "12",
                                "duplicate_citation_count": -3,
                            },
                            {
                                "question": "Q2",
                                "completion_tokens": True,
                                "duplicate_citation_count": "bad",
                            },
                        ],
                    }
                ),
                encoding="utf-8",
            )

            summary, rows = report_bench_watchlist.load_rows(artifact)

        self.assertEqual({"total_prompts": 2}, summary)
        self.assertEqual(["Q1", "Q2"], [row["question"] for row in rows])
        self.assertEqual([12, 0], [row["completion_tokens"] for row in rows])
        self.assertEqual([0, 0], [row["duplicate_citation_count"] for row in rows])

    def test_print_markdown_sanitizes_row_breaking_text(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            artifact = Path(tmpdir) / "bench.json"
            artifact.write_text(
                json.dumps(
                    {
                        "summary": {"total_prompts": 1},
                        "results": [
                            {
                                "section": "sec\rtion",
                                "question": "Question\nwith | pipe",
                                "decision_path": "rag`path",
                                "decision_detail": "detail\r\nmore",
                                "completion_tokens": 5,
                                "duplicate_citation_count": 2,
                                "retrieval_metadata_summary": "retrieval\tbits",
                            }
                        ],
                    }
                ),
                encoding="utf-8",
            )
            output = io.StringIO()

            with redirect_stdout(output):
                report_bench_watchlist.print_markdown([artifact], top_n=1)

        markdown = output.getvalue()
        self.assertIn("Question with \\| pipe", markdown)
        self.assertIn("sec tion", markdown)
        self.assertIn("retrieval bits", markdown)
        self.assertIn("`` rag`path ``", markdown)
        self.assertNotIn("Question\nwith", markdown)


if __name__ == "__main__":
    unittest.main()
