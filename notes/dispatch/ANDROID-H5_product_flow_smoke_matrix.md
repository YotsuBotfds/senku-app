# ANDROID-H5 Product-Flow Smoke Matrix

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H5_product_flow_smoke_matrix`

## Role

Main agent with small androidTest/JVM workers. Functional UX only; no visual polish.

## Preconditions

- Current-head proof gate is green.
- Live route smoke is green.
- No broad route parity or functional matrix is running.

## Outcome

Cover real user flows that matter for release readiness: first run, pack install, Ask unavailable, Saved/back, zero results, and offline-only behavior.

## Boundaries

- Do not change layout, typography, XML spacing, or Compose chrome.
- Do not add screenshot parity assertions.
- Prefer policy/controller/JVM tests where possible; use live smoke only for full-flow proof.

## Tasks

### 1. First-run install flow

Test:

- fresh install
- pack preparing
- pack ready
- pack install failed
- retry reinstall
- auto query while installing
- open saved while installing

### 2. Shared submit while installing

Confirm shared input/buttons are suppressed or safely handled while pack install is active.

Expected:

- no stale Search/Ask publish
- no crash
- clear unavailable/preparing copy
- route state recoverable

### 3. Ask unavailable UX

Test:

- no model and no host
- host blocked
- host unreachable
- model file deleted
- reviewed-card answer without model/host

### 4. Search zero-results flow

Test:

- nonsense query
- unsupported query
- category with no results
- back to Home
- suggestions still usable

### 5. Saved + Ask interplay

Scenario:

- open saved guide
- ask follow-up from guide
- save/unsave while answer visible
- back to saved/home

### 6. Offline-only promise

Confirm no network required for:

- browse
- search
- guide detail
- saved
- deterministic answer
- reviewed-card answer
- abstain/no-source

Host generation remains optional and policy-gated.

## Acceptance

- Product-flow tests express release-critical behavior.
- No visual assertions added.
- No retrieval tuning.
- Failures produce actionable copy/state, not generic crashes.

## Validation

- Focused JVM/controller tests.
- `:app:assembleDebugAndroidTest` for androidTest changes.
- One short live smoke only if app flow smoke changed and device is ready.
- `git diff --check`.

## Delegation hints

- Scout: map existing prompt harness/product flow tests.
- Worker A: first-run/install flow.
- Worker B: Ask unavailable/zero-results.
- Worker C: Saved + Ask interplay.

## Report format

```text
Flows covered:
Live smoke run:
Production behavior changed:
User copy changed:
Validation:
Deferred flows:
```
