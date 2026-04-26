#!/usr/bin/env python3
"""Aggregate final-mode telemetry from Android logcat files."""

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter
from pathlib import Path


FINAL_MODE_RE = re.compile(
    r'ask\.generate final_mode=(?P<final_mode>\S+) '
    r'route=(?P<route>\S+) '
    r'query="(?P<query>.*?)" '
    r'totalElapsedMs=(?P<total_elapsed_ms>\d+)'
)
LOW_COVERAGE_RE = re.compile(
    r'ask\.generate low_coverage_route '
    r'query="(?P<query>.*?)" '
    r'mode=(?P<mode>\S+)'
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Aggregate ask.generate final_mode telemetry from one or more logcats."
    )
    parser.add_argument(
        "--logcat-path",
        action="append",
        dest="logcat_paths",
        required=True,
        help="Path to a logcat file. Repeat the flag for multiple files.",
    )
    parser.add_argument(
        "--output-dir",
        required=True,
        help="Directory where summary.json and summary.md will be written.",
    )
    return parser.parse_args()


def escape_markdown_cell(value: object) -> str:
    text = str(value).replace("\r\n", " ").replace("\r", " ").replace("\n", " ").replace("\t", " ")
    return text.replace("|", r"\|")


def sort_counter(counter: Counter[str]) -> list[tuple[str, int]]:
    return sorted(counter.items(), key=lambda item: (-item[1], item[0]))


def write_json(path: Path, payload: dict) -> None:
    path.write_text(json.dumps(payload, indent=2, sort_keys=True, ensure_ascii=True) + "\n", encoding="utf-8")


def write_markdown(path: Path, payload: dict) -> None:
    lines: list[str] = [
        "# Final Mode Telemetry Summary",
        "",
        "## Inputs",
        "",
    ]

    for logcat_path in payload["input_logcat_paths"]:
        lines.append(f"- `{logcat_path}`")

    lines.extend(
        [
            "",
            "## Counts",
            "",
            f"- Raw `final_mode` lines found: {payload['total_final_mode_lines_found']}",
            (
                "- Unique `final_mode` events: "
                f"{payload['total_unique_final_mode_events']} "
                "(deduped identical D/I twin emissions)"
            ),
            "",
            "## Final Mode Counts",
            "",
            "| final_mode | count |",
            "| --- | ---: |",
        ]
    )

    for final_mode, count in payload["counts_by_final_mode"].items():
        lines.append(f"| {escape_markdown_cell(final_mode)} | {count} |")

    lines.extend(
        [
            "",
            "## Route Counts",
            "",
            "| route | count |",
            "| --- | ---: |",
        ]
    )

    for route, count in payload["counts_by_route"].items():
        lines.append(f"| {escape_markdown_cell(route)} | {count} |")

    lines.extend(
        [
            "",
            "## Per-Query Rows",
            "",
            "| query | final_mode | route | totalElapsedMs | source_logcat_path |",
            "| --- | --- | --- | ---: | --- |",
        ]
    )

    for row in payload["per_query_rows"]:
        lines.append(
            "| {query} | {final_mode} | {route} | {total_elapsed_ms} | {source_logcat_path} |".format(
                query=escape_markdown_cell(row["query"]),
                final_mode=escape_markdown_cell(row["final_mode"]),
                route=escape_markdown_cell(row["route"]),
                total_elapsed_ms=row["total_elapsed_ms"],
                source_logcat_path=escape_markdown_cell(row["source_logcat_path"]),
            )
        )

    lines.extend(
        [
            "",
            "## Low Coverage Route Lines",
            "",
            f"- Raw `low_coverage_route` lines found: {payload['total_low_coverage_route_lines_found']}",
        ]
    )

    if payload["low_coverage_route_rows"]:
        lines.extend(
            [
                "",
                "| query | mode | source_logcat_path |",
                "| --- | --- | --- |",
            ]
        )
        for row in payload["low_coverage_route_rows"]:
            lines.append(
                "| {query} | {mode} | {source_logcat_path} |".format(
                    query=escape_markdown_cell(row["query"]),
                    mode=escape_markdown_cell(row["mode"]),
                    source_logcat_path=escape_markdown_cell(row["source_logcat_path"]),
                )
            )
    else:
        lines.extend(["", "No `low_coverage_route` lines found."])

    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    args = parse_args()
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    input_logcat_paths = [str(Path(logcat_path)) for logcat_path in args.logcat_paths]
    raw_final_mode_line_count = 0
    raw_low_coverage_line_count = 0
    deduped_final_mode_rows: dict[tuple[str, str, str, str, int], dict[str, object]] = {}
    deduped_low_coverage_rows: dict[tuple[str, str, str], dict[str, str]] = {}

    for logcat_path_str in input_logcat_paths:
        logcat_path = Path(logcat_path_str)
        for line in logcat_path.read_text(encoding="utf-8", errors="replace").splitlines():
            final_mode_match = FINAL_MODE_RE.search(line)
            if final_mode_match:
                raw_final_mode_line_count += 1
                row = {
                    "final_mode": final_mode_match.group("final_mode"),
                    "route": final_mode_match.group("route"),
                    "query": final_mode_match.group("query"),
                    "total_elapsed_ms": int(final_mode_match.group("total_elapsed_ms")),
                    "source_logcat_path": logcat_path_str,
                }
                row_key = (
                    row["source_logcat_path"],
                    row["final_mode"],
                    row["route"],
                    row["query"],
                    row["total_elapsed_ms"],
                )
                deduped_final_mode_rows[row_key] = row
                continue

            low_coverage_match = LOW_COVERAGE_RE.search(line)
            if low_coverage_match:
                raw_low_coverage_line_count += 1
                row = {
                    "query": low_coverage_match.group("query"),
                    "mode": low_coverage_match.group("mode"),
                    "source_logcat_path": logcat_path_str,
                }
                row_key = (
                    row["source_logcat_path"],
                    row["query"],
                    row["mode"],
                )
                deduped_low_coverage_rows[row_key] = row

    if raw_final_mode_line_count == 0:
        print("No ask.generate final_mode= lines found.", file=sys.stderr)
        return 1

    final_mode_rows = sorted(
        deduped_final_mode_rows.values(),
        key=lambda row: (row["source_logcat_path"], row["query"], row["final_mode"], row["route"]),
    )
    low_coverage_rows = sorted(
        deduped_low_coverage_rows.values(),
        key=lambda row: (row["source_logcat_path"], row["query"], row["mode"]),
    )

    final_mode_counts = Counter(row["final_mode"] for row in final_mode_rows)
    route_counts = Counter(row["route"] for row in final_mode_rows)

    payload = {
        "input_logcat_paths": input_logcat_paths,
        "total_final_mode_lines_found": raw_final_mode_line_count,
        "total_unique_final_mode_events": len(final_mode_rows),
        "counts_by_final_mode": dict(sort_counter(final_mode_counts)),
        "counts_by_route": dict(sort_counter(route_counts)),
        "per_query_rows": final_mode_rows,
        "total_low_coverage_route_lines_found": raw_low_coverage_line_count,
        "low_coverage_route_rows": low_coverage_rows,
    }

    write_json(output_dir / "summary.json", payload)
    write_markdown(output_dir / "summary.md", payload)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
