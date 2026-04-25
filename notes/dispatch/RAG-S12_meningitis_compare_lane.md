# RAG-S12 Meningitis Compare Lane

## Slice

RAG-S12 - resolve the remaining `EY` #6 meningitis-vs-viral generated gap
without broad deterministic emergency fallback.

## Role

Main agent with scout and worker support. Default `gpt-5.5 medium`; use high
only for delicate medical/safety ownership review.

## Preconditions

- `RAG-S11` reviewed source-family runtime is available for newborn danger
  signs.
- Ambiguous comparison prompts must stay separate from red-flag emergency
  prompts.
- Reviewed-card runtime remains opt-in behind
  `SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1`.

## Outcome

The bare prompt `is this meningitis or a viral illness` now stays on the
generated RAG compare lane, cites expected clinical owner evidence, and no
longer appears as a generated-vs-shadow card gap. Red-flag meningitis prompts
still use the strict reviewed emergency card path.

Implemented:

- `query.py` strengthens meningitis-vs-viral owner/rerank preference toward
  clinical owners such as `GD-589` / `GD-284` and away from public-health drift
  unless the prompt is explicitly about outbreak/community response.
- `query.py` narrows the citation contract for bare meningitis-vs-viral
  comparison prompts to retrieved clinical owner-family guides when available.
- `query.py` keeps reviewed-card selection applicability-gated so shared guide
  IDs do not let the newborn or meningitis card answer unrelated prompts.
- `query.py` treats fever plus stiff neck as a meningitis red flag, so
  `fever with stiff neck: meningitis vs viral illness?` stays in the strict
  emergency-card lane rather than the bare compare lane.
- `scripts/analyze_rag_bench_failures.py` adds
  `not_applicable_compare` for bare meningitis-vs-viral prompts, while still
  applying the strict meningitis card contract when red flags are stated.

## Boundaries

- Do not make ambiguous comparison prompts deterministic emergency answers
  without stated red flags.
- Do not let shared/supporting guide IDs select arbitrary reviewed cards unless
  the card also matches the question shape.
- Do not route public-health actions such as Health Officer notification,
  contact tracing, quarantine, or isolation into clinical comparison prompts.

## Proof

- Fresh EY artifact:
  `artifacts/bench/guide_wave_ey_20260424_165826.json`
- Combined analyzer:
  `artifacts/bench/rag_diagnostics_20260424_1659_rags12_meningitis_compare_final/report.md`

Key counts over EX/EY/EZ/FC:

- deterministic pass: `13`
- expected supported: `9`
- abstain/clarify needed: `2`
- retrieval/ranking/generation/safety-contract misses: `0`
- hit@1: `21/24`
- hit@3/hit@k/cited: `24/24`
- reviewed card-backed answers: `8`
- generated prompts: `1`
- generated-vs-shadow card gaps: `0`
- claim support: `15` no-generated-answer, `9` pass
- answer-card statuses: `15` no-generated-answer, `8` pass,
  `1` not-applicable compare.

## Validation

- Focused S12 suites: `85` tests passed.
- Expanded desktop RAG/safety suites: `221` tests passed.
- `scripts/validate_special_cases.py`: `173` deterministic rules validated.
- `scripts/validate_guide_answer_cards.py`: `6` cards validated.

## Next

The product direction is no longer broad retrieval/rerank patching. The next
high-leverage work is targeted code health around the RAG hot zones:
medical-routing predicates, citation/source-owner policy, reviewed-card
runtime selection, and analyzer acceptance rules. Keep this refactor small,
behavior-preserving, and guarded by the current EX/EY/EZ/FC proof set.
