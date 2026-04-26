#!/usr/bin/env python3
"""Plan report-only artifact retention actions without deleting files."""

from __future__ import annotations

import argparse
import fnmatch
import json
import re
from collections import defaultdict
from dataclasses import dataclass, field
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable


DEFAULT_REFERENCE_ROOTS = (Path("notes/dispatch"), Path("notes/specs"))
DEFAULT_MANIFESTS = (Path("artifacts/runs/run_manifest.jsonl"),)
DEFAULT_ARCHIVE_AFTER_DAYS = 14
DEFAULT_DELETE_AFTER_DAYS = 45
DEFAULT_LARGE_FAMILY_BYTES = 500 * 1024 * 1024
DEFAULT_REPORT_LIMIT = 40

CONTAINER_DIRS = {
    "bench",
    "external_review",
    "instrumented_ui_smoke",
    "live_debug",
    "mobile_pack",
    "prompt_packs",
    "prompts",
    "runs",
    "snapshots",
    "ui_validation",
}

GENERATED_MARKERS = {
    "__pycache__",
    ".cache",
    ".pytest_cache",
    "bench",
    "build",
    "cache",
    "generated",
    "out",
    "output",
    "outputs",
    "prompt_packs",
    "prompts",
    "reports",
    "tmp",
}

GENERATED_SUFFIX_MARKERS = (
    "_diag",
    "_diagnostic",
    "_diagnostics",
    "_harness",
    "_matrix",
    "_output",
    "_outputs",
    "_report",
    "_reports",
    "_review",
    "_smoke",
)

DELETE_NAME_PATTERNS = (
    "__pycache__",
    ".pytest_cache",
    ".cache",
    "/cache/",
    "/tmp/",
    "/temp/",
    "scratch",
    "stale",
)

ARCHIVE_NAME_PATTERNS = (
    "diagnostic",
    "diagnostics",
    "harness",
    "matrix",
    "prompt",
    "report",
    "review",
    "smoke",
    "validation",
)

ARTIFACT_REF_RE = re.compile(r"(?:^|[\s`'\"(])((?:[A-Za-z]:)?[^`'\"\s)]*artifacts/[^\s`'\")]+)")
DATE_TOKEN_RE = re.compile(r"([_-])20\d{6}(?:[_-]\d{4,6})?(?:[_-]\d{3,6})?")
HEX_TOKEN_RE = re.compile(r"([_-])[0-9a-f]{12,64}(?=([_.-]|$))", re.IGNORECASE)


@dataclass
class Family:
    path: str
    family_group: str
    bytes: int = 0
    file_count: int = 0
    dir_count: int = 0
    oldest_mtime: float | None = None
    newest_mtime: float | None = None
    markers: set[str] = field(default_factory=set)
    examples: list[str] = field(default_factory=list)

    def update_mtime(self, mtime: float) -> None:
        if self.oldest_mtime is None or mtime < self.oldest_mtime:
            self.oldest_mtime = mtime
        if self.newest_mtime is None or mtime > self.newest_mtime:
            self.newest_mtime = mtime


