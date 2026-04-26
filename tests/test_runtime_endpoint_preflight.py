import io
import json
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path
from unittest.mock import Mock, patch

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

    def test_check_endpoint_reports_invalid_models_json_as_failed_check(self):
        response = Mock()
        response.ok = True
        response.status_code = 200
        response.json.side_effect = ValueError("not json")
        session = Mock()
        session.get.return_value = response

        check = preflight.check_endpoint(
            role="generation",
            url="http://127.0.0.1:1235/v1",
            expected_model="gemma-4-e2b-it-litert",
            timeout=1.0,
            session=session,
        )

        self.assertFalse(check.ok)
        self.assertFalse(check.model_found)
        self.assertEqual(check.status_code, 200)
        self.assertIn("invalid /models JSON", check.error)

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

    def test_main_fails_loud_unless_warn_only_and_still_writes_json(self):
        failed_check = preflight.EndpointCheck(
            role="generation",
            url="http://127.0.0.1:1235/v1",
            expected_model="gemma-4-e2b-it-litert",
            ok=True,
            status_code=200,
            models=("different-model",),
        )

        with tempfile.TemporaryDirectory() as tmpdir:
            json_out = Path(tmpdir) / "runtime_preflight.json"
            with patch.object(preflight, "check_targets", return_value=[failed_check]):
                with redirect_stdout(io.StringIO()):
                    rc = preflight.main(["--json-out", str(json_out)])
                    warn_rc = preflight.main(["--json-out", str(json_out), "--warn-only"])

            payload = json.loads(json_out.read_text(encoding="utf-8"))

        self.assertEqual(rc, 1)
        self.assertEqual(warn_rc, 0)
        self.assertFalse(payload["checks"][0]["model_found"])
        self.assertEqual(payload["checks"][0]["models"], ["different-model"])


if __name__ == "__main__":
    unittest.main()
