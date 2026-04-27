#!/usr/bin/env python3
"""Validate CaptureSummaryPath output from run_android_instrumented_ui_smoke.ps1."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.validate_android_capture_summary import validate_capture_summary


def validate_instrumented_capture_summary(path: Path):
    return validate_capture_summary(path)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "summary_path",
        help="Path to a run_android_instrumented_ui_smoke.ps1 CaptureSummaryPath JSON file.",
    )
    args = parser.parse_args(argv)

    data, errors = validate_instrumented_capture_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_instrumented_capture_summary: ok")
    print(f"serial: {data.get('serial')}")
    print(f"role: {data.get('role')}")
    print(f"orientation: {data.get('orientation')}")
    print("evidence: non_acceptance")
    return 0


if __name__ == "__main__":
    sys.exit(main())
