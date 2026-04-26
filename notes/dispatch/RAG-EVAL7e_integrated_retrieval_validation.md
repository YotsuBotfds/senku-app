# RAG-EVAL7e Integrated Retrieval Validation

## Scope

Integrated retrieval validation after applying the first EVAL7 repair set:

- airway/choking source packaging for `RE7-AW-002`
- newborn hyphenated-age and poor-feeding phrase normalization for `RE7-NB-001`
- pure stroke/TIA first-aid retrieval and expectation adjustment for
  `RE7-ST-001`

## Commands

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --stats
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py tests/test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_integrated_retrieval_only.json --output-md artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_integrated_retrieval_only.md --progress
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_integrated_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
```

## Results

Integrated retrieval metrics:

| metric | result |
| --- | --- |
| expected hit@1 | 5/10 |
| expected hit@3 | 9/10 |
| expected hit@8 | 10/10 |
| expected owner best rank | 2.20 |
| simple owner share | 37/80 |

Retrieval-backed expectation validation passed with `errors=0` and
`warnings=0`.

## Remaining Follow-Up

`RE7-ST-001` is now valid but still late: `GD-232` appears at rank 8. That is
acceptable for this repair checkpoint because generation should now have at
least one expected owner in top-k, but a later ranking slice can try to move
pure FAST/TIA first aid into the top 3 without overfitting broad headache or
respiratory prompts.
