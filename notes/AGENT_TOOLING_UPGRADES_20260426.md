# Agent Tooling Upgrades - 2026-04-26

Purpose: near-top queue items that reduce overnight friction, worker collision,
runtime drift, and local-only proof loss. This is based on the xhigh tooling
research scout plus local repo context.

Status update, 2026-04-26 overnight: the GitHub repository has been created and
pushed at `https://github.com/YotsuBotfds/senku-app.git`. Local `master` tracks
`origin/master`; the latest tooling-infrastructure proof recorded here is
`7f8e774` (`keep ci embedding service alive for gate`). The top tooling
queue has landed through the retrieval-profile comparison slice and CI gate
self-indexing fix. Current pushed commits:

- `4e172d9` runtime endpoint preflight
- `1cd7110` non-Android GitHub regression gate
- `8dd0c48` worker worktree lanes and status
- `26f8a15` Actions security lint gate
- `00f9863` attested non-Android regression bundle
- `2b89539` bench metrics lake
- `b117a63` RAG trace diagnostics core/analyzer join
- `861e758` bench RAG trace spans
- `dc7805f` PowerShell quality gate
- `9085dd9` optional RAG eval dataset export
- `cb8ffac` retrieval-profile comparison tooling
- `c1ae983` tooling queue completion status
- `b37c427` accepted partial-router drift allowlist for the strict
  non-Android regression gate
- `3f37f58` CI-side embedding service, retrieval index rebuild, and private-repo
  attestation skip for the non-Android regression gate
- `7f8e774` single-step CI embedding/index/gate execution so the FastEmbed
  process stays alive for retrieval eval

## Near-Top Dependency/Security Queue

- Status update, 2026-04-26: the bounded near-top slice now has Dependabot
  coverage for GitHub Actions and root Python requirements metadata, plus a
  local `scripts/run_dependency_security_scan.ps1` lane for `pip-audit`.
  Android Gradle dependency verification remains a documented follow-on and was
  not reopened in this slice.
- Status update, 2026-04-26 dependency lock slice: root Python dependencies now
  keep `requirements.txt` as the human-edited top-level input and add
  `requirements.lock.txt` as a generated Windows CPython 3.13 constraints/lock
  artifact with hashes. Refresh it with
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\compile_python_lock.ps1`;
  use `-WhatIf` to print the exact `uv pip compile` invocation without writing.
- Add dependency locking/scanning prep from the completed tooling scout: create
  a reproducible Python dependency lock path (`uv lock` or equivalent), enable
  Dependabot for Python and GitHub Actions, add a local/CI `pip-audit` or OSV
  scan lane, and track Gradle dependency verification as an Android follow-on
  rather than reopening emulator work in this slice.
- Keep this as queue prep only until assigned: no workflow/code edits from this
  note pass; expected future touch points are dependency metadata, security scan
  scripts/workflows, and Android Gradle verification docs or config.

## Tonight Top 3

1. Runtime endpoint preflight
   - Why: prevents wrong 1234/1235 endpoint, generation-vs-embedding mixups,
     and stale model assumptions before a long bench run.
   - Implement:
     - Add `data/runtime_targets.json` with known local generation and embedding
       endpoints, expected models, and capability notes.
     - Add `scripts/check_runtime_endpoints.py` to call `/v1/models`, print env
       overrides, classify generation/embed availability, and exit nonzero on
       mismatch.
     - Call it from `scripts/run_guide_prompt_validation.ps1` and the future
       non-Android gate before bench work.
   - Touch: `data/runtime_targets.json`,
     `scripts/check_runtime_endpoints.py`,
     `scripts/run_guide_prompt_validation.ps1`.
   - Validate:
     `& .\.venvs\senku-validate\Scripts\python.exe -B scripts\check_runtime_endpoints.py --gen-url http://127.0.0.1:1235/v1 --embed-url http://127.0.0.1:1234/v1`
   - Risk/effort: low-medium, half day.
   - Sources: LM Studio OpenAI-compatible API docs,
     <https://lmstudio.ai/docs/developer/openai-compat>;
     vLLM OpenAI-compatible server docs,
     <https://docs.vllm.ai/en/stable/serving/openai_compatible_server/>.

