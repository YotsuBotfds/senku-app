# RAG-S14 Nugget Evidence Diagnostics

## Slice

Analyzer-only evidence nugget diagnostics for RAG bench failures.

## Role

Main agent / docs and analyzer lane. No runtime query, retrieval, prompt,
guide, or Android behavior changed in this slice.

## Preconditions

- `RAG-S13` code-health extraction is already in place.
- Existing answer-card, claim-support, app-acceptance, and provenance analyzer
  fields remain the baseline.
- Keep this as measurement and reporting only; do not add an LLM judge or
  runtime blocker.

## Outcome

Landed an analyzer-only diagnostic layer for evidence nuggets:

- `rag_bench_answer_diagnostics.py` now exposes
  `evidence_nugget_diagnostics(...)`.
- `scripts/analyze_rag_bench_failures.py` threads nugget fields through rows,
  CSV output, JSON/report summaries, and Markdown report text.
- CSV/report fields now summarize evidence nugget status and counts:
  total/present/cited/supported/missing/contradicted.
- Reports include top missing and contradicted evidence nugget phrases.
- Tests cover the helper and analyzer plumbing in
  `tests/test_rag_bench_answer_diagnostics.py` and
  `tests/test_analyze_rag_bench_failures.py`.

## Boundaries

- No runtime query behavior changed.
- No retrieval, rerank, prompt, or guide content behavior changed.
- No Android behavior changed.
- The diagnostics are not a product gate; they are evidence organization and
  failure-analysis fields for bench artifacts.

## Acceptance

- Evidence nugget diagnostics classify required evidence as present, cited,
  supported, missing, or contradicted.
- Analyzer CSV/report summaries expose nugget status, aggregate counts, and top
  missing/contradicted phrases.
- Existing analyzer classifications remain compatible with answer-card,
  claim-support, app-acceptance, and provenance fields.

## Validation

Focused tests were added in:

- `tests/test_rag_bench_answer_diagnostics.py`
- `tests/test_analyze_rag_bench_failures.py`

Passed validation:

- `py_compile` for `rag_bench_answer_diagnostics.py`,
  `scripts/analyze_rag_bench_failures.py`, and the focused tests.
- `tests.test_rag_bench_answer_diagnostics`,
  `tests.test_analyze_rag_bench_failures`, and
  `tests.test_guide_answer_card_contracts` (`65` tests).
- Standard deterministic/routing lane (`217` tests).
- `scripts/validate_guide_answer_cards.py` (`6` cards).

## Follow-Up

Use these fields to diagnose evidence coverage before considering more
deterministic routing or prompt patches. Any future LLM judge, runtime blocker,
or Android surface should be a separately scoped slice.
