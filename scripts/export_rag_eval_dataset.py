#!/usr/bin/env python3
"""Export Senku bench/diagnostics artifacts as RAG eval JSONL."""

from __future__ import annotations

import argparse
import json
import sys
from collections import defaultdict
from pathlib import Path
from typing import Any, Mapping, Sequence


REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

GUIDE_JOINER = "|"
UNKNOWN_VALUES = {"", "unknown", "none", "null"}


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--bench-json",
        action="append",
        type=Path,
        default=[],
        help="Bench JSON artifact to export. Repeatable.",
    )
    parser.add_argument(
        "--diagnostics-json",
        action="append",
        type=Path,
        default=[],
        help=(
            "Analyzer diagnostics.json path, or a directory containing diagnostics.json. "
            "Repeatable."
        ),
    )
    parser.add_argument(
        "--otel-trace-jsonl",
        action="append",
        type=Path,
        default=[],
        help=(
            "Optional RAG trace JSONL to summarize into metadata. Repeatable. "
            "Accepts rag_trace.iter_otel_spans output shape or analyzer-compatible "
            "raw trace records."
        ),
    )
    parser.add_argument(
        "--output-jsonl",
        type=Path,
        required=True,
        help="Destination JSONL path.",
    )
    return parser.parse_args(argv)


def load_json(path: Path) -> Any:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def resolve_diagnostics_path(path: Path) -> Path:
    if path.is_dir():
        path = path / "diagnostics.json"
    if not path.is_file():
        raise FileNotFoundError(f"diagnostics.json not found: {path}")
    return path


def split_guide_ids(value: Any) -> list[str]:
    if value is None:
        return []
    if isinstance(value, str):
        parts = []
        for chunk in value.replace(",", GUIDE_JOINER).split(GUIDE_JOINER):
            text = chunk.strip()
            if text and text.lower() not in UNKNOWN_VALUES:
                parts.append(text)
        return list(dict.fromkeys(parts))
    if isinstance(value, Mapping):
        ids: list[str] = []
        for item in value.values():
            ids.extend(split_guide_ids(item))
        return list(dict.fromkeys(ids))
    if isinstance(value, Sequence) and not isinstance(value, (str, bytes, bytearray)):
        ids = []
        for item in value:
            ids.extend(split_guide_ids(item))
        return list(dict.fromkeys(ids))
    text = str(value).strip()
    return [text] if text and text.lower() not in UNKNOWN_VALUES else []


def normalize_key(artifact_name: Any, prompt_index: Any) -> tuple[str, str] | None:
    artifact = str(artifact_name or "").strip()
    index = str(prompt_index or "").strip()
    if not artifact or not index:
        return None
    return artifact, index


def row_identity_from_bench(path: Path, result: Mapping[str, Any]) -> tuple[str, str] | None:
    return normalize_key(path.name, result.get("index"))


def row_identity_from_diagnostics(row: Mapping[str, Any]) -> tuple[str, str] | None:
    return normalize_key(row.get("artifact_name"), row.get("prompt_index"))


def explicit_ground_truth(result: Mapping[str, Any], diagnostics: Mapping[str, Any]) -> str:
    metadata = result.get("prompt_metadata") if isinstance(result.get("prompt_metadata"), Mapping) else {}
    for container in (result, metadata, diagnostics):
        for key in (
            "ground_truth",
            "reference_answer",
            "expected_answer",
            "expected_response",
            "ideal_answer",
        ):
            value = container.get(key) if isinstance(container, Mapping) else None
            if value not in (None, ""):
                return value if isinstance(value, str) else json.dumps(value, sort_keys=True)

    expected_ids = split_guide_ids(
        diagnostics.get("expected_guide_ids")
        or metadata.get("expected_guide_ids")
        or metadata.get("expected_guides")
    )
    expected_family = str(
        diagnostics.get("expected_guide_family")
        or metadata.get("expected_guide_family")
        or metadata.get("expected_family")
        or ""
    ).strip()
    if expected_ids and expected_family.lower() not in UNKNOWN_VALUES:
        return f"expected guides: {GUIDE_JOINER.join(expected_ids)}; family: {expected_family}"
    if expected_ids:
        return f"expected guides: {GUIDE_JOINER.join(expected_ids)}"
    if expected_family and expected_family.lower() not in UNKNOWN_VALUES:
        return f"expected family: {expected_family}"
    return ""


