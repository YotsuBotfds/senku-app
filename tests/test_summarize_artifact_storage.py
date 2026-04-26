import json
import tempfile
import unittest
from contextlib import redirect_stderr, redirect_stdout
from io import StringIO
from pathlib import Path
from unittest.mock import patch

from scripts.summarize_artifact_storage import main, render_text, summarize_storage


class SummarizeArtifactStorageTests(unittest.TestCase):
    def make_tmpdir(self):
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def write_bytes(self, path, size):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(b"x" * size)

    def test_summarize_storage_reports_largest_files_dirs_and_suffixes(self):
        root = self.make_tmpdir()
        self.write_bytes(root / "bench" / "run_a" / "summary.json", 10)
        self.write_bytes(root / "bench" / "run_a" / "trace.log", 7)
        self.write_bytes(root / "prompts" / "pack" / "summary.json", 5)
        self.write_bytes(root / "readme", 3)

        summary = summarize_storage(root, limit=3)

        self.assertTrue(summary["exists"])
        self.assertEqual(summary["total_bytes"], 25)
        self.assertEqual(summary["file_count"], 4)
        self.assertEqual(summary["largest_files"][0], {"path": "bench/run_a/summary.json", "bytes": 10})
        self.assertEqual(summary["largest_dirs"][0], {"path": "bench", "bytes": 17, "files": 2})
        suffixes = {row["suffix"]: row for row in summary["suffix_counts"]}
        self.assertEqual(suffixes[".json"], {"suffix": ".json", "count": 2, "bytes": 15})
        self.assertEqual(suffixes["<none>"], {"suffix": "<none>", "count": 1, "bytes": 3})
        self.assertEqual(
            summary["generated_dirs"][0],
            {"path": "bench", "bytes": 17, "files": 2, "markers": ["bench"]},
        )

    def test_summarize_storage_includes_empty_generated_dirs(self):
        root = self.make_tmpdir()
        (root / "empty_diag").mkdir()
        self.write_bytes(root / "regular" / "sample.txt", 6)

        summary = summarize_storage(root, limit=5)

        generated = {row["path"]: row for row in summary["generated_dirs"]}
        self.assertEqual(summary["dir_count"], 2)
        self.assertEqual(
            generated["empty_diag"],
            {"path": "empty_diag", "bytes": 0, "files": 0, "markers": ["_diag"]},
        )

    def test_summarize_storage_uses_path_stat_metadata_only(self):
        root = self.make_tmpdir()
        self.write_bytes(root / "bench" / "payload.json", 12)

        with patch.object(Path, "open", side_effect=AssertionError("file body read")):
            with patch.object(Path, "read_text", side_effect=AssertionError("file body read")):
                with patch.object(Path, "read_bytes", side_effect=AssertionError("file body read")):
                    summary = summarize_storage(root, limit=2)

        self.assertEqual(summary["total_bytes"], 12)
        self.assertEqual(summary["largest_files"], [{"path": "bench/payload.json", "bytes": 12}])

    def test_duplicate_basename_families_are_ranked_by_total_bytes(self):
        root = self.make_tmpdir()
        self.write_bytes(root / "a" / "shared.json", 4)
        self.write_bytes(root / "b" / "shared.json", 9)
        self.write_bytes(root / "c" / "unique.json", 20)

        summary = summarize_storage(root, limit=5)

        duplicates = summary["duplicate_basename_families"]
        self.assertEqual(len(duplicates), 1)
        self.assertEqual(duplicates[0]["basename"], "shared.json")
        self.assertEqual(duplicates[0]["count"], 2)
        self.assertEqual(duplicates[0]["bytes"], 13)
        self.assertEqual(duplicates[0]["examples"][0], {"path": "b/shared.json", "bytes": 9})

    def test_missing_root_returns_empty_summary(self):
        root = self.make_tmpdir() / "missing"

        summary = summarize_storage(root)

        self.assertFalse(summary["exists"])
        self.assertEqual(summary["total_bytes"], 0)
        self.assertEqual(summary["largest_files"], [])

    def test_summarize_storage_allows_zero_limit_for_library_use(self):
        root = self.make_tmpdir()
        self.write_bytes(root / "bench" / "summary.json", 4)

        summary = summarize_storage(root, limit=0)

        self.assertTrue(summary["exists"])
        self.assertEqual(summary["total_bytes"], 4)
        self.assertEqual(summary["largest_files"], [])
        self.assertEqual(summary["largest_dirs"], [])
        self.assertEqual(summary["suffix_counts"], [])
        self.assertEqual(summary["generated_dirs"], [])
        self.assertEqual(summary["duplicate_basename_families"], [])

    def test_render_text_includes_sections(self):
        root = self.make_tmpdir()
        self.write_bytes(root / "bench" / "summary.json", 4)

        text = render_text(summarize_storage(root, limit=2))

        self.assertIn("Artifact storage report:", text)
        self.assertIn("Largest files:", text)
        self.assertIn("Suffix counts:", text)
        self.assertIn("summary.json", text)

    def test_render_text_sanitizes_blank_counts_and_multiline_values(self):
        summary = {
            "root": "artifacts",
            "exists": True,
            "total_bytes": "",
            "file_count": 1,
            "dir_count": 1,
            "largest_files": [{"path": "bench\r\nsummary.json", "bytes": ""}],
            "largest_dirs": [],
            "suffix_counts": [{"suffix": ".json", "count": "", "bytes": None}],
            "generated_dirs": [{"path": "tmp\nreports", "bytes": "", "files": "", "markers": ["tmp\nprefix"]}],
            "duplicate_basename_families": [
                {
                    "basename": "summary\n.json",
                    "count": "",
                    "bytes": "",
                    "examples": [{"path": "bench\rsummary.json"}],
                }
            ],
        }

        text = render_text(summary)

        self.assertIn("Total: 0 B across 1 files, 1 dirs", text)
        self.assertIn("path=bench  summary.json  bytes=0 B", text)
        self.assertIn("path=tmp reports  bytes=0 B  files=  markers=tmp prefix", text)
        self.assertIn("basename=summary .json  count=  bytes=0 B  examples=bench summary.json", text)

    def test_render_text_tolerates_malformed_rows(self):
        summary = {
            "root": "artifacts",
            "exists": True,
            "total_bytes": 9,
            "file_count": 1,
            "dir_count": 1,
            "largest_files": [{"path": "bench/result.json"}, "not-a-row"],
            "largest_dirs": [{"bytes": 9, "files": 1}],
            "suffix_counts": [{"suffix": ".json", "bytes": 9}],
            "generated_dirs": [{"path": "tmp_reports", "markers": ["tmp\nprefix"]}],
            "duplicate_basename_families": [
                {"basename": "result.json", "bytes": 9, "examples": [{"path": "bench/result.json"}, "loose/path"]},
                "not-a-family",
            ],
        }

        text = render_text(summary)

        self.assertIn("path=bench/result.json  bytes=0 B", text)
        self.assertIn("path=  bytes=0 B", text)
        self.assertIn("path=  bytes=9 B  files=1", text)
        self.assertIn("suffix=.json  count=  bytes=9 B", text)
        self.assertIn("path=tmp_reports  bytes=0 B  files=  markers=tmp prefix", text)
        self.assertIn(
            "basename=result.json  count=  bytes=9 B  examples=bench/result.json, loose/path",
            text,
        )
        self.assertIn("basename=  count=  bytes=0 B  examples=", text)

    def test_main_emits_json(self):
        root = self.make_tmpdir()
        self.write_bytes(root / "bench" / "summary.json", 4)
        stdout = StringIO()

        with redirect_stdout(stdout):
            rc = main(["--root", str(root), "--limit", "1", "--json"])

        self.assertEqual(rc, 0)
        payload = json.loads(stdout.getvalue())
        self.assertEqual(payload["total_bytes"], 4)
        self.assertEqual(payload["largest_files"], [{"path": "bench/summary.json", "bytes": 4}])

    def test_main_emits_text_by_default(self):
        root = self.make_tmpdir()
        self.write_bytes(root / "bench" / "summary.json", 4)
        stdout = StringIO()

        with redirect_stdout(stdout):
            rc = main(["--root", str(root), "--limit", "1"])

        self.assertEqual(rc, 0)
        output = stdout.getvalue()
        self.assertIn("Artifact storage report:", output)
        self.assertIn("Largest files:", output)
        self.assertIn("bench/summary.json", output)

    def test_main_rejects_zero_limit(self):
        stderr = StringIO()

        with redirect_stderr(stderr):
            with self.assertRaises(SystemExit) as raised:
                main(["--limit", "0"])

        self.assertEqual(raised.exception.code, 2)
        self.assertIn("--limit must be greater than 0", stderr.getvalue())

    def test_main_rejects_negative_limit(self):
        stderr = StringIO()

        with redirect_stderr(stderr):
            with self.assertRaises(SystemExit) as raised:
                main(["--limit", "-1"])

        self.assertEqual(raised.exception.code, 2)
        self.assertIn("--limit must be greater than 0", stderr.getvalue())


if __name__ == "__main__":
    unittest.main()
