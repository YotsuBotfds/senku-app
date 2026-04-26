import json
import shutil
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

    def test_touched_text_files_uses_git_paths_and_allowlist(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            for directory in ["guides", "notes", "scripts", "other"]:
                (root / directory).mkdir()
            (root / "guides" / "damaged.md").write_text("Bad \ufffd\n", encoding="utf-8")
            (root / "guides" / "new.txt").write_text("New bad \u00c3\u00a9\n", encoding="utf-8")
            (root / "scripts" / "tool.py").write_text("# Bad \u00e2\u2020\u2019\n", encoding="utf-8")
            (root / "notes" / "skipped.md").write_text("Bad \ufffd\n", encoding="utf-8")
            (root / "other" / "skipped.md").write_text("Bad \ufffd\n", encoding="utf-8")
            (root / "guides" / "image.png").write_bytes(b"\x89PNG\r\n")

            def fake_git(args, cwd):
                self.assertEqual(cwd, root)
                if "--cached" in args:
                    return "guides/damaged.md\0other/skipped.md\0"
                if args[0] == "diff":
                    return "scripts\\tool.py\0guides/image.png\0"
                if args[0] == "ls-files":
                    return "guides/new.txt\0notes/skipped.md\0"
                raise AssertionError(f"Unexpected git args: {args}")

            files = scan_mojibake.touched_text_files(
                root=root,
                allowed_paths=["guides", "scripts"],
                git_runner=fake_git,
            )

        self.assertEqual(
            [path.relative_to(root).as_posix() for path in files],
            ["guides/damaged.md", "guides/new.txt", "scripts/tool.py"],
        )

    @unittest.skipIf(shutil.which("git") is None, "git is required for CLI touched-mode test")
    def test_cli_touched_mode_scans_tracked_and_untracked_files(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            notes = root / "notes"
            other = root / "other"
            notes.mkdir()
            other.mkdir()
            (notes / "tracked.md").write_text("# Clean\n", encoding="utf-8")

            subprocess.run(["git", "init"], cwd=root, check=True, capture_output=True, text=True)
            subprocess.run(
                ["git", "config", "user.email", "scanner@example.test"],
                cwd=root,
                check=True,
                capture_output=True,
                text=True,
            )
            subprocess.run(
                ["git", "config", "user.name", "Scanner Test"],
                cwd=root,
                check=True,
                capture_output=True,
                text=True,
            )
            subprocess.run(["git", "add", "notes/tracked.md"], cwd=root, check=True)
            subprocess.run(
                ["git", "commit", "-m", "initial"],
                cwd=root,
                check=True,
                capture_output=True,
                text=True,
            )

            (notes / "tracked.md").write_text("Bad tracked \ufffd\n", encoding="utf-8")
            (notes / "untracked.md").write_text("Bad untracked \u00c3\u00a9\n", encoding="utf-8")
            (other / "ignored.md").write_text("Bad but outside allowlist \ufffd\n", encoding="utf-8")

            report_json = root / "report.json"
            report_md = root / "report.md"
            completed = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT_PATH),
                    "--touched",
                    "--touched-paths",
                    "notes",
                    "--json-output",
                    str(report_json),
                    "--md-output",
                    str(report_md),
                    "--fail-on-findings",
                ],
                cwd=root,
                capture_output=True,
                text=True,
            )

            self.assertEqual(completed.returncode, 1, completed.stderr)
            payload = json.loads(completed.stdout)
            report = json.loads(report_json.read_text(encoding="utf-8"))

            self.assertEqual(payload["files_scanned"], 2)
            self.assertEqual(payload["gate_findings_count"], 2)
            self.assertEqual(report["mode"], "touched")
            self.assertEqual(report["touched_path_allowlist"], ["notes"])
            self.assertEqual(report["gate_findings_count"], 2)
            self.assertEqual(
                sorted({finding["path"] for finding in report["findings"]}),
                ["notes/tracked.md", "notes/untracked.md"],
            )
            self.assertTrue(report_md.exists())

            report_only = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT_PATH),
                    "--touched",
                    "--touched-paths",
                    "notes",
                    "--json-output",
                    str(root / "report_only.json"),
                    "--md-output",
                    str(root / "report_only.md"),
                    "--report-only",
                ],
                cwd=root,
                capture_output=True,
                text=True,
            )

            self.assertEqual(report_only.returncode, 0, report_only.stderr)

            allowed = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT_PATH),
                    "--touched",
                    "--touched-paths",
                    "notes",
                    "--allow-finding-paths",
                    "notes",
                    "--json-output",
                    str(root / "allowed.json"),
                    "--md-output",
                    str(root / "allowed.md"),
                    "--fail-on-findings",
                ],
                cwd=root,
                capture_output=True,
                text=True,
            )
            allowed_report = json.loads((root / "allowed.json").read_text(encoding="utf-8"))

            self.assertEqual(allowed.returncode, 0, allowed.stderr)
            self.assertEqual(allowed_report["findings_count"], 2)
            self.assertEqual(allowed_report["gate_findings_count"], 0)


if __name__ == "__main__":
    unittest.main()
