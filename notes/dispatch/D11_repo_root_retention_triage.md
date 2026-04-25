# Slice D11 - triage repo-root retention backlog and narrow the carry-over queue

- **Role:** main agent (`gpt-5.4 xhigh`). Doc-only/manual-triage slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice updates the live queue and writes one new tracked triage note.
- **Why this slice now:** Wave C is closed through `W-C-4`, and the next clean backlog cut is not one of the huge tracking sweeps. The bounded repo-root retention buckets are still unresolved in `notes/CP9_ACTIVE_QUEUE.md`: zip archives, dated snapshot files, and audit/mockup markdown. The goal here is to make explicit keep/archive/delete recommendations and narrow the queue without widening into `guides/`, the historical `notes/` backlog, screenshots, or scripts.

## Outcome

One doc-only commit that:

1. Adds a tracked retention note:
   - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
2. Updates the live queue:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Makes an explicit disposition recommendation for each of these repo-root files:
   - `4-13guidearchive.zip`
   - `guides.zip`
   - `CURRENT_LOCAL_TESTING_STATE_20260410.md`
   - `LM_STUDIO_MODELS_20260410.json`
   - `UI_DIRECTION_AUDIT_20260414.md`
   - `auditglm.md`
   - `gptaudit4-21.md`
   - `senku_mobile_mockups.md`

Important constraint: this is a triage/decision pass, not an execution pass. Do **not** delete, move, rename, track, or ignore the eight root candidates in this slice. Record the decisions cleanly so a later narrow execution slice can act on them.

Expected scope: 2 tracked files total.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `8693458` (`W-C-closeout: record low-coverage canary probe result`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. `notes/ROOT_RETENTION_TRIAGE_20260423.md` does not already exist as a tracked file.
4. The following repo-root candidates exist and are currently untracked:
   - `4-13guidearchive.zip`
   - `guides.zip`
   - `CURRENT_LOCAL_TESTING_STATE_20260410.md`
   - `LM_STUDIO_MODELS_20260410.json`
   - `UI_DIRECTION_AUDIT_20260414.md`
   - `auditglm.md`
   - `gptaudit4-21.md`
   - `senku_mobile_mockups.md`
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the deferred `guides/` corpus remains untracked
   - the broad historical `notes/` backlog remains untracked
   - the six repo-root `senku_*.png` screenshots remain untracked and deferred
   - the untracked dispatch drafts under `notes/dispatch/` remain present

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `AGENTS.md`
  - `opencode.json`
  - `notes/dispatch/README.md`
  - any file under `guides/`
  - any file under `scripts/`
  - any file under `android-app/`
  - any screenshot file matching `senku_*.png`
  - the eight repo-root candidates themselves
  - `archive/README.md`
- Do **not**:
  - delete any file
  - move any file
  - rename any file
  - add `.gitignore` rules
  - create follow-up execution code
  - widen into the historical `notes/` backlog
  - widen into dispatch-root cleanup

## The triage pass

### Step 1 - inspect the eight root candidates and collect only the minimum evidence needed

Use read-only inspection. Good evidence sources include:

- file size / timestamps where helpful
- zip entry counts and hashes for the two archive files
- brief content inspection of the markdown/json files
- whether the content appears duplicated, superseded, historically valuable, or misleading if left in the root

You are deciding retention value, not rewriting the contents.

### Step 2 - write `notes/ROOT_RETENTION_TRIAGE_20260423.md`

Write a concise Tate-facing note with one row or bullet per candidate file. For each file, record:

- path
- short description of what it is
- current git state (`untracked root file`)
- recommended disposition:
  - `keep-in-root-and-track`
  - `relocate-then-track`
  - `keep-local-only`
  - `delete-candidate`
- rationale in 1-3 sentences
- any blocker or caution

Also include a short summary section at the top:

- how many files fell into each disposition bucket
- what the recommended *next execution slice* should do

Required judgment guidance:

- Prefer `relocate-then-track` for small historical docs that still have durable repo value but do not belong at the root.
- Prefer `keep-local-only` only if the file is useful as local personal context but not appropriate repo history.
- Use `delete-candidate` only when you can explain clearly why the file looks redundant, obsolete, or misleading.
- If uncertain, bias toward preserving via recommendation rather than pretending certainty.

### Step 3 - narrow `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally to reflect that this triage happened.

Required queue changes:

1. Advance the `Last updated` line to mention the root-retention triage note.
2. Keep the truth that no slices are currently in flight unless your own execution state makes that false.
3. In the carry-over backlog:
   - replace the generic buckets `(d)`, `(f)`, and `(g)` with wording that points at `notes/ROOT_RETENTION_TRIAGE_20260423.md`
   - keep screenshots `(e)` separate and still deferred
   - do **not** reprioritize `guides/`, residual historical `notes/`, orphan `.py`, or the PowerShell follow-up
4. Append a completed-log entry for D11 summarizing:
   - repo-root retention triage note created
   - eight root candidates dispositioned
   - queue narrowed without acting on the files yet

This is bookkeeping only, not a project reprioritization pass.

### Step 4 - commit

Single commit only. Suggested subject:

```text
D11: triage repo-root retention backlog
```

Tight equivalent is acceptable if it preserves the two core actions:
- repo-root retention triage
- queue narrowing

## Acceptance

- One commit only.
- `notes/ROOT_RETENTION_TRIAGE_20260423.md` is tracked and names all eight scoped root files.
- Each of the eight files has an explicit disposition recommendation.
- `notes/CP9_ACTIVE_QUEUE.md` points the zip/snapshot/audit carry-over items at the new triage note instead of leaving them as generic buckets.
- The screenshot bucket remains deferred and separate.
- No file outside the explicit touch set changed.
- None of the eight repo-root candidates were deleted, moved, renamed, staged, or otherwise modified in this slice.
- Final `git status --short` still shows the expected unrelated dirt and deferred trees, plus the same eight root candidates still untracked.

## Delegation hints

- Good `gpt-5.4 high` worker slice: bounded doc-only triage with a single new note and one queue update.
- If delegated, the worker should own the file inspection, the disposition note, the minimal queue narrowing, and the single commit, then hand back the sha plus any cautions about later execution risk.

## Anti-recommendations

- Do **not** treat this as permission to clean the repo root aggressively.
- Do **not** delete the zip archives just because they look old.
- Do **not** track the root files directly in this slice; the point is to decide first, execute later.
- Do **not** widen into screenshot visual review or PII review.
- Do **not** widen into `guides/` or the historical `notes/` backlog just because the triage note mentions them as context.
- Do **not** "fix" `notes/dispatch/README.md` here even though it has stale Wave C wording; that is separate drift.

## Report format

- Commit sha + subject.
- Short disposition summary by bucket.
- Confirmation that all eight candidates were recorded in `notes/ROOT_RETENTION_TRIAGE_20260423.md`.
- Confirmation that queue items `(d)`, `(f)`, and `(g)` now point at the triage note.
- Out-of-scope drift still remaining after D11.
