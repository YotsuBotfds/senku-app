"""Audit guide metadata coverage for high-liability retrieval surfaces."""

from __future__ import annotations

import argparse
import json
import re
import sys
from collections.abc import Mapping
from collections import Counter
from datetime import datetime
from pathlib import Path
from typing import Any

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from ingest import parse_frontmatter
from guide_answer_card_contracts import load_answer_cards
from guide_catalog import find_malformed_frontmatter
from metadata_helpers import derive_bridge_metadata, normalize_tags


HIGH_LIABILITY_LEVELS = {"high", "critical"}
CITATION_POLICY_KEYS = (
    "citations_required",
    "requires_citations",
    "citation_required",
    "citation_policy",
    "evidence_required",
)
APPLICABILITY_KEYS = (
    "applicability",
    "applies_to",
    "use_when",
    "not_for",
    "contraindications",
)
BODY_ROUTING_PHRASES = (
    "top routing",
    "quick routing",
    "retrieval routing",
    "routing:",
    "use this guide when",
    "hand off",
    "switch to",
)
FRONTMATTER_KEY_VALUE_RE = re.compile(r"^\s*([^:]+)\s*:\s*(.*)$")
FALSE_LIKE_METADATA_STRINGS = {"", "false", "0", "no", "none", "null"}


def _is_false_like_metadata_value(value: Any) -> bool:
    if value is None:
        return True
    if isinstance(value, bool):
        return not value
    if isinstance(value, str):
        return value.strip().lower() in FALSE_LIKE_METADATA_STRINGS
    if isinstance(value, (int, float)) and not value:
        return True
    return False


def _as_list(value: Any) -> list[str]:
    if _is_false_like_metadata_value(value) or isinstance(value, Mapping):
        return []
    if isinstance(value, (list, tuple, set)):
        return [
            str(item).strip()
            for item in value
            if not _is_false_like_metadata_value(item)
            and not isinstance(item, (Mapping, list, tuple, set))
            and str(item).strip()
        ]
    return [
        item.strip()
        for item in str(value).split(",")
        if not _is_false_like_metadata_value(item)
    ]


def _has_meaningful_scalar_text(value: Any) -> bool:
    if _is_false_like_metadata_value(value) or isinstance(value, (Mapping, list, tuple, set)):
        return False
    return bool(str(value).strip())


def _has_any_key(metadata: dict[str, Any], keys: tuple[str, ...]) -> bool:
    def has_meaningful_value(value: Any) -> bool:
        if _is_false_like_metadata_value(value):
            return False
        if isinstance(value, list):
            return any(has_meaningful_value(item) for item in value)
        if isinstance(value, dict):
            return any(has_meaningful_value(item) for item in value.values())
        return value not in (None, "", [], {})

    for key in keys:
        if has_meaningful_value(metadata.get(key)):
            return True
    return False


def _frontmatter_lines(path: Path) -> dict[str, int]:
    lines = path.read_text(encoding="utf-8").splitlines()
    if not lines or lines[0].strip() != "---":
        return {}
    found: dict[str, int] = {}
    for line_number, line in enumerate(lines[1:], start=2):
        if line.strip() == "---":
            break
        if ":" not in line or line[:1].isspace():
            continue
        key = line.split(":", 1)[0].strip()
        if key:
            found.setdefault(key, line_number)
    return found


def _frontmatter_field(path: Path, key: str) -> str:
    """Extract a best-effort raw frontmatter scalar value before parsing."""
    lines = path.read_text(encoding="utf-8").splitlines()
    if not lines or lines[0].strip() != "---":
        return ""
    key_lower = key.lower()
    for line in lines[1:]:
        if line.strip() == "---":
            break
        if ":" not in line:
            continue
        match = FRONTMATTER_KEY_VALUE_RE.match(line)
        if not match:
            continue
        field = match.group(1).strip().lower()
        if field == key_lower:
            return match.group(2).strip().strip("'\"")
    return ""


def _frontmatter_body(path: Path) -> str:
    """Return guide body text even when frontmatter is malformed."""
    lines = path.read_text(encoding="utf-8").splitlines()
    if not lines or lines[0].strip() != "---":
        return "\n".join(lines)
    for index in range(1, len(lines)):
        if lines[index].strip() == "---":
            return "\n".join(lines[index + 1 :])
    return "\n".join(lines)


