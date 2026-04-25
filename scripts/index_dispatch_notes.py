#!/usr/bin/env python3
"""Build a generated index for dispatch notes."""

from __future__ import annotations

import argparse
import json
import re
from dataclasses import asdict, dataclass
from datetime import datetime
from pathlib import Path
from typing import Iterable, Sequence


DEFAULT_DISPATCH_DIR = Path("notes/dispatch")
DEFAULT_OUTPUT_MD = Path("notes/dispatch/dispatch_index.generated.md")
SKIP_FILENAMES = {"README.md", "dispatch_index.generated.md"}
DEFAULT_CELL_LIMIT = 120
PATH_PATTERN = re.compile(
    r"(?:`([^`\n]*(?:/|\\)[^`\n]*)`)|"
    r"(?:\[[^\]]+\]\(([^)\n]*(?:/|\\)[^)\n]*)\))|"
    r"(?<![\w.-])((?:[A-Za-z0-9_.-]+[/\\]){1,}[A-Za-z0-9_.-]+)"
)
HEADING_PATTERN = re.compile(r"^(#{1,6})\s+(.+?)\s*$")
SLICE_PREFIX_PATTERN = re.compile(
    r"^(?:Slice\s+)?([A-Za-z0-9][A-Za-z0-9_.-]*)(?:\s+[-:\u2014]\s+|\s+)(.+)$",
    re.IGNORECASE,
)
INTERESTING_LINE_PREFIXES = (
    "landed ",
    "added ",
    "updated ",
    "fixed ",
    "passed ",
    "validated ",
    "validation ",
    "artifact ",
    "artifacts ",
    "wrote ",
    "created ",
    "produced ",
)
INTERESTING_HEADINGS = {
    "outcome",
    "validation",
    "proof",
    "acceptance",
    "result",
    "results",
    "report",
}


@dataclass(frozen=True)
class DispatchNote:
    slice_id: str
    title: str
    path: str
    status: str
    headings: list[str]
    linked_paths: list[str]
    proof_lines: list[str]
    mtime: str


def _parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--dispatch-dir",
        type=Path,
        default=DEFAULT_DISPATCH_DIR,
        help="Dispatch note directory to index.",
    )
    parser.add_argument(
        "--output-md",
        type=Path,
        default=DEFAULT_OUTPUT_MD,
        help="Generated Markdown index path.",
    )
    parser.add_argument(
        "--output-json",
        type=Path,
        default=None,
        help="Optional JSON index path.",
    )
    parser.add_argument(
        "--include-completed",
        action="store_true",
        help="Also index notes in the completed subdirectory.",
    )
    return parser.parse_args(argv)


def _normalize_path(path: Path) -> str:
    return path.as_posix()


def _repo_relative(path: Path) -> str:
    try:
        return _normalize_path(path.relative_to(Path.cwd()))
    except ValueError:
        return _normalize_path(path)


def _clean_heading_text(text: str) -> str:
    text = text.strip().strip("#").strip()
    text = text.replace("\u2014", "-")
    return re.sub(r"\s+", " ", text)


def _fallback_title_from_name(path: Path) -> tuple[str, str]:
    stem = path.stem
    if "_" not in stem:
        return stem, stem
    slice_id, title = stem.split("_", 1)
    return slice_id, title.replace("_", " ")


def parse_slice_title(path: Path, text: str) -> tuple[str, str]:
    first_heading = None
    for line in text.splitlines():
        match = HEADING_PATTERN.match(line)
        if match:
            first_heading = _clean_heading_text(match.group(2))
            break
    if not first_heading:
        return _fallback_title_from_name(path)

    match = SLICE_PREFIX_PATTERN.match(first_heading)
    if match:
        return match.group(1).strip(), match.group(2).strip()
    return _fallback_title_from_name(path)[0], first_heading


def extract_headings(text: str) -> list[str]:
    headings = []
    for line in text.splitlines():
        match = HEADING_PATTERN.match(line)
        if match:
            headings.append(_clean_heading_text(match.group(2)))
    return headings


def extract_linked_paths(text: str) -> list[str]:
    paths: list[str] = []
    seen: set[str] = set()
    for match in PATH_PATTERN.finditer(text):
        candidate = next((group for group in match.groups() if group), "")
        candidate = candidate.strip().strip("<>").strip("'\"").rstrip(".,;:")
        if not candidate or candidate.startswith(("http://", "https://", "app://")):
            continue
        normalized = candidate.replace("\\", "/")
        if (
            len(normalized) <= 1
            or normalized.startswith((")", "("))
            or any(char.isspace() for char in normalized)
        ):
            continue
        if normalized not in seen:
            paths.append(normalized)
            seen.add(normalized)
    return paths


