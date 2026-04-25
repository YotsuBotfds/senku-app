# Pre-RC Documentation Drift Audit

Date: 2026-04-19
Slice: `D1_pre_rc_doc_audit`

Scope:
- Audit-only, read-only inspection across the requested docs.
- No emulator, APK, pack, model, bench, or code interaction.
- Output is this punch list only.

## Scout A - `AGENTS.md`

Scout A note: no stale `ui_review_20260417_gallery` baseline link drift was found. The linked 2026-04-17 gallery is current and resolves.

1. Windows venv activation path contradicts the text above it
File: `AGENTS.md:16-20`
Severity: `RC-cleanup`
Finding: the Quick Start text tells Windows users to create `venv_win`, but the PowerShell snippet immediately below activates `.\venv\Scripts\Activate.ps1`. That points back at the checked-in POSIX-origin env instead of the Windows env the doc just told the reader to create.
Proposed fix: change the snippet to `.\venv_win\Scripts\Activate.ps1`, or rename the example env to `venv` consistently in both lines.
Fix type: purely doc

## Scout B - `TESTING_METHODOLOGY.md`

1. The testing guide is missing the S1 / S1.1 reparity gate that current RC work depends on
File: `TESTING_METHODOLOGY.md` (document-wide omission); compare `notes/dispatch/README.md:21-35` and `notes/REVIEWER_BACKEND_TRACKER_20260418.md:29-32`
Severity: `RC-blocking`
Finding: the methodology never says that CP9 Stage 2 should anchor to `artifacts/cp9_stage1_reparity_20260419_183440/`, and it never records that Stage 0 v2/v3/v4/v5 artifacts are invalid because of APK/model mismatch. A validator using only this guide could pick the wrong artifact set and carry that mistake into the RC verdict.
Proposed fix: add a short "CP9 RC artifact discipline" section naming the reparity rollup as the valid precondition for Stage 2 and explicitly excluding the invalid pre-reparity artifact families.
Fix type: purely doc

2. The model-deploy section does not mention the current tablet host-fallback exception
File: `TESTING_METHODOLOGY.md:480-486`; compare `artifacts/cp9_stage0_20260419_142539/apk_deploy_v6/scope_note_tablet_host_fallback.md` (`## Tablet Host Fallback Scope Note`)
Severity: `RC-cleanup`
Finding: `Model Deploy Discipline` frames post-install model push as the default requirement for judging Android behavior, but the current CP9 Stage 0 tablet acceptance path explicitly allows host inference on `emulator-5554` and `emulator-5558` because of storage limits. Without a pointer to that scope note, RC readers can misread valid tablet evidence as deploy failure.
Proposed fix: add one sentence under `Model Deploy Discipline` or a short CP9 addendum pointing to the tablet host-fallback scope note as a documented temporary exception.
Fix type: purely doc

## Scout C - reviewer tracker drift

1. Root backlog and companion tracker disagree on whether Wave B is still open
File: `reviewer_backend_tasks.md:66-84,152-159`; compare `notes/REVIEWER_BACKEND_TRACKER_20260418.md:55-71,369-372,527-540`
Severity: `RC-blocking`
Finding: the root backlog still says Wave B has remaining `BACK-U-01` / `BACK-U-02` work and that Phase B-2 still has pending uncertainty items, while the companion tracker records `BACK-U-01`, `BACK-U-02`, and `BACK-U-03` as landed and says Wave B is closed and RC-gate-ready. RC planning cannot safely treat both docs as current.
Proposed fix: update the root backlog status snapshot and Phase B-2 framing to closed/landed, or add an explicit note that the companion tracker is authoritative for current Wave B status.
Fix type: purely doc

2. The root backend index still presents landed D/L/R work as if it were open backlog
File: `reviewer_backend_tasks.md:234-241,262-332,413-466,682-689`
Severity: `RC-cleanup`
Finding: the Quick Start section and multiple task bodies still read like active backlog for D-lane, L-lane, and `BACK-R-01` / `BACK-R-02` / `BACK-R-04`, even though the same file's state log marks those items done and the companion tracker says those lanes are closed. That makes the root index drift away from its own state log.
Proposed fix: mark landed task headers `[done ...]`, move closed task bodies into a landed appendix, or prune the Quick Start recommendations so they do not point at already-shipped work.
Fix type: purely doc

3. `BACK-R-01` still describes the desktop anchor-prior state incorrectly
File: `reviewer_backend_tasks.md:452-458`; compare `notes/REVIEWER_BACKEND_TRACKER_20260418.md:81-84`
Severity: `RC-cleanup`
Finding: the root row says desktop `query.py` has `ENABLE_ANCHOR_PRIOR = False`, but the companion tracker records desktop anchor-prior as productized and the current code is `ENABLE_ANCHOR_PRIOR = True`. That misstates the actual remaining gap, which is Android-side productization only.
Proposed fix: rewrite the `BACK-R-01` task body to reflect desktop `True`, Android `false`, and leave `BACK-R-05` as the remaining post-release decision.
Fix type: purely doc

