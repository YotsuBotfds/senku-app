# RAG-EVAL5 Deterministic Hijack Repair

## Slice

Repair two deterministic-rule false positives surfaced by the `RAG-EVAL2`
partial/router held-out pack.

## Rows

- `RE2-UP-002`: broad accident triage prompt expected `GD-029` but routed to
  `infected_wound_spreading_emergency`.
- `RE2-BR-004`: electrical restoration prompt expected `GD-649` but routed to
  `classic_stroke_fast` because substring matching saw `tia` inside unrelated
  words.

## Change

- Stroke/TIA direct markers now use word-boundary marker matching.
- Infected-wound systemic danger now requires wound context plus
  infection-specific context before confusion, dizziness, or similar systemic
  terms can activate the deterministic route.
- Added regression tests proving the two held-out prompts fall through while
  intended stroke and infected-wound prompts still route deterministically.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases.SpecialCaseTests.test_stroke_fast_does_not_match_electrical_restoration_language tests.test_special_cases.SpecialCaseTests.test_infected_wound_requires_infection_specific_context_for_systemic_danger tests.test_special_cases.SpecialCaseTests.test_single_fast_stroke_en_prompts_route tests.test_special_cases.SpecialCaseTests.test_infected_wound_spreading_emergency_dl_prompts_route tests.test_registry_overlap tests.test_deterministic_near_miss -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

Result: focused routing, registry overlap, deterministic near-miss, and
special-case registry validation passed.
