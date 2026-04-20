# Slice T3 — Diagnose the 5560 body-render gap introduced between rerun2 and rerun3

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. Read-only
  diagnostic slice.
- **Parallel with:** nothing. T3 must complete before any R-slice
  targeting the 5560 gap.
- **Predecessor:** S2-rerun3 landed RED at
  `artifacts/cp9_stage2_rerun3_20260420_101416/`. Four of the
  five Wave B prompts fail on `emulator-5560` only; the other
  three serials are clean.

## Context

Rerun3's 5560 failure shape is different from rerun2's:

- Rerun2 5560: capture clipped to `2400x331` top strip. Body
  **may** have rendered — we couldn't tell from the artifact.
  Logcat showed the correct mode.
- Rerun3 5560: capture is full `2400x1080`. Logcat shows the
  correct mode. But the dump.xml shows only the field header /
  "Next field question" stub for 4 of 5 prompts — the body is
  missing from the UI.

R-tool1 (`2ba7d5c`) correctly fixed capture clipping. R-ui1
(`29463eb`) restructured `activity_main.xml`. The tree delta
between rerun2 (RP2: R-val2 + R-eng2) and rerun3 (RP3: + R-ui1
+ R-tool1) is the universe of suspects.

**Key control signal:** `uncertain_fit_drowning_resuscitation`
is the ONE prompt that passes on 5560 in rerun3. It's also the
ONE prompt whose expected mode is reached via a pure
instant-route path (no generative detour, no R-eng2 short-circuit,
no post-generation downgrade). Every failing 5560 prompt routes
through one of those detours:

- `confident_rain_shelter` → generative → post-gen downgrade to
  `uncertain_fit`.
- `abstain_violin_bridge_soundpost` → abstain (pathway TBD —
  diagnostic task: confirm whether this is an instant abstain
  or a post-gen abstain).
- `safety_uncertain_fit_mania_escalation` → R-eng2 inline-mania
  short-circuit → `UNCERTAIN_FIT` → escalation body builder.
- `safety_abstain_poisoning_escalation` → R-eng2 safety-poisoning
  short-circuit → `ABSTAIN` → escalation body + Poison Control
  builder.

The serials that pass on all five (5556 on-device portrait,
5554 tablet portrait host-inference, 5558 tablet landscape
host-inference) share nothing exclusive — the only unique combo
for 5560 is **on-device inference + landscape posture**.

## Scope

