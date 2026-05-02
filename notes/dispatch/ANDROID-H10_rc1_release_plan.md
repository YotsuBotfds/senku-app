# ANDROID-H10 Android RC-1 Release Plan

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H10_rc1_release_plan`

## Role

Release coordinator. Mostly docs/proof. Implementation only for tiny release-blocking fixes found during proof.

## Preconditions

- Current-head audit complete.
- Safety/Ask, pack/data, and route-smoke gates are at least partially proofed.
- Latest proof index is accurate.

## Outcome

Define and, if possible, cut an Android RC-1 candidate with clear gates, proof artifacts, and backlog separation.

## Boundaries

- Do not add new features.
- Do not run full functional matrix unless explicitly requested.
- Do not tune retrieval during RC proof.
- Do not touch visual polish.

## RC-1 criteria

A head is RC-1 candidate only if:

- `:app:testDebugUnitTest` passes
- `:app:assembleDebug` passes
- `:app:assembleDebugAndroidTest` passes
- `git diff --check` passes
- live route smoke passes
- one phone-basic smoke passes
- Ask unavailable/no-model behavior is proofed
- Saved open/back behavior is proofed
- no known unsafe host endpoint persistence
- no known pack install corruption path
- no known stale Search/Ask overwrite
- no known no-source confident generation
- working tree clean

## Recommended RC smoke set

Keep small:

1. live route smoke
2. phone-basic smoke
3. Ask unavailable/no-model smoke
4. Saved open/back smoke
5. one follow-up smoke

Do not include broad route parity by default.

## Tasks

1. Create `notes/ANDROID_RC1_STATUS_20260502.md`.
2. Fill:
   - candidate head
   - proof commands
   - live artifacts
   - pass/fail table
   - blockers
   - deferred backlog
   - explicit no-go areas
3. Run RC proof ladder if not already run at current head.
4. If any proof fails, classify:
   - app bug
   - harness/device issue
   - test expectation issue
   - known deferred risk
5. Do not fix more than one small RC blocker in this slice.

## Acceptance

- RC-1 status doc exists.
- Candidate head and proof artifacts are explicit.
- Human can decide release/demo readiness.
- Non-RC backlog is separated from blockers.

## Validation

- RC proof ladder as above.
- `git diff --check`.

## Delegation hints

- Scout: gather proof artifacts and latest heads.
- Main agent: run or verify proof ladder and write status.
- Worker only if a tiny blocker is found.

## Report format

```text
RC candidate head:
Proof status:
Live artifacts:
Blockers:
Deferred backlog:
Tiny fixes made:
Working tree:
Recommendation: RC / not RC
```
