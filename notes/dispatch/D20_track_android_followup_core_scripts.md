# Slice D20 - track the non-destructive Android follow-up core scripts

- **Role:** main agent (`gpt-5.4 xhigh`). Execution/tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`. Read-only scouting elsewhere is fine.
- **Why this slice now:** The full non-AGENTS PowerShell backlog is too wide for one pass. The cleanest first tranche is the non-destructive Android follow-up core: `run_android_detail_followup.ps1`, `run_android_followup_suite.ps1`, `run_android_gap_pack.ps1`, and `run_android_session_batch.ps1`. They sit in one operational lane, have real anchors outside tracker-only surfaces, and avoid the process-kill / cleanup / server-bootstrap scripts that would widen this into a harder safety review.

## Outcome

One focused commit that:

1. Tracks these four currently untracked PowerShell scripts:
   - `scripts/run_android_detail_followup.ps1`
   - `scripts/run_android_followup_suite.ps1`
   - `scripts/run_android_gap_pack.ps1`
   - `scripts/run_android_session_batch.ps1`
2. Updates:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Narrows carry-over item `(j)` so these four are no longer listed as deferred follow-up.

Important constraint: this is a track-only promotion slice for the non-destructive Android follow-up core. Do **not** widen into the logged/matrix wrappers, kill/cleanup scripts, Qwen scout scripts, host-server bootstraps, or `push_litert_model_to_android.ps1`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `3b9be46` (`D19: delete orphan encoding helper scripts`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. These files still exist and are currently untracked:
   - `scripts/run_android_detail_followup.ps1`
   - `scripts/run_android_followup_suite.ps1`
   - `scripts/run_android_gap_pack.ps1`
   - `scripts/run_android_session_batch.ps1`
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
  - `scripts/run_android_detail_followup.ps1`
  - `scripts/run_android_followup_suite.ps1`
  - `scripts/run_android_gap_pack.ps1`
  - `scripts/run_android_session_batch.ps1`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `scripts/run_android_prompt_logged.ps1`
  - `scripts/run_android_detail_followup_logged.ps1`
  - `scripts/run_android_followup_matrix.ps1`
  - `scripts/run_android_detail_followup_matrix.ps1`
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - `scripts/stop_android_device_processes_safe.ps1`
  - `scripts/stop_android_harness_orphans.ps1`
  - `scripts/stop_android_harness_runs.ps1`
  - `scripts/push_litert_model_to_android.ps1`
  - `scripts/invoke_qwen_scout.ps1`
  - `scripts/invoke_qwen27_scout.ps1`
  - `scripts/run_qwen27_scout_job_worker.ps1`
  - any file under `guides/`
  - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - broaden into the rest of item `(j)`
  - edit script behavior unless a tiny safety/parse issue absolutely forces it
  - count tracker/hygiene-report mentions as sufficient operational evidence by themselves

## Verify / track / edit rules

### Step 1 - run the Rule-2b / HARD-STOP prepass on just these four scripts

Before tracking anything, do a read-only prepass across the four targeted
scripts.

Required checks:

1. Read each file once.
2. Secret-scan / HARD-STOP scan for obvious unsafe surfaces:
   - embedded credentials / API keys / bearer tokens
   - destructive recursive delete patterns
   - force-kill process trees
   - download/install/bootstrap behavior
   - environment-specific private hostnames beyond the expected emulator host
3. Verify PowerShell syntax parses cleanly for each target script.
4. Verify at least one real non-tracker operational anchor for the tranche:
   - `TESTING_METHODOLOGY.md`
   - script-to-script orchestration within the tranche
   - existing tracked runner relationships such as `build_android_ui_state_pack.ps1` -> `run_android_detail_followup.ps1`

If any targeted script fails syntax parse, contains a real secret, or turns out
to belong in the harder kill/cleanup/bootstrap buckets, STOP instead of
tracking it.

Expected note: emulator host inference URLs like `http://10.0.2.2:1235/v1` are
normal for this lane and are **not** by themselves a stop condition.

### Step 2 - track the four scripts without widening scope

If the prepass is clean, stage and track exactly these four files.

Bias toward no content edits. If you discover a problem that would require
substantive script changes, STOP and kick the slice back rather than widening
it into implementation work.

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D20.
2. In carry-over item `(j)`, remove or strike the four targeted scripts so they
   are no longer presented as deferred follow-up.
3. Keep the rest of `(j)` intact for the remaining scripts.
4. Append a completed-log entry for D20 summarizing:
   - the four non-destructive Android follow-up core scripts passed the narrow
     Rule-2b/HARD-STOP prepass
   - they are now tracked
   - the remaining PowerShell follow-up still exists for the harder buckets
5. Do **not** reshuffle unrelated backlog items.

## Commit

Single commit only. Suggested subject:

```text
D20: track Android follow-up core scripts
```

Tight equivalent is acceptable if it preserves the two core actions:
- track the four Android follow-up core scripts
- narrow item `(j)` in the queue

## Acceptance

- One commit only.
- All four targeted scripts are now tracked.
- The Rule-2b/HARD-STOP prepass on the four targeted scripts completed cleanly.
- `notes/CP9_ACTIVE_QUEUE.md` no longer lists those four scripts as deferred in item `(j)`.
- No file outside the explicit touch set changed.

## Delegation hints

- Good `gpt-5.4 high` worker slice: narrow safety/parse prepass plus track-only promotion.
- If delegated, the worker should own the read-only prepass, the tracking of the
  four scripts, the queue update, the single commit, and a concise note about
  what remains in the harder PowerShell buckets.

## Anti-recommendations

- Do **not** touch the logged/matrix wrappers in this slice.
- Do **not** touch any `stop_*` or cleanup script in this slice.
- Do **not** broaden into Qwen scout or LiteRT host-server tooling.
- Do **not** turn this into a content-edit refactor unless a real blocker forces a stop.

## Report format

- Commit sha + subject.
- Result of the Rule-2b/HARD-STOP prepass.
- Confirmation that all four scripts are now tracked.
- Short summary of the queue update.
- What remains in the harder PowerShell follow-up buckets after D20.
