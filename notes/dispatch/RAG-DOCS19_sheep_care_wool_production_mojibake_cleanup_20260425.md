# RAG-DOCS19 Sheep Care Wool Production Mojibake Cleanup 2026-04-25

Scope: clean obvious mojibake punctuation and symbol artifacts in `guides/sheep-care-wool-production.md` only.

Changed:

- Replaced corrupted dash artifacts with intended em dashes for asides and en dashes for numeric ranges.
- Replaced corrupted degree and multiplication symbols in temperature and revenue examples.
- Preserved frontmatter, headings, sheep-care procedures, shearing and wool handling steps, measurements, intervals, health warnings, links, and URLs.

Safety/context check:

- Reviewed changed-line contexts for animal-care safety, quantities, and timing.
- No sheep-care procedure, dose-like value, interval, threshold, warning, or URL content was intentionally changed beyond punctuation and symbol restoration.

Validation:

- Before scan: `scripts\scan_mojibake.py --paths guides\sheep-care-wool-production.md --report-only --markdown-limit 200` reported 47 findings.
- After scan: `scripts\scan_mojibake.py --paths guides\sheep-care-wool-production.md --report-only --markdown-limit 200` reported 0 findings.
- `git diff --check -- guides\sheep-care-wool-production.md notes\dispatch\RAG-DOCS19_sheep_care_wool_production_mojibake_cleanup_20260425.md` passed.
- Main re-ingest: `ingest.py --files guides\sheep-care-wool-production.md --force-files` processed 1 file / 46 chunks; collection total 49,725.
