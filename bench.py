#!/usr/bin/env python3
"""Batch test runner for Senku. Runs prompts from a file and produces a markdown report.

Usage:
    python3 bench.py                                        # local only
    python3 bench.py --urls http://192.168.1.50:1234/v1     # desktop only
    python3 bench.py --urls http://localhost:1234/v1,http://192.168.1.50:1234/v1  # both
"""

import argparse
import csv
import json
import math
import os
import queue
import re
import subprocess
import sys
import threading
import time
from datetime import datetime
from collections import Counter

import chromadb
import requests

import config
import query_prompt_runtime as _prompt_runtime
from lmstudio_utils import classify_lm_request_error, normalize_lm_studio_url
from query import (
    _confidence_label,
    _build_uncertain_fit_body,
    _fix_mojibake,
    _is_emergency_mental_health_query,
    _relevance_tag,
    _should_abstain,
    build_special_case_response,
    build_abstain_response,
    build_confidence_system_instruction,
    build_prompt,
    classify_special_case,
    normalize_response_text,
    post_json_with_retry,
    retrieve_results,
    _card_backed_runtime_answer_plan,
)
from query_completion_hardening import (
    _has_substantive_response as _hardening_has_substantive_response,
    _is_obviously_incomplete_crisis_response,
    _is_obviously_incomplete_safety_response as _hardening_is_obviously_incomplete_safety_response,
    _is_valid_crisis_retry_response as _hardening_is_valid_crisis_retry_response,
    _trim_incomplete_final_safety_line as _hardening_trim_incomplete_final_safety_line,
)
from token_estimation import estimate_tokens

DEFAULT_PROMPTS = os.path.join(os.path.dirname(__file__), "test_prompts.txt")
EMPTY_COMPLETION_RETRIES = 2
CRISIS_RETRY_MAX_COMPLETION_TOKENS = 256


class PreparedPromptGenerationError(RuntimeError):
    """Carry partial generation metadata for bench retry and reporting paths."""

    def __init__(self, message, *, meta=None, cause_info=None):
        super().__init__(message)
        self.meta = meta or {}
        self.cause_info = cause_info or {}


def get_collection_count_with_timeout(db_path, collection_name, timeout=10):
    """Return collection count, or None if Chroma hangs on count()."""
    probe = (
        "import chromadb, sys\n"
        "client = chromadb.PersistentClient(path=sys.argv[1])\n"
        "collection = client.get_collection(sys.argv[2])\n"
        "print(collection.count())\n"
    )
    try:
        result = subprocess.run(
            [sys.executable, "-c", probe, db_path, collection_name],
            capture_output=True,
            text=True,
            timeout=timeout,
            check=False,
        )
    except (OSError, subprocess.SubprocessError):
        return None

    if result.returncode != 0:
        return None

    output = result.stdout.strip()
    return int(output) if output.isdigit() else None


def load_prompts(prompt_path):
    """Load prompts from txt/csv/jsonl and preserve structured metadata."""
    source_format = os.path.splitext(prompt_path)[1].lower().lstrip(".") or "txt"
    if source_format == "csv":
        with open(prompt_path, "r", encoding="utf-8", newline="") as f:
            rows = list(csv.DictReader(f))
        return _load_structured_prompt_entries(rows, prompt_path), "csv"
    if source_format == "jsonl":
        rows = []
        with open(prompt_path, "r", encoding="utf-8") as f:
            for line_number, raw_line in enumerate(f, start=1):
                line = raw_line.strip()
                if not line:
                    continue
                try:
                    rows.append(json.loads(line))
                except json.JSONDecodeError as exc:
                    raise ValueError(
                        f"Invalid JSONL record at {prompt_path}:{line_number}: {exc}"
                    ) from exc
        return _load_structured_prompt_entries(rows, prompt_path), "jsonl"
    return _load_text_prompt_entries(prompt_path), "txt"


def _clean_prompt_field(value):
    """Normalize optional prompt metadata into a compact serializable form."""
    if value is None:
        return None
    if isinstance(value, str):
        value = value.strip()
        return value or None
    return value


def _build_prompt_entry(question, section, *, prompt_id=None, metadata=None):
    """Return a normalized prompt entry used throughout the bench harness."""
    metadata = {
        str(key): _clean_prompt_field(value)
        for key, value in (metadata or {}).items()
        if _clean_prompt_field(value) is not None
    }
    return {
        "question": str(question).strip(),
        "section": str(section or "Core Regression").strip(),
        "prompt_id": _clean_prompt_field(prompt_id),
        "lane": _clean_prompt_field(metadata.get("lane")),
        "style": _clean_prompt_field(metadata.get("style")),
        "target_behavior": _clean_prompt_field(metadata.get("target_behavior")),
        "what_it_tests": _clean_prompt_field(metadata.get("what_it_tests")),
        "metadata": metadata,
    }


def _load_text_prompt_entries(prompt_path):
    """Load plain-text prompts and preserve section headers from comment lines."""
    prompts = []
    current_section = "Core Regression"

    with open(prompt_path, "r", encoding="utf-8") as f:
        for raw_line in f:
            line = raw_line.strip()
            if not line:
                continue
            if line.startswith("#"):
                current_section = line.lstrip("#").strip() or current_section
                continue
            prompts.append(_build_prompt_entry(line, current_section))

    return prompts


def _first_present(mapping, keys):
    """Return the first non-empty mapping value for the provided keys."""
    for key in keys:
        if key not in mapping:
            continue
        value = _clean_prompt_field(mapping.get(key))
        if value is not None:
            return key, value
    return None, None


def _load_structured_prompt_entries(rows, prompt_path):
    """Load CSV/JSONL prompt rows while keeping metadata available for reports."""
    prompts = []
    for index, row in enumerate(rows, start=1):
        if not isinstance(row, dict):
            raise ValueError(
                f"Structured prompt row {index} in {prompt_path} is not an object"
            )

        prompt_key, question = _first_present(row, ("prompt", "question"))
        if question is None:
            raise ValueError(
                f"Structured prompt row {index} in {prompt_path} is missing `prompt`/`question`"
            )

        section_key, section = _first_present(row, ("section",))
        section = section or "Core Regression"
        prompt_id_key, prompt_id = _first_present(row, ("id", "prompt_id"))

        excluded_keys = {key for key in (prompt_key, section_key, prompt_id_key) if key}
        metadata = {
            str(key): _clean_prompt_field(value)
            for key, value in row.items()
            if key not in excluded_keys and _clean_prompt_field(value) is not None
        }
        prompts.append(
            _build_prompt_entry(
                question,
                section,
                prompt_id=prompt_id,
                metadata=metadata,
            )
        )

    return prompts


def _parse_filter_values(value):
    """Parse comma-separated filters into a normalized lower-case set."""
    if not value:
        return None
    values = {item.strip().lower() for item in value.split(",") if item.strip()}
    return values or None


def _matches_optional_filter(value, wanted_values):
    """Return True when a value matches the configured filter set."""
    if wanted_values is None:
        return True
    cleaned = _clean_prompt_field(value)
    if cleaned is None:
        return False
    return str(cleaned).strip().lower() in wanted_values


def _apply_prompt_filters(
    prompt_entries,
    *,
    sections=None,
    lanes=None,
    styles=None,
    target_behaviors=None,
    prompt_ids=None,
):
    """Filter prompt entries by section or structured metadata."""
    filtered_entries = []
    for entry in prompt_entries:
        if not _matches_optional_filter(entry.get("section"), sections):
            continue
        if not _matches_optional_filter(entry.get("lane"), lanes):
            continue
        if not _matches_optional_filter(entry.get("style"), styles):
            continue
        if not _matches_optional_filter(entry.get("target_behavior"), target_behaviors):
            continue
        if not _matches_optional_filter(entry.get("prompt_id"), prompt_ids):
            continue
        filtered_entries.append(entry)
    return filtered_entries


def _count_prompt_entry_metadata(prompt_entries, key):
    """Count prompt-entry metadata values for artifact summaries."""
    counts = Counter()
    for entry in prompt_entries:
        value = _clean_prompt_field(entry.get(key))
        if value is not None:
            counts[str(value)] += 1
    return dict(counts)


def _render_prompt_entry_metadata(entry):
    """Build compact prompt metadata lines for markdown reports."""
    lines = []
    if entry.get("prompt_id"):
        lines.append(f"*Prompt ID: {entry['prompt_id']}*")

    details = []
    if entry.get("lane"):
        details.append(f"lane={entry['lane']}")
    if entry.get("style"):
        details.append(f"style={entry['style']}")
    if entry.get("target_behavior"):
        details.append(f"target={entry['target_behavior']}")
    if details:
        lines.append(f"*Prompt metadata: {' | '.join(details)}*")
    if entry.get("what_it_tests"):
        lines.append(f"*What it tests: {entry['what_it_tests']}*")
    return lines


def build_worker_targets(gen_urls, gen_models=None):
    """Assign a stable worker label and model to each generation slot."""
    if gen_models is None:
        gen_models = [config.GEN_MODEL] * len(gen_urls)
    if len(gen_urls) != len(gen_models):
        raise ValueError("Generation URL/model counts must match")

    totals = Counter(gen_urls)
    seen = Counter()
    workers = []
    for url, model in zip(gen_urls, gen_models):
        seen[url] += 1
        base_label = url.split("//")[-1]
        label = f"{base_label}#{seen[url]}" if totals[url] > 1 else base_label
        workers.append({
            "url": url,
            "label": label,
            "model": model,
        })
    return workers


def _parse_url_list(value, default_url=None, *, dedupe=False):
    """Parse a comma-separated URL string or iterable into a clean URL list."""
    if isinstance(value, str):
        urls = [item.strip() for item in value.split(",") if item.strip()]
    elif value:
        urls = [str(item).strip() for item in value if str(item).strip()]
    else:
        urls = [default_url] if default_url else []

    if not dedupe:
        return urls

    seen = set()
    unique_urls = []
    for url in urls:
        normalized = normalize_lm_studio_url(url) or url
        if normalized in seen:
            continue
        seen.add(normalized)
        unique_urls.append(url)
    return unique_urls


def _default_generation_url():
    return getattr(config, "GEN_URL", config.LM_STUDIO_URL)


def _default_embedding_url():
    return getattr(config, "EMBED_URL", config.LM_STUDIO_URL)


def _parse_worker_models(value, worker_count, default_model):
    """Parse per-worker generation models, broadcasting when one model is supplied."""
    if worker_count <= 0:
        return []

    if isinstance(value, str):
        models = [item.strip() for item in value.split(",") if item.strip()]
    elif value:
        models = [str(item).strip() for item in value if str(item).strip()]
    else:
        models = []

    if not models:
        return [default_model] * worker_count
    if len(models) == 1:
        return models * worker_count
    if len(models) != worker_count:
        raise ValueError(
            f"Expected 1 or {worker_count} worker models, got {len(models)}"
        )
    return models


def _coerce_mapping(value):
    """Return a mapping when possible, else an empty dict."""
    return value if isinstance(value, dict) else {}


def _json_safe(value):
    """Convert nested metadata into a JSON-serializable shape."""
    if isinstance(value, dict):
        return {str(key): _json_safe(val) for key, val in value.items()}
    if isinstance(value, list):
        return [_json_safe(item) for item in value]
    if isinstance(value, tuple):
        return [_json_safe(item) for item in value]
    if isinstance(value, set):
        return [_json_safe(item) for item in sorted(value, key=str)]
    if isinstance(value, Counter):
        return dict(value)
    if isinstance(value, (str, int, float, bool)) or value is None:
        return value
    return str(value)


