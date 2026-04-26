# RAG-META8 Heat/Toxicology/Water Family Metadata

## Summary

Added targeted frontmatter metadata for the observed heat illness, toxic exposure, and unsafe-water family:

- GD-526 `thermal-injuries`: added `aliases`, `routing_cues`, `citations_required`, and `applicability`.
- GD-054 `toxicology`: added `aliases`, `routing_cues`, `citations_required`, and `applicability`.
- GD-602 `toxidromes-field-poisoning`: added `aliases`, `routing_cues`, `citations_required`, and `applicability`.
- GD-035 `water-purification`: added `citations_required`.

No guide body content, procedures, titles, IDs, or answer cards were changed.

## Validation

- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B ingest.py --files guides\thermal-injuries.md guides\toxicology.md guides\toxidromes-field-poisoning.md guides\water-purification.md --force-files` (`Files processed: 4`, `Total chunks: 347`, collection total `49709`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v` (`Ran 5 tests`, `OK`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\thermal-injuries.md guides\toxicology.md guides\toxidromes-field-poisoning.md guides\water-purification.md --report-only --markdown-limit 200` (`findings_count: 0`, `gate_findings_count: 0`).
- Pass: `git diff --check -- guides/thermal-injuries.md guides/toxicology.md guides/toxidromes-field-poisoning.md guides/water-purification.md`.
- Fresh audit confirmed GD-526, GD-054, GD-602, and GD-035 now have the targeted routing/citation/applicability fields; remaining gap is reviewed answer-card coverage.
- Fresh priority report moved this family from `add_targeted_metadata` with `4/4` frontmatter/card gaps to `consider_reviewed_answer_card` with `0/4`.
