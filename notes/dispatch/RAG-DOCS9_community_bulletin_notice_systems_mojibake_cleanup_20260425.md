# RAG-DOCS9 Community Bulletin Notice Systems Mojibake Cleanup

Worker: Low Worker N
Date: 2026-04-25

Scope:
- `guides/community-bulletin-notice-systems.md`

Changes:
- Replaced mojibake punctuation for dashes/ranges and multiplication signs.
- Restored obvious visual symbols in the low-literacy icon table and examples.
- Restored box-drawing examples for board layout and emergency notice framing.

Validation:
- Before: `scan_mojibake.py --paths guides\community-bulletin-notice-systems.md --report-only` found 63 findings / 63 gate findings.
- After: `scan_mojibake.py --paths guides\community-bulletin-notice-systems.md --report-only` found 0 findings / 0 gate findings.
- `git diff --check` passed; Git emitted line-ending warnings only.

Notes:
- Main ran serialized incremental ingest after review.
- Headings, frontmatter IDs, procedural meaning, schedules, message formats, and tables were preserved.
- Main ingest: `.\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\community-bulletin-notice-systems.md guides\education-teaching.md`
  - Batch processed `2` guides into `228` chunks.
  - `senku_guides` total chunks after ingest: `49,730`.
