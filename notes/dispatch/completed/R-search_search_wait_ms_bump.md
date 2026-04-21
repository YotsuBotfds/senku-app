# Slice R-search — Bump `SEARCH_WAIT_MS` to absorb normal search timing variance

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. Small scope,
  no delegation needed.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** nothing in flight. Safe to run standalone.
- **Predecessor context:** R-search diagnostic
  (`notes/R-SEARCH_DIAGNOSTIC_20260421.md`) returned verdict
  **(A) harness wait-window too tight**. `SEARCH_WAIT_MS` is
  `10_000L`, but the `"fire"` search path already logs as a slow
  query at **5.8–6.2s** on clean 5554 reruns. Normal
  emulator/runtime variance intermittently pushes completion
  over 10s, leaving `main.search` genuinely in flight when the
  harness times out — not a race, not a missing pop, not a
  RecyclerView visibility issue. Three incidents across 48
  hours on two different serials (5554 twice, 5556 once) all
  cleared 3/3 on re-run, consistent with a pure timing-budget
  miss. The diagnostic explicitly rules out RecyclerView poll
  rewrite and app-side busy-token fix:
  `SearchResultAdapter.getItemCount()` is a direct
  `results.size()`, and `main.search` is created + cleared
  correctly. The remediation is a single constant bump plus
  focused re-validation.

## Scope

One-line constant change plus a brief comment citing the
empirical timing data. Unit suite + androidTest rebuild + push
to all four serials + focused re-run on the two serials that
have flaked (5554, 5556). Single commit. Doc-only validation
rollup.

## Preconditions (HARD GATE — STOP if violated)

1. HEAD is `1edde32689c7cd1175248229ab2cc8b9603a143c` (R-host)
   OR a later doc-only commit. Verify
   `git log --oneline 1edde326..HEAD` shows only doc-only
   commits (if any); if any `android-app/` file drift is
   present, STOP and report.
2. Unit suite 431/431 at HEAD:
   `./gradlew.bat :app:testDebugUnitTest` passes clean.
3. All four emulator serials online:
   `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe devices`
   shows 5556, 5560, 5554, 5558 all as `device`. Do NOT use
   the scrcpy-bundled adb.
4. Per-serial `sys.boot_completed == 1`.
5. Live debug APK
   `99e2bfde98acdd425c9318e0d2b7ad919b14c0043898e7fb0a394ead2ac3c6ef`
   on all four serials (R-host baseline, unchanged).
6. Live androidTest APK
   `a0e6283b05cb1dac48e20ffb1b6eb3ecbf563347cbbb3d59851604b02a686fe1`
   on all four serials (R-host validation baseline; will be
   replaced).
7. Mobile pack homogeneous on all four serials at the current
   `af58bd12...` SQLite / `e5cfa29...` vectors baseline (see
   R-host validation precondition drift note). PACK READY
   badge observed.

If any precondition fails, STOP and report before touching
code.

## Outcome

- `SEARCH_WAIT_MS` in `PromptHarnessSmokeTest.java` bumped from
  `10_000L` to `15_000L` with a one-line provenance comment
  citing the diagnostic.
- Unit suite 431/431 still passes.
- androidTest compile passes.
- New androidTest APK rebuilt and pushed to all four serials,
  homogeneous sha across the matrix.
- Focused `searchQueryShowsResultsWithoutShellPolling` passes
  3/3 on `emulator-5554` AND 3/3 on `emulator-5556` at the new
  substrate. (Total 6 trials, two serials.)
- Single commit.

## Boundaries (HARD GATE)

- Touch only:
  - `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
    (single-constant edit + optional one-line comment).
- Do NOT:
  - Touch any other code, test, production file.
  - Change any other `*_WAIT_MS` constant (e.g.
    `GENERATIVE_DETAIL_WAIT_MS`). If you notice another
    constant that looks similarly tight, flag it in the report
    as out-of-scope — do not fix here.
  - Add a retry rule / `@Rule Retry(3)` / any JUnit retry
    mechanism. The diagnostic explicitly recommends the bump,
    not retry. Retry is a future option if the bump proves
    insufficient.
  - Touch `waitForResultsSettled`, `assertResultsSettled`,
    `main.search` emission, or the search pipeline
    (`SearchResultAdapter`). The diagnostic ruled these out as
    causes.
  - Edit any tracker / queue / dispatch markdown.
  - Run the state-pack four-lane sweep. Focused validation on
    5554 + 5556 is sufficient for this slice; state-pack sweep
    is a follow-up if desired.
  - Republish the gallery. R-search is a test-harness timing
    fix; the 2026-04-21 retrieval-chain gallery remains
    canonical.

## The work

### Step 1 — Apply the bump

Find the declaration of `SEARCH_WAIT_MS` in
`PromptHarnessSmokeTest.java` (near the top of the file with
the other `*_WAIT_MS` constants). Change the value from
`10_000L` to `15_000L`.

Add a one-line comment above the constant citing the
empirical timing basis. Suggested:

```
// Empirical: "fire" search path logs ~5.8-6.2s on 5554; 10s
// caused three intermittent misses across 48 hours on 5554 and
// 5556. See notes/R-SEARCH_DIAGNOSTIC_20260421.md.
private static final long SEARCH_WAIT_MS = 15_000L;
```

Comment wording is bikeshed-open. Keep it short.

### Step 2 — Compile + unit suite

```powershell
./gradlew.bat :app:compileDebugAndroidTestJavaWithJavac
./gradlew.bat :app:testDebugUnitTest
```

Both must pass. Unit suite stays 431/431.

### Step 3 — Commit

Suggested message:
`R-search: bump SEARCH_WAIT_MS from 10s to 15s for timing variance headroom`.

One commit. Stage only `PromptHarnessSmokeTest.java`.

### Step 4 — Rebuild androidTest APK

```powershell
./gradlew.bat :app:assembleDebugAndroidTest
```

Capture the new APK sha at
`android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.

