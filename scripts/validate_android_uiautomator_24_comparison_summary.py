#!/usr/bin/env python3
"""Validate run_android_uiautomator_24_comparison.ps1 dry-run summary.json."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


EXPECTED_KIND = "android_uiautomator_24_comparison"
EXPECTED_REFERENCE_VERSION = "2.3.0"
EXPECTED_CANDIDATE_VERSION = "2.4.0-beta02"
EXPECTED_DEPENDENCY = "androidx.test.uiautomator:uiautomator:2.4.0-beta02"
EXPECTED_PROPERTY = "-Psenku.uiautomatorComparisonVersion=2.4.0-beta02"
EXPECTED_TEST_CLASS = "com.senku.mobile.ScriptedPromptHarnessContractTest"
EXPECTED_GRADLE_TASK = ":app:connectedDebugAndroidTest"
EXPECTED_BASELINE = "fixed_four_emulator_matrix"
EXPECTED_GRADLE_PROJECT_DIR = "android-app"
EXPECTED_GRADLE_WRAPPER = "android-app/gradlew.bat"
EXPECTED_APP_BUILD_GRADLE = "android-app/app/build.gradle"
EXPECTED_RESULT_ROOTS = [
    "android-app/app/build/outputs/androidTest-results/connected/debug",
    "android-app/app/build/reports/androidTests/connected/debug",
]

REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "summary_kind": str,
    "status": str,
    "dry_run": bool,
    "plan_only": bool,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "reference_version": str,
    "observed_current_version": (str, type(None)),
    "candidate_version": str,
    "candidate_dependency": str,
    "dependency_override_property": str,
    "dependency_change_posture": str,
    "gradle_task": str,
    "test_class": str,
    "class_filter_property": str,
    "planned_command": str,
    "gradle_project_dir": str,
    "gradle_wrapper": str,
    "app_build_gradle": str,
    "expected_result_roots": list,
    "comparison_lane": str,
    "comparison_baseline": str,
    "primary_evidence": str,
    "would_modify_gradle_files": bool,
    "would_resolve_candidate_dependency": bool,
    "would_call_am_instrument": bool,
    "would_start_emulators": bool,
    "would_launch_connected_instrumentation": bool,
    "devices_touched": bool,
    "emulator_required": bool,
    "stop_line": str,
}


def _expect_type(
    data: dict[str, Any],
    key: str,
    expected_type: type | tuple[type, ...],
    errors: list[str],
) -> None:
    if key not in data:
        errors.append(f"missing root.{key}")
        return

    value = data[key]
    if not isinstance(value, expected_type):
        if isinstance(expected_type, tuple):
            type_name = "|".join(item.__name__ for item in expected_type)
        else:
            type_name = expected_type.__name__
        errors.append(f"expected root.{key} to be {type_name}, got {type(value).__name__}")
        return

    if isinstance(value, str) and not value.strip():
        errors.append(f"expected root.{key} to be non-empty")


def _expect_equal(data: dict[str, Any], key: str, expected: Any, errors: list[str]) -> None:
    if key in data and data[key] != expected:
        errors.append(f"expected root.{key} to be {expected!r}, got {data[key]!r}")


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

    _expect_equal(data, "summary_kind", EXPECTED_KIND, errors)
    _expect_equal(data, "status", "dry_run_only", errors)
    _expect_equal(data, "dry_run", True, errors)
    _expect_equal(data, "plan_only", True, errors)
    _expect_equal(data, "non_acceptance_evidence", True, errors)
    _expect_equal(data, "acceptance_evidence", False, errors)
    _expect_equal(data, "reference_version", EXPECTED_REFERENCE_VERSION, errors)
    _expect_equal(data, "candidate_version", EXPECTED_CANDIDATE_VERSION, errors)
    _expect_equal(data, "candidate_dependency", EXPECTED_DEPENDENCY, errors)
    _expect_equal(data, "dependency_override_property", EXPECTED_PROPERTY, errors)
    _expect_equal(data, "dependency_change_posture", "no_global_bump_dry_run_property_only", errors)
    _expect_equal(data, "gradle_task", EXPECTED_GRADLE_TASK, errors)
    _expect_equal(data, "test_class", EXPECTED_TEST_CLASS, errors)
    _expect_equal(data, "gradle_project_dir", EXPECTED_GRADLE_PROJECT_DIR, errors)
    _expect_equal(data, "gradle_wrapper", EXPECTED_GRADLE_WRAPPER, errors)
    _expect_equal(data, "app_build_gradle", EXPECTED_APP_BUILD_GRADLE, errors)
    _expect_equal(data, "expected_result_roots", EXPECTED_RESULT_ROOTS, errors)
    _expect_equal(data, "comparison_lane", "uiautomator_24_beta_against_current_uiautomator", errors)
    _expect_equal(data, "comparison_baseline", EXPECTED_BASELINE, errors)
    _expect_equal(data, "primary_evidence", EXPECTED_BASELINE, errors)

    for key in (
        "would_modify_gradle_files",
        "would_resolve_candidate_dependency",
        "would_call_am_instrument",
        "would_start_emulators",
        "would_launch_connected_instrumentation",
        "devices_touched",
        "emulator_required",
    ):
        _expect_equal(data, key, False, errors)

    planned_command = data.get("planned_command")
    if isinstance(planned_command, str):
        if EXPECTED_PROPERTY not in planned_command:
            errors.append("expected root.planned_command to include UIAutomator comparison property")
        if EXPECTED_GRADLE_TASK not in planned_command:
            errors.append("expected root.planned_command to target connectedDebugAndroidTest")

    class_filter_property = data.get("class_filter_property")
    if isinstance(class_filter_property, str) and EXPECTED_TEST_CLASS not in class_filter_property:
        errors.append("expected root.class_filter_property to include the focused test class")

    observed_current_version = data.get("observed_current_version")
    if isinstance(observed_current_version, str) and observed_current_version != EXPECTED_REFERENCE_VERSION:
        errors.append(
            "expected root.observed_current_version to match current checked-in "
            f"UIAutomator {EXPECTED_REFERENCE_VERSION!r}, got {observed_current_version!r}"
        )

    stop_line = data.get("stop_line")
    if isinstance(stop_line, str) and "fixed four-emulator evidence remains primary" not in stop_line:
        errors.append("expected root.stop_line to preserve fixed four-emulator baseline posture")

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "summary_path",
        help="Path to run_android_uiautomator_24_comparison.ps1 summary.json.",
    )
    args = parser.parse_args(argv)

    data, errors = validate_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_uiautomator_24_comparison_summary: ok")
    print(f"status: {data.get('status')}")
    print("evidence: non_acceptance")
    print(f"candidate_version: {data.get('candidate_version')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
