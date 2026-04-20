# Slice R-val3 — Align `waitForLandscapeDockedComposerReady` with post-R-ui2 v3 product behavior

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. Single-file
  androidTest-only code change plus a narrow phone-landscape state-pack
  re-probe.
- **Parallel with:** nothing. Low-risk but single-threaded through
  device state for the re-probe.
- **Predecessor:** S2-rerun4 landed Wave B at 20/20 but state-pack at
  40/45, one new phone-landscape regression in
  `deterministicAskNavigatesToDetailScreen` at line 581 asserting
  `"phone-landscape detail should settle into a docked composer-ready state"`.
  Evidence at
  `artifacts/cp9_stage2_rerun4_20260420_143440/ui_state_pack/20260420_143855/summaries/phone_landscape/deterministicAskNavigatesToDetailScreen/summary.json`
  (and the associated `instrumentation.txt`). This slice is a
  test-assertion fix, NOT a product fix. R-ui2 v3 intentionally
  removed the programmatic landscape composer auto-focus; the test
  helper was written against the old auto-focus behavior and must be
  aligned with the new one.

## Context (no code change required to understand the problem)

The test at `PromptHarnessSmokeTest.java:581` calls
`waitForLandscapeDockedComposerReady(DETAIL_WAIT_MS)` whose body at
line 4105-4120 requires:

```java
requested[0] = composer != null
    && composer.getFocusRequestCount() > 0
    && composer.isComposerFocused()
    && isEffectivelyVisible(composer)
    && isEffectivelyVisible(panel);
```

- `composer.getFocusRequestCount() > 0` — was bumped by the now-removed
  `requestLandscapeDockedComposerFocus` auto-call path in R-ui2 v3.
  Without user interaction, stays `0`. Truthiness: **false post-v3**.
- `composer.isComposerFocused()` — reflects the Compose-side focus
  state. Without auto-focus, stays `false` until user tap. Truthiness:
  **false post-v3**.

The separate rail-hidden assertion at lines 586-596 is STILL valid
post-v3 — R-ui2 v3's scope expansion made the rail always-hidden on
landscape phone, which subsumes the "rail steps aside when composer
focuses" precondition. That assertion continues to pass naturally.

Product behavior the test should now assert: the docked composer HOST
is visible, visible via its container panel, and interactive-ready
(enabled) — but NOT pre-focused.

## Scope

Single file, androidTest only:

