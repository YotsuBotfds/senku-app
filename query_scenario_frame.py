"""Scenario-frame and structured session-state helpers for query.py.

This module is intentionally dependency-light. Callers inject repo-local
classification callbacks rather than importing the heavier query module.
"""

import re


_GENERIC_QUERY_FILLER_WORDS = {
    "a",
    "an",
    "and",
    "are",
    "can",
    "could",
    "do",
    "does",
    "did",
    "for",
    "how",
    "i",
    "if",
    "in",
    "is",
    "it",
    "me",
    "my",
    "of",
    "should",
    "someone",
    "something",
    "that",
    "the",
    "this",
    "to",
    "we",
    "what",
    "when",
    "where",
    "why",
    "you",
}

_SCENARIO_STOPWORDS = _GENERIC_QUERY_FILLER_WORDS | {
    "all",
    "also",
    "around",
    "back",
    "before",
    "from",
    "into",
    "just",
    "more",
    "need",
    "now",
    "out",
    "over",
    "please",
    "still",
    "than",
    "then",
    "there",
    "through",
    "too",
    "up",
    "very",
}

_INVENTORY_LEAD_WORDS = {
    "a",
    "an",
    "one",
    "two",
    "three",
    "four",
    "five",
    "half-full",
    "half",
    "full",
    "empty",
    "spare",
    "single",
    "extra",
    "some",
    "several",
}

_INVENTORY_NEGATION_WORDS = {
    "no",
    "not",
    "without",
    "broken",
    "empty",
    "missing",
    "contaminated",
}

_CONSTRAINT_MARKERS = (
    "no ",
    "without ",
    "limited ",
    "only ",
    "can't ",
    "cannot ",
    "won't ",
    "nearest ",
    "before ",
    "getting dark",
    "below freezing",
    "running low",
    "collapsed",
    "contaminated",
    "destroyed",
    "failed",
    "approaching",
)

_HAZARD_MARKERS = {
    "bleeding": "bleeding",
    "wounded": "injury",
    "injured": "injury",
    "unconscious": "unconscious patient",
    "vomiting": "vomiting",
    "throwing up": "vomiting",
    "seizure": "seizure",
    "poisoning": "poisoning",
    "snake bite": "snake bite",
    "bite": "bite injury",
    "contaminated": "contamination",
    "wildfire": "wildfire",
    "flood": "flooding",
    "storm": "storm damage",
    "hurricane": "storm damage",
    "monsoon": "heavy rain",
    "freezing": "freezing exposure",
    "cold": "cold exposure",
    "wet": "wet exposure",
    "dark": "darkness",
    "fire": "fire risk",
    "smoke": "smoke exposure",
    "armed": "security threat",
    "strangers": "security threat",
}

_PEOPLE_PATTERNS = (
    r"\b\d+\s+people\b",
    r"\b\d+\s+survivors\b",
    r"\bpregnant woman\b",
    r"\bwounded person\b",
    r"\bunconscious\b",
    r"\bdehydrated child\b",
    r"\bchild\b",
    r"\bkid\b",
    r"\badult\b",
    r"\bgroup\b",
    r"\bperson\b",
    r"\bpeople\b",
)

_ENVIRONMENT_MARKERS = (
    "storm",
    "rain",
    "wildfire",
    "flood",
    "valley",
    "monsoon",
    "winter",
    "freezing",
    "cold",
    "heat",
    "night",
    "dark",
    "river",
    "well",
    "camp",
)

_QUERY_VERBS = {
    "is",
    "are",
    "was",
    "were",
    "have",
    "has",
    "had",
    "need",
    "want",
    "cant",
    "wont",
    "should",
    "must",
    "do",
    "does",
    "did",
    "dont",
    "build",
    "make",
    "fix",
    "treat",
    "find",
    "move",
    "get",
    "start",
    "stop",
    "help",
    "keep",
    "run",
    "go",
    "set",
    "cut",
    "dig",
    "kill",
    "eat",
    "drink",
    "boil",
    "burn",
    "use",
    "collect",
    "preserve",
    "protect",
    "repair",
    "filter",
    "purify",
    "evacuate",
    "survive",
    "lost",
    "broke",
    "broken",
    "collapsed",
    "failed",
    "spreading",
    "approaching",
    "trapped",
    "contaminated",
    "destroyed",
    "injured",
    "bleeding",
    "dying",
    "sick",
}

