# RAG-DOCS32 Toxidromes Field Poisoning Mojibake Cleanup

Worker: High Worker AL
Date: 2026-04-25

## Scope

- `guides/toxidromes-field-poisoning.md`

## Changes

- Replaced obvious mojibake punctuation and symbol artifacts only.
- Replacement classes: range dashes, explanatory dashes, consequence arrows, and multiplication-style vial notation.
- Preserved frontmatter, headings, links, toxidrome recognition patterns, vital-sign patterns, antidote and medication wording, dose-like values, exposure and timing guidance, temperatures, thresholds, red flags, and procedural meaning.

## Safety Review

- Reviewed changed hunks around anticholinergic, cholinergic, opioid, sympathomimetic, mushroom/plant poisoning, decontamination, antidote summary, and kit sections.
- Confirmed edits were limited to corrupted punctuation/symbol rendering; numeric values, routes, intervals, emergency escalation language, and medication names were unchanged.

## Validation

- Before: `.\.venvs\senku-validate\Scripts\python.exe .\scripts\scan_mojibake.py --paths guides\toxidromes-field-poisoning.md --report-only --markdown-limit 200`
  - `findings_count`: 167
  - `gate_findings_count`: 167
- After: `.\.venvs\senku-validate\Scripts\python.exe .\scripts\scan_mojibake.py --paths guides\toxidromes-field-poisoning.md --report-only --markdown-limit 200`
  - `findings_count`: 0
  - `gate_findings_count`: 0
- `git diff --check -- guides\toxidromes-field-poisoning.md notes\dispatch\RAG-DOCS32_toxidromes_field_poisoning_mojibake_cleanup_20260425.md`
  - Exit 0; emitted a CRLF normalization warning for the guide only.

## Notes

- Main re-ingest: `ingest.py --files guides\toxidromes-field-poisoning.md --force-files` processed 1 file / 54 chunks; collection total 49,709.
