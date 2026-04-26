# RAG-EVAL8e Spine / Meningitis Retrieval Fix

## Slice

High Worker P implemented a narrow retrieval/rerank fix for the EVAL8d spine and
child meningitis misses. No guides, prompt packs, scripts, Android files, or
deterministic answer rules were edited.

## Code Changes

- `query.py`
  - Added retrieval-only child stiff-neck plus dangerous-rash detection for
    no-fever connectivity-distractor prompts. This intentionally does not
    broaden `meningitis_rash_emergency` deterministic routing.
  - Added a focused possible-spinal-injury movement-boundary predicate gated on
    trauma/fall + neck/back pain + numbness/tingling/weakness + carry/drag/move
    pressure.
  - Added targeted supplemental retrieval lanes and metadata rerank cues for
    `GD-049` / `GD-232` spine owners and for no-fever child meningitis/rash
    retrieval.
  - Updated prompt safety notes to cover the retrieval-only meningitis cluster
    without saying fever must be present.
- `tests/test_query_routing.py`
  - Added predicate, supplemental-lane, metadata-rerank, and distractor guards
    for the EVAL8d spine and no-fever child meningitis shapes.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py tests\test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases -v
```

Result: `80` routing tests passed and `83` special-case tests passed.

Deterministic guard check:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -c "import query; p='The internet is down, cell service is weak, and a child has a stiff neck with a purple rash. Should I troubleshoot the router first or start moving toward help?'; print(query.classify_special_case(p)); print(query._is_meningitis_rash_emergency_query(p)); print(query._is_meningitis_rash_retrieval_query(p)); print(query._retrieval_profile_for_question(p))"
```

Result: `(None, None)`, `False`, `True`, `safety_triage`.

Retrieval-only EVAL8d focused pack:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval8d_spine_meningitis_focused_retrieval_20260426.jsonl --top-k 8 --output-json artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_eval8e_after_retrieval_top8.json --output-md artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_eval8e_after_retrieval_top8.md --progress
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval8d_spine_meningitis_focused_retrieval_20260426.jsonl --retrieval-eval artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_eval8e_after_retrieval_top8.json --retrieval-top-k 8 --fail-on-errors --output-json artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_eval8e_after_retrieval_expectations.json --output-md artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_eval8e_after_retrieval_expectations.md
```

Before from EVAL8d diagnostic: hit@1 `3/6`, hit@3 `3/6`, hit@8 `3/6`, owner
share `16/48`; warnings for `RE8d-SP-001`, `RE8d-SP-003`, `RE8d-CM-001`.

After: hit@1 `6/6`, hit@3 `6/6`, hit@8 `6/6`, owner share `28/48`, expectation
validation `0` errors / `0` warnings.

Key row movement:

- `RE8d-SP-001`: `GD-049` rank 1, `GD-232` rank 8.
- `RE8d-SP-003`: `GD-049` rank 1.
- `RE8d-CM-001`: `GD-589` rank 1, `GD-284` also present.

Retrieval-only EVAL8 integrated pack:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval8_compound_boundary_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_eval8e_after_retrieval_top8.json --output-md artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_eval8e_after_retrieval_top8.md --progress
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval8_compound_boundary_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_eval8e_after_retrieval_top8.json --retrieval-top-k 8 --fail-on-errors --output-json artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_eval8e_after_retrieval_expectations.json --output-md artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_eval8e_after_retrieval_expectations.md
```

After: hit@1 `7/10`, hit@3 `8/10`, hit@8 `10/10`, owner share `38/80`,
expectation validation `0` errors / `0` warnings. `RE8-SP-001` and
`RE8-CM-001` both moved to rank-1 expected owners.

## Cautions

- This is a retrieval/rerank fix, not guide content work and not deterministic
  answer expansion.
- The no-fever child stiff-neck/purple-rash prompt now retrieves and prompt-shapes
  as safety triage, but it does not automatically use the reviewed meningitis
  answer card because the existing card predicate remains stricter.
- `RE8d-SP-003` reaches `GD-049` at rank 1 but not `GD-232` in top 8; the
  primary spine owner is fixed, while first-aid support concentration remains
  weaker for that exact dirty-creek/cabin phrasing.
