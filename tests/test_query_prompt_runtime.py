import importlib
import sys
import unittest
from types import SimpleNamespace


class QueryPromptRuntimeTests(unittest.TestCase):
    def test_import_does_not_import_query_or_bench(self):
        for module_name in ("query_prompt_runtime", "query", "bench"):
            sys.modules.pop(module_name, None)

        importlib.import_module("query_prompt_runtime")

        self.assertNotIn("query", sys.modules)
        self.assertNotIn("bench", sys.modules)

    def test_system_prompt_text_uses_mode_builder_when_present(self):
        import query_prompt_runtime

        config_module = SimpleNamespace(
            SYSTEM_PROMPT="fallback",
            build_system_prompt=lambda mode: f"system for {mode}",
        )

        self.assertEqual(
            query_prompt_runtime.system_prompt_text(config_module, "beam"),
            "system for beam",
        )

    def test_system_prompt_text_falls_back_to_system_prompt(self):
        import query_prompt_runtime

        config_module = SimpleNamespace(SYSTEM_PROMPT="static system prompt")

        self.assertEqual(
            query_prompt_runtime.system_prompt_text(config_module, "ignored"),
            "static system prompt",
        )

    def test_prompt_token_limit_from_runtime_profile_uses_provided_profile(self):
        import query_prompt_runtime

        def unexpected_getter(gen_model, gen_url):
            raise AssertionError("runtime_profile_getter should not be called")

        self.assertEqual(
            query_prompt_runtime.prompt_token_limit_from_runtime_profile(
                "model",
                "http://localhost:1234/v1",
                runtime_profile={"name": "provided", "prompt_token_limit": "4096"},
                runtime_profile_getter=unexpected_getter,
            ),
            4096,
        )

    def test_prompt_token_limit_from_runtime_profile_requires_limit(self):
        import query_prompt_runtime

        with self.assertRaisesRegex(ValueError, "missing required prompt_token_limit"):
            query_prompt_runtime.prompt_token_limit_from_runtime_profile(
                runtime_profile={"name": "broken"},
                runtime_profile_getter=lambda gen_model, gen_url: {
                    "prompt_token_limit": 2048
                },
            )

    def test_estimate_chat_prompt_tokens_counts_system_prompt_with_overhead(self):
        import query_prompt_runtime

        counted_texts = []

        def count_tokens(text):
            counted_texts.append(text)
            return len(text.split())

        def resolve_system_prompt(mode):
            self.assertEqual(mode, "careful")
            return "system words here"

        estimate = query_prompt_runtime.estimate_chat_prompt_tokens(
            "user prompt",
            estimate_tokens_fn=count_tokens,
            system_prompt_resolver=resolve_system_prompt,
            mode="careful",
        )

        self.assertEqual(counted_texts, ["user prompt", "system words here"])
        self.assertEqual(
            estimate,
            {
                "prompt_text_tokens": 2,
                "system_prompt_tokens": 3,
                "estimated_prompt_tokens": 2 + 3 + 24 + 24,
            },
        )

    def test_estimate_chat_prompt_tokens_can_skip_system_prompt(self):
        import query_prompt_runtime

        counted_texts = []

        def count_tokens(text):
            counted_texts.append(text)
            return len(text.split())

        def unexpected_resolver(mode):
            raise AssertionError("system_prompt_resolver should not be called")

        estimate = query_prompt_runtime.estimate_chat_prompt_tokens(
            "user prompt only",
            estimate_tokens_fn=count_tokens,
            system_prompt_resolver=unexpected_resolver,
            use_system_prompt=False,
        )

        self.assertEqual(counted_texts, ["user prompt only"])
        self.assertEqual(
            estimate,
            {
                "prompt_text_tokens": 3,
                "system_prompt_tokens": 0,
                "estimated_prompt_tokens": 3 + 24,
            },
        )

    def test_estimate_chat_prompt_tokens_uses_injected_system_prompt_text(self):
        import query_prompt_runtime

        counted_texts = []

        def count_tokens(text):
            counted_texts.append(text)
            return len(text.split())

        def unexpected_resolver(mode):
            raise AssertionError("system_prompt_resolver should not be called")

        estimate = query_prompt_runtime.estimate_chat_prompt_tokens(
            "prompt",
            estimate_tokens_fn=count_tokens,
            system_prompt_resolver=unexpected_resolver,
            system_prompt_text="injected system",
        )

        self.assertEqual(counted_texts, ["prompt", "injected system"])
        self.assertEqual(estimate["estimated_prompt_tokens"], 1 + 2 + 24 + 24)


if __name__ == "__main__":
    unittest.main()
