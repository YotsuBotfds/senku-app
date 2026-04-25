# CHECKPOINT 9 Plan — Release-Candidate Gate

Date: 2026-04-19
Author: Opus planner
Status: draft for Tate review

---

## Purpose

Sequence the work required to close the CP9 release-candidate gate
now that Wave B (`BACK-U-01` / `BACK-U-02` / `BACK-U-03`) is landed
and the 2026-04-19 daylight hygiene queue is clean.

This plan is the bridge from "Wave B is closed" to "the app is ready
to ship." It does not define post-ship work (`BACK-R-05` Android
anchor-prior productization, etc.) — those live in their own
trackers.

---

## What CP9 Means

From existing project state:

- **CP9 = release-candidate gate** (`opustasks.md` line 70, multiple
  references in `reviewer_backend_tasks.md` and
  `REVIEWER_BACKEND_TRACKER_20260418.md`)
- **Requires playtest sign-off** (`opustasks.md` line 70)
- **Independent of OPUS-E-05** (the DetailActivity ≤1500 line-count
  refactor stays deferred; CP9 does not block on it, per
  `answer_presenter_extraction_spec.md` lines 26-27 and 249-251)
- **Final non-code steps are reviewer sign-off and release APK build**
  (`opustasks.md` Status Snapshot). **For this personal build, the
  signed release APK step is out of scope** — this is personal use,
  not a production channel. Optional artifact: one unsigned/debug APK
  archived alongside the v3 packet for personal record-keeping.

Translation: CP9 closes when the four-emulator matrix packet reflects
current `HEAD`, the external-review gallery is fresh, a playtest pass
is on record, and reviewer sign-off is captured.

---

## Current State Against That Gate

**Landed toward CP9:**

- Wave B backend complete: `BACK-U-01` (`eb398dc`), `BACK-U-02`
  (`d974ebc`), `BACK-U-03` (`af49d91`)
- Last green RC packet: `artifacts/ui_state_pack_release_candidate_v2/20260418_200902`
  — **45/45 across the four-emulator matrix, but built before Wave B
  landed**
- Last external-review gallery:
  `artifacts/external_review/ui_review_20260418_201903_gallery/` —
  **same staleness issue**
- Phase D guide wave targeted reruns green
- OPUS-E-06 landed (`b41128a`)
- 2026-04-19 daylight hygiene queue closed 3/3 — tree posture clean
  and gitignore, note-cell hygiene rule, no-op commit rule, and
  launcher wrapper Phase 1 all in place

**Missing toward CP9:**

1. **Fresh RC packet on post-Wave-B HEAD.** The current RC v2 packet
   does not include uncertain-fit mode (U-01), safety-critical
   escalation (U-02), or confidence-label plumbing (U-03). A new
   four-emulator matrix run is required. Call this the **RC v3
   packet**.

2. **Fresh external-review gallery built from the v3 packet.**
   Currently the gallery points at v2 pack assets.

3. **Playtest pass on record.** Playtest procedures, checklist, and
   artifacts need to be updated to cover Wave B behaviors:
   - Uncertain-fit card variant (warning wash, `UNSURE FIT` pill,
     `!` icon)
   - Safety-critical escalation line above "Closest matches in the
     library" on abstain
   - Three-way mode distinction (confident / uncertain-fit / abstain)
     visible in UI

4. **Reviewer sign-off.** Manual step — the backend reviewer signs
   off on Wave B behaviors against the v3 packet and gallery.

5. **CP9 release note.** Written record of what this RC contains,
   what's deferred, and what's known-outstanding (C-09, E-05,
   BACK-R-05).

---

## Known Deferrals (Not CP9 Blockers)

- **OPUS-E-05** — DetailActivity ≤1500 line-count refactor. Stays
  paused. CP9 ships without it per the E-06 spec's explicit scope
  statement.
