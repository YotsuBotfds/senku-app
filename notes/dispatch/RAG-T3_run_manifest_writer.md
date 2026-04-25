# RAG-T3 Run Manifest Writer

## Slice

Repo-local manifest writer for ignored bench, smoke, and diagnostic artifacts.

## Role

Tooling lane. This closes the repeated gap where local artifacts under
`artifacts/tmp/`, `artifacts/bench/`, or other ignored paths exist without a
durable record tying them to the slice, commands, validation, and commit.

## Outcome

Adds `scripts/write_run_manifest.py`, a stdlib-only CLI that appends JSONL
records to `artifacts/runs/run_manifest.jsonl` by default.

Example:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\write_run_manifest.py --task RAG-S16c --lane retrieval-shadow --role main --model gpt-5.5 --label ez-owner-family --command "scripts\summarize_shadow_comparisons.py ..." --output artifacts\bench\contextual_shadow_compare_ez_newborn_20260425 --metric rows=6 --validation "tests passed"
```

Each record includes:

- task, lane, role, model, and label;
- repeatable commands, inputs, outputs, changed files, validations, metrics, and
  notes;
- commit, generated timestamp, cwd, `git status --short`, dirty flag, and
  best-effort `git diff --stat`.

## Boundaries

- No production retrieval, query, guide, Android, or benchmark behavior changes.
- No new dependencies.
- Manifest output stays ignored under `artifacts/runs/`.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\write_run_manifest.py tests\test_write_run_manifest.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_write_run_manifest -v
```

## Next

Use this after local smokes and shadow comparisons, especially when artifacts
are not committed. Future runners can call the writer automatically once the
common bench commands settle.
