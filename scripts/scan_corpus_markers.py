"""Scan guide markdown for unresolved partials and routing/thin-guide markers."""

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter, defaultdict
from datetime import datetime
from pathlib import Path
from typing import Any

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from ingest import parse_frontmatter


UNRESOLVED_PARTIAL_RE = re.compile(r"{{>\s*([^}]+?)\s*}}")
TEMPLATE_MARKER_RE = re.compile(r"{{(?!>)\s*([^}]+?)\s*}}")
HTML_COMMENT_RE = re.compile(r"<!--(.*?)-->")
ROUTING_PHRASES = (
    "quick routing",
    "use this guide when",
    "hand off",
    "switch to",
    "hub guide",
)
COMMENT_WARNING_WORDS = ("todo", "svg-todo", "quick routing", "use this guide when")


def _as_bool(value: Any) -> bool:
    if isinstance(value, bool):
        return value
    if value is None:
        return False
    return str(value).strip().lower() in {"1", "true", "yes", "y", "on"}


def _as_list(value: Any) -> list[str]:
    if value is None:
        return []
    if isinstance(value, list):
        return [str(item).strip() for item in value if str(item).strip()]
    return [item.strip() for item in str(value).split(",") if item.strip()]


def _normalize_example(text: str, *, max_length: int = 140) -> str:
    normalized = " ".join(str(text).strip().split())
    if len(normalized) <= max_length:
        return normalized
    return normalized[: max_length - 3].rstrip() + "..."


def _hit(
    *,
    marker_type: str,
    severity: str,
    line_number: int,
    text: str,
    marker: str = "",
) -> dict[str, Any]:
    return {
        "type": marker_type,
        "severity": severity,
        "line": line_number,
        "marker": marker,
        "example": _normalize_example(text),
    }


def scan_guide(path: Path) -> dict[str, Any] | None:
    text = path.read_text(encoding="utf-8")
    metadata, _body = parse_frontmatter(text)
    if not metadata:
        return None

    guide_id = str(metadata.get("id") or "").strip()
    slug = str(metadata.get("slug") or path.stem).strip()
    tags = _as_list(metadata.get("tags"))
    bridge = _as_bool(metadata.get("bridge"))
    hits: list[dict[str, Any]] = []

    if bridge:
        hits.append(
            _hit(
                marker_type="bridge_frontmatter",
                severity="info",
                line_number=1,
                text="bridge: true",
                marker="bridge: true",
            )
        )

    for line_number, line in enumerate(text.splitlines(), start=1):
        for match in UNRESOLVED_PARTIAL_RE.finditer(line):
            hits.append(
                _hit(
                    marker_type="unresolved_partial",
                    severity="fail",
                    line_number=line_number,
                    text=line,
                    marker=match.group(1).strip(),
                )
            )
        for match in TEMPLATE_MARKER_RE.finditer(line):
            hits.append(
                _hit(
                    marker_type="template_marker",
                    severity="warn",
                    line_number=line_number,
                    text=line,
                    marker=match.group(1).strip(),
                )
            )
        for match in HTML_COMMENT_RE.finditer(line):
            comment = match.group(1).strip()
            lower_comment = comment.lower()
            severity = (
                "warn"
                if any(word in lower_comment for word in COMMENT_WARNING_WORDS)
                else "info"
            )
            hits.append(
                _hit(
                    marker_type="html_comment",
                    severity=severity,
                    line_number=line_number,
                    text=line,
                    marker=comment,
                )
            )

        lower_line = line.lower()
        for phrase in ROUTING_PHRASES:
            if phrase in lower_line:
                hits.append(
                    _hit(
                        marker_type="routing_phrase",
                        severity="info",
                        line_number=line_number,
                        text=line,
                        marker=phrase,
                    )
                )

    counts = Counter(hit["type"] for hit in hits)
    severities = Counter(hit["severity"] for hit in hits)
    return {
        "file": str(path),
        "source_file": path.name,
        "guide_id": guide_id,
        "slug": slug,
        "title": str(metadata.get("title") or ""),
        "category": str(metadata.get("category") or ""),
        "liability_level": str(metadata.get("liability_level") or ""),
        "tags": tags,
        "bridge": bridge,
        "marker_counts": dict(sorted(counts.items())),
        "severity_counts": dict(sorted(severities.items())),
        "hits": hits,
    }


