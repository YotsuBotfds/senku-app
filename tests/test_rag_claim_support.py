import unittest

from rag_claim_support import diagnose_claim_support, extract_action_lines


class RagClaimSupportTests(unittest.TestCase):
    def test_extracts_bullets_numbers_and_short_imperatives(self):
        answer = """Here is what to do:
- Call 911 now.
1. Keep the person upright.
Use chest thrusts if needed.
This longer background sentence explains why a helper should remain calm and watch the person closely."""

        actions = extract_action_lines(answer)

        self.assertEqual(
            actions,
            [
                "Call 911 now.",
                "Keep the person upright.",
                "Use chest thrusts if needed.",
            ],
        )

    def test_marks_supported_actions_by_card_basis(self):
        cards = [
            {
                "card_id": "choking",
                "guide_id": "GD-232",
                "required_first_actions": ["Call 911"],
                "first_actions": ["Use chest thrusts instead of abdominal thrusts"],
                "urgent_red_flags": ["Cannot speak, cough, or breathe"],
                "forbidden_advice": ["Do not perform abdominal thrusts on infants"],
            }
        ]

        result = diagnose_claim_support(
            "- Call 911 now.\n- Use chest thrusts instead of abdominal thrusts.",
            cards,
            cited_guide_ids=["GD-232"],
            expected_guide_ids=["GD-232"],
        )

        self.assertEqual(result["status"], "pass")
        self.assertEqual(result["action_count"], 2)
        self.assertEqual(result["supported_count"], 2)
        self.assertEqual(
            [action["support_basis"] for action in result["actions"]],
            ["card_required_action", "card_first_action"],
        )

    def test_positive_forbidden_card_advice_fails(self):
        cards = [
            {
                "card_id": "choking",
                "guide_id": "GD-232",
                "required_first_actions": ["Call 911"],
                "first_actions": [],
                "urgent_red_flags": [],
                "forbidden_advice": ["Do not perform abdominal thrusts on infants"],
            }
        ]

        result = diagnose_claim_support(
            "- Perform abdominal thrusts on infants.",
            cards,
            cited_guide_ids=["GD-232"],
            expected_guide_ids=["GD-232"],
        )

        self.assertEqual(result["status"], "fail")
        self.assertEqual(result["forbidden_count"], 1)
        self.assertEqual(result["actions"][0]["support_basis"], "card_forbidden_advice")

    def test_negative_safety_instruction_is_supported(self):
        cards = [
            {
                "card_id": "choking",
                "guide_id": "GD-232",
                "required_first_actions": ["Call 911"],
                "first_actions": [],
                "urgent_red_flags": [],
                "forbidden_advice": ["Do not perform abdominal thrusts on infants"],
            }
        ]

        result = diagnose_claim_support(
            "- Do not perform abdominal thrusts on infants.",
            cards,
            cited_guide_ids=["GD-232"],
            expected_guide_ids=["GD-232"],
        )

        self.assertEqual(result["status"], "pass")
        self.assertEqual(result["forbidden_count"], 0)
        self.assertEqual(
            result["actions"][0]["support_basis"],
            "card_negative_safety_instruction",
        )

    def test_unknown_actions_make_partial_or_fail(self):
        cards = [
            {
                "card_id": "wound",
                "guide_id": "GD-123",
                "required_first_actions": ["Cover the wound"],
                "first_actions": [],
                "urgent_red_flags": [],
                "forbidden_advice": [],
            }
        ]

        partial = diagnose_claim_support(
            "- Cover the wound.\n- Apply mystery herbs.",
            cards,
            cited_guide_ids=["GD-123"],
            expected_guide_ids=["GD-123"],
        )
        fail = diagnose_claim_support(
            "- Apply mystery herbs.",
            cards,
            cited_guide_ids=["GD-123"],
            expected_guide_ids=["GD-123"],
        )

        self.assertEqual(partial["status"], "partial")
        self.assertEqual(partial["unknown_count"], 1)
        self.assertEqual(fail["status"], "fail")

    def test_cited_or_expected_id_on_line_counts_as_owner_basis(self):
        result = diagnose_claim_support(
            "- Keep monitoring closely. [GD-123]",
            [{"card_id": "empty", "guide_id": "GD-123"}],
            cited_guide_ids=["GD-123"],
            expected_guide_ids=[],
        )

        self.assertEqual(result["status"], "pass")
        self.assertEqual(result["actions"][0]["support_basis"], "retrieved_or_cited_owner")

    def test_malformed_card_rows_do_not_hide_valid_support(self):
        result = diagnose_claim_support(
            "- Call 911 now.",
            [
                None,
                "not a card",
                {
                    "card_id": "emergency",
                    "guide_id": "GD-911",
                    "required_first_actions": ["Call 911"],
                },
            ],
        )

        self.assertEqual(result["status"], "pass")
        self.assertEqual(result["supported_count"], 1)
        self.assertEqual(result["actions"][0]["card_id"], "emergency")

    def test_long_bullets_keep_raw_citations_for_support(self):
        long_action = (
            "- Assess breathing, feeding, temperature, alertness, skin color, urine "
            "output, vomiting, and cord infection signs while arranging urgent "
            "newborn evaluation, then document the trend for the clinician because "
            "the situation may worsen quickly. [GD-492]"
        )

        result = diagnose_claim_support(
            long_action,
            [{"card_id": "empty", "guide_id": "GD-492"}],
            cited_guide_ids=["GD-492"],
        )

        self.assertEqual(result["status"], "pass")
        self.assertEqual(result["actions"][0]["support_basis"], "retrieved_or_cited_owner")
        self.assertLessEqual(len(result["actions"][0]["text"]), 180)

    def test_empty_states_are_explicit(self):
        self.assertEqual(
            diagnose_claim_support("", [], generated=False)["status"],
            "no_generated_answer",
        )
        self.assertEqual(
            diagnose_claim_support("This is context only.", [{"card_id": "x"}])["status"],
            "no_claims",
        )
        self.assertEqual(
            diagnose_claim_support("- Call 911.", [])["status"],
            "no_cards",
        )


if __name__ == "__main__":
    unittest.main()
