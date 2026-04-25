"""Export section-family shadow records for RAPTOR-lite retrieval experiments.

The output is intentionally compatible with the S15 contextual shadow comparator:
each record has `chunk_id`, `document`, `contextual_retrieval_text`, and
`metadata`. Records are extractive section summaries, not production content.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import sys
from collections import OrderedDict
from pathlib import Path
from typing import Any

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from ingest import (
    collect_markdown_files,
    make_chunk_id,
    normalize_selected_files,
    process_file,
)


SECTION_FRAGMENT_MAX_CHARS = 220
DEFAULT_MAX_FRAGMENTS = 8
DEFAULT_FAMILY_WINDOW = 2


def _positive_int(value: str) -> int:
    try:
        parsed = int(value)
    except ValueError as exc:
        raise argparse.ArgumentTypeError(f"Invalid integer value: {value}") from exc
    if parsed < 1:
        raise argparse.ArgumentTypeError("Values must be positive integers")
    return parsed


def _clean_fragment(text: str) -> str:
    text = re.sub(r"<[^>]+>", " ", text)
    text = re.sub(r"!\[[^\]]*\]\([^)]+\)", " ", text)
    text = re.sub(r"\[[^\]]+\]\([^)]+\)", " ", text)
    text = text.replace("**", "")
    text = re.sub(r"^[#>*\-\s]+", "", text.strip())
    text = re.sub(r"^\d+[.)]\s+", "", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def extract_section_fragments(
    texts: list[str],
    *,
    max_fragments: int = DEFAULT_MAX_FRAGMENTS,
    max_chars: int = SECTION_FRAGMENT_MAX_CHARS,
) -> list[str]:
    """Return compact extractive evidence fragments in source order."""
    fragments: list[str] = []
    seen = set()
    for text in texts:
        for raw_line in text.splitlines():
            fragment = _clean_fragment(raw_line)
            if not fragment:
                continue
            if len(fragment) < 24 and not fragment.endswith(":"):
                continue
            if set(fragment) <= {"-", "|", " "}:
                continue
            if len(fragment) > max_chars:
                fragment = fragment[: max_chars - 1].rstrip() + "..."
            normalized = fragment.lower()
            if normalized in seen:
                continue
            seen.add(normalized)
            fragments.append(fragment)
            if len(fragments) >= max_fragments:
                return fragments
    return fragments


def load_chunks_from_files(md_files: list[str]) -> list[dict[str, Any]]:
    """Parse guide files into chunks and preserve production-style chunk ids."""
    chunks: list[dict[str, Any]] = []
    errors: list[tuple[str, str]] = []
    for filepath in md_files:
        try:
            meta, file_chunks = process_file(filepath)
            basename = os.path.basename(filepath)
            if meta is None:
                errors.append((basename, "Failed to parse frontmatter"))
                continue
            if not file_chunks:
                errors.append((basename, "Produced zero chunks"))
                continue
            chunks.extend(file_chunks)
        except Exception as exc:
            errors.append((os.path.basename(filepath), str(exc)))

    if errors:
        joined = "; ".join(f"{filename}: {reason}" for filename, reason in errors)
        raise RuntimeError(f"Section-family shadow export failed: {joined}")

    for index, chunk in enumerate(chunks):
        chunk["chunk_id"] = make_chunk_id(chunk, index)
    return chunks


def section_blocks_from_chunks(chunks: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Merge adjacent chunks from the same guide section into section blocks."""
    blocks: OrderedDict[tuple[str, str, str, str], dict[str, Any]] = OrderedDict()
    for chunk in chunks:
        metadata = chunk.get("metadata") or {}
        key = (
            str(metadata.get("source_file", "")),
            str(metadata.get("guide_id", "")),
            str(metadata.get("section_id", "")),
            str(metadata.get("section_heading", "")),
        )
        block = blocks.setdefault(
            key,
            {
                "metadata": dict(metadata),
                "texts": [],
                "source_chunk_ids": [],
            },
        )
        block["texts"].append(str(chunk.get("text", "")))
        block["source_chunk_ids"].append(str(chunk.get("chunk_id", "")))
    return list(blocks.values())


