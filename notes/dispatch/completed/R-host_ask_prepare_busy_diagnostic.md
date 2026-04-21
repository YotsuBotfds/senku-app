# Slice R-host — Diagnose `busy[1]: main.ask.prepare` instrumentation stall

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. Read-only
  diagnostic slice.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** safe to run concurrently with gallery
  finalization (`gallery_finalization_retrieval_chain_closed.md`)
  since R-host is read-only and does not touch emulators.
- **Predecessor context:** across the R-gate1 (`f3b2c68`), R-ret1c
  (`2ec77b8`), and R-anchor1 (`971961b`) probe cycles — three
  sessions running — focused instrumentation via
  `PromptHarnessSmokeTest` on the rain_shelter host-inference path
  has hit `busy[1]: main.ask.prepare` and never emitted the
  `ask.generate mode=` line. State-pack lane covers the gap by
  asserting UI settled text instead of engine completion, but
  post-retrieval-chain mode-flip verification is now blind unless
  planner reads state-pack logcats indirectly (see
  `probe_rain_shelter_mode_flip.md`). Three cycles of mode-flip
  blindness is enough evidence to investigate. Shape: T-style
  diagnostic, output a doc, no code change.

## Scope

Read-only diagnostic. Localize why the harness never leaves
`busy[1]: main.ask.prepare` on focused host-inference probes.
Do NOT fix in this slice. Do NOT rebuild the APK. Do NOT run the
emulator matrix. All evidence must come from:

1. Current source at HEAD (`585320c`).
2. Captured artifacts under `artifacts/postrc_ranchor1_20260420_214117/`,
   `artifacts/postrc_rgate1_20260420_203947/`, and earlier probe
   folders where the stall appeared.
3. State-pack logcat captures under
   `artifacts/external_review/rgal1_state_pack/20260420_215227/raw/`
   for comparison — state-pack lane completes on-device
   generation, so its logcat shape is the control.
4. `git log` / `git diff` for R-val2 (`6665bd8`), R-tool1
   (`2ba7d5c`), R-val3 (`607ab916`), and any other commits that
   touched `PromptHarnessSmokeTest.java` or the host-inference
   pathway since the stall first appeared.

Output: `notes/R-HOST_DIAGNOSTIC_20260420.md` with rooted
hypothesis + remediation scope recommendation.

## Context — what is known

- **Stall signature:** `busy[1]: main.ask.prepare` at
  `PromptHarnessSmokeTest.java:1101` persists through the harness's
  settle window; the `ask.generate mode=` line never prints, and
  the focused probe exits on timeout without a captured answer
  surface.
- **State-pack lane bypasses:** state-pack captures use a
  different settle path (see `assertDetailSettled` /
  `waitForDetailBodyReady`) that does not gate on
  `main.ask.prepare` clearing. This is why state-pack runs return
  PASS while focused probes return stall.
- **Signal emission source:** main-app code somewhere emits the
  `main.ask.prepare` busy token at request start and clears it at
  some later boundary. The diagnostic needs to find:
  1. Where `main.ask.prepare` is emitted (entry point).
  2. Where it is cleared / decremented.
  3. Which pathway the host-inference lane takes between those
     two points, and whether a code change since the token was
     introduced has shifted the clear point past where the
     harness's timeout lands.
- **Not ruled out:** harness-side precondition drift. R-val2
  (`6665bd8`) tightened the harness's settle criterion. R-val3
  (`607ab916`) added landscape composer readiness gating. Either
  could have interacted with the busy-signal accounting, even
  though neither was the proximate author of `main.ask.prepare`.

## The work

### Step 1 — Trace `main.ask.prepare` emission / clear in source

Grep the main-app source for `main.ask.prepare` (exact string) and
any related `ask.prepare` / `HarnessTestSignals` busy-counter
helpers. Map:

- File / line of the busy push.
- File / line of the matching pop / decrement.
- Code path between push and pop on the host-inference lane vs the
  on-device-inference lane.

