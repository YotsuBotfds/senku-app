# RAG-A14j - Android Platform ANR Summary Classifier

Date: 2026-04-26

## Scope

Add a harness-only classifier for Android platform ANR dialogs that block
scripted prompt canaries.

This does not auto-dismiss dialogs, change app behavior, change runtime
selection, or product-enable reviewed-card answers.

## Why

`RAG-A14i` proved the current-head 271-card pack can be pushed into Android app
storage and reported by the smoke summary, but the reviewed-card prompt canary
was blocked by the Android platform dialog:

`System UI isn't responding`

The instrumentation failure was valid, but the wrapper summary collapsed the
cause to a generic instrumentation failure. That made migration triage slower.

## Change

`scripts/run_android_instrumented_ui_smoke.ps1` now scans copied XML dump
artifacts for the Android ANR title and `android:id/aerr_*` controls after
artifact copy and before the generic instrumentation failure throw.

When detected, the wrapper records:

- `failure_reason`: `Android platform ANR dialog blocked run: System UI isn't responding; dump=<name>`
- `platform_anr.detected`
- `platform_anr.dump`
- `platform_anr.has_system_ui_title`
- `platform_anr.has_anr_controls`

The top-level status remains `fail`, so existing summary consumers do not need a
new status enum.

## Validation

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_powershell_quality_gate.ps1 -Path scripts\run_android_instrumented_ui_smoke.ps1,scripts\run_android_prompt.ps1 -SkipAnalyzer -SkipPester
# Parser gate passed for 2 PowerShell file(s).

& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_powershell_quality_gate -v
# Ran 11 tests - OK
```

## Follow-Up

Retry the A14i reviewed-card prompt canary after device cleanup. If the platform
ANR recurs, the summary should now preserve the exact blocker instead of only
reporting a generic instrumentation failure.
