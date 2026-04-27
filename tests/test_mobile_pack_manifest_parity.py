import hashlib
import json
import sqlite3
import tempfile
import unittest
from contextlib import closing
from pathlib import Path

from mobile_pack import ChunkRecord, GuideRecord, export_mobile_pack


def _sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


class MobilePackManifestParityTests(unittest.TestCase):
    def test_manifest_hashes_match_written_pack_files(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            output_dir = Path(tmpdir) / "pack"
            guides = [
                GuideRecord(
                    guide_id="GD-001",
                    slug="guide-one",
                    title="Guide One",
                    source_file="guide_one.md",
                    description="First guide",
                    category="water",
                    tags="water,filter",
                    related="guide-two",
                    body_markdown="Body one",
                ),
                GuideRecord(
                    guide_id="GD-002",
                    slug="guide-two",
                    title="Guide Two",
                    source_file="guide_two.md",
                    description="Second guide",
                    category="fire",
                    tags="fire",
                    related="",
                    body_markdown="Body two",
                ),
            ]
            chunks = [
                ChunkRecord(
                    chunk_id="chunk_0",
                    source_file="guide_one.md",
                    guide_id="GD-001",
                    guide_title="Guide One",
                    slug="guide-one",
                    description="First chunk",
                    category="water",
                    difficulty="basic",
                    last_updated="2026-04-10",
                    version="v1",
                    liability_level="medium",
                    tags="water,filter",
                    related="guide-two",
                    section_id="intro",
                    section_heading="Intro",
                    document="Boil the water first.",
                    embedding=(1.0, 0.0, 0.0),
                ),
                ChunkRecord(
                    chunk_id="chunk_1",
                    source_file="guide_two.md",
                    guide_id="GD-002",
                    guide_title="Guide Two",
                    slug="guide-two",
                    description="Second chunk",
                    category="fire",
                    difficulty="basic",
                    last_updated="2026-04-10",
                    version="v1",
                    liability_level="low",
                    tags="fire",
                    related="",
                    section_id="warmth",
                    section_heading="Warmth",
                    document="Use dry tinder.",
                    embedding=(0.0, 2.0, 0.0),
                ),
            ]

            summary = export_mobile_pack(
                output_dir,
                guides,
                [chunks],
                chunk_count=len(chunks),
                embedding_model_id="test-embed",
                vector_dtype="float16",
                mobile_top_k=10,
                source={"collection_name": "test"},
            )

            sqlite_path = Path(summary.sqlite_path)
            vector_path = Path(summary.vector_path)
            manifest = json.loads(Path(summary.manifest_path).read_text(encoding="utf-8"))

            self.assertEqual(
                _sha256(sqlite_path),
                manifest["files"]["sqlite"]["sha256"],
            )
            self.assertEqual(
                _sha256(vector_path),
                manifest["files"]["vectors"]["sha256"],
            )

    def test_manifest_answer_card_counts_match_sqlite_rows(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            output_dir = Path(tmpdir) / "pack"
            guides = [
                GuideRecord(
                    guide_id="GD-001",
                    slug="guide-one",
                    title="Guide One",
                    source_file="guide_one.md",
                    description="First guide",
                    category="water",
                    tags="water,filter",
                    related="guide-two",
                    body_markdown="Body one",
                ),
                GuideRecord(
                    guide_id="GD-002",
                    slug="guide-two",
                    title="Guide Two",
                    source_file="guide_two.md",
                    description="Second guide",
                    category="fire",
                    tags="fire",
                    related="",
                    body_markdown="Body two",
                ),
            ]
            chunks = [
                ChunkRecord(
                    chunk_id="chunk_0",
                    source_file="guide_one.md",
                    guide_id="GD-001",
                    guide_title="Guide One",
                    slug="guide-one",
                    description="First chunk",
                    category="water",
                    difficulty="basic",
                    last_updated="2026-04-10",
                    version="v1",
                    liability_level="medium",
                    tags="water,filter",
                    related="guide-two",
                    section_id="intro",
                    section_heading="Intro",
                    document="Boil the water first.",
                    embedding=(1.0, 0.0, 0.0),
                ),
                ChunkRecord(
                    chunk_id="chunk_1",
                    source_file="guide_two.md",
                    guide_id="GD-002",
                    guide_title="Guide Two",
                    slug="guide-two",
                    description="Second chunk",
                    category="fire",
                    difficulty="basic",
                    last_updated="2026-04-10",
                    version="v1",
                    liability_level="low",
                    tags="fire",
                    related="",
                    section_id="warmth",
                    section_heading="Warmth",
                    document="Use dry tinder.",
                    embedding=(0.0, 2.0, 0.0),
                ),
            ]
            answer_cards = [
                {
                    "card_id": "card_primary",
                    "guide_id": "GD-001",
                    "slug": "guide-one",
                    "title": "Primary card",
                    "risk_tier": "high",
                    "evidence_owner": "guide-corpus",
                    "review_status": "draft",
                    "runtime_citation_policy": "cite_card_sources",
                    "required_first_actions": ["Do this first.", "Check this."],
                    "first_actions": ["Take action."],
                    "urgent_red_flags": ["If smoke appears."],
                    "forbidden_advice": ["Do not add water."],
                    "do_not": ["Do not panic."],
                    "acceptable_uncertain_fit": "Use caution when uncertain.",
                    "conditional_required_actions": [
                        {
                            "trigger_terms": ["fog", "mist"],
                            "required_actions": ["Seek shelter.", "Call for help."],
                        }
                    ],
                    "source_sections": [
                        {
                            "guide": "GD-001",
                            "slug": "guide-one",
                            "title": "Guide One",
                            "sections": ["Intro"],
                        },
                        {
                            "guide": "GD-002",
                            "slug": "guide-two",
                            "title": "Guide Two",
                            "sections": ["Warmth"],
                        },
                    ],
                },
                {
                    "card_id": "card_secondary",
                    "guide_id": "GD-002",
                    "slug": "guide-two",
                    "title": "Secondary card",
                    "risk_tier": "medium",
                    "evidence_owner": "guide-corpus",
                    "review_status": "reviewed",
                    "runtime_citation_policy": "cite_card_sources",
                    "required_first_actions": ["Assess symptoms."],
                    "first_actions": ["Prepare supplies."],
                    "source_sections": [
                        {
                            "guide": "GD-002",
                            "slug": "guide-two",
                            "title": "Guide Two",
                            "sections": ["Warmth"],
                        },
                    ],
                },
            ]

            summary = export_mobile_pack(
                output_dir,
                guides,
                [chunks],
                chunk_count=len(chunks),
                embedding_model_id="test-embed",
                vector_dtype="float16",
                mobile_top_k=10,
                source={"collection_name": "test"},
                answer_cards=answer_cards,
            )

            with closing(sqlite3.connect(summary.sqlite_path)) as conn:
                manifest_answer_cards = conn.execute(
                    "SELECT value FROM pack_meta WHERE key = 'answer_card_count'"
                ).fetchone()[0]
                sqlite_answer_cards = conn.execute(
                    "SELECT COUNT(*) FROM answer_cards"
                ).fetchone()[0]
                sqlite_clauses = conn.execute(
                    "SELECT COUNT(*) FROM answer_card_clauses"
                ).fetchone()[0]
                sqlite_sources = conn.execute(
                    "SELECT COUNT(*) FROM answer_card_sources"
                ).fetchone()[0]

            manifest = json.loads(Path(summary.manifest_path).read_text(encoding="utf-8"))

            self.assertEqual(int(manifest_answer_cards), 2)
            self.assertEqual(sqlite_answer_cards, 2)
            self.assertEqual(manifest["counts"]["answer_cards"], 2)
            self.assertEqual(manifest["counts"]["answer_cards"], sqlite_answer_cards)
            self.assertEqual(sqlite_clauses, 11)
            self.assertEqual(sqlite_sources, 3)


if __name__ == "__main__":
    unittest.main()
