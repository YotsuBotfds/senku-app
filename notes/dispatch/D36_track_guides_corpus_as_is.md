# Slice D36 - track the guides corpus as-is

- **Role:** main agent (`gpt-5.4 xhigh`). Tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md` or `notes/ROOT_RETENTION_TRIAGE_20260423.md`.
- **Why this slice now:** After D35, `guides/` is the highest-leverage remaining hygiene unlock. It is the last large untracked project corpus that stands between the current repo state and clean guide/app work. The clean version of this slice is purely tracking: add the current corpus to git exactly as it exists, update the queue, and update the root-retention note so the local-only zip is no longer blocking on untracked guide state.

## Outcome

One focused commit that:

1. Tracks the current `guides/` corpus as-is.
2. Updates:
   - `notes/CP9_ACTIVE_QUEUE.md`
   - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
3. Closes carry-over backlog item `(b)` without widening into guide QA, family cleanup, or archive deletion.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `50220ddd56868725204412198655ac136b672619` (`D35: retire global Android device cleanup helper`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
   - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
3. Current-checkout corpus shape is still approximately:
   - `guides/` total files: `754`
   - already tracked guide files: `11`
   - currently untracked guide files: `743`
4. There are no tracked-file modifications or deletes already pending inside `guides/`; this slice expects the delta there to be new-path tracking only.
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - `4-13guidearchive.zip` remains untracked local-only
   - the residual historical root `notes/` backlog remains

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `guides/**`
  - `notes/CP9_ACTIVE_QUEUE.md`
  - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
- Do **not** touch:
  - `GUIDE_PLAN.md`
  - `guideupdates.md`
  - any file under `notes/` beyond the two scoped tracker notes
  - `4-13guidearchive.zip`
  - any script or app source file
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - edit guide content
  - rename guide files
  - fix mojibake, links, taxonomy, metadata, or family overlap in this slice
  - delete the local-only archive

## Verify / track / edit rules

### Step 1 - read-only corpus confirmation

Before staging anything:

1. Confirm the current `guides/` counts are still in the same range (`754` total / `11` tracked / `743` untracked, or a very small drift explainable by current checkout reality).
2. Confirm there are no already-modified tracked guide files; this slice should be an add-only guide delta.

If `guides/` is not an add-only tracking situation anymore, STOP instead of forcing the slice.

### Step 2 - track the corpus as-is

Track the currently untracked `guides/**` files exactly as they exist.

Bias hard toward no content interaction:

1. Do not open/save guides in a way that could rewrite bytes.
2. Do not run formatters, encoders, normalization tools, or bulk search/replace.
3. Do not edit any guide file manually.

### Step 3 - verify the staged guide delta is add-only

Before commit, verify the staged `guides/` delta is tracking-only:

1. Under `guides/`, staged changes are additions only.
2. There are no staged `M`, `D`, or `R` statuses under `guides/`.
3. The count of newly tracked guide paths matches the current untracked guide count from Step 1, modulo any tiny checkout drift you explicitly explain.

If the staged guide delta is not add-only, STOP instead of committing.

### Step 4 - update the two tracker notes

Update the queue and retention note minimally and truthfully:

1. `notes/CP9_ACTIVE_QUEUE.md`
   - advance the `Last updated` line to include D36
   - close carry-over item `(b)` for the `guides/` content-tracking slice
   - add a completed-log entry for D36 stating the corpus was tracked as-is with no guide-content edits
2. `notes/ROOT_RETENTION_TRIAGE_20260423.md`
   - update the `4-13guidearchive.zip` caution so it no longer says the file is waiting on the `guides/` tracking slice to finish
   - keep the archive local-only; do **not** turn this into a delete slice

## Acceptance

- One commit only.
- The previously untracked `guides/**` corpus is now tracked.
- The staged/committed `guides/` delta is additions only.
- No guide file content was edited in this slice.
- `notes/CP9_ACTIVE_QUEUE.md` closes carry-over item `(b)`.
- `notes/ROOT_RETENTION_TRIAGE_20260423.md` no longer treats `4-13guidearchive.zip` as blocked on unfinished guide tracking.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D36: track guides corpus as-is
```

Tight equivalent is acceptable if it preserves the two core actions:
- track the corpus without content edits
- close the guide-tracking carry-over

## Delegation hints

- Good `gpt-5.4 high` worker slice: confirm counts + add-only tracking + two minimal tracker-note updates.
- If delegated, the worker should own the count verification, the add-only
  tracking, the tracker-note updates, the single commit, and a concise note
  about what guide/app-oriented work is now unblocked after D36.

## Anti-recommendations

- Do **not** turn this into guide QA.
- Do **not** fix content, encoding, filenames, or links.
- Do **not** delete `4-13guidearchive.zip`.
- Do **not** widen into the residual historical `notes/` backlog.

## Report format

- Commit sha + subject.
- Guide counts before tracking.
- Confirmation that the `guides/` delta landed as additions only.
- Queue update summary.
- Root-retention note update summary.
- What guide/app work is newly unblocked after D36.