Relevant suspects (verify, don't assume):

- `OfflineAnswerEngine.prepare(...)` and siblings.
- `HarnessTestSignals.snapshot()` — R-val2 added this as a
  hard-fail-on-timeout helper.
- `PromptHarnessSmokeTest.waitForHarnessIdleFallback(...)` at
  `PromptHarnessSmokeTest.java:2431-2440` (T2 cited this as a
  silent-timeout surface).
- Host-inference dispatch path — grep for `host_inference`
  routing and the `:1236` endpoint wiring.

### Step 2 — Compare focused-probe and state-pack logcat traces

For the same rain_shelter query (or any query that hits the
host-inference lane):

- Focused probe (e.g. `postrc_ranchor1_20260420_214117/`): find the
  last busy-signal emission and the next thing the harness logs
  before timeout.
- State-pack sweep (e.g.
  `rgal1_state_pack/20260420_215227/raw/<tablet-posture>/emulator-*/logcat*`):
  find the same busy-signal emission on the state-pack lane, and
  confirm whether `main.ask.prepare` clears before
  `ask.generate mode=` emits or whether the state-pack settle
  criterion lands before the clear too.

If the state-pack lane also shows `main.ask.prepare` never
clearing on host-inference, the stall is real (the state-pack
lane just doesn't care about it). If the state-pack lane shows
the clear happening, the stall is harness-specific to the focused
probe's expectation.

### Step 3 — Identify the remediation shape

Given Steps 1 and 2, recommend one of:

- **(A) Harness precondition drift** — R-val2 / R-val3 tightened
  the settle criterion in a way that collides with
  `main.ask.prepare` accounting. Fix: adjust the harness's
  settle/idle logic. Scope: `PromptHarnessSmokeTest.java` only.
- **(B) Real host-inference hang** — host-inference pathway
  genuinely stalls on `main.ask.prepare` and never emits
  `ask.generate`. Fix: debug host-inference dispatch. Scope:
  `OfflineAnswerEngine.java` and host-inference dispatch code.
- **(C) Busy-counter bookkeeping bug** — push without matching
  pop in a specific code path (e.g. error path, early return).
  Fix: find the missing pop. Scope: wherever the busy token is
  emitted.
- **(D) State-pack lane's settle criterion hides the issue**
  — state-pack sweeps pass only because they accept a settled
  UI that doesn't require `ask.generate mode=` to have fired.
  If so, the state-pack lane's correctness claim is weaker than
  it looks, and this probe's blindness is a symptom of a
  tool-discipline problem. Fix: tighten state-pack settle gate
  (post-RC), but that is a downstream slice, not R-host's
  remediation.

Codex may identify a different shape; that is fine. The doc
should state evidence first, verdict second.

## Boundaries (HARD GATE)

- Read-only. No source edits. No test edits.
- No APK rebuild. No emulator interaction.
- No commit.
- Do not edit any tracker / queue / dispatch markdown.
- Doc output ONLY at `notes/R-HOST_DIAGNOSTIC_20260420.md`.
- If the diagnostic lands and recommends remediation shape (A),
  (B), or (C), the planner will draft the fix slice separately —
  do not draft it in this file.

## Delegation hints

- Step 1 (source grep) is ideal for a Spark xhigh scout lane — pure
  read-only pattern matching over a bounded codebase. Main could
  also do it inline; Spark is cheaper if you want the parallelism.
- Step 2 (artifact logcat comparison) is best kept main-inline
  since it requires judgment about which traces to cross-reference.
- Step 3 (verdict) is main-inline.
- No MCP hint needed — the stall is our own signal, not a
  framework API behavior.

## Acceptance

- `notes/R-HOST_DIAGNOSTIC_20260420.md` exists with:
  - Step 1 trace: busy push / pop location(s).
  - Step 2 trace: focused-probe vs state-pack logcat behaviors at the rain_shelter (or any host-inference) query.
  - Step 3 verdict: one of (A) / (B) / (C) / (D) or a new named shape.
  - Remediation scope recommendation (file / rough LoC, not code).
- No source edits. No commit.

## Report format

Reply with:

- Verdict: (A) / (B) / (C) / (D) / other.
- One-sentence root cause.
- Path to the diagnostic doc.
- Remediation slice shape recommendation (file + one-paragraph sketch).
- Any surprising cross-cutting finding — flag, don't fix.
- Delegation log (lane used per step).
