# RAG-S21 Dangerous Activation Deterministic Extension

## Slice

Extend the existing `mania_no_sleep_immediate_safety` deterministic rule to
cover the current FD dangerous-activation / mania-like crisis prompts.

## Role

Worker-assisted safety-routing slice. Predicate ownership was limited to
`query.py`; test ownership was limited to `tests/test_special_cases.py`.

## Preconditions

- `RAG-S20` taxonomy proof is present at
  `artifacts/bench/rag_diagnostics_20260425_1448_fa_fb_fd_fe_current_taxonomy/`.
- FD rows in that panel are generated RAG with `GD-859` at rank 1, but no
  reviewed answer card exists for the dangerous-activation family.

## Outcome

- `_is_mania_no_sleep_immediate_safety_special_case(...)` now recognizes FD
  phrasings such as barely slept, hardly eaten, keeps pacing, normal rules do
  not apply, walking outside at night, special mission, and unsafe choices.
- The rule still requires activation/risk plus sleep/food impairment or a
  high-risk mania signature.
- Ordinary stress/anxiety insomnia stays out of this deterministic branch.
- FD wave no longer reaches generation; all six FD prompts use the existing
  deterministic crisis-ordering answer.

## Acceptance

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py tests\test_special_cases.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases.SpecialCaseTests.test_mania_no_sleep_de_prompts_route tests.test_special_cases.SpecialCaseTests.test_withdrawal_danger_cf_prompts_route_to_ordered_rule -v
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave fd -PythonPath .\.venvs\senku-validate\Scripts\python.exe
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\guide_wave_fa_20260425_143154.json artifacts\bench\guide_wave_fb_20260425_144548.json artifacts\bench\guide_wave_fd_20260425_150350.json artifacts\bench\guide_wave_fe_20260425_144829.json --output-dir artifacts\bench\rag_diagnostics_20260425_1503_fa_fb_fd_fe_fd_deterministic
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_rag_diagnostics.py artifacts\bench\rag_diagnostics_20260425_1448_fa_fb_fd_fe_current_taxonomy artifacts\bench\rag_diagnostics_20260425_1503_fa_fb_fd_fe_fd_deterministic --label current-taxonomy --label fd-deterministic
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_routing tests.test_registry_overlap tests.test_deterministic_near_miss -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

## Proof

- FD wave: `artifacts/bench/guide_wave_fd_20260425_150350.json`
- Combined panel:
  `artifacts/bench/rag_diagnostics_20260425_1503_fa_fb_fd_fe_fd_deterministic/`
- Panel quality score moved from `8.83` to `9.37`.
- Generated rows moved from `6/24` to `0/24`.
- Deterministic rows moved from `12` to `18`.
- Bad diagnostic buckets remained `0`.
- Owner concentration is unchanged: best-rank `1.36`, top-3 `21/24`, top-k
  `22/24`.

## Boundaries

- This is a deterministic safety-routing extension, not a new guide, answer
  card, Android implementation, or retrieval-ranking fix.
- It intentionally does not solve FA #1, FB #5, or FB #6 owner-concentration
  drift.

## Next Step

Remaining high-leverage score/quality targets:

- FB #5 expectation/prompt ambiguity: "should this be cleaned at home or
  treated urgently" has no wound context and currently lands in
  `uncertain_fit_accepted`.
- FA #1 deterministic poisoning row cites adjacent emergency guides but lacks
  expected-owner evidence support in the analyzer.
- FB #6 wound owner remains rank 4 even though the reviewed-card answer is now
  supported.
