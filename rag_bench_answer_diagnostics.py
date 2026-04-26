"""Answer-card, claim-support, and app-acceptance helpers for RAG bench analysis."""

from __future__ import annotations

import re
from collections import Counter
from typing import Callable

from guide_answer_card_contracts import (
    compose_card_backed_answer,
    compose_evidence_units,
    evaluate_answer_card_contract,
    find_cards_for_guides,
)
from rag_claim_support import diagnose_claim_support


def _truthy(value) -> bool:
    return str(value or "").strip().lower() in {"1", "true", "yes", "y", "on"}


def has_safety_prompt(question: str) -> bool:
    lowered = question.lower()
    markers = (
        "choking",
        "cannot breathe",
        "can't breathe",
        "cannot speak",
        "blue lips",
        "meningitis",
        "newborn",
        "sepsis",
        "poison",
        "drain cleaner",
        "unconscious",
        "severe bleeding",
        "stroke",
        "seizure",
        "cannot swallow",
        "drooling",
    )
    return any(marker in lowered for marker in markers)


def answer_has_emergency_contract(answer: str) -> bool:
    lowered = answer.lower()
    markers = (
        "emergency",
        "urgent",
        "call",
        "911",
        "poison control",
        "cpr",
        "back blows",
        "abdominal thrust",
        "chest thrust",
        "do not",
        "nothing by mouth",
        "evacuat",
    )
    return any(marker in lowered for marker in markers)


def compact_match_phrases(matches: list[dict[str, str]]) -> str:
    phrases = []
    for match in matches:
        phrase = str(match.get("phrase") or "").strip()
        if phrase:
            phrases.append(phrase)
    return "|".join(dict.fromkeys(phrases))


def compact_claim_basis(actions: list[dict]) -> str:
    basis_counts = Counter(
        str(action.get("support_basis") or "unknown") for action in actions
    )
    return "|".join(
        f"{basis}:{count}" for basis, count in sorted(basis_counts.items())
    )


FAMILY_TOKEN_STOPWORDS = {
    "answer",
    "card",
    "child",
    "emergency",
    "first",
    "guide",
    "help",
    "medical",
    "pediatric",
    "red",
    "safety",
    "urgent",
}


def family_tokens(value: str) -> set[str]:
    tokens = {
        token
        for token in re.split(r"[^a-z0-9]+", str(value or "").lower())
        if len(token) >= 3 and token not in FAMILY_TOKEN_STOPWORDS
    }
    return tokens


def select_family_cards(cards: list[dict], expected_family: str) -> list[dict]:
    family = family_tokens(expected_family)
    if not family:
        return cards
    scored_matches = []
    for card in cards:
        card_text = " ".join(
            str(card.get(key) or "")
            for key in ("card_id", "slug", "title")
        )
        score = len(family & family_tokens(card_text))
        if score:
            scored_matches.append((score, card))
    if not scored_matches:
        return cards
    best_score = max(score for score, _card in scored_matches)
    return [card for score, card in scored_matches if score == best_score]


def select_answer_cards(
    *,
    expected_ids: list[str],
    expected_family: str = "",
    cited_ids: list[str],
    candidate_ids: list[str],
    answer_cards: list[dict] | None,
) -> tuple[list[dict], list[str]]:
    guide_ids = expected_ids or list(dict.fromkeys(cited_ids + candidate_ids))
    if not guide_ids:
        return [], []
    cards = find_cards_for_guides(guide_ids, cards=answer_cards or [])
    return select_family_cards(cards, expected_family), guide_ids


