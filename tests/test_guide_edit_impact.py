import importlib.util
import json
import subprocess
import sys
import unittest
from pathlib import Path
from unittest import mock


def load_module():
    module_path = Path(__file__).resolve().parents[1] / "scripts" / "guide_edit_impact.py"
    spec = importlib.util.spec_from_file_location("guide_edit_impact", module_path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


class GuideEditImpactTests(unittest.TestCase):
    def test_classifies_known_guide_as_guide_and_frontmatter(self):
        module = load_module()

        change = module.classify_path("guides/first-aid.md")

        self.assertIn("guide", change.categories)
        self.assertIn("frontmatter", change.categories)
        self.assertTrue(change.exists)

    def test_classifies_known_card_as_card_and_note(self):
        module = load_module()

        change = module.classify_path(
            "notes/specs/guide_answer_cards/choking_airway_obstruction.yaml"
        )

        self.assertEqual(change.categories, ("card", "note"))
        self.assertTrue(change.exists)

    def test_build_plan_suggests_guide_card_and_script_validations(self):
        module = load_module()

        plan = module.build_plan(
            [
                "guides/first-aid.md",
                "notes/specs/guide_answer_cards/choking_airway_obstruction.yaml",
                "scripts/scan_corpus_markers.py",
            ]
        )
        commands = [item["command"] for item in plan["commands"]]

        self.assertIn(
            "& .\\.venvs\\senku-validate\\Scripts\\python.exe -B scripts\\plan_incremental_ingest.py guides\\first-aid.md",
            commands,
        )
        self.assertIn(
            "& .\\.venvs\\senku-validate\\Scripts\\python.exe -B scripts\\scan_corpus_markers.py --fail-on-unresolved",
            commands,
        )
        self.assertIn(
            "& .\\.venvs\\senku-validate\\Scripts\\python.exe -B scripts\\validate_guide_answer_cards.py",
            commands,
        )
        self.assertIn(
            "& .\\.venvs\\senku-validate\\Scripts\\python.exe -B -m unittest tests.test_scan_corpus_markers -v",
            commands,
        )

    def test_json_cli_output_is_parseable(self):
        module = load_module()

        with mock.patch("builtins.print") as mocked_print:
            exit_code = module.main(["--json", "guides/first-aid.md"])

        self.assertEqual(exit_code, 0)
        payload = json.loads(mocked_print.call_args.args[0])
        self.assertEqual(payload["changed_files"][0]["path"], "guides/first-aid.md")

    def test_from_git_status_handles_renames_and_untracked_files(self):
        module = load_module()
        completed = subprocess.CompletedProcess(
            args=["git", "status", "--short"],
            returncode=0,
            stdout=(
                " M guides/first-aid.md\n"
                "R  guides/old.md -> guides/new.md\n"
                "?? notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md\n"
            ),
            stderr="",
        )

        with mock.patch.object(module.subprocess, "run", return_value=completed):
            paths = module.read_git_status_paths()

        self.assertEqual(
            paths,
            [
                "guides/first-aid.md",
                "guides/new.md",
                "notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md",
            ],
        )


if __name__ == "__main__":
    unittest.main()
