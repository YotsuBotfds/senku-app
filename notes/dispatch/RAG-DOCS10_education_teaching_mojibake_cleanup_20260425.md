# RAG-DOCS10 Education Teaching Mojibake Cleanup

Worker: Low Worker O
Date: 2026-04-25

## Scope

- `guides/education-teaching.md`

## Changes

- Replaced obvious mojibake punctuation and symbols only.
- Replacement classes: em dash, spaced em dash, right arrow, checkmark, multiplication sign.
- Preserved headings, frontmatter IDs, age ranges, schedules, lesson structures, tables, and procedural meaning.

## Validation

- Before: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\education-teaching.md --report-only`
  - `findings_count`: 56
  - `gate_findings_count`: 56
- After: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\education-teaching.md --report-only`
  - `findings_count`: 0
  - `gate_findings_count`: 0
- `git diff --check`
  - Exit 0; emitted CRLF normalization warnings only.

## Notes

- Main ran serialized incremental ingest after review.
- Main ingest: `.\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\community-bulletin-notice-systems.md guides\education-teaching.md`
  - Batch processed `2` guides into `228` chunks.
  - `senku_guides` total chunks after ingest: `49,730`.
