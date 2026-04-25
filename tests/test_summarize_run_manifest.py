import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from scripts.summarize_run_manifest import load_manifest, render_markdown


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "summarize_run_manifest.py"


def _write_manifest(path: Path, records: list[dict], *, malformed: bool = False) -> None:
    lines = [json.dumps(record) for record in records]
    if malformed:
        lines.insert(1, "{not-json")
        lines.append(json.dumps(["not", "an", "object"]))
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


class SummarizeRunManifestTests(unittest.TestCase):
    def test_load_manifest_skips_malformed_lines(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            manifest_path = Path(tmpdir) / "manifest.jsonl"
            _write_manifest(
                manifest_path,
                [{"task": "A", "lane": "tooling"}, {"task": "B", "lane": "rag"}],
                malformed=True,
            )

            records, malformed = load_manifest(manifest_path)

        self.assertEqual(len(records), 2)
        self.assertEqual(malformed, 2)
        self.assertEqual(records[0]["task"], "A")

    def test_render_markdown_filters_and_limits_newest_first(self):
        records = [
            {
                "task": "A",
                "lane": "tooling",
                "label": "old",
                "commit": "1111111",
                "generated_at": "2026-04-25T01:00:00+00:00",
                "changed_file": ["scripts/old.py"],
                "validation": ["old ok"],
            },
            {
                "task": "A",
                "lane": "tooling",
                "label": "new",
                "commit": "2222222",
                "generated_at": "2026-04-25T02:00:00+00:00",
                "changed_file": ["scripts/new.py", "tests/test_new.py", "extra.py"],
                "validation": ["new ok"],
            },
            {
                "task": "B",
                "lane": "rag",
                "label": "other",
                "commit": "3333333",
                "generated_at": "2026-04-25T03:00:00+00:00",
            },
        ]

        markdown = render_markdown(records, task="A", lane="tooling", limit=1)

        self.assertIn("Records shown: 1", markdown)
        self.assertIn("Task filter: `A`", markdown)
        self.assertIn("new", markdown)
        self.assertIn("scripts/new.py<br>tests/test_new.py (+1)", markdown)
        self.assertNotIn("old", markdown)
        self.assertNotIn("other", markdown)

    def test_cli_writes_output_and_prints_stdout(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            manifest_path = root / "manifest.jsonl"
            output_path = root / "summary.md"
            _write_manifest(
                manifest_path,
                [
                    {
                        "task": "RAG-T",
                        "lane": "tooling",
                        "label": "manifest-summary",
                        "commit": "abc1234",
                        "generated_at": "2026-04-25T04:00:00+00:00",
                        "validation": ["tests passed"],
                    }
                ],
            )

            completed = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT_PATH),
                    "--manifest",
                    str(manifest_path),
                    "--limit",
                    "5",
                    "--lane",
                    "tooling",
                    "--output-md",
                    str(output_path),
                ],
                check=True,
                capture_output=True,
                text=True,
            )
            output_text = output_path.read_text(encoding="utf-8")

            self.assertIn("manifest-summary", completed.stdout)
            self.assertEqual(output_text, completed.stdout)


if __name__ == "__main__":
    unittest.main()
