# CP9 Stage 0 Resume - Step 6 with corrected query

Date: 2026-04-19
Parent dispatch: `notes/CP9_STAGE_0_DISPATCH_2026-04-19.md`
Prior dispatch: `notes/CP9_STAGE_0_RESUME_2026-04-19.md`
Prior artifact dir: `artifacts/cp9_stage0_20260419_142539/`

---

## Why this resume exists

The first Step 6 smoke failed on `emulator-5556`: `uncertain_fit`
rendered but with no safety escalation line. Root cause is NOT a
Wave B deployment gap — it is a query-phrasing error in my
dispatch.

The query I supplied —
`He has barely slept, keeps pacing and muttering to himself, and refuses to eat. What should we do?`
— matches ZERO entries in the mobile
`SAFETY_CRITICAL_MENTAL_HEALTH_MARKERS` set
(`OfflineAnswerEngine.java` lines 48-63). So
`isSafetyCriticalQuery` returned false and
`buildUncertainFitAnswerBody` correctly omitted escalation.

The original reviewer-worked example from
`notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md` line 84 contains
`normal rules do not apply` — which IS in the mobile marker list.
That phrasing triggers `isSafetyCriticalQuery = true` on mobile
and Wave B U-02 renders the escalation line as designed.

Steps 1-5 already passed. Step 3 produced pack hash
`d752de8fc7917f142be367b35fdf936c10e43b715a93c82130adb843f30cf496`.
This resume redoes only Step 6 and Step 7 against the correct
query.

---

## Ground rules (unchanged)

- No code commits. No tracker commits. Artifact output only.
- No Wave B edits. No guide edits. No BACK-P-03 audit edits.
- Gated-step discipline: if any step fails, stop and log.
- Do not start Stage 1 in the same session.

Reuse the existing artifact directory:

```
STAGE0_DIR=artifacts/cp9_stage0_20260419_142539
```

---

## Preflight re-check

```
git status
git log --oneline -3
git diff --name-only
git diff --cached --name-only
```

HEAD should still be `65252f7`. Both diff commands empty.

Also verify emulators still present and the pack from the prior
Step 5 is still installed (no need to re-push if the pack hash
on-device still matches
`d752de8fc7917f142be367b35fdf936c10e43b715a93c82130adb843f30cf496`;
if the emulators were cold-stopped, re-run Step 4 and Step 5 from
the parent dispatch first):

```
adb devices
```

All four of `emulator-5556`, `emulator-5560`, `emulator-5554`,
`emulator-5558` must be listed. If any are missing, re-run Step 4
from the parent dispatch.

---

## Step 6 (corrected) - Wave B live-on-device smoke

Corrected reviewer-worked example query:

`He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down?`

This phrasing contains `normal rules do not apply`, a literal
entry in `SAFETY_CRITICAL_MENTAL_HEALTH_MARKERS`. Mobile
`isSafetyCriticalQuery` returns true for it. `buildUncertainFitAnswerBody`
(with `safetyCritical=true`) and `buildAbstainAnswerBody` (with
`safetyCritical=true`) both inject
`SAFETY_CRITICAL_ESCALATION_LINE` above the "Possibly relevant /
Closest matches" block.

Harness invocation (adjust the parameter names to match the
script head; prior Step 6 evidence shows the harness works —
`smoke_5556/run.txt` confirms `PromptHarnessSmokeTest#scriptedPromptFlowCompletes`):

```
powershell -NoProfile -File scripts/run_android_prompt.ps1 -Serial emulator-5556 -Query "He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down?" -Output $STAGE0_DIR/smoke_5556_v2/
```

Repeat for 5560, 5554, 5558 writing to
`smoke_5560_v2/`, `smoke_5554_v2/`, `smoke_5558_v2/`.

For each emulator, record:

- Mode reported. Accept either `uncertain_fit` or `abstain` as
  passing — both paths emit the escalation line when
  safetyCritical is true. Wave B U-02 hooks escalation into BOTH
  builders.
- UI evidence: `UNSURE FIT` pill or abstain-card visible in
  screenshot depending on mode.
- Body text: first sentence per Wave B:
  - uncertain_fit: `Senku found guides that may be relevant to`
    (or `Senku found only loosely related guides for` if
    confidenceLabel == LOW)
  - abstain: `Senku doesn't have a guide for`
- **Escalation line (the actual gate):** `ui_dump.xml` must contain
  `If this is urgent or could be a safety risk, stop and call local
  emergency services now` above the "Possibly relevant guides in
  the library:" or "Closest matches in the library:" block.

Write updated per-emulator summary to
`$STAGE0_DIR/smoke_summary_v2.md`. Keep the original
`smoke_summary.md` intact as evidence of the first run and as
support for the BACK-U-04 finding.

Acceptance: escalation line present on all four emulators. If any
emulator fails, stop and log — that would indicate a real Wave B
deployment gap, not a query-phrasing issue.

---

## Step 7 - fallback query (unchanged from parent)

Execute Step 7 exactly as written in the parent dispatch
`notes/CP9_STAGE_0_DISPATCH_2026-04-19.md`. The three candidate
queries do NOT contain mental-health markers — they test the RRF
/ vector-similarity trigger paths instead, which is the intended
behavior for a fallback. Do not substitute those candidates.

Write fallback documentation to
`$STAGE0_DIR/fallback_query.md`.

---

## Step 8 - updated summary

Append a "Resume v2" section to `$STAGE0_DIR/summary.md` that
supersedes the prior Step 6 result. Structure:

```
## Resume v2 (Step 6 corrected query)

Date: <ISO timestamp>
Original Step 6 query phrasing failure root-caused to marker-list
mismatch, not Wave B deployment gap. See
`notes/CP9_STAGE_0_STEP6_RESUME_2026-04-19.md`.

Corrected query:
  He has barely slept, keeps pacing, and says normal rules do not
  apply to him. Is this just stress, or should I help him calm
  down?

| Emulator | Mode | Escalation line | Status |
| ---- | ---- | ---- | ---- |
| emulator-5556 | ... | present/missing | pass/fail |
| emulator-5560 | ... | ... | ... |
| emulator-5554 | ... | ... | ... |
| emulator-5558 | ... | ... | ... |

## Known parity gap (for post-CP9 tracking)

The first Step 6 attempt revealed that mobile
`SAFETY_CRITICAL_MENTAL_HEALTH_MARKERS` is a narrow subset of
desktop's `_MANIA_*_CRISIS_QUERY_MARKERS` in `query.py`. Phrasings
like `barely slept`, `keeps pacing`, `muttering`, `refuses to eat`
trigger escalation on desktop but not on mobile. This is
pre-existing — Wave B U-02 did not introduce it. Flag for
follow-up task BACK-U-04 (post-CP9).

## Stage 0 verdict: GREEN / RED
```

---

## Step 9 - report

Report pass/fail per emulator for Step 6, Step 7 status, updated
summary path, and the final Stage 0 verdict. Stop after reporting.

## If Step 6 still fails

If the corrected query ALSO fails to produce the escalation line
on any emulator, that is a real Wave B deployment gap on that
emulator. Likely causes:

- APK on that emulator predates U-02. Verify APK install state
  via `adb shell pm list packages -f com.senku.mobile` and
  compare build timestamps.
- Pack hash on-device differs from Step 3 export hash. Re-push
  the pack via Step 5 of the parent dispatch and re-run Step 6.

Do NOT edit Wave B code to work around this. If it's a real
deployment gap, surface it and stop — that's planner work.