# Slice R-host — Probe handoff awareness (harness remediation)

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline for the
  implementation; Spark xhigh lane OK for the Step 1 read-only
  source trace if you want to parallelize. No MCP hint needed —
  the fix is entirely within our harness code.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** nothing in flight. R-ret1b corpus-vocab
  revision is still open but independent. State-pack validation
  (post-fix) is a separate slice.
- **Predecessor context:** R-host diagnostic at
  `notes/R-HOST_DIAGNOSTIC_20260420.md` (verdict A — harness
  precondition drift). The focused host-ask probe treats
  `MainActivity`-owned breadcrumb `main.ask.prepare` as the
  authoritative in-flight label for the entire ask lifecycle,
  but production generation ownership moved into `DetailActivity`
  as `detail.pendingGeneration`. The probe also implicitly
  expects a generic `ask.generate mode=...` breadcrumb, but
  current source only emits `mode=` on the low-coverage route
  (confident paths are silent). Together these blind the probe
  across three retrieval-chain slices (R-gate1 / R-ret1c /
  R-anchor1). R-host recommends rewriting the probe to wait on
  `DetailActivity` settle + stop asserting on either stale
  breadcrumb class.

## Scope

Fix the probe. Don't add new production telemetry, don't retune
thresholds, don't touch `main.search` (different bug class —
timing race, not architectural mismatch; flagged out-of-scope
below). Single commit. Single touched file if possible
(`PromptHarnessSmokeTest.java`); one adjacent harness file
(`HarnessTestSignals.java`) OK if a new helper is genuinely
needed.

## Preconditions (HARD GATE — STOP if violated)

1. HEAD on `master` includes the four retrieval-chain commits
   (`2ec77b8`, `0a8b260`, `971961b`, `585320c`) + D4
   (`2e39021`) + any later doc-only commits. Current HEAD is
   `2e39021` as of 2026-04-21; confirm with
   `git log --oneline -5`.
2. `notes/R-HOST_DIAGNOSTIC_20260420.md` is readable and
   unchanged since 2026-04-20 (no inline amendments that would
   shift the remediation recommendation).
3. `android-app/` builds cleanly on current HEAD:
   `./gradlew.bat :app:compileDebugAndroidTestJavaWithJavac` and
   `./gradlew.bat :app:testDebugUnitTest` both pass before you
   touch anything. Baseline unit suite 431/431.

## Outcome

Single commit that:

- Rewrites the focused host-ask probe so it treats the
  `MainActivity` → `DetailActivity` handoff as first-class.
- Stops asserting on `main.ask.prepare` clearance or
  `ask.generate mode=...` presence as proof of settled host
  generation.
- Waits explicitly for a resumed `DetailActivity` or a settled
  rendered detail surface (existing `waitForDetailBodyReady` /
  `assertDetailSettled` is the natural target).
- Updates failure-reporting text to name the activity that
  actually owns the resumed screen (so future triage is not
  re-misled by a stale `MainActivity`-owned snapshot).
- Unit suite 431/431 still passes.
- At least one new instrumentation test locks in the new
  handoff-aware settle behavior (scope of "at least one" is
  your judgment; R-val2's pattern of adding a narrow regression
  test per behavior change is the right shape).

Post-fix validation is NOT part of this slice's acceptance.
Validation (focused rain_shelter re-probe, state-pack re-run)
belongs in a follow-up slice; R-host alone is the fix.

## Boundaries (HARD GATE)