def _count_items(value):
    """Return a lightweight item count for lists, mappings, or scalars."""
    if value is None:
        return 0
    if isinstance(value, dict):
        if "items" in value and isinstance(value["items"], (list, tuple, set)):
            return len(value["items"])
        return len(value)
    if isinstance(value, (list, tuple, set)):
        return len(value)
    if isinstance(value, str):
        return 1 if value.strip() else 0
    return 1


def _extract_optional_meta_value(retrieval_meta, results, key):
    """Pull optional retrieval metadata from several likely shapes."""
    for container in (
        _coerce_mapping(retrieval_meta),
        _coerce_mapping(results),
    ):
        if key in container:
            return container[key]
        for nested_key in ("retrieval_metadata", "retrieval_meta", "meta"):
            nested = container.get(nested_key)
            if isinstance(nested, dict) and key in nested:
                return nested[key]
    return None


def _split_retrieval_payload(payload):
    """Normalize retrieval payloads from current and future query helpers."""
    excluded_keys = {
        "results",
        "retrieval_results",
        "sub_queries",
        "decomposed_queries",
        "queries",
        "ids",
        "documents",
        "metadatas",
        "distances",
    }
    if isinstance(payload, tuple):
        if len(payload) == 2:
            results, sub_queries = payload
            retrieval_meta = {}
        elif len(payload) >= 3:
            results, sub_queries = payload[:2]
            retrieval_meta = payload[2]
        else:
            raise ValueError("Unexpected retrieval payload shape")
    elif isinstance(payload, dict):
        results = payload.get("results")
        if results is None:
            results = payload.get("retrieval_results", payload)
        sub_queries = (
            payload.get("sub_queries")
            or payload.get("decomposed_queries")
            or payload.get("queries")
            or []
        )
        retrieval_meta = {
            key: value
            for key, value in payload.items()
            if key not in excluded_keys
        }
        nested_meta = payload.get("retrieval_metadata")
        if isinstance(nested_meta, dict):
            retrieval_meta = {**nested_meta, **retrieval_meta}
    else:
        raise TypeError(f"Unsupported retrieval payload type: {type(payload)!r}")

    if not isinstance(retrieval_meta, dict):
        retrieval_meta = {"raw": retrieval_meta}

    if isinstance(sub_queries, tuple):
        sub_queries = list(sub_queries)
    elif not isinstance(sub_queries, list):
        sub_queries = [sub_queries] if sub_queries else []

    return results, sub_queries, retrieval_meta


def _summarize_scenario_frame(value):
    """Render a compact scenario-frame summary for reports."""
    if not value:
        return None
    if isinstance(value, dict):
        parts = []
        for key in ("objectives", "assets", "constraints", "hazards", "people"):
            if value.get(key):
                parts.append(f"{key}={_count_items(value[key])}")
        if value.get("deadline"):
            parts.append(f"deadline={value['deadline']}")
        if parts:
            return ", ".join(parts)
        return "present"
    return str(value)[:120]


def _summarize_objective_coverage(value):
    """Render a compact objective coverage summary for reports."""
    if not value:
        return None
    if isinstance(value, dict):
        counts = []
        for key in ("covered", "weak", "missing", "unknown"):
            if key in value:
                counts.append(f"{key}={_count_items(value[key])}")
        if counts:
            return ", ".join(counts)
        if value:
            return "present"
    if isinstance(value, list):
        status_counts = Counter()
        for item in value:
            if isinstance(item, dict):
                status = item.get("status") or item.get("coverage") or item.get("state")
                if status:
                    status_counts[str(status)] += 1
        if status_counts:
            return ", ".join(f"{key}={count}" for key, count in status_counts.items())
        if value:
            return f"{len(value)} items"
    return str(value)[:120]


def _summarize_category_distribution(value):
    """Render a compact category distribution summary for reports."""
    if not value:
        return None
    if isinstance(value, dict):
        items = [
            (str(key), count)
            for key, count in value.items()
            if count not in (None, 0, "")
        ]
        if items:
            items.sort(key=lambda item: (-item[1], item[0]))
            return ", ".join(f"{key}={count}" for key, count in items[:5])
        return "present"
    if isinstance(value, list):
        counts = Counter()
        for item in value:
            if isinstance(item, dict):
                category = item.get("category") or item.get("name") or item.get("label")
                if category:
                    counts[str(category)] += 1
            elif item is not None:
                counts[str(item)] += 1
        if counts:
            return ", ".join(f"{key}={count}" for key, count in counts.most_common(5))
        return f"{len(value)} items"
    return str(value)[:120]


def _summarize_support_signals(value):
    """Render a compact support-signal summary for reports."""
    if not value:
        return None
    if isinstance(value, dict):
        preferred = []
        for key in (
            "source_mode",
            "support_level",
            "direct_support",
            "inferred_support",
            "weak_support",
            "coverage",
        ):
            if key in value and value[key] not in (None, "", [], {}, ()):
                preferred.append(f"{key}={value[key]}")
        if preferred:
            return ", ".join(preferred[:4])
        return "present"
    if isinstance(value, list):
        return f"{len(value)} items"
    return str(value)[:120]


def _frame_is_safety_critical(frame):
    if not isinstance(frame, dict):
        return False
    raw_value = frame.get("safety_critical")
    if isinstance(raw_value, bool):
        return raw_value
    if isinstance(raw_value, (list, tuple, set)):
        return bool(raw_value)
    if isinstance(raw_value, str):
        return raw_value.strip().lower() in {"1", "true", "yes", "critical"}
    return bool(raw_value)


def _profile_is_safety_critical(profile):
    return (profile or "").strip().lower() in {"safety_triage", "normal_vs_urgent"}


def _support_strength(confidence_label, answer_mode, support_signals):
    normalized_confidence = (confidence_label or "").strip().lower()
    normalized_mode = (answer_mode or "").strip().lower()
    if normalized_mode in {"abstain", "uncertain_fit"} or normalized_confidence == "low":
        return "limited"
    if normalized_confidence == "medium":
        return "moderate"
    if normalized_confidence == "high":
        if isinstance(support_signals, dict):
            direct_count = int(support_signals.get("direct", 0) or 0)
            covered_count = int(support_signals.get("covered", 0) or 0)
            if direct_count or covered_count:
                return "strong"
        return "moderate"
    return ""


def _answer_provenance_for_decision(decision_path):
    normalized = (decision_path or "").strip().lower()
    if normalized == "card_backed_runtime":
        return "reviewed_card_runtime"
    if normalized == "rag":
        return "generated_model"
    if normalized == "rag-empty":
        return "no_answer"
    if normalized == "uncertain_fit":
        return "uncertain_fit_card"
    if normalized == "abstain":
        return "abstain_card"
    if normalized == "no-rag":
        return "no_rag"
    if normalized:
        return "deterministic_rule"
    return "unknown"


def _primary_source_titles(source_candidates, limit=3):
    titles = []
    seen = set()
    for candidate in source_candidates or []:
        if not isinstance(candidate, dict):
            continue
        title = (candidate.get("title") or "").strip()
        section = (candidate.get("section") or "").strip()
        if not title:
            continue
        label = f"{title} -> {section}" if section else title
        if label in seen:
            continue
        seen.add(label)
        titles.append(label)
        if len(titles) >= limit:
            break
    return titles


def _runtime_profile(model_name=None, gen_url=None):
    """Return the configured runtime profile for a generation model."""
    if hasattr(config, "get_runtime_profile"):
        return config.get_runtime_profile(model_name or config.GEN_MODEL, gen_url)
    return {
        "name": "broad-quality",
        "top_k": getattr(config, "TOP_K", 24),
        "prompt_token_limit": 8192,
        "empty_completion_retries": EMPTY_COMPLETION_RETRIES,
        "adaptive_completion_retries": 1,
        "completion_retry_multiplier": 1.33,
        "completion_retry_min_step": 256,
        "completion_retry_max_tokens": getattr(config, "MAX_COMPLETION_TOKENS", 2048),
        "cap_hit_token_ratio": 0.92,
        "retry_on_cap_hit": True,
    }


def _next_completion_budget(current_tokens, runtime_profile):
    """Return a larger completion budget, or None when no escalation remains."""
    if not current_tokens or current_tokens <= 0:
        return None

    max_tokens = runtime_profile.get("completion_retry_max_tokens")
    multiplier = float(runtime_profile.get("completion_retry_multiplier", 1.33))
    min_step = int(runtime_profile.get("completion_retry_min_step", 256))
    rounding = int(runtime_profile.get("completion_retry_rounding", 64))
    proposed = max(int(math.ceil(current_tokens * multiplier)), current_tokens + min_step)
    if rounding > 1:
        proposed = int(round(proposed / rounding) * rounding)
        if proposed <= current_tokens:
            proposed = current_tokens + rounding
    if max_tokens:
        proposed = min(proposed, int(max_tokens))
    return proposed if proposed > current_tokens else None


def _build_generation_time_summary(results_map, total_time, prompt_count):
    """Return bench timing metrics without mixing failed generations into success-only averages."""
    success_count = 0
    successful_generation_time = 0.0
    for _, _, _, meta in results_map.values():
        if meta.get("error"):
            continue
        success_count += 1
        successful_generation_time += meta.get("generation_time", 0) or 0

    return {
        "success_count": success_count,
        "error_count": max(prompt_count - success_count, 0),
        "successful_generation_time": round(successful_generation_time, 1),
        "avg_generation_time": round(total_time / max(prompt_count, 1), 1),
        "avg_generation_time_success_only": round(
            successful_generation_time / max(success_count, 1),
            1,
        ),
    }


def _is_completion_cap_hit(finish_reason, usage, requested_tokens, runtime_profile):
    """Return True when the generation likely stopped because of the completion cap."""
    if finish_reason == "length":
        return True
    completion_tokens = (usage or {}).get("completion_tokens") or 0
    ratio = float(runtime_profile.get("cap_hit_token_ratio", 0.92))
    if not requested_tokens or requested_tokens <= 0 or completion_tokens <= 0:
        return False
    return completion_tokens >= max(int(requested_tokens * ratio), requested_tokens - 16)


def _is_prepared_prompt_safety_critical(meta, results=None):
    """Return True when prompt/runtime metadata marks a generated answer safety-critical."""
    retrieval_meta = _coerce_mapping(meta).get("retrieval_metadata")
    if not isinstance(retrieval_meta, dict):
        retrieval_meta = {}
    raw_safety_critical = _extract_optional_meta_value(
        retrieval_meta or meta,
        results or {},
        "safety_critical",
    )
    scenario_frame = _extract_optional_meta_value(
        retrieval_meta or meta,
        results or {},
        "scenario_frame",
    )
    retrieval_profile = _extract_optional_meta_value(
        retrieval_meta or meta,
        results or {},
        "retrieval_profile",
    )
    return (
        _frame_is_safety_critical({"safety_critical": raw_safety_critical})
        or _frame_is_safety_critical(scenario_frame)
        or _profile_is_safety_critical(retrieval_profile)
    )


def _is_obviously_incomplete_safety_response(text):
    return _hardening_is_obviously_incomplete_safety_response(
        text,
        normalize_response_text_fn=normalize_response_text,
    )


def _trim_incomplete_final_safety_line(text):
    return _hardening_trim_incomplete_final_safety_line(
        text,
        normalize_response_text_fn=normalize_response_text,
    )


def _retry_cause_counts(retry_events):
    """Return a compact retry-cause counter for artifact summaries."""
    return dict(
        Counter(event.get("category", "unknown") for event in retry_events or [])
    )


