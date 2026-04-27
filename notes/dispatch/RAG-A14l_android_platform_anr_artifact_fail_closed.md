# RAG-A14l - Android Platform ANR Artifact Fail-Closed

Date: 2026-04-26

## Scope

Tighten the Android smoke wrapper after the phone-landscape proof attempt showed
that copied XML artifacts can contain a platform ANR dialog even when the
instrumentation test body itself reports `OK`.

Harness-only change. No app runtime, layout, pack, product exposure, or checked-in
asset behavior changed.

## Change

`run_android_instrumented_ui_smoke.ps1` now fails immediately when copied XML dump
artifacts contain the Android platform ANR dialog:

`System UI isn't responding`

This fail-closed check runs before the generic instrumentation failure handling
and before `summaryStatus` can be set to `pass`.

## Evidence

The attempted phone-landscape reviewed-card proof on `emulator-5560` remained
blocked by the same platform dialog. A14j correctly classified the scripted
prompt failure:

- `status=fail`
- `failure_reason=Android platform ANR dialog blocked run...`
- `platform_anr.detected=true`
- `installed_pack.counts.answer_cards=271`

The preceding developer-panel install probe also copied dumps containing the
same dialog while instrumentation reported `OK`, which is why this stricter
artifact-level fail-closed behavior is needed.

## Validation

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_powershell_quality_gate.ps1 -Path scripts\run_android_instrumented_ui_smoke.ps1 -SkipAnalyzer -SkipPester
# Parser gate passed for 1 PowerShell file(s).

& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_powershell_quality_gate -v
# Ran 11 tests - OK
```
