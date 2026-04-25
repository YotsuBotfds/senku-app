# Slice D32 - document harness-run stop device-reset behavior

- **Role:** main agent (`gpt-5.4 xhigh`). Investigation + truth-surface repair slice; safe to delegate to a `gpt-5.4 high` worker after D31 lands.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D31, the next process-control surface is `scripts/stop_android_harness_runs.ps1`, but it is not just host-side harness cleanup. In the current checkout it also force-stops Android packages and can `kill -9` package PIDs via adb, which makes it a device-reset helper as well as a host process stopper. The smallest truthful next move is to document that mixed behavior and update the queue, not to force a misleading track tranche.

## Outcome

One focused commit that:

1. Adds a tracked investigation note documenting the current-checkout host-stop plus device-reset behavior inside `scripts/stop_android_harness_runs.ps1`.
2. Updates the live queue so this script is described truthfully as a separate investigation-based follow-up, not a routine low-risk tracker candidate.
3. Does **not** modify any `scripts/*.ps1`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `481248b` (`D31: track Android harness orphan-stop helper`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. This file still exists for read-only inspection:
   - `scripts/stop_android_harness_runs.ps1`
4. Pre-existing unrelated dirt is expected and does **not** block:
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
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `scripts/stop_android_harness_runs.ps1`
  - `scripts/stop_android_harness_orphans.ps1`
  - `scripts/stop_android_device_processes_safe.ps1`
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - `TESTING_METHODOLOGY.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - refactor process-stop behavior
  - add or remove pattern matches
  - change adb/package-reset semantics
  - widen into a process-control implementation slice

## Investigation / edit rules

### Step 1 - confirm the mixed behavior read-only

Do a read-only inspection of:

- `scripts/stop_android_harness_runs.ps1`

Required conclusions to verify before writing:

1. The script matches a bounded set of host-side harness PowerShell runners by command line.
2. It also resolves adb target devices and force-stops `com.senku.mobile` / `com.senku.mobile.test`.
3. It can additionally `kill -9` matching package PIDs on the resolved devices.
4. Even when no matching host harness process is found, the script may still reset on-device package state if adb devices are present.
5. Those behaviors make this a mixed host-stop plus device-reset surface, unlike the narrower orphan-tree helper tracked in D31.

### Step 2 - add a new investigation note

Add one new tracked note under `notes/`. Suggested name:

```text
notes/STOP_ANDROID_HARNESS_RUNS_INVESTIGATION_20260423.md
```

The note should include:

1. A concise statement that `scripts/stop_android_harness_runs.ps1` is not purely a host-side harness cleanup helper in the current checkout.
2. Evidence:
   - host-side runner matching
   - adb device resolution
   - package force-stop / kill behavior
   - the fallback path where device reset can happen even with no matching host harness process
3. Current-checkout conclusion:
   - this script belongs to a mixed process-control / device-reset branch, not the narrow harness-orphan helper tranche
4. Recommended next step:
   - a future allow/repair/retire slice specifically for the mixed host-stop/device-reset behavior
   - keep the global device-process killer and recursive-delete artifact cleanup as separate surfaces

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the live queue minimally and truthfully:

1. Advance the `Last updated` line to include D32.
2. In item `(j)` or nearby carry-over wording, rewrite the `scripts/stop_android_harness_runs.ps1` entry so it no longer reads like a routine tracker candidate. It should state that the script is a mixed host-stop/device-reset follow-up.
3. Add a completed-log entry for D32 summarizing:
   - the script was audited read-only
   - a new investigation note was added
   - the mixed host-stop/device-reset boundary is now explicit in the queue
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- One new investigation note exists under `notes/` and is tracked.
- `notes/CP9_ACTIVE_QUEUE.md` now describes `scripts/stop_android_harness_runs.ps1` truthfully as a mixed host-stop/device-reset follow-up.
- No `scripts/*.ps1` file changed.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D32: document harness-run stop device-reset behavior
```

Tight equivalent is acceptable if it preserves the two core actions:
- add the investigation note
- repair the live queue wording for `scripts/stop_android_harness_runs.ps1`

## Delegation hints

- Good `gpt-5.4 high` worker slice: read-only confirmation + note + queue update.
- If delegated, the worker should own the script inspection, the new note, the
  queue update, the single commit, and a concise statement of why this script is
  no longer treated like a narrow host-side stop helper.

## Anti-recommendations

- Do **not** track `scripts/stop_android_harness_runs.ps1` in this slice.
- Do **not** change adb/package-reset behavior.
- Do **not** widen into `stop_android_device_processes_safe.ps1`.
- Do **not** widen into `cleanup_android_harness_artifacts.ps1`.
- Do **not** turn this into a process-control refactor.

## Report format

- Commit sha + subject.
- Name/path of the new investigation note.
- Short summary of the mixed host-stop/device-reset conclusions.
- Queue update summary.
- What remains open after D32.