def _classify_generation_exception(exc):
    """Normalize generation exceptions for retry policy and bench artifacts."""
    if isinstance(exc, PreparedPromptGenerationError):
        cause_info = dict(exc.cause_info)
        cause_info.setdefault("message", str(exc))
        cause_info.setdefault("retryable", False)
        return cause_info

    if isinstance(exc, requests.RequestException):
        return classify_lm_request_error(exc)

    if isinstance(exc, RuntimeError) and "Empty completion" in str(exc):
        return {
            "category": "empty_completion",
            "status_code": None,
            "retryable": False,
            "message": str(exc),
        }

    return {
        "category": "generation_error",
        "status_code": None,
        "retryable": False,
        "message": str(exc),
    }


def _system_prompt_text(mode):
    """Return the system prompt text used for a given generation mode."""
    return _prompt_runtime.system_prompt_text(config, mode)


def _prompt_token_limit(gen_model=None, gen_url=None, runtime_profile=None):
    """Return the configured prompt-window limit for the active runtime profile."""
    return _prompt_runtime.prompt_token_limit_from_runtime_profile(
        gen_model,
        gen_url,
        runtime_profile=runtime_profile,
        runtime_profile_getter=_runtime_profile,
    )


def _estimate_chat_prompt_tokens(
    prompt_text, *, use_system_prompt=True, mode="default", system_prompt_text=None
):
    """Estimate tokens for the chat request payload before generation."""
    return _prompt_runtime.estimate_chat_prompt_tokens(
        prompt_text,
        estimate_tokens_fn=estimate_tokens,
        system_prompt_resolver=_system_prompt_text,
        use_system_prompt=use_system_prompt,
        mode=mode,
        system_prompt_text=system_prompt_text,
    )


def _prep_worker_plan(embed_urls, gen_urls, *, use_rag):
    """Return the guarded prep worker count and a short reason string."""
    if not use_rag:
        return 1, "no-rag"

    normalized_embeds = [
        normalize_lm_studio_url(url)
        for url in _parse_url_list(embed_urls, dedupe=True)
        if normalize_lm_studio_url(url)
    ]
    normalized_gens = {normalize_lm_studio_url(url) for url in gen_urls}
    if len(normalized_embeds) > 1:
        return min(2, len(normalized_embeds)), "multi-embed-host"

    normalized_embed = normalized_embeds[0] if normalized_embeds else None
    if normalized_embed and normalized_embed not in normalized_gens:
        return 2, "split-host"
    return 1, "same-host"


def _prep_embed_assignments(embed_urls, prep_worker_count):
    """Pin each prep worker to a stable embedding host."""
    cleaned_urls = _parse_url_list(embed_urls, dedupe=True)
    if prep_worker_count <= 0 or not cleaned_urls:
        return []
    return [
        cleaned_urls[index % len(cleaned_urls)]
        for index in range(prep_worker_count)
    ]


def retrieve_chunks(question, collection, top_k, category=None, embed_url=None):
    """Run decomposition, embedding, and retrieval. Returns results and metadata."""
    payload = retrieve_results(question, collection, top_k, category, lm_studio_url=embed_url)
    return _split_retrieval_payload(payload)


def empty_results():
    """Return an empty retrieval payload for baseline/no-RAG runs."""
    return {
        "ids": [[]],
        "documents": [[]],
        "metadatas": [[]],
        "distances": [[]],
    }


def generate_response(
    prompt_text,
    gen_url,
    temperature,
    gen_model=None,
    use_system_prompt=True,
    mode="default",
    max_completion_tokens=None,
    retry_log=None,
    messages=None,
    system_prompt_text=None,
):
    """Send a built prompt to a specific LM Studio URL for generation."""
    url = f"{gen_url}/chat/completions"
    if messages is None:
        messages = []
        if use_system_prompt:
            system_prompt = (
                _system_prompt_text(mode)
                if system_prompt_text is None
                else system_prompt_text
            )
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt_text})
    payload = {
        "model": gen_model or config.GEN_MODEL,
        "messages": messages,
        "stream": False,
        "temperature": temperature,
    }
    if max_completion_tokens and max_completion_tokens > 0:
        payload["max_tokens"] = max_completion_tokens

    start = time.time()
    resp = post_json_with_retry(
        url,
        payload,
        timeout=300,
        context=f"Bench chat completion ({gen_url})",
        retry_log=retry_log,
    )
    elapsed = time.time() - start

    try:
        data = resp.json()
    finally:
        resp.close()

    choice = data["choices"][0]
    response_text = choice["message"]["content"]
    response_text = normalize_response_text(response_text)
    usage = data.get("usage", {})
    finish_reason = choice.get("finish_reason")

    return response_text, elapsed, usage, finish_reason


def _has_substantive_response(text):
    return _hardening_has_substantive_response(text)


def _build_crisis_continuation_messages(question, partial_response_text):
    """Return a strict full-rewrite payload for malformed crisis retries."""
    return [
        {
            "role": "system",
            "content": (
                "The prior crisis-support reply was malformed or incomplete. Rewrite the "
                "entire answer from scratch as a clean final response. Use exactly 4 "
                "numbered steps. Start immediately with step 1 in imperative crisis "
                "action language; do not open with a question. Keep any citations "
                "complete and closed. Do not append to the "
                "draft. Do not leave dangling bullets, headings, or partial citations. "
                "Do not drift into soft self-help, grounding, or wellness tips before "
                "crisis actions. Preserve the crisis behavior mode implied by the "
                "question and draft; for mania-, psychosis-, or dangerous-activation-"
                "like scenarios, keep steps 2-4 on close supervision, means "
                "restriction, and urgent emergency evaluation rather than switching to "
                "a generic hotline or country-check script."
            ),
        },
        {
            "role": "assistant",
            "content": partial_response_text or "",
        },
        {
            "role": "user",
            "content": (
                f"Original user question: {question}\n\n"
                "Return a full replacement reply only, not a continuation. For "
                "observer-language crisis prompts, address the responder directly with "
                "imperative guidance about the other person, not hotline-style "
                "questions to the person in crisis. Required "
                "structure:\n"
                "1. Tell them to stay with the person right now, not leave them "
                "alone, and call 911 or local emergency services now if there is "
                "immediate danger.\n"
                "2. Tell them to keep a trusted "
                "adult with them continuously right now.\n"
                "3. Tell them to reduce access to obvious means of harm right now, "
                "including weapons, medications, car keys, or other dangerous items if "
                "they can do so safely.\n"
                "4. Direct them to get urgent same-day emergency evaluation now "
                "(ER, 911/local emergency services, or local mobile crisis/urgent "
                "psychiatric assessment as appropriate). Do not make this step a "
                "hotline-only instruction, and do not ask for their country.\n\n"
                "Requirements: exactly four numbered steps, concise, crisis-first "
                "actions, no soft self-help drift, complete citations only. For "
                "mania-, psychosis-, or dangerous-activation-like prompts, keep the "
                "opening block on supervision, containment, means restriction, and emergency "
                "evaluation."
            ),
        },
    ]


def _is_valid_crisis_retry_response(text):
    return _hardening_is_valid_crisis_retry_response(
        text,
        normalize_response_text_fn=normalize_response_text,
    )


def prepare_prompt(
    index,
    question,
    collection,
    top_k,
    category,
    use_rag=True,
    mode="default",
    embed_url=None,
    prompt_token_limit=None,
):
    """Retrieve context and build the prompt before distributed generation."""
    reviewed_card_plan = {}
    special_case_path, special_case_detail = classify_special_case(question) if use_rag else (None, None)
    special_case_response = build_special_case_response(question) if use_rag else None
    confidence_label = None
    if special_case_response:
        results = empty_results()
        sub_queries = [question]
        retrieval_meta = {
            "special_case": special_case_detail or special_case_path or "deterministic",
            "decision_path": special_case_path or "deterministic",
            "decision_detail": special_case_detail,
        }
        prompt_text = ""
        decision_path = special_case_path or "deterministic"
        decision_detail = special_case_detail
        confidence_label = "high"
    elif use_rag:
        results, sub_queries, retrieval_meta = retrieve_chunks(
            question,
            collection,
            top_k,
            category,
            embed_url=embed_url,
        )
        should_abstain, match_labels = _should_abstain(results, question)
        confidence_label = retrieval_meta.get("confidence_label") or _confidence_label(
            results,
            retrieval_meta.get("scenario_frame", {"question": question}),
        )
        if should_abstain:
            prompt_text = ""
            special_case_response = build_abstain_response(
                question,
                results,
                match_labels,
                scenario_frame=retrieval_meta.get("scenario_frame", {"question": question}),
            )
            decision_path = "abstain"
            decision_detail = None
            confidence_label = "low"
        else:
            reviewed_card_plan = _card_backed_runtime_answer_plan(question, results) or {}
            card_backed_response = str(reviewed_card_plan.get("answer_text") or "").strip()
            if card_backed_response:
                prompt_text = ""
                special_case_response = card_backed_response
                decision_path = "card_backed_runtime"
            elif retrieval_meta.get("answer_mode") == "uncertain_fit":
                prompt_text = ""
                scenario_frame = retrieval_meta.get("scenario_frame", {"question": question})
                if retrieval_meta.get("safety_critical") and isinstance(scenario_frame, dict):
                    scenario_frame = {**scenario_frame, "safety_critical": True}
                special_case_response = _build_uncertain_fit_body(
                    question,
                    results,
                    confidence_label,
                    scenario_frame=scenario_frame,
                    match_labels=match_labels,
                )
                decision_path = "uncertain_fit"
            else:
                prompt_text = (
                    build_prompt(
                        question,
                        results,
                        mode=mode,
                        prompt_token_limit=prompt_token_limit,
                    )
                    if results["documents"][0]
                    else ""
                )
                decision_path = "rag" if prompt_text else "rag-empty"
            decision_detail = None
    else:
        results = empty_results()
        sub_queries = [question]
        retrieval_meta = {}
        prompt_text = question
        decision_path = "no-rag"
        decision_detail = None

    if isinstance(retrieval_meta, dict) and confidence_label:
        retrieval_meta["confidence_label"] = confidence_label
    meta = {
        "decomposed": use_rag and len(sub_queries) > 1,
        "decision_path": decision_path,
        "decision_detail": decision_detail,
        "answer_provenance": _answer_provenance_for_decision(decision_path),
        "reviewed_card_backed": decision_path == "card_backed_runtime",
        "reviewed_card_ids": reviewed_card_plan.get("card_ids") or [],
        "reviewed_card_review_status": reviewed_card_plan.get("review_status") or "",
        "reviewed_card_guide_ids": reviewed_card_plan.get("guide_ids") or [],
        "reviewed_card_cited_guide_ids": reviewed_card_plan.get("cited_guide_ids") or [],
        "confidence_label": confidence_label,
        "sub_queries": sub_queries,
        "retrieval_metadata": retrieval_meta,
        "special_case_response": normalize_response_text(special_case_response) if special_case_response else "",
        "generation_time": 0,
        "prompt_tokens": 0,
        "completion_tokens": 0,
        "total_chunks_retrieved": len(results["documents"][0]),
        "gen_url": "N/A",
        "gen_worker": "N/A",
        "finish_reason": None,
        "completion_cap_hit": False,
        "completion_attempts": [],
        "retry_events": [],
        "retry_cause_counts": {},
        "completion_retry_count": 0,
        "requested_max_completion_tokens": None,
        "final_max_completion_tokens": None,
        "runtime_profile": None,
    }

    return index, question, prompt_text, results, meta


