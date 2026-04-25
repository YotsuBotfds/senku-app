# Slice D29 - track the FastEmbed host launcher

- **Role:** main agent (`gpt-5.4 xhigh`). Tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D28, `scripts/start_fastembed_server.ps1` is the smallest unblocked launcher left in the deferred PowerShell bucket. It is materially narrower and lower-risk than `scripts/start_litert_host_server.ps1`, which bundles bootstrap/download behavior, and lower-risk than the recursive-delete/process-control bucket.

## Outcome

One focused commit that:

1. Tracks this currently untracked script:
   - `scripts/start_fastembed_server.ps1`
2. Updates:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Narrows item `(j)` so `scripts/start_fastembed_server.ps1` no longer appears as deferred follow-up.

Important constraint: this is a track tranche, not a FastEmbed runtime-repair or guide-validation refactor. Do **not** widen into `scripts/fastembed_openai_server.py`, guide-validation wrappers, or historical guide-validation notes.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `f4f2bee85f33728fa12809b8d33b482585ec810a` (`D28: document overnight launcher missing-loop dependency`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. This file still exists and is currently untracked:
   - `scripts/start_fastembed_server.ps1`
4. This sibling script exists for read-only confirmation:
   - `scripts/fastembed_openai_server.py`
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
  - `scripts/start_fastembed_server.ps1`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `scripts/fastembed_openai_server.py`
  - `scripts/start_litert_host_server.ps1`
  - `scripts/start_overnight_continuation.ps1`
  - any `stop_*` script
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - any app source file
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - repair or refactor FastEmbed runtime behavior
  - change the default `PythonPath`
  - change bind host / port defaults
  - widen into guide-validation runner cleanup
  - widen into the LiteRT host/bootstrap lane

## Verify / track / edit rules

### Step 1 - narrow Rule-2b / HARD-STOP prepass on the launcher

Before tracking anything, do a read-only prepass across:

- `scripts/start_fastembed_server.ps1`
- `scripts/fastembed_openai_server.py` (read-only context only)

Required checks:

1. Read the PowerShell launcher once.
2. Confirm PowerShell parse succeeds.
3. Narrow HARD-STOP scan for:
   - embedded credentials / API keys / bearer tokens
   - bootstrap/download logic
   - destructive local filesystem behavior
   - broad process-kill / recursive delete behavior
4. Treat normal operator/runtime surfaces as acceptable unless they hide a broader risk:
   - localhost bind defaults
   - model-id strings
   - explicit server-script path resolution
   - a local default `PythonPath`
5. Confirm `scripts/fastembed_openai_server.py` exists so the launcher target is real in this checkout.

If the prepass finds a real secret or a true HARD-STOP issue, STOP instead of
tracking the slice.

### Step 2 - cross-reference judgment

Confirm there is tracked repo context that justifies promotion. Read-only confirmation is enough; do not edit the supporting docs.

Expected shape:

1. At least one tracked note/log references `scripts/start_fastembed_server.ps1` as an operator/runtime surface.
2. The launcher is not implicated in the D28 overnight missing-loop dependency.

### Step 3 - track the launcher as-is

If the prepass is clean and the cross-reference check supports promotion, track
`scripts/start_fastembed_server.ps1` unchanged.

Bias hard toward no content edits. If you discover a real issue that would
require behavior changes, STOP and report it instead of widening scope.

### Step 4 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D29.
2. In item `(j)`, close the `scripts/start_fastembed_server.ps1` entry so it no
   longer appears as deferred follow-up.
3. Add a completed-log entry for D29 summarizing:
   - the launcher passed the narrow Rule-2b / HARD-STOP prepass
   - it is now tracked unchanged
   - `scripts/start_litert_host_server.ps1` remains separate because the LiteRT host/bootstrap lane is still a distinct follow-up
   - the recursive-delete/process-control bucket remains open
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- `scripts/start_fastembed_server.ps1` is now tracked.
- Narrow Rule-2b / HARD-STOP prepass passed cleanly.
- `notes/CP9_ACTIVE_QUEUE.md` no longer lists `scripts/start_fastembed_server.ps1` as deferred.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D29: track FastEmbed host launcher
```

Tight equivalent is acceptable if it preserves the two core actions:
- track the FastEmbed launcher
- narrow item `(j)` in the queue

## Delegation hints

- Good `gpt-5.4 high` worker slice: narrow prepass + track launcher + queue update.
- If delegated, the worker should own the read-only prepass, the tracking of
  `scripts/start_fastembed_server.ps1`, the queue update, the single commit, and
  a concise note about what remains open in the LiteRT host/bootstrap and
  process-control buckets after D29.

## Anti-recommendations

- Do **not** refactor `scripts/fastembed_openai_server.py`.
- Do **not** change the default `PythonPath`.
- Do **not** widen into `scripts/start_litert_host_server.ps1`.
- Do **not** widen into `cleanup_android_harness_artifacts.ps1` or `stop_*`.
- Do **not** turn this into a FastEmbed execution/debugging slice.

## Report format

- Commit sha + subject.
- Result of the narrow Rule-2b / HARD-STOP prepass.
- Confirmation that `scripts/start_fastembed_server.ps1` is now tracked unchanged.
- Short summary of the queue update.
- What remains open in the LiteRT host/bootstrap and process-control buckets after D29.
