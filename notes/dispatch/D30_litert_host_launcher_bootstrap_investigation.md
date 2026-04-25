# Slice D30 - document LiteRT host launcher bootstrap assumptions

- **Role:** main agent (`gpt-5.4 xhigh`). Investigation + truth-surface repair slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D29, the remaining non-process-control launcher is `scripts/start_litert_host_server.ps1`, but it is not a plain launcher. In this checkout it bundles LiteRT binary bootstrap/download behavior, local DLL-copy assumptions, and model auto-discovery. The smallest truthful next move is to document those assumptions and update the live queue, not to force a misleading “track unchanged” tranche.

## Outcome

One focused commit that:

1. Adds a tracked investigation note documenting the current-checkout bootstrap/runtime assumptions inside `scripts/start_litert_host_server.ps1`.
2. Updates the live queue so the LiteRT host launcher is described truthfully as a separate bootstrap/runtime follow-up, not a routine low-risk track tranche.
3. Does **not** modify any `scripts/*.ps1`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `5de3fe68f2858d2f5290387b56330f054cb5f999` (`D29: track FastEmbed host launcher`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. These files still exist for read-only inspection:
   - `scripts/start_litert_host_server.ps1`
   - `scripts/litert_native_openai_server.py`
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
  - `scripts/start_litert_host_server.ps1`
  - `scripts/litert_native_openai_server.py`
  - `scripts/start_fastembed_server.ps1`
  - `scripts/start_overnight_continuation.ps1`
  - any `stop_*` script
  - `scripts/cleanup_android_harness_artifacts.ps1`
  - `TESTING_METHODOLOGY.md`
  - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - repair LiteRT launcher behavior
  - change transport truth from D22/D23
  - download binaries
  - turn this into a bootstrap refactor or host-server implementation slice

## Investigation / edit rules

### Step 1 - confirm the launcher assumptions read-only

Do a read-only inspection of:

- `scripts/start_litert_host_server.ps1`
- `scripts/litert_native_openai_server.py`

Required conclusions to verify before writing:

1. `start_litert_host_server.ps1` resolves the tracked Python server script and launches it via Python.
2. The PowerShell launcher includes LiteRT binary bootstrap/download logic, including `Invoke-WebRequest` against the LiteRT release URL when the binary is absent.
3. The launcher also contains local-environment assumptions beyond a pure launcher role, such as DLL-copy attempts and model auto-discovery from repo/home paths.
4. These assumptions make it a separate bootstrap/runtime surface, unlike the narrower `start_fastembed_server.ps1` launcher.
5. This slice does **not** revisit D22/D23 transport truth; the LiteRT push-helper byte-safety issue remains separate.

### Step 2 - add a new investigation note

Add one new tracked note under `notes/`. Suggested name:

```text
notes/LITERT_HOST_LAUNCHER_BOOTSTRAP_INVESTIGATION_20260423.md
```

The note should include:

1. A concise statement that `scripts/start_litert_host_server.ps1` is not just a thin launcher in the current checkout.
2. Evidence:
   - bootstrap/download behavior
   - local DLL-copy assumptions
   - model auto-discovery behavior
   - linkage to `scripts/litert_native_openai_server.py`
3. Current-checkout conclusion:
   - this script belongs to the LiteRT host/bootstrap runtime branch, not the low-risk operator-wrapper tranche
4. Recommended next step:
   - a future allow/repair/retire slice specifically for the LiteRT host launcher and its bootstrap assumptions
   - keep the recursive-delete/process-control bucket separate

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the live queue minimally and truthfully:

1. Advance the `Last updated` line to include D30.
2. In item `(j)` or nearby carry-over wording, rewrite the `scripts/start_litert_host_server.ps1` entry so it no longer reads like a routine deferred tracker candidate. It should state that the launcher is a separate bootstrap/runtime follow-up because of its binary-download/local-assumption behavior.
3. Add a completed-log entry for D30 summarizing:
   - the launcher was audited read-only
   - a new investigation note was added
   - the bootstrap/runtime boundary was made explicit in the live queue
4. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- One new investigation note exists under `notes/` and is tracked.
- `notes/CP9_ACTIVE_QUEUE.md` now describes `scripts/start_litert_host_server.ps1` truthfully as a separate bootstrap/runtime follow-up.
- No `scripts/*.ps1` file changed.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D30: document LiteRT host launcher bootstrap assumptions
```

Tight equivalent is acceptable if it preserves the two core actions:
- add the investigation note
- repair the live queue wording for the LiteRT host launcher

## Delegation hints

- Good `gpt-5.4 high` worker slice: read-only confirmation + note + queue update.
- If delegated, the worker should own the script inspection, the new note, the
  queue update, the single commit, and a concise statement of why the LiteRT
  host launcher is not yet in the same category as the narrow operator wrappers.

## Anti-recommendations

- Do **not** track `scripts/start_litert_host_server.ps1` in this slice.
- Do **not** change `scripts/litert_native_openai_server.py`.
- Do **not** revisit D22/D23 transport conclusions.
- Do **not** widen into `cleanup_android_harness_artifacts.ps1` or `stop_*`.
- Do **not** turn this into a LiteRT host implementation/refactor lane.

## Report format

- Commit sha + subject.
- Name/path of the new investigation note.
- Short summary of the bootstrap/runtime conclusions.
- Queue update summary.
- What remains open after D30.