- Touch only:
  - `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - Optionally:
    `android-app/app/src/main/java/com/senku/mobile/HarnessTestSignals.java`
    if a new in-signals helper is needed (e.g., a
    `getResumedActivity` observable). If you end up needing a
    *new production-side telemetry point*, STOP and report —
    that belongs in a separate slice per R-host §7.
- Do NOT touch:
  - `MainActivity.java`, `DetailActivity.java`,
    `OfflineAnswerEngine.java`, or any production answer-path
    code. The bug is harness-side; the fix is harness-side.
  - `main.search` emission/clear pairs or
    `searchQueryShowsResultsWithoutShellPolling`. The 5554
    (`rgal1_flakecheck_20260420_221049/`) and 5556
    (`rgal1_flakecheck2_5556_20260421_062052/`) flakes cleared
    3/3 on re-run; the failure shape is a timing race at
    `waitForResultsSettled` (RecyclerView adapter + header
    polling), not a stale-breadcrumb assertion. Leave it.
  - Any tracker / queue / dispatch markdown.
  - `R-val2`-introduced tests
    (`previewLengthAnswerBodyDoesNotCountAsSettledDetail`,
    `standardAnswerKeepsDefaultCardTreatment`); the new
    handoff-awareness should preserve these passing.
  - `R-val3`-introduced landscape composer readiness gating.
- No APK push / emulator matrix run from this slice. The
  androidTest APK will need to be rebuilt and pushed for
  post-fix validation, but that's the follow-up slice's scope.
- No commits beyond the single R-host commit.

## The work

### Step 1 — Read the diagnostic and locate the probe(s)

Re-read `notes/R-HOST_DIAGNOSTIC_20260420.md` §1, §6, §7. Then
locate in `PromptHarnessSmokeTest.java`:

- The focused host-ask probe(s) — any test that launches
  `ActivityScenario<MainActivity>`, triggers an ask, and then
  waits on `HarnessTestSignals`-based idleness (`awaitHarnessIdle`,
  `waitForHarnessIdleFallback`, or similar) before asserting
  on an `ask.generate mode=...` line or on
  `main.ask.prepare` clearance.
- Any assertion-message template that reports
  `busy[N]: main.ask.prepare` as the proximate failure cause.

If Step 1 surfaces more than one probe with the same pattern,
fix all of them in this slice (they share the bug; partial fix
would re-fragment the issue).

**Delegation:** this step is read-only. Spark xhigh scout
lane is appropriate if you want to parallelize with Step 3
test-design thinking. Otherwise main-inline is fine.

### Step 2 — Apply the handoff-awareness fix

Replace the stale-breadcrumb assertion pattern with:

1. After the ask is submitted, wait for the resumed activity
   to become `DetailActivity` (use
   `InstrumentationRegistry.getInstrumentation().runOnMainSync(...)`
   + `getResumedActivityOnMainThread()` — `PromptHarnessSmokeTest.java`
   already has helpers along these lines at
   `waitForDetailBodyReady` line 4006+).
2. Once `DetailActivity` is resumed, call the existing
   `assertDetailSettled(...)` / `waitForDetailBodyReady(...)`
   helper as the authoritative settled signal.
3. Remove or gate the old `awaitHarnessIdle` /
   `waitForHarnessIdleFallback` call on the ask-path if it
   was blocking on `main.ask.prepare` clearance. Keep the
   fallback for paths where it's still correct
   (non-generative search, in-MainActivity flows).
4. Update the failure-reporting text so the message names
   whichever activity is currently resumed — not just a raw
   `HarnessTestSignals.snapshot()` dump. Something like:
   `"focused host-ask probe did not reach a settled DetailActivity; resumedActivity=" + name + "; harness signals=" + snapshot()`.

If any probe additionally asserts that an `ask.generate mode=...`
line appeared in logcat (grep the file for `ask.generate` and
`mode=`), remove that assertion — R-host §2 confirmed this
breadcrumb is emitted only on the low-coverage route and is
NOT a general invariant. Replace with a rendered-state check
if mode validation is needed (e.g., detail-card body content
disambiguates confident vs. uncertain_fit vs. abstain; R-val2's
`assertGeneratedTrustSpineSettled(...)` already does this
shape).

### Step 3 — Add a regression test

At minimum one new `@Test` that:

- Launches `ActivityScenario<MainActivity>`, submits a host-ask,
  waits for `DetailActivity` resume + settle.
- Asserts the probe reaches a settled `DetailActivity` without
  relying on `main.ask.prepare` clearance.
- (Optional but recommended) Asserts the failure-message
  template includes the resumed-activity name if the settle
  times out — do this by crafting a path where the settle
  would fail and checking the exception message shape.

Name convention: mirror R-val2's
`previewLengthAnswerBodyDoesNotCountAsSettledDetail` shape —
behavior-descriptive rather than bug-numbered.

Use an existing prompt-fixture query that previously stalled
(per R-host diagnostic: rain_shelter). If the test would require
the host-inference endpoint to be up and that's not reliable in
the unit/instrumentation context, stub the handoff appropriately
— do not weaken the assertion to accommodate environment
fragility.

### Step 4 — Validate

Run:

```powershell
./gradlew.bat :app:compileDebugAndroidTestJavaWithJavac
./gradlew.bat :app:testDebugUnitTest
```

Unit suite must be 431/431 (or 431+N/431+N where N is the
number of new tests you added; confirm each new test name in
the report).

Instrumentation validation (running the new test(s) and any
focused rain_shelter probe against a live emulator) is
**deferred to a follow-up slice**. R-host here is the code fix;
the follow-up slice will rebuild androidTest, push to the matrix,
and run validation probes.

### Step 5 — Commit

Suggested message:
`R-host: probe handoff awareness — wait for DetailActivity settle, not main.ask.prepare`.

One commit. Stage only the touched file(s).

## Delegation hints

- Step 1 (source trace): Spark xhigh scout is appropriate for
  read-only grep across `PromptHarnessSmokeTest.java` (~4000
  lines). Main-inline also fine if you want continuity.
- Step 2 (implementation): main-inline only.
- Step 3 (test design): main-inline; borrow the R-val2 shape.
- Step 4 (validation): main-inline (Gradle runs).
- No MCP needed — the fix is within our own harness code, not
  framework API semantics.

## Anti-recommendations

- Do NOT add new production-side telemetry in this slice. R-host
  §7 flagged "dedicated explicit final-route telemetry point"
  as a SEPARATE follow-up. If you find yourself wanting to emit
  a new breadcrumb from `OfflineAnswerEngine` or
  `DetailActivity`, STOP and report — that is a different
  slice's scope, and conflating them reintroduces the exact
  observability-drift problem R-host diagnosed.
- Do NOT expand scope to `main.search` just because its flakes
  surfaced recently. Flake-check evidence (both 5554 and 5556,
  3/3 passes on re-run) plus the source read at
  `waitForResultsSettled` (`PromptHarnessSmokeTest.java:3980`)
  shows that lane's assertion already polls rendered state
  (RecyclerView adapter + header), not busy-signal clearance.
  It's a timing race, different bug class. File a separate
  slice if it recurs.
- Do NOT touch `OfflineAnswerEngine.resolveAnswerMode(...)` or
  any adjacent production gate logic. R-host explicitly ruled
  out (B) real host hang; production paths are fine. Any edit
  there is out-of-scope.
- Do NOT retune thresholds (`SEARCH_WAIT_MS`, detail wait
  timeouts, idle fallback windows). Timing retunes mask
  architectural issues rather than fix them, and R-host's
  recommended shape doesn't require them.

## Acceptance

- Single commit.
- Touched files are a subset of `PromptHarnessSmokeTest.java`
  and optionally `HarnessTestSignals.java`.
- `./gradlew.bat :app:testDebugUnitTest` passes (431/431 or
  higher including new tests).
- `./gradlew.bat :app:compileDebugAndroidTestJavaWithJavac`
  passes.
- At least one new behavior-descriptive `@Test` added.
- All previously-passing R-val2 / R-val3 tests still pass.
- No edits to tracker / queue / dispatch markdown.
- No production-code edits.

## Report format

Reply with:

- Commit sha + message.
- Files touched (diff summary `+X/-Y` per file).
- New test name(s) + what behavior each locks in.
- Unit suite result (`Tests run: N, Failures: 0`).
- androidTest compile result.
- Count of probes modified in Step 1 (if more than one).
- Any in-scope surprise (e.g., an `ask.generate mode=`
  assertion in a location R-host didn't call out) — name it,
  confirm it was fixed.
- Any out-of-scope drift spotted — flag only, don't fix.
- Delegation log (lane used per step).
