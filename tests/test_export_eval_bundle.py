import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
PYTHON = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"
EXPORTER = REPO_ROOT / "scripts" / "export_eval_bundle.py"


class ExportEvalBundleCliTests(unittest.TestCase):
    def test_cli_creates_output_parent_directories(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            artifact = root / "artifact.json"
            output = root / "nested" / "bundle" / "eval.jsonl"
            artifact.write_text(
                json.dumps(
                    {
                        "results": [
                            {
                                "index": 1,
                                "section": "Basics",
                                "question": "Ready?",
                                "response_text": "Yes.",
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            subprocess.run(
                [
                    str(PYTHON if PYTHON.exists() else sys.executable),
                    "-B",
                    str(EXPORTER),
                    str(artifact),
                    "--output",
                    str(output),
                ],
                cwd=REPO_ROOT,
                check=True,
            )

            rows = [
                json.loads(line)
                for line in output.read_text(encoding="utf-8").splitlines()
                if line.strip()
            ]

        self.assertEqual(rows[0]["question"], "Ready?")
        self.assertEqual(rows[0]["response_text"], "Yes.")

    def test_cli_rejects_directory_output(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            artifact = root / "artifact.json"
            output_dir = root / "existing"
            output_dir.mkdir()
            artifact.write_text(json.dumps({"results": []}), encoding="utf-8")

            result = subprocess.run(
                [
                    str(PYTHON if PYTHON.exists() else sys.executable),
                    "-B",
                    str(EXPORTER),
                    str(artifact),
                    "--output",
                    str(output_dir),
                ],
                cwd=REPO_ROOT,
                text=True,
                capture_output=True,
            )

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("--output must be a file path, not a directory", result.stderr)


if __name__ == "__main__":
    unittest.main()
