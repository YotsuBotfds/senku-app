import importlib
import os
import subprocess
import sys
import unittest
from unittest.mock import patch

import query_response_normalization as normalization


class QueryResponseNormalizationExtractionTests(unittest.TestCase):
    def test_response_normalization_module_import_does_not_load_query(self):
        script = (
            "import importlib, sys; "
            "sys.modules.pop('query', None); "
            "importlib.import_module('query_response_normalization'); "
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

    def test_normalize_response_text_uses_injected_catalog_and_warning_hook(self):
        warnings = []

        normalized = normalization.normalize_response_text(
            "Use clean jars [GD-001, GD-999]. Then seal them [GD-999].",
            valid_guide_ids_provider=lambda: {"GD-001"},
            warn_event=lambda event_name, **fields: warnings.append((event_name, fields)),
        )

        self.assertEqual(normalized, "Use clean jars. Then seal them. [GD-001]")
        self.assertEqual(
            warnings,
            [("citation_hallucination", {"guide_id": "GD-999"})],
        )

    def test_citation_compression_keeps_step_lines_to_one_source(self):
        normalized = normalization.normalize_response_text(
            "1. Boil first [GD-001] and cool [GD-002] before storage [GD-003].",
            valid_guide_ids_provider=lambda: {"GD-001", "GD-002", "GD-003"},
        )

        self.assertEqual(normalized, "1. Boil first and cool before storage. [GD-001]")

    def test_terminal_unclosed_citation_group_is_recovered(self):
        normalized = normalization.normalize_response_text(
            "Use clean jars [GD-1, GD/2",
            valid_guide_ids_provider=lambda: {"GD-001", "GD-002"},
        )

        self.assertEqual(normalized, "Use clean jars [GD-001, GD-002]")

    def test_query_wrapper_keeps_query_logger_warning_behavior(self):
        query = importlib.import_module("query")

        with (
            patch.object(query, "all_guide_ids", return_value={"GD-001"}),
            self.assertLogs("query", level="WARNING") as captured,
        ):
            normalized = query.normalize_response_text("Use clean jars [GD-001, GD-999].")

        self.assertEqual(normalized, "Use clean jars [GD-001].")
        self.assertTrue(
            any("citation_hallucination" in entry for entry in captured.output)
        )

    def test_query_private_duplicate_count_wrapper_remains_available(self):
        query = importlib.import_module("query")

        self.assertEqual(query._duplicate_citation_count("A [GD-001] B [GD-001]"), 1)


if __name__ == "__main__":
    unittest.main()
