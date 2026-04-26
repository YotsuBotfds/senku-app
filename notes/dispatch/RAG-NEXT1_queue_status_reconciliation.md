# RAG-NEXT1 Queue Status Reconciliation

Worker: Dijkstra
Date: 2026-04-25

## Status

Queue/status reconciliation is docs-only and landed in:

- `notes/CP9_ACTIVE_QUEUE.md`
- `notes/dispatch/README.md`

## Current Baselines

- FA/FB/FD/FE runtime-card panel:
  `artifacts/bench/rag_diagnostics_20260425_1708_runtime_card_id_filter/`
  with `0` retrieval/ranking/generation/safety misses and
  `strong_supported:23|uncertain_fit_accepted:1`.
- Partial/router held-out pack:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd397_expectation_cleanup_diag/report.md`
  with `19` expected-supported rows, `2` accepted `uncertain_fit` rows, and
  `0` retrieval/ranking/generation/artifact misses.

## Reconciliation Notes

- `RAG-S1` through `RAG-S22`, `RAG-T12`, the recent RAG-EVAL wave, and the
  RAG-TOOL utilities are landed history unless a fresh regression artifact
  reopens them.
- `gd397_expectation_cleanup` is the current partial/router baseline.
  Earlier `post_fixes`, `metadata_visible`, and `contextual_index` artifacts
  are historical waypoints.
- `RAG-EVAL3`, `RAG-EVAL5`, source packaging for GD-024/GD-029/GD-035/GD-052/
  GD-397/GD-646/GD-648/GD-649, bridge metadata routing for GD-634/GD-635/
  GD-636, and `RAG-TOOL6` incremental ingest planner should not be repeated
  without new evidence.
- Android remains a separate lane. This note does not start Android
  implementation.

## Validation

- `git diff --check`
