# Slice D8 - track notes core, close sidecar YAML gap, and rotate current dispatch files

- **Role:** main agent (`gpt-5.4 xhigh`). Doc-only / tracking-hygiene only. Safe to delegate to a `gpt-5.4 high` worker if main wants to offload, but main owns the routing call.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice touches `notes/CP9_ACTIVE_QUEUE.md`, `notes/dispatch/README.md`, and many `notes/dispatch/` files. Keep it solo.
- **Why this slice now:** `R-track1` landed the atomic repo-root / scripts / tests / subproject sweep (`f9b48ca`) and explicitly left `(a)` sidecar YAML tracking plus `(c)` `notes/` content tracking as immediate carry-over. The highest-value next move is **not** "track all 149 untracked `notes/` files" - that is too broad. The right first cut is the **notes operating spine**: Wave C dependency notes, current workflow/index docs, `notes/specs/`, the small `handoffs/` / `research/` / `reviews/` subtrees, and dispatch-file cleanup/rotation. This closes the sidecar drift risk, makes the next Wave C drafting inputs durable, and shrinks the `notes/` backlog to a clearly historical residual.

## Outcome

One doc-only commit that:

1. Tracks the current **notes operating spine**:
   - root load-bearing notes used by current workflow / AGENTS / Wave C
   - all currently-untracked files under `notes/specs/`
   - all currently-untracked files under `notes/handoffs/`, `notes/research/`, and `notes/reviews/`
2. Validates the sidecar spec against the live deterministic registry with `python scripts/regenerate_deterministic_registry.py --check`.
3. Rotates the currently-landed `notes/dispatch/` slice files into `notes/dispatch/completed/`, and tracks the retained root dispatch files that should stay live at the root.
4. Updates `notes/CP9_ACTIVE_QUEUE.md` and `notes/dispatch/README.md` so:
   - sidecar YAML carry-over `(a)` is closed
   - the stale broad `notes/` carry-over `(c)` is narrowed to the **residual historical notes backlog**
   - the stale "Wave C planning ... blocked on RC v3 telemetry" line is repaired
   - dispatch-root status reflects the actual post-D8 file layout

Expected scope: roughly 45-60 paths, all under `notes/` plus the two tracked queue/readme files. No code, no pack, no emulator, no `.gitignore`.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `f9b48ca` (`R-track1` landed) or descends from it.
2. `git status --short -- notes/CP9_ACTIVE_QUEUE.md notes/dispatch/README.md` is clean before you start. If either file is already dirty, **STOP** and escalate.
3. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - repo-root triage items and `guides/` remain untracked
4. The following files exist and are currently untracked:
   - `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md`
   - `notes/ANDROID_INDEX.md`
   - `notes/GUIDE_INDEX.md`
   - `notes/SUBAGENT_WORKFLOW.md`
   - `notes/SWARM_INDEX.md`
   - `notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`
   - `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`
   - `notes/specs/deterministic_registry_sidecar.yaml`
5. `notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md` is currently **missing** even though `AGENTS.md` references it. Do **not** fabricate or recreate it in this slice. Report it as out-of-scope drift only.

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `notes/CP9_ACTIVE_QUEUE.md`
  - `notes/dispatch/README.md`
  - the explicit `notes/` files/subtrees listed below
