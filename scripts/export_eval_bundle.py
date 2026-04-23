#!/usr/bin/env python3
"""Export bench artifacts into an evaluator-friendly JSONL or CSV bundle."""

import argparse
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from bench_artifact_tools import (  # noqa: E402
    build_eval_rows,
    eval_rows_to_csv,
    eval_rows_to_jsonl,
)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("artifacts", nargs="+", help="Bench JSON or markdown artifact paths")
    parser.add_argument(
        "--format",
        choices=("jsonl", "csv"),
        default="jsonl",
        help="Export format (default: jsonl)",
    )
    parser.add_argument("--output", help="Write to a file instead of stdout")
    args = parser.parse_args()

    rows = build_eval_rows(args.artifacts)
    rendered = eval_rows_to_jsonl(rows) if args.format == "jsonl" else eval_rows_to_csv(rows)
    if args.output:
        Path(args.output).write_text(rendered + ("" if rendered.endswith("\n") else "\n"), encoding="utf-8")
    else:
        print(rendered)


if __name__ == "__main__":
    main()
