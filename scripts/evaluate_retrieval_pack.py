#!/usr/bin/env python3
"""Generation-free retrieval evaluator for JSONL prompt packs."""

from __future__ import annotations

import argparse
import json
import math
import re
import sys
import time
from collections import Counter
from datetime import datetime
from pathlib import Path
from typing import Any, Mapping, Sequence


REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))


GUIDE_ID_RE = re.compile(r"\bGD-\d+\b", re.IGNORECASE)
EXPECTED_GUIDE_KEYS = (
    "expected_guide_id",
    "expected_guide_ids",
    "expected_guides",
    "expected_source_guide_ids",
    "target_guide_id",
    "target_guide_ids",
    "target_guides",
    "guide_id",
    "guide_ids",
)
PRIMARY_EXPECTED_GUIDE_KEYS = (
    "primary_expected_guide_id",
    "primary_expected_guide_ids",
    "primary_expected_guides",
    "primary_target_guide_id",
    "primary_target_guide_ids",
    "primary_target_guides",
)
PROMPT_KEYS = ("prompt", "question")


def _clean_text(value: Any) -> str:
    if value is None:
        return ""
    return str(value).strip()


def extract_guide_ids(value: Any) -> list[str]:
    """Extract normalized guide IDs from common scalar/list/dict shapes."""
    if value is None:
        return []
    if isinstance(value, Mapping):
        values: list[str] = []
        for key in (
            "guide_id",
            "guide_ids",
            "id",
            *EXPECTED_GUIDE_KEYS,
            *PRIMARY_EXPECTED_GUIDE_KEYS,
        ):
            values.extend(extract_guide_ids(value.get(key)))
        return _dedupe(values)
    if isinstance(value, Sequence) and not isinstance(value, (str, bytes, bytearray)):
        values = []
        for item in value:
            values.extend(extract_guide_ids(item))
        return _dedupe(values)

    text = _clean_text(value)
    matches = GUIDE_ID_RE.findall(text)
    if matches:
        return _dedupe(match.upper() for match in matches)
    return []


def _dedupe(values: Sequence[str]) -> list[str]:
    seen = set()
    result = []
    for value in values:
        cleaned = _clean_text(value).upper()
        if cleaned and cleaned not in seen:
            seen.add(cleaned)
            result.append(cleaned)
    return result


def expected_guide_ids(record: Mapping[str, Any]) -> list[str]:
    """Return expected owner guide IDs from prompt-pack metadata."""
    values = []
    for key in EXPECTED_GUIDE_KEYS:
        if key in record:
            values.extend(extract_guide_ids(record.get(key)))
    return _dedupe(values)


def primary_expected_guide_ids(record: Mapping[str, Any]) -> list[str]:
    """Return primary expected guide IDs from prompt-pack metadata."""
    values = []
    for key in PRIMARY_EXPECTED_GUIDE_KEYS:
        if key in record:
            values.extend(extract_guide_ids(record.get(key)))
    return _dedupe(values)


def load_prompt_pack(path: Path) -> list[dict[str, Any]]:
    """Load a JSONL prompt pack while preserving all metadata fields."""
    rows: list[dict[str, Any]] = []
    with path.open("r", encoding="utf-8") as handle:
        for line_number, raw_line in enumerate(handle, start=1):
            line = raw_line.strip()
            if not line:
                continue
            try:
                record = json.loads(line)
            except json.JSONDecodeError as exc:
                raise ValueError(f"Invalid JSONL at {path}:{line_number}: {exc}") from exc
            if not isinstance(record, dict):
                raise ValueError(f"JSONL record at {path}:{line_number} is not an object")
            prompt = first_present(record, PROMPT_KEYS)
            if not prompt:
                raise ValueError(
                    f"JSONL record at {path}:{line_number} is missing prompt/question"
                )
            rows.append(
                {
                    "line_number": line_number,
                    "prompt_id": first_present(record, ("id", "prompt_id")),
                    "section": first_present(record, ("section",)) or "Core Regression",
                    "lane": first_present(record, ("lane",)),
                    "style": first_present(record, ("style",)),
                    "target_behavior": first_present(record, ("target_behavior",)),
                    "prompt": prompt,
                    "record": record,
                    "expected_guide_ids": expected_guide_ids(record),
                    "primary_expected_guide_ids": primary_expected_guide_ids(record),
                }
            )
    return rows


