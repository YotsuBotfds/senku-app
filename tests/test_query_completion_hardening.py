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

    def test_build_generic_incomplete_retry_messages_shape(self):
        messages = hardening._build_generic_incomplete_retry_messages("system", "prompt")

        self.assertEqual([message["role"] for message in messages], ["system", "user"])
        self.assertIn("finish the full answer", messages[0]["content"])
        self.assertIn("complete final response", messages[1]["content"])
        self.assertIn("prompt", messages[1]["content"])

    def test_safety_response_helpers_detect_and_trim_dangling_tail(self):
        response = (
            "1. Check for shock.\n"
            "2. Arrange urgent evaluation.\n"
            "3. Keep them still.\n"
            "4. If the"
        )

        self.assertTrue(hardening._is_obviously_incomplete_safety_response(response))
        self.assertEqual(
            hardening._trim_incomplete_final_safety_line(response),
            ("1. Check for shock.\n2. Arrange urgent evaluation.\n3. Keep them still.", True),
        )

    def test_safety_response_helper_uses_injected_normalizer(self):
        self.assertEqual(
            hardening._trim_incomplete_final_safety_line(
                None,
                normalize_response_text_fn=lambda text: "answer",
            ),
            ("answer", False),
        )

    def test_general_response_helper_detects_dangling_citation_tail(self):
        self.assertTrue(
            hardening._is_obviously_incomplete_response(
                "Keep carts away from the tightest stall corners [GD"
            )
        )

    def test_general_response_helper_detects_mid_list_tail(self):
        self.assertTrue(
            hardening._is_obviously_incomplete_response(
                "1. Put the teacher near the entrance. [GD-653]\n"
                "2. Keep one clear lane through the room. [GD-653]\n"
                "3. Organize seating by activity"
            )
        )

    def test_general_response_helper_allows_complete_numbered_answer(self):
        self.assertFalse(
            hardening._is_obviously_incomplete_response(
                "1. Put the teacher near the entrance. [GD-653]\n"
                "2. Keep one clear lane through the room. [GD-653]\n"
                "3. Organize seating by activity and leave the exit clear. [GD-653]"
            )
        )

    def test_valid_crisis_retry_response_shape(self):
        complete = (
            "1. Call emergency services now and stay with the person right now. [GD-101]\n"
            "2. Keep a trusted adult with them continuously right now. [GD-101]\n"
            "3. Move hazards away if you can do so safely right now. [GD-101]\n"
            "4. Get urgent same-day emergency evaluation now and share what changed. [GD-101]"
        )

        self.assertTrue(hardening._is_valid_crisis_retry_response(complete))
        self.assertFalse(hardening._is_valid_crisis_retry_response(complete + "\n5. Extra."))
        self.assertFalse(
            hardening._is_valid_crisis_retry_response(
                complete.replace("[GD-101]", "GD-101", 1)
            )
        )


if __name__ == "__main__":
    unittest.main()
