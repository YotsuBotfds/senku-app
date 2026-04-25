# Slice D12 - resync `notes/dispatch/README.md` to post-Wave-C / post-D11 reality

- **Role:** main agent (`gpt-5.4 xhigh`). Tiny doc-only slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This is a single-file truthfulness pass for the dispatch root.
- **Why this slice now:** `notes/dispatch/README.md` is materially stale. It still describes a D8-era world where Wave C direction-note drafting is next and the dispatch root is "trustworthy/clean" after D8, but the live queue now says Wave C is closed through `W-C-4`, D11 has landed, no slices are in flight, and the dispatch root still contains unrotated prompt drafts (`D9`, `D10`, `D11`, and the `W-C-*` series). The goal here is to make the dispatch README truthful again without rotating files or reprioritizing backlog.

## Outcome

One tiny doc-only commit that:

1. Updates:
   - `notes/dispatch/README.md`
2. Makes the file truthful about:
   - current project state (Wave C closed; D11 landed; no slices in flight)
   - where to look for the live next-step truth (`notes/CP9_ACTIVE_QUEUE.md`)
   - the actual dispatch-root condition (retained live records plus unrotated prompt drafts still present)

Expected scope: 1 tracked file total.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `5b9214c` (`D11: triage repo-root retention backlog`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/dispatch/README.md`
3. `notes/CP9_ACTIVE_QUEUE.md` already reflects:
   - Wave C closed through `W-C-4`
   - D11 landed
   - no slices currently in flight
4. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the large untracked `guides/` corpus
   - the large untracked historical `notes/` backlog
   - the repo-root retention candidates and screenshots
   - the untracked dispatch prompt drafts under `notes/dispatch/`

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `notes/dispatch/README.md`
- Do **not** touch:
  - `notes/CP9_ACTIVE_QUEUE.md`
  - any file under `notes/dispatch/completed/`
  - any untracked dispatch prompt file
  - `AGENTS.md`
  - `opencode.json`
  - any file under `guides/`, `scripts/`, `android-app/`, or `notes/` outside the README above
- Do **not**:
  - rotate or move dispatch files
  - rename dispatch files
  - reprioritize backlog items
  - create a new notes file
  - widen into root-retention execution, screenshots, or historical-note tracking

## The edit

Refresh `notes/dispatch/README.md` so it mirrors the current tracked truth in `notes/CP9_ACTIVE_QUEUE.md` and the actual dispatch-root state on disk.

Required content changes:

1. **Active slices / current state**
   - remove the stale claim that Wave C direction-note drafting is next
   - state clearly that CP9 is closed, Wave C is closed through `W-C-4`, D11 has landed, and no slices are currently in flight
   - point readers to `notes/CP9_ACTIVE_QUEUE.md` as the live queue source for the next backlog move
2. **Landed / unrotated dispatch files**
   - stop pretending only D8 is pending future rotation
   - acknowledge the unrotated prompt set currently present at the dispatch root:
     - `D9_tracker_doc_reconciliation_and_historical_labeling.md`
     - `D10_wave_c_direction_note_lock.md`
     - `D11_repo_root_retention_triage.md`
     - `W-C-0_panel_expansion_and_runner_preflight.md`
     - `W-C-1a_final_mode_runtime_emission_fix.md`
     - `W-C-1_final_mode_telemetry_aggregation_helper.md`
     - `W-C-2_desktop_abstain_threshold_tuning.md`
     - `W-C-3_android_abstain_vector_mirror.md`
     - `W-C-4_uncertain_fit_upper_band_calibration.md`
     - `W-C-5a_low_coverage_canary_probe_and_closeout.md`
   - do **not** rotate them in this slice; just describe reality
3. **Dispatch-root trust section**
   - rewrite the section so it no longer claims the root is already fully trustworthy/clean after D8
   - make the nuance explicit: the root contains retained live historical records plus unrotated prompt drafts, so future cleanup/rotation needs a fresh inventory rather than trusting an old assumption
4. **ASCII cleanup**
   - repair any remaining mojibake / punctuation drift in the touched README text

Keep the file short and referential. This is a truthfulness resync, not a new policy note.

## Commit

Single commit only. Suggested subject:

```text
D12: resync dispatch README to current queue state
```

Tight equivalent is acceptable if it preserves the two core actions:
- dispatch README resync
- current queue/root-state truthfulness

## Acceptance

- One commit only.
- `git diff --name-only HEAD~1 HEAD` is limited to:
  - `notes/dispatch/README.md`
- `notes/dispatch/README.md` no longer says Wave C direction-note drafting is next.
- `notes/dispatch/README.md` no longer describes a D8-clean dispatch root; it acknowledges the current unrotated prompt set and points readers back to `notes/CP9_ACTIVE_QUEUE.md` for live ordering.
- No file moves or rotations happened.
- No file outside the explicit touch set changed.

## Delegation hints

- Good `gpt-5.4 high` worker slice: one-file doc truthfulness pass.
- If delegated, the worker should own the README rewrite, keep it short, mirror current queue truth without re-planning the backlog, make the single commit, and report the sha plus any still-stale adjacent docs noticed but left untouched.

## Anti-recommendations

- Do **not** touch `notes/CP9_ACTIVE_QUEUE.md` here just to add a breadcrumb.
- Do **not** rotate the untracked dispatch files.
- Do **not** turn this into a broader `notes/dispatch/` cleanup.
- Do **not** reopen Wave C planning or imply a new next slice that disagrees with the queue.
- Do **not** widen into the residual historical `notes/` backlog, screenshot review, or root-retention execution.

## Report format

- Commit sha + subject.
- Short summary of what changed in the README.
- Confirmation that no other files changed.
- Any adjacent stale docs noticed but intentionally left out of scope.
