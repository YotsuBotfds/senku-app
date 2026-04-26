#!/usr/bin/env python3
"""Summarize high-token and citation-heavy prompts from bench JSON artifacts."""

import argparse
import json
import re
from pathlib import Path


CONTROL_RE = re.compile(r"[\x00-\x1f\x7f]+")


def markdown_text(value):
    return CONTROL_RE.sub(" ", str(value or "")).replace("|", "\\|").strip()


def markdown_code(value):
    text = markdown_text(value)
    longest_tick_run = max((len(match.group(0)) for match in re.finditer(r"`+", text)), default=0)
    fence = "`" * (longest_tick_run + 1)
    padding = " " if "`" in text else ""
    return f"{fence}{padding}{text}{padding}{fence}"


def numeric_value(value):
    if isinstance(value, bool):
        return 0
    try:
        number = int(value)
    except (TypeError, ValueError):
        return 0
    return max(number, 0)


def load_rows(path):
    data = json.loads(Path(path).read_text(encoding="utf-8"))
    rows = []
    for result in data.get("results", []):
        if not isinstance(result, dict):
            continue
        rows.append(
            {
                "artifact": Path(path).name,
                "section": result.get("section"),
                "question": result.get("question"),
                "decision_path": result.get("decision_path"),
                "decision_detail": result.get("decision_detail"),
                "completion_tokens": numeric_value(result.get("completion_tokens")),
                "duplicate_citation_count": numeric_value(result.get("duplicate_citation_count")),
                "retrieval_metadata_summary": result.get("retrieval_metadata_summary") or "",
            }
        )
    return data.get("summary", {}), rows


def render_rows(rows, metric, top_n):
    ordered = sorted(rows, key=lambda row: row[metric], reverse=True)
    return ordered[:top_n]


def print_markdown(paths, top_n):
    print("# Bench Watchlist")
    print()
    print(f"Generated from {len(paths)} artifact(s).")
    print()

    all_rows = []
    for path in paths:
        summary, rows = load_rows(path)
        all_rows.extend(rows)
        print(f"## {Path(path).name}")
        print()
        print(
            f"- prompts: {summary.get('total_prompts', 0)}"
            f"; errors: {summary.get('errors', 0)}"
            f"; decision_paths: {summary.get('decision_path_counts', {})}"
            f"; duplicate_citation_total: {summary.get('duplicate_citation_total', 0)}"
        )
        print()

    print("## Highest Completion Token Responses")
    print()
    for row in render_rows(all_rows, "completion_tokens", top_n):
        print(
            f"- {markdown_code(row['completion_tokens'])} tok | {markdown_code(row['artifact'])} | "
            f"{markdown_code(row['decision_path'])} | {markdown_text(row['question'])}"
        )
        print(f"  - section: {markdown_text(row['section'])}")
        if row["decision_detail"]:
            print(f"  - detail: {markdown_code(row['decision_detail'])}")
        if row["retrieval_metadata_summary"]:
            print(f"  - retrieval: {markdown_text(row['retrieval_metadata_summary'])}")
    print()

    print("## Highest Duplicate Citation Counts")
    print()
    for row in render_rows(all_rows, "duplicate_citation_count", top_n):
        print(
            f"- {markdown_code(row['duplicate_citation_count'])} dup cites | "
            f"{markdown_code(row['artifact'])} | {markdown_code(row['decision_path'])} | "
            f"{markdown_text(row['question'])}"
        )
        print(f"  - section: {markdown_text(row['section'])}")
        if row["decision_detail"]:
            print(f"  - detail: {markdown_code(row['decision_detail'])}")
        if row["retrieval_metadata_summary"]:
            print(f"  - retrieval: {markdown_text(row['retrieval_metadata_summary'])}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("artifacts", nargs="+", help="Bench JSON artifact paths")
    parser.add_argument("--top", type=int, default=10, help="Rows to show per watchlist")
    args = parser.parse_args()

    print_markdown(args.artifacts, args.top)


if __name__ == "__main__":
    main()
