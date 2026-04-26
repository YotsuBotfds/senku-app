import unittest

from metadata_helpers import (
    BRIDGE_GUIDE_TAG,
    derive_bridge_metadata,
    normalize_metadata_tag,
    normalize_tag_value,
    normalize_tags,
)


class MetadataHelperTests(unittest.TestCase):
    def test_normalize_metadata_tag_lowercases_text(self):
        self.assertEqual("water-storage", normalize_metadata_tag("Water Storage"))

    def test_normalize_metadata_tag_strips_whitespace(self):
        self.assertEqual("planning", normalize_metadata_tag("  planning  "))

    def test_normalize_metadata_tag_handles_punctuation(self):
        self.assertEqual(
            "healthcare-systems-planning",
            normalize_metadata_tag("Healthcare Systems / Planning"),
        )

    def test_normalize_metadata_tag_folds_unicode_to_ascii(self):
        self.assertEqual("cafe-etude", normalize_metadata_tag("Café Étude"))

    def test_normalize_metadata_tag_is_idempotent(self):
        once = normalize_metadata_tag("Long term / Planning")
        self.assertEqual(once, normalize_metadata_tag(once))

    def test_normalize_tag_value_delegates_to_canonical_helper(self):
        self.assertEqual(
            normalize_metadata_tag("Healthcare Systems / Planning"),
            normalize_tag_value("Healthcare Systems / Planning"),
        )

    def test_normalize_tags_deduplicates_and_normalizes_csv_values(self):
        self.assertEqual(
            ["water-storage", "planning", "long-term"],
            normalize_tags("Water Storage, planning, long term, water_storage"),
        )

    def test_normalize_tags_ignores_nested_malformed_values(self):
        self.assertEqual(
            ["planning", "water-storage"],
            normalize_tags(
                [
                    "planning",
                    {"tag": "bridge-guide"},
                    ["nested"],
                    ("tuple",),
                    "water storage",
                ]
            ),
        )

    def test_derive_bridge_metadata_filters_stale_bridge_tag_from_tags(self):
        bridge_metadata = derive_bridge_metadata(["planning", BRIDGE_GUIDE_TAG])

        self.assertTrue(bridge_metadata["bridge"])
        self.assertEqual(["planning"], bridge_metadata["normalized_tags"])
        self.assertTrue(bridge_metadata["had_bridge_tag"])
        self.assertFalse(bridge_metadata["inconsistent"])

    def test_explicit_bridge_without_stale_tag_stays_consistent(self):
        bridge_metadata = derive_bridge_metadata(["planning"], explicit_bridge=True)

        self.assertTrue(bridge_metadata["bridge"])
        self.assertEqual(["planning"], bridge_metadata["normalized_tags"])
        self.assertFalse(bridge_metadata["had_bridge_tag"])
        self.assertFalse(bridge_metadata["inconsistent"])


if __name__ == "__main__":
    unittest.main()
