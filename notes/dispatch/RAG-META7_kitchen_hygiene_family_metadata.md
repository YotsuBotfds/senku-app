# RAG-META7 Kitchen/Hygiene Family Metadata

## Summary

Added targeted frontmatter metadata for the current top observed kitchen illness family:

- GD-666 `food-safety-contamination-prevention`: added `citations_required` and `applicability`.
- GD-732 `hygiene-disease-prevention-basics`: added `routing_cues`, `citations_required`, and `applicability`.

No guide body content, procedures, titles, IDs, or answer cards were changed.

## Validation

- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B ingest.py --files guides\food-safety-contamination-prevention.md guides\hygiene-disease-prevention-basics.md --force-files` (`Files processed: 2`, `Total chunks: 127`, collection total `49709`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v` (`Ran 5 tests`, `OK`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\food-safety-contamination-prevention.md guides\hygiene-disease-prevention-basics.md --report-only --markdown-limit 200` (`findings_count: 0`, `gate_findings_count: 0`).
- Pass: `git diff --check -- guides/food-safety-contamination-prevention.md guides/hygiene-disease-prevention-basics.md`.
- Fresh audit confirmed GD-666 and GD-732 now have routing/citation/applicability fields; remaining gap is reviewed answer-card coverage.
- Fresh priority report moved the top family frontmatter/card gaps from `2/2` to `0/2`.
