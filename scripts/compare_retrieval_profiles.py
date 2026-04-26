#!/usr/bin/env python3
"""Compare retrieval-only metrics across forced retrieval profiles."""

from __future__ import annotations

import argparse
import json
import re
import sys
from datetime import datetime
from pathlib import Path
from typing import Any, Mapping, Sequence


REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts import evaluate_retrieval_pack as eval_pack


DEFAULT_PROFILES = (
    "safety_triage",
    "normal_vs_urgent",
    "compare_or_boundary",
    "how_to_task",
    "low_support",
)


STATUS_FIELD_NAMES = ("top1_is_bridge", "top1_has_unresolved_partial")
GUIDE_ID_FIELD_NAMES = ("expected_guide_ids", "primary_expected_guide_ids")
STATUS_TRUE_VALUES = {"1", "true", "yes"}


def _coerce_guide_id_list(value: Any) -> list[str]:
    extract = getattr(eval_pack, "extract_guide_ids", None)
    if callable(extract):
        return extract(value)
    if value is None:
        return []
    if isinstance(value, Sequence) and not isinstance(value, (str, bytes, bytearray)):
        values = []
        for item in value:
            values.extend(_coerce_guide_id_list(item))
        return values
    return re.findall(r"\bGD-\d+\b", str(value), flags=re.IGNORECASE)


def _status_tokens(value: Any) -> list[str]:
    if value is None:
        return []
    if isinstance(value, Sequence) and not isinstance(value, (str, bytes, bytearray)):
        tokens = []
        for item in value:
            tokens.extend(_status_tokens(item))
        return tokens
    return [
        token.strip().lower()
        for token in re.split(r"[\s,|]+", str(value))
        if token.strip()
    ]


def _coerce_status_value(value: Any) -> str:
    return "yes" if any(token in STATUS_TRUE_VALUES for token in _status_tokens(value)) else "no"


def _normalize_summary_row(row: Mapping[str, Any]) -> dict[str, Any]:
    normalized = dict(row)
    for field in GUIDE_ID_FIELD_NAMES:
        if field in normalized:
            normalized[field] = _coerce_guide_id_list(normalized.get(field))
    for field in STATUS_FIELD_NAMES:
        if field in normalized:
            normalized[field] = _coerce_status_value(normalized.get(field))
    return normalized


def _rate(summary: Mapping[str, Any], key: str) -> float | None:
    value = summary.get(key)
    if isinstance(value, Mapping):
        rate = value.get("rate")
        return float(rate) if isinstance(rate, (int, float)) else None
    return None


def _mean_owner_share(summary: Mapping[str, Any]) -> float | None:
    value = summary.get("simple_owner_share")
    if isinstance(value, Mapping):
        rate = value.get("rate")
        return float(rate) if isinstance(rate, (int, float)) else None
    return None


def profile_row(profile: str, summary: Mapping[str, Any]) -> dict[str, Any]:
    latency = summary.get("retrieval_latency_ms")
    if not isinstance(latency, Mapping):
        latency = {}
    return {
        "profile": profile,
        "total_prompts": summary.get("total_prompts", 0),
        "expected_owner_rows": summary.get("expected_owner_rows", 0),
        "retrieval_error_rows": summary.get("retrieval_error_rows", 0),
        "hit_at_1_rate": _rate(summary, "expected_hit_at_1"),
        "hit_at_3_rate": _rate(summary, "expected_hit_at_3"),
        "hit_at_k_rate": _rate(summary, "expected_hit_at_k"),
        "mean_owner_share": _mean_owner_share(summary),
        "mean_latency_ms": latency.get("mean"),
        "p95_latency_ms": latency.get("p95"),
        "top1_marker_risk_counts": summary.get("top1_marker_risk_counts") or {},
        "top1_bridge_rows": summary.get("top1_bridge_rows", 0),
        "top1_unresolved_partial_rows": summary.get("top1_unresolved_partial_rows", 0),
    }


def delta_rows(rows: Sequence[Mapping[str, Any]]) -> list[dict[str, Any]]:
    if not rows:
        return []
    baseline = rows[0]
    fields = (
        "hit_at_1_rate",
        "hit_at_3_rate",
        "hit_at_k_rate",
        "mean_owner_share",
        "mean_latency_ms",
        "p95_latency_ms",
    )
    deltas = []
    for row in rows[1:]:
        payload = {"profile": row["profile"], "baseline_profile": baseline["profile"]}
        for field in fields:
            left = row.get(field)
            right = baseline.get(field)
            payload[f"{field}_delta"] = (
                round(float(left) - float(right), 6)
                if isinstance(left, (int, float)) and isinstance(right, (int, float))
                else None
            )
        deltas.append(payload)
    return deltas


