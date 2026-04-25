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
    "reviewed_uncertain_fit",
    "answer_cards",
    "claim_support",
    "evidence_nuggets",
    "expected_owner_best_rank",
    "expected_owner_top3_count",
    "expected_owner_topk_count",
    "expected_owner_top3_share",
    "expected_owner_topk_share",
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


def _is_reviewed_uncertain_fit(row: Mapping[str, Any]) -> bool:
    return (
        str(row.get("answer_provenance") or "").strip() == "reviewed_card_runtime"
        and (
            str(row.get("app_gate_status") or "").strip() == "uncertain_fit"
            or str(row.get("answer_mode") or "").strip() == "uncertain_fit"
        )
    )


def _count_reviewed_uncertain_fit(rows: Sequence[Mapping[str, Any]]) -> int:
    return sum(1 for row in rows if _is_reviewed_uncertain_fit(row))


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


def _safe_int(value: Any) -> int | None:
    if value in ("", None, "unknown"):
        return None
    if isinstance(value, bool):
        return int(value)
    if isinstance(value, int):
        return value
    if isinstance(value, float):
        return int(value)
    try:
        return int(float(str(value).strip()))
    except (TypeError, ValueError):
        return None


def _safe_float(value: Any) -> float | None:
    if value in ("", None, "unknown"):
        return None
    if isinstance(value, bool):
        return float(int(value))
    if isinstance(value, (int, float)):
        return float(value)
    try:
        return float(str(value).strip())
    except (TypeError, ValueError):
        return None


def _ratio_text(numerator: int, denominator: int) -> str:
    if denominator <= 0:
        return "unknown"
    return f"{numerator}/{denominator} ({numerator / denominator:.1%})"


def _average_text(total: float, denominator: int, *, precision: int = 4) -> str:
    if denominator <= 0:
        return "unknown"
    return f"{(round(total / denominator, precision)):.{precision}f}"


def _split_ids(value: Any) -> list[str]:
    if value in ("", None, "unknown"):
        return []
    if isinstance(value, str):
        return [item.strip() for item in value.split("|") if item.strip()]
    if isinstance(value, Sequence):
        return [str(item).strip() for item in value if str(item).strip()]
    return []


def _row_owner_metrics(row: Mapping[str, Any]) -> dict[str, int | float | str]:
    best_rank = _safe_int(row.get("expected_owner_best_rank"))
    top3_count = _safe_int(row.get("expected_owner_top3_count"))
    topk_count = _safe_int(row.get("expected_owner_topk_count"))
    top3_share = _safe_float(row.get("expected_owner_top3_share"))
    topk_share = _safe_float(row.get("expected_owner_topk_share"))
    if (
        best_rank is not None
        and top3_count is not None
        and topk_count is not None
        and top3_share is not None
        and topk_share is not None
    ):
        return {
            "expected_owner_best_rank": best_rank,
            "expected_owner_top3_count": top3_count,
            "expected_owner_topk_count": topk_count,
            "expected_owner_top3_share": top3_share,
            "expected_owner_topk_share": topk_share,
        }

    expected_ids = set(_split_ids(row.get("expected_guide_ids")))
    retrieved_ids = _split_ids(row.get("top_retrieved_guide_ids"))
    if not expected_ids or not retrieved_ids:
        return {
            "expected_owner_best_rank": "unknown",
            "expected_owner_top3_count": "unknown",
            "expected_owner_topk_count": "unknown",
            "expected_owner_top3_share": "unknown",
            "expected_owner_topk_share": "unknown",
        }

    computed_best_rank: int | str = "unknown"
    for index, guide_id in enumerate(retrieved_ids, start=1):
        if guide_id in expected_ids:
            computed_best_rank = index
            break
    computed_top3_count = sum(1 for guide_id in retrieved_ids[:3] if guide_id in expected_ids)
    computed_topk_count = sum(1 for guide_id in retrieved_ids if guide_id in expected_ids)
    return {
        "expected_owner_best_rank": computed_best_rank,
        "expected_owner_top3_count": computed_top3_count,
        "expected_owner_topk_count": computed_topk_count,
        "expected_owner_top3_share": round(computed_top3_count / 3, 4),
        "expected_owner_topk_share": round(computed_topk_count / len(retrieved_ids), 4),
    }


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


