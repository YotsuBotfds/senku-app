# Android Audit 4 - Exception Diagnostics and Closed-State Guards

## Slice

`ANDROID-AUDIT4` - reduce silent failure modes and add closed-state guards where current-head behavior is unsafe.

## Role

Read-only diagnostics scout, then focused implementation worker for one confirmed gap at a time.

## Preconditions

- Current head is clean.
- Read recent pack/vector/host/saved hardening commits.
- Inspect:
  - `AnswerCardDao.java`
  - `PackRepository.java`
  - `ChatSessionStore.java`
  - `OfflineAnswerEngine.java`
  - `AnswerCardRuntime.java`
  - `UserVisibleFailureCopy.java`
  - `LatencyPanel.java`

## Outcome

Silent failures should either be intentionally fail-closed with a test or logged/classified enough for diagnosis. Public repository methods should fail predictably after close.

## Findings being folded in

External audit flagged:
- `catch (SQLiteException ignored)` in answer-card/search paths.
- `catch (JSONException ignored)` in session store.
- `catch (RuntimeException ignored)` in logging/runtime helpers.
- `PackRepository` public methods may throw raw `SQLiteException` after close.
- `UserVisibleFailureCopy` may over-generalize all exceptions.

Current context:
- Recent commits improved pack/vector validation, host response handling, saved/home containment, and user-visible copy.
- Do not undo fail-closed behavior; add diagnostics and tests where useful.

## Tasks

1. Read-only grep:
   - `catch (... ignored)`
   - `catch (... exc)` without logging/classification
   - public methods using closed SQLite/database/vector state.
2. Classify each:
   - intentional fail-closed, tested.
   - should `Log.w`.
   - should classify to user-visible failure.
   - should throw `IllegalStateException` early.
3. Patch one confirmed gap at a time:
   - Prefer `Log.w(TAG, "...", exc)` for diagnostics.
   - Prefer stable user-visible categories for UI copy.
   - Add closed-state guards where public API currently fails unpredictably.
4. Tests:
   - corrupted session JSON does not erase silently without coverage.
   - answer card schema failure logs/fails closed.
   - closed PackRepository method throws clear `IllegalStateException` or documented result.

## Boundaries

Global guardrails for this audit-derived slice:
- Start read-only; patch only verified current-head behavior.
- Do not launch broad extraction waves.
- Do not touch visual polish unless the slice explicitly says accessibility/semantics.
- Do not tune retrieval.
- Prefer focused behavior tests over source-string tests.
- Bundle any tracker update with the code/test slice.
- Stop after one small commit for this slice and report validation.


Do not turn every internal diagnostic into user-facing copy. Do not change retrieval ranking. Do not add noisy logs in tight loops.

## Acceptance

- A catch/closed-state inventory exists.
- At least one high-risk silent failure is patched or explicitly deferred.
- Focused tests cover patched behavior.
- `:app:assembleDebugAndroidTest` passes if Android source changed.
- `git diff --check` passes.

## Delegation hints

- Use read-only scout first; many ignored catches may be intentional.
- Use Android logging docs if unsure about log levels.

## Report format

```text
Current head:
Ignored-catch inventory:
Patched gap:
Tests:
Deferred risks:
Validation:
Working tree:
```
