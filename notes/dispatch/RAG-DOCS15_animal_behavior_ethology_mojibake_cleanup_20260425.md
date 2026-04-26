# RAG-DOCS15 Animal Behavior Ethology Mojibake Cleanup

## Scope

- Cleaned obvious mojibake punctuation/symbol artifacts only in `guides/animal-behavior-ethology.md`.
- Preserved meaning, headings, frontmatter, links, thresholds, schedules, animal-handling cautions, and procedural wording.

## Replacement Classes

- Replaced corrupted en dash range punctuation with ASCII hyphen in numeric ranges.
- Replaced corrupted em dash separators with ASCII hyphen separators in affiliate and see-also text.

## Validation

- Before scan: `scripts\scan_mojibake.py --paths guides\animal-behavior-ethology.md --report-only --markdown-limit 200`
  - `findings_count`: 11
  - `gate_findings_count`: 11
- After scan: `scripts\scan_mojibake.py --paths guides\animal-behavior-ethology.md --report-only --markdown-limit 200`
  - `findings_count`: 0
  - `gate_findings_count`: 0
- Diff check: passed with `git diff --check -- guides\animal-behavior-ethology.md notes\dispatch\RAG-DOCS15_animal_behavior_ethology_mojibake_cleanup_20260425.md`; Git also emitted a line-ending warning for `guides/animal-behavior-ethology.md`.

## Ingest

- Main re-ingest: `ingest.py --files guides\animal-behavior-ethology.md --force-files` processed 1 file / 27 chunks; collection total 49,728.
