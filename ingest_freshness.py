"""Guide/index freshness checks shared by query and validation tooling."""

from __future__ import annotations

import hashlib
import json
import os
from dataclasses import dataclass, field
from typing import Any

import yaml

import config


FRESH = "fresh"
STALE = "stale"
INCOMPLETE_UNTRUSTED = "incomplete_untrusted"
ABSENT_OR_INVALID = "absent_or_invalid"


@dataclass(frozen=True)
class IngestFreshnessReport:
    status: str
    message: str
    guide_count: int = 0
    manifest_count: int = 0
    missing_guide_ids: tuple[str, ...] = field(default_factory=tuple)
    changed_guide_ids: tuple[str, ...] = field(default_factory=tuple)
    extra_manifest_keys: tuple[str, ...] = field(default_factory=tuple)

    @property
    def is_blocking(self) -> bool:
        return self.status == STALE


def file_sha256(filepath: str) -> str:
    digest = hashlib.sha256()
    with open(filepath, "rb") as handle:
        for block in iter(lambda: handle.read(65536), b""):
            digest.update(block)
    return digest.hexdigest()


def parse_frontmatter(text: str) -> dict[str, Any] | None:
    text = text.lstrip("\ufeff")
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        return None
    closing_index = None
    for index in range(1, len(lines)):
        if lines[index].strip() == "---":
            closing_index = index
            break
    if closing_index is None:
        return None
    try:
        meta = yaml.safe_load("\n".join(lines[1:closing_index]))
    except yaml.YAMLError:
        return None
    return meta if isinstance(meta, dict) else None


def collect_guide_file_info(compendium_dir: str = config.COMPENDIUM_DIR) -> dict[str, dict[str, str]]:
    file_info_by_guide_id: dict[str, dict[str, str]] = {}
    for root, _dirs, files in os.walk(compendium_dir):
        for filename in files:
            if not filename.endswith(".md"):
                continue
            path = os.path.join(root, filename)
            try:
                with open(path, "r", encoding="utf-8") as handle:
                    meta = parse_frontmatter(handle.read())
            except OSError:
                continue
            if not meta:
                continue
            guide_id = str(meta.get("id", "")).strip()
            if not guide_id:
                continue
            file_info_by_guide_id[guide_id] = {
                "basename": os.path.basename(path),
                "path": path,
                "sha256": file_sha256(path),
            }
    return file_info_by_guide_id


def load_manifest(path: str) -> tuple[dict[str, Any] | None, str]:
    if not os.path.isfile(path):
        return None, "manifest file is missing"
    try:
        with open(path, "r", encoding="utf-8") as handle:
            data = json.load(handle)
    except (json.JSONDecodeError, OSError) as exc:
        return None, str(exc)
    if not isinstance(data, dict):
        return None, "manifest root is not an object"
    return data, ""


def _manifest_path_basename(value: Any) -> str:
    return os.path.basename(str(value).replace("\\", "/"))


