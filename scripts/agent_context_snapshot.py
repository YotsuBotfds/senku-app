#!/usr/bin/env python3
"""Emit a compact startup context snapshot for agents."""

from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path
from typing import Callable, Sequence

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.index_bench_artifacts import _format_summary, iter_bench_artifacts
from scripts.index_dispatch_notes import build_index as build_dispatch_index
from scripts.summarize_run_manifest import load_manifest, render_markdown as render_manifest
from scripts.worker_lane_status import collect_status as collect_worker_lane_status
from scripts.worker_lane_status import render_markdown as render_worker_lane_markdown


DEFAULT_MAX_LINES = 120
DEFAULT_DISPATCH_LIMIT = 8
DEFAULT_MANIFEST_LIMIT = 5
DEFAULT_ARTIFACT_LIMIT = 10

CommandRunner = Callable[[Sequence[str], Path], subprocess.CompletedProcess[str]]


def run_command(command: Sequence[str], cwd: Path) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        list(command),
        cwd=cwd,
        check=False,
        capture_output=True,
        text=True,
    )


def _escape_md(value: object) -> str:
    return str(value).replace("|", "\\|").replace("\n", " ")


def _relative_path(path: Path, repo_root: Path) -> str:
    try:
        return path.relative_to(repo_root).as_posix()
    except ValueError:
        return path.as_posix()


def _section(title: str, lines: Sequence[str]) -> list[str]:
    return [f"## {title}", "", *lines, ""]


def collect_git_summary(repo_root: Path, runner: CommandRunner = run_command) -> list[str]:
    head = runner(["git", "rev-parse", "--short", "HEAD"], repo_root)
    commits = runner(["git", "log", "-5", "--oneline", "--decorate"], repo_root)
    status = runner(["git", "status", "--short"], repo_root)

    lines: list[str] = []
    if head.returncode == 0:
        lines.append(f"- HEAD: `{head.stdout.strip() or 'unknown'}`")
    else:
        lines.append("- HEAD: unavailable")

    lines.append("")
    lines.append("### Latest Commits")
    if commits.returncode == 0 and commits.stdout.strip():
        lines.extend(f"- `{line}`" for line in commits.stdout.splitlines()[:5])
    else:
        lines.append("- No commit data available.")

    lines.append("")
    lines.append("### Working Tree")
    if status.returncode == 0:
        status_lines = [line for line in status.stdout.splitlines() if line.strip()]
        if status_lines:
            lines.extend(f"- `{line}`" for line in status_lines[:12])
            if len(status_lines) > 12:
                lines.append(f"- ... {len(status_lines) - 12} more status entries")
        else:
            lines.append("- Clean")
    else:
        lines.append("- Git status unavailable.")
    return lines


def collect_dispatch_summary(dispatch_dir: Path, *, limit: int = DEFAULT_DISPATCH_LIMIT) -> list[str]:
    if not dispatch_dir.is_dir():
        return [f"- Dispatch directory not found: `{dispatch_dir}`"]

    notes = sorted(
        build_dispatch_index(dispatch_dir, include_completed=False),
        key=lambda note: note.mtime,
        reverse=True,
    )
    lines = [
        f"- Active notes indexed: {len(notes)}",
        "",
        "| Slice | Title | Updated | Path | Proof / paths |",
        "| --- | --- | --- | --- | --- |",
    ]
    for note in notes[:limit]:
        proof_bits = list(note.proof_lines[:2]) + list(note.linked_paths[:2])
        proof = "<br>".join(_escape_md(bit) for bit in proof_bits) if proof_bits else ""
        lines.append(
            "| {slice_id} | {title} | {mtime} | {path} | {proof} |".format(
                slice_id=_escape_md(note.slice_id),
                title=_escape_md(note.title),
                mtime=_escape_md(note.mtime),
                path=_escape_md(note.path),
                proof=proof,
            )
        )
    if len(notes) > limit:
        lines.append(f"| ... | {len(notes) - limit} more active notes |  |  |  |")
    return lines


def collect_manifest_summary(
    manifest_path: Path, *, limit: int = DEFAULT_MANIFEST_LIMIT
) -> list[str]:
    records, malformed = load_manifest(manifest_path)
    if not records and not manifest_path.exists():
        return [f"- Run manifest not found: `{manifest_path}`"]
    markdown = render_manifest(records, malformed_lines=malformed, limit=limit)
    lines = markdown.splitlines()
    return lines[2:] if lines[:1] == ["# Run Manifest Summary"] else lines


