import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

import query
from scripts.validate_special_cases import build_overlap_matrix_rows


REPO_ROOT = Path(__file__).resolve().parents[1]


class RegistryOverlapTests(unittest.TestCase):
    def test_equal_priority_prefers_longer_lexical_signature(self):
        matches = [
            query.DeterministicSpecialCaseRule(
                "generic_match",
                lambda _: True,
                None,
                "generic prompt",
                "_build_generic_match",
                100,
                "candidate",
                "test fixture",
                ("water",),
            ),
            query.DeterministicSpecialCaseRule(
                "specific_match",
                lambda _: True,
                None,
                "specific prompt",
                "_build_specific_match",
                100,
                "candidate",
                "test fixture",
                ("charcoal", "sand", "water", "filter"),
            ),
        ]

        winner, winner_reason = query._select_deterministic_special_case_rule(
            matches,
            log_first_defined_tie=False,
        )

        self.assertEqual(winner.rule_id, "specific_match")
        self.assertEqual(winner_reason, "lexical_signature")

    def test_equal_priority_and_signature_warns_then_prefers_first_defined(self):
        matches = [
            query.DeterministicSpecialCaseRule(
                "first_match",
                lambda _: True,
                None,
                "first prompt",
                "_build_first_match",
                100,
                "candidate",
                "test fixture",
                ("fire", "rain"),
            ),
            query.DeterministicSpecialCaseRule(
                "second_match",
                lambda _: True,
                None,
                "second prompt",
                "_build_second_match",
                100,
                "candidate",
                "test fixture",
                ("wet", "fire"),
            ),
        ]

        with self.assertLogs("query", level="WARNING") as captured:
            winner, winner_reason = query._select_deterministic_special_case_rule(
                matches,
                log_first_defined_tie=True,
            )

        self.assertEqual(winner.rule_id, "first_match")
        self.assertEqual(winner_reason, "first_defined")
        self.assertTrue(
            any("deterministic_priority_tie" in entry for entry in captured.output)
        )

    def test_overlaps_have_single_priority_winner(self):
        overlaps = query.get_deterministic_special_case_overlaps()
        unresolved = [
            overlap
            for overlap in overlaps
            if len(overlap["winner_rule_ids"]) != 1
        ]
        self.assertFalse(
            unresolved,
            "unresolved deterministic predicate overlaps: "
            + "; ".join(
                f"{overlap['source_rule_id']} -> "
                + ", ".join(
                    f"{match['rule_id']}@{match['priority']}"
                    for match in overlap["matches"]
                )
                for overlap in unresolved
            ),
        )

    def test_child_cleaner_overlap_prefers_specific_rule(self):
        overlaps = {
            overlap["source_rule_id"]: overlap
            for overlap in query.get_deterministic_special_case_overlaps()
        }
        cleaner_overlap = overlaps.get("child_under_sink_cleaner_ingestion")
        self.assertIsNotNone(cleaner_overlap)
        self.assertEqual(
            cleaner_overlap["winner_rule_ids"],
            ["child_under_sink_cleaner_ingestion"],
        )

    def test_overlap_matrix_rows_expose_winner_and_match_metadata(self):
        rows = build_overlap_matrix_rows(
            query.get_deterministic_special_case_overlaps()
        )
        cleaner_rows = [
            row
            for row in rows
            if row["source_rule_id"] == "child_under_sink_cleaner_ingestion"
        ]

        self.assertGreaterEqual(len(cleaner_rows), 2)
        self.assertTrue(
            {
                "source_rule_id",
                "sample_prompt",
                "matched_rule_id",
                "matched_priority",
                "matched_lexical_signature_size",
                "matched_promotion_status",
                "winner_rule_ids",
                "winner_reason",
                "is_winner",
            }.issubset(cleaner_rows[0])
        )
        winner_rows = [row for row in cleaner_rows if row["is_winner"]]
        self.assertEqual(len(winner_rows), 1)
        self.assertEqual(
            winner_rows[0]["matched_rule_id"],
            "child_under_sink_cleaner_ingestion",
        )
        self.assertEqual(
            winner_rows[0]["winner_rule_ids"],
            ["child_under_sink_cleaner_ingestion"],
        )

    def test_validator_writes_overlap_matrix_artifact(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            output_path = Path(tmpdir) / "overlap_matrix.json"
            result = subprocess.run(
                [
                    sys.executable,
                    "-B",
                    "scripts\\validate_special_cases.py",
                    "--overlap-matrix-json",
                    str(output_path),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=True,
            )

            self.assertIn("overlap", result.stdout)
            rows = json.loads(output_path.read_text(encoding="utf-8"))

        self.assertGreater(len(rows), 0)
        winner_rows = [row for row in rows if row["is_winner"]]
        self.assertGreater(len(winner_rows), 0)
        self.assertTrue(
            {
                "source_rule_id",
                "sample_prompt",
                "matched_rule_id",
                "matched_priority",
                "matched_lexical_signature_size",
                "matched_promotion_status",
                "winner_rule_ids",
                "winner_reason",
                "is_winner",
            }.issubset(rows[0])
        )

    def test_child_unknown_pill_overlap_prefers_child_ingestion_rule(self):
        matches = [
            rule
            for rule in query._DETERMINISTIC_SPECIAL_CASE_RULES
            if rule.predicate("toddler swallowed an unknown pill")
        ]

        winner, winner_reason = query._select_deterministic_special_case_rule(
            matches,
            log_first_defined_tie=False,
        )

        self.assertEqual(winner.rule_id, "unknown_child_ingestion")
        self.assertEqual(winner_reason, "priority")


if __name__ == "__main__":
    unittest.main()
