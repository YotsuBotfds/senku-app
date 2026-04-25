"""Helpers for exporting and comparing bench artifacts."""

from __future__ import annotations

import csv
import io
import json
import re
from pathlib import Path


SOURCE_HEADERS = (
    "**Sources:**",
    "**Retrieved Context (not explicitly cited):**",
)

SECTION_HEADER_RE = re.compile(r"^##\s+(\d+)\.\s+(.+)$", re.MULTILINE)

METADATA_PREFIXES = (
    "*Section:",
    "*Decision path:",
    "*Query decomposed into",
    "*Generation:",
    "*Runtime:",
    "*Retrieval metadata:",
)


def resolve_json_artifact_path(path):
    """Return the JSON artifact path for a bench artifact input."""
    artifact_path = Path(path)
    if artifact_path.suffix.lower() == ".md":
        json_path = artifact_path.with_suffix(".json")
        if json_path.exists():
            return json_path
    return artifact_path


def paired_markdown_path(path):
    """Return the paired markdown path for a JSON bench artifact."""
    artifact_path = Path(path)
    if artifact_path.suffix.lower() == ".json":
        return artifact_path.with_suffix(".md")
    return artifact_path


def parse_bench_markdown_responses(markdown_text):
    """Extract response text by prompt index from a bench markdown report."""
    matches = list(SECTION_HEADER_RE.finditer(markdown_text))
    responses = {}

    for index, match in enumerate(matches):
        prompt_index = int(match.group(1))
        section_start = match.end()
        section_end = matches[index + 1].start() if index + 1 < len(matches) else len(markdown_text)
        section_body = markdown_text[section_start:section_end]
        responses[prompt_index] = _extract_response_from_section(section_body)

    return responses


def parse_bench_markdown_results(markdown_text):
    """Extract minimal result rows from a standalone markdown bench report."""
    matches = list(SECTION_HEADER_RE.finditer(markdown_text))
    results = []

    for index, match in enumerate(matches):
        prompt_index = int(match.group(1))
        question = match.group(2).strip()
        section_start = match.end()
        section_end = matches[index + 1].start() if index + 1 < len(matches) else len(markdown_text)
        section_body = markdown_text[section_start:section_end]
        section_name = _extract_metadata_value(section_body, "*Section:")
        results.append(
            {
                "index": prompt_index,
                "section": section_name,
                "question": question,
                "response_text": _extract_response_from_section(section_body),
            }
        )

    return results


def _extract_response_from_section(section_body):
    """Return just the model response content from one markdown prompt section."""
    content = section_body
    for source_header in SOURCE_HEADERS:
        marker = f"\n{source_header}\n"
        if marker in content:
            content = content.split(marker, 1)[0]
            break

    lines = []
    started = False
    for raw_line in content.splitlines():
        line = raw_line.rstrip()
        stripped = line.strip()
        if not started:
            if not stripped:
                continue
            if any(stripped.startswith(prefix) for prefix in METADATA_PREFIXES):
                continue
            if stripped.startswith("**ERROR:**"):
                return ""
            started = True
        lines.append(line)

    return "\n".join(lines).strip()


def _extract_metadata_value(section_body, prefix):
    for raw_line in section_body.splitlines():
        stripped = raw_line.strip()
        if stripped.startswith(prefix):
            return stripped[len(prefix):].strip().strip("*").strip()
    return ""


def load_bench_artifact(path):
    """Load a bench JSON artifact and hydrate response text when needed."""
    json_path = resolve_json_artifact_path(path)
    if json_path.suffix.lower() == ".md":
        markdown_text = json_path.read_text(encoding="utf-8")
        return {
            "path": json_path,
            "markdown_path": json_path,
            "data": {
                "results": parse_bench_markdown_results(markdown_text),
            },
        }
    data = json.loads(json_path.read_text(encoding="utf-8"))
    response_fallbacks = {}
    markdown_path = paired_markdown_path(json_path)
    if markdown_path.exists():
        response_fallbacks = parse_bench_markdown_responses(markdown_path.read_text(encoding="utf-8"))

    for result in data.get("results", []):
        if result.get("response_text"):
            continue
        fallback = response_fallbacks.get(result.get("index"))
        if fallback:
            result["response_text"] = fallback

    return {
        "path": json_path,
        "markdown_path": markdown_path if markdown_path.exists() else None,
        "data": data,
    }


