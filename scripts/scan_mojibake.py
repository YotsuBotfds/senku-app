#!/usr/bin/env python3
"""Scan text files for likely mojibake and encoding damage."""

from __future__ import annotations

import argparse
import json
import re
import subprocess
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Callable, Sequence


DEFAULT_PATHS = ("guides", "notes")
DEFAULT_TOUCHED_PATHS = (
    "guides",
    "notes",
    "scripts",
    "tests",
    "android-app/app/src/main",
    "android-app/app/src/test",
    "android-app/app/src/androidTest",
)
TEXT_SUFFIXES = {
    ".md",
    ".markdown",
    ".txt",
    ".py",
    ".ps1",
    ".java",
    ".kt",
    ".kts",
    ".xml",
    ".toml",
    ".ini",
    ".cfg",
    ".properties",
    ".gradle",
    ".yaml",
    ".yml",
    ".json",
    ".jsonl",
    ".csv",
    ".html",
}

GitRunner = Callable[[Sequence[str], Path], str]

MOJIBAKE_PATTERNS = [
    (
        "replacement_character",
        re.compile(r"\ufffd"),
        "Unicode replacement character, usually from a decode failure.",
    ),
    (
        "utf8_as_cp1252_accent_prefix",
        re.compile(r"[\u00c2\u00c3][\u0080-\u00bf\u00a0-\u00ff]?"),
        "Common UTF-8 bytes decoded as Windows-1252 or Latin-1.",
    ),
    (
        "utf8_as_cp1252_punctuation",
        re.compile(r"\u00e2[\u0080-\u00bf\u201a-\u201e\u2020-\u2022\u20ac\u2122]+"),
        "Common smart quote, dash, arrow, bullet, or trademark mojibake.",
    ),
    (
        "emoji_mojibake",
        re.compile(r"\u00f0[\u0080-\u00ff\u0178\u0152\u2018-\u201d]+"),
        "Likely emoji bytes decoded as text, often seen in icon frontmatter.",
    ),
    (
        "stray_latin1_spacing",
        re.compile(r"\u00c2[\u00a0-\u00bf]"),
        "Latin-1 spacing or symbol byte artifact.",
    ),
]


def iter_text_files(paths: list[Path]) -> list[Path]:
    files: list[Path] = []
    seen: set[Path] = set()
    for path in paths:
        if not path.exists():
            continue
        candidates = [path] if path.is_file() else path.rglob("*")
        for candidate in candidates:
            if not candidate.is_file():
                continue
            if candidate.suffix.lower() not in TEXT_SUFFIXES:
                continue
            resolved = candidate.resolve()
            if resolved in seen:
                continue
            seen.add(resolved)
            files.append(candidate)
    return sorted(files, key=lambda item: str(item).lower())


