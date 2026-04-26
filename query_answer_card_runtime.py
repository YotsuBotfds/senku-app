"""Reviewed answer-card runtime helpers for query/bench paths.

This module intentionally does not import query.py. Callers inject the guide-card
loader/composer functions and query-specific predicates so the legacy query.*
helper names remain patchable.
"""

from functools import lru_cache
import os
import re


@lru_cache(maxsize=1)
def _runtime_answer_cards(load_answer_cards):
    """Load reviewed answer-card pilot data for prompt planning when available."""
    if load_answer_cards is None:
        return ()
    try:
        return tuple(load_answer_cards())
    except Exception:
        return ()


def _card_source_guide_ids(card):
    guide_ids = {str(card.get("guide_id") or "").strip()}
    for source in card.get("source_sections") or []:
        if not isinstance(source, dict):
            continue
        guide_id = str(source.get("guide") or "").strip()
        if guide_id:
            guide_ids.add(guide_id)
    return {guide_id for guide_id in guide_ids if guide_id}


def _card_runtime_citation_guide_ids(
    card,
    allowed_guide_ids,
    *,
    citation_allowlist_from_card_sources,
):
    primary_guide_id = str(card.get("guide_id") or "").strip().upper()
    allowed = {str(guide_id or "").strip().upper() for guide_id in allowed_guide_ids or []}
    if primary_guide_id and primary_guide_id in allowed:
        return [primary_guide_id]

    policy = str(card.get("runtime_citation_policy") or "").strip().lower()
    if policy != "reviewed_source_family":
        return []

    return [
        guide_id
        for guide_id in citation_allowlist_from_card_sources(card)
        if guide_id in allowed
    ]


def _citation_allowlist_from_card_sources(card):
    guide_ids = []
    primary_guide_id = str(card.get("guide_id") or "").strip().upper()
    if primary_guide_id:
        guide_ids.append(primary_guide_id)
    for source in card.get("source_sections") or []:
        if not isinstance(source, dict):
            continue
        guide_id = str(source.get("guide") or "").strip().upper()
        if guide_id:
            guide_ids.append(guide_id)
    return list(dict.fromkeys(guide_ids))


def _citation_allowlist_from_results(results):
    """Return retrieved guide IDs that are allowed to appear as citations."""
    metadatas = (results or {}).get("metadatas") or []
    if not metadatas or not metadatas[0]:
        return []

    allowlist = []
    seen = set()
    for meta in metadatas[0]:
        meta = meta or {}
        guide_id = str(meta.get("guide_id") or "").strip().upper()
        if not guide_id:
            source_file = str(meta.get("source_file") or "").strip().upper()
            match = re.search(r"GD-\d{3}", source_file)
            guide_id = match.group(0) if match else ""
        if not guide_id or guide_id in seen:
            continue
        seen.add(guide_id)
        allowlist.append(guide_id)
    return allowlist


def _answer_cards_for_results(
    results,
    *,
    question=None,
    max_cards=2,
    max_source_ids=2,
    runtime_answer_cards,
    citation_allowlist_from_results,
    prioritized_answer_card_ids_for_question,
    answer_card_matches_question,
    card_source_guide_ids,
):
    allowed_guide_ids = citation_allowlist_from_results(results)
    guide_ids = allowed_guide_ids[:max_source_ids]
    if not allowed_guide_ids:
        return []

    cards = []
    seen_card_ids = set()
    cards_by_guide_id = {}
    for card in runtime_answer_cards():
        review_status = str(card.get("review_status") or "").strip().lower()
        if review_status not in {"pilot_reviewed", "approved"}:
            continue
        guide_id = str(card.get("guide_id") or "").strip()
        if not guide_id:
            continue
        for source_guide_id in card_source_guide_ids(card) or {guide_id}:
            cards_by_guide_id.setdefault(source_guide_id, []).append(card)

    prioritized_card_ids = prioritized_answer_card_ids_for_question(question)
    if prioritized_card_ids:
        retrieved_guide_ids = set(allowed_guide_ids)
        prioritized_cards = []
        for card in runtime_answer_cards():
            review_status = str(card.get("review_status") or "").strip().lower()
            card_id = str(card.get("card_id") or "").strip()
            if review_status not in {"pilot_reviewed", "approved"}:
                continue
            if card_id not in prioritized_card_ids or card_id in seen_card_ids:
                continue
            if not answer_card_matches_question(card, question):
                continue
            if not (retrieved_guide_ids & card_source_guide_ids(card)):
                continue
            prioritized_cards.append(card)
        if max_cards == 1:
            for card in prioritized_cards:
                seen_card_ids.add(str(card.get("card_id") or "").strip())
                cards.append(card)
                return cards
    for guide_id in guide_ids:
        guide_cards = cards_by_guide_id.get(guide_id, [])
        if prioritized_card_ids:
            guide_cards = sorted(
                guide_cards,
                key=lambda card: (
                    str(card.get("card_id") or "").strip()
                    not in prioritized_card_ids
                ),
            )
        for card in guide_cards:
            card_id = str(card.get("card_id") or "").strip()
            if card_id in seen_card_ids:
                continue
            if not answer_card_matches_question(card, question):
                continue
            seen_card_ids.add(card_id)
            cards.append(card)
            if len(cards) >= max_cards:
                return cards
    for card in prioritized_cards if prioritized_card_ids else []:
        card_id = str(card.get("card_id") or "").strip()
        if card_id in seen_card_ids:
            continue
        seen_card_ids.add(card_id)
        cards.append(card)
        if len(cards) >= max_cards:
            return cards
    return cards


