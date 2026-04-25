# RAG-T12 Runtime Reviewed-Card ID Filter

## Slice

Fix analyzer diagnostics so actual reviewed-card runtime answers are evaluated
against the card IDs carried by the artifact, not every reviewed card in the
same expected guide family.

## Outcome

- Added a runtime-card filter in `scripts/analyze_rag_bench_failures.py`.
- For `answer_provenance=reviewed_card_runtime`, answer-card, evidence-nugget,
  and claim-support diagnostics now use only `reviewed_card_ids` from the row
  when those cards are available.
- Shadow card diagnostics still evaluate the broader selected family set, so
  broader family gaps remain visible without misclassifying the actual runtime
  answer.
- Added a regression test proving that a runtime answer backed by one card does
  not fail because a sibling support-family card has extra required actions.

## Proof

Fresh post-card runtime panel:

- `artifacts/bench/rag_diagnostics_20260425_1708_runtime_card_id_filter/`

Measured result:

- bucket counts: `18` deterministic pass, `5` expected supported, `1`
  abstain/clarify
- ranking misses: `0`
- generated prompts: `0/24`
- reviewed-card runtime answers: `5`
- answer-card status: `pass:5|no_generated_answer:19`
- evidence nuggets: `19/19 (100%)`
- app acceptance: `strong_supported:23|uncertain_fit_accepted:1`

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\analyze_rag_bench_failures.py rag_bench_answer_diagnostics.py tests\test_analyze_rag_bench_failures.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_analyze_rag_bench_failures -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\guide_wave_fa_20260425_170442.json artifacts\bench\guide_wave_fb_20260425_170450.json artifacts\bench\guide_wave_fd_20260425_170455.json artifacts\bench\guide_wave_fe_20260425_170458.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-dir artifacts\bench\rag_diagnostics_20260425_1708_runtime_card_id_filter
```

Tests passed: `32`.

## Next

Proceed to the wound-family reviewed-card slice (`GD-235`, `GD-622`) if
continuing card coverage. Do not treat dangerous-activation support cards as
next by default unless a fresh behavior panel exposes a real miss.