_MEANINGFUL_QUERY_VERBS = _QUERY_VERBS - {
    "is",
    "are",
    "was",
    "were",
    "have",
    "has",
    "had",
    "need",
    "want",
    "do",
    "does",
    "did",
}

_SESSION_CONTEXT_HINTS = (
    "what now",
    "what next",
    "and then",
    "what about",
    "how long",
    "should we",
    "do we",
    "next step",
    "next steps",
)

_ANCHOR_RESET_MARKERS = (
    "unrelated:",
    "new question:",
    "switching topics:",
    "different question:",
)


def _empty_domains(_text):
    return set()


def _empty_restarts(_question):
    return []


def _unique_ordered(items):
    """Return non-empty strings in first-seen order."""
    seen = set()
    ordered = []
    for item in items:
        if item is None:
            continue
        normalized = item.strip()
        if not normalized:
            continue
        key = normalized.lower()
        if key in seen:
            continue
        seen.add(key)
        ordered.append(normalized)
    return ordered


def _is_generic_asset_placeholder(text):
    """Return True for vague placeholder phrases that are not real assets."""
    normalized = " ".join(re.findall(r"[a-z0-9']+", (text or "").lower()))
    return normalized in {
        "what i have",
        "what we have",
        "what ive got",
        "what we've got",
        "what weve got",
        "what we got",
        "what i got",
        "what i ve got",
        "what we ve got",
    }


def _split_scenario_clauses(question, *, split_at_question_restart=None):
    """Split a scenario prompt into meaningful user-written clauses."""
    restart_splitter = split_at_question_restart or _empty_restarts
    comma_parts = [p.strip() for p in question.split(",")]
    comma_parts = [
        re.sub(r"^(?:and|but|or|also)\s+", "", p).strip() for p in comma_parts
    ]
    restart_parts = [
        re.sub(r"^(?:and|but|or|also)\s+", "", p).strip()
        for p in restart_splitter(question)
    ]
    return _unique_ordered(
        [part for part in comma_parts + restart_parts if len(part.split()) >= 2]
    )


def _content_tokens(text):
    """Extract lightweight content tokens for overlap checks."""
    return {
        token
        for token in re.findall(r"[a-z0-9-]+", text.lower())
        if len(token) >= 3 and token not in _SCENARIO_STOPWORDS
    }


def _extract_deadline(question):
    """Return a compact deadline/time-pressure phrase when present."""
    patterns = (
        r"\bin\s+(\d+\s+(?:minutes?|hours?|days?|weeks?))\b",
        r"\b(\d+\s+(?:minutes?|hours?|days?|weeks?)\s+walk)\b",
        r"\b(before dark)\b",
        r"\b(tonight|tomorrow|today)\b",
        r"\bstarts in\s+(\d+\s+(?:hours?|days?|weeks?))\b",
    )
    lower = question.lower()
    for pattern in patterns:
        match = re.search(pattern, lower)
        if match:
            return match.group(1)
    return None


def _looks_like_inventory_fragment(clause):
    """Return True for resource inventory fragments that should stay context-only."""
    stripped = clause.lower().strip()
    words = re.findall(r"[a-z0-9-]+", stripped)
    if not words:
        return False

    if re.match(r"^(?:we|i)\s+(?:have|got)\b", stripped):
        if set(words) & _INVENTORY_NEGATION_WORDS:
            return False
        return not bool(set(words) & _MEANINGFUL_QUERY_VERBS)

    if len(words) <= 4 and words[0] in _INVENTORY_LEAD_WORDS:
        return not bool(set(words) & _MEANINGFUL_QUERY_VERBS)

    return False


