#!/usr/bin/env python3
"""Compare mobile-pack count surfaces without requiring hydrated LFS assets."""

from __future__ import annotations

import argparse
import hashlib
import json
import sqlite3
from contextlib import closing
from pathlib import Path
from typing import Any


COUNT_TABLES = (
    "guides",
    "chunks",
    "guide_related_links",
    "retrieval_metadata_guides",
    "deterministic_rules",
    "answer_cards",
    "answer_card_clauses",
    "answer_card_sources",
)


def _load_json(path: Path) -> dict[str, Any] | None:
    if not path.exists():
        return None
    return json.loads(path.read_text(encoding="utf-8"))


def _sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _parse_lfs_pointer(path: Path) -> dict[str, Any] | None:
    try:
        text = path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return None
    except OSError:
        return None
    if not text.startswith("version https://git-lfs.github.com/spec/v1"):
        return None
    pointer: dict[str, Any] = {"kind": "git_lfs_pointer"}
    for line in text.splitlines():
        if line.startswith("oid sha256:"):
            pointer["sha256"] = line.split(":", 1)[1].strip()
        elif line.startswith("size "):
            try:
                pointer["bytes"] = int(line.split(" ", 1)[1].strip())
            except ValueError:
                pointer["bytes"] = line.split(" ", 1)[1].strip()
    return pointer


def _resolve_pack_paths(input_path: Path) -> tuple[Path | None, Path | None, Path]:
    path = input_path.resolve()
    if path.is_dir():
        return path / "senku_manifest.json", path / "senku_mobile.sqlite3", path
    if path.name == "senku_manifest.json":
        return path, path.parent / "senku_mobile.sqlite3", path.parent
    return (path.parent / "senku_manifest.json", path, path.parent)


def _manifest_sqlite_file(manifest: dict[str, Any] | None) -> dict[str, Any] | None:
    if not manifest:
        return None
    files = manifest.get("files")
    if not isinstance(files, dict):
        return None
    sqlite_file = files.get("sqlite")
    return sqlite_file if isinstance(sqlite_file, dict) else None


def summarize_pack(input_path: Path) -> dict[str, Any]:
    manifest_path, sqlite_path, pack_dir = _resolve_pack_paths(input_path)
    manifest = _load_json(manifest_path) if manifest_path else None
    manifest_counts = manifest.get("counts", {}) if isinstance(manifest, dict) else {}
    sqlite_file = _manifest_sqlite_file(manifest)

    summary: dict[str, Any] = {
        "input": str(input_path),
        "pack_dir": str(pack_dir),
        "manifest_path": str(manifest_path) if manifest_path else None,
        "sqlite_path": str(sqlite_path) if sqlite_path else None,
        "manifest_counts": manifest_counts if isinstance(manifest_counts, dict) else {},
        "sqlite_counts": {},
        "manifest_sqlite_mismatches": [],
        "table_errors": {},
        "sqlite_status": "missing",
        "sqlite_file": None,
    }

    if not sqlite_path or not sqlite_path.exists():
        return summary

    actual_bytes = sqlite_path.stat().st_size
    pointer = _parse_lfs_pointer(sqlite_path)
    if pointer:
        expected_sha = str(sqlite_file.get("sha256", "")) if sqlite_file else ""
        expected_bytes = sqlite_file.get("bytes") if sqlite_file else None
        summary["sqlite_status"] = "git_lfs_pointer"
        summary["sqlite_file"] = {
            "actual_bytes": actual_bytes,
            "pointer": pointer,
            "manifest_sha256": expected_sha or None,
            "manifest_bytes": expected_bytes,
            "pointer_matches_manifest": (
                bool(expected_sha)
                and pointer.get("sha256") == expected_sha
                and (expected_bytes is None or pointer.get("bytes") == expected_bytes)
            ),
        }
        return summary

    summary["sqlite_status"] = "materialized"
    summary["sqlite_file"] = {
        "actual_bytes": actual_bytes,
        "sha256": _sha256(sqlite_path),
        "manifest_sha256": sqlite_file.get("sha256") if sqlite_file else None,
        "manifest_bytes": sqlite_file.get("bytes") if sqlite_file else None,
    }

    with closing(sqlite3.connect(sqlite_path)) as conn:
        for table in COUNT_TABLES:
            try:
                row = conn.execute(f"SELECT COUNT(*) FROM {table}").fetchone()
                summary["sqlite_counts"][table] = int(row[0]) if row else 0
            except sqlite3.Error as exc:
                summary["table_errors"][table] = str(exc)
    for key, manifest_value in summary["manifest_counts"].items():
        sqlite_value = summary["sqlite_counts"].get(key)
        if sqlite_value is not None and sqlite_value != manifest_value:
            summary["manifest_sqlite_mismatches"].append(
                {
                    "name": key,
                    "manifest": manifest_value,
                    "sqlite": sqlite_value,
                    "delta": sqlite_value - manifest_value
                    if isinstance(manifest_value, int) and isinstance(sqlite_value, int)
                    else None,
                }
            )
    return summary


def compare_summaries(baseline: dict[str, Any], candidate: dict[str, Any]) -> dict[str, Any]:
    baseline_counts = _effective_counts(baseline)
    candidate_counts = _effective_counts(candidate)
    keys = sorted(set(baseline_counts) | set(candidate_counts))
    deltas = []
    for key in keys:
        base_value = baseline_counts.get(key)
        candidate_value = candidate_counts.get(key)
        delta = (
            candidate_value - base_value
            if isinstance(base_value, int) and isinstance(candidate_value, int)
            else None
        )
        deltas.append(
            {
                "name": key,
                "baseline": base_value,
                "candidate": candidate_value,
                "delta": delta,
            }
        )
    return {
        "baseline": baseline,
        "candidate": candidate,
        "count_deltas": deltas,
    }


def _effective_counts(summary: dict[str, Any]) -> dict[str, Any]:
    counts = dict(summary.get("manifest_counts") or {})
    counts.update(summary.get("sqlite_counts") or {})
    return counts


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("baseline", help="Baseline pack directory, manifest, or senku_mobile.sqlite3")
    parser.add_argument("candidate", help="Candidate pack directory, manifest, or senku_mobile.sqlite3")
    parser.add_argument("--output", help="Optional JSON report path")
    args = parser.parse_args()

    report = compare_summaries(
        summarize_pack(Path(args.baseline)),
        summarize_pack(Path(args.candidate)),
    )

    if args.output:
        output_path = Path(args.output).resolve()
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")

    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
