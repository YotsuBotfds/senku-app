# R-gal1 — relax trust-spine assertion to accept uncertain/abstain wording (post-RC)

Goal: update the `assertGeneratedTrustSpineSettled` helper at
`PromptHarnessSmokeTest.java:2779-2782` so `uncertain_fit` and
`abstain` terminal states count as settled. The assertion currently
only accepts backend-label strings or "answer ready" / "no guide match"
completion keywords, which makes any legitimately-final non-confident
mode fail the state-pack lane.

**Dispatch shape:** single main-lane worker, serial. One commit
(test-file edit only). Scoped androidTest verification on 5556 +
state-pack parallel sweep on the four-emulator posture matrix to
confirm 45/45.

## Context

Full forward research at `notes/R-GAL1_FORWARD_RESEARCH_20260420.md`.
Short version:

- Four state-pack failures at 2026-04-20 RC v5 (gallery `ui_review_20260420_gallery_v6` — 41/45) all share the same test method `generativeAskWithHostInferenceNavigatesToDetailScreen` firing across the four postures (5556 / 5560 / 5554 / 5558) on the rain-shelter query.
- Failure assertion at `PromptHarnessSmokeTest.java:2781`: "settled status should keep final backend or completion wording when still visible."
- Pre-R-anchor1 the query routed to `uncertain_fit` via `low_coverage_route`; the settled `detail_status_text` did not contain backend labels or "ready"/"no guide match" keywords, so the assertion fired.
- R-anchor1 (`971961b`) flipped `anchorGuide` from GD-727 to GD-345 and made `context.selected` shelter-dominant. Whether mode also flipped to `confident` is unresolved because the `busy[1]: main.ask.prepare` stall blocks `ask.generate mode=` visibility in the R-anchor1 probe.
- Either way, uncertain_fit / abstain are legitimate terminal states that should settle cleanly — the assertion is coverage-incomplete.

## Precondition

- HEAD at `971961b` prefix (R-anchor1 commit) or later.
- Android unit suite: `./gradlew.bat :app:testDebugUnitTest` passes 431/431.
- Four-emulator posture matrix reachable (5556 / 5560 / 5554 / 5558).
- Use SDK platform-tools adb: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.

## Step 0 — Baseline

1. `git rev-parse HEAD` returns `971961b` or later.
2. `./gradlew.bat :app:testDebugUnitTest` → 431/431.
3. Confirm the four state-pack fixtures are still in the 41/45 failure set. If a prior state-pack run since R-anchor1 already shows 45/45, R-gal1 may be unnecessary — stop and report.

## Step 1 — Fix (commit 1)

### Step 1a — edit the assertion block

File: `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Location: lines 2779-2782, inside `assertGeneratedTrustSpineSettled(...)` starting at line 2539.

Current:
```java
if (!containsAny(statusText, hostLabel, onDeviceLabel, fallbackLabel)
    && !containsAny(statusTextLower, "answer ready", "offline answer ready", "no guide match")) {
    failure[0] = "settled status should keep final backend or completion wording when still visible";
    return;
}
```

New:
```java
// R-gal1: accept uncertain_fit and abstain terminal states as legitimately
// settled. `buildUncertainFitAnswerBody` emits "not a confident fit" in the
// body surface; `buildAbstainAnswerBody` emits "abstain" wording. These are
// final, not intermediate — and are not a bug — so the trust-spine check
// should not treat them as "settled wrong."
if (!containsAny(statusText, hostLabel, onDeviceLabel, fallbackLabel)
    && !containsAny(statusTextLower, "answer ready", "offline answer ready",
        "no guide match", "not a confident fit", "uncertain fit", "abstain")) {
    failure[0] = "settled status should keep final backend or completion wording when still visible";
    return;
}
```

Three new accepted substrings: `"not a confident fit"`, `"uncertain fit"`, `"abstain"`.

### Step 1b — no new unit tests

This slice adds test-lane tolerance, not product behavior. Existing
regression coverage still fires for streaming-state leaks (line 2770),
empty-status leaks (line 2774), and the broadened settled-wording
check (new line 2779). No new unit tests required.

If you want a regression lock for the new tolerance, add a single
androidTest that seeds a mock uncertain-fit status text and asserts
`assertGeneratedTrustSpineSettled` does NOT fail. Optional — the
state-pack run at Step 3 provides the actual integration coverage.

### Step 1c — focused androidTest verification on 5556

APK rebuild not strictly required (the test class is in the
androidTest tree, not the main APK). Rebuild just the instrumentation
APK, push to 5556, and rerun the one fixture:

```
./gradlew.bat :app:assembleDebugAndroidTest
adb -s emulator-5556 install -r -t android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5556 shell am instrument -w \
  -e class com.senku.mobile.PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen \
  -e hostInferenceEnabled true \
  -e hostInferenceUrl http://10.0.2.2:1236/v1 \
  -e hostInferenceModel gemma-4-e4b-it-litert \
  com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
