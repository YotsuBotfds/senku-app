#!/usr/bin/env python3
"""Run a minimal Android-style mobile-pack answer flow off-emulator."""

from __future__ import annotations

import argparse
import json
import sqlite3
import sys
import time
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Any

import requests

try:
    from mobile_headless_parity import (
        DEFAULT_CONTEXT_ITEMS,
        DEFAULT_CONTEXT_LIMIT,
        DEFAULT_HOST_MAX_TOKENS,
        DEFAULT_HOST_TIMEOUT_SECONDS,
        DEFAULT_OUTPUT_DIR,
        DEFAULT_SEARCH_LIMIT,
        HOST_TEMPERATURE,
        PackAssets,
        QueryTerms,
        SearchResultRow,
        build_fts_query,
        build_guide_section_key,
        clip,
        contains_any,
        lexical_keyword_score,
        normalize_answer_excerpt,
        resolve_path,
        safe_text,
        search_result_from_hit,
        support_score,
        timestamp_slug,
        utc_now_iso,
    )
except ModuleNotFoundError:
    from scripts.mobile_headless_parity import (
        DEFAULT_CONTEXT_ITEMS,
        DEFAULT_CONTEXT_LIMIT,
        DEFAULT_HOST_MAX_TOKENS,
        DEFAULT_HOST_TIMEOUT_SECONDS,
        DEFAULT_OUTPUT_DIR,
        DEFAULT_SEARCH_LIMIT,
        HOST_TEMPERATURE,
        PackAssets,
        QueryTerms,
        SearchResultRow,
        build_fts_query,
        build_guide_section_key,
        clip,
        contains_any,
        lexical_keyword_score,
        normalize_answer_excerpt,
        resolve_path,
        safe_text,
        search_result_from_hit,
        support_score,
        timestamp_slug,
        utc_now_iso,
    )

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

SOAP_PROCESS_MARKERS = frozenset(
    {
        "making soap",
        "soap making",
        "soap making - cold process",
        "cold process soap",
        "hot process soap",
        "making lye from wood ash",
        "wood ash lye",
        "lye water",
        "saponification",
        "render fat",
        "rendering fats",
        "tallow",
        "lard",
        "trace",
        "cure",
    }
)
SOAP_GENERIC_CHEMISTRY_MARKERS = frozenset(
    {
        "acids and bases",
        "chemical safety fundamentals",
        "cleaning product chemistry",
        "storage compatibility",
        "industrial applications",
        "atoms and molecules",
        "chemical reactions",
    }
)
HOUSE_UNDERGROUND_MARKERS = frozenset(
    {
        "earth shelter",
        "earth sheltered",
        "earth sheltering",
        "underground shelter",
        "bunker",
        "berm",
        "cut and cover",
    }
)
HOUSE_SITE_SEASONAL_PROMPT_MARKERS = frozenset(
    {"winter", "summer", "shade", "sun", "solar", "microclimate", "seasonal", "orientation"}
)


def contains_any_marker(text: str, markers: set[str] | frozenset[str]) -> bool:
    normalized = safe_text(text).strip().lower()
    return any(marker in normalized for marker in markers)


def has_strong_soapmaking_guide_signal(row: SearchResultRow) -> bool:
    normalized_title = safe_text(row.title).strip().lower()
    normalized_section = safe_text(row.section_heading).strip().lower()
    combined = normalize_answer_excerpt(
        f"{safe_text(row.title)} {safe_text(row.section_heading)} {safe_text(row.snippet)} {safe_text(row.body)}"
    ).lower()
    process_signal = contains_any_marker(combined, SOAP_PROCESS_MARKERS)
    if not process_signal:
        return False
    practical_heading = contains_any_marker(normalized_title, SOAP_PROCESS_MARKERS) or contains_any_marker(
        normalized_section, SOAP_PROCESS_MARKERS
    )
    if not practical_heading:
        return False
    return not contains_any_marker(normalized_title, SOAP_GENERIC_CHEMISTRY_MARKERS) and not contains_any_marker(
        normalized_section, SOAP_GENERIC_CHEMISTRY_MARKERS
    )


def has_direct_anchor_signal(query_terms: QueryTerms, row: SearchResultRow) -> bool:
    if query_terms.metadata_profile.section_heading_bonus(row.section_heading) > 0:
        return True
    if not query_terms.metadata_profile.has_explicit_topic_focus():
        return False
    row_topics = set(filter(None, row.topic_tags.split(",")))
    explicit_topics = set(query_terms.metadata_profile.explicit_topic_tags)
    if not row_topics.intersection(explicit_topics):
        return False
    if query_terms.metadata_profile.preferred_structure_type == "soapmaking":
        return has_strong_soapmaking_guide_signal(row)
    return lexical_keyword_score(
        query_terms,
        title=row.title,
        section=row.section_heading,
        category=row.category,
        tags="",
        description="",
        document="",
    ) > 0


