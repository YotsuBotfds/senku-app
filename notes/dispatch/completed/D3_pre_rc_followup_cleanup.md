# Slice D3 — Pre-RC follow-up doc cleanup (D1/D2 deferred items)

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Parallel with:** R-pack
  (`notes/dispatch/R-pack_poisoning_chunk_rebuild.md`). Doc-only;
  does not touch any file R-pack writes (`ingest.py`,
  `mobile_pack.py`, `scripts/export_mobile_pack.py`,
  `db/senku_lexical.sqlite3`, `artifacts/mobile_pack/`).
- **Predecessor context:** D1 audit at
  `notes/PRE_RC_DOC_AUDIT_20260419.md` and D2 commit (`66567f7`)
  resolved the S3-consume-blocking findings. D3 absorbs four
  smaller deferred items in one commit so S3 closure stays focused
  on tracker updates and the AGENTS.md baseline bump.

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only:
  - `AGENTS.md`
  - `reviewer_backend_tasks.md`
  - `guideupdates.md`
  - Files inside `notes/dispatch/` (rotation moves only, no content
    edits)
  - One new file: `notes/dated/guide_validation_changelog_20260406.md`
    (or similar) for the moved `guideupdates.md` history
- Do NOT touch any file R-pack is currently editing (`ingest.py`,
  `mobile_pack.py`, `scripts/export_mobile_pack.py`,
  `db/senku_lexical.sqlite3`, anything under `artifacts/mobile_pack/`).
- Do NOT touch the `R-pack_poisoning_chunk_rebuild.md` slice file
  in `notes/dispatch/` — it is in flight.
- Do NOT touch any other in-flight slice file (none currently, but
  re-check `notes/dispatch/README.md` "Active slices" section
  before starting).
- No code changes anywhere. No commits except the single D3 commit.
- No emulator interaction.

## Outcome

A single commit resolving four small RC-cleanup items so S3 closure
doesn't have to absorb them.

## The edits

### Edit 1 — `AGENTS.md` venv path drift

**Audit finding:** D1 Scout A finding 1 (RC-cleanup) at
`AGENTS.md:16-20`.

The Quick Start text tells Windows users to create `venv_win`, but
the PowerShell snippet immediately below activates
`.\venv\Scripts\Activate.ps1` — pointing back at the checked-in
POSIX-origin env instead of the Windows env the doc just told the
reader to create.

Fix: align the snippet with the surrounding text. Two acceptable
shapes:
- Change `.\venv\Scripts\Activate.ps1` to
  `.\venv_win\Scripts\Activate.ps1` (matches the text's
  recommendation).
- OR rename the example env to `venv` consistently in both the
  text and the snippet.

Pick whichever produces less churn against the existing doc.

### Edit 2 — `reviewer_backend_tasks.md` `BACK-U-04` header drift

**D2 out-of-scope flag:** the `BACK-U-04` task header still reads
like open backlog even though the status prose says it is landed
(consistent with the existing `BACK-U-01` / `BACK-U-02` /
`BACK-U-03` headers D2 already marked as done).

Fix: mark the `BACK-U-04` header with `[done <date>]` matching the
convention used for the other Wave B `BACK-U-*` rows D2 closed.
Verify the date by grepping the file's state log (or git log) for
when `BACK-U-04` was actually completed; if there's no clear
landing date, use a defensible date like the abstain-baseline
bundle's `2026-04-18`.

Do NOT delete the task body — keep it as historical record.

### Edit 3 — `guideupdates.md` resolved-history move

**Audit finding:** D1 Scout D finding 1 (RC-cleanup) at
`guideupdates.md:3-36`.

The file defines itself as "Open guide-level issues" and says not
to keep resolved tracker noise, but it is almost entirely a
resolved 2026-04-06 validation changelog and ends with "No
currently open guide-level issues."

Fix:
- Move the 2026-04-06 history block to a new dated note at
  `notes/dated/guide_validation_changelog_20260406.md` (create
  `notes/dated/` if it doesn't exist; check first).
- Reduce `guideupdates.md` to its active-defects intent — leave
  a brief header explaining what the file is for, a one-line
  pointer to the moved history note, and a "No currently open
  guide-level issues" status line.

### Edit 4 — Slice file rotation to `notes/dispatch/completed/`

**Audit finding:** D1 Scout E finding 2 (RC-cleanup).

The README at `notes/dispatch/README.md` says a new dispatcher
should trust that every file in `notes/dispatch/` is current, but
the directory still carries landed slice files alongside the
actually active ones.

Move these landed slice files to `notes/dispatch/completed/`:
- `A1b_pressback_harness_fix.md`
- `P3_docs_drift_and_rotation.md`
- `P4_tracker_cleanup.md`
- `S1_stage1_rebuild.md`
- `D1_pre_rc_doc_audit.md`
- `D2_pre_rc_doc_fixes.md`
- `S2_stage2_validation_sweep.md`
- `T1_stage2_root_cause.md`
- `R-cls_query_metadata_profile_hardening.md`
- `R-eng_offline_answer_engine_gate.md`

DO NOT rotate (in-flight or kept in place per existing convention):
- `R-pack_poisoning_chunk_rebuild.md` (currently in flight per
  `notes/dispatch/README.md` Active slices section — STOP and
  re-check the README's Active list before moving anything; if
  R-pack has landed by the time you run, include it; if anything
  else is newly active, leave it)
- `A1_retry_5560_landscape.md` (superseded; existing convention
  retains in dispatch/ — leave in place per README)
- `P5_scope_note_landscape_phone.md` (cancelled; existing
  convention retains in dispatch/ — leave in place per README)

Use `git mv` so the moves are tracked as renames.

After moving, update `notes/dispatch/README.md`:
- Remove rotated entries from the "Landed (not yet rotated)"
  section.
- Add a one-line note that rotation was performed in D3.

## Acceptance

- All four edits land in a single commit. Use a clear commit
  message (e.g., `D3: pre-RC follow-up doc cleanup (D1/D2
  deferred items)`).
- `git status` shows the four scoped changes plus the rotation
  moves, no other edits.
- `notes/dispatch/` no longer contains the ten rotated files at
  its root level.
- `notes/dispatch/README.md` reflects the rotation.

## Report format

Reply with:
- Commit sha.
- Per-edit one-line confirmation (1 through 4).
- Number of slice files actually rotated (target 10 unless R-pack
  also landed and got included, making 11).
- Path to the new dated guide-validation note.
- Any audit finding you spotted as out-of-scope but worth flagging
  (don't fix).
- Delegation log.
