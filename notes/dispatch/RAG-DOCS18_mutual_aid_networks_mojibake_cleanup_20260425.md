# RAG-DOCS18 Mutual Aid Networks Mojibake Cleanup

Date: 2026-04-25
Worker: Low Worker X
Scope: `guides/mutual-aid-networks.md`

## Summary

Cleaned obvious mojibake punctuation and symbol artifacts in the mutual aid networks guide only.

Replacement classes:
- Range dash artifacts in quantities, schedules, dates, and network-size ranges.
- Arrow artifacts in ledger examples.
- Plus/minus symbol artifact in the contribution-balance target.
- Em dash separator artifacts in prose and affiliate descriptions.

Preserved headings, frontmatter, links, affiliate URLs, governance/community logistics meaning, quantities, and schedules.

## Validation

- Before cleanup: `scripts\scan_mojibake.py --paths guides\mutual-aid-networks.md --report-only --markdown-limit 200` reported 90 findings.
- After cleanup: `scripts\scan_mojibake.py --paths guides\mutual-aid-networks.md --report-only --markdown-limit 200` reported 0 findings.
- Main re-ingest: `ingest.py --files guides\mutual-aid-networks.md --force-files` processed 1 file / 55 chunks; collection total 49,725.
