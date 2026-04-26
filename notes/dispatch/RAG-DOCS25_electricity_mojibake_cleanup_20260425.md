# RAG-DOCS25 Electricity Mojibake Cleanup

Date: 2026-04-25
Scope: `guides/electricity.md`

## Summary

- Replaced one obvious mojibake punctuation artifact with the intended em dash.
- Preserved headings, frontmatter, links, measurements, formulas, and electrical safety language.
- Main re-ingest: `ingest.py --files guides\electricity.md guides\fish-cleaning-preparation.md --force-files` processed 2 files / 129 chunks; collection total 49,725.

## Validation

- Before cleanup: `scripts\scan_mojibake.py --paths guides\electricity.md --report-only --markdown-limit 200` reported one scanned file with one gate finding.
- After cleanup: `scripts\scan_mojibake.py --paths guides\electricity.md --report-only --markdown-limit 200` reported one scanned file with zero findings and zero gate findings.
- Diff check: `git diff --check -- guides\electricity.md notes\dispatch\RAG-DOCS25_electricity_mojibake_cleanup_20260425.md` passed; Git emitted an existing line-ending warning for `guides/electricity.md`.
