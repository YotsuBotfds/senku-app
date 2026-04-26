#!/usr/bin/env python3
"""Validate prompt-pack guide expectations against guide frontmatter."""

from __future__ import annotations

import argparse
import csv
import json
import re
import sys
from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable, Mapping, Sequence


REPORT_SCHEMA_VERSION = 1
REPORT_TYPE = "senku_prompt_expectation_validation"
DEFAULT_PROMPTS_DIR = Path("artifacts/prompts")
DEFAULT_GUIDES_DIR = Path("guides")
SUPPORTED_PACK_SUFFIXES = {".jsonl", ".csv"}
GUIDE_ID_TOKEN_RE = re.compile(r"\bGD-[A-Za-z0-9]+\b")
GUIDE_ID_RE = re.compile(r"^GD-\d{3}$")
PROMPT_ID_KEYS = ("id", "prompt_id", "case_id", "label", "name")
EXPECTED_GUIDE_KEYS = (
    "guide_id",
    "guide_ids",
    "expected_guide_id",
    "expected_guide_ids",
    "expected_guides",
    "expected_source_guide_ids",
    "primary_expected_guide_ids",
    "primary_expected_guides",
    "backup_expected_guide_ids",
    "target_guide_id",
    "target_guide_ids",
    "target_guides",
)
RETRIEVED_GUIDE_KEYS = (
    "top_retrieved_guide_ids",
    "retrieved_guide_ids",
    "source_guide_ids",
    "source_candidates",
    "retrieved",
)
PRIMARY_EXPECTED_GUIDE_KEYS = (
    "primary_expected_guide_ids",
    "primary_expected_guides",
)
PRIMARY_HIT_KEYS_BY_TOP_K = {
    1: "primary_hit_at_1",
    3: "primary_hit_at_3",
}


@dataclass(frozen=True)
class PromptRecord:
    path: str
    line_number: int
    prompt_id: str
    data: dict[str, Any]


def _clean_text(value: Any) -> str:
    return "" if value is None else str(value).strip()


def _display_path(path: Path, root: Path) -> str:
    try:
        return path.resolve().relative_to(root.resolve()).as_posix()
    except ValueError:
        return path.as_posix()


def read_text(path: Path) -> str:
    for encoding in ("utf-8-sig", "utf-16"):
        try:
            return path.read_text(encoding=encoding)
        except UnicodeError:
            continue
    return path.read_text(encoding="utf-8", errors="replace")


def discover_prompt_packs(prompts_dir: Path) -> list[Path]:
    if not prompts_dir.exists():
        return []
    return sorted(
        (
            path
            for path in prompts_dir.rglob("*")
            if path.is_file() and path.suffix.lower() in SUPPORTED_PACK_SUFFIXES
        ),
        key=lambda item: item.as_posix().lower(),
    )


def parse_frontmatter(path: Path) -> dict[str, str]:
    lines = read_text(path).splitlines()
    if not lines or lines[0].strip() != "---":
        return {}
    end_index = None
    for index in range(1, len(lines)):
        if lines[index].strip() == "---":
            end_index = index
            break
    if end_index is None:
        return {}

    meta: dict[str, str] = {}
    for line in lines[1:end_index]:
        if not line or line[0].isspace() or ":" not in line:
            continue
        key, raw_value = line.split(":", 1)
        key = key.strip()
        value = raw_value.strip()
        if len(value) >= 2 and value[0] == value[-1] and value[0] in {"'", '"'}:
            value = value[1:-1]
        if key:
            meta[key] = value
    return meta


def load_guide_catalog(guides_dir: Path, *, root: Path) -> tuple[dict[str, dict[str, str]], list[dict[str, Any]]]:
    catalog: dict[str, dict[str, str]] = {}
    issues: list[dict[str, Any]] = []
    if not guides_dir.exists():
        issues.append(
            issue(
                "error",
                "guides_dir_missing",
                f"Guides directory does not exist: {guides_dir}",
                path=str(guides_dir),
            )
        )
        return catalog, issues

    for path in sorted(guides_dir.glob("*.md"), key=lambda item: item.name.lower()):
        meta = parse_frontmatter(path)
        guide_id = _clean_text(meta.get("id")).upper()
        if not guide_id:
            continue
        if not GUIDE_ID_RE.match(guide_id):
            issues.append(
                issue(
                    "error",
                    "catalog_malformed_guide_id",
                    f"Guide frontmatter id is not well-formed: {guide_id}",
                    path=_display_path(path, root),
                    guide_ids=[guide_id],
                )
            )
            continue
        if guide_id in catalog:
            issues.append(
                issue(
                    "error",
                    "catalog_duplicate_guide_id",
                    f"Duplicate guide id {guide_id} in guide frontmatter.",
                    path=_display_path(path, root),
                    guide_ids=[guide_id],
                    details={"first_path": catalog[guide_id]["source_file"]},
                )
            )
            continue
        catalog[guide_id] = {
            "guide_id": guide_id,
            "slug": _clean_text(meta.get("slug")),
            "title": _clean_text(meta.get("title")),
            "source_file": _display_path(path, root),
        }
    return catalog, issues


