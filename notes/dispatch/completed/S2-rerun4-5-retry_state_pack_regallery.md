# Slice S2-rerun4.5-retry — State-pack regallery retry with 5558 restart

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. Device-state
  critical path; no delegation.
- **Predecessor:** Previous S2-rerun4.5 at
  `artifacts/cp9_stage2_rerun4_5_20260420_151555/` stopped BLOCKED at
  18/45 completed on a **non-assertion ANR** —
  `tablet_landscape/deterministicAskNavigatesToDetailScreen` hit
  `keyDispatchingTimedOut` with MainActivity waiting 5001ms for a
  `FocusEvent(hasFocus=false)`. Planner read this as a flaky
  emulator ANR, not a real regression, based on:
  - R-val3's bounded regression check on 5558 passed 45 minutes
    earlier against identical APK + pack + model + test.
  - Failure mode is a system-level input-dispatch timeout, not an
    assertion failure matching the R-ui2 v3 / R-val3 surface.
  - R-ui2 v3 does not touch MainActivity. MainActivity's
    `stateVisible|adjustResize` + `searchInput.requestFocus()` /
    `imm.showSoftInput()` at AndroidManifest.xml:28 / MainActivity.java:932-949
    is unchanged from before the R-ui2 v3 chain started.
- This slice retries the state-pack regallery with a light
  emulator restart and an ANR-retry policy. If the ANR reproduces
  on any serial, it becomes real and we escalate.

## Preconditions (HARD GATE — STOP if violated)

Identical to S2-rerun4.5 (see
`notes/dispatch/S2-rerun4-5_state_pack_regallery.md`) PLUS:

1. The prior S2-rerun4.5 run at
   `artifacts/cp9_stage2_rerun4_5_20260420_151555/summary.md` reports
   verdict BLOCKED with the `tablet_landscape` ANR at the documented
   location. (This confirms we are retrying the correct slice.)
2. Host system is not under obvious sustained load (e.g., no other
   heavy Gradle or emulator-matrix process running). If it is, wait
   for idle before retrying — the whole point of this slice is to
   rule out transient environmental flake.

If either additional gate fails, STOP and report.

## Outcome

Same as S2-rerun4.5:
- Fresh state-pack sweep across the fixed four-emulator matrix on
  the post-R-val3 substrate.
- New published gallery at
  `artifacts/external_review/ui_review_<YYYYMMDD>_gallery_v6/`.
- Expected coverage: 41/45. Four known R-gal1 `generativeAsk`
  failures (one per posture) remain; all other states pass. No new
  regressions.
- Summary at
  `artifacts/cp9_stage2_rerun4_5_retry_<ts>/summary.md`.

Reuse the `_v6` suffix — the previous S2-rerun4.5 intentionally did
NOT publish anything, so the name is free.

## Boundaries (HARD GATE)

- No code commits.
- No APK rebuild or install — R-val3 left the matrix in the required
  state. Verify via precondition check, do not rebuild.
- No Wave B validation sweep.
- No mobile pack re-export or re-push.
- Do not edit any tracker / queue / dispatch markdown.
- Test-assertion fixes beyond R-val3 are out of scope. If the ANR
  reproduces, STOP and hand back to planner.

## The work

### Step 1 — Light emulator restart for 5558 only

The previous run's ANR was on `emulator-5558`. Restart just that
serial to clear any Zygote / input-dispatch hangover. Keep 5556,
5560, 5554 warm to avoid an unnecessary 10-minute boot cycle on
three otherwise-healthy devices.

Approach (pick whichever `scripts/` helper is already parameterized
for single-serial restart, or do it directly):

1. `adb -s emulator-5558 emu kill` (graceful shutdown).
2. Wait for the serial to disappear from `adb devices`.
3. Relaunch 5558 per the matrix config — its AVD name and args
   should match the live config used by
   `scripts/start_senku_emulator_matrix.ps1`.
4. Wait for `adb -s emulator-5558 shell getprop sys.boot_completed`
   to return `1`.
