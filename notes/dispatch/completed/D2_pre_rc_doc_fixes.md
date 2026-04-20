# Slice D2 — Pre-RC documentation fixes (S3-consume blockers)

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Parallel with:** S2 (`notes/dispatch/S2_stage2_validation_sweep.md`).
  Doc-only; does not touch emulators, APKs, packs, test code, or any
  RC-pipeline state.
- **Predecessor:** D1 audit landed at
  `notes/PRE_RC_DOC_AUDIT_20260419.md`. This slice resolves the
  audit findings that S3 will consume. Defers post-RC findings and
  audit findings already partially handled.
- **Queue row:** parallel work; planner adds an entry on dispatch.

## Context

D1's audit found three RC-blocking and several RC-cleanup items.
Two pre-conditions have already been handled inline by planner:

1. **D1's broken scope-note path** — patched.
2. **Scope-note promotion to a stable `notes/` path** — done. The
   canonical reference is now
   `notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`. The original Stage 0
   artifact at
   `artifacts/cp9_stage0_20260419_142539/apk_deploy_v6/scope_note_tablet_host_fallback.md`
   stays in place as evidence; new doc references should use the
   `notes/` path.

The remaining audit findings that S3 (CP9 closure + RC cut) will
consume must be reconciled before S3 runs. That is this slice's
scope. Read `notes/PRE_RC_DOC_AUDIT_20260419.md` first for the full
audit context.

## Boundaries (HARD GATE — STOP if you would violate)

- No emulator interaction. Do not run any `scripts/run_android_*`
  script or any `adb` command. Do not touch the four-serial matrix.
- No APK, pack, model, or `android-app/app/build/` interaction.
- No code changes — neither `src/main` nor `src/androidTest`.
- Only the three files named in the edits below may be modified.
  Do not opportunistically clean up other files even if you spot
  drift while editing — flag it in your final response and let
  planner triage.
- No re-ingest, no bench rerun, no desktop validation.

## Outcome

Three doc files updated and committed in a single commit:
- `TESTING_METHODOLOGY.md` — adds the CP9 RC artifact discipline
  section + tablet host-fallback exception pointer.
- `reviewer_backend_tasks.md` — Wave B status reconciled, landed
  D/L/R bodies marked / pruned, `BACK-R-01` corrected.
- `notes/REVIEWER_BACKEND_TRACKER_20260418.md` — `BACK-T-04` removed
  from the post-release follow-up list.

## The edits

Read `notes/PRE_RC_DOC_AUDIT_20260419.md` for full per-finding
context (severity, proposed fix, line references). Below are the
specific edits to land. Each edit names the audit finding(s) it
resolves so Codex can verify scope.

### Edit 1 — `TESTING_METHODOLOGY.md`: CP9 RC artifact discipline section

**Audit finding:** Scout B finding 1 (RC-blocking).

Add a new short section titled `CP9 RC artifact discipline` (placed
near the existing `Model Deploy Discipline` section, or in a logical
spot in the doc structure) that:

- Names
  `artifacts/cp9_stage1_reparity_20260419_183440/pack_build.json`
  as the authoritative Stage 1 rollup for RC v3 validation work
  (Stage 2, RC, etc.). Note that the original S1 dir
  `artifacts/cp9_stage1_20260419_181929/` is the predecessor and is
  retained for evidence but has `apk_sha_homogeneous: false` and
  must NOT be used as the S2 precondition input.
- Excludes Stage 0 v2 / v3 / v4 / v5 artifact families as invalid
  for RC validation (each was superseded by Stage 0 v6 / v6b for
  documented reasons in
  `artifacts/cp9_stage0_20260419_142539/summary.md`). Future RC
  validators should anchor to v6b artifacts (the `_v6b` suffix in
  the smoke artifact dir naming) and the reparity rollup.
- Mentions the apk_sha homogeneity check as part of the gate (per
  the S1.1 reparity learning: any code change that triggers a
  rebuild on a single serial during a stage must be followed by a
  four-serial APK parity check before claiming the stage GREEN).

Keep the section tight — half a page is plenty. Cross-link
`notes/CP9_ACTIVE_QUEUE.md` Completed log for the underlying
reasoning trail.

### Edit 2 — `TESTING_METHODOLOGY.md`: tablet host-fallback pointer

**Audit finding:** Scout B finding 2 (RC-cleanup).

In the existing `Model Deploy Discipline` section
(`TESTING_METHODOLOGY.md:480-486` per the audit), add one sentence
or short bullet noting that `emulator-5554` / `emulator-5558` are
running under a documented temporary host-inference exception and
pointing readers at `notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`.

