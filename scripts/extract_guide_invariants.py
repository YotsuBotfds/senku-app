#!/usr/bin/env python3
"""Extract numeric/unit-bearing guide lines as a first-pass invariant audit surface."""

from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
GUIDES_DIR = ROOT / "guides"
DEFAULT_OUTPUT = ROOT / "artifacts" / "guide_invariants"

HEADING_RE = re.compile(r"^(#+)\s+(.+)$")
NUMBER_UNIT_RE = re.compile(
    r"""
    (?:
        \b\d+(?:\.\d+)?\s?(?:%|percent|percentages?)\b|
        \b\d+(?:\.\d+)?\s?(?:mg|g|kg|mcg|ug|mL|ml|L|l|cm|mm|m|km|ft|feet|in|inch|inches|psi|bar)\b|
        \b\d+(?:\.\d+)?\s?(?:°C|°F)\b|
        \b\d+(?:\.\d+)?\s?(?:minutes?|mins?|hours?|hrs?|days?|weeks?|months?|years?)\b|
        \b(?:more than|less than|under|over|at least|within)\s+\d+(?:\.\d+)?\b|
        \b\d+\s?-\s?\d+(?:\.\d+)?\s?(?:mg|g|kg|mL|ml|L|cm|mm|°C|°F|minutes?|hours?|days?)\b
    )
    """,
    re.IGNORECASE | re.VERBOSE,
)


def markdown_code_span(value: object) -> str:
    text = str(value).replace("\r", " ").replace("\n", " ")
    longest_tick_run = max((len(match.group(0)) for match in re.finditer(r"`+", text)), default=0)
    fence = "`" * (longest_tick_run + 1)
    padding = " " if "`" in text else ""
    return f"{fence}{padding}{text}{padding}{fence}"


def _report_field(record: dict, key: str, fallback: str) -> str:
    value = record.get(key, "")
    if value is None or isinstance(value, (list, tuple, set, dict)):
        return fallback
    text = str(value).strip()
    return text or fallback


def parse_frontmatter(text: str) -> tuple[dict, str]:
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        return {}, text
    closing_index = None
    for index in range(1, len(lines)):
        if lines[index].strip() == "---":
            closing_index = index
            break
    if closing_index is None:
        return {}, text
    try:
        meta = yaml.safe_load("\n".join(lines[1:closing_index])) or {}
    except yaml.YAMLError:
        meta = {}
    if not isinstance(meta, dict):
        meta = {}
    body = "\n".join(lines[closing_index + 1 :])
    return meta, body


def _frontmatter_scalar(meta: dict, key: str) -> str:
    value = meta.get(key, "")
    if isinstance(value, str):
        return value.strip()
    if value is None or isinstance(value, (list, tuple, set, dict)):
        return ""
    return str(value).strip()


def iter_invariants(guides_dir: Path):
    for path in sorted(guides_dir.glob("*.md")):
        text = path.read_text(encoding="utf-8")
        meta, body = parse_frontmatter(text)
        slug = _frontmatter_scalar(meta, "slug")
        guide_id = _frontmatter_scalar(meta, "id")
        title = _frontmatter_scalar(meta, "title")
        if not slug:
            continue
        try:
            display_path = str(path.relative_to(ROOT)).replace("\\", "/")
        except ValueError:
            display_path = path.name

        current_heading = "(intro)"
        for line_number, raw_line in enumerate(body.splitlines(), start=1):
            line = raw_line.strip()
            if not line:
                continue
            heading_match = HEADING_RE.match(line)
            if heading_match:
                current_heading = heading_match.group(2).strip()
                continue
            if not NUMBER_UNIT_RE.search(line):
                continue
            yield {
                "guide_id": guide_id,
                "slug": slug,
                "title": title,
                "line_number": line_number,
                "section_heading": current_heading,
                "text": line,
                "matches": NUMBER_UNIT_RE.findall(line),
                "path": display_path,
            }


def write_outputs(records: list[dict], output_dir: Path) -> tuple[Path, Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    jsonl_path = output_dir / "guide_invariants.jsonl"
    summary_path = output_dir / "guide_invariants_summary.md"

    with jsonl_path.open("w", encoding="utf-8") as handle:
        for record in records:
            handle.write(json.dumps(record, ensure_ascii=False) + "\n")

    by_guide = Counter(_report_field(record, "slug", "(missing slug)") for record in records)
    by_section = Counter(_report_field(record, "section_heading", "(missing section)") for record in records)

    lines = [
        "# Guide Invariants Summary",
        "",
        f"- Extracted lines: {len(records)}",
        f"- Guides with matches: {len(by_guide)}",
        "",
        "## Top Guides By Numeric/Unit-Bearing Lines",
        "",
    ]
    for slug, count in by_guide.most_common(25):
        lines.append(f"- {markdown_code_span(slug)}: {count}")

    lines.extend(["", "## Common Sections", ""])
    for heading, count in by_section.most_common(25):
        lines.append(f"- {markdown_code_span(heading)}: {count}")

    summary_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return jsonl_path, summary_path


def main() -> int:
    parser = argparse.ArgumentParser(description="Extract numeric/unit-bearing guide lines for invariant auditing.")
    parser.add_argument("--guides-dir", default=str(GUIDES_DIR))
    parser.add_argument("--output-dir", default=str(DEFAULT_OUTPUT))
    args = parser.parse_args()

    records = list(iter_invariants(Path(args.guides_dir)))
    jsonl_path, summary_path = write_outputs(records, Path(args.output_dir))

    print(f"Wrote invariant JSONL: {jsonl_path}")
    print(f"Wrote invariant summary: {summary_path}")
    print(f"Extracted {len(records)} numeric/unit-bearing lines")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
