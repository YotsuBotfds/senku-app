import json
import tempfile
import unittest
from contextlib import redirect_stdout
from io import StringIO
from pathlib import Path

from scripts.summarize_litert_transport_probe import main, render_markdown, summarize_probe


class SummarizeLiteRtTransportProbeTests(unittest.TestCase):
    def make_tmpdir(self):
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def write_results(self, artifact):
        root = self.make_tmpdir()
        path = root / "results.json"
        path.write_text(json.dumps(artifact), encoding="utf-8")
        return path

    def test_summarizes_counts_methods_payload_classes_and_adb_version(self):
        path = self.write_results(
            {
                "summary": {
                    "adb_version_line": "Android Debug Bridge version 1.0.41",
                    "adb_platform_tools_version": "36.0.0-13206524",
                },
                "results": [
                    {"method": "tmp_staging_push_cp", "payload_class": "control_ascii_256b", "verdict": "safe"},
                    {"method": "cmd_redirect_cat", "payload_class": "binary_random_64mb", "verdict": "unsafe"},
                    {"method": "process_stdin_copy_cat", "payload_class": "real_model_litert", "verdict": "unresolved"},
                ],
            }
        )

        summary = summarize_probe(path)

        self.assertEqual(summary["evidence_kind"], "preflight_non_acceptance")
        self.assertTrue(summary["non_acceptance_evidence"])
        self.assertFalse(summary["acceptance_evidence"])
        self.assertEqual(summary["safe_count"], 1)
        self.assertEqual(summary["unsafe_count"], 1)
        self.assertEqual(summary["unresolved_count"], 1)
        self.assertEqual(summary["methods"], ["cmd_redirect_cat", "process_stdin_copy_cat", "tmp_staging_push_cp"])
        self.assertEqual(
            summary["payload_classes"],
            ["binary_random_64mb", "control_ascii_256b", "real_model_litert"],
        )
        self.assertEqual(summary["adb_platform_tools_version"], "36.0.0-13206524")
        self.assertEqual(summary["blocker_labels"], [])

    def test_reads_utf8_sig_results_artifacts(self):
        root = self.make_tmpdir()
        path = root / "results.json"
        path.write_text(
            "\ufeff" + json.dumps({"results": [{"method": "tmp_staging_push_cp", "verdict": "safe"}]}),
            encoding="utf-8",
        )

        summary = summarize_probe(path)

        self.assertEqual(summary["safe_count"], 1)

    def test_labels_5554_data_partition_blocker_from_storage_confounded_artifact(self):
        path = self.write_results(
            {
                "summary": {},
                "results": [
                    {
                        "method": "tmp_staging_push_cp",
                        "serial": "emulator-5554",
                        "payload_class": "real_model_litert",
                        "verdict": "unresolved",
                        "notes": "storage-confounded failure",
                        "transport_steps": [{"stderr": "No space left on device"}],
                    }
                ],
            }
        )

        summary = summarize_probe(path)

        self.assertEqual(summary["blocker_labels"], ["5554_data_partition_blocker"])
        self.assertEqual(summary["blockers"][0]["examples"][0]["serial"], "emulator-5554")

    def test_labels_direct_stream_not_byte_safe_for_binary_hash_mismatch(self):
        path = self.write_results(
            {
                "summary": {},
                "results": [
                    {
                        "method": "cmd_type_pipe_cat",
                        "serial": "emulator-5556",
                        "payload_class": "binary_random_64mb",
                        "verdict": "unsafe",
                        "expected_bytes": 67108864,
                        "remote_bytes": 1024,
                        "hash_match": False,
                        "notes": "remote bytes/hash mismatch",
                    },
                    {
                        "method": "tmp_staging_push_cp",
                        "serial": "emulator-5556",
                        "payload_class": "binary_random_64mb",
                        "verdict": "safe",
                        "hash_match": True,
                    },
                ],
            }
        )

        summary = summarize_probe(path)

        self.assertEqual(summary["blocker_labels"], ["direct_stream_not_byte_safe"])
        self.assertEqual(summary["blockers"][0]["examples"][0]["method"], "cmd_type_pipe_cat")

    def test_does_not_label_direct_stream_for_ascii_or_unresolved_without_mismatch(self):
        path = self.write_results(
            {
                "results": [
                    {
                        "method": "cmd_redirect_cat",
                        "payload_class": "control_ascii_256b",
                        "verdict": "unsafe",
                        "hash_match": False,
                    },
                    {
                        "method": "process_stdin_copy_cat",
                        "payload_class": "real_model_litert",
                        "verdict": "unresolved",
                        "hash_match": False,
                    },
                ]
            }
        )

        summary = summarize_probe(path)

        self.assertEqual(summary["blocker_labels"], [])

    def test_does_not_label_permission_denied_as_byte_safety_or_partition_blocker(self):
        path = self.write_results(
            {
                "results": [
                    {
                        "method": "cmd_redirect_cat",
                        "serial": "emulator-5554",
                        "payload_class": "binary_random_64mb",
                        "verdict": "unsafe",
                        "expected_bytes": 67108864,
                        "remote_bytes": None,
                        "hash_match": False,
                        "transport_steps": [
                            {
                                "stderr": (
                                    "/system/bin/sh: can't create "
                                    "/data/user/0/com.senku.mobile/files/probe: Permission denied"
                                )
                            }
                        ],
                    }
                ]
            }
        )

        summary = summarize_probe(path)

        self.assertEqual(summary["blocker_labels"], [])

    def test_render_markdown_keeps_non_acceptance_posture_and_labels(self):
        summary = {
            "safe_count": 0,
            "unsafe_count": 1,
            "unresolved_count": 0,
            "methods": ["cmd_redirect_cat"],
            "payload_classes": ["real_model_litert"],
            "adb_platform_tools_version": "",
            "adb_version_line": "",
            "blocker_labels": ["direct_stream_not_byte_safe"],
            "blockers": [
                {
                    "label": "direct_stream_not_byte_safe",
                    "examples": [
                        {
                            "method": "cmd_redirect_cat",
                            "serial": "emulator-5556",
                            "payload_class": "real_model_litert",
                            "verdict": "unsafe",
                            "notes": "hash mismatch",
                        }
                    ],
                }
            ],
        }

        markdown = render_markdown(summary)

        self.assertIn("non-acceptance preflight evidence", markdown)
        self.assertIn("direct_stream_not_byte_safe", markdown)
        self.assertIn("ADB platform-tools version: (not present)", markdown)

    def test_main_writes_json_and_markdown_and_can_fail_on_blockers(self):
        root = self.make_tmpdir()
        results_path = root / "results.json"
        json_out = root / "summary.json"
        markdown_out = root / "summary.md"
        results_path.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "method": "process_stdin_copy_cat",
                            "serial": "emulator-5556",
                            "payload_class": "real_model_litert",
                            "verdict": "unsafe",
                            "expected_bytes": 9,
                            "remote_bytes": 8,
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        stdout = StringIO()

        with redirect_stdout(stdout):
            rc = main(
                [
                    str(results_path),
                    "--json-out",
                    str(json_out),
                    "--markdown-out",
                    str(markdown_out),
                    "--fail-on-blockers",
                ]
            )

        self.assertEqual(rc, 1)
        self.assertEqual(json.loads(stdout.getvalue())["blocker_labels"], ["direct_stream_not_byte_safe"])
        self.assertEqual(json.loads(json_out.read_text(encoding="utf-8"))["unsafe_count"], 1)
        self.assertIn("direct_stream_not_byte_safe", markdown_out.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
