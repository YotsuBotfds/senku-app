#!/usr/bin/env python3
"""Validate run_android_orchestrator_smoke.ps1 dry-run summary.json."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


EXPECTED_TEST_CLASS = "com.senku.mobile.ScriptedPromptHarnessContractTest"
EXPECTED_GRADLE_TASK = ":app:connectedDebugAndroidTest"
EXPECTED_GRADLE_PROPERTY = "-Psenku.enableTestOrchestrator=true"
EXPECTED_CLASS_FILTER_PROPERTY = (
    "-Pandroid.testInstrumentationRunnerArguments.class="
    "com.senku.mobile.ScriptedPromptHarnessContractTest"
)
EXPECTED_APP_APK = "android-app/app/build/outputs/apk/debug/app-debug.apk"
EXPECTED_TEST_APK = "android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
EXPECTED_RESULT_ROOTS = [
    "android-app/app/build/outputs/androidTest-results/connected/debug",
    "android-app/app/build/reports/androidTests/connected/debug",
]
EXPECTED_GRADLE_PROJECT_DIR = "android-app"
EXPECTED_GRADLE_WRAPPER = "android-app/gradlew.bat"
EXPECTED_BASELINE = "fixed_four_emulator_matrix"
EXPECTED_PLANNED_COMMAND = (
    ".\\gradlew.bat :app:connectedDebugAndroidTest "
    "'-Psenku.enableTestOrchestrator=true' "
    "'-Pandroid.testInstrumentationRunnerArguments.class="
    "com.senku.mobile.ScriptedPromptHarnessContractTest' --console=plain"
)

REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "dry_run": bool,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "test_class": str,
    "gradle_task": str,
    "gradle_property": str,
    "class_filter_property": str,
    "clear_package_data": bool,
    "clear_package_data_posture": str,
    "required_app_apk": str,
    "required_test_apk": str,
    "required_inputs": dict,
    "expected_result_roots": list,
    "planned_command": str,
    "gradle_project_dir": str,
    "gradle_wrapper": str,
    "would_call_am_instrument": bool,
    "would_start_emulators": bool,
    "would_launch_connected_instrumentation": bool,
    "comparison_baseline": str,
    "primary_evidence": str,
    "stop_line": str,
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


def _expect_equal(data: dict[str, Any], key: str, expected: Any, errors: list[str]) -> None:
    if key in data and data[key] != expected:
        errors.append(f"expected root.{key} to be {expected!r}, got {data[key]!r}")


def _validate_required_inputs(value: Any, errors: list[str]) -> None:
    if not isinstance(value, dict):
        return

    for key in ("app_apk", "test_apk", "app_apk_exists", "test_apk_exists"):
        if key not in value:
            errors.append(f"missing root.required_inputs.{key}")

    if value.get("app_apk") != EXPECTED_APP_APK:
        errors.append(
            "expected root.required_inputs.app_apk to be "
            f"{EXPECTED_APP_APK!r}, got {value.get('app_apk')!r}"
        )
    if value.get("test_apk") != EXPECTED_TEST_APK:
        errors.append(
            "expected root.required_inputs.test_apk to be "
            f"{EXPECTED_TEST_APK!r}, got {value.get('test_apk')!r}"
        )
    for key in ("app_apk_exists", "test_apk_exists"):
        if key in value and not isinstance(value[key], bool):
            errors.append(
                f"expected root.required_inputs.{key} to be bool, got {type(value[key]).__name__}"
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
    _expect_equal(data, "test_class", EXPECTED_TEST_CLASS, errors)
    _expect_equal(data, "gradle_task", EXPECTED_GRADLE_TASK, errors)
    _expect_equal(data, "gradle_property", EXPECTED_GRADLE_PROPERTY, errors)
    _expect_equal(data, "class_filter_property", EXPECTED_CLASS_FILTER_PROPERTY, errors)
    _expect_equal(data, "clear_package_data", True, errors)
    _expect_equal(data, "clear_package_data_posture", "enabled_by_orchestrator_property", errors)
    _expect_equal(data, "required_app_apk", EXPECTED_APP_APK, errors)
    _expect_equal(data, "required_test_apk", EXPECTED_TEST_APK, errors)
    _expect_equal(data, "expected_result_roots", EXPECTED_RESULT_ROOTS, errors)
    _expect_equal(data, "planned_command", EXPECTED_PLANNED_COMMAND, errors)
    _expect_equal(data, "gradle_project_dir", EXPECTED_GRADLE_PROJECT_DIR, errors)
    _expect_equal(data, "gradle_wrapper", EXPECTED_GRADLE_WRAPPER, errors)
    _expect_equal(data, "would_call_am_instrument", False, errors)
    _expect_equal(data, "would_start_emulators", False, errors)
    _expect_equal(data, "would_launch_connected_instrumentation", False, errors)
    _expect_equal(data, "comparison_baseline", EXPECTED_BASELINE, errors)
    _expect_equal(data, "primary_evidence", EXPECTED_BASELINE, errors)

    planned_command = data.get("planned_command")
    if isinstance(planned_command, str) and EXPECTED_GRADLE_PROPERTY not in planned_command:
        errors.append("expected root.planned_command to include senku.enableTestOrchestrator")

    stop_line = data.get("stop_line")
    if isinstance(stop_line, str) and "fixed four-emulator evidence remains primary" not in stop_line:
        errors.append("expected root.stop_line to preserve fixed four-emulator baseline posture")

    if "evidence_kind" in data:
        errors.append("expected root.evidence_kind to be absent for orchestrator smoke dry-run summary")

    _validate_required_inputs(data.get("required_inputs"), errors)
    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("summary_path", help="Path to run_android_orchestrator_smoke.ps1 summary.json.")
    args = parser.parse_args(argv)

    data, errors = validate_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_orchestrator_smoke_summary: ok")
    print(f"status: {data.get('status')}")
    print("evidence: non_acceptance")
    print(f"primary_evidence: {data.get('primary_evidence')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
