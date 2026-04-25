import importlib
import os
import re
import subprocess
import sys
import unittest

import query_abstain_policy as abstain_policy


def _content_tokens(text):
    return {
        token
        for token in re.findall(r"[a-z0-9]+", str(text or "").lower())
        if len(token) > 2
    }


class QueryAbstainPolicyExtractionTests(unittest.TestCase):
    def test_abstain_policy_module_import_does_not_load_query(self):
        script = (
            "import importlib, sys; "
            "sys.modules.pop('query', None); "
            "importlib.import_module('query_abstain_policy'); "
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

    def test_should_abstain_uses_injected_content_tokenizer(self):
        results = {
            "documents": [["Canvas patching only.", "Tool rust cleanup."]],
            "metadatas": [[
                {
                    "guide_title": "Canvas Repair",
                    "section_heading": "Patching",
                    "category": "crafts",
                    "_vector_hits": 1,
                    "_lexical_hits": 0,
                },
                {
                    "guide_title": "Rust Cleanup",
                    "section_heading": "Tools",
                    "category": "salvage",
                    "_vector_hits": 0,
                    "_lexical_hits": 0,
                },
            ]],
            "distances": [[0.82, 0.95]],
        }

        should_abstain, labels = abstain_policy._should_abstain(
            results,
            "build rain shelter from tarp",
            content_tokens=_content_tokens,
        )

        self.assertTrue(should_abstain)
        self.assertEqual(labels, ["off-topic candidate", "off-topic candidate"])

    def test_truncate_abstain_query_is_stable(self):
        self.assertEqual(
            abstain_policy._truncate_abstain_query("  a   b   c  ", limit=10),
            "a b c",
        )
        self.assertEqual(
            abstain_policy._truncate_abstain_query("abcdefghijkl", limit=8),
            "abcde...",
        )


if __name__ == "__main__":
    unittest.main()