def group_chunks_by_section_family(
    chunks: list[dict[str, Any]],
    *,
    family_window: int = DEFAULT_FAMILY_WINDOW,
    family_stride: int | None = None,
) -> list[dict[str, Any]]:
    """Group adjacent section blocks into deterministic RAPTOR-lite families."""
    if family_window < 1:
        raise ValueError("family_window must be >= 1")
    if family_stride is None:
        family_stride = family_window
    if family_stride < 1:
        raise ValueError("family_stride must be >= 1")

    guide_blocks: OrderedDict[tuple[str, str], list[dict[str, Any]]] = OrderedDict()
    for block in section_blocks_from_chunks(chunks):
        metadata = block.get("metadata") or {}
        guide_key = (
            str(metadata.get("source_file", "")),
            str(metadata.get("guide_id", "")),
        )
        guide_blocks.setdefault(guide_key, []).append(block)

    families: list[dict[str, Any]] = []
    for _guide_key, blocks in guide_blocks.items():
        for family_index, start in enumerate(range(0, len(blocks), family_stride)):
            family_blocks = blocks[start : start + family_window]
            first_metadata = dict(family_blocks[0].get("metadata") or {})
            guide_id = str(first_metadata.get("guide_id", ""))
            source_file = str(first_metadata.get("source_file", ""))
            family_prefix = guide_id or source_file or "guide"
            section_family = f"{family_prefix}:{family_index:02d}"
            section_ids = [
                str((block.get("metadata") or {}).get("section_id", ""))
                for block in family_blocks
            ]
            section_headings = [
                str((block.get("metadata") or {}).get("section_heading", ""))
                for block in family_blocks
            ]
            first_metadata.update(
                {
                    "section_family": section_family,
                    "section_ids": ",".join(section_id for section_id in section_ids if section_id),
                    "section_headings": " | ".join(
                        heading for heading in section_headings if heading
                    ),
                    "family_index": str(family_index),
                    "family_window": str(family_window),
                    "family_stride": str(family_stride),
                }
            )
            texts: list[str] = []
            source_chunk_ids: list[str] = []
            for block in family_blocks:
                texts.extend(block.get("texts") or [])
                source_chunk_ids.extend(block.get("source_chunk_ids") or [])
            families.append(
                {
                    "metadata": first_metadata,
                    "texts": texts,
                    "source_chunk_ids": source_chunk_ids,
                    "section_ids": section_ids,
                    "section_headings": section_headings,
                    "section_family": section_family,
                }
            )
    return families


def section_family_record_id(group: dict[str, Any]) -> str:
    metadata = group.get("metadata") or {}
    raw = "|".join(
        [
            str(metadata.get("source_file", "")),
            str(metadata.get("guide_id", "")),
            str(metadata.get("section_family", "")),
            str(metadata.get("section_ids", "")),
            ",".join(group.get("source_chunk_ids") or []),
        ]
    )
    return "section_family_" + hashlib.sha1(raw.encode("utf-8")).hexdigest()


def build_section_family_text(
    group: dict[str, Any],
    *,
    max_fragments: int = DEFAULT_MAX_FRAGMENTS,
) -> str:
    metadata = group.get("metadata") or {}
    guide_bits = [
        metadata.get("guide_title", ""),
        metadata.get("guide_id", ""),
        metadata.get("slug", ""),
    ]
    guide_label = " | ".join(str(bit).strip() for bit in guide_bits if str(bit).strip())
    lines: list[str] = []
    if guide_label:
        lines.append(f"Guide: {guide_label}")
    if metadata.get("description"):
        lines.append(f"Purpose: {metadata.get('description')}")
    section_label = metadata.get("section_headings") or metadata.get("section_heading")
    if section_label:
        lines.append(f"Section family: {metadata.get('section_family', '')}")
        lines.append(f"Sections: {section_label}")
    if metadata.get("category") or metadata.get("tags"):
        category_tags = ", ".join(
            str(bit).strip()
            for bit in (metadata.get("category", ""), metadata.get("tags", ""))
            if str(bit).strip()
        )
        lines.append(f"Category/tags: {category_tags}")
    if metadata.get("related"):
        lines.append(f"Related: {metadata.get('related')}")
    lines.append(f"Source chunks: {len(group.get('source_chunk_ids') or [])}")

    fragments = extract_section_fragments(
        group.get("texts") or [],
        max_fragments=max_fragments,
    )
    if fragments:
        lines.append("")
        lines.append("Evidence fragments:")
        lines.extend(f"- {fragment}" for fragment in fragments)
    return "\n".join(lines).strip()


