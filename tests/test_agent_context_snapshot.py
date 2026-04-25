import json
import subprocess
import tempfile
import unittest
from contextlib import redirect_stdout
from io import StringIO
from pathlib import Path

from scripts.agent_context_snapshot import (
    build_snapshot,
    cap_markdown_lines,
    collect_artifact_summary,
    main,
)


def fake_runner(command, cwd):
    joined = " ".join(command)
    if joined == "git rev-parse --short HEAD":
        return subprocess.CompletedProcess(command, 0, "abc1234\n", "")
    if joined == "git log -5 --oneline --decorate":
        return subprocess.CompletedProcess(
            command,
            0,
            "abc1234 (HEAD -> main) Latest change\n1111111 Previous change\n",
            "",
        )
    if joined == "git status --short":
        return subprocess.CompletedProcess(
            command,
            0,
            " M scripts/existing.py\n?? notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md\n",
            "",
        )
    return subprocess.CompletedProcess(command, 1, "", "unexpected")


class AgentContextSnapshotTests(unittest.TestCase):
    def make_tmpdir(self):
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def test_build_snapshot_includes_expected_sections_and_caps_output(self):
        root = self.make_tmpdir()
        dispatch = root / "notes" / "dispatch"
        dispatch.mkdir(parents=True)
        (dispatch / "D1_context.md").write_text(
            "# Slice D1 - Context handoff\n\n## Outcome\n\n- Landed `scripts/tool.py`.\n",
            encoding="utf-8",
        )

        manifest = root / "artifacts" / "runs" / "run_manifest.jsonl"
        manifest.parent.mkdir(parents=True)
        manifest.write_text(
            json.dumps(
                {
                    "generated_at": "2026-04-25T12:00:00+00:00",
                    "task": "D1",
                    "lane": "tooling",
                    "label": "snapshot",
                    "commit": "abc1234",
                    "validation": ["unit ok"],
                }
            )
            + "\n",
            encoding="utf-8",
        )

        bench = root / "artifacts" / "bench"
        bench.mkdir(parents=True)
        (bench / "run.json").write_text(
            json.dumps({"summary": {"passed": 2}, "results": [{"id": 1}]}),
            encoding="utf-8",
        )

        markdown = build_snapshot(
            root,
            dispatch_dir=dispatch,
            manifest_path=manifest,
            bench_dir=bench,
            max_lines=80,
            runner=fake_runner,
        )

        self.assertIn("# Agent Startup Context Snapshot", markdown)
        self.assertIn("## Git", markdown)
        self.assertIn("`abc1234`", markdown)
        self.assertIn("PLANNER_HANDOFF_2026-04-25_FAST_MODE.md", markdown)
        self.assertIn("## Recent Dispatch Notes", markdown)
        self.assertIn("Context handoff", markdown)
        self.assertIn("## Recent Run Manifest", markdown)
        self.assertIn("snapshot", markdown)
        self.assertIn("## Latest Bench Artifact Pointers", markdown)
        self.assertIn("run.json", markdown)
        self.assertLessEqual(len(markdown.splitlines()), 80)

    def test_artifact_summary_excludes_log_body_content(self):
        root = self.make_tmpdir()
        log = root / "secret_progress.log"
        log.write_text("RAW BODY SHOULD NOT APPEAR", encoding="utf-8")
        (root / "summary.json").write_text(
            json.dumps({"summary": {"errors": 0}, "results": []}),
            encoding="utf-8",
        )

        markdown = "\n".join(collect_artifact_summary(root, limit=5))

        self.assertIn("secret_progress.log", markdown)
        self.assertIn("skipped=log_not_read", markdown)
        self.assertNotIn("RAW BODY SHOULD NOT APPEAR", markdown)

    def test_cap_markdown_lines_adds_truncation_footer(self):
        markdown = "\n".join(f"line {index}" for index in range(10)) + "\n"

        capped = cap_markdown_lines(markdown, 5)

        self.assertEqual(len(capped.splitlines()), 5)
        self.assertTrue(capped.endswith("... truncated to 5 lines ...\n"))

    def test_main_writes_same_markdown_to_output_and_stdout_path(self):
        root = self.make_tmpdir()
        dispatch = root / "notes" / "dispatch"
        dispatch.mkdir(parents=True)
        manifest = root / "artifacts" / "runs" / "run_manifest.jsonl"
        manifest.parent.mkdir(parents=True)
        bench = root / "artifacts" / "bench"
        bench.mkdir(parents=True)
        output = root / "snapshot.md"

        stdout = StringIO()
        with redirect_stdout(stdout):
            rc = main(
                [
                    "--repo-root",
                    str(root),
                    "--dispatch-dir",
                    str(dispatch),
                    "--manifest",
                    str(manifest),
                    "--bench-dir",
                    str(bench),
                    "--output-md",
                    str(output),
                    "--max-lines",
                    "50",
                ]
            )

        self.assertEqual(rc, 0)
        self.assertEqual(output.read_text(encoding="utf-8"), stdout.getvalue())


if __name__ == "__main__":
    unittest.main()
