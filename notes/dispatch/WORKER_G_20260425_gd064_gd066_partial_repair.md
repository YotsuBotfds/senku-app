# Worker G 2026-04-25 GD-064/GD-066 Partial Repair

Scope: replace top-1 unresolved partial markers from RAG-EVAL2 for `GD-064` and `GD-066` with concise source-local troubleshooting content.

Changed:

- `guides/agriculture.md`: replaced `{{> agriculture-troubleshooter }}` with an agriculture symptom triage table covering water stress, nutrient symptoms, pests, fungal pressure, pollination failure, damping-off, and crop-family escalation.
- `guides/fermentation-baking.md`: replaced `{{> fermentation-baking-troubleshooter }}` with fermentation and baking triage covering starter, dough, oven, vegetable ferment, dairy culture, vinegar, alcohol fermentation, and CO2 safety issues.

Validation:

- Marker scan before: `GD-064 unresolved_partial=1`; `GD-066 unresolved_partial=1`.
- Marker scan after: `GD-064 unresolved_partial=0`; `GD-066 unresolved_partial=0`; repo-wide unresolved partials `54 -> 52`.
- `python -B scripts/scan_corpus_markers.py --output-json %TEMP%/worker_g_corpus_marker_scan.json --output-md %TEMP%/worker_g_corpus_marker_scan.md` completed: scanned 754 guides, 52 unresolved partial markers remain outside this scope.
- `python -B -m unittest tests.test_scan_corpus_markers -v` passed.
