# RAG-EVAL3 Prompt Budget / Context Packaging Triage

## Slice

Dispatch-only triage for the `artifact_error` rows in the RAG-EVAL2 partial
router holdout artifact.

## Source Artifacts

- Bench markdown:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425.md`
- Bench JSON:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425.json`
- Diagnostic report:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_diag/report.md`
- Diagnostic rows:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_diag/diagnostics.csv`

## Affected Rows

| prompt | section | expected | retrieved/source guide ids | error category | error |
| --- | --- | --- | --- | --- | --- |
| `RE2-UP-001` | unresolved partials | `GD-024` | `GD-023`, `GD-345`, `GD-290`, `GD-618`, `GD-024` | `prompt_budget_exceeded` | Prepared prompt exceeded runtime budget: `4063 est > 4000 safe limit`. |
| `RE2-UP-008` | unresolved partials | `GD-066` | `GD-066`, `GD-080`, `GD-666` | `local_completion_500` | Local completion server returned `500` during chat completion. |
| `RE2-UP-010` | unresolved partials | `GD-108` | `GD-288`, `GD-108`, `GD-211` | `local_completion_500` | Local completion server returned `500` during chat completion. |
| `RE2-BR-005` | bridge router | `GD-646` | `GD-250`, `GD-055`, `GD-039`, `GD-239`, `GD-646` | `prompt_budget_exceeded` | Prepared prompt exceeded runtime budget: `4032 est > 4000 safe limit`. |

## Likely Cause

These rows are packaging/runtime-size failures, not answer quality failures.
The diagnostic report marks all four as `artifact_error` with
`no_generated_answer`, no cited guides, and no answer cards. Two rows fail before
generation because `bench.py` detects a prepared prompt just over the small-model
runtime budget. The other two rows make it to the local LiteRT completion server
and receive HTTP `500`; context pressure is plausible, but the artifact does not
prove the server-side cause.

The retrieved/source lists are still useful for follow-up. Three rows retrieved
the expected owner somewhere in the source set (`RE2-UP-001`, `RE2-UP-008`,
`RE2-UP-010`), and `RE2-BR-005` retrieved `GD-646` at rank 5. Do not count these
as retrieval or generation regressions until a successful rerun produces an
answer body.

## Next Validation Commands

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile bench.py query_prompt_runtime.py scripts\analyze_rag_bench_failures.py
```

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_bench_runtime tests.test_query_prompt_runtime tests.test_analyze_rag_bench_failures -v
```

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\bench.py --prompts artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --output artifacts\bench\rag_eval_partial_router_holdouts_20260425_rerun.md
```

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\rag_eval_partial_router_holdouts_20260425_rerun.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-dir artifacts\bench\rag_eval_partial_router_holdouts_20260425_rerun_diag
```

## Handoff Notes

- Keep prompt-budget/context-packaging work separate from the RAG-EVAL2
  retrieval/ranking misses.
- The first success criterion for this slice is `artifact_error: 0` on the
  focused holdout pack; only then compare expected-owner rates.
- If errors persist, inspect final prepared prompt sizing for the four affected
  IDs before changing retrieval/ranking behavior.

## 2026-04-25 Implementation Update

Implemented small-model prompt packaging repairs:

- `query_prompt_runtime.py`: added `safe_prompt_token_limit(...)` for callers
  that need the prompt assembly window after reserving runtime safety margin.
- `bench.py`: passes the safe prompt assembly limit into `prepare_prompt(...)`
  and exports prompt-budget estimate fields into bench JSON rows.
- `query.py`: compacts retrieved guide excerpt bodies for tight runtime windows
  while preserving each guide header / guide ID and citation allowlist surface.
- `query_answer_card_runtime.py`: preserves retrieved guide order while letting
  question-prioritized cards win inside a shared source-family guide.

Fresh held-out rerun:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\bench.py --prompts artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --output artifacts\bench\rag_eval_partial_router_holdouts_20260425_context_compact.md
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\rag_eval_partial_router_holdouts_20260425_context_compact.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-dir artifacts\bench\rag_eval_partial_router_holdouts_20260425_context_compact_diag
```

Result:

- Bench summary: `21/21` successful prompts, `0` errors.
- Diagnostic buckets: `9` retrieval misses, `8` ranking misses, `3`
  expected-supported, `1` abstain/clarify, `0` artifact errors.
- Former artifact rows generated successfully:
  - `RE2-UP-001`: `3625` estimated prompt tokens, `165` completion tokens.
  - `RE2-UP-008`: `3587` estimated prompt tokens, `194` completion tokens,
    cites expected `GD-066`.
  - `RE2-UP-010`: `3613` estimated prompt tokens, `245` completion tokens.
  - `RE2-BR-005`: `3462` estimated prompt tokens, `135` completion tokens.

Focused validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_prompt_runtime tests.test_bench_runtime tests.test_query_answer_card_runtime tests.test_query_routing -v
```

Proof: `111` tests passed.