def _prioritized_answer_card_ids_for_question(
    question,
    *,
    is_airway_obstruction_rag_query,
    has_allergy_or_anaphylaxis_trigger,
    is_newborn_sepsis_danger_query,
    is_meningitis_rash_emergency_query,
    is_poisoning_unknown_ingestion_card_query=lambda question: False,
    is_infected_wound_card_query=lambda question: False,
    is_anaphylaxis_red_zone_card_query=None,
):
    if not question:
        return []

    if is_anaphylaxis_red_zone_card_query is None:
        is_anaphylaxis_red_zone_card_query = _is_anaphylaxis_red_zone_card_query

    prioritized = []
    if is_anaphylaxis_red_zone_card_query(question):
        prioritized.append("anaphylaxis_red_zone")
    if is_airway_obstruction_rag_query(question) and not has_allergy_or_anaphylaxis_trigger(question):
        prioritized.append("choking_airway_obstruction")
    if is_newborn_sepsis_danger_query(question):
        prioritized.append("newborn_danger_sepsis")
    if is_meningitis_rash_emergency_query(question):
        prioritized.append("meningitis_sepsis_child")
    if is_poisoning_unknown_ingestion_card_query(question):
        prioritized.append("poisoning_unknown_ingestion")
    if is_infected_wound_card_query(question):
        prioritized.append("infected_wound_spreading_infection")
    return prioritized


def _answer_card_matches_question(
    card,
    question,
    *,
    is_airway_obstruction_rag_query,
    has_allergy_or_anaphylaxis_trigger,
    is_newborn_sepsis_danger_query,
    is_meningitis_rash_emergency_query,
    is_poisoning_unknown_ingestion_card_query=lambda question: False,
    is_infected_wound_card_query=lambda question: False,
    is_anaphylaxis_red_zone_card_query=None,
):
    if not question:
        return True

    if is_anaphylaxis_red_zone_card_query is None:
        is_anaphylaxis_red_zone_card_query = _is_anaphylaxis_red_zone_card_query

    card_id = str(card.get("card_id") or "").strip()
    if card_id == "anaphylaxis_red_zone":
        return is_anaphylaxis_red_zone_card_query(question)
    if card_id == "newborn_danger_sepsis":
        return is_newborn_sepsis_danger_query(question)
    if card_id == "meningitis_sepsis_child":
        return is_meningitis_rash_emergency_query(question)
    if card_id == "choking_airway_obstruction":
        return is_airway_obstruction_rag_query(question) and not has_allergy_or_anaphylaxis_trigger(question)
    if card_id == "poisoning_unknown_ingestion":
        return is_poisoning_unknown_ingestion_card_query(question)
    if card_id == "infected_wound_spreading_infection":
        return is_infected_wound_card_query(question)
    return True


