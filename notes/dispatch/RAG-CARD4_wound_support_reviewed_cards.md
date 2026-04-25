# RAG-CARD4 - Wound Support Reviewed Cards

## Status

Landed 2026-04-25.

## Scope

Add focused reviewed answer-card coverage for the wound-family support owners
that remained open after metadata coverage:

- `GD-235` / `infection-control`: wound infection escalation, red streaking,
  lymphangitis, spreading cellulitis, systemic deterioration, and safe
  contaminated wound handling.
- `GD-622` / `animal-bite-wound-care`: bite/puncture infection support,
  irrigation, delayed/open closure posture, hand-bite functional decline, and
  rabies time-critical escalation.

## Files

- `notes/specs/guide_answer_cards/infection_control_wound_infection.yaml`
- `notes/specs/guide_answer_cards/animal_bite_wound_infection_support.yaml`
- `tests/test_guide_answer_card_contracts.py`

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_guide_answer_cards.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile guide_answer_card_contracts.py rag_bench_answer_diagnostics.py query_answer_card_runtime.py mobile_pack_answer_cards.py scripts\validate_guide_answer_cards.py scripts\audit_metadata_coverage.py scripts\prioritize_high_liability_families.py tests\test_guide_answer_card_contracts.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_guide_answer_card_contracts tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --cards-dir notes\specs\guide_answer_cards --output-json artifacts\bench\metadata_coverage_audit_20260425_1720_wound_support_cards.json --output-md artifacts\bench\metadata_coverage_audit_20260425_1720_wound_support_cards.md
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_high_liability_families.py artifacts\bench\rag_diagnostics_20260425_1708_runtime_card_id_filter --metadata-audit artifacts\bench\metadata_coverage_audit_20260425_1720_wound_support_cards.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-json artifacts\bench\high_liability_family_priorities_20260425_1720_wound_support_cards.json --output-md artifacts\bench\high_liability_family_priorities_20260425_1720_wound_support_cards.md
```

Result: card schema validation passed for `11` cards; compile passed; focused
unit suite passed `44` tests; audit shows `240` high-liability guides with gaps;
family selector ranks infected-wound / spreading-redness at score `0`,
frontmatter/card gaps `0/0`, action `regression_monitor`.

## Follow-Up

Do not add more wound-family cards just to increase coverage counts. Reopen
only if a fresh behavior panel exposes a real support-owner miss.
