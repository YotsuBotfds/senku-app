#!/usr/bin/env python3
"""Summarize multiple RAG diagnostic artifacts as a trend table.

This script is intentionally a thin wrapper over
``summarize_rag_diagnostics.py`` and reuses its parsing helpers where
possible.
"""

from __future__ import annotations

import argparse
import json
import sys
from collections import Counter
from pathlib import Path
from typing import Any, Mapping, Sequence

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

import summarize_rag_diagnostics as base


DEFAULT_COLUMNS = (
    "label",
    "total_rows",
    "generated",
    "app_acceptance",
    "answer_cards",
    "claim_support",
    "evidence_nuggets",
)


APP_ACCEPTANCE_ORDER = (
    "strong_supported",
    "moderate_supported",
    "uncertain_fit_accepted",
    "card_contract_gap",
    "needs_evidence_owner",
    "unsafe_or_overconfident",
    "abstain_accepted",
)

ANSWER_CARD_ORDER = (
    "pass",
    "partial",
    "fail",
    "no_generated_answer",
    "not_applicable_compare",
    "no_cards",
)

CLAIM_SUPPORT_ORDER = (
    "pass",
    "partial",
    "fail",
    "no_generated_answer",
    "no_claims",
)

EVIDENCE_NUGGET_ORDER = (
    "pass",
    "partial",
    "fail",
    "no_evaluable_answer",
    "not_applicable_compare",
    "no_cards",
)


def _parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "diagnostic_dirs",
        nargs="+",
        type=Path,
        help="Directories containing diagnostics.json files.",
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


def _status_counts(rows: Sequence[Mapping[str, Any]], field: str) -> Counter[str]:
    counter: Counter[str] = Counter()
    for row in rows:
        value = str(row.get(field) or "").strip()
        if value:
            counter[value] += 1
    return counter


def _pick_status_counts(
    *,
    summary_counts: Mapping[str, Any] | None,
    row_counts: Mapping[str, int],
    include_statuses: Sequence[str],
) -> dict[str, int]:
    values: dict[str, int] = {}
    base_counts = summary_counts if isinstance(summary_counts, Mapping) else {}
    for status in include_statuses:
        summary_value = base._as_int(base_counts.get(status))
        if summary_value is not None:
            values[status] = summary_value
            continue
        if status in row_counts:
            values[status] = row_counts[status]
        else:
            values[status] = 0

    for status, count in row_counts.items():
        if status not in values:
            values[status] = count
    return values


def _compact_status_breakdown(
    counts: Mapping[str, Any], order: Sequence[str]
) -> str:
    parts: list[str] = []
    for status in order:
        count = int(base._as_int(counts.get(status)) or 0)
        if count:
            parts.append(f"{status}:{count}")

    remainder = sorted(
        status
        for status in counts.keys()
        if status not in set(order) and int(base._as_int(counts.get(status)) or 0) > 0
    )
    for status in remainder:
        parts.append(f"{status}:{int(base._as_int(counts[status]) or 0)}")
    return "|".join(parts) or "none"


def _rate(value: int | None, total: int | None) -> float | None:
    if value is None or total in {None, 0}:
        return None
    return value / total


def _load_payload(directory: Path) -> tuple[Mapping[str, Any], list[Mapping[str, Any]]]:
    payload = base._load_json(directory / base.DIAGNOSTICS_FILENAME)
    summary = base._safe_get(payload, "summary", default=payload)
    if not isinstance(summary, Mapping):
        summary = {}
    rows = base._safe_get(payload, "rows", default=[])
    if not isinstance(rows, list):
        rows = []
    return summary, [row for row in rows if isinstance(row, Mapping)]


def _collect_evidence_counts(
    summary: Mapping[str, Any], rows: Sequence[Mapping[str, Any]]
) -> tuple[int, int, dict[str, int], float | None]:
    summary_counts = base._safe_get(summary, "evidence_nugget_counts", default=None)
    summary_totals = base._safe_get(summary, "evidence_nugget_totals", default=None)

    if isinstance(summary_counts, Mapping) and isinstance(summary_totals, Mapping):
        status_counts = {
            str(status): int(base._as_int(value) or 0)
            for status, value in summary_counts.items()
        }
        total = int(base._as_int(summary_totals.get("total") or 0) or 0)
        supported = int(base._as_int(summary_totals.get("supported") or 0) or 0)
        return total, supported, status_counts, _rate(supported, total)

    row_counts = _status_counts(rows, "evidence_nugget_status")
    status_counts = dict(row_counts)
    total = 0
    supported = 0
    for row in rows:
        total += int(base._as_int(row.get("evidence_nugget_total")) or 0)
        supported += int(base._as_int(row.get("evidence_nugget_supported")) or 0)
    return total, supported, status_counts, _rate(supported, total)