def load_prompt_pack(path: Path, *, root: Path) -> tuple[list[PromptRecord], list[dict[str, Any]]]:
    suffix = path.suffix.lower()
    if suffix == ".jsonl":
        return load_jsonl_pack(path, root=root)
    if suffix == ".csv":
        return load_csv_pack(path, root=root)
    return [], [
        issue(
            "error",
            "unsupported_prompt_pack_format",
            f"Unsupported prompt pack format: {path.suffix}",
            path=_display_path(path, root),
        )
    ]


def load_jsonl_pack(path: Path, *, root: Path) -> tuple[list[PromptRecord], list[dict[str, Any]]]:
    records: list[PromptRecord] = []
    issues: list[dict[str, Any]] = []
    display = _display_path(path, root)
    for line_number, raw_line in enumerate(read_text(path).splitlines(), start=1):
        line = raw_line.strip()
        if not line:
            continue
        try:
            value = json.loads(line)
        except json.JSONDecodeError as exc:
            issues.append(
                issue(
                    "error",
                    "invalid_jsonl",
                    f"Invalid JSONL record: {exc.msg}",
                    path=display,
                    line=line_number,
                )
            )
            continue
        if not isinstance(value, dict):
            issues.append(
                issue(
                    "error",
                    "prompt_record_not_object",
                    "JSONL prompt records must be objects.",
                    path=display,
                    line=line_number,
                )
            )
            continue
        records.append(
            PromptRecord(
                path=display,
                line_number=line_number,
                prompt_id=first_present(value, PROMPT_ID_KEYS),
                data=value,
            )
        )
    return records, issues


def load_csv_pack(path: Path, *, root: Path) -> tuple[list[PromptRecord], list[dict[str, Any]]]:
    records: list[PromptRecord] = []
    issues: list[dict[str, Any]] = []
    display = _display_path(path, root)
    try:
        reader = csv.DictReader(read_text(path).splitlines())
        if not reader.fieldnames:
            return records, [
                issue("error", "csv_missing_header", "CSV prompt pack has no header.", path=display)
            ]
        for line_number, row in enumerate(reader, start=2):
            data = {str(key or "").strip(): value for key, value in row.items()}
            if not any(_clean_text(value) for value in data.values()):
                continue
            records.append(
                PromptRecord(
                    path=display,
                    line_number=line_number,
                    prompt_id=first_present(data, PROMPT_ID_KEYS),
                    data=data,
                )
            )
    except csv.Error as exc:
        issues.append(
            issue(
                "error",
                "csv_parse_error",
                f"CSV parser error: {exc}",
                path=display,
            )
        )
    return records, issues


def first_present(record: Mapping[str, Any], keys: Sequence[str]) -> str:
    lowered = {str(key).lower(): value for key, value in record.items()}
    for key in keys:
        value = _clean_text(lowered.get(key.lower()))
        if value:
            return value
    return ""


def extract_guide_ids(value: Any) -> tuple[list[str], list[str]]:
    ids: list[str] = []
    malformed: list[str] = []

    def visit(item: Any) -> None:
        if item is None:
            return
        if isinstance(item, Mapping):
            nested_keys = (
                "guide_id",
                "id",
                "expected_guide_id",
                "expected_guide_ids",
                "guide_ids",
            )
            for key in nested_keys:
                if key in item:
                    visit(item.get(key))
            return
        if isinstance(item, Sequence) and not isinstance(item, (str, bytes, bytearray)):
            for nested in item:
                visit(nested)
            return
        text = _clean_text(item)
        if not text:
            return
        for token in GUIDE_ID_TOKEN_RE.findall(text):
            normalized = token.upper()
            if GUIDE_ID_RE.match(normalized):
                ids.append(normalized)
            else:
                malformed.append(token)

    visit(value)
    return dedupe(ids), dedupe(malformed)


