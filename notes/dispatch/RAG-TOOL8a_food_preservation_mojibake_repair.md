# RAG-TOOL8a Food Preservation Mojibake Repair

## Scope

- Repaired high-confidence mojibake in `guides/food-preservation.md`.
- Preserved content semantics; changes are limited to reversible encoding artifacts.
- Did not edit other guides or planner handoff notes.

## Repairs

- Restored the frontmatter icon from mojibake to the jar emoji used by the guide.
- Restored degree symbols in Fahrenheit/Celsius temperature ranges.
- Restored em dashes, en dashes, left arrows, multiplication signs, plus/minus signs, accents, emoji headings, and chemical subscripts.
- Examples include broken temperature units, altitude ranges, arrows,
  multiplication signs, plus/minus signs, accented food names, and nitrate /
  nitrite chemical subscripts.

## Validation

- Before repair:
  - `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\food-preservation.md --report-only`
  - Findings: 300
- After repair:
  - `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\food-preservation.md --report-only`
  - Findings: 0
- Focused re-ingest:
  - `.\.venvs\senku-validate\Scripts\python.exe ingest.py --files guides/food-preservation.md --force-files`
  - Processed 1 guide file into 221 chunks.
- Stats:
  - `.\.venvs\senku-validate\Scripts\python.exe ingest.py --stats`
  - Collection `senku_guides`: 49,743 total chunks; agriculture: 6,059 chunks.

## Remaining Findings

- No mojibake findings remain in `guides/food-preservation.md` from `scripts/scan_mojibake.py`.
