# RAG-EVAL6e Newborn Danger Retrieval Normalization

Worker: Codex
Date: 2026-04-25

## Scope

Implemented the narrow newborn/young-infant danger phrase normalization
recommended by `RAG-EVAL6b_sick_child_newborn_retrieval_triage.md`.

Files changed:

- `query.py`
- `tests/test_query_routing.py`

No guide files or EVAL6 JSONL files were edited. The protected untracked
planner handoff was not modified.

## Runtime Change

Added retrieval-only normalization for newborn/young-infant danger phrasing:

- `not feeding well`
- `feeding poorly`
- `not taking feeds`
- `not nursing well`
- `harder to wake`
- `difficult to wake`
- `feels cold` / `cold to touch` / `cold skin`
- explicit age-under-4-weeks context such as `3 weeks old` or `22 days old`

The existing answer-card detector remains narrower. The new helper is used for
retrieval profile selection, supplemental newborn/sepsis medical retrieval
specs, and metadata rerank support.

Guard coverage was added for routine baby/outage/cold/water prompts that do not
include newborn danger phrasing.

## EVAL6 Retrieval Movement

Command:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_newborn_danger_norm_retrieval_only.json --output-md artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_newborn_danger_norm_retrieval_only.md
```

Sick-child movement versus the prior retrieval-only artifact:

| Row | Before | After |
| --- | --- | --- |
| `RE6-SC-002` | no expected owner in top 8; owner share `0.0000` | expected hit@1 via `GD-298`; `GD-492` also in top 4/5; owner share `0.5000` |
| `RE6-SC-003` | no expected owner in top 8; owner share `0.0000` | expected hit@3 via `GD-284` rank 2 and `GD-492` rank 3; owner share `0.2500` |
| `RE6-SC-004` | expected hit@3, best rank 2 | unchanged: expected hit@3, best rank 2 |

Pack-level movement:

- expected hit@1: `1/14` -> `6/14`
- expected hit@3: `5/14` -> `10/14`
- expected hit@k: `10/14` -> `14/14`
- simple owner share: `29/112` -> `45/112`

Expectation validation against the new retrieval artifact passed with
`errors=0 warnings=0`.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py tests/test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_newborn_danger_norm_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
```
