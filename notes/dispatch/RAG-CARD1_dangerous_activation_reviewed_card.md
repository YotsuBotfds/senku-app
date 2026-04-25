# RAG-CARD1 Dangerous Activation Reviewed Card

## Slice

Add one reviewed answer-card contract for the observed dangerous activation /
acute mania crisis family without changing runtime routing, guide body content,
or Android behavior.

## Outcome

Added `notes/specs/guide_answer_cards/dangerous_activation_mania_crisis.yaml`
owned by `GD-859` (`Recognizing Mental Health Crises`).

The card covers:

- days of little or no sleep plus grandiosity, recklessness,
  invincible-seeming behavior, or nonstop talking;
- urgent medical/crisis evaluation when judgment, reality testing, or safety is
  compromised;
- supervised safety, reduced stimulation, and obvious-means reduction;
- boundaries against routine anxiety, motivation, grounding-only, or peer
  support-only framing.

Added focused contract coverage in `tests/test_guide_answer_card_contracts.py`
to prove the card composes a cited `GD-859` answer, passes the card contract,
and passes claim-support diagnostics.

## Proof

Fresh metadata audit:

- JSON:
  `artifacts/bench/metadata_coverage_audit_20260425_1650_dangerous_activation_card.json`
- Markdown:
  `artifacts/bench/metadata_coverage_audit_20260425_1650_dangerous_activation_card.md`

Fresh family-priority panel:

- JSON:
  `artifacts/bench/high_liability_family_priorities_20260425_1650_dangerous_activation_card.json`
- Markdown:
  `artifacts/bench/high_liability_family_priorities_20260425_1650_dangerous_activation_card.md`

Measured result:

- high-liability guides with gaps: `244`
- dangerous activation family: score `6 -> 4`
- dangerous activation frontmatter/card gaps: `0/3 -> 0/2`
- `GD-859` no longer appears as a reviewed-card gap; `GD-858` and `GD-918`
  remain boundary-support guides without primary reviewed-card ownership.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile guide_answer_card_contracts.py scripts\audit_metadata_coverage.py scripts\prioritize_high_liability_families.py tests\test_guide_answer_card_contracts.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_guide_answer_card_contracts tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --output-json artifacts\bench\metadata_coverage_audit_20260425_1650_dangerous_activation_card.json --output-md artifacts\bench\metadata_coverage_audit_20260425_1650_dangerous_activation_card.md
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_high_liability_families.py artifacts\bench\rag_diagnostics_20260425_1650_fb_support_owner_expectations --metadata-audit artifacts\bench\metadata_coverage_audit_20260425_1650_dangerous_activation_card.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-json artifacts\bench\high_liability_family_priorities_20260425_1650_dangerous_activation_card.json --output-md artifacts\bench\high_liability_family_priorities_20260425_1650_dangerous_activation_card.md --limit 30
```

Focused tests passed: `34`.

## Next

Do not add routine anxiety or PFA cards just to zero this family. If continuing
reviewed-card coverage, prefer a measured support-guide card only when a fresh
prompt panel needs that guide as a primary reviewed-card owner.