def plan_artifact_retention(
    root: str | Path = "artifacts",
    *,
    reference_roots: Iterable[str | Path] = DEFAULT_REFERENCE_ROOTS,
    manifest_paths: Iterable[str | Path] = DEFAULT_MANIFESTS,
    protect_paths: Iterable[str] = (),
    protect_globs: Iterable[str] = (),
    archive_after_days: int = DEFAULT_ARCHIVE_AFTER_DAYS,
    delete_after_days: int = DEFAULT_DELETE_AFTER_DAYS,
    large_family_bytes: int = DEFAULT_LARGE_FAMILY_BYTES,
    now: datetime | None = None,
    report_limit: int = DEFAULT_REPORT_LIMIT,
) -> dict[str, Any]:
    """Return a dry-run artifact retention plan.

    Candidate artifact files are inspected only through paths and stat metadata.
    Reference roots and run manifests are read so they can protect mentioned
    artifact paths from archive/delete recommendations.
    """
    root_path = Path(root)
    now_dt = now or datetime.now(timezone.utc)
    if now_dt.tzinfo is None:
        now_dt = now_dt.replace(tzinfo=timezone.utc)
    now_ts = now_dt.timestamp()

    families = _collect_families(root_path)
    references = collect_protection_references(
        root_path,
        reference_roots=reference_roots,
        manifest_paths=manifest_paths,
        protect_paths=protect_paths,
        protect_globs=protect_globs,
    )
    rows = [
        _classify_family(
            family,
            references,
            protect_globs=tuple(protect_globs),
            now_ts=now_ts,
            archive_after_days=archive_after_days,
            delete_after_days=delete_after_days,
            large_family_bytes=large_family_bytes,
        )
        for family in families.values()
    ]
    rows.sort(key=lambda row: (_action_rank(row["action"]), -int(row["bytes"]), row["path"]))

    action_counts = defaultdict(int)
    total_bytes = 0
    protected_bytes = 0
    candidate_bytes = 0
    for row in rows:
        action_counts[row["action"]] += 1
        total_bytes += int(row["bytes"])
        if row["protected"]:
            protected_bytes += int(row["bytes"])
        if row["action"] in {"archive_candidate", "delete_candidate"}:
            candidate_bytes += int(row["bytes"])

    return {
        "dry_run": True,
        "root": root_path.as_posix(),
        "exists": root_path.exists(),
        "generated_at": now_dt.isoformat(timespec="seconds"),
        "config": {
            "archive_after_days": archive_after_days,
            "delete_after_days": delete_after_days,
            "large_family_bytes": large_family_bytes,
            "report_limit": report_limit,
            "reference_roots": [Path(path).as_posix() for path in reference_roots],
            "manifest_paths": [Path(path).as_posix() for path in manifest_paths],
            "protect_paths": list(protect_paths),
            "protect_globs": list(protect_globs),
        },
        "summary": {
            "family_count": len(rows),
            "total_bytes": total_bytes,
            "protected_bytes": protected_bytes,
            "candidate_bytes": candidate_bytes,
            "action_counts": dict(sorted(action_counts.items())),
            "reference_count": len(references["paths"]),
            "reference_source_count": len(references["sources"]),
        },
        "references": {
            "paths": sorted(references["paths"]),
            "sources": sorted(references["sources"]),
        },
        "families": rows,
        "candidates": [
            row for row in rows if row["action"] in {"archive_candidate", "delete_candidate"}
        ],
    }


def collect_protection_references(
    artifact_root: Path,
    *,
    reference_roots: Iterable[str | Path],
    manifest_paths: Iterable[str | Path],
    protect_paths: Iterable[str],
    protect_globs: Iterable[str],
) -> dict[str, Any]:
    paths: dict[str, set[str]] = defaultdict(set)
    sources: set[str] = set()

    for raw_path in protect_paths:
        rel = _normalize_artifact_ref(raw_path, artifact_root)
        if rel:
            paths[rel].add("explicit:protect-path")
            sources.add("explicit:protect-path")

    for pattern in protect_globs:
        sources.add(f"explicit:protect-glob:{pattern}")

    for reference_root in reference_roots:
        root_path = Path(reference_root)
        if not root_path.exists():
            continue
        files = [root_path] if root_path.is_file() else sorted(root_path.rglob("*"))
        for path in files:
            if not path.is_file():
                continue
            if path.suffix.lower() not in {".md", ".txt", ".json", ".jsonl"}:
                continue
            _collect_refs_from_text_file(path, artifact_root, paths, sources)

    for manifest_path in manifest_paths:
        path = Path(manifest_path)
        if not path.exists() or not path.is_file():
            continue
        _collect_refs_from_manifest(path, artifact_root, paths, sources)

    return {
        "paths": {path: sorted(source_set) for path, source_set in paths.items()},
        "sources": sources,
    }