- **OPUS-C-09** — Stale per-device install-state cache causing false
  `skip_install=true` path. Logged as post-release follow-up in
  `parallelism_plan.md` line 44-45.
- **BACK-R-05** — Android anchor-prior productization decision.
  Post-release, awaiting scout decision.

If any of these should be reconsidered as CP9 blockers, flag before
the plan kicks off.

---

## Plan of Attack (Sequenced)

### Stage 0 — Pre-rebuild verification (Codex-executable, gating)

Before rebuilding the RC packet, confirm the system state on which
Stage 1 will run actually reflects Wave B. Without Stage 0, Stage 1
could rebuild the packet against a pre-Wave-B mobile pack and report
green while not testing Wave B at all.

Sub-steps (each gating — if any fails, stop and investigate before
proceeding):

1. **Desktop re-ingest** (`python3 ingest.py --stats`) — refreshes
   ChromaDB with current ingest logic, exercises the BACK-P-03
   bridge-tag audit against the 754-guide corpus. Any audit failure
   is a real corpus regression, not something to paper over.

2. **Desktop bench rerun** against `test_prompts.txt` — catches any
   retrieval quality regression from the P-03 guide metadata fixes
   (`2f664bd`). Regression floor: no section drops more than one
   pass vs baseline.

3. **Mobile pack re-export** (`python3 scripts/export_mobile_pack.py`)
   — rebuilds the mobile pack from post-Wave-B desktop state. Record
   pack hash.

4. **Launch emulator matrix** via
   `scripts/start_senku_emulator_matrix.ps1`. Confirm all four
   emulators (5556, 5560, 5554, 5558) up via `adb devices`.

5. **Mobile pack hot-swap** to all four emulators via
   `scripts/push_mobile_pack_to_android.ps1`.

6. **Wave B live-on-device smoke.** On each emulator, run the
   reviewer-worked example (`He has barely slept, keeps pacing and
   muttering to himself, and refuses to eat. What should we do?`).
   Every emulator must render the uncertain-fit variant (UNSURE FIT
   pill, warning wash, `!` icon). If any emulator does not, the pack
   push didn't take — Stage 1 is invalid until fixed.

7. **Fallback uncertain-fit query identification.** Run candidate
   queries until one reliably triggers uncertain-fit as a fallback
   in case the primary reviewer example is fragile under rebuild
   conditions. Document in
   `artifacts/cp9_stage0_<timestamp>/fallback_query.md`.

No code commits for Stage 0 — pure verification. Artifacts live under
`artifacts/cp9_stage0_<timestamp>/` with a summary.md that declares
Stage 0 green or red.

**Time:** 1-2 hours including emulator boot + bench rerun.

### Stage 1 — RC v3 packet rebuild (Codex-executable)

Rebuild the four-emulator matrix packet against current HEAD
(`65252f7`, post-daylight-queue). Must include:

- Phone portrait (`5556`)
- Phone landscape (`5560`)
- Tablet portrait (`5554`)
- Tablet landscape (`5558`)

Coverage must include at minimum:

- Abstain card (no-match path, with the U-02 safety-critical
  escalation line shown when applicable)
- **Uncertain-fit card** (new in Wave B — `UNSURE FIT` pill, warning
  wash, `!` icon). Use the reviewer-worked example from the U-01
  addendum (`He has barely slept, keeps pacing...`) to trigger it
  reliably.
- Confident answer card (normal path)
- Confidence-label chip variants (U-03 plumbing, visible when the
  label is MEDIUM or LOW)

Deliverable: `artifacts/ui_state_pack_release_candidate_v3/<timestamp>/`
with a `summary.json` reporting `NN / NN` pass and no regression vs
v2 baseline.

**Scenario list expansion (mandatory).** The existing parallel pack
script's scenario list is pre-Wave-B. Stage 1 must explicitly add:

