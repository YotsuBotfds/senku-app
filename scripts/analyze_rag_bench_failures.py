#!/usr/bin/env python3
"""Offline diagnostics for Senku bench RAG failures."""

from __future__ import annotations

import argparse
import csv
import json
import re
import sys
from collections import Counter, defaultdict
from datetime import datetime
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from guide_answer_card_contracts import load_answer_cards
from rag_bench_answer_diagnostics import (
    answer_card_diagnostics,
    answer_has_emergency_contract,
    app_acceptance_diagnostics,
    claim_support_diagnostics,
    has_safety_prompt,
    select_answer_cards,
    shadow_card_answer_diagnostics,
)

try:
    import yaml
except ImportError:  # pragma: no cover - exercised only in minimal envs.
    yaml = None


GUIDE_ID_RE = re.compile(r"\bGD-\d+\b")
SECTION_RE = re.compile(r"^##\s+(\d+)\.\s+(.+)$", re.MULTILINE)
WAVE_ARTIFACT_RE = re.compile(r"guide_wave_([a-z]+)_", re.IGNORECASE)
DEFAULT_EXPECTATIONS_PATH = Path("notes/specs/rag_prompt_expectations_seed_20260424.yaml")

BUCKETS = (
    "deterministic_pass",
    "expected_supported",
    "rag_unknown_no_expectation",
    "retrieval_miss",
    "ranking_miss",
    "generation_miss",
    "unsupported_or_truncated_answer",
    "safety_contract_miss",
    "abstain_or_clarify_needed",
    "artifact_error",
)

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
EXPECTED_FAMILY_KEYS = (
    "expected_guide_family",
    "expected_family",
    "expected_families",
    "target_guide_family",
    "target_family",
    "guide_family",
)

CSV_FIELDS = (
    "artifact_path",
    "artifact_name",
    "prompt_index",
    "prompt_id",
    "section",
    "prompt_text",
    "decision_path",
    "decision_detail",
    "answer_provenance",
    "reviewed_card_backed",
    "answer_surface_label",
    "reviewed_card_ids",
    "reviewed_card_review_status",
    "reviewed_card_guide_ids",
    "answer_mode",
    "support_strength",
    "safety_critical",
    "retrieval_profile",
    "app_gate_status",
    "app_acceptance_status",
    "app_acceptance_reason",
    "evidence_owner_status",
    "safety_surface_status",
    "ui_surface_bucket",
    "generated",
    "source_mode",
    "cited_guide_ids",
    "top_retrieved_guide_ids",
    "top_retrieved_guide_ranks",
    "expected_guide_ids",
    "expected_guide_family",
    "expected_hit_at_1",
    "expected_hit_at_3",
    "expected_hit_at_k",
    "expected_cited",
    "answer_card_status",
    "answer_card_ids",
    "answer_card_required_hits",
    "answer_card_missing_required",
    "answer_card_forbidden_hits",
    "claim_support_status",
    "claim_action_count",
    "claim_supported_count",
    "claim_unknown_count",
    "claim_forbidden_count",
    "claim_support_basis",
    "shadow_card_answer_status",
    "shadow_claim_support_status",
    "shadow_claim_action_count",
    "shadow_card_answer_cited_guide_ids",
    "cap_hit",
    "retry_count",
    "completion_safety_trimmed",
    "error",
    "error_category",
    "suspected_failure_bucket",
    "short_reason",
)


def load_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def load_expectations(path: Path | None) -> dict:
    if path is None:
        return {}
    if not path.exists():
        raise FileNotFoundError(path)
    if path.suffix.lower() == ".json":
        return load_json(path)
    if yaml is None:
        raise RuntimeError(
            f"Cannot read YAML expectations without PyYAML installed: {path}"
        )
    with path.open("r", encoding="utf-8") as handle:
        return yaml.safe_load(handle) or {}


def load_guide_lookup(guides_dir: Path = Path("guides")) -> dict[str, dict[str, str]]:
    lookup = {"slug_to_id": {}, "title_to_id": {}}
    if not guides_dir.exists():
        return lookup

    for path in guides_dir.glob("*.md"):
        frontmatter = read_frontmatter(path)
        guide_id = frontmatter.get("id")
        if not guide_id:
            continue
        slug = frontmatter.get("slug")
        title = frontmatter.get("title")
        if slug:
            lookup["slug_to_id"][slug] = guide_id
        if title:
            lookup["title_to_id"][normalize_title(title)] = guide_id
    return lookup


def read_frontmatter(path: Path) -> dict[str, str]:
    fields: dict[str, str] = {}
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except UnicodeDecodeError:
        lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
    if not lines or lines[0].strip() != "---":
        return fields
    for line in lines[1:]:
        if line.strip() == "---":
            break
        if ":" not in line or line[:1].isspace():
            continue
        key, value = line.split(":", 1)
        value = value.strip().strip('"').strip("'")
        if value:
            fields[key.strip()] = value
    return fields


