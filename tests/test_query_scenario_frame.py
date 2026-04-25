import subprocess
import sys
import unittest

import query_scenario_frame as scenario_frame


def _detect_domains(text):
    lower = (text or "").lower()
    domains = set()
    if any(marker in lower for marker in ("roof", "leak", "storm")):
        domains.add("shelter")
    if any(marker in lower for marker in ("water", "filter", "boil")):
        domains.add("water")
    if any(marker in lower for marker in ("wound", "bleeding", "child")):
        domains.add("medical")
    return domains


class QueryScenarioFrameTests(unittest.TestCase):
    def test_import_does_not_load_query_or_bench(self):
        completed = subprocess.run(
            [
                sys.executable,
                "-B",
                "-c",
                (
                    "import sys; "
                    "import query_scenario_frame; "
                    "print('query' in sys.modules); "
                    "print('bench' in sys.modules)"
                ),
            ],
            check=True,
            capture_output=True,
            text=True,
        )

        self.assertEqual(completed.stdout.strip().splitlines(), ["False", "False"])

    def test_build_frame_uses_injected_domains_and_restart_splitter(self):
        frame = scenario_frame.build_scenario_frame(
            "We have a tarp, new question: how do we stop the roof leak before dark?",
            detect_domains=_detect_domains,
            split_at_question_restart=lambda question: question.split("new question:"),
        )

        self.assertEqual(frame["domains"], ["shelter"])
        self.assertIn("We have a tarp", frame["clauses"])
        self.assertIn("how do we stop the roof leak before dark?", frame["clauses"])
        self.assertIn("a tarp", frame["assets"])
        self.assertIn("time pressure: before dark", frame["constraints"])
        self.assertEqual(frame["deadline"], "before dark")
        objective_texts = [objective["text"] for objective in frame["objectives"]]
        self.assertIn("how do we stop the roof leak before dark?", objective_texts)
        self.assertTrue(
            any(objective["domains"] == ["shelter"] for objective in frame["objectives"])
        )

    def test_inventory_fragments_become_assets_not_objectives(self):
        frame = scenario_frame.build_scenario_frame(
            "We have rope, a tarp, and two buckets, how do we filter water?",
            detect_domains=_detect_domains,
        )

        self.assertIn("rope", frame["assets"])
        self.assertIn("a tarp", frame["assets"])
        self.assertIn("two buckets", frame["assets"])
        self.assertEqual(
            [objective["text"] for objective in frame["objectives"]],
            ["how do we filter water?"],
        )

    def test_placeholder_assets_are_ignored(self):
        frame = scenario_frame.build_scenario_frame(
            "We have what we have, what now?",
            detect_domains=_detect_domains,
        )

        self.assertEqual(frame["assets"], [])
        self.assertEqual(frame["objectives"], [])

    def test_frame_extracts_constraints_hazards_people_and_environment(self):
        frame = scenario_frame.build_scenario_frame(
            "Two people and a child are wet in a storm, one person is bleeding, "
            "we have no clean water, what first?",
            detect_domains=_detect_domains,
        )

        self.assertIn("we have no clean water", frame["constraints"])
        self.assertIn("bleeding", frame["hazards"])
        self.assertIn("wet exposure", frame["hazards"])
        self.assertIn("child", frame["people"])
        self.assertIn("people", frame["people"])
        self.assertIn("storm", frame["environment"])
        self.assertEqual(frame["environment"], ["storm"])
        self.assertIn("medical", frame["domains"])
        self.assertIn("water", frame["domains"])

    def test_update_and_merge_session_state_dedupe_in_first_seen_order(self):
        initial = {
            "assets": ["rope"],
            "constraints": ["no power"],
            "hazards": ["storm damage"],
            "people": ["child"],
            "environment": ["camp"],
            "deadline": "tonight",
            "active_objectives": ["stop roof leak"],
            "anchor_guide_id": "GD-001",
            "anchor_turn_index": 2,
            "turn_count": 3,
        }
        frame = {
            "assets": ["rope", "tarp"],
            "constraints": ["no power", "time pressure: before dark"],
            "hazards": ["storm damage", "bleeding"],
            "people": ["child", "adult"],
            "environment": ["camp", "rain"],
            "deadline": "before dark",
            "objectives": [{"text": "filter water"}, {"text": "stop roof leak"}],
        }

        updated = scenario_frame.update_session_state(initial, frame)
        merged = scenario_frame.merge_frame_with_session(frame, initial)

        self.assertEqual(updated["assets"], ["rope", "tarp"])
        self.assertEqual(updated["constraints"], ["no power", "time pressure: before dark"])
        self.assertEqual(updated["hazards"], ["storm damage", "bleeding"])
        self.assertEqual(updated["people"], ["child", "adult"])
        self.assertEqual(updated["environment"], ["camp", "rain"])
        self.assertEqual(updated["deadline"], "before dark")
        self.assertEqual(updated["active_objectives"], ["filter water", "stop roof leak"])
        self.assertEqual(merged["assets"], ["rope", "tarp"])
        self.assertEqual(merged["deadline"], "before dark")
        self.assertEqual(merged["session_active_objectives"], ["stop roof leak"])

    def test_render_session_state_text_and_context_gating(self):
        state = scenario_frame.update_session_state(
            scenario_frame.empty_session_state(),
            {
                "assets": ["rope"],
                "constraints": ["no power"],
                "hazards": ["storm damage"],
                "people": ["child"],
                "environment": ["camp"],
                "deadline": "tonight",
                "objectives": [{"text": "stop roof leak"}],
            },
        )
        vague_frame = scenario_frame.build_scenario_frame(
            "what next?",
            detect_domains=lambda _text: set(),
        )
        reset_frame = scenario_frame.build_scenario_frame(
            "new question: how do we filter water?",
            detect_domains=_detect_domains,
        )
        domain_frame = scenario_frame.build_scenario_frame(
            "filter water",
            detect_domains=_detect_domains,
        )

        rendered = scenario_frame._render_session_state_text(state)

        self.assertIn("active objectives: stop roof leak", rendered)
        self.assertTrue(
            scenario_frame._should_use_session_context("what next?", vague_frame, state)
        )
        self.assertFalse(
            scenario_frame._should_use_session_context(
                "new question: how do we filter water?",
                reset_frame,
                state,
            )
        )
        self.assertFalse(
            scenario_frame._should_use_session_context(
                "filter water",
                domain_frame,
                state,
            )
        )


if __name__ == "__main__":
    unittest.main()
