import json
import tempfile
import unittest
from pathlib import Path

from scripts.index_bench_artifacts import (
    infer_artifact_kind,
    iter_bench_artifacts,
    main,
)


class IndexBenchArtifactsTests(unittest.TestCase):
    def make_tmpdir(self):
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def test_iter_bench_artifacts_summarizes_small_json(self):
        root = self.make_tmpdir()
        artifact = root / "sample_run_20260425.json"
        artifact.write_text(
            json.dumps(
                {
                    "timestamp": "2026-04-25T12:00:00",
                    "section_filter": "Core Regression",
                    "config": {
                        "model": "local-model",
                        "runtime_profile": "fast",
                        "mode": "rag",
                        "rag": True,
                        "top_k": 8,
                    },
                    "summary": {
                        "total_prompts": 2,
                        "errors": 1,
                        "wall_duration": 12.5,
                        "ok": True,
                    },
                    "results": [{"index": 1}, {"index": 2}],
                }
            ),
            encoding="utf-8",
        )

        records = list(iter_bench_artifacts(root))

        self.assertEqual(len(records), 1)
        record = records[0]
        self.assertEqual(record["path"], "sample_run_20260425.json")
        self.assertEqual(record["kind"], "json")
        self.assertEqual(record["hints"]["run"], "sample_run_20260425")
        self.assertEqual(record["summary"]["results_count"], 2)
        self.assertEqual(record["summary"]["counts"]["total_prompts"], 2)
        self.assertEqual(record["summary"]["counts"]["errors"], 1)
        self.assertNotIn("ok", record["summary"]["counts"])
        self.assertEqual(record["summary"]["config"]["model"], "local-model")

    def test_iter_bench_artifacts_skips_large_json_logs_and_binary(self):
        root = self.make_tmpdir()
        (root / "large.json").write_text('{"results": []}', encoding="utf-8")
        (root / "run_stdout.log").write_text("log content that should not be parsed", encoding="utf-8")
        (root / "screen.png").write_bytes(b"\x89PNG\r\n")

        records = {record["path"]: record for record in iter_bench_artifacts(root, max_json_bytes=4)}

        self.assertEqual(records["large.json"]["summary"]["skipped"], "json_too_large")
        self.assertEqual(records["run_stdout.log"]["summary"]["skipped"], "log_not_read")
        self.assertEqual(records["screen.png"]["summary"]["skipped"], "binary_not_read")

    def test_iter_bench_artifacts_marks_jsonl_as_not_read(self):
        root = self.make_tmpdir()
        (root / "records.jsonl").write_text(json.dumps({"id": "r1"}) + "\n", encoding="utf-8")

        records = list(iter_bench_artifacts(root))

        self.assertEqual(len(records), 1)
        self.assertEqual(records[0]["kind"], "jsonl")
        self.assertEqual(records[0]["summary"], {"skipped": "jsonl_not_read"})

    def test_main_writes_jsonl_and_markdown(self):
        root = self.make_tmpdir()
        output_jsonl = root / "out" / "manifest.jsonl"
        output_md = root / "out" / "manifest.md"
        (root / "diag_diagnostics.json").write_text(
            json.dumps({"diagnostics": [{"id": 1}], "summary": {"failures": 1}}),
            encoding="utf-8",
        )

        rc = main(
            [
                "--bench-dir",
                str(root),
                "--output-jsonl",
                str(output_jsonl),
                "--output-md",
                str(output_md),
            ]
        )

        self.assertEqual(rc, 0)
        rows = [
            json.loads(line)
            for line in output_jsonl.read_text(encoding="utf-8").splitlines()
            if line.strip()
        ]
        self.assertEqual(rows[0]["kind"], "diagnostic_json")
        self.assertEqual(rows[0]["summary"]["diagnostics_count"], 1)
        markdown = output_md.read_text(encoding="utf-8")
        self.assertIn("# Bench Artifact Index", markdown)
        self.assertIn("diag_diagnostics.json", markdown)
        self.assertIn("diagnostics_count=1", markdown)

    def test_infer_artifact_kind_handles_common_bench_names(self):
        self.assertEqual(infer_artifact_kind(Path("run_stderr.log")), "log")
        self.assertEqual(infer_artifact_kind(Path("run_diagnostics.json")), "diagnostic_json")
        self.assertEqual(infer_artifact_kind(Path("report.md")), "markdown_report")
