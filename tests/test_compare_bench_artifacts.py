import io
import json
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path
from unittest.mock import patch

from scripts import compare_bench_artifacts


def write_artifact(path: Path, *, model: str, error: str | None = None) -> Path:
    path.write_text(
        json.dumps(
            {
                "config": {"model": model},
                "summary": {"errors": 1 if error else 0},
                "results": [
                    {
                        "index": 1,
                        "section": "Core Regression",
                        "question": "What now?",
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


if __name__ == "__main__":
    unittest.main()
