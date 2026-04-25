# RAG-S17 Uncertain-Fit Reviewed-Card Fallback

## Slice

Let strict reviewed-card runtime answer plans fire before uncertain-fit copy when
the runtime can produce a ready, cited reviewed-card answer.

## Role

Answer-quality lane. This targets the two selected-panel rows that were
non-deterministic, non-generated uncertain fits even though a reviewed emergency
card could answer under the existing opt-in runtime gate.

## Outcome

`bench.prepare_prompt(...)` and `query.stream_response(...)` now keep abstain
precedence first, then consult the reviewed-card runtime planner before falling
back to uncertain-fit copy. The reviewed-card helper still owns all strict
prerequisites:

- `SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1`;
- exactly one matching reviewed card;
- approved or pilot-reviewed status;
- critical or high risk tier;
- citation allowlist/source-family compatibility;
- ready composed answer with citations.

If no reviewed-card answer is available, uncertain-fit behavior remains
unchanged.

## Proof

Fresh LiteRT/embedding smoke over EX/EY/EZ/FC:

- `artifacts/bench/guide_wave_ex_20260425_140722.json`
- `artifacts/bench/guide_wave_ey_20260425_140726.json`
- `artifacts/bench/guide_wave_ez_20260425_140736.json`
- `artifacts/bench/guide_wave_fc_20260425_140743.json`
- analyzer:
  `artifacts/bench/rag_diagnostics_20260425_1407_uncertain_fit_card_fallback/report.md`

Trend versus the prior RAGS13 final smoke:

| metric | before | after |
| --- | ---: | ---: |
| reviewed-card runtime answers | 8 | 10 |
| answer-card pass | 8 | 10 |
| claim-support pass | 9 | 11 |
| no-generated-answer card rows | 15 | 13 |
| generated prompts | 1 | 1 |
| retrieval/ranking/generation/safety misses | 0 | 0 |

The two newly answered rows are still `answer_mode=uncertain_fit` /
`app_acceptance_status=uncertain_fit_accepted`. That is intentional for this
slice: runtime answer coverage improved without changing the app-gate/product
surface policy for uncertain comparison prompts.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py bench.py tests\test_bench_runtime.py tests\test_query_routing.py tests\test_uncertain_fit.py tests\test_abstain.py scripts\analyze_rag_bench_failures.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_bench_runtime tests.test_query_routing tests.test_uncertain_fit tests.test_abstain tests.test_query_answer_card_runtime tests.test_analyze_rag_bench_failures -v
```

Focused validation passed `124` tests.

## Next

Decide whether reviewed-card answers that originate from uncertain-fit routing
should remain visually limited-fit or surface as reviewed-card evidence. That is
a product/UX policy decision, not a retrieval fix.
