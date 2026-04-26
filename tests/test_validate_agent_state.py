import os
import json
import tempfile
import unittest
from pathlib import Path

from scripts.validate_agent_state import validate_state


class ValidateAgentStateTests(unittest.TestCase):
    def setUp(self) -> None:
        self._old_cwd = os.getcwd()
        self._tmpdir = tempfile.TemporaryDirectory()
        self.root = Path(self._tmpdir.name)
        os.chdir(self.root)
        Path("artifacts").mkdir()
        Path("notes").mkdir()
        Path("artifacts/report.txt").write_text("ok\n", encoding="utf-8")
        Path("notes/work.md").write_text("ok\n", encoding="utf-8")

    def tearDown(self) -> None:
        os.chdir(self._old_cwd)
        self._tmpdir.cleanup()

    def write_state(self, artifact_path: str, notes_ref: str) -> Path:
        state_path = Path("state.yaml")
        state_path.write_text(
            "\n".join(
                [
                    "schema_version: 1",
                    'last_updated: "2026-04-26T00:00:00-05:00"',
                    "continuation_window:",
                    '  floor_started_at: "2026-04-26T00:00:00-05:00"',
                    '  continue_until_at: "2026-04-26T01:00:00-05:00"',
                    '  last_checkpoint_at: "2026-04-26T00:30:00-05:00"',
                    "current_focus: {}",
                    "tooling_status: {}",
                    "latest_validated_checkpoint:",
                    '  label: "checkpoint"',
                    '  timestamp: "2026-04-26T00:30:00-05:00"',
                    '  query: "probe"',
                    "  artifacts:",
                    f"    report: {json.dumps(artifact_path)}",
                    "pending_actions: []",
                    "files_touched: []",
                    "risks: []",
                    "notes_refs:",
                    f"  work_log: {json.dumps(notes_ref)}",
                    "",
                ]
            ),
            encoding="utf-8",
        )
        return state_path

    def test_valid_existing_artifact_and_note_paths_pass(self) -> None:
        _, errors = validate_state(self.write_state("artifacts/report.txt", "notes/work.md"))

        self.assertEqual(errors, [])

    def test_artifact_path_with_control_character_reports_error(self) -> None:
        _, errors = validate_state(self.write_state("artifacts/bad\x00path.txt", "notes/work.md"))

        self.assertIn(
            "path contains control character: latest_validated_checkpoint.artifacts.report",
            errors,
        )

    def test_notes_ref_path_with_control_character_reports_error(self) -> None:
        _, errors = validate_state(self.write_state("artifacts/report.txt", "notes/bad\npath.md"))

        self.assertIn("path contains control character: notes_refs.work_log", errors)


if __name__ == "__main__":
    unittest.main()