### Step 5 — Push to all four serials

For each serial in `5556 / 5560 / 5554 / 5558`:

```powershell
$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe -s emulator-<SERIAL> install -r android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
```

Verify per-serial installed sha matches the built sha.
Capture as `test_apk_install_<serial>.log` files in the
artifact folder. Acceptance: homogeneous across matrix.

### Step 6 — Focused re-run on 5554 and 5556

Mirror the flake-check pattern (see prior flake-check folders
under `artifacts/external_review/rgal1_flakecheck_20260420_221049/`
and similar).

Run `com.senku.mobile.PromptHarnessSmokeTest#searchQueryShowsResultsWithoutShellPolling`
three times consecutively on `emulator-5554`, then three times
consecutively on `emulator-5556`. Capture per-trial wall-clock,
instrumentation time, status, and if available, the
`ask.search` / search-timing log lines so the post-fix
timing distribution can be compared to the diagnostic's
baseline.

Output folder:
`artifacts/cp9_stage2_r_search_validation_<ts>/` with
subfolders per serial (`focused_probe_5554/`,
`focused_probe_5556/`) each containing the three trials.

Verdict:

- **All 6 trials pass (3/3 on each serial):** fix validated. Proceed to rollup.
- **Any trial fails:** STOP. The bump was insufficient. Capture full evidence (instrumentation output + any available logcat) and report for planner decision. Options would be: larger bump (e.g., `20_000L` or `30_000L`), add retry rule, or re-examine the diagnostic for missed factors. Do NOT raise the bump again within this slice — that's a second slice's decision.

### Step 7 — Rollup

Write `artifacts/cp9_stage2_r_search_validation_<ts>/summary.md`
with:

- Commit sha.
- Old value (`10_000L`) → new value (`15_000L`).
- New androidTest APK sha.
- Per-serial install homogeneity.
- Per-trial timing table (6 rows: 5554 × 3, 5556 × 3).
- Verdict (fix validated / bump insufficient).
- Timing distribution note if `ask.search` log lines were
  captured (compare post-fix to diagnostic's 5.8-6.2s
  baseline).

## Acceptance

- Single commit touching only `PromptHarnessSmokeTest.java`.
- Unit suite 431/431.
- androidTest compile passes.
- androidTest APK homogeneous across matrix.
- Focused re-run passes 3/3 on 5554 AND 3/3 on 5556.
- Rollup written.
- No tracker / queue / dispatch edits.
- No retry rule added. No other wait constants touched.

## Delegation hints

- All steps main-inline. Slice is too small for subagent routing
  overhead.
- No MCP hint needed.

## Anti-recommendations

- Do NOT rewrite `waitForResultsSettled` or the search lane
  harness logic. R-search diagnostic explicitly ruled out
  poll-mechanism issues.
- Do NOT touch `main.search` emission in the app. Diagnostic
  confirmed creation/clear are correct.
- Do NOT bump without the comment. Future planners will need
  to know *why* `SEARCH_WAIT_MS` is 15s, not 10s or 30s. The
  one-line pointer to the diagnostic is load-bearing.
- Do NOT run the full state-pack four-lane sweep. That's
  orthogonal — if desired, it's a separate validation slice
  and should reference this one's commit + evidence. Focused
  re-run on 5554 + 5556 is sufficient acceptance for R-search.
- Do NOT change `GENERATIVE_DETAIL_WAIT_MS`,
  `FOLLOWUP_WAIT_MS`, or any other `*_WAIT_MS` even if you
  suspect they're similarly tight. File as out-of-scope
  observation in the report; a separate audit-and-bump slice
  can cover them if needed.

## Report format

Reply with:

- Commit sha + message.
- Files touched (`+X/-Y` on `PromptHarnessSmokeTest.java`).
- Unit suite result (`Tests run: 431, Failures: 0`).
- androidTest compile result.
- New androidTest APK sha.
- Per-serial installed-APK sha match.
- `test_apk_sha_homogeneous`: true/false.
- Per-trial timing table for the 6 focused probes.
- Verdict (fix validated / bump insufficient).
- Rollup summary path.
- Any out-of-scope drift noticed — flag, don't fix.
- Delegation log (lane used per step).
