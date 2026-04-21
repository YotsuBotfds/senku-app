# Slice — R-host validation (androidTest rebuild + focused probe + state-pack sweep)

- **Role:** main agent (`gpt-5.4 xhigh`). Device-state critical
  path; main-inline for sequencing, parallel `gpt-5.4 high`
  workers for the four-lane state-pack sweep (Step 5 only).
- **Paste to:** **new window** (fresh session from this file).
- **Predecessor context:** R-host code fix landed in commit
  `1edde326` ("R-host: probe handoff awareness - wait for
  DetailActivity settle, not main.ask.prepare"). Touched only
  `PromptHarnessSmokeTest.java` (+115/-21). Two probes rewired
  for handoff awareness:
  `generativeAskWithHostInferenceNavigatesToDetailScreen` and
  `autoFollowUpWithHostInferenceBuildsInlineThreadHistory`.
  New regression test
  `hostAskProbeWaitsForSettledDetailActivityAfterMainHandoff`
  locks in the settled-DetailActivity wait pattern. Unit suite
  431/431 at HEAD; no production code touched; pack + debug APK
  unchanged. This slice validates end-to-end that the fix
  resolves the `busy[1]: main.ask.prepare` stall on live
  emulators and that the state-pack sweep still reports 45/45
  at the new androidTest substrate.
- **Gallery disposition (baked into acceptance below):** the
  2026-04-21 retrieval-chain gallery at
  `artifacts/external_review/ui_review_20260421_retrieval_chain_closed/`
  remains the canonical baseline if state-pack stays 45/45 —
  R-host is a harness-observability fix, not a product change,
  so the visual claims the gallery makes are still valid. If
  state-pack drops below 45/45 on this run, the gallery
  disposition becomes a separate planner decision.

## Preconditions (HARD GATE — STOP if violated)

1. HEAD is `1edde32689c7cd1175248229ab2cc8b9603a143c`
   (R-host). Verify: `git rev-parse HEAD`. If any commit lands
   between this slice's draft and its execution (e.g., a doc
   commit), confirm `git diff 1edde326..HEAD` is empty for
   `android-app/` and proceed with a one-line provenance note
   in the rollup; otherwise STOP.
2. Unit suite 431/431 at HEAD:
   `./gradlew.bat :app:testDebugUnitTest` passes clean.
3. All four emulator serials online:
   `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe devices`
   shows 5556, 5560, 5554, 5558 all as `device`. Do NOT use the
   scrcpy-bundled adb.
4. Per-serial `sys.boot_completed == 1`.
5. Live debug APK
   `99e2bfde98acdd425c9318e0d2b7ad919b14c0043898e7fb0a394ead2ac3c6ef`
   installed on all four serials (unchanged since R-gal1).
6. Mobile pack
   `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`
   installed on all four serials (PACK READY badge).

If any precondition fails, STOP and report before rebuilding /
pushing anything.

## Outcome

- Fresh androidTest APK built at HEAD `1edde326`, pushed to all
  four serials, homogeneous sha across the matrix.
- Focused `rain_shelter` ask probe on `emulator-5556`
  confirms the R-host fix resolves the
  `busy[1]: main.ask.prepare` stall: the probe should now report
  a settled `DetailActivity` surface with the tolerant trust-spine
  assertion, instead of timing out on the stale breadcrumb.
- State-pack sweep across all four serials at the new
  androidTest substrate. Target coverage **45/45**.
- Rollup summary at
  `artifacts/cp9_stage2_post_r_host_<ts>/summary.md` citing:
  new androidTest APK sha, debug APK sha (unchanged), pack sha
  (unchanged), HEAD, state-pack coverage, focused-probe
  verdict, gallery disposition.

## Boundaries (HARD GATE)

- **No code commits.** No source, test, or production edits.
- **No gallery republish** unless state-pack drops below 45/45
  AND the planner authorizes it in a follow-up. Default: leave
  `ui_review_20260421_retrieval_chain_closed/` in place as the
  canonical retrieval-chain baseline.
