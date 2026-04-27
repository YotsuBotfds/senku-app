#!/usr/bin/env python3
"""Validate run_android_managed_device_smoke.ps1 -DryRun summary.json."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


EXPECTED_GRADLE_PROPERTY = "-Psenku.enableManagedDevices=true"
EXPECTED_TASK_GROUP = "senkuManagedSmoke"
EXPECTED_TASK_NAME = ":app:senkuManagedSmoke"
EXPECTED_DEVICES = ["senkuPhoneApi30", "senkuTabletApi30"]
EXPECTED_ARTIFACT_ROOTS = [
    "android-app/app/build/outputs/androidTest-results/managedDevice/senkuPhoneApi30",
    "android-app/app/build/outputs/androidTest-results/managedDevice/senkuTabletApi30",
    "android-app/app/build/reports/androidTests/managedDevice/senkuPhoneApi30",
    "android-app/app/build/reports/androidTests/managedDevice/senkuTabletApi30",
]
EXPECTED_TEST_TARGET = ":app:senkuManagedSmoke"
EXPECTED_TASK_INVENTORY_COMMAND = (
    ".\\gradlew.bat :app:tasks --all -Psenku.enableManagedDevices=true --console=plain"
)
EXPECTED_GRADLE_TASK_NAMES = [
    ":app:senkuPhoneApi30DebugAndroidTest",
    ":app:senkuTabletApi30DebugAndroidTest",
    ":app:senkuManagedSmokeGroupDebugAndroidTest",
]
EXPECTED_PLANNED_COMMAND = (
    ".\\gradlew.bat :app:senkuManagedSmoke "
    "-Psenku.enableManagedDevices=true --console=plain"
)
EXPECTED_GRADLE_PROJECT_DIR = "android-app"
EXPECTED_GRADLE_WRAPPER = "android-app/gradlew.bat"
EXPECTED_BASELINE = "fixed_four_emulator_matrix"
EXPECTED_SCAFFOLD_DEVICES = [
    {"name": "senkuPhoneApi30", "api_level": 30, "image_source": "aosp"},
    {"name": "senkuTabletApi30", "api_level": 30, "image_source": "aosp"},
]

REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "dry_run": bool,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "gradle_property": str,
    "task_group": str,
    "task_name": str,
    "expected_devices": list,
    "expected_artifact_roots": list,
    "expected_test_target": str,
    "planned_task_inventory_command": str,
    "expected_gradle_task_names": list,
    "observed_gradle_task_names": list,
    "observed_expected_gradle_task_names": list,
    "planned_result_evidence_schema": dict,
    "task_inventory_source": str,
    "task_inventory_probe_ran": bool,
    "task_inventory": (dict, type(None)),
    "comparison_baseline": str,
    "planned_command": str,
    "gradle_project_dir": str,
    "gradle_wrapper": str,
    "would_launch_emulators": bool,
    "managed_devices_launched": bool,
    "primary_evidence": str,
    "stop_line": str,
    "managed_device_scaffold": dict,
}


def _expect_type(
    data: dict[str, Any],
    key: str,
    expected_type: type | tuple[type, ...],
    errors: list[str],
    scope: str = "root",
) -> None:
    if key not in data:
        errors.append(f"missing {scope}.{key}")
        return

    value = data[key]
    if not isinstance(value, expected_type):
        if isinstance(expected_type, tuple):
            type_name = "|".join(item.__name__ for item in expected_type)
        else:
            type_name = expected_type.__name__
        errors.append(f"expected {scope}.{key} to be {type_name}, got {type(value).__name__}")
        return

    if isinstance(value, str) and not value.strip():
        errors.append(f"expected {scope}.{key} to be non-empty")


def _expect_equal(
    data: dict[str, Any],
    key: str,
    expected: Any,
    errors: list[str],
    scope: str = "root",
) -> None:
    if key in data and data[key] != expected:
        errors.append(f"expected {scope}.{key} to be {expected!r}, got {data[key]!r}")


def _validate_scaffold(scaffold: Any, errors: list[str]) -> None:
    if not isinstance(scaffold, dict):
        return

    required_scaffold_types: dict[str, type] = {
        "configured_device_names": list,
        "configured_device_api_levels": list,
        "configured_device_image_sources": list,
        "configured_devices": list,
        "smoke_group": str,
        "smoke_group_devices": list,
        "expected_gradle_task_names": list,
        "expected_artifact_roots": list,
    }
    for key, expected_type in required_scaffold_types.items():
        _expect_type(scaffold, key, expected_type, errors, scope="root.managed_device_scaffold")

    _expect_equal(
        scaffold,
        "configured_device_names",
        EXPECTED_DEVICES,
        errors,
        scope="root.managed_device_scaffold",
    )
    _expect_equal(
        scaffold,
        "configured_device_api_levels",
        [30, 30],
        errors,
        scope="root.managed_device_scaffold",
    )
    _expect_equal(
        scaffold,
        "configured_device_image_sources",
        ["aosp", "aosp"],
        errors,
        scope="root.managed_device_scaffold",
    )
    _expect_equal(
        scaffold,
        "configured_devices",
        EXPECTED_SCAFFOLD_DEVICES,
        errors,
        scope="root.managed_device_scaffold",
    )
    _expect_equal(
        scaffold,
        "smoke_group",
        EXPECTED_TASK_GROUP,
        errors,
        scope="root.managed_device_scaffold",
    )
    _expect_equal(
        scaffold,
        "smoke_group_devices",
        EXPECTED_DEVICES,
        errors,
        scope="root.managed_device_scaffold",
    )
    _expect_equal(
        scaffold,
        "expected_gradle_task_names",
        EXPECTED_GRADLE_TASK_NAMES,
        errors,
        scope="root.managed_device_scaffold",
    )
    _expect_equal(
        scaffold,
        "expected_artifact_roots",
        EXPECTED_ARTIFACT_ROOTS,
        errors,
        scope="root.managed_device_scaffold",
    )


def _validate_task_inventory(data: dict[str, Any], errors: list[str]) -> None:
    observed = data.get("observed_gradle_task_names")
    observed_expected = data.get("observed_expected_gradle_task_names")

    if isinstance(observed_expected, list):
        unexpected = [name for name in observed_expected if name not in EXPECTED_GRADLE_TASK_NAMES]
        if unexpected:
            errors.append(
                "expected root.observed_expected_gradle_task_names to contain only expected "
                f"managed-device tasks, got unexpected entries {unexpected!r}"
            )
        missing_observed = [
            name for name in observed_expected if isinstance(observed, list) and name not in observed
        ]
        if missing_observed:
            errors.append(
                "expected root.observed_expected_gradle_task_names entries to also appear in "
                f"root.observed_gradle_task_names, missing {missing_observed!r}"
            )

    if isinstance(observed, list) and all(name in observed for name in EXPECTED_GRADLE_TASK_NAMES):
        _expect_equal(data, "observed_expected_gradle_task_names", EXPECTED_GRADLE_TASK_NAMES, errors)

    inventory = data.get("task_inventory")
    if inventory is None:
        _expect_equal(data, "task_inventory_source", "not_collected", errors)
        _expect_equal(data, "task_inventory_probe_ran", False, errors)
        return

    if not isinstance(inventory, dict):
        return

    for key in (
        "source",
        "gradle_invoked",
        "command",
        "observed_gradle_task_names",
        "observed_expected_gradle_task_names",
    ):
        if key not in inventory:
            errors.append(f"missing root.task_inventory.{key}")

    if inventory.get("source") != data.get("task_inventory_source"):
        errors.append(
            "expected root.task_inventory.source to match root.task_inventory_source, "
            f"got {inventory.get('source')!r} and {data.get('task_inventory_source')!r}"
        )
    if inventory.get("gradle_invoked") != data.get("task_inventory_probe_ran"):
        errors.append(
            "expected root.task_inventory.gradle_invoked to match "
            f"root.task_inventory_probe_ran, got {inventory.get('gradle_invoked')!r} "
            f"and {data.get('task_inventory_probe_ran')!r}"
        )
    if inventory.get("command") != EXPECTED_TASK_INVENTORY_COMMAND:
        errors.append(
            "expected root.task_inventory.command to be "
            f"{EXPECTED_TASK_INVENTORY_COMMAND!r}, got {inventory.get('command')!r}"
        )
    if inventory.get("observed_gradle_task_names") != observed:
        errors.append(
            "expected root.task_inventory.observed_gradle_task_names to mirror "
            "root.observed_gradle_task_names"
        )
    if inventory.get("observed_expected_gradle_task_names") != observed_expected:
        errors.append(
            "expected root.task_inventory.observed_expected_gradle_task_names to mirror "
            "root.observed_expected_gradle_task_names"
        )


def _validate_planned_result_evidence_schema(schema: Any, errors: list[str]) -> None:
    if not isinstance(schema, dict):
        return

    required_schema_types: dict[str, type] = {
        "schema_version": int,
        "status": str,
        "description": str,
        "non_acceptance_evidence": bool,
        "acceptance_evidence": bool,
        "result_fields": dict,
        "evidence_fields": dict,
    }
    for key, expected_type in required_schema_types.items():
        _expect_type(schema, key, expected_type, errors, scope="root.planned_result_evidence_schema")

    _expect_equal(schema, "schema_version", 1, errors, scope="root.planned_result_evidence_schema")
    _expect_equal(
        schema,
        "status",
        "planned_not_collected",
        errors,
        scope="root.planned_result_evidence_schema",
    )
    _expect_equal(
        schema,
        "non_acceptance_evidence",
        True,
        errors,
        scope="root.planned_result_evidence_schema",
    )
    _expect_equal(
        schema,
        "acceptance_evidence",
        False,
        errors,
        scope="root.planned_result_evidence_schema",
    )

    result_fields = schema.get("result_fields")
    if isinstance(result_fields, dict):
        nullable_count_fields = (
            "total_test_count",
            "passed_test_count",
            "failed_test_count",
            "skipped_test_count",
            "failure_count",
        )
        for key in nullable_count_fields:
            if key not in result_fields:
                errors.append(f"missing root.planned_result_evidence_schema.result_fields.{key}")
            elif result_fields[key] is not None:
                errors.append(
                    "expected root.planned_result_evidence_schema.result_fields."
                    f"{key} to be None until real managed-device smoke runs, got {result_fields[key]!r}"
                )

        _expect_type(
            result_fields,
            "failures",
            list,
            errors,
            scope="root.planned_result_evidence_schema.result_fields",
        )
        _expect_type(
            result_fields,
            "per_device_results",
            list,
            errors,
            scope="root.planned_result_evidence_schema.result_fields",
        )
        if isinstance(result_fields.get("failures"), list) and result_fields["failures"]:
            errors.append(
                "expected root.planned_result_evidence_schema.result_fields.failures "
                "to stay empty until real managed-device smoke runs"
            )

        per_device_results = result_fields.get("per_device_results")
        if isinstance(per_device_results, list):
            device_names = [item.get("device_name") for item in per_device_results if isinstance(item, dict)]
            if device_names != EXPECTED_DEVICES:
                errors.append(
                    "expected root.planned_result_evidence_schema.result_fields.per_device_results "
                    f"to cover {EXPECTED_DEVICES!r}, got {device_names!r}"
                )
            for index, item in enumerate(per_device_results):
                scope = f"root.planned_result_evidence_schema.result_fields.per_device_results[{index}]"
                if not isinstance(item, dict):
                    errors.append(f"expected {scope} to be dict, got {type(item).__name__}")
                    continue
                _expect_type(item, "device_name", str, errors, scope=scope)
                _expect_type(item, "artifact_paths", list, errors, scope=scope)
                for key in ("total_test_count", "failed_test_count"):
                    if key not in item:
                        errors.append(f"missing {scope}.{key}")
                    elif item[key] is not None:
                        errors.append(f"expected {scope}.{key} to be None until real managed-device smoke runs")
                if isinstance(item.get("artifact_paths"), list) and item["artifact_paths"]:
                    errors.append(f"expected {scope}.artifact_paths to stay empty until real managed-device smoke runs")

    evidence_fields = schema.get("evidence_fields")
    if isinstance(evidence_fields, dict):
        _expect_equal(
            evidence_fields,
            "artifact_roots",
            EXPECTED_ARTIFACT_ROOTS,
            errors,
            scope="root.planned_result_evidence_schema.evidence_fields",
        )
        for key in ("junit_xml_paths", "html_report_paths", "logcat_paths", "screenshot_paths"):
            _expect_type(
                evidence_fields,
                key,
                list,
                errors,
                scope="root.planned_result_evidence_schema.evidence_fields",
            )
            if isinstance(evidence_fields.get(key), list) and evidence_fields[key]:
                errors.append(
                    "expected root.planned_result_evidence_schema.evidence_fields."
                    f"{key} to stay empty until real managed-device smoke runs"
                )
        for key in ("stdout_path", "stderr_path"):
            if key not in evidence_fields:
                errors.append(f"missing root.planned_result_evidence_schema.evidence_fields.{key}")
            elif evidence_fields[key] is not None:
                errors.append(
                    "expected root.planned_result_evidence_schema.evidence_fields."
                    f"{key} to be None until real managed-device smoke runs"
                )


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
    for key, expected_type in REQUIRED_TYPES.items():
        _expect_type(data, key, expected_type, errors)

    _expect_equal(data, "status", "dry_run_only", errors)
    _expect_equal(data, "dry_run", True, errors)
    _expect_equal(data, "non_acceptance_evidence", True, errors)
    _expect_equal(data, "acceptance_evidence", False, errors)
    _expect_equal(data, "gradle_property", EXPECTED_GRADLE_PROPERTY, errors)
    _expect_equal(data, "task_group", EXPECTED_TASK_GROUP, errors)
    _expect_equal(data, "task_name", EXPECTED_TASK_NAME, errors)
    _expect_equal(data, "expected_devices", EXPECTED_DEVICES, errors)
    _expect_equal(data, "expected_artifact_roots", EXPECTED_ARTIFACT_ROOTS, errors)
    _expect_equal(data, "expected_test_target", EXPECTED_TEST_TARGET, errors)
    _expect_equal(data, "planned_task_inventory_command", EXPECTED_TASK_INVENTORY_COMMAND, errors)
    _expect_equal(data, "expected_gradle_task_names", EXPECTED_GRADLE_TASK_NAMES, errors)
    _expect_equal(data, "planned_command", EXPECTED_PLANNED_COMMAND, errors)
    _expect_equal(data, "gradle_project_dir", EXPECTED_GRADLE_PROJECT_DIR, errors)
    _expect_equal(data, "gradle_wrapper", EXPECTED_GRADLE_WRAPPER, errors)
    _expect_equal(data, "would_launch_emulators", False, errors)
    _expect_equal(data, "managed_devices_launched", False, errors)
    _expect_equal(data, "comparison_baseline", EXPECTED_BASELINE, errors)
    _expect_equal(data, "primary_evidence", EXPECTED_BASELINE, errors)

    planned_command = data.get("planned_command")
    if isinstance(planned_command, str) and EXPECTED_GRADLE_PROPERTY not in planned_command:
        errors.append("expected root.planned_command to include senku.enableManagedDevices")
    if isinstance(planned_command, str) and EXPECTED_TASK_NAME not in planned_command:
        errors.append("expected root.planned_command to target :app:senkuManagedSmoke")

    stop_line = data.get("stop_line")
    if isinstance(stop_line, str):
        if "fixed four-emulator evidence remains primary" not in stop_line:
            errors.append("expected root.stop_line to preserve fixed four-emulator baseline posture")
        if "non-acceptance evidence only" not in stop_line:
            errors.append("expected root.stop_line to preserve non-acceptance posture")

    _validate_scaffold(data.get("managed_device_scaffold"), errors)
    _validate_task_inventory(data, errors)
    _validate_planned_result_evidence_schema(data.get("planned_result_evidence_schema"), errors)
    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "summary_path",
        help="Path to run_android_managed_device_smoke.ps1 -DryRun summary.json.",
    )
    args = parser.parse_args(argv)

    data, errors = validate_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_managed_device_smoke_summary: ok")
    print(f"status: {data.get('status')}")
    print("evidence: non_acceptance")
    print(f"primary_evidence: {data.get('primary_evidence')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
