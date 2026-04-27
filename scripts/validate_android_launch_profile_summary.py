#!/usr/bin/env python3
"""Validate Android emulator launch-profile preflight summaries."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


REQUIRED_TOP_LEVEL: dict[str, type | tuple[type, ...]] = {
    "selected_lanes": list,
    "profile_metadata": dict,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "preflight_only": bool,
}

REQUIRED_PROFILE_METADATA: dict[str, type | tuple[type, ...]] = {
    "profile": str,
    "preflight_only": bool,
    "whatif_metadata_only": bool,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "headless": bool,
    "partition_size_mb": (int, type(None)),
    "data_sizing": str,
    "snapshot_cache_posture": str,
}

OPTIONAL_PROFILE_EXPECTATIONS: dict[str, type | tuple[type, ...]] = {
    "expected_role": str,
    "expected_serial": str,
}

REQUIRED_LANE_FIELDS: dict[str, type | tuple[type, ...]] = {
    "role": str,
    "avd": str,
    "port": int,
    "serial": str,
    "resolution": str,
    "orientation": str,
    "emulator_args": list,
}

VALID_ORIENTATIONS = {"portrait", "landscape"}


def _type_name(expected_type: type | tuple[type, ...]) -> str:
    if isinstance(expected_type, tuple):
        return "|".join(item.__name__ for item in expected_type)
    return expected_type.__name__


def _expect(
    mapping: dict[str, Any],
    key: str,
    expected_type: type | tuple[type, ...],
    errors: list[str],
    scope: str,
) -> None:
    if key not in mapping:
        errors.append(f"missing {scope}.{key}")
        return

    value = mapping[key]
    if not isinstance(value, expected_type):
        errors.append(
            f"expected {scope}.{key} to be {_type_name(expected_type)}, got {type(value).__name__}"
        )
        return

    if isinstance(value, str) and not value.strip():
        errors.append(f"expected {scope}.{key} to be non-empty")
    if isinstance(value, int) and not isinstance(value, bool) and value < 0:
        errors.append(f"expected {scope}.{key} to be non-negative")


def _validate_required_fields(
    mapping: Any,
    required_fields: dict[str, type | tuple[type, ...]],
    errors: list[str],
    scope: str,
) -> None:
    if not isinstance(mapping, dict):
        errors.append(f"expected {scope} to be dict, got {type(mapping).__name__}")
        return

    for key, expected_type in required_fields.items():
        _expect(mapping, key, expected_type, errors, scope)


def _validate_profile_metadata(profile_metadata: Any, errors: list[str]) -> None:
    scope = "profile_metadata"
    _validate_required_fields(profile_metadata, REQUIRED_PROFILE_METADATA, errors, scope)
    if not isinstance(profile_metadata, dict):
        return

    for key, expected_type in OPTIONAL_PROFILE_EXPECTATIONS.items():
        if key in profile_metadata:
            _expect(profile_metadata, key, expected_type, errors, scope)

    if profile_metadata.get("preflight_only") is not True:
        errors.append("expected profile_metadata.preflight_only to be true")
    if profile_metadata.get("whatif_metadata_only") is not True:
        errors.append("expected profile_metadata.whatif_metadata_only to be true")
    if profile_metadata.get("non_acceptance_evidence") is not True:
        errors.append("expected profile_metadata.non_acceptance_evidence to be true")
    if profile_metadata.get("acceptance_evidence") is not False:
        errors.append("expected profile_metadata.acceptance_evidence to be false")
    if isinstance(profile_metadata.get("partition_size_mb"), bool):
        errors.append("expected profile_metadata.partition_size_mb to be int|None, got bool")


def _validate_emulator_args(lane: dict[str, Any], errors: list[str], scope: str) -> None:
    emulator_args = lane.get("emulator_args")
    if not isinstance(emulator_args, list):
        return

    if not emulator_args:
        errors.append(f"expected {scope}.emulator_args to be non-empty")
        return
    if any(not isinstance(item, str) or not item.strip() for item in emulator_args):
        errors.append(f"expected {scope}.emulator_args to contain only non-empty strings")
        return

    expected_pairs = {
        "-avd": lane.get("avd"),
        "-port": str(lane.get("port")),
    }
    for flag, expected_value in expected_pairs.items():
        if flag not in emulator_args:
            errors.append(f"missing {scope}.emulator_args {flag}")
            continue
        flag_index = emulator_args.index(flag)
        if flag_index + 1 >= len(emulator_args):
            errors.append(f"missing value after {scope}.emulator_args {flag}")
            continue
        actual_value = emulator_args[flag_index + 1]
        if actual_value != expected_value:
            errors.append(
                f"expected {scope}.emulator_args {flag} value to be {expected_value}, got {actual_value}"
            )


def _validate_selected_lanes(
    selected_lanes: Any,
    profile_metadata: Any,
    errors: list[str],
) -> None:
    if not isinstance(selected_lanes, list):
        errors.append(f"expected selected_lanes to be list, got {type(selected_lanes).__name__}")
        return
    if not selected_lanes:
        errors.append("expected selected_lanes to contain at least one lane")
        return

    expected_role = None
    expected_serial = None
    if isinstance(profile_metadata, dict):
        expected_role = profile_metadata.get("expected_role")
        expected_serial = profile_metadata.get("expected_serial")

    role_matches = False
    serial_matches = False
    for index, lane in enumerate(selected_lanes):
        scope = f"selected_lanes[{index}]"
        _validate_required_fields(lane, REQUIRED_LANE_FIELDS, errors, scope)
        if not isinstance(lane, dict):
            continue

        orientation = lane.get("orientation")
        if isinstance(orientation, str) and orientation not in VALID_ORIENTATIONS:
            errors.append(
                f"expected {scope}.orientation to be one of {', '.join(sorted(VALID_ORIENTATIONS))}"
            )
        _validate_emulator_args(lane, errors, scope)

        if lane.get("role") == expected_role:
            role_matches = True
        if lane.get("serial") == expected_serial:
            serial_matches = True

    if isinstance(expected_role, str) and expected_role.strip() and not role_matches:
        errors.append(f"expected selected_lanes to include profile_metadata.expected_role {expected_role}")
    if isinstance(expected_serial, str) and expected_serial.strip() and not serial_matches:
        errors.append(
            f"expected selected_lanes to include profile_metadata.expected_serial {expected_serial}"
        )


def validate_launch_profile_summary(path: Path) -> tuple[dict[str, Any] | None, list[str]]:
    if not path.exists():
        return None, [f"summary file not found: {path}"]

    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        return None, [f"failed to parse JSON: {exc}"]

    if not isinstance(data, dict):
        return None, ["top-level JSON document must be an object"]

    errors: list[str] = []
    _validate_required_fields(data, REQUIRED_TOP_LEVEL, errors, "root")

    if data.get("preflight_only") is not True:
        errors.append("expected root.preflight_only to be true")
    if data.get("non_acceptance_evidence") is not True:
        errors.append("expected root.non_acceptance_evidence to be true")
    if data.get("acceptance_evidence") is not False:
        errors.append("expected root.acceptance_evidence to be false")

    profile_metadata = data.get("profile_metadata")
    _validate_profile_metadata(profile_metadata, errors)
    _validate_selected_lanes(data.get("selected_lanes"), profile_metadata, errors)

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "summary_path",
        help="Path to a start_senku_emulator_matrix.ps1 -WhatIf -LaunchProfile summary JSON file.",
    )
    args = parser.parse_args(argv)

    data, errors = validate_launch_profile_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    profile_metadata = data["profile_metadata"]
    print("android_launch_profile_summary: ok")
    print(f"profile: {profile_metadata.get('profile')}")
    print(f"selected_lanes: {len(data.get('selected_lanes', []))}")
    print("evidence: non_acceptance")
    return 0


if __name__ == "__main__":
    sys.exit(main())
