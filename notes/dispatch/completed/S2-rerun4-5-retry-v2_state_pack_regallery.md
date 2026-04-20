# Slice S2-rerun4.5-retry-v2 — State-pack regallery, no-restart variant

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. Device-state
  critical path; no delegation.
- **Predecessor:** S2-rerun4.5 (BLOCKED at 18/45 on a
  `tablet_landscape/deterministicAsk` ANR) and
  S2-rerun4.5-retry (BLOCKED at Step 1 because the
  single-serial restart via `start_senku_emulator_matrix.ps1 -RestartRunning`
  killed emulator-5558 without bringing it back). Tate manually
  recovered 5558 out-of-band; the matrix is back to four healthy
  serials.
- This slice skips the restart lane entirely and just runs the
  state-pack sweep. The original ANR read as host-load flake, not
  sticky device state. If it recurs, a bounded single-test re-probe
  on the affected serial is the escape hatch (same policy as
  S2-rerun4.5-retry, minus the matrix restart step that broke
  things last time).

## Preconditions (HARD GATE — STOP if violated)

1. R-val3 landed: `git merge-base --is-ancestor 607ab916 HEAD`.
2. All four emulator serials online and responsive:
   `adb devices` must show 5556, 5560, 5554, AND 5558 all as
   `device` (not `offline` or `unauthorized`). Use the SDK
   platform-tools adb (`$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`),
   not the scrcpy-bundled adb — the latter was implicated in the
   S2-rerun4.5-retry Step 1 failure and should be avoided for this
   slice.
3. Per-serial `sys.boot_completed == 1` on all four.
4. Debug APK `551385c99a2474e97d8cbd4757d6f65423ec74e9afaeb4e9e9e5d3a3f972a204`
   and androidTest APK
   `b260a219294bc20b5f76313aaa7415d895ab8899beee3372b039e251e9841b9e`
   installed on all four serials. 5558 was recently restarted by
   Tate, so confirm it still has both APKs; reinstall from the
   existing build outputs if missing (`android-app/app/build/outputs/apk/debug/app-debug.apk`
   and `android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`).
5. Mobile pack `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`
   is installed on all four serials. On 5558, confirm via a quick
   `pm list packages com.senku.mobile` + PACK READY badge check
   (or equivalent smoke). If the pack was lost during the out-of-
   band restart, re-push via `scripts/push_mobile_pack_to_android.ps1 -Serial emulator-5558`
   sourcing from `artifacts/mobile_pack/senku_20260419_213821_r-pack/`.

If any precondition fails, STOP and report.

## Outcome

Same as S2-rerun4.5 / retry:

- Fresh state-pack sweep across the fixed four-emulator matrix on
  the post-R-val3 substrate.
- Published gallery at
  `artifacts/external_review/ui_review_<YYYYMMDD>_gallery_v6/`.
- Expected coverage: 41/45 with only the four known R-gal1
  `generativeAskWithHostInferenceNavigatesToDetailScreen` failures
  (one per posture) as carry-over. No other regressions.
- Summary at
  `artifacts/cp9_stage2_rerun4_5_retry_v2_<ts>/summary.md`.

## Boundaries (HARD GATE)