def _is_query_bearing(clause, *, detect_domains=None):
    """Return True when a clause is an information need, not only context."""
    domain_detector = detect_domains or _empty_domains
    if _looks_like_inventory_fragment(clause):
        return False

    words = set(clause.lower().split())
    if words & _QUERY_VERBS:
        return True
    return len(domain_detector(clause)) > 0


def _extract_assets(clauses):
    """Return user-stated assets/resources for the current turn."""
    assets = []
    for clause in clauses:
        lower = clause.lower()
        cleaned = clause.strip(" .")
        if re.match(r"^(?:we|i)\s+(?:have|got)\b", lower):
            asset = re.sub(
                r"^(?:we|i)\s+(?:have|got)\s+", "", cleaned, flags=re.IGNORECASE
            )
            candidate = asset or cleaned
            if not _is_generic_asset_placeholder(candidate):
                assets.append(candidate)
            continue
        if _looks_like_inventory_fragment(clause):
            if not _is_generic_asset_placeholder(cleaned):
                assets.append(cleaned)
            continue
        match = re.search(r"\bwith\s+([^,.]+)", clause, flags=re.IGNORECASE)
        if match:
            candidate = match.group(1).strip()
            if not _is_generic_asset_placeholder(candidate):
                assets.append(candidate)
    return _unique_ordered(assets)


def _extract_constraints(clauses, deadline):
    """Return user-stated constraints or limiting factors."""
    constraints = []
    for clause in clauses:
        lower = clause.lower()
        if any(marker in lower for marker in _CONSTRAINT_MARKERS):
            constraints.append(clause.strip(" ."))
    if deadline:
        constraints.append(f"time pressure: {deadline}")
    return _unique_ordered(constraints)


def _extract_hazards(question):
    """Return salient hazards called out by the user."""
    lower = question.lower()
    hazards = [label for marker, label in _HAZARD_MARKERS.items() if marker in lower]
    return _unique_ordered(hazards)


def _extract_people(question):
    """Return people/patient descriptors from the current turn."""
    people = []
    lower = question.lower()
    for pattern in _PEOPLE_PATTERNS:
        for match in re.finditer(pattern, lower):
            people.append(match.group(0))
    return _unique_ordered(people)


def _extract_environment(question):
    """Return environment/context signals from the question."""
    lower = question.lower()
    return _unique_ordered(
        [marker for marker in _ENVIRONMENT_MARKERS if marker in lower]
    )


def _derive_objectives(question, clauses, *, detect_domains=None):
    """Return objective clauses for coverage tracking and review."""
    domain_detector = detect_domains or _empty_domains
    objectives = []
    for clause in clauses:
        if _looks_like_inventory_fragment(clause):
            continue
        if _is_query_bearing(clause, detect_domains=domain_detector) or domain_detector(
            clause
        ):
            objective_domains = sorted(domain_detector(clause))
            objectives.append(
                {
                    "text": clause.strip(),
                    "domains": objective_domains,
                    "tokens": sorted(_content_tokens(clause)),
                }
            )
    if not objectives:
        if _looks_like_inventory_fragment(question):
            return []
        objectives.append(
            {
                "text": question.strip(),
                "domains": sorted(domain_detector(question)),
                "tokens": sorted(_content_tokens(question)),
            }
        )

    question_domains = sorted(domain_detector(question))
    for objective in objectives:
        if not objective["domains"]:
            objective["domains"] = question_domains
    return objectives


