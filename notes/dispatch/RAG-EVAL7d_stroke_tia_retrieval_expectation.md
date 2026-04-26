# RAG-EVAL7d Stroke/TIA Retrieval Expectation

## Slice

Triage `RE7-ST-001`, the pure stroke-like symptoms row from EVAL7, without
editing guides.

## Finding

`RE7-ST-001` says face droop and slurred speech lasted a few minutes and then
improved. That is a pure FAST/transient neurologic presentation, not a mixed
cardiac/stroke presentation.

Evidence:

- `GD-232` owns direct stroke first-aid recognition: FAST signs, last-known-normal
  time, immediate emergency help or evacuation, and not waiting for improvement.
- `GD-601` explicitly routes FAST-dominant symptoms back to `GD-232` unless
  chest pressure, jaw/arm pain, or other cardiac signs co-occur.

## Change

- Added classic stroke/TIA supplemental retrieval for pure FAST/transient
  symptoms in `query.py`.
- Kept mixed stroke plus cardiac symptoms on the existing mixed-overlap path.
- Moved `RE7-ST-001` primary expected owner from `GD-601` to `GD-232`.
- Kept `GD-601` in `expected_guides` as support for the cardiac/stroke boundary.

## Validation

Run for this slice:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -c "import json, pathlib; p=pathlib.Path('artifacts/prompts/adhoc/rag_eval7_red_team_boundary_holdouts_20260425.jsonl'); rows=[json.loads(line) for line in p.read_text(encoding='utf-8').splitlines() if line.strip()]; print(f'ok rows={len(rows)}')"
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --fail-on-errors
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py tests\test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_stroke_tia_retrieval_only.json --output-md artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_stroke_tia_retrieval_only.md --progress
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_stroke_tia_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
git diff --check
```

Results:

- JSONL parse: `ok rows=10`
- Prompt expectation validation: `pass`; `prompts=10 expected_rows=10 errors=0 warnings=0 suppressed=0`
- `py_compile`: pass
- `tests.test_query_routing`: pass; `Ran 79 tests`
- Fresh retrieval eval:
  `artifacts/bench/rag_eval7_red_team_boundary_holdouts_20260425_stroke_tia_retrieval_only.json`
- Retrieval expectation validation: `pass`; `prompts=10 expected_rows=10 errors=0 warnings=0 suppressed=0`
- `RE7-ST-001`: expected owner list `GD-232`, `GD-601`; `GD-232` retrieved at rank `8`
- `git diff --check`: pass