def normalize_title(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip().lower()


def parse_markdown_source_lines(markdown_path: Path) -> dict[int, list[str]]:
    if not markdown_path.exists():
        return {}
    text = markdown_path.read_text(encoding="utf-8")
    matches = list(SECTION_RE.finditer(text))
    source_lines_by_index: dict[int, list[str]] = {}
    for position, match in enumerate(matches):
        prompt_index = int(match.group(1))
        start = match.end()
        end = matches[position + 1].start() if position + 1 < len(matches) else len(text)
        body = text[start:end]
        marker = "\n**Sources:**\n"
        if marker not in body:
            marker = "\n**Retrieved Context (not explicitly cited):**\n"
            if marker not in body:
                continue
        source_block = body.split(marker, 1)[1]
        lines = []
        for raw_line in source_block.splitlines():
            stripped = raw_line.strip()
            if not stripped or stripped == "---" or stripped.startswith("## "):
                break
            if stripped.startswith("- ") or stripped.startswith("* "):
                lines.append(stripped[2:].strip())
        source_lines_by_index[prompt_index] = lines
    return source_lines_by_index


def unique_guide_ids(value) -> list[str]:
    seen = set()
    guide_ids = []
    for guide_id in GUIDE_ID_RE.findall(str(value or "")):
        if guide_id not in seen:
            seen.add(guide_id)
            guide_ids.append(guide_id)
    return guide_ids


def parse_guide_ids(value) -> list[str]:
    if value is None:
        return []
    if isinstance(value, dict):
        ids = []
        for item in value.values():
            ids.extend(parse_guide_ids(item))
        return list(dict.fromkeys(ids))
    if isinstance(value, (list, tuple, set)):
        ids = []
        for item in value:
            ids.extend(parse_guide_ids(item))
        return list(dict.fromkeys(ids))
    return unique_guide_ids(value)


def first_text(value) -> str:
    if value is None:
        return ""
    if isinstance(value, (list, tuple, set)):
        return ", ".join(str(item).strip() for item in value if str(item).strip())
    if isinstance(value, dict):
        return ", ".join(
            f"{key}={item}" for key, item in value.items() if str(item).strip()
        )
    return str(value).strip()


def _truthy(value) -> bool:
    return str(value or "").strip().lower() in {"1", "true", "yes", "y", "on"}


def wave_key_for_artifact(path: Path) -> str:
    match = WAVE_ARTIFACT_RE.search(path.name)
    return match.group(1).lower() if match else ""


def expected_from_manifest(
    path: Path,
    expectations: dict,
    guide_lookup: dict[str, dict[str, str]],
) -> tuple[list[str], str]:
    wave_key = wave_key_for_artifact(path)
    wave = ((expectations.get("waves") or {}).get(wave_key) or {})
    if not isinstance(wave, dict):
        return [], ""

    ids = []
    for item in wave.get("expected_guides") or []:
        if isinstance(item, dict):
            if item.get("id"):
                ids.append(str(item["id"]))
            elif item.get("slug"):
                guide_id = guide_lookup["slug_to_id"].get(str(item["slug"]))
                if guide_id:
                    ids.append(guide_id)
        else:
            ids.extend(parse_guide_ids(item))

    family = first_text(wave.get("topic") or wave.get("expected_family"))
    return list(dict.fromkeys(ids)), family


def expected_from_result(result: dict) -> tuple[list[str], str]:
    metadata = result.get("prompt_metadata") or {}
    containers = [result, metadata] if isinstance(metadata, dict) else [result]

    expected_ids = []
    expected_family = ""
    for container in containers:
        for key in EXPECTED_GUIDE_KEYS:
            if key in container:
                expected_ids.extend(parse_guide_ids(container.get(key)))
        for key in EXPECTED_FAMILY_KEYS:
            if key in container and not expected_family:
                expected_family = first_text(container.get(key))

    return list(dict.fromkeys(expected_ids)), expected_family


def extract_candidate_guide_ids(
    result: dict,
    markdown_source_lines: list[str],
    guide_lookup: dict[str, dict[str, str]],
) -> list[str]:
    metadata = result.get("retrieval_metadata") or {}
    for key in (
        "top_retrieved_guide_ids",
        "retrieved_guide_ids",
        "top_guide_ids",
        "guide_ids_by_rank",
    ):
        ids = parse_guide_ids(metadata.get(key))
        if ids:
            return ids

    for key in ("top_retrieved_guides", "retrieved_guides", "top_sources", "sources"):
        ids = parse_ranked_guide_dicts(metadata.get(key))
        if ids:
            return ids

    ids = []
    for line in markdown_source_lines:
        ids.extend(unique_guide_ids(line))
        title = line.split(" -> ", 1)[0].strip()
        guide_id = guide_lookup["title_to_id"].get(normalize_title(title))
        if guide_id:
            ids.append(guide_id)
    return list(dict.fromkeys(ids))


def parse_ranked_guide_dicts(value) -> list[str]:
    if not isinstance(value, list):
        return []
    ranked = []
    for item in value:
        if isinstance(item, dict):
            ids = parse_guide_ids(
                item.get("guide_id")
                or item.get("id")
                or item.get("guide")
                or item.get("source")
            )
        else:
            ids = parse_guide_ids(item)
        ranked.extend(ids)
    return list(dict.fromkeys(ranked))


def hit_at(expected_ids: list[str], candidates: list[str], k: int) -> str:
    if not expected_ids:
        return "unknown"
    if not candidates:
        return "unknown"
    return "yes" if set(expected_ids) & set(candidates[:k]) else "no"


def objective_coverage_status(result: dict) -> str:
    coverage = (result.get("retrieval_metadata") or {}).get("objective_coverage")
    if not isinstance(coverage, list):
        return ""
    statuses = [str(row.get("status", "")).lower() for row in coverage if isinstance(row, dict)]
    if not statuses:
        return ""
    if any(status == "missing" for status in statuses):
        return "missing"
    if all(status == "covered" for status in statuses):
        return "covered"
    return ",".join(sorted(set(statuses)))


def metadata_value(result: dict, key: str):
    if key in result:
        return result.get(key)
    retrieval_meta = result.get("retrieval_metadata") or {}
    if isinstance(retrieval_meta, dict):
        return retrieval_meta.get(key)
    return None


def answer_provenance_for_decision(decision_path: str) -> str:
    normalized = str(decision_path or "").strip().lower()
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


def pipe_text(value) -> str:
    if value is None:
        return ""
    if isinstance(value, (list, tuple, set)):
        return "|".join(str(item).strip() for item in value if str(item).strip())
    if isinstance(value, dict):
        return "|".join(
            f"{key}={item}" for key, item in value.items() if str(item).strip()
        )
    text = str(value).strip()
    return text.replace(",", "|") if "," in text else text


def answer_surface_label(provenance: str, app_gate_status: str = "") -> str:
    if app_gate_status == "abstain":
        return "abstain"
    if app_gate_status == "uncertain_fit":
        return "limited_fit"
    if provenance == "reviewed_card_runtime":
        return "reviewed_card_evidence"
    if provenance == "generated_model":
        return "generated_evidence"
    if provenance == "deterministic_rule":
        return "deterministic_rule"
    if provenance == "uncertain_fit_card":
        return "limited_fit"
    if provenance == "abstain_card":
        return "abstain"
    if provenance == "no_rag":
        return "no_rag"
    if provenance == "no_answer":
        return "no_answer"
    return "unknown"


def extract_answer_provenance_fields(result: dict) -> dict[str, str]:
    decision_path = str(result.get("decision_path") or "")
    provenance = first_text(metadata_value(result, "answer_provenance"))
    if not provenance:
        provenance = answer_provenance_for_decision(decision_path)
    reviewed_card_backed = _truthy(metadata_value(result, "reviewed_card_backed"))
    if not reviewed_card_backed and provenance == "reviewed_card_runtime":
        reviewed_card_backed = True

    return {
        "answer_provenance": provenance,
        "reviewed_card_backed": "yes" if reviewed_card_backed else "no",
        "answer_surface_label": answer_surface_label(
            provenance,
            first_text(metadata_value(result, "app_gate_status")),
        ),
        "reviewed_card_ids": pipe_text(metadata_value(result, "reviewed_card_ids")),
        "reviewed_card_review_status": first_text(
            metadata_value(result, "reviewed_card_review_status")
        ),
        "reviewed_card_guide_ids": pipe_text(
            metadata_value(result, "reviewed_card_guide_ids")
        ),
    }


def extract_app_gate_fields(result: dict) -> dict[str, str]:
    answer_mode = first_text(metadata_value(result, "answer_mode"))
    support_strength = first_text(metadata_value(result, "support_strength"))
    safety_critical = first_text(metadata_value(result, "safety_critical"))
    retrieval_profile = first_text(metadata_value(result, "retrieval_profile"))

    decision_path = str(result.get("decision_path") or "")
    gate_status = first_text(metadata_value(result, "app_gate_status"))
    if not gate_status:
        if decision_path == "uncertain_fit" or answer_mode == "uncertain_fit":
            gate_status = "uncertain_fit"
        elif decision_path == "abstain" or answer_mode == "abstain":
            gate_status = "abstain"

    return {
        "answer_mode": answer_mode,
        "support_strength": support_strength,
        "safety_critical": safety_critical,
        "retrieval_profile": retrieval_profile,
        "app_gate_status": gate_status,
    }


def classify_bucket(
    result: dict,
    *,
    expected_ids: list[str],
    candidate_ids: list[str],
    cited_ids: list[str],
) -> tuple[str, str]:
    decision_path = str(result.get("decision_path") or "")
    retrieval_meta = result.get("retrieval_metadata") or {}
    answer_mode = str(
        result.get("answer_mode") or retrieval_meta.get("answer_mode") or ""
    )
    answer = result.get("response_text") or ""

    if result.get("error") or result.get("error_category"):
        return "artifact_error", "artifact has error metadata"
    if decision_path == "deterministic":
        return "deterministic_pass", "deterministic route handled prompt"
    if result.get("completion_cap_hit") or result.get("finish_reason") == "length":
        return "unsupported_or_truncated_answer", "completion hit cap or length stop"
    if decision_path in {"abstain", "rag-empty", "uncertain_fit"} or answer_mode in {
        "abstain",
        "uncertain_fit",
    }:
        reason = f"decision_path={decision_path}"
        if answer_mode:
            reason += f"; answer_mode={answer_mode}"
        return "abstain_or_clarify_needed", reason
    if has_safety_prompt(result.get("question") or "") and not answer_has_emergency_contract(answer):
        return "safety_contract_miss", "safety prompt lacks obvious emergency contract language"

    if expected_ids:
        expected = set(expected_ids)
        candidates = set(candidate_ids)
        cited = set(cited_ids)
        if candidate_ids and not (expected & candidates):
            return "retrieval_miss", "expected guide absent from retrieved/source candidates"
        if candidate_ids and candidate_ids[0] not in expected and expected & candidates:
            return "ranking_miss", "expected guide retrieved but not rank 1"
        if expected & candidates and not (expected & cited):
            return "generation_miss", "expected guide retrieved but not cited"
        return "expected_supported", "expected guide appears supported; no failure inferred"

    if objective_coverage_status(result) == "missing":
        return "retrieval_miss", "artifact reports missing objective coverage"
    if result.get("source_mode") in {"none", ""} and decision_path == "rag":
        return "unsupported_or_truncated_answer", "RAG answer has no source mode"
    return "rag_unknown_no_expectation", "no expected guide metadata available"


def build_rows(
    paths: list[Path],
    expectations: dict | None = None,
    guide_lookup: dict[str, dict[str, str]] | None = None,
    answer_cards: list[dict] | None = None,
) -> list[dict]:
    rows = []
    expectations = expectations or {}
    guide_lookup = guide_lookup or load_guide_lookup()
    if answer_cards is None:
        answer_cards = load_answer_cards()
    for path in paths:
        data = load_json(path)
        markdown_sources = parse_markdown_source_lines(path.with_suffix(".md"))
        manifest_expected_ids, manifest_expected_family = expected_from_manifest(
            path, expectations, guide_lookup
        )
        for result in data.get("results", []):
            prompt_index = result.get("index")
            cited_ids = list(dict.fromkeys(result.get("cited_guide_ids") or []))
            markdown_source_lines = markdown_sources.get(prompt_index, [])
            candidate_ids = extract_candidate_guide_ids(
                result, markdown_source_lines, guide_lookup
            )
            result_expected_ids, result_expected_family = expected_from_result(result)
            expected_ids = list(dict.fromkeys(result_expected_ids + manifest_expected_ids))
            expected_family = result_expected_family or manifest_expected_family
            bucket, reason = classify_bucket(
                result,
                expected_ids=expected_ids,
                candidate_ids=candidate_ids,
                cited_ids=cited_ids,
            )
            app_gate_fields = extract_app_gate_fields(result)
            answer_provenance_fields = extract_answer_provenance_fields(result)
            answer_provenance_fields["answer_surface_label"] = answer_surface_label(
                answer_provenance_fields["answer_provenance"],
                app_gate_fields.get("app_gate_status") or "",
            )
            decision_path = str(result.get("decision_path") or "")
            generated = "yes" if (result.get("generation_time") or 0) > 0 else "no"
            evaluable_answer = (
                "yes"
                if generated == "yes"
                or answer_provenance_fields["answer_provenance"] == "reviewed_card_runtime"
                else "no"
            )
            selected_cards, selected_guide_ids = select_answer_cards(
                expected_ids=expected_ids,
                expected_family=expected_family,
                cited_ids=cited_ids,
                candidate_ids=candidate_ids,
                answer_cards=answer_cards,
            )
            answer_card_fields = answer_card_diagnostics(
                result,
                expected_ids=expected_ids,
                expected_family=expected_family,
                cited_ids=cited_ids,
                candidate_ids=candidate_ids,
                generated=evaluable_answer,
                answer_cards=answer_cards,
                selected_cards=selected_cards,
                selected_guide_ids=selected_guide_ids,
            )
            claim_support_fields = claim_support_diagnostics(
                result,
                expected_ids=expected_ids,
                cited_ids=cited_ids,
                generated=evaluable_answer,
                selected_cards=selected_cards,
            )
            shadow_card_answer_fields = shadow_card_answer_diagnostics(
                result,
                expected_ids=expected_ids,
                expected_family=expected_family,
                cited_ids=cited_ids,
                selected_cards=selected_cards,
                selected_guide_ids=selected_guide_ids,
            )
            app_acceptance_fields = app_acceptance_diagnostics(
                result,
                bucket=bucket,
                expected_ids=expected_ids,
                candidate_ids=candidate_ids,
                cited_ids=cited_ids,
                app_gate_fields=app_gate_fields,
                answer_card_fields=answer_card_fields,
                claim_support_fields=claim_support_fields,
                generated=evaluable_answer,
            )
            ranks = [f"{rank}:{guide_id}" for rank, guide_id in enumerate(candidate_ids, start=1)]
            rows.append(
                {
                    "artifact_path": str(path),
                    "artifact_name": path.name,
                    "prompt_index": prompt_index,
                    "prompt_id": result.get("prompt_id") or "",
                    "section": result.get("section") or "",
                    "prompt_text": result.get("question") or "",
                    "decision_path": result.get("decision_path") or "",
                    "decision_detail": result.get("decision_detail") or "",
                    **answer_provenance_fields,
                    **app_gate_fields,
                    **app_acceptance_fields,
                    "generated": generated,
                    "source_mode": result.get("source_mode") or "",
                    "cited_guide_ids": "|".join(cited_ids),
                    "top_retrieved_guide_ids": "|".join(candidate_ids) if candidate_ids else "unknown",
                    "top_retrieved_guide_ranks": "|".join(ranks) if ranks else "unknown",
                    "expected_guide_ids": "|".join(expected_ids) if expected_ids else "unknown",
                    "expected_guide_family": expected_family or "unknown",
                    "expected_hit_at_1": hit_at(expected_ids, candidate_ids, 1),
                    "expected_hit_at_3": hit_at(expected_ids, candidate_ids, 3),
                    "expected_hit_at_k": hit_at(expected_ids, candidate_ids, len(candidate_ids)),
                    "expected_cited": (
                        "yes"
                        if expected_ids and set(expected_ids) & set(cited_ids)
                        else "no"
                        if expected_ids
                        else "unknown"
                    ),
                    **answer_card_fields,
                    **claim_support_fields,
                    **shadow_card_answer_fields,
                    "cap_hit": bool(result.get("completion_cap_hit")),
                    "retry_count": result.get("completion_retry_count") or 0,
                    "completion_safety_trimmed": _truthy(
                        metadata_value(result, "completion_safety_trimmed")
                    ),
                    "error": result.get("error") or "",
                    "error_category": result.get("error_category") or "",
                    "suspected_failure_bucket": bucket,
                    "short_reason": reason,
                }
            )
    return rows


def summarize(rows: list[dict]) -> dict:
    by_bucket = Counter(row["suspected_failure_bucket"] for row in rows)
    by_artifact_bucket = defaultdict(Counter)
    deterministic = 0
    rag = 0
    generated = 0
    expected_rows = []
    gated_expected_rows = []
    distractors = Counter()
    app_gate_counts = Counter()
    app_acceptance_counts = Counter()
    answer_provenance_counts = Counter()
    answer_surface_label_counts = Counter()
    reviewed_card_backed = 0
    evidence_owner_counts = Counter()
    safety_surface_counts = Counter()
    ui_surface_counts = Counter()
    answer_card_counts = Counter()
    claim_support_counts = Counter()
    shadow_card_answer_counts = Counter()
    shadow_claim_support_counts = Counter()
    generated_shadow_missing_required = Counter()
    generated_shadow_card_gap_rows = []
    completion_safety_trimmed = 0

    for row in rows:
        artifact = row["artifact_name"]
        bucket = row["suspected_failure_bucket"]
        by_artifact_bucket[artifact][bucket] += 1
        if row["decision_path"] == "deterministic":
            deterministic += 1
        else:
            rag += 1
        if row["generated"] == "yes":
            generated += 1
        if _truthy(row.get("completion_safety_trimmed")):
            completion_safety_trimmed += 1
        provenance = row.get("answer_provenance") or ""
        if provenance:
            answer_provenance_counts[provenance] += 1
        surface_label = row.get("answer_surface_label") or ""
        if surface_label:
            answer_surface_label_counts[surface_label] += 1
        if _truthy(row.get("reviewed_card_backed")):
            reviewed_card_backed += 1
        gate_status = row.get("app_gate_status") or ""
        if gate_status:
            app_gate_counts[gate_status] += 1
        for key, counter in (
            ("app_acceptance_status", app_acceptance_counts),
            ("evidence_owner_status", evidence_owner_counts),
            ("safety_surface_status", safety_surface_counts),
            ("ui_surface_bucket", ui_surface_counts),
        ):
            value = row.get(key) or ""
            if value:
                counter[value] += 1
        answer_card_status = row.get("answer_card_status") or ""
        if answer_card_status:
            answer_card_counts[answer_card_status] += 1
        claim_support_status = row.get("claim_support_status") or ""
        if claim_support_status:
            claim_support_counts[claim_support_status] += 1
        shadow_card_answer_status = row.get("shadow_card_answer_status") or ""
        if shadow_card_answer_status:
            shadow_card_answer_counts[shadow_card_answer_status] += 1
        shadow_claim_support_status = row.get("shadow_claim_support_status") or ""
        if shadow_claim_support_status:
            shadow_claim_support_counts[shadow_claim_support_status] += 1
        if (
            row.get("generated") == "yes"
            and row.get("answer_card_status") in {"partial", "fail"}
            and row.get("shadow_card_answer_status") == "pass"
        ):
            missing_required = [
                phrase.strip()
                for phrase in str(row.get("answer_card_missing_required") or "").split("|")
                if phrase.strip()
            ]
            for phrase in missing_required:
                generated_shadow_missing_required[phrase] += 1
            generated_shadow_card_gap_rows.append(
                {
                    "artifact_name": row.get("artifact_name") or "",
                    "prompt_index": row.get("prompt_index") or "",
                    "answer_card_status": row.get("answer_card_status") or "",
                    "expected_guide_ids": row.get("expected_guide_ids") or "",
                    "missing_required": "|".join(missing_required),
                }
            )
        expected_ids = set(row["expected_guide_ids"].split("|")) if row["expected_guide_ids"] != "unknown" else set()
        if expected_ids:
            expected_rows.append(row)
            if gate_status:
                gated_expected_rows.append(row)
            for guide_id in (row["cited_guide_ids"] or "").split("|"):
                if guide_id and guide_id not in expected_ids:
                    distractors[guide_id] += 1
            if row["top_retrieved_guide_ids"] != "unknown":
                for guide_id in row["top_retrieved_guide_ids"].split("|"):
                    if guide_id and guide_id not in expected_ids:
                        distractors[guide_id] += 1

    def rate(column: str, source_rows: list[dict] | None = None) -> str:
        source_rows = expected_rows if source_rows is None else source_rows
        known = [row for row in source_rows if row[column] != "unknown"]
        if not known:
            return "unknown"
        hits = sum(1 for row in known if row[column] == "yes")
        return f"{hits}/{len(known)} ({hits / len(known):.1%})"

    return {
        "total_rows": len(rows),
        "by_bucket": dict(by_bucket),
        "by_artifact_bucket": {key: dict(value) for key, value in by_artifact_bucket.items()},
        "generation_workload": {
            "generated": generated,
            "not_generated": len(rows) - generated,
            "deterministic": deterministic,
            "non_deterministic": rag,
            "completion_safety_trimmed": completion_safety_trimmed,
        },
        "expected_guide_rows": len(expected_rows),
        "expected_hit_at_1": rate("expected_hit_at_1"),
        "expected_hit_at_3": rate("expected_hit_at_3"),
        "expected_hit_at_k": rate("expected_hit_at_k"),
        "expected_cited": rate("expected_cited"),
        "app_gate_counts": dict(app_gate_counts),
        "answer_provenance_counts": dict(answer_provenance_counts),
        "answer_surface_label_counts": dict(answer_surface_label_counts),
        "reviewed_card_backed_rows": reviewed_card_backed,
        "app_acceptance_counts": dict(app_acceptance_counts),
        "evidence_owner_counts": dict(evidence_owner_counts),
        "safety_surface_counts": dict(safety_surface_counts),
        "ui_surface_counts": dict(ui_surface_counts),
        "answer_card_counts": dict(answer_card_counts),
        "claim_support_counts": dict(claim_support_counts),
        "shadow_card_answer_counts": dict(shadow_card_answer_counts),
        "shadow_claim_support_counts": dict(shadow_claim_support_counts),
        "generated_shadow_card_gap_rows": len(generated_shadow_card_gap_rows),
        "top_generated_shadow_missing_required": generated_shadow_missing_required.most_common(20),
        "generated_shadow_card_gap_examples": generated_shadow_card_gap_rows[:20],
        "gated_expected_guide_rows": len(gated_expected_rows),
        "gated_expected_hit_at_1": rate("expected_hit_at_1", gated_expected_rows),
        "gated_expected_hit_at_3": rate("expected_hit_at_3", gated_expected_rows),
        "gated_expected_hit_at_k": rate("expected_hit_at_k", gated_expected_rows),
        "gated_expected_cited": rate("expected_cited", gated_expected_rows),
        "top_recurring_distractor_guide_ids": distractors.most_common(20),
    }


def write_csv(rows: list[dict], path: Path) -> None:
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=CSV_FIELDS, extrasaction="ignore")
        writer.writeheader()
        for row in rows:
            writer.writerow(row)


