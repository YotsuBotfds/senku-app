#!/usr/bin/env python3
"""Compare two RAG diagnostics artifacts and report regressions."""

from __future__ import annotations

import argparse
import json
import math
import re
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Mapping, Sequence


DIAGNOSTICS_FILENAME = "diagnostics.json"

DEFAULT_RULES: dict[str, str] = {
    "expected_supported": "down",
    "retrieval_miss": "up",
    "ranking_miss": "up",
    "generation_miss": "up",
    "unsupported_or_truncated_answer": "up",
    "safety_contract_miss": "up",
    "artifact_error": "up",
    "expected_owner_top3_hits": "down",
    "expected_owner_topk_hits": "down",
    "expected_owner_top3_share": "down",
    "expected_owner_topk_share": "down",
    "expected_cited_count": "down",
    "expected_cited_rate": "down",
    "top1_marker_fail": "up",
    "top1_marker_warn": "up",
    "top1_bridge_rows": "up",
    "top1_unresolved_partial_rows": "up",
}

BUCKETS = (
    "expected_supported",
    "retrieval_miss",
    "ranking_miss",
    "generation_miss",
    "unsupported_or_truncated_answer",
    "safety_contract_miss",
    "artifact_error",
    "abstain_or_clarify_needed",
)

_RATIO_PATTERN = re.compile(
    r"^\s*(?P<num>-?\d+(?:\.\d+)?)\s*/\s*(?P<den>-?\d+(?:\.\d+)?)"
)
_PERCENT_PATTERN = re.compile(r"^\s*(?P<pct>-?\d+(?:\.\d+)?)\s*%")


def _as_number(value: Any) -> float | None:
    if value is None:
        return None
    if isinstance(value, bool):
        return float(int(value))
    if isinstance(value, (int, float)):
        number = float(value)
        return number if math.isfinite(number) else None
    if isinstance(value, str):
        text = value.strip()
        if not text or text.lower() in {"unknown", "n/a", "na"}:
            return None
        try:
            number = float(text)
        except ValueError:
            return None
        return number if math.isfinite(number) else None
    return None


def _as_int(value: Any) -> int:
    number = _as_number(value)
    return int(number) if number is not None else 0


def _parse_ratio(value: Any) -> tuple[float | None, float | None, float | None]:
    if isinstance(value, str):
        text = value.strip().lower()
        ratio_match = _RATIO_PATTERN.match(text)
        if ratio_match is not None:
            numerator = float(ratio_match.group("num"))
            denominator = float(ratio_match.group("den"))
            rate = numerator / denominator if denominator else None
            return numerator, denominator, rate

        percent_match = _PERCENT_PATTERN.match(text)
        if percent_match is not None:
            return None, None, float(percent_match.group("pct")) / 100.0

    number = _as_number(value)
    if number is None:
        return None, None, None
    rate = number / 100.0 if number > 1 else number
    return None, None, rate


def _rate(value: Any) -> float | None:
    return _parse_ratio(value)[2]


def _ratio_numerator(value: Any) -> int | None:
    numerator, _, _ = _parse_ratio(value)
    if numerator is not None:
        return int(numerator)
    number = _as_number(value)
    return int(number) if number is not None else None


def _count_rows(rows: Sequence[Mapping[str, Any]], field: str, value: str) -> int:
    return sum(1 for row in rows if str(row.get(field) or "").strip() == value)


def _split_ids(value: Any) -> list[str]:
    if value is None:
        return []
    if isinstance(value, str):
        if not value.strip() or value.strip().lower() == "unknown":
            return []
        return [item.strip() for item in value.split("|") if item.strip()]
    if isinstance(value, Sequence):
        return [str(item).strip() for item in value if str(item).strip()]
    return []


def _resolve_diagnostics_path(path: Path) -> Path:
    if path.is_dir():
        resolved = path / DIAGNOSTICS_FILENAME
    else:
        resolved = path
    if not resolved.is_file():
        raise FileNotFoundError(f"Missing diagnostics artifact: {resolved}")
    return resolved


def _load_diagnostics(path: Path) -> tuple[Path, Mapping[str, Any], list[Mapping[str, Any]]]:
    diagnostics_path = _resolve_diagnostics_path(path)
    with diagnostics_path.open("r", encoding="utf-8") as handle:
        payload = json.load(handle)
    if not isinstance(payload, Mapping):
        raise ValueError(f"Diagnostics payload must be an object: {diagnostics_path}")

    summary = payload.get("summary", payload)
    if not isinstance(summary, Mapping):
        summary = {}
    rows = payload.get("rows", [])
    if not isinstance(rows, list):
        rows = []
    parsed_rows = [row for row in rows if isinstance(row, Mapping)]
    return diagnostics_path, summary, parsed_rows