def build_section_family_record(
    group: dict[str, Any],
    *,
    max_fragments: int = DEFAULT_MAX_FRAGMENTS,
) -> dict[str, Any]:
    metadata = dict(group.get("metadata") or {})
    source_chunk_ids = [chunk_id for chunk_id in group.get("source_chunk_ids") or [] if chunk_id]
    text = build_section_family_text(group, max_fragments=max_fragments)
    section_family = str(metadata.get("section_family", ""))
    metadata.update(
        {
            "record_type": "section_family_summary",
            "source_chunk_count": str(len(source_chunk_ids)),
            "source_chunk_ids": ",".join(source_chunk_ids),
        }
    )
    return {
        "chunk_id": section_family_record_id(group),
        "section_family": section_family,
        "section_ids": group.get("section_ids") or [],
        "section_headings": group.get("section_headings") or [],
        "document": text,
        "contextual_retrieval_text": text,
        "raptor_lite_text": text,
        "metadata": metadata,
    }


def build_section_family_records(
    chunks: list[dict[str, Any]],
    *,
    family_window: int = DEFAULT_FAMILY_WINDOW,
    family_stride: int | None = None,
    max_fragments: int = DEFAULT_MAX_FRAGMENTS,
) -> list[dict[str, Any]]:
    return [
        build_section_family_record(group, max_fragments=max_fragments)
        for group in group_chunks_by_section_family(
            chunks,
            family_window=family_window,
            family_stride=family_stride,
        )
    ]


def write_section_family_jsonl(
    records: list[dict[str, Any]],
    output_path: os.PathLike[str] | str,
) -> int:
    output = Path(output_path)
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", encoding="utf-8") as handle:
        for record in records:
            json.dump(record, handle, ensure_ascii=False, sort_keys=True)
            handle.write("\n")
    return len(records)


def export_section_family_shadow_jsonl(
    md_files: list[str],
    output_path: os.PathLike[str] | str,
    *,
    family_window: int = DEFAULT_FAMILY_WINDOW,
    family_stride: int | None = None,
    max_fragments: int = DEFAULT_MAX_FRAGMENTS,
) -> int:
    chunks = load_chunks_from_files(md_files)
    records = build_section_family_records(
        chunks,
        family_window=family_window,
        family_stride=family_stride,
        max_fragments=max_fragments,
    )
    return write_section_family_jsonl(records, output_path)


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Export section-family shadow records for retrieval experiments."
    )
    parser.add_argument("--out", required=True, help="Output JSONL path.")
    parser.add_argument(
        "--files",
        nargs="*",
        help="Optional guide filenames/paths. Defaults to all guides.",
    )
    parser.add_argument(
        "--family-window",
        type=_positive_int,
        default=DEFAULT_FAMILY_WINDOW,
        help="Adjacent section count per section-family shadow record.",
    )
    parser.add_argument(
        "--family-stride",
        type=_positive_int,
        default=None,
        help="Advance families by this many sections between windows. Defaults to family-window.",
    )
    parser.add_argument(
        "--max-fragments",
        type=_positive_int,
        default=DEFAULT_MAX_FRAGMENTS,
        help="Maximum extractive fragments per section-family record.",
    )
    args = parser.parse_args(argv)
    if args.family_stride is None:
        args.family_stride = args.family_window
    return args


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    all_md_files = collect_markdown_files()
    md_files = normalize_selected_files(args.files) if args.files else all_md_files
    if args.files:
        requested_basenames = {os.path.basename(req) for req in args.files}
        resolved_basenames = {os.path.basename(path) for path in md_files}
        missing = sorted(requested_basenames - resolved_basenames)
        if missing:
            raise SystemExit(
                "Could not resolve guide file(s): " + ", ".join(missing)
            )
    count = export_section_family_shadow_jsonl(
        md_files,
        args.out,
        family_window=args.family_window,
        family_stride=args.family_stride,
        max_fragments=args.max_fragments,
    )
    print(f"Wrote {count} section-family shadow records to {args.out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
