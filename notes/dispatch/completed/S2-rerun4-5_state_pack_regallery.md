# Slice S2-rerun4.5 — State-pack gallery regeneration on post-R-val3 substrate

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. Device-state
  critical path; no delegation.
- **Predecessor:** R-val3 (`607ab916`) redefined
  `waitForLandscapeDockedComposerReady` to accept interactive
  readiness rather than focus, restoring
  `deterministicAskNavigatesToDetailScreen` on phone_landscape. Bounded
  re-probe in R-val3 already confirmed the fix works on 5560 and three
  regression-check serials. This slice produces the actual published
  gallery artifact S3 closure will cite.
- **What this slice is NOT:** Not a full Wave B re-validation.
  S2-rerun4's Wave B contract is already at 20/20 actual under Option C
  with zero RC-blocking failures. The Wave B result is frozen; no
  re-score is needed. This slice is a narrow state-pack-only
  re-capture to refresh the gallery artifact.

## Preconditions (HARD GATE — STOP if violated)

1. R-val3 landed: `git merge-base --is-ancestor 607ab916 HEAD`.
2. All four emulator serials (5556, 5560, 5554, 5558) are up per the
   fixed matrix. If not, restart per
   `scripts/start_senku_emulator_matrix.ps1`.
3. Debug APK `551385c9…` is still installed on all four serials
   (RP4's APK, unchanged by R-val3). Verify via
   `adb -s emulator-<serial> shell pm path com.senku.mobile` →
   sha256 check.
4. AndroidTest APK `b260a219…` is still installed on all four serials
   (R-val3's APK). Verify via
   `adb -s emulator-<serial> shell pm path com.senku.mobile.test` →
   sha256 check. These should still be present from R-val3's Step 5,
   but worth confirming in case something else touched the devices
   since.

If any precondition fails, STOP and report.

## Outcome

- Fresh state-pack sweep across the fixed four-emulator matrix on the
  post-R-val3 substrate.
- New published gallery at
  `artifacts/external_review/ui_review_<YYYYMMDD>_gallery_v6/` (use
  `_v6` suffix — do NOT clobber the `_v5` gallery from S2-rerun4).
- Expected coverage: 41/45. The four `generativeAskWithHostInferenceNavigatesToDetailScreen`
  failures on each posture (phone_portrait, phone_landscape,
  tablet_portrait, tablet_landscape) persist as the known R-gal1
  carryover. No other failures expected.
- `artifacts/cp9_stage2_rerun4_5_<ts>/summary.md` documenting the
  regenerated gallery, coverage delta vs S2-rerun4 (40/45 → 41/45),
  and per-state-id confirmation that
  `phone_landscape/deterministicAskNavigatesToDetailScreen` now passes.

## Boundaries (HARD GATE)

- No code commits.
- No APK rebuild or install — the matrix under test is exactly what
  R-val3 left in place (debug APK `551385c9…` + androidTest APK
  `b260a219…`).
- No Wave B validation sweep. That's S2-rerun4's frozen result.
- No rain_shelter retrieval tuning attempts; no test-assertion changes;
  no product code changes.
- Do not edit any tracker / queue / dispatch markdown.
- If the state-pack sweep produces a coverage worse than 41/45 (any
  NEW regression beyond R-gal1's known 4 carryover), STOP and report.
  Do not attempt remediation in this slice.

## The work

### Step 1 — State-pack matrix sweep

Run `scripts/build_android_ui_state_pack_parallel.ps1` against the
fixed four-emulator matrix.

Capture output to
`artifacts/cp9_stage2_rerun4_5_<ts>/ui_state_pack/<sweep_ts>/`.

This step produces per-posture captures (phone_portrait,
phone_landscape, tablet_portrait, tablet_landscape), their summary
JSON files, and the raw instrumentation logs.

### Step 2 — Gallery publication

Publish the state-pack sweep output to
`artifacts/external_review/ui_review_<YYYYMMDD>_gallery_v6/` with the
`_v6` suffix. This matches the existing gallery publication pattern
(v3 / v4 / v5 from S2-rerun / S2-rerun3 / S2-rerun4).

The gallery should include:
- `index.html` (per-posture + per-state thumbnail + evidence).
- Copies of the screenshots + dumps under the per-state subdirectories.
- `manifest.json` and `README.md` produced by the state-pack pipeline.

### Step 3 — Summary rollup

Write `artifacts/cp9_stage2_rerun4_5_<ts>/summary.md` with:

- Matrix-level state-pack coverage (expected 41/45).
- Per-posture pass/fail table.
- Per-state delta vs S2-rerun4's `ui_review_20260420_gallery_v5/`:
  - Expected delta: `phone_landscape/deterministicAskNavigatesToDetailScreen`
    pass/fail status changed from FAIL → PASS.
  - The four `generativeAskWithHostInferenceNavigatesToDetailScreen`
    states (one per posture) remain failing, unchanged from S2-rerun4.
  - All other states unchanged.
- APK + pack provenance (debug APK `551385c9…`, androidTest APK
  `b260a219…`, pack `e48d3e1ab068c666…`).
- State-pack tooling status:
  - `apk_sha` reporting matches `551385c9…` (expected — R-tool1's fix
    continues to hold).
  - Finalization mismatch did not reproduce (expected).
- Explicit "ready for S3 cut" flag.

### Step 4 — Cross-check gallery against expected

Spot-verify:
- `gallery_v6/index.html` loads and shows 41/45 across the matrix.
- `deterministicAskNavigatesToDetailScreen` on phone_landscape has a
  green / pass indicator in the gallery UI.
- The four R-gal1 failures are still visible as fails.

## Acceptance

- `ui_review_<YYYYMMDD>_gallery_v6/index.html` exists and shows 41/45.
- `artifacts/cp9_stage2_rerun4_5_<ts>/summary.md` exists and asserts
  coverage is 41/45 with zero new regressions vs the pre-R-val3
  baseline.
- `deterministicAskNavigatesToDetailScreen` on phone_landscape passes
  in the fresh sweep (confirmation that R-val3 holds on the full
  state-pack lane, not just the bounded re-probe).
- State-pack tooling (`apk_sha` reporting, finalization mismatch) is
  green.
- No new state-pack regressions beyond the known R-gal1 cluster.

## Report format

Reply with:
- Path to `summary.md` and `gallery_v6/index.html`.
- Coverage count (expected 41/45).
- Per-state delta list vs S2-rerun4's `_v5` gallery (expected: one
  state flips FAIL → PASS, rest unchanged).
- APK + pack sha confirmation (debug `551385c9…`, androidTest
  `b260a219…`, pack `e48d3e1ab068c666…`).
- State-pack tooling status.
- Any anomaly (new regressions, tooling issues, APK drift).
- Delegation log (expected: "none; main inline").
- Explicit "ready for S3 cut" flag or "S3 blocked — reason".

## Anti-recommendations

- Do NOT run the full Wave B validation sweep. That's S2-rerun4's
  territory — its 20/20 result is frozen and does not need
  re-confirmation.
- Do NOT rebuild or reinstall any APK. R-val3 left the matrix in the
  exact state this slice needs.
- Do NOT re-export or re-push the mobile pack.
- Do NOT flag the four R-gal1 `generativeAsk` failures as blockers —
  they are documented post-RC carry-over. The expected gallery state
  is 41/45 with those four failures, not 45/45.
- Do NOT attempt to fix any new regression discovered during this
  sweep without checking back with planner. STOP and report the new
  regression instead.