def answer_card_diagnostics(
    result: dict,
    *,
    expected_ids: list[str],
    expected_family: str = "",
    cited_ids: list[str],
    candidate_ids: list[str],
    generated: str,
    answer_cards: list[dict] | None,
    selected_cards: list[dict] | None = None,
    selected_guide_ids: list[str] | None = None,
) -> dict[str, str]:
    guide_ids = selected_guide_ids or expected_ids or list(dict.fromkeys(cited_ids + candidate_ids))
    if not guide_ids:
        return {
            "answer_card_status": "no_guide_ids",
            "answer_card_ids": "",
            "answer_card_required_hits": "",
            "answer_card_missing_required": "",
            "answer_card_forbidden_hits": "",
        }

    cards = selected_cards
    if cards is None:
        cards, _guide_ids = select_answer_cards(
            expected_ids=expected_ids,
            expected_family=expected_family,
            cited_ids=cited_ids,
            candidate_ids=candidate_ids,
            answer_cards=answer_cards,
        )
    card_ids = [
        str(card.get("card_id") or "").strip()
        for card in cards
        if str(card.get("card_id") or "").strip()
    ]
    if not cards:
        return {
            "answer_card_status": "no_cards",
            "answer_card_ids": "",
            "answer_card_required_hits": "",
            "answer_card_missing_required": "",
            "answer_card_forbidden_hits": "",
        }

    answer_text = str(result.get("response_text") or "").strip()
    if generated != "yes" or not answer_text:
        return {
            "answer_card_status": "no_generated_answer",
            "answer_card_ids": "|".join(card_ids),
            "answer_card_required_hits": "",
            "answer_card_missing_required": "",
            "answer_card_forbidden_hits": "",
        }

    question_text = str(result.get("question") or result.get("prompt_text") or "")
    not_applicable_status = strict_card_not_applicable_to_prompt(
        question_text,
        cards,
        expected_family=expected_family,
    )
    if not_applicable_status:
        return {
            "answer_card_status": not_applicable_status,
            "answer_card_ids": "|".join(card_ids),
            "answer_card_required_hits": "",
            "answer_card_missing_required": "",
            "answer_card_forbidden_hits": "",
        }

    evaluation = evaluate_answer_card_contract(
        answer_text,
        cards,
        question_text=question_text,
        context=[expected_family],
    )
    return {
        "answer_card_status": str(evaluation.get("status") or ""),
        "answer_card_ids": "|".join(card_ids),
        "answer_card_required_hits": compact_match_phrases(
            evaluation.get("required_action_hits") or []
        ),
        "answer_card_missing_required": compact_match_phrases(
            evaluation.get("missing_required_actions") or []
        ),
        "answer_card_forbidden_hits": compact_match_phrases(
            evaluation.get("forbidden_advice_hits") or []
        ),
    }


def evidence_nugget_diagnostics(
    result: dict,
    *,
    expected_family: str = "",
    cited_ids: list[str],
    generated: str,
    selected_cards: list[dict],
) -> dict[str, str | int]:
    """Return TREC-style evidence coverage fields from reviewed answer cards."""

    empty_fields = {
        "evidence_nugget_status": "",
        "evidence_nugget_total": 0,
        "evidence_nugget_present": 0,
        "evidence_nugget_cited": 0,
        "evidence_nugget_supported": 0,
        "evidence_nugget_missing": 0,
        "evidence_nugget_contradicted": 0,
        "evidence_nugget_missing_phrases": "",
        "evidence_nugget_contradicted_phrases": "",
    }
    if not selected_cards:
        return {**empty_fields, "evidence_nugget_status": "no_cards"}

    answer_text = str(result.get("response_text") or "").strip()
    if generated != "yes" or not answer_text:
        return {**empty_fields, "evidence_nugget_status": "no_evaluable_answer"}

    question_text = str(result.get("question") or result.get("prompt_text") or "")
    not_applicable_status = strict_card_not_applicable_to_prompt(
        question_text,
        selected_cards,
        expected_family=expected_family,
    )
    if not_applicable_status:
        return {**empty_fields, "evidence_nugget_status": not_applicable_status}

    evaluation = evaluate_answer_card_contract(
        answer_text,
        selected_cards,
        question_text=question_text,
        context=[expected_family],
    )
    hits = evaluation.get("required_action_hits") or []
    missing = evaluation.get("missing_required_actions") or []
    contradicted = evaluation.get("forbidden_advice_hits") or []
    total = len(hits) + len(missing)
    cited = set(cited_ids)
    source_ids_by_card = _source_guide_ids_by_card(
        selected_cards,
        question_text=question_text,
        context=[expected_family],
    )
    cited_hits = [
        hit
        for hit in hits
        if cited & _source_ids_for_match(hit, source_ids_by_card)
    ]
    supported = len(cited_hits)

    if contradicted:
        status = "fail"
    elif total == 0:
        status = "no_required_nuggets"
    elif supported == total:
        status = "pass"
    elif hits:
        status = "partial"
    else:
        status = "missing"

    return {
        "evidence_nugget_status": status,
        "evidence_nugget_total": total,
        "evidence_nugget_present": len(hits),
        "evidence_nugget_cited": len(cited_hits),
        "evidence_nugget_supported": supported,
        "evidence_nugget_missing": len(missing),
        "evidence_nugget_contradicted": len(contradicted),
        "evidence_nugget_missing_phrases": compact_match_phrases(missing),
        "evidence_nugget_contradicted_phrases": compact_match_phrases(contradicted),
    }