_ANAPHYLAXIS_EXPOSURE_TERMS = {
    "allergen",
    "allergic reaction",
    "allergy",
    "ate",
    "bee sting",
    "bee stung",
    "bitten",
    "food",
    "medicine",
    "medication",
    "new medicine",
    "new medication",
    "peanut",
    "shellfish",
    "sting",
    "stung",
    "wasp",
}
_ANAPHYLAXIS_SKIN_TERMS = {
    "hives",
    "itchy welts",
    "rash",
    "welts",
}
_ANAPHYLAXIS_AIRWAY_BREATHING_TERMS = {
    "barely talk",
    "barely talking",
    "blue lips",
    "breathing trouble",
    "can barely talk",
    "cant breathe",
    "chest tightness",
    "difficulty breathing",
    "gray color",
    "grey color",
    "shortness of breath",
    "stridor",
    "throat closing",
    "throat swelling",
    "tongue swelling",
    "trouble breathing",
    "wheeze",
    "wheezing",
}
_ANAPHYLAXIS_CIRCULATION_TERMS = {
    "collapse",
    "collapsed",
    "cold clammy",
    "faint",
    "fainted",
    "fainting",
    "faintness",
    "passed out",
}
_ANAPHYLAXIS_SWELLING_TERMS = {
    "face swelling",
    "facial swelling",
    "lip swelling",
    "lips swelling",
    "swollen face",
    "swollen lip",
    "swollen lips",
    "swollen tongue",
    "tongue swelling",
}
_ANAPHYLAXIS_SKIN_ONLY_NORMAL_TERMS = {
    "alert",
    "breathing fine",
    "breathing is fine",
    "breathing is normal",
    "breathing normal",
    "normal alertness",
    "normal breathing",
    "no breathing trouble",
    "no dizziness",
    "no fainting",
    "no swelling",
    "no throat swelling",
    "no trouble breathing",
}
_ANAPHYLAXIS_NEGATED_RED_ZONE_PHRASES = {
    "no blue lips",
    "no breathing trouble",
    "no chest tightness",
    "no collapse",
    "no dizziness",
    "no face swelling",
    "no facial swelling",
    "no fainting",
    "no faintness",
    "no gray color",
    "no grey color",
    "no lip swelling",
    "no lips swelling",
    "no shortness of breath",
    "no stridor",
    "no swollen face",
    "no swollen lips",
    "no swollen tongue",
    "no throat closing",
    "no throat swelling",
    "no tongue swelling",
    "no trouble breathing",
    "not faint",
    "not wheezing",
}


def _is_anaphylaxis_red_zone_card_query(question):
    """Detect allergen-linked airway, breathing, circulation, or swelling danger."""
    lower = str(question or "").lower()
    if not lower:
        return False

    has_exposure = _text_has_any(lower, _ANAPHYLAXIS_EXPOSURE_TERMS)
    has_skin = _text_has_any(lower, _ANAPHYLAXIS_SKIN_TERMS)
    red_zone_text = _without_anaphylaxis_negated_red_zone_phrases(lower)
    has_airway_or_breathing = _text_has_any(
        red_zone_text, _ANAPHYLAXIS_AIRWAY_BREATHING_TERMS
    )
    has_circulation = _text_has_any(red_zone_text, _ANAPHYLAXIS_CIRCULATION_TERMS)
    has_swelling = _text_has_any(red_zone_text, _ANAPHYLAXIS_SWELLING_TERMS)

    if has_exposure and (has_airway_or_breathing or has_circulation or has_swelling):
        return True
    if has_skin and (has_airway_or_breathing or has_circulation or has_swelling):
        return True

    skin_only_normal = (
        has_exposure
        and has_skin
        and _text_has_any(lower, _ANAPHYLAXIS_SKIN_ONLY_NORMAL_TERMS)
    )
    if skin_only_normal:
        return False

    return False


def _without_anaphylaxis_negated_red_zone_phrases(text):
    for phrase in _ANAPHYLAXIS_NEGATED_RED_ZONE_PHRASES:
        text = text.replace(phrase, " ")
    return text


def _text_has_any(text, terms):
    return any(term in text for term in terms)


def _format_card_items(items, *, limit=3):
    values = [_stringify_card_item(item) for item in items or []]
    values = [value for value in values if value]
    return "; ".join(values[:limit])


def _stringify_card_item(item):
    if isinstance(item, dict):
        parts = []
        for key, value in item.items():
            key_text = str(key).strip()
            value_text = str(value).strip()
            if key_text and value_text:
                parts.append(f"{key_text}: {value_text}")
        return "; ".join(parts)
    return str(item).strip()


def _active_conditional_card_actions(card, question):
    normalized_question = str(question or "").lower()
    active_actions = []
    for branch in card.get("conditional_required_actions") or []:
        if not isinstance(branch, dict):
            continue
        trigger_terms = [
            str(term).strip().lower()
            for term in branch.get("trigger_terms") or []
            if str(term).strip()
        ]
        if not trigger_terms or not any(term in normalized_question for term in trigger_terms):
            continue
        active_actions.extend(
            str(action).strip()
            for action in branch.get("required_actions") or []
            if str(action).strip()
        )
    return active_actions