def _body_routing_markers(body: str) -> list[dict[str, Any]]:
    markers = []
    for line_number, line in enumerate(body.splitlines(), start=1):
        lower_line = line.lower()
        for phrase in BODY_ROUTING_PHRASES:
            if phrase in lower_line:
                markers.append(
                    {
                        "body_line": line_number,
                        "marker": phrase,
                        "example": " ".join(line.strip().split())[:160],
                    }
                )
    return markers


def _answer_card_lookup(cards_dir: Path | None) -> dict[str, list[dict[str, Any]]]:
    try:
        cards = load_answer_cards(cards_dir) if cards_dir else load_answer_cards()
    except Exception:
        cards = []
    lookup: dict[str, list[dict[str, Any]]] = {}
    for card in cards:
        guide_id = str(card.get("guide_id") or "").strip()
        if not guide_id:
            continue
        lookup.setdefault(guide_id, []).append(card)
    return lookup


def _malformed_frontmatter_record(path: Path, reason: str, cards_by_guide: dict[str, list[dict[str, Any]]]):
    cards_by_guide = cards_by_guide or {}
    guide_id = _frontmatter_field(path, "id").strip()
    liability_level = _frontmatter_field(path, "liability_level").strip().lower()
    slug = _frontmatter_field(path, "slug").strip() or path.stem
    title = _frontmatter_field(path, "title").strip()
    category = _frontmatter_field(path, "category").strip()
    high_liability = liability_level in HIGH_LIABILITY_LEVELS
    cards = cards_by_guide.get(guide_id, [])
    reviewed_cards = [
        card
        for card in cards
        if str(card.get("review_status") or "").strip().lower()
        in {"pilot_reviewed", "approved"}
    ]
    body = _frontmatter_body(path)
    body_routing_markers = _body_routing_markers(body)
    gaps = ["malformed_frontmatter"]
    severity = "warn"
    if high_liability:
        severity = "gap"
    return {
        "file": str(path),
        "source_file": path.name,
        "guide_id": guide_id,
        "slug": slug,
        "title": title,
        "category": category,
        "liability_level": liability_level,
        "high_liability": high_liability,
        "bridge": False,
        "aliases_count": 0,
        "routing_cues_count": 0,
        "has_structured_routing_support": False,
        "has_body_routing_markers": bool(body_routing_markers),
        "body_routing_marker_count": len(body_routing_markers),
        "body_routing_markers": body_routing_markers[:5],
        "related_count": 0,
        "tags_count": 0,
        "has_routing_support": False,
        "answer_card_count": len(cards),
        "reviewed_answer_card_count": len(reviewed_cards),
        "has_answer_card": bool(cards),
        "has_reviewed_answer_card": bool(reviewed_cards),
        "has_citation_policy": False,
        "has_applicability": False,
        "gaps": gaps,
        "severity": severity,
        "field_lines": {"frontmatter_error": reason},
        "frontmatter_error": reason,
    }