def normalize_manifest(
    manifest: dict[str, Any],
    file_info_by_guide_id: dict[str, dict[str, str]],
) -> tuple[dict[str, dict[str, str]], tuple[str, ...]]:
    normalized: dict[str, dict[str, str]] = {}
    unmatched: list[str] = []
    basename_to_guide_ids: dict[str, list[str]] = {}
    for guide_id, info in file_info_by_guide_id.items():
        basename_to_guide_ids.setdefault(info["basename"], []).append(guide_id)
    basename_to_guide_id = {
        basename: guide_ids[0]
        for basename, guide_ids in basename_to_guide_ids.items()
        if len(guide_ids) == 1
    }
    sha_to_guide_ids: dict[str, list[str]] = {}
    for guide_id, info in file_info_by_guide_id.items():
        sha = info.get("sha256", "")
        if sha:
            sha_to_guide_ids.setdefault(sha, []).append(guide_id)

    for key, raw_entry in manifest.items():
        key_text = str(key)
        if key_text.startswith("_"):
            continue
        entry = raw_entry if isinstance(raw_entry, dict) else {"sha256": str(raw_entry)}
        sha = str(entry.get("sha256", ""))
        source_basename = _manifest_path_basename(entry.get("source_file") or key_text)
        key_basename = _manifest_path_basename(key_text)
        guide_id = ""
        if key_text in file_info_by_guide_id:
            guide_id = key_text
        elif source_basename in basename_to_guide_id:
            guide_id = basename_to_guide_id[source_basename]
        elif key_basename in basename_to_guide_id:
            guide_id = basename_to_guide_id[key_basename]
        elif sha and sha_to_guide_ids.get(sha):
            guide_id = sha_to_guide_ids[sha].pop(0)

        if not guide_id:
            unmatched.append(key_text)
            continue

        info = file_info_by_guide_id[guide_id]
        normalized[guide_id] = {
            "guide_id": guide_id,
            "source_file": str(entry.get("source_file") or info["basename"]),
            "sha256": sha,
        }

    return normalized, tuple(sorted(unmatched))


def _missing_manifest_is_trusted(manifest_count: int, guide_count: int) -> bool:
    if guide_count == 0:
        return False
    tolerance = max(1, int(guide_count * 0.05))
    return manifest_count >= guide_count - tolerance


def evaluate_ingest_freshness(
    *,
    compendium_dir: str = config.COMPENDIUM_DIR,
    manifest_path: str | None = None,
) -> IngestFreshnessReport:
    manifest_path = manifest_path or os.path.join(config.CHROMA_DB_DIR, "ingest_manifest.json")
    guide_info = collect_guide_file_info(compendium_dir)
    manifest, manifest_error = load_manifest(manifest_path)
    if manifest is None:
        return IngestFreshnessReport(
            status=ABSENT_OR_INVALID,
            message=f"Ingest manifest is unavailable or invalid: {manifest_error}.",
            guide_count=len(guide_info),
        )

    normalized, extra_keys = normalize_manifest(manifest, guide_info)
    guide_ids = set(guide_info)
    manifest_ids = set(normalized)
    missing = tuple(sorted(guide_ids - manifest_ids))
    changed = tuple(
        sorted(
            guide_id
            for guide_id in guide_ids & manifest_ids
            if normalized[guide_id].get("sha256") != guide_info[guide_id].get("sha256")
        )
    )
    manifest_count = len(manifest_ids)

    if missing and not _missing_manifest_is_trusted(manifest_count, len(guide_ids)):
        return IngestFreshnessReport(
            status=INCOMPLETE_UNTRUSTED,
            message=(
                "Ingest manifest is incomplete for the current corpus; "
                "freshness cannot be trusted until a rebuild refreshes it."
            ),
            guide_count=len(guide_ids),
            manifest_count=manifest_count,
            missing_guide_ids=missing,
            changed_guide_ids=changed,
            extra_manifest_keys=extra_keys,
        )

    if missing or changed or extra_keys:
        parts = []
        if missing:
            parts.append(f"{len(missing)} missing guide manifest entr{'y' if len(missing) == 1 else 'ies'}")
        if changed:
            parts.append(f"{len(changed)} changed guide SHA entr{'y' if len(changed) == 1 else 'ies'}")
        if extra_keys:
            parts.append(f"{len(extra_keys)} extra manifest key{'s' if len(extra_keys) != 1 else ''}")
        return IngestFreshnessReport(
            status=STALE,
            message="Ingest manifest is stale: " + ", ".join(parts) + ".",
            guide_count=len(guide_ids),
            manifest_count=manifest_count,
            missing_guide_ids=missing,
            changed_guide_ids=changed,
            extra_manifest_keys=extra_keys,
        )

    return IngestFreshnessReport(
        status=FRESH,
        message="Ingest manifest matches current guide files.",
        guide_count=len(guide_ids),
        manifest_count=manifest_count,
    )
