# RAG-CARD5 Community Kitchen Illness Control

Date: 2026-04-26

## Scope

Closed the top resume-queue item for `RE8-KT-001`, the crowded volunteer
kitchen diarrhea cluster with limited handwashing water and pressure to keep
meals going.

## Changes

- Added reviewed answer card `community_kitchen_illness_control`.
- Added strict runtime prioritization/matching for kitchen or mess-hall illness
  cluster prompts, with near-miss exclusions for ordinary kitchen cleanup,
  single-person diarrhea, generic handwashing, and non-kitchen camp outbreaks.
- Updated EVAL8 KT expectations so `GD-902` is an accepted primary owner for
  active diarrhea-outbreak food-service restrictions, alongside `GD-961` and
  `GD-732`.
- Fixed one existing anaphylaxis card invariant punctuation mismatch so the
  answer-card validator is green.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_guide_answer_cards.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_guide_answer_card_contracts tests.test_query_answer_card_runtime -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval8_compound_boundary_holdouts_20260425.jsonl --fail-on-errors
$env:SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS='1'
& .\.venvs\senku-validate\Scripts\python.exe -B bench.py --prompts artifacts\prompts\adhoc\rag_eval8_compound_boundary_holdouts_20260425.jsonl --prompt-id RE8-KT-001 --output artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_community_kitchen_card_20260426_kt_only_v2.md --urls http://127.0.0.1:1235/v1 --worker-models gemma-4-e2b-it-litert --embed-url http://127.0.0.1:1234/v1 --top-k 8 --max-completion-tokens 768
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_community_kitchen_card_20260426_kt_only_v2.json --output-dir artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_community_kitchen_card_20260426_kt_only_v2_diag
```

Results:

- Guide answer-card validation: 13 cards valid.
- Card/runtime tests: 61 tests OK.
- Prompt expectations: 10 expected rows, 0 warnings, 0 errors.
- `RE8-KT-001` proof: `expected_supported`, `card_backed_runtime`,
  `reviewed_card_runtime`, answer-card pass, evidence-nugget pass, claim pass,
  cited expected owner `GD-902`.

## Next Queue Item

Superseded on 2026-04-26. Rank 2 was `RE8-TR-001`; later evidence work added
route/delegation proof and proof hardening for that row, so do not redispatch
this breadcrumb without refreshing the current baseline first.
