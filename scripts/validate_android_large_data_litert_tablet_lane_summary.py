#!/usr/bin/env python3
"""Validate Android large-data LiteRT tablet lane summary JSON."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


CONFIRMATION_TOKEN = "RUN_EMULATOR_5554_LARGE_LITERT_DATA"
FIXED_FOUR_EMULATOR_MATRIX = "fixed_four_emulator_matrix"
EXPECTED_DEVICE = "emulator-5554"
EXPECTED_ROLE = "tablet_portrait"
EXPECTED_PROFILE = "large-litert-data"
EXPECTED_MATRIX = "large_litert_data_emulator_5554"

REQUIRED_TOP_LEVEL: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "dry_run": bool,
    "real_mode": bool,
    "real_mode_guard": dict,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "ui_acceptance_evidence": bool,
    "deploy_evidence": bool,
    "runtime_evidence": bool,
    "evidence_boundary": str,
    "primary_evidence": str,
    "comparison_baseline": str,
    "device": str,
    "role": str,
    "launch_profile": str,
    "partition_size_mb": int,
    "runtime_check": str,
    "selected_roles": list,
    "devices": list,
    "child_artifacts": dict,
    "child_status": dict,
    "planned_commands": dict,
    "child_summaries": dict,
}

REQUIRED_GUARD: dict[str, type | tuple[type, ...]] = {
    "required_switch": str,
    "required_confirmation": str,
    "confirmed": bool,
}


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
    allow_empty_string: bool = False,
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

    if isinstance(value, str) and not allow_empty_string and not value.strip():
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


def _expect_value(
    mapping: dict[str, Any],
    key: str,
    expected: Any,
    errors: list[str],
    scope: str,
) -> None:
    if key in mapping and mapping.get(key) != expected:
        errors.append(f"expected {scope}.{key} to be {expected!r}")


def _expect_string_contains(
    value: Any,
    expected: str,
    errors: list[str],
    scope: str,
) -> None:
    if isinstance(value, str) and expected not in value:
        errors.append(f"expected {scope} to include {expected!r}")


def _validate_lane_markers(data: dict[str, Any], errors: list[str]) -> None:
    _expect_value(data, "primary_evidence", FIXED_FOUR_EMULATOR_MATRIX, errors, "root")
    _expect_value(data, "comparison_baseline", FIXED_FOUR_EMULATOR_MATRIX, errors, "root")
    _expect_value(data, "device", EXPECTED_DEVICE, errors, "root")
    _expect_value(data, "role", EXPECTED_ROLE, errors, "root")
    _expect_value(data, "launch_profile", EXPECTED_PROFILE, errors, "root")

    if isinstance(data.get("partition_size_mb"), int) and data["partition_size_mb"] < 8192:
        errors.append("expected root.partition_size_mb to be at least 8192")
    if isinstance(data.get("evidence_boundary"), str) and "not UI acceptance" not in data["evidence_boundary"]:
        errors.append("expected root.evidence_boundary to state not UI acceptance")

    selected_roles = data.get("selected_roles")
    if isinstance(selected_roles, list) and EXPECTED_ROLE not in selected_roles:
        errors.append(f"expected root.selected_roles to include {EXPECTED_ROLE}")
    devices = data.get("devices")
    if isinstance(devices, list) and EXPECTED_DEVICE not in devices:
        errors.append(f"expected root.devices to include {EXPECTED_DEVICE}")


def _validate_evidence_posture(data: dict[str, Any], errors: list[str]) -> None:
    _expect_value(data, "non_acceptance_evidence", True, errors, "root")
    _expect_value(data, "acceptance_evidence", False, errors, "root")
    _expect_value(data, "ui_acceptance_evidence", False, errors, "root")

    status = data.get("status")
    dry_run = data.get("dry_run")
    real_mode = data.get("real_mode")

    if dry_run is True:
        _expect_value(data, "status", "dry_run_only", errors, "root")
        _expect_value(data, "real_mode", False, errors, "root")
        _expect_value(data, "deploy_evidence", False, errors, "root")
        _expect_value(data, "runtime_evidence", False, errors, "root")
        _expect_value(data, "start_emulator_requested", False, errors, "root")
    elif real_mode is True:
        if status not in {"pass", "failed"}:
            errors.append("expected root.status to be 'pass' or 'failed' in real mode")
    elif isinstance(dry_run, bool) and isinstance(real_mode, bool):
        errors.append("expected exactly one of root.dry_run or root.real_mode to be true")


def _validate_real_mode_guard(data: dict[str, Any], errors: list[str]) -> None:
    guard = data.get("real_mode_guard")
    _validate_required_fields(guard, REQUIRED_GUARD, errors, "real_mode_guard")
    if not isinstance(guard, dict):
        return

    _expect_value(guard, "required_switch", "-RealMode", errors, "real_mode_guard")
    _expect_value(guard, "required_confirmation", CONFIRMATION_TOKEN, errors, "real_mode_guard")
    if data.get("real_mode") is True:
        _expect_value(guard, "confirmed", True, errors, "real_mode_guard")
    if data.get("dry_run") is True:
        _expect_value(guard, "confirmed", False, errors, "real_mode_guard")


def _validate_child_artifacts(data: dict[str, Any], errors: list[str]) -> None:
    artifacts = data.get("child_artifacts")
    if not isinstance(artifacts, dict):
        return

    if not isinstance(artifacts.get("launch_profile_summary"), str) or not artifacts.get("launch_profile_summary", "").strip():
        errors.append("expected child_artifacts.launch_profile_summary to be a non-empty path")

    if data.get("real_mode") is True and data.get("deploy_evidence") is True:
        if not isinstance(artifacts.get("push_summary"), str) or not artifacts.get("push_summary", "").strip():
            errors.append("expected child_artifacts.push_summary when deploy_evidence is true")

    if data.get("runtime_check") == "model_store" and data.get("runtime_evidence") is True:
        if not isinstance(artifacts.get("instrumentation_summary"), str) or not artifacts.get("instrumentation_summary", "").strip():
            errors.append("expected child_artifacts.instrumentation_summary when runtime_evidence is true")


def _validate_child_status(data: dict[str, Any], errors: list[str]) -> None:
    child_status = data.get("child_status")
    if not isinstance(child_status, dict):
        return

    if child_status.get("launch_profile_preflight_exit_code") != 0:
        errors.append("expected child_status.launch_profile_preflight_exit_code to be 0")

    if data.get("dry_run") is True:
        for key in ("emulator_start_exit_code", "model_store_instrumentation_exit_code"):
            if child_status.get(key) is not None:
                errors.append(f"expected child_status.{key} to be null in dry run")
    if data.get("real_mode") is True and data.get("deploy_evidence") is True:
        if child_status.get("model_push_exit_code") != 0:
            errors.append("expected child_status.model_push_exit_code to be 0 when deploy_evidence is true")
    if data.get("runtime_check") == "model_store" and data.get("runtime_evidence") is True:
        if child_status.get("model_store_instrumentation_exit_code") != 0:
            errors.append(
                "expected child_status.model_store_instrumentation_exit_code to be 0 when runtime_evidence is true"
            )


def _validate_planned_commands(data: dict[str, Any], errors: list[str]) -> None:
    planned = data.get("planned_commands")
    if not isinstance(planned, dict):
        return

    _expect_string_contains(planned.get("launch_profile_preflight"), "-LaunchProfile large-litert-data", errors, "planned_commands.launch_profile_preflight")
    _expect_string_contains(planned.get("launch_profile_preflight"), "-Roles tablet_portrait", errors, "planned_commands.launch_profile_preflight")
    _expect_string_contains(planned.get("model_push"), "-Device emulator-5554", errors, "planned_commands.model_push")
    if data.get("dry_run") is True:
        _expect_string_contains(planned.get("model_push"), "-DryRun", errors, "planned_commands.model_push")
        if planned.get("model_store_instrumentation"):
            errors.append("expected planned_commands.model_store_instrumentation to be empty in dry run")
    if data.get("runtime_check") == "readiness_dry_run":
        _expect_string_contains(planned.get("litert_readiness"), EXPECTED_MATRIX, errors, "planned_commands.litert_readiness")


def _validate_launch_profile_child(child: Any, errors: list[str]) -> None:
    if child is None:
        return
    if not isinstance(child, dict):
        errors.append(f"expected child_summaries.launch_profile to be dict|null, got {type(child).__name__}")
        return

    metadata = child.get("profile_metadata")
    if isinstance(metadata, dict):
        _expect_value(metadata, "profile", EXPECTED_PROFILE, errors, "child_summaries.launch_profile.profile_metadata")
        _expect_value(metadata, "expected_role", EXPECTED_ROLE, errors, "child_summaries.launch_profile.profile_metadata")
        _expect_value(metadata, "expected_serial", EXPECTED_DEVICE, errors, "child_summaries.launch_profile.profile_metadata")
        _expect_value(metadata, "acceptance_evidence", False, errors, "child_summaries.launch_profile.profile_metadata")

    lanes = child.get("selected_lanes")
    if isinstance(lanes, list):
        matched = False
        for lane in lanes:
            if not isinstance(lane, dict):
                continue
            if lane.get("role") == EXPECTED_ROLE and lane.get("serial") == EXPECTED_DEVICE:
                matched = True
                if lane.get("orientation") != "portrait":
                    errors.append("expected child_summaries.launch_profile selected tablet lane orientation to be portrait")
        if not matched:
            errors.append("expected child_summaries.launch_profile.selected_lanes to include tablet_portrait emulator-5554")


def _validate_model_push_child(child: Any, data: dict[str, Any], errors: list[str]) -> None:
    if child is None:
        return
    if not isinstance(child, dict):
        errors.append(f"expected child_summaries.model_push to be dict|null, got {type(child).__name__}")
        return

    for key in ("device", "serial"):
        if key in child:
            _expect_value(child, key, EXPECTED_DEVICE, errors, "child_summaries.model_push")
    if "package_name" in child and child.get("package_name") != data.get("package_name"):
        errors.append("expected child_summaries.model_push.package_name to match root.package_name")
    if data.get("dry_run") is True and child.get("dry_run") is not True:
        errors.append("expected child_summaries.model_push.dry_run to be true in dry run")
    if "acceptance_evidence" in child and child.get("acceptance_evidence") is not False:
        errors.append("expected child_summaries.model_push.acceptance_evidence to be false")
    if "ui_acceptance_evidence" in child and child.get("ui_acceptance_evidence") is not False:
        errors.append("expected child_summaries.model_push.ui_acceptance_evidence to be false")


def _validate_readiness_child(child: Any, data: dict[str, Any], errors: list[str]) -> None:
    if child is None:
        return
    if not isinstance(child, dict):
        errors.append(f"expected child_summaries.litert_readiness to be dict|null, got {type(child).__name__}")
        return

    if child.get("backend", {}).get("name") != "litert":
        errors.append("expected child_summaries.litert_readiness.backend.name to be 'litert'")
    request = child.get("request")
    if isinstance(request, dict):
        _expect_value(request, "mode", "single_prompt_smoke", errors, "child_summaries.litert_readiness.request")
    if "package_name" in child and child.get("package_name") != data.get("package_name"):
        errors.append("expected child_summaries.litert_readiness.package_name to match root.package_name")
    if "acceptance_evidence" in child and child.get("acceptance_evidence") is not False:
        errors.append("expected child_summaries.litert_readiness.acceptance_evidence to be false")


def _validate_instrumentation_child(child: Any, errors: list[str]) -> None:
    if child is None:
        return
    if not isinstance(child, dict):
        errors.append(f"expected child_summaries.model_store_instrumentation to be dict|null, got {type(child).__name__}")
        return

    for key in ("device", "serial"):
        if key in child:
            _expect_value(child, key, EXPECTED_DEVICE, errors, "child_summaries.model_store_instrumentation")
    if "acceptance_evidence" in child and child.get("acceptance_evidence") is not False:
        errors.append("expected child_summaries.model_store_instrumentation.acceptance_evidence to be false")
    if "ui_acceptance_evidence" in child and child.get("ui_acceptance_evidence") is not False:
        errors.append("expected child_summaries.model_store_instrumentation.ui_acceptance_evidence to be false")


def _validate_child_summaries(data: dict[str, Any], errors: list[str]) -> None:
    child_summaries = data.get("child_summaries")
    if not isinstance(child_summaries, dict):
        return

    _validate_launch_profile_child(child_summaries.get("launch_profile"), errors)
    _validate_model_push_child(child_summaries.get("model_push"), data, errors)
    _validate_readiness_child(child_summaries.get("litert_readiness"), data, errors)
    _validate_instrumentation_child(child_summaries.get("model_store_instrumentation"), errors)


def validate_large_data_litert_tablet_lane_summary(
    path: Path,
) -> tuple[dict[str, Any] | None, list[str]]:
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
    _validate_evidence_posture(data, errors)
    _validate_real_mode_guard(data, errors)
    _validate_lane_markers(data, errors)
    _validate_child_artifacts(data, errors)
    _validate_child_status(data, errors)
    _validate_planned_commands(data, errors)
    _validate_child_summaries(data, errors)
    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "summary_path",
        help="Path to a run_android_large_data_litert_tablet_lane.ps1 summary JSON file.",
    )
    args = parser.parse_args(argv)

    data, errors = validate_large_data_litert_tablet_lane_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_large_data_litert_tablet_lane_summary: ok")
    print(f"status: {data.get('status')}")
    print(f"device: {data.get('device')}")
    print("evidence: deploy/runtime only, non_acceptance")
    return 0


if __name__ == "__main__":
    sys.exit(main())