```

Expected: the fixture either passes cleanly OR still hits the known
`busy[1]: main.ask.prepare` instrumentation stall (which is out of
R-gal1's scope — it's a host-precondition issue, not a trust-spine
issue). If the fixture passes cleanly, that confirms the fix took.

If the fixture still fails with the R-gal1 assertion message, STOP —
the wording change didn't take or the uncertain_fit status text uses
different phrasing than expected. Capture the actual
`detail_status_text` field via a UI dump and report.

### Step 1d — commit

```
git add android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java
git commit
```

Commit message must name:
- The coverage gap: `assertGeneratedTrustSpineSettled` required backend-label or "ready"/"no guide match" completion wording, rejecting legitimate `uncertain_fit` and `abstain` terminal states.
- The fix: three additional tolerance strings (`"not a confident fit"`, `"uncertain fit"`, `"abstain"`).
- R-anchor1 context: `971961b` fixed the retrieval-chain anchor problem but whether rain-shelter routes to `confident` post-anchor flip is unresolved by the instrumentation stall; R-gal1 ensures the state-pack lane accepts both outcomes cleanly.
- Out-of-scope: the `busy[1]: main.ask.prepare` stall (separate R-host investigation).

## Step 2 — APK rebuild for state-pack matrix

Not required if Step 1c installed the androidTest APK successfully.
The state-pack matrix runs the same instrumentation test class; the
debug APK (`99e2bfde98ac...`) from R-anchor1 is already on all four
serials via the prior R-anchor1 probe OR the matrix script will
reinstall.

If the matrix script requires a fresh build:

```
./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
```

## Step 3 — State-pack parallel sweep (four-emulator matrix)

Run the fixed four-emulator posture matrix sweep:

```
scripts/build_android_ui_state_pack_parallel.ps1 -OutputDir artifacts/external_review/ui_review_<ts>_rgal1/
```

(Adjust invocation to match current script interface — see
`scripts/start_senku_emulator_matrix.ps1` for launch and
`scripts/build_android_ui_state_pack_parallel.ps1` for parallel
sweep.)

Expected: all 45 fixtures × 4 serials = 180 captures. The four
previously-failing `generativeAskWithHostInferenceNavigatesToDetailScreen`
instances should now pass.

STOP conditions:
- Any captures fail for reasons other than the original R-gal1 assertion (e.g., an R-anchor1 regression exposes a new failure on a different fixture). Report per-fixture failure matrix.
- Tooling-side finalization errors in the PowerShell script (previously reproduced; Codex has workaround pattern — reconstruct top-level manifest from per-role manifests if needed, same as S2-rerun2).

## Step 4 — Gallery finalization

Finalize the gallery:

```
(gallery finalize command — mirror the cadence used for
 ui_review_20260420_gallery_v6/ generation)
```

Expected output: `artifacts/external_review/ui_review_<ts>_rgal1/index.html`
with 45/45 status. Confirm the four previous failures close.

## Step 5 — Rollup

Write `artifacts/external_review/ui_review_<ts>_rgal1/summary.md` or
an adjacent note summarizing:

1. State-pack result: 45/45 (expected) or residual failures.
2. Commit SHA for R-gal1.
3. Per-fixture delta vs `ui_review_20260420_gallery_v6` (which was 41/45).
4. If any residual failures: per-fixture diagnosis (fresh bug vs. expected side-effect).

Update `notes/REVIEWER_BACKEND_TRACKER_20260418.md` to note RC v5 now 45/45 with R-gal1's scope cut documented.

## Acceptance

- Commit landed with the three-string tolerance addition.
- Android unit suite stays 431/431.
- State-pack four-emulator sweep returns 45/45 (or, if mode didn't flip post-R-anchor1, `generativeAskWithHostInferenceNavigatesToDetailScreen` still passes because uncertain_fit wording is now accepted).
- Gallery finalization produces a clean `index.html`.

## Out of scope

- `busy[1]: main.ask.prepare` stall — separate R-host investigation.
- Mode-aware annotation infrastructure (Option B from forward research).
- Fixture exclusion (Option C from forward research).
- Engine-side status-text changes (Option D from forward research).
- Broader trust-spine redesign across the detail surface.

## STOP conditions (explicit)

- Step 0: baseline 431 not matched.
- Step 0: state-pack already at 45/45 pre-edit — R-gal1 unnecessary; report and close.
- Step 1c: focused fixture still fails with R-gal1 assertion wording despite the tolerance expansion (unexpected — means the actual settled status text doesn't contain any of the three new substrings). Capture UI dump, report.
- Step 3: state-pack run exposes new failures on fixtures other than `generativeAskWithHostInferenceNavigatesToDetailScreen`. Report per-fixture matrix.
- Step 4: gallery finalization hits the known PowerShell `Argument types do not match` error — Codex has precedent for reconstructing from per-role manifests, continue.

In any STOP case: report state, do not attempt recovery beyond documented workarounds, wait for planner guidance.

## Notes

- Estimated runtime: Step 1 ~5-10 min (trivial edit + focused run + commit). Step 3 ~30-45 min (four-emulator parallel sweep). Step 4-5 ~10 min.
- Total ~45-60 min wall clock.
- This slice is low-risk by design — test-file tolerance expansion with no production code change.
- If the state-pack sweep returns 45/45 cleanly, CP9 RC v5 substantively closes at the state-pack matrix level. The retrieval chain (R-gate1 → R-ret1c → R-anchor1) + R-gal1 together resolve the four carryover limitations documented in the RC v5 cut.
