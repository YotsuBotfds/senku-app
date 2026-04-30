#!/usr/bin/env python3
"""Validate run_android_physical_phone_smoke.ps1 summary.json."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


EXPECTED_PACKAGE = "com.senku.mobile"
EXPECTED_LAUNCH_ACTIVITY = "com.senku.mobile/.MainActivity"
DEFAULT_SUMMARY_ROOT = (
    Path(__file__).resolve().parents[1] / "artifacts" / "bench" / "android_physical_phone_smoke"
)

REQUIRED_TYPES: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "dry_run": bool,
    "physical_device": bool,
    "launches_emulators": bool,
    "serial_required_unless_dry_run": bool,
    "serial": (str, type(None)),
    "package": str,
    "launch_activity": str,
    "evidence": dict,
    "commands": dict,
    "summary_json": str,
    "summary_markdown": str,
}

REQUIRED_EVIDENCE_TYPES: dict[str, type | tuple[type, ...]] = {
    "focus_path": str,
    "screenshot_path": (str, type(None)),
    "dump_path": (str, type(None)),
    "logcat_path": (str, type(None)),
    "focus_contains_launch_activity": (bool, type(None)),
}

REQUIRED_TEXT_CHECK_TYPES: dict[str, type | tuple[type, ...]] = {
    "requested": list,
    "passed": list,
    "missing": list,
}

REQUIRED_INTERACTION_TYPES: dict[str, type | tuple[type, ...]] = {
    "enabled": bool,
    "query": str,
    "steps": list,
}

REQUIRED_INTERACTION_STEP_TYPES: dict[str, type | tuple[type, ...]] = {
    "name": str,
    "status": str,
}

EXPECTED_INTERACTION_STEPS = [
    "tap_saved",
    "tap_query_field",
    "enter_query",
    "submit_query",
    "back",
]
VALID_INTERACTION_STATUSES = {"success", "failed", "skipped"}


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


def _validate_common_contract(data: dict[str, Any], errors: list[str]) -> None:
    for key, expected_type in REQUIRED_TYPES.items():
        _expect_type(data, key, expected_type, errors)

    _expect_equal(data, "physical_device", True, errors)
    _expect_equal(data, "launches_emulators", False, errors)
    _expect_equal(data, "serial_required_unless_dry_run", True, errors)
    _expect_equal(data, "package", EXPECTED_PACKAGE, errors)
    _expect_equal(data, "launch_activity", EXPECTED_LAUNCH_ACTIVITY, errors)

    evidence = data.get("evidence")
    if isinstance(evidence, dict):
        for key, expected_type in REQUIRED_EVIDENCE_TYPES.items():
            _expect_type(evidence, key, expected_type, errors, scope="root.evidence")

    text_checks = data.get("text_checks")
    if text_checks is not None:
        if not isinstance(text_checks, dict):
            errors.append(f"expected root.text_checks to be dict, got {type(text_checks).__name__}")
        else:
            for key, expected_type in REQUIRED_TEXT_CHECK_TYPES.items():
                _expect_type(text_checks, key, expected_type, errors, scope="root.text_checks")
            _validate_text_checks(text_checks, errors)

    interaction = data.get("interaction")
    if interaction is not None:
        if not isinstance(interaction, dict):
            errors.append(f"expected root.interaction to be dict, got {type(interaction).__name__}")
        else:
            for key, expected_type in REQUIRED_INTERACTION_TYPES.items():
                _expect_type(interaction, key, expected_type, errors, scope="root.interaction")
            _validate_interaction(interaction, errors)


def _expect_string_list(value: Any, key: str, errors: list[str]) -> list[str]:
    if not isinstance(value, list):
        return []

    strings: list[str] = []
    for index, item in enumerate(value):
        if not isinstance(item, str):
            errors.append(
                f"expected root.text_checks.{key}[{index}] to be str, got {type(item).__name__}"
            )
        elif not item.strip():
            errors.append(f"expected root.text_checks.{key}[{index}] to be non-empty")
        else:
            strings.append(item)
    return strings


def _validate_text_checks(text_checks: dict[str, Any], errors: list[str]) -> None:
    requested = _expect_string_list(text_checks.get("requested"), "requested", errors)
    passed = _expect_string_list(text_checks.get("passed"), "passed", errors)
    missing = _expect_string_list(text_checks.get("missing"), "missing", errors)

    requested_set = set(requested)
    for fragment in passed:
        if fragment not in requested_set:
            errors.append(f"expected root.text_checks.passed item to be requested: {fragment!r}")
    for fragment in missing:
        if fragment not in requested_set:
            errors.append(f"expected root.text_checks.missing item to be requested: {fragment!r}")


def _validate_interaction(interaction: dict[str, Any], errors: list[str]) -> None:
    if interaction.get("enabled") is not True:
        errors.append(f"expected root.interaction.enabled to be True, got {interaction.get('enabled')!r}")

    steps = interaction.get("steps")
    if not isinstance(steps, list):
        return

    if not steps:
        errors.append("expected root.interaction.steps to be non-empty")
        return

    seen_names: list[str] = []
    for index, step in enumerate(steps):
        scope = f"root.interaction.steps[{index}]"
        if not isinstance(step, dict):
            errors.append(f"expected {scope} to be dict, got {type(step).__name__}")
            continue
        for key, expected_type in REQUIRED_INTERACTION_STEP_TYPES.items():
            _expect_type(step, key, expected_type, errors, scope=scope)
        name = step.get("name")
        status = step.get("status")
        if isinstance(name, str):
            seen_names.append(name)
            if name not in EXPECTED_INTERACTION_STEPS:
                errors.append(f"expected {scope}.name to be a known interaction step, got {name!r}")
        if isinstance(status, str) and status not in VALID_INTERACTION_STATUSES:
            errors.append(f"expected {scope}.status to be success|failed|skipped, got {status!r}")
        message = step.get("message")
        if message is not None and (not isinstance(message, str) or not message.strip()):
            errors.append(f"expected {scope}.message to be non-empty str when present")

    missing_names = [name for name in EXPECTED_INTERACTION_STEPS if name not in seen_names]
    if missing_names:
        errors.append(f"expected root.interaction.steps to include: {', '.join(missing_names)}")


def _validate_dry_run(data: dict[str, Any], errors: list[str]) -> None:
    _expect_equal(data, "status", "dry_run_only", errors)
    _expect_equal(data, "dry_run", True, errors)

    serial = data.get("serial")
    if isinstance(serial, str) and re.match(r"^emulator-\d+$", serial):
        errors.append(f"expected root.serial not to be an emulator serial, got {serial!r}")

    evidence = data.get("evidence")
    if isinstance(evidence, dict) and evidence.get("focus_contains_launch_activity") is True:
        errors.append(
            "expected root.evidence.focus_contains_launch_activity not to be true for dry-run summaries"
        )

    text_checks = data.get("text_checks")
    if isinstance(text_checks, dict):
        if text_checks.get("passed"):
            errors.append("expected root.text_checks.passed to be empty for dry-run summaries")
        if text_checks.get("missing"):
            errors.append("expected root.text_checks.missing to be empty for dry-run summaries")

    interaction = data.get("interaction")
    if isinstance(interaction, dict) and isinstance(interaction.get("steps"), list):
        for index, step in enumerate(interaction["steps"]):
            if isinstance(step, dict) and step.get("status") != "skipped":
                errors.append(
                    f"expected root.interaction.steps[{index}].status to be skipped for dry-run summaries"
                )


def _validate_completed(data: dict[str, Any], errors: list[str]) -> None:
    _expect_equal(data, "status", "completed", errors)
    _expect_equal(data, "dry_run", False, errors)

    serial = data.get("serial")
    if not isinstance(serial, str) or not serial.strip():
        errors.append("expected root.serial to be non-empty for completed physical-phone smoke")
    elif re.match(r"^emulator-\d+$", serial):
        errors.append(f"expected root.serial not to be an emulator serial, got {serial!r}")

    evidence = data.get("evidence")
    if isinstance(evidence, dict):
        _expect_equal(
            evidence,
            "focus_contains_launch_activity",
            True,
            errors,
            scope="root.evidence",
        )

    text_checks = data.get("text_checks")
    if isinstance(text_checks, dict):
        requested = text_checks.get("requested")
        passed = text_checks.get("passed")
        missing = text_checks.get("missing")
        if isinstance(requested, list) and isinstance(passed, list) and isinstance(missing, list):
            accounted_for = set(passed) | set(missing)
            for fragment in requested:
                if isinstance(fragment, str) and fragment not in accounted_for:
                    errors.append(
                        f"expected requested text fragment to be passed or missing: {fragment!r}"
                    )
        if missing:
            errors.append("expected root.text_checks.missing to be empty for completed summaries")

    interaction = data.get("interaction")
    if isinstance(interaction, dict) and isinstance(interaction.get("steps"), list):
        for index, step in enumerate(interaction["steps"]):
            if isinstance(step, dict) and step.get("status") != "success":
                errors.append(
                    f"expected root.interaction.steps[{index}].status to be success for completed summaries"
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
    _validate_common_contract(data, errors)

    status = data.get("status")
    if status == "dry_run_only":
        _validate_dry_run(data, errors)
    elif status == "completed":
        _validate_completed(data, errors)
    else:
        errors.append("expected root.status to be 'dry_run_only' or 'completed'")

    return data, errors


def find_latest_summary(summary_root: Path) -> tuple[Path | None, list[str]]:
    if not summary_root.exists():
        return None, [f"summary root not found: {summary_root}"]
    if not summary_root.is_dir():
        return None, [f"summary root is not a directory: {summary_root}"]

    candidates = [path for path in summary_root.glob("*/summary.json") if path.is_file()]
    root_summary = summary_root / "summary.json"
    if root_summary.is_file():
        candidates.append(root_summary)

    if not candidates:
        return None, [f"no summary.json files found under: {summary_root}"]

    return max(candidates, key=lambda path: (path.stat().st_mtime_ns, str(path))), []


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "summary_path",
        nargs="?",
        help="Path to run_android_physical_phone_smoke.ps1 summary.json.",
    )
    parser.add_argument(
        "--latest",
        action="store_true",
        help="Validate the newest summary.json under --summary-root.",
    )
    parser.add_argument(
        "--summary-root",
        default=str(DEFAULT_SUMMARY_ROOT),
        help="Root containing timestamped physical-phone smoke output directories.",
    )
    args = parser.parse_args(argv)

    if args.latest:
        summary_path, errors = find_latest_summary(Path(args.summary_root))
        if errors:
            for error in errors:
                print(f"ERROR: {error}")
            return 1
        assert summary_path is not None
    elif args.summary_path:
        summary_path = Path(args.summary_path)
    else:
        parser.error("summary_path is required unless --latest is set")

    data, errors = validate_summary(summary_path)
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_physical_phone_smoke_summary: ok")
    print(f"summary_path: {summary_path}")
    print(f"status: {data.get('status')}")
    print(f"physical_device: {data.get('physical_device')}")
    print(f"launches_emulators: {data.get('launches_emulators')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