def first_present(record: Mapping[str, Any], keys: Sequence[str]) -> str:
    for key in keys:
        value = _clean_text(record.get(key))
        if value:
            return value
    return ""


def guide_ids_from_results(results: Mapping[str, Any]) -> list[str]:
    """Extract ranked guide IDs from a Chroma-like retrieval result payload."""
    metadatas = results.get("metadatas") if isinstance(results, Mapping) else None
    if not metadatas:
        return []
    if (
        isinstance(metadatas, Sequence)
        and metadatas
        and isinstance(metadatas[0], Sequence)
        and not isinstance(metadatas[0], Mapping)
    ):
        rows = metadatas[0]
    else:
        rows = metadatas

    guide_ids = []
    for meta in rows:
        if isinstance(meta, Mapping):
            guide_id = extract_guide_ids(meta.get("guide_id"))
            guide_ids.append(guide_id[0] if guide_id else "")
    return guide_ids


def row_metrics(
    expected_ids: Sequence[str],
    retrieved_ids: Sequence[str],
    *,
    top_k: int | None = None,
) -> dict[str, Any]:
    """Compute retrieval owner metrics for one prompt."""
    expected = set(_dedupe(expected_ids))
    retrieved = [_clean_text(item).upper() for item in retrieved_ids if _clean_text(item)]
    if top_k is not None and top_k >= 0:
        retrieved = retrieved[:top_k]

    best_rank = None
    for index, guide_id in enumerate(retrieved, start=1):
        if guide_id in expected:
            best_rank = index
            break

    owner_count = sum(1 for guide_id in retrieved if guide_id in expected)
    retrieved_count = len(retrieved)
    distractors = Counter(guide_id for guide_id in retrieved if guide_id not in expected)

    return {
        "expected_hit_at_1": bool(retrieved[:1] and retrieved[0] in expected),
        "expected_hit_at_3": any(guide_id in expected for guide_id in retrieved[:3]),
        "expected_hit_at_k": owner_count > 0,
        "expected_owner_best_rank": best_rank,
        "expected_owner_count": owner_count,
        "retrieved_count": retrieved_count,
        "owner_share": (owner_count / retrieved_count) if retrieved_count else 0.0,
        "top_distractor_guide_ids": [
            {"guide_id": guide_id, "count": count}
            for guide_id, count in distractors.most_common()
        ],
    }


def primary_row_metrics(
    primary_ids: Sequence[str],
    retrieved_ids: Sequence[str],
    *,
    top_k: int | None = None,
) -> dict[str, Any]:
    """Compute primary-owner metrics for one prompt."""
    metrics = row_metrics(primary_ids, retrieved_ids, top_k=top_k)
    return {
        "primary_hit_at_1": metrics["expected_hit_at_1"],
        "primary_hit_at_3": metrics["expected_hit_at_3"],
        "primary_hit_at_k": metrics["expected_hit_at_k"],
        "primary_best_rank": metrics["expected_owner_best_rank"],
        "primary_owner_best_rank": metrics["expected_owner_best_rank"],
    }