def process_prepared_prompt(
    prepared_prompt,
    temperature,
    gen_url,
    gen_model=None,
    gen_worker=None,
    use_system_prompt=True,
    mode="default",
    max_completion_tokens=None,
    runtime_profile=None,
):
    """Run generation for a prompt whose retrieval context was already prepared."""
    index, question, prompt_text, results, meta = prepared_prompt

    if meta.get("special_case_response"):
        return index, question, meta["special_case_response"], results, meta

    if not prompt_text:
        return index, question, "", results, meta

    gen_model = gen_model or config.GEN_MODEL
    runtime_profile = runtime_profile or _runtime_profile(gen_model)
    total_elapsed = 0.0
    response_text = ""
    usage = {}
    finish_reason = None
    retry_events = []
    completion_attempts = []
    completion_cap_hit = False
    current_max_completion_tokens = max_completion_tokens
    crisis_hardening_active = _is_emergency_mental_health_query(question)
    crisis_retry_used = False
    crisis_retry_base_response_text = ""
    crisis_retry_base_usage = {}
    crisis_retry_base_finish_reason = None
    crisis_retry_base_cap_hit = False
    completion_safety_trimmed = False
    request_messages = None
    request_temperature = temperature
    empty_retry_budget = int(
        runtime_profile.get("empty_completion_retries", EMPTY_COMPLETION_RETRIES)
    )
    adaptive_retry_budget = int(runtime_profile.get("adaptive_completion_retries", 1))
    empty_retries_used = 0
    adaptive_retries_used = 0
    safety_critical_generation = _is_prepared_prompt_safety_critical(meta, results)
    system_prompt_text = None
    if use_system_prompt:
        system_prompt_text = _system_prompt_text(mode)
        confidence_instruction = build_confidence_system_instruction(
            meta.get("confidence_label")
        )
        if confidence_instruction:
            system_prompt_text = (
                f"{system_prompt_text}\n\n{confidence_instruction}"
            )
    prompt_budget_meta = _estimate_chat_prompt_tokens(
        prompt_text,
        use_system_prompt=use_system_prompt,
        mode=mode,
        system_prompt_text=system_prompt_text,
    )
    prompt_token_limit = _prompt_token_limit(
        gen_model=gen_model,
        gen_url=gen_url,
        runtime_profile=runtime_profile,
    )
    prompt_safety_margin = int(runtime_profile.get("prompt_token_safety_margin", 96))
    if current_max_completion_tokens is None:
        current_max_completion_tokens = runtime_profile.get(
            "completion_retry_max_tokens",
            getattr(config, "MAX_COMPLETION_TOKENS", None),
        )
    if prompt_token_limit:
        estimated_prompt_tokens = prompt_budget_meta["estimated_prompt_tokens"]
        available_completion_budget = max(
            prompt_token_limit - estimated_prompt_tokens - prompt_safety_margin,
            0,
        )
        if available_completion_budget <= 0:
            failure_meta = dict(meta)
            failure_meta.update({
                **prompt_budget_meta,
                "prompt_token_limit": prompt_token_limit,
                "prompt_token_safety_margin": prompt_safety_margin,
                "generation_time": round(total_elapsed, 1),
                "prompt_tokens": 0,
                "completion_tokens": 0,
                "gen_url": gen_url,
                "gen_model": gen_model,
                "gen_worker": gen_worker or gen_url,
                "finish_reason": None,
                "completion_cap_hit": False,
                "completion_attempts": [],
                "retry_events": [],
                "retry_cause_counts": {},
                "completion_retry_count": 0,
                "requested_max_completion_tokens": max_completion_tokens,
                "final_max_completion_tokens": 0,
                "runtime_profile": runtime_profile.get("name"),
            })
            raise PreparedPromptGenerationError(
                (
                    "Prepared prompt exceeds runtime prompt budget "
                    f"({estimated_prompt_tokens} est > {prompt_token_limit - prompt_safety_margin} safe limit)"
                ),
                meta=failure_meta,
                cause_info={
                    "category": "prompt_budget_exceeded",
                    "status_code": None,
                    "retryable": False,
                    "message": "Prepared prompt exceeds runtime prompt budget",
                },
            )
        if current_max_completion_tokens:
            current_max_completion_tokens = min(
                int(current_max_completion_tokens),
                int(available_completion_budget),
            )

    while True:
        request_retry_log = []
        crisis_retry_attempt = bool(
            crisis_retry_used
            and request_messages
            and _has_substantive_response(crisis_retry_base_response_text)
        )
        try:
            response_text, elapsed, usage, finish_reason = generate_response(
                prompt_text,
                gen_url,
                request_temperature,
                gen_model=gen_model,
                use_system_prompt=use_system_prompt,
                mode=mode,
                max_completion_tokens=current_max_completion_tokens,
                retry_log=request_retry_log,
                messages=request_messages,
                system_prompt_text=system_prompt_text,
            )
        except Exception as exc:
            retry_events.extend(request_retry_log)
            if crisis_retry_attempt:
                response_text = crisis_retry_base_response_text
                usage = crisis_retry_base_usage
                finish_reason = crisis_retry_base_finish_reason
                completion_cap_hit = crisis_retry_base_cap_hit
                break
            partial_meta = dict(meta)
            partial_meta.update({
                **prompt_budget_meta,
                "prompt_token_limit": prompt_token_limit,
                "prompt_token_safety_margin": prompt_safety_margin,
                "generation_time": round(total_elapsed, 1),
                "prompt_tokens": usage.get("prompt_tokens", 0),
                "completion_tokens": usage.get("completion_tokens", 0),
                "gen_url": gen_url,
                "gen_model": gen_model,
                "gen_worker": gen_worker or gen_url,
                "finish_reason": finish_reason,
                "completion_cap_hit": completion_cap_hit,
                "completion_attempts": completion_attempts,
                "retry_events": retry_events,
                "retry_cause_counts": _retry_cause_counts(retry_events),
                "completion_retry_count": max(len(completion_attempts) - 1, 0),
                "requested_max_completion_tokens": max_completion_tokens,
                "final_max_completion_tokens": current_max_completion_tokens,
                "runtime_profile": runtime_profile.get("name"),
            })
            raise PreparedPromptGenerationError(
                str(exc),
                meta=partial_meta,
                cause_info=_classify_generation_exception(exc),
            ) from exc

        total_elapsed += elapsed
        retry_events.extend(request_retry_log)
        completion_cap_hit = _is_completion_cap_hit(
            finish_reason,
            usage,
            current_max_completion_tokens,
            runtime_profile,
        )
        incomplete_safety_response = (
            safety_critical_generation
            and _is_obviously_incomplete_safety_response(response_text)
        )
        completion_attempts.append({
            "attempt": len(completion_attempts) + 1,
            "max_completion_tokens": current_max_completion_tokens,
            "finish_reason": finish_reason,
            "prompt_tokens": usage.get("prompt_tokens", 0),
            "completion_tokens": usage.get("completion_tokens", 0),
            "response_length": len(response_text or ""),
            "substantive": _has_substantive_response(response_text),
            "cap_hit": completion_cap_hit,
            "incomplete_safety_response": incomplete_safety_response,
            "retry_event_count": len(request_retry_log),
        })

        if crisis_retry_attempt:
            if _is_valid_crisis_retry_response(response_text):
                break
            response_text = crisis_retry_base_response_text
            usage = crisis_retry_base_usage
            finish_reason = crisis_retry_base_finish_reason
            completion_cap_hit = crisis_retry_base_cap_hit
            break

        if not _has_substantive_response(response_text):
            if empty_retries_used >= empty_retry_budget:
                break
            empty_retries_used += 1
            next_budget = _next_completion_budget(
                current_max_completion_tokens,
                runtime_profile,
            )
            if next_budget:
                current_max_completion_tokens = next_budget
            continue

        if (
            completion_cap_hit
            and runtime_profile.get("retry_on_cap_hit", True)
            and adaptive_retries_used < adaptive_retry_budget
        ):
            next_budget = _next_completion_budget(
                current_max_completion_tokens,
                runtime_profile,
            )
            if next_budget:
                adaptive_retries_used += 1
                current_max_completion_tokens = next_budget
                continue

        if (
            incomplete_safety_response
            and adaptive_retries_used < adaptive_retry_budget
        ):
            next_budget = _next_completion_budget(
                current_max_completion_tokens,
                runtime_profile,
            )
            if next_budget:
                adaptive_retries_used += 1
                current_max_completion_tokens = next_budget
                continue

        if (
            crisis_hardening_active
            and not crisis_retry_used
            and _is_obviously_incomplete_crisis_response(response_text)
        ):
            crisis_retry_base_response_text = response_text
            crisis_retry_base_usage = dict(usage)
            crisis_retry_base_finish_reason = finish_reason
            crisis_retry_base_cap_hit = completion_cap_hit
            retry_messages = _build_crisis_continuation_messages(
                question,
                response_text,
            )
            request_messages = retry_messages if use_system_prompt else retry_messages[1:]
            request_temperature = min(request_temperature, 0.05)
            if current_max_completion_tokens:
                current_max_completion_tokens = min(
                    int(current_max_completion_tokens),
                    CRISIS_RETRY_MAX_COMPLETION_TOKENS,
                )
            else:
                current_max_completion_tokens = CRISIS_RETRY_MAX_COMPLETION_TOKENS
            crisis_retry_used = True
            continue

        if crisis_retry_used and not _is_valid_crisis_retry_response(response_text):
            response_text = crisis_retry_base_response_text
            usage = crisis_retry_base_usage
            finish_reason = crisis_retry_base_finish_reason
            completion_cap_hit = crisis_retry_base_cap_hit

        break

    if (
        not _has_substantive_response(response_text)
        and _has_substantive_response(crisis_retry_base_response_text)
    ):
        response_text = crisis_retry_base_response_text
        usage = crisis_retry_base_usage
        finish_reason = crisis_retry_base_finish_reason
        completion_cap_hit = crisis_retry_base_cap_hit

    if safety_critical_generation:
        trimmed_response, did_trim = _trim_incomplete_final_safety_line(response_text)
        if did_trim:
            response_text = trimmed_response
            completion_safety_trimmed = True

    if not _has_substantive_response(response_text):
        failure_meta = dict(meta)
        failure_meta.update({
            **prompt_budget_meta,
            "prompt_token_limit": prompt_token_limit,
            "prompt_token_safety_margin": prompt_safety_margin,
            "generation_time": round(total_elapsed, 1),
            "prompt_tokens": usage.get("prompt_tokens", 0),
            "completion_tokens": usage.get("completion_tokens", 0),
            "gen_url": gen_url,
            "gen_model": gen_model,
            "gen_worker": gen_worker or gen_url,
            "finish_reason": finish_reason,
            "completion_cap_hit": completion_cap_hit,
            "completion_attempts": completion_attempts,
            "retry_events": retry_events,
            "retry_cause_counts": _retry_cause_counts(retry_events),
            "completion_retry_count": max(len(completion_attempts) - 1, 0),
            "completion_safety_trimmed": completion_safety_trimmed,
            "requested_max_completion_tokens": max_completion_tokens,
            "final_max_completion_tokens": current_max_completion_tokens,
            "runtime_profile": runtime_profile.get("name"),
        })
        raise PreparedPromptGenerationError(
            "Empty completion from generation server",
            meta=failure_meta,
            cause_info={
                "category": "empty_completion",
                "status_code": None,
                "retryable": False,
                "message": "Empty completion from generation server",
            },
        )

    updated_meta = dict(meta)
    updated_meta.update({
        **prompt_budget_meta,
        "prompt_token_limit": prompt_token_limit,
        "prompt_token_safety_margin": prompt_safety_margin,
        "generation_time": round(total_elapsed, 1),
        "prompt_tokens": usage.get("prompt_tokens", 0),
        "completion_tokens": usage.get("completion_tokens", 0),
        "gen_url": gen_url,
        "gen_model": gen_model,
        "gen_worker": gen_worker or gen_url,
        "finish_reason": finish_reason,
        "completion_cap_hit": completion_cap_hit,
        "completion_attempts": completion_attempts,
        "retry_events": retry_events,
        "retry_cause_counts": _retry_cause_counts(retry_events),
        "completion_retry_count": max(len(completion_attempts) - 1, 0),
        "completion_safety_trimmed": completion_safety_trimmed,
        "requested_max_completion_tokens": max_completion_tokens,
        "final_max_completion_tokens": current_max_completion_tokens,
        "runtime_profile": runtime_profile.get("name"),
    })

    return index, question, response_text, results, updated_meta


