import unittest
from unittest.mock import patch

from deterministic_special_case_registry import DeterministicSpecialCaseSpec
from guide_catalog import all_guide_ids
import query


class CitationValidationTests(unittest.TestCase):
    def test_hallucinated_guide_id_is_removed_and_warned(self):
        valid_guide_id = sorted(all_guide_ids())[0]
        fixture = f"Boil the water first [{valid_guide_id}, GD-999]."

        with self.assertLogs("query", level="WARNING") as captured:
            normalized = query.normalize_response_text(fixture)

        self.assertIn(valid_guide_id, normalized)
        self.assertNotIn("GD-999", normalized)
        self.assertTrue(
            any("citation_hallucination" in entry for entry in captured.output)
        )

    def test_malformed_duplicate_citation_variants_normalize_stably(self):
        valid_guide_id = sorted(all_guide_ids())[0]
        guide_number = int(valid_guide_id.removeprefix("GD-"))
        fixture = (
            "Boil the water first "
            f"[GD/{guide_number}, {valid_guide_id}, GD-{guide_number}]."
        )

        normalized = query.normalize_response_text(fixture)

        self.assertEqual(normalized, f"Boil the water first [{valid_guide_id}].")

    def test_forbidden_retrieval_phrases_are_scrubbed(self):
        valid_guide_id = sorted(all_guide_ids())[0]
        fixtures = (
            (
                f"The provided notes say boil the water first [{valid_guide_id}].",
                f"The guides say boil the water first [{valid_guide_id}].",
                "the provided notes",
            ),
            (
                "Short answer: The retrieved notes do not cover arithmetic.",
                "Short answer: The guides do not cover arithmetic.",
                "the retrieved notes",
            ),
            (
                f"The supplied context points to evacuation first [{valid_guide_id}].",
                f"The guide support points to evacuation first [{valid_guide_id}].",
                "the supplied context",
            ),
            (
                f"Based on the provided information, shelter comes first [{valid_guide_id}].",
                f"Based on the guides, shelter comes first [{valid_guide_id}].",
                "based on the provided information",
            ),
        )

        for fixture, expected, forbidden_phrase in fixtures:
            with self.subTest(forbidden_phrase=forbidden_phrase):
                normalized = query.normalize_response_text(fixture)
                self.assertEqual(normalized, expected)
                self.assertNotIn(forbidden_phrase, normalized.lower())

    def test_warning_residual_brackets_are_removed_but_citations_remain(self):
        valid_guide_id = sorted(all_guide_ids())[0]
        fixture = (
            "Act immediately [Instructional Mandate]. "
            "Use only boiled water [Safety Warning Implied by High Heat Processes] "
            f"[{valid_guide_id}]. "
            "Keep pressure on the wound [System Instruction]."
        )

        normalized = query.normalize_response_text(fixture)

        self.assertIn(f"[{valid_guide_id}]", normalized)
        self.assertNotIn("[Instructional Mandate]", normalized)
        self.assertNotIn("[Safety Warning Implied by High Heat Processes]", normalized)
        self.assertNotIn("[System Instruction]", normalized)

    def test_warning_residual_scrub_preserves_non_residue_brackets(self):
        valid_guide_id = sorted(all_guide_ids())[0]
        fixture = (
            f"Use the agreed mark from [Community leader] first [{valid_guide_id}] "
            "and keep the chain of custody visible."
        )

        normalized = query.normalize_response_text(fixture)

        self.assertIn("[Community leader]", normalized)
        self.assertIn(f"[{valid_guide_id}]", normalized)

    def test_system_prompt_avoids_retrieval_mechanism_wording(self):
        system_prompt = query.config.build_system_prompt()

        self.assertNotIn("provided reference material", system_prompt)
        self.assertNotIn("retrieved material", system_prompt)
        self.assertNotIn("reference material", system_prompt)
        self.assertIn("guides in scope here", system_prompt)

    def test_bogus_builder_name_warns_and_surfaces_debug_only_fallback_note(self):
        bogus_spec = DeterministicSpecialCaseSpec(
            "bogus_builder",
            "_is_unknown_medication_special_case",
            "_build_missing_response",
            "i found some unmarked pills what do i do",
        )
        with patch.object(query, "DETERMINISTIC_SPECIAL_CASE_SPECS", (bogus_spec,)):
            resolved_rules = query._resolve_deterministic_special_case_rules()

        self.assertEqual(len(resolved_rules), 1)
        self.assertEqual(resolved_rules[0].rule_id, bogus_spec.rule_id)
        self.assertIsNone(resolved_rules[0].builder)

        quiet_notes = []
        with (
            patch.object(query, "_DETERMINISTIC_SPECIAL_CASE_RULES", resolved_rules),
            patch.object(query, "_DETERMINISTIC_SPECIAL_CASE_BUILDERS", {}),
            patch.object(query, "_DETERMINISTIC_SPECIAL_CASE_SPECS_BY_ID", {bogus_spec.rule_id: bogus_spec}),
            self.assertLogs("query", level="WARNING") as captured,
        ):
            response = query.build_special_case_response(
                bogus_spec.sample_prompt,
                debug_enabled=False,
                fallback_note_collector=quiet_notes,
            )

        self.assertIsNone(response)
        self.assertEqual(quiet_notes, [])
        self.assertEqual(len(captured.records), 1)
        self.assertEqual(
            captured.records[0].telemetry,
            {
                "event": "deterministic.builder_missing",
                "builder_name": bogus_spec.builder_name,
                "decision_path": "deterministic",
                "fallback": "retrieval",
                "rule_id": bogus_spec.rule_id,
            },
        )
        self.assertIn('"event": "deterministic.builder_missing"', captured.output[0])

        debug_notes = []
        with (
            patch.object(query, "_DETERMINISTIC_SPECIAL_CASE_RULES", resolved_rules),
            patch.object(query, "_DETERMINISTIC_SPECIAL_CASE_BUILDERS", {}),
            patch.object(query, "_DETERMINISTIC_SPECIAL_CASE_SPECS_BY_ID", {bogus_spec.rule_id: bogus_spec}),
            self.assertLogs("query", level="WARNING"),
        ):
            response = query.build_special_case_response(
                bogus_spec.sample_prompt,
                debug_enabled=True,
                fallback_note_collector=debug_notes,
            )

        self.assertIsNone(response)
        self.assertEqual(
            debug_notes,
            [
                query._build_deterministic_builder_missing_debug_note(
                    bogus_spec.rule_id,
                    bogus_spec.builder_name,
                )
            ],
        )


if __name__ == "__main__":
    unittest.main()