def summarize_rows(rows: Sequence[Mapping[str, Any]], *, distractor_limit: int = 10) -> dict[str, Any]:
    expected_rows = [
        row
        for row in rows
        if row.get("expected_guide_ids") and not row.get("error")
    ]
    error_count = sum(1 for row in rows if row.get("error"))
    denominator = len(expected_rows)
    primary_rows = [
        row
        for row in rows
        if row.get("primary_expected_guide_ids") and not row.get("error")
    ]
    primary_denominator = len(primary_rows)
    best_ranks = [
        int(row["expected_owner_best_rank"])
        for row in expected_rows
        if row.get("expected_owner_best_rank") is not None
    ]
    primary_best_ranks = [
        int(row["primary_owner_best_rank"])
        for row in primary_rows
        if row.get("primary_owner_best_rank") is not None
    ]
    owner_slots = sum(int(row.get("expected_owner_count") or 0) for row in expected_rows)
    retrieved_slots = sum(int(row.get("retrieved_count") or 0) for row in expected_rows)
    distractors = Counter()
    marker_risks = Counter()
    latencies = []
    top1_bridge_rows = 0
    top1_unresolved_partial_rows = 0
    for row in expected_rows:
        latency = row.get("retrieval_elapsed_ms")
        if isinstance(latency, (int, float)):
            latencies.append(float(latency))
        risk = str(row.get("top1_marker_risk") or "").strip()
        if risk:
            marker_risks[risk] += 1
        if _truthy_marker_value(row.get("top1_is_bridge")):
            top1_bridge_rows += 1
        if _truthy_marker_value(row.get("top1_has_unresolved_partial")):
            top1_unresolved_partial_rows += 1
        for item in row.get("top_distractor_guide_ids") or []:
            if isinstance(item, Mapping):
                distractors[str(item.get("guide_id") or "")] += int(item.get("count") or 0)

    return {
        "total_prompts": len(rows),
        "expected_owner_rows": denominator,
        "retrieval_error_rows": error_count,
        "expected_hit_at_1": ratio_summary(
            sum(1 for row in expected_rows if row.get("expected_hit_at_1")),
            denominator,
        ),
        "expected_hit_at_3": ratio_summary(
            sum(1 for row in expected_rows if row.get("expected_hit_at_3")),
            denominator,
        ),
        "expected_hit_at_k": ratio_summary(
            sum(1 for row in expected_rows if row.get("expected_hit_at_k")),
            denominator,
        ),
        "expected_owner_best_rank": (
            f"{(sum(best_ranks) / len(best_ranks)):.2f}" if best_ranks else "unknown"
        ),
        "expected_owner_ranked_rows": len(best_ranks),
        "primary_expected_rows": primary_denominator,
        "primary_hit_at_1": ratio_summary(
            sum(1 for row in primary_rows if row.get("primary_hit_at_1")),
            primary_denominator,
        ),
        "primary_hit_at_3": ratio_summary(
            sum(1 for row in primary_rows if row.get("primary_hit_at_3")),
            primary_denominator,
        ),
        "primary_hit_at_k": ratio_summary(
            sum(1 for row in primary_rows if row.get("primary_hit_at_k")),
            primary_denominator,
        ),
        "primary_owner_best_rank": (
            f"{(sum(primary_best_ranks) / len(primary_best_ranks)):.2f}"
            if primary_best_ranks
            else "unknown"
        ),
        "primary_best_rank": (
            f"{(sum(primary_best_ranks) / len(primary_best_ranks)):.2f}"
            if primary_best_ranks
            else "unknown"
        ),
        "primary_owner_ranked_rows": len(primary_best_ranks),
        "simple_owner_share": ratio_summary(owner_slots, retrieved_slots),
        "retrieval_latency_ms": latency_summary(latencies),
        "top1_marker_risk_counts": dict(marker_risks),
        "top1_bridge_rows": top1_bridge_rows,
        "top1_unresolved_partial_rows": top1_unresolved_partial_rows,
        "top_distractor_guide_ids": [
            {"guide_id": guide_id, "count": count}
            for guide_id, count in distractors.most_common(distractor_limit)
            if guide_id
        ],
    }


def latency_summary(values: Sequence[float]) -> dict[str, Any]:
    if not values:
        return {"count": 0, "mean": None, "p95": None}
    sorted_values = sorted(values)
    p95_index = min(len(sorted_values) - 1, max(0, math.ceil(len(sorted_values) * 0.95) - 1))
    return {
        "count": len(sorted_values),
        "mean": round(sum(sorted_values) / len(sorted_values), 3),
        "p95": round(sorted_values[p95_index], 3),
    }


def _truthy_marker_value(value: Any) -> bool:
    return str(value or "").strip().lower() in {"1", "true", "yes"}