def format_sources(results, response_text, retrieval_meta=None):
    """Format source list and surface optional retrieval metadata."""
    citation_matches = re.findall(r"GD-\d+", response_text) if response_text else []
    cited_ids = list(dict.fromkeys(citation_matches))
    cited_id_set = set(citation_matches)
    lines = []
    seen = set()
    source_candidate_seen = set()
    source_candidates = []
    source_candidate_guide_ids = []
    category_counts = Counter()
    guide_families = set()
    metadatas = results.get("metadatas", [[]])[0] if isinstance(results, dict) else []

    for meta in metadatas:
        category = meta.get("category", "unknown")
        category_counts[category] += 1
        guide_family = meta.get("guide_id") or meta.get("guide_title") or meta.get("source_file")
        if guide_family:
            guide_families.add(guide_family)
        candidate_key = (
            meta.get("guide_id"),
            meta.get("guide_title"),
            meta.get("section_heading"),
        )
        if candidate_key not in source_candidate_seen:
            source_candidate_seen.add(candidate_key)
            guide_id = meta.get("guide_id") or ""
            if guide_id and guide_id not in source_candidate_guide_ids:
                source_candidate_guide_ids.append(guide_id)
            source_candidates.append({
                "rank": len(source_candidates) + 1,
                "guide_id": guide_id,
                "title": meta.get("guide_title") or "",
                "section": meta.get("section_heading") or "",
                "category": category,
                "source_file": meta.get("source_file") or "",
            })
        if cited_id_set and meta.get("guide_id") not in cited_id_set:
            continue
        key = (meta.get("guide_title"), meta.get("section_heading"))
        if key not in seen:
            seen.add(key)
            lines.append(
                f"  - {meta.get('guide_title', 'unknown')} -> "
                f"{meta.get('section_heading', 'unknown')} "
                f"({category})"
            )
    if citation_matches and not lines:
        lines = [f"  - {guide_id} (deterministic citation; no retrieved chunk)" for guide_id in cited_ids]
    if citation_matches:
        source_mode = "cited"
    elif metadatas:
        source_mode = "retrieved"
    else:
        source_mode = "none"

    scenario_frame = _extract_optional_meta_value(retrieval_meta, results, "scenario_frame")
    objective_coverage = _extract_optional_meta_value(retrieval_meta, results, "objective_coverage")
    category_distribution = _extract_optional_meta_value(retrieval_meta, results, "category_distribution")
    if not category_distribution:
        category_distribution = dict(category_counts)
    guide_family_diversity = _extract_optional_meta_value(retrieval_meta, results, "guide_family_diversity")
    if isinstance(guide_family_diversity, (dict, list, tuple, set)):
        guide_family_diversity = _count_items(guide_family_diversity)
    elif isinstance(guide_family_diversity, str) and guide_family_diversity.isdigit():
        guide_family_diversity = int(guide_family_diversity)
    if not guide_family_diversity:
        guide_family_diversity = len(guide_families)
    duplicate_citation_count = _extract_optional_meta_value(retrieval_meta, results, "duplicate_citation_count")
    if isinstance(duplicate_citation_count, (dict, list, tuple, set)):
        duplicate_citation_count = _count_items(duplicate_citation_count)
    elif isinstance(duplicate_citation_count, str) and duplicate_citation_count.isdigit():
        duplicate_citation_count = int(duplicate_citation_count)
    if duplicate_citation_count is None:
        duplicate_citation_count = max(len(citation_matches) - len(cited_ids), 0)
    support_signals = _extract_optional_meta_value(retrieval_meta, results, "support_signals")
    if not support_signals and (retrieval_meta or metadatas):
        support_signals = {
            "source_mode": source_mode,
            "citation_count": len(citation_matches),
            "unique_cited_guide_ids": len(cited_ids),
            "retrieved_chunk_count": len(metadatas),
        }
    retrieval_mix = _extract_optional_meta_value(retrieval_meta, results, "retrieval_mix")
    retrieval_profile = _extract_optional_meta_value(
        retrieval_meta, results, "retrieval_profile"
    )
    answer_mode = _extract_optional_meta_value(retrieval_meta, results, "answer_mode")
    confidence_label = _extract_optional_meta_value(
        retrieval_meta, results, "confidence_label"
    )
    raw_safety_critical = _extract_optional_meta_value(
        retrieval_meta, results, "safety_critical"
    )
    safety_critical = (
        _frame_is_safety_critical({"safety_critical": raw_safety_critical})
        or _frame_is_safety_critical(scenario_frame)
        or _profile_is_safety_critical(retrieval_profile)
    )
    support_strength = _support_strength(
        confidence_label,
        answer_mode,
        support_signals,
    )
    primary_source_titles = _primary_source_titles(source_candidates)

    metadata_summary_parts = []
    if retrieval_profile:
        metadata_summary_parts.append(f"profile={retrieval_profile}")
    scenario_frame_summary = _summarize_scenario_frame(scenario_frame)
    if scenario_frame_summary:
        metadata_summary_parts.append(f"scenario_frame={scenario_frame_summary}")
    objective_coverage_summary = _summarize_objective_coverage(objective_coverage)
    if objective_coverage_summary:
        metadata_summary_parts.append(f"objective_coverage={objective_coverage_summary}")
    category_distribution_summary = _summarize_category_distribution(category_distribution)
    if category_distribution_summary:
        metadata_summary_parts.append(f"categories={category_distribution_summary}")
    retrieval_mix_summary = _summarize_category_distribution(retrieval_mix)
    if retrieval_mix_summary:
        metadata_summary_parts.append(f"retrieval_mix={retrieval_mix_summary}")
    if guide_family_diversity not in (None, 0):
        metadata_summary_parts.append(f"guide_families={guide_family_diversity}")
    if duplicate_citation_count not in (None, 0):
        metadata_summary_parts.append(f"duplicate_citations={duplicate_citation_count}")
    support_signals_summary = _summarize_support_signals(support_signals)
    if support_signals_summary:
        metadata_summary_parts.append(f"support={support_signals_summary}")
    if answer_mode:
        metadata_summary_parts.append(f"answer_mode={answer_mode}")
    if support_strength:
        metadata_summary_parts.append(f"support_strength={support_strength}")
    if confidence_label:
        metadata_summary_parts.append(f"confidence={confidence_label}")

    return {
        "lines": "\n".join(lines) or "  - none",
        "source_mode": source_mode,
        "cited_guide_ids": cited_ids,
        "citation_count": len(citation_matches),
        "duplicate_citation_count": duplicate_citation_count,
        "scenario_frame": scenario_frame,
        "objective_coverage": objective_coverage,
        "category_distribution": category_distribution,
        "retrieval_mix": retrieval_mix,
        "retrieval_profile": retrieval_profile,
        "answer_mode": answer_mode,
        "source_candidates": source_candidates,
        "source_candidate_guide_ids": source_candidate_guide_ids,
        "primary_source_titles": primary_source_titles,
        "guide_family_diversity": guide_family_diversity,
        "support_signals": support_signals,
        "support_strength": support_strength,
        "safety_critical": safety_critical,
        "confidence_label": confidence_label,
        "retrieval_metadata_raw": _coerce_mapping(retrieval_meta),
        "metadata_summary": "; ".join(metadata_summary_parts) if metadata_summary_parts else "",
    }


