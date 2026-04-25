# Slice RAG-S1b - bench ranked retrieval metadata

- **Status:** implemented in the 2026-04-24 RAG-S1 continuation block. Retained as the design/acceptance record.
- **Role:** main agent (`gpt-5.5 medium`; use high only if changing runtime retrieval decisions, which this slice should avoid).
- **Paste to:** after `RAG-S1` if the diagnostic artifact still has to infer source guide IDs from Markdown.
- **Why this slice now:** `RAG-S1` proved the diagnostic direction is useful, but the bench JSON often lacks raw ranked retrieval/source guide IDs for generated RAG rows. The analyzer had to infer candidates from Markdown source titles. Future diagnostics should be artifact-native and machine-readable.

## Outcome

Preserve normalized ranked retrieval/source guide metadata in bench JSON without changing runtime answer behavior.

## Read Set

- `notes/RAG_S1_DIAGNOSTIC_RESULT_20260424.md`
- `scripts/analyze_rag_bench_failures.py`
- `bench.py`
- `bench_artifact_tools.py`
- `query.py` only as needed to understand existing retrieval metadata

## Likely Touch Set

- `bench.py`
- `bench_artifact_tools.py`
- `tests/test_bench_artifact_tools.py` or focused bench-runtime tests
- maybe `scripts/analyze_rag_bench_failures.py` if field names change

## Desired JSON Shape

For each prompt result, preserve something like:

```json
"retrieval_candidates": [
  {
    "rank": 1,
    "guide_id": "GD-232",
    "title": "First Aid & Emergency Response",
    "section": "Choking and Airway Management",
    "category": "medical",
    "source": "hybrid",
    "score": 0.83
  }
]
```

If only cited/source rows are available at the bench layer, use `source_candidates` rather than pretending these are raw retriever ranks.

## Acceptance

- Future bench JSON exposes ordered guide IDs for generated RAG rows.
- `scripts/analyze_rag_bench_failures.py` can consume the new field directly.
- Existing markdown report formatting remains unchanged unless deliberately improved.
- No answer text, routing, retrieval, deterministic rule, or guide-content behavior changes.
- Focused tests cover the new flattened fields.

## Anti-Recommendations

- Do not tune retrieval in this slice.
- Do not add answer-card runtime behavior here.
- Do not infer clinical correctness from this metadata layer; it is observability only.

## Report Format

- Files changed.
- Example JSON row before/after.
- Test commands and result.