def extract_proof_lines(text: str, *, limit: int = 4) -> list[str]:
    proof_lines: list[str] = []
    active_heading = ""
    for raw_line in text.splitlines():
        stripped = raw_line.strip()
        heading_match = HEADING_PATTERN.match(stripped)
        if heading_match:
            active_heading = _clean_heading_text(heading_match.group(2)).lower()
            continue
        if not stripped:
            continue

        line = stripped.lstrip("-*0123456789. ").strip()
        lowered = line.lower()
        heading_key = active_heading.split(" - ", 1)[0]
        if heading_key in INTERESTING_HEADINGS or lowered.startswith(
            INTERESTING_LINE_PREFIXES
        ):
            proof_lines.append(line)
        if len(proof_lines) >= limit:
            break
    return proof_lines


def iter_dispatch_note_paths(
    dispatch_dir: Path, *, include_completed: bool = False
) -> Iterable[Path]:
    if not dispatch_dir.is_dir():
        raise ValueError(f"Dispatch directory does not exist: {dispatch_dir}")

    for path in sorted(dispatch_dir.glob("*.md"), key=lambda item: item.name.lower()):
        if path.name in SKIP_FILENAMES:
            continue
        yield path

    completed_dir = dispatch_dir / "completed"
    if include_completed and completed_dir.is_dir():
        for path in sorted(completed_dir.glob("*.md"), key=lambda item: item.name.lower()):
            if path.name in SKIP_FILENAMES:
                continue
            yield path


def parse_dispatch_note(path: Path, dispatch_dir: Path) -> DispatchNote:
    text = path.read_text(encoding="utf-8")
    slice_id, title = parse_slice_title(path, text)
    completed_dir = dispatch_dir / "completed"
    status = "completed" if completed_dir in path.parents else "active"
    mtime = datetime.fromtimestamp(path.stat().st_mtime).isoformat(timespec="seconds")
    return DispatchNote(
        slice_id=slice_id,
        title=title,
        path=_repo_relative(path),
        status=status,
        headings=extract_headings(text),
        linked_paths=extract_linked_paths(text),
        proof_lines=extract_proof_lines(text),
        mtime=mtime,
    )


def build_index(dispatch_dir: Path, *, include_completed: bool = False) -> list[DispatchNote]:
    return [
        parse_dispatch_note(path, dispatch_dir)
        for path in iter_dispatch_note_paths(
            dispatch_dir, include_completed=include_completed
        )
    ]


def _escape_table_cell(value: str) -> str:
    return value.replace("|", "\\|").replace("\n", " ")


def _format_list(values: Sequence[str], *, limit: int = 3) -> str:
    if not values:
        return ""
    shown = list(values[:limit])
    if len(values) > limit:
        shown.append(f"+{len(values) - limit} more")
    return "<br>".join(_escape_table_cell(_truncate(value)) for value in shown)


def _truncate(value: str, *, limit: int = DEFAULT_CELL_LIMIT) -> str:
    compact = re.sub(r"\s+", " ", value).strip()
    if len(compact) <= limit:
        return compact
    return compact[: limit - 1].rstrip() + "..."


def render_markdown(notes: Sequence[DispatchNote]) -> str:
    counts = {
        "active": sum(1 for note in notes if note.status == "active"),
        "completed": sum(1 for note in notes if note.status == "completed"),
    }
    lines = [
        "# Dispatch Note Index",
        "",
        "<!-- Generated by scripts/index_dispatch_notes.py; do not edit by hand. -->",
        "",
        f"- Active notes: {counts['active']}",
        f"- Completed notes: {counts['completed']}",
        "",
    ]

    for status in ("active", "completed"):
        group = [note for note in notes if note.status == status]
        if not group:
            continue
        lines.extend(
            [
                f"## {status.title()}",
                "",
                "| Slice | Title | Path | Updated | Artifacts / paths | Outcome / proof |",
                "| --- | --- | --- | --- | --- | --- |",
            ]
        )
        for note in group:
            lines.append(
                "| "
                + " | ".join(
                    [
                        _escape_table_cell(note.slice_id),
                        _escape_table_cell(note.title),
                        _escape_table_cell(note.path),
                        _escape_table_cell(note.mtime),
                        _format_list(note.linked_paths),
                        _format_list(note.proof_lines),
                    ]
                )
                + " |"
            )
        lines.append("")

    return "\n".join(lines).rstrip() + "\n"


def write_outputs(
    notes: Sequence[DispatchNote], output_md: Path, output_json: Path | None = None
) -> None:
    output_md.parent.mkdir(parents=True, exist_ok=True)
    output_md.write_text(render_markdown(notes), encoding="utf-8")
    if output_json is not None:
        output_json.parent.mkdir(parents=True, exist_ok=True)
        payload = [asdict(note) for note in notes]
        output_json.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def main(argv: Sequence[str] | None = None) -> int:
    args = _parse_args(argv)
    notes = build_index(args.dispatch_dir, include_completed=args.include_completed)
    write_outputs(notes, args.output_md, args.output_json)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