def should_keep_specialized_direct_signal_route_result(query_terms: QueryTerms, row: SearchResultRow) -> bool:
    if query_terms.metadata_profile.preferred_structure_type != "soapmaking":
        return True
    if not query_terms.metadata_profile.has_explicit_topic_focus():
        return True
    return has_direct_anchor_signal(query_terms, row)


def select_specialized_anchor(query_terms: QueryTerms, ranked_results: list[SearchResultRow]) -> SearchResultRow | None:
    if not query_terms.metadata_profile.has_explicit_topic_focus():
        return None
    if query_terms.metadata_profile.preferred_structure_type != "soapmaking":
        return None

    best_row: SearchResultRow | None = None
    best_score = -10**9
    for index, row in enumerate(ranked_results):
        if not has_direct_anchor_signal(query_terms, row):
            continue
        score = row.support_score if row.support_score is not None else support_score(query_terms, row)
        score += max(0, 12 - index)
        if row.retrieval_mode == "route-focus":
            score += 10
        elif row.retrieval_mode == "guide-focus":
            score += 8 if row.section_heading.strip() else 4
        elif row.retrieval_mode == "hybrid":
            score += 4
        elif row.retrieval_mode == "lexical":
            score += 2
        if has_strong_soapmaking_guide_signal(row):
            score += 18
        if row.content_role == "subsystem":
            score += 6
        elif row.content_role == "safety":
            score -= 4
        if safe_text(row.structure_type).strip().lower() != "soapmaking":
            score -= 8
        normalized_title = safe_text(row.title).strip().lower()
        normalized_section = safe_text(row.section_heading).strip().lower()
        if contains_any_marker(normalized_title, SOAP_GENERIC_CHEMISTRY_MARKERS) or contains_any_marker(
            normalized_section, SOAP_GENERIC_CHEMISTRY_MARKERS
        ):
            score -= 22
        if contains_any_marker(
            normalize_answer_excerpt(
                f"{safe_text(row.title)} {safe_text(row.section_heading)} {safe_text(row.snippet)} {safe_text(row.body)}"
            ).lower(),
            SOAP_PROCESS_MARKERS,
        ):
            score += 6
        if score > best_score:
            best_score = score
            best_row = row
    return best_row


def build_route_hints(query_terms: QueryTerms) -> list[str]:
    hints = [token for token in query_terms.route_profile.expansion_tokens]
    hints.extend(topic.replace("_", " ") for topic in query_terms.metadata_profile.preferred_topic_tags)
    structure = query_terms.metadata_profile.preferred_structure_type.replace("_", " ").strip()
    if structure:
        hints.append(structure)
    if query_terms.route_profile.kind == "house_build":
        hints.extend(["site selection", "site prep", "foundation", "footings", "drainage", "weatherproofing"])
    elif query_terms.route_profile.kind == "water_distribution":
        hints.extend(["gravity fed", "storage tank", "water tower", "distribution layout"])
    elif query_terms.route_profile.kind == "clay_oven":
        hints.extend(["clay oven", "cob oven", "bread oven", "brick oven", "site selection", "foundation", "chimney draft", "curing schedule", "thermal mass", "refractory"])
    seen: set[str] = set()
    ordered: list[str] = []
    for hint in hints:
        normalized = safe_text(hint).strip().lower()
        if normalized and normalized not in seen:
            seen.add(normalized)
            ordered.append(normalized)
    return ordered


def route_scoring_query_terms(query_terms: QueryTerms, hints: list[str]) -> QueryTerms:
    if (
        query_terms.route_profile.kind == "house_build"
        and query_terms.metadata_profile.preferred_structure_type == "cabin_house"
        and query_terms.metadata_profile.has_explicit_topic("site_selection")
    ):
        return query_terms
    return QueryTerms.from_query(" ".join([query_terms.query_lower, *hints]))


@dataclass(frozen=True)
class HeadlessRouteOrderSpec:
    order_sql: str
    args: tuple[str, ...]
    label: str