def _collect_owner_metrics(
    summary: Mapping[str, Any], rows: Sequence[Mapping[str, Any]]
) -> dict[str, str]:
    required_keys = (
        "expected_owner_best_rank",
        "expected_owner_top3_count",
        "expected_owner_topk_count",
        "expected_owner_top3_share",
        "expected_owner_topk_share",
    )
    if all(key in summary for key in required_keys):
        return {
            key: str(base._safe_get(summary, key, default="unknown"))
            for key in required_keys
        }

    owner_rows = [
        row
        for row in rows
        if isinstance(row, Mapping)
        and row.get("expected_guide_ids") not in {"", None, "unknown"}
    ]
    if not owner_rows:
        return {
            "expected_owner_rows": "0",
            "expected_owner_best_rank": "unknown",
            "expected_owner_top3_count": "unknown",
            "expected_owner_topk_count": "unknown",
            "expected_owner_top3_share": "unknown",
            "expected_owner_topk_share": "unknown",
        }

    owner_best_rank_total = 0
    owner_best_rank_known = 0
    owner_top3_hits = 0
    owner_topk_hits = 0
    owner_top3_count_sum = 0
    owner_topk_count_sum = 0
    owner_topk_count_denominator = 0
    owner_top3_share_total = 0.0
    owner_topk_share_total = 0.0
    owner_top3_share_rows = 0
    owner_topk_share_rows = 0

    for row in owner_rows:
        row_metrics = _row_owner_metrics(row)
        best_rank = _safe_int(row_metrics.get("expected_owner_best_rank"))
        if best_rank is not None:
            owner_best_rank_total += best_rank
            owner_best_rank_known += 1
        top3_count = _safe_int(row_metrics.get("expected_owner_top3_count"))
        if top3_count is not None and top3_count > 0:
            owner_top3_hits += 1
        if top3_count is not None:
            owner_top3_count_sum += top3_count
        topk_count = _safe_int(row_metrics.get("expected_owner_topk_count"))
        if topk_count is not None and topk_count > 0:
            owner_topk_hits += 1
        if topk_count is not None:
            owner_topk_count_sum += topk_count
        top_retrieved = row.get("top_retrieved_guide_ids")
        if isinstance(top_retrieved, str) and top_retrieved not in {"", "unknown"}:
            owner_topk_count_denominator += len(
                [item for item in top_retrieved.split("|") if item]
            )
        elif isinstance(top_retrieved, Sequence) and not isinstance(top_retrieved, str):
            owner_topk_count_denominator += len(top_retrieved)
        else:
            topk_share = _safe_float(row_metrics.get("expected_owner_topk_share"))
            if topk_share is not None:
                owner_topk_share_total += topk_share
                owner_topk_share_rows += 1
        top3_share = _safe_float(row_metrics.get("expected_owner_top3_share"))
        if top3_share is not None:
            owner_top3_share_total += top3_share
            owner_top3_share_rows += 1

    return {
        "expected_owner_rows": str(len(owner_rows)),
        "expected_owner_best_rank": _average_text(
            owner_best_rank_total,
            owner_best_rank_known,
            precision=2,
        )
        if owner_best_rank_known
        else "unknown",
        "expected_owner_top3_count": _ratio_text(owner_top3_hits, len(owner_rows)),
        "expected_owner_topk_count": _ratio_text(owner_topk_hits, len(owner_rows)),
        "expected_owner_top3_share": _average_text(
            owner_top3_count_sum,
            len(owner_rows) * 3,
        )
        if owner_rows
        else "unknown",
        "expected_owner_topk_share": _average_text(
            owner_topk_count_sum,
            owner_topk_count_denominator,
        )
        if owner_topk_count_denominator > 0
        else (
            _average_text(owner_topk_share_total, owner_topk_share_rows)
            if owner_topk_share_rows
            else "unknown"
        ),
    }


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
        reviewed_uncertain_fit_count = _count_reviewed_uncertain_fit(parsed_rows)
        owner_metrics = _collect_owner_metrics(summary, parsed_rows)

        row = dict(row)
        row["generated_rows"] = generation
        row["not_generated_rows"] = not_generated
        row["generated"] = (
            f"{generation}/{row['total_rows']} ({_format_percent(_rate(generation, row['total_rows']))})"
            if row["total_rows"]
            else ""
        )
        row["reviewed_uncertain_fit"] = (
            f"{reviewed_uncertain_fit_count}/{row['total_rows']} ({_format_percent(_rate(reviewed_uncertain_fit_count, row['total_rows']))})"
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
        row["expected_owner_best_rank"] = owner_metrics["expected_owner_best_rank"]
        row["expected_owner_top3_count"] = owner_metrics["expected_owner_top3_count"]
        row["expected_owner_topk_count"] = owner_metrics["expected_owner_topk_count"]
        row["expected_owner_top3_share"] = owner_metrics["expected_owner_top3_share"]
        row["expected_owner_topk_share"] = owner_metrics["expected_owner_topk_share"]
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
