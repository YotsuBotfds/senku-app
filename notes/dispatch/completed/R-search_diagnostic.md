# Slice R-search — Diagnose recurring `searchQueryShowsResultsWithoutShellPolling` flake

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. Read-only
  diagnostic slice.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** safe to run concurrently with the
  flake-check3 re-run on 5554 (inline prompt pasted to the
  R-host validation window) — R-search is read-only and doesn't
  touch emulators, so emulator contention is not an issue.
  R-search can also incorporate flake-check3's verdict when it
  returns, if the diagnostic is still in flight.
- **Predecessor context:** R-host diagnostic
  (`notes/R-HOST_DIAGNOSTIC_20260420.md`) resolved the
  `main.ask.prepare` stall as harness precondition drift
  (cross-activity handoff awareness). During that investigation
  a sibling-but-distinct flake surfaced on
  `searchQueryShowsResultsWithoutShellPolling` with failure
  shape `results list never appeared; harness signals=busy[1]: main.search`.
  R-host's source read confirmed this is a **different bug
  class** — `waitForResultsSettled`
  (`PromptHarnessSmokeTest.java:3980`) polls the `R.id.results_list`
  RecyclerView adapter directly; it does NOT gate on
  `HarnessTestSignals.isIdle()` or `main.search` clearance.
  The busy snapshot in the failure message is diagnostic-only.
  That reads as a **timing race** on search completion.
  Incident log across 48 hours:
  1. 2026-04-20 evening — 5554 tablet_portrait fail during
     R-gal1 state-pack sweep; 3/3 cleared at
     `artifacts/external_review/rgal1_flakecheck_20260420_221049/`.
  2. 2026-04-21 early — 5556 phone_portrait fail during gallery
     finalization sweep; 3/3 cleared at
     `artifacts/external_review/rgal1_flakecheck2_5556_20260421_062052/`.
  3. 2026-04-21 — 5554 tablet_portrait fail during R-host
     validation sweep at
     `artifacts/cp9_stage2_post_r_host_20260421_065416/state_pack/`.
  Three incidents with the same failure shape on two different
  serials crosses the "flake vs. pattern" threshold. This slice
  is the R-host-equivalent diagnostic for `main.search` —
  localize the race, decide remediation shape, write a doc.

## Scope

Read-only diagnostic. Localize why
`searchQueryShowsResultsWithoutShellPolling` intermittently
fails to satisfy `waitForResultsSettled` within `SEARCH_WAIT_MS`,
and whether the cause is harness-side (timing/race), app-side
(search pipeline stall), or substrate-side (emulator load).
Do NOT fix in this slice. Do NOT rebuild the APK. Do NOT run
the emulator matrix. All evidence must come from:

1. Current source at HEAD (`1edde326` — R-host).
2. Captured artifacts from the three incidents above, plus
   (if available by dispatch time) the R-host flake-check3
   re-run artifact at
   `artifacts/external_review/rhost_flakecheck3_5554_*/`.
3. `git log` / `git blame` on the search lane's code paths
   (harness-side `waitForResultsSettled`; app-side search
   dispatch and busy-signal accounting).

Output: `notes/R-SEARCH_DIAGNOSTIC_20260421.md` with rooted
hypothesis + remediation scope recommendation.

## Context — what is known

- **Failure shape** (identical across all three incidents):
  `java.lang.AssertionError: results list never appeared; harness signals=busy[1]: main.search`
  at `PromptHarnessSmokeTest.java:2512` (`assertResultsSettled`),
  called from `PromptHarnessSmokeTest.java:275`
  (`searchQueryShowsResultsWithoutShellPolling`).
- **Wait criterion** (`PromptHarnessSmokeTest.java:3980`
  `waitForResultsSettled`): polls `R.id.results_list`
  RecyclerView adapter for `itemCount > 0` AND
  `R.id.results_header` text not containing "searching" /
  "failed". Deadline is `SEARCH_WAIT_MS` from caller.
- **Not a stale-breadcrumb bug.** Unlike `main.ask.prepare`
  (R-host), this probe's assertion does not gate on busy
  clearance — the busy snapshot appears only in the failure
  message for diagnostic context. So the failure *really
  does* mean "results list never rendered in time."
- **Query is `"fire"`** (`PromptHarnessSmokeTest.java:273`),
  hardcoded — same query every time. If this were a
  retrieval-quality issue, it would fail deterministically;
  intermittent failure points at timing/race rather than
  corpus/ranking.
- **Re-runs clear.** Both prior incidents cleared 3/3 when
  re-run on the same serial within hours. The flake is
  genuinely intermittent, not a hard regression.
- **R-host's androidTest change** added a new test
  (`hostAskProbeWaitsForSettledDetailActivityAfterMainHandoff`)
  that runs on host-inference postures (incl. tablet_portrait
  5554 and tablet_landscape 5558). This increases per-serial
  state-pack sweep runtime on those postures. Load-induced
  timing drift is a plausible secondary effect, though the
  prior two flakes pre-date R-host.

## The work

### Step 1 — Source trace the search lane

Map the full path from `submitSearchFromResumedActivity(...)`
through to `R.id.results_list` populating:

- Harness side (`PromptHarnessSmokeTest.java`):
  - `submitSearchFromResumedActivity` (find and cite lines)
  - `assertResultsSettled` at line 2511
  - `waitForResultsSettled` at line 3980
  - Any helpers called inside the poll loop
- App side (`MainActivity.java` and adjacent):
  - Where `main.search` busy token is created (grep
    `beginHarnessTask("main.search")` or
    `beginHarnessTask.*search` pattern in
    `android-app/app/src/main/java/`)
  - Where the token is cleared / decremented
  - What triggers the search results to populate the
    RecyclerView adapter (async task? view model? direct
    SQLite query + adapter.notifyDataSetChanged()?)
  - Whether the search pipeline runs on the main thread, a
    background thread, or an AsyncTask-equivalent

