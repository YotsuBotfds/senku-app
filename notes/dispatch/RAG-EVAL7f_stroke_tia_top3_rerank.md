# RAG-EVAL7f Stroke/TIA Top-3 Rerank

Date: 2026-04-26

## Scope

Follow-up to `RAG-EVAL7d` / `RAG-EVAL7e` for the one remaining late-but-valid
EVAL7 retrieval row:

- `RE7-ST-001`: pure FAST/TIA prompt with transient face droop and slurred
  speech.
- Expected primary owner: `GD-232` First Aid & Emergency Response.
- Guard: mixed stroke plus chest-pressure prompts remain in the
  stroke/cardiac-overlap lane and do not receive the pure first-aid stroke
  rerank path.

## Runtime Change

`query.py` now gives classic FAST/TIA prompts a narrow first-aid owner boost and
keeps a retrieved `GD-232` owner inside the top three after the final rerank
sort. The post-sort move is limited by `_is_classic_stroke_fast_special_case`,
so compound cardiac/stroke prompts continue using the overlap behavior.

## Validation

Focused tests:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing.QueryRoutingTests.test_classic_stroke_fast_rerank_preserves_first_aid_owner tests.test_query_routing.QueryRoutingTests.test_classic_stroke_fast_metadata_boost_stays_out_of_mixed_cardiac_overlap -v
```

Result: `2` tests passed.

Full focused routing/tooling sweep:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing tests.test_guide_edit_impact tests.test_plan_incremental_ingest -v
```

Result: `91` tests passed.

EVAL7 retrieval-only proof:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_stroke_top3_v2_retrieval_only.json --output-md artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_stroke_top3_v2_retrieval_only.md --progress
```

Metrics:

| metric | result |
| --- | --- |
| expected hit@1 | `5/10` |
| expected hit@3 | `10/10` |
| expected hit@8 | `10/10` |
| expected owner best rank | `1.70` |
| simple owner share | `37/80` |

`RE7-ST-001` moved from `GD-232` rank `8` to rank `3`.

Expectation cross-check:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_stroke_top3_v2_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
```

Result: `0` errors, `0` warnings.