- `uncertain_fit_reviewer_example` (primary trigger from spec)
- `uncertain_fit_fallback` (query identified in Stage 0 Step 7)
- `safety_critical_abstain_with_escalation` (U-02 primary path)
- `safety_critical_uncertain_fit_with_escalation` (U-01 + U-02
  interaction — same helper, different mode)
- `confidence_label_medium` (U-03 MEDIUM label chip render)
- `confidence_label_low` (U-03 LOW label chip render)

Existing Phase 8 scenarios (confident answer, no-match abstain,
follow-up session flow, etc.) remain in the sweep as regression
floor.

**Regression definition.** A regression is any scenario that was
PASS in v2 and is FAIL in v3 without a documented Wave B intent
change. Net pass count can grow (45/45 → 51/51 is expected with the
six new scenarios); what must not happen is any existing case
flipping from PASS to FAIL silently.

**Dispatch skeleton:** Build on the existing
`scripts/build_android_ui_state_pack_parallel.ps1` four-lane sweep,
add the six new scenarios to the scenario list, run across the
four-emulator matrix, produce packet + summary.json. ~1-2 hours of
active Codex work.

### Stage 2 — External-review gallery regeneration (Codex-executable)

Build a fresh gallery from the v3 packet assets. Same pattern as the
2026-04-18 regeneration that produced
`artifacts/external_review/ui_review_20260418_201903_gallery/`.

Deliverable: `artifacts/external_review/ui_review_<timestamp>_gallery/index.html`
pointing at v3 assets, with a brief intro paragraph noting what
changed vs the prior gallery (Wave B additions).

**Dispatch skeleton:** ~30 minutes active. Gallery tooling exists;
this is a rebuild not a new feature.

### Stage 3 — Playtest checklist creation + run (mixed)

Senku does not currently have a dedicated playtest checklist file
(the `dead-miles-playtest` skill is for a different project). Create
one as part of this gate work. Location:
`notes/SENKU_PLAYTEST_CHECKLIST_20260419.md`.

Checklist must cover, at minimum:

**Wave B behaviors (new):**

1. Uncertain-fit triggers correctly on the reviewer-worked example
   (`He has barely slept, keeps pacing...`) and the UI shows the
   `UNSURE FIT` variant (warning wash, `!` icon, not confident, not
   abstain)
2. Safety-critical escalation line appears above the related-guides
   block on abstain when the scenario is safety-critical
3. Same escalation line appears above the related-guides block on
   uncertain-fit when the scenario is safety-critical (the U-02
   helper is shared between both paths)
4. Confidence-label chip renders correctly for MEDIUM / LOW label
   variants (U-03 plumbing)
5. Three-way mode distinction is visible: confident → normal card,
   uncertain-fit → warning wash + `UNSURE FIT`, abstain → danger
   wash + `NO MATCH`

**Pre-Wave-B behaviors (regression floor):**

6. Normal confident answer renders correctly on all four postures
7. Abstain card renders correctly on all four postures
8. Follow-up query on a session works (session-flow harness now
   green per `BACK-T-04` `2656311`)
9. Tag normalization renders consistently (`BACK-H-01` `92116a8`)
10. Guide bridge-tag displays consistently (`BACK-P-03` `aa2373c`
    + guide fixes `2f664bd`)

**Codex-executable sub-task:** Draft the checklist file with the
items above, plus any edge-case probes Codex identifies while
reading the Wave B specs. Commit as a new note.

**Manual sub-task (Tate):** Run the checklist against the v3 packet.
**Emulator matrix is truth** (four postures, ground truth for CP9).
**Physical phone + tablet are opportunistic** — run on physical when
plugged in, but do not block CP9 closure on physical device
availability. Record results in a tracker note that links from the
checklist.

### Stage 4 — Reviewer sign-off package (Codex-draftable)

Produce the handoff packet for the backend reviewer. Contents:

