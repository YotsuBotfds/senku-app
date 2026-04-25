# RAG-T11 Family Reviewed-Card Gap Semantics

## Slice

Refine the family-priority selector after the high-liability metadata cleanup
so it does not keep recommending frontmatter work when the only remaining
metadata-audit gap is `missing_reviewed_answer_card`.

## Outcome

Updated `scripts/prioritize_high_liability_families.py`:

- `metadata_gap_guide_count` now counts frontmatter/routing metadata gaps only.
- `missing_reviewed_answer_card` is tracked separately as
  `reviewed_answer_card_gap_guide_count`.
- Family actions now use `consider_reviewed_answer_card` when a family is
  otherwise clean but still lacks reviewed-card coverage on expected guides.
- Markdown output reports `frontmatter/card gaps` as `frontmatter/card`.

Added focused coverage in `tests/test_prioritize_high_liability_families.py`
for card-only gaps and mixed card/frontmatter gaps.

## Proof

Fresh family-priority panel:

- JSON:
  `artifacts/bench/high_liability_family_priorities_20260425_1648_reviewed_card_semantics.json`
- Markdown:
  `artifacts/bench/high_liability_family_priorities_20260425_1648_reviewed_card_semantics.md`

Measured result:

- `dangerous_activation_mania_crisis`: `0/3` frontmatter/card gaps,
  `consider_reviewed_answer_card`
- `infected_wound_spreading_redness`: `0/2` frontmatter/card gaps,
  `consider_reviewed_answer_card`
- `stroke_tia_cardiac_overlap`: `0/1` frontmatter/card gaps,
  `consider_reviewed_answer_card`
- `unknown_child_ingestion_poisoning`: `0/1` frontmatter/card gaps,
  `consider_reviewed_answer_card`

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\prioritize_high_liability_families.py tests\test_prioritize_high_liability_families.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_prioritize_high_liability_families tests.test_prioritize_guide_quality_work tests.test_rag_trend -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_high_liability_families.py artifacts\bench\rag_diagnostics_20260425_1650_fb_support_owner_expectations --metadata-audit artifacts\bench\metadata_coverage_audit_20260425_1743_stroke_cardiac_metadata.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-json artifacts\bench\high_liability_family_priorities_20260425_1648_reviewed_card_semantics.json --output-md artifacts\bench\high_liability_family_priorities_20260425_1648_reviewed_card_semantics.md --limit 30
```

Focused tests passed: `13`.

## Next

Do not add more frontmatter to these four families unless a fresh metadata
audit shows a non-card gap. The next quality slice should choose whether to add
reviewed-card coverage for the highest-value support guide or whether to pursue
`RAG-RUNTIME1` / `RAG-TRUST1` from the deep-research backlog.
