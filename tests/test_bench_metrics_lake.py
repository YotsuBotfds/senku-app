import importlib.util
import io
import json
import sqlite3
import tempfile
import unittest
from contextlib import redirect_stderr, redirect_stdout
from pathlib import Path


def load_module():
    module_path = (
        Path(__file__).resolve().parents[1] / "scripts" / "bench_metrics_lake.py"
    )
    spec = importlib.util.spec_from_file_location("bench_metrics_lake", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class BenchMetricsLakeTests(unittest.TestCase):
    def setUp(self) -> None:
        self.module = load_module()

    def make_tmpdir(self) -> Path:
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def test_ingests_bench_json_into_sqlite_views(self):
        root = self.make_tmpdir()
        artifact = root / "bench_run.json"
        report = root / "bench_run.md"
        artifact.write_text(
            json.dumps(
                {
                    "timestamp": "2026-04-25T12:00:00",
                    "config": {
                        "model": "local-model",
                        "runtime_profile": "fast",
                        "mode": "rag",
                        "rag": True,
                    },
                    "summary": {
                        "total_prompts": 2,
                        "successful_prompts": 1,
                        "errors": 1,
                        "wall_duration": 12.5,
                        "avg_generation_time": 6.25,
                    },
                    "results": [
                        {
                            "prompt_id": "P1",
                            "section": "water",
                            "question": "how do I filter water?",
                            "generation_time": 4.0,
                            "prompt_tokens": 10,
                            "completion_tokens": 20,
                            "chunks_retrieved": 3,
                            "server": "s1",
                        },
                        {
                            "prompt_id": "P2",
                            "section": "fire",
                            "error": "timeout",
                        },
                    ],
                }
            ),
            encoding="utf-8",
        )
        report.write_text("# Bench Run\n\nDetails\n", encoding="utf-8")
        output = root / "lake.sqlite"

        conn = self.module.connect_database(output, "sqlite")
        self.module.initialize_schema(conn)
        summary = self.module.ingest_artifacts(
            conn,
            [artifact],
            base_dir=root,
            backend="sqlite",
            source_label="test",
        )
        conn.close()

        self.assertEqual(summary["artifacts"], 1)
        self.assertGreaterEqual(summary["metrics"], 8)
        self.assertEqual(summary["detail_rows"], 2)

        verify = sqlite3.connect(output)
        try:
            trend = verify.execute(
                """
                SELECT path, model, runtime_profile, total_prompts, errors,
                       detail_row_count
                FROM v_trend_summary
                """
            ).fetchone()
            self.assertEqual(
                trend,
                ("bench_run.json", "local-model", "fast", 2.0, 1.0, 2),
            )
            paired = verify.execute(
                "SELECT paired_markdown_path FROM artifacts"
            ).fetchone()[0]
            self.assertEqual(paired, "bench_run.md")
        finally:
            verify.close()

    def test_ingests_jsonl_and_markdown_artifacts(self):
        root = self.make_tmpdir()
        jsonl_path = root / "rows.jsonl"
        jsonl_path.write_text(
            "\n".join(
                [
                    json.dumps({"id": "r1", "status": "pass", "model": "m1"}),
                    json.dumps({"id": "r2", "status": "fail", "error": "bad"}),
                ]
            )
            + "\n",
            encoding="utf-8",
        )
        markdown_path = root / "summary.md"
        markdown_path.write_text("# Summary\n\n## Detail\n", encoding="utf-8")
        output = root / "lake.sqlite"
        conn = self.module.connect_database(output, "sqlite")
        self.module.initialize_schema(conn)
        summary = self.module.ingest_artifacts(
            conn,
            [jsonl_path, markdown_path],
            base_dir=root,
            backend="sqlite",
            source_label="test",
        )
        conn.close()

        self.assertEqual(summary["artifacts"], 2)
        self.assertEqual(summary["detail_rows"], 2)
        self.assertEqual(summary["markdown_reports"], 1)
        verify = sqlite3.connect(output)
        try:
            row_kinds = verify.execute(
                "SELECT row_kind, COUNT(*) FROM detail_rows GROUP BY row_kind"
            ).fetchall()
            self.assertEqual(row_kinds, [("jsonl", 2)])
            markdown = verify.execute(
                "SELECT title, line_count, heading_count FROM markdown_reports"
            ).fetchone()
            self.assertEqual(markdown, ("Summary", 3, 2))
        finally:
            verify.close()

    def test_jsonl_records_parse_errors_without_dropping_valid_rows(self):
        root = self.make_tmpdir()
        jsonl_path = root / "mixed.jsonl"
        jsonl_path.write_text(
            json.dumps({"id": "r1", "status": "pass"}) + "\n"
            "{not-json\n"
            "7\n",
            encoding="utf-8",
        )
        output = root / "lake.sqlite"

        conn = self.module.connect_database(output, "sqlite")
        self.module.initialize_schema(conn)
        summary = self.module.ingest_artifacts(
            conn,
            [jsonl_path],
            base_dir=root,
            backend="sqlite",
            source_label="test",
        )
        conn.close()

        self.assertEqual(summary["artifacts"], 1)
        self.assertEqual(summary["detail_rows"], 1)
        self.assertEqual(summary["metrics"], 1)
        verify = sqlite3.connect(output)
        try:
            artifact = verify.execute(
                "SELECT json_type, error FROM artifacts WHERE path = ?",
                ("mixed.jsonl",),
            ).fetchone()
            self.assertEqual(artifact[0], "jsonl")
            self.assertIn("line 2: JSONDecodeError", artifact[1])
            metric = verify.execute(
                """
                SELECT metric_value, metric_number
                FROM metrics
                WHERE metric_path = 'jsonl.scalar_line.2'
                """
            ).fetchone()
            self.assertEqual(metric, ("7", 7.0))
        finally:
            verify.close()

    def test_main_auto_falls_back_to_sqlite_when_duckdb_is_unavailable(self):
        if self.module.resolve_backend("auto") != "sqlite":
            self.skipTest("duckdb is installed in this environment")
        root = self.make_tmpdir()
        (root / "run.json").write_text(
            json.dumps({"summary": {"total_prompts": 1}, "results": []}),
            encoding="utf-8",
        )
        output = root / "lake.db"
        stdout = io.StringIO()

        with redirect_stdout(stdout):
            rc = self.module.main(
                [
                    str(root),
                    "--output",
                    str(output),
                    "--backend",
                    "auto",
                    "--base-dir",
                    str(root),
                    "--summary",
                ]
            )

        self.assertEqual(rc, 0)
        self.assertTrue(output.exists())
        self.assertIn("DuckDB unavailable; used SQLite fallback.", stdout.getvalue())
        self.assertIn("| path | model | profile |", stdout.getvalue())

    def test_duckdb_backend_reports_clear_optional_dependency_error(self):
        if self.module.resolve_backend("auto") == "duckdb":
            self.skipTest("duckdb is installed in this environment")

        stderr = io.StringIO()
        with redirect_stderr(stderr):
            rc = self.module.main(["--backend", "duckdb", "missing.json"])

        self.assertEqual(rc, 2)
        self.assertIn("optional 'duckdb' package is not installed", stderr.getvalue())


if __name__ == "__main__":
    unittest.main()
