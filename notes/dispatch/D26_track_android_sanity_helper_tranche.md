# Slice D26 - track the Android sanity-helper tranche

- **Role:** main agent (`gpt-5.4 xhigh`). Tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D25, the cleanest remaining script family is the non-destructive Android cleanup / artifact / sanity subset: `android_fts5_probe`, `launch_debug_detail_state`, `post_rebuild_sanity_check`, and `validate_mobile_pack_deterministic_parity`. They are real operator helpers without the recursive-delete and broad process-control risk carried by `cleanup_android_harness_artifacts.ps1` and the `stop_*` bucket.

## Outcome

One focused commit that:

1. Tracks these four currently untracked scripts:
   - `scripts/android_fts5_probe.ps1`
   - `scripts/launch_debug_detail_state.ps1`
   - `scripts/post_rebuild_sanity_check.ps1`
   - `scripts/validate_mobile_pack_deterministic_parity.ps1`
2. Updates:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Narrows item `(j)` so these four helpers are no longer carried as deferred follow-up.

Important constraint: this is a track tranche, not a cleanup/refactor slice. Do **not** pull in `scripts/cleanup_android_harness_artifacts.ps1`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `21ffa32cc9d6d0c331cc18bf6b9c0376687cb000` (`D25: track Qwen scout lane scripts`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. These files still exist and are currently untracked:
   - `scripts/android_fts5_probe.ps1`
   - `scripts/launch_debug_detail_state.ps1`
   - `scripts/post_rebuild_sanity_check.ps1`
   - `scripts/validate_mobile_pack_deterministic_parity.ps1`
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
  - `scripts/android_fts5_probe.ps1`
  - `scripts/launch_debug_detail_state.ps1`
  - `scripts/post_rebuild_sanity_check.ps1`
  - `scripts/validate_mobile_pack_deterministic_parity.ps1`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - any `stop_*` script
  - any Qwen scout script
  - any app source file
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - redesign helper behavior
  - broaden into runtime Android validation
  - widen into the recursive-delete or process-control bucket

## Verify / track / edit rules

### Step 1 - narrow Rule-2b / HARD-STOP prepass on the four helpers

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
4. Treat temporary file cleanup in `%TEMP%` and adb-invocation helpers as normal
   unless you find broader destructive scope.

If the prepass finds a real secret or a true HARD-STOP issue, STOP instead of
tracking the tranche.

### Step 2 - track the four scripts as-is

If the prepass is clean, track exactly these four files unchanged.

Bias hard toward no content edits. If you discover a real issue that would
require behavior changes, STOP and report it instead of widening scope.

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D26.
2. In item `(j)`, close the four helper entries so they no longer appear as
   deferred follow-up.
3. Add a completed-log entry for D26 summarizing:
   - the four sanity helpers passed the narrow Rule-2b / HARD-STOP prepass
   - they are now tracked unchanged
   - the recursive-delete/process-control bucket remains open elsewhere
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- All four targeted scripts are now tracked.
- Narrow Rule-2b / HARD-STOP prepass passed cleanly.
- `notes/CP9_ACTIVE_QUEUE.md` no longer lists those four helpers as deferred.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D26: track Android sanity helper tranche
```

Tight equivalent is acceptable if it preserves the two core actions:
- track the four helpers
- narrow item `(j)` in the queue

## Delegation hints

- Good `gpt-5.4 high` worker slice: narrow prepass + track tranche + queue update.
- If delegated, the worker should own the read-only prepass, the tracking of the
  four scripts, the queue update, the single commit, and a concise note about
  what remains in the higher-risk script buckets after D26.

## Anti-recommendations

- Do **not** pull in `cleanup_android_harness_artifacts.ps1`.
- Do **not** widen into `stop_*` or bootstrap scripts.
- Do **not** refactor these helpers in place.
- Do **not** broaden into runtime validation or test execution.

## Report format

- Commit sha + subject.
- Result of the narrow Rule-2b / HARD-STOP prepass.
- Confirmation that all four scripts are now tracked unchanged.
- Short summary of the queue update.
- What remains in the higher-risk script buckets after D26.