def render_markdown(plan: dict[str, Any], *, limit: int | None = None) -> str:
    """Render a compact Markdown retention report."""
    summary = plan["summary"]
    config = plan["config"]
    limit = int(limit if limit is not None else config.get("report_limit", DEFAULT_REPORT_LIMIT))
    families = plan["families"][:limit]
    candidates = plan["candidates"][:limit]

    lines = [
        "# Artifact Retention Plan",
        "",
        "- Mode: report-only dry run",
        f"- Root: `{plan['root']}`",
        f"- Generated: `{plan['generated_at']}`",
        f"- Families: {summary['family_count']}",
        f"- Total bytes: {summary['total_bytes']}",
        f"- Protected bytes: {summary['protected_bytes']}",
        f"- Candidate bytes: {summary['candidate_bytes']}",
        f"- Action counts: {_format_counts(summary['action_counts'])}",
        f"- Protection references: {summary['reference_count']} paths from {summary['reference_source_count']} sources",
        "",
        "## Candidates",
        "",
        "| Action | Path | Group | Bytes | Files | Age days | Reasons | Protected |",
        "| --- | --- | --- | ---: | ---: | ---: | --- | --- |",
    ]
    if not candidates:
        lines.append("| - | - | - | 0 | 0 | 0 | - | - |")
    else:
        for row in candidates:
            lines.append(_format_family_row(row))

    lines.extend(
        [
            "",
            "## Family Overview",
            "",
            "| Action | Path | Group | Bytes | Files | Age days | Reasons | Protected |",
            "| --- | --- | --- | ---: | ---: | ---: | --- | --- |",
        ]
    )
    if not families:
        lines.append("| - | - | - | 0 | 0 | 0 | - | - |")
    else:
        for row in families:
            lines.append(_format_family_row(row))

    if len(plan["families"]) > limit:
        lines.extend(["", f"_Rows truncated to {limit} of {len(plan['families'])} families._"])

    lines.extend(
        [
            "",
            "## Notes",
            "",
            "- This planner never deletes, moves, archives, or rewrites candidate artifacts.",
            "- Protection applies when a reference overlaps a family path, including references to files inside that family.",
            "- Candidate artifact bodies are not read; only paths and stat metadata are inspected.",
        ]
    )
    return "\n".join(lines) + "\n"


