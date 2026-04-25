# RAG-TOOL1 Token-saving Context Indexes

## Slice

Promote token-saving repo tooling to the top of the active queue so future
agents can inspect compact generated indexes instead of repeatedly opening
large artifacts, long dispatch notes, and verbose diagnostics.

## Priority

1. `scripts/index_bench_artifacts.py` - landed.
   Generate compact artifact manifests for `artifacts/bench`.
2. `scripts/summarize_run_manifest.py` - landed.
   Read `artifacts/runs/run_manifest.jsonl` into a short recent-run summary.
3. `scripts/index_dispatch_notes.py` - landed.
   Generate a parsed dispatch index from active/completed dispatch notes.
4. `scripts/agent_context_snapshot.py` - landed.
   Combine git status, latest commits, dispatch index, run manifest, and latest
   artifact pointers into one capped startup context page.
5. `scripts/query_rag_diagnostics.py` and `scripts/compact_rag_context.py` -
   landed.
   Support targeted row lookup and compact RAG triage handoffs.
6. `scripts/guide_edit_impact.py`, `scripts/index_prompt_packs.py`, and
   `scripts/summarize_worktree_delta.py` - landed.
   Support validation planning, prompt-pack inventory, and quick dirty-tree
   grouping.

## Landed proof

Commits:

- `b23831b` add run manifest summarizer
- `3b70e09` add bench artifact indexer
- `e6f0dca` add dispatch note indexer
- `11dbc78` add rag diagnostics query cli
- `125399d` add agent context snapshot tool
- `6ee71dc` add compact rag context exporter
- `816c180` add worktree delta summarizer
- `5e3aaa7` add guide edit impact planner
- `3139920` add prompt pack indexer

Representative smoke commands:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\agent_context_snapshot.py --max-lines 80
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\query_rag_diagnostics.py artifacts\bench\rag_eval_partial_router_holdouts_20260425_post_fixes_diag --bucket retrieval_miss --guide-id GD-634
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_worktree_delta.py
```

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
