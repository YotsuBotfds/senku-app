import json
import subprocess
import sys
import tempfile
import unittest
from argparse import Namespace
from pathlib import Path

from scripts.write_run_manifest import (
    CommandResult,
    build_record,
    collect_artifact_paths,
    parse_git_status_short,
    parse_metric,
    parse_metric_value,
)


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


def _args(**overrides) -> Namespace:
    values = {
        "task": "task-a",
        "lane": "tooling",
        "role": None,
        "model": None,
        "label": None,
        "command": [],
        "input": [],
        "output": [],
        "changed_file": [],
        "validation": [],
        "metric": {},
        "note": [],
        "commit": None,
        "diff_stat": None,
        "changed_file_limit": 40,
        "status_limit": 80,
        "artifact_path_limit": 20,
    }
    values.update(overrides)
    return Namespace(**values)


def _fake_git_runner(status_text: str = ""):
    def runner(args):
        if args[:2] == ["status", "--short"]:
            return CommandResult(stdout=status_text)
        if args[:2] == ["branch", "--show-current"]:
            return CommandResult(stdout="main\n")
        if args[:3] == ["rev-parse", "--short", "HEAD"]:
            return CommandResult(stdout="abc1234\n")
        if args[:2] == ["rev-parse", "HEAD"]:
            return CommandResult(stdout="abc1234def5678\n")
        if args[:2] == ["diff", "--stat"]:
            return CommandResult(stdout=" scripts/write_run_manifest.py | 8 ++++++++\n")
        return CommandResult(stdout="")

    return runner


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

    def test_parse_git_status_short_counts_and_caps(self):
        summary = parse_git_status_short(
            " M scripts/write_run_manifest.py\n"
            "A  tests/test_write_run_manifest.py\n"
            "?? artifacts/bench/new-run/report.md\n"
            "R  old.py -> scripts/new.py\n",
            entry_limit=2,
            changed_file_limit=3,
        )

        self.assertFalse(summary["clean"])
        self.assertEqual(summary["total_changed"], 4)
        self.assertEqual(summary["tracked_changed"], 3)
        self.assertEqual(summary["untracked_changed"], 1)
        self.assertEqual(summary["staged_changed"], 2)
        self.assertEqual(summary["unstaged_changed"], 1)
        self.assertEqual(len(summary["entries"]), 2)
        self.assertEqual(
            summary["changed_files"],
            [
                "scripts/write_run_manifest.py",
                "tests/test_write_run_manifest.py",
                "artifacts/bench/new-run/report.md",
            ],
        )
        self.assertTrue(summary["truncated"])
        self.assertTrue(summary["changed_file_truncated"])

    def test_auto_enrichment_uses_git_when_changed_files_omitted(self):
        record = build_record(
            _args(
                output=["artifacts/bench/new-run/report.md"],
                changed_file_limit=10,
                status_limit=10,
            ),
            git_runner=_fake_git_runner(
                " M scripts/write_run_manifest.py\n"
                "?? artifacts/bench/new-run/report.md\n"
            ),
        )

        self.assertEqual(record["commit"], "abc1234")
        self.assertEqual(record["git_head"], "abc1234def5678")
        self.assertEqual(record["git_branch"], "main")
        self.assertEqual(record["changed_file_source"], "git_status")
        self.assertEqual(
            record["changed_file"],
            [
                "scripts/write_run_manifest.py",
                "artifacts/bench/new-run/report.md",
            ],
        )
        self.assertEqual(record["changed_file_count"], 2)
        self.assertFalse(record["changed_file_truncated"])
        self.assertTrue(record["dirty"])
        self.assertEqual(record["git_status_summary"]["untracked_changed"], 1)
        self.assertEqual(
            record["artifact_path"],
            ["artifacts/bench/new-run/report.md"],
        )

    def test_explicit_changed_files_win_over_git_enrichment(self):
        record = build_record(
            _args(
                changed_file=["scripts/explicit.py"],
                output=["artifacts/bench/a.json", "artifacts/bench/a.json"],
            ),
            git_runner=_fake_git_runner(" M scripts/from-git.py\n"),
        )

        self.assertEqual(record["changed_file"], ["scripts/explicit.py"])
        self.assertEqual(record["changed_file_source"], "explicit")
        self.assertEqual(record["changed_file_count"], 1)
        self.assertFalse(record["changed_file_truncated"])
        self.assertEqual(record["artifact_path"], ["artifacts/bench/a.json"])
        self.assertEqual(record["artifact_path_count"], 1)
        self.assertFalse(record["artifact_path_truncated"])

    def test_artifact_path_evidence_records_missing_and_present_outputs(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            present_path = root / "artifacts" / "bench" / "present" / "report.md"
            present_path.parent.mkdir(parents=True)
            present_path.write_text("ok\n", encoding="utf-8")
            manifest_path = root / "manifest.jsonl"

            payload = _run_script(
                [
                    "--task",
                    "artifact-evidence",
                    "--lane",
                    "tooling",
                    "--output",
                    "artifacts/bench/present/report.md",
                    "--output",
                    "artifacts/bench/missing/report.md",
                    "--commit",
                    "abc123",
                    "--manifest-path",
                    str(manifest_path),
                ],
                cwd=tmpdir,
            )

        self.assertEqual(
            payload["artifact_path"],
            [
                "artifacts/bench/present/report.md",
                "artifacts/bench/missing/report.md",
            ],
        )
        self.assertEqual(
            payload["artifact_path_missing"],
            ["artifacts/bench/missing/report.md"],
        )
        self.assertEqual(payload["artifact_path_missing_count"], 1)
        present_evidence = payload["artifact_path_evidence"][0]
        self.assertEqual(present_evidence["path"], "artifacts/bench/present/report.md")
        self.assertIs(present_evidence["exists"], True)
        self.assertEqual(present_evidence["kind"], "file")
        self.assertIsInstance(present_evidence["modified_at"], str)
        self.assertEqual(
            payload["artifact_path_evidence"][1],
            {
                "path": "artifacts/bench/missing/report.md",
                "exists": False,
                "kind": "missing",
            },
        )

    def test_absolute_repo_artifact_path_records_portable_relative_path(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            report_path = root / "artifacts" / "bench" / "absolute" / "report.md"
            report_path.parent.mkdir(parents=True)
            report_path.write_text("ok\n", encoding="utf-8")
            manifest_path = root / "manifest.jsonl"

            payload = _run_script(
                [
                    "--task",
                    "absolute-artifact",
                    "--lane",
                    "tooling",
                    "--output",
                    str(report_path),
                    "--manifest-path",
                    str(manifest_path),
                ],
                cwd=tmpdir,
            )

        self.assertEqual(payload["artifact_path"], ["artifacts/bench/absolute/report.md"])
        self.assertEqual(payload["artifact_path_missing"], [])
        evidence = payload["artifact_path_evidence"][0]
        self.assertEqual(evidence["path"], "artifacts/bench/absolute/report.md")
        self.assertIs(evidence["exists"], True)
        self.assertEqual(evidence["kind"], "file")

    def test_artifact_paths_dedupe_backslash_and_forward_slash_equivalents(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            report_path = root / "artifacts" / "bench" / "dedupe" / "report.md"
            report_path.parent.mkdir(parents=True)
            report_path.write_text("ok\n", encoding="utf-8")
            manifest_path = root / "manifest.jsonl"

            payload = _run_script(
                [
                    "--task",
                    "artifact-dedupe",
                    "--lane",
                    "tooling",
                    "--output",
                    "artifacts/bench/dedupe/report.md",
                    "--output",
                    "artifacts\\bench\\dedupe\\report.md",
                    "--manifest-path",
                    str(manifest_path),
                ],
                cwd=tmpdir,
            )

        self.assertEqual(payload["artifact_path"], ["artifacts/bench/dedupe/report.md"])
        self.assertEqual(payload["artifact_path_count"], 1)
        self.assertFalse(payload["artifact_path_truncated"])
        self.assertEqual(payload["artifact_path_missing"], [])
        self.assertEqual(len(payload["artifact_path_evidence"]), 1)
        self.assertIs(payload["artifact_path_evidence"][0]["exists"], True)

    def test_collect_artifact_paths_limits_guard_and_path_key_dedupe(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            report_path = root / "artifacts" / "bench" / "canonical" / "report.md"
            report_path.parent.mkdir(parents=True, exist_ok=True)
            report_path.write_text("ok\n", encoding="utf-8")

            limited = collect_artifact_paths(
                ["artifacts/bench/canonical/../canonical/report.md"],
                limit=0,
                repo_root=root,
            )
            self.assertEqual(limited["paths"], [])
            self.assertEqual(limited["evidence_paths"], {})
            self.assertEqual(limited["count"], 0)
            self.assertFalse(limited["truncated"])

            paths = collect_artifact_paths(
                [
                    "artifacts/bench/canonical/report.md",
                    str(report_path),
                    "artifacts\\bench\\canonical\\./report.md",
                ],
                limit=3,
                repo_root=root,
            )
            self.assertEqual(paths["paths"], ["artifacts/bench/canonical/report.md"])
            self.assertEqual(paths["count"], 1)
            self.assertFalse(paths["truncated"])
            self.assertEqual(
                paths["evidence_paths"]["artifacts/bench/canonical/report.md"],
                report_path,
            )

    def test_artifact_path_evidence_records_directory_outputs(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            directory_path = root / "artifacts" / "bench" / "run-dir"
            directory_path.mkdir(parents=True)
            manifest_path = root / "manifest.jsonl"

            payload = _run_script(
                [
                    "--task",
                    "artifact-directory-evidence",
                    "--lane",
                    "tooling",
                    "--output",
                    "artifacts/bench/run-dir",
                    "--manifest-path",
                    str(manifest_path),
                ],
                cwd=tmpdir,
            )

        self.assertEqual(payload["artifact_path"], ["artifacts/bench/run-dir"])
        self.assertEqual(payload["artifact_path_missing"], [])
        self.assertEqual(payload["artifact_path_missing_count"], 0)
        directory_evidence = payload["artifact_path_evidence"][0]
        self.assertEqual(directory_evidence["path"], "artifacts/bench/run-dir")
        self.assertIs(directory_evidence["exists"], True)
        self.assertEqual(directory_evidence["kind"], "directory")
        self.assertIsInstance(directory_evidence["modified_at"], str)

    def test_auto_enrichment_caps_changed_files_status_and_artifacts(self):
        status_text = "\n".join(
            [
                " M scripts/a.py",
                " M scripts/b.py",
                " M artifacts/bench/c.json",
                "?? artifacts/bench/d.json",
                "?? notes/e.md",
            ]
        )
        record = build_record(
            _args(
                output=[
                    "artifacts/bench/c.json",
                    "artifacts/bench/d.json",
                    "artifacts/bench/e.json",
                ],
                changed_file_limit=2,
                status_limit=3,
                artifact_path_limit=2,
            ),
            git_runner=_fake_git_runner(status_text),
        )

        self.assertEqual(record["changed_file"], ["scripts/a.py", "scripts/b.py"])
        self.assertEqual(record["changed_file_count"], 5)
        self.assertTrue(record["changed_file_truncated"])
        self.assertTrue(record["git_status_short_truncated"])
        self.assertEqual(len(record["git_status_summary"]["entries"]), 3)
        self.assertEqual(
            record["artifact_path"],
            ["artifacts/bench/c.json", "artifacts/bench/d.json"],
        )
        self.assertEqual(record["artifact_path_count"], 3)
        self.assertTrue(record["artifact_path_truncated"])

    def test_git_failure_is_graceful(self):
        def failing_runner(args):
            return CommandResult(stdout="", stderr="not a git repository", returncode=128)

        record = build_record(_args(), git_runner=failing_runner)

        self.assertIsNone(record["commit"])
        self.assertIsNone(record["git_head"])
        self.assertIsNone(record["git_branch"])
        self.assertIsNone(record["git_status_short"])
        self.assertEqual(record["changed_file"], [])
        self.assertEqual(record["changed_file_source"], "none")
        self.assertFalse(record["dirty"])
        self.assertIn("error", record["git_status_summary"])

    def test_git_unavailable_exception_is_graceful(self):
        def missing_git_runner(args):
            raise OSError("git not found")

        record = build_record(_args(), git_runner=missing_git_runner)

        self.assertIsNone(record["commit"])
        self.assertIsNone(record["git_head"])
        self.assertEqual(record["changed_file"], [])
        self.assertFalse(record["dirty"])
        self.assertEqual(record["git_status_summary"]["error"], "OSError")

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

    def test_cli_runs_outside_git(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            manifest_path = Path(tmpdir) / "manifest.jsonl"
            payload = _run_script(
                [
                    "--task",
                    "outside-git",
                    "--lane",
                    "tooling",
                    "--manifest-path",
                    str(manifest_path),
                ],
                cwd=tmpdir,
            )

            self.assertIsNone(payload["commit"])
            self.assertEqual(payload["changed_file"], [])
            self.assertEqual(payload["changed_file_source"], "none")
            self.assertFalse(payload["dirty"])


if __name__ == "__main__":
    unittest.main()
