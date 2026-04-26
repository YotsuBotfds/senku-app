# RAG-DOCS24 Animal Salvage Butchery Mojibake Cleanup

Date: 2026-04-25

## Scope

- Owned files only:
  - `guides/animal-salvage-butchery.md`
  - `notes/dispatch/RAG-DOCS24_animal_salvage_butchery_mojibake_cleanup_20260425.md`
- Cleaned only obvious mojibake punctuation and symbol artifacts in `guides/animal-salvage-butchery.md`.
- Preserved slaughter and carcass handling steps, spoilage/pathogen warnings, temperature values, timing, measurements, headings, frontmatter, links, and procedural meaning.

## Replacement Classes

- Restored corrupted em dash punctuation in explanatory asides and affiliate descriptions.
- Restored corrupted en dash punctuation in numeric ranges and duration ranges.
- Restored corrupted right-arrow symbols in yield examples.
- Restored corrupted degree symbols in Fahrenheit temperatures.
- Restored corrupted multiplication symbols in yield and feed calculations.

## Safety and Procedure Contexts Checked

- Disaster salvage overview and water/electrolyte handling line.
- Wild/feral animal cooking temperature warning and animal-contact water warning.
- Emergency slaughter timing, unconsciousness timing, bleed-out timing, and post-slaughter handling steps.
- Carcass yield, limb-separation yield, butchery timeline, and spoilage minimization line.
- Drying, salting, smoking, rendering, and cold-chain improvisation durations and temperature thresholds.
- Disease inspection checklist context, cooking safety temperatures, boiling temperature and duration, and fever threshold in the cattle-flood example.
- Slaughter-team size, bleed timing, cattle yield examples, jerky yield calculation, feed calculation, affiliate link punctuation, and URLs.

## Validation

- Before scan:
  - Command: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\animal-salvage-butchery.md --report-only --markdown-limit 200`
  - Findings: `72`
  - Gate findings: `72`
  - Output: `artifacts\text_quality\mojibake_scan_20260426_022925.md`
- After scan:
  - Command: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\animal-salvage-butchery.md --report-only --markdown-limit 200`
  - Findings: `0`
  - Gate findings: `0`
  - Output: `artifacts\text_quality\mojibake_scan_20260426_023316.md`
- Diff check:
  - Command: `git diff --check -- guides\animal-salvage-butchery.md notes\dispatch\RAG-DOCS24_animal_salvage_butchery_mojibake_cleanup_20260425.md`
  - Result: passed; Git emitted only the line-ending notice for `guides/animal-salvage-butchery.md`.

## Ingest

- Main re-ingest: `ingest.py --files guides\animal-salvage-butchery.md --force-files` processed 1 file / 43 chunks; collection total 49,725.
