# Senku Mobile Pack Test App

This Android subproject packages the exported offline Senku mobile pack into the field-manual-style debug app used for local testing, review packs, and parity validation.

## Current Scope

The app currently:

- bundles the 271-card current-head Android asset pack under `app/src/main/assets/mobile_pack`
- installs the pack into app-local storage on first launch
- parses the manifest and vector header
- opens the packaged SQLite database locally
- browses guides offline
- searches chunks offline with:
  - lexical FTS seeding
  - vector-assisted reranking/expansion from `senku_vectors.f16`
  - `LIKE` fallback if FTS is unavailable
- imports a local `.litertlm` or `.task` model into app storage
- builds a compact offline prompt from retrieved Senku context
- runs local Gemma inference through LiteRT-LM
- supports search, ask, recent-thread continuity, and guide/detail review flows
- renders the current field-manual UI across phone/tablet portrait and landscape postures

This is now an inference-integrated debug build.

## Current Validation Baseline

Use the fixed emulator matrix as the default posture proof:

- `emulator-5556` = phone portrait
- `emulator-5560` = phone landscape
- `emulator-5554` = tablet portrait
- `emulator-5558` = tablet landscape

Practical rule:
- use emulators for the day-to-day validation loop
- use physical phone/tablet checks as milestone truth checks and live mirror/review lanes

Recommended launcher from the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start_senku_emulator_matrix.ps1
```

Notes:
- default mode is read-only so each emulator session stays disposable
- app installs, pushed packs, and imported models remain available during the live session, but they do not survive a full emulator restart in read-only mode
- use `-Headless` for no-window launches, and `-PartitionSizeMb <mb>` only when intentionally starting a large-data AVD lane
- use `-WhatIf` to review selected lanes and concrete emulator arguments before launching

Before a long four-posture UI state pack, preflight the selected roles and launcher commands without starting jobs:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 `
  -PlanOnly `
  -RoleFilter phone_portrait,tablet_landscape `
  -SkipBuild `
  -SkipInstall `
  -SkipHostStates
```

Plan-only artifact shape:
- `plan.json` is written under the selected output root and dated run folder.
- `preflight_only=true`, `plan_only=true`, `non_acceptance_evidence=true`,
  and `acceptance_evidence=false` mark it as planning output only.
- `migration_checklist_intent` repeats selected roles, host model identity,
  host/skip flags, parallelism, and the same preflight/non-acceptance fields.
- `launchers` records the per-role commands that would be started by a real
  state-pack run.

Replay the current Android FTS4 fallback proof. This is fallback-path emulator evidence, not FTS5 runtime proof:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_fts_fallback_matrix.ps1 `
  -OutputDir artifacts\bench\android_fts_fallback_matrix_current
```

FTS fallback artifact shape:
- `summary.json` remains the machine-readable matrix result.
- `summary.md` is the concise reviewer summary with
  `runtime_evidence=fts4_fallback`, `not_fts5_runtime_proof=true`, device
  counts, lock posture, adb version, and the paired `summary.json` path.

Optional Gradle Managed Devices tasks are hidden behind an explicit property
gate:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:tasks --all '-Psenku.enableManagedDevices=true' --console=plain
```

Dry-run the first wrapper slice from the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_managed_device_smoke.ps1 -DryRun
```

This scaffold is only a future parallel smoke lane. It does not replace the
fixed four-emulator screenshot/state-pack evidence above.
ATD images are not screenshot-sensitive proof for acceptance artifacts.
The wrapper is currently dry-run-only and records `non_acceptance_evidence=true`;
fixed four-emulator evidence remains primary.

Dry-run artifact shape:
- `summary.json` and `summary.md` are written under the selected output
  directory.
- `status=dry_run_only`, `dry_run=true`, `non_acceptance_evidence=true`, and
  `acceptance_evidence=false` identify the artifact as planning evidence only.
- `planned_command`, `gradle_property`, `task_name`, `expected_devices`, and
  `expected_artifact_roots` describe what a future managed-device smoke would
  use.
- `would_launch_emulators=false` and `managed_devices_launched=false` confirm
  the dry run did not start a managed-device lane.
- `primary_evidence=fixed_four_emulator_matrix` restates the acceptance stop
  line: this helper does not replace fixed four-emulator screenshot/state-pack
  evidence.

Dry-run the Android asset-pack parity gate before running a candidate compare:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_asset_pack_parity_gate.ps1 `
  -CandidatePackDir artifacts\mobile_pack\senku_current_head_20260426_232032 `
  -WhatIf
```