- Link to v3 packet summary
- Link to v3 gallery
- Wave B spec references:
  - `notes/specs/uncertain_fit_mode_spec.md`
  - `notes/specs/paper_answer_card_uncertain_fit_addendum.md`
  - `notes/WAVE_B_CLOSURE_BOOKKEEPING_PROMPT_2026-04-19.md` (or
    equivalent summary)
- Reviewer-worked example outputs from desktop + Android
- Known deferrals list (E-05, C-09, BACK-R-05)

**Dispatch skeleton:** Codex drafts the packet as a markdown note +
HTML mirror. Reviewer sign-off itself is manual.

### Stage 5 — CP9 close-out commit (Codex-executable)

Tracker-only commit recording CP9 closure:

- Flip CP9 status to `[done YYYY-MM-DD]` in `opustasks.md`
- Update `REVIEWER_BACKEND_TRACKER_20260418.md` Current State prose
- Append a CP9-close State Log row in `reviewer_backend_tasks.md`
  (if that tracker tracks checkpoint closures — verify first)
- Link to v3 packet, v3 gallery, playtest record, reviewer sign-off
  note, and release APK artifact
- Note deferrals (E-05, C-09, BACK-R-05)

Follows the 2026-04-19 Wave B closure commit pattern (`d849cfa`).

---

## Acceptance Criteria for CP9 Closure

1. Stage 0 artifacts green — re-ingest clean, desktop bench within
   regression floor, mobile pack re-exported, Wave B confirmed live
   on all four emulators, fallback uncertain-fit query documented
2. `artifacts/ui_state_pack_release_candidate_v3/<timestamp>/summary.json`
   reports pass with no existing v2 case flipped to FAIL
3. All six Wave B scenarios in the v3 packet pass on all four
   emulator postures (24 net new screenshot cases)
4. External-review gallery built from v3 is linked from the plan /
   tracker
5. Playtest checklist created and one pass on record — emulator
   matrix mandatory, physical devices opportunistic
6. Reviewer sign-off packet delivered; reviewer has acknowledged
7. CP9 close-out commit landed; all trackers reflect the closure
8. Deferrals (E-05, C-09, BACK-R-05) explicitly re-confirmed as
   post-release in the CP9 close-out commit

---

## Decisions Resolved (2026-04-19)

1. **Signed release APK out of scope.** Personal build, not
   production. Optional unsigned/debug APK archived with v3 packet.
2. **Playtest checklist does not exist yet.** Stage 3 creates it at
   `notes/SENKU_PLAYTEST_CHECKLIST_20260419.md`.
3. **Physical device posture: opportunistic.** Emulator four-posture
   matrix is ground truth for CP9. Physical phone + tablet are
   nice-to-have when plugged in; they do not block closure.
4. **Deferrals unchanged.** E-05, C-09, BACK-R-05 remain post-release.
5. **Sequencing.** See Rough Time Estimate below. Stage 1 is the
   longest pole and gates Stages 2 and 3's manual run. Stage 3's
   checklist *creation* can run in parallel with Stage 1's rebuild.

---

## Rough Time Estimate

Assuming no surprises:

- Stage 0 (pre-rebuild verification): 1-2 hours Codex + emulator
  boot and bench rerun wait
- Stage 1 (RC v3 packet): 1-2 hours Codex active + emulator wait
- Stage 2 (gallery): 30-45 min Codex
- Stage 3 checklist creation: 30 min Codex (can run parallel to 1)
- Stage 3 manual playtest: 45-60 min Tate (emulator-only, fast pass)
- Stage 4 (reviewer sign-off packet): 30-45 min Codex
- Stage 5 (close-out commit): 15 min Codex

Total: one focused daylight session end-to-end if Stage 0 clears on
first pass and the v3 rebuild doesn't surface regressions. Two
sessions if Stage 0 finds a corpus regression or the rebuild surfaces
real Wave B bugs.

---

## Next Step

Tate review this plan. Flag disagreements on scope, deferrals, or
sequencing. When aligned, I draft Stage 1 dispatch first (since it's
the longest pole) and we start execution.
