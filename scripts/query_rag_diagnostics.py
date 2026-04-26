#!/usr/bin/env python3
"""Query row-level records from a RAG diagnostics artifact."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any, Mapping, Sequence


DIAGNOSTICS_FILENAME = "diagnostics.json"
DEFAULT_COLUMNS = (
    "prompt_id",
    "suspected_failure_bucket",
    "expected_guide_ids",
    "cited_guide_ids",
    "top_retrieved_guide_ids",
    "top1_is_bridge",
    "top1_has_unresolved_partial",
    "answer_card_status",
    "app_acceptance_status",
    "app_acceptance_root_cause",
    "safety_surface_status",
    "ui_surface_bucket",
    "short_reason",
)
GUIDE_ID_FIELDS = (
    "expected_guide_ids",
    "top_retrieved_guide_ids",
    "cited_guide_ids",
)
CELL_WHITESPACE_RE = re.compile(r"\s+")


def _parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "diagnostics_path",
        type=Path,
        help="Path to diagnostics.json or a directory containing diagnostics.json.",
    )
    parser.add_argument(
        "--bucket",
        action="append",
        default=[],
        help="Filter by suspected_failure_bucket. Repeat to include multiple buckets.",
    )
    parser.add_argument(
        "--guide-id",
        action="append",
        default=[],
        help=(
            "Filter rows where the guide appears in expected, top retrieved, "
            "or cited guide ids. Repeat to include multiple guides."
        ),
    )
    parser.add_argument(
        "--prompt-id",
        action="append",
        default=[],
        help="Filter by prompt_id. Repeat to include multiple prompts.",
    )
    parser.add_argument(
        "--top1-unresolved-partial",
        action="store_true",
        help="Only include rows whose top-1 retrieved guide has unresolved partial markers.",
    )
    parser.add_argument(
        "--top1-bridge",
        action="store_true",
        help="Only include rows whose top-1 retrieved guide is a bridge guide.",
    )
    parser.add_argument(
        "--card-status",
        action="append",
        default=[],
        help="Filter by answer_card_status. Repeat to include multiple statuses.",
    )
    parser.add_argument(
        "--acceptance-root-cause",
        action="append",
        default=[],
        help="Filter by app_acceptance_root_cause. Repeat to include multiple causes.",
    )
    parser.add_argument(
        "--app-acceptance-status",
        action="append",
        default=[],
        help="Filter by app_acceptance_status. Repeat to include multiple statuses.",
    )
    parser.add_argument(
        "--safety-surface-status",
        action="append",
        default=[],
        help="Filter by safety_surface_status. Repeat to include multiple statuses.",
    )
    parser.add_argument(
        "--ui-surface-bucket",
        action="append",
        default=[],
        help="Filter by ui_surface_bucket. Repeat to include multiple buckets.",
    )
    parser.add_argument(
        "--json",
        action="store_true",
        help="Emit matching rows as JSON instead of a compact markdown table.",
    )
    return parser.parse_args(argv)


def resolve_diagnostics_path(path: Path) -> Path:
    if path.is_dir():
        path = path / DIAGNOSTICS_FILENAME
    if not path.is_file():
        raise FileNotFoundError(f"diagnostics.json not found: {path}")
    return path


def load_rows(path: Path) -> list[dict[str, Any]]:
    diagnostics_path = resolve_diagnostics_path(path)
    with diagnostics_path.open("r", encoding="utf-8") as handle:
        payload = json.load(handle)
    if not isinstance(payload, Mapping):
        raise ValueError(f"diagnostics payload must be an object: {diagnostics_path}")
    rows = payload.get("rows")
    if not isinstance(rows, list):
        raise ValueError(f"diagnostics payload is missing rows list: {diagnostics_path}")
    return [dict(row) for row in rows if isinstance(row, Mapping)]


def _split_ids(value: Any) -> set[str]:
    if value is None:
        return set()
    if isinstance(value, str):
        return {
            _normalize_filter_value(part) for part in value.split("|") if part.strip()
        }
    if isinstance(value, Sequence) and not isinstance(value, (str, bytes, bytearray)):
        return {_normalize_filter_value(part) for part in value if str(part).strip()}
    return {_normalize_filter_value(value)} if str(value).strip() else set()


def _normalize_filter_value(value: Any) -> str:
    return str(value).strip().casefold()


def _filter_set(values: Sequence[str]) -> set[str]:
    return {_normalize_filter_value(value) for value in values if str(value).strip()}


def _matches_filter(value: Any, allowed: set[str]) -> bool:
    return not allowed or _normalize_filter_value(value or "") in allowed


def _truthy_diagnostic(value: Any) -> bool:
    if isinstance(value, bool):
        return value
    if value is None:
        return False
    text = str(value).strip().lower()
    return text in {"1", "true", "yes", "y"}


def _row_has_guide(row: Mapping[str, Any], guide_ids: set[str]) -> bool:
    if not guide_ids:
        return True
    row_guide_ids: set[str] = set()
    for field in GUIDE_ID_FIELDS:
        row_guide_ids.update(_split_ids(row.get(field)))
    return bool(row_guide_ids.intersection(guide_ids))


def filter_rows(
    rows: Sequence[Mapping[str, Any]],
    *,
    buckets: Sequence[str] = (),
    guide_ids: Sequence[str] = (),
    prompt_ids: Sequence[str] = (),
    top1_unresolved_partial: bool = False,
    top1_bridge: bool = False,
    card_statuses: Sequence[str] = (),
    app_acceptance_statuses: Sequence[str] = (),
    acceptance_root_causes: Sequence[str] = (),
    safety_surface_statuses: Sequence[str] = (),
    ui_surface_buckets: Sequence[str] = (),
) -> list[dict[str, Any]]:
    bucket_set = _filter_set(buckets)
    guide_id_set = _filter_set(guide_ids)
    prompt_id_set = _filter_set(prompt_ids)
    card_status_set = _filter_set(card_statuses)
    app_acceptance_status_set = _filter_set(app_acceptance_statuses)
    root_cause_set = _filter_set(acceptance_root_causes)
    safety_surface_set = _filter_set(safety_surface_statuses)
    ui_surface_set = _filter_set(ui_surface_buckets)

    matches: list[dict[str, Any]] = []
    for row in rows:
        if not _matches_filter(row.get("suspected_failure_bucket"), bucket_set):
            continue
        if not _matches_filter(row.get("prompt_id"), prompt_id_set):
            continue
        if not _matches_filter(row.get("answer_card_status"), card_status_set):
            continue
        if not _matches_filter(
            row.get("app_acceptance_status"), app_acceptance_status_set
        ):
            continue
        if not _matches_filter(row.get("app_acceptance_root_cause"), root_cause_set):
            continue
        if not _matches_filter(row.get("safety_surface_status"), safety_surface_set):
            continue
        if not _matches_filter(row.get("ui_surface_bucket"), ui_surface_set):
            continue
        if top1_unresolved_partial and not _truthy_diagnostic(
            row.get("top1_has_unresolved_partial")
        ):
            continue
        if top1_bridge and not _truthy_diagnostic(row.get("top1_is_bridge")):
            continue
        if not _row_has_guide(row, guide_id_set):
            continue
        matches.append(dict(row))
    return matches


def _format_cell(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, bool):
        text = "true" if value else "false"
    else:
        text = str(value)
    text = CELL_WHITESPACE_RE.sub(" ", text).strip()
    return text.replace("|", "\\|")


def render_markdown(rows: Sequence[Mapping[str, Any]]) -> str:
    lines = [
        "| " + " | ".join(DEFAULT_COLUMNS) + " |",
        "| " + " | ".join("---" for _ in DEFAULT_COLUMNS) + " |",
    ]
    for row in rows:
        lines.append(
            "| "
            + " | ".join(_format_cell(row.get(column)) for column in DEFAULT_COLUMNS)
            + " |"
        )
    return "\n".join(lines)


def query_diagnostics(path: Path, args: argparse.Namespace) -> list[dict[str, Any]]:
    return filter_rows(
        load_rows(path),
        buckets=args.bucket,
        guide_ids=args.guide_id,
        prompt_ids=args.prompt_id,
        top1_unresolved_partial=args.top1_unresolved_partial,
        top1_bridge=args.top1_bridge,
        card_statuses=args.card_status,
        app_acceptance_statuses=args.app_acceptance_status,
        acceptance_root_causes=args.acceptance_root_cause,
        safety_surface_statuses=args.safety_surface_status,
        ui_surface_buckets=args.ui_surface_bucket,
    )


def main(argv: Sequence[str] | None = None) -> None:
    args = _parse_args(argv)
    rows = query_diagnostics(args.diagnostics_path, args)
    if args.json:
        print(json.dumps(rows, indent=2, sort_keys=True))
    else:
        print(render_markdown(rows))


if __name__ == "__main__":
    main()
