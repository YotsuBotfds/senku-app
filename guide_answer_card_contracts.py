from __future__ import annotations

import re
import string
from pathlib import Path
from typing import Any, Iterable

try:
    import yaml
except ImportError:  # pragma: no cover - exercised by dependency-free callers.
    yaml = None


DEFAULT_CARDS_DIR = Path(__file__).resolve().parent / "notes" / "specs" / "guide_answer_cards"
CARD_FIELDS = (
    "card_id",
    "guide_id",
    "slug",
    "title",
    "risk_tier",
    "evidence_owner",
    "required_first_actions",
    "conditional_required_actions",
    "first_actions",
    "urgent_red_flags",
    "forbidden_advice",
    "do_not",
    "routine_boundary",
    "acceptable_uncertain_fit",
    "citation_ids",
    "source_sections",
    "source_invariants",
    "runtime_citation_policy",
    "review_status",
    "notes",
)
CRITICAL_RISK_TIERS = {"critical", "high"}
SUPPORT_STOPWORDS = {
    "a",
    "an",
    "and",
    "are",
    "as",
    "be",
    "before",
    "for",
    "from",
    "has",
    "have",
    "if",
    "in",
    "is",
    "it",
    "of",
    "or",
    "the",
    "them",
    "this",
    "to",
    "while",
    "with",
}
SUPPORT_ANCHOR_TOKENS = {
    "airway",
    "antibiotic",
    "back",
    "breathing",
    "care",
    "chest",
    "cpr",
    "emergency",
    "escalate",
    "escalation",
    "evaluation",
    "feed",
    "feeding",
    "fever",
    "help",
    "hypothermia",
    "immediate",
    "infant",
    "lethargy",
    "medical",
    "meningitis",
    "monitor",
    "newborn",
    "rash",
    "sepsis",
    "shock",
    "thrust",
    "urgent",
    "warm",
}


def load_answer_cards(cards_dir: Path | str = DEFAULT_CARDS_DIR) -> list[dict[str, Any]]:
    if yaml is None:
        raise RuntimeError("PyYAML is required to load guide answer cards")

    root = Path(cards_dir)
    cards: list[dict[str, Any]] = []
    for path in sorted(root.glob("*.yaml")):
        with path.open("r", encoding="utf-8") as handle:
            payload = yaml.safe_load(handle) or {}
        card = {field: payload.get(field) for field in CARD_FIELDS}
        card["source_path"] = str(path)
        cards.append(card)
    return cards


def find_cards_for_guides(
    guide_ids: str | Iterable[str],
    cards: Iterable[dict[str, Any]] | None = None,
) -> list[dict[str, Any]]:
    if isinstance(guide_ids, str):
        wanted = {guide_ids}
    else:
        wanted = {str(guide_id) for guide_id in guide_ids}

    available_cards = list(cards) if cards is not None else load_answer_cards()
    return [card for card in available_cards if str(card.get("guide_id")) in wanted]


def compose_evidence_units(
    cards: Iterable[dict[str, Any]] | None,
    *,
    question_text: str | None = None,
    context: str | Iterable[str] | None = None,
    max_units: int | None = None,
) -> list[dict[str, Any]]:
    """Compose deterministic, citation-ready evidence units from answer cards."""

    card_list = list(cards or [])
    if max_units is not None:
        card_list = card_list[:max_units]

    normalized_context = _normalize_context(question_text, context)
    return [_evidence_packet_card(card, normalized_context) for card in card_list]


def build_evidence_packet(
    cards: Iterable[dict[str, Any]] | None,
    *,
    question_text: str | None = None,
    max_cards: int = 2,
) -> dict[str, Any]:
    """Build a deterministic evidence packet from reviewed answer cards."""

    evidence_units = compose_evidence_units(
        cards,
        question_text=question_text,
        max_units=max_cards,
    )
    if not evidence_units:
        return {"status": "no_cards", "card_ids": [], "guide_ids": [], "cards": []}

    guide_ids = _unique_strings(
        guide_id
        for evidence_unit in evidence_units
        for guide_id in evidence_unit["source_guide_ids"]
    )
    return {
        "status": "ready",
        "card_ids": [unit["card_id"] for unit in evidence_units if unit["card_id"]],
        "guide_ids": guide_ids,
        "cards": evidence_units,
        "evidence_units": evidence_units,
    }


