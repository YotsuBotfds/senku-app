# RAG-A14h Android Installed-Pack Summary Probe

Date: 2026-04-26

## Scope

Add installed mobile-pack metadata to the Android instrumented UI smoke
`summary.json`. This is harness/proof metadata only; it does not change Android
runtime behavior, pack install behavior, product UI, or reviewed-card runtime
exposure.

## Changed Surface

- `scripts/run_android_instrumented_ui_smoke.ps1`

## Summary Field

The wrapper now probes `files/mobile_pack/senku_manifest.json` via
`run-as com.senku.mobile` and emits `installed_pack` with:

- probe status;
- manifest path;
- pack format/version/generated timestamp;
- manifest counts, including answer-card counts when present;
- sqlite manifest bytes/SHA and live app-sandbox listing signature;
- vector manifest bytes/SHA and live app-sandbox listing signature.

The probe intentionally avoids hashing or copying the large sqlite/vector files.

## Validation

Parser / quality gates:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_powershell_quality_gate.ps1 `
  -Path scripts\run_android_instrumented_ui_smoke.ps1 `
  -SkipAnalyzer `
  -SkipPester
# Parser gate passed for 1 PowerShell file(s).

& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_powershell_quality_gate -v
# Ran 8 tests - OK
```

Live wrapper probe:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device emulator-5556 `
  -TestClass com.senku.mobile.DeveloperPanelRuntimeToggleTest `
  -SmokeProfile custom `
  -Orientation portrait `
  -SkipBuild `
  -ArtifactRoot artifacts/android_installed_pack_summary_probe `
  -SummaryPath artifacts\android_installed_pack_summary_probe\summary_field_probe.json `
  -CaptureLogcat `
  -ClearLogcatBeforeRun
# OK (1 test); wrapper status pass.
```

Probe artifact root:

- `artifacts/android_installed_pack_summary_probe/20260426_233110_533/emulator-5556/`

Observed `installed_pack` shape:

- `status`: `available`
- `pack_format`: `senku-mobile-pack-v2`
- `pack_version`: `2`
- `answer_cards`: `6`
- `sqlite.listing_signature`: live app-sandbox `ls -l`
- `vectors.listing_signature`: live app-sandbox `ls -l`

## Boundary

This makes clean-install/no-push and pushed-pack proof easier to audit. It does
not prove that broader desktop card inventory is Android runtime-ready, and the
reviewed-card runtime remains developer/test scoped and default `off`.
