import gc
import shutil
import sqlite3
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import ingest as ingest_module
from ingest import clean_chunk_text, parse_frontmatter, process_file


class IngestTests(unittest.TestCase):
    def test_parse_frontmatter_only_from_leading_fence(self):
        meta, body = parse_frontmatter(
            """Intro text
---
not frontmatter
---
Body
"""
        )

        self.assertIsNone(meta)
        self.assertIn("Intro text", body)

    def test_parse_frontmatter_accepts_leading_yaml_fence(self):
        meta, body = parse_frontmatter(
            """---
id: GD-999
slug: test-guide
---

Body text
"""
        )

        self.assertEqual(meta["id"], "GD-999")
        self.assertEqual(meta["slug"], "test-guide")
        self.assertIn("Body text", body)

    def test_clean_chunk_text_preserves_non_html_angle_bracket_text(self):
        cleaned = clean_chunk_text(
            "Use <bucket> as a placeholder, keep < 5 gallons, and <span>trim tags</span> only."
        )

        self.assertIn("<bucket>", cleaned)
        self.assertIn("< 5 gallons", cleaned)
        self.assertIn("trim tags", cleaned)
        self.assertNotIn("<span>", cleaned)

    def test_process_file_chunks_plain_markdown_guides_without_section_tags(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guide_path = Path(tmpdir) / "plain-guide.md"
            guide_path.write_text(
                """---
id: GD-900
slug: plain-guide
title: Plain Guide
category: utility
description: A guide without explicit section wrappers.
---

## First Section

This guide uses ordinary markdown headings.

## Second Section

It should still produce chunks during ingest.
""",
                encoding="utf-8",
            )

            meta, chunks = process_file(str(guide_path))

        self.assertEqual(meta["id"], "GD-900")
        self.assertGreaterEqual(len(chunks), 2)
        self.assertEqual(chunks[0]["metadata"]["section_id"], "first-section")
        self.assertEqual(chunks[0]["metadata"]["section_heading"], "First Section")
        self.assertEqual(chunks[1]["metadata"]["section_id"], "second-section")
        self.assertEqual(chunks[1]["metadata"]["section_heading"], "Second Section")

    def test_process_file_uses_single_body_chunk_when_no_sections_or_headings_exist(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guide_path = Path(tmpdir) / "body-only-guide.md"
            guide_path.write_text(
                """---
id: GD-901
slug: body-only-guide
title: Body Only Guide
category: utility
description: A guide with no explicit sections.
---

This guide has no section tags and no markdown headings,
but it still contains useful content that should be indexed.
""",
                encoding="utf-8",
            )

            meta, chunks = process_file(str(guide_path))

        self.assertEqual(meta["id"], "GD-901")
        self.assertEqual(len(chunks), 1)
        self.assertEqual(chunks[0]["metadata"]["section_id"], "body")
        self.assertEqual(chunks[0]["metadata"]["section_heading"], "")
        self.assertIn("still contains useful content", chunks[0]["text"])

    def test_fresh_rebuild_populates_poisoning_guides_in_lexical_db(self):
        repo_root = Path(__file__).resolve().parents[1]
        source_guides_dir = repo_root / "guides"
        target_files = [
            "unknown-ingestion-child-poisoning-triage.md",
            "toxicology-poisoning-response.md",
            "toxicology.md",
            "toxidromes-field-poisoning.md",
            "cold-water-survival.md",
        ]

        tmpdir = Path(tempfile.mkdtemp())
        try:
            root = tmpdir
            guides_dir = root / "guides"
            guides_dir.mkdir()
            expected_chunks_by_guide = {}

            for filename in target_files:
                src = source_guides_dir / filename
                dst = guides_dir / filename
                dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
                meta, chunks = process_file(str(dst))
                expected_chunks_by_guide[meta["id"]] = len(chunks)

            db_dir = root / "db"
            lexical_path = db_dir / "senku_lexical.sqlite3"
            manifest_path = db_dir / "ingest_manifest.json"
            report_path = db_dir / "metadata_validation_report.json"

            with (
                patch.object(ingest_module.config, "COMPENDIUM_DIR", str(guides_dir)),
                patch.object(ingest_module.config, "CHROMA_DB_DIR", str(db_dir)),
                patch.object(ingest_module.config, "LEXICAL_DB_PATH", str(lexical_path)),
                patch.object(ingest_module, "MANIFEST_PATH", str(manifest_path)),
                patch.object(ingest_module, "METADATA_VALIDATION_REPORT_PATH", str(report_path)),
                patch.object(ingest_module.requests, "get", return_value=object()),
                patch.object(
                    ingest_module,
                    "embed_batch_with_retry",
                    side_effect=lambda texts, batch_no: [[0.0, 1.0, 2.0] for _ in texts],
                ),
                patch.object(ingest_module, "print_stats", return_value=None),
                patch.object(sys, "argv", ["ingest.py", "--rebuild"]),
            ):
                ingest_module.main()

            with sqlite3.connect(lexical_path) as conn:
                total_rows = conn.execute(
                    "SELECT COUNT(*) FROM lexical_chunk_meta"
                ).fetchone()[0]
                self.assertEqual(total_rows, sum(expected_chunks_by_guide.values()))
                for guide_id, expected_rows in expected_chunks_by_guide.items():
                    with self.subTest(guide_id=guide_id):
                        actual_rows = conn.execute(
                            "SELECT COUNT(*) FROM lexical_chunk_meta WHERE guide_id = ?",
                            (guide_id,),
                        ).fetchone()[0]
                        self.assertEqual(actual_rows, expected_rows)
        finally:
            gc.collect()
            shutil.rmtree(tmpdir, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
