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

## 2026-04-25 Scout Triage Queue

Three read-only scouts reviewed the current RAG/tooling lane with web search
encouraged and converged on the same operating shape: retrieval is now mostly
healthy for the partial/router pack, while agents are still spending too much
context stitching together related diagnostic runs and separating citation
owner misses from retrieval misses.

Actionable order:

1. **Close the current eval-pack owner-citation misses first.**
   Continue source-local packaging for rank-1 retrieved owners that are still
   not cited. After the `GD-648` minimum-operations slice, the remaining
   generation/citation misses are `GD-024`, `GD-052`, and `GD-646`. Gate each
   guide change with forced incremental ingest and a held-out pack rerun.

2. **Add RAG experiment lineage reporting.**
   Build `scripts/rag_experiment_lineage.py` to auto-discover related
   `*_diag` directories by stem, sort runs, join each diagnostic directory to
   its bench JSON/config, and emit run-level deltas plus per-prompt bucket and
   citation-owner transitions. This is the highest-leverage token saver after
   the active eval-pack closure because it replaces manual comparison across
   many variant artifacts.

3. **Add a retrieval-only evaluator.**
   Build a generation-free prompt-pack evaluator for hit@1, hit@3, hit@k,
   MRR/NDCG-style owner rank, owner share, top distractors, and marker overlay.
   Use it for rerank/index experiments before spending generation cycles.

4. **Add a regression gate around RAG trends.**
   Extend `rag_trend.py` or add a thin `rag_gate.py` so a candidate diagnostic
   run can be compared against a baseline and fail on configured regressions.
   This should catch cases like the reverted primary-source-label experiment
   before a later note treats the latest run as an improvement.

5. **Move hardcoded owner-hint rerank rules toward data.**
   Replace or supplement narrow `query.py` owner hints with guide frontmatter
   or a small manifest once the current held-out pack is stable. Keep gates
   strict so data-driven weights do not become broad catchalls.

6. **Make touched-file quality gates delta-based.**
   Extend marker/mojibake tooling so it can fail only on new or touched-file
   damage. The current corpus baselines are intentionally noisy, so broad
   fail-the-world gates would slow every guide patch.

7. **Add report-only artifact retention/storage tooling.**
   Add a dry-run storage reporter/retention planner that summarizes large
   artifact families, duplicate basename families, and generated-directory
   candidates while preserving files referenced by dispatch notes and run
   manifests.

8. **Enrich run manifests automatically.**
   Auto-derive changed files from git status/diff stat when
   `write_run_manifest.py` callers omit explicit changed-file arguments. This
   makes startup snapshots and future lineage reports more trustworthy.

Non-goal for this queue: do not reopen high-liability card families that now
show `0/0` frontmatter/card gaps unless a fresh behavior panel exposes a real
miss.

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
