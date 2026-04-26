# RAG-TOOL9 Worker Orchestration Monitoring

## Why This Slice

Multiple workers can be queued against the same repo while the planner needs a
cheap way to decide who is active, what changed, and whether there is anything
actionable to interrupt. Use the existing live monitor and context snapshot as
the first read instead of spending chat tokens re-running ad hoc status checks.

## Operator Loop

1. Confirm baseline before reading worker output.

```powershell
git rev-parse --short HEAD
git status --short
```

Expected fast-mode baseline is `980154f`. Treat
`notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md` as benign/protected when it is
the only untracked file.

2. Open the live monitor for a low-token shared dashboard.

```powershell
& .\.venvs\senku-validate\Scripts\python.exe scripts\live_queue_monitor.py --host 127.0.0.1 --port 8765
```

Use `http://127.0.0.1:8765/status.json` for machine-readable status or
`http://127.0.0.1:8765/` for the browser view. The useful fields are:

- `git.head` / `git.head_full`: current commit anchor.
- `git.status.clean`: actionable worktree cleanliness after protected benign
  paths are filtered.
- `git.status.raw_clean`: literal `git status --short` cleanliness.
- `git.status.entries`: actionable dirty files to route to the owning worker.
- `git.status.benign_untracked`: protected handoff files that should not page a
  worker by themselves.
- `queues.active`: recently edited dispatch notes.
- `queues.tooling`: current tooling-oriented slices.
- `cp9.active_snippet`: compact active queue context.
- `cp9.rag_landed`: recent RAG landings from the active queue.
- `bench.diagnostics`: newest diagnostic artifacts to inspect after validation.

3. Use the startup snapshot when assigning a new worker.

```powershell
& .\.venvs\senku-validate\Scripts\python.exe scripts\agent_context_snapshot.py --repo-root . --max-lines 120
```

The snapshot is a pasteable startup brief: HEAD, latest commits, visible
worktree status, active dispatch pointers, run-manifest pointers, and bench
artifact pointers. Prefer it when the next worker needs orientation but not the
full live dashboard.

4. Triage status with ownership first.

- If `git.status.clean` is true and `raw_clean` is false, the only visible
  changes are protected benign files; keep dispatching normally.
- If `git.status.entries` contains files outside a worker's declared write
  scope, pause that worker before asking another worker to touch nearby files.
- If a dispatch note was recently edited but no corresponding artifact appears,
  ask the owning worker for validation status instead of assigning overlap.
- If a bench diagnostic appears without a clean worktree, record both facts in
  the handoff so the validator knows whether the artifact reflects committed
  code, local edits, or another worker's patch.

## Guardrails

- Do not modify or track
  `notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md`.
- Do not inspect or modify other workers' owned files unless the planner
  explicitly reassigns them.
- Keep queue semantics unchanged; this slice is orchestration visibility only.
- Prefer docs or small monitor display changes over broad UI churn.

## Validation

Docs-only changes need:

```powershell
git diff --check
```

If `scripts/live_queue_monitor.py` changes, also run:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_live_queue_monitor -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\live_queue_monitor.py tests\test_live_queue_monitor.py
```