def compose_card_backed_answer(
    cards: Iterable[dict[str, Any]] | None,
    *,
    question_text: str | None = None,
    context: str | Iterable[str] | None = None,
    allowed_guide_ids: Iterable[str] | None = None,
    max_cards: int = 1,
    max_supporting_actions: int = 2,
    max_red_flags: int = 2,
    max_negative_actions: int = 1,
) -> dict[str, Any]:
    """Compose a deterministic shadow answer from reviewed answer cards."""

    evidence_units = compose_evidence_units(
        cards,
        question_text=question_text,
        context=context,
        max_units=max_cards,
    )
    if not evidence_units:
        return {
            "status": "no_cards",
            "card_ids": [],
            "guide_ids": [],
            "cited_guide_ids": [],
            "lines": [],
            "answer_text": "",
        }

    allowed = set(_as_string_list(allowed_guide_ids))
    lines: list[dict[str, str]] = []
    for unit in evidence_units:
        citation_id = _card_answer_citation_id(unit, allowed)
        for text in unit["required_first_actions"]:
            lines.append(_card_answer_line(unit, "required_first_action", text, citation_id))
        for text in unit["active_conditional_required_actions"]:
            lines.append(
                _card_answer_line(unit, "conditional_required_action", text, citation_id)
            )
        for text in unit["first_actions"][:max_supporting_actions]:
            lines.append(_card_answer_line(unit, "first_action", text, citation_id))
        negative_lines = _safe_negative_answer_lines(unit, max_negative_actions)
        for text in negative_lines:
            lines.append(_card_answer_line(unit, "negative_safety", text, citation_id))
        for text in unit["urgent_red_flags"][:max_red_flags]:
            lines.append(_card_answer_line(unit, "red_flag", f"Red flag: {text}", citation_id))

    answer_text = "\n".join(
        f"{index}. {line['text']}{_citation_suffix(line)}"
        for index, line in enumerate(lines, start=1)
    )
    guide_ids = _unique_strings(
        guide_id for unit in evidence_units for guide_id in unit["source_guide_ids"]
    )
    return {
        "status": "ready" if answer_text else "empty",
        "card_ids": [unit["card_id"] for unit in evidence_units if unit["card_id"]],
        "guide_ids": guide_ids,
        "cited_guide_ids": _unique_strings(line["citation_id"] for line in lines),
        "lines": lines,
        "answer_text": answer_text,
    }


def _evidence_packet_card(card: dict[str, Any], normalized_context: str) -> dict[str, Any]:
    active_conditionals = _active_conditional_required_actions(
        card,
        normalized_context,
    )
    unit = {
        "unit_id": str(card.get("card_id") or "").strip(),
        "card_id": str(card.get("card_id") or "").strip(),
        "guide_id": str(card.get("guide_id") or "").strip(),
        "title": str(card.get("title") or "").strip(),
        "risk_tier": str(card.get("risk_tier") or "").strip(),
        "source_guide_ids": _card_source_guide_ids(card),
        "citation_ids": _as_string_list(card.get("citation_ids")),
        "source_sections": _as_mapping_list(card.get("source_sections")),
        "source_invariants": _as_mapping_list(card.get("source_invariants")),
        "runtime_citation_policy": str(card.get("runtime_citation_policy") or "").strip(),
        "required_first_actions": _as_string_list(card.get("required_first_actions")),
        "active_conditional_required_actions": active_conditionals,
        "first_actions": _as_string_list(card.get("first_actions")),
        "urgent_red_flags": _as_string_list(card.get("urgent_red_flags")),
        "forbidden_advice": _as_string_list(card.get("forbidden_advice")),
        "do_not": _as_string_list(card.get("do_not")),
    }
    unit["support_phrases"] = _support_phrase_records(unit)
    return unit


def _card_answer_citation_id(unit: dict[str, Any], allowed_guide_ids: set[str]) -> str:
    guide_id = str(unit.get("guide_id") or "").strip()
    source_ids = _as_string_list(unit.get("source_guide_ids"))
    if not guide_id or guide_id not in set(source_ids):
        return ""
    if allowed_guide_ids and guide_id not in allowed_guide_ids:
        policy = str(unit.get("runtime_citation_policy") or "").strip().lower()
        if policy == "reviewed_source_family":
            for source_id in source_ids:
                if source_id in allowed_guide_ids:
                    return source_id
        return ""
    return guide_id


def _card_answer_line(
    unit: dict[str, Any],
    kind: str,
    text: str,
    citation_id: str,
) -> dict[str, str]:
    return {
        "kind": kind,
        "card_id": str(unit.get("card_id") or ""),
        "guide_id": str(unit.get("guide_id") or ""),
        "citation_id": citation_id,
        "text": str(text or "").strip(),
    }


