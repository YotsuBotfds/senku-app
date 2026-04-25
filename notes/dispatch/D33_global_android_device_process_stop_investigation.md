# Slice D33 - document global Android device-process stop behavior

- **Role:** main agent (`gpt-5.4 xhigh`). Investigation + truth-surface repair slice; safe to delegate to a `gpt-5.4 high` worker after D32 lands.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D32, `scripts/stop_android_device_processes_safe.ps1` is the remaining standalone process-kill helper before the recursive-delete cleanup surface. In the current checkout it globally stops Android device/emulator processes by executable name. That makes it an emergency operator cleanup surface, not normal harness-lane hygiene, so the smallest truthful next move is an investigation/truth-surface slice.

## Outcome

One focused commit that:

1. Adds a tracked investigation note documenting the current-checkout global process-stop behavior inside `scripts/stop_android_device_processes_safe.ps1`.
2. Updates the live queue so this script is described truthfully as a global Android device/emulator cleanup follow-up, not a routine low-risk tracker candidate.
3. Does **not** modify any `scripts/*.ps1`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `59e9bd4` (`D32: document harness-run stop device-reset behavior`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. This file still exists for read-only inspection:
   - `scripts/stop_android_device_processes_safe.ps1`
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
  - `scripts/stop_android_device_processes_safe.ps1`
  - `scripts/stop_android_harness_orphans.ps1`
  - `scripts/stop_android_harness_runs.ps1`
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - `TESTING_METHODOLOGY.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - refactor process-stop behavior
  - change the target process-name set
  - widen into recursive-delete cleanup policy

## Investigation / edit rules

### Step 1 - confirm the global kill scope read-only

Do a read-only inspection of:

- `scripts/stop_android_device_processes_safe.ps1`

Required conclusions to verify before writing:

1. The script targets Android device/emulator process names globally, including `adb`, `emulator`, and `qemu-system-*`.
2. The script uses `Stop-Process -Force` against those matching processes.
3. The script intentionally does **not** target `node`.
4. The behavior is global host cleanup, not harness-tree cleanup tied to a single lane or emulator subset.
5. That makes it a separate emergency operator cleanup surface, unlike the narrower harness-orphan helper from D31.

### Step 2 - add a new investigation note

Add one new tracked note under `notes/`. Suggested name:

```text
notes/STOP_ANDROID_DEVICE_PROCESSES_SAFE_INVESTIGATION_20260423.md
```

The note should include:

1. A concise statement that `scripts/stop_android_device_processes_safe.ps1` is a global device/emulator process-stop helper in the current checkout.
2. Evidence:
   - targeted process-name set
   - global host-side `Stop-Process -Force` behavior
   - explicit exclusion of `node`
3. Current-checkout conclusion:
   - this script belongs to the emergency Android device/emulator cleanup branch, not the narrow harness-helper tranche
4. Recommended next step:
   - a future allow/repair/retire slice specifically for this global device-process stop helper
   - keep `cleanup_android_harness_artifacts.ps1` separate as the recursive-delete contract slice

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the live queue minimally and truthfully:

1. Advance the `Last updated` line to include D33.
2. In item `(j)` or nearby carry-over wording, rewrite the `scripts/stop_android_device_processes_safe.ps1` entry so it no longer reads like a routine tracker candidate. It should state that the script is a global Android device/emulator cleanup follow-up.
3. Add a completed-log entry for D33 summarizing:
   - the script was audited read-only
   - a new investigation note was added
   - the global cleanup boundary is now explicit in the queue
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- One new investigation note exists under `notes/` and is tracked.
- `notes/CP9_ACTIVE_QUEUE.md` now describes `scripts/stop_android_device_processes_safe.ps1` truthfully as a global Android device/emulator cleanup follow-up.
- No `scripts/*.ps1` file changed.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D33: document global Android device-process stop behavior
```

Tight equivalent is acceptable if it preserves the two core actions:
- add the investigation note
- repair the live queue wording for `scripts/stop_android_device_processes_safe.ps1`

## Delegation hints

- Good `gpt-5.4 high` worker slice: read-only confirmation + note + queue update.
- If delegated, the worker should own the script inspection, the new note, the
  queue update, the single commit, and a concise statement of why this script is
  separate from the narrower harness-helper lane.

## Anti-recommendations

- Do **not** track `scripts/stop_android_device_processes_safe.ps1` in this slice.
- Do **not** change the process-name set.
- Do **not** widen into `cleanup_android_harness_artifacts.ps1`.
- Do **not** turn this into a process-control refactor.

## Report format

- Commit sha + subject.
- Name/path of the new investigation note.
- Short summary of the global cleanup conclusions.
- Queue update summary.
- What remains open after D33.
