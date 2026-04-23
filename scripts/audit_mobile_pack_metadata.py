#!/usr/bin/env python3
"""Report likely retrieval-metadata mismatches in a Senku mobile pack."""

from __future__ import annotations

import argparse
import json
import sqlite3
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class AuditFamily:
    name: str
    expected_structure: str | None
    required_topics: tuple[str, ...]
    markers: tuple[str, ...]


FAMILIES: tuple[AuditFamily, ...] = (
    AuditFamily(
        name="soapmaking",
        expected_structure="soapmaking",
        required_topics=("soapmaking",),
        markers=("soap making", "making soap", "saponification", "tallow soap", "wood ash lye"),
    ),
    AuditFamily(
        name="message_auth",
        expected_structure="message_auth",
        required_topics=("message_authentication", "chain_of_custody"),
        markers=("challenge-response", "challenge response", "chain of custody", "tamper-evident", "authentic message"),
    ),
    AuditFamily(
        name="water_storage",
        expected_structure="water_storage",
        required_topics=("water_storage",),
        markers=("water storage", "stored water", "rotation schedule", "clean container", "food-safe"),
    ),
    AuditFamily(
        name="water_distribution",
        expected_structure="water_distribution",
        required_topics=("water_distribution",),
        markers=("gravity-fed", "gravity fed", "water distribution", "storage tank", "spring box"),
    ),
    AuditFamily(
        name="community_security",
        expected_structure="community_security",
        required_topics=("community_security",),
        markers=("guard rotation", "watch rotation", "checkpoint", "perimeter", "access control", "water system security"),
    ),
    AuditFamily(
        name="community_governance",
        expected_structure="community_governance",
        required_topics=("community_governance",),
        markers=("commons management", "graduated sanctions", "restorative justice", "membership rules", "trust recovery"),
    ),
    AuditFamily(
        name="glassmaking",
        expected_structure="glassmaking",
        required_topics=("glassmaking",),
        markers=("glassmaking", "glass making", "soda-lime glass", "glass furnace", "annealing"),
    ),
)


def build_like_clause(markers: tuple[str, ...]) -> tuple[str, list[str]]:
    clauses = []
    args: list[str] = []
    for marker in markers:
        clauses.append("lower(document) LIKE ?")
        args.append(f"%{marker.lower()}%")
    return " OR ".join(clauses), args


def split_topics(value: str | None) -> set[str]:
    if not value:
        return set()
    return {item.strip() for item in value.split(",") if item.strip()}


def audit_family(conn: sqlite3.Connection, family: AuditFamily, limit: int) -> dict:
    clause, args = build_like_clause(family.markers)
    rows = conn.execute(
        f"""
        SELECT
            guide_id,
            guide_title,
            section_heading,
            structure_type,
            topic_tags
        FROM chunks
        WHERE {clause}
        ORDER BY guide_id, section_heading
        """,
        args,
    ).fetchall()

    mismatches = []
    per_guide = defaultdict(int)
    for guide_id, guide_title, section_heading, structure_type, topic_tags in rows:
        topics = split_topics(topic_tags)
        structure_ok = family.expected_structure is None or structure_type == family.expected_structure
        topics_ok = all(topic in topics for topic in family.required_topics)
        if structure_ok and topics_ok:
            continue
        mismatch = {
            "guide_id": guide_id,
            "guide_title": guide_title,
            "section_heading": section_heading,
            "structure_type": structure_type,
            "topic_tags": topic_tags or "",
        }
        mismatches.append(mismatch)
        per_guide[(guide_id, guide_title)] += 1

    grouped = [
        {
            "guide_id": guide_id,
            "guide_title": guide_title,
            "count": count,
        }
        for (guide_id, guide_title), count in sorted(
            per_guide.items(),
            key=lambda item: (-item[1], item[0][0]),
        )[:limit]
    ]

    return {
        "family": family.name,
        "expected_structure": family.expected_structure,
        "required_topics": list(family.required_topics),
        "matched_rows": len(rows),
        "mismatch_rows": len(mismatches),
        "guide_counts": [
            {
                "guide_id": guide_id,
                "guide_title": guide_title,
                "count": count,
            }
            for (guide_id, guide_title), count in sorted(
                per_guide.items(),
                key=lambda item: (-item[1], item[0][0]),
            )
        ],
        "top_guides": grouped,
        "sample_rows": mismatches[:limit],
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("sqlite_path", help="Path to senku_mobile.sqlite3")
    parser.add_argument("--limit", type=int, default=10, help="Rows/guides to show per family")
    parser.add_argument("--output", help="Optional JSON output path")
    args = parser.parse_args()

    sqlite_path = Path(args.sqlite_path).resolve()
    if not sqlite_path.exists():
        raise SystemExit(f"SQLite pack not found: {sqlite_path}")

    with sqlite3.connect(sqlite_path) as conn:
        report = {
            "sqlite_path": str(sqlite_path),
            "families": [audit_family(conn, family, args.limit) for family in FAMILIES],
        }

    if args.output:
        output_path = Path(args.output).resolve()
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")

    print(f"Metadata audit for {sqlite_path}")
    for family in report["families"]:
        print()
        print(f"[{family['family']}] matched={family['matched_rows']} mismatches={family['mismatch_rows']}")
        for guide in family["top_guides"]:
            print(f"  {guide['guide_id']}: {guide['count']} :: {guide['guide_title']}")


if __name__ == "__main__":
    main()
