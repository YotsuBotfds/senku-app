# PowerShell Analyzer Debt Plan - 2026-04-26

Scope: small warning burn-down for repo PowerShell scripts, not global analyzer enablement. Own this note only; do not edit `PLANNER_HANDOFF*` files for this lane.

## Current Baseline

- Existing quality wrapper: `scripts/run_powershell_quality_gate.ps1`.
- Current default selection: `scripts`, `tests\powershell`.
- Dry run on 2026-04-26 selected 56 PowerShell files.
- Parser-only gate passed for those 56 files.
- `tests.test_powershell_quality_gate` passed.
- `PSScriptAnalyzer` is not installed in this environment, so analyzer findings are not yet a stable baseline.
- Existing settings include `Severity = @('Error', 'Warning')` and exclude `PSAvoidUsingWriteHost`.

## Priority Order

1. Keep parser safety first.
   - Before changing any PowerShell file, run the parser-only gate on the touched file or directory.
   - This remains the hard local smoke check even while analyzer is optional.

2. Establish a local analyzer inventory, not an enforced gate.
   - Install or enable `PSScriptAnalyzer` only in the validation environment.
   - Run analyzer against one small path slice at a time.
   - Record warning counts by file/rule before fixing anything.

3. Burn down low-risk warnings first.
   - Prefer mechanical, behavior-preserving fixes: unused variables, unapproved verbs where rename blast radius is tiny, ambiguous aliases, missing explicit parameters.
   - Defer warnings in Android/emulator launcher scripts unless that lane is explicitly active.
   - Do not change operator behavior, device state handling, long-running job orchestration, or validation semantics just to satisfy analyzer style.

4. Protect workflow wrappers.
   - Start with `scripts/run_powershell_quality_gate.ps1`, `scripts/run_windows_validation.ps1`, `scripts/run_validation_slice.ps1`, `scripts/run_non_android_regression_gate.ps1`, and `tests\powershell\Run-OvernightQueueWrapperTests.ps1`.
   - For `scripts/run_guide_prompt_validation.ps1`, use parser/analyzer inspection first; make edits only when warnings are clearly local and validation commands remain unchanged.

5. Gate expansion only after warnings are small and understood.
   - Keep `-RequireAnalyzer` out of default workflows until analyzer is available in normal Windows validation and the warning inventory is intentionally near zero.
   - Avoid broad settings churn. Prefer fixing warnings over suppressing them; use targeted suppressions only with a short reason.

## Safe Validation Commands

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_powershell_quality_gate.ps1 -WhatIf
```

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_powershell_quality_gate.ps1 -SkipAnalyzer -SkipPester
```

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_powershell_quality_gate -v
```

Optional analyzer discovery, once `PSScriptAnalyzer` is installed:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_powershell_quality_gate.ps1 -Path .\scripts\run_powershell_quality_gate.ps1 -SkipParser -SkipPester
```

Optional Pester check, once `Pester` is installed:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_powershell_quality_gate.ps1 -Path .\scripts\run_overnight_queue_wrapped.ps1,.\tests\powershell -SkipAnalyzer -RequirePester
```

## Stop Conditions

- Stop and record the finding if an analyzer fix changes command invocation, process lifetime, Android device state, path resolution, queue notes, or validation outputs.
- Stop if a warning requires editing protected planner handoff files.
- Stop if analyzer availability differs between local and CI; keep parser/tests as the only required checks until the environment is aligned.
