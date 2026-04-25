import importlib
import os
import unittest
from unittest.mock import patch

import bench
import config
import query


class RuntimeProfileTests(unittest.TestCase):
    def test_default_runtime_targets_litert_generation_and_embedding_alias(self):
        self.assertEqual(config.DEFAULT_GEN_URL, "http://127.0.0.1:1235/v1")
        self.assertEqual(config.DEFAULT_EMBED_URL, "http://127.0.0.1:1234/v1")
        self.assertEqual(config.LITERT_GEN_MODEL, "gemma-4-e2b-it-litert")
        self.assertEqual(config.BROAD_QUALITY_GEN_MODEL, "google/gemma-4-26b-a4b")
        self.assertEqual(
            config.GEN_URL,
            os.environ.get("SENKU_GEN_URL", config.DEFAULT_GEN_URL),
        )
        self.assertEqual(
            config.EMBED_URL,
            os.environ.get("SENKU_EMBED_URL", config.DEFAULT_EMBED_URL),
        )
        self.assertEqual(config.LM_STUDIO_URL, config.EMBED_URL)
        self.assertEqual(
            config.GEN_MODEL,
            os.environ.get("SENKU_GEN_MODEL", config.LITERT_GEN_MODEL),
        )

    def test_runtime_env_overrides_are_honored(self):
        with patch.dict(
            os.environ,
            {
                "SENKU_GEN_URL": "http://127.0.0.1:7777/v1",
                "SENKU_EMBED_URL": "http://127.0.0.1:8888/v1",
                "SENKU_GEN_MODEL": "test-gen-model",
                "SENKU_EMBED_MODEL": "test-embed-model",
            },
        ):
            importlib.reload(config)
            self.assertEqual(config.GEN_URL, "http://127.0.0.1:7777/v1")
            self.assertEqual(config.EMBED_URL, "http://127.0.0.1:8888/v1")
            self.assertEqual(config.LM_STUDIO_URL, "http://127.0.0.1:8888/v1")
            self.assertEqual(config.GEN_MODEL, "test-gen-model")
            self.assertEqual(config.EMBED_MODEL, "test-embed-model")

        importlib.reload(config)

    def test_every_runtime_profile_declares_prompt_token_limit(self):
        for profile_name, profile in config.RUNTIME_PROFILES.items():
            with self.subTest(profile=profile_name):
                self.assertIsNotNone(profile.get("prompt_token_limit"))
                self.assertGreater(int(profile["prompt_token_limit"]), 0)

    def test_litert_runtime_uses_litert_top_k(self):
        self.assertEqual(
            config.get_runtime_top_k(
                "gemma-4-e2b-it-litert",
                "http://127.0.0.1:1235/v1",
            ),
            config.TOP_K_LITERT,
        )

    def test_query_embeddings_default_to_embedding_url(self):
        class Response:
            def raise_for_status(self):
                return None

            def close(self):
                return None

            def json(self):
                return {"data": [{"index": 0, "embedding": [0.1, 0.2]}]}

        with patch.object(
            query.config, "EMBED_URL", "http://127.0.0.1:8888/v1"
        ), patch.object(
            query.config, "LM_STUDIO_URL", "http://127.0.0.1:1234/v1"
        ), patch.object(query.requests, "post", return_value=Response()) as post:
            self.assertEqual(query.embed_batch(["test"]), [[0.1, 0.2]])

        self.assertEqual(
            post.call_args.args[0],
            "http://127.0.0.1:8888/v1/embeddings",
        )

    def test_stream_response_posts_to_generation_url(self):
        class StreamResponse:
            def iter_lines(self, decode_unicode=True):
                yield 'data: {"choices":[{"delta":{"content":"Grounded answer"}}]}'
                yield "data: [DONE]"

        results = {
            "documents": [["Retrieved evidence."]],
            "metadatas": [[
                {
                    "guide_id": "GD-001",
                    "guide_title": "Guide",
                    "section_heading": "Section",
                }
            ]],
            "distances": [[0.1]],
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
            query.config, "GEN_URL", "http://127.0.0.1:7777/v1"
        ), patch.object(
            query.config, "LM_STUDIO_URL", "http://127.0.0.1:1234/v1"
        ), patch.object(
            query, "_should_abstain", return_value=(False, [])
        ), patch.object(
            query, "_card_backed_runtime_answer", return_value=None
        ), patch.object(
            query, "build_prompt", return_value="Prompt"
        ), patch.object(
            query,
            "_estimate_chat_prompt_tokens",
            return_value={"estimated_prompt_tokens": 10},
        ), patch.object(
            query, "post_json_with_retry", return_value=StreamResponse()
        ) as post, patch.object(query.console, "print"):
            response = query.stream_response("test question", results)

        self.assertEqual(response, "Grounded answer")
        self.assertEqual(
            post.call_args.args[0],
            "http://127.0.0.1:7777/v1/chat/completions",
        )

    def test_prompt_token_limit_parity_between_query_and_bench(self):
        cases = (
            ("google/gemma-4-26b-a4b", "http://localhost:1234/v1"),
            ("gemma-4-e2b-it-litert", "http://127.0.0.1:1235/v1"),
            ("qwen3-27b", "http://localhost:1234/v1"),
        )
        for model_name, gen_url in cases:
            with self.subTest(model=model_name, url=gen_url):
                runtime_profile = config.get_runtime_profile(model_name, gen_url)
                self.assertEqual(
                    query._prompt_token_limit(model_name, gen_url),
                    bench._prompt_token_limit(
                        model_name,
                        gen_url,
                        runtime_profile=runtime_profile,
                    ),
                )

    def test_bench_prompt_token_limit_is_loud_when_profile_is_missing_limit(self):
        with self.assertRaises(ValueError):
            bench._prompt_token_limit(
                "broken-model",
                "http://localhost:1234/v1",
                runtime_profile={"name": "broken"},
            )

    def test_query_prompt_token_limit_is_loud_when_profile_is_missing_limit(self):
        with patch.object(
            query.config,
            "get_runtime_prompt_token_limit",
            side_effect=ValueError("missing required prompt_token_limit"),
        ):
            with self.assertRaises(ValueError):
                query._prompt_token_limit(
                    "broken-model",
                    "http://localhost:1234/v1",
                )


if __name__ == "__main__":
    unittest.main()
