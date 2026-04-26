import importlib.util
import os
import sys
import tempfile
import unittest
from pathlib import Path


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
        self.assertEqual(summary["status"]["total_changed"], 1)
        self.assertEqual(summary["latest_commits"][0]["subject"], "Add monitor")

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

    def test_collect_monitor_data_and_render_html_are_server_independent(self):
        module = load_module()

        def fake_git_runner(repo_root, args):
            if args[:2] == ["status", "--short"]:
                return module.CommandResult(stdout="")
            if args[:2] == ["branch", "--show-current"]:
                return module.CommandResult(stdout="main\n")
            if args[:3] == ["rev-parse", "--short", "HEAD"]:
                return module.CommandResult(stdout="abc1234\n")
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
        self.assertIn("fetch('/status.json?ts='", rendered)
        self.assertIn("setInterval(refresh, REFRESH_MS)", rendered)
        self.assertIn("Polling every <code>20s</code>", rendered)


if __name__ == "__main__":
    unittest.main()
