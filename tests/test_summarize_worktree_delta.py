import json
import subprocess
import sys
import unittest
from pathlib import Path

from scripts.summarize_worktree_delta import (
    lane_for_path,
    render_markdown,
    summarize_worktree_delta,
)


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "summarize_worktree_delta.py"


class SummarizeWorktreeDeltaTests(unittest.TestCase):
    def test_groups_porcelain_status_by_lane_and_excludes_generated_noise(self):
        status = "\n".join(
            [
                " M guides/example.md",
                "A  answer_cards/card.json",
                " M query.py",
                " M android-app/app/src/main/java/Main.kt",
                "?? tests/test_new.py",
                "?? notes/dispatch/RAG-1.md",
                "?? artifacts/bench/run.json",
                "?? scripts/new_tool.py",
                "?? README.md",
                "!! artifacts/prompts/generated.jsonl",
            ]
        )

        summary = summarize_worktree_delta(status_text=status)
        lanes = {lane["lane"]: lane for lane in summary["lanes"]}

        self.assertEqual(8, summary["total_changed"])
        self.assertEqual("guides/example.md", lanes["guides"]["files"][0]["path"])
        self.assertEqual("answer_cards/card.json", lanes["answer cards"]["files"][0]["path"])
        self.assertEqual("query.py", lanes["query/runtime"]["files"][0]["path"])
        self.assertEqual("android-app/app/src/main/java/Main.kt", lanes["android"]["files"][0]["path"])
        self.assertEqual("tests/test_new.py", lanes["tests"]["files"][0]["path"])
        self.assertEqual("notes/dispatch/RAG-1.md", lanes["notes/dispatch"]["files"][0]["path"])
        self.assertEqual("scripts/new_tool.py", lanes["scripts/tooling"]["files"][0]["path"])
        self.assertNotIn("artifacts/generated", lanes)

    def test_can_include_generated_artifacts_when_requested(self):
        status = "?? artifacts/bench/report.md\n!! artifacts/prompts/generated.jsonl\n"

        summary = summarize_worktree_delta(status_text=status, include_generated=True)
        lanes = {lane["lane"]: lane for lane in summary["lanes"]}

        self.assertEqual(2, summary["total_changed"])
        self.assertEqual(2, lanes["artifacts/generated"]["count"])

    def test_renames_and_diff_stat_are_preserved(self):
        status = "R  scripts/old_name.py -> scripts/new_name.py\n"
        diff_stat = " scripts/new_name.py | 12 +++++++-----\n"

        summary = summarize_worktree_delta(status_text=status, diff_stat_text=diff_stat)
        item = summary["lanes"][0]["files"][0]

        self.assertEqual("scripts/tooling", summary["lanes"][0]["lane"])
        self.assertEqual("scripts/old_name.py", item["old_path"])
        self.assertEqual("scripts/new_name.py", item["path"])
        self.assertEqual("12 +++++++-----", item["diff_stat"])

    def test_render_markdown_is_compact(self):
        summary = summarize_worktree_delta(status_text=" M scripts/new_tool.py\n")

        markdown = render_markdown(summary)

        self.assertIn("# Worktree Delta", markdown)
        self.assertIn("Changed files: 1", markdown)
        self.assertIn("## scripts/tooling (1)", markdown)
        self.assertIn("` M` `scripts/new_tool.py`", markdown)

    def test_cli_json_accepts_fixture_strings(self):
        completed = subprocess.run(
            [
                sys.executable,
                str(SCRIPT_PATH),
                "--format",
                "json",
                "--no-diff-stat",
                "--status-fixture",
                "?? tests/test_fixture.py\n",
            ],
            check=True,
            capture_output=True,
            text=True,
        )

        payload = json.loads(completed.stdout)
        self.assertEqual(1, payload["total_changed"])
        self.assertEqual("tests", payload["lanes"][0]["lane"])

    def test_lane_for_root_runtime_modules(self):
        self.assertEqual("query/runtime", lane_for_path("query_completion_hardening.py"))
        self.assertEqual("query/runtime", lane_for_path("rag_bench_answer_diagnostics.py"))


if __name__ == "__main__":
    unittest.main()
