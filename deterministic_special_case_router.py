"""Pure deterministic special-case rule resolution and selection helpers."""

from dataclasses import dataclass
from typing import Callable

from query_routing_text import text_has_marker as _text_has_marker


@dataclass(frozen=True)
class DeterministicSpecialCaseRule:
    """Deterministic control-path rule with a canonical validation prompt."""

    rule_id: str
    predicate: Callable[[str], bool]
    builder: Callable[..., str] | None
    sample_prompt: str
    builder_name: str
    priority: int
    promotion_status: str
    promotion_notes: str | None
    lexical_signature_terms: tuple[str, ...]


def _build_deterministic_builder_missing_debug_note(rule_id, builder_name):
    """Render the debug-only fallback note for missing deterministic builders."""
    return (
        f"Debug note: deterministic rule '{rule_id}' matched, but builder "
        f"'{builder_name}' is unavailable; falling back to retrieval."
    )


def _namespace_lookup(namespace, name):
    if isinstance(namespace, dict):
        return namespace.get(name)
    return getattr(namespace, name, None)


def _resolve_deterministic_special_case_rules(
    specs,
    *,
    predicate_namespaces,
    builder_namespace,
):
    """Build live deterministic rule objects from declarative registry specs."""
    rules = []
    for spec in specs:
        predicate = None
        for namespace in predicate_namespaces:
            predicate = _namespace_lookup(namespace, spec.predicate_name)
            if predicate is not None:
                break
        if predicate is None:
            raise RuntimeError(
                f"Deterministic special-case registry references unknown symbol "
                f"{spec.predicate_name!r} for rule {spec.rule_id!r}"
            )
        builder = _namespace_lookup(builder_namespace, spec.builder_name)
        rules.append(
            DeterministicSpecialCaseRule(
                spec.rule_id,
                predicate,
                builder,
                spec.sample_prompt,
                spec.builder_name,
                spec.priority,
                spec.promotion_status,
                spec.promotion_notes,
                spec.lexical_signature_terms,
            )
        )
    return tuple(rules)


_ACTIVE_DETERMINISTIC_SEMANTIC_EXCLUSION_MARKERS = {
    "generic_puncture": (
        "animal",
        "bite",
        "bit",
        "bit me",
        "bitten",
        "dog",
        "cat",
        "face",
        "joint",
        "mouth",
        "infected",
        "infection",
        "fever",
        "extract",
        "remove",
        "severe bleeding",
        "uncontrolled bleeding",
        "hemorrhage",
        "spurting",
    ),
    "charcoal_sand_water_filter_starter": (),
    "reused_container_water": (
        "bleach",
        "fuel",
        "solvent",
        "solvents",
        "pesticide",
        "pesticides",
        "paint",
        "rust",
        "rusty",
        "chemical",
        "chemicals",
        "barrel",
        "drum",
        "metal drum",
        "steel barrel",
        "detergent",
        "cleaner",
        "cleaning chemical",
        "cleaning detergent",
    ),
    "water_without_fuel": (
        "sunlight",
        "well is contaminated",
        "the well is contaminated",
        "people are sick",
        "two people are sick",
        "wounded",
        "wounded person",
    ),
    "fire_in_rain": (
        "keep a fire going",
        "stay warm",
        "without starting a fire",
    ),
    "weld_without_welder_starter": (
        "screws and bolts",
        "wood",
        "wooden",
        "boards",
    ),
    "metal_splinter": (
        "eye",
        "eyeball",
        "vision",
        "severe bleeding",
        "uncontrolled bleeding",
        "hemorrhage",
        "spurting",
    ),
    "candles_for_light": (
        "buy candles",
        "burn for",
        "burn time",
        "how long do candles burn",
    ),
    "glassmaking_starter": (
        "make a glass",
        "repair",
        "cracked",
        "bottle",
    ),
}


def _passes_deterministic_semantic_gate(question, rule):
    """Apply rule-specific exclusion checks for active Android-promoted rules."""
    if rule.promotion_status != "active":
        return True
    exclusion_markers = _ACTIVE_DETERMINISTIC_SEMANTIC_EXCLUSION_MARKERS.get(
        rule.rule_id
    )
    if exclusion_markers is None:
        return True
    return not _text_has_marker(question, exclusion_markers)


def _lexical_signature_size(rule):
    """Return the explicit lexical signature size used for equal-priority ties."""
    return len(rule.lexical_signature_terms)


def _select_deterministic_special_case_rule(
    matches,
    *,
    log_first_defined_tie,
    warn_event=None,
):
    """Pick one deterministic rule using priority, lexical signature, then order."""
    if not matches:
        return None, None

    winning_priority = max(rule.priority for rule in matches)
    priority_matches = [
        rule for rule in matches if rule.priority == winning_priority
    ]
    if len(priority_matches) == 1:
        return priority_matches[0], "priority"

    winning_signature_size = max(
        _lexical_signature_size(rule) for rule in priority_matches
    )
    signature_matches = [
        rule
        for rule in priority_matches
        if _lexical_signature_size(rule) == winning_signature_size
    ]
    if len(signature_matches) == 1:
        return signature_matches[0], "lexical_signature"

    winner = signature_matches[0]
    if log_first_defined_tie and warn_event is not None:
        warn_event(
            "deterministic_priority_tie",
            lexical_signature_size=winning_signature_size,
            priority=winning_priority,
            tied_rule_ids=",".join(rule.rule_id for rule in signature_matches),
            winner_rule_id=winner.rule_id,
        )
    return winner, "first_defined"


def get_deterministic_special_case_overlaps(rules):
    """Return canonical-prompt overlap records for deterministic predicates."""
    overlaps = []
    for source_rule in rules:
        matches = [
            rule
            for rule in rules
            if rule.predicate(source_rule.sample_prompt)
        ]
        if len(matches) < 2:
            continue
        winner, winner_reason = _select_deterministic_special_case_rule(
            matches,
            log_first_defined_tie=False,
        )
        overlaps.append(
            {
                "source_rule_id": source_rule.rule_id,
                "sample_prompt": source_rule.sample_prompt,
                "matches": [
                    {
                        "rule_id": rule.rule_id,
                        "lexical_signature_size": _lexical_signature_size(rule),
                        "priority": rule.priority,
                        "promotion_status": rule.promotion_status,
                    }
                    for rule in matches
                ],
                "winner_reason": winner_reason,
                "winner_rule_ids": [winner.rule_id] if winner is not None else [],
            }
        )
    return overlaps
