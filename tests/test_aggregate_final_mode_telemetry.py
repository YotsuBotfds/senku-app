import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from scripts import aggregate_final_mode_telemetry as telemetry


class AggregateFinalModeTelemetryTests(unittest.TestCase):
    def test_escape_markdown_cell_sanitizes_row_breaking_characters(self):
        self.assertEqual(
            telemetry.escape_markdown_cell("bad|label\r\nnext\tcell"),
            r"bad\|label next cell",
        )

    def test_write_markdown_sanitizes_cells_without_changing_payload(self):
        payload = {
            "input_logcat_paths": ["log|one.txt"],
            "total_final_mode_lines_found": 1,
            "total_unique_final_mode_events": 1,
            "counts_by_final_mode": {"final|mode\nbad": 1},
            "counts_by_route": {"route\tbad": 1},
            "per_query_rows": [
                {
                    "query": "hello|there\r\nagain",
                    "final_mode": "final|mode\nbad",
                    "route": "route\tbad",
                    "total_elapsed_ms": 17,
                    "source_logcat_path": "log|one.txt",
                }
            ],
            "total_low_coverage_route_lines_found": 1,
            "low_coverage_route_rows": [
                {
                    "query": "low\tcoverage|query",
                    "mode": "mode\rbad",
                    "source_logcat_path": "log|one.txt",
                }
            ],
        }

        with tempfile.TemporaryDirectory() as tmpdir:
            output = Path(tmpdir) / "summary.md"
            telemetry.write_markdown(output, payload)
            markdown = output.read_text(encoding="utf-8")

        table_rows = [line for line in markdown.splitlines() if line.startswith("|")]
        self.assertIn(r"| final\|mode bad | 1 |", table_rows)
        self.assertIn(r"| route bad | 1 |", table_rows)
        self.assertIn(
            r"| hello\|there again | final\|mode bad | route bad | 17 | log\|one.txt |",
            table_rows,
        )
        self.assertIn(r"| low coverage\|query | mode bad | log\|one.txt |", table_rows)
        self.assertNotIn("again | final|mode\nbad", markdown)
        self.assertEqual(payload["per_query_rows"][0]["query"], "hello|there\r\nagain")

    def test_main_preserves_raw_aggregation_values_in_json(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            logcat = root / "logcat.txt"
            output_dir = root / "out"
            logcat.write_text(
                'I ask.generate final_mode=bad|mode route=route|x '
                'query="what|now\tplease" totalElapsedMs=42\n'
                'D ask.generate low_coverage_route query="low|query\ttoo" mode=mode|z\n',
                encoding="utf-8",
            )

            with patch(
                "sys.argv",
                [
                    "aggregate_final_mode_telemetry.py",
                    "--logcat-path",
                    str(logcat),
                    "--output-dir",
                    str(output_dir),
                ],
            ):
                self.assertEqual(telemetry.main(), 0)

            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8"))
            markdown = (output_dir / "summary.md").read_text(encoding="utf-8")

        self.assertEqual(summary["counts_by_final_mode"], {"bad|mode": 1})
        self.assertEqual(summary["counts_by_route"], {"route|x": 1})
        self.assertEqual(summary["per_query_rows"][0]["query"], "what|now\tplease")
        self.assertIn(r"| bad\|mode | 1 |", markdown)
        self.assertIn(r"| what\|now please | bad\|mode | route\|x | 42 |", markdown)

    def test_main_renders_count_tables_in_stable_sorted_order(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            logcat = root / "logcat.txt"
            output_dir = root / "out"
            logcat.write_text(
                "\n".join(
                    [
                        'I ask.generate final_mode=zeta route=route-b query="z" totalElapsedMs=30',
                        'I ask.generate final_mode=alpha route=route-c query="a" totalElapsedMs=10',
                        'I ask.generate final_mode=alpha route=route-a query="b" totalElapsedMs=20',
                        'I ask.generate final_mode=beta route=route-a query="c" totalElapsedMs=40',
                    ]
                )
                + "\n",
                encoding="utf-8",
            )

            with patch(
                "sys.argv",
                [
                    "aggregate_final_mode_telemetry.py",
                    "--logcat-path",
                    str(logcat),
                    "--output-dir",
                    str(output_dir),
                ],
            ):
                self.assertEqual(telemetry.main(), 0)

            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8"))
            markdown = (output_dir / "summary.md").read_text(encoding="utf-8")

        self.assertEqual(list(summary["counts_by_final_mode"].items()), [("alpha", 2), ("beta", 1), ("zeta", 1)])
        self.assertEqual(list(summary["counts_by_route"].items()), [("route-a", 2), ("route-b", 1), ("route-c", 1)])
        self.assertLess(markdown.index("| alpha | 2 |"), markdown.index("| beta | 1 |"))
        self.assertLess(markdown.index("| beta | 1 |"), markdown.index("| zeta | 1 |"))
        self.assertLess(markdown.index("| route-a | 2 |"), markdown.index("| route-b | 1 |"))
        self.assertLess(markdown.index("| route-b | 1 |"), markdown.index("| route-c | 1 |"))


if __name__ == "__main__":
    unittest.main()
