# RAG-CARD2 Acute Coronary / Stroke Overlap Reviewed Card

## Slice

Add one reviewed answer-card contract for the observed acute coronary /
stroke-overlap family without changing runtime routing, guide body content, or
Android behavior.

## Outcome

Added `notes/specs/guide_answer_cards/acute_coronary_stroke_overlap.yaml`
owned by `GD-601` (`Acute Coronary Syndrome & Cardiac Emergencies`).

The card covers:

- chest pressure or heaviness with jaw/arm/back pain, cold sweat, shortness of
  breath, nausea, faintness, dread, or "something feels very wrong";
- mixed cardiac plus stroke/TIA presentations, including FAST-positive signs,
  sudden confusion, collapse, reduced alertness, vomiting, and swallowing
  concern;
- emergency-services / fastest-evacuation-first behavior, rest/positioning,
  breathing/pulse monitoring, CPR/AED escalation, and no oral intake when
  stroke or reduced-alertness signs are present;
- boundaries against routine anxiety/panic framing, pushing through symptoms,
  delaying evacuation for calming exercises, or treating the card as a
  medication dosing protocol.

Added focused contract coverage in `tests/test_guide_answer_card_contracts.py`
to prove the card composes a cited `GD-601` answer, passes the card contract,
and passes claim-support diagnostics without forbidden-advice false positives.

## Proof

Fresh metadata audit:

- JSON:
  `artifacts/bench/metadata_coverage_audit_20260425_1656_acute_coronary_card.json`
- Markdown:
  `artifacts/bench/metadata_coverage_audit_20260425_1656_acute_coronary_card.md`

Fresh family-priority panel:

- JSON:
  `artifacts/bench/high_liability_family_priorities_20260425_1656_acute_coronary_card.json`
- Markdown:
  `artifacts/bench/high_liability_family_priorities_20260425_1656_acute_coronary_card.md`

Measured result:

- high-liability guides with gaps: `243`
- stroke/cardiac overlap family: score `2 -> 0`
- stroke/cardiac frontmatter/card gaps: `0/1 -> 0/0`
- family action changed to `regression_monitor`

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile guide_answer_card_contracts.py scripts\audit_metadata_coverage.py scripts\prioritize_high_liability_families.py tests\test_guide_answer_card_contracts.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_guide_answer_card_contracts tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --output-json artifacts\bench\metadata_coverage_audit_20260425_1656_acute_coronary_card.json --output-md artifacts\bench\metadata_coverage_audit_20260425_1656_acute_coronary_card.md
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_high_liability_families.py artifacts\bench\rag_diagnostics_20260425_1650_fb_support_owner_expectations --metadata-audit artifacts\bench\metadata_coverage_audit_20260425_1656_acute_coronary_card.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-json artifacts\bench\high_liability_family_priorities_20260425_1656_acute_coronary_card.json --output-md artifacts\bench\high_liability_family_priorities_20260425_1656_acute_coronary_card.md --limit 30
```

Focused tests passed: `35`.

## Next

Keep this family on regression monitoring. The remaining observed reviewed-card
coverage gaps are dangerous activation (`GD-858`, `GD-918`), infected wound
support owners (`GD-235`, `GD-622`), and poisoning support owner `GD-301`.
