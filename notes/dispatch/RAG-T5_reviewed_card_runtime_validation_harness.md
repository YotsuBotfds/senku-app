# RAG-T5 Reviewed-Card Runtime Validation Harness

## Slice

Make reviewed-card runtime-answer enablement visible in guide validation and
RAG diagnostics so future comparison panels do not silently mix runtime modes.

## Outcome

`scripts/run_guide_prompt_validation.ps1` now accepts
`-EnableCardBackedRuntimeAnswers`. The switch scopes
`SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1` around the bench invocation and
restores the caller's previous environment afterward.

`bench.py` records the setting in both `config` and `summary`:

- `reviewed_card_runtime_answers_env`
- `reviewed_card_runtime_answers_enabled`

`scripts/analyze_rag_bench_failures.py` now carries the artifact setting into
`diagnostics.csv`, `diagnostics.json`, and `report.md` as
`artifact_reviewed_card_runtime_answers`.

## Why

The accepted FA/FB/FD/FE panel reached `9.56/10` with reviewed-card runtime
answers enabled. A later FA rerun without the env flag dropped back to generated
RAG behavior, which made the panel look like an answer-quality regression when
it was really a harness comparability problem.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile bench.py scripts\analyze_rag_bench_failures.py tests\test_run_guide_prompt_validation_harness.py tests\test_analyze_rag_bench_failures.py
$text = Get-Content -LiteralPath .\scripts\run_guide_prompt_validation.ps1 -Raw; [scriptblock]::Create($text) | Out-Null
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_run_guide_prompt_validation_harness tests.test_analyze_rag_bench_failures -v
```

Focused validation passed `39` tests.

Fresh FA/FB/FD/FE runtime proof:
`artifacts/bench/rag_diagnostics_20260425_1538_runtime_hardened_all_fresh/report.md`.
The panel stayed at `9.56/10` with `0/24` generated rows, `0` bad buckets, and
`enabled: 24` for artifact reviewed-card runtime answers.
