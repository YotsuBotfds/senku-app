# Slice D13 - relocate four durable repo-root docs into `notes/` and repair the live backlinks

- **Role:** main agent (`gpt-5.4 xhigh`). Execution/tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice moves files and updates the live queue / backlinks.
- **Why this slice now:** D11 already made the retention decisions, and D12 cleaned the stale dispatch README. The next narrow win is to execute the four `relocate-then-track` decisions from `notes/ROOT_RETENTION_TRIAGE_20260423.md` so the durable historical docs stop living at the repo root. This closes the already-decided part of the root-retention backlog without touching screenshots, zip archives, or the much larger `guides/` / historical-`notes/` surfaces.

## Outcome

One focused commit that:

1. Moves these four currently-untracked repo-root docs into `notes/`:
   - `CURRENT_LOCAL_TESTING_STATE_20260410.md` -> `notes/dated/CURRENT_LOCAL_TESTING_STATE_20260410.md`
   - `UI_DIRECTION_AUDIT_20260414.md` -> `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
   - `auditglm.md` -> `notes/reviews/auditglm.md`
   - `gptaudit4-21.md` -> `notes/reviews/gptaudit4-21.md`
2. Tracks them at the new paths with light historical framing where needed.
3. Repairs the small set of live tracked backlinks that should follow the move.
4. Updates the queue / retention note so the executed portion of D11 is reflected truthfully.

Important stability choice: keep the original basenames. The directory home is the framing improvement; do **not** add extra rename churn in this slice.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `7a7175f` (`D12: resync dispatch README to current queue state`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
   - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
   - `README_OPEN_IN_CODEX.md`
   - `notes/reviews/AUDITGLM_VERIFICATION_20260413.md`
   - `notes/reviews/AUDIT_REMAINING_ACTIONS_20260413.md`
   - `notes/specs/ui_direction_rev03.md`
   - `uiplanning/IMPLEMENTATION_LOG_20260413.md`
   - `uiplanning/UI_TODO_20260414.md`
   - `uiplanning/UI_TODO_NEXT_20260414.md`
3. These source docs exist at the repo root and are currently untracked:
   - `CURRENT_LOCAL_TESTING_STATE_20260410.md`
   - `UI_DIRECTION_AUDIT_20260414.md`
   - `auditglm.md`
   - `gptaudit4-21.md`
4. The destination paths do not already exist as tracked files:
   - `notes/dated/CURRENT_LOCAL_TESTING_STATE_20260410.md`
   - `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
   - `notes/reviews/auditglm.md`
   - `notes/reviews/gptaudit4-21.md`
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the large untracked `guides/` corpus
   - the large untracked historical `notes/` backlog
   - repo-root files intentionally left out of this slice:
     - `4-13guidearchive.zip`
     - `guides.zip`
     - `LM_STUDIO_MODELS_20260410.json`
     - `senku_mobile_mockups.md`
     - all `senku_*.png`

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - the four source docs above as moves into `notes/`
  - `notes/CP9_ACTIVE_QUEUE.md`
  - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
  - `README_OPEN_IN_CODEX.md`
  - `notes/reviews/AUDITGLM_VERIFICATION_20260413.md`
  - `notes/reviews/AUDIT_REMAINING_ACTIONS_20260413.md`
  - `notes/specs/ui_direction_rev03.md`
  - `uiplanning/IMPLEMENTATION_LOG_20260413.md`
  - `uiplanning/UI_TODO_20260414.md`
  - `uiplanning/UI_TODO_NEXT_20260414.md`
- Do **not** touch:
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
  - `4-13guidearchive.zip`
  - `guides.zip`
  - `LM_STUDIO_MODELS_20260410.json`
  - `senku_mobile_mockups.md`
  - any `senku_*.png`
  - any file under `guides/`
  - the broad historical `notes/` backlog
  - the orphan `.py` pair
  - the non-AGENTS `.ps1` bucket
- Do **not**:
  - delete any of the D11 delete-candidates
  - add `.gitignore` rules
  - create a broader archive/rotation plan
  - widen into screenshot review

## Move / edit rules

### Step 1 - relocate the four docs

These source files are currently untracked, so do **not** use `git mv`.
Use native PowerShell file moves (`Move-Item -LiteralPath ...`) or an
equivalent safe local move, then stage the new paths normally.

Move to these exact destinations:

- `CURRENT_LOCAL_TESTING_STATE_20260410.md` -> `notes/dated/CURRENT_LOCAL_TESTING_STATE_20260410.md`
- `UI_DIRECTION_AUDIT_20260414.md` -> `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
- `auditglm.md` -> `notes/reviews/auditglm.md`
- `gptaudit4-21.md` -> `notes/reviews/gptaudit4-21.md`

### Step 2 - add light historical framing to the moved docs

Keep edits minimal and factual.

Required guidance:

- `notes/dated/CURRENT_LOCAL_TESTING_STATE_20260410.md`
  - add a short note near the top that this is a historical bundle-era local-testing snapshot relocated from the repo root
  - make clear that the `/Users/tbronson/...` path examples are historical context, not the current quick-start
- `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
  - add a short note near the top that this is a historical review note relocated from the repo root
  - preserve the substantive audit text