Relevant knobs/constants:
- `SEARCH_WAIT_MS` in `PromptHarnessSmokeTest.java` (find
  and cite the value)
- Any search timeout / debounce in the app-side search
  dispatcher

**Delegation:** Spark xhigh scout is ideal for this read-only
grep/trace. Main-inline also fine.

### Step 2 — Compare the three incident artifacts

For each of the three incident folders plus (if available)
the flake-check3 re-run folder:

1. `artifacts/external_review/rgal1_flakecheck_20260420_221049/`
   (5554 3/3 pass)
2. `artifacts/external_review/rgal1_flakecheck2_5556_20260421_062052/`
   (5556 3/3 pass)
3. `artifacts/cp9_stage2_post_r_host_20260421_065416/state_pack/matrix_20260421_065416/summaries/tablet_portrait/searchQueryShowsResultsWithoutShellPolling/`
   and paired `raw/.../emulator-5554/instrumentation.txt`
   (fresh 5554 fail)
4. (optional) `artifacts/external_review/rhost_flakecheck3_5554_*/`
   if flake-check3 has run by dispatch time

Extract per-incident:
- Wall-clock time from fixture start to pass/fail.
- Instrumentation elapsed time.
- Any surrounding fixture timing that might indicate emulator
  load (what ran before this fixture on the same serial, and
  how long it took).
- Raw logcat lines mentioning `main.search` (emission / clear),
  `results_list`, and any search-dispatcher activity (if the
  state-pack lane's `logcat_path: null` gap has been closed;
  otherwise note the evidence gap explicitly).
- The value of `SEARCH_WAIT_MS` at the time of each run.

Note: R-host diagnostic §8 flagged that state-pack summaries
report `logcat_path: null`, so raw logcat from state-pack runs
may not be available for the 3rd incident. That's a known
tooling gap; work around it.

### Step 3 — Identify the remediation shape

Given Steps 1 and 2, recommend one of:

- **(A) Harness wait-window too tight.** `SEARCH_WAIT_MS` is
  set below the 95th-percentile observed search completion
  time on slow emulators. Fix: bump the constant (by how
  much, data-driven). Scope: 1 LoC + one regression
  observation.
- **(B) Search pipeline thread race.** Results list update
  is racy w.r.t. the poll (e.g., adapter.notifyDataSetChanged()
  fires before itemCount is visible through a subsequent
  `findViewById` read). Fix: harness polls a different
  surface (e.g., observe the adapter directly, or read
  RecyclerView item count via a safer accessor). Scope:
  harness-side.
- **(C) App-side busy-signal misaccounting.** `main.search`
  busy token is created but never cleared on some code
  path (e.g., zero-result search, exception-handled search,
  specific query class). Fix: find the missing pop. Scope:
  app-side, could regress other lanes so needs care.
- **(D) Emulator load / test ordering.** Tests before
  `searchQueryShowsResultsWithoutShellPolling` leave the
  emulator in a warmer state on some runs, pushing the
  search pipeline over the wait window. Fix: isolate the
  test or reset emulator state before it runs. Scope:
  instrumentation harness or matrix orchestration.
- **(E) Intermittent transient with no fixable root cause.**
  If the evidence doesn't converge on any of A-D, recommend
  formal documentation of the flake as a known intermittent
  with a retry policy (either in-harness retry, or
  state-pack-lane-level "accept 1-of-N on this fixture")
  rather than a code fix. Less satisfying but epistemically
  honest.

R-host's diagnostic doc is the style template — lead with
evidence, verdict second, remediation scope third.

## Boundaries (HARD GATE)

- Read-only. No source edits. No test edits.
- No APK rebuild. No emulator interaction.
- No commit.
- Do not edit any tracker / queue / dispatch markdown.
- Doc output ONLY at `notes/R-SEARCH_DIAGNOSTIC_20260421.md`.
- If the diagnostic lands and recommends remediation shape
  (A), (B), (C), or (D), the planner will draft the fix slice
  separately — do not draft the fix in this file.
- Do NOT touch `main.ask.prepare`, `PromptHarnessSmokeTest.java`
  host-inference probes, or R-host's remediation. R-host's
  scope is closed.
- Do NOT retune `SEARCH_WAIT_MS` or any other constant here —
  that's the remediation slice's decision, informed by this
  diagnostic's data.

## Delegation hints

- Step 1 (source trace): Spark xhigh scout lane is appropriate
  for read-only grep. Main-inline also fine for continuity.
- Step 2 (artifact comparison): main-inline preferred — requires
  judgment about which timing signals are meaningful.
- Step 3 (verdict): main-inline.
- No MCP hint needed. `main.search` and `SEARCH_WAIT_MS` are
  our own code; no framework-API lookup.

## Acceptance

- `notes/R-SEARCH_DIAGNOSTIC_20260421.md` exists with:
  - Step 1 trace: `main.search` emission/clear locations +
    app-side search dispatch thread model + `SEARCH_WAIT_MS`
    value.
  - Step 2 trace: per-incident timing data where available;
    logcat-gap explicit where not.
  - Step 3 verdict: one of (A) / (B) / (C) / (D) / (E) or a
    new named shape.
  - Remediation scope recommendation (file / rough LoC, not
    code).
- No source edits. No commit.

## Report format

Reply with:

- Verdict: (A) / (B) / (C) / (D) / (E) / other.
- One-sentence root cause.
- Path to the diagnostic doc.
- Remediation slice shape recommendation (file + one-paragraph sketch).
- Any surprising cross-cutting finding — flag, don't fix.
- Delegation log (lane used per step).
