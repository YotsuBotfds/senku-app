import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts import worker_lane_status


PORCELAIN = """worktree C:/repo
HEAD abc1234
branch refs/heads/main

worktree C:/repo_worktrees/lane-a
HEAD def5678
branch refs/heads/worker/lane-a

worktree C:/repo_worktrees/detached
HEAD 9999999
detached
locked
locked needs review
"""


class WorkerLaneStatusTests(unittest.TestCase):
    def test_parse_worktree_porcelain_extracts_records_and_branch_short_names(self):
        records = worker_lane_status.parse_worktree_porcelain(PORCELAIN)

        self.assertEqual(len(records), 3)
        self.assertEqual(records[0]["worktree"], "C:/repo")
        self.assertEqual(records[0]["branch_short"], "main")
        self.assertFalse(records[0]["detached"])
        self.assertEqual(records[1]["branch_short"], "worker/lane-a")
        self.assertTrue(records[2]["detached"])
        self.assertTrue(records[2]["locked"])
        self.assertEqual(records[2]["locked_reason"], "needs review")

    def test_load_lane_leases_reads_json_and_reports_malformed_files(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            lease_dir = Path(tmpdir)
            (lease_dir / "lane-a.json").write_text(
                json.dumps({"lane": "lane-a", "branch": "worker/lane-a"}),
                encoding="utf-8",
            )
            (lease_dir / "broken.json").write_text("{", encoding="utf-8")

            leases = worker_lane_status.load_lane_leases(lease_dir)

        self.assertEqual(len(leases["leases"]), 1)
        self.assertEqual(leases["leases"][0]["lane"], "lane-a")
        self.assertEqual(len(leases["malformed"]), 1)
        self.assertEqual(leases["malformed"][0]["error"], "JSONDecodeError")

    def test_collect_status_matches_leases_by_branch_and_reports_dirty_state(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            lease_dir = root / "artifacts" / "runs" / "worker_lanes"
            lease_dir.mkdir(parents=True)
            (lease_dir / "lane-a.json").write_text(
                json.dumps(
                    {
                        "lane": "lane-a",
                        "branch": "worker/lane-a",
                        "worktree_path": "C:/repo_worktrees/lane-a",
                    }
                ),
                encoding="utf-8",
            )
            calls = []

            def fake_runner(command, cwd):
                calls.append((tuple(command), cwd))
                if tuple(command) == ("git", "worktree", "list", "--porcelain"):
                    return subprocess.CompletedProcess(command, 0, PORCELAIN, "")
                if command[:2] == ["git", "-C"] and command[-2:] == ["status", "--short"]:
                    return subprocess.CompletedProcess(command, 0, " M scripts/tool.py\n", "")
                return subprocess.CompletedProcess(command, 0, "", "")

            status = worker_lane_status.collect_status(root, runner=fake_runner)

        lane = status["worktrees"][1]
        self.assertEqual(lane["lane"], "lane-a")
        self.assertEqual(lane["lease"]["branch"], "worker/lane-a")
        self.assertFalse(lane["dirty"]["clean"])
        self.assertEqual(lane["dirty"]["changed"], 1)
        self.assertIn(("git", "worktree", "list", "--porcelain"), [call[0] for call in calls])

    def test_collect_status_marks_worktrees_missing_paths_without_dirty_check(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            calls = []

            def fake_runner(command, cwd):
                calls.append(tuple(command))
                if tuple(command) == ("git", "worktree", "list", "--porcelain"):
                    return subprocess.CompletedProcess(
                        command,
                        0,
                        "HEAD abc1234\nbranch refs/heads/worker/missing-path\n",
                        "",
                    )
                return subprocess.CompletedProcess(command, 0, "", "")

            status = worker_lane_status.collect_status(root, runner=fake_runner)

        self.assertEqual(len(status["worktrees"]), 1)
        self.assertTrue(status["worktrees"][0]["missing_path"])
        self.assertNotIn("dirty", status["worktrees"][0])
        self.assertEqual(calls, [("git", "worktree", "list", "--porcelain")])

    def test_dirty_summary_adds_structured_details_and_status_counts(self):
        status_text = "\n".join(
            [
                " M scripts/tool.py",
                "?? notes/new.md",
                "R  old_name.py -> new_name.py",
            ]
        )

        def fake_runner(command, cwd):
            return subprocess.CompletedProcess(command, 0, status_text, "")

        dirty = worker_lane_status._dirty_summary(Path("C:/repo"), fake_runner)

        self.assertFalse(dirty["clean"])
        self.assertEqual(dirty["changed"], 3)
        self.assertEqual(
            dirty["entries"],
            [" M scripts/tool.py", "?? notes/new.md", "R  old_name.py -> new_name.py"],
        )
        self.assertFalse(dirty["truncated"])
        self.assertEqual(dirty["status_counts"], {"modified": 1, "untracked": 1, "renamed": 1})
        self.assertEqual(dirty["entry_details"][0]["status"], "modified")
        self.assertEqual(dirty["entry_details"][0]["path"], "scripts/tool.py")
        self.assertEqual(dirty["entry_details"][1]["status"], "untracked")
        self.assertEqual(dirty["entry_details"][2]["status"], "renamed")
        self.assertEqual(dirty["entry_details"][2]["original_path"], "old_name.py")
        self.assertEqual(dirty["entry_details"][2]["path"], "new_name.py")

    def test_dirty_summary_caps_entries_and_details_but_counts_all_changes(self):
        status_text = "\n".join(f" M file_{index}.py" for index in range(22))

        def fake_runner(command, cwd):
            return subprocess.CompletedProcess(command, 0, status_text, "")

        dirty = worker_lane_status._dirty_summary(Path("C:/repo"), fake_runner)

        self.assertEqual(dirty["changed"], 22)
        self.assertEqual(len(dirty["entries"]), 20)
        self.assertEqual(len(dirty["entry_details"]), 20)
        self.assertTrue(dirty["truncated"])
        self.assertEqual(dirty["status_counts"], {"modified": 22})

    def test_render_markdown_contains_compact_table(self):
        markdown = worker_lane_status.render_markdown(
            {
                "repo_root": "C:/repo",
                "lease_dir": "C:/repo/artifacts/runs/worker_lanes",
                "leases": [{"lane": "lane-a"}],
                "worktrees": [
                    {
                        "lane": "lane-a",
                        "branch_short": "worker/lane-a",
                        "worktree": "C:/repo_worktrees/lane-a",
                        "dirty": {"clean": True, "changed": 0},
                    }
                ],
                "malformed_leases": [],
                "orphan_leases": [],
            }
        )

        self.assertIn("# Worker Lane Status", markdown)
        self.assertIn("| lane-a | worker/lane-a | clean | C:/repo_worktrees/lane-a |", markdown)

    def test_render_markdown_sanitizes_table_cell_newlines_and_pipes(self):
        markdown = worker_lane_status.render_markdown(
            {
                "repo_root": "C:/repo",
                "lease_dir": "C:/repo/artifacts/runs/worker_lanes",
                "leases": [{"lane": "lane-a"}],
                "worktrees": [
                    {
                        "lane": "lane-a\n| injected | row |",
                        "branch_short": "worker/lane-a\r\nnext",
                        "worktree": "C:/repo|worktrees/lane-a\nextra",
                        "dirty": {"clean": False, "changed": 2},
                    }
                ],
                "malformed_leases": [],
                "orphan_leases": [],
            }
        )

        rows = [line for line in markdown.splitlines() if line.startswith("|")]
        self.assertEqual(len(rows), 3)
        self.assertIn("lane-a \\| injected \\| row \\|", rows[2])
        self.assertIn("worker/lane-a  next", rows[2])
        self.assertIn("C:/repo\\|worktrees/lane-a extra", rows[2])

    def test_render_markdown_sanitizes_table_cell_control_characters(self):
        markdown = worker_lane_status.render_markdown(
            {
                "repo_root": "C:/repo",
                "lease_dir": "C:/repo/artifacts/runs/worker_lanes",
                "leases": [{"lane": "lane-a"}],
                "worktrees": [
                    {
                        "lane": "lane-a\x00\x1f\x7f",
                        "branch_short": "worker/lane-a",
                        "worktree": "C:/repo_worktrees/lane-a\x0bextra",
                        "dirty": {"clean": True, "changed": 0},
                    }
                ],
                "malformed_leases": [],
                "orphan_leases": [],
            }
        )

        rows = [line for line in markdown.splitlines() if line.startswith("|")]
        self.assertEqual(len(rows), 3)
        self.assertNotIn("\x00", rows[2])
        self.assertNotIn("\x1f", rows[2])
        self.assertNotIn("\x7f", rows[2])
        self.assertNotIn("\x0b", rows[2])
        self.assertIn("lane-a   ", rows[2])
        self.assertIn("C:/repo_worktrees/lane-a extra", rows[2])


if __name__ == "__main__":
    unittest.main()