def write_reports(plan: dict[str, Any], *, output_json: str | Path | None, output_md: str | Path | None) -> None:
    if output_json:
        path = Path(output_json)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(plan, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    if output_md:
        path = Path(output_md)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(render_markdown(plan), encoding="utf-8")


def _collect_families(root_path: Path) -> dict[str, Family]:
    families: dict[str, Family] = {}
    if not root_path.exists():
        return families

    seen_dirs: list[tuple[str, float]] = []
    for path in sorted(root_path.rglob("*")):
        try:
            stat = path.stat()
        except OSError:
            continue
        rel_path = path.relative_to(root_path).as_posix()
        if path.is_dir():
            seen_dirs.append((rel_path, stat.st_mtime))
            continue
        if not path.is_file():
            continue
        family_path = infer_family_path(rel_path)
        family = families.setdefault(
            family_path,
            Family(path=family_path, family_group=normalize_family_group(family_path)),
        )
        family.bytes += stat.st_size
        family.file_count += 1
        family.update_mtime(stat.st_mtime)
        family.markers.update(_generated_markers(family_path))
        family.markers.update(_generated_markers(rel_path))
        if len(family.examples) < 5:
            family.examples.append(rel_path)

    for rel_path, mtime in seen_dirs:
        family_path = infer_family_path(rel_path)
        family = families.get(family_path)
        if family is None and _generated_markers(rel_path) and rel_path.lower() not in CONTAINER_DIRS:
            family = families.setdefault(
                family_path,
                Family(path=family_path, family_group=normalize_family_group(family_path)),
            )
        if family is None:
            continue
        family.dir_count += 1
        if family.file_count == 0:
            family.update_mtime(mtime)
        family.markers.update(_generated_markers(rel_path))

    return families


def infer_family_path(rel_path: str) -> str:
    parts = Path(rel_path).parts
    if not parts:
        return "."
    if len(parts) == 1:
        return parts[0]
    first = parts[0].lower()
    if first in CONTAINER_DIRS:
        return "/".join(parts[:2])
    if first in GENERATED_MARKERS or first.startswith(("tmp", "temp")):
        return parts[0]
    return parts[0]


def normalize_family_group(family_path: str) -> str:
    parts = family_path.split("/")
    name = parts[-1]
    name = DATE_TOKEN_RE.sub(r"\1<date>", name)
    name = HEX_TOKEN_RE.sub(r"\1<hash>", name)
    parts[-1] = name
    return "/".join(parts)


def _classify_family(
    family: Family,
    references: dict[str, Any],
    *,
    protect_globs: tuple[str, ...],
    now_ts: float,
    archive_after_days: int,
    delete_after_days: int,
    large_family_bytes: int,
) -> dict[str, Any]:
    newest = family.newest_mtime or now_ts
    age_days = max(0.0, (now_ts - newest) / 86400)
    protected_sources = _protection_sources(family.path, references, protect_globs)
    protected = bool(protected_sources)
    reasons = sorted(_classification_reasons(family, age_days, archive_after_days, delete_after_days, large_family_bytes))

    if protected:
        action = "keep_protected"
    elif "empty_generated_family" in reasons:
        action = "delete_candidate"
    elif any(reason.startswith("delete_name_pattern:") for reason in reasons) and age_days >= delete_after_days:
        action = "delete_candidate"
    elif "older_than_delete_threshold" in reasons and any(
        marker in family.markers
        for marker in {"tmp", "tmp-prefix", ".cache", ".pytest_cache", "__pycache__", "cache"}
    ):
        action = "delete_candidate"
    elif any(
        reason in reasons
        for reason in ("older_than_archive_threshold", "larger_than_large_family_threshold")
    ) or any(reason.startswith("archive_name_pattern:") for reason in reasons):
        action = "archive_candidate"
    else:
        action = "keep"

    return {
        "path": family.path,
        "family_group": family.family_group,
        "bytes": family.bytes,
        "files": family.file_count,
        "dirs": family.dir_count,
        "oldest_mtime": _format_ts(family.oldest_mtime),
        "newest_mtime": _format_ts(family.newest_mtime),
        "age_days": round(age_days, 2),
        "markers": sorted(family.markers),
        "examples": family.examples,
        "protected": protected,
        "protection_sources": protected_sources,
        "action": action,
        "reasons": reasons,
    }


def _classification_reasons(
    family: Family,
    age_days: float,
    archive_after_days: int,
    delete_after_days: int,
    large_family_bytes: int,
) -> set[str]:
    reasons: set[str] = set()
    haystack = f"/{family.path.lower()}/"
    if family.file_count == 0 and family.markers:
        reasons.add("empty_generated_family")
    if age_days >= archive_after_days:
        reasons.add("older_than_archive_threshold")
    if age_days >= delete_after_days:
        reasons.add("older_than_delete_threshold")
    if family.bytes >= large_family_bytes:
        reasons.add("larger_than_large_family_threshold")
    for pattern in DELETE_NAME_PATTERNS:
        if pattern in haystack:
            reasons.add(f"delete_name_pattern:{pattern.strip('/')}")
    for pattern in ARCHIVE_NAME_PATTERNS:
        if pattern in haystack:
            reasons.add(f"archive_name_pattern:{pattern}")
    for marker in sorted(family.markers):
        reasons.add(f"generated_marker:{marker}")
    return reasons


def _protection_sources(
    family_path: str,
    references: dict[str, Any],
    protect_globs: tuple[str, ...],
) -> list[str]:
    sources: set[str] = set()
    for ref_path, ref_sources in references["paths"].items():
        if _path_overlaps(family_path, ref_path):
            sources.update(ref_sources)
    for pattern in protect_globs:
        if _matches_glob(family_path, pattern):
            sources.add(f"explicit:protect-glob:{pattern}")
    return sorted(sources)


def _collect_refs_from_text_file(
    path: Path,
    artifact_root: Path,
    paths: dict[str, set[str]],
    sources: set[str],
) -> None:
    try:
        text = path.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError):
        return
    source = path.as_posix()
    for raw_ref in _extract_artifact_refs(text):
        rel = _normalize_artifact_ref(raw_ref, artifact_root)
        if rel:
            paths[rel].add(source)
            sources.add(source)