def contexts_from_result(result: Mapping[str, Any], diagnostics: Mapping[str, Any]) -> list[str]:
    retrieval_metadata = result.get("retrieval_metadata")
    if not isinstance(retrieval_metadata, Mapping):
        retrieval_metadata = {}

    contexts: list[str] = []
    for candidate in retrieval_metadata.get("source_candidates") or []:
        if not isinstance(candidate, Mapping):
            continue
        guide_id = str(candidate.get("guide_id") or "").strip()
        title = str(candidate.get("title") or "").strip()
        section = str(candidate.get("section") or "").strip()
        category = str(candidate.get("category") or "").strip()
        label_parts = [part for part in (guide_id, title) if part]
        label = " ".join(label_parts)
        if section:
            label = f"{label} -> {section}" if label else section
        if category:
            label = f"{label} ({category})" if label else category
        if label:
            contexts.append(label)

    if not contexts:
        for title in retrieval_metadata.get("primary_source_titles") or []:
            text = str(title).strip()
            if text:
                contexts.append(text)

    if not contexts:
        contexts.extend(
            split_guide_ids(
                diagnostics.get("top_retrieved_guide_ids")
                or retrieval_metadata.get("top_retrieved_guide_ids")
            )
        )

    return list(dict.fromkeys(contexts))


def guide_metadata(result: Mapping[str, Any], diagnostics: Mapping[str, Any]) -> dict[str, Any]:
    retrieval_metadata = result.get("retrieval_metadata")
    if not isinstance(retrieval_metadata, Mapping):
        retrieval_metadata = {}
    cited = split_guide_ids(
        diagnostics.get("cited_guide_ids") or result.get("cited_guide_ids")
    )
    retrieved = split_guide_ids(
        diagnostics.get("top_retrieved_guide_ids")
        or retrieval_metadata.get("top_retrieved_guide_ids")
    )
    expected = split_guide_ids(diagnostics.get("expected_guide_ids"))
    reviewed_card_guide_ids = split_guide_ids(
        diagnostics.get("reviewed_card_guide_ids")
        or result.get("reviewed_card_guide_ids")
    )
    return {
        "cited_guide_ids": cited,
        "top_retrieved_guide_ids": retrieved,
        "expected_guide_ids": expected,
        "reviewed_card_guide_ids": reviewed_card_guide_ids,
    }


def metadata_from_sources(
    *,
    bench_path: Path | None,
    result: Mapping[str, Any],
    diagnostics: Mapping[str, Any],
    trace_summary: Mapping[str, Any] | None,
) -> dict[str, Any]:
    retrieval_metadata = result.get("retrieval_metadata")
    if not isinstance(retrieval_metadata, Mapping):
        retrieval_metadata = {}
    metadata: dict[str, Any] = {
        "artifact_name": diagnostics.get("artifact_name")
        or (bench_path.name if bench_path else ""),
        "artifact_path": diagnostics.get("artifact_path")
        or (str(bench_path) if bench_path else ""),
        "prompt_index": diagnostics.get("prompt_index") or result.get("index") or "",
        "prompt_id": diagnostics.get("prompt_id") or result.get("prompt_id") or "",
        "section": diagnostics.get("section") or result.get("section") or "",
        "decision_path": diagnostics.get("decision_path") or result.get("decision_path") or "",
        "decision_detail": diagnostics.get("decision_detail")
        or result.get("decision_detail")
        or "",
        "source_mode": diagnostics.get("source_mode") or result.get("source_mode") or "",
        "suspected_failure_bucket": diagnostics.get("suspected_failure_bucket") or "",
        "short_reason": diagnostics.get("short_reason") or "",
        "answer_card_status": diagnostics.get("answer_card_status") or "",
        "app_gate_status": diagnostics.get("app_gate_status") or "",
        "app_acceptance_status": diagnostics.get("app_acceptance_status") or "",
        "app_acceptance_root_cause": diagnostics.get("app_acceptance_root_cause") or "",
        "safety_surface_status": diagnostics.get("safety_surface_status") or "",
        "ui_surface_bucket": diagnostics.get("ui_surface_bucket") or "",
        "evidence_owner_status": diagnostics.get("evidence_owner_status") or "",
        "claim_support_status": diagnostics.get("claim_support_status") or "",
        "generated": diagnostics.get("generated") or "",
        "answer_mode": diagnostics.get("answer_mode")
        or retrieval_metadata.get("answer_mode")
        or result.get("answer_mode")
        or "",
        "support_strength": diagnostics.get("support_strength")
        or retrieval_metadata.get("support_strength")
        or result.get("support_strength")
        or "",
        "safety_critical": diagnostics.get("safety_critical")
        or retrieval_metadata.get("safety_critical")
        or result.get("safety_critical")
        or "",
        "retrieval_metadata_summary": result.get("retrieval_metadata_summary") or "",
        **guide_metadata(result, diagnostics),
    }
    if trace_summary:
        metadata["trace"] = dict(trace_summary)
    return metadata


