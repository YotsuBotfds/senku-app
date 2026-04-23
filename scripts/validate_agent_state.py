#!/usr/bin/env python3
"""Validate the structured agent state file used for long-running Android work."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

import yaml


REQUIRED_TOP_LEVEL = {
    "schema_version": int,
    "last_updated": str,
    "continuation_window": dict,
    "current_focus": dict,
    "tooling_status": dict,
    "latest_validated_checkpoint": dict,
    "pending_actions": list,
    "files_touched": list,
    "risks": list,
    "notes_refs": dict,
}

REQUIRED_CONTINUATION = {
    "floor_started_at": str,
    "continue_until_at": str,
    "last_checkpoint_at": str,
}

REQUIRED_CHECKPOINT = {
    "label": str,
    "timestamp": str,
    "query": str,
    "artifacts": dict,
}


def _expect(mapping: dict, key: str, expected_type: type, errors: list[str], scope: str) -> None:
    value = mapping.get(key)
    if value is None:
        errors.append(f"missing {scope}.{key}")
        return
    if not isinstance(value, expected_type):
        errors.append(
            f"expected {scope}.{key} to be {expected_type.__name__}, got {type(value).__name__}"
        )


def validate_state(path: Path) -> tuple[dict | None, list[str]]:
    errors: list[str] = []
    if not path.exists():
        return None, [f"state file not found: {path}"]

    try:
        data = yaml.safe_load(path.read_text(encoding="utf-8"))
    except Exception as exc:  # pragma: no cover - surfaced directly to the caller
        return None, [f"failed to parse YAML: {exc}"]

    if not isinstance(data, dict):
        return None, ["top-level YAML document must be a mapping"]

    for key, expected_type in REQUIRED_TOP_LEVEL.items():
        _expect(data, key, expected_type, errors, "root")

    continuation = data.get("continuation_window")
    if isinstance(continuation, dict):
        for key, expected_type in REQUIRED_CONTINUATION.items():
            _expect(continuation, key, expected_type, errors, "continuation_window")

    checkpoint = data.get("latest_validated_checkpoint")
    if isinstance(checkpoint, dict):
        for key, expected_type in REQUIRED_CHECKPOINT.items():
            _expect(checkpoint, key, expected_type, errors, "latest_validated_checkpoint")
        artifacts = checkpoint.get("artifacts")
        if isinstance(artifacts, dict):
            for artifact_key, artifact_value in artifacts.items():
                if not isinstance(artifact_value, str):
                    errors.append(
                        f"expected latest_validated_checkpoint.artifacts.{artifact_key} to be str"
                    )
                    continue
                artifact_path = Path(artifact_value)
                if not artifact_path.exists():
                    errors.append(
                        f"artifact path does not exist: latest_validated_checkpoint.artifacts.{artifact_key}={artifact_value}"
                    )

    notes_refs = data.get("notes_refs")
    if isinstance(notes_refs, dict):
        for key, value in notes_refs.items():
            if not isinstance(value, str):
                errors.append(f"expected notes_refs.{key} to be str")
                continue
            if not Path(value).exists():
                errors.append(f"notes ref does not exist: notes_refs.{key}={value}")

    return data, errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "state_path",
        nargs="?",
        default="notes/AGENT_STATE.yaml",
        help="Path to the structured agent state YAML file.",
    )
    args = parser.parse_args()

    data, errors = validate_state(Path(args.state_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    checkpoint = data["latest_validated_checkpoint"]
    print("agent_state: ok")
    print(f"label: {checkpoint.get('label', '')}")
    print(f"query: {checkpoint.get('query', '')}")
    print(f"last_updated: {data.get('last_updated', '')}")
    print(f"continue_until_at: {data['continuation_window'].get('continue_until_at', '')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
