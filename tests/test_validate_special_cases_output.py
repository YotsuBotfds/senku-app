import contextlib
import io
import tempfile
import unittest
from pathlib import Path
from unittest import mock

from scripts import validate_special_cases


class ValidateSpecialCasesOutputTests(unittest.TestCase):
    def test_overlap_matrix_json_write_error_returns_clean_cli_failure(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            output_dir = Path(tmpdir) / "overlap_matrix.json"
            output_dir.mkdir()

            with mock.patch.object(
                validate_special_cases.query,
                "get_deterministic_special_case_rules",
                return_value=[],
            ), mock.patch.object(
                validate_special_cases,
                "all_guide_ids",
                return_value=set(),
            ), mock.patch.object(
                validate_special_cases.query,
                "get_deterministic_special_case_overlaps",
                return_value=[],
            ), contextlib.redirect_stderr(io.StringIO()) as stderr:
                exit_code = validate_special_cases.main(
                    ["--overlap-matrix-json", str(output_dir)]
                )

        self.assertEqual(exit_code, 2)
        error_text = stderr.getvalue()
        self.assertIn("failed to write --overlap-matrix-json", error_text)
        self.assertIn(str(output_dir), error_text)


if __name__ == "__main__":
    unittest.main()
