# RAG-EVAL8a Initial Retrieval Validation

## Slice

Ran retrieval-only evaluation for the EVAL8 compound/boundary holdout pack and
validated prompt expectations against the retrieval artifact. This slice is
artifact-only: no runtime code, guide, test, script, prompt-pack, ingestion, or
Android changes.

## Files Changed

- `artifacts/bench/rag_eval8_compound_boundary_holdouts_20260425_initial_retrieval_only.json`
- `artifacts/bench/rag_eval8_compound_boundary_holdouts_20260425_initial_retrieval_only.md`
- `notes/dispatch/RAG-EVAL8a_initial_retrieval_validation.md`

## Commands

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval8_compound_boundary_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_initial_retrieval_only.json --output-md artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_initial_retrieval_only.md --progress
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval8_compound_boundary_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_initial_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
```

## Metrics

- Total prompts: `10`
- Retrieval error rows: `0`
- Expected hit@1: `4/10 (40.0%)`
- Expected hit@3: `5/10 (50.0%)`
- Expected hit@8: `7/10 (70.0%)`
- Expected owner best rank: `2.29`
- Simple owner share: `25/80 (31.2%)`

## Row Results

Rows with expected owner in top 8:

- `RE8-EL-001`: hit@1, best rank `1`
- `RE8-MD-001`: hit@1, best rank `1`
- `RE8-RW-001`: hit@8, best rank `4`
- `RE8-KT-001`: hit@8, best rank `5`
- `RE8-TR-001`: hit@3, best rank `3`
- `RE8-WS-001`: hit@1, best rank `1`
- `RE8-FS-001`: hit@1, best rank `1`

Rows missing expected owner in top 8:

- `RE8-HT-001`: expected `GD-377|GD-602|GD-301|GD-035|GD-232`; retrieved `GD-054|GD-054|GD-898|GD-960|GD-054|GD-512|GD-353|GD-526`
- `RE8-SP-001`: expected `GD-049|GD-232|GD-024|GD-035`; retrieved `GD-731|GD-935|GD-298|GD-935|GD-369|GD-941|GD-030|GD-733`
- `RE8-CM-001`: expected `GD-589|GD-284|GD-298|GD-617|GD-219|GD-232`; retrieved `GD-949|GD-936|GD-733|GD-948|GD-733|GD-918|GD-930|GD-915`

## Expectation Validation

Validation result: `warn`.

- Prompts: `10`
- Expected rows: `10`
- Errors: `0`
- Warnings: `3`
- Suppressed: `0`

Warnings:

- `retrieval_missing_expected_owner` for `RE8-HT-001`
- `retrieval_missing_expected_owner` for `RE8-SP-001`
- `retrieval_missing_expected_owner` for `RE8-CM-001`

## Next Recommendation

Review the three top-8 misses before any guide or runtime work. Start by
classifying whether each miss is expectation fairness/source packaging versus
true retrieval or ranking drift; the misses are concentrated in heat/toxic
exposure, spinal movement/cold exposure, and child meningitis/sepsis boundary
scenarios.