The `-WhatIf` artifact is another non-acceptance helper summary. It records
`baseline_pack_dir`, `candidate_pack_dir`, `output`, `display_command`,
`would_run=false`, and `fail_on_mismatch=true`. It reviews the exact parity
command shape without running the count comparison, and it does not replace the
fixed four-emulator acceptance evidence.

## Build

From this directory:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

Dependency resolution is pinned by Gradle verification metadata at
`gradle/verification-metadata.xml`. Refresh it only when intentionally changing
Android dependencies. The metadata includes the detached Android lint tool
dependencies, but `lintDebug` is not a green validation lane until the existing
lint findings are triaged.

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --write-verification-metadata sha256 :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest --console=plain
```

## APK

Latest debug APK output:

- `app/build/outputs/apk/debug/app-debug.apk`

## Install

If a device is attached:

```powershell
& 'C:\Users\tateb\AppData\Local\Android\Sdk\platform-tools\adb.exe' install -r app\build\outputs\apk\debug\app-debug.apk
```

If the emulator throws a transient signature/certificate parse failure during streamed install, retry with:

```powershell
& 'C:\Users\tateb\AppData\Local\Android\Sdk\platform-tools\adb.exe' install --no-streaming app\build\outputs\apk\debug\app-debug.apk
```

## Instrumented Smoke

The project now includes a first AndroidX instrumentation lane for harness validation.

Run the smoke tests from the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device RFCX607ZM8L
```

Fast repeat check against the current build:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device RFCX607ZM8L `
  -SmokeProfile basic `
  -SkipBuild
```

Run the host-backed generative smoke:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device RFCX607ZM8L `
  -SmokeProfile host `
  -SkipBuild `
  -HostInferenceUrl http://10.0.2.2:1235/v1 `
  -HostInferenceModel gemma-4-e2b-it-litert
```

Run the full phone smoke including follow-up continuity:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device RFCX607ZM8L `
  -SmokeProfile full `
  -SkipBuild `
  -HostInferenceUrl http://10.0.2.2:1235/v1 `
  -CaptureLogcat `
  -ClearLogcatBeforeRun
```

What it currently checks:
- pack install settles
- search flow returns results without shell-polled XML completion logic
- deterministic ask opens the detail screen
- optional host-inference generative ask opens a populated detail screen
- optional host-inference follow-up smoke verifies prior-turn continuity
- posture-focused runs can reuse the same smoke suite with `-Orientation` and `-FontScale`

Artifacts:
- screenshots are copied into `artifacts/instrumented_ui_smoke/<timestamp>/<device>/screenshots`
- XML hierarchy dumps are copied into `artifacts/instrumented_ui_smoke/<timestamp>/<device>/dumps`
- the instrumentation console log is copied into `instrumentation.txt`
- optional device logcat is copied into `logcat.txt`
- a machine-readable `summary.json` is written next to the run artifacts
- treat that `summary.json` as the authoritative contract for wrapper scripts; when richer posture, role, or orientation facts are present there, wrappers should trust them before trying shell-side rechecks
- on physical devices, the runner automatically applies `adb reverse` when the host URL points at `10.0.2.2`

Fast-lane options:
- `-SmokeProfile basic` runs only search + deterministic ask
- `-SmokeProfile host` adds the host-backed generative ask
- `-SmokeProfile full` adds follow-up continuity
- `-SkipBuild` reuses the current built APKs
- `-SkipInstall` skips reinstall when the current app + test APK are already on the device

The tests live under:

- `app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

The legacy shell runner can also use this lane as a preflight:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_prompt.ps1 `
  -Emulator RFCX607ZM8L `
  -Query "How do I start a fire in rain?" `
  -Ask `
  -InferenceMode host `
  -UseInstrumentationPreflight
```

For artifact-friendly single-prompt replay, prefer the logged wrapper:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_prompt_logged.ps1 `
  -Emulator RFCX607ZM8L `
  -Query "How do I start a fire in rain?" `
  -Ask `
  -InferenceMode host `
  -SkipPackPushIfCurrent
