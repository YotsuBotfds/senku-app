# RAG-DOCS4 Home Management Mojibake Cleanup

Low Worker A narrow tranche for `guides/home-management.md`.

## Scope

- Repaired only obvious mojibake punctuation/symbol corruption in `guides/home-management.md`.
- Did not change headings, frontmatter IDs, thresholds, or procedural meaning.
- Did not run ingest.

## Replacement Classes

- Double-encoded em dash mojibake -> em dash.
- Double-encoded en dash mojibake -> en dash.
- Double-encoded Fahrenheit degree marker -> degree sign plus `F`.

## Validation

- `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\home-management.md --report-only`
  - Before: `findings_count: 137`, `gate_findings_count: 137`.
  - After: `findings_count: 0`, `gate_findings_count: 0`.
- `.\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\home-management.md`
  - Processed `1` guide into `97` chunks.
  - `senku_guides` total chunks after ingest: `49,734`.
