import importlib.util
import tempfile
import unittest
from pathlib import Path


def load_module():
    module_path = Path(__file__).resolve().parents[1] / "scripts" / "extract_guide_invariants.py"
    spec = importlib.util.spec_from_file_location("extract_guide_invariants", module_path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class ExtractGuideInvariantsTests(unittest.TestCase):
    def test_extracts_numeric_unit_lines_and_headings(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "sample.md").write_text(
                """---
id: GD-001
slug: sample-guide
title: Sample Guide
---

Intro text
Heat water to 75°C before use.

## Timing

Wait 5 minutes and recheck.
""",
                encoding="utf-8",
            )

            records = list(module.iter_invariants(root))

        self.assertEqual(len(records), 2)
        self.assertEqual(records[0]["section_heading"], "(intro)")
        self.assertIn("75°C", records[0]["text"])
        self.assertEqual(records[1]["section_heading"], "Timing")
        self.assertIn("5 minutes", records[1]["text"])


if __name__ == "__main__":
    unittest.main()
