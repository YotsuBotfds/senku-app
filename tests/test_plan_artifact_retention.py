import json
import os
import shutil
import tempfile
import subprocess
import unittest
from contextlib import redirect_stdout
from io import StringIO
from datetime import datetime, timezone
from pathlib import Path

from scripts.plan_artifact_retention import (
    infer_family_path,
    main,
    normalize_family_group,
    plan_artifact_retention,
    render_markdown,
)


NOW = datetime(2026, 4, 25, 12, 0, tzinfo=timezone.utc)


class ArtifactRetentionPlannerTests(unittest.TestCase):
    def _init_temporary_git_repo(self, root: Path) -> None:
        subprocess.run(
            ["git", "init"],
            cwd=root,
            check=True,
            capture_output=True,
            text=True,
        )
        subprocess.run(
            ["git", "config", "user.email", "retention-tester@example.test"],
            cwd=root,
            check=True,
            capture_output=True,
            text=True,
        )
        subprocess.run(
            ["git", "config", "user.name", "Retention Tester"],
            cwd=root,
            check=True,
            capture_output=True,
            text=True,
        )

    def make_tmpdir(self):
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def write_bytes(self, path: Path, size: int, *, age_days: int = 0) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(b"x" * size)
        self.set_age(path, age_days)

    def write_text(self, path: Path, text: str, *, age_days: int = 0) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")
        self.set_age(path, age_days)

    def set_age(self, path: Path, age_days: int) -> None:
        ts = NOW.timestamp() - age_days * 86400
        os.utime(path, (ts, ts))

    def test_dispatch_reference_protects_whole_artifact_family(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        notes = root / "notes" / "dispatch"
        self.write_text(artifacts / "bench" / "important_run" / "report.md", "# proof\n", age_days=80)
        self.write_text(
            notes / "RAG-X.md",
            "Proof lives at `artifacts/bench/important_run/report.md`.\n",
        )

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[notes],
            manifest_paths=[],
            archive_after_days=1,
            delete_after_days=1,
            now=NOW,
        )

        families = {row["path"]: row for row in plan["families"]}
        row = families["bench/important_run"]
        self.assertTrue(row["protected"])
        self.assertEqual(row["action"], "keep_protected")
        self.assertIn(notes.as_posix() + "/RAG-X.md", row["protection_sources"])

    def test_planner_handoff_reference_notes_are_ignored_when_untracked(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        notes = root / "notes"

        self.write_text(
            artifacts / "bench" / "handoff_run" / "report.md",
            "# proof\n",
            age_days=80,
        )
        self.write_text(
            artifacts / "bench" / "ordinary_run" / "report.md",
            "# proof\n",
            age_days=80,
        )
        self.write_text(
            notes / "PLANNER_HANDOFF_2026-04-26_TEST.md",
            "Handoff artifact: `artifacts/bench/handoff_run/report.md`\n",
        )
        self.write_text(
            notes / "notes_index.md",
            "Current artifact: `artifacts/bench/ordinary_run/report.md`\n",
        )

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[notes],
            manifest_paths=[],
            archive_after_days=1,
            delete_after_days=1,
            now=NOW,
        )

        families = {row["path"]: row for row in plan["families"]}
        handoff_row = families["bench/handoff_run"]
        ordinary_row = families["bench/ordinary_run"]
        self.assertFalse(handoff_row["protected"])
        self.assertNotIn(notes.as_posix() + "/PLANNER_HANDOFF_2026-04-26_TEST.md", handoff_row["protection_sources"])
        self.assertTrue(ordinary_row["protected"])
        self.assertIn(notes.as_posix() + "/notes_index.md", ordinary_row["protection_sources"])
        self.assertIn(notes.as_posix() + "/notes_index.md", plan["references"]["sources"])
        self.assertNotIn(
            notes.as_posix() + "/PLANNER_HANDOFF_2026-04-26_TEST.md",
            plan["references"]["sources"],
        )

    @unittest.skipIf(shutil.which("git") is None, "git is required for committed handoff retention regression")
    def test_tracked_planner_handoff_reference_notes_can_protect_artifacts(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        notes = root / "notes"
        handoff_file = notes / "PLANNER_HANDOFF_2026-04-26_TEST.md"
        untracked_handoff = notes / "PLANNER_HANDOFF_2026-04-26_UNTRACKED.md"

        self.write_text(
            artifacts / "bench" / "handoff_run" / "report.md",
            "# proof\n",
            age_days=80,
        )
        self.write_text(
            artifacts / "bench" / "untracked_handoff_run" / "report.md",
            "# proof\n",
            age_days=80,
        )
        self.write_text(
            handoff_file,
            "Handoff artifact: `artifacts/bench/handoff_run/report.md`\n",
        )
        self.write_text(
            untracked_handoff,
            "Handoff artifact: `artifacts/bench/untracked_handoff_run/report.md`\n",
        )

        self._init_temporary_git_repo(root)
        subprocess.run(
            ["git", "add", "notes/PLANNER_HANDOFF_2026-04-26_TEST.md"],
            cwd=root,
            check=True,
            capture_output=True,
            text=True,
        )
        subprocess.run(
            ["git", "commit", "-m", "Seed tracked planner handoff"],
            cwd=root,
            check=True,
            capture_output=True,
            text=True,
        )

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[notes],
            manifest_paths=[],
            archive_after_days=1,
            delete_after_days=1,
            now=NOW,
        )

        families = {row["path"]: row for row in plan["families"]}
        handoff_row = families["bench/handoff_run"]
        untracked_handoff_row = families["bench/untracked_handoff_run"]
        self.assertTrue(handoff_row["protected"])
        self.assertEqual(handoff_row["action"], "keep_protected")
        self.assertIn(notes.as_posix() + "/PLANNER_HANDOFF_2026-04-26_TEST.md", handoff_row["protection_sources"])
        self.assertIn(notes.as_posix() + "/PLANNER_HANDOFF_2026-04-26_TEST.md", plan["references"]["sources"])
        self.assertFalse(untracked_handoff_row["protected"])
        self.assertNotIn(
            notes.as_posix() + "/PLANNER_HANDOFF_2026-04-26_UNTRACKED.md",
            untracked_handoff_row["protection_sources"],
        )
        self.assertNotIn(
            notes.as_posix() + "/PLANNER_HANDOFF_2026-04-26_UNTRACKED.md",
            plan["references"]["sources"],
        )

    @unittest.skipIf(shutil.which("git") is None, "git is required for staged handoff retention regression")
    def test_staged_planner_handoff_reference_notes_remain_unprotected(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        notes = root / "notes"
        handoff_file = notes / "PLANNER_HANDOFF_2026-04-26_STAGED.md"

        self.write_text(
            artifacts / "bench" / "staged_handoff_run" / "report.md",
            "# proof\n",
            age_days=80,
        )
        self.write_text(
            handoff_file,
            "Staged handoff artifact: `artifacts/bench/staged_handoff_run/report.md`\n",
        )

        self._init_temporary_git_repo(root)
        subprocess.run(
            ["git", "add", "notes/PLANNER_HANDOFF_2026-04-26_STAGED.md"],
            cwd=root,
            check=True,
            capture_output=True,
            text=True,
        )

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[notes],
            manifest_paths=[],
            archive_after_days=1,
            delete_after_days=1,
            now=NOW,
        )

        row = {row["path"]: row for row in plan["families"]}["bench/staged_handoff_run"]
        self.assertFalse(row["protected"])
        self.assertNotEqual(row["action"], "keep_protected")
        self.assertNotIn(handoff_file.as_posix(), row["protection_sources"])
        self.assertNotIn(handoff_file.as_posix(), plan["references"]["sources"])

    def test_explicit_protect_path_and_glob_are_honored(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        self.write_bytes(artifacts / "bench" / "path_run" / "report.md", 4, age_days=80)
        self.write_bytes(artifacts / "bench" / "glob_run" / "report.md", 4, age_days=80)

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[],
            manifest_paths=[],
            protect_paths=["artifacts/bench/path_run/report.md"],
            protect_globs=["bench/glob_*"],
            archive_after_days=1,
            delete_after_days=1,
            now=NOW,
        )

        families = {row["path"]: row for row in plan["families"]}
        self.assertEqual(families["bench/path_run"]["action"], "keep_protected")
        self.assertIn("explicit:protect-path", families["bench/path_run"]["protection_sources"])
        self.assertEqual(families["bench/glob_run"]["action"], "keep_protected")
        self.assertIn(
            "explicit:protect-glob:bench/glob_*",
            families["bench/glob_run"]["protection_sources"],
        )

    def test_manifest_reference_protects_artifact_family(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        manifest = root / "artifacts" / "runs" / "run_manifest.jsonl"
        self.write_bytes(artifacts / "bench" / "manifest_run" / "summary.json", 7, age_days=90)
        self.write_text(
            manifest,
            json.dumps({"output": ["artifacts/bench/manifest_run"]}) + "\n",
        )

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[],
            manifest_paths=[manifest],
            archive_after_days=1,
            delete_after_days=1,
            now=NOW,
        )

        row = {row["path"]: row for row in plan["families"]}["bench/manifest_run"]
        self.assertTrue(row["protected"])
        self.assertEqual(row["action"], "keep_protected")
        self.assertIn(manifest.as_posix(), row["protection_sources"])

    def test_run_manifest_artifact_path_evidence_protects_artifact_family(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        manifest = root / "artifacts" / "runs" / "run_manifest.jsonl"
        self.write_bytes(
            artifacts / "bench" / "evidence_run" / "summary.json",
            7,
            age_days=90,
        )
        self.write_text(
            manifest,
            json.dumps(
                {
                    "artifact_path_evidence": [
                        {
                            "path": "artifacts/bench/evidence_run",
                            "exists": True,
                            "kind": "directory",
                        },
                        {
                            "path": "artifacts/bench/missing_run/report.md",
                            "exists": False,
                            "kind": "missing",
                        },
                    ],
                }
            )
            + "\n",
        )

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[],
            manifest_paths=[manifest],
            archive_after_days=1,
            delete_after_days=1,
            now=NOW,
        )

        row = {row["path"]: row for row in plan["families"]}["bench/evidence_run"]
        self.assertTrue(row["protected"])
        self.assertEqual(row["action"], "keep_protected")
        self.assertIn(manifest.as_posix(), row["protection_sources"])

    def test_malformed_manifest_lines_do_not_abort_retention_planning(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        manifest = root / "artifacts" / "runs" / "run_manifest.jsonl"
        self.write_bytes(
            artifacts / "bench" / "evidence_run" / "summary.json",
            7,
            age_days=90,
        )
        self.write_bytes(
            artifacts / "bench" / "reference_run" / "summary.json",
            7,
            age_days=90,
        )

        manifest_lines = [
            "{not valid json",  # malformed, no artifact refs
            json.dumps(
                {
                    "artifact_path_evidence": [
                        {
                            "path": "artifacts/bench/evidence_run",
                            "exists": True,
                            "kind": "directory",
                        },
                    ]
                }
            ),
            json.dumps({"output": ["artifacts/bench/reference_run/summary.json"]}),
            "{",  # malformed, no artifact refs
        ]
        self.write_text(manifest, "\n".join(manifest_lines))

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[],
            manifest_paths=[manifest],
            archive_after_days=1,
            delete_after_days=1,
            now=NOW,
        )

        families = {row["path"]: row for row in plan["families"]}
        self.assertTrue(families["bench/evidence_run"]["protected"])
        self.assertEqual(families["bench/evidence_run"]["action"], "keep_protected")
        self.assertIn(manifest.as_posix(), families["bench/evidence_run"]["protection_sources"])
        self.assertTrue(families["bench/reference_run"]["protected"])
        self.assertEqual(families["bench/reference_run"]["action"], "keep_protected")
        self.assertIn(manifest.as_posix(), families["bench/reference_run"]["protection_sources"])
        self.assertIn(manifest.as_posix(), plan["references"]["sources"])
        self.assertEqual(plan["summary"]["reference_count"], 2)

    def test_manifest_reference_with_parent_traversal_does_not_protect_prefix_family(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        manifest = root / "artifacts" / "runs" / "run_manifest.jsonl"
        self.write_bytes(
            artifacts / "bench" / "old_run" / "summary.json",
            7,
            age_days=90,
        )
        self.write_text(
            manifest,
            json.dumps({"output": ["artifacts/bench/old_run/../new_run/summary.json"]}) + "\n",
        )

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[],
            manifest_paths=[manifest],
            archive_after_days=1,
            delete_after_days=1,
            now=NOW,
        )

        row = {row["path"]: row for row in plan["families"]}["bench/old_run"]
        self.assertFalse(row["protected"])
        self.assertNotEqual(row["action"], "keep_protected")
        self.assertNotIn(manifest.as_posix(), row["protection_sources"])
        self.assertEqual(plan["summary"]["reference_count"], 0)

    def test_groups_generated_families_by_timestamp_normalized_name(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        self.write_bytes(
            artifacts / "bench" / "rag_diagnostics_20260425_1714_case" / "report.md",
            3,
        )
        self.write_bytes(
            artifacts / "bench" / "rag_diagnostics_20260426_0940_case" / "summary.json",
            5,
        )

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[],
            manifest_paths=[],
            now=NOW,
        )

        families = {row["path"]: row for row in plan["families"]}
        self.assertEqual(infer_family_path("bench/run_a/report.md"), "bench/run_a")
        self.assertEqual(
            normalize_family_group("bench/rag_diagnostics_20260425_1714_case"),
            "bench/rag_diagnostics_<date>_case",
        )
        self.assertEqual(
            families["bench/rag_diagnostics_20260425_1714_case"]["family_group"],
            families["bench/rag_diagnostics_20260426_0940_case"]["family_group"],
        )

    def test_candidate_classification_uses_age_size_and_name_patterns(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        self.write_bytes(artifacts / "bench" / "old_diagnostics_run" / "report.md", 10, age_days=20)
        self.write_bytes(artifacts / "tmp_cache" / "payload.bin", 8, age_days=60)
        self.write_bytes(artifacts / "bench" / "large_recent_run" / "blob.bin", 128, age_days=0)

        plan = plan_artifact_retention(
            artifacts,
            reference_roots=[],
            manifest_paths=[],
            archive_after_days=14,
            delete_after_days=45,
            large_family_bytes=64,
            now=NOW,
        )

        families = {row["path"]: row for row in plan["families"]}
        self.assertEqual(families["bench/old_diagnostics_run"]["action"], "archive_candidate")
        self.assertIn("older_than_archive_threshold", families["bench/old_diagnostics_run"]["reasons"])
        self.assertIn(
            "archive_name_pattern:diagnostic",
            families["bench/old_diagnostics_run"]["reasons"],
        )
        self.assertEqual(families["tmp_cache"]["action"], "delete_candidate")
        self.assertIn("older_than_delete_threshold", families["tmp_cache"]["reasons"])
        self.assertEqual(families["bench/large_recent_run"]["action"], "archive_candidate")
        self.assertIn(
            "larger_than_large_family_threshold",
            families["bench/large_recent_run"]["reasons"],
        )

    def test_main_writes_json_and_markdown_reports(self):
        root = self.make_tmpdir()
        artifacts = root / "artifacts"
        output_json = root / "out" / "plan.json"
        output_md = root / "out" / "plan.md"
        self.write_bytes(artifacts / "bench" / "old_run" / "report.md", 4, age_days=20)

        stdout = StringIO()
        with redirect_stdout(stdout):
            rc = main(
                [
                    "--root",
                    str(artifacts),
                    "--reference-root",
                    str(root / "missing_notes"),
                    "--manifest",
                    str(root / "missing_manifest.jsonl"),
                    "--archive-after-days",
                    "1",
                    "--delete-after-days",
                    "99",
                    "--output-json",
                    str(output_json),
                    "--output-md",
                    str(output_md),
                    "--json",
                ]
            )

        self.assertEqual(rc, 0)
        payload = json.loads(output_json.read_text(encoding="utf-8"))
        self.assertTrue(payload["dry_run"])
        self.assertEqual(payload["families"][0]["path"], "bench/old_run")
        markdown = output_md.read_text(encoding="utf-8")
        self.assertIn("# Artifact Retention Plan", markdown)
        self.assertIn("report-only dry run", markdown)
        self.assertIn("bench/old_run", markdown)
        self.assertIn("never deletes", render_markdown(payload))

    def test_markdown_report_sanitizes_malformed_path_cells(self):
        plan = {
            "dry_run": True,
            "root": "artifacts",
            "exists": True,
            "generated_at": NOW.isoformat(timespec="seconds"),
            "config": {"report_limit": 10},
            "summary": {
                "family_count": 1,
                "total_bytes": 1,
                "protected_bytes": 0,
                "candidate_bytes": 1,
                "action_counts": {"archive_candidate": 1},
                "reference_count": 0,
                "reference_source_count": 0,
            },
            "references": {"paths": {}, "sources": []},
            "families": [
                {
                    "action": "archive_candidate",
                    "path": "bench/bad`<path>",
                    "family_group": "bench/bad`<group>",
                    "bytes": 1,
                    "files": 1,
                    "dirs": 0,
                    "oldest_mtime": None,
                    "newest_mtime": None,
                    "age_days": 99,
                    "markers": [],
                    "examples": [],
                    "protected": False,
                    "protection_sources": [],
                    "reasons": ["archive_name_pattern:report|pipe", "line\rbreak"],
                }
            ],
            "candidates": [],
        }
        plan["candidates"] = plan["families"]

        markdown = render_markdown(plan)

        self.assertIn("<code>bench/bad`&lt;path&gt;</code>", markdown)
        self.assertIn("<code>bench/bad`&lt;group&gt;</code>", markdown)
        self.assertIn("archive_name_pattern:report\\|pipe", markdown)
        self.assertNotIn("`bench/bad`<path>`", markdown)


if __name__ == "__main__":
    unittest.main()
