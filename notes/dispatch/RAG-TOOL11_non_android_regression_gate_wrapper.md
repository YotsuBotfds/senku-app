# RAG-TOOL11 - Non-Android regression gate wrapper

## Scope

Small command-saving wrapper for the non-Android regression gate described in
`notes/dispatch/RAG-TOOL7_non_android_regression_gate_recipe.md`. It keeps the
partial/router holdout pack and optional high-liability compound pack on the
existing repo tools while avoiding hand-built output paths.

## Usage

Dry-run the default fast gate and inspect the exact commands:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_non_android_regression_gate.ps1 -Label <label> -WhatIf
```

Run the default partial/router structural plus retrieval-only gate:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_non_android_regression_gate.ps1 -Label <label>
```

Run the prompt-expectation-only structural gate when you need a fast offline
check with no retrieval index, FastEmbed service, or generated-output
diagnostics:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_non_android_regression_gate.ps1 -Mode Structural -Label <label>
```

Include the optional safety-critical EVAL6 retrieval checks when the edit
touches high-liability guide language, routing, owner hints, or prompt
expectations:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_non_android_regression_gate.ps1 -Label <label> -IncludeSafetyCritical
```

Analyze a generated-output bench JSON and compare the resulting diagnostics
against the current `gd397_expectation_cleanup` baseline:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_non_android_regression_gate.ps1 -Mode Generated -Label <label> -GeneratedBenchJson artifacts\bench\rag_eval_partial_router_holdouts_20260425_<label>.json -FailOnGeneratedRegression
```

By default, the partial/router retrieval-backed expectation check uses
`--fail-on-warnings`. Add `-AllowRetrievalWarnings` only for an accepted
clarify/abstain warning that is documented outside the wrapper.

## Validation

Focused validation for this wrapper:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile tests\test_run_non_android_regression_gate.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_run_non_android_regression_gate -v
powershell -NoProfile -Command '$errors = $null; [System.Management.Automation.PSParser]::Tokenize((Get-Content -Raw .\scripts\run_non_android_regression_gate.ps1), [ref]$errors) | Out-Null; if ($errors) { $errors | ForEach-Object { Write-Error $_ }; exit 1 }'
git diff --check
```
