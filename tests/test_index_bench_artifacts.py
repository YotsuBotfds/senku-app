import json
import tempfile
import unittest
from unittest import mock
from pathlib import Path

from scripts.index_bench_artifacts import (
    _format_summary,
    infer_artifact_kind,
    iter_bench_artifacts,
    main,
    write_markdown,
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

    def test_iter_bench_artifacts_records_stat_errors_without_aborting(self):
        root = self.make_tmpdir()
        (root / "broken.json").write_text("{}", encoding="utf-8")
        (root / "ok.json").write_text('{"results": []}', encoding="utf-8")

        def fake_stat(path):
            if path.name == "broken.json":
                raise OSError("stat failed")
            return path.stat()

        with mock.patch("scripts.index_bench_artifacts._stat_path", side_effect=fake_stat):
            records = {record["path"]: record for record in iter_bench_artifacts(root)}

        self.assertEqual(records["broken.json"]["kind"], "json")
        self.assertEqual(records["broken.json"]["size"], 0)
        self.assertEqual(records["broken.json"]["mtime"], "")
        self.assertEqual(
            records["broken.json"]["summary"],
            {"skipped": "stat_unreadable", "error": "OSError"},
        )
        self.assertEqual(records["ok.json"]["summary"]["results_count"], 0)

    def test_iter_bench_artifacts_summarizes_small_jsonl(self):
        root = self.make_tmpdir()
        (root / "records.jsonl").write_text(
            "\n".join(
                [
                    json.dumps(
                        {
                            "record_type": "result",
                            "report_type": "guide_validation",
                            "prompt": "sensitive prompt text",
                            "body": "large raw content",
                        }
                    ),
                    json.dumps({"record_type": "result", "report_type": "guide_validation"}),
                    json.dumps({"record_type": "diagnostic", "report_type": "routing"}),
                    json.dumps(["not", "an", "object"]),
                    "{not-json",
                    "",
                ]
            )
            + "\n",
            encoding="utf-8",
        )

        records = list(iter_bench_artifacts(root))

        self.assertEqual(len(records), 1)
        summary = records[0]["summary"]
        self.assertEqual(records[0]["kind"], "jsonl")
        self.assertEqual(summary["line_count"], 6)
        self.assertEqual(summary["object_count"], 3)
        self.assertEqual(summary["malformed_lines"], 2)
        self.assertEqual(summary["record_type_counts"], {"result": 2, "diagnostic": 1})
        self.assertEqual(summary["report_type_counts"], {"guide_validation": 2, "routing": 1})
        self.assertNotIn("prompt", summary)
        self.assertNotIn("body", summary)
        self.assertNotIn("sensitive prompt text", json.dumps(summary))

    def test_iter_bench_artifacts_counts_non_object_lines_and_truncates_long_type_values(self):
        root = self.make_tmpdir()
        long_type = "result-" + ("x" * 90)
        (root / "records.jsonl").write_text(
            "\n".join(
                [
                    json.dumps({"record_type": "result", "report_type": "guide_validation"}),
                    json.dumps({"record_type": long_type, "report_type": "guide_validation"}),
                    json.dumps({"record_type": "diagnostic", "report_type": "routing"}),
                    '"not-an-object"',
                    "",
                    "{bad",
                ]
            )
            + "\n",
            encoding="utf-8",
        )

        summary = list(iter_bench_artifacts(root))[0]["summary"]

        self.assertEqual(summary["line_count"], 6)
        self.assertEqual(summary["object_count"], 3)
        self.assertEqual(summary["non_object_lines"], 1)
        self.assertEqual(summary["malformed_lines"], 2)
        self.assertEqual(summary["record_type_counts"]["result"], 1)
        truncated = f"{long_type[:77]}..."
        self.assertEqual(summary["record_type_counts"][truncated], 1)
        self.assertNotIn(long_type, summary["record_type_counts"])

    def test_iter_bench_artifacts_sanitizes_jsonl_type_labels(self):
        root = self.make_tmpdir()
        (root / "records.jsonl").write_text(
            "\n".join(
                [
                    json.dumps({"record_type": " result\nretry\t ", "report_type": "\r routing\n"}),
                    json.dumps({"record_type": "\t\n", "report_type": "routing"}),
                ]
            )
            + "\n",
            encoding="utf-8",
        )

        summary = list(iter_bench_artifacts(root))[0]["summary"]
        formatted = _format_summary(summary)

        self.assertEqual(summary["record_type_counts"], {"result retry": 1})
        self.assertEqual(summary["report_type_counts"], {"routing": 2})
        self.assertNotIn("\n", json.dumps(summary))
        self.assertIn("record_type_counts=result retry:1", formatted)
        self.assertIn("report_type_counts=routing:2", formatted)

    def test_iter_bench_artifacts_skips_large_jsonl(self):
        root = self.make_tmpdir()
        (root / "large.jsonl").write_text(json.dumps({"record_type": "result"}) + "\n", encoding="utf-8")

        records = list(iter_bench_artifacts(root, max_json_bytes=4))

        self.assertEqual(len(records), 1)
        self.assertEqual(records[0]["kind"], "jsonl")
        self.assertEqual(records[0]["summary"], {"skipped": "jsonl_too_large"})

    def test_iter_bench_artifacts_summarizes_markdown_report(self):
        root = self.make_tmpdir()
        (root / "wave_report.md").write_text(
            "# Wave Report\n\nIntro text\n\n## Results\n- ok\n",
            encoding="utf-8",
        )

        records = list(iter_bench_artifacts(root))

        self.assertEqual(len(records), 1)
        record = records[0]
        self.assertEqual(record["kind"], "markdown_report")
        self.assertEqual(record["summary"]["title"], "Wave Report")
        self.assertEqual(record["summary"]["line_count"], 6)
        self.assertEqual(record["summary"]["heading_count"], 2)

    def test_markdown_summary_formatting_and_rendering_exposes_metadata(self):
        root = self.make_tmpdir()
        output_md = root / "manifest.md"
        summary = {
            "title": "Wave Report",
            "line_count": 6,
            "heading_count": 2,
        }

        formatted = _format_summary(summary)
        write_markdown(
            [
                {
                    "path": "wave_report.md",
                    "kind": "markdown_report",
                    "size": 42,
                    "mtime": "2026-04-25T12:00:00+00:00",
                    "summary": summary,
                }
            ],
            output_md,
        )

        self.assertIn("title=Wave Report", formatted)
        self.assertIn("line_count=6", formatted)
        self.assertIn("heading_count=2", formatted)
        markdown = output_md.read_text(encoding="utf-8")
        self.assertIn("wave_report.md", markdown)
        self.assertIn("title=Wave Report; line_count=6; heading_count=2", markdown)

    def test_markdown_output_escapes_carriage_returns(self):
        root = self.make_tmpdir()
        output_md = root / "manifest.md"

        write_markdown(
            [
                {
                    "path": "wave_report.md",
                    "kind": "markdown_report",
                    "size": 42,
                    "mtime": "2026-04-25T12:00:00+00:00",
                    "summary": {
                        "title": "Wave\rReport",
                        "line_count": 6,
                    },
                }
            ],
            output_md,
        )

        markdown = output_md.read_text(encoding="utf-8")
        self.assertNotIn("\r", markdown)
        self.assertIn("title=Wave Report; line_count=6", markdown)
        self.assertEqual(len([line for line in markdown.splitlines() if "wave_report.md" in line]), 1)

    def test_jsonl_summary_formatting_exposes_counts(self):
        summary = {
            "line_count": 4,
            "object_count": 3,
            "malformed_lines": 1,
            "record_type_counts": {"diagnostic": 1, "result": 2},
            "report_type_counts": {"routing": 1},
        }

        formatted = _format_summary(summary)

        self.assertIn("line_count=4", formatted)
        self.assertIn("object_count=3", formatted)
        self.assertIn("malformed_lines=1", formatted)
        self.assertIn("record_type_counts=diagnostic:1, result:2", formatted)
        self.assertIn("report_type_counts=routing:1", formatted)

    def test_jsonl_summary_formatting_preserves_count_order(self):
        root = self.make_tmpdir()
        path = root / "order.jsonl"
        path.write_text(
            "\n".join(
                [
                    json.dumps({"record_type": "beta"}),
                    json.dumps({"record_type": "alpha"}),
                    json.dumps({"record_type": "beta"}),
                    json.dumps({"record_type": "alpha"}),
                    json.dumps({"record_type": "gamma"}),
                    json.dumps({"record_type": "beta"}),
                    "bad json",
                ]
            )
            + "\n",
            encoding="utf-8",
        )

        summary = list(iter_bench_artifacts(root))[0]["summary"]
        formatted = _format_summary(summary)

        self.assertIn("record_type_counts=beta:3, alpha:2, gamma:1", formatted)
        self.assertIn("malformed_lines=1", formatted)

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
