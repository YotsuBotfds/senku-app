# Slice D15 - triage repo-root screenshots and mockup note without acting on the files yet

- **Role:** main agent (`gpt-5.4 xhigh`). Manual-triage/doc-only slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice updates the live queue and writes one new decision note after inspecting local screenshots.
- **Why this slice now:** After D14, the screenshot/mockup bucket is the smallest unresolved repo-root retention branch. The queue still carries the six `senku_*.png` files as a standalone visual-review defer, and D11/D13 already tie `senku_mobile_mockups.md` to that same screenshot-aware decision path. The right next move is not to delete or track anything blindly. It is to perform the visual/content triage first, record explicit keep/delete/preserve decisions, and only then tee up a tiny execution slice if needed.

## Outcome

One doc-only/manual-triage commit that:

1. Adds a tracked screenshot-review note:
   - `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`
2. Updates the live queue:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Records explicit retention recommendations for these seven repo-root items without changing the files themselves:
   - `senku_home_screen_1775908565198.png`
   - `senku_answer_detail_1775908579084.png`
   - `senku_search_results_1775908590965.png`
   - `senku_first_launch_1775908603459.png`
   - `senku_model_loaded_1775908948158.png`
   - `senku_model_not_loaded_1775908960322.png`
   - `senku_mobile_mockups.md`

Important constraint: this is a decision pass only. Do **not** delete, move, rename, track, or ignore the seven root files in this slice.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `198c0d5` (`D14: resync planner truth surfaces after D13`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. `notes/ROOT_SCREENSHOT_REVIEW_20260423.md` does not already exist as a tracked file.
4. These repo-root items exist and are currently untracked:
   - `senku_home_screen_1775908565198.png`
   - `senku_answer_detail_1775908579084.png`
   - `senku_search_results_1775908590965.png`
   - `senku_first_launch_1775908603459.png`
   - `senku_model_loaded_1775908948158.png`
   - `senku_model_not_loaded_1775908960322.png`
   - `senku_mobile_mockups.md`
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `4-13guidearchive.zip`, `guides.zip`, and `LM_STUDIO_MODELS_20260410.json` remain untracked
   - the large untracked `guides/` tree remains
   - the residual historical root `notes/` backlog remains

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - any of the six `senku_*.png` files
  - `senku_mobile_mockups.md`
  - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
  - `notes/dispatch/README.md`
  - any zip archive
  - `LM_STUDIO_MODELS_20260410.json`
  - any file under `guides/`
  - any file under `scripts/`
  - any file under `android-app/`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - delete anything
  - move anything
  - create repo-stable image assets yet
  - widen into a broader UI or gallery review
  - treat bookkeeping mentions as sufficient justification to track screenshots

## The triage pass

### Step 1 - inspect the seven root items read-only

You must actually inspect the local screenshots and the mockups markdown.

Required inspection surface:

- read `senku_mobile_mockups.md`
- visually inspect all six repo-root screenshots

Helpful comparison guidance:

- the mockups note currently uses absolute `.gemini` image paths outside the repo for four screens
- if helpful, compare those external targets against the repo-root screenshots to determine whether the root PNGs are the real canonical copies or just local duplicates

Use read-only inspection only. Do **not** edit the images or the mockup note in this slice.

### Step 2 - answer the retention questions explicitly

Your note must answer, at minimum:

- Is each screenshot safe to keep from a secrets/PII standpoint?
- Is each screenshot still truthful enough to preserve as historical/mockup evidence, or would it be misleading stale product evidence?
- For the four screenshots that correspond to the mockup note, should the repo-root PNGs become the canonical copies if the mockup note is ever preserved?
- Do `senku_model_loaded_1775908948158.png` and `senku_model_not_loaded_1775908960322.png` have any durable repo-history value, or are they local residue?
- Should `senku_mobile_mockups.md` be:
  - `delete-candidate`
  - `keep-local-only`
  - `relocate-and-track`
  - or another clearly named retention outcome

### Step 3 - write `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`

Write a concise note that includes:

1. A short summary section:
   - how many items fell into each bucket
   - whether a follow-up execution slice is recommended
2. A per-item decision matrix for all 7 items with:
   - path
   - what it appears to show
   - safe/not-safe or any caution
   - recommended disposition:
     - `keep-local-only`
     - `relocate-and-track`
     - `track-as-historical`
     - `delete-candidate`
   - rationale
   - blocker or Tate-decision point
3. A canonical-source note for the four mockup-linked screens:
   - repo-root screenshot vs `.gemini` external path
   - which should be treated as canonical if later preserved

Bias toward preservation-by-decision, not preservation-by-action. This slice is meant to make the next execution cut small and obvious.

### Step 4 - narrow `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally:

1. Advance the `Last updated` line to mention the screenshot/mockup triage note.
2. Keep the truth that no slices are currently in flight unless your own execution state makes that false.
3. Replace the generic screenshot bucket `(e)` with wording that points at `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`.
4. Keep the mockup coupling truthful:
   - if the note says `senku_mobile_mockups.md` still needs Tate confirmation before deletion/preservation, say so
5. Do **not** reshuffle unrelated backlog items.
6. Append a completed-log entry for D15 summarizing:
   - screenshot/mockup triage note created
   - seven root items reviewed
   - no root files acted on yet

## Commit

Single commit only. Suggested subject:

```text
D15: triage repo-root screenshots and mockup note
```

Tight equivalent is acceptable if it preserves the two core actions:
- screenshot/mockup triage
- queue narrowing

## Acceptance

- One commit only.
- `notes/ROOT_SCREENSHOT_REVIEW_20260423.md` is tracked and covers all 7 scoped items.
- Each screenshot and `senku_mobile_mockups.md` has an explicit disposition recommendation.
- `notes/CP9_ACTIVE_QUEUE.md` points the screenshot/mockup follow-up at the new note.
- None of the seven root items were deleted, moved, renamed, staged independently, or otherwise modified.
- No file outside the explicit touch set changed.

## Delegation hints

- Good `gpt-5.4 high` worker slice: manual-triage note backed by actual visual inspection, plus one queue update.
- If delegated, the worker should own the screenshot viewing, mockup-note inspection, the decision note, the narrow queue update, and the single commit, then report the sha plus any execution follow-up it recommends.

## Anti-recommendations

- Do **not** delete screenshots in this slice.
- Do **not** delete `senku_mobile_mockups.md` in this slice.
- Do **not** rewrite the mockup note to repo-stable paths yet.
- Do **not** widen into zip, guides, script, or historical-notes cleanup.

## Report format

- Commit sha + subject.
- Disposition summary by bucket.
- Canonical-source conclusion for the four mockup-linked screenshots.
- Whether a follow-up execution slice is recommended, and for what.
- Any safety or staleness cautions surfaced during visual review.
