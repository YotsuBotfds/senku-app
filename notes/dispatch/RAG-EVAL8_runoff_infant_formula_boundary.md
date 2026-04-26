# RAG-EVAL8 Runoff Infant Formula Boundary

## Summary

Added a narrow deterministic guard for the RE8-RW-001 cluster: flood-affected well plus roof runoff plus infant formula pressure. The answer now leads with the infant/high-risk contamination boundary and avoids reassuring that boiling or a clean barrel proves the water safe.

## Scope

- Added `runoff_infant_formula_boundary` to the deterministic special-case registry.
- Added prompt and supplemental retrieval backstops for non-deterministic paths.
- Added focused routing and special-case tests for the roof-runoff/flooded-well baby-formula prompt.

## Validation

- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_routing -v` (`Ran 165 tests`, `OK`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py` (`Validated 174 deterministic special-case rules`).
- Pass: EVAL8 generation rerun `artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_generation_panel_20260425_2213.md`; RE8-RW-001 used `deterministic (runoff_infant_formula_boundary)` and led with "boiling does not prove chemical, sewage, roof-material, or flood contamination safe."
- Diagnostic buckets after rerun: `deterministic_pass: 3`, `expected_supported: 4`, `ranking_miss: 1`, `abstain_or_clarify_needed: 2`.
