import json
import tempfile
import unittest
from pathlib import Path

from scripts.scan_corpus_markers import render_markdown, scan_guides


class ScanCorpusMarkersTests(unittest.TestCase):
    def test_scan_reports_partials_comments_routing_and_bridge_metadata(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides_dir = root / "guides"
            guides_dir.mkdir()
            (guides_dir / "risk-guide.md").write_text(
                """---
id: GD-100
slug: risk-guide
title: Risk Guide
category: health
liability_level: high
bridge: true
---

<!-- TODO replace this routing note -->
Quick routing: use this guide when the user asks about triage.
Keep {{> missing-first-aid-partial }} out of indexed text.
Also flag {{ calculator.value }} but do not fail it.
""",
                encoding="utf-8",
            )
            (guides_dir / "clean-guide.md").write_text(
                """---
id: GD-101
slug: clean-guide
title: Clean Guide
category: utility
---

## Steps

Use ordinary text.
""",
                encoding="utf-8",
            )

            scan = scan_guides(guides_dir)
            markdown = render_markdown(scan)
            payload = json.loads(json.dumps(scan))

        self.assertEqual(payload["summary"]["guides_scanned"], 2)
        self.assertEqual(payload["summary"]["guides_with_markers"], 1)
        self.assertEqual(payload["summary"]["marker_counts"]["unresolved_partial"], 1)
        self.assertEqual(payload["summary"]["marker_counts"]["template_marker"], 1)
        self.assertEqual(payload["summary"]["marker_counts"]["bridge_frontmatter"], 1)
        self.assertGreaterEqual(payload["summary"]["marker_counts"]["routing_phrase"], 1)
        self.assertEqual(payload["guides"][0]["guide_id"], "GD-100")
        self.assertTrue(payload["guides"][0]["bridge"])
        self.assertEqual(payload["guides"][0]["liability_level"], "high")
        self.assertIn("missing-first-aid-partial", markdown)
        self.assertIn("GD-100", markdown)

    def test_guides_without_frontmatter_are_skipped(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir) / "guides"
            guides_dir.mkdir()
            (guides_dir / "draft.md").write_text("{{> draft }}\n", encoding="utf-8")

            scan = scan_guides(guides_dir)

        self.assertEqual(scan["summary"]["guides_scanned"], 0)
        self.assertEqual(scan["summary"]["guides_with_markers"], 0)


if __name__ == "__main__":
    unittest.main()