def _label_for_path(path: Path, explicit: str | None = None) -> str:
    if explicit:
        return explicit
    diagnostics_path = _resolve_diagnostics_path(path)
    if diagnostics_path.name == DIAGNOSTICS_FILENAME:
        return diagnostics_path.parent.name
    return diagnostics_path.stem


def _bucket_count(summary: Mapping[str, Any], rows: Sequence[Mapping[str, Any]], bucket: str) -> int:
    by_bucket = summary.get("by_bucket")
    if isinstance(by_bucket, Mapping) and bucket in by_bucket:
        return _as_int(by_bucket.get(bucket))
    return _count_rows(rows, "suspected_failure_bucket", bucket)


def _marker_counts(summary: Mapping[str, Any], rows: Sequence[Mapping[str, Any]]) -> Counter[str]:
    raw = summary.get("top1_marker_risk_counts")
    if isinstance(raw, Mapping):
        return Counter({str(key): _as_int(value) for key, value in raw.items()})
    return Counter(str(row.get("top1_marker_risk") or "").strip() for row in rows)


def _summary_or_row_yes_count(
    summary: Mapping[str, Any],
    rows: Sequence[Mapping[str, Any]],
    summary_key: str,
    row_key: str,
) -> int:
    raw = summary.get(summary_key)
    if raw is not None:
        return _as_int(raw)
    return sum(1 for row in rows if str(row.get(row_key) or "").strip().lower() == "yes")


def _owner_rows(summary: Mapping[str, Any], rows: Sequence[Mapping[str, Any]]) -> int:
    raw = summary.get("expected_owner_rows")
    if raw is not None:
        return _as_int(raw)
    return sum(1 for row in rows if _split_ids(row.get("expected_guide_ids")))


def _owner_hit_count(
    summary: Mapping[str, Any],
    rows: Sequence[Mapping[str, Any]],
    key: str,
) -> int | None:
    raw = summary.get(key)
    if raw is not None:
        parsed = _ratio_numerator(raw)
        if parsed is not None:
            return parsed
    if rows:
        return sum(1 for row in rows if _as_int(row.get(key)) > 0)
    return None


def _owner_share(
    summary: Mapping[str, Any],
    rows: Sequence[Mapping[str, Any]],
    key: str,
) -> float | None:
    raw = summary.get(key)
    if raw is not None:
        return _rate(raw) if isinstance(raw, str) and "/" in raw else _as_number(raw)
    values = [_as_number(row.get(key)) for row in rows if _as_number(row.get(key)) is not None]
    if not values:
        return None
    return sum(values) / len(values)


def _expected_cited(summary: Mapping[str, Any], rows: Sequence[Mapping[str, Any]]) -> tuple[int | None, float | None]:
    raw = summary.get("expected_cited")
    if raw is not None:
        numerator = _ratio_numerator(raw)
        return numerator, _rate(raw)

    owner_rows = [row for row in rows if _split_ids(row.get("expected_guide_ids"))]
    if not owner_rows:
        return None, None
    cited = 0
    for row in owner_rows:
        expected = set(_split_ids(row.get("expected_guide_ids")))
        cited_ids = set(_split_ids(row.get("cited_guide_ids")))
        if str(row.get("expected_cited") or "").strip().lower() == "yes" or expected.intersection(cited_ids):
            cited += 1
    return cited, cited / len(owner_rows)


def collect_metrics(path: Path, *, label: str | None = None) -> dict[str, Any]:
    diagnostics_path, summary, rows = _load_diagnostics(path)
    total_rows = _as_int(summary.get("total_rows")) or len(rows)
    marker_counts = _marker_counts(summary, rows)
    expected_cited_count, expected_cited_rate = _expected_cited(summary, rows)

    metrics: dict[str, Any] = {
        "label": _label_for_path(path, label),
        "path": diagnostics_path.as_posix(),
        "total_rows": total_rows,
        "expected_owner_rows": _owner_rows(summary, rows),
        "expected_owner_top3_hits": _owner_hit_count(
            summary, rows, "expected_owner_top3_count"
        ),
        "expected_owner_topk_hits": _owner_hit_count(
            summary, rows, "expected_owner_topk_count"
        ),
        "expected_owner_top3_share": _owner_share(
            summary, rows, "expected_owner_top3_share"
        ),
        "expected_owner_topk_share": _owner_share(
            summary, rows, "expected_owner_topk_share"
        ),
        "expected_cited_count": expected_cited_count,
        "expected_cited_rate": expected_cited_rate,
        "top1_marker_fail": marker_counts.get("fail", 0),
        "top1_marker_warn": marker_counts.get("warn", 0),
        "top1_bridge_rows": _summary_or_row_yes_count(
            summary, rows, "top1_bridge_rows", "top1_is_bridge"
        ),
        "top1_unresolved_partial_rows": _summary_or_row_yes_count(
            summary,
            rows,
            "top1_unresolved_partial_rows",
            "top1_has_unresolved_partial",
        ),
    }
    for bucket in BUCKETS:
        metrics[bucket] = _bucket_count(summary, rows, bucket)
    return metrics


