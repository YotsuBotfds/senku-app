# RAG-TOOL7 Non-Android Regression Gate Recipe

## Scope

Use this recipe for non-Android RAG guide, routing, prompt-pack, and retrieval
changes before spending time on broader panels. It uses existing repo tools
only and keeps Android/emulator work out of the lane.

Reference pack and proofs:

- Prompt pack:
  `artifacts/prompts/adhoc/rag_eval_partial_router_holdouts_20260425.jsonl`
- Current generated-output baseline:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd397_expectation_cleanup_diag`
- Current retrieval-only proof:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_data_hints_retrieval_only.json`

## Fast Gate

Run the structural prompt expectation check first. Use `--fail-on-errors` for
normal edit validation because warnings can include accepted clarify paths that
are informative but not always blocking.

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --fail-on-errors
```

Run the retrieval-only evaluator when the change could affect retrieval,
metadata, deterministic routing, owner hints, or guide wording that retrieval
depends on. Pick a fresh label for the output files.

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval_partial_router_holdouts_20260425_<label>_retrieval_only.json --output-md artifacts\bench\rag_eval_partial_router_holdouts_20260425_<label>_retrieval_only.md --progress
```

Cross-check expectations against that retrieval-only output. Use
`--fail-on-warnings` for a strict retrieval gate, especially before promoting a
new retrieval baseline. Use only `--fail-on-errors` when the warning is a known
accepted clarify/abstain case and the task is not trying to close retrieval
coverage.

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval_partial_router_holdouts_20260425_<label>_retrieval_only.json --retrieval-top-k 8 --fail-on-errors --fail-on-warnings
```

## Generated-Output Gate

After running a new non-Android RAG bench artifact, classify failures into a
diagnostics directory. Use the generated bench JSON as input and keep the
output label paired with the bench label.

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\rag_eval_partial_router_holdouts_20260425_<label>.json --output-dir artifacts\bench\rag_eval_partial_router_holdouts_20260425_<label>_diag
```

Compare the new diagnostics against the `gd397_expectation_cleanup` baseline.
Leave the command report-only while investigating. Add `--fail-on-regression`
for the actual gate once the new run is the intended candidate.

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\rag_regression_gate.py artifacts\bench\rag_eval_partial_router_holdouts_20260425_gd397_expectation_cleanup_diag artifacts\bench\rag_eval_partial_router_holdouts_20260425_<label>_diag --format text
```

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\rag_regression_gate.py artifacts\bench\rag_eval_partial_router_holdouts_20260425_gd397_expectation_cleanup_diag artifacts\bench\rag_eval_partial_router_holdouts_20260425_<label>_diag --format text --fail-on-regression
```

## Pass Criteria

- Prompt expectation validation has `0` errors.
- Retrieval-only eval is at least as strong as the current data-hints proof for
  the edited surface, or every owner miss is explained as intentional/accepted.
- Failure analysis shows no new retrieval, ranking, generation, citation-owner,
  or top-1 marker-risk regression relative to `gd397_expectation_cleanup`.
- `rag_regression_gate.py --fail-on-regression` exits cleanly before declaring
  the non-Android RAG candidate ready.

## Final Hygiene

```powershell
git diff --check
```