```

Use `-SkipPackPushIfCurrent` to reuse the current on-device pack when it matches, or `-ForcePackPush` to refresh it explicitly. Each replay emits the prompt `.json`, UI `.xml`, `.logcat.txt`, and `.pack_push.json` artifacts together for pack-cache auditing.

That preflight now uses the fast instrumentation profile automatically:
- `basic` for normal prompt runs
- `host` for host-inference prompt runs
- and it reuses the current local APK build with `-SkipBuild`

The UI validation pack now defaults to instrumentation-backed execution and consumes the instrumentation summary as artifact truth when available:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_ui_validation_pack.ps1 `
  -Devices RFCX607ZM8L `
  -InferenceMode host
```

If you need to force the legacy prompt runner, keep the instrumentation gate in front of it explicitly:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_ui_validation_pack.ps1 `
  -Devices RFCX607ZM8L `
  -InferenceMode host `
  -ForceShellExecution `
  -UseInstrumentationPreflight
```

Pack-runner notes:
- the trusted fast path is instrumentation-first and fact-driven, not serial shell folklore
- the wrapper copies dumps, screenshots, and logcat from the instrumentation summary when those facts are present
- shell-side screenshot/orientation recovery is now only the fallback path when the richer summary facts are missing

For live device observation while harnesses run, use the repo mirror helper from the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start_senku_device_mirrors.ps1
```

Phone-only relaunch:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start_senku_device_mirrors.ps1 -PhoneOnly
```

## Emulator Note

On the x86_64 Android emulator, Gemma 4 LiteRT models currently work on the CPU path in this app when LiteRT-LM is allowed to use the model/runtime default token budget.

- Do not force `EngineConfig.maxNumTokens=512` on the emulator path. That setting caused LiteRT prefill failures with `DYNAMIC_UPDATE_SLICE`.
- GPU/OpenCL is still not the reliable emulator path here; CPU is the working fallback.
- Uninstalling the app clears the imported-model state. Re-import the model, or copy it back into the app's internal `files/models` directory before testing offline answers again.
- The repo still defaults the host-side LiteRT lane to `E2B`, but the Android import path itself is generic. A real `E4B` device lane requires an actual `gemma-4-E4B-it.litertlm` or `.task` artifact, not just a host model ID.

## Push A LiteRT Model Directly

If you already have a local `E4B` or `E2B` `.litertlm` / `.task` file, you can push it straight into the debug app without using the document picker:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\push_litert_model_to_android.ps1 `
  -Device emulator-5556 `
  -ModelPath C:\path\to\gemma-4-E4B-it.litertlm `
  -RestartApp
```

The helper still uses tmp staging: it pushes the model to `/data/local/tmp`
before copying it into the app's internal `files/models/` directory. It now
preflights `/data` free space and fails early when the device cannot hold both
copies. Use `-SkipDataSpaceCheck` only when you have already verified enough
space or are intentionally testing the failure path.

Use `-DryRun` to review the LiteRT push posture without touching a device. It
prints device, package, resolved model path/size, app target path, tmp staging
requirement, `/data` free-space requirement, `-SkipDataSpaceCheck` posture, and
`Transfer posture: skipped by -DryRun.` Add `-SummaryPath` only when you want a
machine-readable non-acceptance dry-run summary. It does not emit an acceptance
artifact and does not replace fixed four-emulator evidence.

If `-ModelPath` is omitted, the helper looks for these common names in repo root, `models\`, and `Downloads`:

- `gemma-4-E4B-it.litertlm`
- `gemma-4-E4B-it.task`
- `gemma-4-E2B-it.litertlm`
- `gemma-4-E2B-it.task`

To run the fast local-model readiness smoke after pushing a model, use the focused
instrumentation class instead of a full generated-answer validation pack:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device emulator-5556 `
  -TestClass com.senku.mobile.ModelFileStoreImportedModelTest#importedLiteRtModelIsDiscoverableAndNonEmpty `
  -SmokeProfile custom `
  -SkipBuild `
  -SkipInstall
```

Omit `-SkipInstall` once after adding or rebuilding the test APK; `adb install -r`
preserves app data, while uninstalling or clearing data removes the imported
model.

## Next Step

Current priorities:

- keep Android deterministic parity widening against desktop
- improve host-independent/on-device answer resilience
- continue tightening the field-manual identity without regressing posture coverage
- keep the fixed four-lane validation matrix and review gallery current as the UI evolves
