# RAG-DOCS12 Tool Sharpening Maintenance Mojibake Cleanup

Worker: Medium Worker R
Date: 2026-04-25
Owned file: guides/tool-sharpening-maintenance.md

## Summary

Reviewed and cleaned obvious mojibake punctuation/symbols in the tool sharpening guide.

## Replacement Classes

- Mojibake em dash sequences restored to em dashes.
- Mojibake en dash/range sequences restored to en dashes.
- Mojibake arrow sequence restored to right arrow in the grit progression.
- Mojibake degree symbols restored in angle references.

## Measurement And Safety Contexts Checked

- Preserved grit ranges: 800-2000, 4000-8000, 4000-8000 in progression.
- Preserved sharpening angles: 10-15, 15-20, 20-25, 25-35, 20-30, 35-40, 20, 45, and 90 degrees.
- Preserved stroke/count ranges: 10-20, 5-10, 3-5, 20-50, 2-3, and saw timing 20-30 minutes.
- Preserved measurement context for 2-3 mm leather and 50 cm handsaw.
- Checked warning/tip safety language around grinding-wheel hazards, metal dust, edge testing, tool control, and field gouging risk; wording meaning unchanged.

## Validation

- Before: `scan_mojibake.py --paths guides\tool-sharpening-maintenance.md --report-only` reported 68 findings.
- After: `scan_mojibake.py --paths guides\tool-sharpening-maintenance.md --report-only` reported 0 findings.
- `git diff --check` passed.

Main ran serialized incremental ingest after review.
- Main ingest: `.\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\insurance-risk-pooling-mutual-aid.md guides\tool-sharpening-maintenance.md`
  - Batch processed `2` guides into `78` chunks.
  - `senku_guides` total chunks after ingest: `49,729`.