def collect_artifact_summary(
    bench_dir: Path, *, limit: int = DEFAULT_ARTIFACT_LIMIT
) -> list[str]:
    if not bench_dir.is_dir():
        return [f"- Bench artifact directory not found: `{bench_dir}`"]

    records = sorted(
        iter_bench_artifacts(bench_dir),
        key=lambda record: str(record.get("mtime", "")),
        reverse=True,
    )
    lines = [
        f"- Artifacts indexed: {len(records)}",
        "",
        "| Path | Kind | Size | Mtime | Run | Summary |",
        "| --- | --- | ---: | --- | --- | --- |",
    ]
    for record in records[:limit]:
        hints = record.get("hints", {})
        summary = _format_summary(record.get("summary", {}))
        lines.append(
            "| {path} | {kind} | {size} | {mtime} | {run} | {summary} |".format(
                path=_escape_md(record.get("path", "")),
                kind=_escape_md(record.get("kind", "")),
                size=record.get("size", 0),
                mtime=_escape_md(record.get("mtime", "")),
                run=_escape_md(hints.get("run", "") if isinstance(hints, dict) else ""),
                summary=_escape_md(summary),
            )
        )
    if len(records) > limit:
        lines.append(f"| ... | {len(records) - limit} more artifacts |  |  |  |  |")
    return lines


def collect_worker_lane_summary(repo_root: Path, runner: CommandRunner = run_command) -> list[str]:
    status = collect_worker_lane_status(repo_root, runner=runner, include_dirty=False)
    markdown = render_worker_lane_markdown(status, limit=8)
    lines = markdown.splitlines()
    return lines[2:] if lines[:1] == ["# Worker Lane Status"] else lines


def cap_markdown_lines(markdown: str, max_lines: int) -> str:
    lines = markdown.rstrip("\n").splitlines()
    if len(lines) <= max_lines:
        return markdown.rstrip() + "\n"
    footer = f"... truncated to {max_lines} lines ..."
    return "\n".join(lines[: max_lines - 1] + [footer]) + "\n"


def build_snapshot(
    repo_root: Path,
    *,
    dispatch_dir: Path | None = None,
    manifest_path: Path | None = None,
    bench_dir: Path | None = None,
    max_lines: int = DEFAULT_MAX_LINES,
    runner: CommandRunner = run_command,
) -> str:
    dispatch_dir = dispatch_dir or repo_root / "notes" / "dispatch"
    manifest_path = manifest_path or repo_root / "artifacts" / "runs" / "run_manifest.jsonl"
    bench_dir = bench_dir or repo_root / "artifacts" / "bench"

    lines = [
        "# Agent Startup Context Snapshot",
        "",
        f"- Repo: `{repo_root}`",
        f"- Dispatch notes: `{_relative_path(dispatch_dir, repo_root)}`",
        f"- Run manifest: `{_relative_path(manifest_path, repo_root)}`",
        f"- Bench artifacts: `{_relative_path(bench_dir, repo_root)}`",
        "- Raw response bodies and log bodies are excluded.",
        "",
    ]
    lines.extend(_section("Git", collect_git_summary(repo_root, runner)))
    lines.extend(_section("Worker Lanes", collect_worker_lane_summary(repo_root, runner)))
    lines.extend(_section("Recent Dispatch Notes", collect_dispatch_summary(dispatch_dir)))
    lines.extend(_section("Recent Run Manifest", collect_manifest_summary(manifest_path)))
    lines.extend(_section("Latest Bench Artifact Pointers", collect_artifact_summary(bench_dir)))
    return cap_markdown_lines("\n".join(lines), max_lines)


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo-root", type=Path, default=Path.cwd())
    parser.add_argument("--dispatch-dir", type=Path)
    parser.add_argument("--manifest", type=Path)
    parser.add_argument("--bench-dir", type=Path)
    parser.add_argument("--output-md", type=Path, help="Optional Markdown output path.")
    parser.add_argument(
        "--max-lines",
        type=int,
        default=DEFAULT_MAX_LINES,
        help=f"Maximum Markdown lines to emit (default: {DEFAULT_MAX_LINES}).",
    )
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    if args.max_lines < 20:
        raise SystemExit("--max-lines must be at least 20.")

    repo_root = args.repo_root.resolve()
    markdown = build_snapshot(
        repo_root,
        dispatch_dir=args.dispatch_dir,
        manifest_path=args.manifest,
        bench_dir=args.bench_dir,
        max_lines=args.max_lines,
    )
    if args.output_md:
        args.output_md.parent.mkdir(parents=True, exist_ok=True)
        args.output_md.write_text(markdown, encoding="utf-8")
    print(markdown, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
