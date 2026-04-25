# Slice D25 - track the Qwen scout lane scripts

- **Role:** main agent (`gpt-5.4 xhigh`). Tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D24, the smallest coherent script family left is the Qwen scout lane. These three scripts parse clean, link directly to the already-tracked `start_qwen27_scout_job.ps1` / `get_qwen27_scout_job.ps1` lane, and avoid the much riskier process-control / cleanup bucket. The main trap is not safety but policy drift: the lane bakes in operator-local endpoint/model assumptions, especially the `192.168.0.88` default in `invoke_qwen_scout.ps1`. This slice should treat that as accepted local operator behavior, not a reason to widen into config cleanup.

## Outcome

One focused commit that:

1. Tracks these three currently untracked scripts:
   - `scripts/invoke_qwen27_scout.ps1`
   - `scripts/invoke_qwen_scout.ps1`
   - `scripts/run_qwen27_scout_job_worker.ps1`
2. Updates:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Narrows item `(j)` so the Qwen trio is no longer carried as deferred follow-up.

Important constraint: this is a track tranche, not a runtime-policy cleanup slice. Do **not** rewrite endpoint defaults, model defaults, or job metadata behavior.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `1d9b40196b8f76ead2dfd20af67753d177d6e7b6` (`D24: track logged matrix Android wrappers`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. These files still exist and are currently untracked:
   - `scripts/invoke_qwen27_scout.ps1`
   - `scripts/invoke_qwen_scout.ps1`
   - `scripts/run_qwen27_scout_job_worker.ps1`
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
  - `scripts/invoke_qwen27_scout.ps1`
  - `scripts/invoke_qwen_scout.ps1`
  - `scripts/run_qwen27_scout_job_worker.ps1`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `scripts/start_qwen27_scout_job.ps1`
  - `scripts/get_qwen27_scout_job.ps1`
  - any Android harness script
  - any `start_*` or `stop_*` process-control script outside this trio
  - any app source file
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - rewrite the endpoint defaults
  - normalize the model defaults
  - widen into swarm/runtime documentation cleanup
  - turn this into an environment-policy refactor

## Verify / track / edit rules

### Step 1 - narrow Rule-2b / HARD-STOP prepass on the Qwen trio

Before tracking anything, do a read-only prepass across the three target
scripts.

Required checks:

1. Read each file once.
2. Confirm PowerShell parse succeeds for all three.
3. Narrow HARD-STOP scan for:
   - embedded credentials / API keys / bearer tokens
   - bootstrap/download logic
   - destructive local filesystem behavior
   - broad process-kill / recursive delete behavior
4. Accept operator-local endpoint/model assumptions as local runtime config,
   not automatically as a blocker, unless you find actual secrets or obviously
   unsafe side effects.

If the prepass finds a real secret or a true HARD-STOP issue, STOP instead of
tracking the tranche.

### Step 2 - track the three scripts as-is

If the prepass is clean, track exactly these three files unchanged.

Bias hard toward no content edits. If you discover a real issue that would
require behavior or policy edits, STOP and report it instead of widening scope.

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D25.
2. In item `(j)`, close the three Qwen scout lane entries so they no longer
   appear as deferred follow-up.
3. Add a completed-log entry for D25 summarizing:
   - the Qwen trio passed the narrow Rule-2b / HARD-STOP prepass
   - they are now tracked unchanged
   - the broader process-control / orchestration bucket remains open elsewhere
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- All three targeted scripts are now tracked.
- Narrow Rule-2b / HARD-STOP prepass passed cleanly.
- `notes/CP9_ACTIVE_QUEUE.md` no longer lists those three scripts as deferred.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D25: track Qwen scout lane scripts
```

Tight equivalent is acceptable if it preserves the two core actions:
- track the Qwen trio
- narrow item `(j)` in the queue

## Delegation hints

- Good `gpt-5.4 high` worker slice: narrow prepass + track tranche + queue update.
- If delegated, the worker should own the read-only prepass, the tracking of the
  three scripts, the queue update, the single commit, and a concise note about
  what remains in the harder script buckets after D25.

## Anti-recommendations

- Do **not** rewrite `192.168.0.88` or endpoint policy here.
- Do **not** broaden into swarm docs or AGENTS edits.
- Do **not** widen into the process-control bucket.
- Do **not** edit the tracked Qwen job launcher/reader scripts.

## Report format

- Commit sha + subject.
- Result of the narrow Rule-2b / HARD-STOP prepass.
- Confirmation that all three scripts are now tracked unchanged.
- Short summary of the queue update.
- What remains in the harder script buckets after D25.
