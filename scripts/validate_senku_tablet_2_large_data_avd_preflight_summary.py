#!/usr/bin/env python3
"""Validate Senku Tablet 2 large-data AVD preflight summary JSON."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


EXPECTED_KIND = "senku_tablet_2_large_data_avd_preflight"
EXPECTED_AVD = "Senku_Tablet_2"
EXPECTED_SERIAL = "emulator-5554"
EXPECTED_REQUIRED_PATH = "config_based_avd_data_partition"
EXPECTED_CONFIRMATION = "PREPARE_SENKU_TABLET_2_LARGE_DATA_AVD"

REQUIRED_FIELDS: dict[str, type | tuple[type, ...]] = {
    "summary_kind": str,
    "schema_version": int,
    "status": str,
    "dry_run": bool,
    "apply": bool,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "ui_acceptance_evidence": bool,
    "deploy_evidence": bool,
    "runtime_evidence": bool,
    "evidence_boundary": str,
    "required_path": str,
    "cli_partition_size_max_mb": int,
    "confirmation_token": str,
    "confirmation_matched": bool,
    "avd_name": str,
    "expected_serial": str,
    "avd_home": str,
    "avd_ini_path": str,
    "avd_path": str,
    "config_path": str,
    "desired_data_partition_size": str,
    "current_data_partition_size": str,
    "config_identity": dict,
    "running_device_check": dict,
    "planned_destructive_actions": dict,
    "destructive_actions_performed": bool,
    "config_updated": bool,
    "quarantined_items": list,
    "next_command": str,
    "stop_line": str,
    "generated_utc": str,
}


def _type_name(expected_type: type | tuple[type, ...]) -> str:
    if isinstance(expected_type, tuple):
        return "|".join(item.__name__ for item in expected_type)
    return expected_type.__name__


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
        errors.append(
            f"expected root.{key} to be {_type_name(expected_type)}, got {type(value).__name__}"
        )
        return
    if isinstance(value, str) and key not in {"blocked_reason"} and not value.strip():
        errors.append(f"expected root.{key} to be non-empty")


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
    for key, expected_type in REQUIRED_FIELDS.items():
        _expect_type(data, key, expected_type, errors)

    if data.get("summary_kind") != EXPECTED_KIND:
        errors.append(f"expected root.summary_kind to be {EXPECTED_KIND!r}")
    if data.get("status") not in {"dry_run_only", "pass", "blocked", "failed"}:
        errors.append("expected root.status to be dry_run_only, pass, blocked, or failed")
    if data.get("avd_name") != EXPECTED_AVD:
        errors.append(f"expected root.avd_name to be {EXPECTED_AVD!r}")
    if data.get("expected_serial") != EXPECTED_SERIAL:
        errors.append(f"expected root.expected_serial to be {EXPECTED_SERIAL!r}")
    if data.get("required_path") != EXPECTED_REQUIRED_PATH:
        errors.append(f"expected root.required_path to be {EXPECTED_REQUIRED_PATH!r}")
    if data.get("confirmation_token") != EXPECTED_CONFIRMATION:
        errors.append("unexpected confirmation token")

    for flag in ("non_acceptance_evidence",):
        if data.get(flag) is not True:
            errors.append(f"expected root.{flag} to be true")
    for flag in ("acceptance_evidence", "ui_acceptance_evidence", "deploy_evidence", "runtime_evidence"):
        if data.get(flag) is not False:
            errors.append(f"expected root.{flag} to be false")
    if "not deploy/runtime or UI acceptance evidence" not in str(data.get("evidence_boundary", "")):
        errors.append("expected root.evidence_boundary to preserve non-acceptance boundary")
    if "fixed four-emulator state-pack proof" not in str(data.get("stop_line", "")):
        errors.append("expected root.stop_line to preserve fixed-four proof boundary")

    if data.get("dry_run") is True:
        if data.get("apply") is not False:
            errors.append("expected root.apply to be false for dry_run")
        if data.get("status") != "dry_run_only":
            errors.append("expected root.status to be dry_run_only when dry_run is true")
        if data.get("destructive_actions_performed") is not False:
            errors.append("expected no destructive actions during dry_run")
        if data.get("config_updated") is not False:
            errors.append("expected config_updated=false during dry_run")
    if data.get("apply") is True and data.get("status") == "pass":
        if data.get("confirmation_matched") is not True:
            errors.append("expected confirmation_matched=true for passing apply")

    identity = data.get("config_identity")
    if isinstance(identity, dict):
        if identity.get("verified") is not True and data.get("status") != "failed":
            errors.append("expected config_identity.verified=true unless status is failed")
        checks = identity.get("checks")
        if not isinstance(checks, dict):
            errors.append("expected config_identity.checks to be dict")

    planned = data.get("planned_destructive_actions")
    if isinstance(planned, dict):
        if "update_config_disk_data_partition_size" not in planned:
            errors.append("missing planned_destructive_actions.update_config_disk_data_partition_size")
        if not isinstance(planned.get("quarantine_stale_userdata_and_snapshots"), list):
            errors.append("expected planned_destructive_actions.quarantine_stale_userdata_and_snapshots to be list")

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("summary_path")
    args = parser.parse_args(argv)

    data, errors = validate_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("senku_tablet_2_large_data_avd_preflight_summary: ok")
    print(f"status: {data.get('status')}")
    print("evidence: avd_maintenance_preflight, non_acceptance")
    return 0


if __name__ == "__main__":
    sys.exit(main())