def eval_record(
    *,
    bench_path: Path | None,
    result: Mapping[str, Any] | None,
    diagnostics: Mapping[str, Any] | None,
    trace_summary: Mapping[str, Any] | None = None,
) -> dict[str, Any]:
    result = result or {}
    diagnostics = diagnostics or {}
    return {
        "question": diagnostics.get("prompt_text") or result.get("question") or "",
        "answer": result.get("response_text") or diagnostics.get("response_text") or "",
        "contexts": contexts_from_result(result, diagnostics),
        "ground_truth": explicit_ground_truth(result, diagnostics),
        "metadata": metadata_from_sources(
            bench_path=bench_path,
            result=result,
            diagnostics=diagnostics,
            trace_summary=trace_summary,
        ),
    }


def load_bench_records(paths: Sequence[Path]) -> dict[tuple[str, str], tuple[Path, dict[str, Any]]]:
    records: dict[tuple[str, str], tuple[Path, dict[str, Any]]] = {}
    for path in paths:
        payload = load_json(path)
        if not isinstance(payload, Mapping):
            raise ValueError(f"Bench payload must be a JSON object: {path}")
        for result in payload.get("results") or []:
            if not isinstance(result, Mapping):
                continue
            key = row_identity_from_bench(path, result)
            if key is not None:
                records[key] = (path, dict(result))
    return records


def load_diagnostics_records(paths: Sequence[Path]) -> dict[tuple[str, str], dict[str, Any]]:
    records: dict[tuple[str, str], dict[str, Any]] = {}
    for raw_path in paths:
        path = resolve_diagnostics_path(raw_path)
        payload = load_json(path)
        if not isinstance(payload, Mapping):
            raise ValueError(f"Diagnostics payload must be a JSON object: {path}")
        for row in payload.get("rows") or []:
            if not isinstance(row, Mapping):
                continue
            key = row_identity_from_diagnostics(row)
            if key is not None:
                records[key] = dict(row)
    return records


def trace_duration_ms(span: Mapping[str, Any]) -> float | None:
    value = span.get("duration_ms")
    if value is not None:
        try:
            return round(float(value), 3)
        except (TypeError, ValueError):
            return None
    try:
        start = float(span.get("start_time_unix_nano"))
        end = float(span.get("end_time_unix_nano"))
    except (TypeError, ValueError):
        return None
    return round((end - start) / 1_000_000, 3)


def trace_status_code(span: Mapping[str, Any]) -> str:
    status = span.get("status")
    if isinstance(status, Mapping):
        return str(status.get("status_code") or status.get("code") or "UNSET")
    return str(status or "UNSET")


def trace_attributes(span: Mapping[str, Any]) -> Mapping[str, Any]:
    attrs = span.get("attributes")
    return attrs if isinstance(attrs, Mapping) else {}


