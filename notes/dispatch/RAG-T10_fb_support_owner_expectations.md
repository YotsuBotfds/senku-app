# RAG-T10 FB Support Owner Expectations

## Slice

Clean up the infected-wound family assignment after `RAG-DOCS2` and `RAG-T9`.
The deterministic infected-wound rule was correctly citing `GD-235` and
`GD-622` as support guides, but the FB expectation manifest only listed
`GD-585`, `GD-589`, and `GD-232`.

## Outcome

Updated `notes/specs/rag_prompt_expectations_seed_20260424.yaml`:

- added expectation notes for FB
- kept `GD-585`, `GD-589`, and `GD-232` as primary expected guides
- added support expected guides `GD-235` infection control and `GD-622` animal
  bite / puncture / hand-function context

Also refined the family prioritizer so non-expected citations from
`uncertain_fit_accepted` rows are tracked separately and do not force
`inspect_retrieval_ranking` as the family action.

## Proof

Fresh diagnostic panel:

- `artifacts/bench/rag_diagnostics_20260425_1650_fb_support_owner_expectations/`

Fresh family-priority panel:

- JSON:
  `artifacts/bench/high_liability_family_priorities_20260425_1658_support_uncertainfit_semantics.json`
- Markdown:
  `artifacts/bench/high_liability_family_priorities_20260425_1658_support_uncertainfit_semantics.md`

Trend comparison against the previous panel:

- expected-owner top-k improved from `23/24` to `24/24`
- expected-owner top-3 share improved from `0.4028` to `0.5139`
- expected-owner top-k share improved from `0.5263` to `0.6842`
- wound non-uncertain non-expected citations dropped to `0`
- wound candidate action changed to `add_targeted_metadata`

The family table now ranks:

1. `dangerous_activation_mania_crisis`: add targeted metadata
2. `infected_wound_spreading_redness`: add targeted metadata
3. `unknown_child_ingestion_poisoning`: add targeted metadata
4. `stroke_tia_cardiac_overlap`: add targeted metadata

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\analyze_rag_bench_failures.py scripts\prioritize_high_liability_families.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_analyze_rag_bench_failures tests.test_compare_contextual_shadow_retrieval -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_prioritize_high_liability_families -v
```

Analyzer/contextual tests passed: `38`. Family prioritizer test passed: `1`.

## Next

The next family-level slice should be targeted high-liability metadata, not
new answer-card expansion, unless a fresh prompt panel shows an actual
non-deterministic missing-card row.
