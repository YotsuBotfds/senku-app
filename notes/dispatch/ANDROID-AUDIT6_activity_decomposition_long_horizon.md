# Android Audit 6 - Activity Decomposition Long-Horizon Plan

## Slice

`ANDROID-AUDIT6` - plan long-term decomposition of `MainActivity` and `DetailActivity` without starting a risky rewrite.

## Role

Read-only architecture planner.

## Preconditions

- Current head is clean.
- Read existing ownership maps and controller/policy docs:
  - `notes/ANDROID_CONTROLLER_POLICY_OWNERSHIP_MAP.md`
  - `notes/LATEST_ANDROID_PROOFS.md`
  - `notes/ANDROID_VALIDATION_LADDER.md`
- Inspect current `MainActivity.java` and `DetailActivity.java`.

## Outcome

Create a realistic decomposition plan that preserves current tested boundaries and avoids a large MVVM/Fragment rewrite.

## Findings being folded in

External audits flagged:
- `MainActivity.java` and `DetailActivity.java` are massive.
- Suggested full MVVM/Fragments/Compose navigation migration.

Current guidance:
- The size concern is valid.
- Full migration is too risky now.
- Existing controller/policy boundaries should be leveraged.
- Next steps should be inventory and seam extraction only where real production logic remains.

## Tasks

1. Read-only responsibility inventory:
   - route/navigation
   - search/ask
   - saved/home-related
   - category/home chrome
   - pack/model install
   - follow-up generation
   - source/evidence/detail state
   - review/demo fixtures
   - formatting/presentation
   - Android view binding/lifecycle glue
2. For each group:
   - existing extracted boundary
   - remaining methods in Activity
   - tests covering it
   - extraction risk
   - recommended action: leave, test, extract, or redesign later
3. Produce a phased plan:
   - Phase 1: no-code inventory/proof.
   - Phase 2: extract pure render/presentation decisions already tested.
   - Phase 3: extract lifecycle-safe controllers.
   - Phase 4: consider ViewModel/Compose navigation only after RC.
4. Define no-go items:
   - no broad Activity rewrite.
   - no Fragment migration before RC.
   - no Compose navigation migration before route/back smoke coverage is complete.

## Boundaries

Global guardrails for this audit-derived slice:
- Start read-only; patch only verified current-head behavior.
- Do not launch broad extraction waves.
- Do not touch visual polish unless the slice explicitly says accessibility/semantics.
- Do not tune retrieval.
- Prefer focused behavior tests over source-string tests.
- Bundle any tracker update with the code/test slice.
- Stop after one small commit for this slice and report validation.


Docs/planning only. Do not edit Activity code.

## Acceptance

- A decomposition map exists.
- It names what remains in each Activity.
- It identifies 3 safe future extraction seams and 3 explicitly deferred rewrites.
- No production code changes.

## Delegation hints

- Use read-only explorer only.
- Use git history if needed to understand why a boundary exists.

## Report format

```text
Current head:
MainActivity responsibility map:
DetailActivity responsibility map:
Safe seams:
Deferred rewrites:
Recommended next implementation slice:
Working tree:
```
