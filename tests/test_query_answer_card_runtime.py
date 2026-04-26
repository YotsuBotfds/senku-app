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

    def test_broad_pediatric_card_does_not_crowd_out_airway_fallback(self):
        choking_card = {
            "card_id": "choking_airway_obstruction",
            "guide_id": "GD-232",
            "review_status": "pilot_reviewed",
            "source_sections": [{"guide": "GD-298"}],
        }
        pediatric_card = {
            "card_id": "pediatric_emergency_medicine",
            "guide_id": "GD-298",
            "review_status": "pilot_reviewed",
        }

        cards = runtime._answer_cards_for_results(
            {"metadatas": [[{"guide_id": "GD-298"}]]},
            question="My child is choking and cannot cough.",
            max_cards=2,
            runtime_answer_cards=lambda: [choking_card, pediatric_card],
            citation_allowlist_from_results=lambda results: ["GD-298"],
            prioritized_answer_card_ids_for_question=lambda question: ["choking_airway_obstruction"],
            answer_card_matches_question=lambda card, question: runtime._answer_card_matches_question(
                card,
                question,
                is_airway_obstruction_rag_query=lambda text: "choking" in text,
                has_allergy_or_anaphylaxis_trigger=lambda text: False,
                is_newborn_sepsis_danger_query=lambda text: False,
                is_meningitis_rash_emergency_query=lambda text: False,
            ),
            card_source_guide_ids=runtime._card_source_guide_ids,
        )

        self.assertEqual([card["card_id"] for card in cards], ["choking_airway_obstruction"])

    def test_prioritized_answer_cards_can_match_full_retrieved_allowlist(self):
        poison_card = {
            "card_id": "poisoning_unknown_ingestion",
            "guide_id": "GD-898",
            "review_status": "approved",
        }
        unrelated_card = {
            "card_id": "unrelated",
            "guide_id": "GD-111",
            "review_status": "approved",
        }

        cards = runtime._answer_cards_for_results(
            {
                "metadatas": [[
                    {"guide_id": "GD-111"},
                    {"guide_id": "GD-222"},
                    {"guide_id": "GD-898"},
                ]]
            },
            question="mouth burns after tasting a mystery liquid",
            max_cards=1,
            max_source_ids=2,
            runtime_answer_cards=lambda: [unrelated_card, poison_card],
            citation_allowlist_from_results=lambda results: ["GD-111", "GD-222", "GD-898"],
            prioritized_answer_card_ids_for_question=lambda question: ["poisoning_unknown_ingestion"],
            answer_card_matches_question=lambda card, question: card["card_id"] == "poisoning_unknown_ingestion",
            card_source_guide_ids=runtime._card_source_guide_ids,
        )

        self.assertEqual([card["card_id"] for card in cards], ["poisoning_unknown_ingestion"])

    def test_prioritized_answer_cards_keep_slot_when_max_cards_is_two(self):
        unrelated_first = {
            "card_id": "first_unrelated",
            "guide_id": "GD-111",
            "review_status": "approved",
        }
        unrelated_second = {
            "card_id": "second_unrelated",
            "guide_id": "GD-222",
            "review_status": "approved",
        }
        anaphylaxis_card = {
            "card_id": "anaphylaxis_red_zone",
            "guide_id": "GD-400",
            "review_status": "pilot_reviewed",
        }

        cards = runtime._answer_cards_for_results(
            {
                "metadatas": [[
                    {"guide_id": "GD-111"},
                    {"guide_id": "GD-222"},
                    {"guide_id": "GD-400"},
                ]]
            },
            question="Bee sting and now throat feels tight",
            max_cards=2,
            runtime_answer_cards=lambda: [
                unrelated_first,
                unrelated_second,
                anaphylaxis_card,
            ],
            citation_allowlist_from_results=lambda results: [
                "GD-111",
                "GD-222",
                "GD-400",
            ],
            prioritized_answer_card_ids_for_question=lambda question: [
                "anaphylaxis_red_zone"
            ],
            answer_card_matches_question=lambda card, question: True,
            card_source_guide_ids=runtime._card_source_guide_ids,
        )

        self.assertEqual(
            [card["card_id"] for card in cards],
            ["first_unrelated", "anaphylaxis_red_zone"],
        )

    def test_anaphylaxis_red_zone_is_prioritized_for_allergen_airway_prompt(self):
        prioritized = runtime._prioritized_answer_card_ids_for_question(
            "Wheezing and throat swelling right after a bee sting. What matters first?",
            is_airway_obstruction_rag_query=lambda question: False,
            has_allergy_or_anaphylaxis_trigger=lambda question: True,
            is_newborn_sepsis_danger_query=lambda question: False,
            is_meningitis_rash_emergency_query=lambda question: False,
        )

        self.assertIn("anaphylaxis_red_zone", prioritized)

    def test_anaphylaxis_red_zone_matches_existing_deterministic_red_zone_phrasings(self):
        anaphylaxis_card = {
            "card_id": "anaphylaxis_red_zone",
            "guide_id": "GD-400",
            "review_status": "pilot_reviewed",
        }
        prompts = [
            "Bee sting and now the throat feels tight. What do I do first?",
            "My rescue inhaler is not helping after I ate something. What do I do first?",
            "Stung many times by bees and now vomiting and dizzy.",
            "Whole body hives and weakness after a sting on the arm.",
        ]

        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertTrue(
                    runtime._answer_card_matches_question(
                        anaphylaxis_card,
                        prompt,
                        is_airway_obstruction_rag_query=lambda question: False,
                        has_allergy_or_anaphylaxis_trigger=lambda question: True,
                        is_newborn_sepsis_danger_query=lambda question: False,
                        is_meningitis_rash_emergency_query=lambda question: False,
                    )
                )

    def test_anaphylaxis_red_zone_does_not_match_mild_skin_only_hives_prompt(self):
        anaphylaxis_card = {
            "card_id": "anaphylaxis_red_zone",
            "guide_id": "GD-400",
            "review_status": "pilot_reviewed",
        }
        mild_hives_question = (
            "After a new medicine I have mild hives and itching only, "
            "normal breathing and normal alertness. Is this anaphylaxis?"
        )

        self.assertFalse(
            runtime._answer_card_matches_question(
                anaphylaxis_card,
                mild_hives_question,
                is_airway_obstruction_rag_query=lambda question: False,
                has_allergy_or_anaphylaxis_trigger=lambda question: True,
                is_newborn_sepsis_danger_query=lambda question: False,
                is_meningitis_rash_emergency_query=lambda question: False,
            )
        )

        cards = runtime._answer_cards_for_results(
            {"metadatas": [[{"guide_id": "GD-400"}]]},
            question=mild_hives_question,
            max_cards=1,
            runtime_answer_cards=lambda: [anaphylaxis_card],
            citation_allowlist_from_results=lambda results: ["GD-400"],
            prioritized_answer_card_ids_for_question=lambda question: [],
            answer_card_matches_question=lambda card, question: runtime._answer_card_matches_question(
                card,
                question,
                is_airway_obstruction_rag_query=lambda text: False,
                has_allergy_or_anaphylaxis_trigger=lambda text: True,
                is_newborn_sepsis_danger_query=lambda text: False,
                is_meningitis_rash_emergency_query=lambda text: False,
            ),
            card_source_guide_ids=runtime._card_source_guide_ids,
        )

        self.assertEqual(cards, [])

    def test_anaphylaxis_red_zone_does_not_match_isolated_face_swelling(self):
        anaphylaxis_card = {
            "card_id": "anaphylaxis_red_zone",
            "guide_id": "GD-400",
            "review_status": "pilot_reviewed",
        }
        moderate_question = (
            "Face swelling after a bee sting, but breathing is normal, "
            "no dizziness, and no hives."
        )

        self.assertFalse(
            runtime._answer_card_matches_question(
                anaphylaxis_card,
                moderate_question,
                is_airway_obstruction_rag_query=lambda question: False,
                has_allergy_or_anaphylaxis_trigger=lambda question: True,
                is_newborn_sepsis_danger_query=lambda question: False,
                is_meningitis_rash_emergency_query=lambda question: False,
            )
        )

    def test_anaphylaxis_red_zone_ignores_negated_airway_and_swelling_terms(self):
        anaphylaxis_card = {
            "card_id": "anaphylaxis_red_zone",
            "guide_id": "GD-400",
            "review_status": "pilot_reviewed",
        }
        mild_hives_question = (
            "After eating shellfish I have hives, but no throat swelling, "
            "no trouble breathing, no lip swelling, and normal alertness."
        )

        self.assertFalse(
            runtime._answer_card_matches_question(
                anaphylaxis_card,
                mild_hives_question,
                is_airway_obstruction_rag_query=lambda question: False,
                has_allergy_or_anaphylaxis_trigger=lambda question: True,
                is_newborn_sepsis_danger_query=lambda question: False,
                is_meningitis_rash_emergency_query=lambda question: False,
            )
        )

        variants = [
            "After shellfish I have hives but no wheezing, no difficulty breathing, and normal alertness.",
            "After shellfish I have hives but no wheeze, no swollen lip, and breathing is normal.",
        ]
        for prompt in variants:
            with self.subTest(prompt=prompt):
                self.assertFalse(
                    runtime._answer_card_matches_question(
                        anaphylaxis_card,
                        prompt,
                        is_airway_obstruction_rag_query=lambda question: False,
                        has_allergy_or_anaphylaxis_trigger=lambda question: True,
                        is_newborn_sepsis_danger_query=lambda question: False,
                        is_meningitis_rash_emergency_query=lambda question: False,
                    )
                )

    def test_community_kitchen_illness_control_prioritized_for_queue_prompt(self):
        prompt = (
            "A crowded volunteer kitchen has several people with diarrhea after "
            "yesterday's meal, handwashing water is limited, and meals still need "
            "to go out. What should the kitchen lead change first?"
        )
        prioritized = runtime._prioritized_answer_card_ids_for_question(
            prompt,
            is_airway_obstruction_rag_query=lambda question: False,
            has_allergy_or_anaphylaxis_trigger=lambda question: False,
            is_newborn_sepsis_danger_query=lambda question: False,
            is_meningitis_rash_emergency_query=lambda question: False,
        )

        self.assertIn("community_kitchen_illness_control", prioritized)

        card = {
            "card_id": "community_kitchen_illness_control",
            "guide_id": "GD-961",
            "review_status": "approved",
        }
        self.assertTrue(
            runtime._answer_card_matches_question(
                card,
                prompt,
                is_airway_obstruction_rag_query=lambda question: False,
                has_allergy_or_anaphylaxis_trigger=lambda question: False,
                is_newborn_sepsis_danger_query=lambda question: False,
                is_meningitis_rash_emergency_query=lambda question: False,
            )
        )

    def test_community_kitchen_illness_control_can_match_source_family(self):
        unrelated_card = {
            "card_id": "ordinary_hygiene",
            "guide_id": "GD-732",
            "review_status": "approved",
        }
        kitchen_card = {
            "card_id": "community_kitchen_illness_control",
            "guide_id": "GD-961",
            "review_status": "approved",
            "source_sections": [
                {"guide": "GD-666"},
                {"guide": "GD-902"},
            ],
        }
        prompt = (
            "In the mess hall, multiple volunteers are vomiting after lunch and "
            "the food line is still open. What should change first?"
        )

        cards = runtime._answer_cards_for_results(
            {
                "metadatas": [[
                    {"guide_id": "GD-732"},
                    {"guide_id": "GD-666"},
                    {"guide_id": "GD-902"},
                ]]
            },
            question=prompt,
            max_cards=1,
            max_source_ids=1,
            runtime_answer_cards=lambda: [unrelated_card, kitchen_card],
            citation_allowlist_from_results=lambda results: [
                "GD-732",
                "GD-666",
                "GD-902",
            ],
            prioritized_answer_card_ids_for_question=(
                lambda question: runtime._prioritized_answer_card_ids_for_question(
                    question,
                    is_airway_obstruction_rag_query=lambda text: False,
                    has_allergy_or_anaphylaxis_trigger=lambda text: False,
                    is_newborn_sepsis_danger_query=lambda text: False,
                    is_meningitis_rash_emergency_query=lambda text: False,
                )
            ),
            answer_card_matches_question=(
                lambda card, question: runtime._answer_card_matches_question(
                    card,
                    question,
                    is_airway_obstruction_rag_query=lambda text: False,
                    has_allergy_or_anaphylaxis_trigger=lambda text: False,
                    is_newborn_sepsis_danger_query=lambda text: False,
                    is_meningitis_rash_emergency_query=lambda text: False,
                )
            ),
            card_source_guide_ids=runtime._card_source_guide_ids,
        )

        self.assertEqual(
            [card["card_id"] for card in cards],
            ["community_kitchen_illness_control"],
        )

    def test_community_kitchen_illness_control_rejects_near_misses(self):
        card = {
            "card_id": "community_kitchen_illness_control",
            "guide_id": "GD-961",
            "review_status": "approved",
        }
        prompts = [
            "The community kitchen has limited handwashing water and meals to send out. How should we run cleanup?",
            "A volunteer kitchen has one cook with diarrhea after yesterday's meal. What should we do?",
            "Handwashing water is limited in camp and people need hygiene rules. What changes first?",
            "A camp has many people with diarrhea after dinner. What outbreak steps come first?",
        ]

        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertFalse(
                    runtime._answer_card_matches_question(
                        card,
                        prompt,
                        is_airway_obstruction_rag_query=lambda question: False,
                        has_allergy_or_anaphylaxis_trigger=lambda question: False,
                        is_newborn_sepsis_danger_query=lambda question: False,
                        is_meningitis_rash_emergency_query=lambda question: False,
                    )
                )

    def test_food_system_outage_illness_boundary_prioritized_for_eval8_prompt(self):
        prompt = (
            "A neighborhood food system has a refrigerator outage, "
            "short-shelf-life food, and several families reporting stomach "
            "illness. How do we keep people fed without spreading sickness?"
        )
        prioritized = runtime._prioritized_answer_card_ids_for_question(
            prompt,
            is_airway_obstruction_rag_query=lambda question: False,
            has_allergy_or_anaphylaxis_trigger=lambda question: False,
            is_newborn_sepsis_danger_query=lambda question: False,
            is_meningitis_rash_emergency_query=lambda question: False,
        )

        self.assertIn("food_system_outage_illness_boundary", prioritized)
        self.assertNotIn("community_kitchen_illness_control", prioritized)

        food_system_card = {
            "card_id": "food_system_outage_illness_boundary",
            "guide_id": "GD-634",
            "review_status": "approved",
        }
        kitchen_card = {
            "card_id": "community_kitchen_illness_control",
            "guide_id": "GD-961",
            "review_status": "approved",
        }
        self.assertTrue(
            runtime._answer_card_matches_question(
                food_system_card,
                prompt,
                is_airway_obstruction_rag_query=lambda question: False,
                has_allergy_or_anaphylaxis_trigger=lambda question: False,
                is_newborn_sepsis_danger_query=lambda question: False,
                is_meningitis_rash_emergency_query=lambda question: False,
            )
        )
        self.assertFalse(
            runtime._answer_card_matches_question(
                kitchen_card,
                prompt,
                is_airway_obstruction_rag_query=lambda question: False,
                has_allergy_or_anaphylaxis_trigger=lambda question: False,
                is_newborn_sepsis_danger_query=lambda question: False,
                is_meningitis_rash_emergency_query=lambda question: False,
            )
        )

    def test_food_system_outage_illness_boundary_can_match_source_family(self):
        unrelated_card = {
            "card_id": "ordinary_rationing",
            "guide_id": "GD-591",
            "review_status": "approved",
        }
        food_system_card = {
            "card_id": "food_system_outage_illness_boundary",
            "guide_id": "GD-634",
            "review_status": "approved",
            "source_sections": [
                {"guide": "GD-591"},
                {"guide": "GD-666"},
                {"guide": "GD-732"},
                {"guide": "GD-961"},
            ],
        }
        prompt = (
            "A neighborhood food system has a refrigerator outage, "
            "short-shelf-life food, and several families reporting stomach "
            "illness. How do we keep people fed without spreading sickness?"
        )

        cards = runtime._answer_cards_for_results(
            {
                "metadatas": [[
                    {"guide_id": "GD-591"},
                    {"guide_id": "GD-666"},
                    {"guide_id": "GD-732"},
                ]]
            },
            question=prompt,
            max_cards=1,
            max_source_ids=1,
            runtime_answer_cards=lambda: [unrelated_card, food_system_card],
            citation_allowlist_from_results=lambda results: [
                "GD-591",
                "GD-666",
                "GD-732",
            ],
            prioritized_answer_card_ids_for_question=(
                lambda question: runtime._prioritized_answer_card_ids_for_question(
                    question,
                    is_airway_obstruction_rag_query=lambda text: False,
                    has_allergy_or_anaphylaxis_trigger=lambda text: False,
                    is_newborn_sepsis_danger_query=lambda text: False,
                    is_meningitis_rash_emergency_query=lambda text: False,
                )
            ),
            answer_card_matches_question=(
                lambda card, question: runtime._answer_card_matches_question(
                    card,
                    question,
                    is_airway_obstruction_rag_query=lambda text: False,
                    has_allergy_or_anaphylaxis_trigger=lambda text: False,
                    is_newborn_sepsis_danger_query=lambda text: False,
                    is_meningitis_rash_emergency_query=lambda text: False,
                )
            ),
            card_source_guide_ids=runtime._card_source_guide_ids,
        )

        self.assertEqual(
            [card["card_id"] for card in cards],
            ["food_system_outage_illness_boundary"],
        )

    def test_food_system_outage_illness_boundary_rejects_near_misses(self):
        card = {
            "card_id": "food_system_outage_illness_boundary",
            "guide_id": "GD-634",
            "review_status": "approved",
        }
        prompts = [
            "The neighborhood fridge failed during an outage. Which perishables should we cook first?",
            "Several families have stomach illness. What hygiene steps stop spread at home?",
            "A refrigerator outage spoiled short-shelf-life food, but nobody is sick. How do we keep people fed?",
            "Several families are vomiting after a picnic, but there is no food shortage or outage pressure.",
        ]

        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertFalse(
                    runtime._answer_card_matches_question(
                        card,
                        prompt,
                        is_airway_obstruction_rag_query=lambda question: False,
                        has_allergy_or_anaphylaxis_trigger=lambda question: False,
                        is_newborn_sepsis_danger_query=lambda question: False,
                        is_meningitis_rash_emergency_query=lambda question: False,
                    )
                )

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
