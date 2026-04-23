#!/usr/bin/env python3
"""Find heuristic contradiction candidates from guide invariants and audit hotspots."""

from __future__ import annotations

import argparse
import json
import re
from collections import defaultdict
from itertools import combinations
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
INVARIANT_PATH = ROOT / "artifacts" / "guide_invariants" / "guide_invariants.jsonl"
HOTSPOT_PATH = ROOT / "artifacts" / "guide_audit_hotspots" / "guide_audit_hotspots.json"
DEFAULT_OUTPUT = ROOT / "artifacts" / "guide_conflict_candidates"

WORD_RE = re.compile(r"[a-z][a-z0-9-]+", re.IGNORECASE)
NUMBER_RE = re.compile(r"\d+(?:\.\d+)?")
GENERIC_HEADINGS = {
    "(intro)",
    "overview",
    "summary",
    "process",
    "steps:",
    "construction steps",
    "instructions",
    "quick reference",
    "related guides",
    "storage conditions",
}
STOPWORDS = {
    "the", "and", "for", "with", "from", "that", "this", "into", "than", "then",
    "when", "what", "where", "your", "their", "there", "about", "should", "could",
    "would", "guide", "section", "step", "steps", "process", "quick", "reference",
    "after", "before", "during", "under", "over", "more", "less", "than", "into",
    "hours", "hour", "minutes", "minute", "days", "day", "weeks", "week", "months",
    "month", "years", "year", "water", "food", "patient", "patients",
}


def normalize_heading(value: str) -> str:
    return re.sub(r"\s+", " ", (value or "").strip().lower())


def extract_keywords(text: str) -> set[str]:
    keywords = set()
    for token in WORD_RE.findall((text or "").lower()):
        if token in STOPWORDS:
            continue
        if NUMBER_RE.fullmatch(token):
            continue
        if len(token) < 4:
            continue
        keywords.add(token)
    return keywords


def classify_unit_family(matches: list[str]) -> str:
    joined = " ".join(matches).lower()
    if any(unit in joined for unit in ("°c", "°f")):
        return "temperature"
    if any(unit in joined for unit in ("mg", "mcg", "ug", " g", "kg")):
        return "mass"
    if any(unit in joined for unit in ("ml", "l")):
        return "volume"
    if "%" in joined or "percent" in joined:
        return "percent"
    if any(unit in joined for unit in ("minute", "hour", "day", "week", "month", "year")):
        return "time"
    if any(unit in joined for unit in ("cm", "mm", " km", "ft", "feet", "inch", "inches")):
        return "length"
    if any(unit in joined for unit in ("psi", "bar")):
        return "pressure"
    return "other"


def load_hotspot_guides(path: Path, limit: int) -> set[str]:
    data = json.loads(path.read_text(encoding="utf-8"))
    return {item["slug"] for item in data.get("top_guides", [])[:limit]}


def load_records(path: Path, hotspot_slugs: set[str]) -> list[dict]:
    records = []
    with path.open("r", encoding="utf-8") as handle:
        for line in handle:
            if not line.strip():
                continue
            record = json.loads(line)
            if hotspot_slugs and record["slug"] not in hotspot_slugs:
                continue
            record["normalized_heading"] = normalize_heading(record["section_heading"])
            record["keywords"] = sorted(extract_keywords(record["text"]))
            record["unit_family"] = classify_unit_family(record.get("matches", []))
            record["numbers"] = NUMBER_RE.findall(record["text"])
            if not record["numbers"]:
                continue
            # Link/product rows produce lots of numeric noise without yielding
            # meaningful cross-guide contradictions.
            if "http://" in record["text"] or "https://" in record["text"]:
                continue
            records.append(record)
    return records


def build_candidates(records: list[dict]) -> list[dict]:
    buckets = defaultdict(list)
    for record in records:
        heading = record["normalized_heading"]
        if heading in GENERIC_HEADINGS:
            continue
        key = (heading, record["unit_family"])
        buckets[key].append(record)

    deduped = {}
    for (heading, unit_family), bucket in buckets.items():
        if len(bucket) < 2:
            continue
        for left, right in combinations(bucket, 2):
            if left["slug"] == right["slug"]:
                continue
            if left["numbers"] == right["numbers"]:
                continue
            overlap = sorted(set(left["keywords"]) & set(right["keywords"]))
            if len(overlap) < 3:
                continue
            guide_a, guide_b = sorted((left["slug"], right["slug"]))
            if guide_a == left["slug"]:
                record_a, record_b = left, right
            else:
                record_a, record_b = right, left
            candidate = {
                "heading": heading,
                "unit_family": unit_family,
                "guide_a": guide_a,
                "guide_b": guide_b,
                "guide_a_id": record_a["guide_id"],
                "guide_b_id": record_b["guide_id"],
                "shared_keywords": overlap,
                "guide_a_numbers": record_a["numbers"],
                "guide_b_numbers": record_b["numbers"],
                "guide_a_text": record_a["text"],
                "guide_b_text": record_b["text"],
                "score": len(overlap),
            }
            dedupe_key = (heading, unit_family, guide_a, guide_b)
            existing = deduped.get(dedupe_key)
            if existing is None or candidate["score"] > existing["score"]:
                deduped[dedupe_key] = candidate

    candidates = list(deduped.values())
    candidates.sort(
        key=lambda item: (
            -item["score"],
            item["heading"],
            item["guide_a"],
            item["guide_b"],
        )
    )
    return candidates


def write_outputs(candidates: list[dict], output_dir: Path) -> tuple[Path, Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    json_path = output_dir / "guide_conflict_candidates.json"
    md_path = output_dir / "guide_conflict_candidates.md"

    json_path.write_text(json.dumps(candidates[:250], indent=2), encoding="utf-8")

    lines = [
        "# Guide Conflict Candidates",
        "",
        f"- Candidates: {len(candidates)}",
        "",
        "## Top Candidates",
        "",
    ]
    for item in candidates[:60]:
        lines.append(
            f"- `{item['guide_a']}` vs `{item['guide_b']}` "
            f"[{item['heading']}, {item['unit_family']}]: "
            f"keywords={','.join(item['shared_keywords'][:6])} "
            f"numbers={item['guide_a_numbers']} vs {item['guide_b_numbers']}"
        )

    md_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return json_path, md_path


def main() -> int:
    parser = argparse.ArgumentParser(description="Find heuristic invariant conflict candidates.")
    parser.add_argument("--invariants", default=str(INVARIANT_PATH))
    parser.add_argument("--hotspots", default=str(HOTSPOT_PATH))
    parser.add_argument("--hotspot-limit", type=int, default=60)
    parser.add_argument("--output-dir", default=str(DEFAULT_OUTPUT))
    args = parser.parse_args()

    hotspot_slugs = load_hotspot_guides(Path(args.hotspots), args.hotspot_limit)
    records = load_records(Path(args.invariants), hotspot_slugs)
    candidates = build_candidates(records)
    json_path, md_path = write_outputs(candidates, Path(args.output_dir))

    print(f"Wrote candidate JSON: {json_path}")
    print(f"Wrote candidate summary: {md_path}")
    print(f"Candidate count: {len(candidates)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
