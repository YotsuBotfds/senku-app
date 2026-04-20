# Slice RP2 — APK rebuild + four-serial re-provision (post R-val2/R-eng2)

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Serial after:** R-val2 (`6665bd8`) + R-eng2 (`8990cc6`). Both
  must be landed before this slice runs.
- **Predecessor context:** R-eng2 changed production Android Java
  (`OfflineAnswerEngine.java`), so the on-emulator APK at RP1's
  `389d8d0f77158a89fbcd274f3fc48afe2019b3785f2b06d0b359e6913e915cbb`
  is stale. R-val2 changed androidTest code
  (`PromptHarnessSmokeTest.java`), which is built into the
  instrumentation test APK used by S2-rerun2. RP2 brings the
  four-serial matrix to a clean state on the post-remediation
  debug APK, reusing R-pack's mobile pack from RP1. The mobile
  pack itself is unchanged — R-val2/R-eng2 did not touch
  `ingest.py`, `mobile_pack.py`, or any pack content.

## Preconditions (HARD GATE — STOP if violated)

1. R-val2 landed: commit `6665bd8` is an ancestor of HEAD
   (`git merge-base --is-ancestor 6665bd8 HEAD`).
2. R-eng2 landed: commit `8990cc6` is an ancestor of HEAD.
3. All four emulator serials (5556, 5560, 5554, 5558) are up per
   the fixed matrix. If not, restart per
   `scripts/start_senku_emulator_matrix.ps1` and re-confirm.
4. R-pack pack directory still exists at
   `artifacts/mobile_pack/senku_20260419_213821_r-pack/` (this
   is what RP1 pushed and what RP2 will reuse).

If any precondition fails, STOP and report. Do not proceed.

## Outcome

Four-serial matrix on a new debug APK (built at HEAD with
R-val2/R-eng2 changes baked in) and R-pack's mobile pack, with
`PACK READY` re-observed on each serial. Authoritative rollup at
`artifacts/cp9_stage1_rcv5_<ts>/pack_build.json` with
`apk_sha_homogeneous: true`, `pack_sha_homogeneous: true`,
`matrix_homogeneous: true`.

## The work (all main inline — device state is critical path)

### Step 1 — Rebuild APK at HEAD

From the `android-app/` directory, run a fresh debug build:

- `./gradlew.bat :app:assembleDebug` (or whatever invocation
  matches RP1's build pattern — check
  `artifacts/cp9_stage1_rcv4_20260419_214851/apk_build.log` for
  RP1's exact command if needed).

Capture stdout to `artifacts/cp9_stage1_rcv5_<ts>/apk_build.log`.

Sha256 the resulting `android-app/app/build/outputs/apk/debug/app-debug.apk`
and write `apk_sha_built.json` with the sha and file size.

The new sha MUST differ from
`389d8d0f77158a89fbcd274f3fc48afe2019b3785f2b06d0b359e6913e915cbb`
(the RP1 APK). If it matches, something failed to rebuild —
STOP and report; the R-eng2 changes are not in the new APK.
(R-val2's harness changes are in the instrumentation test APK,
not the main APK, so the main-APK sha delta is driven solely by
R-eng2.)

### Step 2 — Push new APK to all four serials

For each of `5556`, `5560`, `5554`, `5558`:

- `adb -s emulator-<serial> install -r android-app/app/build/outputs/apk/debug/app-debug.apk`
- Capture install output to
  `artifacts/cp9_stage1_rcv5_<ts>/apk_install_<serial>.log`

### Step 3 — Verify installed APK sha matches across all four serials

For each serial, pull the installed APK via
`adb -s emulator-<serial> shell pm path com.senku.mobile`,
sha256, and write
`artifacts/cp9_stage1_rcv5_<ts>/apk_sha_<serial>.json` with
`{ "serial": "...", "installed_apk_sha": "...", "match_built": true }`.

All four must match the sha from Step 1.

### Step 4 — Verify pack state / re-push if needed

The R-pack pack was pushed to all four serials in RP1 and should
still be present (`adb install -r` preserves app private data).
First probe whether the pack is still present:

- For each serial, run the relevant probe (e.g. `adb -s
  emulator-<serial> shell pm list packages` + a pack-state check
  per the smoke script pattern).

If the pack is present on all four, skip re-push. Record this
decision in the rollup as `"pack_repush_required": false`.

If any serial is missing the pack (or cannot confirm), re-push
using `scripts/push_mobile_pack_to_android.ps1` against the
affected serial(s), sourcing from
`artifacts/mobile_pack/senku_20260419_213821_r-pack/`. Capture
per-serial logs to `pack_push_<serial>.log` in the RP2 dir.
Record as `"pack_repush_required": true` with the serial list.

### Step 5 — Verify pack + PACK READY across the matrix

Run `scripts/run_android_instrumented_ui_smoke.ps1` against each
serial to confirm the `PACK READY` badge appears. Write fresh
per-serial `pack_install_<serial>.json` in the same shape as
RP1's:
```json
{
  "serial": "...",
  "pack_sha": "e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc",
  "installed_ok": true,
  "badge_observed": true,
  "timestamp": "..."
}
```

All four must report `installed_ok: true` and
`badge_observed: true`. The pack_sha must match
`e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`
on all four (same R-pack pack as RP1).

### Step 6 — Roll up

Write `artifacts/cp9_stage1_rcv5_<ts>/pack_build.json` with the
same shape as RP1's rollup:

```json
{
  "pack_sha": "e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc",
  "pack_dir": "<absolute path to artifacts/mobile_pack/senku_20260419_213821_r-pack>",
  "pack_total_bytes": 361753870,
  "serials": ["5556", "5560", "5554", "5558"],
  "matrix_homogeneous": true,
  "pack_sha_homogeneous": true,
  "apk_sha_homogeneous": true,
  "apk_sha": ["<built APK sha>"],
  "model_sha_homogeneous_on_device": true,
  "on_device_model_sha": ["f335f2bfd1b758dc6476db16c0f41854bd6237e2658d604cbe566bcefd00a7bc"],
  "host_inference_serials": ["5554", "5558"],
  "landscape_phone_scope_cut": false,
  "predecessor_artifact_dirs": [
    "artifacts/cp9_stage1_rcv4_20260419_214851",
    "artifacts/mobile_pack/senku_20260419_213821_r-pack"
  ],
  "rcv5_note": "Built at HEAD with R-val2 (6665bd8) + R-eng2 (8990cc6) on top of prior remediation stack. R-val2 harness changes live in the instrumentation test APK (rebuilt by the androidTest lane; main-APK sha delta driven by R-eng2). RC v5 candidate substrate for S2-rerun2.",
  "pack_repush_required": <true|false>,
  "artifacts": [...]
}
```

Also write a fresh `device_identity_matrix.json` mirroring the
RP1 shape but with the new APK values.

## Boundaries

- No code changes anywhere. Pure provisioning.
- No re-export of the mobile pack — reuse
  `artifacts/mobile_pack/senku_20260419_213821_r-pack/` from RP1.
- No changes to model files on any serial.
- No commits. Artifact-only slice (same pattern as RP1).
- Do not edit any tracker, queue, or dispatch markdown.

## Acceptance

- `pack_build.json` exists at `artifacts/cp9_stage1_rcv5_<ts>/`
  with `apk_sha_homogeneous: true`, `pack_sha_homogeneous: true`,
  `matrix_homogeneous: true`.
- All four `pack_install_<serial>.json` report
  `installed_ok: true` and `badge_observed: true`.
- All four `apk_sha_<serial>.json` report `match_built: true`.
- The built APK sha differs from `389d8d0f...` (proves R-eng2
  main-code change made it into the build).
- `git status` shows only new `artifacts/` paths plus pre-existing
  unrelated dirty state.

## Report format

Reply with:
- Built APK sha (Step 1).
- Per-serial installed APK sha (4 lines).
- Whether pack re-push was required (Step 4 decision + per-serial
  notes if so).
- Pack sha (should be
  `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`
  — unchanged from RP1).
- Path to new `pack_build.json`.
- Any anomaly (e.g. APK sha didn't change, PACK READY took two
  tries on a serial, pack was unexpectedly missing on a serial).
- Delegation log (likely "none — all main inline; device state
  coupling").
- Explicit "ready for S2-rerun2" flag or "S2-rerun2 blocked —
  reason".