def main():
    parser = argparse.ArgumentParser(description="Batch test runner for Senku")
    parser.add_argument("--prompts", default=DEFAULT_PROMPTS, help="File with one prompt per line")
    parser.add_argument("--section", type=str, default=None,
                        help="Comma-separated prompt section names from comment headers")
    parser.add_argument("--lane", type=str, default=None,
                        help="Comma-separated structured prompt lanes (for CSV/JSONL packs)")
    parser.add_argument("--style", type=str, default=None,
                        help="Comma-separated structured prompt styles (for CSV/JSONL packs)")
    parser.add_argument("--target-behavior", type=str, default=None,
                        help="Comma-separated structured prompt target-behavior labels (for CSV/JSONL packs)")
    parser.add_argument("--prompt-id", type=str, default=None,
                        help="Comma-separated structured prompt IDs (for CSV/JSONL packs)")
    parser.add_argument("--top-k", type=int, default=None, help="Chunks to retrieve")
    parser.add_argument("--temperature", type=float, default=0.11, help="LLM temperature")
    parser.add_argument(
        "--model",
        type=str,
        default=config.GEN_MODEL,
        help=f"Generation model override (default: {config.GEN_MODEL})",
    )
    parser.add_argument(
        "--worker-models",
        type=str,
        default=None,
        help="Comma-separated per-generation-slot model IDs; supply 1 model to broadcast or one model per URL",
    )
    parser.add_argument(
        "--mode",
        type=str,
        default="default",
        choices=getattr(config, "PROMPT_MODES", ("default", "review", "demo", "public-safe")),
        help="Prompt/output mode",
    )
    parser.add_argument("--category", type=str, default=None, help="Filter by category")
    parser.add_argument("--output", type=str, default=None, help="Output file (default: timestamped)")
    parser.add_argument("--urls", type=str, default=None,
                        help="Comma-separated generation URLs (default: config.GEN_URL)")
    parser.add_argument("--embed-url", type=str, default=None,
                        help="Comma-separated embedding/retrieval URLs (default: config.EMBED_URL)")
    parser.add_argument(
        "--max-completion-tokens",
        type=int,
        default=getattr(config, "MAX_COMPLETION_TOKENS", 2048),
        help="Maximum completion tokens per prompt (default: config.MAX_COMPLETION_TOKENS)",
    )
    parser.add_argument("--no-rag", action="store_true",
                        help="Skip retrieval/context building and send raw prompts directly to the model")
    parser.add_argument("--no-system-prompt", action="store_true",
                        help="Omit Senku's system prompt during generation")
    args = parser.parse_args()
    config.GEN_MODEL = args.model

    # Parse generation URLs
    default_gen_url = _default_generation_url()
    default_embed_url = _default_embedding_url()
    gen_urls = _parse_url_list(args.urls, default_gen_url)
    primary_gen_url = gen_urls[0] if gen_urls else default_gen_url
    runtime_profile = _runtime_profile(config.GEN_MODEL, primary_gen_url)
    if args.top_k is None:
        if hasattr(config, "get_runtime_top_k"):
            args.top_k = config.get_runtime_top_k(config.GEN_MODEL, primary_gen_url)
        else:
            args.top_k = config.TOP_K
    try:
        gen_models = _parse_worker_models(args.worker_models, len(gen_urls), args.model)
    except ValueError as exc:
        print(f"Invalid --worker-models value: {exc}")
        sys.exit(1)
    embed_urls = [] if args.no_rag else _parse_url_list(
        args.embed_url,
        default_embed_url,
        dedupe=True,
    )

    # Load prompts with section metadata
    prompt_entries, prompt_source_format = load_prompts(args.prompts)
    prompt_entries = _apply_prompt_filters(
        prompt_entries,
        sections=_parse_filter_values(args.section),
        lanes=_parse_filter_values(args.lane),
        styles=_parse_filter_values(args.style),
        target_behaviors=_parse_filter_values(args.target_behavior),
        prompt_ids=_parse_filter_values(args.prompt_id),
    )
    if args.section or args.lane or args.style or args.target_behavior or args.prompt_id:
        if not prompt_entries:
            print(
                "No prompts matched the requested filters: "
                f"section={args.section!r}, lane={args.lane!r}, style={args.style!r}, "
                f"target_behavior={args.target_behavior!r}, prompt_id={args.prompt_id!r}"
            )
            sys.exit(1)

    prompts = [entry["question"] for entry in prompt_entries]
    prompt_lane_counts = _count_prompt_entry_metadata(prompt_entries, "lane")
    prompt_style_counts = _count_prompt_entry_metadata(prompt_entries, "style")
    prompt_target_behavior_counts = _count_prompt_entry_metadata(prompt_entries, "target_behavior")
    prompt_id_count = sum(1 for entry in prompt_entries if entry.get("prompt_id"))

    print(f"Loaded {len(prompts)} prompts from {args.prompts}")
    print(f"Prompt source format: {prompt_source_format}")
    if prompt_entries:
        sections = []
        seen_sections = set()
        for entry in prompt_entries:
            if entry["section"] not in seen_sections:
                seen_sections.add(entry["section"])
                sections.append(entry["section"])
        print(f"Prompt sections: {', '.join(sections)}")
    if prompt_lane_counts:
        print(f"Prompt lanes: {prompt_lane_counts}")
    if prompt_style_counts:
        print(f"Prompt styles: {prompt_style_counts}")
    if prompt_target_behavior_counts:
        print(f"Prompt target behaviors: {prompt_target_behavior_counts}")
    if prompt_id_count:
        print(f"Prompt IDs present: {prompt_id_count}")
    print(f"Generation servers: {gen_urls}")
    print(f"Generation models: {gen_models}")
    if not args.no_rag:
        print(f"Embedding servers: {embed_urls}")

    # Verify all servers are reachable
    reachable_gen_pairs = []
    for url, model in zip(gen_urls, gen_models):
        try:
            requests.get(f"{url}/models", timeout=5)
            print(f"  {url} — OK (model={model})")
            reachable_gen_pairs.append((url, model))
        except requests.RequestException:
            print(f"  {url} — UNREACHABLE, removing")
    gen_urls = [url for url, _ in reachable_gen_pairs]
    gen_models = [model for _, model in reachable_gen_pairs]

    if not gen_urls:
        print("No reachable generation servers.")
        sys.exit(1)

    worker_targets = build_worker_targets(gen_urls, gen_models)
    print("Generation worker slots:")
    for worker in worker_targets:
        print(f"  {worker['label']} -> {worker['model']}")
    collection = None
    chunk_count = "N/A" if args.no_rag else None
    if not args.no_rag:
        reachable_embed_urls = []
        for url in embed_urls:
            try:
                requests.get(f"{url}/models", timeout=5)
                print(f"  embed {url} - OK")
                reachable_embed_urls.append(url)
            except requests.RequestException:
                print(f"  embed {url} - UNREACHABLE, removing")

        embed_urls = reachable_embed_urls
        if not embed_urls:
            print("No reachable embedding servers.")
            sys.exit(1)

        client = chromadb.PersistentClient(path=config.CHROMA_DB_DIR)
        try:
            collection = client.get_collection("senku_guides")
        except Exception:
            print("No collection found. Run ingest.py first.")
            sys.exit(1)

        chunk_count = get_collection_count_with_timeout(config.CHROMA_DB_DIR, "senku_guides")

    prep_worker_count, prep_worker_reason = _prep_worker_plan(
        embed_urls,
        gen_urls,
        use_rag=not args.no_rag,
    )
    prep_embed_urls = [] if args.no_rag else _prep_embed_assignments(
        embed_urls,
        prep_worker_count,
    )
    primary_embed_url = prep_embed_urls[0] if prep_embed_urls else None
    embed_config_label = "N/A" if args.no_rag else ", ".join(embed_urls)
    prep_embed_label = "N/A" if args.no_rag else ", ".join(prep_embed_urls)

    # Output file
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_path = args.output or f"bench_{timestamp}.md"

    # Progress log — written incrementally so progress can be monitored
    progress_path = output_path.replace(".md", "_progress.log")
    progress_lock = threading.Lock()
    with open(progress_path, "w", encoding="utf-8") as f:
        f.write("")

    def log_progress(msg):
        try:
            print(msg, flush=True)
        except BrokenPipeError:
            pass
        with progress_lock:
            with open(progress_path, "a", encoding="utf-8") as f:
                f.write(msg + "\n")

    wall_start = time.time()
    log_progress(f"Running {len(prompts)} prompts across {len(gen_urls)} server(s)...\n")

    # Work-stealing queue — each server grabs the next prepared prompt when free
    work_queue = queue.Queue()

    results_map = {}
    results_lock = threading.Lock()
    server_counts = {worker["label"]: 0 for worker in worker_targets}
    total_time = 0
    total_time_lock = threading.Lock()
    metrics_lock = threading.Lock()
    prep_metrics_lock = threading.Lock()
    completed = [0]  # mutable counter for thread-safe increment
    active_generations = [0]
    peak_active_generations = [0]
    first_generation_start = [None]
    last_generation_end = [None]
    prep_processed = [0]
    prepared_count = [0]
    active_preps = [0]
    peak_active_preps = [0]
    source_mode_counts = Counter()
    decision_path_counts = Counter()
    answer_provenance_counts = Counter()
    retrieval_metadata_presence = Counter()
    duplicate_citation_total = 0

    log_progress(
        f"Preparing retrieval context with {prep_worker_count} prep worker(s) "
        f"({prep_worker_reason}) using embed host(s) "
        f"{prep_embed_urls if prep_embed_urls else ['N/A']} while generation workers consume ready prompts..."
    )

    stop_event = threading.Event()
    prep_queue = queue.Queue()
    prepare_prompt_token_limit = _prompt_token_limit(runtime_profile=runtime_profile)

    def worker(worker_target):
        nonlocal total_time
        gen_url = worker_target["url"]
        worker_label = worker_target["label"]
        gen_model = worker_target["model"]
        while True:
            try:
                work_item = work_queue.get(timeout=0.5)
            except queue.Empty:
                if stop_event.is_set():
                    return
                continue

            prepared_prompt = work_item["prepared_prompt"]
            blocked_workers = work_item["blocked_workers"]

            if worker_label in blocked_workers:
                work_queue.put(work_item)
                work_queue.task_done()
                time.sleep(0.2)
                continue

            try:
                with metrics_lock:
                    active_generations[0] += 1
                    peak_active_generations[0] = max(
                        peak_active_generations[0],
                        active_generations[0],
                    )
                    if first_generation_start[0] is None:
                        first_generation_start[0] = time.time()
                try:
                    idx, question, response, results, meta = process_prepared_prompt(
                        prepared_prompt,
                        args.temperature,
                        gen_url,
                        gen_model=gen_model,
                        gen_worker=worker_label,
                        use_system_prompt=not args.no_system_prompt,
                        mode=args.mode,
                        max_completion_tokens=args.max_completion_tokens,
                        runtime_profile=runtime_profile,
                    )
                finally:
                    with metrics_lock:
                        active_generations[0] = max(active_generations[0] - 1, 0)
                        last_generation_end[0] = time.time()
            except Exception as e:
                error_info = _classify_generation_exception(e)
                partial_meta = dict(getattr(e, "meta", {}))
                if error_info.get("retryable") and len(blocked_workers) < len(worker_targets) - 1:
                    work_queue.put({
                        "prepared_prompt": prepared_prompt,
                        "blocked_workers": blocked_workers | {worker_label},
                    })
                    log_progress(
                        f"  [retry] #{prepared_prompt[0] + 1} {prepared_prompt[1][:50]}... "
                        f"({worker_label} failed: {error_info.get('category', 'error')} - {e})"
                    )
                    work_queue.task_done()
                    continue

                idx = prepared_prompt[0]
                question = prepared_prompt[1]
                idx, question, response, results, meta = (
                    idx, question, "", prepared_prompt[3],
                    {
                        **partial_meta,
                        "error": str(e),
                        "error_category": error_info.get("category"),
                        "error_status_code": error_info.get("status_code"),
                        "generation_time": partial_meta.get("generation_time", 0),
                        "prompt_tokens": partial_meta.get("prompt_tokens", 0),
                        "completion_tokens": partial_meta.get("completion_tokens", 0),
                        "gen_url": gen_url,
                        "gen_model": partial_meta.get("gen_model", gen_model),
                        "gen_worker": worker_label,
                        "finish_reason": partial_meta.get("finish_reason"),
                        "completion_cap_hit": partial_meta.get("completion_cap_hit", False),
                        "completion_attempts": partial_meta.get("completion_attempts", []),
                        "retry_events": partial_meta.get("retry_events", []),
                        "retry_cause_counts": partial_meta.get(
                            "retry_cause_counts",
                            _retry_cause_counts(partial_meta.get("retry_events", [])),
                        ),
                        "completion_retry_count": partial_meta.get("completion_retry_count", 0),
                        "requested_max_completion_tokens": partial_meta.get(
                            "requested_max_completion_tokens",
                            args.max_completion_tokens,
                        ),
                        "final_max_completion_tokens": partial_meta.get(
                            "final_max_completion_tokens",
                            args.max_completion_tokens,
                        ),
                        "runtime_profile": partial_meta.get(
                            "runtime_profile",
                            runtime_profile.get("name"),
                        ),
                    },
                )

            gen_time = meta.get("generation_time", 0)

            with results_lock:
                results_map[idx] = (question, response, results, meta)
                server_counts[worker_label] += 1
                completed[0] += 1
                done = completed[0]
            with total_time_lock:
                total_time += gen_time

            log_progress(f"  [{done}/{len(prompts)}] #{idx+1} {question[:50]}... "
                         f"({gen_time}s, {meta.get('completion_tokens', '?')} tok, {worker_label})")

            work_queue.task_done()

    def prep_worker(prep_embed_url):
        while True:
            job = prep_queue.get()
            if job is None:
                prep_queue.task_done()
                return

            i, prompt = job
            with prep_metrics_lock:
                active_preps[0] += 1
                peak_active_preps[0] = max(peak_active_preps[0], active_preps[0])

            try:
                prepared_prompt = prepare_prompt(
                    i,
                    prompt,
                    collection,
                    args.top_k,
                    args.category,
                    use_rag=not args.no_rag,
                    mode=args.mode,
                    embed_url=prep_embed_url,
                    prompt_token_limit=prepare_prompt_token_limit,
                )
                if prepared_prompt[2]:
                    work_queue.put({
                        "prepared_prompt": prepared_prompt,
                        "blocked_workers": set(),
                    })
                    with prep_metrics_lock:
                        prepared_count[0] += 1
                else:
                    idx, question, _, results, meta = prepared_prompt
                    immediate_response = meta.get("special_case_response", "")
                    with results_lock:
                        results_map[idx] = (question, immediate_response, results, meta)
                        completed[0] += 1
            except Exception as e:
                with results_lock:
                    results_map[i] = (
                        prompt,
                        "",
                        {"metadatas": [[]], "documents": [[]]},
                        {"error": str(e), "generation_time": 0, "gen_url": "N/A"},
                    )
                    completed[0] += 1
            finally:
                with prep_metrics_lock:
                    active_preps[0] = max(active_preps[0] - 1, 0)
                    prep_processed[0] += 1
                    processed = prep_processed[0]
                    ready_count = prepared_count[0]
                    active_prep_count = active_preps[0]
                    peak_prep_count = peak_active_preps[0]
                if processed % 10 == 0 or processed == len(prompts):
                    with results_lock:
                        immediate_count = completed[0]
                    with metrics_lock:
                        active_count = active_generations[0]
                        peak_active = peak_active_generations[0]
                    log_progress(
                        f"  [prep {processed}/{len(prompts)}] ready_for_generation={ready_count} "
                        f"immediate_results={immediate_count} "
                        f"queue_depth={work_queue.qsize()} active_generations={active_count} "
                        f"peak_active={peak_active} active_preps={active_prep_count} "
                        f"peak_prep={peak_prep_count}"
                    )
                prep_queue.task_done()

    generation_wall_start = time.time()
    threads = []
    for worker_target in worker_targets:
        t = threading.Thread(target=worker, args=(worker_target,), daemon=True)
        t.start()
        threads.append(t)

    prep_start = time.time()
    prep_threads = []
    for prep_embed_url in prep_embed_urls or [None]:
        t = threading.Thread(target=prep_worker, args=(prep_embed_url,), daemon=True)
        t.start()
        prep_threads.append(t)

    for i, prompt in enumerate(prompts):
        prep_queue.put((i, prompt))
    for _ in range(prep_worker_count):
        prep_queue.put(None)

    prep_queue.join()
    for t in prep_threads:
        t.join()

    if not prepared_count[0]:
        log_progress("No prompts reached generation after retrieval/build.")
    prep_duration = time.time() - prep_start

    work_queue.join()
    stop_event.set()

    for t in threads:
        t.join()
    generation_wall_duration = time.time() - generation_wall_start
    wall_duration = time.time() - wall_start
    if first_generation_start[0] and last_generation_end[0]:
        generation_active_window = max(
            last_generation_end[0] - first_generation_start[0],
            0.0,
        )
    else:
        generation_active_window = 0.0

    # Build report in prompt order
    report = []
    report.append(f"# Senku Bench Report — {datetime.now().strftime('%Y-%m-%d %H:%M')}\n")
    chunk_count_label = chunk_count if chunk_count is not None else "unknown"
    timing_summary = _build_generation_time_summary(results_map, total_time, len(prompts))
    error_count = timing_summary["error_count"]
    success_count = timing_summary["success_count"]
    worker_model_summary = ", ".join(sorted({worker["model"] for worker in worker_targets}))
    report.append(f"**Config:** model=`{config.GEN_MODEL}` | top_k={args.top_k} | "
                  f"temp={args.temperature} | mode={args.mode} | runtime_profile={runtime_profile['name']} | "
                  f"max_completion_tokens={args.max_completion_tokens} | "
                  f"prep_workers={prep_worker_count} ({prep_worker_reason}) | "
                  f"chunks_indexed={chunk_count_label} | "
                  f"servers={len(gen_urls)} | worker_models=`{worker_model_summary}` | "
                  f"embed_urls=`{embed_config_label}` | "
                  f"rag={'off' if args.no_rag else 'on'} | "
                  f"system_prompt={'off' if args.no_system_prompt else 'on'}\n")
    report.append(
        f"**Prompt source:** `{args.prompts}` ({prompt_source_format})\n"
    )
    prompt_filter_bits = []
    for label, value in (
        ("section", args.section),
        ("lane", args.lane),
        ("style", args.style),
        ("target_behavior", args.target_behavior),
        ("prompt_id", args.prompt_id),
    ):
        if value:
            prompt_filter_bits.append(f"{label}={value}")
    if prompt_filter_bits:
        report.append(f"**Prompt filter:** `{'; '.join(prompt_filter_bits)}`\n")
    report.append("---\n")

    section_counts = {}
    for i in range(len(prompts)):
        if i not in results_map:
            question = prompts[i]
            prompt_entry = prompt_entries[i]
            prompt_section = prompt_entry["section"]
            section_counts[prompt_section] = section_counts.get(prompt_section, 0) + 1
            report.append(
                f"## {i + 1}. {question}\n\n"
                f"*Section: {prompt_section}*\n\n"
                f"**ERROR:** worker exited before recording a result\n\n---\n"
            )
            continue

        prompt_entry = prompt_entries[i]
        question, response, results, meta = results_map[i]
        prompt_section = prompt_entry["section"]
        section_counts[prompt_section] = section_counts.get(prompt_section, 0) + 1

        if meta.get("error"):
            error_label = meta.get("error_category")
            prompt_metadata_lines = _render_prompt_entry_metadata(prompt_entry)
            report.append(
                f"## {i + 1}. {question}\n\n"
                f"*Section: {prompt_section}*\n\n"
                + ("\n".join(prompt_metadata_lines) + "\n\n" if prompt_metadata_lines else "")
                + f"**ERROR:** {meta['error']}"
                + (f" (`{error_label}`)" if error_label else "")
                + "\n\n---\n"
            )
            continue

        source_info = format_sources(results, response, meta.get("retrieval_metadata"))
        source_mode_counts[source_info["source_mode"]] += 1
        decision_path_counts[meta.get("decision_path", "unknown")] += 1
        answer_provenance_counts[
            meta.get("answer_provenance")
            or _answer_provenance_for_decision(meta.get("decision_path"))
        ] += 1
        if _count_items(source_info["scenario_frame"]):
            retrieval_metadata_presence["scenario_frame"] += 1
        if _count_items(source_info["objective_coverage"]):
            retrieval_metadata_presence["objective_coverage"] += 1
        if _count_items(source_info["category_distribution"]):
            retrieval_metadata_presence["category_distribution"] += 1
        if source_info["guide_family_diversity"] not in (None, 0):
            retrieval_metadata_presence["guide_family_diversity"] += 1
        if _count_items(source_info["support_signals"]):
            retrieval_metadata_presence["support_signals"] += 1
        duplicate_citation_total += source_info["duplicate_citation_count"] or 0
        sources = source_info["lines"]

        report.append(f"## {i + 1}. {question}\n")
        report.append(f"*Section: {prompt_section}*\n")
        for metadata_line in _render_prompt_entry_metadata(prompt_entry):
            report.append(f"{metadata_line}\n")
        decision_path = meta.get("decision_path", "unknown")
        decision_detail = meta.get("decision_detail")
        decision_label = (
            f"{decision_path} ({decision_detail})"
            if decision_detail and decision_detail != decision_path
            else decision_path
        )
        report.append(f"*Decision path: {decision_label}*\n")
        answer_provenance = (
            meta.get("answer_provenance")
            or _answer_provenance_for_decision(decision_path)
        )
        if answer_provenance:
            report.append(f"*Answer provenance: {answer_provenance}*\n")
        if meta.get("decomposed"):
            report.append(f"*Query decomposed into {len(meta['sub_queries'])} sub-queries: "
                          f"{meta['sub_queries'][1:]}*\n")
        server_label = meta.get("gen_worker") or meta.get("gen_url", "").split("//")[-1]
        report.append(f"*Generation: {meta.get('generation_time', 0)}s | "
                      f"Tokens: {meta.get('prompt_tokens', '?')}→{meta.get('completion_tokens', '?')} | "
                      f"Chunks: {meta.get('total_chunks_retrieved', 0)} | "
                      f"Server: {server_label}*\n")
        if (
            meta.get("finish_reason")
            or meta.get("completion_retry_count")
            or meta.get("completion_cap_hit")
            or meta.get("completion_safety_trimmed")
        ):
            report.append(
                f"*Runtime: finish={meta.get('finish_reason') or 'unknown'} | "
                f"retries={meta.get('completion_retry_count', 0)} | "
                f"cap_hit={'yes' if meta.get('completion_cap_hit') else 'no'} | "
                f"cap={meta.get('final_max_completion_tokens') or 'none'}"
                f"{' | safety_trim=yes' if meta.get('completion_safety_trimmed') else ''}*\n"
            )
        if source_info["metadata_summary"]:
            report.append(f"*Retrieval metadata: {source_info['metadata_summary']}*\n")
        report.append(f"\n{response}\n")
        source_header = (
            "**Sources:**"
            if source_info["source_mode"] != "retrieved"
            else "**Retrieved Context (not explicitly cited):**"
        )
        report.append(f"\n{source_header}\n{sources}\n")
        report.append("\n---\n")

    total_prompt_tokens = sum(
        (meta.get("prompt_tokens") or 0)
        for _, _, _, meta in results_map.values()
    )
    total_completion_tokens = sum(
        (meta.get("completion_tokens") or 0)
        for _, _, _, meta in results_map.values()
    )
    cap_hit_prompts = sum(
        1 for _, _, _, meta in results_map.values()
        if meta.get("completion_cap_hit")
    )
    completion_retry_total = sum(
        meta.get("completion_retry_count", 0)
        for _, _, _, meta in results_map.values()
    )
    retry_prompt_count = sum(
        1 for _, _, _, meta in results_map.values()
        if meta.get("retry_events") or meta.get("completion_retry_count", 0)
    )
    retry_cause_totals = Counter()
    total_retry_events = 0
    for _, _, _, meta in results_map.values():
        retry_events = meta.get("retry_events", [])
        total_retry_events += len(retry_events)
        retry_cause_totals.update(
            event.get("category", "unknown")
            for event in retry_events
        )
    wall_tok_per_sec = round(total_completion_tokens / wall_duration, 2) if wall_duration else 0
    generation_tok_per_sec = round(total_completion_tokens / total_time, 2) if total_time else 0
    prep_share = round(prep_duration / wall_duration, 3) if wall_duration else 0
    effective_generation_concurrency = (
        round(total_time / generation_active_window, 2)
        if generation_active_window and total_time
        else 0
    )

    # Summary
    report.append(f"\n## Summary\n")
    report.append(f"- **Prompts:** {len(prompts)}\n")
    report.append(f"- **Successful prompts:** {success_count}\n")
    report.append(f"- **Errors:** {error_count}\n")
    report.append(f"- **Wall duration:** {round(wall_duration, 1)}s\n")
    report.append(f"- **Prep duration:** {round(prep_duration, 1)}s\n")
    report.append(f"- **Generation wall duration:** {round(generation_wall_duration, 1)}s\n")
    report.append(f"- **Generation active window:** {round(generation_active_window, 1)}s\n")
    report.append(f"- **Total generation time:** {round(total_time, 1)}s\n")
    report.append(f"- **Successful generation time:** {timing_summary['successful_generation_time']}s\n")
    report.append(f"- **Avg per prompt:** {timing_summary['avg_generation_time']}s\n")
    report.append(
        f"- **Avg per successful prompt:** {timing_summary['avg_generation_time_success_only']}s\n"
    )
    report.append(f"- **Completion tok/s (wall):** {wall_tok_per_sec}\n")
    report.append(f"- **Completion tok/s (summed generation):** {generation_tok_per_sec}\n")
    report.append(f"- **Prompt tokens total:** {total_prompt_tokens}\n")
    report.append(f"- **Completion tokens total:** {total_completion_tokens}\n")
    report.append(f"- **Prep share of wall:** {prep_share}\n")
    report.append(f"- **Prep workers:** {prep_worker_count} ({prep_worker_reason})\n")
    report.append(f"- **Prep embed assignments:** {prep_embed_label}\n")
    report.append(f"- **Peak active preps:** {peak_active_preps[0]}\n")
    report.append(f"- **Peak active generations:** {peak_active_generations[0]}\n")
    report.append(f"- **Effective generation concurrency:** {effective_generation_concurrency}\n")
    report.append(f"- **Cap-hit prompts:** {cap_hit_prompts}\n")
    report.append(f"- **Prompts with retries:** {retry_prompt_count}\n")
    report.append(f"- **Completion retries:** {completion_retry_total}\n")
    report.append(f"- **Request retry events:** {total_retry_events}\n")
    report.append(f"\n**Prompt sections:**\n")
    for section, count in section_counts.items():
        report.append(f"- `{section}`: {count} prompts\n")
    if prompt_lane_counts:
        report.append(f"\n**Prompt lanes:**\n")
        for lane, count in sorted(prompt_lane_counts.items()):
            report.append(f"- `{lane}`: {count} prompts\n")
    if prompt_style_counts:
        report.append(f"\n**Prompt styles:**\n")
        for style, count in sorted(prompt_style_counts.items()):
            report.append(f"- `{style}`: {count} prompts\n")
    if prompt_target_behavior_counts:
        report.append(f"\n**Prompt target behaviors:**\n")
        for behavior, count in sorted(prompt_target_behavior_counts.items()):
            report.append(f"- `{behavior}`: {count} prompts\n")
    report.append(f"\n**Server workload:**\n")
    for worker_target in worker_targets:
        label = worker_target["label"]
        report.append(f"- `{label}`: {server_counts.get(label, 0)} prompts\n")
    report.append(f"\n**Source modes:**\n")
    for mode in ("cited", "retrieved", "none"):
        report.append(f"- `{mode}`: {source_mode_counts.get(mode, 0)} prompts\n")
    report.append(f"\n**Decision paths:**\n")
    for path, count in sorted(decision_path_counts.items()):
        report.append(f"- `{path}`: {count} prompts\n")
    report.append(f"\n**Answer provenance:**\n")
    for provenance, count in sorted(answer_provenance_counts.items()):
        report.append(f"- `{provenance}`: {count} prompts\n")
    report.append(f"\n**Retrieval metadata coverage:**\n")
    for key in (
        "scenario_frame",
        "objective_coverage",
        "category_distribution",
        "guide_family_diversity",
        "support_signals",
    ):
        report.append(f"- `{key}`: {retrieval_metadata_presence.get(key, 0)} prompts\n")
    report.append(f"- `duplicate_citation_total`: {duplicate_citation_total}\n")
    report.append(f"\n**Retry causes:**\n")
    if retry_cause_totals:
        for cause, count in sorted(retry_cause_totals.items()):
            report.append(f"- `{cause}`: {count}\n")
    else:
        report.append("- `none`: 0\n")

    with open(output_path, "w", encoding="utf-8") as f:
        f.write("\n".join(report))

    # Write machine-readable JSON alongside the markdown report
    json_path = output_path.replace(".md", ".json")
    json_results = []
    for i in range(len(prompts)):
        if i not in results_map:
            prompt_entry = prompt_entries[i]
            json_results.append({
                "index": i + 1,
                "question": prompts[i],
                "section": prompt_entry["section"],
                "prompt_id": prompt_entry.get("prompt_id"),
                "lane": prompt_entry.get("lane"),
                "style": prompt_entry.get("style"),
                "target_behavior": prompt_entry.get("target_behavior"),
                "what_it_tests": prompt_entry.get("what_it_tests"),
                "prompt_metadata": _json_safe(prompt_entry.get("metadata", {})),
                "response_text": "",
                "error": "worker exited before recording a result",
            })
            continue

        prompt_entry = prompt_entries[i]
        question, response, results, meta = results_map[i]
        source_info = format_sources(results, response, meta.get("retrieval_metadata"))
        json_results.append({
            "index": i + 1,
            "question": question,
            "section": prompt_entry["section"],
            "prompt_id": prompt_entry.get("prompt_id"),
            "lane": prompt_entry.get("lane"),
            "style": prompt_entry.get("style"),
            "target_behavior": prompt_entry.get("target_behavior"),
            "what_it_tests": prompt_entry.get("what_it_tests"),
            "prompt_metadata": _json_safe(prompt_entry.get("metadata", {})),
            "decision_path": meta.get("decision_path"),
            "decision_detail": meta.get("decision_detail"),
            "answer_provenance": meta.get("answer_provenance")
            or _answer_provenance_for_decision(meta.get("decision_path")),
            "reviewed_card_backed": bool(meta.get("reviewed_card_backed")),
            "reviewed_card_ids": _json_safe(meta.get("reviewed_card_ids", [])),
            "reviewed_card_review_status": meta.get("reviewed_card_review_status") or "",
            "reviewed_card_guide_ids": _json_safe(meta.get("reviewed_card_guide_ids", [])),
            "reviewed_card_cited_guide_ids": _json_safe(
                meta.get("reviewed_card_cited_guide_ids", [])
            ),
            "decomposed": meta.get("decomposed", False),
            "sub_queries": meta.get("sub_queries", []),
            "generation_time": meta.get("generation_time", 0),
            "prompt_tokens": meta.get("prompt_tokens", 0),
            "completion_tokens": meta.get("completion_tokens", 0),
            "finish_reason": meta.get("finish_reason"),
            "completion_cap_hit": meta.get("completion_cap_hit", False),
            "completion_retry_count": meta.get("completion_retry_count", 0),
            "completion_safety_trimmed": meta.get("completion_safety_trimmed", False),
            "requested_max_completion_tokens": meta.get("requested_max_completion_tokens"),
            "final_max_completion_tokens": meta.get("final_max_completion_tokens"),
            "runtime_profile": meta.get("runtime_profile"),
            "model": meta.get("gen_model", config.GEN_MODEL),
            "retry_events": _json_safe(meta.get("retry_events", [])),
            "retry_cause_counts": _json_safe(meta.get("retry_cause_counts", {})),
            "completion_attempts": _json_safe(meta.get("completion_attempts", [])),
            "chunks_retrieved": meta.get("total_chunks_retrieved", 0),
            "server": meta.get("gen_url", ""),
            "worker": meta.get("gen_worker"),
            "cited_guide_ids": source_info["cited_guide_ids"],
            "source_mode": source_info["source_mode"],
            "confidence_label": source_info["confidence_label"],
            "answer_mode": source_info["answer_mode"],
            "support_strength": source_info["support_strength"],
            "safety_critical": source_info["safety_critical"],
            "retrieval_profile": source_info["retrieval_profile"],
            "retrieval_metadata": _json_safe({
                "scenario_frame": source_info["scenario_frame"],
                "objective_coverage": source_info["objective_coverage"],
                "category_distribution": source_info["category_distribution"],
                "retrieval_mix": source_info["retrieval_mix"],
                "retrieval_profile": source_info["retrieval_profile"],
                "answer_mode": source_info["answer_mode"],
                "guide_family_diversity": source_info["guide_family_diversity"],
                "duplicate_citation_count": source_info["duplicate_citation_count"],
                "support_signals": source_info["support_signals"],
                "support_strength": source_info["support_strength"],
                "safety_critical": source_info["safety_critical"],
                "top_retrieved_guide_ids": source_info["source_candidate_guide_ids"],
                "source_candidates": source_info["source_candidates"],
                "primary_source_titles": source_info["primary_source_titles"],
            }),
            "retrieval_metadata_summary": source_info["metadata_summary"],
            "citation_count": source_info["citation_count"],
            "duplicate_citation_count": source_info["duplicate_citation_count"],
            "response_text": response,
            "response_length": len(response),
            "error": meta.get("error"),
            "error_category": meta.get("error_category"),
            "error_status_code": meta.get("error_status_code"),
            "rubric": {
                "relevant": None,
                "sourced": None,
                "safe": None,
                "actionable": None,
                "grounded": None,
                "completeness": None,
                "constraint_coverage": None,
                "asset_accounting": None,
                "scope_discipline": None,
                "escalation_discipline": None,
                "calibration": None,
                "citation_precision": None,
                "source_diversity": None,
                "overall": None,
                "root_cause": None,
                "notes": None,
            },
        })

    json_output = {
        "timestamp": datetime.now().isoformat(),
        "config": {
            "model": config.GEN_MODEL,
            "worker_models": [worker["model"] for worker in worker_targets],
            "runtime_profile": runtime_profile["name"],
            "prep_workers": prep_worker_count,
            "prep_worker_reason": prep_worker_reason,
            "top_k": args.top_k,
            "temperature": args.temperature,
            "mode": args.mode,
            "max_completion_tokens": args.max_completion_tokens,
            "chunks_indexed": chunk_count,
            "prompt_source_path": args.prompts,
            "prompt_source_format": prompt_source_format,
            "prompt_filters": {
                "section": args.section,
                "lane": args.lane,
                "style": args.style,
                "target_behavior": args.target_behavior,
                "prompt_id": args.prompt_id,
            },
            "generation_servers": gen_urls,
            "generation_workers": [worker["label"] for worker in worker_targets],
            "embed_url": None if args.no_rag else primary_embed_url,
            "embed_urls": [] if args.no_rag else embed_urls,
            "prep_embed_urls": [] if args.no_rag else prep_embed_urls,
            "rag": not args.no_rag,
            "system_prompt": not args.no_system_prompt,
        },
        "section_filter": args.section,
        "prompt_filters": {
            "section": args.section,
            "lane": args.lane,
            "style": args.style,
            "target_behavior": args.target_behavior,
            "prompt_id": args.prompt_id,
        },
        "summary": {
            "total_prompts": len(prompts),
            "successful_prompts": success_count,
            "errors": error_count,
            "wall_duration": round(wall_duration, 1),
            "prep_duration": round(prep_duration, 1),
            "generation_wall_duration": round(generation_wall_duration, 1),
            "generation_active_window": round(generation_active_window, 1),
            "total_generation_time": round(total_time, 1),
            "successful_generation_time": timing_summary["successful_generation_time"],
            "avg_generation_time": timing_summary["avg_generation_time"],
            "avg_generation_time_success_only": timing_summary["avg_generation_time_success_only"],
            "total_prompt_tokens": total_prompt_tokens,
            "total_completion_tokens": total_completion_tokens,
            "wall_completion_tokens_per_second": wall_tok_per_sec,
            "generation_completion_tokens_per_second": generation_tok_per_sec,
            "prep_share_of_wall": prep_share,
            "prep_workers": prep_worker_count,
            "prep_worker_reason": prep_worker_reason,
            "peak_active_preps": peak_active_preps[0],
            "peak_active_generations": peak_active_generations[0],
            "effective_generation_concurrency": effective_generation_concurrency,
            "cap_hit_prompts": cap_hit_prompts,
            "retry_prompt_count": retry_prompt_count,
            "completion_retry_total": completion_retry_total,
            "retry_event_total": total_retry_events,
            "retry_counts_by_cause": dict(retry_cause_totals),
            "server_workload": {worker["label"]: server_counts.get(worker["label"], 0) for worker in worker_targets},
            "section_counts": section_counts,
            "lane_counts": prompt_lane_counts,
            "style_counts": prompt_style_counts,
            "target_behavior_counts": prompt_target_behavior_counts,
            "source_mode_counts": dict(source_mode_counts),
            "decision_path_counts": dict(decision_path_counts),
            "answer_provenance_counts": dict(answer_provenance_counts),
            "retrieval_metadata_presence": dict(retrieval_metadata_presence),
            "duplicate_citation_total": duplicate_citation_total,
        },
        "results": json_results,
    }

    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(json_output, f, indent=2, ensure_ascii=False)

    log_progress(f"\nReport saved to {output_path}")
    log_progress(f"JSON data saved to {json_path}")


if __name__ == "__main__":
    main()
