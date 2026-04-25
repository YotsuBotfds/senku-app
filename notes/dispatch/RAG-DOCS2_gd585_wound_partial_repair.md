# RAG-DOCS2 GD-585 Wound Partial Repair

## Slice

Replace the unresolved `{{> wound-assessment-tool }}` marker in
`guides/wound-hygiene-infection-prevention.md` with static, source-local
assessment content.

## Outcome

The marker had no backing partial or template source. The replacement is a
short `Wound Assessment Check` block derived from nearby GD-585 material:

- mark and recheck spreading redness
- check gently for fluctuance near the wound
- track fever and systemic symptoms
- inspect drainage and tissue color
- watch for red streaking and lymph node swelling

The edit intentionally avoids adding new procedures, formulas, or dose-like
medical claims. It preserves the existing transition into secondary wound care.

## Proof

Fresh corpus-marker scan:

- JSON: `artifacts/bench/corpus_marker_scan_20260425_1622_gd585_repair.json`
- Markdown: `artifacts/bench/corpus_marker_scan_20260425_1622_gd585_repair.md`
- Result: unresolved partial markers dropped from `55` to `54`.

Fresh priority outputs using the same diagnostic panel plus the repaired marker
scan:

- Guide priorities:
  `artifacts/bench/guide_quality_priorities_20260425_1622_gd585_repair.md`
- Family priorities:
  `artifacts/bench/high_liability_family_priorities_20260425_1622_gd585_repair.md`

The observed wound family now shows `corpus partial: 0` and candidate action
`expand_or_fix_answer_cards` instead of `repair_corpus_partial`.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\scan_corpus_markers.py scripts\prioritize_guide_quality_work.py scripts\prioritize_high_liability_families.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_scan_corpus_markers tests.test_prioritize_guide_quality_work tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_corpus_markers.py --guides-dir guides --output-json artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-md artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.md
```

Focused tests passed: `4`.

## Next

The wound row is no longer blocked on corpus-marker debt. The next useful wound
slice is answer-card coverage/routing review for the still-observed
`abstain_or_clarify_needed` diagnostic row and the `1/0/5` card pass/gap/no-card
family breakdown.
