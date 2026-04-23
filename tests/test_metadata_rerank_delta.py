import unittest
from unittest.mock import patch

import query


class MetadataRerankDeltaTests(unittest.TestCase):
    def test_apply_metadata_rerank_delta_clamps_positive_and_negative(self):
        with self.assertLogs("query", level="DEBUG") as captured:
            positive = query._apply_metadata_rerank_delta(
                0.26,
                "test_positive",
                0.10,
                "chunk-pos",
                debug_enabled=True,
            )
            negative = query._apply_metadata_rerank_delta(
                -0.28,
                "test_negative",
                -0.10,
                "chunk-neg",
                debug_enabled=True,
            )

        self.assertAlmostEqual(positive, query.METADATA_RERANK_DELTA_MAX)
        self.assertAlmostEqual(negative, query.METADATA_RERANK_DELTA_MIN)
        self.assertTrue(
            any(
                "chunk=chunk-pos branch=test_positive delta=0.040 cumulative=0.300"
                in entry
                for entry in captured.output
            )
        )
        self.assertTrue(
            any(
                "chunk=chunk-neg branch=test_negative delta=-0.020 cumulative=-0.300"
                in entry
                for entry in captured.output
            )
        )

    def test_metadata_rerank_delta_caps_large_positive_stack(self):
        question = (
            "My brother is hearing voices, acting invincible, has not slept for days, "
            "won't stop moving, and is making unsafe choices."
        )
        meta = {
            "guide_id": "GD-100",
            "guide_title": "Routine stability and orientation",
            "section_heading": "Sleep hygiene and grounding",
            "slug": "routine-stability-orientation",
            "description": (
                "Psychological first aid, sleep hygiene, bedtime routine, daily routine, "
                "maintain basic function, reduce anxiety."
            ),
            "category": "building",
            "related": "dementia wandering orientation caregiver communication",
            "tags": "journaling, routine self-care, predictable routine",
            "source_file": "GD-100.md",
        }

        delta = query._metadata_rerank_delta(question, meta)

        self.assertAlmostEqual(delta, query.METADATA_RERANK_DELTA_MAX)

    def test_metadata_rerank_delta_emits_branch_debug_logs(self):
        question = (
            "My brother is hearing voices, acting invincible, has not slept for days, "
            "won't stop moving, and is making unsafe choices."
        )
        meta = {
            "guide_id": "GD-100",
            "guide_title": "Routine stability and orientation",
            "section_heading": "Sleep hygiene and grounding",
            "slug": "routine-stability-orientation",
            "description": (
                "Psychological first aid, sleep hygiene, bedtime routine, daily routine, "
                "maintain basic function, reduce anxiety."
            ),
            "category": "building",
            "related": "dementia wandering orientation caregiver communication",
            "tags": "journaling, routine self-care, predictable routine",
            "source_file": "GD-100.md",
        }

        with self.assertLogs("query", level="DEBUG") as captured:
            delta = query._metadata_rerank_delta(question, meta)

        self.assertAlmostEqual(delta, query.METADATA_RERANK_DELTA_MAX)
        self.assertTrue(
            any(
                "chunk=GD-100 branch=mental_health_category_fieldcraft"
                in entry
                for entry in captured.output
            )
        )
        self.assertTrue(
            any(
                "chunk=GD-100 branch=mental_health_support_mania"
                in entry
                for entry in captured.output
            )
        )
        self.assertTrue(
            any(
                "chunk=GD-100 branch=mental_health_sleep_self_management"
                in entry and "cumulative=0.300" in entry
                for entry in captured.output
            )
        )

    def test_bridge_guides_get_uniform_demote_on_acute_overlap_queries(self):
        question = (
            "My dad collapsed, has chest pain, and one side of his face is drooping. "
            "What do I do right now?"
        )
        base_meta = {
            "guide_id": "GD-401",
            "guide_title": "Healthcare Capability Assessment",
            "section_heading": "Capacity overview",
            "slug": "healthcare-capability-assessment",
            "description": "System planning for clinics, wards, staffing, and medical supply chains.",
            "category": "medical",
            "related": "",
            "tags": "healthcare systems, planning",
            "source_file": "GD-401.md",
        }

        plain_delta = query._metadata_rerank_delta(question, dict(base_meta))
        bridge_delta = query._metadata_rerank_delta(
            question,
            dict(base_meta, bridge="bridge-guide"),
        )

        self.assertGreater(bridge_delta, plain_delta)
        self.assertAlmostEqual(bridge_delta - plain_delta, 0.06, places=3)

    def test_planning_style_acute_query_skips_bridge_demotion(self):
        question = (
            "Before we set up a clinic, how should we plan for chest pain and stroke readiness "
            "in a remote camp?"
        )
        bridge_meta = {
            "guide_id": "GD-401",
            "guide_title": "Healthcare Capability Assessment",
            "section_heading": "Capacity overview",
            "slug": "healthcare-capability-assessment",
            "description": "System planning for clinics, wards, staffing, and medical supply chains.",
            "category": "medical",
            "related": "",
            "tags": "healthcare systems, planning",
            "bridge": True,
            "source_file": "GD-401.md",
        }

        delta = query._metadata_rerank_delta(question, bridge_meta)

        self.assertLess(delta, 0.06)
        self.assertFalse(query._is_bridge_demoted_acute_query(question))

    def test_rerank_results_emits_aggregate_timing_debug_log(self):
        results = {
            "ids": [["chunk-a", "chunk-b"]],
            "documents": [["Store water in clean containers.", "Rotate stored water on schedule."]],
            "metadatas": [[
                {
                    "guide_id": "GD-252",
                    "guide_title": "Storage & Material Management",
                    "section_heading": "Water Storage Basics",
                    "category": "resource-management",
                    "source_file": "GD-252.md",
                },
                {
                    "guide_id": "GD-386",
                    "guide_title": "Rationing & Equitable Distribution",
                    "section_heading": "Rotation Schedule",
                    "category": "survival",
                    "source_file": "GD-386.md",
                },
            ]],
            "distances": [[0.21, 0.24]],
        }

        with (
            patch("query.time.perf_counter", side_effect=[10.0, 10.012]),
            self.assertLogs("query", level="DEBUG") as captured,
        ):
            reranked = query.rerank_results(
                "how should i store treated water",
                results,
                top_k=1,
                scenario_frame={"objectives": []},
            )

        timing = reranked["_senku_review"]["rerank_timing"]
        self.assertEqual(timing["top_k"], 1)
        self.assertEqual(timing["chunk_count"], 2)
        self.assertEqual(timing["selected_count"], 1)
        self.assertAlmostEqual(timing["total_ms"], 12.0)
        self.assertAlmostEqual(timing["avg_ms_per_chunk"], 6.0)
        self.assertTrue(
            any(
                "rerank_timing top_k=1 chunks=2 selected=1 total_ms=12.000 avg_ms_per_chunk=6.000"
                in entry
                for entry in captured.output
            )
        )


if __name__ == "__main__":
    unittest.main()
