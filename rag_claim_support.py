from __future__ import annotations

import re
import string
from typing import Any, Iterable

ACTION_LINE_RE = re.compile(r"^\s*(?:(?:[-*]|\u2022)\s+|\d+[.)]\s+)(.+?)\s*$")
SENTENCE_SPLIT_RE = re.compile(r"(?<=[.!?])\s+")
GUIDE_ID_RE = re.compile(r"\bGD-\d+\b")
MAX_ACTION_TEXT = 180
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

IMPERATIVE_STARTS = {
    "avoid",
    "call",
    "check",
    "continue",
    "do",
    "don't",
    "dont",
    "encourage",
    "get",
    "give",
    "go",
    "keep",
    "lower",
    "move",
    "remove",
    "repeat",
    "seek",
    "start",
    "stop",
    "take",
    "use",
    "watch",
}


def diagnose_claim_support(
    answer_text: str,
    cards: Iterable[dict[str, Any]] | None,
    *,
    cited_guide_ids: Iterable[str] | None = None,
    expected_guide_ids: Iterable[str] | None = None,
    generated: bool = True,
) -> dict[str, Any]:
    """Return compact action-support diagnostics for a generated answer."""

    if not generated or not str(answer_text or "").strip():
        return _empty_result("no_generated_answer")

    card_list = list(cards or [])
    if not card_list:
        actions = extract_action_lines(answer_text)
        return _result_for_actions(actions, "no_cards")

    actions = extract_action_lines(answer_text)
    if not actions:
        return _empty_result("no_claims")

    guide_ids = _unique_strings([*(cited_guide_ids or []), *(expected_guide_ids or [])])
    diagnostics = [_diagnose_action(action, card_list, guide_ids) for action in actions]
    forbidden_count = sum(
        1 for item in diagnostics if item["support_basis"] == "card_forbidden_advice"
    )
    supported_count = sum(1 for item in diagnostics if item["supported"])
    unknown_count = sum(1 for item in diagnostics if item["support_basis"] == "unknown")

    if forbidden_count:
        status = "fail"
    elif supported_count == len(diagnostics):
        status = "pass"
    elif supported_count:
        status = "partial"
    else:
        status = "fail"

    return {
        "status": status,
        "claim_count": len(diagnostics),
        "action_count": len(diagnostics),
        "supported_count": supported_count,
        "unknown_count": unknown_count,
        "forbidden_count": forbidden_count,
        "actions": diagnostics,
    }


def extract_action_lines(answer_text: str) -> list[str]:
    actions: list[str] = []
    paragraphs = str(answer_text or "").splitlines()
    for raw_line in paragraphs:
        line = raw_line.strip()
        if not line:
            continue
        match = ACTION_LINE_RE.match(line)
        if match:
            _append_action(actions, match.group(1), preserve_raw=True)
            continue
        for sentence in SENTENCE_SPLIT_RE.split(line):
            sentence = sentence.strip()
            if _looks_like_short_action(sentence):
                _append_action(actions, sentence)
    return actions


def _diagnose_action(
    action: str,
    cards: list[dict[str, Any]],
    guide_ids: list[str],
) -> dict[str, Any]:
    raw_action = str(action or "")
    normalized_action = _normalize(raw_action)
    prohibited = _first_prohibited_match(normalized_action, cards)
    if prohibited:
        return _action_record(
            raw_action,
            "card_forbidden_advice",
            prohibited,
            supported=False,
        )

    for field, basis in (
        ("required_first_actions", "card_required_action"),
        ("first_actions", "card_first_action"),
        ("urgent_red_flags", "card_red_flag"),
        ("forbidden_advice", "card_negative_safety_instruction"),
        ("do_not", "card_negative_safety_instruction"),
    ):
        match = _first_phrase_match(normalized_action, cards, field)
        if match:
            return _action_record(raw_action, basis, match, supported=True)

    line_guide_ids = set(GUIDE_ID_RE.findall(raw_action))
    if line_guide_ids & set(guide_ids):
        return _action_record(
            raw_action,
            "retrieved_or_cited_owner",
            {"phrase": "|".join(sorted(line_guide_ids & set(guide_ids)))},
            supported=True,
        )

    return _action_record(action, "unknown", {}, supported=False)


def _first_phrase_match(
    normalized_action: str,
    cards: list[dict[str, Any]],
    field: str,
) -> dict[str, str]:
    for card in cards:
        for phrase in _as_string_list(card.get(field)):
            target = _find_match(normalized_action, phrase)
            if target:
                return {
                    "card_id": str(card.get("card_id") or ""),
                    "guide_id": str(card.get("guide_id") or ""),
                    "phrase": phrase,
                    "matched_text": target,
                }
    return {}


