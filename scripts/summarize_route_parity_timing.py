#!/usr/bin/env python3
"""Summarize SenkuRouteParity timing breadcrumbs from Android output."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


INSTALL_RE = re.compile(r"install current-head route parity pack took (?P<ms>\d+)ms")
OPEN_RE = re.compile(r"open current-head route parity repository took (?P<ms>\d+)ms")
ROUTE_RE = re.compile(
    r'(?P<stage>search|context|total) '
    r"(?P<expected_structure>\S+) "
    r'route: "(?P<query>.*?)" '
    r"took (?P<ms>\d+)ms"
)
SLOW_SEARCH_MS = 5000
SLOW_CONTEXT_MS = 5000
SLOW_TOTAL_MS = 10000


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Parse SenkuRouteParity logcat/instrumentation timing breadcrumbs."
    )
    parser.add_argument(
        "--input",
        required=True,
        help="Path to a logcat or instrumentation output file containing SenkuRouteParity lines.",
    )
    parser.add_argument(
        "--output-json",
        help="Optional path for the JSON summary. If omitted with no Markdown output, JSON prints to stdout.",
    )
    parser.add_argument(
        "--output-md",
        help="Optional path for a Markdown summary.",
    )
    return parser.parse_args()


def escape_markdown_cell(value: object) -> str:
    text = str(value).replace("\r\n", " ").replace("\r", " ").replace("\n", " ").replace("\t", " ")
    return text.replace("|", r"\|")


def _empty_summary(input_path: Path) -> dict[str, Any]:
    return {
        "input_path": str(input_path),
        "raw_timing_lines_found": 0,
        "unique_timing_breadcrumbs": 0,
        "install_ms": None,
        "repository_open_ms": None,
        "routes": [],
        "slow_routes": [],
        "slow_thresholds": {
            "search_ms": SLOW_SEARCH_MS,
            "context_ms": SLOW_CONTEXT_MS,
            "total_ms": SLOW_TOTAL_MS,
        },
        "warnings": [],
    }


def _set_scalar(summary: dict[str, Any], key: str, value: int, line_number: int) -> None:
    previous = summary.get(key)
    if previous is not None and previous != value:
        summary["warnings"].append(
            f"line {line_number}: replacing {key}={previous} with {value}"
        )
    summary[key] = value


def _route_for(routes: dict[tuple[str, str], dict[str, Any]], expected_structure: str, query: str) -> dict[str, Any]:
    key = (expected_structure, query)
    if key not in routes:
        routes[key] = {
            "expectedStructure": expected_structure,
            "query": query,
            "search_ms": None,
            "context_ms": None,
            "total_ms": None,
        }
    return routes[key]


def parse_route_parity_timing(input_path: Path) -> dict[str, Any]:
    summary = _empty_summary(input_path)
    routes: dict[tuple[str, str], dict[str, Any]] = {}
    seen_breadcrumbs: set[str] = set()

    for line_number, line in enumerate(
        input_path.read_text(encoding="utf-8", errors="replace").splitlines(),
        start=1,
    ):
        install_match = INSTALL_RE.search(line)
        open_match = OPEN_RE.search(line)
        route_match = ROUTE_RE.search(line)
        if not (install_match or open_match or route_match):
            continue

        summary["raw_timing_lines_found"] += 1
        breadcrumb = (
            install_match.group(0)
            if install_match
            else open_match.group(0)
            if open_match
            else route_match.group(0)
        )
        if breadcrumb in seen_breadcrumbs:
            continue
        seen_breadcrumbs.add(breadcrumb)

        if install_match:
            _set_scalar(summary, "install_ms", int(install_match.group("ms")), line_number)
            continue
        if open_match:
            _set_scalar(summary, "repository_open_ms", int(open_match.group("ms")), line_number)
            continue

        assert route_match is not None
        stage = route_match.group("stage")
        route = _route_for(
            routes,
            route_match.group("expected_structure"),
            route_match.group("query"),
        )
        key = f"{stage}_ms"
        value = int(route_match.group("ms"))
        previous = route.get(key)
        if previous is not None and previous != value:
            summary["warnings"].append(
                f"line {line_number}: replacing {key}={previous} with {value} for {route['query']!r}"
            )
        route[key] = value

    summary["unique_timing_breadcrumbs"] = len(seen_breadcrumbs)
    summary["routes"] = sorted(routes.values(), key=lambda row: (row["expectedStructure"], row["query"]))
    summary["slow_routes"] = slow_route_warnings(summary["routes"])
    return summary


def slow_route_warnings(routes: list[dict[str, Any]]) -> list[dict[str, Any]]:
    warnings = []
    thresholds = {
        "search_ms": SLOW_SEARCH_MS,
        "context_ms": SLOW_CONTEXT_MS,
        "total_ms": SLOW_TOTAL_MS,
    }
    for row in routes:
        exceeded = {}
        for key, threshold in thresholds.items():
            value = row.get(key)
            if value is not None and value > threshold:
                exceeded[key] = value
        if exceeded:
            warnings.append(
                {
                    "expectedStructure": row["expectedStructure"],
                    "query": row["query"],
                    "exceeded": exceeded,
                }
            )
    return warnings


def write_json(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True, ensure_ascii=True) + "\n", encoding="utf-8")


def write_markdown(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    lines = [
        "# SenkuRouteParity Timing Summary",
        "",
        f"- Input: `{payload['input_path']}`",
        f"- Raw timing lines found: {payload['raw_timing_lines_found']}",
        f"- Unique timing breadcrumbs: {payload['unique_timing_breadcrumbs']}",
        f"- Install ms: {payload['install_ms'] if payload['install_ms'] is not None else 'n/a'}",
        (
            "- Repository open ms: "
            f"{payload['repository_open_ms'] if payload['repository_open_ms'] is not None else 'n/a'}"
        ),
        "",
        "| expectedStructure | query | search_ms | context_ms | total_ms |",
        "| --- | --- | ---: | ---: | ---: |",
    ]
    for row in payload["routes"]:
        lines.append(
            "| {expectedStructure} | {query} | {search_ms} | {context_ms} | {total_ms} |".format(
                expectedStructure=escape_markdown_cell(row["expectedStructure"]),
                query=escape_markdown_cell(row["query"]),
                search_ms=row["search_ms"] if row["search_ms"] is not None else "n/a",
                context_ms=row["context_ms"] if row["context_ms"] is not None else "n/a",
                total_ms=row["total_ms"] if row["total_ms"] is not None else "n/a",
            )
        )

    if payload["slow_routes"]:
        lines.extend(
            [
                "",
                "## Slow Route Warnings",
                "",
                (
                    f"Non-failing thresholds: search_ms > {payload['slow_thresholds']['search_ms']}, "
                    f"context_ms > {payload['slow_thresholds']['context_ms']}, "
                    f"total_ms > {payload['slow_thresholds']['total_ms']}."
                ),
                "",
                "| expectedStructure | query | exceeded |",
                "| --- | --- | --- |",
            ]
        )
        for row in payload["slow_routes"]:
            exceeded = ", ".join(f"{key}={value}" for key, value in sorted(row["exceeded"].items()))
            lines.append(
                "| {expectedStructure} | {query} | {exceeded} |".format(
                    expectedStructure=escape_markdown_cell(row["expectedStructure"]),
                    query=escape_markdown_cell(row["query"]),
                    exceeded=escape_markdown_cell(exceeded),
                )
            )

    if payload["warnings"]:
        lines.extend(["", "## Warnings", ""])
        lines.extend(f"- {escape_markdown_cell(warning)}" for warning in payload["warnings"])

    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    args = parse_args()
    input_path = Path(args.input)
    summary = parse_route_parity_timing(input_path)
    if summary["raw_timing_lines_found"] == 0:
        print("No SenkuRouteParity timing breadcrumbs found.", file=sys.stderr)
        return 1

    if args.output_json:
        write_json(Path(args.output_json), summary)
    if args.output_md:
        write_markdown(Path(args.output_md), summary)
    if not args.output_json and not args.output_md:
        print(json.dumps(summary, indent=2, sort_keys=True, ensure_ascii=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
