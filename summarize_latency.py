#!/usr/bin/env python3
"""Summarize exported AnswerRun latency breakdowns across JSON/JSONL inputs."""

from __future__ import annotations

import argparse
import json
import math
import sys
from pathlib import Path
from typing import Iterable

STAGE_ALIASES = {
    "retrieval": ("retrievalMs", "retrieval_ms"),
    "rerank": ("rerankMs", "rerank_ms"),
    "prompt_build": ("promptBuildMs", "prompt_build_ms", "promptMs", "prompt_ms"),
    "first_token": ("firstTokenMs", "first_token_ms"),
    "decode": ("decodeMs", "decode_ms", "generationMs", "generation_ms"),
    "total": ("totalMs", "total_ms"),
}


def percentile(values: list[float], pct: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    if len(ordered) == 1:
        return ordered[0]
    position = (len(ordered) - 1) * pct
    lower_index = int(math.floor(position))
    upper_index = int(math.ceil(position))
    if lower_index == upper_index:
        return ordered[lower_index]
    lower = ordered[lower_index]
    upper = ordered[upper_index]
    fraction = position - lower_index
    return lower + ((upper - lower) * fraction)


def _first_number(mapping: dict, keys: Iterable[str]) -> float | None:
    for key in keys:
        if key not in mapping:
            continue
        try:
            return float(mapping[key])
        except (TypeError, ValueError):
            return None
    return None


def _extract_latency_breakdown(record: object) -> dict | None:
    if not isinstance(record, dict):
        return None
    for key in ("latencyBreakdown", "latency_breakdown"):
        candidate = record.get(key)
        if isinstance(candidate, dict):
            return candidate
    for nested_key in ("answerRun", "answer_run", "turn", "latest_turn"):
        nested = record.get(nested_key)
        if isinstance(nested, dict):
            nested_breakdown = _extract_latency_breakdown(nested)
            if nested_breakdown is not None:
                return nested_breakdown
    if any(alias in record for aliases in STAGE_ALIASES.values() for alias in aliases):
        return record
    return None


def _iter_records_from_json(path: Path) -> Iterable[dict]:
    try:
        payload = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return
    if isinstance(payload, list):
        for row in payload:
            if isinstance(row, dict):
                yield row
        return
    if isinstance(payload, dict):
        for key in ("answerRuns", "answer_runs", "records", "rows", "turns"):
            value = payload.get(key)
            if isinstance(value, list):
                for row in value:
                    if isinstance(row, dict):
                        yield row
                return
        yield payload


def iter_latency_rows(paths: Iterable[Path]) -> Iterable[dict[str, float]]:
    for path in paths:
        if path.is_dir():
            yield from iter_latency_rows(sorted(p for p in path.rglob("*") if p.suffix.lower() in {".json", ".jsonl"}))
            continue
        if path.suffix.lower() == ".jsonl":
            for raw_line in path.read_text(encoding="utf-8").splitlines():
                line = raw_line.strip()
                if not line:
                    continue
                try:
                    record = json.loads(line)
                except json.JSONDecodeError:
                    continue
                breakdown = _extract_latency_breakdown(record)
                if breakdown is None:
                    continue
                yield {
                    stage: _first_number(breakdown, aliases) or 0.0
                    for stage, aliases in STAGE_ALIASES.items()
                }
            continue
        if path.suffix.lower() == ".json":
            for record in _iter_records_from_json(path):
                breakdown = _extract_latency_breakdown(record)
                if breakdown is None:
                    continue
                yield {
                    stage: _first_number(breakdown, aliases) or 0.0
                    for stage, aliases in STAGE_ALIASES.items()
                }


def summarize_rows(rows: Iterable[dict[str, float]]) -> list[dict[str, float | int | str]]:
    materialized = list(rows)
    if not materialized:
        return []
    summary = []
    for stage in STAGE_ALIASES:
        values = [row[stage] for row in materialized if stage in row]
        summary.append(
            {
                "stage": stage,
                "count": len(values),
                "p50_ms": round(percentile(values, 0.50), 3),
                "p95_ms": round(percentile(values, 0.95), 3),
                "max_ms": round(max(values) if values else 0.0, 3),
            }
        )
    return summary


def render_summary_table(summary: list[dict[str, float | int | str]]) -> str:
    if not summary:
        return "No latency rows found."
    headers = ("stage", "count", "p50_ms", "p95_ms", "max_ms")
    widths = {
        header: max(len(header), *(len(str(row[header])) for row in summary))
        for header in headers
    }
    lines = [
        "  ".join(header.ljust(widths[header]) for header in headers),
        "  ".join("-" * widths[header] for header in headers),
    ]
    for row in summary:
        lines.append(
            "  ".join(str(row[header]).ljust(widths[header]) for header in headers)
        )
    return "\n".join(lines)


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Summarize p50/p95/max latency stages from exported AnswerRun JSON or JSONL."
    )
    parser.add_argument("paths", nargs="+", help="JSON/JSONL files or directories to scan")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv or sys.argv[1:])
    paths = [Path(raw).expanduser().resolve() for raw in args.paths]
    summary = summarize_rows(iter_latency_rows(paths))
    print(render_summary_table(summary))
    return 0 if summary else 1


if __name__ == "__main__":
    raise SystemExit(main())
