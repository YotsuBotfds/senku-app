import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from scripts.write_run_manifest import parse_metric, parse_metric_value


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "write_run_manifest.py"


def _run_script(args: list[str], *, cwd: str | None = None) -> dict:
    completed = subprocess.run(
        [sys.executable, str(SCRIPT_PATH), *args],
        check=True,
        capture_output=True,
        text=True,
        cwd=cwd,
    )
    return json.loads(completed.stdout.strip())


class WriteRunManifestTests(unittest.TestCase):
    def test_appends_records_and_prints_latest(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            manifest_path = Path(tmpdir) / "artifacts" / "runs" / "run_manifest.jsonl"
            first_payload = _run_script(
                [
                    "--task",
                    "triage",
                    "--lane",
                    "core",
                    "--label",
                    "first-pass",
                    "--role",
                    "worker",
                    "--model",
                    "gpt-test",
                    "--command",
                    "python",
                    "--command",
                    "run.py",
                    "--input",
                    "in-a",
                    "--output",
                    "out-a",
                    "--changed-file",
                    "scripts/example.py",
                    "--validation",
                    "pytest ok",
                    "--metric",
                    "steps=3",
                    "--metric",
                    "ok=true",
                    "--metric",
                    "ratio=0.25",
                    "--metric",
                    "missing=null",
                    "--metric",
                    "mode=manual",
                    "--note",
                    "baseline",
                    "--diff-stat",
                    "1 file changed",
                    "--commit",
                    "abc123",
                    "--manifest-path",
                    str(manifest_path),
                ],
            )
            second_payload = _run_script(
                [
                    "--task",
                    "triage",
                    "--lane",
                    "core",
                    "--command",
                    "python",
                    "--input",
                    "in-b",
                    "--output",
                    "out-b",
                    "--note",
                    "append-check",
                    "--commit",
                    "def456",
                    "--manifest-path",
                    str(manifest_path),
                ],
            )

            self.assertTrue(manifest_path.parent.exists())
            lines = [
                json.loads(line)
                for line in manifest_path.read_text(encoding="utf-8").splitlines()
                if line.strip()
            ]
            self.assertEqual(len(lines), 2)
            self.assertEqual(lines[1]["commit"], "def456")

            self.assertEqual(first_payload["task"], "triage")
            self.assertEqual(first_payload["lane"], "core")
            self.assertEqual(first_payload["label"], "first-pass")
            self.assertEqual(first_payload["role"], "worker")
            self.assertEqual(first_payload["model"], "gpt-test")
            self.assertEqual(first_payload["command"], ["python", "run.py"])
            self.assertEqual(first_payload["input"], ["in-a"])
            self.assertEqual(first_payload["output"], ["out-a"])
            self.assertEqual(first_payload["changed_file"], ["scripts/example.py"])
            self.assertEqual(first_payload["validation"], ["pytest ok"])
            self.assertEqual(first_payload["note"], ["baseline"])
            self.assertEqual(first_payload["commit"], "abc123")
            self.assertEqual(first_payload["git_diff_stat"], "1 file changed")
            self.assertIsInstance(first_payload["generated_at"], str)
            self.assertTrue(first_payload["dirty"] in (False, True))
            self.assertIsInstance(first_payload["git_status_short"], (str, type(None)))
            self.assertEqual(first_payload["metric"]["steps"], 3)
            self.assertIs(first_payload["metric"]["ok"], True)
            self.assertEqual(first_payload["metric"]["ratio"], 0.25)
            self.assertIsNone(first_payload["metric"]["missing"])
            self.assertEqual(first_payload["metric"]["mode"], "manual")

            self.assertEqual(second_payload["commit"], "def456")
            self.assertEqual(second_payload["command"], ["python"])

    def test_metric_parsing(self):
        self.assertEqual(parse_metric_value("17"), 17)
        self.assertEqual(parse_metric_value("17.5"), 17.5)
        self.assertIs(parse_metric_value("true"), True)
        self.assertIs(parse_metric_value("false"), False)
        self.assertIsNone(parse_metric_value("null"))
        self.assertEqual(parse_metric_value("none"), None)
        self.assertEqual(parse_metric_value("hello"), "hello")
        self.assertEqual(parse_metric("k=9"), ("k", 9))

    def test_parent_directory_creation_for_manifest_path(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            manifest_path = Path(tmpdir) / "nested" / "one" / "deep" / "run_manifest.jsonl"
            payload = _run_script(
                [
                    "--task",
                    "parent-check",
                    "--lane",
                    "delta",
                    "--manifest-path",
                    str(manifest_path),
                    "--commit",
                    "feedface",
                ],
            )
            self.assertTrue(manifest_path.parent.exists())
            self.assertEqual(payload["commit"], "feedface")
            self.assertEqual(manifest_path.read_text(encoding="utf-8").count("\n"), 1)


if __name__ == "__main__":
    unittest.main()
