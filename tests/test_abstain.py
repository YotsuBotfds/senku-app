import copy
import unittest
from unittest.mock import patch

import query


def _results_for_abstain(rows):
    documents = []
    metadatas = []
    distances = []
    for row in rows:
        documents.append(row["doc"])
        metadatas.append(row["meta"])
        distances.append(row["dist"])
    return {
        "documents": [documents],
        "metadatas": [metadatas],
        "distances": [distances],
    }


class AbstainPathTests(unittest.TestCase):
    def test_should_abstain_on_weak_top_rows(self):
        results = _results_for_abstain(
            [
                {
                    "doc": "This shelter note is mostly about smoke ventilation.",
                    "meta": {
                        "guide_id": "GD-101",
                        "guide_title": "Shelter Ventilation Basics",
                        "section_heading": "Ventilation",
                        "category": "building",
                        "_vector_hits": 1,
                        "_lexical_hits": 0,
                    },
                    "dist": 0.78,
                },
                {
                    "doc": "Canvas repair and weather patching for existing gear.",
                    "meta": {
                        "guide_id": "GD-102",
                        "guide_title": "Canvas Repair",
                        "section_heading": "Patching",
                        "category": "crafts",
                        "_vector_hits": 1,
                        "_lexical_hits": 0,
                    },
                    "dist": 0.81,
                },
                {
                    "doc": "Tent care, storage, and mildew cleanup.",
                    "meta": {
                        "guide_id": "GD-103",
                        "guide_title": "Tent Maintenance",
                        "section_heading": "Storage",
                        "category": "resource-management",
                        "_vector_hits": 0,
                        "_lexical_hits": 0,
                    },
                    "dist": 0.95,
                },
            ]
        )

        should_abstain, match_labels = query._should_abstain(
            results, "how do i build a rain shelter from a tarp"
        )

        self.assertTrue(should_abstain)
        self.assertEqual(
            ["low match", "off-topic candidate", "off-topic candidate"], match_labels
        )

    def test_should_not_abstain_when_similarity_crosses_threshold(self):
        results = _results_for_abstain(
            [
                {
                    "doc": "Tarp shelter ridge line and rain runoff setup.",
                    "meta": {
                        "guide_id": "GD-104",
                        "guide_title": "Tarp Shelter Setup",
                        "section_heading": "Rain Pitch",
                        "category": "survival",
                        "_vector_hits": 1,
                        "_lexical_hits": 0,
                    },
                    "dist": 0.37,
                }
            ]
        )

        should_abstain, _match_labels = query._should_abstain(
            results, "how do i build a rain shelter from a tarp"
        )

        self.assertFalse(should_abstain)

    def test_build_abstain_response_has_stable_single_card_shape(self):
        results = _results_for_abstain(
            [
                {
                    "doc": "Canvas repair and tarp patching for existing gear.",
                    "meta": {
                        "guide_id": "GD-102",
                        "guide_title": "Canvas Repair",
                        "section_heading": "Patching",
                        "category": "crafts",
                        "_vector_hits": 1,
                        "_lexical_hits": 0,
                    },
                    "dist": 0.81,
                },
                {
                    "doc": "Tent care, storage, and mildew cleanup.",
                    "meta": {
                        "guide_id": "GD-103",
                        "guide_title": "Tent Maintenance",
                        "section_heading": "Storage",
                        "category": "crafts",
                        "_vector_hits": 0,
                        "_lexical_hits": 0,
                    },
                    "dist": 0.95,
                },
            ]
        )
        match_labels = ["low match", "off-topic candidate"]

        response = query.build_abstain_response(
            "how do i build a rain shelter from a tarp", results, match_labels
        )

        self.assertEqual(
            'Senku doesn\'t have a guide for "how do i build a rain shelter from a tarp".\n\n'
            "Closest matches in the library:\n"
            "- [GD-102] Canvas Repair - crafts | low match\n"
            "- [GD-103] Tent Maintenance - crafts | off-topic candidate\n\n"
            "Try:\n"
            "- rephrasing the question\n"
            "- browsing the crafts category\n"
            '- asking a simpler version (for example, "what is X?")',
            response,
        )

    def test_stream_response_bypasses_generation_for_abstain(self):
        results = _results_for_abstain(
            [
                {
                    "doc": "Canvas repair and tarp patching for existing gear.",
                    "meta": {
                        "guide_id": "GD-102",
                        "guide_title": "Canvas Repair",
                        "section_heading": "Patching",
                        "category": "crafts",
                        "_vector_hits": 1,
                        "_lexical_hits": 0,
                    },
                    "dist": 0.81,
                }
            ]
        )

        with patch("query.build_prompt", side_effect=AssertionError("generation should be skipped")):
            response = query.stream_response(
                "how do i build a rain shelter from a tarp",
                results,
            )

        self.assertIn('Senku doesn\'t have a guide for "how do i build a rain shelter from a tarp".', response)

    def test_stream_response_adds_safety_critical_escalation_only_for_flagged_abstain(self):
        weak_results = _results_for_abstain(
            [
                {
                    "doc": "Canvas repair and tarp patching for existing gear.",
                    "meta": {
                        "guide_id": "GD-102",
                        "guide_title": "Canvas Repair",
                        "section_heading": "Patching",
                        "category": "crafts",
                        "_vector_hits": 1,
                        "_lexical_hits": 0,
                    },
                    "dist": 0.81,
                },
                {
                    "doc": "Tent care, storage, and mildew cleanup.",
                    "meta": {
                        "guide_id": "GD-103",
                        "guide_title": "Tent Maintenance",
                        "section_heading": "Storage",
                        "category": "resource-management",
                        "_vector_hits": 0,
                        "_lexical_hits": 0,
                    },
                    "dist": 0.95,
                },
            ]
        )
        cases = [
            (
                "my child may have poisoning after swallowing drain cleaner",
                True,
            ),
            (
                "how do i build a rain shelter from a tarp",
                False,
            ),
        ]

        for question, expect_escalation in cases:
            with self.subTest(question=question):
                results = copy.deepcopy(weak_results)
                with patch(
                    "query.build_prompt",
                    side_effect=AssertionError("generation should be skipped"),
                ):
                    response = query.stream_response(question, results)

                has_escalation = query._SAFETY_CRITICAL_ESCALATION_LINE in response
                self.assertEqual(expect_escalation, has_escalation)
                if expect_escalation:
                    self.assertLess(
                        response.index(query._SAFETY_CRITICAL_ESCALATION_LINE),
                        response.index("Closest matches in the library:"),
                    )


if __name__ == "__main__":
    unittest.main()
