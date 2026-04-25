# Slice D16 - preserve the four-screen mockup bundle as historical assets

- **Role:** main agent (`gpt-5.4 xhigh`). Execution/tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice moves files, rewrites the mockup note, and updates the live queue / decision notes.
- **Why this slice now:** The user chose the preserve path. D15 already did the visual/content triage and concluded the coherent preserve cut is the four mockup-linked screenshots plus `senku_mobile_mockups.md`, while the two model-status screenshots remain separate delete-candidates. The right next move is to preserve the historical mockup bundle as portable tracked assets, not as a zip, and not as current product evidence.

## Outcome

One focused commit that:

1. Preserves the four mockup-linked screenshots as tracked historical assets under:
   - `notes/reviews/assets/senku_mobile_mockups/`
2. Relocates and tracks the note:
   - `senku_mobile_mockups.md` -> `notes/reviews/senku_mobile_mockups.md`
3. Rewrites the note to use repo-stable relative image paths and explicit historical/mockup framing.
4. Updates the queue / screenshot review notes so the preserved bundle is reflected truthfully.
5. Leaves these two untracked root screenshots untouched as pending delete-candidates:
   - `senku_model_loaded_1775908948158.png`
   - `senku_model_not_loaded_1775908960322.png`

Important constraint: preserve the bundle as ordinary tracked files, not as a zip. Do **not** create or track a zip in this slice.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `6fe0dc2d4d2fe4a8b2be70c9adbad841e3c638da` (`D15: triage repo-root screenshots and mockup note`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
   - `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`
   - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
3. These repo-root source files exist and are currently untracked:
   - `senku_home_screen_1775908565198.png`
   - `senku_answer_detail_1775908579084.png`
   - `senku_search_results_1775908590965.png`
   - `senku_first_launch_1775908603459.png`
   - `senku_mobile_mockups.md`
4. These two delete-candidate screenshots also still exist and remain untracked:
   - `senku_model_loaded_1775908948158.png`
   - `senku_model_not_loaded_1775908960322.png`
5. The destination paths do not already exist as tracked files:
   - `notes/reviews/senku_mobile_mockups.md`
   - `notes/reviews/assets/senku_mobile_mockups/senku_home_screen_1775908565198.png`
   - `notes/reviews/assets/senku_mobile_mockups/senku_answer_detail_1775908579084.png`
   - `notes/reviews/assets/senku_mobile_mockups/senku_search_results_1775908590965.png`
   - `notes/reviews/assets/senku_mobile_mockups/senku_first_launch_1775908603459.png`
6. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `4-13guidearchive.zip`, `guides.zip`, and `LM_STUDIO_MODELS_20260410.json` remain untracked
   - the large untracked `guides/` tree remains
   - the residual historical root `notes/` backlog remains

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `senku_home_screen_1775908565198.png` -> moved to `notes/reviews/assets/senku_mobile_mockups/`
  - `senku_answer_detail_1775908579084.png` -> moved to `notes/reviews/assets/senku_mobile_mockups/`
  - `senku_search_results_1775908590965.png` -> moved to `notes/reviews/assets/senku_mobile_mockups/`
  - `senku_first_launch_1775908603459.png` -> moved to `notes/reviews/assets/senku_mobile_mockups/`
  - `senku_mobile_mockups.md` -> moved to `notes/reviews/senku_mobile_mockups.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
  - `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`
  - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
- Do **not** touch:
  - `senku_model_loaded_1775908948158.png`
  - `senku_model_not_loaded_1775908960322.png`
  - `notes/dispatch/README.md`
  - any zip archive
  - `LM_STUDIO_MODELS_20260410.json`
  - any file under `guides/`
  - any file under `scripts/`
  - any file under `android-app/`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - create or track a zip archive
  - widen into deleting the two model-status screenshots
  - treat the preserved bundle as current product evidence
  - widen into a broader historical-note cleanup

## Move / edit rules

### Step 1 - relocate the four screenshots and the note

These five source files are currently untracked, so do **not** use `git mv`.
Use native PowerShell file moves (`Move-Item -LiteralPath ...`) or an
equivalent safe local move, then stage the new paths normally.

Move to these exact destinations:

- `senku_home_screen_1775908565198.png` -> `notes/reviews/assets/senku_mobile_mockups/senku_home_screen_1775908565198.png`
- `senku_answer_detail_1775908579084.png` -> `notes/reviews/assets/senku_mobile_mockups/senku_answer_detail_1775908579084.png`
- `senku_search_results_1775908590965.png` -> `notes/reviews/assets/senku_mobile_mockups/senku_search_results_1775908590965.png`
- `senku_first_launch_1775908603459.png` -> `notes/reviews/assets/senku_mobile_mockups/senku_first_launch_1775908603459.png`
- `senku_mobile_mockups.md` -> `notes/reviews/senku_mobile_mockups.md`

### Step 2 - rewrite the relocated note for historical portability

Edit `notes/reviews/senku_mobile_mockups.md` so it becomes a clearly historical,
portable note rather than a machine-local root document.

Required changes:

1. Add a short historical banner near the top stating that:
   - this is historical concept/mockup material
   - it does **not** represent current product truth
   - it was relocated from the repo root during D16
2. Rewrite the four image links to use relative repo paths pointing at:
   - `./assets/senku_mobile_mockups/senku_home_screen_1775908565198.png`
   - `./assets/senku_mobile_mockups/senku_answer_detail_1775908579084.png`
   - `./assets/senku_mobile_mockups/senku_search_results_1775908590965.png`
   - `./assets/senku_mobile_mockups/senku_first_launch_1775908603459.png`
3. Clean obvious mojibake / punctuation drift in the note:
   - `browse -> search -> read answer -> first-launch onboarding`
   - fix broken `§` / bullet separators / ellipsis drift where practical
4. Preserve the overall note shape and design-token content; this is a portability/historical-labeling pass, not a redesign.
5. Optionally add one short caution sentence near the top noting that counts/text in the mockups are synthetic/stale.

### Step 3 - update the live decision records

Update these tracked notes minimally:

- `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`
  - add a short execution-status note that D16 preserved the four-screen mockup bundle at the new `notes/reviews/` locations
  - keep the two model-status screenshots as unresolved delete-candidates
  - keep the original decision matrix intact as the triage record
- `notes/ROOT_RETENTION_TRIAGE_20260423.md`
  - add a brief execution-status clarification that the earlier delete-candidate call for `senku_mobile_mockups.md` was superseded by D15 review + D16 preservation
  - do **not** rewrite the original candidate table
- `notes/CP9_ACTIVE_QUEUE.md`
  - narrow `(e)` so it now reflects:
    - the historical mockup bundle is preserved
    - only the two model-status screenshots remain as delete-candidate follow-up pending Tate confirmation
  - append a completed-log entry for D16 summarizing the preserved bundle
  - do **not** reshuffle unrelated backlog items

## Commit

Single commit only. Suggested subject:

```text
D16: preserve historical mockup bundle
```

Tight equivalent is acceptable if it preserves the two core actions:
- preserve the four-screen mockup bundle
- leave the two model-status screenshots unresolved

## Acceptance

- One commit only.
- The four repo-root mockup-linked screenshots no longer appear at the root.
- These tracked destination files exist:
  - `notes/reviews/senku_mobile_mockups.md`
  - `notes/reviews/assets/senku_mobile_mockups/senku_home_screen_1775908565198.png`
  - `notes/reviews/assets/senku_mobile_mockups/senku_answer_detail_1775908579084.png`
  - `notes/reviews/assets/senku_mobile_mockups/senku_search_results_1775908590965.png`
  - `notes/reviews/assets/senku_mobile_mockups/senku_first_launch_1775908603459.png`
- `notes/reviews/senku_mobile_mockups.md` uses repo-stable relative image paths and explicit historical/mockup labeling.
- `senku_model_loaded_1775908948158.png` and `senku_model_not_loaded_1775908960322.png` remain untouched and untracked at the repo root.
- `notes/CP9_ACTIVE_QUEUE.md` now treats the remaining screenshot follow-up as the two model-status delete-candidates only.
- No file outside the explicit touch set changed.

## Delegation hints

- Good `gpt-5.4 high` worker slice: bounded preserve move + note rewrite + narrow queue/decision-note updates.
- If delegated, the worker should own the five file moves, the historical-portability rewrite of the note, the three tracked bookkeeping updates, and the single commit, then report the sha plus any tiny remaining screenshot cleanup still pending.

## Anti-recommendations

- Do **not** create a zip as the preserved artifact.
- Do **not** delete the two model-status screenshots here.
- Do **not** present the preserved mockup bundle as current UI truth.
- Do **not** widen into broader asset archival or gallery work.

## Report format

- Commit sha + subject.
- Final preserved paths.
- Summary of note rewrite changes.
- Queue/decision-note summary.
- Confirmation that the two model-status screenshots remain untouched and untracked.
