import re
import unittest

from deterministic_special_case_registry import (
    DETERMINISTIC_SPECIAL_CASE_SPECS,
    VALID_PROMOTION_STATUSES,
)
from guide_catalog import all_guide_ids
import query


class SpecialCaseTests(unittest.TestCase):
    def test_sample_prompts_route_to_declared_rule_ids(self):
        for rule in query.get_deterministic_special_case_rules():
            with self.subTest(rule_id=rule.rule_id):
                decision_path, decision_detail = query.classify_special_case(rule.sample_prompt)
                self.assertEqual(decision_path, "deterministic")
                self.assertEqual(decision_detail, rule.rule_id)

    def test_deterministic_citations_map_to_real_guides(self):
        valid_ids = all_guide_ids()
        for rule in query.get_deterministic_special_case_rules():
            with self.subTest(rule_id=rule.rule_id):
                response = query.build_special_case_response(rule.sample_prompt)
                normalized = query.normalize_response_text(response)
                citations = set(re.findall(r"GD-\d{3}", normalized))
                self.assertTrue(citations)
                self.assertFalse(
                    citations - valid_ids,
                    f"unknown guide ids: {sorted(citations - valid_ids)}",
                )

    def test_infected_puncture_does_not_route_to_generic_puncture(self):
        prompt = "i think this puncture wound is infected and i need antibiotics"
        decision_path, decision_detail = query.classify_special_case(prompt)
        self.assertNotEqual((decision_path, decision_detail), ("deterministic", "generic_puncture"))

    def test_veterinary_snake_bite_does_not_route_to_human_snake_bite_rule(self):
        prompt = "my dog got bit by a snake and the leg is swelling"
        decision_path, decision_detail = query.classify_special_case(prompt)
        self.assertNotEqual((decision_path, decision_detail), ("deterministic", "snake_bite_swelling"))

    def test_system_behavior_prompt_routes_to_system_behavior(self):
        prompt = "how should the answer behave if it starts drifting into unrelated topics"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("system-behavior", "system_behavior"),
        )

    def test_charcoal_sand_water_filter_routes_to_deterministic_builder(self):
        prompt = "how do i build a charcoal sand water filter"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "charcoal_sand_water_filter_starter"),
        )

    def test_reused_container_water_routes_to_deterministic_builder(self):
        prompt = "how do i store water safely in reused containers"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "reused_container_water"),
        )

    def test_old_soda_bottles_route_to_reused_container_builder(self):
        prompt = "can i store drinking water in old soda bottles"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "reused_container_water"),
        )

    def test_water_without_fuel_routes_to_deterministic_builder(self):
        prompt = "how do i purify water without fuel"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "water_without_fuel"),
        )

    def test_every_spec_has_valid_promotion_status(self):
        for spec in DETERMINISTIC_SPECIAL_CASE_SPECS:
            with self.subTest(rule_id=spec.rule_id):
                self.assertIn(spec.promotion_status, VALID_PROMOTION_STATUSES)


if __name__ == "__main__":
    unittest.main()
