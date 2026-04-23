import unittest

import query


LIVE_ANDROID_NEAR_MISS_PANEL = {
    "generic_puncture": (
        "my dog bit me on the foot and the wound is a puncture",
        "i got a deep puncture wound in my eye",
        "i stepped on a nail and now there is uncontrolled bleeding",
    ),
    "charcoal_sand_water_filter_starter": (
        "how do i build a charcoal filter for drinking water",
        "how do i make a sand filter for clean water",
    ),
    "reused_container_water": (
        "can i store water in an old plastic bottle that held bleach",
        "is a rusty steel barrel safe for drinking water",
        "can i store water in old milk jugs that smell like cleaning detergent",
    ),
    "water_without_fuel": (
        "how do i purify water without fuel using sunlight",
        "how do i purify water without fuel if the well is contaminated and two people are sick",
        "how do i purify drinking water without fuel for someone who is wounded",
    ),
    "fire_in_rain": (
        "how do i keep a fire going when it starts to rain",
        "how can i stay warm in the rain without starting a fire",
    ),
    "weld_without_welder_starter": (
        "how can i join metal pieces with screws and bolts",
        "how do i join two wooden boards when i don't have welding gear",
    ),
    "metal_splinter": (
        "there is a metal splinter in my eye",
        "metal splinter in my hand with severe bleeding",
        "i have a wood splinter in my hand",
    ),
    "candles_for_light": (
        "where can i buy candles for light",
        "how long do candles burn for",
    ),
    "glassmaking_starter": (
        "how do i make a glass from sand and clay",
        "how do i repair a cracked glass bottle",
    ),
}


class DeterministicNearMissTests(unittest.TestCase):
    def test_panel_covers_all_live_android_rules(self):
        active_rule_ids = {
            rule.rule_id
            for rule in query.get_deterministic_special_case_rules()
            if rule.promotion_status == "active"
        }
        self.assertEqual(active_rule_ids, set(LIVE_ANDROID_NEAR_MISS_PANEL))

    def test_live_rule_near_misses_do_not_route_to_target_rule(self):
        for rule_id, prompts in LIVE_ANDROID_NEAR_MISS_PANEL.items():
            for prompt in prompts:
                with self.subTest(rule_id=rule_id, prompt=prompt):
                    decision_path, decision_detail = query.classify_special_case(prompt)
                    self.assertNotEqual(
                        (decision_path, decision_detail),
                        ("deterministic", rule_id),
                    )


if __name__ == "__main__":
    unittest.main()
