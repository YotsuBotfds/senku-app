"""Rank observed guide families for high-liability RAG follow-up work."""

from __future__ import annotations

import argparse
import json
from collections import Counter, defaultdict
from datetime import datetime
from pathlib import Path
from typing import Any


BAD_BUCKETS = {
    "retrieval_miss",
    "ranking_miss",
    "generation_miss",
    "unsupported_or_truncated_answer",
    "safety_contract_miss",
    "artifact_error",
}


def _load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def _diagnostics_json_path(path: Path) -> Path:
    return path / "diagnostics.json" if path.is_dir() else path


def _split_ids(value: Any) -> list[str]:
    if value in ("", None, "unknown"):
        return []
    if isinstance(value, str):
        return [item.strip() for item in value.split("|") if item.strip()]
    if isinstance(value, list):
        return [str(item).strip() for item in value if str(item).strip()]
    return []


def _as_int(value: Any) -> int | None:
    if value in ("", None, "unknown"):
        return None
    try:
        return int(float(str(value).strip()))
    except (TypeError, ValueError):
        return None


def _as_float(value: Any) -> float | None:
    if value in ("", None, "unknown"):
        return None
    try:
        return float(str(value).strip())
    except (TypeError, ValueError):
        return None


def _metadata_lookup(path: Path | None) -> dict[str, dict[str, Any]]:
    if path is None:
        return {}
    payload = _load_json(path)
    return {
        str(record.get("guide_id") or "").strip(): record
        for record in payload.get("guides", [])
        if str(record.get("guide_id") or "").strip()
    }


def _marker_lookup(path: Path | None) -> dict[str, dict[str, Any]]:
    if path is None:
        return {}
    payload = _load_json(path)
    return {
        str(record.get("guide_id") or "").strip(): record
        for record in payload.get("guides", [])
        if str(record.get("guide_id") or "").strip()
    }


def _family_key(row: dict[str, Any], expected_ids: list[str]) -> str:
    family = str(row.get("expected_guide_family") or "").strip()
    if family and family != "unknown":
        return family
    if expected_ids:
        return "|".join(expected_ids)
    return "unknown"


def _new_record(family: str) -> dict[str, Any]:
    return {
        "expected_guide_family": family,
        "expected_guide_ids": set(),
        "prompt_rows": 0,
        "safety_rows": 0,
        "non_deterministic_rows": 0,
        "generated_rows": 0,
        "bad_bucket_rows": 0,
        "uncertain_fit_rows": 0,
        "ranking_drift_rows": 0,
        "retrieval_miss_rows": 0,
        "non_expected_owner_cited_rows": 0,
        "card_pass_rows": 0,
        "card_gap_rows": 0,
        "card_missing_rows": 0,
        "card_not_evaluable_rows": 0,
        "owner_best_rank_total": 0.0,
        "owner_best_rank_count": 0,
        "owner_top3_share_total": 0.0,
        "owner_top3_share_count": 0,
        "owner_topk_share_total": 0.0,
        "owner_topk_share_count": 0,
        "buckets": Counter(),
        "app_acceptance": Counter(),
        "recurring_distractors": Counter(),
    }


def _candidate_action(record: dict[str, Any]) -> tuple[str, str]:
    if record["top1_unresolved_partial_rows"] or record["corpus_unresolved_partial_guides"]:
        return (
            "repair_corpus_partial",
            "observed family has unresolved partial evidence in retrieved or expected guides",
        )
    if record["card_gap_rows"] or record["card_missing_rows"]:
        return (
            "expand_or_fix_answer_cards",
            "observed family has missing or incomplete answer-card coverage",
        )
    if record["ranking_drift_rows"] or record["non_expected_owner_cited_rows"]:
        return (
            "inspect_retrieval_ranking",
            "expected owners are present but competing owners or citations need review",
        )
    if record["metadata_gap_guide_count"]:
        return (
            "add_targeted_metadata",
            "observed family has high-liability metadata gaps",
        )
    return ("regression_monitor", "observed family is currently passing")


