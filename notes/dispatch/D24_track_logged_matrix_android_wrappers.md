# Slice D24 - track the logged/matrix Android wrapper tranche

- **Role:** main agent (`gpt-5.4 xhigh`). Tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D20 and D23, the cleanest continuation of the Android script lane is the logged/matrix wrapper tranche. These four wrappers sit directly on top of already-tracked core runners and have real operational linkage. They do contain targeted `Stop-Process -Force` conflict cleanup, so the slice must treat that as a bounded review point instead of assuming they are harmless by default.

## Outcome

One focused commit that:

1. Tracks these four currently untracked scripts:
   - `scripts/run_android_prompt_logged.ps1`
   - `scripts/run_android_detail_followup_logged.ps1`
   - `scripts/run_android_followup_matrix.ps1`
   - `scripts/run_android_detail_followup_matrix.ps1`
2. Updates:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Narrows item `(j)` so these four wrappers are no longer carried as deferred follow-up.

Important constraint: this is a track tranche, not a process-control refactor. Do **not** redesign the conflict-stop logic unless the narrow prepass finds a real blocker that makes tracking unsafe.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `dde0ed232edd4a0d1a0c63bbc6fe63072933c536` (`D23: track push helper and repair transport truth`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. These files still exist and are currently untracked:
   - `scripts/run_android_prompt_logged.ps1`
   - `scripts/run_android_detail_followup_logged.ps1`
   - `scripts/run_android_followup_matrix.ps1`
   - `scripts/run_android_detail_followup_matrix.ps1`
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
  - `scripts/run_android_prompt_logged.ps1`
  - `scripts/run_android_detail_followup_logged.ps1`
  - `scripts/run_android_followup_matrix.ps1`
  - `scripts/run_android_detail_followup_matrix.ps1`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `scripts/run_android_prompt.ps1`
  - `scripts/run_android_detail_followup.ps1`
  - `scripts/run_android_followup_suite.ps1`
  - `scripts/run_android_gap_pack.ps1`
  - any `stop_*` or cleanup script
  - any Qwen scout script
  - any app source file
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - redesign the process-conflict logic
  - widen into the broader process-control bucket
  - modify wrapper behavior unless a tiny parse/encoding fix is absolutely required

## Verify / track / edit rules

### Step 1 - narrow Rule-2b / HARD-STOP prepass on the four wrappers

Before tracking anything, do a read-only prepass across these four scripts.

Required checks:

1. Read each file once.
2. Confirm PowerShell parse succeeds for all four.
3. Narrow HARD-STOP scan for:
   - embedded credentials / API keys / bearer tokens
   - bootstrap/download logic
   - destructive local filesystem behavior
   - unbounded process-kill behavior
4. Treat the existing `Stop-ConflictingHarnessRuns` / `Stop-Process -Force`
   logic as acceptable only if it is still clearly scoped to conflicting harness
   processes on the same emulator and not a general cleanup tree.

If the prepass shows the stop logic is broader than that, STOP instead of
tracking the tranche.

### Step 2 - track the four wrappers as-is

If the prepass is clean, track exactly these four files with no content edits.

Bias hard toward tracking unchanged. If you discover a real safety blocker that
would require behavioral edits, STOP and report it rather than widening the
slice.

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D24.
2. In item `(j)`, close the four wrapper entries so they no longer appear as
   deferred follow-up.
3. Add a completed-log entry for D24 summarizing:
   - the four logged/matrix wrappers passed the narrow Rule-2b / HARD-STOP prepass
   - they are now tracked
   - the harder process-control / cleanup bucket still remains elsewhere in `(j)`
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- All four targeted scripts are now tracked.
- Narrow Rule-2b / HARD-STOP prepass passed cleanly.
- `notes/CP9_ACTIVE_QUEUE.md` no longer lists those four wrappers as deferred.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D24: track logged matrix Android wrappers
```

Tight equivalent is acceptable if it preserves the two core actions:
- track the four wrappers
- narrow item `(j)` in the queue

## Delegation hints

- Good `gpt-5.4 high` worker slice: narrow prepass + track tranche + queue update.
- If delegated, the worker should own the read-only prepass, the tracking of the
  four scripts, the queue update, the single commit, and a concise note about
  what remains in the harder process-control bucket.

## Anti-recommendations

- Do **not** redesign `Stop-ConflictingHarnessRuns` here.
- Do **not** widen into `stop_*` or cleanup scripts.
- Do **not** pull in Qwen scout tooling.
- Do **not** turn this into a runner refactor.

## Report format

- Commit sha + subject.
- Result of the narrow Rule-2b / HARD-STOP prepass.
- Confirmation that all four wrappers are now tracked unchanged.
- Short summary of the queue update.
- What remains in the harder process-control bucket after D24.