This prevents RC readers from misreading valid tablet evidence as
deploy failure.

### Edit 3 — `reviewer_backend_tasks.md`: Wave B status reconciliation

**Audit finding:** Scout C finding 1 (RC-blocking) at
`reviewer_backend_tasks.md:66-84,152-159`.

Update the root backlog status snapshot and Phase B-2 framing so
Wave B reads as closed and RC-gate-ready, matching what
`notes/REVIEWER_BACKEND_TRACKER_20260418.md:55-71,369-372,527-540`
already records.

Two acceptable approaches (pick whichever produces less churn):
- (A) Edit the root status snapshot and Phase B-2 sections directly
  to mark `BACK-U-01` / `BACK-U-02` / `BACK-U-03` landed and Wave B
  closed.
- (B) Add an explicit "Authoritative status: see
  `notes/REVIEWER_BACKEND_TRACKER_20260418.md`" pointer near the
  top of the root backlog status snapshot and update only the
  highest-leverage misleading bits (the headline "Wave B remaining"
  framing).

Approach A is preferred if the deltas are small. Use B only if A
balloons the diff.

### Edit 4 — `reviewer_backend_tasks.md`: landed D/L/R work pruning

**Audit finding:** Scout C finding 2 (RC-cleanup) at
`reviewer_backend_tasks.md:234-241,262-332,413-466,682-689`.

Mark landed D-lane, L-lane, and `BACK-R-01` / `BACK-R-02` /
`BACK-R-04` task headers with `[done <date>]` (matching the
existing convention if there is one — check the file's state log
section for prior `[done ...]` formatting). Either move closed task
bodies into a landed appendix, OR leave them in place but add the
`[done]` marker. Do NOT delete the bodies — they are the historical
record.

Update the root file's Quick Start recommendations so they no
longer point readers at already-shipped work.

### Edit 5 — `reviewer_backend_tasks.md`: `BACK-R-01` desktop anchor-prior fix

**Audit finding:** Scout C finding 3 (RC-cleanup) at
`reviewer_backend_tasks.md:452-458`.

Rewrite the `BACK-R-01` task body to reflect:
- Desktop `query.py`: `ENABLE_ANCHOR_PRIOR = True` (productized).
- Android: `false` (still pending productization).
- Remaining gap: Android-side productization only; `BACK-R-05`
  remains as the post-release decision.

Verify the actual current desktop value yourself by reading
`query.py` before writing the new task body — do not trust either
tracker if they conflict with the code.

### Edit 6 — `notes/REVIEWER_BACKEND_TRACKER_20260418.md`: `BACK-T-04` removal from follow-ups

**Audit finding:** Scout C finding 4 (RC-cleanup) at
`notes/REVIEWER_BACKEND_TRACKER_20260418.md:557-559`.

Remove `BACK-T-04` from the post-release follow-up list since the
root tracker and state log record it as done on 2026-04-19 (commit
`2656311`). Verify the commit exists (`git log 2656311 -1`) before
removing — don't delete based on unverified state.

## Deferred (do NOT address in this slice)

- Scout A finding 1 (`AGENTS.md` venv path drift) — RC-cleanup, not
  S3-consume-blocking. Defer to S3 closure pass or post-RC.
- Scout C finding 5 (new tracker stubs missing from root) — audit
  classified as post-RC. Defer.
- Scout D findings 1, 2, 3 (`guideupdates.md` and `GUIDE_PLAN.md`)
  — not S3-consume-blocking. Defer to post-RC.
- Scout E finding 2 (slice file rotation to `completed/`) — hygiene,
  not S3-consume-blocking. Planner-side or S3 closure.

If you spot a finding outside the audit while editing, flag it in
your report and do not edit it.

## Acceptance

- All six edits landed in a single commit. Use a clear commit
  message naming the audit findings resolved (e.g., "docs: resolve
  D1 audit RC-blocking + RC-cleanup findings").
- `git status` after the commit shows clean working tree (no
  unrelated edits).
- `notes/PRE_RC_DOC_AUDIT_20260419.md` is NOT modified — the audit
  doc is preserved as the historical finding record.
- No emulator, APK, pack, or model interaction during the slice.

## Report format

Reply with:
- Commit sha.
- Per-edit one-line confirmation (1 through 6).
- Verified `BACK-R-01` desktop value (what you read in `query.py`).
- Verified `BACK-T-04` commit (`2656311` exists / does not exist).
- Any audit finding you spotted as out-of-scope but worth flagging
  to planner.
- Delegation log.
