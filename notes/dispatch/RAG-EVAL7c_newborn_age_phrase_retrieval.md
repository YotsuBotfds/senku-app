# RAG-EVAL7c Newborn Age Phrase Retrieval

## Scope

Retrieval-only fix for EVAL7 newborn danger phrasing. No guide or prompt-pack
edits.

Changed newborn/young-infant retrieval normalization to recognize:

- hyphenated young-infant ages such as `2-week-old` and `12-day-old`
- narrow poor-feeding variants `has not fed much` and `not fed much`

Routine baby prompts remain guarded, including normal feeding schedule prompts
for a `2-week-old`.

## RE7-NB-001 Movement

Before (`rag_eval7_red_team_boundary_holdouts_20260425_retrieval_only`):

- top retrieved: `GD-914, GD-914, GD-916, GD-910, GD-950, GD-914, GD-041, GD-933`
- expected-owner hit: no

After, current working tree
(`rag_eval7_red_team_boundary_holdouts_20260425_newborn_phrase_retrieval_only`):

- top retrieved: `GD-298, GD-617, GD-091, GD-284, GD-041, GD-041, GD-916, GD-914`
- expected-owner hit: yes
- best expected-owner rank: 1
- owner share: 3/8

Pack-level current run:

- expected hit@1: 5/10
- expected hit@3: 9/10
- expected hit@8: 9/10
- warnings: 1; remaining warning is RE7-ST-001

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py tests\test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_newborn_phrase_retrieval_only.json --output-md artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_newborn_phrase_retrieval_only.md --progress
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_newborn_phrase_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
```

Results:

- `py_compile`: pass
- `tests.test_query_routing`: pass, 78 tests
- `evaluate_retrieval_pack`: pass
- `validate_prompt_expectations`: warning status, `errors=0`, `warnings=1`
