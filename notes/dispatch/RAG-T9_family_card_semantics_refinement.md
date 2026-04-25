# RAG-T9 Family Card Semantics Refinement

## Slice

Refine `scripts/prioritize_high_liability_families.py` so deterministic rows do
not look like answer-card coverage gaps just because there is no generated
answer to evaluate.

## Outcome

Family rows now split card coverage into:

- `pass`: generated/reviewed answer satisfied the card contract
- `gap`: generated answer was partial or failed the card contract
- `missing`: a non-deterministic row needed card coverage and no card was found
- `skipped`: deterministic, uncertain-fit, or otherwise non-generated rows where
  card evaluation was not meaningful

This prevents deterministic pass rows from being routed into unnecessary
answer-card expansion work.

## Proof

Fresh family-priority artifact after the refinement:

- JSON: `artifacts/bench/high_liability_family_priorities_20260425_1645_card_semantics.json`
- Markdown: `artifacts/bench/high_liability_family_priorities_20260425_1645_card_semantics.md`

Observed changes from the same diagnostic panel plus the GD-585 repaired marker
scan:

- `infected_wound_spreading_redness`: `1/0/0/5`, action
  `inspect_retrieval_ranking`
- `dangerous_activation_mania_crisis`: `0/0/0/6`, action
  `add_targeted_metadata`
- `unknown_child_ingestion_poisoning`: `4/0/0/2`, action
  `add_targeted_metadata`
- `stroke_tia_cardiac_overlap`: `0/0/0/6`, action
  `add_targeted_metadata`

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\prioritize_high_liability_families.py tests\test_prioritize_high_liability_families.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_prioritize_high_liability_families tests.test_prioritize_guide_quality_work tests.test_rag_trend -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_high_liability_families.py artifacts\bench\rag_diagnostics_20260425_1546_runtime_plus_marker_overlay --metadata-audit artifacts\bench\metadata_coverage_audit_20260425_1556.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-json artifacts\bench\high_liability_family_priorities_20260425_1645_card_semantics.json --output-md artifacts\bench\high_liability_family_priorities_20260425_1645_card_semantics.md --limit 30
```

Focused tests passed: `12`.

## Next

Use the refined table before assigning family slices. The wound family is not
blocked on card expansion; the current measured issue is owner/citation drift
from deterministic citations and one ambiguous uncertain-fit prompt.