def _parse_metric_setting(raw: str, *, value_name: str) -> tuple[str, str]:
    if "=" not in raw:
        raise ValueError(f"Expected METRIC={value_name}: {raw}")
    metric, value = raw.split("=", 1)
    metric = metric.strip()
    value = value.strip()
    if not metric or not value:
        raise ValueError(f"Expected METRIC={value_name}: {raw}")
    return metric, value


def parse_rules(
    *,
    no_default_checks: bool = False,
    checks: Sequence[str] | None = None,
    ignored: Sequence[str] | None = None,
) -> dict[str, str]:
    rules = {} if no_default_checks else dict(DEFAULT_RULES)
    for raw in checks or []:
        if ":" not in raw:
            raise ValueError(f"Expected METRIC:up or METRIC:down for --check: {raw}")
        metric, direction = raw.split(":", 1)
        metric = metric.strip()
        direction = direction.strip().lower()
        if not metric or direction not in {"up", "down"}:
            raise ValueError(f"Expected METRIC:up or METRIC:down for --check: {raw}")
        rules[metric] = direction
    for metric in ignored or []:
        rules.pop(metric, None)
    return rules


def parse_allowances(raw_values: Sequence[str] | None = None) -> dict[str, float]:
    allowances: dict[str, float] = {}
    for raw in raw_values or []:
        metric, value = _parse_metric_setting(raw, value_name="NUMBER")
        number = _as_number(value)
        if number is None or number < 0:
            raise ValueError(f"Allowance must be a non-negative number: {raw}")
        allowances[metric] = number
    return allowances


def compare_metrics(
    baseline: Mapping[str, Any],
    current: Mapping[str, Any],
    rules: Mapping[str, str],
    allowances: Mapping[str, float] | None = None,
) -> list[dict[str, Any]]:
    allowances = allowances or {}
    comparisons: list[dict[str, Any]] = []
    for metric, direction in rules.items():
        baseline_value = _as_number(baseline.get(metric))
        current_value = _as_number(current.get(metric))
        delta = (
            current_value - baseline_value
            if baseline_value is not None and current_value is not None
            else None
        )
        allowed = allowances.get(metric, 0.0)
        regressed = False
        if delta is not None:
            if direction == "up":
                regressed = delta > allowed
            else:
                regressed = -delta > allowed
        comparisons.append(
            {
                "metric": metric,
                "direction": direction,
                "baseline": baseline_value,
                "current": current_value,
                "delta": delta,
                "allowed_regression": allowed,
                "regressed": regressed,
                "available": baseline_value is not None and current_value is not None,
            }
        )
    return comparisons


def build_report(
    baseline_path: Path,
    current_path: Path,
    *,
    baseline_label: str | None = None,
    current_label: str | None = None,
    rules: Mapping[str, str] | None = None,
    allowances: Mapping[str, float] | None = None,
    fail_on_regression: bool = False,
) -> dict[str, Any]:
    baseline = collect_metrics(baseline_path, label=baseline_label)
    current = collect_metrics(current_path, label=current_label)
    comparisons = compare_metrics(baseline, current, rules or DEFAULT_RULES, allowances)
    regression_count = sum(1 for item in comparisons if item["regressed"])
    return {
        "generated_at": datetime.now(timezone.utc).astimezone().isoformat(timespec="seconds"),
        "status": "regression" if regression_count else "pass",
        "fail_on_regression": fail_on_regression,
        "exit_code": 1 if fail_on_regression and regression_count else 0,
        "regression_count": regression_count,
        "baseline": baseline,
        "current": current,
        "comparisons": comparisons,
    }


def _format_value(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, float):
        return f"{value:.4f}".rstrip("0").rstrip(".")
    return str(value)


def _table_row(values: Sequence[Any]) -> str:
    return "| " + " | ".join(_format_value(value).replace("|", "\\|") for value in values) + " |"