def _safe_negative_answer_lines(unit: dict[str, Any], limit: int) -> list[str]:
    lines = []
    card = {
        "card_id": str(unit.get("card_id") or ""),
        "guide_id": str(unit.get("guide_id") or ""),
    }
    for phrase in [
        *_as_string_list(unit.get("forbidden_advice")),
        *_as_string_list(unit.get("do_not")),
    ]:
        if len(lines) >= limit:
            break
        text = f"Avoid: {phrase}"
        if _prohibited_advice_hits(_normalize(text), card, [phrase]):
            continue
        lines.append(text)
    return lines


def _citation_suffix(line: dict[str, str]) -> str:
    citation_id = str(line.get("citation_id") or "").strip()
    return f" [{citation_id}]" if citation_id else ""


def _card_source_guide_ids(card: dict[str, Any]) -> list[str]:
    guide_ids = [str(card.get("guide_id") or "").strip()]
    for source in card.get("source_sections") or []:
        if not isinstance(source, dict):
            continue
        guide_ids.append(str(source.get("guide") or "").strip())
    return _unique_strings(guide_ids)


def evaluate_answer_card_contract(
    answer_text: str,
    cards: Iterable[dict[str, Any]] | None,
    question_text: str | None = None,
    context: str | Iterable[str] | None = None,
) -> dict[str, Any]:
    card_list = list(cards or [])
    if not card_list:
        return {
            "status": "no_cards",
            "required_action_hits": [],
            "red_flag_hits": [],
            "forbidden_advice_hits": [],
            "missing_required_actions": [],
            "card_results": [],
        }

    normalized_answer = _normalize(answer_text)
    normalized_context = _normalize_context(question_text, context)
    card_results = [
        _evaluate_card(normalized_answer, normalized_context, card)
        for card in card_list
    ]

    required_action_hits = _flatten(card_results, "required_action_hits")
    red_flag_hits = _flatten(card_results, "red_flag_hits")
    forbidden_advice_hits = _flatten(card_results, "forbidden_advice_hits")
    missing_required_actions = _flatten(card_results, "missing_required_actions")
    status = _aggregate_status(card_results)

    return {
        "status": status,
        "required_action_hits": required_action_hits,
        "red_flag_hits": red_flag_hits,
        "forbidden_advice_hits": forbidden_advice_hits,
        "missing_required_actions": missing_required_actions,
        "card_results": card_results,
    }


def _evaluate_card(
    normalized_answer: str,
    normalized_context: str,
    card: dict[str, Any],
) -> dict[str, Any]:
    required_actions = _active_required_actions(card, normalized_context)
    first_actions = _as_string_list(card.get("first_actions"))
    red_flags = _as_string_list(card.get("urgent_red_flags"))
    forbidden_advice = _as_string_list(card.get("forbidden_advice"))

    required_action_hits, missing_required_actions = _hit_and_miss(
        normalized_answer,
        card,
        required_actions,
    )
    first_action_hits = _hits(normalized_answer, card, first_actions)
    red_flag_hits = _hits(normalized_answer, card, red_flags)
    forbidden_advice_hits = _prohibited_advice_hits(
        normalized_answer,
        card,
        forbidden_advice,
    )
    status = _card_status(
        card,
        required_action_hits,
        first_action_hits,
        red_flag_hits,
        missing_required_actions,
        forbidden_advice_hits,
    )

    return {
        "card_id": card.get("card_id"),
        "guide_id": card.get("guide_id"),
        "risk_tier": card.get("risk_tier"),
        "status": status,
        "required_action_hits": required_action_hits,
        "first_action_hits": first_action_hits,
        "red_flag_hits": red_flag_hits,
        "forbidden_advice_hits": forbidden_advice_hits,
        "missing_required_actions": missing_required_actions,
    }


def _card_status(
    card: dict[str, Any],
    required_action_hits: list[dict[str, str]],
    first_action_hits: list[dict[str, str]],
    red_flag_hits: list[dict[str, str]],
    missing_required_actions: list[dict[str, str]],
    forbidden_advice_hits: list[dict[str, str]],
) -> str:
    if forbidden_advice_hits:
        return "fail"

    risk_tier = str(card.get("risk_tier", "")).lower()
    if risk_tier in CRITICAL_RISK_TIERS:
        if not missing_required_actions:
            return "pass"
        if required_action_hits:
            return "partial"
        if first_action_hits or red_flag_hits:
            return "partial"
        return "fail"

    if missing_required_actions and (
        required_action_hits or first_action_hits or red_flag_hits
    ):
        return "partial"
    if missing_required_actions:
        return "fail"
    return "pass"


