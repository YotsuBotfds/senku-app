# ANDROID-H9 Documentation, Ownership Maps, and Tracker Cleanup

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H9_docs_ownership_tracker_cleanup`

## Role

Docs-focused main agent. No app code. Good slice when Codex usage is limited or the tree needs quiet consolidation.

## Preconditions

- No active code workers.
- Working tree clean.
- Latest proof index accurate.

## Outcome

Reduce navigation overhead for future agents by making ownership, validation, and history concise and findable.

## Boundaries

- No app code changes.
- No test behavior changes unless fixing docs links in script contracts.
- Preserve historical content when splitting tracker.

## Tasks

### 1. Android controller/policy ownership map

Create or update `notes/ANDROID_CONTROLLER_POLICY_OWNERSHIP_MAP.md` under 120 lines.

Map:

- MainActivity boundaries
- MainRouteCoordinator / MainRouteDecisionHelper / MainRouteEffectController
- MainSearchController / AskQueryController / LatestJobGate
- AnswerPresenter / OfflineAnswerEngine
- PackRepository policies
- DetailActivity policies
- HostInference policies
- ReviewDemoPolicy
- Harness scripts

### 2. Route expectations doc

Create/update `notes/ROUTE_EXPECTATIONS_CURRENT_HEAD.md` under 120 lines.

Include:

- prompt
- route family
- expected owner guide(s)
- test location
- live smoke or broad manual

### 3. How to add a route test

Doc rules:

- start with JVM/headless if possible
- live smoke only for fast canaries
- broad parity is manual/heavy
- never tune retrieval without before/after route output

### 4. How to add a policy/helper

Doc rules:

- must remove production logic
- must have production call site
- must have direct behavior tests
- must not duplicate existing logic
- name by ownership/responsibility

### 5. Split backendphases history

Only if tree is quiet:

- `notes/backendphases.md` = short current checklist
- `notes/backendphases_history_20260501.md` = historical log
- preserve content
- no app code in same commit

### 6. Source-string test scout

Read-only classification:

- acceptable script contract
- brittle implementation lock
- should convert to behavior/dry-run test

No mass conversion.

## Acceptance

- Future agent can find ownership and validation rules quickly.
- Backend tracker is shorter or has a clear cleanup plan.
- No app code changed.
- No history lost.

## Validation

- `git diff --check`
- no Gradle required unless docs point to changed commands.

## Delegation hints

- Scout: classify existing docs and source-string tests.
- Main agent: write concise docs.

## Report format

```text
Docs added/updated:
Tracker split:
History preserved:
App code changed:
Validation:
Deferred docs:
```
