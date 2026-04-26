# RAG-DOCS23 Water Distribution Systems Mojibake Cleanup

Worker: Medium Worker AC
Date: 2026-04-25

## Scope

- Owned files only:
  - `guides/water-distribution-systems.md`
  - `notes/dispatch/RAG-DOCS23_water_distribution_systems_mojibake_cleanup_20260425.md`
- Cleaned only obvious mojibake punctuation and symbol artifacts in `guides/water-distribution-systems.md`.
- Preserved headings, frontmatter, links, URLs, pressures, flows, measurements, formulas, public-health cautions, and procedural meaning.

## Replacement Classes

- Restored corrupted dash separators as plain hyphen separators.
- Restored corrupted multiplication symbols in formulas and dimensions as `x`.
- Restored corrupted approximate symbols as the word `approximately`.
- Restored corrupted density variable symbols as `rho`.
- Restored corrupted superscript unit exponents as caret notation.

## Safety and Procedure Contexts Checked

Checked changed-line contexts before finalizing, including:

- Pressure and head formulas: `P = rho*g*h`, `h = P / (rho*g)`, 20-60 PSI, 1.4-4.1 bar, 30/20/40 PSI tower-height examples, and meter/foot conversion rules.
- Flow and pressure-loss formulas: Darcy-Weisbach expression, `v^2/2g`, `9.81 m/s^2`, 2-inch pipe, 50mm pipe, and 2-3 meter head-loss rule.
- Storage and sizing formulas: daily requirement multiplication, 500-person worked example, 37,500 liters, 37.5 cubic meters, 50-60 cubic meter tank, and 13,000-16,000 gallon range.
- Structural and material quantity contexts: 50 cubic meter tank load, 550 metric tons, 1-2 meter foundation depth, 30cm concrete pad, material lifespans, bamboo 3-5 year selection, 2-3cm cuts, and 4-6 week seasoning.
- Water-point and hygiene warnings: 1.2-1.5 meter tap height, 1.5m x 1.5m tap-house footprint, basin outlet submergence warning, greywater non-potable cautions, cross-connection warning, 24-hour greywater storage limit, and `>1m^2` constructed-wetland sizing.
- Repair and affiliate/link lines: quarter-turn tightening warning, soldered/welded-joint repair note, URLs, link text, PSI/GPM ranges, and affiliate note punctuation.

## Validation

- Before scan:
  - Command: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\water-distribution-systems.md --report-only --markdown-limit 200`
  - Findings: `27`
  - Gate findings: `27`
  - Output: `artifacts\text_quality\mojibake_scan_20260426_022927.md`
- After scan:
  - Command: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths guides\water-distribution-systems.md --report-only --markdown-limit 200`
  - Findings: `0`
  - Gate findings: `0`
  - Output: `artifacts\text_quality\mojibake_scan_20260426_023157.md`
- Diff check:
  - Command: `git diff --check -- guides\water-distribution-systems.md notes\dispatch\RAG-DOCS23_water_distribution_systems_mojibake_cleanup_20260425.md`
  - Result: passed; Git emitted only the line-ending normalization warning for `guides/water-distribution-systems.md`.

## Ingest

- Main re-ingest: `ingest.py --files guides\accessible-shelter-design.md guides\water-distribution-systems.md --force-files` processed 2 files / 132 chunks; collection total 49,725.
