# V-ret1-probe — bounded state-pack re-probe for R-ret1 validation (post-RC)

Goal: determine whether R-ret1's marker expansion (commit `2eae0cd`) actually shifted the `rain_shelter` state-pack probe from `uncertain_fit` to `confident`. The answer decides R-gal1's disposition and whether `R-ret1b` (metadata-bonus weight tuning) is needed.

**Dispatch shape:** APK rebuild + re-provision on main lane; per-serial instrumentation runs fan out to 4 `gpt-5.4 high` workers via parallel dispatch (requires session-level subagent grant). Steps 2–4 stay main-inline.

## Context

- Active failing test: `PromptHarnessSmokeTest.java:1078` `generativeAskWithHostInferenceNavigatesToDetailScreen`, failing with assertion at `:2794` "settled status should keep final backend or completion wording when still visible" on all 4 serials. That's the four `41/45` state-pack limitations tracked as R-gal1.
- Probe query (per forward research §4/5): `"How do I build a simple rain shelter from tarp and cord?"`.
- R-ret1 added phrase-level markers (`rain shelter`, `rain fly`, `tarp shelter`, `tarp ridgeline`, `ridgeline shelter`, `tarp and cord`, `tarp and rope`) to `STRUCTURE_TYPE_EMERGENCY_SHELTER`. The probe now classifies as `emergency_shelter`, which should drive `metadataBonus` to favor shelter-family guides over GD-727 Batteries.
- Substrate unchanged: live pack `senku_mobile.sqlite3` / `senku_vectors.f16` / `senku_manifest.json` were NOT touched by R-ret1 or R-hygiene1. No pack rebuild needed.
- Prior RC v5 APK SHA: `551385c99a2474e97d8cbd4757d6f65423ec74e9afaeb4e9e9e5d3a3f972a204`. New APK must differ.

## Precondition

