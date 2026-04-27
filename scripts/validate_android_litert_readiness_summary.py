#!/usr/bin/env python3
"""Validate Android LiteRT readiness matrix dry-run summary JSON."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


FIXED_FOUR_EMULATOR_MATRIX = "fixed_four_emulator_matrix"

REQUIRED_TOP_LEVEL: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "real_run_status": str,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "dry_run": bool,
    "primary_evidence": str,
    "comparison_baseline": str,
    "runtime_evidence": str,
    "package_name": str,
    "model": dict,
    "model_identity": dict,
    "app_private_target": dict,
    "data_free_space_posture": dict,
    "backend": dict,
    "request": dict,
    "logcat_extraction_plan": dict,
}

REQUIRED_MODEL: dict[str, type | tuple[type, ...]] = {
    "path": str,
    "exists": bool,
    "name": str,
    "bytes": (int, type(None)),
    "sha256": str,
}

REQUIRED_MODEL_IDENTITY: dict[str, type | tuple[type, ...]] = {
    "source": str,
    "path": str,
    "name": str,
    "bytes": (int, type(None)),
    "sha256": str,
    "exists": bool,
}

REQUIRED_APP_PRIVATE_TARGET: dict[str, type | tuple[type, ...]] = {
    "directory": str,
    "path": str,
}

REQUIRED_DATA_FREE_SPACE_POSTURE: dict[str, type | tuple[type, ...]] = {
    "adb_required_in_dry_run": bool,
    "adb_queried": bool,
    "check": str,
    "required_bytes": (int, type(None)),
    "required_rule": str,
    "skip_allowed_for_acceptance": bool,
}

REQUIRED_BACKEND: dict[str, type | tuple[type, ...]] = {
    "name": str,
    "package": str,
    "readiness_matrix": str,
    "real_run_status": str,
    "adb_required_in_dry_run": bool,
}

REQUIRED_REQUEST: dict[str, type | tuple[type, ...]] = {
    "mode": str,
    "prompt": str,
    "backend": str,
    "package": str,
    "real_run_status": str,
    "device_required_in_dry_run": bool,
    "expected_artifacts": list,
}

REQUIRED_LOGCAT_EXTRACTION_PLAN: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "real_run_status": str,
    "adb_required_in_dry_run": bool,
    "clear_before_run": bool,
    "capture_after_run": bool,
    "command": str,
    "artifact": str,
    "extraction_filters": list,
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
    allow_empty_strings: set[str] | None = None,
) -> None:
    if not isinstance(mapping, dict):
        errors.append(f"expected {scope} to be dict, got {type(mapping).__name__}")
        return

    allow_empty_strings = allow_empty_strings or set()
    for key, expected_type in required_fields.items():
        _expect(
            mapping,
            key,
            expected_type,
            errors,
            scope,
            allow_empty_string=key in allow_empty_strings,
        )


def _expect_value(
    mapping: dict[str, Any],
    key: str,
    expected: Any,
    errors: list[str],
    scope: str,
) -> None:
    if key in mapping and mapping.get(key) != expected:
        errors.append(f"expected {scope}.{key} to be {expected!r}")


def _validate_model_pair(data: dict[str, Any], errors: list[str]) -> None:
    model = data.get("model")
    model_identity = data.get("model_identity")
    _validate_required_fields(
        model,
        REQUIRED_MODEL,
        errors,
        "model",
        allow_empty_strings={"name", "sha256"},
    )
    _validate_required_fields(
        model_identity,
        REQUIRED_MODEL_IDENTITY,
        errors,
        "model_identity",
        allow_empty_strings={"name", "sha256"},
    )
    if not isinstance(model, dict) or not isinstance(model_identity, dict):
        return

    _expect_value(model_identity, "source", "dry_run_model_path", errors, "model_identity")
    for key in ("path", "name", "bytes", "sha256", "exists"):
        if model.get(key) != model_identity.get(key):
            errors.append(f"expected model_identity.{key} to match model.{key}")

    if model.get("exists") is True:
        if not isinstance(model.get("bytes"), int) or isinstance(model.get("bytes"), bool):
            errors.append("expected model.bytes to be int when model.exists is true")
        if not model.get("name"):
            errors.append("expected model.name to be non-empty when model.exists is true")
        sha256 = model.get("sha256")
        if not isinstance(sha256, str) or len(sha256) != 64:
            errors.append("expected model.sha256 to be a 64-character SHA-256 when model.exists is true")
    elif model.get("bytes") is not None:
        errors.append("expected model.bytes to be None when model.exists is false")


def _validate_data_free_space_posture(
    posture: Any,
    model: Any,
    errors: list[str],
) -> None:
    scope = "data_free_space_posture"
    _validate_required_fields(posture, REQUIRED_DATA_FREE_SPACE_POSTURE, errors, scope)
    if not isinstance(posture, dict):
        return

    _expect_value(posture, "adb_required_in_dry_run", False, errors, scope)
    _expect_value(posture, "adb_queried", False, errors, scope)
    _expect_value(posture, "skip_allowed_for_acceptance", False, errors, scope)
    if "real run" not in str(posture.get("check", "")).lower():
        errors.append("expected data_free_space_posture.check to describe a real run check")
    if "model_bytes * 2 + 67108864" not in str(posture.get("required_rule", "")):
        errors.append("expected data_free_space_posture.required_rule to include model_bytes * 2 + 67108864")

    if isinstance(model, dict) and model.get("exists") is True and isinstance(model.get("bytes"), int):
        expected_required_bytes = (model["bytes"] * 2) + 67108864
        if posture.get("required_bytes") != expected_required_bytes:
            errors.append(
                "expected data_free_space_posture.required_bytes to equal "
                f"model.bytes * 2 + 67108864 ({expected_required_bytes})"
            )
    elif posture.get("required_bytes") is not None:
        errors.append("expected data_free_space_posture.required_bytes to be None when model is absent")


def _validate_backend_and_request(data: dict[str, Any], errors: list[str]) -> None:
    backend = data.get("backend")
    request = data.get("request")
    package_name = data.get("package_name")
    _validate_required_fields(backend, REQUIRED_BACKEND, errors, "backend")
    _validate_required_fields(request, REQUIRED_REQUEST, errors, "request")
    if not isinstance(backend, dict) or not isinstance(request, dict):
        return

    _expect_value(backend, "real_run_status", "not_implemented", errors, "backend")
    _expect_value(backend, "adb_required_in_dry_run", False, errors, "backend")
    _expect_value(request, "real_run_status", "not_implemented", errors, "request")
    _expect_value(request, "device_required_in_dry_run", False, errors, "request")

    for scope, mapping in (("backend", backend), ("request", request)):
        if isinstance(package_name, str) and mapping.get("package") != package_name:
            errors.append(f"expected {scope}.package to match root.package_name")

    if backend.get("name") != request.get("backend"):
        errors.append("expected request.backend to match backend.name")
    if request.get("expected_artifacts") and any(
        not isinstance(item, str) or not item.strip() for item in request["expected_artifacts"]
    ):
        errors.append("expected request.expected_artifacts to contain only non-empty strings")


def _validate_logcat_extraction_plan(plan: Any, errors: list[str]) -> None:
    scope = "logcat_extraction_plan"
    _validate_required_fields(plan, REQUIRED_LOGCAT_EXTRACTION_PLAN, errors, scope)
    if not isinstance(plan, dict):
        return

    _expect_value(plan, "status", "planned_for_real_run", errors, scope)
    _expect_value(plan, "real_run_status", "not_implemented", errors, scope)
    _expect_value(plan, "adb_required_in_dry_run", False, errors, scope)
    _expect_value(plan, "clear_before_run", True, errors, scope)
    _expect_value(plan, "capture_after_run", True, errors, scope)
    if "logcat" not in str(plan.get("command", "")).lower():
        errors.append("expected logcat_extraction_plan.command to include logcat")
    if plan.get("artifact") != "logcat_litert_readiness_<serial>.txt":
        errors.append("expected logcat_extraction_plan.artifact to be 'logcat_litert_readiness_<serial>.txt'")
    filters = plan.get("extraction_filters")
    if isinstance(filters, list):
        for expected_filter in ("Senku:D", "LiteRT:D", "AndroidRuntime:E", "*:S"):
            if expected_filter not in filters:
                errors.append(f"missing logcat_extraction_plan.extraction_filters {expected_filter}")


def validate_litert_readiness_summary(path: Path) -> tuple[dict[str, Any] | None, list[str]]:
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

    _expect_value(data, "status", "dry_run_only", errors, "root")
    _expect_value(data, "dry_run", True, errors, "root")
    _expect_value(data, "real_run_status", "not_implemented", errors, "root")
    _expect_value(data, "runtime_evidence", "none_dry_run_only", errors, "root")
    _expect_value(data, "non_acceptance_evidence", True, errors, "root")
    _expect_value(data, "acceptance_evidence", False, errors, "root")
    _expect_value(data, "primary_evidence", FIXED_FOUR_EMULATOR_MATRIX, errors, "root")
    _expect_value(data, "comparison_baseline", FIXED_FOUR_EMULATOR_MATRIX, errors, "root")

    _validate_model_pair(data, errors)
    _validate_required_fields(
        data.get("app_private_target"),
        REQUIRED_APP_PRIVATE_TARGET,
        errors,
        "app_private_target",
    )
    _validate_data_free_space_posture(data.get("data_free_space_posture"), data.get("model"), errors)
    _validate_backend_and_request(data, errors)
    _validate_logcat_extraction_plan(data.get("logcat_extraction_plan"), errors)

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "summary_path",
        help="Path to a run_android_litert_readiness_matrix.ps1 summary JSON file.",
    )
    args = parser.parse_args(argv)

    data, errors = validate_litert_readiness_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_litert_readiness_summary: ok")
    print(f"status: {data.get('status')}")
    print(f"runtime_evidence: {data.get('runtime_evidence')}")
    print("evidence: non_acceptance")
    return 0


if __name__ == "__main__":
    sys.exit(main())
