import io
import json
import tempfile
import unittest
from contextlib import redirect_stderr, redirect_stdout
from pathlib import Path
from unittest.mock import patch

from scripts import compare_bench_artifacts


def write_artifact(
    path: Path,
    *,
    model: str,
    error: str | None = None,
    question: str = "What now?",
) -> Path:
    path.write_text(
        json.dumps(
            {
                "config": {"model": model},
                "summary": {"errors": 1 if error else 0},
                "results": [
                    {
                        "index": 1,
                        "section": "Core Regression",
                        "question": question,
                        "response_text": "Answer",
                        "decision_path": "rag",
                        "source_mode": "cited",
                        "generation_time": 1.0,
                        "duplicate_citation_count": 0,
                        "completion_cap_hit": False,
                        "finish_reason": "stop",
                        "error": error,
                        "error_category": "runtime" if error else None,
                    }
                ],
            }
        ),
        encoding="utf-8",
    )
    return path


class CompareBenchArtifactsCliTests(unittest.TestCase):
    def test_main_prints_markdown_to_stdout_by_default(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            baseline = write_artifact(root / "baseline.json", model="baseline")
            candidate = write_artifact(
                root / "candidate.json",
                model="candidate",
                error="boom",
            )

            output = io.StringIO()
            with patch(
                "sys.argv",
                ["compare_bench_artifacts.py", str(baseline), str(candidate)],
            ), redirect_stdout(output):
                compare_bench_artifacts.main()

        markdown = output.getvalue()
        self.assertIn("# Bench Artifact Comparison", markdown)
        self.assertIn("Baseline: `baseline.json`", markdown)
        self.assertIn("Candidate: `candidate.json`", markdown)
        self.assertIn("## Error Regressions", markdown)
        self.assertIn("What now?", markdown)

    def test_main_writes_markdown_to_output_without_stdout_body(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            baseline = write_artifact(root / "baseline.json", model="baseline")
            candidate = write_artifact(root / "candidate.json", model="candidate")
            output_path = root / "comparison.md"

            output = io.StringIO()
            with patch(
                "sys.argv",
                [
                    "compare_bench_artifacts.py",
                    str(baseline),
                    str(candidate),
                    "--output",
                    str(output_path),
                ],
            ), redirect_stdout(output):
                compare_bench_artifacts.main()

            markdown = output_path.read_text(encoding="utf-8")

        self.assertEqual("", output.getvalue())
        self.assertIn("# Bench Artifact Comparison", markdown)
        self.assertIn("Baseline: `baseline.json`", markdown)
        self.assertIn("Candidate: `candidate.json`", markdown)

    def test_main_creates_parent_directories_for_output_file(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            baseline = write_artifact(root / "baseline.json", model="baseline")
            candidate = write_artifact(root / "candidate.json", model="candidate")
            output_path = root / "nested" / "reports" / "comparison.md"

            output = io.StringIO()
            with patch(
                "sys.argv",
                [
                    "compare_bench_artifacts.py",
                    str(baseline),
                    str(candidate),
                    "--output",
                    str(output_path),
                ],
            ), redirect_stdout(output):
                compare_bench_artifacts.main()

            markdown = output_path.read_text(encoding="utf-8")

        self.assertEqual("", output.getvalue())
        self.assertIn("# Bench Artifact Comparison", markdown)

    def test_main_rejects_directory_output_path(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            baseline = write_artifact(root / "baseline.json", model="baseline")
            candidate = write_artifact(root / "candidate.json", model="candidate")
            stderr = io.StringIO()

            with patch(
                "sys.argv",
                [
                    "compare_bench_artifacts.py",
                    str(baseline),
                    str(candidate),
                    "--output",
                    str(root),
                ],
            ), redirect_stderr(stderr):
                with self.assertRaises(SystemExit) as raised:
                    compare_bench_artifacts.main()

        self.assertEqual(2, raised.exception.code)
        self.assertIn("--output must be a file path, not a directory", stderr.getvalue())

    def test_main_reports_malformed_json_without_traceback(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            baseline = write_artifact(root / "baseline.json", model="baseline")
            candidate = root / "candidate.json"
            candidate.write_text('{"results": [', encoding="utf-8")
            stderr = io.StringIO()

            with patch(
                "sys.argv",
                ["compare_bench_artifacts.py", str(baseline), str(candidate)],
            ), redirect_stderr(stderr):
                with self.assertRaises(SystemExit) as raised:
                    compare_bench_artifacts.main()

        self.assertEqual(2, raised.exception.code)
        self.assertIn("failed to compare artifacts", stderr.getvalue())
        self.assertNotIn("Traceback", stderr.getvalue())

    def test_main_sanitizes_control_characters_in_rendered_markdown(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            baseline = write_artifact(
                root / "baseline.json",
                model="baseline",
                question="What \x1b[31mnow?",
            )
            candidate = write_artifact(
                root / "candidate.json",
                model="candidate",
                error="boom",
                question="What \x1b[31mnow?",
            )

            output = io.StringIO()
            with patch(
                "sys.argv",
                ["compare_bench_artifacts.py", str(baseline), str(candidate)],
            ), redirect_stdout(output):
                compare_bench_artifacts.main()

        markdown = output.getvalue()
        self.assertNotIn("\x1b", markdown)
        self.assertIn("What ?[31mnow?", markdown)


if __name__ == "__main__":
    unittest.main()