def dedupe(values: Iterable[str]) -> list[str]:
    seen: set[str] = set()
    result: list[str] = []
    for value in values:
        text = _clean_text(value)
        key = text.upper()
        if text and key not in seen:
            seen.add(key)
            result.append(text.upper())
    return result


def expected_field_ids(record: Mapping[str, Any]) -> dict[str, list[str]]:
    field_ids: dict[str, list[str]] = {}
    for key, value in record.items():
        if str(key).lower() not in EXPECTED_GUIDE_KEYS:
            continue
        ids, _malformed = extract_guide_ids(value)
        field_ids[str(key)] = ids
    return field_ids


def expected_ids_from_record(record: Mapping[str, Any]) -> list[str]:
    ids: list[str] = []
    for value in expected_field_ids(record).values():
        ids.extend(value)
    return dedupe(ids)


def primary_expected_ids_from_record(record: Mapping[str, Any]) -> list[str]:
    ids: list[str] = []
    lowered = {str(key).lower(): value for key, value in record.items()}
    for key in PRIMARY_EXPECTED_GUIDE_KEYS:
        extracted, _malformed = extract_guide_ids(lowered.get(key))
        ids.extend(extracted)
    return dedupe(ids)


def validate_prompt_records(
    records: Sequence[PromptRecord],
    *,
    guide_catalog: Mapping[str, Mapping[str, str]],
) -> list[dict[str, Any]]:
    issues: list[dict[str, Any]] = []
    by_prompt_id: dict[str, list[PromptRecord]] = defaultdict(list)

    for record in records:
        has_expectations = record_has_expectation_metadata(record.data)
        if not record.prompt_id:
            if has_expectations:
                issues.append(
                    issue(
                        "error",
                        "missing_prompt_id",
                        "Expectation-bearing prompt record is missing a stable prompt id.",
                        path=record.path,
                        line=record.line_number,
                    )
                )
        else:
            by_prompt_id[record.prompt_id].append(record)

        field_ids: dict[str, list[str]] = {}
        for key, value in record.data.items():
            normalized_key = str(key).lower()
            if normalized_key not in EXPECTED_GUIDE_KEYS:
                continue
            ids, malformed = extract_guide_ids(value)
            field_ids[str(key)] = ids
            nonempty = _clean_text(value) or (
                isinstance(value, Sequence)
                and not isinstance(value, (str, bytes, bytearray))
                and bool(value)
            )
            if malformed:
                issues.append(
                    issue(
                        "error",
                        "malformed_expected_guide_id",
                        f"Expected-guide field {key!r} contains malformed guide id(s): {', '.join(malformed)}.",
                        path=record.path,
                        line=record.line_number,
                        prompt_id=record.prompt_id,
                        guide_ids=malformed,
                        field=str(key),
                    )
                )
            if nonempty and not ids:
                issues.append(
                    issue(
                        "error",
                        "expected_guide_field_without_guide_id",
                        f"Expected-guide field {key!r} does not contain a GD-### guide id.",
                        path=record.path,
                        line=record.line_number,
                        prompt_id=record.prompt_id,
                        field=str(key),
                    )
                )
            for guide_id in ids:
                if guide_id not in guide_catalog:
                    issues.append(
                        issue(
                            "error",
                            "unknown_expected_guide_id",
                            f"Expected guide id {guide_id} is not present in guide frontmatter.",
                            path=record.path,
                            line=record.line_number,
                            prompt_id=record.prompt_id,
                            guide_ids=[guide_id],
                            field=str(key),
                        )
                    )

        issues.extend(expectation_disagreement_issues(record, field_ids))

    for prompt_id, duplicates in sorted(by_prompt_id.items()):
        if len(duplicates) <= 1:
            continue
        if not any(record_has_expectation_metadata(item.data) for item in duplicates):
            continue
        issues.append(
            issue(
                "error",
                "duplicate_prompt_id",
                f"Expectation-bearing prompt id {prompt_id!r} appears {len(duplicates)} times.",
                prompt_id=prompt_id,
                details={
                    "locations": [
                        {"path": item.path, "line": item.line_number}
                        for item in duplicates
                    ]
                },
            )
        )

    return issues


def record_has_expectation_metadata(record: Mapping[str, Any]) -> bool:
    return any(str(key).lower() in EXPECTED_GUIDE_KEYS for key in record)