def scan_guides(guides_dir: Path) -> dict[str, Any]:
    guide_records = []
    marker_counts: Counter[str] = Counter()
    severity_counts: Counter[str] = Counter()
    guide_counts_by_marker: defaultdict[str, int] = defaultdict(int)

    for path in sorted(guides_dir.glob("*.md")):
        record = scan_guide(path)
        if record is None:
            continue
        guide_records.append(record)
        marker_counts.update(record["marker_counts"])
        severity_counts.update(record["severity_counts"])
        for marker_type, count in record["marker_counts"].items():
            if count:
                guide_counts_by_marker[marker_type] += 1

    records_with_hits = [record for record in guide_records if record["hits"]]
    summary = {
        "generated_at": datetime.now().isoformat(timespec="seconds"),
        "guides_scanned": len(guide_records),
        "guides_with_markers": len(records_with_hits),
        "marker_counts": dict(sorted(marker_counts.items())),
        "severity_counts": dict(sorted(severity_counts.items())),
        "guide_counts_by_marker": dict(sorted(guide_counts_by_marker.items())),
    }
    return {
        "summary": summary,
        "guides": records_with_hits,
    }


def _escape_table(value: Any) -> str:
    single_line = " ".join(str(value).split())
    return single_line.replace("|", "\\|")


def _format_example(hit: dict[str, Any]) -> str:
    marker = hit.get("marker")
    if isinstance(marker, (str, int, float, bool)) and str(marker):
        detail = marker
    else:
        detail = hit.get("example", "")
    return f"L{hit.get('line', '?')} {hit.get('type', 'unknown')} {detail}"


def _hit_severity_rank(hit: dict[str, Any]) -> int:
    severity_order = {"fail": 0, "warn": 1, "info": 2}
    return severity_order.get(hit.get("severity"), 99)


def _hit_line_number(hit: dict[str, Any]) -> int:
    line = hit.get("line")
    return line if isinstance(line, int) else sys.maxsize


def render_markdown(scan: dict[str, Any]) -> str:
    summary = scan["summary"]
    lines = [
        "# Corpus Marker Scan",
        "",
        f"- Generated at: `{summary['generated_at']}`",
        f"- Guides scanned: `{summary['guides_scanned']}`",
        f"- Guides with markers: `{summary['guides_with_markers']}`",
        "",
        "## Marker Counts",
        "",
    ]
    for marker_type, count in summary["marker_counts"].items():
        guide_count = summary["guide_counts_by_marker"].get(marker_type, 0)
        lines.append(f"- `{marker_type}`: {count} hits in {guide_count} guides")
    if not summary["marker_counts"]:
        lines.append("- `none`: 0")

    lines.extend(
        [
            "",
            "## Guide Hits",
            "",
            "| severity | guide | category | liability | bridge | markers | examples |",
            "| --- | --- | --- | --- | --- | --- | --- |",
        ]
    )
    for record in sorted(
        scan["guides"],
        key=lambda item: (
            min((_hit_severity_rank(hit) for hit in item.get("hits", [])), default=99),
            item.get("guide_id", ""),
            item.get("source_file", ""),
        ),
    ):
        severities = ",".join(sorted(record["severity_counts"]))
        marker_counts = ", ".join(
            f"{key}:{value}" for key, value in record["marker_counts"].items()
        )
        example_hits = sorted(
            record["hits"],
            key=lambda hit: (_hit_severity_rank(hit), _hit_line_number(hit)),
        )
        examples = "; ".join(
            _format_example(hit) for hit in example_hits[:3]
        )
        lines.append(
            "| "
            f"{_escape_table(severities)} | "
            f"{_escape_table(record['guide_id'] or record['source_file'])} | "
            f"{_escape_table(record['category'])} | "
            f"{_escape_table(record['liability_level'])} | "
            f"{str(record['bridge']).lower()} | "
            f"{_escape_table(marker_counts)} | "
            f"{_escape_table(examples)} |"
        )
    return "\n".join(lines) + "\n"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Scan guide markdown for unresolved partials and routing markers."
    )
    parser.add_argument("--guides-dir", type=Path, default=Path("guides"))
    parser.add_argument(
        "--output-json",
        type=Path,
        default=Path("artifacts/bench/corpus_marker_scan.json"),
    )
    parser.add_argument(
        "--output-md",
        type=Path,
        default=Path("artifacts/bench/corpus_marker_scan.md"),
    )
    parser.add_argument(
        "--fail-on-unresolved",
        action="store_true",
        help="Exit non-zero when unresolved partial markers are present.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    scan = scan_guides(args.guides_dir)
    args.output_json.parent.mkdir(parents=True, exist_ok=True)
    args.output_json.write_text(
        json.dumps(scan, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )
    args.output_md.parent.mkdir(parents=True, exist_ok=True)
    args.output_md.write_text(render_markdown(scan), encoding="utf-8")
    print(
        "Scanned {guides_scanned} guides; found {guides_with_markers} with markers.".format(
            **scan["summary"]
        )
    )
    unresolved = scan["summary"]["marker_counts"].get("unresolved_partial", 0)
    if unresolved:
        print(f"Unresolved partial markers: {unresolved}")
    if args.fail_on_unresolved and unresolved:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
