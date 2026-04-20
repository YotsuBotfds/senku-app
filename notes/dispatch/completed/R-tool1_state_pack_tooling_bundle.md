# Slice R-tool1 — State-pack tooling bundle (5560 clipping + finalization + apk_sha reporting)

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. No
  subagent grant needed — bounded tooling investigation.
- **Parallel with:** `R-ui1_fully_scrollable_activity_main.md`.
  No file overlap (R-ui1 is XML-only; R-tool1 is PS1 + optional
  androidTest helpers). Dispatch both concurrently.
- **Predecessor:** S2-rerun2 landed RED at
  `artifacts/cp9_stage2_rerun2_20260420_070512/` — see
  `summary.md` "Notes" for the tooling repro evidence.

## Context

S2-rerun2 ships substantial Wave B progress (14/20 actual vs
S2-rerun's 7/20) but three distinct state-pack tooling bugs
have muddied every validation run since S2 and now block the
pre-RC cut. All three live in the same tooling lane, so
bundling is efficient. Priority order by impact is:

1. **5560 landscape capture clipping** (highest impact). When
   the UI validation pack runs on `emulator-5560`, some answer
   screens capture to a `2400x331` top strip — just the hero
   panel, no detail body. This has now cost us two validation
   runs (S2-rerun clipped `uncertain_fit_drowning_resuscitation`
   on 5560; S2-rerun2 clipped `abstain_violin_bridge_soundpost`
   and `safety_uncertain_fit_mania_escalation` on 5560). Codex
   reproduced the clipping on a bounded 5560 rerun in
   S2-rerun2 — it's sticky, not a one-off. Logcat shows the
   engine emitted the right mode both times; the capture is
   the chokepoint. Unblocks evidence verification for every
   future validation run.

2. **State-pack `apk_sha` reporting** (medium impact). The
   state-pack summary reports
   `apk_sha = e3b0c442798fcc4c57a21fa9a5e43f82b85e9c56ea45f2a5fe05ad95f2a86e4b`
   (SHA256 of empty string) instead of the real deployed APK
   sha. RP2's per-serial provenance shows
   `804119cbebc4a64a08cf622fe87354d725d417a5716ddb16ae67a238abc259f3`
   on all four serials, so the APK IS correct on-device —
   the bug is that the state-pack builder can't read it.
   Breaks the gallery's proof-of-APK-provenance story; S3
   closure will want this clean so the gallery can
   independently prove the cut built from the right tree.
   Starting point: `scripts/build_android_ui_state_pack.ps1`
   around lines 233, 262-310 (aggregation and
   `Normalize-IdentityValue` pipeline for `summary.apk_sha`).
   The upstream producer that writes each per-device
   `summary.apk_sha` is likely in
   `scripts/run_android_instrumented_ui_smoke.ps1` or another
   per-device smoke runner.

3. **`build_android_ui_state_pack_parallel.ps1` finalization
   mismatch** (lowest impact, has workaround). The
   parallel state-pack runner hits a PowerShell
   `Argument types do not match` error during finalization.
   Codex has worked around it by reconstructing the top-level
   `manifest.json`/`summary.json` from `role_manifests/*.json`
   on S2, S2-rerun, and S2-rerun2. The workaround works
   reliably, but eliminating the error removes the workaround
   burden from every future run. Fix priority is third because
   the workaround is well-understood.

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only scripts under `scripts/*.ps1` and any helper
  utilities those scripts `.` (dot-source) directly.
- May touch `androidTest` helper Java/Kotlin ONLY if a
  diagnostic reveals the per-device `summary.apk_sha`
  producer lives there (e.g., if instrumented code writes
  the summary). Keep any such change narrow — add sha
  extraction or fix an existing extraction; do NOT rewrite
  the smoke lane.
- Do NOT touch `main/` Java (engine, classifier, activity,
  render path — all unrelated).
- Do NOT touch `activity_main.xml` (R-ui1 owns it).
- Do NOT touch `OfflineAnswerEngine.java`,
  `QueryMetadataProfile.java`, `DetailActivity.java`, or any
  core engine/UI file.
- Do NOT modify the core UI validation pack prompt set or
  Wave B contract — bug fixes only; no behavior changes to
  what gets validated.
- Do NOT fix the gallery script's
  `build_android_ui_state_pack_parallel.ps1` finalization
  error by rewriting the parallel runner from scratch —
  identify the specific type mismatch and fix it narrowly.
- Single commit for the three fixes. If one sub-fix is
  genuinely out-of-scope of the others and shouldn't land
  together, flag it and stop — don't force-bundle.

## Outcome

1. `emulator-5560` landscape captures produce full detail
   screen images, not clipped top strips. Verified by running
   the instrumented UI smoke against 5560 and observing a
   full-body screenshot.
2. State-pack `summary.json` reports the actual deployed APK
   sha matching RP2's `apk_sha_<serial>.json` files on all
   four serials.
3. `build_android_ui_state_pack_parallel.ps1` completes
   finalization without the `Argument types do not match`
   error; the top-level `manifest.json` / `summary.json` are
   produced directly by the script, not via the Codex
   workaround.

## The work

### Sub-issue 1 — 5560 landscape capture clipping (highest priority)

1. **Reproduce.** Start `emulator-5560` in landscape posture
   per `scripts/start_senku_emulator_matrix.ps1`. Run the UI
   validation pack against 5560 for a prompt known to have
   been clipped in S2-rerun2 (e.g., `abstain_violin_bridge_soundpost`
   or `safety_uncertain_fit_mania_escalation`). Confirm the
   output screenshot is `2400x331` or similar top-strip shape.

2. **Localize.** Trace the capture path.
   - Likely starting points:
     `scripts/run_android_ui_validation_pack.ps1`,
     `scripts/run_android_instrumented_ui_smoke.ps1`, or a
     helper in
     `android-app/app/src/androidTest/java/com/senku/mobile/`
     that drives the screenshot invocation.
   - Look for any `adb shell screencap`, `screenshot`,
     `UiDevice.takeScreenshot`, or equivalent call that
     constructs the capture target bounds.
   - Suspects in order of likelihood:
     a. A landscape-specific branch of the capture code uses
        the wrong display metrics (reads portrait
        width/height when 5560 is in landscape, so only the
        top band captures).
     b. Screenshot timing fires before the WindowManager
        finishes a posture-change layout, so the layout
        thinks it's still portrait-sized.
     c. The screenshot command is piped through a scaler or
        crop helper that has a landscape bug.
   - Rule in/out by examining one captured file's pixel
     dimensions and comparing against the device's current
     display metrics from `adb shell wm size` on 5560
     landscape.

3. **Fix.** Narrowly — prefer the minimal change. If it's a
   metrics-read bug, correct the axis. If it's a timing bug,
   add a post-posture-change settle before capture. If it's
   a helper script bug, fix the helper.

4. **Verify.** Re-run the UI validation pack against 5560
   landscape for the two previously-clipped prompts. Confirm
   full-body captures. Keep one "before" and one "after"
   screenshot as evidence in the report.

### Sub-issue 2 — State-pack `apk_sha` reporting

1. **Reproduce.** Run the state-pack builder against RP2's
   deployed matrix — no need to re-provision. Observe the
   output `summary.json` reports `apk_sha = e3b0c442...`
   (empty-string SHA256).

2. **Localize.** Walk the aggregation chain.
   - `scripts/build_android_ui_state_pack.ps1` aggregates
     per-device `summary.apk_sha` values at lines 262-310
     (the `Normalize-IdentityValue` pipeline → `apk_shas`
     HashSet → single-value extraction). If every per-device
     `apk_sha` is empty, the aggregation chain correctly
     produces the empty-string SHA256 as a degenerate case.
   - Find WHERE the per-device `summary.apk_sha` gets
     written. Likely in
     `scripts/run_android_instrumented_ui_smoke.ps1` or a
     smoke runner that writes the per-device summary JSON.
     Look for where it reads the installed APK sha — it
     probably shells out to
     `adb shell pm path com.senku.mobile` and then
     attempts to pull the APK and sha it.
   - If that pull-and-sha step silently fails, it could be
     emitting an empty string which propagates to
     `e3b0c442...` downstream.

3. **Fix.** Minimal. If the extraction silently fails, make
   it fail loud and emit the sha. Cross-check that the
   per-device step uses the exact path `pm path`
   returns; on multi-APK apps (split APKs for different
   ABIs) there can be multiple paths and the script may
   pick the wrong one.

4. **Verify.** Re-run the state-pack builder and confirm
   `summary.json` reports
   `apk_sha = 804119cbebc4a64a08cf622fe87354d725d417a5716ddb16ae67a238abc259f3`
   matching RP2's provenance.

### Sub-issue 3 — `build_android_ui_state_pack_parallel.ps1` finalization

1. **Reproduce.** Run the parallel state-pack script against
   RP2's matrix. Observe the `Argument types do not match`
   error during finalization.

2. **Localize.** The error is a PowerShell type-coercion
   failure. Typical causes:
   - Passing a `hashtable` where a
     `System.Management.Automation.PSCustomObject` is
     expected (or vice versa).
   - Piping a `string[]` through a cmdlet that expects a
     single `string`, or the inverse.
   - Using `.Add()` on a `PSCustomObject` property that
     isn't a real collection.
   - Boolean-to-string coercion in a `Set-Content` path.

   Reading the stack trace from the error should localize
   the offending line. If the error message is generic, turn
   on `$ErrorActionPreference = 'Stop'` and
   `Set-PSDebug -Trace 2` transiently to get a full trace,
   then remove the tracing before committing.

3. **Fix.** Narrow type correction. Do NOT rewrite the
   finalization. Do NOT skip running the finalization to
   mask the error (the whole point is removing the Codex
   reconstruction workaround).

4. **Verify.** Run the parallel script end-to-end and
   confirm:
   - No `Argument types do not match` error.
   - Top-level `manifest.json` and `summary.json` are
     produced without needing manual reconstruction from
     `role_manifests/*.json`.

### Step 4 — Regression check

After all three fixes:

- Run the parallel state-pack end-to-end against RP2's
  matrix.
- Confirm every per-role `summary.json` reports the correct
  `apk_sha`.
- Confirm 5560 landscape captures are full-body.
- Confirm no finalization error, no manual reconstruction
  needed.
- If any ONE of the three bugs reproduces, stop and report
  which one; the other two do not block the commit but the
  bug needs a paper trail.

### Step 5 — Commit

Single commit. Suggested title:
`R-tool1: fix 5560 landscape capture clipping, state-pack apk_sha reporting, and parallel finalization mismatch`

## Acceptance

- 5560 landscape UI validation pack produces full-body
  detail captures; "before" and "after" screenshots
  attached in the report.
- State-pack `summary.json` reports the correct APK sha
  matching RP2's provenance.
- `build_android_ui_state_pack_parallel.ps1` completes
  finalization without the `Argument types do not match`
  error; no manual reconstruction needed.
- Single commit; PS1-only (plus optional narrow androidTest
  helper if a diagnostic forces it).
- `git status` shows only the scripts/ edits plus pre-
  existing unrelated dirty state.

## Report format

Reply with:
- Commit sha.
- Per-sub-issue: root cause (one paragraph) + file+line of
  the fix + one-line summary of what changed.
- Paths to before/after evidence:
  - 5560 landscape screenshot pair.
  - State-pack `summary.json` apk_sha field (before
    vs after).
  - Parallel script output (before: error; after: clean
    finalization).
- Any out-of-scope finding.
- Delegation log (expected "none; main inline").

## Anti-recommendations

- Do NOT mask the 5560 clipping by downsizing or cropping
  captures. The fix must produce full-body evidence.
- Do NOT mask the apk_sha bug by hard-coding the expected
  sha. Read it from the device.
- Do NOT wrap the finalization error in `try/catch` and
  swallow it. Fix the type mismatch.
- Do NOT expand scope beyond these three bugs. The gallery
  gallery 41/45 regression (`generativeAskWithHostInferenceNavigatesToDetailScreen`)
  is a separate post-RC investigation (`R-gal1`); R-tool1
  does NOT touch it.