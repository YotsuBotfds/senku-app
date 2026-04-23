import unittest
from unittest.mock import Mock

import requests

from lmstudio_utils import (
    classify_lm_request_error,
    embedding_models_to_try,
    is_retryable_lm_request,
    normalize_lm_studio_url,
    should_try_embedding_fallback,
)


class LMStudioUtilsTests(unittest.TestCase):
    def test_embedding_model_aliases_are_symmetric(self):
        self.assertEqual(
            embedding_models_to_try("nomic-ai/text-embedding-nomic-embed-text-v1.5"),
            [
                "nomic-ai/text-embedding-nomic-embed-text-v1.5",
                "stratalab-org/text-embedding-nomic-embed-text-v1.5",
            ],
        )
        self.assertEqual(
            embedding_models_to_try("stratalab-org/text-embedding-nomic-embed-text-v1.5"),
            [
                "stratalab-org/text-embedding-nomic-embed-text-v1.5",
                "nomic-ai/text-embedding-nomic-embed-text-v1.5",
            ],
        )

    def test_url_normalization_strips_trailing_slash(self):
        self.assertEqual(
            normalize_lm_studio_url("http://localhost:1234/v1/"),
            "http://localhost:1234/v1",
        )

    def test_embedding_fallback_only_on_known_model_load_400s(self):
        response = Mock(status_code=400, text="failed to load model")
        err = requests.HTTPError(response=response)
        self.assertTrue(should_try_embedding_fallback(err))

        response = Mock(status_code=400, text="unknown model")
        err = requests.HTTPError(response=response)
        self.assertTrue(should_try_embedding_fallback(err))

        response = Mock(status_code=400, text="some other bad request")
        err = requests.HTTPError(response=response)
        self.assertFalse(should_try_embedding_fallback(err))

    def test_retryable_request_detection(self):
        self.assertTrue(is_retryable_lm_request(requests.Timeout()))
        self.assertTrue(is_retryable_lm_request(requests.ConnectionError()))
        self.assertTrue(
            is_retryable_lm_request(
                requests.HTTPError(response=Mock(status_code=503, text="busy"))
            )
        )
        self.assertFalse(
            is_retryable_lm_request(
                requests.HTTPError(response=Mock(status_code=404, text="not found"))
            )
        )

    def test_context_pressure_400_is_classified_non_retryable(self):
        error = requests.HTTPError(
            response=Mock(status_code=400, text="request exceeds context length")
        )
        info = classify_lm_request_error(error)
        self.assertEqual(info["category"], "context_pressure")
        self.assertFalse(info["retryable"])

    def test_litert_context_overflow_500_is_classified_non_retryable(self):
        error = requests.HTTPError(
            response=Mock(
                status_code=500,
                text=(
                    "LiteRT backend CPU failed with code 1. "
                    "Error: Input token ids are too long. "
                    "Exceeding the maximum number of tokens allowed: 4117 >= 4096"
                ),
            )
        )
        info = classify_lm_request_error(error)
        self.assertEqual(info["category"], "context_overflow")
        self.assertFalse(info["retryable"])

    def test_generic_400_is_classified_non_retryable_runtime_failure(self):
        error = requests.HTTPError(
            response=Mock(status_code=400, text="opaque runtime failure")
        )
        info = classify_lm_request_error(error)
        self.assertEqual(info["category"], "runtime_400")
        self.assertFalse(info["retryable"])

    def test_generic_request_exception_is_not_treated_as_retryable(self):
        error = requests.RequestException("invalid adapter")
        info = classify_lm_request_error(error)
        self.assertEqual(info["category"], "request_error")
        self.assertFalse(info["retryable"])


if __name__ == "__main__":
    unittest.main()
