import json
import sqlite3
import subprocess
import tempfile
import unittest
from contextlib import closing
from pathlib import Path

from scripts.validate_android_migration_summary import validate_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_asset_pack_parity_gate.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


def write_pack(path: Path, answer_cards: int) -> None:
    path.mkdir(parents=True, exist_ok=True)
    db_path = path / "senku_mobile.sqlite3"
    with closing(sqlite3.connect(db_path)) as conn:
        conn.execute("CREATE TABLE answer_cards (id TEXT)")
        conn.executemany(
            "INSERT INTO answer_cards (id) VALUES (?)",
            [(f"card_{index}",) for index in range(answer_cards)],
        )
        conn.commit()
    (path / "senku_manifest.json").write_text(
        json.dumps({"counts": {"answer_cards": answer_cards}}),
        encoding="utf-8",
    )


class RunAndroidAssetPackParityGateTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def run_script(self, *args, check=True):
        return subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(SCRIPT),
                *args,
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=check,
        )

    def test_default_contract_uses_checked_in_asset_pack_and_fail_gate(self):
        self.assertIn(
            '[string]$BaselinePackDir = "android-app\\app\\src\\main\\assets\\mobile_pack"',
            self.script,
        )
        self.assertIn("[Parameter(Mandatory = $true)]", self.script)
        self.assertIn("[string]$CandidatePackDir", self.script)
        self.assertIn("scripts\\compare_mobile_pack_counts.py", self.script)
        self.assertIn("--fail-on-mismatch", self.script)
        self.assertIn("--output", self.script)

    def test_whatif_writes_machine_readable_summary_without_compare(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            baseline = Path(tmpdir) / "baseline"
            candidate = Path(tmpdir) / "candidate"
            output = Path(tmpdir) / "nested" / "report.json"
            write_pack(baseline, 2)
            write_pack(candidate, 3)

            result = self.run_script(
                "-BaselinePackDir",
                str(baseline),
                "-CandidatePackDir",
                str(candidate),
                "-Output",
                str(output),
                "-WhatIf",
            )
            report = json.loads(output.read_text(encoding="utf-8-sig"))

        self.assertIn("Android asset-pack parity gate dry run.", result.stdout)
        self.assertIn("scripts\\compare_mobile_pack_counts.py", result.stdout)
        self.assertIn("--fail-on-mismatch", result.stdout)
        self.assertIn("--output", result.stdout)
        self.assertIn("dry-run summary written", result.stdout)
        self.assertEqual(report["baseline_pack_dir"], str(baseline))
        self.assertEqual(report["candidate_pack_dir"], str(candidate))
        self.assertEqual(report["output"], str(output))
        self.assertIn("scripts\\compare_mobile_pack_counts.py", report["display_command"])
        self.assertEqual(report["status"], "dry_run_only")
        self.assertTrue(report["dry_run"])
        self.assertTrue(report["non_acceptance_evidence"])
        self.assertFalse(report["acceptance_evidence"])
        self.assertTrue(report["asset_pack_parity_evidence"])
        self.assertFalse(report["ui_acceptance_evidence"])
        self.assertEqual(report["evidence_kind"], "asset_pack_parity")
        self.assertEqual(report["comparison_baseline"], "fixed_four_emulator_matrix")
        self.assertEqual(report["primary_evidence"], "fixed_four_emulator_matrix")
        self.assertIn("fixed four-emulator evidence remains primary", report["stop_line"])
        self.assertIn("not UI acceptance evidence", report["stop_line"])
        self.assertFalse(report["would_run"])
        self.assertTrue(report["fail_on_mismatch"])

    def test_whatif_summary_validates_as_non_acceptance_artifact(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            baseline = Path(tmpdir) / "baseline"
            candidate = Path(tmpdir) / "candidate"
            output = Path(tmpdir) / "whatif.json"
            write_pack(baseline, 2)
            write_pack(candidate, 3)

            self.run_script(
                "-BaselinePackDir",
                str(baseline),
                "-CandidatePackDir",
                str(candidate),
                "-Output",
                str(output),
                "-WhatIf",
            )

            report, errors = validate_summary(output)

        self.assertEqual(errors, [])
        self.assertIsNotNone(report)
        self.assertTrue(report["non_acceptance_evidence"])

    def test_real_gate_writes_report_for_non_regressing_candidate(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            baseline = Path(tmpdir) / "baseline"
            candidate = Path(tmpdir) / "candidate"
            output = Path(tmpdir) / "report.json"
            write_pack(baseline, 2)
            write_pack(candidate, 3)

            result = self.run_script(
                "-BaselinePackDir",
                str(baseline),
                "-CandidatePackDir",
                str(candidate),
                "-Output",
                str(output),
            )

            report = json.loads(output.read_text(encoding="utf-8-sig"))

        self.assertIn("Android asset-pack parity report written", result.stdout)
        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["baseline_pack_dir"], str(baseline))
        self.assertEqual(report["candidate_pack_dir"], str(candidate))
        self.assertEqual(report["output"], str(output))
        self.assertTrue(report["fail_on_mismatch"])
        self.assertFalse(report["dry_run"])
        self.assertTrue(report["non_acceptance_evidence"])
        self.assertFalse(report["acceptance_evidence"])
        self.assertTrue(report["asset_pack_parity_evidence"])
        self.assertFalse(report["ui_acceptance_evidence"])
        self.assertEqual(report["evidence_kind"], "asset_pack_parity")
        self.assertEqual(report["comparison_baseline"], "fixed_four_emulator_matrix")
        self.assertEqual(report["primary_evidence"], "fixed_four_emulator_matrix")
        self.assertIn("fixed four-emulator evidence remains primary", report["stop_line"])
        self.assertIn("not UI acceptance evidence", report["stop_line"])
        self.assertEqual(report["candidate_highlights"]["sqlite_counts"]["answer_cards"], 3)
        self.assertEqual(
            report["count_deltas"],
            [{"name": "answer_cards", "baseline": 2, "candidate": 3, "delta": 1}],
        )

    def test_real_gate_summary_validates_as_non_acceptance_artifact(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            baseline = Path(tmpdir) / "baseline"
            candidate = Path(tmpdir) / "candidate"
            output = Path(tmpdir) / "report.json"
            write_pack(baseline, 2)
            write_pack(candidate, 3)

            self.run_script(
                "-BaselinePackDir",
                str(baseline),
                "-CandidatePackDir",
                str(candidate),
                "-Output",
                str(output),
            )

            report, errors = validate_summary(output)

        self.assertEqual(errors, [])
        self.assertIsNotNone(report)
        self.assertFalse(report["dry_run"])
        self.assertTrue(report["non_acceptance_evidence"])
        self.assertTrue(report["asset_pack_parity_evidence"])
        self.assertFalse(report["ui_acceptance_evidence"])

    def test_real_gate_writes_fail_wrapper_before_returning_failure(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            baseline = Path(tmpdir) / "baseline"
            candidate = Path(tmpdir) / "candidate"
            output = Path(tmpdir) / "report.json"
            write_pack(baseline, 3)
            write_pack(candidate, 2)

            result = self.run_script(
                "-BaselinePackDir",
                str(baseline),
                "-CandidatePackDir",
                str(candidate),
                "-Output",
                str(output),
                check=False,
            )

            report = json.loads(output.read_text(encoding="utf-8-sig"))

        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(report["status"], "fail")
        self.assertTrue(report["fail_on_mismatch"])
        self.assertEqual(report["candidate_highlights"]["sqlite_counts"]["answer_cards"], 2)
        self.assertEqual(
            report["count_deltas"],
            [{"name": "answer_cards", "baseline": 3, "candidate": 2, "delta": -1}],
        )

    def test_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_asset_pack_parity_gate.ps1",
                "-SkipAnalyzer",
                "-SkipPester",
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)


if __name__ == "__main__":
    unittest.main()
