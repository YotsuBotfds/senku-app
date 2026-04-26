#!/usr/bin/env python3
"""Summarize current worktree changes by repo lane."""

from __future__ import annotations

import argparse
import json
import subprocess
from dataclasses import dataclass
from pathlib import Path
from typing import Any


LANE_ORDER = [
    "guides",
    "answer cards",
    "query/runtime",
    "android",
    "tests",
    "notes/dispatch",
    "artifacts/generated",
    "scripts/tooling",
    "other",
]

GENERATED_PREFIXES = (
    "artifacts/bench/",
    "artifacts/prompts/",
    "artifacts/runs/",
    "artifacts/mobile_pack/",
)

GENERATED_SUFFIXES = (
    ".pyc",
    ".pyo",
    ".tmp",
    ".log",
)

PROTECTED_BENIGN_UNTRACKED = {
    "notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md",
    "notes/PLANNER_HANDOFF_2026-04-25_POST_CLI_TERMINATION.md",
    "notes/PLANNER_HANDOFF_2026-04-26_AWAITING_DEEP_RESEARCH.md",
    "notes/PLANNER_HANDOFF_2026-04-26_POST_CARD5_PAUSE.md",
}


@dataclass(frozen=True)
class WorktreeEntry:
    status: str
    path: str
    lane: str
    old_path: str | None = None
    diff_stat: str | None = None


def normalize_path(path: str) -> str:
    return path.strip().strip('"').replace("\\", "/")


def lane_for_path(path: str) -> str:
    normalized = normalize_path(path)
    name = Path(normalized).name
    if normalized.startswith("guides/"):
        return "guides"
    if normalized.startswith("answer_cards/") or normalized.startswith("cards/"):
        return "answer cards"
    if normalized.startswith("android-app/"):
        return "android"
    if normalized.startswith("tests/") or name.startswith("test_"):
        return "tests"
    if normalized.startswith("notes/dispatch/") or normalized.startswith("notes/handoffs/"):
        return "notes/dispatch"
    if normalized.startswith("artifacts/"):
        return "artifacts/generated"
    if normalized.startswith("scripts/") or normalized.startswith("tools/"):
        return "scripts/tooling"
    if normalized in {
        "query.py",
        "config.py",
        "bench.py",
        "ingest.py",
        "guide_catalog.py",
        "deterministic_special_case_registry.py",
        "special_case_builders.py",
    } or normalized.startswith(
        (
            "query_",
            "rag_",
            "retrieval_",
            "runtime_",
            "bench_",
        )
    ):
        return "query/runtime"
    return "other"


def is_generated_noise(path: str, status: str = "") -> bool:
    normalized = normalize_path(path)
    if status == "!!":
        return True
    return normalized.startswith(GENERATED_PREFIXES) or normalized.endswith(GENERATED_SUFFIXES)


def parse_porcelain_status(
    status_text: str,
    *,
    include_generated: bool = False,
) -> tuple[list[WorktreeEntry], list[str]]:
    entries: list[WorktreeEntry] = []
    benign_untracked: list[str] = []
    for raw_line in status_text.splitlines():
        if not raw_line.strip():
            continue
        status = raw_line[:2]
        if status == "##":
            continue
        if len(raw_line) < 4 or raw_line[2] != " ":
            continue
        rest = raw_line[3:] if len(raw_line) > 3 else ""
        old_path: str | None = None
        path = rest
        if " -> " in rest:
            old_path, path = rest.split(" -> ", 1)
            old_path = normalize_path(old_path)
        path = normalize_path(path)
        if not path:
            continue
        if status == "??" and path in PROTECTED_BENIGN_UNTRACKED:
            benign_untracked.append(path)
            continue
        if not include_generated and is_generated_noise(path, status=status):
            continue
        entries.append(
            WorktreeEntry(
                status=status,
                path=path,
                old_path=old_path,
                lane=lane_for_path(path),
            )
        )
    return entries, benign_untracked


def parse_diff_stat(diff_stat_text: str) -> dict[str, str]:
    stats: dict[str, str] = {}
    for raw_line in diff_stat_text.splitlines():
        line = raw_line.strip()
        if not line or "|" not in line or line.startswith(("---", "files changed")):
            continue
        path_part, stat_part = line.split("|", 1)
        path = normalize_path(path_part)
        if " => " in path:
            path = normalize_path(path.split(" => ", 1)[1].replace("{", "").replace("}", ""))
        stats[path] = stat_part.strip()
    return stats