- `git rev-parse HEAD` returns `1e9e7e3` prefix (R-hygiene1 last commit).
- Unit suite baseline: `./gradlew.bat :app:testDebugUnitTest` passes 403/403.
- Fixed emulator matrix running on 5556 / 5560 / 5554 / 5558. If any AVD is down, use primitive `emulator.exe -avd <name> -no-snapshot-load` recovery — do NOT use `start_senku_emulator_matrix.ps1 -RestartRunning -Roles <single>` (documented trap from S2-rerun4.5-retry).
- Use SDK platform-tools adb explicitly: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`. Do not trust PATH.

## Step 0 — Verify baseline + record prior APK SHA

```
git rev-parse HEAD                                       # expect 1e9e7e3...
./gradlew.bat :app:testDebugUnitTest                     # expect 403/403
```

Record the current `app-debug.apk` SHA if one exists pre-build; compare to the post-rebuild SHA in Step 1 to confirm the new APK materially differs from RC v5's `551385c9…`.

## Step 1 — Rebuild debug APK + re-provision all 4 serials

Main-lane, serial work.

1. Clean rebuild debug APK:
   ```
   ./gradlew.bat :app:assembleDebug
   ```
2. Compute new APK SHA (sha256 of `android-app/app/build/outputs/apk/debug/app-debug.apk`). MUST differ from `551385c99a2474e97d8cbd4757d6f65423ec74e9afaeb4e9e9e5d3a3f972a204` — if identical, STOP and flag (would mean R-ret1's Java change didn't hit the build for some reason).
3. Also rebuild the androidTest APK (harness carries state-pack tests):
   ```
   ./gradlew.bat :app:assembleDebugAndroidTest
   ```
   Compute SHA. androidTest SHA may differ from RC v5's `b260a219…` due to Gradle non-determinism even without source change (R-val3 lesson). That's fine; not a hard gate.
4. Install both APKs on all 4 serials using the SDK adb explicitly:
   ```
   adb -s emulator-5556 install -r app-debug.apk
   adb -s emulator-5556 install -r app-debug-androidTest.apk
   ```
   Repeat for 5560, 5554, 5558. Verify `installed_ok` via `adb shell pm path com.senku.mobile` + `adb shell pm path com.senku.mobile.test` on each serial.
5. Pack check (sanity only — should be unchanged): `adb -s <serial> shell ls -la /data/user/0/com.senku.mobile/files/pack/` on one serial. Expected: current live pack. No re-push.
6. Record per-serial installed APK SHA in `artifacts/postrc_vret1_probe_<timestamp>/provision.json`. Planner needs `apk_sha_homogeneous: true` across all 4 serials.

STOP condition: APK SHA mismatch across serials, or any install failure.

## Step 2 — Per-serial bounded instrumentation (4-way parallel)

**Imperative parallel dispatch:** fan this step out to 4 `gpt-5.4 high` workers, one per serial. Each worker runs the single failing test method against its assigned serial, captures logcat + UI dumps, and writes a per-serial result JSON.

For each serial (5556 phone-portrait, 5560 phone-landscape, 5554 tablet-portrait, 5558 tablet-landscape):

1. Start logcat capture tagged for senku/LiteRT:
   ```
   adb -s emulator-<serial> logcat -c
   adb -s emulator-<serial> logcat -v threadtime > logcat_<serial>.txt &
   ```
2. Run ONLY the failing test method via instrumentation, with host-inference args matching the existing CP9 matrix contract (tablets on host-inference lane per `notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`, phones on-device E4B):
   ```
   adb -s emulator-<serial> shell am instrument -w \
     -e class com.senku.mobile.PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen \
     -e hostInferenceEnabled <true|false per serial> \
     -e hostInferenceUrl <if enabled> \
     -e hostInferenceModel <if enabled> \
     com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
   ```
   (Use exact host-inference args from the RC v5 S2-rerun4 matrix — check `artifacts/cp9_stage2_rerun4_20260420_143440/` for the per-serial arg set.)
3. Capture UI dump on completion: `adb -s <serial> exec-out uiautomator dump /dev/tty > dump_<serial>.xml`.
4. Stop logcat capture.
5. Write per-serial result JSON with: test pass/fail, assertion error text if any, logcat lines matching `ask\.(confident|uncertain_fit|abstain|prompt)` + `metadataProfile` + `preferredStructureType`, body content snippet from UI dump.

Artifact structure:
```
artifacts/postrc_vret1_probe_<timestamp>/
├── provision.json
├── per_serial/
│   ├── 5556/
│   │   ├── result.json
│   │   ├── logcat.txt
│   │   └── dump.xml
│   ├── 5560/…
│   ├── 5554/…
│   └── 5558/…
└── summary.md
```

STOP condition: instrumentation fails to start on any serial (ANR, crash on launch, adb error that isn't benign).

## Step 3 — Main-lane rollup + verdict

Read the 4 per-serial result JSONs. Classify each serial into one of:

| Signal pattern | Classification |
| --- | --- |
| Test PASSES (no assertion) + logcat shows `ask.confident` for probe | **A — fully resolved** |
| Test FAILS with assertion at :2794 + logcat shows `ask.confident` for probe | **B — R-gal1 Option A needed (assertion still asserts against confident wording wrongly)** |
| Test FAILS + logcat shows `ask.uncertain_fit` for probe | **C — R-ret1 insufficient, file R-ret1b (weight tuning)** |
| Test FAILS + logcat shows `ask.abstain` or no `ask.*` completion | **D — regression, diagnose** |

Per-serial ledger: phone-portrait (5556), phone-landscape (5560), tablet-portrait (5554), tablet-landscape (5558).

### Decision matrix for planner

| All 4 serials | Recommended planner move |
| --- | --- |
| All A | R-gal1 closes without code; file closure note; state-pack gallery refreshes to 45/45 on next regen |
| All B | Draft R-gal1 Option A (relax assertion at `PromptHarnessSmokeTest.java:2794` to accept confident terminal wording) |
| All C | Draft R-ret1b (metadata-bonus weight tuning for `emergency_shelter` bucket) with concrete top-row ranking evidence from logcat |
| Mixed A/B/C | Serial-specific diagnosis; likely host-inference lane differs from on-device routing — flag for T5-shape root-cause slice |
| Any D | STOP, file T-shape diagnostic before remediation |

## Step 4 — Summary artifact + planner handoff

Write `summary.md` in the artifact dir containing:

1. APK SHAs (old RC v5 / new) and androidTest APK SHAs.
2. Per-serial classification (A / B / C / D).
3. For each serial, cite: (a) test pass/fail, (b) key logcat line showing `ask.<mode>` for the probe, (c) `preferredStructureType` observed for the query.
4. Planner recommendation following the decision matrix above.
5. Delta vs prior state-pack run at `artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/` — per-serial assertion status change.

## Acceptance

- All 4 serials produced per-serial result JSONs.
- Classification table filled for all 4 serials.
- `summary.md` written with the planner decision-matrix outcome.
- No changes to production code, tests, or pack. Pure validation lane.

## Out of scope

- Writing R-gal1 Option A or R-ret1b code. This slice only produces evidence for the decision.
- Full state-pack gallery regen. That lives in a follow-on slice if planner decides R-gal1 closes (outcome A) or if remediation lands (outcome B/C).
- Any production-code change.
- Any pack rebuild (unchanged since RC v5).
- Unit-test changes.
- Touching top-level untracked state (still not in scope).

## STOP conditions (explicit)

- Step 0: unit suite baseline isn't 403/403.
- Step 1: new APK SHA equals `551385c9…` (build didn't pick up R-ret1 change).
- Step 1: APK install fails on any serial, or `apk_sha_homogeneous` false.
- Step 2: instrumentation fails to START on any serial (crash, ANR on launch, adb error). For bounded flake retry: if one serial's single instrumentation run hits a `keyDispatchingTimedOut` ANR, ONE re-probe is allowed after confirming the other 3 completed cleanly. Two ANRs or a fresh crash → STOP.
- Step 3: any serial classified D (regression).

In any STOP case: report state, do NOT attempt recovery, wait for planner guidance. Do not retune, rebuild, or re-push on your own.

## Dispatch notes

- Naming convention: `V-` prefix (validation slice, not remediation or engineering), to distinguish from `R-*` remediation/engineering slices. `V-ret1-probe` reads as "validation probe for R-ret1."
- Session-level subagent grant required for Step 2's parallel fan-out (same pattern as S2-rerun2/3/4 Step 1).
- Estimated runtime: Step 1 ~8 min (APK build + 4 installs), Step 2 ~3–5 min per serial in parallel ≈ 5 min total, Step 3 ~2 min. Total ~15–20 min wall clock.
