# Android Audit 2 - Compose Host Callback and Accessibility Verification

## Slice

`ANDROID-AUDIT2` - verify Compose host callback state and accessibility semantics from the external audit.

## Role

Read-only Compose scout first; implementation only if a current-head behavior test reproduces the issue.

## Preconditions

- Current head is clean.
- Read:
  - `BottomTabBar.kt`
  - `SenkuTopBarHostView.kt`
  - `CategoryShelf.kt`
  - `PaperAnswerCard.kt`
  - `DockedComposer.kt`
  - `EvidencePane.kt`
  - `TabletDetailScreen.kt`
- Check existing Compose host tests before adding new ones.

## Outcome

Determine whether plain callback fields in `AbstractComposeView` host views can actually go stale, and fix only verified issues.

## Findings being folded in

External audit flagged:
- `BottomTabBarHostView.selectionHandler` is a plain field.
- `SenkuTopBarHostView.actionHandler` is a plain field.
- `CategoryShelfHostView.selectionHandler` is a plain field.
- Raw `">"` / `"v"` expand-collapse indicators may be poor accessibility semantics.
- Some Compose allocations/annotations may be inefficient or incorrect.

Important nuance:
- Current host click lambdas appear to read the host field at invocation time, so stale/null handler claims may be over-stated.
- Do not blindly wrap handlers in `mutableStateOf` unless a behavior test shows it matters.

## Tasks

1. Add/extend host behavior tests:
   - set handler A, click invokes A.
   - update to handler B without changing visible model if possible, click invokes B.
   - set null handler, click does not crash.
   - update active tab/state and ensure handler remains current.
2. If tests fail, patch:
   - use `mutableStateOf<Handler?>` or `rememberUpdatedState` pattern where appropriate.
   - avoid broad Compose rewrites.
3. Accessibility scout:
   - locate raw text chevrons/expanders.
   - verify content descriptions/semantics.
   - patch only controls with missing or misleading semantics.
4. Optional performance scout:
   - verify `remember`/`rememberSaveable` misses only if they cause user-visible state loss or repeated heavy allocation.

## Boundaries

Global guardrails for this audit-derived slice:
- Start read-only; patch only verified current-head behavior.
- Do not launch broad extraction waves.
- Do not touch visual polish unless the slice explicitly says accessibility/semantics.
- Do not tune retrieval.
- Prefer focused behavior tests over source-string tests.
- Bundle any tracker update with the code/test slice.
- Stop after one small commit for this slice and report validation.


No visual token/spacing polish. No Material3 migration. No full Compose architecture migration.

## Acceptance

- Callback behavior is tested for all three host views, or scout explains why existing tests cover it.
- Any patch is minimal and behavior-driven.
- Accessibility changes are semantic only.
- Compose UI tests or focused JVM/Kotlin tests pass.
- `:app:assembleDebugAndroidTest` passes if Compose source changed.

## Delegation hints

- Use one read-only scout; then one Compose worker if needed.
- Consult Compose docs for `AbstractComposeView`, state observation, and lambda capture before editing.

## Report format

```text
Current head:
Callback findings:
A11y findings:
Files changed:
Tests:
Validation:
Deferred Compose items:
Working tree:
```
