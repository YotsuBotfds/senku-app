# RAG-TOOL8c Food Safety Mojibake Repair

## Scope

- Repaired high-confidence reversible mojibake only in `guides/food-safety-contamination-prevention.md`.
- Preserved guide semantics; changes are punctuation/symbol restoration such as
  em dashes, en dashes, degree symbols, comparison symbols, multiplication
  signs, and accented characters.
- Did not edit other guides or protected planner handoff files.

## Scan Results

- Before: `scripts/scan_mojibake.py --paths guides\food-safety-contamination-prevention.md`
  - Files scanned: `1`
  - Findings: `146`
  - Gate findings: `146`
  - Counts: `stray_latin1_spacing=34`, `utf8_as_cp1252_accent_prefix=37`, `utf8_as_cp1252_punctuation=75`
- After: same target file scan
  - Files scanned: `1`
  - Findings: `0`
  - Gate findings: `0`

## Ingest And Validation

- Re-ingest command: `.\.venvs\senku-validate\Scripts\python.exe -B ingest.py --files guides\food-safety-contamination-prevention.md --force-files`
  - Parsed `1` file into `78` chunks.
  - Embedded `78/78` chunks.
  - Collection total after ingest: `49737` chunks.
- Stats command: `.\.venvs\senku-validate\Scripts\python.exe -B ingest.py --stats`
  - Collection: `senku_guides`
  - Total chunks: `49737`
  - Agriculture chunks: `6059`
- `git diff --check`: passed with no whitespace errors.
  - Git printed line-ending warnings for `guides/food-safety-contamination-prevention.md` and the pre-existing modified `notes/dispatch/RAG-TOOL8_mojibake_cleanup_queue.md`.

## Remaining Findings

- Target guide: no remaining mojibake findings from `scan_mojibake.py`.
- No additional guide files were scanned or modified for this scoped repair.
