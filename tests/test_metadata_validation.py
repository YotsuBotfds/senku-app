import json
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace

import metadata_validation as mv


class MetadataValidationTests(unittest.TestCase):
    def test_coerce_guide_record_accepts_mapping_id_fallback_and_strips(self):
        record = {
            "id": " GD-001 ",
            "slug": " wound-care ",
            "title": " Wound Care ",
            "category": " medical ",
            "description": " clean wounds ",
            "source_file": " guides/wound.md ",
        }

        self.assertEqual(
            {
                "guide_id": "GD-001",
                "slug": "wound-care",
                "title": "Wound Care",
                "category": "medical",
                "description": "clean wounds",
                "source_file": "guides/wound.md",
            },
            mv.coerce_guide_record(record),
        )

    def test_coerce_guide_record_preserves_false_like_guide_id(self):
        record = {
            "guide_id": 0,
            "id": "fallback-id",
            "slug": "zero-id",
            "title": "Zero ID",
            "category": "utility",
            "description": "False-like scalar id",
            "source_file": "guides/zero.md",
        }

        self.assertEqual("0", mv.coerce_guide_record(record)["guide_id"])

    def test_coerce_guide_record_treats_collection_metadata_as_missing(self):
        record = {
            "guide_id": [],
            "id": "fallback-id",
            "slug": ["not", "a", "slug"],
            "title": {"text": "Title"},
            "category": (),
            "description": set(),
            "source_file": "guides/malformed.md",
        }

        self.assertEqual(
            {
                "guide_id": "fallback-id",
                "slug": "",
                "title": "",
                "category": "",
                "description": "",
                "source_file": "guides/malformed.md",
            },
            mv.coerce_guide_record(record),
        )

    def test_coerce_guide_record_accepts_object_attrs(self):
        record = SimpleNamespace(
            guide_id="GD-002",
            slug="water",
            title="Water",
            category="utility",
            description="Store water",
            source_file="guides/water.md",
        )

        self.assertEqual(
            {
                "guide_id": "GD-002",
                "slug": "water",
                "title": "Water",
                "category": "utility",
                "description": "Store water",
                "source_file": "guides/water.md",
            },
            mv.coerce_guide_record(record),
        )

    def test_validate_guide_records_truncates_errors_but_counts_all(self):
        records = [{"guide_id": f"GD-{index:03d}"} for index in range(3)]

        report = mv.validate_guide_records(
            records,
            scope="unit",
            max_emitted_errors=2,
        )

        self.assertEqual("fail", report["status"])
        self.assertEqual(3, report["summary"]["guides_checked"])
        self.assertEqual(3, report["summary"]["guide_errors"])
        self.assertEqual(2, report["summary"]["errors_emitted"])
        self.assertTrue(report["summary"]["errors_truncated"])
        self.assertEqual(2, len(report["errors"]))
        self.assertEqual(
            ["slug", "title", "category", "description", "source_file"],
            report["errors"][0]["missing_fields"],
        )

    def test_validate_guide_records_flags_collection_required_fields(self):
        report = mv.validate_guide_records(
            [
                {
                    "guide_id": "GD-004",
                    "slug": {"value": "bad"},
                    "title": ["Bad Title"],
                    "category": "medical",
                    "description": "Malformed scalar metadata",
                    "source_file": "guides/malformed.md",
                }
            ],
            scope="unit",
        )

        self.assertEqual("fail", report["status"])
        self.assertEqual(1, report["summary"]["guide_errors"])
        self.assertEqual(["slug", "title"], report["errors"][0]["missing_fields"])

    def test_report_has_errors_and_format_validation_errors(self):
        report = mv.validate_guide_records(
            [{"guide_id": "", "source_file": ""}],
            scope="unit",
            max_emitted_errors=1,
        )

        self.assertTrue(mv.report_has_errors(report))
        self.assertFalse(mv.report_has_errors({"status": "pass"}))

        text = mv.format_validation_errors(report)
        self.assertIn("Metadata validation failed.", text)
        self.assertIn("scope=unit", text)
        self.assertIn("guide_errors=1", text)
        self.assertIn("<unknown> (<missing-id>): missing", text)
        self.assertIn("guide_id", text)

    def test_write_validation_report_creates_parent_and_json(self):
        report = mv.validate_guide_records(
            [
                {
                    "guide_id": "GD-003",
                    "slug": "shelter",
                    "title": "Shelter",
                    "category": "planning",
                    "description": "Find shelter",
                    "source_file": "guides/shelter.md",
                }
            ],
            scope="unit",
        )

        with tempfile.TemporaryDirectory() as temp_dir:
            destination = Path(temp_dir) / "nested" / mv.REPORT_FILENAME

            written = mv.write_validation_report(report, destination)

            self.assertEqual(destination, written)
            loaded = json.loads(destination.read_text(encoding="utf-8"))
            self.assertEqual("pass", loaded["status"])
            self.assertEqual("unit", loaded["scope"])
            self.assertEqual(mv.REPORT_TYPE, loaded["report_type"])


if __name__ == "__main__":
    unittest.main()
