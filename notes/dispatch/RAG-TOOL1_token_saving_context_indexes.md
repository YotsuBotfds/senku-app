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

## 2026-04-25 RAG Lineage Reporter Proof

Implemented `scripts/rag_experiment_lineage.py`, a stdlib reporter that
discovers related `*_diag` directories by stem, filters focused partial probes
out of default discovered lineages, summarizes run-level diagnostic counts, and
prints prompt-level bucket/citation transitions between full-pack runs.

Validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_rag_experiment_lineage -v
```

Smoke:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\rag_experiment_lineage.py --bench-dir artifacts\bench --stem rag_eval_partial_router_holdouts_20260425 --limit 8 --output-json artifacts\bench\rag_eval_partial_router_holdouts_20260425_lineage.json --output-md artifacts\bench\rag_eval_partial_router_holdouts_20260425_lineage.md
```

Current output shows the full-pack progression from `primary_source_labels`
through `gd035_gd649_packaging`: expected-supported rows moved from `11` to
`17`, ranking misses moved from `2` to `0`, and expected-owner cited moved from
`12/21` to `18/21`. The reverted `primary_source_labels` dip remains visible
when the lineage includes the earlier `citation_priority` baseline.

After the `GD-029` packaging pass, the same lineage command shows the current
full-pack floor at `18` expected-supported rows, `0` generation misses, `0`
ranking misses, `1` known expectation-drift retrieval miss, and expected-owner
cited `19/21`.

## 2026-04-25 Retrieval-only Evaluator Proof

Implemented `scripts/evaluate_retrieval_pack.py`, a generation-free prompt-pack
retrieval evaluator. It loads JSONL prompt packs, extracts expected owner guide
IDs, calls the existing repo retrieval wrapper, and reports hit@1, hit@3,
hit@k, best rank, simple owner share, and top distractor guide IDs.

Validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_evaluate_retrieval_pack -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\evaluate_retrieval_pack.py tests\test_evaluate_retrieval_pack.py
```

Sample:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --top-k 24 --embed-url http://127.0.0.1:1234/v1 --output-json artifacts\bench\retrieval_pack_eval.json --output-md artifacts\bench\retrieval_pack_eval.md --progress
```

## 2026-04-25 RAG Regression Gate Proof

Implemented `scripts/rag_regression_gate.py`, a stdlib diagnostics comparator
for CI/report usage. It accepts diagnostic directories or direct
`diagnostics.json` files, compares baseline/current bucket, owner-hit,
expected-citation, and top-1 marker metrics, and remains report-only unless
`--fail-on-regression` is passed.

Default checks catch expected-supported drops, retrieval/ranking/generation
miss increases, expected-owner hit/share drops, expected-cited drops, and
top-1 marker risk increases. Callers can tune behavior with `--check`,
`--ignore-metric`, `--allow-regression`, and `--no-default-checks`, and emit
Markdown, text, or JSON.

Validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_rag_regression_gate -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_rag_trend tests.test_rag_regression_gate -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\rag_regression_gate.py tests\test_rag_regression_gate.py
```

Smoke:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\rag_regression_gate.py artifacts\bench\rag_eval_partial_router_holdouts_20260425_gd029_gd035_gd649_packaging_diag artifacts\bench\rag_eval_partial_router_holdouts_20260425_gd397_expectation_cleanup_diag --format text
```

The smoke reported `PASS` with `0` regressions.

## 2026-04-25 Artifact Storage Reporter Proof

Updated `scripts/summarize_artifact_storage.py` and fixture coverage so storage
triage can use path/stat metadata only. The reporter summarizes total
bytes/files/dirs, largest files, largest dirs, suffix counts, generated-dir
candidates, and duplicate basename families.

Validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_summarize_artifact_storage -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_artifact_storage.py --root artifacts --limit 3
```

Current real smoke reported about `63.5 GiB`, `90673` files, and `69099`
directories under `artifacts`.

## 2026-04-25 Run Manifest Auto-Enrichment Proof

Updated `scripts/write_run_manifest.py` so records auto-capture capped git
state when callers omit explicit fields: branch, full/short head, capped status
summary, auto-derived changed tracked/untracked files, dirty state, diff stat,
and relevant artifact paths from supplied inputs/outputs/changed files.
Explicit `--changed-file`, `--commit`, and `--diff-stat` arguments still win.

Validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_write_run_manifest -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\write_run_manifest.py tests\test_write_run_manifest.py
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\write_run_manifest.py --task RAG-TOOL1 --lane tooling --role worker-c --label auto-enrichment-smoke --output artifacts\bench\run_manifest_auto_enrichment_smoke --manifest-path artifacts\tmp\run_manifest_auto_enrichment_smoke.jsonl --changed-file-limit 12 --status-limit 12
```

## 2026-04-25 Data-backed Owner Hint Proof

Moved the narrow partial/router owner-hint rerank rules out of inline
`query.py` branches and into `data/rag_owner_rerank_hints.json`. Runtime
behavior stays conservative: a rule applies only when the retrieved guide ID
matches and every marker group has at least one phrase present in the question.

Validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing.QueryRoutingTests.test_rag_eval_unresolved_partial_rerank_owner_hints tests.test_query_routing.QueryRoutingTests.test_rag_eval_bridge_rerank_owner_hints tests.test_query_routing.QueryRoutingTests.test_rag_eval_owner_hint_manifest_scope_is_strict -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py tests\test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --output-json artifacts\bench\rag_eval_partial_router_holdouts_20260425_data_hints_retrieval_only.json --output-md artifacts\bench\rag_eval_partial_router_holdouts_20260425_data_hints_retrieval_only.md --top-k 8
```

The retrieval-only smoke preserved the latest held-out-pack shape:
expected-owner hit@1/top3/top-k `20/21`, best rank `1.00`.

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