def audit_guide(
    path: Path,
    *,
    answer_cards_by_guide: dict[str, list[dict[str, Any]]] | None = None,
    frontmatter_issue: str | None = None,
) -> dict[str, Any] | None:
    text = path.read_text(encoding="utf-8")
    metadata, body = parse_frontmatter(text)
    if not metadata:
        if frontmatter_issue is not None:
            return _malformed_frontmatter_record(
                path,
                frontmatter_issue,
                cards_by_guide=answer_cards_by_guide,
            )
        return None

    answer_cards_by_guide = answer_cards_by_guide or {}
    guide_id = str(metadata.get("id") or "").strip()
    cards = answer_cards_by_guide.get(guide_id, [])
    tags = normalize_tags(metadata.get("tags", []))
    aliases = _as_list(metadata.get("aliases") or metadata.get("alias"))
    routing_cues = _as_list(metadata.get("routing_cues"))
    body_routing_markers = _body_routing_markers(body)
    related = _as_list(metadata.get("related"))
    liability_level = str(metadata.get("liability_level") or "").strip().lower()
    bridge = derive_bridge_metadata(
        tags,
        explicit_bridge=metadata.get("bridge", False),
    )["bridge"]
    has_structured_routing_support = bool(aliases or routing_cues or bridge)
    has_body_routing_markers = bool(body_routing_markers)
    has_answer_card = bool(cards)
    reviewed_cards = [
        card
        for card in cards
        if str(card.get("review_status") or "").strip().lower()
        in {"pilot_reviewed", "approved"}
    ]
    has_reviewed_answer_card = bool(reviewed_cards)
    has_card_citation_policy = any(
        _has_meaningful_scalar_text(card.get("runtime_citation_policy"))
        for card in cards
    )
    has_card_applicability = any(
        _has_meaningful_scalar_text(card.get("routine_boundary"))
        or _has_meaningful_scalar_text(card.get("acceptable_uncertain_fit"))
        for card in cards
    )
    has_citation_policy = _has_any_key(metadata, CITATION_POLICY_KEYS) or has_card_citation_policy
    has_applicability = _has_any_key(metadata, APPLICABILITY_KEYS) or has_card_applicability
    high_liability = liability_level in HIGH_LIABILITY_LEVELS

    gaps: list[str] = []
    if not liability_level:
        gaps.append("missing_liability_level")
    if high_liability:
        if not aliases:
            gaps.append("missing_aliases")
        if not routing_cues:
            gaps.append("missing_routing_cues")
        if not has_structured_routing_support:
            gaps.append("missing_routing_support")
        if not has_citation_policy:
            gaps.append("missing_citation_policy")
        if not has_applicability:
            gaps.append("missing_applicability")
        if not has_reviewed_answer_card:
            gaps.append("missing_reviewed_answer_card")
        if not related:
            gaps.append("missing_related")
        if not tags:
            gaps.append("missing_tags")

    severity = "none"
    if gaps:
        severity = "warn"
    if high_liability and (
        "missing_routing_support" in gaps
        or "missing_citation_policy" in gaps
        or "missing_applicability" in gaps
    ):
        severity = "gap"

    field_lines = _frontmatter_lines(path)
    return {
        "file": str(path),
        "source_file": path.name,
        "guide_id": guide_id,
        "slug": str(metadata.get("slug") or path.stem).strip(),
        "title": str(metadata.get("title") or "").strip(),
        "category": str(metadata.get("category") or "").strip(),
        "liability_level": liability_level,
        "high_liability": high_liability,
        "bridge": bool(bridge),
        "aliases_count": len(aliases),
        "routing_cues_count": len(routing_cues),
        "has_structured_routing_support": has_structured_routing_support,
        "has_body_routing_markers": has_body_routing_markers,
        "body_routing_marker_count": len(body_routing_markers),
        "body_routing_markers": body_routing_markers[:5],
        "related_count": len(related),
        "tags_count": len(tags),
        "has_routing_support": has_structured_routing_support,
        "answer_card_count": len(cards),
        "reviewed_answer_card_count": len(reviewed_cards),
        "has_answer_card": has_answer_card,
        "has_reviewed_answer_card": has_reviewed_answer_card,
        "has_citation_policy": has_citation_policy,
        "has_applicability": has_applicability,
        "gaps": gaps,
        "severity": severity,
        "field_lines": field_lines,
    }


def audit_guides(
    guides_dir: Path,
    *,
    cards_dir: Path | None = None,
) -> dict[str, Any]:
    answer_cards_by_guide = _answer_card_lookup(cards_dir)
    malformed_frontmatter = sorted(
        [
            {
                "source_file": issue["source_file"],
                "frontmatter_error": issue["reason"],
            }
            for issue in find_malformed_frontmatter(guides_dir)
        ],
        key=lambda issue: issue["source_file"],
    )
    malformed_by_source = {
        issue["source_file"]: issue["frontmatter_error"] for issue in malformed_frontmatter
    }
    frontmatter_error_counts = Counter(
        issue["frontmatter_error"] for issue in malformed_frontmatter
    )
    records = []
    for path in sorted(guides_dir.glob("*.md")):
        record = audit_guide(
            path,
            answer_cards_by_guide=answer_cards_by_guide,
            frontmatter_issue=malformed_by_source.get(path.name),
        )
        if record is not None:
            records.append(record)

    high_records = [record for record in records if record["high_liability"]]
    gap_records = [record for record in high_records if record["gaps"]]
    gap_counts: Counter[str] = Counter()
    severity_counts: Counter[str] = Counter()
    liability_counts: Counter[str] = Counter()
    for record in records:
        liability_counts[record["liability_level"] or "missing"] += 1
        severity_counts[record["severity"]] += 1
        gap_counts.update(record["gaps"])

    summary = {
        "generated_at": datetime.now().isoformat(timespec="seconds"),
        "guides_scanned": len(records),
        "high_liability_guides": len(high_records),
        "high_liability_guides_with_gaps": len(gap_records),
        "malformed_frontmatter_count": len(malformed_frontmatter),
        "gap_counts": dict(sorted(gap_counts.items())),
        "severity_counts": dict(sorted(severity_counts.items())),
        "liability_counts": dict(sorted(liability_counts.items())),
        "answer_card_guides": len(answer_cards_by_guide),
        "frontmatter_errors": malformed_frontmatter,
        "frontmatter_error_counts": dict(sorted(frontmatter_error_counts.items())),
    }
    return {
        "summary": summary,
        "guides": records,
    }