def _first_prohibited_match(
    normalized_action: str,
    cards: list[dict[str, Any]],
) -> dict[str, str]:
    action_tokens = normalized_action.split()
    for card in cards:
        for phrase in [
            *_as_string_list(card.get("forbidden_advice")),
            *_as_string_list(card.get("do_not")),
        ]:
            for target in _forbidden_action_targets(phrase):
                if _has_unnegated_token_match(action_tokens, target.split()):
                    return {
                        "card_id": str(card.get("card_id") or ""),
                        "guide_id": str(card.get("guide_id") or ""),
                        "phrase": phrase,
                        "matched_text": target,
                    }
    return {}


def _find_match(normalized_action: str, phrase: str) -> str:
    for candidate in _matchable_phrases(phrase):
        if candidate and candidate in normalized_action:
            return candidate
    return _overlap_match(normalized_action, phrase)


def _overlap_match(normalized_action: str, phrase: str) -> str:
    action_tokens = set(_support_tokens(normalized_action))
    phrase_tokens = _support_tokens(phrase)
    if len(phrase_tokens) < 3:
        return ""
    overlap = [token for token in phrase_tokens if token in action_tokens]
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
    for part in re.split(
        r"[.;:,]|\b(?:and|or|but|while|when|with|without|unless|if)\b",
        phrase,
        flags=re.I,
    ):
        normalized = _normalize(part)
        if len(normalized.split()) >= 3:
            candidates.add(normalized)
    return sorted(candidates, key=lambda part: (len(part.split()), len(part)), reverse=True)


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


def _has_unnegated_token_match(action_tokens: list[str], target_tokens: list[str]) -> bool:
    if not action_tokens or not target_tokens:
        return False
    last_start = len(action_tokens) - len(target_tokens)
    for start in range(last_start + 1):
        if action_tokens[start : start + len(target_tokens)] != target_tokens:
            continue
        if _is_negated(action_tokens, start):
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


def _looks_like_short_action(sentence: str) -> bool:
    words = _normalize(sentence).split()
    if not words or len(words) > 16:
        return False
    first = words[0]
    return first in IMPERATIVE_STARTS or (first == "do" and len(words) > 1 and words[1] == "not")


def _append_action(actions: list[str], value: str, *, preserve_raw: bool = False) -> None:
    text = " ".join(str(value or "").split())
    action = text if preserve_raw else _compact_text(text)
    if action and action not in actions:
        actions.append(action)


def _action_record(
    action: str,
    basis: str,
    match: dict[str, str],
    *,
    supported: bool,
) -> dict[str, Any]:
    return {
        "text": _compact_text(action),
        "support_basis": basis,
        "supported": supported,
        "card_id": match.get("card_id", ""),
        "guide_id": match.get("guide_id", ""),
        "matched_phrase": _compact_text(match.get("phrase", "")),
    }


def _result_for_actions(actions: list[str], status: str) -> dict[str, Any]:
    diagnostics = [
        _action_record(action, "unknown", {}, supported=False)
        for action in actions
    ]
    return {
        "status": status,
        "claim_count": len(diagnostics),
        "action_count": len(diagnostics),
        "supported_count": 0,
        "unknown_count": len(diagnostics),
        "forbidden_count": 0,
        "actions": diagnostics,
    }


def _empty_result(status: str) -> dict[str, Any]:
    return {
        "status": status,
        "claim_count": 0,
        "action_count": 0,
        "supported_count": 0,
        "unknown_count": 0,
        "forbidden_count": 0,
        "actions": [],
    }


def _compact_text(value: str) -> str:
    text = " ".join(str(value or "").split())
    if len(text) <= MAX_ACTION_TEXT:
        return text
    return text[: MAX_ACTION_TEXT - 3].rstrip() + "..."


def _normalize(text: str) -> str:
    lower = str(text or "").lower()
    table = str.maketrans({char: " " for char in string.punctuation})
    return " ".join(lower.translate(table).split())


def _as_string_list(value: Any) -> list[str]:
    if not value:
        return []
    if isinstance(value, str):
        return [value]
    return [str(item) for item in value if str(item).strip()]


def _unique_strings(values: Iterable[str]) -> list[str]:
    seen = set()
    result = []
    for value in values:
        text = str(value or "").strip()
        if text and text not in seen:
            seen.add(text)
            result.append(text)
    return result
