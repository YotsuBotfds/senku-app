# RAG-A14i Android Current-Head Pack Push Probe

Date: 2026-04-26

## Scope

Push the fresh current-head mobile-pack export into an installed Android app
sandbox and prove the installed-pack summary reports that pushed pack.

This is probe/proof metadata only. It does not replace checked-in Android
assets, product-enable reviewed-card runtime, change the default preference, or
promote the full current-head answer-card inventory into Android runtime
selection.

## Preconditions

- `RAG-CH6` exported and compared
  `artifacts/mobile_pack/senku_current_head_20260426_232032/`.
- `RAG-A14h` added `summary.json.installed_pack`.
- Runtime remains developer/test scoped and default `off`.

## Push Proof

Command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\push_mobile_pack_to_android.ps1 `
  -Device emulator-5556 `
  -PackDir artifacts\mobile_pack\senku_current_head_20260426_232032 `
  -ForceStop `
  -ShowInstalledManifest
```

Installed pack verification:

- `290738176 files/mobile_pack/senku_mobile.sqlite3`
- `76555808 files/mobile_pack/senku_vectors.f16`
- `3059 files/mobile_pack/senku_manifest.json`

Installed manifest counts:

- `guides`: 754
- `chunks`: 49,841
- `deterministic_rules`: 9
- `guide_related_links`: 5,750
- `retrieval_metadata_guides`: 237
- `answer_cards`: 271

## Summary Proof

Command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device emulator-5556 `
  -TestClass com.senku.mobile.DeveloperPanelRuntimeToggleTest `
  -SmokeProfile custom `
  -Orientation portrait `
  -SkipBuild `
  -SkipInstall `
  -ArtifactRoot artifacts/android_current_head_pack_probe `
  -SummaryPath artifacts\android_current_head_pack_probe\summary_field_probe.json `
  -CaptureLogcat `
  -ClearLogcatBeforeRun
```

Result:

- `OK (1 test)`
- wrapper `status`: `pass`
- artifact root:
  `artifacts/android_current_head_pack_probe/20260426_233328_389/emulator-5556/`
- `installed_pack.status`: `available`
- `installed_pack.counts.answer_cards`: 271
- sqlite listing showed the materialized 290,738,176-byte pushed DB
- vector listing showed the materialized 76,555,808-byte pushed vector file

## Runtime Canary Attempt

Two reviewed-card prompt canary attempts were run after pushing the current-head
pack:

- `emulator-5556`, phone portrait, via `run_android_prompt.ps1`
- `emulator-5560`, phone landscape, via direct `run_android_instrumented_ui_smoke.ps1`

Both attempts were blocked by an Android platform dialog:

- `System UI isn't responding`
- foreground package became `android`, so `PromptHarnessSmokeTest` correctly
  failed its foreground guard before claiming a screenshot/assertion pass.

The `emulator-5560` logcat still showed Senku opening the poisoning detail with
`firstSource=GD-898`, but the canary is not counted as green because the System
UI ANR occluded the app.

Artifacts:

- `artifacts/instrumented_ui_smoke/20260426_233410_787/emulator-5556/`
- `artifacts/android_current_head_pack_runtime_canary_5560/20260426_233923_664/emulator-5560/`

## Boundary

Do not describe this as clean-install/no-push proof. It is a pushed-pack probe.
Do not claim the 271-card inventory is Android runtime-ready. The checked-in
Android asset pack remains the six-card pilot pack, and `AnswerCardRuntime`
selection remains unchanged.
