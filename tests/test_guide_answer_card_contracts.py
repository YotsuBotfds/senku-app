import unittest

import query
from guide_answer_card_contracts import (
    build_evidence_packet,
    compose_card_backed_answer,
    compose_evidence_units,
    evaluate_answer_card_contract,
    find_cards_for_guides,
    load_answer_cards,
)
from rag_claim_support import diagnose_claim_support


class GuideAnswerCardContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.cards = load_answer_cards()

    def test_load_answer_cards_includes_contract_fields(self):
        card = next(
            card
            for card in self.cards
            if card["card_id"] == "poisoning_unknown_ingestion"
        )

        self.assertEqual(card["guide_id"], "GD-898")
        self.assertEqual(card["risk_tier"], "critical")
        self.assertIn("required_first_actions", card)
        self.assertIn("urgent_red_flags", card)
        self.assertIn("forbidden_advice", card)
        self.assertIn("acceptable_uncertain_fit", card)
        self.assertIn("source_sections", card)

    def test_load_answer_cards_preserves_source_invariants(self):
        card = next(
            card
            for card in self.cards
            if card["card_id"] == "choking_airway_obstruction"
        )

        self.assertIn("source_invariants", card)
        self.assertEqual(
            card["source_invariants"][0]["name"],
            "no blind finger sweeps in pediatric airway support",
        )

    def test_compose_evidence_units_returns_active_card_fields(self):
        card = find_cards_for_guides("GD-898", cards=self.cards)[0]

        units = compose_evidence_units([card])

        self.assertEqual(len(units), 1)
        unit = units[0]
        self.assertEqual(unit["unit_id"], "poisoning_unknown_ingestion")
        self.assertEqual(unit["citation_ids"][0], "unknown-ingestion-overview")
        self.assertIn("GD-301", unit["source_guide_ids"])
        self.assertTrue(unit["source_sections"])
        self.assertTrue(unit["source_invariants"])
        self.assertTrue(unit["support_phrases"])

    def test_load_answer_cards_includes_toxicology_contract_fields(self):
        card = next(
            card
            for card in self.cards
            if card["card_id"] == "poisoning_unknown_ingestion_toxicology"
        )

        self.assertEqual(card["guide_id"], "GD-301")
        self.assertEqual(card["risk_tier"], "critical")
        self.assertIn("required_first_actions", card)
        self.assertIn("forbidden_advice", card)
        self.assertIn("source_sections", card)

    def test_compose_evidence_units_returns_gd301_fields(self):
        card = next(
            card
            for card in find_cards_for_guides("GD-301", cards=self.cards)
            if card["card_id"] == "poisoning_unknown_ingestion_toxicology"
        )

        units = compose_evidence_units([card])

        self.assertEqual(len(units), 1)
        unit = units[0]
        self.assertEqual(unit["unit_id"], "poisoning_unknown_ingestion_toxicology")
        self.assertEqual(unit["source_guide_ids"], ["GD-301"])
        self.assertIn("toxicology-overview", unit["citation_ids"])
        self.assertIn("GD-301", unit["source_guide_ids"])
        self.assertTrue(unit["support_phrases"])

    def test_compose_card_backed_answer_for_gd301_passes_contract_and_claim_support(self):
        card = next(
            card
            for card in find_cards_for_guides("GD-301", cards=self.cards)
            if card["card_id"] == "poisoning_unknown_ingestion_toxicology"
        )

        plan = compose_card_backed_answer([card], allowed_guide_ids=["GD-301"])

        self.assertEqual(plan["status"], "ready")
        self.assertEqual(plan["card_ids"], ["poisoning_unknown_ingestion_toxicology"])
        self.assertEqual(plan["cited_guide_ids"], ["GD-301"])
        self.assertIn("Call poison control", plan["answer_text"])
        self.assertIn("[GD-301]", plan["answer_text"])
        contract = evaluate_answer_card_contract(plan["answer_text"], [card])
        self.assertEqual(contract["status"], "pass")
        self.assertEqual(contract["forbidden_advice_hits"], [])
        claim = diagnose_claim_support(
            plan["answer_text"],
            [card],
            cited_guide_ids=plan["cited_guide_ids"],
            expected_guide_ids=["GD-301"],
        )
        self.assertEqual(claim["status"], "pass")
        self.assertEqual(claim["forbidden_count"], 0)

    def test_gd301_contract_flags_unnegated_harmful_charcoal_advice(self):
        card = next(
            card
            for card in find_cards_for_guides("GD-301", cards=self.cards)
            if card["card_id"] == "poisoning_unknown_ingestion_toxicology"
        )
        answer = """
        Call poison control now. The patient is drowsy and the exposure is unknown.
        Give activated charcoal for unknown ingestion now and hope for the best.
        """

        result = evaluate_answer_card_contract(answer, [card])

        self.assertEqual(result["status"], "fail")
        self.assertTrue(result["forbidden_advice_hits"])
        self.assertTrue(
            any(
                "activated charcoal" in record["phrase"]
                for record in result["forbidden_advice_hits"]
            )
        )

    def test_compose_evidence_units_resolves_conditional_requirements_from_question(self):
        card = find_cards_for_guides("GD-284", cards=self.cards)[0]

        non_cord_units = compose_evidence_units(
            [card],
            question_text="My 2 week old has a fever and will not feed.",
        )
        cord_units = compose_evidence_units(
            [card],
            question_text="The umbilical cord stump has pus and spreading redness.",
        )

        self.assertEqual(non_cord_units[0]["active_conditional_required_actions"], [])
        self.assertIn(
            "If umbilical infection has fever or spreading redness, treat it as life-threatening and seek antibiotic-capable care.",
            cord_units[0]["active_conditional_required_actions"],
        )

    def test_compose_evidence_units_support_phrases_include_basis_labels(self):
        card = find_cards_for_guides("GD-898", cards=self.cards)[0]

        unit = compose_evidence_units([card])[0]
        basis_labels = {record["basis"] for record in unit["support_phrases"]}

        self.assertIn("card_required_action", basis_labels)
        self.assertIn("card_red_flag", basis_labels)
        self.assertIn("card_negative_safety_instruction", basis_labels)
        self.assertTrue(
            all(record["card_id"] == "poisoning_unknown_ingestion" for record in unit["support_phrases"])
        )

    def test_build_evidence_packet_keeps_card_order_and_guide_owners(self):
        abdomen_card = find_cards_for_guides("GD-380", cards=self.cards)[0]
        poisoning_card = find_cards_for_guides("GD-898", cards=self.cards)[0]

        packet = build_evidence_packet([abdomen_card, poisoning_card])

        self.assertEqual(packet["status"], "ready")
        self.assertEqual(
            packet["card_ids"],
            ["abdominal_internal_bleeding", "poisoning_unknown_ingestion"],
        )
        self.assertEqual(packet["cards"], packet["evidence_units"])
        self.assertIn("GD-380", packet["guide_ids"])
        self.assertIn("GD-898", packet["guide_ids"])

    def test_build_evidence_packet_returns_no_cards_for_empty_input(self):
        packet = build_evidence_packet([])

        self.assertEqual(packet["status"], "no_cards")
        self.assertEqual(packet["cards"], [])

    def test_build_evidence_packet_caps_cards_and_dedupes_source_guide_ids(self):
        poisoning_card = find_cards_for_guides("GD-898", cards=self.cards)[0]
        abdomen_card = find_cards_for_guides("GD-380", cards=self.cards)[0]

        packet = build_evidence_packet(
            [poisoning_card, abdomen_card],
            max_cards=1,
        )

        self.assertEqual(packet["card_ids"], ["poisoning_unknown_ingestion"])
        self.assertEqual(packet["guide_ids"].count("GD-898"), 1)
        self.assertIn("GD-301", packet["guide_ids"])
        self.assertNotIn("GD-380", packet["guide_ids"])

    def test_compose_card_backed_answer_orders_required_actions_first(self):
        card = find_cards_for_guides("GD-898", cards=self.cards)[0]

        plan = compose_card_backed_answer([card], allowed_guide_ids=["GD-898"])

        self.assertEqual(plan["status"], "ready")
        self.assertEqual(plan["card_ids"], ["poisoning_unknown_ingestion"])
        self.assertEqual(plan["cited_guide_ids"], ["GD-898"])
        self.assertEqual(plan["lines"][0]["kind"], "required_first_action")
        self.assertIn(
            "Call poison control, EMS, or the fastest available clinician immediately",
            plan["answer_text"],
        )
        self.assertIn("[GD-898]", plan["answer_text"])
        result = evaluate_answer_card_contract(plan["answer_text"], [card])
        self.assertEqual(result["status"], "pass")

    def test_compose_card_backed_answer_resolves_active_conditional_branch(self):
        card = find_cards_for_guides("GD-284", cards=self.cards)[0]

        non_cord_plan = compose_card_backed_answer(
            [card],
            question_text="My newborn has a fever and will not feed.",
        )
        cord_plan = compose_card_backed_answer(
            [card],
            question_text="The umbilical cord stump has pus and spreading redness.",
        )

        self.assertNotIn("umbilical infection", non_cord_plan["answer_text"])
        self.assertIn("umbilical infection", cord_plan["answer_text"])
        result = evaluate_answer_card_contract(
            cord_plan["answer_text"],
            [card],
            question_text="The umbilical cord stump has pus and spreading redness.",
        )
        self.assertEqual(result["status"], "pass")

    def test_compose_card_backed_answer_keeps_negative_safety_supported(self):
        card = find_cards_for_guides("GD-898", cards=self.cards)[0]

        plan = compose_card_backed_answer([card])

        self.assertIn("Avoid: Do not induce vomiting", plan["answer_text"])
        card_result = evaluate_answer_card_contract(plan["answer_text"], [card])
        self.assertEqual(card_result["forbidden_advice_hits"], [])
        claim_result = diagnose_claim_support(
            plan["answer_text"],
            [card],
            cited_guide_ids=plan["cited_guide_ids"],
            expected_guide_ids=["GD-898"],
        )
        self.assertEqual(claim_result["status"], "pass")
        self.assertEqual(claim_result["forbidden_count"], 0)

    def test_dangerous_activation_card_leads_with_crisis_escalation(self):
        card = find_cards_for_guides("GD-859", cards=self.cards)[0]

        self.assertEqual(card["card_id"], "dangerous_activation_mania_crisis")
        plan = compose_card_backed_answer([card], allowed_guide_ids=["GD-859"])

        self.assertEqual(plan["status"], "ready")
        self.assertEqual(plan["cited_guide_ids"], ["GD-859"])
        self.assertIn("acute mental health crisis", plan["answer_text"])
        self.assertIn("urgent medical evaluation", plan["answer_text"])
        self.assertIn("[GD-859]", plan["answer_text"])
        card_result = evaluate_answer_card_contract(plan["answer_text"], [card])
        self.assertEqual(card_result["status"], "pass")
        claim_result = diagnose_claim_support(
            plan["answer_text"],
            [card],
            cited_guide_ids=plan["cited_guide_ids"],
            expected_guide_ids=["GD-859"],
        )
        self.assertEqual(claim_result["status"], "pass")
        self.assertEqual(claim_result["forbidden_count"], 0)

    def test_acute_coronary_overlap_card_stays_emergency_first(self):
        card = find_cards_for_guides("GD-601", cards=self.cards)[0]

        self.assertEqual(card["card_id"], "acute_coronary_stroke_overlap")
        plan = compose_card_backed_answer([card], allowed_guide_ids=["GD-601"])

        self.assertEqual(plan["status"], "ready")
        self.assertEqual(plan["cited_guide_ids"], ["GD-601"])
        self.assertIn("cardiac emergency", plan["answer_text"])
        self.assertIn("fastest available evacuation", plan["answer_text"])
        self.assertIn("do not give food, water, or pills", plan["answer_text"])
        self.assertIn("[GD-601]", plan["answer_text"])
        card_result = evaluate_answer_card_contract(plan["answer_text"], [card])
        self.assertEqual(card_result["status"], "pass")
        claim_result = diagnose_claim_support(
            plan["answer_text"],
            [card],
            cited_guide_ids=plan["cited_guide_ids"],
            expected_guide_ids=["GD-601"],
        )
        self.assertEqual(claim_result["status"], "pass")
        self.assertEqual(claim_result["forbidden_count"], 0)

    def test_compose_card_backed_answer_does_not_cite_unallowed_backup_owner(self):
        card = [
            candidate
            for candidate in self.cards
            if candidate["card_id"] == "abdominal_internal_bleeding"
        ][0]

        plan = compose_card_backed_answer([card], allowed_guide_ids=["GD-232"])

        self.assertEqual(plan["cited_guide_ids"], [])
        self.assertNotIn("[GD-232]", plan["answer_text"])
        self.assertNotIn("[GD-380]", plan["answer_text"])

    def test_compose_card_backed_answer_can_cite_reviewed_source_family_when_opted_in(self):
        card = find_cards_for_guides("GD-284", cards=self.cards)[0]

        plan = compose_card_backed_answer([card], allowed_guide_ids=["GD-492"])

        self.assertEqual(plan["cited_guide_ids"], ["GD-492"])
        self.assertIn("[GD-492]", plan["answer_text"])
        self.assertNotIn("[GD-284]", plan["answer_text"])

    def test_compose_card_backed_answer_returns_no_cards_for_empty_input(self):
        plan = compose_card_backed_answer([])

        self.assertEqual(plan["status"], "no_cards")
        self.assertEqual(plan["answer_text"], "")

    def test_find_cards_for_guides_filters_by_guide_id(self):
        matches = find_cards_for_guides(["GD-898", "GD-NOPE"], cards=self.cards)

        self.assertEqual([card["card_id"] for card in matches], ["poisoning_unknown_ingestion"])

    def test_evaluate_passes_when_critical_required_actions_are_present(self):
        cards = find_cards_for_guides("GD-898", cards=self.cards)
        answer = """
        Call poison control EMS immediately for unknown ingestion. Check airway;
        breathing before product identification comes next. Make the scene safe,
        stop cleanup until help gives instructions, and do not induce vomiting.
        """

        result = evaluate_answer_card_contract(answer, cards)

        self.assertEqual(result["status"], "pass")
        self.assertEqual(len(result["required_action_hits"]), 4)
        self.assertEqual(result["missing_required_actions"], [])
        self.assertEqual(result["forbidden_advice_hits"], [])

    def test_evaluate_marks_partial_when_some_critical_actions_are_missing(self):
        cards = find_cards_for_guides("GD-232", cards=self.cards)
        answer = "Keep them upright, encourage coughing, and watch closely."

        result = evaluate_answer_card_contract(answer, cards)

        self.assertEqual(result["status"], "partial")
        self.assertEqual(len(result["required_action_hits"]), 1)
        self.assertGreater(len(result["missing_required_actions"]), 0)

    def test_evaluate_marks_partial_when_branch_action_is_present(self):
        cards = find_cards_for_guides("GD-232", cards=self.cards)
        answer = "For an infant, use 5 back blows and 5 chest thrusts."

        result = evaluate_answer_card_contract(answer, cards)

        self.assertEqual(result["status"], "partial")
        self.assertEqual(result["required_action_hits"], [])
        self.assertTrue(result["card_results"][0]["first_action_hits"])

    def test_unknown_age_choking_prompt_accepts_age_appropriate_branching(self):
        cards = find_cards_for_guides("GD-232", cards=self.cards)
        answer = """
        If they are coughing and talking, keep them upright, encourage coughing,
        and watch closely. If they cannot speak, cough, or breathe, call for
        emergency help and start age-appropriate choking rescue. If they collapse,
        lower them to the floor, call for help/AED, start CPR, and remove only a
        clearly visible object.
        """

        result = evaluate_answer_card_contract(
            answer,
            cards,
            question_text="Someone is choking and I do not know their age. What should I do?",
        )

        self.assertEqual(result["status"], "pass")
        missing_phrases = [item["phrase"] for item in result["missing_required_actions"]]
        self.assertNotIn(
            "If an adult or older child cannot speak, cough, or breathe, call for emergency help and start abdominal thrusts.",
            missing_phrases,
        )
        self.assertNotIn(
            "If an infant cannot cry, cough, or breathe, use 5 back blows followed by 5 chest thrusts.",
            missing_phrases,
        )

    def test_infant_choking_prompt_enforces_infant_mechanics(self):
        cards = find_cards_for_guides("GD-232", cards=self.cards)
        answer = """
        If the baby cannot breathe, call for emergency help and start
        age-appropriate choking rescue. If they collapse, lower them to the
        floor, call for help/AED, start CPR, and remove only a clearly visible
        object.
        """

        result = evaluate_answer_card_contract(
            answer,
            cards,
            question_text="My baby is choking and cannot cry.",
        )

        missing_phrases = [item["phrase"] for item in result["missing_required_actions"]]
        self.assertIn(
            "If an infant cannot cry, cough, or breathe, use 5 back blows followed by 5 chest thrusts.",
            missing_phrases,
        )

    def test_meningitis_red_flag_prompt_does_not_require_sepsis_first_hour_language(self):
        cards = find_cards_for_guides("GD-589", cards=self.cards)
        answer = """
        Treat fever plus meningitis or brain-warning signs as suspected
        meningitis or meningococcemia. Escalate urgently for emergency medical
        evaluation and antibiotic-capable care.
        """

        result = evaluate_answer_card_contract(
            answer,
            cards,
            question_text="Child has fever, stiff neck, and purple rash that does not fade.",
        )

        self.assertEqual(result["status"], "pass")
        missing_phrases = [item["phrase"] for item in result["missing_required_actions"]]
        self.assertNotIn(
            "If sepsis, shock, or very-sick appearance is explicitly suspected, prioritize immediate sepsis screening and first-hour priorities.",
            missing_phrases,
        )

    def test_meningitis_sepsis_framing_enforces_first_hour_language(self):
        cards = find_cards_for_guides("GD-589", cards=self.cards)
        answer = """
        Treat fever with confusion as suspected meningitis or meningococcemia.
        Escalate urgently for emergency medical evaluation and antibiotic-capable
        care.
        """

        result = evaluate_answer_card_contract(
            answer,
            cards,
            question_text="Child with fever and confusion looks very sick; could this be sepsis?",
        )

        missing_phrases = [item["phrase"] for item in result["missing_required_actions"]]
        self.assertIn(
            "If sepsis, shock, or very-sick appearance is explicitly suspected, prioritize immediate sepsis screening and first-hour priorities.",
            missing_phrases,
        )

    def test_evaluate_preserves_negative_safety_instruction(self):
        cards = find_cards_for_guides("GD-898", cards=self.cards)
        answer = """
        Call poison control EMS immediately. Check airway and breathing. Do not
        give activated charcoal for caustics hydrocarbons unless you know the
        airway is safe.
        """

        result = evaluate_answer_card_contract(answer, cards)

        self.assertNotEqual(result["status"], "fail")
        self.assertEqual(result["forbidden_advice_hits"], [])

    def test_evaluate_fails_when_forbidden_advice_is_positive_advice(self):
        cards = find_cards_for_guides("GD-898", cards=self.cards)
        answer = """
        Call poison control EMS immediately. Check airway and breathing. Give
        activated charcoal for caustics or hydrocarbons while arranging help.
        """

        result = evaluate_answer_card_contract(answer, cards)

        self.assertEqual(result["status"], "fail")
        self.assertEqual(len(result["forbidden_advice_hits"]), 1)
        self.assertIn("activated charcoal", result["forbidden_advice_hits"][0]["phrase"])

    def test_evaluate_returns_no_cards_status_for_empty_contract_set(self):
        result = evaluate_answer_card_contract("Any answer text.", [])

        self.assertEqual(result["status"], "no_cards")
        self.assertEqual(result["card_results"], [])

    def test_newborn_non_cord_prompt_does_not_require_cord_infection_branch(self):
        cards = find_cards_for_guides("GD-284", cards=self.cards)
        answer = """
        Seek urgent medical evaluation for a newborn with fever, breathing
        trouble, poor feeding, or unusual sleepiness. Keep the newborn warm and
        monitor breathing closely while arranging urgent evaluation.
        """

        result = evaluate_answer_card_contract(
            answer,
            cards,
            question_text="My 2 week old has a fever and is not feeding well.",
        )

        self.assertEqual(result["status"], "pass")
        self.assertEqual(len(result["required_action_hits"]), 2)
        self.assertEqual(result["missing_required_actions"], [])

    def test_newborn_cord_prompt_enforces_conditional_antibiotic_care_branch(self):
        cards = find_cards_for_guides("GD-284", cards=self.cards)
        answer = """
        Get emergency help now for a sick newborn. Keep the newborn warm and
        monitor breathing closely while arranging urgent evaluation.
        """

        result = evaluate_answer_card_contract(
            answer,
            cards,
            question_text="The umbilical cord stump has pus and spreading redness.",
        )

        self.assertEqual(result["status"], "partial")
        self.assertEqual(len(result["required_action_hits"]), 2)
        missing_phrases = [item["phrase"] for item in result["missing_required_actions"]]
        self.assertIn(
            "If umbilical infection has fever or spreading redness, treat it as life-threatening and seek antibiotic-capable care.",
            missing_phrases,
        )

    def test_newborn_danger_prompt_note_requires_warmth_while_arranging_evaluation(self):
        notes = query._prompt_mode_notes(
            "default",
            {"objective_coverage": []},
            "My newborn has a fever and will not feed.",
        )

        newborn_notes = "\n".join(note for note in notes if "Newborn danger signs" in note)
        self.assertIn("keep the newborn warm", newborn_notes)
        self.assertIn("urgent medical evaluation", newborn_notes)
        self.assertIn("while arranging that help", newborn_notes)


if __name__ == "__main__":
    unittest.main()
