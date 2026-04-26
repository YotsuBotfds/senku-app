# Low Worker Q Dispatch: Insurance Risk Pooling Mojibake Cleanup

Date: 2026-04-25
Worker: Low Worker Q
Scope: `guides/insurance-risk-pooling-mutual-aid.md`

## Summary

Cleaned obvious mojibake punctuation and symbol artifacts in the insurance risk pooling guide. Preserved headings, frontmatter IDs, amounts, ratios, schedules, governance rules, examples, and procedural meaning.

## Replacement Classes

- Em dash artifacts restored to em dash.
- En dash range artifacts restored to en dash.
- Multiplication artifacts restored to multiplication sign.
- Division artifacts restored to division sign.
- Greater-than-or-equal artifacts restored to greater-than-or-equal sign.

## Validation

- Before: `scan_mojibake.py --paths guides\insurance-risk-pooling-mutual-aid.md --report-only` reported 46 findings.
- After: `scan_mojibake.py --paths guides\insurance-risk-pooling-mutual-aid.md --report-only` reported 0 findings.
- `git diff --check` exited successfully; Git emitted line-ending warnings only.

Main ran serialized incremental ingest after review.
- Main ingest: `.\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\insurance-risk-pooling-mutual-aid.md guides\tool-sharpening-maintenance.md`
  - Batch processed `2` guides into `78` chunks.
  - `senku_guides` total chunks after ingest: `49,729`.
