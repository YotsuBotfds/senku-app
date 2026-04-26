# RAG-TOOL6 - Incremental ingest planner

## Scope

Worker Benacerraf added a dry-run planner for the existing incremental guide
ingest path. The goal is to reduce next-agent command drift after guide edits
without changing `ingest.py` or mutating Chroma, lexical index, or manifest
data.

## Tool

Use:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\scripts\plan_incremental_ingest.py
```

With explicit paths:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\scripts\plan_incremental_ingest.py guides\water-purification.md notes\specs\rag_prompt_expectations_seed_20260424.yaml
```

For ingest plumbing changes that need unchanged guides re-embedded:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\scripts\plan_incremental_ingest.py --force-files guides\water-purification.md
```

The planner prints exact PowerShell commands for:

- `ingest.py --files ...` for changed guide markdown files;
- `ingest.py --stats` after each incremental batch;
- `ingest.py --rebuild` when a deleted guide markdown path means
  incremental ingest cannot remove stale records safely.

It also warns when changed files are specs or ordinary non-guide files, because
`ingest.py --files` only re-indexes guide markdown under `guides/`.

## Notes

- This is report-only. It does not import Chroma, call embedding endpoints, or
  write any ingest artifact.
- It mirrors the current ingest contract: incremental mode requires an
  existing collection and a healthy embedding endpoint, skips unchanged SHA
  matches unless `--force-files` is used, then deletes/re-adds records by guide
  id in Chroma and the lexical index.
- Spec changes still need their own validators or pack rebuilds. The planner
  deliberately does not guess those downstream lanes.

## Validation

Focused validation passed:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\plan_incremental_ingest.py tests\test_plan_incremental_ingest.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_plan_incremental_ingest -v
```
