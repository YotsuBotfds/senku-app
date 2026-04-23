import json
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
HARNESS_SCRIPT = REPO_ROOT / "scripts" / "run_guide_prompt_validation.ps1"


class GuidePromptValidationHarnessTests(unittest.TestCase):
    def run_powershell(self, expression):
        command = f"& {{ . '{HARNESS_SCRIPT}'; {expression} }}"
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-Command",
                command,
            ],
            capture_output=True,
            text=True,
            check=True,
            cwd=REPO_ROOT,
        )
        return result.stdout.strip()

    def test_retry_delay_schedule_matches_spec_bases(self):
        first = self.run_powershell(
            "Get-LiteRtRetryDelayMilliseconds -RetryNumber 1 -FixedJitterMs 17"
        )
        second = self.run_powershell(
            "Get-LiteRtRetryDelayMilliseconds -RetryNumber 2 -FixedJitterMs 17"
        )

        self.assertEqual(first, "217")
        self.assertEqual(second, "817")

    def test_detects_litert_http_500_artifact_for_retry(self):
        artifact = {
            "results": [
                {
                    "index": 1,
                    "error": "HTTP 500",
                    "error_category": "server_error",
                    "error_status_code": 500,
                    "server": "http://127.0.0.1:1235/v1",
                    "model": "gemma-4-e2b-it-litert",
                }
            ]
        }

        with tempfile.TemporaryDirectory() as temp_root:
            json_path = Path(temp_root) / "artifact.json"
            json_path.write_text(json.dumps(artifact), encoding="utf-8")

            output = self.run_powershell(
                (
                    "$info = Get-LiteRtHostFlakeInfo "
                    f"-JsonPath '{json_path}' "
                    "-GenerationModel 'gemma-4-e2b-it-litert' "
                    "-GenerationUrl 'http://127.0.0.1:1235/v1'; "
                    "$info | ConvertTo-Json -Compress -Depth 20"
                )
            )

        info = json.loads(output)
        self.assertTrue(info["is_litert_target"])
        self.assertTrue(info["artifact_found"])
        self.assertTrue(info["should_retry"])
        self.assertEqual(info["server_error_500_count"], 1)
        self.assertEqual(info["prompt_indexes"], [1])

    def test_non_500_artifact_does_not_trigger_retry(self):
        artifact = {
            "results": [
                {
                    "index": 2,
                    "error": "opaque runtime failure",
                    "error_category": "runtime_400",
                    "error_status_code": 400,
                    "server": "http://127.0.0.1:1235/v1",
                    "model": "gemma-4-e2b-it-litert",
                }
            ]
        }

        with tempfile.TemporaryDirectory() as temp_root:
            json_path = Path(temp_root) / "artifact.json"
            json_path.write_text(json.dumps(artifact), encoding="utf-8")

            output = self.run_powershell(
                (
                    "$info = Get-LiteRtHostFlakeInfo "
                    f"-JsonPath '{json_path}' "
                    "-GenerationModel 'gemma-4-e2b-it-litert' "
                    "-GenerationUrl 'http://127.0.0.1:1235/v1'; "
                    "$info | ConvertTo-Json -Compress -Depth 20"
                )
            )

        info = json.loads(output)
        self.assertTrue(info["is_litert_target"])
        self.assertFalse(info["should_retry"])
        self.assertEqual(info["server_error_500_count"], 0)

    def test_litert_context_overflow_500_does_not_trigger_retry(self):
        artifact = {
            "results": [
                {
                    "index": 3,
                    "error": (
                        "LiteRT backend CPU failed with code 1. "
                        "Error: Input token ids are too long. "
                        "Exceeding the maximum number of tokens allowed: 4117 >= 4096"
                    ),
                    "error_category": "context_overflow",
                    "error_status_code": 500,
                    "server": "http://127.0.0.1:1235/v1",
                    "model": "gemma-4-e2b-it-litert",
                }
            ]
        }

        with tempfile.TemporaryDirectory() as temp_root:
            json_path = Path(temp_root) / "artifact.json"
            json_path.write_text(json.dumps(artifact), encoding="utf-8")

            output = self.run_powershell(
                (
                    "$info = Get-LiteRtHostFlakeInfo "
                    f"-JsonPath '{json_path}' "
                    "-GenerationModel 'gemma-4-e2b-it-litert' "
                    "-GenerationUrl 'http://127.0.0.1:1235/v1'; "
                    "$info | ConvertTo-Json -Compress -Depth 20"
                )
            )

        info = json.loads(output)
        self.assertTrue(info["is_litert_target"])
        self.assertFalse(info["should_retry"])
        self.assertEqual(info["server_error_500_count"], 0)

    def test_non_litert_500_artifact_does_not_trigger_retry(self):
        artifact = {
            "results": [
                {
                    "index": 4,
                    "error": "HTTP 500",
                    "error_category": "server_error",
                    "error_status_code": 500,
                    "server": "http://127.0.0.1:1234/v1",
                    "model": "gemma-4-e4b-it",
                }
            ]
        }

        with tempfile.TemporaryDirectory() as temp_root:
            json_path = Path(temp_root) / "artifact.json"
            json_path.write_text(json.dumps(artifact), encoding="utf-8")

            output = self.run_powershell(
                (
                    "$info = Get-LiteRtHostFlakeInfo "
                    f"-JsonPath '{json_path}' "
                    "-GenerationModel 'gemma-4-e4b-it' "
                    "-GenerationUrl 'http://127.0.0.1:1234/v1'; "
                    "$info | ConvertTo-Json -Compress -Depth 20"
                )
            )

        info = json.loads(output)
        self.assertFalse(info["is_litert_target"])
        self.assertFalse(info["should_retry"])
        self.assertEqual(info["server_error_500_count"], 1)

    def test_marker_writer_tags_json_and_markdown(self):
        artifact = {
            "summary": {
                "errors": 1,
            },
            "results": [
                {
                    "index": 3,
                    "error": "HTTP 500",
                    "error_category": "server_error",
                    "error_status_code": 500,
                    "server": "http://127.0.0.1:1235/v1",
                    "model": "gemma-4-e2b-it-litert",
                }
            ],
        }

        with tempfile.TemporaryDirectory() as temp_root:
            temp_root_path = Path(temp_root)
            json_path = temp_root_path / "artifact.json"
            md_path = temp_root_path / "artifact.md"
            json_path.write_text(json.dumps(artifact), encoding="utf-8")
            md_path.write_text("# Existing Report\n", encoding="utf-8")

            self.run_powershell(
                (
                    "$info = Get-LiteRtHostFlakeInfo "
                    f"-JsonPath '{json_path}' "
                    "-GenerationModel 'gemma-4-e2b-it-litert' "
                    "-GenerationUrl 'http://127.0.0.1:1235/v1'; "
                    "Set-LiteRtHostFlakeMarker "
                    f"-JsonPath '{json_path}' "
                    f"-MarkdownPath '{md_path}' "
                    "-FlakeInfo $info "
                    "-Wave 'w' "
                    "-AttemptCount 3 "
                    "-RetryDelaysMs @(217, 817)"
                )
            )

            updated_artifact = json.loads(json_path.read_text(encoding="utf-8"))
            updated_markdown = md_path.read_text(encoding="utf-8")

        self.assertEqual(updated_artifact["summary"]["harness_failure_tag"], "litert_host_flake")
        self.assertEqual(updated_artifact["summary"]["litert_harness_attempts"], 3)
        self.assertEqual(updated_artifact["summary"]["litert_harness_retry_count"], 2)
        self.assertEqual(updated_artifact["summary"]["litert_harness_retry_delays_ms"], [217, 817])
        self.assertEqual(updated_artifact["harness"]["guide_prompt_validation"]["tag"], "litert_host_flake")
        self.assertIn("litert_host_flake", updated_markdown)
        self.assertIn("Retry delays ms: 217, 817", updated_markdown)


if __name__ == "__main__":
    unittest.main()
