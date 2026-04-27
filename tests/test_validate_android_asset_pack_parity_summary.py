import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_asset_pack_parity_summary import validate_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_asset_pack_parity_summary.py"


def make_summary() -> dict:
    return {
        "status": "dry_run_only",
        "baseline_pack_dir": "C:\\repo\\android-app\\app\\src\\main\\assets\\mobile_pack",
        "candidate_pack_dir": "C:\\tmp\\candidate_pack",
        "output": "C:\\tmp\\android_asset_pack_parity_gate.json",
        "fail_on_mismatch": True,
        "dry_run": True,
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "asset_pack_parity_evidence": True,
        "ui_acceptance_evidence": False,
        "evidence_kind": "asset_pack_parity",
        "comparison_baseline": "fixed_four_emulator_matrix",
        "primary_evidence": "fixed_four_emulator_matrix",
        "stop_line": (
            "STOP: fixed four-emulator evidence remains primary; "
            "this asset-pack parity gate is non-acceptance evidence only, "
            "not UI acceptance evidence."
        ),
        "display_command": "& python -B scripts\\compare_mobile_pack_counts.py",
        "would_run": False,
    }


class ValidateAndroidAssetPackParitySummaryTests(unittest.TestCase):
    def write_summary(self, payload: object) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_whatif_summary_passes(self):
        data, errors = validate_summary(self.write_summary(make_summary()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["status"], "dry_run_only")
        self.assertTrue(data["dry_run"])

    def test_valid_real_run_summary_passes_with_optional_summary_markdown(self):
        summary = make_summary()
        summary["status"] = "pass"
        summary["dry_run"] = False
        del summary["would_run"]
        summary["summary_markdown"] = "C:\\tmp\\android_asset_pack_parity_gate.md"

        data, errors = validate_summary(self.write_summary(summary))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertFalse(data["dry_run"])

    def test_real_run_summary_markdown_is_optional(self):
        summary = make_summary()
        summary["status"] = "fail"
        summary["dry_run"] = False
        del summary["would_run"]

        _, errors = validate_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_rejects_missing_paths_and_wrong_evidence_flags(self):
        summary = make_summary()
        del summary["baseline_pack_dir"]
        summary["candidate_pack_dir"] = ""
        summary["output"] = ""
        summary["fail_on_mismatch"] = False
        summary["non_acceptance_evidence"] = False
        summary["acceptance_evidence"] = True
        summary["asset_pack_parity_evidence"] = False
        summary["ui_acceptance_evidence"] = True
        summary["evidence_kind"] = "ui_acceptance"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("missing root.baseline_pack_dir", errors)
        self.assertIn("expected root.candidate_pack_dir to be non-empty", errors)
        self.assertIn("expected root.output to be non-empty", errors)
        self.assertIn("expected root.fail_on_mismatch to be True", errors)
        self.assertIn("expected root.non_acceptance_evidence to be True", errors)
        self.assertIn("expected root.acceptance_evidence to be False", errors)
        self.assertIn("expected root.asset_pack_parity_evidence to be True", errors)
        self.assertIn("expected root.ui_acceptance_evidence to be False", errors)
        self.assertIn("expected root.evidence_kind to be 'asset_pack_parity'", errors)

    def test_rejects_non_fixed_four_baseline(self):
        summary = make_summary()
        summary["comparison_baseline"] = "asset_pack_counts"
        summary["primary_evidence"] = "asset_pack_counts"

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.comparison_baseline to be 'fixed_four_emulator_matrix'", errors)
        self.assertIn("expected root.primary_evidence to be 'fixed_four_emulator_matrix'", errors)

    def test_rejects_status_dry_run_mismatch(self):
        summary = make_summary()
        summary["status"] = "pass"
        summary["dry_run"] = True

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.dry_run to be false for real-run summaries", errors)

    def test_rejects_whatif_summary_markdown_and_would_run_true(self):
        summary = make_summary()
        summary["summary_markdown"] = "C:\\tmp\\summary.md"
        summary["would_run"] = True

        _, errors = validate_summary(self.write_summary(summary))

        self.assertIn("expected root.summary_markdown to be absent for dry-run summaries", errors)
        self.assertIn("expected root.would_run to be false for dry-run summaries", errors)

    def test_cli_reports_failure(self):
        summary = make_summary()
        summary["dry_run"] = False
        path = self.write_summary(summary)
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 1)
        self.assertIn("ERROR: expected root.dry_run to be true when root.status is 'dry_run_only'", result.stdout)


if __name__ == "__main__":
    unittest.main()
