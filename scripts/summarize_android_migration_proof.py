"""Summarize Android migration-proof evidence JSON files.

The helper is intentionally read-only: it loads one or more existing evidence
summaries and emits compact rows for reviewer checklist notes.
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any


FIELDS = [
    "status",
    "passes",
    "fail_count",
    "skip_count",
    "artifact_path",
    "platform_anr_count",
    "matrix_homogeneous",
    "matrix_model_name",
    "identity_missing",
    "apk_sha",
    "installed_pack_counts",
    "installed_pack_source",
    "installed_pack_cache",
    "installed_pack_pushed",
    "runtime_evidence",
    "host_adb_platform_tools_version",
    "evidence_posture",
]


COUNT_KEYS = (
    "guides",
    "chunks",
    "answer_cards",
    "answer_card_clauses",
    "answer_card_sources",
    "deterministic_rules",
    "guide_related_links",
    "retrieval_metadata_guides",
)

FIXED_FOUR_EMULATORS = {
    "emulator-5554",
    "emulator-5556",
    "emulator-5558",
    "emulator-5560",
}

FIXED_FOUR_ROLES = {
    "phone_portrait",
    "phone_landscape",
    "tablet_portrait",
    "tablet_landscape",
}


def load_summary(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8-sig") as handle:
        data = json.load(handle)
    if not isinstance(data, dict):
        raise ValueError(f"{path} must contain a JSON object")
    return data


def summarize_file(path: Path) -> dict[str, Any]:
    summary = load_summary(path)
    row = summarize_summary(summary)
    row["artifact_path"] = str(path)
    return row


def summarize_summary(summary: dict[str, Any]) -> dict[str, Any]:
    installed_pack = _first_dict(summary.get("installed_pack"))
    counts = _installed_pack_counts(installed_pack, summary)

    return {
        "status": _status(summary),
        "passes": _passes(summary),
        "fail_count": _first_present(summary, "fail_count", "failed_count"),
        "skip_count": _first_present(summary, "skip_count", "skipped_count"),
        "artifact_path": _artifact_path(summary),
        "platform_anr_count": summary.get("platform_anr_count"),
        "matrix_homogeneous": summary.get("matrix_homogeneous"),
        "matrix_model_name": summary.get("matrix_model_name") or summary.get("model_name"),
        "identity_missing": _identity_missing(summary),
        "apk_sha": _first_present(summary, "matrix_apk_sha", "apk_sha"),
        "installed_pack_counts": counts,
        "installed_pack_source": _installed_pack_source(installed_pack, summary),
        "installed_pack_cache": _first_present(installed_pack, "cache_hit", "used_cache", "cached"),
        "installed_pack_pushed": _first_present(installed_pack, "pushed", "push_used", "pack_pushed"),
        "runtime_evidence": summary.get("runtime_evidence"),
        "host_adb_platform_tools_version": summary.get("host_adb_platform_tools_version"),
        "evidence_posture": _evidence_posture(summary),
    }


def rows_to_markdown(rows: list[dict[str, Any]]) -> str:
    header = "| " + " | ".join(FIELDS) + " |"
    divider = "| " + " | ".join("---" for _ in FIELDS) + " |"
    body = [
        "| " + " | ".join(_markdown_cell(row.get(field)) for field in FIELDS) + " |"
        for row in rows
    ]
    return "\n".join([header, divider, *body])


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description="Emit compact migration-proof rows from Android evidence summaries."
    )
    parser.add_argument("summary_json", nargs="+", type=Path)
    parser.add_argument(
        "--format",
        choices=("json", "markdown"),
        default="json",
        help="Output format. Defaults to compact JSON rows.",
    )
    args = parser.parse_args(argv)

    rows = [summarize_file(path) for path in args.summary_json]
    if args.format == "markdown":
        print(rows_to_markdown(rows))
    else:
        print(json.dumps(rows, indent=2, sort_keys=True))
    return 0


def _status(summary: dict[str, Any]) -> Any:
    if summary.get("status") is not None:
        return summary["status"]
    if summary.get("passed") is not None:
        return "pass" if summary["passed"] else "fail"
    failed_devices = summary.get("failed_devices")
    if isinstance(failed_devices, list):
        return "pass" if not failed_devices else "fail"
    return None


def _passes(summary: dict[str, Any]) -> str | None:
    passed = _first_present(summary, "pass_count", "passed_count")
    if summary.get("pass_count") is not None:
        total = _first_present(summary, "total_states", "total_count")
    elif summary.get("passed_count") is not None:
        total = _first_present(summary, "device_count", "total_count", "total_states")
    else:
        total = _first_present(summary, "total_states", "total_count", "device_count", "expected_tests")
    if passed is None and summary.get("passed") is True:
        passed = 1
    if passed is None:
        return None
    return f"{passed}/{total}" if total is not None else str(passed)


def _artifact_path(summary: dict[str, Any]) -> Any:
    return _first_present(
        summary,
        "output",
        "output_dir",
        "artifact_dir",
        "artifact_path",
        "summary_path",
    )


def _identity_missing(summary: dict[str, Any]) -> bool | None:
    if summary.get("identity_missing") is not None:
        return bool(summary["identity_missing"])
    devices = summary.get("devices")
    if not isinstance(devices, list):
        return None
    values = [
        device.get("identity_missing")
        for device in devices
        if isinstance(device, dict) and device.get("identity_missing") is not None
    ]
    if not values:
        return None
    return any(bool(value) for value in values)


def _installed_pack_counts(
    installed_pack: dict[str, Any], summary: dict[str, Any]
) -> dict[str, Any] | None:
    candidates = [
        installed_pack.get("counts"),
        installed_pack.get("manifest_counts"),
        installed_pack.get("sqlite_counts"),
        _nested_dict(installed_pack, "manifest", "counts"),
        _nested_dict(installed_pack, "sqlite", "counts"),
        _nested_dict(summary, "candidate_highlights", "manifest_counts"),
        _nested_dict(summary, "baseline_highlights", "manifest_counts"),
    ]
    for candidate in candidates:
        if isinstance(candidate, dict):
            counts = {key: candidate[key] for key in COUNT_KEYS if key in candidate}
            if counts:
                return counts

    direct_counts = {key: installed_pack[key] for key in COUNT_KEYS if key in installed_pack}
    return direct_counts or None


def _installed_pack_source(installed_pack: dict[str, Any], summary: dict[str, Any]) -> Any:
    return _first_present(
        installed_pack,
        "source",
        "manifest_source",
        "listing_source",
        "pack_source",
    ) or _first_present(summary, "evidence_kind", "asset_pack_source")


def _evidence_posture(summary: dict[str, Any]) -> str:
    acceptance = summary.get("acceptance_evidence")
    non_acceptance = summary.get("non_acceptance_evidence")
    explicit_fixed_four_acceptance = _has_explicit_fixed_four_acceptance(summary)
    if non_acceptance is True or acceptance is False:
        return "non-acceptance"
    if acceptance is True and explicit_fixed_four_acceptance:
        return "acceptance"
    if _has_helper_marker(summary):
        if explicit_fixed_four_acceptance:
            return "acceptance"
        return "non-acceptance"
    if summary.get("ui_acceptance_evidence") is False:
        return "non-acceptance"
    if acceptance is True:
        return "non-acceptance"
    return "acceptance"


def _has_helper_marker(summary: dict[str, Any]) -> bool:
    if summary.get("metadata_only") is True or summary.get("plan_only") is True:
        return True
    if summary.get("preflight_only") is True:
        return True
    if summary.get("dry_run") is True or summary.get("status") == "dry_run_only":
        return True
    if summary.get("runtime_evidence") is not None:
        return True
    intent = summary.get("migration_checklist_intent")
    return isinstance(intent, dict) and (
        intent.get("metadata_only") is True
        or intent.get("plan_only") is True
        or intent.get("preflight_only") is True
    )


def _has_explicit_fixed_four_acceptance(summary: dict[str, Any]) -> bool:
    if summary.get("acceptance_evidence") is not True and summary.get("ui_acceptance_evidence") is not True:
        return False
    if summary.get("status") not in (None, "pass"):
        return False
    if summary.get("fail_count") not in (None, 0):
        return False
    if summary.get("platform_anr_count") not in (None, 0):
        return False
    if summary.get("matrix_homogeneous") is False:
        return False

    pass_count = _first_present(summary, "pass_count", "passed_count")
    total_count = _first_present(summary, "total_states", "total_count", "device_count")
    if pass_count is not None and total_count is not None and pass_count != total_count:
        return False

    return _covers_fixed_four_matrix(summary)


def _covers_fixed_four_matrix(summary: dict[str, Any]) -> bool:
    intent = _first_dict(summary.get("migration_checklist_intent"))
    roles = _string_set(summary.get("selected_roles")) | _string_set(intent.get("selected_roles"))
    devices = _string_set(summary.get("devices"))

    for device in summary.get("devices", []):
        if not isinstance(device, dict):
            continue
        devices.update(_string_set(device.get("device")))
        roles.update(_string_set(device.get("role")))
        roles.update(_string_set(device.get("roles")))
        roles.update(_string_set(device.get("resolved_role")))
        roles.update(_string_set(device.get("resolved_roles")))

    return FIXED_FOUR_ROLES.issubset(roles) or FIXED_FOUR_EMULATORS.issubset(devices)


def _string_set(value: Any) -> set[str]:
    if isinstance(value, str) and value.strip():
        return {value}
    if isinstance(value, list):
        return {item for item in value if isinstance(item, str) and item.strip()}
    return set()


def _first_present(source: dict[str, Any], *keys: str) -> Any:
    for key in keys:
        if source.get(key) is not None:
            return source[key]
    return None


def _first_dict(value: Any) -> dict[str, Any]:
    return value if isinstance(value, dict) else {}


def _nested_dict(source: dict[str, Any], *keys: str) -> dict[str, Any] | None:
    value: Any = source
    for key in keys:
        if not isinstance(value, dict):
            return None
        value = value.get(key)
    return value if isinstance(value, dict) else None


def _markdown_cell(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, (dict, list)):
        text = json.dumps(value, sort_keys=True, separators=(",", ":"))
    elif isinstance(value, bool):
        text = "true" if value else "false"
    else:
        text = str(value)
    return text.replace("|", "\\|").replace("\n", " ")


if __name__ == "__main__":
    raise SystemExit(main())
