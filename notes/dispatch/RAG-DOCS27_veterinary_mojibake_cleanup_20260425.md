# RAG-DOCS27 Veterinary Mojibake Cleanup

Date: 2026-04-25
Scope: `guides/veterinary.md`

## Summary

- Cleaned obvious mojibake punctuation and symbol artifacts in the veterinary guide.
- Replacement classes were degree temperature symbols, copper sulfate subscript formatting, arrow separators, dash punctuation, and one notes disclosure marker.
- Preserved headings, frontmatter, links, URLs, vital-sign values, medication storage ranges, deworming/drug-class guidance, disease warnings, birth/neonate care, food-safety thresholds, doses/percentages, timing, and procedural meaning.

## Validation

- Before cleanup: `scripts\scan_mojibake.py --paths guides\veterinary.md --report-only --markdown-limit 200` reported 55 findings.
- After cleanup: `scripts\scan_mojibake.py --paths guides\veterinary.md --report-only --markdown-limit 200` reported 0 findings.
- Diff hygiene: `git diff --check -- guides\veterinary.md notes\dispatch\RAG-DOCS27_veterinary_mojibake_cleanup_20260425.md` passed.

## Review Notes

- Changed hunks were reviewed for semantic drift in safety-sensitive areas, including temperature thresholds, medication/dosing-like text, disease-control warnings, zoonotic risk language, food-safety cooling lines, birthing/neonate care, and procedure limits.
- Main re-ingest: `ingest.py --files guides\veterinary.md --force-files` processed 1 file / 105 chunks; collection total 49,725.
