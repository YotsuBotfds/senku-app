import unittest
from unittest.mock import Mock

from scripts import check_runtime_endpoints as preflight


class RuntimeEndpointPreflightTests(unittest.TestCase):
    def test_models_url_normalizes_trailing_slash(self):
        self.assertEqual(
            preflight.models_url("http://127.0.0.1:1235/v1"),
            "http://127.0.0.1:1235/v1/models",
        )
        self.assertEqual(
            preflight.models_url("http://127.0.0.1:1235/v1/"),
            "http://127.0.0.1:1235/v1/models",
        )

    def test_parse_models_accepts_openai_compatible_payload(self):
        self.assertEqual(
            preflight.parse_models(
                {
                    "data": [
                        {"id": "gemma-4-e2b-it-litert"},
                        {"id": "nomic-ai/text-embedding-nomic-embed-text-v1.5"},
                    ]
                }
            ),
            (
                "gemma-4-e2b-it-litert",
                "nomic-ai/text-embedding-nomic-embed-text-v1.5",
            ),
        )

    def test_check_endpoint_reports_expected_model_match(self):
        response = Mock()
        response.ok = True
        response.status_code = 200
        response.json.return_value = {"data": [{"id": "gemma-4-e2b-it-litert"}]}
        session = Mock()
        session.get.return_value = response

        check = preflight.check_endpoint(
            role="generation",
            url="http://127.0.0.1:1235/v1",
            expected_model="gemma-4-e2b-it-litert",
            timeout=1.0,
            session=session,
        )

        self.assertTrue(check.ok)
        self.assertTrue(check.model_found)
        session.get.assert_called_once_with(
            "http://127.0.0.1:1235/v1/models",
            timeout=1.0,
        )

    def test_resolve_targets_prefers_explicit_args_over_registry(self):
        parser = preflight.build_arg_parser()
        args = parser.parse_args(
            [
                "--registry",
                "missing.json",
                "--gen-url",
                "http://gen/v1",
                "--gen-model",
                "gen-model",
                "--embed-url",
                "http://embed/v1",
                "--embed-model",
                "embed-model",
            ]
        )

        targets = preflight.resolve_targets(args)

        self.assertEqual(targets["generation"]["url"], "http://gen/v1")
        self.assertEqual(targets["generation"]["model"], "gen-model")
        self.assertEqual(targets["embedding"]["url"], "http://embed/v1")
        self.assertEqual(targets["embedding"]["model"], "embed-model")


if __name__ == "__main__":
    unittest.main()
