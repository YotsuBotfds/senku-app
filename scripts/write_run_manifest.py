#!/usr/bin/env python3
"""Append normalized run metadata to a JSONL manifest."""

from __future__ import annotations

import argparse
import json
import os
import subprocess
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Callable, Sequence


DEFAULT_MANIFEST_PATH = Path("artifacts/runs/run_manifest.jsonl")
DEFAULT_CHANGED_FILE_LIMIT = 40
DEFAULT_STATUS_LIMIT = 80
DEFAULT_ARTIFACT_PATH_LIMIT = 20


@dataclass(frozen=True)
class CommandResult:
    stdout: str
    stderr: str = ""
    returncode: int = 0
    error: str | None = None


GitRunner = Callable[[Sequence[str]], CommandResult]


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


def run_git(args: Sequence[str]) -> CommandResult:
    try:
        result = subprocess.run(
            ["git", *args],
            capture_output=True,
            text=True,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        return CommandResult(stdout="", stderr="", returncode=1, error=exc.__class__.__name__)
    return CommandResult(
        stdout=result.stdout,
        stderr=result.stderr,
        returncode=result.returncode,
    )


def _safe_git(git_runner: GitRunner, args: Sequence[str]) -> CommandResult:
    try:
        return git_runner(args)
    except (OSError, subprocess.TimeoutExpired) as exc:
        return CommandResult(stdout="", stderr="", returncode=1, error=exc.__class__.__name__)


def _git_error(result: CommandResult) -> str | None:
    if result.error:
        return result.error
    if result.returncode:
        return (result.stderr or result.stdout or f"git exited {result.returncode}").strip()
    return None


def _clean_git_value(result: CommandResult) -> str | None:
    if _git_error(result):
        return None
    value = result.stdout.strip()
    return value or None


def _cap_lines(text: str, limit: int) -> tuple[str, bool]:
    lines = text.splitlines()
    if len(lines) <= limit:
        return text, False
    return "\n".join(lines[:limit]) + "\n", True


def _normalize_status_path(raw_path: str) -> str:
    path = raw_path.strip()
    if " -> " in path:
        path = path.split(" -> ", 1)[1].strip()
    if len(path) >= 2 and path[0] == path[-1] == '"':
        path = path[1:-1]
    return path.replace("\\", "/")


def parse_git_status_short(
    status_text: str,
    *,
    entry_limit: int = DEFAULT_STATUS_LIMIT,
    changed_file_limit: int = DEFAULT_CHANGED_FILE_LIMIT,
) -> dict[str, object]:
    counts: dict[str, int] = {}
    entries: list[dict[str, str]] = []
    changed_files: list[str] = []
    total = 0
    tracked = 0
    untracked = 0
    staged = 0
    unstaged = 0

    for raw_line in status_text.splitlines():
        if not raw_line.strip():
            continue
        total += 1
        status = raw_line[:2] if len(raw_line) >= 2 else raw_line.strip()
        path = _normalize_status_path(raw_line[3:] if len(raw_line) > 3 else "")
        kind = "untracked" if status == "??" else "tracked"
        counts[status] = counts.get(status, 0) + 1
        if kind == "untracked":
            untracked += 1
        else:
            tracked += 1
        if status[:1] not in {" ", "?"}:
            staged += 1
        if len(status) > 1 and status[1:2] not in {" ", "?"}:
            unstaged += 1
        if path and len(changed_files) < changed_file_limit:
            changed_files.append(path)
        if len(entries) < entry_limit:
            entries.append({"status": status, "path": path, "kind": kind})

    return {
        "clean": total == 0,
        "total_changed": total,
        "tracked_changed": tracked,
        "untracked_changed": untracked,
        "staged_changed": staged,
        "unstaged_changed": unstaged,
        "status_counts": counts,
        "entries": entries,
        "truncated": total > len(entries),
        "changed_files": changed_files,
        "changed_file_count": total,
        "changed_file_truncated": total > len(changed_files),
    }


def get_short_commit_value(
    override: str | None,
    git_runner: GitRunner = run_git,
) -> str | None:
    if override is not None:
        return override
    return _clean_git_value(_safe_git(git_runner, ["rev-parse", "--short", "HEAD"]))


def get_git_status_short(
    git_runner: GitRunner = run_git,
    *,
    limit: int = DEFAULT_STATUS_LIMIT,
) -> str | None:
    result = _safe_git(git_runner, ["status", "--short", "--untracked-files=all"])
    if _git_error(result):
        return None
    return _cap_lines(result.stdout, limit)[0]


def get_git_diff_stat(git_runner: GitRunner = run_git) -> str | None:
    result = _safe_git(git_runner, ["diff", "--stat"])
    if _git_error(result):
        return None
    return result.stdout


def get_repo_root(git_runner: GitRunner = run_git) -> Path:
    value = _clean_git_value(_safe_git(git_runner, ["rev-parse", "--show-toplevel"]))
    if value:
        return Path(value)
    return Path.cwd()


def collect_git_state(
    *,
    changed_file_limit: int = DEFAULT_CHANGED_FILE_LIMIT,
    status_limit: int = DEFAULT_STATUS_LIMIT,
    git_runner: GitRunner = run_git,
) -> dict[str, object]:
    status_result = _safe_git(git_runner, ["status", "--short", "--untracked-files=all"])
    branch_result = _safe_git(git_runner, ["branch", "--show-current"])
    head_short_result = _safe_git(git_runner, ["rev-parse", "--short", "HEAD"])
    head_full_result = _safe_git(git_runner, ["rev-parse", "HEAD"])
    diff_stat_result = _safe_git(git_runner, ["diff", "--stat"])

    status_error = _git_error(status_result)
    if status_error:
        status_summary: dict[str, object] = {
            "clean": False,
            "total_changed": 0,
            "tracked_changed": 0,
            "untracked_changed": 0,
            "staged_changed": 0,
            "unstaged_changed": 0,
            "status_counts": {},
            "entries": [],
            "truncated": False,
            "changed_files": [],
            "changed_file_count": 0,
            "changed_file_truncated": False,
            "error": status_error,
        }
        status_text = None
        status_truncated = False
    else:
        status_summary = parse_git_status_short(
            status_result.stdout,
            entry_limit=status_limit,
            changed_file_limit=changed_file_limit,
        )
        status_text, status_truncated = _cap_lines(status_result.stdout, status_limit)

    return {
        "head_short": _clean_git_value(head_short_result),
        "head": _clean_git_value(head_full_result),
        "branch": _clean_git_value(branch_result),
        "status_short": status_text,
        "status_short_truncated": status_truncated,
        "status_summary": status_summary,
        "diff_stat": _clean_git_value(diff_stat_result)
        if _clean_git_value(diff_stat_result)
        else (None if _git_error(diff_stat_result) else diff_stat_result.stdout),
        "error": status_error,
    }


def _is_artifact_path(value: str) -> bool:
    normalized = value.replace("\\", "/").strip()
    if not normalized:
        return False
    return normalized == "artifacts" or normalized.startswith("artifacts/") or "/artifacts/" in normalized


def _portable_path(path: Path) -> str:
    return path.as_posix()


def _normalize_artifact_path(value: str, repo_root: Path) -> tuple[str, Path] | None:
    raw_path = value.strip()
    if not raw_path:
        return None

    portable_raw_path = raw_path.replace("\\", "/")
    real_path = Path(raw_path)
    if not real_path.is_absolute():
        real_path = repo_root.joinpath(*portable_raw_path.split("/"))

    try:
        relative_path = real_path.resolve(strict=False).relative_to(
            repo_root.resolve(strict=False)
        )
    except ValueError:
        return None
    else:
        record_path = _portable_path(relative_path)

    if not _is_artifact_path(record_path):
        return None
    return record_path, real_path


def _artifact_path_key(path: Path) -> str:
    normalized = _portable_path(path.resolve(strict=False))
    if os.name == "nt":
        return normalized.casefold()
    return normalized


def collect_artifact_paths(
    values: Sequence[str],
    *,
    limit: int = DEFAULT_ARTIFACT_PATH_LIMIT,
    repo_root: Path | None = None,
) -> dict[str, object]:
    if limit < 1:
        return {
            "paths": [],
            "evidence_paths": {},
            "count": 0,
            "truncated": False,
        }

    paths: list[str] = []
    evidence_paths: dict[str, Path] = {}
    seen: set[str] = set()
    total_unique = 0
    root = repo_root or Path.cwd()
    for value in values:
        normalized = _normalize_artifact_path(value, root)
        if normalized is None:
            continue
        record_path, real_path = normalized
        key = _artifact_path_key(real_path)
        if key in seen:
            continue
        seen.add(key)
        evidence_paths[record_path] = real_path
        total_unique += 1
        if len(paths) >= limit:
            continue
        paths.append(record_path)
    return {
        "paths": paths,
        "evidence_paths": evidence_paths,
        "count": total_unique,
        "truncated": total_unique > len(paths),
    }


def _artifact_kind(path: Path) -> str:
    if path.is_file():
        return "file"
    if path.is_dir():
        return "directory"
    return "other"


def _format_mtime(path: Path) -> str | None:
    try:
        return datetime.fromtimestamp(path.stat().st_mtime, timezone.utc).isoformat(
            timespec="seconds"
        )
    except OSError:
        return None


def collect_artifact_path_evidence(
    paths: Sequence[str],
    *,
    evidence_paths: dict[str, Path] | None = None,
    repo_root: Path | None = None,
) -> dict[str, object]:
    entries: list[dict[str, object]] = []
    missing: list[str] = []
    root = repo_root or Path.cwd()
    for value in paths:
        path = (evidence_paths or {}).get(value, root / Path(value))
        try:
            exists = path.exists()
        except OSError as exc:
            entries.append(
                {
                    "path": value,
                    "exists": False,
                    "kind": "unknown",
                    "error": exc.__class__.__name__,
                }
            )
            missing.append(value)
            continue

        entry: dict[str, object] = {
            "path": value,
            "exists": exists,
            "kind": _artifact_kind(path) if exists else "missing",
        }
        if exists:
            entry["modified_at"] = _format_mtime(path)
        else:
            missing.append(value)
        entries.append(entry)

    return {
        "entries": entries,
        "missing": missing,
        "missing_count": len(missing),
    }


def build_record(
    args: argparse.Namespace,
    *,
    git_runner: GitRunner = run_git,
) -> dict[str, object]:
    repo_root = get_repo_root(git_runner)
    git_state = collect_git_state(
        changed_file_limit=args.changed_file_limit,
        status_limit=args.status_limit,
        git_runner=git_runner,
    )
    status_summary = git_state["status_summary"]
    auto_changed_files = (
        list(status_summary.get("changed_files", []))
        if isinstance(status_summary, dict)
        else []
    )
    changed_files = args.changed_file or auto_changed_files
    changed_file_source = "explicit" if args.changed_file else ("git_status" if auto_changed_files else "none")
    artifact_summary = collect_artifact_paths(
        [*args.input, *args.output, *changed_files],
        limit=args.artifact_path_limit,
        repo_root=repo_root,
    )
    artifact_evidence = collect_artifact_path_evidence(
        artifact_summary["paths"],
        evidence_paths=artifact_summary["evidence_paths"],
        repo_root=repo_root,
    )
    generated_at = datetime.now(timezone.utc).isoformat(timespec="seconds")
    return {
        "task": args.task,
        "lane": args.lane,
        "role": args.role,
        "model": args.model,
        "label": args.label,
        "command": args.command,
        "input": args.input,
        "output": args.output,
        "changed_file": changed_files,
        "changed_file_source": changed_file_source,
        "changed_file_count": len(args.changed_file)
        if args.changed_file
        else (
            int(status_summary.get("changed_file_count", 0))
            if isinstance(status_summary, dict)
            else 0
        ),
        "changed_file_truncated": False
        if args.changed_file
        else (
            bool(status_summary.get("changed_file_truncated", False))
            if isinstance(status_summary, dict)
            else False
        ),
        "validation": args.validation,
        "metric": args.metric,
        "note": args.note,
        "commit": args.commit if args.commit is not None else git_state["head_short"],
        "git_head": git_state["head"],
        "git_branch": git_state["branch"],
        "generated_at": generated_at,
        "cwd": str(Path.cwd()),
        "git_status_short": git_state["status_short"],
        "git_status_short_truncated": git_state["status_short_truncated"],
        "git_status_summary": status_summary,
        "git_diff_stat": args.diff_stat if args.diff_stat is not None else git_state["diff_stat"],
        "dirty": bool(
            isinstance(status_summary, dict)
            and int(status_summary.get("total_changed", 0)) > 0
        ),
        "artifact_path": artifact_summary["paths"],
        "artifact_path_count": artifact_summary["count"],
        "artifact_path_truncated": artifact_summary["truncated"],
        "artifact_path_evidence": artifact_evidence["entries"],
        "artifact_path_missing": artifact_evidence["missing"],
        "artifact_path_missing_count": artifact_evidence["missing_count"],
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
        "--changed-file-limit",
        type=int,
        default=DEFAULT_CHANGED_FILE_LIMIT,
        help=f"Maximum auto-detected changed files to record (default: {DEFAULT_CHANGED_FILE_LIMIT}).",
    )
    parser.add_argument(
        "--status-limit",
        type=int,
        default=DEFAULT_STATUS_LIMIT,
        help=f"Maximum git status lines to record (default: {DEFAULT_STATUS_LIMIT}).",
    )
    parser.add_argument(
        "--artifact-path-limit",
        type=int,
        default=DEFAULT_ARTIFACT_PATH_LIMIT,
        help=f"Maximum relevant artifact paths to record (default: {DEFAULT_ARTIFACT_PATH_LIMIT}).",
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
    if args.changed_file_limit < 1:
        raise SystemExit("--changed-file-limit must be at least 1.")
    if args.status_limit < 1:
        raise SystemExit("--status-limit must be at least 1.")
    if args.artifact_path_limit < 1:
        raise SystemExit("--artifact-path-limit must be at least 1.")
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
