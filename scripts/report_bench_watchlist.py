#!/usr/bin/env python3
"""Summarize high-token and citation-heavy prompts from bench JSON artifacts."""

import argparse
import json
from pathlib import Path


def load_rows(path):
    data = json.loads(Path(path).read_text(encoding="utf-8"))
    rows = []
    for result in data.get("results", []):
        rows.append(
            {
                "artifact": Path(path).name,
                "section": result.get("section"),
                "question": result.get("question"),
                "decision_path": result.get("decision_path"),
                "decision_detail": result.get("decision_detail"),
                "completion_tokens": result.get("completion_tokens") or 0,
                "duplicate_citation_count": result.get("duplicate_citation_count") or 0,
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
            f"- `{row['completion_tokens']}` tok | `{row['artifact']}` | "
            f"`{row['decision_path']}` | {row['question']}"
        )
        print(f"  - section: {row['section']}")
        if row["decision_detail"]:
            print(f"  - detail: `{row['decision_detail']}`")
        if row["retrieval_metadata_summary"]:
            print(f"  - retrieval: {row['retrieval_metadata_summary']}")
    print()

    print("## Highest Duplicate Citation Counts")
    print()
    for row in render_rows(all_rows, "duplicate_citation_count", top_n):
        print(
            f"- `{row['duplicate_citation_count']}` dup cites | `{row['artifact']}` | "
            f"`{row['decision_path']}` | {row['question']}"
        )
        print(f"  - section: {row['section']}")
        if row["decision_detail"]:
            print(f"  - detail: `{row['decision_detail']}`")
        if row["retrieval_metadata_summary"]:
            print(f"  - retrieval: {row['retrieval_metadata_summary']}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("artifacts", nargs="+", help="Bench JSON artifact paths")
    parser.add_argument("--top", type=int, default=10, help="Rows to show per watchlist")
    args = parser.parse_args()

    print_markdown(args.artifacts, args.top)


if __name__ == "__main__":
    main()