def ratio_summary(numerator: int, denominator: int) -> dict[str, Any]:
    return {
        "count": numerator,
        "total": denominator,
        "rate": (numerator / denominator) if denominator else None,
        "text": ratio_text(numerator, denominator),
    }


def ratio_text(numerator: int, denominator: int) -> str:
    if denominator <= 0:
        return "unknown"
    return f"{numerator}/{denominator} ({(numerator / denominator) * 100:.1f}%)"


def retrieve_for_prompt(
    prompt: str,
    collection: Any,
    *,
    top_k: int,
    category: str | None,
    embed_url: str | None,
    retrieval_profile_override: str | None = None,
) -> tuple[list[str], dict[str, Any]]:
    """Run the repository retrieval wrapper without invoking generation."""
    from bench import retrieve_chunks

    if retrieval_profile_override:
        import query

        original = query._retrieval_profile_for_question

        def forced_profile(_question: str) -> str:
            return str(retrieval_profile_override)

        query._retrieval_profile_for_question = forced_profile
        try:
            results, _sub_queries, retrieval_meta = retrieve_chunks(
                prompt,
                collection,
                top_k,
                category=category,
                embed_url=embed_url,
            )
        finally:
            query._retrieval_profile_for_question = original
    else:
        results, _sub_queries, retrieval_meta = retrieve_chunks(
            prompt,
            collection,
            top_k,
            category=category,
            embed_url=embed_url,
        )
    return guide_ids_from_results(results), {
        "retrieval_meta": retrieval_meta,
    }


def load_collection() -> Any:
    import chromadb
    import config

    client = chromadb.PersistentClient(path=config.CHROMA_DB_DIR)
    return client.get_collection("senku_guides")


def evaluate_pack(
    prompt_rows: Sequence[Mapping[str, Any]],
    *,
    collection: Any,
    top_k: int,
    category: str | None = None,
    embed_url: str | None = None,
    retrieval_profile_override: str | None = None,
    corpus_marker_lookup: dict[str, dict] | None = None,
    progress: bool = False,
) -> list[dict[str, Any]]:
    rows = []
    corpus_marker_lookup = corpus_marker_lookup or {}
    for index, entry in enumerate(prompt_rows, start=1):
        prompt = str(entry["prompt"])
        expected_ids = _dedupe(entry.get("expected_guide_ids") or [])
        primary_ids = _dedupe(entry.get("primary_expected_guide_ids") or [])
        row = {
            "prompt_index": index,
            "line_number": entry.get("line_number"),
            "prompt_id": entry.get("prompt_id") or "",
            "section": entry.get("section") or "",
            "lane": entry.get("lane") or "",
            "style": entry.get("style") or "",
            "target_behavior": entry.get("target_behavior") or "",
            "prompt": prompt,
            "expected_guide_ids": expected_ids,
            "primary_expected_guide_ids": primary_ids,
        }
        if progress:
            label = row["prompt_id"] or f"line {row['line_number']}"
            print(f"[{index}/{len(prompt_rows)}] retrieving {label}", file=sys.stderr, flush=True)
        try:
            start = time.perf_counter()
            retrieved_ids, meta = retrieve_for_prompt(
                prompt,
                collection,
                top_k=top_k,
                category=category,
                embed_url=embed_url,
                retrieval_profile_override=retrieval_profile_override,
            )
            row["retrieval_elapsed_ms"] = round((time.perf_counter() - start) * 1000, 3)
            row["top_retrieved_guide_ids"] = retrieved_ids
            row.update(row_metrics(expected_ids, retrieved_ids, top_k=top_k))
            if primary_ids:
                row.update(primary_row_metrics(primary_ids, retrieved_ids, top_k=top_k))
            if corpus_marker_lookup:
                from scripts.analyze_rag_bench_failures import top1_marker_fields

                row.update(top1_marker_fields(retrieved_ids, corpus_marker_lookup))
            row.update(meta)
        except Exception as exc:  # pragma: no cover - live retrieval failures vary.
            row["top_retrieved_guide_ids"] = []
            row["error"] = f"{type(exc).__name__}: {exc}"
        rows.append(row)
    return rows