`android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Three tightly-scoped edits:

1. **Redefine the helper body at lines 4105-ish** (`waitForLandscapeDockedComposerReady(long timeoutMs)`):
   - Remove `composer.getFocusRequestCount() > 0` from the truth condition.
   - Remove `composer.isComposerFocused()` from the truth condition.
   - Add `composer.isEnabled()` to verify the composer is interactive.
   - Keep `isEffectivelyVisible(composer)` and `isEffectivelyVisible(panel)`.
   - Add a one-line comment above the helper explaining the
     post-R-ui2 v3 product decision: "Landscape composer is
     interactive-ready (visible + enabled) by default; user interaction
     is required for focus. See R-ui2 v3 (commit f095194)."

2. **Leave the state-id capture label `"landscape_focus_ready"` at line 585 unchanged** for gallery stability. The state-id is a historical label; renaming it would churn per-state-id delta tracking across the gallery history for no functional benefit. Do NOT rename.

3. **Leave the rail-hidden assertion at lines 586-596 unchanged.** Its precondition (composer focused) has been replaced by a broader precondition (landscape phone), but the assertion's truth (rail hidden) still holds. No action required.

Do NOT touch:
- The test method itself (except via the helper).
- Any other test's focus-related helpers (none identified that match
  this pattern).
- `DockedComposer.kt` or `DockedComposerHostView` (these are product
  code; R-ui2 v3 already decided on the new semantics).
- The main debug APK.
- Any resource, manifest, layout, or production Java source.

## Boundaries (HARD GATE — STOP if violated)

- Touch only `PromptHarnessSmokeTest.java` and no other file.
- Do NOT modify product behavior — this is a test-alignment slice, not
  a product fix. If you find yourself editing DetailActivity.java,
  DockedComposer.kt, layouts, or manifest, stop and report.
- Do NOT run a full Wave B validation sweep — S2-rerun4 already
  covered that.
- Do NOT run a full matrix state-pack sweep — the regression was
  5560-only; a phone-landscape-only re-probe is sufficient.
- Do NOT rebuild the main debug APK.

## Outcome

- `waitForLandscapeDockedComposerReady` redefined as interactive-
  readiness (visible + enabled), not focus-readiness.
- `deterministicAskNavigatesToDetailScreen` state-pack test passes on
  emulator-5560 phone-landscape.
- State-pack matrix coverage returns to 41/45 (the S2-rerun3 baseline
  — R-gal1's 4 carryover `generativeAsk` failures remain, same as
  before).
- Single commit; single file modified.

## The work

### Step 1 — Verify prerequisites

From the Senku repo root:
- `git merge-base --is-ancestor f095194 HEAD` must succeed (R-ui2 v3
  is in history).
- `artifacts/cp9_stage2_rerun4_20260420_143440/summary.md` exists and
  reports the `deterministicAskNavigatesToDetailScreen` regression
  (sanity check that we're remediating the right thing).
- All four emulator serials (5556, 5560, 5554, 5558) are up per the
  fixed matrix. If not, restart via
  `scripts/start_senku_emulator_matrix.ps1`.
- Debug APK `551385c9…` is installed on all four serials (no action
  needed — RP4 did this; this slice does not rebuild the main APK).

### Step 2 — Apply the helper fix

Open
`android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`.

Locate `waitForLandscapeDockedComposerReady(long timeoutMs)` at
around line 4105. Inside the `runOnMainSync` lambda, replace the
truth condition assignment:

**Before:**
```java
requested[0] = composer != null
    && composer.getFocusRequestCount() > 0
    && composer.isComposerFocused()
    && isEffectivelyVisible(composer)
    && isEffectivelyVisible(panel);
```

**After:**
```java
// R-ui2 v3 (commit f095194) removed programmatic landscape composer
// auto-focus. Composer is interactive-ready (visible + enabled) by
// default; user interaction is required for focus.
requested[0] = composer != null
    && composer.isEnabled()
    && isEffectivelyVisible(composer)
    && isEffectivelyVisible(panel);
```

Verify via local compile that `DockedComposerHostView.isEnabled()`
resolves. Kotlin views inherit `View.isEnabled()`, which returns the
enabled state set by the host. `DockedComposerHostView` at
`android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
extends an abstract compose container; its `isEnabled()` comes from
the View base class and reflects the bound model's `enabled` state
via `DockedComposerModel.enabled` (see `DockedComposer.kt:64-73`).

### Step 3 — Local unit / compile check

From the `android-app/` directory:

- `./gradlew.bat :app:compileDebugAndroidTestJavaWithJavac`
  (ensures the helper change compiles; this does not run
  instrumentation).

If it fails to compile, fix syntax / type errors and retry.

### Step 4 — Rebuild androidTest APK

- `./gradlew.bat :app:assembleDebugAndroidTest`

The resulting APK sha will differ from RP4's `1ab24200…` because
androidTest source changed. Record the new sha.

Do NOT rebuild `:app:assembleDebug`. The main debug APK stays at
RP4's `551385c9…`. This slice does not change the product.

### Step 5 — Install new androidTest APK on all four serials

For each of `5556`, `5560`, `5554`, `5558`:

- `adb -s emulator-<serial> install -r android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`

Capture install output to
`artifacts/r_val3_<ts>/apk_install_androidtest_<serial>.log`.

Verify installed sha via
`adb -s emulator-<serial> shell pm path com.senku.mobile.test` →
sha256 on pulled APK → matches built sha from Step 4. Write
`artifacts/r_val3_<ts>/apk_sha_androidtest_<serial>.json` with the
same shape as RP4's entries.

### Step 6 — Bounded phone-landscape state-pack re-probe on 5560

Run a single-serial, single-test state-pack probe against
emulator-5560 for `deterministicAskNavigatesToDetailScreen`. Do
NOT run the full matrix state-pack sweep.

