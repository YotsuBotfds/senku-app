#!/usr/bin/env python3
"""Scan text files for likely mojibake and encoding damage."""

from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


DEFAULT_PATHS = ("guides", "notes")
TEXT_SUFFIXES = {
    ".md",
    ".markdown",
    ".txt",
    ".yaml",
    ".yml",
    ".json",
    ".jsonl",
    ".csv",
    ".html",
}

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


def scan_paths(paths: list[Path], *, root: Path, max_snippet_chars: int = 160) -> dict[str, Any]:
    files = iter_text_files(paths)
    findings: list[dict[str, Any]] = []
    for path in files:
        findings.extend(scan_file(path, root=root, max_snippet_chars=max_snippet_chars))

    by_kind = Counter(finding["kind"] for finding in findings)
    by_path = Counter(finding["path"] for finding in findings)
    return {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "paths": [str(path) for path in paths],
        "files_scanned": len(files),
        "findings_count": len(findings),
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
        "--output-dir",
        default="artifacts/text_quality",
        help="Directory for JSON and Markdown reports.",
    )
    parser.add_argument("--json-output", help="Explicit JSON report path.")
    parser.add_argument("--md-output", help="Explicit Markdown report path.")
    parser.add_argument("--max-snippet-chars", type=int, default=160)
    parser.add_argument("--markdown-limit", type=int, default=200)
    parser.add_argument(
        "--fail-on-findings",
        action="store_true",
        help="Exit 1 when any likely mojibake is found.",
    )
    args = parser.parse_args(argv)

    root = Path.cwd().resolve()
    scan_targets = [Path(path) for path in args.paths]
    report = scan_paths(
        scan_targets,
        root=root,
        max_snippet_chars=max(40, args.max_snippet_chars),
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
                "json_output": str(json_path),
                "md_output": str(md_path),
            },
            ensure_ascii=True,
        )
    )
    return 1 if args.fail_on_findings and report["findings_count"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
