"""Join diagnostics and audits into a guide-quality work shortlist."""

from __future__ import annotations

import argparse
import json
from collections import Counter, defaultdict
from datetime import datetime
from pathlib import Path
from typing import Any


GOOD_BUCKETS = {"deterministic_pass", "expected_supported"}
IMPORTANT_GAPS = (
    "missing_reviewed_answer_card",
    "missing_routing_support",
    "missing_aliases",
    "missing_citation_policy",
    "missing_applicability",
)


def _load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def _split_ids(value: Any) -> list[str]:
    if value in ("", None, "unknown"):
        return []
    if isinstance(value, str):
        return [
            item.strip()
            for item in value.replace(",", "|").split("|")
            if item.strip()
        ]
    if isinstance(value, list):
        return [str(item).strip() for item in value if str(item).strip()]
    return []


def _diagnostics_json_path(path: Path) -> Path:
    if path.is_dir():
        return path / "diagnostics.json"
    return path


def _metadata_lookup(path: Path | None) -> dict[str, dict[str, Any]]:
    if path is None:
        return {}
    payload = _load_json(path)
    lookup = {}
    for record in payload.get("guides", []):
        guide_id = str(record.get("guide_id") or "").strip()
        if guide_id:
            lookup[guide_id] = record
    return lookup


def _marker_lookup(path: Path | None) -> dict[str, dict[str, Any]]:
    if path is None:
        return {}
    payload = _load_json(path)
    lookup = {}
    for record in payload.get("guides", []):
        guide_id = str(record.get("guide_id") or "").strip()
        if guide_id:
            lookup[guide_id] = record
    return lookup


def _new_record(guide_id: str) -> dict[str, Any]:
    return {
        "guide_id": guide_id,
        "prompt_rows": 0,
        "bad_rows": 0,
        "expected_rows": 0,
        "cited_rows": 0,
        "top1_rows": 0,
        "topk_rows": 0,
        "buckets": Counter(),
        "app_acceptance": Counter(),
        "artifacts": Counter(),
        "prompt_examples": [],
    }


def _add_example(record: dict[str, Any], row: dict[str, Any]) -> None:
    if len(record["prompt_examples"]) >= 3:
        return
    record["prompt_examples"].append(
        {
            "artifact": row.get("artifact_name") or "",
            "prompt_index": row.get("prompt_index") or "",
            "bucket": row.get("suspected_failure_bucket") or "",
            "prompt": row.get("prompt_text") or "",
        }
    )


def _candidate_action(
    *,
    bad_rows: int,
    high_liability: bool,
    gaps: list[str],
    marker_counts: dict[str, Any],
) -> tuple[str, str]:
    if marker_counts.get("unresolved_partial"):
        return (
            "repair_corpus_partial",
            "guide is observed in diagnostics and has unresolved partial markers",
        )
    if high_liability and "missing_reviewed_answer_card" in gaps:
        return (
            "consider_reviewed_answer_card",
            "high-liability observed guide lacks reviewed answer-card coverage",
        )
    if "missing_routing_support" in gaps:
        return (
            "add_routing_metadata",
            "observed guide lacks aliases, routing cues, bridge, or body routing markers",
        )
    if bad_rows:
        return (
            "inspect_diagnostic_failure",
            "observed guide participates in non-passing diagnostic rows",
        )
    return (
        "regression_monitor",
        "observed guide is currently passing but appears in measured prompt panels",
    )


