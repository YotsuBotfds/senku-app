# Slice R-val2 — Harness settle/capture discipline fix

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Parallel with:** `R-eng2_safety_mode_gate.md`. No file
  overlap (this slice touches `androidTest` only; R-eng2 touches
  `main` only).
- **Predecessor:** T2 root cause at
  `notes/T2_S2_RERUN_ROOT_CAUSE_20260420.md`, specifically the
  Cross-Cutting Finding #1 (capture-lane chokepoint) and
  Recommended Remediation #1.

## Context

T2 established that the visible `placeholder_answer` surface
across all three failing Wave B prompts × four serials is NOT an
engine or DetailActivity render regression. The preview body
that `PromptHarnessSmokeTest` captures is the intentional
"building answer / sources are ready below" body from
`DetailActivity.applyPreparedPreviewState(...)`
(`DetailActivity.java:3045-3056`) and
`buildGeneratingPreviewBody(...)`
(`DetailActivity.java:3132-3137`). It is meant to be a
transitional surface.

The bug is that the harness accepts that preview as "settled":
- `PromptHarnessSmokeTest.assertDetailSettled(...)` at line 2449
  only calls `waitForDetailBodyReady(...)` at line 3905-3946,
  which succeeds as soon as any answer-mode body exceeds a
  minimum length threshold.
- `waitForHarnessIdleFallback(...)` at line 2431-2440 does not
  fail hard on timeout, so unresolved `detail.pendingGeneration`
  work can roll forward into "settled" assertions.
- The generative state-pack capture path at line 2697-2709 also
  inherits this permissive settle criterion, which is why all
  four visual-regression failures are
  `generativeAskWithHostInferenceNavigatesToDetailScreen` with
  the assertion "settled status should keep final backend or
  completion wording when still visible" — the wording is
  missing because the capture fires on the preview.

Until R-val2 lands, we cannot distinguish "UI rendered the wrong
final mode" from "generation never completed" from "the capture
raced the final render." The harness is lying to us.

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only
  `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  plus any helpers it calls that live under `androidTest/`.
- Do NOT touch any `main/` Java code (`DetailActivity.java`,
  `OfflineAnswerEngine.java`, `QueryMetadataProfile.java`, etc.).
  R-eng2 owns the engine side.
- Do NOT change the intentional preview copy in
  `DetailActivity.buildGeneratingPreviewBody(...)`. The T2 doc
  explicitly anti-recommends this — the preview surface is
  correct; the bug is that the harness accepts it.
- Do NOT rewrite the harness broadly. Scope is strictly the
  settle / capture / idle-wait assertions named below.
- Single commit for the harness fix.
- No emulator interaction required — this is a unit/instrumentation
  source-level fix; validation of runtime behavior happens in
  RP2 + S2-rerun2 later in the chain.

## Outcome

The harness refuses to count the preview body as a final
generative answer. Idle-wait fallbacks fail hard and surface
active labels. The generative state-pack capture only snapshots
after a terminal route or final status wording appears. A new
test (or an updated assertion shape) captures the regression
pattern so a future preview-as-settled slip fails loud.

## The work

### Step 1 — Inventory the permissive assertions

Read the current code at these exact locations and confirm the
T2 diagnosis on-source:

- `PromptHarnessSmokeTest.java:2431-2440` — `waitForHarnessIdleFallback(...)`
- `PromptHarnessSmokeTest.java:2449-2453` — `assertDetailSettled(...)`
- `PromptHarnessSmokeTest.java:2697-2709` — generative capture
  settle path (`generativeAskWithHostInferenceNavigatesToDetailScreen`
  and related state-pack paths)
- `PromptHarnessSmokeTest.java:3905-3946` — `waitForDetailBodyReady(...)`

Identify what each function treats as "settled" today and why
the preview body slips through. Map the call graph so Step 2's
fix targets the minimum surface.

### Step 2 — Tighten settle criteria

Make settle assertions reject the preview body. Options the
slice does NOT prescribe (Codex picks the minimal one that
works):

- Compare the body against a known-preview sentinel (call into
  `DetailActivity`'s preview copy via `R.string` resources or
  a shared constant to avoid duplicated string literals).
- Require a terminal mode marker (final Wave B card: NO MATCH,
  UNSURE FIT, confident answer body, safety escalation line,
  etc.).
- Wait for `detail.pendingGeneration` to clear before counting
  as settled.

The minimum correct change: the harness must not report
"settled" while the UI is on the transitional preview surface.

### Step 3 — Make idle-wait fallback fail hard

`waitForHarnessIdleFallback(...)` must fail the test on timeout
rather than returning and letting assertions continue. On
timeout:

- Capture the active `HarnessTestSignals` labels (per T2's
  explicit callout in Cross-Cutting Finding #2).
- Fail with a clear message naming the unresolved label(s), so
  future diagnostics don't have to reverse-engineer the state
  from artifacts.

### Step 4 — Tighten the generative capture lane

At line 2697-2709 (and any sibling state-pack capture points),
only snapshot once the detail screen is truly settled. The fix
here likely reuses Step 2's tightened `assertDetailSettled(...)`;
if there's a separate capture path that bypasses it, route it
through the same check.

### Step 5 — Add or harden tests that prove the capture bug is closed

Add one or more focused tests that:
- Force a known preview-phase state.
- Run the old loose assertion → should have passed (regression
  signal).
- Run the new strict assertion → must fail (or require the
  final surface to proceed).

If the existing test framework doesn't have an easy way to stage
a "preview-only" state, document why in the report and use the
tightest equivalent you can (e.g., a unit test of the new strict
assertion with a synthetic dump fragment).

### Step 6 — Run tests + commit

- `./gradlew.bat :app:testDebugUnitTest --tests
  "com.senku.mobile.PromptHarnessSmokeTest*"` (or whichever
  gradle task matches — instrumentation tests may need
  `connectedDebugAndroidTest`; if the test class is
  `androidTest` only, run the unit suite adjacent to it to prove
  no main-code regressions).
- Any instrumentation-only tests that need a device are out of
  scope here — we validate runtime in RP2 + S2-rerun2.

Commit suggestion:
`R-val2: tighten harness settle/capture discipline`

## Acceptance

- `PromptHarnessSmokeTest` no longer treats the preview body as
  a final answer.
- `waitForHarnessIdleFallback(...)` fails hard on timeout and
  names the active labels.
- Generative state-pack capture only snapshots after terminal
  route / final status wording.
- At least one new or hardened test proves the bug is closed.
- Single commit.
- Unit tests that can run without a connected device pass.

## Report format

Reply with:
- Commit sha.
- One-paragraph summary of the tightened settle criterion
  (what was loose before, what strict now).
- Count of new/updated tests + the test class/method names.
- Any out-of-scope finding (e.g. discovered a third settle path
  that needed tightening, or a related test helper that has the
  same permissive shape).
- Open question for planner: does any production code OTHER than
  the harness rely on the loose "body > N chars" definition? If
  yes, call it out — may be a post-RC tooling follow-up.
- Delegation log.
