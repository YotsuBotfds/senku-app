import subprocess
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

from scripts import validate_bench_retry_slots


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "validate_bench_retry_slots.py"


class ValidateBenchRetrySlotsTests(unittest.TestCase):
    def test_validate_retry_slots_passes_for_duplicate_url_workers(self):
        self.assertEqual([], validate_bench_retry_slots.validate_retry_slots())

    def test_validate_retry_slots_reports_label_mismatch_without_assertion(self):
        with patch(
            "scripts.validate_bench_retry_slots.build_worker_targets",
            return_value=[{"label": "shared"}, {"label": "shared"}],
        ):
            errors = validate_bench_retry_slots.validate_retry_slots()

        self.assertTrue(errors)
        self.assertIn("expected duplicate URL labels", errors[0])

    def test_cli_reports_success(self):
        result = subprocess.run(
            [sys.executable, str(SCRIPT)],
            cwd=REPO_ROOT,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=False,
        )

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("Validated duplicate-URL retry isolation", result.stdout)


if __name__ == "__main__":
    unittest.main()
