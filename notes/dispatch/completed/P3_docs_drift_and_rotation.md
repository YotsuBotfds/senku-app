# Slice P3 — docs drift cleanup + P1/P2 rotation

- **Role:** main agent (`gpt-5.4 xhigh`). Delegates to Spark xhigh scout
  / `gpt-5.4 high` worker per `notes/SUBAGENT_WORKFLOW.md` and the
  inline tags below.
- **Parallel with:** S1, P4. No emulator / APK / model / pack touch.
- **Queue row:** `notes/CP9_ACTIVE_QUEUE.md` → On-deck P3.

## Preconditions

None.

## Outcome

One commit that

1. adds a Windows-venv note to the quick-start sections so fresh
   Windows checkouts don't dead-end on the missing
   `venv/Scripts/Activate.ps1` (P2's docs-drift follow-up), and
2. rotates the already-landed P1/P2 slice files into
   `notes/dispatch/completed/` per the dispatch README convention.

## The edits (subagent directive inline per step)

1. **`AGENTS.md` quick-start** *(`gpt-5.4 high` — small doc edit)*.
   The current POSIX / PowerShell block assumes a usable `venv/`.
   Add one or two terse sentences before the block (or appended to
   it) stating that the checked-in `venv/` is POSIX-origin — Windows
   contributors should create their own venv (e.g.
   `py -3 -m venv venv_win`) and activate that, or run under WSL.
   Keep it short; this is orientation, not a tutorial.

2. **`TESTING_METHODOLOGY.md`** *(`gpt-5.4 high` — small doc edit;
   batch with step 1 in the same commit)*.
   If a "Quick start" or "Environment" section exists, add the same
   note there. If not, fold it into the existing Model Deploy
   Discipline section or add a small "Environment" section adjacent
   to it.

3. **Rotate landed slices** *(main inline — `git mv` chain wants a
   consistent owner)*.
   Create `notes/dispatch/completed/` if it doesn't exist, then
   `git mv`:
   - `notes/dispatch/P1_planner_cleanup.md` →
     `notes/dispatch/completed/P1_planner_cleanup.md`
   - `notes/dispatch/P2_stage1_preflight.md` →
     `notes/dispatch/completed/P2_stage1_preflight.md`

4. **Update `notes/dispatch/README.md`** *(main inline — depends on
   step 3 completion)*.
   Remove the "Landed (not yet rotated)" block for P1/P2 (they're
   now in `completed/`). Leave the "Superseded" block for
   `A1_retry_5560_landscape.md` and the "Landed" entry for
   `A1b_pressback_harness_fix.md` in place — their rotation belongs
   to a later cleanup slice.

## Acceptance

- One commit with all edits above.
- `notes/dispatch/completed/P1_planner_cleanup.md` and
  `notes/dispatch/completed/P2_stage1_preflight.md` exist; the
  originals no longer exist at the old paths.
- `notes/dispatch/README.md` no longer lists P1 or P2 under Active
  or Landed.
- Windows-venv note visible in both `AGENTS.md` and
  `TESTING_METHODOLOGY.md`.

## Boundaries

- No emulator / APK / model / pack touch.
- No tracker edits — P4 owns those.
- Do not rotate `A1_retry_5560_landscape.md` or
  `A1b_pressback_harness_fix.md` — a later cleanup slice handles
  those.

## Report format

Reply with:

- Commit sha.
- `ls notes/dispatch/` and `ls notes/dispatch/completed/` outputs.
- The two docs notes verbatim (so Opus can eyeball the tone).
- Delegation log: which lane ran each step and a one-line "why."