You can use `scripts/run_android_instrumented_ui_smoke.ps1` with
`-Test com.senku.mobile.PromptHarnessSmokeTest#deterministicAskNavigatesToDetailScreen`
targeted at `emulator-5560`, OR adapt the state-pack script to a
single-test, single-serial invocation. Pick whichever tool is
already parameterized in that direction.

Expected outcome: `deterministicAskNavigatesToDetailScreen` passes on
emulator-5560; the `landscape_focus_ready` capture is produced
(screenshot + dump).

If it fails, STOP and report. Read the instrumentation log and
identify whether:
- `composer.isEnabled()` is `false` in the new assertion (composer
  model not enabled yet → timing / prepare-settle issue).
- `isEffectivelyVisible(composer)` or `isEffectivelyVisible(panel)`
  is `false` (layout / rail-suppression side effect we missed).
- Some OTHER assertion in the test body (lines 522-572 or the
  deterministic-route chip check) is what's actually failing.

Report the specific cause. Do NOT try a v2 / v3 remediation without
checking back with planner.

### Step 7 — Regression check on other three serials (optional but recommended)

Run the same single-test probe on `5556`, `5554`, `5558`. These serials
were passing the test before R-val3; they should still pass. A quick
verification closes the "did R-val3 break something else" concern.

Expected: all three continue to pass (their paths don't hit the
landscape-phone helper at all; `landscapePhone[0]` stays `false`).

### Step 8 — Commit

Single commit. Suggested title:
`R-val3: align waitForLandscapeDockedComposerReady with post-R-ui2 v3 composer behavior`

Body should cite:
- The failing assertion path (PromptHarnessSmokeTest.java:581 →
  line 4105 helper).
- The product decision (R-ui2 v3, commit f095194, intentionally
  removed programmatic landscape composer auto-focus).
- The new readiness definition (visible + enabled, not focused).
- The state-pack re-probe result on 5560 (and optionally on the other
  three serials).

## Acceptance

- `PromptHarnessSmokeTest.java` helper redefined per Step 2; no other
  file modified.
- `./gradlew.bat :app:compileDebugAndroidTestJavaWithJavac` succeeds.
- `./gradlew.bat :app:assembleDebugAndroidTest` succeeds.
- New androidTest APK installed on all four serials; per-serial sha
  matches built sha.
- `deterministicAskNavigatesToDetailScreen` passes on 5560 via bounded
  re-probe.
- Optional sanity check on 5556 / 5554 / 5558 passes.
- Single commit with the four-file-or-fewer diff summary naming only
  `PromptHarnessSmokeTest.java`.

## Report format

Reply with:
- Commit sha.
- Single-file diff summary (what changed in `PromptHarnessSmokeTest.java`).
- Gradle assemble success lines (compile and assembleDebugAndroidTest).
- New androidTest APK sha.
- Per-serial installed androidTest APK sha (four lines).
- 5560 bounded state-pack re-probe result (pass/fail + artifact paths
  for the screenshot + dump).
- Optional: 5556 / 5554 / 5558 regression check result.
- Delegation log (expected: "none; main inline").
- Explicit "ready for S2-rerun4.5 confirmation or direct to S3" flag.

## Anti-recommendations

- Do NOT rename the `"landscape_focus_ready"` state-id. It's a
  historical label; renaming churns the gallery's per-state-id delta
  history for no functional gain.
- Do NOT rewrite `DockedComposerHostView.getFocusRequestCount()` to
  artificially return `> 0`. That would "fix" the test by moving the
  lie — the test would pass but against a false signal.
- Do NOT broaden scope to the `generativeAskWithHostInferenceNavigatesToDetailScreen`
  state-pack failures (R-gal1). Those have a different root cause
  (trust-spine assertion strictness on `uncertain_fit` mode) and
  are explicitly deferred to post-RC. R-val3 fixes one specific
  test-assertion gap; R-gal1 fixes another.
- Do NOT revert any piece of R-ui2 v3. The v3 product decision is
  correct; the test is what was wrong.
- Do NOT rebuild the main debug APK. Only the androidTest APK needs
  to change.