def collect_priorities(
    diagnostic_paths: list[Path],
    *,
    metadata_audit_path: Path | None = None,
    corpus_marker_scan_path: Path | None = None,
) -> dict[str, Any]:
    metadata = _metadata_lookup(metadata_audit_path)
    markers = _marker_lookup(corpus_marker_scan_path)
    records: defaultdict[str, dict[str, Any]] = defaultdict(lambda: _new_record(""))

    for diagnostic_path in diagnostic_paths:
        payload = _load_json(_diagnostics_json_path(diagnostic_path))
        for row in payload.get("rows", []):
            if not isinstance(row, dict):
                continue
            expected_ids = _split_ids(row.get("expected_guide_ids"))
            cited_ids = _split_ids(row.get("cited_guide_ids"))
            top_ids = _split_ids(row.get("top_retrieved_guide_ids"))
            related_ids = list(dict.fromkeys(expected_ids + cited_ids + top_ids[:3]))
            bucket = str(row.get("suspected_failure_bucket") or "")
            app_status = str(row.get("app_acceptance_status") or "")
            bad = bool(bucket and bucket not in GOOD_BUCKETS)
            for guide_id in related_ids:
                record = records[guide_id]
                if not record["guide_id"]:
                    record["guide_id"] = guide_id
                record["prompt_rows"] += 1
                record["buckets"][bucket] += 1
                if app_status:
                    record["app_acceptance"][app_status] += 1
                record["artifacts"][row.get("artifact_name") or "unknown"] += 1
                if bad:
                    record["bad_rows"] += 1
                if guide_id in expected_ids:
                    record["expected_rows"] += 1
                if guide_id in cited_ids:
                    record["cited_rows"] += 1
                if top_ids and guide_id == top_ids[0]:
                    record["top1_rows"] += 1
                if guide_id in top_ids:
                    record["topk_rows"] += 1
                _add_example(record, row)

    output_records = []
    for guide_id, record in records.items():
        meta = metadata.get(guide_id, {})
        marker = markers.get(guide_id, {})
        gaps = list(meta.get("gaps") or [])
        marker_counts = marker.get("marker_counts") or {}
        high_liability = bool(meta.get("high_liability"))
        candidate_action, reason = _candidate_action(
            bad_rows=record["bad_rows"],
            high_liability=high_liability,
            gaps=gaps,
            marker_counts=marker_counts,
        )
        score = (
            record["bad_rows"] * 8
            + record["expected_rows"] * 3
            + record["top1_rows"] * 2
            + (5 if high_liability else 0)
            + (3 if "missing_reviewed_answer_card" in gaps else 0)
            + (2 if "missing_routing_support" in gaps else 0)
            + (3 if marker_counts.get("unresolved_partial") else 0)
        )
        output_records.append(
            {
                "guide_id": guide_id,
                "score": score,
                "prompt_rows": record["prompt_rows"],
                "bad_rows": record["bad_rows"],
                "expected_rows": record["expected_rows"],
                "cited_rows": record["cited_rows"],
                "top1_rows": record["top1_rows"],
                "topk_rows": record["topk_rows"],
                "category": meta.get("category") or "",
                "liability_level": meta.get("liability_level") or "",
                "high_liability": high_liability,
                "metadata_gaps": [gap for gap in gaps if gap in IMPORTANT_GAPS],
                "has_reviewed_answer_card": bool(meta.get("has_reviewed_answer_card")),
                "marker_types": sorted(
                    key for key, count in marker_counts.items() if count
                ),
                "candidate_action": candidate_action,
                "reason": reason,
                "buckets": dict(record["buckets"]),
                "app_acceptance": dict(record["app_acceptance"]),
                "artifacts": dict(record["artifacts"]),
                "prompt_examples": record["prompt_examples"],
            }
        )

    output_records.sort(
        key=lambda item: (
            -int(item["score"]),
            -int(item["bad_rows"]),
            -int(item["expected_rows"]),
            item["guide_id"],
        )
    )
    return {
        "summary": {
            "generated_at": datetime.now().isoformat(timespec="seconds"),
            "diagnostic_inputs": [str(path) for path in diagnostic_paths],
            "guides_ranked": len(output_records),
            "high_liability_guides_ranked": sum(
                1 for record in output_records if record["high_liability"]
            ),
        },
        "guides": output_records,
    }


def _escape_table(value: Any) -> str:
    return str(value).replace("|", "\\|").replace("\n", " ")


def render_markdown(payload: dict[str, Any], *, limit: int = 40) -> str:
    summary = payload["summary"]
    lines = [
        "# Guide Quality Work Priorities",
        "",
        f"- Generated at: `{summary['generated_at']}`",
        f"- Diagnostic inputs: `{len(summary['diagnostic_inputs'])}`",
        f"- Guides ranked: `{summary['guides_ranked']}`",
        f"- High-liability guides ranked: `{summary['high_liability_guides_ranked']}`",
        "",
        "## Top Guides",
        "",
        "| score | guide | category | liability | prompts | bad | expected | top1 | reviewed card | action | gaps | markers | buckets | reason |",
        "| ---: | --- | --- | --- | ---: | ---: | ---: | ---: | --- | --- | --- | --- | --- | --- |",
    ]
    for record in payload["guides"][:limit]:
        gaps = ",".join(record["metadata_gaps"]) or "none"
        markers = ",".join(record["marker_types"]) or "none"
        buckets = ",".join(
            f"{bucket}:{count}" for bucket, count in sorted(record["buckets"].items())
        )
        lines.append(
            "| "
            f"{record['score']} | "
            f"{_escape_table(record['guide_id'])} | "
            f"{_escape_table(record['category'])} | "
            f"{_escape_table(record['liability_level'])} | "
            f"{record['prompt_rows']} | "
            f"{record['bad_rows']} | "
            f"{record['expected_rows']} | "
            f"{record['top1_rows']} | "
            f"{str(record['has_reviewed_answer_card']).lower()} | "
            f"{_escape_table(record['candidate_action'])} | "
            f"{_escape_table(gaps)} | "
            f"{_escape_table(markers)} | "
            f"{_escape_table(buckets)} | "
            f"{_escape_table(record['reason'])} |"
        )
    return "\n".join(lines) + "\n"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Prioritize guide-quality work from diagnostics and audits."
    )
    parser.add_argument("diagnostics", nargs="+", type=Path)
    parser.add_argument("--metadata-audit", type=Path, default=None)
    parser.add_argument("--corpus-marker-scan", type=Path, default=None)
    parser.add_argument(
        "--output-json",
        type=Path,
        default=Path("artifacts/bench/guide_quality_priorities.json"),
    )
    parser.add_argument(
        "--output-md",
        type=Path,
        default=Path("artifacts/bench/guide_quality_priorities.md"),
    )
    parser.add_argument("--limit", type=int, default=40)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    payload = collect_priorities(
        args.diagnostics,
        metadata_audit_path=args.metadata_audit,
        corpus_marker_scan_path=args.corpus_marker_scan,
    )
    args.output_json.parent.mkdir(parents=True, exist_ok=True)
    args.output_json.write_text(
        json.dumps(payload, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )
    args.output_md.parent.mkdir(parents=True, exist_ok=True)
    args.output_md.write_text(
        render_markdown(payload, limit=args.limit),
        encoding="utf-8",
    )
    print(
        "Ranked {guides_ranked} guides from {count} diagnostic input(s).".format(
            count=len(args.diagnostics),
            **payload["summary"],
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
