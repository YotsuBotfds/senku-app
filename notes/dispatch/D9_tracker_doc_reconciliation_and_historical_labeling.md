# Slice D9 - reconcile tracker docs, relabel stale live lanes, and repair tracker-surface mojibake

- **Role:** main agent (`gpt-5.4 xhigh`). Doc-only / tracker-surface cleanup only. Safe to delegate to a `gpt-5.4 high` worker, but main owns the routing call.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice touches the live queue plus the current index/tracker surface; keep it solo.
- **Why this slice now:** D8 (`ab28a2c`) landed the notes operating spine and cleaned the dispatch root, but the tracker audit still found three classes of doc drift:
  1. current-facing index docs still mix live authority with stale or untracked companion notes,
  2. two dated trackers still present themselves as live execution order even though they are historical lane records,
  3. mojibake / punctuation drift remains in the live tracker surface (`notes/CP9_ACTIVE_QUEUE.md`, `notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`, and nearby tracker docs).
  The goal here is to make the tracked "first stop" docs truthful and durable without widening into the full historical `notes/` backlog.

## Outcome

One doc-only commit that:

1. Reconciles the current-facing tracked docs so they point to truthful, durable anchors:
   - `notes/ANDROID_INDEX.md`
   - `notes/GUIDE_INDEX.md`
   - `notes/SWARM_INDEX.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
2. Relabels stale "live" trackers as historical lane records instead of active execution order:
   - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
   - `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`
3. Tracks the small set of companion docs that should stay durable in the tracker surface:
   - `notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`
   - `notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md`
   - `archive/README.md`
   - `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`
4. Repairs mojibake / punctuation damage in the touched tracker docs and leaves the live queue pointing at the right next substantive move after D9: Wave C direction-note drafting.

Expected scope: 9 files total, all docs only.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `ab28a2c` (`D8: track notes core + sidecar spec + dispatch rotation`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
   - `notes/ANDROID_INDEX.md`
   - `notes/GUIDE_INDEX.md`
   - `notes/SWARM_INDEX.md`
   - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
3. These companion docs exist and are currently untracked:
   - `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`
   - `notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`
   - `notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md`
   - `archive/README.md`
4. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the deferred `guides/` corpus and residual historical `notes/` backlog remain untracked
   - repo-root triage items (zip files, screenshots, dated snapshots, audit markdown) remain untracked
5. `notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md` is still missing while `AGENTS.md` references it. Do **not** recreate or fabricate it in this slice. Report it as out-of-scope drift only.

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only these files:
  - `notes/CP9_ACTIVE_QUEUE.md`
  - `notes/ANDROID_INDEX.md`
  - `notes/GUIDE_INDEX.md`
  - `notes/SWARM_INDEX.md`
  - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
  - `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`
  - `notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`
  - `notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md`
  - `archive/README.md`
- Do **not** touch:
  - `AGENTS.md`
  - `notes/dispatch/README.md`
  - `notes/dispatch/D8_notes_core_tracking_sidecar_and_dispatch_rotation.md`
  - any `guides/` file
  - any repo-root triage item (`4-13guidearchive.zip`, `guides.zip`, screenshots, dated snapshots, audit markdown)
  - any code, tests, artifacts, APKs, Android sources, or scripts
- Do **not** widen into the broader historical queue/handoff backlog. Specifically leave alone:
  - `PLANNER_HANDOFF_*`
  - `CP9_STAGE_*`
  - `ACTIVE_WORK_LOG_*`
  - `AGENT_STATE.yaml`
  - `OVERNIGHT_*`
  - `DAYLIGHT_*`
  - other dated Android/guide worklogs not named above
- Do **not** solve the tracker-surface problem by mass-tracking the entire Android or guide note backlog. Prefer truthful index rewrites plus the small companion track set above.

## The edits

### Edit 1 - track the small companion docs that should stay live

Track these four docs in place:

- `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`
- `notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`
- `notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md`
- `archive/README.md`

`notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md` and `archive/README.md` can be pure tracking if no text repair is needed. The other two are expected to need content edits before staging.

### Edit 2 - make `notes/ANDROID_INDEX.md` truthful as a first stop

Rework the doc so it clearly separates:

- live tracked anchors,
- current artifact references,
- historical/local context that is no longer the live source of truth.

Required content changes:

1. Update the "current review" surface so it no longer presents the 2026-04-17 review set as the current baseline. The current broad visual baseline is:
   - `artifacts/external_review/ui_review_20260421_retrieval_chain_closed/`
2. Keep the durable tracked anchors (`android-app/README.md`, testing docs, harness scripts, `notes/CP9_ACTIVE_QUEUE.md` where useful).
3. If you keep references to local dated Android notes or 2026-04-17 review execution notes, explicitly mark them as historical/local context rather than current authority.
4. Prefer removing or demoting direct links to untracked dated Android notes instead of widening the track set to absorb them.

### Edit 3 - make `notes/GUIDE_INDEX.md` truthful as a first stop

Rework the doc so it does **not** present untracked 2026-04-13 guide notes as the authoritative current guide spine.

Required content changes:

1. Keep the durable active anchors:
   - `../GUIDE_PLAN.md`
   - `../guideupdates.md`
   - `notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md` (tracked in this slice)
   - where helpful, the validation runner / methodology anchors already tracked elsewhere
2. If the older dated guide notes remain mentioned, label them as historical/local guide-lane context rather than the live truth source.
3. After your edit, any remaining **direct links** from `notes/GUIDE_INDEX.md` should point only to tracked files.

### Edit 4 - make `notes/SWARM_INDEX.md` durable

1. Track `archive/README.md` and keep `notes/SWARM_INDEX.md` pointing at it.
2. Confirm the doc reads coherently now that the OpenCode sidecar lane and generic engineering swarm layer are archived.
3. Do **not** widen into restoring or recreating `notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md`.

### Edit 5 - relabel stale live trackers as historical records

#### `notes/REVIEWER_BACKEND_TRACKER_20260418.md`

Required changes:

1. Add a clear banner near the top that this is a historical reviewer-backend closure record / lane snapshot, **not** the live execution-order source anymore.
2. Remove or rewrite the line that says "Use this file for live execution order and lane policy".
3. Keep the substantive closure record, landed-chain evidence, and post-RC context intact.
4. If useful, point readers to the actual live planner source (`notes/CP9_ACTIVE_QUEUE.md`) and/or the root task registry, but do not broaden into rewriting the whole backend plan stack.

#### `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`

Required changes:

1. Track the file.
2. Add a clear banner near the top that this is a historical 2026-04-17 mental-health routing lane tracker, not the live current queue.
3. Remove or rewrite the line that says "Use this file for live execution order and lane policy".
4. Keep the substantive lane notes intact; this is a relabel/repair pass, not a content rethink.

### Edit 6 - repair mojibake / punctuation drift in the touched tracker surface

At minimum, normalize the mojibake currently visible in:

- `notes/CP9_ACTIVE_QUEUE.md`
- `notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`
- `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
- `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`
- `archive/README.md` if touched

