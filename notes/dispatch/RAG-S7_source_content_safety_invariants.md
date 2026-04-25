# Slice RAG-S7 - source-content safety invariants

- **Status:** initial validator support landed 2026-04-24; next work is invariant expansion across the remaining critical pilot cards.
- **Role:** main agent (`gpt-5.5 medium`; use high only for safety-critical guide edits).
- **Paste to:** after a diagnostic shows generated answers are faithfully following a contradictory or unsafe guide chunk.
- **Why this slice now:** the `EX` airway work proved that a RAG answer can be wrong because the retrieved guide text itself conflicts with a reviewed answer card or another safety guide. Fixing retrieval or prompts alone is not enough when the source chunk carries the bad instruction.

## Outcome

Create a repeatable source-content invariant check for safety-critical guide families.

## Current Implementation

- `notes/specs/guide_answer_card_schema.yaml` documents optional `source_invariants`.
- `guide_answer_card_contracts.py` preserves `source_invariants` for runtime/card tooling.
- `scripts/validate_guide_answer_cards.py` validates optional invariant blocks when present.
- The critical pilot cards now carry tiny source invariants:
  - `choking_airway_obstruction`: no blind finger sweeps; visible-object-only airway removal.
  - `poisoning_unknown_ingestion`: Poison Control / EMS first, airway first, no vomiting, no caustic charcoal drift.
  - `newborn_danger_sepsis`: first-28-days danger framing, fever/hypothermia escalation, and life-threatening cord-infection escalation.
  - `meningitis_sepsis_child`: meningitis/sepsis emergency ownership and first-hour sepsis priority text remain present.
  - `infected_wound_spreading_infection`: mark spreading redness, recheck every 2 hours, and red-streaking lymphangitis escalation.
  - `abdominal_internal_bleeding`: GI bleed/shock and ectopic internal-bleeding anchors; trauma-impact-specific invariants are intentionally skipped until GD-380 gets a crisp trauma source anchor.
- `tests/test_guide_answer_cards.py` covers satisfied invariants, source-content conflicts, malformed invariant blocks, invalid regexes, and the airway no-blind-sweep content guard.

Latest validation: `200` desktop tests passed; `scripts/validate_special_cases.py` validated `173` deterministic rules; `scripts/validate_guide_answer_cards.py` validated `6` cards.

## Candidate Invariants

- Choking / airway obstruction:
  - no blind finger sweeps;
  - remove only clearly visible objects;
  - do not use infant abdominal thrusts;
  - partial obstruction means encourage coughing and monitor, not immediate random thrusts.
- Poisoning / ingestion:
  - do not induce vomiting unless poison control/clinician directs it;
  - do not neutralize acids/bases with household remedies;
  - airway/breathing comes before product identification.
- Newborn danger signs:
  - fever, hypothermia, poor feeding, breathing trouble, weak cry, or hard-to-wake state is urgent evaluation, not watchful waiting.
- Meningitis / sepsis:
  - fever plus stiff neck, altered mental status, hard-to-wake behavior, or non-blanching rash requires urgent medical evaluation.

## Likely Touch Set

- `tests/test_guide_answer_cards.py` or a new `tests/test_guide_content_safety.py`
- `notes/specs/guide_answer_cards/*.yaml`
- relevant `guides/*.md`
- `ingest.py --files <edited guide>` after any guide edit

## Acceptance

- Each reviewed critical answer card can declare a small list of source-content invariants or forbidden source patterns.
- Tests fail if any relevant guide/source section contains a direct contradiction. Initial implementation done for choking/airway.
- Any guide edit is followed by focused re-ingest and a targeted bench proof.
- Analyzer reports distinguish `source_content_conflict` from retrieval/ranking/generation misses.

## Next Step

Next work is analyzer/reporting classification for `source_content_conflict` if a future invariant fails in a bench-backed source row. If a guide edit is needed, grep sibling guides/cards for the same invariant, edit the guide, re-ingest only the edited guide, and rerun the targeted bench/analyzer proof.

## Anti-Recommendations

- Do not make this a free-form medical rewrite lane.
- Do not edit safety-critical guide content without grepping sibling guides/cards for the shared invariant.
- Do not hide source contradictions with prompt-only bans when the guide itself needs correction.
