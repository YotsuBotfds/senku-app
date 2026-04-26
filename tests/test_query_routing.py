import unittest
from unittest.mock import patch

import query
from guide_answer_card_contracts import (
    evaluate_answer_card_contract,
    find_cards_for_guides,
    load_answer_cards,
)
from rag_claim_support import diagnose_claim_support


class QueryRoutingTests(unittest.TestCase):
    def test_house_build_prompt_gets_construction_domain_and_building_specs(self):
        frame = query.build_scenario_frame("how do i build a house")
        self.assertIn("construction", frame["domains"])

        specs = query._supplemental_retrieval_specs("how do i build a house", 10)
        self.assertTrue(any(spec["category"] == "building" for spec in specs))
        self.assertTrue(any("one room house" in spec["text"] for spec in specs))
        self.assertTrue(any("window door assembly" in spec["text"] for spec in specs))
        self.assertTrue(any("roof waterproofing foundation drainage" in spec["text"] for spec in specs))

    def test_canoe_prompt_gets_construction_domain_and_transport_specs(self):
        frame = query.build_scenario_frame("how do i build a canoe")
        self.assertIn("construction", frame["domains"])

        specs = query._supplemental_retrieval_specs("how do i build a canoe", 10)
        self.assertTrue(any(spec["category"] == "transportation" for spec in specs))
        self.assertTrue(any("dugout canoe construction" in spec["text"] for spec in specs))
        self.assertTrue(any("boat caulking pitch resin" in spec["text"] for spec in specs))

    def test_water_purification_and_storage_prompts_get_targeted_specs(self):
        purification_frame = query.build_scenario_frame("how do i build a charcoal sand water filter")
        self.assertIn("water", purification_frame["domains"])
        purification_specs = query._supplemental_retrieval_specs("how do i build a charcoal sand water filter", 10)
        self.assertTrue(any(spec["category"] == "survival" for spec in purification_specs))
        self.assertTrue(any(spec["category"] == "building" for spec in purification_specs))
        self.assertTrue(any(spec["category"] == "chemistry" for spec in purification_specs))
        self.assertTrue(any("water purification filtration systems" in spec["text"] for spec in purification_specs))
        self.assertTrue(any("charcoal sand water filter" in spec["text"] for spec in purification_specs))
        self.assertTrue(any("biosand filter construction" in spec["text"] for spec in purification_specs))

        storage_frame = query.build_scenario_frame("how do i store water safely in reused containers")
        self.assertIn("water", storage_frame["domains"])
        storage_specs = query._supplemental_retrieval_specs("how do i store water safely in reused containers", 10)
        self.assertTrue(any(spec["category"] == "resource-management" for spec in storage_specs))
        self.assertTrue(any(spec["category"] == "building" for spec in storage_specs))
        self.assertTrue(any("safe water storage" in spec["text"] for spec in storage_specs))
        self.assertTrue(any("water storage systems rationing protocol" in spec["text"] for spec in storage_specs))
        self.assertTrue(any("container selection preparation food grade" in spec["text"] for spec in storage_specs))

    def test_generic_puncture_prompt_gets_conservative_medical_specs(self):
        frame = query.build_scenario_frame("what do i do for a deep puncture wound")
        self.assertIn("medical", frame["domains"])
        specs = query._supplemental_retrieval_specs("what do i do for a deep puncture wound", 10)
        self.assertTrue(any("puncture wound first aid do not probe" in spec["text"] for spec in specs))
        self.assertTrue(any("wound hygiene infection prevention field sanitation" in spec["text"] for spec in specs))

    def test_safety_triage_profile_adds_owner_focused_specs(self):
        choking_specs = query._supplemental_retrieval_specs(
            "child is choking on a grape", 8
        )
        self.assertEqual(
            query._retrieval_profile_for_question("child is choking on a grape"),
            "safety_triage",
        )
        self.assertTrue(
            any("emergency-airway-management choking" in spec["text"] for spec in choking_specs)
        )
        food_bolus_specs = query._supplemental_retrieval_specs(
            "drooling and cannot swallow after a bite of food", 8
        )
        self.assertEqual(
            query._retrieval_profile_for_question(
                "drooling and cannot swallow after a bite of food"
            ),
            "safety_triage",
        )
        self.assertTrue(
            any("foreign body airway obstruction" in spec["text"] for spec in food_bolus_specs)
        )
        self.assertTrue(
            any("no poison ingestion" in spec["text"] for spec in food_bolus_specs)
        )
        self.assertEqual(
            query._retrieval_profile_for_question("is this choking or just panic"),
            "normal_vs_urgent",
        )
        meningitis_specs = query._supplemental_retrieval_specs(
            "is this meningitis or a viral illness", 8
        )
        self.assertEqual(
            query._retrieval_profile_for_question("is this meningitis or a viral illness"),
            "compare_or_boundary",
        )
        self.assertTrue(
            any("suspected meningitis meningococcemia viral illness" in spec["text"] for spec in meningitis_specs)
        )

        newborn_specs = query._supplemental_retrieval_specs(
            "newborn is limp, will not feed, and is hard to wake", 8
        )
        self.assertTrue(
            any("newborn sepsis neonatal fever" in spec["text"] for spec in newborn_specs)
        )

        newborn_holdout_specs = query._supplemental_retrieval_specs(
            "A newborn is not feeding well, feels cold, and we have limited phone battery during an outage.",
            8,
        )
        self.assertEqual(
            query._retrieval_profile_for_question(
                "A newborn is not feeding well, feels cold, and we have limited phone battery during an outage."
            ),
            "safety_triage",
        )
        self.assertTrue(
            any("newborn sepsis neonatal fever" in spec["text"] for spec in newborn_holdout_specs)
        )

        abdomen_specs = query._supplemental_retrieval_specs(
            "child fell and now has belly pain", 8
        )
        self.assertTrue(
            any("abdominal trauma hard belly" in spec["text"] for spec in abdomen_specs)
        )
        self.assertTrue(
            any("child fall belly pain" in spec["text"] for spec in abdomen_specs)
        )
        handlebar_specs = query._supplemental_retrieval_specs(
            "left side pain after handlebar injury", 8
        )
        self.assertEqual(
            query._retrieval_profile_for_question("left side pain after handlebar injury"),
            "safety_triage",
        )
        self.assertTrue(
            any("handlebar injury shock" in spec["text"] for spec in handlebar_specs)
        )
        self.assertTrue(
            any("solid organ injury" in spec["text"] for spec in handlebar_specs)
        )

    def test_newborn_danger_phrase_normalization_stays_retrieval_only(self):
        prompts = [
            "A newborn is not feeding well and feels cold during an outage.",
            "The baby is 3 weeks old, harder to wake than usual, and the house is dropping below freezing.",
            "The infant is cold to touch and not nursing well.",
            "22 days old and difficult to wake up after the storm.",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertTrue(query._is_newborn_sepsis_danger_retrieval_query(prompt))
                self.assertEqual(query._retrieval_profile_for_question(prompt), "safety_triage")
                specs = query._supplemental_retrieval_specs(prompt, 8)
                self.assertTrue(
                    any("newborn sepsis neonatal fever" in spec["text"] for spec in specs)
                )

        self.assertFalse(
            query._is_newborn_sepsis_danger_query(
                "A newborn is not feeding well and feels cold during an outage."
            )
        )

    def test_newborn_danger_phrase_normalization_guards_routine_baby_prompts(self):
        prompts = [
            "How do I keep a baby warm during an outage with limited phone battery?",
            "Baby has a mild cold and the water is out; how do we clean bottles?",
            "Infant feeding schedule during an outage.",
            "The house feels cold and we have no clean tap water.",
            "My baby is harder to settle at bedtime.",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertFalse(query._is_newborn_sepsis_danger_retrieval_query(prompt))
                specs = query._supplemental_retrieval_specs(prompt, 8)
                self.assertFalse(
                    any("newborn sepsis neonatal fever" in spec["text"] for spec in specs)
                )

    def test_boundary_safety_prompt_uses_normal_vs_urgent_profile(self):
        self.assertEqual(
            query._retrieval_profile_for_question(
                "do we watch at home or get urgent help first"
            ),
            "normal_vs_urgent",
        )

    def test_rag_eval_unresolved_partial_rerank_owner_hints(self):
        cases = [
            (
                "The lights are out and it is below freezing. What are the most important steps to set up a livable shelter and reduce hypothermia risk tonight with basic materials?",
                {"guide_id": "GD-024", "guide_title": "Winter Survival Systems"},
                {"guide_id": "GD-345", "guide_title": "Primitive Shelter Construction Techniques"},
                -0.13,
            ),
            (
                "After a small camp accident, one person is confused and dizzy while another has minor scrapes. How should a responder sort priorities in the first 10 minutes?",
                {"guide_id": "GD-029", "guide_title": "Disaster Triage & MCI"},
                {"guide_id": "GD-558", "guide_title": "Minor Wound Care"},
                -0.14,
            ),
            (
                "Two old medicine labels disagree and the patient weight is uncertain. What can Senku safely verify before giving anything?",
                {"guide_id": "GD-239", "guide_title": "Medications"},
                {"guide_id": "GD-056", "guide_title": "Pharmacy Inventory"},
                -0.12,
            ),
            (
                "We have one small solar panel and limited battery capacity after power loss. What is the safest setup order to keep charging for lights and phone alerts?",
                {"guide_id": "GD-108", "guide_title": "Solar Technology"},
                {"guide_id": "GD-288", "guide_title": "Emergency Power Bootstrap"},
                -0.13,
            ),
            (
                "What is a practical checklist for choosing a shelter site to avoid flooding risk and wind exposure before nightfall?",
                {"guide_id": "GD-446", "guide_title": "Shelter Site Selection & Hazard Assessment"},
                {"guide_id": "GD-023", "guide_title": "Survival Basics & First 72 Hours"},
                -0.13,
            ),
            (
                "After flooding, our water is cloudy and has debris. What is the safest order of steps to make a drinking-water plan immediately?",
                {"guide_id": "GD-035", "guide_title": "Water Purification"},
                {"guide_id": "GD-931", "guide_title": "Questionable Water Assessment"},
                -0.22,
            ),
            (
                "I got burned on my forearm by hot water. What should I do first, what should I not do, and when should I escalate?",
                {"guide_id": "GD-052", "guide_title": "Burn Treatment & Management"},
                {"guide_id": "GD-946", "guide_title": "Sunburn, Sun Protection & Heat-Skin Care"},
                -0.15,
            ),
            (
                "How should I safely preserve cooked food for the next few days during a prolonged outage?",
                {"guide_id": "GD-065", "guide_title": "Food Preservation"},
                {"guide_id": "GD-260", "guide_title": "Food Salvage & Shelf Life"},
                -0.14,
            ),
            (
                "During storm evacuation, an adult has chest pressure, arm tingling, and wants to drive themself while we also handle animals and water.",
                {"guide_id": "GD-601", "guide_title": "Acute Coronary Syndrome & Cardiac Emergencies"},
                {"guide_id": "GD-695", "guide_title": "Storm Preparedness and Recovery"},
                -0.22,
            ),
        ]

        rule_by_guide = {
            rule["guide_id"]: rule for rule in query._load_rag_owner_rerank_hints()
        }
        for question, owner_meta, distractor_meta, expected_delta in cases:
            with self.subTest(owner=owner_meta["guide_id"]):
                applied = []
                query._apply_rag_owner_rerank_hints(
                    question.lower(),
                    owner_meta["guide_id"],
                    lambda branch, delta: applied.append((branch, delta)),
                )
                self.assertEqual(len(applied), 1)
                self.assertEqual(
                    applied[0][0],
                    rule_by_guide[owner_meta["guide_id"]]["branch"],
                )
                self.assertAlmostEqual(applied[0][1], expected_delta)
                self.assertLess(
                    query._metadata_rerank_delta(question, owner_meta),
                    query._metadata_rerank_delta(question, distractor_meta),
                )

    def test_rag_eval_bridge_rerank_owner_hints(self):
        cases = [
            (
                "We have inconsistent water flow and no maintenance tools. What is a safe minimum operations checklist to stabilize a small water system?",
                {"guide_id": "GD-648", "guide_title": "Water System Lifecycle - Drilling to Troubleshooting"},
                {"guide_id": "GD-935", "guide_title": "Cold Water Immersion and Drowning Prevention"},
                -0.13,
            ),
            (
                "Field treatment needs reusable instruments cleaned between patients. What is a safe sterilization workflow we can do with limited fuel and supplies?",
                {"guide_id": "GD-646", "guide_title": "Sterilization Ecosystem - Water Testing to Medical Application"},
                {"guide_id": "GD-250", "guide_title": "Boiling and Water Disinfection"},
                -0.14,
            ),
            (
                "A community kitchen is getting understocked. How can we prioritize procurement, storage, and cooking reliability for the next week?",
                {"guide_id": "GD-637", "guide_title": "Community Nutrition Pipeline"},
                {"guide_id": "GD-252", "guide_title": "Food Storage Management"},
                -0.12,
            ),
            (
                "Our shelter has only limited medical supplies and few trained people. How can a local team make safe, high-impact decisions at once?",
                {"guide_id": "GD-635", "guide_title": "Healthcare Capability Assessment"},
                {"guide_id": "GD-667", "guide_title": "Shelter Medical Station Setup"},
                -0.13,
            ),
            (
                "After a storm, the electrical system is partially alive. What is a safe sequence for load triage and temporary restoration?",
                {"guide_id": "GD-649", "guide_title": "Electrical System Bootstrap - Hand-Crank to Microgrid"},
                {"guide_id": "GD-695", "guide_title": "Storm Preparedness and Recovery"},
                -0.22,
            ),
        ]

        rule_by_guide = {
            rule["guide_id"]: rule for rule in query._load_rag_owner_rerank_hints()
        }
        for question, owner_meta, distractor_meta, expected_delta in cases:
            with self.subTest(owner=owner_meta["guide_id"]):
                applied = []
                query._apply_rag_owner_rerank_hints(
                    question.lower(),
                    owner_meta["guide_id"],
                    lambda branch, delta: applied.append((branch, delta)),
                )
                self.assertEqual(len(applied), 1)
                self.assertEqual(
                    applied[0][0],
                    rule_by_guide[owner_meta["guide_id"]]["branch"],
                )
                self.assertAlmostEqual(applied[0][1], expected_delta)
                self.assertLess(
                    query._metadata_rerank_delta(question, owner_meta),
                    query._metadata_rerank_delta(question, distractor_meta),
                )

    def test_rag_eval_owner_hint_manifest_scope_is_strict(self):
        expected_guide_ids = [
            "GD-024",
            "GD-029",
            "GD-239",
            "GD-108",
            "GD-446",
            "GD-035",
            "GD-052",
            "GD-065",
            "GD-635",
            "GD-649",
            "GD-648",
            "GD-646",
            "GD-637",
            "GD-601",
        ]
        rules = query._load_rag_owner_rerank_hints()
        self.assertEqual([rule["guide_id"] for rule in rules], expected_guide_ids)

        broad_questions = [
            "How do I prepare for winter?",
            "What should I know about water?",
            "How do I keep food safe?",
            "How should I plan a shelter?",
        ]
        for question in broad_questions:
            for guide_id in expected_guide_ids:
                with self.subTest(question=question, guide_id=guide_id):
                    applied = []
                    query._apply_rag_owner_rerank_hints(
                        question.lower(),
                        guide_id,
                        lambda branch, delta: applied.append((branch, delta)),
                    )
                    self.assertEqual(applied, [])

    def test_fire_in_rain_and_join_metal_without_welder_get_targeted_specs(self):
        fire_specs = query._supplemental_retrieval_specs("how do i start a fire in rain", 10)
        self.assertTrue(any("fire in wet conditions" in spec["text"] for spec in fire_specs))
        self.assertTrue(any(spec["category"] == "survival" for spec in fire_specs))

        weld_specs = query._supplemental_retrieval_specs("how do i join metal without a welder", 10)
        self.assertTrue(any(spec["category"] == "metalworking" for spec in weld_specs))
        self.assertTrue(any("forge welding brazing soldering" in spec["text"] for spec in weld_specs))

    def test_house_metadata_rerank_prefers_building_over_census(self):
        question = "how do i build a house"
        building_meta = {
            "guide_title": "Primitive Shelter Construction Techniques",
            "section_heading": "One-Room Cabin",
            "slug": "primitive-shelter-construction-techniques",
            "description": "Site selection, foundations, roof framing, and weatherproofing.",
            "category": "building",
            "related": "",
            "tags": "cabin, shelter construction, foundation, roofing",
            "source_file": "GD-345.md",
        }
        census_meta = {
            "guide_title": "Census, Vital Records & Population Tracking",
            "section_heading": "Household Surveys",
            "slug": "census-vital-records-population-tracking",
            "description": "Population census, household surveys, and record organization.",
            "category": "society",
            "related": "",
            "tags": "population census, household surveys, records",
            "source_file": "GD-208.md",
        }

        self.assertLess(
            query._metadata_rerank_delta(question, building_meta),
            query._metadata_rerank_delta(question, census_meta),
        )

    def test_water_purification_metadata_rerank_prefers_purification_over_alkali(self):
        question = "how do i build a charcoal sand water filter"
        purification_meta = {
            "guide_title": "Water Purification",
            "section_heading": "Filtration Systems",
            "slug": "water-purification",
            "description": "Boiling, filtration, chemical treatment, and safe storage.",
            "category": "survival",
            "related": "",
            "tags": "water purification, filtration, safe drinking water",
            "source_file": "GD-035.md",
        }
        alkali_meta = {
            "guide_title": "Alkali & Soda Production",
            "section_heading": "Overview",
            "slug": "alkali-soda-production",
            "description": "Potash, lye, and soda ash production from wood ash.",
            "category": "chemistry",
            "related": "",
            "tags": "alkali, soda, ash, chemistry",
            "source_file": "GD-178.md",
        }

        self.assertLess(
            query._metadata_rerank_delta(question, purification_meta),
            query._metadata_rerank_delta(question, alkali_meta),
        )

    def test_generic_puncture_metadata_rerank_prefers_first_aid_over_animal_bite(self):
        question = "what do i do for a deep puncture wound"
        first_aid_meta = {
            "guide_title": "First Aid & Emergency Response",
            "section_heading": "Wound Management",
            "slug": "first-aid-emergency-response",
            "description": "First aid essentials, wound management, irrigation, infection prevention.",
            "category": "medical",
            "related": "",
            "tags": "first aid, wound management, puncture wound, irrigation, infection prevention",
            "source_file": "GD-232.md",
        }
        bite_meta = {
            "guide_title": "Animal Bite Wound Care & Rabies Post-Exposure Protocols",
            "section_heading": "Immediate Wound Care: First 24 Hours",
            "slug": "animal-bite-wound-care-rabies-post-exposure-protocols",
            "description": "Bite wound care, rabies, tetanus, and infection monitoring.",
            "category": "medical",
            "related": "",
            "tags": "animal bite, rabies, puncture wound, tetanus",
            "source_file": "GD-622.md",
        }

        self.assertLess(
            query._metadata_rerank_delta(question, first_aid_meta),
            query._metadata_rerank_delta(question, bite_meta),
        )

    def test_bleach_eye_metadata_rerank_prefers_eye_guide_over_lab_safety(self):
        question = "bleach splashed in my eye while cleaning and it still burns after rinsing"
        eye_meta = {
            "guide_title": "Eye Injuries, Infections & Emergency Ophthalmology",
            "section_heading": "Chemical Burns: Immediate Irrigation Protocol",
            "slug": "eye-injuries-emergency-care",
            "description": "Chemical burns, ocular exposure, and emergency irrigation.",
            "category": "medical",
            "related": "chemical-safety toxicology-poisoning-response",
            "tags": "eye injuries, ophthalmology, ocular exposure, chemical burns",
            "source_file": "GD-425.md",
        }
        lab_meta = {
            "guide_title": "Micro-Scale Chemistry Lab Safety & SOPs",
            "section_heading": "Emergency Procedures",
            "slug": "chemistry-lab-protocols-safety",
            "description": "Lab safety, SOPs, PPE, and chemistry handling procedures.",
            "category": "chemistry",
            "related": "",
            "tags": "lab safety, chemistry lab safety, sops, emergency procedures",
            "source_file": "GD-399.md",
        }

        self.assertLess(
            query._metadata_rerank_delta(question, eye_meta),
            query._metadata_rerank_delta(question, lab_meta),
        )

    def test_retinal_detachment_eye_emergency_routing_boundaries(self):
        prompts = [
            "sudden flashes and floaters then a dark curtain over one eye",
            "lost part of vision in one eye after seeing bright flashes",
            "migraine or eye emergency because half my vision went dark all at once",
            "new shower of floaters and shadow creeping across the side vision",
            "painless sudden vision loss in one eye that is getting worse",
            "sudden gray curtain falling over one eye and vision is getting worse",
            "sudden loss of vision in one eye",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertTrue(query._is_retinal_detachment_eye_emergency_query(prompt))
                self.assertIn("medical", query.build_scenario_frame(prompt)["domains"])
                specs = query._supplemental_retrieval_specs(prompt, 8)
                self.assertTrue(
                    any("retinal detachment" in spec["text"] for spec in specs),
                    [spec["text"] for spec in specs],
                )

        near_misses = [
            "gradual blurry vision and i need stronger glasses",
            "red crusty eye but vision stable",
            "usual migraine aura in both eyes that resolves, no curtain or one-eye loss",
            "bright signal flashes in the dark because i am on a lost trail",
            "metal chip flew into my eye and it still feels stuck",
            "vision loss with face droop and slurred speech",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertFalse(query._is_retinal_detachment_eye_emergency_query(prompt))

    def test_solvent_fume_metadata_rerank_prefers_toxicology_over_cookstove(self):
        question = "I inhaled paint thinner fumes in the shed and now feel sick"
        toxicology_meta = {
            "guide_title": "Toxicology and Poisoning Response",
            "section_heading": "Decontamination Methods",
            "slug": "toxicology-poisoning-response",
            "description": "Poison control, decontamination, inhaled poisons, and toxic exposures.",
            "category": "medical",
            "related": "chemical-safety unknown-ingestion-child-poisoning-triage",
            "tags": "toxicology, poison control, chemical exposure, decontamination, inhaled poisons",
            "source_file": "GD-301.md",
        }
        cookstove_meta = {
            "guide_title": "Cookstoves, Indoor Heating & Safety",
            "section_heading": "Draft Troubleshooting",
            "slug": "cookstoves-indoor-heating-safety",
            "description": "Indoor heating, chimneys, draft troubleshooting, and smoke backflow.",
            "category": "survival",
            "related": "smoke-inhalation-carbon-monoxide-fire-gas-exposure",
            "tags": "cookstoves, indoor heating, chimney, draft troubleshooting, carbon monoxide",
            "source_file": "GD-904.md",
        }

        self.assertLess(
            query._metadata_rerank_delta(question, toxicology_meta),
            query._metadata_rerank_delta(question, cookstove_meta),
        )

    def test_mixed_cleaner_metadata_rerank_prefers_chemical_exposure_over_cookstove(self):
        question = "I mixed cleaners and now my chest feels tight and I am coughing"
        chemical_meta = {
            "guide_title": "Chemical Safety",
            "section_heading": "13. Chemical Exposure Treatment",
            "slug": "chemical-safety",
            "description": "Chemical exposure treatment, decontamination, and incompatible cleaners.",
            "category": "chemistry",
            "related": "toxicology-poisoning-response",
            "tags": "chemical exposure, mixed cleaners, bleach ammonia, poison control",
            "source_file": "GD-227.md",
        }
        cookstove_meta = {
            "guide_title": "Cookstoves, Indoor Heating & Safety",
            "section_heading": "Draft Troubleshooting",
            "slug": "cookstoves-indoor-heating-safety",
            "description": "Indoor heating, chimneys, draft troubleshooting, and smoke backflow.",
            "category": "survival",
            "related": "smoke-inhalation-carbon-monoxide-fire-gas-exposure",
            "tags": "cookstoves, indoor heating, chimney, draft troubleshooting, carbon monoxide",
            "source_file": "GD-904.md",
        }

        self.assertLess(
            query._metadata_rerank_delta(question, chemical_meta),
            query._metadata_rerank_delta(question, cookstove_meta),
        )

        vinegar_question = "I mixed bleach and vinegar to clean the tub and now my chest is tight"
        toxicology_meta = {
            "guide_title": "Toxicology and Poisoning Response",
            "section_heading": "Decontamination Methods",
            "slug": "toxicology-poisoning-response",
            "description": "Poison control, decontamination, inhaled poisons, and toxic exposures.",
            "category": "medical",
            "related": "chemical-safety unknown-ingestion-child-poisoning-triage",
            "tags": "toxicology, poison control, chemical exposure, decontamination, inhaled poisons",
            "source_file": "GD-301.md",
        }
        hygiene_meta = {
            "guide_title": "Personal Hygiene & Grooming Without Running Water",
            "section_heading": "Bathing Without Plumbing",
            "slug": "personal-hygiene-grooming",
            "description": "Bathing, grooming, and hygiene without plumbing.",
            "category": "medical",
            "related": "",
            "tags": "hygiene, bathing, cleaning",
            "source_file": "GD-666.md",
        }
        dental_meta = {
            "guide_title": "Emergency Dental Procedures",
            "section_heading": "Overview",
            "slug": "emergency-dental-procedures",
            "description": "Dental emergencies, tooth pain, extraction, and oral infection.",
            "category": "medical",
            "related": "",
            "tags": "dental, tooth, extraction",
            "source_file": "GD-047.md",
        }
        self.assertTrue(query._is_household_chemical_inhalation_query(vinegar_question))
        self.assertLess(
            query._metadata_rerank_delta(vinegar_question, toxicology_meta),
            query._metadata_rerank_delta(vinegar_question, hygiene_meta),
        )
        self.assertLess(
            query._metadata_rerank_delta(vinegar_question, toxicology_meta),
            query._metadata_rerank_delta(vinegar_question, dental_meta),
        )
        vinegar_specs = query._supplemental_retrieval_specs(vinegar_question, 8)
        self.assertTrue(
            any("bleach ammonia vinegar acid" in spec["text"] for spec in vinegar_specs),
            [spec["text"] for spec in vinegar_specs],
        )
        self.assertTrue(
            any(
                spec.get("category") == "chemistry" and "incompatible cleaners" in spec["text"]
                for spec in vinegar_specs
            ),
            vinegar_specs,
        )

    def test_stove_co_prompt_does_not_trigger_household_chemical_rerank(self):
        question = "I get a headache every time we run the stove indoors for heat"
        self.assertFalse(query._is_household_chemical_eye_query(question))
        self.assertFalse(query._is_household_chemical_inhalation_query(question))

    def test_indoor_combustion_co_prompt_prefers_smoke_co_owners(self):
        question = "Blocked ventilation and now I'm worried about carbon monoxide. What should I do right now?"
        self.assertTrue(query._is_indoor_combustion_co_smoke_query(question))

        smoke_meta = {
            "guide_title": "Smoke Inhalation, Carbon Monoxide & Fire-Gas Exposure",
            "slug": "smoke-inhalation-carbon-monoxide-fire-gas-exposure",
            "description": "Field protocol for CO poisoning and fire-gas exposure.",
            "tags": "smoke inhalation, carbon monoxide, fire-gas exposure",
            "source_file": "GD-899.md",
        }
        hot_water_meta = {
            "guide_title": "Hot Water Systems for Bathing, Cleaning & Sanitation",
            "slug": "hot-water-systems-bathing-cleaning",
            "section_heading": "When a Heating Setup Becomes Dangerous",
            "description": "Water heating hazards.",
            "source_file": "GD-944.md",
        }

        self.assertLess(
            query._metadata_rerank_delta(question, smoke_meta),
            query._metadata_rerank_delta(question, hot_water_meta),
        )
        specs = query._supplemental_retrieval_specs(question, 8)
        self.assertTrue(
            any("smoke inhalation carbon monoxide fire-gas exposure" in spec["text"] for spec in specs),
            specs,
        )

    def test_blocked_upstairs_bedroom_exit_routes_to_closed_room_fire(self):
        question = "Should we try another exit first or go out the window if the upstairs bedroom door is blocked?"
        self.assertTrue(query._is_closed_room_fire_question(question))
        self.assertEqual(
            query.classify_special_case(question),
            ("deterministic", "closed_room_fire"),
        )

        specs = query._supplemental_retrieval_specs(question, 8)
        self.assertTrue(
            any("closed bedroom fire evacuation" in spec["text"] for spec in specs),
            specs,
        )
        self.assertFalse(
            query._is_closed_room_fire_question(
                "The bedroom window is blocked by furniture during inspection. What should we fix first?"
            )
        )

    def test_common_ailments_gateway_detects_broad_symptom_start_prompts(self):
        self.assertTrue(
            query._is_common_ailments_gateway_query(
                "This cough will not go away. Which guide should I start with?"
            )
        )
        self.assertTrue(
            query._is_common_ailments_gateway_query(
                "I got a rash after using a new soap. Is this just a mild reaction or something urgent?"
            )
        )
        self.assertTrue(
            query._is_common_ailments_gateway_query(
                "It burns when I pee. Is this home-care first or something more urgent?"
            )
        )
        self.assertFalse(
            query._is_common_ailments_gateway_query(
                "I have chest pain after exertion. Is this rest-and-watch or urgent help first?"
            )
        )
        self.assertFalse(
            query._is_common_ailments_gateway_query(
                "One side is weak and speech is slurred. Do I start in common ailments or first aid?"
            )
        )
        self.assertFalse(
            query._is_common_ailments_gateway_query(
                "I am coughing and having trouble breathing. Is this urgent help?"
            )
        )
        self.assertFalse(
            query._is_common_ailments_gateway_query(
                "I have a rash with throat swelling. Is this a mild reaction or urgent?"
            )
        )

    def test_maintenance_record_query_routes_to_basic_record_keeping(self):
        question = "What records should we keep for maintenance, repairs, and failures so the same mistake stops repeating?"
        self.assertTrue(query._is_maintenance_record_query(question))

        specs = query._supplemental_retrieval_specs(question, 8)
        self.assertTrue(
            any(
                "basic record-keeping maintenance logs repair history" in spec["text"]
                for spec in specs
            ),
            [spec["text"] for spec in specs],
        )

        records_meta = {
            "guide_title": "Basic Record-Keeping",
            "slug": "basic-record-keeping",
            "description": "maintenance logs repair history failure logs repeat failure prevention lessons learned from breakdowns",
            "category": "culture-knowledge",
        }
        hydro_meta = {
            "guide_title": "Hydroelectric Power",
            "section_heading": "Seasonal Operation & Common Mistakes",
            "description": "predictive maintenance and repairs for hydroelectric systems",
            "category": "power-generation",
        }
        self.assertLess(
            query._metadata_rerank_delta(question, records_meta),
            query._metadata_rerank_delta(question, hydro_meta),
        )

    def test_wave_e_safety_prompt_notes_cover_crisis_and_wet_roof(self):
        basic_results = {
            "documents": [["Focused safety note."]],
            "metadatas": [[{
                "guide_title": "Safety Guide",
                "guide_id": "GD-999",
                "section_heading": "Safety Section",
                "category": "medical",
                "difficulty": "beginner",
                "description": "",
            }]],
            "distances": [[0.2]],
        }

        anxiety_question = "when is anxiety becoming a crisis instead of just stress"
        self.assertTrue(query._is_anxiety_crisis_explainer_query(anxiety_question))
        anxiety_specs = query._supplemental_retrieval_specs(anxiety_question, 8)
        self.assertTrue(
            any("anxiety stress crisis red flags" in spec["text"] for spec in anxiety_specs),
            [spec["text"] for spec in anxiety_specs],
        )
        anxiety_prompt = query.build_prompt(anxiety_question, basic_results)
        self.assertIn("thoughts of suicide or self-harm", anxiety_prompt)
        self.assertIn("urgent help/988", anxiety_prompt)

        panic_boundary_question = (
            "How do I tell if this is a panic attack or something more dangerous?"
        )
        self.assertTrue(
            query._is_anxiety_crisis_explainer_query(panic_boundary_question)
        )
        panic_boundary_specs = query._supplemental_retrieval_specs(
            panic_boundary_question, 8
        )
        self.assertTrue(
            any(
                "anxiety stress crisis red flags" in spec["text"]
                for spec in panic_boundary_specs
            ),
            [spec["text"] for spec in panic_boundary_specs],
        )
        panic_boundary_prompt = query.build_prompt(
            panic_boundary_question, basic_results
        )
        self.assertIn("thoughts of suicide or self-harm", panic_boundary_prompt)
        self.assertIn("urgent help/988", panic_boundary_prompt)

        roof_question = "how do i patch a roof in the rain"
        self.assertTrue(query._is_active_rain_roof_repair_query(roof_question))
        roof_prompt = query.build_prompt(roof_question, basic_results)
        self.assertIn("Do not tell the user to climb onto a wet roof", roof_prompt)
        self.assertIn("durable patching waits for dry, stable conditions", roof_prompt)

    def test_wet_roof_electrical_prompts_route_to_hazard_first_retrieval(self):
        prompts = [
            "A storm damaged the roof, water is coming in, and some outlets got wet. What is safe to do first?",
            "Can we patch the roof now if there may be electrical damage inside and standing water near the panel?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertTrue(query._is_electrical_hazard_query(prompt))
                self.assertEqual(query._retrieval_profile_for_question(prompt), "safety_triage")
                specs = query._supplemental_retrieval_specs(prompt, 8)
                self.assertTrue(
                    any("electrical safety hazard prevention" in spec["text"] for spec in specs),
                    [spec["text"] for spec in specs],
                )

        electrical_meta = {
            "guide_title": "Electrical Safety & Hazard Prevention",
            "slug": "electrical-safety-hazard-prevention",
            "description": "electrical safety wet outlet standing water near panel de-energize do not touch",
            "category": "power-generation",
        }
        roof_meta = {
            "guide_title": "Roof Leak Emergency Repair",
            "slug": "roof-leak-emergency-repair",
            "description": "storm damage roof patch temporary repair active roof leak",
            "category": "building",
        }
        question = prompts[1]
        self.assertLess(
            query._metadata_rerank_delta(question, electrical_meta),
            query._metadata_rerank_delta(question, roof_meta),
        )

    def test_market_tax_and_building_habitability_prompts_get_guardrails(self):
        basic_results = {
            "documents": [["Focused planning note."]],
            "metadatas": [[{
                "guide_title": "Planning Guide",
                "guide_id": "GD-999",
                "section_heading": "Planning Section",
                "category": "resource-management",
                "difficulty": "beginner",
                "description": "",
            }]],
            "distances": [[0.2]],
        }

        tax_question = "How do we charge market fees or taxes without making the whole thing feel arbitrary?"
        self.assertTrue(query._is_market_tax_revenue_query(tax_question))
        tax_specs = query._supplemental_retrieval_specs(tax_question, 8)
        self.assertTrue(
            any("taxation public revenue market fees taxes" in spec["text"] for spec in tax_specs),
            [spec["text"] for spec in tax_specs],
        )
        tax_prompt = query.build_prompt(tax_question, basic_results)
        self.assertIn("combine marketplace fee visibility with taxation/revenue-system guidance", tax_prompt)
        self.assertIn("receipts/records", tax_prompt)

        building_question = "This house looks mostly okay, but the floor feels soft and the roof leaked. Is it usable after repair?"
        self.assertTrue(query._is_building_habitability_safety_query(building_question))
        building_specs = query._supplemental_retrieval_specs(building_question, 8)
        self.assertTrue(
            any("building inspection habitability checklist" in spec["text"] for spec in building_specs),
            [spec["text"] for spec in building_specs],
        )
        building_prompt = query.build_prompt(building_question, basic_results)
        self.assertIn("lead with no-entry triage", building_prompt)
        self.assertIn("soft/bouncy floor", building_prompt)

    def test_food_and_market_space_routing_boundaries(self):
        basic_results = {
            "documents": [["Focused food storage note."]],
            "metadatas": [[{
                "guide_title": "Food Storage Guide",
                "guide_id": "GD-999",
                "section_heading": "Food Section",
                "category": "agriculture",
                "difficulty": "beginner",
                "description": "",
            }]],
            "distances": [[0.2]],
        }
        food_question = "We have salted fish in a hot humid room and only jars. How should we store it?"
        self.assertTrue(query._is_food_storage_container_query(food_question))
        food_specs = query._supplemental_retrieval_specs(food_question, 8)
        self.assertTrue(
            any("food storage packaging salted fish" in spec["text"] for spec in food_specs),
            [spec["text"] for spec in food_specs],
        )
        self.assertTrue(
            any("food-preservation food storage packaging" in spec["text"] for spec in food_specs),
            [spec["text"] for spec in food_specs],
        )
        packaging_meta = {
            "guide_title": "Food Storage & Packaging",
            "slug": "food-storage-packaging",
            "description": "Food-grade sealed containers, salted fish, dried beans, dried herbs, cool dry storage.",
            "category": "agriculture",
        }
        salt_meta = {
            "guide_title": "Salt Production",
            "slug": "salt-production",
            "description": "Salt storage, salt purity, brine evaporation, and salt crystals.",
            "category": "chemistry",
        }
        self.assertLess(
            query._metadata_rerank_delta(food_question, packaging_meta),
            query._metadata_rerank_delta(food_question, salt_meta),
        )
        ambiguous_setup_question = (
            "We have salt, jars, and a hot humid room. Is this a preservation question "
            "or a storage-container question?"
        )
        self.assertTrue(query._is_food_storage_container_query(ambiguous_setup_question))
        self.assertTrue(query._is_salt_jars_hot_humid_setup_query(ambiguous_setup_question))
        ambiguous_specs = query._supplemental_retrieval_specs(ambiguous_setup_question, 8)
        self.assertTrue(
            any("Food Preservation salt jars hot humid" in spec["text"] for spec in ambiguous_specs),
            [spec["text"] for spec in ambiguous_specs],
        )
        self.assertTrue(
            any("Food Storage Packaging hot humid" in spec["text"] for spec in ambiguous_specs),
            [spec["text"] for spec in ambiguous_specs],
        )
        self.assertLess(
            query._metadata_rerank_delta(ambiguous_setup_question, packaging_meta),
            query._metadata_rerank_delta(ambiguous_setup_question, salt_meta),
        )
        preservation_meta = {
            "guide_title": "Food Preservation",
            "slug": "food-preservation",
            "description": "Salting, drying, fermentation, and curing before food storage.",
            "category": "agriculture",
        }
        self.assertLess(
            query._metadata_rerank_delta(ambiguous_setup_question, preservation_meta),
            query._metadata_rerank_delta(ambiguous_setup_question, salt_meta),
        )
        self.assertGreaterEqual(
            query._metadata_rerank_delta(ambiguous_setup_question, salt_meta),
            0.26,
        )
        spice_meta = {
            "guide_title": "Spices, Seasonings & Herb Cultivation",
            "slug": "spices-seasonings",
            "description": "Drying and storing herbs, spices, potency, and seasonings.",
            "category": "agriculture",
        }
        drying_meta = {
            "guide_title": "Drying & Dehydration Techniques",
            "slug": "drying-dehydration-techniques",
            "description": "Drying foods and storage after dehydration.",
            "category": "agriculture",
        }
        self.assertLess(
            query._metadata_rerank_delta(ambiguous_setup_question, packaging_meta),
            query._metadata_rerank_delta(ambiguous_setup_question, spice_meta),
        )
        self.assertLess(
            query._metadata_rerank_delta(ambiguous_setup_question, packaging_meta),
            query._metadata_rerank_delta(ambiguous_setup_question, drying_meta),
        )
        ambiguous_prompt = query.build_prompt(ambiguous_setup_question, basic_results)
        self.assertIn("preservation-first and container/packaging second", ambiguous_prompt)

        dry_meat_question = (
            "What is the simplest way to dry meat or fish so humidity, animals, "
            "and dirt do not ruin it?"
        )
        self.assertTrue(query._is_dry_meat_fish_contamination_query(dry_meat_question))
        dry_meat_specs = query._supplemental_retrieval_specs(dry_meat_question, 8)
        self.assertTrue(
            any("raised rack screen cheesecloth" in spec["text"] for spec in dry_meat_specs),
            [spec["text"] for spec in dry_meat_specs],
        )
        dry_meat_prompt = query.build_prompt(dry_meat_question, basic_results)
        self.assertIn("raised rack or enclosed", dry_meat_prompt)
        self.assertIn("screen/cheesecloth", dry_meat_prompt)
        self.assertIn("Do not describe salt as optional", dry_meat_prompt)
        drying_meta = {
            "guide_title": "Drying & Dehydration Techniques",
            "slug": "drying-dehydration-techniques",
            "description": "Solar drying with screens, racks, airflow, and protection from insects.",
            "category": "agriculture",
        }
        heritage_meta = {
            "guide_title": "Traditional Women's Trades & Fiber Knowledge",
            "slug": "traditional-womens-trades",
            "description": "Traditional food preservation heritage.",
            "category": "crafts",
        }
        self.assertLess(
            query._metadata_rerank_delta(dry_meat_question, drying_meta),
            query._metadata_rerank_delta(dry_meat_question, heritage_meta),
        )

        reranked = query.rerank_results(
            ambiguous_setup_question,
            {
                "ids": [["salt", "spice", "preserve", "package"]],
                "documents": [[
                    "Salt storage in jars for humid climates.",
                    "Herbs in jars for humid storage.",
                    "Food preservation before container choice in hot humid rooms.",
                    "Food Storage Packaging uses sealed jars and desiccants after preservation.",
                ]],
                "metadatas": [[
                    {"guide_id": "GD-076", "guide_title": "Salt Production", "section_heading": "Salt Storage", "category": "agriculture"},
                    {"guide_id": "GD-084", "guide_title": "Spices, Seasonings & Herb Cultivation", "section_heading": "Drying & Storing", "category": "agriculture"},
                    {"guide_id": "GD-065", "guide_title": "Food Preservation", "section_heading": "Salt Curing", "category": "agriculture"},
                    {"guide_id": "GD-966", "guide_title": "Food Storage Packaging", "section_heading": "Overview", "category": "agriculture"},
                ]],
                "distances": [[0.05, 0.06, 0.20, 0.21]],
            },
            top_k=2,
            scenario_frame=query.build_scenario_frame(ambiguous_setup_question),
        )
        self.assertEqual(
            ["GD-065", "GD-966"],
            [meta["guide_id"] for meta in reranked["metadatas"][0]],
        )

        market_question = "How do we connect storage to market flow without blocked corners and carts jamming the stalls?"
        self.assertTrue(query._is_market_space_layout_query(market_question))
        market_specs = query._supplemental_retrieval_specs(market_question, 8)
        self.assertTrue(
            any("marketplace trade space physical footprint" in spec["text"] for spec in market_specs),
            [spec["text"] for spec in market_specs],
        )
        layout_meta = {
            "guide_title": "Marketplace Trade Space Basics",
            "slug": "marketplace-trade-space-basics",
            "description": "Stalls, walking lanes, cart clearance, loading edge, blocked corners, and notices inside the market footprint.",
            "category": "resource-management",
        }
        tax_meta = {
            "guide_title": "Taxation & Public Revenue Systems",
            "slug": "taxation-revenue-systems",
            "description": "Tax assessment and collection, public revenue, rates, receipts, and appeals.",
            "category": "resource-management",
        }
        self.assertLess(
            query._metadata_rerank_delta(market_question, layout_meta),
            query._metadata_rerank_delta(market_question, tax_meta),
        )

    def test_android_gap_desktop_prompt_routing_boundaries(self):
        basic_results = {
            "documents": [["Focused routing note."]],
            "metadatas": [[{
                "guide_title": "Routing Guide",
                "guide_id": "GD-999",
                "section_heading": "Routing Section",
                "category": "communications",
                "difficulty": "beginner",
                "description": "",
            }]],
            "distances": [[0.2]],
        }

        adhesive_question = "How do I make simple adhesives or binders for wood, leather, paper, or containers?"
        self.assertTrue(query._is_adhesive_binder_query(adhesive_question))
        adhesive_specs = query._supplemental_retrieval_specs(adhesive_question, 8)
        self.assertTrue(
            any("adhesives binders formulation adhesive selection" in spec["text"] for spec in adhesive_specs),
            [spec["text"] for spec in adhesive_specs],
        )
        adhesive_meta = {
            "guide_title": "Adhesives & Binders Formulation",
            "slug": "adhesives-binders-formulation",
            "description": "Adhesive selection, binder families, hide glue, casein, starch paste, pine pitch.",
            "category": "crafts",
        }
        soap_meta = {
            "guide_title": "Soap Making",
            "slug": "soap-making",
            "description": "Soap, bleach, fuel, dye, and process chemistry.",
            "category": "chemistry",
        }
        self.assertLess(
            query._metadata_rerank_delta(adhesive_question, adhesive_meta),
            query._metadata_rerank_delta(adhesive_question, soap_meta),
        )
        adhesive_prompt = query.build_prompt(adhesive_question, basic_results)
        self.assertIn("wood, leather, paper, and containers", adhesive_prompt)

        auth_question = "If someone posts an evacuation order on the board, how should people verify it before acting?"
        self.assertTrue(query._is_message_auth_query(auth_question))
        auth_specs = query._supplemental_retrieval_specs(auth_question, 8)
        self.assertTrue(
            any("message authentication courier protocols prove note real" in spec["text"] for spec in auth_specs),
            [spec["text"] for spec in auth_specs],
        )
        auth_meta = {
            "guide_title": "Message Authentication & Courier Protocols",
            "slug": "message-authentication-courier",
            "description": "Message authentication, challenge-response, tamper evidence, posted orders.",
            "category": "communications",
        }
        wildfire_meta = {
            "guide_title": "Wildfire Defense & Evacuation",
            "slug": "wildfire-defense-evacuation",
            "description": "Wildfire evacuation planning and emergency orders.",
            "category": "defense",
        }
        self.assertLess(
            query._metadata_rerank_delta(auth_question, auth_meta),
            query._metadata_rerank_delta(auth_question, wildfire_meta),
        )
        auth_prompt = query.build_prompt(auth_question, basic_results)
        self.assertIn("sender identity, tamper evidence", auth_prompt)

        simple_auth_prompt = query.build_prompt(
            "We run couriers between camps. What is the simplest way to prove a note is real without making the system too fragile?",
            basic_results,
        )
        self.assertIn("Do not require witnesses", simple_auth_prompt)

        posted_order_prompt = query.build_prompt(auth_question, basic_results)
        self.assertIn("issuing authority or pre-agreed authentication signal", posted_order_prompt)
        self.assertIn("do not tell people simply to abort/ignore protective action", posted_order_prompt)

    def test_artifact_review_followup_prompt_notes_cover_regressions(self):
        basic_results = {
            "documents": [["Focused safety note."]],
            "metadatas": [[{
                "guide_title": "Safety Guide",
                "guide_id": "GD-999",
                "section_heading": "Safety Section",
                "category": "medical",
                "difficulty": "beginner",
                "description": "",
            }]],
            "distances": [[0.2]],
        }

        chemical_question = "The garage has a sharp chemical smell and one container leaked, but I cannot identify the product."
        self.assertTrue(query._is_unknown_leaking_chemical_container_query(chemical_question))
        chemical_prompt = query.build_prompt(chemical_question, basic_results)
        self.assertIn("do not touch, open, identify, or move", chemical_prompt)

        skin_burn_question = "I found an unlabeled bottle under the sink and my hands started burning after I touched it."
        self.assertTrue(query._is_unknown_chemical_skin_burn_query(skin_burn_question))
        skin_burn_prompt = query.build_prompt(skin_burn_question, basic_results)
        self.assertIn("possible chemical burn/toxic exposure", skin_burn_prompt)
        self.assertIn("do not answer as poison ivy", skin_burn_prompt)
        skin_burn_specs = query._supplemental_retrieval_specs(skin_burn_question, 8)
        self.assertTrue(
            any("unknown unlabeled under sink bottle hands" in spec["text"] for spec in skin_burn_specs),
            [spec["text"] for spec in skin_burn_specs],
        )

        powder_question = (
            "There is a white powder on the counter and I cannot tell if it is "
            "fertilizer, detergent, or something toxic."
        )
        self.assertTrue(query._is_unknown_loose_chemical_powder_query(powder_question))
        powder_prompt = query.build_prompt(powder_question, basic_results)
        self.assertIn("Do not move, sweep, brush, smell, taste", powder_prompt)
        powder_specs = query._supplemental_retrieval_specs(powder_question, 8)
        self.assertTrue(
            any("unknown loose chemical powder" in spec["text"] for spec in powder_specs),
            [spec["text"] for spec in powder_specs],
        )

        canned_question = "Soft spots in canned fruit: still safe or not?"
        self.assertTrue(query._is_canned_fruit_soft_spot_query(canned_question))
        canned_prompt = query.build_prompt(canned_question, basic_results)
        self.assertIn("Do not say to discard only affected portions", canned_prompt)
        canned_specs = query._supplemental_retrieval_specs(canned_question, 8)
        self.assertTrue(
            any("canned fruit soft spots" in spec["text"] for spec in canned_specs),
            [spec["text"] for spec in canned_specs],
        )

        rice_question = "The rice tastes off after the power went out, how do we tell spoilage from just stale food?"
        self.assertTrue(query._is_cooked_rice_power_outage_spoilage_query(rice_question))
        rice_prompt = query.build_prompt(rice_question, basic_results)
        self.assertIn("stop eating and discard it", rice_prompt)
        rice_specs = query._supplemental_retrieval_specs(rice_question, 8)
        self.assertTrue(
            any("cooked rice power outage tastes off" in spec["text"] for spec in rice_specs),
            [spec["text"] for spec in rice_specs],
        )

        industrial_odor_question = (
            "A chemical smells wrong and may be part of an industrial process. "
            "Do we stay in chemistry-fundamentals or hand off immediately?"
        )
        self.assertTrue(query._is_industrial_chemical_smell_boundary_query(industrial_odor_question))
        industrial_odor_prompt = query.build_prompt(industrial_odor_question, basic_results)
        self.assertIn("do not tell the user to trust their nose", industrial_odor_prompt)
        self.assertIn("use the phrase 'trust your nose'", industrial_odor_prompt)
        self.assertIn("hand off to chemical safety or industrial accident response", industrial_odor_prompt)
        industrial_odor_specs = query._supplemental_retrieval_specs(industrial_odor_question, 8)
        self.assertTrue(
            any("chemical industrial accident response strong chemical odor" in spec["text"] for spec in industrial_odor_specs),
            [spec["text"] for spec in industrial_odor_specs],
        )

        powder_liquid_question = (
            "This powder or liquid is not labeled. Should this route to "
            "chemistry-fundamentals or chemical-safety first?"
        )
        self.assertTrue(query._is_unknown_loose_chemical_powder_query(powder_liquid_question))
        powder_liquid_specs = query._supplemental_retrieval_specs(powder_liquid_question, 8)
        self.assertTrue(
            any("unknown loose chemical powder" in spec["text"] for spec in powder_liquid_specs),
            [spec["text"] for spec in powder_liquid_specs],
        )

        spill_sick_question = (
            "We had a chemical spill in a workshop and now someone feels sick. "
            "Which guide owns the first response?"
        )
        self.assertTrue(query._is_chemical_spill_sick_exposure_query(spill_sick_question))
        spill_sick_prompt = query.build_prompt(spill_sick_question, basic_results)
        self.assertIn("chemical spill plus sick-person prompts", spill_sick_prompt)
        self.assertIn("exposure triage first", spill_sick_prompt)
        spill_sick_specs = query._supplemental_retrieval_specs(spill_sick_question, 8)
        self.assertTrue(
            any("chemical spill workshop someone feels sick" in spec["text"] for spec in spill_sick_specs),
            [spec["text"] for spec in spill_sick_specs],
        )

        feedstock_exposure_question = (
            "Is this a precursor/feedstock question, or is it really a poisoning "
            "or exposure question?"
        )
        self.assertTrue(query._is_precursor_feedstock_exposure_boundary_query(feedstock_exposure_question))
        feedstock_exposure_prompt = query.build_prompt(feedstock_exposure_question, basic_results)
        self.assertIn("explain the split instead of using a blanket rule", feedstock_exposure_prompt)
        self.assertIn("do not say every substance question is poisoning/exposure", feedstock_exposure_prompt)
        self.assertIn("known materials with no spill", feedstock_exposure_prompt)
        feedstock_exposure_specs = query._supplemental_retrieval_specs(feedstock_exposure_question, 8)
        self.assertTrue(
            any("precursor feedstock raw material boundary" in spec["text"] for spec in feedstock_exposure_specs),
            [spec["text"] for spec in feedstock_exposure_specs],
        )
        self.assertTrue(
            any("poisoning exposure boundary unknown chemical" in spec["text"] for spec in feedstock_exposure_specs),
            [spec["text"] for spec in feedstock_exposure_specs],
        )
        self.assertTrue(
            all("category" in spec for spec in feedstock_exposure_specs),
            feedstock_exposure_specs,
        )

        sealed_drum_question = (
            "An unlabeled drum in the workshop is sealed and nobody feels sick. "
            "Is this disposal, feedstock, or safety triage first?"
        )
        self.assertTrue(query._is_unlabeled_sealed_drum_safety_triage_query(sealed_drum_question))
        sealed_drum_prompt = query.build_prompt(sealed_drum_question, basic_results)
        self.assertIn("unknown-chemical safety/disposal triage first", sealed_drum_prompt)
        self.assertIn("Do not infer bitumen", sealed_drum_prompt)
        sealed_drum_specs = query._supplemental_retrieval_specs(sealed_drum_question, 8)
        self.assertTrue(
            any("unlabeled sealed drum unknown chemical safety triage" in spec["text"] for spec in sealed_drum_specs),
            [spec["text"] for spec in sealed_drum_specs],
        )

        stretcher_question = "The clinic entrance is too narrow for a stretcher, where should the access door go?"
        self.assertTrue(query._is_stretcher_access_query(stretcher_question))
        stretcher_prompt = query.build_prompt(stretcher_question, basic_results)
        self.assertIn("not wheelchair-only door-width prompts", stretcher_prompt)

        compartment_prompt = query.build_prompt(
            "maybe just a bad bruise but the pain is out of proportion and the leg is getting tighter",
            basic_results,
        )
        self.assertIn("Do not make urgent evaluation conditional", compartment_prompt)
        self.assertIn("if numbness persists", compartment_prompt)
        self.assertIn("back-pain saddle/bladder red flags", compartment_prompt)

    def test_common_ailments_gateway_gets_targeted_medical_specs(self):
        specs = query._supplemental_retrieval_specs(
            "This cough will not go away. Which guide should I start with?", 8
        )

        self.assertTrue(
            any(
                "common ailments recognition basic home care" in spec["text"]
                for spec in specs
            )
        )
        self.assertTrue(
            any(
                "when to seek professional medical care common ailments" in spec["text"]
                for spec in specs
            )
        )

        acute_specs = query._supplemental_retrieval_specs(
            "I am coughing and having trouble breathing. Is this urgent help?", 8
        )
        self.assertFalse(
            any(
                "common ailments recognition basic home care" in spec["text"]
                for spec in acute_specs
            )
        )

    def test_common_ailments_gateway_metadata_rerank_prefers_gateway_over_focused_guides(self):
        question = "I got a rash after using a new soap. Is this just a mild reaction or something urgent?"
        common_meta = {
            "guide_title": "Common Ailments: Recognition & Basic Home Care",
            "section_heading": "Red Flag Summary: When to Evacuate or Seek Emergency Care",
            "slug": "common-ailments-recognition-care",
            "description": "Recognition and basic home care for mild versus urgent symptoms.",
            "category": "medical",
            "related": "common-rashes-skin-irritation cough-cold-sore-throat-home-care",
            "tags": "common ailments, red flags, home care",
            "source_file": "GD-733.md",
        }
        rash_meta = {
            "guide_title": "Common Rashes, Skin Irritation & Fungal Skin Problems",
            "section_heading": "Contact Dermatitis and Irritation",
            "slug": "common-rashes-skin-irritation",
            "description": "Common skin irritation and rash care.",
            "category": "medical",
            "related": "common-ailments-recognition-care",
            "tags": "rash, skin irritation, contact dermatitis",
            "source_file": "GD-938.md",
        }

        self.assertLess(
            query._metadata_rerank_delta(question, common_meta),
            query._metadata_rerank_delta(question, rash_meta),
        )

    def test_urinary_overlap_and_hematuria_routing_boundaries(self):
        vaginal_question = "Burning when I pee plus vaginal itching and discharge. Is this still a urinary problem?"
        self.assertTrue(query._is_urinary_query(vaginal_question))
        self.assertTrue(query._is_urinary_vaginal_overlap_query(vaginal_question))
        vaginal_specs = query._supplemental_retrieval_specs(vaginal_question, 8)
        self.assertTrue(
            any("vaginal itching discharge" in spec["text"] for spec in vaginal_specs),
            [spec["text"] for spec in vaginal_specs],
        )

        vaginal_meta = {
            "guide_title": "Common Vaginal Infections, Itching & Discharge",
            "section_heading": "Overview",
            "slug": "common-vaginal-infections",
            "description": "vaginal itching discharge yeast infection vaginitis bacterial vaginosis",
            "category": "medical",
            "related": "common-ailments-recognition-care",
            "tags": "vaginal itching, discharge changed, yeast infection",
        }
        urinary_meta = {
            "guide_title": "Common Ailments: Recognition & Basic Home Care",
            "section_heading": "Urinary Tract Infections",
            "slug": "common-ailments-recognition-care",
            "description": "urinary tract infection burning urination urgency frequency",
            "category": "medical",
            "related": "common-vaginal-infections",
            "tags": "urinary, uti, dysuria",
        }
        self.assertLess(
            query._metadata_rerank_delta(vaginal_question, vaginal_meta),
            0,
        )
        self.assertLess(
            query._metadata_rerank_delta(vaginal_question, urinary_meta),
            0,
        )

        hematuria_question = "Blood in the urine without fever. When is this urgent?"
        self.assertTrue(query._is_hematuria_query(hematuria_question))
        hematuria_specs = query._supplemental_retrieval_specs(hematuria_question, 8)
        self.assertTrue(
            any("visible blood in urine hematuria" in spec["text"] for spec in hematuria_specs),
            [spec["text"] for spec in hematuria_specs],
        )
        nosebleed_meta = {
            "guide_title": "Nosebleeds: Basic Care & When To Worry",
            "section_heading": "When To Worry",
            "slug": "nosebleeds-basic-care",
            "description": "nosebleed firm pressure bleeding red flags",
            "category": "medical",
            "related": "",
            "tags": "nosebleed, bleeding",
        }
        self.assertGreater(
            query._metadata_rerank_delta(hematuria_question, nosebleed_meta),
            query._metadata_rerank_delta(hematuria_question, urinary_meta),
        )

        basic_results = {
            "documents": [["Focused medical note."]],
            "metadatas": [[{
                "guide_title": "Common Ailments",
                "guide_id": "GD-733",
                "section_heading": "Urinary",
                "category": "medical",
                "difficulty": "beginner",
                "description": "",
            }]],
            "distances": [[0.2]],
        }
        vaginal_prompt = query.build_prompt(vaginal_question, basic_results)
        self.assertIn("do not name antibiotic choices or doses", vaginal_prompt)
        hematuria_prompt = query.build_prompt(hematuria_question, basic_results)
        self.assertIn("Blood in urine is itself a urinary red-flag question", hematuria_prompt)
        self.assertIn("do not import microscopy workflow", hematuria_prompt)

    def test_safety_wave_route_detectors_cover_high_risk_cues(self):
        self.assertTrue(
            query._is_gi_bleed_emergency_query(
                "The stool is black and sticky like tar, and they feel weak and dizzy. What do I do first?"
            )
        )
        self.assertTrue(
            query._is_gi_bleed_emergency_query(
                "They threw up blood after heavy drinking. What do I do first?"
            )
        )
        self.assertTrue(
            query._is_gi_bleed_emergency_query(
                "I cannot tell if this is hemorrhoids, reflux, or dangerous bleeding. What matters first?"
            )
        )
        self.assertTrue(query._is_gi_bleed_emergency_query("bright red blood in vomit"))
        self.assertTrue(
            query._is_gi_bleed_emergency_query("is this a bleed or a minor stomach issue")
        )
        self.assertTrue(
            query._is_surgical_abdomen_emergency_query("stomach pain plus passing out")
        )
        self.assertTrue(
            query._is_gyn_emergency_query(
                "There is bleeding in early pregnancy with dizziness. What matters first?"
            )
        )
        self.assertTrue(
            query._is_gyn_emergency_query(
                "There is heavy bleeding with severe pelvic pain. What do I do first?"
            )
        )
        for prompt in [
            "They missed a period, have one-sided pelvic pain, and almost fainted. What matters first?",
            "I cannot tell if this is period cramps or an emergency. What do I do first?",
            "This sounds gynecologic, but I need the emergency first-action path. What matters first?",
            "early pregnancy spotting and one-sided lower belly pain and shoulder pain",
            "pregnant maybe 6 weeks and now cramping on one side and feeling faint",
            "miscarriage or emergency because the bleeding is light but the pain is sharp on one side",
            "late period positive test and sudden pelvic pain with dizziness",
            "first trimester bleeding and one-sided pain that is getting worse",
            "pregnant with belly pain and almost passed out in the bathroom",
            "pregnant and heavy bleeding with cramps",
            "one-sided lower belly pain and a positive pregnancy test",
            "shoulder pain and dizziness in early pregnancy",
            "bleeding after a positive pregnancy test",
        ]:
            self.assertTrue(query._is_gyn_emergency_query(prompt), prompt)
        self.assertFalse(
            query._is_gyn_emergency_query("Normal period cramps and I want warmth options.")
        )
        for boundary in [
            "early pregnancy spotting only, no pain or dizziness",
            "pregnant with mild nausea only",
            "postpartum soaking pads and faint after delivery",
            "generic shoulder injury after a fall",
        ]:
            self.assertFalse(query._is_gyn_emergency_query(boundary), boundary)
        for prompt in [
            "They have right lower belly pain and will not walk upright. What matters first?",
            "There is sudden belly pain with guarding. What do I do first?",
            "They are vomiting and the belly is getting hard. What matters first?",
            "Their belly is swollen and hard, they cannot keep anything down, and they have not pooped or farted since yesterday.",
            "There is belly pain with fever and one spot is very tender. What matters first?",
            "I cannot tell if this is stomach flu or a surgical abdomen. What do I do first?",
        ]:
            self.assertTrue(query._is_surgical_abdomen_emergency_query(prompt), prompt)
        for boundary in [
            "I have ordinary stomach flu with diarrhea but no hard belly or guarding.",
            "Mild constipation for two days but no vomiting or swollen belly.",
            "Bleeding in early pregnancy with dizziness and lower belly pain.",
            "Coffee-ground vomit with stomach pain and weakness.",
        ]:
            self.assertFalse(query._is_surgical_abdomen_emergency_query(boundary), boundary)
        self.assertTrue(
            query._is_crush_compartment_query(
                "ankle looks swollen tight and it hurts badly when someone moves my toes"
            )
        )
        self.assertTrue(
            query._is_crush_compartment_query(
                "after being pinned under weight my foot is numb and the calf feels hard"
            )
        )
        self.assertTrue(
            query._is_serotonin_syndrome_query(
                "medicine reaction or panic because i have diarrhea fever and cannot stop moving"
            )
        )
        self.assertTrue(
            query._is_serotonin_syndrome_query(
                "agitated sweaty and trembling after mixing antidepressants and cough medicine"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "fever stiff neck and a purple rash that does not fade when pressed"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "flu or emergency because he has a bad headache fever and spots on the legs"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "sore all over with fever and a spreading purplish rash and now acting confused"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "child has high fever and is hard to wake and the rash looks bruise-like"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "stiff neck throwing up and sleepy with little purple dots"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "child is hard to wake and has a rash"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "petechial rash and very sick-looking"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "Headache with stiff neck and fever. What matters first?"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "fever, light hurts the eyes, the neck is rigid, and they are vomiting"
            )
        )
        self.assertTrue(
            query._is_meningitis_rash_emergency_query(
                "vomiting with fever and neck stiffness"
            )
        )
        self.assertTrue(
            query._is_eye_globe_injury_query(
                "stick scratched my eye and there is something poking out so should i pull it out"
            )
        )
        self.assertTrue(
            query._is_eye_globe_injury_query(
                "eye injury from flying debris, vision is darker on that side, and there may still be something in the eye"
            )
        )
        self.assertTrue(
            query._is_electrical_hazard_query(
                "Someone was shocked and cannot let go of the wire. What do we do first?"
            )
        )
        self.assertTrue(
            query._is_downed_power_line_query(
                "There is a downed power line across the driveway. What do we do first?"
            )
        )
        self.assertTrue(
            query._is_drowning_cold_water_query(
                "Cold-water rescue and they are gasping hard. What first?"
            )
        )
        self.assertTrue(
            query._is_drowning_cold_water_query(
                "I see someone face down and silent in the water. What matters first?"
            )
        )
        self.assertTrue(
            query._is_post_rescue_drowning_breathing_query(
                "They seemed okay after being pulled from the water but now they are coughing and short of breath."
            )
        )
        self.assertTrue(
            query._is_urgent_nosebleed_query(
                "Nosebleed after starting blood thinners and now dizzy and pale."
            )
        )
        self.assertTrue(
            query._is_urgent_nosebleed_query(
                "Should I lean forward or get urgent help?"
            )
        )
        self.assertTrue(
            query._is_cardiac_first_query(
                "Is this a panic attack or heart attack if I have chest pressure and shortness of breath?"
            )
        )
        self.assertTrue(
            query._is_generic_choking_help_special_case(
                "drooling and cannot swallow after a bite of food"
            )
        )
        self.assertFalse(
            query._is_mental_health_crisis_query(
                "Is this a panic attack or heart attack if I have chest pressure and shortness of breath?"
            )
        )

    def test_safety_wave_queries_get_targeted_medical_specs(self):
        cases = [
            (
                "There is stomach pain and vomiting blood. What do I do first?",
                "possible gastrointestinal bleeding bleed-first",
            ),
            (
                "There is bleeding in early pregnancy with dizziness. What matters first?",
                "gynecological emergency early pregnancy bleeding dizziness",
            ),
            (
                "early pregnancy spotting and one-sided lower belly pain and shoulder pain",
                "gynecological emergency early pregnancy bleeding dizziness",
            ),
            (
                "pregnant maybe 6 weeks and now cramping on one side and feeling faint",
                "gynecological emergency early pregnancy bleeding dizziness",
            ),
            (
                "miscarriage or emergency because the bleeding is light but the pain is sharp on one side",
                "gynecological emergency early pregnancy bleeding dizziness",
            ),
            (
                "late period positive test and sudden pelvic pain with dizziness",
                "gynecological emergency early pregnancy bleeding dizziness",
            ),
            (
                "first trimester bleeding and one-sided pain that is getting worse",
                "gynecological emergency early pregnancy bleeding dizziness",
            ),
            (
                "pregnant with belly pain and almost passed out in the bathroom",
                "gynecological emergency early pregnancy bleeding dizziness",
            ),
            (
                "forearm got crushed and now my fingers tingle and the pain keeps building",
                "crush injury compartment syndrome pain out of proportion",
            ),
            (
                "flu or dangerous medication reaction because i have clonus fever and confusion",
                "serotonin syndrome antidepressant cough medicine",
            ),
            (
                "sore all over with fever and a spreading purplish rash and now acting confused",
                "meningitis meningococcemia sepsis emergency",
            ),
            (
                "fever stiff neck and a purple rash that does not fade when pressed",
                "meningitis meningococcemia sepsis emergency",
            ),
            (
                "fever, light hurts the eyes, the neck is rigid, and they are vomiting",
                "meningitis meningococcemia sepsis emergency",
            ),
            (
                "vomiting with fever and neck stiffness",
                "meningitis meningococcemia sepsis emergency",
            ),
            (
                "metal chip flew into my eye while grinding and it still feels stuck",
                "eye injury embedded object penetrating globe high-speed",
            ),
            (
                "Someone was shocked and cannot let go of the wire. What do we do first?",
                "electrical safety hazard prevention shock cannot let go",
            ),
            (
                "There is a downed power line across the driveway. What do we do first?",
                "downed utility power line first action stay far",
            ),
            (
                "Cold-water rescue and they are gasping hard. What first?",
                "drowning rescue priorities reach throw row go",
            ),
            (
                "I see someone face down and silent in the water. What matters first?",
                "active drowning now call alert help immediately",
            ),
            (
                "They seemed okay after being pulled from the water but now they are coughing and short of breath.",
                "after water rescue coughing short of breath requires urgent medical evaluation now",
            ),
            (
                "Nosebleed after starting blood thinners and now dizzy and pale.",
                "nosebleed urgent red flags",
            ),
            (
                "Is this a panic attack or heart attack if I have chest pressure and shortness of breath?",
                "acute coronary cardiac emergencies chest pressure",
            ),
            (
                "During storm evacuation, an adult has chest pressure, arm tingling, and wants to drive themself while we also handle animals and water.",
                "acute coronary cardiac emergencies chest pressure",
            ),
            (
                "sleepy after possible medicine ingestion",
                "unknown-ingestion-child-poisoning-triage toxicology poisoning response",
            ),
            (
                "mouth burns after tasting a liquid",
                "unknown-ingestion-child-poisoning-triage toxicology poisoning response",
            ),
        ]

        for question, expected in cases:
            with self.subTest(question=question):
                specs = query._supplemental_retrieval_specs(question, 8)
                self.assertTrue(
                    any(expected in spec["text"] for spec in specs),
                    [spec["text"] for spec in specs],
                )

    def test_poisoning_ingestion_boundary_uses_safety_profile(self):
        for question in (
            "toddler swallowed an unknown pill",
            "sleepy after possible medicine ingestion",
            "mouth burns after tasting a liquid",
        ):
            with self.subTest(question=question):
                self.assertTrue(query._is_poisoning_unknown_ingestion_card_query(question))
                self.assertEqual(query._retrieval_profile_for_question(question), "safety_triage")

    def test_safety_wave_metadata_rerank_prefers_emergency_owners_over_distractors(self):
        gi_question = "The stool is black and sticky like tar, and they feel weak and dizzy."
        gi_meta = {
            "guide_title": "Acute Abdominal Emergencies & Surgical Decision-Making",
            "section_heading": "GI Bleeding Emergencies",
            "slug": "acute-abdominal-emergencies",
            "description": "Gastrointestinal bleeding, hematemesis, melena, and shock concerns.",
            "category": "medical",
        }
        dehydration_meta = {
            "guide_title": "Common Ailments: Recognition & Basic Home Care",
            "section_heading": "Gastrointestinal Illness: Diarrhea, Vomiting, Nausea",
            "slug": "common-ailments-recognition-care",
            "description": "Dehydration and routine GI illness care.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(gi_question, gi_meta),
            query._metadata_rerank_delta(gi_question, dehydration_meta),
        )

        surgical_question = "They are vomiting and the belly is getting hard. What matters first?"
        surgical_meta = {
            "guide_title": "Acute Abdominal Emergencies & Surgical Decision-Making",
            "section_heading": "Peritoneal Signs and Bowel Obstruction",
            "slug": "acute-abdominal-emergencies",
            "description": "Surgical abdomen, guarding, rigid belly, bowel obstruction, appendicitis.",
            "category": "medical",
        }
        routine_gi_meta = {
            "guide_title": "Home Sick Care & Hygiene",
            "section_heading": "Vomiting and Dehydration",
            "slug": "home-sick-care-hygiene",
            "description": "Routine vomiting, hydration, dehydration checks, and home sick care.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(surgical_question, surgical_meta),
            query._metadata_rerank_delta(surgical_question, routine_gi_meta),
        )
        surgical_specs = query._supplemental_retrieval_specs(surgical_question, 8)
        self.assertTrue(
            any("acute abdominal emergencies surgical abdomen" in spec["text"] for spec in surgical_specs),
            surgical_specs,
        )

        gyn_question = "There is bleeding in early pregnancy with dizziness. What matters first?"
        gyn_meta = {
            "guide_title": "Gynecological Emergencies & Women's Health",
            "section_heading": "Ectopic Pregnancy: Recognition and Management",
            "slug": "gynecological-emergencies-womens-health",
            "description": "Early pregnancy bleeding dizziness and ectopic pregnancy emergencies.",
            "category": "medical",
        }
        postpartum_meta = {
            "guide_title": "Postpartum Care - Mother & Infant",
            "section_heading": "Immediate Postpartum",
            "slug": "postpartum-care-mother-infant",
            "description": "Postpartum hemorrhage, uterine massage, and maternal monitoring.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(gyn_question, gyn_meta),
            query._metadata_rerank_delta(gyn_question, postpartum_meta),
        )
        ef_question = "first trimester bleeding and one-sided pain that is getting worse"
        acute_abdomen_meta = {
            "guide_title": "Acute Abdominal Emergencies & Surgical Decision-Making",
            "section_heading": "Reproductive Abdomen and Ectopic Pregnancy",
            "slug": "acute-abdominal-emergencies",
            "description": "Ectopic pregnancy and reproductive abdomen emergencies.",
            "category": "medical",
        }
        menstrual_meta = {
            "guide_title": "Menstrual Pain Management",
            "section_heading": "Routine Cramp Comfort",
            "slug": "menstrual-pain-management",
            "description": "Period cramps, warmth, hydration, and routine comfort care.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(ef_question, gyn_meta),
            query._metadata_rerank_delta(ef_question, menstrual_meta),
        )
        self.assertLess(
            query._metadata_rerank_delta(ef_question, acute_abdomen_meta),
            query._metadata_rerank_delta(ef_question, postpartum_meta),
        )

        crush_question = "after being pinned under weight my foot is numb and the calf feels hard"
        crush_meta = {
            "guide_title": "Shock, Bleeding & Trauma Stabilization",
            "section_heading": "Crush Injuries and Crush Syndrome",
            "slug": "shock-bleeding-trauma-stabilization",
            "description": "Crush syndrome, compartment syndrome, immobilization, and urgent evacuation.",
            "category": "survival",
        }
        foot_meta = {
            "guide_title": "Foot and Nail Care",
            "section_heading": "My Feet Hurt From Walking All Day",
            "slug": "foot-and-nail-care",
            "description": "Foot hygiene, dry socks, walking pain, and routine rest.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(crush_question, crush_meta),
            query._metadata_rerank_delta(crush_question, foot_meta),
        )

        serotonin_question = "after changing meds i am restless overheated and twitching all over"
        serotonin_meta = {
            "guide_title": "Toxicology and Poisoning Response",
            "section_heading": "Serotonin Syndrome",
            "slug": "toxicology-poisoning-response",
            "description": "Serotonergic toxidrome, poison control, cooling, and supportive care.",
            "category": "medical",
        }
        anxiety_meta = {
            "guide_title": "Anxiety, Stress & Daily Self-Care",
            "section_heading": "When Anxiety Spikes: A 2-Minute Reset",
            "slug": "anxiety-stress-daily-self-care",
            "description": "Panic, routine self-care, and calming exercises.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(serotonin_question, serotonin_meta),
            query._metadata_rerank_delta(serotonin_question, anxiety_meta),
        )

        meningitis_question = "sore all over with fever and a spreading purplish rash and now acting confused"
        sepsis_meta = {
            "guide_title": "Sepsis Recognition and Antibiotic Protocols",
            "section_heading": "CNS/Meningitis",
            "slug": "sepsis-recognition-antibiotic-protocols",
            "description": "Meningitis, meningococcemia, non-blanching purple rash, confusion, and emergency care.",
            "category": "medical",
        }
        bug_bite_meta = {
            "guide_title": "Bug Bites, Stings & Itch Relief",
            "section_heading": "How Do I Tell If a Bug Bite Is Serious?",
            "slug": "bug-bites-stings-itch-relief",
            "description": "Rash infection progression, warmth, pus, and red streaks.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(meningitis_question, sepsis_meta),
            query._metadata_rerank_delta(meningitis_question, bug_bite_meta),
        )
        public_health_surveillance_meta = {
            "guide_title": "Public Health & Disease Surveillance",
            "section_heading": "Disease Surveillance Systems: Structure & Operation",
            "slug": "public-health-disease-surveillance",
            "description": "Suspected meningitis reporting, Health Officer notification, contact tracing, quarantine, and isolation.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(meningitis_question, sepsis_meta),
            query._metadata_rerank_delta(
                meningitis_question, public_health_surveillance_meta
            ),
        )
        meningitis_rigid_neck_question = (
            "fever, light hurts the eyes, the neck is rigid, and they are vomiting"
        )
        common_ailments_meta = {
            "guide_title": "Common Ailments Recognition and Care",
            "section_heading": "Gastrointestinal Illness",
            "slug": "common-ailments-recognition-care",
            "description": "Routine fever, headache, vomiting, and home care red flags.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(meningitis_rigid_neck_question, sepsis_meta),
            query._metadata_rerank_delta(
                meningitis_rigid_neck_question, common_ailments_meta
            ),
        )
        meningitis_vs_viral_question = "is this meningitis or a viral illness"
        headache_meta = {
            "guide_id": "GD-949",
            "guide_title": "Headaches: Basic Care",
            "section_heading": "Routine Headache Care",
            "slug": "headaches-basic-care",
            "description": "Common headaches from tension, mild illness, and eye strain.",
            "category": "medical",
        }
        sepsis_meta["guide_id"] = "GD-589"
        public_health_surveillance_meta["guide_id"] = "GD-268"
        self.assertLess(
            query._metadata_rerank_delta(meningitis_vs_viral_question, sepsis_meta),
            query._metadata_rerank_delta(meningitis_vs_viral_question, headache_meta),
        )
        self.assertLess(
            query._metadata_rerank_delta(meningitis_vs_viral_question, sepsis_meta),
            query._metadata_rerank_delta(
                meningitis_vs_viral_question, public_health_surveillance_meta
            ),
        )
        self.assertLess(
            query._metadata_rerank_delta(
                meningitis_vs_viral_question, public_health_surveillance_meta
            ),
            query._metadata_rerank_delta(meningitis_vs_viral_question, bug_bite_meta),
        )
        self.assertLess(
            query._metadata_rerank_delta(meningitis_vs_viral_question, headache_meta),
            query._metadata_rerank_delta(
                meningitis_vs_viral_question, public_health_surveillance_meta
            ),
        )

        choking_question = "child is choking on a grape"
        airway_meta = {
            "guide_title": "First Aid & Emergency Response",
            "section_heading": "Choking and Airway Management",
            "slug": "first-aid",
            "description": "Foreign body airway obstruction, back blows, abdominal thrusts, and chest thrusts.",
            "category": "medical",
        }
        ingestion_meta = {
            "guide_title": "Unknown Ingestion & Child Accidental Poisoning Triage",
            "section_heading": "The First Five Minutes",
            "slug": "unknown-ingestion-child-poisoning-triage",
            "description": "Poisoning, toxicology, swallowed substances, and unknown ingestion.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(choking_question, airway_meta),
            query._metadata_rerank_delta(choking_question, ingestion_meta),
        )
        food_bolus_question = "drooling and cannot swallow after a bite of food"
        self.assertLess(
            query._metadata_rerank_delta(food_bolus_question, airway_meta),
            query._metadata_rerank_delta(food_bolus_question, ingestion_meta),
        )
        self.assertLessEqual(
            query._metadata_rerank_delta(food_bolus_question, airway_meta),
            -0.26,
        )
        self.assertGreaterEqual(
            query._metadata_rerank_delta(food_bolus_question, ingestion_meta),
            0.30,
        )
        poison_question = "sleepy after possible medicine ingestion"
        toxicology_meta = {
            "guide_title": "Toxicology and Poisoning Response",
            "section_heading": "Initial Assessment",
            "slug": "toxicology-poisoning-response",
            "description": "Poison control, toxidrome, medication ingestion, and altered mental status.",
            "category": "medical",
        }
        allergy_meta = {
            "guide_title": "Allergy and Anaphylaxis",
            "section_heading": "Medication Hives",
            "slug": "allergy-anaphylaxis",
            "description": "Food allergy, hives, medication side effects, and routine rash checks.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(poison_question, toxicology_meta),
            query._metadata_rerank_delta(poison_question, allergy_meta),
        )
        panic_anatomy_meta = {
            "guide_title": "Human Anatomy Basics: Body Systems & Functions",
            "section_heading": "Respiratory Anatomy",
            "slug": "anatomy-basics-body-systems",
            "description": "Respiratory anatomy and panic breathing basics.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta("is this choking or just panic", airway_meta),
            query._metadata_rerank_delta("is this choking or just panic", panic_anatomy_meta),
        )

        newborn_question = "newborn is limp, will not feed, and is hard to wake"
        newborn_sepsis_meta = {
            "guide_title": "Sepsis Recognition and Antibiotic Protocols",
            "section_heading": "Newborn and Infant Danger Signs",
            "slug": "sepsis-recognition-antibiotic-protocols",
            "description": "Newborn sepsis, poor feeding, fever or low temperature, and hard to wake.",
            "category": "medical",
        }
        routine_baby_meta = {
            "guide_title": "Infant & Child Care",
            "section_heading": "Routine Feeding and Sleep",
            "slug": "infant-child-care",
            "description": "Routine baby care, feeding rhythms, and normal newborn behavior.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(newborn_question, newborn_sepsis_meta),
            query._metadata_rerank_delta(newborn_question, routine_baby_meta),
        )

        abdomen_question = "pale and dizzy after abdominal trauma"
        abdomen_meta = {
            "guide_title": "Acute Abdominal Emergencies",
            "section_heading": "Blunt Abdominal Trauma",
            "slug": "acute-abdominal-emergencies",
            "description": "Abdominal trauma, hard belly, shock, and internal bleeding.",
            "category": "medical",
        }
        routine_gi_meta = {
            "guide_title": "Common Ailments Recognition and Care",
            "section_heading": "Routine Stomach Upset",
            "slug": "common-ailments-recognition-care",
            "description": "Routine stomach symptoms, constipation, diarrhea, and digestive regularity.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(abdomen_question, abdomen_meta),
            query._metadata_rerank_delta(abdomen_question, routine_gi_meta),
        )
        child_fall_question = "child fell and now has belly pain"
        child_fall_gyn_meta = {
            "guide_title": "Gynecological Emergencies & Women's Health",
            "section_heading": "Ectopic Pregnancy: Recognition and Management",
            "slug": "gynecological-emergencies-womens-health",
            "description": "Ectopic pregnancy, vaginal bleeding, pelvic pain, and shock.",
            "category": "medical",
        }
        ear_nutrition_meta = {
            "guide_title": "Child Nutrition: School-Age Children & Teens",
            "section_heading": "Red Flags For Weight Loss Or Poor Growth",
            "slug": "child-nutrition-school-age-teens",
            "description": "Nutrition, ear pain, routine child symptoms, and growth concerns.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(child_fall_question, abdomen_meta),
            query._metadata_rerank_delta(child_fall_question, ear_nutrition_meta),
        )
        self.assertLessEqual(
            query._metadata_rerank_delta(child_fall_question, abdomen_meta),
            -0.24,
        )
        self.assertGreater(
            query._metadata_rerank_delta(child_fall_question, child_fall_gyn_meta),
            query._metadata_rerank_delta(child_fall_question, abdomen_meta),
        )
        self.assertLessEqual(
            query._metadata_rerank_delta(abdomen_question, abdomen_meta),
            -0.20,
        )
        handlebar_question = "left side pain after handlebar injury"
        back_pain_meta = {
            "guide_title": "Back Pain & Musculoskeletal Self-Care",
            "section_heading": "Routine Back Pain",
            "slug": "back-pain-musculoskeletal-self-care",
            "description": "Routine back pain, musculoskeletal self-care, rest, and NSAID guidance.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(handlebar_question, abdomen_meta),
            query._metadata_rerank_delta(handlebar_question, back_pain_meta),
        )
        self.assertGreaterEqual(
            query._metadata_rerank_delta(handlebar_question, back_pain_meta),
            0.30,
        )

        wound_question = "cut has spreading redness with pus and red streaks"
        wound_meta = {
            "guide_title": "Wound Hygiene, Infection Prevention & Field Sanitation",
            "section_heading": "When a Wound Is Getting Worse",
            "slug": "wound-hygiene-infection-prevention",
            "description": "Wound hygiene, infection prevention, pus, red streaks, and field sanitation.",
            "category": "medical",
        }
        rash_meta = {
            "guide_title": "Bug Bites, Stings & Itch Relief",
            "section_heading": "Common Rashes",
            "slug": "bug-bites-stings-itch-relief",
            "description": "Bug bites, poison ivy, contact rash, common rashes, and routine skin irritation.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(wound_question, wound_meta),
            query._metadata_rerank_delta(wound_question, rash_meta),
        )

        eye_question = "eye injury from flying debris, vision is darker on that side, and there may still be something in the eye"
        eye_meta = {
            "guide_title": "Eye Injuries, Infections & Emergency Ophthalmology",
            "section_heading": "Penetrating Injury: Do NOT Remove",
            "slug": "eye-injuries-emergency-care",
            "description": "Embedded high-speed debris, darker vision, shield without pressure, and urgent eye care.",
            "category": "medical",
        }
        pink_eye_meta = {
            "guide_title": "Red Eye, Pink Eye & Eye Irritation Home Care",
            "section_heading": "Safe Flushing For Something In My Eye",
            "slug": "eye-irritation-pink-eye-home-care",
            "description": "Mild red-eye complaints, safe flushing, styes, and eyelid bumps.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(eye_question, eye_meta),
            query._metadata_rerank_delta(eye_question, pink_eye_meta),
        )
        retinal_question = "new shower of floaters and shadow creeping across the side vision"
        retinal_eye_meta = {
            "guide_title": "Eye Injuries, Infections & Emergency Ophthalmology",
            "section_heading": "Complaint-First Triage",
            "slug": "eye-injuries-emergency-care",
            "description": "Sudden vision loss is an emergency; suspect retinal or optic nerve disease.",
            "category": "medical",
        }
        retinal_optics_meta = {
            "guide_title": "Optics & Vision Care",
            "section_heading": "Retinal Detachment Warning Signs",
            "slug": "optics-vision",
            "description": "Sudden floaters, flashes, and dark curtain can mean retinal detachment.",
            "category": "medical",
        }
        routine_optometry_meta = {
            "guide_title": "Vision Correction & Optometry",
            "section_heading": "Routine Eye Exams",
            "slug": "vision-correction-optometry",
            "description": "Gradual blurry vision, glasses, refraction, and routine correction.",
            "category": "medical",
        }
        astronomy_meta = {
            "guide_title": "Astronomy & Calendar Systems",
            "section_heading": "Observing The Night Sky",
            "slug": "astronomy-calendar-systems",
            "description": "Dark adaptation, bright lights, floaters, and observing stars.",
            "category": "sciences",
        }
        self.assertLess(
            query._metadata_rerank_delta(retinal_question, retinal_eye_meta),
            query._metadata_rerank_delta(retinal_question, pink_eye_meta),
        )
        self.assertLess(
            query._metadata_rerank_delta(retinal_question, retinal_optics_meta),
            query._metadata_rerank_delta(retinal_question, routine_optometry_meta),
        )
        self.assertLess(
            query._metadata_rerank_delta(retinal_question, retinal_eye_meta),
            query._metadata_rerank_delta(retinal_question, astronomy_meta),
        )

        electrical_question = "The outlet sparked and now smells hot. Should I test it?"
        electrical_meta = {
            "guide_title": "Electrical Safety & Hazard Prevention",
            "slug": "electrical-safety-hazard-prevention",
            "description": "Sparking outlet, live wire, do not touch, de-energize, and electrical hazard response.",
            "category": "survival",
        }
        repair_meta = {
            "guide_title": "Simple Home Repairs",
            "slug": "simple-home-repairs",
            "description": "Home repair, outlet testing, and test functionality after repair.",
            "category": "building",
        }
        self.assertLess(
            query._metadata_rerank_delta(electrical_question, electrical_meta),
            query._metadata_rerank_delta(electrical_question, repair_meta),
        )

        drowning_question = "Someone is drowning right now near shore. What do we do first?"
        drowning_meta = {
            "guide_title": "Drowning Prevention & Water Safety",
            "slug": "drowning-prevention-water-safety",
            "description": "Rescue priorities, reach throw row go, post-rescue breathing, CPR.",
            "category": "survival",
        }
        headache_meta = {
            "guide_title": "Headaches: Basic Care & Red Flags",
            "slug": "headaches-basic-care",
            "description": "Headache symptoms and routine red flags.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(drowning_question, drowning_meta),
            query._metadata_rerank_delta(drowning_question, headache_meta),
        )

        nosebleed_question = "The nosebleed will not stop after 20 minutes and blood is going down the throat."
        nosebleed_meta = {
            "guide_title": "Nosebleeds: Basic Care & When To Worry",
            "slug": "nosebleeds-basic-care",
            "description": "Epistaxis, blood down throat, blood thinners, lean forward, firm pressure, urgent medical attention.",
            "category": "medical",
        }
        dental_meta = {
            "guide_title": "Emergency Dental Care",
            "slug": "emergency-dental",
            "description": "Tooth injury, dental bleeding, and tooth pain.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(nosebleed_question, nosebleed_meta),
            query._metadata_rerank_delta(nosebleed_question, dental_meta),
        )

        blood_loss_question = "They are pale and dizzy after losing blood. What should I do first?"
        shock_meta = {
            "guide_title": "Hemorrhagic Shock, Septic Shock & Emergency Resuscitation",
            "slug": "shock-recognition-resuscitation",
            "description": "Hemorrhagic shock, blood loss, shock recognition, and resuscitation.",
            "category": "medical",
        }
        trauma_meta = {
            "guide_title": "Trauma Hemorrhage Control",
            "slug": "trauma-hemorrhage-control",
            "description": "Major bleeding, tourniquet, direct pressure, wound packing, blood loss.",
            "category": "medical",
        }
        self.assertTrue(query._is_major_blood_loss_shock_query(blood_loss_question))
        self.assertLess(
            query._metadata_rerank_delta(blood_loss_question, shock_meta),
            query._metadata_rerank_delta(blood_loss_question, nosebleed_meta),
        )
        self.assertLess(
            query._metadata_rerank_delta(blood_loss_question, trauma_meta),
            query._metadata_rerank_delta(blood_loss_question, nosebleed_meta),
        )
        blood_loss_specs = query._supplemental_retrieval_specs(blood_loss_question, 8)
        self.assertTrue(
            any("hemorrhagic shock blood loss" in spec["text"] for spec in blood_loss_specs),
            blood_loss_specs,
        )

        cardiac_question = "Chest tightness after stress but worse with exertion, is this panic?"
        cardiac_meta = {
            "guide_title": "Acute Coronary & Cardiac Emergencies",
            "slug": "acute-coronary-cardiac-emergencies",
            "description": "Chest pain, chest pressure, angina, heart attack, cardiac emergency.",
            "category": "medical",
        }
        anxiety_meta = {
            "guide_title": "Anxiety, Stress & Daily Self-Care",
            "slug": "anxiety-stress-self-care",
            "description": "Anxiety, panic, routine self-care, calming and grounding.",
            "category": "medical",
        }
        self.assertLess(
            query._metadata_rerank_delta(cardiac_question, cardiac_meta),
            query._metadata_rerank_delta(cardiac_question, anxiety_meta),
        )

    def test_safety_owner_citation_allowlist_prioritizes_retrieved_target_owners(self):
        self.assertEqual(
            query._prioritized_citation_allowlist_for_question(
                "drooling and cannot swallow after a bite of food",
                ["GD-898", "GD-911", "GD-232", "GD-579"],
            ),
            ["GD-232", "GD-579", "GD-898", "GD-911"],
        )
        meningitis_allowlist = query._prioritized_citation_allowlist_for_question(
            "is this meningitis or a viral illness",
            ["GD-949", "GD-268", "GD-589", "GD-298"],
        )
        self.assertEqual(
            meningitis_allowlist[0],
            "GD-589",
        )
        self.assertLess(
            meningitis_allowlist.index("GD-589"),
            meningitis_allowlist.index("GD-949"),
        )
        self.assertLess(
            meningitis_allowlist.index("GD-589"),
            meningitis_allowlist.index("GD-298"),
        )
        self.assertEqual(
            query._prioritized_citation_allowlist_for_question(
                "is this meningitis or a viral illness",
                ["GD-949", "GD-298", "GD-284", "GD-589"],
            ),
            ["GD-589", "GD-284", "GD-298", "GD-949"],
        )
        self.assertEqual(
            query._prioritized_citation_allowlist_for_question(
                "left side pain after handlebar injury",
                ["GD-915", "GD-380", "GD-039"],
            ),
            ["GD-380", "GD-915", "GD-039"],
        )

        fallback_results = {
            "metadatas": [[
                {"source_file": "GD-898.md"},
                {"guide_id": "GD-232"},
            ]]
        }
        self.assertEqual(
            query._citation_allowlist_from_results(fallback_results),
            ["GD-898", "GD-232"],
        )

        airway_results = {
            "documents": [["Airway note.", "Allergy note.", "First aid note."]],
            "metadatas": [[
                {"guide_id": "GD-298"},
                {"guide_id": "GD-400"},
                {"guide_id": "GD-232"},
            ]],
        }
        airway_contract = query._add_citation_allowlist_contract(
            "answer body",
            airway_results,
            question="is this choking or just panic",
        )
        self.assertIn("[GD-298]", airway_contract)
        self.assertIn("[GD-232]", airway_contract)
        self.assertNotIn("[GD-400]", airway_contract)

        general_contract = query._add_citation_allowlist_contract(
            "answer body",
            {
                "documents": [["Primary excerpt.", "Support excerpt."]],
                "metadatas": [[{"guide_id": "GD-024"}, {"guide_id": "GD-023"}]],
            },
            question="what should I do first?",
        )
        self.assertIn("Prefer the earliest listed retrieved guide", general_contract)

        allergic_airway_contract = query._add_citation_allowlist_contract(
            "answer body",
            airway_results,
            question="is this choking or anaphylaxis after a bee sting",
        )
        self.assertIn("[GD-400]", allergic_airway_contract)

        airway_prompt = query.build_prompt(
            "is this choking or just panic",
            {
                "documents": [["Airway owner excerpt.", "Allergy adjacent excerpt."]],
                "metadatas": [[
                    {
                        "guide_title": "Pediatric Emergency Medicine",
                        "guide_id": "GD-298",
                        "section_heading": "Foreign Body Airway Obstruction",
                        "category": "medical",
                        "difficulty": "intermediate",
                        "description": "Choking and airway obstruction.",
                    },
                    {
                        "guide_title": "Allergic Reactions, Anaphylaxis & Antihistamine Production",
                        "guide_id": "GD-400",
                        "section_heading": "Panic, Anaphylaxis, or Asthma Unclear",
                        "category": "medical",
                        "difficulty": "intermediate",
                        "description": "Allergy and anaphylaxis.",
                    },
                ]],
                "distances": [[0.1, 0.11]],
                "_senku": {
                    "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                    "objective_coverage": [],
                    "result_annotations": [{"support_signal": "direct"}, {"support_signal": "peripheral"}],
                },
            },
        )
        self.assertIn("Airway owner excerpt.", airway_prompt)
        self.assertNotIn("Allergy adjacent excerpt.", airway_prompt)
        self.assertIn("choking_airway_obstruction [GD-232]", airway_prompt)

        broad_airway_results = {
            "documents": [[
                "Respiratory distress triage: attempt simple airway opening with head tilt.",
                "Choking and airway management: cannot speak or cough, use choking rescue.",
            ]],
            "metadatas": [[
                {
                    "guide_title": "Pediatric Emergencies: Sepsis, Dehydration & Respiratory Distress",
                    "guide_id": "GD-617",
                    "section_heading": "Respiratory Distress Triage",
                    "category": "medical",
                    "difficulty": "advanced",
                    "description": "Respiratory distress triage.",
                },
                {
                    "guide_title": "First Aid & Emergency Response",
                    "guide_id": "GD-232",
                    "section_heading": "Choking and Airway Management",
                    "category": "medical",
                    "difficulty": "beginner",
                    "description": "Choking and airway obstruction.",
                },
            ]],
            "distances": [[0.1, 0.11]],
            "_senku": {
                "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                "objective_coverage": [],
                "result_annotations": [{"support_signal": "peripheral"}, {"support_signal": "direct"}],
            },
        }
        broad_airway_prompt = query.build_prompt(
            "is this choking or just panic",
            broad_airway_results,
        )
        self.assertNotIn("Respiratory distress triage: attempt simple airway opening", broad_airway_prompt)
        self.assertIn("Choking and airway management", broad_airway_prompt)
        broad_airway_contract = query._add_citation_allowlist_contract(
            "answer body",
            broad_airway_results,
            question="is this choking or just panic",
        )
        self.assertIn("[GD-232]", broad_airway_contract)
        self.assertNotIn("[GD-617]", broad_airway_contract)

    def test_safety_wave_prompt_notes_keep_emergency_actions_bounded(self):
        basic_results = {
            "documents": [["Focused emergency note."]],
            "metadatas": [[{
                "guide_title": "Emergency Guide",
                "guide_id": "GD-999",
                "section_heading": "Emergency Section",
                "category": "medical",
                "difficulty": "intermediate",
                "description": "",
            }]],
            "distances": [[0.2]],
            "_senku": {
                "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                "objective_coverage": [],
                "result_annotations": [{"support_signal": "direct", "matched_objectives": []}],
            },
        }

        gi_prompt = query.build_prompt(
            "There is bright red blood with bowel movements, but they are dizzy and pale.",
            basic_results,
        )
        self.assertIn("do not write 'apply direct pressure'", gi_prompt)
        self.assertIn("do not mention direct pressure at all", gi_prompt)

        gyn_prompt = query.build_prompt(
            "There is heavy bleeding with severe pelvic pain. What do I do first?",
            basic_results,
        )
        self.assertIn("do not give uterotonic drug names/doses", gyn_prompt)
        ef_prompt = query.build_prompt(
            "pregnant maybe 6 weeks and now cramping on one side and feeling faint",
            basic_results,
        )
        self.assertIn("Gynecologic/early-pregnancy red flags", ef_prompt)
        self.assertIn("do not recommend vaginal direct pressure", ef_prompt)
        self.assertIn("do not frame the action as postpartum hemorrhage care", ef_prompt)
        self.assertIn("use emergency evaluation/evacuation for possible ectopic pregnancy", ef_prompt)
        retinal_prompt = query.build_prompt(
            "sudden flashes and floaters then a dark curtain over one eye",
            basic_results,
        )
        self.assertIn("possible retinal detachment", retinal_prompt)
        self.assertIn("Do not treat as migraine", retinal_prompt)

        surgical_prompt = query.build_prompt(
            "Their belly is swollen and hard, they cannot keep anything down, and they have not pooped or farted since yesterday.",
            basic_results,
        )
        self.assertIn("Surgical-abdomen red flags", surgical_prompt)
        self.assertIn("Escalate urgently before dehydration", surgical_prompt)

        serotonin_prompt = query.build_prompt(
            "after changing meds i am restless overheated and twitching all over",
            basic_results,
        )
        self.assertIn("do not tell the user to stop all medicines indefinitely", serotonin_prompt)
        self.assertIn("Do not use broad stop phrases", serotonin_prompt)

        meningitis_prompt = query.build_prompt(
            "sore all over with fever and a spreading purplish rash and now acting confused",
            basic_results,
        )
        self.assertIn("must explicitly be to call EMS", meningitis_prompt)
        self.assertIn("do not lead with routine flu/rash care", meningitis_prompt)
        meningitis_nonblanching_prompt = query.build_prompt(
            "fever stiff neck and a purple rash that does not fade when pressed",
            basic_results,
        )
        self.assertIn("must explicitly be to call EMS", meningitis_nonblanching_prompt)
        self.assertIn(
            "do not lead with routine flu/rash care",
            meningitis_nonblanching_prompt,
        )
        meningitis_vs_viral_prompt = query.build_prompt(
            "is this meningitis or a viral illness",
            basic_results,
        )
        self.assertIn("Meningitis-vs-viral comparison prompts", meningitis_vs_viral_prompt)
        self.assertIn("if/then compare shape", meningitis_vs_viral_prompt)
        self.assertIn("you cannot diagnose from the label alone", meningitis_vs_viral_prompt)
        self.assertIn("Do not recommend Health Officer notification", meningitis_vs_viral_prompt)

        choking_prompt = query.build_prompt(
            "drooling and cannot swallow after a bite of food",
            basic_results,
        )
        self.assertIn("Choking/possible airway-obstruction prompts", choking_prompt)
        self.assertIn("Do not answer as poison ingestion", choking_prompt)
        self.assertIn("Do not mention anaphylaxis unless", choking_prompt)
        self.assertIn("Do not advise blind finger sweeps", choking_prompt)
        self.assertIn("has no allergy trigger", choking_prompt)
        self.assertIn(
            "do not mention allergy, allergic reaction, anaphylaxis, epinephrine",
            choking_prompt,
        )
        choking_vs_panic_prompt = query.build_prompt(
            "is this choking or just panic",
            basic_results,
        )
        self.assertIn("choking-versus-panic boundary prompts", choking_vs_panic_prompt)
        self.assertIn("give complete branch logic", choking_vs_panic_prompt)
        self.assertIn("do not end on a bare conditional heading", choking_vs_panic_prompt)

        newborn_prompt = query.build_prompt(
            "newborn looks very sick and weak",
            basic_results,
        )
        self.assertIn("Newborn danger signs", newborn_prompt)
        self.assertIn("urgent medical evaluation immediately", newborn_prompt)
        self.assertIn("first numbered instruction must explicitly say", newborn_prompt)
        self.assertIn("do not substitute topical antiseptic", newborn_prompt)

        abdominal_trauma_prompt = query.build_prompt(
            "pale and dizzy after abdominal trauma",
            basic_results,
        )
        self.assertIn("Abdominal trauma", abdominal_trauma_prompt)
        self.assertIn("Do not lead with routine stomach upset", abdominal_trauma_prompt)

        electrical_prompt = query.build_prompt(
            "Someone was shocked and cannot let go of the wire. What do we do first?",
            basic_results,
        )
        self.assertIn("do not touch the person, wire, wet panel", electrical_prompt)

        downed_line_prompt = query.build_prompt(
            "There is a downed power line across the driveway. What do we do first?",
            basic_results,
        )
        self.assertIn("do not approach the line", downed_line_prompt)
        self.assertIn("Do not say to verify power is off before", downed_line_prompt)
        self.assertIn("call the utility/emergency responders", downed_line_prompt)

        drowning_prompt = query.build_prompt(
            "Cold-water rescue and they are gasping hard. What first?",
            basic_results,
        )
        self.assertIn("reach/throw/row/go ladder", drowning_prompt)

        face_down_prompt = query.build_prompt(
            "I see someone face down and silent in the water. What matters first?",
            basic_results,
        )
        self.assertIn("Do not lead with observe/assess/watchful waiting", face_down_prompt)

        post_rescue_prompt = query.build_prompt(
            "They seemed okay after being pulled from the water but now they are coughing and short of breath.",
            basic_results,
        )
        self.assertIn("must explicitly say to get urgent medical evaluation/help now", post_rescue_prompt)

        nosebleed_prompt = query.build_prompt(
            "Nosebleed after starting blood thinners and now dizzy and pale.",
            basic_results,
        )
        self.assertIn("arranging urgent medical help", nosebleed_prompt)

        cardiac_prompt = query.build_prompt(
            "Is this a panic attack or heart attack if I have chest pressure and shortness of breath?",
            basic_results,
        )
        self.assertIn("must be cardiac-first", cardiac_prompt)

    def test_build_prompt_includes_reviewed_answer_card_contract_for_matching_guides(self):
        poison_results = {
            "documents": [["Unknown ingestion first actions."]],
            "metadatas": [[{
                "guide_title": "Unknown Ingestion & Child Accidental Poisoning Triage",
                "guide_id": "GD-898",
                "section_heading": "First Five Minutes",
                "category": "medical",
                "difficulty": "intermediate",
                "description": "Poisoning triage.",
            }]],
            "distances": [[0.05]],
            "_senku": {
                "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                "objective_coverage": [],
                "result_annotations": [{"support_signal": "direct", "matched_objectives": []}],
            },
        }

        prompt = query.build_prompt(
            "child swallowed something from an unlabeled cleaner bottle",
            poison_results,
        )

        self.assertIn("Evidence packet from reviewed guide answer cards", prompt)
        self.assertIn("poisoning_unknown_ingestion [GD-898]", prompt)
        self.assertIn("Citation anchors: unknown-ingestion-overview", prompt)
        self.assertIn("Call poison control", prompt)
        self.assertIn("Check airway and breathing", prompt)
        self.assertIn("Do not induce vomiting", prompt)
        self.assertIn("home remedies", prompt)

    def test_build_prompt_includes_abdominal_answer_card_contract_constraints(self):
        abdomen_results = {
            "documents": [["Abdominal trauma and shock excerpt."]],
            "metadatas": [[{
                "guide_title": "Acute Abdominal Emergencies & Surgical Decision-Making",
                "guide_id": "GD-380",
                "section_heading": "Blunt Abdominal Trauma",
                "category": "medical",
                "difficulty": "intermediate",
                "description": "Internal bleeding and shock.",
            }]],
            "distances": [[0.05]],
            "_senku": {
                "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                "objective_coverage": [],
                "result_annotations": [{"support_signal": "direct", "matched_objectives": []}],
            },
        }

        prompt = query.build_prompt(
            "pale and dizzy after abdominal trauma",
            abdomen_results,
        )

        self.assertIn("abdominal_internal_bleeding [GD-380]", prompt)
        self.assertIn("Check for signs of serious internal injury", prompt)
        self.assertIn("Urgent medical evaluation", prompt)
        self.assertIn("Keep the patient NPO", prompt)
        self.assertIn("shock", prompt)

    def test_answer_card_contract_block_preserves_retrieved_order(self):
        results = {
            "metadatas": [[
                {"guide_id": "GD-380"},
                {"guide_id": "GD-898"},
            ]]
        }

        block = query._answer_card_contract_block(
            "pale dizzy after abdominal trauma and possible cleaner ingestion",
            results,
        )

        abdomen_index = block.index("abdominal_internal_bleeding [GD-380]")
        poison_index = block.index("poisoning_unknown_ingestion [GD-898]")
        self.assertLess(abdomen_index, poison_index)

    def test_answer_card_contract_block_uses_source_file_fallback(self):
        results = {"metadatas": [[{"source_file": "GD-898.md"}]]}

        block = query._answer_card_contract_block(
            "child swallowed something from an unlabeled cleaner bottle",
            results,
        )

        self.assertIn("Evidence packet from reviewed guide answer cards", block)
        self.assertIn("poisoning_unknown_ingestion [GD-898]", block)

    def test_answer_card_contract_block_returns_empty_without_matching_card(self):
        self.assertEqual(
            query._answer_cards_for_results({"metadatas": [[{"guide_id": "GD-999"}]]}),
            [],
        )
        self.assertEqual(
            query._answer_card_contract_block(
                "generic emergency question",
                {"metadatas": [[{"guide_id": "GD-999"}]]},
            ),
            "",
        )

    def test_answer_card_contract_block_renders_active_choking_branch_only(self):
        infant_results = {"metadatas": [[{"guide_id": "GD-232"}]]}

        infant_block = query._answer_card_contract_block(
            "My baby is choking and cannot cry.",
            infant_results,
        )
        infant_active_line = next(
            line
            for line in infant_block.splitlines()
            if line.strip().startswith("Active conditional requirements:")
        )

        self.assertIn("5 back blows followed by 5 chest thrusts", infant_active_line)
        self.assertNotIn("abdominal thrusts", infant_active_line)

        adult_block = query._answer_card_contract_block(
            "An adult is choking and cannot speak.",
            infant_results,
        )
        adult_active_line = next(
            line
            for line in adult_block.splitlines()
            if line.strip().startswith("Active conditional requirements:")
        )

        self.assertIn("abdominal thrusts", adult_active_line)
        self.assertNotIn("back blows", adult_active_line)

    def test_answer_card_contract_supporting_airway_owner_fallback(self):
        results = {"metadatas": [[{"guide_id": "GD-298"}]]}

        cards = query._answer_cards_for_results(
            results,
            question="My child is choking and cannot cough.",
        )

        self.assertEqual([card["card_id"] for card in cards], ["choking_airway_obstruction"])

    def test_answer_card_contract_supporting_newborn_owner_fallback(self):
        results = {"metadatas": [[{"guide_id": "GD-492"}, {"guide_id": "GD-298"}]]}

        cards = query._answer_cards_for_results(
            results,
            question="newborn is limp, will not feed, and is hard to wake",
            max_cards=1,
        )

        self.assertEqual([card["card_id"] for card in cards], ["newborn_danger_sepsis"])

    def test_answer_card_contract_supporting_meningitis_owner_fallback_for_red_flags(self):
        results = {"metadatas": [[{"guide_id": "GD-268"}, {"guide_id": "GD-949"}]]}

        cards = query._answer_cards_for_results(
            results,
            question="child has fever and a purple rash that does not fade when pressed",
            max_cards=1,
        )

        self.assertEqual([card["card_id"] for card in cards], ["meningitis_sepsis_child"])

    def test_answer_card_contract_uses_meningitis_card_for_stiff_neck_compare(self):
        results = {"metadatas": [[{"guide_id": "GD-589"}, {"guide_id": "GD-284"}]]}

        cards = query._answer_cards_for_results(
            results,
            question="fever with stiff neck: meningitis vs viral illness?",
            max_cards=1,
        )

        self.assertEqual([card["card_id"] for card in cards], ["meningitis_sepsis_child"])

    def test_answer_card_contract_does_not_use_meningitis_card_for_ambiguous_comparison(self):
        results = {"metadatas": [[
            {"guide_id": "GD-284"},
            {"guide_id": "GD-589"},
            {"guide_id": "GD-268"},
            {"guide_id": "GD-949"},
        ]]}

        cards = query._answer_cards_for_results(
            results,
            question="is this meningitis or a viral illness",
            max_cards=1,
        )

        self.assertEqual(cards, [])
        self.assertNotIn(
            "meningitis_sepsis_child",
            [card["card_id"] for card in cards],
        )
        self.assertNotIn(
            "newborn_danger_sepsis",
            [card["card_id"] for card in cards],
        )

    def test_answer_card_contract_prioritizes_airway_card_over_retrieved_poison_card(self):
        results = {
            "metadatas": [[
                {"guide_id": "GD-298"},
                {"guide_id": "GD-898"},
                {"guide_id": "GD-232"},
            ]]
        }

        cards = query._answer_cards_for_results(
            results,
            question="is this choking or just panic",
            max_cards=1,
        )

        self.assertEqual([card["card_id"] for card in cards], ["choking_airway_obstruction"])

    def test_newborn_citation_allowlist_prioritizes_reviewed_owner_family(self):
        allowlist = query._prioritized_citation_allowlist_for_question(
            "baby has fever or low temperature and is acting weak",
            ["GD-617", "GD-298", "GD-284", "GD-492"],
        )

        self.assertEqual(allowlist, ["GD-284", "GD-492", "GD-298", "GD-617"])

    def test_answer_card_contract_budget_gate_starts_at_768_tokens(self):
        results = {"metadatas": [[{"guide_id": "GD-898"}]]}

        with patch.object(query, "estimate_tokens", return_value=10):
            self.assertEqual(
                query._answer_card_contract_block(
                    "child swallowed something from an unlabeled cleaner bottle",
                    results,
                    prompt_token_limit=767,
                ),
                "",
            )
            self.assertIn(
                "Evidence packet from reviewed guide answer cards",
                query._answer_card_contract_block(
                    "child swallowed something from an unlabeled cleaner bottle",
                    results,
                    prompt_token_limit=768,
                ),
            )

    def test_build_prompt_skips_answer_card_contract_without_match_or_tiny_budget(self):
        no_card_results = {
            "documents": [["Focused non-card note."]],
            "metadatas": [[{
                "guide_title": "Emergency Guide",
                "guide_id": "GD-999",
                "section_heading": "Emergency Section",
                "category": "medical",
                "difficulty": "intermediate",
                "description": "",
            }]],
            "distances": [[0.2]],
            "_senku": {
                "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                "objective_coverage": [],
                "result_annotations": [{"support_signal": "direct", "matched_objectives": []}],
            },
        }
        poison_results = {
            "documents": [["Unknown ingestion first actions."]],
            "metadatas": [[{
                "guide_title": "Unknown Ingestion & Child Accidental Poisoning Triage",
                "guide_id": "GD-898",
                "section_heading": "First Five Minutes",
                "category": "medical",
                "difficulty": "intermediate",
                "description": "Poisoning triage.",
            }]],
            "distances": [[0.05]],
            "_senku": no_card_results["_senku"],
        }

        no_card_prompt = query.build_prompt("generic emergency question", no_card_results)
        tiny_budget_prompt = query.build_prompt(
            "child swallowed something from an unlabeled cleaner bottle",
            poison_results,
            prompt_token_limit=512,
        )
        with patch.object(
            query,
            "_estimate_chat_prompt_tokens",
            return_value={"estimated_prompt_tokens": 9999},
        ):
            over_budget_prompt = query.build_prompt(
                "child swallowed something from an unlabeled cleaner bottle",
                poison_results,
                prompt_token_limit=4096,
            )

        self.assertNotIn("Evidence packet from reviewed guide answer cards", no_card_prompt)
        self.assertNotIn("Evidence packet from reviewed guide answer cards", tiny_budget_prompt)
        self.assertNotIn("Evidence packet from reviewed guide answer cards", over_budget_prompt)

    def test_build_prompt_compacts_context_for_small_runtime_budget(self):
        long_doc = " ".join(f"detail{i}" for i in range(900))
        results = {
            "documents": [[long_doc, long_doc, long_doc]],
            "metadatas": [[
                {
                    "guide_title": "Guide One",
                    "guide_id": "GD-101",
                    "section_heading": "Primary Section",
                    "category": "survival",
                    "difficulty": "basic",
                },
                {
                    "guide_title": "Guide Two",
                    "guide_id": "GD-102",
                    "section_heading": "Secondary Section",
                    "category": "survival",
                    "difficulty": "basic",
                },
                {
                    "guide_title": "Guide Three",
                    "guide_id": "GD-103",
                    "section_heading": "Tertiary Section",
                    "category": "survival",
                    "difficulty": "basic",
                },
            ]],
            "distances": [[0.1, 0.2, 0.3]],
            "_senku": {
                "scenario_frame": {
                    "objectives": [],
                    "assets": [],
                    "constraints": [],
                    "hazards": [],
                    "people": [],
                },
                "objective_coverage": [],
                "result_annotations": [
                    {"support_signal": "direct", "matched_objectives": []},
                    {"support_signal": "direct", "matched_objectives": []},
                    {"support_signal": "direct", "matched_objectives": []},
                ],
            },
        }

        prompt = query.build_prompt(
            "make a fast shelter plan",
            results,
            prompt_token_limit=1200,
        )

        self.assertIn("GD-101", prompt)
        self.assertIn("GD-102", prompt)
        self.assertIn("GD-103", prompt)
        self.assertIn("...", prompt)
        self.assertLess(prompt.count("detail"), 900)

    def test_card_backed_runtime_answer_gate_off_returns_none(self):
        results = {"metadatas": [[{"guide_id": "GD-898"}]]}

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "0"},
        ), patch.object(query, "_answer_cards_for_results") as cards_for_results:
            self.assertIsNone(
                query._card_backed_runtime_answer(
                    "child swallowed something from an unlabeled bottle",
                    results,
                )
            )

        cards_for_results.assert_not_called()

    def test_card_backed_runtime_answer_gate_on_success(self):
        results = {
            "documents": [["Unknown ingestion first actions."]],
            "metadatas": [[{"guide_id": "GD-898"}]],
        }
        card = {
            "card_id": "poisoning_unknown_ingestion",
            "guide_id": "GD-898",
            "review_status": "approved",
            "risk_tier": "critical",
        }
        plan = {
            "status": "ready",
            "cited_guide_ids": ["GD-898"],
            "answer_text": "Call poison control now. [GD-898]",
        }

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ), patch.object(
            query, "_answer_cards_for_results", return_value=[card]
        ) as cards_for_results, patch.object(
            query, "_compose_guide_card_backed_answer", return_value=plan
        ) as compose:
            self.assertEqual(
                query._card_backed_runtime_answer(
                    "child swallowed something from an unlabeled bottle",
                    results,
                ),
                "Call poison control now. [GD-898]",
            )

        cards_for_results.assert_called_once_with(
            results,
            question="child swallowed something from an unlabeled bottle",
            max_cards=1,
        )
        compose.assert_called_once_with(
            [card],
            question_text="child swallowed something from an unlabeled bottle",
            allowed_guide_ids=["GD-898"],
            max_cards=1,
        )

    def test_card_backed_runtime_answer_uses_real_reviewed_card(self):
        results = {
            "documents": [["Unknown ingestion first actions and no vomiting."]],
            "metadatas": [[{"guide_id": "GD-898"}]],
        }

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ):
            answer = query._card_backed_runtime_answer(
                "child swallowed something from an unlabeled cleaner bottle",
                results,
            )

        self.assertIsNotNone(answer)
        self.assertIn("Call poison control", answer)
        self.assertIn("Do not induce vomiting", answer)
        self.assertIn("[GD-898]", answer)

    def test_card_backed_runtime_answer_plan_includes_reviewed_card_provenance(self):
        results = {
            "documents": [["Unknown ingestion first actions and no vomiting."]],
            "metadatas": [[{"guide_id": "GD-898"}]],
        }

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ):
            plan = query._card_backed_runtime_answer_plan(
                "child swallowed something from an unlabeled cleaner bottle",
                results,
            )

        self.assertIsNotNone(plan)
        self.assertEqual(plan["card_ids"], ["poisoning_unknown_ingestion"])
        self.assertEqual(plan["review_status"], "pilot_reviewed")
        self.assertIn("GD-898", plan["guide_ids"])
        self.assertEqual(plan["cited_guide_ids"], ["GD-898"])
        self.assertIn("Call poison control", plan["answer_text"])

    def test_poisoning_card_priority_accepts_secondary_source_owner(self):
        results = {
            "documents": [["Possible medication ingestion and overdose triage."]],
            "metadatas": [[{"guide_id": "GD-914"}, {"guide_id": "GD-301"}]],
        }

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ):
            plan = query._card_backed_runtime_answer_plan(
                "sleepy after possible medicine ingestion",
                results,
            )

        self.assertIsNotNone(plan)
        self.assertEqual(plan["card_ids"], ["poisoning_unknown_ingestion"])
        self.assertEqual(plan["cited_guide_ids"], ["GD-301"])
        self.assertIn("Call poison control", plan["answer_text"])

    def test_poisoning_card_priority_can_use_lower_ranked_retrieved_owner(self):
        results = {
            "documents": [["Chemical burn context."]],
            "metadatas": [[
                {"guide_id": "GD-325"},
                {"guide_id": "GD-399"},
                {"guide_id": "GD-054"},
                {"guide_id": "GD-939"},
                {"guide_id": "GD-666"},
                {"guide_id": "GD-898"},
            ]],
        }

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ):
            plan = query._card_backed_runtime_answer_plan(
                "mouth burns after tasting a liquid",
                results,
            )

        self.assertIsNotNone(plan)
        self.assertEqual(plan["card_ids"], ["poisoning_unknown_ingestion"])
        self.assertEqual(plan["cited_guide_ids"], ["GD-898"])

    def test_poisoning_card_does_not_match_medicine_allergy_prompt(self):
        results = {"metadatas": [[{"guide_id": "GD-898"}]]}

        self.assertFalse(
            query._is_poisoning_unknown_ingestion_card_query(
                "I took medicine and now have hives but can breathe"
            )
        )
        self.assertEqual(
            query._answer_cards_for_results(
                results,
                question="I took medicine and now have hives but can breathe",
                max_cards=1,
            ),
            [],
        )

    def test_infected_wound_card_priority_uses_retrieved_owner_beyond_top_two(self):
        results = {
            "documents": [["Wound care adjacent context."]],
            "metadatas": [[
                {"guide_id": "GD-731"},
                {"guide_id": "GD-917"},
                {"guide_id": "GD-732"},
                {"guide_id": "GD-585"},
                {"guide_id": "GD-300"},
            ]],
        }

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ):
            plan = query._card_backed_runtime_answer_plan(
                "wound pain is getting worse instead of better",
                results,
            )

        self.assertIsNotNone(plan)
        self.assertEqual(plan["card_ids"], ["infected_wound_spreading_infection"])
        self.assertEqual(plan["cited_guide_ids"], ["GD-585"])
        self.assertIn("Escalate urgently", plan["answer_text"])

    def test_infected_wound_card_does_not_match_non_wound_pain(self):
        results = {"metadatas": [[{"guide_id": "GD-585"}]]}

        self.assertEqual(
            query._answer_cards_for_results(
                results,
                question="knee pain is getting worse instead of better",
                max_cards=1,
            ),
            [],
        )

    def test_card_backed_runtime_answer_uses_question_only_for_conditionals(self):
        results = {
            "documents": [[
                "Adult or older child complete obstruction instructions mention abdominal thrusts.",
                "If air is moving, encourage coughing and watch closely.",
            ]],
            "metadatas": [[
                {"guide_id": "GD-298"},
                {"guide_id": "GD-898"},
                {"guide_id": "GD-232"},
            ]],
        }

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ):
            answer = query._card_backed_runtime_answer(
                "is this choking or just panic",
                results,
            )

        self.assertIsNotNone(answer)
        self.assertIn("keep them upright, encourage coughing", answer)
        self.assertNotIn("start abdominal thrusts", answer)
        cards = find_cards_for_guides("GD-232", cards=load_answer_cards())
        card_result = evaluate_answer_card_contract(
            answer,
            cards,
            question_text="is this choking or just panic",
        )
        self.assertEqual(card_result["status"], "pass")
        claim_result = diagnose_claim_support(
            answer,
            cards,
            cited_guide_ids=["GD-232"],
            expected_guide_ids=["GD-232"],
            generated=True,
        )
        self.assertEqual(claim_result["status"], "pass")
        self.assertEqual(claim_result["forbidden_count"], 0)

    def test_card_backed_runtime_answer_allows_newborn_source_family_with_primary_owner_citation(self):
        results = {
            "metadatas": [[
                {"guide_id": "GD-492"},
                {"guide_id": "GD-298"},
                {"guide_id": "GD-284"},
            ]],
        }

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ):
            plan = query._card_backed_runtime_answer_plan(
                "newborn is limp, will not feed, and is hard to wake",
                results,
            )

        self.assertIsNotNone(plan)
        self.assertEqual(plan["card_ids"], ["newborn_danger_sepsis"])
        self.assertEqual(plan["cited_guide_ids"], ["GD-284"])
        self.assertIn("Keep the newborn warm", plan["answer_text"])
        self.assertIn("immediate escalation", plan["answer_text"])
        cards = [
            card
            for card in load_answer_cards()
            if card["card_id"] == "newborn_danger_sepsis"
        ]
        card_result = evaluate_answer_card_contract(
            plan["answer_text"],
            cards,
            question_text="newborn is limp, will not feed, and is hard to wake",
        )
        self.assertEqual(card_result["status"], "pass")
        claim_result = diagnose_claim_support(
            plan["answer_text"],
            cards,
            cited_guide_ids=["GD-284"],
            expected_guide_ids=["GD-284"],
            generated=True,
        )
        self.assertEqual(claim_result["status"], "pass")

    def test_card_backed_runtime_answer_allows_newborn_reviewed_source_family_without_primary_owner(self):
        results = {"metadatas": [[{"guide_id": "GD-492"}, {"guide_id": "GD-298"}]]}

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ):
            plan = query._card_backed_runtime_answer_plan(
                "newborn is limp, will not feed, and is hard to wake",
                results,
            )

        self.assertIsNotNone(plan)
        self.assertEqual(plan["card_ids"], ["newborn_danger_sepsis"])
        self.assertEqual(plan["cited_guide_ids"], ["GD-492"])

    def test_card_backed_runtime_answer_rejects_allowed_guide_mismatch(self):
        results = {"metadatas": [[{"guide_id": "GD-301"}]]}
        card = {
            "card_id": "poisoning_unknown_ingestion",
            "guide_id": "GD-898",
            "review_status": "approved",
            "risk_tier": "critical",
        }

        with patch.dict(
            query.os.environ,
            {"SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS": "1"},
        ), patch.object(
            query, "_answer_cards_for_results", return_value=[card]
        ), patch.object(query, "_compose_guide_card_backed_answer") as compose:
            self.assertIsNone(
                query._card_backed_runtime_answer(
                    "child swallowed something from an unlabeled bottle",
                    results,
                )
            )

        compose.assert_not_called()

    def test_stream_response_card_backed_answer_short_circuits_build_prompt(self):
        results = {
            "documents": [["Unknown ingestion first actions."]],
            "metadatas": [[{"guide_id": "GD-898"}]],
            "distances": [[0.05]],
            "_senku": {
                "scenario_frame": {
                    "objectives": [],
                    "assets": [],
                    "constraints": [],
                    "hazards": [],
                    "people": [],
                },
                "confidence_label": "high",
                "answer_mode": "confident",
            },
        }

        with patch.object(
            query, "_should_abstain", return_value=(False, [])
        ), patch.object(
            query, "_card_backed_runtime_answer", return_value="Card-backed answer. [GD-898]"
        ) as runtime_answer, patch.object(
            query, "build_prompt"
        ) as build_prompt, patch.object(query.console, "print"):
            response = query.stream_response(
                "child swallowed something from an unlabeled bottle",
                results,
            )

        self.assertEqual(response, "Card-backed answer. [GD-898]")
        runtime_answer.assert_called_once_with(
            "child swallowed something from an unlabeled bottle",
            results,
        )
        build_prompt.assert_not_called()

    def test_stream_response_card_backed_answer_can_override_uncertain_fit(self):
        results = {
            "documents": [["Newborn danger sign source family note."]],
            "metadatas": [[{"guide_id": "GD-492"}]],
            "distances": [[0.22]],
            "_senku": {
                "scenario_frame": {
                    "objectives": [],
                    "assets": [],
                    "constraints": [],
                    "hazards": [],
                    "people": [],
                },
                "confidence_label": "medium",
                "answer_mode": "uncertain_fit",
            },
        }

        with patch.object(
            query, "_should_abstain", return_value=(False, [])
        ), patch.object(
            query,
            "_card_backed_runtime_answer",
            return_value="Get urgent medical care now. [GD-492]",
        ) as runtime_answer, patch.object(
            query,
            "_build_uncertain_fit_body",
            side_effect=AssertionError("ready reviewed card should answer first"),
        ), patch.object(
            query, "build_prompt"
        ) as build_prompt, patch.object(query.console, "print"):
            response = query.stream_response(
                "is this normal newborn behavior or sepsis",
                results,
            )

        self.assertEqual(response, "Get urgent medical care now. [GD-492]")
        self.assertEqual(results["_senku"]["answer_mode"], "uncertain_fit")
        runtime_answer.assert_called_once_with(
            "is this normal newborn behavior or sepsis",
            results,
        )
        build_prompt.assert_not_called()

    def test_stream_response_uncertain_fit_falls_back_without_card_answer(self):
        results = {
            "documents": [["Weak routine newborn note."]],
            "metadatas": [[{"guide_id": "GD-401"}]],
            "distances": [[0.34]],
            "_senku": {
                "scenario_frame": {
                    "objectives": [],
                    "assets": [],
                    "constraints": [],
                    "hazards": [],
                    "people": [],
                },
                "confidence_label": "medium",
                "answer_mode": "uncertain_fit",
            },
        }

        with patch.object(
            query, "_should_abstain", return_value=(False, ["weak match"])
        ), patch.object(
            query,
            "_card_backed_runtime_answer",
            return_value=None,
        ) as runtime_answer, patch.object(
            query,
            "_build_uncertain_fit_body",
            return_value="uncertain fit fallback",
        ) as uncertain_fit_body, patch.object(
            query, "build_prompt"
        ) as build_prompt, patch.object(query.console, "print"):
            response = query.stream_response(
                "is this normal newborn behavior or sepsis",
                results,
            )

        self.assertEqual(response, "uncertain fit fallback")
        runtime_answer.assert_called_once_with(
            "is this normal newborn behavior or sepsis",
            results,
        )
        uncertain_fit_body.assert_called_once()
        build_prompt.assert_not_called()

    def test_gi_bleed_rerank_excludes_nosebleed_chunks(self):
        results = {
            "ids": [["gi", "nose"]],
            "documents": [["GI bleed emergency note.", "Nosebleed pressure note."]],
            "metadatas": [[
                {
                    "guide_title": "Acute Abdominal Emergencies & Surgical Decision-Making",
                    "guide_id": "GD-380",
                    "section_heading": "GI Bleeding Emergencies",
                    "slug": "acute-abdominal-emergencies",
                    "description": "Vomiting blood and melena.",
                    "category": "medical",
                },
                {
                    "guide_title": "Nosebleeds: Basic Care & When To Worry",
                    "guide_id": "GD-947",
                    "section_heading": "First Steps",
                    "slug": "nosebleeds-basic-care",
                    "description": "Nosebleed pressure and leaning forward.",
                    "category": "medical",
                },
            ]],
            "distances": [[0.2, 0.01]],
        }

        reranked = query.rerank_results(
            "They threw up blood after heavy drinking. What do I do first?",
            results,
            2,
            scenario_frame=query.build_scenario_frame(
                "They threw up blood after heavy drinking. What do I do first?"
            ),
        )

        guide_ids = [meta["guide_id"] for meta in reranked["metadatas"][0]]
        self.assertEqual(guide_ids, ["GD-380"])

    def test_water_sanitation_style_domain_split_is_deduped(self):
        question = "how do i handle clean water sanitation shelter and medical needs"
        sub_queries = query.decompose_query(question)

        self.assertEqual(sub_queries[0], question)
        self.assertLessEqual(len(sub_queries), 3)

    def test_build_prompt_includes_house_and_canoe_scope_notes(self):
        house_results = {
            "documents": [["Low-tech cabin build notes."]],
            "metadatas": [[{
                "guide_title": "Primitive Shelter Construction Techniques",
                "guide_id": "GD-345",
                "section_heading": "One-Room Cabin",
                "category": "building",
                "difficulty": "intermediate",
                "description": "",
            }]],
            "distances": [[0.2]],
            "_senku": {
                "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                "objective_coverage": [],
                "result_annotations": [{"support_signal": "direct", "matched_objectives": []}],
            },
        }
        canoe_results = {
            "documents": [["Dugout canoe notes."]],
            "metadatas": [[{
                "guide_title": "Small Watercraft Construction",
                "guide_id": "GD-682",
                "section_heading": "Dugout Canoe Construction",
                "category": "transportation",
                "difficulty": "intermediate",
                "description": "",
            }]],
            "distances": [[0.2]],
            "_senku": {
                "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                "objective_coverage": [],
                "result_annotations": [{"support_signal": "direct", "matched_objectives": []}],
            },
        }

        house_prompt = query.build_prompt("how do i build a house", house_results)
        canoe_prompt = query.build_prompt("how do i build a canoe", canoe_results)

        self.assertIn("low-tech starter dwelling path", house_prompt)
        self.assertIn("synthesize one coherent starter cabin path", house_prompt)
        self.assertIn("site/drainage, floor/foundation, walls/frame, and roof/weatherproofing", house_prompt)
        self.assertIn("simplest grounded build path", canoe_prompt)
        self.assertIn("Choose one primary build path", canoe_prompt)
        self.assertIn("shape the hull, seal/fit out, shoreline test and limits", canoe_prompt)

    def test_prompt_includes_water_fire_and_weld_scope_notes(self):
        basic_results = {
            "documents": [["Simple field note."]],
            "metadatas": [[{
                "guide_title": "Water Purification",
                "guide_id": "GD-035",
                "section_heading": "Filtration Systems",
                "category": "survival",
                "difficulty": "intermediate",
                "description": "",
            }]],
            "distances": [[0.2]],
            "_senku": {
                "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                "objective_coverage": [],
                "result_annotations": [{"support_signal": "direct", "matched_objectives": []}],
            },
        }

        water_prompt = query.build_prompt("how do i build a charcoal sand water filter", basic_results)
        fire_prompt = query.build_prompt("how do i start a fire in rain", basic_results)
        weld_prompt = query.build_prompt("how do i join metal without a welder", basic_results)

        self.assertIn("filtration, disinfection, and storage clearly separated", water_prompt)
        self.assertIn("source triage, prefilter, disinfect, safe storage", water_prompt)
        self.assertIn("dry-core ignition plan", fire_prompt)
        self.assertIn("fire suppression, wildfire management", fire_prompt)
        self.assertIn("joining metal without an electric welder", weld_prompt)
        self.assertIn("choose method, prep joint, heat/join, inspect limits", weld_prompt)

    def test_build_prompt_handles_sparse_metadata_rows(self):
        sparse_results = {
            "documents": [["Sparse field note."]],
            "metadatas": [[{}]],
            "distances": [[0.2]],
            "_senku": {
                "scenario_frame": {"objectives": [], "assets": [], "constraints": [], "hazards": [], "people": []},
                "objective_coverage": [],
                "result_annotations": [{"support_signal": "direct", "matched_objectives": []}],
            },
        }

        prompt = query.build_prompt("how do i build a house", sparse_results)

        self.assertIn("Guide: Unknown guide", prompt)
        self.assertIn("Category: unknown", prompt)
        self.assertIn("Difficulty: unknown", prompt)

    def test_retrieve_results_raises_on_embedding_count_mismatch(self):
        class DummyCollection:
            def query(self, **kwargs):
                raise AssertionError("collection.query should not run on embedding mismatch")

        with patch("query.embed_batch", return_value=[]):
            with self.assertRaisesRegex(RuntimeError, "Embedding response count mismatch"):
                query.retrieve_results("how do i build a house", DummyCollection(), 5)


if __name__ == "__main__":
    unittest.main()
