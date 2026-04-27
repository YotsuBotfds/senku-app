#!/usr/bin/env python3
"""Validate Android migration preflight bundle summary JSON."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


EXPECTED_FLAGS: dict[str, bool] = {
    "metadata_only": True,
    "preflight_only": True,
    "non_acceptance_evidence": True,
    "acceptance_evidence": False,
    "emulator_required": False,
    "devices_touched": False,
    "adb_required": False,
}

EXPECTED_STEPS = {
    "tooling_version_manifest",
    "managed_device_smoke_dry_run",
    "litert_readiness_dry_run",
    "senku_tablet_2_large_data_avd_preflight",
    "orchestrator_smoke_dry_run",
    "uiautomator_24_comparison_dry_run",
    "harness_matrix_plan_only",
    "ui_state_pack_plan_only",
}

OPTIONAL_PROOF_STEPS = {
    "migration_proof_summarizer_json",
    "migration_proof_summarizer_markdown",
}

EXPECTED_VALIDATORS = {
    "validate_tooling_version_manifest",
    "validate_managed_device_smoke",
    "validate_litert_readiness",
    "validate_senku_tablet_2_large_data_avd_preflight",
    "validate_orchestrator_smoke",
    "validate_uiautomator_24_comparison",
    "validate_harness_matrix_plan",
    "validate_ui_state_pack_plan",
}

STEP_CONTRACTS: dict[str, dict[str, list[str]]] = {
    "tooling_version_manifest": {
        "summary_fragments": ["tooling_version_manifest", "summary.json"],
        "required_command_tokens": ["write_android_tooling_version_manifest.py"],
        "forbidden_command_tokens": ["-realrun"],
    },
    "managed_device_smoke_dry_run": {
        "summary_fragments": ["managed_device_smoke", "summary.json"],
        "required_command_tokens": ["run_android_managed_device_smoke.ps1", "-dryrun"],
        "forbidden_command_tokens": ["-realrun"],
    },
    "litert_readiness_dry_run": {
        "summary_fragments": ["litert_readiness", "summary.json"],
        "required_command_tokens": ["run_android_litert_readiness_matrix.ps1", "-dryrun"],
        "forbidden_command_tokens": ["-realrun"],
    },
    "senku_tablet_2_large_data_avd_preflight": {
        "summary_fragments": ["senku_tablet_2_large_data_avd_preflight", "summary.json"],
        "required_command_tokens": ["prepare_senku_tablet_2_large_data_avd.ps1"],
        "forbidden_command_tokens": ["-realrun"],
    },
    "orchestrator_smoke_dry_run": {
        "summary_fragments": ["orchestrator_smoke", "summary.json"],
        "required_command_tokens": ["run_android_orchestrator_smoke.ps1", "-dryrun"],
        "forbidden_command_tokens": ["-realrun"],
    },
    "uiautomator_24_comparison_dry_run": {
        "summary_fragments": ["uiautomator_24_comparison", "summary.json"],
        "required_command_tokens": ["run_android_uiautomator_24_comparison.ps1", "-dryrun"],
        "forbidden_command_tokens": ["-realrun"],
    },
    "harness_matrix_plan_only": {
        "summary_fragments": ["harness_matrix_plan", "summary.json"],
        "required_command_tokens": ["run_android_harness_matrix.ps1", "-planonly"],
        "forbidden_command_tokens": ["-realrun"],
    },
    "ui_state_pack_plan_only": {
        "summary_fragments": ["ui_state_pack_plan", "plan.json"],
        "required_command_tokens": ["build_android_ui_state_pack_parallel.ps1", "-planonly"],
        "forbidden_command_tokens": ["-realrun"],
    },
}

REQUIRED_FIXTURES = {
    "tiny_model_path",
    "task_inventory_path",
    "harness_run_file",
}

REQUIRED_MIGRATION_SUMMARIZER_FIELDS = {
    "json_path",
    "markdown_path",
    "inputs",
}

REQUIRED_SELF_VALIDATION_FIELDS = {
    "name",
    "command",
    "target",
    "status",
    "exit_code",
    "stdout_path",
}


def _type_name(expected_type: type | tuple[type, ...]) -> str:
    if isinstance(expected_type, tuple):
        return "|".join(item.__name__ for item in expected_type)
    return expected_type.__name__


def _expect_type(
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

    if isinstance(value, bool) and expected_type is int:
        errors.append(f"expected {scope}.{key} to be int, got bool")
    if isinstance(value, str) and not value.strip():
        errors.append(f"expected {scope}.{key} to be non-empty")


def _expect_str(value: Any, errors: list[str], scope: str) -> None:
    if not isinstance(value, str) or not value.strip():
        errors.append(f"expected {scope} to be non-empty str")


def _validate_expected_flags(data: dict[str, Any], errors: list[str]) -> None:
    for key, expected in EXPECTED_FLAGS.items():
        if key not in data:
            errors.append(f"missing root.{key}")
            continue
        value = data[key]
        if not isinstance(value, bool):
            errors.append(f"expected root.{key} to be bool, got {type(value).__name__}")
        elif value is not expected:
            errors.append(f"expected root.{key} to be {str(expected).lower()}")


def _validate_step_contract(
    step: dict[str, Any],
    name: str,
    scope: str,
    errors: list[str],
) -> None:
    contract = STEP_CONTRACTS.get(name)
    if contract is None:
        return

    command = str(step.get("command", "")).lower()
    for token in contract.get("required_command_tokens", []):
        if token.lower() not in command:
            errors.append(f"expected {scope}.command to include {token!r}")
    for token in contract.get("forbidden_command_tokens", []):
        if token.lower() in command:
            errors.append(f"expected {scope}.command to omit {token!r}")

    summary_path = step.get("summary_path")
    summary_text = "" if summary_path is None else str(summary_path).replace("\\", "/").lower()
    for fragment in contract.get("summary_fragments", []):
        if fragment.lower() not in summary_text:
            errors.append(f"expected {scope}.summary_path to include {fragment!r}")


def _validate_step(step: Any, index: int, errors: list[str]) -> str | None:
    scope = f"steps[{index}]"
    if not isinstance(step, dict):
        errors.append(f"expected {scope} to be dict, got {type(step).__name__}")
        return None

    _expect_type(step, "name", str, errors, scope)
    _expect_type(step, "status", str, errors, scope)
    _expect_type(step, "exit_code", int, errors, scope)
    _expect_type(step, "command", str, errors, scope)
    _expect_type(step, "stdout_path", str, errors, scope)

    name = step.get("name")
    if isinstance(step.get("status"), str) and step["status"] not in {"pass", "fail"}:
        errors.append(f"expected {scope}.status to be 'pass' or 'fail'")
    if isinstance(step.get("exit_code"), int) and not isinstance(step.get("exit_code"), bool):
        if step["exit_code"] < 0:
            errors.append(f"expected {scope}.exit_code to be non-negative")
    if "summary_path" in step and step.get("summary_path") is not None:
        _expect_str(step.get("summary_path"), errors, f"{scope}.summary_path")
    if "markdown_path" in step and step.get("markdown_path") is not None:
        _expect_str(step.get("markdown_path"), errors, f"{scope}.markdown_path")

    if isinstance(name, str):
        _validate_step_contract(step, name, scope, errors)

    return name if isinstance(name, str) else None


def _validate_steps(data: dict[str, Any], errors: list[str]) -> set[str]:
    steps = data.get("steps")
    if not isinstance(steps, list):
        errors.append(f"expected root.steps to be list, got {type(steps).__name__}")
        return set()
    if not steps:
        errors.append("expected root.steps to be non-empty")
        return set()

    names = {
        name
        for index, step in enumerate(steps)
        if (name := _validate_step(step, index, errors)) is not None
    }
    missing = sorted(EXPECTED_STEPS - names)
    for name in missing:
        errors.append(f"missing root.steps lane {name!r}")
    if not names.intersection(OPTIONAL_PROOF_STEPS) and "migration_summarizer" in data:
        errors.append("expected migration proof summary steps when root.migration_summarizer is emitted")
    return names


def _validate_validation_commands(data: dict[str, Any], errors: list[str]) -> set[str]:
    commands = data.get("validation_commands")
    if not isinstance(commands, list):
        errors.append(
            f"expected root.validation_commands to be list, got {type(commands).__name__}"
        )
        return set()
    if not commands:
        errors.append("expected root.validation_commands to be non-empty")
        return set()

    names: set[str] = set()
    for index, command in enumerate(commands):
        scope = f"validation_commands[{index}]"
        if not isinstance(command, dict):
            errors.append(f"expected {scope} to be dict, got {type(command).__name__}")
            continue
        _expect_type(command, "name", str, errors, scope)
        _expect_type(command, "command", str, errors, scope)
        _expect_type(command, "target", str, errors, scope)
        if "status" in command:
            _expect_type(command, "status", str, errors, scope)
        if "exit_code" in command:
            _expect_type(command, "exit_code", int, errors, scope)
        if "stdout_path" in command:
            _expect_type(command, "stdout_path", str, errors, scope)
        name = command.get("name")
        if isinstance(name, str):
            names.add(name)

    for name in sorted(EXPECTED_VALIDATORS - names):
        errors.append(f"missing root.validation_commands validator {name!r}")
    return names


def _validate_fixtures(data: dict[str, Any], errors: list[str]) -> None:
    fixtures = data.get("fixtures")
    if not isinstance(fixtures, dict):
        errors.append(f"expected root.fixtures to be dict, got {type(fixtures).__name__}")
        return

    for key in sorted(REQUIRED_FIXTURES):
        if key not in fixtures:
            errors.append(f"missing fixtures.{key}")
        else:
            _expect_str(fixtures[key], errors, f"fixtures.{key}")


def _validate_migration_summarizer(data: dict[str, Any], errors: list[str]) -> None:
    if "migration_summarizer" not in data:
        return

    summarizer = data["migration_summarizer"]
    if not isinstance(summarizer, dict):
        errors.append(
            f"expected root.migration_summarizer to be dict, got {type(summarizer).__name__}"
        )
        return

    for key in sorted(REQUIRED_MIGRATION_SUMMARIZER_FIELDS):
        if key not in summarizer:
            errors.append(f"missing migration_summarizer.{key}")
    if "json_path" in summarizer:
        _expect_str(summarizer["json_path"], errors, "migration_summarizer.json_path")
    if "markdown_path" in summarizer:
        _expect_str(summarizer["markdown_path"], errors, "migration_summarizer.markdown_path")

    inputs = summarizer.get("inputs")
    if not isinstance(inputs, list):
        errors.append(
            f"expected migration_summarizer.inputs to be list, got {type(inputs).__name__}"
        )
    elif not inputs:
        errors.append("expected migration_summarizer.inputs to be non-empty")
    else:
        for index, item in enumerate(inputs):
            _expect_str(item, errors, f"migration_summarizer.inputs[{index}]")


def _validate_self_validation(
    data: dict[str, Any],
    errors: list[str],
    *,
    allow_pending: bool = False,
) -> None:
    self_validation = data.get("self_validation")
    if not isinstance(self_validation, dict):
        errors.append(
            f"expected root.self_validation to be dict, got {type(self_validation).__name__}"
        )
        return

    for key in sorted(REQUIRED_SELF_VALIDATION_FIELDS):
        if key not in self_validation:
            errors.append(f"missing self_validation.{key}")

    _expect_type(self_validation, "name", str, errors, "self_validation")
    _expect_type(self_validation, "command", str, errors, "self_validation")
    _expect_type(self_validation, "target", str, errors, "self_validation")
    _expect_type(self_validation, "status", str, errors, "self_validation")
    if allow_pending and self_validation.get("exit_code") is None:
        pass
    else:
        _expect_type(self_validation, "exit_code", int, errors, "self_validation")
    if allow_pending and self_validation.get("stdout_path") is None:
        pass
    else:
        _expect_type(self_validation, "stdout_path", str, errors, "self_validation")

    if self_validation.get("name") != "validate_migration_preflight_bundle_summary":
        errors.append(
            "expected self_validation.name to be 'validate_migration_preflight_bundle_summary'"
        )
    expected_statuses = {"pass", "not_run"} if allow_pending else {"pass"}
    if self_validation.get("status") not in expected_statuses:
        if allow_pending:
            errors.append("expected self_validation.status to be 'pass' or 'not_run'")
        else:
            errors.append("expected self_validation.status to be 'pass'")
    expected_exit_codes = {0, None} if allow_pending else {0}
    if self_validation.get("exit_code") not in expected_exit_codes:
        if allow_pending:
            errors.append("expected self_validation.exit_code to be 0 or null")
        else:
            errors.append("expected self_validation.exit_code to be 0")


def validate_android_migration_preflight_bundle_summary(
    path: Path,
    *,
    allow_pending_self_validation: bool = False,
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
    _expect_type(data, "bundle_kind", str, errors, "root")
    _expect_type(data, "schema_version", int, errors, "root")
    _expect_type(data, "status", str, errors, "root")
    _expect_type(data, "output_dir", str, errors, "root")
    if data.get("bundle_kind") != "android_migration_preflight_bundle":
        errors.append("expected root.bundle_kind to be 'android_migration_preflight_bundle'")
    if isinstance(data.get("status"), str) and data["status"] not in {"pass", "fail"}:
        errors.append("expected root.status to be 'pass' or 'fail'")

    _validate_expected_flags(data, errors)
    _validate_fixtures(data, errors)
    _validate_steps(data, errors)
    _validate_validation_commands(data, errors)
    _validate_migration_summarizer(data, errors)
    _validate_self_validation(
        data,
        errors,
        allow_pending=allow_pending_self_validation,
    )

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "summary_path",
        help="Path to run_android_migration_preflight_bundle.ps1 summary JSON.",
    )
    parser.add_argument(
        "--allow-pending-self-validation",
        action="store_true",
        help="Allow the wrapper's bootstrap self-validation record before it is updated.",
    )
    args = parser.parse_args(argv)

    data, errors = validate_android_migration_preflight_bundle_summary(
        Path(args.summary_path),
        allow_pending_self_validation=args.allow_pending_self_validation,
    )
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_migration_preflight_bundle_summary: ok")
    print(f"status: {data.get('status')}")
    print("evidence: non_acceptance")
    print(f"steps: {len(data.get('steps', []))}")
    print(f"validators: {len(data.get('validation_commands', []))}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
