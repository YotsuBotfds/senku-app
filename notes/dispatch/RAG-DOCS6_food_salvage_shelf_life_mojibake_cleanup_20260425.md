# RAG-DOCS6 Food Salvage Shelf Life Mojibake Cleanup

Worker: Low Worker G
Date: 2026-04-25
Scope: `guides/food-salvage-shelf-life.md`

## Summary

- Repaired obvious mojibake punctuation and symbol corruption only.
- No content, thresholds, temperatures, shelf-life durations, headings, or frontmatter IDs were intentionally changed.
- Main ran serialized incremental ingest after integration review.

## Replacement Classes

- Double-encoded em dash mojibake -> em dash.
- Double-encoded Fahrenheit/Celsius degree markers -> degree sign plus `F` / `C`.
- Double-encoded accented entree spelling -> `entrée`.

## Validation

- Pre-patch: `.\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\food-salvage-shelf-life.md --report-only`
  - Result: `findings_count=83`, `gate_findings_count=83`.
- Post-patch: `.\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\food-salvage-shelf-life.md --report-only`
  - Result: `findings_count=0`, `gate_findings_count=0`.
- Post-patch: `git diff --check`
  - Result: passed; only existing line-ending warnings were emitted for `guides/first-aid.md` and `guides/food-salvage-shelf-life.md`.
- Main ingest: `.\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\food-salvage-shelf-life.md`
  - Processed `1` guide into `81` chunks.
  - `senku_guides` total chunks after ingest: `49,732`.