def _run_git(args: Sequence[str], cwd: Path) -> str:
    completed = subprocess.run(
        ["git", *args],
        cwd=cwd,
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if completed.returncode != 0:
        stderr = completed.stderr.decode("utf-8", errors="replace").strip()
        command = "git " + " ".join(args)
        raise RuntimeError(f"{command} failed: {stderr}")
    return completed.stdout.decode("utf-8", errors="surrogateescape")


def git_repo_root(cwd: Path, *, git_runner: GitRunner = _run_git) -> Path:
    output = git_runner(["rev-parse", "--show-toplevel"], cwd)
    root_text = output.strip().rstrip("\0")
    if not root_text:
        raise RuntimeError("git rev-parse --show-toplevel returned no path")
    return Path(root_text).resolve()


def _split_nul_paths(output: str) -> list[str]:
    return [item for item in output.split("\0") if item]


def changed_git_paths(root: Path, *, git_runner: GitRunner = _run_git) -> list[str]:
    outputs = [
        git_runner(["diff", "--name-only", "-z", "--diff-filter=ACMRTUXB", "--cached", "--"], root),
        git_runner(["diff", "--name-only", "-z", "--diff-filter=ACMRTUXB", "--"], root),
        git_runner(["ls-files", "--others", "--exclude-standard", "-z"], root),
    ]

    paths: list[str] = []
    seen: set[str] = set()
    for output in outputs:
        for rel_path in _split_nul_paths(output):
            normalized = rel_path.replace("\\", "/").strip("/")
            key = normalized.casefold()
            if not normalized or key in seen:
                continue
            seen.add(key)
            paths.append(normalized)
    return paths


def _normalize_allowed_path(value: str, *, root: Path) -> str:
    text = value.strip()
    if text in {"", "."}:
        return "."

    path = Path(text)
    if path.is_absolute():
        try:
            text = path.resolve().relative_to(root).as_posix()
        except ValueError:
            text = path.as_posix()
    else:
        text = text.replace("\\", "/")

    normalized = text.strip("/")
    return normalized.casefold() if normalized else "."


def _path_is_allowed(
    rel_path: str,
    allowed_paths: Sequence[str],
    *,
    root: Path,
    empty_matches_all: bool = True,
) -> bool:
    normalized = rel_path.replace("\\", "/").strip("/").casefold()
    allowed = [_normalize_allowed_path(item, root=root) for item in allowed_paths]
    if not allowed:
        return empty_matches_all
    if "." in allowed:
        return True
    return any(normalized == item or normalized.startswith(item + "/") for item in allowed)


def touched_text_files(
    *,
    root: Path,
    allowed_paths: Sequence[str] = DEFAULT_TOUCHED_PATHS,
    git_runner: GitRunner = _run_git,
) -> list[Path]:
    files: list[Path] = []
    seen: set[Path] = set()
    for rel_path in changed_git_paths(root, git_runner=git_runner):
        if not _path_is_allowed(rel_path, allowed_paths, root=root):
            continue
        path = (root / rel_path).resolve()
        try:
            path.relative_to(root)
        except ValueError:
            continue
        if path.suffix.lower() not in TEXT_SUFFIXES:
            continue
        if not path.is_file():
            continue
        if path in seen:
            continue
        seen.add(path)
        files.append(path)
    return sorted(files, key=lambda item: _display_path(item, root).lower())


def _line_snippet(line: str, *, max_chars: int) -> str:
    text = line.strip()
    if len(text) <= max_chars:
        return text
    return text[: max_chars - 4].rstrip() + " ..."


def scan_file(path: Path, *, root: Path, max_snippet_chars: int = 160) -> list[dict[str, Any]]:
    display_path = _display_path(path, root)
    try:
        text = path.read_text(encoding="utf-8")
    except UnicodeDecodeError as exc:
        return [
            {
                "path": display_path,
                "line": None,
                "column": None,
                "kind": "utf8_decode_error",
                "match": str(exc),
                "snippet": "",
                "note": "File could not be decoded as UTF-8.",
            }
        ]

    findings: list[dict[str, Any]] = []
    for line_no, line in enumerate(text.splitlines(), start=1):
        for kind, pattern, note in MOJIBAKE_PATTERNS:
            match = pattern.search(line)
            if not match:
                continue
            findings.append(
                {
                    "path": display_path,
                    "line": line_no,
                    "column": match.start() + 1,
                    "kind": kind,
                    "match": match.group(0),
                    "snippet": _line_snippet(line, max_chars=max_snippet_chars),
                    "note": note,
                }
            )
    return findings


def _display_path(path: Path, root: Path) -> str:
    display = path.relative_to(root) if path.is_relative_to(root) else path
    return display.as_posix()


def scan_paths(
    paths: list[Path],
    *,
    root: Path,
    max_snippet_chars: int = 160,
    mode: str = "paths",
    touched_allowlist: Sequence[str] | None = None,
    allowed_finding_paths: Sequence[str] | None = None,
) -> dict[str, Any]:
    files = iter_text_files(paths)
    findings: list[dict[str, Any]] = []
    for path in files:
        findings.extend(scan_file(path, root=root, max_snippet_chars=max_snippet_chars))

    by_kind = Counter(finding["kind"] for finding in findings)
    by_path = Counter(finding["path"] for finding in findings)
    gate_findings = [
        finding
        for finding in findings
        if not _path_is_allowed(
            str(finding.get("path", "")),
            allowed_finding_paths or [],
            root=root,
            empty_matches_all=False,
        )
    ]
    return {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "mode": mode,
        "paths": [
            _display_path(path.resolve(), root) if path.exists() else path.as_posix()
            for path in paths
        ],
        "touched_path_allowlist": list(touched_allowlist or []),
        "allowed_finding_paths": list(allowed_finding_paths or []),
        "files_scanned": len(files),
        "findings_count": len(findings),
        "allowed_findings_count": len(findings) - len(gate_findings),
        "gate_findings_count": len(gate_findings),
        "counts_by_kind": dict(sorted(by_kind.items())),
        "top_files": [
            {"path": path, "findings": count}
            for path, count in by_path.most_common(25)
        ],
        "findings": findings,
    }


def render_markdown(report: dict[str, Any], *, limit: int = 200) -> str:
    lines = [
        "# Mojibake Scan",
        "",
        f"- Generated at: `{report['generated_at']}`",
        f"- Files scanned: `{report['files_scanned']}`",
        f"- Findings: `{report['findings_count']}`",
        f"- Gate findings: `{report.get('gate_findings_count', report['findings_count'])}`",
        "",
        "## Counts By Kind",
        "",
        "| Kind | Count |",
        "| --- | ---: |",
    ]
    counts = report.get("counts_by_kind") or {}
    if counts:
        for kind, count in counts.items():
            lines.append(f"| `{kind}` | {count} |")
    else:
        lines.append("| `none` | 0 |")

    lines.extend(["", "## Top Files", "", "| File | Findings |", "| --- | ---: |"])
    top_files = report.get("top_files") or []
    if top_files:
        for row in top_files:
            lines.append(f"| `{_escape_md(row['path'])}` | {row['findings']} |")
    else:
        lines.append("| `none` | 0 |")

    findings = report.get("findings") or []
    lines.extend(
        [
            "",
            f"## First {min(limit, len(findings))} Findings",
            "",
            "| File | Line | Kind | Snippet |",
            "| --- | ---: | --- | --- |",
        ]
    )
    for finding in findings[:limit]:
        line = finding.get("line") or "-"
        lines.append(
            "| `{path}` | {line} | `{kind}` | {snippet} |".format(
                path=_escape_md(finding.get("path", "")),
                line=line,
                kind=_escape_md(finding.get("kind", "")),
                snippet=_escape_md(finding.get("snippet", "")),
            )
        )
    if len(findings) > limit:
        lines.append(f"| ... | ... | ... | {len(findings) - limit} more findings omitted |")
    return "\n".join(lines) + "\n"


def _escape_md(value: object) -> str:
    return str(value).replace("\n", " ").replace("|", "\\|")


def output_paths(output_dir: Path, stamp: str) -> tuple[Path, Path]:
    return (
        output_dir / f"mojibake_scan_{stamp}.json",
        output_dir / f"mojibake_scan_{stamp}.md",
    )


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--paths",
        nargs="+",
        default=list(DEFAULT_PATHS),
        help="Files or directories to scan.",
    )
    parser.add_argument(
        "--touched",
        action="store_true",
        help="Scan only changed tracked and untracked text files reported by git.",
    )
    parser.add_argument(
        "--touched-paths",
        nargs="+",
        default=list(DEFAULT_TOUCHED_PATHS),
        help=(
            "Path prefixes allowed in --touched mode. Use '.' to include all changed "
            "text files."
        ),
    )
    parser.add_argument(
        "--allow-finding-paths",
        nargs="+",
        default=[],
        help=(
            "Path prefixes whose findings are reported but ignored by "
            "--fail-on-findings."
        ),
    )
    parser.add_argument(
        "--output-dir",
        default="artifacts/text_quality",
        help="Directory for JSON and Markdown reports.",
    )
    parser.add_argument("--json-output", help="Explicit JSON report path.")
    parser.add_argument("--md-output", help="Explicit Markdown report path.")
    parser.add_argument("--max-snippet-chars", type=int, default=160)
    parser.add_argument("--markdown-limit", type=int, default=200)
    failure_group = parser.add_mutually_exclusive_group()
    failure_group.add_argument(
        "--fail-on-findings",
        action="store_true",
        help="Exit 1 when any likely mojibake is found.",
    )
    failure_group.add_argument(
        "--report-only",
        action="store_true",
        help="Always exit 0; this is the default behavior.",
    )
    args = parser.parse_args(argv)

    cwd = Path.cwd().resolve()
    try:
        root = git_repo_root(cwd) if args.touched else cwd
        scan_targets = (
            touched_text_files(root=root, allowed_paths=args.touched_paths)
            if args.touched
            else [Path(path) for path in args.paths]
        )
    except RuntimeError as exc:
        parser.error(str(exc))
    report = scan_paths(
        scan_targets,
        root=root,
        max_snippet_chars=max(40, args.max_snippet_chars),
        mode="touched" if args.touched else "paths",
        touched_allowlist=args.touched_paths if args.touched else None,
        allowed_finding_paths=args.allow_finding_paths,
    )

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S")
    default_json_path, default_md_path = output_paths(output_dir, stamp)
    json_path = Path(args.json_output) if args.json_output else default_json_path
    md_path = Path(args.md_output) if args.md_output else default_md_path
    json_path.parent.mkdir(parents=True, exist_ok=True)
    md_path.parent.mkdir(parents=True, exist_ok=True)

    json_path.write_text(
        json.dumps(report, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )
    md_path.write_text(
        render_markdown(report, limit=max(0, args.markdown_limit)),
        encoding="utf-8",
    )

    print(
        json.dumps(
            {
                "files_scanned": report["files_scanned"],
                "findings_count": report["findings_count"],
                "gate_findings_count": report["gate_findings_count"],
                "json_output": str(json_path),
                "md_output": str(md_path),
            },
            ensure_ascii=True,
        )
    )
    return 1 if args.fail_on_findings and report["gate_findings_count"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