def compare_profiles(
    prompt_pack: Path,
    profiles: Sequence[str],
    *,
    collection: Any,
    top_k: int,
    category: str | None = None,
    embed_url: str | None = None,
    corpus_marker_lookup: dict[str, dict] | None = None,
    limit: int | None = None,
    progress: bool = False,
) -> dict[str, Any]:
    prompt_rows = eval_pack.load_prompt_pack(prompt_pack)
    if limit is not None:
        prompt_rows = prompt_rows[:limit]

    runs = []
    for profile in profiles:
        rows = eval_pack.evaluate_pack(
            prompt_rows,
            collection=collection,
            top_k=top_k,
            category=category,
            embed_url=embed_url,
            retrieval_profile_override=profile,
            corpus_marker_lookup=corpus_marker_lookup,
            progress=progress,
        )
        rows = [_normalize_summary_row(row) for row in rows]
        summary = eval_pack.summarize_rows(rows)
        runs.append(
            {
                "profile": profile,
                "summary": summary,
                "rows": rows,
            }
        )

    profile_rows = [profile_row(run["profile"], run["summary"]) for run in runs]
    return {
        "prompt_pack": str(prompt_pack),
        "generated_at": datetime.now().isoformat(timespec="seconds"),
        "config": {
            "profiles": list(profiles),
            "top_k": top_k,
            "category": category,
            "embed_url": embed_url,
            "limit": limit,
            "generation": "disabled",
        },
        "profile_rows": profile_rows,
        "deltas_vs_baseline": delta_rows(profile_rows),
        "runs": runs,
    }


def _md_value(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, float):
        return f"{value:.4f}".rstrip("0").rstrip(".")
    return str(value).replace("|", "\\|").replace("\r", " ").replace("\n", " ")


def _marker_risk_text(marker_risks: Mapping[Any, Any] | None) -> str:
    if not marker_risks:
        return ""
    return ", ".join(
        f"{str(key)}:{value}"
        for key, value in sorted(marker_risks.items(), key=lambda item: str(item[0]))
    )


def render_markdown(payload: Mapping[str, Any]) -> str:
    lines = [
        "# Retrieval Profile Comparison",
        "",
        f"- prompt_pack: `{payload['prompt_pack']}`",
        f"- generated_at: `{payload['generated_at']}`",
        f"- top_k: `{payload['config']['top_k']}`",
        "",
        "| profile | hit@1 | hit@3 | hit@k | owner share | mean ms | p95 ms | marker risks |",
        "| --- | --- | --- | --- | --- | --- | --- | --- |",
    ]
    for row in payload["profile_rows"]:
        marker_risks = _marker_risk_text(row.get("top1_marker_risk_counts") or {})
        lines.append(
            "| {profile} | {hit1} | {hit3} | {hitk} | {share} | {mean} | {p95} | {risks} |".format(
                profile=_md_value(row["profile"]),
                hit1=_md_value(row.get("hit_at_1_rate")),
                hit3=_md_value(row.get("hit_at_3_rate")),
                hitk=_md_value(row.get("hit_at_k_rate")),
                share=_md_value(row.get("mean_owner_share")),
                mean=_md_value(row.get("mean_latency_ms")),
                p95=_md_value(row.get("p95_latency_ms")),
                risks=_md_value(marker_risks),
            )
        )

    lines.extend(["", "## Deltas Vs Baseline", ""])
    if payload["deltas_vs_baseline"]:
        lines.append("| profile | hit@1 delta | hit@3 delta | hit@k delta | owner share delta | mean ms delta |")
        lines.append("| --- | --- | --- | --- | --- | --- |")
        for row in payload["deltas_vs_baseline"]:
            lines.append(
                "| {profile} | {hit1} | {hit3} | {hitk} | {share} | {latency} |".format(
                    profile=_md_value(row["profile"]),
                    hit1=_md_value(row.get("hit_at_1_rate_delta")),
                    hit3=_md_value(row.get("hit_at_3_rate_delta")),
                    hitk=_md_value(row.get("hit_at_k_rate_delta")),
                    share=_md_value(row.get("mean_owner_share_delta")),
                    latency=_md_value(row.get("mean_latency_ms_delta")),
                )
            )
    else:
        lines.append("- none")
    return "\n".join(lines) + "\n"


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("prompt_pack", type=Path, help="JSONL prompt pack")
    parser.add_argument("--profiles", nargs="+", default=list(DEFAULT_PROFILES))
    parser.add_argument("--top-k", type=int, default=8)
    parser.add_argument("--category", default=None)
    parser.add_argument("--embed-url", default=None)
    parser.add_argument("--corpus-marker-scan", type=Path, default=None)
    parser.add_argument("--output-json", type=Path, default=None)
    parser.add_argument("--output-md", type=Path, default=None)
    parser.add_argument("--limit", type=int, default=None)
    parser.add_argument("--progress", action="store_true")
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    corpus_marker_lookup = {}
    if args.corpus_marker_scan:
        from scripts.analyze_rag_bench_failures import load_corpus_marker_lookup

        corpus_marker_lookup = load_corpus_marker_lookup(args.corpus_marker_scan)
    payload = compare_profiles(
        args.prompt_pack,
        args.profiles,
        collection=eval_pack.load_collection(),
        top_k=args.top_k,
        category=args.category,
        embed_url=args.embed_url,
        corpus_marker_lookup=corpus_marker_lookup,
        limit=args.limit,
        progress=args.progress,
    )
    markdown = render_markdown(payload)
    if args.output_json:
        args.output_json.parent.mkdir(parents=True, exist_ok=True)
        args.output_json.write_text(
            json.dumps(eval_pack.json_safe(payload), indent=2, sort_keys=True) + "\n",
            encoding="utf-8",
        )
    if args.output_md:
        args.output_md.parent.mkdir(parents=True, exist_ok=True)
        args.output_md.write_text(markdown, encoding="utf-8")
    if not args.output_md:
        print(markdown, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
