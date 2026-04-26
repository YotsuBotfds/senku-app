#!/usr/bin/env python3
"""Summarize `diagnostics.json` files from one or more RAG diagnostic runs."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any, Mapping, Sequence

DIAGNOSTICS_FILENAME = "diagnostics.json"
DEFAULT_COLUMNS = (
    "label",
    "generated_at",
    "total_rows",
    "expected_rows",
    "bad_buckets",
    "hit_at_1_rate",
    "hit_at_3_rate",
    "hit_at_k_rate",
    "cited_rate",
    "strong_supported",
    "moderate_supported",
    "uncertain_fit_accepted",
    "root_supported",
    "root_evidence_owner",
    "root_card_contract",
    "root_safety_surface",
    "root_gate_policy",
    "card_pass",
    "card_partial",
    "card_fail",
    "claim_pass",
    "generated_shadow_card_gap_rows",
    "generated_model",
    "reviewed_card_runtime",
    "reviewed_card_runtime_enabled",
    "reviewed_card_runtime_disabled",
    "reviewed_card_runtime_unknown",
    "deterministic_rule",
    "quality_score_10",
)
BAD_BUCKET_KEYS = (
    "retrieval_miss",
    "ranking_miss",
    "generation_miss",
    "unsupported_or_truncated_answer",
    "safety_contract_miss",
    "artifact_error",
)

_MISSING = object()
_RATIO_PATTERN = re.compile(r"^\s*(?P<num>-?\d+(?:\.\d+)?)\s*/\s*(?P<den>-?\d+(?:\.\d+)?)")
_PERCENT_PATTERN = re.compile(r"^\s*(?P<pct>-?\d+(?:\.\d+)?)\s*%")
_FALLBACK_FIELD_BY_PATH = {
    ("app_acceptance_counts", "strong_supported"): "app_acceptance_status",
    ("app_acceptance_counts", "moderate_supported"): "app_acceptance_status",
    ("app_acceptance_counts", "uncertain_fit_accepted"): "app_acceptance_status",
    ("app_acceptance_root_cause_counts", "supported"): "app_acceptance_root_cause",
    ("app_acceptance_root_cause_counts", "evidence_owner"): "app_acceptance_root_cause",
    ("app_acceptance_root_cause_counts", "card_contract"): "app_acceptance_root_cause",
    ("app_acceptance_root_cause_counts", "safety_surface"): "app_acceptance_root_cause",
    ("app_acceptance_root_cause_counts", "gate_policy"): "app_acceptance_root_cause",
    ("answer_card_counts", "pass"): "answer_card_status",
    ("answer_card_counts", "partial"): "answer_card_status",
    ("answer_card_counts", "fail"): "answer_card_status",
    ("claim_support_counts", "pass"): "claim_support_status",
    ("answer_provenance_counts", "generated_model"): "answer_provenance",
    ("answer_provenance_counts", "reviewed_card_runtime"): "answer_provenance",
    ("answer_provenance_counts", "deterministic_rule"): "answer_provenance",
    ("artifact_reviewed_card_runtime_answer_counts", "enabled"): "artifact_reviewed_card_runtime_answers",
    ("artifact_reviewed_card_runtime_answer_counts", "disabled"): "artifact_reviewed_card_runtime_answers",
    ("artifact_reviewed_card_runtime_answer_counts", "unknown"): "artifact_reviewed_card_runtime_answers",
}


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


def _load_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        payload = json.load(handle)
    if not isinstance(payload, dict):
        raise ValueError(f"diagnostics.json payload must be an object: {path}")
    return payload


def _safe_get(mapping: Mapping[str, Any], *keys: str, default: Any = None) -> Any:
    current: Any = mapping
    for key in keys:
        if not isinstance(current, Mapping) or key not in current:
            return default
        current = current[key]
    return current


def _as_int(value: Any) -> int | None:
    if isinstance(value, bool):
        return int(value)
    if isinstance(value, int):
        return value
    if isinstance(value, float):
        return int(value)
    if isinstance(value, str):
        try:
            return int(float(value.strip()))
        except ValueError:
            return None
    if isinstance(value, Mapping):
        total = 0
        found = False
        for nested in value.values():
            nested_count = _as_int(nested)
            if nested_count is not None:
                total += nested_count
                found = True
        if found:
            return total
    return None


def _parse_rate(value: Any) -> float | None:
    """Parse rates like ``21/24 (87.5%)`` or ``87.5%`` into a ratio."""
    if value is None:
        return None
    if isinstance(value, (int, float)):
        ratio = float(value)
        if ratio > 1:
            return ratio / 100.0
        return ratio
    if not isinstance(value, str):
        return None

    text = value.strip().lower()
    if not text or text in {"unknown", "n/a", "na", "none"}:
        return None

    ratio_match = _RATIO_PATTERN.match(text)
    if ratio_match is not None:
        numerator = float(ratio_match.group("num"))
        denominator = float(ratio_match.group("den"))
        if denominator == 0:
            return None
        return numerator / denominator

    percent_match = _PERCENT_PATTERN.match(text)
    if percent_match is not None:
        return float(percent_match.group("pct")) / 100.0

    try:
        return float(text) if float(text) <= 1 else float(text) / 100.0
    except ValueError:
        return None


def _count(mapping: Mapping[str, Any], rows: list[dict[str, Any]], *path: str) -> int:
    """Extract a count from a nested mapping, with a row-level fallback.

    The fallback is used if nested keys are absent but a corresponding per-row
    status field can be counted directly.
    """
    if path:
        raw = _safe_get(mapping, *path, default=_MISSING)
    else:
        raw = _MISSING

    if raw is not _MISSING:
        count = _as_int(raw)
        if count is not None:
            return count

    if rows:
        status_field = _FALLBACK_FIELD_BY_PATH.get(tuple(path))
        if status_field is not None:
            return sum(
                1
                for row in rows
                if isinstance(row, Mapping)
                and str(row.get(status_field) or "").strip() == path[-1]
            )
        if len(path) == 1:
            key = path[0]
            if key == "generated_shadow_card_gap_rows":
                return sum(
                    1
                    for row in rows
                    if isinstance(row, Mapping)
                    and str(row.get(key) or "").strip() == "yes"
                )

    return 0


def _format_cell(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, float):
        return f"{value:.4f}".rstrip("0").rstrip(".")
    return str(value)


def _rate(numerator: int | None, denominator: int | None) -> float | None:
    if denominator is None or denominator <= 0 or numerator is None:
        return None
    return numerator / denominator


def _clamp_rate(value: float | None) -> float | None:
    if value is None:
        return None
    return max(0.0, min(1.0, value))


def _quality_score_10(row: Mapping[str, Any]) -> float | None:
    """Return a transparent run-level heuristic for selected diagnostics panels.

    This is a triage score for comparing known diagnostic artifacts, not an
    overall product-quality rating.
    """
    total_rows = _as_int(row.get("total_rows"))
    if not total_rows:
        return None

    bad_buckets = _as_int(row.get("bad_buckets")) or 0
    good_bucket_rate = _clamp_rate(1.0 - (bad_buckets / total_rows))

    hit_at_1 = _clamp_rate(row.get("hit_at_1_rate"))
    hit_at_3 = _clamp_rate(row.get("hit_at_3_rate"))
    hit_at_k = _clamp_rate(row.get("hit_at_k_rate"))
    cited = _clamp_rate(row.get("cited_rate"))
    retrieval_score = None
    if None not in {hit_at_1, hit_at_3, hit_at_k, cited}:
        retrieval_score = (0.5 * hit_at_1) + (0.2 * hit_at_3) + (0.1 * hit_at_k) + (0.2 * cited)

    supported_count = (
        (_as_int(row.get("strong_supported")) or 0)
        + (_as_int(row.get("moderate_supported")) or 0)
        + (_as_int(row.get("uncertain_fit_accepted")) or 0)
    )
    app_support_rate = _rate(supported_count, total_rows)

    card_pass = _as_int(row.get("card_pass")) or 0
    card_partial = _as_int(row.get("card_partial")) or 0
    card_fail = _as_int(row.get("card_fail")) or 0
    card_scored = card_pass + card_partial + card_fail
    card_score = (card_pass + 0.5 * card_partial) / card_scored if card_scored else 1.0

    claim_pass = _as_int(row.get("claim_pass")) or 0
    claim_denominator = (
        (_as_int(row.get("generated_model")) or 0)
        + (_as_int(row.get("reviewed_card_runtime")) or 0)
    )
    claim_score = _rate(claim_pass, claim_denominator) if claim_denominator else 1.0
    claim_score = _clamp_rate(claim_score)

    component_weights = (
        (good_bucket_rate, 0.35),
        (retrieval_score, 0.25),
        (app_support_rate, 0.20),
        (card_score, 0.10),
        (claim_score, 0.10),
    )
    weighted_sum = 0.0
    used_weight = 0.0
    for component, weight in component_weights:
        component = _clamp_rate(component)
        if component is None:
            continue
        weighted_sum += component * weight
        used_weight += weight

    if used_weight <= 0:
        return None
    return round(10.0 * (weighted_sum / used_weight), 2)


def _escape_table_cell(value: Any) -> str:
    return _format_cell(value).replace("|", "\\|")


def _summarize_directory(directory: Path, label: str | None = None) -> dict[str, Any]:
    if not directory.is_dir():
        raise ValueError(f"Not a directory: {directory}")
    payload_path = directory / DIAGNOSTICS_FILENAME
    if not payload_path.exists():
        raise FileNotFoundError(f"Missing {DIAGNOSTICS_FILENAME}: {directory}")

    payload = _load_json(payload_path)
    summary = payload.get("summary", payload) if isinstance(payload, Mapping) else {}
    rows = _safe_get(payload, "rows", default=[])
    if not isinstance(summary, Mapping):
        summary = {}
    if not isinstance(rows, list):
        rows = []

    parsed_rows = [row for row in rows if isinstance(row, Mapping)]

    total_rows = _as_int(_safe_get(summary, "total_rows", default=None))
    if total_rows is None:
        total_rows = len(parsed_rows)

    result = {
        "label": label or directory.name,
        "generated_at": payload.get("generated_at"),
        "total_rows": total_rows,
        "expected_rows": _as_int(_safe_get(summary, "expected_guide_rows", default=None)),
        "bad_buckets": sum(
            _count(summary, parsed_rows, "by_bucket", bucket_key)
            for bucket_key in BAD_BUCKET_KEYS
        ),
        "hit_at_1_rate": _parse_rate(_safe_get(summary, "expected_hit_at_1")),
        "hit_at_3_rate": _parse_rate(_safe_get(summary, "expected_hit_at_3")),
        "hit_at_k_rate": _parse_rate(_safe_get(summary, "expected_hit_at_k")),
        "cited_rate": _parse_rate(_safe_get(summary, "expected_cited")),
        "strong_supported": _count(
            summary,
            parsed_rows,
            "app_acceptance_counts",
            "strong_supported",
        ),
        "moderate_supported": _count(
            summary,
            parsed_rows,
            "app_acceptance_counts",
            "moderate_supported",
        ),
        "uncertain_fit_accepted": _count(
            summary,
            parsed_rows,
            "app_acceptance_counts",
            "uncertain_fit_accepted",
        ),
        "root_supported": _count(
            summary,
            parsed_rows,
            "app_acceptance_root_cause_counts",
            "supported",
        ),
        "root_evidence_owner": _count(
            summary,
            parsed_rows,
            "app_acceptance_root_cause_counts",
            "evidence_owner",
        ),
        "root_card_contract": _count(
            summary,
            parsed_rows,
            "app_acceptance_root_cause_counts",
            "card_contract",
        ),
        "root_safety_surface": _count(
            summary,
            parsed_rows,
            "app_acceptance_root_cause_counts",
            "safety_surface",
        ),
        "root_gate_policy": _count(
            summary,
            parsed_rows,
            "app_acceptance_root_cause_counts",
            "gate_policy",
        ),
        "card_pass": _count(summary, parsed_rows, "answer_card_counts", "pass"),
        "card_partial": _count(summary, parsed_rows, "answer_card_counts", "partial"),
        "card_fail": _count(summary, parsed_rows, "answer_card_counts", "fail"),
        "claim_pass": _count(summary, parsed_rows, "claim_support_counts", "pass"),
        "generated_shadow_card_gap_rows": _count(summary, parsed_rows, "generated_shadow_card_gap_rows"),
        "generated_model": _count(summary, parsed_rows, "answer_provenance_counts", "generated_model"),
        "reviewed_card_runtime": _count(summary, parsed_rows, "answer_provenance_counts", "reviewed_card_runtime"),
        "reviewed_card_runtime_enabled": _count(
            summary,
            parsed_rows,
            "artifact_reviewed_card_runtime_answer_counts",
            "enabled",
        ),
        "reviewed_card_runtime_disabled": _count(
            summary,
            parsed_rows,
            "artifact_reviewed_card_runtime_answer_counts",
            "disabled",
        ),
        "reviewed_card_runtime_unknown": _count(
            summary,
            parsed_rows,
            "artifact_reviewed_card_runtime_answer_counts",
            "unknown",
        ),
        "deterministic_rule": _count(summary, parsed_rows, "answer_provenance_counts", "deterministic_rule"),
    }
    result["quality_score_10"] = _quality_score_10(result)
    return result


def collect_summaries(
    directories: Sequence[Path], labels: Sequence[str] | None = None
) -> list[dict[str, Any]]:
    if labels and len(labels) != len(directories):
        raise ValueError(
            f"--label must be used once per directory: got {len(labels)} labels for {len(directories)} directories"
        )
    resolved_labels = list(labels) if labels else [path.name for path in directories]
    return [
        _summarize_directory(directory, label=resolved_labels[index])
        for index, directory in enumerate(directories)
    ]


def render_markdown_table(
    rows: Sequence[Mapping[str, Any]],
    columns: Sequence[str] = DEFAULT_COLUMNS,
) -> str:
    header = "| " + " | ".join(columns) + " |"
    divider = "| " + " | ".join(["---"] * len(columns)) + " |"
    body = [
        "| "
        + " | ".join(_escape_table_cell(row.get(column)) for column in columns)
        + " |"
        for row in rows
    ]
    return "\n".join([header, divider, *body])


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