def collect_summaries(
    diagnostic_dirs: Sequence[Path], labels: Sequence[str] | None = None
) -> list[dict[str, Any]]:
    core_rows = base.collect_summaries(diagnostic_dirs, labels)
    rows_out: list[dict[str, Any]] = []

    for directory, row in zip(diagnostic_dirs, core_rows):
        summary, parsed_rows = _load_payload(directory)

        generation = int(
            base._as_int(base._safe_get(summary, "generation_workload", "generated")) or 0
        )
        if generation <= 0 and parsed_rows:
            generation = sum(1 for item in parsed_rows if item.get("generated") == "yes")

        not_generated = int(
            base._as_int(base._safe_get(summary, "generation_workload", "not_generated"))
            or 0
        )
        if not_generated == 0 and parsed_rows:
            not_generated = sum(1 for item in parsed_rows if item.get("generated") == "no")

        card_status_counts = _pick_status_counts(
            summary_counts=base._safe_get(summary, "answer_card_counts", default=None),
            row_counts=_status_counts(parsed_rows, "answer_card_status"),
            include_statuses=ANSWER_CARD_ORDER,
        )
        claim_status_counts = _pick_status_counts(
            summary_counts=base._safe_get(summary, "claim_support_counts", default=None),
            row_counts=_status_counts(parsed_rows, "claim_support_status"),
            include_statuses=CLAIM_SUPPORT_ORDER,
        )
        app_status_counts = _pick_status_counts(
            summary_counts=base._safe_get(summary, "app_acceptance_counts", default=None),
            row_counts=_status_counts(parsed_rows, "app_acceptance_status"),
            include_statuses=APP_ACCEPTANCE_ORDER,
        )
        evidence_total, evidence_supported, evidence_status_counts, evidence_rate = (
            _collect_evidence_counts(summary, parsed_rows)
        )

        row = dict(row)
        row["generated_rows"] = generation
        row["not_generated_rows"] = not_generated
        row["generated"] = (
            f"{generation}/{row['total_rows']} ({_format_percent(_rate(generation, row['total_rows']))})"
            if row["total_rows"]
            else ""
        )
        row["answer_cards"] = _compact_status_breakdown(
            card_status_counts, ANSWER_CARD_ORDER
        )
        row["claim_support"] = _compact_status_breakdown(
            claim_status_counts, CLAIM_SUPPORT_ORDER
        )
        row["app_acceptance"] = _compact_status_breakdown(
            app_status_counts, APP_ACCEPTANCE_ORDER
        )
        row["evidence_nugget_statuses"] = evidence_status_counts
        row["evidence_nugget_total"] = evidence_total
        row["evidence_nugget_supported"] = evidence_supported
        row["evidence_nugget_coverage"] = evidence_rate
        row["evidence_nuggets"] = _compact_evidence_summary(
            evidence_status_counts, evidence_supported, evidence_total, evidence_rate
        )
        rows_out.append(row)

    return rows_out


def _format_percent(value: float | None) -> str:
    if value is None:
        return ""
    return f"{value:.1%}"


def _compact_evidence_summary(
    status_counts: Mapping[str, int],
    supported: int,
    total: int,
    rate: float | None,
) -> str:
    status_text = _compact_status_breakdown(status_counts, EVIDENCE_NUGGET_ORDER)
    if total <= 0:
        return f"unavailable | {status_text}"
    return f"{supported}/{total} ({_format_percent(rate)}) | {status_text}"


def render_markdown_table(
    rows: Sequence[Mapping[str, Any]], columns: Sequence[str] = DEFAULT_COLUMNS
) -> str:
    return base.render_markdown_table(rows, columns)


def _to_json(rows: Sequence[Mapping[str, Any]]) -> str:
    return json.dumps(list(rows), indent=2, sort_keys=True)


def main(argv: Sequence[str] | None = None) -> int:
    args = _parse_args(argv)
    try:
        rows = collect_summaries(args.diagnostic_dirs, args.label)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        raise SystemExit(str(exc)) from exc

    output = _to_json(rows) if args.json else render_markdown_table(rows)
    print(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
