#!/usr/bin/env python3
"""Validate run_android_asset_pack_parity_gate.ps1 summary JSON."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


FIXED_FOUR_EMULATOR_MATRIX = "fixed_four_emulator_matrix"
ASSET_PACK_PARITY_EVIDENCE = "asset_pack_parity"
REAL_STATUSES = {"pass", "fail"}

REQUIRED_FIELDS: dict[str, type | tuple[type, ...]] = {
    "status": str,
    "baseline_pack_dir": str,
    "candidate_pack_dir": str,
    "output": str,
    "fail_on_mismatch": bool,
    "dry_run": bool,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "asset_pack_parity_evidence": bool,
    "ui_acceptance_evidence": bool,
    "evidence_kind": str,
    "comparison_baseline": str,
    "primary_evidence": str,
    "stop_line": str,
}


def _expect(
    mapping: dict[str, Any],
    key: str,
    expected_type: type | tuple[type, ...],
    errors: list[str],
    scope: str = "root",
) -> None:
    if key not in mapping:
        errors.append(f"missing {scope}.{key}")
        return

    value = mapping[key]
    if not isinstance(value, expected_type):
        if isinstance(expected_type, tuple):
            type_name = "|".join(item.__name__ for item in expected_type)
        else:
            type_name = expected_type.__name__
        errors.append(f"expected {scope}.{key} to be {type_name}, got {type(value).__name__}")
        return

    if isinstance(value, str) and not value.strip():
        errors.append(f"expected {scope}.{key} to be non-empty")


def _expect_value(data: dict[str, Any], key: str, expected: Any, errors: list[str]) -> None:
    if key in data and data.get(key) != expected:
        errors.append(f"expected root.{key} to be {expected!r}")


def _validate_status_and_dry_run(data: dict[str, Any], errors: list[str]) -> None:
    status = data.get("status")
    dry_run = data.get("dry_run")

    if status == "dry_run_only":
        if dry_run is not True:
            errors.append("expected root.dry_run to be true when root.status is 'dry_run_only'")
        if "summary_markdown" in data:
            errors.append("expected root.summary_markdown to be absent for dry-run summaries")
        if "would_run" in data and data.get("would_run") is not False:
            errors.append("expected root.would_run to be false for dry-run summaries")
        return

    if status in REAL_STATUSES:
        if dry_run is not False:
            errors.append("expected root.dry_run to be false for real-run summaries")
        if "summary_markdown" in data:
            _expect(data, "summary_markdown", str, errors)
        return

    if isinstance(status, str):
        errors.append("expected root.status to be 'dry_run_only', 'pass', or 'fail'")


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
        _expect(data, key, expected_type, errors)

    _expect_value(data, "fail_on_mismatch", True, errors)
    _expect_value(data, "non_acceptance_evidence", True, errors)
    _expect_value(data, "acceptance_evidence", False, errors)
    _expect_value(data, "asset_pack_parity_evidence", True, errors)
    _expect_value(data, "ui_acceptance_evidence", False, errors)
    _expect_value(data, "evidence_kind", ASSET_PACK_PARITY_EVIDENCE, errors)
    _expect_value(data, "comparison_baseline", FIXED_FOUR_EMULATOR_MATRIX, errors)
    _expect_value(data, "primary_evidence", FIXED_FOUR_EMULATOR_MATRIX, errors)

    stop_line = data.get("stop_line")
    if isinstance(stop_line, str):
        if "fixed four-emulator evidence remains primary" not in stop_line:
            errors.append("expected root.stop_line to preserve the fixed four-emulator boundary")
        if "not UI acceptance evidence" not in stop_line:
            errors.append("expected root.stop_line to state this is not UI acceptance evidence")

    _validate_status_and_dry_run(data, errors)
    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("summary_path", help="Path to an Android asset-pack parity gate JSON summary.")
    args = parser.parse_args(argv)

    data, errors = validate_summary(Path(args.summary_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_asset_pack_parity_summary: ok")
    print(f"status: {data.get('status')}")
    print(f"dry_run: {str(data.get('dry_run')).lower()}")
    print("evidence: non_acceptance_asset_pack_parity")
    print(f"comparison_baseline: {data.get('comparison_baseline')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
