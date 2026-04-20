# Slice RP3 — APK rebuild + four-serial re-provision (post R-ui1 / R-tool1)

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Serial after:** R-val2 (`6665bd8`) + R-eng2 (`8990cc6`) + R-ui1
  (`29463eb`) + R-tool1 (`2ba7d5c`). All four must be landed
  before this slice runs.
- **Predecessor context:** R-ui1 restructured
  `android-app/app/src/main/res/layout/activity_main.xml` (main-APK
  resource change), so the on-emulator debug APK at RP2's
  `804119cbebc4a64a08cf622fe87354d725d417a5716ddb16ae67a238abc259f3`
  is stale. R-tool1 changed
  `PromptHarnessSmokeTest.java` (androidTest harness — affects the
  instrumentation test APK, not the main APK) plus PowerShell
  tooling (does not affect either APK). RP3 brings the four-serial
  matrix to a clean state on the post-R-ui1 debug APK, with the
  post-R-tool1 instrumentation test APK installed so S3's
  validation inherits the tighter settle/capture discipline. The
  mobile pack itself is unchanged — none of R-val2 / R-eng2 /
  R-ui1 / R-tool1 touched `ingest.py`, `mobile_pack.py`, or any
  pack content.

## Preconditions (HARD GATE — STOP if violated)

1. R-val2 landed: `git merge-base --is-ancestor 6665bd8 HEAD`.
2. R-eng2 landed: `git merge-base --is-ancestor 8990cc6 HEAD`.
3. R-ui1 landed: `git merge-base --is-ancestor 29463eb HEAD`.
4. R-tool1 landed: `git merge-base --is-ancestor 2ba7d5c HEAD`.
5. All four emulator serials (5556, 5560, 5554, 5558) are up per
   the fixed matrix. If not, restart per
   `scripts/start_senku_emulator_matrix.ps1` and re-confirm.
6. R-pack pack directory still exists at
   `artifacts/mobile_pack/senku_20260419_213821_r-pack/` (this is
   what RP1 pushed, RP2 reused, and what RP3 will reuse again).

If any precondition fails, STOP and report. Do not proceed.

## Outcome

Four-serial matrix on a new debug APK (built at HEAD with
R-ui1's XML restructure baked in) and R-pack's mobile pack, with
`PACK READY` re-observed on each serial. Authoritative rollup at
`artifacts/cp9_stage1_rcv6_<ts>/pack_build.json` with
`apk_sha_homogeneous: true`, `pack_sha_homogeneous: true`,
`matrix_homogeneous: true`.

## The work (all main inline — device state is critical path)

### Step 1 — Rebuild APK at HEAD

From the `android-app/` directory, run a fresh debug build plus
the androidTest lane so both APKs reflect HEAD:

- `./gradlew.bat :app:assembleDebug` (mirror RP1/RP2 invocation;
  check `artifacts/cp9_stage1_rcv5_20260420_063320/apk_build.log`
  for the exact command if needed).
- `./gradlew.bat :app:assembleDebugAndroidTest` (so the
  instrumentation test APK includes R-tool1's
  `PromptHarnessSmokeTest.java` changes).

Capture stdout to `artifacts/cp9_stage1_rcv6_<ts>/apk_build.log`
and `.../apk_build_androidtest.log`.

Sha256 both resulting APKs and write:
- `apk_sha_built.json` with the debug-APK sha and file size.
- `apk_sha_built_androidtest.json` with the instrumentation-APK
  sha and file size.

The new debug-APK sha MUST differ from
`804119cbebc4a64a08cf622fe87354d725d417a5716ddb16ae67a238abc259f3`
(the RP2 APK). If it matches, something failed to rebuild —
STOP and report; R-ui1's XML change is not in the new APK. (The
debug-APK sha delta is driven solely by R-ui1's resource
restructure; R-tool1 lives in androidTest and its sha delta
lands in the instrumentation test APK, not the main debug APK.)

The instrumentation-APK sha is not pinned against a prior
baseline — just record it for provenance.

### Step 2 — Push new APK to all four serials

For each of `5556`, `5560`, `5554`, `5558`:

- `adb -s emulator-<serial> install -r android-app/app/build/outputs/apk/debug/app-debug.apk`
- `adb -s emulator-<serial> install -r android-app/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
- Capture install output to
  `artifacts/cp9_stage1_rcv6_<ts>/apk_install_<serial>.log` and
  `apk_install_androidtest_<serial>.log`.

### Step 3 — Verify installed APK sha matches across all four serials

For each serial, pull the installed debug APK via
`adb -s emulator-<serial> shell pm path com.senku.mobile`,
sha256, and write
`artifacts/cp9_stage1_rcv6_<ts>/apk_sha_<serial>.json` with
`{ "serial": "...", "installed_apk_sha": "...", "match_built": true }`.

For the instrumentation test APK, pull via
`adb -s emulator-<serial> shell pm path com.senku.mobile.test`,
sha256, and write
`apk_sha_androidtest_<serial>.json` with the same shape.

All four debug-APK shas must match the sha from Step 1.
All four instrumentation-APK shas must match each other and
match the Step 1 instrumentation sha.

### Step 4 — Verify pack state / re-push if needed

The R-pack pack was pushed to all four serials in RP1 and
should have survived RP2's APK reinstall (`adb install -r`
preserves app private data). First probe whether the pack is
still present:

- For each serial, run the relevant probe (e.g. `adb -s
  emulator-<serial> shell pm list packages` + a pack-state
  check per the smoke script pattern, or read the `PACK READY`
  badge via an instrumented smoke run).

If the pack is present on all four, skip re-push. Record this
decision in the rollup as `"pack_repush_required": false`.

If any serial is missing the pack (or cannot confirm), re-push
using `scripts/push_mobile_pack_to_android.ps1` against the
affected serial(s), sourcing from
`artifacts/mobile_pack/senku_20260419_213821_r-pack/`. Capture
per-serial logs to `pack_push_<serial>.log` in the RP3 dir.
Record as `"pack_repush_required": true` with the serial list.

### Step 5 — Verify pack + PACK READY across the matrix

Run `scripts/run_android_instrumented_ui_smoke.ps1` against
each serial to confirm the `PACK READY` badge appears, and
also confirm the Home tab now scrolls as one unit (R-ui1's
restructure lives in the new debug APK — a quick
"scroll from hero to Categories without bouncing" check is
enough; this is not a full Wave B run, just a provisioning
smoke). Write fresh per-serial `pack_install_<serial>.json`
in the same shape as RP2's:

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
on all four (same R-pack pack as RP1 / RP2).

### Step 6 — Roll up

Write `artifacts/cp9_stage1_rcv6_<ts>/pack_build.json` with
the same shape as RP2's rollup:

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
    "artifacts/cp9_stage1_rcv5_20260420_063320",
    "artifacts/mobile_pack/senku_20260419_213821_r-pack"
  ],
  "rcv6_note": "Built at HEAD with R-ui1 (29463eb) + R-tool1 (2ba7d5c) on top of RP2's remediation stack (R-val2 6665bd8 + R-eng2 8990cc6). Debug-APK sha delta driven by R-ui1's activity_main.xml restructure; instrumentation-APK sha delta driven by R-tool1's PromptHarnessSmokeTest.java tightening. RC v5 candidate substrate for S3 cut.",
  "pack_repush_required": <true|false>,
  "artifacts": [...]
}
```

Also write a fresh `device_identity_matrix.json` mirroring the
RP2 shape but with the new APK values.

## Boundaries

- No code changes anywhere. Pure provisioning.
- No re-export of the mobile pack — reuse
  `artifacts/mobile_pack/senku_20260419_213821_r-pack/` from RP1.
- No changes to model files on any serial.
- No commits. Artifact-only slice (same pattern as RP1 / RP2).
- Do not edit any tracker, queue, or dispatch markdown.
- Do NOT run a full Wave B validation sweep. That is S2-rerun3
  / S3's territory. RP3 is a substrate provisioning slice only —
  the Step 5 smoke is only to confirm PACK READY and a basic
  scroll sanity, not to score against the Wave B contract.

## Acceptance

- `pack_build.json` exists at `artifacts/cp9_stage1_rcv6_<ts>/`
  with `apk_sha_homogeneous: true`, `pack_sha_homogeneous: true`,
  `matrix_homogeneous: true`.
- All four `pack_install_<serial>.json` report
  `installed_ok: true` and `badge_observed: true`.
- All four `apk_sha_<serial>.json` report `match_built: true`
  for the debug APK.
- All four `apk_sha_androidtest_<serial>.json` report matching
  shas for the instrumentation test APK.
- The built debug-APK sha differs from `804119cb...` (proves
  R-ui1's resource change made it into the build).
- `git status` shows only new `artifacts/` paths plus pre-
  existing unrelated dirty state.

## Report format

Reply with:
- Built debug-APK sha (Step 1).
- Built instrumentation-APK sha (Step 1).
- Per-serial installed debug-APK sha (4 lines).
- Per-serial installed instrumentation-APK sha (4 lines).
- Whether pack re-push was required (Step 4 decision + per-serial
  notes if so).
- Pack sha (should be
  `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`
  — unchanged from RP1 / RP2).
- Path to new `pack_build.json`.
- One-line note on the Step 5 scroll sanity check (e.g. "5556
  Home tab scrolls as one unit from hero to Categories").
- Any anomaly (e.g. APK sha didn't change, PACK READY took two
  tries on a serial, pack was unexpectedly missing on a serial,
  instrumentation APK install hit a `INSTALL_FAILED_*` error).
- Delegation log (likely "none — all main inline; device state
  coupling").
- Explicit "ready for S3" flag or "S3 blocked — reason".