def render_markdown(report: Mapping[str, Any]) -> str:
    lines = [
        "# RAG Regression Gate",
        "",
        f"Status: `{report.get('status', '')}`",
        f"Fail on regression: `{str(report.get('fail_on_regression', False)).lower()}`",
        f"Regressions: `{report.get('regression_count', 0)}`",
        "",
        "## Runs",
        "",
        _table_row(["role", "label", "total_rows", "path"]),
        _table_row(["---", "---", "---:", "---"]),
    ]
    for role in ("baseline", "current"):
        run = report.get(role)
        if isinstance(run, Mapping):
            lines.append(
                _table_row(
                    [
                        role,
                        f"`{run.get('label', '')}`",
                        run.get("total_rows", ""),
                        f"`{run.get('path', '')}`",
                    ]
                )
            )

    lines.extend(
        [
            "",
            "## Checks",
            "",
            _table_row(
                [
                    "metric",
                    "direction",
                    "baseline",
                    "current",
                    "delta",
                    "allowed",
                    "status",
                ]
            ),
            _table_row(["---", "---", "---:", "---:", "---:", "---:", "---"]),
        ]
    )
    for item in report.get("comparisons", []):
        if not isinstance(item, Mapping):
            continue
        status = "regression" if item.get("regressed") else "ok"
        if not item.get("available"):
            status = "unavailable"
        lines.append(
            _table_row(
                [
                    f"`{item.get('metric', '')}`",
                    item.get("direction", ""),
                    item.get("baseline"),
                    item.get("current"),
                    item.get("delta"),
                    item.get("allowed_regression"),
                    status,
                ]
            )
        )
    lines.append("")
    return "\n".join(lines)


def render_text(report: Mapping[str, Any]) -> str:
    status = str(report.get("status", "")).upper()
    lines = [
        f"RAG regression gate: {status}",
        f"regressions: {report.get('regression_count', 0)}",
    ]
    for item in report.get("comparisons", []):
        if not isinstance(item, Mapping):
            continue
        if item.get("regressed"):
            lines.append(
                "{metric}: {baseline} -> {current} (delta {delta}, allowed {allowed})".format(
                    metric=item.get("metric"),
                    baseline=_format_value(item.get("baseline")),
                    current=_format_value(item.get("current")),
                    delta=_format_value(item.get("delta")),
                    allowed=_format_value(item.get("allowed_regression")),
                )
            )
    if len(lines) == 2:
        lines.append("no regressions found")
    return "\n".join(lines)


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("baseline", type=Path, help="Baseline diagnostics dir or diagnostics.json file.")
    parser.add_argument("current", type=Path, help="Current diagnostics dir or diagnostics.json file.")
    parser.add_argument("--baseline-label")
    parser.add_argument("--current-label")
    parser.add_argument(
        "--format",
        choices=("markdown", "text", "json"),
        default="markdown",
        help="Output format for stdout.",
    )
    parser.add_argument("--json", action="store_true", help="Alias for --format json.")
    parser.add_argument("--output", type=Path, help="Optional file to write the rendered report.")
    parser.add_argument(
        "--fail-on-regression",
        action="store_true",
        help="Exit 1 when any configured check regresses.",
    )
    parser.add_argument(
        "--check",
        action="append",
        default=[],
        metavar="METRIC:DIRECTION",
        help="Add or replace a check. Direction must be up or down.",
    )
    parser.add_argument(
        "--allow-regression",
        action="append",
        default=[],
        metavar="METRIC=NUMBER",
        help="Allow this much movement in the regressing direction for a metric.",
    )
    parser.add_argument(
        "--ignore-metric",
        action="append",
        default=[],
        metavar="METRIC",
        help="Remove a default check by metric name.",
    )
    parser.add_argument(
        "--no-default-checks",
        action="store_true",
        help="Use only checks supplied with --check.",
    )
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    try:
        output_format = "json" if args.json else args.format
        rules = parse_rules(
            no_default_checks=args.no_default_checks,
            checks=args.check,
            ignored=args.ignore_metric,
        )
        allowances = parse_allowances(args.allow_regression)
        report = build_report(
            args.baseline,
            args.current,
            baseline_label=args.baseline_label,
            current_label=args.current_label,
            rules=rules,
            allowances=allowances,
            fail_on_regression=args.fail_on_regression,
        )
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        raise SystemExit(str(exc)) from exc

    if output_format == "json":
        rendered = json.dumps(report, indent=2, sort_keys=True)
    elif output_format == "text":
        rendered = render_text(report)
    else:
        rendered = render_markdown(report)

    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(rendered + "\n", encoding="utf-8")
    print(rendered)
    return int(report["exit_code"])


if __name__ == "__main__":
    raise SystemExit(main())