def render_markdown(payload: Mapping[str, Any]) -> str:
    summary = payload["summary"]
    config = payload["config"]
    lines = [
        "# Retrieval Pack Evaluation",
        "",
        f"- prompt_pack: `{payload['prompt_pack']}`",
        f"- generated_at: `{payload['generated_at']}`",
        f"- top_k: `{config['top_k']}`",
        f"- category: `{config.get('category') or ''}`",
        f"- embed_url: `{config.get('embed_url') or ''}`",
        f"- retrieval_profile_override: `{config.get('retrieval_profile_override') or ''}`",
        f"- total_prompts: {summary['total_prompts']}",
        f"- expected_owner_rows: {summary['expected_owner_rows']}",
        f"- primary_expected_rows: {summary['primary_expected_rows']}",
        f"- retrieval_error_rows: {summary['retrieval_error_rows']}",
        "",
        "| metric | value |",
        "| --- | --- |",
        f"| expected hit@1 | {summary['expected_hit_at_1']['text']} |",
        f"| expected hit@3 | {summary['expected_hit_at_3']['text']} |",
        f"| expected hit@k | {summary['expected_hit_at_k']['text']} |",
        f"| expected owner best rank | {summary['expected_owner_best_rank']} |",
        f"| primary hit@1 | {summary['primary_hit_at_1']['text']} |",
        f"| primary hit@3 | {summary['primary_hit_at_3']['text']} |",
        f"| primary hit@k | {summary['primary_hit_at_k']['text']} |",
        f"| primary best rank | {summary['primary_best_rank']} |",
        f"| simple owner share | {summary['simple_owner_share']['text']} |",
        f"| retrieval latency mean ms | {summary['retrieval_latency_ms']['mean'] or ''} |",
        f"| retrieval latency p95 ms | {summary['retrieval_latency_ms']['p95'] or ''} |",
        "",
        "## Top Distractors",
        "",
    ]
    distractors = summary.get("top_distractor_guide_ids") or []
    if distractors:
        lines.extend(
            f"- {item['guide_id']}: {item['count']}" for item in distractors
        )
    else:
        lines.append("- none")

    lines.extend(
        [
            "",
            "## Rows",
            "",
            "| # | id | expected | primary_guide_ids | top retrieved | hit@1 | hit@3 | hit@k | best rank | primary hit@1 | primary hit@3 | primary hit@k | primary best rank | owner share | error |",
            "| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |",
        ]
    )
    for row in payload["rows"]:
        expected = "|".join(_dedupe(row.get("expected_guide_ids") or []))
        primary = "|".join(_dedupe(row.get("primary_expected_guide_ids") or []))
        retrieved = "|".join((row.get("top_retrieved_guide_ids") or [])[: config["top_k"]])
        lines.append(
            "| {idx} | {pid} | {expected} | {primary} | {retrieved} | {hit1} | {hit3} | "
            "{hitk} | {rank} | {primary_hit1} | {primary_hit3} | {primary_hitk} | "
            "{primary_rank} | {share:.4f} | {error} |".format(
                idx=row.get("prompt_index"),
                pid=_md_cell(row.get("prompt_id") or ""),
                expected=_md_cell(expected),
                primary=_md_cell(primary),
                retrieved=_md_cell(retrieved),
                hit1="yes" if row.get("expected_hit_at_1") else "no",
                hit3="yes" if row.get("expected_hit_at_3") else "no",
                hitk="yes" if row.get("expected_hit_at_k") else "no",
                rank=row.get("expected_owner_best_rank") or "",
                primary_hit1=_yes_no_blank(row.get("primary_hit_at_1")),
                primary_hit3=_yes_no_blank(row.get("primary_hit_at_3")),
                primary_hitk=_yes_no_blank(row.get("primary_hit_at_k")),
                primary_rank=row.get("primary_best_rank")
                or row.get("primary_owner_best_rank")
                or "",
                share=float(row.get("owner_share") or 0.0),
                error=_md_cell(row.get("error") or ""),
            )
        )
    return "\n".join(lines) + "\n"


