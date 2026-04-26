# RAG-DOCS14 Elder Care Mojibake Cleanup

Worker: Medium Worker T
Date: 2026-04-25
Owned file: guides/elder-care.md

## Summary

Reviewed and cleaned obvious mojibake punctuation/symbols in the elder care guide. No clinical/care thresholds, medication-like statements, schedules, safety cautions, headings, frontmatter IDs, or procedural meanings were intentionally changed.

## Replacement Classes

- Mojibake right-arrow sequences in quick routing restored to ASCII `->`.
- Mojibake em-dash separator sequences restored to ASCII ` - ` separators.
- Mojibake degree-F temperature unit restored in the sepsis red-flag threshold.

## Safety And Care Contexts Checked

- Quick-routing lines for forgetting, wandering, falls, poor intake, sudden confusion, and caregiving overview; preserved guide targets and urgency wording.
- Arthritis and pain-support text; preserved willow bark/turmeric context, 5-10 minute simmering, 1-3 times daily use, and the consistency-over-dosage caution.
- Diabetes and sensory-loss text; preserved low-glycemic advice, cinnamon/fenugreek wording, foot inspection, and wound-risk warnings.
- Crisis table severe infection/sepsis row; preserved the `103°F+` high-fever red flag and follow-up monitoring/antimicrobial duration.
- Mobility, fall-prevention, and assistive-device sections; preserved cane height, monthly inspection, footwear/balance exercises, and handmade-equipment safety warning.
- Nutrition, hydration, wound care, end-of-life, caregiver burnout, seasonal risk, and common-mistake sections; preserved 1.2-1.5 g/kg protein, 6-8 cups daily fluids, every-2-hours hydration schedule, 2-day/4-day caregiver limits, and aspiration/dehydration/fall cautions.

## Validation

- Before: `.\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\elder-care.md --report-only --markdown-limit 200` reported 43 findings / 43 gate findings.
- After: `.\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\elder-care.md --report-only --markdown-limit 200` reported 0 findings / 0 gate findings.
- Main re-ingest: `ingest.py --files guides\education-system-design.md guides\elder-care.md --force-files` processed 2 files / 188 chunks; collection total 49,728.
