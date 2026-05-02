# Android Audit 5 - PromptHarness Flake Reduction

## Slice

`ANDROID-AUDIT5` - reduce `PromptHarnessSmokeTest` flakiness and improve wait semantics without changing app behavior.

## Role

Harness/test worker. No production changes unless a clear app-side idling hook is required and reviewed.

## Preconditions

- Current head is clean.
- Read:
  - `PromptHarnessSmokeTest.java`
  - `HarnessBusyIdlingResource.java`
  - `HarnessTestSignals.java`
  - live smoke scripts
  - route smoke runner
  - validation ladder notes

## Outcome

Make the largest smoke test less dependent on fixed sleeps and easier to split/diagnose.

## Findings being folded in

External audit flagged:
- `PromptHarnessSmokeTest` is very large.
- It contains many `SystemClock.sleep()` calls.
- Fixed sleeps are flaky on slow CI/emulators.

Current context:
- Recent work improved route smoke and harness breadcrumbs.
- Do not run the full functional matrix by default.

## Tasks

1. Read-only inventory:
   - count `SystemClock.sleep()` calls.
   - group by purpose: install settle, UI settle, keyboard, generation, route change, screenshot.
   - identify which can be replaced by existing idling resources or polling.
2. Patch one small group:
   - replace fixed sleep with bounded polling for a visible condition or `HarnessBusyIdlingResource`.
   - keep timeout explicit and artifact-friendly.
3. Split planning:
   - identify natural sub-classes or presets.
   - do not split the whole file in one commit unless trivial.
4. Add/extend contract tests for smoke runner status if script behavior changes.

## Boundaries

Global guardrails for this audit-derived slice:
- Start read-only; patch only verified current-head behavior.
- Do not launch broad extraction waves.
- Do not touch visual polish unless the slice explicitly says accessibility/semantics.
- Do not tune retrieval.
- Prefer focused behavior tests over source-string tests.
- Bundle any tracker update with the code/test slice.
- Stop after one small commit for this slice and report validation.


Do not rewrite the whole harness. Do not increase default smoke duration. Do not run the full matrix unless explicitly requested.

## Acceptance

- Sleep inventory reported.
- At least one high-value fixed sleep group replaced or deferred with reason.
- Tests/contract gates pass.
- If live run is done, use a short preset only.

## Delegation hints

- Use one harness worker only; PromptHarness file is large and conflict-prone.
- Consult Android test/idling docs if replacing waits.

## Report format

```text
Current head:
Sleep inventory:
Changed wait:
Tests:
Live run:
Deferred wait groups:
Working tree:
```
