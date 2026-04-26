# RAG-META9 Winter Survival Citation Policy

## Summary

Added `citations_required: true` to GD-024 `winter-survival-systems` so below-freezing shelter and hypothermia-prevention guidance is treated as citation-required high-liability support.

No guide body content, procedures, titles, IDs, routing cues, or answer cards were changed.

## Validation

- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B ingest.py --files guides\winter-survival-systems.md --force-files` (`Files processed: 1`, `Total chunks: 147`, collection total `49709`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v` (`Ran 5 tests`, `OK`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\winter-survival-systems.md --report-only --markdown-limit 200` (`findings_count: 0`, `gate_findings_count: 0`).
- Pass: `git diff --check -- guides/winter-survival-systems.md`.