2. Worktree worker lanes and status
   - Why: lets multiple agents work in isolated linked worktrees, reducing
     dirty-tree collisions and main-thread staging friction.
   - Implement:
     - Add `scripts/start_agent_worktree.ps1` to create a named branch/worktree
       under a predictable sibling directory.
     - Add `scripts/worker_lane_status.py` to report worktree path, branch,
       base SHA, dirty state, and lane lease JSON.
     - Surface lane status in `scripts/live_queue_monitor.py` and
       `scripts/agent_context_snapshot.py`.
     - Update `notes/SUBAGENT_WORKFLOW.md` with ownership and closeout rules.
   - Touch: `scripts/start_agent_worktree.ps1`,
     `scripts/worker_lane_status.py`, `scripts/live_queue_monitor.py`,
     `scripts/agent_context_snapshot.py`, `notes/SUBAGENT_WORKFLOW.md`.
   - Validate: `git worktree list --porcelain`; unit tests for monitor/snapshot.
   - Risk/effort: medium, half to one day.
   - Sources: Git worktree docs,
     <https://git-scm.com/docs/git-worktree>.

3. Non-Android GitHub regression gate
   - Why: publishes the local fast gate as a repeatable manual/PR check without
     reopening Android. Makes proofs visible in GitHub instead of only local
     ignored artifacts.
   - Implement:
     - Add `scripts/run_non_android_regression_gate.ps1` for py_compile,
       deterministic/routing tests, answer-card validation, prompt expectation
       validation, and optional focused bench/analyzer.
     - Add `.github/workflows/non_android_regression.yml` with
       `workflow_dispatch` and `workflow_call`.
     - Upload markdown/json diagnostics with `actions/upload-artifact` and write
       `$GITHUB_STEP_SUMMARY`.
   - Touch: `.github/workflows/non_android_regression.yml`,
     `scripts/run_non_android_regression_gate.ps1`.
   - Validate: local parser/what-if, then GitHub `workflow_dispatch`.
   - Risk/effort: low-medium, half day.
   - Sources: GitHub reusable workflows,
     <https://docs.github.com/en/actions/how-tos/reuse-automations/reuse-workflows>;
     upload-artifact, <https://github.com/actions/upload-artifact>.

## Next Queue

4. Actions security lint gate
   - Add actionlint/zizmor workflow, explicit `permissions: contents: read`, and
     CODEOWNERS for `.github/` and high-risk scripts.
   - Validate: `uvx actionlint .github/workflows/*.yml`;
     `uvx zizmor .github/workflows --offline`.
   - Source: GitHub secure use reference,
     <https://docs.github.com/en/actions/reference/security/secure-use>.

5. DuckDB metrics lake
   - Export diagnostics, run manifests, and trend summaries to Parquet for fast
     cross-run queries.
   - Touch: `scripts/export_bench_metrics_lake.py`,
     `scripts/query_bench_metrics.py`, maybe `scripts/rag_trend.py`.
   - Validate: unit tests and one sample query over `artifacts/bench`.
   - Source: DuckDB Parquet docs,
     <https://duckdb.org/docs/stable/data/parquet/overview>.

6. CI artifact publication and attestation
   - Attach artifact URLs to run manifests and use GitHub attestations for
     release-grade bundles.
   - Touch: `.github/workflows/*.yml`, `scripts/write_run_manifest.py`,
     `scripts/export_eval_bundle.py`.

7. RAG trace JSONL / OpenTelemetry shim
   - Emit lightweight per-prompt spans for retrieve, rerank, compose, generate,
     and verify.
   - Touch: `rag_trace.py`, `bench.py`, `query.py`,
     `scripts/analyze_rag_bench_failures.py`.

8. Optional Ragas/Phoenix eval export
   - Export Senku diagnostics rows to external eval-friendly local datasets
     without making those tools required runtime services.
   - Touch: `scripts/export_rag_eval_dataset.py` and tests.

9. Adaptive retrieval profile ablations
   - Extend retrieval-only eval to compare retrieval profiles by hit@k, owner
     concentration, marker risk, and latency.
   - Touch: `scripts/evaluate_retrieval_pack.py`,
     `scripts/compare_retrieval_profiles.py`.

10. PowerShell quality gate
    - Add Pester and PSScriptAnalyzer checks for wrappers, cleanup scripts, and
      launcher safety surfaces.
    - Touch: `scripts/run_powershell_quality_gate.ps1`,
      `tests/powershell/*.Tests.ps1`, `PSScriptAnalyzerSettings.psd1`.

## Queue Placement

- Put items 1-3 above further EVAL8 micro-fixes unless a fresh safety failure
  appears.
- Keep item 4 immediately after the first GitHub workflow lands.
- Keep Android/emulator work separate unless explicitly reopened.
- Continue preserving local planner handoff notes unless the user asks to
  publish them.
