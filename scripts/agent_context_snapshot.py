#!/usr/bin/env python3
"""Emit a compact startup context snapshot for agents."""

from __future__ import annotations

import argparse
import re
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
STRICT_RETRIEVAL_WORKFLOW = Path(".github/workflows/strict_retrieval_head_health.yml")
PROTECTED_BENIGN_UNTRACKED = {
    "notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md",
    "notes/PLANNER_HANDOFF_2026-04-25_POST_CLI_TERMINATION.md",
    "notes/PLANNER_HANDOFF_2026-04-26_AWAITING_DEEP_RESEARCH.md",
    "notes/PLANNER_HANDOFF_2026-04-26_POST_CARD5_PAUSE.md",
}

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


def _split_actionable_status_lines(
    status_text: str,
    *,
    benign_untracked_paths: set[str] = PROTECTED_BENIGN_UNTRACKED,
) -> tuple[list[str], list[str]]:
    benign_paths = {path.replace("\\", "/") for path in benign_untracked_paths}
    actionable: list[str] = []
    benign: list[str] = []
    for line in status_text.splitlines():
        if not line.strip():
            continue
        status = line[:2]
        path = (line[3:].strip() if len(line) > 3 else "").replace("\\", "/")
        normalized_line = f"{status} {path}".rstrip()
        if status == "??" and path in benign_paths:
            benign.append(normalized_line)
        else:
            actionable.append(normalized_line)
    return actionable, benign


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
        actionable_lines, benign_lines = _split_actionable_status_lines(status.stdout)
        if actionable_lines:
            lines.extend(f"- `{line}`" for line in actionable_lines[:12])
            if len(actionable_lines) > 12:
                lines.append(f"- ... {len(actionable_lines) - 12} more actionable status entries")
        else:
            lines.append("- Clean actionable tree")
        if benign_lines:
            lines.append(
                "- Benign untracked: "
                + ", ".join(f"`{line[3:]}`" for line in benign_lines[:4])
            )
            if len(benign_lines) > 4:
                lines.append(f"- ... {len(benign_lines) - 4} more benign untracked entries")
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


def _extract_yaml_scalar(yaml_text: str, key: str) -> str | None:
    assignment_pattern = rf"(?m)^(\s*{re.escape(key)}:\s*)(.*)$"
    for _, value_part in re.findall(assignment_pattern, yaml_text):
        parsed = _parse_yaml_scalar(value_part)
        if parsed is not None:
            return parsed
    return None


def _parse_yaml_scalar(value_text: str) -> str | None:
    value = value_text.strip()
    if not value:
        return ""

    if value[0] == "'":
        i = 1
        chunks: list[str] = []
        while i < len(value):
            char = value[i]
            if char == "'":
                if i + 1 < len(value) and value[i + 1] == "'":
                    chunks.append("'")
                    i += 2
                    continue
                break
            chunks.append(char)
            i += 1
        return "".join(chunks).strip()

    if value[0] == '"':
        i = 1
        chunks: list[str] = []
        while i < len(value):
            char = value[i]
            if char == "\\" and i + 1 < len(value):
                i += 1
                escapes = {
                    "n": "\n",
                    "r": "\r",
                    "t": "\t",
                    '"': '"',
                    "\\": "\\",
                    "/": "/",
                }
                escaped = value[i]
                chunks.append(escapes.get(escaped, escaped))
                i += 1
                continue
            if char == '"':
                break
            chunks.append(char)
            i += 1
        return "".join(chunks).strip()

    # Plain style scalar: strip unescaped inline comment.
    comment_idx = None
    for idx, char in enumerate(value):
        if char == "#":
            if idx == 0 or value[idx - 1].isspace():
                comment_idx = idx
                break
    if comment_idx is not None:
        return value[:comment_idx].strip()

    return value.strip()


def _metadata_audit_signal_lines(records: list[dict]) -> list[str]:
    latest_audit_record = None
    for record in reversed(records):
        if record.get("lane") != "metadata-audit":
            continue
        metric = record.get("metric")
        if isinstance(metric, dict) and metric:
            latest_audit_record = record
            break
    if latest_audit_record is None:
        return []

    metric = latest_audit_record.get("metric", {})
    signal_bits: list[str] = []
    for key in ("malformed_frontmatter_count",):
        if key in metric:
            signal_bits.append(f"{key}={metric[key]}")
    if not signal_bits:
        return []

    label = latest_audit_record.get("label") or latest_audit_record.get("lane") or "metadata-audit"
    return [f"- Metadata-audit signal ({label}): " + ", ".join(signal_bits)]


def _strict_retrieval_workflow_signal(repo_root: Path) -> list[str]:
    workflow_path = repo_root / STRICT_RETRIEVAL_WORKFLOW
    if not workflow_path.exists():
        return []
    text = workflow_path.read_text(encoding="utf-8")
    if "strict-retrieval-head-health" not in text:
        return []

    mode = _extract_yaml_scalar(text, "mode")
    allow_retrieval_warnings = _extract_yaml_scalar(text, "allow_retrieval_warnings")
    retrieval_index_flavor = _extract_yaml_scalar(text, "retrieval_index_flavor")
    if not any((mode, allow_retrieval_warnings, retrieval_index_flavor)):
        return ["- Strict-retrieval workflow file present (status details unavailable)."]

    pieces = []
    if mode is not None:
        pieces.append(f"mode={mode}")
    if allow_retrieval_warnings is not None:
        pieces.append(f"allow_retrieval_warnings={allow_retrieval_warnings}")
    if retrieval_index_flavor is not None:
        pieces.append(f"retrieval_index_flavor={retrieval_index_flavor}")
    return ["- Strict-retrieval head-health workflow configured: " + ", ".join(pieces)]


def collect_manifest_summary(
    manifest_path: Path,
    *,
    limit: int = DEFAULT_MANIFEST_LIMIT,
    repo_root: Path | None = None,
) -> list[str]:
    records, malformed = load_manifest(manifest_path)
    if not records and not manifest_path.exists():
        return [f"- Run manifest not found: `{manifest_path}`"]
    markdown = render_manifest(records, malformed_lines=malformed, limit=limit)
    lines = markdown.splitlines()
    output = lines[2:] if lines[:1] == ["# Run Manifest Summary"] else lines
    root = repo_root or manifest_path.parent.parent.parent
    metadata_signal = _metadata_audit_signal_lines(records)
    strict_signal = _strict_retrieval_workflow_signal(root.resolve())
    if metadata_signal:
        output.extend(["", *metadata_signal])
    if strict_signal:
        output.extend(["", *strict_signal])
    return output


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
    lines.extend(_section(
        "Recent Run Manifest",
        collect_manifest_summary(manifest_path, repo_root=repo_root),
    ))
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
