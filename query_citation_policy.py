"""Citation owner-priority helpers for query prompt contracts."""

from __future__ import annotations


def _guide_id_in(guide_id, guide_ids):
    try:
        return guide_id in guide_ids
    except TypeError:
        return False


def _prioritized_citation_allowlist_for_question(
    question,
    guide_ids,
    *,
    is_airway_obstruction_rag_query,
    is_meningitis_rash_emergency_query,
    is_meningitis_vs_viral_query,
    is_newborn_sepsis_danger_query,
    is_abdominal_trauma_danger_query,
):
    """Put retrieved target owners first for narrow safety-owner conflicts."""
    if not question or not guide_ids:
        return guide_ids

    owner_priority = []
    if is_airway_obstruction_rag_query(question):
        owner_priority.extend(["GD-232", "GD-579", "GD-298", "GD-284", "GD-617"])
    if is_meningitis_rash_emergency_query(question):
        owner_priority.extend(["GD-589", "GD-284", "GD-298", "GD-232", "GD-235", "GD-268", "GD-949"])
    elif is_meningitis_vs_viral_query(question):
        owner_priority.extend(["GD-589", "GD-284", "GD-232", "GD-235", "GD-268", "GD-298", "GD-949"])
    if is_newborn_sepsis_danger_query(question):
        owner_priority.extend(["GD-284", "GD-492", "GD-298", "GD-617", "GD-589"])
    if is_abdominal_trauma_danger_query(question):
        owner_priority.extend(["GD-380", "GD-297", "GD-232"])
    if not owner_priority:
        return guide_ids

    priority_index = {guide_id: index for index, guide_id in enumerate(owner_priority)}
    guide_ids_with_index = list(enumerate(guide_ids))

    ordered_guide_ids_with_index = sorted(
        guide_ids_with_index,
        key=lambda guide_id_with_index: (
            0 if _guide_id_in(guide_id_with_index[1], priority_index) else 1,
            priority_index.get(guide_id_with_index[1], guide_id_with_index[0])
            if _guide_id_in(guide_id_with_index[1], priority_index)
            else guide_id_with_index[0],
            guide_id_with_index[0],
        ),
    )
    return [guide_id for _index, guide_id in ordered_guide_ids_with_index]


def _citation_guide_ids_for_question(
    question,
    results,
    raw_allowed_guide_ids,
    *,
    prioritized_citation_allowlist_for_question,
    is_airway_obstruction_rag_query,
    has_allergy_or_anaphylaxis_trigger,
    airway_obstruction_allowed_guide_ids_from_results,
    is_meningitis_vs_viral_query,
    is_meningitis_rash_emergency_query,
):
    """Return the retrieved guide IDs allowed for this prompt's citation contract."""
    allowed_guide_ids = prioritized_citation_allowlist_for_question(
        question, raw_allowed_guide_ids
    )
    if (
        question
        and is_airway_obstruction_rag_query(question)
        and not has_allergy_or_anaphylaxis_trigger(question)
    ):
        airway_owner_ids = {"GD-232", "GD-579", "GD-298", "GD-284", "GD-617"}
        airway_allowed_ids = airway_obstruction_allowed_guide_ids_from_results(results)
        if not airway_allowed_ids:
            airway_allowed_ids = [
                guide_id for guide_id in allowed_guide_ids if _guide_id_in(guide_id, airway_owner_ids)
            ]
        if airway_allowed_ids:
            allowed_guide_ids = airway_allowed_ids
    if (
        question
        and is_meningitis_vs_viral_query(question)
        and not is_meningitis_rash_emergency_query(question)
    ):
        meningitis_compare_owner_ids = {"GD-589", "GD-284", "GD-232", "GD-235", "GD-268"}
        owner_allowed_ids = [
            guide_id
            for guide_id in allowed_guide_ids
            if _guide_id_in(guide_id, meningitis_compare_owner_ids)
        ]
        if owner_allowed_ids:
            allowed_guide_ids = owner_allowed_ids
    return allowed_guide_ids
