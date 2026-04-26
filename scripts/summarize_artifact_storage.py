#!/usr/bin/env python3
"""Summarize artifact storage without reading file bodies."""

from __future__ import annotations

import argparse
import json
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any


GENERATED_DIR_MARKERS = {
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

GENERATED_DIR_SUFFIX_MARKERS = (
    "_diag",
    "_diagnostic",
    "_diagnostics",
    "_output",
    "_outputs",
    "_report",
    "_reports",
)


def summarize_storage(root: str | Path, limit: int = 20) -> dict[str, Any]:
    """Return a read-only storage summary for files below root."""
    root_path = Path(root)
    limit = max(0, int(limit))
    files = []
    dir_sizes = defaultdict(int)
    dir_file_counts = defaultdict(int)
    dir_paths = set()
    suffix_counts = defaultdict(lambda: {"count": 0, "bytes": 0})
    basename_groups = defaultdict(list)

    if not root_path.exists():
        return {
            "root": root_path.as_posix(),
            "exists": False,
            "total_bytes": 0,
            "file_count": 0,
            "dir_count": 0,
            "largest_files": [],
            "largest_dirs": [],
            "suffix_counts": [],
            "generated_dirs": [],
            "duplicate_basename_families": [],
        }

    for path in sorted(root_path.rglob("*")):
        try:
            if path.is_dir():
                dir_paths.add(path.relative_to(root_path).as_posix())
                continue
            if not path.is_file():
                continue
            stat = path.stat()
        except OSError:
            continue

        size = stat.st_size
        rel_path = path.relative_to(root_path).as_posix()
        files.append({"path": rel_path, "bytes": size})

        suffix = path.suffix.lower() or "<none>"
        suffix_counts[suffix]["count"] += 1
        suffix_counts[suffix]["bytes"] += size
        basename_groups[path.name].append({"path": rel_path, "bytes": size})

        parent = path.parent
        while True:
            dir_rel = "." if parent == root_path else parent.relative_to(root_path).as_posix()
            dir_sizes[dir_rel] += size
            dir_file_counts[dir_rel] += 1
            if parent == root_path:
                break
            dir_paths.add(dir_rel)
            parent = parent.parent

    largest_dirs = [
        {
            "path": path,
            "bytes": dir_sizes[path],
            "files": dir_file_counts[path],
        }
        for path in sorted(dir_paths, key=lambda item: (-dir_sizes[item], item))
    ][:limit]

    generated_dirs = [
        {
            "path": path,
            "bytes": dir_sizes[path],
            "files": dir_file_counts[path],
            "markers": _generated_dir_markers(path),
        }
        for path in sorted(dir_paths, key=lambda item: (-dir_sizes[item], item))
        if _generated_dir_markers(path)
    ][:limit]

    duplicates = []
    for basename, entries in basename_groups.items():
        if len(entries) < 2:
            continue
        entries = sorted(entries, key=lambda item: (-item["bytes"], item["path"]))
        duplicates.append(
            {
                "basename": basename,
                "count": len(entries),
                "bytes": sum(entry["bytes"] for entry in entries),
                "examples": entries[:limit],
            }
        )

    return {
        "root": root_path.as_posix(),
        "exists": True,
        "total_bytes": sum(file["bytes"] for file in files),
        "file_count": len(files),
        "dir_count": len(dir_paths),
        "largest_files": sorted(files, key=lambda item: (-item["bytes"], item["path"]))[:limit],
        "largest_dirs": largest_dirs,
        "suffix_counts": _sort_suffix_counts(suffix_counts)[:limit],
        "generated_dirs": generated_dirs,
        "duplicate_basename_families": sorted(
            duplicates, key=lambda item: (-item["bytes"], item["basename"])
        )[:limit],
    }


def _sort_suffix_counts(suffix_counts: dict[str, dict[str, int]]) -> list[dict[str, int | str]]:
    rows = [
        {"suffix": suffix, "count": values["count"], "bytes": values["bytes"]}
        for suffix, values in suffix_counts.items()
    ]
    return sorted(rows, key=lambda item: (-item["bytes"], item["suffix"]))


def _generated_dir_markers(rel_path: str) -> list[str]:
    markers = []
    for part in Path(rel_path).parts:
        lowered = part.lower()
        if lowered in GENERATED_DIR_MARKERS:
            markers.append(lowered)
        if lowered.startswith(("tmp", "temp")) and lowered not in markers:
            markers.append("tmp-prefix")
        for suffix in GENERATED_DIR_SUFFIX_MARKERS:
            if lowered.endswith(suffix) and suffix not in markers:
                markers.append(suffix)
    return sorted(markers)


def render_text(summary: dict[str, Any]) -> str:
    """Render a compact human-readable storage report."""
    lines = [
        f"Artifact storage report: {summary['root']}",
        f"Exists: {summary['exists']}",
        f"Total: {_format_bytes(summary['total_bytes'])} across {summary['file_count']} files, "
        f"{summary['dir_count']} dirs",
    ]
    if not summary["exists"]:
        return "\n".join(lines) + "\n"

    _append_table(lines, "Largest files", summary["largest_files"], ("path", "bytes"))
    _append_table(lines, "Largest dirs", summary["largest_dirs"], ("path", "bytes", "files"))
    _append_table(lines, "Suffix counts", summary["suffix_counts"], ("suffix", "count", "bytes"))
    _append_table(lines, "Generated dirs", summary["generated_dirs"], ("path", "bytes", "files", "markers"))
    _append_duplicates(lines, summary["duplicate_basename_families"])
    return "\n".join(lines) + "\n"


def _append_table(lines: list[str], title: str, rows: list[dict[str, Any]], columns: tuple[str, ...]) -> None:
    lines.extend(["", title + ":"])
    if not rows:
        lines.append("  (none)")
        return
    for row in rows:
        parts = []
        for column in columns:
            value = row[column]
            if column == "bytes":
                value = _format_bytes(value)
            if isinstance(value, list):
                value = ",".join(str(item) for item in value)
            parts.append(f"{column}={value}")
        lines.append("  " + "  ".join(parts))


def _append_duplicates(lines: list[str], rows: list[dict[str, Any]]) -> None:
    lines.extend(["", "Duplicate basename families:"])
    if not rows:
        lines.append("  (none)")
        return
    for row in rows:
        examples = ", ".join(example["path"] for example in row["examples"])
        lines.append(
            f"  basename={row['basename']}  count={row['count']}  "
            f"bytes={_format_bytes(row['bytes'])}  examples={examples}"
        )


def _format_bytes(size: int) -> str:
    units = ("B", "KiB", "MiB", "GiB")
    value = float(size)
    for unit in units:
        if value < 1024 or unit == units[-1]:
            if unit == "B":
                return f"{int(value)} {unit}"
            return f"{value:.1f} {unit}"
        value /= 1024
    return f"{size} B"


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default="artifacts", help="Artifact tree to summarize")
    parser.add_argument("--limit", type=int, default=20, help="Rows per section")
    parser.add_argument("--json", action="store_true", help="Emit JSON instead of text")
    args = parser.parse_args(argv)

    summary = summarize_storage(args.root, args.limit)
    if args.json:
        print(json.dumps(summary, indent=2, sort_keys=True))
    else:
        print(render_text(summary), end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
