#!/usr/bin/env python3
"""Validate compact Android migration-proof fields in a state-pack summary."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


REQUIRED_TOP_LEVEL: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "total_states": int,
    "pass_count": int,
    "fail_count": int,
    "platform_anr_count": int,
    "matrix_homogeneous": bool,
    "matrix_apk_sha": str,
    "matrix_model_name": str,
    "matrix_model_sha": str,
    "devices": list,
}

REQUIRED_DEVICE_FIELDS: dict[str, type | tuple[type, ...]] = {
    "device": str,
    "roles": list,
    "state_count": int,
    "pass_count": int,
    "fail_count": int,
    "apk_sha": str,
    "model_name": str,
    "model_sha": str,
    "identity_conflict": bool,
    "identity_missing": bool,
}

REQUIRED_INSTALLED_PACK_FIELDS: dict[str, type | tuple[type, ...]] = {
    "metadata_present": bool,
    "matrix_homogeneous": bool,
    "devices": list,
}

REQUIRED_INSTALLED_PACK_DEVICE_FIELDS: dict[str, type | tuple[type, ...]] = {
    "device": str,
    "state_count": int,
    "metadata_count": int,
    "metadata_missing": bool,
    "metadata_conflict": bool,
}


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
        if isinstance(expected_type, tuple):
            type_names = "|".join(item.__name__ for item in expected_type)
        else:
            type_names = expected_type.__name__
        errors.append(f"expected {scope}.{key} to be {type_names}, got {type(value).__name__}")
        return

    if isinstance(value, str) and not value.strip():
        errors.append(f"expected {scope}.{key} to be non-empty")


def _validate_device(device: Any, index: int, errors: list[str]) -> None:
    scope = f"devices[{index}]"
    if not isinstance(device, dict):
        errors.append(f"expected {scope} to be dict, got {type(device).__name__}")
        return

    for key, expected_type in REQUIRED_DEVICE_FIELDS.items():
        _expect(device, key, expected_type, errors, scope)

    roles = device.get("roles")
    if isinstance(roles, list):
        if not roles:
            errors.append(f"expected {scope}.roles to be non-empty")
        for role_index, role in enumerate(roles):
            if not isinstance(role, str) or not role.strip():
                errors.append(f"expected {scope}.roles[{role_index}] to be non-empty str")


def _validate_non_negative_count(mapping: dict[str, Any], key: str, errors: list[str], scope: str) -> None:
    value = mapping.get(key)
    if isinstance(value, int) and not isinstance(value, bool) and value < 0:
        errors.append(f"expected {scope}.{key} to be non-negative")


def _validate_installed_pack_device(device: Any, index: int, errors: list[str]) -> None:
    scope = f"installed_pack.devices[{index}]"
    if not isinstance(device, dict):
        errors.append(f"expected {scope} to be dict, got {type(device).__name__}")
        return

    for key, expected_type in REQUIRED_INSTALLED_PACK_DEVICE_FIELDS.items():
        _expect(device, key, expected_type, errors, scope)

    _validate_non_negative_count(device, "state_count", errors, scope)
    _validate_non_negative_count(device, "metadata_count", errors, scope)
    state_count = device.get("state_count")
    metadata_count = device.get("metadata_count")
    if (
        isinstance(state_count, int)
        and not isinstance(state_count, bool)
        and isinstance(metadata_count, int)
        and not isinstance(metadata_count, bool)
        and metadata_count > state_count
    ):
        errors.append(f"expected {scope}.metadata_count to be <= {scope}.state_count")


def _validate_installed_pack(installed_pack: Any, errors: list[str]) -> None:
    scope = "installed_pack"
    if not isinstance(installed_pack, dict):
        errors.append(f"expected root.{scope} to be dict, got {type(installed_pack).__name__}")
        return

    for key, expected_type in REQUIRED_INSTALLED_PACK_FIELDS.items():
        _expect(installed_pack, key, expected_type, errors, scope)

    devices = installed_pack.get("devices")
    if isinstance(devices, list):
        if not devices:
            errors.append(f"expected {scope}.devices to be non-empty")
        for index, device in enumerate(devices):
            _validate_installed_pack_device(device, index, errors)


def validate_summary(path: Path) -> tuple[dict[str, Any] | None, list[str]]:
    if not path.exists():
        return None, [f"summary file not found: {path}"]

    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        return None, [f"failed to parse JSON: {exc}"]

    if not isinstance(data, dict):
        return None, ["top-level JSON document must be an object"]

    errors: list[str] = []
    for key, expected_type in REQUIRED_TOP_LEVEL.items():
        _expect(data, key, expected_type, errors, "root")

    devices = data.get("devices")
    if isinstance(devices, list):
        if not devices:
            errors.append("expected root.devices to be non-empty")
        for index, device in enumerate(devices):
            _validate_device(device, index, errors)

    if "installed_pack" in data:
        _validate_installed_pack(data["installed_pack"], errors)

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("summary_path", help="Path to an Android state-pack summary.json file.")
    args = parser.parse_args(argv)

    data, errors = validate_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_migration_summary: ok")
    print(f"status: {data.get('status')}")
    print(f"devices: {len(data.get('devices', []))}")
    print(f"total_states: {data.get('total_states')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
