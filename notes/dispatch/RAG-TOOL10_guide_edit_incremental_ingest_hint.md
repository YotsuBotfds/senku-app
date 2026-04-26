# RAG-TOOL10 - Guide edit incremental ingest hint

## Scope

Small follow-up to the guide edit tooling lane. The goal is to reduce
next-agent manual analysis after guide edits by routing the existing impact
helper through the incremental ingest planner instead of suggesting a broad
`ingest.py` command.

## Usage

After editing guides, ask the impact helper for the focused workflow:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\guide_edit_impact.py --from-git-status
```

For explicit paths:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\guide_edit_impact.py guides\first-aid.md
```

When guide markdown is present, the first validation command now calls:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\plan_incremental_ingest.py <touched guides>
```

Run the printed planner command, then run the `ingest.py --files ...` and
`ingest.py --stats` commands that the planner emits. Keep full rebuilds for
deleted guide paths, missing stores, or ingest plumbing changes that need
unchanged guides re-embedded.

## Validation

Focused validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\guide_edit_impact.py scripts\plan_incremental_ingest.py tests\test_guide_edit_impact.py tests\test_plan_incremental_ingest.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_guide_edit_impact tests.test_plan_incremental_ingest -v
```