def _collect_refs_from_manifest(
    path: Path,
    artifact_root: Path,
    paths: dict[str, set[str]],
    sources: set[str],
) -> None:
    source = path.as_posix()
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except (OSError, UnicodeDecodeError):
        return

    for line in lines:
        stripped = line.strip()
        if not stripped:
            continue
        try:
            payload = json.loads(stripped)
        except json.JSONDecodeError:
            for raw_ref in _extract_artifact_refs(stripped):
                rel = _normalize_artifact_ref(raw_ref, artifact_root)
                if rel:
                    paths[rel].add(source)
                    sources.add(source)
            continue
        for raw_ref in _iter_json_strings(payload):
            rel = _normalize_artifact_ref(raw_ref, artifact_root)
            if rel:
                paths[rel].add(source)
                sources.add(source)


def _extract_artifact_refs(text: str) -> list[str]:
    refs = []
    for match in ARTIFACT_REF_RE.finditer(text):
        refs.append(match.group(1))
    return refs


def _iter_json_strings(value: Any) -> Iterable[str]:
    if isinstance(value, str):
        yield value
    elif isinstance(value, dict):
        for item in value.values():
            yield from _iter_json_strings(item)
    elif isinstance(value, list):
        for item in value:
            yield from _iter_json_strings(item)


def _normalize_artifact_ref(value: str, artifact_root: Path) -> str | None:
    text = value.replace("\\", "/").strip().strip("`'\"<>")
    text = text.rstrip(".,;:")
    root_name = artifact_root.name.replace("\\", "/").strip("/")
    if not text:
        return None

    artifact_prefix = f"{root_name}/"
    index = text.find(artifact_prefix)
    if index >= 0:
        text = text[index + len(artifact_prefix) :]
    else:
        try:
            value_path = Path(value)
            if value_path.is_absolute():
                rel = value_path.relative_to(artifact_root.resolve())
                text = rel.as_posix()
        except (OSError, ValueError):
            pass

    text = text.strip("/")
    if not text or text == root_name:
        return None
    if not _looks_like_artifact_relative_path(text):
        return None
    return text


def _looks_like_artifact_relative_path(value: str) -> bool:
    first = value.split("/", 1)[0].lower()
    return (
        first in CONTAINER_DIRS
        or first in GENERATED_MARKERS
        or first.startswith(("tmp", "temp"))
    )


def _generated_markers(rel_path: str) -> set[str]:
    markers: set[str] = set()
    normalized = rel_path.replace("\\", "/")
    for part in Path(normalized).parts:
        lowered = part.lower()
        if lowered in GENERATED_MARKERS:
            markers.add(lowered)
        if lowered.startswith(("tmp", "temp")):
            markers.add("tmp-prefix")
        for suffix in GENERATED_SUFFIX_MARKERS:
            if lowered.endswith(suffix):
                markers.add(suffix)
    return markers


def _matches_glob(path: str, pattern: str) -> bool:
    normalized = path.replace("\\", "/")
    clean_pattern = pattern.replace("\\", "/")
    return fnmatch.fnmatch(normalized, clean_pattern) or fnmatch.fnmatch(
        f"artifacts/{normalized}",
        clean_pattern,
    )


def _path_overlaps(left: str, right: str) -> bool:
    left = left.strip("/")
    right = right.strip("/")
    if left == right:
        return True
    return left.startswith(right + "/") or right.startswith(left + "/")


