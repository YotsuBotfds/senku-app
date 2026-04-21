import json
import sqlite3
import struct
import tempfile
import unittest
from contextlib import closing
from pathlib import Path

from mobile_pack import (
    STRUCTURE_TYPE_COMMUNITY_GOVERNANCE,
    STRUCTURE_TYPE_COMMUNITY_SECURITY,
    STRUCTURE_TYPE_EMERGENCY_SHELTER,
    STRUCTURE_TYPE_FAIR_TRIAL,
    STRUCTURE_TYPE_GLASSMAKING,
    STRUCTURE_TYPE_MESSAGE_AUTH,
    STRUCTURE_TYPE_EARTH_SHELTER,
    STRUCTURE_TYPE_GENERAL,
    STRUCTURE_TYPE_SAFETY_POISONING,
    STRUCTURE_TYPE_SANITATION_SYSTEM,
    STRUCTURE_TYPE_SOAPMAKING,
    STRUCTURE_TYPE_WATER_DISTRIBUTION,
    STRUCTURE_TYPE_WATER_PURIFICATION,
    STRUCTURE_TYPE_WATER_STORAGE,
    VECTOR_DTYPE_CODES,
    VECTOR_FILE_MAGIC,
    VECTOR_HEADER_STRUCT,
    _derive_chunk_metadata,
    _derive_guide_metadata,
    load_deterministic_rule_records,
    parse_android_deterministic_predicate_names,
    validate_mobile_pack_deterministic_parity,
    ChunkRecord,
    GuideRecord,
    export_mobile_pack,
    load_guide_records,
)


