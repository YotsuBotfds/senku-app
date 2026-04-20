# Slice RP4 — APK rebuild + four-serial re-provision (post R-ui2 v3)

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. No subagent
  delegation — device-state coupling is critical path.
- **Serial after:** R-val2 (`6665bd8`) + R-eng2 (`8990cc6`) +
  R-ui1 (`29463eb`) + R-tool1 (`2ba7d5c`) + **R-ui2 (`f095194`)**.
  All five must be landed before this slice runs.
- **Predecessor context:** R-ui2 v3 removed the
  `shouldAutoFocusLandscapeComposer` auto-focus call chain in
  `DetailActivity.java`, suppressed the follow-up suggestion rail
  on landscape phone (to keep the answer body visible), and
  committed three previously-untracked v2 hygiene files
  (`AndroidManifest.xml` with `stateHidden|adjustResize`,
  both `activity_detail.xml` layout variants with root focus-trap
  attrs). The main debug APK at RP3's
  `66fb10cb02bfa140a96a12ea2971723349c4a89478cea51574e3ca41f9ca8e3b`
  is stale relative to HEAD. R-ui2 v3 did NOT touch any
  `androidTest` code, so the instrumentation-test APK sha from
  RP3 (`b02279159dbf44758c20892e5a0f548bd9904247f0bb814484dc93e4a0f52cb3`)
  is expected to match — treat as a sanity check, not a hard gate,
  because Gradle builds can exhibit minor non-determinism
  (observed in S1 / S1.1 where identical-source APKs hashed
  differently across successive builds). The mobile pack itself is
  unchanged — no slice since R-pack has touched `ingest.py`,
  `mobile_pack.py`, or any pack content.

## Preconditions (HARD GATE — STOP if violated)

1. R-val2 landed: `git merge-base --is-ancestor 6665bd8 HEAD`.
2. R-eng2 landed: `git merge-base --is-ancestor 8990cc6 HEAD`.
3. R-ui1 landed: `git merge-base --is-ancestor 29463eb HEAD`.
4. R-tool1 landed: `git merge-base --is-ancestor 2ba7d5c HEAD`.
5. R-ui2 landed: `git merge-base --is-ancestor f095194 HEAD`.
6. All four emulator serials (5556, 5560, 5554, 5558) are up per
   the fixed matrix. If not, restart per
   `scripts/start_senku_emulator_matrix.ps1` and re-confirm.
7. R-pack pack directory still exists at
   `artifacts/mobile_pack/senku_20260419_213821_r-pack/` (sha
   `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`).

If any precondition fails, STOP and report. Do not proceed.

## Outcome

Four-serial matrix on a new debug APK (built at HEAD with R-ui2's
DetailActivity + manifest + layout changes baked in) and R-pack's
unchanged mobile pack, with `PACK READY` re-observed on each
serial. Authoritative rollup at
`artifacts/cp9_stage1_rcv7_<ts>/pack_build.json` with
`apk_sha_homogeneous: true`, `pack_sha_homogeneous: true`,
`matrix_homogeneous: true`.

## The work (all main inline — device state is critical path)

### Step 1 — Rebuild APK at HEAD

From the `android-app/` directory, run a fresh debug build plus
the androidTest lane so both APKs reflect HEAD:

- `./gradlew.bat :app:assembleDebug` (mirror RP3 invocation; check
  `artifacts/cp9_stage1_rcv6_20260420_093252/apk_build.log` for
  the exact command if needed).
