# RAG-DOCS17 Trapping Snares Mojibake Cleanup

Date: 2026-04-25

## Scope

- Cleaned obvious mojibake punctuation and symbol artifacts only in `guides/trapping-snares.md`.
- No snare/trap dimensions, wire sizes, distances, trigger descriptions, welfare/legal/safety warnings, check intervals, headings, frontmatter, links, or procedural meaning were intentionally changed.

## Replacement Classes

- Mojibaked en dash sequences restored to en dashes in numeric ranges and captions.
- Mojibaked em dash sequences restored to em dashes in explanatory punctuation.
- Mojibaked multiplication signs restored in box/cage/body-grip/net dimensions.
- Mojibaked degree sign restored in drying temperature.
- Mojibaked lightning symbols restored in diagram alt text.
- Mojibaked down-triangle symbol restored in notes UI text.

## Safety And Procedure Contexts Checked

- Legal and animal welfare notices, modern legality warning, trap checking intervals, and humane dispatch context.
- Snare placement, spring/drag snare construction, wire specification table, and anchor/camouflage guidance.
- Deadfall weights, trigger descriptions, and rock stability guidance.
- Box, cage, body-grip, leg-hold, fish trap, trotline, limb line, jug fishing, and gill net dimensions/check intervals.
- Fur/pelt processing temperature and drying timing.
- Affiliate/link text and note UI artifacts.

## Validation

- Before mojibake scan: `findings_count=61`, `gate_findings_count=61`.
- After mojibake scan: `findings_count=0`, `gate_findings_count=0`.
- Command: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\trapping-snares.md --report-only --markdown-limit 200`.
- Diff check: `git diff --check -- guides\trapping-snares.md notes\dispatch\RAG-DOCS17_trapping_snares_mojibake_cleanup_20260425.md` passed; Git emitted only an LF-to-CRLF working-copy warning for `guides/trapping-snares.md`.

## Ingest

- Main re-ingest: `ingest.py --files guides\veterinary-parasitology.md guides\trapping-snares.md --force-files` processed 2 files / 169 chunks; collection total 49,726.
