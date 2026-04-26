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

    def test_render_markdown_sanitizes_table_cells_without_changing_counts(self):
        scan = {
            "summary": {
                "generated_at": "2026-04-26T12:00:00",
                "guides_scanned": 1,
                "guides_with_markers": 1,
                "marker_counts": {"html_comment": 1},
                "severity_counts": {"warn": 1},
                "guide_counts_by_marker": {"html_comment": 1},
            },
            "guides": [
                {
                    "source_file": "weird.md",
                    "guide_id": "GD|BAD",
                    "category": "cat\rbroken",
                    "liability_level": "low\nrisk",
                    "bridge": False,
                    "marker_counts": {"html_comment": 1},
                    "severity_counts": {"warn": 1},
                    "hits": [
                        {
                            "type": "html_comment",
                            "severity": "warn",
                            "line": 7,
                            "marker": "TODO | route\rbreak",
                            "example": "ignored",
                        }
                    ],
                }
            ],
        }

        markdown = render_markdown(scan)

        self.assertEqual(scan["summary"]["marker_counts"]["html_comment"], 1)
        self.assertIn("GD\\|BAD", markdown)
        self.assertIn("cat broken", markdown)
        self.assertIn("low risk", markdown)
        self.assertIn("TODO \\| route break", markdown)
        self.assertNotIn("\r", markdown)

    def test_render_markdown_falls_back_for_malformed_marker_values(self):
        scan = {
            "summary": {
                "generated_at": "2026-04-26T12:00:00",
                "guides_scanned": 1,
                "guides_with_markers": 1,
                "marker_counts": {"template_marker": 1},
                "severity_counts": {"warn": 1},
                "guide_counts_by_marker": {"template_marker": 1},
            },
            "guides": [
                {
                    "source_file": "bad-marker.md",
                    "guide_id": "GD-200",
                    "category": "utility",
                    "liability_level": "",
                    "bridge": False,
                    "marker_counts": {"template_marker": 1},
                    "severity_counts": {"warn": 1},
                    "hits": [
                        {
                            "type": "template_marker",
                            "severity": "warn",
                            "line": 4,
                            "marker": {"bad": "shape"},
                            "example": "Use {{ bad.shape }} safely.",
                        }
                    ],
                }
            ],
        }

        markdown = render_markdown(scan)

        self.assertIn("L4 template_marker Use {{ bad.shape }} safely.", markdown)
        self.assertNotIn("{'bad': 'shape'}", markdown)

    def test_render_markdown_tolerates_missing_hit_sort_fields(self):
        scan = {
            "summary": {
                "generated_at": "2026-04-26T12:00:00",
                "guides_scanned": 1,
                "guides_with_markers": 1,
                "marker_counts": {"template_marker": 1},
                "severity_counts": {"warn": 1},
                "guide_counts_by_marker": {"template_marker": 1},
            },
            "guides": [
                {
                    "source_file": "malformed-row.md",
                    "guide_id": "GD-201",
                    "category": "utility",
                    "liability_level": "",
                    "bridge": False,
                    "marker_counts": {"template_marker": 1},
                    "severity_counts": {"warn": 1},
                    "hits": [
                        {
                            "type": "template_marker",
                            "marker": "missing.sort.fields",
                            "example": "Use {{ missing.sort.fields }} safely.",
                        }
                    ],
                }
            ],
        }

        markdown = render_markdown(scan)

        self.assertIn("GD-201", markdown)
        self.assertIn("L? template_marker missing.sort.fields", markdown)


if __name__ == "__main__":
    unittest.main()
