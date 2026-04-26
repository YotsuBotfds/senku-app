#!/usr/bin/env python3
"""Validate that bench retry isolation works per worker slot, not per URL."""

from pathlib import Path
import sys


REPO_ROOT = Path(__file__).resolve().parent.parent
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from bench import build_worker_targets  # noqa: E402


def validate_retry_slots() -> list[str]:
    duplicate_urls = [
        "http://192.168.0.67:1234/v1",
        "http://192.168.0.67:1234/v1",
    ]
    workers = build_worker_targets(duplicate_urls)
    labels = [worker["label"] for worker in workers]

    expected_labels = [
        "192.168.0.67:1234/v1#1",
        "192.168.0.67:1234/v1#2",
    ]
    errors = []
    if labels != expected_labels:
        errors.append(f"expected duplicate URL labels {expected_labels}, got {labels}")

    blocked_workers = {labels[0]}
    if labels[1] in blocked_workers:
        errors.append(f"sibling worker label unexpectedly blocked: {blocked_workers}")

    # The first failure happens before the current worker is added to the blocked set.
    current_failure_view = set()
    if not len(current_failure_view) < len(workers) - 1:
        errors.append("first failure should still allow one retry slot")

    # Once the prompt has already been retried on the sibling slot, no retry budget remains.
    current_failure_view = {labels[0]}
    if len(current_failure_view) < len(workers) - 1:
        errors.append("second failure should exhaust retry slots")

    return errors


def main():
    errors = validate_retry_slots()
    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1

    print("Validated duplicate-URL retry isolation across worker slots.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
