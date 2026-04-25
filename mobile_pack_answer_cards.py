"""Answer-card table export helpers for Senku mobile packs."""

from __future__ import annotations

import json
from typing import Mapping, Sequence

from guide_answer_card_contracts import load_answer_cards


def prepare_answer_cards_for_mobile_pack(
    guide_by_id: Mapping[str, object], *, answer_cards: Sequence[dict] | None = None
) -> list[dict]:
    cards = load_answer_cards() if answer_cards is None else list(answer_cards)
    exportable_cards = []
    known_guide_ids = set(guide_by_id)
    for card in cards:
        guide_id = _safe_text(card.get("guide_id")).strip()
        if guide_id not in known_guide_ids:
            continue

        card_id = _safe_text(card.get("card_id")).strip()
        if not card_id:
            raise ValueError("Answer card with blank card_id cannot be exported")

        unknown_sources = sorted(
            {
                source_guide_id
                for source_guide_id in (
                    _safe_text(source.get("guide")).strip()
                    for source in _as_mapping_list(card.get("source_sections"))
                )
                if source_guide_id and source_guide_id not in known_guide_ids
            }
        )
        if unknown_sources:
            raise ValueError(
                f"Answer card {card_id} references unknown source guide ids: "
                f"{', '.join(unknown_sources)}"
            )
        exportable_cards.append(card)
    return exportable_cards


def insert_answer_cards(conn, answer_cards: Sequence[dict]) -> None:
    card_rows = []
    clause_rows = []
    source_rows = []
    for card in answer_cards:
        card_id = _safe_text(card.get("card_id")).strip()
        card_rows.append(
            (
                card_id,
                _safe_text(card.get("guide_id")).strip(),
                _safe_text(card.get("slug")).strip(),
                _safe_text(card.get("title")).strip(),
                _safe_text(card.get("risk_tier")).strip(),
                _safe_text(card.get("evidence_owner")).strip(),
                _safe_text(card.get("review_status")).strip(),
                _safe_text(card.get("runtime_citation_policy")).strip(),
                _safe_text(card.get("routine_boundary")).strip(),
                _safe_text(card.get("acceptable_uncertain_fit")).strip(),
                _safe_text(card.get("notes")).strip(),
            )
        )
        clause_rows.extend(_answer_card_clause_rows(card_id, card))
        source_rows.extend(_answer_card_source_rows(card_id, card))

    if card_rows:
        conn.executemany(
            """
            INSERT INTO answer_cards (
                card_id, guide_id, slug, title, risk_tier, evidence_owner,
                review_status, runtime_citation_policy, routine_boundary,
                acceptable_uncertain_fit, notes
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            card_rows,
        )
    if clause_rows:
        conn.executemany(
            """
            INSERT INTO answer_card_clauses (
                card_id, clause_kind, ordinal, text, trigger_terms_json
            ) VALUES (?, ?, ?, ?, ?)
            """,
            clause_rows,
        )
    if source_rows:
        conn.executemany(
            """
            INSERT INTO answer_card_sources (
                card_id, source_guide_id, slug, title, sections_json, is_primary
            ) VALUES (?, ?, ?, ?, ?, ?)
            """,
            source_rows,
        )
    conn.commit()


def _answer_card_clause_rows(card_id: str, card: Mapping[str, object]) -> list[tuple]:
    rows = []
    ordinals_by_kind = {}

    def append_clause(clause_kind, text, trigger_terms_json=None):
        ordinal = ordinals_by_kind.get(clause_kind, 0) + 1
        ordinals_by_kind[clause_kind] = ordinal
        rows.append((card_id, clause_kind, ordinal, text, trigger_terms_json))

    clause_fields = (
        ("required_first_actions", "required_first_action"),
        ("first_actions", "first_action"),
        ("urgent_red_flags", "urgent_red_flag"),
        ("forbidden_advice", "forbidden_advice"),
        ("do_not", "do_not"),
        ("acceptable_uncertain_fit", "acceptable_uncertain_fit"),
    )
    for field_name, clause_kind in clause_fields:
        for text in _as_string_list(card.get(field_name)):
            append_clause(clause_kind, text)

    for branch in _as_mapping_list(card.get("conditional_required_actions")):
        trigger_terms = _as_string_list(branch.get("trigger_terms"))
        trigger_terms_json = json.dumps(trigger_terms)
        for text in _as_string_list(branch.get("required_actions")):
            append_clause("conditional_required_action", text, trigger_terms_json)
    return rows


def _answer_card_source_rows(card_id: str, card: Mapping[str, object]) -> list[tuple]:
    rows = []
    primary_guide_id = _safe_text(card.get("guide_id")).strip()
    for source in _as_mapping_list(card.get("source_sections")):
        source_guide_id = _safe_text(source.get("guide")).strip()
        if not source_guide_id:
            continue
        rows.append(
            (
                card_id,
                source_guide_id,
                _safe_text(source.get("slug")).strip(),
                _safe_text(source.get("title")).strip(),
                json.dumps(_as_string_list(source.get("sections"))),
                1 if source_guide_id == primary_guide_id else 0,
            )
        )
    return rows


def _as_string_list(value) -> list[str]:
    if value is None:
        return []
    if isinstance(value, str):
        text = value.strip()
        return [text] if text else []
    if isinstance(value, (list, tuple, set)):
        return [
            str(item).strip()
            for item in value
            if str(item).strip()
        ]
    text = str(value).strip()
    return [text] if text else []


def _as_mapping_list(value) -> list[Mapping[str, object]]:
    if not isinstance(value, list):
        return []
    return [item for item in value if isinstance(item, dict)]


def _safe_text(value) -> str:
    return str(value or "")
