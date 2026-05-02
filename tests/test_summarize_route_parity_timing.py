import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from scripts import summarize_route_parity_timing as timing


class SummarizeRouteParityTimingTests(unittest.TestCase):
    def test_parse_logcat_and_instrumentation_timing_breadcrumbs(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            log = Path(tmpdir) / "route_parity.log"
            log.write_text(
                "\n".join(
                    [
                        "05-01 10:00:00.000 I/SenkuRouteParity(123): install current-head route parity pack took 111ms",
                        "INSTRUMENTATION_STATUS: route_parity_timing=install current-head route parity pack took 111ms",
                        "05-01 10:00:00.100 I/SenkuRouteParity(123): open current-head route parity repository took 22ms",
                        '05-01 10:00:01.000 I/SenkuRouteParity(123): search cabin_house route: "how do i build a house" took 333ms',
                        'INSTRUMENTATION_STATUS: route_parity_timing=context cabin_house route: "how do i build a house" took 44ms',
                        'INSTRUMENTATION_STATUS: route_parity_timing=total cabin_house route: "how do i build a house" took 400ms',
                        '05-01 10:00:02.000 I/SenkuRouteParity(123): search water_distribution route: "how do i design a gravity-fed water distribution system with storage tanks" took 555ms',
                        '05-01 10:00:02.200 I/SenkuRouteParity(123): context water_distribution route: "how do i design a gravity-fed water distribution system with storage tanks" took 66ms',
                        '05-01 10:00:02.500 I/SenkuRouteParity(123): total water_distribution route: "how do i design a gravity-fed water distribution system with storage tanks" took 700ms',
                    ]
                )
                + "\n",
                encoding="utf-8",
            )

            summary = timing.parse_route_parity_timing(log)

        self.assertEqual(summary["raw_timing_lines_found"], 9)
        self.assertEqual(summary["unique_timing_breadcrumbs"], 8)
        self.assertEqual(summary["install_ms"], 111)
        self.assertEqual(summary["repository_open_ms"], 22)
        self.assertEqual(
            summary["slow_thresholds"],
            {"search_ms": 5000, "context_ms": 5000, "total_ms": 10000},
        )
        self.assertEqual(summary["slow_routes"], [])
        self.assertEqual(
            summary["routes"],
            [
                {
                    "expectedStructure": "cabin_house",
                    "query": "how do i build a house",
                    "search_ms": 333,
                    "context_ms": 44,
                    "total_ms": 400,
                },
                {
                    "expectedStructure": "water_distribution",
                    "query": "how do i design a gravity-fed water distribution system with storage tanks",
                    "search_ms": 555,
                    "context_ms": 66,
                    "total_ms": 700,
                },
            ],
        )

    def test_main_writes_json_and_markdown_outputs(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            log = root / "route_parity.log"
            output_json = root / "summary.json"
            output_md = root / "summary.md"
            log.write_text(
                "\n".join(
                    [
                        "I/SenkuRouteParity: install current-head route parity pack took 10ms",
                        "I/SenkuRouteParity: open current-head route parity repository took 20ms",
                        'I/SenkuRouteParity: search soapmaking route: "how do i make soap | safely" took 30ms',
                        'I/SenkuRouteParity: context soapmaking route: "how do i make soap | safely" took 40ms',
                        'I/SenkuRouteParity: total soapmaking route: "how do i make soap | safely" took 50ms',
                    ]
                )
                + "\n",
                encoding="utf-8",
            )

            with patch(
                "sys.argv",
                [
                    "summarize_route_parity_timing.py",
                    "--input",
                    str(log),
                    "--output-json",
                    str(output_json),
                    "--output-md",
                    str(output_md),
                ],
            ):
                self.assertEqual(timing.main(), 0)

            summary = json.loads(output_json.read_text(encoding="utf-8"))
            markdown = output_md.read_text(encoding="utf-8")

        self.assertEqual(summary["install_ms"], 10)
        self.assertEqual(summary["routes"][0]["expectedStructure"], "soapmaking")
        self.assertEqual(summary["slow_routes"], [])
        self.assertIn(r"how do i make soap \| safely", markdown)
        self.assertIn("| soapmaking |", markdown)
        self.assertNotIn("Slow Route Warnings", markdown)

    def test_slow_route_warnings_are_non_failing_summary_fields(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            log = root / "route_parity.log"
            output_json = root / "summary.json"
            output_md = root / "summary.md"
            log.write_text(
                "\n".join(
                    [
                        'I/SenkuRouteParity: search cabin_house route: "how do i weatherproof a cabin roof" took 320014ms',
                        'I/SenkuRouteParity: context cabin_house route: "how do i weatherproof a cabin roof" took 12146ms',
                        'I/SenkuRouteParity: total cabin_house route: "how do i weatherproof a cabin roof" took 332284ms',
                    ]
                )
                + "\n",
                encoding="utf-8",
            )

            with patch(
                "sys.argv",
                [
                    "summarize_route_parity_timing.py",
                    "--input",
                    str(log),
                    "--output-json",
                    str(output_json),
                    "--output-md",
                    str(output_md),
                ],
            ):
                self.assertEqual(timing.main(), 0)

            summary = json.loads(output_json.read_text(encoding="utf-8"))
            markdown = output_md.read_text(encoding="utf-8")

        self.assertEqual(
            summary["slow_routes"],
            [
                {
                    "expectedStructure": "cabin_house",
                    "query": "how do i weatherproof a cabin roof",
                    "exceeded": {
                        "context_ms": 12146,
                        "search_ms": 320014,
                        "total_ms": 332284,
                    },
                }
            ],
        )
        self.assertIn("Slow Route Warnings", markdown)
        self.assertIn("search_ms=320014", markdown)
        self.assertIn("context_ms=12146", markdown)
        self.assertIn("total_ms=332284", markdown)

    def test_main_returns_nonzero_without_timing_breadcrumbs(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            log = Path(tmpdir) / "empty.log"
            log.write_text("unrelated log line\n", encoding="utf-8")

            with patch("sys.argv", ["summarize_route_parity_timing.py", "--input", str(log)]):
                self.assertEqual(timing.main(), 1)


if __name__ == "__main__":
    unittest.main()