def _answer_card_contract_block(
    question,
    results,
    *,
    prompt_token_limit=None,
    answer_cards_for_results,
    build_guide_evidence_packet,
    format_card_items,
    estimate_tokens,
):
    cards = answer_cards_for_results(results, question=question)
    if not cards:
        return ""
    if build_guide_evidence_packet is None:
        return ""
    evidence_packet = build_guide_evidence_packet(cards, question_text=question)
    if evidence_packet.get("status") != "ready":
        return ""

    lines = [
        "Evidence packet from reviewed guide answer cards:",
        "- Use this as the answer plan when it matches the prompt. Lead with required first actions before routine advice, cite the retrieved guide owners, and do not provide forbidden advice.",
    ]
    guide_ids = evidence_packet.get("guide_ids") or []
    if guide_ids:
        lines.append(f"- Evidence guide ids: {', '.join(guide_ids)}.")
    for card in evidence_packet.get("cards") or []:
        card_id = str(card.get("card_id") or "").strip()
        guide_id = str(card.get("guide_id") or "").strip()
        title = str(card.get("title") or "").strip()
        label = f"{card_id} [{guide_id}]"
        if title:
            label = f"{label} {title}"
        lines.append(f"- {label}")

        citation_ids = format_card_items(card.get("citation_ids"), limit=4)
        if citation_ids:
            lines.append(f"  Citation anchors: {citation_ids}.")

        required = format_card_items(card.get("required_first_actions"))
        if required:
            lines.append(f"  Required first actions: {required}.")

        conditional = format_card_items(card.get("active_conditional_required_actions"))
        if conditional:
            lines.append(f"  Active conditional requirements: {conditional}.")

        first_actions = format_card_items(card.get("first_actions"), limit=2)
        if first_actions:
            lines.append(f"  Grounded action outline: {first_actions}.")

        red_flags = format_card_items(card.get("urgent_red_flags"), limit=2)
        if red_flags:
            lines.append(f"  Urgent red flags: {red_flags}.")

        forbidden = format_card_items(card.get("forbidden_advice"), limit=3)
        if forbidden:
            lines.append(f"  Forbidden advice: {forbidden}.")
        do_not = format_card_items(card.get("do_not"), limit=2)
        if do_not:
            lines.append(f"  Do not: {do_not}.")
    block = "\n".join(lines)
    if prompt_token_limit:
        if prompt_token_limit < 768:
            return ""
        max_contract_tokens = max(96, min(360, int(prompt_token_limit * 0.12)))
        if estimate_tokens(block) > max_contract_tokens:
            return ""
    return block


def _card_backed_runtime_answer_plan(
    question,
    results,
    *,
    answer_cards_for_results,
    citation_allowlist_from_results,
    card_runtime_citation_guide_ids,
    compose_guide_card_backed_answer,
):
    """Return deterministic reviewed-card answer text and provenance when enabled."""
    if os.environ.get("SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS", "").strip() != "1":
        return None
    if compose_guide_card_backed_answer is None:
        return None

    cards = answer_cards_for_results(results, question=question, max_cards=1)
    if len(cards) != 1:
        return None

    card = cards[0] or {}
    review_status = str(card.get("review_status") or "").strip().lower()
    if review_status not in {"pilot_reviewed", "approved"}:
        return None
    risk_tier = str(card.get("risk_tier") or "").strip().lower()
    if risk_tier not in {"critical", "high"}:
        return None

    allowed_guide_ids = citation_allowlist_from_results(results)
    runtime_citation_guide_ids = card_runtime_citation_guide_ids(card, allowed_guide_ids)
    if not runtime_citation_guide_ids:
        return None

    plan = compose_guide_card_backed_answer(
        cards,
        question_text=question,
        allowed_guide_ids=allowed_guide_ids,
        max_cards=1,
    )
    if plan.get("status") != "ready":
        return None
    if not plan.get("cited_guide_ids"):
        return None
    answer_text = str(plan.get("answer_text") or "").strip()
    if not answer_text:
        return None

    return {
        "answer_text": answer_text,
        "card_ids": plan.get("card_ids") or [str(card.get("card_id") or "").strip()],
        "guide_ids": plan.get("guide_ids") or runtime_citation_guide_ids,
        "cited_guide_ids": plan.get("cited_guide_ids") or [],
        "review_status": review_status,
        "risk_tier": risk_tier,
    }


def _card_backed_runtime_answer(question, results, *, card_backed_runtime_answer_plan):
    """Return a deterministic reviewed-card answer only when explicitly enabled."""
    plan = card_backed_runtime_answer_plan(question, results)
    if not plan:
        return None
    return str(plan.get("answer_text") or "").strip() or None
