"""Shared structural metadata validation for ingest and mobile-pack flows."""

from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable, Mapping


REPORT_SCHEMA_VERSION = 1
REPORT_TYPE = "senku_metadata_validation"
REPORT_FILENAME = "metadata_validation_report.json"
GUIDE_REQUIRED_FIELDS = (
    "guide_id",
    "slug",
    "title",
    "category",
    "description",
    "source_file",
)
MAX_EMITTED_ERRORS = 100


def _safe_text(value) -> str:
    if value is None or isinstance(value, (Mapping, list, tuple, set)):
        return ""
    return str(value).strip()


def _first_nonempty_text(*values) -> str:
    for value in values:
        text = _safe_text(value)
        if text:
            return text
    return ""


def coerce_guide_record(record) -> dict[str, str]:
    if isinstance(record, Mapping):
        data = dict(record)
    else:
        data = {
            "guide_id": getattr(record, "guide_id", ""),
            "slug": getattr(record, "slug", ""),
            "title": getattr(record, "title", ""),
            "category": getattr(record, "category", ""),
            "description": getattr(record, "description", ""),
            "source_file": getattr(record, "source_file", ""),
        }
    return {
        "guide_id": _first_nonempty_text(data.get("guide_id"), data.get("id")),
        "slug": _safe_text(data.get("slug")),
        "title": _safe_text(data.get("title")),
        "category": _safe_text(data.get("category")),
        "description": _safe_text(data.get("description")),
        "source_file": _safe_text(data.get("source_file")),
    }


def validate_guide_records(
    guide_records: Iterable,
    *,
    scope: str,
    max_emitted_errors: int = MAX_EMITTED_ERRORS,
) -> dict:
    normalized = [coerce_guide_record(record) for record in guide_records]
    errors = []
    emitted = 0

    for record in normalized:
        missing_fields = [
            field for field in GUIDE_REQUIRED_FIELDS if not _safe_text(record.get(field))
        ]
        if not missing_fields:
            continue
        if emitted < max_emitted_errors:
            errors.append(
                {
                    "record_type": "guide",
                    "record_id": record.get("guide_id", ""),
                    "source_file": record.get("source_file", ""),
                    "missing_fields": missing_fields,
                }
            )
        emitted += 1

    return {
        "schema_version": REPORT_SCHEMA_VERSION,
        "report_type": REPORT_TYPE,
        "scope": scope,
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "status": "fail" if emitted else "pass",
        "required_fields": {
            "guide": list(GUIDE_REQUIRED_FIELDS),
        },
        "summary": {
            "guides_checked": len(normalized),
            "guide_errors": emitted,
            "errors_emitted": len(errors),
            "errors_truncated": emitted > len(errors),
        },
        "errors": errors,
    }


def write_validation_report(report: dict, destination) -> Path:
    path = Path(destination)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return path


def report_has_errors(report: Mapping) -> bool:
    return _safe_text(report.get("status")).lower() == "fail"


def format_validation_errors(report: Mapping) -> str:
    summary = report.get("summary") or {}
    lines = [
        "Metadata validation failed.",
        f"scope={_safe_text(report.get('scope'))}",
        f"guide_errors={summary.get('guide_errors', 0)}",
    ]
    for error in (report.get("errors") or [])[:10]:
        source_file = _safe_text(error.get("source_file")) or "<unknown>"
        record_id = _safe_text(error.get("record_id")) or "<missing-id>"
        missing = ", ".join(error.get("missing_fields") or [])
        lines.append(f"{source_file} ({record_id}): missing {missing}")
    if summary.get("errors_truncated"):
        lines.append("additional errors omitted from report output")
    return "\n".join(lines)
