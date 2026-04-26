import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from scripts import validate_prompt_expectations as validator


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_prompt_expectations.py"


class PromptExpectationValidatorTests(unittest.TestCase):
    def make_tmpdir(self) -> Path:
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def write_guide(self, root: Path, guide_id: str, slug: str) -> None:
        guides = root / "guides"
        guides.mkdir(exist_ok=True)
        (guides / f"{slug}.md").write_text(
            "---\n"
            f"id: {guide_id}\n"
            f"slug: {slug}\n"
            f"title: {slug.replace('-', ' ').title()}\n"
            "---\n\n"
            "Body.\n",
            encoding="utf-8",
        )

    def test_jsonl_and_csv_validate_known_guides_and_unique_prompt_ids(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        jsonl_pack = root / "pack.jsonl"
        jsonl_pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "guide_id": "GD-397",
                    "prompt": "How do I sharpen a dull blade?",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        csv_pack = root / "pack.csv"
        csv_pack.write_text(
            "id,expected_guide_ids,prompt\n"
            "P-2,GD-397,Second prompt\n",
            encoding="utf-8",
        )

        report = validator.validate(
            [jsonl_pack, csv_pack],
            guides_dir=root / "guides",
            root=root,
        )

        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["summary"]["prompts_checked"], 2)
        self.assertEqual(report["summary"]["expected_owner_rows"], 2)
        self.assertEqual(report["issues"], [])

    def test_flags_duplicate_malformed_unknown_and_field_disagreement(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "RE2-UP-009",
                    "guide_id": "GD-397",
                    "expected_guide_ids": "GD-120",
                    "prompt": "Sharpen a dull field blade.",
                }
            )
            + "\n"
            + json.dumps(
                {
                    "id": "DUP",
                    "guide_id": "GD-12",
                    "prompt": "Malformed guide id.",
                }
            )
            + "\n"
            + json.dumps(
                {
                    "id": "DUP",
                    "guide_id": "GD-999",
                    "prompt": "Unknown guide id.",
                }
            )
            + "\n",
            encoding="utf-8",
        )

        report = validator.validate([pack], guides_dir=root / "guides", root=root)
        codes = {item["code"] for item in report["issues"]}

        self.assertEqual(report["status"], "fail")
        self.assertIn("expectation_field_disagreement", codes)
        self.assertIn("malformed_expected_guide_id", codes)
        self.assertIn("expected_guide_field_without_guide_id", codes)
        self.assertIn("unknown_expected_guide_id", codes)
        self.assertIn("duplicate_prompt_id", codes)

    def test_allowed_drift_manifest_suppresses_matching_issue(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "RE2-UP-009",
                    "guide_id": "GD-397",
                    "expected_guide_ids": "GD-120",
                    "prompt": "Sharpen a dull field blade.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        manifest = root / "allowed_drift.json"
        manifest.write_text(
            json.dumps(
                {
                    "allowed_drift": [
                        {
                            "prompt_id": "RE2-UP-009",
                            "issue_codes": ["expectation_field_disagreement"],
                            "guide_ids": ["GD-120", "GD-397"],
                            "reason": "fixture suppression",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            allowed_drift_manifest=manifest,
        )

        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["summary"]["suppressed_issues"], 1)
        self.assertEqual(report["suppressed_issues"][0]["suppression_reason"], "fixture suppression")

    def test_retrieval_eval_json_flags_expected_owner_absent_from_top_k(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "RE2-UP-009",
                    "guide_id": "GD-397",
                    "prompt": "Sharpen a dull field blade.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        retrieval = root / "retrieval.json"
        retrieval.write_text(
            json.dumps(
                {
                    "rows": [
                        {
                            "prompt_id": "RE2-UP-009",
                            "expected_guide_ids": ["GD-120"],
                            "top_retrieved_guide_ids": ["GD-397", "GD-111"],
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[retrieval],
            retrieval_top_k=2,
        )

        self.assertEqual(report["status"], "warn")
        self.assertEqual(report["issues"][0]["code"], "retrieval_missing_expected_owner")
        self.assertEqual(report["issues"][0]["prompt_id"], "RE2-UP-009")

    def test_retrieval_eval_markdown_parses_escaped_pipe_lists(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps({"id": "P-1", "guide_id": "GD-397", "prompt": "Prompt"}) + "\n",
            encoding="utf-8",
        )
        md = root / "retrieval.md"
        md.write_text(
            "| id | expected | top retrieved |\n"
            "| --- | --- | --- |\n"
            "| P-1 | GD-397 | GD-111\\|GD-397 |\n",
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[md],
        )

        self.assertEqual(report["status"], "pass")

    def test_cli_writes_json_and_markdown_and_fail_flags_gate_exit(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps({"id": "P-1", "guide_id": "GD-999", "prompt": "Prompt"}) + "\n",
            encoding="utf-8",
        )
        report_json = root / "report.json"
        report_md = root / "report.md"

        completed = subprocess.run(
            [
                sys.executable,
                str(SCRIPT_PATH),
                str(pack),
                "--guides-dir",
                str(root / "guides"),
                "--output-json",
                str(report_json),
                "--output-md",
                str(report_md),
                "--fail-on-errors",
            ],
            cwd=root,
            capture_output=True,
            text=True,
        )

        self.assertEqual(completed.returncode, 1)
        payload = json.loads(report_json.read_text(encoding="utf-8"))
        self.assertEqual(payload["status"], "fail")
        self.assertTrue(report_md.exists())
        self.assertIn("# Prompt Expectation Validation", report_md.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
