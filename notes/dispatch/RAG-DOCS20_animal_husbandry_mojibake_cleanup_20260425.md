# RAG-DOCS20 Animal Husbandry Mojibake Cleanup

Date: 2026-04-25

## Scope

- Owned files only:
  - `guides/animal-husbandry.md`
  - `notes/dispatch/RAG-DOCS20_animal_husbandry_mojibake_cleanup_20260425.md`
- Cleaned only obvious mojibake punctuation, symbol, temperature, and accent artifacts in `guides/animal-husbandry.md`.
- Preserved livestock birth, disease, slaughter, food-safety, dosing/schedule-like text, temperatures, measurements, headings, frontmatter, links, and procedural meaning.

## Replacement Classes

- Restored corrupted em dash and en dash punctuation.
- Restored corrupted right-arrow symbols in lifecycle text.
- Restored corrupted degree symbols in Fahrenheit temperatures.
- Restored corrupted multiplication symbols in crossbreeding and feed-calculation text.
- Restored corrupted accented food and breed-name characters.

## Safety and Procedure Contexts Checked

- Egg storage and incubation temperatures, poultry scalding, brooding temperatures, and poultry processing text.
- Dairy cooling temperatures, goat cheese heading and step text, calving and kidding support text, and colostrum/feeding schedules.
- Piglet warmth, pig processing temperatures, swine shade/heat-stress warnings, farrowing setup, and pork cut descriptions.
- Cattle calving timing, bloat emergency treatment warning, crossbreeding descriptions, and vulnerable-weaned-calf shelter/water line.
- Hay/feed calculation, winter-feed requirements, passive housing temperature, meat aging/rendering temperatures, and food-safety internal temperatures.
- Rabbit heat-stress temperatures, dog wound-care and herding-control warnings, bee lifecycle arrows, affiliate/link punctuation.

## Validation

- Before scan:
  - Command: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\animal-husbandry.md --report-only --markdown-limit 200`
  - Findings: `83`
  - Gate findings: `83`
  - Output: `artifacts\text_quality\mojibake_scan_20260426_022335.md`
- After scan:
  - Command: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\animal-husbandry.md --report-only --markdown-limit 200`
  - Findings: `0`
  - Gate findings: `0`
  - Output: `artifacts\text_quality\mojibake_scan_20260426_022654.md`
- Diff check:
  - Command: `git diff --check -- guides\animal-husbandry.md notes\dispatch\RAG-DOCS20_animal_husbandry_mojibake_cleanup_20260425.md`
  - Result: passed; Git emitted only the line-ending notice for `guides/animal-husbandry.md`.

## Ingest

- Main re-ingest: `ingest.py --files guides\animal-husbandry.md --force-files` processed 1 file / 234 chunks; collection total 49,725.