def _source_guide_ids_by_card(
    cards: list[dict],
    *,
    question_text: str,
    context: list[str],
) -> dict[str, set[str]]:
    source_ids: dict[str, set[str]] = {}
    for unit in compose_evidence_units(
        cards,
        question_text=question_text,
        context=context,
    ):
        ids = {
            str(guide_id).strip()
            for guide_id in unit.get("source_guide_ids") or []
            if str(guide_id).strip()
        }
        for key in (unit.get("card_id"), unit.get("guide_id")):
            key = str(key or "").strip()
            if key:
                source_ids[key] = ids
    return source_ids


def _source_ids_for_match(match: dict, source_ids_by_card: dict[str, set[str]]) -> set[str]:
    card_id = str(match.get("card_id") or "").strip()
    guide_id = str(match.get("guide_id") or "").strip()
    return source_ids_by_card.get(card_id) or source_ids_by_card.get(guide_id) or {guide_id}


def strict_card_not_applicable_to_compare_prompt(question_text: str, cards: list[dict]) -> bool:
    card_ids = {
        str(card.get("card_id") or "").strip()
        for card in cards or []
    }
    return (
        "meningitis_sepsis_child" in card_ids
        and is_bare_meningitis_vs_viral_compare(question_text)
    )


def strict_card_not_applicable_to_prompt(
    question_text: str,
    cards: list[dict],
    *,
    expected_family: str = "",
) -> str:
    if strict_card_not_applicable_to_compare_prompt(question_text, cards):
        return "not_applicable_compare"

    card_ids = {
        str(card.get("card_id") or "").strip()
        for card in cards or []
        if str(card.get("card_id") or "").strip()
    }
    strict_prompt_scoped_cards = {
        "choking_airway_obstruction": has_choking_airway_prompt,
        "newborn_danger_sepsis": has_newborn_danger_prompt,
    }
    family = family_tokens(expected_family)
    family_matches_strict_card = any(
        family
        & family_tokens(
            " ".join(
                str(card.get(key) or "")
                for key in ("card_id", "slug", "title")
            )
        )
        for card in cards or []
        if str(card.get("card_id") or "").strip() in strict_prompt_scoped_cards
    )
    if (
        card_ids
        and card_ids <= set(strict_prompt_scoped_cards)
        and not family_matches_strict_card
        and not any(strict_prompt_scoped_cards[card_id](question_text) for card_id in card_ids)
    ):
        return "not_applicable_prompt"

    return ""


def has_choking_airway_prompt(question_text: str) -> bool:
    lower = str(question_text or "").lower()
    markers = (
        "choking",
        "choke",
        "food stuck",
        "object stuck",
        "object in throat",
        "foreign body",
        "airway obstruction",
        "blocked airway",
        "throat blocked",
        "cannot speak",
        "can't speak",
        "can not speak",
        "cannot cough",
        "can't cough",
        "can not cough",
        "cannot breathe",
        "can't breathe",
        "can not breathe",
        "not breathing",
        "blue lips",
        "turning blue",
        "cyanosis",
        "infant cannot cry",
        "infant can't cry",
        "baby cannot cry",
        "baby can't cry",
        "back blows",
        "abdominal thrust",
        "heimlich",
        "gagging",
        "drooling",
    )
    return any(marker in lower for marker in markers)


def has_newborn_danger_prompt(question_text: str) -> bool:
    lower = str(question_text or "").lower()
    has_newborn_context = any(
        marker in lower
        for marker in (
            "newborn",
            "neonate",
            "neonatal",
            "baby",
            "infant",
            "cord stump",
        )
    )
    has_danger_context = any(
        marker in lower
        for marker in (
            "hard to wake",
            "limp",
            "will not feed",
            "won't feed",
            "poor feeding",
            "fever",
            "low temperature",
            "breathing trouble",
            "seizure",
            "jerking",
            "very sick",
            "lethargic",
            "sleepy",
            "pus",
            "spreading redness",
        )
    )
    return has_newborn_context and has_danger_context