def build_scenario_frame(
    question,
    *,
    detect_domains=None,
    split_at_question_restart=None,
    safety_critical_callback=None,
):
    """Parse user-facing scenario structure without changing decomposition."""
    domain_detector = detect_domains or _empty_domains
    clauses = _split_scenario_clauses(
        question, split_at_question_restart=split_at_question_restart
    )
    deadline = _extract_deadline(question)
    frame = {
        "question": question,
        "clauses": clauses,
        "domains": sorted(domain_detector(question)),
        "objectives": _derive_objectives(
            question, clauses, detect_domains=domain_detector
        ),
        "assets": _extract_assets(clauses),
        "constraints": _extract_constraints(clauses, deadline),
        "hazards": _extract_hazards(question),
        "people": _extract_people(question),
        "environment": _extract_environment(question),
        "deadline": deadline,
    }
    if safety_critical_callback:
        frame["safety_critical"] = safety_critical_callback(frame)
    else:
        frame["safety_critical"] = False
    return frame


def empty_session_state():
    """Return the default structured session state."""
    return {
        "assets": [],
        "constraints": [],
        "hazards": [],
        "people": [],
        "environment": [],
        "deadline": None,
        "active_objectives": [],
        "anchor_guide_id": "",
        "anchor_turn_index": None,
        "turn_count": 0,
    }


def _copy_session_state(session_state):
    """Return a detached copy of the structured session state."""
    state = empty_session_state()
    if not session_state:
        return state
    for key in state:
        value = session_state.get(key)
        if isinstance(state[key], list):
            state[key] = list(value or [])
        else:
            state[key] = value
    return state


def merge_frame_with_session(frame, session_state):
    """Merge current-turn structure onto prior session context for prompting/review."""
    merged = dict(frame)
    state = _copy_session_state(session_state)
    for key in ("assets", "constraints", "hazards", "people", "environment"):
        merged[key] = _unique_ordered(state.get(key, []) + frame.get(key, []))
    merged["deadline"] = frame.get("deadline") or state.get("deadline")
    merged["session_active_objectives"] = list(state.get("active_objectives", []))
    return merged


def update_session_state(session_state, frame):
    """Persist user-provided scenario facts into structured session state."""
    state = _copy_session_state(session_state)
    frame = frame or {}
    for key in ("assets", "constraints", "hazards", "people", "environment"):
        state[key] = _unique_ordered(state[key] + frame.get(key, []))[:12]
    if frame.get("deadline"):
        state["deadline"] = frame["deadline"]
    if frame.get("objectives"):
        state["active_objectives"] = _unique_ordered(
            [
                obj.get("text")
                for obj in frame["objectives"]
                if isinstance(obj, dict)
            ]
            + state["active_objectives"]
        )[:8]
    return state


def _session_state_is_empty(session_state):
    """Return True when the structured session state has no stored facts."""
    state = session_state or {}
    return not any(
        state.get(key)
        for key in (
            "assets",
            "constraints",
            "hazards",
            "people",
            "environment",
            "active_objectives",
        )
    ) and not state.get("deadline")


def _render_session_state_text(session_state):
    """Render session state as compact text for prompts/debug."""
    if _session_state_is_empty(session_state):
        return ""

    state = session_state or {}
    parts = []
    if state.get("active_objectives"):
        parts.append("active objectives: " + "; ".join(state["active_objectives"][:4]))
    if state.get("people"):
        parts.append("people: " + ", ".join(state["people"][:4]))
    if state.get("assets"):
        parts.append("assets: " + ", ".join(state["assets"][:4]))
    if state.get("constraints"):
        parts.append("constraints: " + "; ".join(state["constraints"][:4]))
    if state.get("hazards"):
        parts.append("hazards: " + ", ".join(state["hazards"][:4]))
    if state.get("deadline"):
        parts.append("deadline: " + state["deadline"])
    return " | ".join(parts)


def _should_use_session_context(question, frame, session_state):
    """Return True when retrieval should widen a vague follow-up using session state."""
    if _session_state_is_empty(session_state):
        return False
    lower = question.lower().strip()
    if any(lower.startswith(marker) for marker in _ANCHOR_RESET_MARKERS):
        return False
    if any(hint in lower for hint in _SESSION_CONTEXT_HINTS):
        return True
    return len(_content_tokens(question)) <= 4 and not frame.get("domains")