def collect_family_priorities(
    diagnostic_paths: list[Path],
    *,
    metadata_audit_path: Path | None = None,
    corpus_marker_scan_path: Path | None = None,
) -> dict[str, Any]:
    metadata = _metadata_lookup(metadata_audit_path)
    markers = _marker_lookup(corpus_marker_scan_path)
    families: dict[str, dict[str, Any]] = {}

    for diagnostic_path in diagnostic_paths:
        payload = _load_json(_diagnostics_json_path(diagnostic_path))
        for row in payload.get("rows", []):
            if not isinstance(row, dict):
                continue
            expected_ids = _split_ids(row.get("expected_guide_ids"))
            family = _family_key(row, expected_ids)
            if family == "unknown":
                continue
            record = families.setdefault(family, _new_record(family))
            cited_ids = _split_ids(row.get("cited_guide_ids"))
            top_ids = _split_ids(row.get("top_retrieved_guide_ids"))
            expected = set(expected_ids)
            bucket = str(row.get("suspected_failure_bucket") or "")
            app_status = str(row.get("app_acceptance_status") or "")
            card_status = str(row.get("answer_card_status") or "")

            record["prompt_rows"] += 1
            record["expected_guide_ids"].update(expected_ids)
            record["buckets"][bucket] += 1
            if app_status:
                record["app_acceptance"][app_status] += 1
            if str(row.get("safety_critical") or "").lower() in {"true", "yes", "1"}:
                record["safety_rows"] += 1
            deterministic_row = row.get("decision_path") == "deterministic"
            if not deterministic_row:
                record["non_deterministic_rows"] += 1
            if row.get("generated") == "yes":
                record["generated_rows"] += 1
            if bucket in BAD_BUCKETS:
                record["bad_bucket_rows"] += 1
            if app_status == "uncertain_fit_accepted":
                record["uncertain_fit_rows"] += 1
            if bucket == "ranking_miss":
                record["ranking_drift_rows"] += 1
            if bucket == "retrieval_miss":
                record["retrieval_miss_rows"] += 1
            if card_status == "pass":
                record["card_pass_rows"] += 1
            elif card_status in {"partial", "fail"}:
                record["card_gap_rows"] += 1
            elif card_status in {"no_cards", "no_guide_ids"} and not deterministic_row:
                record["card_missing_rows"] += 1
            elif card_status in {"no_cards", "no_guide_ids", "no_generated_answer"}:
                record["card_not_evaluable_rows"] += 1

            non_expected_cited = [guide_id for guide_id in cited_ids if guide_id not in expected]
            if non_expected_cited:
                record["non_expected_owner_cited_rows"] += 1
                record["recurring_distractors"].update(non_expected_cited)
            record["recurring_distractors"].update(
                guide_id for guide_id in top_ids[:3] if guide_id not in expected
            )

            best_rank = _as_float(row.get("expected_owner_best_rank"))
            if best_rank is not None:
                record["owner_best_rank_total"] += best_rank
                record["owner_best_rank_count"] += 1
            top3_share = _as_float(row.get("expected_owner_top3_share"))
            if top3_share is not None:
                record["owner_top3_share_total"] += top3_share
                record["owner_top3_share_count"] += 1
            topk_share = _as_float(row.get("expected_owner_topk_share"))
            if topk_share is not None:
                record["owner_topk_share_total"] += topk_share
                record["owner_topk_share_count"] += 1

            if row.get("top1_marker_risk") == "fail":
                record.setdefault("top1_marker_fail_rows", 0)
                record["top1_marker_fail_rows"] += 1
            if row.get("top1_has_unresolved_partial") == "yes":
                record.setdefault("top1_unresolved_partial_rows", 0)
                record["top1_unresolved_partial_rows"] += 1

    output = []
    for family, record in families.items():
        expected_ids = sorted(record["expected_guide_ids"])
        metadata_gap_count = 0
        missing_routing_support = 0
        missing_citation_policy = 0
        missing_applicability = 0
        reviewed_card_guide_count = 0
        corpus_unresolved_partial_guides = 0
        for guide_id in expected_ids:
            meta = metadata.get(guide_id, {})
            gaps = set(meta.get("gaps") or [])
            if gaps:
                metadata_gap_count += 1
            if "missing_routing_support" in gaps:
                missing_routing_support += 1
            if "missing_citation_policy" in gaps:
                missing_citation_policy += 1
            if "missing_applicability" in gaps:
                missing_applicability += 1
            if meta.get("has_reviewed_answer_card"):
                reviewed_card_guide_count += 1
            marker_counts = (markers.get(guide_id) or {}).get("marker_counts") or {}
            if marker_counts.get("unresolved_partial"):
                corpus_unresolved_partial_guides += 1

        record["metadata_gap_guide_count"] = metadata_gap_count
        record["missing_routing_support_count"] = missing_routing_support
        record["missing_citation_policy_count"] = missing_citation_policy
        record["missing_applicability_count"] = missing_applicability
        record["reviewed_card_guide_count"] = reviewed_card_guide_count
        record["corpus_unresolved_partial_guides"] = corpus_unresolved_partial_guides
        record.setdefault("top1_marker_fail_rows", 0)
        record.setdefault("top1_unresolved_partial_rows", 0)

        action, reason = _candidate_action(record)
        score = (
            record["bad_bucket_rows"] * 12
            + record["retrieval_miss_rows"] * 10
            + record["ranking_drift_rows"] * 8
            + record["non_expected_owner_cited_rows"] * 5
            + record["metadata_gap_guide_count"] * 2
            + record["missing_routing_support_count"] * 2
            + record["missing_citation_policy_count"]
            + record["missing_applicability_count"]
            + record["top1_marker_fail_rows"] * 5
            + record["top1_unresolved_partial_rows"] * 6
            + record["corpus_unresolved_partial_guides"] * 4
            + record["card_missing_rows"]
        )
        output.append(
            {
                "expected_guide_family": family,
                "expected_guide_ids": expected_ids,
                "prompt_rows": record["prompt_rows"],
                "safety_rows": record["safety_rows"],
                "non_deterministic_rows": record["non_deterministic_rows"],
                "generated_rows": record["generated_rows"],
                "bad_bucket_rows": record["bad_bucket_rows"],
                "uncertain_fit_rows": record["uncertain_fit_rows"],
                "ranking_drift_rows": record["ranking_drift_rows"],
                "retrieval_miss_rows": record["retrieval_miss_rows"],
                "expected_owner_avg_best_rank": _average(
                    record["owner_best_rank_total"], record["owner_best_rank_count"]
                ),
                "expected_owner_top3_share_avg": _average(
                    record["owner_top3_share_total"], record["owner_top3_share_count"]
                ),
                "expected_owner_topk_share_avg": _average(
                    record["owner_topk_share_total"], record["owner_topk_share_count"]
                ),
                "non_expected_owner_cited_rows": record["non_expected_owner_cited_rows"],
                "card_pass_rows": record["card_pass_rows"],
                "card_gap_rows": record["card_gap_rows"],
                "card_missing_rows": record["card_missing_rows"],
                "card_not_evaluable_rows": record["card_not_evaluable_rows"],
                "reviewed_card_guide_count": reviewed_card_guide_count,
                "metadata_gap_guide_count": metadata_gap_count,
                "missing_routing_support_count": missing_routing_support,
                "missing_citation_policy_count": missing_citation_policy,
                "missing_applicability_count": missing_applicability,
                "top1_marker_fail_rows": record["top1_marker_fail_rows"],
                "top1_unresolved_partial_rows": record["top1_unresolved_partial_rows"],
                "corpus_unresolved_partial_guides": corpus_unresolved_partial_guides,
                "recurring_distractor_ids": [
                    guide_id for guide_id, _count in record["recurring_distractors"].most_common(8)
                ],
                "candidate_action": action,
                "priority_score": score,
                "reason": reason,
                "buckets": dict(record["buckets"]),
                "app_acceptance": dict(record["app_acceptance"]),
            }
        )

    output.sort(
        key=lambda item: (
            -int(item["priority_score"]),
            -int(item["bad_bucket_rows"]),
            -int(item["prompt_rows"]),
            item["expected_guide_family"],
        )
    )
    for index, item in enumerate(output, start=1):
        item["rank"] = index
    return {
        "summary": {
            "generated_at": datetime.now().isoformat(timespec="seconds"),
            "diagnostic_inputs": [str(path) for path in diagnostic_paths],
            "families_ranked": len(output),
        },
        "families": output,
    }


