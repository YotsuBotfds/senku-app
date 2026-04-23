import unittest

import query


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


if __name__ == "__main__":
    unittest.main()