- `./gradlew.bat :app:assembleDebugAndroidTest` (for
  completeness and to confirm androidTest-side stability; we
  expect the resulting sha to match RP3's).

Capture stdout to `artifacts/cp9_stage1_rcv7_<ts>/apk_build.log`
and `.../apk_build_androidtest.log`.

Sha256 both resulting APKs and write:
- `apk_sha_built.json` with the debug-APK sha and file size.
- `apk_sha_built_androidtest.json` with the instrumentation-APK
  sha and file size.

The new debug-APK sha MUST differ from
`66fb10cb02bfa140a96a12ea2971723349c4a89478cea51574e3ca41f9ca8e3b`
(the RP3 APK). If it matches, something failed to rebuild —
STOP and report; R-ui2's DetailActivity change is not in the new
APK.

The instrumentation-APK sha is EXPECTED to match RP3's
`b02279159dbf44758c20892e5a0f548bd9904247f0bb814484dc93e4a0f52cb3`
because R-ui2 v3 did not touch `PromptHarnessSmokeTest.java` or
any other androidTest file. If it matches, note as "instrumentation
sha stable." If it differs, do not stop — record the delta in
the rollup as `instrumentation_sha_delta_note` with a brief
explanation (likely Gradle build non-determinism, same phenomenon
as S1 / S1.1's APK reparity episode) and continue; subsequent
steps still work on whatever was built.

### Step 2 — Push new APK to all four serials

For each of `5556`, `5560`, `5554`, `5558`:

- `adb -s emulator-<serial> install -r android-app/app/build/outputs/apk/debug/app-debug.apk`
- `adb -s emulator-<serial> install -r android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
- Capture install output to
  `artifacts/cp9_stage1_rcv7_<ts>/apk_install_<serial>.log` and
  `apk_install_androidtest_<serial>.log`.

### Step 3 — Verify installed APK sha matches across all four serials

For each serial, pull the installed debug APK via
`adb -s emulator-<serial> shell pm path com.senku.mobile`,
sha256, and write
`artifacts/cp9_stage1_rcv7_<ts>/apk_sha_<serial>.json` with
`{ "serial": "...", "installed_apk_sha": "...", "match_built": true }`.

For the instrumentation test APK, pull via
`adb -s emulator-<serial> shell pm path com.senku.mobile.test`,
sha256, and write
`apk_sha_androidtest_<serial>.json` with the same shape.

All four debug-APK shas must match the sha from Step 1.
All four instrumentation-APK shas must match each other and
match the Step 1 instrumentation sha.

### Step 4 — Verify pack state / re-push if needed

The R-pack pack was pushed to all four serials in RP1 and should
have survived subsequent APK reinstalls (`adb install -r`
preserves app private data). RP3 observed all four serials
retained the pack. First probe whether the pack is still present:

- For each serial, run the relevant probe (e.g. `adb -s
  emulator-<serial> shell pm list packages` + a pack-state check
  per the smoke script pattern, or read the `PACK READY` badge
  via an instrumented smoke run).

If the pack is present on all four, skip re-push. Record this
decision in the rollup as `"pack_repush_required": false`.

If any serial is missing the pack (or cannot confirm), re-push
using `scripts/push_mobile_pack_to_android.ps1` against the
affected serial(s), sourcing from
`artifacts/mobile_pack/senku_20260419_213821_r-pack/`. Capture
per-serial logs to `pack_push_<serial>.log` in the RP4 dir.
Record as `"pack_repush_required": true` with the serial list.

### Step 5 — Verify pack + PACK READY across the matrix

Run `scripts/run_android_instrumented_ui_smoke.ps1` against each
serial to confirm the `PACK READY` badge appears. This is a
provisioning smoke, not a Wave B run — scoring is S2-rerun4's
territory. Optionally eyeball that on the 5560 landscape serial,
the DetailActivity body + escalation text are visible at settle
(this is the R-ui2 v3 behavior change; minor sanity confirmation
only, not required as a gate).

Write fresh per-serial `pack_install_<serial>.json` in the same
shape as RP3's:

```json
{
  "serial": "...",
  "pack_sha": "e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc",
  "installed_ok": true,
  "badge_observed": true,
  "timestamp": "..."
}
```

All four must report `installed_ok: true` and `badge_observed: true`.
The pack_sha must match
`e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`
on all four (same R-pack pack as RP1 / RP2 / RP3).

Known flake to watch for: RP3 hit a transient System UI ANR on
emulator-5554 (tablet portrait) that needed dismissing before
the final PACK READY smoke passed. If it recurs, dismiss the ANR
dialog (per the standard adb input-event pattern) and retry; do
not treat it as a blocker. Note in the rollup's anomaly section
if it happens again.

### Step 6 — Roll up

Write `artifacts/cp9_stage1_rcv7_<ts>/pack_build.json` with the
same shape as RP3's rollup:

```json
{
  "pack_sha": "e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc",
  "pack_dir": "<absolute path to artifacts/mobile_pack/senku_20260419_213821_r-pack>",
  "pack_total_bytes": 361753870,
  "serials": ["5556", "5560", "5554", "5558"],
  "matrix_homogeneous": true,
  "pack_sha_homogeneous": true,
  "apk_sha_homogeneous": true,
  "apk_sha": ["<built debug APK sha>"],
  "apk_sha_androidtest": ["<built instrumentation APK sha>"],
  "model_sha_homogeneous_on_device": true,
  "on_device_model_sha": ["f335f2bfd1b758dc6476db16c0f41854bd6237e2658d604cbe566bcefd00a7bc"],
  "host_inference_serials": ["5554", "5558"],
  "landscape_phone_scope_cut": false,
  "predecessor_artifact_dirs": [
    "artifacts/cp9_stage1_rcv6_20260420_093252",
    "artifacts/mobile_pack/senku_20260419_213821_r-pack"
  ],
  "rcv7_note": "Built at HEAD with R-ui2 v3 (f095194) on top of RP3's stack (R-ui1 29463eb + R-tool1 2ba7d5c + RP2's R-val2 6665bd8 + R-eng2 8990cc6 + R-pack bd84835 + R-eng 1f76ccf + R-cls e07d4e7). Debug-APK sha delta driven by R-ui2's DetailActivity.java auto-focus removal, follow-up suggestion rail suppression on landscape phone, and the three v2-hygiene files (AndroidManifest.xml, both activity_detail.xml layouts) that R-ui2 committed as new-tracked entries. Instrumentation-APK sha expected stable vs RP3 (no androidTest changes in R-ui2). RC v5 candidate substrate for S3 cut after S2-rerun4 GREEN.",
  "pack_repush_required": <true|false>,
  "pack_repush_serials": [<per-serial list if any>],
  "instrumentation_sha_delta_note": "<omit if sha matches RP3; include with explanation if it differs>",
  "artifacts": [...]
}
```

Also write a fresh `device_identity_matrix.json` mirroring RP3's
shape but with the new APK values.

## Boundaries

- No code changes anywhere. Pure provisioning.
- No re-export of the mobile pack — reuse
  `artifacts/mobile_pack/senku_20260419_213821_r-pack/` from
  RP1/RP2/RP3.
- No changes to model files on any serial.
- No commits. Artifact-only slice (same pattern as RP1 / RP2 / RP3).
- Do not edit any tracker, queue, or dispatch markdown.
- Do NOT run a full Wave B validation sweep. S2-rerun4's territory.

## Acceptance

- `pack_build.json` exists at `artifacts/cp9_stage1_rcv7_<ts>/`
  with `apk_sha_homogeneous: true`, `pack_sha_homogeneous: true`,
  `matrix_homogeneous: true`.
- All four `pack_install_<serial>.json` report
  `installed_ok: true` and `badge_observed: true`.
- All four `apk_sha_<serial>.json` report `match_built: true`
  for the debug APK.
- All four `apk_sha_androidtest_<serial>.json` report matching
  shas for the instrumentation test APK.
- The built debug-APK sha differs from `66fb10cb...` (proves
  R-ui2's code change made it into the build).
- `git status` shows only new `artifacts/` paths plus pre-
  existing unrelated dirty state.

## Report format

Reply with:
- Built debug-APK sha (Step 1).
- Built instrumentation-APK sha (Step 1) + whether it matches
  RP3's `b02279159...` (sanity-check note).
- Per-serial installed debug-APK sha (4 lines).
- Per-serial installed instrumentation-APK sha (4 lines).
- Whether pack re-push was required (Step 4 decision + per-serial
  notes if so).
- Pack sha (should be `e48d3e1ab068c666...` — unchanged from
  RP1 / RP2 / RP3).
- Path to new `pack_build.json`.
- One-line note on the Step 5 PACK READY sanity across all four
  serials; optional one-liner on the 5560 landscape eyeball check
  (body + escalation visible post-R-ui2).
- Any anomaly (APK sha didn't change, PACK READY took two tries
  on a serial, pack unexpectedly missing, instrumentation APK
  hit `INSTALL_FAILED_*`, transient System UI ANR on 5554 — all
  known patterns to flag if they recur).
- Delegation log (likely "none — all main inline; device state
  coupling").
- Explicit "ready for S2-rerun4" flag or "S2-rerun4 blocked — reason".
