# Android Current-Head Pack Guards - 2026-04-27

Use this note when running Android guards that require the pushed current-head
mobile pack, not the checked-in six-card asset pack.

## Boundary

These guards prove Android can read and constrain a pushed 271-card current-head
pack. They do not replace checked-in Android assets, expand runtime card
selection, product-enable reviewed-card runtime, or turn the runtime on by
default.

Missing or non-current packs intentionally make the current-head tests skip.
That is expected on clean-install lanes.

## Expected Pack

Current export:

`artifacts/mobile_pack/senku_current_head_20260426_232032`

Expected manifest inventory:

- `answer_cards=271`
- `guides=754`
- `chunks=49841`
- `deterministic_rules=9`
- `guide_related_links=5750`
- `retrieval_metadata_guides=237`

Expected files:

- SQLite bytes: `290738176`
- SQLite sha256: `bca1dc3d6de3e8ecd4d2ac585b97e4914974cb6d6889443a313646f295d686c5`
- Vectors bytes: `76555808`
- Vectors sha256: `5c4decacbf506b31acf8ae1d2568771be24004c46c96944456c8d33b7948eeb1`

## Prepare A Device

Install the debug app/test package without clearing app-private files unless the
package is absent:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device emulator-5556 `
  -TestClass com.senku.mobile.DeveloperPanelRuntimeToggleTest `
  -SmokeProfile custom `
  -Orientation portrait `
  -SkipBuild `
  -ArtifactRoot artifacts/android_current_head_guard_install_probe_5556 `
  -SummaryPath artifacts\android_current_head_guard_install_probe_5556\summary.json
```

Push the current-head pack:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\push_mobile_pack_to_android.ps1 `
  -Device emulator-5556 `
  -PackDir artifacts\mobile_pack\senku_current_head_20260426_232032 `
  -ForceStop `
  -ShowInstalledManifest
```

## Run Guards

Matrix run, useful for broad smoke but allowed to skip on lanes without the
pushed pack:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:connectedDebugAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardCurrentHeadPackCensusTest,com.senku.mobile.AnswerCardRuntimeAllowlistCurrentHeadTest,com.senku.mobile.PackMigrationInstallTest" `
  --console=plain
```

Direct proof on a lane after pushing the current-head pack:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb -s emulator-5556 shell am instrument -w `
  -e class com.senku.mobile.AnswerCardCurrentHeadPackCensusTest,com.senku.mobile.AnswerCardRuntimeAllowlistCurrentHeadTest,com.senku.mobile.PackMigrationInstallTest `
  com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
```

Expected direct proof result:

`OK (3 tests)`

Fresh direct proof, 2026-04-27 00:40 CT:

- `emulator-5556` current-head pack restored.
- Direct `am instrument` for
  `AnswerCardCurrentHeadPackCensusTest`,
  `AnswerCardRuntimeAllowlistCurrentHeadTest`, and
  `PackMigrationInstallTest` passed: `OK (3 tests)`.

Repaired lane evidence, 2026-04-27 00:48-00:49 CT:

- `emulator-5554` install-probe artifact
  `artifacts/android_current_head_guard_install_probe_5554/summary.json`
  reports `status=pass`, `platform_anr=null`, requested portrait settled, and
  instrumentation `OK (1 test)`. Direct current-head guard proof also passed:
  `OK (3 tests)`.
- `emulator-5558` install-probe artifact
  `artifacts/android_current_head_guard_install_probe_5558/summary.json`
  reports `status=pass`, `platform_anr=null`, requested portrait settled, and
  instrumentation `OK (1 test)`. Direct current-head guard proof also passed:
  `OK (3 tests)`.
- `emulator-5560` has an install-probe artifact at
  `artifacts/android_current_head_guard_install_probe_5560/summary.json`, but
  it records `platform_anr` from the Android System UI dialog. After the pack
  was re-pushed separately, direct current-head guard proof passed:
  `OK (3 tests)`. Do not count this as clean phone-landscape UI proof; it only
  proves the installed pack and guard classes.

Matrix state-pack evidence, 2026-04-27 00:51-00:54 CT:

- Four-role state pack:
  `artifacts/ui_state_pack_current_head_20260427_matrix/20260427_005131/`.
- Rollup status: `partial`; pass count `31 / 45`.
- `phone_portrait` on `emulator-5556`: `11 / 11`.
- `tablet_portrait` on `emulator-5554`: `10 / 11`; only
  `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` failed because the
  destination page did not preserve cross-reference context.
- `tablet_landscape` on `emulator-5558`: `10 / 11`; same linked-guide handoff
  context failure.
- `phone_landscape` on `emulator-5560`: `0 / 12`; blocked by System UI ANR and
  early instrumentation failures. Treat this as a lane/platform blocker, not a
  current-head pack failure.

## Guard Classes

- `AnswerCardCurrentHeadPackCensusTest`: inventory, required fields,
  relationship integrity, source presence, and no orphan answer-card rows.
- `AnswerCardRuntimeAllowlistCurrentHeadTest`: six pilot runtime cards still
  plan, while deterministic non-pilot current-head samples do not satisfy any
  of the six runtime planner hooks.
- `PackMigrationInstallTest`: normal app install keeps a usable pushed
  current-head pack when checked-in app assets are older.

## Stop Lines

- Do not count skipped current-head tests as proof that a device has the pushed
  current-head pack.
- Do not use these guards to approve card expansion or product exposure.
- Do not assume duplicate guide ownership is invalid; duplicate ownership is
  allowed by current pack semantics.
- Do not use destructive force-refresh instrumentation; that path was removed
  from the guard lane.
- Re-push the pack after any command that reinstalls, clears, or removes the app.
- Phone-landscape reviewed-card UI proof remains blocked by Android System UI
  ANR until a clean landscape canary runs without `platform_anr`.