def expectation_disagreement_issues(
    record: PromptRecord,
    field_ids: Mapping[str, Sequence[str]],
) -> list[dict[str, Any]]:
    guide_fields = {
        key: set(ids)
        for key, ids in field_ids.items()
        if key.lower() in {"guide_id", "guide_ids"} and ids
    }
    expectation_fields = {
        key: set(ids)
        for key, ids in field_ids.items()
        if key.lower() not in {"guide_id", "guide_ids"} and ids
    }
    issues: list[dict[str, Any]] = []
    for guide_key, guide_ids in guide_fields.items():
        for expected_key, expected_ids in expectation_fields.items():
            if guide_ids & expected_ids:
                continue
            all_ids = sorted(guide_ids | expected_ids)
            issues.append(
                issue(
                    "error",
                    "expectation_field_disagreement",
                    (
                        f"Expected-guide fields {guide_key!r} and {expected_key!r} "
                        f"do not overlap ({', '.join(all_ids)})."
                    ),
                    path=record.path,
                    line=record.line_number,
                    prompt_id=record.prompt_id,
                    guide_ids=all_ids,
                    details={
                        guide_key: sorted(guide_ids),
                        expected_key: sorted(expected_ids),
                    },
                )
            )
    return issues


def load_allowed_drift(path: Path | None) -> list[dict[str, Any]]:
    if path is None:
        return []
    data = json.loads(read_text(path))
    if isinstance(data, list):
        items = data
    elif isinstance(data, dict):
        items = (
            data.get("allowed_drift")
            or data.get("exceptions")
            or data.get("allowlist")
            or []
        )
    else:
        raise ValueError("Allowed-drift manifest must be a JSON object or array.")
    if not isinstance(items, list):
        raise ValueError("Allowed-drift manifest entries must be a list.")
    return [item for item in items if isinstance(item, dict)]


def issue_is_suppressed(item: Mapping[str, Any], suppressions: Sequence[Mapping[str, Any]]) -> tuple[bool, str]:
    prompt_id = _clean_text(item.get("prompt_id"))
    code = _clean_text(item.get("code"))
    guide_ids = set(dedupe(item.get("guide_ids") or []))
    path = _clean_text(item.get("path"))
    for suppression in suppressions:
        prompt_ids = values_as_set(
            suppression.get("prompt_ids", suppression.get("prompt_id"))
        )
        if prompt_ids and prompt_id not in prompt_ids:
            continue
        codes = values_as_set(suppression.get("issue_codes", suppression.get("code")))
        if codes and code not in codes:
            continue
        suppressed_guides = set(
            dedupe(
                list(values_as_set(suppression.get("guide_ids", suppression.get("guide_id"))))
                + list(values_as_set(suppression.get("expected_guide_ids")))
            )
        )
        if suppressed_guides and not (guide_ids & suppressed_guides):
            continue
        paths = values_as_set(suppression.get("paths", suppression.get("path")))
        if paths and path not in paths:
            continue
        return True, _clean_text(suppression.get("reason"))
    return False, ""


def values_as_set(value: Any) -> set[str]:
    if value is None:
        return set()
    if isinstance(value, Sequence) and not isinstance(value, (str, bytes, bytearray)):
        return {_clean_text(item) for item in value if _clean_text(item)}
    text = _clean_text(value)
    return {text} if text else set()


def apply_suppressions(
    issues: Sequence[dict[str, Any]],
    suppressions: Sequence[Mapping[str, Any]],
) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    active: list[dict[str, Any]] = []
    suppressed: list[dict[str, Any]] = []
    for item in issues:
        matched, reason = issue_is_suppressed(item, suppressions)
        if matched:
            copy = dict(item)
            copy["suppressed"] = True
            copy["suppression_reason"] = reason
            suppressed.append(copy)
        else:
            active.append(item)
    return active, suppressed


def load_retrieval_eval(path: Path, *, root: Path) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    suffix = path.suffix.lower()
    if suffix == ".json":
        return load_retrieval_eval_json(path, root=root)
    if suffix in {".md", ".markdown", ".txt"}:
        return load_retrieval_eval_markdown(path, root=root)
    return [], [
        issue(
            "warning",
            "unsupported_retrieval_eval_format",
            f"Unsupported retrieval eval format: {path.suffix}",
            path=_display_path(path, root),
        )
    ]


def load_retrieval_eval_json(path: Path, *, root: Path) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    try:
        data = json.loads(read_text(path))
    except json.JSONDecodeError as exc:
        return [], [
            issue(
                "warning",
                "invalid_retrieval_eval_json",
                f"Invalid retrieval eval JSON: {exc.msg}",
                path=_display_path(path, root),
            )
        ]
    rows = rows_from_json_payload(data)
    for row in rows:
        row["_eval_path"] = _display_path(path, root)
    return rows, []


