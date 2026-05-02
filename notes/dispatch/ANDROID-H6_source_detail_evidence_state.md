# ANDROID-H6 Source, Detail, and Evidence State

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H6_source_detail_evidence_state`

## Role

Main agent plus focused policy/controller test workers. Functional behavior only.

## Preconditions

- Current source/detail policies are in place.
- Do not start visual detail/tablet polish.

## Outcome

Make source chips, evidence rails, related guides, and detail back/home behavior reliable across answer, guide, emergency, and task-root surfaces.

## Boundaries

- No layout polish.
- No Compose token changes.
- No source-string tests unless script-contract only.
- Prefer pure policy/controller tests.

## Tasks

### 1. Source chip state machine

Test:

- implicit anchor selected
- tap source A
- tap source A again
- tap source B
- clear selection
- open full guide
- return
- source missing
- same source as implicit anchor
- stale token ignored

### 2. Evidence rail consistency

Phone/tablet should agree on:

- source order
- selected state
- stale token behavior
- xref cap
- placeholder copy
- loaded preview copy
- missing-source behavior

### 3. Related-guide ordering

Test:

- workflow relevance first
- owner guide excluded
- duplicates removed
- missing guide skipped
- cap enforced
- fire/survival relevance priority

### 4. Emergency surface functional route

Functional, not visual:

- open emergency from Ask
- open emergency from guide detail
- back behavior
- save behavior
- source provenance
- semantic top chrome label
- unavailable pack/source path

### 5. Detail task-root back/home

Test:

- detail launched from main
- detail launched as task root
- answer detail
- guide detail
- emergency detail
- saved detail
- source full guide

Assert visible Back/Home semantics and route outcome.

## Acceptance

- Detail/source state machine is covered by behavior tests.
- No UI visual polish landed.
- Missing/stale sources are recoverable.
- Back/Home semantics are explicit.

## Validation

- Focused tests: detail source/presentation/evidence policies, DetailActivity state tests, EmergencySurfacePolicyTest.
- `:app:assembleDebugAndroidTest` if androidTest/source touched.
- `git diff --check`.

## Delegation hints

- Worker A: source chip state machine.
- Worker B: evidence rail consistency.
- Worker C: emergency/task-root routes.

## Report format

```text
Source scenarios covered:
Evidence scenarios covered:
Emergency/detail routes covered:
Production behavior changed:
Validation:
Deferred visual items:
```
