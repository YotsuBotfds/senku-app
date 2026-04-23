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


if __name__ == "__main__":
    unittest.main()