def write_json(rows: list[dict], summary: dict, path: Path) -> None:
    payload = {
        "generated_at": datetime.now().isoformat(timespec="seconds"),
        "summary": summary,
        "rows": rows,
    }
    path.write_text(json.dumps(payload, indent=2, ensure_ascii=False), encoding="utf-8")


def write_markdown(rows: list[dict], summary: dict, path: Path) -> None:
    lines = [
        "# RAG Bench Failure Diagnostics",
        "",
        f"- Generated at: `{datetime.now().isoformat(timespec='seconds')}`",
        f"- Prompt rows: `{summary['total_rows']}`",
        f"- Rows with expected guide metadata: `{summary['expected_guide_rows']}`",
        "",
        "## Bucket Counts",
        "",
    ]
    for bucket in BUCKETS:
        lines.append(f"- `{bucket}`: {summary['by_bucket'].get(bucket, 0)}")
    for bucket, count in sorted(summary["by_bucket"].items()):
        if bucket not in BUCKETS:
            lines.append(f"- `{bucket}`: {count}")

    workload = summary["generation_workload"]
    lines.extend(
        [
            "",
            "## Workload",
            "",
            f"- Generated prompts: `{workload['generated']}`",
            f"- Non-generated prompts: `{workload['not_generated']}`",
            f"- Deterministic prompts: `{workload['deterministic']}`",
            f"- Non-deterministic prompts: `{workload['non_deterministic']}`",
            f"- Safety-trimmed completions: `{workload['completion_safety_trimmed']}`",
            f"- Reviewed card-backed answers: `{summary.get('reviewed_card_backed_rows', 0)}`",
            "",
            "## Answer Provenance",
            "",
        ]
    )
    provenance_counts = summary.get("answer_provenance_counts") or {}
    if provenance_counts:
        lines.append("- provenance:")
        for provenance, count in sorted(provenance_counts.items()):
            lines.append(f"  - `{provenance}`: {count}")
    else:
        lines.append("- provenance: `none`")
    surface_counts = summary.get("answer_surface_label_counts") or {}
    if surface_counts:
        lines.append("- surface label:")
        for label, count in sorted(surface_counts.items()):
            lines.append(f"  - `{label}`: {count}")
    else:
        lines.append("- surface label: `none`")

    lines.extend(
        [
            "",
            "## Expected Guide Rates",
            "",
            f"- hit@1: `{summary['expected_hit_at_1']}`",
            f"- hit@3: `{summary['expected_hit_at_3']}`",
            f"- hit@k: `{summary['expected_hit_at_k']}`",
            f"- cited: `{summary['expected_cited']}`",
            "",
            "## App Gates",
            "",
            f"- Gated expected-guide rows: `{summary['gated_expected_guide_rows']}`",
            f"- gated hit@1: `{summary['gated_expected_hit_at_1']}`",
            f"- gated hit@3: `{summary['gated_expected_hit_at_3']}`",
            f"- gated hit@k: `{summary['gated_expected_hit_at_k']}`",
            f"- gated cited: `{summary['gated_expected_cited']}`",
        ]
    )
    if summary["app_gate_counts"]:
        for gate_status, count in sorted(summary["app_gate_counts"].items()):
            lines.append(f"- `{gate_status}`: {count}")
    else:
        lines.append("- `none`: 0")

    lines.extend(["", "## App Acceptance", ""])
    for title, key in (
        ("acceptance", "app_acceptance_counts"),
        ("evidence owner", "evidence_owner_counts"),
        ("safety surface", "safety_surface_counts"),
        ("ui surface", "ui_surface_counts"),
    ):
        counts = summary.get(key) or {}
        if counts:
            lines.append(f"- {title}:")
            for status, count in sorted(counts.items()):
                lines.append(f"  - `{status}`: {count}")
        else:
            lines.append(f"- {title}: `none`")

    lines.extend(["", "## Answer Cards", ""])
    if summary["answer_card_counts"]:
        for status, count in sorted(summary["answer_card_counts"].items()):
            lines.append(f"- `{status}`: {count}")
    else:
        lines.append("- `none`: 0")

    lines.extend(["", "## Claim Support", ""])
    if summary["claim_support_counts"]:
        for status, count in sorted(summary["claim_support_counts"].items()):
            lines.append(f"- `{status}`: {count}")
    else:
        lines.append("- `none`: 0")

    lines.extend(["", "## Shadow Card Composer", ""])
    shadow_card_counts = summary.get("shadow_card_answer_counts") or {}
    if shadow_card_counts:
        lines.append("- card answer:")
        for status, count in sorted(shadow_card_counts.items()):
            lines.append(f"  - `{status}`: {count}")
    else:
        lines.append("- card answer: `none`")
    shadow_claim_counts = summary.get("shadow_claim_support_counts") or {}
    if shadow_claim_counts:
        lines.append("- claim support:")
        for status, count in sorted(shadow_claim_counts.items()):
            lines.append(f"  - `{status}`: {count}")
    else:
        lines.append("- claim support: `none`")

    lines.extend(["", "## Generated vs Shadow Card Gaps", ""])
    lines.append(f"- Rows: `{summary.get('generated_shadow_card_gap_rows', 0)}`")
    top_missing_required = summary.get("top_generated_shadow_missing_required") or []
    if top_missing_required:
        lines.append(
            "- Top missing required actions where generated card status is partial/fail but shadow card answer passes:"
        )
        for phrase, count in top_missing_required:
            lines.append(f"  - `{phrase}`: {count}")
    else:
        lines.append("- Top missing required actions: `none`")

    gap_examples = summary.get("generated_shadow_card_gap_examples") or []
    if gap_examples:
        lines.extend(
            [
                "",
                "| artifact | # | generated card status | expected | missing required |",
                "| --- | ---: | --- | --- | --- |",
            ]
        )
        for example in gap_examples:
            lines.append(
                "| "
                f"`{example.get('artifact_name')}` | {example.get('prompt_index')} | "
                f"`{example.get('answer_card_status')}` | "
                f"`{example.get('expected_guide_ids')}` | "
                f"{escape_table(example.get('missing_required') or 'none')} |"
            )

    lines.extend(
        [
            "",
            "## Counts By Artifact",
            "",
            "| artifact | bucket | count |",
            "| --- | --- | ---: |",
        ]
    )
    for artifact in sorted(summary["by_artifact_bucket"]):
        for bucket, count in sorted(summary["by_artifact_bucket"][artifact].items()):
            lines.append(f"| `{artifact}` | `{bucket}` | {count} |")

    lines.extend(["", "## Recurring Distractor Guide IDs", ""])
    if summary["top_recurring_distractor_guide_ids"]:
        for guide_id, count in summary["top_recurring_distractor_guide_ids"]:
            lines.append(f"- `{guide_id}`: {count}")
    else:
        lines.append("- `unknown`: no expected guide metadata was available for distractor accounting")

    lines.extend(
        [
            "",
            "## Prompt Diagnostics",
            "",
            "| artifact | # | bucket | decision | provenance | gate | accept | cards | claims | shadow | expected | top/source guide ids | cited | reason |",
            "| --- | ---: | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |",
        ]
    )
    for row in rows:
        lines.append(
            "| "
            f"`{row['artifact_name']}` | {row['prompt_index']} | "
            f"`{row['suspected_failure_bucket']}` | `{row['decision_path']}` | "
            f"`{row.get('answer_provenance') or 'unknown'}` | "
            f"`{row.get('app_gate_status') or 'none'}` | "
            f"`{row.get('app_acceptance_status') or 'none'}` | "
            f"`{row.get('answer_card_status') or 'none'}` | "
            f"`{row.get('claim_support_status') or 'none'}:{row.get('claim_action_count') or 0}` | "
            f"`{row.get('shadow_card_answer_status') or 'none'}/"
            f"{row.get('shadow_claim_support_status') or 'none'}:"
            f"{row.get('shadow_claim_action_count') or 0}` | "
            f"`{row['expected_guide_ids']}` | `{row['top_retrieved_guide_ids']}` | "
            f"`{row['cited_guide_ids'] or 'none'}` | {escape_table(row['short_reason'])} |"
        )

    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def escape_table(value: str) -> str:
    return str(value).replace("|", "\\|").replace("\n", " ")


