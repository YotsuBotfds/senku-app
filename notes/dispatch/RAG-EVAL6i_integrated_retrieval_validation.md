# RAG-EVAL6i Integrated Retrieval Validation

Date: 2026-04-25

## Scope

Integrated validation after applying:

- newborn/young-infant danger retrieval normalization
- contaminated-well plus food-scarcity source packaging
- electrical/trauma source packaging
- EVAL6 expected-owner adjustments

## Commands

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\water-purification.md guides\questionable-water-assessment-clarification.md guides\water-testing-quality-assessment.md guides\food-safety-contamination-prevention.md guides\food-rationing.md guides\electrical-safety-hazard-prevention.md guides\first-aid.md
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py tests/test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --fail-on-errors
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_integrated_retrieval_only.json --output-md artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_integrated_retrieval_only.md --progress
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_integrated_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
git diff --check
```

## Results

- Guide ingest: 7 files, 643 chunks, collection `senku_guides`, total 49,746 chunks.
- `tests.test_query_routing`: 76 tests passed.
- Prompt expectation validation without retrieval: passed, 14 expected rows, 0 errors, 0 warnings.
- Prompt expectation validation with retrieval: passed, 14 expected rows, 0 errors, 0 warnings.
- `git diff --check`: passed; only line-ending conversion warnings were printed.

Integrated retrieval metrics:

| metric | result |
| --- | --- |
| expected hit@1 | 7/14 |
| expected hit@3 | 10/14 |
| expected hit@8 | 14/14 |
| expected owner best rank | 2.79 |
| simple owner share | 48/112 |

## Remaining Follow-Ups

- `RE6-SH-001` and `RE6-SH-003`: storm/roof repair owners still outrank
  `GD-513`; inspect whether electrical hazard isolation needs stronger
  retrieval packaging or a narrow owner hint.
- `RE6-EV-002` and `RE6-IC-004`: evacuation/livestock rows hit expected owners
  only at rank 5 or 8; likely need route-family source packaging.
- Keep the integrated retrieval artifact as a local bench proof unless the
  artifact policy changes; the prompt pack and notes are the tracked contract.
