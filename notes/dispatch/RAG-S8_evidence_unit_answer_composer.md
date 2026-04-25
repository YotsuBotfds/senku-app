# Slice RAG-S8 - evidence-unit answer composer

- **Status:** initial runtime evidence-unit composer landed 2026-04-24; analyzer/runtime bench rerun still pending.
- **Role:** main agent (`gpt-5.5 medium`; use high only if safety-critical generated-answer blocking enters scope).
- **Paste to:** after `RAG-S2`, `RAG-S5`, and `RAG-S7` have stable enough card/claim/source signals to provide ordered evidence units.
- **Why this slice now:** the current proof set no longer has retrieval/ranking misses. The remaining product gap is turning supported guide evidence into complete, well-shaped answers without relying on the model to rediscover every required branch from raw chunks.

## Outcome

Build a compact evidence packet before generation:

- selected answer-card first actions;
- active conditional first actions from the user prompt/context;
- matching urgent red flags;
- relevant `do_not` / forbidden-advice clauses;
- citation anchors, source sections, source invariants, and source guide IDs;
- one expected evidence owner when known;
- confidence/app-surface status inputs.

The model should then compose from the evidence packet, not from raw retrieved chunks alone.

## 2026-04-24 Initial Landing

Implemented in `guide_answer_card_contracts.py` and `query.py`:

- `compose_evidence_units(...)` returns deterministic evidence units from reviewed cards.
- `build_evidence_packet(...)` now wraps those units and preserves card order, capped card count, deduped guide IDs, and empty/no-card status.
- Evidence units include card ID, guide ID, source guide IDs, citation IDs, source sections, source invariants, required first actions, active conditional requirements, first actions, urgent red flags, forbidden advice, `do_not`, and support phrases labeled with the existing claim-support basis vocabulary.
- Runtime prompt injection now renders an "Evidence packet from reviewed guide answer cards" block with citation anchors and active conditional requirements.
- Prompt selection covers `source_file` fallback, supporting airway-owner source-section fallback, retrieved-card ordering, and a token gate that starts at `768`.

Validation:

- `tests.test_guide_answer_card_contracts` and `tests.test_query_routing` passed together: `69` tests.
- Broad desktop suite passed `212` tests.
- `scripts/validate_special_cases.py` validated `173` deterministic rules.
- `scripts/validate_guide_answer_cards.py` validated `6` cards.

Fresh proof:

- Final wave artifacts:
  - `artifacts/bench/guide_wave_ex_20260424_150429.json`
  - `artifacts/bench/guide_wave_ey_20260424_150441.json`
  - `artifacts/bench/guide_wave_ez_20260424_150451.json`
  - `artifacts/bench/guide_wave_fc_20260424_151507.json`
- Diagnostic artifact: `artifacts/bench/rag_diagnostics_20260424_1516_rags8_evidence_packet_trimmed/report.md`
- Counts over `24` rows: deterministic pass `13`, expected supported `9`, abstain/clarify needed `2`.
- Retrieval/ranking/generation/safety-contract misses remain `0`; hit@1 `21/24`, hit@3/hit@k/cited `24/24`.
- App acceptance: `15` strong supported, `7` moderate supported, `2` uncertain-fit accepted.
- Answer cards: `15` no-generated-answer, `7` partial, `2` pass.
- Claim support: `15` no-generated-answer, `9` pass.

Follow-up fix: `FC` #3 (`vomiting after a hit to the stomach`) repeatedly ended with a dangling LiteRT stop (`4. If the`) despite `finish=stop` and no cap hit. A prompt-shape experiment worsened card status, so it was not kept. The durable fix is in `bench.py`: safety-critical generated answers now retry dangling final conditional tails, then trim the final incomplete safety line if the retry repeats the same malformed tail and the remaining answer is substantive. The final proof shows this row as `card_pass`, `claim_pass`, and `strong_supported`.

Superseding follow-up: `RAG-S9` now exists at
`notes/dispatch/RAG-S9_shadow_card_answer_composer.md`. Its analyzer-only proof
shows direct reviewed-card composition at `24/24` card pass and `24/24` claim
pass on the same EX/EY/EZ/FC proof set, without changing production answers.

## Research Motivation

TREC RAG 2025 submissions point toward evidence selection, evidence-card/nugget compression, citation-first generation, and gap-aware refinement as stronger levers than surface-level rewriting. Self-RAG / Adaptive-RAG point toward adaptive retrieval and self-checking. For Senku, the practical local version is:

1. choose the evidence units;
2. order them by safety and user task;
3. generate;
4. run claim/card support diagnostics.

## Likely Touch Set

- `guide_answer_card_contracts.py`
- `rag_claim_support.py`
- `query.py` prompt/card block assembly
- `scripts/analyze_rag_bench_failures.py`
- `tests/test_guide_answer_card_contracts.py`
- `tests/test_query_routing.py`
- `tests/test_analyze_rag_bench_failures.py`

## Acceptance

- Evidence-packet creation is deterministic and testable without a model call.
- Safety prompts lead with required first actions and `do_not` clauses before background.
- The composer can explain when an expected card/evidence owner is missing and should abstain or ask one detail instead of generating confidently.
- Analyzer reports evidence-packet coverage and preserves retrieval/ranking miss buckets separately.
- A fresh EX/EY/EZ/FC proof keeps `0` retrieval/ranking/generation/safety misses and improves generated-row answer-card pass rate without loosening safety clauses.

## Anti-Recommendations

- Do not make this an LLM judge.
- Do not hide unsupported actions behind fluent prose.
- Do not add new deterministic predicates to compensate for weak evidence-packet selection.
- Do not start with every guide; keep the pilot card families until the evidence packet shape is proven.