- No code commits.
- No APK rebuild. The exact APKs R-val3 produced are in place.
- No mobile pack re-export.
- **No multi-serial restart.** The restart lane in
  `start_senku_emulator_matrix.ps1 -RestartRunning -Roles ...`
  broke 5558 in the prior retry. If a single-serial restart
  becomes necessary (per Step 3's ANR policy), restart that
  serial manually with `adb emu kill` + direct
  `emulator.exe -avd <avd_name>` invocation via `Start-Process`,
  not the matrix script's restart lane.
- No Wave B validation sweep — frozen at 20/20 from S2-rerun4.
- Do not edit any tracker / queue / dispatch markdown.

## The work

### Step 1 — Confirm matrix health

Run and capture:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

All four serials must appear as `device`. Write the output to
`artifacts/cp9_stage2_rerun4_5_retry_v2_<ts>/adb_devices_precheck.txt`.

For each serial, capture `sys.boot_completed`:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s emulator-<serial> shell getprop sys.boot_completed
```

Must return `1` on all four. Log to
`artifacts/cp9_stage2_rerun4_5_retry_v2_<ts>/boot_completed_<serial>.txt`.

If any serial isn't ready, STOP and report. Do NOT try to restart
via the matrix script.

### Step 2 — Confirm APKs + pack survived on 5558

5558 was out-of-band-restarted by Tate. The AVD was launched with
`-no-snapshot-load` so any previously-installed APK / pack state
may be gone. Check on 5558 specifically:

- `adb -s emulator-5558 shell pm path com.senku.mobile` → sha
  matches `551385c9…`.
- `adb -s emulator-5558 shell pm path com.senku.mobile.test` →
  sha matches `b260a219…`.
- Either (a) probe for PACK READY via an instrumented smoke
  (`scripts/run_android_instrumented_ui_smoke.ps1 -Serial emulator-5558`)
  OR (b) confirm the pack files exist in the app's private dir if
  that check is cheaper.

If any of these are missing, reinstall / re-push on 5558 only:
- APK: `adb -s emulator-5558 install -r android-app/app/build/outputs/apk/debug/app-debug.apk`
- Test APK: `adb -s emulator-5558 install -r android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
- Pack: `scripts/push_mobile_pack_to_android.ps1 -Serial emulator-5558` sourcing from `artifacts/mobile_pack/senku_20260419_213821_r-pack/`.

Other three serials are assumed to still be in the correct state
from R-val3; a quick `pm path` check on each is fine as a sanity,
but no reinstall unless it fails.

Record any reinstall / re-push action in the rollup under
`post_restart_provisioning` with a one-line per-serial note.

### Step 3 — State-pack matrix sweep with ANR-retry policy

Run `scripts/build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts/cp9_stage2_rerun4_5_retry_v2_<ts>/ui_state_pack -SkipBuild -SkipInstall`.

**Stop policy (carried over from S2-rerun4.5-retry):**

- If a state fails with an **assertion failure** outside the known
  R-gal1 cluster (the four `generativeAsk` states — one per posture):
  STOP immediately and report. This is real.
- If a state fails with a **`keyDispatchingTimedOut` or similar
  ANR** failure mode (`Instrumentation run failed` without a JUnit
  assertion trace, OR an input-dispatch / ANR trace):
  1. Capture the failing state's summary + instrumentation log.
  2. Do NOT restart the emulator via the matrix script. If a
     serial-level restart seems warranted, stop and report — we
     hit that trap last time. For this run, try just a
     **bounded single-test re-probe on the same affected serial**
     first, no restart.
  3. Re-run the SINGLE failing test on that serial only (the same
     shape as R-val3's Step 6 bounded re-probe).
  4. If the bounded re-probe passes: record as "ANR flake,
     recovered on in-place retry" and continue the main sweep.
  5. If the bounded re-probe reproduces the ANR: STOP and report.
     The ANR is real. Do NOT try a second retry.
- Hard cap: ONE ANR-retry across the whole sweep. Two ANR hits =
  STOP and report.

### Step 4 — Gallery publication

Publish to
`artifacts/external_review/ui_review_<YYYYMMDD>_gallery_v6/` with
`_v6` suffix. Only do this if Step 3 completed with coverage = 41/45
and only the known R-gal1 cluster failing.

### Step 5 — Summary rollup

Write `artifacts/cp9_stage2_rerun4_5_retry_v2_<ts>/summary.md` with
the same shape as S2-rerun4.5-retry's Step 5 spec:

- Matrix-level state-pack coverage.
- Per-posture pass/fail table.
- Per-state delta vs `_v5` gallery.
- APK + pack provenance (expected: debug `551385c9…`, androidTest
  `b260a219…`, pack `e48d3e1ab068c666…`).
- `post_restart_provisioning` section documenting whether 5558
  needed APK / pack reinstall.
- Flake-retry ledger (one entry per ANR-retry fired, same format).
- State-pack tooling status.
- Explicit "ready for S3 cut" flag or "S3 blocked — reason".

### Step 6 — Cross-check

Same as prior retries:
- `gallery_v6/index.html` loads and shows 41/45.
- Both `phone_landscape` and `tablet_landscape`
  `deterministicAskNavigatesToDetailScreen` are PASS.
- The four R-gal1 `generativeAsk` failures are visible as FAIL.

## Acceptance

- `ui_review_<YYYYMMDD>_gallery_v6/index.html` exists and shows 41/45.
- `summary.md` documents coverage 41/45, zero new regressions, and
  either an empty or all-recovered flake-retry ledger.
- Both `phone_landscape/deterministicAsk…` and
  `tablet_landscape/deterministicAsk…` are PASS.
- No new regressions beyond the known R-gal1 cluster.

## Report format

Reply with:
- Path to `summary.md` and `gallery_v6/index.html`.
- Coverage count (expected 41/45).
- `post_restart_provisioning` summary (whether 5558 needed any
  reinstall/re-push after the out-of-band restart).
- Flake-retry ledger.
- Per-state delta list vs `_v5` gallery.
- APK + pack sha confirmation.
- Delegation log (expected: "none; main inline").
- Explicit "ready for S3 cut" flag or "S3 blocked — reason".

## Anti-recommendations

- Do NOT use `start_senku_emulator_matrix.ps1 -RestartRunning` in
  this slice. That path killed 5558 last time. If restart becomes
  necessary, stop and hand back to planner — emulator restart lane
  needs investigation before being trusted again.
- Do NOT use the scrcpy-bundled adb. Use the SDK platform-tools
  adb explicitly. The scrcpy adb was observed reporting incomplete
  device lists during the prior retry.
- Do NOT treat post-restart provisioning as scope creep. 5558 was
  restarted with `-no-snapshot-load`; APK / pack state may genuinely
  be missing. Checking and reinstalling if needed is part of the
  precondition, not new scope.
- Do NOT publish `_gallery_v6` if coverage < 41/45. A partial
  gallery is worse than no gallery.
- Do NOT skip the flake-retry ledger even if it's empty. A documented
  "no ANR-retry needed" is evidence too.