def is_bare_meningitis_vs_viral_compare(question_text: str) -> bool:
    lower = str(question_text or "").lower()
    has_meningitis_context = any(
        marker in lower
        for marker in ("meningitis", "meningococcemia", "meningococcal", "sepsis")
    )
    has_routine_context = any(
        marker in lower
        for marker in ("viral", "viral illness", "virus", "flu", "cold", "routine illness")
    )
    has_boundary_language = any(marker in lower for marker in (" or ", "versus", "vs ", "v. "))
    has_stated_red_flag = any(
        marker in lower
        for marker in (
            "fever",
            "stiff neck",
            "neck is stiff",
            "neck stiffness",
            "rigid neck",
            "severe headache",
            "bad headache",
            "photophobia",
            "light hurts",
            "vomiting",
            "throwing up",
            "confusion",
            "confused",
            "sleepy",
            "hard to wake",
            "purple",
            "dark rash",
            "bruise-like",
            "non-blanching",
            "nonblanching",
            "petechial",
            "rash",
            "shock",
            "very sick",
            "very ill",
        )
    )
    return (
        has_meningitis_context
        and has_routine_context
        and has_boundary_language
        and not has_stated_red_flag
    )


def claim_support_diagnostics(
    result: dict,
    *,
    expected_ids: list[str],
    cited_ids: list[str],
    generated: str,
    selected_cards: list[dict],
) -> dict[str, str | int]:
    question_text = str(result.get("question") or result.get("prompt_text") or "")
    not_applicable_status = strict_card_not_applicable_to_prompt(
        question_text,
        selected_cards,
    )
    if not_applicable_status:
        return {
            "claim_support_status": not_applicable_status,
            "claim_action_count": 0,
            "claim_supported_count": 0,
            "claim_unknown_count": 0,
            "claim_forbidden_count": 0,
            "claim_support_basis": "",
        }

    evaluation = diagnose_claim_support(
        str(result.get("response_text") or ""),
        selected_cards,
        cited_guide_ids=cited_ids,
        expected_guide_ids=expected_ids,
        generated=generated == "yes",
    )
    return {
        "claim_support_status": str(evaluation.get("status") or ""),
        "claim_action_count": evaluation.get("action_count") or 0,
        "claim_supported_count": evaluation.get("supported_count") or 0,
        "claim_unknown_count": evaluation.get("unknown_count") or 0,
        "claim_forbidden_count": evaluation.get("forbidden_count") or 0,
        "claim_support_basis": compact_claim_basis(evaluation.get("actions") or []),
    }


def shadow_card_answer_diagnostics(
    result: dict,
    *,
    expected_ids: list[str],
    expected_family: str,
    cited_ids: list[str],
    selected_cards: list[dict],
    selected_guide_ids: list[str],
) -> dict[str, str | int]:
    question_text = str(result.get("question") or result.get("prompt_text") or "")
    not_applicable_status = strict_card_not_applicable_to_prompt(
        question_text,
        selected_cards,
        expected_family=expected_family,
    )
    if not_applicable_status:
        return {
            "shadow_card_answer_status": not_applicable_status,
            "shadow_claim_support_status": not_applicable_status,
            "shadow_claim_action_count": 0,
            "shadow_card_answer_cited_guide_ids": "",
            "shadow_card_answer_text": "",
        }

    plan = compose_card_backed_answer(
        selected_cards,
        question_text=question_text,
        context=[expected_family],
        allowed_guide_ids=expected_ids or cited_ids or selected_guide_ids,
    )
    answer_text = str(plan.get("answer_text") or "")
    if not answer_text:
        status = str(plan.get("status") or "no_answer")
        return {
            "shadow_card_answer_status": status,
            "shadow_claim_support_status": status,
            "shadow_claim_action_count": 0,
            "shadow_card_answer_cited_guide_ids": "",
            "shadow_card_answer_text": "",
        }

    card_evaluation = evaluate_answer_card_contract(
        answer_text,
        selected_cards,
        question_text=question_text,
        context=[expected_family],
    )
    claim_evaluation = diagnose_claim_support(
        answer_text,
        selected_cards,
        cited_guide_ids=plan.get("cited_guide_ids") or [],
        expected_guide_ids=expected_ids,
        generated=True,
    )
    return {
        "shadow_card_answer_status": str(card_evaluation.get("status") or ""),
        "shadow_claim_support_status": str(claim_evaluation.get("status") or ""),
        "shadow_claim_action_count": claim_evaluation.get("action_count") or 0,
        "shadow_card_answer_cited_guide_ids": "|".join(plan.get("cited_guide_ids") or []),
        "shadow_card_answer_text": answer_text,
    }


