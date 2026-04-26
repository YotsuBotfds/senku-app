#!/usr/bin/env python3
"""Export compact Markdown context from RAG diagnostics."""

from __future__ import annotations

import argparse
import json
import sys
from collections import Counter
from pathlib import Path
from typing import Any


DEFAULT_OUTPUT_NAME = "llm_context.md"
GOOD_BUCKETS = {"expected_supported", "deterministic_pass"}
INTERESTING_STATUSES = {
    "app_acceptance_status": {"needs_evidence_owner", "uncertain_fit_accepted"},
    "answer_card_status": {"fail", "missing_required", "forbidden_hit"},
    "evidence_nugget_status": {"missing", "contradicted", "fail"},
    "claim_support_status": {"fail", "unknown", "forbidden", "no_generated_answer"},
}
ROW_TEXT_FIELDS = (
    "prompt_text",
    "short_reason",
    "app_acceptance_reason",
    "error",
)


def load_diagnostics(path: Path) -> dict[str, Any]:
    source = path / "diagnostics.json" if path.is_dir() else path
    with source.open("r", encoding="utf-8") as handle:
        data = json.load(handle)
    if not isinstance(data, dict):
        raise ValueError(f"diagnostics must be a JSON object: {source}")
    rows = data.get("rows")
    if not isinstance(rows, list):
        raise ValueError(f"diagnostics missing rows list: {source}")
    summary = data.get("summary")
    if not isinstance(summary, dict):
        raise ValueError(f"diagnostics missing summary object: {source}")
    return data


def truncate(value: Any, limit: int) -> str:
    text = str(value or "").replace("\r", " ").replace("\n", " ").strip()
    text = " ".join(text.split())
    if len(text) <= limit:
        return text
    if limit <= 3:
        return text[:limit]
    return text[: limit - 3].rstrip() + "..."


def pipe_list(value: Any, limit: int = 5) -> str:
    parts = [part for part in str(value or "").split("|") if part]
    if parts == ["none"]:
        return ""
    if not parts:
        return ""
    shown = parts[:limit]
    suffix = f" +{len(parts) - limit}" if len(parts) > limit else ""
    return "|".join(shown) + suffix


def table_escape(value: Any) -> str:
    return str(value or "").replace("\r", " ").replace("\n", " ").replace("|", "\\|")


def counter_from_rows(rows: list[dict[str, Any]], field: str) -> Counter[str]:
    counter: Counter[str] = Counter()
    for row in rows:
        value = str(row.get(field) or "").strip()
        if value:
            counter[value] += 1
    return counter


def counter_from_summary_or_rows(
    summary: dict[str, Any],
    summary_field: str,
    rows: list[dict[str, Any]],
    row_field: str,
) -> dict[str, Any] | Counter[str]:
    value = summary.get(summary_field)
    if isinstance(value, dict):
        return value
    return counter_from_rows(rows, row_field)


def format_counter(counter: dict[str, Any] | Counter[str]) -> str:
    if not counter:
        return "`none`"
    return ", ".join(
        f"`{key}`: {counter[key]}" for key in sorted(counter, key=lambda item: str(item))
    )


def artifact_links(source: Path) -> list[tuple[str, Path]]:
    root = source if source.is_dir() else source.parent
    names = ("diagnostics.json", "diagnostics.csv", "report.md")
    return [(name, root / name) for name in names if (root / name).exists()]


def is_interesting_row(row: dict[str, Any]) -> bool:
    bucket = str(row.get("suspected_failure_bucket") or "").strip()
    if bucket and bucket not in GOOD_BUCKETS:
        return True
    if str(row.get("top1_marker_risk") or "").strip() in {"fail", "warn"}:
        return True
    if str(row.get("top1_has_unresolved_partial") or "").strip() == "yes":
        return True
    for field, statuses in INTERESTING_STATUSES.items():
        if str(row.get(field) or "").strip() in statuses:
            return True
    return bool(str(row.get("error") or "").strip())


def compact_rows(
    rows: list[dict[str, Any]],
    max_rows: int,
    text_limit: int,
) -> list[dict[str, Any]]:
    selected = [row for row in rows if is_interesting_row(row)]
    return selected[:max_rows]