class MobilePackTests(unittest.TestCase):
    def test_load_guide_records_reads_frontmatter_and_body(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir)
            (guides_dir / "guide_one.md").write_text(
                """---
id: GD-001
slug: guide-one
title: Guide One
description: First guide
category: water
difficulty: basic
last_updated: 2026-04-10
version: v1
liability_level: medium
tags:
  - water
  - filter
related:
  - guide-two
---
<section id="intro">
## Intro

Guide body.
</section>
""",
                encoding="utf-8",
            )

            records = load_guide_records(guides_dir)

        self.assertEqual(len(records), 1)
        self.assertEqual(records[0].guide_id, "GD-001")
        self.assertEqual(records[0].slug, "guide-one")
        self.assertEqual(records[0].tags, "water,filter")
        self.assertEqual(records[0].related, "guide-two")
        self.assertIn("Guide body.", records[0].body_markdown)

    def test_export_mobile_pack_writes_sqlite_manifest_and_float16_vectors(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
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
                root / "pack",
                guides,
                [chunks],
                chunk_count=len(chunks),
                embedding_model_id="test-embed",
                vector_dtype="float16",
                mobile_top_k=10,
                source={"collection_name": "test"},
            )

            sqlite_path = Path(summary.sqlite_path)
            manifest_path = Path(summary.manifest_path)
            vector_path = Path(summary.vector_path)

            with closing(sqlite3.connect(sqlite_path)) as conn:
                guide_count = conn.execute("SELECT COUNT(*) FROM guides").fetchone()[0]
                chunk_count = conn.execute("SELECT COUNT(*) FROM chunks").fetchone()[0]
                fts_count = conn.execute("SELECT COUNT(*) FROM lexical_chunks_fts").fetchone()[0]
                fts4_count = conn.execute("SELECT COUNT(*) FROM lexical_chunks_fts4").fetchone()[0]
                related_guide = conn.execute(
                    """
                    SELECT related_guide_id
                    FROM guide_related
                    WHERE guide_id = 'GD-001' AND related_ref = 'guide-two'
                    """
                ).fetchone()[0]
                deterministic_rule_count = conn.execute(
                    "SELECT COUNT(*) FROM deterministic_rules"
                ).fetchone()[0]
                deterministic_rule_row = conn.execute(
                    """
                    SELECT rule_id, predicate_name, builder_name, sample_prompt
                    FROM deterministic_rules
                    ORDER BY rule_id
                    LIMIT 1
                    """
                ).fetchone()
                vector_row = conn.execute(
                    "SELECT vector_row_id FROM chunks WHERE chunk_id = 'chunk_1'"
                ).fetchone()[0]

            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            payload = vector_path.read_bytes()
            header = VECTOR_HEADER_STRUCT.unpack(payload[: VECTOR_HEADER_STRUCT.size])
            first_row = struct.unpack("<3e", payload[VECTOR_HEADER_STRUCT.size : VECTOR_HEADER_STRUCT.size + 6])
            second_row = struct.unpack("<3e", payload[VECTOR_HEADER_STRUCT.size + 6 : VECTOR_HEADER_STRUCT.size + 12])

        self.assertEqual(guide_count, 2)
        self.assertEqual(chunk_count, 2)
        self.assertEqual(fts_count, 2)
        self.assertEqual(fts4_count, 2)
        self.assertEqual(related_guide, "GD-002")
        self.assertGreater(deterministic_rule_count, 0)
        self.assertEqual(len(deterministic_rule_row), 4)
        self.assertTrue(all(deterministic_rule_row))
        self.assertEqual(vector_row, 1)
        self.assertEqual(header[0], VECTOR_FILE_MAGIC)
        self.assertEqual(header[3], 2)
        self.assertEqual(header[4], 3)
        self.assertEqual(header[5], VECTOR_DTYPE_CODES["float16"])
        self.assertEqual(first_row, (1.0, 0.0, 0.0))
        self.assertEqual(second_row, (0.0, 1.0, 0.0))
        self.assertEqual(manifest["counts"]["chunks"], 2)
        self.assertEqual(manifest["embedding"]["vector_dtype"], "float16")
        self.assertEqual(manifest["embedding"]["dimension"], 3)
        self.assertEqual(manifest["runtime_defaults"]["mobile_top_k"], 10)
        self.assertEqual(
            manifest["schema"]["deterministic_rules"]["columns"],
            ["rule_id", "predicate_name", "builder_name", "sample_prompt"],
        )
        self.assertTrue(manifest["schema"]["deterministic_rules"]["metadata_only"])

    def test_export_mobile_pack_writes_int8_vector_payload(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides = [
                GuideRecord(
                    guide_id="GD-001",
                    slug="guide-one",
                    title="Guide One",
                    source_file="guide_one.md",
                    description="First guide",
                    category="water",
                )
            ]
            chunks = [
                ChunkRecord(
                    chunk_id="chunk_0",
                    source_file="guide_one.md",
                    guide_id="GD-001",
                    guide_title="Guide One",
                    slug="guide-one",
                    description="",
                    category="water",
                    difficulty="basic",
                    last_updated="",
                    version="",
                    liability_level="",
                    tags="",
                    related="",
                    section_id="intro",
                    section_heading="Intro",
                    document="Normalized vector.",
                    embedding=(0.5, 0.0, -0.5),
                )
            ]

            summary = export_mobile_pack(
                root / "pack",
                guides,
                [chunks],
                chunk_count=1,
                embedding_model_id="test-embed",
                vector_dtype="int8",
                mobile_top_k=8,
            )
            payload = Path(summary.vector_path).read_bytes()
            header = VECTOR_HEADER_STRUCT.unpack(payload[: VECTOR_HEADER_STRUCT.size])
            row = struct.unpack("<3b", payload[VECTOR_HEADER_STRUCT.size : VECTOR_HEADER_STRUCT.size + 3])

        self.assertEqual(header[5], VECTOR_DTYPE_CODES["int8"])
        self.assertEqual(row, (90, 0, -90))

    def test_load_deterministic_rule_records_matches_checked_in_android_manifest(self):
        records = load_deterministic_rule_records()

        self.assertEqual(
            [record.rule_id for record in records],
            [
                "generic_puncture",
                "charcoal_sand_water_filter_starter",
                "reused_container_water",
                "water_without_fuel",
                "fire_in_rain",
                "weld_without_welder_starter",
                "metal_splinter",
                "candles_for_light",
                "glassmaking_starter",
            ],
        )
        self.assertEqual(
            [record.predicate_name for record in records],
            [
                "_is_generic_puncture_special_case",
                "_is_charcoal_sand_water_filter_special_case",
                "_is_reused_container_water_special_case",
                "_is_water_without_fuel_special_case",
                "_is_fire_in_rain_special_case",
                "_is_weld_without_welder_special_case",
                "_is_metal_splinter_special_case",
                "_is_candles_for_light_special_case",
                "_is_glassmaking_starter_special_case",
            ],
        )

    def test_validate_mobile_pack_deterministic_parity_matches_checked_in_router(self):
        result = validate_mobile_pack_deterministic_parity()

        self.assertEqual(result["manifest_entry_count"], 9)
        self.assertEqual(result["java_predicate_count"], 9)
        self.assertEqual(result["exported_rule_count"], 9)

    def test_validate_mobile_pack_deterministic_parity_detects_router_manifest_drift(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            temp_root = Path(tmpdir)
            manifest_path = temp_root / "deterministic_predicate_manifest.txt"
            router_path = temp_root / "DeterministicAnswerRouter.java"

            manifest_path.write_text(
                (
                    "# rule_id|desktop_predicate_name|android_predicate_name\n"
                    "generic_puncture|_is_generic_puncture_special_case|isGenericPuncture\n"
                ),
                encoding="utf-8",
            )
            router_path.write_text(
                (
                    "public final class DeterministicAnswerRouter {\n"
                    "    private static final int ACTIVE_RULE_COUNT = 1;\n"
                    "    private static boolean isDifferentPredicate(String lower) {\n"
                    "        return false;\n"
                    "    }\n"
                    "}\n"
                ),
                encoding="utf-8",
            )

            with self.assertRaisesRegex(
                ValueError,
                "Manifest expects Android predicates missing from router: isGenericPuncture",
            ):
                validate_mobile_pack_deterministic_parity(
                    manifest_path=manifest_path,
                    router_path=router_path,
                )

    def test_guide_metadata_stays_conservative_for_non_building_guides(self):
        guide = GuideRecord(
            guide_id="GD-067",
            slug="animal-husbandry",
            title="Animal Husbandry & Veterinary",
            source_file="animal-husbandry.md",
            description="Foundational animal care and livestock handling.",
            category="agriculture",
            tags="animals,veterinary,livestock",
            body_markdown="Pig farming needs drainage, ventilation, and a sturdy wallow area.",
        )

        meta = _derive_guide_metadata(guide)

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_GENERAL)

    def test_poisoning_guides_detect_safety_poisoning_structure_and_tags(self):
        repo_guides = Path(__file__).resolve().parents[1] / "guides"
        records = {record.guide_id: record for record in load_guide_records(repo_guides)}
        expected_topic_overlap = {
            "GD-898": {"poisoning_triage"},
            "GD-301": {"poisoning_triage", "antidotes"},
            "GD-054": {"poisoning_triage", "toxidrome", "antidotes"},
            "GD-602": {"toxidrome", "antidotes"},
        }

        for guide_id, required_tags in expected_topic_overlap.items():
            with self.subTest(guide_id=guide_id):
                meta = _derive_guide_metadata(records[guide_id])
                tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

                self.assertEqual(meta.structure_type, STRUCTURE_TYPE_SAFETY_POISONING)
                self.assertTrue(tags)
                self.assertTrue(tags & required_tags)

        self.assertIn(
            "lye_safety",
            set(_derive_guide_metadata(records["GD-898"]).topic_tags.split(",")),
        )
        self.assertIn(
            "lye_safety",
            set(_derive_guide_metadata(records["GD-301"]).topic_tags.split(",")),
        )

    def test_emergency_shelter_markers_cover_rain_and_tarp_phrase_variants(self):
        positive_phrases = (
            "rain shelter",
            "rain fly",
            "tarp shelter",
            "tarp ridgeline",
            "ridgeline shelter",
        )

        for index, phrase in enumerate(positive_phrases, start=1):
            with self.subTest(phrase=phrase):
                guide = GuideRecord(
                    guide_id=f"GD-90{index}",
                    slug=f"{phrase.replace(' ', '-')}-guide",
                    title=f"{phrase.title()} Basics",
                    source_file=f"{phrase.replace(' ', '-')}.md",
                    description=f"Field notes for building a {phrase} with a tarp and cord.",
                    category="construction",
                    tags="shelter,tarp",
                    body_markdown="Anchor the corners, tension the line, and check drainage away from the sleeping area.",
                )

                meta = _derive_guide_metadata(guide)

                self.assertEqual(meta.structure_type, STRUCTURE_TYPE_EMERGENCY_SHELTER)

        negative = GuideRecord(
            guide_id="GD-999",
            slug="tarp-storage-guide",
            title="Tarp Storage Basics",
            source_file="tarp-storage-guide.md",
            description="How to fold, dry, and store a tarp after use.",
            category="resource-management",
            tags="tarp,storage",
            body_markdown="Keep the tarp clean, dry it completely, and store it away from sharp edges.",
        )

        negative_meta = _derive_guide_metadata(negative)

        self.assertEqual(negative_meta.structure_type, STRUCTURE_TYPE_GENERAL)

    def test_emergency_shelter_markers_cover_corpus_vetted_shelter_phrases(self):
        positive_phrases = (
            "shelter site",
            "primitive shelter",
            "seasonal shelter",
            "temporary shelter",
            "cave shelter",
        )

        for index, phrase in enumerate(positive_phrases, start=1):
            with self.subTest(phrase=phrase):
                guide = GuideRecord(
                    guide_id=f"GD-95{index}",
                    slug=f"{phrase.replace(' ', '-')}-guide",
                    title=f"{phrase.title()} Basics",
                    source_file=f"{phrase.replace(' ', '-')}.md",
                    description=f"Field notes for evaluating a {phrase} before the weather turns.",
                    category="survival",
                    tags="shelter,fieldcraft",
                    body_markdown="Check wind, drainage, and local hazards before settling in for the night.",
                )

                meta = _derive_guide_metadata(guide)

                self.assertEqual(meta.structure_type, STRUCTURE_TYPE_EMERGENCY_SHELTER)

        arctic_shape = GuideRecord(
            guide_id="GD-445",
            slug="arctic-survival-boreal",
            title="Arctic Survival & Boreal Adaptation",
            source_file="arctic-survival-boreal.md",
            description="Frostbite prevention, snow travel, igloo and snow shelter construction, and cold camp routine planning.",
            category="survival",
            tags="arctic,cold-weather,survival",
            body_markdown="Preserve heat, manage layers, and maintain a dry sleep system in severe cold.",
        )
        self.assertNotEqual(
            _derive_guide_metadata(arctic_shape).structure_type,
            STRUCTURE_TYPE_EMERGENCY_SHELTER,
        )

        nuclear_shape = GuideRecord(
            guide_id="GD-563",
            slug="nuclear-preparedness-fallout",
            title="Nuclear Preparedness & Fallout Shelter Operations",
            source_file="nuclear-preparedness-fallout.md",
            description="Fallout shelter design, dose-rate awareness, shelter construction (purpose-built and expedient), and contamination control.",
            category="preparedness",
            tags="nuclear,fallout,shelter",
            body_markdown="Track exposure, improve shielding, and minimize contamination transfer between zones.",
        )
        self.assertNotEqual(
            _derive_guide_metadata(nuclear_shape).structure_type,
            STRUCTURE_TYPE_EMERGENCY_SHELTER,
        )

    def test_GD_446_reclassifies_from_cabin_house_to_emergency_shelter_post_marker_addition(self):
        # The new "shelter site" marker intentionally wins before CABIN_HOUSE's
        # longer site-selection phrase because _detect_structure_type is first-match.
        guide = GuideRecord(
            guide_id="GD-446",
            slug="shelter-site-assessment",
            title="Shelter Site Selection & Hazard Assessment",
            source_file="shelter-site-assessment.md",
            description="Terrain analysis, water proximity, wind exposure, natural hazards, defensibility, resource proximity, and seasonal considerations for shelter site selection.",
            category="survival",
            tags="essential,shelter,site-selection,campsite,camp-site,tent-location,safe-camp,flood-risk,shelter-location,where-to-build,base-camp,terrain-check,hazard-check",
            body_markdown="Consider flood risk, drainage, and prevailing wind when selecting a shelter site.",
        )

        meta = _derive_guide_metadata(guide)

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_EMERGENCY_SHELTER)

    def test_GD_294_tags_emergency_shelter_post_marker_addition(self):
        guide = GuideRecord(
            guide_id="GD-294",
            slug="cave-shelter-systems",
            title="Cave Shelter Systems and Long-Term Habitation",
            source_file="cave-shelter-systems.md",
            description="Cave selection criteria, cold trap zones and ventilation, humidity management, radon and CO2 risks, lighting, water sources, and considerations for extended occupation.",
            category="survival",
            tags="practical,survival",
            body_markdown="Evaluate cave air quality, humidity, and stability before extended use.",
        )

        meta = _derive_guide_metadata(guide)

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_EMERGENCY_SHELTER)

    def test_chunk_metadata_detects_water_storage_without_inheriting_wrong_domain_tags(self):
        guide = GuideRecord(
            guide_id="GD-252",
            slug="storage-material-management",
            title="Storage & Material Management",
            source_file="storage-material-management.md",
            description="Broad storage guide.",
            category="resource-management",
            tags="storage,containers",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_252",
            source_file="storage-material-management.md",
            guide_id="GD-252",
            guide_title="Storage & Material Management",
            slug="storage-material-management",
            description="Long-term treated water storage guidance.",
            category="resource-management",
            difficulty="basic",
            last_updated="2026-04-11",
            version="v1",
            liability_level="medium",
            tags="water,container,storage",
            related="",
            section_id="water-storage",
            section_heading="Water Storage: Hydration Assurance",
            document="Use food-safe containers, sanitize them, fill with treated water, seal, label, and rotate stored water on a schedule.",
            embedding=(1.0, 0.0, 0.0),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_WATER_STORAGE)
        self.assertIn("container_sanitation", meta.topic_tags)
        self.assertIn("water_rotation", meta.topic_tags)

    def test_non_water_home_inventory_chunk_does_not_inherit_water_storage_metadata(self):
        guide = GuideRecord(
            guide_id="GD-373",
            slug="home-inventory",
            title="Home Inventory",
            source_file="home-inventory.md",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            tags="preparedness,inventory,water",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_373_medical",
            source_file="home-inventory.md",
            guide_id="GD-373",
            guide_title="Home Inventory",
            slug="home-inventory",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="inventory,medical",
            related="",
            section_id="medical-supplies",
            section_heading="Medical Supplies: Beyond Band-Aids",
            document="Store bandages, prescription refills, gloves, and oral rehydration salts for outages and injuries.",
            embedding=(0.1, 0.1, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_GENERAL)
        self.assertNotIn("water_storage", tags)
        self.assertNotIn("container_sanitation", tags)
        self.assertNotIn("water_rotation", tags)

    def test_water_home_inventory_chunk_still_keeps_water_storage_metadata(self):
        guide = GuideRecord(
            guide_id="GD-373",
            slug="home-inventory",
            title="Home Inventory",
            source_file="home-inventory.md",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            tags="preparedness,inventory,water",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_373_water",
            source_file="home-inventory.md",
            guide_id="GD-373",
            guide_title="Home Inventory",
            slug="home-inventory",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="inventory,water",
            related="",
            section_id="water-storage",
            section_heading="Water Storage & Purification",
            document="Use food-safe containers, sanitize them, fill with treated water, seal, label, and rotate stored water.",
            embedding=(0.1, 0.1, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_WATER_STORAGE)
        self.assertIn("water_storage", tags)

    def test_home_inventory_food_chunk_with_barrel_stove_does_not_inherit_water_storage(self):
        guide = GuideRecord(
            guide_id="GD-373",
            slug="home-inventory",
            title="Home Inventory",
            source_file="home-inventory.md",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            tags="preparedness,inventory,water",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_373_food",
            source_file="home-inventory.md",
            guide_id="GD-373",
            guide_title="Home Inventory",
            slug="home-inventory",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="inventory,food",
            related="",
            section_id="food",
            section_heading="Food",
            document="Keep dry staples, a barrel stove, and a clean metal drum for emergency cooking and heating setups.",
            embedding=(0.1, 0.1, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_GENERAL)
        self.assertNotIn("water_storage", tags)

    def test_home_inventory_heat_chunk_with_tank_and_hot_water_bottles_does_not_inherit_water_storage(self):
        guide = GuideRecord(
            guide_id="GD-373",
            slug="home-inventory",
            title="Home Inventory",
            source_file="home-inventory.md",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            tags="preparedness,inventory,water",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_373_heat",
            source_file="home-inventory.md",
            guide_id="GD-373",
            guide_title="Home Inventory",
            slug="home-inventory",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="inventory,heat",
            related="",
            section_id="heat-power",
            section_heading="Heat & Power",
            document="Keep spare fuel canisters, a 20 lb tank adapter, and hot water bottles for cold-weather bedding.",
            embedding=(0.1, 0.1, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_GENERAL)
        self.assertNotIn("water_storage", tags)

    def test_home_inventory_summary_chunk_needs_more_than_food_grade_bucket_to_inherit_water_storage(self):
        guide = GuideRecord(
            guide_id="GD-373",
            slug="home-inventory",
            title="Home Inventory",
            source_file="home-inventory.md",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            tags="preparedness,inventory,water",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_373_summary",
            source_file="home-inventory.md",
            guide_id="GD-373",
            guide_title="Home Inventory",
            slug="home-inventory",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="inventory,summary",
            related="",
            section_id="summary",
            section_heading="Summary",
            document=(
                "Stock essential supplies for water storage, food preservation, heating, lighting, and emergency "
                "documents. Review food grade bucket counts, pantry readiness, and backup cooking supplies across "
                "the household."
            ),
            embedding=(0.1, 0.1, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_GENERAL)
        self.assertNotIn("water_storage", tags)

    def test_general_heading_with_real_water_context_can_still_inherit_water_storage(self):
        guide = GuideRecord(
            guide_id="GD-373",
            slug="home-inventory",
            title="Home Inventory",
            source_file="home-inventory.md",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            tags="preparedness,inventory,water",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_373_supplies",
            source_file="home-inventory.md",
            guide_id="GD-373",
            guide_title="Home Inventory",
            slug="home-inventory",
            description="Complete home preparedness inventory covering water storage, food, heat, and medical supplies.",
            category="resource-management",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="inventory,supplies",
            related="",
            section_id="supplies",
            section_heading="Supplies",
            document="For drinking water, keep food-safe containers sanitized, date filled, and rotated so treated water stays ready to use.",
            embedding=(0.1, 0.1, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_WATER_STORAGE)
        self.assertIn("water_storage", tags)

    def test_water_purification_guide_does_not_flip_to_storage_from_rainwater_mentions(self):
        guide = GuideRecord(
            guide_id="GD-035",
            slug="water-purification",
            title="Water Purification",
            source_file="water-purification.md",
            description="Boiling, filtration, chemical treatment, solar disinfection, well construction, rainwater harvesting, and water testing.",
            category="survival",
            tags="water,purification,disinfection",
            body_markdown="Use boiling, filtration, and disinfection to make water safe to drink.",
        )

        meta = _derive_guide_metadata(guide)

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_WATER_PURIFICATION)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()
        self.assertIn("water_purification", tags)
        self.assertNotIn("water_storage", tags)

    def test_water_distribution_guide_gets_distribution_topic_without_container_bias(self):
        guide = GuideRecord(
            guide_id="GD-553",
            slug="water-system-design-distribution",
            title="Water System Design and Distribution",
            source_file="water-system-design-distribution.md",
            description="Designing gravity-fed distribution networks, elevated storage, and household taps for settlement water systems.",
            category="building",
            tags="water,distribution,gravity-fed",
            body_markdown="Gravity-fed distribution systems rely on elevation, pipes, taps, and storage tanks rather than container cleaning workflows.",
        )

        meta = _derive_guide_metadata(guide)

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_WATER_DISTRIBUTION)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()
        self.assertIn("water_distribution", tags)
        self.assertNotIn("container_sanitation", tags)

    def test_plumbing_and_water_systems_guide_stays_water_distribution(self):
        guide = GuideRecord(
            guide_id="GD-105",
            slug="plumbing-pipes",
            title="Plumbing & Water Systems",
            source_file="plumbing-pipes.md",
            description="Pipe materials, water distribution networks, drainage systems, leakage prevention, and hydraulic infrastructure.",
            category="building",
            tags="essential",
            body_markdown="Gravity-fed water systems, indoor plumbing, and pipe materials for long-term settlement water infrastructure.",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_WATER_DISTRIBUTION)
        self.assertIn("water_distribution", tags)

    def test_community_water_distribution_guide_stays_out_of_general(self):
        guide = GuideRecord(
            guide_id="GD-270",
            slug="water-distribution-systems",
            title="Community Water Distribution Systems",
            source_file="community-water-distribution.md",
            description="Engineering guide for gravity-fed water distribution, water tower construction, pipe selection, distribution networks, and greywater reuse systems.",
            category="building",
            tags="critical,new,rebuild",
            body_markdown="Use gravity-fed layouts, elevated storage, taps, and distribution piping for community water points without modern pumps.",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_WATER_DISTRIBUTION)
        self.assertIn("water_distribution", tags)
        self.assertNotEqual(meta.structure_type, STRUCTURE_TYPE_GENERAL)

    def test_water_distribution_chunk_gets_distribution_topic_without_container_bias(self):
        guide = GuideRecord(
            guide_id="GD-270",
            slug="community-water-distribution",
            title="Community Water Distribution Systems",
            source_file="community-water-distribution.md",
            description="Engineering guide for gravity-fed water distribution, storage tanks, and settlement plumbing.",
            category="building",
            tags="water,distribution,plumbing",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_270",
            source_file="community-water-distribution.md",
            guide_id="GD-270",
            guide_title="Community Water Distribution Systems",
            slug="community-water-distribution",
            description="Gravity-fed system layout.",
            category="building",
            difficulty="intermediate",
            last_updated="2026-04-11",
            version="v1",
            liability_level="medium",
            tags="water,distribution,plumbing",
            related="",
            section_id="gravity-fed-design",
            section_heading="Gravity-Fed Water Distribution Design",
            document=(
                "Use an elevated storage tank, distribution piping, and simple taps so water can move from the cistern "
                "through the community system without pump dependence."
            ),
            embedding=(0.5, 0.5, 0.5),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_WATER_DISTRIBUTION)
        self.assertIn("water_distribution", tags)
        self.assertNotIn("container_sanitation", tags)
        self.assertNotIn("water_rotation", tags)

    def test_water_storage_chunk_with_incidental_distribution_note_stays_out_of_distribution_topic(self):
        guide = GuideRecord(
            guide_id="GD-252",
            slug="storage-material-management",
            title="Storage & Material Management",
            source_file="storage-material-management.md",
            description="Storage guidance for critical supplies and materials.",
            category="resource-management",
            tags="storage,water",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_252",
            source_file="storage-material-management.md",
            guide_id="GD-252",
            guide_title="Storage & Material Management",
            slug="storage-material-management",
            description="Hydration assurance for stored water.",
            category="resource-management",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="storage,water",
            related="",
            section_id="water-storage",
            section_heading="Water Storage: Hydration Assurance",
            document=(
                "Cisterns and tanks must be covered and screened against insects, animals, and debris. "
                "Elevate above ground level if possible (gravity-fed distribution)."
            ),
            embedding=(0.2, 0.2, 0.2),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertIn("water_storage", tags)
        self.assertNotIn("water_distribution", tags)

    def test_chunk_metadata_avoids_false_house_labels_for_pig_farming(self):
        guide = GuideRecord(
            guide_id="GD-067",
            slug="animal-husbandry",
            title="Animal Husbandry & Veterinary",
            source_file="animal-husbandry.md",
            description="Foundational animal care and livestock handling.",
            category="agriculture",
            tags="animals,veterinary,livestock",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_067",
            source_file="animal-husbandry.md",
            guide_id="GD-067",
            guide_title="Animal Husbandry & Veterinary",
            slug="animal-husbandry",
            description="Pig care basics.",
            category="agriculture",
            difficulty="basic",
            last_updated="2026-04-11",
            version="v1",
            liability_level="low",
            tags="animals,pigs",
            related="",
            section_id="pig-farming",
            section_heading="Pig Farming",
            document="Good pig pens need drainage and ventilation, but this section is about livestock management, not building a house.",
            embedding=(0.0, 1.0, 0.0),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_GENERAL)
        self.assertNotIn("site_selection", meta.topic_tags)

    def test_weatherproofing_marker_does_not_backfill_roofing_topic(self):
        guide = GuideRecord(
            guide_id="GD-444",
            slug="accessible-shelter-design",
            title="Accessible Shelter & Universal Design",
            source_file="accessible-shelter-design.md",
            description="Shelter accessibility guidance with weatherproofing concerns.",
            category="building",
            tags="accessible,shelter",
            related="insulation-weatherproofing",
        )

        meta = _derive_guide_metadata(guide)

        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertIn("weatherproofing", tags)
        self.assertNotIn("roofing", tags)

    def test_swale_section_does_not_get_water_storage_metadata_from_incidental_phrase(self):
        guide = GuideRecord(
            guide_id="GD-333",
            slug="drainage-earthworks",
            title="Drainage and Earthworks",
            source_file="drainage-earthworks.md",
            description="Swales, drains, and runoff control for construction sites.",
            category="building",
            tags="drainage,earthworks,water",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_333",
            source_file="drainage-earthworks.md",
            guide_id="GD-333",
            guide_title="Drainage and Earthworks",
            slug="drainage-earthworks",
            description="Swale design for water harvesting and flow.",
            category="building",
            difficulty="basic",
            last_updated="2026-04-11",
            version="v1",
            liability_level="medium",
            tags="drainage,earthworks,water",
            related="",
            section_id="swale-design",
            section_heading="Swale Design for Water Harvesting and Flow",
            document=(
                "A shallow swale planted with native species becomes habitat for frogs, birds, and pollinators "
                "while managing runoff. This green infrastructure approach supports water storage and water features "
                "without relying on purely engineered drainage."
            ),
            embedding=(0.0, 0.0, 1.0),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertNotEqual(meta.structure_type, STRUCTURE_TYPE_WATER_STORAGE)
        self.assertNotIn("water_storage", tags)

    def test_pond_construction_does_not_inherit_house_topic_tags_from_generic_construction_word(self):
        guide = GuideRecord(
            guide_id="GD-068",
            slug="aquaculture-fish-farming",
            title="Aquaculture & Fish Farming",
            source_file="aquaculture-fish-farming.md",
            description="Pond design, species selection, and fish harvest planning.",
            category="agriculture",
            tags="aquaculture,ponds,water",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_068",
            source_file="aquaculture-fish-farming.md",
            guide_id="GD-068",
            guide_title="Aquaculture & Fish Farming",
            slug="aquaculture-fish-farming",
            description="Pond construction for reliable fish production.",
            category="agriculture",
            difficulty="basic",
            last_updated="2026-04-11",
            version="v1",
            liability_level="medium",
            tags="aquaculture,pond,water",
            related="",
            section_id="pond-construction",
            section_heading="Pond Construction",
            document=(
                "Choose soil that holds water, inspect seepage, and cut spillways carefully so the pond remains stable "
                "through seasonal changes."
            ),
            embedding=(0.0, 1.0, 1.0),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertNotIn("site_selection", tags)
        self.assertNotIn("drainage", tags)

    def test_soap_guide_gets_soapmaking_and_lye_safety_tags(self):
        guide = GuideRecord(
            guide_id="GD-129",
            slug="soap-candles",
            title="Soap & Candle Making",
            source_file="soap-candles.md",
            description="Bar soap, candles, and lye-safe handling.",
            category="crafts",
            tags="soap,candles,lye",
            body_markdown="Mix lye carefully for saponification and flush chemical burns with water right away.",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertIn("soapmaking", tags)
        self.assertIn("lye_safety", tags)

    def test_message_auth_guide_gets_auth_and_chain_of_custody_tags(self):
        guide = GuideRecord(
            guide_id="GD-664",
            slug="signals-intelligence-comsec",
            title="Signals Intelligence & Communications Security",
            source_file="signals-intelligence-comsec.md",
            description="Message verification, challenge-response, and courier security.",
            category="communications",
            tags="communications,comsec,courier",
            body_markdown="Use challenge-response codes, authenticate stations, and preserve chain of custody for courier notes.",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertIn("message_authentication", tags)
        self.assertIn("chain_of_custody", tags)

    def test_outbreak_guide_gets_latrine_and_wash_station_tags(self):
        guide = GuideRecord(
            guide_id="GD-902",
            slug="camp-outbreak-dysentery-operations-quickstart",
            title="Camp Outbreak & Dysentery Operations Quickstart",
            source_file="camp-outbreak.md",
            description="Immediate camp sanitation triage.",
            category="medical",
            tags="outbreak,dysentery,sanitation",
            body_markdown="Inspect the latrine, enforce handwashing, and establish a hygiene station away from water points.",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertIn("latrine_design", tags)
        self.assertIn("wash_station", tags)

    def test_underground_bunker_guide_gets_earth_shelter_metadata(self):
        guide = GuideRecord(
            guide_id="GD-873",
            slug="underground-shelter-bunker",
            title="Underground Shelter & Bunker Construction",
            source_file="underground-shelter-bunker.md",
            description="Comprehensive guide to underground shelters and bunkers.",
            category="building",
            tags="rebuild,defense,survival",
        )

        meta = _derive_guide_metadata(guide)

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_EARTH_SHELTER)

    def test_homestead_chemistry_guide_detects_soapmaking_metadata(self):
        guide = GuideRecord(
            guide_id="GD-122",
            slug="homestead-chemistry",
            title="Homestead Chemistry",
            source_file="homestead-chemistry.md",
            description="Soap making, candle making, cleaning products, tanning & leatherwork, natural adhesives, and basic chemistry.",
            category="crafts",
            tags="soap,lye,tallow,chemistry",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_SOAPMAKING)
        self.assertIn("soapmaking", tags)
        self.assertIn("lye_safety", tags)

    def test_related_guides_do_not_flip_general_chemistry_metadata(self):
        guide = GuideRecord(
            guide_id="GD-571",
            slug="chemistry-fundamentals",
            title="Chemistry Fundamentals",
            source_file="chemistry-fundamentals.md",
            description="Basic chemical concepts, atoms and molecules, common reactions, and safety principles.",
            category="chemistry",
            tags="chemistry,beginner,fundamentals,reactions,safety",
            related="glass-making-raw-materials,soap-candles,water-purification",
            body_markdown="Atoms, molecules, chemical reactions, and oxidation-reduction basics.",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_GENERAL)
        self.assertNotIn("soapmaking", tags)
        self.assertNotIn("glassmaking", tags)

    def test_soapmaking_chunk_body_overrides_mismatched_guide_structure(self):
        guide = GuideRecord(
            guide_id="GD-571",
            slug="chemistry-fundamentals",
            title="Chemistry Fundamentals",
            source_file="chemistry-fundamentals.md",
            description="General chemistry basics with a separate glassmaking section elsewhere.",
            category="chemistry",
            tags="chemistry,glass",
            body_markdown="Glassmaking, annealing, and furnace basics.",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_571_soap",
            source_file="chemistry-fundamentals.md",
            guide_id="GD-571",
            guide_title="Chemistry Fundamentals",
            slug="chemistry-fundamentals",
            description="Practical synthesis procedures for useful chemicals.",
            category="chemistry",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="chemistry",
            related="",
            section_id="practical-synthesis",
            section_heading="Practical Synthesis Procedures: Making Useful Chemicals",
            document=(
                "Making soap from animal fats and wood ash lye is one of the most practical chemistry skills. "
                "Continue stirring until saponification reaches trace, then cure the bars before use."
            ),
            embedding=(0.1, 0.2, 0.3),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_SOAPMAKING)
        self.assertIn("soapmaking", tags)
        self.assertIn("lye_safety", tags)

    def test_mixed_compounds_chunk_without_local_soap_focus_does_not_inherit_soapmaking(self):
        guide = GuideRecord(
            guide_id="GD-572",
            slug="everyday-compounds-production",
            title="Everyday Compounds and Production",
            source_file="everyday-compounds-production.md",
            description="Making practical compounds at home: soap, salt extraction, and natural dyes.",
            category="crafts",
            tags="chemistry,production,soap,compounds,beginner",
            related="chemistry-fundamentals,water-purification",
            body_markdown=(
                "Making Soap with lye water, animal fat, and curing. "
                "Salt extraction and crystallization from seawater."
            ),
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_572_salt",
            source_file="everyday-compounds-production.md",
            guide_id="GD-572",
            guide_title="Everyday Compounds and Production",
            slug="everyday-compounds-production",
            description="Salt extraction and crystallization from seawater.",
            category="crafts",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="chemistry,salt",
            related="",
            section_id="salt-extraction",
            section_heading="Salt Extraction and Crystallization",
            document=(
                "Evaporate seawater in shallow pans, protect the brine from contamination, "
                "and scrape formed salt crystals once the water is gone."
            ),
            embedding=(0.2, 0.3, 0.4),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertNotEqual(meta.structure_type, STRUCTURE_TYPE_SOAPMAKING)
        self.assertNotIn("soapmaking", tags)
        self.assertNotIn("lye_safety", tags)

    def test_incidental_soap_reference_does_not_flip_chunk_to_soapmaking(self):
        guide = GuideRecord(
            guide_id="GD-722",
            slug="ph-acids-bases-water-chemistry-essentials",
            title="pH, Acids & Bases: Water Chemistry Essentials",
            source_file="ph-acids-bases.md",
            description="General acid-base chemistry and pH guidance.",
            category="chemistry",
            tags="chemistry,ph,bases",
            body_markdown="General chemistry and water treatment context.",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_722_bases",
            source_file="ph-acids-bases.md",
            guide_id="GD-722",
            guide_title="pH, Acids & Bases: Water Chemistry Essentials",
            slug="ph-acids-bases-water-chemistry-essentials",
            description="Understanding why bases feel slippery.",
            category="chemistry",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="chemistry,bases",
            related="",
            section_id="understanding-bases",
            section_heading="Understanding Bases",
            document=(
                "Bases feel slippery because they break down oils on your skin. "
                "Weak base solutions can clean, but strong bases destroy tissue and require careful handling."
            ),
            embedding=(0.3, 0.2, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertNotEqual(meta.structure_type, STRUCTURE_TYPE_SOAPMAKING)
        self.assertNotIn("soapmaking", tags)

    def test_message_authentication_chunk_detects_society_metadata(self):
        guide = GuideRecord(
            guide_id="GD-389",
            slug="message-authentication-courier-protocols",
            title="Message Authentication & Courier Protocols",
            source_file="message-authentication-courier-protocols.md",
            description="Seals, courier verification, tamper evidence, and chain-of-custody for sensitive notes.",
            category="society",
            tags="courier,authentication,seals",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_389",
            source_file="message-authentication-courier-protocols.md",
            guide_id="GD-389",
            guide_title="Message Authentication & Courier Protocols",
            slug="message-authentication-courier-protocols",
            description="Courier trust and authentication basics.",
            category="society",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="courier,authentication,seals",
            related="",
            section_id="auth",
            section_heading="Chain of Custody and Documentation",
            document=(
                "Use tamper-evident seals, courier identity checks, and a written chain of custody log "
                "to verify sensitive orders and detect note forgery."
            ),
            embedding=(0.1, 0.2, 0.3),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_MESSAGE_AUTH)
        self.assertIn("message_authentication", tags)
        self.assertIn("chain_of_custody", tags)

    def test_message_auth_chunk_body_overrides_generic_security_guide_structure(self):
        guide = GuideRecord(
            guide_id="GD-651",
            slug="critical-infrastructure-physical-security",
            title="Critical Infrastructure Physical Security",
            source_file="critical-infrastructure-physical-security.md",
            description="General security and guard procedures.",
            category="society",
            tags="security,guards",
            body_markdown="General guard posts and perimeter discipline.",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_651_auth",
            source_file="critical-infrastructure-physical-security.md",
            guide_id="GD-651",
            guide_title="Critical Infrastructure Physical Security",
            slug="critical-infrastructure-physical-security",
            description="Courier verification procedures.",
            category="society",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="security,courier",
            related="",
            section_id="courier-auth",
            section_heading="Courier Verification Procedures",
            document=(
                "Use challenge-response checks, tamper-evident seals, and a written chain of custody "
                "log to verify sensitive notes and detect forged messages."
            ),
            embedding=(0.1, 0.2, 0.3),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(guide_meta.structure_type, STRUCTURE_TYPE_COMMUNITY_SECURITY)
        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_MESSAGE_AUTH)
        self.assertIn("message_authentication", tags)
        self.assertIn("chain_of_custody", tags)

    def test_generic_security_chunk_without_auth_markers_stays_out_of_message_auth(self):
        guide = GuideRecord(
            guide_id="GD-651",
            slug="critical-infrastructure-physical-security",
            title="Critical Infrastructure Physical Security",
            source_file="critical-infrastructure-physical-security.md",
            description="General security and guard procedures.",
            category="society",
            tags="security,guards",
            body_markdown="General guard posts and perimeter discipline.",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_651_patrols",
            source_file="critical-infrastructure-physical-security.md",
            guide_id="GD-651",
            guide_title="Critical Infrastructure Physical Security",
            slug="critical-infrastructure-physical-security",
            description="Patrol routes and watch rotations.",
            category="society",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="security,patrols",
            related="",
            section_id="patrol-routes",
            section_heading="Patrol Routes and Watch Rotations",
            document=(
                "Rotate patrol routes, maintain overlapping observation, and keep perimeter reports "
                "consistent across shifts."
            ),
            embedding=(0.3, 0.2, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_COMMUNITY_SECURITY)
        self.assertIn("community_security", tags)
        self.assertNotIn("message_authentication", tags)

    def test_security_chunk_with_light_verification_language_stays_community_security(self):
        guide = GuideRecord(
            guide_id="GD-651",
            slug="critical-infrastructure-physical-security",
            title="Critical Infrastructure Physical Security",
            source_file="critical-infrastructure-physical-security.md",
            description="General security and guard procedures.",
            category="defense",
            tags="security,guards",
            body_markdown="General guard posts, perimeter discipline, and early warning for vulnerable systems.",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_651_water_system",
            source_file="critical-infrastructure-physical-security.md",
            guide_id="GD-651",
            guide_title="Critical Infrastructure Physical Security",
            slug="critical-infrastructure-physical-security",
            description="Water system access control and patrol handoff.",
            category="defense",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="security,water system",
            related="",
            section_id="water-system-security",
            section_heading="Water System Security",
            document=(
                "Verify shift handoff at access-control points, keep patrol routes overlapping, "
                "and protect pumps, valves, and tanks with layered observation."
            ),
            embedding=(0.3, 0.2, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_COMMUNITY_SECURITY)
        self.assertIn("community_security", tags)
        self.assertIn("resource_security", tags)
        self.assertNotIn("message_authentication", tags)

    def test_security_guide_metadata_uses_community_security_structure(self):
        guide = GuideRecord(
            guide_id="GD-651",
            slug="critical-infrastructure-physical-security",
            title="Critical Infrastructure Physical Security",
            source_file="critical-infrastructure-physical-security.md",
            description="Guard rotation, access control, and early warning for vulnerable systems.",
            category="defense",
            tags="security,guards,critical infrastructure",
            body_markdown="Protect water systems and food storage with layered access control, patrol discipline, and early warning.",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_COMMUNITY_SECURITY)
        self.assertIn("community_security", tags)
        self.assertIn("resource_security", tags)

    def test_governance_guide_metadata_uses_community_governance_structure(self):
        guide = GuideRecord(
            guide_id="GD-626",
            slug="commons-management-sustainable-resource-governance",
            title="Commons Management & Sustainable Resource Governance",
            source_file="commons-management-sustainable-resource-governance.md",
            description="Shared rules, mutual aid, graduated sanctions, and trust-building for common resources.",
            category="resource-management",
            tags="commons,governance,trust",
            body_markdown="Use monitoring, graduated sanctions, mediation, and restitution to protect the commons and rebuild trust.",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_COMMUNITY_GOVERNANCE)
        self.assertIn("community_governance", tags)
        self.assertIn("conflict_resolution", tags)

    def test_governance_chunk_detects_conflict_and_trust_topics(self):
        guide = GuideRecord(
            guide_id="GD-626",
            slug="commons-management-sustainable-resource-governance",
            title="Commons Management & Sustainable Resource Governance",
            source_file="commons-management-sustainable-resource-governance.md",
            description="Shared rules and mutual aid.",
            category="resource-management",
            tags="commons,governance,trust",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_626_restitution",
            source_file="commons-management-sustainable-resource-governance.md",
            guide_id="GD-626",
            guide_title="Commons Management & Sustainable Resource Governance",
            slug="commons-management-sustainable-resource-governance",
            description="Theft response and restitution procedures.",
            category="resource-management",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="theft,restitution,trust",
            related="",
            section_id="sanctions",
            section_heading="Monitoring and Graduated Sanctions",
            document=(
                "Verify the theft, document the loss, require restitution, and use graduated sanctions with mediation "
                "and trust rebuilding to protect shared stores."
            ),
            embedding=(0.4, 0.2, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_COMMUNITY_GOVERNANCE)
        self.assertIn("community_governance", tags)
        self.assertIn("conflict_resolution", tags)
        self.assertIn("trust_systems", tags)

    def test_sanitation_chunk_detects_latrine_design_metadata(self):
        guide = GuideRecord(
            guide_id="GD-855",
            slug="field-medicine-diagnosis-treatment",
            title="Field Medicine: Diagnosis & Treatment",
            source_file="field-medicine-diagnosis-treatment.md",
            description="Diagnosis, treatment, and sanitation guidance for austere settings.",
            category="medical",
            tags="sanitation,field medicine",
        )
        guide_meta = _derive_guide_metadata(guide)
        chunk = ChunkRecord(
            chunk_id="chunk_855",
            source_file="field-medicine-diagnosis-treatment.md",
            guide_id="GD-855",
            guide_title="Field Medicine: Diagnosis & Treatment",
            slug="field-medicine-diagnosis-treatment",
            description="Off-grid sanitation systems.",
            category="medical",
            difficulty="basic",
            last_updated="2026-04-12",
            version="v1",
            liability_level="medium",
            tags="sanitation,latrine",
            related="",
            section_id="latrine-construction",
            section_heading="Latrine Construction for Off-Grid Living",
            document=(
                "Site the pit latrine downhill and away from water, add a handwashing station, "
                "and maintain the trench so it does not become a disease source."
            ),
            embedding=(0.3, 0.2, 0.1),
        )

        meta = _derive_chunk_metadata(chunk, guide_meta)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_SANITATION_SYSTEM)
        self.assertIn("latrine_design", tags)
        self.assertIn("wash_station", tags)

    def test_glassmaking_guide_detects_glass_metadata(self):
        guide = GuideRecord(
            guide_id="GD-123",
            slug="glass-optics-ceramics",
            title="Glass, Optics & Ceramics",
            source_file="glass-optics-ceramics.md",
            description="Glassmaking raw materials, furnaces, forming, and annealing.",
            category="crafts",
            tags="glass,annealing,ceramics",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_GLASSMAKING)
        self.assertIn("glassmaking", tags)
        self.assertIn("annealing", tags)

    def test_fair_trial_guide_detects_trial_metadata(self):
        guide = GuideRecord(
            guide_id="GD-777",
            slug="criminal-justice-procedures-small-communities",
            title="Criminal Justice Procedures for Small Communities",
            source_file="criminal-justice.md",
            description="Trial procedure, evidence standards, hearing order, and appeal.",
            category="society",
            tags="trial,evidence,appeal",
        )

        meta = _derive_guide_metadata(guide)
        tags = set(meta.topic_tags.split(",")) if meta.topic_tags else set()

        self.assertEqual(meta.structure_type, STRUCTURE_TYPE_FAIR_TRIAL)
        self.assertIn("trial_procedure", tags)
        self.assertIn("evidence_rules", tags)


if __name__ == "__main__":
    unittest.main()