- No debug APK rebuild. No pack re-export.
- No tracker / queue / dispatch markdown edits.
- No explicit final-route telemetry work (that is R-host §7's
  separate follow-up slice; do NOT conflate).
- No `main.search` investigation (different bug class, see
  `R-host_probe_handoff_awareness.md` anti-recommendations).
- If the state-pack finalization PowerShell mismatch or
  `summary.json` apk-sha fallthrough bugs reproduce, apply the
  same known workaround pattern Codex applied in prior runs
  (reconstruct top-level manifest from per-role manifests;
  overwrite empty-string sha field if needed; flag recurrence
  for post-RC tooling cleanup).

## The work

### Step 1 — Verify preconditions

Run and capture:

```powershell
git rev-parse HEAD                 # expect 1edde32689c7cd1175248229ab2cc8b9603a143c
git log --oneline 1edde326..HEAD   # expect empty, or doc-only commits with note
./gradlew.bat :app:testDebugUnitTest
$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe devices
```

Per-serial probe: `sys.boot_completed`, `pm list packages
com.senku.mobile`, installed debug APK sha, installed pack sha
(via PACK READY badge or equivalent).

### Step 2 — Rebuild androidTest APK

```powershell
./gradlew.bat :app:assembleDebugAndroidTest
```

Capture the build output APK sha:
`android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.

Expected: a new sha distinct from `ddb84d98...` (the prior
substrate). Gradle non-determinism means any exact value is
not predicted; just confirm it built and capture the sha.

### Step 3 — Push to all four serials, verify homogeneity

For each serial in `5556 / 5560 / 5554 / 5558`:

```powershell
$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe -s emulator-<SERIAL> install -r android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
```

Then verify the installed sha per serial matches the built
sha. Capture as `test_apk_install_<serial>.log` files in the
artifact folder.

Acceptance gate: installed `androidTest` APK sha is homogeneous
across all four serials (`test_apk_sha_homogeneous: true`).

If 5556 or any serial drifts, STOP and report.

### Step 4 — Focused rain_shelter probe on 5556

Run `com.senku.mobile.PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen`
(the probe R-host modified) against `emulator-5556` with the
rain_shelter-family query that previously triggered the stall.
Capture logcat and the instrumentation output.

Alternative if the probe's query is not parameterizable to
rain_shelter: run the new regression test
`hostAskProbeWaitsForSettledDetailActivityAfterMainHandoff` on
5556 — it was designed specifically to exercise the
handoff-awareness code path.

Verdict branches:

- **Probe passes, no `busy[1]: main.ask.prepare` in failure text or logs that contradict settle:** R-host fix validated on at least one serial. Proceed to Step 5.
- **Probe fails with the old stall shape (`busy[1]: main.ask.prepare`):** STOP. R-host fix did not actually resolve the issue in practice (either the wait-for-DetailActivity logic has a gap or the probe didn't reach the code paths Codex modified). Capture full logcat + dumpsys and report for planner diagnosis; do NOT proceed to state-pack sweep.
- **Probe fails with a NEW failure shape:** STOP. This is a regression R-host introduced. Capture evidence and report. Do NOT proceed.

Save evidence under
`artifacts/cp9_stage2_post_r_host_<ts>/focused_probe_5556/`.

### Step 5 — State-pack sweep across all four serials

```powershell
scripts/build_android_ui_state_pack_parallel.ps1
```

Four parallel `gpt-5.4 high` workers, one per
posture/serial lane (phone_portrait 5556, phone_landscape 5560,
tablet_portrait 5554, tablet_landscape 5558). Main reconciles
finalization.

If the finalization errors the same way as prior runs,
reconstruct the top-level manifest from per-role manifests
using the known workaround and continue.

Target coverage: **45/45** (same as
`ui_review_20260421_retrieval_chain_closed/`).

Confirm the two previously-problematic fixture classes pass
on the new substrate:

- `generativeAskWithHostInferenceNavigatesToDetailScreen` × 4 postures (was the R-gal1 concern; now uses R-host's DetailActivity-settle logic — should pass cleanly).
- `autoFollowUpWithHostInferenceBuildsInlineThreadHistory` × 4 postures (second probe R-host modified; should also pass cleanly).

Also confirm `searchQueryShowsResultsWithoutShellPolling` passes on all four postures (the prior flake-check twins; R-host did NOT touch this — it's a timing-race fixture that cleared 3/3 on re-run at both 5554 and 5556. If it flakes a third time, file a separate slice; do not dispatch a flake-check3 from this slice unless it's a single-serial, single-fixture incident that a 3× re-run would address cleanly).

### Step 6 — Coverage verification & rollup

Parse the produced `summary.json` / per-role manifests.

If coverage is `45/45`:

- Write rollup at `artifacts/cp9_stage2_post_r_host_<ts>/summary.md`.
- Include: HEAD `1edde326`, new androidTest APK sha, debug APK sha (`99e2bfde...` unchanged), pack sha (`e48d3e1a...` unchanged), `matrix_homogeneous` interpretation (accept the known missing-model-identity drift pattern as benign per prior runs), focused-probe verdict, state-pack coverage breakdown per posture, gallery disposition (canonical `ui_review_20260421_retrieval_chain_closed/` retained; no republish).
- Note the R-host code provenance chain:
  four retrieval-chain commits (`2ec77b8` → `0a8b260` →
  `971961b` → `585320c`) + D4 (`2e39021`, doc-only) + R-host
  (`1edde326`, harness-observability). No production code
  touched across R-host.

If coverage is `<45/45`:

- STOP. Do not publish a new gallery. Do not republish the
  existing one.
- Capture failing-fixture evidence (dumps, logcat, summary
  entries) in the artifact folder.
- Report per-failure root-cause sketch if obvious from the
  dump (e.g., "failure is `main.ask.prepare` stall same shape
  as pre-R-host — fix did not resolve", vs. "failure is a new
  shape — regression introduced by R-host", vs. "failure is
  unrelated `main.search` timing race on a single serial").

Do NOT attempt remediation in this slice regardless of the
failure shape.

## Acceptance

- Preconditions verified.
- androidTest APK rebuilt, pushed to all four serials, sha
  homogeneous.
- Focused probe on 5556 passes (or slice stops with captured
  evidence).
- State-pack sweep completes across all four serials.
- Rollup written.
- No code commits. No gallery republish. No tracker edits.
- If `45/45`: R-host is validated end-to-end.
- If `<45/45`: slice stops with captured evidence for planner
  decision.

## Delegation hints

- Steps 1–4 (preconditions, rebuild, push, focused probe):
  main-inline. Sequential.
- Step 5 (state-pack sweep): parallel fan-out, one
  `gpt-5.4 high` worker per serial/posture. Requires Tate's
  session-level subagent grant — confirm before firing the fan
  out. Main reconciles finalization.
- Step 6 (rollup): main-inline.
- No MCP hint needed — standard Gradle / adb workflow.

## Report format

Reply with:

- HEAD + unit suite result (confirm preconditions passed).
- New androidTest APK sha.
- Per-serial `test_apk_install_<serial>` summary
  (`installed_ok`, sha match).
- `test_apk_sha_homogeneous`: `true` / `false`.
- Focused 5556 probe verdict (pass / old-stall / new-regression)
  with evidence path.
- State-pack coverage (`X/45` with per-posture breakdown).
- Per-posture status for
  `generativeAskWithHostInferenceNavigatesToDetailScreen`,
  `autoFollowUpWithHostInferenceBuildsInlineThreadHistory`,
  `searchQueryShowsResultsWithoutShellPolling`.
- Gallery disposition (retained / republish-requested).
- Rollup summary path.
- Any tooling workaround applied.
- Any regression surfaced — flag, do NOT fix.
- Delegation log (lanes used per step).