def _md_cell(value: Any) -> str:
    return str(value).replace("|", "\\|").replace("\n", " ").strip()


def _yes_no_blank(value: Any) -> str:
    if value is None:
        return ""
    return "yes" if value else "no"


def build_payload(
    *,
    prompt_pack: Path,
    rows: Sequence[Mapping[str, Any]],
    top_k: int,
    category: str | None,
    embed_url: str | None,
) -> dict[str, Any]:
    return json_safe(
        {
            "prompt_pack": str(prompt_pack),
            "generated_at": datetime.now().isoformat(timespec="seconds"),
            "config": {
                "top_k": top_k,
                "category": category,
                "embed_url": embed_url,
                "retrieval_profile_override": None,
                "generation": "disabled",
            },
            "summary": summarize_rows(rows),
            "rows": list(rows),
        }
    )


def json_safe(value: Any) -> Any:
    if isinstance(value, Mapping):
        return {str(key): json_safe(item) for key, item in value.items()}
    if isinstance(value, (list, tuple)):
        return [json_safe(item) for item in value]
    if isinstance(value, set):
        return [json_safe(item) for item in sorted(value, key=str)]
    if isinstance(value, (str, int, float, bool)) or value is None:
        return value
    return str(value)


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Evaluate retrieval-only expected-owner metrics for a JSONL prompt pack."
    )
    parser.add_argument("prompt_pack", type=Path, help="JSONL prompt pack to evaluate")
    parser.add_argument("--top-k", type=int, default=24, help="retrieval depth")
    parser.add_argument("--category", default=None, help="optional retrieval category filter")
    parser.add_argument(
        "--embed-url",
        default=None,
        help="embedding endpoint override, for example http://127.0.0.1:1234/v1",
    )
    parser.add_argument("--output-json", type=Path, default=None, help="write JSON report")
    parser.add_argument("--output-md", type=Path, default=None, help="write Markdown report")
    parser.add_argument("--limit", type=int, default=None, help="evaluate only the first N prompts")
    parser.add_argument(
        "--retrieval-profile-override",
        default=None,
        help="Force one retrieval profile inside this analyzer run only.",
    )
    parser.add_argument(
        "--corpus-marker-scan",
        type=Path,
        default=None,
        help="Optional JSON output from scripts/scan_corpus_markers.py.",
    )
    parser.add_argument("--progress", action="store_true", help="print progress to stderr")
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    prompt_rows = load_prompt_pack(args.prompt_pack)
    if args.limit is not None:
        prompt_rows = prompt_rows[: args.limit]
    collection = load_collection()
    corpus_marker_lookup = {}
    if args.corpus_marker_scan:
        from scripts.analyze_rag_bench_failures import load_corpus_marker_lookup

        corpus_marker_lookup = load_corpus_marker_lookup(args.corpus_marker_scan)
    rows = evaluate_pack(
        prompt_rows,
        collection=collection,
        top_k=args.top_k,
        category=args.category,
        embed_url=args.embed_url,
        retrieval_profile_override=args.retrieval_profile_override,
        corpus_marker_lookup=corpus_marker_lookup,
        progress=args.progress,
    )
    payload = build_payload(
        prompt_pack=args.prompt_pack,
        rows=rows,
        top_k=args.top_k,
        category=args.category,
        embed_url=args.embed_url,
    )
    payload["config"]["retrieval_profile_override"] = args.retrieval_profile_override
    markdown = render_markdown(payload)

    if args.output_json:
        args.output_json.parent.mkdir(parents=True, exist_ok=True)
        args.output_json.write_text(
            json.dumps(payload, indent=2, sort_keys=True) + "\n",
            encoding="utf-8",
        )
    if args.output_md:
        args.output_md.parent.mkdir(parents=True, exist_ok=True)
        args.output_md.write_text(markdown, encoding="utf-8")
    if not args.output_md:
        print(markdown, end="")
    return 1 if payload["summary"]["retrieval_error_rows"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