Use plain ASCII punctuation where practical (`-`, `->`, quoted text with straight quotes) rather than curly quotes, em dashes, or broken byte-sequence artifacts. This is a cleanup pass, not a wording expansion pass.

### Edit 7 - refresh `notes/CP9_ACTIVE_QUEUE.md` without reprioritizing the project

This file remains the live queue. Make only the tracker-surface-related updates needed after D9:

1. Advance the "Last updated" line to reflect D9.
2. Keep the truth that no slices are currently in flight unless your own execution state makes that false.
3. Keep Wave C direction-note drafting as the next substantive planner move after this docs cleanup, unless you find a stronger already-landed source proving otherwise.
4. Append a D9 completed-log entry summarizing:
   - tracker/index reconciliation,
   - historical relabeling of stale "live" trackers,
   - companion tracker docs tracked,
   - mojibake repair in the tracker surface.
5. Do **not** re-open closed carry-over that D8 already resolved.
6. Do **not** reshuffle unrelated post-RC backlog priority in this slice.

### Edit 8 - commit

Single commit only. Suggested subject:

```text
D9: reconcile tracker docs and relabel historical lanes
```

Tight equivalent is acceptable if it preserves the two core actions:
- tracker-doc reconciliation
- historical relabeling

## Acceptance

- One commit only.
- The four companion docs are tracked:
  - `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`
  - `notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`
  - `notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md`
  - `archive/README.md`
- `notes/ANDROID_INDEX.md` no longer presents the 2026-04-17 review set as the current baseline; it points readers at the 2026-04-21 retrieval-chain-closed gallery/artifact set as current.
- `notes/GUIDE_INDEX.md` no longer presents untracked dated guide notes as the authoritative current guide spine.
- `notes/SWARM_INDEX.md` points at a tracked `archive/README.md`.
- `notes/REVIEWER_BACKEND_TRACKER_20260418.md` and `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md` both carry explicit historical/not-live labeling.
- `notes/CP9_ACTIVE_QUEUE.md` remains the live planner source and records D9 in the completed log.
- The following grep returns **no matches** across the touched files:

```powershell
rg -n '[^\x00-\x7F]' `
  notes/CP9_ACTIVE_QUEUE.md `
  notes/ANDROID_INDEX.md `
  notes/GUIDE_INDEX.md `
  notes/SWARM_INDEX.md `
  notes/REVIEWER_BACKEND_TRACKER_20260418.md `
  notes/APP_ROUTING_HARDENING_TRACKER_20260417.md `
  notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md `
  notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md `
  archive/README.md
```

- No file outside the explicit touch set changed.
- Final `git status --short` still shows the expected unrelated dirt and deferred trees, but none of the touched files remain unstaged/modified after commit.

## Delegation hints

- Good worker slice: doc-only, bounded path set, no runtime work.
- If delegated, the worker should own the index rewrites, historical banners, companion-doc tracking, mojibake cleanup, and the final queue log entry, then hand back the commit sha plus any still-outstanding drift.

## Anti-recommendations

- Do **not** broaden into the full historical `notes/` backlog.
- Do **not** track the whole Android note stack just because `notes/ANDROID_INDEX.md` mentions it; rewrite the index truthfully instead.
- Do **not** track the whole 2026-04-13 guide note family just because `notes/GUIDE_INDEX.md` mentioned it; only the prompt-validation queue is in scope to track here.
- Do **not** touch `AGENTS.md` or try to repair its missing `notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md` link in this slice.
- Do **not** touch `notes/dispatch/README.md` or rotate any dispatch files in this slice.
- Do **not** re-rank the broader post-RC backlog. This is a tracker-surface truthfulness pass, not a planning reset.

## Report format

- Commit sha + subject.
- Files changed and which of the four companion docs were newly tracked.
- Short verification bullets:
  - current Android review reference updated
  - guide index authority cleaned up
  - historical banners added
  - mojibake grep clean
- Out-of-scope drift still remaining after D9.