5. Reinstall the APKs on 5558 if the AVD cold-start wiped them.
   Expected NOT to wipe data (snapshots usually preserve), but
   confirm:
   - `adb -s emulator-5558 shell pm path com.senku.mobile` must
     return a path; sha must match `551385c9…`.
   - `adb -s emulator-5558 shell pm path com.senku.mobile.test`
     must return a path; sha must match `b260a219…`.
   - If either is missing, reinstall from
     `android-app/app/build/outputs/apk/debug/app-debug.apk` and
     `android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.
   - If the mobile pack badge is missing after restart, re-push from
     `artifacts/mobile_pack/senku_20260419_213821_r-pack/` via
     `scripts/push_mobile_pack_to_android.ps1 -Serial emulator-5558`.

If the restart itself fails (e.g., AVD won't boot), STOP and report
— that's infrastructure territory, not a slice retry problem.

### Step 2 — Verify the other three serials are still in the right state

Quick provenance sanity check on 5556, 5560, 5554:
- `adb -s emulator-<serial> shell pm path com.senku.mobile` → sha
  matches `551385c9…`.
- `adb -s emulator-<serial> shell pm path com.senku.mobile.test` →
  sha matches `b260a219…`.
- No reinstall needed unless one is unexpectedly drifted.

### Step 3 — State-pack matrix sweep

Run `scripts/build_android_ui_state_pack_parallel.ps1` against the
fixed four-emulator matrix exactly as S2-rerun4.5 did, with output
to `artifacts/cp9_stage2_rerun4_5_retry_<ts>/ui_state_pack/<sweep_ts>/`.

**Stop policy (differs from S2-rerun4.5):**

- If a state fails with an **assertion failure** outside the known
  R-gal1 cluster (the four `generativeAsk` states — one per posture
  — that have been carry-over since S2-rerun3): STOP immediately
  and report. This is real.
- If a state fails with a **`keyDispatchingTimedOut` or similar ANR**
  failure mode (`Instrumentation run failed` without a JUnit
  assertion trace, OR an input-dispatch / ANR trace):
  1. Capture the failing state's summary + instrumentation log.
  2. Restart the affected emulator serial only (same procedure as
     Step 1 but for whichever serial ANRd).
  3. Re-run the SINGLE failing test on that serial only (bounded,
     same shape as R-val3's Step 6 re-probe).
  4. If the bounded re-probe passes: record in the rollup as an
     "ANR flake, recovered on bounded retry" note and continue the
     main sweep from where it stopped.
  5. If the bounded re-probe reproduces the ANR: STOP and report.
     The ANR is real. Do NOT try a second retry.
- Hard cap: at most ONE ANR-retry across the whole sweep. If two
  different serials ANR on different states, STOP and report —
  that's a host-system-wide problem beyond this slice's scope.

### Step 4 — Gallery publication

Publish to
`artifacts/external_review/ui_review_<YYYYMMDD>_gallery_v6/` with the
`_v6` suffix (unchanged from S2-rerun4.5). Only do this if Step 3
completed with coverage = 41/45 and only the known R-gal1 cluster
failing.

### Step 5 — Summary rollup

Write `artifacts/cp9_stage2_rerun4_5_retry_<ts>/summary.md` with the
same shape as the S2-rerun4.5 slice's Step 3 spec, plus a new
section:

**Flake-retry ledger:** one table entry per ANR-retry that fired,
with columns `serial`, `state_id`, `initial_failure_mode` (e.g.
`keyDispatchingTimedOut`), `bounded_retry_result` (pass / reproduced),
`artifact_dir`. If no ANR-retry fired, write "No ANR-retry needed;
main sweep completed on first pass." and move on.

If the flake-retry ledger has any "reproduced" entry, the slice
ends with a RED verdict regardless of other passes — the ANR is
real and we need to diagnose.

If the flake-retry ledger is empty or has only "pass" entries, the
slice ends with GREEN and explicit "ready for S3 cut" flag.

### Step 6 — Cross-check gallery against expected

Same as S2-rerun4.5 Step 4:
- `gallery_v6/index.html` loads and shows 41/45.
- `phone_landscape/deterministicAskNavigatesToDetailScreen` is PASS.
- `tablet_landscape/deterministicAskNavigatesToDetailScreen` is PASS
  (this was the blocking flake).
- The four R-gal1 `generativeAsk` failures are visible as FAIL.

## Acceptance

- `ui_review_<YYYYMMDD>_gallery_v6/index.html` exists and shows 41/45.
- `artifacts/cp9_stage2_rerun4_5_retry_<ts>/summary.md` documents
  coverage 41/45 and either an empty flake-retry ledger or retries
  that all passed on bounded re-probe.
- Both phone_landscape and tablet_landscape `deterministicAsk…`
  states are PASS in the fresh gallery.
- No new regressions beyond the known R-gal1 cluster.

## Report format

Reply with:
- Path to `summary.md` and `gallery_v6/index.html`.
- Coverage count (expected 41/45).
- Flake-retry ledger contents (empty / with entries per the spec).
- Per-state delta list vs S2-rerun4's `_v5` gallery.
- APK + pack sha confirmation.
- Any anomaly during the 5558 restart or APK/pack reinstall.
- Delegation log (expected: "none; main inline").
- Explicit "ready for S3 cut" flag or "S3 blocked — reason".

## Anti-recommendations

- Do NOT retry more than ONCE per ANR. A single flake is plausible;
  two ANRs on the same serial is a pattern.
- Do NOT retry across serials (e.g. ANR on 5558 → restart all four).
  That masks signal.
- Do NOT treat a genuine assertion failure as flake. ANR flakes
  have a specific signature (`Instrumentation run failed` +
  `keyDispatchingTimedOut` / `Input dispatching timed out`). A
  JUnit assertion trace is NOT flake, even if intermittent.
- Do NOT adjust the state-pack script's timeouts to hide the ANR.
  If the ANR is real at 5s dispatch timeout, shifting the bar
  doesn't fix anything.
- Do NOT publish `_gallery_v6` if Step 3 did not reach 41/45. A
  partial gallery is worse than no gallery.
