#!/usr/bin/env python3
"""Summarize run manifest JSONL records as compact Markdown."""

from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any


DEFAULT_MANIFEST_PATH = Path("artifacts/runs/run_manifest.jsonl")


def _as_list(value: Any) -> list[Any]:
    if value is None:
        return []
    if isinstance(value, list):
        return value
    return [value]


def _compact_list(value: Any, *, limit: int = 2) -> str:
    items = [str(item) for item in _as_list(value) if item not in (None, "")]
    if not items:
        return "-"
    shown = items[:limit]
    suffix = f" (+{len(items) - limit})" if len(items) > limit else ""
    return "<br>".join(_escape_markdown(item) for item in shown) + suffix


def _escape_markdown(value: object) -> str:
    text = str(value).replace("\n", " ")
    return text.replace("|", "\\|")


def load_manifest(path: Path) -> tuple[list[dict[str, Any]], int]:
    records: list[dict[str, Any]] = []
    malformed = 0
    if not path.exists():
        return records, malformed

    with path.open("r", encoding="utf-8") as handle:
        for line in handle:
            stripped = line.strip()
            if not stripped:
                continue
            try:
                payload = json.loads(stripped)
            except json.JSONDecodeError:
                malformed += 1
                continue
            if isinstance(payload, dict):
                records.append(payload)
            else:
                malformed += 1
    return records, malformed


def filter_records(
    records: list[dict[str, Any]],
    *,
    task: str | None = None,
    lane: str | None = None,
) -> list[dict[str, Any]]:
    selected = records
    if task:
        selected = [record for record in selected if record.get("task") == task]
    if lane:
        selected = [record for record in selected if record.get("lane") == lane]
    return selected


def render_markdown(
    records: list[dict[str, Any]],
    *,
    malformed_lines: int = 0,
    limit: int | None = None,
    task: str | None = None,
    lane: str | None = None,
) -> str:
    selected = filter_records(records, task=task, lane=lane)
    if limit is not None:
        selected = selected[-limit:]
    newest_first = list(reversed(selected))

    lines = [
        "# Run Manifest Summary",
        "",
        f"- Records shown: {len(newest_first)}",
        f"- Records matched: {len(selected)}",
        f"- Malformed lines skipped: {malformed_lines}",
    ]
    if task:
        lines.append(f"- Task filter: `{task}`")
    if lane:
        lines.append(f"- Lane filter: `{lane}`")

    lines.extend(
        [
            "",
            "| Generated | Task | Lane | Label | Commit | Changed | Validation |",
            "| --- | --- | --- | --- | --- | --- | --- |",
        ]
    )
    for record in newest_first:
        lines.append(
            "| {generated} | {task} | {lane} | {label} | {commit} | {changed} | {validation} |".format(
                generated=_escape_markdown(record.get("generated_at", "-") or "-"),
                task=_escape_markdown(record.get("task", "-") or "-"),
                lane=_escape_markdown(record.get("lane", "-") or "-"),
                label=_escape_markdown(record.get("label", "-") or "-"),
                commit=_escape_markdown(record.get("commit", "-") or "-"),
                changed=_compact_list(record.get("changed_file")),
                validation=_compact_list(record.get("validation")),
            )
        )

    return "\n".join(lines) + "\n"


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Summarize JSONL run manifest records as compact Markdown."
    )
    parser.add_argument(
        "--manifest",
        default=str(DEFAULT_MANIFEST_PATH),
        help="JSONL manifest path (default: artifacts/runs/run_manifest.jsonl).",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=20,
        help="Maximum matching records to show, newest first (default: 20).",
    )
    parser.add_argument("--task", help="Only include records for this task.")
    parser.add_argument("--lane", help="Only include records for this lane.")
    parser.add_argument("--output-md", help="Optional Markdown output path.")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    if args.limit < 1:
        raise SystemExit("--limit must be at least 1.")

    records, malformed = load_manifest(Path(args.manifest))
    markdown = render_markdown(
        records,
        malformed_lines=malformed,
        limit=args.limit,
        task=args.task,
        lane=args.lane,
    )
    if args.output_md:
        output_path = Path(args.output_md)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(markdown, encoding="utf-8")
    print(markdown, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
