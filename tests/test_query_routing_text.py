import subprocess
import sys
import unittest

import query_routing_text as routing_text


class QueryRoutingTextTests(unittest.TestCase):
    def test_import_does_not_load_query_or_bench(self):
        completed = subprocess.run(
            [
                sys.executable,
                "-B",
                "-c",
                (
                    "import sys; "
                    "import query_routing_text; "
                    "print('query' in sys.modules); "
                    "print('bench' in sys.modules)"
                ),
            ],
            check=True,
            capture_output=True,
            text=True,
        )

        self.assertEqual(completed.stdout.strip().splitlines(), ["False", "False"])

    def test_phrase_markers_match_substrings_case_insensitively(self):
        self.assertTrue(
            routing_text.text_has_marker(
                "The CHILD has a stiff neck today.",
                {"stiff neck"},
            )
        )
        self.assertTrue(
            routing_text.text_has_marker(
                "The child has a stiff neck today.",
                {"STIFF NECK"},
            )
        )

    def test_single_word_markers_require_word_boundaries(self):
        self.assertTrue(routing_text.text_has_marker("Use a clean jar.", {"jar"}))
        self.assertFalse(routing_text.text_has_marker("Use clean jars.", {"jar"}))
        self.assertTrue(routing_text.text_has_marker("Use clean jars.", {"jars"}))
        self.assertFalse(routing_text.text_has_marker("Use clean jars.", {"ar"}))
        self.assertFalse(routing_text.text_has_marker("The target is starch.", {"tar"}))

    def test_single_word_markers_casefold_before_boundary_check(self):
        self.assertTrue(routing_text.text_has_marker("Keep STRASSE clear.", {"stra\u00dfe"}))
        self.assertFalse(routing_text.text_has_marker("Keep STRASSEWAY clear.", {"stra\u00dfe"}))

    def test_empty_text_and_missing_markers_are_false(self):
        self.assertFalse(routing_text.text_has_marker("", {"water"}))
        self.assertFalse(routing_text.text_has_marker(None, {"water"}))
        self.assertFalse(routing_text.text_has_marker("filter water", set()))

    def test_malformed_marker_values_are_ignored(self):
        self.assertFalse(routing_text.text_has_marker("filter water", {None, ""}))
        self.assertTrue(routing_text.text_has_marker("filter water", {None, "WATER"}))

    def test_malformed_text_values_are_false(self):
        self.assertFalse(routing_text.text_has_marker(12345, {"12345"}))

    def test_legacy_private_alias_points_to_public_helper(self):
        self.assertIs(routing_text._text_has_marker, routing_text.text_has_marker)


if __name__ == "__main__":
    unittest.main()