def rows_from_json_payload(data: Any) -> list[dict[str, Any]]:
    if isinstance(data, list):
        return [dict(item) for item in data if isinstance(item, Mapping)]
    if not isinstance(data, Mapping):
        return []
    for key in ("rows", "results", "prompts", "items"):
        value = data.get(key)
        if isinstance(value, list):
            return [dict(item) for item in value if isinstance(item, Mapping)]
    return []


def load_retrieval_eval_markdown(path: Path, *, root: Path) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    rows: list[dict[str, Any]] = []
    lines = read_text(path).splitlines()
    display = _display_path(path, root)
    for index, line in enumerate(lines):
        if not is_markdown_separator(line):
            continue
        if index == 0 or not lines[index - 1].lstrip().startswith("|"):
            continue
        headers = [normalize_header(cell) for cell in split_markdown_row(lines[index - 1])]
        for row_line in lines[index + 1 :]:
            if not row_line.lstrip().startswith("|") or is_markdown_separator(row_line):
                break
            cells = split_markdown_row(row_line)
            if len(cells) != len(headers):
                continue
            row = dict(zip(headers, cells))
            if "id" in row and "prompt_id" not in row:
                row["prompt_id"] = row["id"]
            if "top_retrieved" in row and "top_retrieved_guide_ids" not in row:
                row["top_retrieved_guide_ids"] = row["top_retrieved"]
            if "expected" in row and "expected_guide_ids" not in row:
                row["expected_guide_ids"] = row["expected"]
            row["_eval_path"] = display
            rows.append(row)
    return rows, []


def is_markdown_separator(line: str) -> bool:
    text = line.strip()
    if not text.startswith("|") or not text.endswith("|"):
        return False
    return all(char in {"|", "-", ":", " "} for char in text)


def split_markdown_row(line: str) -> list[str]:
    text = line.strip()
    if text.startswith("|"):
        text = text[1:]
    if text.endswith("|"):
        text = text[:-1]
    cells: list[str] = []
    current: list[str] = []
    escaped = False
    for char in text:
        if char == "\\" and not escaped:
            escaped = True
            continue
        if char == "|" and not escaped:
            cells.append("".join(current).strip())
            current = []
            continue
        current.append(char)
        escaped = False
    cells.append("".join(current).strip())
    return cells


