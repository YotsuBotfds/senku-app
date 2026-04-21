# Slice R-telemetry — Explicit `final_mode` breadcrumb at every ask terminal return

- **Role:** main agent (`gpt-5.4 xhigh`). Main-inline throughout; sidecar-eligible for the test-writing step if desired.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** nothing in flight at dispatch time. Not parallel-safe with any other `OfflineAnswerEngine.java` or `OfflineAnswerEngineTest.java` slice.
- **Predecessor context:** R-host diagnostic (`notes/R-HOST_DIAGNOSTIC_20260420.md` §2, §7) found that `ask.generate mode=...` is emitted **only on the low-coverage downgrade path** — confident, early-abstain, early-uncertain-fit, deterministic, and source-summary-fallback terminals are all silent. R-host's landed probe fix (`1edde326`) works around this by asserting on `DetailActivity` rendered-surface state. That's correct for the probe, but leaves post-chain observability dependent on indirect signals. R-host §7 flagged this slice as the separate follow-up that gives future harness probes (and future planners) a direct truth surface. Forward research at `notes/R-TELEMETRY_FORWARD_RESEARCH_20260421.md` did the topology map; this slice draft closes the four open questions and cites file:line independently.

## Scope

Add a dedicated `ask.generate final_mode=<mode> route=<string> query="..." totalElapsedMs=<ms>` log line at every terminal return from `OfflineAnswerEngine.generate(...)` in `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`. One emission per ask lifecycle, no double-emission. Introduces a small private helper to enforce format consistency. Adds 7 unit tests to `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java` — one per terminal route plus one regression guard that asserts exactly-one emission. Purely additive: no existing log line is removed or renamed; no production behavior changes; no harness or script edits.

## Preconditions (HARD GATE — STOP if violated)

