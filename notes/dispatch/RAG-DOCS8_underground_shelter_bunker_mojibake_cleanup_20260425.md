# RAG-DOCS8 Underground Shelter Bunker Mojibake Cleanup

Worker: Medium Worker J
Date: 2026-04-25

## Scope

- Updated guide content only in `guides/underground-shelter-bunker.md`; added this dispatch note.
- Cleaned obvious mojibake/text-quality corruption in punctuation and unit symbols.
- Main ran serialized incremental ingest after integration review.
- Did not edit tests, scripts, vector DB files, or other guides.

## Replacement Classes

- Restored mojibake en dash ranges, e.g. `10-15`, `150-300`, `2-4`, to proper en dash ranges.
- Restored mojibake em dash separators in prose, affiliate text, and See Also links.
- Restored mojibake multiplication signs in dimensions and load formulas.
- Restored mojibake division sign in the distributed-load formula.
- Restored mojibake square/cubic meter superscripts.

## Contexts Checked

- Site assessment measurements: soil boring depth, bearing strengths, floor area, shelter depth, ceiling height.
- Excavation and shoring measurements: cut-and-cover depth, post spacing, timber dimensions, lagging thickness, lateral pressure, steel sheet piling, excavation rates.
- Structural load formulas: roof load and distributed-load examples, concrete slab area/volume, timber beam dimensions, soil bearing capacities.
- Ventilation and air-quality safety: duct diameters, air-change rates, blower capacity, filter lifetime, oxygen safe range, CO2 accumulation/scrubbing durations.
- Waterproofing/drainage: drain tile, sump size, pump wattage, inflow capacity, clay liner, membrane thickness, French drain dimensions and slope.
- Egress/habitability/sanitation/fallout: door thickness, airlock and exit dimensions, room sizes, bunk dimensions, water storage volume, toilet capacity, drainage field, greywater interval, PF range.

## Validation

- Before: `scripts\scan_mojibake.py --paths guides\underground-shelter-bunker.md --report-only`
  - Findings: 94
  - Gate findings: 94
- After: `scripts\scan_mojibake.py --paths guides\underground-shelter-bunker.md --report-only`
  - Findings: 0
  - Gate findings: 0

## Notes

- No ambiguous replacements were made or left pending; all repaired strings were direct mojibake decodes in local measurement/safety context.
- Main ingest: `.\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\semaphore-flag-signaling.md guides\underground-shelter-bunker.md`
  - Batch processed `2` guides into `100` chunks.
  - `senku_guides` total chunks after ingest: `49,732`.
