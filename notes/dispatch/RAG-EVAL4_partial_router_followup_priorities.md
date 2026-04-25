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

## Post-fix proof

After `RAG-EVAL5`, the GD-064/GD-066 partial repairs, and the bridge metadata
pass, a focused rerun wrote:

- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_post_fixes.json`
- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_post_fixes_diag/report.md`

Result:

- deterministic rows: `0` (the two hijacks now fall through)
- top-1 unresolved-partial rows: `0`
- top-1 bridge rows: `1`
- expected-owner top-k hit rate: `11/21`, up from `10/21`
- expected-owner top-3 hit rate: `8/21`, up from `7/21`
- bucket counts: `9` retrieval misses, `5` ranking misses, `4` artifact errors,
  `2` expected supported, `1` accepted `uncertain_fit`

Interpretation:

- The deterministic fixes succeeded, but `RE2-UP-002` and `RE2-BR-004` now
  expose real RAG ranking/retrieval work instead of being hidden by
  deterministic passes.
- GD-064/GD-066 content repairs succeeded: the marker overlay no longer shows
  top-1 unresolved partials.
- Bridge metadata improved guide metadata coverage but did not move `GD-634`,
  `GD-635`, or `GD-636` into retrieved candidates without re-ingest or deeper
  retrieval/ranking work.
