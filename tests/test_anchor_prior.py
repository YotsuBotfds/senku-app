import unittest
from unittest.mock import patch

import query


def _result_set(retrieval_kind, guide_ids):
    ids = [f"{guide_id}-{index}" for index, guide_id in enumerate(guide_ids)]
    return {
        "ids": [ids],
        "documents": [[f"{guide_id} document" for guide_id in guide_ids]],
        "metadatas": [
            [
                {
                    "guide_id": guide_id,
                    "guide_title": f"Guide {guide_id}",
                    "section_heading": f"Section {index}",
                }
                for index, guide_id in enumerate(guide_ids)
            ]
        ],
        "distances": [[0.10 + (index * 0.01) for index, _ in enumerate(guide_ids)]],
        "_retrieval_kind": retrieval_kind,
    }


def _anchor_state(anchor_guide_id="GD-444", turn_count=1, anchor_turn_index=0):
    state = query.empty_session_state()
    state["anchor_guide_id"] = anchor_guide_id
    state["anchor_turn_index"] = anchor_turn_index
    state["turn_count"] = turn_count
    return state


def _guide_ids(results):
    return [meta["guide_id"] for meta in results["metadatas"][0]]


class AnchorPriorTests(unittest.TestCase):
    def test_follow_up_anchor_chunk_wins_when_present(self):
        result_sets = [
            _result_set("lexical", ["GD-999", "GD-444"]),
            _result_set("vector", ["GD-999", "GD-444"]),
        ]

        with patch.object(query, "ENABLE_ANCHOR_PRIOR", True), patch(
            "query.get_anchor_related_link_weights", return_value={}
        ):
            merged = query.merge_results(
                result_sets,
                2,
                question="what should I do next?",
                session_state=_anchor_state(),
            )

        self.assertEqual(["GD-444", "GD-999"], _guide_ids(merged))

    def test_stronger_multi_lane_off_topic_signal_can_outbid_anchor_prior(self):
        result_sets = [
            _result_set("vector", ["GD-999"]),
            _result_set("lexical", ["GD-999"]),
            _result_set("vector", ["GD-999"]),
            _result_set("lexical", ["GD-999"]),
            _result_set("vector", ["GD-999"]),
            _result_set("lexical", ["GD-999", "GD-000", "GD-111", "GD-222", "GD-333", "GD-444"]),
        ]

        with patch.object(query, "ENABLE_ANCHOR_PRIOR", True), patch(
            "query.get_anchor_related_link_weights", return_value={}
        ):
            merged = query.merge_results(
                result_sets,
                2,
                question="what should I do next?",
                session_state=_anchor_state(),
            )

        self.assertEqual("GD-999", _guide_ids(merged)[0])

    def test_anchor_prior_expires_after_three_turns(self):
        result_sets = [
            _result_set("lexical", ["GD-999", "GD-444"]),
            _result_set("vector", ["GD-999", "GD-444"]),
        ]

        with patch.object(query, "ENABLE_ANCHOR_PRIOR", True), patch(
            "query.get_anchor_related_link_weights", return_value={}
        ):
            merged = query.merge_results(
                result_sets,
                2,
                question="what should I do next?",
                session_state=_anchor_state(turn_count=4, anchor_turn_index=0),
            )

        self.assertEqual(["GD-999", "GD-444"], _guide_ids(merged))

    def test_explicit_reset_keyword_skips_anchor_prior(self):
        result_sets = [
            _result_set("lexical", ["GD-999", "GD-444"]),
            _result_set("vector", ["GD-999", "GD-444"]),
        ]

        with patch.object(query, "ENABLE_ANCHOR_PRIOR", True), patch(
            "query.get_anchor_related_link_weights", return_value={}
        ):
            merged = query.merge_results(
                result_sets,
                2,
                question="unrelated: how do I purify water?",
                session_state=_anchor_state(),
            )

        self.assertEqual(["GD-999", "GD-444"], _guide_ids(merged))

    def test_subject_reset_question_does_not_trigger_anchor_prior(self):
        result_sets = [
            _result_set("lexical", ["GD-999", "GD-444"]),
            _result_set("vector", ["GD-999", "GD-444"]),
        ]

        with patch.object(query, "ENABLE_ANCHOR_PRIOR", True), patch(
            "query.get_anchor_related_link_weights", return_value={}
        ):
            merged = query.merge_results(
                result_sets,
                2,
                question="how do I build a rain shelter?",
                session_state=_anchor_state(),
            )

        self.assertEqual(["GD-999", "GD-444"], _guide_ids(merged))

    def test_anchor_prior_only_reranks_present_chunks(self):
        result_sets = [
            _result_set("lexical", ["GD-999", "GD-888"]),
            _result_set("vector", ["GD-999", "GD-888"]),
        ]

        with patch.object(query, "ENABLE_ANCHOR_PRIOR", True), patch(
            "query.get_anchor_related_link_weights", return_value={"GD-777": 0.5}
        ):
            merged = query.merge_results(
                result_sets,
                2,
                question="what should I do next?",
                session_state=_anchor_state(),
            )

        self.assertEqual(["GD-999", "GD-888"], _guide_ids(merged))

    def test_anchor_prior_bonus_is_clamped(self):
        result_sets = [_result_set("vector", ["GD-444"])]

        with patch.object(query, "ENABLE_ANCHOR_PRIOR", True), patch.object(
            query, "ANCHOR_BASE_BONUS", 0.25
        ), patch("query.get_anchor_related_link_weights", return_value={}):
            merged = query.merge_results(
                result_sets,
                1,
                question="what should I do next?",
                session_state=_anchor_state(),
            )

        self.assertAlmostEqual(0.116393, merged["metadatas"][0][0]["_rrf_score"], places=6)

    def test_multi_turn_anchor_prior_biases_second_and_third_follow_ups(self):
        turn_two_sets = [
            _result_set("lexical", ["GD-999", "GD-423"]),
            _result_set("vector", ["GD-999", "GD-423"]),
        ]
        turn_three_sets = [
            _result_set("lexical", ["GD-998", "GD-423"]),
            _result_set("vector", ["GD-998", "GD-423"]),
        ]
        session_state = query._record_anchor_turn(
            query.empty_session_state(),
            "how do i build a charcoal sand water filter",
            "GD-423",
        )

        with patch.object(query, "ENABLE_ANCHOR_PRIOR", True), patch(
            "query.get_anchor_related_link_weights", return_value={}
        ):
            turn_two = query.merge_results(
                turn_two_sets,
                2,
                question="what about charcoal",
                session_state=session_state,
            )
            session_state = query._record_anchor_turn(
                session_state,
                "what about charcoal",
                query._primary_result_guide_id(turn_two),
            )
            turn_three = query.merge_results(
                turn_three_sets,
                2,
                question="what about sand",
                session_state=session_state,
            )

        self.assertEqual("GD-423", _guide_ids(turn_two)[0])
        self.assertEqual("GD-423", _guide_ids(turn_three)[0])

    def test_anchor_reset_turn_clears_follow_up_bias_for_next_question(self):
        result_sets = [
            _result_set("lexical", ["GD-999", "GD-423"]),
            _result_set("vector", ["GD-999", "GD-423"]),
        ]
        session_state = query._record_anchor_turn(
            query.empty_session_state(),
            "how do i build a charcoal sand water filter",
            "GD-423",
        )
        session_state = query._record_anchor_turn(
            session_state,
            "unrelated: how do i purify water?",
            "GD-777",
        )

        with patch.object(query, "ENABLE_ANCHOR_PRIOR", True), patch(
            "query.get_anchor_related_link_weights", return_value={}
        ):
            merged = query.merge_results(
                result_sets,
                2,
                question="what about sand",
                session_state=session_state,
            )

        self.assertEqual(["GD-999", "GD-423"], _guide_ids(merged))


if __name__ == "__main__":
    unittest.main()
