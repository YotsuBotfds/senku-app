# ANDROID-H4 Harness, CI, and Proof Operations

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H4_harness_ci_proof_ops`

## Role

Main agent plus script-contract workers. No Android UI/product implementation unless a harness bug requires a tiny app-side proof hook.

## Preconditions

- Route smoke runner exists.
- Timing parser exists.
- Latest proof index exists.
- GitHub Actions may still be stale; local proof is authoritative until CI is fixed.

## Outcome

Make proof reproducible, bounded, and easy to inspect when Codex is not being watched live.

## Boundaries

- Do not run full functional matrix by default.
- Do not modify production app code unless fixing a confirmed harness token/leak/timeout boundary.
- Do not overhaul CI in one step.
- Prefer script contract tests and dry-run behavior.

## Tasks

### 1. No raw long ADB contract

Add a contract test that new/changed scripts do not use raw unbounded `adb` for long operations:

- install
- push/pull large files
- instrumentation
- logcat wait loops
- reverse/forward

Allow short calls only with explicit comment or bounded helper.

### 2. Harness stop/orphan coverage

Audit and cover stop/orphan helpers for:

- route smoke runner
- instrumented UI smoke
- functional UX matrix wrapper
- route parity/broad runs
- logcat collectors
- local server wrappers
- adb listener processes

### 3. Matrix status dashboard

For long matrix runs, ensure artifact includes:

- preset name
- child PID
- elapsed time
- timeout
- artifact root
- device serial
- last log line or last status marker

### 4. Proof artifact index

Maintain `notes/LATEST_ANDROID_PROOFS.md` or artifact JSON with:

- latest full JVM/assembly proof
- latest route smoke
- latest phone smoke
- latest physical smoke
- latest route parity timing
- latest functional matrix if any

### 5. Minimal CI scout

Read-only first:

- list workflows
- explain stale/failing workflows
- identify cheapest reliable green gate

Potential first workflow:

- Python script contracts
- PowerShell parser gates
- selected JVM tests
- no emulator

Do not add emulator CI yet.

### 6. Artifact retention cleanup

Add cleanup or docs for:

- logcat
- screenshots
- dumps
- route smoke summaries
- route parity timing
- matrix summaries
- harness locks

## Acceptance

- Harness long operations are bounded or called out.
- Proof index is accurate.
- CI plan is minimal and explicit.
- No app behavior changed unless fixing a confirmed harness bug.

## Validation

- Python script contracts.
- PowerShell parser gate for touched scripts.
- Dry-run script tests.
- `git diff --check`.
- Live route smoke only if route smoke script changed materially.

## Delegation hints

- Worker A: script contracts for bounded ADB.
- Worker B: stop/orphan coverage.
- Scout: CI read-only triage.
- Main agent: proof-index updates and summary.

## Report format

```text
Scripts changed:
Bounded operations added:
Contracts run:
CI findings:
Artifacts/index updated:
Production code changed:
Validation:
```
