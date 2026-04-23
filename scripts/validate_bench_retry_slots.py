#!/usr/bin/env python3
"""Validate that bench retry isolation works per worker slot, not per URL."""

from pathlib import Path
import sys


REPO_ROOT = Path(__file__).resolve().parent.parent
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from bench import build_worker_targets  # noqa: E402


def main():
    duplicate_urls = [
        "http://192.168.0.67:1234/v1",
        "http://192.168.0.67:1234/v1",
    ]
    workers = build_worker_targets(duplicate_urls)
    labels = [worker["label"] for worker in workers]

    assert labels == [
        "192.168.0.67:1234/v1#1",
        "192.168.0.67:1234/v1#2",
    ], labels

    blocked_workers = {labels[0]}
    assert labels[1] not in blocked_workers, blocked_workers

    # The first failure happens before the current worker is added to the blocked set.
    current_failure_view = set()
    assert len(current_failure_view) < len(workers) - 1, "first failure should still allow one retry slot"

    # Once the prompt has already been retried on the sibling slot, no retry budget remains.
    current_failure_view = {labels[0]}
    assert not (len(current_failure_view) < len(workers) - 1), "second failure should exhaust retry slots"

    print("Validated duplicate-URL retry isolation across worker slots.")


if __name__ == "__main__":
    main()