def headless_route_order_spec(query_terms: QueryTerms) -> HeadlessRouteOrderSpec:
    metadata_profile = query_terms.metadata_profile
    if (
        query_terms.route_profile.kind == "house_build"
        and metadata_profile.preferred_structure_type == "cabin_house"
        and metadata_profile.has_explicit_topic("site_selection")
    ):
        query_lower = safe_text(query_terms.query_lower)
        seasonal_exposure_focus = any(
            marker in query_lower for marker in ("winter", "summer", "shade", "microclimate", "orientation")
        )
        if seasonal_exposure_focus:
            return HeadlessRouteOrderSpec(
                order_sql=(
                    " ORDER BY CASE "
                    "WHEN lower(section_heading) LIKE ? THEN 0 "
                    "WHEN lower(section_heading) LIKE ? THEN 0 "
                    "WHEN lower(section_heading) LIKE ? THEN 0 "
                    "WHEN lower(section_heading) LIKE ? THEN 0 "
                    "WHEN lower(section_heading) LIKE ? THEN 1 "
                    "WHEN lower(section_heading) LIKE ? THEN 1 "
                    "WHEN lower(guide_title) LIKE ? THEN 1 "
                    "WHEN lower(section_heading) LIKE ? THEN 2 "
                    "WHEN lower(section_heading) LIKE ? THEN 3 "
                    "WHEN lower(section_heading) LIKE ? THEN 4 "
                    "ELSE 9 END, rowid "
                ),
                args=(
                    "%wind exposure%",
                    "%microclimate%",
                    "%seasonal%",
                    "%sun exposure%",
                    "%site assessment%",
                    "%terrain analysis%",
                    "%site selection%",
                    "%natural hazards%",
                    "%foundation%",
                    "%drainage%",
                ),
                label="site_selection_microclimate_priority",
            )
        return HeadlessRouteOrderSpec(
            order_sql=(
                " ORDER BY CASE "
                "WHEN lower(section_heading) LIKE ? THEN 0 "
                "WHEN lower(section_heading) LIKE ? THEN 0 "
                "WHEN lower(section_heading) LIKE ? THEN 0 "
                "WHEN lower(section_heading) LIKE ? THEN 0 "
                "WHEN lower(section_heading) LIKE ? THEN 0 "
                "WHEN lower(guide_title) LIKE ? THEN 1 "
                "WHEN lower(section_heading) LIKE ? THEN 1 "
                "WHEN lower(section_heading) LIKE ? THEN 2 "
                "WHEN lower(section_heading) LIKE ? THEN 3 "
                "ELSE 9 END, rowid "
            ),
            args=(
                "%site assessment%",
                "%terrain analysis%",
                "%wind exposure%",
                "%water proximity%",
                "%site assessment checklist%",
                "%site selection%",
                "%natural hazards%",
                "%foundation%",
                "%drainage%",
            ),
            label="site_selection_priority",
        )
    return HeadlessRouteOrderSpec(order_sql=" ORDER BY rowid ", args=(), label="rowid")


def should_keep_broad_house_route_row(query_terms: QueryTerms, row: SearchResultRow, section_heading_score: int) -> bool:
    metadata_profile = query_terms.metadata_profile
    if metadata_profile.preferred_structure_type != "cabin_house":
        return True
    combined = normalize_answer_excerpt(f"{safe_text(row.title)} {safe_text(row.section_heading)}").lower()
    query_lower = safe_text(query_terms.query_lower)
    allow_earth_shelter = contains_any(query_lower, HOUSE_UNDERGROUND_MARKERS)
    if not allow_earth_shelter and contains_any(combined, HOUSE_UNDERGROUND_MARKERS):
        return False
    if metadata_profile.has_explicit_topic_focus():
        return section_heading_score > 0
    if section_heading_score < 0:
        return False
    normalized_category = safe_text(row.category).strip().lower()
    normalized_structure = safe_text(row.structure_type).strip().lower()
    overlap = metadata_profile.preferred_topic_overlap_count(row.topic_tags)
    if (
        section_heading_score == 0
        and normalized_category != "building"
        and normalized_structure != "cabin_house"
        and overlap < 2
    ):
        return False
    return True


def prompt_context_limit(question: str, query_terms: QueryTerms, context_results: list[SearchResultRow]) -> int:
    capped = min(max(DEFAULT_CONTEXT_ITEMS, query_terms.route_profile.preferred_context_items), len(context_results))
    normalized = safe_text(question).strip().lower()
    metadata_profile = query_terms.metadata_profile
    seasonal_house_site = (
        metadata_profile.preferred_structure_type == "cabin_house"
        and metadata_profile.has_explicit_topic("site_selection")
        and contains_any_marker(normalized, HOUSE_SITE_SEASONAL_PROMPT_MARKERS)
    )
    if seasonal_house_site:
        capped = min(max(capped, 4), len(context_results))
    return capped


def last_sentence_break(text: str) -> int:
    best = -1
    for marker in (".", "!", "?", ";"):
        best = max(best, text.rfind(marker))
    return best + 1 if best >= 0 else -1


