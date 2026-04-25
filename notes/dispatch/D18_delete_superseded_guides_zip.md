# Slice D18 - delete the superseded `guides.zip` snapshot

- **Role:** main agent (`gpt-5.4 xhigh`). Execution/tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** light read-only scouting is fine, but no parallel writer should touch the same tracker notes.
- **Why this slice now:** After D17, the smallest remaining repo-root execution branch is the zip cleanup. `guides.zip` is already documented as the older, partial, superseded snapshot, while `4-13guidearchive.zip` matches the current untracked `guides/` tree and remains the intended local fallback. The right next move is to delete only `guides.zip`, keep `4-13guidearchive.zip` untouched, and close the pending zip wording in the truth surfaces.

## Outcome

One focused commit that:

1. Deletes the untracked repo-root file:
   - `guides.zip`
2. Leaves this untracked repo-root fallback untouched:
   - `4-13guidearchive.zip`
3. Updates:
   - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
4. Closes the actionable zip-cleanup branch without widening into `guides/` tracking.

Important constraint: this slice deletes only `guides.zip`. Do **not** touch the `guides/` directory or `4-13guidearchive.zip`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `71cf97ec4ae12507464b65d726241d4358ac00d0` (`D17: delete residual model-status screenshots`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
3. These repo-root files still exist and are currently untracked:
   - `guides.zip`
   - `4-13guidearchive.zip`
4. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - the large untracked `guides/` tree remains
   - the residual historical root `notes/` backlog remains

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `guides.zip` (delete)
  - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `4-13guidearchive.zip`
  - any file under `guides/`
  - `notes/dispatch/README.md`
  - `LM_STUDIO_MODELS_20260410.json`
  - any screenshot or mockup asset
  - any file under `scripts/`
  - any file under `android-app/`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - rename or move either zip
  - create a replacement archive
  - widen into the `guides/` content-tracking pass
  - widen into residual historical-note cleanup

## Delete / edit rules

### Step 1 - delete `guides.zip`

`guides.zip` is untracked, so do **not** use `git rm`.

Use native PowerShell deletion (`Remove-Item -LiteralPath ...`) or an
equivalent safe local delete for:

- `guides.zip`

After deletion, confirm:

- `guides.zip` is absent
- `4-13guidearchive.zip` is still present
- the repo-root `guides/` directory remains untouched

### Step 2 - update `notes/ROOT_RETENTION_TRIAGE_20260423.md`

Keep the original candidate table intact as the historical triage record, but
bring the execution-facing prose current.

Required changes:

1. Update the post-triage prose near the top so it no longer says
   `guides.zip` remains a pending delete-candidate.
2. In `Execution Status` or `Notes`, make it explicit that:
   - D18 deleted `guides.zip` as the superseded partial snapshot
   - `4-13guidearchive.zip` remains intentionally local-only as the fuller fallback
   - the actionable zip-cleanup branch is now closed
3. Do **not** change the candidate table row for `4-13guidearchive.zip`.
4. Do **not** rewrite the table broadly; preserve it as the original D11 triage artifact.

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the live tracker minimally and truthfully:

1. Advance the `Last updated` line to include D18.
2. In carry-over backlog item `(d)`, mark the repo-root zip archive execution
   follow-up as closed rather than pending.
3. Make the closed wording explicit:
   - `guides.zip` was deleted by D18
   - `4-13guidearchive.zip` remains intentionally local-only as the fallback
4. Append a completed-log entry for D18 summarizing the delete + queue closure.
5. Do **not** reshuffle unrelated backlog items.

## Commit

Single commit only. Suggested subject:

```text
D18: delete superseded guides zip
```

Tight equivalent is acceptable if it preserves the two core actions:
- delete `guides.zip`
- close the pending zip-cleanup wording in the tracker notes

## Acceptance

- One commit only.
- `guides.zip` is absent from the repo root.
- `4-13guidearchive.zip` remains present and untouched.
- The repo-root `guides/` directory remains untouched.
- `notes/ROOT_RETENTION_TRIAGE_20260423.md` truthfully reflects D18 execution status while preserving the original candidate table.
- `notes/CP9_ACTIVE_QUEUE.md` no longer carries the zip branch as pending actionable backlog.
- No file outside the explicit touch set changed.

## Delegation hints

- Good `gpt-5.4 high` worker slice: one untracked delete + two narrow tracker-note updates.
- If delegated, the worker should own the delete, the two note updates, the
  single commit, and a clear report confirming `4-13guidearchive.zip` remained
  untouched.

## Anti-recommendations

- Do **not** delete `4-13guidearchive.zip`.
- Do **not** touch the `guides/` tree.
- Do **not** widen into `LM_STUDIO_MODELS_20260410.json`.
- Do **not** start the broader `guides/` tracking pass here.

## Report format

- Commit sha + subject.
- Confirmation that `guides.zip` was deleted.
- Confirmation that `4-13guidearchive.zip` remains untouched.
- Short summary of each note update.
- Any tiny stale wording you corrected while closing the zip branch.