- `notes/reviews/auditglm.md`
  - add a short note near the top that this is a historical audit relocated from the repo root
  - point readers to `AUDITGLM_VERIFICATION_20260413.md` as the verification companion
- `notes/reviews/gptaudit4-21.md`
  - add a short note near the top that this is a historical read-only audit relocated from the repo root

Do **not** rewrite the bodies beyond that framing.

### Step 3 - repair the live tracked backlinks

Update tracked references that should follow the move:

- `README_OPEN_IN_CODEX.md`
  - point the suggested first prompt at `notes/dated/CURRENT_LOCAL_TESTING_STATE_20260410.md`
- `notes/reviews/AUDITGLM_VERIFICATION_20260413.md`
  - point references at `notes/reviews/auditglm.md` or local same-folder `auditglm.md`
- `notes/reviews/AUDIT_REMAINING_ACTIONS_20260413.md`
  - same `auditglm.md` path repair
- `notes/specs/ui_direction_rev03.md`
  - point the supersedes line at `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
- `uiplanning/IMPLEMENTATION_LOG_20260413.md`
  - repair tracked references to `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
- `uiplanning/UI_TODO_20260414.md`
  - repair the audit-anchor reference to `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
- `uiplanning/UI_TODO_NEXT_20260414.md`
  - repair tracked references to `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`

Do **not** widen into untracked historical handoff notes just because they mention the old root paths.

### Step 4 - update queue / retention bookkeeping

Update these two tracked notes minimally:

- `notes/ROOT_RETENTION_TRIAGE_20260423.md`
  - add a short execution-status note that the four `relocate-then-track` items landed in D13 at their new paths
  - leave the `keep-local-only` and `delete-candidate` decisions intact
- `notes/CP9_ACTIVE_QUEUE.md`
  - reflect that the relocate-and-track portion of `(f)` and `(g)` is now complete
  - keep the unresolved parts truthful:
    - `LM_STUDIO_MODELS_20260410.json` remains local-only / no action now
    - `senku_mobile_mockups.md` remains coupled to the screenshot-aware follow-up / delete-candidate path
    - `4-13guidearchive.zip` and `guides.zip` remain under the zip-archive follow-up
  - append a completed-log entry for D13 summarizing the four doc relocations

Keep the queue change narrow. Do **not** reshuffle the broader backlog.

## Commit

Single commit only. Suggested subject:

```text
D13: relocate durable root docs into notes
```

Tight equivalent is acceptable if it preserves the two core actions:
- relocate the four durable root docs
- repair the live tracked references

## Acceptance

- One commit only.
- The four repo-root source docs no longer appear at the root.
- These four tracked destination files now exist:
  - `notes/dated/CURRENT_LOCAL_TESTING_STATE_20260410.md`
  - `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
  - `notes/reviews/auditglm.md`
  - `notes/reviews/gptaudit4-21.md`
- `README_OPEN_IN_CODEX.md` and the listed tracked review/UI files now point at the moved locations.
- `notes/ROOT_RETENTION_TRIAGE_20260423.md` records that the relocate-and-track items landed.
- `notes/CP9_ACTIVE_QUEUE.md` reflects the executed portion of D11 without reopening broader backlog planning.
- `4-13guidearchive.zip`, `guides.zip`, `LM_STUDIO_MODELS_20260410.json`, `senku_mobile_mockups.md`, and all screenshots remain untouched and untracked.
- No file outside the explicit touch set changed.

## Delegation hints

- Good `gpt-5.4 high` worker slice: bounded move + backlink repair + minimal queue bookkeeping.
- If delegated, the worker should own the four safe file moves, the light framing banners, the listed tracked backlink fixes, the narrow queue/triage-note update, and the single commit, then report the sha plus any historical stale references intentionally left out of scope.

## Anti-recommendations

- Do **not** delete `guides.zip` or `senku_mobile_mockups.md` here.
- Do **not** touch the screenshot bucket here.
- Do **not** rename the basenames; the folder home is enough framing for this slice.
- Do **not** widen into the giant historical `notes/` backlog just because some untracked handoffs still mention the old root paths.
- Do **not** touch `notes/dispatch/README.md`; D12 already handled that truth surface.

## Report format

- Commit sha + subject.
- The four final destination paths.
- Short list of tracked backlink files updated.
- Queue/retention-note summary.
- Any stale historical references noticed but intentionally left out of scope.
