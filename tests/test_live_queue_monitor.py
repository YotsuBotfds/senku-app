import importlib.util
import json
import os
import sys
import tempfile
import threading
import unittest
from http.server import ThreadingHTTPServer
from pathlib import Path
from urllib.request import urlopen


def load_module():
    module_path = Path(__file__).resolve().parents[1] / "scripts" / "live_queue_monitor.py"
    spec = importlib.util.spec_from_file_location("live_queue_monitor", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


class LiveQueueMonitorTests(unittest.TestCase):
    def test_parse_git_status_short_counts_and_limits_entries(self):
        module = load_module()

        summary = module.parse_git_status_short(
            " M query.py\n?? scripts/live_queue_monitor.py\nR  old.py -> new.py\n",
            limit=2,
        )

        self.assertFalse(summary["clean"])
        self.assertEqual(summary["total_changed"], 3)
        self.assertEqual(summary["status_counts"], {" M": 1, "??": 1, "R ": 1})
        self.assertEqual(len(summary["entries"]), 2)
        self.assertTrue(summary["truncated"])

    def test_parse_git_status_short_ignores_branch_header_metadata(self):
        module = load_module()

        summary = module.parse_git_status_short(
            "## main...origin/main [ahead 1]\n M query.py\n",
        )

        self.assertFalse(summary["clean"])
        self.assertEqual(summary["total_changed"], 1)
        self.assertEqual(summary["raw_total_changed"], 1)
        self.assertEqual(summary["status_counts"], {" M": 1})
        self.assertEqual(summary["raw_status_counts"], {" M": 1})
        self.assertEqual(summary["entries"], [{"status": " M", "path": "query.py"}])

    def test_parse_git_status_short_splits_benign_untracked_paths(self):
        module = load_module()

        summary = module.parse_git_status_short(
            "?? notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md\n M query.py\n",
            benign_untracked_paths={"notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md"},
        )

        self.assertFalse(summary["clean"])
        self.assertFalse(summary["raw_clean"])
        self.assertEqual(summary["total_changed"], 1)
        self.assertEqual(summary["raw_total_changed"], 2)
        self.assertEqual(summary["status_counts"], {" M": 1})
        self.assertEqual(summary["raw_status_counts"], {"??": 1, " M": 1})
        self.assertEqual(
            summary["benign_untracked"],
            [{"status": "??", "path": "notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md"}],
        )
        self.assertEqual(summary["entries"], [{"status": " M", "path": "query.py"}])

    def test_parse_git_status_short_is_clean_when_only_benign_untracked_is_present(self):
        module = load_module()

        summary = module.parse_git_status_short(
            "?? notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md\n",
            benign_untracked_paths={"notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md"},
        )

        self.assertTrue(summary["clean"])
        self.assertFalse(summary["raw_clean"])
        self.assertEqual(summary["total_changed"], 0)
        self.assertEqual(summary["raw_total_changed"], 1)
        self.assertEqual(summary["entries"], [])
        self.assertEqual(summary["benign_untracked_count"], 1)

        all_protected = "".join(
            f"?? {path}\n" for path in sorted(module.PROTECTED_BENIGN_UNTRACKED)
        )
        summary = module.parse_git_status_short(
            all_protected,
            benign_untracked_paths=module.PROTECTED_BENIGN_UNTRACKED,
        )
        self.assertTrue(summary["clean"])
        self.assertEqual(summary["total_changed"], 0)
        self.assertEqual(
            summary["benign_untracked_count"],
            len(module.PROTECTED_BENIGN_UNTRACKED),
        )

    def test_parse_git_status_short_normalizes_windows_protected_handoff_paths(self):
        module = load_module()

        summary = module.parse_git_status_short(
            "?? notes\\PLANNER_HANDOFF_2026-04-26_AWAITING_DEEP_RESEARCH.md\n M query.py\n",
            benign_untracked_paths=module.PROTECTED_BENIGN_UNTRACKED,
        )

        self.assertFalse(summary["clean"])
        self.assertFalse(summary["raw_clean"])
        self.assertEqual(summary["total_changed"], 1)
        self.assertEqual(summary["raw_total_changed"], 2)
        self.assertEqual(summary["status_counts"], {" M": 1})
        self.assertEqual(summary["raw_status_counts"], {"??": 1, " M": 1})
        self.assertEqual(
            summary["benign_untracked"],
            [
                {
                    "status": "??",
                    "path": "notes/PLANNER_HANDOFF_2026-04-26_AWAITING_DEEP_RESEARCH.md",
                }
            ],
        )
        self.assertEqual(summary["entries"], [{"status": " M", "path": "query.py"}])

    def test_collect_git_summary_uses_injected_runner(self):
        module = load_module()
        calls = []

        def fake_git_runner(repo_root, args):
            calls.append(tuple(args))
            if args[:2] == ["status", "--short"]:
                return module.CommandResult(stdout=" M notes/example.md\n")
            if args[:2] == ["branch", "--show-current"]:
                return module.CommandResult(stdout="main\n")
            if args[:3] == ["rev-parse", "--short", "HEAD"]:
                return module.CommandResult(stdout="abc1234\n")
            if args[:2] == ["rev-parse", "HEAD"]:
                return module.CommandResult(stdout="abc1234def5678\n")
            if args and args[0] == "log":
                return module.CommandResult(
                    stdout="abc1234\t2026-04-25T12:00:00+00:00\tAdd monitor\n"
                )
            return module.CommandResult(stdout="")

        summary = module.collect_git_summary(
            Path("repo"),
            commit_limit=1,
            git_runner=fake_git_runner,
        )

        self.assertIn(("status", "--short"), calls)
        self.assertEqual(summary["branch"], "main")
        self.assertEqual(summary["head"], "abc1234")
        self.assertEqual(summary["head_full"], "abc1234def5678")
        self.assertEqual(summary["status"]["total_changed"], 1)
        self.assertEqual(summary["latest_commits"][0]["subject"], "Add monitor")

    def test_collect_git_summary_treats_protected_handoff_as_benign(self):
        module = load_module()

        def fake_git_runner(repo_root, args):
            if args[:2] == ["status", "--short"]:
                return module.CommandResult(stdout="?? notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md\n")
            if args[:2] == ["branch", "--show-current"]:
                return module.CommandResult(stdout="master\n")
            if args[:3] == ["rev-parse", "--short", "HEAD"]:
                return module.CommandResult(stdout="3c8678b\n")
            if args[:2] == ["rev-parse", "HEAD"]:
                return module.CommandResult(stdout="3c8678b000000000000000000000000000000000\n")
            return module.CommandResult(stdout="")

        summary = module.collect_git_summary(Path("repo"), git_runner=fake_git_runner)

        self.assertTrue(summary["status"]["clean"])
        self.assertFalse(summary["status"]["raw_clean"])
        self.assertEqual(summary["status"]["total_changed"], 0)
        self.assertEqual(summary["status"]["raw_total_changed"], 1)
        self.assertEqual(summary["status"]["benign_untracked_count"], 1)

    def test_collect_dispatch_pointers_sorts_recent_and_filters_tooling(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dispatch_dir = root / "notes" / "dispatch"
            dispatch_dir.mkdir(parents=True)
            (dispatch_dir / "README.md").write_text("# Dispatch readme\n", encoding="utf-8")
            older = dispatch_dir / "RAG-EVAL9_eval_task.md"
            newer = dispatch_dir / "RAG-TOOL3_live_queue_monitor.md"
            older.write_text("# RAG-EVAL9 Eval task\n", encoding="utf-8")
            newer.write_text("# RAG-TOOL3 Live Queue Monitor\n", encoding="utf-8")
            os.utime(older, (1000, 1000))
            os.utime(newer, (2000, 2000))

            pointers = module.collect_dispatch_pointers(root, limit=5)

        self.assertEqual(pointers["active_count"], 2)
        self.assertEqual(pointers["active"][0]["path"], "notes/dispatch/RAG-TOOL3_live_queue_monitor.md")
        self.assertEqual(pointers["active"][0]["title"], "RAG-TOOL3 Live Queue Monitor")
        self.assertEqual(pointers["tooling_count"], 1)
        self.assertEqual(pointers["tooling"][0]["name"], "RAG-TOOL3_live_queue_monitor.md")

    def test_collect_bench_artifacts_returns_newest_and_diagnostics(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            bench_dir = root / "artifacts" / "bench"
            diag_dir = bench_dir / "run_diag"
            diag_dir.mkdir(parents=True)
            report = diag_dir / "report.md"
            artifact = bench_dir / "run.json"
            report.write_text("# Report\n", encoding="utf-8")
            artifact.write_text("{}", encoding="utf-8")
            os.utime(diag_dir, (3000, 3000))
            os.utime(report, (4000, 4000))
            os.utime(artifact, (5000, 5000))

            bench = module.collect_bench_artifacts(root, limit=10)

        self.assertEqual(bench["newest"][0]["path"], "artifacts/bench/run.json")
        diagnostic_paths = {item["path"] for item in bench["diagnostics"]}
        self.assertIn("artifacts/bench/run_diag", diagnostic_paths)
        self.assertIn("artifacts/bench/run_diag/report.md", diagnostic_paths)

    def test_collect_cp9_summary_extracts_active_snippet_and_rag_landings(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            notes_dir = root / "notes"
            notes_dir.mkdir()
            cp9 = notes_dir / "CP9_ACTIVE_QUEUE.md"
            cp9.write_text(
                "\n".join(
                    [
                        "# CP9 Active Queue",
                        "",
                        "## Active",
                        "",
                        "No slices currently in flight.",
                        "- `RAG-S10` answer-provenance labels landed: proof exists.",
                        "- `RAG-A7` kept the Android runtime path narrow.",
                        "",
                        "## Backlog",
                        "- Older item.",
                    ]
                ),
                encoding="utf-8",
            )

            summary = module.collect_cp9_summary(root, limit=3)

        self.assertEqual(summary["path"], "notes/CP9_ACTIVE_QUEUE.md")
        self.assertEqual(summary["title"], "CP9 Active Queue")
        self.assertEqual(summary["active_snippet"][0], "No slices currently in flight.")
        self.assertIn("RAG-S10 answer-provenance labels landed", summary["rag_landed"][0])
        self.assertEqual(len(summary["rag_landed"]), 1)

    def test_collect_monitor_data_and_render_html_are_server_independent(self):
        module = load_module()

        def fake_git_runner(repo_root, args):
            if args[:2] == ["status", "--short"]:
                return module.CommandResult(stdout="")
            if args[:3] == ["worktree", "list", "--porcelain"]:
                return module.CommandResult(
                    stdout="worktree C:/repo\nHEAD abc1234\nbranch refs/heads/main\n"
                )
            if args[:4] == ["-C", "C:/repo", "status", "--short"]:
                return module.CommandResult(stdout="")
            if args[:2] == ["branch", "--show-current"]:
                return module.CommandResult(stdout="main\n")
            if args[:3] == ["rev-parse", "--short", "HEAD"]:
                return module.CommandResult(stdout="abc1234\n")
            if args[:2] == ["rev-parse", "HEAD"]:
                return module.CommandResult(stdout="abc1234def5678\n")
            return module.CommandResult(stdout="")

        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "notes" / "dispatch").mkdir(parents=True)
            (root / "artifacts" / "bench").mkdir(parents=True)
            data = module.collect_monitor_data(
                root,
                git_runner=fake_git_runner,
                now=lambda: "2026-04-25T12:34:56-05:00",
            )

        rendered = module.render_html_page(refresh_seconds=20)

        self.assertEqual(data["timestamp"], "2026-04-25T12:34:56-05:00")
        self.assertTrue(data["git"]["status"]["clean"])
        self.assertIn("worker_lanes", data)
        self.assertEqual(data["worker_lanes"]["worktrees"][0]["branch_short"], "main")
        self.assertIn("cp9", data)
        self.assertIn("fetch('/status.json?ts='", rendered)
        self.assertIn("setInterval(refresh, REFRESH_MS)", rendered)
        self.assertIn("Polling every <code>20s</code>", rendered)
        self.assertIn("Worker Lanes", rendered)
        self.assertIn("CP9 / RAG Queue", rendered)

    def test_collect_monitor_data_surfaces_worker_lane_dirty_status_counts(self):
        module = load_module()

        def fake_git_runner(repo_root, args):
            if args[:3] == ["status", "--short"]:
                return module.CommandResult(stdout="")
            if args[:3] == ["worktree", "list", "--porcelain"]:
                return module.CommandResult(
                    stdout="worktree C:/repo_worktrees/lane-a\nHEAD abc1234\nbranch refs/heads/worker/lane-a\n"
                )
            if args[:3] == ["branch", "--show-current"]:
                return module.CommandResult(stdout="main\n")
            if args[:3] == ["rev-parse", "--short", "HEAD"]:
                return module.CommandResult(stdout="abc1234\n")
            if args[:2] == ["rev-parse", "HEAD"]:
                return module.CommandResult(stdout="abc1234def5678\n")
            if args[:1] == ["-C"] and args[2:4] == ["status", "--short"]:
                return module.CommandResult(stdout=" M scripts/monitor.py\n?? notes/new-note.md\n")
            return module.CommandResult(stdout="")

        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "notes" / "dispatch").mkdir(parents=True)
            (root / "artifacts" / "bench").mkdir(parents=True)
            data = module.collect_monitor_data(
                root,
                git_runner=fake_git_runner,
                now=lambda: "2026-04-25T12:34:56-05:00",
            )

        lane = data["worker_lanes"]["worktrees"][0]
        self.assertEqual(lane["branch_short"], "worker/lane-a")
        self.assertFalse(lane["dirty"]["clean"])
        self.assertEqual(lane["dirty"]["changed"], 2)
        self.assertEqual(
            lane["dirty"]["status_counts"],
            {"modified": 1, "untracked": 1},
        )
        self.assertEqual(lane["dirty"]["entry_details"][0]["status"], "modified")
        self.assertEqual(lane["dirty"]["entry_details"][1]["status"], "untracked")

    def test_render_html_page_formats_dirty_status_counts(self):
        module = load_module()

        rendered = module.render_html_page(refresh_seconds=20)

        self.assertIn("const formatDirtySummary = (dirty) => {", rendered)
        self.assertIn("return `${changedLabel} (${summaryParts.join(\", \")})`;", rendered)
        self.assertIn("const dirty = formatDirtySummary(item.dirty);", rendered)

    def test_render_html_page_guards_malformed_worker_lane_payloads(self):
        module = load_module()

        rendered = module.render_html_page(refresh_seconds=20)

        self.assertIn(
            "const isRecord = (value) => !!value && typeof value === 'object' && !Array.isArray(value);",
            rendered,
        )
        self.assertIn("const asArray = (value) => Array.isArray(value) ? value : [];", rendered)
        self.assertIn("if (!isRecord(dirty) || dirty.error) {", rendered)
        self.assertIn(
            "const counts = isRecord(dirty.status_counts) ? dirty.status_counts : {};",
            rendered,
        )
        self.assertIn("const items = asArray(values);", rendered)
        self.assertIn("if (!isRecord(item)) {", rendered)

    def test_render_html_page_guards_malformed_status_payloads(self):
        module = load_module()

        rendered = module.render_html_page(refresh_seconds=20)

        self.assertIn("data = isRecord(data) ? data : {};", rendered)
        self.assertIn("const benign = asArray(status.benign_untracked);", rendered)

    def test_status_endpoint_returns_json_monitor_state(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "notes" / "dispatch").mkdir(parents=True)
            (root / "artifacts" / "bench").mkdir(parents=True)
            server = ThreadingHTTPServer(
                ("127.0.0.1", 0),
                module.make_handler(root, refresh_seconds=20),
            )
            thread = threading.Thread(target=server.serve_forever)
            thread.start()
            try:
                host, port = server.server_address
                with urlopen(f"http://{host}:{port}/status", timeout=5) as response:
                    body = response.read()
                    content_type = response.headers.get("Content-Type")
                    cache_control = response.headers.get("Cache-Control")
            finally:
                server.shutdown()
                thread.join(timeout=5)
                server.server_close()

        data = json.loads(body.decode("utf-8"))

        self.assertEqual(content_type, "application/json; charset=utf-8")
        self.assertEqual(cache_control, "no-store")
        self.assertIn("timestamp", data)
        self.assertIn("git", data)
        self.assertIn("worker_lanes", data)
        self.assertIn("queues", data)


if __name__ == "__main__":
    unittest.main()
