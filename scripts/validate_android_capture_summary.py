#!/usr/bin/env python3
"""Validate the canonical Android ADB capture summary contract."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


REQUIRED_TOP_LEVEL: dict[str, type | tuple[type, ...]] = {
    "schema_version": int,
    "serial": str,
    "role": str,
    "orientation": str,
    "apk_sha256": str,
    "platform_tools_version": str,
    "artifacts": dict,
    "package_data_posture": dict,
    "model_identity": dict,
    "installed_pack_metadata": dict,
    "evidence_posture": dict,
}

REQUIRED_ARTIFACTS: dict[str, type | tuple[type, ...]] = {
    "screenshot": dict,
    "ui_dump": dict,
    "logcat": dict,
}

REQUIRED_ARTIFACT_FIELDS: dict[str, type | tuple[type, ...]] = {
    "path": str,
    "sha256": str,
}

REQUIRED_PACKAGE_DATA_POSTURE: dict[str, type | tuple[type, ...]] = {
    "cleared_before_capture": bool,
    "restored_after_capture": bool,
    "description": str,
}

REQUIRED_MODEL_IDENTITY: dict[str, type | tuple[type, ...]] = {
    "name": str,
    "sha256": str,
}

REQUIRED_INSTALLED_PACK_METADATA: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "pack_format": str,
    "pack_version": int,
}

REQUIRED_EVIDENCE_POSTURE: dict[str, type | tuple[type, ...]] = {
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
}

OPTIONAL_MIGRATION_METADATA: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "capture_summary_schema_version": int,
    "android_layout_change": bool,
    "large_data_lane_change": bool,
    "reindex_required": bool,
}

VALID_ORIENTATIONS = {"portrait", "landscape"}
OPTIONAL_ARTIFACTS = {"screenrecord"}


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


def _validate_artifact(artifact: Any, errors: list[str], scope: str) -> None:
    _validate_required_fields(artifact, REQUIRED_ARTIFACT_FIELDS, errors, scope)


def _validate_artifacts(artifacts: Any, errors: list[str]) -> None:
    scope = "artifacts"
    _validate_required_fields(artifacts, REQUIRED_ARTIFACTS, errors, scope)
    if not isinstance(artifacts, dict):
        return

    for name in REQUIRED_ARTIFACTS:
        if name in artifacts:
            _validate_artifact(artifacts[name], errors, f"{scope}.{name}")

    for name in OPTIONAL_ARTIFACTS:
        if name in artifacts and artifacts[name] is not None:
            _validate_artifact(artifacts[name], errors, f"{scope}.{name}")


def _validate_evidence_posture(evidence_posture: Any, errors: list[str]) -> None:
    scope = "evidence_posture"
    _validate_required_fields(evidence_posture, REQUIRED_EVIDENCE_POSTURE, errors, scope)
    if not isinstance(evidence_posture, dict):
        return

    if evidence_posture.get("non_acceptance_evidence") is not True:
        errors.append("expected evidence_posture.non_acceptance_evidence to be true")
    if evidence_posture.get("acceptance_evidence") is not False:
        errors.append("expected evidence_posture.acceptance_evidence to be false")


def _validate_migration_metadata(data: dict[str, Any], errors: list[str]) -> None:
    if "migration_metadata" not in data:
        return

    scope = "migration_metadata"
    migration_metadata = data.get("migration_metadata")
    _validate_required_fields(migration_metadata, OPTIONAL_MIGRATION_METADATA, errors, scope)
    if not isinstance(migration_metadata, dict):
        return

    if migration_metadata.get("capture_summary_schema_version") != data.get("schema_version"):
        errors.append(
            "expected migration_metadata.capture_summary_schema_version to match root.schema_version"
        )


def validate_capture_summary(path: Path) -> tuple[dict[str, Any] | None, list[str]]:
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

    orientation = data.get("orientation")
    if isinstance(orientation, str) and orientation not in VALID_ORIENTATIONS:
        errors.append(
            "expected root.orientation to be one of "
            f"{', '.join(sorted(VALID_ORIENTATIONS))}"
        )

    _validate_artifacts(data.get("artifacts"), errors)
    _validate_required_fields(
        data.get("package_data_posture"),
        REQUIRED_PACKAGE_DATA_POSTURE,
        errors,
        "package_data_posture",
    )
    _validate_required_fields(
        data.get("model_identity"),
        REQUIRED_MODEL_IDENTITY,
        errors,
        "model_identity",
    )
    _validate_required_fields(
        data.get("installed_pack_metadata"),
        REQUIRED_INSTALLED_PACK_METADATA,
        errors,
        "installed_pack_metadata",
    )
    _validate_migration_metadata(data, errors)
    _validate_evidence_posture(data.get("evidence_posture"), errors)

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("summary_path", help="Path to an Android capture summary JSON file.")
    args = parser.parse_args(argv)

    data, errors = validate_capture_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_capture_summary: ok")
    print(f"serial: {data.get('serial')}")
    print(f"role: {data.get('role')}")
    print(f"orientation: {data.get('orientation')}")
    print("evidence: non_acceptance")
    return 0


if __name__ == "__main__":
    sys.exit(main())