def attach_diff_stats(entries: list[WorktreeEntry], diff_stats: dict[str, str]) -> list[WorktreeEntry]:
    updated: list[WorktreeEntry] = []
    for entry in entries:
        stat = diff_stats.get(entry.path)
        updated.append(
            WorktreeEntry(
                status=entry.status,
                path=entry.path,
                lane=entry.lane,
                old_path=entry.old_path,
                diff_stat=stat,
            )
        )
    return updated


def summarize_worktree_delta(
    *,
    status_text: str,
    diff_stat_text: str = "",
    include_generated: bool = False,
) -> dict[str, Any]:
    entries, benign_untracked = parse_porcelain_status(
        status_text,
        include_generated=include_generated,
    )
    if diff_stat_text:
        entries = attach_diff_stats(entries, parse_diff_stat(diff_stat_text))

    grouped: dict[str, list[dict[str, str]]] = {lane: [] for lane in LANE_ORDER}
    for entry in entries:
        item = {"status": entry.status, "path": entry.path}
        if entry.old_path:
            item["old_path"] = entry.old_path
        if entry.diff_stat:
            item["diff_stat"] = entry.diff_stat
        grouped[entry.lane].append(item)

    lanes = [
        {"lane": lane, "count": len(items), "files": items}
        for lane, items in grouped.items()
        if items
    ]
    return {
        "total_changed": sum(lane["count"] for lane in lanes),
        "lanes": lanes,
        "excluded_generated_noise": not include_generated,
        "excluded_benign_untracked": benign_untracked,
        "excluded_benign_untracked_count": len(benign_untracked),
    }


def render_markdown(summary: dict[str, Any]) -> str:
    lines = [
        "# Worktree Delta",
        "",
        f"- Changed files: {summary['total_changed']}",
        f"- Generated artifact noise excluded: {str(summary['excluded_generated_noise']).lower()}",
        f"- Benign untracked excluded: {summary.get('excluded_benign_untracked_count', 0)}",
        "",
    ]
    if not summary["lanes"]:
        lines.append("No worktree changes found.")
        return "\n".join(lines) + "\n"

    for lane in summary["lanes"]:
        lines.append(f"## {lane['lane']} ({lane['count']})")
        for item in lane["files"]:
            rename = f" (from `{item['old_path']}`)" if item.get("old_path") else ""
            stat = f" - {item['diff_stat']}" if item.get("diff_stat") else ""
            lines.append(f"- `{item['status']}` `{item['path']}`{rename}{stat}")
        lines.append("")
    return "\n".join(lines).rstrip() + "\n"


def run_git(args: list[str]) -> str:
    completed = subprocess.run(args, check=True, capture_output=True, text=True)
    return completed.stdout


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Summarize git worktree changes by repository lane."
    )
    parser.add_argument(
        "--format",
        choices=("markdown", "json"),
        default="markdown",
        help="Output format (default: markdown).",
    )
    parser.add_argument(
        "--include-generated",
        action="store_true",
        help="Include ignored and generated artifact noise.",
    )
    parser.add_argument(
        "--no-diff-stat",
        action="store_true",
        help="Skip git diff --stat enrichment.",
    )
    parser.add_argument("--status-fixture", help="Use this porcelain status string instead of Git.")
    parser.add_argument("--diff-stat-fixture", help="Use this diff --stat string instead of Git.")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    status_text = (
        args.status_fixture
        if args.status_fixture is not None
        else run_git(["git", "status", "--porcelain=v1", "--ignored=matching"])
    )
    diff_stat_text = ""
    if not args.no_diff_stat:
        diff_stat_text = (
            args.diff_stat_fixture
            if args.diff_stat_fixture is not None
            else run_git(["git", "diff", "--stat"])
        )

    summary = summarize_worktree_delta(
        status_text=status_text,
        diff_stat_text=diff_stat_text,
        include_generated=args.include_generated,
    )
    if args.format == "json":
        print(json.dumps(summary, indent=2, sort_keys=True))
    else:
        print(render_markdown(summary), end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
