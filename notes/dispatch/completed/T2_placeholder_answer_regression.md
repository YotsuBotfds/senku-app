# Slice T2 — S2-rerun `placeholder_answer` common-mode regression diagnostic

- **Role:** main agent (`gpt-5.4 xhigh`). Diagnostic only — NO
  code fix in this slice. Same shape as T1.
- **Predecessor:** S2-rerun RED at
  `artifacts/cp9_stage2_rerun_20260419_221343/summary.md` +
  `wave_b_coverage.json`.
- **Not parallelizable:** unlike T1 (which had 3 distinct
  failure tracks) this is one unified failure signature. A single
  explorer scout can localize; parallel fan-out is unnecessary
  overhead here. Main lane with optional inline Zeno scout for
  code reading is fine.

## The failure, in one paragraph

Three Wave B prompts fail identically on all four serials
(5556 / 5560 / 5554 / 5558) in S2-rerun:

- `confident_rain_shelter` (expected `confident`)
- `safety_uncertain_fit_mania_escalation` (expected `uncertain_fit`
  WITH safety escalation line)
- `safety_abstain_poisoning_escalation` (expected `abstain` WITH
  safety escalation line)

All three land on the **same rendered surface**: the intermediate
"building answer" placeholder at
`DetailActivity.java:3132-3137` —
`buildGeneratingPreviewBody(sourceCount)` returns either
`"Finding guide evidence to ground this answer..."` (sourceCount
== 0) or `"Sources are ready below. Building the answer from
those guides without leaving this thread."` (sourceCount > 0).
The UI stalls on that body and never transitions to any Wave B
final mode. For the two safety prompts, this means the
safety-critical escalation line and Poison Control clause are
entirely absent from the rendered card — eight RC-blocking
failures (two safety prompts × four serials).

**Violin-bridge is fixed** on all four serials — the
`abstain_violin_bridge_soundpost` prompt now renders `NO MATCH`
correctly, which validates R-eng's gate hardening approach in
principle. The regression is not in the abstain path; it is in
the path that was supposed to deliver `confident`, `uncertain_fit`,
and `abstain-with-escalation` surfaces.

One additional data point: the visual state-pack gallery went
from 45/45 → 41/45. All four failing captures are
`generativeAskWithHostInferenceNavigatesToDetailScreen` — one
per posture — with the same assertion message
`"settled status should keep final backend or completion
wording when still visible"`. This exactly matches the engine/UI
stall story: when the UI tries to settle, the final backend/
completion wording is missing from the surface.

## Context you need to load

- Stage 2 rerun summary (verdict + per-prompt evidence):
  `artifacts/cp9_stage2_rerun_20260419_221343/summary.md`
- Per-prompt coverage with artifact dir pointers:
  `artifacts/cp9_stage2_rerun_20260419_221343/wave_b_coverage.json`
- T1 root-cause doc for reference on the prior failure shapes:
  `notes/T1_STAGE2_ROOT_CAUSE_20260419.md`
- R-eng commit (the most likely source of the regression):
  `git show 1f76ccf -- android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
- R-cls commit (sibling remediation, possibly relevant for the
  mania case if routing changed):
  `git show e07d4e7 -- android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
- R-pack commit (ruled out structurally because all failures
  replicate across host-inference lanes that don't use the pack's
  retrieval the same way, but worth keeping in mind):
  `git show bd84835`

## Hypotheses to verify, in rough priority order

### H1 (highest prior) — R-eng's post-generation downgrade leaves the UI in the generating-preview state

Per R-eng's commit message and the earlier queue log entry:
`generate()` now swaps the surface to `abstain` or
`uncertain_fit` via post-generation downgrade when
`low_coverage_detected` fires. The new swap may:

- Emit a surface-descriptor shape the DetailActivity
  `renderDetailState()` / `createAnswerProgressListener` path does
  not recognize as a completed final mode, so it never replaces
  `buildGeneratingPreviewBody` text with the Wave B card.
- Fire for cases where the mode-decision ladder already resolved
  to `confident` or `uncertain_fit` with legitimate evidence, so
  the downgrade is incorrectly pre-empting a valid final surface.
- Drop the `safety_critical_escalation` augmentation when it swaps
  surfaces (would explain missing escalation line even if the
  surface change itself were benign).

Trace to run:
1. Pull the per-prompt logcat for each of the three failing
   prompts on one serial (e.g. 5556). Logcat lives at
   `artifacts/cp9_stage2_rerun_20260419_221343/validation_5556/emulator-5556/<case_id>/logcat.txt`.
2. Search for `OfflineAnswerEngine`, `low_coverage`, `generate`,
   `resolveAnswerMode`, `shouldAbstain`, `placeholder`, and
   `AnswerProgressListener` keywords. Extract the time-ordered
   sequence of emissions.
3. Correlate with `ui_dump.xml` / final screenshot — did the
   engine emit a completion event at all? Did `renderDetailState()`
   see it?

### H2 — DetailActivity render state machine does not handle the downgraded mode

Even if the engine emits the right downgrade surface, the
DetailActivity `renderDetailState()` code path may route on the
ORIGINAL mode decision (e.g. `confident`) rather than the
post-downgrade mode. If so:

- The Wave B card render branch for the original mode expects a
  finished answer body, doesn't get one (generate was short-
  circuited), and falls through to keep the generating-preview
  body.
- The safety escalation augmentation may be guarded on the
  original mode, so it doesn't apply to the downgraded surface.

