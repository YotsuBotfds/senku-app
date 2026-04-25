# Slice D10 - lock Wave C direction note and make W-C-0 draftable

- **Role:** main agent (`gpt-5.4 xhigh`). Doc-only / planner-note only. Safe to delegate to a `gpt-5.4 high` worker for speed, but main owns the routing call.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice touches the live queue plus the tracked Wave C research note and writes the new direction note that future W-C slices will depend on.
- **Why this slice now:** D9 (`601e38b`) cleaned the tracker surface and moved the live queue cleanly to Wave C direction-note drafting. The forward research already says the next move is a short planner direction note, then `W-C-0` panel expansion. The remaining risk is a hidden dependency: `scripts/run_abstain_regression_panel.ps1` is still untracked, but Wave C panel work depends on it. This slice exists to lock the Wave C series order, choose the W-C-0 panel/input strategy, and explicitly decide how W-C-0 handles the runner dependency before anyone drafts the first substantive tuning slice.

## Outcome

One doc-only commit that:

1. Creates `notes/WAVE_C_DIRECTION_20260422.md`, a short planner direction note that locks the Wave C sequence and the W-C-0 precursor decisions.
2. Normalizes `notes/WAVE_C_FORWARD_RESEARCH_20260422.md` to plain ASCII if needed, without changing its substantive conclusions.
3. Updates `notes/CP9_ACTIVE_QUEUE.md` so the completed log records the direction note and the next substantive move becomes `W-C-0` panel expansion / runner-preflight.

Expected scope: 3 files total, all under `notes/`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `601e38b` (`D9: reconcile tracker docs and relabel historical lanes`) or descends from it.
2. `git status --short -- notes/CP9_ACTIVE_QUEUE.md notes/WAVE_C_FORWARD_RESEARCH_20260422.md` is clean before you start. If either is already dirty, **STOP** and escalate.
3. `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md` exists and is tracked.
4. `notes/WAVE_C_FORWARD_RESEARCH_20260422.md` exists and is tracked.
5. `scripts/run_abstain_regression_panel.ps1` is still **untracked** at slice-draft time. Do **not** track or edit it in this slice; the point is to lock how W-C-0 will handle that dependency.
6. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the deferred `guides/` corpus and residual historical `notes/` backlog remain untracked
   - repo-root zip/screenshot/audit items remain untracked

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `notes/CP9_ACTIVE_QUEUE.md`
  - `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`
  - `notes/WAVE_C_DIRECTION_20260422.md` (new file)
- Do **not** touch:
  - any file under `scripts/`
  - `query.py`
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - any `guides/` file
  - any repo-root triage item
- Do **not** turn this into `W-C-0`.
  - no panel query-list creation
  - no runner tracking
  - no script edits
  - no artifact generation
  - no threshold moves
- Do **not** widen into general Wave C research cleanup beyond ASCII normalization of the tracked forward-research note.

## The edits

### Edit 1 - normalize the tracked Wave C research note

Repair any mojibake / non-ASCII punctuation in `notes/WAVE_C_FORWARD_RESEARCH_20260422.md` using plain ASCII equivalents only.

Rules:
- keep the substance unchanged
- keep headings / section order intact
- do not expand scope or add new conclusions

This is byte cleanup only so the note can be cited cleanly by the new direction note and later slice prompts.

### Edit 2 - author `notes/WAVE_C_DIRECTION_20260422.md`

Write a short planner direction note (~40-90 lines) that uses the tracked research and current queue as inputs and locks the following decisions.

Required decisions to lock:

1. **Wave C order**
   - Default order stays:
     - `W-C-0` panel expansion / runner-preflight
     - `W-C-1` telemetry aggregation helper
     - `W-C-2` desktop abstain-threshold tuning
     - `W-C-3` Android alignment / mirror
     - `W-C-4` uncertain-fit band calibration
     - `W-C-5` optional post-gen downgrade revisit only if evidence warrants

2. **W-C-0 panel target**
   - Choose **fork**, not in-place expansion of `abstain_baseline_20260418`.
   - The note should make the planner recommendation explicit: preserve the 2026-04-18 baseline as the historical reference and create a new near-boundary panel for the 2026-04-22+ evidence era.
   - Suggested naming is fine to lock, for example `abstain_nearboundary_20260423`.

3. **Panel input representation**
   - Lock the representation choice for W-C-0.
   - Preferred decision: use a tracked YAML sidecar under `notes/specs/` for the near-boundary query list rather than baking the list only into an untracked runner or only into artifacts.
   - You are not creating the sidecar in this slice; you are choosing the representation for W-C-0.

