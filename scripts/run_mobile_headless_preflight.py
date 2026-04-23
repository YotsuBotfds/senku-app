#!/usr/bin/env python3
"""Run a headless Senku mobile preflight over family packs and pack metadata."""

from __future__ import annotations

import argparse
import json
import sqlite3
from collections import defaultdict
from datetime import datetime
from pathlib import Path

from metadata_validation import (
    REPORT_FILENAME as METADATA_VALIDATION_REPORT_FILENAME,
    format_validation_errors,
    report_has_errors,
    validate_guide_records,
    write_validation_report,
)

try:  # pragma: no cover - import path varies between CLI script and test/module execution
    from scripts.audit_mobile_pack_metadata import FAMILIES, audit_family
except ImportError:  # pragma: no cover
    from audit_mobile_pack_metadata import FAMILIES, audit_family


AUDIT_MAP: dict[str, tuple[str, ...]] = {
    "construction_watercraft": (),
    "water_infra_sanitation": ("water_storage", "water_distribution"),
    "craft_refinement": ("soapmaking", "glassmaking"),
    "medical_first_aid": (),
    "comms_governance_security": ("message_auth", "community_security", "community_governance"),
    "multi_objective_separation": ("water_storage", "community_security", "community_governance"),
    "mixed_family_followups": (
        "water_storage",
        "water_distribution",
        "soapmaking",
        "glassmaking",
        "message_auth",
        "community_security",
        "community_governance",
    ),
}


def repo_root() -> Path:
    return Path(__file__).resolve().parent.parent


def resolve_path(value: str | None, default: Path) -> Path:
    if not value:
        return default.resolve()
    candidate = Path(value)
    if candidate.is_absolute():
        return candidate.resolve()
    return (repo_root() / candidate).resolve()


