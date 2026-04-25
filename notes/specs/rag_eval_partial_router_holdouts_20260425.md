# RAG-EVAL2 Held-out Prompt Pack

## Slice

Build a focused held-out pack that catches primary-answer regressions when the
top-1 retrieved context is a `bridge` guide or contains unresolved partial markers.

## Scope

- Data source: `artifacts/prompts/adhoc/rag_eval_partial_router_holdouts_20260425.jsonl`
- Coverage:
  - unresolved partial markers: 13 prompts
  - bridge markers: 8 prompts
- All prompts are safety-framed and do not request operationally harmful actions.
- This is an evaluation-only slice for routing/diagnostic signal quality; no guide
  edits, no runtime rule edits, no Android changes.

## Expected signal

These prompts should surface:

- top-1 guide is `bridge` (`bridge_frontmatter`) on the expected bridge guides
- top-1 unresolved-partial marker is avoided as the primary answer source when a
  stronger answer exists
- primary-owner concentration remains stable in marker-overlaid diagnostic runs
- residual regressions are reported for manual prioritization

## Validation

1. Confirm JSONL format and metadata shape:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_bench_prompt_loading -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile bench.py
```

2. Run a focused desktop smoke with the held-out pack:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\bench.py --prompts artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --output artifacts\bench\rag_eval_partial_router_holdouts_20260425.md
```

3. Apply the marker overlay to the focused artifact:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\rag_eval_partial_router_holdouts_20260425.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-dir artifacts\bench\rag_eval_partial_router_holdouts_20260425_diag
```

Latest local proof at
`artifacts/bench/rag_eval_partial_router_holdouts_20260425_diag/report.md`:

- 21 rows with expected-guide metadata.
- Bucket counts: 8 retrieval misses, 4 ranking misses, 4 artifact errors,
  2 deterministic passes, 2 expected supported, 1 accepted `uncertain_fit`.
- Marker overlay: 2 top-1 unresolved-partial rows, 1 top-1 bridge row.
- Expected-owner top-k hit rate: 10/21; cited rate: 5/21.

## Acceptance

- JSONL loads without schema errors.
- Bench JSON artifact writes successfully.
- Any unresolved-partial or bridge-only primary matches are called out in the
  analyzer output for follow-up, not treated as green by default.
