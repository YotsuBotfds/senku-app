#!/usr/bin/env python3
"""Merge two structured prompt packs into one combined master pack."""

from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path


def parse_args():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-pack", required=True, help="Base structured prompt pack (.csv or .jsonl)")
    parser.add_argument("--addon-pack", required=True, help="Add-on structured prompt pack (.csv or .jsonl)")
    parser.add_argument(
        "--output-prefix",
        required=True,
        help="Output prefix without extension; writes .txt, .csv, and .jsonl",
    )
    parser.add_argument(
        "--dedupe-prompt-text",
        action="store_true",
        help="Drop exact duplicate prompt text, preferring the first occurrence",
    )
    return parser.parse_args()


def clean_text(value):
    if value is None:
        return ""
    return str(value).strip()


def load_structured_rows(path):
    input_path = Path(path)
    suffix = input_path.suffix.lower()
    if suffix == ".csv":
        with input_path.open("r", encoding="utf-8", newline="") as handle:
            return [dict(row) for row in csv.DictReader(handle)]
    if suffix == ".jsonl":
        rows = []
        for line_number, raw_line in enumerate(input_path.read_text(encoding="utf-8").splitlines(), start=1):
            line = raw_line.strip()
            if not line:
                continue
            payload = json.loads(line)
            if not isinstance(payload, dict):
                raise ValueError(f"Expected object rows in {input_path} at line {line_number}")
            rows.append(payload)
        return rows
    raise ValueError(f"Unsupported structured prompt pack format: {input_path.suffix}")


def normalize_row(row, *, source_pack, fallback_index):
    normalized = dict(row)
    prompt = clean_text(normalized.get("prompt") or normalized.get("question"))
    normalized["prompt"] = prompt
    normalized.setdefault("source_pack", source_pack)
    normalized.setdefault("source_index", fallback_index)
    return normalized


def ensure_unique_prompt_ids(rows):
    seen_ids = {}
    auto_counter = 1
    for row in rows:
        prompt_id = clean_text(row.get("id") or row.get("prompt_id"))
        if not prompt_id:
            prompt_id = f"MX-{auto_counter:03d}"
            auto_counter += 1
        if prompt_id in seen_ids and clean_text(seen_ids[prompt_id].get("prompt")) != clean_text(row.get("prompt")):
            suffix = 2
            candidate = f"{prompt_id}-{suffix}"
            while candidate in seen_ids:
                suffix += 1
                candidate = f"{prompt_id}-{suffix}"
            prompt_id = candidate
        row["id"] = prompt_id
        seen_ids[prompt_id] = row
    return rows


def merge_rows(base_rows, addon_rows, *, dedupe_prompt_text=False):
    merged = []
    seen_prompts = set()
    skipped_duplicates = 0

    for row in list(base_rows) + list(addon_rows):
        prompt = clean_text(row.get("prompt"))
        if not prompt:
            continue
        if dedupe_prompt_text and prompt in seen_prompts:
            skipped_duplicates += 1
            continue
        merged.append(row)
        seen_prompts.add(prompt)

    ensure_unique_prompt_ids(merged)
    return merged, skipped_duplicates


def write_txt(rows, output_path):
    lines = []
    current_section = None
    for row in rows:
        section = clean_text(row.get("section")) or "Unsectioned"
        prompt = clean_text(row.get("prompt"))
        if not prompt:
            continue
        if section != current_section:
            if lines:
                lines.append("")
            lines.append(f"# {section}")
            current_section = section
        lines.append(prompt)
    output_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def write_csv(rows, output_path):
    fieldnames = []
    preferred = [
        "id",
        "lane",
        "section",
        "style",
        "target_behavior",
        "what_it_tests",
        "prompt",
        "source_pack",
        "source_index",
    ]
    for key in preferred:
        if any(key in row for row in rows):
            fieldnames.append(key)
    for row in rows:
        for key in row.keys():
            if key not in fieldnames:
                fieldnames.append(key)

    with output_path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        for row in rows:
            writer.writerow(row)


def write_jsonl(rows, output_path):
    with output_path.open("w", encoding="utf-8") as handle:
        for row in rows:
            handle.write(json.dumps(row, ensure_ascii=False) + "\n")


def main():
    args = parse_args()
    base_path = Path(args.base_pack)
    addon_path = Path(args.addon_pack)
    base_rows = [
        normalize_row(row, source_pack=base_path.name, fallback_index=index)
        for index, row in enumerate(load_structured_rows(base_path), start=1)
    ]
    addon_rows = [
        normalize_row(row, source_pack=addon_path.name, fallback_index=index)
        for index, row in enumerate(load_structured_rows(addon_path), start=1)
    ]
    merged_rows, skipped_duplicates = merge_rows(
        base_rows,
        addon_rows,
        dedupe_prompt_text=args.dedupe_prompt_text,
    )

    output_prefix = Path(args.output_prefix)
    output_prefix.parent.mkdir(parents=True, exist_ok=True)
    txt_path = output_prefix.with_suffix(".txt")
    csv_path = output_prefix.with_suffix(".csv")
    jsonl_path = output_prefix.with_suffix(".jsonl")

    write_txt(merged_rows, txt_path)
    write_csv(merged_rows, csv_path)
    write_jsonl(merged_rows, jsonl_path)

    print(f"Base rows: {len(base_rows)}")
    print(f"Addon rows: {len(addon_rows)}")
    print(f"Skipped duplicate prompt text: {skipped_duplicates}")
    print(f"Merged total: {len(merged_rows)}")
    print(f"Wrote {txt_path}")
    print(f"Wrote {csv_path}")
    print(f"Wrote {jsonl_path}")


if __name__ == "__main__":
    main()
