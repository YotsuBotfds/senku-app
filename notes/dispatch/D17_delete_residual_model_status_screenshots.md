# Slice D17 - delete the two residual model-status mockup screenshots

- **Role:** main agent (`gpt-5.4 xhigh`). Execution/tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** light read-only scouting is fine, but no parallel writer should touch the same tracker notes.
- **Why this slice now:** D16 preserved the only screenshot bundle with durable historical value. The only repo-root screenshot residue left is `senku_model_loaded_1775908948158.png` and `senku_model_not_loaded_1775908960322.png`, both already classified by D15 as local-only delete-candidates with no durable note linkage. The user delegated the choice to continue, so this slice chooses the delete path and closes the screenshot branch cleanly.

## Outcome

One focused commit that:

1. Deletes these two untracked repo-root screenshots:
   - `senku_model_loaded_1775908948158.png`
   - `senku_model_not_loaded_1775908960322.png`
2. Updates:
   - `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`
   - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Closes the screenshot/mockup follow-up branch as complete.
4. Leaves the preserved historical mockup bundle untouched at:
   - `notes/reviews/senku_mobile_mockups.md`
   - `notes/reviews/assets/senku_mobile_mockups/`

Important constraint: this slice deletes only the two residual model-status screenshots. Do **not** widen into zip cleanup, `guides/`, the residual historical `notes/` backlog, or any `scripts/` follow-up.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `02b153bc7c617b563a60d85fefb8550178ee58be` (`D16: preserve historical mockup bundle`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
   - `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`
   - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
3. These repo-root files still exist and are currently untracked:
   - `senku_model_loaded_1775908948158.png`
   - `senku_model_not_loaded_1775908960322.png`
4. These preserved historical files already exist and are tracked:
   - `notes/reviews/senku_mobile_mockups.md`
   - `notes/reviews/assets/senku_mobile_mockups/senku_home_screen_1775908565198.png`
   - `notes/reviews/assets/senku_mobile_mockups/senku_answer_detail_1775908579084.png`
   - `notes/reviews/assets/senku_mobile_mockups/senku_search_results_1775908590965.png`
   - `notes/reviews/assets/senku_mobile_mockups/senku_first_launch_1775908603459.png`
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `4-13guidearchive.zip`, `guides.zip`, and `LM_STUDIO_MODELS_20260410.json` remain untracked
   - the large untracked `guides/` tree remains
   - the residual historical root `notes/` backlog remains

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `senku_model_loaded_1775908948158.png` (delete)
  - `senku_model_not_loaded_1775908960322.png` (delete)
  - `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`
  - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `notes/reviews/senku_mobile_mockups.md`
  - anything under `notes/reviews/assets/senku_mobile_mockups/`
  - any other `senku_*.png` file
  - `notes/dispatch/README.md`
  - any zip archive
  - `LM_STUDIO_MODELS_20260410.json`
  - any file under `guides/`
  - any file under `scripts/`
  - any file under `android-app/`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - recreate, relocate, or track the deleted screenshots
  - widen into a general screenshot or asset cleanup
  - widen into the zip branch
  - rewrite the historical mockup note/bundle

## Delete / edit rules

### Step 1 - delete the two residual untracked screenshots

These two files are untracked, so do **not** use `git rm`.

Use native PowerShell deletion (`Remove-Item -LiteralPath ...`) or an
equivalent safe local delete for:

- `senku_model_loaded_1775908948158.png`
- `senku_model_not_loaded_1775908960322.png`

After deletion, confirm both paths are absent and no other repo-root screenshot
files were touched.

### Step 2 - update `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`

Keep the original decision matrix intact as the historical triage record, but
update the execution-facing sections so the note reflects reality after D17.

Required changes:

1. In `Execution Status`, add that D17 executed the delete path for:
   - `senku_model_loaded_1775908948158.png`
   - `senku_model_not_loaded_1775908960322.png`
2. Make it explicit that the screenshot/mockup branch is now closed:
   - the four-screen historical mockup bundle was preserved by D16
   - the two residual model-status screenshots were deleted by D17
   - there is no remaining repo-root screenshot follow-up
3. Update `Recommended Next Move` so it no longer asks for Tate confirmation.
4. Do **not** rewrite the per-item matrix beyond minimal wording if needed for
   coherence; preserve it as the original triage artifact.

### Step 3 - update `notes/ROOT_RETENTION_TRIAGE_20260423.md`

Keep the original candidate table intact, but bring the status prose current.

Required changes:

1. In `Execution Status` or `Notes`, clarify that:
   - D16 preserved the four-screen mockup bundle under `notes/reviews/`
   - D17 deleted the two residual model-status screenshots
   - the screenshot/mockup residue is now closed
2. Do **not** rewrite the original candidate table.
3. Do **not** change the zip dispositions in this slice.

### Step 4 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the live tracker minimally and truthfully:

1. Advance the `Last updated` line to include D17 and note that the residual
   screenshot delete-candidates are gone.
2. In carry-over backlog item `(e)`, mark the screenshot/mockup follow-up as
   closed rather than pending.
3. Append a completed-log entry for D17 summarizing:
   - the two residual model-status screenshots were deleted
   - the screenshot/mockup branch is now fully closed after D16 + D17
4. While you are already in the file, if struck carry-over item `(g)` still
   mentions `senku_mobile_mockups.md` needing Tate confirmation or a later
   preservation pass, rewrite only that clause so it truthfully reflects D16
   having already preserved the note and historical mockup bundle.
5. Do **not** reshuffle unrelated backlog items.

## Commit

Single commit only. Suggested subject:

```text
D17: delete residual model-status screenshots
```

Tight equivalent is acceptable if it preserves the two core actions:
- delete the two residual model-status screenshots
- close the screenshot/mockup follow-up in the tracker notes

## Acceptance

- One commit only.
- `senku_model_loaded_1775908948158.png` is absent from the repo root.
- `senku_model_not_loaded_1775908960322.png` is absent from the repo root.
- The preserved historical bundle under `notes/reviews/` remains untouched.
- `notes/ROOT_SCREENSHOT_REVIEW_20260423.md` now says the screenshot branch is closed and no longer waits on Tate confirmation.
- `notes/ROOT_RETENTION_TRIAGE_20260423.md` truthfully reflects D16 + D17 execution status.
- `notes/CP9_ACTIVE_QUEUE.md` no longer carries screenshot follow-up as pending backlog.
- No file outside the explicit touch set changed.

## Delegation hints

- Good `gpt-5.4 high` worker slice: tiny delete + narrow tracker-note cleanup.
- If delegated, the worker should own the two deletes, the three note updates,
  the single commit, and a clean report of what screenshot residue remains
  afterward (expected answer: none).

## Anti-recommendations

- Do **not** touch the preserved mockup assets.
- Do **not** widen into the zip branch.
- Do **not** revive the deleted screenshots under a tracked location.
- Do **not** perform broader historical-note cleanup here.

## Report format

- Commit sha + subject.
- Confirmation that both root screenshots were deleted.
- Short summary of each note update.
- Confirmation that the preserved mockup bundle remained untouched.
- Any tiny stale-tracker wording you corrected while closing the branch.
