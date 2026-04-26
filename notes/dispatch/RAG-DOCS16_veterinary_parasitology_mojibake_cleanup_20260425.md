# RAG-DOCS16 Veterinary Parasitology Mojibake Cleanup

Date: 2026-04-25

## Scope

- Owned files only:
  - `guides/veterinary-parasitology.md`
  - `notes/dispatch/RAG-DOCS16_veterinary_parasitology_mojibake_cleanup_20260425.md`
- Cleaned only obvious mojibake punctuation and symbol artifacts in `guides/veterinary-parasitology.md`.
- Preserved species names, parasite names, headings, frontmatter, links, treatment cautions, dose-like text, schedule-like text, thresholds, and procedural meaning.

## Replacement Classes

- Restored corrupted em dash separators.
- Restored corrupted right arrows.
- Restored corrupted degree temperature symbols.
- Restored corrupted plus-minus symbols.
- Restored corrupted multiplication and division symbols in formulas.

## Safety and Procedure Contexts Checked

Checked changed-line contexts before finalizing, including:

- Temperature thresholds and compost heat requirements: `>60°C`, `15-25°C`, `59-77°F`, `5°C`, `35°C`, `<15°C`, `165°F`.
- Treatment dosing and safety cautions: ivermectin `0.2mg/kg`, levamisole `7mg/kg`, `350mg`, `3.5mL`, under-dose / over-dose warnings, pregnant-animal drug safety warning, levamisole narrow-margin caution.
- Formula/procedure lines: `weight × dose ÷ concentration = volume`, `(eggs / 0.6mL) × 50 = EPG`, McMaster procedure steps, fecal egg thresholds.
- Management intervals and schedules: `3+ weeks`, `3-4 week rotation`, `8+ weeks`, `2-3 weeks`, `4-8 weeks`, `6-8 week spell`, monthly/quarterly tropical treatment schedule.
- Warning blocks: water-source reservoir warning, drug-resistance rotation warning, under-dose critical warning.
- Affiliate/link lines: punctuation only; URLs and link text preserved.

## Validation

- Before scan:
  - Command: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\veterinary-parasitology.md --report-only --markdown-limit 200`
  - Findings: `36`
  - Gate findings: `36`
  - Output: `artifacts\text_quality\mojibake_scan_20260426_021905.md`
- After scan:
  - Command: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\veterinary-parasitology.md --report-only --markdown-limit 200`
  - Findings: `0`
  - Gate findings: `0`
  - Output: `artifacts\text_quality\mojibake_scan_20260426_022038.md`
- Direct marker check:
  - Command: searched for common mojibake marker characters in `guides\veterinary-parasitology.md`.
  - Result: no matches.
- Diff check:
  - Command: `git diff --check -- guides\veterinary-parasitology.md notes\dispatch\RAG-DOCS16_veterinary_parasitology_mojibake_cleanup_20260425.md`
  - Result: passed; Git emitted only the existing line-ending notice for `guides/veterinary-parasitology.md`.

## Ingest

- Main re-ingest: `ingest.py --files guides\veterinary-parasitology.md guides\trapping-snares.md --force-files` processed 2 files / 169 chunks; collection total 49,726.
