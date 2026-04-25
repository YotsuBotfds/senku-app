# Slice D34 - document Android harness artifact cleanup path safety

- **Role:** main agent (`gpt-5.4 xhigh`). Investigation + truth-surface repair slice; safe to delegate to a `gpt-5.4 high` worker after D33 lands.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D33, the last remaining deferred process-control script is `scripts/cleanup_android_harness_artifacts.ps1`. In the current checkout it accepts caller-provided target paths and performs recursive delete on items older than a cutoff. That makes it a path-safety/retention-policy surface, not a routine low-risk helper, so the smallest truthful next move is an investigation/truth-surface slice.

## Outcome

One focused commit that:

1. Adds a tracked investigation note documenting the current-checkout path-safety and retention behavior inside `scripts/cleanup_android_harness_artifacts.ps1`.
2. Updates the live queue so this script is described truthfully as a recursive-delete/path-safety follow-up, not a routine tracker candidate.
3. Does **not** modify any `scripts/*.ps1`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `c75e5b6` (`D33: document global Android device-process stop behavior`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. This file still exists for read-only inspection:
   - `scripts/cleanup_android_harness_artifacts.ps1`
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
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - `scripts/stop_android_harness_orphans.ps1`
  - `scripts/stop_android_harness_runs.ps1`
  - `scripts/stop_android_device_processes_safe.ps1`
  - `TESTING_METHODOLOGY.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - change target-path handling
  - change retention semantics
  - refactor recursive-delete behavior

## Investigation / edit rules

### Step 1 - confirm the path-safety / recursive-delete behavior read-only

Do a read-only inspection of:

- `scripts/cleanup_android_harness_artifacts.ps1`

Required conclusions to verify before writing:

1. The script accepts target paths from the caller and resolves them either as absolute paths or relative to repo root.
2. The default target set is within repo artifact directories, but the script can also operate on caller-provided absolute paths.
3. The script uses `Remove-Item -Recurse -Force` on matching items older than the cutoff date.
4. `-WhatIf` only suppresses deletion when explicitly supplied.
5. That makes the script a recursive-delete/path-safety contract surface, not a routine lane-bounded helper.

### Step 2 - add a new investigation note

Add one new tracked note under `notes/`. Suggested name:

```text
notes/CLEANUP_ANDROID_HARNESS_ARTIFACTS_INVESTIGATION_20260423.md
```

The note should include:

1. A concise statement that `scripts/cleanup_android_harness_artifacts.ps1` is a path-safety and retention-policy helper in the current checkout.
2. Evidence:
   - caller-provided target path handling
   - absolute-path acceptance
   - recursive delete behavior
   - cutoff-date retention logic
   - `WhatIf` behavior
3. Current-checkout conclusion:
   - this script belongs to the recursive-delete/path-safety branch, not the routine Android helper tranche
4. Recommended next step:
   - a future allow/repair/retire slice specifically for the artifact cleanup contract

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the live queue minimally and truthfully:

1. Advance the `Last updated` line to include D34.
2. In item `(j)` or nearby carry-over wording, rewrite the `scripts/cleanup_android_harness_artifacts.ps1` entry so it no longer reads like a routine tracker candidate. It should state that the script is a recursive-delete/path-safety follow-up.
3. Add a completed-log entry for D34 summarizing:
   - the script was audited read-only
   - a new investigation note was added
   - the recursive-delete/path-safety boundary is now explicit in the queue
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- One new investigation note exists under `notes/` and is tracked.
- `notes/CP9_ACTIVE_QUEUE.md` now describes `scripts/cleanup_android_harness_artifacts.ps1` truthfully as a recursive-delete/path-safety follow-up.
- No `scripts/*.ps1` file changed.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D34: document Android harness artifact cleanup path safety
```

Tight equivalent is acceptable if it preserves the two core actions:
- add the investigation note
- repair the live queue wording for `scripts/cleanup_android_harness_artifacts.ps1`

## Delegation hints

- Good `gpt-5.4 high` worker slice: read-only confirmation + note + queue update.
- If delegated, the worker should own the script inspection, the new note, the
  queue update, the single commit, and a concise statement of why this script is
  a recursive-delete/path-safety surface rather than a routine helper.

## Anti-recommendations

- Do **not** track `scripts/cleanup_android_harness_artifacts.ps1` in this slice.
- Do **not** change target-path handling or retention semantics.
- Do **not** turn this into a cleanup refactor.

## Report format

- Commit sha + subject.
- Name/path of the new investigation note.
- Short summary of the path-safety / recursive-delete conclusions.
- Queue update summary.
- What remains open after D34.