Trace to run:
1. Grep DetailActivity for `renderDetailState`, `AnswerSurface`,
   `answerMode`, `escalation`, `low_coverage`, `downgrade`. Map
   the switch/if ladder that decides what body to show.
2. Check whether the rendered body is produced from the same
   mode variable the engine writes on downgrade, or from a
   stale mode variable that was set pre-downgrade.

### H3 — The mode decision is over-restrictive on the healthy post-R-pack corpus

R-pack expanded the corpus from 14,784 → 49,654 chunks. This
means top-k retrievals for these three prompts are structurally
different from what R-eng was tuned against:

- `confident_rain_shelter`: more competing chunks may dilute
  route-focused signal, pushing the gate to downgrade even
  though the shelter evidence is strong.
- `mania_escalation`: more chunks mean different top rows; if
  mania-adjacent guides scored differently, the gate may now
  fire `low_coverage_detected` where it previously didn't.
- `poisoning_escalation`: same reasoning, further amplified by
  R-cls's new `safety_poisoning` bucket that now actually
  matches (GD-898/301/054/602 have `structure_type='safety_poisoning'`
  in the new pack); ironically this could be routing the query
  somewhere that then triggers the downgrade.

Trace to run:
1. Probe the per-prompt logcat for the resolved mode (before
   any downgrade). Is it even reaching generate() with a sane
   mode?
2. If mode resolution is sane and generate() completes, the bug
   is downstream (H1/H2). If mode resolution is already
   downgraded or abstain-like before generate() runs, the bug
   is in the mode-decision ladder.

### H4 (lowest prior) — Safety escalation emission decoupled from post-downgrade surface

Independent of H1/H2, verify whether the safety escalation line
is emitted on a code path that:

- Runs only for the originally-decided mode (so when downgrade
  fires, the escalation path is skipped), OR
- Runs only when the Wave B card is finalized (so when the UI
  stalls on the placeholder, there is no card to append to).

Trace to run:
1. Grep DetailActivity / related formatters for `escalation`,
   `safety`, `Poison Control`, `Call poison`, `911`, `emergency`.
   Find where the escalation line is appended.
2. Check whether that code path runs for the failing prompts
   (would appear in logcat) or is entirely skipped.

## The work

### Step 1 — Load evidence

Read the three case dirs for ONE serial (5556 is fine; the
failure is identical across all four):
- `artifacts/cp9_stage2_rerun_20260419_221343/validation_5556/emulator-5556/confident_rain_shelter/`
- `...safety_uncertain_fit_mania_escalation/`
- `...safety_abstain_poisoning_escalation/`

Each case dir has `logcat.txt`, `ui_dump.xml`, and screenshot(s).

### Step 2 — Localize the stall

For each failing prompt, answer concretely:
1. Did `OfflineAnswerEngine.generate()` complete? What mode did
   `resolveAnswerMode()` return? Did `low_coverage_detected`
   fire? Did the post-generation downgrade run?
2. What did `AnswerProgressListener` receive? Was
   `renderDetailState()` invoked after the expected final
   emission, or did it stop after the generating-preview was set?
3. Was the safety escalation line's code path entered at all for
   the two safety prompts? If not, why not?

### Step 3 — Write `notes/T2_S2_RERUN_ROOT_CAUSE_20260420.md`

Single document, same shape as T1:
- Per-prompt root cause (1-3 paragraphs each).
- Cross-cutting finding(s): likely one paragraph here since the
  three failures appear to share root cause.
- Remediation recommendation (scope + slice count). Likely one
  R-eng2 slice (tighten the post-generation downgrade + surface
  transition) + optional R-detail slice (DetailActivity render
  fix if H2 is the true cause). Avoid fragmenting into many
  slices; this is a single common-mode bug unless the evidence
  forces otherwise.
- Anti-recommendations: what NOT to do (e.g. do not revert
  R-eng — violin-bridge proves the gate hardening works; the
  bug is in the downgrade consumer path, not the gate).

Commit the doc (tracked, planner will cite).

### Step 4 — Report to planner

Reply with:
- Root cause named (one paragraph).
- Affected code paths (file + line, tight).
- Proposed remediation slice(s) with scope boundaries.
- Verdict on each hypothesis (H1-H4) — which is correct, which
  was ruled out, which remain plausible.
- Any out-of-scope finding (e.g. independent tooling bugs
  surfaced during investigation).
- Delegation log.

## Boundaries

- No code fix. Diagnostic only.
- No emulator interaction (logs + dumps already captured).
- No changes to trackers / queue / dispatch markdown.
- Single `notes/T2_S2_RERUN_ROOT_CAUSE_20260420.md` doc; commit
  it in one commit titled roughly
  `T2: root-cause S2-rerun placeholder_answer regression`.
- Do not propose more than 2 remediation slices without an
  explicit cross-cutting justification — the failure signature
  is unified, so remediation should be too.

## Acceptance

- Root-cause doc exists and names the chokepoint with file + line.
- All three failing prompts have per-prompt evidence
  (mode transition trace from logcat).
- Verdict on each hypothesis H1-H4.
- Remediation scope proposed (single slice if possible; two if
  engine vs. rendering split is clean).
- Commit lands.

## Report format

Reply with (in order):
- Commit sha.
- One-paragraph root cause.
- Verdict on H1-H4 (one line each).
- Proposed remediation slice(s) + scope.
- Any out-of-scope finding.
- Delegation log.
