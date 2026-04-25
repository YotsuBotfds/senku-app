# RAG-S19 Infected Wound Reviewed-Card Runtime

## Slice

Let the reviewed `infected_wound_spreading_infection` card answer wound
boundary prompts when `GD-585` is already retrieved but outside the top-two
card-selection window.

## Outcome

`query_answer_card_runtime.py` now accepts an injected
`is_infected_wound_card_query` predicate. The desktop wrapper wires that to the
existing `_is_infected_wound_boundary_query`.

This keeps retrieval/ranking behavior unchanged but allows the strict reviewed
card runtime path to select the wound card from the full retrieved allowlist
when the prompt is a wound-infection boundary. Non-wound worsening pain remains
excluded.

## Proof

Fresh LiteRT/embedding FB smoke:

- `artifacts/bench/guide_wave_fb_20260425_144548.json`
- analyzer:
  `artifacts/bench/rag_diagnostics_20260425_1445_fb_wound_card_runtime/report.md`

FB #6, `wound pain is getting worse instead of better`, changed from generated
RAG citing adjacent guides to reviewed-card runtime citing `GD-585`:

| metric | before | after |
| --- | --- | --- |
| decision path | `rag` | `card_backed_runtime` |
| answer provenance | `generated_model` | `reviewed_card_runtime` |
| app acceptance | `needs_evidence_owner` | `moderate_supported` |
| answer card | `partial` | `pass` |
| claim support | `pass` | `pass` |
| evidence nugget | `missing` | `pass` |
| cited guides | `GD-300|GD-732` | `GD-585` |

Owner concentration intentionally did not change: `GD-585` remains rank 4,
with top-k owner share `0.2`. This slice fixes answer/card/citation behavior
without claiming the remaining ranking drift is solved.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_answer_card_runtime tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_response_normalization tests.test_citation_validation tests.test_query_completion_hardening tests.test_query_prompt_runtime tests.test_query_citation_policy tests.test_rag_bench_answer_diagnostics tests.test_query_scenario_frame tests.test_query_routing_text tests.test_query_abstain_policy tests.test_query_answer_card_runtime tests.test_abstain tests.test_uncertain_fit tests.test_query_routing tests.test_bench_runtime tests.test_bench_config tests.test_runtime_profiles tests.test_analyze_rag_bench_failures tests.test_guide_answer_card_contracts tests.test_registry_overlap tests.test_deterministic_near_miss tests.test_rag_trend -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

Broad validation passed `336` unit tests plus `173` deterministic special-case
rules.

## Next

If desired, use the owner-family concentration metric for a later pure ranking
slice. The failed broad rerank experiment showed that changing wound retrieval
language can make FB #6 worse by pushing `GD-585` out of top-k, so prefer a
measured rank-improvement slice over another safety-profile broadening patch.
