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


if __name__ == "__main__":
    unittest.main()
