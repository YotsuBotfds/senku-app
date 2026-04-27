#!/usr/bin/env python3
"""Validate run_android_harness_matrix.ps1 -PlanOnly summary.json output."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


PLAN_KIND = "android_harness_matrix"
ROOT_BOOL_REQUIREMENTS: dict[str, bool] = {
    "preflight_only": True,
    "plan_only": True,
    "non_acceptance_evidence": True,
    "acceptance_evidence": False,
    "will_start_jobs": False,
    "will_touch_emulators": False,
}
ROOT_REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "plan_kind": str,
    "row_count": int,
    "rows": list,
    "posture_groups": list,
    "runner_commands": list,
    "migration_checklist_intent": dict,
}
ROW_REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "row_number": int,
    "run_label": str,
    "mode": str,
    "emulator": str,
    "posture": str,
    "device_role": str,
    "orientation": str,
    "warm_start": bool,
    "will_push_pack": bool,
    "skip_pack_push_if_current": bool,
    "force_pack_push": bool,
    "runner_command": str,
}
POSTURE_GROUP_REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "posture": str,
    "row_count": int,
    "emulators": list,
    "run_labels": list,
}
VALIDATION_COMMAND_REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "name": str,
    "command": str,
    "validates": str,
    "plan_only": bool,
    "will_start_jobs": bool,
    "will_touch_emulators": bool,
}
OPTIONAL_PACK_INTENT_TYPES: dict[str, type | tuple[type, ...]] = {
    "push_pack_dir": (str, type(None)),
    "pack_push_dir": (str, type(None)),
    "skip_pack_push_if_current": bool,
    "force_pack_push": bool,
    "will_push_pack": bool,
}
MIRRORED_INTENT_KEYS = (
    "plan_kind",
    "preflight_only",
    "plan_only",
    "non_acceptance_evidence",
    "acceptance_evidence",
    "will_start_jobs",
    "will_touch_emulators",
)


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


def _expect_str_list(value: Any, errors: list[str], scope: str) -> None:
    if not isinstance(value, list):
        errors.append(f"expected {scope} to be list, got {type(value).__name__}")
        return
    if not value:
        errors.append(f"expected {scope} to be non-empty")
    for index, item in enumerate(value):
        if not isinstance(item, str) or not item.strip():
            errors.append(f"expected {scope}[{index}] to be non-empty str")


def _expect_bool(mapping: dict[str, Any], key: str, expected: bool, errors: list[str], scope: str) -> None:
    _expect(mapping, key, bool, errors, scope)
    if isinstance(mapping.get(key), bool) and mapping[key] is not expected:
        errors.append(f"expected {scope}.{key} to be {str(expected).lower()}")


def _validate_pack_intent(mapping: dict[str, Any], errors: list[str], scope: str) -> None:
    for key, expected_type in OPTIONAL_PACK_INTENT_TYPES.items():
        if key in mapping:
            value = mapping[key]
            if not isinstance(value, expected_type):
                if isinstance(expected_type, tuple):
                    type_names = "|".join(item.__name__ for item in expected_type)
                else:
                    type_names = expected_type.__name__
                errors.append(f"expected {scope}.{key} to be {type_names}, got {type(value).__name__}")

    if "push_pack_dir" in mapping and "will_push_pack" in mapping:
        push_pack_dir = mapping.get("push_pack_dir")
        will_push_pack = mapping.get("will_push_pack")
        if isinstance(push_pack_dir, str) and isinstance(will_push_pack, bool):
            if bool(push_pack_dir.strip()) != will_push_pack:
                errors.append(f"expected {scope}.will_push_pack to mirror non-empty {scope}.push_pack_dir")


def _validate_rows(data: dict[str, Any], errors: list[str]) -> None:
    rows = data.get("rows")
    if not isinstance(rows, list):
        return
    if not rows:
        errors.append("expected root.rows to be non-empty")

    runner_commands = data.get("runner_commands")
    row_count = data.get("row_count")
    if isinstance(row_count, int) and not isinstance(row_count, bool) and row_count != len(rows):
        errors.append("expected root.row_count to equal len(root.rows)")
    if isinstance(runner_commands, list) and len(runner_commands) != len(rows):
        errors.append("expected root.runner_commands to contain one command per row")

    selected_emulators = data.get("selected_emulators")
    if selected_emulators is not None:
        _expect_str_list(selected_emulators, errors, "root.selected_emulators")

    for index, row in enumerate(rows):
        scope = f"rows[{index}]"
        if not isinstance(row, dict):
            errors.append(f"expected {scope} to be dict, got {type(row).__name__}")
            continue
        for key, expected_type in ROW_REQUIRED_TYPES.items():
            _expect(row, key, expected_type, errors, scope)
        _validate_pack_intent(row, errors, scope)

        if isinstance(runner_commands, list) and index < len(runner_commands):
            if row.get("runner_command") != runner_commands[index]:
                errors.append(f"expected root.runner_commands[{index}] to mirror {scope}.runner_command")


def _validate_posture_groups(data: dict[str, Any], errors: list[str]) -> None:
    rows = data.get("rows")
    groups = data.get("posture_groups")
    if not isinstance(rows, list) or not isinstance(groups, list):
        return
    if not groups:
        errors.append("expected root.posture_groups to be non-empty")

    posture_to_rows: dict[str, list[dict[str, Any]]] = {}
    for row in rows:
        if isinstance(row, dict) and isinstance(row.get("posture"), str):
            posture_to_rows.setdefault(row["posture"], []).append(row)

    seen_postures: set[str] = set()
    for index, group in enumerate(groups):
        scope = f"posture_groups[{index}]"
        if not isinstance(group, dict):
            errors.append(f"expected {scope} to be dict, got {type(group).__name__}")
            continue
        for key, expected_type in POSTURE_GROUP_REQUIRED_TYPES.items():
            _expect(group, key, expected_type, errors, scope)
        if not isinstance(group.get("posture"), str):
            continue

        posture = group["posture"]
        seen_postures.add(posture)
        matching_rows = posture_to_rows.get(posture, [])
        if group.get("row_count") != len(matching_rows):
            errors.append(f"expected {scope}.row_count to match rows with posture {posture!r}")
        expected_emulators = sorted(
            {row["emulator"] for row in matching_rows if isinstance(row.get("emulator"), str)}
        )
        expected_labels = [row["run_label"] for row in matching_rows if isinstance(row.get("run_label"), str)]
        if isinstance(group.get("emulators"), list) and sorted(group["emulators"]) != expected_emulators:
            errors.append(f"expected {scope}.emulators to match selected rows")
        if isinstance(group.get("run_labels"), list) and group["run_labels"] != expected_labels:
            errors.append(f"expected {scope}.run_labels to match selected rows")

    missing_postures = sorted(set(posture_to_rows) - seen_postures)
    if missing_postures:
        errors.append("expected root.posture_groups to cover row postures: " + ", ".join(missing_postures))


def _validate_validation_commands(data: dict[str, Any], errors: list[str]) -> None:
    commands = data.get("validation_commands")
    if commands is None:
        return
    if not isinstance(commands, list):
        errors.append(f"expected root.validation_commands to be list, got {type(commands).__name__}")
        return

    for index, command in enumerate(commands):
        scope = f"validation_commands[{index}]"
        if not isinstance(command, dict):
            errors.append(f"expected {scope} to be dict, got {type(command).__name__}")
            continue
        for key, expected_type in VALIDATION_COMMAND_REQUIRED_TYPES.items():
            _expect(command, key, expected_type, errors, scope)
        if command.get("plan_only") is not True:
            errors.append(f"expected {scope}.plan_only to be true")
        if command.get("will_start_jobs") is not False:
            errors.append(f"expected {scope}.will_start_jobs to be false")
        if command.get("will_touch_emulators") is not False:
            errors.append(f"expected {scope}.will_touch_emulators to be false")
        if command.get("validates") not in ("summary.json", "plan summary"):
            errors.append(f"expected {scope}.validates to target summary.json or plan summary")
        if isinstance(command.get("command"), str) and "validate_" not in command["command"]:
            errors.append(f"expected {scope}.command to look like a validation command")


def _validate_migration_checklist_intent(data: dict[str, Any], errors: list[str]) -> None:
    intent = data.get("migration_checklist_intent")
    if not isinstance(intent, dict):
        return

    for key in MIRRORED_INTENT_KEYS:
        _expect(intent, key, type(data.get(key)) if key in data else object, errors, "migration_checklist_intent")
        if key in data and intent.get(key) != data.get(key):
            errors.append(f"expected migration_checklist_intent.{key} to mirror root.{key}")

    root_postures = sorted(
        group["posture"]
        for group in data.get("posture_groups", [])
        if isinstance(group, dict) and isinstance(group.get("posture"), str)
    )
    intent_postures = intent.get("posture_groups")
    if not isinstance(intent_postures, list):
        errors.append("missing migration_checklist_intent.posture_groups")
    elif sorted(intent_postures) != root_postures:
        errors.append("expected migration_checklist_intent.posture_groups to mirror root.posture_groups")


def validate_plan(path: Path) -> tuple[dict[str, Any] | None, list[str]]:
    if not path.exists():
        return None, [f"plan summary file not found: {path}"]

    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        return None, [f"failed to parse JSON: {exc}"]

    if not isinstance(data, dict):
        return None, ["top-level JSON document must be an object"]

    errors: list[str] = []
    for key, expected_type in ROOT_REQUIRED_TYPES.items():
        _expect(data, key, expected_type, errors, "root")

    if data.get("plan_kind") != PLAN_KIND:
        errors.append(f"expected root.plan_kind to be {PLAN_KIND!r}")
    for key, expected in ROOT_BOOL_REQUIREMENTS.items():
        _expect_bool(data, key, expected, errors, "root")
    if "dry_run" in data:
        errors.append("expected root.dry_run to be absent for PlanOnly summaries")

    _validate_pack_intent(data, errors, "root")
    _validate_rows(data, errors)
    _validate_posture_groups(data, errors)
    _validate_validation_commands(data, errors)
    _validate_migration_checklist_intent(data, errors)
    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("summary_path", help="Path to a PlanOnly summary.json or plan JSON file.")
    args = parser.parse_args(argv)

    data, errors = validate_plan(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_harness_matrix_plan: ok")
    print(f"rows: {data.get('row_count')}")
    print(f"postures: {len(data.get('posture_groups', []))}")
    print("will_touch_emulators: false")
    return 0


if __name__ == "__main__":
    sys.exit(main())
