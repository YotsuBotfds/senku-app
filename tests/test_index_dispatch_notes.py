import importlib.util
import json
import os
import sys
import tempfile
import unittest
from pathlib import Path


def load_module():
    module_path = (
        Path(__file__).resolve().parents[1] / "scripts" / "index_dispatch_notes.py"
    )
    spec = importlib.util.spec_from_file_location("index_dispatch_notes", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


class IndexDispatchNotesTests(unittest.TestCase):
    def test_build_index_excludes_readme_and_completed_by_default(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir) / "dispatch"
            completed = root / "completed"
            completed.mkdir(parents=True)
            (root / "README.md").write_text("# Readme\n", encoding="utf-8")
            (root / "A1_first.md").write_text(
                "\n".join(
                    [
                        "# Slice A1 - First active task",
                        "",
                        "## Outcome",
                        "",
                        "- Landed `scripts/example.py` and `tests/test_example.py`.",
                    ]
                ),
                encoding="utf-8",
            )
            (completed / "B2_done.md").write_text(
                "# Slice B2 - Completed task\n", encoding="utf-8"
            )

            notes = module.build_index(root)

        self.assertEqual([note.slice_id for note in notes], ["A1"])
        self.assertEqual(notes[0].status, "active")
        self.assertEqual(notes[0].title, "First active task")
        self.assertIn("scripts/example.py", notes[0].linked_paths)
        self.assertIn("Landed `scripts/example.py`", notes[0].proof_lines[0])

    def test_include_completed_sets_status_from_location(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir) / "dispatch"
            completed = root / "completed"
            completed.mkdir(parents=True)
            (root / "A1_first.md").write_text(
                "# A1 Active title\n\n## Slice\n\nactive details\n", encoding="utf-8"
            )
            (completed / "B2_done.md").write_text(
                "# Slice B2 - Done title\n\n## Validation\n\n- Passed focused tests.\n",
                encoding="utf-8",
            )

            notes = module.build_index(root, include_completed=True)

        self.assertEqual([note.status for note in notes], ["active", "completed"])
        self.assertEqual(notes[1].slice_id, "B2")
        self.assertEqual(notes[1].title, "Done title")
        self.assertEqual(notes[1].proof_lines, ["Passed focused tests."])

    def test_render_markdown_contains_counts_and_core_columns(self):
        module = load_module()
        note = module.DispatchNote(
            slice_id="RAG-S14",
            title="Nugget Evidence Diagnostics",
            path="notes/dispatch/RAG-S14_nugget_evidence_diagnostics.md",
            status="active",
            headings=["RAG-S14 Nugget Evidence Diagnostics", "Outcome"],
            linked_paths=["scripts/analyze_rag_bench_failures.py"],
            proof_lines=["Tests cover analyzer plumbing."],
            mtime="2026-04-25T10:58:00",
        )

        rendered = module.render_markdown([note])

        self.assertIn("- Active notes: 1", rendered)
        self.assertIn("- Completed notes: 0", rendered)
        self.assertIn("| Slice | Title | Path | Updated |", rendered)
        self.assertIn("RAG-S14", rendered)
        self.assertIn("scripts/analyze_rag_bench_failures.py", rendered)

    def test_render_markdown_flattens_row_breaking_cell_fields(self):
        module = load_module()
        note = module.DispatchNote(
            slice_id="A1\rBROKEN",
            title="Title\nwith row break",
            path="notes\\dispatch\\A1.md",
            status="active",
            headings=[],
            linked_paths=[],
            proof_lines=[],
            mtime="2026-04-26T12:00:00",
        )

        rendered = module.render_markdown([note])

        self.assertIn("A1 BROKEN", rendered)
        self.assertIn("Title with row break", rendered)
        self.assertNotIn("A1\rBROKEN", rendered)
        self.assertNotIn("Title\nwith row break", rendered)

    def test_main_writes_markdown_and_json(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            cwd = Path.cwd()
            tmp_path = Path(tmpdir)
            root = tmp_path / "notes" / "dispatch"
            root.mkdir(parents=True)
            (root / "D1_task.md").write_text(
                "# Slice D1 - Task title\n\n## Proof\n\n- Validated `artifacts/out/report.md`.\n",
                encoding="utf-8",
            )
            output_md = tmp_path / "index.md"
            output_json = tmp_path / "index.json"

            try:
                os.chdir(tmp_path)
                exit_code = module.main(
                    [
                        "--dispatch-dir",
                        str(root),
                        "--output-md",
                        str(output_md),
                        "--output-json",
                        str(output_json),
                    ]
                )
            finally:
                os.chdir(cwd)

            payload = json.loads(output_json.read_text(encoding="utf-8"))
            rendered = output_md.read_text(encoding="utf-8")

            self.assertEqual(exit_code, 0)
            self.assertIn("# Dispatch Note Index", rendered)
            self.assertEqual(payload[0]["slice_id"], "D1")
            self.assertEqual(payload[0]["linked_paths"], ["artifacts/out/report.md"])


if __name__ == "__main__":
    unittest.main()