def app_acceptance_diagnostics(
    result: dict,
    *,
    bucket: str,
    expected_ids: list[str],
    candidate_ids: list[str],
    cited_ids: list[str],
    app_gate_fields: dict[str, str],
    answer_card_fields: dict[str, str],
    claim_support_fields: dict[str, str | int],
    generated: str,
    safety_prompt_detector: Callable[[str], bool] = has_safety_prompt,
    emergency_contract_detector: Callable[[str], bool] = answer_has_emergency_contract,
) -> dict[str, str]:
    expected = set(expected_ids)
    candidates = set(candidate_ids)
    cited = set(cited_ids)
    expected_cited = bool(expected and expected & cited)
    expected_retrieved = bool(expected and expected & candidates)

    if expected_cited:
        evidence_owner_status = "expected_owner_cited"
    elif expected_retrieved:
        evidence_owner_status = "expected_owner_retrieved_not_cited"
    elif cited:
        evidence_owner_status = "non_expected_owner_cited"
    else:
        evidence_owner_status = "unknown"

    answer_text = str(result.get("response_text") or "")
    safety_critical = _truthy(app_gate_fields.get("safety_critical")) or safety_prompt_detector(
        result.get("question") or ""
    )
    if not safety_critical:
        safety_surface_status = "not_safety_critical"
    elif emergency_contract_detector(answer_text):
        safety_surface_status = "emergency_first_supported"
    else:
        safety_surface_status = "emergency_first_missing"

    gate_status = app_gate_fields.get("app_gate_status") or ""
    card_status = answer_card_fields.get("answer_card_status") or ""
    claim_status = str(claim_support_fields.get("claim_support_status") or "")
    support_strength = (app_gate_fields.get("support_strength") or "").lower()

    if gate_status == "uncertain_fit":
        app_status = "uncertain_fit_accepted"
    elif gate_status == "abstain":
        app_status = "abstain_accepted"
    elif safety_surface_status == "emergency_first_missing":
        app_status = "unsafe_or_overconfident"
    elif generated == "yes" and evidence_owner_status != "expected_owner_cited":
        app_status = "needs_evidence_owner"
    elif card_status == "fail" or claim_status == "fail":
        app_status = "card_contract_gap"
    elif (
        bucket in {"deterministic_pass", "expected_supported"}
        and evidence_owner_status == "expected_owner_cited"
        and (
            card_status
            in {"pass", "no_generated_answer", "not_applicable_compare", "not_applicable_prompt", ""}
        )
        and (
            claim_status
            in {"pass", "no_generated_answer", "not_applicable_compare", "not_applicable_prompt", ""}
        )
    ):
        app_status = "strong_supported"
    elif (
        evidence_owner_status == "expected_owner_cited"
        and card_status in {"pass", "partial", "not_applicable_compare", "not_applicable_prompt", ""}
        and claim_status in {"pass", "partial", "not_applicable_compare", "not_applicable_prompt", ""}
    ):
        app_status = "moderate_supported"
    elif bucket in {"deterministic_pass", "expected_supported"} and evidence_owner_status in {
        "expected_owner_cited",
        "expected_owner_retrieved_not_cited",
    }:
        app_status = "moderate_supported"
    elif support_strength in {"high", "strong"} and evidence_owner_status == "expected_owner_cited":
        app_status = "moderate_supported"
    else:
        app_status = "needs_evidence_owner"

    if gate_status == "abstain":
        ui_surface_bucket = "abstain"
    elif gate_status == "uncertain_fit":
        ui_surface_bucket = "limited_fit"
    elif safety_surface_status == "emergency_first_supported" and safety_critical:
        ui_surface_bucket = "emergency_first"
    elif app_status == "strong_supported":
        ui_surface_bucket = "strong_evidence"
    elif app_status == "moderate_supported":
        ui_surface_bucket = "moderate_evidence"
    else:
        ui_surface_bucket = "limited_fit"

    reasons = [
        evidence_owner_status,
        f"card_{card_status or 'none'}",
        f"claim_{claim_status or 'none'}",
        safety_surface_status,
    ]
    if gate_status:
        reasons.append(f"gate_{gate_status}")
    if bucket:
        reasons.append(f"bucket_{bucket}")

    return {
        "app_acceptance_status": app_status,
        "app_acceptance_reason": "|".join(reasons),
        "evidence_owner_status": evidence_owner_status,
        "safety_surface_status": safety_surface_status,
        "ui_surface_bucket": ui_surface_bucket,
    }
