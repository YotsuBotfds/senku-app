# Slice RP1 — APK rebuild + four-serial re-provision (post R-cls/R-eng/R-pack)

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Serial after:** R-cls (`e07d4e7`) + R-eng (`1f76ccf`) + R-pack
  (commit + new pack artifact dir). All three must be landed
  before this slice runs.
- **Predecessor context:** R-cls and R-eng changed production
  Android Java code, so the on-emulator APK at `88d0041e` is
  stale. R-pack rebuilt the mobile pack to address the missing
  poisoning chunks. RP1 brings the four-serial matrix to a clean
  state with the new APK and the new pack so S2-rerun has a
  trustworthy substrate.

## Preconditions (HARD GATE — STOP if violated)

1. R-cls landed: commit `e07d4e7` is in HEAD (verify with
   `git log -1 --format=%H e07d4e7` or `git merge-base --is-ancestor`).
2. R-eng landed: commit `1f76ccf` is in HEAD.
3. R-pack landed: there is a commit on HEAD whose message starts
   with `R-pack` (or whatever convention R-pack used — check `git
   log` for it), AND a new pack artifact dir exists matching
   `artifacts/mobile_pack/senku_*_r-pack*/` (or whatever R-pack's
   output dir naming was — read R-pack's report or
   `notes/CP9_ACTIVE_QUEUE.md` Completed log for the path). If
   the path is ambiguous, use the most recent
   `artifacts/mobile_pack/senku_*/` dir created after R-pack's
   commit.
4. All four emulator serials (5556, 5560, 5554, 5558) are up per
   the fixed matrix. If not, restart per
   `scripts/start_senku_emulator_matrix.ps1` and re-confirm.
5. The R-pack pack contains nonzero chunk rows for GD-898 /
   GD-301 / GD-054 / GD-602 (this is R-pack's acceptance — verify
   by spot-probing the new pack's SQLite if R-pack's report
   doesn't already include the counts).

If any precondition fails, STOP and report. Do not proceed.

## Outcome

Four-serial matrix on a single new APK (built at HEAD with R-cls +
R-eng changes baked in) and R-pack's new pack, with `PACK READY`
re-observed on each. Authoritative rollup at
`artifacts/cp9_stage1_rcv4_<ts>/pack_build.json` (or
`cp9_stage1_rpack_<ts>` — pick one and stick with it).

## The work (all main inline — device state is critical path)

### Step 1 — Rebuild APK at HEAD

From the `android-app/` directory, run a fresh debug build:
- `./gradlew.bat :app:assembleDebug` (or whatever invocation
  matches the existing build pattern — check
  `android-app/README.md` or scripts under `scripts/` for the
  canonical build invocation if Gradle direct doesn't work).

Capture stdout to `artifacts/cp9_stage1_rcv4_<ts>/apk_build.log`.

Sha256 the resulting `android-app/app/build/outputs/apk/debug/app-debug.apk`
and write `apk_sha_built.json` with the sha and the file size.

The new sha MUST differ from `88d0041e942a2be8723d8d1489a2421c2fe271d8919c0cbbe52eccbd0e9d9a18`
(the S1.1 reparity APK). If it matches, something failed to
rebuild — STOP and report; the R-cls + R-eng changes are not in
the new APK.

### Step 2 — Push new APK to all four serials

For each of `5556`, `5560`, `5554`, `5558`:
- `adb -s emulator-<serial> install -r android-app/app/build/outputs/apk/debug/app-debug.apk`
- Capture install output to `artifacts/cp9_stage1_rcv4_<ts>/apk_install_<serial>.log`

### Step 3 — Verify installed APK sha matches across all four serials

For each serial, pull the installed APK via `adb -s
emulator-<serial> shell pm path com.senku.mobile`, sha256, and
write `artifacts/cp9_stage1_rcv4_<ts>/apk_sha_<serial>.json` with
`{ "serial": "...", "installed_apk_sha": "...", "match_built":
true }`.

All four must match the sha from Step 1.

### Step 4 — Push R-pack's new pack to all four serials

Use `scripts/push_mobile_pack_to_android.ps1` against each serial,
sourcing from R-pack's new pack dir. Capture per-serial push logs
to `artifacts/cp9_stage1_rcv4_<ts>/pack_push_<serial>.log`.

### Step 5 — Verify pack + PACK READY across the matrix

Run `scripts/run_android_instrumented_ui_smoke.ps1` against each
serial to confirm the `PACK READY` badge appears. Write fresh
per-serial `pack_install_<serial>.json` in the same shape as
S1's:
```json
{
  "serial": "...",
  "pack_sha": "...",  // sha256 of the new pack
  "installed_ok": true,
  "badge_observed": true,
  "timestamp": "..."
}
```

All four must report `installed_ok: true` and `badge_observed:
true`. The pack_sha must match across all four serials.

### Step 6 — Roll up

Write `artifacts/cp9_stage1_rcv4_<ts>/pack_build.json` with the
same shape as S1's reparity rollup:
```json
{
  "pack_sha": "<R-pack's new pack sha>",
  "pack_dir": "<R-pack's new pack dir absolute path>",
  "pack_total_bytes": ...,
  "manifest_bytes": ...,
  "sqlite_bytes": ...,
  "vector_bytes": ...,
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
    "artifacts/cp9_stage1_reparity_20260419_183440",
    "<R-pack's new pack dir>"
  ],
  "rcv4_note": "Built at HEAD with R-cls (e07d4e7) + R-eng (1f76ccf) + R-pack (<sha>). RC v4 candidate substrate for S2-rerun.",
  "artifacts": [...]
}
```

Also write a fresh `device_identity_matrix.json` mirroring the
S1.1 reparity shape but with the new APK / pack values.

## Boundaries

- No code changes anywhere. Pure provisioning.
- No re-export of the mobile pack — use R-pack's output as-is.
- No changes to model files on any serial.
- No commits. Artifact-only slice (S1 / S1.1 pattern).
- Do not edit any tracker, queue, or dispatch markdown.

## Acceptance

- `pack_build.json` exists at the new artifact dir with
  `apk_sha_homogeneous: true`, `pack_sha_homogeneous: true`,
  `matrix_homogeneous: true`.
- All four `pack_install_<serial>.json` report `installed_ok:
  true` and `badge_observed: true`.
- All four `apk_sha_<serial>.json` report `match_built: true`.
- The built APK sha differs from `88d0041e...` (proves R-cls /
  R-eng made it into the build).
- `git status` shows only new `artifacts/` paths plus pre-existing
  unrelated dirty state.

## Report format

Reply with:
- Built APK sha (Step 1).
- Per-serial installed APK sha (4 lines).
- Per-serial pack push timestamp (4 lines).
- Pack sha (R-pack's new pack).
- Path to new `pack_build.json`.
- Any anomaly (e.g. APK sha didn't change, PACK READY took two
  tries on a serial, etc.).
- Delegation log (likely "none — all main inline; device state
  coupling").
- Explicit "ready for S2-rerun" flag or "S2-rerun blocked —
  reason".
