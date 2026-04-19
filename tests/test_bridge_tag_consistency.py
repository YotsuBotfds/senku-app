import unittest

import ingest


class BridgeTagConsistencyTests(unittest.TestCase):
    def _collect_corpus_warnings(self):
        records = []
        for filepath in ingest.collect_markdown_files():
            meta, _chunks = ingest.process_file(filepath)
            if meta is None:
                continue
            records.append(
                {
                    "guide_id": meta.get("id", ""),
                    "source_file": filepath,
                    "bridge": ingest.derive_bridge_metadata(
                        meta.get("tags", []),
                        explicit_bridge=meta.get("bridge", False),
                    )["bridge"],
                    "tags": meta.get("tags", []),
                }
            )
        return ingest._bridge_tag_consistency_warnings(records)

    def test_warns_when_bridge_flag_and_stale_bridge_tag_both_exist(self):
        warnings = ingest._bridge_tag_consistency_warnings(
            [
                {
                    "guide_id": "GD-bridge",
                    "bridge": True,
                    "tags": ["planning", "bridge-guide"],
                }
            ]
        )

        self.assertEqual(
            warnings,
            [
                "bridge-tag inconsistency: GD-bridge has bridge=True and "
                "tag_has_bridge_guide=True"
            ],
        )

    def test_clean_bridge_record_emits_no_warning(self):
        warnings = ingest._bridge_tag_consistency_warnings(
            [
                {
                    "guide_id": "GD-clean",
                    "bridge": True,
                    "tags": ["planning"],
                }
            ]
        )

        self.assertEqual(warnings, [])

    def test_corpus_has_no_bridge_tag_inconsistencies(self):
        warnings = self._collect_corpus_warnings()
        self.assertEqual([], warnings, "\n".join(warnings))


if __name__ == "__main__":
    unittest.main()