4. **Runner dependency handling**
   - Explicitly record that `scripts/run_abstain_regression_panel.ps1` is still untracked at direction-note time.
   - Lock the policy that W-C-0 must begin with a narrow runner-preflight:
     - verify the current runner still matches the expected CLI shape,
     - rerun the Rule-2b secret-scan / HARD-STOP hygiene checks appropriate for a deferred script,
     - if clean, track the runner in the same W-C-0 commit that introduces the panel input sidecar,
     - if not clean or if the surface proves broader than expected, STOP and split a micro-slice instead of quietly depending on untracked infrastructure.

5. **Scout-audit policy**
   - Lock:
     - D10 / `W-C-0` / `W-C-1`: no scout-audit by default
     - `W-C-2` / `W-C-3` / `W-C-4`: yes, scout-audit required before dispatch because threshold moves are small-surface but high-consequence

6. **Safety / regression discipline**
   - Lock that `W-C-2+` slices must:
     - preserve Android safety invariants and acute-mental-health routing invariants,
     - confirm or add guard tests if missing,
     - run pre/post mode-flip probe diffs rather than treating mode changes as incidental.

7. **Desktop final-mode emission**
   - Lock the decision to leave desktop verification on Python tests for now; do not add a desktop `final_mode` emission mirror as part of the initial Wave C series.

8. **Tracker integration**
   - State that `notes/CP9_ACTIVE_QUEUE.md` remains the live queue, D10 is the direction-lock step, and `W-C-0` becomes the next substantive move after this note lands.

Required evidence/citation discipline:
- Cite the tracked Wave C research note for the series decomposition / immediate-next-step guidance.
- Cite `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md` for why `0.67 / 3` is still a watchpoint, not yet an automatic production move.
- Cite live code locations for the current threshold surfaces and Android `final_mode` emission:
  - `query.py`
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
- Keep citations compact; this is a planner note, not a full research memo.

### Edit 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Make only the minimal queue updates needed after D10:

1. Advance the "Last updated" line to D10 / Wave C direction lock.
2. Keep the truth that no slices are currently in flight unless your own execution state makes that false.
3. Change the "next" wording from generic Wave C direction-note drafting to the concrete next move:
   - `W-C-0` panel expansion / runner-preflight
4. Append a completed-log entry summarizing:
   - Wave C direction note landed
   - series order locked
   - W-C-0 forked-panel decision locked
   - runner-preflight dependency called out explicitly
5. Do **not** reprioritize unrelated backlog.

### Edit 4 - commit

Single commit only. Suggested subject:

```text
D10: lock Wave C direction note and W-C-0 prep
```

Tight equivalent is acceptable if it preserves the two core actions:
- Wave C direction note landed
- W-C-0 preflight strategy locked

## Acceptance

- One commit only.
- `notes/WAVE_C_DIRECTION_20260422.md` exists and is tracked.
- The direction note explicitly locks:
  - the Wave C slice order,
  - fork-not-expand for `W-C-0`,
  - tracked YAML sidecar as the preferred W-C-0 panel-input representation,
  - the untracked-runner preflight policy,
  - scout-audit policy by slice tier,
  - safety/probe regression discipline for `W-C-2+`,
  - "no desktop final_mode mirror yet".
- `notes/CP9_ACTIVE_QUEUE.md` records D10 and makes `W-C-0` the next substantive planner move.
- `notes/WAVE_C_FORWARD_RESEARCH_20260422.md` is ASCII-clean after the byte cleanup.
- The following grep returns **no matches**:

```powershell
rg -n '[^\x00-\x7F]' `
  notes/WAVE_C_FORWARD_RESEARCH_20260422.md `
  notes/WAVE_C_DIRECTION_20260422.md `
  notes/CP9_ACTIVE_QUEUE.md
```

- No file outside the explicit touch set changed.
- Final `git status --short` still shows the expected unrelated dirt and deferred trees, but none of the touched files remain unstaged/modified after commit.

## Delegation hints

- Good `gpt-5.4 high` worker slice: doc-only, bounded, no runtime work.
- If delegated, the worker should own the ASCII cleanup, the direction note draft, the queue update, and the single commit, then hand back the sha plus the precise W-C-0 next-step summary.

## Anti-recommendations

- Do **not** turn this into `W-C-0`.
- Do **not** track or edit `scripts/run_abstain_regression_panel.ps1` in this slice.
- Do **not** add the YAML sidecar yet; this slice only locks the representation decision.
- Do **not** touch threshold constants or tests here.
- Do **not** add a desktop `final_mode` emission mirror here.
- Do **not** widen into broader notes backlog cleanup or dispatch-root bookkeeping.

## Report format

- Commit sha + subject.
- Files changed.
- Short verification bullets:
  - direction note landed
  - W-C-0 strategy locked
  - queue now points at `W-C-0`
  - Wave C research note ASCII-clean
- Any out-of-scope drift still remaining after D10.
