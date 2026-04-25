# RAG-T6 RAG Trend Marker Overlay Columns

## Slice

Surface corpus-marker overlay fields in `scripts/rag_trend.py` so session-start
trend tables can show whether top-1 bridge or unresolved-partial guides are in
play.

## Outcome

`rag_trend.py` now includes:

- `top1_marker_risk`
- `top1_bridge`
- `top1_unresolved_partial`

The trend table prefers analyzer summary fields and falls back to row data when
needed.

## Proof

Comparison between the fresh runtime panel and the same panel with marker
overlay:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\rag_trend.py artifacts\bench\rag_diagnostics_20260425_1538_runtime_hardened_all_fresh artifacts\bench\rag_diagnostics_20260425_1546_runtime_plus_marker_overlay --label runtime-no-overlay --label runtime-marker-overlay
```

The overlay row reports `info:11|none:13`, `0/24` top-1 bridge rows, and `0/24`
top-1 unresolved-partial rows.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\rag_trend.py tests\test_rag_trend.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_rag_trend -v
```

Focused validation passed `10` tests.
