# RAG-META4 Poisoning Family Metadata

## Slice

Add targeted frontmatter metadata for the observed unknown-ingestion / poisoning
family without changing guide body content or runtime behavior.

## Outcome

Updated frontmatter only:

- `guides/toxicology-poisoning-response.md` (`GD-301`): `aliases`,
  `routing_cues`, `citations_required`, `applicability`
- `guides/unknown-ingestion-child-poisoning-triage.md` (`GD-898`): `aliases`,
  `routing_cues`

`GD-232` already had the needed metadata from `RAG-META3`.

## Proof

Fresh metadata audit:

- JSON: `artifacts/bench/metadata_coverage_audit_20260425_1738_poisoning_metadata.json`
- Markdown: `artifacts/bench/metadata_coverage_audit_20260425_1738_poisoning_metadata.md`

Fresh family-priority panel:

- JSON:
  `artifacts/bench/high_liability_family_priorities_20260425_1738_poisoning_metadata.json`
- Markdown:
  `artifacts/bench/high_liability_family_priorities_20260425_1738_poisoning_metadata.md`

Measured effect:

- Poisoning family score dropped from `6` to `2`.
- `GD-232` and `GD-898` now have no metadata gaps in this family.
- `GD-301` now only shows `missing_reviewed_answer_card`.
- High-liability guides with gaps dropped from `246` to `245`.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\audit_metadata_coverage.py scripts\prioritize_high_liability_families.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --output-json artifacts\bench\metadata_coverage_audit_20260425_1738_poisoning_metadata.json --output-md artifacts\bench\metadata_coverage_audit_20260425_1738_poisoning_metadata.md
```

Focused tests passed: `4`.

## Next

The remaining poisoning-family metadata drag is reviewed answer-card coverage
for `GD-301`; do not keep adding frontmatter unless a fresh audit exposes a new
metadata gap.