def default_output_dir() -> Path:
    stamp = datetime.now().strftime("%Y%m%d_%H%M")
    return Path("artifacts") / "bench" / f"rag_diagnostics_{stamp}"


def analyze(
    paths: list[Path],
    output_dir: Path,
    expectations_path: Path | None = None,
) -> tuple[list[dict], dict]:
    expectations = load_expectations(expectations_path) if expectations_path else {}
    rows = build_rows(paths, expectations=expectations)
    summary = summarize(rows)
    output_dir.mkdir(parents=True, exist_ok=True)
    write_markdown(rows, summary, output_dir / "report.md")
    write_csv(rows, output_dir / "diagnostics.csv")
    write_json(rows, summary, output_dir / "diagnostics.json")
    return rows, summary


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Analyze existing Senku bench JSON artifacts for RAG failure taxonomy."
    )
    parser.add_argument("artifacts", nargs="+", help="Bench JSON artifact paths")
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=None,
        help="Directory for report.md, diagnostics.csv, and diagnostics.json",
    )
    parser.add_argument(
        "--expectations",
        type=Path,
        default=DEFAULT_EXPECTATIONS_PATH if DEFAULT_EXPECTATIONS_PATH.exists() else None,
        help="Optional JSON/YAML prompt expectation manifest",
    )
    parser.add_argument(
        "--no-expectations",
        action="store_true",
        help="Disable default expectation manifest loading",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    paths = [Path(path) for path in args.artifacts]
    missing = [str(path) for path in paths if not path.exists()]
    if missing:
        raise SystemExit(f"Missing artifact(s): {', '.join(missing)}")
    output_dir = args.output_dir or default_output_dir()
    expectations_path = None if args.no_expectations else args.expectations
    rows, summary = analyze(paths, output_dir, expectations_path=expectations_path)
    print(f"Wrote {len(rows)} diagnostic rows to {output_dir}")
    print("Bucket counts:")
    for bucket, count in sorted(summary["by_bucket"].items()):
        print(f"  {bucket}: {count}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