def build_eval_rows(paths):
    """Flatten one or more artifacts into evaluator-friendly rows."""
    rows = []
    for raw_path in paths:
        artifact = load_bench_artifact(raw_path)
        data = artifact["data"]
        config = data.get("config", {})
        summary = data.get("summary", {})
        for result in data.get("results", []):
            retrieval_metadata = result.get("retrieval_metadata") or {}
            top_retrieved_guide_ids = retrieval_metadata.get("top_retrieved_guide_ids") or []
            rows.append({
                "artifact_name": artifact["path"].name,
                "artifact_path": str(artifact["path"]),
                "artifact_timestamp": data.get("timestamp"),
                "artifact_section_filter": data.get("section_filter"),
                "artifact_total_prompts": summary.get("total_prompts"),
                "model": config.get("model"),
                "runtime_profile": config.get("runtime_profile"),
                "mode": config.get("mode"),
                "temperature": config.get("temperature"),
                "top_k": config.get("top_k"),
                "max_completion_tokens": config.get("max_completion_tokens"),
                "rag": config.get("rag"),
                "system_prompt": config.get("system_prompt"),
                "index": result.get("index"),
                "section": result.get("section"),
                "question": result.get("question"),
                "response_text": result.get("response_text", ""),
                "error": result.get("error"),
                "error_category": result.get("error_category"),
                "decision_path": result.get("decision_path"),
                "decision_detail": result.get("decision_detail"),
                "source_mode": result.get("source_mode"),
                "cited_guide_ids": ",".join(result.get("cited_guide_ids", [])),
                "citation_count": result.get("citation_count"),
                "duplicate_citation_count": result.get("duplicate_citation_count"),
                "generation_time": result.get("generation_time"),
                "prompt_tokens": result.get("prompt_tokens"),
                "completion_tokens": result.get("completion_tokens"),
                "finish_reason": result.get("finish_reason"),
                "completion_cap_hit": result.get("completion_cap_hit"),
                "completion_retry_count": result.get("completion_retry_count"),
                "retrieval_metadata_summary": result.get("retrieval_metadata_summary"),
                "top_retrieved_guide_ids": ",".join(top_retrieved_guide_ids),
                "response_length": result.get("response_length"),
                "server": result.get("server"),
                "worker": result.get("worker"),
            })
    return rows


def eval_rows_to_jsonl(rows):
    """Render evaluator rows as JSONL text."""
    return "\n".join(json.dumps(row, ensure_ascii=False) for row in rows)


def eval_rows_to_csv(rows):
    """Render evaluator rows as CSV text."""
    fieldnames = [
        "artifact_name",
        "artifact_path",
        "artifact_timestamp",
        "artifact_section_filter",
        "artifact_total_prompts",
        "model",
        "runtime_profile",
        "mode",
        "temperature",
        "top_k",
        "max_completion_tokens",
        "rag",
        "system_prompt",
        "index",
        "section",
        "question",
        "response_text",
        "error",
        "error_category",
        "decision_path",
        "decision_detail",
        "source_mode",
        "cited_guide_ids",
        "citation_count",
        "duplicate_citation_count",
        "generation_time",
        "prompt_tokens",
        "completion_tokens",
        "finish_reason",
        "completion_cap_hit",
        "completion_retry_count",
        "retrieval_metadata_summary",
        "top_retrieved_guide_ids",
        "response_length",
        "server",
        "worker",
    ]
    buffer = io.StringIO()
    writer = csv.DictWriter(buffer, fieldnames=fieldnames, extrasaction="ignore")
    writer.writeheader()
    for row in rows:
        writer.writerow(row)
    return buffer.getvalue()


