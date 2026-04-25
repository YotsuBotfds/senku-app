# RAG-S11 Reviewed Source-Family Runtime

## Slice

RAG-S11 - reviewed source-family runtime coverage for newborn danger signs.

## Role

Main agent with scout support. Default `gpt-5.5 medium`; use high only for
delicate safety-ownership review.

## Preconditions

- `RAG-S9` card-backed runtime remains opt-in behind
  `SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1`.
- `RAG-S10` provenance labels are available in bench/analyzer artifacts.
- Reviewed cards must keep true evidence gaps separate from retrieval/citation
  surfacing work.

## Outcome

The newborn danger-sign reviewed card now safely covers reviewed source-family
retrievals where the primary `GD-284` card owner is not in the top citation
set, without globally allowing backup-owner citations for every card.

Implemented:

- `guide_answer_card_contracts.py` loads optional `runtime_citation_policy`.
- `compose_card_backed_answer(...)` still defaults to primary-owner citations,
  but a card with `runtime_citation_policy: reviewed_source_family` may cite the
  first retrieved guide from its reviewed source sections.
- `query.py` adds targeted source-family card prioritization for:
  - choking prompts without allergy triggers;
  - newborn danger-sign/sepsis prompts;
  - red-flag meningitis prompts only.
- `query.py` keeps ambiguous meningitis-vs-viral comparison prompts out of the
  deterministic reviewed-card runtime unless red flags are present.
- `newborn_danger_sepsis.yaml` now includes reviewed supporting source sections
  for `GD-492`, `GD-298`, and `GD-617`, and opts into reviewed source-family
  runtime citations.
- `meningitis_sepsis_child.yaml` now records the reviewed `GD-298` meningitis
  source section, but does not opt into broad runtime fallback.

## Boundaries

- Do not make generic "any card source can select/cite any card" behavior.
- Do not convert ambiguous comparison prompts like "is this meningitis or a
  viral illness" into deterministic emergency answers unless the user names
  red flags.
- Do not change Android UI labels in this slice.

## Acceptance

- Focused unit suites pass for query routing and card contracts.
- `scripts/validate_guide_answer_cards.py` passes.
- Fresh `EZ` opt-in proof shows newborn rows move from generated answers to
  reviewed-card runtime answers without generation errors.
- Combined EX/EY/EZ/FC analyzer proof preserves:
  - retrieval/ranking/generation/safety-contract misses at `0`;
  - hit@3/hit@k/cited at `24/24`;
  - `EY` ambiguous comparison as the only generated-vs-shadow card gap.

## Proof

- Fresh EZ artifact:
  `artifacts/bench/guide_wave_ez_20260424_162406.json`
- Combined analyzer:
  `artifacts/bench/rag_diagnostics_20260424_1625_rags11_newborn_source_family/report.md`

Key counts over EX/EY/EZ/FC:

- deterministic pass: `13`
- expected supported: `9`
- abstain/clarify needed: `2`
- retrieval/ranking/generation/safety-contract misses: `0`
- reviewed card-backed answers: `8`
- generated prompts: `1`
- answer provenance: `13` deterministic, `8` reviewed-card runtime, `2`
  uncertain-fit, `1` generated model.

## Next

The remaining generated-vs-shadow gap is `EY` #6, the ambiguous meningitis vs
viral comparison. Treat that as a compare/clarify answer-shape problem and
citation-owner preference problem, not a broad deterministic emergency-card
fallback.
