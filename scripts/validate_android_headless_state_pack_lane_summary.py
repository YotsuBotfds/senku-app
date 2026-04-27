#!/usr/bin/env python3
"""Validate run_android_headless_state_pack_lane.ps1 plan/summary JSON."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


ROLE_DEVICE_MATRIX: dict[str, dict[str, str]] = {
    "phone_portrait": {
        "device": "emulator-5556",
        "orientation": "portrait",
        "avd": "Senku_Large_4",
        "dimensions": "1080x2400",
    },
    "phone_landscape": {
        "device": "emulator-5560",
        "orientation": "landscape",
        "avd": "Senku_Large_3",
        "dimensions": "2400x1080",
    },
    "tablet_portrait": {
        "device": "emulator-5554",
        "orientation": "portrait",
        "avd": "Senku_Tablet_2",
        "dimensions": "1600x2560",
    },
    "tablet_landscape": {
        "device": "emulator-5558",
        "orientation": "landscape",
        "avd": "Senku_Tablet",
        "dimensions": "2560x1600",
    },
}

FIXED_ROLES = list(ROLE_DEVICE_MATRIX)

PLAN_REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "run_id": str,
    "output_root": str,
    "run_dir": str,
    "fixed_matrix_roles": list,
    "fixed_matrix": list,
    "launch_profile": str,
    "headless": bool,
    "mode": str,
    "max_parallel_devices": int,
    "effective_max_parallel_devices": int,
    "boot_wait_seconds": int,
    "host_inference_url": str,
    "host_inference_model": str,
    "skip_build": bool,
    "skip_install": bool,
    "skip_host_states": bool,
    "plan_only": bool,
    "whatif": bool,
    "real_run_requested": bool,
    "will_start_emulators": bool,
    "will_run_state_pack": bool,
    "acceptance_evidence": bool,
    "non_acceptance_evidence": bool,
    "acceptance_label_allowed": bool,
    "acceptance_criteria": list,
    "planning_artifacts_are_acceptance": bool,
    "commands": dict,
}

SUMMARY_REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "run_id": str,
    "output_root": str,
    "run_dir": str,
    "state_pack_summary_path": (str, type(None)),
    "status": str,
    "real_run": bool,
    "fixed_four_matrix_output": bool,
    "state_pack_passed": bool,
    "acceptance_evidence": bool,
    "non_acceptance_evidence": bool,
    "acceptance_label_allowed": bool,
    "acceptance_criteria": list,
    "pack_status": (str, type(None)),
    "pack_total_states": int,
    "pack_pass_count": int,
    "pack_fail_count": int,
}


def _typename(expected_type: type | tuple[type, ...]) -> str:
    if isinstance(expected_type, tuple):
        return "|".join(item.__name__ for item in expected_type)
    return expected_type.__name__


def _is_instance(value: Any, expected_type: type | tuple[type, ...]) -> bool:
    if expected_type is int:
        return isinstance(value, int) and not isinstance(value, bool)
    if isinstance(expected_type, tuple) and int in expected_type and isinstance(value, bool):
        return False
    return isinstance(value, expected_type)


def _expect_type(
    mapping: dict[str, Any],
    key: str,
    expected_type: type | tuple[type, ...],
    errors: list[str],
    scope: str = "root",
) -> None:
    if key not in mapping:
        errors.append(f"missing {scope}.{key}")
        return
    value = mapping[key]
    if not _is_instance(value, expected_type):
        errors.append(f"expected {scope}.{key} to be {_typename(expected_type)}, got {type(value).__name__}")
        return
    if isinstance(value, str) and not value.strip():
        errors.append(f"expected {scope}.{key} to be non-empty")


def _expect_value(mapping: dict[str, Any], key: str, expected: Any, errors: list[str], scope: str = "root") -> None:
    if key in mapping and mapping[key] != expected:
        errors.append(f"expected {scope}.{key} to be {expected!r}, got {mapping[key]!r}")


def _read_json(path: Path, noun: str) -> tuple[dict[str, Any] | None, list[str]]:
    if not path.exists():
        return None, [f"{noun} file not found: {path}"]
    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        return None, [f"failed to parse JSON: {exc}"]
    if not isinstance(data, dict):
        return None, ["top-level JSON document must be an object"]
    return data, []


def _roles_from_device(device: dict[str, Any], scope: str, errors: list[str]) -> list[str]:
    roles = device.get("roles")
    if isinstance(roles, list):
        parsed: list[str] = []
        for index, role in enumerate(roles):
            if not isinstance(role, str) or not role.strip():
                errors.append(f"expected {scope}.roles[{index}] to be non-empty str")
                continue
            parsed.append(role)
        if not parsed:
            errors.append(f"expected {scope}.roles to be non-empty")
        return parsed

    if "roles" in device:
        errors.append(f"expected {scope}.roles to be list")

    role = device.get("role")
    if isinstance(role, str) and role.strip():
        return [role]
    errors.append(f"expected {scope}.roles list or legacy scalar role")
    return []


def _validate_fixed_matrix_entries(entries: Any, errors: list[str], scope: str) -> None:
    if not isinstance(entries, list):
        return
    seen_roles: list[str] = []
    for index, entry in enumerate(entries):
        entry_scope = f"{scope}[{index}]"
        if not isinstance(entry, dict):
            errors.append(f"expected {entry_scope} to be dict, got {type(entry).__name__}")
            continue
        role = entry.get("role")
        if not isinstance(role, str) or not role.strip():
            errors.append(f"expected {entry_scope}.role to be non-empty str")
            continue
        seen_roles.append(role)
        expected = ROLE_DEVICE_MATRIX.get(role)
        if expected is None:
            errors.append(f"expected {entry_scope}.role to be a fixed matrix role, got {role!r}")
            continue
        for key, expected_value in expected.items():
            if entry.get(key) != expected_value:
                errors.append(f"expected {entry_scope}.{key} to be {expected_value!r}")
    if sorted(seen_roles) != sorted(FIXED_ROLES):
        errors.append(f"expected {scope} to contain the fixed four roles")


def _validate_fixed_four_pack_summary(pack: dict[str, Any], errors: list[str], scope: str = "state_pack") -> None:
    _expect_value(pack, "status", "pass", errors, scope)

    for key in ("total_states", "pass_count", "fail_count"):
        _expect_type(pack, key, int, errors, scope)

    total = pack.get("total_states")
    passed = pack.get("pass_count")
    failed = pack.get("fail_count")
    if isinstance(total, int) and not isinstance(total, bool) and total <= 0:
        errors.append(f"expected {scope}.total_states to be greater than zero")
    if isinstance(total, int) and isinstance(passed, int) and not isinstance(total, bool) and not isinstance(passed, bool):
        if passed != total:
            errors.append(f"expected {scope}.pass_count to equal {scope}.total_states")
    if isinstance(failed, int) and not isinstance(failed, bool) and failed != 0:
        errors.append(f"expected {scope}.fail_count to be 0")

    devices = pack.get("devices")
    if not isinstance(devices, list):
        errors.append(f"missing {scope}.devices")
        return

    roles_seen: set[str] = set()
    devices_with_roles = 0
    for index, device in enumerate(devices):
        device_scope = f"{scope}.devices[{index}]"
        if not isinstance(device, dict):
            errors.append(f"expected {device_scope} to be dict, got {type(device).__name__}")
            continue
        roles = _roles_from_device(device, device_scope, errors)
        if roles:
            devices_with_roles += 1
        for role in roles:
            if role not in ROLE_DEVICE_MATRIX:
                errors.append(f"expected {device_scope} role to be a fixed matrix role, got {role!r}")
                continue
            roles_seen.add(role)
            expected_device = ROLE_DEVICE_MATRIX[role]["device"]
            if "device" in device and device.get("device") != expected_device:
                errors.append(f"expected {device_scope}.device for {role!r} to be {expected_device!r}")

    if devices_with_roles < 4:
        errors.append(f"expected {scope}.devices to include four role-bearing device entries")
    if sorted(roles_seen) != sorted(FIXED_ROLES):
        errors.append(f"expected {scope}.devices roles to cover fixed four matrix")


def _resolve_pack_path(summary_path: Path, pack_path_value: Any) -> Path | None:
    if not isinstance(pack_path_value, str) or not pack_path_value.strip():
        return None
    pack_path = Path(pack_path_value)
    if pack_path.is_absolute():
        return pack_path
    candidate = (summary_path.parent / pack_path).resolve()
    if candidate.exists():
        return candidate
    return Path(pack_path_value).resolve()


def _validate_plan(data: dict[str, Any], errors: list[str]) -> None:
    for key, expected_type in PLAN_REQUIRED_TYPES.items():
        _expect_type(data, key, expected_type, errors)

    _expect_value(data, "headless", True, errors)
    _expect_value(data, "mode", "read_only", errors)
    _expect_value(data, "acceptance_evidence", False, errors)
    _expect_value(data, "non_acceptance_evidence", True, errors)
    _expect_value(data, "acceptance_label_allowed", False, errors)
    _expect_value(data, "planning_artifacts_are_acceptance", False, errors)
    _expect_value(data, "will_start_emulators", False, errors)
    _expect_value(data, "will_run_state_pack", False, errors)

    plan_only = data.get("plan_only")
    whatif = data.get("whatif")
    if plan_only is not True and whatif is not True:
        errors.append("expected PlanOnly/WhatIf lane plan to have plan_only or whatif true")

    if data.get("fixed_matrix_roles") != FIXED_ROLES:
        errors.append("expected root.fixed_matrix_roles to be fixed four matrix order")
    _validate_fixed_matrix_entries(data.get("fixed_matrix"), errors, "root.fixed_matrix")

    commands = data.get("commands")
    if isinstance(commands, dict):
        for key in ("emulator_profile_preflight", "emulator_real_run", "state_pack_real_run"):
            _expect_type(commands, key, str, errors, "root.commands")
        preflight = commands.get("emulator_profile_preflight")
        if isinstance(preflight, str) and "-WhatIf" not in preflight:
            errors.append("expected root.commands.emulator_profile_preflight to include -WhatIf")


def _validate_summary(data: dict[str, Any], path: Path, errors: list[str]) -> None:
    for key, expected_type in SUMMARY_REQUIRED_TYPES.items():
        _expect_type(data, key, expected_type, errors)

    acceptance = data.get("acceptance_evidence")
    real_run = data.get("real_run")
    fixed_four = data.get("fixed_four_matrix_output")
    pack_passed = data.get("state_pack_passed")

    if acceptance:
        _expect_value(data, "status", "pass", errors)
        _expect_value(data, "real_run", True, errors)
        _expect_value(data, "fixed_four_matrix_output", True, errors)
        _expect_value(data, "state_pack_passed", True, errors)
        _expect_value(data, "non_acceptance_evidence", False, errors)
        _expect_value(data, "acceptance_label_allowed", True, errors)
        _expect_value(data, "pack_status", "pass", errors)
        if data.get("pack_total_states", 0) <= 0:
            errors.append("expected root.pack_total_states to be greater than zero")
        if data.get("pack_pass_count") != data.get("pack_total_states"):
            errors.append("expected root.pack_pass_count to equal root.pack_total_states")
        _expect_value(data, "pack_fail_count", 0, errors)
        pack_path = _resolve_pack_path(path, data.get("state_pack_summary_path"))
        if pack_path is None:
            errors.append("expected root.state_pack_summary_path for acceptance evidence")
            return
        pack, pack_errors = _read_json(pack_path, "state-pack summary")
        if pack_errors:
            errors.extend(pack_errors)
            return
        assert pack is not None
        _validate_fixed_four_pack_summary(pack, errors)
        return

    _expect_value(data, "acceptance_evidence", False, errors)
    _expect_value(data, "non_acceptance_evidence", True, errors)
    _expect_value(data, "acceptance_label_allowed", False, errors)
    if fixed_four is True and pack_passed is True:
        errors.append("expected non-acceptance summaries to avoid fixed-four state-pack acceptance claims")
    if data.get("status") == "pass":
        errors.append("expected non-acceptance summaries not to use pass status")


def validate_summary(path: Path) -> tuple[dict[str, Any] | None, list[str]]:
    data, errors = _read_json(path, "summary")
    if errors:
        return data, errors
    assert data is not None

    errors = []
    if "fixed_matrix_roles" in data or "will_start_emulators" in data or "plan_only" in data:
        _validate_plan(data, errors)
    else:
        _validate_summary(data, path, errors)
    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("summary_path", help="Path to headless_lane_plan.json or headless_lane_summary.json.")
    args = parser.parse_args(argv)

    data, errors = validate_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    if "fixed_matrix_roles" in data:
        print("android_headless_state_pack_lane_plan: ok")
        print("evidence: non_acceptance")
    else:
        print("android_headless_state_pack_lane_summary: ok")
        print(f"evidence: {'acceptance' if data.get('acceptance_evidence') else 'non_acceptance'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
