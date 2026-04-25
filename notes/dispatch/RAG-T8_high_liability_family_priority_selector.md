# RAG-T8 High-Liability Family Priority Selector

## Slice

Collapse observed guide-level priority evidence into family-grain work items so
real follow-up slices can target coherent retrieval/card/content surfaces.

## Outcome

Adds `scripts/prioritize_high_liability_families.py`, which consumes one or
more RAG diagnostic directories plus optional metadata and marker audits. It
emits JSON/Markdown family rows with:

- expected guide family and guide IDs
- prompt, safety, generated, non-deterministic, and bad-bucket row counts
- owner-rank concentration averages
- card pass/gap/missing/skipped counts
- reviewed-card guide coverage
- metadata gap counts
- top-1 and corpus unresolved-partial counts
- recurring distractor IDs
- candidate action and reason

## Proof

Fresh family-priority artifact:

- JSON: `artifacts/bench/high_liability_family_priorities_20260425_1606.json`
- Markdown: `artifacts/bench/high_liability_family_priorities_20260425_1606.md`

The selector ranked four observed families from the fresh FA/FB/FD/FE panel:

1. `infected_wound_spreading_redness`: repair corpus partial
2. `dangerous_activation_mania_crisis`: expand or fix answer cards
3. `stroke_tia_cardiac_overlap`: expand or fix answer cards
4. `unknown_child_ingestion_poisoning`: expand or fix answer cards

This is a better assignment table than the raw `248` metadata gaps because it
starts from measured prompt behavior.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\prioritize_high_liability_families.py tests\test_prioritize_high_liability_families.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_high_liability_families.py artifacts\bench\rag_diagnostics_20260425_1546_runtime_plus_marker_overlay --metadata-audit artifacts\bench\metadata_coverage_audit_20260425_1556.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1544.json --output-json artifacts\bench\high_liability_family_priorities_20260425_1606.json --output-md artifacts\bench\high_liability_family_priorities_20260425_1606.md --limit 30
```

Focused validation passed `1` test.

## Next

Use the family table before assigning content or answer-card work. The current
top family is wound because observed prompts intersect with an unresolved
partial on `GD-585`; the next safest slice is likely to inspect and replace or
classify that partial rather than broad metadata churn.

## Follow-Up

`RAG-T9` refined the card columns after `RAG-DOCS2`: deterministic rows with no
generated answer are now counted as skipped/not evaluable instead of missing
answer-card coverage. Re-run the selector before assigning new card-expansion
work.