def _format_ts(value: float | None) -> str | None:
    if value is None:
        return None
    return datetime.fromtimestamp(value, timezone.utc).isoformat(timespec="seconds")


def _action_rank(action: str) -> int:
    return {
        "delete_candidate": 0,
        "archive_candidate": 1,
        "keep_protected": 2,
        "keep": 3,
    }.get(action, 9)


def _format_counts(counts: dict[str, int]) -> str:
    if not counts:
        return "-"
    return ", ".join(f"{key}={value}" for key, value in sorted(counts.items()))


def _format_family_row(row: dict[str, Any]) -> str:
    reasons = ", ".join(row["reasons"][:5])
    if len(row["reasons"]) > 5:
        reasons += f" (+{len(row['reasons']) - 5})"
    protected = "yes" if row["protected"] else "no"
    return (
        f"| {_escape_md(row['action'])} | `{_escape_md(row['path'])}` | "
        f"`{_escape_md(row['family_group'])}` | {row['bytes']} | {row['files']} | "
        f"{row['age_days']} | {_escape_md(reasons or '-')} | {protected} |"
    )


def _escape_md(value: object) -> str:
    return str(value).replace("|", "\\|").replace("\n", " ")


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default="artifacts", help="Artifact root to inspect.")
    parser.add_argument(
        "--reference-root",
        action="append",
        default=[],
        help="Dispatch/spec/reference root to scan for artifact mentions. Repeatable.",
    )
    parser.add_argument(
        "--manifest",
        action="append",
        default=[],
        help="Run manifest JSONL path to scan for artifact mentions. Repeatable.",
    )
    parser.add_argument(
        "--protect-path",
        action="append",
        default=[],
        help="Artifact path or repo-relative artifacts/... path to protect. Repeatable.",
    )
    parser.add_argument(
        "--protect-glob",
        action="append",
        default=[],
        help="Glob matching artifact-relative or artifacts/... paths to protect. Repeatable.",
    )
    parser.add_argument("--archive-after-days", type=int, default=DEFAULT_ARCHIVE_AFTER_DAYS)
    parser.add_argument("--delete-after-days", type=int, default=DEFAULT_DELETE_AFTER_DAYS)
    parser.add_argument("--large-family-bytes", type=int, default=DEFAULT_LARGE_FAMILY_BYTES)
    parser.add_argument("--limit", type=int, default=DEFAULT_REPORT_LIMIT, help="Markdown rows per section.")
    parser.add_argument("--output-json", help="Optional JSON report path.")
    parser.add_argument("--output-md", help="Optional Markdown report path.")
    parser.add_argument("--json", action="store_true", help="Print JSON to stdout instead of Markdown.")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    if args.archive_after_days < 0:
        raise SystemExit("--archive-after-days must be non-negative.")
    if args.delete_after_days < 0:
        raise SystemExit("--delete-after-days must be non-negative.")
    if args.large_family_bytes < 0:
        raise SystemExit("--large-family-bytes must be non-negative.")
    if args.limit < 1:
        raise SystemExit("--limit must be at least 1.")

    reference_roots = args.reference_root or [path.as_posix() for path in DEFAULT_REFERENCE_ROOTS]
    manifest_paths = args.manifest or [path.as_posix() for path in DEFAULT_MANIFESTS]
    plan = plan_artifact_retention(
        args.root,
        reference_roots=reference_roots,
        manifest_paths=manifest_paths,
        protect_paths=args.protect_path,
        protect_globs=args.protect_glob,
        archive_after_days=args.archive_after_days,
        delete_after_days=args.delete_after_days,
        large_family_bytes=args.large_family_bytes,
        report_limit=args.limit,
    )
    write_reports(plan, output_json=args.output_json, output_md=args.output_md)
    if args.json:
        print(json.dumps(plan, indent=2, sort_keys=True))
    else:
        print(render_markdown(plan, limit=args.limit), end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
