#!/usr/bin/env python3
"""Summarize contextual shadow comparison artifacts.

Given one or more comparison output directories, this script loads each
`compare_contextual_shadow_summary.json` and prints a compact table view.
"""

from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path
from typing import Any, Mapping, Sequence


SUMMARY_FILENAME = "compare_contextual_shadow_summary.json"
ROW_COUNT_FILES = (
    "compare_contextual_shadow_retrieval_rows.jsonl",
    "compare_contextual_shadow_retrieval_rows.csv",
)
DEFAULT_COLUMNS = [
    "label",
    "row_count",
    "comparable_rows_hit_at_1",
    "baseline_scored_rows",
    "shadow_scored_rows",
    "baseline_hit_at_1_rate",
    "shadow_hit_at_1_rate",
    "hit_at_1_net",
    "hit_at_3_net",
    "hit_at_k_net",
    "mean_top_k_overlap_jaccard",
    "baseline_primary_hit_at_1_rate",
    "shadow_primary_hit_at_1_rate",
    "primary_hit_at_1_net",
    "mean_baseline_owner_family_concentration",
    "mean_shadow_owner_family_concentration",
    "mean_owner_family_concentration_delta",
]


def _parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "comparison_dirs",
        nargs="+",
        type=Path,
        help="One or more directories containing compare_contextual_shadow_summary.json",
    )
    parser.add_argument(
        "--label",
        action="append",
        default=[],
        metavar="LABEL",
        help="Optional label for the corresponding directory (repeat once per directory).",
    )
    parser.add_argument(
        "--json",
        action="store_true",
        help="Emit machine-readable JSON instead of markdown table.",
    )
    return parser.parse_args(argv)


def _load_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        payload = json.load(handle)
    if not isinstance(payload, dict):
        raise ValueError(f"Summary file is not a JSON object: {path}")
    return payload


def _safe_get(payload: Mapping[str, Any], *keys: str, default: Any = None) -> Any:
    current: Any = payload
    for key in keys:
        if not isinstance(current, Mapping) or key not in current:
            return default
        current = current[key]
    return current


def _count_jsonl_rows(path: Path) -> int:
    with path.open("r", encoding="utf-8") as handle:
        return sum(1 for line in handle if line.strip())


def _count_csv_rows(path: Path) -> int:
    with path.open("r", encoding="utf-8", newline="") as handle:
        reader = csv.reader(handle)
        try:
            next(reader)
        except StopIteration:
            return 0
        return sum(
            1
            for row in reader
            if any(cell.strip() for cell in row if isinstance(cell, str))
        )


def _coerce_positive_int(value: Any) -> int | None:
    if isinstance(value, bool):
        return None
    if isinstance(value, int):
        return value if value > 0 else None
    if isinstance(value, str):
        try:
            value = int(value.strip())
        except ValueError:
            return None
        return value if value > 0 else None
    return None


def _infer_row_count(summary: Mapping[str, Any], output_dir: Path) -> int | None:
    row_count = _safe_get(summary, "row_count")
    resolved_row_count = _coerce_positive_int(row_count)
    if resolved_row_count is not None:
        return resolved_row_count

    candidate_files = []
    for row_name in ROW_COUNT_FILES:
        file_path = output_dir / row_name
        if file_path.exists():
            candidate_files.append(file_path)

    if not candidate_files:
        candidate_files.extend(sorted(output_dir.glob("*rows.jsonl")))
        candidate_files.extend(sorted(output_dir.glob("*rows.csv")))

    for file_path in candidate_files:
        if file_path.suffix == ".jsonl":
            return _count_jsonl_rows(file_path)
        if file_path.suffix == ".csv":
            return _count_csv_rows(file_path)
    return None


def _format_cell(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, float):
        if value.is_integer():
            return str(int(value))
        return f"{value:.4f}".rstrip("0").rstrip(".")
    return str(value)


def _escape_table_cell(value: Any) -> str:
    return (
        _format_cell(value)
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("\n", "<br>")
        .replace("|", "\\|")
    )


