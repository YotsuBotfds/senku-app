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
7. `scripts/plan_incremental_ingest.py` - landed.
   Plan focused guide re-ingest batches from changed guide/catalog inputs.

## 2026-04-25 Scout Triage Queue

Three read-only scouts reviewed the current RAG/tooling lane with web search
encouraged and converged on the same operating shape: retrieval is now mostly
healthy for the partial/router pack, while agents are still spending too much
context stitching together related diagnostic runs and separating citation
owner misses from retrieval misses.

Actionable order, now mostly landed:

1. **Eval-pack owner/citation closure - landed.**
   The partial/router held-out pack now has `19` expected-supported rows,
   `2` accepted clarify rows, and `0` retrieval/ranking/generation/artifact
   misses after source packaging plus the `GD-397` expectation cleanup.

2. **RAG experiment lineage reporting - landed.**
   `scripts/rag_experiment_lineage.py` discovers related diagnostics and shows
   the full-pack progression through `gd397_expectation_cleanup`.

3. **Retrieval-only evaluator - landed.**
   `scripts/evaluate_retrieval_pack.py` gives generation-free owner hit/rank
   checks before spending local generation cycles.

4. **RAG regression gate - landed.**
   `scripts/rag_regression_gate.py` compares diagnostics and can fail on
   configured count/rate regressions.

5. **Data-backed owner-hint rerank rules - landed.**
   The narrow partial/router owner hints now live in
   `data/rag_owner_rerank_hints.json` with strict marker-group matching.

6. **Touched-file quality gates - landed for mojibake.**
   `scripts/scan_mojibake.py --touched` can scan only changed text files and
   fail only on non-allowlisted findings.

7. **Report-only artifact retention/storage tooling - landed.**
   `scripts/summarize_artifact_storage.py` summarizes storage, and
   `scripts/plan_artifact_retention.py` builds protected dry-run retention
   plans.

8. **Run manifest auto-enrichment - landed.**
   `scripts/write_run_manifest.py` now captures capped git state and artifact
   path context when explicit fields are omitted.

9. **Prompt expectation validator - landed.**
   `scripts/validate_prompt_expectations.py` catches structural prompt-pack
   expectation drift and can cross-check retrieval-only eval outputs.

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

## 2026-04-25 Artifact Retention Planner Proof

Added `scripts/plan_artifact_retention.py`, a stdlib dry-run planner that
groups generated artifact families, protects paths referenced by dispatch/spec
notes or run manifests, and emits JSON/Markdown archive/delete candidates
without deleting, moving, or rewriting artifacts.

Validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_plan_artifact_retention -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\plan_artifact_retention.py tests\test_plan_artifact_retention.py
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\plan_artifact_retention.py --root artifacts --limit 8 --output-json artifacts\bench\artifact_retention_plan_20260425.json --output-md artifacts\bench\artifact_retention_plan_20260425.md --json
```

The real smoke completed as a report-only scan over `artifacts`.

## 2026-04-25 Prompt Expectation Validator Proof

Added `scripts/validate_prompt_expectations.py`, a stdlib linter for JSONL/CSV
prompt packs. It validates expected-guide ID shape and existence, prompt ID
uniqueness for expectation-bearing rows, field overlap between `guide_id` and
explicit expectation fields, optional allowed-drift suppressions, and optional
retrieval-eval owner-hit warnings.

Validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_validate_prompt_expectations -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\validate_prompt_expectations.py tests\test_validate_prompt_expectations.py
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --fail-on-errors
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py --prompts-dir artifacts\prompts --fail-on-errors
```

The focused held-out pack and broad prompt discovery both pass with `0`
errors. With retrieval-eval input, `RE2-UP-011` reports a warning because it is
an accepted clarify path whose expected owner is absent from top-k; keep
`--fail-on-warnings` for strict retrieval-only gates.

## 2026-04-25/26 Tooling Queue Visibility

- **RAG-TOOL6 incremental ingest planner - landed.**
  `scripts/plan_incremental_ingest.py` gives workers a scoped guide ingest plan
  before trusting retrieval after guide/catalog edits.
- **RAG-TOOL7 non-Android regression gate recipe - queued.**
  Capture the desktop-only validation recipe that keeps Android/emulator work
  out of ordinary RAG/tooling regression gates.
- **RAG-TOOL8 mojibake cleanup queue - queued.**
  Track text-encoding cleanup targets through
  `notes/dispatch/RAG-TOOL8_mojibake_cleanup_queue.md`.
- **RAG-TOOL9 worker orchestration monitoring - queued.**
  Track multi-worker status and stale-handoff checks through
  `notes/dispatch/RAG-TOOL9_worker_orchestration_monitoring.md`.

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