1. HEAD is `35d7cae` (D5 tracker reconciliation) OR a later doc-only commit. Verify via `git log --oneline 35d7cae..HEAD` — if any non-doc commit touches `OfflineAnswerEngine.java` or `OfflineAnswerEngineTest.java`, STOP and report (slice would then need rebasing).
2. Unit suite at HEAD passes clean: `./gradlew.bat :app:testDebugUnitTest` returns `Tests run: 431, Failures: 0` (the exact figure will become 438 after this slice; any baseline drift from 431 means a predecessor slice shifted the floor and this slice's preconditions need updating).
3. `OfflineAnswerEngine.java` line-number anchors match:
   - Line 21: `private static final String TAG = "SenkuMobile";`
   - Line 22: `private static final Locale QUERY_LOCALE = Locale.US;`
   - Line 148-152: `public enum AnswerMode { CONFIDENT, UNCERTAIN_FIT, ABSTAIN }`
   - Line 437-466: deterministic terminal return (`return answerRun;` at 465)
   - Line 467-493: abstain terminal return (`return new AnswerRun(...);` at ~480)
   - Line 494-521: uncertain_fit terminal return (`return new AnswerRun(...);` at ~507)
   - Line 594-596: post-generation `ask.generate query=... totalElapsedMs=` line (existing, stays)
   - Line 612-616: `usedSourceFallback = true` assignment inside `if (resolvedAnswer.isEmpty())`
   - Line 640-651: low-coverage downgrade return (`return downgradedRun;` at 651)
   - Line 653-669: confident terminal return (`return answerRun;` at 669)
   - Line 1107-1119: `normalizeAnswerMode(...)` helper
   - Line 1892-1897: `logDebug(String, String)` helper (calls `debugLogSink.accept(...)`)
   - Line 2080-2115: `buildLowCoverageDowngradeAnswerRun(...)` with existing `ask.generate low_coverage_route query="X" mode=<X>` emission at line 2094-2098
   If any anchor is off by more than ±5 lines, STOP and report — the file has drifted and the emit-site picks below need re-anchoring.

If any precondition fails, STOP and report before touching code.

## Backward-compat audit (pre-verified by planner — do not re-do unless you distrust)

Planner grepped all live code paths for consumers of the existing `ask.generate` / `ask.abstain` / `ask.uncertain_fit` / `low_coverage_route` / `final_mode` strings on 2026-04-21 day:

- `android-app/app/src/main/` → only producer is `OfflineAnswerEngine.java`; no consumer elsewhere.
- `android-app/app/src/androidTest/` → **zero matches**. R-host (`1edde326`) removed all harness dependency on these strings.
- `android-app/app/src/test/` → one match: `OfflineAnswerEngineTest.java:1117` filters for `message.startsWith("ask.latency")`, which does NOT collide with the new `ask.generate final_mode=` prefix.
- `scripts/` → **zero matches**. No rollup, harness, or automation script parses these strings.
- `artifacts/` matches are all in frozen archive/review bundles (old build shadows under `artifacts/tmp/`, `artifacts/external_review/...`); not live code.

**Conclusion: this slice is purely additive with zero consumer risk.** No existing emissions are removed or renamed. The new prefix `ask.generate final_mode=` is distinct from every existing `ask.generate ...` shape, so future parsers can unambiguously distinguish the new marker.

## Outcome

- One new private helper `logAskFinalMode(String query, AnswerMode finalMode, String route, long totalElapsedMs)` in `OfflineAnswerEngine.java`, centrally responsible for emission format.
- Five call sites in `generate(...)` emit via the helper, one per terminal return path:
  - DETERMINISTIC → `final_mode=confident route=deterministic`
  - ABSTAIN (early, from `prepare()`) → `final_mode=abstain route=early_abstain`
  - UNCERTAIN_FIT (early, from `prepare()`) → `final_mode=uncertain_fit route=early_uncertain_fit`
  - LOW_COVERAGE_DOWNGRADE → `final_mode=<abstain|uncertain_fit> route=low_coverage_downgrade` (sub-mode from `downgradedMode` boolean)
  - CONFIDENT (standard or source-summary-fallback) → `final_mode=confident route=<confident_generation|source_summary_fallback>`
- Existing `ask.generate low_coverage_route query="X" mode=<X>` at line 2094-2098 is **kept unchanged** (additive — removing it is a separate hygiene slice if any consumer ever surfaces; "keep both" is safest).
- Existing `ask.abstain`, `ask.uncertain_fit`, `ask.prompt`, `ask.generate query=... totalElapsedMs=...`, `ask.generate host_failed_falling_back`, `ask.generate using_streamed_fallback`, `ask.generate preferring_richer_stream`, `ask.generate using_source_summary_fallback`, `ask.generate low_coverage_detected` emissions are **unchanged**.
- Seven new unit tests in `OfflineAnswerEngineTest.java`, one per terminal route plus one exactly-one-emission regression guard.
- Unit suite 438/438 passes (431 baseline + 7 new).
- Single commit.

## Boundaries (HARD GATE)

- Touch only:
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
- Do NOT:
  - Touch any other production or test file, including `DetailActivity.java`, `PromptHarnessSmokeTest.java`, `PackRepository.java`, `QueryMetadataProfile.java`, etc.
  - Change the existing `ask.generate low_coverage_route query="X" mode=<X>` line at 2094-2098. It stays as-is for this slice.
  - Add any `final_mode=` emission anywhere OTHER than at the five terminal return sites. In particular, do NOT emit from inside `resolveAnswerMode(...)` — that's a double-emission risk flagged in the research (mode can flip post-generation via low-coverage downgrade).
  - Emit `final_mode=` from `prepare(...)`. Prepare's existing `ask.abstain`, `ask.uncertain_fit`, `ask.prompt` markers stay as-is. The new `final_mode=` emission belongs exclusively to `generate(...)` terminal returns — this is what gives the marker its "one per ask lifecycle at the very end of the pipeline" guarantee.
  - Change the `AnswerMode` enum or `normalizeAnswerMode(...)` helper.
  - Change `buildLowCoverageDowngradeAnswerRun(...)` signature or return type — add the emission via the call site at line 651 in `generate(...)`, NOT inside the builder, because the builder is reused.
  - Add `evidenceLevel`, `metadataProfile`, `preferredStructureType`, `confidenceLabel`, or any classification-fingerprint field to the new emission. Those belong in the separately-tracked ask-telemetry-enrichment slice; conflating scope here makes both slices harder to review and land.
  - Emit a `final_mode=` line for host-fallback, streamed-fallback, preferring-richer-stream, or low-coverage-detected intermediate states — those are NOT terminal returns (generation continues past each of them).
  - Add a retry rule, instrumentation, UI log, or any other observability surface that isn't the single `logDebug` call.
  - Edit any tracker, queue, or dispatch markdown.
  - Re-run instrumentation on any emulator. This slice is additive telemetry only; unit-suite gate is sufficient acceptance. No state-pack sweep, no gallery republish.
- Single commit. Message: `R-telemetry: emit ask.generate final_mode=<mode> route=<route> at every terminal return`.

## The work

### Step 1 — Add the private emission helper

Insert a new static helper method immediately after `logDebug(String tag, String message)` at ~line 1897, before `logToAndroidDebug(...)` at ~line 1899. Scope: `private static`.

Exact shape:

```java
private static void logAskFinalMode(
    String query,
    AnswerMode finalMode,
    String route,
    long totalElapsedMs
) {
    logDebug(
        TAG,
        "ask.generate final_mode=" + finalMode.name().toLowerCase(QUERY_LOCALE) +
            " route=" + route +
            " query=\"" + safe(query) + "\"" +
            " totalElapsedMs=" + Math.max(0L, totalElapsedMs)
    );
}
```

Notes:
- Uses `safe(query)` — same `safe(...)` helper used by `logFirstTokenMs` at line 676. If `safe(...)` isn't visible from this scope, fall back to `(query == null ? "" : query)` (check the file for the visibility of `safe(...)` before choosing).
- `Math.max(0L, totalElapsedMs)` mirrors the defensive clamping used in `LatencyBreakdown` constructor at line 2419-2424.
- `.name().toLowerCase(QUERY_LOCALE)` matches the existing low-coverage emission style at line 2097, so `AnswerMode.ABSTAIN` → `"abstain"`, `AnswerMode.UNCERTAIN_FIT` → `"uncertain_fit"`, `AnswerMode.CONFIDENT` → `"confident"`.
- Level: `logDebug`. Rationale: matches every existing non-error `ask.*` marker (all at debug). Warn is reserved for anomalies (host_failed, streamed_fallback, preferring_richer_stream, source_summary_fallback). The `final_mode` line is not an anomaly — it's normal terminal state.

### Step 2 — Add emission at each of the five terminal return sites in `generate(...)`

Each site gets exactly one `logAskFinalMode(...)` call, placed **immediately before the `return ...` statement** so the emit is the last thing that happens before control leaves the function.

**Site A — DETERMINISTIC terminal return (~line 465)**

Currently:
```
AnswerRun answerRun = new AnswerRun(
    prepared.query,
    prepared.answerBody,
    ...
);
rememberSessionLatencyBreakdown(answerRun);
return answerRun;
```

Add before `return answerRun;`:
```java
logAskFinalMode(prepared.query, AnswerMode.CONFIDENT, "deterministic", totalMs);
```

`totalMs` is already in scope at this point (declared at line 438). Use that local, do NOT recompute.

**Site B — ABSTAIN early terminal return (~line 480, inside `if (prepared.abstain)` block at line 467)**

Currently (line 480-492):
```
return new AnswerRun(
    prepared.query,
    prepared.answerBody,
    ...
    "Abstain | no guide match | instant",
    "",
    latencyBreakdown,
    prepared.confidenceLabel
);
```

Add before the `return new AnswerRun(...);` expression:
```java
logAskFinalMode(prepared.query, AnswerMode.ABSTAIN, "early_abstain", totalMs);
```

`totalMs` is in scope from line 468.

**Site C — UNCERTAIN_FIT early terminal return (~line 507, inside `if (prepared.mode == AnswerMode.UNCERTAIN_FIT)` at line 494)**

Currently (line 507-520):
```
return new AnswerRun(
    prepared.query,
    prepared.answerBody,
    ...
    "Uncertain fit | related guides | instant",
    "",
    latencyBreakdown,
    prepared.confidenceLabel,
    prepared.mode
);
```

Add before the `return new AnswerRun(...);` expression:
```java
logAskFinalMode(prepared.query, AnswerMode.UNCERTAIN_FIT, "early_uncertain_fit", totalMs);
```

`totalMs` is in scope from line 495.

**Site D — LOW_COVERAGE_DOWNGRADE terminal return (~line 651)**

Currently (line 640-652):
```
if (lowCoverage) {
    AnswerRun downgradedRun = buildLowCoverageDowngradeAnswerRun(
        modelFile,
        prepared,
        elapsedMs,
        subtitle,
        latencyBreakdown,
        hostBackendUsed,
        hostFallbackUsed
    );
    rememberSessionLatencyBreakdown(downgradedRun);
    return downgradedRun;
}
```

Add before `rememberSessionLatencyBreakdown(downgradedRun);`:
```java
logAskFinalMode(prepared.query, downgradedRun.mode, "low_coverage_downgrade", elapsedMs);
```

Rationale for using `downgradedRun.mode`: the downgrade sub-mode (ABSTAIN vs UNCERTAIN_FIT) is decided inside `buildLowCoverageDowngradeAnswerRun` at line 2093 via `abstainShape`. The returned `AnswerRun.mode` field carries that decision out. `elapsedMs` is declared at line 594.

Note: the existing `ask.generate low_coverage_route query="X" mode=<X>` inside `buildLowCoverageDowngradeAnswerRun` at line 2094-2098 stays untouched. Two lines emit for this path (intentional — existing line is intermediate-observability, new line is canonical terminal).

**Site E — CONFIDENT terminal return (~line 669)**

Currently (line 653-669):
```
AnswerRun answerRun = new AnswerRun(
    prepared.query,
    answerBody,
    prepared.sources,
    elapsedMs,
    prepared.sessionUsed,
    false,
    false,
    subtitle,
    null,
    latencyBreakdown,
    prepared.confidenceLabel,
    hostBackendUsed,
    hostFallbackUsed
);
rememberSessionLatencyBreakdown(answerRun);
return answerRun;
```

Add before `rememberSessionLatencyBreakdown(answerRun);`:
```java
String confidentRoute = usedSourceFallback ? "source_summary_fallback" : "confident_generation";
logAskFinalMode(prepared.query, AnswerMode.CONFIDENT, confidentRoute, elapsedMs);
```

`usedSourceFallback` is declared at line 599 and potentially set true at line 615. `elapsedMs` is from line 594. Both are in scope.

### Step 3 — Add unit tests to `OfflineAnswerEngineTest.java`

Location: append after the existing `generateLogsRegexParseableLatencySummaryLine` test (ends ~line 1153) or cluster with `generateReturnsAbstainWithoutCallingGenerators` / `generateReturnsUncertainFitWithoutCallingGenerators` (lines ~1749-1833) — either placement is fine; pick what reads naturally.

All seven tests share a log-capture pattern modeled on `generateLogsRegexParseableLatencySummaryLine` at line 1113-1153. Extract a small private helper to avoid repetition:

```java
private static java.util.List<String> captureFinalModeLines(
    java.util.function.Consumer<OfflineAnswerEngine.PreparedAnswer> exerciseGenerate
) throws Exception {
    // Caller constructs the PreparedAnswer and calls the returned runner.
    // This helper only wires the sink + filter.
    throw new UnsupportedOperationException("inline the capture block per test");
}
```

Actually a helper here risks over-abstraction; the research pattern at line 1113-1153 is ~40 LoC and fine to duplicate per test. Duplicate it — each test stays readable in isolation.

**Test 1 — `generateDeterministicPathEmitsFinalModeConfidentRouteDeterministicTelemetry`**

Shape:
1. `AtomicReference<List<String>> finalModeLines = new AtomicReference<>(new ArrayList<>());`
2. `OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> { if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) finalModeLines.get().add(message); });`
3. Construct a deterministic PreparedAnswer via `OfflineAnswerEngine.PreparedAnswer.restoredDeterministic(...)` at line 2498 (check the factory signature — it may need a ruleId + answerBody).
4. Call `OfflineAnswerEngine.generate(null, tempModel, prepared);`.
5. Assert `finalModeLines.get().size() == 1` and the captured line matches `ask.generate final_mode=confident route=deterministic query="<expected>" totalElapsedMs=\d+`.

Use a regex matcher via `java.util.regex.Pattern` similar to line 1149-1151.

**Test 2 — `generateEarlyAbstainPathEmitsFinalModeAbstainRouteEarlyAbstainTelemetry`**

Same capture pattern. Use `PreparedAnswer.abstain(...)` to construct the prepared — same signature as line 1759-1767 or 2703-2735. Assert exactly one line matching `ask.generate final_mode=abstain route=early_abstain query="..." totalElapsedMs=\d+`.

**Test 3 — `generateEarlyUncertainFitPathEmitsFinalModeUncertainFitRouteEarlyUncertainFitTelemetry`**

Same capture pattern. Use `PreparedAnswer.uncertainFit(...)` to construct the prepared — same signature as line 1806-1822. Assert exactly one line matching `ask.generate final_mode=uncertain_fit route=early_uncertain_fit query="..." totalElapsedMs=\d+`.

**Test 4 — `generateConfidentPathEmitsFinalModeConfidentRouteConfidentGenerationTelemetry`**

Same capture pattern. Use `PreparedAnswer.restoredGenerative(...)` at line 2771 or 2796 with a non-empty query + sources + injected generators that return a non-low-coverage answer body (e.g., "Short answer: on-device path." following the line 1128-1131 example). Assert exactly one line matching `ask.generate final_mode=confident route=confident_generation query="..." totalElapsedMs=\d+`.

To ensure `usedSourceFallback` is false, the on-device generator must return a non-empty resolvedAnswer. Use a non-trivial body and pass it through `listener.onPartialText(...)`.

To ensure `lowCoverage` is false, the answer body must not match `PromptBuilder.isLowCoverageAnswer(...)` semantics — check that helper's behavior before choosing test text. If unclear, reuse the exact text from line 1165 (`"Short answer: confidence path."`) which the existing confidence-label test uses successfully through the confident path.

**Test 5 — `generateLowCoverageDowngradePathEmitsFinalModeAbstainOrUncertainFitRouteLowCoverageDowngradeTelemetry`**

Trigger via generated body that passes `PromptBuilder.isLowCoverageAnswer(resolvedAnswer)`. The exact trigger text may need investigation — grep `PromptBuilder.isLowCoverageAnswer` for its definition, or look at how R-eng (`1f76ccf`) and R-eng2 (`8990cc6`) test this path.

Assert exactly one line matching `ask.generate final_mode=(abstain|uncertain_fit) route=low_coverage_downgrade query="..." totalElapsedMs=\d+`. Accept either sub-mode since the `abstainShape` logic at line 2088 decides dynamically — the test should assert the route string is `low_coverage_downgrade` and the mode is one of the two non-CONFIDENT values, not pinning to a specific sub-mode unless the fixture deterministically forces one.

**Test 6 — `generateSourceSummaryFallbackPathEmitsFinalModeConfidentRouteSourceSummaryFallbackTelemetry`**

Trigger the source-summary-fallback path at line 612-616 by ensuring `resolvedAnswer.isEmpty()` at line 612. `generate(...)` at lines 531-577 only invokes the on-device generator when host is **disabled** (`inferenceSettings.enabled == false`) OR host **throws**. Pick one of the two branches below — NOT "both empty", since a host-success with `""` means on-device never runs:

- **Branch A (host disabled):** use `restoredGenerative` with `hostEnabled=false` and an on-device generator that returns `""` and never calls `listener.onPartialText(...)` (so `bestStreamCandidate.bestRawText` stays empty).
- **Branch B (host throws):** use `restoredGenerative` with `hostEnabled=true`, inject a host generator that throws a non-fatal `Exception`, AND inject an on-device generator that returns `""` and never calls the listener. This exercises the host-fallback path into the source-summary lane.

Either branch lands at Site E with `usedSourceFallback == true`, `lowCoverage == false` (forced by the `!usedSourceFallback` gate at line 617). Assert exactly one line matching `ask.generate final_mode=confident route=source_summary_fallback query="..." totalElapsedMs=\d+`. Pick Branch A for the primary test; Branch B is optional as a second test if you want explicit host-fallback-into-source-summary coverage.

**Test 7 — `generateEachTerminalReturnEmitsExactlyOneFinalModeLine` (regression guard)**

Exercise **all six routes** in sequence (six separate `generate(...)` calls, each with a different fixture), accumulate all `ask.generate final_mode=...` lines, and assert:
- Exactly 6 lines captured total (one per call).
- Each call produces exactly one line (use per-call counting, not just a final total — reset or snapshot the capture between calls so a double-emission on one path + zero-emission on another doesn't cancel out).
- The set of `route=` values captured equals exactly `{deterministic, early_abstain, early_uncertain_fit, low_coverage_downgrade, confident_generation, source_summary_fallback}` — no substitutions, no optional omissions. If `source_summary_fallback` silently regresses (e.g., a future refactor makes line 612 unreachable), omitting it from this guard would let the regression pass unnoticed.
- Each captured line's `final_mode=<X>` value is consistent with its `route=<Y>` value per the mapping: `deterministic→confident`, `early_abstain→abstain`, `early_uncertain_fit→uncertain_fit`, `low_coverage_downgrade→abstain|uncertain_fit` (accept either), `confident_generation→confident`, `source_summary_fallback→confident`. Catches the "wrong mode on right route" failure class.

This test is the future-proofing regression guard. If someone adds a new terminal return path without a `logAskFinalMode(...)` call, or accidentally emits twice from one path, or emits with mismatched mode/route values, this test fails immediately.

### Step 4 — Compile and run the unit suite

```powershell
./gradlew.bat :app:compileDebugJavaWithJavac
./gradlew.bat :app:compileDebugUnitTestJavaWithJavac
./gradlew.bat :app:testDebugUnitTest
```

All three must pass. Expected count: **438/438** (baseline 431 + 7 new). If the count is 431 exactly or any test fails, STOP — either the tests aren't being picked up (check package declaration, `@Test` annotations, method visibility — must be `public void`), or the emission sites don't fire. Read the failure output and diagnose before proceeding.

### Step 5 — Commit

```powershell
git add android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java
git commit -m "R-telemetry: emit ask.generate final_mode=<mode> route=<route> at every terminal return"
```

Single commit. Only these two files staged. No other files should appear in `git diff HEAD~1` for this commit.

## Acceptance

- Single commit. Only `OfflineAnswerEngine.java` (+helper + 5 emissions) and `OfflineAnswerEngineTest.java` (+7 tests) touched.
- `./gradlew.bat :app:compileDebugJavaWithJavac` passes.
- `./gradlew.bat :app:compileDebugUnitTestJavaWithJavac` passes.
- `./gradlew.bat :app:testDebugUnitTest` reports 438/438 (baseline 431 + 7 new).
- Existing `ask.generate low_coverage_route query="X" mode=<X>` emission at line 2094-2098 is unchanged (verify via `grep -n 'low_coverage_route' android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`).
- Existing `ask.abstain`, `ask.uncertain_fit`, `ask.prompt`, `ask.generate query=... totalElapsedMs=...`, `ask.generate host_failed_falling_back`, `ask.generate using_streamed_fallback`, `ask.generate preferring_richer_stream`, `ask.generate using_source_summary_fallback`, `ask.generate low_coverage_detected` emissions are all unchanged (verify via grep — producer-line counts before and after should match modulo the 5 new helper invocations).
- No emulator run, no state-pack sweep, no APK rebuild, no gallery republish.
- No tracker / queue / dispatch markdown edits.
- `git status` on scoped files shows clean after commit.

## Delegation hints

- **Step 1 (helper)**: main-inline. Trivial.
- **Step 2 (5 emissions)**: main-inline. Precise surgery; each site is <5 lines.
- **Step 3 (7 tests)**: main-inline OR sidecar. Tests are mechanical but verbose — if you want to offload the log-capture boilerplate, OpenCode sidecar is a reasonable fit. Same-file writes, so don't run sidecar in parallel with anything else touching this test file.
- **Step 4 (compile + test)**: main-inline.
- **Step 5 (commit)**: main-inline.

**MCP hints:** none needed. No framework-library question in scope. Standard Java + existing `setDebugLogSinkForTest` pattern.

## Anti-recommendations

- Do NOT emit `final_mode=` from `resolveAnswerMode(...)`. That function is pure (no side effects) and calls from multiple paths; emitting there creates double-emission risk when low-coverage later flips the mode. Keep emission strictly at terminal returns in `generate(...)`.
- Do NOT emit from `prepare(...)`. Prepare's existing `ask.abstain` / `ask.uncertain_fit` / `ask.prompt` markers stay as-is. New `final_mode=` is generate-only — that's what gives the "one per ask lifecycle" guarantee.
- Do NOT rename `ask.generate low_coverage_route query="X" mode=<X>` to the new format. Keep-both is safer; rename is a separate hygiene slice if a consumer is ever flagged.
- Do NOT add emissions for `ask.generate host_failed_falling_back`, `ask.generate using_streamed_fallback`, `ask.generate preferring_richer_stream`, `ask.generate using_source_summary_fallback`, or `ask.generate low_coverage_detected` — these are INTERMEDIATE states, not terminals. Generation continues past each of them. Emitting `final_mode=` there would be a false terminal claim.
- Do NOT add `evidenceLevel`, `metadataProfile`, `preferredStructureType`, `confidenceLabel`, `hostBackendUsed`, `hostFallbackUsed`, or any other ask-profile field to the `final_mode` emission. Those belong in the separately-tracked ask-telemetry-enrichment slice. Scope separation keeps both slices reviewable.
- Do NOT change the `AnswerMode` enum (adding values, renaming, etc.).
- Do NOT add a `logAskFinalMode` variant that takes additional arguments — one overload, one shape, five call sites. The helper exists to enforce format consistency; overloading dilutes that.
- Do NOT bypass the `logDebug` path to use `Log.d(...)` directly. Tests rely on `debugLogSink` interception via `setDebugLogSinkForTest`.
- Do NOT run state-pack sweep, focused probe, or gallery republish. Additive telemetry; unit-suite gate is sufficient.
- Do NOT widen scope by fixing unrelated issues spotted in `OfflineAnswerEngine.java`. File as out-of-scope observation if you spot something; next slice handles.

## Report format

Reply with:

- Commit sha + message.
- Files touched with `+X/-Y` counts.
- Unit suite result (`Tests run: 438, Failures: 0`).
- Compile results for debug + debugUnitTest targets.
- `grep -n 'low_coverage_route' android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java` output (should show the existing line 2094-2098 unchanged).
- `grep -cn 'ask\.generate final_mode=' android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java` output (should be 1 — the helper's `logDebug` call).
- `grep -cn 'logAskFinalMode' android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java` output (should be 6 — 1 helper definition + 5 call sites).
- `grep -cn 'ask\.generate final_mode=' android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java` output (should be ≥7 — test regex patterns + expectations).
- Any out-of-scope drift noticed — flag, don't fix.
- Delegation log (lane used per step).

## If anything fails during Step 4

- **Tests compile but fail:** capture the failure message and stop. Common suspects: (a) `PromptBuilder.isLowCoverageAnswer` doesn't flag the chosen fixture text — check the helper's definition and pick a different fixture; (b) `usedSourceFallback` path doesn't trigger because the on-device generator returns non-empty via partial emissions — set up the generator to return empty AND suppress partial emissions; (c) deterministic path requires a matching rule in `DeterministicAnswerRouter` — use `restoredDeterministic` factory which bypasses the router.
- **Compile fails:** most likely `safe(...)` or `QUERY_LOCALE` scoping. Cross-reference lines 676 (safe) and 22 (QUERY_LOCALE) — both are accessible from within the same class; adjust if somehow they aren't.
- **Unit suite count differs from 438:** either a predecessor slice shifted the baseline (verify `./gradlew.bat :app:testDebugUnitTest` at commit `35d7cae` to confirm 431, then re-run at HEAD), or one of the new tests isn't being picked up (missing `@Test`, non-public method, wrong package). Diagnose before moving forward.

**If blocked at Step 4 for more than 30 minutes, STOP and report.** Do not add retries, fallbacks, or workarounds to the emission sites — the slice's correctness depends on exactly-one-per-path emission. A hung test is a signal, not a nuisance.
