# RAG-EVAL1 Corpus Marker Diagnostic Overlay

## Slice

Let RAG diagnostics consume the corpus marker scan so retrieval panels can show
when a top-ranked guide is bridge-like or partial-bearing.

## Outcome

`scripts/analyze_rag_bench_failures.py` now accepts:

```powershell
--corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1544.json
```

When supplied, diagnostics add per-row columns:

- `top1_marker_risk`
- `top1_is_bridge`
- `top1_has_unresolved_partial`
- `top1_marker_types`

The Markdown/JSON summary also reports top-1 marker risk counts, bridge rows,
and unresolved-partial rows.

## Proof

Fresh FA/FB/FD/FE runtime panel with marker overlay:
`artifacts/bench/rag_diagnostics_20260425_1546_runtime_plus_marker_overlay/report.md`.

Result:

- quality score remained `9.56/10`
- generated rows remained `0/24`
- bad buckets remained `0`
- top-1 marker risk was `info: 11`, `none: 13`
- top-1 bridge rows: `0`
- top-1 unresolved-partial rows: `0`

So unresolved partials are a real corpus-packaging issue, but they are not
currently explaining the accepted FA/FB/FD/FE panel.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\analyze_rag_bench_failures.py tests\test_analyze_rag_bench_failures.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_analyze_rag_bench_failures -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\guide_wave_fa_20260425_153735.json artifacts\bench\guide_wave_fb_20260425_153814.json artifacts\bench\guide_wave_fd_20260425_153814.json artifacts\bench\guide_wave_fe_20260425_153814.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1544.json --output-dir artifacts\bench\rag_diagnostics_20260425_1546_runtime_plus_marker_overlay
```

Focused analyzer validation passed `31` tests.

## Next

Use this overlay on any future retrieval miss or ranking miss panel before
changing rerank behavior. If top-1 is bridge/thin/partial-bearing and the
expected owner is lower, that row should become a targeted prompt/packaging
investigation instead of a generic retrieval tune.
