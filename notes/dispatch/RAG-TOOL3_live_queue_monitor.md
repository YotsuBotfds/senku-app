# RAG-TOOL3 Live Queue Monitor

## Why This Slice

Concurrent work is moving through dispatch notes, bench artifacts, and git
status quickly. A local browser monitor gives agents a cheap shared dashboard
without asking the main RAG merge lane to keep repeating status checks.

## Scope

Add a stdlib-only local webapp under `scripts/` that serves:

- an HTML dashboard at `/`
- a JSON snapshot at `/status.json`
- git status summary and latest commits
- active dispatch and tooling note pointers
- newest bench diagnostics and artifacts
- a snapshot timestamp

## Usage

```powershell
& .\.venvs\senku-validate\Scripts\python.exe scripts\live_queue_monitor.py --host 127.0.0.1 --port 8765
```

Open `http://127.0.0.1:8765/`. The browser fetches fresh JSON about every 20
seconds.

## Guardrails

- Do not touch active guide files as part of this tooling slice.
- Do not edit `notes/dispatch/RAG-EVAL4_partial_router_followup_priorities.md`.
- Do not edit `notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md`.
- Keep monitor tests server-independent where possible.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_live_queue_monitor -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\live_queue_monitor.py tests\test_live_queue_monitor.py
```

## 2026-04-25 Curie Hardening Proof

- JSON git status now reports both raw and actionable status. The protected
  `notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md` path is surfaced under
  `git.status.benign_untracked` and does not make actionable status dirty.
- JSON git summary now includes `head_full` alongside the short `head`.
- `/status.json` now includes a small `cp9` block sourced from
  `notes/CP9_ACTIVE_QUEUE.md`: active queue snippet plus recent RAG landed
  lines from the active section.
- The HTML dashboard keeps the stdlib/minimal shape and adds only a compact
  CP9 / RAG Queue panel plus the benign-handoff line in Git Status.

Validation run:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_live_queue_monitor -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\live_queue_monitor.py tests\test_live_queue_monitor.py
```

Result: `9` focused monitor tests passed, and `py_compile` passed.