def compare_artifacts(baseline_path, candidate_path):
    """Compare two bench artifacts and return structured deltas."""
    baseline = load_bench_artifact(baseline_path)
    candidate = load_bench_artifact(candidate_path)
    baseline_data = baseline["data"]
    candidate_data = candidate["data"]
    baseline_rows = {
        _row_key(row): row
        for row in baseline_data.get("results", [])
    }
    candidate_rows = {
        _row_key(row): row
        for row in candidate_data.get("results", [])
    }

    common_keys = sorted(set(baseline_rows) & set(candidate_rows))
    only_baseline = [baseline_rows[key] for key in sorted(set(baseline_rows) - set(candidate_rows))]
    only_candidate = [candidate_rows[key] for key in sorted(set(candidate_rows) - set(baseline_rows))]

    row_deltas = []
    for key in common_keys:
        row_deltas.append(_compare_result_rows(baseline_rows[key], candidate_rows[key]))

    error_regressions = [row for row in row_deltas if row["candidate_error"] and not row["baseline_error"]]
    error_fixes = [row for row in row_deltas if row["baseline_error"] and not row["candidate_error"]]
    cap_hit_regressions = [row for row in row_deltas if row["candidate_cap_hit"] and not row["baseline_cap_hit"]]
    cap_hit_fixes = [row for row in row_deltas if row["baseline_cap_hit"] and not row["candidate_cap_hit"]]
    decision_path_changes = [row for row in row_deltas if row["decision_path_changed"]]
    source_mode_changes = [row for row in row_deltas if row["source_mode_changed"]]

    slower = sorted(
        [row for row in row_deltas if row["generation_time_delta"] is not None and row["generation_time_delta"] > 0],
        key=lambda row: row["generation_time_delta"],
        reverse=True,
    )
    faster = sorted(
        [row for row in row_deltas if row["generation_time_delta"] is not None and row["generation_time_delta"] < 0],
        key=lambda row: row["generation_time_delta"],
    )
    duplicate_outliers = sorted(
        [
            row for row in row_deltas
            if abs(row["duplicate_citation_delta"]) >= 2
            or (row["candidate_duplicate_citation_count"] or 0) >= 4
        ],
        key=lambda row: (abs(row["duplicate_citation_delta"]), row["candidate_duplicate_citation_count"]),
        reverse=True,
    )

    return {
        "baseline": {
            "path": str(baseline["path"]),
            "name": baseline["path"].name,
            "config": baseline_data.get("config", {}),
            "summary": baseline_data.get("summary", {}),
        },
        "candidate": {
            "path": str(candidate["path"]),
            "name": candidate["path"].name,
            "config": candidate_data.get("config", {}),
            "summary": candidate_data.get("summary", {}),
        },
        "prompt_overlap": len(common_keys),
        "only_in_baseline": only_baseline,
        "only_in_candidate": only_candidate,
        "row_deltas": row_deltas,
        "error_regressions": error_regressions,
        "error_fixes": error_fixes,
        "cap_hit_regressions": cap_hit_regressions,
        "cap_hit_fixes": cap_hit_fixes,
        "decision_path_changes": decision_path_changes,
        "source_mode_changes": source_mode_changes,
        "slowest_regressions": slower[:10],
        "fastest_improvements": faster[:10],
        "duplicate_outliers": duplicate_outliers[:10],
        "replay_packs": {
            "errors": [row["question"] for row in error_regressions],
            "cap_hits": [row["question"] for row in cap_hit_regressions],
            "duplicate_citations": [row["question"] for row in duplicate_outliers[:10]],
        },
    }


def render_artifact_comparison_markdown(comparison):
    """Render a human-readable markdown comparison report."""
    baseline = comparison["baseline"]
    candidate = comparison["candidate"]
    baseline_summary = baseline["summary"]
    candidate_summary = candidate["summary"]

    lines = [
        "# Bench Artifact Comparison",
        "",
        f"Baseline: `{baseline['name']}`",
        f"Candidate: `{candidate['name']}`",
        "",
        "## Summary",
        "",
        f"- overlap prompts: {comparison['prompt_overlap']}",
        f"- baseline errors: {baseline_summary.get('errors', 0)}",
        f"- candidate errors: {candidate_summary.get('errors', 0)}",
        f"- baseline duplicate citations: {baseline_summary.get('duplicate_citation_total', 0)}",
        f"- candidate duplicate citations: {candidate_summary.get('duplicate_citation_total', 0)}",
        f"- baseline cap-hit prompts: {baseline_summary.get('cap_hit_prompts', 0)}",
        f"- candidate cap-hit prompts: {candidate_summary.get('cap_hit_prompts', 0)}",
        f"- baseline wall duration: {baseline_summary.get('wall_duration', 'n/a')}",
        f"- candidate wall duration: {candidate_summary.get('wall_duration', 'n/a')}",
        "",
    ]

    _append_delta_section(lines, "Error Regressions", comparison["error_regressions"], _render_error_row)
    _append_delta_section(lines, "Error Fixes", comparison["error_fixes"], _render_error_row)
    _append_delta_section(lines, "Decision Path Changes", comparison["decision_path_changes"], _render_path_row)
    _append_delta_section(lines, "Source Mode Changes", comparison["source_mode_changes"], _render_source_mode_row)
    _append_delta_section(lines, "Largest Latency Regressions", comparison["slowest_regressions"], _render_latency_row)
    _append_delta_section(lines, "Largest Latency Improvements", comparison["fastest_improvements"], _render_latency_row)
    _append_delta_section(lines, "Duplicate Citation Outliers", comparison["duplicate_outliers"], _render_duplicate_row)
    _append_delta_section(lines, "Cap-Hit Regressions", comparison["cap_hit_regressions"], _render_cap_hit_row)
    _append_delta_section(lines, "Cap-Hit Fixes", comparison["cap_hit_fixes"], _render_cap_hit_row)

    lines.extend([
        "## Replay Packs",
        "",
    ])
    for label, prompts in comparison["replay_packs"].items():
        lines.append(f"### {label}")
        lines.append("")
        if prompts:
            lines.append("```text")
            lines.extend(prompts)
            lines.append("```")
        else:
            lines.append("_none_")
        lines.append("")

    return "\n".join(lines).rstrip() + "\n"


