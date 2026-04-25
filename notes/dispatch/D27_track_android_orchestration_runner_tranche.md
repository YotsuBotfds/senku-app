# Slice D27 - track the Android orchestration runner tranche

- **Role:** main agent (`gpt-5.4 xhigh`). Tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D26, the last coherent non-destructive Android operator family is the orchestration runner tranche. These four scripts mainly coordinate already-tracked runners, manifests, logs, jobs, and artifact directories. They avoid the higher-risk host/bootstrap and recursive-delete/process-control buckets.

## Outcome

One focused commit that:

1. Tracks these four currently untracked scripts:
   - `scripts/run_android_harness_matrix.ps1`
   - `scripts/run_android_prompt_batch.ps1`
   - `scripts/run_e2b_e4b_diff.ps1`
   - `scripts/start_android_detail_followup_lane.ps1`
2. Updates:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Narrows item `(j)` so these four orchestration runners are no longer carried as deferred follow-up.

Important constraint: this is a track tranche, not a validation-pack or host-server refactor. Do **not** widen into the model-lane semantics behind `run_android_ui_validation_pack.ps1`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `7904c548ccdaec639195f23ce64bb67626abcee5` (`D26: track Android sanity helper tranche`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. These files still exist and are currently untracked:
   - `scripts/run_android_harness_matrix.ps1`
   - `scripts/run_android_prompt_batch.ps1`
   - `scripts/run_e2b_e4b_diff.ps1`
   - `scripts/start_android_detail_followup_lane.ps1`
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
  - `scripts/run_android_harness_matrix.ps1`
  - `scripts/run_android_prompt_batch.ps1`
  - `scripts/run_e2b_e4b_diff.ps1`
  - `scripts/start_android_detail_followup_lane.ps1`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `scripts/run_android_ui_validation_pack.ps1`
  - `scripts/start_litert_host_server.ps1`
  - any `stop_*` script
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - any app source file
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - refactor validation-pack semantics
  - change host-inference URLs or model defaults
  - widen into host/bootstrap/start or process-control buckets

## Verify / track / edit rules

### Step 1 - narrow Rule-2b / HARD-STOP prepass on the four orchestration scripts

Before tracking anything, do a read-only prepass across the four target
scripts.

Required checks:

1. Read each file once.
2. Confirm PowerShell parse succeeds for all four.
3. Narrow HARD-STOP scan for:
   - embedded credentials / API keys / bearer tokens
   - bootstrap/download logic
   - destructive local filesystem behavior
   - broad process-kill / recursive delete behavior
4. Treat temp-file cleanup and host-inference URLs like `10.0.2.2` as normal
   operator/runtime surfaces unless you find broader risk.

If the prepass finds a real secret or a true HARD-STOP issue, STOP instead of
tracking the tranche.

### Step 2 - track the four scripts as-is

If the prepass is clean, track exactly these four files unchanged.

Bias hard toward no content edits. If you discover a real issue that would
require behavior changes, STOP and report it instead of widening scope.

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D27.
2. In item `(j)`, close the four orchestration entries so they no longer appear
   as deferred follow-up.
3. Add a completed-log entry for D27 summarizing:
   - the four orchestration runners passed the narrow Rule-2b / HARD-STOP prepass
   - they are now tracked unchanged
   - the host/bootstrap and recursive-delete/process-control buckets remain open
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- All four targeted scripts are now tracked.
- Narrow Rule-2b / HARD-STOP prepass passed cleanly.
- `notes/CP9_ACTIVE_QUEUE.md` no longer lists those four orchestration runners as deferred.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D27: track Android orchestration runner tranche
```

Tight equivalent is acceptable if it preserves the two core actions:
- track the four orchestration runners
- narrow item `(j)` in the queue

## Delegation hints

- Good `gpt-5.4 high` worker slice: narrow prepass + track tranche + queue update.
- If delegated, the worker should own the read-only prepass, the tracking of the
  four scripts, the queue update, the single commit, and a concise note about
  what remains in the host/bootstrap and process-control buckets after D27.

## Anti-recommendations

- Do **not** refactor validation-pack behavior here.
- Do **not** widen into host/bootstrap/start lanes.
- Do **not** widen into `cleanup_android_harness_artifacts.ps1` or `stop_*`.
- Do **not** change model-lane semantics.

## Report format

- Commit sha + subject.
- Result of the narrow Rule-2b / HARD-STOP prepass.
- Confirmation that all four scripts are now tracked unchanged.
- Short summary of the queue update.
- What remains in the host/bootstrap and process-control buckets after D27.
