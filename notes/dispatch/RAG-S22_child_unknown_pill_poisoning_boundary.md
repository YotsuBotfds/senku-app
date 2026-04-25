# RAG-S22 Child Unknown Pill Poisoning Boundary

## Slice

Route "child/toddler swallowed an unknown pill" prompts to the existing
child-ingestion poisoning deterministic rule instead of the generic
found-medication rule.

## Role

Scout-assisted safety-routing slice. The scout confirmed that
`unknown_medication` was intercepting FA #1 before the poisoning answer-card
lane could help.

## Preconditions

- `RAG-S21` combined panel reached quality score `9.37`, but FA #1 still
  surfaced as `needs_evidence_owner`.
- `unknown_child_ingestion` already had a poisoning-first answer with
  `GD-898` / `GD-232`, but its predicate required vomiting or cleaner wording.

## Outcome

- `_is_unknown_child_ingestion_special_case(...)` now catches the narrow
  combination of:
  - child marker;
  - ingestion verb;
  - unknown pill/medicine/tablet/capsule wording.
- `unknown_child_ingestion` now has priority `101`, so it intentionally wins
  over generic `unknown_medication` for already-swallowed child cases.
- The generic found-medication prompt still routes to `unknown_medication`.

## Acceptance

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py deterministic_special_case_registry.py tests\test_special_cases.py tests\test_registry_overlap.py tests\test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases.SpecialCaseTests.test_unknown_child_medication_ingestion_prefers_poisoning_rule tests.test_registry_overlap.RegistryOverlapTests.test_child_unknown_pill_overlap_prefers_child_ingestion_rule tests.test_query_routing.QueryRoutingTests.test_poisoning_ingestion_boundary_uses_safety_profile -v
$env:SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS='1'; powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave fa -PythonPath .\.venvs\senku-validate\Scripts\python.exe
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\guide_wave_fa_20260425_151325.json artifacts\bench\guide_wave_fb_20260425_144548.json artifacts\bench\guide_wave_fd_20260425_150350.json artifacts\bench\guide_wave_fe_20260425_144829.json --output-dir artifacts\bench\rag_diagnostics_20260425_1513_fa_child_pill_runtime_fd_deterministic
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_routing tests.test_registry_overlap tests.test_deterministic_near_miss -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

## Proof

- FA proof: `artifacts/bench/guide_wave_fa_20260425_151325.json`
- Combined proof:
  `artifacts/bench/rag_diagnostics_20260425_1513_fa_child_pill_runtime_fd_deterministic/`
- Combined quality score moved from `9.37` to `9.56`.
- Bad diagnostic buckets remain `0`.
- Generated rows remain `0/24`.
- App acceptance moved from `strong_supported:16|moderate_supported:6|uncertain_fit_accepted:1|needs_evidence_owner:1`
  to `strong_supported:17|moderate_supported:6|uncertain_fit_accepted:1`.
- Owner concentration improved from top-3 `21/24` and top-k `22/24` to top-3
  `22/24` and top-k `23/24`.

## Runtime Note

The comparable proof requires `SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1`.
Without that opt-in, FA #3-#6 fall back to generated RAG and the combined panel
regresses despite the FA #1 routing fix.

## Boundaries

- This does not change the generic "found unmarked pills" guidance.
- This does not change Android routing.
- This does not solve FB #5 prompt ambiguity or FB #6 wound owner rank drift.
