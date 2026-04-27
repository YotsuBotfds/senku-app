# RAG-A14m - Android Prompt Wrapper ANR Evidence

Date: 2026-04-26

## Scope

Surface A14j/A14l platform-ANR evidence from the higher-level
`run_android_prompt.ps1` wrapper.

Harness-only change. No app runtime, UI, pack, asset, or product exposure
behavior changed.

## Change

`run_android_prompt.ps1` now reads the instrumentation summary file after a
nonzero instrumented execution exit. If the summary includes `failure_reason` or
`platform_anr` fields, the wrapper includes them in the thrown error:

`Instrumentation execution failed (failure_reason=...; platform_anr.reason=...; platform_anr.dump=...)`

This keeps the short prompt wrapper from hiding the exact System UI blocker that
the lower-level smoke wrapper already captured.

## Validation

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_powershell_quality_gate.ps1 -Path scripts\run_android_prompt.ps1 -SkipAnalyzer -SkipPester
# Parser gate passed for 1 PowerShell file(s).

& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_powershell_quality_gate -v
# Ran 12 tests - OK
```
