# Slice RAG-S2 - guide answer cards schema spike

- **Status:** pilot exists 2026-04-24. Schema is at `notes/specs/guide_answer_card_schema.yaml`; `6` pilot cards live under `notes/specs/guide_answer_cards/`; local YAML / required-field validation passed.
- **Role:** main agent (`gpt-5.5 medium`; use high only for safety-critical medical card review).
- **Paste to:** after `RAG-S1` lands or after its diagnostic report identifies answer-contract misses as a top bucket.
- **Why this slice now:** deterministic builders are becoming structured guide summaries in code. Move that structure into reviewable guide answer cards derived from the guide corpus.

## Outcome

Define a schema and create a small reviewed pilot set of guide answer cards for high-risk families.

## Candidate Pilot Families

- poisoning / unknown ingestion
- choking / airway obstruction
- stroke / TIA / cardiac overlap
- meningitis / sepsis
- newborn danger signs
- infected wounds

## Likely Touch Set

- new schema under `notes/specs/guide_answer_card_schema.yaml`
- pilot cards under `notes/specs/guide_answer_cards/` or another agreed path
- optional validator script if cheap

## Card Fields

- `guide_id`
- `slug`
- `title`
- `applies_when`
- `first_actions`
- `urgent_red_flags`
- `do_not`
- `routine_boundary`
- `clarifying_questions`
- `citation_ids`
- `source_sections`
- `review_status`

## Acceptance

- Schema exists and is documented. Done: `notes/specs/guide_answer_card_schema.yaml`.
- 5-10 pilot cards exist for safety-critical guide families. Done: `6` cards under `notes/specs/guide_answer_cards/`.
- Cards cite source guide IDs/sections.
- YAML / required-field validation passes locally.
- A short note explains how cards will be ingested or passed to generation in a later slice.

## Current Use

Use the pilot as a reviewed answer-card/evidence-owner contract input for runtime generation and `S5a` diagnostics. The 2026-04-24 `14:10` proof (`artifacts/bench/rag_diagnostics_20260424_1410_child_choking_gate/report.md`) proves runtime answer-card injection for reviewed owner cards and supporting `source_sections` with a token guard: `24` rows, zero retrieval/ranking/generation/safety-contract misses, answer cards `15` no-generated-answer / `7` partial / `2` pass, and claim support `15` no-generated-answer / `9` pass. The loader now preserves `source_sections`, and prompt card blocks include `do_not` clauses. Next work is card-clause normalization and reviewed expansion, not broad unreviewed extraction.

The 2026-04-24 `14:55` card-clause slice normalized two strict contracts without changing retrieval:

- choking adult/infant mechanics are conditional, while the universal airway boundary and collapse/CPR behavior stay required;
- meningitis/sepsis keeps emergency escalation required, while first-hour sepsis wording is conditional on explicit sepsis/shock/very-sick framing;
- newborn prompt guidance now requires keeping the newborn warm while arranging urgent evaluation;
- `source_invariants` is preserved by the loader and validated when present.

Validation for this slice: `200` desktop unit tests passed, `scripts/validate_special_cases.py` validated `173` rules, `scripts/validate_guide_answer_cards.py` validated `6` cards, and `artifacts/bench/rag_diagnostics_20260424_1455_card_clause_invariants/report.md` preserved the `14:10` bucket counts.

Next `RAG-S2` work should continue with answer-card clause precision and evidence-unit preparation for `RAG-S8`. Do not treat remaining generated-row `partial` card status as a retrieval/ranking defect unless the analyzer reopens those buckets.

## Anti-Recommendations

- Do not let cards introduce advice absent from guides.
- Do not wire cards into runtime in this slice.
- Do not create cards for every guide before proving value on the pilot.
