# RAG-CARD3 GD-301 Poisoning Reviewed Card

## Slice

Add one reviewed answer-card contract for the observed
`unknown_child_ingestion_poisoning` family’s remaining `GD-301` coverage gap
without changing runtime routing, guide body content, or Android behavior.

## Outcome

Added `notes/specs/guide_answer_cards/poisoning_unknown_ingestion_toxicology.yaml`
owned by `GD-301` (`Toxicology and Poisoning Response`).

The card covers:

- unknown or uncertain ingestion / toxic exposure involving household
  chemicals, cleaners, pills, rodenticide, solvents, or unlabeled liquids;
- airway/breathing/circulation-first triage, poison control / EMS escalation,
  scene isolation, exposure-source control, and no induced vomiting;
- bounded support actions for fresh air, monitoring, contaminated clothing
  removal, water irrigation, and clean handoff documentation;
- boundaries against charcoal for unknown/caustic/hydrocarbon ingestions unless
  poison control directs it, scene neutralization/mixing, delayed airway
  checks, forced oral remedies, and routine household-chemistry framing.

Added focused contract coverage in `tests/test_guide_answer_card_contracts.py`
to prove the card loads, composes evidence units, produces a cited `GD-301`
answer, passes card and claim-support diagnostics, and still flags unnegated
harmful charcoal advice.

## Proof

Fresh metadata audit:

- JSON:
  `artifacts/bench/metadata_coverage_audit_20260425_1700_poisoning_gd301_card.json`
- Markdown:
  `artifacts/bench/metadata_coverage_audit_20260425_1700_poisoning_gd301_card.md`

Fresh family-priority panel:

- JSON:
  `artifacts/bench/high_liability_family_priorities_20260425_1700_poisoning_gd301_card.json`
- Markdown:
  `artifacts/bench/high_liability_family_priorities_20260425_1700_poisoning_gd301_card.md`

Measured result:

- high-liability guides with gaps: `243 -> 242`
- unknown-child-ingestion / poisoning family: score `2 -> 0`
- poisoning frontmatter/card gaps: `0/1 -> 0/0`
- family action changed to `regression_monitor`

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_guide_answer_cards.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile guide_answer_card_contracts.py scripts\validate_guide_answer_cards.py scripts\audit_metadata_coverage.py scripts\prioritize_high_liability_families.py tests\test_guide_answer_card_contracts.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_guide_answer_card_contracts tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --output-json artifacts\bench\metadata_coverage_audit_20260425_1700_poisoning_gd301_card.json --output-md artifacts\bench\metadata_coverage_audit_20260425_1700_poisoning_gd301_card.md
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_high_liability_families.py artifacts\bench\rag_diagnostics_20260425_1650_fb_support_owner_expectations --metadata-audit artifacts\bench\metadata_coverage_audit_20260425_1700_poisoning_gd301_card.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-json artifacts\bench\high_liability_family_priorities_20260425_1700_poisoning_gd301_card.json --output-md artifacts\bench\high_liability_family_priorities_20260425_1700_poisoning_gd301_card.md --limit 30
```

Focused tests passed: `39`.

## Next

Keep the poisoning family on regression monitoring. The remaining observed
reviewed-card coverage gaps are dangerous activation support guides (`GD-858`,
`GD-918`) and infected-wound support owners (`GD-235`, `GD-622`); do not add
those solely to close counters without a fresh behavior reason.
