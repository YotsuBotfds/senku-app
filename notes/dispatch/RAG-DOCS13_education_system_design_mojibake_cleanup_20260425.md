# RAG-DOCS13 Education System Design Mojibake Cleanup

Date: 2026-04-25
Worker: Low Worker S

## Scope

- Touched only `guides/education-system-design.md`.
- Cleanup was limited to obvious mojibake punctuation/symbols.
- No ingest run; main serializes ingest.

## Replacement Classes

- Restored mojibake en dash ranges to `–`.
- Restored mojibake em dash separators to `—`.
- Restored mojibake multiplication sign in `10×10`.

## Validation

- Before: `scan_mojibake.py --paths guides\education-system-design.md --report-only` reported 60 findings / 60 gate findings.
- After: `scan_mojibake.py --paths guides\education-system-design.md --report-only` reported 0 findings / 0 gate findings.
- `git diff --check` passed.
- Main re-ingest: `ingest.py --files guides\education-system-design.md guides\elder-care.md --force-files` processed 2 files / 188 chunks; collection total 49,728.