def trace_key(span: Mapping[str, Any]) -> tuple[str, str] | None:
    attrs = trace_attributes(span)
    return normalize_key(
        span.get("artifact_name") or attrs.get("artifact_name"),
        span.get("prompt_index") or attrs.get("prompt_index"),
    )


def summarize_trace_spans(paths: Sequence[Path]) -> dict[tuple[str, str], dict[str, Any]]:
    try:
        from rag_trace import iter_otel_spans
    except ImportError:  # pragma: no cover - repo-local helper should exist.
        iter_otel_spans = None

    grouped: dict[tuple[str, str], list[dict[str, Any]]] = defaultdict(list)
    for path in paths:
        if iter_otel_spans is not None:
            spans = iter_otel_spans(path)
        else:
            spans = _iter_raw_trace_records(path)
        for span in spans:
            if not isinstance(span, Mapping):
                continue
            key = trace_key(span)
            if key is not None:
                grouped[key].append(dict(span))

    summaries: dict[tuple[str, str], dict[str, Any]] = {}
    for key, spans in grouped.items():
        duration_by_phase: dict[str, float] = defaultdict(float)
        error_phases = set()
        compact_spans = []
        for span in spans:
            attrs = trace_attributes(span)
            phase = str(
                attrs.get("phase")
                or span.get("phase")
                or str(span.get("name") or "").rsplit(".", 1)[-1]
            ).strip().lower()
            duration_ms = trace_duration_ms(span)
            if phase and duration_ms is not None:
                duration_by_phase[phase] += duration_ms
            status_code = trace_status_code(span)
            if status_code.lower() not in {"ok", "success", "unset"} and phase:
                error_phases.add(phase)
            context = span.get("context")
            trace_id = context.get("trace_id") if isinstance(context, Mapping) else span.get("trace_id")
            span_id = context.get("span_id") if isinstance(context, Mapping) else span.get("span_id")
            compact_spans.append(
                {
                    "name": span.get("name") or "",
                    "phase": phase,
                    "status_code": status_code,
                    "duration_ms": duration_ms,
                    "trace_id": trace_id or "",
                    "span_id": span_id or "",
                    "parent_id": span.get("parent_id") or span.get("parent_span_id") or None,
                }
            )
        summaries[key] = {
            "span_count": len(spans),
            "error_phases": sorted(error_phases),
            "duration_ms_by_phase": {
                phase: round(duration, 3)
                for phase, duration in sorted(duration_by_phase.items())
            },
            "spans": compact_spans,
        }
    return summaries


def _iter_raw_trace_records(path: Path):
    with path.open("r", encoding="utf-8") as handle:
        for line in handle:
            stripped = line.strip()
            if stripped:
                yield json.loads(stripped)


def build_records(
    *,
    bench_paths: Sequence[Path],
    diagnostics_paths: Sequence[Path],
    trace_paths: Sequence[Path] = (),
) -> list[dict[str, Any]]:
    bench_records = load_bench_records(bench_paths)
    diagnostics_records = load_diagnostics_records(diagnostics_paths)
    trace_records = summarize_trace_spans(trace_paths) if trace_paths else {}
    keys = sorted(set(bench_records) | set(diagnostics_records))
    return [
        eval_record(
            bench_path=bench_records.get(key, (None, {}))[0],
            result=bench_records.get(key, (None, {}))[1],
            diagnostics=diagnostics_records.get(key, {}),
            trace_summary=trace_records.get(key),
        )
        for key in keys
    ]


def write_jsonl(records: Sequence[Mapping[str, Any]], path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        for record in records:
            handle.write(json.dumps(record, ensure_ascii=False, sort_keys=True) + "\n")


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    if not args.bench_json and not args.diagnostics_json:
        raise SystemExit("At least one --bench-json or --diagnostics-json is required.")
    records = build_records(
        bench_paths=args.bench_json,
        diagnostics_paths=args.diagnostics_json,
        trace_paths=args.otel_trace_jsonl,
    )
    write_jsonl(records, args.output_jsonl)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
