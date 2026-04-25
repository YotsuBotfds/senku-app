import importlib
import os
import subprocess
import sys
import unittest
from unittest.mock import patch

import query_answer_card_runtime as runtime


class QueryAnswerCardRuntimeExtractionTests(unittest.TestCase):
    def test_runtime_module_import_does_not_load_query(self):
        script = (
            "import importlib, sys; "
            "sys.modules.pop('query', None); "
            "importlib.import_module('query_answer_card_runtime'); "
            "print('query' in sys.modules)"
        )

        completed = subprocess.run(
            [sys.executable, "-c", script],
            cwd=os.getcwd(),
            check=True,
            capture_output=True,
            text=True,
        )

        self.assertEqual(completed.stdout.strip(), "False")

    def test_answer_cards_for_results_prioritizes_reviewed_matching_source_family(self):
        poison_card = {
            "card_id": "poisoning_unknown_ingestion",
            "guide_id": "GD-898",
            "review_status": "approved",
        }
        airway_card = {
            "card_id": "choking_airway_obstruction",
            "guide_id": "GD-232",
            "review_status": "pilot_reviewed",
            "source_sections": [{"guide": "GD-298"}],
        }
        draft_card = {
            "card_id": "draft_airway",
            "guide_id": "GD-298",
            "review_status": "draft",
        }
        calls = []

        def fake_runtime_answer_cards():
            calls.append("load")
            return [poison_card, airway_card, draft_card]

        def fake_prioritized_ids(question):
            self.assertEqual(question, "is this choking or just panic")
            return ["choking_airway_obstruction"]

        def fake_matches(card, question):
            return card["card_id"] != "draft_airway" and "choking" in question

        cards = runtime._answer_cards_for_results(
            {"metadatas": [[{"guide_id": "GD-298"}, {"guide_id": "GD-898"}]]},
            question="is this choking or just panic",
            max_cards=2,
            runtime_answer_cards=fake_runtime_answer_cards,
            citation_allowlist_from_results=lambda results: ["GD-298", "GD-898"],
            prioritized_answer_card_ids_for_question=fake_prioritized_ids,
            answer_card_matches_question=fake_matches,
            card_source_guide_ids=runtime._card_source_guide_ids,
        )

        self.assertEqual(
            [card["card_id"] for card in cards],
            ["choking_airway_obstruction", "poisoning_unknown_ingestion"],
        )
        self.assertGreaterEqual(len(calls), 1)

    def test_card_backed_runtime_answer_plan_uses_injected_composer(self):
        selected_card = {
            "card_id": "newborn_danger_sepsis",
            "guide_id": "GD-284",
            "review_status": "approved",
            "risk_tier": "critical",
        }
        results = {"metadatas": [[{"source_file": "guides/GD-492.md"}]]}
        compose_calls = []

        def fake_answer_cards_for_results(received_results, *, question, max_cards):
            self.assertIs(received_results, results)
            self.assertEqual(question, "newborn is limp and hard to wake")
            self.assertEqual(max_cards, 1)
            return [selected_card]

        def fake_card_runtime_citation_guide_ids(card, allowed_guide_ids):
            self.assertIs(card, selected_card)
            self.assertEqual(allowed_guide_ids, ["GD-492"])
            return ["GD-492"]

        def fake_compose(cards, *, question_text, allowed_guide_ids, max_cards):
            compose_calls.append((cards, question_text, allowed_guide_ids, max_cards))
            return {
                "status": "ready",
                "answer_text": "Get urgent medical care now and keep the newborn warm. [GD-492]",
                "card_ids": ["newborn_danger_sepsis"],
                "guide_ids": ["GD-284", "GD-492"],
                "cited_guide_ids": ["GD-492"],
            }

        with patch.dict(
            runtime.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ):
            plan = runtime._card_backed_runtime_answer_plan(
                "newborn is limp and hard to wake",
                results,
                answer_cards_for_results=fake_answer_cards_for_results,
                citation_allowlist_from_results=runtime._citation_allowlist_from_results,
                card_runtime_citation_guide_ids=fake_card_runtime_citation_guide_ids,
                compose_guide_card_backed_answer=fake_compose,
            )

        self.assertEqual(
            plan,
            {
                "answer_text": "Get urgent medical care now and keep the newborn warm. [GD-492]",
                "card_ids": ["newborn_danger_sepsis"],
                "guide_ids": ["GD-284", "GD-492"],
                "cited_guide_ids": ["GD-492"],
                "review_status": "approved",
                "risk_tier": "critical",
            },
        )
        self.assertEqual(
            compose_calls,
            [([selected_card], "newborn is limp and hard to wake", ["GD-492"], 1)],
        )


class QueryCitationAllowlistWrapperTests(unittest.TestCase):
    def test_add_citation_allowlist_contract_uses_source_file_gd_fallback(self):
        query = importlib.import_module("query")
        results = {"metadatas": [[{"source_file": "reviewed/GD-898.md"}]]}

        with patch.object(
            query,
            "_estimate_chat_prompt_tokens",
            return_value={"estimated_prompt_tokens": 20},
        ):
            prompt = query._add_citation_allowlist_contract(
                "answer body",
                results,
                prompt_token_limit=4096,
                question="child swallowed something from an unlabeled cleaner bottle",
            )

        self.assertIn(
            "these exact retrieved guide IDs only: [GD-898]",
            prompt,
        )
        self.assertIn("answer body", prompt)


if __name__ == "__main__":
    unittest.main()
