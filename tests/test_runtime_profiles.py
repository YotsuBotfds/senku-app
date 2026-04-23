import unittest
from unittest.mock import patch

import bench
import config
import query


class RuntimeProfileTests(unittest.TestCase):
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