def load_manifest(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def count_prompt_cases(path: Path, kind: str) -> int:
    text = path.read_text(encoding="utf-8").strip()
    if not text:
        return 0

    if kind == "follow_up" or path.suffix.lower() == ".jsonl":
        return sum(
            1
            for line in text.splitlines()
            if line.strip() and not line.strip().startswith("#")
        )

    return sum(
        1
        for line in text.splitlines()
        if line.strip() and not line.strip().startswith("#")
    )


def summarize_pack(
    pack: dict,
    prompt_root: Path,
    audit_index: dict[str, dict],
) -> dict:
    prompt_path = (prompt_root / pack["file"]).resolve()
    prompt_count = count_prompt_cases(prompt_path, pack["kind"])
    family = pack["family"]
    audit_refs = list(AUDIT_MAP.get(family, ()))
    return {
        "family": family,
        "file": pack["file"],
        "kind": pack["kind"],
        "prompt_path": str(prompt_path),
        "prompt_count": prompt_count,
        "suggested_emulators": list(pack.get("suggested_emulators", [])),
        "source_files": list(pack.get("source_files", [])),
        "audit_refs": audit_refs,
        "audits": [audit_index[name] for name in audit_refs if name in audit_index],
    }


def build_audit_index(sqlite_path: Path, selected_families: set[str], limit: int) -> dict[str, dict]:
    with sqlite3.connect(sqlite_path) as conn:
        return {
            family.name: audit_family(conn, family, limit)
            for family in FAMILIES
            if family.name in selected_families
        }


def validate_pack_guide_metadata(sqlite_path: Path) -> dict:
    with sqlite3.connect(sqlite_path) as conn:
        rows = conn.execute(
            """
            SELECT guide_id, slug, title, category, description, source_file
            FROM guides
            """
        ).fetchall()
    return validate_guide_records(
        [
            {
                "guide_id": row[0],
                "slug": row[1],
                "title": row[2],
                "category": row[3],
                "description": row[4],
                "source_file": row[5],
            }
            for row in rows
        ],
        scope="mobile_headless_preflight",
    )


def build_global_review_queue(audit_index: dict[str, dict], limit: int) -> list[dict]:
    review_queue: dict[tuple[str, str], dict] = defaultdict(
        lambda: {"guide_id": "", "guide_title": "", "total_mismatches": 0, "families": []}
    )
    for audit in audit_index.values():
        for guide in audit.get("guide_counts", []):
            key = (guide["guide_id"], guide["guide_title"])
            entry = review_queue[key]
            entry["guide_id"] = guide["guide_id"]
            entry["guide_title"] = guide["guide_title"]
            entry["total_mismatches"] += guide["count"]
            entry["families"].append(
                {
                    "family": audit["family"],
                    "count": guide["count"],
                }
            )

    return sorted(
        review_queue.values(),
        key=lambda item: (-item["total_mismatches"], item["guide_id"]),
    )[:limit]


def build_audit_regressions(current_index: dict[str, dict], baseline_index: dict[str, dict]) -> list[dict]:
    regressions = []
    for family_name in sorted(set(current_index) | set(baseline_index)):
        current = current_index.get(family_name, {})
        baseline = baseline_index.get(family_name, {})
        current_mismatches = current.get("mismatch_rows", 0)
        baseline_mismatches = baseline.get("mismatch_rows", 0)
        regressions.append(
            {
                "family": family_name,
                "current_mismatch_rows": current_mismatches,
                "baseline_mismatch_rows": baseline_mismatches,
                "delta_mismatch_rows": current_mismatches - baseline_mismatches,
                "matched_rows": current.get("matched_rows", 0),
                "baseline_matched_rows": baseline.get("matched_rows", 0),
            }
        )

    return sorted(
        regressions,
        key=lambda item: (-item["delta_mismatch_rows"], item["family"]),
    )


def build_global_regression_queue(
    current_index: dict[str, dict],
    baseline_index: dict[str, dict],
    limit: int,
) -> list[dict]:
    entries: dict[tuple[str, str], dict] = {}
    all_families = sorted(set(current_index) | set(baseline_index))

    for family_name in all_families:
        current_counts = {
            (guide["guide_id"], guide["guide_title"]): guide["count"]
            for guide in current_index.get(family_name, {}).get("guide_counts", [])
        }
        baseline_counts = {
            (guide["guide_id"], guide["guide_title"]): guide["count"]
            for guide in baseline_index.get(family_name, {}).get("guide_counts", [])
        }
        for key in set(current_counts) | set(baseline_counts):
            delta = current_counts.get(key, 0) - baseline_counts.get(key, 0)
            if delta <= 0:
                continue
            entry = entries.setdefault(
                key,
                {
                    "guide_id": key[0],
                    "guide_title": key[1],
                    "total_delta": 0,
                    "families": [],
                },
            )
            entry["total_delta"] += delta
            entry["families"].append(
                {
                    "family": family_name,
                    "delta": delta,
                    "current": current_counts.get(key, 0),
                    "baseline": baseline_counts.get(key, 0),
                }
            )

    return sorted(
        entries.values(),
        key=lambda item: (-item["total_delta"], item["guide_id"]),
    )[:limit]


def build_report(
    manifest_path: Path,
    prompt_root: Path,
    sqlite_path: Path,
    limit: int,
    baseline_sqlite_path: Path | None = None,
) -> dict:
    manifest = load_manifest(manifest_path)
    family_index = {family.name: family for family in FAMILIES}
    selected_families = {name for names in AUDIT_MAP.values() for name in names}
    audit_index = build_audit_index(sqlite_path, selected_families, limit)
    baseline_audit_index = (
        build_audit_index(baseline_sqlite_path, selected_families, limit)
        if baseline_sqlite_path
        else {}
    )

    packs = [
        summarize_pack(pack, prompt_root, audit_index)
        for pack in manifest.get("packs", [])
    ]

    global_review_queue = build_global_review_queue(audit_index, limit)
    audit_regressions = build_audit_regressions(audit_index, baseline_audit_index)
    global_regression_queue = build_global_regression_queue(
        audit_index,
        baseline_audit_index,
        limit,
    )

    return {
        "generated_at": datetime.now().astimezone().isoformat(),
        "manifest_path": str(manifest_path),
        "prompt_root": str(prompt_root),
        "sqlite_path": str(sqlite_path),
        "baseline_sqlite_path": str(baseline_sqlite_path) if baseline_sqlite_path else None,
        "audit_limit": limit,
        "available_audit_families": sorted(family_index.keys()),
        "packs": packs,
        "audit_summary": {
            name: {
                "matched_rows": audit["matched_rows"],
                "mismatch_rows": audit["mismatch_rows"],
                "top_guides": audit["top_guides"],
            }
            for name, audit in audit_index.items()
        },
        "baseline_audit_summary": {
            name: {
                "matched_rows": audit["matched_rows"],
                "mismatch_rows": audit["mismatch_rows"],
                "top_guides": audit["top_guides"],
            }
            for name, audit in baseline_audit_index.items()
        },
        "audit_regressions": audit_regressions,
        "global_review_queue": global_review_queue,
        "global_regression_queue": global_regression_queue,
    }


def main() -> None:
    root = repo_root()
    default_manifest = root / "artifacts" / "prompts" / "families_20260412" / "android_family_manifest_20260412.json"
    default_prompt_root = default_manifest.parent
    default_sqlite = root / "android-app" / "app" / "src" / "main" / "assets" / "mobile_pack" / "senku_mobile.sqlite3"
    default_output_dir = root / "artifacts" / "bench" / "headless_preflight_20260412"

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", help="Path to the Android family manifest JSON")
    parser.add_argument("--prompt-root", help="Directory that contains the manifest prompt files")
    parser.add_argument("--sqlite-path", help="Path to the active mobile-pack SQLite database")
    parser.add_argument("--baseline-sqlite-path", help="Optional path to a baseline mobile-pack SQLite database")
    parser.add_argument("--limit", type=int, default=10, help="Audit sample/top-guide limit")
    parser.add_argument("--output-dir", help="Directory for the emitted report")
    args = parser.parse_args()

    manifest_path = resolve_path(args.manifest, default_manifest)
    prompt_root = resolve_path(args.prompt_root, default_prompt_root)
    sqlite_path = resolve_path(args.sqlite_path, default_sqlite)
    baseline_sqlite_path = (
        resolve_path(args.baseline_sqlite_path, root / "artifacts" / "mobile_pack" / "senku_20260412_full_metadata_refresh_v5" / "senku_mobile.sqlite3")
        if args.baseline_sqlite_path or (root / "artifacts" / "mobile_pack" / "senku_20260412_full_metadata_refresh_v5" / "senku_mobile.sqlite3").exists()
        else None
    )
    output_dir = resolve_path(args.output_dir, default_output_dir)

    if not manifest_path.exists():
        raise SystemExit(f"Manifest not found: {manifest_path}")
    if not sqlite_path.exists():
        raise SystemExit(f"SQLite pack not found: {sqlite_path}")
    if baseline_sqlite_path and not baseline_sqlite_path.exists():
        raise SystemExit(f"Baseline SQLite pack not found: {baseline_sqlite_path}")

    metadata_report = validate_pack_guide_metadata(sqlite_path)
    output_dir.mkdir(parents=True, exist_ok=True)
    write_validation_report(
        metadata_report,
        output_dir / METADATA_VALIDATION_REPORT_FILENAME,
    )
    if report_has_errors(metadata_report):
        raise SystemExit(format_validation_errors(metadata_report))

    report = build_report(
        manifest_path,
        prompt_root,
        sqlite_path,
        args.limit,
        baseline_sqlite_path=baseline_sqlite_path,
    )

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_path = output_dir / f"mobile_headless_preflight_{timestamp}.json"
    output_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")

    print(f"Headless mobile preflight written to {output_path}")
    for pack in report["packs"]:
        audit_suffix = ""
        if pack["audits"]:
            audit_suffix = " | " + ", ".join(
                f"{audit['family']} mismatches={audit['mismatch_rows']}"
                for audit in pack["audits"]
            )
        print(
            f"[{pack['family']}] kind={pack['kind']} prompts={pack['prompt_count']}"
            f" emulators={','.join(pack['suggested_emulators']) or '-'}{audit_suffix}"
        )
    if report["global_review_queue"]:
        print("Top metadata review guides:")
        for item in report["global_review_queue"][:5]:
            families = ", ".join(
                f"{family['family']}:{family['count']}"
                for family in item["families"]
            )
            print(
                f"  {item['guide_id']} total={item['total_mismatches']} :: "
                f"{item['guide_title']} [{families}]"
            )
    if report["audit_regressions"]:
        print("Top family mismatch deltas:")
        for item in report["audit_regressions"][:5]:
            if item["delta_mismatch_rows"] == 0:
                continue
            print(
                f"  {item['family']} delta={item['delta_mismatch_rows']} "
                f"(current={item['current_mismatch_rows']} baseline={item['baseline_mismatch_rows']})"
            )
    if report["global_regression_queue"]:
        print("Top metadata regression guides:")
        for item in report["global_regression_queue"][:5]:
            families = ", ".join(
                f"{family['family']}:+{family['delta']}"
                for family in item["families"]
            )
            print(
                f"  {item['guide_id']} delta={item['total_delta']} :: "
                f"{item['guide_title']} [{families}]"
            )


if __name__ == "__main__":
    main()
