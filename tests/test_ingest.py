import gc
import json
import os
import shutil
import sqlite3
import sys
import tempfile
import unittest
from contextlib import closing
from pathlib import Path
from unittest.mock import patch

import ingest as ingest_module
from ingest import (
    build_contextual_retrieval_text,
    clean_chunk_text,
    collect_manifest_file_info,
    parse_frontmatter,
    process_file,
    resolve_embedding_batch_size,
)


class IngestTests(unittest.TestCase):
    def test_resolve_embedding_batch_size_uses_env_override(self):
        with patch.dict(os.environ, {"SENKU_INGEST_EMBED_BATCH_SIZE": "8"}):
            self.assertEqual(resolve_embedding_batch_size(), 8)

    def test_resolve_embedding_batch_size_rejects_non_positive_values(self):
        with self.assertRaises(ValueError):
            resolve_embedding_batch_size(0)

    def test_collect_manifest_file_info_reads_frontmatter_without_chunks(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guide_path = Path(tmpdir) / "manifest-guide.md"
            guide_path.write_text(
                """---
id: GD-910
slug: manifest-guide
title: Manifest Guide
category: utility
description: Manifest metadata only.
---

## Body

Content.
""",
                encoding="utf-8",
            )

            info = collect_manifest_file_info([str(guide_path)])

        self.assertEqual(info["GD-910"]["basename"], "manifest-guide.md")
        self.assertEqual(len(info["GD-910"]["sha256"]), 64)

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

    def test_process_file_preserves_routing_frontmatter_in_metadata(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guide_path = Path(tmpdir) / "routing-guide.md"
            guide_path.write_text(
                """---
id: GD-904
slug: routing-guide
title: Routing Guide
category: utility
description: A guide with routing metadata.
aliases:
  - cloudy water after flood
  - scald burn first aid
routing_cues:
  - settle filter disinfect store
  - cool water first
applicability:
  - Use for post-flood water treatment order.
  - Use for hot water burns before blister care.
---

## First Section

This guide has frontmatter routing language.
""",
                encoding="utf-8",
            )

            _meta, chunks = process_file(str(guide_path))

        metadata = chunks[0]["metadata"]
        self.assertIn("cloudy water after flood", metadata["aliases"])
        self.assertIn("settle filter disinfect store", metadata["routing_cues"])
        self.assertIn("post-flood water treatment order", metadata["applicability"])

    def test_build_contextual_retrieval_text_preserves_chunk_with_context(self):
        chunk = {
            "text": "Keep this exact operational step.",
            "metadata": {
                "guide_id": "GD-777",
                "guide_title": "Context Guide",
                "slug": "context-guide",
                "description": "Purpose text",
                "category": "utility",
                "tags": "water,storage",
                "section_heading": "Storage Steps",
                "liability_level": "medium",
                "related": "GD-778",
                "aliases": "cloudy water after flood",
                "routing_cues": "settle filter disinfect store",
                "applicability": "Use for post-flood water treatment order.",
            },
        }

        text = build_contextual_retrieval_text(chunk)

        self.assertIn("Guide: Context Guide | GD-777 | context-guide", text)
        self.assertIn("Purpose: Purpose text", text)
        self.assertIn("Section: Storage Steps", text)
        self.assertIn("Category/tags: utility, water,storage", text)
        self.assertIn("Liability: medium", text)
        self.assertIn("Related: GD-778", text)
        self.assertIn("Aliases: cloudy water after flood", text)
        self.assertIn("Routing cues: settle filter disinfect store", text)
        self.assertIn("Applicability: Use for post-flood water treatment order.", text)
        self.assertTrue(text.endswith("Keep this exact operational step."))

    def test_contextual_shadow_only_writes_jsonl_without_chroma_or_embeddings(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides_dir = root / "guides"
            guides_dir.mkdir()
            guide_path = guides_dir / "shadow-guide.md"
            guide_path.write_text(
                """---
id: GD-902
slug: shadow-guide
title: Shadow Guide
category: utility
description: A guide for contextual shadow export.
tags: [shadow, test]
---

## First Section

This raw chunk should stay raw.
""",
                encoding="utf-8",
            )
            _, chunks = process_file(str(guide_path))
            output_path = root / "contextual-shadow.jsonl"

            with (
                patch.object(ingest_module.config, "COMPENDIUM_DIR", str(guides_dir)),
                patch.object(
                    ingest_module.chromadb,
                    "PersistentClient",
                    side_effect=AssertionError("Chroma should not be opened"),
                ),
                patch.object(
                    ingest_module,
                    "embed_batch_with_retry",
                    side_effect=AssertionError("Embeddings should not run"),
                ),
                patch.object(
                    ingest_module.requests,
                    "get",
                    side_effect=AssertionError("Endpoint check should not run"),
                ),
                patch.object(
                    sys,
                    "argv",
                    [
                        "ingest.py",
                        "--contextual-shadow-jsonl",
                        str(output_path),
                        "--contextual-shadow-only",
                    ],
                ),
            ):
                ingest_module.main()

            records = [
                json.loads(line)
                for line in output_path.read_text(encoding="utf-8").splitlines()
            ]

        self.assertEqual(len(records), len(chunks))
        self.assertEqual(records[0]["document"], chunks[0]["text"])
        self.assertEqual(records[0]["metadata"]["guide_id"], "GD-902")
        self.assertIn(
            "Guide: Shadow Guide | GD-902 | shadow-guide",
            records[0]["contextual_retrieval_text"],
        )
        self.assertIn(
            "This raw chunk should stay raw.",
            records[0]["contextual_retrieval_text"],
        )

    def test_contextual_shadow_only_removes_stale_jsonl_on_parse_error(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides_dir = root / "guides"
            guides_dir.mkdir()
            guide_path = guides_dir / "bad-guide.md"
            guide_path.write_text("This guide has no frontmatter.", encoding="utf-8")
            output_path = root / "contextual-shadow.jsonl"
            output_path.write_text('{"stale": true}\n', encoding="utf-8")

            with (
                patch.object(ingest_module.config, "COMPENDIUM_DIR", str(guides_dir)),
                patch.object(
                    ingest_module.chromadb,
                    "PersistentClient",
                    side_effect=AssertionError("Chroma should not be opened"),
                ),
                patch.object(
                    ingest_module.requests,
                    "get",
                    side_effect=AssertionError("Endpoint check should not run"),
                ),
                patch.object(
                    sys,
                    "argv",
                    [
                        "ingest.py",
                        "--contextual-shadow-jsonl",
                        str(output_path),
                        "--contextual-shadow-only",
                    ],
                ),
            ):
                with self.assertRaises(SystemExit):
                    ingest_module.main()

            self.assertFalse(output_path.exists())

    def test_contextual_shadow_ids_match_fallback_embedded_chunks(self):
        tmpdir = Path(tempfile.mkdtemp())
        try:
            root = tmpdir
            guides_dir = root / "guides"
            guides_dir.mkdir()
            guide_path = guides_dir / "fallback-guide.md"
            guide_path.write_text(
                """---
id: GD-903
slug: fallback-guide
title: Fallback Guide
category: utility
description: A guide for fallback chunk id alignment.
---

## First Section

First fallback chunk should fail embedding.

## Second Section

Second fallback chunk should still align to its shadow record.
""",
                encoding="utf-8",
            )
            db_dir = root / "db"
            lexical_path = db_dir / "senku_lexical.sqlite3"
            manifest_path = db_dir / "ingest_manifest.json"
            report_path = db_dir / "metadata_validation_report.json"
            shadow_path = db_dir / "contextual-shadow.jsonl"

            def embed_side_effect(texts, batch_no):
                if len(texts) > 1:
                    raise RuntimeError("batch failed")
                if "First fallback chunk" in texts[0]:
                    raise RuntimeError("first chunk failed")
                return [[0.0, 1.0, 2.0]]

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
                    side_effect=embed_side_effect,
                ),
                patch.object(ingest_module, "print_stats", return_value=None),
                patch.object(
                    sys,
                    "argv",
                    [
                        "ingest.py",
                        "--rebuild",
                        "--contextual-shadow-jsonl",
                        str(shadow_path),
                    ],
                ),
            ):
                ingest_module.main()

            shadow_records = [
                json.loads(line)
                for line in shadow_path.read_text(encoding="utf-8").splitlines()
            ]
            self.assertEqual(len(shadow_records), 2)
            with closing(sqlite3.connect(lexical_path)) as conn:
                rows = conn.execute(
                    "SELECT chunk_id, document FROM lexical_chunk_meta"
                ).fetchall()

            self.assertEqual(len(rows), 1)
            self.assertEqual(rows[0][0], shadow_records[1]["chunk_id"])
            self.assertEqual(rows[0][1], shadow_records[1]["document"])
        finally:
            gc.collect()
            shutil.rmtree(tmpdir, ignore_errors=True)

    def test_incremental_ingest_preserves_unselected_manifest_entries(self):
        tmpdir = Path(tempfile.mkdtemp())
        try:
            root = tmpdir
            guides_dir = root / "guides"
            guides_dir.mkdir()
            first = guides_dir / "first-guide.md"
            second = guides_dir / "second-guide.md"
            first.write_text(
                """---
id: GD-904
slug: first-guide
title: First Guide
category: utility
description: First test guide.
---

## First

First guide content.
""",
                encoding="utf-8",
            )
            second.write_text(
                """---
id: GD-905
slug: second-guide
title: Second Guide
category: utility
description: Second test guide.
---

## Second

Second guide content.
""",
                encoding="utf-8",
            )
            db_dir = root / "db"
            lexical_path = db_dir / "senku_lexical.sqlite3"
            manifest_path = db_dir / "ingest_manifest.json"
            report_path = db_dir / "metadata_validation_report.json"

            common_patches = (
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
            )

            with (
                common_patches[0],
                common_patches[1],
                common_patches[2],
                common_patches[3],
                common_patches[4],
                common_patches[5],
                common_patches[6],
                common_patches[7],
                patch.object(sys, "argv", ["ingest.py", "--rebuild"]),
            ):
                ingest_module.main()

            first.write_text(first.read_text(encoding="utf-8") + "\nExtra line.\n", encoding="utf-8")

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
                patch.object(sys, "argv", ["ingest.py", "--files", "first-guide.md"]),
            ):
                ingest_module.main()

            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            self.assertIn("GD-904", manifest)
            self.assertIn("GD-905", manifest)
            self.assertEqual(manifest["GD-905"]["source_file"], "second-guide.md")
        finally:
            gc.collect()
            shutil.rmtree(tmpdir, ignore_errors=True)

    def test_rebuild_with_files_builds_scoped_index(self):
        tmpdir = Path(tempfile.mkdtemp())
        try:
            root = tmpdir
            guides_dir = root / "guides"
            guides_dir.mkdir()
            first = guides_dir / "first-guide.md"
            second = guides_dir / "second-guide.md"
            first.write_text(
                """---
id: GD-906
slug: first-guide
title: First Guide
category: utility
description: First test guide.
---

## First

First guide content.
""",
                encoding="utf-8",
            )
            second.write_text(
                """---
id: GD-907
slug: second-guide
title: Second Guide
category: utility
description: Second test guide.
---

## Second

Second guide content.
""",
                encoding="utf-8",
            )
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
                patch.object(sys, "argv", ["ingest.py", "--rebuild", "--files", "first-guide.md"]),
            ):
                ingest_module.main()

            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            self.assertEqual(set(manifest), {"GD-906"})
            with closing(sqlite3.connect(lexical_path)) as conn:
                guide_ids = {
                    row[0]
                    for row in conn.execute(
                        "SELECT DISTINCT guide_id FROM lexical_chunk_meta"
                    ).fetchall()
                }
            self.assertEqual(guide_ids, {"GD-906"})
        finally:
            gc.collect()
            shutil.rmtree(tmpdir, ignore_errors=True)

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
            shadow_path = db_dir / "contextual_shadow.jsonl"

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
                patch.object(
                    sys,
                    "argv",
                    [
                        "ingest.py",
                        "--rebuild",
                        "--contextual-shadow-jsonl",
                        str(shadow_path),
                    ],
                ),
            ):
                ingest_module.main()

            with closing(sqlite3.connect(lexical_path)) as conn:
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
                shadow_records = [
                    json.loads(line)
                    for line in shadow_path.read_text(encoding="utf-8").splitlines()
                ]
                self.assertEqual(len(shadow_records), total_rows)
                lexical_document = conn.execute(
                    "SELECT document FROM lexical_chunk_meta WHERE chunk_id = ?",
                    (shadow_records[0]["chunk_id"],),
                ).fetchone()[0]
                lexical_search_text = conn.execute(
                    "SELECT search_text FROM lexical_chunks_fts WHERE chunk_id = ?",
                    (shadow_records[0]["chunk_id"],),
                ).fetchone()[0]
                self.assertEqual(lexical_document, shadow_records[0]["document"])
                self.assertNotEqual(
                    lexical_document,
                    shadow_records[0]["contextual_retrieval_text"],
                )
                self.assertEqual(
                    lexical_search_text,
                    shadow_records[0]["contextual_retrieval_text"],
                )
                self.assertIn(
                    "Guide:",
                    shadow_records[0]["contextual_retrieval_text"],
                )
        finally:
            gc.collect()
            shutil.rmtree(tmpdir, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