def _aggregate_status(card_results: list[dict[str, Any]]) -> str:
    statuses = {str(result.get("status")) for result in card_results}
    if "fail" in statuses:
        return "fail"
    if "partial" in statuses:
        return "partial"
    return "pass"


def _flatten(card_results: list[dict[str, Any]], key: str) -> list[dict[str, str]]:
    flattened: list[dict[str, str]] = []
    for result in card_results:
        flattened.extend(result.get(key, []))
    return flattened


def _hit_and_miss(
    normalized_answer: str,
    card: dict[str, Any],
    phrases: Iterable[str],
) -> tuple[list[dict[str, str]], list[dict[str, str]]]:
    hits: list[dict[str, str]] = []
    misses: list[dict[str, str]] = []
    for phrase in phrases:
        target = _find_required_action_match(normalized_answer, card, phrase)
        if target:
            hits.append(_match_record(card, phrase, target))
        else:
            misses.append(_match_record(card, phrase, _normalize(phrase)))
    return hits, misses


def _active_required_actions(card: dict[str, Any], normalized_context: str) -> list[str]:
    actions = _as_string_list(card.get("required_first_actions"))
    actions.extend(_active_conditional_required_actions(card, normalized_context))
    return actions


def _active_conditional_required_actions(
    card: dict[str, Any],
    normalized_context: str,
) -> list[str]:
    actions: list[str] = []
    for branch in _as_conditional_required_actions(card.get("conditional_required_actions")):
        if _conditional_branch_applies(branch, normalized_context):
            actions.extend(_as_string_list(branch.get("required_actions")))
    return actions


def _as_conditional_required_actions(value: Any) -> list[dict[str, Any]]:
    if not isinstance(value, list):
        return []
    return [item for item in value if isinstance(item, dict)]


def _conditional_branch_applies(branch: dict[str, Any], normalized_context: str) -> bool:
    if not normalized_context:
        return False
    trigger_terms = _as_string_list(branch.get("trigger_terms"))
    return any(_normalize(term) in normalized_context for term in trigger_terms)


def _find_required_action_match(
    normalized_answer: str,
    card: dict[str, Any],
    phrase: str,
) -> str:
    target = _find_match(normalized_answer, phrase, allow_overlap=False)
    if target:
        return target
    for alias in _required_action_aliases(card, phrase):
        target = _find_match(normalized_answer, alias, allow_overlap=False)
        if target:
            return target
    return ""


def _required_action_aliases(card: dict[str, Any], phrase: str) -> list[str]:
    aliases = card.get("required_action_aliases")
    if isinstance(aliases, dict):
        value = aliases.get(phrase)
        if value:
            return _as_string_list(value)

    if card.get("card_id") != "newborn_danger_sepsis":
        return []
    normalized_phrase = _normalize(phrase)
    if "requiring immediate escalation" in normalized_phrase:
        return [
            "seek urgent medical evaluation",
            "urgent medical evaluation",
            "emergency help now",
            "emergency medical help now",
        ]
    return []


def _hits(
    normalized_answer: str,
    card: dict[str, Any],
    phrases: Iterable[str],
) -> list[dict[str, str]]:
    hits: list[dict[str, str]] = []
    for phrase in phrases:
        target = _find_match(normalized_answer, phrase)
        if target:
            hits.append(_match_record(card, phrase, target))
    return hits


def _prohibited_advice_hits(
    normalized_answer: str,
    card: dict[str, Any],
    phrases: Iterable[str],
) -> list[dict[str, str]]:
    hits: list[dict[str, str]] = []
    answer_tokens = normalized_answer.split()
    for phrase in phrases:
        for target in _forbidden_action_targets(phrase):
            if _has_unnegated_token_match(answer_tokens, target.split()):
                hits.append(_match_record(card, phrase, target))
                break
    return hits


def _forbidden_action_targets(phrase: str) -> list[str]:
    targets = set()
    for candidate in _matchable_phrases(phrase):
        target = _strip_negative_lead(candidate)
        if len(target.split()) >= 2:
            targets.add(target)
    return sorted(targets, key=lambda item: (len(item.split()), len(item)), reverse=True)


def _strip_negative_lead(candidate: str) -> str:
    words = candidate.split()
    while words and words[0] in {"do", "dont", "don", "never", "avoid", "no"}:
        words = words[1:]
    if words and words[0] == "not":
        words = words[1:]
    return " ".join(words)


def _has_unnegated_token_match(answer_tokens: list[str], target_tokens: list[str]) -> bool:
    if not answer_tokens or not target_tokens:
        return False
    last_start = len(answer_tokens) - len(target_tokens)
    for start in range(last_start + 1):
        if answer_tokens[start : start + len(target_tokens)] != target_tokens:
            continue
        if _is_negated(answer_tokens, start):
            continue
        return True
    return False


