import unittest
from unittest.mock import patch

import bench


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


if __name__ == "__main__":
    unittest.main()