4. `BACK-T-04` is still listed as an open follow-up in the companion tracker even though the root file marks it done
File: `notes/REVIEWER_BACKEND_TRACKER_20260418.md:557-559`; compare `reviewer_backend_tasks.md:618-624,692`
Severity: `RC-cleanup`
Finding: the companion tracker still tells the reader to file `BACK-T-04` post-release, but the root tracker and state log record it as done on 2026-04-19 with commit `2656311`.
Proposed fix: remove `BACK-T-04` from the companion tracker follow-up list and keep only still-open follow-ons.
Fix type: purely doc

5. New tracker-only follow-ons are missing from the root backend index
File: `notes/REVIEWER_BACKEND_TRACKER_20260418.md:413-478`; root `reviewer_backend_tasks.md` has no `BACK-T-05` / `BACK-P-05` / `BACK-P-06` / `BACK-P-07` entries
Severity: `post-RC`
Finding: the companion tracker now carries new follow-on backlog stubs that the supposed root backend index does not include. The two docs no longer index the same task surface.
Proposed fix: either promote those stubs into `reviewer_backend_tasks.md` or explicitly label them as tracker-local post-RC notes that are intentionally excluded from the root index.
Fix type: purely doc

## Scout D - `guideupdates.md` and `GUIDE_PLAN.md`

1. `guideupdates.md` keeps resolved history in a file that says not to do that
File: `guideupdates.md:3-36`
Severity: `RC-cleanup`
Finding: the file defines itself as "Open guide-level issues" and says not to keep resolved tracker noise, but it is almost entirely a resolved 2026-04-06 validation changelog and then ends with "No currently open guide-level issues."
Proposed fix: move the 2026-04-06 history into a dated note and leave `guideupdates.md` as a short active-defects tracker, even if that means the file is only a one-line "none open" status.
Fix type: purely doc

2. The "Current Active Queue" mixes true backlog with already-landed validation watchlist items
File: `GUIDE_PLAN.md` section `## Current Active Queue` (for example `GUIDE_PLAN.md:162-174`)
Severity: `RC-cleanup`
Finding: numbered queue slots include items explicitly labeled `guide landed; keep for retrieval validation only`. That blurs active sequencing with watchlist-only follow-up and makes the queue read as busier than the remaining guide-direction backlog actually is.
Proposed fix: split the section into `Active backlog` versus `Validation watchlist`, and keep landed validation-only items out of the numbered priority queue.
Fix type: purely doc

3. The guide plan still reads like a 2026-04-13 execution checklist rather than the current backlog
File: `GUIDE_PLAN.md:3-7,579-583`
Severity: `post-RC`
Finding: the status timestamp and "Suggested Sequence From Here" still speak to the 2026-04-13 guide wave (`current guide wave`, restart `wave_w`, `wave_x`, `wave_y`, restore embedding endpoint) even though later guide waves and validation passes are already reflected elsewhere in the repo.
Proposed fix: refresh the status timestamp and replace the old execution checklist with current next actions, or link that dated execution flow out to a dated validation note instead of leaving it inline as current backlog.
Fix type: purely doc

## Scout E - slice / dispatch hygiene

1. The D1 prompt points at a scope-note path that does not exist
File: `notes/dispatch/D1_pre_rc_doc_audit.md:61-66`
Severity: `RC-blocking`
Finding: the prompt instructs executors to use `apk_deploy_v6/scope_note_tablet_host_fallback.md`, but that path does not exist at repo root. The available note is under `artifacts/cp9_stage0_20260419_142539/apk_deploy_v6/scope_note_tablet_host_fallback.md`, so the prompt breaks one of its own inputs unless the executor manually searches for it.
Proposed fix: move or copy the scope note to a stable `notes/` path and update the prompt, or at minimum rewrite the prompt to the actual artifact path.
Fix type: purely doc

2. The dispatch README says the root dispatch folder should be current-only, but it still contains non-active slices
File: `notes/dispatch/README.md` sections `Active slices`, `Landed (not yet rotated)`, `Superseded - do not redispatch`, `Cancelled - not dispatched`
Severity: `RC-cleanup`
Finding: the README says a new dispatcher should be able to trust that every file in `notes/dispatch/` is current, but the directory still contains landed (`A1b`, `P3`, `P4`, `S1`), superseded (`A1`), and cancelled (`P5`) slice files alongside the actually active slices (`S2`, `D1`). That weakens the dispatch folder's advertised contract right before RC closure work.
Proposed fix: rotate non-active slice files into `completed/` or an `archived/` subdirectory immediately, or rename/prefix them so the root `notes/dispatch/` directory is truthfully active-only.
Fix type: purely doc

Scout E note: no duplicate or misnamed files were found under `notes/dispatch/completed/`; the hygiene drift is in the unrotated root-level slice set.

## Triage summary

Counts:
- `RC-blocking`: 3
- `RC-cleanup`: 8
- `post-RC`: 2

Recommended sequencing:
1. Fix the broken D1 scope-note reference so the dispatch prompt can be executed without manual repo archaeology.
2. Add the S1 / S1.1 reparity rule to `TESTING_METHODOLOGY.md` so RC-stage validators anchor to the right artifact family.
3. Reconcile the reviewer backend docs so Wave B closure status, open follow-ups, and anchor-prior framing are consistent before S3 closure consumes them.
4. Clean up the dispatch and guide trackers (`guideupdates.md`, `GUIDE_PLAN.md`, `notes/dispatch/README.md`) once the RC-blocking items above are corrected.
