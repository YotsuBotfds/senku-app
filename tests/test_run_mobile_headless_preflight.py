import unittest

from scripts.run_mobile_headless_preflight import (
    build_audit_regressions,
    build_global_regression_queue,
    build_global_review_queue,
)


class MobileHeadlessPreflightTests(unittest.TestCase):
    def test_build_global_review_queue_uses_all_guide_counts(self):
        audit_index = {
            "soapmaking": {
                "family": "soapmaking",
                "guide_counts": [
                    {"guide_id": "GD-100", "guide_title": "Soap Guide", "count": 4},
                    {"guide_id": "GD-200", "guide_title": "Chemistry Guide", "count": 2},
                ],
            },
            "message_auth": {
                "family": "message_auth",
                "guide_counts": [
                    {"guide_id": "GD-100", "guide_title": "Soap Guide", "count": 3},
                ],
            },
        }

        queue = build_global_review_queue(audit_index, limit=5)

        self.assertEqual(queue[0]["guide_id"], "GD-100")
        self.assertEqual(queue[0]["total_mismatches"], 7)
        self.assertEqual(len(queue[0]["families"]), 2)

    def test_build_audit_regressions_sorts_highest_delta_first(self):
        current_index = {
            "soapmaking": {"mismatch_rows": 12, "matched_rows": 40},
            "message_auth": {"mismatch_rows": 3, "matched_rows": 10},
        }
        baseline_index = {
            "soapmaking": {"mismatch_rows": 5, "matched_rows": 40},
            "message_auth": {"mismatch_rows": 4, "matched_rows": 10},
        }

        regressions = build_audit_regressions(current_index, baseline_index)

        self.assertEqual(regressions[0]["family"], "soapmaking")
        self.assertEqual(regressions[0]["delta_mismatch_rows"], 7)
        self.assertEqual(regressions[1]["family"], "message_auth")
        self.assertEqual(regressions[1]["delta_mismatch_rows"], -1)

    def test_build_global_regression_queue_highlights_guides_that_got_worse(self):
        current_index = {
            "soapmaking": {
                "guide_counts": [
                    {"guide_id": "GD-100", "guide_title": "Soap Guide", "count": 6},
                    {"guide_id": "GD-200", "guide_title": "Chemistry Guide", "count": 1},
                ]
            },
            "message_auth": {
                "guide_counts": [
                    {"guide_id": "GD-100", "guide_title": "Soap Guide", "count": 4},
                ]
            },
        }
        baseline_index = {
            "soapmaking": {
                "guide_counts": [
                    {"guide_id": "GD-100", "guide_title": "Soap Guide", "count": 2},
                    {"guide_id": "GD-200", "guide_title": "Chemistry Guide", "count": 1},
                ]
            },
            "message_auth": {
                "guide_counts": [
                    {"guide_id": "GD-100", "guide_title": "Soap Guide", "count": 1},
                ]
            },
        }

        queue = build_global_regression_queue(current_index, baseline_index, limit=5)

        self.assertEqual(queue[0]["guide_id"], "GD-100")
        self.assertEqual(queue[0]["total_delta"], 7)
        self.assertEqual(
            [family["family"] for family in queue[0]["families"]],
            ["message_auth", "soapmaking"],
        )


if __name__ == "__main__":
    unittest.main()
