import unittest

import query
from ingest_freshness import (
    ABSENT_OR_INVALID,
    FRESH,
    INCOMPLETE_UNTRUSTED,
    STALE,
    IngestFreshnessReport,
)


class QueryIngestFreshnessPreflightTests(unittest.TestCase):
    def test_stale_ingest_blocks_without_override(self):
        action, message = query._ingest_freshness_preflight_message(
            IngestFreshnessReport(status=STALE, message="Ingest manifest is stale."),
            allow_stale_ingest=False,
        )

        self.assertEqual(action, "block")
        self.assertIn("Run ingest.py --rebuild before querying", message)
        self.assertIn("--allow-stale-ingest", message)

    def test_stale_ingest_warns_with_override(self):
        action, message = query._ingest_freshness_preflight_message(
            IngestFreshnessReport(status=STALE, message="Ingest manifest is stale."),
            allow_stale_ingest=True,
        )

        self.assertEqual(action, "warn")
        self.assertIn("Continuing because --allow-stale-ingest was set", message)

    def test_untrusted_or_absent_manifest_warns_without_blocking(self):
        for status in (INCOMPLETE_UNTRUSTED, ABSENT_OR_INVALID):
            with self.subTest(status=status):
                action, message = query._ingest_freshness_preflight_message(
                    IngestFreshnessReport(
                        status=status,
                        message=f"Freshness status is {status}.",
                    )
                )

                self.assertEqual(action, "warn")
                self.assertIn("enable strict freshness checks", message)

    def test_fresh_ingest_continues_silently(self):
        action, message = query._ingest_freshness_preflight_message(
            IngestFreshnessReport(
                status=FRESH,
                message="Ingest manifest matches current guide files.",
            )
        )

        self.assertEqual(action, "ok")
        self.assertEqual(message, "")


if __name__ == "__main__":
    unittest.main()
