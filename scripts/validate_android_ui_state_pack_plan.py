#!/usr/bin/env python3
"""Validate build_android_ui_state_pack_parallel.ps1 -PlanOnly plan.json."""

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

FIXED_ROLE_ORDER = list(ROLE_DEVICE_MATRIX)

REQUIRED_ROOT_FIELDS: dict[str, type | tuple[type, ...]] = {
    "run_id": str,
    "output_root": str,
    "preflight_only": bool,
    "plan_only": bool,
    "acceptance_evidence": bool,
    "non_acceptance_evidence": bool,
    "will_build": bool,
    "will_install": bool,
    "will_start_role_jobs": bool,
    "will_finalize": bool,
    "selected_roles": list,
    "devices": list,
    "skip_build": bool,
    "skip_install": bool,
    "skip_host_states": bool,
    "host_inference_url": str,
    "host_inference_model": str,
    "max_parallel_devices": int,
    "effective_max_parallel_devices": int,
    "migration_checklist_intent": dict,
    "launchers": list,
}

REQUIRED_DEVICE_FIELDS: dict[str, type | tuple[type, ...]] = {
    "role": str,
    "device": str,
    "orientation": str,
    "avd": str,
    "dimensions": str,
}

REQUIRED_LAUNCHER_FIELDS: dict[str, type | tuple[type, ...]] = {
    "role": str,
    "command": str,
}

