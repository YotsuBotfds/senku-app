import unittest

from bench import _build_generation_time_summary


class BenchSummaryTests(unittest.TestCase):
    def test_success_only_average_excludes_failed_generation_time(self):
        results_map = {
            0: (
                "Prompt A",
                "Answer A",
                {},
                {"generation_time": 6.0},
            ),
            1: (
                "Prompt B",
                "",
                {},
                {"generation_time": 4.0, "error": "boom"},
            ),
        }

        summary = _build_generation_time_summary(results_map, total_time=10.0, prompt_count=2)

        self.assertEqual(summary["success_count"], 1)
        self.assertEqual(summary["error_count"], 1)
        self.assertEqual(summary["successful_generation_time"], 6.0)
        self.assertEqual(summary["avg_generation_time"], 5.0)
        self.assertEqual(summary["avg_generation_time_success_only"], 6.0)

    def test_generation_time_summary_treats_malformed_rows_as_errors(self):
        results_map = {
            0: (
                "Prompt A",
                "Answer A",
                {},
                {"generation_time": "2.5"},
            ),
            1: ("Prompt B", "Answer B"),
            2: (
                "Prompt C",
                "Answer C",
                {},
                ["not", "metadata"],
            ),
            3: (
                "Prompt D",
                "Answer D",
                {},
                {"generation_time": "not-a-number"},
            ),
        }

        summary = _build_generation_time_summary(results_map, total_time=8.0, prompt_count=4)

        self.assertEqual(summary["success_count"], 2)
        self.assertEqual(summary["error_count"], 2)
        self.assertEqual(summary["successful_generation_time"], 2.5)
        self.assertEqual(summary["avg_generation_time"], 2.0)
        self.assertEqual(summary["avg_generation_time_success_only"], 1.2)


if __name__ == "__main__":
    unittest.main()