def prompt_clip(text: str, limit: int) -> str:
    compact = " ".join(safe_text(text).replace("\n", " ").split()).strip()
    if len(compact) <= limit:
        return compact
    hard_limit = max(0, limit - 3)
    prefix = compact[:hard_limit].strip()
    sentence_break = last_sentence_break(prefix)
    if sentence_break >= max(48, hard_limit // 2):
        return prefix[:sentence_break].strip()
    return prefix + "..."


def source_label(row: SearchResultRow) -> str:
    if row.guide_id.strip() and row.title.strip():
        return f"[{row.guide_id.strip()}] {row.title.strip()}"
    if row.guide_id.strip():
        return f"[{row.guide_id.strip()}]"
    return row.title.strip() or "Unknown source"


def normalized_metadata_value(value: str) -> str:
    return " ".join(safe_text(value).replace("_", " ").split()).strip()


def normalized_topic_tags(topic_tags: str) -> str:
    parts: list[str] = []
    for raw_part in safe_text(topic_tags).split(","):
        normalized = normalized_metadata_value(raw_part)
        if not normalized or normalized in parts:
            continue
        parts.append(normalized)
        if len(parts) >= 3:
            break
    return ", ".join(parts)


def context_metadata_line(row: SearchResultRow, index: int) -> str:
    parts = ["Metadata: anchor note" if index == 0 else "Metadata: support note"]
    for label, value in (
        ("category", normalized_metadata_value(row.category)),
        ("role", normalized_metadata_value(row.content_role)),
        ("horizon", normalized_metadata_value(row.time_horizon)),
        ("structure", normalized_metadata_value(row.structure_type)),
        ("topics", normalized_topic_tags(row.topic_tags)),
    ):
        if value:
            parts.append(f"{label}={value}")
    return " | ".join(parts)


def answer_format_spec(query_terms: QueryTerms) -> tuple[list[str], str, str]:
    if query_terms.route_profile.prefers_summary_key_points_format():
        return (
            ["Summary", "Key points", "Risks or limits"],
            "Summary, Key points, Risks or limits",
            "Keep Summary to 1-2 sentences, Key points to at most 4 short numbered lines, and Risks or limits to 1-2 sentences.",
        )
    return (
        ["Short answer", "Steps", "Limits or safety"],
        "Short answer, Steps, Limits or safety",
        "Keep Short answer to one sentence, Steps to at most 4 numbered lines, and Limits or safety to 1-2 sentences.",
    )


def build_system_prompt(question: str) -> str:
    query_terms = QueryTerms.from_query(question)
    lines = [
        "You are Senku, an offline field-guide assistant.",
        "Use only the retrieved notes below.",
        "If the notes clearly cover the question, answer directly from them.",
        "If support is partial, say what is uncertain instead of pretending.",
        "Prefer practical steps, concrete materials, and tradeoffs.",
        "Avoid ellipses and avoid copying markdown symbols into the answer.",
        "Keep the tone clear and useful on a phone screen.",
        "Cite guide IDs in square brackets like [GD-572].",
        "When retrieved notes include metadata lines, treat anchor notes and matching structure/topic tags as stronger evidence than generic support notes.",
    ]
    lines.extend(query_terms.route_profile.prompt_guidance_lines())
    if query_terms.route_profile.is_seasonal_house_site_selection_prompt(question):
        lines.append("For seasonal site-selection questions, answer the siting tradeoff first.")
        lines.append("If the retrieved notes support it, talk directly about winter solar gain, summer shade, wind exposure, and orientation.")
    return "\n".join(lines)


def build_prompt(question: str, context_results: list[SearchResultRow], session_context: str = "") -> str:
    query_terms = QueryTerms.from_query(question)
    answer_labels, label_instruction, constraint_line = answer_format_spec(query_terms)
    lines = [
        "You are Senku, an offline field-guide assistant.",
        "Use only the retrieved notes below.",
        "If the notes clearly cover the question, answer directly from them.",
        "If support is partial, say what is uncertain instead of pretending.",
        "Prefer practical steps, concrete materials, and tradeoffs.",
        "When the notes describe a process, use short numbered steps with one step per line.",
        "Avoid ellipses and avoid copying markdown symbols into the answer.",
        "Keep the tone clear and useful on a phone screen.",
        "Cite guide IDs in square brackets like [GD-572].",
        f"Return only these labels once: {label_instruction}.",
        constraint_line,
        "Do not repeat the prompt, do not include an extra Answer heading, and do not restate the notes verbatim.",
    ]
    if safe_text(session_context).strip():
        lines.extend(
            [
                "Session context from earlier turns is available below.",
                "Use it only to resolve vague follow-up references like it, that, next, or what about.",
                "If session context conflicts with the retrieved notes, trust the retrieved notes.",
                "Answer the newest follow-up question directly instead of repeating the whole earlier plan.",
                "Only bring forward earlier steps when they are needed to make the follow-up answer clear.",
                "Keep the answer scoped to the same resource, system, or build step shown in the retrieved notes.",
                "Do not broaden a follow-up into generic advice for other materials or supplies unless the retrieved notes explicitly do that.",
                f"Session context: {safe_text(session_context).strip()}",
            ]
        )
    lines.extend(query_terms.route_profile.prompt_guidance_lines())
    if query_terms.route_profile.is_seasonal_house_site_selection_prompt(question):
        lines.append("For seasonal site-selection questions, answer the specific siting tradeoff first instead of falling back to generic drainage or foundation advice.")
        lines.append("If the retrieved notes support it, talk directly about winter solar gain, summer shade, wind exposure, and orientation.")
    lines.append(f"Question: {question.strip()}")
    lines.append("")
    lines.append("Retrieved notes:")
    capped = prompt_context_limit(question, query_terms, context_results)
    for index, row in enumerate(context_results[:capped], start=1):
        label = source_label(row)
        if row.section_heading.strip():
            label += f" / {row.section_heading.strip()}"
        lines.append(f"[{index}] {label}")
        lines.append(context_metadata_line(row, index - 1))
        lines.append("Note: " + prompt_clip(row.body or row.snippet, query_terms.route_profile.prompt_excerpt_chars))
    if capped == 0:
        lines.append("No retrieved notes were available.")
    lines.append("")
    lines.append("Answer format:")
    for label in answer_labels:
        lines.append(f"{label}:")
    lines.extend(["", "Answer:"])
    return "\n".join(lines)


def request_host_answer(
    host_url: str,
    host_model: str,
    prompt: str,
    timeout_seconds: int,
    max_tokens: int,
    *,
    system_prompt: str = "",
) -> dict[str, Any]:
    endpoint = host_url.rstrip("/") + "/chat/completions"
    messages: list[dict[str, str]] = []
    if safe_text(system_prompt).strip():
        messages.append({"role": "system", "content": system_prompt})
    messages.append({"role": "user", "content": prompt})
    response = requests.post(
        endpoint,
        json={
            "model": host_model,
            "temperature": HOST_TEMPERATURE,
            "stream": False,
            "max_tokens": max_tokens,
            "messages": messages,
        },
        timeout=timeout_seconds,
    )
    response.raise_for_status()
    payload = response.json()
    content = payload.get("choices", [{}])[0].get("message", {}).get("content", "")
    return {
        "answer": normalize_answer_excerpt(safe_text(content).strip()),
        "backend": safe_text(payload.get("senku_backend", "host")).strip() or "host",
        "elapsed_seconds": payload.get("senku_elapsed_seconds", 0.0),
    }


def structure_alignment_bonus(query_terms: QueryTerms, row: SearchResultRow) -> int:
    preferred = safe_text(query_terms.metadata_profile.preferred_structure_type).strip().lower()
    actual = safe_text(row.structure_type).strip().lower()
    if not preferred or not actual or actual == "general":
        return 0
    if actual == preferred:
        return 12
    preferred_topics = set(query_terms.metadata_profile.preferred_topic_tags)
    actual_topics = set(filter(None, row.topic_tags.split(",")))
    if preferred_topics and preferred_topics.intersection(actual_topics):
        return 3
    return -12


def broad_house_priority_bonus(query_terms: QueryTerms, row: SearchResultRow) -> int:
    if not query_terms.route_profile.is_starter_build_project() or query_terms.metadata_profile.has_explicit_topic_focus():
        return 0
    title = safe_text(row.title).strip().lower()
    section = safe_text(row.section_heading).strip().lower()
    combined = f"{title} {section}"
    score = 0
    if any(term in combined for term in ("foundation", "footing", "footings", "frost line", "frost heave")):
        score += 28
    if any(term in combined for term in ("site selection", "site prep", "drainage")):
        score += 18
    if any(term in combined for term in ("stone foundation", "crawlspace", "slab-on-grade", "pier & post")):
        score += 12
    if any(term in combined for term in ("insulation", "weatherstripping", "air sealing", "thermal efficiency")):
        score -= 28
    if any(term in combined for term in ("roofing", "roof insulation", "wall construction", "door", "window")):
        score -= 10
    if any(term in combined for term in ("outbuildings", "calculator", "climate zone")):
        score -= 18
    return score


class HeadlessRunner:
    def __init__(self, pack_assets: PackAssets, *, force_no_fts: bool = False) -> None:
        self.pack_assets = pack_assets
        self.connection = sqlite3.connect(str(pack_assets.sqlite_path))
        self.connection.row_factory = sqlite3.Row
        self.force_no_fts = force_no_fts
        self.fts_table, self.fts_supports_bm25 = self._detect_fts()
        self.fts_available = bool(self.fts_table) and not force_no_fts
        if force_no_fts:
            self.fts_table = ""
            self.fts_supports_bm25 = False

    def close(self) -> None:
        self.connection.close()

    def _detect_fts(self) -> tuple[str, bool]:
        for table_name, supports_bm25 in (("lexical_chunks_fts", True), ("lexical_chunks_fts4", False)):
            try:
                self.connection.execute(f"SELECT count(*) FROM {table_name}").fetchone()
                return table_name, supports_bm25
            except sqlite3.Error:
                continue
        return "", False

    def search(self, query: str, limit: int) -> list[SearchResultRow]:
        query_terms = QueryTerms.from_query(query)
        route_results = self._route_results(query_terms, limit) if query_terms.route_profile.route_focused else []
        lexical_results = self._lexical_results(query_terms, limit * 6)
        merged: list[SearchResultRow] = []
        seen: set[str] = set()
        for row in [*route_results, *lexical_results]:
            key = row.source_key()
            if key in seen:
                continue
            seen.add(key)
            merged.append(row)
            if len(merged) >= limit:
                break
        return merged

    def _route_results(self, query_terms: QueryTerms, limit: int) -> list[SearchResultRow]:
        categories = list(query_terms.route_profile.preferred_categories)
        if not categories:
            return []
        placeholders = ",".join("?" for _ in categories)
        hints = build_route_hints(query_terms)
        clauses: list[str] = []
        args: list[str] = list(categories)
        for hint in hints[:12]:
            like = f"%{hint}%"
            clauses.append("(guide_title LIKE ? OR section_heading LIKE ? OR topic_tags LIKE ? OR description LIKE ? OR document LIKE ?)")
            args.extend([like, like, like, like, like])
        if not clauses:
            return []
        route_terms = route_scoring_query_terms(query_terms, hints)
        order_spec = headless_route_order_spec(query_terms)
        sql = (
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, description, "
            "content_role, time_horizon, structure_type, topic_tags "
            f"FROM chunks WHERE category IN ({placeholders}) AND ({' OR '.join(clauses)})"
            f"{order_spec.order_sql}LIMIT ?"
        )
        args.extend(order_spec.args)
        args.append(str(600 if query_terms.route_profile.is_starter_build_project() else 240))
        best: dict[str, tuple[int, SearchResultRow]] = {}
        for hit in self.connection.execute(sql, args):
            row = self._row_from_chunk(hit, "route-focus")
            if not should_keep_specialized_direct_signal_route_result(query_terms, row):
                continue
            section_bonus = query_terms.metadata_profile.section_heading_bonus(row.section_heading)
            if not should_keep_broad_house_route_row(query_terms, row, section_bonus):
                continue
            score = lexical_keyword_score(route_terms, title=row.title, section=row.section_heading, category=row.category, tags=row.topic_tags, description=safe_text(hit["description"]), document=row.body)
            score += query_terms.metadata_profile.metadata_bonus(row.category, row.content_role, row.time_horizon, row.structure_type, row.topic_tags)
            score += section_bonus
            score += structure_alignment_bonus(query_terms, row)
            score += broad_house_priority_bonus(query_terms, row)
            if query_terms.route_profile.is_starter_build_project():
                score += max(-20, min(18, section_bonus))
            if score <= 0:
                continue
            row.support_score = score
            key = row.source_key()
            if key not in best or score > best[key][0]:
                best[key] = (score, row)
        ordered = sorted(best.values(), key=lambda item: (-item[0], item[1].guide_id, item[1].section_heading))
        return [row for _, row in ordered[:limit]]

    def _lexical_results(self, query_terms: QueryTerms, limit: int) -> list[SearchResultRow]:
        clauses: list[str] = []
        args: list[str] = []
        if self.fts_available:
            fts_query = build_fts_query(query_terms)
            if fts_query:
                order_clause = f" ORDER BY bm25({self.fts_table})" if self.fts_supports_bm25 else ""
                sql = (
                    "SELECT c.chunk_id, c.vector_row_id, c.guide_title, c.guide_id, c.section_heading, c.category, c.document, "
                    "c.content_role, c.time_horizon, c.structure_type, c.topic_tags "
                    f"FROM {self.fts_table} f JOIN chunks c ON c.chunk_id = f.chunk_id "
                    f"WHERE {self.fts_table} MATCH ?{order_clause} LIMIT ?"
                )
                rows = [search_result_from_hit(dict(hit)) for hit in self.connection.execute(sql, (fts_query, limit))]
                if rows:
                    return rows
        for token in query_terms.keyword_tokens()[:12]:
            like = f"%{token}%"
            clauses.append("(guide_title LIKE ? OR section_heading LIKE ? OR topic_tags LIKE ? OR description LIKE ? OR document LIKE ?)")
            args.extend([like, like, like, like, like])
        if not clauses:
            return []
        sql = (
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, description, "
            "content_role, time_horizon, structure_type, topic_tags "
            f"FROM chunks WHERE {' OR '.join(clauses)} LIMIT ?"
        )
        args.append(str(limit))
        scored: list[tuple[int, SearchResultRow]] = []
        for hit in self.connection.execute(sql, args):
            row = self._row_from_chunk(hit, "lexical")
            score = lexical_keyword_score(query_terms, title=row.title, section=row.section_heading, category=row.category, tags=row.topic_tags, description=safe_text(hit["description"]), document=row.body)
            score += query_terms.metadata_profile.metadata_bonus(row.category, row.content_role, row.time_horizon, row.structure_type, row.topic_tags)
            score += query_terms.metadata_profile.section_heading_bonus(row.section_heading)
            score += structure_alignment_bonus(query_terms, row)
            if score > 0:
                row.support_score = score
                scored.append((score, row))
        scored.sort(key=lambda item: (-item[0], item[1].guide_id, item[1].section_heading))
        return [row for _, row in scored[: max(limit // 3, DEFAULT_SEARCH_LIMIT)]]

    def build_context(self, query: str, ranked_results: list[SearchResultRow], limit: int) -> list[SearchResultRow]:
        if not ranked_results:
            return []
        query_terms = QueryTerms.from_query(query)
        anchor = self._select_anchor(query_terms, ranked_results)
        rows = self.connection.execute(
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, description, content_role, time_horizon, structure_type, topic_tags "
            "FROM chunks WHERE guide_id = ?",
            (anchor.guide_id,),
        ).fetchall()
        guide_sections_by_key: dict[str, tuple[int, SearchResultRow]] = {}
        for hit in rows:
            row = self._row_from_chunk(hit, "guide-focus")
            score = lexical_keyword_score(query_terms, title=row.title, section=row.section_heading, category=row.category, tags=row.topic_tags, description=safe_text(hit["description"]), document=row.body)
            score += query_terms.metadata_profile.metadata_bonus(row.category, row.content_role, row.time_horizon, row.structure_type, row.topic_tags)
            score += query_terms.metadata_profile.section_heading_bonus(row.section_heading)
            score += structure_alignment_bonus(query_terms, row)
            score += broad_house_priority_bonus(query_terms, row)
            if row.section_heading.strip().lower() == anchor.section_heading.strip().lower():
                score += 40
            if query_terms.route_profile.is_starter_build_project() and not query_terms.metadata_profile.has_explicit_topic_focus():
                section_bonus = query_terms.metadata_profile.section_heading_bonus(row.section_heading)
                if section_bonus > 0:
                    score += 8 + min(10, section_bonus)
                elif section_bonus < 0:
                    score += max(-20, section_bonus)
            row.support_score = score
            key = build_guide_section_key(row.guide_id, row.title, row.section_heading)
            existing = guide_sections_by_key.get(key)
            if existing is None or score > existing[0]:
                guide_sections_by_key[key] = (score, row)
        guide_sections = sorted(guide_sections_by_key.values(), key=lambda item: (-item[0], item[1].section_heading))
        context = [row for _, row in guide_sections[: min(limit, max(DEFAULT_CONTEXT_ITEMS, query_terms.route_profile.preferred_context_items))]]
        seen = {build_guide_section_key(row.guide_id, row.title, row.section_heading) for row in context}
        for row in ranked_results:
            key = build_guide_section_key(row.guide_id, row.title, row.section_heading)
            if key in seen or row.guide_id == anchor.guide_id:
                continue
            context.append(row)
            seen.add(key)
            if len(context) >= limit:
                break
        return context

    def _select_anchor(self, query_terms: QueryTerms, ranked_results: list[SearchResultRow]) -> SearchResultRow:
        specialized_anchor = select_specialized_anchor(query_terms, ranked_results)
        if specialized_anchor is not None:
            return specialized_anchor
        route_candidates = [(index, row) for index, row in enumerate(ranked_results) if row.retrieval_mode == "route-focus"]
        if query_terms.route_profile.kind == "water_storage" and route_candidates:
            return route_candidates[0][1]
        if not route_candidates:
            return max(
                ranked_results,
                key=lambda row: (
                    support_score(query_terms, row),
                    query_terms.metadata_profile.section_heading_bonus(row.section_heading),
                    -len(row.section_heading),
                ),
            )

        best_score = -10**9
        best_row = route_candidates[0][1]
        for index, row in route_candidates:
            score = (row.support_score if row.support_score is not None else support_score(query_terms, row)) + max(0, 12 - index)
            score += broad_house_priority_bonus(query_terms, row)
            if query_terms.metadata_profile.has_explicit_topic_focus():
                score += query_terms.metadata_profile.section_heading_bonus(row.section_heading) * 2
            if query_terms.route_profile.is_starter_build_project() and not query_terms.metadata_profile.has_explicit_topic_focus():
                section_bonus = query_terms.metadata_profile.section_heading_bonus(row.section_heading)
                if section_bonus > 0:
                    score += 8 + min(10, section_bonus)
                elif section_bonus < 0:
                    score += max(-20, section_bonus)
            if score > best_score:
                best_score = score
                best_row = row
        return best_row

    def _row_from_chunk(self, hit: sqlite3.Row, retrieval_mode: str) -> SearchResultRow:
        return SearchResultRow(
            title=safe_text(hit["guide_title"]),
            subtitle=f"{safe_text(hit['guide_id'])} | {safe_text(hit['category'])} | {safe_text(hit['section_heading'])} | {retrieval_mode}",
            snippet=clip(safe_text(hit["document"]), 220),
            body=safe_text(hit["document"]),
            guide_id=safe_text(hit["guide_id"]),
            section_heading=safe_text(hit["section_heading"]),
            category=safe_text(hit["category"]),
            retrieval_mode=retrieval_mode,
            content_role=safe_text(hit["content_role"]),
            time_horizon=safe_text(hit["time_horizon"]),
            structure_type=safe_text(hit["structure_type"]),
            topic_tags=safe_text(hit["topic_tags"]),
            chunk_id=safe_text(hit["chunk_id"]),
            vector_row_id=int(hit["vector_row_id"]),
        )


def load_cases(query: str | None, queries_file: str | None) -> list[dict[str, Any]]:
    if query:
        return [{"case_id": "inline_query", "query": safe_text(query).lstrip("\ufeff")}]
    if not queries_file:
        raise SystemExit("Provide either --query or --queries-file")
    path = resolve_path(queries_file)
    if path.suffix.lower() == ".jsonl":
        cases = []
        for index, line in enumerate(path.read_text(encoding="utf-8-sig").splitlines(), start=1):
            if not line.strip():
                continue
            payload = json.loads(line)
            query_text = safe_text(payload.get("query") or payload.get("initial_query") or payload.get("question") or payload.get("prompt")).lstrip("\ufeff").strip()
            if query_text:
                cases.append({"case_id": safe_text(payload.get("case_id") or payload.get("id") or f"{path.stem}_{index}"), "query": query_text, "source": payload})
        return cases
    cases = []
    for index, line in enumerate(path.read_text(encoding="utf-8-sig").splitlines(), start=1):
        stripped = line.lstrip("\ufeff").strip()
        if stripped and not stripped.startswith("#"):
            cases.append({"case_id": f"{path.stem}_{index}", "query": stripped})
    return cases


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--query")
    parser.add_argument("--queries-file")
    parser.add_argument("--pack-manifest")
    parser.add_argument("--pack-dir")
    parser.add_argument("--output-dir")
    parser.add_argument("--search-limit", type=int, default=DEFAULT_SEARCH_LIMIT)
    parser.add_argument("--context-limit", type=int, default=DEFAULT_CONTEXT_LIMIT)
    parser.add_argument("--max-queries", type=int, default=0)
    parser.add_argument("--force-no-fts", action="store_true")
    parser.add_argument("--include-host-answer", action="store_true")
    parser.add_argument("--host-url", default="http://127.0.0.1:1235/v1")
    parser.add_argument("--host-model", default="gemma-4-e2b-it-litert")
    parser.add_argument("--host-timeout-seconds", type=int, default=DEFAULT_HOST_TIMEOUT_SECONDS)
    parser.add_argument("--max-tokens", type=int, default=DEFAULT_HOST_MAX_TOKENS)
    args = parser.parse_args()

    pack_assets = PackAssets.load(pack_manifest=args.pack_manifest, pack_dir=args.pack_dir)
    output_dir = resolve_path(args.output_dir, DEFAULT_OUTPUT_DIR)
    output_dir.mkdir(parents=True, exist_ok=True)
    cases = load_cases(args.query, args.queries_file)
    if args.max_queries > 0:
        cases = cases[: args.max_queries]

    runner = HeadlessRunner(pack_assets, force_no_fts=args.force_no_fts)
    results: list[dict[str, Any]] = []
    try:
        for case in cases:
            started = time.perf_counter()
            ranked = runner.search(case["query"], args.search_limit)
            context = runner.build_context(case["query"], ranked, args.context_limit)
            system_prompt = build_system_prompt(case["query"])
            prompt = build_prompt(case["query"], context)
            payload = {
                "case_id": case["case_id"],
                "query": case["query"],
                "generated_at": utc_now_iso(),
                "runtime": {"fts_available": runner.fts_available, "force_no_fts": args.force_no_fts},
                "timing": {"search_prompt_seconds": round(time.perf_counter() - started, 3)},
                "query_terms": {
                    "route_kind": QueryTerms.from_query(case["query"]).route_profile.kind,
                    "preferred_structure_type": QueryTerms.from_query(case["query"]).metadata_profile.preferred_structure_type,
                },
                "search_results": [row.report_dict() for row in ranked],
                "context_results": [row.report_dict() for row in context],
                "system_prompt": system_prompt,
                "prompt": prompt,
            }
            if args.include_host_answer:
                payload["host_answer"] = request_host_answer(
                    args.host_url,
                    args.host_model,
                    prompt,
                    args.host_timeout_seconds,
                    args.max_tokens,
                    system_prompt=system_prompt,
                )
            if "source" in case:
                payload["input_source"] = case["source"]
            results.append(payload)
    finally:
        runner.close()

    input_slug = Path(args.queries_file).stem if args.queries_file else "inline"
    slug = f"{input_slug}_{datetime.now().strftime('%Y%m%d_%H%M%S_%f')}"
    json_path = output_dir / f"mobile_headless_answers_{slug}.json"
    jsonl_path = output_dir / f"mobile_headless_answers_{slug}.jsonl"
    json_path.write_text(json.dumps({"generated_at": utc_now_iso(), "results": results}, indent=2) + "\n", encoding="utf-8")
    with jsonl_path.open("w", encoding="utf-8") as handle:
        for result in results:
            handle.write(json.dumps(result) + "\n")
    print(f"Headless answers written to {json_path}")
    print(f"JSONL mirror written to {jsonl_path}")
    for result in results[:10]:
        top_source = result["context_results"][0]["source_label"] if result["context_results"] else "-"
        print(f"[{result['case_id']}] {result['query']} | top_context={top_source}")


if __name__ == "__main__":
    main()
