#!/usr/bin/env python3
"""Select guide source files needed for prompt-pack retrieval smoke indexes."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any, Iterable, Mapping, Sequence

import yaml


GUIDE_ID_RE = re.compile(r"\bGD-\d{3}\b", re.IGNORECASE)
GUIDE_KEYS = {
    "guide_id",
    "guide_ids",
    "expected_guide_id",
    "expected_guide_ids",
    "expected_guides",
    "expected_source_guide_ids",
    "primary_expected_guide_id",
    "primary_expected_guide_ids",
    "primary_expected_guides",
    "backup_expected_guide_ids",
    "target_guide_id",
    "target_guide_ids",
    "target_guides",
}


def clean_text(value: Any) -> str:
    return "" if value is None else str(value).strip()


def clean_slug(value: Any) -> str:
    return clean_text(value).strip("'\"").lower()


def extract_guide_ids(value: Any) -> list[str]:
    ids: list[str] = []

    def visit(item: Any) -> None:
        if item is None:
            return
        if isinstance(item, Mapping):
            for key, nested in item.items():
                if str(key).lower() in GUIDE_KEYS:
                    visit(nested)
            return
        if isinstance(item, Sequence) and not isinstance(item, (str, bytes, bytearray)):
            for nested in item:
                visit(nested)
            return
        ids.extend(match.upper() for match in GUIDE_ID_RE.findall(clean_text(item)))

    visit(value)
    return dedupe(ids)


def dedupe(values: Iterable[str]) -> list[str]:
    seen: set[str] = set()
    result: list[str] = []
    for value in values:
        text = clean_text(value).upper()
        if text and text not in seen:
            seen.add(text)
            result.append(text)
    return result


def parse_frontmatter(path: Path) -> dict[str, Any]:
    lines = path.read_text(encoding="utf-8").lstrip("\ufeff").splitlines()
    if not lines or lines[0].strip() != "---":
        return {}
    frontmatter: list[str] = []
    for line in lines[1:]:
        if line.strip() == "---":
            break
        frontmatter.append(line)
    meta = yaml.safe_load("\n".join(frontmatter)) or {}
    return meta if isinstance(meta, dict) else {}


def extract_related_refs(value: Any) -> list[str]:
    refs: list[str] = []

    def visit(item: Any) -> None:
        if item is None:
            return
        if isinstance(item, Mapping):
            for nested in item.values():
                visit(nested)
            return
        if isinstance(item, Sequence) and not isinstance(item, (str, bytes, bytearray)):
            for nested in item:
                visit(nested)
            return
        text = clean_text(item)
        if text:
            refs.append(text)

    visit(value)
    return dedupe(refs)


def load_guide_catalog(guides_dir: Path) -> dict[str, dict[str, Any]]:
    records: list[dict[str, Any]] = []
    slug_to_id: dict[str, str] = {}
    for path in sorted(guides_dir.glob("*.md"), key=lambda item: item.name.lower()):
        meta = parse_frontmatter(path)
        guide_id = clean_text(meta.get("id")).upper()
        if not guide_id:
            continue
        slug = clean_slug(meta.get("slug") or path.stem)
        if slug:
            slug_to_id[slug] = guide_id
        records.append(
            {
                "guide_id": guide_id,
                "slug": slug,
                "path": path,
                "source_file": path.name,
                "related_refs": extract_related_refs(meta.get("related")),
            }
        )

    catalog: dict[str, dict[str, Any]] = {}
    for record in records:
        guide_id = record["guide_id"]
        related: list[str] = []
        for related_ref in record["related_refs"]:
            ids = extract_guide_ids(related_ref)
            if ids:
                related.extend(ids)
                continue
            related_id = slug_to_id.get(clean_slug(related_ref))
            if related_id:
                related.append(related_id)
        catalog[guide_id] = {
            "path": record["path"],
            "source_file": record["source_file"],
            "slug": record["slug"],
            "related": dedupe(related),
        }
    return catalog


def load_prompt_pack_guide_ids(path: Path) -> list[str]:
    ids: list[str] = []
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line:
            continue
        record = json.loads(line)
        if isinstance(record, Mapping):
            ids.extend(extract_guide_ids(record))
    return dedupe(ids)


def expand_related(
    guide_ids: Iterable[str],
    catalog: Mapping[str, Mapping[str, Any]],
    *,
    depth: int,
) -> list[str]:
    selected = set(dedupe(guide_ids))
    frontier = set(selected)
    for _ in range(max(0, depth)):
        next_frontier: set[str] = set()
        for guide_id in frontier:
            for related_id in catalog.get(guide_id, {}).get("related", []):
                if related_id not in selected:
                    selected.add(related_id)
                    next_frontier.add(related_id)
        frontier = next_frontier
        if not frontier:
            break
    return sorted(selected)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("prompt_packs", nargs="+", type=Path)
    parser.add_argument("--guides-dir", type=Path, default=Path("guides"))
    parser.add_argument("--include-related-depth", type=int, default=1)
    parser.add_argument("--output", type=Path, default=None)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    catalog = load_guide_catalog(args.guides_dir)
    ids: list[str] = []
    for pack in args.prompt_packs:
        ids.extend(load_prompt_pack_guide_ids(pack))
    selected = expand_related(ids, catalog, depth=args.include_related_depth)
    missing = [guide_id for guide_id in selected if guide_id not in catalog]
    if missing:
        raise SystemExit(f"Prompt packs reference unknown guide IDs: {', '.join(missing)}")
    lines = [catalog[guide_id]["source_file"] for guide_id in selected]
    text = "\n".join(lines) + ("\n" if lines else "")
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(text, encoding="utf-8")
    else:
        print(text, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
