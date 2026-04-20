# Slice R-eng2 — Mode-gate narrowing for safety prompts

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Parallel with:** `R-val2_harness_settle_capture.md`. No file
  overlap (this slice touches `main` only; R-val2 touches
  `androidTest` only).
- **Predecessor:** T2 root cause at
  `notes/T2_S2_RERUN_ROOT_CAUSE_20260420.md`, specifically
  Cross-Cutting Finding #3 and Recommended Remediation #2.

## Context

Two safety prompts fail with no visible escalation line in the
S2-rerun captures:

- `safety_uncertain_fit_mania_escalation` — expected
  `UNCERTAIN_FIT` mode with a safety escalation line.
- `safety_abstain_poisoning_escalation` — expected `ABSTAIN`
  mode with a safety escalation line and Poison Control clause.

T2 localized the chokepoint: in both traces, the engine logs
`ask.prompt`, which means `OfflineAnswerEngine.resolveAnswerMode(...)`
(around `OfflineAnswerEngine.java:1210-1238`) did NOT
short-circuit to `UNCERTAIN_FIT` or `ABSTAIN`. The traces then
run into generation (and are captured in preview state — R-val2
fixes the capture side). Because the mode gate skipped the
intended branches, the escalation body builders never ran:

- `buildUncertainFitAnswerBody(...)` at
  `OfflineAnswerEngine.java:1513-1563` is the mania-shape path.
- `buildAbstainAnswerBody(...)` at
  `OfflineAnswerEngine.java:1450-1502` is the poisoning-shape
  path.

The poisoning classifier metadata is already correct: the T2
evidence shows the log `structure=safety_poisoning
explicitTopics=[lye_safety]` (R-cls's work is doing exactly what
it was supposed to). The gap is purely that the mode gate does
not map `safety_poisoning` → `ABSTAIN` (or whatever equivalent
safety-critical stop it needs).

R-eng's `1f76ccf` is not to be reverted. The violin-bridge fix
it introduced still works on all four serials, which proves the
general gate hardening was correct. R-eng2 narrows the gate only
for the two safety prompt shapes, leaving the violin-bridge
behavior intact.

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only main-code Java:
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - Supporting unit tests at
    `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - Any additional helpers pulled in by the gate change (find
    via grep; likely constant files or a classification result
    struct). Stay as narrow as possible.
- Do NOT touch any `androidTest/` file (R-val2 owns those).
- Do NOT touch `QueryMetadataProfile.java` — R-cls's
  classification is already correct; the gap is in the gate's
  consumption of classifier output, not the classifier itself.
- Do NOT touch `DetailActivity.java`. The UI render path is
  correct per T2.
- Do NOT revert or rework R-eng's `1f76ccf` broadly. The
  violin-bridge fix it landed is verified working. Change only
  the specific decision point that routes safety prompts.
- Do NOT widen the gate in a way that over-triggers UNCERTAIN_FIT
  / ABSTAIN for non-safety prompts. Keep the change scoped to
  the safety-poisoning / mania-shape fingerprints.
- Single commit for the gate fix + tests.

## Outcome

For the two safety prompt shapes, `resolveAnswerMode(...)`
returns the expected mode (UNCERTAIN_FIT for mania, ABSTAIN for
poisoning) BEFORE generation. The escalation builders run
synchronously, emitting the safety escalation line and (for
poisoning) the Poison Control clause. Non-safety prompts
(violin-bridge, rain_shelter, drowning-resuscitation) are
unaffected.

## The work

### Step 1 — Reproduce the decision on-source

Read the current `resolveAnswerMode(...)` at
`OfflineAnswerEngine.java:1210-1238` (and any supporting
confidence helpers — T2 points to line 287). Map the exact
branch ladder.

For each safety prompt shape, trace by hand what inputs the
gate would see:

- Mania: top retrieval rows (first-aid, improvised weapons,
  civil disputes per T2), anchor (GD-197 Justice & Legal
  Systems per T2), classifier output (from
  `QueryMetadataProfile`), and confidence scores. Figure out
  why the gate falls through to generation instead of
  UNCERTAIN_FIT.
- Poisoning: classifier output is
  `structure=safety_poisoning explicitTopics=[lye_safety]` per
  T2, selected context anchors to GD-232 (Poisoning and
  Overdose). Figure out why the gate falls through instead of
  ABSTAIN.

Capture the decision-point finding(s) in a short internal note —
which condition(s) fire for the safety shape but don't route to
the intended mode.

### Step 2 — Tighten the gate

Add a narrow, evidence-gated routing rule at the decision point
such that:

- When the classifier returns `structure_type='safety_poisoning'`
  (or an equivalent explicit safety signal) AND the query shape
  matches a safety-critical pattern, the gate returns ABSTAIN
  (if context is mismatched or sparse) or UNCERTAIN_FIT (if
  context is adjacent but not authoritative). Pick the right
  branch for the poisoning case based on T2's evidence —
  poisoning expected ABSTAIN with Poison Control.
- When a mania-shape / acute-mental-health signal is detected
  (either via the classifier or a targeted heuristic in the
  gate itself — avoid duplicating classifier logic if possible),
  the gate returns UNCERTAIN_FIT.

Prefer reusing `QueryMetadataProfile` output over introducing
new classification logic in the engine. If that isn't possible,
flag the gap as an out-of-scope finding (would become an R-cls2
follow-up slice).

Keep the change narrow: it should not widen the gate for
violin-bridge, rain_shelter, or drowning-resuscitation. Those
prompts' current routing is correct (or is a separate bug
outside this slice's scope).

### Step 3 — Add unit tests

Add targeted tests under
`android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
that:

- Assert `resolveAnswerMode(...)` returns `ABSTAIN` for a
  poisoning-shape input (synthetic classifier output + context
  fingerprint).
- Assert `resolveAnswerMode(...)` returns `UNCERTAIN_FIT` for a
  mania-shape input.
- Assert the synchronous escalation body is produced by the
  correct builder path for each.
- Assert that unrelated prompts (e.g. a violin-bridge-shaped
  input and a rain_shelter-shaped input) are NOT rerouted by
  the new rule — i.e. the change is specific.

### Step 4 — Run unit tests + commit

- `./gradlew.bat :app:testDebugUnitTest --tests
  "com.senku.mobile.OfflineAnswerEngineTest*"`

Commit suggestion:
`R-eng2: route safety prompts to abstain/uncertain_fit before generation`

## Acceptance

- `resolveAnswerMode(...)` returns ABSTAIN for poisoning input
  and UNCERTAIN_FIT for mania input in unit tests.
- The escalation body is produced synchronously for both (unit
  tested).
- Unrelated prompts are unaffected (regression unit tests pass).
- Full `OfflineAnswerEngineTest` suite passes (prior 47 tests
  plus any new ones).
- Single commit.

## Report format

Reply with:
- Commit sha.
- The exact decision-point diff (file + line + what condition
  was tightened or added).
- Classifier-output shape used for each safety fingerprint
  (what `structure_type` / `topic_tags` / etc. trigger the new
  rule).
- Count of new unit tests + class/method names.
- Verdict on whether classifier output was sufficient or whether
  a post-RC R-cls2 slice may be needed.
- Any out-of-scope finding.
- Delegation log.