# Deep Research Report Triage - 2026-04-26

Source report: `C:\Users\tateb\Downloads\deep-research-report.md`
Triage time: 2026-04-26 morning

## Corrections

- The report's HEAD claim is stale. It cites `3010c2d`, but current local and
  remote `master` are `71505a3e0ca416f7ca62347cd415c612b0be78ac`.
- Classic commit statuses are empty for `71505a3`, but GitHub Actions check
  runs are visible:
  - Actions Security Lint: success
  - PowerShell Quality: success
  - Non-Android Regression: timed out/cancelled in `Run non-Android regression gate`
- The current CI blocker is not a missing generation server. The workflow starts
  a local FastEmbed embedding shim at `http://127.0.0.1:8801/v1`; ingest then
  hit repeated embedding timeouts/500s during rebuild.

## Immediate Safety / Correctness

No direct unsafe app behavior was proven by the report. Most safety concerns are
architecture-level inferences and should not become guide, deterministic-rule,
or analyzer edits without a failing row or focused proof.

Current EVAL8 proof status from `rag_eval8_compound_boundary_holdouts_20260425_after_fs_ws_md_20260426_diag`:

- `RE8-CM-001`: reviewed-card runtime pass, `strong_supported`, card/evidence/claim pass.
- `RE8-FS-001`: reviewed-card runtime pass, `strong_supported`, card/evidence/claim pass.
- `RE8-TR-001`: generated answer is `expected_supported`; remaining value is proof hardening, not a known behavior miss.

## Validation Infrastructure

### DR-CI-001 - Resolve Current Non-Android Gate Timeout
- Source: deep research / CI / user log
- Finding type: ci_workflow
- Severity: high
- Evidence: run `24955889219` timed out after repeated FastEmbed embedding
  retries during `ingest.py --rebuild`.
- Files likely touched: `.github/workflows/non_android_regression.yml`,
  `scripts/fastembed_openai_server.py`, `ingest.py`, focused tests.
- Validation: focused Python tests, PowerShell parser gate, then rerun
  Non-Android Regression on current head.
- Delegation: central_only
- Done when: current head has a completed non-Android result, and FastEmbed
  failures no longer consume the whole job budget.

### DR-RAG-001 - Verify Stale-Ingest Fail-Closed Gap
- Source: deep research / local inspection
- Finding type: retrieval_ranking
- Severity: high
- Evidence: `db/ingest_manifest.json` exists, but local inspection found it can
  be incomplete after incremental paths, and `query.py` does not validate guide
  freshness before trusting retrieval.
- Files likely touched: `ingest.py`, `query.py`, possible new manifest helper,
  tests.
- Validation: focused tests for changed guide, missing guide, truncated
  manifest, deleted guide, and fresh manifest pass.
- Delegation: subagent_good for fixture/audit; central_only for fail-closed
  design.
- Done when: guide/index drift is machine-detectable, or current behavior is
  proven already safe.

### DR-SAFE-001 - Inventory Cross-Layer Liability Smoke Coverage
- Source: deep research inference
- Finding type: bench_harness
- Severity: medium
- Evidence: many focused route/card/diagnostic tests exist; report did not prove
  a concrete end-to-end gap.
- Files likely touched: tests and/or notes only until a real gap is found.
- Validation: map top high-liability rows to assertions for route, owner,
  reviewed-card activation, emergency-first wording, and diagnostic bucket.
- Delegation: subagent_good
- Done when: missing full-chain assertions are concrete test tasks, or existing
  tests are cited.

## Backlog Reconciliation

### DR-CI-002 - Decide Push-Level Required Check Policy
- Source: deep research / local workflow proof
- Finding type: ci_workflow
- Severity: medium
- Evidence: check runs are visible, but Non-Android Regression is
  workflow-dispatch/workflow-call rather than push-triggered.
- Files likely touched: `.github/workflows/*.yml`, branch protection settings.
- Validation: maintainer decision plus visible required checks on a new push.
- Delegation: needs_human_decision
- Done when: required push/PR/manual check policy is explicit.

### DR-ART-001 - Convert Retention Report Into Approval Proposal
- Source: deep research / handoff
- Finding type: artifact_hygiene
- Severity: medium
- Evidence: report-only retention files show about `63.6 GiB` under artifacts
  and about `30.1 GiB` cleanup candidates.
- Files likely touched: notes only unless cleanup is approved.
- Validation: no deletion; approval-ready keep/archive/delete proposal.
- Delegation: subagent_good
- Done when: protected proof artifacts are separated from cleanup candidates.

## Later Refactor

- Deterministic rule matrix enhancement is useful as report-only visibility, but
  not urgent: registry priorities, overlap tests, near-miss tests, and
  `validate_special_cases.py` already exist.
- Acceptance-vs-diagnostic taxonomy separation remains a low-priority audit
  unless a current false pass or false failure is found.
- Broad architecture refactors from the report are parked until tied to direct
  file, test, artifact, or prompt evidence.
