#!/usr/bin/env python3
"""Compare two bench artifacts and report prompt-level deltas."""

import argparse
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from bench_artifact_tools import compare_artifacts, render_artifact_comparison_markdown  # noqa: E402


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("baseline", help="Baseline bench JSON or markdown artifact")
    parser.add_argument("candidate", help="Candidate bench JSON or markdown artifact")
    parser.add_argument("--output", help="Write markdown comparison to a file instead of stdout")
    args = parser.parse_args()

    comparison = compare_artifacts(args.baseline, args.candidate)
    rendered = render_artifact_comparison_markdown(comparison)
    if args.output:
        Path(args.output).write_text(rendered, encoding="utf-8")
    else:
        print(rendered)


if __name__ == "__main__":
    main()
