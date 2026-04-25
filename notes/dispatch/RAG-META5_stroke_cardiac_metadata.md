# RAG-META5 Stroke/Cardiac Metadata

## Slice

Add targeted frontmatter metadata for the observed stroke/TIA plus cardiac
overlap family without changing guide body content or runtime behavior.

## Outcome

Updated `guides/acute-coronary-cardiac-emergencies.md` (`GD-601`) frontmatter:

- `aliases`
- `routing_cues`
- `citations_required`
- `applicability`

`GD-232` was already clean from `RAG-META3` and was left unchanged.

## Proof

Fresh metadata audit:

- JSON: `artifacts/bench/metadata_coverage_audit_20260425_1743_stroke_cardiac_metadata.json`
- Markdown: `artifacts/bench/metadata_coverage_audit_20260425_1743_stroke_cardiac_metadata.md`

Fresh family-priority panel:

- JSON:
  `artifacts/bench/high_liability_family_priorities_20260425_1743_stroke_cardiac_metadata.json`
- Markdown:
  `artifacts/bench/high_liability_family_priorities_20260425_1743_stroke_cardiac_metadata.md`

Measured effect:

- Stroke/cardiac family score dropped from `4` to `2`.
- `GD-232` is clean.
- `GD-601` now only shows `missing_reviewed_answer_card`.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\audit_metadata_coverage.py scripts\prioritize_high_liability_families.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --output-json artifacts\bench\metadata_coverage_audit_20260425_1743_stroke_cardiac_metadata.json --output-md artifacts\bench\metadata_coverage_audit_20260425_1743_stroke_cardiac_metadata.md
```

Focused tests passed: `4`.

## Next

The observed four-family panel now mostly points at reviewed-card coverage gaps
rather than missing frontmatter. Use fresh family priorities before adding more
metadata.