def _row_key(row):
    return (
        row.get("section"),
        row.get("question"),
        row.get("index"),
    )


def _compare_result_rows(baseline_row, candidate_row):
    baseline_time = baseline_row.get("generation_time")
    candidate_time = candidate_row.get("generation_time")
    if baseline_time is None or candidate_time is None:
        generation_delta = None
    else:
        generation_delta = round(candidate_time - baseline_time, 3)

    baseline_dup = baseline_row.get("duplicate_citation_count") or 0
    candidate_dup = candidate_row.get("duplicate_citation_count") or 0

    return {
        "section": baseline_row.get("section") or candidate_row.get("section"),
        "question": baseline_row.get("question") or candidate_row.get("question"),
        "baseline_error": bool(baseline_row.get("error")),
        "candidate_error": bool(candidate_row.get("error")),
        "baseline_error_category": baseline_row.get("error_category"),
        "candidate_error_category": candidate_row.get("error_category"),
        "baseline_decision_path": baseline_row.get("decision_path"),
        "candidate_decision_path": candidate_row.get("decision_path"),
        "decision_path_changed": baseline_row.get("decision_path") != candidate_row.get("decision_path"),
        "baseline_source_mode": baseline_row.get("source_mode"),
        "candidate_source_mode": candidate_row.get("source_mode"),
        "source_mode_changed": baseline_row.get("source_mode") != candidate_row.get("source_mode"),
        "baseline_generation_time": baseline_time,
        "candidate_generation_time": candidate_time,
        "generation_time_delta": generation_delta,
        "baseline_duplicate_citation_count": baseline_dup,
        "candidate_duplicate_citation_count": candidate_dup,
        "duplicate_citation_delta": candidate_dup - baseline_dup,
        "baseline_cap_hit": bool(baseline_row.get("completion_cap_hit")),
        "candidate_cap_hit": bool(candidate_row.get("completion_cap_hit")),
        "baseline_finish_reason": baseline_row.get("finish_reason"),
        "candidate_finish_reason": candidate_row.get("finish_reason"),
    }


def _append_delta_section(lines, title, rows, renderer):
    lines.extend([f"## {title}", ""])
    if not rows:
        lines.extend(["_none_", ""])
        return
    for row in rows:
        lines.append(renderer(row))
    lines.append("")


def _render_error_row(row):
    before = row.get("baseline_error_category") or "none"
    after = row.get("candidate_error_category") or "none"
    return f"- `{row['section']}` | {row['question']} | error: `{before}` -> `{after}`"


def _render_path_row(row):
    return (
        f"- `{row['section']}` | {row['question']} | "
        f"`{row['baseline_decision_path']}` -> `{row['candidate_decision_path']}`"
    )


def _render_source_mode_row(row):
    return (
        f"- `{row['section']}` | {row['question']} | "
        f"`{row['baseline_source_mode']}` -> `{row['candidate_source_mode']}`"
    )


def _render_latency_row(row):
    return (
        f"- `{row['section']}` | {row['question']} | "
        f"{row['baseline_generation_time']}s -> {row['candidate_generation_time']}s "
        f"({row['generation_time_delta']:+.3f}s)"
    )


def _render_duplicate_row(row):
    return (
        f"- `{row['section']}` | {row['question']} | "
        f"dup cites {row['baseline_duplicate_citation_count']} -> "
        f"{row['candidate_duplicate_citation_count']} "
        f"({row['duplicate_citation_delta']:+d})"
    )


def _render_cap_hit_row(row):
    return (
        f"- `{row['section']}` | {row['question']} | "
        f"cap hit {row['baseline_cap_hit']} -> {row['candidate_cap_hit']} "
        f"({row['baseline_finish_reason']} -> {row['candidate_finish_reason']})"
    )
