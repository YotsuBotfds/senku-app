# RAG-EVAL4 Partial Router Follow-up Priorities

## Slice

Capture the concrete follow-up queue from the `RAG-EVAL2` held-out pack after
marker-overlay diagnostics.

## Source

- Prompt pack: `artifacts/prompts/adhoc/rag_eval_partial_router_holdouts_20260425.jsonl`
- Diagnostics:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_diag/report.md`
- Marker scan:
  `artifacts/bench/corpus_marker_scan_20260425_1622_gd585_repair.md`

## Priority queue

| priority | prompt ids | guides | owner lane | action |
| ---: | --- | --- | --- | --- |
| 1 | `RE2-UP-001`, `RE2-UP-008`, `RE2-UP-010`, `RE2-BR-005` | `GD-024`, `GD-066`, `GD-108`, `GD-646` | prompt-budget/runtime packaging | Separate prompt-budget overflows from local completion server 500s before treating these rows as retrieval or ranking failures. |
| 2 | `RE2-UP-002`, `RE2-BR-004` | `GD-029`, `GD-649` | deterministic rule side effects | Tighten deterministic triggers so broad injury/electrical wording falls through unless the wound or stroke signature is explicit. |
| 3 | `RE2-BR-001`, `RE2-BR-002`, `RE2-BR-006` | `GD-634`, `GD-635`, `GD-636` | bridge metadata/routing | Strengthen bridge guide aliases/routing cues and reciprocal language from high-ranking neighbors. |
| 4 | `RE2-UP-005`, `RE2-UP-012`, `RE2-BR-003`, `RE2-BR-007` | `GD-239`, `GD-446`, `GD-648`, `GD-637` | metadata/routing/rerank | Expected owner is retrieved but not rank 1; inspect exact-task ownership and broad recurring distractors. |
| 5 | `RE2-UP-006`, `RE2-UP-008`, `RE2-BR-008` | `GD-064`, `GD-066`, `GD-961` | guide content / bridge audit | Repair top-1 unresolved partial sections in `GD-064` and `GD-066`; audit `GD-961` so mess-hall routing does not become a broad kitchen/procurement distractor. |

## Guardrails

- Do not change rerank behavior until prompt-budget and deterministic side
  effects have been separated.
- Do not count artifact-error rows as generation misses unless a successful
  rerun produces an answer body with unsupported claims.
- Do not convert bridge guides into broad catchalls; add only concrete query
  language that matches the held-out failure.

## Validation

After any follow-up slice, rerun the held-out pack and marker overlay:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\bench.py --prompts artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --output artifacts\bench\rag_eval_partial_router_holdouts_20260425_rerun.md
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\rag_eval_partial_router_holdouts_20260425_rerun.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-dir artifacts\bench\rag_eval_partial_router_holdouts_20260425_rerun_diag
```
