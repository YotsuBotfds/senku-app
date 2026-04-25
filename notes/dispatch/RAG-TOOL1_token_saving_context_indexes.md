# RAG-TOOL1 Token-saving Context Indexes

## Slice

Promote token-saving repo tooling to the top of the active queue so future
agents can inspect compact generated indexes instead of repeatedly opening
large artifacts, long dispatch notes, and verbose diagnostics.

## Priority

1. `scripts/index_bench_artifacts.py`
   Generate compact artifact manifests for `artifacts/bench`.
2. `scripts/summarize_run_manifest.py`
   Read `artifacts/runs/run_manifest.jsonl` into a short recent-run summary.
3. `scripts/index_dispatch_notes.py`
   Generate a parsed dispatch index from active/completed dispatch notes.
4. `scripts/agent_context_snapshot.py`
   Combine git status, latest commits, dispatch index, run manifest, and latest
   artifact pointers into one capped startup context page.
5. `scripts/query_rag_diagnostics.py` and `scripts/compact_rag_context.py`
   Support targeted row lookup and compact RAG triage handoffs.

## Why now

The current backlog work repeatedly touches:

- generated bench artifacts and diagnostics,
- prompt packs,
- metadata audits and marker scans,
- dispatch notes and queue notes,
- run manifests and handoffs.

Small generated indexes should reduce context load, repeated shell exploration,
and accidental over-reading of large ignored artifact trees.

## Guardrails

- Prefer read-only indexers and compact generated Markdown/JSONL outputs.
- Do not make long living prose notes the primary interface.
- Skip binary/large artifact bodies; record paths, sizes, mtimes, and safe
  summary fields instead.
- Generated indexes should be reproducible and safe to regenerate.

## Validation

Each tool should include fixture-based unit tests plus one real smoke run on the
current repo when inexpensive.
