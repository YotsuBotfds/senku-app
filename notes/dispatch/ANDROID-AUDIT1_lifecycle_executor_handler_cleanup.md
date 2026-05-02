# Android Audit 1 - Lifecycle Executor and Handler Cleanup

## Slice

`ANDROID-AUDIT1` - verify and fix Activity lifecycle cleanup for executors, handlers, pending runnables, and repository resources.

## Role

Implementation worker after a read-only lifecycle scout.

## Preconditions

- Current head is clean.
- Read `DetailActivity.java`, `MainActivity.java`, `AnswerPresenter.java`, `LatestJobGate.java`, and any existing lifecycle tests.
- Check recent commits for harness-token cleanup, especially follow-up generation.

## Outcome

Activities must not leave executor work, handler callbacks, streaming ticks, stall ticks, repository references, or harness busy signals alive after destruction.

## Findings being folded in

External audits flagged:
- `MainActivity` and `DetailActivity` are very large and own executor resources.
- `DetailActivity` has `ExecutorService executor`, a main-thread `Handler`, `streamingCursorTick`, and `generationStallTick`.
- Handler runnables should be cancelled on destroy.
- Executors should be shut down on destroy.
- Lifecycle cleanup should be tested for no harness-token leaks and no post-destroy UI mutation.

## Tasks

1. Read-only scout:
   - Does `MainActivity` override `onDestroy()`?
   - Does `DetailActivity` override `onDestroy()`?
   - Are `executor.shutdownNow()` or `shutdown()` called?
   - Are `uiHandler.removeCallbacksAndMessages(null)` or equivalent targeted removals called?
   - Are streaming/stall monitors cleared?
   - Are repository close/failure paths contained?
   - Does pending generation know when the Activity is destroyed?
2. If missing, add minimal lifecycle cleanup:
   - `uiHandler.removeCallbacksAndMessages(null)` or targeted callback removals.
   - `executor.shutdownNow()` if safe.
   - clear streaming/stall monitors.
   - close or release owned repository if Activity owns it.
3. Add focused tests where possible:
   - use a fake host/controller if direct Activity testing is too heavy.
   - add a lifecycle policy/helper test only if extracting a tiny helper removes real production logic.
   - otherwise add instrumentation/lifecycle smoke only if needed.

## Boundaries

Global guardrails for this audit-derived slice:
- Start read-only; patch only verified current-head behavior.
- Do not launch broad extraction waves.
- Do not touch visual polish unless the slice explicitly says accessibility/semantics.
- Do not tune retrieval.
- Prefer focused behavior tests over source-string tests.
- Bundle any tracker update with the code/test slice.
- Stop after one small commit for this slice and report validation.


Do not start full Activity decomposition. Do not migrate to ViewModel/Fragments in this slice.

## Acceptance

- Destroy path is explicit in both large Activities, or scout report explains why one is not needed.
- Handler callbacks cannot continue to update destroyed views.
- Executor cannot keep the Activity alive after destroy.
- Existing follow-up generation token fix remains green.
- Focused tests pass.
- `:app:assembleDebugAndroidTest` passes if Activity source changed.
- `git diff --check` passes.

## Delegation hints

- Start with a read-only scout.
- If patching, one worker only; this touches high-risk Activity files.
- Use Android lifecycle docs if uncertain about `shutdownNow()` vs allowing current job to complete.

## Report format

```text
Current head:
Lifecycle findings:
Files changed:
Tests added:
Validation:
Known deferred lifecycle risks:
Working tree:
```
