# RAG-DOCS7 Semaphore Flag Signaling Mojibake Cleanup

Date: 2026-04-25
Worker: Low Worker I

## Scope

- Cleaned obvious mojibake punctuation and symbol corruption in `guides/semaphore-flag-signaling.md`.
- No content rewrites, heading changes, frontmatter ID changes, semaphore mapping changes, table restructuring, tests, scripts, vector DB files, or ingest changes.

## Replacement Classes

- Corrupted en dash ranges converted to ASCII hyphen ranges.
- Corrupted em dash separators converted to spaced ASCII hyphen separators.
- Corrupted multiplication sign in binocular/flag dimensions converted to `x`.
- Corrupted right-arrow symbols converted to `->`.

## Validation

- Before: `scripts\scan_mojibake.py --paths guides\semaphore-flag-signaling.md --report-only` reported 66 findings / 66 gate findings.
- After: `scripts\scan_mojibake.py --paths guides\semaphore-flag-signaling.md --report-only` reported 0 findings / 0 gate findings.
- `git diff --check` passed with only the existing LF-to-CRLF warning for `guides/semaphore-flag-signaling.md`.
- Main ingest: `.\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\semaphore-flag-signaling.md guides\underground-shelter-bunker.md`
  - Batch processed `2` guides into `100` chunks.
  - `senku_guides` total chunks after ingest: `49,732`.