def _escape_table(value: Any) -> str:
    return str(value).replace("|", "\\|").replace("\n", " ")


def render_markdown(audit: dict[str, Any]) -> str:
    summary = audit["summary"]
    lines = [
        "# Metadata Coverage Audit",
        "",
        f"- Generated at: `{summary['generated_at']}`",
        f"- Guides scanned: `{summary['guides_scanned']}`",
        f"- High-liability guides: `{summary['high_liability_guides']}`",
        f"- High-liability guides with gaps: `{summary['high_liability_guides_with_gaps']}`",
        f"- Malformed frontmatter files: `{summary['malformed_frontmatter_count']}`",
        "",
        "## Gap Counts",
        "",
    ]
    for gap, count in summary["gap_counts"].items():
        lines.append(f"- `{gap}`: {count}")
    if not summary["gap_counts"]:
        lines.append("- `none`: 0")

    malformed_frontmatter = summary["frontmatter_errors"]
    if malformed_frontmatter:
        lines.extend(
            [
                "",
                "## Malformed Frontmatter",
                "",
                "| source_file | frontmatter_error |",
                "| --- | --- |",
            ]
        )
        for issue in malformed_frontmatter:
            lines.append(
                "| "
                f"{_escape_table(issue['source_file'])} | "
                f"{_escape_table(issue['frontmatter_error'])} |"
            )

    lines.extend(
        [
            "",
            "## High-Liability Gaps",
            "",
            "| severity | guide | category | liability | aliases | structured routing | body routing hints | related | tags | citation | applicability | gaps |",
            "| --- | --- | --- | --- | ---: | ---: | ---: | ---: | ---: | --- | --- | --- |",
        ]
    )
    severity_order = {"gap": 0, "warn": 1, "none": 2}
    high_gap_records = [
        record
        for record in audit["guides"]
        if record["high_liability"] and record["gaps"]
    ]
    for record in sorted(
        high_gap_records,
        key=lambda item: (
            severity_order.get(item["severity"], 99),
            item["category"],
            item["guide_id"],
        ),
    ):
        guide_label = record["guide_id"] or record["source_file"]
        lines.append(
            "| "
            f"{_escape_table(record['severity'])} | "
            f"{_escape_table(guide_label)} | "
            f"{_escape_table(record['category'])} | "
            f"{_escape_table(record['liability_level'])} | "
            f"{record['aliases_count']} | "
            f"{record['routing_cues_count']} | "
            f"{record['body_routing_marker_count']} | "
            f"{record['related_count']} | "
            f"{record['tags_count']} | "
            f"{str(record['has_citation_policy']).lower()} | "
            f"{str(record['has_applicability']).lower()} | "
            f"{_escape_table(','.join(record['gaps']))} |"
        )
    if not high_gap_records:
        lines.append(
            "| none | none | none | none | 0 | 0 | 0 | 0 | 0 | true | true | none |"
        )
    return "\n".join(lines) + "\n"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Audit metadata coverage for high-liability guide surfaces."
    )
    parser.add_argument("--guides-dir", type=Path, default=Path("guides"))
    parser.add_argument(
        "--cards-dir",
        type=Path,
        default=None,
        help="Optional guide answer-card directory for reviewed-card coverage.",
    )
    parser.add_argument(
        "--output-json",
        type=Path,
        default=Path("artifacts/bench/metadata_coverage_audit.json"),
    )
    parser.add_argument(
        "--output-md",
        type=Path,
        default=Path("artifacts/bench/metadata_coverage_audit.md"),
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    audit = audit_guides(args.guides_dir, cards_dir=args.cards_dir)
    args.output_json.parent.mkdir(parents=True, exist_ok=True)
    args.output_json.write_text(
        json.dumps(audit, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )
    args.output_md.parent.mkdir(parents=True, exist_ok=True)
    args.output_md.write_text(render_markdown(audit), encoding="utf-8")
    print(
        "Scanned {guides_scanned} guides; found {high_liability_guides_with_gaps} "
        "high-liability guides with gaps.".format(**audit["summary"])
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
