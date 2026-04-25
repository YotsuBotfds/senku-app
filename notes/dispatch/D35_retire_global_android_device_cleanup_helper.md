# Slice D35 - retire the global Android device cleanup helper

- **Role:** main agent (`gpt-5.4 xhigh`). Narrow allow/repair/retire slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D34, `scripts/stop_android_device_processes_safe.ps1` is the cleanest substantive branch left. D33 already truth-surfaced that the helper is a host-global Android process killer keyed only by raw process name, and current repo evidence shows no live tracked operator doc recommending it. In this checkout, retirement is cleaner than blessing or refactoring a global emergency kill-switch with no active anchored workflow.

## Outcome

One focused commit that:

1. Retires the untracked global cleanup helper by deleting:
   - `scripts/stop_android_device_processes_safe.ps1`
2. Updates:
   - `notes/STOP_ANDROID_DEVICE_PROCESSES_SAFE_INVESTIGATION_20260423.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Closes the D33 follow-up branch as retired/removed rather than keeping it as an open allow/repair/retire candidate.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `5669b49fee12cd986f6c238df7722ef447c76e0c` (`D34: document Android harness artifact cleanup path safety`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `notes/STOP_ANDROID_DEVICE_PROCESSES_SAFE_INVESTIGATION_20260423.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
3. This script still exists and is currently untracked:
   - `scripts/stop_android_device_processes_safe.ps1`
4. Read-only repo checks still show no live tracked operator doc instructing routine use of this helper. Historical / untracked notes may mention it, but those do **not** count as a live tracked anchor.
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
  - `scripts/stop_android_device_processes_safe.ps1` (delete)
  - `notes/STOP_ANDROID_DEVICE_PROCESSES_SAFE_INVESTIGATION_20260423.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `scripts/stop_android_harness_orphans.ps1`
  - `scripts/stop_android_harness_runs.ps1`
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - `TESTING_METHODOLOGY.md`
  - `android-app/README.md`
  - `notes/ACTIVE_WORK_LOG_20260412.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - refactor the helper instead of making the disposition
  - widen into general Android process-control policy
  - edit historical untracked notes just to remove mentions

## Investigation / edit rules

### Step 1 - reconfirm the retirement case read-only

Before deleting anything, do a quick read-only reconfirmation:

1. `scripts/stop_android_device_processes_safe.ps1` still matches host processes only by raw process name and force-stops them globally.
2. The helper still has no live tracked operator anchor in current workflow docs.
3. The narrower tracked helper `scripts/stop_android_harness_orphans.ps1` remains available for lane-bounded harness cleanup, so deleting this global helper does not remove the only cleanup path.

If any of those three conditions fail, STOP instead of retiring the script.

### Step 2 - retire the helper

Delete `scripts/stop_android_device_processes_safe.ps1`.

Do **not** replace it in this slice. The point here is disposition, not a redesigned emergency-cleanup interface.

### Step 3 - update the investigation note

Update `notes/STOP_ANDROID_DEVICE_PROCESSES_SAFE_INVESTIGATION_20260423.md`
minimally so it now records:

1. The original D33 truth-surface findings remain the basis for the decision.
2. As of this slice, the helper was retired from the repo rather than tracked.
3. Why:
   - host-global raw-process kill scope
   - no live tracked operator anchor
   - narrower tracked harness cleanup exists elsewhere
4. If a future global Android cleanup helper is needed, it should be reintroduced only via a new explicit contract, not by silently restoring this file.

### Step 4 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D35.
2. In carry-over item `(j)`, close the `scripts/stop_android_device_processes_safe.ps1` entry as retired/removed.
3. Add a completed-log entry for D35 summarizing:
   - D33 findings were reconfirmed
   - the untracked helper was retired instead of tracked
   - the narrower harness helper remains the live tracked cleanup surface
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- `scripts/stop_android_device_processes_safe.ps1` is deleted.
- `notes/STOP_ANDROID_DEVICE_PROCESSES_SAFE_INVESTIGATION_20260423.md` records the retirement disposition.
- `notes/CP9_ACTIVE_QUEUE.md` no longer carries the helper as an open allow/repair/retire branch.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D35: retire global Android device cleanup helper
```

Tight equivalent is acceptable if it preserves the two core actions:
- retire the helper
- close the queue branch

## Delegation hints

- Good `gpt-5.4 high` worker slice: reconfirmation + delete + note update + queue update.
- If delegated, the worker should own the reconfirmation, the file deletion,
  the investigation-note update, the queue update, the single commit, and a
  concise statement of what Android cleanup/process-control branches still
  remain after D35.

## Anti-recommendations

- Do **not** redesign the helper in this slice.
- Do **not** widen into `stop_android_harness_runs.ps1`.
- Do **not** widen into `cleanup_android_harness_artifacts.ps1`.
- Do **not** treat untracked historical notes as a reason to keep the script alive.

## Report format

- Commit sha + subject.
- Short summary of the reconfirmed retirement case.
- Confirmation that the helper was deleted.
- Investigation-note update summary.
- Queue update summary.
- What Android cleanup/process-control branches remain after D35.
