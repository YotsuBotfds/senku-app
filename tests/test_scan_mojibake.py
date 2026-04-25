import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from scripts import scan_mojibake


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "scan_mojibake.py"


class MojibakeScanTests(unittest.TestCase):
    def test_scan_paths_reports_likely_mojibake(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides = root / "guides"
            guides.mkdir()
            damaged = guides / "damaged.md"
            damaged.write_text(
                "---\n"
                "title: Broken Icon\n"
                "icon: \u00f0\u0178\u2019\u00a7\n"
                "---\n\n"
                "A sentence with a mojibake arrow \u00e2\u2020\u2019 here.\n",
                encoding="utf-8",
            )
            clean = guides / "clean.md"
            clean.write_text("# Clean\n\nPlain ASCII content.\n", encoding="utf-8")

            report = scan_mojibake.scan_paths([guides], root=root)

        self.assertEqual(report["files_scanned"], 2)
        self.assertGreaterEqual(report["findings_count"], 2)
        kinds = {finding["kind"] for finding in report["findings"]}
        self.assertIn("emoji_mojibake", kinds)
        self.assertIn("utf8_as_cp1252_punctuation", kinds)
        self.assertTrue(
            any(finding["path"] == "guides/damaged.md" for finding in report["findings"])
        )

    def test_cli_writes_json_and_markdown_reports(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            notes = root / "notes"
            output = root / "artifacts"
            notes.mkdir()
            (notes / "note.md").write_text(
                "Bad replacement \ufffd and bad prefix \u00c3\u00a9.\n",
                encoding="utf-8",
            )

            completed = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT_PATH),
                    "--paths",
                    "notes",
                    "--output-dir",
                    str(output),
                ],
                check=True,
                capture_output=True,
                text=True,
                cwd=root,
            )
            payload = json.loads(completed.stdout)
            json_path = Path(payload["json_output"])
            md_path = Path(payload["md_output"])

            self.assertTrue(json_path.exists())
            self.assertTrue(md_path.exists())
            self.assertGreaterEqual(payload["findings_count"], 2)
            self.assertIn("# Mojibake Scan", md_path.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
