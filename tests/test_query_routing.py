import unittest
from unittest.mock import patch

import query


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

    def test_stove_co_prompt_does_not_trigger_household_chemical_rerank(self):
        question = "I get a headache every time we run the stove indoors for heat"
        self.assertFalse(query._is_household_chemical_eye_query(question))
        self.assertFalse(query._is_household_chemical_inhalation_query(question))

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
