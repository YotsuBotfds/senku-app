# Slice D31 - track the Android harness orphan-stop helper

- **Role:** main agent (`gpt-5.4 xhigh`). Tracking slice; safe to delegate to a `gpt-5.4 high` worker after D30 lands.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** Inside the remaining process-control bucket, `scripts/stop_android_harness_orphans.ps1` is the cleanest first tranche. It has a live tracked operational anchor in `TESTING_METHODOLOGY.md`, and its scope is bounded to stale Android harness trees rather than global device/emulator cleanup or filesystem retention policy.

## Outcome

One focused commit that:

1. Tracks this currently untracked script:
   - `scripts/stop_android_harness_orphans.ps1`
2. Updates:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Narrows item `(j)` so `scripts/stop_android_harness_orphans.ps1` no longer appears as deferred follow-up.

Important constraint: this is a narrow track tranche, not a process-control redesign. Do **not** widen into the broader harness-run stop script, the global Android device-process killer, or the recursive-delete artifact cleanup helper.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `cc57b29` (`D30: document LiteRT host launcher bootstrap assumptions`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. This file still exists and is currently untracked:
   - `scripts/stop_android_harness_orphans.ps1`
4. This tracked doc still contains the live operator anchor:
   - `TESTING_METHODOLOGY.md`
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
  - `scripts/stop_android_harness_orphans.ps1`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `scripts/stop_android_harness_runs.ps1`
  - `scripts/stop_android_device_processes_safe.ps1`
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - any `start_*` launcher
  - any app source file
  - `TESTING_METHODOLOGY.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - broaden the harness-pattern list
  - refactor the descendant-stop semantics
  - widen into global device cleanup or recursive-delete behavior

## Verify / track / edit rules

### Step 1 - narrow Rule-2b / HARD-STOP prepass on the helper

Before tracking anything, do a read-only prepass on
`scripts/stop_android_harness_orphans.ps1`.

Required checks:

1. Read the script once.
2. Confirm PowerShell parse succeeds.
3. Narrow HARD-STOP scan for:
   - embedded credentials / API keys / bearer tokens
   - bootstrap/download logic
   - destructive local filesystem behavior
   - unexpectedly broad process-kill behavior outside the harness-tree scope
4. Verify the intended scope is still bounded:
   - target roots are identified by the explicit Android harness command-line patterns
   - descendant collection is limited to those harness roots
   - the script does **not** contain adb package force-stop behavior

If the prepass finds a real secret, bootstrap/download behavior, destructive
filesystem behavior, adb/device-state reset behavior, or a broader-than-expected
kill scope, STOP instead of tracking the slice.

### Step 2 - cross-reference judgment

Confirm there is tracked repo context that justifies promotion. Read-only confirmation is enough; do not edit the supporting docs.

Expected shape:

1. `TESTING_METHODOLOGY.md` still directs operators to use `scripts/stop_android_harness_orphans.ps1` before rerunning interrupted harness lanes.
2. The script’s behavior still matches that tracked operator guidance.

### Step 3 - track the helper as-is

If the prepass is clean and the cross-reference check supports promotion, track
`scripts/stop_android_harness_orphans.ps1` unchanged.

Bias hard toward no content edits. If you discover a real issue that would
require behavior changes, STOP and report it instead of widening scope.

### Step 4 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D31.
2. In item `(j)`, close the `scripts/stop_android_harness_orphans.ps1`
   entry so it no longer appears as deferred follow-up.
3. Add a completed-log entry for D31 summarizing:
   - the helper passed the narrow Rule-2b / HARD-STOP prepass
   - it is now tracked unchanged
   - `stop_android_harness_runs.ps1`, `stop_android_device_processes_safe.ps1`, and `cleanup_android_harness_artifacts.ps1` remain separate follow-up surfaces
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- `scripts/stop_android_harness_orphans.ps1` is now tracked.
- Narrow Rule-2b / HARD-STOP prepass passed cleanly.
- `notes/CP9_ACTIVE_QUEUE.md` no longer lists `scripts/stop_android_harness_orphans.ps1` as deferred.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D31: track Android harness orphan-stop helper
```

Tight equivalent is acceptable if it preserves the two core actions:
- track the harness orphan-stop helper
- narrow item `(j)` in the queue

## Delegation hints

- Good `gpt-5.4 high` worker slice: narrow prepass + track helper + queue update.
- If delegated, the worker should own the read-only prepass, the tracking of
  `scripts/stop_android_harness_orphans.ps1`, the queue update, the single
  commit, and a concise note about what remains open in the broader
  process-control bucket after D31.

## Anti-recommendations

- Do **not** widen into `stop_android_harness_runs.ps1`.
- Do **not** widen into `stop_android_device_processes_safe.ps1`.
- Do **not** widen into `cleanup_android_harness_artifacts.ps1`.
- Do **not** broaden the harness-pattern set.
- Do **not** turn this into a process-control redesign slice.

## Report format

- Commit sha + subject.
- Result of the narrow Rule-2b / HARD-STOP prepass.
- Confirmation that `scripts/stop_android_harness_orphans.ps1` is now tracked unchanged.
- Short summary of the queue update.
- What remains open in the broader process-control bucket after D31.
