# RAG-META3 Wound Family Metadata

## Slice

Add targeted high-liability frontmatter metadata for the observed infected-wound
family support guides without changing guide body content or deterministic
rules.

## Outcome

Updated frontmatter only:

- `guides/first-aid.md` (`GD-232`): `routing_cues`, `citations_required`
- `guides/sepsis-recognition-antibiotic-protocols.md` (`GD-589`):
  `routing_cues`, `citations_required`
- `guides/infection-control.md` (`GD-235`): `aliases`, `routing_cues`,
  `citations_required`, `applicability`
- `guides/animal-bite-wound-care.md` (`GD-622`): `aliases`, `routing_cues`,
  `citations_required`, `applicability`

`GD-585` already had the needed metadata and was left unchanged.

## Proof

Fresh metadata audit:

- JSON: `artifacts/bench/metadata_coverage_audit_20260425_1732_wound_family_metadata.json`
- Markdown: `artifacts/bench/metadata_coverage_audit_20260425_1732_wound_family_metadata.md`

Fresh family-priority panel:

- JSON:
  `artifacts/bench/high_liability_family_priorities_20260425_1732_wound_family_metadata.json`
- Markdown:
  `artifacts/bench/high_liability_family_priorities_20260425_1732_wound_family_metadata.md`

Measured effect:

- Wound family score dropped from `14` to `4`.
- `GD-232`, `GD-585`, and `GD-589` now have no metadata gaps in this family.
- `GD-235` and `GD-622` now only show `missing_reviewed_answer_card`.
- High-liability guides with gaps dropped from `248` to `246`.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\audit_metadata_coverage.py scripts\prioritize_high_liability_families.py tests\test_audit_metadata_coverage.py tests\test_prioritize_high_liability_families.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --output-json artifacts\bench\metadata_coverage_audit_20260425_1732_wound_family_metadata.json --output-md artifacts\bench\metadata_coverage_audit_20260425_1732_wound_family_metadata.md
```

Focused tests passed: `4`.

## Next

If the wound family returns to the top of the measured queue, the remaining
frontmatter-adjacent gap is reviewed-card coverage for `GD-235` and `GD-622`,
not more metadata.
