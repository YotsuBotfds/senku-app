# Slice D19 - delete the two orphan encoding helper scripts

- **Role:** main agent (`gpt-5.4 xhigh`). Execution/tracking slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** light read-only scouting is fine, but no parallel writer should touch `notes/CP9_ACTIVE_QUEUE.md`.
- **Why this slice now:** After D18, the smallest remaining actionable cleanup is the two orphan Python scripts deferred by R-track1 Rule 18. Current evidence says they have no live repo refs, no tests, no real CLI contract, and no tracked entrypoint role. `scripts/check_mojibake.py` is just a local mojibake-pattern printer; `scripts/scan_encoding.py` points at `scripts/guides` rather than the repo-root `guides/` tree, so salvaging it would widen into repair work instead of hygiene. The right next move is a tiny delete-only closeout.

## Outcome

One focused commit that:

1. Deletes these two untracked scripts:
   - `scripts/check_mojibake.py`
   - `scripts/scan_encoding.py`
2. Updates:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Closes carry-over backlog item `(h)` for the orphan `.py` Rule-18 defers.

Important constraint: this slice touches only the two orphan Python files plus the queue note. Do **not** widen into the larger non-AGENTS PowerShell follow-up, the residual historical `notes/` backlog, or the `guides/` tracking pass.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `7a5eec0` (`D18: delete superseded guides zip`) or descends from it.
2. Before you edit, this tracked file is clean in `git status --short`:
   - `notes/CP9_ACTIVE_QUEUE.md`
3. These files still exist and are currently untracked:
   - `scripts/check_mojibake.py`
   - `scripts/scan_encoding.py`
4. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - `4-13guidearchive.zip` remains untracked local-only
   - the large untracked `guides/` tree remains
   - the residual historical root `notes/` backlog remains

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `scripts/check_mojibake.py` (delete)
  - `scripts/scan_encoding.py` (delete)
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - any `.ps1` file under `scripts/`
  - any other `.py` file under `scripts/`
  - any file under `guides/`
  - `notes/ROOT_RETENTION_TRIAGE_20260423.md`
  - `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - repair or repurpose `scripts/scan_encoding.py`
  - widen into a script-audit tranche
  - create tests or replacement utilities
  - widen into the PowerShell follow-up

## Verify / delete / edit rules

### Step 1 - verify the scripts are still orphaned

Before deleting, do a quick read-only verification that the deletion premise is
still true.

Required checks:

1. Read both files once so you verify they still match the expected scratch
   utility shape.
2. Run a repo ref search for the script names, excluding noise surfaces like
   `notes/`, `artifacts/`, and local venvs. Desired result: no operational refs
   elsewhere in the repo.

If you find a real tracked caller, real test coverage, or evidence that one of
these files became an intentional supported utility, STOP instead of deleting.

### Step 2 - delete the two orphan scripts

These files are untracked, so do **not** use `git rm`.

Use native PowerShell deletion (`Remove-Item -LiteralPath ...`) or an
equivalent safe local delete for:

- `scripts/check_mojibake.py`
- `scripts/scan_encoding.py`

After deletion, confirm both paths are absent.

### Step 3 - update `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D19.
2. Close carry-over backlog item `(h)` rather than leaving it as deferred.
3. Make the closed wording explicit:
   - both orphan encoding helper scripts were deleted by D19
   - the Rule-18 orphan `.py` branch is now closed
4. Append a completed-log entry for D19 summarizing:
   - the two untracked scripts were verified orphaned and deleted
   - the queue no longer carries `(h)` as open backlog
5. Do **not** reshuffle unrelated backlog items.

## Commit

Single commit only. Suggested subject:

```text
D19: delete orphan encoding helper scripts
```

Tight equivalent is acceptable if it preserves the two core actions:
- delete the two orphan scripts
- close backlog item `(h)` in the queue

## Acceptance

- One commit only.
- `scripts/check_mojibake.py` is absent.
- `scripts/scan_encoding.py` is absent.
- `notes/CP9_ACTIVE_QUEUE.md` no longer carries `(h)` as pending/open backlog.
- No file outside the explicit touch set changed.

## Delegation hints

- Good `gpt-5.4 high` worker slice: tiny verify-delete-closeout pass.
- If delegated, the worker should own the orphan verification, the two deletes,
  the queue update, the single commit, and a clear note about any unexpected
  ref that would have blocked deletion (expected answer: none).

## Anti-recommendations

- Do **not** widen into the PowerShell follow-up.
- Do **not** try to fix `scripts/scan_encoding.py`.
- Do **not** touch the `guides/` tree.
- Do **not** broaden this into a general `scripts/` cleanup.

## Report format

- Commit sha + subject.
- Confirmation that both scripts were verified orphaned and deleted.
- Short summary of the queue update.
- Any blocker you checked for and did not hit.
