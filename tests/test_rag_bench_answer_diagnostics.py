import subprocess
import sys
import unittest

from rag_bench_answer_diagnostics import (
    app_acceptance_diagnostics,
    compact_claim_basis,
    compact_match_phrases,
    evidence_nugget_diagnostics,
    is_bare_meningitis_vs_viral_compare,
    select_family_cards,
)


class RagBenchAnswerDiagnosticsTests(unittest.TestCase):
    def test_import_does_not_load_query_module(self):
        completed = subprocess.run(
            [
                sys.executable,
                "-B",
                "-c",
                (
                    "import sys; "
                    "import rag_bench_answer_diagnostics; "
                    "print('query' in sys.modules); "
                    "print('bench' in sys.modules); "
                    "print('scripts.analyze_rag_bench_failures' in sys.modules)"
                ),
            ],
            check=True,
            capture_output=True,
            text=True,
        )

        self.assertEqual(completed.stdout.strip().splitlines(), ["False", "False", "False"])

    def test_compact_match_phrases_dedupes_and_skips_blanks(self):
        matches = [
            {"phrase": " Call 911 "},
            {"phrase": ""},
            {"phrase": None},
            {"phrase": "Call 911"},
            {"phrase": "Do not wait"},
            {},
        ]

        self.assertEqual(
            compact_match_phrases(matches),
            "Call 911|Do not wait",
        )

    def test_compact_claim_basis_counts_and_sorts_support_basis_values(self):
        actions = [
            {"support_basis": "card_required_action"},
            {"support_basis": "answer_card"},
            {"support_basis": "card_required_action"},
            {"support_basis": ""},
            {},
        ]

        self.assertEqual(
            compact_claim_basis(actions),
            "answer_card:1|card_required_action:2|unknown:2",
        )

    def test_select_family_cards_narrows_by_expected_family_tokens(self):
        cards = [
            {
                "card_id": "airway_choking_child",
                "slug": "choking-blue-lips",
                "title": "Choking and breathing emergencies",
            },
            {
                "card_id": "rash_child",
                "slug": "common-rash",
                "title": "Rash basics",
            },
            {
                "card_id": "airway_asthma",
                "slug": "breathing-wheeze",
                "title": "Breathing trouble",
            },
        ]

        selected = select_family_cards(cards, "child airway choking emergency")

        self.assertEqual([card["card_id"] for card in selected], ["airway_choking_child"])

    def test_select_family_cards_falls_back_when_no_family_match(self):
        cards = [
            {"card_id": "rash_child", "slug": "common-rash", "title": "Rash basics"},
            {"card_id": "ear_pain", "slug": "earache", "title": "Ear pain"},
        ]

        self.assertIs(select_family_cards(cards, "airway choking"), cards)

    def test_evidence_nugget_diagnostics_returns_no_cards(self):
        diagnostics = evidence_nugget_diagnostics(
            {"question": "Prompt", "response_text": "Answer"},
            cited_ids=[],
            generated="yes",
            selected_cards=[],
        )

        self.assertEqual(diagnostics["evidence_nugget_status"], "no_cards")
        self.assertEqual(diagnostics["evidence_nugget_total"], 0)

    def test_evidence_nugget_diagnostics_counts_supported_and_missing_required_actions(self):
        diagnostics = evidence_nugget_diagnostics(
            {
                "question": "Child swallowed unknown cleaner",
                "response_text": "Call Poison Control now. [GD-222]",
            },
            cited_ids=["GD-222"],
            generated="yes",
            selected_cards=[
                {
                    "card_id": "poisoning",
                    "guide_id": "GD-111",
                    "risk_tier": "critical",
                    "required_first_actions": [
                        "Call Poison Control now",
                        "Check breathing and alertness",
                    ],
                    "forbidden_advice": [],
                    "source_sections": [{"guide": "GD-222"}],
                }
            ],
        )

        self.assertEqual(diagnostics["evidence_nugget_status"], "partial")
        self.assertEqual(diagnostics["evidence_nugget_total"], 2)
        self.assertEqual(diagnostics["evidence_nugget_present"], 1)
        self.assertEqual(diagnostics["evidence_nugget_cited"], 1)
        self.assertEqual(diagnostics["evidence_nugget_supported"], 1)
        self.assertEqual(diagnostics["evidence_nugget_missing"], 1)
        self.assertEqual(
            diagnostics["evidence_nugget_missing_phrases"],
            "Check breathing and alertness",
        )

    def test_evidence_nugget_diagnostics_flags_forbidden_contradictions(self):
        diagnostics = evidence_nugget_diagnostics(
            {
                "question": "Can we wait?",
                "response_text": "Call 911 now, then give milk. [GD-111]",
            },
            cited_ids=["GD-111"],
            generated="yes",
            selected_cards=[
                {
                    "card_id": "poisoning",
                    "guide_id": "GD-111",
                    "risk_tier": "critical",
                    "required_first_actions": ["Call 911 now"],
                    "forbidden_advice": ["give milk"],
                }
            ],
        )

        self.assertEqual(diagnostics["evidence_nugget_status"], "fail")
        self.assertEqual(diagnostics["evidence_nugget_contradicted"], 1)
        self.assertEqual(diagnostics["evidence_nugget_contradicted_phrases"], "give milk")

    def test_is_bare_meningitis_vs_viral_compare_true_for_bare_compare(self):
        self.assertTrue(
            is_bare_meningitis_vs_viral_compare(
                "Could this be meningitis vs a viral illness?"
            )
        )

    def test_is_bare_meningitis_vs_viral_compare_false_for_red_flag_compare(self):
        self.assertFalse(
            is_bare_meningitis_vs_viral_compare(
                "Fever with stiff neck: meningitis vs viral illness?"
            )
        )
        self.assertFalse(
            is_bare_meningitis_vs_viral_compare(
                "Meningitis or flu when there is a dark rash?"
            )
        )

    def test_app_acceptance_classifies_uncertain_fit_gate(self):
        diagnostics = app_acceptance_diagnostics(
            {"question": "Is this a fit?", "response_text": "I cannot confidently answer."},
            bucket="abstain_or_clarify_needed",
            expected_ids=["GD-1"],
            candidate_ids=[],
            cited_ids=[],
            app_gate_fields={"app_gate_status": "uncertain_fit"},
            answer_card_fields={},
            claim_support_fields={},
            generated="no",
            safety_prompt_detector=lambda _question: False,
            emergency_contract_detector=lambda _answer: False,
        )

        self.assertEqual(diagnostics["app_acceptance_status"], "uncertain_fit_accepted")
        self.assertEqual(diagnostics["app_acceptance_root_cause"], "gate_policy")
        self.assertEqual(diagnostics["ui_surface_bucket"], "limited_fit")
        self.assertEqual(diagnostics["evidence_owner_status"], "unknown")

    def test_app_acceptance_keeps_uncertain_fit_limited_even_with_card_pass(self):
        diagnostics = app_acceptance_diagnostics(
            {"question": "Is this choking or just panic?", "response_text": "Call emergency help. [GD-1]"},
            bucket="abstain_or_clarify_needed",
            expected_ids=["GD-1"],
            candidate_ids=["GD-1"],
            cited_ids=["GD-1"],
            app_gate_fields={
                "app_gate_status": "uncertain_fit",
                "safety_critical": "true",
            },
            answer_card_fields={"answer_card_status": "pass"},
            claim_support_fields={"claim_support_status": "pass"},
            generated="no",
            safety_prompt_detector=lambda _question: True,
            emergency_contract_detector=lambda _answer: True,
        )

        self.assertEqual(diagnostics["app_acceptance_status"], "uncertain_fit_accepted")
        self.assertEqual(diagnostics["ui_surface_bucket"], "limited_fit")
        self.assertEqual(diagnostics["evidence_owner_status"], "expected_owner_cited")
        self.assertIn("gate_uncertain_fit", diagnostics["app_acceptance_reason"])

    def test_app_acceptance_classifies_expected_owner_cited_strong_path(self):
        diagnostics = app_acceptance_diagnostics(
            {"question": "Can this wait?", "response_text": "Call 911 now. [GD-1]"},
            bucket="expected_supported",
            expected_ids=["GD-1"],
            candidate_ids=["GD-1"],
            cited_ids=["GD-1"],
            app_gate_fields={"safety_critical": "true"},
            answer_card_fields={"answer_card_status": "pass"},
            claim_support_fields={"claim_support_status": "pass"},
            generated="yes",
            safety_prompt_detector=lambda _question: False,
            emergency_contract_detector=lambda _answer: True,
        )

        self.assertEqual(diagnostics["app_acceptance_status"], "strong_supported")
        self.assertEqual(diagnostics["app_acceptance_root_cause"], "supported")
        self.assertEqual(diagnostics["evidence_owner_status"], "expected_owner_cited")
        self.assertEqual(diagnostics["safety_surface_status"], "emergency_first_supported")
        self.assertEqual(diagnostics["ui_surface_bucket"], "emergency_first")

    def test_deterministic_emergency_surface_does_not_change_acceptance_status(self):
        cases = [
            (
                "RE9-SM-001",
                "indoor_combustion_co_exposure",
                "GD-899",
                "Get everyone out to fresh air immediately. Call emergency help now. [GD-899]",
            ),
            (
                "RE9-AN-001",
                "anaphylaxis_red_zone",
                "GD-400",
                "Use epinephrine if available and call emergency services now. [GD-400]",
            ),
            (
                "RE9-PO-001",
                "child_under_sink_cleaner_ingestion",
                "GD-898",
                "Call Poison Control or emergency help now before cleanup tasks. [GD-898]",
            ),
            (
                "RE9-MH-001",
                "mania_no_sleep_immediate_safety",
                "GD-859",
                "Get urgent mental-health or emergency help now; do not let them drive. [GD-859]",
            ),
        ]

        for prompt_id, decision_detail, expected_id, response_text in cases:
            with self.subTest(prompt_id=prompt_id):
                diagnostics = app_acceptance_diagnostics(
                    {
                        "question": f"{prompt_id} deterministic emergency prompt",
                        "decision_path": "deterministic",
                        "decision_detail": decision_detail,
                        "answer_provenance": "deterministic_rule",
                        "response_text": response_text,
                    },
                    bucket="deterministic_pass",
                    expected_ids=[expected_id],
                    candidate_ids=[expected_id],
                    cited_ids=[expected_id],
                    app_gate_fields={"safety_critical": "False"},
                    answer_card_fields={"answer_card_status": "no_generated_answer"},
                    claim_support_fields={"claim_support_status": "not_applicable_prompt"},
                    generated="no",
                    safety_prompt_detector=lambda _question: False,
                    emergency_contract_detector=lambda answer: "emergency" in answer.lower(),
                )

                self.assertEqual(diagnostics["app_acceptance_status"], "strong_supported")
                self.assertEqual(diagnostics["evidence_owner_status"], "expected_owner_cited")
                self.assertEqual(
                    diagnostics["safety_surface_status"],
                    "emergency_first_supported",
                )
                self.assertEqual(diagnostics["ui_surface_bucket"], "emergency_first")
                self.assertIn("bucket_deterministic_pass", diagnostics["app_acceptance_reason"])

    def test_app_acceptance_classifies_generated_expected_owner_missing(self):
        diagnostics = app_acceptance_diagnostics(
            {"question": "Can this wait?", "response_text": "Call 911 now. [GD-2]"},
            bucket="expected_supported",
            expected_ids=["GD-1"],
            candidate_ids=["GD-1"],
            cited_ids=["GD-2"],
            app_gate_fields={"safety_critical": "true"},
            answer_card_fields={"answer_card_status": "pass"},
            claim_support_fields={"claim_support_status": "pass"},
            generated="yes",
            safety_prompt_detector=lambda _question: False,
            emergency_contract_detector=lambda _answer: True,
        )

        self.assertEqual(diagnostics["app_acceptance_status"], "needs_evidence_owner")
        self.assertEqual(diagnostics["app_acceptance_root_cause"], "evidence_owner")
        self.assertEqual(
            diagnostics["evidence_owner_status"],
            "expected_owner_retrieved_not_cited",
        )
        self.assertEqual(diagnostics["safety_surface_status"], "emergency_first_supported")
        self.assertEqual(diagnostics["ui_surface_bucket"], "emergency_first")

    def test_app_acceptance_missing_owner_beats_diagnostic_bucket(self):
        diagnostics = app_acceptance_diagnostics(
            {
                "question": "Generated emergency answer with the wrong owner",
                "response_text": "Call emergency help now. [GD-2]",
                "decision_path": "deterministic",
                "answer_provenance": "deterministic_rule",
            },
            bucket="deterministic_pass",
            expected_ids=["GD-1"],
            candidate_ids=["GD-2"],
            cited_ids=["GD-2"],
            app_gate_fields={"safety_critical": "true"},
            answer_card_fields={"answer_card_status": "pass"},
            claim_support_fields={"claim_support_status": "pass"},
            generated="yes",
            safety_prompt_detector=lambda _question: True,
            emergency_contract_detector=lambda _answer: True,
        )

        self.assertEqual(diagnostics["app_acceptance_status"], "needs_evidence_owner")
        self.assertEqual(diagnostics["evidence_owner_status"], "non_expected_owner_cited")
        self.assertEqual(diagnostics["safety_surface_status"], "emergency_first_supported")
        self.assertEqual(diagnostics["ui_surface_bucket"], "emergency_first")
        self.assertIn("bucket_deterministic_pass", diagnostics["app_acceptance_reason"])

    def test_app_acceptance_classifies_safety_emergency_first_missing(self):
        diagnostics = app_acceptance_diagnostics(
            {"question": "Safety prompt", "response_text": "Watch closely at home. [GD-1]"},
            bucket="expected_supported",
            expected_ids=["GD-1"],
            candidate_ids=["GD-1"],
            cited_ids=["GD-1"],
            app_gate_fields={"safety_critical": "true"},
            answer_card_fields={"answer_card_status": "pass"},
            claim_support_fields={"claim_support_status": "pass"},
            generated="yes",
            safety_prompt_detector=lambda _question: True,
            emergency_contract_detector=lambda _answer: False,
        )

        self.assertEqual(diagnostics["app_acceptance_status"], "unsafe_or_overconfident")
        self.assertEqual(diagnostics["app_acceptance_root_cause"], "safety_surface")
        self.assertEqual(diagnostics["evidence_owner_status"], "expected_owner_cited")
        self.assertEqual(diagnostics["safety_surface_status"], "emergency_first_missing")
        self.assertEqual(diagnostics["ui_surface_bucket"], "limited_fit")


if __name__ == "__main__":
    unittest.main()
