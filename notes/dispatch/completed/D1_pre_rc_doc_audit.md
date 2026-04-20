# Slice D1 — Pre-RC documentation drift audit

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Parallel with:** S2 (`notes/dispatch/S2_stage2_validation_sweep.md`).
  Both can run concurrently — this slice is doc-only and does not
  touch emulators, APKs, packs, test code, the desktop bench, or
  any RC-pipeline state.
- **Queue row:** parallel work; planner adds an entry on dispatch.

## Context

S2 (Stage 2 RC validation sweep) is in flight. Before S3 (CP9 closure
+ RC cut) lands, we want a single punch list of every documentation
drift, stale reference, or doc inconsistency that has accumulated
during CP9, so S3 can absorb fixes into the closure pass rather than
leaving them as post-RC follow-ups.

This is **audit-only**. Produce the punch list. Do NOT make any of
the fixes yet. Planner will triage each finding into RC-blocking vs
post-RC and dispatch follow-up slices as needed.

## Boundaries (HARD GATE — STOP if you would violate)

- No emulator interaction. Do not run any `scripts/run_android_*`
  script or any `adb` command. Do not touch the four-serial matrix.
- No APK, pack, model, or `android-app/app/build/` interaction.
- No code changes — neither `src/main` nor `src/androidTest`. No
  commits.
- No edits to any tracker, queue, README, or doc file other than the
  audit doc itself. This slice WRITES exactly one file and READS
  many. The one written file is the punch list.
- No re-ingest, no bench rerun, no desktop validation. Read-only
  inspection of `query.py` / `ingest.py` is fine if needed for
  context.

If a finding requires running a command to verify, document the
command in the punch list with "verification needed" rather than
running it.

## Outcome

A single punch-list doc at `notes/PRE_RC_DOC_AUDIT_20260419.md`
enumerating every documentation drift, stale reference, or doc
inconsistency caught during the audit. Each finding tagged with
severity and a proposed fix.

## The audit (parallel scout fan-out)

**Dispatch parallel `gpt-5.3-codex-spark xhigh` scouts for the
per-file scans below.** Each scout reads its assigned file(s) and
returns a structured finding list. Scouts run concurrently — the
file scans are independent. Main agent synthesizes the rolled-up
punch list after scouts return.

If you cannot dispatch parallel scouts (rate cap, SDK error),
report the reason in your final response and run the per-file scans
sequentially in the main lane — do not skip any scout assignment.

### Scout assignments

1. **Scout A — `AGENTS.md`.** Find: stale baseline references (the
   `external_review/ui_review_20260417_gallery/` link is current as
   of dispatch; anything older is stale), references to slices /
   scripts / files that have been renamed or removed, framing that
   conflicts with the tablet host-inference scope cut formalized in
   `artifacts/cp9_stage0_20260419_142539/apk_deploy_v6/scope_note_tablet_host_fallback.md`,
   "Wave B is code-complete" phrasing that may need tightening for RC.

2. **Scout B — `TESTING_METHODOLOGY.md`.** Find: outdated procedures
   (e.g., APK or pack workflows that have changed), missing
   references to the Model Deploy Discipline section P1 added,
   missing references to S1 / S1.1 reparity workflow (likely missing
   — note explicitly), inconsistencies with `AGENTS.md`'s Quick
   Start section.

3. **Scout C — Reviewer tracker drift.** Read BOTH top-level
   `reviewer_backend_tasks.md` AND
   `notes/REVIEWER_BACKEND_TRACKER_20260418.md`. Find: rows present
   in one but not the other, rows whose status differs, sections
   whose framing has diverged. Carry-over item already known —
   audit's job is to enumerate the specific deltas, not consolidate.

4. **Scout D — `guideupdates.md` and `GUIDE_PLAN.md`.** Find: stale
   entries (concrete defects that have been fixed but not removed
   from `guideupdates.md`), references to retrieval issues that may
   have been resolved by Wave B work, drift between the two docs.

5. **Scout E — Slice / dispatch hygiene.** Read
   `notes/dispatch/README.md` and `notes/dispatch/completed/`. Find:
   slice files in `notes/dispatch/` that have landed but not been
   rotated to `completed/`, slice files in `completed/` that are
   misnamed or duplicated, README references that point at slice
   files that no longer exist. Note: planner has just updated the
   README to reflect S1 / S1.1 / A1b / P3 / P4 as landed; the
   physical slice files in those names have not yet been moved to
   `completed/`. Flag that as a Scout E finding rather than fixing
   it.

### Synthesis (main inline after scouts return)

Combine all scout outputs into `notes/PRE_RC_DOC_AUDIT_20260419.md`
with one section per scout. Each finding has:

- File path + line range (or section name).
- Severity:
  - **RC-blocking** — a doc claim that contradicts what RC v3
    actually ships (e.g., wrong APK sha referenced, wrong scope-cut
    framing).
  - **RC-cleanup** — worth fixing in S3 closure for tidiness, but
    not blocking the cut.
  - **post-RC** — can wait until after the RC cut lands.
- Proposed fix in one or two sentences.
- Whether the fix is purely doc (planner-side) or requires
  code/script changes (follow-up slice needed).

End the doc with a **Triage summary** section: counts per severity,
plus recommended sequencing for RC-blocking and RC-cleanup items.

## Acceptance

- `notes/PRE_RC_DOC_AUDIT_20260419.md` exists, structured per the
  synthesis section above.
- Findings cover all five scout assignments. If a scout returns "no
  findings," report it explicitly as a finding — do not omit the
  section.
- Zero changes to any file other than the new audit doc. `git
  status` shows only `?? notes/PRE_RC_DOC_AUDIT_20260419.md` plus
  any pre-existing unrelated dirty state.
- No emulator, APK, pack, or model interaction during the slice.

## Report format

Reply with:

- Path to the audit doc.
- Counts per severity (RC-blocking / RC-cleanup / post-RC).
- Top 3 RC-blocking findings, one line each.
- Delegation log: which scouts ran in parallel, any that fell back
  to main inline, why.
- Whether anything caught in the audit needs immediate planner
  attention (i.e., a finding so severe that S2's verdict could be
  invalidated by what you found).
