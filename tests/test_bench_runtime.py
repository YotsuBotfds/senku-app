import unittest
from unittest.mock import patch

import bench
import query


SMALL_MODEL_PROFILE = {
    "name": "small-model",
    "top_k": 8,
    "prompt_token_limit": 4096,
    "empty_completion_retries": 1,
    "adaptive_completion_retries": 1,
    "completion_retry_multiplier": 1.34,
    "completion_retry_min_step": 256,
    "completion_retry_max_tokens": 2048,
    "cap_hit_token_ratio": 0.90,
    "retry_on_cap_hit": True,
}


class BenchRuntimeTests(unittest.TestCase):
    def test_format_sources_preserves_ranked_source_candidates(self):
        results = {
            "metadatas": [[
                {
                    "guide_id": "GD-232",
                    "guide_title": "First Aid & Emergency Response",
                    "section_heading": "Choking and Airway Management",
                    "category": "medical",
                    "source_file": "first-aid.md",
                },
                {
                    "guide_id": "GD-579",
                    "guide_title": "Emergency Airway Management & Respiratory Distress Protocols",
                    "section_heading": "Foreign Body Airway Obstruction",
                    "category": "medical",
                    "source_file": "emergency-airway-management.md",
                },
                {
                    "guide_id": "GD-232",
                    "guide_title": "First Aid & Emergency Response",
                    "section_heading": "Choking and Airway Management",
                    "category": "medical",
                    "source_file": "first-aid.md",
                },
            ]]
        }

        retrieval_meta = {
            "retrieval_profile": "safety_triage",
            "answer_mode": "confident",
            "confidence_label": "high",
            "scenario_frame": {"safety_critical": True},
            "safety_critical": True,
            "support_signals": {"direct": 1, "related": 1},
        }

        source_info = bench.format_sources(
            results,
            "Use first aid. [GD-232]",
            retrieval_meta,
        )

        self.assertEqual(source_info["source_candidate_guide_ids"], ["GD-232", "GD-579"])
        self.assertEqual(
            [candidate["rank"] for candidate in source_info["source_candidates"]],
            [1, 2],
        )
        self.assertEqual(source_info["source_candidates"][0]["section"], "Choking and Airway Management")
        self.assertEqual(source_info["retrieval_profile"], "safety_triage")
        self.assertEqual(source_info["answer_mode"], "confident")
        self.assertEqual(source_info["support_strength"], "strong")
        self.assertTrue(source_info["safety_critical"])
        self.assertEqual(
            source_info["primary_source_titles"][:1],
            ["First Aid & Emergency Response -> Choking and Airway Management"],
        )

    def test_format_sources_treats_safety_profile_as_safety_critical(self):
        source_info = bench.format_sources(
            {"metadatas": [[]]},
            "answer",
            {
                "retrieval_profile": "safety_triage",
                "scenario_frame": {"safety_critical": False},
                "safety_critical": False,
                "confidence_label": "medium",
            },
        )

        self.assertTrue(source_info["safety_critical"])
        self.assertEqual(source_info["support_strength"], "moderate")

    @patch("bench._card_backed_runtime_answer_plan", return_value=None)
    @patch("bench._should_abstain", return_value=(False, ["weak match"]))
    @patch("bench.retrieve_chunks")
    def test_prepare_prompt_returns_uncertain_fit_as_immediate_response(
        self,
        mock_retrieve_chunks,
        _mock_should_abstain,
        mock_card_backed_answer_plan,
    ):
        results = {
            "documents": [["Weak routine newborn note."]],
            "metadatas": [[{
                "guide_id": "GD-401",
                "guide_title": "Routine Infant Care",
                "section_heading": "Normal behavior",
                "category": "medical",
                "_rrf_score": 0.023,
                "_vector_hits": 1,
                "_lexical_hits": 0,
            }]],
            "distances": [[0.34]],
        }
        retrieval_meta = {
            "answer_mode": "uncertain_fit",
            "confidence_label": "medium",
            "retrieval_profile": "safety_triage",
            "safety_critical": True,
            "scenario_frame": {"question": "is this normal newborn behavior or sepsis"},
        }
        mock_retrieve_chunks.return_value = (results, ["is this normal newborn behavior or sepsis"], retrieval_meta)

        index, question, prompt_text, prepared_results, meta = bench.prepare_prompt(
            0,
            "is this normal newborn behavior or sepsis",
            collection=object(),
            top_k=6,
            category=None,
        )

        self.assertEqual(index, 0)
        self.assertEqual(question, "is this normal newborn behavior or sepsis")
        self.assertEqual(prompt_text, "")
        self.assertIs(prepared_results, results)
        self.assertEqual(meta["decision_path"], "uncertain_fit")
        self.assertEqual(meta["confidence_label"], "medium")
        self.assertIn("not a confident fit", meta["special_case_response"])
        self.assertIn(query._SAFETY_CRITICAL_ESCALATION_LINE, meta["special_case_response"])
        mock_card_backed_answer_plan.assert_called_once_with(
            "is this normal newborn behavior or sepsis",
            results,
        )

    def test_prepare_prompt_returns_card_backed_runtime_as_immediate_response(self):
        results = {
            "documents": [["Unknown ingestion first actions."]],
            "metadatas": [[{"guide_id": "GD-898"}]],
            "distances": [[0.05]],
        }
        retrieval_meta = {
            "answer_mode": "confident",
            "confidence_label": "high",
            "retrieval_profile": "safety_triage",
            "scenario_frame": {
                "question": "child swallowed something from an unlabeled cleaner bottle"
            },
        }

        card_plan = {
            "answer_text": "Call poison control now. [GD-898]",
            "card_ids": ["poisoning_unknown_ingestion"],
            "review_status": "pilot_reviewed",
            "guide_ids": ["GD-898", "GD-301"],
            "cited_guide_ids": ["GD-898"],
        }

        with patch("bench.classify_special_case", return_value=(None, None)), patch(
            "bench.build_special_case_response", return_value=None
        ), patch(
            "bench.retrieve_chunks",
            return_value=(
                results,
                ["child swallowed something from an unlabeled cleaner bottle"],
                retrieval_meta,
            ),
        ), patch("bench._should_abstain", return_value=(False, [])), patch(
            "bench._card_backed_runtime_answer_plan",
            return_value=card_plan,
        ) as card_backed_answer_plan, patch("bench.build_prompt") as build_prompt:
            index, question, prompt_text, prepared_results, meta = bench.prepare_prompt(
                0,
                "child swallowed something from an unlabeled cleaner bottle",
                collection=object(),
                top_k=8,
                category=None,
            )

        self.assertEqual(index, 0)
        self.assertEqual(question, "child swallowed something from an unlabeled cleaner bottle")
        self.assertEqual(prompt_text, "")
        self.assertIs(prepared_results, results)
        self.assertEqual(meta["decision_path"], "card_backed_runtime")
        self.assertEqual(meta["answer_provenance"], "reviewed_card_runtime")
        self.assertTrue(meta["reviewed_card_backed"])
        self.assertEqual(meta["reviewed_card_ids"], ["poisoning_unknown_ingestion"])
        self.assertEqual(meta["reviewed_card_review_status"], "pilot_reviewed")
        self.assertEqual(meta["reviewed_card_guide_ids"], ["GD-898", "GD-301"])
        self.assertEqual(meta["reviewed_card_cited_guide_ids"], ["GD-898"])
        self.assertEqual(meta["generation_time"], 0)
        self.assertEqual(meta["special_case_response"], "Call poison control now. [GD-898]")
        card_backed_answer_plan.assert_called_once_with(
            "child swallowed something from an unlabeled cleaner bottle",
            results,
        )
        build_prompt.assert_not_called()

    def test_prepare_prompt_allows_card_backed_runtime_for_uncertain_fit(self):
        results = {
            "documents": [["Newborn danger sign source family note."]],
            "metadatas": [[{"guide_id": "GD-492"}]],
            "distances": [[0.22]],
        }
        retrieval_meta = {
            "answer_mode": "uncertain_fit",
            "confidence_label": "medium",
            "retrieval_profile": "safety_triage",
            "safety_critical": True,
            "scenario_frame": {"question": "is this normal newborn behavior or sepsis"},
        }
        card_plan = {
            "answer_text": "Get urgent medical care now and keep the newborn warm. [GD-492]",
            "card_ids": ["newborn_danger_sepsis"],
            "review_status": "pilot_reviewed",
            "guide_ids": ["GD-284", "GD-492"],
            "cited_guide_ids": ["GD-492"],
        }

        with patch("bench.classify_special_case", return_value=(None, None)), patch(
            "bench.build_special_case_response", return_value=None
        ), patch(
            "bench.retrieve_chunks",
            return_value=(
                results,
                ["is this normal newborn behavior or sepsis"],
                retrieval_meta,
            ),
        ), patch("bench._should_abstain", return_value=(False, [])), patch(
            "bench._card_backed_runtime_answer_plan",
            return_value=card_plan,
        ) as card_backed_answer_plan, patch(
            "bench._build_uncertain_fit_body",
            side_effect=AssertionError("ready reviewed card should answer first"),
        ), patch("bench.build_prompt") as build_prompt:
            _index, _question, prompt_text, prepared_results, meta = bench.prepare_prompt(
                0,
                "is this normal newborn behavior or sepsis",
                collection=object(),
                top_k=8,
                category=None,
            )

        self.assertEqual(prompt_text, "")
        self.assertIs(prepared_results, results)
        self.assertEqual(meta["decision_path"], "card_backed_runtime")
        self.assertEqual(meta["answer_provenance"], "reviewed_card_runtime")
        self.assertTrue(meta["reviewed_card_backed"])
        self.assertEqual(meta["reviewed_card_ids"], ["newborn_danger_sepsis"])
        self.assertEqual(meta["reviewed_card_cited_guide_ids"], ["GD-492"])
        self.assertEqual(meta["retrieval_metadata"]["answer_mode"], "uncertain_fit")
        self.assertEqual(
            meta["special_case_response"],
            "Get urgent medical care now and keep the newborn warm. [GD-492]",
        )
        card_backed_answer_plan.assert_called_once_with(
            "is this normal newborn behavior or sepsis",
            results,
        )
        build_prompt.assert_not_called()

    def test_prepare_prompt_labels_generated_rag_provenance(self):
        results = {
            "documents": [["Strong retrieved guide context."]],
            "metadatas": [[{"guide_id": "GD-232"}]],
            "distances": [[0.05]],
        }
        retrieval_meta = {
            "answer_mode": "confident",
            "confidence_label": "high",
            "support_signals": {"direct": 1},
            "scenario_frame": {"question": "what should I do first"},
        }

        with patch("bench.classify_special_case", return_value=(None, None)), patch(
            "bench.build_special_case_response", return_value=None
        ), patch(
            "bench.retrieve_chunks",
            return_value=(results, ["what should I do first"], retrieval_meta),
        ), patch("bench._should_abstain", return_value=(False, [])), patch(
            "bench._card_backed_runtime_answer_plan", return_value=None
        ), patch("bench.build_prompt", return_value="Prompt with guide context."):
            _index, _question, prompt_text, _prepared_results, meta = bench.prepare_prompt(
                0,
                "what should I do first",
                collection=object(),
                top_k=8,
                category=None,
            )

        self.assertEqual(prompt_text, "Prompt with guide context.")
        self.assertEqual(meta["decision_path"], "rag")
        self.assertEqual(meta["answer_provenance"], "generated_model")
        self.assertFalse(meta["reviewed_card_backed"])
        self.assertEqual(meta["reviewed_card_ids"], [])

    def test_generation_time_summary_excludes_error_prompts_from_success_only_metrics(self):
        results_map = {
            0: ("ok prompt", "ok answer", bench.empty_results(), {"generation_time": 8.0}),
            1: (
                "failed prompt",
                "",
                bench.empty_results(),
                {"generation_time": 5.0, "error": "boom"},
            ),
            2: ("second ok", "answer", bench.empty_results(), {"generation_time": 3.5}),
        }

        summary = bench._build_generation_time_summary(results_map, total_time=16.5, prompt_count=3)
        self.assertEqual(summary["success_count"], 2)
        self.assertEqual(summary["error_count"], 1)
        self.assertEqual(summary["successful_generation_time"], 11.5)
        self.assertEqual(summary["avg_generation_time"], 5.5)
        self.assertEqual(summary["avg_generation_time_success_only"], 5.8)

    def test_prep_worker_plan_stays_single_worker_on_same_host(self):
        workers, reason = bench._prep_worker_plan(
            "http://localhost:1234/v1",
            ["http://localhost:1234/v1", "http://localhost:1234/v1"],
            use_rag=True,
        )
        self.assertEqual((workers, reason), (1, "same-host"))

    def test_prep_worker_plan_enables_two_workers_on_split_hosts(self):
        workers, reason = bench._prep_worker_plan(
            "http://localhost:1234/v1",
            ["http://192.168.0.67:1234/v1", "http://192.168.0.67:1234/v1"],
            use_rag=True,
        )
        self.assertEqual((workers, reason), (2, "split-host"))

    def test_prep_worker_plan_enables_two_workers_for_multiple_embed_hosts(self):
        workers, reason = bench._prep_worker_plan(
            ["http://192.168.0.88:1234/v1", "http://192.168.0.67:1234/v1"],
            [
                "http://192.168.0.88:1234/v1",
                "http://192.168.0.88:1234/v1",
                "http://192.168.0.67:1234/v1",
                "http://192.168.0.67:1234/v1",
            ],
            use_rag=True,
        )
        self.assertEqual((workers, reason), (2, "multi-embed-host"))

    def test_prep_embed_assignments_pin_one_worker_per_host(self):
        assignments = bench._prep_embed_assignments(
            ["http://192.168.0.88:1234/v1", "http://192.168.0.67:1234/v1"],
            2,
        )
        self.assertEqual(
            assignments,
            ["http://192.168.0.88:1234/v1", "http://192.168.0.67:1234/v1"],
        )

    def test_parse_url_list_dedupes_embedding_hosts(self):
        urls = bench._parse_url_list(
            "http://localhost:1234/v1,http://localhost:1234/v1,http://192.168.0.67:1234/v1",
            dedupe=True,
        )
        self.assertEqual(
            urls,
            ["http://localhost:1234/v1", "http://192.168.0.67:1234/v1"],
        )

    def test_parse_worker_models_broadcasts_default_model(self):
        models = bench._parse_worker_models(None, 3, "gemma-4-e4b-it@q4_k_s")
        self.assertEqual(models, ["gemma-4-e4b-it@q4_k_s"] * 3)

    def test_parse_worker_models_accepts_per_slot_models(self):
        models = bench._parse_worker_models(
            "gemma-4-e4b-it@q4_k_s,gemma-4-e4b-it",
            2,
            "ignored",
        )
        self.assertEqual(models, ["gemma-4-e4b-it@q4_k_s", "gemma-4-e4b-it"])

    def test_build_worker_targets_retains_assigned_models(self):
        workers = bench.build_worker_targets(
            ["http://192.168.0.67:1234/v1", "http://192.168.0.67:1234/v1", "http://192.168.0.203:1234/v1"],
            ["gemma-4-e4b-it@q4_k_s", "gemma-4-e4b-it@q4_k_s", "gemma-4-e4b-it"],
        )
        self.assertEqual(
            [worker["model"] for worker in workers],
            ["gemma-4-e4b-it@q4_k_s", "gemma-4-e4b-it@q4_k_s", "gemma-4-e4b-it"],
        )

    def test_next_completion_budget_rounds_to_clean_cap(self):
        self.assertEqual(bench._next_completion_budget(768, SMALL_MODEL_PROFILE), 1024)

    def test_incomplete_safety_response_detects_dangling_conditional_tail(self):
        self.assertTrue(
            bench._is_obviously_incomplete_safety_response(
                "1. Check for shock.\n2. Arrange urgent evaluation.\n3. Keep them still.\n4. If the"
            )
        )
        self.assertFalse(
            bench._is_obviously_incomplete_safety_response(
                "1. Check for shock.\n2. If the person worsens, arrange urgent evaluation."
            )
        )

    @patch("bench.generate_response")
    def test_process_prepared_prompt_escalates_after_empty_response(self, mock_generate):
        mock_generate.side_effect = [
            ("", 0.1, {"prompt_tokens": 100, "completion_tokens": 0}, None),
            ("answer", 0.2, {"prompt_tokens": 100, "completion_tokens": 180}, "stop"),
        ]

        prepared_prompt = (
            0,
            "test prompt",
            "prompt body",
            bench.empty_results(),
            {"decision_path": "no-rag"},
        )

        _, _, response, _, meta = bench.process_prepared_prompt(
            prepared_prompt,
            0.11,
            "http://localhost:1234/v1",
            gen_worker="local",
            mode="default",
            max_completion_tokens=768,
            runtime_profile=SMALL_MODEL_PROFILE,
        )

        self.assertEqual(response, "answer")
        self.assertEqual(
            mock_generate.call_args_list[0].kwargs["max_completion_tokens"],
            768,
        )
        self.assertEqual(
            mock_generate.call_args_list[1].kwargs["max_completion_tokens"],
            1024,
        )
        self.assertEqual(meta["completion_retry_count"], 1)
        self.assertEqual(meta["final_max_completion_tokens"], 1024)
        self.assertEqual(len(meta["completion_attempts"]), 2)

    @patch("bench.generate_response")
    def test_process_prepared_prompt_retries_cap_hit_with_larger_budget(self, mock_generate):
        mock_generate.side_effect = [
            ("partial answer", 0.1, {"prompt_tokens": 100, "completion_tokens": 768}, "length"),
            ("full answer", 0.2, {"prompt_tokens": 100, "completion_tokens": 640}, "stop"),
        ]

        prepared_prompt = (
            0,
            "test prompt",
            "prompt body",
            bench.empty_results(),
            {"decision_path": "no-rag"},
        )

        _, _, response, _, meta = bench.process_prepared_prompt(
            prepared_prompt,
            0.11,
            "http://localhost:1234/v1",
            gen_worker="local",
            mode="default",
            max_completion_tokens=768,
            runtime_profile=SMALL_MODEL_PROFILE,
        )

        self.assertEqual(response, "full answer")
        self.assertEqual(
            mock_generate.call_args_list[1].kwargs["max_completion_tokens"],
            1024,
        )
        self.assertFalse(meta["completion_cap_hit"])
        self.assertEqual(meta["completion_retry_count"], 1)

    @patch("bench.generate_response")
    def test_process_prepared_prompt_retries_malformed_safety_stop_response(self, mock_generate):
        mock_generate.side_effect = [
            (
                "1. Start first aid.\n2. Call emergency services.\n3. **If Unresponsive:** If",
                0.1,
                {"prompt_tokens": 100, "completion_tokens": 120},
                "stop",
            ),
            (
                "1. Start first aid.\n2. Call emergency services.\n3. If unresponsive, begin CPR.",
                0.2,
                {"prompt_tokens": 100, "completion_tokens": 160},
                "stop",
            ),
        ]

        prepared_prompt = (
            0,
            "is this choking or just panic",
            "prompt body",
            bench.empty_results(),
            {
                "decision_path": "rag",
                "retrieval_metadata": {"safety_critical": True},
            },
        )

        _, _, response, _, meta = bench.process_prepared_prompt(
            prepared_prompt,
            0.11,
            "http://localhost:1234/v1",
            gen_worker="local",
            mode="default",
            max_completion_tokens=768,
            runtime_profile=SMALL_MODEL_PROFILE,
        )

        self.assertIn("begin CPR", response)
        self.assertEqual(
            mock_generate.call_args_list[1].kwargs["max_completion_tokens"],
            1024,
        )
        self.assertFalse(meta["completion_cap_hit"])
        self.assertEqual(meta["completion_retry_count"], 1)
        self.assertTrue(meta["completion_attempts"][0]["incomplete_safety_response"])

    @patch("bench.generate_response")
    def test_process_prepared_prompt_retries_dangling_conditional_tail(self, mock_generate):
        mock_generate.side_effect = [
            (
                "1. Check for shock.\n2. Arrange urgent evaluation.\n3. Keep them still.\n4. If the",
                0.1,
                {"prompt_tokens": 100, "completion_tokens": 160},
                "stop",
            ),
            (
                "1. Check for shock.\n2. Arrange urgent evaluation.\n3. Keep them still.",
                0.2,
                {"prompt_tokens": 100, "completion_tokens": 180},
                "stop",
            ),
        ]

        prepared_prompt = (
            0,
            "vomiting after a hit to the stomach",
            "prompt body",
            bench.empty_results(),
            {
                "decision_path": "rag",
                "retrieval_metadata": {"safety_critical": True},
            },
        )

        _, _, response, _, meta = bench.process_prepared_prompt(
            prepared_prompt,
            0.11,
            "http://localhost:1234/v1",
            gen_worker="local",
            mode="default",
            max_completion_tokens=768,
            runtime_profile=SMALL_MODEL_PROFILE,
        )

        self.assertEqual(
            response,
            "1. Check for shock.\n2. Arrange urgent evaluation.\n3. Keep them still.",
        )
        self.assertEqual(
            mock_generate.call_args_list[1].kwargs["max_completion_tokens"],
            1024,
        )
        self.assertFalse(meta["completion_cap_hit"])
        self.assertEqual(meta["completion_retry_count"], 1)
        self.assertTrue(meta["completion_attempts"][0]["incomplete_safety_response"])

    @patch("bench.generate_response")
    def test_process_prepared_prompt_trims_repeated_dangling_safety_tail(self, mock_generate):
        dangling = "1. Check for shock.\n2. Arrange urgent evaluation.\n3. Keep them still.\n4. If the"
        mock_generate.side_effect = [
            (
                dangling,
                0.1,
                {"prompt_tokens": 100, "completion_tokens": 160},
                "stop",
            ),
            (
                dangling,
                0.2,
                {"prompt_tokens": 100, "completion_tokens": 160},
                "stop",
            ),
        ]

        prepared_prompt = (
            0,
            "vomiting after a hit to the stomach",
            "prompt body",
            bench.empty_results(),
            {
                "decision_path": "rag",
                "retrieval_metadata": {"safety_critical": True},
            },
        )

        _, _, response, _, meta = bench.process_prepared_prompt(
            prepared_prompt,
            0.11,
            "http://localhost:1234/v1",
            gen_worker="local",
            mode="default",
            max_completion_tokens=768,
            runtime_profile=SMALL_MODEL_PROFILE,
        )

        self.assertEqual(
            response,
            "1. Check for shock.\n2. Arrange urgent evaluation.\n3. Keep them still.",
        )
        self.assertEqual(meta["completion_retry_count"], 1)
        self.assertTrue(meta["completion_safety_trimmed"])
        self.assertTrue(meta["completion_attempts"][1]["incomplete_safety_response"])

    @patch("bench.generate_response")
    def test_process_prepared_prompt_does_not_retry_malformed_non_safety_response(self, mock_generate):
        mock_generate.return_value = (
            "1. Start with context.\n2. Compare options.\n3. **If Unsure:** If",
            0.1,
            {"prompt_tokens": 100, "completion_tokens": 120},
            "stop",
        )

        prepared_prompt = (
            0,
            "routine comparison",
            "prompt body",
            bench.empty_results(),
            {"decision_path": "rag", "retrieval_metadata": {"safety_critical": "False"}},
        )

        _, _, response, _, meta = bench.process_prepared_prompt(
            prepared_prompt,
            0.11,
            "http://localhost:1234/v1",
            gen_worker="local",
            mode="default",
            max_completion_tokens=768,
            runtime_profile=SMALL_MODEL_PROFILE,
        )

        self.assertEqual(response, "1. Start with context.\n2. Compare options.\n3. **If Unsure:** If")
        self.assertEqual(mock_generate.call_count, 1)
        self.assertEqual(meta["completion_retry_count"], 0)
        self.assertFalse(meta["completion_attempts"][0]["incomplete_safety_response"])


if __name__ == "__main__":
    unittest.main()
