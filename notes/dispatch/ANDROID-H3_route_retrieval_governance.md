# ANDROID-H3 Route and Retrieval Governance

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H3_route_retrieval_governance`

## Role

Main agent with route-output scout. High reasoning only if a route expectation is ambiguous.

## Preconditions

- `notes/PACK_RETRIEVAL_POLICY_MAP.md` exists.
- Live route smoke runner exists.
- Broad route parity is documented as manual/heavy.
- No active retrieval tuning is in progress.

## Outcome

Make route behavior governable: expectations visible, fast route smokes limited, broad parity timed and diagnostic, and retrieval changes gated by before/after artifacts.

## Boundaries

- Do not tune retrieval in this slice unless a golden route test fails with assertion output.
- Do not add expensive route specs to live smoke.
- Do not promote broad parity to default validation.
- Do not edit route policy based on answer prose alone.

## Tasks

### 1. Route expectations manifest

Create or update `notes/ROUTE_EXPECTATIONS_CURRENT_HEAD.md` under 120 lines.

Include:

- prompt
- route family
- expected search owner(s)
- expected context owner(s)
- test location
- live-smoke or broad-manual classification
- last artifact/proof pointer if known

### 2. Fast live smoke governance

Keep live smoke small:

- rain shelter / `GD-345`
- optionally one fast governance route if proven fast

Do not add house, water distribution, or soapmaking live smoke unless timing data proves fast.

### 3. Broad route parity timing workflow

When broad parity is run:

- hard outer timeout
- preserve logcat/instrumentation output
- parse `SenkuRouteParity` breadcrumbs
- write JSON and Markdown timing summary
- identify slowest completed route
- do not tune retrieval during same run

### 4. Slow-route warning summary

Make parser/report flag, not fail, slow routes:

- search_ms > 5000
- context_ms > 5000
- total_ms > 10000

### 5. Before/after route artifact script

Add a no-production-change diagnostic that emits for selected prompts:

- top search rows
- answer context rows
- guide IDs
- sections
- retrieval modes
- scores/support labels if available
- timings

Use this as prerequisite for route tuning.

### 6. Route tuning guardrail doc

Document:

```text
No route policy, threshold, owner hint, candidate budget, or anchor change without before/after route-output proof.
```

## Acceptance

- Route expectations are visible and concise.
- Live route smoke remains fast.
- Broad parity output can be summarized even on timeout.
- No retrieval behavior changes unless backed by failing assertion and before/after artifact.

## Validation

- Parser tests for timing summaries if touched.
- Route smoke live only if route smoke changed.
- `:app:assembleDebugAndroidTest` if androidTest changed.
- `git diff --check`.

## Delegation hints

- Scout: map existing route tests and route expectations.
- Worker A: docs/manifest.
- Worker B: timing parser/report enhancements.
- Main agent: enforce no tuning.

## Report format

```text
Route docs updated:
Live smoke changed:
Broad parity run:
Timing artifact:
Retrieval changed:
Validation:
Deferred route risks:
```