def summarize_comparison(directory: Path, *, label: str | None = None) -> dict[str, Any]:
    if not directory.is_dir():
        raise ValueError(f"Not a directory: {directory}")

    summary_path = directory / SUMMARY_FILENAME
    if not summary_path.exists():
        raise FileNotFoundError(
            f"Missing {SUMMARY_FILENAME} in comparison directory: {directory}"
        )

    summary = _load_json(summary_path)

    row_count = _infer_row_count(summary, directory)
    comparable_hit_at_1 = _safe_get(summary, "deltas", "hit_at_1", "comparable_rows")
    baseline_scored_rows = _safe_get(summary, "baseline", "scored_rows")
    shadow_scored_rows = _safe_get(summary, "shadow", "scored_rows")
    baseline_hit_at_1_rate = _safe_get(summary, "baseline", "hit_at_1", "rate")
    shadow_hit_at_1_rate = _safe_get(summary, "shadow", "hit_at_1", "rate")
    hit_at_1_net = _safe_get(summary, "deltas", "hit_at_1", "net_improved")
    hit_at_3_net = _safe_get(summary, "deltas", "hit_at_3", "net_improved")
    hit_at_k_net = _safe_get(summary, "deltas", "hit_at_k", "net_improved")
    baseline_primary_hit_at_1_rate = _safe_get(
        summary, "baseline_primary", "hit_at_1", "rate"
    )
    shadow_primary_hit_at_1_rate = _safe_get(
        summary, "shadow_primary", "hit_at_1", "rate"
    )
    primary_hit_at_1_net = _safe_get(
        summary, "primary_family_deltas", "hit_at_1", "net_improved"
    )

    result = {
        "label": label or directory.name,
        "row_count": row_count,
        "comparable_rows_hit_at_1": comparable_hit_at_1,
        "baseline_scored_rows": baseline_scored_rows,
        "shadow_scored_rows": shadow_scored_rows,
        "baseline_hit_at_1_rate": baseline_hit_at_1_rate,
        "shadow_hit_at_1_rate": shadow_hit_at_1_rate,
        "hit_at_1_net": hit_at_1_net,
        "hit_at_3_net": hit_at_3_net,
        "hit_at_k_net": hit_at_k_net,
        "mean_top_k_overlap_jaccard": _safe_get(
            summary, "mean_top_k_overlap_jaccard"
        ),
        "baseline_primary_hit_at_1_rate": baseline_primary_hit_at_1_rate,
        "shadow_primary_hit_at_1_rate": shadow_primary_hit_at_1_rate,
        "primary_hit_at_1_net": primary_hit_at_1_net,
        "mean_baseline_owner_family_concentration": _safe_get(
            summary, "mean_baseline_owner_family_concentration"
        ),
        "mean_shadow_owner_family_concentration": _safe_get(
            summary, "mean_shadow_owner_family_concentration"
        ),
        "mean_owner_family_concentration_delta": _safe_get(
            summary, "mean_owner_family_concentration_delta"
        ),
    }
    return result


def collect_summaries(
    dirs: Sequence[Path], labels: Sequence[str] | None = None
) -> list[dict[str, Any]]:
    if labels and len(labels) != len(dirs):
        raise ValueError(
            f"--label requires exactly one value per comparison directory: got {len(labels)} labels for {len(dirs)} dirs"
        )

    resolved_labels = list(labels) if labels else [path.name for path in dirs]
    return [
        summarize_comparison(path, label=resolved_labels[index])
        for index, path in enumerate(dirs)
    ]


def render_markdown_table(
    rows: Sequence[Mapping[str, Any]], columns: Sequence[str] = DEFAULT_COLUMNS
) -> str:
    header = "| " + " | ".join(columns) + " |"
    divider = "| " + " | ".join(["---"] * len(columns)) + " |"
    body = [
        "| "
        + " | ".join(
            _escape_table_cell(row.get(column))
            for column in columns
        )
        + " |"
        for row in rows
    ]
    return "\n".join([header, divider, *body])


def _to_json(rows: Sequence[Mapping[str, Any]]) -> str:
    return json.dumps(list(rows), indent=2, sort_keys=True)


def main(argv: Sequence[str] | None = None) -> int:
    args = _parse_args(argv)
    try:
        rows = collect_summaries(args.comparison_dirs, args.label)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        raise SystemExit(str(exc)) from exc

    if args.json:
        output = _to_json(rows)
    else:
        output = render_markdown_table(rows)

    print(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