- Do **not** touch:
  - `AGENTS.md`
  - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md`
  - any non-`notes/` file
  - any `guides/` file
  - any repo-root triage item (`4-13guidearchive.zip`, screenshots, dated snapshots, audit markdown)
  - any code, tests, pack assets, APKs, artifacts, or Android sources
- Do **not** widen from "notes operating spine" to the whole `notes/` root. Specifically **defer** the large residual root backlog such as:
  - `PLANNER_HANDOFF_*`
  - `CP9_STAGE_*`
  - `ACTIVE_WORK_LOG_*`
  - `AGENT_STATE.yaml`
  - `ANDROID_*` / `GUIDE_*` / `WAVE_*` historical worklogs not explicitly listed below
- Do **not** rotate `A1_retry_5560_landscape.md`, `P5_scope_note_landscape_phone.md`, or `probe_rain_shelter_mode_flip.md`.
- Do **not** rotate D8's own slice file in this commit. Same bootstrap constraint as prior D-slices.

## Track Set

### A. Root notes to TRACK (explicit list)

Track these seven files:

- `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md`
- `notes/ANDROID_INDEX.md`
- `notes/GUIDE_INDEX.md`
- `notes/SUBAGENT_WORKFLOW.md`
- `notes/SWARM_INDEX.md`
- `notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`
- `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`

These are the current load-bearing root notes for Wave C / workflow / indexes. If any are missing, **STOP**.

### B. Small notes subtrees to TRACK wholesale (currently-untracked files only)

Track all currently-untracked files under:

- `notes/handoffs/`
- `notes/research/`
- `notes/reviews/`
- `notes/specs/`

At slice-draft time this is:
- `notes/handoffs/` = 3 files
- `notes/research/` = 2 files
- `notes/reviews/` = 5 files
- `notes/specs/` = 15 untracked files

Do not modify contents; pure tracking only.

### C. Dispatch files to rotate + track

Rotate these root dispatch files into `notes/dispatch/completed/`:

- `notes/dispatch/D7_post_r_ret1b_tracker_reconciliation.md`
- `notes/dispatch/R-pack-drift1_forensic_investigation.md`
- `notes/dispatch/R-hygiene2_metadata_report_mobile_write_removal.md`
- `notes/dispatch/R-anchor-refactor1_pack_support_breakdown.md`
- `notes/dispatch/R-anchor-refactor1_SCOUT_PROMPT.md`
- `notes/dispatch/R-tool2_state_pack_logcat_capture.md`
- `notes/dispatch/R-tool2_SCOUT_PROMPT.md`
- `notes/dispatch/R-track1_core_entry_point_tracking_audit.md`
- `notes/dispatch/R-track1_SCOUT_PROMPT.md`

Track these retained root dispatch files **at the root** (do not rotate them):

- `notes/dispatch/A1_retry_5560_landscape.md`
- `notes/dispatch/P5_scope_note_landscape_phone.md`
- `notes/dispatch/probe_rain_shelter_mode_flip.md`

Track these already-in-`completed/` but still-untracked files in place:

- `notes/dispatch/completed/R-gate1_rerank_gate_decouple_from_route_focus.md`
- `notes/dispatch/completed/R-hygiene1_android_untracked_sweep.md`
- `notes/dispatch/completed/S3_cp9_closure_rc_v5_cut.md`
- `notes/dispatch/completed/T5_retrieval_ranking_telemetry.md`
- `notes/dispatch/completed/V-ret1-probe_bounded_state_pack_reprobe.md`

Important move rule:
- If the source file is tracked, use `git mv`.
- If the source file is untracked, use plain filesystem move (`Move-Item` / `mv`) and then `git add` the destination.
- Do **not** leave duplicate copies in both root and `completed/`.

## The edits

### Edit 1 - validate the sidecar spec before staging it

Run:

```powershell
python scripts/regenerate_deterministic_registry.py --check
```

Expected result: success with "deterministic registry is up to date".

If this fails, **STOP**. Do not "fix the YAML" or regenerate the registry in this slice. Report the failure and leave the sidecar carry-over open.

### Edit 2 - stage the notes operating spine

Add the explicit root-note list plus the currently-untracked files in:

- `notes/handoffs/`
- `notes/research/`
- `notes/reviews/`
- `notes/specs/`

No content edits. Pure tracking.

### Edit 3 - clean up `notes/dispatch/`

After the moves/tracking above, the intended root `notes/dispatch/` file set is:

- `README.md` (tracked, stays)
- `A1_retry_5560_landscape.md` (retained root record)
- `P5_scope_note_landscape_phone.md` (retained root record)
- `probe_rain_shelter_mode_flip.md` (retained root record)
- `D8_notes_core_tracking_sidecar_and_dispatch_rotation.md` (bootstrap; do not rotate in this slice)

`R-tool2_*`, `R-track1_*`, `R-pack-drift1_*`, `R-hygiene2_*`, `R-anchor-refactor1_*`, and `D7_*` should no longer remain at the dispatch root after this slice lands.

### Edit 4 - update `notes/CP9_ACTIVE_QUEUE.md`

Make these changes:

1. **Last updated** line:
   - advance it to D8 / notes-core tracking / sidecar closure

2. **Dispatch order cheat-sheet** and **Active** section:
   - keep "No slices currently in flight"
   - make "next" read as Wave C direction-note drafting plus the remaining post-`R-track1` follow-ups

3. **Post-RC Tracked / Blocked-Deferred cleanup**:
   - repair the stale line at current lines 55-56 that says:
     - `Wave C planning ... blocked on RC v3 telemetry`
   - telemetry already landed; Wave C is **not** blocked on telemetry now
   - either move Wave C into `Post-RC Tracked` as the next substantive direction, or reword the deferred line into a truthful residual note. Do **not** leave the stale blocker.

4. **Carry-over Backlog**:
   - close `(a)` sidecar YAML tracking (tracked + validated in this slice)
   - narrow `(c)` from "notes content-tracking slice" to the **residual historical notes backlog**
   - explicitly say the residual `notes/` backlog now means the large root dated/historical material still left untracked, such as planner handoffs, stage resumes, historical audits/logs, and similar root notes
   - leave `(b) guides`, `(d) zip triage`, `(e) screenshots`, `(f) dated snapshots`, `(g) audit markdown`, `(h)` orphan `.py`, and `(j)` non-AGENTS PowerShell follow-up intact

5. **Completed rolling log**:
   - append a D8 entry summarizing:
     - notes-core tracked
     - `notes/specs/deterministic_registry_sidecar.yaml` tracked and `--check` clean
     - dispatch root cleaned/rotated
     - `notes/` backlog narrowed from broad carry-over to residual historical root notes

### Edit 5 - update `notes/dispatch/README.md`

Make these changes:

1. **Active slices**:
   - keep "No slices are currently in flight"
   - keep the high-level post-RC summary current

2. **Landed (not yet rotated)**:
   - replace the current R-track1-era list
   - after D8, only D8's own file should be pending future rotation under the bootstrap constraint
   - retained root records (`A1_retry...`, `P5...`, `probe_rain_shelter_mode_flip...`) should be named as retained live files, not "pending rotation"

3. **Dispatch-root trust statement**:
   - after D8, `notes/dispatch/` should again read as a trustworthy live root: retained records + current bootstrap file only

### Edit 6 - commit

Single commit only. Suggested subject:

```text
D8: track notes core + sidecar spec + dispatch rotation
```

Tight equivalent is acceptable if it preserves the three actions: notes core, sidecar spec, dispatch rotation.

## Acceptance

- One commit only.
- `python scripts/regenerate_deterministic_registry.py --check` passes.
- The seven explicit root notes are tracked.
- All currently-untracked files under `notes/handoffs/`, `notes/research/`, `notes/reviews/`, and `notes/specs/` are tracked.
- The nine rotated dispatch-root files now exist only under `notes/dispatch/completed/`.
- The five already-in-`completed/` untracked files are now tracked in place.
- `notes/dispatch/README.md` reflects the post-D8 root accurately.
- `notes/CP9_ACTIVE_QUEUE.md`:
  - no longer shows sidecar YAML as open carry-over
  - no longer claims Wave C is blocked on RC v3 telemetry
  - narrows the `notes/` carry-over to the residual historical root backlog
- No file outside `notes/` changed.
- No code/tests/artifacts/emulator work performed.
- Final `git status --short` still shows the expected unrelated dirt and deferred trees, but **none** of the touched `notes/` paths remain unstaged/modified after commit.

## Delegation hints

- Good worker slice: doc-only, bounded path set, no runtime risk beyond the sidecar `--check`.
- If delegated, the worker should own the filesystem moves + tracker/readme reconciliation, then hand back the commit sha and residual out-of-scope drift.

## Anti-recommendations

- Do **not** broaden into the full root `notes/` backlog.
- Do **not** touch `guides/` in this slice.
- Do **not** touch zip/screenshot/dated-snapshot/audit-markdown triage items in this slice.
- Do **not** "fix" missing `notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md` by inventing a replacement. Report it only.
- Do **not** regenerate or rewrite `deterministic_special_case_registry.py`; this slice is `--check` only.
- Do **not** rotate D8's own slice file.
- Do **not** edit `AGENTS.md`, even though it currently references one missing note.

## Report format

Reply with:

- commit sha + subject
- `python scripts/regenerate_deterministic_registry.py --check` output
- files added/moved with `+X/-Y` counts
- rotation list executed
- final `git status --short` for the touched `notes/` paths
- post-commit `Get-ChildItem notes/dispatch -File | Select-Object Name`
- any out-of-scope drift noticed (especially the missing `notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md`)
- delegation log (main-inline or worker)
