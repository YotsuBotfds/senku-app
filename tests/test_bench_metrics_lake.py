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
                """
                SELECT row_kind, COUNT(*)
                FROM detail_rows
                GROUP BY row_kind
                ORDER BY row_kind
                """
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

    def test_artifact_disappears_between_discovery_and_stat(self):
        root = self.make_tmpdir()
        good_path = root / "good.json"
        good_path.write_text(
            json.dumps({"summary": {"total_prompts": 1}, "results": []}),
            encoding="utf-8",
        )
        disappearing_path = root / "disappearing.json"
        disappearing_path.write_text(
            json.dumps({"summary": {"total_prompts": 2}, "results": []}),
            encoding="utf-8",
        )

        artifact_paths = self.module.iter_artifact_paths([root])
        disappearing_path.unlink()

        output = root / "lake.sqlite"
        conn = self.module.connect_database(output, "sqlite")
        self.module.initialize_schema(conn)
        summary = self.module.ingest_artifacts(
            conn,
            artifact_paths,
            base_dir=root,
            backend="sqlite",
            source_label="test",
        )
        conn.close()

        self.assertEqual(summary["artifacts"], 2)
        self.assertEqual(summary["metrics"], 1)
        self.assertEqual(summary["detail_rows"], 0)

        verify = sqlite3.connect(output)
        try:
            artifact_errors = {
                row[0]: row[1]
                for row in verify.execute(
                    "SELECT path, error FROM artifacts ORDER BY path"
                ).fetchall()
            }
            self.assertIn("good.json", artifact_errors)
            self.assertIn("disappearing.json", artifact_errors)
            self.assertIsNone(artifact_errors["good.json"])
            self.assertIsNotNone(artifact_errors["disappearing.json"])
            self.assertIn("FileNotFoundError", artifact_errors["disappearing.json"])
            metric_count = verify.execute(
                "SELECT COUNT(*) FROM metrics WHERE artifact_id IN "
                "(SELECT artifact_id FROM artifacts WHERE path = ?)",
                ("good.json",),
            ).fetchone()[0]
            self.assertEqual(metric_count, 1)
        finally:
            verify.close()

    def test_jsonl_manifest_artifact_path_evidence_adds_detail_rows(self):
        root = self.make_tmpdir()
        jsonl_path = root / "run_manifest.jsonl"
        manifest_record = {
            "record_type": "run_manifest",
            "task": "task_001",
            "lane": "lane_smoke",
            "label": "baseline",
            "commit": "abc123",
            "generated_at": "2026-04-26T10:00:00Z",
            "run_id": "run-1",
            "artifact_path_evidence": [
                {
                    "status": "present",
                    "kind": "bench_json",
                    "path": "artifacts/bench/run.json",
                },
                {
                    "status": "missing",
                    "kind": "markdown_report",
                    "path": "artifacts/bench/run.md",
                },
            ],
        }
        jsonl_path.write_text(
            json.dumps(manifest_record) + "\n"
            + json.dumps({"id": "plain-row", "status": "ok"}) + "\n",
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
        self.assertEqual(summary["detail_rows"], 4)
        verify = sqlite3.connect(output)
        try:
            row_kinds = verify.execute(
                """
                SELECT row_kind, COUNT(*)
                FROM detail_rows
                GROUP BY row_kind
                ORDER BY row_kind
                """
            ).fetchall()
            self.assertEqual(row_kinds, [("artifact_path_evidence", 2), ("jsonl", 2)])
            evidence = verify.execute(
                """
                SELECT status, raw_json
                FROM detail_rows
                WHERE row_kind = 'artifact_path_evidence'
                ORDER BY row_index
                """
            ).fetchall()
            self.assertEqual([row[0] for row in evidence], ["present", "missing"])
            evidence_json = [json.loads(row[1]) for row in evidence]
            self.assertEqual(
                evidence_json,
                [
                    {
                        "task": "task_001",
                        "lane": "lane_smoke",
                        "label": "baseline",
                        "commit": "abc123",
                        "generated_at": "2026-04-26T10:00:00Z",
                        "record_type": "run_manifest",
                        "kind": "bench_json",
                        "path": "artifacts/bench/run.json",
                        "status": "present",
                    },
                    {
                        "task": "task_001",
                        "lane": "lane_smoke",
                        "label": "baseline",
                        "commit": "abc123",
                        "generated_at": "2026-04-26T10:00:00Z",
                        "record_type": "run_manifest",
                        "kind": "markdown_report",
                        "path": "artifacts/bench/run.md",
                        "status": "missing",
                    },
                ],
            )
        finally:
            verify.close()

    def test_jsonl_artifact_path_evidence_exposes_parent_context_columns(self):
        root = self.make_tmpdir()
        jsonl_path = root / "run_manifest.jsonl"
        jsonl_path.write_text(
            json.dumps(
                {
                    "record_type": "run_manifest",
                    "task": "task_001",
                    "lane": "lane_smoke",
                    "label": "baseline",
                    "commit": "abc123",
                    "generated_at": "2026-04-26T10:00:00Z",
                    "artifact_path_evidence": [
                        {"status": "present", "path": "artifacts/bench/run.json"},
                    ],
                },
            )
            + "\n",
            encoding="utf-8",
        )
        output = root / "lake.sqlite"

        conn = self.module.connect_database(output, "sqlite")
        self.module.initialize_schema(conn)
        self.module.ingest_artifacts(
            conn,
            [jsonl_path],
            base_dir=root,
            backend="sqlite",
            source_label="test",
        )
        conn.close()

        verify = sqlite3.connect(output)
        try:
            row = verify.execute(
                """
                SELECT
                    evidence_record_type,
                    evidence_task,
                    evidence_lane,
                    evidence_label,
                    evidence_commit,
                    evidence_generated_at,
                    row_kind,
                    entity_id
                FROM detail_rows
                WHERE row_kind = 'artifact_path_evidence'
                """
            ).fetchone()
            self.assertEqual(
                row,
                (
                    "run_manifest",
                    "task_001",
                    "lane_smoke",
                    "baseline",
                    "abc123",
                    "2026-04-26T10:00:00Z",
                    "artifact_path_evidence",
                    "artifacts/bench/run.json",
                ),
            )
        finally:
            verify.close()

    def test_jsonl_artifact_path_evidence_with_malformed_and_scalar_lines(self):
        root = self.make_tmpdir()
        jsonl_path = root / "manifest.jsonl"
        jsonl_path.write_text(
            json.dumps(
                {
                    "record_type": "run_manifest",
                    "task": "task_alpha",
                    "lane": "lane_alpha",
                    "label": "label_alpha",
                    "commit": "a1b2c3",
                    "generated_at": "2026-04-26T08:00:00Z",
                    "artifact_path_evidence": [
                        {"path": "artifacts/bench/primary.json", "status": "present"}
                    ],
                }
            )
            + "\n"
            + "{not-json\n"
            + "7\n"
            + json.dumps(
                {
                    "record_type": "run_manifest",
                    "task": "task_beta",
                    "lane": "lane_beta",
                    "artifact_path_evidence": [
                        {"path": "artifacts/bench/secondary.json", "exists": False}
                    ],
                }
            )
            + "\n",
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
        self.assertEqual(summary["detail_rows"], 4)
        self.assertEqual(summary["metrics"], 1)
        verify = sqlite3.connect(output)
        try:
            artifact = verify.execute(
                "SELECT json_type, error FROM artifacts WHERE path = ?",
                ("manifest.jsonl",),
            ).fetchone()
            self.assertEqual(artifact[0], "jsonl")
            self.assertIn("line 2: JSONDecodeError", artifact[1])

            evidence = verify.execute(
                """
                SELECT row_index, status, raw_json
                FROM detail_rows
                WHERE row_kind = 'artifact_path_evidence'
                ORDER BY row_index
                """
            ).fetchall()
            evidence_rows = [
                (row_index, status, json.loads(raw_json))
                for row_index, status, raw_json in evidence
            ]
            self.assertEqual([item[0] for item in evidence_rows], [0, 1])
            self.assertEqual([item[1] for item in evidence_rows], ["present", "missing"])
            self.assertEqual(
                [item[2]["path"] for item in evidence_rows],
                ["artifacts/bench/primary.json", "artifacts/bench/secondary.json"],
            )
            self.assertEqual(
                [item[2]["task"] for item in evidence_rows],
                ["task_alpha", "task_beta"],
            )
            self.assertEqual(
                [item[2]["lane"] for item in evidence_rows],
                ["lane_alpha", "lane_beta"],
            )
            self.assertEqual(
                [item[2].get("label") for item in evidence_rows],
                ["label_alpha", None],
            )
            self.assertEqual(
                [item[2].get("record_type") for item in evidence_rows],
                ["run_manifest", "run_manifest"],
            )
            self.assertEqual(
                [item[2].get("generated_at") for item in evidence_rows],
                ["2026-04-26T08:00:00Z", None],
            )
            self.assertEqual(
                [item[2].get("commit") for item in evidence_rows],
                ["a1b2c3", None],
            )

            duplicate_indexes = verify.execute(
                """
                SELECT row_index, COUNT(*)
                FROM detail_rows
                WHERE row_kind = 'artifact_path_evidence'
                GROUP BY row_index
                HAVING COUNT(*) > 1
                """
            ).fetchall()
            self.assertEqual(duplicate_indexes, [])
        finally:
            verify.close()

    def test_initialize_schema_backfills_evidence_columns_in_legacy_detail_rows(self):
        root = self.make_tmpdir()
        output = root / "legacy_lake.sqlite"

        legacy_conn = sqlite3.connect(output)
        try:
            legacy_conn.executescript(
                """
                CREATE TABLE ingest_runs (
                    run_id TEXT PRIMARY KEY,
                    source_label TEXT,
                    ingested_at TEXT NOT NULL,
                    backend TEXT NOT NULL
                );
                CREATE TABLE artifacts (
                    artifact_id INTEGER PRIMARY KEY,
                    run_id TEXT NOT NULL,
                    path TEXT NOT NULL,
                    absolute_path TEXT NOT NULL,
                    kind TEXT NOT NULL,
                    suffix TEXT NOT NULL,
                    size_bytes INTEGER NOT NULL,
                    mtime_utc TEXT NOT NULL,
                    paired_markdown_path TEXT,
                    json_type TEXT,
                    top_level_keys_json TEXT,
                    error TEXT
                );
                CREATE TABLE metrics (
                    artifact_id INTEGER NOT NULL,
                    metric_path TEXT NOT NULL,
                    metric_value TEXT,
                    metric_number DOUBLE,
                    metric_type TEXT NOT NULL
                );
                CREATE TABLE detail_rows (
                    artifact_id INTEGER NOT NULL,
                    row_kind TEXT NOT NULL,
                    row_index INTEGER NOT NULL,
                    entity_id TEXT,
                    section TEXT,
                    lane TEXT,
                    style TEXT,
                    status TEXT,
                    question TEXT,
                    error TEXT,
                    generation_time DOUBLE,
                    prompt_tokens DOUBLE,
                    completion_tokens DOUBLE,
                    chunks_retrieved DOUBLE,
                    server TEXT,
                    model TEXT,
                    raw_json TEXT NOT NULL
                );
                CREATE TABLE markdown_reports (
                    artifact_id INTEGER NOT NULL,
                    title TEXT,
                    line_count INTEGER NOT NULL,
                    heading_count INTEGER NOT NULL
                );
                """
            )
            legacy_conn.commit()

            self.module.initialize_schema(legacy_conn)
            columns = self.module._detail_rows_columns(legacy_conn)
            self.assertIn("evidence_record_type", columns)
            self.assertIn("evidence_task", columns)
            self.assertIn("evidence_lane", columns)
            self.assertIn("evidence_label", columns)
            self.assertIn("evidence_commit", columns)
            self.assertIn("evidence_generated_at", columns)

            jsonl_path = root / "run_manifest.jsonl"
            jsonl_path.write_text(
                json.dumps(
                    {
                        "record_type": "run_manifest",
                        "task": "task_legacy",
                        "lane": "lane_smoke",
                        "label": "legacy",
                        "commit": "abc123",
                        "generated_at": "2026-04-26T10:00:00Z",
                        "artifact_path_evidence": [
                            {
                                "status": "present",
                                "path": "artifacts/bench/run.json",
                                "kind": "bench_json",
                            },
                        ],
                    },
                    ensure_ascii=False,
                )
                + "\n",
                encoding="utf-8",
            )

            summary = self.module.ingest_artifacts(
                legacy_conn,
                [jsonl_path],
                base_dir=root,
                backend="sqlite",
                source_label="test",
            )
            self.assertEqual(summary["artifacts"], 1)
            self.assertEqual(summary["detail_rows"], 2)

            row_kinds = legacy_conn.execute(
                "SELECT row_kind, COUNT(*) FROM detail_rows GROUP BY row_kind ORDER BY row_kind"
            ).fetchall()
            self.assertEqual(row_kinds, [("artifact_path_evidence", 1), ("jsonl", 1)])

            row = legacy_conn.execute(
                """
                SELECT
                    evidence_record_type,
                    evidence_task,
                    evidence_lane,
                    evidence_label,
                    evidence_commit,
                    evidence_generated_at,
                    row_kind,
                    entity_id
                FROM detail_rows
                WHERE row_kind = 'artifact_path_evidence'
                """
            ).fetchone()
            self.assertEqual(
                row,
                (
                    "run_manifest",
                    "task_legacy",
                    "lane_smoke",
                    "legacy",
                    "abc123",
                    "2026-04-26T10:00:00Z",
                    "artifact_path_evidence",
                    "artifacts/bench/run.json",
                ),
            )
        finally:
            legacy_conn.close()

    def test_jsonl_artifact_path_evidence_preserves_missing_fields(self):
        root = self.make_tmpdir()
        jsonl_path = root / "manifest.jsonl"
        jsonl_path.write_text(
            json.dumps(
                {
                    "record_type": "run_manifest",
                    "task": "task_main",
                    "lane": "lane_main",
                    "label": "label_main",
                    "commit": "deadbeef",
                    "generated_at": "2026-04-26T09:00:00Z",
                    "artifact_path_evidence": [
                        {"kind": "bench_json"},
                        {"path": "artifacts/bench/run.md", "status": "missing"},
                        {
                            "artifact_path": "artifacts/bench/legacy.json",
                            "status": "present",
                            "kind": "file",
                        },
                    ]
                }
            )
            + "\n",
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
        self.assertEqual(summary["detail_rows"], 4)
        verify = sqlite3.connect(output)
        try:
            evidence = verify.execute(
                """
                SELECT row_index, status, raw_json
                FROM detail_rows
                WHERE row_kind = 'artifact_path_evidence'
                ORDER BY row_index
                """
            ).fetchall()
            self.assertEqual([item[0] for item in evidence], [0, 1, 2])
            self.assertEqual([item[1] for item in evidence], [None, "missing", "present"])

            row_three = json.loads(evidence[2][2])
            self.assertEqual(row_three["artifact_path"], "artifacts/bench/legacy.json")
            self.assertEqual(row_three["kind"], "file")
            self.assertEqual(row_three["task"], "task_main")
            self.assertEqual(row_three["lane"], "lane_main")
            self.assertEqual(row_three["label"], "label_main")
            self.assertEqual(row_three["commit"], "deadbeef")
            self.assertEqual(row_three["generated_at"], "2026-04-26T09:00:00Z")
            self.assertEqual(row_three["record_type"], "run_manifest")
        finally:
            verify.close()

    def test_jsonl_artifact_path_evidence_normalizes_blank_status_and_path(self):
        root = self.make_tmpdir()
        jsonl_path = root / "manifest.jsonl"
        jsonl_path.write_text(
            json.dumps(
                {
                    "record_type": "run_manifest",
                    "task": "task_main",
                    "lane": "lane_main",
                    "label": "label_main",
                    "commit": "deadbeef",
                    "generated_at": "2026-04-26T09:00:00Z",
                    "artifact_path_evidence": [
                        {
                            "status": "",
                            "path": "  ",
                            "artifact_path": "artifacts/bench/run.json",
                            "exists": False,
                        }
                    ],
                }
            )
            + "\n",
            encoding="utf-8",
        )
        output = root / "lake.sqlite"
        conn = self.module.connect_database(output, "sqlite")
        self.module.initialize_schema(conn)
        self.module.ingest_artifacts(
            conn,
            [jsonl_path],
            base_dir=root,
            backend="sqlite",
            source_label="test",
        )
        conn.close()

        verify = sqlite3.connect(output)
        try:
            status, entity_id, raw_json = verify.execute(
                """
                SELECT status, entity_id, raw_json
                FROM detail_rows
                WHERE row_kind = 'artifact_path_evidence'
                """
            ).fetchone()
        finally:
            verify.close()

        payload = json.loads(raw_json)
        self.assertEqual(status, "missing")
        self.assertEqual(entity_id, "artifacts/bench/run.json")
        self.assertEqual(payload["path"], "artifacts/bench/run.json")
        self.assertEqual(payload["task"], "task_main")

    def test_jsonl_artifact_path_evidence_accepts_single_object(self):
        root = self.make_tmpdir()
        jsonl_path = root / "manifest.jsonl"
        jsonl_path.write_text(
            json.dumps(
                {
                    "record_type": "run_manifest",
                    "task": "task_single",
                    "lane": "lane_single",
                    "artifact_path_evidence": {
                        "artifact_path": "artifacts/bench/single.json",
                        "exists": True,
                    },
                }
            )
            + "\n",
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
        self.assertEqual(summary["detail_rows"], 2)

        verify = sqlite3.connect(output)
        try:
            status, entity_id, raw_json = verify.execute(
                """
                SELECT status, entity_id, raw_json
                FROM detail_rows
                WHERE row_kind = 'artifact_path_evidence'
                """
            ).fetchone()
        finally:
            verify.close()

        payload = json.loads(raw_json)
        self.assertEqual(status, "present")
        self.assertEqual(entity_id, "artifacts/bench/single.json")
        self.assertEqual(payload["path"], "artifacts/bench/single.json")
        self.assertEqual(payload["task"], "task_single")
        self.assertEqual(payload["lane"], "lane_single")

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
