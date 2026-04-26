import contextlib
import io
import tempfile
import unittest
from pathlib import Path
from unittest import mock

from scripts import validate_special_cases


class ValidateSpecialCasesOutputTests(unittest.TestCase):
    def test_overlap_matrix_rows_reject_malformed_match_metadata(self):
        malformed_overlaps = [
            {
                "source_rule_id": "source",
                "sample_prompt": "prompt",
                "matches": [
                    {
                        "rule_id": "source",
                        "priority": 10,
                        "promotion_status": "stable",
                    }
                ],
                "winner_rule_ids": ["source"],
                "winner_reason": "priority",
            }
        ]

        with self.assertRaisesRegex(
            ValueError,
            "malformed overlap match 0.0 for source: missing lexical_signature_size",
        ):
            validate_special_cases.build_overlap_matrix_rows(malformed_overlaps)

    def test_malformed_overlap_matrix_rows_return_clean_cli_failure(self):
        malformed_overlaps = [
            {
                "source_rule_id": "source",
                "sample_prompt": "prompt",
                "matches": [{"rule_id": "source"}],
                "winner_rule_ids": ["source"],
                "winner_reason": "priority",
            }
        ]

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
            return_value=malformed_overlaps,
        ), contextlib.redirect_stderr(io.StringIO()) as stderr:
            exit_code = validate_special_cases.main([])

        self.assertEqual(exit_code, 2)
        error_text = stderr.getvalue()
        self.assertIn("failed to build overlap matrix rows", error_text)
        self.assertIn("malformed overlap match 0.0 for source", error_text)

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
