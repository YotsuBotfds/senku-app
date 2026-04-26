#!/usr/bin/env python3
"""Build a structured prompt pack that preserves canonical Senku phrasings."""

import argparse
import csv
import json
import re
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CANONICAL = REPO_ROOT / "test_prompts.txt"
CONTROL_TEXT_RE = re.compile(r"[ \t]*[\x00-\x1f\x7f]+[ \t]*")


def parse_args(argv=None):
    parser = argparse.ArgumentParser(
        description="Append exact canonical test_prompts entries to a structured prompt pack"
    )
    parser.add_argument(
        "--structured-pack",
        required=True,
        help="Input structured prompt pack (.csv or .jsonl)",
    )
    parser.add_argument(
        "--canonical-prompts",
        default=str(DEFAULT_CANONICAL),
        help="Canonical test_prompts.txt file to preserve verbatim",
    )
    parser.add_argument(
        "--output-prefix",
        required=True,
        help="Output prefix without extension; writes .txt, .csv, and .jsonl",
    )
    return parser.parse_args(argv)


def clean_text(value):
    if value is None:
        return ""
    return CONTROL_TEXT_RE.sub(" ", str(value)).strip()


def clean_row_value(value):
    if isinstance(value, str) or value is None:
        return clean_text(value)
    return value


def load_structured_rows(path):
    input_path = Path(path)
    suffix = input_path.suffix.lower()
    if suffix == ".csv":
        with input_path.open("r", encoding="utf-8-sig", newline="") as f:
            reader = csv.DictReader(f)
            rows = []
            for row in reader:
                if None in row:
                    raise ValueError(
                        f"Unexpected extra columns in {input_path} at line {reader.line_num}"
                    )
                rows.append(dict(row))
            return rows
    if suffix == ".jsonl":
        rows = []
        for line_number, raw_line in enumerate(
            input_path.read_text(encoding="utf-8-sig").splitlines(),
            start=1,
        ):
            line = raw_line.strip()
            if not line:
                continue
            payload = json.loads(line)
            if not isinstance(payload, dict):
                raise ValueError(
                    f"Expected object rows in {input_path} at line {line_number}"
                )
            rows.append(payload)
        return rows
    raise ValueError(f"Unsupported structured prompt pack format: {input_path.suffix}")


def load_canonical_entries(path):
    entries = []
    current_section = "Core Regression"
    for raw_line in Path(path).read_text(encoding="utf-8-sig").splitlines():
        line = clean_text(raw_line)
        if not line:
            continue
        if line.startswith("#"):
            current_section = line.lstrip("#").strip() or current_section
            continue
        entries.append(
            {
                "section": current_section,
                "prompt": line,
            }
        )
    return entries


def carryforward_lane(section):
    if section in {"Core Regression", "Quality Floor Tests"}:
        return "core-carryforward"
    if section in {"System Stress Tests (RAG behavior)", "Targeted Weak Spot Tests"}:
        return "stress-carryforward"
    return "coverage-carryforward"


def append_carryforward_rows(structured_rows, canonical_entries):
    existing_prompts = {clean_text(row.get("prompt") or row.get("question")) for row in structured_rows}
    appended_rows = []
    for index, entry in enumerate(canonical_entries, start=1):
        prompt = clean_text(entry["prompt"])
        if prompt in existing_prompts:
            continue
        appended_rows.append(
            {
                "id": f"CF-{index:03d}",
                "lane": carryforward_lane(entry["section"]),
                "section": entry["section"],
                "style": "carryforward",
                "target_behavior": "canonical-regression",
                "what_it_tests": "Exact carry-forward prompt preserved from test_prompts.txt",
                "prompt": prompt,
                "source_pack": "test_prompts.txt",
                "source_index": index,
            }
        )
        existing_prompts.add(prompt)
    return structured_rows + appended_rows, appended_rows


def write_txt(rows, output_path):
    lines = []
    current_section = None
    for row in rows:
        section = clean_text(row.get("section")) or "Core Regression"
        prompt = clean_text(row.get("prompt") or row.get("question"))
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

    with output_path.open("w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for row in rows:
            writer.writerow({key: clean_row_value(value) for key, value in row.items()})


def write_jsonl(rows, output_path):
    with output_path.open("w", encoding="utf-8") as f:
        for row in rows:
            cleaned_row = {key: clean_row_value(value) for key, value in row.items()}
            f.write(json.dumps(cleaned_row, ensure_ascii=False) + "\n")


def main(argv=None):
    args = parse_args(argv)
    structured_rows = load_structured_rows(args.structured_pack)
    canonical_entries = load_canonical_entries(args.canonical_prompts)
    combined_rows, appended_rows = append_carryforward_rows(
        structured_rows,
        canonical_entries,
    )

    output_prefix = Path(args.output_prefix)
    output_prefix.parent.mkdir(parents=True, exist_ok=True)

    txt_path = output_prefix.with_suffix(".txt")
    csv_path = output_prefix.with_suffix(".csv")
    jsonl_path = output_prefix.with_suffix(".jsonl")

    write_txt(combined_rows, txt_path)
    write_csv(combined_rows, csv_path)
    write_jsonl(combined_rows, jsonl_path)

    print(f"Structured rows: {len(structured_rows)}")
    print(f"Canonical carry-forward rows added: {len(appended_rows)}")
    print(f"Combined total: {len(combined_rows)}")
    print(f"Wrote {txt_path}")
    print(f"Wrote {csv_path}")
    print(f"Wrote {jsonl_path}")
    return 0


if __name__ == "__main__":
    main()
