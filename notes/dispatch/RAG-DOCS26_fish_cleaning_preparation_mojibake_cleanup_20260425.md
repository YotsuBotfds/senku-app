# RAG-DOCS26 Fish Cleaning Preparation Mojibake Cleanup

Date: 2026-04-25
Worker: Medium Worker AF

## Scope

- Cleaned only obvious mojibake punctuation artifacts in `guides/fish-cleaning-preparation.md`.
- Replaced corrupted dash punctuation in quick-triage text with ASCII hyphens.
- Replaced corrupted right-arrow punctuation before section links with the existing ASCII arrow style used in the same block.

## Safety/Procedure Review

- Preserved fish-cleaning sequence, spoilage and odor warnings, food-safety defaults, headings, anchors, links, URLs, frontmatter, and procedural meaning.
- Checked changed-line context for odor/spoilage discard guidance, safe-parts guidance, and filleting steps; no safety or procedure meaning was changed.

## Validation

- Before scan: `scripts\scan_mojibake.py --paths guides\fish-cleaning-preparation.md --report-only --markdown-limit 200` reported 8 findings.
- After scan: `scripts\scan_mojibake.py --paths guides\fish-cleaning-preparation.md --report-only --markdown-limit 200` reported 0 findings and 0 gate findings.
- Diff check: `git diff --check -- guides\fish-cleaning-preparation.md notes\dispatch\RAG-DOCS26_fish_cleaning_preparation_mojibake_cleanup_20260425.md` passed.
- Main re-ingest: `ingest.py --files guides\electricity.md guides\fish-cleaning-preparation.md --force-files` processed 2 files / 129 chunks; collection total 49,725.
