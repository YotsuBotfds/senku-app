import tempfile
import unittest
from contextlib import redirect_stdout
from io import StringIO
from pathlib import Path

from scripts.plan_incremental_ingest import (
    ChangedPath,
    build_plan,
    collect_git_changes,
    main,
    render_plan,
)


class IncrementalIngestPlannerTests(unittest.TestCase):
    def make_root(self):
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        root = Path(tmpdir.name)
        (root / "guides").mkdir()
        (root / "notes" / "specs").mkdir(parents=True)
        return root

    def test_explicit_paths_plan_incremental_ingest_and_warn_for_non_guides(self):
        root = self.make_root()
        (root / "guides" / "water.md").write_text("# guide\n", encoding="utf-8")
        (root / "notes" / "specs" / "card.yaml").write_text("id: card\n", encoding="utf-8")
        (root / "query.py").write_text("# code\n", encoding="utf-8")

        plan = build_plan(
            ["guides/water.md", "notes/specs/card.yaml", "query.py"],
            root=root,
        )

        self.assertEqual(plan["guide_files"], ["guides/water.md"])
        self.assertEqual(plan["spec_files"], ["notes/specs/card.yaml"])
        self.assertEqual(plan["other_files"], ["query.py"])
        self.assertEqual(
            plan["commands"][0],
            r"& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\water.md",
        )
        self.assertEqual(
            plan["commands"][1],
            r"& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --stats",
        )
        self.assertTrue(any("Spec files" in warning for warning in plan["warnings"]))
        self.assertTrue(any("Non-guide files" in warning for warning in plan["warnings"]))

    def test_explicit_basename_resolves_to_existing_guide_and_force_files(self):
        root = self.make_root()
        (root / "guides" / "burns.md").write_text("# guide\n", encoding="utf-8")

        plan = build_plan(["burns.md"], root=root, force_files=True)

        self.assertEqual(plan["guide_files"], ["guides/burns.md"])
        self.assertIn("--force-files", plan["commands"][0])

    def test_deleted_guide_uses_full_rebuild_guidance(self):
        root = self.make_root()

        def fake_git_runner(args, cwd):
            if args[0] == "diff":
                return "D\tguides/removed.md\n"
            if args[0] == "ls-files":
                return ""
            raise AssertionError(args)

        plan = build_plan(root=root, git_runner=fake_git_runner)

        self.assertEqual(plan["guide_files"], [])
        self.assertEqual(plan["deleted_guide_files"], ["guides/removed.md"])
        self.assertEqual(
            plan["commands"][0],
            r"& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --rebuild",
        )
        self.assertTrue(any("Deleted or missing guide" in warning for warning in plan["warnings"]))

    def test_collect_git_changes_includes_renames_and_untracked_paths(self):
        root = self.make_root()

        def fake_git_runner(args, cwd):
            self.assertEqual(cwd, root)
            if args[0] == "diff":
                return "R100\tguides/old.md\tguides/new.md\nM\tnotes/specs/rag.yaml\n"
            if args[0] == "ls-files":
                return "guides/untracked.md\n"
            raise AssertionError(args)

        changes = collect_git_changes(root, git_runner=fake_git_runner)

        self.assertEqual(
            changes,
            [
                ChangedPath("guides/old.md", "D"),
                ChangedPath("guides/new.md", "R"),
                ChangedPath("notes/specs/rag.yaml", "M"),
                ChangedPath("guides/untracked.md", "??"),
            ],
        )

    def test_render_plan_mentions_dry_run_and_existing_collection(self):
        root = self.make_root()
        (root / "guides" / "water.md").write_text("# guide\n", encoding="utf-8")

        text = render_plan(build_plan(["guides/water.md"], root=root))

        self.assertIn("dry run only", text)
        self.assertIn("existing collection", text)
        self.assertIn("guides\\water.md", text)

    def test_main_prints_json(self):
        root = self.make_root()
        (root / "guides" / "water.md").write_text("# guide\n", encoding="utf-8")

        stdout = StringIO()
        with redirect_stdout(stdout):
            rc = main(["--root", str(root), "--json", "guides/water.md"])

        self.assertEqual(rc, 0)
        self.assertIn('"guide_files"', stdout.getvalue())


if __name__ == "__main__":
    unittest.main()