def render_markdown(
    data: dict[str, Any],
    source: Path,
    *,
    max_rows: int = 30,
    text_limit: int = 180,
) -> str:
    summary = data["summary"]
    rows = [row for row in data["rows"] if isinstance(row, dict)]
    selected_rows = compact_rows(rows, max_rows=max_rows, text_limit=text_limit)

    row_bucket_counts = counter_from_rows(rows, "suspected_failure_bucket")
    summary_bucket_counts = summary.get("by_bucket") or {}
    marker_risk_counts = summary.get("top1_marker_risk_counts") or counter_from_rows(
        rows, "top1_marker_risk"
    )

    lines = [
        "# Compact RAG Context",
        "",
        f"- Source: `{source}`",
        f"- Generated at: `{data.get('generated_at', 'unknown')}`",
        f"- Total rows: `{summary.get('total_rows', len(rows))}`",
        f"- Bad/interesting rows shown: `{len(selected_rows)}` of `{sum(1 for row in rows if is_interesting_row(row))}`",
        f"- Bucket counts: {format_counter(summary_bucket_counts)}",
        f"- Marker risk counts: {format_counter(marker_risk_counts)}",
        f"- Top-1 bridge rows: `{summary.get('top1_bridge_rows', 0)}`",
        f"- Top-1 unresolved-partial rows: `{summary.get('top1_unresolved_partial_rows', 0)}`",
        "",
        "## Retrieval Counts",
        "",
        f"- Expected guide rows: `{summary.get('expected_guide_rows', 0)}`",
        f"- Hit@1: `{summary.get('expected_hit_at_1', 'n/a')}`",
        f"- Hit@3: `{summary.get('expected_hit_at_3', 'n/a')}`",
        f"- Hit@k: `{summary.get('expected_hit_at_k', 'n/a')}`",
        f"- Expected cited: `{summary.get('expected_cited', 'n/a')}`",
        f"- Row bucket recount: {format_counter(row_bucket_counts)}",
        "",
        "## App/Card/Claim/Evidence",
        "",
        f"- App acceptance: {format_counter(summary.get('app_acceptance_counts') or counter_from_rows(rows, 'app_acceptance_status'))}",
        f"- App acceptance root causes: {format_counter(counter_from_summary_or_rows(summary, 'app_acceptance_root_cause_counts', rows, 'app_acceptance_root_cause'))}",
        f"- Evidence owner: {format_counter(summary.get('evidence_owner_counts') or counter_from_rows(rows, 'evidence_owner_status'))}",
        f"- Answer cards: {format_counter(summary.get('answer_card_counts') or counter_from_rows(rows, 'answer_card_status'))}",
        f"- Evidence nuggets: {format_counter(summary.get('evidence_nugget_counts') or counter_from_rows(rows, 'evidence_nugget_status'))}",
        f"- Claim support: {format_counter(summary.get('claim_support_counts') or counter_from_rows(rows, 'claim_support_status'))}",
        "",
        "## Bad/Interesting Rows",
        "",
        "| idx | id | bucket | app | app_acceptance_root_cause | safety_surface_status | ui_surface_bucket | card | claim | evidence | expected | top retrieved | markers | text |",
        "| ---: | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |",
    ]

    for row in selected_rows:
        marker_bits = [
            str(row.get("top1_marker_risk") or ""),
            "bridge" if str(row.get("top1_is_bridge") or "") == "yes" else "",
            "unresolved" if str(row.get("top1_has_unresolved_partial") or "") == "yes" else "",
            pipe_list(row.get("top1_marker_types"), limit=3),
        ]
        marker_text = ",".join(bit for bit in marker_bits if bit) or "none"
        row_text = " / ".join(
            truncate(row.get(field), text_limit)
            for field in ROW_TEXT_FIELDS
            if truncate(row.get(field), text_limit)
        )
        lines.append(
            "| "
            + " | ".join(
                table_escape(value)
                for value in (
                    row.get("prompt_index", ""),
                    row.get("prompt_id", ""),
                    row.get("suspected_failure_bucket", ""),
                    row.get("app_acceptance_status", ""),
                    row.get("app_acceptance_root_cause", ""),
                    row.get("safety_surface_status", ""),
                    row.get("ui_surface_bucket", ""),
                    row.get("answer_card_status", ""),
                    row.get("claim_support_status", ""),
                    row.get("evidence_nugget_status", ""),
                    pipe_list(row.get("expected_guide_ids")),
                    pipe_list(row.get("top_retrieved_guide_ids")),
                    marker_text,
                    row_text,
                )
            )
            + " |"
        )

    if sum(1 for row in rows if is_interesting_row(row)) > len(selected_rows):
        lines.extend(
            [
                "",
                f"_Rows capped at {max_rows}; rerun with `--max-rows` for more._",
            ]
        )

    links = artifact_links(source)
    if links:
        lines.extend(["", "## Artifact Links", ""])
        for name, path in links:
            lines.append(f"- [{name}]({path.as_posix()})")

    lines.append("")
    return "\n".join(lines)


def write_context(
    source: Path,
    output: Path | None,
    *,
    max_rows: int,
    text_limit: int,
) -> str:
    data = load_diagnostics(source)
    markdown = render_markdown(
        data,
        source,
        max_rows=max_rows,
        text_limit=text_limit,
    )
    if output is not None:
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(markdown, encoding="utf-8")
    return markdown


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("diagnostics", type=Path, help="Diagnostics directory or diagnostics.json")
    parser.add_argument(
        "-o",
        "--output",
        type=Path,
        help=f"Markdown output path. Defaults to <diagnostics-dir>/{DEFAULT_OUTPUT_NAME}.",
    )
    parser.add_argument("--stdout", action="store_true", help="Print Markdown to stdout")
    parser.add_argument("--max-rows", type=int, default=30, help="Maximum rows to include")
    parser.add_argument("--text-limit", type=int, default=180, help="Maximum text per row field")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    source = args.diagnostics
    output = args.output
    if output is None and not args.stdout:
        output = (source if source.is_dir() else source.parent) / DEFAULT_OUTPUT_NAME
    markdown = write_context(
        source,
        output,
        max_rows=args.max_rows,
        text_limit=args.text_limit,
    )
    if args.stdout:
        sys.stdout.write(markdown)
    elif output is not None:
        print(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
