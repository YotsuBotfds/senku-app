import importlib
import os
import subprocess
import sys
import unittest

import query_completion_hardening as hardening


class QueryCompletionHardeningExtractionTests(unittest.TestCase):
    def test_completion_hardening_module_import_does_not_load_query(self):
        script = (
            "import importlib, sys; "
            "sys.modules.pop('query', None); "
            "importlib.import_module('query_completion_hardening'); "
            "print('query' in sys.modules)"
        )

        completed = subprocess.run(
            [sys.executable, "-c", script],
            cwd=os.getcwd(),
            check=True,
            capture_output=True,
            text=True,
        )

        self.assertEqual(completed.stdout.strip(), "False")

    def test_numbered_step_numbers_extracts_order(self):
        self.assertEqual(
            hardening._numbered_step_numbers("1. Start\n2. Continue\n10. Finish"),
            [1, 2, 10],
        )

    def test_malformed_trailing_citation_detection(self):
        self.assertTrue(hardening._has_malformed_trailing_citation("Call now [GD-085"))
        self.assertTrue(hardening._has_malformed_trailing_citation(""))
        self.assertFalse(hardening._has_malformed_trailing_citation("Call now [GD-085]"))

    def test_incomplete_crisis_response_detection(self):
        self.assertTrue(
            hardening._is_obviously_incomplete_crisis_response(
                "1. Call for help.\n2. Stay nearby."
            )
        )
        self.assertTrue(
            hardening._is_obviously_incomplete_crisis_response(
                "1. Call for help.\n3. Stay nearby.\n4. Remove hazards."
            )
        )
        complete = (
            "1. Call emergency services now and stay with the person. [GD-101]\n"
            "2. Move hazards away and keep supervision continuous. [GD-101]\n"
            "3. Watch breathing, consciousness, and severe agitation. [GD-101]\n"
            "4. Hand off to trained help and share what changed. [GD-101]"
        )
        self.assertFalse(hardening._is_obviously_incomplete_crisis_response(complete))

    def test_build_crisis_retry_messages_shape(self):
        messages = hardening._build_crisis_retry_messages("system", "prompt")

        self.assertEqual([message["role"] for message in messages], ["system", "user"])
        self.assertIn("finish the full answer", messages[0]["content"])
        self.assertIn("exactly 4 numbered steps", messages[1]["content"])


if __name__ == "__main__":
    unittest.main()
