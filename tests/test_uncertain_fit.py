import copy
import unittest
from unittest.mock import patch

import query


def _row(
    *,
    doc,
    guide_id,
    guide_title,
    section_heading,
    category,
    dist,
    rrf_score,
    vector_hits=1,
    lexical_hits=0,
):
    return {
        "doc": doc,
        "meta": {
            "guide_id": guide_id,
            "guide_title": guide_title,
            "section_heading": section_heading,
            "category": category,
            "_rrf_score": rrf_score,
            "_vector_hits": vector_hits,
            "_lexical_hits": lexical_hits,
        },
        "dist": dist,
    }


def _results(rows, *, annotations=None, objective_coverage=None):
    return {
        "documents": [[row["doc"] for row in rows]],
        "metadatas": [[row["meta"] for row in rows]],
        "distances": [[row["dist"] for row in rows]],
        "_senku": {
            "result_annotations": annotations or [],
            "objective_coverage": objective_coverage or [],
        },
    }


class UncertainFitModeTests(unittest.TestCase):
    def test_resolve_answer_mode_hits_boundaries_and_safety_gate(self):
        confident_results = _results(
            [
                _row(
                    doc="Tarp shelter ridge-line setup with rain runoff and stake order.",
                    guide_id="GD-201",
                    guide_title="Tarp Shelter Setup",
                    section_heading="Rain pitch",
                    category="survival",
                    dist=0.31,
                    rrf_score=0.022,
                    vector_hits=1,
                    lexical_hits=1,
                ),
                _row(
                    doc="Supporting tarp shelter notes for runoff and anchors.",
                    guide_id="GD-202",
                    guide_title="Shelter Anchors",
                    section_heading="Anchor choices",
                    category="survival",
                    dist=0.33,
                    rrf_score=0.020,
                    vector_hits=1,
                    lexical_hits=1,
                ),
            ]
        )
        upper_band_uncertain_results = _results(
            [
                _row(
                    doc="Tarp shelter ridge-line setup with rain runoff and cord tensioning.",
                    guide_id="GD-205",
                    guide_title="Ridgeline Rain Cover",
                    section_heading="Corded tarp setup",
                    category="survival",
                    dist=0.35,
                    rrf_score=0.022,
                    vector_hits=1,
                    lexical_hits=1,
                ),
                _row(
                    doc="Drainage trench reminders for a tarp rain cover with cord anchors.",
                    guide_id="GD-206",
                    guide_title="Rain Cover Drainage",
                    section_heading="Anchor and runoff checks",
                    category="survival",
                    dist=0.36,
                    rrf_score=0.021,
                    vector_hits=1,
                    lexical_hits=1,
                ),
            ]
        )
        uncertain_results = _results(
            [
                _row(
                    doc="Tarp shelter repair notes with patching and tie-out reminders.",
                    guide_id="GD-203",
                    guide_title="Shelter Repair",
                    section_heading="Patch and retension",
                    category="survival",
                    dist=0.37,
                    rrf_score=0.018,
                    vector_hits=1,
                    lexical_hits=1,
                ),
                _row(
                    doc="Related shelter maintenance checklist.",
                    guide_id="GD-204",
                    guide_title="Camp Maintenance",
                    section_heading="Rain checklist",
                    category="survival",
                    dist=0.42,
                    rrf_score=0.017,
                    vector_hits=1,
                    lexical_hits=0,
                ),
            ]
        )
        abstain_results = _results(
            [
                _row(
                    doc="Canvas repair and weather patching for existing gear.",
                    guide_id="GD-102",
                    guide_title="Canvas Repair",
                    section_heading="Patching",
                    category="crafts",
                    dist=0.81,
                    rrf_score=0.010,
                    vector_hits=1,
                    lexical_hits=0,
                ),
                _row(
                    doc="Tent care, storage, and mildew cleanup.",
                    guide_id="GD-103",
                    guide_title="Tent Maintenance",
                    section_heading="Storage",
                    category="resource-management",
                    dist=0.95,
                    rrf_score=0.008,
                    vector_hits=0,
                    lexical_hits=0,
                ),
            ]
        )
        safety_uncertain_results = _results(
            [
                _row(
                    doc="Behavior crisis supervision notes without a direct emergency owner match.",
                    guide_id="GD-301",
                    guide_title="Behavior Supervision Notes",
                    section_heading="Observation",
                    category="mental-health",
                    dist=0.32,
                    rrf_score=0.023,
                    vector_hits=1,
                    lexical_hits=0,
                ),
                _row(
                    doc="General de-escalation checklist.",
                    guide_id="GD-302",
                    guide_title="Calm Presence Checklist",
                    section_heading="Observation",
                    category="mental-health",
                    dist=0.33,
                    rrf_score=0.021,
                    vector_hits=1,
                    lexical_hits=0,
                ),
            ]
        )
        safety_confident_results = _results(
            [
                _row(
                    doc="Immediate crisis-owner steps for supervision, means restriction, and escalation.",
                    guide_id="GD-303",
                    guide_title="Mental Health Crisis Guide",
                    section_heading="Immediate actions",
                    category="mental-health",
                    dist=0.28,
                    rrf_score=0.024,
                    vector_hits=1,
                    lexical_hits=1,
                ),
                _row(
                    doc="Same-day urgent escalation checklist.",
                    guide_id="GD-304",
                    guide_title="Crisis Escalation",
                    section_heading="Urgent help",
                    category="mental-health",
                    dist=0.32,
                    rrf_score=0.021,
                    vector_hits=1,
                    lexical_hits=1,
                ),
            ],
            annotations=[{"support_signal": "direct"}],
            objective_coverage=[{"status": "covered"}],
        )
        safety_profile_weak_results = _results(
            [
                _row(
                    doc="Weakly related newborn note about normal behavior and observation.",
                    guide_id="GD-401",
                    guide_title="Routine Infant Care",
                    section_heading="Normal behavior",
                    category="medical",
                    dist=0.34,
                    rrf_score=0.023,
                    vector_hits=1,
                    lexical_hits=0,
                ),
                _row(
                    doc="Another weak newborn reference without direct sepsis triage ownership.",
                    guide_id="GD-402",
                    guide_title="Newborn Monitoring",
                    section_heading="Routine checks",
                    category="medical",
                    dist=0.36,
                    rrf_score=0.021,
                    vector_hits=1,
                    lexical_hits=0,
                ),
            ],
            annotations=[{"support_signal": "weak"}, {"support_signal": "weak"}],
            objective_coverage=[{"status": "missing"}],
        )
        safety_profile_weak_results["_senku"].update(
            {
                "retrieval_profile": "safety_triage",
                "safety_critical": True,
            }
        )

        cases = [
            (
                "how do i build a tarp lean-to shelter",
                confident_results,
                "confident",
            ),
            (
                "how do i rig a tarp rain cover with cord",
                upper_band_uncertain_results,
                "uncertain_fit",
            ),
            (
                "how do i repair a tarp shelter after wind damage",
                uncertain_results,
                "uncertain_fit",
            ),
            (
                "how do i build a rain shelter from a tarp",
                abstain_results,
                "abstain",
            ),
            (
                "he has barely slept, keeps pacing, and says normal rules do not apply to him",
                safety_uncertain_results,
                "uncertain_fit",
            ),
            (
                "he has barely slept, keeps pacing, and says normal rules do not apply to him",
                safety_confident_results,
                "confident",
            ),
            (
                "is this normal newborn behavior or sepsis",
                safety_profile_weak_results,
                "uncertain_fit",
            ),
        ]

        for question, results, expected in cases:
            with self.subTest(question=question, expected=expected):
                confidence_label = query._confidence_label(results, {"question": question})
                if results is safety_profile_weak_results:
                    confidence_label = "medium"
                mode = query._resolve_answer_mode(
                    results,
                    {"question": question},
                    confidence_label,
                )
                self.assertEqual(expected, mode)

    def test_stream_response_routes_reviewer_example_to_uncertain_fit(self):
        reviewer_example = (
            "He has barely slept, keeps pacing, says normal rules do not apply to him. "
            "Is this just stress, or should I help him calm down?"
        )
        results = _results(
            [
                _row(
                    doc="Behavior crisis supervision notes with urgent escalation reminders.",
                    guide_id="GD-305",
                    guide_title="Behavior Supervision Notes",
                    section_heading="Observation",
                    category="mental-health",
                    dist=0.32,
                    rrf_score=0.023,
                    vector_hits=1,
                    lexical_hits=0,
                ),
                _row(
                    doc="General de-escalation checklist for observers.",
                    guide_id="GD-306",
                    guide_title="Calm Presence Checklist",
                    section_heading="Observation",
                    category="mental-health",
                    dist=0.33,
                    rrf_score=0.021,
                    vector_hits=1,
                    lexical_hits=0,
                ),
            ]
        )

        with patch(
            "query.build_prompt",
            side_effect=AssertionError("generation should be skipped for uncertain-fit"),
        ):
            response = query.stream_response(reviewer_example, copy.deepcopy(results))

        self.assertIn("not a confident fit", response)
        self.assertIn("Possibly relevant guides in the library:", response)
        self.assertIn(query._SAFETY_CRITICAL_ESCALATION_LINE, response)
        self.assertNotIn("Closest matches in the library:", response)
        self.assertFalse(response.startswith('Senku doesn\'t have a guide for "'))

    def test_stream_response_uses_review_safety_flag_for_uncertain_fit_copy(self):
        prompt = "is this normal newborn behavior or sepsis"
        results = _results(
            [
                _row(
                    doc="Weak routine newborn note.",
                    guide_id="GD-401",
                    guide_title="Routine Infant Care",
                    section_heading="Normal behavior",
                    category="medical",
                    dist=0.34,
                    rrf_score=0.023,
                    vector_hits=1,
                    lexical_hits=0,
                ),
            ],
            annotations=[{"support_signal": "weak"}],
            objective_coverage=[{"status": "missing"}],
        )
        results["_senku"].update(
            {
                "retrieval_profile": "safety_triage",
                "safety_critical": True,
                "confidence_label": "medium",
            }
        )

        with patch(
            "query.build_prompt",
            side_effect=AssertionError("generation should be skipped for weak safety profile"),
        ):
            response = query.stream_response(prompt, copy.deepcopy(results))

        self.assertIn("not a confident fit", response)
        self.assertIn(query._SAFETY_CRITICAL_ESCALATION_LINE, response)


if __name__ == "__main__":
    unittest.main()