def _average(total: float, count: int) -> str:
    if count <= 0:
        return "unknown"
    return f"{total / count:.4f}"


def _escape_table(value: Any) -> str:
    return str(value).replace("|", "\\|").replace("\n", " ")


def render_markdown(payload: dict[str, Any], *, limit: int = 40) -> str:
    summary = payload["summary"]
    lines = [
        "# High-Liability Family Priorities",
        "",
        f"- Generated at: `{summary['generated_at']}`",
        f"- Diagnostic inputs: `{len(summary['diagnostic_inputs'])}`",
        f"- Families ranked: `{summary['families_ranked']}`",
        "",
        "| rank | score | family | expected guides | prompts | bad | rank drift | top1 partial | corpus partial | card pass/gap/missing/skipped | metadata gaps | action | reason |",
        "| ---: | ---: | --- | --- | ---: | ---: | ---: | ---: | ---: | --- | ---: | --- | --- |",
    ]
    for row in payload["families"][:limit]:
        card_text = (
            f"{row['card_pass_rows']}/{row['card_gap_rows']}/"
            f"{row['card_missing_rows']}/{row['card_not_evaluable_rows']}"
        )
        lines.append(
            "| "
            f"{row['rank']} | "
            f"{row['priority_score']} | "
            f"{_escape_table(row['expected_guide_family'])} | "
            f"{_escape_table(','.join(row['expected_guide_ids']))} | "
            f"{row['prompt_rows']} | "
            f"{row['bad_bucket_rows']} | "
            f"{row['ranking_drift_rows']} | "
            f"{row['top1_unresolved_partial_rows']} | "
            f"{row['corpus_unresolved_partial_guides']} | "
            f"{card_text} | "
            f"{row['metadata_gap_guide_count']} | "
            f"{_escape_table(row['candidate_action'])} | "
            f"{_escape_table(row['reason'])} |"
        )
    return "\n".join(lines) + "\n"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Rank observed expected-guide families for RAG follow-up."
    )
    parser.add_argument("diagnostics", nargs="+", type=Path)
    parser.add_argument("--metadata-audit", type=Path, default=None)
    parser.add_argument("--corpus-marker-scan", type=Path, default=None)
    parser.add_argument(
        "--output-json",
        type=Path,
        default=Path("artifacts/bench/high_liability_family_priorities.json"),
    )
    parser.add_argument(
        "--output-md",
        type=Path,
        default=Path("artifacts/bench/high_liability_family_priorities.md"),
    )
    parser.add_argument("--limit", type=int, default=40)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    payload = collect_family_priorities(
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
    args.output_md.write_text(render_markdown(payload, limit=args.limit), encoding="utf-8")
    print(
        "Ranked {families_ranked} families from {count} diagnostic input(s).".format(
            count=len(args.diagnostics),
            **payload["summary"],
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
