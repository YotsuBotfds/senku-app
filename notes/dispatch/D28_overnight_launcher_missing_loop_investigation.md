# Slice D28 - document the overnight launcher missing-loop dependency

- **Role:** main agent (`gpt-5.4 xhigh`). Investigation + truth-surface repair slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice writes the current-checkout truth for the overnight/start branch.
- **Why this slice now:** The remaining start/bootstrap bucket is not a simple tracking pass. In this checkout, both `scripts/start_overnight_continuation.ps1` and tracked `scripts/run_overnight_queue_wrapped.ps1` reference `scripts/overnight_continue_loop.ps1`, but that file is absent. Historical queue/spec notes still speak as if the loop is present. The smallest truthful next move is to document the missing dependency and repair stale truth surfaces without trying to recreate or fix the overnight lane.

## Outcome

One focused commit that:

1. Adds a tracked investigation note documenting the missing-loop dependency in the current checkout.
2. Adds narrow status banners / truth-surface repairs to the historical overnight notes that still assume `scripts/overnight_continue_loop.ps1` is present.
3. Updates the live queue so the host/start branch is described truthfully.
4. Does **not** modify any `scripts/*.ps1`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `3406b238b12c43a0728f1b491c4cd04fd0bf26de` (`D27: track Android orchestration runner tranche`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `notes/specs/overnight_launcher_guardrail_spec.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
3. `notes/DAYLIGHT_HYGIENE_QUEUE_2026-04-19.md` may currently be untracked; if so, this slice is allowed to absorb and track it while adding the historical current-checkout banner.
4. `scripts/overnight_continue_loop.ps1` is still absent in this checkout.
5. These scripts still exist for read-only inspection:
   - `scripts/start_overnight_continuation.ps1`
   - `scripts/run_overnight_queue_wrapped.ps1`
   - `scripts/start_fastembed_server.ps1`
   - `scripts/start_litert_host_server.ps1`
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - `4-13guidearchive.zip` remains untracked local-only
   - the large untracked `guides/` tree remains
   - the residual historical root `notes/` backlog remains

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - one new investigation note under `notes/`
  - `notes/specs/overnight_launcher_guardrail_spec.md`
  - `notes/DAYLIGHT_HYGIENE_QUEUE_2026-04-19.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - any `scripts/*.ps1`
  - `tests/powershell/Run-OvernightQueueWrapperTests.ps1`
  - `reviewer_backend_tasks.md`
  - `notes/OVERNIGHT_STATUS.md`
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - recreate `scripts/overnight_continue_loop.ps1`
  - modify launcher behavior
  - sweep `start_fastembed_server.ps1` or `start_litert_host_server.ps1` into a tracking tranche
  - widen into an overnight recovery or implementation slice

## Investigation / edit rules

### Step 1 - confirm the missing-loop dependency read-only

Do a read-only inspection of:

- `scripts/start_overnight_continuation.ps1`
- `scripts/run_overnight_queue_wrapped.ps1`
- `scripts/start_fastembed_server.ps1`
- `scripts/start_litert_host_server.ps1`

Required conclusions to verify before writing:

1. `start_overnight_continuation.ps1` points at `scripts/overnight_continue_loop.ps1`.
2. `run_overnight_queue_wrapped.ps1` also references that same loop script.
3. `scripts/overnight_continue_loop.ps1` is absent in this checkout.
4. `start_fastembed_server.ps1` and `start_litert_host_server.ps1` are adjacent but not implicated in the missing-loop dependency.

### Step 2 - add a new investigation note

Add one new tracked note under `notes/`. Suggested name:

```text
notes/OVERNIGHT_LAUNCHER_MISSING_LOOP_INVESTIGATION_20260423.md
```

The note should include:

1. A concise statement of the missing-loop problem.
2. Evidence:
   - which scripts reference `scripts/overnight_continue_loop.ps1`
   - confirmation that the file is absent in this checkout
3. Current-checkout conclusions:
   - `start_overnight_continuation.ps1` is present but not runnable here
   - `run_overnight_queue_wrapped.ps1` is tracked and historically landed, but its pending-task child-launch path depends on the same missing loop
   - `start_fastembed_server.ps1` is adjacent but not implicated
   - `start_litert_host_server.ps1` is adjacent but not implicated
4. Recommended next step:
   - restore-or-retire follow-up for the overnight loop path
   - separate future Rule-2b/cross-reference slices for `start_fastembed_server.ps1` and `start_litert_host_server.ps1`

### Step 3 - add narrow status banners to the historical overnight notes

Update these two existing tracked docs with short current-checkout banners near the top:

- `notes/specs/overnight_launcher_guardrail_spec.md`
- `notes/DAYLIGHT_HYGIENE_QUEUE_2026-04-19.md`

Required banner content:

1. This is historical 2026-04-19 planning/spec material.
2. As of 2026-04-23, the scripts referenced here still assume
   `scripts/overnight_continue_loop.ps1`, but that file is absent in the current
   checkout.
3. Point readers to the new investigation note for current state.

Do **not** rewrite the historical body; add only a short banner/status block.

### Step 4 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the live queue minimally and truthfully:

1. Advance the `Last updated` line to include D28.
2. In item `(j)` or nearby carry-over wording, make the overnight/start branch truthful:
   - `start_overnight_continuation.ps1` remains deferred and blocked on the missing loop dependency
   - `start_fastembed_server.ps1` and `start_litert_host_server.ps1` remain deferred for separate follow-up
3. Add a completed-log entry for D28 summarizing:
   - missing-loop dependency confirmed
   - investigation note added
   - historical overnight docs bannered
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- One new investigation note exists under `notes/` and is tracked.
- `notes/DAYLIGHT_HYGIENE_QUEUE_2026-04-19.md` is tracked after the slice if it started untracked.
- `notes/specs/overnight_launcher_guardrail_spec.md` has a current-checkout banner.
- `notes/DAYLIGHT_HYGIENE_QUEUE_2026-04-19.md` has a current-checkout banner.
- `notes/CP9_ACTIVE_QUEUE.md` now describes the overnight/start branch truthfully.
- No `scripts/*.ps1` file changed.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D28: document overnight launcher missing-loop dependency
```

Tight equivalent is acceptable if it preserves the two core actions:
- add the investigation note
- repair the stale overnight truth surfaces

## Delegation hints

- Good `gpt-5.4 high` worker slice: read-only confirmation + note + narrow banners + queue update.
- If delegated, the worker should own the script inspection, the new note, the
  two banners, the queue update, the single commit, and a concise statement of
  what remains deferred after D28.

## Anti-recommendations

- Do **not** recreate `scripts/overnight_continue_loop.ps1`.
- Do **not** modify `start_overnight_continuation.ps1` or `run_overnight_queue_wrapped.ps1`.
- Do **not** broaden into tracking `start_fastembed_server.ps1` or `start_litert_host_server.ps1` here.
- Do **not** turn this into an overnight execution/recovery slice.

## Report format

- Commit sha + subject.
- Name/path of the new investigation note.
- Short summary of the missing-loop conclusions.
- Summary of the two historical-note banners and queue update.
- What remains deferred after D28.