def _is_negated(tokens: list[str], start: int) -> bool:
    window = tokens[max(0, start - 5) : start]
    if any(token in {"not", "never", "avoid", "avoiding", "without", "no"} for token in window):
        return True
    if "dont" in window or "don" in window:
        return True
    return False


def _match_record(card: dict[str, Any], phrase: str, matched_text: str) -> dict[str, str]:
    return {
        "card_id": str(card.get("card_id", "")),
        "guide_id": str(card.get("guide_id", "")),
        "phrase": phrase,
        "matched_text": matched_text,
    }


def _find_match(normalized_answer: str, phrase: str, *, allow_overlap: bool = True) -> str:
    for candidate in _matchable_phrases(phrase):
        if candidate in normalized_answer:
            return candidate
    if not allow_overlap:
        return ""
    return _overlap_match(normalized_answer, phrase)


def _overlap_match(normalized_answer: str, phrase: str) -> str:
    answer_tokens = set(_support_tokens(normalized_answer))
    phrase_tokens = _support_tokens(phrase)
    if len(phrase_tokens) < 3:
        return ""
    overlap = [token for token in phrase_tokens if token in answer_tokens]
    if len(overlap) < 3:
        return ""
    anchor_hits = SUPPORT_ANCHOR_TOKENS & set(overlap)
    if len(anchor_hits) < 2:
        return ""
    ratio = len(set(overlap)) / max(1, len(set(phrase_tokens)))
    threshold = 0.55 if len(set(phrase_tokens)) <= 8 else 0.35
    return " ".join(overlap) if ratio >= threshold else ""


def _support_tokens(text: str) -> list[str]:
    tokens = []
    for token in _normalize(text).split():
        if token in SUPPORT_STOPWORDS:
            continue
        if len(token) <= 1 and not token.isdigit():
            continue
        tokens.append(_support_stem(token))
    return tokens


def _support_stem(token: str) -> str:
    if token.endswith("ies") and len(token) > 4:
        return token[:-3] + "y"
    if token.endswith("ing") and len(token) > 6:
        return token[:-3]
    if token.endswith("s") and len(token) > 4:
        return token[:-1]
    return token


def _matchable_phrases(phrase: str) -> list[str]:
    candidates = {_normalize(phrase)}
    for part in re.split(r"[.;:,]|\b(?:and|or|but|while|when|with|without|unless|if)\b", phrase, flags=re.I):
        normalized = _normalize(part)
        if len(normalized.split()) >= 3:
            candidates.add(normalized)
    return sorted(candidates, key=lambda part: (len(part.split()), len(part)), reverse=True)


def _normalize(text: str) -> str:
    lower = text.lower()
    spaced = lower.translate(str.maketrans({char: " " for char in string.punctuation}))
    return " ".join(spaced.split())


def _normalize_context(
    question_text: str | None,
    context: str | Iterable[str] | None,
) -> str:
    parts = []
    if question_text:
        parts.append(str(question_text))
    if isinstance(context, str):
        parts.append(context)
    elif context:
        parts.extend(str(item) for item in context)
    return _normalize(" ".join(parts))


def _as_string_list(value: Any) -> list[str]:
    if not value:
        return []
    if isinstance(value, str):
        return [value]
    return [str(item) for item in value if str(item).strip()]


def _as_mapping_list(value: Any) -> list[dict[str, Any]]:
    if not isinstance(value, list):
        return []
    return [dict(item) for item in value if isinstance(item, dict)]


def _unique_strings(values: Iterable[str]) -> list[str]:
    seen = set()
    result = []
    for value in values:
        text = str(value or "").strip()
        if not text or text in seen:
            continue
        seen.add(text)
        result.append(text)
    return result


def _support_phrase_records(unit: dict[str, Any]) -> list[dict[str, str]]:
    records: list[dict[str, str]] = []
    for field, basis in (
        ("required_first_actions", "card_required_action"),
        ("active_conditional_required_actions", "card_required_action"),
        ("first_actions", "card_first_action"),
        ("urgent_red_flags", "card_red_flag"),
        ("forbidden_advice", "card_negative_safety_instruction"),
        ("do_not", "card_negative_safety_instruction"),
    ):
        for phrase in _as_string_list(unit.get(field)):
            records.append(
                {
                    "basis": basis,
                    "card_id": str(unit.get("card_id") or ""),
                    "guide_id": str(unit.get("guide_id") or ""),
                    "phrase": phrase,
                }
            )
    return records
