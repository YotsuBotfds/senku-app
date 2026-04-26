import importlib
import os
import subprocess
import sys
import unittest

import query_citation_policy as citation_policy


def _false(_question):
    return False


class QueryCitationPolicyTests(unittest.TestCase):
    def test_citation_policy_import_does_not_load_query(self):
        script = (
            "import importlib, sys; "
            "sys.modules.pop('query', None); "
            "importlib.import_module('query_citation_policy'); "
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

    def test_owner_priority_orders_retrieved_target_owners_first(self):
        allowlist = citation_policy._prioritized_citation_allowlist_for_question(
            "drooling and cannot swallow after a bite of food",
            ["GD-898", "GD-911", "GD-232", "GD-579"],
            is_airway_obstruction_rag_query=lambda question: True,
            is_meningitis_rash_emergency_query=_false,
            is_meningitis_vs_viral_query=_false,
            is_newborn_sepsis_danger_query=_false,
            is_abdominal_trauma_danger_query=_false,
        )

        self.assertEqual(allowlist, ["GD-232", "GD-579", "GD-898", "GD-911"])

    def test_owner_priority_tolerates_unhashable_allowlist_entries(self):
        malformed_entry = {"guide_id": "GD-898"}
        unhashable_entry = ["GD-579"]

        allowlist = citation_policy._prioritized_citation_allowlist_for_question(
            "drooling and cannot swallow after a bite of food",
            [malformed_entry, "GD-911", "GD-232", unhashable_entry],
            is_airway_obstruction_rag_query=lambda question: True,
            is_meningitis_rash_emergency_query=_false,
            is_meningitis_vs_viral_query=_false,
            is_newborn_sepsis_danger_query=_false,
            is_abdominal_trauma_danger_query=_false,
        )

        self.assertEqual(allowlist, ["GD-232", malformed_entry, "GD-911", unhashable_entry])

    def test_newborn_owner_priority_uses_reviewed_owner_family_order(self):
        allowlist = citation_policy._prioritized_citation_allowlist_for_question(
            "baby has fever or low temperature and is acting weak",
            ["GD-617", "GD-298", "GD-284", "GD-492"],
            is_airway_obstruction_rag_query=_false,
            is_meningitis_rash_emergency_query=_false,
            is_meningitis_vs_viral_query=_false,
            is_newborn_sepsis_danger_query=lambda question: True,
            is_abdominal_trauma_danger_query=_false,
        )

        self.assertEqual(allowlist, ["GD-284", "GD-492", "GD-298", "GD-617"])

    def test_no_owner_priority_preserves_original_allowlist(self):
        guide_ids = ["GD-911", "GD-898", "GD-232"]

        allowlist = citation_policy._prioritized_citation_allowlist_for_question(
            "routine care question",
            guide_ids,
            is_airway_obstruction_rag_query=_false,
            is_meningitis_rash_emergency_query=_false,
            is_meningitis_vs_viral_query=_false,
            is_newborn_sepsis_danger_query=_false,
            is_abdominal_trauma_danger_query=_false,
        )

        self.assertIs(allowlist, guide_ids)

    def test_airway_non_allergy_question_narrows_to_retrieved_airway_owner_ids(self):
        allowlist = citation_policy._citation_guide_ids_for_question(
            "is this choking or just panic",
            {"metadatas": [[{"guide_id": "GD-298"}]]},
            ["GD-298", "GD-400", "GD-232"],
            prioritized_citation_allowlist_for_question=lambda question, guide_ids: guide_ids,
            is_airway_obstruction_rag_query=lambda question: True,
            has_allergy_or_anaphylaxis_trigger=_false,
            airway_obstruction_allowed_guide_ids_from_results=lambda results: ["GD-232"],
            is_meningitis_vs_viral_query=_false,
            is_meningitis_rash_emergency_query=_false,
        )

        self.assertEqual(allowlist, ["GD-232"])

    def test_airway_non_allergy_falls_back_to_owner_ids_from_allowlist(self):
        allowlist = citation_policy._citation_guide_ids_for_question(
            "is this choking or just panic",
            {"metadatas": [[{"guide_id": "GD-400"}]]},
            ["GD-400", "GD-298", "GD-232"],
            prioritized_citation_allowlist_for_question=lambda question, guide_ids: guide_ids,
            is_airway_obstruction_rag_query=lambda question: True,
            has_allergy_or_anaphylaxis_trigger=_false,
            airway_obstruction_allowed_guide_ids_from_results=lambda results: [],
            is_meningitis_vs_viral_query=_false,
            is_meningitis_rash_emergency_query=_false,
        )

        self.assertEqual(allowlist, ["GD-298", "GD-232"])

    def test_airway_allergy_question_does_not_narrow_allergy_sources(self):
        allowlist = citation_policy._citation_guide_ids_for_question(
            "is this choking or anaphylaxis after a bee sting",
            {"metadatas": [[{"guide_id": "GD-400"}]]},
            ["GD-298", "GD-400", "GD-232"],
            prioritized_citation_allowlist_for_question=lambda question, guide_ids: guide_ids,
            is_airway_obstruction_rag_query=lambda question: True,
            has_allergy_or_anaphylaxis_trigger=lambda question: True,
            airway_obstruction_allowed_guide_ids_from_results=lambda results: ["GD-232"],
            is_meningitis_vs_viral_query=_false,
            is_meningitis_rash_emergency_query=_false,
        )

        self.assertEqual(allowlist, ["GD-298", "GD-400", "GD-232"])

    def test_meningitis_compare_filters_to_clinical_owner_ids(self):
        allowlist = citation_policy._citation_guide_ids_for_question(
            "is this meningitis or a viral illness",
            {"metadatas": [[{"guide_id": "GD-949"}]]},
            ["GD-949", "GD-268", "GD-589", "GD-298"],
            prioritized_citation_allowlist_for_question=lambda question, guide_ids: guide_ids,
            is_airway_obstruction_rag_query=_false,
            has_allergy_or_anaphylaxis_trigger=_false,
            airway_obstruction_allowed_guide_ids_from_results=lambda results: [],
            is_meningitis_vs_viral_query=lambda question: True,
            is_meningitis_rash_emergency_query=_false,
        )

        self.assertEqual(allowlist, ["GD-268", "GD-589"])

    def test_meningitis_rash_emergency_does_not_apply_compare_filter(self):
        allowlist = citation_policy._citation_guide_ids_for_question(
            "fever with stiff neck and purple rash",
            {"metadatas": [[{"guide_id": "GD-949"}]]},
            ["GD-949", "GD-268", "GD-589", "GD-298"],
            prioritized_citation_allowlist_for_question=lambda question, guide_ids: guide_ids,
            is_airway_obstruction_rag_query=_false,
            has_allergy_or_anaphylaxis_trigger=_false,
            airway_obstruction_allowed_guide_ids_from_results=lambda results: [],
            is_meningitis_vs_viral_query=lambda question: True,
            is_meningitis_rash_emergency_query=lambda question: True,
        )

        self.assertEqual(allowlist, ["GD-949", "GD-268", "GD-589", "GD-298"])


if __name__ == "__main__":
    unittest.main()