def normalize_header(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", value.lower()).strip("_")


def retrieval_eval_issues(
    eval_rows: Sequence[Mapping[str, Any]],
    *,
    prompt_expectations: Mapping[str, Sequence[str]],
    prompt_primary_expectations: Mapping[str, Sequence[str]] | None = None,
    top_k: int | None = None,
) -> list[dict[str, Any]]:
    issues: list[dict[str, Any]] = []
    prompt_primary_expectations = prompt_primary_expectations or {}
    for index, row in enumerate(eval_rows, start=1):
        prompt_id = first_present(row, ("prompt_id", "id", "case_id", "label"))
        expected = expected_ids_from_record(row)
        if not expected and prompt_id:
            expected = list(prompt_expectations.get(prompt_id) or [])
        primary_expected = primary_expected_ids_from_record(row)
        if not primary_expected and row_has_primary_expectation_metadata(row) and prompt_id:
            primary_expected = list(prompt_primary_expectations.get(prompt_id) or [])
        retrieved = retrieved_ids_from_row(row)
        if top_k is not None and top_k >= 0:
            retrieved = retrieved[:top_k]
        if not prompt_id:
            issues.append(
                issue(
                    "warning",
                    "retrieval_eval_missing_prompt_id",
                    "Retrieval eval row has no prompt id; expected-owner drift cannot be matched to a prompt.",
                    path=_clean_text(row.get("_eval_path")),
                    line=index,
                )
            )
            continue
        if not expected:
            continue
        if not retrieved:
            issues.append(
                issue(
                    "warning",
                    "retrieval_eval_missing_top_k",
                    "Retrieval eval row has expected owner metadata but no retrieved top-k guide ids.",
                    path=_clean_text(row.get("_eval_path")),
                    line=index,
                    prompt_id=prompt_id,
                    guide_ids=expected,
                )
            )
            continue
        if not (set(expected) & set(retrieved)):
            issues.append(
                issue(
                    "warning",
                    "retrieval_missing_expected_owner",
                    "Expected guide owner never appears in retrieved top-k.",
                    path=_clean_text(row.get("_eval_path")),
                    line=index,
                    prompt_id=prompt_id,
                    guide_ids=expected,
                    details={"top_retrieved_guide_ids": retrieved},
                )
            )
        issues.extend(
            primary_retrieval_eval_issues(
                row,
                row_index=index,
                prompt_id=prompt_id,
                primary_expected=primary_expected,
                retrieved=retrieved,
                top_k=top_k,
            )
        )
    return issues


def row_has_primary_expectation_metadata(row: Mapping[str, Any]) -> bool:
    return any(str(key).lower() in PRIMARY_EXPECTED_GUIDE_KEYS for key in row)


def primary_retrieval_eval_issues(
    row: Mapping[str, Any],
    *,
    row_index: int,
    prompt_id: str,
    primary_expected: Sequence[str],
    retrieved: Sequence[str],
    top_k: int | None,
) -> list[dict[str, Any]]:
    if not primary_expected:
        return []

    hit_key = primary_hit_key(top_k)
    reported_hit = bool_from_row_value(row.get(hit_key))
    if reported_hit is True:
        return []

    if reported_hit is None and retrieved and set(primary_expected) & set(retrieved):
        return []

    details: dict[str, Any] = {
        "top_retrieved_guide_ids": list(retrieved),
        "primary_hit_field": hit_key,
    }
    for key in ("primary_best_rank", "primary_owner_best_rank"):
        if key in row:
            details[key] = row.get(key)
    if reported_hit is not None:
        details["reported_primary_hit"] = reported_hit

    return [
        issue(
            "warning",
            "retrieval_missing_primary_expected_owner",
            "Primary expected guide owner never appears in retrieved top-k.",
            path=_clean_text(row.get("_eval_path")),
            line=row_index,
            prompt_id=prompt_id,
            guide_ids=primary_expected,
            details=details,
        )
    ]


def primary_hit_key(top_k: int | None) -> str:
    if top_k is not None:
        return PRIMARY_HIT_KEYS_BY_TOP_K.get(top_k, "primary_hit_at_k")
    return "primary_hit_at_k"


def bool_from_row_value(value: Any) -> bool | None:
    if isinstance(value, bool):
        return value
    if value is None:
        return None
    text = _clean_text(value).lower()
    if not text:
        return None
    if text in {"1", "true", "t", "yes", "y"}:
        return True
    if text in {"0", "false", "f", "no", "n"}:
        return False
    return None


def retrieved_ids_from_row(row: Mapping[str, Any]) -> list[str]:
    values: list[str] = []
    for key in RETRIEVED_GUIDE_KEYS:
        if key in row:
            ids, _malformed = extract_guide_ids(row.get(key))
            values.extend(ids)
    retrieval_meta = row.get("retrieval_metadata")
    if isinstance(retrieval_meta, Mapping):
        for key in RETRIEVED_GUIDE_KEYS:
            if key in retrieval_meta:
                ids, _malformed = extract_guide_ids(retrieval_meta.get(key))
                values.extend(ids)
    return dedupe(values)


def issue(
    severity: str,
    code: str,
    message: str,
    *,
    path: str = "",
    line: int | None = None,
    prompt_id: str = "",
    guide_ids: Sequence[str] | None = None,
    field: str = "",
    details: Mapping[str, Any] | None = None,
) -> dict[str, Any]:
    return {
        "severity": severity,
        "code": code,
        "message": message,
        "path": path,
        "line": line,
        "prompt_id": prompt_id,
        "guide_ids": list(guide_ids or []),
        "field": field,
        "details": dict(details or {}),
    }


def validate(
    prompt_packs: Sequence[Path],
    *,
    guides_dir: Path = DEFAULT_GUIDES_DIR,
    root: Path | None = None,
    allowed_drift_manifest: Path | None = None,
    retrieval_eval_paths: Sequence[Path] = (),
    retrieval_top_k: int | None = None,
) -> dict[str, Any]:
    root = (root or Path.cwd()).resolve()
    catalog, catalog_issues = load_guide_catalog(guides_dir, root=root)
    records: list[PromptRecord] = []
    issues: list[dict[str, Any]] = list(catalog_issues)
    for path in prompt_packs:
        loaded, pack_issues = load_prompt_pack(path, root=root)
        records.extend(loaded)
        issues.extend(pack_issues)
    issues.extend(validate_prompt_records(records, guide_catalog=catalog))

    prompt_expectations: dict[str, list[str]] = {}
    prompt_primary_expectations: dict[str, list[str]] = {}
    for record in records:
        if record.prompt_id:
            prompt_expectations[record.prompt_id] = expected_ids_from_record(record.data)
            prompt_primary_expectations[record.prompt_id] = primary_expected_ids_from_record(record.data)

    eval_rows: list[dict[str, Any]] = []
    for path in retrieval_eval_paths:
        loaded_rows, eval_load_issues = load_retrieval_eval(path, root=root)
        eval_rows.extend(loaded_rows)
        issues.extend(eval_load_issues)
    issues.extend(
        retrieval_eval_issues(
            eval_rows,
            prompt_expectations=prompt_expectations,
            prompt_primary_expectations=prompt_primary_expectations,
            top_k=retrieval_top_k,
        )
    )

    try:
        suppressions = load_allowed_drift(allowed_drift_manifest)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        suppressions = []
        issues.append(
            issue(
                "error",
                "invalid_allowed_drift_manifest",
                f"Could not load allowed-drift manifest: {exc}",
                path=str(allowed_drift_manifest or ""),
            )
        )
    active_issues, suppressed_issues = apply_suppressions(issues, suppressions)

    summary = build_summary(
        records=records,
        prompt_packs=prompt_packs,
        guide_catalog=catalog,
        issues=active_issues,
        suppressed_issues=suppressed_issues,
        eval_rows=eval_rows,
    )
    return {
        "schema_version": REPORT_SCHEMA_VERSION,
        "report_type": REPORT_TYPE,
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "status": summary["status"],
        "config": {
            "prompt_packs": [_display_path(path, root) for path in prompt_packs],
            "guides_dir": _display_path(guides_dir, root),
            "allowed_drift_manifest": (
                _display_path(allowed_drift_manifest, root)
                if allowed_drift_manifest
                else ""
            ),
            "retrieval_eval_paths": [
                _display_path(path, root) for path in retrieval_eval_paths
            ],
            "retrieval_top_k": retrieval_top_k,
        },
        "summary": summary,
        "issues": active_issues,
        "suppressed_issues": suppressed_issues,
    }


def build_summary(
    *,
    records: Sequence[PromptRecord],
    prompt_packs: Sequence[Path],
    guide_catalog: Mapping[str, Any],
    issues: Sequence[Mapping[str, Any]],
    suppressed_issues: Sequence[Mapping[str, Any]],
    eval_rows: Sequence[Mapping[str, Any]],
) -> dict[str, Any]:
    severities = Counter(_clean_text(item.get("severity")) for item in issues)
    expected_rows = sum(1 for record in records if expected_ids_from_record(record.data))
    expected_ids = []
    for record in records:
        expected_ids.extend(expected_ids_from_record(record.data))
    status = "fail" if severities.get("error") else "warn" if severities.get("warning") else "pass"
    return {
        "status": status,
        "prompt_packs_checked": len(prompt_packs),
        "prompts_checked": len(records),
        "prompt_ids_seen": sum(1 for record in records if record.prompt_id),
        "expected_owner_rows": expected_rows,
        "unique_expected_guide_ids": len(set(expected_ids)),
        "guide_catalog_ids": len(guide_catalog),
        "retrieval_eval_rows": len(eval_rows),
        "errors": severities.get("error", 0),
        "warnings": severities.get("warning", 0),
        "suppressed_issues": len(suppressed_issues),
        "issue_counts_by_code": dict(Counter(item.get("code", "") for item in issues)),
    }


def render_markdown(report: Mapping[str, Any], *, limit: int = 200) -> str:
    summary = report["summary"]
    lines = [
        "# Prompt Expectation Validation",
        "",
        f"- Generated at: `{report['generated_at']}`",
        f"- Status: `{report['status']}`",
        f"- Prompt packs checked: `{summary['prompt_packs_checked']}`",
        f"- Prompts checked: `{summary['prompts_checked']}`",
        f"- Expected-owner rows: `{summary['expected_owner_rows']}`",
        f"- Guide catalog IDs: `{summary['guide_catalog_ids']}`",
        f"- Retrieval eval rows: `{summary['retrieval_eval_rows']}`",
        f"- Errors: `{summary['errors']}`",
        f"- Warnings: `{summary['warnings']}`",
        f"- Suppressed issues: `{summary['suppressed_issues']}`",
        "",
        "## Issue Counts",
        "",
        "| Code | Count |",
        "| --- | ---: |",
    ]
    counts = summary.get("issue_counts_by_code") or {}
    if counts:
        for code, count in sorted(counts.items()):
            lines.append(f"| `{escape_md(code)}` | {count} |")
    else:
        lines.append("| `none` | 0 |")

    issues = list(report.get("issues") or [])
    lines.extend(
        [
            "",
            f"## First {min(limit, len(issues))} Active Issues",
            "",
            "| Severity | Code | Prompt | Location | Guides | Message |",
            "| --- | --- | --- | --- | --- | --- |",
        ]
    )
    if issues:
        for item in issues[:limit]:
            location = item.get("path") or ""
            if item.get("line") is not None:
                location = f"{location}:{item.get('line')}"
            lines.append(
                "| `{severity}` | `{code}` | `{prompt}` | `{location}` | `{guides}` | {message} |".format(
                    severity=escape_md(item.get("severity", "")),
                    code=escape_md(item.get("code", "")),
                    prompt=escape_md(item.get("prompt_id", "")),
                    location=escape_md(location),
                    guides=escape_md(",".join(item.get("guide_ids") or [])),
                    message=escape_md(item.get("message", "")),
                )
            )
    else:
        lines.append("| `none` | `none` |  |  |  | No active issues. |")
    if len(issues) > limit:
        lines.append(f"| ... | ... | ... | ... | ... | {len(issues) - limit} more issues omitted |")
    return "\n".join(lines) + "\n"


def render_text(report: Mapping[str, Any], *, limit: int = 50) -> str:
    summary = report["summary"]
    lines = [
        f"Prompt expectation validation: {report['status']}",
        f"prompts={summary['prompts_checked']} expected_rows={summary['expected_owner_rows']} "
        f"errors={summary['errors']} warnings={summary['warnings']} suppressed={summary['suppressed_issues']}",
    ]
    for item in list(report.get("issues") or [])[:limit]:
        location = item.get("path") or ""
        if item.get("line") is not None:
            location = f"{location}:{item.get('line')}"
        lines.append(
            "[{severity}] {code} {prompt} {location}: {message}".format(
                severity=item.get("severity"),
                code=item.get("code"),
                prompt=item.get("prompt_id") or "",
                location=location,
                message=item.get("message"),
            ).strip()
        )
    return "\n".join(lines) + "\n"


def escape_md(value: Any) -> str:
    return str(value).replace("\n", " ").replace("|", "\\|")


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "prompt_packs",
        nargs="*",
        type=Path,
        help="JSONL/CSV prompt packs to validate. Defaults to --prompts-dir discovery.",
    )
    parser.add_argument("--prompts-dir", type=Path, default=DEFAULT_PROMPTS_DIR)
    parser.add_argument("--guides-dir", type=Path, default=DEFAULT_GUIDES_DIR)
    parser.add_argument("--allowed-drift-manifest", type=Path, default=None)
    parser.add_argument(
        "--retrieval-eval",
        action="append",
        type=Path,
        default=[],
        help="Optional retrieval-only eval JSON/Markdown report to cross-check top-k owner hits.",
    )
    parser.add_argument(
        "--retrieval-top-k",
        type=int,
        default=None,
        help="Limit retrieved guide IDs inspected from retrieval eval rows.",
    )
    parser.add_argument("--output-json", type=Path, default=None)
    parser.add_argument("--output-md", type=Path, default=None)
    parser.add_argument("--output-text", type=Path, default=None)
    parser.add_argument("--markdown-limit", type=int, default=200)
    parser.add_argument("--text-limit", type=int, default=50)
    parser.add_argument("--fail-on-errors", action="store_true")
    parser.add_argument("--fail-on-warnings", action="store_true")
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    prompt_packs = list(args.prompt_packs) or discover_prompt_packs(args.prompts_dir)
    report = validate(
        prompt_packs,
        guides_dir=args.guides_dir,
        allowed_drift_manifest=args.allowed_drift_manifest,
        retrieval_eval_paths=args.retrieval_eval,
        retrieval_top_k=args.retrieval_top_k,
    )
    markdown = render_markdown(report, limit=max(0, args.markdown_limit))
    text = render_text(report, limit=max(0, args.text_limit))

    if args.output_json:
        args.output_json.parent.mkdir(parents=True, exist_ok=True)
        args.output_json.write_text(
            json.dumps(report, indent=2, sort_keys=True) + "\n",
            encoding="utf-8",
        )
    if args.output_md:
        args.output_md.parent.mkdir(parents=True, exist_ok=True)
        args.output_md.write_text(markdown, encoding="utf-8")
    if args.output_text:
        args.output_text.parent.mkdir(parents=True, exist_ok=True)
        args.output_text.write_text(text, encoding="utf-8")
    if not args.output_json and not args.output_md and not args.output_text:
        print(text, end="")

    errors = report["summary"]["errors"]
    warnings = report["summary"]["warnings"]
    if args.fail_on_errors and errors:
        return 1
    if args.fail_on_warnings and (errors or warnings):
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
