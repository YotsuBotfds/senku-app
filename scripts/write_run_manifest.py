#!/usr/bin/env python3
"""Append normalized run metadata to a JSONL manifest."""

from __future__ import annotations

import argparse
import json
import subprocess
from datetime import datetime, timezone
from pathlib import Path


DEFAULT_MANIFEST_PATH = Path("artifacts/runs/run_manifest.jsonl")


def parse_metric(raw: str) -> tuple[str, object]:
    if "=" not in raw:
        raise argparse.ArgumentTypeError(
            "metric must be in KEY=VALUE form."
        )
    key, value = raw.split("=", 1)
    if not key:
        raise argparse.ArgumentTypeError("metric key must not be empty.")

    return key, parse_metric_value(value)


def parse_metric_value(value: str) -> object:
    lowered = value.lower()
    if lowered in {"true", "false"}:
        return lowered == "true"
    if lowered in {"null", "none", "nil"}:
        return None
    try:
        return int(value)
    except ValueError:
        pass

    try:
        return float(value)
    except ValueError:
        return value


def get_short_commit_value(override: str | None) -> str | None:
    if override is not None:
        return override

    try:
        result = subprocess.run(
            ["git", "rev-parse", "--short", "HEAD"],
            capture_output=True,
            text=True,
            check=False,
        )
        if result.returncode != 0:
            return None
        value = result.stdout.strip()
        return value or None
    except OSError:
        return None


def get_git_status_short() -> str | None:
    try:
        result = subprocess.run(
            ["git", "status", "--short"],
            capture_output=True,
            text=True,
            check=False,
        )
        if result.returncode != 0:
            return None
        return result.stdout
    except OSError:
        return None


def get_git_diff_stat() -> str | None:
    try:
        result = subprocess.run(
            ["git", "diff", "--stat"],
            capture_output=True,
            text=True,
            check=False,
        )
        if result.returncode != 0:
            return None
        return result.stdout
    except OSError:
        return None


def build_record(args: argparse.Namespace) -> dict[str, object]:
    status = get_git_status_short()
    return {
        "task": args.task,
        "lane": args.lane,
        "role": args.role,
        "model": args.model,
        "label": args.label,
        "command": args.command,
        "input": args.input,
        "output": args.output,
        "changed_file": args.changed_file,
        "validation": args.validation,
        "metric": args.metric,
        "note": args.note,
        "commit": get_short_commit_value(args.commit),
        "generated_at": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "cwd": str(Path.cwd()),
        "git_status_short": status,
        "git_diff_stat": args.diff_stat if args.diff_stat is not None else get_git_diff_stat(),
        "dirty": bool(status and status.strip()),
    }


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Append a run record to JSONL manifest.")
    parser.add_argument("--task", required=True, help="Task identifier.")
    parser.add_argument("--lane", required=True, help="Execution lane.")
    parser.add_argument("--role", help="Optional agent role, such as scout or worker.")
    parser.add_argument("--model", help="Optional model identifier used for the run.")
    parser.add_argument("--label", help="Optional human label.")
    parser.add_argument(
        "--command",
        action="append",
        default=[],
        help="Command used; repeatable.",
    )
    parser.add_argument(
        "--input",
        action="append",
        default=[],
        help="Input artifact; repeatable.",
    )
    parser.add_argument(
        "--output",
        action="append",
        default=[],
        help="Output artifact; repeatable.",
    )
    parser.add_argument(
        "--changed-file",
        action="append",
        default=[],
        help="File changed by the slice; repeatable.",
    )
    parser.add_argument(
        "--validation",
        action="append",
        default=[],
        help="Validation command or result; repeatable.",
    )
    parser.add_argument(
        "--metric",
        action="append",
        default=[],
        type=parse_metric,
        help="Metric key/value pair in KEY=VALUE form; repeatable.",
    )
    parser.add_argument(
        "--note",
        action="append",
        default=[],
        help="Note; repeatable.",
    )
    parser.add_argument(
        "--commit",
        help="Commit short hash override.",
    )
    parser.add_argument(
        "--diff-stat",
        help="Diff stat override. Defaults to best-effort `git diff --stat`.",
    )
    parser.add_argument(
        "--manifest-path",
        default=str(DEFAULT_MANIFEST_PATH),
        help="JSONL manifest path (default: artifacts/runs/run_manifest.jsonl).",
    )
    return parser.parse_args(argv)


def normalize_metrics(raw_metrics: list[tuple[str, object]]) -> dict[str, object]:
    metrics: dict[str, object] = {}
    for key, value in raw_metrics:
        metrics[key] = value
    return metrics


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    args.metric = normalize_metrics(args.metric)
    manifest_path = Path(args.manifest_path)
    manifest_path.parent.mkdir(parents=True, exist_ok=True)

    record = build_record(args)
    line = json.dumps(record)
    with manifest_path.open("a", encoding="utf-8") as handle:
        handle.write(line + "\n")

    print(line)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