REQUIRED_INTENT_FIELDS: dict[str, type | tuple[type, ...]] = {
    "selected_roles": list,
    "host_flags": dict,
    "host_model_identity": dict,
    "skip_flags": dict,
    "max_parallel_devices": int,
    "effective_max_parallel_devices": int,
    "acceptance_evidence": bool,
    "non_acceptance_evidence": bool,
    "preflight_only": bool,
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
    if not _is_instance(value, expected_type):
        errors.append(f"expected {scope}.{key} to be {_typename(expected_type)}, got {type(value).__name__}")
        return

    if isinstance(value, str) and not value.strip():
        errors.append(f"expected {scope}.{key} to be non-empty")


def _expect_bool_value(mapping: dict[str, Any], key: str, expected: bool, errors: list[str], scope: str) -> None:
    if mapping.get(key) is not expected:
        errors.append(f"expected {scope}.{key} to be {str(expected).lower()}")


def _validate_selected_roles(data: dict[str, Any], errors: list[str]) -> list[str]:
    roles = data.get("selected_roles")
    if not isinstance(roles, list):
        return []

    selected: list[str] = []
    for index, role in enumerate(roles):
        if not isinstance(role, str) or not role.strip():
            errors.append(f"expected root.selected_roles[{index}] to be non-empty str")
            continue
        selected.append(role)
        if role not in ROLE_DEVICE_MATRIX:
            errors.append(f"expected root.selected_roles[{index}] to be a fixed matrix role, got {role!r}")

    if not selected:
        errors.append("expected root.selected_roles to be non-empty")

    expected_order = [role for role in FIXED_ROLE_ORDER if role in selected]
    if selected != expected_order:
        errors.append(
            "expected root.selected_roles to follow fixed matrix order: "
            f"{', '.join(expected_order)}"
        )

    if len(set(selected)) != len(selected):
        errors.append("expected root.selected_roles to be unique")

    return selected


def _validate_devices(data: dict[str, Any], selected_roles: list[str], errors: list[str]) -> None:
    devices = data.get("devices")
    if not isinstance(devices, list):
        return

    device_roles: list[str] = []
    for index, device in enumerate(devices):
        scope = f"root.devices[{index}]"
        if not isinstance(device, dict):
            errors.append(f"expected {scope} to be dict, got {type(device).__name__}")
            continue
        for key, expected_type in REQUIRED_DEVICE_FIELDS.items():
            _expect(device, key, expected_type, errors, scope)

        role = device.get("role")
        if not isinstance(role, str):
            continue
        device_roles.append(role)
        expected = ROLE_DEVICE_MATRIX.get(role)
        if expected is None:
            errors.append(f"expected {scope}.role to be a fixed matrix role, got {role!r}")
            continue
        for key, expected_value in expected.items():
            if device.get(key) != expected_value:
                errors.append(f"expected {scope}.{key} to be {expected_value!r}")

    if device_roles != selected_roles:
        errors.append("expected root.devices roles to exactly match root.selected_roles")


def _validate_skip_and_host_flags(data: dict[str, Any], intent: dict[str, Any], errors: list[str]) -> None:
    host_flags = intent.get("host_flags")
    if isinstance(host_flags, dict):
        _expect(host_flags, "skip_host_states", bool, errors, "migration_checklist_intent.host_flags")
        _expect(host_flags, "will_request_host_states", bool, errors, "migration_checklist_intent.host_flags")
        if host_flags.get("skip_host_states") != data.get("skip_host_states"):
            errors.append("expected migration_checklist_intent.host_flags.skip_host_states to mirror root.skip_host_states")
        if isinstance(data.get("skip_host_states"), bool) and host_flags.get("will_request_host_states") is not (not data["skip_host_states"]):
            errors.append(
                "expected migration_checklist_intent.host_flags.will_request_host_states "
                "to be the inverse of root.skip_host_states"
            )

    skip_flags = intent.get("skip_flags")
    if isinstance(skip_flags, dict):
        for key in ("skip_build", "skip_install", "skip_host_states"):
            _expect(skip_flags, key, bool, errors, "migration_checklist_intent.skip_flags")
            if skip_flags.get(key) != data.get(key):
                errors.append(f"expected migration_checklist_intent.skip_flags.{key} to mirror root.{key}")

    host_model_identity = intent.get("host_model_identity")
    if isinstance(host_model_identity, dict):
        _expect(host_model_identity, "host_inference_url", str, errors, "migration_checklist_intent.host_model_identity")
        _expect(host_model_identity, "host_inference_model", str, errors, "migration_checklist_intent.host_model_identity")
        if host_model_identity.get("host_inference_url") != data.get("host_inference_url"):
            errors.append("expected migration_checklist_intent.host_model_identity.host_inference_url to mirror root.host_inference_url")
        if host_model_identity.get("host_inference_model") != data.get("host_inference_model"):
            errors.append(
                "expected migration_checklist_intent.host_model_identity.host_inference_model "
                "to mirror root.host_inference_model"
            )


def _validate_intent(data: dict[str, Any], selected_roles: list[str], errors: list[str]) -> None:
    intent = data.get("migration_checklist_intent")
    if not isinstance(intent, dict):
        return

    for key, expected_type in REQUIRED_INTENT_FIELDS.items():
        _expect(intent, key, expected_type, errors, "migration_checklist_intent")

    if intent.get("selected_roles") != selected_roles:
        errors.append("expected migration_checklist_intent.selected_roles to mirror root.selected_roles")
    _expect_bool_value(intent, "acceptance_evidence", False, errors, "migration_checklist_intent")
    _expect_bool_value(intent, "non_acceptance_evidence", True, errors, "migration_checklist_intent")
    _expect_bool_value(intent, "preflight_only", True, errors, "migration_checklist_intent")
    for key in ("acceptance_evidence", "non_acceptance_evidence", "preflight_only"):
        if intent.get(key) != data.get(key):
            errors.append(f"expected migration_checklist_intent.{key} to mirror root.{key}")
    for key in ("max_parallel_devices", "effective_max_parallel_devices"):
        if intent.get(key) != data.get(key):
            errors.append(f"expected migration_checklist_intent.{key} to mirror root.{key}")

    _validate_skip_and_host_flags(data, intent, errors)


def _validate_launchers(data: dict[str, Any], selected_roles: list[str], errors: list[str]) -> None:
    launchers = data.get("launchers")
    if not isinstance(launchers, list):
        return

    launcher_roles: list[str] = []
    for index, launcher in enumerate(launchers):
        scope = f"root.launchers[{index}]"
        if not isinstance(launcher, dict):
            errors.append(f"expected {scope} to be dict, got {type(launcher).__name__}")
            continue
        for key, expected_type in REQUIRED_LAUNCHER_FIELDS.items():
            _expect(launcher, key, expected_type, errors, scope)

        role = launcher.get("role")
        command = launcher.get("command")
        if isinstance(role, str):
            launcher_roles.append(role)
        if isinstance(role, str) and isinstance(command, str):
            if role not in selected_roles:
                errors.append(f"expected {scope}.role to be selected")
            if f'-RoleFilter "{role}"' not in command:
                errors.append(f"expected {scope}.command to contain selected role {role!r}")
            if "-SkipFinalize" not in command or "-SkipBuild" not in command:
                errors.append(f"expected {scope}.command to keep launcher in slice/preflight posture")

    if sorted(launcher_roles) != sorted(selected_roles):
        errors.append("expected root.launchers to contain exactly one launcher for each selected role")


def validate_plan(path: Path) -> tuple[dict[str, Any] | None, list[str]]:
    if not path.exists():
        return None, [f"plan file not found: {path}"]

    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        return None, [f"failed to parse JSON: {exc}"]

    if not isinstance(data, dict):
        return None, ["top-level JSON document must be an object"]

    errors: list[str] = []
    for key, expected_type in REQUIRED_ROOT_FIELDS.items():
        _expect(data, key, expected_type, errors, "root")

    for key in ("plan_only", "preflight_only", "non_acceptance_evidence"):
        _expect_bool_value(data, key, True, errors, "root")
    for key in ("acceptance_evidence", "will_build", "will_install", "will_start_role_jobs", "will_finalize"):
        _expect_bool_value(data, key, False, errors, "root")

    max_parallel = data.get("max_parallel_devices")
    effective_max = data.get("effective_max_parallel_devices")
    if isinstance(max_parallel, int) and not isinstance(max_parallel, bool):
        expected_effective = max(1, max_parallel)
        if effective_max != expected_effective:
            errors.append(f"expected root.effective_max_parallel_devices to be {expected_effective}")

    selected_roles = _validate_selected_roles(data, errors)
    _validate_devices(data, selected_roles, errors)
    _validate_intent(data, selected_roles, errors)
    _validate_launchers(data, selected_roles, errors)

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("plan_path", help="Path to a -PlanOnly plan.json file.")
    args = parser.parse_args(argv)

    data, errors = validate_plan(Path(args.plan_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_ui_state_pack_plan: ok")
    print("evidence: non_acceptance")
    print("preflight_only: true")
    print(f"selected_roles: {len(data.get('selected_roles', []))}")
    print(f"devices: {len(data.get('devices', []))}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
