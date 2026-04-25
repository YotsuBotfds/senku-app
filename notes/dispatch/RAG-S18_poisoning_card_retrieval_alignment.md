# RAG-S18 Poisoning Card Retrieval Alignment

## Slice

Tighten unknown-ingestion / poisoning prompts so reviewed-card runtime and
retrieval agree on the same owner family.

## Outcome

Possible ingestion prompts now have a strict poisoning-card predicate that
requires ingestion/exposure wording plus source and danger/uncertainty cues,
with an allergy/anaphylaxis guard unless the prompt is explicitly poisoning.
The predicate covers the expanded-panel failures:

- `sleepy after possible medicine ingestion`
- `mouth burns after tasting a liquid`
- `not sure what they ate but they are acting off`

When the predicate fires, retrieval uses the safety-triage profile, adds a
targeted unknown-ingestion / toxicology supplemental lane, and applies an
owner-positive metadata rerank for toxicology / poison-control guides. Reviewed
cards can also match retrieved source-guide families, not only primary guide
IDs, and priority cards may use the full retrieved allowlist when the top two
IDs are adjacent but not the owner.

`poisoning_unknown_ingestion` now opts into
`runtime_citation_policy: reviewed_source_family`, allowing GD-301 citation
when GD-898 is absent but the reviewed toxicology source family is retrieved.

## Proof

Fresh LiteRT/embedding FA smoke:

- `artifacts/bench/guide_wave_fa_20260425_143154.json`
- analyzer:
  `artifacts/bench/rag_diagnostics_20260425_1431_fa_poison_card_alignment/report.md`

FA before this slice, within the FA/FB/FD/FE expanded panel, had FA #3 as a
generated card-contract failure, FA #4 as partial, and FA #6 as uncertain fit.
After this slice:

| row | result |
| --- | --- |
| FA #3 sleepy after possible medicine ingestion | reviewed-card runtime; card pass; claim pass; evidence pass; still ranking_miss because GD-054 remains rank 1 |
| FA #4 mouth burns after tasting a liquid | reviewed-card runtime; strong supported; card pass; claim pass; evidence pass |
| FA #5 vomit or poison control | reviewed-card runtime; strong supported; card pass; claim pass; evidence pass |
| FA #6 not sure what they ate but acting off | reviewed-card runtime; strong supported; card pass; claim pass; evidence pass |

The remaining FA #3 issue is owner-family concentration / rank-1 drift, not
generation or card-contract coverage.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_guide_answer_card_contracts tests.test_query_answer_card_runtime tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_response_normalization tests.test_citation_validation tests.test_query_completion_hardening tests.test_query_prompt_runtime tests.test_query_citation_policy tests.test_rag_bench_answer_diagnostics tests.test_query_scenario_frame tests.test_query_routing_text tests.test_query_abstain_policy tests.test_query_answer_card_runtime tests.test_abstain tests.test_uncertain_fit tests.test_query_routing tests.test_bench_runtime tests.test_bench_config tests.test_runtime_profiles tests.test_analyze_rag_bench_failures tests.test_guide_answer_card_contracts tests.test_registry_overlap tests.test_deterministic_near_miss -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

Broad validation passed `322` unit tests plus `173` deterministic special-case
rules.

## Next

Build the owner-family concentration metric before more ranking tweaks, then
use it on FA #3 and FB #6 to quantify whether the expected owner family is
moving closer or merely still present somewhere in top-k.
