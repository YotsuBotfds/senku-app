# RAG-EVAL2 Partial Router Holdout Pack

## Slice

Build and run the `RAG-EVAL2` held-out pack for primary-answer regressions where
top-1 is bridge-thin or unresolved-partial-bearing.

## Role

- Main worker (focused prompt-diagnostics slice)

## Preconditions

- `scripts/scan_corpus_markers.py` output is available and includes bridge and
  unresolved partial markers.
- Marker overlay flow from `RAG-EVAL1` is in place and trusted for reporting.

## Outcome

1. Add held-out prompt pack:
   `artifacts/prompts/adhoc/rag_eval_partial_router_holdouts_20260425.jsonl`
2. Add spec note:
   `notes/specs/rag_eval_partial_router_holdouts_20260425.md`
3. Confirm JSONL metadata fields are preserved through `bench.py` loaders.
4. Run a focused pack pass and use marker-overlay diagnostics where needed.

## Latest proof

- Focused run:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425.json`
- Marker-overlay diagnostics:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_diag/report.md`
- Result: 21 rows with expected-guide metadata.
- Bucket counts: 8 retrieval misses, 4 ranking misses, 4 artifact errors,
  2 deterministic passes, 2 expected supported, 1 accepted `uncertain_fit`.
- Artifact errors split into 2 prompt-budget overflows and 2 local generation
  server 500s.
- Marker overlay: 2 top-1 unresolved-partial rows and 1 top-1 bridge row.
- Expected-owner rates: hit@1 3/21, hit@3 7/21, hit@k 10/21, cited 5/21.

## Boundaries

- No guide edits.
- No query predicate/routing model edits.
- No Android runtime/product changes.
- No operational-harmful prompt content.

## Acceptance

- `tests.test_bench_prompt_loading` passes for JSONL load shape.
- `bench.py` successfully writes run JSON with this pack.
- Regressions appear as explicit rows in analyzer outputs and can be triaged as
  unresolved-partial, bridge, or marker-noise.

## Delegation hints

- Use `RAG-EVAL1` dispatch and marker scan outputs as the source of truth for
  prompt selection.
- Prefer prompt IDs grouped by marker class (`RE2-UP-*`, `RE2-BR-*`) for quick
  triage in future reruns.
- Keep prompts safety-first and stop phrasing if it drifts toward harmful or
  illegal instructions.

## Report format

- Report run result path.
- Report how many unresolved-partial / bridge-primary rows appeared in this pack.
- Flag prompts that should likely be moved to retrieval/ranking remediation vs.
  content repair.

## Notes

- Parent diagnostic context:
  `notes/dispatch/RAG-EVAL1_corpus_marker_diagnostic_overlay.md`
- The four `artifact_error` rows are not retrieval misses: RE2-UP-001 and
  RE2-BR-005 exceeded the runtime prompt budget, while RE2-UP-008 and
  RE2-UP-010 hit local generation server 500s. Triage them separately from
  retrieval/ranking misses.