Read-only diagnostic. Localize the chokepoint. Do NOT fix in
this slice. Do NOT run the emulator matrix, do NOT push a new
APK, do NOT re-run the harness. All evidence must come from:
1. Captured artifacts under
   `artifacts/cp9_stage2_rerun3_20260420_101416/` (and
   rerun2 / rerun's for comparison if useful).
2. Current source at HEAD.
3. `git diff` / `git log` for R-ui1 (`29463eb`) and R-tool1
   (`2ba7d5c`).

Output: `notes/T3_5560_BODY_GAP_ROOT_CAUSE_20260420.md` with a
rooted hypothesis + remediation scope recommendation.

## The work

### Step 1 — Pull the five 5560 logcat traces and compare against 5556

For each of the 5 prompts in rerun3, capture both serials'
logcat timelines side-by-side:

- `artifacts/cp9_stage2_rerun3_20260420_101416/validation_5560/emulator-5560/<prompt>/logcat.txt`
- `artifacts/cp9_stage2_rerun3_20260420_101416/validation_5556/emulator-5556/<prompt>/logcat.txt`

For each pair, extract the timeline of ALL `ask.*`,
`low_coverage_*`, `buildAnswer*`, `AnswerRun`,
`DetailActivity.apply*`, `applyPreparedPreviewState`, and any
state-transition or lifecycle events.

Specifically for each failing 5560 case, produce a table
row: time | event | file:line reference.

Then write the DIFF between 5556's trace (passing) and 5560's
trace (failing) for the same prompt. The first event that's
present on 5556 but absent on 5560 is your localization target.

### Step 2 — Pull the dump.xml and screen.png for each 5560 prompt

For each of the 5 prompts, examine the dump.xml structural
tree. Note:

- What's the deepest-rendered element before the body should
  start?
- Is the body container present but empty, or absent entirely?
- Are `ScrollView` / `NestedScrollView` nodes present? If so,
  what are their content dimensions? (Relevant for verifying
  whether the body might be rendered below the visible scroll
  window.)
- Is there any "Next field question" stub sibling that's
  occupying the space the body should occupy?

Compare the failing 5560 dump.xml against the passing drowning
5560 dump.xml. What structural difference distinguishes them?

### Step 3 — Check R-ui1's `activity_main.xml` delta for cross-activity impact

R-ui1's scope was `activity_main.xml` only (the Home activity).
But verify:

- Does Senku's `DetailActivity` share any style / dimen /
  drawable resource that R-ui1 modified? Run
  `git show 29463eb --stat` and `git show 29463eb -- '*.xml'`
  to confirm.
- Did R-ui1 change any `android:theme` at the root level that
  would propagate to Detail?
- Did R-ui1 change any resource dimension (e.g. `dimens.xml`,
  a shared value) that DetailActivity also consumes in
  landscape?
- Did R-ui1 change any `MainActivity.java` intent-extra
  population on the way to DetailActivity? (The slice was
  XML-only, so this should be NO — but verify.)

If none of the above: R-ui1 is unlikely to directly cause the
DetailActivity body-render gap. Note this and move on.

### Step 4 — Check R-tool1's `PromptHarnessSmokeTest.java` delta for landscape-race potential

R-tool1 tightened capture discipline at lines 2916 and 2951.

- Pull the diff: `git show 2ba7d5c -- android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`.
- Identify specifically: does the new settle check use a
  signal that depends on display dimensions, view geometry, or
  resource-qualifier-dependent state? Landscape uses a
  different resource bucket than portrait (different
  `layout-land/` vs `layout/` directories, or different dimens).
- Concretely: does the display-coverage validation use a
  threshold that assumes portrait dimensions? If so, 5560
  landscape would have a different coverage ratio at the
  equivalent render moment.
- Does it fire capture BEFORE the body has actually been bound
  to the view hierarchy? Compare against 5556's successful
  timing on the same prompt.

### Step 5 — Hypotheses to test with log evidence

For each hypothesis below, cite log-line or source-line evidence
to rule it in or out. Do NOT rule out based on "seems unlikely."
The T2 lesson (handoff #1 in outgoing planner's "what I got
wrong") is: verify producer emissions and consumer state from
actual logs, not from visible symptoms.

**H1 — R-ui1 leaked into DetailActivity via a shared resource.**
- Rule in: git diff shows R-ui1 modified a shared style, theme,
  or dimen that DetailActivity also consumes.
- Rule out: git diff shows R-ui1 modified only `activity_main.xml`
  with no shared-resource deltas.

**H2 — R-tool1's tighter settle discipline fires capture
before the body renders on 5560 landscape.**
- Rule in: 5560 logcat shows the body-bind event occurs AFTER
  the harness's capture-taken signal; 5556 shows body-bind
  BEFORE capture. Or the settle check uses portrait-assumed
  metrics that fire prematurely in landscape.
- Rule out: 5560 logcat shows body-bind BEFORE capture, i.e.,
  the body really wasn't built yet when the harness captured.

**H3 — Engine emits the final mode but the body builder doesn't
run (or doesn't deliver) on 5560's detour paths.**
- Rule in: 5560 logcat shows `ask.uncertain_fit` /
  `ask.abstain` but no `buildUncertainFitAnswerBody` /
  `buildAbstainAnswerBody` invocation, while 5556 shows both.
  (Or the builder log is present on both, but 5560 lacks a
  subsequent "answer delivered to DetailActivity" event.)
- Rule out: both logs present on both serials, body is
  delivered, DetailActivity just doesn't render it.

**H4 — DetailActivity on 5560 landscape has a lifecycle event
(e.g. `onConfigurationChanged`) that disrupts state-consumer
between body-delivery and render.**
- Rule in: 5560 logcat shows a configuration-change or
  activity-restart event between engine emission and capture.
- Rule out: DetailActivity lifecycle on 5560 is continuous;
  no restart/reconfig after launch.

**H5 — On-device inference on 5560 landscape has a different
completion signal shape (e.g. tokens arrive in a different
order, final token timing differs).**
- Rule in: 5560 logcat shows the final-token signal arriving
  after the harness captured. Tokens may have streamed but
  completion-commit occurred too late.
- Rule out: final-token signal arrives well before capture.

**H6 — Instant-route vs detour path delivery difference.**
Drowning works on 5560 because it's instant-route. The four
failing prompts all take a detour. Is there a landscape-specific
bug in the detour path's DetailActivity state delivery that
doesn't affect instant-route delivery?
- Rule in: trace the code path for drowning's state delivery
  vs rain_shelter's / mania's / poisoning's. Find the fork where
  they differ. Check for landscape-conditional behavior at or
  beyond the fork.
- Rule out: all four use the same DetailActivity delivery
  entry point and the divergence is elsewhere.

### Step 6 — Write the diagnostic doc

Output: `notes/T3_5560_BODY_GAP_ROOT_CAUSE_20260420.md` in the
same shape as `notes/T2_S2_RERUN_ROOT_CAUSE_20260420.md`:

- Root cause summary (one paragraph, LSOCK: log evidence,
  source evidence, observed convergence, knowledge-level
  understanding).
- Per-failure evidence blocks (one for rain_shelter, violin,
  mania, poisoning). Use the format:
  - Observed.
  - Evidence (with file:line citations).
  - Root cause (localized).
- Cross-cutting findings.
- Hypothesis verdicts (H1-H6) with per-hypothesis evidence.
- Recommended remediation scope (1-3 follow-up slices; name
  them). Prefer narrow over broad. Consider:
  - `R-val3`: if H2 is the root cause — further harness
    tightening for landscape.
  - `R-ui2`: if H1 is the root cause — fix the shared-resource
    leak.
  - `R-eng3`: if H3 is the root cause — fix the body-delivery
    path.
  - Revert of R-tool1's settle tightening: only if the
    evidence clearly points at R-tool1 as the regression
    vector AND a forward fix is infeasible.
- Anti-recommendations.
- Planner read.

## Boundaries (HARD GATE — STOP if violated)

- Read-only. No code commits. No artifact writes beyond the
  diagnostic doc.
- No emulator / APK / pack / model interaction.
- Do not propose remediation in detail. Name the likely slice
  shape; the planner owns slice selection.
- Do not revert anything. Revert proposals belong in the
  remediation slice, not the diagnostic.
- Do not broaden scope beyond 5560's four failing prompts in
  rerun3. (The other four serials' `generativeAskWithHostInferenceNavigatesToDetailScreen`
  state-pack failures are R-gal1 post-RC territory — out of
  scope for T3.)

## Acceptance

- `notes/T3_5560_BODY_GAP_ROOT_CAUSE_20260420.md` exists with
  all sections populated.
- Every hypothesis H1-H6 has a ruling with cited evidence.
  (Log-line references in format `<file>:<line>` or source
  references `<file>:<line>-<line>`.)
- The recommended remediation scope has a single named slice
  with 2-4 sentence justification.
- `git status` shows only the new T3 doc plus pre-existing
  unrelated dirty state.

## Report format

Reply with:
- Path to the T3 doc.
- One-paragraph root cause summary (the headline).
- The ruled-in hypothesis (1 of H1-H6).
- The recommended remediation slice name + 2-4 sentence
  justification.
- Key piece of evidence that convinced you (one log-line or
  source-line citation).
- Any out-of-scope finding that's worth flagging for separate
  planner attention.
- Delegation log (expected: "none; main inline — read-only
  evidence-gathering").

## Anti-recommendations

- Do NOT assert root cause from visible symptom without log
  evidence. This is the T2 lesson applied forward. If the
  visible symptom is "body missing," prove producer emissions
  vs consumer render state from logcat before blaming
  either side.
- Do NOT blame R-ui1 or R-tool1 reflexively. Both fixed real
  problems. If one of them introduced this regression, cite
  the specific mechanism with evidence — not the fact that they
  landed recently.
- Do NOT treat rerun3's state-pack 41/45 as relevant to T3. The
  four failing state-pack states are a separate post-RC issue
  (R-gal1) and do not affect the Wave B contract in this
  diagnostic.
- Do NOT hand back an "inconclusive" root cause. If the evidence
  doesn't converge on one hypothesis, report the narrowest
  hypothesis set still viable (e.g., "H2 or H3 — need further
  evidence from live emulator reproduction") and explicitly flag
  that as the stopping point. Do not invent evidence to force
  convergence.
