import unittest
from pathlib import Path

from scripts import abstain_regression_panel as panel_script


class AbstainRegressionPanelTests(unittest.TestCase):
    def test_normalize_panel_payload_preserves_valid_list_panel(self):
        rows = [
            {
                "id": "AB-001",
                "question": "Is this unrelated?",
                "bucket": "off_topic",
                "expected_abstain": True,
                "extra": "kept",
            }
        ]

        metadata, panel = panel_script._normalize_panel_payload(Path("panel.json"), rows)

        self.assertEqual(metadata, {})
        self.assertEqual(panel, rows)

    def test_normalize_panel_payload_preserves_valid_mapping_panel_metadata(self):
        rows = [
            {
                "id": "AB-002",
                "question": "What should I do for a minor burn?",
                "bucket": "supported",
                "expected_abstain": False,
            }
        ]

        metadata, panel = panel_script._normalize_panel_payload(
            Path("burn_panel.yaml"),
            {
                "panel_id": "burn-panel",
                "title": "Burn Panel",
                "description": "Focused burn questions.",
                "queries": rows,
            },
        )

        self.assertEqual(metadata["panel_id"], "burn-panel")
        self.assertEqual(metadata["title"], "Burn Panel")
        self.assertEqual(metadata["description"], "Focused burn questions.")
        self.assertEqual(panel, rows)

    def test_normalize_panel_payload_rejects_non_mapping_rows(self):
        with self.assertRaisesRegex(ValueError, "row 2 must be a mapping"):
            panel_script._normalize_panel_payload(
                Path("panel.json"),
                [
                    {
                        "id": "AB-001",
                        "question": "Valid row.",
                        "bucket": "off_topic",
                        "expected_abstain": True,
                    },
                    "not a row",
                ],
            )

    def test_normalize_panel_payload_rejects_missing_required_row_fields(self):
        with self.assertRaisesRegex(
            ValueError, "row 1 is missing required field\\(s\\): bucket, expected_abstain"
        ):
            panel_script._normalize_panel_payload(
                Path("panel.yaml"),
                {
                    "queries": [
                        {
                            "id": "AB-003",
                            "question": "Missing fields.",
                        }
                    ]
                },
            )

    def test_normalize_panel_payload_rejects_blank_string_fields(self):
        with self.assertRaisesRegex(
            ValueError, "row 1 must include non-empty string field\\(s\\): question"
        ):
            panel_script._normalize_panel_payload(
                Path("panel.yaml"),
                [
                    {
                        "id": "AB-004",
                        "question": "   ",
                        "bucket": "off_topic",
                        "expected_abstain": True,
                    }
                ],
            )

    def test_normalize_panel_payload_rejects_non_boolean_expected_abstain(self):
        with self.assertRaisesRegex(
            ValueError, "row 1 field expected_abstain must be a boolean"
        ):
            panel_script._normalize_panel_payload(
                Path("panel.yaml"),
                [
                    {
                        "id": "AB-005",
                        "question": "Valid text.",
                        "bucket": "off_topic",
                        "expected_abstain": "true",
                    }
                ],
            )


if __name__ == "__main__":
    unittest.main()
