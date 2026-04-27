#!/usr/bin/env python3
"""Validate run_android_fts_fallback_matrix.ps1 summary.json evidence."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


RUNTIME_EVIDENCE = "fts4_fallback"
VALID_RESULT_STATUSES = {"passed", "failed"}
VALID_DEVICE_LOCK_POSTURES = {
    "not_acquired_dry_run",
    "required_per_device",
    "skipped_by_flag",
}
PLATFORM_TOOLS_VERSION_RE = re.compile(r"^\d+\.\d+\.\d+(?:-\d+)?$")

REQUIRED_TOP_LEVEL: dict[str, type | tuple[type, ...]] = {
    "runtime_evidence": str,
    "not_fts5_runtime_proof": bool,
    "failed_devices": list,
    "failed_device_results": list,
    "dry_run": bool,
    "device_lock_posture": str,
    "device_lock_used": bool,
    "adb_path": str,
    "host_adb_platform_tools_version": str,
    "device_count": int,
    "passed_count": int,
    "devices": list,
}

REQUIRED_RESULT_FIELDS: dict[str, type | tuple[type, ...]] = {
    "device": str,
    "status": str,
    "passed": bool,
    "runtime_evidence": str,
    "not_fts5_runtime_proof": bool,
}

REQUIRED_FAILED_RESULT_FIELDS: dict[str, type | tuple[type, ...]] = {
    "device": str,
    "exit_code": int,
    "command": str,
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
    if expected_type is int:
        type_ok = isinstance(value, int) and not isinstance(value, bool)
    else:
        type_ok = isinstance(value, expected_type)
    if not type_ok:
        if isinstance(expected_type, tuple):
            type_names = "|".join(item.__name__ for item in expected_type)
        else:
            type_names = expected_type.__name__
        errors.append(f"expected {scope}.{key} to be {type_names}, got {type(value).__name__}")
        return

    if isinstance(value, str) and not value.strip():
        errors.append(f"expected {scope}.{key} to be non-empty")


def _validate_non_negative_count(mapping: dict[str, Any], key: str, errors: list[str], scope: str) -> None:
    value = mapping.get(key)
    if isinstance(value, int) and not isinstance(value, bool) and value < 0:
        errors.append(f"expected {scope}.{key} to be non-negative")


def _validate_str_list(value: Any, errors: list[str], scope: str) -> None:
    if not isinstance(value, list):
        return
    for index, item in enumerate(value):
        if not isinstance(item, str) or not item.strip():
            errors.append(f"expected {scope}[{index}] to be non-empty str")


def _validate_failed_device_result(item: Any, index: int, errors: list[str]) -> None:
    scope = f"root.failed_device_results[{index}]"
    if not isinstance(item, dict):
        errors.append(f"expected {scope} to be dict, got {type(item).__name__}")
        return

    for key, expected_type in REQUIRED_FAILED_RESULT_FIELDS.items():
        _expect(item, key, expected_type, errors, scope)


def _validate_result(item: Any, index: int, errors: list[str]) -> None:
    scope = f"root.results[{index}]"
    if not isinstance(item, dict):
        errors.append(f"expected {scope} to be dict, got {type(item).__name__}")
        return

    for key, expected_type in REQUIRED_RESULT_FIELDS.items():
        _expect(item, key, expected_type, errors, scope)

    status = item.get("status")
    passed = item.get("passed")
    if isinstance(status, str) and status not in VALID_RESULT_STATUSES:
        errors.append(f"expected {scope}.status to be one of failed, passed")
    if status == "passed" and passed is not True:
        errors.append(f"expected {scope}.passed to be true when status is passed")
    if status == "failed" and passed is not False:
        errors.append(f"expected {scope}.passed to be false when status is failed")
    if item.get("runtime_evidence") != RUNTIME_EVIDENCE:
        errors.append(f"expected {scope}.runtime_evidence to be {RUNTIME_EVIDENCE!r}")
    if item.get("not_fts5_runtime_proof") is not True:
        errors.append(f"expected {scope}.not_fts5_runtime_proof to be true")
    if "fts5_runtime_proof" in item and item.get("fts5_runtime_proof") is not False:
        errors.append(f"expected {scope}.fts5_runtime_proof to be false")


def _validate_host_and_lock_shape(data: dict[str, Any], errors: list[str]) -> None:
    posture = data.get("device_lock_posture")
    if isinstance(posture, str) and posture and posture not in VALID_DEVICE_LOCK_POSTURES:
        expected = ", ".join(sorted(VALID_DEVICE_LOCK_POSTURES))
        errors.append(f"expected root.device_lock_posture to be one of {expected}")

    version = data.get("host_adb_platform_tools_version")
    if isinstance(version, str) and version and version != "dry_run":
        if PLATFORM_TOOLS_VERSION_RE.fullmatch(version) is None:
            errors.append(
                "expected root.host_adb_platform_tools_version to be 'dry_run' "
                "or a platform-tools version like 36.0.0-13206524"
            )


def validate_fts_fallback_summary(path: Path) -> tuple[dict[str, Any] | None, list[str]]:
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

    if data.get("runtime_evidence") != RUNTIME_EVIDENCE:
        errors.append(f"expected root.runtime_evidence to be {RUNTIME_EVIDENCE!r}")
    if data.get("not_fts5_runtime_proof") is not True:
        errors.append("expected root.not_fts5_runtime_proof to be true")
    if "fts5_runtime_proof" in data and data.get("fts5_runtime_proof") is not False:
        errors.append("expected root.fts5_runtime_proof to be false")
    _validate_host_and_lock_shape(data, errors)

    _validate_non_negative_count(data, "device_count", errors, "root")
    _validate_non_negative_count(data, "passed_count", errors, "root")
    _validate_str_list(data.get("devices"), errors, "root.devices")
    _validate_str_list(data.get("failed_devices"), errors, "root.failed_devices")

    devices = data.get("devices")
    failed_devices = data.get("failed_devices")
    if isinstance(devices, list):
        if not devices:
            errors.append("expected root.devices to be non-empty")
        if isinstance(data.get("device_count"), int) and not isinstance(data.get("device_count"), bool):
            if data["device_count"] != len(devices):
                errors.append("expected root.device_count to match len(root.devices)")
    if isinstance(devices, list) and isinstance(failed_devices, list):
        unknown_failed_devices = sorted(set(failed_devices) - set(devices))
        if unknown_failed_devices:
            errors.append(
                "expected root.failed_devices to be a subset of root.devices, "
                f"unknown: {', '.join(unknown_failed_devices)}"
            )
        expected_failed_count = len(failed_devices)
        passed_count = data.get("passed_count")
        if isinstance(passed_count, int) and not isinstance(passed_count, bool):
            if passed_count + expected_failed_count != len(devices):
                errors.append(
                    "expected root.passed_count plus len(root.failed_devices) "
                    "to match len(root.devices)"
                )

    failed_results = data.get("failed_device_results")
    if isinstance(failed_results, list):
        for index, item in enumerate(failed_results):
            _validate_failed_device_result(item, index, errors)
        if isinstance(failed_devices, list):
            failed_result_devices = [
                item.get("device") for item in failed_results if isinstance(item, dict)
            ]
            if sorted(failed_result_devices) != sorted(failed_devices):
                errors.append("expected root.failed_device_results devices to match root.failed_devices")

    results = data.get("results")
    if "results" in data:
        if not isinstance(results, list):
            errors.append(f"expected root.results to be list, got {type(results).__name__}")
        else:
            for index, item in enumerate(results):
                _validate_result(item, index, errors)
            if isinstance(devices, list):
                result_devices = [item.get("device") for item in results if isinstance(item, dict)]
                if sorted(result_devices) != sorted(devices):
                    errors.append("expected root.results devices to match root.devices")
            failed_from_results = [
                item.get("device")
                for item in results
                if isinstance(item, dict) and item.get("status") == "failed"
            ]
            if isinstance(failed_devices, list) and sorted(failed_from_results) != sorted(failed_devices):
                errors.append("expected failed root.results devices to match root.failed_devices")

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("summary_path", help="Path to run_android_fts_fallback_matrix.ps1 summary.json.")
    args = parser.parse_args(argv)

    data, errors = validate_fts_fallback_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_fts_fallback_summary: ok")
    print(f"devices: {data.get('device_count')}")
    print(f"passed_count: {data.get('passed_count')}")
    print(f"failed_count: {len(data.get('failed_devices', []))}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
