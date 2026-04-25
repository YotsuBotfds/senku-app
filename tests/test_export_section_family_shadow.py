import json
import tempfile
import unittest
from pathlib import Path

from scripts.export_section_family_shadow import (
    build_section_family_records,
    export_section_family_shadow_jsonl,
    group_chunks_by_section_family,
)


def _chunk(text, section_id, heading, chunk_id):
    return {
        "chunk_id": chunk_id,
        "text": text,
        "metadata": {
            "source_file": "field-guide.md",
            "guide_id": "GD-100",
            "guide_title": "Field Guide",
            "slug": "field-guide",
            "description": "A guide for field checks.",
            "category": "medical",
            "tags": "triage,field",
            "related": "GD-101",
            "section_id": section_id,
            "section_heading": heading,
        },
    }


class ExportSectionFamilyShadowTests(unittest.TestCase):
    def test_group_chunks_by_section_family_uses_adjacent_windows(self):
        chunks = [
            _chunk("Intro first chunk.", "intro", "Overview", "chunk_1"),
            _chunk("Intro second chunk.", "intro", "Overview", "chunk_2"),
            _chunk("Steps chunk.", "steps", "Steps", "chunk_3"),
            _chunk("Warnings chunk.", "warnings", "Warnings", "chunk_4"),
        ]

        groups = group_chunks_by_section_family(chunks, family_window=2)

        self.assertEqual(len(groups), 2)
        self.assertEqual(groups[0]["section_family"], "GD-100:00")
        self.assertEqual(groups[0]["metadata"]["section_ids"], "intro,steps")
        self.assertEqual(groups[0]["metadata"]["section_headings"], "Overview | Steps")
        self.assertEqual(groups[0]["source_chunk_ids"], ["chunk_1", "chunk_2", "chunk_3"])
        self.assertEqual(groups[1]["section_family"], "GD-100:01")
        self.assertEqual(groups[1]["metadata"]["section_ids"], "warnings")

    def test_build_section_family_records_emit_raptor_lite_shape(self):
        chunks = [
            _chunk("## Overview\nCheck breathing and alertness.", "intro", "Overview", "chunk_1"),
            _chunk("## Steps\nCall for help and keep warm.", "steps", "Steps", "chunk_2"),
            _chunk("## Warnings\nDo not delay evacuation.", "warnings", "Warnings", "chunk_3"),
        ]

        records = build_section_family_records(
            chunks,
            family_window=2,
            max_fragments=3,
        )

        self.assertEqual(len(records), 2)
        first = records[0]
        self.assertEqual(first["section_family"], "GD-100:00")
        self.assertEqual(first["section_ids"], ["intro", "steps"])
        self.assertEqual(first["metadata"]["record_type"], "section_family_summary")
        self.assertEqual(first["metadata"]["source_chunk_count"], "2")
        self.assertEqual(first["raptor_lite_text"], first["contextual_retrieval_text"])
        self.assertEqual(first["document"], first["raptor_lite_text"])
        self.assertIn("Guide: Field Guide | GD-100 | field-guide", first["raptor_lite_text"])
        self.assertIn("Section family: GD-100:00", first["raptor_lite_text"])
        self.assertIn("Sections: Overview | Steps", first["raptor_lite_text"])
        self.assertIn("Evidence fragments:", first["raptor_lite_text"])

    def test_export_section_family_shadow_jsonl_from_markdown_file(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guide_path = root / "field-guide.md"
            guide_path.write_text(
                """---
id: GD-100
slug: field-guide
title: Field Guide
category: medical
description: A guide for field checks.
tags: [triage, field]
---

## Overview

Check breathing and alertness.

## Steps

Call for help and keep warm.

## Warnings

Do not delay evacuation.
""",
                encoding="utf-8",
            )
            output_path = root / "section_family.jsonl"

            count = export_section_family_shadow_jsonl(
                [str(guide_path)],
                output_path,
                family_window=2,
                max_fragments=3,
            )
            records = [
                json.loads(line)
                for line in output_path.read_text(encoding="utf-8").splitlines()
            ]

        self.assertEqual(count, 2)
        self.assertEqual(len(records), 2)
        self.assertEqual(records[0]["section_family"], "GD-100:00")
        self.assertEqual(records[1]["section_family"], "GD-100:01")
        self.assertEqual(records[0]["metadata"]["section_ids"], "overview,steps")
        self.assertIn("raptor_lite_text", records[0])


if __name__ == "__main__":
    unittest.main()
